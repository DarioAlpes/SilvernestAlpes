package co.smartobjects.nfc.operacionessobretags

import co.smartobjects.nfc.lectorestags.ILectorTag
import co.smartobjects.nfc.lectorestags.parametrosDeAutenticacion.ParametrosAutenticacionUltralight
import co.smartobjects.nfc.lectorestags.ultralightc.LectorUltralightC
import co.smartobjects.nfc.lectorestags.ultralightc.LectorUltralightCMemoria
import co.smartobjects.nfc.tags.ITag
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertTrue

internal open class OperacionUltralightCBasePruebas
{
    protected val proveedorLlaves = ProveedorLlavesPrueba()
    protected lateinit var paginasUsuarioIniciales: ByteArray
    protected lateinit var operacionesCompuestas: OperacionesCompuestas<ITag, ILectorTag<ITag>>
    private lateinit var lector: LectorUltralightCMemoria


    @BeforeEach
    fun antesDeCadaTest()
    {
        operacionesCompuestas = obtenerOperacionesCompuestasParaUltralightC()
    }

    private fun obtenerOperacionesCompuestasParaUltralightC(): OperacionesCompuestas<ITag, ILectorTag<ITag>>
    {
        lector = LectorUltralightCMemoria()
        paginasUsuarioIniciales = Arrays.copyOf(lector.paginasUsuario, lector.paginasUsuario.size)
        return OperacionesCompuestasUltralightC(lector, proveedorLlaves)
    }

    protected fun activarAutenticacion(operacionesCompuestas: OperacionesCompuestas<ITag, ILectorTag<ITag>>)
    {
        operacionesCompuestas.operacionesBase.lector.conectar()
        operacionesCompuestas.operacionesBase.activarAutenticacion()
        operacionesCompuestas.operacionesBase.lector.desconectar()
    }

    protected fun cambiarLlaveUltralightC(operacionesCompuestas: OperacionesCompuestas<ITag, ILectorTag<ITag>>, llave: ByteArray)
    {
        assertTrue(operacionesCompuestas.operacionesBase.lector is LectorUltralightC, "Se esperaba un lector sobre UltralightC")
        operacionesCompuestas.operacionesBase.lector.conectar()
        operacionesCompuestas.operacionesBase.autenticarConLlaveProveedor()
        (operacionesCompuestas.operacionesBase.lector as LectorUltralightC).escribirLlave(llave)
        operacionesCompuestas.operacionesBase.lector.desconectar()
    }

    protected fun cambiarParametrosAutenticacionUltralightC(operacionesCompuestas: OperacionesCompuestas<ITag, ILectorTag<ITag>>, parametrosAutenticacion: ParametrosAutenticacionUltralight)
    {
        assertTrue(operacionesCompuestas.operacionesBase.lector is LectorUltralightC, "Se esperaba un lector sobre UltralightC")
        operacionesCompuestas.operacionesBase.lector.conectar()
        operacionesCompuestas.operacionesBase.autenticarConLlaveProveedor()
        (operacionesCompuestas.operacionesBase.lector as LectorUltralightC).escribirPaginaActivarAutenticacion(parametrosAutenticacion.protegerLectura)
        (operacionesCompuestas.operacionesBase.lector as LectorUltralightC).escribirPaginaInicialAutenticacion(parametrosAutenticacion.paginaInicialAutenticacion)
        operacionesCompuestas.operacionesBase.lector.desconectar()
    }

    @Test
    fun dar_uid_ultralight_c()
    {
        assertTrue(Arrays.equals(lector.uid, operacionesCompuestas.darUID()))
    }
}