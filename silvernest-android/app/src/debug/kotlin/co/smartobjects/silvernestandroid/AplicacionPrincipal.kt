package co.smartobjects.silvernestandroid

import com.facebook.stetho.Stetho

class DebugAplicacionPrincipal : AplicacionPrincipal()
{
    override fun onCreate()
    {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
    }
}