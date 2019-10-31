package co.smartobjects.persistencia.operativas.ordenes

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.fondos.Dinero
import co.smartobjects.entidades.fondos.Fondo
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.entidades.operativas.EntidadTransaccional
import co.smartobjects.entidades.operativas.compras.Compra
import co.smartobjects.entidades.operativas.compras.CreditoFondo
import co.smartobjects.entidades.operativas.compras.Pago
import co.smartobjects.entidades.operativas.ordenes.LoteDeOrdenes
import co.smartobjects.entidades.operativas.ordenes.Orden
import co.smartobjects.entidades.operativas.ordenes.Transaccion
import co.smartobjects.entidades.operativas.reservas.Reserva
import co.smartobjects.entidades.operativas.reservas.SesionDeManilla
import co.smartobjects.entidades.ubicaciones.consumibles.ConsumibleEnPuntoDeVenta
import co.smartobjects.persistencia.*
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorio
import co.smartobjects.persistencia.excepciones.ErrorCreacionActualizacionPorDuplicidad
import co.smartobjects.persistencia.excepciones.ErrorDeLlaveForanea
import co.smartobjects.persistencia.excepciones.ErrorEliminacionViolacionDeRestriccion
import co.smartobjects.persistencia.excepciones.EsquemaNoExiste
import co.smartobjects.persistencia.operativas.ClasePruebasEntidadesTransaccionales
import co.smartobjects.persistencia.operativas.compras.RepositorioCompras
import co.smartobjects.persistencia.operativas.compras.RepositorioComprasSQL
import co.smartobjects.persistencia.operativas.reservas.RepositorioReservas
import co.smartobjects.persistencia.operativas.reservas.RepositorioReservasSQL
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.junit.jupiter.api.*
import org.threeten.bp.ZonedDateTime
import kotlin.test.*

@DisplayName("OrdenDAO")
internal class OrdenDAOPruebas : ClasePruebasEntidadesTransaccionales()
{
    private val repositorioCompras: RepositorioCompras by lazy { RepositorioComprasSQL(configuracionRepositorios) }
    private val repositorioReservas: RepositorioReservas by lazy { RepositorioReservasSQL(configuracionRepositorios) }
    private val repositorio: RepositorioOrdenes by lazy { RepositorioOrdenesSQL(configuracionRepositorios) }
    private val repositorioOrdenesDeUnaSesion: RepositorioOrdenesDeUnaSesionDeManilla by lazy { RepositorioOrdenesDeUnaSesionDeManillaSQL(configuracionRepositorios) }

    override val creadoresRepositoriosUsados: List<CreadorRepositorio<*>> by lazy {
        super.creadoresRepositoriosUsados +
        listOf<CreadorRepositorio<*>>(
                repositorioCompras,
                repositorioReservas,
                repositorio
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
                "el código externo de prueba"
                                                         ))
    }


    private fun crearSesionesDeManillaValidas(): List<SesionDeManilla>
    {
        val creditosFondo =
                listOf(
                        CreditoFondo(
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
                                personaCreada.id!!,
                                fondoCreado.id!!,
                                "el código externo de prueba",
                                impuestoCreado.id!!,
                                "un-uuid",
                                ubicacionCreada.id!!,
                                grupoDeClientesCreado.id!!
                                    )
                      )
        val compraCreada = repositorioCompras.crear(
                idClientePruebas,
                Compra(idClientePruebas, usuarioCreado.datosUsuario.usuario, creditosFondo, listOf(), listOf(Pago(Decimal(1000), Pago.MetodoDePago.EFECTIVO, "12-3")), fechaHoraActual)
                                                   )

        repositorioCompras.actualizarCamposIndividuales(
                idClientePruebas,
                compraCreada.id,
                EntidadTransaccional.CampoCreacionTerminada<Compra>(true)
                    .let {
                        mapOf<String, CampoModificable<Compra, *>>(it.nombreCampo to it)
                    }
                                                       )

        val sesionDeManillaACrear =
                SesionDeManilla(
                        idClientePruebas,
                        null,
                        personaCreada.id!!,
                        null,
                        null,
                        null,
                        compraCreada.creditos.filter { it.idPersonaDueña == personaCreada.id!! }.map { it.id!! }.toSet()
                               )

        val reservaACrear =
                Reserva(
                        idClientePruebas,
                        usuarioCreado.datosUsuario.usuario,
                        listOf(sesionDeManillaACrear)
                       )

        val reservaCreada =
                repositorioReservas.crear(idClientePruebas, reservaACrear).also {
                    repositorioReservas.actualizarCamposIndividuales(
                            idClientePruebas,
                            it.id,
                            EntidadTransaccional.CampoCreacionTerminada<Reserva>(true)
                                .let {
                                    mapOf<String, CampoModificable<Reserva, *>>(it.nombreCampo to it)
                                }
                                                                    )
                }

        return reservaCreada.sesionesDeManilla
    }

    private fun darInstanciaEntidadValida(idDeSesionDeManilla: Long = crearSesionesDeManillaValidas().first().id!!)
            : LoteDeOrdenes
    {
        val transaccionDebito = Transaccion.Debito(
                idClientePruebas,
                null,
                usuarioCreado.datosUsuario.usuario,
                Decimal(1),
                grupoDeClientesCreado.id!!,
                "un id de dispositivo",
                ConsumibleEnPuntoDeVenta(ubicacionCreada.id!!, fondoCreado.id!!, "código externo fondo consumible")
                                                  )
        val transaccionCredito =
                Transaccion.Credito(
                        idClientePruebas,
                        null,
                        usuarioCreado.datosUsuario.usuario,
                        ubicacionCreada.id!!,
                        fondoCreado.id!!,
                        "código externo fondo",
                        Decimal(1),
                        grupoDeClientesCreado.id!!,
                        "un id de dispositivo",
                        ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO).minusDays(10),
                        ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO).plusDays(10)
                                   )

        val ordenes = List(2) {
            Orden(
                    idClientePruebas,
                    null,
                    idDeSesionDeManilla,
                    listOf(transaccionDebito, transaccionCredito),
                    ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
                 )
        }

        return LoteDeOrdenes(idClientePruebas, usuarioCreado.datosUsuario.usuario, ordenes)
    }

    private fun LoteDeOrdenes.mapOrdenes(mapDeOrdenes: (Int, Orden) -> Orden): LoteDeOrdenes
    {
        return copiar(ordenes = ordenes.mapIndexed(mapDeOrdenes))
    }

    private fun Orden.mapTransacciones(mapDeTransacciones: (Int, Transaccion) -> Transaccion): Orden
    {
        return copiar(transacciones = transacciones.mapIndexed(mapDeTransacciones))
    }


    @Nested
    inner class Crear
    {
        private lateinit var entidadDePrueba: LoteDeOrdenes

        @BeforeEach
        fun instanciarEntidadDePruebaYBorrarOrdenesYTransaccionesDeReserva()
        {
            entidadDePrueba = darInstanciaEntidadValida()

            borrarTodasLasOrdenesDAO(idClientePruebas, configuracionRepositorios)
        }

        @TestConMultiplesDAO
        fun retorna_misma_entidad_con_mismo_id_de_orden_y_id_creditos_asignado_en_creditos_por_bd()
        {
            val entidadCreada = repositorio.crear(idClientePruebas, entidadDePrueba)

            val entidadCreadaModificada =
                    entidadCreada.map {
                        assertNotNull(it.id)
                        for (transaccion in it.transacciones)
                        {
                            assertNotNull(transaccion.id)
                        }
                        it.copiar(id = null, transacciones = it.transacciones.map { it.copiar(id = null) })
                    }

            val entidadEsperada = entidadDePrueba.ordenes

            assertEquals(entidadEsperada.size, listarTodasLasOrdenesDAO(idClientePruebas, configuracionRepositorios).size)
            assertEquals(entidadEsperada.sumBy { it.transacciones.size }, listarTodasLasTransaccionesDAO(idClientePruebas, configuracionRepositorios).size)
            assertEquals(entidadEsperada, entidadCreadaModificada)
        }

        @TestConMultiplesDAO
        fun siempre_crea_las_ordenes_como_no_terminadas_sin_importar_si_se_envia_como_terminadas()
        {
            val entidadAInsertar = entidadDePrueba.copiar(creacionTerminada = true)

            repositorio.crear(idClientePruebas, entidadAInsertar)

            listarTodasLasOrdenesDAOSegunIdTransaccion(idClientePruebas, configuracionRepositorios, entidadAInsertar.id).forEach {
                assertFalse(it.creacionTerminada)
            }
        }

        @TestConMultiplesDAO
        fun se_ignora_el_id_de_las_ordenes_y_el_id_es_asignado_por_la_bd()
        {
            val entidadAInsertar = entidadDePrueba.let {
                it.mapOrdenes { i, orden ->
                    orden.copiar(id = 523L * (911 + i))
                }
            }

            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            fun List<Orden>.darIdsOrdenados() = map { it.id }.sortedBy { it }

            val idsIniciales = entidadAInsertar.ordenes.darIdsOrdenados()
            val idsObtenidos = entidadCreada.darIdsOrdenados()

            assertEquals(idsIniciales, idsObtenidos)
        }

        @TestConMultiplesDAO
        fun se_ignora_el_id_de_las_transacciones_y_el_id_es_asignado_por_la_bd()
        {
            val entidadAInsertar = entidadDePrueba.let {
                it.mapOrdenes { i, orden ->
                    val idsNuevos = List(orden.transacciones.size) { it + 523L * (911 + i) }
                    orden.mapTransacciones { j, transaccion -> transaccion.copiar(id = idsNuevos[j]) }
                }
            }

            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            fun List<Orden>.darIdsTransaccionesOrdenados() = flatMap { it.transacciones.map { it.id } }.sortedBy { it }

            val idsIniciales = entidadAInsertar.ordenes.darIdsTransaccionesOrdenados()
            val idsObtenidos = entidadCreada.darIdsTransaccionesOrdenados()

            assertNotEquals(idsIniciales, idsObtenidos)
        }

        @TestConMultiplesDAO
        fun utiliza_el_parametro_id_cliente_y_retorna_misma_entidad_con_id_cliente_correcto()
        {
            val entidadAInsertar = entidadDePrueba.run {
                val idClienteNuevo = idCliente + 10L
                copiar(
                        idCliente = idClienteNuevo,
                        ordenes = ordenes.map {
                            it.copiar(
                                    idCliente = idClienteNuevo,
                                    transacciones = it.transacciones.map { it.copiar(idCliente = idClienteNuevo) }
                                     )
                        }
                      )
            }

            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            val idsExtraidos = entidadCreada.flatMap { listOf(it.idCliente) + it.transacciones.map { it.idCliente } }.distinct()

            assertEquals(1, idsExtraidos.size)
            assertEquals(idsExtraidos.first(), idClientePruebas)
        }

        @TestConMultiplesDAO
        fun reemplaza_las_ordenes_cuando_se_recrea_con_el_mismo_id_y_no_esta_marcada_como_terminada()
        {
            repositorio.crear(idClientePruebas, entidadDePrueba)

            val ordenesARecrear = entidadDePrueba.ordenes.take(1)
            val entidadARecrear = entidadDePrueba.copiar(ordenes = ordenesARecrear)
            assertEquals(entidadDePrueba.id, entidadARecrear.id)

            val entidadesEnBdConIdsCambiados =
                    repositorio.crear(idClientePruebas, entidadARecrear)
                        .map { it.copiar(id = null, transacciones = it.transacciones.map { it.copiar(id = null) }) }
                        .toList()

            assertEquals(entidadARecrear.ordenes.size, listarTodasLasOrdenesDAO(idClientePruebas, configuracionRepositorios).size)
            assertEquals(entidadARecrear.ordenes.flatMap { it.transacciones }.size, listarTodasLasTransaccionesDAO(idClientePruebas, configuracionRepositorios).size)
            assertEquals(ordenesARecrear, entidadesEnBdConIdsCambiados)
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_ErrorCreacionActualizacionPorDuplicidad_al_intentar_crear_un_lote_de_ordenes_con_id_existente_marcado_como_terminada_y_no_edita_entidad_previa()
        {
            val entidadInicial = entidadDePrueba.copiar(ordenes = entidadDePrueba.ordenes.take(1))

            repositorio.crear(idClientePruebas, entidadInicial)

            repositorio.actualizarCamposIndividuales(
                    idClientePruebas,
                    EntidadTransaccional.CampoCreacionTerminada<LoteDeOrdenes>(true).let {
                        mapOf<String, CampoModificable<LoteDeOrdenes, *>>(it.nombreCampo to it)
                    },
                    IdTransaccionActualizacionTerminacionOrden(entidadDePrueba.id)
                                                    )

            val entidadesEsperadas = listarTodasLasOrdenesDAO(idClientePruebas, configuracionRepositorios)

            assertThrows<ErrorCreacionActualizacionPorDuplicidad> {
                repositorio.crear(idClientePruebas, entidadDePrueba.copiar(ordenes = entidadDePrueba.ordenes.takeLast(1)))
            }

            val entidadesConsultadas = listarTodasLasOrdenesDAO(idClientePruebas, configuracionRepositorios)

            assertEquals(entidadInicial.ordenes.size, entidadesConsultadas.size)
            assertEquals(entidadInicial.ordenes.flatMap { it.transacciones }.size, listarTodasLasTransaccionesDAO(idClientePruebas, configuracionRepositorios).size)
            assertEquals(entidadesEsperadas, entidadesConsultadas)
        }
    }

    @Nested
    inner class Consultar
    {
        @Nested
        inner class PorId
        {
            private lateinit var entidadExistente: Orden

            @BeforeEach
            private fun crearEntidadInicialYBorrarOrdenesDeReserva()
            {
                val entidadAInsertar = darInstanciaEntidadValida()
                borrarTodasLasOrdenesDAO(idClientePruebas, configuracionRepositorios)

                entidadExistente = repositorio.crear(idClientePruebas, entidadAInsertar).first()
            }

            @TestConMultiplesDAO
            fun existente_retorna_entidad_correcta()
            {
                val reservaEncontrada = repositorio.buscarPorId(idClientePruebas, entidadExistente.id!!)

                assertEquals(entidadExistente, reservaEncontrada)
            }

            @TestConMultiplesDAO
            fun no_existente_retorna_null()
            {
                val reservaEncontrada = repositorio.buscarPorId(idClientePruebas, entidadExistente.id!! + 12345678)

                assertNull(reservaEncontrada)
            }

            @TestConMultiplesDAO
            fun existente_en_otro_cliente_retorna_null()
            {
                ejecutarConClienteAlternativo {
                    val entidadConsultada = repositorio.buscarPorId(it.id!!, entidadExistente.id!!)
                    assertNull(entidadConsultada)
                }
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_EsquemaNoExiste_si_se_consulta_por_id_con_id_cliente_inexistente()
            {
                assertThrows<EsquemaNoExiste> { repositorio.buscarPorId(idClientePruebas + 100, 1) }
            }
        }

        @Nested
        inner class ListadoCompleto
        {
            private var idDeSesionDeManilla: Long = 0

            @BeforeEach
            fun crearidDeSesionDeManillaUnicoYBorrarOrdenesDeReserva()
            {
                idDeSesionDeManilla = crearSesionesDeManillaValidas().first().id!!

                borrarTodasLasOrdenesDAO(idClientePruebas, configuracionRepositorios)
            }

            @TestConMultiplesDAO
            fun retorna_entidades_correctas()
            {
                val entidadesPrueba = (1..3).map {
                    val aCrear = darInstanciaEntidadValida(idDeSesionDeManilla)
                    repositorio.crear(idClientePruebas, aCrear)
                }.flatMap { it }

                val listadoEsperado = entidadesPrueba.sortedBy { it.id }

                val listadoConsultado = repositorio.listar(idClientePruebas).toList()

                assertEquals(listadoEsperado.size, listadoConsultado.size)
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
                repositorio.crear(idClientePruebas, darInstanciaEntidadValida(idDeSesionDeManilla))

                ejecutarConClienteAlternativo {
                    val listadoConsultado = repositorio.listar(it.id!!)
                    assertTrue(listadoConsultado.none())
                }
            }
        }
    }

    @Nested
    inner class ConsultarOrdenesDeUnaSesionDeManilla
    {
        private var idDeSesionManillaPrueba: Long = 0

        @BeforeEach
        private fun crearEntidadInicialYBorrarOrdenesDeReserva()
        {
            idDeSesionManillaPrueba = crearSesionesDeManillaValidas().first().id!!
            borrarTodasLasOrdenesDAO(idClientePruebas, configuracionRepositorios)
        }

        @Nested
        inner class SinOrdenes
        {
            @TestConMultiplesDAO
            fun listado_de_entidades_sin_entidades_retorna_lista_vacia()
            {
                val parametros = IdSesionDeManillaParaConsultaOrdenes(idDeSesionManillaPrueba)

                val listadoConsultado = repositorioOrdenesDeUnaSesion.listarSegunParametros(idClientePruebas, parametros)

                assertTrue(listadoConsultado.none())
            }
        }

        @Nested
        inner class ConOrdenes
        {
            private lateinit var loteDeOrdenesPrueba: LoteDeOrdenes

            @BeforeEach
            private fun crearEntidadInicial()
            {
                loteDeOrdenesPrueba = darInstanciaEntidadValida(idDeSesionManillaPrueba)
                loteDeOrdenesPrueba = loteDeOrdenesPrueba.copiar(ordenes = repositorio.crear(idClientePruebas, loteDeOrdenesPrueba))
            }

            @TestConMultiplesDAO
            fun no_marcadas_como_terminadas_retorna_lista_vacia()
            {
                val parametros = IdSesionDeManillaParaConsultaOrdenes(idDeSesionManillaPrueba)
                val listadoConsultado = repositorioOrdenesDeUnaSesion.listarSegunParametros(idClientePruebas, parametros).toList()

                assertTrue(listadoConsultado.none())
            }

            @Nested
            inner class MarcadasComoTerminadas
            {
                @BeforeEach
                fun marcarOrdenesComoTerminadas()
                {
                    repositorio.actualizarCamposIndividuales(
                            idClientePruebas,
                            EntidadTransaccional.CampoCreacionTerminada<LoteDeOrdenes>(true).let {
                                mapOf<String, CampoModificable<LoteDeOrdenes, *>>(it.nombreCampo to it)
                            },
                            IdTransaccionActualizacionTerminacionOrden(loteDeOrdenesPrueba.id)
                                                            )
                }

                @TestConMultiplesDAO
                fun retorna_lista_correcta()
                {
                    val parametros = IdSesionDeManillaParaConsultaOrdenes(idDeSesionManillaPrueba)

                    val listadoEsperado = loteDeOrdenesPrueba.ordenes.filter { it.idSesionDeManilla == idDeSesionManillaPrueba }.sortedBy { it.id }

                    val listadoConsultado = repositorioOrdenesDeUnaSesion.listarSegunParametros(idClientePruebas, parametros).sortedBy { it.id }.toList()

                    assertEquals(listadoEsperado, listadoConsultado)
                }

                @TestConMultiplesDAO
                fun y_id_de_sesion_diferente_a_la_de_las_ordenes_retorna_lista_vacia()
                {
                    val parametros = IdSesionDeManillaParaConsultaOrdenes(idDeSesionManillaPrueba + 123123)

                    val listadoConsultado = repositorioOrdenesDeUnaSesion.listarSegunParametros(idClientePruebas, parametros)

                    assertTrue(listadoConsultado.none())
                }

                @TestConMultiplesDAO
                fun ejecutando_en_otro_cliente_retorna_lista_vacia()
                {
                    val parametros = IdSesionDeManillaParaConsultaOrdenes(idDeSesionManillaPrueba)

                    ejecutarConClienteAlternativo {
                        val listadoConsultado = repositorioOrdenesDeUnaSesion.listarSegunParametros(it.id!!, parametros)
                        assertTrue(listadoConsultado.none())
                    }
                }
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_EsquemaNoExiste_si_se_consulta_listado_de_entidades_con_id_cliente_inexistente()
            {
                val parametros = IdSesionDeManillaParaConsultaOrdenes(idDeSesionManillaPrueba)

                assertThrows<EsquemaNoExiste> {
                    repositorioOrdenesDeUnaSesion.listarSegunParametros(idClientePruebas + 100, parametros)
                }
            }
        }
    }

    @Nested
    inner class ActualizarCreacionTerminadaDeLoteDeOrdenes
    {
        private lateinit var entidadDePrueba: LoteDeOrdenes
        private lateinit var filtroAUsar: IdTransaccionActualizacionTerminacionOrden

        @BeforeEach
        fun crearEntidadDePruebaYBorrarOrdenesDeReserva()
        {
            val loteDeOrdenes = darInstanciaEntidadValida()

            borrarTodasLasOrdenesDAO(idClientePruebas, configuracionRepositorios)

            entidadDePrueba = loteDeOrdenes.copiar(ordenes = repositorio.crear(idClientePruebas, loteDeOrdenes))
            filtroAUsar = IdTransaccionActualizacionTerminacionOrden(entidadDePrueba.id)
        }

        @TestConMultiplesDAO
        fun solo_se_actualizan_los_campos_necesarios()
        {
            val entidadesEsperadas =
                    listarTodasLasOrdenesDAO(idClientePruebas, configuracionRepositorios).map {
                        if (it.idTransaccion == entidadDePrueba.id) it.copy(creacionTerminada = true) else it
                    }

            val nuevoValor = EntidadTransaccional.CampoCreacionTerminada<LoteDeOrdenes>(true)

            repositorio.actualizarCamposIndividuales(
                    idClientePruebas,
                    mapOf<String, CampoModificable<LoteDeOrdenes, *>>(nuevoValor.nombreCampo to nuevoValor),
                    filtroAUsar
                                                    )

            val entidadesConsultadas = listarTodasLasOrdenesDAO(idClientePruebas, configuracionRepositorios)

            assertEquals(entidadesEsperadas, entidadesConsultadas)
        }

        @TestConMultiplesDAO
        fun permite_actualizar_a_false_si_estaba_en_true()
        {
            val entidadesEsperadas =
                    listarTodasLasOrdenesDAO(idClientePruebas, configuracionRepositorios).map {
                        if (it.idTransaccion == entidadDePrueba.id) it.copy(creacionTerminada = false) else it
                    }

            val valorTrue = EntidadTransaccional.CampoCreacionTerminada<LoteDeOrdenes>(true)

            repositorio.actualizarCamposIndividuales(
                    idClientePruebas,
                    mapOf<String, CampoModificable<LoteDeOrdenes, *>>(valorTrue.nombreCampo to valorTrue),
                    filtroAUsar
                                                    )

            val valorFalse = EntidadTransaccional.CampoCreacionTerminada<LoteDeOrdenes>(false)

            repositorio.actualizarCamposIndividuales(
                    idClientePruebas,
                    mapOf<String, CampoModificable<LoteDeOrdenes, *>>(valorFalse.nombreCampo to valorFalse),
                    filtroAUsar
                                                    )

            val entidadesConsultadas = listarTodasLasOrdenesDAO(idClientePruebas, configuracionRepositorios)

            assertEquals(entidadesEsperadas, entidadesConsultadas)
        }

        @TestConMultiplesDAO
        fun para_filtros_que_no_generan_coincidencia_no_hace_nada()
        {
            val entidadesEsperadas = listarTodasLasOrdenesDAO(idClientePruebas, configuracionRepositorios)

            val nuevoValor = EntidadTransaccional.CampoCreacionTerminada<LoteDeOrdenes>(true)

            repositorio.actualizarCamposIndividuales(
                    idClientePruebas,
                    mapOf<String, CampoModificable<LoteDeOrdenes, *>>(nuevoValor.nombreCampo to nuevoValor),
                    IdTransaccionActualizacionTerminacionOrden(entidadDePrueba.id + "no-existe")
                                                    )

            val entidadesConsultadas = listarTodasLasOrdenesDAO(idClientePruebas, configuracionRepositorios)

            assertEquals(entidadesEsperadas, entidadesConsultadas)
        }

        @TestConMultiplesDAO
        fun con_id_existente_en_otro_cliente_lanza_excepcion_EntidadNoExiste()
        {
            ejecutarConClienteAlternativo {
                val entidadesEsperadas = listarTodasLasOrdenesDAO(idClientePruebas, configuracionRepositorios)

                val nuevoValor = EntidadTransaccional.CampoCreacionTerminada<LoteDeOrdenes>(true)
                repositorio.actualizarCamposIndividuales(
                        it.id!!,
                        mapOf<String, CampoModificable<LoteDeOrdenes, *>>(nuevoValor.nombreCampo to nuevoValor),
                        IdTransaccionActualizacionTerminacionOrden(entidadDePrueba.id + "no-existe")
                                                        )

                val entidadesConsultadas = listarTodasLasOrdenesDAO(idClientePruebas, configuracionRepositorios)

                assertEquals(entidadesEsperadas, entidadesConsultadas)
            }
        }

        @TestConMultiplesDAO
        fun con_id_cliente_inexistente_lanza_excepcion_EsquemaNoExiste()
        {
            val nuevoValor = EntidadTransaccional.CampoCreacionTerminada<LoteDeOrdenes>(true)

            assertThrows<EsquemaNoExiste> {
                repositorio.actualizarCamposIndividuales(
                        idClientePruebas + 100,
                        mapOf<String, CampoModificable<LoteDeOrdenes, *>>(nuevoValor.nombreCampo to nuevoValor),
                        filtroAUsar
                                                        )
            }
        }
    }

    @Nested
    inner class Eliminar
    {
        private lateinit var loteDePrueba: LoteDeOrdenes
        private lateinit var entidadDePrueba: Orden

        @BeforeEach
        fun crearEntidadInicialYBorrarOrdenesDeReserva()
        {
            loteDePrueba = darInstanciaEntidadValida()

            borrarTodasLasOrdenesDAO(idClientePruebas, configuracionRepositorios)

            loteDePrueba = loteDePrueba.copiar(ordenes = repositorio.crear(idClientePruebas, loteDePrueba))
            entidadDePrueba = loteDePrueba.ordenes.first()
        }

        @TestConMultiplesDAO
        fun si_existe_la_entidad_y_no_esta_terminada_la_transaccion_se_elimina_correctamente()
        {
            val resultadoEliminacion = repositorio.eliminarPorId(idClientePruebas, entidadDePrueba.id!!)
            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.id!!)

            assertTrue(resultadoEliminacion)
            assertNull(entidadConsultada)
        }

        @TestConMultiplesDAO
        fun se_eliminan_las_transacciones_correctamente()
        {
            val resultadoEliminacion = repositorio.eliminarPorId(idClientePruebas, entidadDePrueba.id!!)
            assertTrue(resultadoEliminacion)
            assertFalse(existeAlgunaTransaccionDAO(idClientePruebas, configuracionRepositorios) { it.ordenDAO.id == entidadDePrueba.id })
        }

        @TestConMultiplesDAO
        fun lanza_ErrorEliminacionViolacionDeRestriccion_si_la_entidad_se_encuentra_terminada()
        {
            repositorio.actualizarCamposIndividuales(
                    idClientePruebas,
                    EntidadTransaccional.CampoCreacionTerminada<LoteDeOrdenes>(true).let {
                        mapOf<String, CampoModificable<LoteDeOrdenes, *>>(it.nombreCampo to it)
                    },
                    IdTransaccionActualizacionTerminacionOrden(loteDePrueba.id)
                                                    )

            val excepcionLanzada = assertThrows<ErrorEliminacionViolacionDeRestriccion> {
                repositorio.eliminarPorId(idClientePruebas, entidadDePrueba.id!!)
            }

            assertEquals(Orden.NOMBRE_ENTIDAD, excepcionLanzada.entidad)

            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.id!!)

            assertEquals(entidadDePrueba, entidadConsultada)
        }

        @TestConMultiplesDAO
        fun con_id_existente_en_otro_cliente_retorna_false_y_no_elimina_entidad_existente()
        {
            ejecutarConClienteAlternativo {
                val resultadoEliminacion = repositorio.eliminarPorId(it.id!!, entidadDePrueba.id!!)
                assertFalse(resultadoEliminacion)
            }
            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.id!!)
            assertEquals(entidadDePrueba, entidadConsultada)
        }

        @TestConMultiplesDAO
        fun con_id_cliente_inexistente_lanza_excepcion_EsquemaNoExiste()
        {
            assertThrows<EsquemaNoExiste> {
                repositorio.eliminarPorId(idClientePruebas + 100, entidadDePrueba.id!!)
            }
        }

        @TestConMultiplesDAO
        fun si_no_existe_la_entidad_retorna_false_y_no_elimina_entidad_existente()
        {
            val resultadoEliminacion = repositorio.eliminarPorId(idClientePruebas, entidadDePrueba.id!! + 1000)
            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.id!!)

            assertFalse(resultadoEliminacion)
            assertEquals(entidadDePrueba, entidadConsultada)
        }

        @[Nested DisplayName("lanza LanzaErrorDeLlaveForanea y no elimina entidad si intenta borrar")]
        inner class LanzaErrorDeLlaveForanea
        {
            @AfterEach
            fun verificarQueLaEntidadSigaSiendoLaMisma()
            {
                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.id!!)
                assertEquals(entidadDePrueba, entidadConsultada)
            }

            @TestConMultiplesDAO
            fun el_usuario_asociado_a_cualquier_orden()
            {
                assertThrows<ErrorDeLlaveForanea> {
                    repositorioUsuarios.eliminarPorId(idClientePruebas, usuarioCreado.datosUsuario.usuario)
                }
            }

            @TestConMultiplesDAO
            fun una_sesion_manilla_asociada_a_una_orden()
            {
                val reservasExistentes = repositorioReservas.listar(idClientePruebas).toList()
                assertEquals(1, reservasExistentes.size)
                val idReserva = reservasExistentes.first().id

                repositorioReservas.actualizarCamposIndividuales(
                        idClientePruebas,
                        idReserva,
                        EntidadTransaccional.CampoCreacionTerminada<Reserva>(false).let {
                            mapOf<String, CampoModificable<Reserva, *>>(it.nombreCampo to it)
                        }
                                                                )

                assertThrows<ErrorDeLlaveForanea> {
                    repositorioReservas.eliminarPorId(idClientePruebas, idReserva)
                }
            }

            @TestConMultiplesDAO
            fun la_ubicacion_asociada_a_cualquier_transaccion()
            {
                assertThrows<ErrorDeLlaveForanea> {
                    repositorioUbicaciones.eliminarPorId(idClientePruebas, ubicacionCreada.id!!)
                }
            }

            @TestConMultiplesDAO
            fun el_fondo_asociado_a_cualquier_transaccion()
            {
                assertThrows<ErrorDeLlaveForanea> {
                    repositorioMonedas.eliminarPorId(idClientePruebas, fondoCreado.id!!)
                }
            }

            @TestConMultiplesDAO
            fun el_grupo_de_clientes_asociado_a_cualquier_transaccion()
            {
                assertThrows<ErrorDeLlaveForanea> {
                    repositorioGrupoClientes.eliminarPorId(idClientePruebas, grupoDeClientesCreado.id!!)
                }
            }
        }
    }
}