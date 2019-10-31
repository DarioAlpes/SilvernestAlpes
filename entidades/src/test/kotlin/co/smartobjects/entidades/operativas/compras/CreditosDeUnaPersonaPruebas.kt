package co.smartobjects.entidades.operativas.compras

import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.threeten.bp.ZonedDateTime
import kotlin.test.assertEquals

@DisplayName("CreditosDeUnaPersona")
internal class CreditosDeUnaPersonaPruebas
{
    companion object
    {
        private val fechaActual = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
        private fun crearCreditoFondoSegunIndice(indice: Int): CreditoFondo
        {
            return CreditoFondo(
                    indice.toLong(),
                    indice.toLong() + 1,
                    Decimal(indice * 10),
                    Decimal(indice * 1000),
                    Decimal(indice * 150),
                    fechaActual.plusDays(indice.toLong()),
                    fechaActual.plusDays(indice.toLong()),
                    false,
                    "Taquilla",
                    "Un usuario",
                    7357474L,
                    indice.toLong() * 6,
                    "código externo fondo",
                    indice.toLong() * 3,
                    "un-uuid-de-dispositivo",
                    indice.toLong() * 4,
                    indice.toLong() * 5
                               )

        }

        private fun crearCreditoPaqueteSegunIndice(indice: Int, numeroFondosCredito: Int): CreditoPaquete
        {
            return CreditoPaquete(
                    indice.toLong(),
                    "código externo paquete $indice",
                    (indice..numeroFondosCredito + indice).map {
                        crearCreditoFondoSegunIndice(it)
                    }
                                 )

        }
    }

    @Test
    fun se_instancia_correctamente_con_creditos_fondo_y_creditos_paquete()
    {
        val creditoFondo1 = crearCreditoFondoSegunIndice(1)
        val creditoFondo2 = crearCreditoFondoSegunIndice(2)
        val creditoPaquete1 = crearCreditoPaqueteSegunIndice(10, 2)
        val creditoPaquete2 = crearCreditoPaqueteSegunIndice(20, 3)
        val entidad = CreditosDeUnaPersona(
                1,
                1,
                listOf(creditoFondo1, creditoFondo2),
                listOf(creditoPaquete1, creditoPaquete2)
                                          )

        val creditosEsperados =
                listOf(creditoFondo1, creditoFondo2) +
                creditoPaquete1.creditosFondos +
                creditoPaquete2.creditosFondos

        assertEquals(creditosEsperados.sortedBy { it.id }, entidad.creditos.sortedBy { it.id })
        assertEquals(listOf(creditoPaquete1, creditoPaquete2), entidad.creditosPaquetes)
        assertEquals(listOf(creditoFondo1, creditoFondo2), entidad.creditosFondos)
    }

    @Test
    fun se_instancia_correctamente_con_creditos_fondo_pero_sin_creditos_paquete()
    {
        val creditoFondo1 = crearCreditoFondoSegunIndice(1)
        val creditoFondo2 = crearCreditoFondoSegunIndice(2)
        val entidad = CreditosDeUnaPersona(
                1,
                1,
                listOf(creditoFondo1, creditoFondo2),
                listOf()
                                          )

        val creditosEsperados = listOf(creditoFondo1, creditoFondo2)

        assertEquals(creditosEsperados.sortedBy { it.id }, entidad.creditos.sortedBy { it.id })
        assertEquals(listOf(), entidad.creditosPaquetes)
        assertEquals(listOf(creditoFondo1, creditoFondo2), entidad.creditosFondos)
    }

    @Test
    fun se_instancia_correctamente_sin_creditos_fondo_pero_con_creditos_paquete()
    {
        val creditoPaquete1 = crearCreditoPaqueteSegunIndice(10, 2)
        val creditoPaquete2 = crearCreditoPaqueteSegunIndice(20, 3)
        val entidad = CreditosDeUnaPersona(
                1,
                1,
                listOf(),
                listOf(creditoPaquete1, creditoPaquete2)
                                          )

        val creditosEsperados = creditoPaquete1.creditosFondos + creditoPaquete2.creditosFondos

        assertEquals(creditosEsperados.sortedBy { it.id }, entidad.creditos.sortedBy { it.id })
        assertEquals(listOf(creditoPaquete1, creditoPaquete2), entidad.creditosPaquetes)
        assertEquals(listOf(), entidad.creditosFondos)
    }

    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos_y_mantiene_id_y_campos_asociados()
    {
        val creditoFondo1 = crearCreditoFondoSegunIndice(1)
        val creditoPaquete1 = crearCreditoPaqueteSegunIndice(10, 2)

        val entidadInicial = CreditosDeUnaPersona(
                1,
                1,
                listOf(creditoFondo1),
                listOf(creditoPaquete1)
                                                 )
        val creditoFondo2 = crearCreditoFondoSegunIndice(2)
        val creditoPaquete2 = crearCreditoPaqueteSegunIndice(20, 3)

        val entidadCopiada = entidadInicial.copiar(
                1,
                1,
                listOf(creditoFondo2),
                listOf(creditoPaquete2)
                                                  )

        val entidadEsperada = CreditosDeUnaPersona(
                1,
                1,
                listOf(creditoFondo2),
                listOf(creditoPaquete2)
                                                  )
        assertEquals(entidadEsperada, entidadCopiada)
    }
}