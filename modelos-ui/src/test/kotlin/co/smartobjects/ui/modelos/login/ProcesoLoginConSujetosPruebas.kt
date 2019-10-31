package co.smartobjects.ui.modelos.login

import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.entidades.usuarios.Rol
import co.smartobjects.entidades.usuarios.Usuario
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.usuarios.UsuariosAPI
import co.smartobjects.red.modelos.ErrorDePeticion
import co.smartobjects.ui.modelos.PruebasModelosRxBase
import co.smartobjects.ui.modelos.ResultadoAccionUI
import co.smartobjects.ui.modelos.mockConDefaultAnswer
import co.smartobjects.ui.modelos.schedulerThreadActual
import co.smartobjects.utilidades.Opcional
import io.reactivex.subjects.BehaviorSubject
import org.junit.jupiter.api.*
import org.mockito.Mockito.*
import java.io.IOException

@DisplayName("ProcesoLoginConSujetos")
internal class ProcesoLoginConSujetosPruebas : PruebasModelosRxBase()
{
    // Para estas pruebas se va a suponer que CredencialesUsuarioUI funciona correctamente
    private val mockCredenciales = mockConDefaultAnswer(CredencialesUsuarioUI::class.java)
    private val mockApiLogin = mockConDefaultAnswer(UsuariosAPI::class.java)


    @Nested
    inner class ConMockDeModeloDeCredenciales
    {
        private val credencialesPruebas = Usuario.CredencialesUsuario("Usuario", "Contraseña".toCharArray())
        private val usuarioPruebas = Usuario(
                Usuario.DatosUsuario(0, "1", "2", "3", true,"","","","","",""),
                setOf(Rol(
                        "a",
                        "b",
                        setOf(PermisoBack(0, "1", PermisoBack.Accion.GET_TODOS))
                         ))
                                            )

        //Nota: No se mockean observables de usuario o contraseña para asegurar que no se usen directamente
        private val sujetoSonCredencialesValidas = BehaviorSubject.create<Boolean>()

        @BeforeEach
        fun mockearSonCredencialesValidas()
        {
            doReturn(sujetoSonCredencialesValidas)
                .`when`(mockCredenciales)
                .sonCredencialesValidas
        }

        private fun mockearDarCredenciales()
        {
            doReturn(credencialesPruebas)
                .`when`(mockCredenciales)
                .aCredencialesUsuario()
        }

        private fun mockearFinalizarProceso()
        {
            doNothing()
                .`when`(mockCredenciales)
                .finalizarProceso()
        }

        private fun mockearRespuestaDeRedExitosa()
        {
            doReturn(RespuestaIndividual.Exitosa(usuarioPruebas))
                .`when`(mockApiLogin)
                .login(credencialesPruebas)
        }

        private fun mockearRespuestaDeRedInvalida(errorEsperado: IllegalStateException)
        {
            doThrow(errorEsperado)
                .`when`(mockApiLogin)
                .login(credencialesPruebas)
        }

        private fun mockearRespuestaDeRedErrorTimeout()
        {
            doReturn(RespuestaIndividual.Error.Timeout<Usuario>())
                .`when`(mockApiLogin)
                .login(credencialesPruebas)
        }

        private fun mockearRespuestaDeRedErrorRed()
        {
            doReturn(RespuestaIndividual.Error.Red<Usuario>(IOException("Error")))
                .`when`(mockApiLogin)
                .login(credencialesPruebas)
        }

        private fun mockearRespuestaDeRedErrorBack(mensajeError: String)
        {
            doReturn(RespuestaIndividual.Error.Back<Usuario>(400, ErrorDePeticion(100, mensajeError)))
                .`when`(mockApiLogin)
                .login(credencialesPruebas)
        }


        @Nested
        inner class ConNFCInicializadoExitosamente
        {
            private val proceso: ProcesoLogin by lazy { ProcesoLoginConSujetos(mockCredenciales, mockApiLogin, schedulerThreadActual) }


            @Nested
            inner class UsuarioSesionIniciada
            {
                private val testUsuarioSesionIniciada by lazy { proceso.usuarioSesionIniciada.test() }

                @Test
                fun empieza_sin_valor()
                {
                    testUsuarioSesionIniciada.assertValueCount(0)
                }
            }

            @Nested
            inner class Estado
            {
                private val testEstado by lazy { proceso.estado.test() }

                @Test
                fun empieza_con_valor_ESPERANDO_CREDENCIALES()
                {
                    testEstado.assertValue(ProcesoLogin.Estado.ESPERANDO_CREDENCIALES)
                    testEstado.assertValueCount(1)
                }
            }

            @Nested
            inner class PuedeIniciarSesion
            {
                private val testPuedeIniciarSesion by lazy { proceso.puedeIniciarSesion.test() }

                @Test
                fun empieza_con_valor_false()
                {
                    testPuedeIniciarSesion.assertValue(false)
                    testPuedeIniciarSesion.assertValueCount(1)
                }

                @Nested
                @Suppress("ClassName")
                inner class EnEstadoESPERANDO_CREDENCIALES
                {
                    @Test
                    fun emite_true_cuando_sonCredencialesValidas_en_credenciales_cambia_a_true()
                    {
                        testPuedeIniciarSesion.assertValue(false)
                        testPuedeIniciarSesion.assertValueCount(1)
                        sujetoSonCredencialesValidas.onNext(true)
                        testPuedeIniciarSesion.assertValueCount(2)
                        testPuedeIniciarSesion.assertValueAt(1, true)
                    }

                    @Test
                    fun emite_false_cuando_sonCredencialesValidas_en_credenciales_cambia_a_false()
                    {
                        testPuedeIniciarSesion.assertValue(false)
                        testPuedeIniciarSesion.assertValueCount(1)
                        sujetoSonCredencialesValidas.onNext(true)
                        testPuedeIniciarSesion.assertValueCount(2)
                        testPuedeIniciarSesion.assertValueAt(1, true)
                        sujetoSonCredencialesValidas.onNext(false)
                        testPuedeIniciarSesion.assertValueCount(3)
                        testPuedeIniciarSesion.assertValueAt(2, false)
                    }
                }
            }

            @Nested
            inner class ErrorGlobal
            {
                private val testErrorGlobal by lazy { proceso.errorGlobal.test() }

                @Test
                fun empieza_con_valor_vacio()
                {
                    testErrorGlobal.assertValue(Opcional.Vacio())
                    testErrorGlobal.assertValueCount(1)
                }
            }

            @Nested
            inner class IntentarLogin
            {
                @Nested
                @Suppress("ClassName")
                inner class EnEstadoESPERANDO_CREDENCIALES
                {
                    @Test
                    fun retorna_MODELO_EN_ESTADO_INVALIDO_cuando_credenciales_no_a_emitido_valor_sobre_sonCredencialesValidas()
                    {
                        Assertions.assertEquals(ResultadoAccionUI.MODELO_EN_ESTADO_INVALIDO, proceso.intentarLogin())
                    }

                    @Test
                    fun retorna_MODELO_EN_ESTADO_INVALIDO_cuando_credenciales_emitio_false_sobre_sonCredencialesValidas()
                    {
                        sujetoSonCredencialesValidas.onNext(false)
                        Assertions.assertEquals(ResultadoAccionUI.MODELO_EN_ESTADO_INVALIDO, proceso.intentarLogin())
                    }

                    @Nested
                    inner class ConObservaleCredencialesValidasTrueCuandoACredencialesUsuarioLanzaExcepcion
                    {
                        @BeforeEach
                        fun emitirSonCredencialesValidasYfallarEnACredencialesUsuario()
                        {
                            doThrow(IllegalStateException("Error"))
                                .`when`(mockCredenciales)
                                .aCredencialesUsuario()
                            sujetoSonCredencialesValidas.onNext(true)
                        }

                        @Test
                        fun retorna_OBSERVABLES_EN_ESTADO_INVALIDO()
                        {
                            Assertions.assertEquals(ResultadoAccionUI.OBSERVABLES_EN_ESTADO_INVALIDO, proceso.intentarLogin())
                        }

                        @Nested
                        inner class ErrorGlobal
                        {
                            private val testErrorGlobal by lazy { proceso.errorGlobal.test() }

                            @Test
                            fun emite_error_correcto()
                            {
                                testErrorGlobal.assertValue(Opcional.Vacio())
                                testErrorGlobal.assertValueCount(1)
                                proceso.intentarLogin()
                                testErrorGlobal.assertValueCount(2)
                                testErrorGlobal.assertValueAt(1, Opcional.De("Credenciales inválidas"))
                            }
                        }
                    }

                    @Nested
                    inner class ConCredencialesValidas
                    {
                        @BeforeEach
                        private fun emitirSonCredencialesValidasYMockear()
                        {
                            mockearDarCredenciales()
                            mockearFinalizarProceso()
                            sujetoSonCredencialesValidas.onNext(true)
                        }

                        @Test
                        fun retorna_ACCION_INICIADA_cuando_api_login_lanza_IllegalStateException()
                        {
                            mockearRespuestaDeRedExitosa()
                            Assertions.assertEquals(ResultadoAccionUI.ACCION_INICIADA, proceso.intentarLogin())
                        }

                        @Test
                        fun retorna_ACCION_INICIADA_cuando_api_login_lanza_IllegalStateExceptionVacia()
                        {
                            val errorEsperado = IllegalStateException()
                            erroresEsperados.add(errorEsperado)
                            mockearRespuestaDeRedInvalida(errorEsperado)
                            Assertions.assertEquals(ResultadoAccionUI.ACCION_INICIADA, proceso.intentarLogin())
                        }

                        @Test
                        fun retorna_ACCION_INICIADA_cuando_api_login_retorna_respuesta_ErrorTimeout()
                        {
                            mockearRespuestaDeRedErrorTimeout()
                            Assertions.assertEquals(ResultadoAccionUI.ACCION_INICIADA, proceso.intentarLogin())
                        }

                        @Test
                        fun retorna_ACCION_INICIADA_cuando_api_login_retorna_respuesta_ErrorRed()
                        {
                            mockearRespuestaDeRedErrorRed()
                            Assertions.assertEquals(ResultadoAccionUI.ACCION_INICIADA, proceso.intentarLogin())
                        }

                        @Test
                        fun retorna_ACCION_INICIADA_cuando_api_login_retorna_respuesta_ErrorBack()
                        {
                            mockearRespuestaDeRedErrorBack("Error de pruebas")
                            Assertions.assertEquals(ResultadoAccionUI.ACCION_INICIADA, proceso.intentarLogin())
                        }

                        @Test
                        fun llama_finalizarProceso_de_credenciales_cuando_api_login_lanza_IllegalStateException()
                        {
                            mockearRespuestaDeRedExitosa()
                            proceso.intentarLogin()
                            verify(mockCredenciales).finalizarProceso()
                        }

                        @Test
                        fun no_llama_finalizarProceso_de_credenciales_cuando_api_login_lanza_IllegalStateExceptionVacia()
                        {
                            val errorEsperado = IllegalStateException()
                            erroresEsperados.add(errorEsperado)
                            mockearRespuestaDeRedInvalida(errorEsperado)
                            proceso.intentarLogin()
                            verify(mockCredenciales, times(0)).finalizarProceso()
                        }

                        @Test
                        fun no_llama_finalizarProceso_de_credenciales_cuando_api_login_retorna_respuesta_ErrorTimeout()
                        {
                            mockearRespuestaDeRedErrorTimeout()
                            proceso.intentarLogin()
                            verify(mockCredenciales, times(0)).finalizarProceso()
                        }

                        @Test
                        fun no_llama_finalizarProceso_de_credenciales_cuando_api_login_retorna_respuesta_ErrorRed()
                        {
                            mockearRespuestaDeRedErrorRed()
                            proceso.intentarLogin()
                            verify(mockCredenciales, times(0)).finalizarProceso()
                        }

                        @Test
                        fun no_llama_finalizarProceso_de_credenciales_cuando_api_login_retorna_respuesta_ErrorBack()
                        {
                            mockearRespuestaDeRedErrorBack("Error de pruebas")
                            proceso.intentarLogin()
                            verify(mockCredenciales, times(0)).finalizarProceso()
                        }

                        @Nested
                        inner class ErrorGlobal
                        {
                            private val testErrorGlobal by lazy { proceso.errorGlobal.test() }

                            @Test
                            fun emite_error_vacio_cuando_api_login_lanza_IllegalStateException()
                            {
                                mockearRespuestaDeRedExitosa()
                                testErrorGlobal.assertValue(Opcional.Vacio())
                                testErrorGlobal.assertValueCount(1)
                                proceso.intentarLogin()
                                testErrorGlobal.assertValueCount(2)
                                testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                            }

                            @Test
                            fun emite_error_vacio_cuando_api_login_lanza_IllegalStateExceptionVacia()
                            {
                                val errorEsperado = IllegalStateException()
                                erroresEsperados.add(errorEsperado)
                                mockearRespuestaDeRedInvalida(errorEsperado)
                                testErrorGlobal.assertValue(Opcional.Vacio())
                                testErrorGlobal.assertValueCount(1)
                                proceso.intentarLogin()
                                testErrorGlobal.assertValueCount(2)
                                testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                            }

                            @Test
                            fun emite_error_vacio_y_luego_error_correcto_cuando_api_login_retorna_respuesta_ErrorTimeout()
                            {
                                mockearRespuestaDeRedErrorTimeout()
                                testErrorGlobal.assertValue(Opcional.Vacio())
                                testErrorGlobal.assertValueCount(1)
                                proceso.intentarLogin()
                                testErrorGlobal.assertValueCount(3)
                                testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                                testErrorGlobal.assertValueAt(2, Opcional.De("Timeout contactando el backend"))
                            }

                            @Test
                            fun emite_error_vacio_y_luego_error_correcto_cuando_api_login_retorna_respuesta_ErrorRed()
                            {
                                mockearRespuestaDeRedErrorRed()
                                testErrorGlobal.assertValue(Opcional.Vacio())
                                testErrorGlobal.assertValueCount(1)
                                proceso.intentarLogin()
                                testErrorGlobal.assertValueCount(3)
                                testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                                testErrorGlobal.assertValueAt(2, Opcional.De("Error contactando el backend"))
                            }

                            @Test
                            fun emite_error_vacio_y_luego_error_correcto_cuando_api_login_retorna_respuesta_ErrorBack()
                            {
                                val mensajeError = "Error de pruebas"
                                mockearRespuestaDeRedErrorBack(mensajeError)
                                testErrorGlobal.assertValue(Opcional.Vacio())
                                testErrorGlobal.assertValueCount(1)
                                proceso.intentarLogin()
                                testErrorGlobal.assertValueCount(3)
                                testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                                testErrorGlobal.assertValueAt(2, Opcional.De("Error en petición: $mensajeError"))
                            }
                        }

                        @Nested
                        inner class UsuarioSesionIniciada
                        {
                            private val testUsuarioSesionIniciada by lazy { proceso.usuarioSesionIniciada.test() }

                            @Test
                            fun emite_valor_correcto_cuando_api_login_lanza_IllegalStateException()
                            {
                                testUsuarioSesionIniciada.assertValueCount(0)
                                mockearRespuestaDeRedExitosa()
                                proceso.intentarLogin()
                                testUsuarioSesionIniciada.assertValueCount(1)
                                testUsuarioSesionIniciada.assertValue(usuarioPruebas)
                            }

                            @Test
                            fun no_emite_valor_cuando_api_login_lanza_IllegalStateExceptionVacia()
                            {
                                testUsuarioSesionIniciada.assertValueCount(0)
                                val errorEsperado = IllegalStateException()
                                erroresEsperados.add(errorEsperado)
                                mockearRespuestaDeRedInvalida(errorEsperado)
                                proceso.intentarLogin()
                                testUsuarioSesionIniciada.assertValueCount(0)
                            }

                            @Test
                            fun no_emite_valor_cuando_api_login_retorna_respuesta_ErrorTimeout()
                            {
                                testUsuarioSesionIniciada.assertValueCount(0)
                                mockearRespuestaDeRedErrorTimeout()
                                proceso.intentarLogin()
                                testUsuarioSesionIniciada.assertValueCount(0)
                            }

                            @Test
                            fun no_emite_valor_cuando_api_login_retorna_respuesta_ErrorRed()
                            {
                                testUsuarioSesionIniciada.assertValueCount(0)
                                mockearRespuestaDeRedErrorRed()
                                proceso.intentarLogin()
                                testUsuarioSesionIniciada.assertValueCount(0)
                            }

                            @Test
                            fun no_emite_valor_cuando_api_login_retorna_respuesta_ErrorBack()
                            {
                                testUsuarioSesionIniciada.assertValueCount(0)
                                mockearRespuestaDeRedErrorBack("Error de pruebas")
                                proceso.intentarLogin()
                                testUsuarioSesionIniciada.assertValueCount(0)
                            }
                        }

                        @Nested
                        inner class Estado
                        {
                            private val testEstado by lazy { proceso.estado.test() }

                            @Test
                            fun cambia_a_INICIANDO_SESION_y_luego_a_SESION_INICIADA_cuando_api_de_login_retorna_Exitosa()
                            {
                                mockearRespuestaDeRedExitosa()
                                testEstado.assertValue(ProcesoLogin.Estado.ESPERANDO_CREDENCIALES)
                                testEstado.assertValueCount(1)
                                proceso.intentarLogin()
                                testEstado.assertValueCount(3)
                                testEstado.assertValueAt(1, ProcesoLogin.Estado.INICIANDO_SESION)
                                testEstado.assertValueAt(2, ProcesoLogin.Estado.SESION_INICIADA)
                            }

                            @Test
                            fun cambia_a_INICIANDO_SESION_cuando_api_de_login_retorna_ExitosaVacia()
                            {
                                val errorEsperado = IllegalStateException()
                                erroresEsperados.add(errorEsperado)
                                mockearRespuestaDeRedInvalida(errorEsperado)
                                testEstado.assertValue(ProcesoLogin.Estado.ESPERANDO_CREDENCIALES)
                                testEstado.assertValueCount(1)
                                proceso.intentarLogin()
                                testEstado.assertValueCount(2)
                                testEstado.assertValueAt(1, ProcesoLogin.Estado.INICIANDO_SESION)
                            }

                            @Test
                            fun cambia_a_INICIANDO_SESION_y_luego_a_ESPERANDO_CREDENCIALES_cuando_api_de_login_retorna_ErrorTimeout()
                            {
                                mockearRespuestaDeRedErrorTimeout()
                                testEstado.assertValue(ProcesoLogin.Estado.ESPERANDO_CREDENCIALES)
                                testEstado.assertValueCount(1)
                                proceso.intentarLogin()
                                testEstado.assertValueCount(3)
                                testEstado.assertValueAt(1, ProcesoLogin.Estado.INICIANDO_SESION)
                                testEstado.assertValueAt(2, ProcesoLogin.Estado.ESPERANDO_CREDENCIALES)
                            }

                            @Test
                            fun cambia_a_INICIANDO_SESION_y_luego_a_ESPERANDO_CREDENCIALES_cuando_api_de_login_retorna_ErrorBack()
                            {
                                mockearRespuestaDeRedErrorBack("Error de pruebas")
                                testEstado.assertValue(ProcesoLogin.Estado.ESPERANDO_CREDENCIALES)
                                testEstado.assertValueCount(1)
                                proceso.intentarLogin()
                                testEstado.assertValueCount(3)
                                testEstado.assertValueAt(1, ProcesoLogin.Estado.INICIANDO_SESION)
                                testEstado.assertValueAt(2, ProcesoLogin.Estado.ESPERANDO_CREDENCIALES)
                            }
                        }
                    }
                }
            }
        }
    }
}