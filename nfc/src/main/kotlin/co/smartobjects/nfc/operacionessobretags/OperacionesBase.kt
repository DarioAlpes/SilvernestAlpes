package co.smartobjects.nfc.operacionessobretags

import co.smartobjects.nfc.lectorestags.ILectorTag
import co.smartobjects.nfc.lectorestags.ultralightc.LectorUltralightC
import co.smartobjects.nfc.lectorestags.ultralightev1.LectorUltralightEV1
import co.smartobjects.nfc.tags.ITag
import co.smartobjects.nfc.tags.mifare.ultralight.UltralightC
import co.smartobjects.nfc.tags.mifare.ultralight.UltralightEV1

abstract class OperacionesBase<out Tag : ITag, out LectorTag : ILectorTag<Tag>>
(
        val lector: LectorTag,
        protected val proveedorLlave: ProveedorLlaves
)
{
    abstract fun autenticarConLlaveProveedor(): Boolean
    abstract fun autenticarConLlavePorDefecto(): Boolean
    abstract fun estaActivadaLaAutenticacion(): Boolean
    abstract fun leerTodosLosDatosDeUsuario(): ByteArray
    abstract fun activarAutenticacion()
    abstract fun escribirDatosDeUsuario(datosAEscribir: ByteArray)
    abstract fun desactivarAutenticacion()
    abstract fun borrarDatosDeUsuario()

    fun obtenerLlave(): ByteArray
    {
        return proveedorLlave.generarLlaveDeTagSegunUUIDyLlaveMaestra(lector)
    }
}

class OperacionesBaseUltralightEV1(lector: LectorUltralightEV1, proveedorLlave: ProveedorLlaves) :
        OperacionesBase<UltralightEV1, LectorUltralightEV1>(lector, proveedorLlave)
{
    override fun autenticarConLlaveProveedor(): Boolean
    {
        return lector.autenticar(obtenerLlave(), UltralightEV1.PACK_POR_DEFECTO)
    }

    override fun autenticarConLlavePorDefecto(): Boolean
    {
        return lector.autenticar(UltralightEV1.CONTRASEÑA_POR_DEFECTO, UltralightEV1.PACK_POR_DEFECTO)
    }

    override fun estaActivadaLaAutenticacion(): Boolean
    {
        return lector.verificarParametrosDeAutenticacion(UltralightEV1.PARAMETROS_DE_CONFIGURACION_DE_AUTENTICACION)
    }

    override fun leerTodosLosDatosDeUsuario(): ByteArray
    {
        return lector.leerTodasLasPaginasDeUsuario()
    }

    override fun activarAutenticacion()
    {
        lector.activarAutenticacion(obtenerLlave(), UltralightEV1.PACK_POR_DEFECTO, UltralightEV1.PARAMETROS_DE_CONFIGURACION_DE_AUTENTICACION)
    }

    override fun escribirDatosDeUsuario(datosAEscribir: ByteArray)
    {
        lector.escribirEnPaginasDeUsuario(datosAEscribir)
    }

    override fun desactivarAutenticacion()
    {
        lector.desactivarAutenticacion()
    }

    override fun borrarDatosDeUsuario()
    {
        lector.borrarPaginasDeUsuario()
    }
}

class OperacionesBaseUltralightC(lector: LectorUltralightC, proveedorLlave: ProveedorLlaves) :
        OperacionesBase<UltralightC, LectorUltralightC>(lector, proveedorLlave)
{
    override fun autenticarConLlaveProveedor(): Boolean
    {
        return lector.autenticar(obtenerLlave())
    }

    override fun autenticarConLlavePorDefecto(): Boolean
    {
        return lector.autenticar(UltralightC.LLAVE_POR_DEFECTO)
    }

    override fun estaActivadaLaAutenticacion(): Boolean
    {
        return lector.verificarParametrosDeAutenticacion(UltralightC.PARAMETROS_DE_CONFIGURACION_DE_AUTENTICACION)
    }

    override fun leerTodosLosDatosDeUsuario(): ByteArray
    {
        return lector.leerTodasLasPaginasDeUsuario()
    }

    override fun activarAutenticacion()
    {
        lector.activarAutenticacion(obtenerLlave(), UltralightC.PARAMETROS_DE_CONFIGURACION_DE_AUTENTICACION)
    }

    override fun escribirDatosDeUsuario(datosAEscribir: ByteArray)
    {
        lector.escribirEnPaginasDeUsuario(datosAEscribir)
    }

    override fun desactivarAutenticacion()
    {
        lector.desactivarAutenticacion()
    }

    override fun borrarDatosDeUsuario()
    {
        lector.borrarPaginasDeUsuario()
    }
}