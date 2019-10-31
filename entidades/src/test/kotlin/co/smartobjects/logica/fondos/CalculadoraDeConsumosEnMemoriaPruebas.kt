package co.smartobjects.logica.fondos

import co.smartobjects.entidades.fondos.Sku
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.entidades.tagscodificables.FondoEnTag
import co.smartobjects.entidades.ubicaciones.consumibles.Consumo
import co.smartobjects.mockConDefaultAnswer
import co.smartobjects.utilidades.Decimal
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.doReturn
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DisplayName("CalculadoraDeConsumosEnMemoria")
internal class CalculadoraDeConsumosEnMemoriaPruebas
{
    @Test
    fun si_se_usa_una_lista_vacia_de_consumos_arroja_excepcion()
    {
        val fondo1 = Sku(1, 1, "Fondo 1", true, false, false, Precio(Decimal.CERO, 1), "a", 1, null)
        val fondo2 = Sku(1, 2, "Fondo 2", true, false, false, Precio(Decimal.CERO, 1), "b", 1, null)
        val fondos = sequenceOf(fondo1, fondo2)

        val calculadora = CalculadoraDeConsumosEnMemoria(fondos)

        val excepcion = assertThrows<IllegalStateException> {
            calculadora.descontarConsumosDeFondos(listOf(), listOf(mockConDefaultAnswer(CalculadoraDeConsumos.FondoEnTagConIdPaquete::class.java)))
        }

        assertEquals("Se debe definir consumos", excepcion.message)
    }

    @Test
    fun si_se_usa_una_lista_vacia_de_fondos_en_tag_arroja_excepcion()
    {
        val fondo1 = Sku(1, 1, "Fondo 1", true, false, false, Precio(Decimal.CERO, 1), "a", 1, null)
        val fondo2 = Sku(1, 2, "Fondo 2", true, false, false, Precio(Decimal.CERO, 1), "b", 1, null)
        val fondos = sequenceOf(fondo1, fondo2)

        val calculadora = CalculadoraDeConsumosEnMemoria(fondos)

        val excepcion = assertThrows<IllegalStateException> {
            calculadora.descontarConsumosDeFondos(listOf(mockConDefaultAnswer(Consumo::class.java)), listOf())
        }

        assertEquals("Se debe tener fondos en el tag", excepcion.message)
    }

    @Nested
    inner class ConCantidadSuficienteEnFondo
    {
        @Nested
        inner class DeTipoAcceso

        @Nested
        inner class TipoSku
        {
            @Nested
            inner class Limitado
            {
                private val fondo1 =
                        Sku(1, 1, "Fondo 1", true, false, false, Precio(Decimal.CERO, 1), "a", 1, null)

                private val fondo2 =
                        Sku(1, 2, "Fondo 2", true, false, false, Precio(Decimal.CERO, 1), "b", 1, null)

                private val fondos = sequenceOf(fondo1, fondo2)

                private val calculadora = CalculadoraDeConsumosEnMemoria(fondos)

                private val cantidadAConsumirDeFondo1 = Decimal.UNO
                private val cantidadAConsumirDeFondo2 = Decimal.UNO

                private val consumosARealizar =
                        listOf(
                                Consumo(999, fondo1.id!!, cantidadAConsumirDeFondo1),
                                Consumo(999, fondo2.id!!, cantidadAConsumirDeFondo2)
                              )

                private val cantidadDisponibleFondo1 = cantidadAConsumirDeFondo1 + 3
                private val cantidadDisponibleFondo2 = cantidadAConsumirDeFondo2 + 7

                private val fondosEnTag = listOf(
                        CalculadoraDeConsumos.FondoEnTagConIdPaquete(FondoEnTag(fondo1.id!!, cantidadDisponibleFondo1)),
                        CalculadoraDeConsumos.FondoEnTagConIdPaquete(FondoEnTag(fondo2.id!!, cantidadDisponibleFondo2))
                                                )

                @Test
                fun se_realizaron_todos_los_consumos_por_completo()
                {
                    val saldo = calculadora.descontarConsumosDeFondos(consumosARealizar, fondosEnTag)

                    assertTrue(saldo.todosLosConsumosRealizadosPorCompleto)
                }

                @Test
                fun hay_un_resultado_de_consumo_por_consumo_y_estan_en_el_mismo_orden_que_los_consumos_a_realizar()
                {
                    val saldo = calculadora.descontarConsumosDeFondos(consumosARealizar, fondosEnTag)

                    assertEquals(saldo.desgloses.size, consumosARealizar.size)
                    for ((i, resultado) in saldo.desgloses.withIndex())
                    {
                        assertEquals(resultado.consumo, consumosARealizar[i])
                    }
                }

                @Test
                fun en_cada_resultado_de_consumo_solo_hay_un_consumo_realizado_y_es_correcto()
                {
                    val consumosRealizadosEsperados =
                            listOf(
                                    CalculadoraDeConsumos.ConsumoRealizado(
                                            fondo1.id!!,
                                            cantidadDisponibleFondo1,
                                            cantidadAConsumirDeFondo1,
                                            cantidadDisponibleFondo1 - cantidadAConsumirDeFondo1
                                                                          ),
                                    CalculadoraDeConsumos.ConsumoRealizado(
                                            fondo2.id!!,
                                            cantidadDisponibleFondo2,
                                            cantidadAConsumirDeFondo2,
                                            cantidadDisponibleFondo2 - cantidadAConsumirDeFondo2
                                                                          )
                                  )

                    val saldo = calculadora.descontarConsumosDeFondos(consumosARealizar, fondosEnTag)

                    for ((i, consumosRealizados) in saldo.desgloses.withIndex())
                    {
                        assertEquals(1, consumosRealizados.consumosRealizados.size)
                        assertEquals(consumosRealizadosEsperados[i], consumosRealizados.consumosRealizados.first())
                    }
                }

                @Test
                fun los_nuevos_fondos_en_el_tag_son_los_mismos_originales_menos_la_cantidad_consumida()
                {
                    val fondosEnTagEsperados =
                            listOf(
                                    fondosEnTag[0].fondoEnTag.copiar(cantidad = fondosEnTag[0].cantidad - cantidadAConsumirDeFondo1),
                                    fondosEnTag[1].fondoEnTag.copiar(cantidad = fondosEnTag[1].cantidad - cantidadAConsumirDeFondo1)
                                  )

                    val saldo = calculadora.descontarConsumosDeFondos(consumosARealizar, fondosEnTag)

                    assertEquals(fondosEnTagEsperados, saldo.fondosEnTag)
                }
            }

            @Nested
            inner class Ilimitado
        }
    }

    @Nested
    inner class SinCantidadSuficienteEnFondo
    {
        @Nested
        inner class DeTipoAcceso

        @Nested
        inner class TipoSku
        {
            @Nested
            inner class Limitado
            {
                @Nested
                inner class SinFondoDisponibleNiCategoriasSkuNiDinero
                {
                    private val fondo1 =
                            Sku(1, 1, "Fondo 1", true, false, false, Precio(Decimal.CERO, 1), "a", 1, null)

                    private val fondo2 =
                            Sku(1, 2, "Fondo 2", true, false, false, Precio(Decimal.CERO, 1), "b", 1, null)

                    private val fondoNoDisponible1 =
                            Sku(1, 3, "Fondo No Disponible 1", true, false, false, Precio(Decimal.CERO, 1), "c", 1, null)

                    private val fondoNoDisponible2 =
                            Sku(1, 4, "Fondo No Disponible 2", true, false, false, Precio(Decimal.CERO, 1), "d", 1, null)

                    private val fondos = sequenceOf(fondo1, fondo2, fondoNoDisponible1, fondoNoDisponible2)

                    private val calculadora = CalculadoraDeConsumosEnMemoria(fondos)

                    private val fondosEnTag = listOf(
                            CalculadoraDeConsumos.FondoEnTagConIdPaquete(FondoEnTag(fondo1.id!!, Decimal.UNO)),
                            CalculadoraDeConsumos.FondoEnTagConIdPaquete(FondoEnTag(fondo2.id!!, Decimal.UNO))
                                                    )

                    private val cantidadAConsumirDeFondoNoDisponible1 = Decimal.UNO
                    private val cantidadAConsumirDeFondoNoDisponible2 = Decimal.UNO

                    private val consumosARealizar =
                            listOf(
                                    Consumo(999, fondoNoDisponible1.id!!, cantidadAConsumirDeFondoNoDisponible1),
                                    Consumo(999, fondoNoDisponible2.id!!, cantidadAConsumirDeFondoNoDisponible2)
                                  )

                    @Test
                    fun no_se_realizaron_todos_los_consumos_por_completo()
                    {
                        val saldo = calculadora.descontarConsumosDeFondos(consumosARealizar, fondosEnTag)

                        assertFalse(saldo.todosLosConsumosRealizadosPorCompleto)
                    }

                    @Test
                    fun hay_un_resultado_de_consumo_por_consumo_y_estan_en_el_mismo_orden_que_los_consumos_a_realizar()
                    {
                        val saldo = calculadora.descontarConsumosDeFondos(consumosARealizar, fondosEnTag)

                        assertEquals(saldo.desgloses.size, consumosARealizar.size)
                        for ((i, resultado) in saldo.desgloses.withIndex())
                        {
                            assertEquals(resultado.consumo, consumosARealizar[i])
                        }
                    }

                    @Test
                    fun en_cada_resultado_de_consumo_solo_hay_un_consumo_realizado_y_es_correcto()
                    {
                        val consumosRealizadosEsperados =
                                listOf(
                                        CalculadoraDeConsumos.ConsumoRealizado(fondoNoDisponible1.id!!, Decimal.CERO, Decimal.CERO, Decimal.CERO),
                                        CalculadoraDeConsumos.ConsumoRealizado(fondoNoDisponible2.id!!, Decimal.CERO, Decimal.CERO, Decimal.CERO)
                                      )

                        val saldo = calculadora.descontarConsumosDeFondos(consumosARealizar, fondosEnTag)

                        for ((i, consumosRealizados) in saldo.desgloses.withIndex())
                        {
                            assertEquals(1, consumosRealizados.consumosRealizados.size)
                            assertEquals(consumosRealizadosEsperados[i], consumosRealizados.consumosRealizados.first())
                        }
                    }

                    @Test
                    fun los_nuevos_fondos_en_el_tag_son_los_mismos_originales()
                    {
                        val fondosEnTagEsperados = listOf(fondosEnTag[0].fondoEnTag, fondosEnTag[1].fondoEnTag)

                        val saldo = calculadora.descontarConsumosDeFondos(consumosARealizar, fondosEnTag)

                        assertEquals(fondosEnTagEsperados, saldo.fondosEnTag)
                    }
                }

                @Nested
                inner class ConFondoConsumidoAMediasYSinFondosDeCategoriaSkuNiDinero
                {
                    private val fondo1 =
                            Sku(1, 1, "Fondo 1", true, false, false, Precio(Decimal.CERO, 1), "a", 1, null)

                    private val fondo2 =
                            Sku(1, 2, "Fondo 2", true, false, false, Precio(Decimal.CERO, 1), "b", 1, null)

                    private val fondos = sequenceOf(fondo1, fondo2)

                    private val calculadora = CalculadoraDeConsumosEnMemoria(fondos)

                    private val cantidadDisponibleFondo1 = Decimal.UNO
                    private val cantidadDisponibleFondo2 = Decimal.UNO

                    private val fondosEnTag = listOf(
                            CalculadoraDeConsumos.FondoEnTagConIdPaquete(FondoEnTag(fondo1.id!!, cantidadDisponibleFondo1)),
                            CalculadoraDeConsumos.FondoEnTagConIdPaquete(FondoEnTag(fondo2.id!!, cantidadDisponibleFondo2))
                                                    )

                    private val cantidadAConsumirDeFondo1 = cantidadDisponibleFondo1 + 2
                    private val cantidadAConsumirDeFondo2 = cantidadDisponibleFondo2 + 5

                    private val consumosARealizar =
                            listOf(
                                    Consumo(999, fondo1.id!!, cantidadAConsumirDeFondo1),
                                    Consumo(999, fondo2.id!!, cantidadAConsumirDeFondo2)
                                  )

                    @Test
                    fun no_se_realizaron_todos_los_consumos_por_completo()
                    {
                        val saldo = calculadora.descontarConsumosDeFondos(consumosARealizar, fondosEnTag)

                        assertFalse(saldo.todosLosConsumosRealizadosPorCompleto)
                    }

                    @Test
                    fun hay_un_resultado_de_consumo_por_consumo_y_estan_en_el_mismo_orden_que_los_consumos_a_realizar()
                    {
                        val saldo = calculadora.descontarConsumosDeFondos(consumosARealizar, fondosEnTag)

                        assertEquals(saldo.desgloses.size, consumosARealizar.size)
                        for ((i, resultado) in saldo.desgloses.withIndex())
                        {
                            assertEquals(resultado.consumo, consumosARealizar[i])
                        }
                    }

                    @Test
                    fun en_cada_resultado_de_consumo_solo_hay_un_consumo_realizado_y_es_correcto()
                    {
                        val consumosRealizadosEsperados =
                                listOf(
                                        CalculadoraDeConsumos.ConsumoRealizado(
                                                fondo1.id!!,
                                                cantidadDisponibleFondo1,
                                                cantidadDisponibleFondo1,
                                                Decimal.CERO
                                                                              ),
                                        CalculadoraDeConsumos.ConsumoRealizado(
                                                fondo2.id!!,
                                                cantidadDisponibleFondo2,
                                                cantidadDisponibleFondo2,
                                                Decimal.CERO
                                                                              )
                                      )

                        val saldo = calculadora.descontarConsumosDeFondos(consumosARealizar, fondosEnTag)

                        for ((i, consumosRealizados) in saldo.desgloses.withIndex())
                        {
                            assertEquals(1, consumosRealizados.consumosRealizados.size)
                            assertEquals(consumosRealizadosEsperados[i], consumosRealizados.consumosRealizados.first())
                        }
                    }

                    @Test
                    fun los_nuevos_fondos_en_el_tag_son_los_mismos_originales()
                    {
                        val fondosEnTagEsperados = listOf(fondosEnTag[0].fondoEnTag, fondosEnTag[1].fondoEnTag)

                        val saldo = calculadora.descontarConsumosDeFondos(consumosARealizar, fondosEnTag)

                        assertEquals(fondosEnTagEsperados, saldo.fondosEnTag)
                    }
                }
            }

            @Nested
            inner class Ilimitado
        }
    }
}

@DisplayName("CalculadoraDeConsumos.ResultadosDeConsumos")
internal class CalculadoraDeConsumosResultadosDeConsumosPruebas
{
    @Test
    fun los_descuentos_son_exitosos_en_su_totalidad_si_todos_los_consumos_fueron_completos()
    {
        val casoExitoso =
                CalculadoraDeConsumos.ResultadosDeConsumos(
                        listOf(
                                mockConDefaultAnswer(CalculadoraDeConsumos.DesgloseDeConsumo::class.java).also {
                                    doReturn(true).`when`(it).consumidoCompetamente
                                },
                                mockConDefaultAnswer(CalculadoraDeConsumos.DesgloseDeConsumo::class.java).also {
                                    doReturn(true).`when`(it).consumidoCompetamente
                                }
                              ),
                        listOf(mockConDefaultAnswer(FondoEnTag::class.java))
                                                          )

        assertTrue(casoExitoso.todosLosConsumosRealizadosPorCompleto)

        val casoFallido =
                CalculadoraDeConsumos.ResultadosDeConsumos(
                        listOf(
                                mockConDefaultAnswer(CalculadoraDeConsumos.DesgloseDeConsumo::class.java).also {
                                    doReturn(true).`when`(it).consumidoCompetamente
                                },
                                mockConDefaultAnswer(CalculadoraDeConsumos.DesgloseDeConsumo::class.java).also {
                                    doReturn(false).`when`(it).consumidoCompetamente
                                }
                              ),
                        listOf(mockConDefaultAnswer(FondoEnTag::class.java))
                                                          )

        assertFalse(casoFallido.todosLosConsumosRealizadosPorCompleto)
    }
}

@DisplayName("CalculadoraDeConsumos.DesgloseDeConsumo")
internal class CalculadoraDeConsumosDesgloseDeConsumoPruebas
{
    @Test
    fun se_consumio_completamente_si_para_todos_los_consumos_realizados_no_falta_consumir_cantidad()
    {
        val casoExitoso = CalculadoraDeConsumos.DesgloseDeConsumo(
                mockConDefaultAnswer(Consumo::class.java).also { doReturn(Decimal(13)).`when`(it).cantidad },
                listOf(
                        CalculadoraDeConsumos.ConsumoRealizado(1, Decimal.UNO, Decimal(3), Decimal.UNO),
                        CalculadoraDeConsumos.ConsumoRealizado(1, Decimal.UNO, Decimal(10), Decimal.UNO)
                      )
                                                                 )

        assertTrue(casoExitoso.consumidoCompetamente)

        val casoFallido = CalculadoraDeConsumos.DesgloseDeConsumo(
                mockConDefaultAnswer(Consumo::class.java).also { doReturn(Decimal(8)).`when`(it).cantidad },
                listOf(
                        CalculadoraDeConsumos.ConsumoRealizado(1, Decimal.UNO, Decimal(2), Decimal.UNO),
                        CalculadoraDeConsumos.ConsumoRealizado(1, Decimal.UNO, Decimal(3), Decimal.UNO)
                      )
                                                                 )

        assertFalse(casoFallido.consumidoCompetamente)
    }
}