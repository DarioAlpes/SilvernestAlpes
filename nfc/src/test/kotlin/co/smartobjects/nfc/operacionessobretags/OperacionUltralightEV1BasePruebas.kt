package co.smartobjects.nfc.operacionessobretags

import co.smartobjects.nfc.lectorestags.ILectorTag
import co.smartobjects.nfc.lectorestags.parametrosDeAutenticacion.ParametrosAutenticacionUltralight
import co.smartobjects.nfc.lectorestags.ultralightev1.LectorUltralightEV1
import co.smartobjects.nfc.lectorestags.ultralightev1.LectorUltralightEV1Memoria
import co.smartobjects.nfc.tags.ITag
import co.smartobjects.nfc.tags.mifare.ultralight.UltralightEV1
import java.util.*
import kotlin.test.assertTrue

internal open class OperacionUltralightEV1BasePruebas
{
    protected val proveedorLlaves = ProveedorLlavesPrueba()
    protected lateinit var paginasUsuarioIniciales: ByteArray
    protected lateinit var operacionesCompuestas: OperacionesCompuestas<ITag, ILectorTag<ITag>>
    protected lateinit var lector: LectorUltralightEV1Memoria

    protected fun obtenerOperacionesCompuestasParaUltralightEV1(version: UltralightEV1.VersionUltralightEV1): OperacionesCompuestas<ITag, ILectorTag<ITag>>
    {
        lector = LectorUltralightEV1Memoria(version.hexDeVersion)
        paginasUsuarioIniciales = Arrays.copyOf(lector.paginasUsuario, lector.paginasUsuario.size)
        return OperacionesCompuestasUltralightEV1(lector, proveedorLlaves)
    }

    protected fun activarAutenticacion(operacionesCompuestas: OperacionesCompuestas<ITag, ILectorTag<ITag>>)
    {
        operacionesCompuestas.operacionesBase.lector.conectar()
        operacionesCompuestas.operacionesBase.activarAutenticacion()
        operacionesCompuestas.operacionesBase.lector.desconectar()
    }

    protected fun cambiarContraseñaUltralightEV1(operacionesCompuestas: OperacionesCompuestas<ITag, ILectorTag<ITag>>, contraseña: ByteArray)
    {
        assertTrue(operacionesCompuestas.operacionesBase.lector is LectorUltralightEV1, "Se esperaba un lector sobre UltralightEV1")
        operacionesCompuestas.operacionesBase.lector.conectar()
        operacionesCompuestas.operacionesBase.autenticarConLlaveProveedor()
        (operacionesCompuestas.operacionesBase.lector as LectorUltralightEV1).escribirContraseña(contraseña)
        operacionesCompuestas.operacionesBase.lector.desconectar()
    }

    protected fun cambiarPackUltralightEV1(operacionesCompuestas: OperacionesCompuestas<ITag, ILectorTag<ITag>>, pack: ByteArray)
    {
        assertTrue(operacionesCompuestas.operacionesBase.lector is LectorUltralightEV1, "Se esperaba un lector sobre UltralightEV1")
        operacionesCompuestas.operacionesBase.lector.conectar()
        operacionesCompuestas.operacionesBase.autenticarConLlaveProveedor()
        (operacionesCompuestas.operacionesBase.lector as LectorUltralightEV1).escribirPack(pack)
        operacionesCompuestas.operacionesBase.lector.desconectar()
    }

    protected fun cambiarParametrosAutenticacionUltralightEV1(operacionesCompuestas: OperacionesCompuestas<ITag, ILectorTag<ITag>>, parametrosAutenticacion: ParametrosAutenticacionUltralight)
    {
        assertTrue(operacionesCompuestas.operacionesBase.lector is LectorUltralightEV1, "Se esperaba un lector sobre UltralightEV1")
        operacionesCompuestas.operacionesBase.lector.conectar()
        operacionesCompuestas.operacionesBase.autenticarConLlaveProveedor()
        (operacionesCompuestas.operacionesBase.lector as LectorUltralightEV1).escribirPaginaActivarAutenticacion(parametrosAutenticacion.maximoNumeroIntentos, parametrosAutenticacion.protegerLectura)
        (operacionesCompuestas.operacionesBase.lector as LectorUltralightEV1).escribirPaginaInicialAutenticacion(parametrosAutenticacion.paginaInicialAutenticacion)
        operacionesCompuestas.operacionesBase.lector.desconectar()
    }
}