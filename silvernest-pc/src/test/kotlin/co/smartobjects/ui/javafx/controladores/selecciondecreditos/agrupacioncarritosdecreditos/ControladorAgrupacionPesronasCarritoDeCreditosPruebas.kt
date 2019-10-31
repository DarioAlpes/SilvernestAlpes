package co.smartobjects.ui.javafx.controladores.selecciondecreditos.agrupacioncarritosdecreditos

import co.smartobjects.ui.javafx.PruebaJavaFXBase
import co.smartobjects.ui.javafx.controladores.genericos.ControladorListaFiltrable
import co.smartobjects.ui.javafx.mockConDefaultAnswer
import co.smartobjects.ui.modelos.ListaFiltrableUI
import co.smartobjects.ui.modelos.ListaFiltrableUIConSujetos
import co.smartobjects.ui.modelos.selecciondecreditos.agrupacioncarritosdecreditos.AgrupacionPersonasCarritosDeCreditosUI
import co.smartobjects.ui.modelos.selecciondecreditos.agrupacioncarritosdecreditos.PersonaConCarrito
import co.smartobjects.utilidades.Decimal
import com.jfoenix.controls.JFXButton
import io.reactivex.Observable
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.BehaviorSubject
import javafx.event.ActionEvent
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.verify
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ControladorAgrupacionPesronasCarritoDeCreditosPruebas : PruebaJavaFXBase()
{
    companion object
    {
        private val TOTAL_SIN_IMPUESTO = Decimal(123)
        private val IMPUESTO_TOTAL = Decimal(789)
        private val GRAN_TOTAL = Decimal(487)
        private val VALOR_YA_PAGADO = Decimal(263)
    }

    private val schedulerDePrueba = TestScheduler()
    private val mockAgrupacionPersonasCarritoDeCreditos =
            mockConDefaultAnswer(AgrupacionPersonasCarritosDeCreditosUI::class.java)
                .also {
                    doReturn(Observable.just(ListaFiltrableUIConSujetos(listOf<PersonaConCarrito>())))
                        .`when`(it)
                        .personasConCarritos

                    doReturn(Observable.just(TOTAL_SIN_IMPUESTO)).`when`(it).totalSinImpuesto
                    doReturn(Observable.just(IMPUESTO_TOTAL)).`when`(it).impuestoTotal
                    doReturn(Observable.just(GRAN_TOTAL)).`when`(it).saldo
                    doReturn(Observable.just(VALOR_YA_PAGADO)).`when`(it).valorYaPagado
                    doReturn(Observable.just(false)).`when`(it).estaAgregandoProducto
                    doReturn(BehaviorSubject.createDefault(true)).`when`(it).puedePagar
                    doReturn(Observable.just(false)).`when`(it).todosLosCreditosEstanPagados

                    doNothing().`when`(it).cancelarCreditosAgregados()
                    doNothing().`when`(it).confirmarCreditosAgregados()
                    doNothing().`when`(it).pagar()
                }

    private val mockControladorListaFiltrable =
            (mockConDefaultAnswer(ControladorListaFiltrable::class.java) as ControladorListaFiltrable<PersonaConCarrito>)
                .also {
                    doNothing()
                        .`when`(it)
                        .inicializar(
                                cualquiera<Observable<ListaFiltrableUI<PersonaConCarrito>>>(),
                                cualquiera(),
                                cualquiera()
                                    )
                }


    private val controladorEnPruebas = ControladorAgrupacionPersonasCarritosDeCreditos()

    @BeforeEach
    private fun asignarCamposAControladorEInicializar()
    {
        controladorEnPruebas.listaPersonasCarritosDeCreditos = mockControladorListaFiltrable
        controladorEnPruebas.total = Label()
        controladorEnPruebas.impuesto = Label()
        controladorEnPruebas.granTotal = Label()
        controladorEnPruebas.contendorBotonesAgregando = HBox()
        controladorEnPruebas.botonCancelar = JFXButton()
        controladorEnPruebas.botonConfirmar = JFXButton()
        controladorEnPruebas.botonPagar = JFXButton()
        controladorEnPruebas.pagado = Label()

        controladorEnPruebas.inicializar(mockAgrupacionPersonasCarritoDeCreditos, schedulerDePrueba)
        schedulerDePrueba.triggerActions()
    }

    private fun formateaDecimalComoDineroEsperado(numero: Decimal): String
    {
        val formateador = NumberFormat.getCurrencyInstance(Locale.getDefault())
            .apply {
                maximumFractionDigits = 0
                val simbolos = (this as DecimalFormat).decimalFormatSymbols
                simbolos.currencySymbol = ""
                decimalFormatSymbols = simbolos
            }

        return "$ ${formateador.format(numero.valor)}"
    }

    @Test
    fun el_total_sin_impuestos_mostrado_es_correcto()
    {
        validarEnThreadUI {
            assertEquals(formateaDecimalComoDineroEsperado(TOTAL_SIN_IMPUESTO), controladorEnPruebas.total.text)
        }
    }

    @Test
    fun el_impuesto_total_mostrado_es_correcto()
    {
        validarEnThreadUI {
            assertEquals(formateaDecimalComoDineroEsperado(IMPUESTO_TOTAL), controladorEnPruebas.impuesto.text)
        }
    }

    @Test
    fun el_gran_total_mostrado_es_correcto()
    {
        validarEnThreadUI {
            assertEquals(formateaDecimalComoDineroEsperado(GRAN_TOTAL), controladorEnPruebas.granTotal.text)
        }
    }

    @Test
    fun el_valor_ya_pagado_mostrado_es_correcto()
    {
        validarEnThreadUI {
            assertEquals(formateaDecimalComoDineroEsperado(VALOR_YA_PAGADO), controladorEnPruebas.pagado.text)
        }
    }

    @Test
    fun al_cancelar_se_invoca_el_metodo_de_cancelar_creditos_agregados()
    {
        controladorEnPruebas.botonCancelar.fireEvent(ActionEvent())
        verify(mockAgrupacionPersonasCarritoDeCreditos).cancelarCreditosAgregados()
    }

    @Test
    fun al_confirmar_se_invoca_el_metodo_de_confirmar_creditos_agregados()
    {
        controladorEnPruebas.botonConfirmar.fireEvent(ActionEvent())
        verify(mockAgrupacionPersonasCarritoDeCreditos).confirmarCreditosAgregados()
    }

    @Test
    fun si_no_puede_pagar_el_boton_de_pagar_esta_deshabilitado()
    {
        val sujeto = mockAgrupacionPersonasCarritoDeCreditos.puedePagar as BehaviorSubject<Boolean>
        sujeto.onNext(false)
        validarEnThreadUI {
            assertTrue(controladorEnPruebas.botonPagar.isDisable)
        }
    }

    @Test
    fun al_pagar_se_invoca_el_metodo_de_pagar()
    {
        controladorEnPruebas.botonPagar.fireEvent(ActionEvent())
        verify(mockAgrupacionPersonasCarritoDeCreditos).pagar()
    }
}