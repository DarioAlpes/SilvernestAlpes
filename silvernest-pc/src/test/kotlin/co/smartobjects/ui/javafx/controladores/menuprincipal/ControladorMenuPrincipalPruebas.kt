package co.smartobjects.ui.javafx.controladores.menuprincipal

import co.smartobjects.ui.javafx.PruebaJavaFXBase
import co.smartobjects.ui.javafx.controladores.consumos.ControladorConsumos
import co.smartobjects.ui.javafx.controladores.registropersonas.ControladorRegistrarPersonas
import co.smartobjects.ui.javafx.mockConDefaultAnswer
import co.smartobjects.ui.modelos.menuprincipal.MenuPrincipalUI
import javafx.event.ActionEvent
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify


internal class ControladorMenuPrincipalPruebas : PruebaJavaFXBase()
{
    private val controladorEnPruebas = ControladorMenuPrincipal()


    @BeforeEach
    private fun asignarCamposAControladorEInicializar()
    {
        validarEnThreadUI {
            controladorEnPruebas.controladorDeFlujo = darMockControladorDeFlujoInterno()
        }
        controladorEnPruebas.botonIrARegistrar = BotonMenuPrincipal("REGISTRO")
        controladorEnPruebas.botonIrAConsumos = BotonMenuPrincipal("COMPRAS")

        controladorEnPruebas.menuPrincipal = mockConDefaultAnswer(MenuPrincipalUI::class.java)

        controladorEnPruebas.inicializar()
    }

    @Test
    @Disabled
    fun al_hacer_click_en_boton_de_ir_a_reservas_se_cambia_a_pantalla_correcta()
    {
        controladorEnPruebas.botonIrARegistrar.fireEvent(ActionEvent())

        validarEnThreadUI {
            verify(controladorEnPruebas.controladorDeFlujo).navigate(ControladorRegistrarPersonas::class.java)
        }
    }

    @Test
    @Disabled
    fun al_hacer_click_en_boton_de_ir_a_comprar_se_cambia_a_pantalla_correcta()
    {
        controladorEnPruebas.botonIrAConsumos.fireEvent(ActionEvent())

        validarEnThreadUI {
            verify(controladorEnPruebas.controladorDeFlujo).navigate(ControladorConsumos::class.java)
        }
    }
}