package co.smartobjects.persistencia.fondos.libros

import co.smartobjects.entidades.fondos.Dinero
import co.smartobjects.entidades.fondos.Paquete
import co.smartobjects.entidades.fondos.libros.*
import co.smartobjects.entidades.fondos.precios.GrupoClientes
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.entidades.ubicaciones.Ubicacion
import co.smartobjects.persistencia.EntidadDAOBasePruebas
import co.smartobjects.persistencia.TestConMultiplesDAO
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorio
import co.smartobjects.persistencia.excepciones.*
import co.smartobjects.persistencia.existeAlgunaReglaDAO
import co.smartobjects.persistencia.fondos.grupoClientesCategoriaA
import co.smartobjects.persistencia.fondos.grupoClientesCategoriaB
import co.smartobjects.persistencia.fondos.grupoClientesCategoriaC
import co.smartobjects.persistencia.fondos.impuestoPruebasPorDefecto
import co.smartobjects.persistencia.fondos.monedas.RepositorioMonedas
import co.smartobjects.persistencia.fondos.monedas.RepositorioMonedasSQL
import co.smartobjects.persistencia.fondos.paquete.RepositorioPaquetes
import co.smartobjects.persistencia.fondos.paquete.RepositorioPaquetesSQL
import co.smartobjects.persistencia.fondos.precios.gruposclientes.RepositorioGrupoClientes
import co.smartobjects.persistencia.fondos.precios.gruposclientes.RepositorioGrupoClientesSQL
import co.smartobjects.persistencia.fondos.precios.impuestos.RepositorioImpuestos
import co.smartobjects.persistencia.fondos.precios.impuestos.RepositorioImpuestosSQL
import co.smartobjects.persistencia.listarTodasLasReglas
import co.smartobjects.persistencia.ubicaciones.RepositorioUbicaciones
import co.smartobjects.persistencia.ubicaciones.RepositorioUbicacionesSQL
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.threeten.bp.ZonedDateTime
import kotlin.test.*

@DisplayName("LibroSegunReglasDAOPruebas")
internal class LibroSegunReglasDAOPruebas : EntidadDAOBasePruebas()
{
    companion object
    {
        private const val NUMERO_DE_PRECIOS_EN_LIBRO = 5
    }

    private val repositorioImpuestos: RepositorioImpuestos by lazy { RepositorioImpuestosSQL(configuracionRepositorios) }
    private val repositorioUbicaciones: RepositorioUbicaciones by lazy { RepositorioUbicacionesSQL(configuracionRepositorios) }
    private val repositorioGrupoClientes: RepositorioGrupoClientes by lazy { RepositorioGrupoClientesSQL(configuracionRepositorios) }
    private val repositorioMonedas: RepositorioMonedas by lazy { RepositorioMonedasSQL(configuracionRepositorios) }
    private val repositorioPaquetes: RepositorioPaquetes by lazy { RepositorioPaquetesSQL(configuracionRepositorios) }
    private val repositorioLibrosDePrecios: RepositorioLibrosDePrecios by lazy { RepositorioLibrosDePreciosSQL(configuracionRepositorios) }
    private val repositorioLibrosDeProhibiciones: RepositorioLibrosDeProhibiciones by lazy { RepositorioLibrosDeProhibicionesSQL(configuracionRepositorios) }
    private val repositorio: RepositorioLibrosSegunReglas by lazy { RepositorioLibrosSegunReglasSQL(configuracionRepositorios) }

    override val creadoresRepositoriosUsados: List<CreadorRepositorio<*>> by lazy {
        listOf<CreadorRepositorio<*>>(
                repositorioImpuestos,
                repositorioGrupoClientes,
                repositorioUbicaciones,
                repositorioMonedas,
                repositorioPaquetes,
                repositorioLibrosDePrecios,
                repositorioLibrosDeProhibiciones,
                repositorio
                                     )
    }

    private lateinit var gruposDeClientes: List<GrupoClientes>
    private lateinit var monedas: List<Dinero>
    private var idLibroDePrecios: Long = -1
    private var idLibroDeProhibiciones: Long = -1

    @BeforeEach
    fun antesDeCadaTest()
    {
        val impuesto = repositorioImpuestos.crear(idClientePruebas, impuestoPruebasPorDefecto)

        gruposDeClientes = listOf(
                repositorioGrupoClientes.crear(idClientePruebas, grupoClientesCategoriaA),
                repositorioGrupoClientes.crear(idClientePruebas, grupoClientesCategoriaB),
                repositorioGrupoClientes.crear(idClientePruebas, grupoClientesCategoriaC)
                                 )

        monedas = List(NUMERO_DE_PRECIOS_EN_LIBRO) {
            repositorioMonedas.crear(
                    idClientePruebas,
                    Dinero(
                            idClientePruebas,
                            null,
                            "Dinero de prueba $it",
                            true,
                            false,
                            false,
                            Precio(Decimal(99999), impuesto.id!!),
                            "el código externo de prueba $it"
                          )
                                    )
        }

        val preciosDeFondos =
                monedas.map {
                    PrecioEnLibro(Precio(Decimal(it.id!!.toInt()), impuesto.id!!), it.id!!)
                }.toSet()

        val libroDePreciosACrear = LibroDePrecios(idClientePruebas, null, "Libro de precios en prueba", preciosDeFondos)
        idLibroDePrecios = repositorioLibrosDePrecios.crear(idClientePruebas, libroDePreciosACrear).id!!

        val paquete = crearPaquete("Paquete de pruebas")
        val libroDeProhibicionesACrear =
                LibroDeProhibiciones(
                        idClientePruebas,
                        null,
                        "Libro de prohibiciones en prueba",
                        setOf(Prohibicion.DeFondo(monedas.first().id!!)),
                        setOf(Prohibicion.DePaquete(paquete.id!!))
                                    )
        idLibroDeProhibiciones = repositorioLibrosDeProhibiciones.crear(idClientePruebas, libroDeProhibicionesACrear).id!!
    }

    private fun crearUbicacion(nombre: String): Ubicacion
    {
        return repositorioUbicaciones.crear(
                idClientePruebas,
                Ubicacion(
                        idClientePruebas,
                        null,
                        nombre,
                        Ubicacion.Tipo.PROPIEDAD,
                        Ubicacion.Subtipo.POS,
                        null,
                        linkedSetOf()
                         )
                                           )
    }

    private fun crearPaquete(nombre: String): Paquete
    {
        val fechaInicial = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)

        return repositorioPaquetes.crear(
                idClientePruebas,
                Paquete(
                        idClientePruebas,
                        null,
                        nombre,
                        "Descripción",
                        true,
                        fechaInicial,
                        fechaInicial.plusDays(1),
                        monedas.map {
                            Paquete.FondoIncluido(it.id!!, "código externo incluido", Decimal.UNO)
                        },
                        "código externo paquete"
                       )
                                        )
    }

    private fun darInstanciaUsandoElLibroDePrecios(
            reglasIdUbicacion: MutableSet<ReglaDeIdUbicacion>? = null,
            reglasIdGrupoDeClientes: MutableSet<ReglaDeIdGrupoDeClientes>? = null,
            reglasIdPaquete: MutableSet<ReglaDeIdPaquete>? = null
                                                  ): LibroSegunReglas
    {
        return LibroSegunReglas(
                idClientePruebas,
                null,
                "Libro segun reglas",
                idLibroDePrecios,
                reglasIdUbicacion ?: mutableSetOf(),
                reglasIdGrupoDeClientes ?: mutableSetOf(),
                reglasIdPaquete ?: mutableSetOf()
                               )
    }

    private fun darInstanciaLibroSegunReglasVariandoReglas(i: Int, incluirReglas: Boolean): LibroSegunReglas
    {
        return if (incluirReglas)
        {
            val reglaDeUbicacion = mutableSetOf(ReglaDeIdUbicacion(crearUbicacion("ubicacion $i").id!!))
            val reglaDeGrupo = mutableSetOf(ReglaDeIdGrupoDeClientes(gruposDeClientes[i].id!!)) // Asume que 0 <= i < gruposDeClientes.size(=3)
            val reglaDePaquete = mutableSetOf(ReglaDeIdPaquete(crearPaquete("paquete $i").id!!))

            darInstanciaUsandoElLibroDePrecios(reglaDeUbicacion, reglaDeGrupo, reglaDePaquete)
        }
        else
        {
            darInstanciaUsandoElLibroDePrecios()
        }.let {
            it.copiar(nombre = it.nombre + i)
        }
    }

    private fun crearLibroSegunReglasVariandoReglas(i: Int, incluirReglas: Boolean): LibroSegunReglas
    {
        val entidadAInsertar = darInstanciaLibroSegunReglasVariandoReglas(i, incluirReglas)
        return repositorio.crear(idClientePruebas, entidadAInsertar)
    }

    @Nested
    inner class Crear
    {
        @Nested
        inner class ConReglas
        {
            private lateinit var reglasEsperadas: ReglaDAO.ReglasConvertidas

            @BeforeEach
            fun generarReglas()
            {
                val reglaDeUbicacion =
                        mutableSetOf(
                                ReglaDeIdUbicacion(crearUbicacion("ubicacion 1").id!!),
                                ReglaDeIdUbicacion(crearUbicacion("ubicacion 2").id!!)
                                    )
                val reglaDeGrupo =
                        mutableSetOf(
                                ReglaDeIdGrupoDeClientes(gruposDeClientes.first().id!!),
                                ReglaDeIdGrupoDeClientes(gruposDeClientes.last().id!!)
                                    )
                val reglaDePaquete =
                        mutableSetOf(
                                ReglaDeIdPaquete(crearPaquete("paquete 1").id!!),
                                ReglaDeIdPaquete(crearPaquete("paquete 2").id!!)
                                    )

                reglasEsperadas = ReglaDAO.ReglasConvertidas(reglaDeUbicacion, reglaDeGrupo, reglaDePaquete)
            }

            @TestConMultiplesDAO
            fun guarda_el_id_del_libro_y_crea_las_reglas()
            {
                val entidadAInsertar = darInstanciaUsandoElLibroDePrecios(
                        reglasEsperadas.reglasIdUbicacion,
                        reglasEsperadas.reglasIdGrupoDeClientes,
                        reglasEsperadas.reglasIdPaquete
                                                                         )

                val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

                assertNotNull(entidadCreada.id)
                assertEquals(entidadAInsertar.copiar(id = entidadCreada.id), entidadCreada)

                val reglasAlmacenadas = listarTodasLasReglas(idClientePruebas, configuracionRepositorios)

                assertEquals(reglasEsperadas, reglasAlmacenadas)
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_ErrorCreacionViolacionDeRestriccion_si_para_un_mismo_libro_se_le_asignan_las_mismas_reglas()
            {
                val entidadExistente = crearLibroSegunReglasVariandoReglas(0, true)

                val entidadAInsertar = entidadExistente.copiar(id = null, nombre = entidadExistente.nombre + "asdfasdf")

                val excepcion = assertThrows<ErrorCreacionViolacionDeRestriccion> {
                    repositorio.crear(idClientePruebas, entidadAInsertar)
                }

                assertEquals(excepcion.entidad, LibroSegunReglas.NOMBRE_ENTIDAD)
            }
        }

        @Nested
        inner class SinReglas
        {
            private val reglasEsperadas = ReglaDAO.ReglasConvertidas(mutableSetOf(), mutableSetOf(), mutableSetOf())

            @TestConMultiplesDAO
            fun guarda_el_id_del_libro_y_no_crea_reglas()
            {
                val entidadAInsertar = darInstanciaUsandoElLibroDePrecios()
                val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

                assertNotNull(entidadCreada.id)
                assertEquals(entidadAInsertar.copiar(id = entidadCreada.id), entidadCreada)

                val reglasAlmacenadas = listarTodasLasReglas(idClientePruebas, configuracionRepositorios)

                assertEquals(reglasEsperadas, reglasAlmacenadas)
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_ErrorCreacionViolacionDeRestriccion_si_para_un_mismo_libro_no_se_le_asignan_reglas()
            {
                val entidadExistente = crearLibroSegunReglasVariandoReglas(0, false)

                val entidadAInsertar = entidadExistente.copiar(id = null, nombre = entidadExistente.nombre + "asdfasdf")

                val excepcion = assertThrows<ErrorCreacionViolacionDeRestriccion> {
                    repositorio.crear(idClientePruebas, entidadAInsertar)
                }

                assertEquals(excepcion.entidad, LibroSegunReglas.NOMBRE_ENTIDAD)
            }
        }

        @TestConMultiplesDAO
        fun con_id_asignado_retorna_misma_entidad_con_id_original()
        {
            val entidadAInsertar = darInstanciaUsandoElLibroDePrecios().copiar(id = 9876543)

            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertEquals(9876543, entidadCreada.id)
            assertEquals(entidadAInsertar.copiar(id = entidadCreada.id), entidadCreada)
        }

        @TestConMultiplesDAO
        fun utiliza_el_parametro_id_cliente_y_retorna_misma_entidad_con_id_cliente_correcto()
        {
            val entidadAInsertar = darInstanciaUsandoElLibroDePrecios().copiar(idCliente = 9876543)

            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertEquals(idClientePruebas, entidadCreada.idCliente)
            val entidadEsperada = entidadAInsertar.copiar(id = entidadCreada.id, idCliente = idClientePruebas)

            assertEquals(entidadEsperada, entidadCreada)
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_con_ErrorCreacionActualizacionPorDuplicidad_si_se_usa_nombre_duplicado()
        {
            val entidadAInsertar = darInstanciaLibroSegunReglasVariandoReglas(0, true)

            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            val entidadNuevaPeroConMismoNombre =
                    darInstanciaLibroSegunReglasVariandoReglas(1, true)
                        .copiar(nombre = entidadCreada.nombre)

            assertThrows<ErrorCreacionActualizacionPorDuplicidad> {
                repositorio.crear(idClientePruebas, entidadNuevaPeroConMismoNombre)
            }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_ErrorDeLlaveForanea_si_usa_un_id_de_una_ubicacion_inexistente()
        {
            val reglaDeUbicacion = mutableSetOf(ReglaDeIdUbicacion(34545L))

            val entidadAInsertar = darInstanciaUsandoElLibroDePrecios(reglasIdUbicacion = reglaDeUbicacion)

            assertThrows<ErrorDeLlaveForanea> {
                repositorio.crear(idClientePruebas, entidadAInsertar)
            }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_ErrorDeLlaveForanea_si_usa_un_id_de_grupo_de_clientes_inexistente()
        {
            val reglaDeGrupo = mutableSetOf(ReglaDeIdGrupoDeClientes(35453L))

            val entidadAInsertar = darInstanciaUsandoElLibroDePrecios(reglasIdGrupoDeClientes = reglaDeGrupo)

            assertThrows<ErrorDeLlaveForanea> {
                repositorio.crear(idClientePruebas, entidadAInsertar)
            }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_ErrorDeLlaveForanea_si_usa_un_id_de_un_paquete_inexistente()
        {
            val reglaDePaquete = mutableSetOf(ReglaDeIdPaquete(25343L))

            val entidadAInsertar = darInstanciaUsandoElLibroDePrecios(reglasIdPaquete = reglaDePaquete)

            assertThrows<ErrorDeLlaveForanea> {
                repositorio.crear(idClientePruebas, entidadAInsertar)
            }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_EsquemaNoExiste_si_cliente_con_id_dado_no_existe()
        {
            val entidadAInsertar = darInstanciaUsandoElLibroDePrecios()

            assertThrows<EsquemaNoExiste> {
                repositorio.crear(idClientePruebas + 100, entidadAInsertar)
            }
        }
    }

    @Nested
    inner class Consultar
    {
        @Nested
        inner class ConLibroDePrecios
        {
            @TestConMultiplesDAO
            fun por_id_existente_retorna_entidad_correcta()
            {
                val entidadPrueba = crearLibroSegunReglasVariandoReglas(0, true)

                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadPrueba.id!!)

                assertEquals(entidadPrueba, entidadConsultada)
            }

            @TestConMultiplesDAO
            fun listado_de_entidades_retorna_entidades_correctas()
            {
                val listadoEsperado = listOf(crearLibroSegunReglasVariandoReglas(0, true), crearLibroSegunReglasVariandoReglas(1, true)).sortedBy { it.id!! }

                val listadoConsultado = repositorio.listar(idClientePruebas).sortedBy { it.id!! }.toList()

                assertEquals(listadoEsperado, listadoConsultado)
            }
        }

        @TestConMultiplesDAO
        fun listado_de_entidades_sin_entidades_retorna_lista_vacia()
        {
            val listadoConsultado = repositorio.listar(idClientePruebas)

            assertTrue(listadoConsultado.none())
        }

        @TestConMultiplesDAO
        fun por_id_existente_en_otro_cliente_retorna_null()
        {
            val entidadPrueba = crearLibroSegunReglasVariandoReglas(0, true)

            ejecutarConClienteAlternativo {
                val entidadConsultada = repositorio.buscarPorId(it.id!!, entidadPrueba.id!!)
                assertNull(entidadConsultada)
            }
        }

        @TestConMultiplesDAO
        fun por_id_no_existente_retorna_null()
        {
            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, 789456789)

            assertNull(entidadConsultada)
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_EsquemaNoExiste_si_se_consulta_por_id_con_id_cliente_inexistente()
        {
            assertThrows<EsquemaNoExiste> { repositorio.buscarPorId(idClientePruebas + 100, 789456789) }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_EsquemaNoExiste_si_se_consulta_listado_de_entidades_con_id_cliente_inexistente()
        {
            assertThrows<EsquemaNoExiste> { repositorio.listar(idClientePruebas + 100) }
        }

        @TestConMultiplesDAO
        fun listado_de_entidades_con_entidades_en_otro_cliente_retorna_lista_vacia()
        {
            crearLibroSegunReglasVariandoReglas(0, true)

            ejecutarConClienteAlternativo {
                val listadoConsultado = repositorio.listar(it.id!!)
                assertTrue(listadoConsultado.none())
            }
        }
    }

    @Nested
    inner class Actualizar
    {
        private lateinit var entidadDePrueba: LibroSegunReglas

        @BeforeEach
        fun crearEntidadInicial()
        {
            entidadDePrueba = crearLibroSegunReglasVariandoReglas(0, true)
        }

        @[Nested DisplayName("un libro con reglas")]
        inner class ConReglas
        {
            @TestConMultiplesDAO
            fun se_actualizan_todos_los_campos_correctamente()
            {
                val entidadModificada = darInstanciaLibroSegunReglasVariandoReglas(1, true).let {
                    it.copiar(id = entidadDePrueba.id, nombre = it.nombre + entidadDePrueba.nombre, idLibro = idLibroDeProhibiciones)
                }

                repositorio.actualizar(idClientePruebas, entidadDePrueba.id!!, entidadModificada)

                val entidadActualizada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.id!!)

                assertEquals(entidadModificada, entidadActualizada)
            }

            @TestConMultiplesDAO
            fun que_repite_reglas_para_el_mismo_libro_asociado_lanza_ErrorCreacionViolacionDeRestriccion()
            {
                val otraEntidadDePrueba = crearLibroSegunReglasVariandoReglas(1, true)

                val entidadModificada = entidadDePrueba.copiar(
                        nombre = entidadDePrueba.nombre + "nuevo",
                        reglasIdUbicacion = otraEntidadDePrueba.reglasIdUbicacion,
                        reglasIdGrupoDeClientes = otraEntidadDePrueba.reglasIdGrupoDeClientes,
                        reglasIdPaquete = otraEntidadDePrueba.reglasIdPaquete
                                                              )

                val excepcion = assertThrows<ErrorActualizacionViolacionDeRestriccion> {
                    repositorio.actualizar(idClientePruebas, entidadDePrueba.id!!, entidadModificada)
                }

                assertEquals(LibroSegunReglas.NOMBRE_ENTIDAD, excepcion.entidad)
            }
        }

        @[Nested DisplayName("un libro sin reglas")]
        inner class SinReglas
        {
            @TestConMultiplesDAO
            fun se_actualizan_todos_los_campos_correctamente()
            {
                val entidadModificada = crearLibroSegunReglasVariandoReglas(1, false).let {
                    it.copiar(id = entidadDePrueba.id, nombre = it.nombre + entidadDePrueba.nombre, idLibro = idLibroDeProhibiciones)
                }

                repositorio.actualizar(idClientePruebas, entidadDePrueba.id!!, entidadModificada)

                val entidadActualizada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.id!!)

                assertEquals(entidadModificada, entidadActualizada)
            }

            @TestConMultiplesDAO
            fun que_repite_reglas_para_el_mismo_libro_asociado_lanza_ErrorCreacionViolacionDeRestriccion()
            {
                val otraEntidadDePrueba = crearLibroSegunReglasVariandoReglas(1, false)

                val entidadModificada = entidadDePrueba.copiar(
                        nombre = entidadDePrueba.nombre + "nuevo",
                        reglasIdUbicacion = otraEntidadDePrueba.reglasIdUbicacion,
                        reglasIdGrupoDeClientes = otraEntidadDePrueba.reglasIdGrupoDeClientes,
                        reglasIdPaquete = otraEntidadDePrueba.reglasIdPaquete
                                                              )

                val excepcion = assertThrows<ErrorActualizacionViolacionDeRestriccion> {
                    repositorio.actualizar(idClientePruebas, entidadDePrueba.id!!, entidadModificada)
                }

                assertEquals(LibroSegunReglas.NOMBRE_ENTIDAD, excepcion.entidad)
            }
        }

        @TestConMultiplesDAO
        fun se_actualizan_todos_los_demas_campos_excepto_id_de_libro_usando_las_mismas_reglas_originales()
        {
            val entidadModificada = entidadDePrueba.copiar(
                    nombre = entidadDePrueba.nombre + "Algo nuevo"
                                                          )

            repositorio.actualizar(idClientePruebas, entidadDePrueba.id!!, entidadModificada)

            val entidadActualizada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.id!!)

            assertEquals(entidadModificada, entidadActualizada)
        }

        @TestConMultiplesDAO
        fun utiliza_el_parametro_id_cliente_y_retorna_misma_entidad_con_id_cliente_correcto()
        {
            val entidadConCambios = entidadDePrueba.copiar(idCliente = 987654)

            repositorio.actualizar(idClientePruebas, entidadConCambios.id!!, entidadConCambios)

            val entidadActualizada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.id!!)

            assertEquals(idClientePruebas, entidadActualizada!!.idCliente)
        }

        @TestConMultiplesDAO
        fun con_id_inexistente_lanza_excepcion_EntidadNoExiste()
        {
            val entidadConCambios = entidadDePrueba.copiar(id = 789456)

            assertThrows<EntidadNoExiste> {
                repositorio.actualizar(idClientePruebas, entidadConCambios.id!!, entidadConCambios)
            }
        }

        @TestConMultiplesDAO
        fun con_nombre_duplicado_lanza_excepcion_ErrorCreacionActualizacionPorDuplicidad()
        {
            val entidadNuevaPeroConMismoNombre = darInstanciaLibroSegunReglasVariandoReglas(1, true)
            val entidadConCambios =
                    repositorio.crear(
                            idClientePruebas,
                            entidadNuevaPeroConMismoNombre.copiar(nombre = entidadDePrueba.nombre + " con cambio")
                                     )

            assertThrows<ErrorCreacionActualizacionPorDuplicidad> {
                repositorio.actualizar(idClientePruebas, entidadConCambios.id!!, entidadConCambios.copiar(nombre = entidadDePrueba.nombre))
            }
        }

        @[Nested DisplayName("lanza ErroDeLlaveForanea")]
        inner class ErroDeLlaveForanea
        {
            @TestConMultiplesDAO
            fun si_usa_un_id_de_una_ubicacion_inexistente()
            {
                val entidadConCambios = entidadDePrueba.copiar(reglasIdUbicacion = mutableSetOf(ReglaDeIdUbicacion(34545L)))

                assertThrows<ErrorDeLlaveForanea> {
                    repositorio.actualizar(idClientePruebas, entidadConCambios.id!!, entidadConCambios)
                }
            }

            @TestConMultiplesDAO
            fun si_usa_un_id_de_grupo_de_clientes_inexistente()
            {
                val entidadConCambios = entidadDePrueba.copiar(reglasIdGrupoDeClientes = mutableSetOf(ReglaDeIdGrupoDeClientes(35453L)))

                assertThrows<ErrorDeLlaveForanea> {
                    repositorio.actualizar(idClientePruebas, entidadConCambios.id!!, entidadConCambios)
                }
            }

            @TestConMultiplesDAO
            fun si_usa_un_id_de_un_paquete_inexistente()
            {
                val entidadConCambios = entidadDePrueba.copiar(reglasIdPaquete = mutableSetOf(ReglaDeIdPaquete(25343L)))

                assertThrows<ErrorDeLlaveForanea> {
                    repositorio.actualizar(idClientePruebas, entidadConCambios.id!!, entidadConCambios)
                }
            }
        }

        @TestConMultiplesDAO
        fun con_id_existente_en_otro_cliente_lanza_excepcion_EntidadNoExiste()
        {
            ejecutarConClienteAlternativo {
                assertThrows<EntidadNoExiste> {
                    repositorio.actualizar(it.id!!, entidadDePrueba.id!!, entidadDePrueba)
                }
            }
        }

        @TestConMultiplesDAO
        fun con_id_cliente_inexistente_lanza_excepcion_EsquemaNoExiste()
        {
            assertThrows<EsquemaNoExiste> {
                repositorio.actualizar(idClientePruebas + 100, entidadDePrueba.id!!, entidadDePrueba)
            }
        }
    }

    @Nested
    inner class Eliminar
    {
        private lateinit var entidadInicial: LibroSegunReglas

        @BeforeEach
        fun crearEntidadInicial()
        {
            entidadInicial = crearLibroSegunReglasVariandoReglas(0, true)
        }

        @TestConMultiplesDAO
        fun si_existe_la_entidad_se_elimina_esta_y_sus_reglas_correctamente()
        {
            val resultadoEliminacion = repositorio.eliminarPorId(idClientePruebas, entidadInicial.id!!)
            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadInicial.id!!)

            assertTrue(resultadoEliminacion)
            assertNull(entidadConsultada)
            assertFalse(existeAlgunaReglaDAO(idClientePruebas, configuracionRepositorios))
        }

        @TestConMultiplesDAO
        fun si_no_existe_la_entidad_retorna_false_y_no_elimina_entidad_existente()
        {
            val resultadoEliminacion = repositorio.eliminarPorId(idClientePruebas, entidadInicial.id!! + 1)
            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadInicial.id!!)

            assertFalse(resultadoEliminacion)
            assertEquals(entidadInicial, entidadConsultada)
        }

        @TestConMultiplesDAO
        fun si_se_intenta_eliminar_el_libro_asociado_lanza_excepcion_ErrorDeLlaveForanea()
        {
            assertThrows<ErrorDeLlaveForanea> {
                repositorioLibrosDePrecios.eliminarPorId(idClientePruebas, entidadInicial.idLibro)
            }
        }

        @TestConMultiplesDAO
        fun si_se_intenta_eliminar_una_ubicacion_usada_en_alguna_regla_lanza_excepcion_ErrorDeLlaveForanea()
        {
            assertThrows<ErrorDeLlaveForanea> {
                repositorioUbicaciones.eliminarPorId(idClientePruebas, entidadInicial.reglasIdUbicacion.first().restriccion)
            }
        }

        @TestConMultiplesDAO
        fun si_se_intenta_eliminar_un_grupo_de_clientes_usado_en_alguna_regla_lanza_excepcion_ErrorDeLlaveForanea()
        {
            assertThrows<ErrorDeLlaveForanea> {
                repositorioGrupoClientes.eliminarPorId(idClientePruebas, entidadInicial.reglasIdGrupoDeClientes.first().restriccion)
            }
        }

        @TestConMultiplesDAO
        fun si_se_intenta_eliminar_un_paquete_usado_en_alguna_regla_lanza_excepcion_ErrorDeLlaveForanea()
        {
            assertThrows<ErrorDeLlaveForanea> {
                repositorioPaquetes.eliminarPorId(idClientePruebas, entidadInicial.reglasIdPaquete.first().restriccion)
            }
        }

        @TestConMultiplesDAO
        fun con_id_existente_en_otro_cliente_retorna_false_y_no_elimina_entidad_existente()
        {
            ejecutarConClienteAlternativo {
                val resultadoEliminacion = repositorio.eliminarPorId(it.id!!, entidadInicial.id!!)
                kotlin.test.assertFalse(resultadoEliminacion)
            }
            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadInicial.id!!)
            assertEquals(entidadInicial, entidadConsultada)
        }

        @TestConMultiplesDAO
        fun con_id_cliente_inexistente_lanza_excepcion_EsquemaNoExiste()
        {
            assertThrows<EsquemaNoExiste> {
                repositorio.eliminarPorId(idClientePruebas + 100, entidadInicial.id!!)
            }
        }
    }
}