package co.smartobjects.persistencia.fondos.libros

import co.smartobjects.entidades.fondos.Dinero
import co.smartobjects.entidades.fondos.Fondo
import co.smartobjects.entidades.fondos.libros.LibroDePrecios
import co.smartobjects.entidades.fondos.libros.PrecioEnLibro
import co.smartobjects.entidades.fondos.precios.Impuesto
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.persistencia.EntidadDAOBasePruebas
import co.smartobjects.persistencia.TestConMultiplesDAO
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorio
import co.smartobjects.persistencia.excepciones.EntidadNoExiste
import co.smartobjects.persistencia.excepciones.ErrorCreacionActualizacionPorDuplicidad
import co.smartobjects.persistencia.excepciones.ErrorDeLlaveForanea
import co.smartobjects.persistencia.excepciones.EsquemaNoExiste
import co.smartobjects.persistencia.existeAlgunPrecioDeFondoDAO
import co.smartobjects.persistencia.fondos.impuestoPruebasPorDefecto
import co.smartobjects.persistencia.fondos.monedas.RepositorioMonedas
import co.smartobjects.persistencia.fondos.monedas.RepositorioMonedasSQL
import co.smartobjects.persistencia.fondos.precios.impuestos.RepositorioImpuestos
import co.smartobjects.persistencia.fondos.precios.impuestos.RepositorioImpuestosSQL
import co.smartobjects.utilidades.Decimal
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import kotlin.test.*

@DisplayName("LibroDePrecios")
internal class LibroDePreciosPruebas : EntidadDAOBasePruebas()
{
    companion object
    {
        private const val NUMERO_DE_PRECIOS_EN_LIBRO = 5
    }

    private val repositorioImpuestos: RepositorioImpuestos by lazy { RepositorioImpuestosSQL(configuracionRepositorios) }
    private val repositorioMonedas: RepositorioMonedas by lazy { RepositorioMonedasSQL(configuracionRepositorios) }
    private val repositorio: RepositorioLibrosDePrecios by lazy { RepositorioLibrosDePreciosSQL(configuracionRepositorios) }

    override val creadoresRepositoriosUsados: List<CreadorRepositorio<*>> by lazy {
        listOf<CreadorRepositorio<*>>(repositorioImpuestos, repositorioMonedas, repositorio)
    }

    private lateinit var impuesto: Impuesto

    @BeforeEach
    fun antesDeCadaTest()
    {
        impuesto = repositorioImpuestos.crear(idClientePruebas, impuestoPruebasPorDefecto)
    }

    private fun instanciarLibroDePreciosDePrueba(): LibroDePrecios
    {
        val preciosDeFondos =
                List(NUMERO_DE_PRECIOS_EN_LIBRO) {
                    val fondo = repositorioMonedas.crear(
                            idClientePruebas,
                            Dinero(
                                    idClientePruebas,
                                    null,
                                    "Dinero de prueba $it",
                                    true,
                                    false,
                                    false,
                                    Precio(Decimal(99999), impuesto.id!!),
                                    "el código externo de prueba $it"
                                  )
                                                        )

                    PrecioEnLibro(Precio(Decimal(it), impuesto.id!!), fondo.id!!)
                }
                    .toSet()

        return LibroDePrecios(idClientePruebas, null, "Libro de precios en prueba", preciosDeFondos)
    }

    private fun instanciarLibroDePreciosDePrueba(fondosCreados: List<Fondo<Dinero>>): LibroDePrecios
    {
        val preciosDeFondos =
                List(fondosCreados.size
                    ) {
                    PrecioEnLibro(Precio(Decimal(it), impuesto.id!!), fondosCreados[it].id!!)
                }
                    .toSet()

        return LibroDePrecios(idClientePruebas, null, "Libro de precios en prueba", preciosDeFondos)
    }

    private fun LibroDePrecios.actualizarIdImpuestoPrimerPrecio(nuevoIdImpuesto: Long): LibroDePrecios
    {
        val primerPrecioConIdImpuestoEditado = this.precios.first().precio.copiar(idImpuesto = nuevoIdImpuesto)
        val listaPrecios = precios.toMutableList()
        listaPrecios[0] = precios.first().copiar(precio = primerPrecioConIdImpuestoEditado)

        return copiar(precios = listaPrecios.toSet())
    }

    private fun LibroDePrecios.actualizarIdFondoPrimerPrecio(nuevoIdFondo: Long): LibroDePrecios
    {
        val primerPrecioConIdFondoEditado = this.precios.first().copiar(idFondo = nuevoIdFondo)
        val listaPrecios = precios.toMutableList()
        listaPrecios[0] = primerPrecioConIdFondoEditado

        return copiar(precios = listaPrecios.toSet())
    }

    @Nested
    inner class Instanciacion
    {
        @TestConMultiplesDAO
        fun al_pasar_un_libro_de_precios_se_usa_el_tipo_de_libro_correcto()
        {
            val entidadEnNegocio = LibroDePrecios(1, 2, "no importa", setOf(PrecioEnLibro(Precio(Decimal.DIEZ, 43535), 3453454)))
            val entidadEnDao = LibroDAO(entidadEnNegocio)

            assertEquals(LibroDAO.TipoEnBD.PRECIOS, entidadEnDao.tipo)
        }
    }

    @Nested
    inner class Crear
    {
        @TestConMultiplesDAO
        fun sin_id_asignado_retorna_misma_entidad_con_id_asignado_por_bd()
        {
            val entidadAInsertar = instanciarLibroDePreciosDePrueba()
            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertNotNull(entidadCreada.id)
            assertEquals(entidadAInsertar.copiar(id = entidadCreada.id), entidadCreada)
        }

        @TestConMultiplesDAO
        fun con_id_asignado_retorna_misma_entidad_con_id_original()
        {
            val entidadAInsertar = instanciarLibroDePreciosDePrueba().copiar(id = 9876543)

            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertEquals(9876543, entidadCreada.id)
            assertEquals(entidadAInsertar.copiar(id = entidadCreada.id), entidadCreada)
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_con_ErrorDeLlaveForanea_con_un_impuesto_inexistente_en_algun_precio()
        {
            val entidadAInsertar = instanciarLibroDePreciosDePrueba()
            val entidadModificada = entidadAInsertar.actualizarIdImpuestoPrimerPrecio(impuesto.id!! + 100)
            assertThrows<ErrorDeLlaveForanea> { repositorio.crear(idClientePruebas, entidadModificada) }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_con_ErrorDeLlaveForanea_con_un_fondo_inexiste_en_algun_precio()
        {
            val entidadAInsertar = instanciarLibroDePreciosDePrueba()
            val entidadModificada = entidadAInsertar.actualizarIdFondoPrimerPrecio(entidadAInsertar.precios.first().idFondo + 100)
            assertThrows<ErrorDeLlaveForanea> { repositorio.crear(idClientePruebas, entidadModificada) }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_con_ErrorCreacionActualizacionPorDuplicidad_si_se_usa_nombre_duplicado()
        {
            val entidadAInsertar = instanciarLibroDePreciosDePrueba()

            repositorio.crear(idClientePruebas, entidadAInsertar)

            val preciosDeFondos =
                    List(NUMERO_DE_PRECIOS_EN_LIBRO) {
                        val fondo = repositorioMonedas.crear(
                                idClientePruebas,
                                Dinero(
                                        idClientePruebas,
                                        null,
                                        "Dinero de prueba nuevo $it",
                                        true,
                                        false,
                                        false,
                                        Precio(Decimal(99999), impuesto.id!!),
                                        "el código externo de prueba $it"
                                      )
                                                            )

                        PrecioEnLibro(Precio(Decimal(it), impuesto.id!!), fondo.id!!)
                    }
                        .toSet()

            assertThrows<ErrorCreacionActualizacionPorDuplicidad> {
                repositorio.crear(idClientePruebas, entidadAInsertar.copiar(precios = preciosDeFondos))
            }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_EsquemaNoExiste_si_cliente_con_id_dado_no_existe()
        {
            val entidadAInsertar = instanciarLibroDePreciosDePrueba()

            assertThrows<EsquemaNoExiste> { repositorio.crear(idClientePruebas + 100, entidadAInsertar) }
        }
    }

    @Nested
    inner class Consultar
    {
        @TestConMultiplesDAO
        fun por_id_existente_retorna_entidad_correcta()
        {
            val entidadPrueba = repositorio.crear(idClientePruebas, instanciarLibroDePreciosDePrueba())

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
            val fondos =
                    List(NUMERO_DE_PRECIOS_EN_LIBRO) {
                        repositorioMonedas.crear(
                                idClientePruebas,
                                Dinero(
                                        idClientePruebas,
                                        null,
                                        "Dinero de prueba $it",
                                        true,
                                        false,
                                        false,
                                        Precio(Decimal(99999), impuesto.id!!),
                                        "el código externo de prueba $it"
                                      )
                                                )
                    }

            val entidadesPrueba = (1..3).map {
                repositorio.crear(
                        idClientePruebas,
                        instanciarLibroDePreciosDePrueba(fondos)
                            .copiar(nombre = "Entidad $it")
                                 )
            }

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
            val entidadPrueba = repositorio.crear(idClientePruebas, instanciarLibroDePreciosDePrueba())

            ejecutarConClienteAlternativo {
                val entidadConsultada = repositorio.buscarPorId(it.id!!, entidadPrueba.id!!)
                kotlin.test.assertNull(entidadConsultada)
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
            repositorio.crear(idClientePruebas, instanciarLibroDePreciosDePrueba())

            ejecutarConClienteAlternativo {
                val listadoConsultado = repositorio.listar(it.id!!)
                assertTrue(listadoConsultado.none())
            }
        }
    }

    @Nested
    inner class Actualizar
    {
        private lateinit var entidadDePrueba: LibroDePrecios

        @BeforeEach
        fun crearEntidadInicial()
        {
            entidadDePrueba = repositorio.crear(idClientePruebas, instanciarLibroDePreciosDePrueba())
        }

        @TestConMultiplesDAO
        fun se_actualizan_todos_los_campos_correctamente()
        {
            val nuevosFondos =
                    List(NUMERO_DE_PRECIOS_EN_LIBRO) {
                        repositorioMonedas.crear(
                                idClientePruebas,
                                Dinero(
                                        idClientePruebas,
                                        null,
                                        "Dinero de prueba $it nuevo",
                                        true,
                                        false,
                                        false,
                                        Precio(Decimal(99999), impuesto.id!!),
                                        "el código externo de prueba $it"
                                      )
                                                )
                    }

            val preciosDeFondosNuevos =
                    entidadDePrueba.precios.mapIndexed { index, precioDeFondo ->
                        PrecioEnLibro(Precio(precioDeFondo.precio.valor + 100, impuesto.id!!), nuevosFondos[index].id!!)
                    }.toSet()

            val entidadModificada =
                    entidadDePrueba.copiar(
                            nombre = entidadDePrueba.nombre + "Algo nuevo",
                            precios = preciosDeFondosNuevos
                                          )

            repositorio.actualizar(idClientePruebas, entidadDePrueba.id!!, entidadModificada)

            val entidadActualizada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.id!!)

            assertEquals(entidadModificada, entidadActualizada)
        }

        @TestConMultiplesDAO
        fun utiliza_el_parametro_id_cliente_y_retorna_misma_entidad_con_id_cliente_correcto()
        {
            val entidadConCambios = entidadDePrueba.copiar(idCliente = 987654)

            repositorio.actualizar(idClientePruebas, entidadConCambios.id!!, entidadConCambios)

            val entidadActualizada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.id!!)

            assertEquals(idClientePruebas, entidadActualizada!!.idCliente)
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
            val entidadConCambios = repositorio.crear(idClientePruebas, entidadDePrueba.copiar(id = null, nombre = entidadDePrueba.nombre + " con cambio"))

            assertThrows<ErrorCreacionActualizacionPorDuplicidad> {
                repositorio.actualizar(idClientePruebas, entidadConCambios.id!!, entidadConCambios.copiar(nombre = entidadDePrueba.nombre))
            }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_con_ErrorDeLlaveForanea_con_un_impuesto_inexistente_en_algun_precio()
        {
            val entidadConCambios = entidadDePrueba.actualizarIdImpuestoPrimerPrecio(impuesto.id!! + 100)

            assertThrows<ErrorDeLlaveForanea> {
                repositorio.actualizar(idClientePruebas, entidadConCambios.id!!, entidadConCambios)
            }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_con_ErrorDeLlaveForanea_con_un_fondo_inexiste_en_algun_precio()
        {
            val entidadConCambios = entidadDePrueba.actualizarIdFondoPrimerPrecio(entidadDePrueba.precios.first().idFondo + 100)

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
    inner class Eliminar
    {
        private lateinit var entidadInicial: LibroDePrecios

        @BeforeEach
        fun crearEntidadInicial()
        {
            entidadInicial = repositorio.crear(idClientePruebas, instanciarLibroDePreciosDePrueba())
        }

        @TestConMultiplesDAO
        fun si_existe_la_entidad_se_elimina_esta_y_sus_precios_correctamente()
        {
            val resultadoEliminacion = repositorio.eliminarPorId(idClientePruebas, entidadInicial.id!!)
            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadInicial.id!!)

            assertTrue(resultadoEliminacion)
            assertNull(entidadConsultada)
            assertFalse(existeAlgunPrecioDeFondoDAO(idClientePruebas, configuracionRepositorios))
        }

        @TestConMultiplesDAO
        fun si_no_existe_la_entidad_retorna_false_y_no_elimina_entidad_existente()
        {
            val resultadoEliminacion = repositorio.eliminarPorId(idClientePruebas, entidadInicial.id!! + 1)
            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadInicial.id!!)

            assertFalse(resultadoEliminacion)
            assertEquals(entidadInicial, entidadConsultada)
        }

        @TestConMultiplesDAO
        fun si_se_intenta_eliminar_algun_fondo_usado_lanza_excepcion_ErrorDeLlaveForanea()
        {
            assertThrows<ErrorDeLlaveForanea> {
                repositorioMonedas.eliminarPorId(idClientePruebas, entidadInicial.precios.first().idFondo)
            }
        }

        @TestConMultiplesDAO
        fun si_se_intenta_eliminar_algun_impuesto_usado_lanza_excepcion_ErrorDeLlaveForanea()
        {
            assertThrows<ErrorDeLlaveForanea> {
                repositorioImpuestos.eliminarPorId(idClientePruebas, entidadInicial.precios.first().precio.idImpuesto)
            }
        }

        @TestConMultiplesDAO
        fun con_id_existente_en_otro_cliente_retorna_false_y_no_elimina_entidad_existente()
        {
            ejecutarConClienteAlternativo {
                val resultadoEliminacion = repositorio.eliminarPorId(it.id!!, entidadInicial.id!!)
                kotlin.test.assertFalse(resultadoEliminacion)
            }
            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadInicial.id!!)
            assertEquals(entidadInicial, entidadConsultada)
        }

        @TestConMultiplesDAO
        fun con_id_cliente_inexistente_lanza_excepcion_EsquemaNoExiste()
        {
            assertThrows<EsquemaNoExiste> {
                repositorio.eliminarPorId(idClientePruebas + 100, entidadInicial.id!!)
            }
        }
    }
}
