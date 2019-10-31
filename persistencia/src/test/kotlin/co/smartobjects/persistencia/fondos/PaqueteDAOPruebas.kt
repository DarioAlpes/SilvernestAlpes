package co.smartobjects.persistencia.fondos

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.fondos.Dinero
import co.smartobjects.entidades.fondos.Paquete
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.persistencia.EntidadDAOBasePruebas
import co.smartobjects.persistencia.TestConMultiplesDAO
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorio
import co.smartobjects.persistencia.excepciones.EntidadNoExiste
import co.smartobjects.persistencia.excepciones.ErrorCreacionActualizacionPorDuplicidad
import co.smartobjects.persistencia.excepciones.ErrorDeLlaveForanea
import co.smartobjects.persistencia.excepciones.EsquemaNoExiste
import co.smartobjects.persistencia.existeAlgunFondoPaqueteDAO
import co.smartobjects.persistencia.fondos.monedas.RepositorioMonedas
import co.smartobjects.persistencia.fondos.monedas.RepositorioMonedasSQL
import co.smartobjects.persistencia.fondos.paquete.RepositorioPaquetes
import co.smartobjects.persistencia.fondos.paquete.RepositorioPaquetesSQL
import co.smartobjects.persistencia.fondos.precios.impuestos.RepositorioImpuestos
import co.smartobjects.persistencia.fondos.precios.impuestos.RepositorioImpuestosSQL
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.junit.jupiter.api.*
import org.threeten.bp.ZonedDateTime
import java.math.BigDecimal
import kotlin.test.*


@DisplayName("PaqueteDAO")
internal class PaqueteDAOPruebas : EntidadDAOBasePruebas()
{
    private val repositorioMonedas: RepositorioMonedas by lazy { RepositorioMonedasSQL(configuracionRepositorios) }
    private val repositorioImpuestos: RepositorioImpuestos by lazy { RepositorioImpuestosSQL(configuracionRepositorios) }
    private val repositorio: RepositorioPaquetes by lazy { RepositorioPaquetesSQL(configuracionRepositorios) }

    override val creadoresRepositoriosUsados: List<CreadorRepositorio<*>> by lazy {
        listOf<CreadorRepositorio<*>>(repositorioImpuestos, repositorioMonedas, repositorio)
    }

    private lateinit var fondosIncluidosPorDefecto: List<Dinero>

    @BeforeEach
    fun antesDeCadaTest()
    {
        val impuestoCreado = repositorioImpuestos.crear(idClientePruebas, impuestoPruebasPorDefecto)

        fondosIncluidosPorDefecto = (1..3).map {
            repositorioMonedas.crear(
                    idClientePruebas,
                    Dinero(
                            idClientePruebas,
                            null,
                            "Dinero $it",
                            true,
                            false,
                            false,
                            Precio(Decimal.DIEZ, impuestoCreado.id!!),
                            "el código externo de prueba dinero"
                          )
                                    )
        }
    }

    private fun darInstanciaEntidadValida(): Paquete
    {
        val fechaInicial = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
        return Paquete(
                idClientePruebas,
                null,
                "Paquete de prueba",
                "Descripción",
                true,
                fechaInicial,
                fechaInicial.plusDays(1),
                fondosIncluidosPorDefecto.map {
                    Paquete.FondoIncluido(it.id!!, "código externo incluido", Decimal.UNO)
                },
                "código externo paquete"
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
            val entidadEsperada = entidadAInsertar.copiar(
                    id = entidadCreada.id,
                    idCliente = idClientePruebas
                                                         )

            assertEquals(entidadEsperada, entidadCreada)
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_con_ErrorCreacionActualizacionPorDuplicidad_si_se_usa_nombre_duplicado()
        {
            val entidadAInsertar = darInstanciaEntidadValida()

            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertThrows<ErrorCreacionActualizacionPorDuplicidad> {
                repositorio.crear(idClientePruebas, entidadCreada)
            }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_ErrorCreacionActualizacionPorDuplicidad_si_repite_un_fondo_incluido()
        {
            val entidadAInsertar = darInstanciaEntidadValida().copiar(
                    fondosIncluidos = listOf(
                            Paquete.FondoIncluido(fondosIncluidosPorDefecto[0].id!!, "código externo incluido", Decimal(1)),
                            Paquete.FondoIncluido(fondosIncluidosPorDefecto[0].id!!, "código externo incluido", Decimal(3))
                                            )
                                                                     )

            assertThrows<ErrorCreacionActualizacionPorDuplicidad> {
                repositorio.crear(idClientePruebas, entidadAInsertar)
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
        private lateinit var entidadDePrueba: Paquete

        @BeforeEach
        fun crearCategoriaPrueba()
        {
            entidadDePrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadValida())
        }

        @TestConMultiplesDAO
        fun se_actualizan_todos_los_campos_correctamente()
        {
            val impuestoCreado =
                    repositorioImpuestos.crear(
                            idClientePruebas,
                            impuestoPruebasPorDefecto.copiar(
                                    nombre = impuestoPruebasPorDefecto.nombre + "nuevo",
                                    tasa = Decimal(impuestoPruebasPorDefecto.tasa.valor.add(BigDecimal.ONE))
                                                            )
                                              )

            val fondoNuevo =
                    repositorioMonedas.crear(
                            idClientePruebas,
                            Dinero(
                                    idClientePruebas,
                                    null,
                                    "Dinero ${fondosIncluidosPorDefecto.size + 1}",
                                    true,
                                    false,
                                    false,
                                    Precio(Decimal.UNO, impuestoCreado.id!!),
                                    "el código externo de prueba dinero nuevo"
                                  )
                                            )

            val entidadModificada = Paquete(
                    idClientePruebas,
                    entidadDePrueba.id,
                    entidadDePrueba.nombre + "nuevo",
                    entidadDePrueba.descripcion + "Descripción",
                    !entidadDePrueba.disponibleParaLaVenta,
                    entidadDePrueba.validoHasta,
                    entidadDePrueba.validoHasta.plusDays(1),
                    listOf(Paquete.FondoIncluido(fondoNuevo.id!!, "código externo incluido", Decimal.UNO)),
                    "código externo paquete nuevo"
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

            assertThrows<EntidadNoExiste> { repositorio.actualizar(idClientePruebas, entidadConCambios.id!!, entidadConCambios) }
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
        fun con_fondo_inexistente_lanza_excepcion_ErrorDeLlaveForanea()
        {
            val entidadConCambios =
                    entidadDePrueba.copiar(
                            fondosIncluidos = entidadDePrueba.fondosIncluidos.map { it.copy(idFondo = 23424324) }
                                          )

            assertThrows<ErrorDeLlaveForanea> {
                repositorio.actualizar(idClientePruebas, entidadConCambios.id!!, entidadConCambios)
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
    inner class ActualizarParcialmente
    {
        private lateinit var entidadDePrueba: Paquete

        @BeforeEach
        fun crearEntidadDePrueba()
        {
            entidadDePrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadValida())
        }

        @TestConMultiplesDAO
        fun solo_se_actualizan_los_campos_necesarios()
        {
            val nombreNuevo = Paquete.CampoNombre(entidadDePrueba.nombre + "Nuevo")
            val descripcionNuevo = Paquete.CampoDescripcion(entidadDePrueba.descripcion + "Nuevo")
            val disponibleParaLaVentaNuevo = Paquete.CampoDisponibleParaLaVenta(!entidadDePrueba.disponibleParaLaVenta)

            val camposAProbar =
                    mapOf<String, CampoModificable<Paquete, *>>(
                            nombreNuevo.nombreCampo to nombreNuevo,
                            descripcionNuevo.nombreCampo to descripcionNuevo,
                            disponibleParaLaVentaNuevo.nombreCampo to disponibleParaLaVentaNuevo
                                                               )

            repositorio.actualizarCamposIndividuales(idClientePruebas, entidadDePrueba.id!!, camposAProbar)

            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.id!!)
            val entidadEsperada =
                    entidadDePrueba.copiar(
                            nombre = nombreNuevo.valor,
                            descripcion = descripcionNuevo.valor,
                            disponibleParaLaVenta = disponibleParaLaVentaNuevo.valor
                                          )

            assertEquals(entidadEsperada, entidadConsultada)
        }

        @TestConMultiplesDAO
        fun con_id_inexistente_lanza_excepcion_EntidadNoExiste()
        {
            val campoNombre = Paquete.CampoNombre(entidadDePrueba.nombre)

            assertThrows<EntidadNoExiste> {
                repositorio.actualizarCamposIndividuales(
                        idClientePruebas,
                        entidadDePrueba.id!! + 789456,
                        mapOf<String, CampoModificable<Paquete, *>>(campoNombre.nombreCampo to campoNombre)
                                                        )
            }
        }

        @TestConMultiplesDAO
        fun con_nombre_duplicado_lanza_excepcion_ErrorCreacionActualizacionPorDuplicidad()
        {
            val entidadConCambios = repositorio.crear(
                    idClientePruebas,
                    Paquete(
                            idClientePruebas,
                            null,
                            entidadDePrueba.nombre + "nuevo",
                            "Descripción",
                            true,
                            entidadDePrueba.validoHasta.plusDays(2),
                            entidadDePrueba.validoHasta.plusDays(7),
                            fondosIncluidosPorDefecto.map {
                                Paquete.FondoIncluido(it.id!!, "código externo incluido", Decimal(123))
                            },
                            "código externo paquete"
                           )
                                                     )

            val campoNombreDuplicado = Paquete.CampoNombre(entidadDePrueba.nombre)

            assertThrows<ErrorCreacionActualizacionPorDuplicidad> {
                repositorio.actualizarCamposIndividuales(
                        idClientePruebas,
                        entidadConCambios.id!!,
                        mapOf<String, CampoModificable<Paquete, *>>(campoNombreDuplicado.nombreCampo to campoNombreDuplicado)
                                                        )
            }
        }

        @TestConMultiplesDAO
        fun con_id_existente_en_otro_cliente_lanza_excepcion_EntidadNoExiste()
        {
            val campoNombre = Paquete.CampoNombre(entidadDePrueba.nombre)

            ejecutarConClienteAlternativo {
                assertThrows<EntidadNoExiste> {
                    repositorio.actualizarCamposIndividuales(
                            it.id!!,
                            entidadDePrueba.id!!,
                            mapOf<String, CampoModificable<Paquete, *>>(campoNombre.nombreCampo to campoNombre)
                                                            )
                }
            }
        }

        @TestConMultiplesDAO
        fun con_id_cliente_inexistente_lanza_excepcion_EsquemaNoExiste()
        {
            val campoNombre = Paquete.CampoNombre(entidadDePrueba.nombre)

            assertThrows<EsquemaNoExiste> {
                repositorio.actualizarCamposIndividuales(
                        idClientePruebas + 100,
                        entidadDePrueba.id!!,
                        mapOf<String, CampoModificable<Paquete, *>>(campoNombre.nombreCampo to campoNombre)
                                                        )
            }
        }
    }

    @Nested
    inner class Eliminar
    {
        private lateinit var entidadDePrueba: Paquete

        @BeforeEach
        fun crearEntidadDePrueba()
        {
            entidadDePrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadValida())
        }

        @TestConMultiplesDAO
        fun si_existe_la_entidad_se_elimina_correctamente()
        {
            val resultadoEliminacion = repositorio.eliminarPorId(idClientePruebas, entidadDePrueba.id!!)
            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.id!!)

            assertTrue(resultadoEliminacion)
            assertNull(entidadConsultada)
        }

        @TestConMultiplesDAO
        fun se_eliminan_relaciones_fondo_paquete_correctamente()
        {
            val resultadoEliminacion = repositorio.eliminarPorId(idClientePruebas, entidadDePrueba.id!!)
            assertTrue(resultadoEliminacion)
            assertFalse(existeAlgunFondoPaqueteDAO(idClientePruebas, configuracionRepositorios))
        }

        @TestConMultiplesDAO
        fun con_id_existente_en_otro_cliente_retorna_false_y_no_elimina_entidad_existente()
        {
            ejecutarConClienteAlternativo {
                val resultadoEliminacion = repositorio.eliminarPorId(it.id!!, entidadDePrueba.id!!)
                kotlin.test.assertFalse(resultadoEliminacion)
            }
            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.id!!)
            assertEquals(entidadDePrueba, entidadConsultada)
        }

        @TestConMultiplesDAO
        fun con_id_cliente_inexistente_lanza_excepcion_EsquemaNoExiste()
        {
            assertThrows<EsquemaNoExiste> { repositorio.eliminarPorId(idClientePruebas + 100, entidadDePrueba.id!!) }
        }

        @TestConMultiplesDAO
        fun si_no_existe_la_entidad_retorna_false_y_no_elimina_entidad_existente()
        {
            val resultadoEliminacion = repositorio.eliminarPorId(idClientePruebas, entidadDePrueba.id!! + 1)
            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.id!!)

            assertFalse(resultadoEliminacion)
            assertEquals(entidadDePrueba, entidadConsultada)
        }
    }
}