package co.smartobjects.silvernestandroid.inyecciondedependencias

import co.smartobjects.nfc.operacionessobretags.ProveedorLlaves
import co.smartobjects.red.clientes.retrofit.ManejadorDePeticiones
import co.smartobjects.red.clientes.retrofit.ManejadorDePeticionesRetrofit
import co.smartobjects.silvernestandroid.BuildConfig
import co.smartobjects.silvernestandroid.utilidades.persistencia.DependenciasBD
import co.smartobjects.silvernestandroid.utilidades.persistencia.DependenciasBDPaper
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.asCoroutineDispatcher
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module.module
import java.util.concurrent.Executors


private val ejecutor = Executors.newSingleThreadExecutor()
internal val schedulerBD = Schedulers.from(ejecutor)
internal val dispatcherBD = ejecutor.asCoroutineDispatcher()

internal val dependenciasAplicacionDependientesDeContexto = module(createOnStart = true) {
    single<ManejadorDePeticiones>(createOnStart = true) {
        ManejadorDePeticionesRetrofit(
                BuildConfig.ID_CLIENTE,
                BuildConfig.URL_BASE,
                PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(androidApplication()))
                                     )
    }

    single<DependenciasBD>(createOnStart = true) {
        //        DependenciasBDAndroid(androidApplication())
        DependenciasBDPaper()
    }
}

internal val dependenciasAplicacion = module {

    /*single<SincronizadorDeDatos> {

        val manejadorPeticiones = get<ManejadorDePeticiones>()
        val dependenciasBD = get<DependenciasBD>()

        val gestorDeDescargas =
                GestorDescargaDeDatosImpl(
                        BuildConfig.ID_CLIENTE,
                        GestorDescargaDeDatosImpl.APIs(
                                manejadorPeticiones.apiDeUbicaciones,
                                manejadorPeticiones.apiDeUbicacionesContabilizables,
                                manejadorPeticiones.apiDeImpuestos,
                                manejadorPeticiones.apiDeAccesos,
                                manejadorPeticiones.apiDeEntradas,
                                manejadorPeticiones.apiDeCategoriasSku,
                                manejadorPeticiones.apiDeSkus,
                                manejadorPeticiones.apiDeMonedas,
                                manejadorPeticiones.apiDePaquetes,
                                manejadorPeticiones.apiDeLibrosSegunReglasCompleto,
                                manejadorPeticiones.apiDeGruposClientes,
                                manejadorPeticiones.apiDeValoresGruposEdad,
                                manejadorPeticiones.apiDeLlavesNFC
                                                      ),
                        GestorDescargaDeDatosImpl.Repositorios(
                                dependenciasBD.repositorioUbicaciones,
                                dependenciasBD.repositorioUbicacionesContabilizables,
                                dependenciasBD.repositorioImpuestos,
                                dependenciasBD.repositorioAccesos,
                                dependenciasBD.repositorioEntradas,
                                dependenciasBD.repositorioCategoriasSkus,
                                dependenciasBD.repositorioSkus,
                                dependenciasBD.repositorioMonedas,
                                dependenciasBD.repositorioPaquetes,
                                dependenciasBD.repositorioLibrosSegunReglas,
                                dependenciasBD.repositorioLibroDePrecios,
                                dependenciasBD.repositorioLibroDeProhibiciones,
                                dependenciasBD.repositorioGrupoClientes,
                                dependenciasBD.repositorioValoresGruposEdad,
                                dependenciasBD.repositorioLlavesNFC
                                                              )
                                         )

        SincronizadorDeDatosImpl(gestorDeDescargas)
    }*/

    single<ProveedorLlaves> {
        ProveedorLlaves.crearProveedorDeLlaves(BuildConfig.ID_CLIENTE, get<DependenciasBD>().repositorioLlavesNFC)
    }
}