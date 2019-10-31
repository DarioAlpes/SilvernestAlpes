package co.smartobjects.silvernestandroid

import android.support.multidex.MultiDexApplication
import android.util.Log
import co.smartobjects.entidades.usuarios.Usuario
import co.smartobjects.silvernestandroid.inyecciondedependencias.dependenciasAplicacion
import co.smartobjects.silvernestandroid.inyecciondedependencias.dependenciasAplicacionDependientesDeContexto
import co.smartobjects.silvernestandroid.inyecciondedependencias.dependenciasDePantallas
import io.paperdb.Paper
import io.reactivex.plugins.RxJavaPlugins
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.koin.android.ext.android.startKoin
import kotlin.coroutines.CoroutineContext

open class AplicacionPrincipal : MultiDexApplication(), CoroutineScope
{
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    companion object
    {
        const val TAG = "Silvernest"
    }

    internal var usuarioActual: Usuario? = null

    override fun onCreate()
    {
        super.onCreate()

        Paper.init(this)

        startKoin(this, listOf(dependenciasAplicacionDependientesDeContexto, dependenciasAplicacion, dependenciasDePantallas))

        job = Job()

        instalarManejadoresDeExcepciones()

        /*val dependenciasBD = get<DependenciasBD>() as DependenciasBDAndroid

        with(dependenciasBD)
        {
            launch(Dispatchers.IO) {
                dependenciasBD
                    .readableDatabase
                    .rawQuery("SELECT name FROM sqlite_master WHERE type='table' LIMIT 1", null)
                    .position.also {
                    Log.d(TAG, "Inicialización de base de datos iniciando")
                }

                repositorioClientes.crearTablaSiNoExiste()

                repositorioClientes.inicializarConexionAEsquemaDeSerNecesario(BuildConfig.ID_CLIENTE)

                configuradoresRepositoriosHijo.forEach {
                    it.inicializarParaCliente(BuildConfig.ID_CLIENTE)
                }
            }
        }*/
    }

    private fun instalarManejadoresDeExcepciones()
    {
        RxJavaPlugins.setErrorHandler { Log.e(TAG, "Error RX", it) }

        val handlerComun = Thread.UncaughtExceptionHandler { _, e -> Log.e(TAG, "Error General", e) }
        Thread.setDefaultUncaughtExceptionHandler(handlerComun)
        Thread.currentThread().uncaughtExceptionHandler = handlerComun
    }
}