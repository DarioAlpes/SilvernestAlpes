package co.smartobjects.nfc.tags.mifare.ultralight

import co.smartobjects.nfc.excepciones.NFCProtocolException
import co.smartobjects.nfc.lectorestags.parametrosDeAutenticacion.ParametrosAutenticacionUltralight
import co.smartobjects.nfc.utils.cifrarConDES
import co.smartobjects.nfc.utils.descifrarConDES
import co.smartobjects.utilidades.aHexString
import java.util.*

class UltralightC : UltralightTag
{
    companion object
    {
        val PARAMETROS_DE_CONFIGURACION_DE_AUTENTICACION =
                ParametrosAutenticacionUltralight(paginaInicialAutenticacion = 3, protegerLectura = true)

        val COMANDO_INICIAR_AUTENTICACION = byteArrayOf(0x1A, 0x00)
        private const val COMANDO_NEGOCIAR_AUTENTICACION = 0xAF.toByte()
        private const val COMANDO_SOLUCION_RETO_AUTENTICACION = 0x00.toByte()

        const val PAGINA_INICIAL_LLAVE = 44.toByte()
        const val PAGINA_ACTIVAR_AUTENTICACION = 43.toByte()
        const val PAGINA_CONFIGURACION_PAGINA_INICIAL_AUTENTICACION = 42.toByte()
        const val PAGINA_USUARIO_INICIAL = 4.toByte()
        const val PAGINA_USUARIO_FINAL = 39.toByte()
        const val PAGINAS_DE_USUARIO = (PAGINA_USUARIO_FINAL - PAGINA_USUARIO_INICIAL + 1).toByte()
        const val BYTES_POR_PAGINA = 4.toByte()

        // llave por defecto de NXP: BREAKMEIFYOUCAN! (16 bytes)
        val LLAVE_POR_DEFECTO =
                byteArrayOf(
                        0x49.toByte(),
                        0x45.toByte(),
                        0x4D.toByte(),
                        0x4B.toByte(),
                        0x41.toByte(),
                        0x45.toByte(),
                        0x52.toByte(),
                        0x42.toByte(),
                        0x21.toByte(),
                        0x4E.toByte(),
                        0x41.toByte(),
                        0x43.toByte(),
                        0x55.toByte(),
                        0x4F.toByte(),
                        0x59.toByte(),
                        0x46.toByte()
                           )

        const val NUMERO_BYTES_LLAVE = 16

        fun prepararRespuestaRetoAutenticacion(llave: ByteArray, retoDeTag: ByteArray, retoParaTag: ByteArray): ByteArray
        {
            if (retoDeTag.size != 9 || retoDeTag[0] != COMANDO_NEGOCIAR_AUTENTICACION)
            {
                throw NFCProtocolException("Error autenticacando el tag, se esperaba un arreglo de 9 bytes empezando por ${COMANDO_NEGOCIAR_AUTENTICACION.aHexString()}. " +
                                           "Se obtuvo ${retoDeTag.aHexString(" ")}")
            }

            val retoDeTagCifrado = ByteArray(retoDeTag.size - 1)
            System.arraycopy(retoDeTag, 1, retoDeTagCifrado, 0, retoDeTagCifrado.size)
            val retoDeTagDescifrado = descifrarConDES(retoDeTagCifrado, llave, ByteArray(8))

            // Mensaje a cifrar = reto + mensaje descifrado rotado un byte a la izquierda
            val retoDeTagMasRetoParaTag = Arrays.copyOf(retoParaTag, retoParaTag.size + retoDeTagDescifrado.size)
            System.arraycopy(retoDeTagDescifrado, 1, retoDeTagMasRetoParaTag, retoParaTag.size, retoDeTagDescifrado.size - 1)
            retoDeTagMasRetoParaTag[retoDeTagMasRetoParaTag.size - 1] = retoDeTagDescifrado[0]

            val retoDeTagMasRetoParaTagCifrado = cifrarConDES(retoDeTagMasRetoParaTag, llave, retoDeTagCifrado)
            val comandoRetoDeTagMasRetoParaTag = ByteArray(retoDeTagMasRetoParaTagCifrado.size + 1)
            comandoRetoDeTagMasRetoParaTag[0] = COMANDO_NEGOCIAR_AUTENTICACION
            System.arraycopy(retoDeTagMasRetoParaTagCifrado, 0, comandoRetoDeTagMasRetoParaTag, 1, retoDeTagMasRetoParaTagCifrado.size)

            return comandoRetoDeTagMasRetoParaTag
        }

        fun verificarRespuestaRetoTag(llave: ByteArray, retoParaTag: ByteArray, comandoRetoDeTagMasRetoParaTag: ByteArray, resultadoRetoTag: ByteArray): Boolean
        {
            if (resultadoRetoTag.size != 9 || resultadoRetoTag[0] != COMANDO_SOLUCION_RETO_AUTENTICACION)
            {
                throw NFCProtocolException("Error autenticacando el tag, se esperaba un arreglo de 9 bytes empezando por ${COMANDO_SOLUCION_RETO_AUTENTICACION.aHexString()}. " +
                                           "Se obtuvo ${resultadoRetoTag.aHexString(" ")}")
            }
            val ivBytes = ByteArray(8)
            System.arraycopy(comandoRetoDeTagMasRetoParaTag, comandoRetoDeTagMasRetoParaTag.size - ivBytes.size, ivBytes, 0, ivBytes.size)

            val solucionDeTagCifrado = ByteArray(resultadoRetoTag.size - 1)
            System.arraycopy(resultadoRetoTag, 1, solucionDeTagCifrado, 0, solucionDeTagCifrado.size)
            val solucionDeTagDescifrado = descifrarConDES(solucionDeTagCifrado, llave, ivBytes)

            return ((0..solucionDeTagDescifrado.size - 2).all { solucionDeTagDescifrado[it] == retoParaTag[it + 1] })
                   && retoParaTag[0] == solucionDeTagDescifrado[solucionDeTagDescifrado.size - 1]
        }

        fun configurarByteActivarAutenticacion(byteOriginalAutenticacion: Byte, protegerLectura: Boolean): Byte
        {
            // El byte menos significativo queda en 0 si se quiere proteger la lectura y 1 si no
            return if (protegerLectura)
            {
                (byteOriginalAutenticacion.toInt() and 1.inv()).toByte()
            }
            else
            {
                (byteOriginalAutenticacion.toInt() or 1).toByte()
            }
        }
    }

    override fun darNombre(): String = "Ultralight C"

    override fun darTamañoLlave(): Int = NUMERO_BYTES_LLAVE

    fun padDatosAEscribir(datosAEscribir: ByteArray): ByteArray
    {
        return if (datosAEscribir.size.rem(BYTES_POR_PAGINA) == 0)
        {
            datosAEscribir
        }
        else
        {
            val tamañoFinal = ((datosAEscribir.size / BYTES_POR_PAGINA) + 1) * BYTES_POR_PAGINA
            val datosFinal = ByteArray(tamañoFinal)

            System.arraycopy(datosAEscribir, 0, datosFinal, 0, datosAEscribir.size)

            datosFinal
        }
    }

    fun cabeEnPaginasUsuario(datosAEscribir: ByteArray): Boolean
    {
        val maximoNumeroBytes = BYTES_POR_PAGINA * PAGINAS_DE_USUARIO

        return datosAEscribir.size <= maximoNumeroBytes
    }

    fun darPaginaRealDeUsuario(paginaUsuario: Byte): Byte
    {
        if (paginaUsuario > PAGINAS_DE_USUARIO - 1)
        {
            throw NFCProtocolException("No se puede trabajar sobre la pagina de usuario $paginaUsuario, solo existen $PAGINAS_DE_USUARIO de usuario")
        }
        return (PAGINA_USUARIO_INICIAL + paginaUsuario).toByte()
    }
}