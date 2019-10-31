package co.smartobjects.ui.modelos.codificacion

import co.smartobjects.entidades.operativas.compras.CreditoFondoConNombre
import co.smartobjects.entidades.operativas.compras.CreditoPaqueteConNombre
import co.smartobjects.entidades.operativas.reservas.Reserva
import co.smartobjects.entidades.operativas.reservas.SesionDeManilla
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.personas.PersonaConGrupoCliente
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.base.RespuestaVacia
import co.smartobjects.red.clientes.operativas.reservas.ReservasAPI
import co.smartobjects.red.modelos.ErrorDePeticion
import co.smartobjects.red.modelos.operativas.TransaccionEntidadTerminadaDTO
import co.smartobjects.red.modelos.operativas.reservas.ReservaDTO
import co.smartobjects.ui.modelos.*
import co.smartobjects.ui.modelos.pagos.ProcesoPagarUI
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import io.reactivex.exceptions.CompositeException
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.threeten.bp.LocalDate
import java.io.IOException

internal class ProcesoCreacionReservaPruebas : PruebasModelosRxBase()
{
    companion object
    {
        private const val ID_CLIENTE = 1L
        private const val USUARIO = "usuario x"
    }

    private val contextoDeSesion = mockConDefaultAnswer(ContextoDeSesion::class.java).also {
        doReturn(ID_CLIENTE).`when`(it).idCliente
        doReturn(USUARIO).`when`(it).nombreDeUsuario
    }

    private val creditosAProcesar =
            List(5) {
                val idPersona = it.toLong() + 1

                ProcesoPagarUI.CreditosACodificarPorPersona(
                        PersonaConGrupoCliente(
                                Persona(
                                        1, idPersona, "Persona $idPersona", Persona.TipoDocumento.CC, "$idPersona$idPersona", Persona.Genero.MASCULINO,
                                        LocalDate.now(ZONA_HORARIA_POR_DEFECTO), Persona.Categoria.A, Persona.Afiliacion.COTIZANTE, false,
                                        null,"empresa","0", Persona.Tipo.NO_AFILIADO
                                       ),
                                null
                                              ),
                        listOf(
                                CreditoFondoConNombre("Crédito Fondo Pagado 10", crearCreditoFondo(10, idPersona, true)),
                                CreditoFondoConNombre("Crédito Fondo Pagado 20", crearCreditoFondo(20, idPersona, true))
                              ),
                        listOf(
                                CreditoPaqueteConNombre("Crédito Paquete Pagado 30", 1, crearCreditoPaquete(30, idPersona, true)),
                                CreditoPaqueteConNombre("Crédito Paquete Pagado 40", 1, crearCreditoPaquete(40, idPersona, true))
                              )
                                                           )
            }

    private val apiReservas = mockConDefaultAnswer(ReservasAPI::class.java)

    private val reservaACrearEsperada =
            Reserva(
                    ID_CLIENTE,
                    USUARIO,
                    creditosAProcesar.map {
                        SesionDeManilla(
                                ID_CLIENTE,
                                null,
                                it.personaConGrupoCliente.persona.id!!,
                                null,
                                null,
                                null,
                                it.creditosFondoTotales.asSequence().map { it.id!! }.toSet()
                                       )
                    }
                   )

    private val reservaConNumeroEsperada = reservaACrearEsperada.copiar(creacionTerminada = true, numeroDeReserva = 1234)

    @Nested
    inner class AlInstanciar
    {
        @Test
        fun estado_emite_sin_crear()
        {
            val modelo = ProcesoCreacionReserva(contextoDeSesion, creditosAProcesar, apiReservas, Schedulers.trampoline())

            modelo.estado.test().assertValue(ProcesoCreacionReservaUI.Estado.SIN_CREAR)
        }

        @Test
        fun el_mensaje_de_error_no_emite()
        {
            val modelo = ProcesoCreacionReserva(contextoDeSesion, creditosAProcesar, apiReservas, Schedulers.trampoline())

            modelo.mensajesDeError.test().assertEmpty()
        }

        @Test
        fun reserva_con_numero_asignado_no_emite()
        {
            val modelo = ProcesoCreacionReserva(contextoDeSesion, creditosAProcesar, apiReservas, Schedulers.trampoline())

            modelo.reservaConNumeroAsignado.test().assertEmpty()
        }
    }

    @Nested
    inner class AlCrearReserva
    {
        @Nested
        inner class BackendRetornaExitoso
        {
            private lateinit var modelo: ProcesoCreacionReservaUI

            @BeforeEach
            fun mockearDependencias()
            {
                doReturn(RespuestaIndividual.Exitosa(reservaACrearEsperada))
                    .`when`(apiReservas)
                    .actualizar(anyString(), cualquiera())
                doReturn(RespuestaVacia.Exitosa).`when`(apiReservas).actualizarCampos(anyString(), cualquiera())
                doReturn(RespuestaIndividual.Exitosa(reservaConNumeroEsperada)).`when`(apiReservas).consultar(anyString())

                modelo = ProcesoCreacionReserva(contextoDeSesion, creditosAProcesar, apiReservas, Schedulers.trampoline())
            }


            @Test
            fun cambia_el_estado_a_creando()
            {
                val observadorEstado = modelo.estado.test()

                modelo.intentarCrearActivarYConsultarReserva()

                observadorEstado.assertValueAt(1, ProcesoCreacionReservaUI.Estado.CREANDO)
            }

            @Test
            fun reinicia_el_mensaje_de_error()
            {
                val observadorEstado = modelo.mensajesDeError.test()

                modelo.intentarCrearActivarYConsultarReserva()

                observadorEstado.assertValue("")
            }

            @Test
            fun solo_invoca_el_endpoint_de_actualizar_reserva_y_luego_el_de_activar()
            {
                modelo.intentarCrearActivarYConsultarReserva()

                with(inOrder(apiReservas))
                {
                    verify(apiReservas).actualizar(anyString(), cualquiera())
                    verify(apiReservas).actualizarCampos(anyString(), eqParaKotlin(TransaccionEntidadTerminadaDTO(true)))
                }
            }
        }

        @Nested
        inner class BackendRetornaVacio
        {
            private lateinit var modelo: ProcesoCreacionReservaUI

            @BeforeEach
            fun mockearDependencias()
            {
                doReturn(RespuestaIndividual.Vacia<Reserva>()).`when`(apiReservas).actualizar(anyString(), cualquiera())

                modelo = ProcesoCreacionReserva(contextoDeSesion, creditosAProcesar, apiReservas, Schedulers.trampoline())

                erroresEsperados.add(CompositeException(IllegalStateException()))
            }


            @Test
            fun cambia_el_estado_a_creando_y_vuelve_a_sin_crear()
            {
                val observadorEstado = modelo.estado.test()

                modelo.intentarCrearActivarYConsultarReserva()

                observadorEstado.assertValues(
                        ProcesoCreacionReservaUI.Estado.SIN_CREAR,
                        ProcesoCreacionReservaUI.Estado.CREANDO,
                        ProcesoCreacionReservaUI.Estado.SIN_CREAR
                                             )
            }

            @Test
            fun reinicia_el_mensaje_de_error_y_no_emite_error()
            {
                val observadorEstado = modelo.mensajesDeError.test()

                modelo.intentarCrearActivarYConsultarReserva()

                observadorEstado.assertValue("")
            }

            @Test
            fun solo_invoca_el_endpoint_de_actualizar_reserva()
            {
                modelo.intentarCrearActivarYConsultarReserva()

                with(inOrder(apiReservas))
                {
                    verify(apiReservas).actualizar(anyString(), cualquiera())
                    verifyNoMoreInteractions()
                }
            }

            @Test
            fun si_fallo_no_permite_reintentar_creacion()
            {
                modelo.intentarCrearActivarYConsultarReserva()

                modelo.intentarCrearActivarYConsultarReserva()

                with(inOrder(apiReservas))
                {
                    verify(apiReservas).actualizar(anyString(), cualquiera())
                    verifyNoMoreInteractions()
                }
            }

            @Test
            fun reserva_con_numero_asignado_no_emite()
            {
                modelo.intentarCrearActivarYConsultarReserva()

                modelo.reservaConNumeroAsignado.test().assertEmpty()
            }
        }

        @Nested
        inner class BackendRetornaErrorTimeout
        {
            private lateinit var modelo: ProcesoCreacionReservaUI

            @BeforeEach
            fun mockearDependencias()
            {
                doReturn(RespuestaIndividual.Error.Timeout<Reserva>()).`when`(apiReservas).actualizar(anyString(), cualquiera())

                modelo = ProcesoCreacionReserva(contextoDeSesion, creditosAProcesar, apiReservas, Schedulers.trampoline())
            }


            @Test
            fun cambia_el_estado_a_creando_y_vuelve_a_sin_crear()
            {
                val observadorEstado = modelo.estado.test()

                modelo.intentarCrearActivarYConsultarReserva()

                observadorEstado.assertValues(
                        ProcesoCreacionReservaUI.Estado.SIN_CREAR,
                        ProcesoCreacionReservaUI.Estado.CREANDO,
                        ProcesoCreacionReservaUI.Estado.SIN_CREAR
                                             )
            }

            @Test
            fun reinicia_el_mensaje_de_error_y_luego_emite_error_diciendo_que_hubo_un_timeout()
            {
                val observadorEstado = modelo.mensajesDeError.test()

                modelo.intentarCrearActivarYConsultarReserva()

                observadorEstado.assertValues("", "Timeout contactando el backend")
            }

            @Test
            fun solo_invoca_el_endpoint_de_actualizar_reserva()
            {
                modelo.intentarCrearActivarYConsultarReserva()

                with(inOrder(apiReservas))
                {
                    verify(apiReservas).actualizar(anyString(), cualquiera())
                    verifyNoMoreInteractions()
                }
            }

            @Test
            fun reserva_con_numero_asignado_no_emite()
            {
                modelo.intentarCrearActivarYConsultarReserva()

                modelo.reservaConNumeroAsignado.test().assertEmpty()
            }

            @Nested
            inner class ReintentandoCreacion
            {
                private lateinit var observadorEstado: TestObserver<ProcesoCreacionReservaUI.Estado>
                private lateinit var observadorDeMensajesDeError: TestObserver<String>

                @BeforeEach
                fun primerIntento()
                {
                    observadorEstado = modelo.estado.test()
                    observadorDeMensajesDeError = modelo.mensajesDeError.test()
                    modelo.intentarCrearActivarYConsultarReserva()
                }

                @Test
                fun emite_los_mismos_estados_de_antes()
                {
                    modelo.intentarCrearActivarYConsultarReserva()

                    observadorEstado.assertValues(
                            ProcesoCreacionReservaUI.Estado.SIN_CREAR,
                            ProcesoCreacionReservaUI.Estado.CREANDO,
                            ProcesoCreacionReservaUI.Estado.SIN_CREAR,
                            ProcesoCreacionReservaUI.Estado.CREANDO,
                            ProcesoCreacionReservaUI.Estado.SIN_CREAR
                                                 )
                }

                @Test
                fun vuelve_a_emitir_los_mismos_mensajes()
                {
                    modelo.intentarCrearActivarYConsultarReserva()

                    observadorDeMensajesDeError.assertValues(
                            "",
                            "Timeout contactando el backend",
                            "",
                            "Timeout contactando el backend"
                                                            )
                }

                @Test
                fun por_cada_intento_invoca_solo_el_endpoint_de_crear_reserva()
                {
                    modelo.intentarCrearActivarYConsultarReserva()

                    with(inOrder(apiReservas))
                    {
                        verify(apiReservas, times(2)).actualizar(anyString(), cualquiera())
                        verifyNoMoreInteractions()
                    }
                }
            }
        }

        @Nested
        inner class BackendRetornaErrorRed
        {
            private lateinit var modelo: ProcesoCreacionReservaUI

            @BeforeEach
            fun mockearDependencias()
            {
                doReturn(RespuestaIndividual.Error.Red<Reserva>(IOException()))
                    .`when`(apiReservas)
                    .actualizar(anyString(), cualquiera())

                modelo = ProcesoCreacionReserva(contextoDeSesion, creditosAProcesar, apiReservas, Schedulers.trampoline())
            }


            @Test
            fun cambia_el_estado_a_creando_y_vuelve_a_sin_crear()
            {
                val observadorEstado = modelo.estado.test()

                modelo.intentarCrearActivarYConsultarReserva()

                observadorEstado.assertValues(
                        ProcesoCreacionReservaUI.Estado.SIN_CREAR,
                        ProcesoCreacionReservaUI.Estado.CREANDO,
                        ProcesoCreacionReservaUI.Estado.SIN_CREAR
                                             )
            }

            @Test
            fun reinicia_el_mensaje_de_error_y_luego_emite_error_diciendo_que_hubo_un_error_en_la_conexion()
            {
                val observadorEstado = modelo.mensajesDeError.test()

                modelo.intentarCrearActivarYConsultarReserva()

                observadorEstado.assertValues("", "Hubo un error en la conexión y no fue posible contactar al servidor")
            }

            @Test
            fun solo_invoca_el_endpoint_de_actualizar_reserva()
            {
                modelo.intentarCrearActivarYConsultarReserva()

                with(inOrder(apiReservas))
                {
                    verify(apiReservas).actualizar(anyString(), cualquiera())
                    verifyNoMoreInteractions()
                }
            }

            @Test
            fun reserva_con_numero_asignado_no_emite()
            {
                modelo.intentarCrearActivarYConsultarReserva()

                modelo.reservaConNumeroAsignado.test().assertEmpty()
            }

            @Nested
            inner class ReintentandoCreacion
            {
                private lateinit var observadorEstado: TestObserver<ProcesoCreacionReservaUI.Estado>
                private lateinit var observadorDeMensajesDeError: TestObserver<String>

                @BeforeEach
                fun primerIntento()
                {
                    observadorEstado = modelo.estado.test()
                    observadorDeMensajesDeError = modelo.mensajesDeError.test()
                    modelo.intentarCrearActivarYConsultarReserva()
                }

                @Test
                fun emite_los_mismos_estados_de_antes()
                {
                    modelo.intentarCrearActivarYConsultarReserva()

                    observadorEstado.assertValues(
                            ProcesoCreacionReservaUI.Estado.SIN_CREAR,
                            ProcesoCreacionReservaUI.Estado.CREANDO,
                            ProcesoCreacionReservaUI.Estado.SIN_CREAR,
                            ProcesoCreacionReservaUI.Estado.CREANDO,
                            ProcesoCreacionReservaUI.Estado.SIN_CREAR
                                                 )
                }

                @Test
                fun vuelve_a_emitir_los_mismos_mensajes()
                {
                    modelo.intentarCrearActivarYConsultarReserva()

                    observadorDeMensajesDeError.assertValues(
                            "",
                            "Hubo un error en la conexión y no fue posible contactar al servidor",
                            "",
                            "Hubo un error en la conexión y no fue posible contactar al servidor"
                                                            )
                }

                @Test
                fun por_cada_intento_invoca_solo_el_endpoint_de_crear_reserva()
                {
                    modelo.intentarCrearActivarYConsultarReserva()

                    with(inOrder(apiReservas))
                    {
                        verify(apiReservas, times(2)).actualizar(anyString(), cualquiera())
                        verifyNoMoreInteractions()
                    }
                }
            }
        }

        @Nested
        inner class BackendRetornaErrorBack
        {
            @Nested
            inner class ConErrorReservaYaTerminada
            {
                private lateinit var modelo: ProcesoCreacionReservaUI

                @BeforeEach
                fun mockearDependencias()
                {
                    val errorDePeticion = ErrorDePeticion(ReservaDTO.CodigosError.ESTA_MARCADA_CON_CREACION_TERMINADA, "no importa")
                    doReturn(RespuestaIndividual.Error.Back<Reserva>(400, errorDePeticion))
                        .`when`(apiReservas)
                        .actualizar(anyString(), cualquiera())

                    doReturn(RespuestaIndividual.Exitosa(reservaConNumeroEsperada)).`when`(apiReservas).consultar(anyString())

                    modelo = ProcesoCreacionReserva(contextoDeSesion, creditosAProcesar, apiReservas, Schedulers.trampoline())
                }


                @Test
                fun cambia_el_estado_a_creando_y_luego_activado()
                {
                    val observadorEstado = modelo.estado.test()

                    modelo.intentarCrearActivarYConsultarReserva()

                    observadorEstado.assertValueAt(0, ProcesoCreacionReservaUI.Estado.SIN_CREAR)
                    observadorEstado.assertValueAt(1, ProcesoCreacionReservaUI.Estado.CREANDO)
                    observadorEstado.assertValueAt(2, ProcesoCreacionReservaUI.Estado.CREADA)
                    observadorEstado.assertValueAt(3, ProcesoCreacionReservaUI.Estado.ACTIVADA)
                }

                @Test
                fun reinicia_el_mensaje_de_error_y_no_emite_nada()
                {
                    val observadorEstado = modelo.mensajesDeError.test()

                    modelo.intentarCrearActivarYConsultarReserva()

                    observadorEstado.assertValues("")
                }

                @Test
                fun invoca_el_endpoint_de_actualizar_reserva_y_luego_el_de_consulta()
                {
                    modelo.intentarCrearActivarYConsultarReserva()

                    with(inOrder(apiReservas))
                    {
                        verify(apiReservas).actualizar(anyString(), cualquiera())
                        verify(apiReservas).consultar(anyString())
                        verifyNoMoreInteractions()
                    }
                }
            }

            @Nested
            inner class ConErrorSesionesDeManillaInvalidas
            {
                private lateinit var modelo: ProcesoCreacionReservaUI

                @BeforeEach
                fun mockearDependencias()
                {
                    val errorDePeticion = ErrorDePeticion(ReservaDTO.CodigosError.SESIONES_DE_MANILLAS_INVALIDAS, "no importa")
                    doReturn(RespuestaIndividual.Error.Back<Reserva>(400, errorDePeticion))
                        .`when`(apiReservas)
                        .actualizar(anyString(), cualquiera())

                    doReturn(RespuestaIndividual.Exitosa(reservaConNumeroEsperada)).`when`(apiReservas).consultar(anyString())

                    modelo = ProcesoCreacionReserva(contextoDeSesion, creditosAProcesar, apiReservas, Schedulers.trampoline())
                }


                @Test
                fun cambia_el_estado_a_creando_y_vuelve_a_sin_crear()
                {
                    val observadorEstado = modelo.estado.test()

                    modelo.intentarCrearActivarYConsultarReserva()

                    observadorEstado.assertValueAt(0, ProcesoCreacionReservaUI.Estado.SIN_CREAR)
                    observadorEstado.assertValueAt(1, ProcesoCreacionReservaUI.Estado.CREANDO)
                    observadorEstado.assertValueAt(2, ProcesoCreacionReservaUI.Estado.SIN_CREAR)
                }

                @Test
                fun reinicia_el_mensaje_de_error_y_luego_emite_error_diciendo_que_las_manillas_se_encuentran_en_estado_inconcistente()
                {
                    val observadorEstado = modelo.mensajesDeError.test()

                    modelo.intentarCrearActivarYConsultarReserva()

                    observadorEstado.assertValues("", "El estado de las manillas es inconsistente, no se puede crear la reserva")
                }

                @Test
                fun solo_invoca_el_endpoint_de_actualizar_reserva()
                {
                    modelo.intentarCrearActivarYConsultarReserva()

                    with(inOrder(apiReservas))
                    {
                        verify(apiReservas).actualizar(anyString(), cualquiera())
                        verifyNoMoreInteractions()
                    }
                }

                @Test
                fun reserva_con_numero_asignado_no_emite()
                {
                    modelo.intentarCrearActivarYConsultarReserva()

                    modelo.reservaConNumeroAsignado.test().assertEmpty()
                }

                @Nested
                inner class ReintentandoCreacion
                {
                    private lateinit var observadorEstado: TestObserver<ProcesoCreacionReservaUI.Estado>
                    private lateinit var observadorDeMensajesDeError: TestObserver<String>

                    @BeforeEach
                    fun primerIntento()
                    {
                        observadorEstado = modelo.estado.test()
                        observadorDeMensajesDeError = modelo.mensajesDeError.test()
                        modelo.intentarCrearActivarYConsultarReserva()
                    }

                    @Test
                    fun emite_los_mismos_estados_de_antes()
                    {
                        modelo.intentarCrearActivarYConsultarReserva()

                        observadorEstado.assertValues(
                                ProcesoCreacionReservaUI.Estado.SIN_CREAR,
                                ProcesoCreacionReservaUI.Estado.CREANDO,
                                ProcesoCreacionReservaUI.Estado.SIN_CREAR,
                                ProcesoCreacionReservaUI.Estado.CREANDO,
                                ProcesoCreacionReservaUI.Estado.SIN_CREAR
                                                     )
                    }

                    @Test
                    fun vuelve_a_emitir_los_mismos_mensajes()
                    {
                        modelo.intentarCrearActivarYConsultarReserva()

                        observadorDeMensajesDeError.assertValues(
                                "",
                                "El estado de las manillas es inconsistente, no se puede crear la reserva",
                                "",
                                "El estado de las manillas es inconsistente, no se puede crear la reserva"
                                                                )
                    }

                    @Test
                    fun por_cada_intento_invoca_solo_el_endpoint_de_crear_reserva()
                    {
                        modelo.intentarCrearActivarYConsultarReserva()

                        with(inOrder(apiReservas))
                        {
                            verify(apiReservas, times(2)).actualizar(anyString(), cualquiera())
                            verifyNoMoreInteractions()
                        }
                    }
                }
            }

            @Nested
            inner class ConErrorDesconocido
            {
                private val mensajeDeError = "error reicbido esperado"
                private lateinit var modelo: ProcesoCreacionReservaUI

                @BeforeEach
                fun mockearDependencias()
                {
                    val errorDePeticion = ErrorDePeticion(-1, mensajeDeError)
                    doReturn(RespuestaIndividual.Error.Back<Reserva>(400, errorDePeticion))
                        .`when`(apiReservas)
                        .actualizar(anyString(), cualquiera())

                    doReturn(RespuestaIndividual.Exitosa(reservaConNumeroEsperada)).`when`(apiReservas).consultar(anyString())

                    modelo = ProcesoCreacionReserva(contextoDeSesion, creditosAProcesar, apiReservas, Schedulers.trampoline())
                }


                @Test
                fun cambia_el_estado_a_creando_y_vuelve_a_sin_crear()
                {
                    val observadorEstado = modelo.estado.test()

                    modelo.intentarCrearActivarYConsultarReserva()

                    observadorEstado.assertValueAt(0, ProcesoCreacionReservaUI.Estado.SIN_CREAR)
                    observadorEstado.assertValueAt(1, ProcesoCreacionReservaUI.Estado.CREANDO)
                    observadorEstado.assertValueAt(2, ProcesoCreacionReservaUI.Estado.SIN_CREAR)
                }

                @Test
                fun reinicia_el_mensaje_de_error_y_luego_emite_error_mostrando_el_error_recibido()
                {
                    val observadorEstado = modelo.mensajesDeError.test()

                    modelo.intentarCrearActivarYConsultarReserva()

                    observadorEstado.assertValues("", "Error en petición: $mensajeDeError")
                }

                @Test
                fun solo_invoca_el_endpoint_de_actualizar_reserva()
                {
                    modelo.intentarCrearActivarYConsultarReserva()

                    with(inOrder(apiReservas))
                    {
                        verify(apiReservas).actualizar(anyString(), cualquiera())
                        verifyNoMoreInteractions()
                    }
                }

                @Test
                fun reserva_con_numero_asignado_no_emite()
                {
                    modelo.intentarCrearActivarYConsultarReserva()

                    modelo.reservaConNumeroAsignado.test().assertEmpty()
                }

                @Nested
                inner class ReintentandoCreacion
                {
                    private lateinit var observadorEstado: TestObserver<ProcesoCreacionReservaUI.Estado>
                    private lateinit var observadorDeMensajesDeError: TestObserver<String>

                    @BeforeEach
                    fun primerIntento()
                    {
                        observadorEstado = modelo.estado.test()
                        observadorDeMensajesDeError = modelo.mensajesDeError.test()
                        modelo.intentarCrearActivarYConsultarReserva()
                    }

                    @Test
                    fun emite_los_mismos_estados_de_antes()
                    {
                        modelo.intentarCrearActivarYConsultarReserva()

                        observadorEstado.assertValues(
                                ProcesoCreacionReservaUI.Estado.SIN_CREAR,
                                ProcesoCreacionReservaUI.Estado.CREANDO,
                                ProcesoCreacionReservaUI.Estado.SIN_CREAR,
                                ProcesoCreacionReservaUI.Estado.CREANDO,
                                ProcesoCreacionReservaUI.Estado.SIN_CREAR
                                                     )
                    }

                    @Test
                    fun vuelve_a_emitir_los_mismos_mensajes()
                    {
                        modelo.intentarCrearActivarYConsultarReserva()

                        observadorDeMensajesDeError.assertValues(
                                "",
                                "Error en petición: $mensajeDeError",
                                "",
                                "Error en petición: $mensajeDeError"
                                                                )
                    }

                    @Test
                    fun por_cada_intento_invoca_solo_el_endpoint_de_crear_reserva()
                    {
                        modelo.intentarCrearActivarYConsultarReserva()

                        with(inOrder(apiReservas))
                        {
                            verify(apiReservas, times(2)).actualizar(anyString(), cualquiera())
                            verifyNoMoreInteractions()
                        }
                    }
                }
            }
        }
    }

    @Nested
    inner class AlActivarReserva
    {
        @BeforeEach
        fun crearReserva()
        {
            doReturn(RespuestaIndividual.Exitosa(reservaACrearEsperada))
                .`when`(apiReservas)
                .actualizar(anyString(), cualquiera())
        }

        @Nested
        inner class BackendRetornaExitoso
        {
            private lateinit var modelo: ProcesoCreacionReservaUI

            @BeforeEach
            fun mockearDependencias()
            {
                doReturn(RespuestaVacia.Exitosa).`when`(apiReservas).actualizarCampos(anyString(), cualquiera())
                doReturn(RespuestaIndividual.Exitosa(reservaConNumeroEsperada)).`when`(apiReservas).consultar(anyString())

                modelo = ProcesoCreacionReserva(contextoDeSesion, creditosAProcesar, apiReservas, Schedulers.trampoline())
            }


            @Test
            fun cambia_el_estado_de_creada_a_activando_y_luego_activada()
            {
                val observadorEstado = modelo.estado.test()

                modelo.intentarCrearActivarYConsultarReserva()

                observadorEstado.assertValueAt(2, ProcesoCreacionReservaUI.Estado.CREADA)
                observadorEstado.assertValueAt(3, ProcesoCreacionReservaUI.Estado.ACTIVANDO)
                observadorEstado.assertValueAt(4, ProcesoCreacionReservaUI.Estado.ACTIVADA)
            }

            @Test
            fun reinicia_el_mensaje_de_error()
            {
                val observadorEstado = modelo.mensajesDeError.test()

                modelo.intentarCrearActivarYConsultarReserva()

                observadorEstado.assertValue("")
            }

            @Test
            fun invoca_el_endpoint_de_actualizar_reserva_el_de_activar_y_finalmente_el_de_consultar()
            {
                modelo.intentarCrearActivarYConsultarReserva()

                with(inOrder(apiReservas))
                {
                    verify(apiReservas).actualizar(anyString(), cualquiera())
                    verify(apiReservas).actualizarCampos(anyString(), eqParaKotlin(TransaccionEntidadTerminadaDTO(true)))
                    verify(apiReservas).consultar(anyString())
                    verifyNoMoreInteractions()
                }
            }
        }

        @Nested
        inner class BackendRetornaErrorTimeout
        {
            private lateinit var modelo: ProcesoCreacionReservaUI

            @BeforeEach
            fun mockearDependencias()
            {
                doReturn(RespuestaVacia.Error.Timeout).`when`(apiReservas).actualizarCampos(anyString(), cualquiera())

                modelo = ProcesoCreacionReserva(contextoDeSesion, creditosAProcesar, apiReservas, Schedulers.trampoline())
            }


            @Test
            fun cambia_el_estado_de_creada_a_activando_y_luego_activada()
            {
                val observadorEstado = modelo.estado.test()

                modelo.intentarCrearActivarYConsultarReserva()

                observadorEstado.assertValueAt(2, ProcesoCreacionReservaUI.Estado.CREADA)
                observadorEstado.assertValueAt(3, ProcesoCreacionReservaUI.Estado.ACTIVANDO)
                observadorEstado.assertValueAt(4, ProcesoCreacionReservaUI.Estado.CREADA)
            }

            @Test
            fun reinicia_el_mensaje_de_error_y_luego_emite_error_diciendo_que_hubo_un_timeout()
            {
                val observadorEstado = modelo.mensajesDeError.test()

                modelo.intentarCrearActivarYConsultarReserva()

                observadorEstado.assertValues("", "Timeout contactando el backend")
            }

            @Test
            fun solo_invoca_los_endpoints_de_actualizar_y_activar_reserva()
            {
                modelo.intentarCrearActivarYConsultarReserva()

                with(inOrder(apiReservas))
                {
                    verify(apiReservas).actualizar(anyString(), cualquiera())
                    verify(apiReservas).actualizarCampos(anyString(), eqParaKotlin(TransaccionEntidadTerminadaDTO(true)))
                    verifyNoMoreInteractions()
                }
            }

            @Test
            fun reserva_con_numero_asignado_no_emite()
            {
                modelo.intentarCrearActivarYConsultarReserva()

                modelo.reservaConNumeroAsignado.test().assertEmpty()
            }

            @Nested
            inner class ReintentandoActivacion
            {
                private lateinit var observadorEstado: TestObserver<ProcesoCreacionReservaUI.Estado>
                private lateinit var observadorDeMensajesDeError: TestObserver<String>

                @BeforeEach
                fun primerIntento()
                {
                    observadorEstado = modelo.estado.test()
                    observadorDeMensajesDeError = modelo.mensajesDeError.test()
                    modelo.intentarCrearActivarYConsultarReserva()
                }

                @Test
                fun emite_los_mismos_estados_de_antes()
                {
                    modelo.intentarCrearActivarYConsultarReserva()

                    observadorEstado.assertValues(
                            ProcesoCreacionReservaUI.Estado.SIN_CREAR,
                            ProcesoCreacionReservaUI.Estado.CREANDO,
                            ProcesoCreacionReservaUI.Estado.CREADA,
                            ProcesoCreacionReservaUI.Estado.ACTIVANDO,
                            ProcesoCreacionReservaUI.Estado.CREADA,
                            ProcesoCreacionReservaUI.Estado.ACTIVANDO,
                            ProcesoCreacionReservaUI.Estado.CREADA
                                                 )
                }

                @Test
                fun vuelve_a_emitir_los_mismos_mensajes()
                {
                    modelo.intentarCrearActivarYConsultarReserva()

                    observadorDeMensajesDeError.assertValues(
                            "",
                            "Timeout contactando el backend",
                            "",
                            "Timeout contactando el backend"
                                                            )
                }

                @Test
                fun llama_endpoint_crear_reserva_una_vez_y_por_cada_intento_el_endpoint_de_activar_reserva()
                {
                    modelo.intentarCrearActivarYConsultarReserva()

                    with(inOrder(apiReservas))
                    {
                        verify(apiReservas).actualizar(anyString(), cualquiera())
                        verify(apiReservas, times(2)).actualizarCampos(anyString(), eqParaKotlin(TransaccionEntidadTerminadaDTO(true)))
                        verifyNoMoreInteractions()
                    }
                }
            }
        }

        @Nested
        inner class BackendRetornaErrorDeRed
        {
            private lateinit var modelo: ProcesoCreacionReservaUI

            @BeforeEach
            fun mockearDependencias()
            {
                doReturn(RespuestaVacia.Error.Red(IOException())).`when`(apiReservas).actualizarCampos(anyString(), cualquiera())

                modelo = ProcesoCreacionReserva(contextoDeSesion, creditosAProcesar, apiReservas, Schedulers.trampoline())
            }


            @Test
            fun cambia_el_estado_de_creada_a_activando_y_luego_activada()
            {
                val observadorEstado = modelo.estado.test()

                modelo.intentarCrearActivarYConsultarReserva()

                observadorEstado.assertValueAt(2, ProcesoCreacionReservaUI.Estado.CREADA)
                observadorEstado.assertValueAt(3, ProcesoCreacionReservaUI.Estado.ACTIVANDO)
                observadorEstado.assertValueAt(4, ProcesoCreacionReservaUI.Estado.CREADA)
            }

            @Test
            fun reinicia_el_mensaje_de_error_y_luego_emite_error_diciendo_que_hubo_un_error_en_la_conexion()
            {
                val observadorEstado = modelo.mensajesDeError.test()

                modelo.intentarCrearActivarYConsultarReserva()

                observadorEstado.assertValues("", "Hubo un error en la conexión y no fue posible contactar al servidor")
            }

            @Test
            fun solo_invoca_los_endpoints_de_actualizar_y_activar_reserva()
            {
                modelo.intentarCrearActivarYConsultarReserva()

                with(inOrder(apiReservas))
                {
                    verify(apiReservas).actualizar(anyString(), cualquiera())
                    verify(apiReservas).actualizarCampos(anyString(), eqParaKotlin(TransaccionEntidadTerminadaDTO(true)))
                    verifyNoMoreInteractions()
                }
            }

            @Test
            fun reserva_con_numero_asignado_no_emite()
            {
                modelo.intentarCrearActivarYConsultarReserva()

                modelo.reservaConNumeroAsignado.test().assertEmpty()
            }

            @Nested
            inner class ReintentandoActivacion
            {
                private lateinit var observadorEstado: TestObserver<ProcesoCreacionReservaUI.Estado>
                private lateinit var observadorDeMensajesDeError: TestObserver<String>

                @BeforeEach
                fun primerIntento()
                {
                    observadorEstado = modelo.estado.test()
                    observadorDeMensajesDeError = modelo.mensajesDeError.test()
                    modelo.intentarCrearActivarYConsultarReserva()
                }

                @Test
                fun emite_los_mismos_estados_de_antes()
                {
                    modelo.intentarCrearActivarYConsultarReserva()

                    observadorEstado.assertValues(
                            ProcesoCreacionReservaUI.Estado.SIN_CREAR,
                            ProcesoCreacionReservaUI.Estado.CREANDO,
                            ProcesoCreacionReservaUI.Estado.CREADA,
                            ProcesoCreacionReservaUI.Estado.ACTIVANDO,
                            ProcesoCreacionReservaUI.Estado.CREADA,
                            ProcesoCreacionReservaUI.Estado.ACTIVANDO,
                            ProcesoCreacionReservaUI.Estado.CREADA
                                                 )
                }

                @Test
                fun vuelve_a_emitir_los_mismos_mensajes()
                {
                    modelo.intentarCrearActivarYConsultarReserva()

                    observadorDeMensajesDeError.assertValues(
                            "",
                            "Hubo un error en la conexión y no fue posible contactar al servidor",
                            "",
                            "Hubo un error en la conexión y no fue posible contactar al servidor"
                                                            )
                }

                @Test
                fun llama_endpoint_crear_reserva_una_vez_y_por_cada_intento_el_endpoint_de_activar_reserva()
                {
                    modelo.intentarCrearActivarYConsultarReserva()

                    with(inOrder(apiReservas))
                    {
                        verify(apiReservas).actualizar(anyString(), cualquiera())
                        verify(apiReservas, times(2)).actualizarCampos(anyString(), eqParaKotlin(TransaccionEntidadTerminadaDTO(true)))
                        verifyNoMoreInteractions()
                    }
                }
            }
        }

        @Nested
        inner class BackendRetornaErrorBack
        {
            private val mensajeDeError = "error reicbido esperado"
            private lateinit var modelo: ProcesoCreacionReservaUI

            @BeforeEach
            fun mockearDependencias()
            {
                val errorDePeticion = ErrorDePeticion(-1, mensajeDeError)
                doReturn(RespuestaVacia.Error.Back(400, errorDePeticion))
                    .`when`(apiReservas)
                    .actualizarCampos(anyString(), cualquiera())

                modelo = ProcesoCreacionReserva(contextoDeSesion, creditosAProcesar, apiReservas, Schedulers.trampoline())
            }


            @Test
            fun cambia_el_estado_de_creada_a_activando_y_luego_activada()
            {
                val observadorEstado = modelo.estado.test()

                modelo.intentarCrearActivarYConsultarReserva()

                observadorEstado.assertValueAt(2, ProcesoCreacionReservaUI.Estado.CREADA)
                observadorEstado.assertValueAt(3, ProcesoCreacionReservaUI.Estado.ACTIVANDO)
                observadorEstado.assertValueAt(4, ProcesoCreacionReservaUI.Estado.CREADA)
            }

            @Test
            fun reinicia_el_mensaje_de_error_y_luego_emite_error_mostrando_el_error_recibido()
            {
                val observadorEstado = modelo.mensajesDeError.test()

                modelo.intentarCrearActivarYConsultarReserva()

                observadorEstado.assertValues("", "Error en petición: $mensajeDeError")
            }

            @Test
            fun solo_invoca_los_endpoints_de_actualizar_y_activar_reserva()
            {
                modelo.intentarCrearActivarYConsultarReserva()

                with(inOrder(apiReservas))
                {
                    verify(apiReservas).actualizar(anyString(), cualquiera())
                    verify(apiReservas).actualizarCampos(anyString(), eqParaKotlin(TransaccionEntidadTerminadaDTO(true)))
                    verifyNoMoreInteractions()
                }
            }

            @Test
            fun reserva_con_numero_asignado_no_emite()
            {
                modelo.intentarCrearActivarYConsultarReserva()

                modelo.reservaConNumeroAsignado.test().assertEmpty()
            }

            @Nested
            inner class ReintentandoActivacion
            {
                private lateinit var observadorEstado: TestObserver<ProcesoCreacionReservaUI.Estado>
                private lateinit var observadorDeMensajesDeError: TestObserver<String>

                @BeforeEach
                fun primerIntento()
                {
                    observadorEstado = modelo.estado.test()
                    observadorDeMensajesDeError = modelo.mensajesDeError.test()
                    modelo.intentarCrearActivarYConsultarReserva()
                }

                @Test
                fun emite_los_mismos_estados_de_antes()
                {
                    modelo.intentarCrearActivarYConsultarReserva()

                    observadorEstado.assertValues(
                            ProcesoCreacionReservaUI.Estado.SIN_CREAR,
                            ProcesoCreacionReservaUI.Estado.CREANDO,
                            ProcesoCreacionReservaUI.Estado.CREADA,
                            ProcesoCreacionReservaUI.Estado.ACTIVANDO,
                            ProcesoCreacionReservaUI.Estado.CREADA,
                            ProcesoCreacionReservaUI.Estado.ACTIVANDO,
                            ProcesoCreacionReservaUI.Estado.CREADA
                                                 )
                }

                @Test
                fun vuelve_a_emitir_los_mismos_mensajes()
                {
                    modelo.intentarCrearActivarYConsultarReserva()

                    observadorDeMensajesDeError.assertValues(
                            "",
                            "Error en petición: $mensajeDeError",
                            "",
                            "Error en petición: $mensajeDeError"
                                                            )
                }

                @Test
                fun llama_endpoint_crear_reserva_una_vez_y_por_cada_intento_el_endpoint_de_activar_reserva()
                {
                    modelo.intentarCrearActivarYConsultarReserva()

                    with(inOrder(apiReservas))
                    {
                        verify(apiReservas).actualizar(anyString(), cualquiera())
                        verify(apiReservas, times(2)).actualizarCampos(anyString(), eqParaKotlin(TransaccionEntidadTerminadaDTO(true)))
                        verifyNoMoreInteractions()
                    }
                }
            }

        }
    }

    @Nested
    inner class AlConsultarReserva
    {
        @BeforeEach
        fun crearYActivarReserva()
        {
            doReturn(RespuestaIndividual.Exitosa(reservaACrearEsperada))
                .`when`(apiReservas)
                .actualizar(anyString(), cualquiera())

            doReturn(RespuestaVacia.Exitosa).`when`(apiReservas).actualizarCampos(anyString(), cualquiera())
        }

        @Nested
        inner class BackendRetornaExitoso
        {
            private lateinit var modelo: ProcesoCreacionReservaUI

            @BeforeEach
            fun mockearDependencias()
            {
                doReturn(RespuestaIndividual.Exitosa(reservaConNumeroEsperada)).`when`(apiReservas).consultar(anyString())

                modelo = ProcesoCreacionReserva(contextoDeSesion, creditosAProcesar, apiReservas, Schedulers.trampoline())
            }


            @Test
            fun cambia_el_estado_de_creada_a_consultando_numero_de_reserva_y_luego_a_proceso_finalizado()
            {
                val observadorEstado = modelo.estado.test()

                modelo.intentarCrearActivarYConsultarReserva()

                observadorEstado.assertValueAt(4, ProcesoCreacionReservaUI.Estado.ACTIVADA)
                observadorEstado.assertValueAt(5, ProcesoCreacionReservaUI.Estado.CONSULTANDO_NUMERO_DE_RESERVA)
                observadorEstado.assertValueAt(6, ProcesoCreacionReservaUI.Estado.PROCESO_FINALIZADO)
            }

            @Test
            fun reinicia_el_mensaje_de_error()
            {
                val observadorEstado = modelo.mensajesDeError.test()

                modelo.intentarCrearActivarYConsultarReserva()

                observadorEstado.assertValue("")
            }

            @Test
            fun invoca_el_endpoint_de_actualizar_reserva_el_de_activar_y_finalmente_el_de_consultar()
            {
                modelo.intentarCrearActivarYConsultarReserva()

                with(inOrder(apiReservas))
                {
                    verify(apiReservas).actualizar(anyString(), cualquiera())
                    verify(apiReservas).actualizarCampos(anyString(), eqParaKotlin(TransaccionEntidadTerminadaDTO(true)))
                    verify(apiReservas).consultar(anyString())
                    verifyNoMoreInteractions()
                }
            }

            @Test
            fun reserva_con_numero_asignado_emite_reserva_devuelta_por_backend()
            {
                val observador = modelo.reservaConNumeroAsignado.test()

                modelo.intentarCrearActivarYConsultarReserva()

                observador.assertResult(reservaConNumeroEsperada)
            }
        }

        @Nested
        inner class BackendRetornaVacio
        {
            private lateinit var modelo: ProcesoCreacionReservaUI

            @BeforeEach
            fun mockearDependencias()
            {
                doReturn(RespuestaIndividual.Vacia<Reserva>()).`when`(apiReservas).consultar(anyString())

                modelo = ProcesoCreacionReserva(contextoDeSesion, creditosAProcesar, apiReservas, Schedulers.trampoline())

                erroresEsperados.add(CompositeException(IllegalStateException()))
            }


            @Test
            fun cambia_el_estado_a_consultando_numero_de_reserva_y_vuelve_a_activada()
            {
                val observadorEstado = modelo.estado.test()

                modelo.intentarCrearActivarYConsultarReserva()

                observadorEstado.assertValues(
                        ProcesoCreacionReservaUI.Estado.SIN_CREAR,
                        ProcesoCreacionReservaUI.Estado.CREANDO,
                        ProcesoCreacionReservaUI.Estado.CREADA,
                        ProcesoCreacionReservaUI.Estado.ACTIVANDO,
                        ProcesoCreacionReservaUI.Estado.ACTIVADA,
                        ProcesoCreacionReservaUI.Estado.CONSULTANDO_NUMERO_DE_RESERVA,
                        ProcesoCreacionReservaUI.Estado.ACTIVADA
                                             )
            }

            @Test
            fun reinicia_el_mensaje_de_error_y_no_emite_error()
            {
                val observadorEstado = modelo.mensajesDeError.test()

                modelo.intentarCrearActivarYConsultarReserva()

                observadorEstado.assertValue("")
            }

            @Test
            fun invoca_el_endpoint_de_actualizar_reserva_el_de_activar_y_finalmente_el_de_consultar()
            {
                modelo.intentarCrearActivarYConsultarReserva()

                with(inOrder(apiReservas))
                {
                    verify(apiReservas).actualizar(anyString(), cualquiera())
                    verify(apiReservas).actualizarCampos(anyString(), eqParaKotlin(TransaccionEntidadTerminadaDTO(true)))
                    verify(apiReservas).consultar(anyString())
                    verifyNoMoreInteractions()
                }
            }

            @Test
            fun si_fallo_no_permite_reintentar_creacion()
            {
                modelo.intentarCrearActivarYConsultarReserva()

                modelo.intentarCrearActivarYConsultarReserva()

                with(inOrder(apiReservas))
                {
                    verify(apiReservas).actualizar(anyString(), cualquiera())
                    verify(apiReservas).actualizarCampos(anyString(), eqParaKotlin(TransaccionEntidadTerminadaDTO(true)))
                    verify(apiReservas).consultar(anyString())
                    verifyNoMoreInteractions()
                }
            }

            @Test
            fun reserva_con_numero_asignado_no_emite()
            {
                modelo.intentarCrearActivarYConsultarReserva()

                modelo.reservaConNumeroAsignado.test().assertEmpty()
            }
        }

        @Nested
        inner class BackendRetornaErrorTimeout
        {
            private lateinit var modelo: ProcesoCreacionReservaUI

            @BeforeEach
            fun mockearDependencias()
            {
                doReturn(RespuestaIndividual.Error.Timeout<Reserva>()).`when`(apiReservas).consultar(anyString())

                modelo = ProcesoCreacionReserva(contextoDeSesion, creditosAProcesar, apiReservas, Schedulers.trampoline())
            }


            @Test
            fun cambia_el_estado_de_creada_a_consultando_numero_de_reserva_y_luego_a_proceso_finalizado()
            {
                val observadorEstado = modelo.estado.test()

                modelo.intentarCrearActivarYConsultarReserva()

                observadorEstado.assertValueAt(4, ProcesoCreacionReservaUI.Estado.ACTIVADA)
                observadorEstado.assertValueAt(5, ProcesoCreacionReservaUI.Estado.CONSULTANDO_NUMERO_DE_RESERVA)
                observadorEstado.assertValueAt(6, ProcesoCreacionReservaUI.Estado.ACTIVADA)
            }

            @Test
            fun reinicia_el_mensaje_de_error_y_luego_emite_error_diciendo_que_hubo_un_timeout()
            {
                val observadorEstado = modelo.mensajesDeError.test()

                modelo.intentarCrearActivarYConsultarReserva()

                observadorEstado.assertValues("", "Timeout contactando el backend")
            }

            @Test
            fun invoca_el_endpoint_de_actualizar_reserva_el_de_activar_y_finalmente_el_de_consultar()
            {
                modelo.intentarCrearActivarYConsultarReserva()

                with(inOrder(apiReservas))
                {
                    verify(apiReservas).actualizar(anyString(), cualquiera())
                    verify(apiReservas).actualizarCampos(anyString(), eqParaKotlin(TransaccionEntidadTerminadaDTO(true)))
                    verify(apiReservas).consultar(anyString())
                    verifyNoMoreInteractions()
                }
            }

            @Test
            fun reserva_con_numero_asignado_no_emite()
            {
                modelo.intentarCrearActivarYConsultarReserva()

                modelo.reservaConNumeroAsignado.test().assertEmpty()
            }

            @Nested
            inner class ReintentandoConsulta
            {
                private lateinit var observadorEstado: TestObserver<ProcesoCreacionReservaUI.Estado>
                private lateinit var observadorDeMensajesDeError: TestObserver<String>

                @BeforeEach
                fun primerIntento()
                {
                    observadorEstado = modelo.estado.test()
                    observadorDeMensajesDeError = modelo.mensajesDeError.test()
                    modelo.intentarCrearActivarYConsultarReserva()
                }

                @Test
                fun emite_los_mismos_estados_de_antes()
                {
                    modelo.intentarCrearActivarYConsultarReserva()

                    observadorEstado.assertValues(
                            ProcesoCreacionReservaUI.Estado.SIN_CREAR,
                            ProcesoCreacionReservaUI.Estado.CREANDO,
                            ProcesoCreacionReservaUI.Estado.CREADA,
                            ProcesoCreacionReservaUI.Estado.ACTIVANDO,
                            ProcesoCreacionReservaUI.Estado.ACTIVADA,
                            ProcesoCreacionReservaUI.Estado.CONSULTANDO_NUMERO_DE_RESERVA,
                            ProcesoCreacionReservaUI.Estado.ACTIVADA,
                            ProcesoCreacionReservaUI.Estado.CONSULTANDO_NUMERO_DE_RESERVA,
                            ProcesoCreacionReservaUI.Estado.ACTIVADA
                                                 )
                }

                @Test
                fun vuelve_a_emitir_los_mismos_mensajes()
                {
                    modelo.intentarCrearActivarYConsultarReserva()

                    observadorDeMensajesDeError.assertValues(
                            "",
                            "Timeout contactando el backend",
                            "",
                            "Timeout contactando el backend"
                                                            )
                }

                @Test
                fun llama_los_endpoints_de_crear_y_activar_reserva_una_vez_y_por_cada_intento_el_endpoint_de_consultar_reserva()
                {
                    modelo.intentarCrearActivarYConsultarReserva()

                    with(inOrder(apiReservas))
                    {
                        verify(apiReservas).actualizar(anyString(), cualquiera())
                        verify(apiReservas).actualizarCampos(anyString(), eqParaKotlin(TransaccionEntidadTerminadaDTO(true)))
                        verify(apiReservas, times(2)).consultar(anyString())
                        verifyNoMoreInteractions()
                    }
                }
            }
        }

        @Nested
        inner class BackendRetornaErrorRed
        {
            private lateinit var modelo: ProcesoCreacionReservaUI

            @BeforeEach
            fun mockearDependencias()
            {
                doReturn(RespuestaIndividual.Error.Red<Reserva>(IOException()))
                    .`when`(apiReservas)
                    .consultar(anyString())

                modelo = ProcesoCreacionReserva(contextoDeSesion, creditosAProcesar, apiReservas, Schedulers.trampoline())
            }


            @Test
            fun cambia_el_estado_de_creada_a_consultando_numero_de_reserva_y_luego_a_proceso_finalizado()
            {
                val observadorEstado = modelo.estado.test()

                modelo.intentarCrearActivarYConsultarReserva()

                observadorEstado.assertValueAt(4, ProcesoCreacionReservaUI.Estado.ACTIVADA)
                observadorEstado.assertValueAt(5, ProcesoCreacionReservaUI.Estado.CONSULTANDO_NUMERO_DE_RESERVA)
                observadorEstado.assertValueAt(6, ProcesoCreacionReservaUI.Estado.ACTIVADA)
            }

            @Test
            fun reinicia_el_mensaje_de_error_y_luego_emite_error_diciendo_que_hubo_un_timeout()
            {
                val observadorEstado = modelo.mensajesDeError.test()

                modelo.intentarCrearActivarYConsultarReserva()

                observadorEstado.assertValues("", "Hubo un error en la conexión y no fue posible contactar al servidor")
            }

            @Test
            fun invoca_el_endpoint_de_actualizar_reserva_el_de_activar_y_finalmente_el_de_consultar()
            {
                modelo.intentarCrearActivarYConsultarReserva()

                with(inOrder(apiReservas))
                {
                    verify(apiReservas).actualizar(anyString(), cualquiera())
                    verify(apiReservas).actualizarCampos(anyString(), eqParaKotlin(TransaccionEntidadTerminadaDTO(true)))
                    verify(apiReservas).consultar(anyString())
                    verifyNoMoreInteractions()
                }
            }

            @Test
            fun reserva_con_numero_asignado_no_emite()
            {
                modelo.intentarCrearActivarYConsultarReserva()

                modelo.reservaConNumeroAsignado.test().assertEmpty()
            }

            @Nested
            inner class ReintentandoConsulta
            {
                private lateinit var observadorEstado: TestObserver<ProcesoCreacionReservaUI.Estado>
                private lateinit var observadorDeMensajesDeError: TestObserver<String>

                @BeforeEach
                fun primerIntento()
                {
                    observadorEstado = modelo.estado.test()
                    observadorDeMensajesDeError = modelo.mensajesDeError.test()
                    modelo.intentarCrearActivarYConsultarReserva()
                }

                @Test
                fun emite_los_mismos_estados_de_antes()
                {
                    modelo.intentarCrearActivarYConsultarReserva()

                    observadorEstado.assertValues(
                            ProcesoCreacionReservaUI.Estado.SIN_CREAR,
                            ProcesoCreacionReservaUI.Estado.CREANDO,
                            ProcesoCreacionReservaUI.Estado.CREADA,
                            ProcesoCreacionReservaUI.Estado.ACTIVANDO,
                            ProcesoCreacionReservaUI.Estado.ACTIVADA,
                            ProcesoCreacionReservaUI.Estado.CONSULTANDO_NUMERO_DE_RESERVA,
                            ProcesoCreacionReservaUI.Estado.ACTIVADA,
                            ProcesoCreacionReservaUI.Estado.CONSULTANDO_NUMERO_DE_RESERVA,
                            ProcesoCreacionReservaUI.Estado.ACTIVADA
                                                 )
                }

                @Test
                fun vuelve_a_emitir_los_mismos_mensajes()
                {
                    modelo.intentarCrearActivarYConsultarReserva()

                    observadorDeMensajesDeError.assertValues(
                            "",
                            "Hubo un error en la conexión y no fue posible contactar al servidor",
                            "",
                            "Hubo un error en la conexión y no fue posible contactar al servidor"
                                                            )
                }

                @Test
                fun llama_los_endpoints_de_crear_y_activar_reserva_una_vez_y_por_cada_intento_el_endpoint_de_consultar_reserva()
                {
                    modelo.intentarCrearActivarYConsultarReserva()

                    with(inOrder(apiReservas))
                    {
                        verify(apiReservas).actualizar(anyString(), cualquiera())
                        verify(apiReservas).actualizarCampos(anyString(), eqParaKotlin(TransaccionEntidadTerminadaDTO(true)))
                        verify(apiReservas, times(2)).consultar(anyString())
                        verifyNoMoreInteractions()
                    }
                }
            }
        }

        @Nested
        inner class BackendRetornaErrorBack
        {
            private val mensajeDeError = "error reicbido esperado"
            private lateinit var modelo: ProcesoCreacionReservaUI

            @BeforeEach
            fun mockearDependencias()
            {
                val errorDePeticion = ErrorDePeticion(-1, mensajeDeError)
                doReturn(RespuestaIndividual.Error.Back<Reserva>(400, errorDePeticion))
                    .`when`(apiReservas)
                    .consultar(anyString())

                modelo = ProcesoCreacionReserva(contextoDeSesion, creditosAProcesar, apiReservas, Schedulers.trampoline())
            }


            @Test
            fun cambia_el_estado_de_creada_a_consultando_numero_de_reserva_y_luego_a_proceso_finalizado()
            {
                val observadorEstado = modelo.estado.test()

                modelo.intentarCrearActivarYConsultarReserva()

                observadorEstado.assertValueAt(4, ProcesoCreacionReservaUI.Estado.ACTIVADA)
                observadorEstado.assertValueAt(5, ProcesoCreacionReservaUI.Estado.CONSULTANDO_NUMERO_DE_RESERVA)
                observadorEstado.assertValueAt(6, ProcesoCreacionReservaUI.Estado.ACTIVADA)
            }

            @Test
            fun reinicia_el_mensaje_de_error_y_luego_emite_error_diciendo_que_hubo_un_timeout()
            {
                val observadorEstado = modelo.mensajesDeError.test()

                modelo.intentarCrearActivarYConsultarReserva()

                observadorEstado.assertValues("", "Error en petición: $mensajeDeError")
            }

            @Test
            fun invoca_el_endpoint_de_actualizar_reserva_el_de_activar_y_finalmente_el_de_consultar()
            {
                modelo.intentarCrearActivarYConsultarReserva()

                with(inOrder(apiReservas))
                {
                    verify(apiReservas).actualizar(anyString(), cualquiera())
                    verify(apiReservas).actualizarCampos(anyString(), eqParaKotlin(TransaccionEntidadTerminadaDTO(true)))
                    verify(apiReservas).consultar(anyString())
                    verifyNoMoreInteractions()
                }
            }

            @Test
            fun reserva_con_numero_asignado_no_emite()
            {
                modelo.intentarCrearActivarYConsultarReserva()

                modelo.reservaConNumeroAsignado.test().assertEmpty()
            }

            @Nested
            inner class ReintentandoConsulta
            {
                private lateinit var observadorEstado: TestObserver<ProcesoCreacionReservaUI.Estado>
                private lateinit var observadorDeMensajesDeError: TestObserver<String>

                @BeforeEach
                fun primerIntento()
                {
                    observadorEstado = modelo.estado.test()
                    observadorDeMensajesDeError = modelo.mensajesDeError.test()
                    modelo.intentarCrearActivarYConsultarReserva()
                }

                @Test
                fun emite_los_mismos_estados_de_antes()
                {
                    modelo.intentarCrearActivarYConsultarReserva()

                    observadorEstado.assertValues(
                            ProcesoCreacionReservaUI.Estado.SIN_CREAR,
                            ProcesoCreacionReservaUI.Estado.CREANDO,
                            ProcesoCreacionReservaUI.Estado.CREADA,
                            ProcesoCreacionReservaUI.Estado.ACTIVANDO,
                            ProcesoCreacionReservaUI.Estado.ACTIVADA,
                            ProcesoCreacionReservaUI.Estado.CONSULTANDO_NUMERO_DE_RESERVA,
                            ProcesoCreacionReservaUI.Estado.ACTIVADA,
                            ProcesoCreacionReservaUI.Estado.CONSULTANDO_NUMERO_DE_RESERVA,
                            ProcesoCreacionReservaUI.Estado.ACTIVADA
                                                 )
                }

                @Test
                fun vuelve_a_emitir_los_mismos_mensajes()
                {
                    modelo.intentarCrearActivarYConsultarReserva()

                    observadorDeMensajesDeError.assertValues(
                            "",
                            "Error en petición: $mensajeDeError",
                            "",
                            "Error en petición: $mensajeDeError"
                                                            )
                }

                @Test
                fun llama_los_endpoints_de_crear_y_activar_reserva_una_vez_y_por_cada_intento_el_endpoint_de_consultar_reserva()
                {
                    modelo.intentarCrearActivarYConsultarReserva()

                    with(inOrder(apiReservas))
                    {
                        verify(apiReservas).actualizar(anyString(), cualquiera())
                        verify(apiReservas).actualizarCampos(anyString(), eqParaKotlin(TransaccionEntidadTerminadaDTO(true)))
                        verify(apiReservas, times(2)).consultar(anyString())
                        verifyNoMoreInteractions()
                    }
                }
            }
        }
    }
}