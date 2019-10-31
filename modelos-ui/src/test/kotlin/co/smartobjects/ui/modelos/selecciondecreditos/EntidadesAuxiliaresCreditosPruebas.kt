package co.smartobjects.ui.modelos.selecciondecreditos

import co.smartobjects.entidades.fondos.Paquete
import co.smartobjects.ui.modelos.mockConDefaultAnswer
import co.smartobjects.utilidades.Decimal
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doReturn
import kotlin.test.assertEquals


@DisplayName("ProductoPaquete")
class ProductoPaquetePruebas
{
    @Test
    fun id_corresponde_al_id_del_paquete()
    {
        val idPaqueteEsperado = 123456L
        val paqueteDePrueba = mockConDefaultAnswer(Paquete::class.java).also {
            doReturn(idPaqueteEsperado).`when`(it).id
            doReturn("código externo paquete").`when`(it).codigoExterno
            doReturn("asdf").`when`(it).nombre
            doReturn(listOf(Paquete.FondoIncluido(1, "código externo fondo 1", Decimal.UNO))).`when`(it).fondosIncluidos
        }

        val productoPaquete = ProductoPaquete(paqueteDePrueba, listOf(), listOf())

        assertEquals(idPaqueteEsperado, productoPaquete.id)
    }

    @Test
    fun codigo_externo_paquete_corresponde_al_codigo_externo_del_paquete()
    {
        val codigoExternoEsperado = "código externo para prueba"
        val paqueteDePrueba = mockConDefaultAnswer(Paquete::class.java).also {
            doReturn(123456L).`when`(it).id
            doReturn(codigoExternoEsperado).`when`(it).codigoExterno
            doReturn("asdf").`when`(it).nombre
            doReturn(listOf(Paquete.FondoIncluido(1, "código externo fondo 1", Decimal.UNO))).`when`(it).fondosIncluidos
        }

        val productoPaquete = ProductoPaquete(paqueteDePrueba, listOf(), listOf())

        assertEquals(codigoExternoEsperado, productoPaquete.codigoExternoPaquete)
    }

    @Test
    fun nombre_corresponde_al_nombre_del_paquete()
    {
        val nombrePaqueteEsperado = "El nombre"
        val paqueteDePrueba = mockConDefaultAnswer(Paquete::class.java).also {
            doReturn(123456L).`when`(it).id
            doReturn("código externo paquete").`when`(it).codigoExterno
            doReturn(nombrePaqueteEsperado).`when`(it).nombre
            doReturn(listOf(Paquete.FondoIncluido(1, "código externo fondo 1", Decimal.UNO))).`when`(it).fondosIncluidos
        }

        val productoPaquete = ProductoPaquete(paqueteDePrueba, listOf(), listOf())

        assertEquals(nombrePaqueteEsperado, productoPaquete.nombre)
    }

    @Test
    fun ids_fondos_incluidos_corresponden_a_los_ids_de_fondos_incluidos_en_el_paquete()
    {
        val idsFondosIncluidosEsperados = LinkedHashSet(listOf(1L, 2L, 3L))
        val paqueteDePrueba = mockConDefaultAnswer(Paquete::class.java).also { paquete ->
            doReturn(123456L).`when`(paquete).id
            doReturn("código externo paquete").`when`(paquete).codigoExterno
            doReturn("asdf").`when`(paquete).nombre
            doReturn(idsFondosIncluidosEsperados.map { Paquete.FondoIncluido(it, "código externo fondo 1", Decimal.UNO) })
                .`when`(paquete)
                .fondosIncluidos
        }

        val productoPaquete = ProductoPaquete(paqueteDePrueba, listOf(), listOf())

        assertEquals(idsFondosIncluidosEsperados, productoPaquete.idsFondosIncluidos)
    }

    @Test
    fun cantidades_fondos_incluidos_corresonden_a_las_cantidades_de_fondos_incluidos_en_el_paquete()
    {
        val cantidadesFondosIncluidos = listOf(Decimal(1), Decimal(2), Decimal(3))
        val paqueteDePrueba = mockConDefaultAnswer(Paquete::class.java).also { paquete ->
            doReturn(123456L).`when`(paquete).id
            doReturn("código externo paquete").`when`(paquete).codigoExterno
            doReturn("asdf").`when`(paquete).nombre
            doReturn(cantidadesFondosIncluidos.mapIndexed { i: Int, cantidad: Decimal -> Paquete.FondoIncluido(i.toLong(), "código externo fondo $i", cantidad) })
                .`when`(paquete)
                .fondosIncluidos
        }

        val productoPaquete = ProductoPaquete(paqueteDePrueba, listOf(), listOf())

        assertEquals(cantidadesFondosIncluidos, productoPaquete.cantidadesFondosIncluidos)
    }
}