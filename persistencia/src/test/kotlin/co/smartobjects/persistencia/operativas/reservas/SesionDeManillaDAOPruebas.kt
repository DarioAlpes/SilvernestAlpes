package co.smartobjects.persistencia.operativas.reservas

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.fondos.Dinero
import co.smartobjects.entidades.fondos.Fondo
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.entidades.operativas.EntidadTransaccional
import co.smartobjects.entidades.operativas.compras.Compra
import co.smartobjects.entidades.operativas.compras.CreditoFondo
import co.smartobjects.entidades.operativas.compras.Pago
import co.smartobjects.entidades.operativas.reservas.Reserva
import co.smartobjects.entidades.operativas.reservas.SesionDeManilla
import co.smartobjects.persistencia.TestConMultiplesDAO
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorio
import co.smartobjects.persistencia.buscarSesionDeManillaDAO
import co.smartobjects.persistencia.excepciones.EntidadNoExiste
import co.smartobjects.persistencia.excepciones.ErrorActualizacionViolacionDeRestriccion
import co.smartobjects.persistencia.excepciones.EsquemaNoExiste
import co.smartobjects.persistencia.operativas.ClasePruebasEntidadesTransaccionales
import co.smartobjects.persistencia.operativas.compras.RepositorioCompras
import co.smartobjects.persistencia.operativas.compras.RepositorioComprasSQL
import co.smartobjects.persistencia.operativas.ordenes.RepositorioOrdenes
import co.smartobjects.persistencia.operativas.ordenes.RepositorioOrdenesSQL
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.threeten.bp.ZonedDateTime
import kotlin.system.measureNanoTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class SesionDeManillaDAOPruebas : ClasePruebasEntidadesTransaccionales()
{
    private val repositorio: RepositorioDeSesionDeManilla by lazy { RepositorioDeSesionDeManillaSQL(configuracionRepositorios) }
    private val repositorioCompras: RepositorioCompras by lazy { RepositorioComprasSQL(configuracionRepositorios) }
    private val repositorioReservas: RepositorioReservas by lazy { RepositorioReservasSQL(configuracionRepositorios) }
    private val repositorioOrdenes: RepositorioOrdenes by lazy { RepositorioOrdenesSQL(configuracionRepositorios) }

    override val creadoresRepositoriosUsados: List<CreadorRepositorio<*>> by lazy {
        super.creadoresRepositoriosUsados +
        listOf<CreadorRepositorio<*>>(
                repositorioCompras,
                repositorioReservas,
                repositorio,
                repositorioOrdenes
                                     )
    }

    private val fondosCreados: List<Fondo<*>> by lazy {
        List(4) {
            repositorioMonedas.crear(idClientePruebas, Dinero(
                    idClientePruebas,
                    null,
                    "Dinero-$it",
                    true,
                    false,
                    false,
                    Precio(Decimal.UNO, impuestoCreado.id!!),
                    "el código externo de prueba $it"
                                                             ))
        }
    }

    private val reservaCreada by lazy {
        val creditos = crearCompraYDarCreditos(true)

        repositorioReservas.crear(
                idClientePruebas,
                Reserva(
                        idClientePruebas,
                        usuarioCreado.datosUsuario.usuario,
                        listOf(
                                SesionDeManilla(
                                        idClientePruebas,
                                        null,
                                        personaCreada.id!!,
                                        null,
                                        null,
                                        null,
                                        creditos.asSequence().filter { it.idPersonaDueña == personaCreada.id!! }.map { it.id!! }.toSet()
                                               )
                              )
                       )
                                 )
    }

    private fun crearCreditoFondo(idPersona: Long, idFondo: Long): CreditoFondo
    {
        return CreditoFondo(
                idClientePruebas,
                null,
                Decimal(10),
                Decimal(1000),
                Decimal(150),
                fechaHoraActual,
                fechaHoraActual,
                false,
                "Taquilla",
                usuarioCreado.datosUsuario.usuario,
                idPersona,
                idFondo,
                "el código externo de prueba $idFondo",
                impuestoCreado.id!!,
                "un-uuid",
                ubicacionCreada.id!!,
                grupoDeClientesCreado.id!!
                           )
    }

    private fun crearCompraYDarCreditos(marcarComoTerminada: Boolean): List<CreditoFondo>
    {
        val creditosFondo =
                listOf(
                        crearCreditoFondo(personaCreada.id!!, fondosCreados[0].id!!),
                        crearCreditoFondo(personaCreada.id!!, fondosCreados[1].id!!)
                      )

        var compraCreada = repositorioCompras.crear(
                idClientePruebas,
                Compra(
                        idClientePruebas,
                        usuarioCreado.datosUsuario.usuario,
                        creditosFondo,
                        listOf(),
                        listOf(Pago(Decimal(1000), Pago.MetodoDePago.EFECTIVO, 1.toString())),
                        fechaHoraActual
                      )
                                                   )

        if (marcarComoTerminada)
        {
            val nuevoValor = EntidadTransaccional.CampoCreacionTerminada<Compra>(true)

            repositorioCompras.actualizarCamposIndividuales(
                    idClientePruebas,
                    compraCreada.id,
                    mapOf<String, CampoModificable<Compra, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                           )

            compraCreada = compraCreada.copiar(creacionTerminada = true)
        }

        return compraCreada.creditos
    }


    @Nested
    inner class Consultar
    {
        @TestConMultiplesDAO
        fun por_id_existente_retorna_entidad_correcta()
        {
            val entidadEsperada = reservaCreada.sesionesDeManilla.first()

            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, reservaCreada.sesionesDeManilla.first().id!!)

            assertEquals(entidadEsperada, entidadConsultada)
        }

        @TestConMultiplesDAO
        fun por_id_no_existente_retorna_null()
        {
            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, 789456789)

            assertNull(entidadConsultada)
        }

        @TestConMultiplesDAO
        fun por_id_existente_en_otro_cliente_retorna_null()
        {
            ejecutarConClienteAlternativo {
                val entidadConsultada = repositorio.buscarPorId(it.id!!, reservaCreada.sesionesDeManilla.first().id!!)
                assertNull(entidadConsultada)
            }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_EsquemaNoExiste_si_se_consulta_por_id_con_id_cliente_inexistente()
        {
            assertThrows<EsquemaNoExiste> { repositorio.buscarPorId(idClientePruebas + 100, 789456789) }
        }
    }

    @Nested
    inner class ActualizarParcialmente
    {
        private lateinit var entidadDePrueba: SesionDeManilla

        @BeforeEach
        fun crearEntidadDePrueba()
        {
            val nuevoValor = EntidadTransaccional.CampoCreacionTerminada<Reserva>(true)
            repositorioReservas.actualizarCamposIndividuales(
                    idClientePruebas,
                    reservaCreada.id,
                    mapOf<String, CampoModificable<Reserva, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                            )

            entidadDePrueba = reservaCreada.sesionesDeManilla.first()
        }

        @TestConMultiplesDAO
        fun solo_se_actualizan_los_campos_necesarios()
        {
            val campoUuid = SesionDeManilla.CampoUuidTag(byteArrayOf(0, 1, 2, 3))

            repositorio.actualizarCamposIndividuales(
                    idClientePruebas,
                    entidadDePrueba.id!!,
                    mapOf<String, CampoModificable<SesionDeManilla, *>>(campoUuid.nombreCampo to campoUuid)
                                                    )

            var entidadConsultada = buscarSesionDeManillaDAO(idClientePruebas, configuracionRepositorios, entidadDePrueba.id!!)

            // Se desactiva después de activar únicamente
            val campoFechaDesactivacion = SesionDeManilla.CampoFechaDesactivacion(entidadConsultada!!.fechaActivacion, entidadConsultada.fechaActivacion!!.plusDays(1))

            repositorio.actualizarCamposIndividuales(
                    idClientePruebas,
                    entidadDePrueba.id!!,
                    mapOf<String, CampoModificable<SesionDeManilla, *>>(campoFechaDesactivacion.nombreCampo to campoFechaDesactivacion)
                                                    )

            entidadConsultada = buscarSesionDeManillaDAO(idClientePruebas, configuracionRepositorios, entidadDePrueba.id!!)

            val entidadEsperada =
                    SesionDeManillaDAO(entidadDePrueba, reservaCreada.id)
                        .copy(id = entidadDePrueba.id,
                              uuidTag = campoUuid.valor,
                              fechaActivacion = entidadConsultada!!.fechaActivacion,
                              fechaDesactivacion = entidadConsultada.fechaDesactivacion
                             )

            assertEquals(entidadEsperada, entidadConsultada)
        }

        @TestConMultiplesDAO
        fun con_id_inexistente_lanza_excepcion_EntidadNoExiste()
        {
            val nuevoValor = SesionDeManilla.CampoUuidTag(byteArrayOf(0, 1, 2, 3))

            assertThrows<EntidadNoExiste> {
                repositorio.actualizarCamposIndividuales(
                        idClientePruebas,
                        entidadDePrueba.id!! + 789456,
                        mapOf<String, CampoModificable<SesionDeManilla, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                        )
            }
        }

        @TestConMultiplesDAO
        fun con_id_existente_en_otro_cliente_lanza_excepcion_EntidadNoExiste()
        {
            val nuevoValor = SesionDeManilla.CampoUuidTag(byteArrayOf(0, 1, 2, 3))

            ejecutarConClienteAlternativo {
                assertThrows<EntidadNoExiste> {
                    repositorio.actualizarCamposIndividuales(
                            it.id!!,
                            entidadDePrueba.id!!,
                            mapOf<String, CampoModificable<SesionDeManilla, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                            )
                }
            }
        }

        @[Nested DisplayName("el campo de uuid del tag")]
        inner class AlActualizarElUuid
        {
            @TestConMultiplesDAO
            fun se_inserta_fecha_de_activacion_si_el_uuid_tag_era_nulo()
            {
                val nuevoValor = SesionDeManilla.CampoUuidTag(byteArrayOf(0, 1, 2, 3))

                val timestampAntesOperacion = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
                val tiempoTranscurridoEnOperacion = measureNanoTime {
                    repositorio.actualizarCamposIndividuales(
                            idClientePruebas,
                            entidadDePrueba.id!!,
                            mapOf<String, CampoModificable<SesionDeManilla, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                            )
                }

                val entidadConsultada = buscarSesionDeManillaDAO(idClientePruebas, configuracionRepositorios, entidadDePrueba.id!!)

                assertNotNull(entidadConsultada!!.fechaActivacion)
                if (timestampAntesOperacion != entidadConsultada.fechaActivacion!!)
                {
                    assertTrue(timestampAntesOperacion.isBefore(entidadConsultada.fechaActivacion))
                    assertTrue(timestampAntesOperacion.plusNanos(tiempoTranscurridoEnOperacion).isAfter(entidadConsultada.fechaActivacion))
                }
            }

            @TestConMultiplesDAO
            fun falla_la_actualizacion_si_ya_habia_un_uuid_tag_asignado_en_bd()
            {
                val nuevoValor = SesionDeManilla.CampoUuidTag(byteArrayOf(0, 1, 2, 3))
                val mapaDeValores = mapOf<String, CampoModificable<SesionDeManilla, *>>(nuevoValor.nombreCampo to nuevoValor)

                repositorio.actualizarCamposIndividuales(idClientePruebas, entidadDePrueba.id!!, mapaDeValores)

                val entidadConFechaActivacionInicial = buscarSesionDeManillaDAO(idClientePruebas, configuracionRepositorios, entidadDePrueba.id!!)

                assertThrows<ErrorActualizacionViolacionDeRestriccion> {
                    repositorio.actualizarCamposIndividuales(idClientePruebas, entidadDePrueba.id!!, mapaDeValores)
                }

                val entidadConsultada = buscarSesionDeManillaDAO(idClientePruebas, configuracionRepositorios, entidadDePrueba.id!!)

                assertEquals(entidadConFechaActivacionInicial, entidadConsultada)
            }
        }

        @[Nested DisplayName("el campo de fecha de desactivación")]
        inner class AlActualizarLaFechaDeDesactivacion
        {
            @TestConMultiplesDAO
            fun se_inserta_fecha_de_desactivacion_si_el_uuid_tag_era_no_nulo()
            {
                val nuevoValor = SesionDeManilla.CampoUuidTag(byteArrayOf(0, 1, 2, 3))

                repositorio.actualizarCamposIndividuales(
                        idClientePruebas,
                        entidadDePrueba.id!!,
                        mapOf<String, CampoModificable<SesionDeManilla, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                        )

                val fechaDeDesactivacion = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
                val campoFechaDesactivacion = SesionDeManilla.CampoFechaDesactivacion(fechaDeDesactivacion.minusDays(10), fechaDeDesactivacion)

                repositorio.actualizarCamposIndividuales(
                        idClientePruebas,
                        entidadDePrueba.id!!,
                        mapOf<String, CampoModificable<SesionDeManilla, *>>(campoFechaDesactivacion.nombreCampo to campoFechaDesactivacion)
                                                        )

                val entidadConsultada = buscarSesionDeManillaDAO(idClientePruebas, configuracionRepositorios, entidadDePrueba.id!!)

                assertEquals(fechaDeDesactivacion, entidadConsultada!!.fechaDesactivacion)
            }

            @Nested
            inner class FallaSi
            {
                @TestConMultiplesDAO
                fun no_habia_un_uuid_tag_asignado_en_bd()
                {
                    val fechaDeDesactivacion = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
                    val campoFechaDesactivacion = SesionDeManilla.CampoFechaDesactivacion(fechaDeDesactivacion.minusDays(10), fechaDeDesactivacion)

                    val excepcion = assertThrows<ErrorActualizacionViolacionDeRestriccion> {
                        repositorio.actualizarCamposIndividuales(
                                idClientePruebas,
                                entidadDePrueba.id!!,
                                mapOf<String, CampoModificable<SesionDeManilla, *>>(campoFechaDesactivacion.nombreCampo to campoFechaDesactivacion)
                                                                )
                    }

                    assertTrue(excepcion.message!!.contains("La sesión no ha sido activada todavía"))
                }

                @TestConMultiplesDAO
                fun ya_habia_sido_desactivada()
                {
                    val nuevoValor = SesionDeManilla.CampoUuidTag(byteArrayOf(0, 1, 2, 3))

                    repositorio.actualizarCamposIndividuales(
                            idClientePruebas,
                            entidadDePrueba.id!!,
                            mapOf<String, CampoModificable<SesionDeManilla, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                            )

                    val fechaDeDesactivacion = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
                    val campoFechaDesactivacion = SesionDeManilla.CampoFechaDesactivacion(fechaDeDesactivacion.minusDays(10), fechaDeDesactivacion)

                    repositorio.actualizarCamposIndividuales(
                            idClientePruebas,
                            entidadDePrueba.id!!,
                            mapOf<String, CampoModificable<SesionDeManilla, *>>(campoFechaDesactivacion.nombreCampo to campoFechaDesactivacion)
                                                            )

                    val excepcion = assertThrows<ErrorActualizacionViolacionDeRestriccion> {
                        repositorio.actualizarCamposIndividuales(
                                idClientePruebas,
                                entidadDePrueba.id!!,
                                mapOf<String, CampoModificable<SesionDeManilla, *>>(campoFechaDesactivacion.nombreCampo to campoFechaDesactivacion)
                                                                )
                    }

                    assertTrue(excepcion.message!!.contains("La sesión ya había sido desactivada"))
                }
            }
        }

        @TestConMultiplesDAO
        fun con_id_cliente_inexistente_lanza_excepcion_EsquemaNoExiste()
        {
            val nuevoValor = SesionDeManilla.CampoUuidTag(byteArrayOf(0, 1, 2, 3))

            assertThrows<EsquemaNoExiste> {
                repositorio.actualizarCamposIndividuales(
                        idClientePruebas + 100,
                        entidadDePrueba.id!!,
                        mapOf<String, CampoModificable<SesionDeManilla, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                        )
            }
        }
    }
}