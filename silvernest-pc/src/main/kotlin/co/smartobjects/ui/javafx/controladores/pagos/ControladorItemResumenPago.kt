package co.smartobjects.ui.javafx.controladores.pagos

import co.smartobjects.ui.modelos.pagos.ProcesoPagarUI
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label
import javafx.scene.layout.GridPane

internal class ControladorItemResumenPago : GridPane()
{
    @FXML
    internal lateinit var labelNombreDelProducto: Label
    @FXML
    internal lateinit var labelNombresReglasAplicadas: Label

    init
    {
        val loader = FXMLLoader(javaClass.getResource("/layouts/pagos/itemResumenPago.fxml"))

        loader.setController(this)
        loader.setRoot(this)
        loader.load<GridPane>()
    }

    internal fun asignarResumenPagoProducto(resumenPagoProducto: ProcesoPagarUI.ResumenPagoProducto)
    {
        labelNombreDelProducto.text = "${resumenPagoProducto.cantidad.valor.toPlainString()} ${resumenPagoProducto.nombre}"
        labelNombresReglasAplicadas.text = resumenPagoProducto.nombresRestriccionesUsadasParaPrecio.joinToString()
    }
}