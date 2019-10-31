package co.smartobjects.persistencia.usuarios.roles

import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.entidades.usuarios.Rol
import co.smartobjects.persistencia.EntidadDAOBasePruebas
import co.smartobjects.persistencia.TestConMultiplesDAO
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorio
import co.smartobjects.persistencia.excepciones.EntidadNoExiste
import co.smartobjects.persistencia.excepciones.ErrorCreacionActualizacionPorDuplicidad
import co.smartobjects.persistencia.excepciones.EsquemaNoExiste
import co.smartobjects.persistencia.existeAlgunPermisoBackDAO
import co.smartobjects.persistencia.existeAlgunRolDAO
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import kotlin.test.assertEquals

@DisplayName("RolesDAO")
internal class RolesDAOPruebas : EntidadDAOBasePruebas()
{
    companion object
    {
        private const val NOMBRE_ROL_POR_DEFECTO = "Configuración"
    }

    private val repositorio: RepositorioRoles by lazy { RepositorioRolesSQL(configuracionRepositorios) }
    override val creadoresRepositoriosUsados: List<CreadorRepositorio<*>> by lazy {
        listOf<CreadorRepositorio<*>>(repositorio)
    }

    private fun darInstanciaEntidadValida(): Rol
    {
        val permisos =
                setOf(
                        PermisoBack(idClientePruebas, "ElEndpoint", PermisoBack.Accion.POST),
                        PermisoBack(idClientePruebas, "ElEndpoint", PermisoBack.Accion.PUT)
                     )

        return Rol("ElRol", "Descripcion", permisos)
    }

    @Nested
    inner class Crear
    {
        @TestConMultiplesDAO
        fun retorna_misma_entidad()
        {
            val entidadAInsertar = darInstanciaEntidadValida()
            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertEquals(entidadAInsertar, entidadCreada)
        }

        @TestConMultiplesDAO
        fun permite_crear_dos_roles_con_los_mismos_permisos()
        {
            val entidadAInsertar = darInstanciaEntidadValida()
            val entidadAInsertar2 = darInstanciaEntidadValida().copiar(nombre = "Segundo rol")
            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)
            val entidadCreada2 = repositorio.crear(idClientePruebas, entidadAInsertar2)

            assertEquals(entidadAInsertar, entidadCreada)
            assertEquals(entidadAInsertar2, entidadCreada2)
        }


        @TestConMultiplesDAO
        fun usa_un_solo_permiso()
        {
            val entidadOriginal = darInstanciaEntidadValida()
            val entidadAInsertar = entidadOriginal.copiar(permisos = setOf(entidadOriginal.permisos.first()))
            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertEquals(entidadAInsertar, entidadCreada)
        }

        @TestConMultiplesDAO
        fun usa_todas_las_posibles_acciones()
        {
            val permisos = PermisoBack.Accion.values().map { PermisoBack(idClientePruebas, "elEndpoint", it) }.toSet()
            val entidadAInsertar = darInstanciaEntidadValida().copiar(permisos = permisos)
            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertEquals(entidadAInsertar, entidadCreada)
        }

        @TestConMultiplesDAO
        fun ignora_los_ids_de_cliente_de_los_permisos_y_retorna_misma_entidad_con_id_cliente_dado_por_parametro_asignado_por_bd()
        {
            val entidadConIdsCorrectos = darInstanciaEntidadValida()
            val entidadAInsertar = entidadConIdsCorrectos.copiar(permisos = entidadConIdsCorrectos.permisos.map { (it as PermisoBack).copiar(idCliente = 99999) }.toSet())

            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertEquals(entidadConIdsCorrectos, entidadCreada)
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_con_ErrorCreacionActualizacionPorDuplicidad_si_se_usa_nombre_duplicado()
        {
            val entidadAInsertar = darInstanciaEntidadValida()

            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertThrows(ErrorCreacionActualizacionPorDuplicidad::class.java, { repositorio.crear(idClientePruebas, entidadCreada.copiar(permisos = setOf(entidadAInsertar.permisos.first()))) })
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_EsquemaNoExiste_si_cliente_con_id_dado_no_existe()
        {
            val entidadAInsertar = darInstanciaEntidadValida()

            assertThrows(EsquemaNoExiste::class.java, { repositorio.crear(idClientePruebas + 100, entidadAInsertar) })
        }
    }

    @Nested
    inner class Consultar
    {
        @TestConMultiplesDAO
        fun por_id_existente_retorna_entidad_correcta()
        {
            val entidadPrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadValida())

            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadPrueba.nombre)

            assertEquals(entidadPrueba, entidadConsultada)
        }

        @TestConMultiplesDAO
        fun con_un_solo_permiso_retorna_entidad_correcta()
        {
            val entidadOriginal = darInstanciaEntidadValida()
            val entidadAInsertar = entidadOriginal.copiar(permisos = setOf(entidadOriginal.permisos.first()))
            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadCreada.nombre)

            assertEquals(entidadCreada, entidadConsultada)
        }

        @TestConMultiplesDAO
        fun con_todas_las_posibles_acciones_retorna_entidad_correcta()
        {
            val permisos = PermisoBack.Accion.values().map { PermisoBack(idClientePruebas, "elEndpoint", it) }.toSet()
            val entidadAInsertar = darInstanciaEntidadValida().copiar(permisos = permisos)

            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadCreada.nombre)

            assertEquals(entidadCreada, entidadConsultada)
        }

        @TestConMultiplesDAO
        fun listado_de_entidades_retorna_entidades_correctas()
        {
            val entidadAInsertar = darInstanciaEntidadValida()

            val entidadAInsertar2 = darInstanciaEntidadValida().copiar(nombre = "Segundo rol")

            val permisos = PermisoBack.Accion.values().map { PermisoBack(idClientePruebas, "elEndpoint", it) }.toSet()

            val entidadAInsertar3 = darInstanciaEntidadValida().copiar(nombre = "Tercer rol", permisos = permisos)

            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)
            val entidadCreada2 = repositorio.crear(idClientePruebas, entidadAInsertar2)
            val entidadCreada3 = repositorio.crear(idClientePruebas, entidadAInsertar3)

            val entidadesPrueba =
                    listOf(
                            entidadCreada,
                            entidadCreada2,
                            entidadCreada3
                          )

            val listadoEsperado = entidadesPrueba.sortedBy { it.nombre }

            val listadoConsultado = repositorio.listar(idClientePruebas)

            assertEquals(listadoEsperado, listadoConsultado.sortedBy { it.nombre }.toList())
        }

        @TestConMultiplesDAO
        fun por_id_no_existente_retorna_null()
        {
            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, "Rol no existente")

            Assertions.assertNull(entidadConsultada)
        }

        @TestConMultiplesDAO
        fun por_id_existente_en_otro_cliente_retorna_null()
        {
            val entidadPrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadValida())

            ejecutarConClienteAlternativo {
                val entidadConsultada = repositorio.buscarPorId(it.id!!, entidadPrueba.nombre)
                Assertions.assertNull(entidadConsultada)
            }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_EsquemaNoExiste_si_se_consulta_por_id_con_id_cliente_inexistente()
        {
            assertThrows(EsquemaNoExiste::class.java, { repositorio.buscarPorId(idClientePruebas + 100, "Rol no existente") })
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_EsquemaNoExiste_si_se_consulta_listado_de_entidades_con_id_cliente_inexistente()
        {
            assertThrows(EsquemaNoExiste::class.java, { repositorio.listar(idClientePruebas + 100) })
        }

        @TestConMultiplesDAO
        fun listado_de_entidades_con_entidades_en_otro_cliente_retorna_lista_vacia()
        {
            repositorio.crear(idClientePruebas, darInstanciaEntidadValida())

            ejecutarConClienteAlternativo {
                val listadoConsultado = repositorio.listar(it.id!!)
                assertEquals(emptyList(), listadoConsultado.toList())
            }
        }
    }

    @Nested
    inner class Actualizar
    {
        private lateinit var entidadDePrueba: Rol

        @BeforeEach
        fun crearEntidadDePrueba()
        {
            entidadDePrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadValida())
        }

        @TestConMultiplesDAO
        fun se_actualizan_todos_los_campos_correctamente()
        {
            val entidadModificada = Rol(
                    entidadDePrueba.nombre,
                    "Descripcion nueva",
                    setOf(PermisoBack(idClientePruebas, "ElOtroEndpoint", PermisoBack.Accion.GET_TODOS), PermisoBack(idClientePruebas, "ElOtroEndpoint", PermisoBack.Accion.GET_UNO))
                                       )
            val entidadActualizada = repositorio.actualizar(idClientePruebas, entidadModificada.nombre, entidadModificada)
            assertEquals(entidadModificada, entidadActualizada)
        }

        @TestConMultiplesDAO
        fun utiliza_el_parametro_id_cliente_y_retorna_misma_entidad_con_id_cliente_correcto_en_los_permisos()
        {
            val entidadConCambios = entidadDePrueba.copiar(permisos = entidadDePrueba.permisos.map { (it as PermisoBack).copiar(idCliente = 987654) }.toSet())

            val entidadActualizada = repositorio.actualizar(idClientePruebas, entidadConCambios.nombre, entidadConCambios)

            assertEquals(entidadDePrueba, entidadActualizada)
        }

        @TestConMultiplesDAO
        fun con_id_inexistente_lanza_excepcion_EntidadNoExiste()
        {
            val entidadConCambios = entidadDePrueba.copiar(nombre = "Rol inexistente")

            assertThrows(EntidadNoExiste::class.java, { repositorio.actualizar(idClientePruebas, entidadConCambios.nombre, entidadConCambios) })
        }

        @TestConMultiplesDAO
        fun con_id_existente_en_otro_cliente_lanza_excepcion_EntidadNoExiste()
        {
            ejecutarConClienteAlternativo {
                assertThrows(EntidadNoExiste::class.java, { repositorio.actualizar(it.id!!, entidadDePrueba.nombre, entidadDePrueba) })
            }
        }

        @TestConMultiplesDAO
        fun con_id_cliente_inexistente_lanza_excepcion_EsquemaNoExiste()
        {
            assertThrows(EsquemaNoExiste::class.java, { repositorio.actualizar(idClientePruebas + 100, entidadDePrueba.nombre, entidadDePrueba) })
        }
    }

    @Nested
    inner class Eliminar
    {
        private lateinit var entidadPrueba: Rol

        @BeforeEach
        fun crearEntidadDePrueba()
        {
            entidadPrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadValida())
        }

        @TestConMultiplesDAO
        fun si_existe_la_entidad_se_elimina_correctamente()
        {
            val resultadoEliminacion = repositorio.eliminarPorId(idClientePruebas, entidadPrueba.nombre)
            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadPrueba.nombre)

            Assertions.assertTrue(resultadoEliminacion)
            Assertions.assertNull(entidadConsultada)
        }

        @TestConMultiplesDAO
        fun se_eliminan_permisos()
        {
            val resultadoEliminacion = repositorio.eliminarPorId(idClientePruebas, entidadPrueba.nombre)
            assertTrue(resultadoEliminacion)
            assertFalse(existeAlgunRolDAO(idClientePruebas, configuracionRepositorios))
            assertFalse(existeAlgunPermisoBackDAO(idClientePruebas, configuracionRepositorios))
        }

        @TestConMultiplesDAO
        fun si_no_existe_la_entidad_retorna_false_y_no_elimina_entidad_existente()
        {
            val resultadoEliminacion = repositorio.eliminarPorId(idClientePruebas, "${entidadPrueba.nombre} inexistente")
            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadPrueba.nombre)

            Assertions.assertFalse(resultadoEliminacion)
            assertEquals(entidadPrueba, entidadConsultada)
        }

        @TestConMultiplesDAO
        fun con_id_existente_en_otro_cliente_retorna_false_y_no_elimina_entidad_existente()
        {
            ejecutarConClienteAlternativo {
                val resultadoEliminacion = repositorio.eliminarPorId(it.id!!, entidadPrueba.nombre)
                kotlin.test.assertFalse(resultadoEliminacion)
            }
            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadPrueba.nombre)
            assertEquals(entidadPrueba, entidadConsultada)
        }

        @TestConMultiplesDAO
        fun con_id_cliente_inexistente_lanza_excepcion_EsquemaNoExiste()
        {
            assertThrows(EsquemaNoExiste::class.java, { repositorio.eliminarPorId(idClientePruebas + 100, entidadPrueba.nombre) })
        }
    }
}