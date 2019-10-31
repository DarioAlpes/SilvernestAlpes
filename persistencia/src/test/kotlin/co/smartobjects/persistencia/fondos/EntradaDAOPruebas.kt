package co.smartobjects.persistencia.fondos

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.fondos.Acceso
import co.smartobjects.entidades.fondos.Entrada
import co.smartobjects.entidades.fondos.Fondo
import co.smartobjects.entidades.fondos.precios.Impuesto
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.entidades.ubicaciones.Ubicacion
import co.smartobjects.persistencia.EntidadDAOBasePruebas
import co.smartobjects.persistencia.TestConMultiplesDAO
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorio
import co.smartobjects.persistencia.excepciones.EntidadNoExiste
import co.smartobjects.persistencia.excepciones.ErrorCreacionActualizacionPorDuplicidad
import co.smartobjects.persistencia.excepciones.ErrorDeLlaveForanea
import co.smartobjects.persistencia.excepciones.EsquemaNoExiste
import co.smartobjects.persistencia.existeAlgunAccesoDAO
import co.smartobjects.persistencia.existeAlgunFondoDAO
import co.smartobjects.persistencia.fondos.acceso.RepositorioAccesos
import co.smartobjects.persistencia.fondos.acceso.RepositorioAccesosSQL
import co.smartobjects.persistencia.fondos.acceso.RepositorioEntradas
import co.smartobjects.persistencia.fondos.acceso.RepositorioEntradasSQL
import co.smartobjects.persistencia.fondos.precios.impuestos.RepositorioImpuestos
import co.smartobjects.persistencia.fondos.precios.impuestos.RepositorioImpuestosSQL
import co.smartobjects.persistencia.ubicaciones.RepositorioUbicaciones
import co.smartobjects.persistencia.ubicaciones.RepositorioUbicacionesSQL
import co.smartobjects.utilidades.Decimal
import org.junit.jupiter.api.*
import java.math.BigDecimal
import java.util.*
import kotlin.test.*

@DisplayName("EntradaDAO")
internal class EntradaDAOPruebas : EntidadDAOBasePruebas()
{
    private val repositorioImpuestos: RepositorioImpuestos by lazy { RepositorioImpuestosSQL(configuracionRepositorios) }
    private val repositorioUbicaciones: RepositorioUbicaciones by lazy { RepositorioUbicacionesSQL(configuracionRepositorios) }
    private val repositorioAccesos: RepositorioAccesos by lazy { RepositorioAccesosSQL(configuracionRepositorios) }
    private val repositorio: RepositorioEntradas by lazy { RepositorioEntradasSQL(configuracionRepositorios) }
    override val creadoresRepositoriosUsados: List<CreadorRepositorio<*>> by lazy {
        listOf<CreadorRepositorio<*>>(
                repositorioImpuestos, repositorioUbicaciones, repositorioAccesos, repositorio
                                     )
    }

    private lateinit var impuestoPrueba: Impuesto

    @BeforeEach
    fun antesDeCadaPrueba()
    {
        impuestoPrueba = repositorioImpuestos.crear(idClientePruebas, impuestoPruebasPorDefecto)
    }

    private fun instanciarEntradaPrueba(): Entrada
    {
        return Entrada(
                idClientePruebas,
                null,
                "Entrada",
                true,
                true,
                Precio(Decimal.UNO, impuestoPrueba.id!!),
                "el código externo de prueba",
                0L
                      )
    }

    private fun crearUbicacionPrueba(idCliente: Long = idClientePruebas): Ubicacion
    {
        return repositorioUbicaciones.crear(
                idCliente,
                Ubicacion(
                        idClientePruebas,
                        null,
                        "Ubicacion abuela",
                        Ubicacion.Tipo.PROPIEDAD,
                        Ubicacion.Subtipo.POS,
                        null,
                        linkedSetOf()
                         )
                                           )
    }

    private fun darInstanciaEntradaConUbicacionValida(): Entrada
    {
        val ubicacionCreada = crearUbicacionPrueba()

        return instanciarEntradaPrueba().copiar(idUbicacion = ubicacionCreada.id!!)
    }

    @Nested
    inner class Crear
    {
        @TestConMultiplesDAO
        fun sin_id_asignado_retorna_misma_entidad_con_id_asignado_por_bd()
        {
            val entidadAInsertar = darInstanciaEntradaConUbicacionValida()

            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertNotNull(entidadCreada.id)
            assertEquals(entidadAInsertar.copiar(id = entidadCreada.id), entidadCreada)
        }

        @TestConMultiplesDAO
        fun con_id_asignado_retorna_misma_entidad_con_id_original()
        {
            val entidadAInsertar = darInstanciaEntradaConUbicacionValida().copiar(id = 9876543)

            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertEquals(9876543, entidadCreada.id)
            assertEquals(entidadAInsertar.copiar(id = entidadCreada.id), entidadCreada)
        }

        @TestConMultiplesDAO
        fun utiliza_el_parametro_id_cliente_y_retorna_misma_entidad_con_id_cliente_correcto()
        {
            val entidadAInsertar = darInstanciaEntradaConUbicacionValida().copiar(idCliente = Random().nextLong())

            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertEquals(idClientePruebas, entidadCreada.idCliente)
            val entidadEsperada =
                    entidadAInsertar
                        .copiar(id = entidadCreada.id)
                        .copiar(idCliente = idClientePruebas)
            assertEquals(entidadEsperada, entidadCreada)
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_con_ErrorCreacionActualizacionPorDuplicidad_si_se_usa_nombre_duplicado()
        {
            val entidadAInsertar = darInstanciaEntradaConUbicacionValida()

            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertThrows<ErrorCreacionActualizacionPorDuplicidad> {
                repositorio.crear(idClientePruebas, entidadCreada)
            }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_ErrorDeLlaveForanea_si_se_referencia_una_ubicacion_que_no_existe()
        {
            val entidadACrear = instanciarEntradaPrueba()

            assertThrows<ErrorDeLlaveForanea> {
                repositorio.crear(idClientePruebas, entidadACrear)
            }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_ErrorDeLlaveForanea_si_se_referencia_un_impuesto_que_no_existe()
        {
            val entidadACrear = darInstanciaEntradaConUbicacionValida().let {
                it.copiar(precioPorDefecto = it.precioPorDefecto.copiar(idImpuesto = it.precioPorDefecto.idImpuesto + 1))
            }

            assertThrows<ErrorDeLlaveForanea> {
                repositorio.crear(idClientePruebas, entidadACrear)
            }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_EsquemaNoExiste_si_cliente_con_id_dado_no_existe()
        {
            val entidadAInsertar = darInstanciaEntradaConUbicacionValida()

            assertThrows<EsquemaNoExiste> {
                repositorio.crear(idClientePruebas + 100, entidadAInsertar)
            }
        }
    }

    @Nested
    inner class Consultar
    {
        @TestConMultiplesDAO
        fun por_id_existente_retorna_entidad_correcta()
        {
            val entidadCreada = repositorio.crear(idClientePruebas, darInstanciaEntradaConUbicacionValida())

            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadCreada.id!!)

            assertEquals(entidadCreada, entidadConsultada)
        }

        @TestConMultiplesDAO
        fun una_entrada_por_id_existente_retorna_la_entrada_como_acceso()
        {
            val ubicacion = crearUbicacionPrueba()
            val entidadPrueba =
                    repositorio.crear(
                            idClientePruebas,
                            Entrada(
                                    idClientePruebas,
                                    null,
                                    "Entrada 1",
                                    true,
                                    true,
                                    Precio(Decimal.DIEZ, impuestoPrueba.id!!),
                                    "el código externo de prueba",
                                    ubicacion.id!!
                                   ))

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
            val ubicacion = crearUbicacionPrueba()
            val entidadesPrueba = (1..3).map {
                repositorio.crear(
                        idClientePruebas,
                        instanciarEntradaPrueba()
                            .copiar(nombre = "Entidad $it", idUbicacion = ubicacion.id!!)
                                 )
            }

            val listadoEsperado = entidadesPrueba.sortedBy { it.id!! }

            val listadoConsultado = repositorio.listar(idClientePruebas).toList()

            assertEquals(listadoEsperado, listadoConsultado.sortedBy { it.id!! })
        }

        @TestConMultiplesDAO
        fun listado_de_entidades_con_accesos_retorna_solo_entradas()
        {
            val ubicacion = crearUbicacionPrueba()

            val entradasCreadas =
                    (1..3).map {
                        repositorio.crear(
                                idClientePruebas,
                                instanciarEntradaPrueba()
                                    .copiar(nombre = "Entrada $it", idUbicacion = ubicacion.id!!)
                                         )
                    }.sortedBy { it.id }

            repositorioAccesos.crear(
                    idClientePruebas,
                    Acceso(
                            idClientePruebas,
                            null,
                            "Acceso 1",
                            true,
                            true,
                            true,
                            Precio(Decimal.DIEZ, impuestoPrueba.id!!),
                            "el código externo de prueba acceso 1",
                            ubicacion.id!!
                          )
                                    )

            repositorioAccesos.crear(
                    idClientePruebas,
                    Acceso(
                            idClientePruebas,
                            null,
                            "Acceso 2",
                            true,
                            true,
                            true,
                            Precio(Decimal.DIEZ, impuestoPrueba.id!!),
                            "el código externo de prueba acceso 2",
                            ubicacion.id!!
                          )
                                    )

            val entradasConsultadas = repositorio.listar(idClientePruebas).toList().sortedBy { it.id }

            assertEquals(entradasCreadas, entradasConsultadas)
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
            val entidadPrueba = repositorio.crear(idClientePruebas, darInstanciaEntradaConUbicacionValida())

            ejecutarConClienteAlternativo {
                val entidadConsultada = repositorio.buscarPorId(it.id!!, entidadPrueba.id!!)
                assertNull(entidadConsultada)
            }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_EsquemaNoExiste_si_se_consulta_por_id_con_id_cliente_inexistente()
        {
            assertThrows<EsquemaNoExiste> {
                repositorio.buscarPorId(idClientePruebas + 100, 789456789)
            }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_EsquemaNoExiste_si_se_consulta_listado_de_entidades_con_id_cliente_inexistente()
        {
            assertThrows<EsquemaNoExiste> {
                repositorio.listar(idClientePruebas + 100)
            }
        }

        @TestConMultiplesDAO
        fun listado_de_entidades_con_entidades_en_otro_cliente_retorna_lista_vacia()
        {
            repositorio.crear(idClientePruebas, darInstanciaEntradaConUbicacionValida())

            ejecutarConClienteAlternativo {
                val listadoConsultado = repositorio.listar(it.id!!)
                Assertions.assertTrue(listadoConsultado.none())
            }
        }
    }

    @Nested
    inner class Actualizar
    {
        private lateinit var entidadDePrueba: Entrada

        @BeforeEach
        fun crearEntidadDePrueba()
        {
            entidadDePrueba = repositorio.crear(idClientePruebas, darInstanciaEntradaConUbicacionValida())
        }

        @TestConMultiplesDAO
        fun se_actualizan_todos_los_campos_correctamente()
        {
            val nuevaUbicacion =
                    repositorioUbicaciones.crear(
                            idClientePruebas,
                            Ubicacion(
                                    idClientePruebas,
                                    null,
                                    "Ubicacion nueva",
                                    Ubicacion.Tipo.PROPIEDAD,
                                    Ubicacion.Subtipo.POS,
                                    null,
                                    linkedSetOf()
                                     )
                                                )

            val impuestoCreado =
                    repositorioImpuestos.crear(
                            idClientePruebas,
                            impuestoPruebasPorDefecto.copiar(
                                    nombre = impuestoPruebasPorDefecto.nombre + "nuevo",
                                    tasa = Decimal(impuestoPruebasPorDefecto.tasa.valor.add(BigDecimal.ONE))
                                                            )
                                              )

            val entidadAActualizar =
                    Entrada(
                            idClientePruebas,
                            entidadDePrueba.id,
                            "Entrada Nuevo",
                            false,
                            false,
                            Precio(Decimal.DIEZ, impuestoCreado.id!!),
                            "el código externo de prueba entrada",
                            nuevaUbicacion.id!!
                           )

            val entidadActualizada = repositorio.actualizar(idClientePruebas, entidadAActualizar.id!!, entidadAActualizar)

            assertEquals(entidadAActualizar, entidadActualizada)
        }

        @TestConMultiplesDAO
        fun utiliza_el_parametro_id_cliente_y_retorna_misma_entidad_con_id_cliente_correcto()
        {
            val entidadConCambios = entidadDePrueba.copiar(idCliente = Random().nextLong())

            val entidadActualizada = repositorio.actualizar(idClientePruebas, entidadConCambios.id!!, entidadConCambios)

            assertEquals(idClientePruebas, entidadActualizada.idCliente)
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
            val entidadConCambios = repositorio.crear(
                    idClientePruebas,
                    Entrada(
                            idClientePruebas,
                            null,
                            entidadDePrueba.nombre + "cambio",
                            true,
                            true,
                            Precio(Decimal.DIEZ, impuestoPrueba.id!!),
                            "el código externo de prueba",
                            entidadDePrueba.idUbicacion
                           )
                                                     )

            assertThrows<ErrorCreacionActualizacionPorDuplicidad> {
                repositorio.actualizar(idClientePruebas, entidadConCambios.id!!, entidadConCambios.copiar(nombre = entidadDePrueba.nombre))
            }
        }

        @TestConMultiplesDAO
        fun con_ubicacion_inexistente_lanza_excepcion_ErrorDeLlaveForanea()
        {
            val entidadConCambios = entidadDePrueba.copiar(idUbicacion = 789456)

            assertThrows<ErrorDeLlaveForanea> {
                repositorio.actualizar(idClientePruebas, entidadConCambios.id!!, entidadConCambios)
            }
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
                val idUbicacion = crearUbicacionPrueba(it.id!!).id!!
                assertThrows<EntidadNoExiste> { repositorio.actualizar(it.id!!, entidadDePrueba.id!!, entidadDePrueba.copiar(idUbicacion = idUbicacion)) }
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
        private lateinit var entidadDePrueba: Entrada

        @BeforeEach
        fun crearEntidadDePrueba()
        {
            entidadDePrueba = repositorio.crear(idClientePruebas, darInstanciaEntradaConUbicacionValida())
        }

        @TestConMultiplesDAO
        fun solo_se_actualizan_los_campos_necesarios()
        {
            val nuevoValor = Fondo.CampoDisponibleParaLaVenta<Entrada>(!entidadDePrueba.disponibleParaLaVenta)

            repositorio.actualizarCamposIndividuales(
                    idClientePruebas,
                    entidadDePrueba.id!!,
                    mapOf<String, CampoModificable<Entrada, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                    )

            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.id!!)
            val entidadEsperada =
                    Entrada(
                            entidadDePrueba.idCliente,
                            entidadDePrueba.id,
                            entidadDePrueba.nombre,
                            nuevoValor.valor,
                            entidadDePrueba.esIlimitado,
                            entidadDePrueba.precioPorDefecto,
                            entidadDePrueba.codigoExterno,
                            entidadDePrueba.idUbicacion
                           )

            assertEquals(entidadEsperada, entidadConsultada)
        }

        @TestConMultiplesDAO
        fun con_id_inexistente_lanza_excepcion_EntidadNoExiste()
        {
            val idDePrueba = entidadDePrueba.id!! + 789456
            val nuevoValor = Fondo.CampoDisponibleParaLaVenta<Entrada>(!entidadDePrueba.disponibleParaLaVenta)

            assertThrows<EntidadNoExiste> {
                repositorio.actualizarCamposIndividuales(
                        idClientePruebas,
                        idDePrueba,
                        mapOf<String, CampoModificable<Entrada, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                        )
            }
        }

        @TestConMultiplesDAO
        fun con_id_existente_en_otro_cliente_lanza_excepcion_EntidadNoExiste()
        {
            val idDePrueba = entidadDePrueba.id!! + 789456
            val nuevoValor = Fondo.CampoDisponibleParaLaVenta<Entrada>(!entidadDePrueba.disponibleParaLaVenta)

            ejecutarConClienteAlternativo {
                assertThrows<EntidadNoExiste> {
                    repositorio.actualizarCamposIndividuales(
                            it.id!!,
                            idDePrueba,
                            mapOf<String, CampoModificable<Entrada, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                            )
                }
            }
        }

        @TestConMultiplesDAO
        fun con_id_cliente_inexistente_lanza_excepcion_EsquemaNoExiste()
        {
            val idDePrueba = entidadDePrueba.id!! + 789456
            val nuevoValor = Fondo.CampoDisponibleParaLaVenta<Entrada>(!entidadDePrueba.disponibleParaLaVenta)

            assertThrows<EsquemaNoExiste> {
                repositorio.actualizarCamposIndividuales(
                        idClientePruebas + 100,
                        idDePrueba,
                        mapOf<String, CampoModificable<Entrada, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                        )
            }
        }
    }

    @Nested
    inner class Eliminar
    {
        private lateinit var entidadPrueba: Entrada

        @BeforeEach
        fun crearEntidadDePrueba()
        {
            entidadPrueba = repositorio.crear(idClientePruebas, darInstanciaEntradaConUbicacionValida())
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
            assertFalse(existeAlgunAccesoDAO(idClientePruebas, configuracionRepositorios))
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
            assertThrows<EsquemaNoExiste> {
                repositorio.eliminarPorId(idClientePruebas + 100, entidadPrueba.id!!)
            }
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
        fun si_se_intenta_borrar_la_ubicacion_lanza_excepcion_ErrorDeLlaveForanea_y_no_elimina_entidad()
        {
            assertThrows<ErrorDeLlaveForanea> { repositorioUbicaciones.eliminarPorId(idClientePruebas, entidadPrueba.idUbicacion) }
            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadPrueba.id!!)
            assertEquals(entidadPrueba, entidadConsultada)
        }
    }
}