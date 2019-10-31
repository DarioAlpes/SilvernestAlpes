package co.smartobjects.ui.javafx.controladores.consumos

import co.smartobjects.ui.modelos.consumos.CodificacionDeConsumosUI
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label
import javafx.scene.layout.GridPane
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox

class ControladorDesgloseDeConsumo : GridPane()
{
    @FXML
    internal lateinit var recuadroBordeTitulo: Pane
    @FXML
    internal lateinit var nombreProductoAConsumir: Label
    @FXML
    internal lateinit var cantidadAConsumir: Label
    @FXML
    internal lateinit var consumosRealizados: VBox

    init
    {
        val loader = FXMLLoader(javaClass.getResource("/layouts/consumos/desgloseDeConsumo.fxml"))

        loader.setController(this)
        loader.setRoot(this)
        loader.load<GridPane>()
    }

    fun inicializar(desgloseDeConsumoConNombres: CodificacionDeConsumosUI.DesgloseDeConsumoConNombres)
    {
        nombreProductoAConsumir.text = desgloseDeConsumoConNombres.consumoConNombreConsumible.nombreConsumible
        cantidadAConsumir.text = desgloseDeConsumoConNombres.consumoConNombreConsumible.consumo.cantidad.valor.toPlainString()

        val controaldoresConsumosRealizados = desgloseDeConsumoConNombres.consumosRealizados.map {
            ControladorConsumoRealizado().apply {
                this@apply.inicializar(it)
            }
        }

        if (!desgloseDeConsumoConNombres.consumidoCompetamente)
        {
            recuadroBordeTitulo.styleClass.removeAll("borde-inferior-color-texto")
            recuadroBordeTitulo.styleClass.add("borde-inferior-color-operacion-fallida")

            nombreProductoAConsumir.styleClass.removeAll("etiqueta-25px-color-texto-bold")
            nombreProductoAConsumir.styleClass.add("etiqueta-25px-color-operacion-fallida-bold")

            cantidadAConsumir.styleClass.removeAll("etiqueta-25px-color-texto-bold")
            cantidadAConsumir.styleClass.add("etiqueta-25px-color-operacion-fallida-bold")
        }

        consumosRealizados.children.setAll(controaldoresConsumosRealizados)
    }
}