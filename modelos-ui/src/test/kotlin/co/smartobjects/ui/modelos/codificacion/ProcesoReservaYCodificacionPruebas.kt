package co.smartobjects.ui.modelos.codificacion

import co.smartobjects.entidades.operativas.compras.CreditoFondo
import co.smartobjects.entidades.operativas.compras.CreditoFondoConNombre
import co.smartobjects.entidades.operativas.compras.CreditoPaqueteConNombre
import co.smartobjects.entidades.operativas.reservas.Reserva
import co.smartobjects.entidades.operativas.reservas.SesionDeManilla
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.personas.PersonaConGrupoCliente
import co.smartobjects.nfc.ProveedorOperacionesNFC
import co.smartobjects.nfc.ResultadoNFC
import co.smartobjects.red.clientes.operativas.reservas.ReservasAPI
import co.smartobjects.red.clientes.operativas.reservas.sesionesdemanilla.SesionDeManillaAPI
import co.smartobjects.ui.modelos.ContextoDeSesion
import co.smartobjects.ui.modelos.PruebasModelosRxBase
import co.smartobjects.ui.modelos.mockConDefaultAnswer
import co.smartobjects.ui.modelos.pagos.ProcesoPagarUI
import co.smartobjects.ui.modelos.ultimoEmitido
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doReturn
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ProcesoReservaYCodificacionPruebas : PruebasModelosRxBase()
{
    private val mockContextoDeSesion = mockConDefaultAnswer(ContextoDeSesion::class.java)
    private val mockApiReservas = mockConDefaultAnswer(ReservasAPI::class.java)
    private val mockApiSesionesDeManilla = mockConDefaultAnswer(SesionDeManillaAPI::class.java)
    private val mockProveedorOperacionesNFC = mockConDefaultAnswer(ProveedorOperacionesNFC::class.java).also {
        doReturn(PublishProcessor.create<ResultadoNFC>()).`when`(it).resultadosNFCLeidos
        doReturn(BehaviorProcessor.createDefault(false)).`when`(it).listoParaLectura
        doNothing().`when`(it).permitirLecturaNFC = anyBoolean()
    }

    private val dependenciasModelo =
            ProcesoReservaYCodificacion.Dependencias(
                    mockContextoDeSesion,
                    mockApiReservas,
                    mockApiSesionesDeManilla,
                    mockProveedorOperacionesNFC
                                                    )
    private val mocksSesionesDeManilla = List(4) {
        val idPersona = it.toLong() + 1
        mockConDefaultAnswer(SesionDeManilla::class.java).also {
            doReturn(idPersona).`when`(it).idPersona
        }
    }

    private val creditosACodificar = mocksSesionesDeManilla.map {
        val idPersona = it.idPersona
        val mockPersona = mockConDefaultAnswer(Persona::class.java).also {
            doReturn(idPersona).`when`(it).id
        }
        mockConDefaultAnswer(ProcesoPagarUI.CreditosACodificarPorPersona::class.java).also {
            doReturn(PersonaConGrupoCliente(mockPersona, null)).`when`(it).personaConGrupoCliente
            doReturn(listOf<CreditoFondo>()).`when`(it).creditosFondoTotales
            doReturn(listOf<CreditoFondoConNombre>()).`when`(it).creditosFondoPagados
            doReturn(listOf<CreditoPaqueteConNombre>()).`when`(it).creditosPaquetePagados
        }
    }

    private val emisorReservaActivada = PublishSubject.create<Reserva>()
    private val procesoCreacionDeReserva = mockConDefaultAnswer(ProcesoCreacionReservaUI::class.java).also {
        doReturn(emisorReservaActivada.firstOrError()).`when`(it).reservaConNumeroAsignado
        doReturn("mock").`when`(it).toString()
    }


    @Nested
    inner class SinHaberEmitidoReservaActivada
    {
        @Test
        fun los_modelos_hijos_son_solo_el_proceso_de_creacion_de_reserva()
        {
            val modelo = ProcesoReservaYCodificacion(dependenciasModelo, creditosACodificar, procesoCreacionDeReserva)

            modelo.procesoCodificacionUI.subscribe { _ -> }

            assertEquals(listOf(procesoCreacionDeReserva), modelo.modelosHijos)
        }
    }

    @Nested
    inner class AlEmitirReservaActivada
    {
        private val mockReserva = mockConDefaultAnswer(Reserva::class.java).also {
            doReturn(mocksSesionesDeManilla).`when`(it).sesionesDeManilla
            doReturn("mock reserva").`when`(it).toString()
        }

        @Test
        fun los_modelos_hijos_son_solo_el_proceso_de_creacion_de_reserva_y_un_proceso_de_codificacion()
        {
            val modelo = ProcesoReservaYCodificacion(
                    dependenciasModelo,
                    creditosACodificar,
                    procesoCreacionDeReserva,
                    Schedulers.trampoline()
                                                    )

            modelo.procesoCodificacionUI.subscribe()

            emisorReservaActivada.onNext(mockReserva)

            assertEquals(2, modelo.modelosHijos.size)
            assertEquals(procesoCreacionDeReserva, modelo.modelosHijos.first())
            assertTrue(modelo.modelosHijos.last() is ProcesoCodificacionUI)
        }

        @Test
        fun los_items_a_codificar_del_proceso_de_codificacion_emitido_son_correctos()
        {
            val creditosEnCompra =
                    mocksSesionesDeManilla.map { sesionDeManilla ->
                        ProcesoPagarUI.CreditosACodificarPorPersona(
                                PersonaConGrupoCliente(
                                        mockConDefaultAnswer(Persona::class.java).also {
                                            doReturn(sesionDeManilla.idPersona).`when`(it).id
                                        },
                                        null
                                                      ),
                                listOf(),
                                listOf()
                                                                   )
                    }

            val itemsACodificarEsperados =
                    mocksSesionesDeManilla.mapIndexed { index, sesionDeManilla ->
                        ItemACodificar(
                                sesionDeManilla,
                                creditosEnCompra[index],
                                PublishProcessor.create(),
                                mockApiSesionesDeManilla,
                                Schedulers.trampoline()
                                      )
                    }

            val modelo = ProcesoReservaYCodificacion(
                    dependenciasModelo,
                    creditosEnCompra,
                    procesoCreacionDeReserva,
                    Schedulers.trampoline()
                                                    )

            val observadorDePrueba = modelo.procesoCodificacionUI.test()

            emisorReservaActivada.onNext(mockReserva)

            observadorDePrueba.ultimoEmitido().itemsACodificar.forEachIndexed { index, itemACodificarUI ->
                assertEquals(itemACodificarUI, itemsACodificarEsperados[index])
            }
        }

        @Test
        fun el_resultado_emitido_se_encuentra_siempre_disponible()
        {
            val modelo = ProcesoReservaYCodificacion(
                    dependenciasModelo,
                    creditosACodificar,
                    procesoCreacionDeReserva,
                    Schedulers.trampoline()
                                                    )

            modelo.procesoCodificacionUI.subscribe()

            emisorReservaActivada.onNext(mockReserva)

            val observadorDePrueba = modelo.procesoCodificacionUI.test()

            observadorDePrueba.assertValueCount(1)
        }
    }
}