package co.smartobjects.ui.javafx.controladores.consumos

import co.smartobjects.ui.modelos.consumos.CodificacionDeConsumosUI
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label
import javafx.scene.layout.GridPane


class ControladorConsumoRealizado : GridPane()
{
    @FXML
    internal lateinit var nombreFondoConsumido: Label
    @FXML
    internal lateinit var inicial: Label
    @FXML
    internal lateinit var consumido: Label
    @FXML
    internal lateinit var saldo: Label

    init
    {
        val loader = FXMLLoader(javaClass.getResource("/layouts/consumos/consumoRealizado.fxml"))

        loader.setController(this)
        loader.setRoot(this)
        loader.load<GridPane>()
    }

    fun inicializar(consumoRealizadoConNombre: CodificacionDeConsumosUI.ConsumoRealizadoConNombre)
    {
        nombreFondoConsumido.text = consumoRealizadoConNombre.nombreFondoConsumido
        inicial.text = consumoRealizadoConNombre.cantidadInicial.valor.toPlainString()
        consumido.text = consumoRealizadoConNombre.cantidadConsumida.valor.toPlainString()
        saldo.text = consumoRealizadoConNombre.cantidadFinal.valor.toPlainString()
    }
}