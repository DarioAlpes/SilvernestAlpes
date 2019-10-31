package co.smartobjects.nfc.operacionessobretags

import co.smartobjects.nfc.lectorestags.ILectorTag
import co.smartobjects.nfc.tags.ITag
import co.smartobjects.nfc.utils.comprimir
import co.smartobjects.nfc.utils.descomprimir
import co.smartobjects.utilidades.aHexString
import org.junit.Assert.assertArrayEquals
import java.nio.charset.StandardCharsets
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


internal class ProveedorLlavesPrueba : ProveedorLlaves
{
    override val llaveMaestra: ByteArray
        get() = ByteArray(128, Int::toByte)

    override fun generarLlaveDeTagSegunUUIDyLlaveMaestra(lector: ILectorTag<ITag>): ByteArray
    {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val numeroBits = lector.tag.darTamañoLlave() * 8
        val spec =
                PBEKeySpec(
                        String(llaveMaestra, StandardCharsets.US_ASCII).toCharArray(),
                        lector.darUID(),
                        1000,
                        numeroBits
                          )
        val tmp = factory.generateSecret(spec)
        return tmp.encoded
    }
}


internal class CallbackLecturaPruebas(private val resultadoEsperado: ResultadoLecturaNFC) : OperacionLectura.Callback
{
    internal var llamoAlgunCallback = false
        private set

    override fun tagLeidoVacio()
    {
        assertEquals(resultadoEsperado, ResultadoLecturaNFC.TagVacio)
        llamoAlgunCallback = true
    }

    override fun tagConLlaveDesconocida()
    {
        assertEquals(resultadoEsperado, ResultadoLecturaNFC.LlaveDesconocida)
        llamoAlgunCallback = true
    }

    override fun tagSinAutenticacionActivada()
    {
        assertEquals(resultadoEsperado, ResultadoLecturaNFC.SinAutenticacionActivada)
        llamoAlgunCallback = true
    }

    override fun errorLectura()
    {
        assertTrue(resultadoEsperado is ResultadoLecturaNFC.ErrorDeLectura)
        llamoAlgunCallback = true
    }

    override fun tagLeidoConDatos(bytesLeidos: ByteArray)
    {
        assertTrue(resultadoEsperado is ResultadoLecturaNFC.TagLeido, "No se esperaba leer un tag con datos")
        val bytesEsperados = (resultadoEsperado as ResultadoLecturaNFC.TagLeido).valor

        assertArrayEquals("\nBytes esperados: ${bytesEsperados.aHexString(" ")} \nBytes leidos:    ${bytesLeidos.aHexString(" ")}", bytesEsperados, bytesLeidos)
        llamoAlgunCallback = true
    }
}

internal class CallbackEscrituraPruebas(
        private val numeroDeBytesUsuario: Int,
        private val usarCompresion: Boolean,
        val operacionesCompuestas: OperacionesCompuestas<ITag, ILectorTag<ITag>>,
        private val resultadoEsperado: Boolean,
        private val datosEscritos: ByteArray
                                       ) : OperacionEscritura.Callback
{
    internal var llamoAlgunCallback = false
        private set

    override fun tagEscrito()
    {
        assertTrue(resultadoEsperado)
        // Al descomprimir se pierden los 0s del final
        val tamañoPadding = if (usarCompresion)
        {
            datosEscritos.size
        }
        else
        {
            numeroDeBytesUsuario
        }
        val paginasConPadding = ByteArray(tamañoPadding)
        System.arraycopy(datosEscritos, 0, paginasConPadding, 0, datosEscritos.size)
        val callbacksLectura = CallbackLecturaPruebas(ResultadoLecturaNFC.TagLeido(paginasConPadding))
        OperacionLectura(usarCompresion, callbacksLectura, operacionesCompuestas).ejecutarOperacion()
        assertTrue(callbacksLectura.llamoAlgunCallback)
        llamoAlgunCallback = true
    }

    override fun errorEscritura()
    {
        assertFalse(resultadoEsperado)
        llamoAlgunCallback = true
    }
}

internal class CallbackBorradoPruebas(
        val operacionesCompuestas: OperacionesCompuestas<ITag, ILectorTag<ITag>>,
        private val resultadoEsperado: Boolean
                                     ) : OperacionBorrado.Callback
{

    internal var llamoAlgunCallback = false
        private set

    override fun tagBorrado()
    {
        assertTrue(resultadoEsperado)
        val callbacksLectura = CallbackLecturaPruebas(ResultadoLecturaNFC.TagVacio)
        OperacionLectura(false, callbacksLectura, operacionesCompuestas).ejecutarOperacion()
        assertTrue(callbacksLectura.llamoAlgunCallback)
        llamoAlgunCallback = true
    }

    override fun errorBorrado()
    {
        assertFalse(resultadoEsperado)
        llamoAlgunCallback = true
    }
}

class OperacionLectura
(
        private val usarCompresion: Boolean,
        private val callbacks: Callback,
        val operacionesCompuestas: OperacionesCompuestas<ITag, ILectorTag<ITag>>
) : Operacion<ResultadoLecturaNFC>()
{
    override fun operacion(): ResultadoLecturaNFC
    {
        return operacionesCompuestas.leerTag()
    }

    override fun procesarResultado(resultado: ResultadoLecturaNFC)
    {
        when (resultado)
        {
            is ResultadoLecturaNFC.TagLeido                 -> callbacks.tagLeidoConDatos(if (usarCompresion) descomprimir(resultado.valor) else resultado.valor)
            is ResultadoLecturaNFC.TagVacio                 -> callbacks.tagLeidoVacio()
            is ResultadoLecturaNFC.LlaveDesconocida         -> callbacks.tagConLlaveDesconocida()
            is ResultadoLecturaNFC.SinAutenticacionActivada -> callbacks.tagSinAutenticacionActivada()
            is ResultadoLecturaNFC.ErrorDeLectura           -> callbacks.errorLectura()
        }
    }

    interface Callback
    {
        fun tagLeidoVacio()
        fun tagConLlaveDesconocida()
        fun tagSinAutenticacionActivada()
        fun errorLectura()
        fun tagLeidoConDatos(bytesLeidos: ByteArray)
    }
}

class OperacionEscritura
(
        private val usarCompresion: Boolean,
        private val datosAEscribir: ByteArray,
        private val callbacks: Callback,
        val operacionesCompuestas: OperacionesCompuestas<ITag, ILectorTag<ITag>>
) : Operacion<Boolean>()
{
    override fun operacion(): Boolean
    {
        return operacionesCompuestas.escribirTag(if (usarCompresion) comprimir(datosAEscribir) else datosAEscribir)
    }

    override fun procesarResultado(resultado: Boolean)
    {
        if (resultado)
        {
            callbacks.tagEscrito()
        }
        else
        {
            callbacks.errorEscritura()
        }
    }

    interface Callback
    {
        fun tagEscrito()
        fun errorEscritura()
    }
}

class OperacionBorrado
(
        private val callbacks: Callback,
        val operacionesCompuestas: OperacionesCompuestas<ITag, ILectorTag<ITag>>
) : Operacion<Boolean>()
{
    override fun operacion(): Boolean
    {
        return operacionesCompuestas.borrarTag()
    }

    override fun procesarResultado(resultado: Boolean)
    {
        if (resultado)
        {
            callbacks.tagBorrado()
        }
        else
        {
            callbacks.errorBorrado()
        }
    }

    interface Callback
    {
        fun tagBorrado()
        fun errorBorrado()
    }
}