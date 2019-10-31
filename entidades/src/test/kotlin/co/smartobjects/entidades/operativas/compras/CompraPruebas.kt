package co.smartobjects.entidades.operativas.compras

import co.smartobjects.entidades.excepciones.EntidadConCampoFueraDeRango
import co.smartobjects.entidades.excepciones.EntidadConCampoVacio
import co.smartobjects.entidades.excepciones.EntidadMalInicializada
import co.smartobjects.entidades.operativas.EntidadTransaccional
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.FECHA_MINIMA_CREACION
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@DisplayName("Compra")
internal class CompraPruebas
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
                    "código externo fondo  $indice",
                    indice.toLong() * 3,
                    "un-uuid-de-dispositivo $indice",
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

        private val pagoPruebas1 = Pago(Decimal(1000), Pago.MetodoDePago.EFECTIVO, "12-3")

        private val pagoPruebas2 = Pago(Decimal(20000), Pago.MetodoDePago.TARJETA_CREDITO, "45-6")
    }

    @Test
    fun se_instancia_correctamente_con_creditos_fondo_y_creditos_paquete()
    {
        val creditoFondo1 = crearCreditoFondoSegunIndice(1)
        val creditoFondo2 = crearCreditoFondoSegunIndice(2)
        val creditoPaquete1 = crearCreditoPaqueteSegunIndice(10, 2)
        val creditoPaquete2 = crearCreditoPaqueteSegunIndice(20, 3)
        val entidad = Compra(
                1,
                "Usuario",
                listOf(creditoFondo1, creditoFondo2),
                listOf(creditoPaquete1, creditoPaquete2),
                listOf(pagoPruebas1),
                fechaActual
                            )

        val creditosEsperados = listOf(creditoFondo1, creditoFondo2) + creditoPaquete1.creditosFondos + creditoPaquete2.creditosFondos

        assertEquals(creditosEsperados.sortedBy { it.id }, entidad.creditos.sortedBy { it.id })
        assertEquals(creditosEsperados.sortedBy { it.id }, entidad.campoCreditos.valor.sortedBy { it.id })
        assertEquals(listOf(creditoPaquete1, creditoPaquete2), entidad.creditosPaquetes)
        assertEquals(listOf(creditoFondo1, creditoFondo2), entidad.creditosFondos)
        assertEquals(fechaActual, entidad.campoFechaDeRealizacion.valor)
        assertEquals("Usuario", entidad.campoNombreUsuario.valor)
        assertEquals(listOf(pagoPruebas1), entidad.campoPagos.valor)
        assertEquals(false, entidad.creacionTerminada)
        assertEquals(false, entidad.campoCreacionTerminada.valor)
    }

    @Test
    fun se_instancia_correctamente_con_creditos_fondo_pero_sin_creditos_paquete()
    {
        val creditoFondo1 = crearCreditoFondoSegunIndice(1)
        val creditoFondo2 = crearCreditoFondoSegunIndice(2)
        val entidad = Compra(
                1,
                "Usuario",
                listOf(creditoFondo1, creditoFondo2),
                listOf(),
                listOf(pagoPruebas1),
                fechaActual
                            )

        val creditosEsperados = listOf(creditoFondo1, creditoFondo2)

        assertEquals(creditosEsperados.sortedBy { it.id }, entidad.creditos.sortedBy { it.id })
        assertEquals(creditosEsperados.sortedBy { it.id }, entidad.campoCreditos.valor.sortedBy { it.id })
        assertEquals(listOf(), entidad.creditosPaquetes)
        assertEquals(listOf(creditoFondo1, creditoFondo2), entidad.creditosFondos)
        assertEquals(fechaActual, entidad.campoFechaDeRealizacion.valor)
        assertEquals("Usuario", entidad.campoNombreUsuario.valor)
        assertEquals(listOf(pagoPruebas1), entidad.campoPagos.valor)
        assertEquals(false, entidad.creacionTerminada)
        assertEquals(false, entidad.campoCreacionTerminada.valor)
    }

    @Test
    fun se_instancia_correctamente_sin_creditos_fondo_pero_con_creditos_paquete()
    {
        val creditoPaquete1 = crearCreditoPaqueteSegunIndice(10, 2)
        val creditoPaquete2 = crearCreditoPaqueteSegunIndice(20, 3)
        val entidad = Compra(
                1,
                "Usuario",
                listOf(),
                listOf(creditoPaquete1, creditoPaquete2),
                listOf(pagoPruebas1),
                fechaActual
                            )

        val creditosEsperados = creditoPaquete1.creditosFondos + creditoPaquete2.creditosFondos

        assertEquals(creditosEsperados.sortedBy { it.id }, entidad.creditos.sortedBy { it.id })
        assertEquals(creditosEsperados.sortedBy { it.id }, entidad.campoCreditos.valor.sortedBy { it.id })
        assertEquals(listOf(creditoPaquete1, creditoPaquete2), entidad.creditosPaquetes)
        assertEquals(listOf(), entidad.creditosFondos)
        assertEquals(fechaActual, entidad.campoFechaDeRealizacion.valor)
        assertEquals("Usuario", entidad.campoNombreUsuario.valor)
        assertEquals(listOf(pagoPruebas1), entidad.campoPagos.valor)
        assertEquals(false, entidad.creacionTerminada)
        assertEquals(false, entidad.campoCreacionTerminada.valor)
    }

    @Test
    fun instanciar_con_constructor_para_nuevo_credito_asigna_id_unico_segun_tiempo_en_milisegundos_uuid_aleatorio_y_nombre_de_usuario()
    {
        val creditoFondo1 = crearCreditoFondoSegunIndice(1)
        val creditoPaquete1 = crearCreditoPaqueteSegunIndice(10, 2)
        val entidad = Compra(
                1,
                "Usuario",
                listOf(creditoFondo1),
                listOf(creditoPaquete1),
                listOf(pagoPruebas1),
                fechaActual
                            )

        assertEquals(false, entidad.creacionTerminada)
        assertEquals(false, entidad.campoCreacionTerminada.valor)
        assertEquals("${entidad.tiempoCreacion}${EntidadTransaccional.SEPARADOR_COMPONENTES_ID}${entidad.nombreUsuario}${EntidadTransaccional.SEPARADOR_COMPONENTES_ID}${entidad.uuid}", entidad.id)
        // Se usa el tiempo actual
        assertTrue(entidad.tiempoCreacion <= ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO).toInstant().toEpochMilli())
        assertTrue(entidad.tiempoCreacion >= ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO).toInstant().toEpochMilli() - 1000)

        // Asigna un uuid aleatorio
        val otraEntidad = Compra(
                1,
                "Usuario",
                listOf(creditoFondo1),
                listOf(creditoPaquete1),
                listOf(pagoPruebas1),
                fechaActual
                                )
        // Tecnicamente esta prueba puede fallar si el uuid generado al azar colisiona, pero la probabilidad lo hace imposible en la practica siempre que el uuid se genere aleatoriamente
        assertNotEquals(entidad.uuid, otraEntidad.uuid)
    }

    @Test
    fun instanciar_con_constructor_para_credito_existente_usa_parametros_dados()
    {
        val creditoFondo1 = crearCreditoFondoSegunIndice(1)
        val creditoPaquete1 = crearCreditoPaqueteSegunIndice(10, 2)
        val usuario = "Usuario"
        val tiempo = 123456789L
        val uuid = UUID.randomUUID()
        val entidad = Compra(
                1,
                usuario,
                uuid,
                tiempo,
                true,
                listOf(creditoFondo1),
                listOf(creditoPaquete1),
                listOf(pagoPruebas1),
                fechaActual
                            )

        assertEquals(true, entidad.creacionTerminada)
        assertEquals(true, entidad.campoCreacionTerminada.valor)
        assertEquals(tiempo, entidad.tiempoCreacion)
        assertEquals(uuid, entidad.uuid)
        assertEquals("$tiempo${EntidadTransaccional.SEPARADOR_COMPONENTES_ID}$usuario${EntidadTransaccional.SEPARADOR_COMPONENTES_ID}$uuid", entidad.id)
    }

    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos_y_mantiene_id_y_campos_asociados()
    {
        val creditoFondo1 = crearCreditoFondoSegunIndice(1)
        val creditoPaquete1 = crearCreditoPaqueteSegunIndice(10, 2)
        val entidadInicial = Compra(
                1,
                "Usuario",
                listOf(creditoFondo1),
                listOf(creditoPaquete1),
                listOf(pagoPruebas1),
                fechaActual
                                   )
        val creditoFondo2 = crearCreditoFondoSegunIndice(2)
        val creditoPaquete2 = crearCreditoPaqueteSegunIndice(20, 3)
        val entidadCopiada = entidadInicial.copiar(
                1,
                true,
                listOf(creditoFondo2),
                listOf(creditoPaquete2),
                listOf(pagoPruebas2),
                fechaActual.plusDays(1)
                                                  )
        val entidadEsperada = Compra(
                1,
                "Usuario",
                entidadInicial.uuid,
                entidadInicial.tiempoCreacion,
                true,
                listOf(creditoFondo2),
                listOf(creditoPaquete2),
                listOf(pagoPruebas2),
                fechaActual.plusDays(1)
                                    )
        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun no_se_permite_instanciar_con_nombre_usuario_vacio()
    {
        val excepcion = assertThrows<EntidadConCampoVacio> {
            Compra(
                    1,
                    "",
                    listOf(crearCreditoFondoSegunIndice(1)),
                    listOf(),
                    listOf(pagoPruebas1),
                    fechaActual
                  )
        }

        assertEquals(EntidadTransaccional.Campos.NOMBRE_USUARIO, excepcion.nombreDelCampo)
        assertEquals(EntidadTransaccional.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun no_se_permite_instanciar_con_nombre_usuario_con_espacios_o_tabs()
    {
        val excepcion = assertThrows<EntidadConCampoVacio> {
            Compra(
                    1,
                    "              ",
                    listOf(crearCreditoFondoSegunIndice(1)),
                    listOf(),
                    listOf(pagoPruebas1),
                    fechaActual
                  )
        }

        assertEquals(EntidadTransaccional.Campos.NOMBRE_USUARIO, excepcion.nombreDelCampo)
        assertEquals(EntidadTransaccional.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun no_se_permite_instanciar_sin_creditos_fondo_o_creditos_paquete()
    {
        val excepcion = assertThrows<EntidadConCampoVacio> {
            Compra(
                    1,
                    "Usuario",
                    listOf(),
                    listOf(),
                    listOf(pagoPruebas1),
                    fechaActual
                  )
        }

        assertEquals(Compra.Campos.CREDITOS, excepcion.nombreDelCampo)
        assertEquals(Compra.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun no_se_permite_instanciar_sin_pagos()
    {
        val excepcion = assertThrows<EntidadConCampoVacio> {
            Compra(
                    1,
                    "Usuario",
                    listOf(crearCreditoFondoSegunIndice(1)),
                    listOf(),
                    listOf(),
                    fechaActual
                  )
        }

        assertEquals(Compra.Campos.PAGOS, excepcion.nombreDelCampo)
        assertEquals(Compra.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun el_id_de_la_zona_horaria_de_la_fecha_de_realizacion_es_UTC()
    {
        val zonaHorariaPorDefectoInvalida = "America/Bogota"
        val fechaRealizacion = ZonedDateTime.now(ZoneId.of(zonaHorariaPorDefectoInvalida)).plusDays(1)
        val excepcion = assertThrows<EntidadMalInicializada> {
            Compra(
                    1,
                    "Usuario",
                    listOf(crearCreditoFondoSegunIndice(1)),
                    listOf(),
                    listOf(pagoPruebas1),
                    fechaRealizacion
                  )
        }

        assertEquals(Compra.Campos.FECHA_REALIZACION, excepcion.nombreDelCampo)
        assertEquals(Compra.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        assertEquals(fechaRealizacion.toString(), excepcion.valorUsado)
        assertTrue(excepcion.cause is EntidadMalInicializada)
        assertEquals(zonaHorariaPorDefectoInvalida, (excepcion.cause as EntidadMalInicializada).valorUsado)
        assertEquals(Compra.Campos.FECHA_REALIZACION, (excepcion.cause as EntidadMalInicializada).nombreDelCampo)
        assertEquals(Compra.NOMBRE_ENTIDAD, (excepcion.cause as EntidadMalInicializada).nombreEntidad)
    }

    @Test
    fun la_fecha_de_validez_hasta_es_siempre_mayor_o_igual_a_la_fecha_minima()
    {
        val fechaAUsar = FECHA_MINIMA_CREACION.minusSeconds(1)
        val excepcion = assertThrows<EntidadConCampoFueraDeRango> {
            Compra(
                    1,
                    "Usuario",
                    listOf(crearCreditoFondoSegunIndice(1)),
                    listOf(),
                    listOf(pagoPruebas1),
                    fechaAUsar
                  )
        }

        assertEquals(Compra.Campos.FECHA_REALIZACION, excepcion.nombreDelCampo)
        assertEquals(Compra.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        assertEquals(fechaAUsar.toString(), excepcion.valorUsado)
        assertEquals(FECHA_MINIMA_CREACION.toString(), excepcion.valorDelLimite)
        assertEquals(EntidadConCampoFueraDeRango.Limite.INFERIOR, excepcion.limiteSobrepasado)
    }
}