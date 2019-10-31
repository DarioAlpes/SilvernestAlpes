package co.smartobjects.silvernestandroid.utilidades.controlesui

import android.content.Context
import android.support.v7.widget.AppCompatSpinner
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import co.smartobjects.silvernestandroid.utilidades.UtilidadesInterfaz

class SpinnerListenerSoloParaUsuario : AppCompatSpinner, AdapterView.OnItemSelectedListener
{
    private var spinnerTouched = false
    private var callbackClickEnItem: AdapterView.OnItemClickListener? = null
    private var callbackItemSeleccionado: AdapterView.OnItemSelectedListener? = null

    constructor(context: Context) : super(context)
    {
        super.setOnItemSelectedListener(this)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    {
        super.setOnItemSelectedListener(this)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    {
        super.setOnItemSelectedListener(this)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean
    {
        spinnerTouched = true
        return super.onTouchEvent(event)
    }

    override fun setOnItemClickListener(listener: AdapterView.OnItemClickListener?)
    {
        callbackClickEnItem = listener
    }

    override fun setOnItemSelectedListener(onItemSelectedListener: AdapterView.OnItemSelectedListener?)
    {
        this.callbackItemSeleccionado = onItemSelectedListener
        super.setOnItemSelectedListener(this)
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long)
    {
        if (spinnerTouched)
        {
            callbackItemSeleccionado?.onItemSelected(parent, view, position, id)
        }

        callbackClickEnItem?.onItemClick(parent, view, position, id)

        spinnerTouched = false
    }

    override fun onNothingSelected(parent: AdapterView<*>)
    {
        if (spinnerTouched)
        {
            callbackItemSeleccionado?.onNothingSelected(parent)
        }
        spinnerTouched = false
    }

    fun seleccionSimulandoTouch(posicion: Int)
    {
        spinnerTouched = true
        super.setSelection(posicion)
    }

    override fun setEnabled(enabled: Boolean)
    {
        super.setEnabled(enabled)
        alpha = if (enabled) 1f else UtilidadesInterfaz.VALOR_ALFA_DESHABILITADO
        selectedView?.isEnabled = enabled
    }
}