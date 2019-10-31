package co.smartobjects.ui.modelos.pagos

import co.smartobjects.entidades.operativas.compras.Pago
import co.smartobjects.ui.modelos.PruebasModelosRxBase
import co.smartobjects.ui.modelos.ResultadoAccionUI
import co.smartobjects.ui.modelos.mockConDefaultAnswer
import co.smartobjects.ui.modelos.verificarUltimoValorEmitido
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.Opcional
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.BehaviorSubject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import kotlin.test.assertEquals

@DisplayName("ProcesoAgregarPagoConSujetos")
internal class ProcesoAgregarPagoConSujetosPruebas : PruebasModelosRxBase()
{
    // Para estas pruebas se va a suponer que PagoUI funciona correctamente
    private val mockPago = mockConDefaultAnswer(PagoUI::class.java)

    private val pagoPruebas = Pago(Decimal.DIEZ, Pago.MetodoDePago.EFECTIVO, "123")

    //Nota: No se mockean observables para asegurar que no se usen directamente
    private val sujetoEsPagoValido = BehaviorSubject.create<Boolean>()

    @BeforeEach
    fun mockearEsPagoValido()
    {
        doReturn(sujetoEsPagoValido)
            .`when`(mockPago)
            .esPagoValido
    }

    private fun mockearDarPago()
    {
        doReturn(pagoPruebas)
            .`when`(mockPago)
            .aPago()
    }

    private fun mockearFinalizarProcesoPago()
    {
        doNothing()
            .`when`(mockPago)
            .finalizarProceso()
    }

    private val proceso: ProcesoAgregarPago by lazy { ProcesoAgregarPagoConSujetos(mockPago) }

    @Nested
    inner class Estado
    {
        private val testEstado by lazy { proceso.estado.test() }

        @Test
        fun empieza_con_valor_ESPERANDO_DATOS()
        {
            testEstado.assertValue(ProcesoAgregarPago.Estado.ESPERANDO_DATOS)
            testEstado.assertValueCount(1)
        }
    }

    @Nested
    inner class PuedeAgregarPago
    {
        private val testPuedeAgregarPago by lazy { proceso.puedeAgregarPago.test() }

        @Test
        fun empieza_con_valor_false()
        {
            testPuedeAgregarPago.assertValue(false)
            testPuedeAgregarPago.assertValueCount(1)
        }

        @Nested
        @Suppress("ClassName")
        inner class EnEstadoESPERANDO_DATOS
        {
            @Test
            fun emite_true_cuando_esPagoValido_en_pago_cambia_a_true()
            {
                testPuedeAgregarPago.assertValue(false)
                testPuedeAgregarPago.assertValueCount(1)
                sujetoEsPagoValido.onNext(true)
                testPuedeAgregarPago.assertValueCount(2)
                testPuedeAgregarPago.assertValueAt(1, true)
            }

            @Test
            fun emite_false_cuando_esPagoValido_en_pago_cambia_a_false()
            {
                testPuedeAgregarPago.assertValue(false)
                testPuedeAgregarPago.assertValueCount(1)
                sujetoEsPagoValido.onNext(true)
                testPuedeAgregarPago.assertValueCount(2)
                testPuedeAgregarPago.assertValueAt(1, true)
                sujetoEsPagoValido.onNext(false)
                testPuedeAgregarPago.assertValueCount(3)
                testPuedeAgregarPago.assertValueAt(2, false)
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
    inner class IntentarAgregarPago
    {
        private val mockModeloUIConListaDePagos = mockConDefaultAnswer(ModeloUIConListaDePagos::class.java)

        @Nested
        @Suppress("ClassName")
        inner class EnEstadoESPERANDO_DATOS
        {
            @Test
            fun retorna_MODELO_EN_ESTADO_INVALIDO_cuando_pago_no_a_emitido_valor_sobre_esPagoValido()
            {
                assertEquals(ResultadoAccionUI.MODELO_EN_ESTADO_INVALIDO, proceso.intentarAgregarPago(mockModeloUIConListaDePagos))
            }

            @Test
            fun retorna_MODELO_EN_ESTADO_INVALIDO_cuando_persona_emitio_false_sobre_esPagoValido()
            {
                sujetoEsPagoValido.onNext(false)
                assertEquals(ResultadoAccionUI.MODELO_EN_ESTADO_INVALIDO, proceso.intentarAgregarPago(mockModeloUIConListaDePagos))
            }

            @Nested
            inner class ConObservalePagoValidoTrueCuandoAPagoLanzaExcepcion
            {
                @BeforeEach
                fun emitirEsPagoValidoYFallarEnAPago()
                {
                    doThrow(IllegalStateException("Error"))
                        .`when`(mockPago)
                        .aPago()
                    sujetoEsPagoValido.onNext(true)
                }

                @Test
                fun retorna_OBSERVABLES_EN_ESTADO_INVALIDO()
                {
                    assertEquals(ResultadoAccionUI.OBSERVABLES_EN_ESTADO_INVALIDO, proceso.intentarAgregarPago(mockModeloUIConListaDePagos))
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
                        proceso.intentarAgregarPago(mockModeloUIConListaDePagos)
                        testErrorGlobal.assertValueCount(2)
                        testErrorGlobal.assertValueAt(1, Opcional.De("Pago inválido"))
                    }
                }
            }

            @Nested
            inner class ConPagoValido
            {
                @BeforeEach
                private fun emitirEsPagoValidoYMockear()
                {
                    mockearDarPago()
                    mockearFinalizarProcesoPago()
                    sujetoEsPagoValido.onNext(true)
                }

                @Nested
                inner class Exitoso
                {
                    @BeforeEach
                    private fun mockearPagoExitoso()
                    {
                        doReturn(Opcional.Vacio<String>())
                            .`when`(mockModeloUIConListaDePagos)
                            .agregarPago(pagoPruebas)
                    }

                    @Test
                    fun retorna_ACCION_INICIADA_cuando_modeloUIConListaDePagos_retorna_empty()
                    {
                        assertEquals(ResultadoAccionUI.ACCION_INICIADA, proceso.intentarAgregarPago(mockModeloUIConListaDePagos))
                    }

                    @Test
                    fun NO_llama_finalizarProceso_de_modeloUIConListaDePagos_cuando_modeloUIConListaDePagos_retorna_empty()
                    {
                        proceso.intentarAgregarPago(mockModeloUIConListaDePagos)
                        verify(mockModeloUIConListaDePagos, times(0)).finalizarProceso()
                    }

                    @Nested
                    inner class ErrorGlobal
                    {
                        private val testErrorGlobal by lazy { proceso.errorGlobal.test() }

                        @Test
                        fun emite_error_vacio_cuando_modeloUIConListaDePagos_retorna_empty()
                        {
                            testErrorGlobal.assertValue(Opcional.Vacio())
                            testErrorGlobal.assertValueCount(1)
                            proceso.intentarAgregarPago(mockModeloUIConListaDePagos)
                            testErrorGlobal.assertValueCount(2)
                            testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                        }
                    }

                    @Nested
                    inner class Estado
                    {
                        private val testEstado by lazy { proceso.estado.test() }

                        @Test
                        fun cambia_a_PAGO_AGREGADO_cuando_modeloUIConListaDePagos_retorna_empty()
                        {
                            testEstado.assertValue(ProcesoAgregarPago.Estado.ESPERANDO_DATOS)
                            testEstado.assertValueCount(1)
                            proceso.intentarAgregarPago(mockModeloUIConListaDePagos)
                            testEstado.assertValueCount(2)
                            testEstado.assertValueAt(1, ProcesoAgregarPago.Estado.PAGO_AGREGADO)
                        }
                    }
                }

                @Nested
                inner class ConError
                {
                    private fun mockearAgregarPagoConError(errorEsperado: String)
                    {
                        doReturn(Opcional.De(errorEsperado))
                            .`when`(mockModeloUIConListaDePagos)
                            .agregarPago(pagoPruebas)
                    }

                    @Test
                    fun retorna_ACCION_INICIADA_cuando_modeloUIConListaDePagos_retorna_error()
                    {
                        mockearAgregarPagoConError("Error")
                        assertEquals(ResultadoAccionUI.ACCION_INICIADA, proceso.intentarAgregarPago(mockModeloUIConListaDePagos))
                    }

                    @Test
                    fun no_llama_finalizarProceso_de_pago_cuando_modeloUIConListaDePagos_retorna_error()
                    {
                        mockearAgregarPagoConError("Error")
                        proceso.intentarAgregarPago(mockModeloUIConListaDePagos)
                        verify(mockPago, times(0)).finalizarProceso()
                    }

                    @Test
                    fun no_llama_finalizarProceso_de_modeloUIConListaDePagos_cuando_modeloUIConListaDePagos_retorna_error()
                    {
                        mockearAgregarPagoConError("Error")
                        proceso.intentarAgregarPago(mockModeloUIConListaDePagos)
                        verify(mockModeloUIConListaDePagos, times(0)).finalizarProceso()
                    }

                    @Nested
                    inner class ErrorGlobal
                    {
                        private val testErrorGlobal by lazy { proceso.errorGlobal.test() }

                        @Test
                        fun emite_error_vacio_y_luego_error_correcto_cuando_modeloUIConListaDePagos_retorna_error()
                        {
                            val errorEsperado = "Error de prueba"
                            mockearAgregarPagoConError(errorEsperado)
                            testErrorGlobal.assertValue(Opcional.Vacio())
                            testErrorGlobal.assertValueCount(1)
                            proceso.intentarAgregarPago(mockModeloUIConListaDePagos)
                            testErrorGlobal.assertValueCount(3)
                            testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                            testErrorGlobal.assertValueAt(2, Opcional.De(errorEsperado))
                        }
                    }

                    @Nested
                    inner class Estado
                    {
                        private val testEstado by lazy { proceso.estado.test() }

                        @Test
                        fun no_cambia_cuando_modeloUIConListaDePagos_retorna_error()
                        {
                            mockearAgregarPagoConError("Error")
                            testEstado.assertValue(ProcesoAgregarPago.Estado.ESPERANDO_DATOS)
                            testEstado.assertValueCount(1)
                            proceso.intentarAgregarPago(mockModeloUIConListaDePagos)
                            testEstado.assertValueCount(1)
                        }
                    }
                }
            }
        }
    }

    @Nested
    inner class Reiniciar
    {
        private val mockModeloUIConListaDePagos = mockConDefaultAnswer(ModeloUIConListaDePagos::class.java)

        private fun mockearAgregarPagoExitoso()
        {
            mockearDarPago()
            mockearFinalizarProcesoPago()
            sujetoEsPagoValido.onNext(true)

            doReturn(Opcional.Vacio<String>())
                .`when`(mockModeloUIConListaDePagos)
                .agregarPago(pagoPruebas)
        }

        private fun <T> mockearYDarObservador(observable: Observable<T>): TestObserver<T>
        {
            val observador = observable.test()

            mockearAgregarPagoExitoso()
            proceso.intentarAgregarPago(mockModeloUIConListaDePagos)

            return observador
        }

        @Test
        fun deja_el_estado_en_esperando_datos()
        {
            val observador = mockearYDarObservador(proceso.estado)

            proceso.reiniciar()

            observador.assertValuesOnly(
                    ProcesoAgregarPago.Estado.ESPERANDO_DATOS,
                    ProcesoAgregarPago.Estado.PAGO_AGREGADO,
                    ProcesoAgregarPago.Estado.ESPERANDO_DATOS
                                       )
        }

        @Test
        fun deja_puede_agregar_pago_en_false()
        {
            val observador = mockearYDarObservador(proceso.puedeAgregarPago)

            proceso.reiniciar()

            observador.verificarUltimoValorEmitido(false)
        }

        @Test
        fun borra_el_error_global()
        {
            val observador = mockearYDarObservador(proceso.errorGlobal)

            proceso.reiniciar()

            observador.verificarUltimoValorEmitido(Opcional.Vacio())
        }
    }
}