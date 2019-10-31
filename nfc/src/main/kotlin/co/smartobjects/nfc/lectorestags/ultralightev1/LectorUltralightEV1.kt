package co.smartobjects.nfc.lectorestags.ultralightev1

import co.smartobjects.nfc.excepciones.NFCProtocolException
import co.smartobjects.nfc.lectorestags.ILectorTag
import co.smartobjects.nfc.lectorestags.parametrosDeAutenticacion.ParametrosAutenticacionUltralight
import co.smartobjects.nfc.tags.mifare.ultralight.UltralightEV1
import co.smartobjects.utilidades.aHexString
import java.util.*

abstract class LectorUltralightEV1 : ILectorTag<UltralightEV1>
{
    abstract fun leerPaginasEntre(paginaInicial: Byte, paginaFinal: Byte): ByteArray
    abstract fun autenticar(contraseña: ByteArray, pack: ByteArray): Boolean
    abstract fun escribirPagina(paginaAEscribir: Byte, datos: ByteArray)

    fun leerTodasLasPaginasDeUsuario(): ByteArray
    {
        val paginaFinal = tag.darPaginaUsuarioFinal()
        return leerPaginasEntre(UltralightEV1.PAGINA_USUARIO_INICIAL, paginaFinal)
    }

    fun leerPaginaInicialAutenticacion(): ByteArray
    {
        val paginaConfiguracionPaginaInicialAutenticacion = tag.darPaginaConfiguracionPaginaInicialAutenticacion()
        val datosPaginaConfiguracionPaginaInicialAutenticacion =
                leerPaginasEntre(paginaConfiguracionPaginaInicialAutenticacion, paginaConfiguracionPaginaInicialAutenticacion)

        if (datosPaginaConfiguracionPaginaInicialAutenticacion.size != UltralightEV1.BYTES_POR_PAGINA.toInt())
        {
            throw NFCProtocolException("Error leyendo la pagina de configuración de pagina inicial" +
                                       "Datos pagina inicial leidos: ${datosPaginaConfiguracionPaginaInicialAutenticacion.aHexString(" ")}")
        }

        return datosPaginaConfiguracionPaginaInicialAutenticacion
    }

    fun leerPaginaActivarAutenticacion(): ByteArray
    {
        val paginaConfiguracionActivarAutenticacion = tag.darPaginaConfiguracionActivarAutenticacion()
        val datosPaginaConfiguracionActivarAutenticacion =
                leerPaginasEntre(paginaConfiguracionActivarAutenticacion, paginaConfiguracionActivarAutenticacion)

        if (datosPaginaConfiguracionActivarAutenticacion.size != UltralightEV1.BYTES_POR_PAGINA.toInt())
        {
            throw NFCProtocolException("Error leyendo la pagina de configuración para activar autenticación. " +
                                       "Datos pagina autenticación leidos: ${datosPaginaConfiguracionActivarAutenticacion.aHexString(" ")}")
        }

        return datosPaginaConfiguracionActivarAutenticacion
    }

    fun verificarPaginaInicialAutenticacion(paginaInicialAutenticacionEsperada: Byte): Boolean
    {
        val datosPaginaConfiguracionPaginaInicialAutenticacion = leerPaginaInicialAutenticacion()

        return datosPaginaConfiguracionPaginaInicialAutenticacion[3] == paginaInicialAutenticacionEsperada
    }

    fun verificarPaginaActivarAutenticacion(maximoNumeroIntentosEsperado: Byte, protegerLecturaEsperado: Boolean): Boolean
    {
        val datosPaginaConfiguracionPaginaInicialAutenticacion = leerPaginaActivarAutenticacion()
        val paginaEsperada =
                UltralightEV1.configurarByteActivarAutenticacion(
                        datosPaginaConfiguracionPaginaInicialAutenticacion[0],
                        maximoNumeroIntentosEsperado,
                        protegerLecturaEsperado
                                                                )

        return datosPaginaConfiguracionPaginaInicialAutenticacion[0] == paginaEsperada
    }

    fun verificarParametrosDeAutenticacion(parametros: ParametrosAutenticacionUltralight): Boolean
    {
        return verificarPaginaInicialAutenticacion(parametros.paginaInicialAutenticacion)
               && verificarPaginaActivarAutenticacion(parametros.maximoNumeroIntentos, parametros.protegerLectura)
    }

    fun escribirContraseña(contraseña: ByteArray)
    {
        escribirPagina(tag.darPaginaContraseña(), contraseña)
    }

    fun escribirPack(pack: ByteArray)
    {
        val paddedPack = tag.padPack(pack)
        escribirPagina(tag.darPaginaPack(), paddedPack)
    }

    fun escribirPaginaActivarAutenticacion(maximoNumeroIntentos: Byte, protegerLectura: Boolean)
    {
        val paginaConfiguracionActivarAutenticacion = tag.darPaginaConfiguracionActivarAutenticacion()
        val datosPaginaConfiguracionActivarAutenticacion = leerPaginaActivarAutenticacion()
        datosPaginaConfiguracionActivarAutenticacion[0] =
                UltralightEV1.configurarByteActivarAutenticacion(
                        datosPaginaConfiguracionActivarAutenticacion[0],
                        maximoNumeroIntentos,
                        protegerLectura
                                                                )
        escribirPagina(paginaConfiguracionActivarAutenticacion, datosPaginaConfiguracionActivarAutenticacion)
    }

    fun escribirPaginaInicialAutenticacion(paginaInicialAutenticacion: Byte)
    {
        val paginaConfiguracionPaginaInicialAutenticacion = tag.darPaginaConfiguracionPaginaInicialAutenticacion()
        val datosPaginaConfiguracionPaginaInicialAutenticacion = leerPaginaInicialAutenticacion()
        datosPaginaConfiguracionPaginaInicialAutenticacion[3] = paginaInicialAutenticacion
        escribirPagina(paginaConfiguracionPaginaInicialAutenticacion, datosPaginaConfiguracionPaginaInicialAutenticacion)
    }

    fun desactivarAutenticacion()
    {
        escribirPaginaInicialAutenticacion(0xFF.toByte())
        escribirPaginaActivarAutenticacion(0, false)
        escribirContraseña(UltralightEV1.CONTRASEÑA_POR_DEFECTO)
        escribirPack(UltralightEV1.PACK_POR_DEFECTO)
    }

    fun activarAutenticacion(contraseña: ByteArray, pack: ByteArray, parametrosAutenticacion: ParametrosAutenticacionUltralight)
    {
        escribirContraseña(contraseña)
        escribirPack(pack)
        escribirPaginaActivarAutenticacion(parametrosAutenticacion.maximoNumeroIntentos, parametrosAutenticacion.protegerLectura)
        escribirPaginaInicialAutenticacion(parametrosAutenticacion.paginaInicialAutenticacion)
    }

    fun borrarPaginasDeUsuario()
    {
        val bytesAEscribir = ByteArray(UltralightEV1.BYTES_POR_PAGINA.toInt())
        for (pagina in 0 until tag.version.darNumeroDePaginasDeUsuario())
        {
            escribirPagina(tag.darPaginaRealDeUsuario(pagina.toByte()), bytesAEscribir)
        }
    }

    fun escribirEnPaginasDeUsuario(datosAEscribir: ByteArray)
    {
        val datosConPadding = tag.padDatosAEscribir(datosAEscribir)
        if (!tag.cabeEnPaginasUsuario(datosConPadding))
        {
            throw NFCProtocolException("No hay espacio suficiente para escribir ${datosConPadding.size} bytes de datos")
        }
        val numeroDePaginas = datosConPadding.size / UltralightEV1.BYTES_POR_PAGINA
        for (pagina in 0 until numeroDePaginas)
        {
            val posicionInicial = pagina * UltralightEV1.BYTES_POR_PAGINA
            val paginaAEscribir = Arrays.copyOfRange(datosConPadding, posicionInicial, posicionInicial + UltralightEV1.BYTES_POR_PAGINA)
            escribirPagina(tag.darPaginaRealDeUsuario(pagina.toByte()), paginaAEscribir)
        }
    }
}