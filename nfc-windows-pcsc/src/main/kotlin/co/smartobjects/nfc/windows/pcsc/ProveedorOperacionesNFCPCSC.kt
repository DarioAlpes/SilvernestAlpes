package co.smartobjects.nfc.windows.pcsc

import co.smartobjects.entidades.clientes.Cliente
import co.smartobjects.nfc.ProveedorOperacionesNFC
import co.smartobjects.nfc.ResultadoNFC
import co.smartobjects.nfc.excepciones.NFCProtocolException
import co.smartobjects.nfc.operacionessobretags.*
import co.smartobjects.nfc.tags.mifare.ultralight.UltralightC
import co.smartobjects.nfc.tags.mifare.ultralight.UltralightEV1
import co.smartobjects.nfc.windows.pcsc.lectores.pcsc.LectorPCSC
import co.smartobjects.nfc.windows.pcsc.lectores.pcsc.PCSCTag
import co.smartobjects.nfc.windows.pcsc.lectorestags.ultralightc.LectorPCSCUltralightC
import co.smartobjects.nfc.windows.pcsc.lectorestags.ultralightev1.LectorPCSCUltralightEV1
import co.smartobjects.persistencia.clientes.RepositorioClientesSQL
import co.smartobjects.persistencia.clientes.llavesnfc.RepositorioLlavesNFC
import co.smartobjects.persistencia.clientes.llavesnfc.RepositorioLlavesNFCSQL
import co.smartobjects.persistencia.sqlite.ConfiguracionPersistenciaSQLiteEnMemoria
import co.smartobjects.utilidades.aHexString
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.PublishProcessor
import io.reactivex.rxkotlin.combineLatest
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.*
import javax.smartcardio.CardException
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.properties.Delegates

class ProveedorOperacionesNFCPCSC constructor(private val idCliente: Long, private val repositorioLlavesNFC: RepositorioLlavesNFC)
    : ProveedorOperacionesNFC, CoroutineScope
{
    companion object
    {
        private const val MENSAJE_LECTOR_SUSPENDIDO = "Suspendido"
        private const val MENSAJE_LECTOR_PROCESANDO = "Procesando..."
        private const val MENSAJE_TAG_NO_SOPORTADO = "Tag no soportado"
        private const val MENSAJE_FINALIZADO = "Finalizado"
    }

    private lateinit var proveedorLlaves: ProveedorLlaves

    private val jobNFC: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = jobNFC + Dispatchers.IO

    private var jobHijoActual: Job? = null

    private var continuacionLecturaNFC: Continuation<Unit>? = null

    override var permitirLecturaNFC by Delegates.observable(false)
    { _, valorAntiguo, valorNuevo ->
        synchronized(jobNFC) {
            if (valorNuevo && valorNuevo != valorAntiguo)
            {
                continuacionLecturaNFC?.resume(Unit)
            }
        }
    }

    private val proveedorDeLlavesInicializado by lazy(mode = LazyThreadSafetyMode.NONE) {
        intentarInicializarProveedorLlaves()
    }

    private var ultimoTagPCSC: PCSCTag<*>? = null
    private lateinit var lector: LectorPCSC

    private val eventosErrorLector = PublishSubject.create<Throwable>()
    override val errorLector: Observable<Throwable> = eventosErrorLector.hide()

    private val eventosLecturasNFC = PublishProcessor.create<ResultadoNFC>()
    override val resultadosNFCLeidos: Flowable<ResultadoNFC> = eventosLecturasNFC.hide()

    private val eventosHayLectorConectado = BehaviorProcessor.createDefault(false)
    override val hayLectorConectado: Flowable<Boolean> = eventosHayLectorConectado.distinctUntilChanged()

    private val eventosListoParaLectura = BehaviorProcessor.createDefault(false)
    override val listoParaLectura: Flowable<Boolean> =
            eventosListoParaLectura.combineLatest(hayLectorConectado)
                .map { it.first && it.second && permitirLecturaNFC }
                .distinctUntilChanged()

    private suspend fun suspenderLecturaNFC() = suspendCoroutine<Unit> { continuacionLecturaNFC = it }

    @Synchronized
    fun intentarConectarseALector(): Boolean
    {   println("\nintentarConectarseALector")
        jobHijoActual?.cancel()
        eventosHayLectorConectado.onNext(intentarInicializarLector())
        if (eventosHayLectorConectado.value!!)
        {
            jobHijoActual = launch {
                var milisegundosDesdeUltimaPosibleDesconexionLector = 0L
                var contadorDeExcepciones = 0

                while (proveedorDeLlavesInicializado && eventosHayLectorConectado.value!!
                       && isActive && !jobNFC.isCompleted)
                {
                    if (!permitirLecturaNFC)
                    {
                        lector.mostrarMensajePorControl(MENSAJE_LECTOR_SUSPENDIDO)
                        suspenderLecturaNFC()
                    }

                    if (!isActive || jobNFC.isCompleted)
                    {
                        break
                    }

                    eventosListoParaLectura.onNext(true)

                    val posibleResultadoNFC = esperarTagYDarOperacionesCompuestas(1000)
                    if (posibleResultadoNFC != null)
                    {
                        eventosListoParaLectura.onNext(false)
                        eventosLecturasNFC.onNext(posibleResultadoNFC)

                        when (posibleResultadoNFC)
                        {
                            is ResultadoNFC.Exitoso              ->
                            {
                                with(lector) {
                                    mostrarMensajePorControl(MENSAJE_FINALIZADO)
                                    hacerSonidoExitoPorControl()
                                    esperarDesconexionTag(0)
                                    hacerSonidoTagEncontradoOPerdidoPorControl()
                                }
                                eventosListoParaLectura.onNext(true)
                            }
                            is ResultadoNFC.Error.TagNoSoportado ->
                            {
                                with(lector) {
                                    mostrarMensajePorControl(MENSAJE_TAG_NO_SOPORTADO)
                                    hacerSonidoErrorPorControl()
                                    esperarDesconexionTag(0)
                                }
                                eventosListoParaLectura.onNext(true)
                            }
                            else                                 ->
                            {
                                val milis = System.currentTimeMillis()

                                contadorDeExcepciones++

                                if (contadorDeExcepciones > 15)
                                {
                                    if (milis - milisegundosDesdeUltimaPosibleDesconexionLector < 1000L)
                                    {
                                        eventosHayLectorConectado.onNext(false)
                                        eventosErrorLector.onNext(Exception("Conexión con lector perdida"))
                                    }
                                    else
                                    {
                                        contadorDeExcepciones = 0
                                    }
                                }

                                milisegundosDesdeUltimaPosibleDesconexionLector = milis
                            }
                        }
                    }
                }
            }
        }

        return eventosHayLectorConectado.value!!
    }

    private fun intentarInicializarProveedorLlaves(): Boolean
    {   println("\nintentarInicializarProveedorLlaves")
        return try
        {
            proveedorLlaves = ProveedorLlaves.crearProveedorDeLlaves(idCliente, repositorioLlavesNFC)
            true
        }
        catch (error: LlaveNFCMaestraNoEncontrada)
        {
            eventosErrorLector.onNext(error)
            false
        }
    }

    private fun intentarInicializarLector(): Boolean
    {   println("\nintentarInicializarLector")
        val posibleLectorPCSC =
                try
                {
                    LectorPCSC.darPrimerLector()?.apply {
                        desactivarSonidosPorDefecto()
                    }

                }
                catch (error: NFCProtocolException)
                {
                    eventosErrorLector.onNext(error)
                    null
                }

        if (posibleLectorPCSC != null)
        {   println("\nposibleLectorPCSC: "+posibleLectorPCSC.toString())
            lector = posibleLectorPCSC
            lector.mostrarMensajePorControl("Hola")
            println("\nLector: "+lector.toString())
            lector.hacerSonidoTagEncontradoOPerdidoPorControl()
            lector
        }

        return posibleLectorPCSC != null
    }

    private fun esperarTagYDarOperacionesCompuestas(timeout: Long): ResultadoNFC?
    {
        try
        {
            val lectorActual = lector
            lectorActual.mostrarMensajePorControl("Esperando tag")
            val tagPCSC = lector.esperarTag(timeout)
            @Suppress("FoldInitializerAndIfToElvis")
            if (tagPCSC == null)
            {
                return null
            }

            @Suppress("UNCHECKED_CAST")
            val operacionesTag: OperacionesCompuestas<*, *> =
                    when (tagPCSC.tag)
                    {
                        is UltralightEV1 ->
                        {
                            val lectorTag = LectorPCSCUltralightEV1(lectorActual, tagPCSC as PCSCTag<UltralightEV1>)
                            OperacionesCompuestasUltralightEV1(lectorTag, proveedorLlaves)
                        }
                        is UltralightC   ->
                        {
                            val lectorTag = LectorPCSCUltralightC(lectorActual, tagPCSC as PCSCTag<UltralightC>)
                            OperacionesCompuestasUltralightC(lectorTag, proveedorLlaves)
                        }
                        else             ->
                        {
                            return ResultadoNFC.Error.TagNoSoportado(tagPCSC.tag.darNombre())
                        }
                    }

            lectorActual.mostrarMensajePorTag(MENSAJE_LECTOR_PROCESANDO, tagPCSC)
            lectorActual.hacerSonidoTagEncontradoOPerdidoPorTag(tagPCSC)

            ultimoTagPCSC = tagPCSC

            return ResultadoNFC.Exitoso(operacionesTag)
        }
        catch (e: CardException)
        {
            return ResultadoNFC.Error.ConectandoseAlTag
        }
        catch (e: NFCProtocolException)
        {
            return ResultadoNFC.Error.ConectandoseAlTag
        }
        finally
        {
            try
            {
                ultimoTagPCSC?.tarjeta?.disconnect(true)
            }
            catch (e: CardException)
            {
            }
        }
    }
}

fun main(args: Array<String>)
{
    val configuracionBD = ConfiguracionPersistenciaSQLiteEnMemoria("base_datos_en_memoria")

    val repoClientes = RepositorioClientesSQL(configuracionBD).also {
        it.crearTablaSiNoExiste()
    }
    val cliente = repoClientes.crear(Cliente(null, "Cliente de prueba"))

    repoClientes.inicializarConexionAEsquemaDeSerNecesario(cliente.id!!)

    val repositorioLlavesNFC = RepositorioLlavesNFCSQL(configuracionBD).also {
        it.inicializarParaCliente(cliente.id!!)
        it.crear(cliente.id!!, Cliente.LlaveNFC(1, "0123456789"))
    }

    val proveedor = ProveedorOperacionesNFCPCSC(1, repositorioLlavesNFC)
    proveedor.resultadosNFCLeidos.subscribe(
            {
                when (it)
                {
                    is ResultadoNFC.Exitoso ->
                    {   println("\nResultadoNFC.Exitoso")
                        println("Se encontro tag. UUID: ${it.operacion.darUID().aHexString()}. Tipo: ${it.operacion.javaClass.name}")
                        val leidoInicial = it.operacion.leerTag()
                        if (leidoInicial is ResultadoLecturaNFC.TagLeido)
                        {
                            println("Leido1: ${leidoInicial.valor.aHexString()}")
                        }
                        else
                        {
                            println(leidoInicial)
                        }
                        println("A escribir: ${"123".toByteArray().aHexString()}")
                        it.operacion.escribirTag("123".toByteArray())
                        println("Leido2: ${(it.operacion.leerTag() as ResultadoLecturaNFC.TagLeido).valor.aHexString()}")
                        it.operacion.borrarTag()
                        println("Leido final: ${it.operacion.leerTag()}")
                    }
                    else                    -> println(it)
                }
            },
            {
            })

    proveedor.intentarConectarseALector()

    while (true)
    {
        val leido = readLine()
        if (leido == "exit")
        {
            break
        }
        else if (leido == "r")
        {
            proveedor.permitirLecturaNFC = true
        }
        else if (leido == "s")
        {
            proveedor.permitirLecturaNFC = false
        }
    }
}
