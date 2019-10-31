package co.smartobjects.silvernestandroid.conteodeubicaciones

import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import co.smartobjects.entidades.ubicaciones.Ubicacion
import co.smartobjects.nfc.ResultadoNFC
import co.smartobjects.silvernestandroid.AplicacionPrincipal
import co.smartobjects.silvernestandroid.R
import co.smartobjects.silvernestandroid.utilidades.UtilidadesInterfaz
import co.smartobjects.silvernestandroid.utilidades.controladores.nfc.ActividadReceptoraDeIntentsNFC
import co.smartobjects.silvernestandroid.utilidades.controlesui.ManillaCodificacionFragmento
import co.smartobjects.silvernestandroid.utilidades.controlesui.SpinnerListenerSoloParaUsuario
import co.smartobjects.silvernestandroid.utilidades.isVisible
import co.smartobjects.silvernestandroid.utilidades.observarEnUI
import co.smartobjects.ui.modelos.contabilizacionubicaciones.ProcesoContabilizacionUbicacionesUI
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import org.koin.android.ext.android.inject
import org.koin.android.scope.ext.android.bindScope
import org.koin.android.scope.ext.android.getOrCreateScope


class ConteoDeUbicacionesActividad : ActividadReceptoraDeIntentsNFC()
{
    override val idLayout: Int = R.layout.conteo_de_ubicaciones_actividad

    private val menuUbicaciones by lazy(LazyThreadSafetyMode.NONE) { findViewById<SpinnerListenerSoloParaUsuario>(R.id.spinner_ubicaciones) }
    private val fragmentoIconoManilla by lazy { supportFragmentManager.findFragmentById(R.id.fragmento_icono_manilla) as ManillaCodificacionFragmento }
    private val conteo by lazy(LazyThreadSafetyMode.NONE) { findViewById<TextView>(R.id.label_conteo) }

    private val procesoContabilizacionUbicaciones: ProcesoContabilizacionUbicacionesUI by inject()

    private val adaptador by lazy { AdaptadorUbicacionesContabilizables(this@ConteoDeUbicacionesActividad, R.layout.item_de_spinner_sencillo) }

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        bindScope(getOrCreateScope(ProcesoContabilizacionUbicacionesUI::class.simpleName!!))

        menuUbicaciones.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener
                {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long)
                    {
                        procesoContabilizacionUbicaciones.cambiarUbicacion(menuUbicaciones.adapter.getItem(position) as Ubicacion)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>)
                    {
                    }
                }

        menuUbicaciones.adapter = adaptador

        procesoContabilizacionUbicaciones
            .ubicacionesContabilizables
            .observarEnUI()
            .subscribe { ubicaciones ->
                adaptador.clear()
                adaptador.addAll(ubicaciones)
                adaptador.notifyDataSetChanged()
                menuUbicaciones.setSelection(0)
            }
            .addTo(compositeDisposable)

        procesoContabilizacionUbicaciones
            .conteo
            .observarEnUI()
            .subscribe {
                conteo.text = getString(R.string.mensaje_conteo, it)
            }
            .addTo(compositeDisposable)

        procesoContabilizacionUbicaciones
            .mensajesDeError
            .filter { it.isNotEmpty() }
            .observarEnUI()
            .subscribe {
                UtilidadesInterfaz.mostrarToastError(this@ConteoDeUbicacionesActividad, it, Toast.LENGTH_LONG)
            }
            .addTo(compositeDisposable)

        procesoContabilizacionUbicaciones
            .estado
            .observarEnUI()
            .subscribe {
                Log.d(AplicacionPrincipal.TAG, "ProcesoContabilizacionUbicacionesUI.Estado = $it")
                when (it!!)
                {
                    ProcesoContabilizacionUbicacionesUI.Estado.CargandoUbicaciones ->
                    {
                        seDebeEjecutarOperacion = false
                        menuUbicaciones.isEnabled = false

                        fragmentoIconoManilla.cambiarImagenMano(ManillaCodificacionFragmento.ImagenMano.Gris)
                        fragmentoIconoManilla.cambiarMensajeDeEstado(getString(R.string.mensaje_cargando_ubicaciones))
                    }
                    ProcesoContabilizacionUbicacionesUI.Estado.UbicacionesCargadas ->
                    {
                        menuUbicaciones.isEnabled = true
                    }
                    is ProcesoContabilizacionUbicacionesUI.Estado.EsperandoTag     ->
                    {
                        seDebeEjecutarOperacion = true
                        menuUbicaciones.isEnabled = true

                        when (it)
                        {
                            ProcesoContabilizacionUbicacionesUI.Estado.EsperandoTag.Neutro          ->
                            {
                                fragmentoIconoManilla.cambiarImagenMano(ManillaCodificacionFragmento.ImagenMano.Gris)
                                fragmentoIconoManilla.cambiarMensajeDeEstado(getString(R.string.mensaje_esperando_manilla))
                            }
                            ProcesoContabilizacionUbicacionesUI.Estado.EsperandoTag.AnteriorExitoso ->
                            {
                                fragmentoIconoManilla.cambiarImagenMano(ManillaCodificacionFragmento.ImagenMano.Verde)
                                fragmentoIconoManilla.cambiarMensajeDeEstado(getString(R.string.mensaje_conteo_correcto), R.color.operacion_exitosa)
                                fragmentoIconoManilla.animarAEstadoPorDefecto()
                            }
                            ProcesoContabilizacionUbicacionesUI.Estado.EsperandoTag.AnteriorFallido ->
                            {
                                fragmentoIconoManilla.cambiarImagenMano(ManillaCodificacionFragmento.ImagenMano.Roja)
                                fragmentoIconoManilla.cambiarMensajeDeEstado(getString(R.string.error_conteo_fallido), R.color.operacion_fallida)
                                fragmentoIconoManilla.animarAEstadoPorDefecto()
                            }
                        }
                    }
                    ProcesoContabilizacionUbicacionesUI.Estado.Codificando         ->
                    {
                        seDebeEjecutarOperacion = false
                        menuUbicaciones.isEnabled = false
                    }
                }
            }
            .addTo(compositeDisposable)
    }

    override fun configurarToolbar(instanciaToolbar: Toolbar?)
    {
        super.configurarToolbar(instanciaToolbar)

        if (instanciaToolbar != null)
        {
            supportActionBar!!.setDisplayHomeAsUpEnabled(false)

            with(instanciaToolbar.findViewById<TextView>(R.id.toolbar_titulo))
            {
                isVisible = true
                setText(R.string.mensaje_conteo_en_ubicacion)
            }
        }
    }

    private var seDebeEjecutarOperacion = false
    override fun seDebeEjecutarOperacion(): Boolean = seDebeEjecutarOperacion

    override fun callbackTagDetectado()
    {
        fragmentoIconoManilla.cambiarImagenMano(ManillaCodificacionFragmento.ImagenMano.TitilearAzul)
        fragmentoIconoManilla.cambiarMensajeDeEstado(getString(R.string.mensaje_intentando_realizar_conteo))
    }

    override fun callbackResultadoNFC(resultadoNFC: ResultadoNFC)
    {
        Log.d(AplicacionPrincipal.TAG, resultadoNFC.toString())

        when (resultadoNFC)
        {
            is ResultadoNFC.Exitoso              ->
            {
                procesoContabilizacionUbicaciones.contabilizarManilla(resultadoNFC)
            }
            is ResultadoNFC.Error.TagNoSoportado ->
            {
                UtilidadesInterfaz.mostrarToastError(this, "El tag '${resultadoNFC.nombreTag}' no se encuentra soportado", Toast.LENGTH_LONG)
            }
            ResultadoNFC.Error.ConectandoseAlTag ->
            {
                UtilidadesInterfaz.mostrarToastError(this, "Error al conectarse al tag", Toast.LENGTH_LONG)
            }
        }
    }
}
