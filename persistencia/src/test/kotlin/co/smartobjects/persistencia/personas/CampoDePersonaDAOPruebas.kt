package co.smartobjects.persistencia.personas

import co.smartobjects.entidades.personas.CampoDePersona
import co.smartobjects.persistencia.EntidadDAOBasePruebas
import co.smartobjects.persistencia.TestConMultiplesDAO
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorio
import co.smartobjects.persistencia.excepciones.EsquemaNoExiste
import co.smartobjects.persistencia.personas.camposdepersona.CampoDePersonaDAO
import co.smartobjects.persistencia.personas.camposdepersona.ListadoCamposPredeterminados
import co.smartobjects.persistencia.personas.camposdepersona.RepositorioCampoDePersonasSQL
import co.smartobjects.persistencia.personas.camposdepersona.RepositorioCamposDePersonas
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.function.Executable
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.util.stream.Stream
import kotlin.test.assertEquals


@DisplayName("CampoDePersonaDAO")
internal class CampoDePersonaDAOPruebas
{
    internal class ProveedorPredeterminadoDAOyPredeterminado : ArgumentsProvider
    {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments>
        {
            return Stream.of(
                    Arguments.of(CampoDePersonaDAO.Predeterminado.NOMBRE_COMPLETO, CampoDePersona.Predeterminado.NOMBRE_COMPLETO),
                    Arguments.of(CampoDePersonaDAO.Predeterminado.TIPO_DOCUMENTO, CampoDePersona.Predeterminado.TIPO_DOCUMENTO),
                    Arguments.of(CampoDePersonaDAO.Predeterminado.NUMERO_DOCUMENTO, CampoDePersona.Predeterminado.NUMERO_DOCUMENTO),
                    Arguments.of(CampoDePersonaDAO.Predeterminado.GENERO, CampoDePersona.Predeterminado.GENERO),
                    Arguments.of(CampoDePersonaDAO.Predeterminado.FECHA_NACIMIENTO, CampoDePersona.Predeterminado.FECHA_NACIMIENTO),
                    Arguments.of(CampoDePersonaDAO.Predeterminado.CATEGORIA, CampoDePersona.Predeterminado.CATEGORIA),
                    Arguments.of(CampoDePersonaDAO.Predeterminado.AFILIACION, CampoDePersona.Predeterminado.AFILIACION),
                    Arguments.of(CampoDePersonaDAO.Predeterminado.ES_ANONIMA, CampoDePersona.Predeterminado.ES_ANONIMA),
                    Arguments.of(CampoDePersonaDAO.Predeterminado.LLAVE_IMAGEN, CampoDePersona.Predeterminado.LLAVE_IMAGEN),
                    Arguments.of(CampoDePersonaDAO.Predeterminado.EMPRESA, CampoDePersona.Predeterminado.EMPRESA)
                            )
        }
    }


    @Nested
    inner class Conversion
    {
        @DisplayName("Cuando el campo predeterminado en DAO")
        @ParameterizedTest(name = "Es ''{0}'' asigna ''{1}'' en negocio")
        @ArgumentsSource(ProveedorPredeterminadoDAOyPredeterminado::class)
        fun de_dao_a_negocio_correctamente(enumeracionDAO: CampoDePersonaDAO.Predeterminado, enumeracionNegocio: CampoDePersona.Predeterminado)
        {
            val entidadDAO = CampoDePersonaDAO(enumeracionDAO, true)

            val entidadNegocio = entidadDAO.aEntidadDeNegocio(0)

            assertEquals(enumeracionNegocio, entidadNegocio.campo)
        }

        @DisplayName("Cuando el campo predeterminado")
        @ParameterizedTest(name = "Es ''{1}'' asigna ''{0}'' en dao")
        @ArgumentsSource(ProveedorPredeterminadoDAOyPredeterminado::class)
        fun de_negocio_a_dao_correctamente(enumeracionDAO: CampoDePersonaDAO.Predeterminado, enumeracionNegocio: CampoDePersona.Predeterminado)
        {
            val entidadNegocio = CampoDePersona(enumeracionNegocio, true)

            val entidadDAO = CampoDePersonaDAO(entidadNegocio)

            assertEquals(enumeracionDAO, entidadDAO.campo)
        }
    }

    @Nested
    inner class Listado_ListadoCamposPredeterminados
    {
        @Test
        fun los_campos_son_todos_los_predeterminados_marcados_como_no_requeridos()
        {
            val entidad = ListadoCamposPredeterminados()
            val camposEsperados = CampoDePersona.Predeterminado.values().map { CampoDePersona(it, false) }

            assertEquals(camposEsperados, entidad.campos.toList())
        }
    }

    @Nested
    inner class EnBD : EntidadDAOBasePruebas()
    {
        private val repositorio: RepositorioCamposDePersonas by lazy { RepositorioCampoDePersonasSQL(configuracionRepositorios) }
        override val creadoresRepositoriosUsados: List<CreadorRepositorio<*>> by lazy {
            listOf<CreadorRepositorio<*>>(repositorio)
        }


        @Nested
        inner class Crear
        {
            @TestConMultiplesDAO
            fun retorna_misma_entidad_con_id_asignado_por_bd()
            {
                val entidadAInsertar = ListadoCamposPredeterminados()
                val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

                assertEquals(entidadAInsertar.campos.toList(), entidadCreada)
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_EsquemaNoExiste_si_cliente_con_id_dado_no_existe()
            {
                val entidadAInsertar = ListadoCamposPredeterminados()

                assertThrows<EsquemaNoExiste> { repositorio.crear(idClientePruebas + 100, entidadAInsertar) }
            }
        }

        @Nested
        inner class Consultar
        {
            @BeforeEach
            fun crearCamposPredeterminados()
            {
                repositorio.crear(idClientePruebas, ListadoCamposPredeterminados())
            }

            @TestConMultiplesDAO
            fun por_id_existente_retorna_entidad_correcta()
            {
                val assertsQueFallaran =
                        CampoDePersona.Predeterminado.values().map {
                            Executable {
                                val campoEsperado = CampoDePersona(it, false)
                                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, it)

                                assertEquals(campoEsperado, entidadConsultada)
                            }
                        }

                Assertions.assertAll("Algunos campos no se pudieron consultar", *(assertsQueFallaran.toTypedArray()))
            }

            @TestConMultiplesDAO
            fun listado_de_entidades_retorna_entidades_correctas()
            {
                val entidadesPrueba =
                        listOf(
                                CampoDePersona(CampoDePersona.Predeterminado.NOMBRE_COMPLETO, false),
                                CampoDePersona(CampoDePersona.Predeterminado.TIPO_DOCUMENTO, false),
                                CampoDePersona(CampoDePersona.Predeterminado.NUMERO_DOCUMENTO, false),
                                CampoDePersona(CampoDePersona.Predeterminado.GENERO, false),
                                CampoDePersona(CampoDePersona.Predeterminado.FECHA_NACIMIENTO, false),
                                CampoDePersona(CampoDePersona.Predeterminado.CATEGORIA, false),
                                CampoDePersona(CampoDePersona.Predeterminado.AFILIACION, false),
                                CampoDePersona(CampoDePersona.Predeterminado.ES_ANONIMA, false),
                                CampoDePersona(CampoDePersona.Predeterminado.LLAVE_IMAGEN, false),
                                CampoDePersona(CampoDePersona.Predeterminado.EMPRESA, false)
                              )

                val listadoEsperado = entidadesPrueba.sortedBy { it.campo }

                val listadoConsultado = repositorio.listar(idClientePruebas)

                assertEquals(listadoEsperado, listadoConsultado.sortedBy { it.campo }.toList())
            }

            @TestConMultiplesDAO
            fun inicializar_nuevamente_con_diferente_instancia_del_repositorio_mantiene_entidades_correctas()
            {
                RepositorioCampoDePersonasSQL(configuracionRepositorios).inicializarParaCliente(idClientePruebas)
                val entidadesPrueba =
                        listOf(
                                CampoDePersona(CampoDePersona.Predeterminado.NOMBRE_COMPLETO, false),
                                CampoDePersona(CampoDePersona.Predeterminado.TIPO_DOCUMENTO, false),
                                CampoDePersona(CampoDePersona.Predeterminado.NUMERO_DOCUMENTO, false),
                                CampoDePersona(CampoDePersona.Predeterminado.GENERO, false),
                                CampoDePersona(CampoDePersona.Predeterminado.FECHA_NACIMIENTO, false),
                                CampoDePersona(CampoDePersona.Predeterminado.CATEGORIA, false),
                                CampoDePersona(CampoDePersona.Predeterminado.AFILIACION, false),
                                CampoDePersona(CampoDePersona.Predeterminado.ES_ANONIMA, false),
                                CampoDePersona(CampoDePersona.Predeterminado.LLAVE_IMAGEN, false),
                                CampoDePersona(CampoDePersona.Predeterminado.EMPRESA, false)
                              )

                val listadoEsperado = entidadesPrueba.sortedBy { it.campo }

                val listadoConsultado = repositorio.listar(idClientePruebas)

                assertEquals(listadoEsperado, listadoConsultado.sortedBy { it.campo }.toList())
            }
        }

        @Nested
        inner class Actualizar
        {
            @BeforeEach
            fun crearCamposPredeterminados()
            {
                repositorio.crear(idClientePruebas, ListadoCamposPredeterminados())
            }


            @TestConMultiplesDAO
            fun se_actualizan_todos_los_campos_correctamente()
            {
                val assertsQueFallaran =
                        CampoDePersona.Predeterminado.values().map {
                            Executable {
                                val campoEsperado = CampoDePersona(it, true)

                                repositorio.actualizar(idClientePruebas, campoEsperado.campo, campoEsperado)

                                val entidadActualizada = repositorio.buscarPorId(idClientePruebas, it)

                                assertEquals(campoEsperado, entidadActualizada)
                            }
                        }

                Assertions.assertAll("Algunos campos no se pudieron consultar", *(assertsQueFallaran.toTypedArray()))
            }

            @TestConMultiplesDAO
            fun inicializar_nuevamente_con_diferente_instancia_del_repositorio_mantiene_entidades_correctas_despues_de_editarlas()
            {
                CampoDePersona.Predeterminado.values().forEach {
                    val campoEsperado = CampoDePersona(it, true)

                    repositorio.actualizar(idClientePruebas, campoEsperado.campo, campoEsperado)

                    val entidadActualizada = repositorio.buscarPorId(idClientePruebas, it)

                    assertEquals(campoEsperado, entidadActualizada)
                }

                RepositorioCampoDePersonasSQL(configuracionRepositorios).inicializarParaCliente(idClientePruebas)
                val entidadesPrueba =
                        listOf(
                                CampoDePersona(CampoDePersona.Predeterminado.NOMBRE_COMPLETO, true),
                                CampoDePersona(CampoDePersona.Predeterminado.TIPO_DOCUMENTO, true),
                                CampoDePersona(CampoDePersona.Predeterminado.NUMERO_DOCUMENTO, true),
                                CampoDePersona(CampoDePersona.Predeterminado.GENERO, true),
                                CampoDePersona(CampoDePersona.Predeterminado.FECHA_NACIMIENTO, true),
                                CampoDePersona(CampoDePersona.Predeterminado.CATEGORIA, true),
                                CampoDePersona(CampoDePersona.Predeterminado.AFILIACION, true),
                                CampoDePersona(CampoDePersona.Predeterminado.ES_ANONIMA, true),
                                CampoDePersona(CampoDePersona.Predeterminado.LLAVE_IMAGEN, true),
                                CampoDePersona(CampoDePersona.Predeterminado.EMPRESA, false)
                              )

                val listadoEsperado = entidadesPrueba.sortedBy { it.campo }

                val listadoConsultado = repositorio.listar(idClientePruebas)

                assertEquals(listadoEsperado, listadoConsultado.sortedBy { it.campo }.toList())
            }

            @TestConMultiplesDAO
            fun solo_se_modifican_los_del_cliente_con_id_dado()
            {
                ejecutarConClienteAlternativo { cliente ->
                    repositorio.crear(cliente.id!!, ListadoCamposPredeterminados())

                    CampoDePersona.Predeterminado.values().forEach {
                        val campoEsperado = CampoDePersona(it, true)

                        repositorio.actualizar(cliente.id!!, campoEsperado.campo, campoEsperado)

                        val entidadActualizada = repositorio.buscarPorId(cliente.id!!, it)

                        assertEquals(campoEsperado, entidadActualizada)
                    }
                }

                val entidadesPrueba = CampoDePersona.Predeterminado.values().map { CampoDePersona(it, false) }
                val listadoEsperado = entidadesPrueba.sortedBy { it.campo }
                val listadoConsultado = repositorio.listar(idClientePruebas)

                assertEquals(listadoEsperado, listadoConsultado.sortedBy { it.campo }.toList())
            }
        }
    }
}