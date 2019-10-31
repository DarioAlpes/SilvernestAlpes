package co.smartobjects.ui.javafx.controladores.selecciondecreditos

import co.smartobjects.ui.javafx.PruebaJavaFXBase
import co.smartobjects.ui.javafx.controladores.catalogo.ControladorCatalogoProductos
import co.smartobjects.ui.javafx.controladores.genericos.DialogoDeEspera
import co.smartobjects.ui.javafx.controladores.menufiltradofondos.ControladorMenuFiltrado
import co.smartobjects.ui.javafx.controladores.selecciondecreditos.agrupacioncarritosdecreditos.ControladorAgrupacionPersonasCarritosDeCreditos
import co.smartobjects.ui.javafx.dependencias.agregarDependenciaDePantallaUnSoloUso
import co.smartobjects.ui.javafx.iconosyfonts.PrompterIconosCategorias
import co.smartobjects.ui.javafx.mockConDefaultAnswer
import co.smartobjects.ui.modelos.catalogo.CatalogoUI
import co.smartobjects.ui.modelos.menufiltrado.MenuFiltradoFondosUI
import co.smartobjects.ui.modelos.selecciondecreditos.ProcesoCompraYSeleccionCreditosUI
import co.smartobjects.ui.modelos.selecciondecreditos.ProcesoSeleccionCreditosUI
import co.smartobjects.ui.modelos.selecciondecreditos.agrupacioncarritosdecreditos.AgrupacionPersonasCarritosDeCreditosUI
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.verify
import kotlin.test.assertEquals

internal class ControladorSeleccionCreditosPruebas : PruebaJavaFXBase()
{
    private val schedulerDePrueba = TestScheduler()

    private val mockMenuDeFiltradoController =
            mockConDefaultAnswer(ControladorMenuFiltrado::class.java).also {
                doNothing().`when`(it).inicializar(cualquiera(), cualquiera())
            }
    private val mockCatalogoDeProductosController =
            mockConDefaultAnswer(ControladorCatalogoProductos::class.java).also {
                doNothing().`when`(it).inicializar(cualquiera(), cualquiera())
            }
    private val mockAgrupacionPersonasCarritosDeCreditosController =
            mockConDefaultAnswer(ControladorAgrupacionPersonasCarritosDeCreditos::class.java).also {
                doNothing().`when`(it).inicializar(cualquiera(), cualquiera())
            }

    private val mockMenuDeFiltrado = mockConDefaultAnswer(MenuFiltradoFondosUI::class.java) as MenuFiltradoFondosUI<PrompterIconosCategorias>
    private val mockCatalogo = mockConDefaultAnswer(CatalogoUI::class.java)
    private val mockAgrupacionPersonasCarritosDeCreditos = mockConDefaultAnswer(AgrupacionPersonasCarritosDeCreditosUI::class.java)
    private val eventosCreditosAProcesar = PublishSubject.create<List<ProcesoSeleccionCreditosUI.CreditosPorPersonaAProcesar>>()

    private val mockProcesoSeleccionCreditosUI =
            (mockConDefaultAnswer(ProcesoSeleccionCreditosUI::class.java) as ProcesoSeleccionCreditosUI<PrompterIconosCategorias>)
                .also {
                    doReturn(mockMenuDeFiltrado).`when`(it).menuFiltradoFondos
                    doReturn(mockCatalogo).`when`(it).catalogo
                    doReturn(mockAgrupacionPersonasCarritosDeCreditos).`when`(it).agrupacionCarritoDeCreditos
                    doReturn(eventosCreditosAProcesar.firstOrError()).`when`(it).creditosPorPersonaAProcesar
                }

    private val sujetoEstado = BehaviorSubject.createDefault(ProcesoCompraYSeleccionCreditosUI.EstadoConsulta.ESPERANDO)
    private val mockProcesoCompraYSeleccionCreditosUI =
            (mockConDefaultAnswer(ProcesoCompraYSeleccionCreditosUI::class.java) as ProcesoCompraYSeleccionCreditosUI<PrompterIconosCategorias>)
                .also {
                    doReturn(mockProcesoSeleccionCreditosUI).`when`(it).procesoSeleccionCreditos
                    doReturn(sujetoEstado.hide()!!).`when`(it).estadoConsulta
                }

    private val dialogoDeDeEspera = DialogoDeEspera()

    private val controladorEnPruebas = ControladorSeleccionCreditos().also {
        it.schedulerBackground = schedulerDePrueba
    }


    @BeforeEach
    private fun asignarCamposAControladorEInicializar()
    {
        validarEnThreadUI {
            controladorEnPruebas.controladorDeFlujo = darMockControladorDeFlujoInterno()
            controladorEnPruebas.controladorDeFlujo.contextoFlujo.agregarDependenciaDePantallaUnSoloUso(
                    ControladorSeleccionCreditos.Dependencias(emptyList())
                                                                                    )
        }

        controladorEnPruebas.procesoCompraYSeleccionCreditos = mockProcesoCompraYSeleccionCreditosUI
        controladorEnPruebas.menuDeFiltrado = mockMenuDeFiltradoController
        controladorEnPruebas.catalogoDeProductos = mockCatalogoDeProductosController
        controladorEnPruebas.agrupacionPersonasCarritosDeCreditos = mockAgrupacionPersonasCarritosDeCreditosController
        controladorEnPruebas.dialogoDeEspera = dialogoDeDeEspera
    }

    @Nested
    inner class AlInicializar
    {
        @BeforeEach
        fun inicializarModelo()
        {
            //controladorEnPruebas.inicializar()
        }

        @Test
        fun se_inicializa_el_controlador_de_menu_de_filtrado_con_parametros_correctos()
        {
            verify(mockMenuDeFiltradoController).inicializar(mockMenuDeFiltrado, schedulerDePrueba)
        }

        @Test
        fun se_inicializa_el_controlador_de_catalogo_con_parametros_correctos()
        {
            verify(mockCatalogoDeProductosController).inicializar(mockCatalogo, schedulerDePrueba)
        }

        @Test
        fun se_inicializa_el_controlador_de_agrupacion_de_personas_con_carritos_con_parametros_correctos()
        {
            //verify(mockAgrupacionPersonasCarritosDeCreditosController).inicializar(mockAgrupacionPersonasCarritosDeCreditos, schedulerDePrueba)
        }
    }

    @Nested
    inner class AlCambiarEstadoDeConsulta
    {
        @BeforeEach
        fun inicializarModelo()
        {
            controladorEnPruebas.inicializar()
        }


        @Nested
        inner class AlCambiarObservable
        {
            @Test
            fun solo_es_visible_cuando_el_estado_es_CONSULTANDO()
            {
                ProcesoCompraYSeleccionCreditosUI.EstadoConsulta.values().forEach {
                    sujetoEstado.onNext(it)
                    validarEnThreadUI {
                        assertEquals(
                                it == ProcesoCompraYSeleccionCreditosUI.EstadoConsulta.CONSULTANDO,
                                controladorEnPruebas.dialogoDeEspera.isVisible
                                    )
                    }
                }
            }

            @Test
            fun cambia_a_mensaje_correcto_al_cambiar_a_CONSULTANDO()
            {
                val mensajeIncorrecto = "Mensaje incorrecto"
                ProcesoCompraYSeleccionCreditosUI.EstadoConsulta.values().forEach {
                    controladorEnPruebas.dialogoDeEspera.labelTituloDialogo.text = mensajeIncorrecto
                    sujetoEstado.onNext(it)
                    validarEnThreadUI {
                        val mensajeEsperado = if (it == ProcesoCompraYSeleccionCreditosUI.EstadoConsulta.CONSULTANDO) "Consultando compras previas..." else mensajeIncorrecto
                        assertEquals(mensajeEsperado, controladorEnPruebas.dialogoDeEspera.labelTituloDialogo.text)
                    }
                }
            }
        }
    }
}