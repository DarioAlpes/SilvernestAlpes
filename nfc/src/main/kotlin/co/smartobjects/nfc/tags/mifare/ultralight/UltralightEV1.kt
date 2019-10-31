package co.smartobjects.nfc.tags.mifare.ultralight

import co.smartobjects.nfc.excepciones.NFCProtocolException
import co.smartobjects.nfc.lectorestags.parametrosDeAutenticacion.ParametrosAutenticacionUltralight
import co.smartobjects.utilidades.aHexString

class UltralightEV1(versionComoByte: Byte) : UltralightTag
{
    enum class VersionUltralightEV1(val hexDeVersion: Byte)
    {
        MF0UL11(0x0B)
        {
            override fun darOffsetPaginasConfiguracion(): Byte = 1

            override fun darNumeroDePaginasDeUsuario(): Byte = (384 / 8 / BYTES_POR_PAGINA).toByte()
        }
        ,
        MF0UL21(0x0E)
        {
            override fun darOffsetPaginasConfiguracion(): Byte = 2

            override fun darNumeroDePaginasDeUsuario(): Byte = (1024 / 8 / BYTES_POR_PAGINA).toByte()
        };

        abstract fun darNumeroDePaginasDeUsuario(): Byte
        abstract fun darOffsetPaginasConfiguracion(): Byte
    }

    companion object
    {
        val PARAMETROS_DE_CONFIGURACION_DE_AUTENTICACION =
                ParametrosAutenticacionUltralight(0, 7, true)

        const val PAGINA_USUARIO_INICIAL = 4.toByte()
        const val BYTES_POR_PAGINA = 4.toByte()
        private const val NUMERO_BYTES_VERSION = 8.toByte()
        private const val ULTRALIGHT_EV1_VERSION_ID = 0x01.toByte()

        val CONTRASEÑA_POR_DEFECTO = byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte())
        val PACK_POR_DEFECTO = byteArrayOf(0x00.toByte(), 0x00.toByte())
        val TAMAÑO_CONTRASEÑA = CONTRASEÑA_POR_DEFECTO.size
        fun esUltralightEV1(version: ByteArray): Boolean
        {
            return version.size.toByte() == NUMERO_BYTES_VERSION &&
                   version[2] == UltralightTag.ULTRALIGHT_VERSION_ID &&
                   version[4] == ULTRALIGHT_EV1_VERSION_ID &&
                   (version[6] == VersionUltralightEV1.MF0UL11.hexDeVersion || version[6] == VersionUltralightEV1.MF0UL21.hexDeVersion)
        }

        fun darComandoLeerPaginasEntre(paginaInicial: Byte, paginaFinal: Byte): ByteArray
        {
            return byteArrayOf(0x3A, paginaInicial, paginaFinal)
        }

        fun darComandoAutenticarTag(contraseña: ByteArray): ByteArray
        {
            val comando = ByteArray(contraseña.size + 1)
            comando[0] = 0x1B.toByte()
            System.arraycopy(contraseña, 0, comando, 1, contraseña.size)
            return comando
        }

        fun darComandoEscribirDatos(paginaAEscribir: Byte, datos: ByteArray): ByteArray
        {
            if (datos.size.toByte() != BYTES_POR_PAGINA)
            {
                throw NFCProtocolException("Se deben escribir paginas completas ($BYTES_POR_PAGINA bytes)")
            }
            val comando = ByteArray(datos.size + 2)
            comando[0] = 0xA2.toByte()
            comando[1] = paginaAEscribir
            System.arraycopy(datos, 0, comando, 2, datos.size)
            return comando
        }

        fun configurarByteActivarAutenticacion(byteOriginalAutenticacion: Byte, maximoNumeroIntentos: Byte, protegerLectura: Boolean): Byte
        {
            if (maximoNumeroIntentos < 0 || maximoNumeroIntentos > 7)
            {
                throw NFCProtocolException("El maximo número de intentos debe estar entre 0 y 7")
            }

            var byteResultado = byteOriginalAutenticacion

            // El byte más significativo queda en 1 si se quiere proteger la lectura y 0 si no
            byteResultado =
                    if (protegerLectura)
                    {
                        (byteResultado.toInt() or (1 shl 7)).toByte()
                    }
                    else
                    {
                        (byteResultado.toInt() and (1 shl 7).inv()).toByte()
                    }

            // Apagar los 3 bits menos significativos para el número de intentos
            byteResultado = (byteResultado.toInt() and (1 or (1 shl 1) or (1 shl 2)).inv()).toByte()

            // Prender los 3 bits más significativos para los número de intentos según el valor deseado
            byteResultado = (byteResultado.toInt() or maximoNumeroIntentos.toInt()).toByte()

            return byteResultado
        }
    }

    val version: VersionUltralightEV1

    init
    {
        version = when (versionComoByte)
        {
            VersionUltralightEV1.MF0UL11.hexDeVersion -> VersionUltralightEV1.MF0UL11
            VersionUltralightEV1.MF0UL21.hexDeVersion -> VersionUltralightEV1.MF0UL21
            else                                      -> throw NFCProtocolException("Versión de tag no soportado: ${versionComoByte.aHexString()}")
        }
    }

    override fun darNombre(): String
    {
        return "Ultralight EV1"
    }

    override fun darTamañoLlave(): Int
    {
        return TAMAÑO_CONTRASEÑA
    }

    fun darPaginaConfiguracionPaginaInicialAutenticacion(): Byte
    {
        return (darPaginaUsuarioFinal() + version.darOffsetPaginasConfiguracion()).toByte()
    }

    fun darPaginaConfiguracionActivarAutenticacion(): Byte
    {
        return (darPaginaConfiguracionPaginaInicialAutenticacion() + 1).toByte()
    }

    fun darPaginaContraseña(): Byte
    {
        return (darPaginaConfiguracionActivarAutenticacion() + 1).toByte()
    }

    fun darPaginaPack(): Byte
    {
        return (darPaginaContraseña() + 1).toByte()
    }

    fun darPaginaUsuarioFinal(): Byte
    {
        return darPaginaRealDeUsuario((version.darNumeroDePaginasDeUsuario() - 1).toByte())
    }

    fun darPaginaRealDeUsuario(paginaUsuario: Byte): Byte
    {
        if (paginaUsuario > version.darNumeroDePaginasDeUsuario() - 1)
        {
            throw NFCProtocolException("No se puede trabajar sobre la pagina de usuario $paginaUsuario, solo existen ${version.darNumeroDePaginasDeUsuario()} de usuario")
        }
        return (UltralightEV1.PAGINA_USUARIO_INICIAL + paginaUsuario).toByte()
    }

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

    fun padPack(pack: ByteArray): ByteArray
    {
        if (pack.size != 2)
        {
            throw NFCProtocolException("El pack debe tener un tamaño de 2 bytes")
        }

        return padDatosAEscribir(pack)
    }

    fun cabeEnPaginasUsuario(datosAEscribir: ByteArray): Boolean
    {
        val maximoNumeroBytes = BYTES_POR_PAGINA * version.darNumeroDePaginasDeUsuario()
        return datosAEscribir.size <= maximoNumeroBytes
    }
}