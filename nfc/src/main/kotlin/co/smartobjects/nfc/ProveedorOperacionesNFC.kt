package co.smartobjects.nfc

import co.smartobjects.nfc.operacionessobretags.OperacionesCompuestas
import io.reactivex.Flowable
import io.reactivex.Observable


sealed class ResultadoNFC
{
    class Exitoso(val operacion: OperacionesCompuestas<*, *>) : ResultadoNFC()
    sealed class Error : ResultadoNFC()
    {
        class TagNoSoportado(val nombreTag: String) : ResultadoNFC.Error()
        object ConectandoseAlTag : ResultadoNFC.Error()
    }
}

interface ProveedorOperacionesNFC
{
    var permitirLecturaNFC: Boolean

    /**
     * Emite solo si no está suspendida la lectura NFC
     */
    val resultadosNFCLeidos: Flowable<ResultadoNFC>
    val listoParaLectura: Flowable<Boolean>
    val hayLectorConectado: Flowable<Boolean>
    val errorLector: Observable<Throwable>
}