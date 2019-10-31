package co.smartobjects.ui.javafx.controladores.selecciondecreditos.agrupacioncarritosdecreditos

import co.smartobjects.entidades.personas.PersonaConGrupoCliente
import co.smartobjects.ui.javafx.comoDineroFormateado
import co.smartobjects.ui.javafx.controladores.carritocreditos.ControladorCarritoDeCreditos
import co.smartobjects.ui.javafx.controladores.registropersonas.ControladorInformacionPersonaConGrupo
import co.smartobjects.ui.javafx.observarEnFx
import co.smartobjects.ui.modelos.selecciondecreditos.agrupacioncarritosdecreditos.PersonaConCarrito
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.GridPane
import javafx.scene.layout.Pane
import java.net.URL
import java.util.*

internal class ControladorItemPersonaConCarrito : Initializable
{
    @FXML
    internal lateinit var raiz: GridPane

    @FXML
    internal lateinit var informacionPersonaConGrupo: ControladorInformacionPersonaConGrupo

    @FXML
    internal lateinit var saldo: Label

    @FXML
    internal lateinit var carritoDeCreditos: ControladorCarritoDeCreditos

    init
    {
        val fxmlLoader = FXMLLoader(javaClass.getResource("/layouts/selecciondecreditos/agrupacioncarritosdecreditos/itemPersonaConCarrito.fxml"))
        fxmlLoader.setController(this)
        fxmlLoader.load<Pane>()
    }

    override fun initialize(location: URL?, resources: ResourceBundle?)
    {
    }

    fun inicializar(personaConCarrito: PersonaConCarrito): Node
    {
        informacionPersonaConGrupo
            .asignarPersonaConGrupoCliente(PersonaConGrupoCliente(personaConCarrito.persona, personaConCarrito.grupoDeClientes))

        personaConCarrito.carritoDeCreditos.saldo.comoDineroFormateado(1).observarEnFx().subscribe {
            saldo.text = it
        }

        carritoDeCreditos.inicializar(personaConCarrito.carritoDeCreditos)

        return raiz
    }
}