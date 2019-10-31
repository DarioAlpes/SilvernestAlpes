package co.smartobjects.ui.javafx.controladores.selecciondecreditos.agrupacioncarritosdecreditos

import co.smartobjects.entidades.fondos.precios.GrupoClientes
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.personas.PersonaConGrupoCliente
import co.smartobjects.ui.javafx.PruebaJavaFXBase
import co.smartobjects.ui.javafx.comoDineroFormateado
import co.smartobjects.ui.javafx.controladores.carritocreditos.ControladorCarritoDeCreditos
import co.smartobjects.ui.javafx.controladores.registropersonas.ControladorInformacionPersonaConGrupo
import co.smartobjects.ui.javafx.mockConDefaultAnswer
import co.smartobjects.ui.modelos.carritocreditos.CarritoDeCreditosUI
import co.smartobjects.ui.modelos.selecciondecreditos.agrupacioncarritosdecreditos.PersonaConCarrito
import co.smartobjects.utilidades.Decimal
import io.reactivex.Observable
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.verify
import kotlin.test.assertEquals

internal class ControladorItemPersonaConCarritoPruebas : PruebaJavaFXBase()
{
    @Test
    fun al_actualizar_se_conectan_controles_correctamente_el_producto()
    {
        val objetoDePrueba = ControladorItemPersonaConCarrito()

        val mockControladorInformacionPersonaConGrupo = mockConDefaultAnswer(ControladorInformacionPersonaConGrupo::class.java).also {
            doNothing().`when`(it).asignarPersonaConGrupoCliente(cualquiera())
        }

        val mockControladorCarritoDeCreditos = mockConDefaultAnswer(ControladorCarritoDeCreditos::class.java).also {
            doNothing().`when`(it).inicializar(cualquiera())
        }

        objetoDePrueba.informacionPersonaConGrupo = mockControladorInformacionPersonaConGrupo
        objetoDePrueba.carritoDeCreditos = mockControladorCarritoDeCreditos

        val mockPersona = mockConDefaultAnswer(Persona::class.java)
        val mockGrupoClientes = mockConDefaultAnswer(GrupoClientes::class.java)
        val saldoEsperado = Decimal(453.23454)
        val saldoFormateado = saldoEsperado.comoDineroFormateado(1)
        val mockCarritoDeCreditos = mockConDefaultAnswer(CarritoDeCreditosUI::class.java).also {
            doReturn(Observable.just(saldoEsperado)).`when`(it).saldo
        }
        val personaConCarrito =
                PersonaConCarrito(
                        mockPersona,
                        mockGrupoClientes,
                        mockCarritoDeCreditos
                                 )

        objetoDePrueba.inicializar(personaConCarrito)

        validarEnThreadUI {
            assertEquals(saldoFormateado, objetoDePrueba.saldo.text)
        }

        verify(mockControladorInformacionPersonaConGrupo)
            .asignarPersonaConGrupoCliente(PersonaConGrupoCliente(mockPersona, mockGrupoClientes))

        verify(mockControladorCarritoDeCreditos)
            .inicializar(mockCarritoDeCreditos)
    }
}