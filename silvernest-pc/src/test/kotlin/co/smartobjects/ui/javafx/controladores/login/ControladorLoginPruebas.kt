package co.smartobjects.ui.javafx.controladores.login

import co.smartobjects.entidades.excepciones.EntidadMalInicializada
import co.smartobjects.entidades.usuarios.Usuario
import co.smartobjects.ui.javafx.PruebaJavaFXBase
import co.smartobjects.ui.javafx.controladores.genericos.CampoTextoConSugerencias
import co.smartobjects.ui.javafx.controladores.genericos.DialogoDeEspera
import co.smartobjects.ui.javafx.controladores.menuprincipal.ControladorMenuPrincipal
import co.smartobjects.ui.javafx.mockConDefaultAnswer
import co.smartobjects.ui.modelos.ResultadoAccionUI
import co.smartobjects.ui.modelos.login.CredencialesUsuarioUI
import co.smartobjects.ui.modelos.login.ProcesoLogin
import co.smartobjects.utilidades.Opcional
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXPasswordField
import io.datafx.controller.flow.context.FlowActionHandler
import io.reactivex.Notification
import io.reactivex.subjects.BehaviorSubject
import javafx.event.ActionEvent
import javafx.scene.control.Label
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import kotlin.test.*

@DisplayName("ControladorLogin")
internal class ControladorLoginPruebas : PruebaJavaFXBase()
{
    // Modelos UI
    private val sujetoUsuario: BehaviorSubject<Notification<String>> = BehaviorSubject.create()
    private val sujetoContraseña: BehaviorSubject<Notification<CharArray>> = BehaviorSubject.create()
    private val mockCredencialesUsuarioUI: CredencialesUsuarioUI = mockConDefaultAnswer(CredencialesUsuarioUI::class.java).apply {
        doReturn(sujetoUsuario)
            .`when`(this)
            .usuario
        doReturn(sujetoContraseña)
            .`when`(this)
            .contraseña
    }
    private val sujetoPuedeIniciarSesion: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)
    private val sujetoEstado: BehaviorSubject<ProcesoLogin.Estado> = BehaviorSubject.createDefault(ProcesoLogin.Estado.ESPERANDO_CREDENCIALES)
    private val sujetoErrorGlobal: BehaviorSubject<Opcional<String>> = BehaviorSubject.createDefault(Opcional.Vacio())
    private val sujetoUsuarioSesionIniciada: BehaviorSubject<Usuario> = BehaviorSubject.create()
    private val mockProcesoLogin: ProcesoLogin = mockConDefaultAnswer(ProcesoLogin::class.java).apply {
        doReturn(mockCredencialesUsuarioUI)
            .`when`(this)
            .credenciales
        doReturn(sujetoPuedeIniciarSesion)
            .`when`(this)
            .puedeIniciarSesion
        doReturn(sujetoEstado)
            .`when`(this)
            .estado
        doReturn(sujetoErrorGlobal)
            .`when`(this)
            .errorGlobal
        doReturn(sujetoUsuarioSesionIniciada)
            .`when`(this)
            .usuarioSesionIniciada
    }

    // Campos UI Credenciales
    private val campoUsuario: CampoTextoConSugerencias = CampoTextoConSugerencias()
    private val campoContraseña: JFXPasswordField = JFXPasswordField()
    private val botonIniciarSesion: JFXButton = JFXButton()
    private val labelErrorLogin: Label = Label()

    private val labelTituloDialogo: Label = Label()
    private val dialogoDeDeEsperaController: DialogoDeEspera = DialogoDeEspera()

    private val controladorLogin = ControladorLogin()

    @BeforeEach
    private fun asignarCamposAControladorEInicializar()
    {
        validarEnThreadUI {
            controladorLogin.contextoFlujo = darMockContextoFlujo()
        }
        controladorLogin.navegadorFlujo = mockConDefaultAnswer(FlowActionHandler::class.java)

        controladorLogin.procesoLogin = mockProcesoLogin

        controladorLogin.campoUsuario = campoUsuario
        controladorLogin.campoContraseña = campoContraseña
        controladorLogin.botonIniciarSesion = botonIniciarSesion
        controladorLogin.labelError = labelErrorLogin
        controladorLogin.dialogoDeEspera = dialogoDeDeEsperaController
        dialogoDeDeEsperaController.labelTituloDialogo = labelTituloDialogo

        mockearCambiarUsuario("")
        mockearCambiarContraseña(charArrayOf())

        controladorLogin.inicializar()
    }

    private fun mockearCambiarUsuario(usuarioEsperado: String)
    {
        doNothing()
            .`when`(mockCredencialesUsuarioUI)
            .cambiarUsuario(usuarioEsperado)
    }

    private fun mockearCambiarContraseña(contraseñaEsperada: CharArray)
    {
        doNothing()
            .`when`(mockCredencialesUsuarioUI)
            .cambiarContraseña(contraseñaEsperada)
    }

    private fun mockearIntentarLogin()
    {
        doReturn(ResultadoAccionUI.ACCION_INICIADA)
            .`when`(mockProcesoLogin)
            .intentarLogin()
    }

    @Nested
    inner class Credenciales
    {
        @Nested
        inner class InputUsuario
        {
            @Test
            fun empieza_vacio()
            {
                assertEquals("", controladorLogin.campoUsuario.text)
            }

            @Test
            fun empieza_sin_errores()
            {
                assertTrue(controladorLogin.campoUsuario.validators.all { !it.hasErrors })
            }

            @Test
            fun llama_a_cambiarUsuario_con_valor_vacio_al_inicializar()
            {
                verify(mockCredencialesUsuarioUI).cambiarUsuario("")
            }

            @Nested
            inner class AlCambiarObservable
            {
                @Test
                fun a_valor_valido_se_cambia_el_valor_del_campo_y_llama_cambiarUsuario()
                {
                    val usuarioValido = "Usuario"
                    mockearCambiarUsuario(usuarioValido)
                    sujetoUsuario.onNext(Notification.createOnNext(usuarioValido))
                    validarEnThreadUI {
                        assertEquals(usuarioValido, controladorLogin.campoUsuario.text)
                        verify(mockCredencialesUsuarioUI).cambiarUsuario(usuarioValido)
                    }
                }

                @Test
                fun a_valor_de_error_con_excepción_EntidadMalInicializada_se_hace_trim_al_campo_y_activa_validador_con_error_correcto()
                {
                    val errorAEnviar = EntidadMalInicializada("entidad", "campo", "valor", "Descripcion")
                    val usuarioValido = "Usuario"
                    val usuarioAUsar = "    $usuarioValido    "
                    mockearCambiarUsuario(usuarioAUsar)
                    mockearCambiarUsuario(usuarioValido)
                    campoUsuario.text = usuarioAUsar

                    sujetoUsuario.onNext(Notification.createOnError(errorAEnviar))

                    validarEnThreadUI {
                        assertEquals(usuarioValido, controladorLogin.campoUsuario.text)

                        val validadorFallido = controladorLogin.campoUsuario.validators.firstOrNull {
                            it.hasErrors && it.message == errorAEnviar.descripcionDeLaRestriccion
                        }
                        assertNotNull(validadorFallido)

                        verify(mockCredencialesUsuarioUI).cambiarUsuario("")
                        verify(mockCredencialesUsuarioUI).cambiarUsuario(usuarioAUsar)
                        verify(mockCredencialesUsuarioUI).cambiarUsuario(usuarioValido)
                    }
                }

                @Test
                fun a_valor_de_error_con_excepción_diferente_a_EntidadMalInicializada_se_hace_trim_al_campo_y_activa_validador_con_error_correcto()
                {
                    val errorAEnviar = Exception("Error esperado")
                    val usuarioValido = "Usuario"
                    val usuarioAUsar = "    $usuarioValido    "
                    mockearCambiarUsuario(usuarioAUsar)
                    mockearCambiarUsuario(usuarioValido)
                    campoUsuario.text = usuarioAUsar

                    sujetoUsuario.onNext(Notification.createOnError(errorAEnviar))
                    validarEnThreadUI {
                        assertEquals(usuarioValido, controladorLogin.campoUsuario.text)

                        val validadorFallido = controladorLogin.campoUsuario.validators.firstOrNull {
                            it.hasErrors && it.message == errorAEnviar.message
                        }
                        assertNotNull(validadorFallido)

                        verify(mockCredencialesUsuarioUI).cambiarUsuario("")
                        verify(mockCredencialesUsuarioUI).cambiarUsuario(usuarioAUsar)
                        verify(mockCredencialesUsuarioUI).cambiarUsuario(usuarioValido)
                    }
                }

                @Test
                fun a_valor_correcto_despues_de_error_limpia_el_validador()
                {
                    val errorAEnviar = EntidadMalInicializada("entidad", "campo", "valor", "Descripcion")
                    val usuarioValido = "Usuario"
                    mockearCambiarUsuario(usuarioValido)
                    campoUsuario.text = usuarioValido

                    sujetoUsuario.onNext(Notification.createOnError(errorAEnviar))
                    sujetoUsuario.onNext(Notification.createOnNext(usuarioValido))

                    validarEnThreadUI {
                        val validadorFallido = controladorLogin.campoUsuario.validators.firstOrNull {
                            it.hasErrors
                        }
                        assertNull(validadorFallido)

                        verify(mockCredencialesUsuarioUI).cambiarUsuario("")
                        verify(mockCredencialesUsuarioUI).cambiarUsuario(usuarioValido)
                    }
                }
            }

            @Nested
            inner class AlCambiarCampoTexto
            {
                @Test
                fun a_valor_correcto_llama_metodo_cambiarUsuario_con_valor_correcto()
                {
                    val usuarioValido = "Usuario"
                    mockearCambiarUsuario(usuarioValido)
                    controladorLogin.campoUsuario.text = usuarioValido
                    verify(mockCredencialesUsuarioUI).cambiarUsuario(usuarioValido)
                }

                @Test
                fun a_valor_vacio_llama_metodo_cambiarUsuario_con_valor_correcto()
                {
                    val usuario = ""
                    mockearCambiarUsuario(usuario)
                    controladorLogin.campoUsuario.text = usuario
                    verify(mockCredencialesUsuarioUI).cambiarUsuario(usuario)
                }

                @Test
                fun a_valor_con_espacios_y_tabs_llama_metodo_cambiarUsuario_con_valor_correcto()
                {
                    val usuario = "        "
                    mockearCambiarUsuario(usuario)
                    controladorLogin.campoUsuario.text = usuario
                    verify(mockCredencialesUsuarioUI).cambiarUsuario(usuario)
                }

                @Test
                fun a_valor_con_espacios_al_comienzo_y_final_llama_metodo_cambiarUsuario_sin_hacer_trim()
                {
                    val usuarioValido = "      Usuario        "
                    mockearCambiarUsuario(usuarioValido)
                    controladorLogin.campoUsuario.text = usuarioValido
                    verify(mockCredencialesUsuarioUI).cambiarUsuario(usuarioValido)
                }

                @Test
                fun a_mismo_valor_de_campo_no_llama_metodo_cambiarUsuario_nuevamente()
                {
                    val usuarioValido = "Usuario"
                    mockearCambiarUsuario(usuarioValido)
                    controladorLogin.campoUsuario.text = usuarioValido
                    controladorLogin.campoUsuario.text = usuarioValido
                    verify(mockCredencialesUsuarioUI).cambiarUsuario(usuarioValido)
                }
            }

            @Nested
            inner class AlActivarAccion
            {
                // Probablemente falta probar que use el resultado de intentarLogin para algo o quitar ese retorno
                @Test
                fun llama_intentarLogin()
                {
                    mockearIntentarLogin()
                    controladorLogin.campoUsuario.fireEvent(ActionEvent())
                    verify(mockProcesoLogin).intentarLogin()
                }
            }
        }

        @Nested
        inner class InputContraseña
        {
            @Test
            fun empieza_vacio()
            {
                assertEquals("", controladorLogin.campoContraseña.text)
            }

            @Test
            fun empieza_sin_errores()
            {
                assertTrue(controladorLogin.campoContraseña.validators.all { !it.hasErrors })
            }

            @Test
            fun llama_a_cambiarContraseña_con_valor_vacio_al_inicializar()
            {
                verify(mockCredencialesUsuarioUI).cambiarContraseña(charArrayOf())
            }

            @Nested
            inner class AlCambiarObservable
            {
                @Test
                fun a_valor_valido_se_cambia_el_valor_del_campo_y_llama_cambiarContraseña()
                {
                    val contraseñaValida = "Contraseña".toCharArray()
                    mockearCambiarContraseña(contraseñaValida)
                    sujetoContraseña.onNext(Notification.createOnNext(contraseñaValida))
                    validarEnThreadUI {
                        assertEquals(String(contraseñaValida), controladorLogin.campoContraseña.text)
                        verify(mockCredencialesUsuarioUI).cambiarContraseña(contraseñaValida)
                    }
                }

                @Test
                fun a_valor_de_error_con_excepción_EntidadMalInicializada_se_cambia_el_valor_del_campo_a_vacio_y_activa_validador_con_error_correcto()
                {
                    val errorAEnviar = EntidadMalInicializada("entidad", "campo", "valor", "Descripcion")
                    val contraseñaValida = "Contraseña".toCharArray()
                    mockearCambiarContraseña(contraseñaValida)
                    campoContraseña.text = String(contraseñaValida)

                    sujetoContraseña.onNext(Notification.createOnError(errorAEnviar))
                    validarEnThreadUI {
                        assertEquals("", controladorLogin.campoContraseña.text)

                        val validadorFallido = controladorLogin.campoContraseña.validators.firstOrNull {
                            it.hasErrors && it.message == errorAEnviar.descripcionDeLaRestriccion
                        }
                        assertNotNull(validadorFallido)

                        verify(mockCredencialesUsuarioUI, times(2)).cambiarContraseña(charArrayOf())
                    }
                }

                @Test
                fun a_valor_de_error_con_excepción_diferente_a_EntidadMalInicializada_se_cambia_el_valor_del_campo_a_vacio_y_activa_validador_con_error_correcto()
                {
                    val errorAEnviar = Exception("Error esperado")
                    val contraseñaValida = "Contraseña".toCharArray()
                    mockearCambiarContraseña(contraseñaValida)
                    campoContraseña.text = String(contraseñaValida)

                    sujetoContraseña.onNext(Notification.createOnError(errorAEnviar))

                    validarEnThreadUI {
                        assertEquals("", controladorLogin.campoContraseña.text)

                        val validadorFallido = controladorLogin.campoContraseña.validators.firstOrNull {
                            it.hasErrors && it.message == errorAEnviar.message
                        }
                        assertNotNull(validadorFallido)

                        verify(mockCredencialesUsuarioUI, times(2)).cambiarContraseña(charArrayOf())
                    }
                }

                @Test
                fun a_valor_correcto_despues_de_error_limpia_el_validador()
                {
                    val errorAEnviar = EntidadMalInicializada("entidad", "campo", "valor", "Descripcion")
                    val contraseñaValida = "Contraseña".toCharArray()
                    mockearCambiarContraseña(contraseñaValida)
                    campoContraseña.text = String(contraseñaValida)

                    sujetoContraseña.onNext(Notification.createOnError(errorAEnviar))
                    sujetoContraseña.onNext(Notification.createOnNext(contraseñaValida))

                    validarEnThreadUI {
                        val validadorFallido = controladorLogin.campoContraseña.validators.firstOrNull {
                            it.hasErrors
                        }
                        assertNull(validadorFallido)

                        verify(mockCredencialesUsuarioUI, times(2)).cambiarContraseña(charArrayOf())
                    }
                }
            }

            @Nested
            inner class AlCambiarCampoTexto
            {
                @Test
                fun a_valor_correcto_llama_metodo_cambiarContraseña_con_valor_correcto()
                {
                    val contraseñaValida = "Contraseña".toCharArray()
                    mockearCambiarContraseña(contraseñaValida)
                    controladorLogin.campoContraseña.text = String(contraseñaValida)
                    verify(mockCredencialesUsuarioUI).cambiarContraseña(contraseñaValida)
                }

                @Test
                fun a_valor_vacio_llama_metodo_cambiarContraseña_con_valor_correcto()
                {
                    mockearCambiarContraseña(charArrayOf())
                    controladorLogin.campoContraseña.text = ""
                    verify(mockCredencialesUsuarioUI).cambiarContraseña(charArrayOf())
                }

                @Test
                fun a_valor_con_espacios_y_tabs_llama_metodo_cambiarContraseña_con_valor_correcto()
                {
                    val contraseñaValida = "        ".toCharArray()
                    mockearCambiarContraseña(contraseñaValida)
                    controladorLogin.campoContraseña.text = String(contraseñaValida)
                    verify(mockCredencialesUsuarioUI).cambiarContraseña(contraseñaValida)
                }

                @Test
                fun a_valor_con_espacios_al_comienzo_y_final_llama_metodo_cambiarContraseña_sin_hacer_trim()
                {
                    val contraseñaValida = "       Contraseña       ".toCharArray()
                    mockearCambiarContraseña(contraseñaValida)
                    controladorLogin.campoContraseña.text = String(contraseñaValida)
                    verify(mockCredencialesUsuarioUI).cambiarContraseña(contraseñaValida)
                }

                @Test
                fun a_mismo_valor_de_campo_no_llama_metodo_cambiarContraseña_nuevamente()
                {
                    val contraseñaValida = "Contraseña".toCharArray()
                    mockearCambiarContraseña(contraseñaValida)
                    controladorLogin.campoContraseña.text = String(contraseñaValida)
                    controladorLogin.campoContraseña.text = String(contraseñaValida)
                    verify(mockCredencialesUsuarioUI).cambiarContraseña(contraseñaValida)
                }
            }

            @Nested
            inner class AlActivarAccion
            {
                // Probablemente falta probar que use el resultado de intentarLogin para algo o quitar ese retorno
                @Test
                fun llama_intentarLogin()
                {
                    mockearIntentarLogin()
                    controladorLogin.campoContraseña.fireEvent(ActionEvent())
                    verify(mockProcesoLogin).intentarLogin()
                }
            }
        }
    }

    @Nested
    inner class Proceso
    {
        @Nested
        inner class BotonIniciarSesion
        {
            @Test
            fun empieza_desactivado()
            {
                validarEnThreadUI {
                    assertTrue(controladorLogin.botonIniciarSesion.isDisable)
                }
            }

            @Nested
            inner class AlCambiarObservable
            {
                @Test
                fun al_cambiar_observable_puedeIniciarSesion_a_true_se_activa()
                {
                    sujetoPuedeIniciarSesion.onNext(true)
                    validarEnThreadUI {
                        assertFalse(controladorLogin.botonIniciarSesion.isDisable)
                    }
                }

                @Test
                fun al_cambiar_observable_puedeIniciarSesion_a_true_y_luego_false_se_desactiva_nuevamente()
                {
                    sujetoPuedeIniciarSesion.onNext(true)
                    sujetoPuedeIniciarSesion.onNext(false)
                    validarEnThreadUI {
                        assertTrue(controladorLogin.botonIniciarSesion.isDisable)
                    }
                }
            }

            @Nested
            inner class AlActivarAccion
            {
                // Probablemente falta probar que use el resultado de intentarLogin para algo o quitar ese retorno
                @Test
                fun llama_intentarLogin()
                {
                    mockearIntentarLogin()
                    controladorLogin.botonIniciarSesion.fireEvent(ActionEvent())
                    verify(mockProcesoLogin).intentarLogin()
                }
            }
        }

        @Nested
        inner class DialogoDeEspera
        {
            @Nested
            inner class AlCambiarObservable
            {
                @Test
                fun solo_es_visible_cuando_el_estado_es_INICIANDO_SESION()
                {
                    ProcesoLogin.Estado.values().forEach {
                        sujetoEstado.onNext(it)
                        validarEnThreadUI {
                            assertEquals(
                                    it == ProcesoLogin.Estado.INICIANDO_SESION,
                                    controladorLogin.dialogoDeEspera.isVisible
                                        )
                        }
                    }
                }
            }
        }

        @Nested
        inner class LabelTituloDialogoDeEspera
        {
            @Nested
            inner class AlCambiarObservable
            {
                @Test
                fun cambia_a_mensaje_correcto_al_cambiar_a_INICIANDO_SESION()
                {
                    val mensajeIncorrecto = "Mensaje incorrecto"
                    ProcesoLogin.Estado.values().forEach {
                        controladorLogin.dialogoDeEspera.labelTituloDialogo.text = mensajeIncorrecto
                        sujetoEstado.onNext(it)
                        validarEnThreadUI {
                            val mensajeEsperado = if (it == ProcesoLogin.Estado.INICIANDO_SESION) "Iniciando sesión..." else mensajeIncorrecto
                            assertEquals(mensajeEsperado, controladorLogin.dialogoDeEspera.labelTituloDialogo.text)
                        }
                    }
                }
            }
        }

        @Nested
        inner class LabelError
        {
            @Test
            fun empieza_vacia()
            {
                validarEnThreadUI {
                    assertEquals("", controladorLogin.labelError.text)
                }
            }

            @Nested
            inner class AlCambiarObservable
            {
                @Test
                fun muestra_el_valor_dado_en_observable()
                {
                    sujetoErrorGlobal.onNext(Opcional.De("Error prueba"))
                    validarEnThreadUI {
                        assertEquals("Error prueba", controladorLogin.labelError.text)
                    }
                    sujetoErrorGlobal.onNext(Opcional.Vacio())
                    validarEnThreadUI {
                        assertEquals("", controladorLogin.labelError.text)
                    }
                }
            }
        }

        @Nested
        inner class CambioVista
        {
            @Nested
            inner class AlCambiarObservableDeUsuario
            {
                @Test
                fun llama_cambiador_vista_a_menu_principal()
                {
                    doNothing().`when`(controladorLogin.navegadorFlujo).navigate(cualquiera<Class<*>>())

                    sujetoUsuarioSesionIniciada.onNext(mockConDefaultAnswer(Usuario::class.java))

                    validarEnThreadUI {
                        verify(controladorLogin.navegadorFlujo)
                            .navigate(ControladorMenuPrincipal::class.java)
                    }
                }
            }
        }
    }
}