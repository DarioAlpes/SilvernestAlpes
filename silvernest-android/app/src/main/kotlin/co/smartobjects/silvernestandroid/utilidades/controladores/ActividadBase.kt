package co.smartobjects.silvernestandroid.utilidades.controladores

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.LayoutRes
import co.smartobjects.silvernestandroid.AplicacionPrincipal
import co.smartobjects.silvernestandroid.R
import co.smartobjects.silvernestandroid.utilidades.children
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

abstract class ActividadBase : AppCompatActivity(), CoroutineScope
{
    protected val aplicacionPrincipal by lazy { application as AplicacionPrincipal }

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    @get:LayoutRes
    protected abstract val idLayout: Int

    private val toolbar by lazy(LazyThreadSafetyMode.NONE) { findViewById<Toolbar?>(R.id.toolbar_base) }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        preSetContentView()
        setContentView(idLayout)

        job = Job()

        configurarToolbar(toolbar)
    }

    override fun onDestroy()
    {
        super.onDestroy()
        job.cancel()
    }

    protected open fun preSetContentView()
    {
    }

    private fun esconderTecladoAlHacerClickAfuera(viewGroup: ViewGroup)
    {
        for (vistaHija in viewGroup.children)
        {
            if (vistaHija !is EditText)
            {
                vistaHija.setOnTouchListener { _, _ ->
                    if (currentFocus != null)
                    {
                        esconderTeclado(this@ActividadBase, currentFocus!!.windowToken)
                    }
                    false
                }
            }

            if (vistaHija is ViewGroup)
            {
                esconderTecladoAlHacerClickAfuera(vistaHija)
            }
        }
    }

    protected fun esconderTeclado(ctx: Context, tokenDeWindow: IBinder)
    {
        val inputManager = ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(tokenDeWindow, 0)
    }

    protected fun esconderTeclado(ctx: Context)
    {
        val inputManager = ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        val v = (ctx as Activity).currentFocus ?: return

        inputManager.hideSoftInputFromWindow(v.windowToken, 0)
    }

    protected open fun configurarToolbar(instanciaToolbar: Toolbar?)
    {
        if (instanciaToolbar != null)
        {
            setSupportActionBar(instanciaToolbar)

            with(supportActionBar!!)
            {
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(true)
            }

            instanciaToolbar.setNavigationOnClickListener { onBackPressed() }
        }
    }
}