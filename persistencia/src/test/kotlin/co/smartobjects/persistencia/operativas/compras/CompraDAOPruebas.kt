package co.smartobjects.persistencia.operativas.compras

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.fondos.Dinero
import co.smartobjects.entidades.fondos.Fondo
import co.smartobjects.entidades.fondos.Paquete
import co.smartobjects.entidades.fondos.precios.GrupoClientes
import co.smartobjects.entidades.fondos.precios.Impuesto
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.entidades.operativas.EntidadTransaccional
import co.smartobjects.entidades.operativas.compras.Compra
import co.smartobjects.entidades.operativas.compras.CreditoFondo
import co.smartobjects.entidades.operativas.compras.CreditoPaquete
import co.smartobjects.entidades.operativas.compras.Pago
import co.smartobjects.entidades.operativas.reservas.Reserva
import co.smartobjects.entidades.operativas.reservas.SesionDeManilla
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.ubicaciones.Ubicacion
import co.smartobjects.entidades.usuarios.Rol
import co.smartobjects.entidades.usuarios.Usuario
import co.smartobjects.persistencia.*
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorio
import co.smartobjects.persistencia.excepciones.EntidadNoExiste
import co.smartobjects.persistencia.excepciones.ErrorCreacionActualizacionPorDuplicidad
import co.smartobjects.persistencia.excepciones.ErrorDeLlaveForanea
import co.smartobjects.persistencia.excepciones.EsquemaNoExiste
import co.smartobjects.persistencia.fondos.grupoClientesCategoriaB
import co.smartobjects.persistencia.operativas.ClasePruebasEntidadesTransaccionales
import co.smartobjects.persistencia.operativas.ordenes.RepositorioOrdenes
import co.smartobjects.persistencia.operativas.ordenes.RepositorioOrdenesSQL
import co.smartobjects.persistencia.operativas.reservas.RepositorioDeSesionDeManilla
import co.smartobjects.persistencia.operativas.reservas.RepositorioDeSesionDeManillaSQL
import co.smartobjects.persistencia.operativas.reservas.RepositorioReservas
import co.smartobjects.persistencia.operativas.reservas.RepositorioReservasSQL
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZonedDateTime
import java.util.*
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DisplayName("CompraDAO")
internal class CompraDAOPruebas
{
    internal class ProveedorMetodoDePagoDAOConMetodoDePago : ArgumentsProvider
    {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments>
        {
            return Stream.of(
                    Arguments.of(PagoDAO.MetodoDePago.EFECTIVO, Pago.MetodoDePago.EFECTIVO),
                    Arguments.of(PagoDAO.MetodoDePago.TARJETA_CREDITO, Pago.MetodoDePago.TARJETA_CREDITO),
                    Arguments.of(PagoDAO.MetodoDePago.TARJETA_DEBITO, Pago.MetodoDePago.TARJETA_DEBITO)
                            )
        }
    }

    @Nested
    inner class Conversion
    {
        @Nested
        inner class EnPagoDAO
        {
            @Nested
            @DisplayName("Al convertir a entidad de negocio")
            inner class AEntidadNegocio
            {
                @DisplayName("Cuando el metodo de pago en DAO")
                @ParameterizedTest(name = "Es ''{0}'' asigna ''{1}'' en negocio")
                @ArgumentsSource(ProveedorMetodoDePagoDAOConMetodoDePago::class)
                fun paraMetodoDePago(metodoPagoDAO: PagoDAO.MetodoDePago, metodoPago: Pago.MetodoDePago)
                {
                    val entidadDAO = PagoDAO(metodoPago = metodoPagoDAO, numeroDeTransaccionPOS = "12-3")
                    val entidadNegocio = entidadDAO.aEntidadDeNegocio(1)
                    assertEquals(metodoPago, entidadNegocio.metodoPago)
                }
            }

            @Nested
            @DisplayName("Al convertir desde entidad de negocio")
            inner class DesdeEntidadNegocio
            {
                @DisplayName("Cuando el metodo de pago")
                @ParameterizedTest(name = "Es ''{1}'' asigna ''{0}'' en dao")
                @ArgumentsSource(ProveedorMetodoDePagoDAOConMetodoDePago::class)
                fun paraMetodoDePago(metodoPagoDAO: PagoDAO.MetodoDePago, metodoPago: Pago.MetodoDePago)
                {
                    val entidadNegocio = Pago(Decimal.DIEZ, metodoPago, "12-3")
                    val entidadDAO = PagoDAO(entidadNegocio, "123")
                    assertEquals(metodoPagoDAO, entidadDAO.metodoPago)
                }
            }
        }
    }

    @Nested
    inner class EnBD : ClasePruebasEntidadesTransaccionales()
    {
        private val repositorioSesionesDeManilla: RepositorioDeSesionDeManilla by lazy { RepositorioDeSesionDeManillaSQL(configuracionRepositorios) }
        private val repositorioOrdenes: RepositorioOrdenes by lazy { RepositorioOrdenesSQL(configuracionRepositorios) }
        private val repositorioComprasDeUnaPersona: RepositorioComprasDeUnaPersona by lazy { RepositorioComprasDeUnaPersonaSQL(configuracionRepositorios) }
        private val repositorioPersonasDeUnaCompra: RepositorioPersonasDeUnaCompra by lazy { RepositorioPersonasDeUnaCompraSQL(configuracionRepositorios) }
        private val repositorioReservas: RepositorioReservas by lazy { RepositorioReservasSQL(configuracionRepositorios) }
        private val repositorio: RepositorioCompras by lazy { RepositorioComprasSQL(configuracionRepositorios) }

        override val creadoresRepositoriosUsados: List<CreadorRepositorio<*>> by lazy {
            super.creadoresRepositoriosUsados + listOf<CreadorRepositorio<*>>(repositorio, repositorioReservas, repositorioSesionesDeManilla, repositorioOrdenes)
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


        private fun darInstanciaEntidadValidaSegunNombreUsuario(nombreUsuario: String): Compra
        {
            return Compra(idClientePruebas, nombreUsuario, listOf(creditoFondoConNulos), listOf(), listOf(pagoPruebas), fechaHoraActual)
        }

        private fun darInstanciaEntidadValida(): Compra
        {
            return darInstanciaEntidadValidaSegunNombreUsuario(usuarioCreado.datosUsuario.usuario)
        }

        private fun darInstanciaEntidadValidaConMultiplesCreditos(): Compra
        {
            return Compra(
                    idClientePruebas,
                    usuarioCreado.datosUsuario.usuario,
                    listOf(creditoFondoConNulos, creditoFondoSinNulos),
                    listOf(
                            CreditoPaquete(paqueteCreado.id!!, "código externo paquete", listOf(creditoFondoConNulos, creditoFondoSinNulos)),
                            CreditoPaquete(paqueteCreado.id!!, "código externo paquete", listOf(creditoFondoConNulos, creditoFondoSinNulos))
                          ),
                    listOf(pagoPruebas),
                    fechaHoraActual
                         )
        }

        private fun darInstanciaEntidadValidaConMultiplesCreditosConIdAleatorio(indice: Int): Compra
        {
            return Compra(
                    idClientePruebas,
                    usuarioCreado.datosUsuario.usuario,
                    UUID.randomUUID(),
                    ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO).toInstant().toEpochMilli() - indice * 100,
                    false,
                    listOf(creditoFondoConNulos, creditoFondoSinNulos),
                    listOf(
                            CreditoPaquete(paqueteCreado.id!!, "código externo paquete", listOf(creditoFondoConNulos, creditoFondoSinNulos)),
                            CreditoPaquete(paqueteCreado.id!!, "código externo paquete", listOf(creditoFondoConNulos, creditoFondoSinNulos))
                          ),
                    listOf(pagoPruebas.copiar(numeroDeTransaccionPOS = pagoPruebas.numeroDeTransaccionPOS + indice)),
                    fechaHoraActual
                         )
        }

        private fun darInstanciaEntidadValidaConMultiplesCreditosConMultiplesPersonas(): Compra
        {
            return Compra(
                    idClientePruebas,
                    usuarioCreado.datosUsuario.usuario,
                    UUID.randomUUID(),
                    ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO).toInstant().toEpochMilli() - 100,
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
                    listOf(pagoPruebas.copiar(numeroDeTransaccionPOS = pagoPruebas.numeroDeTransaccionPOS)),
                    fechaHoraActual
                         )
        }

        private fun Compra.actualizarIdsCreditos(compraCreada: Compra): Compra
        {
            return copiar(
                    creditosFondos = creditosFondos.zip(compraCreada.creditosFondos).map { it.first.copiar(id = it.second.id) },
                    creditosPaquetes = creditosPaquetes.zip(compraCreada.creditosPaquetes).map {
                        it.first.copiar(creditosFondos = it.first.creditosFondos.zip(it.second.creditosFondos).map { it.first.copiar(id = it.second.id) })
                    }
                         )
        }

        private fun Compra.actualizarIdsClientes(idAAsignar: Long): Compra
        {
            return copiar(
                    idCliente = idAAsignar,
                    creditosFondos = creditosFondos.map { it.copiar(idCliente = idAAsignar) },
                    creditosPaquetes = creditosPaquetes.map { it.copiar(creditosFondos = it.creditosFondos.map { it.copiar(idCliente = idAAsignar) }) }
                         )
        }

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

        @Nested
        inner class Crear
        {
            @[Nested DisplayName("Retorna misma entidad, id compra y id créditos asignados a creditos por bd para compra")]
            inner class FuncionaCorrectamente
            {
                @TestConMultiplesDAO
                fun por_defecto()
                {
                    val entidadAInsertar = darInstanciaEntidadValida()
                    val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

                    assertEquals(entidadAInsertar.id, entidadCreada.id)
                    assertEquals(entidadAInsertar.actualizarIdsCreditos(entidadCreada), entidadCreada)
                }

                @TestConMultiplesDAO
                fun con_multiples_creditos_fondos_y_creditos_paquetes()
                {
                    val entidadAInsertar = darInstanciaEntidadValidaConMultiplesCreditos()
                    val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

                    assertEquals(entidadAInsertar.actualizarIdsCreditos(entidadCreada), entidadCreada)
                }

                @TestConMultiplesDAO
                fun con_multiples_creditos_fondos_y_sin_creditos_paquetes()
                {
                    val entidadAInsertar = darInstanciaEntidadValidaConMultiplesCreditos().copiar(creditosPaquetes = listOf())
                    val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

                    assertEquals(entidadAInsertar.actualizarIdsCreditos(entidadCreada), entidadCreada)
                }

                @TestConMultiplesDAO
                fun sin_creditos_fondos_y_con_multiples_creditos_paquetes()
                {
                    val entidadAInsertar = darInstanciaEntidadValidaConMultiplesCreditos().copiar(creditosFondos = listOf())
                    val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

                    assertEquals(entidadAInsertar.actualizarIdsCreditos(entidadCreada), entidadCreada)
                }
            }

            @TestConMultiplesDAO
            fun crea_los_creditos_como_no_consumidos_sin_importar_si_se_envian_como_consumidos()
            {
                val entidadAInsertar = darInstanciaEntidadValidaConMultiplesCreditos().run {
                    copiar(
                            creditosFondos = creditosFondos.map { it.copiar(consumido = true) },
                            creditosPaquetes = creditosPaquetes.map { it.copiar(creditosFondos = it.creditosFondos.map { it.copiar(consumido = true) }) }
                          )

                }
                val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)
                val entidadEsperada = entidadAInsertar.run {
                    copiar(
                            creditosFondos = creditosFondos.map { it.copiar(consumido = false) },
                            creditosPaquetes = creditosPaquetes.map { it.copiar(creditosFondos = it.creditosFondos.map { it.copiar(consumido = false) }) }
                          )

                }.actualizarIdsCreditos(entidadCreada)

                assertEquals(entidadEsperada, entidadCreada)
            }

            @TestConMultiplesDAO
            fun crea_la_compra_como_no_finalizada_sin_importar_si_se_envia_como_finalizada()
            {
                val entidadAInsertar = darInstanciaEntidadValida().copiar(creacionTerminada = true)
                val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

                assertEquals(entidadAInsertar.copiar(creacionTerminada = false).actualizarIdsCreditos(entidadCreada), entidadCreada)
            }

            @TestConMultiplesDAO
            fun ignora_el_id_de_los_creditos_y_retorna_misma_entidad_con_ids_creditos_asignados_por_bd_y_id_compra_correcto()
            {
                val entidadAInsertar = darInstanciaEntidadValidaConMultiplesCreditos().run {
                    copiar(
                            creditosFondos = creditosFondos.map { it.copiar(id = 9876543) },
                            creditosPaquetes = creditosPaquetes.map { it.copiar(creditosFondos = it.creditosFondos.map { it.copiar(id = 9876543) }) }
                          )

                }
                val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

                assertEquals(entidadAInsertar.id, entidadCreada.id)
                assertEquals(entidadAInsertar.actualizarIdsCreditos(entidadCreada), entidadCreada)
            }

            @TestConMultiplesDAO
            fun utiliza_el_parametro_id_cliente_y_retorna_misma_entidad_con_id_cliente_correcto()
            {
                val entidadAInsertar = darInstanciaEntidadValidaConMultiplesCreditos().actualizarIdsClientes(9876543)

                val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

                assertEquals(idClientePruebas, entidadCreada.idCliente)
                val entidadEsperada = entidadAInsertar.actualizarIdsCreditos(entidadCreada).actualizarIdsClientes(idClientePruebas)
                assertEquals(entidadEsperada, entidadCreada)
            }

            @TestConMultiplesDAO
            fun reemplaza_la_compra_cuando_se_recrea_con_el_mismo_id_y_no_esta_marcada_como_terminada()
            {
                val entidadAInsertarOriginal = darInstanciaEntidadValidaConMultiplesCreditos()
                repositorio.crear(idClientePruebas, entidadAInsertarOriginal)
                val entidadAInsertarModificada = entidadAInsertarOriginal.copiar(
                        creditosPaquetes = listOf(entidadAInsertarOriginal.creditosPaquetes.first()),
                        creditosFondos = listOf(entidadAInsertarOriginal.creditosFondos.first())
                                                                                )
                val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertarModificada)

                assertEquals(entidadAInsertarModificada.actualizarIdsCreditos(entidadCreada), entidadCreada)
                assertEquals(entidadCreada, repositorio.buscarPorId(idClientePruebas, entidadAInsertarOriginal.id))
                assertEquals(entidadCreada, repositorio.buscarPorId(idClientePruebas, entidadAInsertarModificada.id))
                assertEquals(listOf(entidadCreada), repositorio.listar(idClientePruebas).toList())
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_ErrorCreacionActualizacionPorDuplicidad_al_intentar_crear_una_compra_con_id_existente_marcada_como_finalizada_y_no_edita_entidad_previa()
            {
                val entidadAInsertarOriginal = darInstanciaEntidadValidaConMultiplesCreditos()
                val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertarOriginal)

                val nuevoValor = EntidadTransaccional.CampoCreacionTerminada<Compra>(true)
                repositorio.actualizarCamposIndividuales(
                        idClientePruebas,
                        entidadCreada.id,
                        mapOf<String, CampoModificable<Compra, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                        )

                val entidadAInsertarModificada = entidadAInsertarOriginal.copiar(
                        creditosPaquetes = listOf(entidadAInsertarOriginal.creditosPaquetes.first()),
                        creditosFondos = listOf(entidadAInsertarOriginal.creditosFondos.first()),
                        pagos = entidadAInsertarOriginal.pagos.map { it.copiar(numeroDeTransaccionPOS = it.numeroDeTransaccionPOS + 10) }
                                                                                )

                assertThrows<ErrorCreacionActualizacionPorDuplicidad> {
                    repositorio.crear(idClientePruebas, entidadAInsertarModificada)
                }
                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadCreada.id)
                assertEquals(entidadCreada.copiar(creacionTerminada = true), entidadConsultada)
            }

            @Nested
            inner class LanzaErrorDeLlaveForanea
            {
                @[Nested DisplayName("si usa un usuario inexistente")]
                inner class UsuarioInexistente
                {
                    @TestConMultiplesDAO
                    fun en_compra()
                    {
                        val entidadAInsertar = darInstanciaEntidadValidaSegunNombreUsuario(usuarioCreado.datosUsuario.usuario + "-inexistente")

                        assertThrows<ErrorDeLlaveForanea> {
                            repositorio.crear(idClientePruebas, entidadAInsertar)
                        }
                    }

                    @TestConMultiplesDAO
                    fun en_un_credito_fondo_individual()
                    {
                        val entidadAInsertar = darInstanciaEntidadValidaConMultiplesCreditos().run {
                            copiar(creditosFondos = creditosFondos.map { it.copiar(nombreUsuario = "Usuario inexistente") })
                        }

                        assertThrows<ErrorDeLlaveForanea> {
                            repositorio.crear(idClientePruebas, entidadAInsertar)
                        }
                    }

                    @TestConMultiplesDAO
                    fun en_un_credito_fondo_de_un_credito_paquete()
                    {
                        val entidadAInsertar = darInstanciaEntidadValidaConMultiplesCreditos().run {
                            copiar(
                                    creditosPaquetes = creditosPaquetes.map {
                                        it.copiar(creditosFondos = creditosFondos.map { it.copiar(nombreUsuario = "Usuario inexistente") })
                                    }
                                  )
                        }

                        assertThrows<ErrorDeLlaveForanea> {
                            repositorio.crear(idClientePruebas, entidadAInsertar)
                        }
                    }
                }

                @[Nested DisplayName("si usa un impuesto inexistente")]
                inner class ImpuestoInexistente
                {
                    @TestConMultiplesDAO
                    fun en_un_credito_fondo_individual()
                    {
                        val entidadAInsertar = darInstanciaEntidadValidaConMultiplesCreditos().run {
                            copiar(creditosFondos = creditosFondos.map { it.copiar(idImpuestoPagado = 2345345645) })
                        }

                        assertThrows<ErrorDeLlaveForanea> {
                            repositorio.crear(idClientePruebas, entidadAInsertar)
                        }
                    }

                    @TestConMultiplesDAO
                    fun en_un_credito_fondo_de_un_credito_paquete()
                    {
                        val entidadAInsertar = darInstanciaEntidadValidaConMultiplesCreditos().run {
                            copiar(
                                    creditosPaquetes = creditosPaquetes.map {
                                        it.copiar(creditosFondos = creditosFondos.map { it.copiar(idImpuestoPagado = 2345345645) })
                                    }
                                  )
                        }

                        assertThrows<ErrorDeLlaveForanea> {
                            repositorio.crear(idClientePruebas, entidadAInsertar)
                        }
                    }
                }

                @[Nested DisplayName("si usa un fondo inexistente")]
                inner class FondoInexistente
                {
                    @TestConMultiplesDAO
                    fun en_un_credito_fondo_individual()
                    {
                        val entidadAInsertar = darInstanciaEntidadValidaConMultiplesCreditos().run {
                            copiar(creditosFondos = creditosFondos.map { it.copiar(idFondoComprado = 2345345645) })
                        }

                        assertThrows<ErrorDeLlaveForanea> {
                            repositorio.crear(idClientePruebas, entidadAInsertar)
                        }
                    }

                    @TestConMultiplesDAO
                    fun en_un_credito_fondo_de_un_credito_paquete()
                    {
                        val entidadAInsertar = darInstanciaEntidadValidaConMultiplesCreditos().run {
                            copiar(
                                    creditosPaquetes = creditosPaquetes.map {
                                        it.copiar(creditosFondos = creditosFondos.map { it.copiar(idFondoComprado = 2345345645) })
                                    }
                                  )
                        }

                        assertThrows<ErrorDeLlaveForanea> {
                            repositorio.crear(idClientePruebas, entidadAInsertar)
                        }
                    }
                }

                @[Nested DisplayName("si usa una persona inexistente")]
                inner class PersonaInexistente
                {
                    @TestConMultiplesDAO
                    fun en_un_credito_fondo_individual()
                    {
                        val entidadAInsertar = darInstanciaEntidadValidaConMultiplesCreditos().run {
                            copiar(creditosFondos = creditosFondos.map { it.copiar(idPersonaDueña = 2345345645) })
                        }

                        assertThrows<ErrorDeLlaveForanea> {
                            repositorio.crear(idClientePruebas, entidadAInsertar)
                        }
                    }

                    @TestConMultiplesDAO
                    fun en_un_credito_fondo_de_un_credito_paquete()
                    {
                        val entidadAInsertar = darInstanciaEntidadValidaConMultiplesCreditos().run {
                            copiar(
                                    creditosPaquetes = creditosPaquetes.map {
                                        it.copiar(creditosFondos = creditosFondos.map { it.copiar(idPersonaDueña = 2345345645) })
                                    }
                                  )
                        }

                        assertThrows<ErrorDeLlaveForanea> {
                            repositorio.crear(idClientePruebas, entidadAInsertar)
                        }
                    }
                }

                @[Nested DisplayName("si usa una ubicacion inexistente")]
                inner class UbicacionInexistente
                {
                    @TestConMultiplesDAO
                    fun en_un_credito_fondo_individual()
                    {
                        val entidadAInsertar = darInstanciaEntidadValidaConMultiplesCreditos().run {
                            copiar(creditosFondos = creditosFondos.map { it.copiar(idUbicacionCompra = 2345345645) })
                        }

                        assertThrows<ErrorDeLlaveForanea> {
                            repositorio.crear(idClientePruebas, entidadAInsertar)
                        }
                    }

                    @TestConMultiplesDAO
                    fun en_un_credito_fondo_de_un_credito_paquete()
                    {
                        val entidadAInsertar = darInstanciaEntidadValidaConMultiplesCreditos().run {
                            copiar(
                                    creditosPaquetes = creditosPaquetes.map {
                                        it.copiar(creditosFondos = creditosFondos.map { it.copiar(idUbicacionCompra = 2345345645) })
                                    }
                                  )
                        }

                        assertThrows<ErrorDeLlaveForanea> {
                            repositorio.crear(idClientePruebas, entidadAInsertar)
                        }
                    }
                }

                @[Nested DisplayName("si usa un grupo de clientes inexistente")]
                inner class GrupoDeClientesInexistente
                {
                    @TestConMultiplesDAO
                    fun en_un_credito_fondo_individual()
                    {
                        val entidadAInsertar = darInstanciaEntidadValidaConMultiplesCreditos().run {
                            copiar(creditosFondos = creditosFondos.map { it.copiar(idGrupoClientesPersona = 2345345645) })
                        }

                        assertThrows<ErrorDeLlaveForanea> {
                            repositorio.crear(idClientePruebas, entidadAInsertar)
                        }
                    }

                    @TestConMultiplesDAO
                    fun en_un_credito_fondo_de_un_credito_paquete()
                    {
                        val entidadAInsertar = darInstanciaEntidadValidaConMultiplesCreditos().run {
                            copiar(
                                    creditosPaquetes = creditosPaquetes.map {
                                        it.copiar(creditosFondos = creditosFondos.map { it.copiar(idGrupoClientesPersona = 2345345645) })
                                    }
                                  )
                        }

                        assertThrows<ErrorDeLlaveForanea> {
                            repositorio.crear(idClientePruebas, entidadAInsertar)
                        }
                    }
                }
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_EsquemaNoExiste_si_cliente_con_id_dado_no_existe()
            {
                val entidadAInsertar = darInstanciaEntidadValida()

                assertThrows<EsquemaNoExiste> { repositorio.crear(idClientePruebas + 100, entidadAInsertar) }
            }
        }

        @Nested
        inner class Consultar
        {
            @TestConMultiplesDAO
            fun por_id_existente_retorna_entidad_correcta()
            {
                val entidadPrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadValidaConMultiplesCreditos())

                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadPrueba.id)

                assertEquals(entidadPrueba, entidadConsultada)
            }

            @TestConMultiplesDAO
            fun por_id_existente_con_multiples_creditos_fondos_y_sin_creditos_paquetes_retorna_entidad_correcta()
            {
                val entidadPrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadValidaConMultiplesCreditos().copiar(creditosPaquetes = listOf()))

                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadPrueba.id)

                assertEquals(entidadPrueba, entidadConsultada)
            }

            @TestConMultiplesDAO
            fun por_id_existente_con_multiples_creditos_paquetes_y_sin_creditos_fondos_retorna_entidad_correcta()
            {
                val entidadPrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadValidaConMultiplesCreditos().copiar(creditosFondos = listOf()))

                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadPrueba.id)

                assertEquals(entidadPrueba, entidadConsultada)
            }

            @TestConMultiplesDAO
            fun listado_de_entidades_sin_entidades_retorna_lista_vacia()
            {
                val listadoConsultado = repositorio.listar(idClientePruebas)

                assertTrue(listadoConsultado.none())
            }

            @TestConMultiplesDAO
            fun listado_de_entidades_retorna_entidades_correctas()
            {
                val entidadesPrueba = (1..3).map {
                    repositorio.crear(
                            idClientePruebas,
                            darInstanciaEntidadValidaConMultiplesCreditosConIdAleatorio(it)
                                     )
                }

                val listadoEsperado = entidadesPrueba.sortedBy { it.id }

                val listadoConsultado = repositorio.listar(idClientePruebas).toList()

                assertEquals(listadoEsperado, listadoConsultado.sortedBy { it.id })
            }

            @TestConMultiplesDAO
            fun por_id_no_existente_retorna_null()
            {
                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, "entidad-inexistente")

                assertNull(entidadConsultada)
            }

            @TestConMultiplesDAO
            fun por_id_existente_en_otro_cliente_retorna_null()
            {
                val entidadPrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadValidaConMultiplesCreditos())

                ejecutarConClienteAlternativo {
                    val entidadConsultada = repositorio.buscarPorId(it.id!!, entidadPrueba.id)
                    assertNull(entidadConsultada)
                }
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_EsquemaNoExiste_si_se_consulta_por_id_con_id_cliente_inexistente()
            {
                assertThrows<EsquemaNoExiste> { repositorio.buscarPorId(idClientePruebas + 100, "un-id") }
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_EsquemaNoExiste_si_se_consulta_listado_de_entidades_con_id_cliente_inexistente()
            {
                assertThrows<EsquemaNoExiste> { repositorio.listar(idClientePruebas + 100) }
            }

            @TestConMultiplesDAO
            fun listado_de_entidades_con_entidades_en_otro_cliente_retorna_lista_vacia()
            {
                repositorio.crear(idClientePruebas, darInstanciaEntidadValida())

                ejecutarConClienteAlternativo {
                    val listadoConsultado = repositorio.listar(it.id!!)
                    assertTrue(listadoConsultado.none())
                }
            }
        }

        @Nested
        inner class ConsultarComprasDeUnaPersona
        {
            private fun crearCompraYMarcarComoFinalizada(compra: Compra): Compra
            {
                val entidadCreada = repositorio.crear(idClientePruebas, compra)

                val nuevoValor = EntidadTransaccional.CampoCreacionTerminada<Compra>(true)
                repositorio.actualizarCamposIndividuales(
                        idClientePruebas,
                        entidadCreada.id,
                        mapOf<String, CampoModificable<Compra, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                        )

                return entidadCreada.copiar(creacionTerminada = true)
            }

            @Nested
            inner class FiltrandoSoloCreditosPresentesOFuturos
            {
                @TestConMultiplesDAO
                fun sin_entidades_retorna_lista_vacia()
                {
                    val parametros = FiltroComprasPersonaConCreditosPresentesOFuturos(personaCreada.id!!, fechaHoraActual)
                    val listadoConsultado = repositorioComprasDeUnaPersona.listarSegunParametros(idClientePruebas, parametros)

                    assertTrue(listadoConsultado.none())
                }

                @TestConMultiplesDAO
                fun compras_sin_marcar_como_finalizadas_retorna_lista_vacia()
                {
                    val parametros = FiltroComprasPersonaConCreditosPresentesOFuturos(personaCreada.id!!, fechaHoraActual)
                    (1..3).map {
                        repositorio.crear(
                                idClientePruebas,
                                darInstanciaEntidadValidaConMultiplesCreditosConIdAleatorio(it)
                                         )
                    }
                    val listadoConsultado = repositorioComprasDeUnaPersona.listarSegunParametros(idClientePruebas, parametros).toList()
                    assertTrue(listadoConsultado.none())
                }

                @Nested
                inner class ComprasMarcadasFinalizadas
                {
                    @TestConMultiplesDAO
                    fun retorna_lista_correcta()
                    {
                        val parametros = FiltroComprasPersonaConCreditosPresentesOFuturos(personaCreada.id!!, fechaHoraActual)
                        val entidadesPrueba = (1..3).map {
                            crearCompraYMarcarComoFinalizada(darInstanciaEntidadValidaConMultiplesCreditosConIdAleatorio(it))
                        }

                        val listadoEsperado = entidadesPrueba.sortedBy { it.id }

                        val listadoConsultado = repositorioComprasDeUnaPersona.listarSegunParametros(idClientePruebas, parametros).toList()
                        assertEquals(listadoEsperado, listadoConsultado.sortedBy { it.id })
                    }

                    @TestConMultiplesDAO
                    fun consumiendo_todos_los_creditos_en_reserva_retorna_lista_vacia()
                    {
                        val parametros = FiltroComprasPersonaConCreditosPresentesOFuturos(personaCreada.id!!, fechaHoraActual)
                        val entidadesPrueba = (1..3).map {
                            crearCompraYMarcarComoFinalizada(darInstanciaEntidadValidaConMultiplesCreditosConIdAleatorio(it))
                        }
                        repositorioReservas.crear(idClientePruebas, darReservaACrearConsumiendoTodosLosCreditos(entidadesPrueba))
                        val listadoConsultado = repositorioComprasDeUnaPersona.listarSegunParametros(idClientePruebas, parametros).toList()
                        assertTrue(listadoConsultado.none())
                    }

                    @TestConMultiplesDAO
                    fun consumiendo_todos_los_creditos_menos_un_credito_fondo_en_reserva_retorna_lista_completa()
                    {
                        val parametros = FiltroComprasPersonaConCreditosPresentesOFuturos(personaCreada.id!!, fechaHoraActual)
                        val entidadesPrueba = (1..3).map {
                            crearCompraYMarcarComoFinalizada(darInstanciaEntidadValidaConMultiplesCreditosConIdAleatorio(it))
                        }
                        val idsPrimerosCreditoFondo = entidadesPrueba.map { it.creditosFondos.first().id }.toSet()
                        val comprasSinPrimero = entidadesPrueba.map { it.copiar(creditosFondos = it.creditosFondos.drop(1)) }
                        repositorioReservas.crear(idClientePruebas, darReservaACrearConsumiendoTodosLosCreditos(comprasSinPrimero))


                        val listadoEsperado = entidadesPrueba.map {
                            it.copiar(
                                    creditosFondos = it.creditosFondos.map { it.copiar(consumido = !idsPrimerosCreditoFondo.contains(it.id)) },
                                    creditosPaquetes = it.creditosPaquetes.map { it.copiar(creditosFondos = it.creditosFondos.map { it.copiar(consumido = true) }) }
                                     )
                        }.sortedBy { it.id }

                        val listadoConsultado = repositorioComprasDeUnaPersona.listarSegunParametros(idClientePruebas, parametros).toList()
                        assertEquals(listadoEsperado, listadoConsultado.sortedBy { it.id })
                    }

                    @TestConMultiplesDAO
                    fun consumiendo_todos_los_creditos_menos_un_credito_paquete_en_reserva_retorna_lista_completa()
                    {
                        val parametros = FiltroComprasPersonaConCreditosPresentesOFuturos(personaCreada.id!!, fechaHoraActual)
                        val entidadesPrueba = (1..3).map {
                            crearCompraYMarcarComoFinalizada(darInstanciaEntidadValidaConMultiplesCreditosConIdAleatorio(it))
                        }
                        val idsPrimerosCreditoPaquete = entidadesPrueba.map { it.creditosPaquetes.first().creditosFondos.first().id }.toSet()
                        val comprasSinPrimero =
                                entidadesPrueba.map {
                                    it.copiar(
                                            creditosPaquetes = it.creditosPaquetes.map {
                                                it.copiar(creditosFondos = it.creditosFondos.filter { !idsPrimerosCreditoPaquete.contains(it.id) })
                                            }
                                             )
                                }
                        repositorioReservas.crear(idClientePruebas, darReservaACrearConsumiendoTodosLosCreditos(comprasSinPrimero))


                        val listadoEsperado = entidadesPrueba.map {
                            it.copiar(
                                    creditosFondos = it.creditosFondos.map { it.copiar(consumido = true) },
                                    creditosPaquetes = it.creditosPaquetes.map { it.copiar(creditosFondos = it.creditosFondos.map { it.copiar(consumido = !idsPrimerosCreditoPaquete.contains(it.id)) }) }
                                     )
                        }.sortedBy { it.id }

                        val listadoConsultado = repositorioComprasDeUnaPersona.listarSegunParametros(idClientePruebas, parametros).toList()
                        assertEquals(listadoEsperado, listadoConsultado.sortedBy { it.id })
                    }

                    @TestConMultiplesDAO
                    fun fecha_menor_a_la_del_credito_retorna_lista_correcta()
                    {
                        val parametros = FiltroComprasPersonaConCreditosPresentesOFuturos(personaCreada.id!!, fechaHoraActual.minusDays(1))
                        val entidadesPrueba = (1..3).map {
                            crearCompraYMarcarComoFinalizada(darInstanciaEntidadValidaConMultiplesCreditosConIdAleatorio(it))
                        }

                        val listadoEsperado = entidadesPrueba.sortedBy { it.id }

                        val listadoConsultado = repositorioComprasDeUnaPersona.listarSegunParametros(idClientePruebas, parametros).toList()
                        assertEquals(listadoEsperado, listadoConsultado.sortedBy { it.id })
                    }

                    @TestConMultiplesDAO
                    fun fecha_entre_valido_desde_y_valido_hasta_del_credito_retorna_lista_correcta()
                    {
                        val parametros = FiltroComprasPersonaConCreditosPresentesOFuturos(personaCreada.id!!, fechaHoraActual)
                        val entidadesPrueba = (1..3).map {
                            val compraACrear =
                                    darInstanciaEntidadValidaConMultiplesCreditosConIdAleatorio(it).run {
                                        copiar(
                                                creditosPaquetes = creditosPaquetes.map {
                                                    it.copiar(it.idPaquete, "código externo paquete", creditosFondos.map {
                                                        it.copiar(
                                                                validoDesde = it.validoDesde!!.minusDays(1),
                                                                validoHasta = it.validoHasta!!.plusDays(1)
                                                                 )
                                                    })
                                                },
                                                creditosFondos = creditosFondos.map {
                                                    it.copiar(
                                                            validoDesde = it.validoDesde!!.minusDays(1),
                                                            validoHasta = it.validoHasta!!.plusDays(1)
                                                             )
                                                }
                                              )
                                    }

                            crearCompraYMarcarComoFinalizada(compraACrear)
                        }

                        val listadoEsperado = entidadesPrueba.sortedBy { it.id }

                        val listadoConsultado = repositorioComprasDeUnaPersona.listarSegunParametros(idClientePruebas, parametros).toList()
                        assertEquals(listadoEsperado, listadoConsultado.sortedBy { it.id })
                    }

                    @TestConMultiplesDAO
                    fun solo_un_credito_de_fondo_con_fecha_menor_o_igual_a_la_del_filtro_retorna_la_compra()
                    {
                        val parametros = FiltroComprasPersonaConCreditosPresentesOFuturos(personaCreada.id!!, fechaHoraActual)
                        val entidadACrear = darInstanciaEntidadValida().run {
                            copiar(
                                    creditosPaquetes = listOf(),
                                    creditosFondos = listOf(
                                            creditoFondoConNulos.copiar(validoDesde = fechaHoraActual),
                                            creditoFondoSinNulos.copiar(validoDesde = fechaHoraActual.minusDays(2), validoHasta = fechaHoraActual.minusDays(1))
                                                           )
                                  )
                        }

                        val entidadCreada = crearCompraYMarcarComoFinalizada(entidadACrear)

                        val listadoEsperado = listOf(entidadCreada)

                        val listadoConsultado = repositorioComprasDeUnaPersona.listarSegunParametros(idClientePruebas, parametros).toList()
                        assertEquals(listadoEsperado, listadoConsultado)
                    }

                    @TestConMultiplesDAO
                    fun solo_un_credito_de_paquete_con_fecha_menor_o_igual_a_la_del_filtro_retorna_la_compra()
                    {
                        val parametros = FiltroComprasPersonaConCreditosPresentesOFuturos(personaCreada.id!!, fechaHoraActual)
                        val entidadACrear = darInstanciaEntidadValida().run {
                            copiar(
                                    creditosPaquetes = listOf(
                                            CreditoPaquete(
                                                    paqueteCreado.id!!,
                                                    "código externo paquete",
                                                    listOf(
                                                            creditoFondoConNulos.copiar(validoDesde = fechaHoraActual),
                                                            creditoFondoSinNulos.copiar(validoDesde = fechaHoraActual.minusDays(2), validoHasta = fechaHoraActual.minusDays(1))
                                                          )
                                                          ),
                                            CreditoPaquete(
                                                    paqueteCreado.id!!,
                                                    "código externo paquete",
                                                    listOf(
                                                            creditoFondoSinNulos.copiar(validoDesde = fechaHoraActual.minusDays(2), validoHasta = fechaHoraActual.minusDays(1)),
                                                            creditoFondoSinNulos.copiar(validoDesde = fechaHoraActual.minusDays(2), validoHasta = fechaHoraActual.minusDays(1))
                                                          )
                                                          )
                                                             ),
                                    creditosFondos = listOf()
                                  )
                        }
                        val entidadCreada = crearCompraYMarcarComoFinalizada(entidadACrear)
                        val listadoEsperado = listOf(entidadCreada)

                        val listadoConsultado = repositorioComprasDeUnaPersona.listarSegunParametros(idClientePruebas, parametros).toList()

                        assertEquals(listadoEsperado, listadoConsultado)
                    }

                    @TestConMultiplesDAO
                    fun fecha_mayor_a_la_del_credito_retorna_lista_vacia()
                    {
                        val parametros = FiltroComprasPersonaConCreditosPresentesOFuturos(personaCreada.id!!, fechaHoraActual.with(LocalTime.MAX).plusNanos(1))
                        (1..3).map {
                            crearCompraYMarcarComoFinalizada(darInstanciaEntidadValidaConMultiplesCreditosConIdAleatorio(it))
                        }

                        val listadoConsultado = repositorioComprasDeUnaPersona.listarSegunParametros(idClientePruebas, parametros).toList()
                        assertTrue(listadoConsultado.isEmpty())
                    }

                    @TestConMultiplesDAO
                    fun id_persona_diferente_a_la_de_los_creditos_retorna_lista_vacia()
                    {
                        val parametros = FiltroComprasPersonaConCreditosPresentesOFuturos(personaCreada.id!! + 100, fechaHoraActual)
                        (1..3).map {
                            crearCompraYMarcarComoFinalizada(darInstanciaEntidadValidaConMultiplesCreditosConIdAleatorio(it))
                        }

                        val listadoConsultado = repositorioComprasDeUnaPersona.listarSegunParametros(idClientePruebas, parametros).toList()
                        assertTrue(listadoConsultado.none())
                    }
                }

                @TestConMultiplesDAO
                fun lanza_excepcion_EsquemaNoExiste_si_se_consulta_listado_de_entidades_con_id_cliente_inexistente()
                {
                    val parametros = FiltroComprasPersonaConCreditosPresentesOFuturos(personaCreada.id!!, fechaHoraActual)
                    assertThrows<EsquemaNoExiste> { repositorioComprasDeUnaPersona.listarSegunParametros(idClientePruebas + 100, parametros) }
                }

                @TestConMultiplesDAO
                fun compras_marcadas_como_terminadas_en_otro_cliente_retorna_lista_vacia()
                {
                    val parametros = FiltroComprasPersonaConCreditosPresentesOFuturos(personaCreada.id!!, fechaHoraActual)
                    val entidadCreada = repositorio.crear(idClientePruebas, darInstanciaEntidadValida())
                    val nuevoValor = EntidadTransaccional.CampoCreacionTerminada<Compra>(true)
                    repositorio.actualizarCamposIndividuales(
                            idClientePruebas,
                            entidadCreada.id,
                            mapOf<String, CampoModificable<Compra, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                            )

                    ejecutarConClienteAlternativo {
                        val listadoConsultado = repositorioComprasDeUnaPersona.listarSegunParametros(it.id!!, parametros)
                        Assertions.assertTrue(listadoConsultado.none())
                    }
                }
            }
        }

        @Nested
        inner class ConsultarPersonasDeUnaCompra
        {
            @TestConMultiplesDAO
            fun listado_de_entidades_sin_entidades_retorna_lista_vacia()
            {
                val parametros = NumeroTransaccionPago(darInstanciaEntidadValida().pagos.first().numeroDeTransaccionPOS)
                val listadoConsultado = repositorioPersonasDeUnaCompra.listarSegunParametros(idClientePruebas, parametros)

                assertTrue(listadoConsultado.none())
            }

            @TestConMultiplesDAO
            fun listado_de_entidades_sin_marcar_compras_como_finalizadas_retorna_lista_con_persona_correcta_con_cualquier_numero_de_transaccion()
            {
                val entidadPrueba =
                        repositorio.crear(
                                idClientePruebas,
                                darInstanciaEntidadValidaConMultiplesCreditos()
                                         )
                entidadPrueba.pagos.forEach {
                    val parametros = NumeroTransaccionPago(it.numeroDeTransaccionPOS)
                    assertEquals(listOf(personaCreada), repositorioPersonasDeUnaCompra.listarSegunParametros(idClientePruebas, parametros).toList())
                }
            }

            @TestConMultiplesDAO
            fun listado_de_entidades_con_numero_transaccion_diferente_retorna_lista_vacia()
            {
                repositorio.crear(
                        idClientePruebas,
                        darInstanciaEntidadValidaConMultiplesCreditos()
                                 )
                val parametros = NumeroTransaccionPago("Numero transaccion inexistente")
                val listadoConsultado = repositorioPersonasDeUnaCompra.listarSegunParametros(idClientePruebas, parametros)

                assertTrue(listadoConsultado.none())
            }

            @TestConMultiplesDAO
            fun listado_de_entidades_marcando_compras_como_finalizadas_credito_retorna_lista_con_persona_correcta_con_cualquier_numero_de_transaccion()
            {
                val entidadPrueba = repositorio.crear(
                        idClientePruebas,
                        darInstanciaEntidadValidaConMultiplesCreditos()
                                                     )
                val nuevoValor = EntidadTransaccional.CampoCreacionTerminada<Compra>(true)
                repositorio.actualizarCamposIndividuales(
                        idClientePruebas,
                        entidadPrueba.id,
                        mapOf<String, CampoModificable<Compra, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                        )
                entidadPrueba.pagos.forEach {
                    val parametros = NumeroTransaccionPago(it.numeroDeTransaccionPOS)
                    assertEquals(listOf(personaCreada), repositorioPersonasDeUnaCompra.listarSegunParametros(idClientePruebas, parametros).toList())
                }
            }

            @TestConMultiplesDAO
            fun listado_de_entidades_marcando_compras_como_finalizadas_y_usando_todos_los_creditos_en_reserva_lista_con_persona_correcta_con_cualquier_numero_de_transaccion()
            {
                val entidadPrueba = repositorio.crear(
                        idClientePruebas,
                        darInstanciaEntidadValidaConMultiplesCreditos()
                                                     )
                val nuevoValor = EntidadTransaccional.CampoCreacionTerminada<Compra>(true)
                repositorio.actualizarCamposIndividuales(
                        idClientePruebas,
                        entidadPrueba.id,
                        mapOf<String, CampoModificable<Compra, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                        )
                repositorioReservas.crear(idClientePruebas, darReservaACrearConsumiendoTodosLosCreditos(listOf(entidadPrueba)))
                entidadPrueba.pagos.forEach {
                    val parametros = NumeroTransaccionPago(it.numeroDeTransaccionPOS)
                    assertEquals(listOf(personaCreada), repositorioPersonasDeUnaCompra.listarSegunParametros(idClientePruebas, parametros).toList())
                }
            }

            @TestConMultiplesDAO
            fun listado_de_entidades_retorna_lista_con_personas_correctas_cuando_existen_multiples_personas()
            {
                val entidadPrueba =
                        repositorio.crear(
                                idClientePruebas,
                                darInstanciaEntidadValidaConMultiplesCreditosConMultiplesPersonas()
                                         )
                val listaEsperada = listOf(personaCreada, personaCreada2, personaCreada3, personaCreada4).sortedBy { it.id }
                entidadPrueba.pagos.forEach {
                    val parametros = NumeroTransaccionPago(it.numeroDeTransaccionPOS)
                    assertEquals(listaEsperada, repositorioPersonasDeUnaCompra.listarSegunParametros(idClientePruebas, parametros).toList().sortedBy { it.id })
                }
            }
        }

        @Nested
        inner class ActualizarParcialmente
        {
            private lateinit var entidadDePrueba: Compra

            @BeforeEach
            fun crearEntidadDePrueba()
            {
                entidadDePrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadValidaConMultiplesCreditos())
            }

            @TestConMultiplesDAO
            fun solo_se_actualizan_los_campos_necesarios()
            {
                val nuevoValor = EntidadTransaccional.CampoCreacionTerminada<Compra>(true)

                repositorio.actualizarCamposIndividuales(
                        idClientePruebas,
                        entidadDePrueba.id,
                        mapOf<String, CampoModificable<Compra, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                        )

                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.id)
                val entidadEsperada =
                        Compra(
                                entidadDePrueba.idCliente,
                                entidadDePrueba.nombreUsuario,
                                entidadDePrueba.uuid,
                                entidadDePrueba.tiempoCreacion,
                                nuevoValor.valor,
                                entidadDePrueba.creditosFondos,
                                entidadDePrueba.creditosPaquetes,
                                entidadDePrueba.pagos,
                                entidadDePrueba.fechaDeRealizacion
                              )

                assertEquals(entidadEsperada, entidadConsultada)
            }

            @TestConMultiplesDAO
            fun permite_mandar_a_false_creacion_terminada_si_estaba_en_true()
            {
                val valorTrue = EntidadTransaccional.CampoCreacionTerminada<Compra>(true)

                repositorio.actualizarCamposIndividuales(
                        idClientePruebas,
                        entidadDePrueba.id,
                        mapOf<String, CampoModificable<Compra, *>>(valorTrue.nombreCampo to valorTrue)
                                                        )
                val valorFalse = EntidadTransaccional.CampoCreacionTerminada<Compra>(false)

                repositorio.actualizarCamposIndividuales(
                        idClientePruebas,
                        entidadDePrueba.id,
                        mapOf<String, CampoModificable<Compra, *>>(valorFalse.nombreCampo to valorFalse)
                                                        )

                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.id)

                assertEquals(entidadDePrueba, entidadConsultada)
            }

            @TestConMultiplesDAO
            fun con_id_inexistente_lanza_excepcion_EntidadNoExiste()
            {
                val idDePrueba: String = entidadDePrueba.id + "inexistente"
                val nuevoValor = EntidadTransaccional.CampoCreacionTerminada<Compra>(true)

                assertThrows<EntidadNoExiste> {
                    repositorio.actualizarCamposIndividuales(
                            idClientePruebas,
                            idDePrueba,
                            mapOf<String, CampoModificable<Compra, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                            )
                }
            }

            @TestConMultiplesDAO
            fun con_id_existente_en_otro_cliente_lanza_excepcion_EntidadNoExiste()
            {
                val idDePrueba = entidadDePrueba.id
                val nuevoValor = EntidadTransaccional.CampoCreacionTerminada<Compra>(true)

                ejecutarConClienteAlternativo {
                    assertThrows<EntidadNoExiste> {
                        repositorio.actualizarCamposIndividuales(
                                it.id!!,
                                idDePrueba,
                                mapOf<String, CampoModificable<Compra, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                                )
                    }
                }
            }

            @TestConMultiplesDAO
            fun con_id_cliente_inexistente_lanza_excepcion_EsquemaNoExiste()
            {
                val idDePrueba = entidadDePrueba.id
                val nuevoValor = EntidadTransaccional.CampoCreacionTerminada<Compra>(true)

                assertThrows<EsquemaNoExiste> {
                    repositorio.actualizarCamposIndividuales(
                            idClientePruebas + 100,
                            idDePrueba,
                            mapOf<String, CampoModificable<Compra, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                            )
                }
            }
        }

        @Nested
        inner class Eliminar
        {
            private val entidadDePrueba: Compra by lazy {
                repositorio.crear(idClientePruebas, darInstanciaEntidadValidaConMultiplesCreditos())
            }

            @TestConMultiplesDAO
            fun si_existe_la_entidad_se_elimina_correctamente()
            {
                val resultadoEliminacion = repositorio.eliminarPorId(idClientePruebas, entidadDePrueba.id)
                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.id)

                assertTrue(resultadoEliminacion)
                assertNull(entidadConsultada)
            }

            @TestConMultiplesDAO
            fun se_eliminan_creditos_paquete_dao_correctamente()
            {
                val resultadoEliminacion = repositorio.eliminarPorId(idClientePruebas, entidadDePrueba.id)
                assertTrue(resultadoEliminacion)
                assertFalse(existeAlgunCreditoPaqueteDAO(idClientePruebas, configuracionRepositorios))
            }

            @TestConMultiplesDAO
            fun se_eliminan_creditos_fondo_dao_correctamente()
            {
                val resultadoEliminacion = repositorio.eliminarPorId(idClientePruebas, entidadDePrueba.id)
                assertTrue(resultadoEliminacion)
                assertFalse(existeAlgunCreditoFondoDAO(idClientePruebas, configuracionRepositorios))
            }

            @TestConMultiplesDAO
            fun se_eliminan_pagos_dao_correctamente()
            {
                val resultadoEliminacion = repositorio.eliminarPorId(idClientePruebas, entidadDePrueba.id)
                assertTrue(resultadoEliminacion)
                assertFalse(existeAlgunPagoDAO(idClientePruebas, configuracionRepositorios))
            }

            @TestConMultiplesDAO
            fun se_eliminan_compras_dao_correctamente()
            {
                val resultadoEliminacion = repositorio.eliminarPorId(idClientePruebas, entidadDePrueba.id)
                assertTrue(resultadoEliminacion)
                assertFalse(existeAlgunaCompraDAO(idClientePruebas, configuracionRepositorios))
            }

            @TestConMultiplesDAO
            fun con_id_existente_en_otro_cliente_retorna_false_y_no_elimina_entidad_existente()
            {
                ejecutarConClienteAlternativo {
                    val resultadoEliminacion = repositorio.eliminarPorId(it.id!!, entidadDePrueba.id)
                    assertFalse(resultadoEliminacion)
                }
                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.id)
                assertEquals(entidadDePrueba, entidadConsultada)
            }

            @TestConMultiplesDAO
            fun con_id_cliente_inexistente_lanza_excepcion_EsquemaNoExiste()
            {
                assertThrows<EsquemaNoExiste> { repositorio.eliminarPorId(idClientePruebas + 100, entidadDePrueba.id) }
            }

            @TestConMultiplesDAO
            fun si_no_existe_la_entidad_retorna_false_y_no_elimina_entidad_existente()
            {
                val resultadoEliminacion = repositorio.eliminarPorId(idClientePruebas, entidadDePrueba.id + "inexistente")
                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.id)

                assertFalse(resultadoEliminacion)
                assertEquals(entidadDePrueba, entidadConsultada)
            }

            @[Nested DisplayName("lanza LanzaErrorDeLlaveForanea y no elimina entidad si intenta borrar")]
            inner class LanzaErrorDeLlaveForanea
            {
                @Nested
                inner class ElUsuario
                {
                    private lateinit var otroUsuario: Usuario

                    @BeforeEach
                    fun crearUsuario()
                    {
                        otroUsuario = repositorioUsuarios.crear(idClientePruebas, Usuario.UsuarioParaCreacion(
                                Usuario.DatosUsuario(idClientePruebas, "Otro Usuario", "El nombre completo", "elotroemail@mail.com", true),
                                charArrayOf('1', '2', '3'),
                                setOf(Rol.RolParaCreacionDeUsuario(rolCreado.nombre))
                                                                                                             ))
                    }

                    @TestConMultiplesDAO
                    fun de_la_compra()
                    {
                        val otraEntidadPrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadValidaSegunNombreUsuario(otroUsuario.datosUsuario.usuario))
                        assertThrows<ErrorDeLlaveForanea> {
                            repositorioUsuarios.eliminarPorId(idClientePruebas, otroUsuario.datosUsuario.usuario)
                        }

                        val entidadConsultada = repositorio.buscarPorId(idClientePruebas, otraEntidadPrueba.id)
                        assertEquals(otraEntidadPrueba, entidadConsultada)
                    }

                    @TestConMultiplesDAO
                    fun de_un_credito_individual()
                    {
                        val entidadACrear = darInstanciaEntidadValidaConMultiplesCreditos().run {
                            copiar(creditosFondos = creditosFondos.map {
                                it.copiar(nombreUsuario = otroUsuario.datosUsuario.usuario)
                            })
                        }
                        val otraEntidadPrueba = repositorio.crear(idClientePruebas, entidadACrear)
                        assertThrows<ErrorDeLlaveForanea> {
                            repositorioUsuarios.eliminarPorId(idClientePruebas, otroUsuario.datosUsuario.usuario)
                        }

                        val entidadConsultada = repositorio.buscarPorId(idClientePruebas, otraEntidadPrueba.id)
                        assertEquals(otraEntidadPrueba, entidadConsultada)
                    }

                    @TestConMultiplesDAO
                    fun de_un_credito_paquete()
                    {
                        val entidadACrear = darInstanciaEntidadValidaConMultiplesCreditos().run {
                            copiar(creditosPaquetes = creditosPaquetes.map {
                                it.copiar(creditosFondos = creditosFondos.map {
                                    it.copiar(nombreUsuario = otroUsuario.datosUsuario.usuario)
                                })
                            })
                        }
                        val otraEntidadPrueba = repositorio.crear(idClientePruebas, entidadACrear)
                        assertThrows<ErrorDeLlaveForanea> {
                            repositorioUsuarios.eliminarPorId(idClientePruebas, otroUsuario.datosUsuario.usuario)
                        }

                        val entidadConsultada = repositorio.buscarPorId(idClientePruebas, otraEntidadPrueba.id)
                        assertEquals(otraEntidadPrueba, entidadConsultada)
                    }
                }

                @Nested
                inner class ElImpuesto
                {
                    private lateinit var otroImpuesto: Impuesto

                    @BeforeEach
                    fun crearImpuesto()
                    {
                        otroImpuesto = repositorioImpuestos.crear(idClientePruebas, Impuesto(0, null, "Otro impuesto", Decimal(19)))
                    }

                    @TestConMultiplesDAO
                    fun de_un_credito_individual()
                    {
                        val entidadACrear = darInstanciaEntidadValidaConMultiplesCreditos().run {
                            copiar(creditosFondos = creditosFondos.map { it.copiar(idImpuestoPagado = otroImpuesto.id!!) })
                        }
                        val otraEntidadPrueba = repositorio.crear(idClientePruebas, entidadACrear)
                        assertThrows<ErrorDeLlaveForanea> {
                            repositorioImpuestos.eliminarPorId(idClientePruebas, otroImpuesto.id!!)
                        }

                        val entidadConsultada = repositorio.buscarPorId(idClientePruebas, otraEntidadPrueba.id)
                        assertEquals(otraEntidadPrueba, entidadConsultada)
                    }

                    @TestConMultiplesDAO
                    fun de_un_credito_de_paquete()
                    {
                        val entidadACrear = darInstanciaEntidadValidaConMultiplesCreditos().run {
                            copiar(creditosPaquetes = creditosPaquetes.map { it.copiar(creditosFondos = creditosFondos.map { it.copiar(idImpuestoPagado = otroImpuesto.id!!) }) })
                        }
                        val otraEntidadPrueba = repositorio.crear(idClientePruebas, entidadACrear)
                        assertThrows<ErrorDeLlaveForanea> {
                            repositorioImpuestos.eliminarPorId(idClientePruebas, otroImpuesto.id!!)
                        }

                        val entidadConsultada = repositorio.buscarPorId(idClientePruebas, otraEntidadPrueba.id)
                        assertEquals(otraEntidadPrueba, entidadConsultada)
                    }
                }

                @Nested
                inner class ElFondo
                {
                    private lateinit var otroFondo: Fondo<*>

                    @BeforeEach
                    fun crearFondo()
                    {
                        otroFondo = repositorioMonedas.crear(
                                idClientePruebas,
                                Dinero(
                                        idClientePruebas,
                                        null,
                                        "Otra moneda pruebas",
                                        true,
                                        false,
                                        false,
                                        Precio(Decimal.UNO, impuestoCreado.id!!),
                                        "el código externo de prueba dinero"
                                      ))
                    }

                    @TestConMultiplesDAO
                    fun de_un_credito_individual()
                    {
                        val entidadACrear = darInstanciaEntidadValidaConMultiplesCreditos().run {
                            copiar(creditosFondos = creditosFondos.map { it.copiar(idFondoComprado = otroFondo.id!!) })
                        }
                        val otraEntidadPrueba = repositorio.crear(idClientePruebas, entidadACrear)
                        assertThrows<ErrorDeLlaveForanea> {
                            repositorioMonedas.eliminarPorId(idClientePruebas, otroFondo.id!!)
                        }

                        val entidadConsultada = repositorio.buscarPorId(idClientePruebas, otraEntidadPrueba.id)
                        assertEquals(otraEntidadPrueba, entidadConsultada)
                    }

                    @TestConMultiplesDAO
                    fun de_un_credito_de_paquete()
                    {
                        val entidadACrear = darInstanciaEntidadValidaConMultiplesCreditos().run {
                            copiar(creditosPaquetes = creditosPaquetes.map {
                                it.copiar(creditosFondos = creditosFondos.map {
                                    it.copiar(idFondoComprado = otroFondo.id!!)
                                })
                            })
                        }
                        val otraEntidadPrueba = repositorio.crear(idClientePruebas, entidadACrear)
                        assertThrows<ErrorDeLlaveForanea> {
                            repositorioMonedas.eliminarPorId(idClientePruebas, otroFondo.id!!)
                        }

                        val entidadConsultada = repositorio.buscarPorId(idClientePruebas, otraEntidadPrueba.id)
                        assertEquals(otraEntidadPrueba, entidadConsultada)
                    }
                }

                @Nested
                inner class LaPersona
                {
                    private lateinit var otraPersona: Persona

                    @BeforeEach
                    fun crearPersona()
                    {
                        otraPersona = repositorioPersonas.crear(
                                idClientePruebas,
                                Persona(
                                        idClientePruebas,
                                        null,
                                        "Persona prueba",
                                        Persona.TipoDocumento.CC,
                                        "12345",
                                        Persona.Genero.DESCONOCIDO,
                                        LocalDate.now(ZONA_HORARIA_POR_DEFECTO),
                                        Persona.Categoria.D,
                                        Persona.Afiliacion.COTIZANTE,
                                        null,
                                        "empresa",
                                        "0",
                                        Persona.Tipo.NO_AFILIADO
                                       )
                                                               )
                    }

                    @TestConMultiplesDAO
                    fun de_un_credito_individual()
                    {
                        val entidadACrear = darInstanciaEntidadValidaConMultiplesCreditos().run {
                            copiar(creditosFondos = creditosFondos.map { it.copiar(idPersonaDueña = otraPersona.id!!) })
                        }
                        val otraEntidadPrueba = repositorio.crear(idClientePruebas, entidadACrear)
                        assertThrows<ErrorDeLlaveForanea> {
                            repositorioPersonas.eliminarPorId(idClientePruebas, otraPersona.id!!)
                        }

                        val entidadConsultada = repositorio.buscarPorId(idClientePruebas, otraEntidadPrueba.id)
                        assertEquals(otraEntidadPrueba, entidadConsultada)
                    }

                    @TestConMultiplesDAO
                    fun de_un_credito_de_paquete()
                    {
                        val entidadACrear = darInstanciaEntidadValidaConMultiplesCreditos().run {
                            copiar(creditosPaquetes = creditosPaquetes.map {
                                it.copiar(creditosFondos = creditosFondos.map {
                                    it.copiar(idPersonaDueña = otraPersona.id!!)
                                })
                            })
                        }
                        val otraEntidadPrueba = repositorio.crear(idClientePruebas, entidadACrear)
                        assertThrows<ErrorDeLlaveForanea> {
                            repositorioPersonas.eliminarPorId(idClientePruebas, otraPersona.id!!)
                        }

                        val entidadConsultada = repositorio.buscarPorId(idClientePruebas, otraEntidadPrueba.id)
                        assertEquals(otraEntidadPrueba, entidadConsultada)
                    }
                }

                @Nested
                inner class LaUbicacion
                {
                    private lateinit var otraUbicacion: Ubicacion

                    @BeforeEach
                    fun crearUbicacion()
                    {
                        otraUbicacion = repositorioUbicaciones.crear(
                                idClientePruebas,
                                Ubicacion(
                                        idClientePruebas,
                                        null,
                                        "Otra Ubicacion madre",
                                        Ubicacion.Tipo.PROPIEDAD,
                                        Ubicacion.Subtipo.POS,
                                        null,
                                        linkedSetOf()
                                         )
                                                                    )
                    }

                    @TestConMultiplesDAO
                    fun de_un_credito_individual()
                    {
                        val entidadACrear = darInstanciaEntidadValidaConMultiplesCreditos().run {
                            copiar(creditosFondos = creditosFondos.map { it.copiar(idUbicacionCompra = otraUbicacion.id!!) })
                        }
                        val otraEntidadPrueba = repositorio.crear(idClientePruebas, entidadACrear)
                        assertThrows<ErrorDeLlaveForanea> {
                            repositorioUbicaciones.eliminarPorId(idClientePruebas, otraUbicacion.id!!)
                        }

                        val entidadConsultada = repositorio.buscarPorId(idClientePruebas, otraEntidadPrueba.id)
                        assertEquals(otraEntidadPrueba, entidadConsultada)
                    }

                    @TestConMultiplesDAO
                    fun de_un_credito_de_paquete()
                    {
                        val entidadACrear = darInstanciaEntidadValidaConMultiplesCreditos().run {
                            copiar(creditosPaquetes = creditosPaquetes.map { it.copiar(creditosFondos = creditosFondos.map { it.copiar(idUbicacionCompra = otraUbicacion.id!!) }) })
                        }
                        val otraEntidadPrueba = repositorio.crear(idClientePruebas, entidadACrear)
                        assertThrows<ErrorDeLlaveForanea> {
                            repositorioUbicaciones.eliminarPorId(idClientePruebas, otraUbicacion.id!!)
                        }

                        val entidadConsultada = repositorio.buscarPorId(idClientePruebas, otraEntidadPrueba.id)
                        assertEquals(otraEntidadPrueba, entidadConsultada)
                    }
                }

                @Nested
                inner class ElGrupoDeClientes
                {
                    private lateinit var otroGrupoClientes: GrupoClientes

                    @BeforeEach
                    fun crearGrupoClientes()
                    {
                        otroGrupoClientes = repositorioGrupoClientes.crear(idClientePruebas, grupoClientesCategoriaB)
                    }

                    @TestConMultiplesDAO
                    fun de_un_credito_individual()
                    {
                        val entidadACrear = darInstanciaEntidadValidaConMultiplesCreditos().run {
                            copiar(creditosFondos = creditosFondos.map {
                                it.copiar(idGrupoClientesPersona = otroGrupoClientes.id!!)
                            })
                        }
                        val otraEntidadPrueba = repositorio.crear(idClientePruebas, entidadACrear)
                        assertThrows<ErrorDeLlaveForanea> {
                            repositorioGrupoClientes.eliminarPorId(idClientePruebas, otroGrupoClientes.id!!)
                        }

                        val entidadConsultada = repositorio.buscarPorId(idClientePruebas, otraEntidadPrueba.id)
                        assertEquals(otraEntidadPrueba, entidadConsultada)
                    }

                    @TestConMultiplesDAO
                    fun de_un_credito_de_paquete()
                    {
                        val entidadACrear = darInstanciaEntidadValidaConMultiplesCreditos().run {
                            copiar(creditosPaquetes = creditosPaquetes.map {
                                it.copiar(creditosFondos = creditosFondos.map {
                                    it.copiar(idGrupoClientesPersona = otroGrupoClientes.id!!)
                                })
                            })
                        }
                        val otraEntidadPrueba = repositorio.crear(idClientePruebas, entidadACrear)
                        assertThrows<ErrorDeLlaveForanea> {
                            repositorioGrupoClientes.eliminarPorId(idClientePruebas, otroGrupoClientes.id!!)
                        }

                        val entidadConsultada = repositorio.buscarPorId(idClientePruebas, otraEntidadPrueba.id)
                        assertEquals(otraEntidadPrueba, entidadConsultada)
                    }
                }
            }
        }
    }
}