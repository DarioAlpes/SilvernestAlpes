package co.smartobjects.nfc.operacionessobretags

import co.smartobjects.nfc.excepciones.NFCProtocolException
import co.smartobjects.nfc.lectorestags.ILectorTag
import co.smartobjects.nfc.lectorestags.ultralightc.LectorUltralightC
import co.smartobjects.nfc.lectorestags.ultralightev1.LectorUltralightEV1
import co.smartobjects.nfc.tags.ITag
import co.smartobjects.nfc.tags.mifare.ultralight.UltralightC
import co.smartobjects.nfc.tags.mifare.ultralight.UltralightEV1

abstract class OperacionesCompuestas<out Tag : ITag, out LectorTag : ILectorTag<Tag>>
(
        val operacionesBase: OperacionesBase<Tag, LectorTag>
)
{
    fun escribirTag(datosAEscribir: ByteArray): Boolean
    {
        try
        {
            operacionesBase.lector.conectar()
            if (!(operacionesBase.autenticarConLlaveProveedor() && operacionesBase.estaActivadaLaAutenticacion()))
            {
                operacionesBase.lector.reconectar()
                if (operacionesBase.autenticarConLlavePorDefecto())
                {
                    operacionesBase.activarAutenticacion()
                }
                else
                {
                    return false
                }
            }
            operacionesBase.escribirDatosDeUsuario(datosAEscribir)
            return true
        }
        catch (nfcException: NFCProtocolException)
        {
            return false
        }
        catch (e: Exception)
        {
            return false
        }
        finally
        {
            operacionesBase.lector.desconectar()
        }
    }

    fun darUID(): ByteArray
    {
        try
        {
            operacionesBase.lector.conectar()
            return operacionesBase.lector.darUID()
        }
        finally
        {
            operacionesBase.lector.desconectar()
        }
    }

    fun borrarTag(): Boolean
    {
        try
        {
            operacionesBase.lector.conectar()
            if (!operacionesBase.autenticarConLlaveProveedor())
            {
                operacionesBase.lector.reconectar()
                if (!operacionesBase.autenticarConLlavePorDefecto())
                {
                    return false
                }
            }
            operacionesBase.desactivarAutenticacion()
            operacionesBase.borrarDatosDeUsuario()
            return true
        }
        catch (nfcException: NFCProtocolException)
        {
            return false
        }
        catch (e: Exception)
        {
            return false
        }
        finally
        {
            operacionesBase.lector.desconectar()
        }
    }

    fun leerTag(): ResultadoLecturaNFC
    {
        try
        {
            operacionesBase.lector.conectar()
            if (operacionesBase.autenticarConLlaveProveedor())
            {
                return if (operacionesBase.estaActivadaLaAutenticacion())
                {

                    ResultadoLecturaNFC.TagLeido(operacionesBase.leerTodosLosDatosDeUsuario())
                }
                else
                {
                    ResultadoLecturaNFC.SinAutenticacionActivada
                }
            }
            else
            {
                operacionesBase.lector.reconectar()
                return if (operacionesBase.autenticarConLlavePorDefecto())
                {
                    ResultadoLecturaNFC.TagVacio
                }
                else
                {
                    ResultadoLecturaNFC.LlaveDesconocida
                }
            }
        }
        catch (nfcException: NFCProtocolException)
        {
            return ResultadoLecturaNFC.ErrorDeLectura(nfcException)
        }
        catch (e: Exception)
        {
            return ResultadoLecturaNFC.ErrorDeLectura(e)
        }
        finally
        {
            operacionesBase.lector.desconectar()
        }
    }
}

sealed class ResultadoLecturaNFC
{
    object TagVacio : ResultadoLecturaNFC()
    object LlaveDesconocida : ResultadoLecturaNFC()
    object SinAutenticacionActivada : ResultadoLecturaNFC()
    class ErrorDeLectura(val e: Exception) : ResultadoLecturaNFC()
    class TagLeido(val valor: ByteArray) : ResultadoLecturaNFC()
}

class OperacionesCompuestasUltralightEV1(lector: LectorUltralightEV1, proveedorLlave: ProveedorLlaves) :
        OperacionesCompuestas<UltralightEV1, LectorUltralightEV1>(OperacionesBaseUltralightEV1(lector, proveedorLlave))

class OperacionesCompuestasUltralightC(lector: LectorUltralightC, proveedorLlave: ProveedorLlaves) :
        OperacionesCompuestas<UltralightC, LectorUltralightC>(OperacionesBaseUltralightC(lector, proveedorLlave))