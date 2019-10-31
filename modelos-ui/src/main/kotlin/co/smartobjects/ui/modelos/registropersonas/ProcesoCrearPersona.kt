package co.smartobjects.ui.modelos.registropersonas

import co.smartobjects.entidades.personas.DocumentoCompleto
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.personas.PersonaConFamiliares
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.personas.PersonasAPI
import co.smartobjects.red.modelos.personas.PersonaConFamiliaresDTO
import co.smartobjects.ui.modelos.ModeloUI
import co.smartobjects.ui.modelos.ResultadoAccionUI
import co.smartobjects.utilidades.Opcional
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject

interface ProcesoCrearPersona : ModeloUI
{
    val persona: PersonaUI
    val familiares: Set<Persona>
    val estado: Observable<Estado>
    val errorGlobal: Observable<Opcional<String>>
    val personaCreada: Observable<Persona>
    val debeConsultarPersona: Boolean
    val puedeCrearPersona: Observable<Boolean>

    /**
     * Si se debe consultar la persona por documento llama `intentarConsultarPersonaPorDocumento`, en caso contrario llama `intentarCrearPersona`
     */
    fun intentarCrearPersonaOConsultarPersonaPorDocumentoDeSerNecesario(): ResultadoAccionUI
    {
        return if (debeConsultarPersona)
        {
            intentarConsultarPersonaPorDocumento()
        }
        else
        {
            intentarCrearPersona()
        }
    }

    fun intentarCrearPersona(): ResultadoAccionUI
    fun intentarConsultarPersonaPorDocumento(): ResultadoAccionUI
    fun consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeidaPorCodigoDeBarras: Persona)

    override val modelosHijos: List<ModeloUI>
        get() = listOf(persona)

    enum class Estado
    {
        ESPERANDO_DATOS, CONSULTANDO_PERSONA, CREANDO_PERSONA
    }
}

class ProcesoCrearPersonaConSujetos internal constructor(
        override val persona: PersonaUI,
        private val apiPersona: PersonasAPI,
        private val ioScheduler: Scheduler = Schedulers.io()
                                                        ) : ProcesoCrearPersona
{
    constructor(idCliente: Long, apiPersona: PersonasAPI, ioScheduler: Scheduler = Schedulers.io())
            : this(PersonaUIConSujetos(idCliente), apiPersona, ioScheduler)

    private val sujetoEstado: BehaviorSubject<ProcesoCrearPersona.Estado> = BehaviorSubject.createDefault(ProcesoCrearPersona.Estado.ESPERANDO_DATOS)
    private val sujetoPersonaCreada: BehaviorSubject<Persona> = BehaviorSubject.create()
    private val sujetoEsPersonaCorrecta: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)
    private val sujetoErrorGlobal: BehaviorSubject<Opcional<String>> = BehaviorSubject.createDefault(Opcional.Vacio())
    private val sujetoDebeConsultarPersona: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    override val estado: Observable<ProcesoCrearPersona.Estado> = sujetoEstado
    override val errorGlobal: Observable<Opcional<String>> = sujetoErrorGlobal
    override val personaCreada: Observable<Persona> = sujetoPersonaCreada
    override var familiares: Set<Persona> = setOf()
    override val puedeCrearPersona: Observable<Boolean> =
            Observables.combineLatest(sujetoEsPersonaCorrecta, estado)
            { esPersonaCorrecta, estado ->
                esPersonaCorrecta && estado == ProcesoCrearPersona.Estado.ESPERANDO_DATOS
            }

    override val debeConsultarPersona: Boolean
        get()
        {
            return sujetoDebeConsultarPersona.value!!
        }

    override val observadoresInternos: List<Subject<*>> =
            listOf(
                    sujetoEstado,
                    sujetoPersonaCreada,
                    sujetoEsPersonaCorrecta,
                    sujetoErrorGlobal,
                    sujetoDebeConsultarPersona
                  )

    private val disposables = CompositeDisposable()

    init
    {
        persona.esPersonaValida.subscribe(sujetoEsPersonaCorrecta)
        Observables.combineLatest(persona.tipoDocumento, persona.numeroDocumento) { tipoDocumento, numeroDocumento ->
            Pair(tipoDocumento, numeroDocumento)
        }.distinctUntilChanged().subscribe { sujetoDebeConsultarPersona.onNext(it.second.isOnNext) }.addTo(disposables)
    }

    private inline fun crearInvocacionConsultaPorDocumento(
            documentoCompleto: DocumentoCompleto,
            crossinline manejarPersonaCasoExitoso: (RespuestaIndividual.Exitosa<PersonaConFamiliares>) -> Unit,
            crossinline manejarPersonaCasoExitosoVacio: () -> Unit
                                                          ) =
            Observable
                .fromCallable { apiPersona.consultarPorDocumento(documentoCompleto) }
                .subscribeOn(ioScheduler)
                .subscribe {
                    when (it)
                    {
                        is RespuestaIndividual.Exitosa       ->
                        {
                            manejarPersonaCasoExitoso(it)
                            print("\nRecibio datos"+it)

                            familiares = it.respuesta.familiares
                            sujetoDebeConsultarPersona.onNext(false)
                        }
                        is RespuestaIndividual.Vacia         ->
                        {   print("\nRespuesta vacia")
                            sujetoErrorGlobal.onNext(Opcional.De("El servidor retornó una respuesta vacía"))
                        }
                        is RespuestaIndividual.Error.Timeout ->
                        {   print("\nRespuesta timeout")
                            sujetoErrorGlobal.onNext(Opcional.De("Timeout contactando el backends"))
                        }
                        is RespuestaIndividual.Error.Red     ->
                        {   print("\nRespuesta red")
                            sujetoErrorGlobal.onNext(Opcional.De("Error contactando el backend"))
                        }
                        is RespuestaIndividual.Error.Back    ->
                        {   print("\nRespuesta error")
                            if (it.error.codigoInterno == PersonaConFamiliaresDTO.CodigosError.NO_EXISTE)
                            {
                                manejarPersonaCasoExitosoVacio()
                                sujetoDebeConsultarPersona.onNext(false)
                                familiares = setOf()
                            }
                            else
                            {
                                sujetoErrorGlobal.onNext(Opcional.De("Error en petición: ${it.error.mensaje}"))
                            }
                        }
                    }
                    sujetoEstado.onNext(ProcesoCrearPersona.Estado.ESPERANDO_DATOS)
                }.addTo(disposables)

    override fun consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeidaPorCodigoDeBarras: Persona)
    {
        if (sujetoEstado.value == ProcesoCrearPersona.Estado.ESPERANDO_DATOS)
        {
            val documentoCompleto = DocumentoCompleto(personaLeidaPorCodigoDeBarras)

            sujetoErrorGlobal.onNext(Opcional.Vacio())
            sujetoEstado.onNext(ProcesoCrearPersona.Estado.CONSULTANDO_PERSONA)

            crearInvocacionConsultaPorDocumento(
                    documentoCompleto,
                    {
                        val personaCombinada =
                                personaLeidaPorCodigoDeBarras
                                    .copiar(
                                            id = it.respuesta.persona.id,
                                            categoria = it.respuesta.persona.categoria,
                                            afiliacion = it.respuesta.persona.afiliacion
                                           )

                        persona.asignarPersona(personaCombinada)
                    },
                    {
                        persona
                            .asignarPersona(
                                    personaLeidaPorCodigoDeBarras
                                        .copiar(afiliacion = Persona.Afiliacion.NO_AFILIADO, categoria = Persona.Categoria.D)
                                           )
                    }
                                               )

            ResultadoAccionUI.ACCION_INICIADA
        }
    }

    override fun intentarConsultarPersonaPorDocumento(): ResultadoAccionUI
    {
        return if (sujetoEstado.value == ProcesoCrearPersona.Estado.ESPERANDO_DATOS)
        {
            if (debeConsultarPersona)
            {
                val documentoCompleto = try
                {
                    persona.darDocumentoCompleto()
                }
                catch (e: IllegalStateException)
                {
                    sujetoErrorGlobal.onNext(Opcional.De("Documento inválido"))
                    return ResultadoAccionUI.OBSERVABLES_EN_ESTADO_INVALIDO
                }
                sujetoErrorGlobal.onNext(Opcional.Vacio())
                sujetoEstado.onNext(ProcesoCrearPersona.Estado.CONSULTANDO_PERSONA)

                crearInvocacionConsultaPorDocumento(
                        documentoCompleto,
                        { persona.asignarPersona(it.respuesta.persona) },
                        {
                            persona.cambiarAfiliacion(Persona.Afiliacion.NO_AFILIADO)
                            persona.cambiarCategoria(Persona.Categoria.D)
                        }
                                                   )

                ResultadoAccionUI.ACCION_INICIADA
            }
            else
            {
                ResultadoAccionUI.MODELO_EN_ESTADO_INVALIDO
            }
        }
        else
        {
            ResultadoAccionUI.PROCESO_EN_ESTADO_INVALIDO
        }
    }

    override fun intentarCrearPersona(): ResultadoAccionUI
    {
        return if (sujetoEstado.value == ProcesoCrearPersona.Estado.ESPERANDO_DATOS)
        {
            if (sujetoEsPersonaCorrecta.value!! && !debeConsultarPersona)
            {
                val personaActual = try
                {
                    persona.aPersona()
                }
                catch (e: IllegalStateException)
                {
                    sujetoErrorGlobal.onNext(Opcional.De("Persona inválida"))
                    return ResultadoAccionUI.OBSERVABLES_EN_ESTADO_INVALIDO
                }
                sujetoErrorGlobal.onNext(Opcional.Vacio())
                sujetoEstado.onNext(ProcesoCrearPersona.Estado.CREANDO_PERSONA)

                Observable
                    .fromCallable {
                        if (personaActual.id == null)
                        {
                            apiPersona.crear(personaActual)
                        }
                        else
                        {
                            apiPersona.actualizar(personaActual)
                        }
                    }
                    .subscribeOn(ioScheduler)
                    .subscribe {
                        when (it)
                        {
                            is RespuestaIndividual.Exitosa       ->
                            {
                                persona.asignarPersona(it.respuesta)
                                sujetoPersonaCreada.onNext(it.respuesta)
                            }
                            is RespuestaIndividual.Vacia         ->
                            {
                            }
                            is RespuestaIndividual.Error.Timeout ->
                            {
                                sujetoErrorGlobal.onNext(Opcional.De("Timeout contactando el backend"))
                            }
                            is RespuestaIndividual.Error.Red     ->
                            {
                                sujetoErrorGlobal.onNext(Opcional.De("Error contactando el backend"))
                            }
                            is RespuestaIndividual.Error.Back    ->
                            {
                                sujetoErrorGlobal.onNext(Opcional.De("Error en petición: ${it.error.mensaje}"))
                            }
                        }

                        sujetoEstado.onNext(ProcesoCrearPersona.Estado.ESPERANDO_DATOS)
                    }.addTo(disposables)

                ResultadoAccionUI.ACCION_INICIADA
            }
            else
            {
                ResultadoAccionUI.MODELO_EN_ESTADO_INVALIDO
            }
        }
        else
        {
            ResultadoAccionUI.PROCESO_EN_ESTADO_INVALIDO
        }
    }
}