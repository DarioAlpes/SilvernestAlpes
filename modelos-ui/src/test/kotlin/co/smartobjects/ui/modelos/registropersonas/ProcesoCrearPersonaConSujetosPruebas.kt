package co.smartobjects.ui.modelos.registropersonas

import co.smartobjects.entidades.personas.DocumentoCompleto
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.personas.PersonaConFamiliares
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.personas.PersonasAPI
import co.smartobjects.red.modelos.ErrorDePeticion
import co.smartobjects.red.modelos.personas.PersonaConFamiliaresDTO
import co.smartobjects.ui.modelos.*
import co.smartobjects.utilidades.Opcional
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import io.reactivex.Notification
import io.reactivex.subjects.BehaviorSubject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.threeten.bp.LocalDate
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DisplayName("ProcesoCrearPersonaConSujetos")
internal class ProcesoCrearPersonaConSujetosPruebas : PruebasModelosRxBase()
{
    companion object
    {
        private const val ID_CLIENTE = 10L
    }

    // Para estas pruebas se va a suponer que PersonaUI funciona correctamente
    private val mockPersona = mockConDefaultAnswer(PersonaUI::class.java)
    private val mockApiPersonas = mockConDefaultAnswer(PersonasAPI::class.java)

    private val personaPruebasSinId = Persona(
            ID_CLIENTE,
            null,
            "Nombre Valido",
            Persona.TipoDocumento.CC,
            "1234",
            Persona.Genero.MASCULINO,
            LocalDate.now(),
            Persona.Categoria.A,
            Persona.Afiliacion.COTIZANTE,
            null,
            "empresa",
            "0",
            Persona.Tipo.NO_AFILIADO
                                             )

    private val familiarPruebas = Persona(
            ID_CLIENTE,
            null,
            "Preubas",
            Persona.TipoDocumento.TI,
            "567",
            Persona.Genero.FEMENINO,
            LocalDate.now(),
            Persona.Categoria.C,
            Persona.Afiliacion.BENEFICIARIO,
            null,
            "empresa",
            "0",
            Persona.Tipo.NO_AFILIADO
                                         )

    private val documentoPersonaPruebas = DocumentoCompleto(personaPruebasSinId)
    private val personaEsperadaPruebas = personaPruebasSinId.copiar(id = 20)
    private val personaLeida =
            Persona(
                    1,
                    2,
                    "persona de lectura",
                    Persona.TipoDocumento.NUIP,
                    "213-456-789",
                    Persona.Genero.MASCULINO,
                    LocalDate.now(ZONA_HORARIA_POR_DEFECTO).minusYears(25),
                    Persona.Categoria.D,
                    Persona.Afiliacion.BENEFICIARIO,
                    false,
                    null,
                    "empresa",
                    "0",
                    Persona.Tipo.NO_AFILIADO
                   )
    private val documentoPersonaLeida = DocumentoCompleto(personaLeida)
    private val personaCombinadaConLecturaEsperada =
            personaLeida
                .copiar(
                        id = personaEsperadaPruebas.id,
                        categoria = personaEsperadaPruebas.categoria,
                        afiliacion = personaEsperadaPruebas.afiliacion
                       )

    //Nota: No se mockean observables para asegurar que no se usen directamente
    private val sujetoEsPersonaValida = BehaviorSubject.create<Boolean>()
    private val sujetoTipoDocumento = BehaviorSubject.create<Persona.TipoDocumento>()
    private val sujetoNumeroDocumento = BehaviorSubject.create<Notification<String>>()

    @BeforeEach
    fun mockearNumeroDocumento()
    {
        doReturn(sujetoNumeroDocumento)
            .`when`(mockPersona)
            .numeroDocumento

        doReturn(sujetoTipoDocumento)
            .`when`(mockPersona)
            .tipoDocumento

        doReturn(sujetoEsPersonaValida)
            .`when`(mockPersona)
            .esPersonaValida

        doNothing()
            .`when`(mockPersona)
            .asignarPersona(personaEsperadaPruebas)

        doNothing()
            .`when`(mockPersona)
            .asignarPersona(personaLeida)

        doNothing()
            .`when`(mockPersona)
            .asignarPersona(personaLeida.copiar(afiliacion = Persona.Afiliacion.NO_AFILIADO, categoria = Persona.Categoria.D))

        doNothing()
            .`when`(mockPersona)
            .asignarPersona(personaCombinadaConLecturaEsperada)

        doNothing()
            .`when`(mockPersona)
            .asignarPersona(personaCombinadaConLecturaEsperada.copiar(afiliacion = Persona.Afiliacion.NO_AFILIADO, categoria = Persona.Categoria.D))

        doReturn(ID_CLIENTE)
            .`when`(mockPersona)
            .idCliente
    }

    private fun mockearDarPersona(persona: Persona)
    {
        doReturn(persona)
            .`when`(mockPersona)
            .aPersona()
    }

    private fun mockearDarDocumentoCompleto()
    {
        doReturn(documentoPersonaPruebas)
            .`when`(mockPersona)
            .darDocumentoCompleto()
    }

    private fun mockearFinalizarProceso()
    {
        doNothing()
            .`when`(mockPersona)
            .finalizarProceso()
    }

    private fun mockearRespuestaDeRedExitosaAlConsultarSinFamiliares()
    {
        doReturn(RespuestaIndividual.Exitosa(PersonaConFamiliares(personaEsperadaPruebas, emptySet())))
            .`when`(mockApiPersonas)
            .consultarPorDocumento(documentoPersonaPruebas)

        doReturn(RespuestaIndividual.Exitosa(PersonaConFamiliares(personaEsperadaPruebas, emptySet())))
            .`when`(mockApiPersonas)
            .consultarPorDocumento(documentoPersonaLeida)
    }

    private fun mockearRespuestaDeRedExitosaAlConsultarConFamiliares()
    {
        doReturn(RespuestaIndividual.Exitosa(PersonaConFamiliares(personaEsperadaPruebas, setOf(familiarPruebas))))
            .`when`(mockApiPersonas)
            .consultarPorDocumento(documentoPersonaPruebas)

        doReturn(RespuestaIndividual.Exitosa(PersonaConFamiliares(personaEsperadaPruebas, setOf(familiarPruebas))))
            .`when`(mockApiPersonas)
            .consultarPorDocumento(documentoPersonaLeida)
    }

    private fun mockearRespuestaDeRedInvalidaAlConsultar(errorEsperado: IllegalStateException)
    {
        doThrow(errorEsperado)
            .`when`(mockApiPersonas)
            .consultarPorDocumento(documentoPersonaPruebas)

        doThrow(errorEsperado)
            .`when`(mockApiPersonas)
            .consultarPorDocumento(documentoPersonaLeida)
    }

    private fun mockearRespuestaDeRedErrorTimeoutAlConsultar()
    {
        doReturn(RespuestaIndividual.Error.Timeout<Persona>())
            .`when`(mockApiPersonas)
            .consultarPorDocumento(documentoPersonaPruebas)

        doReturn(RespuestaIndividual.Error.Timeout<Persona>())
            .`when`(mockApiPersonas)
            .consultarPorDocumento(documentoPersonaLeida)
    }

    private fun mockearRespuestaDeRedErrorRedAlConsultar()
    {
        doReturn(RespuestaIndividual.Error.Red<Persona>(IOException("Error")))
            .`when`(mockApiPersonas)
            .consultarPorDocumento(documentoPersonaPruebas)

        doReturn(RespuestaIndividual.Error.Red<Persona>(IOException("Error")))
            .`when`(mockApiPersonas)
            .consultarPorDocumento(documentoPersonaLeida)
    }

    private fun mockearRespuestaDeRedErrorBackAlConsultar(mensajeError: String)
    {
        doReturn(RespuestaIndividual.Error.Back<Persona>(400, ErrorDePeticion(100, mensajeError)))
            .`when`(mockApiPersonas)
            .consultarPorDocumento(documentoPersonaPruebas)

        doReturn(RespuestaIndividual.Error.Back<Persona>(400, ErrorDePeticion(100, mensajeError)))
            .`when`(mockApiPersonas)
            .consultarPorDocumento(documentoPersonaLeida)
    }

    private fun mockearRespuestaDeRedErrorBackPersonaNoExisteAlConsultar()
    {
        val errorBack =
                RespuestaIndividual.Error.Back<PersonaConFamiliares>(
                        404,
                        ErrorDePeticion(PersonaConFamiliaresDTO.CodigosError.NO_EXISTE, "No existe")
                                                                    )

        doReturn(errorBack)
            .`when`(mockApiPersonas)
            .consultarPorDocumento(documentoPersonaPruebas)

        doReturn(errorBack)
            .`when`(mockApiPersonas)
            .consultarPorDocumento(documentoPersonaLeida)
    }

    private fun mockearRespuestaDeRedExitosaAlCrear()
    {
        doReturn(RespuestaIndividual.Exitosa(personaEsperadaPruebas))
            .`when`(mockApiPersonas)
            .crear(personaPruebasSinId)
    }

    private fun mockearRespuestaDeRedInvalidaAlCrear(errorEsperado: IllegalStateException)
    {
        doThrow(errorEsperado)
            .`when`(mockApiPersonas)
            .crear(personaPruebasSinId)
    }

    private fun mockearRespuestaDeRedErrorTimeoutAlCrear()
    {
        doReturn(RespuestaIndividual.Error.Timeout<Persona>())
            .`when`(mockApiPersonas)
            .crear(personaPruebasSinId)
    }

    private fun mockearRespuestaDeRedErrorRedAlCrear()
    {
        doReturn(RespuestaIndividual.Error.Red<Persona>(IOException("Error")))
            .`when`(mockApiPersonas)
            .crear(personaPruebasSinId)
    }

    private fun mockearRespuestaDeRedErrorBackAlCrear(mensajeError: String)
    {
        doReturn(RespuestaIndividual.Error.Back<Persona>(400, ErrorDePeticion(100, mensajeError)))
            .`when`(mockApiPersonas)
            .crear(personaPruebasSinId)
    }

    private fun mockearRespuestaDeRedExitosaAlActualizar()
    {
        doReturn(RespuestaIndividual.Exitosa(personaEsperadaPruebas))
            .`when`(mockApiPersonas)
            .actualizar(personaEsperadaPruebas)
    }

    private fun mockearRespuestaDeRedInvalidaAlActualizar(errorEsperado: IllegalStateException)
    {
        doThrow(errorEsperado)
            .`when`(mockApiPersonas)
            .actualizar(personaEsperadaPruebas)
    }

    private fun mockearRespuestaDeRedErrorTimeoutAlActualizar()
    {
        doReturn(RespuestaIndividual.Error.Timeout<Persona>())
            .`when`(mockApiPersonas)
            .actualizar(personaEsperadaPruebas)
    }

    private fun mockearRespuestaDeRedErrorRedAlActualizar()
    {
        doReturn(RespuestaIndividual.Error.Red<Persona>(IOException("Error")))
            .`when`(mockApiPersonas)
            .actualizar(personaEsperadaPruebas)
    }

    private fun mockearRespuestaDeRedErrorBackAlActualizar(mensajeError: String)
    {
        doReturn(RespuestaIndividual.Error.Back<Persona>(400, ErrorDePeticion(100, mensajeError)))
            .`when`(mockApiPersonas)
            .actualizar(personaEsperadaPruebas)
    }

    @Nested
    inner class InicializadoExitosamente
    {
        private val proceso: ProcesoCrearPersona by lazy { ProcesoCrearPersonaConSujetos(mockPersona, mockApiPersonas, schedulerThreadActual) }

        @Nested
        inner class PersonaCreada
        {
            private val testPersonaCreada by lazy { proceso.personaCreada.test() }

            @Test
            fun empieza_sin_valor()
            {
                testPersonaCreada.assertValueCount(0)
            }
        }

        @Nested
        inner class Estado
        {
            private val testEstado by lazy { proceso.estado.test() }

            @Test
            fun empieza_con_valor_ESPERANDO_DATOS()
            {
                testEstado.assertValue(ProcesoCrearPersona.Estado.ESPERANDO_DATOS)
                testEstado.assertValueCount(1)
            }
        }

        @Nested
        inner class PuedeCrearPersona
        {
            private val testPuedeCrearPersona by lazy { proceso.puedeCrearPersona.test() }

            @Test
            fun empieza_con_valor_false()
            {
                testPuedeCrearPersona.assertValue(false)
                testPuedeCrearPersona.assertValueCount(1)
            }

            @Nested
            @Suppress("ClassName")
            inner class EnEstadoESPERANDO_DATOS
            {
                @Test
                fun emite_true_cuando_esPersonaValida_en_persona_cambia_a_true()
                {
                    testPuedeCrearPersona.assertValue(false)
                    testPuedeCrearPersona.assertValueCount(1)
                    sujetoEsPersonaValida.onNext(true)
                    testPuedeCrearPersona.assertValueCount(2)
                    testPuedeCrearPersona.assertValueAt(1, true)
                }

                @Test
                fun emite_false_cuando_esPersonaValida_en_persona_cambia_a_false()
                {
                    testPuedeCrearPersona.assertValue(false)
                    testPuedeCrearPersona.assertValueCount(1)
                    sujetoEsPersonaValida.onNext(true)
                    testPuedeCrearPersona.assertValueCount(2)
                    testPuedeCrearPersona.assertValueAt(1, true)
                    sujetoEsPersonaValida.onNext(false)
                    testPuedeCrearPersona.assertValueCount(3)
                    testPuedeCrearPersona.assertValueAt(2, false)
                }
            }
        }

        @Nested
        inner class DebeConsultarPersona
        {
            @Test
            fun empieza_con_valor_false()
            {
                assertFalse(proceso.debeConsultarPersona)
            }

            @Nested
            @Suppress("ClassName")
            inner class EnEstadoESPERANDO_DATOS
            {
                @Test
                fun cambia_a_true_cuando_numeroDocumento_y_tipoDocumento_en_persona_cambian_a_valores_validos()
                {
                    sujetoTipoDocumento.onNext(Persona.TipoDocumento.CC)
                    sujetoNumeroDocumento.onNext(Notification.createOnNext("123"))
                    assertTrue(proceso.debeConsultarPersona)
                }

                @Test
                fun se_mantiene_en_true_cuando_numeroDocumento_y_tipoDocumento_en_persona_cambian_a_valores_validos_y_se_vuelve_a_cambiar_numeroDocumento_a_valor_valido()
                {
                    sujetoTipoDocumento.onNext(Persona.TipoDocumento.CC)
                    sujetoNumeroDocumento.onNext(Notification.createOnNext("123"))
                    assertTrue(proceso.debeConsultarPersona)
                    sujetoNumeroDocumento.onNext(Notification.createOnNext("456"))
                    assertTrue(proceso.debeConsultarPersona)
                }

                @Test
                fun se_mantiene_en_true_cuando_numeroDocumento_y_tipoDocumento_en_persona_cambian_a_valores_validos_y_se_vuelve_a_cambiar_tipoDocumento_a_valor_valido()
                {
                    sujetoTipoDocumento.onNext(Persona.TipoDocumento.CC)
                    sujetoNumeroDocumento.onNext(Notification.createOnNext("123"))
                    assertTrue(proceso.debeConsultarPersona)
                    sujetoTipoDocumento.onNext(Persona.TipoDocumento.TI)
                    assertTrue(proceso.debeConsultarPersona)
                }

                @Test
                fun se_mantiene_en_true_cuando_numeroDocumento_y_tipoDocumento_en_persona_cambian_a_valores_validos_y_se_vuelve_a_cambiar_numeroDocumento_a_mismo_valor()
                {
                    sujetoTipoDocumento.onNext(Persona.TipoDocumento.CC)
                    sujetoNumeroDocumento.onNext(Notification.createOnNext("123"))
                    assertTrue(proceso.debeConsultarPersona)
                    sujetoNumeroDocumento.onNext(Notification.createOnNext("123"))
                    assertTrue(proceso.debeConsultarPersona)
                }

                @Test
                fun se_mantiene_en_true_cuando_numeroDocumento_y_tipoDocumento_en_persona_cambian_a_valores_validos_y_se_vuelve_a_cambiar_tipoDocumento_a_mismo_valor()
                {
                    sujetoTipoDocumento.onNext(Persona.TipoDocumento.CC)
                    sujetoNumeroDocumento.onNext(Notification.createOnNext("123"))
                    assertTrue(proceso.debeConsultarPersona)
                    sujetoTipoDocumento.onNext(Persona.TipoDocumento.CC)
                    assertTrue(proceso.debeConsultarPersona)
                }

                @Test
                fun se_mantiene_en_false_cuando_tipoDocumento_cambia_a_valor_valido_pero_numero_documento_cambia_a_valor_invalido_en_persona()
                {
                    assertFalse(proceso.debeConsultarPersona)
                    sujetoTipoDocumento.onNext(Persona.TipoDocumento.CC)
                    sujetoNumeroDocumento.onNext(Notification.createOnError(Exception()))
                    assertFalse(proceso.debeConsultarPersona)
                }

                @Test
                fun cambia_a_true_y_luego_a_false_cuando_numeroDocumento_y_tipoDocumento_en_persona_cambian_a_valores_validos_y_luego_numero_documento_cambia_a_valor_invalido()
                {
                    sujetoTipoDocumento.onNext(Persona.TipoDocumento.CC)
                    sujetoNumeroDocumento.onNext(Notification.createOnNext("123"))
                    assertTrue(proceso.debeConsultarPersona)
                    sujetoNumeroDocumento.onNext(Notification.createOnError(Exception()))
                    assertFalse(proceso.debeConsultarPersona)
                }

                @Test
                fun cambia_a_true_y_luego_a_false_y_nuevamente_a_true_cuando_numeroDocumento_y_tipoDocumento_en_persona_cambian_a_valores_validos_y_luego_numero_documento_cambia_a_valor_invalido_y_luego_a_mismo_valor_valido()
                {
                    sujetoTipoDocumento.onNext(Persona.TipoDocumento.CC)
                    sujetoNumeroDocumento.onNext(Notification.createOnNext("123"))
                    assertTrue(proceso.debeConsultarPersona)
                    sujetoNumeroDocumento.onNext(Notification.createOnError(Exception()))
                    assertFalse(proceso.debeConsultarPersona)
                    sujetoNumeroDocumento.onNext(Notification.createOnNext("123"))
                    assertTrue(proceso.debeConsultarPersona)
                }

                @Test
                fun cambia_a_true_y_luego_a_false_y_nuevamente_a_true_cuando_numeroDocumento_y_tipoDocumento_en_persona_cambian_a_valores_validos_y_luego_numero_documento_cambia_a_valor_invalido_y_luego_a_nuevo_valor_valido()
                {
                    sujetoTipoDocumento.onNext(Persona.TipoDocumento.CC)
                    sujetoNumeroDocumento.onNext(Notification.createOnNext("123"))
                    assertTrue(proceso.debeConsultarPersona)
                    sujetoNumeroDocumento.onNext(Notification.createOnError(Exception()))
                    assertFalse(proceso.debeConsultarPersona)
                    sujetoNumeroDocumento.onNext(Notification.createOnNext("456"))
                    assertTrue(proceso.debeConsultarPersona)
                }

                @Test
                fun cambia_a_false_al_llamar_consultarPersonaPorDocumento_con_documento_valido_cuando_al_respuesta_es_exitosa()
                {
                    mockearDarDocumentoCompleto()
                    mockearRespuestaDeRedExitosaAlConsultarSinFamiliares()
                    sujetoTipoDocumento.onNext(personaPruebasSinId.tipoDocumento)
                    sujetoNumeroDocumento.onNext(Notification.createOnNext(personaPruebasSinId.numeroDocumento))
                    assertTrue(proceso.debeConsultarPersona)
                    proceso.intentarConsultarPersonaPorDocumento()
                    assertFalse(proceso.debeConsultarPersona)
                }

                @Test
                fun cambia_a_false_al_llamar_consultarPersonaPorDocumento_con_documento_valido_cuando_la_respuesta_es_persona_no_existe()
                {
                    doNothing().`when`(mockPersona).cambiarAfiliacion(cualquiera())
                    doNothing().`when`(mockPersona).cambiarCategoria(cualquiera())

                    mockearDarDocumentoCompleto()
                    mockearRespuestaDeRedErrorBackPersonaNoExisteAlConsultar()

                    sujetoTipoDocumento.onNext(personaPruebasSinId.tipoDocumento)
                    sujetoNumeroDocumento.onNext(Notification.createOnNext(personaPruebasSinId.numeroDocumento))
                    assertTrue(proceso.debeConsultarPersona)
                    proceso.intentarConsultarPersonaPorDocumento()
                    assertFalse(proceso.debeConsultarPersona)
                }

                @Test
                fun se_mantiene_en_true_al_llamar_consultarPersonaPorDocumento_con_documento_valido_cuando_al_respuesta_es_ErrorBack()
                {
                    mockearDarDocumentoCompleto()
                    mockearRespuestaDeRedErrorBackAlConsultar("Error de pruebas")
                    sujetoTipoDocumento.onNext(personaPruebasSinId.tipoDocumento)
                    sujetoNumeroDocumento.onNext(Notification.createOnNext(personaPruebasSinId.numeroDocumento))
                    assertTrue(proceso.debeConsultarPersona)
                    proceso.intentarConsultarPersonaPorDocumento()
                    assertTrue(proceso.debeConsultarPersona)
                }

                @Test
                fun se_mantiene_en_true_al_llamar_consultarPersonaPorDocumento_con_documento_valido_cuando_lanza_IllegalStateException()
                {
                    mockearDarDocumentoCompleto()
                    val errorEsperado = IllegalStateException()
                    erroresEsperados.add(errorEsperado)
                    mockearRespuestaDeRedInvalidaAlConsultar(errorEsperado)
                    sujetoTipoDocumento.onNext(personaPruebasSinId.tipoDocumento)
                    sujetoNumeroDocumento.onNext(Notification.createOnNext(personaPruebasSinId.numeroDocumento))
                    assertTrue(proceso.debeConsultarPersona)
                    proceso.intentarConsultarPersonaPorDocumento()
                    assertTrue(proceso.debeConsultarPersona)
                }

                @Test
                fun se_mantiene_en_true_al_llamar_consultarPersonaPorDocumento_con_documento_valido_cuando_al_respuesta_es_ErrorTimeout()
                {
                    mockearDarDocumentoCompleto()
                    mockearRespuestaDeRedErrorTimeoutAlConsultar()
                    sujetoTipoDocumento.onNext(personaPruebasSinId.tipoDocumento)
                    sujetoNumeroDocumento.onNext(Notification.createOnNext(personaPruebasSinId.numeroDocumento))
                    assertTrue(proceso.debeConsultarPersona)
                    proceso.intentarConsultarPersonaPorDocumento()
                    assertTrue(proceso.debeConsultarPersona)
                }

                @Test
                fun se_mantiene_en_true_al_llamar_consultarPersonaPorDocumento_con_documento_valido_cuando_al_respuesta_es_ErrorRed()
                {
                    mockearDarDocumentoCompleto()
                    mockearRespuestaDeRedErrorRedAlConsultar()
                    sujetoTipoDocumento.onNext(personaPruebasSinId.tipoDocumento)
                    sujetoNumeroDocumento.onNext(Notification.createOnNext(personaPruebasSinId.numeroDocumento))
                    assertTrue(proceso.debeConsultarPersona)
                    proceso.intentarConsultarPersonaPorDocumento()
                    assertTrue(proceso.debeConsultarPersona)
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
        inner class IntentarCrearPersona
        {
            @Nested
            @Suppress("ClassName")
            inner class EnEstadoESPERANDO_DATOS
            {
                @Test
                fun retorna_MODELO_EN_ESTADO_INVALIDO_cuando_persona_no_a_emitido_valor_sobre_esPersonaValida()
                {
                    assertEquals(ResultadoAccionUI.MODELO_EN_ESTADO_INVALIDO, proceso.intentarCrearPersona())
                }

                @Test
                fun retorna_MODELO_EN_ESTADO_INVALIDO_cuando_persona_emitio_false_sobre_esPersonaValida()
                {
                    sujetoEsPersonaValida.onNext(false)
                    assertEquals(ResultadoAccionUI.MODELO_EN_ESTADO_INVALIDO, proceso.intentarCrearPersona())
                }

                @Nested
                inner class ConObservalePersonaValidaTrueCuandoAPersonaLanzaExcepcion
                {
                    @BeforeEach
                    fun emitirEsPersonaValidaYFallarEnAPersona()
                    {
                        doThrow(IllegalStateException("Error"))
                            .`when`(mockPersona)
                            .aPersona()
                        sujetoEsPersonaValida.onNext(true)
                    }

                    @Test
                    fun retorna_OBSERVABLES_EN_ESTADO_INVALIDO()
                    {
                        assertEquals(ResultadoAccionUI.OBSERVABLES_EN_ESTADO_INVALIDO, proceso.intentarCrearPersona())
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
                            proceso.intentarCrearPersona()
                            testErrorGlobal.assertValueCount(2)
                            testErrorGlobal.assertValueAt(1, Opcional.De("Persona inválida"))
                        }
                    }
                }

                @Nested
                inner class ConPersonaValida
                {
                    @BeforeEach
                    private fun emitirEsPersonaValidaYMockearFinalizarProceso()
                    {
                        mockearFinalizarProceso()
                        sujetoEsPersonaValida.onNext(true)
                    }

                    @Nested
                    inner class ConIdNoNulo
                    {
                        @BeforeEach
                        private fun emitirPersonaConId()
                        {
                            mockearDarPersona(personaEsperadaPruebas)
                            mockearFinalizarProceso()
                            sujetoEsPersonaValida.onNext(true)
                        }

                        @Test
                        fun llama_a_actualizar_y_no_a_crear_en_api_de_red()
                        {
                            mockearRespuestaDeRedExitosaAlActualizar()
                            proceso.intentarCrearPersona()
                            verify(mockApiPersonas).actualizar(personaEsperadaPruebas)
                            verify(mockApiPersonas, times(0)).crear(personaEsperadaPruebas)
                        }

                        @Test
                        fun retorna_ACCION_INICIADA_cuando_api_personas_retorna_respuesta_Exitosa_en_actualizar()
                        {
                            mockearRespuestaDeRedExitosaAlActualizar()
                            assertEquals(ResultadoAccionUI.ACCION_INICIADA, proceso.intentarCrearPersona())
                        }

                        @Test
                        fun retorna_ACCION_INICIADA_cuando_api_personas_lanza_IllegalStateException_en_actualizar()
                        {
                            val errorEsperado = IllegalStateException()
                            erroresEsperados.add(errorEsperado)
                            mockearRespuestaDeRedInvalidaAlActualizar(errorEsperado)
                            assertEquals(ResultadoAccionUI.ACCION_INICIADA, proceso.intentarCrearPersona())
                        }

                        @Test
                        fun retorna_ACCION_INICIADA_cuando_api_personas_retorna_respuesta_ErrorTimeout_en_actualizar()
                        {
                            mockearRespuestaDeRedErrorTimeoutAlActualizar()
                            assertEquals(ResultadoAccionUI.ACCION_INICIADA, proceso.intentarCrearPersona())
                        }

                        @Test
                        fun retorna_ACCION_INICIADA_cuando_api_personas_retorna_respuesta_ErrorRed_en_actualizar()
                        {
                            mockearRespuestaDeRedErrorRedAlActualizar()
                            assertEquals(ResultadoAccionUI.ACCION_INICIADA, proceso.intentarCrearPersona())
                        }

                        @Test
                        fun retorna_ACCION_INICIADA_cuando_api_personas_retorna_respuesta_ErrorBack_en_actualizar()
                        {
                            mockearRespuestaDeRedErrorBackAlActualizar("Error de pruebas")
                            assertEquals(ResultadoAccionUI.ACCION_INICIADA, proceso.intentarCrearPersona())
                        }

                        @Test
                        fun no_llama_finalizarProceso_de_persona_cuando_api_personas_lanza_IllegalStateException_en_actualizar()
                        {
                            val errorEsperado = IllegalStateException()
                            erroresEsperados.add(errorEsperado)
                            mockearRespuestaDeRedInvalidaAlActualizar(errorEsperado)
                            proceso.intentarCrearPersona()
                            verify(mockPersona, times(0)).finalizarProceso()
                        }

                        @Test
                        fun no_llama_finalizarProceso_de_persona_cuando_api_personas_retorna_respuesta_ErrorTimeout_en_actualizar()
                        {
                            mockearRespuestaDeRedErrorTimeoutAlActualizar()
                            proceso.intentarCrearPersona()
                            verify(mockPersona, times(0)).finalizarProceso()
                        }

                        @Test
                        fun no_llama_finalizarProceso_de_persona_cuando_api_personas_retorna_respuesta_ErrorRed_en_actualizar()
                        {
                            mockearRespuestaDeRedErrorRedAlActualizar()
                            proceso.intentarCrearPersona()
                            verify(mockPersona, times(0)).finalizarProceso()
                        }

                        @Test
                        fun no_llama_finalizarProceso_de_persona_cuando_api_personas_persona_respuesta_ErrorBack_en_actualizar()
                        {
                            mockearRespuestaDeRedErrorBackAlActualizar("Error de pruebas")
                            proceso.intentarCrearPersona()
                            verify(mockPersona, times(0)).finalizarProceso()
                        }

                        @Nested
                        inner class ErrorGlobal
                        {
                            private val testErrorGlobal by lazy { proceso.errorGlobal.test() }

                            @Test
                            fun emite_error_vacio_cuando_api_personas_retorna_respuesta_Exitosa_en_actualizar()
                            {
                                mockearRespuestaDeRedExitosaAlActualizar()
                                testErrorGlobal.assertValue(Opcional.Vacio())
                                testErrorGlobal.assertValueCount(1)
                                proceso.intentarCrearPersona()
                                testErrorGlobal.assertValueCount(2)
                                testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                            }

                            @Test
                            fun emite_error_vacio_cuando_api_personas_lanza_IllegalStateException_en_actualizar()
                            {
                                val errorEsperado = IllegalStateException()
                                erroresEsperados.add(errorEsperado)
                                mockearRespuestaDeRedInvalidaAlActualizar(errorEsperado)
                                testErrorGlobal.assertValue(Opcional.Vacio())
                                testErrorGlobal.assertValueCount(1)
                                proceso.intentarCrearPersona()
                                testErrorGlobal.assertValueCount(2)
                                testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                            }

                            @Test
                            fun emite_error_vacio_y_luego_error_correcto_cuando_api_personas_retorna_respuesta_ErrorTimeout_en_actualizar()
                            {
                                mockearRespuestaDeRedErrorTimeoutAlActualizar()
                                testErrorGlobal.assertValue(Opcional.Vacio())
                                testErrorGlobal.assertValueCount(1)
                                proceso.intentarCrearPersona()
                                testErrorGlobal.assertValueCount(3)
                                testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                                testErrorGlobal.assertValueAt(2, Opcional.De("Timeout contactando el backend"))
                            }

                            @Test
                            fun emite_error_vacio_y_luego_error_correcto_cuando_api_personas_retorna_respuesta_ErrorRed_en_actualizar()
                            {
                                mockearRespuestaDeRedErrorRedAlActualizar()
                                testErrorGlobal.assertValue(Opcional.Vacio())
                                testErrorGlobal.assertValueCount(1)
                                proceso.intentarCrearPersona()
                                testErrorGlobal.assertValueCount(3)
                                testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                                testErrorGlobal.assertValueAt(2, Opcional.De("Error contactando el backend"))
                            }

                            @Test
                            fun emite_error_vacio_y_luego_error_correcto_cuando_api_personas_retorna_respuesta_ErrorBack_en_actualizar()
                            {
                                val mensajeError = "Error de pruebas"
                                mockearRespuestaDeRedErrorBackAlActualizar(mensajeError)
                                testErrorGlobal.assertValue(Opcional.Vacio())
                                testErrorGlobal.assertValueCount(1)
                                proceso.intentarCrearPersona()
                                testErrorGlobal.assertValueCount(3)
                                testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                                testErrorGlobal.assertValueAt(2, Opcional.De("Error en petición: $mensajeError"))
                            }
                        }

                        @Nested
                        inner class PersonaCreada
                        {
                            private val testPersonaCreada by lazy { proceso.personaCreada.test() }

                            @Test
                            fun emite_valor_correcto_cuando_api_personas_retorna_respuesta_Exitosa_en_actualizar()
                            {
                                testPersonaCreada.assertValueCount(0)
                                mockearRespuestaDeRedExitosaAlActualizar()
                                proceso.intentarCrearPersona()
                                testPersonaCreada.assertValueCount(1)
                                testPersonaCreada.assertValue(personaEsperadaPruebas)
                            }

                            @Test
                            fun no_emite_valor_cuando_api_personas_lanza_IllegalStateException_en_actualizar()
                            {
                                testPersonaCreada.assertValueCount(0)
                                val errorEsperado = IllegalStateException()
                                erroresEsperados.add(errorEsperado)
                                mockearRespuestaDeRedInvalidaAlActualizar(errorEsperado)
                                proceso.intentarCrearPersona()
                                testPersonaCreada.assertValueCount(0)
                            }

                            @Test
                            fun no_emite_valor_cuando_api_personas_retorna_respuesta_ErrorTimeout_en_actualizar()
                            {
                                testPersonaCreada.assertValueCount(0)
                                mockearRespuestaDeRedErrorTimeoutAlActualizar()
                                proceso.intentarCrearPersona()
                                testPersonaCreada.assertValueCount(0)
                            }

                            @Test
                            fun no_emite_valor_cuando_api_personas_retorna_respuesta_ErrorRed_en_actualizar()
                            {
                                testPersonaCreada.assertValueCount(0)
                                mockearRespuestaDeRedErrorRedAlActualizar()
                                proceso.intentarCrearPersona()
                                testPersonaCreada.assertValueCount(0)
                            }

                            @Test
                            fun no_emite_valor_cuando_api_personas_retorna_respuesta_ErrorBack_en_actualizar()
                            {
                                testPersonaCreada.assertValueCount(0)
                                mockearRespuestaDeRedErrorBackAlActualizar("Error de pruebas")
                                proceso.intentarCrearPersona()
                                testPersonaCreada.assertValueCount(0)
                            }
                        }

                        @Nested
                        inner class AsignarPersona
                        {
                            @Test
                            fun se_llama_con_valor_correcto_cuando_api_personas_retorna_respuesta_Exitosa_en_actualizar()
                            {
                                mockearRespuestaDeRedExitosaAlActualizar()
                                proceso.intentarCrearPersona()
                                verify(mockPersona).asignarPersona(personaEsperadaPruebas)
                            }

                            @Test
                            fun no_se_llama_cuando_api_personas_lanza_IllegalStateException_en_actualizar()
                            {
                                val errorEsperado = IllegalStateException()
                                erroresEsperados.add(errorEsperado)
                                mockearRespuestaDeRedInvalidaAlActualizar(errorEsperado)
                                proceso.intentarCrearPersona()
                                verify(mockPersona, times(0)).asignarPersona(personaEsperadaPruebas)
                            }

                            @Test
                            fun no_se_llama_cuando_api_personas_retorna_respuesta_ErrorTimeout_en_actualizar()
                            {
                                mockearRespuestaDeRedErrorTimeoutAlActualizar()
                                proceso.intentarCrearPersona()
                                verify(mockPersona, times(0)).asignarPersona(personaEsperadaPruebas)
                            }

                            @Test
                            fun no_se_llama_cuando_api_personas_retorna_respuesta_ErrorRed_en_actualizar()
                            {
                                mockearRespuestaDeRedErrorRedAlActualizar()
                                proceso.intentarCrearPersona()
                                verify(mockPersona, times(0)).asignarPersona(personaEsperadaPruebas)
                            }

                            @Test
                            fun no_se_llama_cuando_api_personas_retorna_respuesta_ErrorBack_en_actualizar()
                            {
                                mockearRespuestaDeRedErrorBackAlActualizar("Error de pruebas")
                                proceso.intentarCrearPersona()
                                verify(mockPersona, times(0)).asignarPersona(personaEsperadaPruebas)
                            }
                        }

                        @Nested
                        inner class Estado
                        {
                            private val testEstado by lazy { proceso.estado.test() }

                            @Test
                            fun cambia_a_CREANDO_PERSONA_y_luego_a_PERSONA_CREADA_cuando_api_de_personas_retorna_Exitosa_en_actualizar()
                            {
                                mockearRespuestaDeRedExitosaAlActualizar()
                                testEstado.assertValue(ProcesoCrearPersona.Estado.ESPERANDO_DATOS)
                                testEstado.assertValueCount(1)
                                proceso.intentarCrearPersona()
                                testEstado.assertValueCount(3)
                                testEstado.assertValueAt(1, ProcesoCrearPersona.Estado.CREANDO_PERSONA)
                                testEstado.assertValueAt(2, ProcesoCrearPersona.Estado.ESPERANDO_DATOS)
                            }

                            @Test
                            fun cambia_a_CREANDO_PERSONA_cuando_api_de_personas_lanza_IllegalStateException_en_actualizar()
                            {
                                val errorEsperado = IllegalStateException()
                                erroresEsperados.add(errorEsperado)
                                mockearRespuestaDeRedInvalidaAlActualizar(errorEsperado)
                                testEstado.assertValue(ProcesoCrearPersona.Estado.ESPERANDO_DATOS)
                                testEstado.assertValueCount(1)
                                proceso.intentarCrearPersona()
                                testEstado.assertValueCount(2)
                                testEstado.assertValueAt(1, ProcesoCrearPersona.Estado.CREANDO_PERSONA)
                            }

                            @Test
                            fun cambia_a_CREANDO_PERSONA_y_luego_a_ESPERANDO_DATOS_cuando_api_de_personas_retorna_ErrorTimeout_en_actualizar()
                            {
                                mockearRespuestaDeRedErrorTimeoutAlActualizar()
                                testEstado.assertValue(ProcesoCrearPersona.Estado.ESPERANDO_DATOS)
                                testEstado.assertValueCount(1)
                                proceso.intentarCrearPersona()
                                testEstado.assertValueCount(3)
                                testEstado.assertValueAt(1, ProcesoCrearPersona.Estado.CREANDO_PERSONA)
                                testEstado.assertValueAt(2, ProcesoCrearPersona.Estado.ESPERANDO_DATOS)
                            }

                            @Test
                            fun cambia_a_CREANDO_PERSONA_y_luego_a_ESPERANDO_DATOS_cuando_api_de_personas_retorna_ErrorBack_en_actualizar()
                            {
                                mockearRespuestaDeRedErrorBackAlActualizar("Error de pruebas")
                                testEstado.assertValue(ProcesoCrearPersona.Estado.ESPERANDO_DATOS)
                                testEstado.assertValueCount(1)
                                proceso.intentarCrearPersona()
                                testEstado.assertValueCount(3)
                                testEstado.assertValueAt(1, ProcesoCrearPersona.Estado.CREANDO_PERSONA)
                                testEstado.assertValueAt(2, ProcesoCrearPersona.Estado.ESPERANDO_DATOS)
                            }
                        }
                    }

                    @Nested
                    inner class ConIdNulo
                    {
                        @BeforeEach
                        private fun emitirPersonaSinId()
                        {
                            mockearDarPersona(personaPruebasSinId)
                            mockearFinalizarProceso()
                            sujetoEsPersonaValida.onNext(true)
                        }

                        @Test
                        fun llama_a_crear_y_no_a_actualizar_en_api_de_red()
                        {
                            mockearRespuestaDeRedExitosaAlCrear()
                            proceso.intentarCrearPersona()
                            verify(mockApiPersonas).crear(personaPruebasSinId)
                            verify(mockApiPersonas, times(0)).actualizar(personaPruebasSinId)
                        }

                        @Test
                        fun retorna_ACCION_INICIADA_cuando_api_personas_retorna_respuesta_Exitosa_en_crear()
                        {
                            mockearRespuestaDeRedExitosaAlCrear()
                            assertEquals(ResultadoAccionUI.ACCION_INICIADA, proceso.intentarCrearPersona())
                        }

                        @Test
                        fun retorna_ACCION_INICIADA_cuando_api_personas_lanza_IllegalStateException_en_crear()
                        {
                            val errorEsperado = IllegalStateException()
                            erroresEsperados.add(errorEsperado)
                            mockearRespuestaDeRedInvalidaAlCrear(errorEsperado)
                            assertEquals(ResultadoAccionUI.ACCION_INICIADA, proceso.intentarCrearPersona())
                        }

                        @Test
                        fun retorna_ACCION_INICIADA_cuando_api_personas_retorna_respuesta_ErrorTimeout_en_crear()
                        {
                            mockearRespuestaDeRedErrorTimeoutAlCrear()
                            assertEquals(ResultadoAccionUI.ACCION_INICIADA, proceso.intentarCrearPersona())
                        }

                        @Test
                        fun retorna_ACCION_INICIADA_cuando_api_personas_retorna_respuesta_ErrorRed_en_crear()
                        {
                            mockearRespuestaDeRedErrorRedAlCrear()
                            assertEquals(ResultadoAccionUI.ACCION_INICIADA, proceso.intentarCrearPersona())
                        }

                        @Test
                        fun retorna_ACCION_INICIADA_cuando_api_personas_retorna_respuesta_ErrorBack_en_crear()
                        {
                            mockearRespuestaDeRedErrorBackAlCrear("Error de pruebas")
                            assertEquals(ResultadoAccionUI.ACCION_INICIADA, proceso.intentarCrearPersona())
                        }

                        @Test
                        fun no_llama_finalizarProceso_de_persona_cuando_api_personas_lanza_IllegalStateException_en_crear()
                        {
                            val errorEsperado = IllegalStateException()
                            erroresEsperados.add(errorEsperado)
                            mockearRespuestaDeRedInvalidaAlCrear(errorEsperado)
                            proceso.intentarCrearPersona()
                            verify(mockPersona, times(0)).finalizarProceso()
                        }

                        @Test
                        fun no_llama_finalizarProceso_de_persona_cuando_api_personas_retorna_respuesta_ErrorTimeout_en_crear()
                        {
                            mockearRespuestaDeRedErrorTimeoutAlCrear()
                            proceso.intentarCrearPersona()
                            verify(mockPersona, times(0)).finalizarProceso()
                        }

                        @Test
                        fun no_llama_finalizarProceso_de_persona_cuando_api_personas_retorna_respuesta_ErrorRed_en_crear()
                        {
                            mockearRespuestaDeRedErrorRedAlCrear()
                            proceso.intentarCrearPersona()
                            verify(mockPersona, times(0)).finalizarProceso()
                        }

                        @Test
                        fun no_llama_finalizarProceso_de_persona_cuando_api_personas_persona_respuesta_ErrorBack_en_crear()
                        {
                            mockearRespuestaDeRedErrorBackAlCrear("Error de pruebas")
                            proceso.intentarCrearPersona()
                            verify(mockPersona, times(0)).finalizarProceso()
                        }

                        @Nested
                        inner class ErrorGlobal
                        {
                            private val testErrorGlobal by lazy { proceso.errorGlobal.test() }

                            @Test
                            fun emite_error_vacio_cuando_api_personas_retorna_respuesta_Exitosa_en_crear()
                            {
                                mockearRespuestaDeRedExitosaAlCrear()
                                testErrorGlobal.assertValue(Opcional.Vacio())
                                testErrorGlobal.assertValueCount(1)
                                proceso.intentarCrearPersona()
                                testErrorGlobal.assertValueCount(2)
                                testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                            }

                            @Test
                            fun emite_error_vacio_cuando_api_personas_lanza_IllegalStateException_en_crear()
                            {
                                val errorEsperado = IllegalStateException()
                                erroresEsperados.add(errorEsperado)
                                mockearRespuestaDeRedInvalidaAlCrear(errorEsperado)
                                testErrorGlobal.assertValue(Opcional.Vacio())
                                testErrorGlobal.assertValueCount(1)
                                proceso.intentarCrearPersona()
                                testErrorGlobal.assertValueCount(2)
                                testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                            }

                            @Test
                            fun emite_error_vacio_y_luego_error_correcto_cuando_api_personas_retorna_respuesta_ErrorTimeout_en_crear()
                            {
                                mockearRespuestaDeRedErrorTimeoutAlCrear()
                                testErrorGlobal.assertValue(Opcional.Vacio())
                                testErrorGlobal.assertValueCount(1)
                                proceso.intentarCrearPersona()
                                testErrorGlobal.assertValueCount(3)
                                testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                                testErrorGlobal.assertValueAt(2, Opcional.De("Timeout contactando el backend"))
                            }

                            @Test
                            fun emite_error_vacio_y_luego_error_correcto_cuando_api_personas_retorna_respuesta_ErrorRed_en_crear()
                            {
                                mockearRespuestaDeRedErrorRedAlCrear()
                                testErrorGlobal.assertValue(Opcional.Vacio())
                                testErrorGlobal.assertValueCount(1)
                                proceso.intentarCrearPersona()
                                testErrorGlobal.assertValueCount(3)
                                testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                                testErrorGlobal.assertValueAt(2, Opcional.De("Error contactando el backend"))
                            }

                            @Test
                            fun emite_error_vacio_y_luego_error_correcto_cuando_api_personas_retorna_respuesta_ErrorBack_en_crear()
                            {
                                val mensajeError = "Error de pruebas"
                                mockearRespuestaDeRedErrorBackAlCrear(mensajeError)
                                testErrorGlobal.assertValue(Opcional.Vacio())
                                testErrorGlobal.assertValueCount(1)
                                proceso.intentarCrearPersona()
                                testErrorGlobal.assertValueCount(3)
                                testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                                testErrorGlobal.assertValueAt(2, Opcional.De("Error en petición: $mensajeError"))
                            }
                        }

                        @Nested
                        inner class PersonaCreada
                        {
                            private val testPersonaCreada by lazy { proceso.personaCreada.test() }

                            @Test
                            fun emite_valor_correcto_cuando_api_personas_retorna_respuesta_Exitosa_en_crear()
                            {
                                testPersonaCreada.assertValueCount(0)
                                mockearRespuestaDeRedExitosaAlCrear()
                                proceso.intentarCrearPersona()
                                testPersonaCreada.assertValueCount(1)
                                testPersonaCreada.assertValue(personaEsperadaPruebas)
                            }

                            @Test
                            fun no_emite_valor_cuando_api_personas_lanza_IllegalStateException_en_crear()
                            {
                                testPersonaCreada.assertValueCount(0)
                                val errorEsperado = IllegalStateException()
                                erroresEsperados.add(errorEsperado)
                                mockearRespuestaDeRedInvalidaAlCrear(errorEsperado)
                                proceso.intentarCrearPersona()
                                testPersonaCreada.assertValueCount(0)
                            }

                            @Test
                            fun no_emite_valor_cuando_api_personas_retorna_respuesta_ErrorTimeout_en_crear()
                            {
                                testPersonaCreada.assertValueCount(0)
                                mockearRespuestaDeRedErrorTimeoutAlCrear()
                                proceso.intentarCrearPersona()
                                testPersonaCreada.assertValueCount(0)
                            }

                            @Test
                            fun no_emite_valor_cuando_api_personas_retorna_respuesta_ErrorRed_en_crear()
                            {
                                testPersonaCreada.assertValueCount(0)
                                mockearRespuestaDeRedErrorRedAlCrear()
                                proceso.intentarCrearPersona()
                                testPersonaCreada.assertValueCount(0)
                            }

                            @Test
                            fun no_emite_valor_cuando_api_personas_retorna_respuesta_ErrorBack_en_crear()
                            {
                                testPersonaCreada.assertValueCount(0)
                                mockearRespuestaDeRedErrorBackAlCrear("Error de pruebas")
                                proceso.intentarCrearPersona()
                                testPersonaCreada.assertValueCount(0)
                            }
                        }

                        @Nested
                        inner class AsignarPersona
                        {
                            @Test
                            fun se_llama_con_valor_correcto_cuando_api_personas_retorna_respuesta_Exitosa_en_crear()
                            {
                                mockearRespuestaDeRedExitosaAlCrear()
                                proceso.intentarCrearPersona()
                                verify(mockPersona).asignarPersona(personaEsperadaPruebas)
                            }

                            @Test
                            fun no_se_llama_cuando_api_personas_lanza_IllegalStateException_en_crear()
                            {
                                val errorEsperado = IllegalStateException()
                                erroresEsperados.add(errorEsperado)
                                mockearRespuestaDeRedInvalidaAlCrear(errorEsperado)
                                proceso.intentarCrearPersona()
                                verify(mockPersona, times(0)).asignarPersona(personaEsperadaPruebas)
                            }

                            @Test
                            fun no_se_llama_cuando_api_personas_retorna_respuesta_ErrorTimeout_en_crear()
                            {
                                mockearRespuestaDeRedErrorTimeoutAlCrear()
                                proceso.intentarCrearPersona()
                                verify(mockPersona, times(0)).asignarPersona(personaEsperadaPruebas)
                            }

                            @Test
                            fun no_se_llama_cuando_api_personas_retorna_respuesta_ErrorRed_en_crear()
                            {
                                mockearRespuestaDeRedErrorRedAlCrear()
                                proceso.intentarCrearPersona()
                                verify(mockPersona, times(0)).asignarPersona(personaEsperadaPruebas)
                            }

                            @Test
                            fun no_se_llama_cuando_api_personas_retorna_respuesta_ErrorBack_en_crear()
                            {
                                mockearRespuestaDeRedErrorBackAlCrear("Error de pruebas")
                                proceso.intentarCrearPersona()
                                verify(mockPersona, times(0)).asignarPersona(personaEsperadaPruebas)
                            }
                        }

                        @Nested
                        inner class Estado
                        {
                            private val testEstado by lazy { proceso.estado.test() }

                            @Test
                            fun cambia_a_CREANDO_PERSONA_y_luego_a_PERSONA_CREADA_cuando_api_de_personas_retorna_Exitosa_en_crear()
                            {
                                mockearRespuestaDeRedExitosaAlCrear()
                                testEstado.assertValue(ProcesoCrearPersona.Estado.ESPERANDO_DATOS)
                                testEstado.assertValueCount(1)
                                proceso.intentarCrearPersona()
                                testEstado.assertValueCount(3)
                                testEstado.assertValueAt(1, ProcesoCrearPersona.Estado.CREANDO_PERSONA)
                                testEstado.assertValueAt(2, ProcesoCrearPersona.Estado.ESPERANDO_DATOS)
                            }

                            @Test
                            fun cambia_a_CREANDO_PERSONA_cuando_api_de_personas_lanza_IllegalStateException_en_crear()
                            {
                                val errorEsperado = IllegalStateException()
                                erroresEsperados.add(errorEsperado)
                                mockearRespuestaDeRedInvalidaAlCrear(errorEsperado)
                                testEstado.assertValue(ProcesoCrearPersona.Estado.ESPERANDO_DATOS)
                                testEstado.assertValueCount(1)
                                proceso.intentarCrearPersona()
                                testEstado.assertValueCount(2)
                                testEstado.assertValueAt(1, ProcesoCrearPersona.Estado.CREANDO_PERSONA)
                            }

                            @Test
                            fun cambia_a_CREANDO_PERSONA_y_luego_a_ESPERANDO_DATOS_cuando_api_de_personas_retorna_ErrorTimeout_en_crear()
                            {
                                mockearRespuestaDeRedErrorTimeoutAlCrear()
                                testEstado.assertValue(ProcesoCrearPersona.Estado.ESPERANDO_DATOS)
                                testEstado.assertValueCount(1)
                                proceso.intentarCrearPersona()
                                testEstado.assertValueCount(3)
                                testEstado.assertValueAt(1, ProcesoCrearPersona.Estado.CREANDO_PERSONA)
                                testEstado.assertValueAt(2, ProcesoCrearPersona.Estado.ESPERANDO_DATOS)
                            }

                            @Test
                            fun cambia_a_CREANDO_PERSONA_y_luego_a_ESPERANDO_DATOS_cuando_api_de_personas_retorna_ErrorBack_en_crear()
                            {
                                mockearRespuestaDeRedErrorBackAlCrear("Error de pruebas")
                                testEstado.assertValue(ProcesoCrearPersona.Estado.ESPERANDO_DATOS)
                                testEstado.assertValueCount(1)
                                proceso.intentarCrearPersona()
                                testEstado.assertValueCount(3)
                                testEstado.assertValueAt(1, ProcesoCrearPersona.Estado.CREANDO_PERSONA)
                                testEstado.assertValueAt(2, ProcesoCrearPersona.Estado.ESPERANDO_DATOS)
                            }
                        }
                    }
                }
            }
        }

        @Nested
        inner class IntentarConsultarPersonaPorDocumento
        {
            @Nested
            @Suppress("ClassName")
            inner class EnEstadoESPERANDO_DATOS
            {
                @Test
                fun retorna_MODELO_EN_ESTADO_INVALIDO_cuando_persona_no_a_emitido_valor_sobre_campos_documento()
                {
                    assertEquals(ResultadoAccionUI.MODELO_EN_ESTADO_INVALIDO, proceso.intentarConsultarPersonaPorDocumento())
                }

                @Test
                fun retorna_MODELO_EN_ESTADO_INVALIDO_cuando_persona_no_a_emitido_valor_sobre_tipo_documento()
                {
                    sujetoNumeroDocumento.onNext(Notification.createOnNext(documentoPersonaPruebas.numeroDocumento))
                    assertEquals(ResultadoAccionUI.MODELO_EN_ESTADO_INVALIDO, proceso.intentarConsultarPersonaPorDocumento())
                }

                @Test
                fun retorna_MODELO_EN_ESTADO_INVALIDO_cuando_persona_no_a_emitido_valor_sobre_numero_documento()
                {
                    sujetoTipoDocumento.onNext(documentoPersonaPruebas.tipoDocumento)
                    assertEquals(ResultadoAccionUI.MODELO_EN_ESTADO_INVALIDO, proceso.intentarConsultarPersonaPorDocumento())
                }

                @Test
                fun retorna_MODELO_EN_ESTADO_INVALIDO_cuando_persona_emitio_error_sobre_numero_documento()
                {
                    sujetoTipoDocumento.onNext(documentoPersonaPruebas.tipoDocumento)
                    sujetoNumeroDocumento.onNext(Notification.createOnError(Exception()))
                    assertEquals(ResultadoAccionUI.MODELO_EN_ESTADO_INVALIDO, proceso.intentarConsultarPersonaPorDocumento())
                }

                @Nested
                inner class ConObservableDocumentoValidoTrueCuandoDarDocumentoCompletoLanzaExcepcion
                {
                    @BeforeEach
                    fun emitirDocumentoValidoYFallarEnDarDocumentoCompleto()
                    {
                        doThrow(IllegalStateException("Error"))
                            .`when`(mockPersona)
                            .darDocumentoCompleto()
                        sujetoTipoDocumento.onNext(documentoPersonaPruebas.tipoDocumento)
                        sujetoNumeroDocumento.onNext(Notification.createOnNext(documentoPersonaPruebas.numeroDocumento))
                    }

                    @Test
                    fun retorna_OBSERVABLES_EN_ESTADO_INVALIDO()
                    {
                        assertEquals(ResultadoAccionUI.OBSERVABLES_EN_ESTADO_INVALIDO, proceso.intentarConsultarPersonaPorDocumento())
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
                            proceso.intentarConsultarPersonaPorDocumento()
                            testErrorGlobal.assertValueCount(2)
                            testErrorGlobal.assertValueAt(1, Opcional.De("Documento inválido"))
                        }
                    }
                }

                @Nested
                inner class ConDocumentoValido
                {
                    @BeforeEach
                    private fun emitirEsPersonaValidaYMockear()
                    {
                        mockearDarDocumentoCompleto()
                        sujetoTipoDocumento.onNext(documentoPersonaPruebas.tipoDocumento)
                        sujetoNumeroDocumento.onNext(Notification.createOnNext(documentoPersonaPruebas.numeroDocumento))
                    }

                    @Test
                    fun retorna_ACCION_INICIADA_cuando_api_personas_retorna_respuesta_Exitosa()
                    {
                        mockearRespuestaDeRedExitosaAlConsultarSinFamiliares()
                        assertEquals(ResultadoAccionUI.ACCION_INICIADA, proceso.intentarConsultarPersonaPorDocumento())
                    }

                    @Test
                    fun retorna_ACCION_INICIADA_cuando_api_personas_lanza_IllegalStateException()
                    {
                        val errorEsperado = IllegalStateException()
                        erroresEsperados.add(errorEsperado)
                        mockearRespuestaDeRedInvalidaAlConsultar(errorEsperado)
                        assertEquals(ResultadoAccionUI.ACCION_INICIADA, proceso.intentarConsultarPersonaPorDocumento())
                    }

                    @Test
                    fun retorna_ACCION_INICIADA_cuando_api_personas_retorna_respuesta_ErrorTimeout()
                    {
                        mockearRespuestaDeRedErrorTimeoutAlConsultar()
                        assertEquals(ResultadoAccionUI.ACCION_INICIADA, proceso.intentarConsultarPersonaPorDocumento())
                    }

                    @Test
                    fun retorna_ACCION_INICIADA_cuando_api_personas_retorna_respuesta_ErrorRed()
                    {
                        mockearRespuestaDeRedErrorRedAlConsultar()
                        assertEquals(ResultadoAccionUI.ACCION_INICIADA, proceso.intentarConsultarPersonaPorDocumento())
                    }

                    @Test
                    fun retorna_ACCION_INICIADA_cuando_api_personas_retorna_respuesta_ErrorBack()
                    {
                        mockearRespuestaDeRedErrorBackAlConsultar("Error de pruebas")
                        assertEquals(ResultadoAccionUI.ACCION_INICIADA, proceso.intentarConsultarPersonaPorDocumento())
                    }

                    @Test
                    fun no_llama_finalizarProceso_de_persona_cuando_api_personas_retorna_respuesta_Exitosa()
                    {
                        mockearRespuestaDeRedExitosaAlConsultarSinFamiliares()
                        proceso.intentarConsultarPersonaPorDocumento()
                        verify(mockPersona, times(0)).finalizarProceso()
                    }

                    @Test
                    fun no_llama_finalizarProceso_de_persona_cuando_api_personas_lanza_IllegalStateException()
                    {
                        val errorEsperado = IllegalStateException()
                        erroresEsperados.add(errorEsperado)
                        mockearRespuestaDeRedInvalidaAlConsultar(errorEsperado)
                        proceso.intentarConsultarPersonaPorDocumento()
                        verify(mockPersona, times(0)).finalizarProceso()
                    }

                    @Test
                    fun no_llama_finalizarProceso_de_persona_cuando_api_personas_retorna_respuesta_ErrorTimeout()
                    {
                        mockearRespuestaDeRedErrorTimeoutAlConsultar()
                        proceso.intentarConsultarPersonaPorDocumento()
                        verify(mockPersona, times(0)).finalizarProceso()
                    }

                    @Test
                    fun no_llama_finalizarProceso_de_persona_cuando_api_personas_retorna_respuesta_ErrorRed()
                    {
                        mockearRespuestaDeRedErrorRedAlConsultar()
                        proceso.intentarConsultarPersonaPorDocumento()
                        verify(mockPersona, times(0)).finalizarProceso()
                    }

                    @Test
                    fun no_llama_finalizarProceso_de_persona_cuando_api_personas_persona_respuesta_ErrorBack()
                    {
                        mockearRespuestaDeRedErrorBackAlConsultar("Error de pruebas")
                        proceso.intentarConsultarPersonaPorDocumento()
                        verify(mockPersona, times(0)).finalizarProceso()
                    }

                    @Nested
                    inner class ErrorGlobal
                    {
                        private val testErrorGlobal by lazy { proceso.errorGlobal.test() }

                        @Test
                        fun emite_error_vacio_cuando_api_personas_retorna_respuesta_Exitosa()
                        {
                            mockearRespuestaDeRedExitosaAlConsultarSinFamiliares()
                            testErrorGlobal.assertValue(Opcional.Vacio())
                            testErrorGlobal.assertValueCount(1)
                            proceso.intentarConsultarPersonaPorDocumento()
                            testErrorGlobal.assertValueCount(2)
                            testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                        }

                        @Test
                        fun emite_error_vacio_cuando_api_personas_retorna_respuesta_ErrorBack_con_codigo_persona_no_existe()
                        {
                            doNothing().`when`(mockPersona).cambiarAfiliacion(cualquiera())
                            doNothing().`when`(mockPersona).cambiarCategoria(cualquiera())

                            mockearRespuestaDeRedErrorBackPersonaNoExisteAlConsultar()
                            testErrorGlobal.assertValue(Opcional.Vacio())
                            testErrorGlobal.assertValueCount(1)
                            proceso.intentarConsultarPersonaPorDocumento()
                            testErrorGlobal.assertValueCount(2)
                            testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                        }

                        @Test
                        fun emite_error_vacio_cuando_api_personas_lanza_IllegalStateException()
                        {
                            val errorEsperado = IllegalStateException()
                            erroresEsperados.add(errorEsperado)
                            mockearRespuestaDeRedInvalidaAlConsultar(errorEsperado)
                            testErrorGlobal.assertValue(Opcional.Vacio())
                            testErrorGlobal.assertValueCount(1)
                            proceso.intentarConsultarPersonaPorDocumento()
                            testErrorGlobal.assertValueCount(2)
                            testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                        }

                        @Test
                        fun emite_error_vacio_y_luego_error_correcto_cuando_api_personas_retorna_respuesta_ErrorTimeout()
                        {
                            mockearRespuestaDeRedErrorTimeoutAlConsultar()
                            testErrorGlobal.assertValue(Opcional.Vacio())
                            testErrorGlobal.assertValueCount(1)
                            proceso.intentarConsultarPersonaPorDocumento()
                            testErrorGlobal.assertValueCount(3)
                            testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                            testErrorGlobal.assertValueAt(2, Opcional.De("Timeout contactando el backend"))
                        }

                        @Test
                        fun emite_error_vacio_y_luego_error_correcto_cuando_api_personas_retorna_respuesta_ErrorRed()
                        {
                            mockearRespuestaDeRedErrorRedAlConsultar()
                            testErrorGlobal.assertValue(Opcional.Vacio())
                            testErrorGlobal.assertValueCount(1)
                            proceso.intentarConsultarPersonaPorDocumento()
                            testErrorGlobal.assertValueCount(3)
                            testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                            testErrorGlobal.assertValueAt(2, Opcional.De("Error contactando el backend"))
                        }

                        @Test
                        fun emite_error_vacio_y_luego_error_correcto_cuando_api_personas_retorna_respuesta_ErrorBack()
                        {
                            val mensajeError = "Error de pruebas"
                            mockearRespuestaDeRedErrorBackAlConsultar(mensajeError)
                            testErrorGlobal.assertValue(Opcional.Vacio())
                            testErrorGlobal.assertValueCount(1)
                            proceso.intentarConsultarPersonaPorDocumento()
                            testErrorGlobal.assertValueCount(3)
                            testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                            testErrorGlobal.assertValueAt(2, Opcional.De("Error en petición: $mensajeError"))
                        }
                    }

                    @Nested
                    inner class PersonaCreada
                    {
                        private val testPersonaCreada by lazy { proceso.personaCreada.test() }

                        @Test
                        fun no_emite_valor_cuando_api_personas_retorna_respuesta_Exitosa()
                        {
                            testPersonaCreada.assertValueCount(0)
                            mockearRespuestaDeRedExitosaAlConsultarSinFamiliares()
                            proceso.intentarConsultarPersonaPorDocumento()
                            testPersonaCreada.assertValueCount(0)
                        }

                        @Test
                        fun no_emite_valor_cuando_api_personas_lanza_IllegalStateException()
                        {
                            testPersonaCreada.assertValueCount(0)
                            val errorEsperado = IllegalStateException()
                            erroresEsperados.add(errorEsperado)
                            mockearRespuestaDeRedInvalidaAlConsultar(errorEsperado)
                            proceso.intentarConsultarPersonaPorDocumento()
                            testPersonaCreada.assertValueCount(0)
                        }

                        @Test
                        fun no_emite_valor_cuando_api_personas_retorna_respuesta_ErrorTimeout()
                        {
                            testPersonaCreada.assertValueCount(0)
                            mockearRespuestaDeRedErrorTimeoutAlConsultar()
                            proceso.intentarConsultarPersonaPorDocumento()
                            testPersonaCreada.assertValueCount(0)
                        }

                        @Test
                        fun no_emite_valor_cuando_api_personas_retorna_respuesta_ErrorRed()
                        {
                            testPersonaCreada.assertValueCount(0)
                            mockearRespuestaDeRedErrorRedAlConsultar()
                            proceso.intentarConsultarPersonaPorDocumento()
                            testPersonaCreada.assertValueCount(0)
                        }

                        @Test
                        fun no_emite_valor_cuando_api_personas_retorna_respuesta_ErrorBack()
                        {
                            testPersonaCreada.assertValueCount(0)
                            mockearRespuestaDeRedErrorBackAlConsultar("Error de pruebas")
                            proceso.intentarConsultarPersonaPorDocumento()
                            testPersonaCreada.assertValueCount(0)
                        }
                    }

                    @Nested
                    inner class Familiares
                    {
                        @Test
                        fun empieza_con_lista_vacia()
                        {
                            assertTrue(proceso.familiares.isEmpty())
                        }

                        @Test
                        fun se_mantiene_como_lista_vacia_cuando_api_personas_retorna_respuesta_Exitosa_sin_familiares()
                        {
                            mockearRespuestaDeRedExitosaAlConsultarSinFamiliares()
                            proceso.intentarConsultarPersonaPorDocumento()
                            assertTrue(proceso.familiares.isEmpty())
                        }

                        @Test
                        fun cambia_a_lista_correcta_cuando_api_personas_retorna_respuesta_Exitosa_con_familiares()
                        {
                            mockearRespuestaDeRedExitosaAlConsultarConFamiliares()
                            proceso.intentarConsultarPersonaPorDocumento()
                            assertEquals(setOf(familiarPruebas), proceso.familiares)
                        }

                        @Test
                        fun cambia_a_lista_correcta_y_luego_a_lista_vacia_cuando_api_personas_retorna_respuesta_Exitosa_con_familiares_y_luego_respuesta_Exitosa_sin_familiares()
                        {
                            mockearRespuestaDeRedExitosaAlConsultarConFamiliares()
                            proceso.intentarConsultarPersonaPorDocumento()
                            assertEquals(setOf(familiarPruebas), proceso.familiares)

                            sujetoNumeroDocumento.onNext(Notification.createOnNext("otro"))
                            sujetoNumeroDocumento.onNext(Notification.createOnNext(documentoPersonaPruebas.numeroDocumento))
                            mockearRespuestaDeRedExitosaAlConsultarSinFamiliares()
                            proceso.intentarConsultarPersonaPorDocumento()
                            assertTrue(proceso.familiares.isEmpty())
                        }

                        @Test
                        fun se_mantiene_como_lista_vacia_personas_lanza_IllegalStateException()
                        {
                            val errorEsperado = IllegalStateException()
                            erroresEsperados.add(errorEsperado)
                            mockearRespuestaDeRedInvalidaAlConsultar(errorEsperado)
                            proceso.intentarConsultarPersonaPorDocumento()
                            assertTrue(proceso.familiares.isEmpty())
                        }

                        @Test
                        fun cambia_a_lista_correcta_y_se_mantiene_cuando_api_personas_retorna_respuesta_Exitosa_con_familiares_y_luego_respuesta_ExitosaVacia()
                        {
                            mockearRespuestaDeRedExitosaAlConsultarConFamiliares()
                            proceso.intentarConsultarPersonaPorDocumento()
                            assertEquals(setOf(familiarPruebas), proceso.familiares)

                            sujetoNumeroDocumento.onNext(Notification.createOnNext("otro"))
                            sujetoNumeroDocumento.onNext(Notification.createOnNext(documentoPersonaPruebas.numeroDocumento))
                            val errorEsperado = IllegalStateException()
                            erroresEsperados.add(errorEsperado)
                            mockearRespuestaDeRedInvalidaAlConsultar(errorEsperado)
                            proceso.intentarConsultarPersonaPorDocumento()
                            assertEquals(setOf(familiarPruebas), proceso.familiares)
                        }

                        @Test
                        fun se_mantiene_como_lista_vacia_personas_retorna_respuesta_ErrorTimeout()
                        {
                            mockearRespuestaDeRedErrorTimeoutAlConsultar()
                            proceso.intentarConsultarPersonaPorDocumento()
                            assertTrue(proceso.familiares.isEmpty())
                        }

                        @Test
                        fun cambia_a_lista_correcta_y_se_mantiene_cuando_api_personas_retorna_respuesta_Exitosa_con_familiares_y_luego_respuesta_ErrorTimeout()
                        {
                            mockearRespuestaDeRedExitosaAlConsultarConFamiliares()
                            proceso.intentarConsultarPersonaPorDocumento()
                            assertEquals(setOf(familiarPruebas), proceso.familiares)

                            sujetoNumeroDocumento.onNext(Notification.createOnNext("otro"))
                            sujetoNumeroDocumento.onNext(Notification.createOnNext(documentoPersonaPruebas.numeroDocumento))
                            mockearRespuestaDeRedErrorTimeoutAlConsultar()
                            proceso.intentarConsultarPersonaPorDocumento()
                            assertEquals(setOf(familiarPruebas), proceso.familiares)
                        }

                        @Test
                        fun se_mantiene_como_lista_vacia_personas_retorna_respuesta_ErrorRed()
                        {
                            mockearRespuestaDeRedErrorRedAlConsultar()
                            proceso.intentarConsultarPersonaPorDocumento()
                            assertTrue(proceso.familiares.isEmpty())
                        }

                        @Test
                        fun cambia_a_lista_correcta_y_se_mantiene_cuando_api_personas_retorna_respuesta_Exitosa_con_familiares_y_luego_respuesta_ErrorRed()
                        {
                            mockearRespuestaDeRedExitosaAlConsultarConFamiliares()
                            proceso.intentarConsultarPersonaPorDocumento()
                            assertEquals(setOf(familiarPruebas), proceso.familiares)

                            sujetoNumeroDocumento.onNext(Notification.createOnNext("otro"))
                            sujetoNumeroDocumento.onNext(Notification.createOnNext(documentoPersonaPruebas.numeroDocumento))
                            mockearRespuestaDeRedErrorRedAlConsultar()
                            proceso.intentarConsultarPersonaPorDocumento()
                            assertEquals(setOf(familiarPruebas), proceso.familiares)
                        }

                        @Test
                        fun se_mantiene_como_lista_vacia_personas_retorna_respuesta_ErrorBack_diferente_a_persona_no_existe()
                        {
                            mockearRespuestaDeRedErrorBackAlConsultar("Error de pruebas")
                            assertTrue(proceso.familiares.isEmpty())
                        }

                        @Test
                        fun cambia_a_lista_correcta_y_se_mantiene_cuando_api_personas_retorna_respuesta_Exitosa_con_familiares_y_luego_respuesta_ErrorBack_diferente_a_persona_no_existe()
                        {
                            mockearRespuestaDeRedExitosaAlConsultarConFamiliares()
                            proceso.intentarConsultarPersonaPorDocumento()
                            assertEquals(setOf(familiarPruebas), proceso.familiares)

                            sujetoNumeroDocumento.onNext(Notification.createOnNext("otro"))
                            sujetoNumeroDocumento.onNext(Notification.createOnNext(documentoPersonaPruebas.numeroDocumento))
                            mockearRespuestaDeRedErrorBackAlConsultar("Error de pruebas")
                            proceso.intentarConsultarPersonaPorDocumento()
                            assertEquals(setOf(familiarPruebas), proceso.familiares)
                        }

                        @Test
                        fun se_mantiene_como_lista_vacia_personas_retorna_respuesta_ErrorBack_persona_no_existe()
                        {
                            doNothing().`when`(mockPersona).cambiarAfiliacion(cualquiera())
                            doNothing().`when`(mockPersona).cambiarCategoria(cualquiera())

                            mockearRespuestaDeRedErrorBackPersonaNoExisteAlConsultar()
                            proceso.intentarConsultarPersonaPorDocumento()
                            assertTrue(proceso.familiares.isEmpty())
                        }

                        @Test
                        fun cambia_a_lista_correcta_y_luego_a_lista_vacia_cuando_api_personas_retorna_respuesta_Exitosa_con_familiares_y_luego_respuesta_ErrorBack_persona_no_existe()
                        {
                            mockearRespuestaDeRedExitosaAlConsultarConFamiliares()

                            proceso.intentarConsultarPersonaPorDocumento()
                            assertEquals(setOf(familiarPruebas), proceso.familiares)

                            sujetoNumeroDocumento.onNext(Notification.createOnNext("otro"))
                            sujetoNumeroDocumento.onNext(Notification.createOnNext(documentoPersonaPruebas.numeroDocumento))

                            doNothing().`when`(mockPersona).cambiarAfiliacion(cualquiera())
                            doNothing().`when`(mockPersona).cambiarCategoria(cualquiera())
                            mockearRespuestaDeRedErrorBackPersonaNoExisteAlConsultar()

                            proceso.intentarConsultarPersonaPorDocumento()
                            assertTrue(proceso.familiares.isEmpty())
                        }
                    }

                    @Nested
                    inner class AsignarPersona
                    {
                        @Test
                        fun se_llama_con_valor_correcto_cuando_api_personas_retorna_respuesta_Exitosa()
                        {
                            mockearRespuestaDeRedExitosaAlConsultarSinFamiliares()
                            proceso.intentarConsultarPersonaPorDocumento()
                            verify(mockPersona).asignarPersona(personaEsperadaPruebas)
                        }

                        @Test
                        fun no_se_llama_cuando_api_personas_lanza_IllegalStateException()
                        {
                            val errorEsperado = IllegalStateException()
                            erroresEsperados.add(errorEsperado)
                            mockearRespuestaDeRedInvalidaAlConsultar(errorEsperado)
                            proceso.intentarConsultarPersonaPorDocumento()
                            verify(mockPersona, times(0)).asignarPersona(personaEsperadaPruebas)
                        }

                        @Test
                        fun no_se_llama_cuando_api_personas_retorna_respuesta_ErrorTimeout()
                        {
                            mockearRespuestaDeRedErrorTimeoutAlConsultar()
                            proceso.intentarConsultarPersonaPorDocumento()
                            verify(mockPersona, times(0)).asignarPersona(personaEsperadaPruebas)
                        }

                        @Test
                        fun no_se_llama_cuando_api_personas_retorna_respuesta_ErrorRed()
                        {
                            mockearRespuestaDeRedErrorRedAlConsultar()
                            proceso.intentarConsultarPersonaPorDocumento()
                            verify(mockPersona, times(0)).asignarPersona(personaEsperadaPruebas)
                        }

                        @Test
                        fun no_se_llama_cuando_api_personas_retorna_respuesta_ErrorBack()
                        {
                            mockearRespuestaDeRedErrorBackAlConsultar("Error de pruebas")
                            proceso.intentarConsultarPersonaPorDocumento()
                            verify(mockPersona, times(0)).asignarPersona(personaEsperadaPruebas)
                        }
                    }

                    @Nested
                    inner class Estado
                    {
                        private val testEstado by lazy { proceso.estado.test() }

                        @Test
                        fun cambia_a_CONSULTANDO_PERSONA_y_luego_a_ESPERANDO_DATOS_cuando_api_de_personas_retorna_Exitosa()
                        {
                            mockearRespuestaDeRedExitosaAlConsultarSinFamiliares()
                            testEstado.assertValue(ProcesoCrearPersona.Estado.ESPERANDO_DATOS)
                            testEstado.assertValueCount(1)
                            proceso.intentarConsultarPersonaPorDocumento()
                            testEstado.assertValueCount(3)
                            testEstado.assertValueAt(1, ProcesoCrearPersona.Estado.CONSULTANDO_PERSONA)
                            testEstado.assertValueAt(2, ProcesoCrearPersona.Estado.ESPERANDO_DATOS)
                        }

                        @Test
                        fun cambia_a_CONSULTANDO_PERSONA_cuando_api_de_personas_lanza_IllegalStateException()
                        {
                            val errorEsperado = IllegalStateException()
                            erroresEsperados.add(errorEsperado)
                            mockearRespuestaDeRedInvalidaAlConsultar(errorEsperado)
                            testEstado.assertValue(ProcesoCrearPersona.Estado.ESPERANDO_DATOS)
                            testEstado.assertValueCount(1)
                            proceso.intentarConsultarPersonaPorDocumento()
                            testEstado.assertValueCount(2)
                            testEstado.assertValueAt(1, ProcesoCrearPersona.Estado.CONSULTANDO_PERSONA)
                        }

                        @Test
                        fun cambia_a_CONSULTANDO_PERSONA_y_luego_a_ESPERANDO_DATOS_cuando_api_de_personas_retorna_ErrorTimeout()
                        {
                            mockearRespuestaDeRedErrorTimeoutAlConsultar()
                            testEstado.assertValue(ProcesoCrearPersona.Estado.ESPERANDO_DATOS)
                            testEstado.assertValueCount(1)
                            proceso.intentarConsultarPersonaPorDocumento()
                            testEstado.assertValueCount(3)
                            testEstado.assertValueAt(1, ProcesoCrearPersona.Estado.CONSULTANDO_PERSONA)
                            testEstado.assertValueAt(2, ProcesoCrearPersona.Estado.ESPERANDO_DATOS)
                        }

                        @Test
                        fun cambia_a_CONSULTANDO_PERSONA_y_luego_a_ESPERANDO_DATOS_cuando_api_de_personas_retorna_ErrorBack()
                        {
                            mockearRespuestaDeRedErrorBackAlConsultar("Error de pruebas")
                            testEstado.assertValue(ProcesoCrearPersona.Estado.ESPERANDO_DATOS)
                            testEstado.assertValueCount(1)
                            proceso.intentarConsultarPersonaPorDocumento()
                            testEstado.assertValueCount(3)
                            testEstado.assertValueAt(1, ProcesoCrearPersona.Estado.CONSULTANDO_PERSONA)
                            testEstado.assertValueAt(2, ProcesoCrearPersona.Estado.ESPERANDO_DATOS)
                        }
                    }

                    @Test
                    fun se_cambia_la_afiliacion_a_no_afiliada_y_la_categoria_a_D_cuando_api_personas_retorna_respuesta_persona_no_existe()
                    {
                        doNothing().`when`(mockPersona).cambiarAfiliacion(cualquiera())
                        doNothing().`when`(mockPersona).cambiarCategoria(cualquiera())

                        mockearRespuestaDeRedErrorBackPersonaNoExisteAlConsultar()

                        proceso.intentarConsultarPersonaPorDocumento()

                        verify(mockPersona).cambiarAfiliacion(Persona.Afiliacion.NO_AFILIADO)
                        verify(mockPersona).cambiarCategoria(Persona.Categoria.D)
                    }
                }
            }
        }

        @Nested
        inner class IntentarCrearPersonaOConsultarPersonaPorDocumentoDeSerNecesario
        {
            private lateinit var spyProceso: ProcesoCrearPersona

            @BeforeEach
            fun inicializarSpy()
            {
                spyProceso = Mockito.spy(proceso)
            }

            @Nested
            inner class CuandoDebeConsultarPersonaRetornaFalse
            {
                @BeforeEach
                fun mockear_debeConsultarPersona()
                {
                    doReturn(false)
                        .`when`(spyProceso)
                        .debeConsultarPersona
                }

                private fun mockearRespuestaIntentarCrearPersona(accionUI: ResultadoAccionUI)
                {
                    doReturn(accionUI)
                        .`when`(spyProceso)
                        .intentarCrearPersona()
                }

                @Test
                fun llama_a_intentarCrearPersona()
                {
                    spyProceso.intentarCrearPersonaOConsultarPersonaPorDocumentoDeSerNecesario()
                    verify(spyProceso).intentarCrearPersona()
                }

                @Test
                fun no_llama_a_intentarConsultarPersonaPorDocumento()
                {
                    spyProceso.intentarCrearPersonaOConsultarPersonaPorDocumentoDeSerNecesario()
                    verify(spyProceso, times(0)).intentarConsultarPersonaPorDocumento()
                }

                @ParameterizedTest(name = "retorna_{0}_cuando_intentarCrearPersona_retorna_{0}")
                @EnumSource(value = ResultadoAccionUI::class)
                fun retorna_lo_mismo_que_intentarCrearPersona(resultadoEsperado: ResultadoAccionUI)
                {
                    mockearRespuestaIntentarCrearPersona(resultadoEsperado)
                    assertEquals(resultadoEsperado, spyProceso.intentarCrearPersonaOConsultarPersonaPorDocumentoDeSerNecesario())
                }
            }

            @Nested
            inner class CuandoDebeConsultarPersonaRetornaTrue
            {
                @BeforeEach
                fun mockear_debeConsultarPersona()
                {
                    doReturn(true)
                        .`when`(spyProceso)
                        .debeConsultarPersona
                }

                @BeforeEach
                private fun mockearDarDocumentoCompletoYRespuestaDeRed()
                {
                    mockearDarDocumentoCompleto()
                    mockearRespuestaDeRedExitosaAlConsultarSinFamiliares()
                }

                private fun mockearRespuestaIntentarConsultarPersonaPorDocumento(accionUI: ResultadoAccionUI)
                {
                    doReturn(accionUI)
                        .`when`(spyProceso)
                        .intentarConsultarPersonaPorDocumento()
                }

                @Test
                fun llama_a_intentarConsultarPersonaPorDocumento()
                {
                    spyProceso.intentarCrearPersonaOConsultarPersonaPorDocumentoDeSerNecesario()
                    verify(spyProceso).intentarConsultarPersonaPorDocumento()
                }

                @Test
                fun no_llama_a_intentarCrearPersona()
                {
                    spyProceso.intentarCrearPersonaOConsultarPersonaPorDocumentoDeSerNecesario()
                    verify(spyProceso, times(0)).intentarCrearPersona()
                }

                @ParameterizedTest(name = "retorna_{0}_cuando_intentarCrearPersona_retorna_{0}")
                @EnumSource(value = ResultadoAccionUI::class)
                fun retorna_lo_mismo_que_intentarConsultarPersonaPorDocumento(resultadoEsperado: ResultadoAccionUI)
                {
                    mockearRespuestaIntentarConsultarPersonaPorDocumento(resultadoEsperado)
                    assertEquals(resultadoEsperado, spyProceso.intentarCrearPersonaOConsultarPersonaPorDocumentoDeSerNecesario())
                }
            }
        }

        @Nested
        inner class ConsultarPersonaPorDocumentoYCombinarConLecturaBarras
        {
            @Nested
            @Suppress("ClassName")
            inner class EnEstadoESPERANDO_DATOS
            {
                @Test
                fun no_llama_finalizarProceso_de_persona_cuando_api_personas_retorna_respuesta_Exitosa()
                {
                    mockearRespuestaDeRedExitosaAlConsultarSinFamiliares()
                    proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaCombinadaConLecturaEsperada)
                    verify(mockPersona, times(0)).finalizarProceso()
                }

                @Test
                fun no_llama_finalizarProceso_de_persona_cuando_api_personas_lanza_IllegalStateException()
                {
                    val errorEsperado = IllegalStateException()
                    erroresEsperados.add(errorEsperado)
                    mockearRespuestaDeRedInvalidaAlConsultar(errorEsperado)
                    proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                    verify(mockPersona, times(0)).finalizarProceso()
                }

                @Test
                fun no_llama_finalizarProceso_de_persona_cuando_api_personas_retorna_respuesta_ErrorTimeout()
                {
                    mockearRespuestaDeRedErrorTimeoutAlConsultar()
                    proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                    verify(mockPersona, times(0)).finalizarProceso()
                }

                @Test
                fun no_llama_finalizarProceso_de_persona_cuando_api_personas_retorna_respuesta_ErrorRed()
                {
                    mockearRespuestaDeRedErrorRedAlConsultar()
                    proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                    verify(mockPersona, times(0)).finalizarProceso()
                }

                @Test
                fun no_llama_finalizarProceso_de_persona_cuando_api_personas_persona_respuesta_ErrorBack()
                {
                    mockearRespuestaDeRedErrorBackAlConsultar("Error de pruebas")
                    proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                    verify(mockPersona, times(0)).finalizarProceso()
                }

                @Nested
                inner class ErrorGlobal
                {
                    private val testErrorGlobal by lazy { proceso.errorGlobal.test() }

                    @Test
                    fun emite_error_vacio_cuando_api_personas_retorna_respuesta_Exitosa()
                    {
                        mockearRespuestaDeRedExitosaAlConsultarSinFamiliares()
                        testErrorGlobal.assertValue(Opcional.Vacio())
                        testErrorGlobal.assertValueCount(1)
                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        testErrorGlobal.assertValueCount(2)
                        testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                    }

                    @Test
                    fun emite_error_vacio_cuando_api_personas_retorna_respuesta_ErrorBack_con_codigo_persona_con_familiares_no_existe()
                    {
                        doNothing().`when`(mockPersona).cambiarAfiliacion(cualquiera())
                        doNothing().`when`(mockPersona).cambiarCategoria(cualquiera())

                        mockearRespuestaDeRedErrorBackPersonaNoExisteAlConsultar()

                        testErrorGlobal.assertValue(Opcional.Vacio())
                        testErrorGlobal.assertValueCount(1)
                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        testErrorGlobal.assertValueCount(2)
                        testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                    }

                    @Test
                    fun emite_error_vacio_cuando_api_personas_lanza_IllegalStateException()
                    {
                        val errorEsperado = IllegalStateException()
                        erroresEsperados.add(errorEsperado)
                        mockearRespuestaDeRedInvalidaAlConsultar(errorEsperado)
                        testErrorGlobal.assertValue(Opcional.Vacio())
                        testErrorGlobal.assertValueCount(1)
                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        testErrorGlobal.assertValueCount(2)
                        testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                    }

                    @Test
                    fun emite_error_vacio_y_luego_error_correcto_cuando_api_personas_retorna_respuesta_ErrorTimeout()
                    {
                        mockearRespuestaDeRedErrorTimeoutAlConsultar()
                        testErrorGlobal.assertValue(Opcional.Vacio())
                        testErrorGlobal.assertValueCount(1)
                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        testErrorGlobal.assertValueCount(3)
                        testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                        testErrorGlobal.assertValueAt(2, Opcional.De("Timeout contactando el backend"))
                    }

                    @Test
                    fun emite_error_vacio_y_luego_error_correcto_cuando_api_personas_retorna_respuesta_ErrorRed()
                    {
                        mockearRespuestaDeRedErrorRedAlConsultar()
                        testErrorGlobal.assertValue(Opcional.Vacio())
                        testErrorGlobal.assertValueCount(1)
                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        testErrorGlobal.assertValueCount(3)
                        testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                        testErrorGlobal.assertValueAt(2, Opcional.De("Error contactando el backend"))
                    }

                    @Test
                    fun emite_error_vacio_y_luego_error_correcto_cuando_api_personas_retorna_respuesta_ErrorBack()
                    {
                        val mensajeError = "Error de pruebas"
                        mockearRespuestaDeRedErrorBackAlConsultar(mensajeError)
                        testErrorGlobal.assertValue(Opcional.Vacio())
                        testErrorGlobal.assertValueCount(1)
                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        testErrorGlobal.assertValueCount(3)
                        testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                        testErrorGlobal.assertValueAt(2, Opcional.De("Error en petición: $mensajeError"))
                    }
                }

                @Nested
                inner class PersonaCreada
                {
                    private val testPersonaCreada by lazy { proceso.personaCreada.test() }

                    @Test
                    fun no_emite_valor_cuando_api_personas_retorna_respuesta_Exitosa()
                    {
                        testPersonaCreada.assertValueCount(0)
                        mockearRespuestaDeRedExitosaAlConsultarSinFamiliares()
                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        testPersonaCreada.assertValueCount(0)
                    }

                    @Test
                    fun no_emite_valor_cuando_api_personas_lanza_IllegalStateException()
                    {
                        testPersonaCreada.assertValueCount(0)
                        val errorEsperado = IllegalStateException()
                        erroresEsperados.add(errorEsperado)
                        mockearRespuestaDeRedInvalidaAlConsultar(errorEsperado)
                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        testPersonaCreada.assertValueCount(0)
                    }

                    @Test
                    fun no_emite_valor_cuando_api_personas_retorna_respuesta_ErrorTimeout()
                    {
                        testPersonaCreada.assertValueCount(0)
                        mockearRespuestaDeRedErrorTimeoutAlConsultar()
                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        testPersonaCreada.assertValueCount(0)
                    }

                    @Test
                    fun no_emite_valor_cuando_api_personas_retorna_respuesta_ErrorRed()
                    {
                        testPersonaCreada.assertValueCount(0)
                        mockearRespuestaDeRedErrorRedAlConsultar()
                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        testPersonaCreada.assertValueCount(0)
                    }

                    @Test
                    fun no_emite_valor_cuando_api_personas_retorna_respuesta_ErrorBack()
                    {
                        testPersonaCreada.assertValueCount(0)
                        mockearRespuestaDeRedErrorBackAlConsultar("Error de pruebas")
                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        testPersonaCreada.assertValueCount(0)
                    }
                }

                @Nested
                inner class Familiares
                {
                    @Test
                    fun se_mantiene_como_lista_vacia_cuando_api_personas_retorna_respuesta_Exitosa_sin_familiares()
                    {
                        mockearRespuestaDeRedExitosaAlConsultarSinFamiliares()
                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        assertTrue(proceso.familiares.isEmpty())
                    }

                    @Test
                    fun cambia_a_lista_correcta_cuando_api_personas_retorna_respuesta_Exitosa_con_familiares()
                    {
                        mockearRespuestaDeRedExitosaAlConsultarConFamiliares()
                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        assertEquals(setOf(familiarPruebas), proceso.familiares)
                    }

                    @Test
                    fun cambia_a_lista_correcta_y_luego_a_lista_vacia_cuando_api_personas_retorna_respuesta_Exitosa_con_familiares_y_luego_respuesta_Exitosa_sin_familiares()
                    {
                        mockearRespuestaDeRedExitosaAlConsultarConFamiliares()
                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        assertEquals(setOf(familiarPruebas), proceso.familiares)

                        sujetoNumeroDocumento.onNext(Notification.createOnNext("otro"))
                        sujetoNumeroDocumento.onNext(Notification.createOnNext(documentoPersonaPruebas.numeroDocumento))
                        mockearRespuestaDeRedExitosaAlConsultarSinFamiliares()
                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        assertTrue(proceso.familiares.isEmpty())
                    }

                    @Test
                    fun se_mantiene_como_lista_vacia_personas_lanza_IllegalStateException()
                    {
                        val errorEsperado = IllegalStateException()
                        erroresEsperados.add(errorEsperado)
                        mockearRespuestaDeRedInvalidaAlConsultar(errorEsperado)
                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        assertTrue(proceso.familiares.isEmpty())
                    }

                    @Test
                    fun cambia_a_lista_correcta_y_se_mantiene_cuando_api_personas_retorna_respuesta_Exitosa_con_familiares_y_luego_respuesta_ExitosaVacia()
                    {
                        mockearRespuestaDeRedExitosaAlConsultarConFamiliares()
                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        assertEquals(setOf(familiarPruebas), proceso.familiares)

                        sujetoNumeroDocumento.onNext(Notification.createOnNext("otro"))
                        sujetoNumeroDocumento.onNext(Notification.createOnNext(documentoPersonaPruebas.numeroDocumento))
                        val errorEsperado = IllegalStateException()
                        erroresEsperados.add(errorEsperado)
                        mockearRespuestaDeRedInvalidaAlConsultar(errorEsperado)
                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        assertEquals(setOf(familiarPruebas), proceso.familiares)
                    }

                    @Test
                    fun se_mantiene_como_lista_vacia_personas_retorna_respuesta_ErrorTimeout()
                    {
                        mockearRespuestaDeRedErrorTimeoutAlConsultar()
                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        assertTrue(proceso.familiares.isEmpty())
                    }

                    @Test
                    fun cambia_a_lista_correcta_y_se_mantiene_cuando_api_personas_retorna_respuesta_Exitosa_con_familiares_y_luego_respuesta_ErrorTimeout()
                    {
                        mockearRespuestaDeRedExitosaAlConsultarConFamiliares()
                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        assertEquals(setOf(familiarPruebas), proceso.familiares)

                        sujetoNumeroDocumento.onNext(Notification.createOnNext("otro"))
                        sujetoNumeroDocumento.onNext(Notification.createOnNext(documentoPersonaPruebas.numeroDocumento))
                        mockearRespuestaDeRedErrorTimeoutAlConsultar()
                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        assertEquals(setOf(familiarPruebas), proceso.familiares)
                    }

                    @Test
                    fun se_mantiene_como_lista_vacia_personas_retorna_respuesta_ErrorRed()
                    {
                        mockearRespuestaDeRedErrorRedAlConsultar()
                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        assertTrue(proceso.familiares.isEmpty())
                    }

                    @Test
                    fun cambia_a_lista_correcta_y_se_mantiene_cuando_api_personas_retorna_respuesta_Exitosa_con_familiares_y_luego_respuesta_ErrorRed()
                    {
                        mockearRespuestaDeRedExitosaAlConsultarConFamiliares()
                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        assertEquals(setOf(familiarPruebas), proceso.familiares)

                        sujetoNumeroDocumento.onNext(Notification.createOnNext("otro"))
                        sujetoNumeroDocumento.onNext(Notification.createOnNext(documentoPersonaPruebas.numeroDocumento))
                        mockearRespuestaDeRedErrorRedAlConsultar()
                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        assertEquals(setOf(familiarPruebas), proceso.familiares)
                    }

                    @Test
                    fun se_mantiene_como_lista_vacia_personas_retorna_respuesta_ErrorBack()
                    {
                        mockearRespuestaDeRedErrorBackAlConsultar("Error de pruebas")
                        assertTrue(proceso.familiares.isEmpty())
                    }

                    @Test
                    fun cambia_a_lista_correcta_y_se_mantiene_cuando_api_personas_retorna_respuesta_Exitosa_con_familiares_y_luego_respuesta_ErrorBack()
                    {
                        mockearRespuestaDeRedExitosaAlConsultarConFamiliares()
                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        assertEquals(setOf(familiarPruebas), proceso.familiares)

                        sujetoNumeroDocumento.onNext(Notification.createOnNext("otro"))
                        sujetoNumeroDocumento.onNext(Notification.createOnNext(documentoPersonaPruebas.numeroDocumento))
                        mockearRespuestaDeRedErrorBackAlConsultar("Error de pruebas")
                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        assertEquals(setOf(familiarPruebas), proceso.familiares)
                    }

                    @Test
                    fun se_mantiene_como_lista_vacia_personas_retorna_respuesta_ErrorBack_con_codigo_persona_con_familiares_no_existe()
                    {
                        doNothing().`when`(mockPersona).cambiarAfiliacion(cualquiera())
                        doNothing().`when`(mockPersona).cambiarCategoria(cualquiera())
                        mockearRespuestaDeRedErrorBackPersonaNoExisteAlConsultar()

                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)

                        assertTrue(proceso.familiares.isEmpty())
                    }

                    @Test
                    @DisplayName("cambia_a_listas_correctas_cuando_api_personas_retorna_respuesta_Exitosa_con_familiares_y_luego_respuesta_ErrorBack_con_codigo_persona_con_familiares_no_existe")
                    fun cambia_a_listas_correctas_cuando_api_personas_retorna_personas_y_luego_cuando_no_retorna()
                    {

                        mockearRespuestaDeRedExitosaAlConsultarConFamiliares()
                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        assertEquals(setOf(familiarPruebas), proceso.familiares)

                        sujetoNumeroDocumento.onNext(Notification.createOnNext("otro"))
                        sujetoNumeroDocumento.onNext(Notification.createOnNext(documentoPersonaPruebas.numeroDocumento))

                        doNothing().`when`(mockPersona).cambiarAfiliacion(cualquiera())
                        doNothing().`when`(mockPersona).cambiarCategoria(cualquiera())

                        mockearRespuestaDeRedErrorBackPersonaNoExisteAlConsultar()

                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        assertTrue(proceso.familiares.isEmpty())
                    }
                }

                @Nested
                inner class AsignarPersona
                {
                    @Test
                    @DisplayName("Se invoca con argumento: la persona leída de lector pero con id, categoría y afiliación de la retornada por el back cuando retorna respuesta exitosa")
                    fun probar_combinacion_persona_lector_y_persona_back()
                    {
                        mockearRespuestaDeRedExitosaAlConsultarSinFamiliares()
                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        verify(mockPersona).asignarPersona(personaCombinadaConLecturaEsperada)
                    }

                    @Test
                    fun se_invoca_con_argumento_la_persona_leida_de_lector_cuando_api_personas_retorna_respuesta_exitosa_vacia()
                    {
                        mockearRespuestaDeRedExitosaAlConsultarSinFamiliares()
                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        verify(mockPersona).asignarPersona(personaCombinadaConLecturaEsperada)
                    }

                    @Test
                    fun se_invoca_con_persona_correcta_no_afiliada_y_categoria_d_cuando_api_personas_retorna_respuesta_persona_no_existe()
                    {
                        mockearRespuestaDeRedErrorBackPersonaNoExisteAlConsultar()

                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)

                        verify(mockPersona).asignarPersona(personaLeida.copiar(afiliacion = Persona.Afiliacion.NO_AFILIADO, categoria = Persona.Categoria.D))
                    }

                    @Test
                    fun no_se_llama_cuando_api_personas_lanza_IllegalStateException()
                    {
                        val errorEsperado = IllegalStateException()
                        erroresEsperados.add(errorEsperado)
                        mockearRespuestaDeRedInvalidaAlConsultar(errorEsperado)
                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        verify(mockPersona, times(0)).asignarPersona(personaEsperadaPruebas)
                    }

                    @Test
                    fun no_se_llama_cuando_api_personas_retorna_respuesta_ErrorTimeout()
                    {
                        mockearRespuestaDeRedErrorTimeoutAlConsultar()
                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        verify(mockPersona, times(0)).asignarPersona(personaEsperadaPruebas)
                    }

                    @Test
                    fun no_se_llama_cuando_api_personas_retorna_respuesta_ErrorRed()
                    {
                        mockearRespuestaDeRedErrorRedAlConsultar()
                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        verify(mockPersona, times(0)).asignarPersona(personaEsperadaPruebas)
                    }

                    @Test
                    fun no_se_llama_cuando_api_personas_retorna_respuesta_ErrorBack()
                    {
                        mockearRespuestaDeRedErrorBackAlConsultar("Error de pruebas")
                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        verify(mockPersona, times(0)).asignarPersona(personaEsperadaPruebas)
                    }
                }

                @Nested
                inner class Estado
                {
                    private val testEstado by lazy { proceso.estado.test() }

                    @Test
                    fun cambia_a_CONSULTANDO_PERSONA_y_luego_a_ESPERANDO_DATOS_cuando_api_de_personas_retorna_Exitosa()
                    {
                        mockearRespuestaDeRedExitosaAlConsultarSinFamiliares()
                        testEstado.assertValue(ProcesoCrearPersona.Estado.ESPERANDO_DATOS)
                        testEstado.assertValueCount(1)
                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        testEstado.assertValueCount(3)
                        testEstado.assertValueAt(1, ProcesoCrearPersona.Estado.CONSULTANDO_PERSONA)
                        testEstado.assertValueAt(2, ProcesoCrearPersona.Estado.ESPERANDO_DATOS)
                    }

                    @Test
                    fun cambia_a_CONSULTANDO_PERSONA_y_luego_a_ESPERANDO_DATOS_cuando_api_de_personas_retorna_ErrorBack_con_codigo_persona_con_familiares_no_existe()
                    {
                        doNothing().`when`(mockPersona).cambiarAfiliacion(cualquiera())
                        doNothing().`when`(mockPersona).cambiarCategoria(cualquiera())
                        mockearRespuestaDeRedErrorBackPersonaNoExisteAlConsultar()

                        testEstado.assertValue(ProcesoCrearPersona.Estado.ESPERANDO_DATOS)
                        testEstado.assertValueCount(1)

                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)

                        testEstado.assertValueCount(3)
                        testEstado.assertValueAt(1, ProcesoCrearPersona.Estado.CONSULTANDO_PERSONA)
                        testEstado.assertValueAt(2, ProcesoCrearPersona.Estado.ESPERANDO_DATOS)
                    }

                    @Test
                    fun cambia_a_CONSULTANDO_PERSONA_cuando_api_de_personas_lanza_IllegalStateException()
                    {
                        val errorEsperado = IllegalStateException()
                        erroresEsperados.add(errorEsperado)
                        mockearRespuestaDeRedInvalidaAlConsultar(errorEsperado)
                        testEstado.assertValue(ProcesoCrearPersona.Estado.ESPERANDO_DATOS)
                        testEstado.assertValueCount(1)
                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        testEstado.assertValueCount(2)
                        testEstado.assertValueAt(1, ProcesoCrearPersona.Estado.CONSULTANDO_PERSONA)
                    }

                    @Test
                    fun cambia_a_CONSULTANDO_PERSONA_y_luego_a_ESPERANDO_DATOS_cuando_api_de_personas_retorna_ErrorTimeout()
                    {
                        mockearRespuestaDeRedErrorTimeoutAlConsultar()
                        testEstado.assertValue(ProcesoCrearPersona.Estado.ESPERANDO_DATOS)
                        testEstado.assertValueCount(1)
                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        testEstado.assertValueCount(3)
                        testEstado.assertValueAt(1, ProcesoCrearPersona.Estado.CONSULTANDO_PERSONA)
                        testEstado.assertValueAt(2, ProcesoCrearPersona.Estado.ESPERANDO_DATOS)
                    }

                    @Test
                    fun cambia_a_CONSULTANDO_PERSONA_y_luego_a_ESPERANDO_DATOS_cuando_api_de_personas_retorna_ErrorBack()
                    {
                        mockearRespuestaDeRedErrorBackAlConsultar("Error de pruebas")
                        testEstado.assertValue(ProcesoCrearPersona.Estado.ESPERANDO_DATOS)
                        testEstado.assertValueCount(1)
                        proceso.consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaLeida)
                        testEstado.assertValueCount(3)
                        testEstado.assertValueAt(1, ProcesoCrearPersona.Estado.CONSULTANDO_PERSONA)
                        testEstado.assertValueAt(2, ProcesoCrearPersona.Estado.ESPERANDO_DATOS)
                    }
                }
            }
        }
    }
}