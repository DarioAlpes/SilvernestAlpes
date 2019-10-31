package co.smartobjects.ui.modelos.codificacion

import co.smartobjects.entidades.operativas.compras.CreditoFondoConNombre
import co.smartobjects.entidades.operativas.compras.CreditoPaqueteConNombre
import co.smartobjects.entidades.operativas.reservas.SesionDeManilla
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.personas.PersonaConGrupoCliente
import co.smartobjects.entidades.tagscodificables.TagConsumos
import co.smartobjects.nfc.ResultadoNFC
import co.smartobjects.nfc.operacionessobretags.ResultadoLecturaNFC
import co.smartobjects.nfc.utils.comprimir
import co.smartobjects.red.clientes.base.RespuestaVacia
import co.smartobjects.red.clientes.operativas.reservas.sesionesdemanilla.SesionDeManillaAPI
import co.smartobjects.red.modelos.ErrorDePeticion
import co.smartobjects.red.modelos.operativas.reservas.SesionDeManillaDTO
import co.smartobjects.ui.modelos.*
import co.smartobjects.ui.modelos.pagos.ProcesoPagarUI
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import co.smartobjects.utilidades.sumar
import io.reactivex.Flowable
import io.reactivex.observers.TestObserver
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.threeten.bp.LocalDate
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


internal class ItemACodificarPruebas : PruebasModelosRxBase()
{
    companion object
    {
        private const val ID_CLIENTE = 1L
        private const val ID_PERSONA = 1L
        private const val ID_SESION_MANILLA = 1L
    }

    private val mockOperacionesCompuestas = OperacionesCompuestasMockeadas.crear()
    private val creditosACodificar =
            ProcesoPagarUI.CreditosACodificarPorPersona(
                    PersonaConGrupoCliente(
                            Persona(
                                    ID_CLIENTE, ID_PERSONA, "Pepito pérez", Persona.TipoDocumento.TI, "123456", Persona.Genero.MASCULINO,
                                    LocalDate.now(ZONA_HORARIA_POR_DEFECTO), Persona.Categoria.A, Persona.Afiliacion.COTIZANTE, false,
                                    null,"empresa","",Persona.Tipo.NO_AFILIADO
                                   ),
                            null
                                          ),
                    listOf(
                            CreditoFondoConNombre("Crédito Fondo Pagado 0", crearCreditoFondo(0, ID_PERSONA)),
                            CreditoFondoConNombre("Crédito Fondo Pagado 1", crearCreditoFondo(1, ID_PERSONA))
                          ),
                    listOf(
                            CreditoPaqueteConNombre("Crédito Paquete Pagado 2", 1, crearCreditoPaquete(2, ID_PERSONA)),
                            CreditoPaqueteConNombre("Crédito Paquete Pagado 3", 1, crearCreditoPaquete(3, ID_PERSONA))
                          )
                                                       )

    private val apiSesionDeManilla = mockConDefaultAnswer(SesionDeManillaAPI::class.java).also {
        doReturn(RespuestaVacia.Exitosa).`when`(it).actualizarCampos(anyLong(), cualquiera())
    }

    private val sesionDeManilla = SesionDeManilla(
            ID_CLIENTE,
            ID_SESION_MANILLA,
            ID_PERSONA,
            null,
            null,
            null,
            creditosACodificar.creditosFondoTotales.asSequence().map { it.idFondoComprado }.toSet()
                                                 )

    @Test
    fun total_pagado_es_la_suma_de_todos_los_valores_pagados_de_los_creditos_fondo()
    {
        val totalPagadoEsperado = creditosACodificar.creditosFondoTotales.map { it.valorPagado }.sumar()

        val emisorResultadoNFCExitoso = PublishProcessor.create<ResultadoNFC.Exitoso>()

        val modelo = ItemACodificar(sesionDeManilla, creditosACodificar, emisorResultadoNFCExitoso, apiSesionDeManilla)

        assertEquals(totalPagadoEsperado, modelo.totalPagado)
    }

    @Test
    fun items_creditos_a_codificar_son_los_creditos_fondo_y_los_creditos_paquete_concatenados()
    {
        val itemsCreditosACodificarEsperados =
                (
                        creditosACodificar.creditosFondoPagados.asSequence().map {
                            ItemACodificarUI.ItemCreditoACodificar(it.nombreDeFondo, it.creditoAsociado.valorPagado, it.creditoAsociado.cantidad.aInt())
                        } + creditosACodificar.creditosPaquetePagados.asSequence().map {
                            ItemACodificarUI.ItemCreditoACodificar(it.nombreDelPaquete, it.creditoAsociado.valorPagado, it.cantidad)
                        }
                ).toList()

        val emisorResultadoNFCExitoso = PublishProcessor.create<ResultadoNFC.Exitoso>()

        val modelo = ItemACodificar(sesionDeManilla, creditosACodificar, emisorResultadoNFCExitoso, apiSesionDeManilla)

        assertEquals(itemsCreditosACodificarEsperados, modelo.itemsCreditosACodificar)
    }

    @Nested
    inner class NoHabilitadoParaCodificar
    {
        @Test
        fun no_interactua_con_la_operaciones_nfc()
        {
            val emisorResultadoNFCExitoso = PublishProcessor.create<ResultadoNFC.Exitoso>()

            ItemACodificar(
                    sesionDeManilla,
                    creditosACodificar,
                    emisorResultadoNFCExitoso,
                    apiSesionDeManilla,
                    Schedulers.trampoline()
                          )

            emisorResultadoNFCExitoso.onNext(ResultadoNFC.Exitoso(mockOperacionesCompuestas))

            verifyZeroInteractions(mockOperacionesCompuestas)
        }

        @Test
        fun estado_emite_sin_codificar()
        {
            val modelo = ItemACodificar(
                    sesionDeManilla,
                    creditosACodificar,
                    Flowable.empty(),
                    apiSesionDeManilla,
                    Schedulers.trampoline()
                                       )

            val observador = modelo.estado.test()

            observador.assertValue(ItemACodificarUI.Estado.EtapaSesionManilla.SinCodificar)
        }
    }

    @Test
    fun en_estado_activado_no_puede_codificar()
    {
        val emisorResultadoNFCExitoso = PublishProcessor.create<ResultadoNFC.Exitoso>()

        val modelo = ItemACodificar(
                sesionDeManilla,
                creditosACodificar,
                emisorResultadoNFCExitoso,
                apiSesionDeManilla,
                Schedulers.trampoline()
                                   )

        val observadorEstado = modelo.estado.test()
        val observadorMensajesDeError = modelo.mensajesDeError.test()

        modelo.habilitarParaCodificacion()
        mockOperacionesCompuestas.retornoDeLeerTag = ResultadoLecturaNFC.TagVacio
        mockOperacionesCompuestas.retornoDeEscribirTag = true
        emisorResultadoNFCExitoso.onNext(ResultadoNFC.Exitoso(mockOperacionesCompuestas))

        modelo.habilitarParaCodificacion()

        observadorEstado.assertValues(
                ItemACodificarUI.Estado.EtapaSesionManilla.SinCodificar,
                ItemACodificarUI.Estado.EtapaSesionManilla.EsperandoTag,
                ItemACodificarUI.Estado.Codificando,
                ItemACodificarUI.Estado.EtapaSesionManilla.Codificada(OperacionesCompuestasMockeadas.UIDTagPrueba),
                ItemACodificarUI.Estado.Activando,
                ItemACodificarUI.Estado.EtapaSesionManilla.Activada
                                     )
        observadorMensajesDeError.assertValues("", "La sesión ya se encuentra activa y tiene un tag asociado")
    }

    @Nested
    inner class HabilitadoParaCodificar
    {
        private val emisorResultadoNFCExitoso = PublishProcessor.create<ResultadoNFC.Exitoso>()

        private val modelo = ItemACodificar(
                sesionDeManilla,
                creditosACodificar,
                emisorResultadoNFCExitoso,
                apiSesionDeManilla,
                Schedulers.trampoline()
                                           )

        @BeforeEach
        fun habilitarCodificacion()
        {
            modelo.habilitarParaCodificacion()
        }

        @Test
        fun estado_emite_esprando_tag()
        {
            val observador = modelo.estado.test()

            observador.assertValue(ItemACodificarUI.Estado.EtapaSesionManilla.EsperandoTag)
        }

        @Nested
        inner class AlRecibirOperacionesNFC
        {
            @Test
            fun primero_lee_el_tag()
            {
                emisorResultadoNFCExitoso.onNext(ResultadoNFC.Exitoso(mockOperacionesCompuestas))
                inOrder(mockOperacionesCompuestas).verify(mockOperacionesCompuestas).leerTag()
            }

            @Test
            fun reinicia_mensaje_de_error()
            {
                val observadorMensajesDeError = modelo.mensajesDeError.test()
                emisorResultadoNFCExitoso.onNext(ResultadoNFC.Exitoso(mockOperacionesCompuestas))

                observadorMensajesDeError.assertValue("")
            }

            @Nested
            inner class CuandoLecturaEsTagVacio
            {
                private val observadorEstado = modelo.estado.test()
                private val observadorMensajesDeError = modelo.mensajesDeError.test()

                @BeforeEach
                fun mockearResultadoDeLectura()
                {
                    mockOperacionesCompuestas.retornoDeLeerTag = ResultadoLecturaNFC.TagVacio
                }

                @Nested
                inner class CodificoCorrectamente
                {
                    @BeforeEach
                    fun mockearResultadoDeEscrituraYEmitir()
                    {
                        mockOperacionesCompuestas.retornoDeEscribirTag = true
                    }

                    @Test
                    fun se_codifica_en_el_tag_el_id_de_sesion_de_manilla_correcto_y_los_creditos_pagados_y_no_pagados()
                    {
                        emisorResultadoNFCExitoso.onNext(ResultadoNFC.Exitoso(mockOperacionesCompuestas))

                        val tagCodificado = TagConsumos(ID_SESION_MANILLA, creditosACodificar.creditosFondoTotales)

                        val informacionEscritaEsperada = comprimir(tagCodificado.aByteArray())

                        verify(mockOperacionesCompuestas).escribirTag(informacionEscritaEsperada)
                    }

                    @Test
                    fun se_invoca_el_api_para_activar_la_manilla_correctamente()
                    {
                        val parametrosDeActualizacionEsperados =
                                SesionDeManillaAPI.ParametrosActualizacionParcial.Activacion(OperacionesCompuestasMockeadas.UIDTagPrueba)

                        emisorResultadoNFCExitoso.onNext(ResultadoNFC.Exitoso(mockOperacionesCompuestas))

                        verify(apiSesionDeManilla).actualizarCampos(eq(ID_SESION_MANILLA), eqParaKotlin(parametrosDeActualizacionEsperados))
                    }

                    @Nested
                    inner class SiLaActivacion
                    {
                        @Nested
                        inner class FueExitosa
                        {
                            @BeforeEach
                            fun mockearRespuestaPatch()
                            {
                                doReturn(RespuestaVacia.Exitosa)
                                    .`when`(apiSesionDeManilla)
                                    .actualizarCampos(anyLong(), cualquiera())
                            }

                            @Test
                            fun el_estado_emitido_es_activado()
                            {
                                emisorResultadoNFCExitoso.onNext(ResultadoNFC.Exitoso(mockOperacionesCompuestas))

                                observadorEstado.assertValues(
                                        ItemACodificarUI.Estado.EtapaSesionManilla.SinCodificar,
                                        ItemACodificarUI.Estado.EtapaSesionManilla.EsperandoTag,
                                        ItemACodificarUI.Estado.Codificando,
                                        ItemACodificarUI.Estado.EtapaSesionManilla.Codificada(OperacionesCompuestasMockeadas.UIDTagPrueba),
                                        ItemACodificarUI.Estado.Activando,
                                        ItemACodificarUI.Estado.EtapaSesionManilla.Activada
                                                             )
                            }

                            @Test
                            fun mensajes_de_error_no_emite_nada()
                            {
                                observadorMensajesDeError.assertEmpty()
                            }
                        }

                        @Nested
                        inner class FalloCon
                        {
                            @Nested
                            inner class UnErrorTimeout
                            {
                                @BeforeEach
                                fun mockearRespuestaPatch()
                                {
                                    doReturn(RespuestaVacia.Error.Timeout)
                                        .`when`(apiSesionDeManilla)
                                        .actualizarCampos(anyLong(), cualquiera())

                                    emisorResultadoNFCExitoso.onNext(ResultadoNFC.Exitoso(mockOperacionesCompuestas))
                                }

                                @Test
                                fun el_estado_emitido_es_activando_y_luego_codificado_con_el_uuid_del_tag()
                                {
                                    observadorEstado.assertValues(
                                            ItemACodificarUI.Estado.EtapaSesionManilla.SinCodificar,
                                            ItemACodificarUI.Estado.EtapaSesionManilla.EsperandoTag,
                                            ItemACodificarUI.Estado.Codificando,
                                            ItemACodificarUI.Estado.EtapaSesionManilla.Codificada(OperacionesCompuestasMockeadas.UIDTagPrueba),
                                            ItemACodificarUI.Estado.Activando,
                                            ItemACodificarUI.Estado.EtapaSesionManilla.Codificada(OperacionesCompuestasMockeadas.UIDTagPrueba)
                                                                 )
                                }

                                @Test
                                fun mensajes_de_error_emite_mensaje_diciendo_que_hubo_un_timeout_al_intentar_activar_la_sesion()
                                {
                                    observadorMensajesDeError.assertValueCount(2)
                                    observadorMensajesDeError.verificarUltimoValorEmitido("Tiempo de espera al servidor agotado. No se pudo activar la sesión asociada con el servidor")
                                }
                            }

                            @Nested
                            inner class UnErrorRed
                            {
                                @BeforeEach
                                fun mockearRespuestaPatch()
                                {
                                    doReturn(RespuestaVacia.Error.Red(IOException("Excepción de red")))
                                        .`when`(apiSesionDeManilla)
                                        .actualizarCampos(anyLong(), cualquiera())

                                    emisorResultadoNFCExitoso.onNext(ResultadoNFC.Exitoso(mockOperacionesCompuestas))
                                }

                                @Test
                                fun el_estado_emitido_es_activando_y_luego_codificado_con_el_uuid_del_tag_y_luego_activando()
                                {
                                    observadorEstado.assertValues(
                                            ItemACodificarUI.Estado.EtapaSesionManilla.SinCodificar,
                                            ItemACodificarUI.Estado.EtapaSesionManilla.EsperandoTag,
                                            ItemACodificarUI.Estado.Codificando,
                                            ItemACodificarUI.Estado.EtapaSesionManilla.Codificada(OperacionesCompuestasMockeadas.UIDTagPrueba),
                                            ItemACodificarUI.Estado.Activando,
                                            ItemACodificarUI.Estado.EtapaSesionManilla.Codificada(OperacionesCompuestasMockeadas.UIDTagPrueba)
                                                                 )
                                }

                                @Test
                                fun mensajes_de_error_emite_mensaje_diciendo_que_hubo_un_error_de_red()
                                {
                                    observadorMensajesDeError.assertValueCount(2)
                                    observadorMensajesDeError.verificarUltimoValorEmitido("Hubo un error en la conexión y no fue posible contactar al servidor")
                                }
                            }

                            @Nested
                            inner class UnErrorBack
                            {
                                @Nested
                                inner class SesionYaTieneTagAsociado
                                {
                                    @BeforeEach
                                    fun mockearRespuestaPatch()
                                    {
                                        val error = ErrorDePeticion(SesionDeManillaDTO.CodigosError.SESION_YA_TIENE_TAG_ASOCIADO, "no importa")

                                        doReturn(RespuestaVacia.Error.Back(400, error))
                                            .`when`(apiSesionDeManilla)
                                            .actualizarCampos(anyLong(), cualquiera())

                                        emisorResultadoNFCExitoso.onNext(ResultadoNFC.Exitoso(mockOperacionesCompuestas))
                                    }

                                    @Test
                                    fun el_estado_emitido_es_activado()
                                    {
                                        observadorEstado.assertValues(
                                                ItemACodificarUI.Estado.EtapaSesionManilla.SinCodificar,
                                                ItemACodificarUI.Estado.EtapaSesionManilla.EsperandoTag,
                                                ItemACodificarUI.Estado.Codificando,
                                                ItemACodificarUI.Estado.EtapaSesionManilla.Codificada(OperacionesCompuestasMockeadas.UIDTagPrueba),
                                                ItemACodificarUI.Estado.Activando,
                                                ItemACodificarUI.Estado.EtapaSesionManilla.Activada
                                                                     )
                                    }

                                    @Test
                                    fun mensajes_de_error_emite_mensaje_diciendo_que_ya_esta_activa()
                                    {
                                        observadorMensajesDeError.assertValueCount(2)
                                        observadorMensajesDeError.verificarUltimoValorEmitido("La sesión ya se encuentra activa y tiene un tag asociado")
                                    }
                                }

                                @Nested
                                inner class Desconocido
                                {
                                    @BeforeEach
                                    fun mockearRespuestaPatch()
                                    {
                                        doReturn(RespuestaVacia.Error.Back(400, ErrorDePeticion(-1, "mensaje del error")))
                                            .`when`(apiSesionDeManilla)
                                            .actualizarCampos(anyLong(), cualquiera())

                                        emisorResultadoNFCExitoso.onNext(ResultadoNFC.Exitoso(mockOperacionesCompuestas))
                                    }

                                    @Test
                                    fun el_estado_emitido_es_activando_y_luego_codificado_con_el_uuid_del_tag_y_luego_activando()
                                    {
                                        observadorEstado.assertValues(
                                                ItemACodificarUI.Estado.EtapaSesionManilla.SinCodificar,
                                                ItemACodificarUI.Estado.EtapaSesionManilla.EsperandoTag,
                                                ItemACodificarUI.Estado.Codificando,
                                                ItemACodificarUI.Estado.EtapaSesionManilla.Codificada(OperacionesCompuestasMockeadas.UIDTagPrueba),
                                                ItemACodificarUI.Estado.Activando,
                                                ItemACodificarUI.Estado.EtapaSesionManilla.Codificada(OperacionesCompuestasMockeadas.UIDTagPrueba)
                                                                     )
                                    }

                                    @Test
                                    fun mensajes_de_error_emite_mensaje_diciendo_que_hubo_un_error_de_back_y_el_mensaje_asociado()
                                    {
                                        observadorMensajesDeError.assertValueCount(2)
                                        observadorMensajesDeError.verificarUltimoValorEmitido("Error en petición: mensaje del error")
                                    }
                                }
                            }
                        }
                    }
                }

                @Nested
                inner class NoCodifico
                {
                    @BeforeEach
                    fun mockearResultadoDeEscrituraYEmitir()
                    {
                        mockOperacionesCompuestas.retornoDeEscribirTag = false
                        emisorResultadoNFCExitoso.onNext(ResultadoNFC.Exitoso(mockOperacionesCompuestas))
                    }

                    @Test
                    fun mensajes_de_error_emite_mensaje_diciendo_que_el_tag_esta_programado_con_una_llave_desconocida()
                    {
                        observadorMensajesDeError.verificarUltimoValorEmitido("Error al intentar escribir en el tag")
                    }

                    @Test
                    fun queda_como_no_habilitado_para_codificar_y_solo_interactua_con_las_operaciones_nfc_leyendo_el_tag()
                    {
                        verify(mockOperacionesCompuestas).leerTag()
                    }

                    @Test
                    fun estado_emite_el_estado_del_tag_luego_esperando_tag_luego_codificando_y_finalmente_el_estado_del_tag_inicial()
                    {
                        observadorEstado.assertValues(
                                ItemACodificarUI.Estado.EtapaSesionManilla.SinCodificar,
                                ItemACodificarUI.Estado.EtapaSesionManilla.EsperandoTag,
                                ItemACodificarUI.Estado.Codificando,
                                ItemACodificarUI.Estado.EtapaSesionManilla.EsperandoTag
                                                     )
                    }
                }
            }

            @Nested
            inner class CuandoLecturaEsLlaveDesconocida
            {
                private val observadorEstado = modelo.estado.test()
                private val observadorMensajesDeError = modelo.mensajesDeError.test()

                @BeforeEach
                fun mockearResultadoDeLecturaYEmitir()
                {
                    mockOperacionesCompuestas.retornoDeLeerTag = ResultadoLecturaNFC.LlaveDesconocida
                    emisorResultadoNFCExitoso.onNext(ResultadoNFC.Exitoso(mockOperacionesCompuestas))
                }

                @Test
                fun mensajes_de_error_emite_mensaje_diciendo_que_el_tag_esta_programado_con_una_llave_desconocida()
                {
                    observadorMensajesDeError.verificarUltimoValorEmitido("El tag está programado con una llave desconocida")
                }

                @Test
                fun queda_como_no_habilitado_para_codificar_y_solo_interactua_con_las_operaciones_nfc_leyendo_el_tag()
                {
                    verify(mockOperacionesCompuestas).leerTag()
                }

                @Test
                fun estado_emite_el_estado_del_tag_luego_esperando_tag_luego_codificando_y_finalmente_el_estado_del_tag_inicial()
                {
                    observadorEstado.assertValues(
                            ItemACodificarUI.Estado.EtapaSesionManilla.SinCodificar,
                            ItemACodificarUI.Estado.EtapaSesionManilla.EsperandoTag,
                            ItemACodificarUI.Estado.Codificando,
                            ItemACodificarUI.Estado.EtapaSesionManilla.EsperandoTag
                                                 )
                }
            }

            @Nested
            inner class CuandoLecturaEsSinAutenticacionActivada
            {
                private val observadorEstado = modelo.estado.test()
                private val observadorMensajesDeError = modelo.mensajesDeError.test()

                @BeforeEach
                fun mockearResultadoDeLecturaYEmitir()
                {
                    mockOperacionesCompuestas.retornoDeLeerTag = ResultadoLecturaNFC.SinAutenticacionActivada
                    emisorResultadoNFCExitoso.onNext(ResultadoNFC.Exitoso(mockOperacionesCompuestas))
                }

                @Test
                fun mensajes_de_error_emite_mensaje_diciendo_que_el_tag_no_tiene_autenticacion_activada()
                {
                    observadorMensajesDeError.verificarUltimoValorEmitido("El tag no tiene autenticación activada")
                }

                @Test
                fun queda_como_no_habilitado_para_codificar_y_solo_interactua_con_las_operaciones_nfc_leyendo_el_tag()
                {
                    verify(mockOperacionesCompuestas).leerTag()
                }

                @Test
                fun estado_emite_el_estado_del_tag_luego_esperando_tag_luego_codificando_y_finalmente_el_estado_del_tag_inicial()
                {
                    observadorEstado.assertValues(
                            ItemACodificarUI.Estado.EtapaSesionManilla.SinCodificar,
                            ItemACodificarUI.Estado.EtapaSesionManilla.EsperandoTag,
                            ItemACodificarUI.Estado.Codificando,
                            ItemACodificarUI.Estado.EtapaSesionManilla.EsperandoTag
                                                 )
                }
            }

            @Nested
            inner class CuandoLecturaEsErrorDeLectura
            {
                private val observadorEstado = modelo.estado.test()
                private val observadorMensajesDeError = modelo.mensajesDeError.test()

                @BeforeEach
                fun mockearResultadoDeLecturaYEmitir()
                {
                    mockOperacionesCompuestas.retornoDeLeerTag = ResultadoLecturaNFC.ErrorDeLectura(Exception())
                    emisorResultadoNFCExitoso.onNext(ResultadoNFC.Exitoso(mockOperacionesCompuestas))
                }

                @Test
                fun mensajes_de_error_emite_mensaje_diciendo_que_el_tag_no_tiene_autenticacion_activada()
                {
                    observadorMensajesDeError.verificarUltimoValorEmitido("Error al leer el tag")
                }

                @Test
                fun queda_como_no_habilitado_para_codificar_y_solo_interactua_con_las_operaciones_nfc_leyendo_el_tag()
                {
                    verify(mockOperacionesCompuestas).leerTag()
                }

                @Test
                fun estado_emite_el_estado_del_tag_luego_esperando_tag_luego_codificando_y_finalmente_el_estado_del_tag_inicial()
                {
                    observadorEstado.assertValues(
                            ItemACodificarUI.Estado.EtapaSesionManilla.SinCodificar,
                            ItemACodificarUI.Estado.EtapaSesionManilla.EsperandoTag,
                            ItemACodificarUI.Estado.Codificando,
                            ItemACodificarUI.Estado.EtapaSesionManilla.EsperandoTag
                                                 )
                }
            }

            @Nested
            inner class CuandoLecturaEsTagLeido
            {
                private val observadorEstado = modelo.estado.test()
                private val observadorMensajesDeError = modelo.mensajesDeError.test()

                @BeforeEach
                fun mockearResultadoDeLecturaYEmitir()
                {
                    mockOperacionesCompuestas.retornoDeLeerTag = ResultadoLecturaNFC.TagLeido(byteArrayOf())
                    emisorResultadoNFCExitoso.onNext(ResultadoNFC.Exitoso(mockOperacionesCompuestas))
                }

                @Test
                fun mensajes_de_error_emite_mensaje_diciendo_que_el_tag_no_tiene_autenticacion_activada()
                {
                    observadorMensajesDeError.verificarUltimoValorEmitido("El tag leído ya contiene datos")
                }

                @Test
                fun queda_como_no_habilitado_para_codificar_y_solo_interactua_con_las_operaciones_nfc_leyendo_el_tag()
                {
                    verify(mockOperacionesCompuestas).leerTag()
                }

                @Test
                fun estado_emite_el_estado_del_tag_luego_esperando_tag_luego_codificando_y_finalmente_el_estado_del_tag_inicial()
                {
                    observadorEstado.assertValues(
                            ItemACodificarUI.Estado.EtapaSesionManilla.SinCodificar,
                            ItemACodificarUI.Estado.EtapaSesionManilla.EsperandoTag,
                            ItemACodificarUI.Estado.Codificando,
                            ItemACodificarUI.Estado.EtapaSesionManilla.EsperandoTag
                                                 )
                }
            }

        }
    }

    @Nested
    inner class IntentarActivarSesion
    {
        private val emisorResultadoNFCExitoso = PublishProcessor.create<ResultadoNFC.Exitoso>()

        private val modelo = ItemACodificar(
                sesionDeManilla,
                creditosACodificar,
                emisorResultadoNFCExitoso,
                apiSesionDeManilla,
                Schedulers.trampoline()
                                           )

        @Nested
        inner class EnEstadoDiferenteACodificada
        {
            private val observadorEstado = modelo.estado.test()

            @BeforeEach
            fun forzarAEstadoActivado()
            {
                val observadorDeEstado = modelo.estado.test()
                modelo.habilitarParaCodificacion()
                mockOperacionesCompuestas.retornoDeLeerTag = ResultadoLecturaNFC.TagVacio
                mockOperacionesCompuestas.retornoDeEscribirTag = true
                emisorResultadoNFCExitoso.onNext(ResultadoNFC.Exitoso(mockOperacionesCompuestas))

                observadorDeEstado.verificarUltimoValorEmitido(ItemACodificarUI.Estado.EtapaSesionManilla.Activada)
            }

            @Test
            fun retorna_false()
            {
                assertFalse(modelo.intentarActivarSesion())
            }

            @Test
            fun no_cambia_el_estado()
            {
                modelo.intentarActivarSesion()
                observadorEstado.assertValues(
                        ItemACodificarUI.Estado.EtapaSesionManilla.SinCodificar,
                        ItemACodificarUI.Estado.EtapaSesionManilla.EsperandoTag,
                        ItemACodificarUI.Estado.Codificando,
                        ItemACodificarUI.Estado.EtapaSesionManilla.Codificada(OperacionesCompuestasMockeadas.UIDTagPrueba),
                        ItemACodificarUI.Estado.Activando,
                        ItemACodificarUI.Estado.EtapaSesionManilla.Activada
                                             )
            }
        }

        @Nested
        inner class EnEstadoCodificada
        {
            @BeforeEach
            fun forzarAEstadoCodificadoYHabilitarParaCodificacion()
            {
                val observadorDeEstado = modelo.estado.test()
                modelo.habilitarParaCodificacion()
                mockOperacionesCompuestas.retornoDeLeerTag = ResultadoLecturaNFC.TagVacio
                mockOperacionesCompuestas.retornoDeEscribirTag = true

                doReturn(RespuestaVacia.Error.Timeout)
                    .`when`(apiSesionDeManilla)
                    .actualizarCampos(anyLong(), cualquiera())

                emisorResultadoNFCExitoso.onNext(ResultadoNFC.Exitoso(mockOperacionesCompuestas))

                observadorDeEstado.verificarUltimoValorEmitido(ItemACodificarUI.Estado.EtapaSesionManilla.Codificada(OperacionesCompuestasMockeadas.UIDTagPrueba))
            }

            @Test
            fun retorna_true()
            {
                assertTrue(modelo.intentarActivarSesion())
            }

            @Nested
            inner class SiLaActivacion
            {
                private lateinit var observadorEstado: TestObserver<ItemACodificarUI.Estado>
                private lateinit var observadorMensajesDeError: TestObserver<String>

                @Nested
                inner class FueExitosa
                {
                    @BeforeEach
                    fun mockearRespuestaPatchEInstanciarObservadores()
                    {
                        observadorEstado = modelo.estado.test()
                        observadorMensajesDeError = modelo.mensajesDeError.test()

                        doReturn(RespuestaVacia.Exitosa)
                            .`when`(apiSesionDeManilla)
                            .actualizarCampos(anyLong(), cualquiera())

                        modelo.intentarActivarSesion()
                    }

                    @Test
                    fun el_estado_emitido_es_activado()
                    {
                        observadorEstado.assertValues(
                                ItemACodificarUI.Estado.EtapaSesionManilla.Codificada(OperacionesCompuestasMockeadas.UIDTagPrueba),
                                ItemACodificarUI.Estado.Activando,
                                ItemACodificarUI.Estado.EtapaSesionManilla.Activada
                                                     )
                    }

                    @Test
                    fun mensajes_de_error_no_emite_nada()
                    {
                        observadorMensajesDeError.assertEmpty()
                    }
                }

                @Nested
                inner class FalloCon
                {
                    @BeforeEach
                    fun instanciarObservadores()
                    {
                        observadorEstado = modelo.estado.test()
                        observadorMensajesDeError = modelo.mensajesDeError.test()
                    }

                    @Nested
                    inner class UnErrorTimeout
                    {
                        @BeforeEach
                        fun mockearRespuestaPatch()
                        {
                            doReturn(RespuestaVacia.Error.Timeout)
                                .`when`(apiSesionDeManilla)
                                .actualizarCampos(anyLong(), cualquiera())

                            modelo.intentarActivarSesion()
                        }

                        @Test
                        fun el_estado_emite_activando_y_luego_vuelve_a_codificado()
                        {
                            observadorEstado.assertValues(
                                    ItemACodificarUI.Estado.EtapaSesionManilla.Codificada(OperacionesCompuestasMockeadas.UIDTagPrueba),
                                    ItemACodificarUI.Estado.Activando,
                                    ItemACodificarUI.Estado.EtapaSesionManilla.Codificada(OperacionesCompuestasMockeadas.UIDTagPrueba)
                                                         )
                        }

                        @Test
                        fun mensajes_de_error_emite_mensaje_diciendo_que_hubo_un_timeout_al_intentar_activar_la_sesion()
                        {
                            observadorMensajesDeError.assertValue("Tiempo de espera al servidor agotado. No se pudo activar la sesión asociada con el servidor")
                        }
                    }

                    @Nested
                    inner class UnErrorRed
                    {
                        @BeforeEach
                        fun mockearRespuestaPatch()
                        {
                            doReturn(RespuestaVacia.Error.Red(IOException("Excepción de red")))
                                .`when`(apiSesionDeManilla)
                                .actualizarCampos(anyLong(), cualquiera())

                            modelo.intentarActivarSesion()
                        }

                        @Test
                        fun el_estado_emite_activando_y_luego_vuelve_a_codificado()
                        {
                            observadorEstado.assertValues(
                                    ItemACodificarUI.Estado.EtapaSesionManilla.Codificada(OperacionesCompuestasMockeadas.UIDTagPrueba),
                                    ItemACodificarUI.Estado.Activando,
                                    ItemACodificarUI.Estado.EtapaSesionManilla.Codificada(OperacionesCompuestasMockeadas.UIDTagPrueba)
                                                         )
                        }

                        @Test
                        fun mensajes_de_error_emite_mensaje_diciendo_que_hubo_un_error_de_red()
                        {
                            observadorMensajesDeError.assertValue("Hubo un error en la conexión y no fue posible contactar al servidor")
                        }
                    }

                    @Nested
                    inner class UnErrorBack
                    {
                        @Nested
                        inner class SesionYaTieneTagAsociado
                        {
                            @BeforeEach
                            fun mockearRespuestaPatch()
                            {
                                val error = ErrorDePeticion(SesionDeManillaDTO.CodigosError.SESION_YA_TIENE_TAG_ASOCIADO, "no importa")

                                doReturn(RespuestaVacia.Error.Back(400, error))
                                    .`when`(apiSesionDeManilla)
                                    .actualizarCampos(anyLong(), cualquiera())

                                modelo.intentarActivarSesion()
                            }

                            @Test
                            fun el_estado_emite_activando_y_luego_a_activado()
                            {
                                observadorEstado.assertValues(
                                        ItemACodificarUI.Estado.EtapaSesionManilla.Codificada(OperacionesCompuestasMockeadas.UIDTagPrueba),
                                        ItemACodificarUI.Estado.Activando,
                                        ItemACodificarUI.Estado.EtapaSesionManilla.Activada
                                                             )
                            }

                            @Test
                            fun mensajes_de_error_emite_mensaje_diciendo_que_ya_esta_activa()
                            {
                                observadorMensajesDeError.assertValue("La sesión ya se encuentra activa y tiene un tag asociado")
                            }
                        }

                        @Nested
                        inner class Desconocido
                        {
                            @BeforeEach
                            fun mockearRespuestaPatch()
                            {
                                doReturn(RespuestaVacia.Error.Back(400, ErrorDePeticion(-1, "mensaje del error")))
                                    .`when`(apiSesionDeManilla)
                                    .actualizarCampos(anyLong(), cualquiera())

                                modelo.intentarActivarSesion()
                            }

                            @Test
                            fun el_estado_emite_activando_y_luego_vuelve_a_codificado()
                            {
                                observadorEstado.assertValues(
                                        ItemACodificarUI.Estado.EtapaSesionManilla.Codificada(OperacionesCompuestasMockeadas.UIDTagPrueba),
                                        ItemACodificarUI.Estado.Activando,
                                        ItemACodificarUI.Estado.EtapaSesionManilla.Codificada(OperacionesCompuestasMockeadas.UIDTagPrueba)
                                                             )
                            }

                            @Test
                            fun mensajes_de_error_emite_mensaje_diciendo_que_hubo_un_error_de_back_y_el_mensaje_asociado()
                            {
                                observadorMensajesDeError.assertValue("Error en petición: mensaje del error")
                            }
                        }
                    }
                }
            }
        }
    }
}