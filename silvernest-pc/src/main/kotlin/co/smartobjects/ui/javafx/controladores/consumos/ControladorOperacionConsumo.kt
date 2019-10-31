package co.smartobjects.ui.javafx.controladores.consumos

import co.smartobjects.ui.javafx.controladores.carritocreditos.ControladorCarritoDeCreditos
import co.smartobjects.ui.javafx.hacerEscondible
import co.smartobjects.ui.javafx.inicializarLabelMonetario
import co.smartobjects.ui.javafx.usarSchedulersEnUI
import co.smartobjects.ui.modelos.consumos.CodificacionDeConsumosUI
import co.smartobjects.utilidades.formatearComoFechaHoraConMesCompleto
import com.jfoenix.controls.JFXScrollPane
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.control.TitledPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox


class ControladorOperacionConsumo : VBox()
{
    @FXML
    internal lateinit var carritoDeCreditos: ControladorCarritoDeCreditos
    @FXML
    internal lateinit var labelCarritoVacio: Label

    @FXML
    internal lateinit var desgloseDeTotales: GridPane
    @FXML
    internal lateinit var total: Label
    @FXML
    internal lateinit var impuesto: Label
    @FXML
    internal lateinit var granTotal: Label

    @FXML
    internal lateinit var labelEsperandoManilla: Label

    @FXML
    private lateinit var expandibleResultado: TitledPane
    @FXML
    private lateinit var tituloResultado: Label
    @FXML
    private lateinit var scrollResultadoConsumos: ScrollPane
    @FXML
    private lateinit var listaDesglosesDeConsumos: VBox

    init
    {
        val loader = FXMLLoader(javaClass.getResource("/layouts/consumos/operacionConsumo.fxml"))

        loader.setController(this)
        loader.setRoot(this)
        loader.load<VBox>()

        if (scrollResultadoConsumos.content != null)
        {
            JFXScrollPane.smoothScrolling(scrollResultadoConsumos)
        }
    }

    fun inicializar(codificacionDeConsumos: CodificacionDeConsumosUI, schedulerBackground: Scheduler = Schedulers.io())
    {
        labelCarritoVacio.isVisible = true
        labelCarritoVacio.hacerEscondible()

        carritoDeCreditos.inicializar(codificacionDeConsumos)

        desgloseDeTotales.isVisible = false
        desgloseDeTotales.hacerEscondible()

        inicializarLabelMonetario(total, codificacionDeConsumos.totalSinImpuesto.usarSchedulersEnUI(schedulerBackground))
        inicializarLabelMonetario(impuesto, codificacionDeConsumos.impuestoTotal.usarSchedulersEnUI(schedulerBackground))
        inicializarLabelMonetario(granTotal, codificacionDeConsumos.granTotal.usarSchedulersEnUI(schedulerBackground))

        labelEsperandoManilla.isVisible = false
        labelEsperandoManilla.hacerEscondible()

        codificacionDeConsumos.estado.usarSchedulersEnUI(schedulerBackground).subscribe {
            labelCarritoVacio.isVisible = it === CodificacionDeConsumosUI.Estado.CON_CARRITO_VACIO
            desgloseDeTotales.isVisible = !labelCarritoVacio.isVisible
            labelEsperandoManilla.isVisible = desgloseDeTotales.isVisible
        }

        inicializarExpandibleResultadoConsumo(codificacionDeConsumos, schedulerBackground)
    }

    private fun inicializarExpandibleResultadoConsumo(codificacionDeConsumos: CodificacionDeConsumosUI, schedulerBackground: Scheduler)
    {
        codificacionDeConsumos.ultimosResultadoDeConsumos.usarSchedulersEnUI(schedulerBackground).subscribe {
            if (it.esVacio)
            {
                expandibleResultado.isVisible = false
            }
            else
            {
                expandibleResultado.isVisible = true

                val resultadoConsumos = it.valor

                expandibleResultado.text =
                        (if (resultadoConsumos.todosLosConsumosRealizadosPorCompleto) "Exitosa" else "Fallida") +
                        "   " + formatearComoFechaHoraConMesCompleto(resultadoConsumos.fechaYHoraDeRealizacion)

                tituloResultado.text = if (resultadoConsumos.todosLosConsumosRealizadosPorCompleto) "EXITOSA" else "SIN SALDO"
                tituloResultado.styleClass.removeAll("etiqueta-fondo-color-primario", "etiqueta-fondo-color-operacion-fallida")
                tituloResultado.styleClass.add(if (resultadoConsumos.todosLosConsumosRealizadosPorCompleto) "etiqueta-fondo-color-primario" else "etiqueta-fondo-color-operacion-fallida")

                val controladoresConsumosRealizados = resultadoConsumos.desgloseConNombres.map {
                    ControladorDesgloseDeConsumo().apply { inicializar(it) }
                }

                listaDesglosesDeConsumos.children.setAll(controladoresConsumosRealizados)
                scrollResultadoConsumos.vvalue = 0.0
            }
        }
    }
}