package co.smartobjects.ui.javafx.controladores.registropersonas

import co.smartobjects.entidades.personas.Persona
import co.smartobjects.red.clientes.personas.PersonasAPI
import co.smartobjects.ui.javafx.*
import co.smartobjects.ui.javafx.controladores.ControladorFechaSegunCampos
import co.smartobjects.ui.javafx.controladores.genericos.DialogoDeEspera
import co.smartobjects.ui.modelos.registropersonas.ProcesoCrearPersona
import co.smartobjects.ui.modelos.registropersonas.ProcesoCrearPersonaConSujetos
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXComboBox
import com.jfoenix.controls.JFXTextField
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label
import javafx.scene.control.Tooltip
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import org.threeten.bp.LocalDate

// Constructor para inyectar proceso en pruebas, no se debe usar en ejecución normal
internal class ControladorCrearPersona
(
        private val fechaInicialAUsar: LocalDate? = null
) : VBox()
{
    @FXML
    internal lateinit var comboTipoDocumento: JFXComboBox<Persona.TipoDocumento>
    @FXML
    internal lateinit var campoNumeroDocumento: JFXTextField
    @FXML
    internal lateinit var campoNombreCompleto: JFXTextField
    @FXML
    internal lateinit var comboGenero: JFXComboBox<Persona.Genero>
    @FXML
    internal lateinit var campoEmpresa: JFXTextField
    @FXML
    internal lateinit var campoNitEmpresa: JFXTextField
    @FXML
    internal lateinit var comboCategoria: JFXComboBox<Persona.Categoria>
    @FXML
    internal lateinit var comboTipo: JFXComboBox<Persona.Tipo>
    @FXML
    internal lateinit var controladorFechaDeNacimiento: ControladorFechaSegunCampos

    @FXML
    internal lateinit var botonReiniciarCreacionPersona: JFXButton
    @FXML
    internal lateinit var panelTooltipoBotonCrearPersona: StackPane
    @FXML
    internal lateinit var botonCrearPersona: JFXButton
    @FXML
    internal lateinit var labelError: Label

    internal lateinit var procesoCrearPersona: ProcesoCrearPersona

    private val tooltipBotonCrear = inicializarTooltipConTexto("Falta completar el formulario")

    init
    {
        val loader = FXMLLoader(javaClass.getResource("/layouts/registropersonas/crearPersona.fxml"))

        loader.setController(this)
        loader.setRoot(this)
        loader.load<VBox>()
    }


    internal val informacionBindingDialogoDeEspera: DialogoDeEspera.InformacionBindingDialogoEspera<ProcesoCrearPersona.Estado> by lazy {
        val mensajeCrear = DialogoDeEspera.InformacionMensajeEspera(ProcesoCrearPersona.Estado.CREANDO_PERSONA, "Creando persona...")
        val mensajeConsultar = DialogoDeEspera.InformacionMensajeEspera(ProcesoCrearPersona.Estado.CONSULTANDO_PERSONA, "Consultando persona..")
        DialogoDeEspera.InformacionBindingDialogoEspera(procesoCrearPersona.estado, listOf(mensajeCrear, mensajeConsultar))
    }


    fun inicializar(idCliente: Long, personasApi: PersonasAPI)
    {
        procesoCrearPersona = ProcesoCrearPersonaConSujetos(idCliente, personasApi)

        comboTipoDocumento.inicializarBindingCampo(
                Persona.TipoDocumento.CC,
                Persona.TipoDocumento.values().toList(),
                procesoCrearPersona.persona.tipoDocumento,
                { procesoCrearPersona.persona.cambiarTipoDocumento(it) }
                                                  )
        comboTipoDocumento.alPerderFoco { procesoCrearPersona.intentarConsultarPersonaPorDocumento() }
        comboTipoDocumento.alCambiarDeValor { procesoCrearPersona.intentarConsultarPersonaPorDocumento() }

        Platform.runLater { campoNumeroDocumento.requestFocus() }
        campoNumeroDocumento.inicializarBindingCampoRequerido(
                "",
                procesoCrearPersona.persona.numeroDocumento,
                { procesoCrearPersona.persona.cambiarNumeroDocumento(it) },
                { procesoCrearPersona.intentarCrearPersonaOConsultarPersonaPorDocumentoDeSerNecesario() }
                                                             )
        campoNumeroDocumento.alPerderFoco { procesoCrearPersona.intentarConsultarPersonaPorDocumento() }

        campoNombreCompleto.inicializarBindingCampoRequerido(
                "",
                procesoCrearPersona.persona.nombreCompleto,
                { procesoCrearPersona.persona.cambiarNombreCompleto(it) },
                { procesoCrearPersona.intentarCrearPersonaOConsultarPersonaPorDocumentoDeSerNecesario() }
                                                            )

        campoEmpresa.inicializarBindingCampoRequerido(
                "n/a",
                procesoCrearPersona.persona.empresa,
                { procesoCrearPersona.persona.cambiarEmpresa(it) },
                { procesoCrearPersona.intentarCrearPersonaOConsultarPersonaPorDocumentoDeSerNecesario() }
        )

        campoNitEmpresa.inicializarBindingCampoRequerido(
                "0",
                procesoCrearPersona.persona.nitEmpresa,
                { procesoCrearPersona.persona.cambiarNitEmpresa(it) },
                { procesoCrearPersona.intentarCrearPersonaOConsultarPersonaPorDocumentoDeSerNecesario() }
        )

        comboGenero.inicializarBindingCampo(
                Persona.Genero.MASCULINO,
                Persona.Genero.values().toList(),
                procesoCrearPersona.persona.genero,
                { procesoCrearPersona.persona.cambiarGenero(it) }
                                           )

        comboCategoria.inicializarBindingCampo(
                Persona.Categoria.D,
                Persona.Categoria.values().toList(),
                procesoCrearPersona.persona.categoria,
                { procesoCrearPersona.persona.cambiarCategoria(it) }
                                              )

        comboTipo.inicializarBindingCampo(
                Persona.Tipo.NO_AFILIADO,
                Persona.Tipo.values().toList(),
                procesoCrearPersona.persona.tipo,
                { procesoCrearPersona.persona.cambiarTipo(it) }
        )

        controladorFechaDeNacimiento.inicializarSegunModeloYNombre(
                "Nacimiento",
                fechaInicialAUsar ?: LocalDate.now().minusYears(25),
                procesoCrearPersona.persona.fechaNacimiento,
                { procesoCrearPersona.intentarCrearPersonaOConsultarPersonaPorDocumentoDeSerNecesario() }
                                                                  )

        botonReiniciarCreacionPersona.setOnAction {
            reiniciarFormulario()
        }

        procesoCrearPersona.persona.cambiarCategoria(Persona.Categoria.D)

        procesoCrearPersona.persona.cambiarAfiliacion(Persona.Afiliacion.COTIZANTE)

        botonCrearPersona.inicializarBindingAccion(procesoCrearPersona.puedeCrearPersona) {
            procesoCrearPersona.intentarCrearPersonaOConsultarPersonaPorDocumentoDeSerNecesario()
        }
        procesoCrearPersona.puedeCrearPersona.observarEnFx().subscribe {
            if (it)
            {
                Tooltip.uninstall(panelTooltipoBotonCrearPersona, tooltipBotonCrear)
            }
            else
            {
                Tooltip.install(panelTooltipoBotonCrearPersona, tooltipBotonCrear)
            }
        }

        val camposHabilitados = procesoCrearPersona.estado.map { it !== ProcesoCrearPersona.Estado.CONSULTANDO_PERSONA }
        comboTipoDocumento.bindEstaHabilitado(camposHabilitados)
        campoNumeroDocumento.bindEstaHabilitado(camposHabilitados)
        campoNombreCompleto.bindEstaHabilitado(camposHabilitados)
        comboGenero.bindEstaHabilitado(camposHabilitados)
        comboCategoria.bindEstaHabilitado(camposHabilitados)
        controladorFechaDeNacimiento.bindEstaHabilitado(camposHabilitados)
        botonReiniciarCreacionPersona.bindEstaHabilitado(camposHabilitados)
        botonCrearPersona.bindEstaHabilitado(camposHabilitados)
        campoEmpresa.bindEstaHabilitado(camposHabilitados)
        campoNitEmpresa.bindEstaHabilitado(camposHabilitados)
        comboTipo.bindEstaHabilitado(camposHabilitados)

        labelError.inicializarBindingLabelError(procesoCrearPersona.errorGlobal)
    }

    fun reiniciarFormulario()
    {
        comboTipoDocumento.value = Persona.TipoDocumento.CC

        campoNumeroDocumento.clear()
        campoNumeroDocumento.resetValidation()
        campoNumeroDocumento.requestFocus()

        reiniciarFormularioDejandoDocumentoCompletoIntacto()
    }

    fun reiniciarFormularioDejandoDocumentoCompletoIntacto()
    {
        procesoCrearPersona.persona.anularId()

        campoNombreCompleto.clear()
        campoNombreCompleto.resetValidation()

        campoEmpresa.clear()
        campoEmpresa.text = "n/a"
        campoEmpresa.resetValidation()
        campoNitEmpresa.clear()
        campoNitEmpresa.text = "0"
        campoNitEmpresa.resetValidation()
        comboGenero.value = Persona.Genero.MASCULINO
        comboCategoria.value = Persona.Categoria.D
        comboTipo.value = Persona.Tipo.NO_AFILIADO

        labelError.text = ""

        controladorFechaDeNacimiento.reiniciar()
    }
}