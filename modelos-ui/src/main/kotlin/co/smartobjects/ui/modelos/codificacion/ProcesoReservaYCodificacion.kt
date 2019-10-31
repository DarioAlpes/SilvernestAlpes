package co.smartobjects.ui.modelos.codificacion

import co.smartobjects.nfc.ProveedorOperacionesNFC
import co.smartobjects.red.clientes.operativas.reservas.ReservasAPI
import co.smartobjects.red.clientes.operativas.reservas.sesionesdemanilla.SesionDeManillaAPI
import co.smartobjects.ui.modelos.ContextoDeSesion
import co.smartobjects.ui.modelos.ModeloUI
import co.smartobjects.ui.modelos.pagos.ProcesoPagarUI
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

interface ProcesoReservaYCodificacionUI : ModeloUI
{
    val procesoCreacionReservaUI: ProcesoCreacionReservaUI
    val procesoCodificacionUI: Single<ProcesoCodificacionUI>
}

class ProcesoReservaYCodificacion internal constructor
(
        private val dependencias: Dependencias,
        creditosDeUnaCompra: List<ProcesoPagarUI.CreditosACodificarPorPersona>,
        _procesoCreacionReservaUI: ProcesoCreacionReservaUI? = null,
        schedulerBackground: Scheduler = Schedulers.io()
) : ProcesoReservaYCodificacionUI
{
    constructor(
            dependencias: Dependencias,
            creditosDeUnaCompra: List<ProcesoPagarUI.CreditosACodificarPorPersona>,
            schedulerBackground: Scheduler = Schedulers.io()
               )
            : this(dependencias, creditosDeUnaCompra, null, schedulerBackground)

    override val procesoCreacionReservaUI: ProcesoCreacionReservaUI =
            _procesoCreacionReservaUI
            ?: ProcesoCreacionReserva(dependencias.contextoDeSesion, creditosDeUnaCompra, dependencias.apiDeReservas, schedulerBackground)

    override val procesoCodificacionUI: Single<ProcesoCodificacionUI> =
            procesoCreacionReservaUI
                .reservaConNumeroAsignado
                .map { it ->
                    val creditosAProcesar = creditosDeUnaCompra.toMutableList()
                    val sesionesDeManillaYCreditosACodificar = mutableListOf<ProcesoCodificacionUI.SesionDeManillaYCreditosACodificar>()

                    for (sesionDeManilla in it.sesionesDeManilla)
                    {
                        // Asume que los creditos de la compra tienen las mismas personas que las sesiones de manilla de la reserva
                        val posicionPersonaEncontrada = creditosAProcesar.indexOfFirst {
                            it.personaConGrupoCliente.persona.id!! == sesionDeManilla.idPersona
                        }

                        sesionesDeManillaYCreditosACodificar.add(
                                ProcesoCodificacionUI.SesionDeManillaYCreditosACodificar(
                                        sesionDeManilla,
                                        creditosAProcesar[posicionPersonaEncontrada]
                                                                                        )
                                                                )

                        creditosAProcesar.removeAt(posicionPersonaEncontrada)
                    }

                    val procesDeCodificacion =
                            ProcesoCodificacion(
                                    sesionesDeManillaYCreditosACodificar,
                                    dependencias.proveedorOperacionesNFC,
                                    dependencias.apiSesionDeManilla,
                                    schedulerBackground
                                               ) as ProcesoCodificacionUI

                    modelosHijosAgregados.add(procesDeCodificacion)

                    procesDeCodificacion
                }
                .doOnSuccess { it.iniciarCodificacionDesdePrimeroSinCodificar() }
                .cache()
                .subscribeOn(schedulerBackground)

    override val observadoresInternos: List<Observer<*>> = emptyList()
    private val modelosHijosAgregados = mutableListOf<ModeloUI>(procesoCreacionReservaUI)
    override val modelosHijos: List<ModeloUI>
        get() = modelosHijosAgregados

    private val disposables = CompositeDisposable()


    override fun finalizarProceso()
    {
        disposables.dispose()
        super.finalizarProceso()
    }

    class Dependencias(
            val contextoDeSesion: ContextoDeSesion,
            val apiDeReservas: ReservasAPI,
            val apiSesionDeManilla: SesionDeManillaAPI,
            val proveedorOperacionesNFC: ProveedorOperacionesNFC
                      )
}