package co.smartobjects.ui.javafx.controladores.registropersonas

import co.smartobjects.entidades.fondos.precios.SegmentoClientes
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.personas.PersonaConGrupoCliente
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.layout.GridPane
import javafx.scene.layout.Pane

internal class ControladorInformacionPersonaConGrupo : GridPane()
{
    @FXML
    internal lateinit var imagen: ImageView
    @FXML
    internal lateinit var labelNombre: Label
    @FXML
    internal lateinit var labelDocumento: Label
    @FXML
    internal lateinit var labelCategoria: Label
    @FXML
    internal lateinit var labelGrupoEdad: Label
    @FXML
    internal lateinit var labelTipo: Label

    init
    {
        val loader = FXMLLoader(javaClass.getResource("/layouts/registropersonas/informacionPersonaConGrupo.fxml"))

        loader.setController(this)
        loader.setRoot(this)
        loader.load<Pane>()
    }

    internal fun asignarPersonaConGrupoCliente(personaConGrupoCliente: PersonaConGrupoCliente)
    {
        labelNombre.text = personaConGrupoCliente.persona.nombreCompleto
        labelDocumento.text = "${personaConGrupoCliente.persona.tipoDocumento} ${personaConGrupoCliente.persona.numeroDocumento}"
        labelCategoria.text = "Categoría: ${personaConGrupoCliente.persona.categoria}"
        println("afiliacion: "+personaConGrupoCliente.persona.afiliacion)
        val afil = personaConGrupoCliente.persona.afiliacion
        if(afil.equals(Persona.Afiliacion.COTIZANTE)){
            println("entro")
            labelTipo.text = "Tipo: ${personaConGrupoCliente.persona.tipo}"
        }else{
            println("no entro")
            labelTipo.text = "Tipo: ${personaConGrupoCliente.persona.afiliacion}"
        }
        val categoria =
                personaConGrupoCliente
                    .posibleGrupoCliente
                    ?.segmentosClientes
                    ?.firstOrNull { it.campo == SegmentoClientes.NombreCampo.CATEGORIA }
                    ?.valor ?: "N/A"
        //labelCategoria.text = "Categoría: $categoria"
        val grupoEdad =
                personaConGrupoCliente
                    .posibleGrupoCliente
                    ?.segmentosClientes
                    ?.firstOrNull { it.campo == SegmentoClientes.NombreCampo.GRUPO_DE_EDAD }
                    ?.valor ?: "N/A"
        labelGrupoEdad.text = "Grupo Edad: $grupoEdad"
        val tipo =
                personaConGrupoCliente
                        .posibleGrupoCliente
                        ?.segmentosClientes
                        ?.firstOrNull { it.campo == SegmentoClientes.NombreCampo.TIPO }
                        ?.valor ?: "N/A"
        //labelTipo.text = "Tipo: $tipo"
    }
}