package co.smartobjects.entidades.operativas.compras

import co.smartobjects.entidades.excepciones.EntidadConCampoFueraDeRango
import co.smartobjects.entidades.excepciones.EntidadConCampoHeterogeneo
import co.smartobjects.entidades.excepciones.EntidadConCampoVacio
import co.smartobjects.mockConDefaultAnswer
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.spy
import org.threeten.bp.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DisplayName("CreditoPaquete")
internal class CreditoPaquetePruebas
{
    companion object
    {
        private val fechaActual = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
        private val creditoFondoPruebas1 = CreditoFondo(
                1,
                null,
                Decimal(10),
                Decimal(1000),
                Decimal(150),
                fechaActual,
                fechaActual,
                false,
                "Taquilla",
                "Un usuario",
                2,
                6,
                "código externo fondo 1",
                3,
                "un-uuid-de-dispositivo",
                4,
                5
                                                       )
        private val creditoFondoPruebas2 = CreditoFondo(
                1,
                1,
                Decimal(20),
                Decimal(2000),
                Decimal(250),
                fechaActual.plusDays(2),
                fechaActual.plusDays(2),
                true,
                "Orbita",
                "Otro usuario",
                2,
                12,
                "código externo fondo 2",
                4,
                "otro-uuid-de-dispositivo",
                6,
                7
                                                       )
    }

    @Test
    fun campos_quedan_con_valores_correctos()
    {
        val entidad = CreditoPaquete(1, "código externo paquete", listOf(creditoFondoPruebas1, creditoFondoPruebas2))

        assertEquals(listOf(creditoFondoPruebas1, creditoFondoPruebas2), entidad.campoCreditosFondos.valor)
    }

    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val entidadInicial = CreditoPaquete(1, "código externo paquete 1", listOf(creditoFondoPruebas1))
        val entidadCopiada = entidadInicial.copiar(2, "código externo paquete 2", listOf(creditoFondoPruebas2))
        val entidadEsperada = CreditoPaquete(2, "código externo paquete 2", listOf(creditoFondoPruebas2))
        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun el_valor_pagado_se_calcula_como_la_suma_de_todos_los_valores_pagados()
    {
        val creditos = listOf(creditoFondoPruebas1, creditoFondoPruebas2)
        val entidad = CreditoPaquete(1, "código externo paquete", creditos)

        assertEquals(creditos.map { it.valorPagado }.reduce { acc, siguiente -> acc + siguiente }, entidad.valorPagado)
    }

    @Test
    fun el_valor_pagado_sin_impuesto_se_calcula_como_la_suma_de_todos_los_valores_pagados()
    {
        val creditos = listOf(creditoFondoPruebas1, creditoFondoPruebas2)
        val entidad = CreditoPaquete(1, "código externo paquete", creditos)

        assertEquals(creditos.map { it.valorPagadoSinImpuesto }.reduce { acc, siguiente -> acc + siguiente }, entidad.valorPagadoSinImpuesto)
    }

    @Test
    fun el_valor_pagado_se_calcula_como_la_suma_de_todos_los_valores_de_impuesto_pagados()
    {
        val creditos = listOf(creditoFondoPruebas1, creditoFondoPruebas2)
        val entidad = CreditoPaquete(1, "código externo paquete", creditos)

        assertEquals(creditos.map { it.valorImpuestoPagado }.reduce { acc, siguiente -> acc + siguiente }, entidad.valorImpuestoPagado)
    }

    @Test
    fun no_se_permite_instanciar_sin_creditos_fondo_incluidos()
    {
        val excepcion = assertThrows<EntidadConCampoVacio> {
            CreditoPaquete(1, "código externo paquete", listOf())
        }

        assertEquals(CreditoPaquete.Campos.CREDITOS_FONDOS, excepcion.nombreDelCampo)
        assertEquals(CreditoPaquete.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun no_se_permite_instanciar_si_todos_los_creditos_fondos_no_tienen_el_mismo_dueño()
    {
        val idDueñoDiferente = 786321L
        val creditosPaquete =
                listOf(
                        creditoFondoPruebas1,
                        creditoFondoPruebas2.copiar(idPersonaDueña = idDueñoDiferente)
                      )


        val excepcion = assertThrows<EntidadConCampoHeterogeneo> {
            CreditoPaquete(1, "código externo paquete", creditosPaquete)
        }

        assertEquals("${CreditoFondo.Campos.ID_PERSONA_DUEÑA} en cada ${CreditoPaquete.Campos.CREDITOS_FONDOS}", excepcion.nombreDelCampo)
        assertEquals(CreditoPaquete.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        assertEquals(setOf(creditoFondoPruebas1.idPersonaDueña, idDueñoDiferente), excepcion.valoresDiferentes)
    }
}

@DisplayName("CreditoPaqueteConNombre")
class CreditoPaqueteConNombrePruebas
{
    @Test
    fun no_se_puede_instanciar_con_cantidad_inferior_a_1()
    {
        val mockCreditoFondo = mockConDefaultAnswer(CreditoFondo::class.java).also {
            doReturn(1234L).`when`(it).id
            doReturn(Decimal.UNO).`when`(it).valorPagado
            doReturn(1L).`when`(it).idCliente
            doReturn(1L).`when`(it).idImpuestoPagado
            doReturn(Decimal.UNO).`when`(it).tasaImpositivaUsada
        }
        val mockCreditoPaquete = mockConDefaultAnswer(CreditoPaquete::class.java).also {
            doReturn(listOf(mockCreditoFondo)).`when`(it).creditosFondos
        }

        val excepcion = assertThrows<EntidadConCampoFueraDeRango> { CreditoPaqueteConNombre("nombre", 0, mockCreditoPaquete) }

        assertEquals(CreditoPaqueteConNombre::cantidad.name, excepcion.nombreDelCampo)
        assertEquals(CreditoPaqueteConNombre::class.java.simpleName, excepcion.nombreEntidad)
        assertEquals(0.toString(), excepcion.valorUsado)
        assertEquals(1.toString(), excepcion.valorDelLimite)
        assertEquals(EntidadConCampoFueraDeRango.Limite.INFERIOR, excepcion.limiteSobrepasado)

    }

    @Nested
    inner class EstaPagado
    {
        @Test
        fun esta_pagado_si_algun_credito_de_fondo_tiene_un_id_diferente_a_nulo()
        {
            val mockCreditoFondo = mockConDefaultAnswer(CreditoFondo::class.java).also {
                doReturn(1234L).`when`(it).id
                doReturn(Decimal.UNO).`when`(it).valorPagado
                doReturn(1L).`when`(it).idCliente
                doReturn(1L).`when`(it).idImpuestoPagado
                doReturn(Decimal.UNO).`when`(it).tasaImpositivaUsada
            }
            val mockCreditoPaquete = mockConDefaultAnswer(CreditoPaquete::class.java).also {
                doReturn(listOf(mockCreditoFondo)).`when`(it).creditosFondos
            }

            val creditoPagado = CreditoPaqueteConNombre("nombre", 1, mockCreditoPaquete)

            assertTrue(creditoPagado.estaPagado)
        }

        @Test
        fun no_esta_pagado_si_algun_credito_de_fondo_tiene_un_id_diferente_igual_a_nulo()
        {
            val mockCredito = mockConDefaultAnswer(CreditoFondo::class.java).also {
                doReturn(null).`when`(it).id
                doReturn(Decimal.UNO).`when`(it).valorPagado
                doReturn(1L).`when`(it).idCliente
                doReturn(1L).`when`(it).idImpuestoPagado
                doReturn(Decimal.UNO).`when`(it).tasaImpositivaUsada
            }
            val mockCreditoPaquete = mockConDefaultAnswer(CreditoPaquete::class.java).also {
                doReturn(listOf(mockCredito)).`when`(it).creditosFondos
            }
            val creditoNoPagado = CreditoPaqueteConNombre("nombre", 1, mockCreditoPaquete)

            assertFalse(creditoNoPagado.estaPagado)
        }
    }

    @Nested
    inner class PrecioConImpuestos
    {
        @Test
        fun se_calcula_correctamente()
        {
            val mockCredito1 = mockConDefaultAnswer(CreditoFondo::class.java).also {
                doReturn(1234L).`when`(it).id
                doReturn(Decimal.UNO).`when`(it).valorPagado
                doReturn(1L).`when`(it).idCliente
                doReturn(1L).`when`(it).idImpuestoPagado
                doReturn(Decimal.UNO).`when`(it).tasaImpositivaUsada
            }
            val mockCredito2 = mockConDefaultAnswer(CreditoFondo::class.java).also {
                doReturn(1234L).`when`(it).id
                doReturn(Decimal.DIEZ).`when`(it).valorPagado
                doReturn(1L).`when`(it).idCliente
                doReturn(1L).`when`(it).idImpuestoPagado
                doReturn(Decimal.UNO).`when`(it).tasaImpositivaUsada
            }
            val mockCreditoPaquete = mockConDefaultAnswer(CreditoPaquete::class.java).also {
                doReturn(listOf(mockCredito1, mockCredito2)).`when`(it).creditosFondos
            }

            val creditoPaquete = CreditoPaqueteConNombre("nombre", 1, mockCreditoPaquete)
                .run {
                    spy(this).also {
                        doReturn(true).`when`(it).estaPagado
                    }
                }

            assertEquals(Decimal.UNO + Decimal.DIEZ, creditoPaquete.precioConImpuestos)
        }
    }
}