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
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.usuarios.Rol
import co.smartobjects.entidades.usuarios.Usuario
import co.smartobjects.persistencia.*
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorio
import co.smartobjects.persistencia.excepciones.*
import co.smartobjects.persistencia.operativas.ClasePruebasEntidadesTransaccionales
import co.smartobjects.persistencia.operativas.compras.RepositorioCompras
import co.smartobjects.persistencia.operativas.compras.RepositorioComprasSQL
import co.smartobjects.persistencia.operativas.ordenes.RepositorioOrdenes
import co.smartobjects.persistencia.operativas.ordenes.RepositorioOrdenesSQL
import co.smartobjects.persistencia.operativas.ordenes.TransaccionDAO
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.threeten.bp.LocalDate
import org.threeten.bp.ZonedDateTime
import kotlin.test.*

@DisplayName("ReservaDAO")
internal class ReservaDAOPruebas : ClasePruebasEntidadesTransaccionales()
{
    private val repositorioCompras: RepositorioCompras by lazy { RepositorioComprasSQL(configuracionRepositorios) }
    private val repositorioSesionesDeManilla: RepositorioDeSesionDeManilla by lazy { RepositorioDeSesionDeManillaSQL(configuracionRepositorios) }
    private val repositorio: RepositorioReservas by lazy { RepositorioReservasSQL(configuracionRepositorios) }
    private val repositorioOrdenes: RepositorioOrdenes by lazy { RepositorioOrdenesSQL(configuracionRepositorios) }

    override val creadoresRepositoriosUsados: List<CreadorRepositorio<*>> by lazy {
        super.creadoresRepositoriosUsados +
        listOf<CreadorRepositorio<*>>(
                repositorioCompras,
                repositorio,
                repositorioSesionesDeManilla,
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

    private val segundaPersona: Persona by lazy {
        repositorioPersonas.crear(idClientePruebas, Persona(
                idClientePruebas,
                null,
                personaCreada.nombreCompleto + " nuevo",
                Persona.TipoDocumento.CC,
                personaCreada.numeroDocumento + "789",
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

    private fun crearCreditosFondoParaPersonas(): List<CreditoFondo>
    {
        val creditosPrimeraPersona =
                listOf(
                        crearCreditoFondo(personaCreada.id!!, fondosCreados[0].id!!),
                        crearCreditoFondo(personaCreada.id!!, fondosCreados[1].id!!)
                      )

        val creditosSegundaPersona =
                listOf(
                        crearCreditoFondo(segundaPersona.id!!, fondosCreados[2].id!!),
                        crearCreditoFondo(segundaPersona.id!!, fondosCreados[3].id!!)
                      )

        return creditosPrimeraPersona + creditosSegundaPersona
    }

    private fun crearCompraYDarCreditos(marcarComoTerminada: Boolean, indice: Int = 1): List<CreditoFondo>
    {
        val creditosFondo = crearCreditosFondoParaPersonas()
        var compraCreada = repositorioCompras.crear(
                idClientePruebas,
                Compra(
                        idClientePruebas,
                        usuarioCreado.datosUsuario.usuario,
                        creditosFondo,
                        listOf(),
                        listOf(Pago(Decimal(1000), Pago.MetodoDePago.EFECTIVO, indice.toString())),
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

    private fun crearSesionesDeManillaSinActivar(personas: List<Persona>, creditosCreados: List<CreditoFondo>): List<SesionDeManilla>
    {
        return personas.map { persona ->
            SesionDeManilla(
                    idClientePruebas,
                    null,
                    persona.id!!,
                    null,
                    null,
                    null,
                    creditosCreados.filter { it.idPersonaDueña == persona.id!! }.map { it.id!! }.toSet()
                           )
        }
    }

    private fun darInstanciaEntidadValida(
            nombreUsuario: String = usuarioCreado.datosUsuario.usuario,
            creditosCreados: List<CreditoFondo> = listOf(),
            indice: Int = 1
                                         ): Reserva
    {
        val creditos = if (creditosCreados.isEmpty()) crearCompraYDarCreditos(true, indice) else creditosCreados

        return Reserva(idClientePruebas, nombreUsuario, crearSesionesDeManillaSinActivar(listOf(personaCreada, segundaPersona), creditos))
    }

    private fun Reserva.mapSesionesManilla(mapDeSesiones: (Int, SesionDeManilla) -> SesionDeManilla): Reserva
    {
        return copiar(sesionesDeManilla = sesionesDeManilla.mapIndexed(mapDeSesiones))
    }

    private fun Reserva.actualizarIdsSesionManilla(idsNuevos: List<Long>): Reserva
    {
        return mapSesionesManilla { i, sesionDeManilla -> sesionDeManilla.copiar(id = idsNuevos[i]) }
    }

    private fun Reserva.actualizarUuidTodasSesiones(vararg uuidsTags: ByteArray?): Reserva
    {
        return mapSesionesManilla { indice, sesionDeManilla -> sesionDeManilla.copiar(uuidTag = uuidsTags[indice % uuidsTags.size]) }
    }

    private fun Reserva.actualizarFechaActivacionTodasSesiones(fechaActivacion: ZonedDateTime?): Reserva
    {
        return mapSesionesManilla { _, sesionDeManilla -> sesionDeManilla.copiar(fechaActivacion = fechaActivacion) }
    }

    private fun Reserva.actualizarFechaDesactivacionTodasSesiones(fechaDesactivacion: ZonedDateTime?): Reserva
    {
        return mapSesionesManilla { _, sesionDeManilla -> sesionDeManilla.copiar(fechaDesactivacion = fechaDesactivacion) }
    }

    private fun Reserva.actualizarIdsDeCliente(idClienteNuevo: Long): Reserva
    {
        return copiar(
                idCliente = idClienteNuevo,
                sesionesDeManilla = sesionesDeManilla.map { it.copiar(idCliente = idClienteNuevo) }
                     )
    }

    @Nested
    inner class Crear
    {
        @TestConMultiplesDAO
        fun retorna_misma_entidad_con_mismo_id_de_reserva_y_id_sesiones_de_manilla_asignado_por_bd()
        {
            val entidadAInsertar = darInstanciaEntidadValida()

            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            val entidadEsperada = entidadAInsertar.actualizarIdsSesionManilla(entidadCreada.sesionesDeManilla.map { it.id!! })

            assertEquals(entidadEsperada, entidadCreada)
        }

        @TestConMultiplesDAO
        fun siempre_crea_la_reserva_como_no_terminada_sin_importar_si_se_envia_como_terminada()
        {
            val entidadAInsertar = darInstanciaEntidadValida().copiar(creacionTerminada = true)
            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            val entidadEsperada =
                    entidadAInsertar
                        .actualizarIdsSesionManilla(entidadCreada.sesionesDeManilla.map { it.id!! })
                        .copiar(creacionTerminada = false, numeroDeReserva = null)

            assertEquals(entidadEsperada, entidadCreada)
        }

        @TestConMultiplesDAO
        fun siempre_crea_la_reserva_sin_numero_de_reserva()
        {
            val entidadAInsertar = darInstanciaEntidadValida().copiar(numeroDeReserva = 123)

            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            val entidadEsperada =
                    entidadAInsertar
                        .actualizarIdsSesionManilla(entidadCreada.sesionesDeManilla.map { it.id!! })
                        .copiar(numeroDeReserva = null)

            assertEquals(entidadEsperada, entidadCreada)
        }

        @TestConMultiplesDAO
        fun siempre_crea_las_sesiones_de_manilla_sin_uuid()
        {
            val entidadAInsertar =
                    darInstanciaEntidadValida().apply {
                        val uuidsTagsDiferentes = sesionesDeManilla.mapIndexed { index, _ ->
                            byteArrayOf((index).toByte(), (index + 1).toByte(), (index + 2).toByte())
                        }

                        actualizarUuidTodasSesiones(*uuidsTagsDiferentes.toTypedArray())
                    }

            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            val entidadEsperada =
                    entidadAInsertar
                        .actualizarIdsSesionManilla(entidadCreada.sesionesDeManilla.map { it.id!! })
                        .actualizarUuidTodasSesiones(null)

            assertEquals(entidadEsperada, entidadCreada)
        }

        @TestConMultiplesDAO
        fun siempre_crea_las_sesiones_de_manilla_sin_fecha_de_activacion()
        {
            val entidadAInsertar =
                    darInstanciaEntidadValida()
                        .actualizarFechaActivacionTodasSesiones(fechaHoraActual)

            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            val entidadEsperada =
                    entidadAInsertar
                        .actualizarIdsSesionManilla(entidadCreada.sesionesDeManilla.map { it.id!! })
                        .actualizarFechaActivacionTodasSesiones(null)

            assertEquals(entidadEsperada, entidadCreada)
        }

        @TestConMultiplesDAO
        fun siempre_crea_las_sesiones_de_manilla_sin_fecha_de_desactivacion()
        {
            val entidadAInsertar =
                    darInstanciaEntidadValida()
                        .actualizarFechaActivacionTodasSesiones(fechaHoraActual)
                        .actualizarFechaDesactivacionTodasSesiones(fechaHoraActual)

            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            val entidadEsperada =
                    entidadAInsertar
                        .actualizarIdsSesionManilla(entidadCreada.sesionesDeManilla.map { it.id!! })
                        .actualizarFechaDesactivacionTodasSesiones(null)
                        .actualizarFechaActivacionTodasSesiones(null)

            assertEquals(entidadEsperada, entidadCreada)
        }

        @TestConMultiplesDAO
        fun se_ignora_el_id_de_las_sesiones_manilla_y_el_id_es_asignado_por_la_bd()
        {
            val entidadAInsertar =
                    darInstanciaEntidadValida().let {
                        it.actualizarIdsSesionManilla(it.sesionesDeManilla.mapIndexed { index, _ -> index + 10L })
                    }

            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            val entidadEsperada = entidadAInsertar.actualizarIdsSesionManilla(entidadCreada.sesionesDeManilla.map { it.id!! })

            assertEquals(entidadEsperada, entidadCreada)
        }

        @TestConMultiplesDAO
        fun utiliza_el_parametro_id_cliente_y_retorna_misma_entidad_con_id_cliente_correcto()
        {
            val entidadAInsertar = darInstanciaEntidadValida().run { actualizarIdsDeCliente(idCliente + 10L) }

            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertEquals(idClientePruebas, entidadCreada.idCliente)
            val entidadEsperada =
                    entidadAInsertar
                        .actualizarIdsDeCliente(idClientePruebas)
                        .actualizarIdsSesionManilla(entidadCreada.sesionesDeManilla.map { it.id!! })

            assertEquals(entidadEsperada, entidadCreada)
        }

        @TestConMultiplesDAO
        fun reemplaza_la_reserva_cuando_se_recrea_con_el_mismo_id_y_no_esta_marcada_como_terminada()
        {
            val entidadInicial = darInstanciaEntidadValida()
            repositorio.crear(idClientePruebas, entidadInicial)

            val entidadARecrear = entidadInicial.copiar(sesionesDeManilla = entidadInicial.sesionesDeManilla.take(1))
            assertEquals(entidadInicial.id, entidadARecrear.id)

            val entidadCreada = repositorio.crear(idClientePruebas, entidadARecrear)

            val entidadActualEnBD = repositorio.buscarPorId(idClientePruebas, entidadInicial.id)
            val entidadEsperada = entidadARecrear.actualizarIdsSesionManilla(entidadCreada.sesionesDeManilla.map { it.id!! })

            assertEquals(entidadARecrear.sesionesDeManilla.size.toLong(), contarSesionDeManillaDAO(idClientePruebas, configuracionRepositorios))
            assertEquals(entidadARecrear.sesionesDeManilla.flatMap { it.idsCreditosCodificados }.size.toLong(), contarCreditoEnSesionDeManillaDAO(idClientePruebas, configuracionRepositorios))
            assertEquals(entidadEsperada, entidadCreada)
            assertEquals(entidadCreada, entidadActualEnBD)
        }

        @TestConMultiplesDAO
        fun se_marcan_solo_los_creditos_de_la_reserva_como_consumidos()
        {
            val creditosDeOtraCompra = crearCompraYDarCreditos(true, 2)

            val entidadCreada = repositorio.crear(idClientePruebas, darInstanciaEntidadValida())

            val idsCreditosAsociados = entidadCreada.sesionesDeManilla.flatMap { it.idsCreditosCodificados }.toSet()
            val creditosMarcadosEnBD = darCreditosSegunIds(configuracionRepositorios, idClientePruebas, idsCreditosAsociados)
            val creditosNoMarcadosEnBD = darCreditosSegunIds(configuracionRepositorios, idClientePruebas, creditosDeOtraCompra.map { it.id!! }.toSet())

            assertTrue(creditosMarcadosEnBD.all { it.consumido })
            assertTrue(creditosNoMarcadosEnBD.none { it.consumido })
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_ErrorCreacionActualizacionPorDuplicidad_al_intentar_crear_una_reserva_con_id_existente_marcada_como_terminada_y_no_edita_entidad_previa()
        {
            val entidadCreada = repositorio.crear(idClientePruebas, darInstanciaEntidadValida())

            val nuevoValor = EntidadTransaccional.CampoCreacionTerminada<Reserva>(true)
            repositorio.actualizarCamposIndividuales(
                    idClientePruebas,
                    entidadCreada.id,
                    mapOf<String, CampoModificable<Reserva, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                    )

            val creditos = crearCompraYDarCreditos(true, 2)
            val nuevaReserva =
                    Reserva(
                            idClientePruebas,
                            usuarioCreado.datosUsuario.usuario,
                            entidadCreada.uuid,
                            entidadCreada.tiempoCreacion,
                            false,
                            null,
                            crearSesionesDeManillaSinActivar(listOf(personaCreada, segundaPersona), creditos)
                           )

            val entidadEsperada = entidadCreada.copiar(creacionTerminada = true, numeroDeReserva = 1)

            assertThrows<ErrorCreacionActualizacionPorDuplicidad> {
                repositorio.crear(idClientePruebas, nuevaReserva)
            }

            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadCreada.id)

            assertEquals(entidadEsperada, entidadConsultada)
        }

        @[Nested DisplayName("lanza ErrorCreacionViolacionDeRestriccion si")]
        inner class LanzaErrorCreacionViolacionDeRestriccion
        {
            @TestConMultiplesDAO
            fun los_creditos_a_codificar_pertencen_a_otra_persona()
            {
                val creditosConDueñoErroneo =
                        crearCompraYDarCreditos(true)
                            .map {
                                val idPersonaErroneo = if (it.idPersonaDueña == personaCreada.id!!) segundaPersona.id!! else personaCreada.id!!
                                it.copiar(idPersonaDueña = idPersonaErroneo)
                            }

                val entidadAInsertar = darInstanciaEntidadValida(creditosCreados = creditosConDueñoErroneo)

                val excepcionLanzada = assertThrows<ErrorCreacionViolacionDeRestriccion> {
                    repositorio.crear(idClientePruebas, entidadAInsertar)
                }

                assertEquals(excepcionLanzada.entidad, SesionDeManilla.NOMBRE_ENTIDAD)
                assertEquals(excepcionLanzada.restriccion, "El crédito a codificar no le pertence a la persona")
            }

            @TestConMultiplesDAO
            fun la_persona_no_aparece_en_creditos_fondos_para_los_creditos_asociados()
            {
                val entidadCorrecta = darInstanciaEntidadValida()

                val personaInexistente =
                        repositorioPersonas.crear(idClientePruebas, Persona(
                                idClientePruebas,
                                null,
                                personaCreada.nombreCompleto + segundaPersona.nombreCompleto,
                                Persona.TipoDocumento.CC,
                                personaCreada.numeroDocumento + segundaPersona.numeroDocumento,
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

                val entidadConPersonaInexistente =
                        entidadCorrecta.copiar(
                                sesionesDeManilla = entidadCorrecta.sesionesDeManilla.map { it.copiar(idPersona = personaInexistente.id!!) }.drop(1)
                                              )

                val excepcionLanzada = assertThrows<ErrorCreacionViolacionDeRestriccion> {
                    repositorio.crear(idClientePruebas, entidadConPersonaInexistente)
                }

                assertEquals(excepcionLanzada.entidad, SesionDeManilla.NOMBRE_ENTIDAD)
                assertEquals(excepcionLanzada.restriccion, "El crédito a codificar no le pertence a la persona")
            }

            @TestConMultiplesDAO
            fun las_compras_asociadas_a_los_creditos_no_estan_finalizadas()
            {
                val creditosCreados = crearCompraYDarCreditos(false)
                val entidadAInsertar = darInstanciaEntidadValida(creditosCreados = creditosCreados)

                val excepcionLanzada = assertThrows<ErrorCreacionViolacionDeRestriccion> {
                    repositorio.crear(idClientePruebas, entidadAInsertar)
                }

                assertEquals(excepcionLanzada.entidad, SesionDeManilla.NOMBRE_ENTIDAD)
                assertEquals(excepcionLanzada.restriccion, "La compra asociada al crédito no ha finalizado")
            }

            @TestConMultiplesDAO
            fun el_credito_a_codificar_ya_fue_consumido()
            {
                val creditosCreados = crearCompraYDarCreditos(true)
                val entidadAInsertar = darInstanciaEntidadValida(creditosCreados = creditosCreados)

                marcarCreditoComoConsumido(configuracionRepositorios, idClientePruebas, creditosCreados.first().id!!)

                val excepcionLanzada = assertThrows<ErrorCreacionViolacionDeRestriccion> {
                    repositorio.crear(idClientePruebas, entidadAInsertar)
                }

                assertEquals(excepcionLanzada.entidad, SesionDeManilla.NOMBRE_ENTIDAD)
                assertEquals(excepcionLanzada.restriccion, "El crédito ya fue consumido")
            }
        }

        @[Nested DisplayName("lanza LanzaErrorDeLlaveForanea si")]
        inner class LanzaErrorDeLlaveForanea
        {
            @TestConMultiplesDAO
            fun el_usuario_de_la_reserva_no_existe()
            {
                val entidadCrear = darInstanciaEntidadValida(usuarioCreado.datosUsuario.usuario + "-inexistente")

                assertThrows<ErrorDeLlaveForanea> {
                    repositorio.crear(idClientePruebas, entidadCrear)
                }
            }

            @TestConMultiplesDAO
            fun la_persona_de_una_sesion_de_manilla_no_existe()
            {
                val entidadCrear =
                        darInstanciaEntidadValida()
                            .mapSesionesManilla { _, sesionDeManilla ->
                                sesionDeManilla.copiar(
                                        idPersona = sesionDeManilla.idPersona + 100L
                                                      )
                            }

                assertThrows<ErrorDeLlaveForanea> {
                    repositorio.crear(idClientePruebas, entidadCrear)
                }
            }

            @TestConMultiplesDAO
            fun alguno_de_los_creditos_a_codificar_no_existe()
            {
                val entidadCrear =
                        darInstanciaEntidadValida()
                            .mapSesionesManilla { _, sesionDeManilla ->
                                sesionDeManilla.copiar(
                                        idsCreditosInicialmenteCodificados = sesionDeManilla.idsCreditosCodificados.map { it + 100L }.toSet()
                                                      )
                            }

                assertThrows<ErrorDeLlaveForanea> {
                    repositorio.crear(idClientePruebas, entidadCrear)
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
        @Nested
        inner class PorId
        {
            private lateinit var entidadExistente: Reserva

            @BeforeEach
            private fun crearEntidadInicial()
            {
                entidadExistente = repositorio.crear(idClientePruebas, darInstanciaEntidadValida())
            }

            @TestConMultiplesDAO
            fun existente_retorna_entidad_correcta()
            {
                val reservaEncontrada = repositorio.buscarPorId(idClientePruebas, entidadExistente.id)

                assertEquals(entidadExistente, reservaEncontrada)
            }

            @TestConMultiplesDAO
            fun no_existente_retorna_null()
            {
                val reservaEncontrada = repositorio.buscarPorId(idClientePruebas, entidadExistente.id + "A")

                assertNull(reservaEncontrada)
            }

            @TestConMultiplesDAO
            fun existente_en_otro_cliente_retorna_null()
            {
                ejecutarConClienteAlternativo {
                    val entidadConsultada = repositorio.buscarPorId(it.id!!, entidadExistente.id)
                    assertNull(entidadConsultada)
                }
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_EsquemaNoExiste_si_se_consulta_por_id_con_id_cliente_inexistente()
            {
                assertThrows<EsquemaNoExiste> { repositorio.buscarPorId(idClientePruebas + 100, "un-id") }
            }
        }

        @Nested
        inner class ListadoCompleto
        {
            @TestConMultiplesDAO
            fun retorna_entidades_correctas()
            {
                val entidadesPrueba = (1..3).map {
                    repositorio.crear(idClientePruebas, darInstanciaEntidadValida(indice = it))
                }

                val listadoEsperado = entidadesPrueba.sortedBy { it.id }

                val listadoConsultado = repositorio.listar(idClientePruebas).toList()

                assertEquals(listadoEsperado, listadoConsultado.sortedBy { it.id })
            }

            @TestConMultiplesDAO
            fun sin_entidades_retorna_lista_vacia()
            {
                val listadoConsultado = repositorio.listar(idClientePruebas)

                assertTrue(listadoConsultado.none())
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
    }

    @Nested
    inner class ActualizarParcialmente
    {
        private lateinit var entidadDePrueba: Reserva
        @BeforeEach
        fun crearEntidadDePrueba()
        {
            entidadDePrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadValida())
        }

        @TestConMultiplesDAO
        fun solo_se_actualizan_los_campos_necesarios()
        {
            val nuevoValor = EntidadTransaccional.CampoCreacionTerminada<Reserva>(true)

            repositorio.actualizarCamposIndividuales(
                    idClientePruebas,
                    entidadDePrueba.id,
                    mapOf<String, CampoModificable<Reserva, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                    )

            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.id)

            val entidadEsperada =
                    Reserva(
                            entidadDePrueba.idCliente,
                            entidadDePrueba.nombreUsuario,
                            entidadDePrueba.uuid,
                            entidadDePrueba.tiempoCreacion,
                            nuevoValor.valor,
                            entidadConsultada!!.numeroDeReserva,
                            entidadDePrueba.sesionesDeManilla
                           )

            assertEquals(entidadEsperada, entidadConsultada)
        }

        @[Nested DisplayName("para el campo de creación terminada")]
        inner class AlActualizarCreacionTerminada
        {
            @TestConMultiplesDAO
            fun permite_actualizar_a_false_si_estaba_en_true()
            {
                val valorTrue = EntidadTransaccional.CampoCreacionTerminada<Reserva>(true)

                repositorio.actualizarCamposIndividuales(
                        idClientePruebas,
                        entidadDePrueba.id,
                        mapOf<String, CampoModificable<Reserva, *>>(valorTrue.nombreCampo to valorTrue)
                                                        )

                val valorFalse = EntidadTransaccional.CampoCreacionTerminada<Reserva>(false)

                repositorio.actualizarCamposIndividuales(
                        idClientePruebas,
                        entidadDePrueba.id,
                        mapOf<String, CampoModificable<Reserva, *>>(valorFalse.nombreCampo to valorFalse)
                                                        )

                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.id)

                val entidadEsperada =
                        Reserva(
                                entidadDePrueba.idCliente,
                                entidadDePrueba.nombreUsuario,
                                entidadDePrueba.uuid,
                                entidadDePrueba.tiempoCreacion,
                                false,
                                entidadConsultada!!.numeroDeReserva,
                                entidadDePrueba.sesionesDeManilla
                               )

                assertEquals(entidadEsperada, entidadConsultada)
            }

            @Nested
            inner class AlMarcarReservaComoTerminada
            {
                @BeforeEach
                fun marcarReservaComoTerminada()
                {
                    val nuevoValor = EntidadTransaccional.CampoCreacionTerminada<Reserva>(true)

                    repositorio.actualizarCamposIndividuales(
                            idClientePruebas,
                            entidadDePrueba.id,
                            mapOf<String, CampoModificable<Reserva, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                            )
                }

                @Nested
                inner class ParaCadaSesionDeManillaSeCreanOrdenes
                {
                    @TestConMultiplesDAO
                    fun con_usuario_correcto()
                    {
                        val ordenesDao = listarTodasLasOrdenesDAO(idClientePruebas, configuracionRepositorios)

                        assertTrue(ordenesDao.all { it.usuarioDAO.usuario == usuarioCreado.datosUsuario.usuario })
                    }

                    @TestConMultiplesDAO
                    fun una_por_sesion_de_manilla()
                    {
                        val idsSesionesDeManillaEsperados =
                                entidadDePrueba
                                    .sesionesDeManilla
                                    .map { it.id!! }
                                    .sortedBy { it }

                        val idsSesionesManillaEnOrdenes =
                                listarTodasLasOrdenesDAO(idClientePruebas, configuracionRepositorios)
                                    .map { it.sesionDeManillaDAO.id!! }
                                    .sortedBy { it }

                        assertEquals(idsSesionesDeManillaEsperados, idsSesionesManillaEnOrdenes)
                    }

                    @TestConMultiplesDAO
                    fun marcadas_como_finalizadas()
                    {
                        val ordenesDAO = listarTodasLasOrdenesDAO(idClientePruebas, configuracionRepositorios)

                        assertTrue(ordenesDAO.all { it.creacionTerminada })
                    }

                    @TestConMultiplesDAO
                    fun con_transacciones_con_valores_correctos()
                    {
                        val transaccionesDAO = listarTodasLasTransaccionesDAO(idClientePruebas, configuracionRepositorios)
                        val idDeOrdenConTransaccionesDAO =
                                transaccionesDAO
                                    .groupBy({ it.ordenDAO.id!! }, { it })


                        val idDeSesionDeManillaConCreditosFondoEnBD = mutableMapOf<Long, List<CreditoFondo>>()

                        entidadDePrueba.sesionesDeManilla.forEach {
                            idDeSesionDeManillaConCreditosFondoEnBD[it.id!!] =
                                    darCreditosSegunIds(configuracionRepositorios, idClientePruebas, it.idsCreditosCodificados)
                                        .sortedBy { it.idFondoComprado }
                        }

                        val ordenesDAOOrdenadas = listarTodasLasOrdenesDAO(idClientePruebas, configuracionRepositorios)

                        for (ordenDao in ordenesDAOOrdenadas)
                        {
                            val transaccionesAsociadas = idDeOrdenConTransaccionesDAO[ordenDao.id!!]!!.sortedBy { it.fondoDAO.id!! }
                            val creditosAsociadosAManilla = idDeSesionDeManillaConCreditosFondoEnBD[ordenDao.sesionDeManillaDAO.id!!]!!

                            assertEquals(transaccionesAsociadas.size, creditosAsociadosAManilla.size)

                            for (i in 0 until transaccionesAsociadas.size)
                            {
                                assertEquals(creditosAsociadosAManilla[i].nombreUsuario, transaccionesAsociadas[i].usuarioDAO.usuario)
                                assertEquals(creditosAsociadosAManilla[i].idUbicacionCompra, transaccionesAsociadas[i].ubicacionDAO?.id)
                                assertEquals(creditosAsociadosAManilla[i].idFondoComprado, transaccionesAsociadas[i].fondoDAO.id!!)
                                assertEquals(creditosAsociadosAManilla[i].cantidad, Decimal(transaccionesAsociadas[i].cantidad))
                                assertEquals(creditosAsociadosAManilla[i].idGrupoClientesPersona, transaccionesAsociadas[i].grupoClientesCompraDAO?.id)
                                assertEquals("Back-end", transaccionesAsociadas[i].idDispositivo)
                                assertEquals(creditosAsociadosAManilla[i].validoDesde, transaccionesAsociadas[i].validoDesde)
                                assertEquals(creditosAsociadosAManilla[i].validoHasta, transaccionesAsociadas[i].validoHasta)
                                assertEquals(TransaccionDAO.Tipo.CREDITO, transaccionesAsociadas[i].tipo)
                            }
                        }

                        assertTrue(true)
                    }
                }

                @TestConMultiplesDAO
                fun se_crea_numero_de_reserva()
                {
                    val reservaBuscada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.id)!!

                    assertNotNull(reservaBuscada.numeroDeReserva)
                }

                @TestConMultiplesDAO
                fun es_una_operacion_idempotente_en_ordenes_y_numero_de_reserva_despues_de_que_se_marco_la_primera_vez()
                {
                    val reservaAntesDeRepetirOperacion = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.id)!!

                    val ordenesAntesDeReptirOperacion =
                            listarTodasLasOrdenesDAO(idClientePruebas, configuracionRepositorios)
                                .sortedBy { it.id }

                    val transaccionesAntesDeReptirOperacion =
                            listarTodasLasTransaccionesDAO(idClientePruebas, configuracionRepositorios)
                                .sortedBy { it.id }


                    repeat(10) { marcarReservaComoTerminada() }

                    val reservaDespuesDeReptirLaOperacion = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.id)!!

                    assertEquals(reservaAntesDeRepetirOperacion, reservaDespuesDeReptirLaOperacion)

                    val ordenesDespuesDeRepetirOperacion =
                            listarTodasLasOrdenesDAO(idClientePruebas, configuracionRepositorios)
                                .sortedBy { it.id }

                    val transaccionesDespuesDeRepetirOperacion =
                            listarTodasLasTransaccionesDAO(idClientePruebas, configuracionRepositorios)
                                .sortedBy { it.id }

                    assertEquals(ordenesAntesDeReptirOperacion, ordenesDespuesDeRepetirOperacion)
                    assertEquals(transaccionesAntesDeReptirOperacion.size, transaccionesDespuesDeRepetirOperacion.size)
                    for (i in 0 until transaccionesAntesDeReptirOperacion.size)
                    {
                        val transaccionAntesDeOperacion = transaccionesAntesDeReptirOperacion[i]
                        // Se ajusta la fecha de realización porque no se completa
                        val transaccionDespuesDeOperacionAjustada =
                                transaccionesDespuesDeRepetirOperacion[i].let {
                                    it.copy(ordenDAO = it.ordenDAO.copy(fechaDeRealizacion = transaccionAntesDeOperacion.ordenDAO.fechaDeRealizacion))
                                }

                        assertEquals(transaccionAntesDeOperacion, transaccionDespuesDeOperacionAjustada)
                    }
                }

                @TestConMultiplesDAO
                fun funciona_para_una_reserva_con_creditos_con_ubicacion_nula()
                {
                    val creditosConUbicacionNula = crearCreditosFondoParaPersonas().map { it.copiar(idUbicacionCompra = null) }
                    val compraCreada = repositorioCompras.crear(
                            idClientePruebas,
                            Compra(
                                    idClientePruebas,
                                    usuarioCreado.datosUsuario.usuario,
                                    creditosConUbicacionNula,
                                    listOf(),
                                    listOf(Pago(Decimal(1000), Pago.MetodoDePago.EFECTIVO, "4356456")),
                                    fechaHoraActual
                                  )
                                                               )

                    val marcadoDeCompras = EntidadTransaccional.CampoCreacionTerminada<Compra>(true)

                    repositorioCompras.actualizarCamposIndividuales(
                            idClientePruebas,
                            compraCreada.id,
                            mapOf<String, CampoModificable<Compra, *>>(marcadoDeCompras.nombreCampo to marcadoDeCompras)
                                                                   )

                    val entidadAInsertar = darInstanciaEntidadValida(creditosCreados = compraCreada.creditos)

                    val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

                    val nuevoValor = EntidadTransaccional.CampoCreacionTerminada<Reserva>(true)

                    repositorio.actualizarCamposIndividuales(
                            idClientePruebas,
                            entidadCreada.id,
                            mapOf<String, CampoModificable<Reserva, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                            )

                    val entidadEsperada = entidadAInsertar.actualizarIdsSesionManilla(entidadCreada.sesionesDeManilla.map { it.id!! })

                    assertEquals(entidadEsperada, entidadCreada)
                }
            }
        }

        @TestConMultiplesDAO
        fun con_id_inexistente_lanza_excepcion_EntidadNoExiste()
        {
            val nuevoValor = EntidadTransaccional.CampoCreacionTerminada<Reserva>(true)

            assertThrows<EntidadNoExiste> {
                repositorio.actualizarCamposIndividuales(
                        idClientePruebas,
                        entidadDePrueba.id + "inexistente",
                        mapOf<String, CampoModificable<Reserva, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                        )
            }
        }

        @TestConMultiplesDAO
        fun con_id_existente_en_otro_cliente_lanza_excepcion_EntidadNoExiste()
        {
            val nuevoValor = EntidadTransaccional.CampoCreacionTerminada<Reserva>(true)

            ejecutarConClienteAlternativo {
                assertThrows<EntidadNoExiste> {
                    repositorio.actualizarCamposIndividuales(
                            it.id!!,
                            entidadDePrueba.id,
                            mapOf<String, CampoModificable<Reserva, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                            )
                }
            }
        }

        @TestConMultiplesDAO
        fun con_id_cliente_inexistente_lanza_excepcion_EsquemaNoExiste()
        {
            val nuevoValor = EntidadTransaccional.CampoCreacionTerminada<Reserva>(true)

            assertThrows<EsquemaNoExiste> {
                repositorio.actualizarCamposIndividuales(
                        idClientePruebas + 100,
                        entidadDePrueba.id,
                        mapOf<String, CampoModificable<Reserva, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                        )
            }
        }
    }

    @Nested
    inner class Eliminar
    {
        private lateinit var entidadDePrueba: Reserva

        @BeforeEach
        private fun crearEntidadInicial()
        {
            entidadDePrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadValida())
        }

        @TestConMultiplesDAO
        fun si_existe_la_entidad_y_no_esta_terminada_la_transaccion_se_elimina_correctamente()
        {
            val resultadoEliminacion = repositorio.eliminarPorId(idClientePruebas, entidadDePrueba.id)
            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.id)

            assertTrue(resultadoEliminacion)
            assertNull(entidadConsultada)
        }

        @TestConMultiplesDAO
        fun se_eliminan_las_sesiones_manilla_correctamente()
        {
            val resultadoEliminacion = repositorio.eliminarPorId(idClientePruebas, entidadDePrueba.id)
            assertTrue(resultadoEliminacion)
            assertFalse(existeAlgunaSesionDeManillaDAO(idClientePruebas, configuracionRepositorios))
        }

        @TestConMultiplesDAO
        fun se_eliminan_los_creditos_en_sesiones_manilla_correctamente()
        {
            val resultadoEliminacion = repositorio.eliminarPorId(idClientePruebas, entidadDePrueba.id)
            assertTrue(resultadoEliminacion)
            assertFalse(existeAlgunCreditoEnSesionManillaDAO(idClientePruebas, configuracionRepositorios))
        }

        @TestConMultiplesDAO
        fun marca_los_creditos_asociados_a_las_manillas_como_no_consumidos()
        {
            val idsCreditosAsociados = entidadDePrueba.sesionesDeManilla.flatMap { it.idsCreditosCodificados }.toSet()

            val resultadoEliminacion = repositorio.eliminarPorId(idClientePruebas, entidadDePrueba.id)
            assertTrue(resultadoEliminacion)

            val creditosDesmarcadosEnBD = darCreditosSegunIds(configuracionRepositorios, idClientePruebas, idsCreditosAsociados)

            assertTrue(creditosDesmarcadosEnBD.none { it.consumido })
        }

        @TestConMultiplesDAO
        fun lanza_ErrorEliminacionViolacionDeRestriccion_si_la_entidad_se_encuentra_terminada()
        {
            val nuevoValor = EntidadTransaccional.CampoCreacionTerminada<Reserva>(true)
            repositorio.actualizarCamposIndividuales(
                    idClientePruebas,
                    entidadDePrueba.id,
                    mapOf<String, CampoModificable<Reserva, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                    )
            entidadDePrueba = entidadDePrueba.copiar(creacionTerminada = true, numeroDeReserva = 1)

            val excepcionLanzada = assertThrows<ErrorEliminacionViolacionDeRestriccion> {
                repositorio.eliminarPorId(idClientePruebas, entidadDePrueba.id)
            }

            assertEquals(Reserva.NOMBRE_ENTIDAD, excepcionLanzada.entidad)

            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.id)

            assertEquals(entidadDePrueba, entidadConsultada)
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
            assertThrows<EsquemaNoExiste> {
                repositorio.eliminarPorId(idClientePruebas + 100, entidadDePrueba.id)
            }
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
            @TestConMultiplesDAO
            fun el_usuario_asociado_a_la_reserva()
            {
                val otroUsuario = repositorioUsuarios.crear(idClientePruebas, Usuario.UsuarioParaCreacion(
                        Usuario.DatosUsuario(idClientePruebas, "Otro Usuario", "El nombre completo", "elotroemail@mail.com", true),
                        charArrayOf('1', '2', '3'),
                        setOf(Rol.RolParaCreacionDeUsuario(rolCreado.nombre))
                                                                                                         ))

                val otraEntidadPrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadValida(otroUsuario.datosUsuario.usuario, indice = 2))
                assertThrows<ErrorDeLlaveForanea> {
                    repositorioUsuarios.eliminarPorId(idClientePruebas, otroUsuario.datosUsuario.usuario)
                }

                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, otraEntidadPrueba.id)
                assertEquals(otraEntidadPrueba, entidadConsultada)
            }

            @TestConMultiplesDAO
            fun la_persona_de_una_sesion_manilla()
            {
                assertThrows<ErrorDeLlaveForanea> {
                    repositorioPersonas.eliminarPorId(idClientePruebas, personaCreada.id!!)
                }

                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.id)
                assertEquals(entidadDePrueba, entidadConsultada)
            }

            @TestConMultiplesDAO
            fun la_compra_asociada_a_un_credito_de_una_sesion_manilla()
            {
                val comprasExistentes = repositorioCompras.listar(idClientePruebas).toList()
                assertEquals(1, comprasExistentes.size)

                val idCompraAsociada = comprasExistentes.first().id

                assertThrows<ErrorDeLlaveForanea> {
                    repositorioCompras.eliminarPorId(idClientePruebas, idCompraAsociada)
                }

                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.id)
                assertEquals(entidadDePrueba, entidadConsultada)
            }
        }
    }
}