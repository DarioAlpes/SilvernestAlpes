package co.smartobjects.ui.javafx.controladores.carritocreditos

import co.smartobjects.ui.javafx.PruebaJavaFXBase
import co.smartobjects.ui.javafx.mockConDefaultAnswer
import co.smartobjects.ui.modelos.carritocreditos.CarritoDeCreditosUI
import co.smartobjects.ui.modelos.carritocreditos.ItemCreditoUI
import co.smartobjects.utilidades.Decimal
import io.reactivex.Observable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doReturn
import kotlin.test.assertEquals

internal class ControladorCarritoDeCreditosPruebas : PruebaJavaFXBase()
{
    private val mockCreditosNoModificables =
            List(2) { indice ->
                mockConDefaultAnswer(ItemCreditoUI::class.java)
                    .also {
                        doReturn(false).`when`(it).estaPagado
                        doReturn("Producto NO Modificable $indice").`when`(it).nombreProducto
                        doReturn(Observable.just(indice + 1)).`when`(it).cantidad
                        doReturn(Observable.just(Decimal((indice + 1) * 100))).`when`(it).precioConImpuestos
                        doReturn(true).`when`(it).noEsModificable

                        doNothing().`when`(it).sumarUno()
                        doNothing().`when`(it).restarUno()
                        doNothing().`when`(it).borrar()
                    }
            }

    private val mockCreditosUI =
            mockConDefaultAnswer(CarritoDeCreditosUI::class.java)
                .also {
                    doReturn(Observable.just(mockCreditosNoModificables))
                        .`when`(it)
                        .creditosTotales

                    doNothing().`when`(it).pagar()
                }


    private val controladorEnPruebas = ControladorCarritoDeCreditos()

    @BeforeEach
    private fun asignarCamposAControladorEInicializar()
    {
        controladorEnPruebas.inicializar(mockCreditosUI)
    }

    @Test
    fun para_el_carrito_de_creditos_el_numero_total_de_creditos_mostrados_es_correcto()
    {
        validarEnThreadUI {
            assertEquals(
                    mockCreditosNoModificables.size,
                    controladorEnPruebas.items.size
                        )
        }
    }
}