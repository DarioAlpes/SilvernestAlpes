package co.smartobjects.silvernestandroid.utilidades.controlesui

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.support.annotation.ColorRes
import android.support.v4.app.Fragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextSwitcher
import android.widget.TextView
import co.smartobjects.silvernestandroid.R
import co.smartobjects.silvernestandroid.utilidades.UtilidadesInterfaz
import co.smartobjects.silvernestandroid.utilidades.conForegroundColor
import java.lang.ref.WeakReference


internal class ManillaCodificacionFragmento : Fragment()
{
    internal enum class ImagenMano
    {
        TitilearAzul, Gris, Verde, Roja
    }

    private lateinit var iconoMano: WeakReference<ImageView>
    private lateinit var labelEstado: WeakReference<TextSwitcher>
    private lateinit var actividad: WeakReference<Activity>

    private val animacionEntrada by lazy { AnimationUtils.loadAnimation(this@ManillaCodificacionFragmento.activity, android.R.anim.fade_in) }
    private val animacionSalida by lazy { AnimationUtils.loadAnimation(this@ManillaCodificacionFragmento.activity, android.R.anim.fade_out) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val vistaRaiz = inflater.inflate(R.layout.fragmento_mano_codificacion, container, false)

        actividad = WeakReference<Activity>(this@ManillaCodificacionFragmento.activity)

        iconoMano = WeakReference(vistaRaiz.findViewById(R.id.iv_mano_con_manilla) as ImageView)
        labelEstado = WeakReference(vistaRaiz.findViewById(R.id.txvw_label_estado_codificacion) as TextSwitcher)

        labelEstado.get()?.setFactory {
            TextView(actividad.get()).apply {
                layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER)
                gravity = Gravity.CENTER
                setTextAppearance(context, R.style.EtiquetaInterna)
            }
        }
        labelEstado.get()?.inAnimation = animacionEntrada
        labelEstado.get()?.outAnimation = animacionSalida
        labelEstado.get()?.setCurrentText(getString(R.string.mensaje_esperando_manilla))

        return vistaRaiz
    }

    fun cambiarImagenMano(tipo: ImagenMano)
    {
        if (iconoMano.get() != null && actividad.get() != null)
        {
            iconoMano.get()?.clearAnimation()
            when (tipo)
            {
                ManillaCodificacionFragmento.ImagenMano.Gris         ->
                {
                    UtilidadesInterfaz.animarVistaFadeInOut(actividad.get(), iconoMano.get()) {
                        it?.setImageResource(R.drawable.ic_manilla)
                    }
                }
                ManillaCodificacionFragmento.ImagenMano.Verde        ->
                {
                    UtilidadesInterfaz.animarVistaFadeInOut(actividad.get(), iconoMano.get()) {
                        it?.setImageResource(R.drawable.ic_manilla_codificacion_exitosa)
                    }
                }
                ManillaCodificacionFragmento.ImagenMano.Roja         ->
                {
                    UtilidadesInterfaz.animarVistaFadeInOut(actividad.get(), iconoMano.get()) {
                        it?.setImageResource(R.drawable.ic_manilla_codificacion_fallida)
                    }
                }
                ManillaCodificacionFragmento.ImagenMano.TitilearAzul ->
                {
                    iconoMano.get()?.setImageResource(R.drawable.ic_manilla_en_procesamiento)

                    val animation = AlphaAnimation(1f, 0.1f)
                    animation.duration = 400
                    animation.interpolator = LinearInterpolator()
                    animation.repeatCount = Animation.INFINITE
                    animation.repeatMode = Animation.REVERSE
                    iconoMano.get()?.startAnimation(animation)
                }
            }
        }
    }

    fun cambiarMensajeDeEstado(mensaje: String)
    {
        cambiarMensajeDeEstado(mensaje, R.color.etiqueta)
    }

    fun cambiarMensajeDeEstado(mensaje: String, @ColorRes colorMensajeInicial: Int)
    {
        cambiarMensajeDeEstado(mensaje, null, colorMensajeInicial)
    }

    fun cambiarMensajeDeEstado(mensajeInicial: String, mensajeFinal: String?)
    {
        cambiarMensajeDeEstado(mensajeInicial, mensajeFinal, R.color.etiqueta)
    }

    fun cambiarMensajeDeEstado(mensajeInicial: String, mensajeFinal: String?, @ColorRes colorMensajeInicial: Int)
    {
        val actividadRef = actividad.get()
        if (actividadRef != null && labelEstado.get() != null)
        {
            labelEstado.get()?.setText(mensajeInicial.conForegroundColor(actividadRef, colorMensajeInicial))
            if (mensajeFinal != null)
            {
                Handler().postDelayed(
                        {
                            if (isAdded && actividad.get() != null)
                            {
                                this@ManillaCodificacionFragmento.cambiarMensajeDeEstado(mensajeFinal)
                            }
                        }
                        , 2500L)
            }
        }
    }

    fun animarAEstadoPorDefecto(milisegundosDeEspera: Long = 2800)
    {
        animarAEstadoPorDefecto(milisegundosDeEspera, null)
    }

    fun animarAEstadoPorDefecto(milisegundosDeEspera: Long = 2800, callbackPostAnimacion: (() -> Unit)?)
    {
        Handler().postDelayed(
                {
                    if (isAdded && actividad.get() != null)
                    {
                        this@ManillaCodificacionFragmento.cambiarImagenMano(ManillaCodificacionFragmento.ImagenMano.Gris)
                        this@ManillaCodificacionFragmento.cambiarMensajeDeEstado(getString(R.string.mensaje_esperando_manilla))
                        if (callbackPostAnimacion != null)
                        {
                            callbackPostAnimacion()
                        }
                    }
                }
                , milisegundosDeEspera)
    }
}