package co.smartobjects.sincronizadordecontenido

import co.smartobjects.entidades.clientes.Cliente
import co.smartobjects.entidades.fondos.libros.LibroDePrecios
import co.smartobjects.entidades.fondos.libros.LibroDeProhibiciones
import co.smartobjects.entidades.fondos.libros.LibroSegunReglasCompleto
import co.smartobjects.entidades.ubicaciones.contabilizables.UbicacionesContabilizables
import co.smartobjects.persistencia.basederepositorios.Creable
import co.smartobjects.persistencia.basederepositorios.CreableConDiferenteSalida
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorio
import co.smartobjects.persistencia.clientes.llavesnfc.RepositorioLlavesNFC
import co.smartobjects.persistencia.fondos.acceso.RepositorioAccesos
import co.smartobjects.persistencia.fondos.acceso.RepositorioEntradas
import co.smartobjects.persistencia.fondos.categoriaskus.RepositorioCategoriasSkus
import co.smartobjects.persistencia.fondos.libros.RepositorioLibrosDePrecios
import co.smartobjects.persistencia.fondos.libros.RepositorioLibrosDeProhibiciones
import co.smartobjects.persistencia.fondos.libros.RepositorioLibrosSegunReglas
import co.smartobjects.persistencia.fondos.monedas.RepositorioMonedas
import co.smartobjects.persistencia.fondos.paquete.RepositorioPaquetes
import co.smartobjects.persistencia.fondos.precios.gruposclientes.RepositorioGrupoClientes
import co.smartobjects.persistencia.fondos.precios.impuestos.RepositorioImpuestos
import co.smartobjects.persistencia.fondos.skus.RepositorioSkus
import co.smartobjects.persistencia.personas.valorgrupoedad.RepositorioValoresGruposEdad
import co.smartobjects.persistencia.ubicaciones.RepositorioUbicaciones
import co.smartobjects.persistencia.ubicaciones.contabilizables.RepositorioUbicacionesContabilizables
import co.smartobjects.red.clientes.base.ConsultarAPI
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.base.RespuestaVacia
import co.smartobjects.red.clientes.clientes.LlavesNFCAPI
import co.smartobjects.red.clientes.fondos.*
import co.smartobjects.red.clientes.fondos.libros.LibrosSegunReglasCompletoAPI
import co.smartobjects.red.clientes.fondos.precios.GruposClientesAPI
import co.smartobjects.red.clientes.fondos.precios.ImpuestosAPI
import co.smartobjects.red.clientes.personas.ValoresGrupoEdadAPI
import co.smartobjects.red.clientes.ubicaciones.UbicacionesAPI
import co.smartobjects.red.clientes.ubicaciones.contabilizables.UbicacionesContabilizablesAPI
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import org.threeten.bp.ZonedDateTime
import sun.reflect.generics.reflectiveObjects.NotImplementedException
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore


interface GestorDescargaDeDatos
{
    val estaDescargando: Observable<Boolean>

    fun descargarYAlmacenarDatosIndependientesDeLaUbicacion(): Maybe<List<RespuestaVacia>>
}


class GestorDescargaDeDatosImpl
(
        private val idCliente: Long,
        private val apisRest: APIs,
        private val repositorios: Repositorios,
        private val schedulerBackground: Scheduler = Schedulers.io(),
        private val schedulerUnicoThreadEscrituraPersistencia: Scheduler = Schedulers.from(Executors.newSingleThreadExecutor())
) : GestorDescargaDeDatos
{
    private val repositorioCreacionLibrosSegunReglasCompletos =
            RepositorioCreacionLibrosSegunReglasCompletos(
                    repositorios.repositorioLibrosSegunReglas,
                    repositorios.repositorioLibrosDePrecios,
                    repositorios.repositorioLibrosDeProhibiciones
                                                         )

    private val enPeticion = Semaphore(1, true)
    private val eventosEstaDescargando = BehaviorSubject.createDefault(false)
    override val estaDescargando: Observable<Boolean> = eventosEstaDescargando.distinctUntilChanged()

    override fun descargarYAlmacenarDatosIndependientesDeLaUbicacion(): Maybe<List<RespuestaVacia>>
    {   print("Sincronizar Datos");
        return if (enPeticion.tryAcquire())
        {
            eventosEstaDescargando.onNext(true)

            repositorios.repositorioCategoriasSkus.limpiarParaCliente(idCliente)
            repositorios.repositorioMonedas.limpiarParaCliente(idCliente)
            repositorios.repositorioSkus.limpiarParaCliente(idCliente)
            repositorios.repositorioEntradas.limpiarParaCliente(idCliente)
            repositorios.repositorioAccesos.limpiarParaCliente(idCliente)
            repositorios.repositorioUbicaciones.limpiarParaCliente(idCliente)
            repositorios.repositorioUbicacionesContabilizables.limpiarParaCliente(idCliente)
            repositorios.repositorioImpuestos.limpiarParaCliente(idCliente)
            repositorios.repositorioPaquetes.limpiarParaCliente(idCliente)
            repositorioCreacionLibrosSegunReglasCompletos.limpiarParaCliente(idCliente)
            repositorios.repositorioGrupoClientes.limpiarParaCliente(idCliente)
            repositorios.repositorioValoresGruposEdad.limpiarParaCliente(idCliente)
            repositorios.repositorioLlavesNFC.limpiarParaCliente(idCliente)

            val descargaCategoriasSku = crearDescarga(apisRest.apiCategoriasSku, repositorios.repositorioCategoriasSkus)
            val descargaMonedas = crearDescarga(apisRest.apiMonedas, repositorios.repositorioMonedas)
            val descargaSkus = crearDescarga(apisRest.apiSkus, repositorios.repositorioSkus)
            val descargaEntradas = crearDescarga(apisRest.apiEntradas, repositorios.repositorioEntradas)
            val descargaAccesos = crearDescarga(apisRest.apiAccesos, repositorios.repositorioAccesos)

            val descargaDeFondos =
                    Singles
                        .zip(
                                descargaCategoriasSku,
                                descargaMonedas,
                                descargaSkus,
                                descargaEntradas,
                                descargaAccesos
                            )
                        { respuestaCategorias, respuestasMonedas, respuestasSkus, respuestaEntradas, respuestaAccesos ->

                            listOf(respuestaCategorias,
                                   respuestasMonedas,
                                   respuestasSkus,
                                   respuestaEntradas,
                                   respuestaAccesos
                                  )
                        }
                        .subscribeOn(schedulerBackground)

            val descargaUbicaciones = crearDescarga(apisRest.apiUbicacionesAPI, repositorios.repositorioUbicaciones)
            val descargaUbicacionesContabilizables = crearDescargaUbicacionesContabilizables(apisRest.apiUbicacionesContabilizables, repositorios.repositorioUbicacionesContabilizables)
            val descargaUbicacionesTotales =
                    Singles
                        .zip(
                                descargaUbicaciones,
                                descargaUbicacionesContabilizables
                            )
                        { resUbicaciones, resUbicacionesContabilizables ->

                            listOf(resUbicaciones, resUbicacionesContabilizables)
                        }
                        .subscribeOn(schedulerBackground)
            val descargaImpuestos = crearDescarga(apisRest.apiDeImpuestosAPI, repositorios.repositorioImpuestos)
            val descargaPaquetes = crearDescarga(apisRest.apiDePaquetes, repositorios.repositorioPaquetes)
            val descargaLibroSegunReglasCompletos = crearDescarga(apisRest.apiDeLibrosSegunReglasCompletoAPI, repositorioCreacionLibrosSegunReglasCompletos)
            val descargaGruposClientes = crearDescarga(apisRest.apiDeGrupoClientes, repositorios.repositorioGrupoClientes)
            val descargaValoresGruposEdad = crearDescarga(apisRest.apiDeValoresGruposEdad, repositorios.repositorioValoresGruposEdad)
            val descargarLlaveNFC = descargarLlaveNFC(apisRest.apiLlavesNFC, repositorios.repositorioLlavesNFC)

            Singles
                .zip(
                        descargaUbicacionesTotales,
                        descargaImpuestos,
                        descargaDeFondos,
                        descargaPaquetes,
                        descargaLibroSegunReglasCompletos,
                        descargaGruposClientes,
                        descargaValoresGruposEdad,
                        descargarLlaveNFC
                    )
                { resUbicaciones, resImpuestos, listaResFondos, resPaquetes, resLibros, resGrupos, resValoresGruposEdad, resLlaveNFC ->
                    (
                            resUbicaciones.asSequence() +
                            resImpuestos +
                            listaResFondos.asSequence() +
                            resPaquetes +
                            resLibros +
                            resGrupos +
                            resValoresGruposEdad +
                            resLlaveNFC
                    ).toList()
                }
                .doFinally {
                    enPeticion.release()
                    eventosEstaDescargando.onNext(false)
                }
                .toMaybe()
                .subscribeOn(schedulerBackground)
        }
        else
        {
            Maybe.empty<List<RespuestaVacia>>()
        }.subscribeOn(schedulerBackground).observeOn(Schedulers.trampoline())
    }

    private fun <T, R> crearDescarga(api: ConsultarAPI<List<T>>, repositorio: R): Single<RespuestaVacia>
            where R : CreableConDiferenteSalida<T, *>
    {
        return Single
            .fromCallable {
                api.consultar()
            }
            .observeOn(schedulerUnicoThreadEscrituraPersistencia)
            .map {
                when (it)
                {
                    is RespuestaIndividual.Exitosa ->
                    {
                        for (entidad in it.respuesta)
                        {
                            repositorio.crear(idCliente, entidad)
                        }
                        RespuestaVacia.Exitosa
                    }
                    else                           ->
                    {
                        RespuestaVacia.desdeRespuestaInvidual(it)
                    }
                }
            }
            .observeOn(schedulerBackground)
            .subscribeOn(schedulerBackground)
    }

    private fun descargarLlaveNFC(apiLlavesNFC: LlavesNFCAPI, repositorioLlavesNFC: RepositorioLlavesNFC): Single<RespuestaVacia>
    {
        return Single
            .fromCallable {
                apiLlavesNFC.consultar(ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO))
            }
            .observeOn(schedulerUnicoThreadEscrituraPersistencia)
            .map {
                when (it)
                {
                    is RespuestaIndividual.Exitosa ->
                    {
                        repositorioLlavesNFC.crear(idCliente, Cliente.LlaveNFC(idCliente, "0123456789"))
                        //repositorioLlavesNFC.crear(idCliente, it.respuesta)
                        RespuestaVacia.Exitosa
                    }
                    else                           ->
                    {
                        RespuestaVacia.desdeRespuestaInvidual(it)
                    }
                }
            }
            .observeOn(schedulerBackground)
            .subscribeOn(schedulerBackground)
    }

    private fun crearDescargaUbicacionesContabilizables(
            apiUbicacionesContabilizables: UbicacionesContabilizablesAPI,
            repositorioUbicacionesContabilizables: RepositorioUbicacionesContabilizables
                                                       )
            : Single<RespuestaVacia>
    {
        return Single
            .fromCallable {
                apiUbicacionesContabilizables.consultar()
            }
            .observeOn(schedulerUnicoThreadEscrituraPersistencia)
            .map {
                when (it)
                {
                    is RespuestaIndividual.Exitosa ->
                    {
                        val ubicacionesConsultadas = UbicacionesContabilizables(idCliente, it.respuesta.map { it }.toSet())

                        repositorioUbicacionesContabilizables.crear(idCliente, ubicacionesConsultadas)

                        RespuestaVacia.Exitosa
                    }
                    else                           ->
                    {
                        RespuestaVacia.desdeRespuestaInvidual(it)
                    }
                }
            }
            .observeOn(schedulerBackground)
            .subscribeOn(schedulerBackground)
    }

    class APIs(
            val apiUbicacionesAPI: UbicacionesAPI,
            val apiUbicacionesContabilizables: UbicacionesContabilizablesAPI,
            val apiDeImpuestosAPI: ImpuestosAPI,
            val apiAccesos: AccesosAPI,
            val apiEntradas: EntradasAPI,
            val apiCategoriasSku: CategoriasSkuAPI,
            val apiSkus: SkusAPI,
            val apiMonedas: MonedasAPI,
            val apiDePaquetes: PaquetesAPI,
            val apiDeLibrosSegunReglasCompletoAPI: LibrosSegunReglasCompletoAPI,
            val apiDeGrupoClientes: GruposClientesAPI,
            val apiDeValoresGruposEdad: ValoresGrupoEdadAPI,
            val apiLlavesNFC: LlavesNFCAPI
              )

    class Repositorios(
            val repositorioUbicaciones: RepositorioUbicaciones,
            val repositorioUbicacionesContabilizables: RepositorioUbicacionesContabilizables,
            val repositorioImpuestos: RepositorioImpuestos,
            val repositorioAccesos: RepositorioAccesos,
            val repositorioEntradas: RepositorioEntradas,
            val repositorioCategoriasSkus: RepositorioCategoriasSkus,
            val repositorioSkus: RepositorioSkus,
            val repositorioMonedas: RepositorioMonedas,
            val repositorioPaquetes: RepositorioPaquetes,
            val repositorioLibrosSegunReglas: RepositorioLibrosSegunReglas,
            val repositorioLibrosDePrecios: RepositorioLibrosDePrecios,
            val repositorioLibrosDeProhibiciones: RepositorioLibrosDeProhibiciones,
            val repositorioGrupoClientes: RepositorioGrupoClientes,
            val repositorioValoresGruposEdad: RepositorioValoresGruposEdad,
            val repositorioLlavesNFC: RepositorioLlavesNFC
                      )

    private class RepositorioCreacionLibrosSegunReglasCompletos
    (
            private val repositorioLibrosSegunReglas: RepositorioLibrosSegunReglas,
            private val repositorioLibrosDePrecios: RepositorioLibrosDePrecios,
            private val repositorioLibrosDeProhibiciones: RepositorioLibrosDeProhibiciones
    ) : CreadorRepositorio<LibroSegunReglasCompleto<*>>,
        Creable<LibroSegunReglasCompleto<*>>
    {
        override val nombreEntidad: String = LibroSegunReglasCompleto.NOMBRE_ENTIDAD

        override fun inicializarParaCliente(idCliente: Long)
        {
            throw NotImplementedException()
        }

        override fun limpiarParaCliente(idCliente: Long)
        {
            repositorioLibrosSegunReglas.limpiarParaCliente(idCliente)
            repositorioLibrosDePrecios.limpiarParaCliente(idCliente)
            repositorioLibrosDeProhibiciones.limpiarParaCliente(idCliente)
        }

        override fun crear(idCliente: Long, entidadACrear: LibroSegunReglasCompleto<*>): LibroSegunReglasCompleto<*>
        {
            if (entidadACrear.libro is LibroDePrecios)
            {
                repositorioLibrosDePrecios.crear(idCliente, entidadACrear.libro as LibroDePrecios)
            }
            else
            {
                repositorioLibrosDeProhibiciones.crear(idCliente, entidadACrear.libro as LibroDeProhibiciones)
            }

            repositorioLibrosSegunReglas.crear(idCliente, entidadACrear.aLibroSegunReglas())

            return entidadACrear
        }
    }
}