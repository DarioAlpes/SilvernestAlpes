package co.smartobjects.ui.modelos.registropersonas

import co.smartobjects.entidades.excepciones.EntidadConCampoVacio
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.personas.PersonasDeUnaCompraAPI
import co.smartobjects.red.modelos.ErrorDePeticion
import co.smartobjects.ui.modelos.PruebasModelosRxBase
import co.smartobjects.ui.modelos.ResultadoAccionUI
import co.smartobjects.ui.modelos.mockConDefaultAnswer
import co.smartobjects.ui.modelos.schedulerThreadActual
import co.smartobjects.utilidades.Opcional
import io.reactivex.Notification
import org.junit.jupiter.api.*
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.doThrow
import org.threeten.bp.LocalDate
import java.io.IOException

@DisplayName("ProcesoConsultarPersonasPorNumeroTransaccionConSujetos")
internal class ProcesoConsultarPersonasPorNumeroTransaccionConSujetosPruebas : PruebasModelosRxBase()
{
    companion object
    {
        private const val ID_CLIENTE = 10L
    }

    // Para estas pruebas se va a suponer que PersonaUI funciona correctamente
    private val mockApiPersonasPorNumeroTransaccion = mockConDefaultAnswer(PersonasDeUnaCompraAPI::class.java)

    private val personaPruebas1 = Persona(
            ID_CLIENTE,
            1,
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

    private val personaPruebas2 = Persona(
            ID_CLIENTE,
            2,
            "Pruebas",
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

    private val numeroTransaccionPruebas = "#890"

    private fun mockearRespuestaDeRedExitosaAlConsultar()
    {
        doReturn(RespuestaIndividual.Exitosa(listOf(personaPruebas1, personaPruebas2)))
            .`when`(mockApiPersonasPorNumeroTransaccion)
            .consultar(numeroTransaccionPruebas)
    }

    private fun mockearRespuestaDeRedInvalidaAlConsultar(errorEsperado: IllegalStateException)
    {
        doThrow(errorEsperado)
            .`when`(mockApiPersonasPorNumeroTransaccion)
            .consultar(numeroTransaccionPruebas)
    }

    private fun mockearRespuestaDeRedErrorTimeoutAlConsultar()
    {
        doReturn(RespuestaIndividual.Error.Timeout<List<Persona>>())
            .`when`(mockApiPersonasPorNumeroTransaccion)
            .consultar(numeroTransaccionPruebas)
    }

    private fun mockearRespuestaDeRedErrorRedAlConsultar()
    {
        doReturn(RespuestaIndividual.Error.Red<List<Persona>>(IOException("Error")))
            .`when`(mockApiPersonasPorNumeroTransaccion)
            .consultar(numeroTransaccionPruebas)
    }

    private fun mockearRespuestaDeRedErrorBackAlConsultar(mensajeError: String)
    {
        doReturn(RespuestaIndividual.Error.Back<List<Persona>>(400, ErrorDePeticion(100, mensajeError)))
            .`when`(mockApiPersonasPorNumeroTransaccion)
            .consultar(numeroTransaccionPruebas)
    }

    private val proceso: ProcesoConsultarPersonasPorNumeroTransaccion by lazy { ProcesoConsultarPersonasPorNumeroTransaccionConSujetos(mockApiPersonasPorNumeroTransaccion, schedulerThreadActual) }

    @Nested
    inner class PersonasConsultadas
    {
        private val testPersonasConsultadas by lazy { proceso.personasConsultadas.test() }

        @Test
        fun empieza_sin_valor()
        {
            testPersonasConsultadas.assertValueCount(0)
        }
    }

    @Nested
    inner class CambiarNumeroTransaccion
    {
        private val testNumeroTransaccionPOS = proceso.numeroTransaccionPOS.test()

        @Test
        fun no_emite_valor_al_inicializar()
        {
            testNumeroTransaccionPOS.assertValueCount(0)
        }

        @Test
        fun con_valor_valido_emite_valor_cambiado_una_vez_por_cada_llamado()
        {
            val numeroEventos = 10
            (0 until numeroEventos).forEach {
                val numeroTransaccion = "123-$it"
                proceso.cambiarNumeroTransaccionPOS(numeroTransaccion)
                testNumeroTransaccionPOS.assertValueAt(it, Notification.createOnNext(numeroTransaccion))
            }
            testNumeroTransaccionPOS.assertValueCount(numeroEventos)
        }

        @Test
        fun con_espacios_emite_valor_con_trim()
        {
            proceso.cambiarNumeroTransaccionPOS("        123-4       ")
            testNumeroTransaccionPOS.assertValue(Notification.createOnNext("123-4"))
            testNumeroTransaccionPOS.assertValueCount(1)
        }

        @Test
        fun con_valor_vacio_emite_error_EntidadConCampoVacio()
        {
            proceso.cambiarNumeroTransaccionPOS("")
            testNumeroTransaccionPOS.assertValue({ it.isOnError })
            testNumeroTransaccionPOS.assertValue({ it.error!! is EntidadConCampoVacio })
            testNumeroTransaccionPOS.assertValueCount(1)
        }

        @Test
        fun con_valor_con_espacios_y_tabs_emite_error_EntidadConCampoVacio()
        {
            proceso.cambiarNumeroTransaccionPOS("             ")
            testNumeroTransaccionPOS.assertValue({ it.isOnError })
            testNumeroTransaccionPOS.assertValue({ it.error!! is EntidadConCampoVacio })
            testNumeroTransaccionPOS.assertValueCount(1)
        }

        @Test
        fun a_error_y_luego_a_valor_valido_emite_ambos_valores()
        {
            proceso.cambiarNumeroTransaccionPOS("")
            proceso.cambiarNumeroTransaccionPOS("123-4")
            testNumeroTransaccionPOS.assertValueAt(0, { it.isOnError })
            testNumeroTransaccionPOS.assertValueAt(0, { it.error!! is EntidadConCampoVacio })
            testNumeroTransaccionPOS.assertValueAt(1, Notification.createOnNext("123-4"))
            testNumeroTransaccionPOS.assertValueCount(2)
        }

        @Test
        fun a_valor_valido_y_luego_a_error_emite_ambos_valores()
        {
            proceso.cambiarNumeroTransaccionPOS("123-4")
            proceso.cambiarNumeroTransaccionPOS("")
            testNumeroTransaccionPOS.assertValueAt(0, Notification.createOnNext("123-4"))
            testNumeroTransaccionPOS.assertValueAt(1, { it.isOnError })
            testNumeroTransaccionPOS.assertValueAt(1, { it.error!! is EntidadConCampoVacio })
            testNumeroTransaccionPOS.assertValueCount(2)
        }

        @Test
        fun no_emite_mas_eventos_despues_de_llamar_finalizar_proceso()
        {
            proceso.finalizarProceso()
            proceso.cambiarNumeroTransaccionPOS("123-4")
            proceso.cambiarNumeroTransaccionPOS("")
            testNumeroTransaccionPOS.assertValueCount(0)
        }
    }

    @Nested
    inner class Estado
    {
        private val testEstado by lazy { proceso.estado.test() }

        @Test
        fun empieza_con_valor_ESPERANDO_DATOS()
        {
            testEstado.assertValue(ProcesoConsultarPersonasPorNumeroTransaccion.Estado.ESPERANDO_DATOS)
            testEstado.assertValueCount(1)
        }
    }

    @Nested
    inner class PuedeConsultarPersonas
    {
        private val testPuedeConsultarPersonas = proceso.puedeConsultarPersonas.test()

        @Test
        fun empieza_con_valor_false()
        {
            testPuedeConsultarPersonas.assertValue(false)
            testPuedeConsultarPersonas.assertValueCount(1)
        }

        @Nested
        @Suppress("ClassName")
        inner class EnEstadoESPERANDO_DATOS
        {
            @Test
            fun emite_true_al_cambiar_numero_transaccion_a_valor_valido()
            {
                proceso.cambiarNumeroTransaccionPOS("123-4")
                testPuedeConsultarPersonas.assertValueCount(2)
                testPuedeConsultarPersonas.assertValueAt(1, true)
            }

            @Test
            fun emite_true_y_luego_false_al_cambiar_numero_transaccion_a_valor_valido_y_luego_a_valor_invalido()
            {
                proceso.cambiarNumeroTransaccionPOS("123-4")
                proceso.cambiarNumeroTransaccionPOS("   ")
                testPuedeConsultarPersonas.assertValueCount(3)
                testPuedeConsultarPersonas.assertValueAt(2, false)
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
    inner class IntentarConsultarPersonasPorNumeroTransaccion
    {
        @Nested
        @Suppress("ClassName")
        inner class EnEstadoESPERANDO_DATOS
        {
            @Test
            fun retorna_OBSERVABLES_EN_ESTADO_INVALIDO_cuando_no_se_a_asignado_valor_en_numero_de_transaccion()
            {
                Assertions.assertEquals(ResultadoAccionUI.OBSERVABLES_EN_ESTADO_INVALIDO, proceso.intentarConsultarPersonasPorNumeroTransaccion())
            }

            @Test
            fun retorna_OBSERVABLES_EN_ESTADO_INVALIDO_cuando_se_a_asigno_valor_invalido_en_numero_de_transaccion()
            {
                proceso.cambiarNumeroTransaccionPOS("   ")
                Assertions.assertEquals(ResultadoAccionUI.OBSERVABLES_EN_ESTADO_INVALIDO, proceso.intentarConsultarPersonasPorNumeroTransaccion())
            }

            @Nested
            inner class ConNumeroDeTransaccionValido
            {
                @BeforeEach
                private fun cambiarNumeroDeTransaccionAValorValido()
                {
                    proceso.cambiarNumeroTransaccionPOS(numeroTransaccionPruebas)
                }

                @Test
                fun retorna_ACCION_INICIADA_cuando_api_red_lanza_IllegalStateException()
                {
                    mockearRespuestaDeRedExitosaAlConsultar()
                    Assertions.assertEquals(ResultadoAccionUI.ACCION_INICIADA, proceso.intentarConsultarPersonasPorNumeroTransaccion())
                }

                @Test
                fun retorna_ACCION_INICIADA_cuando_api_red_lanza_IllegalStateExceptionVacia()
                {
                    val errorEsperado = IllegalStateException()
                    erroresEsperados.add(errorEsperado)
                    mockearRespuestaDeRedInvalidaAlConsultar(errorEsperado)
                    Assertions.assertEquals(ResultadoAccionUI.ACCION_INICIADA, proceso.intentarConsultarPersonasPorNumeroTransaccion())
                }

                @Test
                fun retorna_ACCION_INICIADA_cuando_api_red_retorna_respuesta_ErrorTimeout()
                {
                    mockearRespuestaDeRedErrorTimeoutAlConsultar()
                    Assertions.assertEquals(ResultadoAccionUI.ACCION_INICIADA, proceso.intentarConsultarPersonasPorNumeroTransaccion())
                }

                @Test
                fun retorna_ACCION_INICIADA_cuando_api_red_retorna_respuesta_ErrorRed()
                {
                    mockearRespuestaDeRedErrorRedAlConsultar()
                    Assertions.assertEquals(ResultadoAccionUI.ACCION_INICIADA, proceso.intentarConsultarPersonasPorNumeroTransaccion())
                }

                @Test
                fun retorna_ACCION_INICIADA_cuando_api_red_retorna_respuesta_ErrorBack()
                {
                    mockearRespuestaDeRedErrorBackAlConsultar("Error de pruebas")
                    Assertions.assertEquals(ResultadoAccionUI.ACCION_INICIADA, proceso.intentarConsultarPersonasPorNumeroTransaccion())
                }

                @Nested
                inner class ErrorGlobal
                {
                    private val testErrorGlobal = proceso.errorGlobal.test()

                    @Test
                    fun emite_error_vacio_cuando_api_red_lanza_IllegalStateException()
                    {
                        mockearRespuestaDeRedExitosaAlConsultar()
                        testErrorGlobal.assertValue(Opcional.Vacio())
                        testErrorGlobal.assertValueCount(1)
                        proceso.intentarConsultarPersonasPorNumeroTransaccion()
                        testErrorGlobal.assertValueCount(2)
                        testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                    }

                    @Test
                    fun emite_error_vacio_cuando_api_red_lanza_IllegalStateExceptionVacia()
                    {
                        val errorEsperado = IllegalStateException()
                        erroresEsperados.add(errorEsperado)
                        mockearRespuestaDeRedInvalidaAlConsultar(errorEsperado)
                        testErrorGlobal.assertValue(Opcional.Vacio())
                        testErrorGlobal.assertValueCount(1)
                        proceso.intentarConsultarPersonasPorNumeroTransaccion()
                        testErrorGlobal.assertValueCount(2)
                        testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                    }

                    @Test
                    fun emite_error_vacio_y_luego_error_correcto_cuando_api_red_retorna_respuesta_ErrorTimeout()
                    {
                        mockearRespuestaDeRedErrorTimeoutAlConsultar()
                        testErrorGlobal.assertValue(Opcional.Vacio())
                        testErrorGlobal.assertValueCount(1)
                        proceso.intentarConsultarPersonasPorNumeroTransaccion()
                        testErrorGlobal.assertValueCount(3)
                        testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                        testErrorGlobal.assertValueAt(2, Opcional.De("Timeout contactando el backend"))
                    }

                    @Test
                    fun emite_error_vacio_y_luego_error_correcto_cuando_api_red_retorna_respuesta_ErrorRed()
                    {
                        mockearRespuestaDeRedErrorRedAlConsultar()
                        testErrorGlobal.assertValue(Opcional.Vacio())
                        testErrorGlobal.assertValueCount(1)
                        proceso.intentarConsultarPersonasPorNumeroTransaccion()
                        testErrorGlobal.assertValueCount(3)
                        testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                        testErrorGlobal.assertValueAt(2, Opcional.De("Error contactando el backend"))
                    }

                    @Test
                    fun emite_error_vacio_y_luego_error_correcto_cuando_api_red_retorna_respuesta_ErrorBack()
                    {
                        val mensajeError = "Error de pruebas"
                        mockearRespuestaDeRedErrorBackAlConsultar(mensajeError)
                        testErrorGlobal.assertValue(Opcional.Vacio())
                        testErrorGlobal.assertValueCount(1)
                        proceso.intentarConsultarPersonasPorNumeroTransaccion()
                        testErrorGlobal.assertValueCount(3)
                        testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                        testErrorGlobal.assertValueAt(2, Opcional.De("Error en petición: $mensajeError"))
                    }
                }

                @Nested
                inner class PersonasConsultadas
                {
                    private val testPersonasConsultadas = proceso.personasConsultadas.test()

                    @Test
                    fun emite_valor_correcto_cuando_api_red_lanza_IllegalStateException()
                    {
                        testPersonasConsultadas.assertValueCount(0)
                        mockearRespuestaDeRedExitosaAlConsultar()
                        proceso.intentarConsultarPersonasPorNumeroTransaccion()
                        testPersonasConsultadas.assertValueCount(1)
                        testPersonasConsultadas.assertValue(listOf(personaPruebas1, personaPruebas2))
                    }

                    @Test
                    fun no_emite_valor_cuando_api_red_lanza_IllegalStateExceptionVacia()
                    {
                        testPersonasConsultadas.assertValueCount(0)
                        val errorEsperado = IllegalStateException()
                        erroresEsperados.add(errorEsperado)
                        mockearRespuestaDeRedInvalidaAlConsultar(errorEsperado)
                        proceso.intentarConsultarPersonasPorNumeroTransaccion()
                        testPersonasConsultadas.assertValueCount(0)
                    }

                    @Test
                    fun no_emite_valor_cuando_api_red_retorna_respuesta_ErrorTimeout()
                    {
                        testPersonasConsultadas.assertValueCount(0)
                        mockearRespuestaDeRedErrorTimeoutAlConsultar()
                        proceso.intentarConsultarPersonasPorNumeroTransaccion()
                        testPersonasConsultadas.assertValueCount(0)
                    }

                    @Test
                    fun no_emite_valor_cuando_api_red_retorna_respuesta_ErrorRed()
                    {
                        testPersonasConsultadas.assertValueCount(0)
                        mockearRespuestaDeRedErrorRedAlConsultar()
                        proceso.intentarConsultarPersonasPorNumeroTransaccion()
                        testPersonasConsultadas.assertValueCount(0)
                    }

                    @Test
                    fun no_emite_valor_cuando_api_red_retorna_respuesta_ErrorBack()
                    {
                        testPersonasConsultadas.assertValueCount(0)
                        mockearRespuestaDeRedErrorBackAlConsultar("Error de pruebas")
                        proceso.intentarConsultarPersonasPorNumeroTransaccion()
                        testPersonasConsultadas.assertValueCount(0)
                    }
                }

                @Nested
                inner class PuedeConsultarPersonas
                {
                    private val testPuedeConsultarPersonas = proceso.puedeConsultarPersonas.test()

                    @Test
                    fun emite_false_cuando_api_red_lanza_IllegalStateException()
                    {
                        val errorEsperado = IllegalStateException()
                        erroresEsperados.add(errorEsperado)
                        mockearRespuestaDeRedInvalidaAlConsultar(errorEsperado)
                        proceso.intentarConsultarPersonasPorNumeroTransaccion()
                        testPuedeConsultarPersonas.assertValueCount(3)
                        testPuedeConsultarPersonas.assertValueAt(2, false)
                    }

                    @Test
                    fun emite_false_y_luego_true_cuando_api_red_retorna_respuesta_ErrorTimeout()
                    {
                        mockearRespuestaDeRedErrorTimeoutAlConsultar()
                        proceso.intentarConsultarPersonasPorNumeroTransaccion()
                        testPuedeConsultarPersonas.assertValueCount(4)
                        testPuedeConsultarPersonas.assertValueAt(2, false)
                        testPuedeConsultarPersonas.assertValueAt(3, true)
                    }

                    @Test
                    fun emite_false_y_luego_true_cuando_api_red_retorna_respuesta_ErrorRed()
                    {
                        mockearRespuestaDeRedErrorRedAlConsultar()
                        proceso.intentarConsultarPersonasPorNumeroTransaccion()
                        testPuedeConsultarPersonas.assertValueCount(4)
                        testPuedeConsultarPersonas.assertValueAt(2, false)
                        testPuedeConsultarPersonas.assertValueAt(3, true)
                    }

                    @Test
                    fun emite_false_y_luego_true_cuando_api_red_retorna_respuesta_ErrorBack()
                    {
                        mockearRespuestaDeRedErrorBackAlConsultar("Error de pruebas")
                        proceso.intentarConsultarPersonasPorNumeroTransaccion()
                        testPuedeConsultarPersonas.assertValueCount(4)
                        testPuedeConsultarPersonas.assertValueAt(2, false)
                        testPuedeConsultarPersonas.assertValueAt(3, true)
                    }
                }

                @Nested
                inner class Estado
                {
                    private val testEstado = proceso.estado.test()

                    @Test
                    fun cambia_a_CONSULTANDO_PERSONAS_y_luego_a_PERSONAS_CONSULTADAS_cuando_api_de_personas_retorna_Exitosa()
                    {
                        mockearRespuestaDeRedExitosaAlConsultar()
                        testEstado.assertValue(ProcesoConsultarPersonasPorNumeroTransaccion.Estado.ESPERANDO_DATOS)
                        testEstado.assertValueCount(1)
                        proceso.intentarConsultarPersonasPorNumeroTransaccion()
                        testEstado.assertValueCount(3)
                        testEstado.assertValueAt(1, ProcesoConsultarPersonasPorNumeroTransaccion.Estado.CONSULTANDO_PERSONAS)
                        testEstado.assertValueAt(2, ProcesoConsultarPersonasPorNumeroTransaccion.Estado.ESPERANDO_DATOS)
                    }

                    @Test
                    fun cambia_a_CONSULTANDO_PERSONAS_cuando_api_de_personas_lanza_IllegalStateException()
                    {
                        val errorEsperado = IllegalStateException()
                        erroresEsperados.add(errorEsperado)
                        mockearRespuestaDeRedInvalidaAlConsultar(errorEsperado)
                        testEstado.assertValue(ProcesoConsultarPersonasPorNumeroTransaccion.Estado.ESPERANDO_DATOS)
                        testEstado.assertValueCount(1)
                        proceso.intentarConsultarPersonasPorNumeroTransaccion()
                        testEstado.assertValueCount(2)
                        testEstado.assertValueAt(1, ProcesoConsultarPersonasPorNumeroTransaccion.Estado.CONSULTANDO_PERSONAS)
                    }

                    @Test
                    fun cambia_a_CONSULTANDO_PERSONAS_y_luego_a_ESPERANDO_DATOS_cuando_api_de_personas_retorna_ErrorTimeout()
                    {
                        mockearRespuestaDeRedErrorTimeoutAlConsultar()
                        testEstado.assertValue(ProcesoConsultarPersonasPorNumeroTransaccion.Estado.ESPERANDO_DATOS)
                        testEstado.assertValueCount(1)
                        proceso.intentarConsultarPersonasPorNumeroTransaccion()
                        testEstado.assertValueCount(3)
                        testEstado.assertValueAt(1, ProcesoConsultarPersonasPorNumeroTransaccion.Estado.CONSULTANDO_PERSONAS)
                        testEstado.assertValueAt(2, ProcesoConsultarPersonasPorNumeroTransaccion.Estado.ESPERANDO_DATOS)
                    }

                    @Test
                    fun cambia_a_CONSULTANDO_PERSONAS_y_luego_a_ESPERANDO_DATOS_cuando_api_de_personas_retorna_ErrorBack()
                    {
                        mockearRespuestaDeRedErrorBackAlConsultar("Error de pruebas")
                        testEstado.assertValue(ProcesoConsultarPersonasPorNumeroTransaccion.Estado.ESPERANDO_DATOS)
                        testEstado.assertValueCount(1)
                        proceso.intentarConsultarPersonasPorNumeroTransaccion()
                        testEstado.assertValueCount(3)
                        testEstado.assertValueAt(1, ProcesoConsultarPersonasPorNumeroTransaccion.Estado.CONSULTANDO_PERSONAS)
                        testEstado.assertValueAt(2, ProcesoConsultarPersonasPorNumeroTransaccion.Estado.ESPERANDO_DATOS)
                    }
                }
            }
        }
    }
}