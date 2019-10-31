package co.smartobjects.nfc.lectorestags.ultralightev1

import co.smartobjects.nfc.tags.mifare.ultralight.UltralightEV1
import co.smartobjects.nfc.utils.byteAIntSinSigno
import java.util.*

class LectorUltralightEV1Memoria(version: Byte) : LectorUltralightEV1()
{
    private val _tag = UltralightEV1(version)
    var uid = byteArrayOf(6, 7, 8, 9)
    var contraseña = UltralightEV1.CONTRASEÑA_POR_DEFECTO
    var pack = UltralightEV1.PACK_POR_DEFECTO
    internal var paginasUsuario = ByteArray(tag.version.darNumeroDePaginasDeUsuario() * UltralightEV1.BYTES_POR_PAGINA)
    var paginaConfiguracionPaginaInicialAutenticacion = byteArrayOf(5, 6, 7, 0xFF.toByte())
    var paginaConfiguracionActivarAutenticacion = byteArrayOf(UltralightEV1.configurarByteActivarAutenticacion(0, 0, false), 8, 9, 10)
    private var conectado = false
    private var bloqueado = false
    private var autenticado = false
    override val tag: UltralightEV1
        get() = _tag

    override fun conectar()
    {
        if (bloqueado)
        {
            throw AssertionError("Intentando conectarse a tag bloqueado (HALT), debe reconectarse antes")
        }
        conectado = true
    }

    override fun desconectar()
    {
        conectado = false
        bloqueado = false
        autenticado = false
    }

    override fun darUID(): ByteArray
    {
        verificarEstadoConexion()
        return uid
    }

    override fun leerPaginasEntre(paginaInicial: Byte, paginaFinal: Byte): ByteArray
    {
        if (paginaInicial > paginaFinal)
        {
            bloqueado = true
            throw AssertionError("Pagina final debe ser menor o igual a pagina inicial")
        }
        if (!puedeLeerPagina(paginaFinal))
        {
            throw AssertionError("La pagina $paginaFinal no se puede leer sin autenticación")
        }
        if (esPaginaDeUsuario(paginaInicial) && esPaginaDeUsuario(paginaFinal))
        {
            val posicionInicial = (paginaInicial - UltralightEV1.PAGINA_USUARIO_INICIAL) * UltralightEV1.BYTES_POR_PAGINA
            val posicionFinal = (paginaFinal - UltralightEV1.PAGINA_USUARIO_INICIAL + 1) * UltralightEV1.BYTES_POR_PAGINA
            return Arrays.copyOfRange(paginasUsuario, posicionInicial, posicionFinal)
        }
        else if (paginaInicial == paginaFinal)
        {
            if (paginaInicial == tag.darPaginaConfiguracionPaginaInicialAutenticacion())
            {
                return Arrays.copyOf(paginaConfiguracionPaginaInicialAutenticacion, paginaConfiguracionPaginaInicialAutenticacion.size)
            }
            else if (paginaInicial == tag.darPaginaConfiguracionActivarAutenticacion())
            {
                return Arrays.copyOf(paginaConfiguracionActivarAutenticacion, paginaConfiguracionActivarAutenticacion.size)
            }
        }
        bloqueado = true
        throw AssertionError("Solo se soporta la lectura de las paginas de configuracion y las paginas de usuario")
    }

    override fun autenticar(contraseña: ByteArray, pack: ByteArray): Boolean
    {
        verificarEstadoConexion()
        autenticado = if (Arrays.equals(contraseña, this.contraseña))
        {
            Arrays.equals(pack, this.pack)
        }
        else
        {
            bloqueado = true
            false
        }
        return autenticado
    }

    override fun escribirPagina(paginaAEscribir: Byte, datos: ByteArray)
    {
        if (datos.size != UltralightEV1.BYTES_POR_PAGINA.toInt())
        {
            bloqueado = true
            throw AssertionError("Tamaño incorrecto de datos a escribir")
        }
        if (!puedeEscribirPagina(paginaAEscribir))
        {
            throw AssertionError("La pagina $paginaAEscribir no se puede escribir sin autenticación")
        }
        if (esPaginaDeUsuario(paginaAEscribir))
        {
            val posicionInicialUsuario = (paginaAEscribir - UltralightEV1.PAGINA_USUARIO_INICIAL) * UltralightEV1.BYTES_POR_PAGINA
            System.arraycopy(datos, 0, paginasUsuario, posicionInicialUsuario, UltralightEV1.BYTES_POR_PAGINA.toInt())
        }
        else if (paginaAEscribir == tag.darPaginaContraseña())
        {
            contraseña = Arrays.copyOf(datos, contraseña.size)
        }
        else if (paginaAEscribir == tag.darPaginaPack())
        {
            pack = Arrays.copyOf(datos, pack.size)
        }
        else if (paginaAEscribir == tag.darPaginaConfiguracionPaginaInicialAutenticacion())
        {
            for (i in 0..2)
            {
                if (datos[i] != paginaConfiguracionPaginaInicialAutenticacion[i])
                {
                    bloqueado = true
                    throw AssertionError("Error en pagina de configuración para pagina inicial de autenticación, se esta modificando un byte incorrecto")
                }
            }
            paginaConfiguracionPaginaInicialAutenticacion = Arrays.copyOf(datos, paginaConfiguracionPaginaInicialAutenticacion.size)
        }
        else if (paginaAEscribir == tag.darPaginaConfiguracionActivarAutenticacion())
        {
            for (i in 1..3)
            {
                if (datos[i] != paginaConfiguracionActivarAutenticacion[i])
                {
                    bloqueado = true
                    throw AssertionError("Error en pagina de configuración de autenticación, se esta modificando un byte incorrecto")
                }
            }
            if (apagarBytesAutenticacion(datos[0]) != apagarBytesAutenticacion(paginaConfiguracionActivarAutenticacion[0]))
            {
                throw AssertionError("Se estan modificando bits incorrectos del byte de configuración de autenticación")
            }
            paginaConfiguracionActivarAutenticacion = Arrays.copyOf(datos, paginaConfiguracionActivarAutenticacion.size)
        }
        else
        {
            bloqueado = true
            throw AssertionError("Se estan modificando paginas no soportadas")
        }
    }

    private fun puedeLeerPagina(pagina: Byte): Boolean
    {
        return puedeEscribirPagina(pagina) || !estaActivadaProteccionDeLectura()
    }

    private fun puedeEscribirPagina(pagina: Byte): Boolean
    {
        return autenticado || byteAIntSinSigno(pagina) < byteAIntSinSigno(paginaConfiguracionPaginaInicialAutenticacion[3])
    }

    private fun estaActivadaProteccionDeLectura(): Boolean
    {
        return ((1 shl 7) and paginaConfiguracionPaginaInicialAutenticacion[3].toInt()) == (1 shl 7)
    }

    private fun esPaginaDeUsuario(pagina: Byte): Boolean
    {
        return pagina >= UltralightEV1.PAGINA_USUARIO_INICIAL && pagina <= tag.darPaginaUsuarioFinal()
    }

    private fun apagarBytesAutenticacion(byteInicial: Byte): Byte
    {
        val byteParaApagar = ((1 shl 7) or 7).inv()
        return (byteInicial.toInt() and byteParaApagar).toByte()
    }

    private fun verificarEstadoConexion()
    {
        if (!conectado)
        {
            bloqueado = true
            throw AssertionError("El tag no esta conectado")
        }
    }
}