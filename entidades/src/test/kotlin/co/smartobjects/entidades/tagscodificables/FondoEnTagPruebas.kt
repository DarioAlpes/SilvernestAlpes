package co.smartobjects.entidades.tagscodificables

import co.smartobjects.entidades.operativas.compras.CreditoFondo
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.threeten.bp.ZonedDateTime
import java.nio.ByteBuffer
import kotlin.test.assertEquals


@DisplayName("FondoEnTag")
internal class FondoEnTagPruebas
{
    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val entidadInicial = FondoEnTag(5, Decimal(10))
        val entidadEsperada = FondoEnTag(11, Decimal(20))

        val entidadCopiada = entidadInicial.copiar(Decimal(20), 11)

        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Nested
    inner class Instanciacion
    {
        @Test
        fun funciona_desde_un_credito_fondo()
        {
            val fechaActual = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
            val cantidadCreditoFondo = Decimal(10)
            val idFondoComprado: Long = 6
            val creditoFondo = CreditoFondo(
                    1,
                    null,
                    cantidadCreditoFondo,
                    Decimal(1000),
                    Decimal(150),
                    fechaActual,
                    fechaActual,
                    false,
                    "Taquilla",
                    "Un usuario",
                    2,
                    idFondoComprado,
                    "código externo fondo",
                    3,
                    "un-uuid-de-dispositivo",
                    4,
                    5
                                           )

            val entidad = FondoEnTag(creditoFondo)

            assertEquals(idFondoComprado, entidad.idFondoComprado)
            assertEquals(Decimal(10), entidad.cantidad)
        }
    }

    @Nested
    inner class EscrituraEnTag
    {
        private val entidadEsperada = FondoEnTag(-8608480567731124088, Decimal(1.1))


        @Test
        fun el_tamaño_en_bytes_es_la_suma_del_tamaño_en_bytes_de_los_campos_que_se_escriben()
        {
            val tamañoBytesDeCantidad = 8
            val tamañoBytesDeIdFondoComprado = 8
            val tamañoBytesEsperado = tamañoBytesDeCantidad + tamañoBytesDeIdFondoComprado

            assertEquals(tamañoBytesEsperado, FondoEnTag.TAMAÑO_EN_BYTES)
            assertEquals(FondoEnTag.TAMAÑO_EN_BYTES, entidadEsperada.tamañoTotalEnBytes)
        }

        @Test
        fun se_puede_seriailzar_y_deserializar_correctamente_desde_un_buffer_de_bytes()
        {
            val buffer = ByteBuffer.allocate(FondoEnTag.TAMAÑO_EN_BYTES)
            entidadEsperada.escribirComoBytes(buffer)

            val arregloConDatos = buffer.array()

            val entidadDeserializada = FondoEnTag(ByteBuffer.wrap(arregloConDatos))

            assertEquals(entidadEsperada, entidadDeserializada)
        }
    }
}