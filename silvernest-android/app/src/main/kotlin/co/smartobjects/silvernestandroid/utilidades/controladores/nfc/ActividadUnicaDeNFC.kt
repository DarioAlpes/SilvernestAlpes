package co.smartobjects.silvernestandroid.utilidades.controladores.nfc

import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import co.smartobjects.nfc.ResultadoNFC
import co.smartobjects.nfc.operacionessobretags.OperacionesCompuestas
import co.smartobjects.nfc.operacionessobretags.OperacionesCompuestasUltralightC
import co.smartobjects.nfc.operacionessobretags.ProveedorLlaves
import co.smartobjects.silvernestandroid.AplicacionPrincipal
import co.smartobjects.silvernestandroid.BuildConfig
import co.smartobjects.silvernestandroid.utilidades.nfc.LectorTaplinxUltralightC
import com.nxp.nfclib.CardType
import com.nxp.nfclib.NxpNfcLib
import com.nxp.nfclib.ultralight.UltralightFactory
import org.koin.android.ext.android.inject
import java.util.concurrent.Semaphore


interface ReceptorEstadoNFC
{
    fun seDebeEjecutarOperacion(): Boolean
    fun callbackTagDetectado()
    fun callbackResultadoNFC(resultadoNFC: ResultadoNFC)
}

class ActividadUnicaDeNFC : AppCompatActivity()
{
    companion object
    {
        @JvmStatic
        var receptorEstadoNFC: ReceptorEstadoNFC? = null
            @JvmStatic @Synchronized get
            @JvmStatic private set

        @JvmStatic
        @Synchronized
        internal fun eliminarReceptorEstadoNFC(receptorAEliminar: ReceptorEstadoNFC)
        {
            if (receptorEstadoNFC === receptorAEliminar)
            {
                receptorEstadoNFC = null
            }
        }

        @JvmStatic
        @Synchronized
        internal fun asignarReceptorEstadoNFC(receptorAAsignar: ReceptorEstadoNFC)
        {
            receptorEstadoNFC = receptorAAsignar
        }
    }

    private val libInstance: NxpNfcLib = NxpNfcLib.getInstance()
    private var ultimoTipoTarjetaLeido: CardType = CardType.UnknownCard
    private val semaforoNFC: Semaphore = Semaphore(1, true)

    private val proveedorLlaves: ProveedorLlaves by inject()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        if (!libInstance.isActivityRegistered)
        {
            libInstance.registerActivity(this, BuildConfig.TAPLINX_API_KEY)
        }
    }

    override fun onResume()
    {
        super.onResume()

        if (intent != null)
        {
            procesarIntent(intent)
        }
        else
        {
            // No debería pasar
            finish()
        }
    }

    private fun procesarIntent(intentRecibido: Intent)
    {
        val action = intentRecibido.action
        if (action == NfcAdapter.ACTION_TAG_DISCOVERED || action == NfcAdapter.ACTION_TECH_DISCOVERED)
        {
            try
            {
                ultimoTipoTarjetaLeido = libInstance.getCardType(intentRecibido)

                Log.d(AplicacionPrincipal.TAG, "New intent on ActividadUnicaDeNFC: " + intentRecibido.action)
                Log.d(AplicacionPrincipal.TAG, "Tag Type = $ultimoTipoTarjetaLeido")
                procesarTagSegunTipo(ultimoTipoTarjetaLeido)
            }
            catch (e: Exception)
            {
                Log.e(AplicacionPrincipal.TAG, "", e)
            }
        }
    }

    private fun procesarTagSegunTipo(tipo: CardType)
    {
        val receptorDeIntentsFinal = receptorEstadoNFC
        if (receptorDeIntentsFinal != null && receptorDeIntentsFinal.seDebeEjecutarOperacion())
        {
            if (semaforoNFC.tryAcquire())
            {
                receptorDeIntentsFinal.callbackTagDetectado()

                val posibleResultadoNFC = generarResultadoNFCSegunTipoDeTag(tipo)
                if (posibleResultadoNFC != null)
                {
                    receptorDeIntentsFinal.callbackResultadoNFC(posibleResultadoNFC)
                }

                semaforoNFC.release()
            }
        }

        finish()
    }

    private fun generarResultadoNFCSegunTipoDeTag(tipo: CardType): ResultadoNFC?
    {
        val operacionesTag: OperacionesCompuestas<*, *> =
                when (tipo)
                {
                    CardType.UltralightC ->
                    {
                        val ultralightC = UltralightFactory.getInstance().getUltralightC(libInstance.customModules)
                        val lector = LectorTaplinxUltralightC(ultralightC)
                        OperacionesCompuestasUltralightC(lector, proveedorLlaves)
                    }
                    else                 ->
                    {
                        return ResultadoNFC.Error.TagNoSoportado(libInstance.getCardType(intent).tagName)
                    }
                }

        return ResultadoNFC.Exitoso(operacionesTag)
    }
}