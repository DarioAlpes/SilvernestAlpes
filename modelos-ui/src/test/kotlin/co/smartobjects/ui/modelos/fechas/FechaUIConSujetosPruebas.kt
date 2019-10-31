package co.smartobjects.ui.modelos.fechas

import co.smartobjects.campos.CampoEntidad
import co.smartobjects.campos.ValidadorCampo
import co.smartobjects.entidades.excepciones.EntidadMalInicializada
import co.smartobjects.ui.modelos.PruebasModelosRxBase
import io.reactivex.Notification
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Nested
import org.threeten.bp.LocalDate

@DisplayName("FechaUIConSujetos")
internal class FechaUIConSujetosPruebas: PruebasModelosRxBase()
{
    private class CampoSinValidacion(fecha: LocalDate): CampoEntidad<Any, LocalDate>(fecha, null, "Entidad", "Campo")
    private class CampoConValidacionControlada(fecha: LocalDate, fallar: Boolean, excepcionEsperada: Exception):
            CampoEntidad<Any, LocalDate>(
                    fecha,
                    object: ValidadorCampo<LocalDate>
                    {
                        override fun validarCampo(valorLimpio: LocalDate, valorOriginal: LocalDate, nombreEntidad: String, nombreCampo: String)
                        {
                            if(fallar)
                            {
                                throw excepcionEsperada
                            }
                        }

                    },
                    "Entidad",
                    "Campo")

    @Nested
    inner class ConCampoConValidacion
    {
        private val excepcionEsperada = EntidadMalInicializada("Entidad", "Campo", "Un valor", "Debe fallar")
        private var fallar: Boolean = false
        private val modelo: FechaUI = FechaUIConSujetos({ CampoConValidacionControlada(it, fallar, excepcionEsperada) })

        @Nested
        inner class CambiarDia
        {
            private val testDia = modelo.dia.test()

            @Test
            fun emite_valor_correcto_al_cambiar_todos_los_campos_a_valor_valido_con_validacion_exitosa()
            {
                fallar = false
                modelo.cambiarDia("1")
                modelo.cambiarMes("2")
                modelo.cambiarAño("3")
                testDia.assertValue(Notification.createOnNext(1))
                testDia.assertValueCount(1)
            }

            @Test
            fun emite_valor_correcto_al_cambiar_todos_los_campos_a_valor_valido_con_validacion_fallida()
            {
                fallar = true
                modelo.cambiarDia("1")
                modelo.cambiarMes("2")
                modelo.cambiarAño("3")
                testDia.assertValue(Notification.createOnNext(1))
                testDia.assertValueCount(1)
            }
        }

        @Nested
        inner class CambiarMes
        {
            private val testMes = modelo.mes.test()

            @Test
            fun emite_valor_correcto_al_cambiar_todos_los_campos_a_valor_valido_con_validacion_exitosa()
            {
                fallar = false
                modelo.cambiarDia("1")
                modelo.cambiarMes("2")
                modelo.cambiarAño("3")
                testMes.assertValue(Notification.createOnNext(2))
                testMes.assertValueCount(1)
            }

            @Test
            fun emite_valor_correcto_al_cambiar_todos_los_campos_a_valor_valido_con_validacion_fallida()
            {
                fallar = true
                modelo.cambiarDia("1")
                modelo.cambiarMes("2")
                modelo.cambiarAño("3")
                testMes.assertValue(Notification.createOnNext(2))
                testMes.assertValueCount(1)
            }
        }

        @Nested
        inner class CambiarAño
        {
            private val testAño = modelo.año.test()

            @Test
            fun emite_valor_correcto_al_cambiar_todos_los_campos_a_valor_valido_con_validacion_exitosa()
            {
                fallar = false
                modelo.cambiarDia("1")
                modelo.cambiarMes("2")
                modelo.cambiarAño("3")
                testAño.assertValue(Notification.createOnNext(3))
                testAño.assertValueCount(1)
            }

            @Test
            fun emite_valor_correcto_al_cambiar_todos_los_campos_a_valor_valido_con_validacion_fallida()
            {
                fallar = true
                modelo.cambiarDia("1")
                modelo.cambiarMes("2")
                modelo.cambiarAño("3")
                testAño.assertValue(Notification.createOnNext(3))
                testAño.assertValueCount(1)
            }
        }

        @Nested
        inner class Fecha
        {
            private val testFecha = modelo.fecha.test()

            @Test
            fun emite_valor_correcto_al_cambiar_todos_los_campos_a_valores_validos_con_validacion_exitosa()
            {
                fallar = false
                modelo.cambiarDia("1")
                modelo.cambiarMes("2")
                modelo.cambiarAño("3")
                testFecha.assertValue(Notification.createOnNext(LocalDate.of(3, 2, 1)))
                testFecha.assertValueCount(1)
            }

            @Test
            fun emite_evento_error_al_cambiar_todos_los_campos_a_valores_validos_con_validacion_fallido()
            {
                fallar = true
                modelo.cambiarDia("1")
                modelo.cambiarMes("2")
                modelo.cambiarAño("3")
                testFecha.assertValue(Notification.createOnError(excepcionEsperada))
                testFecha.assertValueCount(1)
            }

            @Test
            fun emite_valor_correcto_al_cambiar_todos_los_campos_a_valores_validos_con_validacion_exitosa_despues_de_validacion_fallida()
            {
                fallar = true
                modelo.cambiarDia("1")
                modelo.cambiarMes("2")
                modelo.cambiarAño("3")
                fallar = false
                modelo.cambiarDia("1")
                testFecha.assertValueAt(0, Notification.createOnError(excepcionEsperada))
                testFecha.assertValueAt(1, Notification.createOnNext(LocalDate.of(3, 2, 1)))
                testFecha.assertValueCount(2)
            }

            @Test
            fun emite_evento_error_al_cambiar_todos_los_campos_a_valores_validos_con_validacion_fallida_despues_de_valdiacion_exitosa()
            {
                fallar = false
                modelo.cambiarDia("1")
                modelo.cambiarMes("2")
                modelo.cambiarAño("3")
                fallar = true
                modelo.cambiarDia("1")
                testFecha.assertValueAt(0, Notification.createOnNext(LocalDate.of(3, 2, 1)))
                testFecha.assertValueAt(1, Notification.createOnError(excepcionEsperada))
                testFecha.assertValueCount(2)
            }
        }

        @Nested
        inner class EsFechaValida
        {
            private val testEsFechaValida = modelo.esFechaValida.test()

            @Test
            fun emite_true_al_cambiar_todos_los_campos_a_valores_validos_con_validacion_exitosa()
            {
                fallar = false
                modelo.cambiarDia("1")
                modelo.cambiarMes("2")
                modelo.cambiarAño("3")
                testEsFechaValida.assertValue(true)
                testEsFechaValida.assertValueCount(1)
            }

            @Test
            fun emite_false_al_cambiar_todos_los_campos_a_valores_validos_con_validacion_fallido()
            {
                fallar = true
                modelo.cambiarDia("1")
                modelo.cambiarMes("2")
                modelo.cambiarAño("3")
                testEsFechaValida.assertValue(false)
                testEsFechaValida.assertValueCount(1)
            }

            @Test
            fun emite_true_al_cambiar_todos_los_campos_a_valores_validos_con_validacion_exitosa_despues_de_validacion_fallida()
            {
                fallar = true
                modelo.cambiarDia("1")
                modelo.cambiarMes("2")
                modelo.cambiarAño("3")
                fallar = false
                modelo.cambiarDia("1")
                testEsFechaValida.assertValueAt(0, false)
                testEsFechaValida.assertValueAt(1, true)
                testEsFechaValida.assertValueCount(2)
            }

            @Test
            fun emite_false_al_cambiar_todos_los_campos_a_valores_validos_con_validacion_fallida_despues_de_valdiacion_exitosa()
            {
                fallar = false
                modelo.cambiarDia("1")
                modelo.cambiarMes("2")
                modelo.cambiarAño("3")
                fallar = true
                modelo.cambiarDia("1")
                testEsFechaValida.assertValueAt(0, true)
                testEsFechaValida.assertValueAt(1, false)
                testEsFechaValida.assertValueCount(2)
            }
        }

        @Nested
        inner class AsignarFecha
        {
            private val fechaPruebas = LocalDate.now()

            @Nested
            inner class EnDia
            {
                private val testDia = modelo.dia.test()

                @Test
                fun emite_valor_correcto_con_validacion_exitosa()
                {
                    fallar = false
                    modelo.asignarFecha(fechaPruebas)
                    testDia.assertValue(Notification.createOnNext(fechaPruebas.dayOfMonth))
                    testDia.assertValueCount(1)
                }

                @Test
                fun emite_valor_correcto_con_validacion_fallida()
                {
                    fallar = true
                    modelo.asignarFecha(fechaPruebas)
                    testDia.assertValue(Notification.createOnNext(fechaPruebas.dayOfMonth))
                    testDia.assertValueCount(1)
                }
            }

            @Nested
            inner class EnMes
            {
                private val testMes = modelo.mes.test()

                @Test
                fun emite_valor_correcto_con_validacion_exitosa()
                {
                    fallar = false
                    modelo.asignarFecha(fechaPruebas)
                    testMes.assertValue(Notification.createOnNext(fechaPruebas.monthValue))
                    testMes.assertValueCount(1)
                }

                @Test
                fun emite_valor_correcto_con_validacion_fallida()
                {
                    fallar = true
                    modelo.asignarFecha(fechaPruebas)
                    testMes.assertValue(Notification.createOnNext(fechaPruebas.monthValue))
                    testMes.assertValueCount(1)
                }
            }

            @Nested
            inner class EnAño
            {
                private val testAño = modelo.año.test()

                @Test
                fun emite_valor_correcto_con_validacion_exitosa()
                {
                    fallar = false
                    modelo.asignarFecha(fechaPruebas)
                    testAño.assertValue(Notification.createOnNext(fechaPruebas.year))
                    testAño.assertValueCount(1)
                }

                @Test
                fun emite_valor_correcto_con_validacion_fallida()
                {
                    fallar = true
                    modelo.asignarFecha(fechaPruebas)
                    testAño.assertValue(Notification.createOnNext(fechaPruebas.year))
                    testAño.assertValueCount(1)
                }
            }

            @Nested
            inner class EnFecha
            {
                private val testFecha = modelo.fecha.test()

                @Test
                fun emite_valor_correcto_con_validacion_exitosa()
                {
                    fallar = false
                    modelo.asignarFecha(fechaPruebas)
                    testFecha.assertValueAt(0, Notification.createOnNext(fechaPruebas))
                    testFecha.assertValueCount(1)
                }

                @Test
                fun emite_evento_error_con_validacion_fallido()
                {
                    fallar = true
                    modelo.asignarFecha(fechaPruebas)
                    testFecha.assertValue(Notification.createOnError(excepcionEsperada))
                    testFecha.assertValueCount(1)
                }

                @Test
                fun emite_valor_correcto_con_validacion_exitosa_despues_de_validacion_fallida()
                {
                    fallar = true
                    modelo.asignarFecha(fechaPruebas)
                    fallar = false
                    modelo.asignarFecha(fechaPruebas)
                    testFecha.assertValueAt(0, Notification.createOnError(excepcionEsperada))
                    testFecha.assertValueAt(1, Notification.createOnNext(fechaPruebas))
                    testFecha.assertValueCount(2)
                }

                @Test
                fun emite_evento_error_con_validacion_fallida_despues_de_valdiacion_exitosa()
                {
                    fallar = false
                    modelo.asignarFecha(fechaPruebas)
                    fallar = true
                    modelo.asignarFecha(fechaPruebas)
                    testFecha.assertValueAt(0, Notification.createOnNext(fechaPruebas))
                    testFecha.assertValueAt(1, Notification.createOnError(excepcionEsperada))
                    testFecha.assertValueCount(2)
                }
            }

            @Nested
            inner class EnEsFechaValida
            {
                private val testEsFechaValida = modelo.esFechaValida.test()

                @Test
                fun emite_true_con_validacion_exitosa()
                {
                    fallar = false
                    modelo.asignarFecha(fechaPruebas)
                    testEsFechaValida.assertValue(true)
                    testEsFechaValida.assertValueCount(1)
                }

                @Test
                fun emite_false_con_validacion_fallido()
                {
                    fallar = true
                    modelo.asignarFecha(fechaPruebas)
                    testEsFechaValida.assertValue(false)
                    testEsFechaValida.assertValueCount(1)
                }

                @Test
                fun emite_true_con_validacion_exitosa_despues_de_validacion_fallida()
                {
                    fallar = true
                    modelo.asignarFecha(fechaPruebas)
                    fallar = false
                    modelo.asignarFecha(fechaPruebas)
                    testEsFechaValida.assertValueAt(0, false)
                    testEsFechaValida.assertValueAt(1, true)
                    testEsFechaValida.assertValueCount(2)
                }

                @Test
                fun emite_evento_error_con_validacion_fallida_despues_de_valdiacion_exitosa()
                {
                    fallar = false
                    modelo.asignarFecha(fechaPruebas)
                    fallar = true
                    modelo.asignarFecha(fechaPruebas)
                    testEsFechaValida.assertValueAt(0, true)
                    testEsFechaValida.assertValueAt(1, false)
                    testEsFechaValida.assertValueCount(2)
                }
            }
        }

        @Nested
        inner class AFecha
        {
            private val fechaPruebas = LocalDate.now()

            @Test
            fun retorna_fecha_correcta_con_fecha_valida_y_validacion_exitosa()
            {
                fallar = false
                modelo.asignarFecha(fechaPruebas)
                Assertions.assertEquals(fechaPruebas, modelo.aFecha())
            }

            @Test
            fun lanza_IllegalStateException_con_fecha_valida_y_validacion_fallida()
            {
                fallar = true
                modelo.asignarFecha(fechaPruebas)
                Assertions.assertThrows(IllegalStateException::class.java, { modelo.aFecha() })
            }
        }
    }

    @Nested
    inner class ConCampoSinValidacion
    {
        private val modelo: FechaUI = FechaUIConSujetos({ CampoSinValidacion(it) })

        @Nested
        inner class CambiarDia
        {
            private val testDia = modelo.dia.test()

            @Test
            fun no_emite_valor_al_inicializar()
            {
                testDia.assertValueCount(0)
            }

            @Test
            fun con_valor_valido_emite_valor_cambiado_una_vez_por_cada_llamado()
            {
                val numeroEventos = 31
                (1..numeroEventos).forEach {
                    modelo.cambiarDia(it.toString())
                    testDia.assertValueAt(it - 1, Notification.createOnNext(it))
                }
                testDia.assertValueCount(numeroEventos)
            }

            @Test
            fun con_espacios_emite_error_correcto()
            {
                modelo.cambiarDia("        1       ")
                testDia.assertValue({ it.isOnError })
                testDia.assertValue({ it.error!!.message == "Inválido" })
                testDia.assertValueCount(1)
            }

            @Test
            fun con_valor_vacio_emite_error_correcto()
            {
                modelo.cambiarDia("")
                testDia.assertValue({ it.isOnError })
                testDia.assertValue({ it.error!!.message == "Inválido" })
                testDia.assertValueCount(1)
            }

            @Test
            fun con_valor_con_espacios_y_tabs_emite_error_correcto()
            {
                modelo.cambiarDia("             ")
                testDia.assertValue({ it.isOnError })
                testDia.assertValue({ it.error!!.message == "Inválido" })
                testDia.assertValueCount(1)
            }

            @Test
            fun con_valor_no_numerico_emite_error_correcto()
            {
                modelo.cambiarDia("asd")
                testDia.assertValue({ it.isOnError })
                testDia.assertValue({ it.error!!.message == "Inválido" })
                testDia.assertValueCount(1)
            }

            @Test
            fun con_valor_menor_a_1_emite_error_correcto()
            {
                modelo.cambiarDia("0")
                testDia.assertValue({ it.isOnError })
                testDia.assertValue({ it.error!!.message == "Inválido" })
                testDia.assertValueCount(1)
            }

            @Test
            fun con_valor_mayor_a_31_emite_error_correcto()
            {
                modelo.cambiarDia("32")
                testDia.assertValue({ it.isOnError })
                testDia.assertValue({ it.error!!.message == "Inválido" })
                testDia.assertValueCount(1)
            }

            @Test
            fun a_error_y_luego_a_valor_valido_emite_correcto()
            {
                modelo.cambiarDia("0")
                modelo.cambiarDia("1")
                testDia.assertValueAt(0, { it.isOnError })
                testDia.assertValueAt(0, { it.error!!.message == "Inválido" })
                testDia.assertValueAt(1, Notification.createOnNext(1))
                testDia.assertValueCount(2)
            }

            @Test
            fun a_valor_valido_y_luego_a_error_emite_ambos_valores()
            {
                modelo.cambiarDia("1")
                modelo.cambiarDia("0")
                testDia.assertValueAt(0, Notification.createOnNext(1))
                testDia.assertValueAt(1, { it.isOnError })
                testDia.assertValueAt(1, { it.error!!.message == "Inválido" })
                testDia.assertValueCount(2)
            }

            @Test
            fun no_emite_mas_eventos_despues_de_llamar_finalizar_proceso()
            {
                modelo.finalizarProceso()
                modelo.cambiarDia("1")
                modelo.cambiarDia("0")
                testDia.assertValueCount(0)
            }
        }

        @Nested
        inner class CambiarMes
        {
            private val testMes = modelo.mes.test()

            @Test
            fun no_emite_valor_al_inicializar()
            {
                testMes.assertValueCount(0)
            }

            @Test
            fun con_valor_valido_emite_valor_cambiado_una_vez_por_cada_llamado()
            {
                val numeroEventos = 12
                (1..12).forEach {
                    modelo.cambiarMes(it.toString())
                    testMes.assertValueAt(it - 1, Notification.createOnNext(it))
                }
                testMes.assertValueCount(numeroEventos)
            }

            @Test
            fun con_espacios_emite_error_correcto()
            {
                modelo.cambiarMes("        1       ")
                testMes.assertValue({ it.isOnError })
                testMes.assertValue({ it.error!!.message == "Inválido" })
                testMes.assertValueCount(1)
            }

            @Test
            fun con_valor_vacio_emite_error_correcto()
            {
                modelo.cambiarMes("")
                testMes.assertValue({ it.isOnError })
                testMes.assertValue({ it.error!!.message == "Inválido" })
                testMes.assertValueCount(1)
            }

            @Test
            fun con_valor_con_espacios_y_tabs_emite_error_correcto()
            {
                modelo.cambiarMes("             ")
                testMes.assertValue({ it.isOnError })
                testMes.assertValue({ it.error!!.message == "Inválido" })
                testMes.assertValueCount(1)
            }

            @Test
            fun con_valor_no_numerico_emite_error_correcto()
            {
                modelo.cambiarMes("asd")
                testMes.assertValue({ it.isOnError })
                testMes.assertValue({ it.error!!.message == "Inválido" })
                testMes.assertValueCount(1)
            }

            @Test
            fun con_valor_menor_a_1_emite_error_correcto()
            {
                modelo.cambiarMes("0")
                testMes.assertValue({ it.isOnError })
                testMes.assertValue({ it.error!!.message == "Inválido" })
                testMes.assertValueCount(1)
            }

            @Test
            fun con_valor_mayor_a_12_emite_error_correcto()
            {
                modelo.cambiarMes("13")
                testMes.assertValue({ it.isOnError })
                testMes.assertValue({ it.error!!.message == "Inválido" })
                testMes.assertValueCount(1)
            }

            @Test
            fun a_error_y_luego_a_valor_valido_emite_correcto()
            {
                modelo.cambiarMes("0")
                modelo.cambiarMes("1")
                testMes.assertValueAt(0, { it.isOnError })
                testMes.assertValueAt(0, { it.error!!.message == "Inválido" })
                testMes.assertValueAt(1, Notification.createOnNext(1))
                testMes.assertValueCount(2)
            }

            @Test
            fun a_valor_valido_y_luego_a_error_emite_ambos_valores()
            {
                modelo.cambiarMes("1")
                modelo.cambiarMes("0")
                testMes.assertValueAt(0, Notification.createOnNext(1))
                testMes.assertValueAt(1, { it.isOnError })
                testMes.assertValueAt(1, { it.error!!.message == "Inválido" })
                testMes.assertValueCount(2)
            }

            @Test
            fun no_emite_mas_eventos_despues_de_llamar_finalizar_proceso()
            {
                modelo.finalizarProceso()
                modelo.cambiarMes("1")
                modelo.cambiarMes("0")
                testMes.assertValueCount(0)
            }
        }

        @Nested
        inner class CambiarAño
        {
            private val testAño = modelo.año.test()

            @Test
            fun no_emite_valor_al_inicializar()
            {
                testAño.assertValueCount(0)
            }

            @Test
            fun con_valor_valido_futuro_emite_valor_cambiado_una_vez_por_cada_llamado()
            {
                val añoActual = LocalDate.now().year
                val numeroEventos = 10
                (0 until numeroEventos).forEach {
                    val año = añoActual + it
                    modelo.cambiarAño(año.toString())
                    testAño.assertValueAt(it, Notification.createOnNext(año))
                }
                testAño.assertValueCount(numeroEventos)
            }

            @Test
            fun con_valor_valido_pasado_emite_valor_cambiado_una_vez_por_cada_llamado()
            {
                val añoActual = LocalDate.now().year
                val numeroEventos = 10
                (0 until numeroEventos).forEach {
                    val año = añoActual - it
                    modelo.cambiarAño(año.toString())
                    testAño.assertValueAt(it, Notification.createOnNext(año))
                }
                testAño.assertValueCount(numeroEventos)
            }

            @Test
            fun con_espacios_emite_error_correcto()
            {
                modelo.cambiarAño("        1       ")
                testAño.assertValue({ it.isOnError })
                testAño.assertValue({ it.error!!.message == "Inválido" })
                testAño.assertValueCount(1)
            }

            @Test
            fun con_valor_vacio_emite_error_correcto()
            {
                modelo.cambiarAño("")
                testAño.assertValue({ it.isOnError })
                testAño.assertValue({ it.error!!.message == "Inválido" })
                testAño.assertValueCount(1)
            }

            @Test
            fun con_valor_con_espacios_y_tabs_emite_error_correcto()
            {
                modelo.cambiarAño("             ")
                testAño.assertValue({ it.isOnError })
                testAño.assertValue({ it.error!!.message == "Inválido" })
                testAño.assertValueCount(1)
            }

            @Test
            fun con_valor_no_numerico_emite_error_correcto()
            {
                modelo.cambiarAño("asd")
                testAño.assertValue({ it.isOnError })
                testAño.assertValue({ it.error!!.message == "Inválido" })
                testAño.assertValueCount(1)
            }

            @Test
            fun a_error_y_luego_a_valor_valido_emite_correcto()
            {
                modelo.cambiarAño("a")
                modelo.cambiarAño("1")
                testAño.assertValueAt(0, { it.isOnError })
                testAño.assertValueAt(0, { it.error!!.message == "Inválido" })
                testAño.assertValueAt(1, Notification.createOnNext(1))
                testAño.assertValueCount(2)
            }

            @Test
            fun a_valor_valido_y_luego_a_error_emite_ambos_valores()
            {
                modelo.cambiarAño("1")
                modelo.cambiarAño("a")
                testAño.assertValueAt(0, Notification.createOnNext(1))
                testAño.assertValueAt(1, { it.isOnError })
                testAño.assertValueAt(1, { it.error!!.message == "Inválido" })
                testAño.assertValueCount(2)
            }

            @Test
            fun no_emite_mas_eventos_despues_de_llamar_finalizar_proceso()
            {
                modelo.finalizarProceso()
                modelo.cambiarAño("a")
                modelo.cambiarAño("a")
                testAño.assertValueCount(0)
            }
        }

        @Nested
        inner class Fecha
        {
            private val testFecha = modelo.fecha.test()

            @Test
            fun no_emite_evento_al_cambiar_todos_los_valores_menos_dia()
            {
                modelo.cambiarMes("2")
                modelo.cambiarAño("3")
                testFecha.assertValueCount(0)
            }

            @Test
            fun no_emite_evento_al_cambiar_todos_los_valores_menos_mes()
            {
                modelo.cambiarDia("1")
                modelo.cambiarAño("3")
                testFecha.assertValueCount(0)
            }

            @Test
            fun no_emite_evento_al_cambiar_todos_los_valores_menos_año()
            {
                modelo.cambiarDia("1")
                modelo.cambiarMes("2")
                testFecha.assertValueCount(0)
            }

            @Test
            fun emite_valor_correcto_al_cambiar_dia_mes_y_año_a_valores_validos_en_ese_orden()
            {
                modelo.cambiarDia("1")
                modelo.cambiarMes("2")
                modelo.cambiarAño("3")
                testFecha.assertValue(Notification.createOnNext(LocalDate.of(3, 2, 1)))
                testFecha.assertValueCount(1)
            }

            @Test
            fun emite_valor_correcto_al_cambiar_mes_año_y_dia_a_valores_validos_en_ese_orden()
            {
                modelo.cambiarMes("2")
                modelo.cambiarAño("3")
                modelo.cambiarDia("1")
                testFecha.assertValue(Notification.createOnNext(LocalDate.of(3, 2, 1)))
                testFecha.assertValueCount(1)
            }

            @Test
            fun emite_valor_correcto_al_cambiar_dia_año_y_mes_a_valores_validos_en_ese_orden()
            {
                modelo.cambiarDia("1")
                modelo.cambiarAño("3")
                modelo.cambiarMes("2")
                testFecha.assertValue(Notification.createOnNext(LocalDate.of(3, 2, 1)))
                testFecha.assertValueCount(1)
            }

            @Test
            fun emite_evento_error_al_cambiar_dia_invalido_sin_cambiar_otros_valores()
            {
                modelo.cambiarDia("0")
                testFecha.assertValue({ it.isOnError })
                testFecha.assertValue({ it.error!!.message == "Fecha inválida" })
                testFecha.assertValueCount(1)
            }

            @Test
            fun emite_evento_error_al_cambiar_dia_invalido_cambiando_otros_valores_a_valores_validos()
            {
                modelo.cambiarMes("2")
                modelo.cambiarAño("3")
                modelo.cambiarDia("0")
                testFecha.assertValue({ it.isOnError })
                testFecha.assertValue({ it.error!!.message == "Fecha inválida" })
                testFecha.assertValueCount(1)
            }

            @Test
            fun emite_valor_correcto_al_cambiar_dia_a_valor_valido_despues_de_ser_invalido_con_otros_valores_en_valores_validos()
            {
                modelo.cambiarMes("2")
                modelo.cambiarAño("3")
                modelo.cambiarDia("0")
                modelo.cambiarDia("1")
                testFecha.assertValueAt(1, Notification.createOnNext(LocalDate.of(3, 2, 1)))
                testFecha.assertValueCount(2)
            }

            @Test
            fun emite_evento_error_al_cambiar_mes_invalido_sin_cambiar_otros_valores()
            {
                modelo.cambiarMes("0")
                testFecha.assertValue({ it.isOnError })
                testFecha.assertValue({ it.error!!.message == "Fecha inválida" })
                testFecha.assertValueCount(1)
            }

            @Test
            fun emite_evento_error_al_cambiar_mes_invalido_cambiando_otros_valores_a_valores_validos()
            {
                modelo.cambiarDia("1")
                modelo.cambiarAño("3")
                modelo.cambiarMes("0")
                testFecha.assertValue({ it.isOnError })
                testFecha.assertValue({ it.error!!.message == "Fecha inválida" })
                testFecha.assertValueCount(1)
            }

            @Test
            fun emite_valor_correcto_al_cambiar_mes_a_valor_valido_despues_de_ser_invalido_con_otros_valores_en_valores_validos()
            {
                modelo.cambiarDia("1")
                modelo.cambiarAño("3")
                modelo.cambiarMes("0")
                modelo.cambiarMes("2")
                testFecha.assertValueAt(1, Notification.createOnNext(LocalDate.of(3, 2, 1)))
                testFecha.assertValueCount(2)
            }

            @Test
            fun emite_evento_error_al_cambiar_año_invalido_sin_cambiar_otros_valores()
            {
                modelo.cambiarAño("")
                testFecha.assertValue({ it.isOnError })
                testFecha.assertValue({ it.error!!.message == "Fecha inválida" })
                testFecha.assertValueCount(1)
            }

            @Test
            fun emite_evento_error_al_cambiar_año_invalido_cambiando_otros_valores_a_valores_validos()
            {
                modelo.cambiarDia("1")
                modelo.cambiarMes("2")
                modelo.cambiarAño("")
                testFecha.assertValue({ it.isOnError })
                testFecha.assertValue({ it.error!!.message == "Fecha inválida" })
                testFecha.assertValueCount(1)
            }

            @Test
            fun emite_valor_correcto_al_cambiar_año_a_valor_valido_despues_de_ser_invalido_con_otros_valores_en_valores_validos()
            {
                modelo.cambiarDia("1")
                modelo.cambiarMes("2")
                modelo.cambiarAño("")
                modelo.cambiarAño("3")
                testFecha.assertValueAt(1, Notification.createOnNext(LocalDate.of(3, 2, 1)))
                testFecha.assertValueCount(2)
            }

            @Test
            fun emite_evento_error_al_cambiar_todos_los_campos_a_valores_validos_pero_que_representan_una_fecha_invalida()
            {
                modelo.cambiarDia("31")
                modelo.cambiarMes("2")
                modelo.cambiarAño("3")
                testFecha.assertValue({ it.isOnError })
                testFecha.assertValue({ it.error!!.message == "Fecha inválida" })
                testFecha.assertValueCount(1)
            }

            @Test
            fun no_emite_mas_eventos_despues_de_llamar_finalizar_proceso()
            {
                modelo.finalizarProceso()
                modelo.cambiarDia("1")
                modelo.cambiarMes("2")
                modelo.cambiarAño("3")
                modelo.cambiarDia("")
                modelo.cambiarMes("")
                modelo.cambiarAño("")
                modelo.cambiarDia("1")
                modelo.cambiarMes("2")
                modelo.cambiarAño("3")
            }
        }

        @Nested
        inner class EsFechaValida
        {
            private val testEsFechaValida = modelo.esFechaValida.test()

            @Test
            fun no_emite_evento_al_cambiar_todos_los_valores_menos_dia()
            {
                modelo.cambiarMes("2")
                modelo.cambiarAño("3")
                testEsFechaValida.assertValueCount(0)
            }

            @Test
            fun no_emite_evento_al_cambiar_todos_los_valores_menos_mes()
            {
                modelo.cambiarDia("1")
                modelo.cambiarAño("3")
                testEsFechaValida.assertValueCount(0)
            }

            @Test
            fun no_emite_evento_al_cambiar_todos_los_valores_menos_año()
            {
                modelo.cambiarDia("1")
                modelo.cambiarMes("2")
                testEsFechaValida.assertValueCount(0)
            }

            @Test
            fun emite_evento_true_al_cambiar_todos_los_campos_a_valores_validos()
            {
                modelo.cambiarDia("1")
                modelo.cambiarMes("2")
                modelo.cambiarAño("3")
                testEsFechaValida.assertValue(true)
                testEsFechaValida.assertValueCount(1)
            }

            @Test
            fun emite_evento_false_al_cambiar_todos_los_campos_a_valores_validos_pero_que_representan_una_fecha_invalida()
            {
                modelo.cambiarDia("31")
                modelo.cambiarMes("2")
                modelo.cambiarAño("3")
                testEsFechaValida.assertValue(false)
                testEsFechaValida.assertValueCount(1)
            }

            @Test
            fun emite_evento_false_al_cambiar_dia_a_valor_invalido_y_el_resto_de_valores_a_valor_validos()
            {
                modelo.cambiarDia("0")
                modelo.cambiarMes("2")
                modelo.cambiarAño("3")
                testEsFechaValida.assertValue(false)
                testEsFechaValida.assertValueCount(1)
            }

            @Test
            fun emite_evento_false_al_cambiar_mes_a_valor_invalido_y_el_resto_de_valores_a_valor_validos()
            {
                modelo.cambiarDia("1")
                modelo.cambiarMes("0")
                modelo.cambiarAño("3")
                testEsFechaValida.assertValue(false)
                testEsFechaValida.assertValueCount(1)
            }

            @Test
            fun emite_evento_false_al_cambiar_año_a_valor_invalido_y_el_resto_de_valores_a_valor_validos()
            {
                modelo.cambiarDia("1")
                modelo.cambiarMes("2")
                modelo.cambiarAño("")
                testEsFechaValida.assertValue(false)
                testEsFechaValida.assertValueCount(1)
            }

            @Test
            fun no_emite_mas_eventos_despues_de_llamar_finalizar_proceso()
            {
                modelo.finalizarProceso()
                modelo.cambiarDia("1")
                modelo.cambiarMes("2")
                modelo.cambiarAño("3")
                modelo.cambiarDia("")
                modelo.cambiarMes("")
                modelo.cambiarAño("")
                modelo.cambiarDia("1")
                modelo.cambiarMes("2")
                modelo.cambiarAño("3")
            }
        }

        @Nested
        inner class AsignarFecha
        {
            private val fechaPruebas = LocalDate.now()

            @Nested
            inner class EnDia
            {
                private val testDia = modelo.dia.test()

                @Test
                fun emite_valor_correcto()
                {
                    modelo.asignarFecha(fechaPruebas)
                    testDia.assertValue(Notification.createOnNext(fechaPruebas.dayOfMonth))
                    testDia.assertValueCount(1)
                }
            }

            @Nested
            inner class EnMes
            {
                private val testMes = modelo.mes.test()

                @Test
                fun emite_valor_correcto()
                {
                    modelo.asignarFecha(fechaPruebas)
                    testMes.assertValue(Notification.createOnNext(fechaPruebas.monthValue))
                    testMes.assertValueCount(1)
                }
            }

            @Nested
            inner class EnAño
            {
                private val testAño = modelo.año.test()

                @Test
                fun emite_valor_correcto()
                {
                    modelo.asignarFecha(fechaPruebas)
                    testAño.assertValue(Notification.createOnNext(fechaPruebas.year))
                    testAño.assertValueCount(1)
                }
            }

            @Nested
            inner class EnFecha
            {
                private val testFecha = modelo.fecha.test()

                @Test
                fun emite_valor_correcto()
                {
                    modelo.asignarFecha(fechaPruebas)
                    testFecha.assertValue(Notification.createOnNext(fechaPruebas))
                    testFecha.assertValueCount(1)
                }
            }

            @Nested
            inner class EnEsFechaValida
            {
                private val testEsFechaValida = modelo.esFechaValida.test()

                @Test
                fun emite_valor_correcto()
                {
                    modelo.asignarFecha(fechaPruebas)
                    testEsFechaValida.assertValue(true)
                    testEsFechaValida.assertValueCount(1)
                }
            }
        }

        @Nested
        inner class AFecha
        {
            private val fechaPruebas = LocalDate.now()

            @Test
            fun retorna_fecha_correcta_al_asignar_campos_validos()
            {
                modelo.cambiarDia(fechaPruebas.dayOfMonth.toString())
                modelo.cambiarMes(fechaPruebas.monthValue.toString())
                modelo.cambiarAño(fechaPruebas.year.toString())
                Assertions.assertEquals(fechaPruebas, modelo.aFecha())
            }

            @Test
            fun retorna_fecha_correcta_al_asignar_fecha_valida()
            {
                modelo.asignarFecha(fechaPruebas)
                Assertions.assertEquals(fechaPruebas, modelo.aFecha())
            }

            @Test
            fun lanza_IllegalStateException_si_no_se_han_asignado_ningun_campo()
            {
                Assertions.assertThrows(IllegalStateException::class.java, { modelo.aFecha() })
            }

            @Test
            fun lanza_IllegalStateException_con_todos_los_campos_validos_pero_sin_asignar_dia()
            {
                modelo.cambiarMes(fechaPruebas.monthValue.toString())
                modelo.cambiarAño(fechaPruebas.year.toString())
                Assertions.assertThrows(IllegalStateException::class.java, { modelo.aFecha() })
            }

            @Test
            fun lanza_IllegalStateException_con_todos_los_campos_validos_pero_sin_asignar_mes()
            {
                modelo.cambiarDia(fechaPruebas.dayOfMonth.toString())
                modelo.cambiarAño(fechaPruebas.year.toString())
                Assertions.assertThrows(IllegalStateException::class.java, { modelo.aFecha() })
            }

            @Test
            fun lanza_IllegalStateException_con_todos_los_campos_validos_pero_sin_asignar_numero_año()
            {
                modelo.cambiarDia(fechaPruebas.dayOfMonth.toString())
                modelo.cambiarMes(fechaPruebas.monthValue.toString())
                Assertions.assertThrows(IllegalStateException::class.java, { modelo.aFecha() })
            }

            @Test
            fun lanza_IllegalStateException_con_todos_los_campos_validos_pero_con_dia_invalido()
            {
                modelo.cambiarDia("0")
                modelo.cambiarMes(fechaPruebas.monthValue.toString())
                modelo.cambiarAño(fechaPruebas.year.toString())
                Assertions.assertThrows(IllegalStateException::class.java, { modelo.aFecha() })
            }

            @Test
            fun lanza_IllegalStateException_con_todos_los_campos_validos_pero_con_mes_invalido()
            {
                modelo.cambiarDia(fechaPruebas.dayOfMonth.toString())
                modelo.cambiarMes("0")
                modelo.cambiarAño(fechaPruebas.year.toString())
                Assertions.assertThrows(IllegalStateException::class.java, { modelo.aFecha() })
            }

            @Test
            fun lanza_IllegalStateException_con_todos_los_campos_validos_pero_con_año_invalido()
            {
                modelo.cambiarDia(fechaPruebas.dayOfMonth.toString())
                modelo.cambiarMes(fechaPruebas.monthValue.toString())
                modelo.cambiarAño("")
                Assertions.assertThrows(IllegalStateException::class.java, { modelo.aFecha() })
            }

            @Test
            fun lanza_IllegalStateException_con_todos_los_campos_validos_pero_representando_una_fecha_invalida_invalido()
            {
                modelo.cambiarDia("31")
                modelo.cambiarMes("2")
                modelo.cambiarAño("3")
                Assertions.assertThrows(IllegalStateException::class.java, { modelo.aFecha() })
            }
        }
    }
}