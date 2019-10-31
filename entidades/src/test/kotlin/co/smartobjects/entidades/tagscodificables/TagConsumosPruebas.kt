package co.smartobjects.entidades.tagscodificables

import co.smartobjects.entidades.excepciones.EntidadConCampoFueraDeRango
import co.smartobjects.entidades.excepciones.EntidadConCampoVacio
import co.smartobjects.entidades.operativas.compras.CreditoFondo
import co.smartobjects.mockConDefaultAnswer
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.doReturn
import org.threeten.bp.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue


internal class TagConsumosPruebas
{
    @Nested
    inner class InstanciacionDesdeCreditosFondo
    {
        @Test
        fun no_se_puede_instanciar_con_creditos_fondo_vacios()
        {
            val ex = assertThrows<EntidadConCampoVacio> { TagConsumos(1L, emptyList()) }

            assertEquals(ex.nombreEntidad, TagConsumos.NOMBRE_ENTIDAD)
            assertEquals(ex.nombreDelCampo, "créditos a codificar")
            assertEquals(ex.nombreDelCampo, "créditos a codificar")
        }

        @Test
        fun no_se_puede_instanciar_con_mas_de_255_creditos_creditos_fondo_vacios()
        {
            val creditosACodificar = List(256) { mockConDefaultAnswer(CreditoFondo::class.java) }

            val excepcion = assertThrows<EntidadConCampoFueraDeRango> {
                TagConsumos(1L, creditosACodificar)
            }

            assertEquals(TagConsumos.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
            assertEquals("número de créditos a codificar", excepcion.nombreDelCampo)
            assertEquals(creditosACodificar.size.toString(), excepcion.valorUsado)
            assertEquals("255", excepcion.valorDelLimite)
            assertEquals(EntidadConCampoFueraDeRango.Limite.SUPERIOR, excepcion.limiteSobrepasado)
        }

        @Nested
        inner class FechaValidoDesdeMinima
        {
            @Test
            fun se_calcula_correctamente_si_no_hay_fecha_valido_desde_nulas()
            {
                val fechaMinimaEsperada = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
                val creditosFondo =
                        listOf(
                                mockConDefaultAnswer(CreditoFondo::class.java).also {
                                    doReturn(Decimal.CERO).`when`(it).cantidad
                                    doReturn(1L).`when`(it).idFondoComprado
                                    doReturn(fechaMinimaEsperada).`when`(it).validoDesde
                                    doReturn(null).`when`(it).validoHasta
                                },
                                mockConDefaultAnswer(CreditoFondo::class.java).also {
                                    doReturn(Decimal.CERO).`when`(it).cantidad
                                    doReturn(1L).`when`(it).idFondoComprado
                                    doReturn(fechaMinimaEsperada.plusNanos(1)).`when`(it).validoDesde
                                    doReturn(null).`when`(it).validoHasta
                                }
                              )

                val entidad = TagConsumos(1L, creditosFondo)

                assertEquals(fechaMinimaEsperada, entidad.fechaValidoDesdeMinima)
            }

            @Test
            fun se_calcula_correctamente_si_hay_fecha_valido_desde_nulas()
            {
                val creditosFondo =
                        listOf(
                                mockConDefaultAnswer(CreditoFondo::class.java).also {
                                    doReturn(Decimal.CERO).`when`(it).cantidad
                                    doReturn(1L).`when`(it).idFondoComprado
                                    doReturn(null).`when`(it).validoDesde
                                    doReturn(null).`when`(it).validoHasta
                                },
                                mockConDefaultAnswer(CreditoFondo::class.java).also {
                                    doReturn(Decimal.CERO).`when`(it).cantidad
                                    doReturn(1L).`when`(it).idFondoComprado
                                    doReturn(ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)).`when`(it).validoDesde
                                    doReturn(null).`when`(it).validoHasta
                                }
                              )

                val entidad = TagConsumos(1L, creditosFondo)

                assertNull(entidad.fechaValidoDesdeMinima)
            }
        }

        @Nested
        inner class FechaValidoHastaMaxima
        {
            @Test
            fun se_calcula_correctamente_si_no_hay_fecha_valido_hasta_nulas()
            {
                val fechaMaximaEsperada = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
                val creditosFondo =
                        listOf(
                                mockConDefaultAnswer(CreditoFondo::class.java).also {
                                    doReturn(Decimal.CERO).`when`(it).cantidad
                                    doReturn(1L).`when`(it).idFondoComprado
                                    doReturn(null).`when`(it).validoDesde
                                    doReturn(fechaMaximaEsperada).`when`(it).validoHasta
                                },
                                mockConDefaultAnswer(CreditoFondo::class.java).also {
                                    doReturn(Decimal.CERO).`when`(it).cantidad
                                    doReturn(1L).`when`(it).idFondoComprado
                                    doReturn(null).`when`(it).validoDesde
                                    doReturn(fechaMaximaEsperada.minusNanos(1)).`when`(it).validoHasta
                                }
                              )

                val entidad = TagConsumos(1L, creditosFondo)

                assertEquals(fechaMaximaEsperada, entidad.fechaValidoHastaMaxima)
            }

            @Test
            fun se_calcula_correctamente_si_hay_fecha_valido_hasta_nulas()
            {
                val creditosFondo =
                        listOf(
                                mockConDefaultAnswer(CreditoFondo::class.java).also {
                                    doReturn(Decimal.CERO).`when`(it).cantidad
                                    doReturn(1L).`when`(it).idFondoComprado
                                    doReturn(null).`when`(it).validoDesde
                                    doReturn(null).`when`(it).validoHasta
                                },
                                mockConDefaultAnswer(CreditoFondo::class.java).also {
                                    doReturn(Decimal.CERO).`when`(it).cantidad
                                    doReturn(1L).`when`(it).idFondoComprado
                                    doReturn(null).`when`(it).validoDesde
                                    doReturn(ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)).`when`(it).validoHasta
                                }
                              )

                val entidad = TagConsumos(1L, creditosFondo)

                assertNull(entidad.fechaValidoHastaMaxima)
            }
        }

        @Nested
        inner class PuedeConsumirEnFecha
        {
            private val creditoFondo =
                    mockConDefaultAnswer(CreditoFondo::class.java).also {
                        doReturn(Decimal.CERO).`when`(it).cantidad
                        doReturn(1L).`when`(it).idFondoComprado
                    }

            @Test
            fun si_las_fechas_de_limites_son_nulas_retorna_true()
            {
                doReturn(null).`when`(creditoFondo).validoDesde
                doReturn(null).`when`(creditoFondo).validoHasta

                val entidad = TagConsumos(1L, listOf(creditoFondo))

                assertTrue(entidad.puedeConsumirEnFecha(ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)))
            }

            @Nested
            inner class FechaInferiorNulaYSuperiorNoNula
            {
                @Test
                fun si_no_sobrepasa_la_superior_retorna_true()
                {
                    doReturn(null).`when`(creditoFondo).validoDesde
                    doReturn(ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO).plusYears(1)).`when`(creditoFondo).validoHasta

                    val entidad = TagConsumos(1L, listOf(creditoFondo))

                    assertTrue(entidad.puedeConsumirEnFecha(ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)))
                }

                @Test
                fun si_sobrepasa_la_superior_retorna_false()
                {
                    doReturn(null).`when`(creditoFondo).validoDesde
                    doReturn(ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO).minusYears(1)).`when`(creditoFondo).validoHasta

                    val entidad = TagConsumos(1L, listOf(creditoFondo))

                    assertFalse(entidad.puedeConsumirEnFecha(ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)))
                }
            }

            @Nested
            inner class FechaInferiorNoNulaYSuperiorNula
            {
                @Test
                fun si_no_sobrepasa_la_inferior_retorna_true()
                {
                    doReturn(ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO).minusYears(1)).`when`(creditoFondo).validoDesde
                    doReturn(null).`when`(creditoFondo).validoHasta

                    val entidad = TagConsumos(1L, listOf(creditoFondo))

                    assertTrue(entidad.puedeConsumirEnFecha(ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)))
                }

                @Test
                fun si_sobrepasa_la_inferior_retorna_false()
                {
                    doReturn(ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO).plusYears(1)).`when`(creditoFondo).validoDesde
                    doReturn(null).`when`(creditoFondo).validoHasta

                    val entidad = TagConsumos(1L, listOf(creditoFondo))

                    assertFalse(entidad.puedeConsumirEnFecha(ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)))
                }
            }

            @Nested
            inner class FechaInferiorNoNulaYSuperiorNoNula
            {
                @Test
                fun si_no_sobrepasa_los_limites_retorna_true()
                {
                    doReturn(ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO).minusYears(1)).`when`(creditoFondo).validoDesde
                    doReturn(ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO).plusYears(1)).`when`(creditoFondo).validoHasta

                    val entidad = TagConsumos(1L, listOf(creditoFondo))

                    assertTrue(entidad.puedeConsumirEnFecha(ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)))
                }

                @Test
                fun si_sobrepasa_la_inferior_retorna_false()
                {
                    doReturn(ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO).plusYears(1)).`when`(creditoFondo).validoDesde
                    doReturn(ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO).plusYears(2)).`when`(creditoFondo).validoHasta

                    val entidad = TagConsumos(1L, listOf(creditoFondo))

                    assertFalse(entidad.puedeConsumirEnFecha(ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)))
                }

                @Test
                fun si_sobrepasa_la_superior_retorna_false()
                {
                    doReturn(ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO).minusYears(2)).`when`(creditoFondo).validoDesde
                    doReturn(ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO).minusYears(1)).`when`(creditoFondo).validoHasta

                    val entidad = TagConsumos(1L, listOf(creditoFondo))

                    assertFalse(entidad.puedeConsumirEnFecha(ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)))
                }
            }
        }
    }

    @Test
    fun el_tamaño_total_de_bytes_corresponde_a_la_cabecera_mas_cada_uno_de_los_creditos_a_codificar()
    {
        val numeroDeCreditosACodificar = 17
        val entidad = TagConsumos(1L, List(numeroDeCreditosACodificar) {
            mockConDefaultAnswer(CreditoFondo::class.java).also {
                doReturn(Decimal.CERO).`when`(it).cantidad
                doReturn(1L).`when`(it).idFondoComprado
                doReturn(null).`when`(it).validoDesde
                doReturn(null).`when`(it).validoHasta
            }
        })
        val tamañoTotalEnBytesDeCreditos = numeroDeCreditosACodificar * FondoEnTag.TAMAÑO_EN_BYTES

        assertEquals(3 * 8 + 1 + tamañoTotalEnBytesDeCreditos, entidad.tamañoTotalEnBytes)
    }

    @Test
    fun al_convertir_a_un_byte_array_se_retorna_arreglo_correcto()
    {
        val idSesionDeManilla = 1002010565L

        val cantidad1 = Decimal(123.456)
        val idFondo1 = 4005006L
        val fechaValidoDesde1 = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO).minusDays(2)
        val fechaValidoHasta1 = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO).plusDays(5)

        val cantidad2 = Decimal(7894.000513)
        val idFondo2 = 89710049L
        val fechaValidoDesde2 = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
        val fechaValidoHasta2 = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO).plusDays(1)

        val creditosFondo =
                listOf(
                        mockConDefaultAnswer(CreditoFondo::class.java).also {
                            doReturn(cantidad1).`when`(it).cantidad
                            doReturn(idFondo1).`when`(it).idFondoComprado
                            doReturn(fechaValidoDesde1).`when`(it).validoDesde
                            doReturn(fechaValidoHasta1).`when`(it).validoHasta
                        },
                        mockConDefaultAnswer(CreditoFondo::class.java).also {
                            doReturn(cantidad2).`when`(it).cantidad
                            doReturn(idFondo2).`when`(it).idFondoComprado
                            doReturn(fechaValidoDesde2).`when`(it).validoDesde
                            doReturn(fechaValidoHasta2).`when`(it).validoHasta
                        }
                      )

        val arregloEsperado =
                concatenarArreglos(
                        longABytes(idSesionDeManilla),
                        longABytes(fechaValidoDesde1.toInstant().toEpochMilli()),
                        longABytes(fechaValidoHasta1.toInstant().toEpochMilli()),
                        byteArrayOf(creditosFondo.size.toByte()),
                        FondoEnTag(idFondo1, cantidad1).aByteArray(),
                        FondoEnTag(idFondo2, cantidad2).aByteArray()
                                  )

        val entidad = TagConsumos(idSesionDeManilla, creditosFondo)

        assertArrayEquals(arregloEsperado, entidad.aByteArray())
    }

    @Nested
    inner class InstanciacionDesdeByteArray
    {
        @Test
        fun lanza_excepcion_si_el_tamaño_del_arreglo_es_inferior_al_tamaño_de_la_cabecera()
        {
            val arregloBytes = byteArrayOf()
            val excepcion = assertThrows<EntidadConCampoFueraDeRango> { TagConsumos(arregloBytes) }

            assertEquals("Tamaño del arreglo de bytes", excepcion.nombreDelCampo)
            assertEquals(TagConsumos.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
            assertEquals(arregloBytes.size.toString(), excepcion.valorUsado)
            assertEquals(TagConsumos.TAMAÑO_EN_BYTES_CABECERA.toString(), excepcion.valorDelLimite)
            assertEquals(EntidadConCampoFueraDeRango.Limite.INFERIOR, excepcion.limiteSobrepasado)
        }

        @Test
        fun lanza_excepcion_si_el_numero_de_creditos_a_deserializar_es_inferior_a_1()
        {
            val arregloBytes =
                    concatenarArreglos(
                            longABytes(1L),
                            longABytes(10000015151L),
                            longABytes(10000015151L),
                            byteArrayOf(0.toByte())
                                      )

            val excepcion = assertThrows<EntidadConCampoFueraDeRango> { TagConsumos(arregloBytes) }

            assertEquals("Número de créditos a leer en arreglo", excepcion.nombreDelCampo)
            assertEquals(TagConsumos.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
            assertEquals("0", excepcion.valorUsado)
            assertEquals("1", excepcion.valorDelLimite)
            assertEquals(EntidadConCampoFueraDeRango.Limite.INFERIOR, excepcion.limiteSobrepasado)
        }

        @Test
        fun lanza_excepcion_si_el_tamaño_esperado_no_concuerda_con_el_tamaño_del_arreglo()
        {
            val cantidadDeCreditos = 10
            val arregloBytes =
                    concatenarArreglos(
                            longABytes(1L),
                            longABytes(10000015151L),
                            longABytes(10000015151L),
                            byteArrayOf(cantidadDeCreditos.toByte())
                                      )

            val excepcion = assertThrows<EntidadConCampoFueraDeRango> { TagConsumos(arregloBytes) }

            assertEquals("Tamaño esperado del arreglo", excepcion.nombreDelCampo)
            assertEquals(TagConsumos.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
            assertEquals(arregloBytes.size.toString(), excepcion.valorUsado)
            assertEquals(
                    (TagConsumos.TAMAÑO_EN_BYTES_CABECERA + cantidadDeCreditos * FondoEnTag.TAMAÑO_EN_BYTES).toString(),
                    excepcion.valorDelLimite
                        )
            assertEquals(EntidadConCampoFueraDeRango.Limite.DIFERENTE, excepcion.limiteSobrepasado)
        }

        @Test
        fun se_instancia_correctamente()
        {
            val creditosFondo =
                    listOf(
                            mockConDefaultAnswer(CreditoFondo::class.java).also {
                                doReturn(Decimal(123.456)).`when`(it).cantidad
                                doReturn(4005006L).`when`(it).idFondoComprado
                                doReturn(ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO).minusDays(2)).`when`(it).validoDesde
                                doReturn(ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO).plusDays(5)).`when`(it).validoHasta
                            },
                            mockConDefaultAnswer(CreditoFondo::class.java).also {
                                doReturn(Decimal(7894.000513)).`when`(it).cantidad
                                doReturn(89710049L).`when`(it).idFondoComprado
                                doReturn(ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)).`when`(it).validoDesde
                                doReturn(ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO).plusDays(1)).`when`(it).validoHasta
                            }
                          )

            val entidad = TagConsumos(1002010565L, creditosFondo)

            assertEquals(entidad, TagConsumos(entidad.aByteArray()))
        }
    }
}