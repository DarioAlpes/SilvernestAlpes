package co.smartobjects.silvernestandroid.inyecciondedependencias

import co.smartobjects.red.clientes.retrofit.ManejadorDePeticiones
import co.smartobjects.silvernestandroid.BuildConfig
import co.smartobjects.silvernestandroid.utilidades.persistencia.DependenciasBD
import co.smartobjects.ui.modelos.contabilizacionubicaciones.ProcesoContabilizacionUbicaciones
import co.smartobjects.ui.modelos.contabilizacionubicaciones.ProcesoContabilizacionUbicacionesUI
import co.smartobjects.ui.modelos.login.ProcesoLogin
import co.smartobjects.ui.modelos.login.ProcesoLoginConSujetos
import org.koin.dsl.module.module

internal val dependenciasDePantallas = module {

    scope<ProcesoLogin>(ProcesoLogin::class.simpleName!!) {
        ProcesoLoginConSujetos(get<ManejadorDePeticiones>().apiDeUsuarios)
    }

    scope<ProcesoContabilizacionUbicacionesUI>(ProcesoContabilizacionUbicacionesUI::class.simpleName!!) {
        ProcesoContabilizacionUbicaciones(
                BuildConfig.ID_CLIENTE,
                get<ManejadorDePeticiones>().apiDeConteosEnUbicacion,
                get<DependenciasBD>().repositorioUbicaciones,
                get<DependenciasBD>().repositorioUbicacionesContabilizables
                                         )
    }
}