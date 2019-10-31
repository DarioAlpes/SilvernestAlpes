package co.smartobjects.persistencia.personas

import co.smartobjects.entidades.personas.ValorGrupoEdad
import co.smartobjects.persistencia.EntidadDAOBasePruebas
import co.smartobjects.persistencia.TestConMultiplesDAO
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorio
import co.smartobjects.persistencia.excepciones.*
import co.smartobjects.persistencia.fondos.precios.gruposclientes.RepositorioGrupoClientes
import co.smartobjects.persistencia.fondos.precios.gruposclientes.RepositorioGrupoClientesSQL
import co.smartobjects.persistencia.personas.valorgrupoedad.RepositorioValoresGruposEdad
import co.smartobjects.persistencia.personas.valorgrupoedad.RepositorioValoresGruposEdadSQL
import co.smartobjects.persistencia.personas.valorgrupoedad.ValorGrupoEdadDAO
import org.junit.jupiter.api.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


@DisplayName("ValorGrupoEdadDAO")
internal class ValorGrupoEdadDAOPruebas
{
    @Nested
    inner class Conversion
    {
        @Test
        fun de_dao_a_negocio_correctamente()
        {
            val entidadDAO = ValorGrupoEdadDAO("el valor", 1, 5)

            val entidadNegocio = entidadDAO.aEntidadDeNegocio(0)

            assertEquals(entidadDAO.valor, entidadNegocio.valor)
            assertEquals(entidadDAO.edadMinima, entidadNegocio.edadMinima)
            assertEquals(entidadDAO.edadMaxima, entidadNegocio.edadMaxima)
        }

        @Test
        fun de_negocio_a_dao_correctamente()
        {
            val entidadNegocio = ValorGrupoEdad("el valor", 1, 5)

            val entidadDAO = ValorGrupoEdadDAO(entidadNegocio)

            assertEquals(entidadNegocio.valor, entidadDAO.valor)
            assertEquals(entidadNegocio.edadMinima, entidadDAO.edadMinima)
            assertEquals(entidadNegocio.edadMaxima, entidadDAO.edadMaxima)
        }
    }

    @Nested
    inner class EnBD : EntidadDAOBasePruebas()
    {
        private val repositorioGrupoClientes: RepositorioGrupoClientes by lazy { RepositorioGrupoClientesSQL(configuracionRepositorios) }
        private val repositorio: RepositorioValoresGruposEdad by lazy { RepositorioValoresGruposEdadSQL(configuracionRepositorios) }
        override val creadoresRepositoriosUsados: List<CreadorRepositorio<*>> by lazy {
            listOf<CreadorRepositorio<*>>(repositorio, repositorioGrupoClientes)
        }

        private fun darInstanciaEntidadValida(): ValorGrupoEdad
        {
            return ValorGrupoEdad("Grupo", 0, 20)
        }

        @Nested
        inner class Crear
        {
            @TestConMultiplesDAO
            fun retorna_misma_entidad_con_id_asignado_por_bd()
            {
                val entidadAInsertar = darInstanciaEntidadValida()
                val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

                assertNotNull(entidadCreada.valor)
                assertEquals(entidadAInsertar.copiar(valor = entidadCreada.valor), entidadCreada)
            }

            @TestConMultiplesDAO
            fun con_rango_completo_retorna_misma_entidad_con_id_asignado_por_bd()
            {
                val entidadAInsertar = ValorGrupoEdad("Nuevo", null, null)
                val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

                assertNotNull(entidadCreada.valor)
                assertEquals(entidadAInsertar.copiar(valor = entidadCreada.valor), entidadCreada)
            }

            @TestConMultiplesDAO
            fun con_rango_abierto_inferior_completo_retorna_misma_entidad_con_id_asignado_por_bd()
            {
                val entidadAInsertar = ValorGrupoEdad("Nuevo", null, 5)
                val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

                assertNotNull(entidadCreada.valor)
                assertEquals(entidadAInsertar.copiar(valor = entidadCreada.valor), entidadCreada)
            }

            @TestConMultiplesDAO
            fun con_rango_abierto_superior_completo_retorna_misma_entidad_con_id_asignado_por_bd()
            {
                val entidadAInsertar = ValorGrupoEdad("Nuevo", 5, null)
                val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

                assertNotNull(entidadCreada.valor)
                assertEquals(entidadAInsertar.copiar(valor = entidadCreada.valor), entidadCreada)
            }

            @TestConMultiplesDAO
            fun permite_crear_valor_con_rango_abierto_inferior_si_existe_ya_un_grupo_que_no_intersecte()
            {
                val entidadAInsertar = ValorGrupoEdad("Inicial", 90, 91)

                repositorio.crear(idClientePruebas, entidadAInsertar)

                repositorio.crear(idClientePruebas, ValorGrupoEdad("Nuevo", null, 89))
            }

            @TestConMultiplesDAO
            fun permite_crear_valor_con_rango_abierto_superior_si_existe_ya_un_grupo_que_no_intersecte()
            {
                val entidadAInsertar = ValorGrupoEdad("Inicial", 1, 2)

                repositorio.crear(idClientePruebas, entidadAInsertar)

                repositorio.crear(idClientePruebas, ValorGrupoEdad("Nuevo", 3, null))
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_con_ErrorCreacionActualizacionPorDuplicidad_y_no_se_modifica_entidad_existente_si_se_repite_valor()
            {
                val entidadAInsertar = darInstanciaEntidadValida()

                repositorio.crear(idClientePruebas, entidadAInsertar)

                val excepcion = Assertions.assertThrows(ErrorCreacionActualizacionPorDuplicidad::class.java, {
                    repositorio.crear(
                            idClientePruebas,
                            entidadAInsertar.copiar(
                                    edadMinima = entidadAInsertar.edadMaxima!! + 1,
                                    edadMaxima = entidadAInsertar.edadMaxima!! * 2)
                                     )
                })

                assertEquals(ValorGrupoEdad.NOMBRE_ENTIDAD, excepcion.entidad)
                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadAInsertar.valor)
                assertEquals(entidadAInsertar, entidadConsultada)
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_con_ErrorCreacionViolacionDeRestriccion_si_existe_ya_un_grupo_con_rango_sin_limites()
            {
                val entidadAInsertar = ValorGrupoEdad("Nuevo", null, null)

                repositorio.crear(idClientePruebas, entidadAInsertar)

                val excepcion = Assertions.assertThrows(ErrorCreacionViolacionDeRestriccion::class.java, {
                    repositorio.crear(idClientePruebas, darInstanciaEntidadValida())
                })

                assertEquals(ValorGrupoEdad.NOMBRE_ENTIDAD, excepcion.entidad)
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_con_ErrorCreacionViolacionDeRestriccion_con_rango_sin_limites_si_existe_ya_un_grupo()
            {
                val entidadAInsertar = ValorGrupoEdad("Inicial", 1, 2)

                repositorio.crear(idClientePruebas, entidadAInsertar)

                val excepcion = Assertions.assertThrows(ErrorCreacionViolacionDeRestriccion::class.java, {
                    repositorio.crear(idClientePruebas, ValorGrupoEdad("Nuevo", null, null))
                })

                assertEquals(ValorGrupoEdad.NOMBRE_ENTIDAD, excepcion.entidad)
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_con_ErrorCreacionViolacionDeRestriccion_con_rango_abierto_inferior_si_existe_ya_un_grupo_que_intersecte()
            {
                val entidadAInsertar = ValorGrupoEdad("Inicial", 1, 2)

                repositorio.crear(idClientePruebas, entidadAInsertar)

                val excepcion = Assertions.assertThrows(ErrorCreacionViolacionDeRestriccion::class.java, {
                    repositorio.crear(idClientePruebas, ValorGrupoEdad("Nuevo", null, 3))
                })

                assertEquals(ValorGrupoEdad.NOMBRE_ENTIDAD, excepcion.entidad)
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_con_ErrorCreacionViolacionDeRestriccion_con_rango_abierto_superior_si_existe_ya_un_grupo_que_intersecte()
            {
                val entidadAInsertar = ValorGrupoEdad("Inicial", 90, 91)

                repositorio.crear(idClientePruebas, entidadAInsertar)

                val excepcion = Assertions.assertThrows(ErrorCreacionViolacionDeRestriccion::class.java, {
                    repositorio.crear(idClientePruebas, ValorGrupoEdad("Nuevo", 89, null))
                })

                assertEquals(ValorGrupoEdad.NOMBRE_ENTIDAD, excepcion.entidad)
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_con_ErrorCreacionViolacionDeRestriccion_si_intersecta_rango_abierto_inferior_con_edad_minima()
            {
                val entidadInicial = ValorGrupoEdad("Inicial", null, 10)

                repositorio.crear(idClientePruebas, entidadInicial)

                val entidadNueva = ValorGrupoEdad("Nuevo", entidadInicial.edadMaxima!! - 3, entidadInicial.edadMaxima!! + 1)

                val excepcion = Assertions.assertThrows(ErrorCreacionViolacionDeRestriccion::class.java, {
                    repositorio.crear(idClientePruebas, entidadNueva)
                })

                assertEquals(ValorGrupoEdad.NOMBRE_ENTIDAD, excepcion.entidad)
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_con_ErrorCreacionViolacionDeRestriccion_si_intersecta_rango_abierto_inferior_con_edad_maxima()
            {
                val entidadInicial = ValorGrupoEdad("Inicial", null, 10)

                repositorio.crear(idClientePruebas, entidadInicial)

                val entidadNueva = ValorGrupoEdad("Nuevo", entidadInicial.edadMaxima!! - 3, entidadInicial.edadMaxima!! - 1)

                val excepcion = Assertions.assertThrows(ErrorCreacionViolacionDeRestriccion::class.java, {
                    repositorio.crear(idClientePruebas, entidadNueva)
                })

                assertEquals(ValorGrupoEdad.NOMBRE_ENTIDAD, excepcion.entidad)
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_con_ErrorCreacionViolacionDeRestriccion_si_intersecta_rango_abierto_superior_con_edad_maxima()
            {
                val entidadInicial = ValorGrupoEdad("Inicial", 10, null)

                repositorio.crear(idClientePruebas, entidadInicial)

                val entidadNueva = ValorGrupoEdad("Nuevo", entidadInicial.edadMinima!! - 5, entidadInicial.edadMinima!! + 1)

                val excepcion = Assertions.assertThrows(ErrorCreacionViolacionDeRestriccion::class.java, {
                    repositorio.crear(idClientePruebas, entidadNueva)
                })

                assertEquals(ValorGrupoEdad.NOMBRE_ENTIDAD, excepcion.entidad)
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_con_ErrorCreacionViolacionDeRestriccion_si_intersecta_rango_abierto_superior_con_edad_minima()
            {
                val entidadInicial = ValorGrupoEdad("Inicial", 10, null)

                repositorio.crear(idClientePruebas, entidadInicial)

                val entidadNueva = ValorGrupoEdad("Nuevo", entidadInicial.edadMinima!! + 1, entidadInicial.edadMinima!! + 3)

                val excepcion = Assertions.assertThrows(ErrorCreacionViolacionDeRestriccion::class.java, {
                    repositorio.crear(idClientePruebas, entidadNueva)
                })

                assertEquals(ValorGrupoEdad.NOMBRE_ENTIDAD, excepcion.entidad)
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_con_ErrorCreacionActualizacionPorDuplicidad_si_se_intersectan_rangos()
            {
                val entidadAInsertar = darInstanciaEntidadValida()

                repositorio.crear(idClientePruebas, entidadAInsertar)

                val excepcion = Assertions.assertThrows(ErrorCreacionViolacionDeRestriccion::class.java, {
                    repositorio.crear(idClientePruebas, entidadAInsertar.copiar(valor = entidadAInsertar.valor))
                })

                assertEquals(ValorGrupoEdad.NOMBRE_ENTIDAD, excepcion.entidad)
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_EsquemaNoExiste_si_cliente_con_id_dado_no_existe()
            {
                val entidadAInsertar = darInstanciaEntidadValida()

                Assertions.assertThrows(EsquemaNoExiste::class.java, { repositorio.crear(idClientePruebas + 100, entidadAInsertar) })
            }
        }

        @Nested
        inner class Consultar
        {
            @TestConMultiplesDAO
            fun por_id_existente_retorna_entidad_correcta()
            {
                val entidadPrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadValida())

                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadPrueba.valor)

                assertEquals(entidadPrueba, entidadConsultada)
            }

            @TestConMultiplesDAO
            fun listado_de_entidades_sin_entidades_retorna_lista_vacia()
            {
                val listadoConsultado = repositorio.listar(idClientePruebas)

                Assertions.assertTrue(listadoConsultado.none())
            }

            @TestConMultiplesDAO
            fun listado_de_entidades_retorna_entidades_correctas()
            {
                val intervalo = 2
                val entidadesPrueba =
                        generateSequence(0) { it + intervalo }
                            .take(3)
                            .map {
                                repositorio.crear(
                                        idClientePruebas,
                                        darInstanciaEntidadValida().copiar(valor = "$it", edadMinima = it, edadMaxima = it + intervalo - 1)
                                                 )
                            }

                val listadoEsperado = entidadesPrueba.sortedBy { it.valor }.toList()


                val listadoConsultado = repositorio.listar(idClientePruebas)

                assertEquals(listadoEsperado, listadoConsultado.sortedBy { it.valor }.toList())
            }

            @TestConMultiplesDAO
            fun por_id_no_existente_retorna_null()
            {
                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, "asdfasdfasgergheh45h45h4h45h")

                Assertions.assertNull(entidadConsultada)
            }

            @TestConMultiplesDAO
            fun por_id_existente_en_otro_cliente_retorna_null()
            {
                val entidadPrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadValida())

                ejecutarConClienteAlternativo {
                    val entidadConsultada = repositorio.buscarPorId(it.id!!, entidadPrueba.valor)
                    kotlin.test.assertNull(entidadConsultada)
                }
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_EsquemaNoExiste_si_se_consulta_por_id_con_id_cliente_inexistente()
            {
                Assertions.assertThrows(EsquemaNoExiste::class.java, { repositorio.buscarPorId(idClientePruebas + 100, "asdasd") })
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_EsquemaNoExiste_si_se_consulta_listado_de_entidades_con_id_cliente_inexistente()
            {
                Assertions.assertThrows(EsquemaNoExiste::class.java, { repositorio.listar(idClientePruebas + 100) })
            }

            @TestConMultiplesDAO
            fun listado_de_entidades_con_entidades_en_otro_cliente_retorna_lista_vacia()
            {
                repositorio.crear(idClientePruebas, darInstanciaEntidadValida())

                ejecutarConClienteAlternativo {
                    val listadoConsultado = repositorio.listar(it.id!!)
                    Assertions.assertTrue(listadoConsultado.none())
                }
            }
        }

        @Nested
        inner class Actualizar
        {
            @TestConMultiplesDAO
            fun se_actualizan_todos_los_campos_correctamente()
            {
                val entidadDePrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadValida())
                val entidadModificada = ValorGrupoEdad("Nuevo nombre", 5, 6)
                val entidadActualizada = repositorio.actualizar(idClientePruebas, entidadDePrueba.valor, entidadModificada)
                assertEquals(entidadModificada, entidadActualizada)
            }

            @TestConMultiplesDAO
            fun con_id_inexistente_lanza_excepcion_EntidadNoExiste()
            {
                val entidadDePrueba = darInstanciaEntidadValida()

                Assertions.assertThrows(EntidadNoExiste::class.java, { repositorio.actualizar(idClientePruebas, "asdfasdf34g34g3gsgr", entidadDePrueba) })
            }

            @TestConMultiplesDAO
            fun con_valor_duplicado_lanza_excepcion_ErrorCreacionActualizacionPorDuplicidad_y_no_altera_entidad_actual()
            {
                val entidadDePrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadValida())

                val entidadConCambios = repositorio.crear(
                        idClientePruebas,
                        entidadDePrueba.copiar(
                                valor = "id dummy",
                                edadMinima = entidadDePrueba.edadMaxima!! + 1,
                                edadMaxima = entidadDePrueba.edadMaxima!! * 2
                                              )
                                                         )

                Assertions.assertThrows(ErrorCreacionActualizacionPorDuplicidad::class.java, {
                    val entidadModificada = entidadConCambios.copiar(valor = entidadDePrueba.valor)
                    repositorio.actualizar(idClientePruebas, entidadConCambios.valor, entidadModificada)
                })

                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.valor)
                assertEquals(entidadDePrueba, entidadConsultada)
            }

            @TestConMultiplesDAO
            fun no_lanza_excepcion_con_ErrorCreacionViolacionDeRestriccion_si_intersecta_con_rango_previo()
            {
                val entidadDePrueba = repositorio.crear(idClientePruebas, ValorGrupoEdad("Inicial", null, 10))

                val entidadConCambios = repositorio.crear(
                        idClientePruebas,
                        ValorGrupoEdad("Nuevo", entidadDePrueba.edadMaxima!! + 1, entidadDePrueba.edadMaxima!! + 2)
                                                         )

                val entidadModificada = entidadConCambios.copiar(edadMinima = entidadDePrueba.edadMaxima!! + 1, edadMaxima = null)

                repositorio.actualizar(idClientePruebas, entidadConCambios.valor, entidadModificada)

                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadConCambios.valor)
                assertEquals(entidadModificada, entidadConsultada)
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_ErrorActualizacionViolacionDeRestriccion_y_no_altera_entidad_actual_si_se_actualiza_a_grupo_a_rango_completo_y_hay_mas_grupos()
            {
                val entidadDePrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadValida())

                val entidadConCambios = repositorio.crear(
                        idClientePruebas,
                        ValorGrupoEdad("Nuevo", entidadDePrueba.edadMaxima!! + 1, entidadDePrueba.edadMaxima!! + 2)
                                                         )

                Assertions.assertThrows(ErrorActualizacionViolacionDeRestriccion::class.java, {
                    val entidadModificada = entidadConCambios.copiar(edadMinima = null, edadMaxima = null)

                    repositorio.actualizar(idClientePruebas, entidadConCambios.valor, entidadModificada)
                })

                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.valor)
                assertEquals(entidadDePrueba, entidadConsultada)
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_con_ErrorCreacionViolacionDeRestriccion_si_intersecta_rango_abierto_inferior_con_edad_minima()
            {
                val entidadDePrueba = repositorio.crear(idClientePruebas, ValorGrupoEdad("Inicial", null, 10))

                val entidadConCambios = repositorio.crear(
                        idClientePruebas,
                        ValorGrupoEdad("Nuevo", entidadDePrueba.edadMaxima!! + 1, entidadDePrueba.edadMaxima!! + 2)
                                                         )

                Assertions.assertThrows(ErrorActualizacionViolacionDeRestriccion::class.java, {
                    val entidadModificada = entidadConCambios.copiar(edadMinima = entidadDePrueba.edadMaxima!! - 1)

                    repositorio.actualizar(idClientePruebas, entidadConCambios.valor, entidadModificada)
                })

                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.valor)
                assertEquals(entidadDePrueba, entidadConsultada)
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_con_ErrorCreacionViolacionDeRestriccion_si_intersecta_rango_abierto_inferior_con_edad_maxima()
            {
                val entidadDePrueba = repositorio.crear(idClientePruebas, ValorGrupoEdad("Inicial", null, 10))

                val entidadConCambios = repositorio.crear(
                        idClientePruebas,
                        ValorGrupoEdad("Nuevo", entidadDePrueba.edadMaxima!! + 1, entidadDePrueba.edadMaxima!! + 2)
                                                         )

                Assertions.assertThrows(ErrorActualizacionViolacionDeRestriccion::class.java, {
                    val entidadModificada = entidadConCambios.copiar(edadMinima = entidadDePrueba.edadMaxima!! - 3, edadMaxima = entidadDePrueba.edadMaxima!! - 1)

                    repositorio.actualizar(idClientePruebas, entidadConCambios.valor, entidadModificada)
                })

                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.valor)
                assertEquals(entidadDePrueba, entidadConsultada)
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_con_ErrorCreacionViolacionDeRestriccion_si_intersecta_rango_abierto_superior_con_edad_maxima()
            {

                val entidadDePrueba = repositorio.crear(idClientePruebas, ValorGrupoEdad("Inicial", 10, null))

                val entidadConCambios = repositorio.crear(
                        idClientePruebas,
                        ValorGrupoEdad("Nuevo", entidadDePrueba.edadMinima!! - 2, entidadDePrueba.edadMinima!! - 1)
                                                         )

                Assertions.assertThrows(ErrorActualizacionViolacionDeRestriccion::class.java, {
                    val entidadModificada = entidadConCambios.copiar(edadMaxima = entidadDePrueba.edadMinima!! + 1)

                    repositorio.actualizar(idClientePruebas, entidadConCambios.valor, entidadModificada)
                })

                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.valor)
                assertEquals(entidadDePrueba, entidadConsultada)
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_con_ErrorCreacionViolacionDeRestriccion_si_intersecta_rango_abierto_superior_con_edad_minima()
            {
                val entidadDePrueba = repositorio.crear(idClientePruebas, ValorGrupoEdad("Inicial", null, 10))

                val entidadConCambios = repositorio.crear(
                        idClientePruebas,
                        ValorGrupoEdad("Nuevo", entidadDePrueba.edadMaxima!! + 1, entidadDePrueba.edadMaxima!! + 2)
                                                         )

                Assertions.assertThrows(ErrorActualizacionViolacionDeRestriccion::class.java, {
                    val entidadModificada = entidadConCambios.copiar(edadMinima = entidadDePrueba.edadMaxima!!, edadMaxima = entidadDePrueba.edadMaxima!! + 2)

                    repositorio.actualizar(idClientePruebas, entidadConCambios.valor, entidadModificada)
                })

                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.valor)
                assertEquals(entidadDePrueba, entidadConsultada)
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_ErrorCreacionActualizacionPorDuplicidad_y_no_altera_entidad_actual_con_rango_de_valores_que_intersecta_otro()
            {
                val entidadDePrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadValida())

                val entidadConCambios = repositorio.crear(
                        idClientePruebas,
                        entidadDePrueba.copiar(
                                valor = "nuevo rango",
                                edadMinima = entidadDePrueba.edadMaxima!! + 1,
                                edadMaxima = entidadDePrueba.edadMaxima!! * 2
                                              )
                                                         )

                Assertions.assertThrows(ErrorActualizacionViolacionDeRestriccion::class.java, {
                    val entidadModificada = entidadConCambios.copiar(
                            edadMinima = entidadDePrueba.edadMinima!! + 2,
                            edadMaxima = entidadDePrueba.edadMaxima!! + 4
                                                                    )
                    repositorio.actualizar(idClientePruebas, entidadConCambios.valor, entidadModificada)
                })

                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.valor)
                assertEquals(entidadDePrueba, entidadConsultada)
            }

            @TestConMultiplesDAO
            fun con_id_existente_en_otro_cliente_lanza_excepcion_EntidadNoExiste()
            {
                val entidadDePrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadValida())
                ejecutarConClienteAlternativo {
                    Assertions.assertThrows(EntidadNoExiste::class.java, { repositorio.actualizar(it.id!!, entidadDePrueba.valor, entidadDePrueba) })
                }
            }

            @TestConMultiplesDAO
            fun con_id_cliente_inexistente_lanza_excepcion_EsquemaNoExiste()
            {
                val entidadDePrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadValida())
                Assertions.assertThrows(EsquemaNoExiste::class.java, { repositorio.actualizar(idClientePruebas + 100, entidadDePrueba.valor, entidadDePrueba) })
            }
        }

        @Nested
        inner class Eliminar
        {
            private lateinit var entidadPrueba: ValorGrupoEdad

            @BeforeEach
            fun crearEntidadDePrueba()
            {
                entidadPrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadValida())
            }

            @TestConMultiplesDAO
            fun si_existe_la_entidad_se_elimina_correctamente()
            {
                val resultadoEliminacion = repositorio.eliminarPorId(idClientePruebas, entidadPrueba.valor)
                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadPrueba.valor)

                Assertions.assertTrue(resultadoEliminacion)
                Assertions.assertNull(entidadConsultada)
            }

            @TestConMultiplesDAO
            fun si_no_existe_la_entidad_retorna_false_y_no_elimina_entidad_existente()
            {
                val resultadoEliminacion = repositorio.eliminarPorId(idClientePruebas, entidadPrueba.valor + "asdf")
                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadPrueba.valor)

                Assertions.assertFalse(resultadoEliminacion)
                assertEquals(entidadPrueba, entidadConsultada)
            }

            @TestConMultiplesDAO
            fun con_id_existente_en_otro_cliente_retorna_false_y_no_elimina_entidad_existente()
            {
                ejecutarConClienteAlternativo {
                    val resultadoEliminacion = repositorio.eliminarPorId(it.id!!, entidadPrueba.valor)
                    kotlin.test.assertFalse(resultadoEliminacion)
                }
                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadPrueba.valor)
                assertEquals(entidadPrueba, entidadConsultada)
            }

            @TestConMultiplesDAO
            fun con_id_cliente_inexistente_lanza_excepcion_EsquemaNoExiste()
            {
                Assertions.assertThrows(EsquemaNoExiste::class.java, { repositorio.eliminarPorId(idClientePruebas + 100, entidadPrueba.valor) })
            }
        }
    }
}