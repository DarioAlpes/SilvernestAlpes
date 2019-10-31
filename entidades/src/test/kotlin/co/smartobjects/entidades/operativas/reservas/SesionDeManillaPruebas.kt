package co.smartobjects.entidades.operativas.reservas

import co.smartobjects.entidades.excepciones.EntidadConCampoVacio
import co.smartobjects.entidades.excepciones.EntidadMalInicializada
import co.smartobjects.entidades.excepciones.RelacionEntreCamposInvalida
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DisplayName("SesionDeManilla")
class SesionDeManillaPruebas
{
    private val timestampHoy = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)

    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val entidadInicial =
                SesionDeManilla(
                        1,
                        null,
                        1,
                        byteArrayOf(0),
                        timestampHoy,
                        timestampHoy.plusDays(1),
                        setOf<Long>(1, 2, 3)
                               )
        val entidadEsperada =
                SesionDeManilla(
                        2,
                        1,
                        2,
                        byteArrayOf(0, 1, 2),
                        timestampHoy.plusDays(2),
                        timestampHoy.plusDays(4),
                        setOf<Long>(7, 8, 9)
                               )

        val entidadCopiada =
                entidadInicial.copiar(
                        2,
                        1,
                        2,
                        byteArrayOf(0, 1, 2),
                        timestampHoy.plusDays(2),
                        timestampHoy.plusDays(4),
                        setOf<Long>(7, 8, 9)
                                     )

        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Nested
    inner class Instanciacion
    {
        @Test
        fun campos_quedan_con_valores_correctos()
        {
            val uuid = byteArrayOf(0)
            val fechaActual = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
            val idsCreditosInicialmenteCodificados = setOf<Long>(1, 2, 3)
            val entidad = SesionDeManilla(1, null, 1, uuid, fechaActual, fechaActual, idsCreditosInicialmenteCodificados)

            assertEquals(uuid, entidad.campoUuidTag!!.valor)
            assertEquals(uuid, entidad.uuidTag)
            assertEquals(fechaActual, entidad.campoFechaActivacion!!.valor)
            assertEquals(fechaActual, entidad.fechaActivacion)
            assertEquals(fechaActual, entidad.campoFechaDesactivacion!!.valor)
            assertEquals(fechaActual, entidad.fechaDesactivacion)
            assertEquals(idsCreditosInicialmenteCodificados, entidad.campoIdsCreditosCodificados.valor)
        }

        @Nested
        inner class UUID
        {
            @Test
            fun permite_ser_nulo()
            {
                SesionDeManilla(
                        1,
                        null,
                        1,
                        null,
                        timestampHoy,
                        timestampHoy.plusDays(1),
                        setOf<Long>(1, 2, 3)
                               )
            }

            @Test
            fun si_no_es_nulo_no_puede_estar_vacio()
            {
                val excepcion = Assertions.assertThrows(EntidadConCampoVacio::class.java, {
                    SesionDeManilla(
                            1,
                            null,
                            1,
                            byteArrayOf(),
                            timestampHoy,
                            timestampHoy.plusDays(1),
                            setOf<Long>(1, 2, 3)
                                   )
                })

                assertEquals(SesionDeManilla.Campos.UUID_TAG, excepcion.nombreDelCampo)
                assertEquals(SesionDeManilla.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
            }
        }

        @Nested
        inner class FechasDeActivacionYDesactivacion
        {
            @Test
            fun ambas_nulas_funciona_correctamente()
            {
                val uuid = byteArrayOf(0)
                val idsCreditosInicialmenteCodificados = setOf<Long>(1, 2, 3)
                val entidad = SesionDeManilla(1, null, 1, uuid, null, null, idsCreditosInicialmenteCodificados)

                assertEquals(null, entidad.campoFechaActivacion?.valor)
                assertEquals(null, entidad.fechaActivacion)
                assertEquals(null, entidad.campoFechaDesactivacion?.valor)
                assertEquals(null, entidad.fechaDesactivacion)
            }

            @Test
            fun el_id_de_la_zona_horaria_de_la_fecha_de_activacion_es_la_zona_horaria_por_defecto()
            {
                val zonaHorariaPorDefectoInvalida = "America/Bogota"
                val fechaInvalida = ZonedDateTime.now(ZoneId.of(zonaHorariaPorDefectoInvalida))
                val excepcion = Assertions.assertThrows(EntidadMalInicializada::class.java, {
                    SesionDeManilla(
                            1,
                            null,
                            1,
                            byteArrayOf(0),
                            fechaInvalida,
                            timestampHoy,
                            setOf<Long>(1, 2, 3)
                                   )
                })

                assertEquals(SesionDeManilla.Campos.FECHA_ACTIVACION, excepcion.nombreDelCampo)
                assertEquals(SesionDeManilla.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                assertEquals(fechaInvalida.toString(), excepcion.valorUsado)
                assertTrue(excepcion.cause is EntidadMalInicializada)
                assertEquals(zonaHorariaPorDefectoInvalida, (excepcion.cause as EntidadMalInicializada).valorUsado)
                assertEquals(SesionDeManilla.Campos.FECHA_ACTIVACION, (excepcion.cause as EntidadMalInicializada).nombreDelCampo)
                assertEquals(SesionDeManilla.NOMBRE_ENTIDAD, (excepcion.cause as EntidadMalInicializada).nombreEntidad)
            }

            @Test
            fun el_id_de_la_zona_horaria_de_la_fecha_de_desactivacion_es_la_zona_horaria_por_defecto()
            {
                val zonaHorariaPorDefectoInvalida = "America/Bogota"
                val fechaInvalida = ZonedDateTime.now(ZoneId.of(zonaHorariaPorDefectoInvalida)).plusDays(5)
                val excepcion = Assertions.assertThrows(EntidadMalInicializada::class.java, {
                    SesionDeManilla(
                            1,
                            null,
                            1,
                            byteArrayOf(0),
                            timestampHoy,
                            fechaInvalida,
                            setOf<Long>(1, 2, 3)
                                   )
                })

                assertEquals(SesionDeManilla.Campos.FECHA_DESACTIVACION, excepcion.nombreDelCampo)
                assertEquals(SesionDeManilla.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                assertEquals(fechaInvalida.toString(), excepcion.valorUsado)
                assertTrue(excepcion.cause is EntidadMalInicializada)
                assertEquals(zonaHorariaPorDefectoInvalida, (excepcion.cause as EntidadMalInicializada).valorUsado)
                assertEquals(SesionDeManilla.Campos.FECHA_DESACTIVACION, (excepcion.cause as EntidadMalInicializada).nombreDelCampo)
                assertEquals(SesionDeManilla.NOMBRE_ENTIDAD, (excepcion.cause as EntidadMalInicializada).nombreEntidad)
            }

            @Test
            fun la_fecha_de_activacion_es_siempre_menor_o_igual_a_la_fecha_de_activacion()
            {
                val fechaDeDesactivacion = timestampHoy.minusNanos(1)
                val excepcion = Assertions.assertThrows(RelacionEntreCamposInvalida::class.java, {
                    SesionDeManilla(
                            1,
                            null,
                            1,
                            byteArrayOf(0),
                            timestampHoy,
                            fechaDeDesactivacion,
                            setOf<Long>(1, 2, 3)
                                   )
                })

                assertEquals(SesionDeManilla.Campos.FECHA_ACTIVACION, excepcion.nombreDelCampo)
                assertEquals(SesionDeManilla.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                assertEquals(SesionDeManilla.Campos.FECHA_ACTIVACION, excepcion.nombreDelCampoIzquierdo)
                assertEquals(SesionDeManilla.Campos.FECHA_DESACTIVACION, excepcion.nombreDelCampoDerecho)
                assertEquals(timestampHoy.toString(), excepcion.valorUsadoPorCampoIzquierdo)
                assertEquals(fechaDeDesactivacion.toString(), excepcion.valorUsadoPorCampoDerecho)
                assertEquals(RelacionEntreCamposInvalida.Relacion.MENOR, excepcion.relacionViolada)
            }

            @Test
            fun si_hay_fecha_de_desactivacion_debe_haber_fecha_de_activacion()
            {
                val excepcion = Assertions.assertThrows(RelacionEntreCamposInvalida::class.java, {
                    SesionDeManilla(
                            1,
                            null,
                            1,
                            byteArrayOf(0),
                            null,
                            timestampHoy,
                            setOf<Long>(1, 2, 3)
                                   )
                })

                assertEquals(SesionDeManilla.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                assertEquals(SesionDeManilla.Campos.FECHA_DESACTIVACION, excepcion.nombreDelCampo)
                assertEquals(SesionDeManilla.Campos.FECHA_DESACTIVACION, excepcion.nombreDelCampoIzquierdo)
                assertEquals(SesionDeManilla.Campos.FECHA_ACTIVACION, excepcion.nombreDelCampoDerecho)
                assertEquals(timestampHoy.toString(), excepcion.valorUsadoPorCampoIzquierdo)
                assertEquals("null", excepcion.valorUsadoPorCampoDerecho)
                assertEquals(RelacionEntreCamposInvalida.Relacion.NULO_SI, excepcion.relacionViolada)
            }
        }

        @Test
        fun no_se_permite_instanciar_sin_ids_creditos_codificados()
        {
            val excepcion = Assertions.assertThrows(EntidadConCampoVacio::class.java, {
                SesionDeManilla(
                        1,
                        null,
                        1,
                        byteArrayOf(0),
                        timestampHoy,
                        timestampHoy.plusDays(1),
                        setOf()
                               )
            })

            assertEquals(SesionDeManilla.Campos.IDS_CREDITOS_CODIFICADOS, excepcion.nombreDelCampo)
            assertEquals(SesionDeManilla.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        }
    }
}