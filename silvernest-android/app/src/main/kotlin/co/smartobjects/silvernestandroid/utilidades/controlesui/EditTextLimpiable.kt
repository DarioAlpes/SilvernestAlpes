package co.smartobjects.silvernestandroid.utilidades.controlesui

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.support.design.widget.TextInputEditText
import android.support.v7.content.res.AppCompatResources
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import co.smartobjects.silvernestandroid.R

interface ICampoLimpiable
{
    fun setClearIconVisible(visible: Boolean)
}

class EditTextLimpiable : TextInputEditText, View.OnTouchListener, View.OnFocusChangeListener, ICampoLimpiable
{
    private var clearIcon: Drawable? = null
    private var callbacksClearableEditText: CallbacksClearableEditText? = null
    private var callbackTouch: OnTouchListener? = null
    private var callbackFocus: OnFocusChangeListener? = null
    private var texto: String? = null

    constructor(context: Context) : super(context)
    {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)
    {
        init()
    }

    fun definirListnerLimpiado(callbacksClearableEditText: CallbacksClearableEditText)
    {
        this.callbacksClearableEditText = callbacksClearableEditText
    }

    override fun setOnTouchListener(l: OnTouchListener)
    {
        this.callbackTouch = l
    }

    override fun setOnFocusChangeListener(f: OnFocusChangeListener)
    {
        this.callbackFocus = f
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean
    {
        texto = ""
        if (compoundDrawables[2] != null)
        {
            val regionTocable = width - paddingRight
            val tocoIcono = event.x > regionTocable - clearIcon!!.intrinsicWidth && event.x <= regionTocable
            if (tocoIcono)
            {
                if (event.action == MotionEvent.ACTION_UP)
                {
                    setText("")
                    callbacksClearableEditText?.didClearText()
                }
                return true
            }
        }
        return callbackTouch != null && callbackTouch!!.onTouch(v, event)
    }

    override fun onFocusChange(v: View, hasFocus: Boolean)
    {
        if (hasFocus)
        {
            setClearIconVisible(!text.toString().isEmpty())
        }
        else
        {
            setClearIconVisible(false)
        }
        if (callbackFocus != null)
        {
            callbackFocus!!.onFocusChange(v, hasFocus)
        }
    }

    private val defaultClearIconId: Int
        get()
        {
            var id = resources.getIdentifier("ic_clear", "drawable", "android")
            if (id == 0)
            {
                id = android.R.drawable.presence_offline
            }
            return id
        }

    private fun init()
    {
        clearIcon = compoundDrawables[2]
        if (clearIcon == null)
        {
            clearIcon = AppCompatResources.getDrawable(context, defaultClearIconId)
        }
        clearIcon!!.setBounds(0, 0, clearIcon!!.intrinsicWidth, clearIcon!!.intrinsicHeight)
        setClearIconVisible(false)
        super.setOnTouchListener(this)
        super.setOnFocusChangeListener(this)
    }

    override fun setClearIconVisible(visible: Boolean)
    {
        val recursos = context?.resources ?: Resources.getSystem()

        val iconoAMostrar = if (visible) clearIcon else null
        setCompoundDrawables(compoundDrawables[0], compoundDrawables[1], iconoAMostrar, compoundDrawables[3])
        compoundDrawablePadding =
                TypedValue
                    .applyDimension(
                            TypedValue.COMPLEX_UNIT_PX,
                            context!!.resources.getDimension(R.dimen.padding_clear_icon),
                            recursos.displayMetrics
                                   ).toInt()
    }

    interface CallbacksClearableEditText
    {
        fun didClearText()
    }

    fun setTextYEsconderBotonDeLimpiado(texto: String)
    {
        setText(texto)
        setClearIconVisible(false)
    }
}