package co.smartobjects.ui.javafx.lectorbarras

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.pmw.tinylog.Logger
import purejavacomm.*
import java.io.InputStream

internal class LectorBarrasHoneyWellXenon1900
{
    companion object
    {
        private const val TIMEOUT_APERTURA_PUERTO = 1_000
        //private const val VID_PID_HONEYWELL = "VID_0C2E&PID_090A" // Se deja por si acaso
    }

    private var puerto: SerialPort? = null
    private var observadorEventosSeriales: ObservadorEventosSeriales? = null

    fun inicializar(): Single<EstadoLector>
    {
        return Single
            .fromCallable {
                Logger.info("Inicializando lector de barras")
                com.fazecast.jSerialComm.SerialPort.getCommPorts()
            }
            .map {
                val nombrePuertoCOM = it.firstOrNull { it.descriptivePortName.contains("Xenon 1900 Area-Imaging Scanner") }?.systemPortName

                if (nombrePuertoCOM == null)
                {
                    Logger.warn("No se pudo obtener el nombre del puerto.\n Puertos disponibles: ${
                    it.joinToString { "${it.descriptivePortName} (${it.systemPortName})" }
                    }")

                    EstadoLector.FalloDesconocido
                }
                else
                {
                    Logger.info("Nombre del puerto COM: $nombrePuertoCOM")

                    val resultadoInstanciaPuerto = obtenerInstanciaDePuerto(nombrePuertoCOM)

                    when (resultadoInstanciaPuerto)
                    {
                        LectorBarrasHoneyWellXenon1900.ResultadoInstanciaPuerto.ERROR         ->
                        {
                            Logger.warn("Error obteniendo puerto")

                            EstadoLector.FalloDesconocido
                        }
                        LectorBarrasHoneyWellXenon1900.ResultadoInstanciaPuerto.PUERTO_EN_USO ->
                        {
                            EstadoLector.PuertoEnUso
                        }
                        LectorBarrasHoneyWellXenon1900.ResultadoInstanciaPuerto.EXITO         ->
                        {
                            EstadoLector.Inicializado(observadorEventosSeriales!!.lecturas)
                        }
                    }
                }
            }
    }

    private fun obtenerInstanciaDePuerto(nombreDelPuerto: String): ResultadoInstanciaPuerto
    {
        try
        {
            Logger.info("Hay ${CommPortIdentifier.getPortIdentifiers().asSequence().count()} puertos disponibles")

            for (commPortIdentifier in CommPortIdentifier.getPortIdentifiers().asSequence())
            {
                Logger.info("Puerto: ${commPortIdentifier.name}")
                if (commPortIdentifier.name.contains(nombreDelPuerto))
                {
                    puerto = commPortIdentifier.open("Silvernest", TIMEOUT_APERTURA_PUERTO) as SerialPort
                    observadorEventosSeriales?.terminar()
                    observadorEventosSeriales = ObservadorEventosSeriales(puerto!!)
                    with(puerto!!)
                    {
                        notifyOnDataAvailable(true)
                        addEventListener(observadorEventosSeriales)
                        flowControlMode = SerialPort.FLOWCONTROL_XONXOFF_IN + SerialPort.FLOWCONTROL_XONXOFF_OUT
                    }

                    return ResultadoInstanciaPuerto.EXITO
                }
            }
        }
        catch (e: PortInUseException)
        {
            Logger.error(e, "Dueño del puerto actual: ${e.currentOwner}\n")
            apagarLector()

            return ResultadoInstanciaPuerto.PUERTO_EN_USO
        }
        catch (e: Exception)
        {
            Logger.error(e)
            apagarLector()
        }

        return ResultadoInstanciaPuerto.ERROR
    }

    fun apagarLector()
    {
        Logger.info("Apagando lector")
        observadorEventosSeriales?.terminar()
        puerto?.close()
    }

    private class ObservadorEventosSeriales(puerto: SerialPort)
        : SerialPortEventListener
    {
        private val eventosLecturas = PublishSubject.create<String>()
        private val input: InputStream = puerto.inputStream
        val lecturas: Observable<String> = eventosLecturas

        override fun serialEvent(event: SerialPortEvent)
        {
            try
            {
                when (event.eventType)
                {
                    SerialPortEvent.DATA_AVAILABLE ->
                    {
                        val numeroBytesDisponibles = input.available()
                        val buffer = ByteArray(numeroBytesDisponibles)
                        input.read(buffer, 0, numeroBytesDisponibles)

                        val lectura = String(buffer)
                        Logger.debug("Informando lectura: $lectura")
                        eventosLecturas.onNext(lectura)
                    }
                    else                           ->
                    {
                        Logger.warn("Se recibió evento de lectura de tipo: ${event.eventType}")
                    }
                }
            }
            catch (e: Exception)
            {
                Logger.error(e)
                terminar()
            }
        }

        fun terminar()
        {
            eventosLecturas.onComplete()
            input.close()
        }
    }

    sealed class EstadoLector
    {
        class Inicializado(val lecturas: Observable<String>) : EstadoLector()
        object PuertoEnUso : EstadoLector()
        object FalloDesconocido : EstadoLector()
    }

    private enum class ResultadoInstanciaPuerto
    {
        ERROR, PUERTO_EN_USO, EXITO
    }
}