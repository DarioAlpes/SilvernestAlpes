package co.smartobjects.silvernestandroid.utilidades.controlesui

import android.content.Context
import android.support.design.widget.TextInputLayout
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.annotation.StringRes
import co.smartobjects.silvernestandroid.R
import kotlin.properties.Delegates


open class TextInputLayoutErrorEscondible : TextInputLayout
{
    private var hintSinFoco = ""
    private var hintConTextoOConFoco by Delegates.observable("") { _, _, newValue ->
        if (newValue.isNotEmpty())
        {
            agregarListenerCambioHint()
        }
        else
        {
            editText?.onFocusChangeListener = null
        }
    }
    private var hintConTextoSinFoco = ""

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    {
        inicializarAtributos(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    {
        inicializarAtributos(context, attrs, defStyleAttr)
    }

    private fun inicializarAtributos(context: Context, attrs: AttributeSet, defStyleAttr: Int)
    {
        val atributos = context.theme.obtainStyledAttributes(attrs, R.styleable.TextInputLayoutErrorEscondible, defStyleAttr, 0)

        try
        {
            hintSinFoco = atributos.getString(R.styleable.TextInputLayoutErrorEscondible_hintSinFoco) ?: ""
            hintConTextoOConFoco = atributos.getString(R.styleable.TextInputLayoutErrorEscondible_hintConTextoOConFoco) ?: ""
        }
        finally
        {
            atributos.recycle()
        }
    }

    override fun onFinishInflate()
    {
        super.onFinishInflate()

        if (hintSinFoco.isEmpty())
        {
            hintSinFoco = hint.toString()
        }

        agregarListenerCambioHintSegunTexto()

        if (hintConTextoOConFoco.isNotEmpty())
        {
            agregarListenerCambioHint()
            hint = darHintSegunFocoYTexto()
        }
    }

    private fun agregarListenerCambioHint()
    {
        editText?.setOnFocusChangeListener { _, _ -> hint = darHintSegunFocoYTexto() }
    }

    private fun agregarListenerCambioHintSegunTexto()
    {
        val watcher = object : TextWatcher
        {
            override fun afterTextChanged(s: Editable?)
            {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int)
            {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {
                hint = if (hintConTextoSinFoco.isNotEmpty()) darHintSegunFocoYTexto() else this@TextInputLayoutErrorEscondible.hint
            }
        }

        editText?.addTextChangedListener(watcher)
    }

    private fun darHintSegunFocoYTexto() =
            if (hintConTextoOConFoco.isNotEmpty() && (editText?.text?.isNotEmpty() == true || editText?.hasFocus() == true))
            {
                hintConTextoOConFoco
            }
            else
            {
                hintSinFoco
            }

    override fun setError(pError: CharSequence?)
    {
        super.setError(pError)
        isErrorEnabled = !TextUtils.isEmpty(pError?.trim())
    }

    private inline fun ejecutarSinAnimacion(accion: () -> Unit)
    {
        isHintAnimationEnabled = false
        accion()
        isHintAnimationEnabled = true
    }

    fun darTexto(): String = editText!!.text.toString()

    fun darEditable(): Editable = editText!!.text

    fun setSelection(start: Int, stop: Int)
    {
        editText!!.setSelection(start, stop)
    }

    fun setSelection(index: Int)
    {
        editText!!.setSelection(index)
    }

    fun setTextSinAnimacion(textoNuevo: CharSequence)
    {
        ejecutarSinAnimacion { setText(textoNuevo) }
    }

    fun setTextSinAnimacion(@StringRes idString: Int)
    {
        ejecutarSinAnimacion { setText(idString) }
    }

    fun setText(textoNuevo: CharSequence)
    {
        editText!!.setText(textoNuevo)
        hint = darHintSegunFocoYTexto()
    }

    fun setText(@StringRes idString: Int)
    {
        editText!!.setText(idString)
        hint = darHintSegunFocoYTexto()
    }

    fun requestFocusEnCampo()
    {
        editText!!.requestFocus()
    }

    fun quitarHintSegunFoco()
    {
        hintConTextoOConFoco = ""
    }

    fun setHintSegunFoco(hintSinFoco: String, hintConFoco: String)
    {
        this.hintSinFoco = hintSinFoco
        this.hintConTextoOConFoco = hintConFoco
    }

    fun isEmpty() = darTexto().isEmpty()
    fun isNotEmpty() = darTexto().isNotEmpty()
}