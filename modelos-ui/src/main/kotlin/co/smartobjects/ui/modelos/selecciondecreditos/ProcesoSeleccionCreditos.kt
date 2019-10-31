package co.smartobjects.ui.modelos.selecciondecreditos

import co.smartobjects.entidades.operativas.compras.CreditoFondo
import co.smartobjects.entidades.operativas.compras.CreditoFondoConNombre
import co.smartobjects.entidades.operativas.compras.CreditoPaqueteConNombre
import co.smartobjects.entidades.personas.PersonaConGrupoCliente
import co.smartobjects.ui.modelos.ContextoDeSesion
import co.smartobjects.ui.modelos.ModeloUI
import co.smartobjects.ui.modelos.catalogo.CatalogoUI
import co.smartobjects.ui.modelos.menufiltrado.MenuFiltradoFondosUI
import co.smartobjects.ui.modelos.menufiltrado.ProveedorIconosCategoriasFiltrado
import co.smartobjects.ui.modelos.selecciondecreditos.agrupacioncarritosdecreditos.AgrupacionPersonasCarritosDeCreditosUI
import io.reactivex.Observer
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo

interface ProcesoSeleccionCreditosUI<TipoIconoFiltrado : ProveedorIconosCategoriasFiltrado.Icono> : ModeloUI
{
    val menuFiltradoFondos: MenuFiltradoFondosUI<TipoIconoFiltrado>
    val catalogo: CatalogoUI
    val agrupacionCarritoDeCreditos: AgrupacionPersonasCarritosDeCreditosUI

    val creditosPorPersonaAProcesar: Single<List<CreditosPorPersonaAProcesar>>

    data class CreditosPorPersonaAProcesar(
            val personaConGrupoCliente: PersonaConGrupoCliente,
            val creditosFondoAPagar: List<CreditoFondoConNombre>,
            val creditosPaqueteAPagar: List<CreditoPaqueteConNombre>,
            val creditosFondoPagados: List<CreditoFondoConNombre>,
            val creditosPaquetePagados: List<CreditoPaqueteConNombre>
                                          )
    {
        val creditosFondoTotales: List<CreditoFondo> =
                (
                        creditosFondoAPagar.asSequence().map { it.creditoAsociado } +
                        creditosFondoPagados.asSequence().map { it.creditoAsociado } +
                        creditosPaqueteAPagar.asSequence().flatMap { it.creditoAsociado.creditosFondos.asSequence() } +
                        creditosPaquetePagados.asSequence().flatMap { it.creditoAsociado.creditosFondos.asSequence() }
                ).toList()
    }
}

class ProcesoSeleccionCreditos<TipoIconoFiltrado : ProveedorIconosCategoriasFiltrado.Icono>
(
        private val contextoDeSesion: ContextoDeSesion,
        override val menuFiltradoFondos: MenuFiltradoFondosUI<TipoIconoFiltrado>,
        override val catalogo: CatalogoUI,
        override val agrupacionCarritoDeCreditos: AgrupacionPersonasCarritosDeCreditosUI
) : ProcesoSeleccionCreditosUI<TipoIconoFiltrado>
{
    override val observadoresInternos: List<Observer<*>> = listOf()
    override val modelosHijos: List<ModeloUI> = listOf(menuFiltradoFondos, catalogo, agrupacionCarritoDeCreditos)

    private val disposables = CompositeDisposable()

    init
    {
        catalogo
            .catalogoDeProductos
            .firstElement()
            .subscribe {
                for (productoUI in it.catalogo)
                {
                    productoUI.productoAAgregar.subscribe { producto ->
                        agrupacionCarritoDeCreditos.agregarProducto(producto)
                    }.addTo(disposables)

                    agrupacionCarritoDeCreditos.estaAgregandoProducto.filter { !it }.subscribe {
                        productoUI.terminarAgregar()
                    }.addTo(disposables)
                }
            }.addTo(disposables)

        menuFiltradoFondos.filtrosDisponibles.subscribe { filtros ->
            filtros.forEach {
                it.filtroActivado.subscribe(catalogo::filtrarPorCriterio).addTo(disposables)
            }
        }.addTo(disposables)
    }

    override val creditosPorPersonaAProcesar =
            agrupacionCarritoDeCreditos
                .creditosAProcesar
                .map {
                    it.map {
                        val creditosFondoAPagar = mutableListOf<CreditoFondoConNombre>()
                        val creditosPaqueteAPagar = mutableListOf<CreditoPaqueteConNombre>()
                        for (creditoUI in it.creditosAPagar)
                        {
                            if (creditoUI.esPaquete)
                            {
                                val creditoPaqueteConNombre =
                                        creditoUI.aCreditoPaquete(
                                                contextoDeSesion.idCliente,
                                                contextoDeSesion.origenDeProcesamiento,
                                                contextoDeSesion.nombreDeUsuario,
                                                it.persona.id!!,
                                                contextoDeSesion.idDispositivoDeProcesamiento,
                                                contextoDeSesion.idUbicacion,
                                                it.grupoDeClientes?.id
                                                                 )!!

                                creditosPaqueteAPagar.add(creditoPaqueteConNombre)
                            }
                            else
                            {
                                val creditoFondo =
                                        creditoUI.aCreditoFondo(
                                                contextoDeSesion.idCliente,
                                                contextoDeSesion.origenDeProcesamiento,
                                                contextoDeSesion.nombreDeUsuario,
                                                it.persona.id!!,
                                                contextoDeSesion.idDispositivoDeProcesamiento,
                                                contextoDeSesion.idUbicacion,
                                                it.grupoDeClientes?.id
                                                               )!!

                                creditosFondoAPagar.add(CreditoFondoConNombre(creditoUI.nombreProducto, creditoFondo))
                            }
                        }

                        val creditosFondoPagados = mutableListOf<CreditoFondoConNombre>()
                        val creditosPaquetePagados = mutableListOf<CreditoPaqueteConNombre>()
                        for (creditoUI in it.creditosPagados)
                        {
                            if (creditoUI.esPaquete)
                            {
                                val creditoPaqueteConNombre =
                                        creditoUI.aCreditoPaquete(
                                                contextoDeSesion.idCliente,
                                                contextoDeSesion.origenDeProcesamiento,
                                                contextoDeSesion.nombreDeUsuario,
                                                it.persona.id!!,
                                                contextoDeSesion.idDispositivoDeProcesamiento,
                                                contextoDeSesion.idUbicacion,
                                                it.grupoDeClientes?.id
                                                                 )!!

                                creditosPaquetePagados.add(creditoPaqueteConNombre)
                            }
                            else
                            {
                                val creditoFondo =
                                        creditoUI.aCreditoFondo(
                                                contextoDeSesion.idCliente,
                                                contextoDeSesion.origenDeProcesamiento,
                                                contextoDeSesion.nombreDeUsuario,
                                                it.persona.id!!,
                                                contextoDeSesion.idDispositivoDeProcesamiento,
                                                contextoDeSesion.idUbicacion,
                                                it.grupoDeClientes?.id
                                                               )!!

                                creditosFondoPagados.add(CreditoFondoConNombre(creditoUI.nombreProducto, creditoFondo))
                            }
                        }

                        ProcesoSeleccionCreditosUI
                            .CreditosPorPersonaAProcesar(
                                    PersonaConGrupoCliente(it.persona, it.grupoDeClientes),
                                    creditosFondoAPagar,
                                    creditosPaqueteAPagar,
                                    creditosFondoPagados,
                                    creditosPaquetePagados
                                                        )
                    }
                }
                .doOnSuccess {
                    finalizarProceso()
                }!!

    override fun finalizarProceso()
    {
        disposables.dispose()
        super.finalizarProceso()
    }
}