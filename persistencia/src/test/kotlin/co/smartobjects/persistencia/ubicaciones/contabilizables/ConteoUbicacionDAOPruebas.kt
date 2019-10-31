package co.smartobjects.persistencia.ubicaciones.contabilizables

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.fondos.Dinero
import co.smartobjects.entidades.fondos.precios.GrupoClientes
import co.smartobjects.entidades.fondos.precios.Impuesto
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.entidades.operativas.EntidadTransaccional
import co.smartobjects.entidades.operativas.compras.Compra
import co.smartobjects.entidades.operativas.compras.CreditoFondo
import co.smartobjects.entidades.operativas.compras.Pago
import co.smartobjects.entidades.operativas.reservas.Reserva
import co.smartobjects.entidades.operativas.reservas.SesionDeManilla
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.ubicaciones.Ubicacion
import co.smartobjects.entidades.ubicaciones.contabilizables.ConteoUbicacion
import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.entidades.usuarios.Rol
import co.smartobjects.entidades.usuarios.Usuario
import co.smartobjects.persistencia.EntidadDAOBasePruebas
import co.smartobjects.persistencia.TestConMultiplesDAO
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorio
import co.smartobjects.persistencia.excepciones.ErrorCreacionActualizacionPorDuplicidad
import co.smartobjects.persistencia.excepciones.ErrorCreacionViolacionDeRestriccion
import co.smartobjects.persistencia.excepciones.ErrorDeLlaveForanea
import co.smartobjects.persistencia.excepciones.EsquemaNoExiste
import co.smartobjects.persistencia.fondos.grupoClientesCategoriaA
import co.smartobjects.persistencia.fondos.impuestoPruebasPorDefecto
import co.smartobjects.persistencia.fondos.monedas.RepositorioMonedas
import co.smartobjects.persistencia.fondos.monedas.RepositorioMonedasSQL
import co.smartobjects.persistencia.fondos.paquete.RepositorioPaquetes
import co.smartobjects.persistencia.fondos.paquete.RepositorioPaquetesSQL
import co.smartobjects.persistencia.fondos.precios.gruposclientes.RepositorioGrupoClientes
import co.smartobjects.persistencia.fondos.precios.gruposclientes.RepositorioGrupoClientesSQL
import co.smartobjects.persistencia.fondos.precios.impuestos.RepositorioImpuestos
import co.smartobjects.persistencia.fondos.precios.impuestos.RepositorioImpuestosSQL
import co.smartobjects.persistencia.listarTodasLasSesionDeManillaDAO
import co.smartobjects.persistencia.listarTodasLosConteosUbicacionDAO
import co.smartobjects.persistencia.operativas.compras.RepositorioCompras
import co.smartobjects.persistencia.operativas.compras.RepositorioComprasSQL
import co.smartobjects.persistencia.operativas.ordenes.RepositorioOrdenes
import co.smartobjects.persistencia.operativas.ordenes.RepositorioOrdenesSQL
import co.smartobjects.persistencia.operativas.reservas.*
import co.smartobjects.persistencia.personas.RepositorioPersonas
import co.smartobjects.persistencia.personas.RepositorioPersonasSQL
import co.smartobjects.persistencia.ubicaciones.RepositorioUbicaciones
import co.smartobjects.persistencia.ubicaciones.RepositorioUbicacionesSQL
import co.smartobjects.persistencia.usuarios.Hasher
import co.smartobjects.persistencia.usuarios.RepositorioUsuarios
import co.smartobjects.persistencia.usuarios.RepositorioUsuariosSQL
import co.smartobjects.persistencia.usuarios.roles.RepositorioRoles
import co.smartobjects.persistencia.usuarios.roles.RepositorioRolesSQL
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.threeten.bp.LocalDate
import org.threeten.bp.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


@DisplayName("ConteoUbicacionDAO")
internal class ConteoUbicacionDAOPruebas : EntidadDAOBasePruebas()
{
    private val hasherDummy = object : Hasher
    {
        override fun calcularHash(entrada: CharArray) = "un-hash-que-no-importa"
    }
    private val repositorioMonedas: RepositorioMonedas by lazy { RepositorioMonedasSQL(configuracionRepositorios) }
    private val repositorioPersonas: RepositorioPersonas by lazy { RepositorioPersonasSQL(configuracionRepositorios) }
    private val repositorioUbicaciones: RepositorioUbicaciones by lazy { RepositorioUbicacionesSQL(configuracionRepositorios) }
    private val repositorioImpuestos: RepositorioImpuestos by lazy { RepositorioImpuestosSQL(configuracionRepositorios) }
    private val repositorioGrupoClientes: RepositorioGrupoClientes by lazy { RepositorioGrupoClientesSQL(configuracionRepositorios) }
    private val repositorioPaquetes: RepositorioPaquetes by lazy { RepositorioPaquetesSQL(configuracionRepositorios) }
    private val repositorioRoles: RepositorioRoles by lazy { RepositorioRolesSQL(configuracionRepositorios) }
    private val repositorioUsuarios: RepositorioUsuarios by lazy { RepositorioUsuariosSQL(configuracionRepositorios, hasherDummy) }
    private val repositorioCompras: RepositorioCompras by lazy { RepositorioComprasSQL(configuracionRepositorios) }
    private val repositorioSesionesDeManilla: RepositorioDeSesionDeManilla by lazy { RepositorioDeSesionDeManillaSQL(configuracionRepositorios) }
    private val repositorioReservas: RepositorioReservas by lazy { RepositorioReservasSQL(configuracionRepositorios) }
    private val repositorioOrdenes: RepositorioOrdenes by lazy { RepositorioOrdenesSQL(configuracionRepositorios) }
    private val repositorio: RepositorioConteosUbicaciones by lazy { RepositorioConteosUbicacionesSQL(configuracionRepositorios) }

    override val creadoresRepositoriosUsados: List<CreadorRepositorio<*>> by lazy {
        listOf<CreadorRepositorio<*>>(
                repositorioImpuestos,
                repositorioGrupoClientes,
                repositorioMonedas,
                repositorioPaquetes,
                repositorioUbicaciones,
                repositorioPersonas,
                repositorioRoles,
                repositorioUsuarios,
                repositorioCompras,
                repositorioReservas,
                repositorioSesionesDeManilla,
                repositorioOrdenes,
                repositorio
                                     )
    }


    private val ubicacionConConteos by lazy {
        repositorioUbicaciones.crear(
                idClientePruebas,
                Ubicacion(
                        idClientePruebas,
                        null,
                        "Ubicacion con conteos",
                        Ubicacion.Tipo.PROPIEDAD,
                        Ubicacion.Subtipo.POS,
                        null,
                        linkedSetOf()
                         )
                                    )
    }

    private val ubicacionSinConteos by lazy {
        repositorioUbicaciones.crear(
                idClientePruebas,
                Ubicacion(
                        idClientePruebas,
                        null,
                        "Ubicacion sin conteos",
                        Ubicacion.Tipo.PROPIEDAD,
                        Ubicacion.Subtipo.POS,
                        null,
                        linkedSetOf()
                         )
                                    )
    }

    private val fechaHoraActual = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)!!
    private val grupoDeClientesCreado: GrupoClientes by lazy { repositorioGrupoClientes.crear(idClientePruebas, grupoClientesCategoriaA) }
    private val impuestoCreado: Impuesto by lazy { repositorioImpuestos.crear(idClientePruebas, impuestoPruebasPorDefecto) }
    private val personaCreada: Persona by lazy {
        repositorioPersonas.crear(idClientePruebas, Persona(
                idClientePruebas,
                null,
                "Persona prueba",
                Persona.TipoDocumento.CC,
                "123",
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

    private val usuarioCreado: Usuario by lazy {
        val permisos =
                setOf(
                        PermisoBack(idClientePruebas, "ElEndpoint", PermisoBack.Accion.POST),
                        PermisoBack(idClientePruebas, "ElEndpoint", PermisoBack.Accion.PUT)
                     )

        val rolCreado = repositorioRoles.crear(idClientePruebas, Rol(
                "ElRol",
                "Descripcion",
                permisos
                                                                    )
                                              )

        repositorioUsuarios.crear(idClientePruebas, Usuario.UsuarioParaCreacion(
                Usuario.DatosUsuario(
                        idClientePruebas,
                        "El Usuario",
                        "El nombre completo",
                        "elemail@mail.com",
                        true
                                    ),
                charArrayOf('1', '2', '3'),
                setOf(Rol.RolParaCreacionDeUsuario(rolCreado.nombre))
                                                                               )
                                 )
    }

    private val reservaCreada by lazy {
        val creditos = crearCompraYDarCreditos()

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
                                        creditos
                                            .filter { it.idPersonaDueña == personaCreada.id!! }
                                            .map { it.id!! }.toSet()
                                               )
                              )
                       )
                                 )
    }

    private fun crearCompraYDarCreditos(): List<CreditoFondo>
    {
        val fondoCreado =
                repositorioMonedas.crear(idClientePruebas, Dinero(
                        idClientePruebas,
                        null,
                        "Dinero",
                        true,
                        false,
                        false,
                        Precio(Decimal.UNO, impuestoCreado.id!!),
                        "el código externo de prueba"
                                                                 ))

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
                                ubicacionSinConteos.id!!,
                                grupoDeClientesCreado.id!!
                                    )
                      )
        var compraCreada = repositorioCompras.crear(
                idClientePruebas,
                Compra(
                        idClientePruebas,
                        usuarioCreado.datosUsuario.usuario,
                        creditosFondo,
                        listOf(),
                        listOf(Pago(Decimal(1000), Pago.MetodoDePago.EFECTIVO, "1")),
                        fechaHoraActual
                      )
                                                   )

        val nuevoValor = EntidadTransaccional.CampoCreacionTerminada<Compra>(true)

        repositorioCompras.actualizarCamposIndividuales(
                idClientePruebas,
                compraCreada.id,
                mapOf<String, CampoModificable<Compra, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                       )

        compraCreada = compraCreada.copiar(creacionTerminada = true)

        return compraCreada.creditos
    }

    private fun darInstanciaEntidadValida(idDeSesionDeManilla: Long): ConteoUbicacion
    {
        return ConteoUbicacion(idClientePruebas, ubicacionConConteos.id!!, idDeSesionDeManilla, fechaHoraActual)
    }


    @BeforeEach
    fun crearReservaDePruebas()
    {
        reservaCreada.id
    }


    @Nested
    inner class Crear
    {
        private lateinit var sesionDeManillaDAO: SesionDeManillaDAO

        @BeforeEach
        fun obtenerIdDeSesionDeManilla()
        {
            val sesionesManilla = listarTodasLasSesionDeManillaDAO(idClientePruebas, configuracionRepositorios)

            sesionDeManillaDAO = sesionesManilla.first()
        }


        @Nested
        inner class SinSesionDeManillaActiva
        {
            @TestConMultiplesDAO
            fun lanza_excepcion_ErrorCreacionViolacionDeRestriccion()
            {
                val entidadAInsertar = darInstanciaEntidadValida(sesionDeManillaDAO.id!!)

                assertThrows<ErrorCreacionViolacionDeRestriccion> { repositorio.crear(idClientePruebas, entidadAInsertar) }
            }
        }

        @Nested
        inner class ConSesionDeManillaActiva
        {
            @BeforeEach
            fun activarSesionDeManilla()
            {
                val nuevoValor = SesionDeManilla.CampoUuidTag(byteArrayOf(0, 1, 2, 3))

                repositorioSesionesDeManilla.actualizarCamposIndividuales(
                        idClientePruebas,
                        sesionDeManillaDAO.id!!,
                        mapOf<String, CampoModificable<SesionDeManilla, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                                         )
            }


            @TestConMultiplesDAO
            fun retorna_entidad_creada()
            {
                val entidadAInsertar = darInstanciaEntidadValida(sesionDeManillaDAO.id!!)
                val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

                val entidadesEnBD = listarTodasLosConteosUbicacionDAO(idClientePruebas, configuracionRepositorios)
                assertEquals(1, entidadesEnBD.size)
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_ErrorDeLlaveForanea_si_usa_una_ubicacion_inexistente()
            {
                val entidadAInsertar = darInstanciaEntidadValida(sesionDeManillaDAO.id!!).run {
                    copiar(idUbicacion = 15649621518L)
                }

                assertThrows<ErrorDeLlaveForanea> { repositorio.crear(idClientePruebas, entidadAInsertar) }
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_ErrorDeLlaveForanea_si_usa_una_sesion_de_manilla_inexistente()
            {
                val entidadAInsertar = darInstanciaEntidadValida(sesionDeManillaDAO.id!!).run {
                    copiar(idSesionDeManilla = 50054045L)
                }

                assertThrows<ErrorDeLlaveForanea> { repositorio.crear(idClientePruebas, entidadAInsertar) }
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_ErrorCreacionActualizacionPorDuplicidad_si_se_repite_un_conteo_para_una_sesion_ubicacion_y_tiempo()
            {
                val entidadAInsertar =
                        ConteoUbicacion(idClientePruebas, ubicacionConConteos.id!!, sesionDeManillaDAO.id!!, fechaHoraActual)

                repositorio.crear(idClientePruebas, entidadAInsertar)

                assertThrows<ErrorCreacionActualizacionPorDuplicidad> {
                    repositorio.crear(idClientePruebas, entidadAInsertar)
                }

                repositorio.crear(idClientePruebas, entidadAInsertar.copiar(fechaDeRealizacion = fechaHoraActual.plusMinutes(1)))
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_EsquemaNoExiste_si_cliente_con_id_dado_no_existe()
            {
                val entidadAInsertar = darInstanciaEntidadValida(sesionDeManillaDAO.id!!)

                assertThrows<EsquemaNoExiste> { repositorio.crear(idClientePruebas + 100, entidadAInsertar) }
            }
        }
    }

    @Nested
    inner class Consultar
    {
        @Nested
        inner class FiltrandoPorTodas
        {
            @Nested
            inner class SinEntidades
            {
                @TestConMultiplesDAO
                fun listado_de_entidades_sin_entidades_retorna_lista_vacia()
                {
                    val listadoConsultado = repositorio.listarSegunParametros(idClientePruebas, FiltroConteosUbicaciones.Todos)

                    assertTrue(listadoConsultado.none())
                }
            }

            @Nested
            inner class ConEntidades
            {
                private lateinit var entidadCreada: ConteoUbicacion

                @BeforeEach
                fun obtenerIdDeSesionDeManilla()
                {
                    val sesionesManilla = listarTodasLasSesionDeManillaDAO(idClientePruebas, configuracionRepositorios)
                    val entidadAInsertar = darInstanciaEntidadValida(sesionesManilla.first().id!!)

                    val nuevoValor = SesionDeManilla.CampoUuidTag(byteArrayOf(0, 1, 2, 3))

                    repositorioSesionesDeManilla.actualizarCamposIndividuales(
                            idClientePruebas,
                            sesionesManilla.first().id!!,
                            mapOf<String, CampoModificable<SesionDeManilla, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                                             )

                    entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)
                }


                @TestConMultiplesDAO
                fun listado_de_entidades_retorna_entidades_correctas()
                {
                    val listadoConsultado = repositorio.listarSegunParametros(idClientePruebas, FiltroConteosUbicaciones.Todos).toList()

                    assertEquals(listOf(entidadCreada), listadoConsultado)
                }

                @TestConMultiplesDAO
                fun listado_de_entidades_con_entidades_en_otro_cliente_retorna_lista_vacia()
                {
                    ejecutarConClienteAlternativo {
                        val listadoConsultado = repositorio.listarSegunParametros(it.id!!, FiltroConteosUbicaciones.Todos)
                        assertTrue(listadoConsultado.none())
                    }
                }

                @TestConMultiplesDAO
                fun lanza_excepcion_EsquemaNoExiste_si_se_consulta_listado_de_entidades_con_id_cliente_inexistente()
                {
                    assertThrows<EsquemaNoExiste> {
                        repositorio.listarSegunParametros(idClientePruebas + 100, FiltroConteosUbicaciones.Todos)
                    }
                }
            }
        }
    }

    @Nested
    inner class Eliminar
    {
        private lateinit var entidadPrueba: ConteoUbicacion

        @BeforeEach
        fun prepararPrueba()
        {
            val sesionesManilla = listarTodasLasSesionDeManillaDAO(idClientePruebas, configuracionRepositorios)
            val entidadAInsertar = darInstanciaEntidadValida(sesionesManilla.first().id!!)

            val nuevoValor = SesionDeManilla.CampoUuidTag(byteArrayOf(0, 1, 2, 3))

            repositorioSesionesDeManilla.actualizarCamposIndividuales(
                    idClientePruebas,
                    sesionesManilla.first().id!!,
                    mapOf<String, CampoModificable<SesionDeManilla, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                                     )

            entidadPrueba = repositorio.crear(idClientePruebas, entidadAInsertar)
        }

        @Nested
        inner class FiltrandoPorTodas
        {
            @TestConMultiplesDAO
            fun borra_todas_las_entidades()
            {
                val resultadoEliminacion = repositorio.eliminarSegunFiltros(idClientePruebas, FiltroConteosUbicaciones.Todos)
                val entidadConsultada = repositorio.listarSegunParametros(idClientePruebas, FiltroConteosUbicaciones.Todos)

                assertTrue(resultadoEliminacion)
                assertTrue(entidadConsultada.none())
            }

            @TestConMultiplesDAO
            fun con_id_existente_en_otro_cliente_retorna_false_y_no_elimina_entidad_existente()
            {
                ejecutarConClienteAlternativo {
                    val resultadoEliminacion = repositorio.eliminarSegunFiltros(it.id!!, FiltroConteosUbicaciones.Todos)
                    assertFalse(resultadoEliminacion)
                }

                val entidadConsultada = repositorio.listarSegunParametros(idClientePruebas, FiltroConteosUbicaciones.Todos)

                assertEquals(listOf(entidadPrueba), entidadConsultada.toList())
            }

            @TestConMultiplesDAO
            fun con_id_cliente_inexistente_lanza_excepcion_EsquemaNoExiste()
            {
                assertThrows<EsquemaNoExiste> {
                    repositorio.eliminarSegunFiltros(idClientePruebas + 100, FiltroConteosUbicaciones.Todos)
                }
            }
        }


        @[Nested DisplayName("lanza LanzaErrorDeLlaveForanea y no elimina entidad si intenta borrar")]
        inner class LanzaErrorDeLlaveForanea
        {
            @TestConMultiplesDAO
            fun la_ubicacion_asociada_a_un_conteo()
            {
                assertThrows<ErrorDeLlaveForanea> {
                    repositorioUbicaciones.eliminarPorId(idClientePruebas, entidadPrueba.idUbicacion)
                }
            }
        }
    }
}