package co.smartobjects.ui.javafx.controladores.pagos

import co.smartobjects.entidades.operativas.compras.Pago
import co.smartobjects.ui.javafx.comoDineroFormateado
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label
import javafx.scene.layout.GridPane

internal class ControladorInformacionPago : GridPane()
{
    @FXML
    internal lateinit var labelNumeroTransaccion: Label
    @FXML
    internal lateinit var labelValorPagado: Label
    @FXML
    internal lateinit var iconoMetodoPago: FontAwesomeIconView

    init
    {
        val loader = FXMLLoader(javaClass.getResource("/layouts/pagos/informacionPago.fxml"))

        loader.setController(this)
        loader.setRoot(this)
        loader.load<GridPane>()
    }

    internal fun asignarPago(pago: Pago)
    {
        labelNumeroTransaccion.text = pago.numeroDeTransaccionPOS
        labelValorPagado.text = pago.valorPagado.comoDineroFormateado()
        iconoMetodoPago.setIcon(
                when (pago.metodoPago)
                {
                    Pago.MetodoDePago.EFECTIVO        -> FontAwesomeIcon.MONEY
                    Pago.MetodoDePago.TARJETA_DEBITO  -> FontAwesomeIcon.BANK
                    Pago.MetodoDePago.TARJETA_CREDITO -> FontAwesomeIcon.CREDIT_CARD
                    Pago.MetodoDePago.TIC             -> FontAwesomeIcon.CIRCLE_ALT
                }
                               )
    }
}