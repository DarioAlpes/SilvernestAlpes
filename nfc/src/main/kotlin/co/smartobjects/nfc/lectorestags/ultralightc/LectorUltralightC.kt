package co.smartobjects.nfc.lectorestags.ultralightc

import co.smartobjects.nfc.excepciones.NFCProtocolException
import co.smartobjects.nfc.lectorestags.ILectorTag
import co.smartobjects.nfc.lectorestags.parametrosDeAutenticacion.ParametrosAutenticacionUltralight
import co.smartobjects.nfc.tags.mifare.ultralight.UltralightC
import co.smartobjects.utilidades.aHexString
import java.util.*

abstract class LectorUltralightC : ILectorTag<UltralightC>
{
    abstract fun leerPaginasEntre(paginaInicial: Byte, paginaFinal: Byte): ByteArray
    abstract fun autenticar(llave: ByteArray): Boolean
    abstract fun escribirPagina(paginaAEscribir: Byte, datos: ByteArray)

    fun leerTodasLasPaginasDeUsuario(): ByteArray
    {
        return leerPaginasEntre(UltralightC.PAGINA_USUARIO_INICIAL, UltralightC.PAGINA_USUARIO_FINAL)
    }

    private fun leerPaginaInicialAutenticacion(): ByteArray
    {
        val datosPaginaConfiguracionPaginaInicialAutenticacion =
                leerPaginasEntre(
                        UltralightC.PAGINA_CONFIGURACION_PAGINA_INICIAL_AUTENTICACION,
                        UltralightC.PAGINA_CONFIGURACION_PAGINA_INICIAL_AUTENTICACION
                                )

        if (datosPaginaConfiguracionPaginaInicialAutenticacion.size != UltralightC.BYTES_POR_PAGINA.toInt())
        {
            throw NFCProtocolException("Error leyendo la pagina de configuración de pagina inicial" +
                                       "Datos pagina inicial leidos: ${datosPaginaConfiguracionPaginaInicialAutenticacion.aHexString(" ")}")
        }

        return datosPaginaConfiguracionPaginaInicialAutenticacion
    }

    private fun leerPaginaActivarAutenticacion(): ByteArray
    {
        val datosPaginaConfiguracionActivarAutenticacion = leerPaginasEntre(UltralightC.PAGINA_ACTIVAR_AUTENTICACION, UltralightC.PAGINA_ACTIVAR_AUTENTICACION)
        if (datosPaginaConfiguracionActivarAutenticacion.size != UltralightC.BYTES_POR_PAGINA.toInt())
        {
            throw NFCProtocolException("Error leyendo la pagina de configuración para activar autenticación. " +
                                       "Datos pagina autenticación leidos: ${datosPaginaConfiguracionActivarAutenticacion.aHexString(" ")}")
        }
        return datosPaginaConfiguracionActivarAutenticacion
    }

    private fun verificarPaginaInicialAutenticacion(paginaInicialAutenticacionEsperada: Byte): Boolean
    {
        val datosPaginaConfiguracionPaginaInicialAutenticacion = leerPaginaInicialAutenticacion()
        return datosPaginaConfiguracionPaginaInicialAutenticacion[0] == paginaInicialAutenticacionEsperada
    }

    private fun verificarPaginaActivarAutenticacion(protegerLecturaEsperado: Boolean): Boolean
    {
        val datosPaginaConfiguracionPaginaInicialAutenticacion = leerPaginaActivarAutenticacion()
        val paginaEsperada = UltralightC.configurarByteActivarAutenticacion(datosPaginaConfiguracionPaginaInicialAutenticacion[0], protegerLecturaEsperado)
        return datosPaginaConfiguracionPaginaInicialAutenticacion[0] == paginaEsperada
    }

    fun verificarParametrosDeAutenticacion(parametros: ParametrosAutenticacionUltralight): Boolean
    {
        return verificarPaginaInicialAutenticacion(parametros.paginaInicialAutenticacion) && verificarPaginaActivarAutenticacion(parametros.protegerLectura)
    }

    fun escribirLlave(llave: ByteArray)
    {
        if (llave.size != 16)
        {
            throw NFCProtocolException("La llave debe tener un tamaño de 16 bytes")
        }

        val primeraLlave = ByteArray(8)
        System.arraycopy(llave, 0, primeraLlave, 0, primeraLlave.size)
        primeraLlave.reverse()

        for (i in 0..1)
        {
            val pagina = (UltralightC.PAGINA_INICIAL_LLAVE + i).toByte()
            val paginaLlave = ByteArray(UltralightC.BYTES_POR_PAGINA.toInt())
            System.arraycopy(primeraLlave, i * UltralightC.BYTES_POR_PAGINA.toInt(), paginaLlave, 0, paginaLlave.size)
            escribirPagina(pagina, paginaLlave)
        }

        val segundaLlave = ByteArray(8)
        System.arraycopy(llave, primeraLlave.size, segundaLlave, 0, segundaLlave.size)
        segundaLlave.reverse()

        for (i in 0..1)
        {
            val pagina = (UltralightC.PAGINA_INICIAL_LLAVE + i + 2).toByte()
            val paginaLlave = ByteArray(UltralightC.BYTES_POR_PAGINA.toInt())
            System.arraycopy(segundaLlave, i * UltralightC.BYTES_POR_PAGINA.toInt(), paginaLlave, 0, paginaLlave.size)
            escribirPagina(pagina, paginaLlave)
        }
    }

    fun escribirPaginaActivarAutenticacion(protegerLectura: Boolean)
    {
        val datosPaginaConfiguracionActivarAutenticacion = leerPaginaActivarAutenticacion()
        datosPaginaConfiguracionActivarAutenticacion[0] = UltralightC.configurarByteActivarAutenticacion(datosPaginaConfiguracionActivarAutenticacion[0], protegerLectura)
        escribirPagina(UltralightC.PAGINA_ACTIVAR_AUTENTICACION, datosPaginaConfiguracionActivarAutenticacion)
    }

    fun escribirPaginaInicialAutenticacion(paginaInicialAutenticacion: Byte)
    {
        val datosPaginaConfiguracionPaginaInicialAutenticacion = leerPaginaInicialAutenticacion()
        datosPaginaConfiguracionPaginaInicialAutenticacion[0] = paginaInicialAutenticacion
        escribirPagina(UltralightC.PAGINA_CONFIGURACION_PAGINA_INICIAL_AUTENTICACION, datosPaginaConfiguracionPaginaInicialAutenticacion)
    }

    fun desactivarAutenticacion()
    {
        escribirPaginaInicialAutenticacion(0x30.toByte())
        escribirPaginaActivarAutenticacion(false)
        escribirLlave(UltralightC.LLAVE_POR_DEFECTO)
    }

    fun activarAutenticacion(llave: ByteArray, parametrosAutenticacion: ParametrosAutenticacionUltralight)
    {
        escribirLlave(llave)
        escribirPaginaActivarAutenticacion(parametrosAutenticacion.protegerLectura)
        escribirPaginaInicialAutenticacion(parametrosAutenticacion.paginaInicialAutenticacion)
    }

    fun borrarPaginasDeUsuario()
    {
        val bytesAEscribir = ByteArray(UltralightC.BYTES_POR_PAGINA.toInt())
        for (pagina in 0 until UltralightC.PAGINAS_DE_USUARIO)
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
        val numeroDePaginas = datosConPadding.size / UltralightC.BYTES_POR_PAGINA
        for (pagina in 0 until numeroDePaginas)
        {
            val posicionInicial = pagina * UltralightC.BYTES_POR_PAGINA
            val paginaAEscribir = Arrays.copyOfRange(datosConPadding, posicionInicial, posicionInicial + UltralightC.BYTES_POR_PAGINA)
            escribirPagina(tag.darPaginaRealDeUsuario(pagina.toByte()), paginaAEscribir)
        }
    }
}