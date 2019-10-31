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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.util.*
import kotlin.test.*

@DisplayName("AccesoDAO")
internal class AccesoDAOPruebas : EntidadDAOBasePruebas()
{
    private val repositorioImpuestos: RepositorioImpuestos by lazy { RepositorioImpuestosSQL(configuracionRepositorios) }
    private val repositorioUbicaciones: RepositorioUbicaciones by lazy { RepositorioUbicacionesSQL(configuracionRepositorios) }
    private val repositorioEntradas: RepositorioEntradas by lazy { RepositorioEntradasSQL(configuracionRepositorios) }
    private val repositorio: RepositorioAccesos by lazy { RepositorioAccesosSQL(configuracionRepositorios) }
    override val creadoresRepositoriosUsados: List<CreadorRepositorio<*>> by lazy {
        listOf<CreadorRepositorio<*>>(
                repositorioImpuestos, repositorioUbicaciones, repositorio, repositorioEntradas
                                     )
    }

    private lateinit var impuestoPrueba: Impuesto

    @BeforeEach
    fun antesDeCadaPrueba()
    {
        impuestoPrueba = repositorioImpuestos.crear(idClientePruebas, impuestoPruebasPorDefecto)
    }

    private fun instanciarAccesoPrueba(): Acceso
    {
        return Acceso(
                idClientePruebas,
                null,
                "Acceso",
                true,
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

    private fun darInstanciaAccesoConUbicacionValida(): Acceso
    {
        val ubicacionCreada = crearUbicacionPrueba()

        return instanciarAccesoPrueba().copiar(idUbicacion = ubicacionCreada.id!!)
    }

    @Nested
    inner class Crear
    {
        @TestConMultiplesDAO
        fun sin_id_asignado_retorna_misma_entidad_con_id_asignado_por_bd()
        {
            val entidadAInsertar = darInstanciaAccesoConUbicacionValida()

            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertNotNull(entidadCreada.id)
            assertEquals(entidadAInsertar.copiar(id = entidadCreada.id), entidadCreada)
        }

        @TestConMultiplesDAO
        fun con_id_asignado_retorna_misma_entidad_con_id_original()
        {
            val entidadAInsertar = darInstanciaAccesoConUbicacionValida().copiar(id = 36363L)

            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertEquals(36363L, entidadCreada.id)
            assertEquals(entidadAInsertar.copiar(id = entidadCreada.id), entidadCreada)
        }

        @TestConMultiplesDAO
        fun utiliza_el_parametro_id_cliente_y_retorna_misma_entidad_con_id_cliente_correcto()
        {
            val entidadAInsertar = darInstanciaAccesoConUbicacionValida().copiar(idCliente = Random().nextLong())

            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertEquals(idClientePruebas, entidadCreada.idCliente)
            val entidadEsperada = entidadAInsertar.copiar(id = entidadCreada.id, idCliente = idClientePruebas)
            assertEquals(entidadEsperada, entidadCreada)
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_con_ErrorCreacionActualizacionPorDuplicidad_si_se_usa_nombre_duplicado()
        {
            val entidadAInsertar = darInstanciaAccesoConUbicacionValida()

            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertThrows<ErrorCreacionActualizacionPorDuplicidad> {
                repositorio.crear(idClientePruebas, entidadCreada)
            }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_ErrorDeLlaveForanea_si_se_referencia_una_ubicacion_que_no_existe()
        {
            val entidadACrear = instanciarAccesoPrueba()

            assertThrows<ErrorDeLlaveForanea> {
                repositorio.crear(idClientePruebas, entidadACrear)
            }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_ErrorDeLlaveForanea_si_se_referencia_un_impuesto_que_no_existe()
        {
            val entidadACrear = darInstanciaAccesoConUbicacionValida().let {
                it.copiar(precioPorDefecto = it.precioPorDefecto.copiar(idImpuesto = it.precioPorDefecto.idImpuesto + 1))
            }

            assertThrows<ErrorDeLlaveForanea> {
                repositorio.crear(idClientePruebas, entidadACrear)
            }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_EsquemaNoExiste_si_cliente_con_id_dado_no_existe()
        {
            val entidadAInsertar = darInstanciaAccesoConUbicacionValida()

            assertThrows<EsquemaNoExiste> { repositorio.crear(idClientePruebas + 100, entidadAInsertar) }
        }
    }

    @Nested
    inner class Consultar
    {
        @TestConMultiplesDAO
        fun por_id_existente_retorna_entidad_correcta()
        {
            val entidadCreada = repositorio.crear(idClientePruebas, darInstanciaAccesoConUbicacionValida())

            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadCreada.id!!)

            assertEquals(entidadCreada, entidadConsultada)
        }

        @TestConMultiplesDAO
        fun una_entrada_por_id_existente_retorna_null()
        {
            val ubicacion = crearUbicacionPrueba()
            val entidadPrueba =
                    repositorioEntradas.crear(
                            idClientePruebas,
                            Entrada(
                                    idClientePruebas,
                                    null,
                                    "Entrada 1",
                                    true,
                                    true,
                                    Precio(Decimal.UNO, impuestoPrueba.id!!),
                                    "el código externo de prueba entrada",
                                    ubicacion.id!!
                                   ))

            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadPrueba.id!!)

            assertNull(entidadConsultada)
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
                        instanciarAccesoPrueba()
                            .copiar(nombre = "Entidad $it", idUbicacion = ubicacion.id!!)
                                 )
            }

            val listadoEsperado = entidadesPrueba.sortedBy { it.id!! }

            val listadoConsultado = repositorio.listar(idClientePruebas).toList()

            assertEquals(listadoEsperado, listadoConsultado.sortedBy { it.id!! })
        }

        @TestConMultiplesDAO
        fun listado_de_entidades_con_entradas_no_retorna_entidades()
        {
            val ubicacion = crearUbicacionPrueba()
            repositorioEntradas.crear(
                    idClientePruebas,
                    Entrada(
                            idClientePruebas,
                            null,
                            "Entrada 1",
                            true,
                            true,
                            Precio(Decimal.UNO, impuestoPrueba.id!!),
                            "el código externo de prueba entrada",
                            ubicacion.id!!
                           )
                                     )

            val listadoConsultado = repositorio.listar(idClientePruebas).toList()

            assertTrue(listadoConsultado.none())
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
            val entidadPrueba = repositorio.crear(idClientePruebas, darInstanciaAccesoConUbicacionValida())

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
            repositorio.crear(idClientePruebas, darInstanciaAccesoConUbicacionValida())

            ejecutarConClienteAlternativo {
                val listadoConsultado = repositorio.listar(it.id!!)
                assertTrue(listadoConsultado.none())
            }
        }
    }

    @Nested
    inner class Actualizar
    {
        private lateinit var entidadDePrueba: Acceso

        @BeforeEach
        fun crearEntidadDePrueba()
        {
            entidadDePrueba = repositorio.crear(idClientePruebas, darInstanciaAccesoConUbicacionValida())
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
                    Acceso(
                            idClientePruebas,
                            entidadDePrueba.id,
                            "Acceso Nuevo",
                            false,
                            false,
                            false,
                            Precio(Decimal.UNO, impuestoCreado.id!!),
                            "el código externo de prueba acceso",
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
                    Acceso(
                            idClientePruebas,
                            null,
                            entidadDePrueba.nombre + "cambio",
                            true,
                            true,
                            true,
                            Precio(Decimal.UNO, impuestoPrueba.id!!),
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
                assertThrows<EntidadNoExiste> {
                    repositorio.actualizar(it.id!!, entidadDePrueba.id!!, entidadDePrueba.copiar(idUbicacion = idUbicacion))
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
        private lateinit var entidadDePrueba: Acceso

        @BeforeEach
        fun crearEntidadDePrueba()
        {
            entidadDePrueba = repositorio.crear(idClientePruebas, darInstanciaAccesoConUbicacionValida())
        }

        @TestConMultiplesDAO
        fun solo_se_actualizan_los_campos_necesarios()
        {
            val nuevoValor = Fondo.CampoDisponibleParaLaVenta<Acceso>(!entidadDePrueba.disponibleParaLaVenta)
            repositorio.actualizarCamposIndividuales(
                    idClientePruebas,
                    entidadDePrueba.id!!,
                    mapOf<String, CampoModificable<Acceso, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                    )

            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.id!!)
            val entidadEsperada =
                    Acceso(
                            entidadDePrueba.idCliente,
                            entidadDePrueba.id,
                            entidadDePrueba.nombre,
                            !entidadDePrueba.disponibleParaLaVenta,
                            entidadDePrueba.debeAparecerSoloUnaVez,
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
            val nuevoValor = Fondo.CampoDisponibleParaLaVenta<Acceso>(!entidadDePrueba.disponibleParaLaVenta)
            assertThrows<EntidadNoExiste> {
                repositorio.actualizarCamposIndividuales(
                        idClientePruebas,
                        entidadDePrueba.id!! + 789456,
                        mapOf<String, CampoModificable<Acceso, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                        )
            }
        }

        @TestConMultiplesDAO
        fun con_id_existente_en_otro_cliente_lanza_excepcion_EntidadNoExiste()
        {
            val nuevoValor = Fondo.CampoDisponibleParaLaVenta<Acceso>(!entidadDePrueba.disponibleParaLaVenta)
            ejecutarConClienteAlternativo {
                assertThrows<EntidadNoExiste> {
                    repositorio.actualizarCamposIndividuales(
                            it.id!!,
                            entidadDePrueba.id!! + 789456,
                            mapOf<String, CampoModificable<Acceso, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                            )
                }
            }
        }

        @TestConMultiplesDAO
        fun con_id_cliente_inexistente_lanza_excepcion_EsquemaNoExiste()
        {
            val nuevoValor = Fondo.CampoDisponibleParaLaVenta<Acceso>(!entidadDePrueba.disponibleParaLaVenta)
            assertThrows<EsquemaNoExiste> {
                repositorio.actualizarCamposIndividuales(
                        idClientePruebas + 100,
                        entidadDePrueba.id!! + 789456,
                        mapOf<String, CampoModificable<Acceso, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                        )
            }
        }
    }

    @Nested
    inner class Eliminar
    {
        private lateinit var entidadPrueba: Acceso

        @BeforeEach
        fun crearEntidadDePrueba()
        {
            entidadPrueba = repositorio.crear(idClientePruebas, darInstanciaAccesoConUbicacionValida())
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
            assertThrows<EsquemaNoExiste> {
                repositorio.eliminarPorId(idClientePruebas + 100, entidadPrueba.id!!)
            }
        }

        @TestConMultiplesDAO
        fun si_se_intenta_borrar_la_ubicacion_lanza_excepcion_ErrorDeLlaveForanea_y_no_elimina_entidad()
        {
            assertThrows<ErrorDeLlaveForanea> {
                repositorioUbicaciones.eliminarPorId(idClientePruebas, entidadPrueba.idUbicacion)
            }

            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadPrueba.id!!)
            assertEquals(entidadPrueba, entidadConsultada)
        }
    }
}