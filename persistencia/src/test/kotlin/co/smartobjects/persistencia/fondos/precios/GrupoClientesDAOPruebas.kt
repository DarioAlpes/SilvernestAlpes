package co.smartobjects.persistencia.fondos.precios

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.fondos.precios.GrupoClientes
import co.smartobjects.entidades.fondos.precios.SegmentoClientes
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.personas.ValorGrupoEdad
import co.smartobjects.persistencia.EntidadDAOBasePruebas
import co.smartobjects.persistencia.TestConMultiplesDAO
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorio
import co.smartobjects.persistencia.excepciones.*
import co.smartobjects.persistencia.existeAlgunGrupoClientesDAO
import co.smartobjects.persistencia.existeAlgunSegmentoClientesDAO
import co.smartobjects.persistencia.fondos.precios.gruposclientes.RepositorioGrupoClientes
import co.smartobjects.persistencia.fondos.precios.gruposclientes.RepositorioGrupoClientesSQL
import co.smartobjects.persistencia.personas.valorgrupoedad.RepositorioValoresGruposEdad
import co.smartobjects.persistencia.personas.valorgrupoedad.RepositorioValoresGruposEdadSQL
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import kotlin.test.*

@DisplayName("GrupoClientesDAO")
internal class GrupoClientesDAOPruebas : EntidadDAOBasePruebas()
{
    companion object
    {
        const val VALOR_GRUPO_EDAD_VALIDO = "Edad"
    }

    private val repositorioValoresGruposEdad: RepositorioValoresGruposEdad by lazy { RepositorioValoresGruposEdadSQL(configuracionRepositorios) }
    private val repositorio: RepositorioGrupoClientes by lazy { RepositorioGrupoClientesSQL(configuracionRepositorios) }
    override val creadoresRepositoriosUsados: List<CreadorRepositorio<*>> by lazy {
        listOf<CreadorRepositorio<*>>(repositorioValoresGruposEdad, repositorio)
    }

    private val segmentoCategoria = SegmentoClientes(null, SegmentoClientes.NombreCampo.CATEGORIA, Persona.Categoria.A.toString())
    private val segmentoGrupoEdad = SegmentoClientes(null, SegmentoClientes.NombreCampo.GRUPO_DE_EDAD, VALOR_GRUPO_EDAD_VALIDO)

    private fun darInstanciaEntidadValida(): GrupoClientes
    {
        val segmentos = listOf(segmentoCategoria, segmentoGrupoEdad)
        return GrupoClientes(
                null,
                "Nombre del grupo",
                segmentos
                            )
    }

    private fun crearValorGrupoEdadValido(idCliente: Long, valor: String, inicial: Int, final: Int): ValorGrupoEdad
    {
        return repositorioValoresGruposEdad.crear(idCliente, ValorGrupoEdad(valor, inicial, final))
    }

    private fun GrupoClientes.actualizarIdsSegmentos(segmentosConIds: List<SegmentoClientes>): GrupoClientes
    {
        return GrupoClientes(
                id,
                nombre,
                segmentosClientes.zip(segmentosConIds).map { it.first.copiar(id = it.second.id) }
                            )
    }

    @Nested
    inner class Crear
    {
        @TestConMultiplesDAO
        fun sin_id_asignado_retorna_misma_entidad_con_id_asignado_por_bd()
        {
            crearValorGrupoEdadValido(idClientePruebas, VALOR_GRUPO_EDAD_VALIDO, 0, 10)
            val entidadAInsertar = darInstanciaEntidadValida()
            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertNotNull(entidadCreada.id)
            assertEquals(entidadAInsertar.copiar(id = entidadCreada.id).actualizarIdsSegmentos(entidadCreada.segmentosClientes), entidadCreada)
        }

        @TestConMultiplesDAO
        fun con_id_asignado_retorna_misma_entidad_con_id_original()
        {
            crearValorGrupoEdadValido(idClientePruebas, VALOR_GRUPO_EDAD_VALIDO, 0, 10)
            val entidadAInsertar = darInstanciaEntidadValida().copiar(id = 9876543)

            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertEquals(entidadAInsertar.id, entidadCreada.id)
            assertEquals(entidadAInsertar.copiar(id = entidadCreada.id).actualizarIdsSegmentos(entidadCreada.segmentosClientes), entidadCreada)
        }

        @TestConMultiplesDAO
        fun usa_un_solo_segmento_de_categoria()
        {
            val entidadAInsertar = darInstanciaEntidadValida().copiar(segmentosClientes = listOf(segmentoCategoria))
            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertNotNull(entidadCreada.id)
            assertEquals(entidadAInsertar.copiar(id = entidadCreada.id).actualizarIdsSegmentos(entidadCreada.segmentosClientes), entidadCreada)
        }

        @TestConMultiplesDAO
        fun usa_un_solo_segmento_de_grupo_edad()
        {
            crearValorGrupoEdadValido(idClientePruebas, VALOR_GRUPO_EDAD_VALIDO, 0, 10)
            val entidadAInsertar = darInstanciaEntidadValida().copiar(segmentosClientes = listOf(segmentoGrupoEdad))
            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertNotNull(entidadCreada.id)
            assertEquals(entidadAInsertar.copiar(id = entidadCreada.id).actualizarIdsSegmentos(entidadCreada.segmentosClientes), entidadCreada)
        }

        @TestConMultiplesDAO
        fun ignora_los_ids_de_los_segmentos_y_retorna_misma_entidad_con_id_asignado_por_bd()
        {
            crearValorGrupoEdadValido(idClientePruebas, VALOR_GRUPO_EDAD_VALIDO, 0, 10)
            val entidadAInsertar = darInstanciaEntidadValida().run { copiar(segmentosClientes = segmentosClientes.map { it.copiar(id = 987654) }) }

            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertNotEquals(entidadAInsertar.id, entidadCreada.id)
            assertEquals(entidadAInsertar.copiar(id = entidadCreada.id).actualizarIdsSegmentos(entidadCreada.segmentosClientes), entidadCreada)
        }

        @TestConMultiplesDAO
        fun permite_crear_grupos_sobre_los_mismos_campos_con_diferentes_valores()
        {
            val valorNiño = "NIÑO"
            crearValorGrupoEdadValido(idClientePruebas, VALOR_GRUPO_EDAD_VALIDO, 0, 10)
            crearValorGrupoEdadValido(idClientePruebas, valorNiño, 11, 20)
            val entidadAInsertar = darInstanciaEntidadValida()

            repositorio.crear(idClientePruebas, entidadAInsertar)
            repositorio.crear(idClientePruebas, entidadAInsertar.copiar(segmentosClientes = listOf(segmentoCategoria.copiar(valor = "B"), segmentoGrupoEdad), nombre = "${entidadAInsertar.nombre} categoria B"))
            repositorio.crear(idClientePruebas, entidadAInsertar.copiar(segmentosClientes = listOf(segmentoCategoria, segmentoGrupoEdad.copiar(valor = valorNiño)), nombre = "${entidadAInsertar.nombre} niño"))
        }

        @TestConMultiplesDAO
        fun permite_crear_grupos_si_grupo_mas_especifico_ya_existe()
        {
            crearValorGrupoEdadValido(idClientePruebas, VALOR_GRUPO_EDAD_VALIDO, 0, 10)
            val entidadAInsertar = darInstanciaEntidadValida()

            repositorio.crear(idClientePruebas, entidadAInsertar)
            repositorio.crear(idClientePruebas, entidadAInsertar.copiar(segmentosClientes = listOf(segmentoGrupoEdad), nombre = "${entidadAInsertar.nombre} grupo edad"))
            repositorio.crear(idClientePruebas, entidadAInsertar.copiar(segmentosClientes = listOf(segmentoCategoria), nombre = "${entidadAInsertar.nombre} categoria"))
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_con_ErrorDeLlaveForanea_con_un_solo_segmento_de_grupo_edad_con_valor_inexistente()
        {
            val entidadAInsertar = darInstanciaEntidadValida().copiar(segmentosClientes = listOf(segmentoGrupoEdad))
            assertThrows<ErrorDeLlaveForanea> { repositorio.crear(idClientePruebas, entidadAInsertar) }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_con_ErrorDeLlaveForanea_con_un_segmento_de_grupo_edad_con_valor_inexistente_si_antes_hay_mas_segmentos_validos()
        {
            val entidadAInsertar = darInstanciaEntidadValida().copiar(segmentosClientes = listOf(segmentoCategoria, segmentoGrupoEdad))
            assertThrows<ErrorDeLlaveForanea> { repositorio.crear(idClientePruebas, entidadAInsertar) }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_con_ErrorCreacionViolacionDeRestriccion_si_se_intenta_crear_otro_grupo_sobre_el_mismo_segmento()
        {
            crearValorGrupoEdadValido(idClientePruebas, VALOR_GRUPO_EDAD_VALIDO, 0, 10)
            val entidadAInsertar = darInstanciaEntidadValida()

            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertThrows<ErrorCreacionViolacionDeRestriccion> { repositorio.crear(idClientePruebas, entidadCreada.copiar(nombre = "${entidadCreada.nombre} dos")) }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_con_ErrorCreacionActualizacionPorDuplicidad_si_se_usa_nombre_duplicado()
        {
            crearValorGrupoEdadValido(idClientePruebas, VALOR_GRUPO_EDAD_VALIDO, 0, 10)
            val entidadAInsertar = darInstanciaEntidadValida()

            repositorio.crear(idClientePruebas, entidadAInsertar)

            assertThrows<ErrorCreacionActualizacionPorDuplicidad> { repositorio.crear(idClientePruebas, entidadAInsertar.copiar(segmentosClientes = listOf(segmentoGrupoEdad))) }
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
            crearValorGrupoEdadValido(idClientePruebas, VALOR_GRUPO_EDAD_VALIDO, 0, 10)
            val entidadPrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadValida())

            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadPrueba.id!!)

            assertEquals(entidadPrueba, entidadConsultada)
        }

        @TestConMultiplesDAO
        fun con_un_solo_segmento_de_categoria_retorna_entidad_correcta()
        {
            val entidadAInsertar = darInstanciaEntidadValida().copiar(segmentosClientes = listOf(segmentoCategoria))

            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadCreada.id!!)

            assertEquals(entidadCreada, entidadConsultada)
        }

        @TestConMultiplesDAO
        fun con_un_solo_segmento_de_grupo_edad_retorna_entidad_correcta()
        {
            crearValorGrupoEdadValido(idClientePruebas, VALOR_GRUPO_EDAD_VALIDO, 0, 10)
            val entidadAInsertar = darInstanciaEntidadValida().copiar(segmentosClientes = listOf(segmentoGrupoEdad))

            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadCreada.id!!)

            assertEquals(entidadCreada, entidadConsultada)
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
            crearValorGrupoEdadValido(idClientePruebas, VALOR_GRUPO_EDAD_VALIDO, 0, 10)
            val entidadAInsertar = darInstanciaEntidadValida()
            val entidadConDosSegmentos = repositorio.crear(idClientePruebas, entidadAInsertar)
            val entidadConGrupoEdad = repositorio.crear(idClientePruebas, entidadAInsertar.copiar(segmentosClientes = listOf(segmentoGrupoEdad), nombre = "${entidadAInsertar.nombre} grupo edad"))
            val entidadConCategoria = repositorio.crear(idClientePruebas, entidadAInsertar.copiar(segmentosClientes = listOf(segmentoCategoria), nombre = "${entidadAInsertar.nombre} categoria"))

            val entidadesPrueba = listOf(entidadConDosSegmentos, entidadConGrupoEdad, entidadConCategoria)

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
            crearValorGrupoEdadValido(idClientePruebas, VALOR_GRUPO_EDAD_VALIDO, 0, 10)
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
            crearValorGrupoEdadValido(idClientePruebas, VALOR_GRUPO_EDAD_VALIDO, 0, 10)
            repositorio.crear(idClientePruebas, darInstanciaEntidadValida())

            ejecutarConClienteAlternativo {
                val listadoConsultado = repositorio.listar(it.id!!)
                assertTrue(listadoConsultado.none())
            }
        }
    }

    @Nested
    inner class ActualizarParcialmente
    {
        private lateinit var entidadDePrueba: GrupoClientes

        @BeforeEach
        fun crearCategoriaPrueba()
        {
            crearValorGrupoEdadValido(idClientePruebas, VALOR_GRUPO_EDAD_VALIDO, 0, 10)
            entidadDePrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadValida())
        }

        @TestConMultiplesDAO
        fun solo_se_actualizan_los_campos_necesarios()
        {
            repositorio.actualizarCamposIndividuales(
                    idClientePruebas,
                    entidadDePrueba.id!!,
                    mapOf<String, CampoModificable<GrupoClientes, *>>(entidadDePrueba.campoNombre.nombreCampo to entidadDePrueba.campoNombre)
                                                    )

            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.id!!)
            val entidadEsperada = entidadDePrueba.copiar(nombre = entidadDePrueba.nombre)
            assertEquals(entidadEsperada, entidadConsultada)
        }

        @TestConMultiplesDAO
        fun con_id_inexistente_lanza_excepcion_EntidadNoExiste()
        {
            assertThrows<EntidadNoExiste> {
                repositorio.actualizarCamposIndividuales(
                        idClientePruebas,
                        entidadDePrueba.id!! + 789456,
                        mapOf<String, CampoModificable<GrupoClientes, *>>(entidadDePrueba.campoNombre.nombreCampo to entidadDePrueba.campoNombre)
                                                        )
            }
        }

        @TestConMultiplesDAO
        fun con_nombre_duplicado_lanza_excepcion_ErrorCreacionActualizacionPorDuplicidad()
        {
            val entidadConCambios = repositorio.crear(
                    idClientePruebas,
                    GrupoClientes(null, entidadDePrueba.nombre + "Entidad con cambio", listOf(segmentoCategoria))
                                                     )

            assertThrows<ErrorCreacionActualizacionPorDuplicidad> {
                repositorio.actualizarCamposIndividuales(
                        idClientePruebas,
                        entidadConCambios.id!!,
                        mapOf<String, CampoModificable<GrupoClientes, *>>(entidadDePrueba.campoNombre.nombreCampo to entidadDePrueba.campoNombre)
                                                        )
            }
        }

        @TestConMultiplesDAO
        fun con_id_existente_en_otro_cliente_lanza_excepcion_EntidadNoExiste()
        {
            val entidadConCambios = repositorio.crear(
                    idClientePruebas,
                    GrupoClientes(null, entidadDePrueba.nombre + "Entidad con cambio", listOf(segmentoCategoria))
                                                     )

            ejecutarConClienteAlternativo {
                assertThrows<EntidadNoExiste> {
                    repositorio.actualizarCamposIndividuales(
                            it.id!!,
                            entidadConCambios.id!!,
                            mapOf<String, CampoModificable<GrupoClientes, *>>(entidadDePrueba.campoNombre.nombreCampo to entidadDePrueba.campoNombre)
                                                            )
                }
            }
        }

        @TestConMultiplesDAO
        fun con_id_cliente_inexistente_lanza_excepcion_EsquemaNoExiste()
        {
            val entidadConCambios = repositorio.crear(
                    idClientePruebas,
                    GrupoClientes(null, entidadDePrueba.nombre + "Entidad con cambio", listOf(segmentoCategoria))
                                                     )

            assertThrows<EsquemaNoExiste> {
                repositorio.actualizarCamposIndividuales(
                        idClientePruebas + 100,
                        entidadConCambios.id!!,
                        mapOf<String, CampoModificable<GrupoClientes, *>>(entidadDePrueba.campoNombre.nombreCampo to entidadDePrueba.campoNombre)
                                                        )
            }
        }

        @TestConMultiplesDAO
        fun permite_actualizar_valor_grupo_edad_usado_si_no_se_cambia_nombre()
        {
            repositorioValoresGruposEdad.actualizar(
                    idClientePruebas,
                    VALOR_GRUPO_EDAD_VALIDO,
                    ValorGrupoEdad(VALOR_GRUPO_EDAD_VALIDO, 20, 100)
                                                   )
        }

        @TestConMultiplesDAO
        fun si_se_intenta_actualizar_nombre_valor_grupo_edad_usado_lanza_excepcion_ErrorDeLlaveForanea()
        {
            assertThrows<ErrorDeLlaveForanea> {
                repositorioValoresGruposEdad.actualizar(
                        idClientePruebas,
                        VALOR_GRUPO_EDAD_VALIDO,
                        ValorGrupoEdad("${VALOR_GRUPO_EDAD_VALIDO}_editado", 0, 10)
                                                       )
            }
        }
    }

    @Nested
    inner class Eliminar
    {
        private lateinit var entidadPrueba: GrupoClientes

        @BeforeEach
        fun crearEntidadDePrueba()
        {
            crearValorGrupoEdadValido(idClientePruebas, VALOR_GRUPO_EDAD_VALIDO, 0, 10)
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
        fun se_eliminan_segmentos()
        {
            val resultadoEliminacion = repositorio.eliminarPorId(idClientePruebas, entidadPrueba.id!!)
            assertTrue(resultadoEliminacion)
            assertFalse(existeAlgunGrupoClientesDAO(idClientePruebas, configuracionRepositorios))
            assertFalse(existeAlgunSegmentoClientesDAO(idClientePruebas, configuracionRepositorios))
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
                assertFalse(resultadoEliminacion)
            }
            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadPrueba.id!!)
            assertEquals(entidadPrueba, entidadConsultada)
        }

        @TestConMultiplesDAO
        fun con_id_cliente_inexistente_lanza_excepcion_EsquemaNoExiste()
        {
            assertThrows<EsquemaNoExiste> { repositorio.eliminarPorId(idClientePruebas + 100, entidadPrueba.id!!) }
        }

        @TestConMultiplesDAO
        fun si_se_intenta_eliminar_valor_grupo_edad_usado_lanza_excepcion_ErrorDeLlaveForanea()
        {
            assertThrows<ErrorDeLlaveForanea> { repositorioValoresGruposEdad.eliminarPorId(idClientePruebas, VALOR_GRUPO_EDAD_VALIDO) }
        }
    }
}