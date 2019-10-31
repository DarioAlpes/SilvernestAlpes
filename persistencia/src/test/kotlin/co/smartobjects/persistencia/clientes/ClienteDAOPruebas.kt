package co.smartobjects.persistencia.clientes

import co.smartobjects.entidades.clientes.Cliente
import co.smartobjects.persistencia.EntidadDAOBasePruebas
import co.smartobjects.persistencia.TestConMultiplesDAO
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorio
import co.smartobjects.persistencia.excepciones.EntidadNoExiste
import co.smartobjects.persistencia.excepciones.ErrorCreacionActualizacionPorDuplicidad
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("ClienteDAO")
internal class ClienteDAOPruebas : EntidadDAOBasePruebas(false)
{
    override val creadoresRepositoriosUsados: List<CreadorRepositorio<*>> = listOf()

    private fun darInstanciaEntidadValida(): Cliente
    {
        return Cliente(
                null,
                "Cliente prueba"
                      )
    }


    @Nested
    inner class Crear
    {
        @TestConMultiplesDAO
        fun sin_id_asignado_retorna_misma_entidad_con_id_asignado_por_bd()
        {
            val entidadAInsertar = darInstanciaEntidadValida()
            val entidadCreada = repositorioClientes.crear(entidadAInsertar)

            assertNotNull(entidadCreada.id)
            assertEquals(entidadAInsertar.copiar(id = entidadCreada.id), entidadCreada)
        }

        @TestConMultiplesDAO
        fun con_id_asignado_retorna_misma_entidad_con_id_original()
        {
            val entidadAInsertar = darInstanciaEntidadValida().copiar(id = 9876543)

            val entidadCreada = repositorioClientes.crear(entidadAInsertar)

            assertEquals(entidadAInsertar.id, entidadCreada.id)
            assertEquals(entidadAInsertar.copiar(id = entidadCreada.id), entidadCreada)
        }

        @TestConMultiplesDAO
        fun crea()
        {
            val entidadAInsertar = darInstanciaEntidadValida()
            val entidadCreada = repositorioClientes.crear(entidadAInsertar)

            assertNotNull(entidadCreada.id)
            assertEquals(entidadAInsertar.copiar(id = entidadCreada.id), entidadCreada)
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_con_ErrorCreacionActualizacionPorDuplicidad_si_se_usa_nombre_duplicado()
        {
            val entidadAInsertar = darInstanciaEntidadValida()

            val entidadCreada = repositorioClientes.crear(entidadAInsertar)

            assertThrows<ErrorCreacionActualizacionPorDuplicidad> { repositorioClientes.crear(entidadCreada) }
        }
    }

    @Nested
    inner class Consultar
    {
        @TestConMultiplesDAO
        fun por_id_existente_retorna_entidad_correcta()
        {
            val entidadPrueba = repositorioClientes.crear(darInstanciaEntidadValida())

            val entidadConsultada = repositorioClientes.buscarPorId(entidadPrueba.id!!)

            assertEquals(entidadPrueba, entidadConsultada)
        }

        @TestConMultiplesDAO
        fun listado_de_entidades_sin_entidades_retorna_lista_vacia()
        {
            val listadoConsultado = repositorioClientes.listar()

            assertTrue(listadoConsultado.none())
        }

        @TestConMultiplesDAO
        fun listado_de_entidades_retorna_entidades_correctas()
        {
            val entidadesPrueba = (1..3).map {
                repositorioClientes.crear(
                        darInstanciaEntidadValida()
                            .copiar(nombre = "Entidad $it")
                                         )
            }

            val listadoEsperado = entidadesPrueba.sortedBy { it.id!! }


            val listadoConsultado = repositorioClientes.listar().toList()

            assertEquals(listadoEsperado, listadoConsultado.sortedBy { it.id!! })
        }

        @TestConMultiplesDAO
        fun por_id_no_existente_retorna_null()
        {
            val entidadConsultada = repositorioClientes.buscarPorId(789456789)

            assertNull(entidadConsultada)
        }
    }

    @Nested
    inner class Actualizar
    {
        private lateinit var entidadDePrueba: Cliente

        @BeforeEach
        fun crearCategoriaPrueba()
        {
            entidadDePrueba = repositorioClientes.crear(darInstanciaEntidadValida())
        }

        @TestConMultiplesDAO
        fun se_actualizan_todos_los_campos_correctamente()
        {
            val entidadModificada = Cliente(
                    entidadDePrueba.id,
                    "Cliente prueba modificado"
                                           )
            val entidadActualizada = repositorioClientes.actualizar(entidadModificada.id!!, entidadModificada)
            assertEquals(entidadModificada, entidadActualizada)
        }

        @TestConMultiplesDAO
        fun con_id_inexistente_lanza_excepcion_EntidadNoExiste()
        {
            val entidadConCambios = entidadDePrueba.copiar(id = 789456)

            assertThrows<EntidadNoExiste> { repositorioClientes.actualizar(entidadConCambios.id!!, entidadConCambios) }
        }

        @TestConMultiplesDAO
        fun con_nombre_duplicado_lanza_excepcion_ErrorCreacionActualizacionPorDuplicidad()
        {
            val entidadConCambios = repositorioClientes.crear(
                    darInstanciaEntidadValida().copiar(nombre = "Entidad con cambio")
                                                             )

            assertThrows<ErrorCreacionActualizacionPorDuplicidad> {
                repositorioClientes.actualizar(entidadConCambios.id!!, entidadConCambios.copiar(nombre = entidadDePrueba.nombre))
            }
        }
    }

    @Nested
    inner class Eliminar
    {
        private lateinit var entidadPrueba: Cliente

        @BeforeEach
        fun crearEntidadDePrueba()
        {
            entidadPrueba = repositorioClientes.crear(darInstanciaEntidadValida())
        }

        @TestConMultiplesDAO
        fun si_existe_la_entidad_se_elimina_correctamente()
        {
            val resultadoEliminacion = repositorioClientes.eliminarPorId(entidadPrueba.id!!)
            val entidadConsultada = repositorioClientes.buscarPorId(entidadPrueba.id!!)

            assertTrue(resultadoEliminacion)
            assertNull(entidadConsultada)
        }

        @TestConMultiplesDAO
        fun si_no_existe_la_entidad_retorna_false_y_no_elimina_entidad_existente()
        {
            val resultadoEliminacion = repositorioClientes.eliminarPorId(entidadPrueba.id!! + 1)
            val entidadConsultada = repositorioClientes.buscarPorId(entidadPrueba.id!!)

            assertFalse(resultadoEliminacion)
            assertEquals(entidadPrueba, entidadConsultada)
        }
    }
}