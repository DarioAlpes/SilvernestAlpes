package co.smartobjects.ui.modelos.cerrarsesion

import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.entidades.usuarios.Rol
import co.smartobjects.entidades.usuarios.Usuario
import co.smartobjects.red.clientes.base.RespuestaVacia
import co.smartobjects.red.clientes.usuarios.UsuariosAPI
import co.smartobjects.red.modelos.ErrorDePeticion
import co.smartobjects.ui.modelos.PruebasModelosRxBase
import co.smartobjects.ui.modelos.mockConDefaultAnswer
import co.smartobjects.ui.modelos.verificarUltimoValorEmitido
import co.smartobjects.utilidades.Opcional
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFalse


@DisplayName("ProcesoCerrarSesion")
internal class ProcesoCerrarSesionPruebas : PruebasModelosRxBase()
{
    private val mockApi = mockConDefaultAnswer(UsuariosAPI::class.java)
    private val eventosDeUsuarioActual = BehaviorSubject.createDefault(Opcional.Vacio<Usuario>())

    private val modelo = spy(ProcesoCerrarSesion(mockApi, eventosDeUsuarioActual, Schedulers.trampoline()))


    @Nested
    inner class Inicializacion
    {
        @Test
        fun los_modelos_hijos_estan_vacios()
        {
            assertEquals(modelo.modelosHijos, listOf())
        }
    }

    @Nested
    inner class PruebasDeEstado
    {
        private val observadorDePrueba = modelo.estado.test()

        @Test
        fun al_suscribirse_el_estado_es_esperando()
        {
            observadorDePrueba.assertValuesOnly(ProcesoCerrarSesionUI.Estado.Esperando)
        }

        @Test
        fun si_no_hay_un_usuario_actual_no_hace_nada_y_no_cambia_de_estado()
        {
            modelo.cerrarSesion()

            observadorDePrueba.assertValuesOnly(ProcesoCerrarSesionUI.Estado.Esperando)
        }

        @Nested
        inner class ConUsuarioActual
        {
            private val datosUsuario = Usuario.DatosUsuario(1, "el usuario", "nombre", "email", true,"","","","","","")
            private val usuario =
                    Usuario(
                            datosUsuario,
                            setOf(Rol("asd", "asd", setOf(PermisoBack(1, "asdf", PermisoBack.Accion.GET_UNO))))
                           )


            @BeforeEach
            fun emitirUsuario()
            {
                eventosDeUsuarioActual.onNext(Opcional.De(usuario))
            }

            @Test
            fun si_cambia_el_usuario_no_se_cierra_la_sesion()
            {
                eventosDeUsuarioActual.onNext(Opcional.De(usuario))

                verify(mockApi, times(0)).logout(datosUsuario.usuario)
            }

            @Nested
            inner class EnEstadoEsperandoYSeCierraSesion
            {
                @Test
                fun se_invoca_el_api_para_cerrar_sesion_correctamente()
                {
                    doReturn(RespuestaVacia.Exitosa)
                        .`when`(mockApi)
                        .logout(anyString())

                    modelo.cerrarSesion()

                    verify(mockApi).logout(datosUsuario.usuario)
                }

                @Nested
                inner class Exitosamente
                {
                    @BeforeEach
                    fun cerrarSesion()
                    {
                        doReturn(RespuestaVacia.Exitosa)
                            .`when`(mockApi)
                            .logout(anyString())

                        modelo.cerrarSesion()
                    }

                    @Test
                    fun se_cambia_el_estado_a_cerrando_sesion_y_luego_sesion_cerrada()
                    {
                        observadorDePrueba.assertValuesOnly(
                                ProcesoCerrarSesionUI.Estado.Esperando,
                                ProcesoCerrarSesionUI.Estado.CerrandoSesion,
                                ProcesoCerrarSesionUI.Estado.Resultado.SesionCerrada
                                                           )
                    }

                    @Test
                    fun el_usuario_actual_es_vacio()
                    {
                        assertEquals(Opcional.Vacio(), eventosDeUsuarioActual.value!!)
                    }

                    @Test
                    fun el_usuario_actual_no_se_encuentra_completado()
                    {
                        assertFalse(eventosDeUsuarioActual.hasComplete())
                    }

                    @Test
                    fun con_un_usuario_nuevo_puede_volver_a_cerrar_sesion()
                    {
                        eventosDeUsuarioActual.onNext(Opcional.De(usuario))

                        modelo.cerrarSesion()

                        verify(mockApi, times(2)).logout(datosUsuario.usuario)

                        observadorDePrueba.assertValuesOnly(
                                ProcesoCerrarSesionUI.Estado.Esperando,
                                ProcesoCerrarSesionUI.Estado.CerrandoSesion,
                                ProcesoCerrarSesionUI.Estado.Resultado.SesionCerrada,
                                ProcesoCerrarSesionUI.Estado.CerrandoSesion,
                                ProcesoCerrarSesionUI.Estado.Resultado.SesionCerrada
                                                           )
                    }

                    @Test
                    fun reiniciar_estado_deja_el_estado_en_esperando()
                    {
                        observadorDePrueba.verificarUltimoValorEmitido(ProcesoCerrarSesionUI.Estado.Resultado.SesionCerrada)

                        modelo.reiniciarEstado()

                        observadorDePrueba.verificarUltimoValorEmitido(ProcesoCerrarSesionUI.Estado.Esperando)
                    }

                    @Test
                    fun no_se_finalizo_el_proceso()
                    {
                        verify(modelo, times(0)).finalizarProceso()
                    }
                }

                @Nested
                inner class Timeout
                {
                    private val estadoFinal =
                            ProcesoCerrarSesionUI
                                .Estado
                                .Resultado
                                .ErrorCerrandoSesion("Tiempo de espera al servidor agotado. No se pudo cerrar sesión.")

                    @BeforeEach
                    fun cerrarSesion()
                    {
                        doReturn(RespuestaVacia.Error.Timeout)
                            .`when`(mockApi)
                            .logout(anyString())

                        modelo.cerrarSesion()
                    }

                    @Test
                    fun se_cambia_el_estado_a_cerrando_sesion_y_luego_error_cerrando_sesion()
                    {
                        observadorDePrueba.assertValuesOnly(
                                ProcesoCerrarSesionUI.Estado.Esperando,
                                ProcesoCerrarSesionUI.Estado.CerrandoSesion,
                                estadoFinal
                                                           )
                    }

                    @Test
                    fun el_usuario_actual_es_el_mismo()
                    {
                        assertEquals(Opcional.De(usuario), eventosDeUsuarioActual.value!!)
                    }

                    @Test
                    fun el_usuario_actual_no_se_encuentra_completado()
                    {
                        assertFalse(eventosDeUsuarioActual.hasComplete())
                    }

                    @Test
                    fun puede_volver_intentar_cerrar_sesion()
                    {
                        modelo.cerrarSesion()

                        verify(mockApi, times(2)).logout(datosUsuario.usuario)

                        observadorDePrueba.assertValuesOnly(
                                ProcesoCerrarSesionUI.Estado.Esperando,
                                ProcesoCerrarSesionUI.Estado.CerrandoSesion,
                                estadoFinal,
                                ProcesoCerrarSesionUI.Estado.CerrandoSesion,
                                estadoFinal
                                                           )
                    }

                    @Test
                    fun reiniciar_estado_deja_el_estado_en_esperando()
                    {
                        observadorDePrueba.verificarUltimoValorEmitido(estadoFinal)

                        modelo.reiniciarEstado()

                        observadorDePrueba.verificarUltimoValorEmitido(ProcesoCerrarSesionUI.Estado.Esperando)
                    }

                    @Test
                    fun no_se_finalizo_el_proceso()
                    {
                        verify(modelo, times(0)).finalizarProceso()
                    }
                }

                @Nested
                inner class ErrorRed
                {
                    private val estadoFinal =
                            ProcesoCerrarSesionUI
                                .Estado
                                .Resultado
                                .ErrorCerrandoSesion("Hubo un error en la conexión y no fue posible contactar al servidor")


                    @BeforeEach
                    fun cerrarSesion()
                    {
                        doReturn(RespuestaVacia.Error.Red(IOException("Excepcion")))
                            .`when`(mockApi)
                            .logout(anyString())

                        modelo.cerrarSesion()
                    }

                    @Test
                    fun se_cambia_el_estado_a_cerrando_sesion_y_luego_error_cerrando_sesion()
                    {
                        observadorDePrueba.assertValuesOnly(
                                ProcesoCerrarSesionUI.Estado.Esperando,
                                ProcesoCerrarSesionUI.Estado.CerrandoSesion,
                                estadoFinal
                                                           )
                    }

                    @Test
                    fun el_usuario_actual_es_el_mismo()
                    {
                        assertEquals(Opcional.De(usuario), eventosDeUsuarioActual.value!!)
                    }

                    @Test
                    fun el_usuario_actual_no_se_encuentra_completado()
                    {
                        assertFalse(eventosDeUsuarioActual.hasComplete())
                    }

                    @Test
                    fun puede_volver_intentar_cerrar_sesion()
                    {
                        modelo.cerrarSesion()

                        verify(mockApi, times(2)).logout(datosUsuario.usuario)

                        observadorDePrueba.assertValuesOnly(
                                ProcesoCerrarSesionUI.Estado.Esperando,
                                ProcesoCerrarSesionUI.Estado.CerrandoSesion,
                                estadoFinal,
                                ProcesoCerrarSesionUI.Estado.CerrandoSesion,
                                estadoFinal
                                                           )
                    }

                    @Test
                    fun reiniciar_estado_deja_el_estado_en_esperando()
                    {
                        observadorDePrueba.verificarUltimoValorEmitido(estadoFinal)

                        modelo.reiniciarEstado()

                        observadorDePrueba.verificarUltimoValorEmitido(ProcesoCerrarSesionUI.Estado.Esperando)
                    }

                    @Test
                    fun no_se_finalizo_el_proceso()
                    {
                        verify(modelo, times(0)).finalizarProceso()
                    }
                }

                @Nested
                inner class ErrorBack
                {
                    private val estadoFinal =
                            ProcesoCerrarSesionUI
                                .Estado
                                .Resultado
                                .ErrorCerrandoSesion("La petición realizada es errónea y no pudo ser procesada.\n" +
                                                     "algo")


                    @BeforeEach
                    fun cerrarSesion()
                    {
                        doReturn(RespuestaVacia.Error.Back(400, ErrorDePeticion(1, "algo")))
                            .`when`(mockApi)
                            .logout(anyString())

                        modelo.cerrarSesion()
                    }

                    @Test
                    fun se_cambia_el_estado_a_cerrando_sesion_y_luego_error_cerrando_sesion()
                    {
                        observadorDePrueba.assertValuesOnly(
                                ProcesoCerrarSesionUI.Estado.Esperando,
                                ProcesoCerrarSesionUI.Estado.CerrandoSesion,
                                estadoFinal
                                                           )
                    }

                    @Test
                    fun el_usuario_actual_es_el_mismo()
                    {
                        assertEquals(Opcional.De(usuario), eventosDeUsuarioActual.value!!)
                    }

                    @Test
                    fun el_usuario_actual_no_se_encuentra_completado()
                    {
                        assertFalse(eventosDeUsuarioActual.hasComplete())
                    }

                    @Test
                    fun puede_volver_intentar_cerrar_sesion()
                    {
                        modelo.cerrarSesion()

                        verify(mockApi, times(2)).logout(datosUsuario.usuario)

                        observadorDePrueba.assertValuesOnly(
                                ProcesoCerrarSesionUI.Estado.Esperando,
                                ProcesoCerrarSesionUI.Estado.CerrandoSesion,
                                estadoFinal,
                                ProcesoCerrarSesionUI.Estado.CerrandoSesion,
                                estadoFinal
                                                           )
                    }

                    @Test
                    fun reiniciar_estado_deja_el_estado_en_esperando()
                    {
                        observadorDePrueba.verificarUltimoValorEmitido(estadoFinal)

                        modelo.reiniciarEstado()

                        observadorDePrueba.verificarUltimoValorEmitido(ProcesoCerrarSesionUI.Estado.Esperando)
                    }

                    @Test
                    fun no_se_finalizo_el_proceso()
                    {
                        verify(modelo, times(0)).finalizarProceso()
                    }
                }
            }
        }
    }
}