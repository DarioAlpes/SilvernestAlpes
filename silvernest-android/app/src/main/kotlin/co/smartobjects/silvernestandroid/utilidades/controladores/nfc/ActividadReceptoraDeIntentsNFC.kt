package co.smartobjects.silvernestandroid.utilidades.controladores.nfc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.util.Log
import co.smartobjects.silvernestandroid.AplicacionPrincipal
import co.smartobjects.silvernestandroid.R
import co.smartobjects.silvernestandroid.utilidades.controladores.ActividadBase
import java.lang.ref.WeakReference

abstract class ActividadReceptoraDeIntentsNFC : ActividadBase(), ReceptorEstadoNFC
{
    private var esVisible: Boolean = false
    private var nfcEstaActivo: Boolean = false

    private val mReceiver = object : BroadcastReceiver()
    {
        override fun onReceive(context: Context, intent: Intent)
        {
            val action = intent.action
            if (action == NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)
            {
                val state = intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE, NfcAdapter.STATE_OFF)
                when (state)
                {
                    NfcAdapter.STATE_TURNING_OFF ->
                    {
                        nfcEstaActivo = false
                        ActividadUnicaDeNFC.eliminarReceptorEstadoNFC(this@ActividadReceptoraDeIntentsNFC)
                    }
                    NfcAdapter.STATE_ON          ->
                    {
                        nfcEstaActivo = true
                        if (esVisible)
                        {
                            registrarseComoReceptorDeIntents()
                        }
                    }
                }
            }
        }
    }

    private fun registrarseComoReceptorDeIntents()
    {
        if (nfcEstaActivo)
        {
            ActividadUnicaDeNFC.asignarReceptorEstadoNFC(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        val filter = IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)
        this.registerReceiver(mReceiver, filter)
    }

    override fun onStart()
    {
        esVisible = true
        super.onStart()
        nfcEstaActivo = mostrarDialogoDeActivarNFC()
        registrarseComoReceptorDeIntents()
    }

    override fun onStop()
    {
        super.onStop()
        esVisible = false
        ActividadUnicaDeNFC.eliminarReceptorEstadoNFC(this)
    }

    override fun onDestroy()
    {
        super.onDestroy()
        this.unregisterReceiver(mReceiver)
    }

    /**
     * Método que sirve para informar al usuario que el NFC no está activo
     */
    open fun informarQueNoHayNFCActivo()
    {
        Handler().postDelayed({
                                  val referencia = WeakReference<ActividadReceptoraDeIntentsNFC>(this)
                                  if (referencia.get() != null)
                                  {
                                      val builder = AlertDialog.Builder(referencia.get()!!)
                                          .setTitle(R.string.titulo_dialog_activar_nfc)
                                          .setMessage(R.string.mensaje_dialogo_activar_nfc)
                                          .setPositiveButton(R.string.mensaje_boton_activar_nfc) { _, _ -> startActivity(Intent(Settings.ACTION_NFC_SETTINGS)) }
                                          .setNegativeButton(R.string.mensaje_boton_activar_nfc_despues) { _, _ -> }

                                      val dialogo = builder.create()
                                      if (referencia.get() != null && referencia.get()!!.esVisible && !referencia.get()!!.nfcEstaActivo)
                                      {
                                          dialogo.show()
                                      }
                                  }
                              }, 290L)
    }

    private fun mostrarDialogoDeActivarNFC(): Boolean
    {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null)
        {
            Log.wtf(AplicacionPrincipal.TAG, "Esto no debería pasar")
            return false
        }
        else
        {
            return if (nfcAdapter.isEnabled)
            {
                true
            }
            else
            {
                informarQueNoHayNFCActivo()
                false
            }
        }
    }
}