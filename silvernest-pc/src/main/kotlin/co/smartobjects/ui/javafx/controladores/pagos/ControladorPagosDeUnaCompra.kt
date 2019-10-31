package co.smartobjects.ui.javafx.controladores.pagos

import co.smartobjects.ui.javafx.CssToColorHelper
import co.smartobjects.ui.javafx.comoDineroConNegativosFormateado
import co.smartobjects.ui.javafx.controladores.genericos.ControladorItemEliminable
import co.smartobjects.ui.javafx.inicializarBindingListaTransformandoANodo
import co.smartobjects.ui.javafx.observarEnFx
import co.smartobjects.ui.modelos.pagos.PagosDeUnaCompraUI
import co.smartobjects.ui.modelos.pagos.ProcesoAgregarPago
import co.smartobjects.utilidades.formatearComoFechaConMesCompleto
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import org.threeten.bp.ZonedDateTime


internal class ControladorPagosDeUnaCompra : VBox()
{
    private lateinit var modeloPagosDeUnaCompra: PagosDeUnaCompraUI

    @FXML
    internal lateinit var saldoPendiente: Label
    @FXML
    internal lateinit var agregarPago: ControladorAgregarPago
    @FXML
    internal lateinit var fechaDeCompra: Label
    @FXML
    internal lateinit var contenedorPagos: VBox

    private val extractorDeColorEnCSS = CssToColorHelper()

    init
    {
        val loader = FXMLLoader(javaClass.getResource("/layouts/pagos/pagosDeUnaCompra.fxml"))

        loader.setController(this)
        loader.setRoot(this)
        loader.load<VBox>()

        children.add(extractorDeColorEnCSS)
    }

    fun inicializar(modeloPagosDeUnaCompra: PagosDeUnaCompraUI, fechaDeRealizacion: ZonedDateTime)
    {
        this.modeloPagosDeUnaCompra = modeloPagosDeUnaCompra
        fechaDeCompra.text = formatearComoFechaConMesCompleto(fechaDeRealizacion).toUpperCase()

        inicializarSaldoPendiente()
        inicializarAgregarPago()
        inicializarPanelDeCompra()
    }

    private fun inicializarSaldoPendiente()
    {
        val valorFaltantePorPagarFormateado = modeloPagosDeUnaCompra.valorFaltantePorPagar.observarEnFx()
        valorFaltantePorPagarFormateado.subscribe {
            saldoPendiente.text = it.comoDineroConNegativosFormateado()
            saldoPendiente.textFill = extractorDeColorEnCSS.getNamedColor(if (it < 0) "-color-saldo-en-contra" else "white")
        }
    }

    private fun inicializarAgregarPago()
    {
        agregarPago.inicializar(modeloPagosDeUnaCompra)
        agregarPago.procesoAgregarPago.estado.observarEnFx().subscribe {
            if (it === ProcesoAgregarPago.Estado.PAGO_AGREGADO)
            {
                agregarPago.procesoAgregarPago.reiniciar()
            }
        }
    }

    private fun inicializarPanelDeCompra()
    {
        contenedorPagos.inicializarBindingListaTransformandoANodo(modeloPagosDeUnaCompra.pagosRegistrados) {
            val controladorInformacionPago = ControladorInformacionPago().apply { asignarPago(it) }

            ControladorItemEliminable().apply {
                inicializar(controladorInformacionPago) { modeloPagosDeUnaCompra.eliminarPago(it) }
            }
        }
    }
}