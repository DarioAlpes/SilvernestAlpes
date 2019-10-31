package co.smartobjects.persistencia.fondos

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.fondos.CategoriaSku
import co.smartobjects.entidades.fondos.Fondo
import co.smartobjects.entidades.fondos.precios.Impuesto
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.persistencia.EntidadDAOBasePruebas
import co.smartobjects.persistencia.TestConMultiplesDAO
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorio
import co.smartobjects.persistencia.excepciones.*
import co.smartobjects.persistencia.existeAlgunFondoDAO
import co.smartobjects.persistencia.existeAlgunaCategoriaSkuDAO
import co.smartobjects.persistencia.fondos.categoriaskus.RepositorioCategoriasSkus
import co.smartobjects.persistencia.fondos.categoriaskus.RepositorioCategoriasSkusSQL
import co.smartobjects.persistencia.fondos.precios.impuestos.RepositorioImpuestos
import co.smartobjects.persistencia.fondos.precios.impuestos.RepositorioImpuestosSQL
import co.smartobjects.utilidades.Decimal
import org.junit.jupiter.api.*
import java.math.BigDecimal
import java.util.*
import kotlin.test.*

@DisplayName("CategoriaSkuDAO")
internal class CategoriaSkuDAOPruebas : EntidadDAOBasePruebas()
{
    private val repositorioImpuestos: RepositorioImpuestos by lazy { RepositorioImpuestosSQL(configuracionRepositorios) }
    private val repositorio: RepositorioCategoriasSkus by lazy { RepositorioCategoriasSkusSQL(configuracionRepositorios) }
    override val creadoresRepositoriosUsados: List<CreadorRepositorio<*>> by lazy {
        listOf<CreadorRepositorio<*>>(repositorioImpuestos, repositorio)
    }

    private lateinit var impuestoPrueba: Impuesto

    @BeforeEach
    fun antesDeCadaTest()
    {
        impuestoPrueba = repositorioImpuestos.crear(idClientePruebas, impuestoPruebasPorDefecto)
    }

    private fun darInstanciaEntidadValida(): CategoriaSku
    {
        return CategoriaSku(
                idClientePruebas,
                null,
                "Categoria prueba",
                true,
                false,
                false,
                Precio(Decimal.UNO, impuestoPrueba.id!!),
                "el código externo de prueba",
                null,
                LinkedHashSet(),
                null
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
            val entidadEsperada =
                    entidadAInsertar
                        .copiar(id = entidadCreada.id, idCliente = idClientePruebas)
            assertEquals(entidadEsperada, entidadCreada)
        }

        @TestConMultiplesDAO
        fun cuando_se_envia_una_lista_no_vacia_de_ancestros_y_sin_id_padre_retorna_una_lista_vacia_de_ancestros()
        {
            val entidadAInsertar = darInstanciaEntidadValida().copiar(idsDeAncestros = LinkedHashSet(listOf<Long>(1, 2, 3)))

            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertEquals(idClientePruebas, entidadCreada.idCliente)
            val entidadEsperada = entidadAInsertar.copiar(id = entidadCreada.id, idsDeAncestros = LinkedHashSet())
            assertEquals(entidadEsperada, entidadCreada)
        }

        @TestConMultiplesDAO
        fun se_ignora_la_lista_de_ancestros_y_retorna_lista_de_ancestros_correcta()
        {
            val entidadAInsertarAbuela = darInstanciaEntidadValida().copiar(nombre = "Entidad abuela")
            val entidadCreadaAbuela = repositorio.crear(idClientePruebas, entidadAInsertarAbuela)
            val entidadEsperadaAbuela = entidadAInsertarAbuela.copiar(id = entidadCreadaAbuela.id, idsDeAncestros = LinkedHashSet())
            assertEquals(entidadEsperadaAbuela, entidadCreadaAbuela)

            val entidadAInsertarPadre = darInstanciaEntidadValida().copiar(idDelPadre = entidadCreadaAbuela.id, nombre = "Entidad padre")
            val entidadCreadaPadre = repositorio.crear(idClientePruebas, entidadAInsertarPadre)
            val entidadEsperadaPadre = entidadAInsertarPadre.copiar(id = entidadCreadaPadre.id, idsDeAncestros = LinkedHashSet(listOf(entidadCreadaAbuela.id!!)))
            assertEquals(entidadEsperadaPadre, entidadCreadaPadre)

            val entidadAInsertar = darInstanciaEntidadValida().copiar(idDelPadre = entidadCreadaPadre.id)
            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)
            val entidadEsperada = entidadAInsertar.copiar(id = entidadCreada.id, idsDeAncestros = LinkedHashSet(listOf(entidadCreadaAbuela.id!!, entidadCreadaPadre.id!!)))
            assertEquals(entidadEsperada, entidadCreada)

        }

        @TestConMultiplesDAO
        fun lanza_excepcion_con_ErrorCreacionActualizacionPorDuplicidad_si_se_usa_nombre_duplicado()
        {
            val entidadAInsertar = darInstanciaEntidadValida()

            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertThrows<ErrorCreacionActualizacionPorDuplicidad> { repositorio.crear(idClientePruebas, entidadCreada) }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_ErrorDeLlaveForanea_si_se_referencia_una_categoria_padre_que_no_existe()
        {
            val entidadAInsertar = darInstanciaEntidadValida().copiar(idDelPadre = 9876543)

            assertThrows<ErrorDeLlaveForanea> {
                repositorio.crear(idClientePruebas, entidadAInsertar)
            }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_ErrorDeLlaveForanea_si_se_referencia_un_impuesto_que_no_existe()
        {
            val entidadACrear = darInstanciaEntidadValida().let {
                it.copiar(precioPorDefecto = it.precioPorDefecto.copiar(idImpuesto = it.precioPorDefecto.idImpuesto + 1))
            }

            assertThrows<ErrorDeLlaveForanea> {
                repositorio.crear(idClientePruebas, entidadACrear)
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
        fun con_ancestros_retorna_entidad_correcta()
        {
            val entidadAInsertarAbuela = darInstanciaEntidadValida().copiar(nombre = "Entidad abuela")
            val entidadCreadaAbuela = repositorio.crear(idClientePruebas, entidadAInsertarAbuela)
            val entidadConsultadaAbuela = repositorio.buscarPorId(idClientePruebas, entidadCreadaAbuela.id!!)
            assertEquals(entidadCreadaAbuela, entidadConsultadaAbuela)

            val entidadAInsertarPadre = darInstanciaEntidadValida().copiar(idDelPadre = entidadCreadaAbuela.id, nombre = "Entidad padre")
            val entidadCreadaPadre = repositorio.crear(idClientePruebas, entidadAInsertarPadre)
            val entidadConsultadPadre = repositorio.buscarPorId(idClientePruebas, entidadCreadaPadre.id!!)
            assertEquals(entidadCreadaPadre, entidadConsultadPadre)

            val entidadAInsertar = darInstanciaEntidadValida().copiar(idDelPadre = entidadCreadaPadre.id)
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
            val entidadesPrueba = (1..3).map {
                repositorio.crear(
                        idClientePruebas,
                        darInstanciaEntidadValida()
                            .copiar(nombre = "Entidad $it")
                                 )
            }

            val listadoEsperado = entidadesPrueba.sortedBy { it.id!! }

            val listadoConsultado = repositorio.listar(idClientePruebas).toList()

            assertEquals(listadoEsperado, listadoConsultado.sortedBy { it.id!! })
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
                Assertions.assertTrue(listadoConsultado.none())
            }
        }
    }

    @Nested
    inner class Actualizar
    {
        private lateinit var entidadDePrueba: CategoriaSku

        @BeforeEach
        fun crearCategoriaPrueba()
        {
            entidadDePrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadValida())
        }

        @TestConMultiplesDAO
        fun se_actualizan_todos_los_campos_correctamente()
        {
            val entidadPadre =
                    repositorio.crear(
                            idClientePruebas,
                            darInstanciaEntidadValida().copiar(nombre = "Entidad padre")
                                     )

            val impuestoCreado =
                    repositorioImpuestos.crear(
                            idClientePruebas,
                            impuestoPruebasPorDefecto.copiar(
                                    nombre = impuestoPruebasPorDefecto.nombre + "nuevo",
                                    tasa = Decimal(impuestoPruebasPorDefecto.tasa.valor.add(BigDecimal.ONE))
                                                            )
                                              )

            val entidadModificada = CategoriaSku(
                    entidadDePrueba.idCliente,
                    entidadDePrueba.id,
                    "Categoria prueba modificada",
                    false,
                    true,
                    true,
                    Precio(Decimal.DIEZ, impuestoCreado.id!!),
                    "el código externo de prueba nuevo",
                    entidadPadre.id,
                    LinkedHashSet(listOf(entidadPadre.id!!)),
                    null
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
        fun cuando_se_envia_una_lista_no_vacia_de_ancestros_y_sin_id_padre_retorna_una_lista_vacia_de_ancestros()
        {
            val entidadConCambios = entidadDePrueba.copiar(idsDeAncestros = LinkedHashSet(listOf<Long>(1, 2, 3)))
            val entidadActualizada = repositorio.actualizar(idClientePruebas, entidadConCambios.id!!, entidadConCambios)

            assertEquals(entidadConCambios.copiar(idsDeAncestros = LinkedHashSet()), entidadActualizada)
        }

        @TestConMultiplesDAO
        fun se_ignora_la_lista_de_ancestros_y_retorna_lista_de_ancestros_correcta()
        {
            val entidadAbuela = repositorio.crear(idClientePruebas, darInstanciaEntidadValida().copiar(nombre = "Entidad abuela"))
            val entidadPadre = repositorio.crear(idClientePruebas, darInstanciaEntidadValida().copiar(nombre = "Entidad padre", idDelPadre = entidadAbuela.id))

            val entidadConCambios = entidadDePrueba.copiar(idDelPadre = entidadPadre.id, idsDeAncestros = LinkedHashSet(listOf<Long>(1, 2, 3)))

            val entidadActualizada = repositorio.actualizar(idClientePruebas, entidadConCambios.id!!, entidadConCambios)
            assertEquals(entidadConCambios.copiar(idsDeAncestros = linkedSetOf(entidadAbuela.id!!, entidadPadre.id!!)), entidadActualizada)
        }

        @TestConMultiplesDAO
        fun con_padre_e_hijos_actualiza_los_ancestros_de_las_entidades_hijas()
        {
            val entidadAbuela = repositorio.crear(idClientePruebas, darInstanciaEntidadValida().copiar(nombre = "Entidad abuela"))
            val entidadPadre = repositorio.crear(idClientePruebas, darInstanciaEntidadValida().copiar(nombre = "Entidad padre", idDelPadre = entidadAbuela.id))
            val entidadHija = repositorio.crear(idClientePruebas, darInstanciaEntidadValida().copiar(nombre = "Entidad hija", idDelPadre = entidadDePrueba.id))
            val entidadNieta = repositorio.crear(idClientePruebas, darInstanciaEntidadValida().copiar(nombre = "Entidad nieta", idDelPadre = entidadHija.id))

            val entidadConCambios = entidadDePrueba.copiar(idDelPadre = entidadPadre.id)

            val entidadActualizada = repositorio.actualizar(idClientePruebas, entidadConCambios.id!!, entidadConCambios)
            assertEquals(entidadConCambios.copiar(idsDeAncestros = linkedSetOf(entidadAbuela.id!!, entidadPadre.id!!)), entidadActualizada)
            assertEquals(entidadHija.copiar(idsDeAncestros = linkedSetOf(entidadAbuela.id!!, entidadPadre.id!!, entidadDePrueba.id!!)), repositorio.buscarPorId(idClientePruebas, entidadHija.id!!))
            assertEquals(entidadNieta.copiar(idsDeAncestros = linkedSetOf(entidadAbuela.id!!, entidadPadre.id!!, entidadDePrueba.id!!, entidadHija.id!!)), repositorio.buscarPorId(idClientePruebas, entidadNieta.id!!))
        }

        @TestConMultiplesDAO
        fun con_id_inexistente_lanza_excepcion_EntidadNoExiste()
        {
            val entidadConCambios = entidadDePrueba.copiar(id = 789456)

            assertThrows<EntidadNoExiste> { repositorio.actualizar(idClientePruebas, entidadConCambios.id!!, entidadConCambios) }
        }

        @TestConMultiplesDAO
        fun con_padre_formando_ciclo_lanza_excepcion_ErrorDeJerarquiaPorCiclo()
        {
            val entidadAbuela = repositorio.crear(idClientePruebas, darInstanciaEntidadValida().copiar(nombre = "Entidad abuela", idDelPadre = entidadDePrueba.id))
            val entidadPadre = repositorio.crear(idClientePruebas, darInstanciaEntidadValida().copiar(nombre = "Entidad padre", idDelPadre = entidadAbuela.id))
            val entidadConCambios = entidadDePrueba.copiar(idDelPadre = entidadPadre.id)

            assertThrows<ErrorDeJerarquiaPorCiclo> { repositorio.actualizar(idClientePruebas, entidadConCambios.id!!, entidadConCambios) }
        }

        @TestConMultiplesDAO
        fun con_nombre_duplicado_lanza_excepcion_ErrorCreacionActualizacionPorDuplicidad()
        {
            val entidadConCambios = repositorio.crear(
                    idClientePruebas,
                    darInstanciaEntidadValida().copiar(nombre = "Entidad con cambio")
                                                     )

            assertThrows<ErrorCreacionActualizacionPorDuplicidad> {
                repositorio.actualizar(idClientePruebas, entidadConCambios.id!!, entidadConCambios.copiar(nombre = entidadDePrueba.nombre))
            }
        }

        @TestConMultiplesDAO
        fun con_padre_inexistente_lanza_excepcion_ErrorDeLlaveForanea()
        {
            val entidadConCambios = entidadDePrueba.copiar(idDelPadre = 789456)

            assertThrows<ErrorDeLlaveForanea> { repositorio.actualizar(idClientePruebas, entidadConCambios.id!!, entidadConCambios) }
        }

        @TestConMultiplesDAO
        fun con_impuesto_inexistente_lanza_excepcion_ErrorDeLlaveForanea()
        {
            val entidadConCambios = entidadDePrueba.copiar(
                    precioPorDefecto = entidadDePrueba.precioPorDefecto.copiar(idImpuesto = entidadDePrueba.precioPorDefecto.idImpuesto + 1)
                                                          )

            assertThrows<ErrorDeLlaveForanea> {
                repositorio.actualizar(idClientePruebas, entidadConCambios.id!!, entidadConCambios)
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
    inner class ActualizarParcialmente
    {
        private lateinit var entidadDePrueba: CategoriaSku

        @BeforeEach
        fun crearCategoriaPrueba()
        {
            entidadDePrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadValida())
        }

        @TestConMultiplesDAO
        fun solo_se_actualizan_los_campos_necesarios()
        {
            val nuevoValor = Fondo.CampoDisponibleParaLaVenta<CategoriaSku>(!entidadDePrueba.disponibleParaLaVenta)
            repositorio.actualizarCamposIndividuales(
                    idClientePruebas,
                    entidadDePrueba.id!!,
                    mapOf<String, CampoModificable<CategoriaSku, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                    )

            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.id!!)
            val entidadEsperada =
                    CategoriaSku(
                            entidadDePrueba.idCliente,
                            entidadDePrueba.id,
                            entidadDePrueba.nombre,
                            !entidadDePrueba.disponibleParaLaVenta,
                            entidadDePrueba.debeAparecerSoloUnaVez,
                            entidadDePrueba.esIlimitado,
                            entidadDePrueba.precioPorDefecto,
                            entidadDePrueba.codigoExterno,
                            entidadDePrueba.idDelPadre,
                            entidadDePrueba.idsDeAncestros,
                            entidadDePrueba.llaveDeIcono
                                )

            assertEquals(entidadEsperada, entidadConsultada)
        }

        @TestConMultiplesDAO
        fun con_id_inexistente_lanza_excepcion_EntidadNoExiste()
        {
            val nuevoValor = Fondo.CampoDisponibleParaLaVenta<CategoriaSku>(!entidadDePrueba.disponibleParaLaVenta)
            assertThrows<EntidadNoExiste> {
                repositorio.actualizarCamposIndividuales(
                        idClientePruebas,
                        entidadDePrueba.id!! + 789456,
                        mapOf<String, CampoModificable<CategoriaSku, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                        )
            }
        }

        @TestConMultiplesDAO
        fun con_id_existente_en_otro_cliente_lanza_excepcion_EntidadNoExiste()
        {
            val nuevoValor = Fondo.CampoDisponibleParaLaVenta<CategoriaSku>(!entidadDePrueba.disponibleParaLaVenta)
            ejecutarConClienteAlternativo {
                assertThrows<EntidadNoExiste> {
                    repositorio.actualizarCamposIndividuales(
                            it.id!!,
                            entidadDePrueba.id!! + 789456,
                            mapOf<String, CampoModificable<CategoriaSku, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                            )
                }
            }
        }

        @TestConMultiplesDAO
        fun con_id_cliente_inexistente_lanza_excepcion_EsquemaNoExiste()
        {
            val nuevoValor = Fondo.CampoDisponibleParaLaVenta<CategoriaSku>(!entidadDePrueba.disponibleParaLaVenta)
            assertThrows<EsquemaNoExiste> {
                repositorio.actualizarCamposIndividuales(
                        idClientePruebas + 100,
                        entidadDePrueba.id!! + 789456,
                        mapOf<String, CampoModificable<CategoriaSku, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                        )
            }
        }
    }

    @Nested
    inner class Eliminar
    {
        private lateinit var entidadPrueba: CategoriaSku

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
        fun se_elimina_el_fondo_asociado()
        {
            val resultadoEliminacion = repositorio.eliminarPorId(idClientePruebas, entidadPrueba.id!!)
            assertTrue(resultadoEliminacion)
            assertFalse(existeAlgunFondoDAO(idClientePruebas, configuracionRepositorios))
            assertFalse(existeAlgunaCategoriaSkuDAO(idClientePruebas, configuracionRepositorios))
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

        @TestConMultiplesDAO
        fun si_se_intenta_borrar_el_padre_lanza_excepcion_ErrorDeLlaveForanea_y_no_elimina_entidad()
        {
            repositorio.crear(idClientePruebas, darInstanciaEntidadValida().copiar(nombre = "Entidad hija", idDelPadre = entidadPrueba.id))

            assertThrows<ErrorDeLlaveForanea> { repositorio.eliminarPorId(idClientePruebas, entidadPrueba.id!!) }

            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadPrueba.id!!)
            assertEquals(entidadPrueba, entidadConsultada)
        }
    }
}