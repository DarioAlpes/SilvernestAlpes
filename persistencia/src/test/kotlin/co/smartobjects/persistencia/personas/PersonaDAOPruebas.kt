package co.smartobjects.persistencia.personas

import co.smartobjects.entidades.personas.DocumentoCompleto
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.personas.PersonaConFamiliares
import co.smartobjects.persistencia.EntidadDAOBasePruebas
import co.smartobjects.persistencia.TestConMultiplesDAO
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorio
import co.smartobjects.persistencia.excepciones.EntidadNoExiste
import co.smartobjects.persistencia.excepciones.ErrorCreacionActualizacionPorDuplicidad
import co.smartobjects.persistencia.excepciones.EsquemaNoExiste
import co.smartobjects.persistencia.listarTodasLasRelacionesPersonas
import co.smartobjects.persistencia.personas.relacionesdepersonas.RepositorioRelacionesPersonas
import co.smartobjects.persistencia.personas.relacionesdepersonas.RepositorioRelacionesPersonasSQL
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.threeten.bp.LocalDate
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("PersonaDAO")
internal class PersonaDAOPruebas
{
    internal class ProveedorTipoDocumentoDAOConTipoDocumento : ArgumentsProvider
    {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments>
        {
            return Stream.of(
                    Arguments.of(PersonaDAO.TipoDocumento.CC, Persona.TipoDocumento.CC),
                    Arguments.of(PersonaDAO.TipoDocumento.CD, Persona.TipoDocumento.CD),
                    Arguments.of(PersonaDAO.TipoDocumento.CE, Persona.TipoDocumento.CE),
                    Arguments.of(PersonaDAO.TipoDocumento.PA, Persona.TipoDocumento.PA),
                    Arguments.of(PersonaDAO.TipoDocumento.RC, Persona.TipoDocumento.RC),
                    Arguments.of(PersonaDAO.TipoDocumento.NIT, Persona.TipoDocumento.NIT),
                    Arguments.of(PersonaDAO.TipoDocumento.NUIP, Persona.TipoDocumento.NUIP),
                    Arguments.of(PersonaDAO.TipoDocumento.TI, Persona.TipoDocumento.TI)
                            )
        }
    }

    internal class ProveedorGeneroDAOConGenero : ArgumentsProvider
    {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments>
        {
            return Stream.of(
                    Arguments.of(PersonaDAO.Genero.MASCULINO, Persona.Genero.MASCULINO),
                    Arguments.of(PersonaDAO.Genero.FEMENINO, Persona.Genero.FEMENINO),
                    Arguments.of(PersonaDAO.Genero.DESCONOCIDO, Persona.Genero.DESCONOCIDO)
                            )
        }
    }

    internal class ProveedorCategoriaDAOConCategoria : ArgumentsProvider
    {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments>
        {
            return Stream.of(
                    Arguments.of(PersonaDAO.Categoria.A, Persona.Categoria.A),
                    Arguments.of(PersonaDAO.Categoria.B, Persona.Categoria.B),
                    Arguments.of(PersonaDAO.Categoria.C, Persona.Categoria.C),
                    Arguments.of(PersonaDAO.Categoria.D, Persona.Categoria.D)
                            )
        }
    }

    internal class ProveedorAfiliacionDAOConAfiliacion : ArgumentsProvider
    {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments>
        {
            return Stream.of(
                    Arguments.of(PersonaDAO.Afiliacion.COTIZANTE, Persona.Afiliacion.COTIZANTE),
                    Arguments.of(PersonaDAO.Afiliacion.BENEFICIARIO, Persona.Afiliacion.BENEFICIARIO)
                            )
        }
    }

    @Nested
    inner class Conversion
    {
        @Nested
        @DisplayName("Al convertir a entidad de negocio")
        inner class AEntidadNegocio
        {
            @DisplayName("Cuando el Tipo de Documento en DAO")
            @ParameterizedTest(name = "Es ''{0}'' asigna ''{1}'' en negocio")
            @ArgumentsSource(ProveedorTipoDocumentoDAOConTipoDocumento::class)
            fun paraTipoDocumento(tipoDocumentoDAO: PersonaDAO.TipoDocumento, tipoDocumentoNegocio: Persona.TipoDocumento)
            {
                val entidadDAO = PersonaDAO(nombreCompleto = "Entidad prueba", numeroDocumento = "123", esAnonima = false, tipoDocumento = tipoDocumentoDAO)
                val entidadNegocio = entidadDAO.aEntidadDeNegocio(1)
                assertEquals(tipoDocumentoNegocio, entidadNegocio.tipoDocumento)
            }

            @DisplayName("Cuando el Genero en DAO")
            @ParameterizedTest(name = "Es ''{0}'' asigna ''{1}'' en negocio")
            @ArgumentsSource(ProveedorGeneroDAOConGenero::class)
            fun paraGenero(generoDAO: PersonaDAO.Genero, generoNegocio: Persona.Genero)
            {
                val entidadDAO = PersonaDAO(nombreCompleto = "Entidad prueba", numeroDocumento = "123", esAnonima = false, genero = generoDAO)
                val entidadNegocio = entidadDAO.aEntidadDeNegocio(1)
                assertEquals(generoNegocio, entidadNegocio.genero)
            }

            @DisplayName("Cuando la Categoría en DAO")
            @ParameterizedTest(name = "Es ''{0}'' asigna ''{1}'' en negocio")
            @ArgumentsSource(ProveedorCategoriaDAOConCategoria::class)
            fun paraCategoria(categoriaDAO: PersonaDAO.Categoria, categoriaNegocio: Persona.Categoria)
            {
                val entidadDAO = PersonaDAO(nombreCompleto = "Entidad prueba", numeroDocumento = "123", esAnonima = false, categoria = categoriaDAO)
                val entidadNegocio = entidadDAO.aEntidadDeNegocio(1)
                assertEquals(categoriaNegocio, entidadNegocio.categoria)
            }

            @DisplayName("Cuando la Afiliación en DAO")
            @ParameterizedTest(name = "Es ''{0}'' asigna ''{1}'' en negocio")
            @ArgumentsSource(ProveedorAfiliacionDAOConAfiliacion::class)
            fun paraAfiliacion(afiliacionDAO: PersonaDAO.Afiliacion, afiliacionNegocio: Persona.Afiliacion)
            {
                val entidadDAO = PersonaDAO(nombreCompleto = "Entidad prueba", numeroDocumento = "123", esAnonima = false, afiliacion = afiliacionDAO)
                val entidadNegocio = entidadDAO.aEntidadDeNegocio(1)
                assertEquals(afiliacionNegocio, entidadNegocio.afiliacion)
            }
        }

        @Nested
        @DisplayName("Al convertir desde entidad de negocio")
        inner class DesdeEntidadNegocio
        {
            private fun darEntidadNegocioPruebaSegunTipoDocumentoGeneroCategoriaYAfiliacion(
                    tipoDocumento: Persona.TipoDocumento = Persona.TipoDocumento.CC,
                    genero: Persona.Genero = Persona.Genero.DESCONOCIDO,
                    categoria: Persona.Categoria = Persona.Categoria.D,
                    afiliacion: Persona.Afiliacion = Persona.Afiliacion.COTIZANTE): Persona
            {
                return Persona(
                        1,
                        null,
                        "Persona prueba",
                        tipoDocumento,
                        "123",
                        genero,
                        LocalDate.now(),
                        categoria,
                        afiliacion,
                        null,
                        "empresa",
                        "0",
                        Persona.Tipo.NO_AFILIADO
                              )
            }

            @DisplayName("Cuando el Tipo de Documento")
            @ParameterizedTest(name = "Es ''{1}'' asigna ''{0}'' en dao")
            @ArgumentsSource(ProveedorTipoDocumentoDAOConTipoDocumento::class)
            fun paraTipoDocumento(tipoDocumentoDAO: PersonaDAO.TipoDocumento, tipoDocumentoNegocio: Persona.TipoDocumento)
            {
                val entidadNegocio = darEntidadNegocioPruebaSegunTipoDocumentoGeneroCategoriaYAfiliacion(tipoDocumento = tipoDocumentoNegocio)
                val entidadDAOCreando = PersonaDAO(entidadNegocio)
                assertEquals(tipoDocumentoDAO, entidadDAOCreando.tipoDocumento)
                val entidadDAOEditando = PersonaDAO(entidadNegocio)
                assertEquals(tipoDocumentoDAO, entidadDAOEditando.tipoDocumento)
            }

            @DisplayName("Cuando el Genero")
            @ParameterizedTest(name = "Es ''{1}'' asigna ''{0}'' en dao")
            @ArgumentsSource(ProveedorGeneroDAOConGenero::class)
            fun paraGenero(generoDAO: PersonaDAO.Genero, generoNegocio: Persona.Genero)
            {
                val entidadNegocio = darEntidadNegocioPruebaSegunTipoDocumentoGeneroCategoriaYAfiliacion(genero = generoNegocio)
                val entidadDAOCreando = PersonaDAO(entidadNegocio)
                assertEquals(generoDAO, entidadDAOCreando.genero)
                val entidadDAOEditando = PersonaDAO(entidadNegocio)
                assertEquals(generoDAO, entidadDAOEditando.genero)
            }

            @DisplayName("Cuando la Categoría")
            @ParameterizedTest(name = "Es ''{1}'' asigna ''{0}'' en dao")
            @ArgumentsSource(ProveedorCategoriaDAOConCategoria::class)
            fun paraCategoria(categoriaDAO: PersonaDAO.Categoria, categoriaNegocio: Persona.Categoria)
            {
                val entidadNegocio = darEntidadNegocioPruebaSegunTipoDocumentoGeneroCategoriaYAfiliacion(categoria = categoriaNegocio)
                val entidadDAOCreando = PersonaDAO(entidadNegocio)
                assertEquals(categoriaDAO, entidadDAOCreando.categoria)
                val entidadDAOEditando = PersonaDAO(entidadNegocio)
                assertEquals(categoriaDAO, entidadDAOEditando.categoria)
            }

            @DisplayName("Cuando la Afiliación")
            @ParameterizedTest(name = "Es ''{1}'' asigna ''{0}'' en dao")
            @ArgumentsSource(ProveedorAfiliacionDAOConAfiliacion::class)
            fun paraAfiliacion(afiliacionDAO: PersonaDAO.Afiliacion, afiliacionNegocio: Persona.Afiliacion)
            {
                val entidadNegocio = darEntidadNegocioPruebaSegunTipoDocumentoGeneroCategoriaYAfiliacion(afiliacion = afiliacionNegocio)
                val entidadDAOCreando = PersonaDAO(entidadNegocio)
                assertEquals(afiliacionDAO, entidadDAOCreando.afiliacion)
                val entidadDAOEditando = PersonaDAO(entidadNegocio)
                assertEquals(afiliacionDAO, entidadDAOEditando.afiliacion)
            }
        }
    }

    @Nested
    inner class EnBD : EntidadDAOBasePruebas()
    {
        private val repositorio: RepositorioPersonas by lazy { RepositorioPersonasSQL(configuracionRepositorios) }
        private val repositorioRelacionesPersonas: RepositorioRelacionesPersonas by lazy { RepositorioRelacionesPersonasSQL(configuracionRepositorios) }
        override val creadoresRepositoriosUsados: List<CreadorRepositorio<*>> by lazy {
            listOf<CreadorRepositorio<*>>(repositorio, repositorioRelacionesPersonas)
        }

        private fun darInstanciaEntidadValida(): Persona
        {
            return Persona(
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
        }

        @Nested
        inner class Crear
        {
            @TestConMultiplesDAO
            fun sin_id_asignado_retorna_misma_entidad_con_id_asignado_por_bd()
            {
                val entidadAInsertar = darInstanciaEntidadValida()
                val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

                assertNotNull(entidadCreada.id)
                assertEquals(entidadAInsertar.copiar(id = entidadCreada.id), entidadCreada)
            }

            @TestConMultiplesDAO
            fun con_id_asignado_retorna_misma_entidad_con_id_original()
            {
                val entidadAInsertar = darInstanciaEntidadValida().copiar(id = 9876543)

                val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

                assertEquals(9876543, entidadCreada.id)
                assertEquals(entidadAInsertar.copiar(id = entidadCreada.id), entidadCreada)
            }

            @TestConMultiplesDAO
            fun utiliza_el_parametro_id_cliente_y_retorna_misma_entidad_con_id_cliente_correcto()
            {
                val entidadAInsertar = darInstanciaEntidadValida().copiar(idCliente = 9876543)

                val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

                assertEquals(idClientePruebas, entidadCreada.idCliente)
                val entidadEsperada = entidadAInsertar.copiar(id = entidadCreada.id, idCliente = idClientePruebas)
                assertEquals(entidadEsperada, entidadCreada)
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_con_ErrorCreacionActualizacionPorDuplicidad_si_se_usa_documento_duplicado()
            {
                val entidadAInsertar = darInstanciaEntidadValida()

                val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

                assertThrows<ErrorCreacionActualizacionPorDuplicidad> {
                    repositorio.crear(idClientePruebas, entidadCreada)
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
                val entidadPrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadValida())

                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadPrueba.id!!)

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
                            darInstanciaEntidadValida()
                                .copiar(numeroDocumento = "$it")
                                     )
                }

                val listadoEsperado = entidadesPrueba.sortedBy { it.id!! }


                val listadoConsultado = repositorio.listar(idClientePruebas)

                assertEquals(listadoEsperado, listadoConsultado.sortedBy { it.id!! }.toList())
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
                val entidadPrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadValida())

                ejecutarConClienteAlternativo {
                    val entidadConsultada = repositorio.buscarPorId(it.id!!, entidadPrueba.id!!)
                    assertNull(entidadConsultada)
                }
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
                repositorio.crear(idClientePruebas, darInstanciaEntidadValida())

                ejecutarConClienteAlternativo {
                    val listadoConsultado = repositorio.listar(it.id!!)
                    assertTrue(listadoConsultado.none())
                }
            }
        }

        @Nested
        inner class ConsultarPorDocumento
        {
            @TestConMultiplesDAO
            fun por_documento_existente_retorna_entidad_correcta()
            {
                Persona.TipoDocumento.values().forEach {
                    val entidadPrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadValida().copiar(tipoDocumento = it))

                    val entidadConsultada = repositorio.buscarSegunParametros(
                            idClientePruebas,
                            DocumentoCompletoDAO(DocumentoCompleto(it, entidadPrueba.numeroDocumento))
                                                                             )

                    assertEquals(entidadPrueba, entidadConsultada)
                    repositorio.eliminarPorId(idClientePruebas, entidadPrueba.id!!)
                }
            }

            @TestConMultiplesDAO
            fun por_documento_no_existente_retorna_null()
            {
                Persona.TipoDocumento.values().forEach { tipoDocumento ->
                    val entidadPrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadValida().copiar(tipoDocumento = tipoDocumento))
                    Persona.TipoDocumento.values().filter { it != tipoDocumento }.forEach {
                        val entidadConsultada = repositorio.buscarSegunParametros(
                                idClientePruebas,
                                DocumentoCompletoDAO(DocumentoCompleto(it, entidadPrueba.numeroDocumento))
                                                                                 )
                        assertNull(entidadConsultada)
                    }
                    val entidadConsultada = repositorio.buscarSegunParametros(
                            idClientePruebas,
                            DocumentoCompletoDAO(DocumentoCompleto(tipoDocumento, "${entidadPrueba.numeroDocumento}123"))
                                                                             )
                    assertNull(entidadConsultada)
                    repositorio.eliminarPorId(idClientePruebas, entidadPrueba.id!!)
                }
            }

            @TestConMultiplesDAO
            fun por_documento_existente_en_otro_cliente_retorna_null()
            {
                Persona.TipoDocumento.values().forEach { tipoDocumento ->
                    val entidadPrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadValida().copiar(tipoDocumento = tipoDocumento))

                    ejecutarConClienteAlternativo {
                        val entidadConsultada = repositorio.buscarSegunParametros(
                                it.id!!,
                                DocumentoCompletoDAO(DocumentoCompleto(tipoDocumento, entidadPrueba.numeroDocumento))
                                                                                 )
                        assertNull(entidadConsultada)
                    }
                    repositorio.eliminarPorId(idClientePruebas, entidadPrueba.id!!)
                }
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_EsquemaNoExiste_si_se_consulta_por_documento_con_id_cliente_inexistente()
            {
                assertThrows<EsquemaNoExiste> {
                    repositorio.buscarSegunParametros(
                            idClientePruebas + 100,
                            DocumentoCompletoDAO(DocumentoCompleto(Persona.TipoDocumento.CC, "123"))
                                                     )
                }
            }
        }

        @Nested
        inner class Actualizar
        {
            private lateinit var entidadDePrueba: Persona

            @BeforeEach
            fun crearEntidadPrueba()
            {
                entidadDePrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadValida())
            }

            @TestConMultiplesDAO
            fun se_actualizan_todos_los_campos_correctamente()
            {
                val entidadModificada = Persona(
                        idClientePruebas,
                        entidadDePrueba.id,
                        "Persona modificada",
                        Persona.TipoDocumento.TI,
                        "123456",
                        Persona.Genero.MASCULINO,
                        LocalDate.now(ZONA_HORARIA_POR_DEFECTO).minusYears(20),
                        Persona.Categoria.A,
                        Persona.Afiliacion.BENEFICIARIO,
                        null,
                        "empresa",
                        "0",
                        Persona.Tipo.NO_AFILIADO
                                               )
                val entidadActualizada = repositorio.actualizar(idClientePruebas, entidadModificada.id!!, entidadModificada)
                assertEquals(entidadModificada, entidadActualizada)
            }

            @TestConMultiplesDAO
            fun utiliza_el_parametro_id_cliente_y_retorna_misma_entidad_con_id_cliente_correcto()
            {
                val entidadConCambios = entidadDePrueba.copiar(idCliente = 987654)

                val entidadActualizada = repositorio.actualizar(idClientePruebas, entidadConCambios.id!!, entidadConCambios)

                assertEquals(idClientePruebas, entidadActualizada.idCliente)
            }

            @TestConMultiplesDAO
            fun con_id_inexistente_lanza_excepcion_EntidadNoExiste()
            {
                val entidadConCambios = entidadDePrueba.copiar(id = 789456)

                assertThrows<EntidadNoExiste> { repositorio.actualizar(idClientePruebas, 789456, entidadConCambios) }
            }

            @TestConMultiplesDAO
            fun con_documento_duplicado_lanza_excepcion_ErrorCreacionActualizacionPorDuplicidad()
            {
                val entidadConCambios = repositorio.crear(
                        idClientePruebas,
                        darInstanciaEntidadValida().copiar(numeroDocumento = "12345678")
                                                         )

                assertThrows<ErrorCreacionActualizacionPorDuplicidad> {
                    repositorio.actualizar(idClientePruebas, entidadConCambios.id!!, entidadConCambios.copiar(numeroDocumento = entidadDePrueba.numeroDocumento))
                }
            }

            @TestConMultiplesDAO
            fun con_id_existente_en_otro_cliente_lanza_excepcion_EntidadNoExiste()
            {
                ejecutarConClienteAlternativo {
                    assertThrows<EntidadNoExiste> { repositorio.actualizar(it.id!!, entidadDePrueba.id!!, entidadDePrueba) }
                }
            }

            @TestConMultiplesDAO
            fun con_id_cliente_inexistente_lanza_excepcion_EsquemaNoExiste()
            {
                assertThrows<EsquemaNoExiste> { repositorio.actualizar(idClientePruebas + 100, entidadDePrueba.id!!, entidadDePrueba) }
            }
        }

        @Nested
        inner class Eliminar
        {
            private lateinit var entidadPrueba: Persona

            @BeforeEach
            fun crearEntidadDePrueba()
            {
                entidadPrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadValida())
            }

            @TestConMultiplesDAO
            fun si_existe_la_entidad_se_elimina_correctamente()
            {
                val resultadoEliminacion = repositorio.eliminarPorId(idClientePruebas, entidadPrueba.id!!)
                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadPrueba.id!!)

                assertTrue(resultadoEliminacion)
                assertNull(entidadConsultada)
            }

            @TestConMultiplesDAO
            fun si_existen_relaciones_de_persona_se_eliminan()
            {
                val familiarCreado =
                        repositorio.crear(idClientePruebas,
                                          Persona(
                                                  idClientePruebas,
                                                  null,
                                                  "Persona familiar",
                                                  Persona.TipoDocumento.NUIP,
                                                  "23646456",
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

                val relacionesDeFamiliares = PersonaConFamiliares(entidadPrueba, setOf(familiarCreado))

                repositorioRelacionesPersonas.crear(idClientePruebas, relacionesDeFamiliares)

                assertTrue {
                    listarTodasLasRelacionesPersonas(idClientePruebas, configuracionRepositorios)
                        .isNotEmpty()
                }

                val resultadoEliminacion = repositorio.eliminarPorId(idClientePruebas, entidadPrueba.id!!)

                val relacionesEnBd = listarTodasLasRelacionesPersonas(idClientePruebas, configuracionRepositorios)

                assertTrue(resultadoEliminacion)
                assertTrue(relacionesEnBd.isEmpty())
            }

            @TestConMultiplesDAO
            fun si_no_existe_la_entidad_retorna_false_y_no_elimina_entidad_existente()
            {
                val resultadoEliminacion = repositorio.eliminarPorId(idClientePruebas, entidadPrueba.id!! + 1)
                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadPrueba.id!!)

                assertFalse(resultadoEliminacion)
                assertEquals(entidadPrueba, entidadConsultada)
            }

            @TestConMultiplesDAO
            fun con_id_existente_en_otro_cliente_retorna_false_y_no_elimina_entidad_existente()
            {
                ejecutarConClienteAlternativo {
                    val resultadoEliminacion = repositorio.eliminarPorId(it.id!!, entidadPrueba.id!!)
                    kotlin.test.assertFalse(resultadoEliminacion)
                }
                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadPrueba.id!!)
                assertEquals(entidadPrueba, entidadConsultada)
            }

            @TestConMultiplesDAO
            fun con_id_cliente_inexistente_lanza_excepcion_EsquemaNoExiste()
            {
                assertThrows<EsquemaNoExiste> { repositorio.eliminarPorId(idClientePruebas + 100, entidadPrueba.id!!) }
            }
        }
    }
}