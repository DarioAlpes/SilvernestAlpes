package co.smartobjects.persistencia.ubicaciones.contabilizables

import co.smartobjects.entidades.ubicaciones.Ubicacion
import co.smartobjects.entidades.ubicaciones.contabilizables.UbicacionesContabilizables
import co.smartobjects.persistencia.EntidadDAOBasePruebas
import co.smartobjects.persistencia.TestConMultiplesDAO
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorio
import co.smartobjects.persistencia.excepciones.ErrorDeLlaveForanea
import co.smartobjects.persistencia.excepciones.EsquemaNoExiste
import co.smartobjects.persistencia.listarTodasLasUbicacionContabilizableDAO
import co.smartobjects.persistencia.ubicaciones.RepositorioUbicaciones
import co.smartobjects.persistencia.ubicaciones.RepositorioUbicacionesSQL
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue


@DisplayName("UbicacionContabilizableDAO")
internal class UbicacionContabilizableDAOPruebas : EntidadDAOBasePruebas()
{
    private val repositorioUbicaciones: RepositorioUbicaciones by lazy { RepositorioUbicacionesSQL(configuracionRepositorios) }
    private val repositorio: RepositorioUbicacionesContabilizables by lazy { RepositorioUbicacionesContabilizablesSQL(configuracionRepositorios) }

    override val creadoresRepositoriosUsados: List<CreadorRepositorio<*>> by lazy {
        listOf<CreadorRepositorio<*>>(repositorioUbicaciones, repositorio)
    }


    private val ubicacionesPorDefecto: List<Ubicacion> by lazy {
        List(5) {
            repositorioUbicaciones.crear(
                    idClientePruebas,
                    Ubicacion(
                            idClientePruebas,
                            it + 10000L,
                            "Ubicacion $it",
                            Ubicacion.Tipo.PROPIEDAD,
                            Ubicacion.Subtipo.POS,
                            null,
                            linkedSetOf()
                             )
                                        )
        }
    }

    private fun darInstanciaEntidadValida(): UbicacionesContabilizables
    {
        return UbicacionesContabilizables(idClientePruebas, ubicacionesPorDefecto.map { it.id!! }.toSet())
    }

    @Nested
    inner class Crear
    {
        @TestConMultiplesDAO
        fun retona_listado_de_ids_de_las_ubicaciones_a_contabilizar()
        {
            val entidadAInsertar = darInstanciaEntidadValida()
            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertEquals(entidadAInsertar.idsUbicaciones.toList().sortedBy { it }, entidadCreada.sortedBy { it })

            val entidadesEnBD = listarTodasLasUbicacionContabilizableDAO(idClientePruebas, configuracionRepositorios)
            assertEquals(ubicacionesPorDefecto.size, entidadesEnBD.size)
            assertEquals(ubicacionesPorDefecto.map { it.id!! }, entidadesEnBD.map { it.ubicacionDAO.id!! })
        }

        @TestConMultiplesDAO
        fun siempre_se_reemplazan_todas_las_ubicaciones_contabilizables()
        {
            val nuevasUbicaciones =
                    List(13) {
                        repositorioUbicaciones.crear(
                                idClientePruebas,
                                Ubicacion(
                                        idClientePruebas,
                                        null,
                                        "Ubicacion ${it + ubicacionesPorDefecto.size}",
                                        Ubicacion.Tipo.PROPIEDAD,
                                        Ubicacion.Subtipo.POS,
                                        null,
                                        linkedSetOf()
                                         )
                                                    )
                    }

            val idsNuevasUbicaciones = nuevasUbicaciones.map { it.id!! }

            val entidadAInsertar = darInstanciaEntidadValida()
            repositorio.crear(idClientePruebas, entidadAInsertar)

            val entidadParaReemplazar = UbicacionesContabilizables(idClientePruebas, idsNuevasUbicaciones.toSet())
            val entidadCreadaReemplazada = repositorio.crear(idClientePruebas, entidadParaReemplazar)
            assertEquals(entidadParaReemplazar.idsUbicaciones.toList().sortedBy { it }, entidadCreadaReemplazada.sortedBy { it })

            val entidadesEnBD = listarTodasLasUbicacionContabilizableDAO(idClientePruebas, configuracionRepositorios)
            assertEquals(nuevasUbicaciones.size, entidadesEnBD.size)
            assertEquals(idsNuevasUbicaciones, entidadesEnBD.map { it.ubicacionDAO.id!! })
        }

        @TestConMultiplesDAO
        fun funciona_con_set_vacio_y_elimina_todos_las_ubicaciones_contabilizables()
        {
            val entidadAInsertar = darInstanciaEntidadValida()
            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            val entidadParaReemplazar = UbicacionesContabilizables(idClientePruebas, emptySet())
            val entidadCreadaReemplazada = repositorio.crear(idClientePruebas, entidadParaReemplazar)
            assertEquals(entidadParaReemplazar.idsUbicaciones.toList().sortedBy { it }, entidadCreadaReemplazada.sortedBy { it })

            val entidadesEnBD = listarTodasLasUbicacionContabilizableDAO(idClientePruebas, configuracionRepositorios)
            assertTrue(entidadesEnBD.isEmpty())
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_ErrorDeLlaveForanea_si_usa_una_ubicacion_inexistente()
        {
            val entidadAInsertar = darInstanciaEntidadValida().run {
                copiar(idsUbicaciones = idsUbicaciones.map { it + 123456L }.toSet())
            }

            assertThrows<ErrorDeLlaveForanea> { repositorio.crear(idClientePruebas, entidadAInsertar) }
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
        fun listado_de_entidades_sin_entidades_retorna_lista_vacia()
        {
            val listadoConsultado = repositorio.listar(idClientePruebas)

            assertTrue(listadoConsultado.none())
        }

        @TestConMultiplesDAO
        fun listado_de_entidades_retorna_entidades_correctas()
        {
            val entidadCreada = repositorio.crear(idClientePruebas, darInstanciaEntidadValida())

            val listadoConsultado = repositorio.listar(idClientePruebas).sortedBy { it }.toList()

            assertEquals(entidadCreada.sortedBy { it }, listadoConsultado)
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

        @TestConMultiplesDAO
        fun lanza_excepcion_EsquemaNoExiste_si_se_consulta_listado_de_entidades_con_id_cliente_inexistente()
        {
            assertThrows<EsquemaNoExiste> { repositorio.listar(idClientePruebas + 100) }
        }
    }

    @[Nested DisplayName("lanza LanzaErrorDeLlaveForanea y no elimina entidad si intenta borrar")]
    inner class LanzaErrorDeLlaveForanea
    {
        @BeforeEach
        fun crearUbicacionesContabilizables()
        {
            val entidadAInsertar = darInstanciaEntidadValida()
            repositorio.crear(idClientePruebas, entidadAInsertar)
        }


        @TestConMultiplesDAO
        fun la_ubicacion_asociada_a_ubicacion_contabilizable()
        {
            assertThrows<ErrorDeLlaveForanea> {
                repositorioUbicaciones.eliminarPorId(idClientePruebas, ubicacionesPorDefecto.first().id!!)
            }
        }
    }
}