package co.smartobjects.persistencia.operativas.compras

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.fondos.Dinero
import co.smartobjects.entidades.fondos.Fondo
import co.smartobjects.entidades.fondos.Paquete
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.entidades.operativas.EntidadTransaccional
import co.smartobjects.entidades.operativas.compras.*
import co.smartobjects.entidades.operativas.reservas.Reserva
import co.smartobjects.entidades.operativas.reservas.SesionDeManilla
import co.smartobjects.persistencia.TestConMultiplesDAO
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorio
import co.smartobjects.persistencia.excepciones.EntidadNoExiste
import co.smartobjects.persistencia.excepciones.EsquemaNoExiste
import co.smartobjects.persistencia.operativas.ClasePruebasEntidadesTransaccionales
import co.smartobjects.persistencia.operativas.ordenes.RepositorioOrdenes
import co.smartobjects.persistencia.operativas.ordenes.RepositorioOrdenesSQL
import co.smartobjects.persistencia.operativas.reservas.RepositorioDeSesionDeManilla
import co.smartobjects.persistencia.operativas.reservas.RepositorioDeSesionDeManillaSQL
import co.smartobjects.persistencia.operativas.reservas.RepositorioReservas
import co.smartobjects.persistencia.operativas.reservas.RepositorioReservasSQL
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.threeten.bp.LocalTime
import org.threeten.bp.ZonedDateTime
import java.util.*
import kotlin.test.assertEquals


@DisplayName("RepositorioCreditosDeUnaPersona")
internal class RepositorioCreditosDeUnaPersonaPruebas : ClasePruebasEntidadesTransaccionales()
{
    private val repositorioSesionesDeManilla: RepositorioDeSesionDeManilla by lazy { RepositorioDeSesionDeManillaSQL(configuracionRepositorios) }
    private val repositorioOrdenes: RepositorioOrdenes by lazy { RepositorioOrdenesSQL(configuracionRepositorios) }
    private val repositorioReservas: RepositorioReservas by lazy { RepositorioReservasSQL(configuracionRepositorios) }
    private val repositorioCompras: RepositorioCompras by lazy { RepositorioComprasSQL(configuracionRepositorios) }
    private val repositorio: RepositorioCreditosDeUnaPersona by lazy { RepositorioCreditosDeUnaPersonaSQL(configuracionRepositorios) }

    override val creadoresRepositoriosUsados: List<CreadorRepositorio<*>> by lazy {
        super.creadoresRepositoriosUsados +
        listOf<CreadorRepositorio<*>>(repositorioCompras, repositorioReservas, repositorioSesionesDeManilla, repositorioOrdenes)
    }


    private val paqueteCreado: Paquete by lazy {
        repositorioPaquetes.crear(
                idClientePruebas,
                Paquete(
                        idClientePruebas,
                        null,
                        "Paquete de prueba",
                        "Descripción",
                        true,
                        fechaHoraActual.with(LocalTime.MIDNIGHT),
                        fechaHoraActual.plusDays(1).with(LocalTime.MAX),
                        listOf(Paquete.FondoIncluido(fondoCreado.id!!, "código externo incluido", Decimal.UNO)),
                        "código externo paquete"
                       )
                                 )
    }

    private val fondoCreado: Fondo<*> by lazy {
        repositorioMonedas.crear(idClientePruebas, Dinero(
                idClientePruebas,
                null,
                "Dinero pruebas",
                true,
                false,
                false,
                Precio(Decimal.UNO, impuestoCreado.id!!),
                "el código externo de prueba dinero"
                                                         ))
    }

    private val creditoFondoConNulos: CreditoFondo by lazy {
        CreditoFondo(
                idClientePruebas,
                null,
                Decimal(10),
                Decimal(1000),
                Decimal(150),
                fechaHoraActual.with(LocalTime.MIDNIGHT),
                fechaHoraActual.with(LocalTime.MAX),
                false,
                "Taquilla",
                usuarioCreado.datosUsuario.usuario,
                personaCreada.id!!,
                fondoCreado.id!!,
                "el código externo de prueba",
                impuestoCreado.id!!,
                "un-uuid",
                null,
                null
                    )
    }

    private val creditoFondoSinNulos: CreditoFondo by lazy {
        creditoFondoConNulos.copiar(idUbicacionCompra = ubicacionCreada.id!!, idGrupoClientesPersona = grupoDeClientesCreado.id!!)
    }

    private val pagoPruebas: Pago by lazy { Pago(Decimal(1000), Pago.MetodoDePago.EFECTIVO, "12-3") }


    private fun darInstanciaEntidadValida(): Compra
    {
        return Compra(
                idClientePruebas,
                usuarioCreado.datosUsuario.usuario,
                listOf(creditoFondoConNulos),
                listOf(),
                listOf(pagoPruebas),
                fechaHoraActual
                     )
    }

    private fun darInstanciaEntidadValidaConMultiplesCreditosConMultiplesPersonas(indice: Int): Compra
    {
        return Compra(
                idClientePruebas,
                usuarioCreado.datosUsuario.usuario,
                UUID.randomUUID(),
                ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO).toInstant().toEpochMilli() - indice * 100,
                false,
                listOf(creditoFondoConNulos.copiar(idPersonaDueña = personaCreada3.id!!), creditoFondoSinNulos.copiar(idPersonaDueña = personaCreada2.id!!)),
                listOf(
                        CreditoPaquete(
                                paqueteCreado.id!!,
                                "código externo paquete",
                                listOf(
                                        creditoFondoConNulos.copiar(idPersonaDueña = personaCreada.id!!),
                                        creditoFondoSinNulos.copiar(idPersonaDueña = personaCreada.id!!)
                                      )
                                      ),
                        CreditoPaquete(
                                paqueteCreado.id!!,
                                "código externo paquete",
                                listOf(
                                        creditoFondoConNulos.copiar(idPersonaDueña = personaCreada4.id!!),
                                        creditoFondoSinNulos.copiar(idPersonaDueña = personaCreada4.id!!)
                                      )
                                      )
                      ),
                listOf(pagoPruebas.copiar(numeroDeTransaccionPOS = pagoPruebas.numeroDeTransaccionPOS + indice)),
                fechaHoraActual
                     )
    }


    @Nested
    inner class Buscar
    {
        @Nested
        inner class NoConsumidosValidosParaDia
        {
            private val resultadoVacio by lazy {
                CreditosDeUnaPersona(idClientePruebas, personaCreada.id!!, emptyList(), emptyList())
            }

            @TestConMultiplesDAO
            fun sin_entidades_retorna_listas_vacias()
            {
                val parametros = FiltroCreditosPersona.NoConsumidosValidosParaDia(personaCreada.id!!, fechaHoraActual)
                val entidadConsultada = repositorio.buscarSegunParametros(idClientePruebas, parametros)

                assertEquals(resultadoVacio, entidadConsultada)
            }

            @TestConMultiplesDAO
            fun compras_sin_marcar_como_finalizadas_retorna_listas_vacias()
            {
                val parametros = FiltroCreditosPersona.NoConsumidosValidosParaDia(personaCreada.id!!, fechaHoraActual)
                (1..3).map {
                    repositorioCompras.crear(
                            idClientePruebas,
                            darInstanciaEntidadValidaConMultiplesCreditosConMultiplesPersonas(it)
                                            )
                }
                val entidadConsultada = repositorio.buscarSegunParametros(idClientePruebas, parametros)

                assertEquals(resultadoVacio, entidadConsultada)
            }

            @Suppress("NOTHING_TO_INLINE")
            @Nested
            inner class ComprasMarcadasFinalizadas
            {
                private fun crearCompraYMarcarComoFinalizada(compra: Compra): Compra
                {
                    val entidadCreada = repositorioCompras.crear(idClientePruebas, compra)

                    val nuevoValor = EntidadTransaccional.CampoCreacionTerminada<Compra>(true)
                    repositorioCompras.actualizarCamposIndividuales(
                            idClientePruebas,
                            entidadCreada.id,
                            mapOf<String, CampoModificable<Compra, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                                   )

                    return entidadCreada.copiar(creacionTerminada = true)
                }

                private fun darCompraVariasPersonasConCreditosConteniendoDia(dia: ZonedDateTime, spreadDias: Long): Compra
                {
                    assert(spreadDias >= 0)

                    val inicioDia = dia.with(LocalTime.MIDNIGHT)
                    val finalDia = dia.with(LocalTime.MAX)

                    val creditoConNulosEnDia =
                            creditoFondoConNulos.copiar(
                                    validoDesde = inicioDia.minusDays(spreadDias),
                                    validoHasta = finalDia.plusDays(spreadDias)
                                                       )

                    val creditoSinNulosEnDia =
                            creditoFondoSinNulos.copiar(
                                    validoDesde = inicioDia.minusDays(spreadDias),
                                    validoHasta = finalDia.plusDays(spreadDias)
                                                       )

                    return Compra(
                            idClientePruebas,
                            usuarioCreado.datosUsuario.usuario,
                            UUID.randomUUID(),
                            creditoConNulosEnDia.validoDesde!!.toInstant().toEpochMilli() - 1000,
                            false,
                            listOf(
                                    creditoConNulosEnDia.copiar(idPersonaDueña = personaCreada.id!!),
                                    creditoConNulosEnDia.copiar(idPersonaDueña = personaCreada2.id!!),
                                    creditoSinNulosEnDia.copiar(idPersonaDueña = personaCreada3.id!!)
                                  ),
                            listOf(
                                    CreditoPaquete(
                                            paqueteCreado.id!!,
                                            "código externo paquete",
                                            listOf(
                                                    creditoConNulosEnDia.copiar(idPersonaDueña = personaCreada.id!!),
                                                    creditoSinNulosEnDia.copiar(idPersonaDueña = personaCreada.id!!)
                                                  )
                                                  ),
                                    CreditoPaquete(
                                            paqueteCreado.id!!,
                                            "código externo paquete",
                                            listOf(
                                                    creditoConNulosEnDia.copiar(idPersonaDueña = personaCreada4.id!!),
                                                    creditoSinNulosEnDia.copiar(idPersonaDueña = personaCreada4.id!!)
                                                  )
                                                  )
                                  ),
                            listOf(pagoPruebas.copiar(numeroDeTransaccionPOS = UUID.randomUUID().toString())),
                            creditoConNulosEnDia.validoDesde!!
                                 )
                }

                private fun crearComprasEnDiferentesDias(fechaHoraReferencia: ZonedDateTime, spreadDias: Long = 0): List<Compra>
                {
                    return listOf(-1, 0, 1).map {
                        val fecha = fechaHoraReferencia.plusDays(it.toLong() * spreadDias)
                        crearCompraYMarcarComoFinalizada(darCompraVariasPersonasConCreditosConteniendoDia(fecha, spreadDias))
                    }
                }

                private inline fun List<Compra>.filtrarComprasPorPersonaYFecha(idPersona: Long, fecha: ZonedDateTime): List<Compra>
                {
                    return filter {
                        it.creditos.any {
                            it.idPersonaDueña == idPersona
                            && it.validoDesde!! <= fecha && fecha <= it.validoHasta!!
                        }
                    }
                }

                private inline fun List<Compra>.extraerCreditosNoConsumidosDePersona(idPersona: Long): CreditosDeUnaPersona?
                {
                    return map {
                        val creditosFondo = it.creditosFondos.filter { it.idPersonaDueña == idPersona && !it.consumido }
                        val creditosPaquete =
                                it.creditosPaquetes
                                    .filter { it.creditosFondos.any { it.idPersonaDueña == idPersona && !it.consumido } }
                                    .map {
                                        val creditosFondosEnPaqueteFiltrados =
                                                it.creditosFondos.filter { it.idPersonaDueña == idPersona && !it.consumido }
                                        it.copiar(creditosFondos = creditosFondosEnPaqueteFiltrados)
                                    }

                        CreditosDeUnaPersona(idClientePruebas, idPersona, creditosFondo, creditosPaquete)
                    }.reduce { acc, creditosDeUnaPersona ->
                        acc.copiar(
                                creditosFondos = acc.creditosFondos + creditosDeUnaPersona.creditosFondos,
                                creditosPaquetes = acc.creditosPaquetes + creditosDeUnaPersona.creditosPaquetes
                                  )
                    }
                }


                @TestConMultiplesDAO
                fun para_creditos_con_duracion_de_un_dia_retorna_entidad_correcta()
                {
                    val parametros = FiltroCreditosPersona.NoConsumidosValidosParaDia(personaCreada.id!!, fechaHoraActual)

                    val entidadesPrueba = crearComprasEnDiferentesDias(fechaHoraActual)
                    val entidadEsperada =
                            entidadesPrueba
                                .filtrarComprasPorPersonaYFecha(personaCreada.id!!, fechaHoraActual)
                                .extraerCreditosNoConsumidosDePersona(personaCreada.id!!)

                    val entidadConsultada = repositorio.buscarSegunParametros(idClientePruebas, parametros)

                    assertEquals(entidadEsperada, entidadConsultada)
                }

                @TestConMultiplesDAO
                fun para_creditos_con_duracion_superior_a_un_dia_retorna_entidad_correcta()
                {
                    val parametros = FiltroCreditosPersona.NoConsumidosValidosParaDia(personaCreada.id!!, fechaHoraActual)

                    val entidadesPrueba = crearComprasEnDiferentesDias(fechaHoraActual, 2)
                    val entidadEsperada =
                            entidadesPrueba
                                .filtrarComprasPorPersonaYFecha(personaCreada.id!!, fechaHoraActual)
                                .extraerCreditosNoConsumidosDePersona(personaCreada.id!!)

                    val entidadConsultada = repositorio.buscarSegunParametros(idClientePruebas, parametros)

                    assertEquals(entidadEsperada, entidadConsultada)
                }

                @TestConMultiplesDAO
                fun para_creditos_fuera_del_rango_retorna_listas_vacias()
                {
                    val parametros = FiltroCreditosPersona.NoConsumidosValidosParaDia(personaCreada.id!!, fechaHoraActual)

                    val entidadACrear = darInstanciaEntidadValida().run {
                        copiar(
                                creditosPaquetes = emptyList(),
                                creditosFondos = listOf(
                                        creditoFondoConNulos.copiar(validoDesde = fechaHoraActual.minusDays(2), validoHasta = fechaHoraActual.minusDays(1)),
                                        creditoFondoConNulos.copiar(validoDesde = fechaHoraActual.plusDays(1), validoHasta = fechaHoraActual.plusDays(2))
                                                       )
                              )
                    }

                    crearCompraYMarcarComoFinalizada(entidadACrear)

                    val entidadConsultada = repositorio.buscarSegunParametros(idClientePruebas, parametros)

                    assertEquals(resultadoVacio, entidadConsultada)
                }

                @TestConMultiplesDAO
                fun para_credito_sin_valido_desde_y_con_valido_hasta_mayor_a_la_fecha_retorna_entidad_correcta()
                {
                    val parametros = FiltroCreditosPersona.NoConsumidosValidosParaDia(personaCreada.id!!, fechaHoraActual)

                    val entidadACrear = darInstanciaEntidadValida().run {
                        copiar(
                                creditosPaquetes = emptyList(),
                                creditosFondos = listOf(
                                        creditoFondoConNulos.copiar(validoDesde = null, validoHasta = fechaHoraActual.plusDays(2))
                                                       )
                              )
                    }

                    val entidadCreada = crearCompraYMarcarComoFinalizada(entidadACrear)

                    val entidadEsperada =
                            CreditosDeUnaPersona(
                                    idClientePruebas,
                                    personaCreada.id!!,
                                    entidadCreada.creditosFondos,
                                    entidadCreada.creditosPaquetes
                                                )

                    val entidadConsultada = repositorio.buscarSegunParametros(idClientePruebas, parametros)

                    assertEquals(entidadEsperada, entidadConsultada)
                }

                @TestConMultiplesDAO
                fun para_credito_sin_valido_desde_y_con_valido_hasta_menor_a_la_fecha_retorna_listas_vacias()
                {
                    val parametros = FiltroCreditosPersona.NoConsumidosValidosParaDia(personaCreada.id!!, fechaHoraActual)

                    val entidadACrear = darInstanciaEntidadValida().run {
                        copiar(
                                creditosPaquetes = emptyList(),
                                creditosFondos = listOf(
                                        creditoFondoConNulos.copiar(validoDesde = null, validoHasta = fechaHoraActual.minusDays(2))
                                                       )
                              )
                    }

                    crearCompraYMarcarComoFinalizada(entidadACrear)

                    val entidadConsultada = repositorio.buscarSegunParametros(idClientePruebas, parametros)

                    assertEquals(resultadoVacio, entidadConsultada)
                }

                @TestConMultiplesDAO
                fun para_credito_con_valido_desde_menor_a_la_fecha_y_sin_valido_hasta_retorna_entidad_correcta()
                {
                    val parametros = FiltroCreditosPersona.NoConsumidosValidosParaDia(personaCreada.id!!, fechaHoraActual)

                    val entidadACrear = darInstanciaEntidadValida().run {
                        copiar(
                                creditosPaquetes = emptyList(),
                                creditosFondos = listOf(
                                        creditoFondoConNulos.copiar(validoDesde = fechaHoraActual.minusDays(2), validoHasta = null)
                                                       )
                              )
                    }

                    val entidadCreada = crearCompraYMarcarComoFinalizada(entidadACrear)

                    val entidadEsperada =
                            CreditosDeUnaPersona(
                                    idClientePruebas,
                                    personaCreada.id!!,
                                    entidadCreada.creditosFondos,
                                    entidadCreada.creditosPaquetes
                                                )

                    val entidadConsultada = repositorio.buscarSegunParametros(idClientePruebas, parametros)

                    assertEquals(entidadEsperada, entidadConsultada)
                }

                @TestConMultiplesDAO
                fun para_credito_con_valido_desde_mayor_a_la_fecha_y_sin_valido_hasta_retorna_listas_vacias()
                {
                    val parametros = FiltroCreditosPersona.NoConsumidosValidosParaDia(personaCreada.id!!, fechaHoraActual)

                    val entidadACrear = darInstanciaEntidadValida().run {
                        copiar(
                                creditosPaquetes = emptyList(),
                                creditosFondos = listOf(
                                        creditoFondoConNulos.copiar(validoDesde = fechaHoraActual.plusDays(2), validoHasta = null)
                                                       )
                              )
                    }

                    crearCompraYMarcarComoFinalizada(entidadACrear)

                    val entidadConsultada = repositorio.buscarSegunParametros(idClientePruebas, parametros)

                    assertEquals(resultadoVacio, entidadConsultada)
                }

                @TestConMultiplesDAO
                fun para_credito_sin_valido_desde_y_sin_valido_hasta_retorna_entidad_correcta()
                {
                    val parametros = FiltroCreditosPersona.NoConsumidosValidosParaDia(personaCreada.id!!, fechaHoraActual)

                    val entidadACrear = darInstanciaEntidadValida().run {
                        copiar(
                                creditosPaquetes = emptyList(),
                                creditosFondos = listOf(
                                        creditoFondoConNulos.copiar(validoDesde = null, validoHasta = null)
                                                       )
                              )
                    }

                    val entidadCreada = crearCompraYMarcarComoFinalizada(entidadACrear)

                    val entidadEsperada =
                            CreditosDeUnaPersona(
                                    idClientePruebas,
                                    personaCreada.id!!,
                                    entidadCreada.creditosFondos,
                                    entidadCreada.creditosPaquetes
                                                )

                    val entidadConsultada = repositorio.buscarSegunParametros(idClientePruebas, parametros)

                    assertEquals(entidadEsperada, entidadConsultada)
                }

                @Nested
                inner class ConsumiendoTodosLosCreditosEnReserva
                {
                    private fun darReservaACrearConsumiendoTodosLosCreditos(compras: List<Compra>): Reserva
                    {
                        val idsPersonas = compras.flatMap { it.creditos.map { it.idPersonaDueña } }.distinct()
                        val idsCreditosPorPersona = idsPersonas.associateBy({ it }, { mutableSetOf<Long>() })

                        compras.forEach {
                            it.creditos.forEach {
                                idsCreditosPorPersona[it.idPersonaDueña]!!.add(it.id!!)
                            }
                        }

                        return Reserva(
                                idClientePruebas,
                                usuarioCreado.datosUsuario.usuario,
                                idsCreditosPorPersona.map {
                                    SesionDeManilla(idClientePruebas, null, it.key, null, null, null, it.value)
                                }
                                      )
                    }

                    @TestConMultiplesDAO
                    fun retorna_listas_vacias()
                    {
                        val parametros = FiltroCreditosPersona.NoConsumidosValidosParaDia(personaCreada.id!!, fechaHoraActual)

                        val entidadesPrueba = crearComprasEnDiferentesDias(fechaHoraActual)

                        repositorioReservas.crear(idClientePruebas, darReservaACrearConsumiendoTodosLosCreditos(entidadesPrueba))

                        val entidadConsultada = repositorio.buscarSegunParametros(idClientePruebas, parametros)

                        assertEquals(resultadoVacio, entidadConsultada)
                    }

                    @TestConMultiplesDAO
                    fun menos_un_credito_fondo_en_reserva_retorna_solo_ese_credito()
                    {
                        val parametros = FiltroCreditosPersona.NoConsumidosValidosParaDia(personaCreada.id!!, fechaHoraActual)
                        val entidadesPrueba = crearComprasEnDiferentesDias(fechaHoraActual)
                        val idCreditoFondoNoConsumido = entidadesPrueba.first().creditosFondos.first().id
                        val comprasSinPrimero =
                                entidadesPrueba
                                    .map {
                                        it.copiar(
                                                creditosFondos = it.creditosFondos.filter {
                                                    it.id != idCreditoFondoNoConsumido
                                                }
                                                 )
                                    }

                        repositorioReservas.crear(idClientePruebas, darReservaACrearConsumiendoTodosLosCreditos(comprasSinPrimero))

                        val entidadEsperada =
                                entidadesPrueba
                                    .map {
                                        it.copiar(
                                                creditosFondos = it.creditosFondos.map { it.copiar(consumido = it.id != idCreditoFondoNoConsumido) },
                                                creditosPaquetes = it.creditosPaquetes.map { it.copiar(creditosFondos = it.creditosFondos.map { it.copiar(consumido = true) }) }
                                                 )
                                    }
                                    .filtrarComprasPorPersonaYFecha(personaCreada.id!!, fechaHoraActual)
                                    .extraerCreditosNoConsumidosDePersona(personaCreada.id!!)

                        val entidadConsultada = repositorio.buscarSegunParametros(idClientePruebas, parametros)

                        assertEquals(entidadEsperada, entidadConsultada)
                    }

                    @TestConMultiplesDAO
                    fun menos_un_credito_paquete_en_reserva_retorna_solo_ese_credito()
                    {
                        val parametros = FiltroCreditosPersona.NoConsumidosValidosParaDia(personaCreada.id!!, fechaHoraActual)
                        val entidadesPrueba = crearComprasEnDiferentesDias(fechaHoraActual)
                        val idCreditoFondoNoConsumido =
                                entidadesPrueba
                                    .first()
                                    .creditosPaquetes
                                    .first { it.creditosFondos.first().idPersonaDueña == personaCreada.id!! }
                                    .creditosFondos
                                    .first()
                                    .id

                        val comprasSinQuitandoCreditoFondo =
                                entidadesPrueba.map {
                                    it.copiar(
                                            creditosPaquetes = it.creditosPaquetes.map {
                                                it.copiar(creditosFondos = it.creditosFondos.filter { it.id != idCreditoFondoNoConsumido })
                                            }
                                             )
                                }

                        repositorioReservas.crear(
                                idClientePruebas,
                                darReservaACrearConsumiendoTodosLosCreditos(comprasSinQuitandoCreditoFondo)
                                                 )


                        val entidadEsperada =
                                entidadesPrueba
                                    .map {
                                        it.copiar(
                                                creditosFondos = it.creditosFondos.map { it.copiar(consumido = true) },
                                                creditosPaquetes = it.creditosPaquetes.map {
                                                    it.copiar(creditosFondos = it.creditosFondos.map {
                                                        it.copiar(consumido = it.id != idCreditoFondoNoConsumido)
                                                    })
                                                }
                                                 )
                                    }
                                    .filtrarComprasPorPersonaYFecha(personaCreada.id!!, fechaHoraActual)
                                    .extraerCreditosNoConsumidosDePersona(personaCreada.id!!)

                        val entidadConsultada = repositorio.buscarSegunParametros(idClientePruebas, parametros)
                        assertEquals(entidadEsperada, entidadConsultada)
                    }
                }

                @TestConMultiplesDAO
                fun solo_un_credito_de_fondo_con_fecha_en_el_dia_del_filtro_retorna_el_credito()
                {
                    val parametros = FiltroCreditosPersona.NoConsumidosValidosParaDia(personaCreada.id!!, fechaHoraActual)
                    val entidadACrear = darInstanciaEntidadValida().run {
                        copiar(
                                creditosPaquetes = emptyList(),
                                creditosFondos = listOf(
                                        creditoFondoConNulos.copiar(validoDesde = fechaHoraActual, validoHasta = fechaHoraActual),
                                        creditoFondoSinNulos.copiar(validoDesde = fechaHoraActual.minusDays(99), validoHasta = fechaHoraActual.minusDays(99))
                                                       )
                              )
                    }

                    val entidadCreada = crearCompraYMarcarComoFinalizada(entidadACrear)

                    val entidadEsperada =
                            CreditosDeUnaPersona(
                                    idClientePruebas,
                                    personaCreada.id!!,
                                    entidadCreada.creditosFondos.filter { it.validoDesde!! <= fechaHoraActual && fechaHoraActual <= it.validoHasta!! },
                                    entidadCreada.creditosPaquetes
                                                )

                    val entidadConsultada = repositorio.buscarSegunParametros(idClientePruebas, parametros)

                    assertEquals(entidadEsperada, entidadConsultada)
                }

                @TestConMultiplesDAO
                fun solo_un_credito_de_paquete_con_fecha_en_el_dia_del_filtro_retorna_el_credito()
                {
                    val parametros = FiltroCreditosPersona.NoConsumidosValidosParaDia(personaCreada.id!!, fechaHoraActual)
                    val entidadACrear = darInstanciaEntidadValida().run {
                        copiar(
                                creditosPaquetes = listOf(
                                        CreditoPaquete(
                                                paqueteCreado.id!!,
                                                "código externo paquete",
                                                listOf(
                                                        creditoFondoConNulos.copiar(validoDesde = fechaHoraActual, validoHasta = fechaHoraActual),
                                                        creditoFondoSinNulos.copiar(validoDesde = fechaHoraActual.minusDays(99), validoHasta = fechaHoraActual.minusDays(99))
                                                      )
                                                      ),
                                        CreditoPaquete(
                                                paqueteCreado.id!!,
                                                "código externo paquete",
                                                listOf(
                                                        creditoFondoSinNulos.copiar(validoDesde = fechaHoraActual.minusDays(99), validoHasta = fechaHoraActual.minusDays(99)),
                                                        creditoFondoSinNulos.copiar(validoDesde = fechaHoraActual.minusDays(99), validoHasta = fechaHoraActual.minusDays(99))
                                                      )
                                                      )
                                                         ),
                                creditosFondos = listOf()
                              )
                    }

                    val entidadCreada = crearCompraYMarcarComoFinalizada(entidadACrear)

                    val creditosPaqueteEsperados =
                            entidadCreada.creditosPaquetes.take(1)
                                .map {
                                    it.copiar(creditosFondos = it.creditosFondos.filter {
                                        it.validoDesde!! <= fechaHoraActual && fechaHoraActual <= it.validoHasta!!
                                    })
                                }

                    val entidadEsperada =
                            CreditosDeUnaPersona(
                                    idClientePruebas,
                                    personaCreada.id!!,
                                    entidadCreada.creditosFondos,
                                    creditosPaqueteEsperados
                                                )

                    val listadoConsultado = repositorio.buscarSegunParametros(idClientePruebas, parametros)

                    assertEquals(entidadEsperada, listadoConsultado)
                }
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_EntidadNoExiste_si_no_existe_la_persona_buscada()
            {
                val parametros = FiltroCreditosPersona.NoConsumidosValidosParaDia(personaCreada.id!! + 1000, fechaHoraActual)
                assertThrows<EntidadNoExiste> {
                    repositorio.buscarSegunParametros(idClientePruebas, parametros)
                }
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_EsquemaNoExiste_si_se_consulta_listado_de_entidades_con_id_cliente_inexistente()
            {
                val parametros = FiltroCreditosPersona.NoConsumidosValidosParaDia(personaCreada.id!!, fechaHoraActual)
                assertThrows<EsquemaNoExiste> {
                    repositorio.buscarSegunParametros(idClientePruebas + 100, parametros)
                }
            }

            @TestConMultiplesDAO
            fun compras_marcadas_como_terminadas_en_otro_cliente_retorna_listas_vacias()
            {
                val parametros = FiltroCreditosPersona.NoConsumidosValidosParaDia(personaCreada.id!!, fechaHoraActual)
                val entidadCreada = repositorioCompras.crear(idClientePruebas, darInstanciaEntidadValida())
                val nuevoValor = EntidadTransaccional.CampoCreacionTerminada<Compra>(true)
                repositorioCompras.actualizarCamposIndividuales(
                        idClientePruebas,
                        entidadCreada.id,
                        mapOf<String, CampoModificable<Compra, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                               )

                ejecutarConClienteAlternativo {

                    repositorioPersonas.crear(it.id!!, personaCreada)

                    assertEquals(
                            CreditosDeUnaPersona(it.id!!, personaCreada.id!!, emptyList(), emptyList()),
                            repositorio.buscarSegunParametros(it.id!!, parametros)
                                )
                }
            }
        }
    }
}