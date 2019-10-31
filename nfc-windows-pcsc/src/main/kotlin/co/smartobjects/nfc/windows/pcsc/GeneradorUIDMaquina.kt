package co.smartobjects.nfc.windows.pcsc

import co.smartobjects.utilidades.aHexString
import kotlinx.coroutines.*
import oshi.SystemInfo
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

interface GeneradorUIDMaquina
{
    fun generarBloqueante() = runBlocking { generar() }
    suspend fun generar(): String?
}

class GeneradorUIDMaquinaImpl : GeneradorUIDMaquina
{
    private lateinit var cacheUIDGenerado: String

    override suspend fun generar(): String?
    {
        if (this::cacheUIDGenerado.isInitialized)
        {
            return cacheUIDGenerado
        }

        return try
        {
            val systemInfo = SystemInfo()

            val segmentoOsVendorYOsFamily = GlobalScope.async(Dispatchers.IO) {
                val vendedorSO = async { systemInfo.operatingSystem.manufacturer }
                val familiaSO = async { systemInfo.operatingSystem.family }

                vendedorSO.await() + familiaSO.await()
            }

            val boardSerialNumber = GlobalScope.async(Dispatchers.IO) { systemInfo.hardware.computerSystem.baseboard.serialNumber }
            val boardManufacter = GlobalScope.async(Dispatchers.IO) { systemInfo.hardware.computerSystem.baseboard.manufacturer }
            val boardModel = GlobalScope.async(Dispatchers.IO) { systemInfo.hardware.computerSystem.baseboard.model }
            val boardVersion = GlobalScope.async(Dispatchers.IO) { systemInfo.hardware.computerSystem.baseboard.version }

            // Toca probar si estos siguen siendo lentos al ejecutarse
            val informacionProcesador = GlobalScope.async(Dispatchers.IO) {
                val processorIdentifier = async { systemInfo.hardware.processor.identifier }
                val processors = async { systemInfo.hardware.processor.logicalProcessorCount }

                processorIdentifier.await() + processors.await()
            }

            val resultados =
                    listOf(segmentoOsVendorYOsFamily, boardSerialNumber, boardManufacter, boardModel, boardVersion, informacionProcesador)
                        .map { it.await() }

            cacheUIDGenerado = calcularSHA256yDarEnHex(resultados.joinToString())

            cacheUIDGenerado
        }
        catch (t: Throwable)
        {
            null
        }
    }

    private fun calcularSHA256yDarEnHex(valor: String): String
    {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(valor.toByteArray(StandardCharsets.UTF_8))
        return hash.aHexString()
    }
}