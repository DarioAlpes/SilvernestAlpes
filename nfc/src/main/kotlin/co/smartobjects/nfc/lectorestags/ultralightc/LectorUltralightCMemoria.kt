package co.smartobjects.nfc.lectorestags.ultralightc

import co.smartobjects.nfc.tags.mifare.ultralight.UltralightC
import co.smartobjects.nfc.utils.byteAIntSinSigno
import java.util.*

class LectorUltralightCMemoria : LectorUltralightC()
{
    private val _tag = UltralightC()
    var uid = byteArrayOf(1, 2, 3, 4)
    val llave: ByteArray = Arrays.copyOf(UltralightC.LLAVE_POR_DEFECTO, UltralightC.LLAVE_POR_DEFECTO.size)
    internal var paginasUsuario = ByteArray(UltralightC.PAGINAS_DE_USUARIO * UltralightC.BYTES_POR_PAGINA)
    var paginaConfiguracionPaginaInicialAutenticacion = byteArrayOf(0x30.toByte(), 5, 6, 7)
    var paginaConfiguracionActivarAutenticacion = byteArrayOf(1, 8, 9, 10)
    private var conectado = false
    private var bloqueado = false
    private var autenticado = false
    override val tag: UltralightC
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
            val posicionInicial = (paginaInicial - UltralightC.PAGINA_USUARIO_INICIAL) * UltralightC.BYTES_POR_PAGINA
            val posicionFinal = (paginaFinal - UltralightC.PAGINA_USUARIO_INICIAL + 1) * UltralightC.BYTES_POR_PAGINA
            return Arrays.copyOfRange(paginasUsuario, posicionInicial, posicionFinal)
        }
        else if (paginaInicial == paginaFinal)
        {
            if (paginaInicial == UltralightC.PAGINA_CONFIGURACION_PAGINA_INICIAL_AUTENTICACION)
            {
                return Arrays.copyOf(paginaConfiguracionPaginaInicialAutenticacion, paginaConfiguracionPaginaInicialAutenticacion.size)
            }
            else if (paginaInicial == UltralightC.PAGINA_ACTIVAR_AUTENTICACION)
            {
                return Arrays.copyOf(paginaConfiguracionActivarAutenticacion, paginaConfiguracionActivarAutenticacion.size)
            }
        }
        bloqueado = true
        throw AssertionError("Solo se soporta la lectura de las paginas de configuracion y las paginas de usuario")
    }

    override fun autenticar(llave: ByteArray): Boolean
    {
        verificarEstadoConexion()
        autenticado = Arrays.equals(llave, this.llave)
        return autenticado
    }

    override fun escribirPagina(paginaAEscribir: Byte, datos: ByteArray)
    {
        if (datos.size != UltralightC.BYTES_POR_PAGINA.toInt())
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
            val posicionInicialUsuario = (paginaAEscribir - UltralightC.PAGINA_USUARIO_INICIAL) * UltralightC.BYTES_POR_PAGINA
            System.arraycopy(datos, 0, paginasUsuario, posicionInicialUsuario, UltralightC.BYTES_POR_PAGINA.toInt())
        }
        else if (esPaginaDeLlave(paginaAEscribir))
        {
            datos.reverse()
            val paginaLlave = paginaAEscribir - UltralightC.PAGINA_INICIAL_LLAVE
            val posicionLlave = if (paginaLlave < 2)
            {
                (1 - paginaLlave) * UltralightC.BYTES_POR_PAGINA
            }
            else
            {
                (5 - paginaLlave) * UltralightC.BYTES_POR_PAGINA
            }
            System.arraycopy(datos, 0, llave, posicionLlave, datos.size)
        }
        else if (paginaAEscribir == UltralightC.PAGINA_CONFIGURACION_PAGINA_INICIAL_AUTENTICACION)
        {
            for (i in 1..3)
            {
                if (datos[i] != paginaConfiguracionPaginaInicialAutenticacion[i])
                {
                    bloqueado = true
                    throw AssertionError("Error en pagina de configuración para pagina inicial de autenticación, se esta modificando un byte incorrecto")
                }
            }
            if (datos[0] < 0x03 || datos[0] > 0x30)
            {
                throw AssertionError("La pagina de configuración inicial debe estar entre 0x03 y 0x30 (inclusive)")
            }
            paginaConfiguracionPaginaInicialAutenticacion = Arrays.copyOf(datos, paginaConfiguracionPaginaInicialAutenticacion.size)
        }
        else if (paginaAEscribir == UltralightC.PAGINA_ACTIVAR_AUTENTICACION)
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

    fun esPaginaDeLlave(pagina: Byte): Boolean
    {
        return pagina >= UltralightC.PAGINA_INICIAL_LLAVE && pagina <= (UltralightC.PAGINA_INICIAL_LLAVE + UltralightC.NUMERO_BYTES_LLAVE - 1)
    }

    private fun puedeLeerPagina(pagina: Byte): Boolean
    {
        return puedeEscribirPagina(pagina) || !estaActivadaProteccionDeLectura()
    }

    private fun puedeEscribirPagina(pagina: Byte): Boolean
    {
        return autenticado || byteAIntSinSigno(pagina) < byteAIntSinSigno(paginaConfiguracionPaginaInicialAutenticacion[0])
    }

    private fun estaActivadaProteccionDeLectura(): Boolean
    {
        return (1 and paginaConfiguracionPaginaInicialAutenticacion[0].toInt()) == 1
    }

    private fun esPaginaDeUsuario(pagina: Byte): Boolean
    {
        return pagina >= UltralightC.PAGINA_USUARIO_INICIAL && pagina <= UltralightC.PAGINA_USUARIO_FINAL
    }

    private fun apagarBytesAutenticacion(byteInicial: Byte): Byte
    {
        val byteParaApagar = 1.inv()
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