package co.smartobjects.silvernestandroid.utilidades

import android.content.Context
import android.net.ConnectivityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

object UtilidadesDispositivo
{
    suspend fun tieneInternetDisponible(ctx: Context): Boolean = withContext(Dispatchers.IO) {
        if (estaConectadoAUnaRed(ctx))
        {
            try
            {
                val ipAddr = InetAddress.getByName("google.com")
                @Suppress("ReplaceCallWithBinaryOperator")
                !ipAddr.equals("")
            }
            catch (e: Exception)
            {
                false
            }
        }
        else
        {
            false
        }
    }

    private fun estaConectadoAUnaRed(ctx: Context): Boolean
    {
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null
    }
}