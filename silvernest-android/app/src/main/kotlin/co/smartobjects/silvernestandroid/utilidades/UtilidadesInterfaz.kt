package co.smartobjects.silvernestandroid.utilidades

import android.content.Context
import android.support.annotation.ColorRes
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.content.res.AppCompatResources
import android.text.*
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import co.smartobjects.silvernestandroid.R
import co.smartobjects.silvernestandroid.utilidades.controlesui.ICampoLimpiable


object UtilidadesInterfaz
{
    const val VALOR_ALFA_DESHABILITADO = 0.38f

    fun inicializarTextInputLayoutConTextoLimpiable(textInputLayout: TextInputLayout, vararg filtros: InputFilter)
    {
        val campo = textInputLayout.editText!!
        val watcher = object : TextWatcher
        {
            override fun onTextChanged(cs: CharSequence, arg1: Int, arg2: Int, arg3: Int)
            {
                (campo as ICampoLimpiable).setClearIconVisible(campo.hasFocus() && cs.toString().isNotEmpty())
            }

            override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int)
            {
            }

            override fun afterTextChanged(textoCambiado: Editable)
            {
                if (textInputLayout.error != null && textoCambiado.isNotEmpty())
                {
                    textInputLayout.error = null
                }
            }
        }
        campo.addTextChangedListener(watcher)

        agregarFiltrosAEditText(campo, *filtros)
    }

    fun agregarFiltrosAEditText(campoDeTexto: EditText, vararg filtrosNuevos: InputFilter)
    {
        val editFilters = campoDeTexto.filters
        val newFilters = kotlin.arrayOfNulls<InputFilter>(editFilters.size + filtrosNuevos.size)
        System.arraycopy(editFilters, 0, newFilters, 0, editFilters.size)
        System.arraycopy(filtrosNuevos, 0, newFilters, editFilters.size, filtrosNuevos.size)
        campoDeTexto.filters = newFilters
    }

    fun darToastError(contextoActividad: Context, @StringRes mensaje: Int, duracion: Int): Toast
    {
        return darToast(true, contextoActividad, contextoActividad.getString(mensaje), duracion)
    }

    fun darToastError(contextoActividad: Context, mensaje: String, duracion: Int): Toast
    {
        return darToast(true, contextoActividad, mensaje, duracion)
    }

    fun darToastEstandar(contextoActividad: Context, @StringRes mensaje: Int, duracion: Int): Toast
    {
        return darToast(false, contextoActividad, contextoActividad.getString(mensaje), duracion)
    }

    fun darToastEstandar(contextoActividad: Context, mensaje: String, duracion: Int): Toast
    {
        return darToast(false, contextoActividad, mensaje, duracion)
    }

    fun mostrarToastError(contextoActividad: Context, @StringRes mensaje: Int, duracion: Int)
    {
        darToastError(contextoActividad, contextoActividad.getString(mensaje), duracion).show()
    }

    fun mostrarToastError(contextoActividad: Context, mensaje: String, duracion: Int)
    {
        darToastError(contextoActividad, mensaje, duracion).show()
    }

    fun mostrarToastEstandar(contextoActividad: Context, @StringRes mensaje: Int, duracion: Int)
    {
        darToastEstandar(contextoActividad, contextoActividad.getString(mensaje), duracion).show()
    }

    fun mostrarToastEstandar(contextoActividad: Context, mensaje: String, duracion: Int)
    {
        darToastEstandar(contextoActividad, mensaje, duracion).show()
    }

    private fun darToast(esDeError: Boolean, contextoActividad: Context, mensaje: String, duracion: Int): Toast
    {
        val inflate = contextoActividad.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val viewToast = inflate.inflate(R.layout.layout_toast, null)

        val contenedorToast = viewToast.findViewById(R.id.contenedor_toast) as LinearLayout

        if (esDeError)
        {
            val drawable = DrawableCompat.wrap(AppCompatResources.getDrawable(contextoActividad, R.drawable.fondo_toast)!!)
            drawable.mutate()
            DrawableCompat.setTint(drawable, ContextCompat.getColor(contextoActividad, R.color.color_fondo_mensaje_de_error))
            contenedorToast.background = drawable
        }

        val mensajeToast = viewToast.findViewById(R.id.mensaje_toast) as TextView
        mensajeToast.text = mensaje

        return Toast(contextoActividad).apply {
            view = viewToast
            val mergenInferior = contextoActividad.resources.getDimensionPixelOffset(R.dimen.margen_inferior_toast)
            setGravity(Gravity.BOTTOM or Gravity.CENTER_VERTICAL, 0, mergenInferior)
            duration = duracion
        }
    }

    /**
     * Método para animar un View con una función de cambio sobre [vista] utilizando fade-in y fade-out
     * @param ctx Contexto desde el cual se llama el método
     * *
     * @param vista Vista a animar.
     * *
     * @param funcionDeCambio Función encargada de cambiar el estado de la vista
     */
    fun <TipoVista : View?> animarVistaFadeInOut(ctx: Context?, vista: TipoVista, funcionDeCambio: (TipoVista) -> Unit)
    {
        val out = AnimationUtils.loadAnimation(ctx, android.R.anim.fade_out)
        vista?.startAnimation(out)
        out.setAnimationListener(
                object : Animation.AnimationListener
                {
                    override fun onAnimationStart(animation: Animation)
                    {
                    }

                    override fun onAnimationEnd(animation: Animation)
                    {
                        funcionDeCambio(vista)
                        vista?.startAnimation(AnimationUtils.loadAnimation(ctx, android.R.anim.fade_in))
                    }

                    override fun onAnimationRepeat(animation: Animation)
                    {
                    }
                })
    }
}

inline var View.isVisible: Boolean
    get() = visibility == View.VISIBLE
    set(value)
    {
        visibility = if (value) View.VISIBLE else View.GONE
    }

val ViewGroup.children: Sequence<View>
    get() = object : Sequence<View>
    {
        override fun iterator() = this@children.iterator()
    }

operator fun ViewGroup.iterator() = object : MutableIterator<View>
{
    private var index = 0
    override fun hasNext() = index < childCount
    override fun next() = getChildAt(index++) ?: throw IndexOutOfBoundsException()
    override fun remove() = removeViewAt(--index)
}

fun CharSequence.comoSpannable(): Spannable = SpannableString.valueOf(this)
fun Char.comoSpannable(): Spannable = SpannableString(toString())

fun Spannable.spanEntero(span: Any): Spannable
{
    setSpan(span, 0, length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
    return this
}

fun CharSequence.conForegroundColor(contexto: Context, @ColorRes idColor: Int): Spannable
{
    return comoSpannable().spanEntero(ForegroundColorSpan(ContextCompat.getColor(contexto, idColor)))
}