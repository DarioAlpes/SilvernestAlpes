package co.smartobjects.persistencia.fondos.libros

import co.smartobjects.entidades.fondos.Dinero
import co.smartobjects.entidades.fondos.Paquete
import co.smartobjects.entidades.fondos.libros.LibroDeProhibiciones
import co.smartobjects.entidades.fondos.libros.Prohibicion
import co.smartobjects.entidades.fondos.precios.Impuesto
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.persistencia.EntidadDAOBasePruebas
import co.smartobjects.persistencia.TestConMultiplesDAO
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorio
import co.smartobjects.persistencia.excepciones.EntidadNoExiste
import co.smartobjects.persistencia.excepciones.ErrorCreacionActualizacionPorDuplicidad
import co.smartobjects.persistencia.excepciones.ErrorDeLlaveForanea
import co.smartobjects.persistencia.excepciones.EsquemaNoExiste
import co.smartobjects.persistencia.existeAlgunaProhibicionDAO
import co.smartobjects.persistencia.fondos.impuestoPruebasPorDefecto
import co.smartobjects.persistencia.fondos.monedas.RepositorioMonedas
import co.smartobjects.persistencia.fondos.monedas.RepositorioMonedasSQL
import co.smartobjects.persistencia.fondos.paquete.RepositorioPaquetes
import co.smartobjects.persistencia.fondos.paquete.RepositorioPaquetesSQL
import co.smartobjects.persistencia.fondos.precios.impuestos.RepositorioImpuestos
import co.smartobjects.persistencia.fondos.precios.impuestos.RepositorioImpuestosSQL
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.threeten.bp.ZonedDateTime
import kotlin.test.*

@DisplayName("LibroDeProhibiciones")
internal class LibroDeProhibicionesPruebas : EntidadDAOBasePruebas()
{
    private val repositorioImpuestos: RepositorioImpuestos by lazy { RepositorioImpuestosSQL(configuracionRepositorios) }
    private val repositorioMonedas: RepositorioMonedas by lazy { RepositorioMonedasSQL(configuracionRepositorios) }
    private val repositorioPaquetes: RepositorioPaquetes by lazy { RepositorioPaquetesSQL(configuracionRepositorios) }
    private val repositorio: RepositorioLibrosDeProhibiciones by lazy { RepositorioLibrosDeProhibicionesSQL(configuracionRepositorios) }

    override val creadoresRepositoriosUsados: List<CreadorRepositorio<*>> by lazy {
        listOf<CreadorRepositorio<*>>(repositorioImpuestos, repositorioMonedas, repositorioPaquetes, repositorio)
    }

    private lateinit var impuesto: Impuesto

    @BeforeEach
    fun antesDeCadaTest()
    {
        impuesto = repositorioImpuestos.crear(idClientePruebas, impuestoPruebasPorDefecto)
    }

    private fun instanciarLibroDeProhibicionesDePrueba(): LibroDeProhibiciones
    {
        val fondo = repositorioMonedas.crear(
                idClientePruebas,
                Dinero(
                        idClientePruebas,
                        null,
                        "Dinero de prueba 0",
                        true,
                        false,
                        false,
                        Precio(Decimal(99999), impuesto.id!!),
                        "el código externo de prueba dinero"
                      )
                                            )

        val fechaInicial = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
        val paquete =
                repositorioPaquetes.crear(
                        idClientePruebas,
                        Paquete(
                                idClientePruebas,
                                null,
                                "Paquete de prueba",
                                "Descripción",
                                true,
                                fechaInicial,
                                fechaInicial.plusDays(1),
                                listOf(Paquete.FondoIncluido(fondo.id!!, "código externo incluido", Decimal.UNO)),
                                "código externo paquete"
                               )
                                         )


        return LibroDeProhibiciones(idClientePruebas, null, "Libro prohibido", setOf(Prohibicion.DeFondo(fondo.id!!)), setOf(Prohibicion.DePaquete(paquete.id!!)))
    }

    private fun LibroDeProhibiciones.actualizarIdFondoPrimeraProhibicionDeFondo(nuevoId: Long): LibroDeProhibiciones
    {
        val primeraProhibicionConIdEditado = this.prohibicionesDeFondo.first().copiar(id = nuevoId)
        val listaDeProhibiciones = prohibicionesDeFondo.toMutableList()
        listaDeProhibiciones[0] = primeraProhibicionConIdEditado

        return copiar(prohibicionesDeFondo = listaDeProhibiciones.toSet())
    }

    private fun LibroDeProhibiciones.actualizarIdFondoPrimeraProhibicionDePaquete(nuevoId: Long): LibroDeProhibiciones
    {
        val primeraProhibicionConIdEditado = this.prohibicionesDePaquete.first().copiar(id = nuevoId)
        val listaDeProhibiciones = prohibicionesDePaquete.toMutableList()
        listaDeProhibiciones[0] = primeraProhibicionConIdEditado

        return copiar(prohibicionesDePaquete = listaDeProhibiciones.toSet())
    }

    @Nested
    inner class Instanciacion
    {
        @TestConMultiplesDAO
        fun al_pasar_un_libro_de_prohibiciones_se_usa_el_tipo_de_libro_correcto()
        {
            val entidadEnNegocio = LibroDeProhibiciones(1, 2, "no importa", setOf(Prohibicion.DeFondo(0)), setOf(Prohibicion.DePaquete(0)))
            val entidadEnDao = LibroDAO(entidadEnNegocio)

            assertEquals(LibroDAO.TipoEnBD.PROHIBICIONES, entidadEnDao.tipo)
        }
    }

    @Nested
    inner class Crear
    {
        @TestConMultiplesDAO
        fun sin_id_asignado_retorna_misma_entidad_con_id_asignado_por_bd()
        {
            val entidadAInsertar = instanciarLibroDeProhibicionesDePrueba()
            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertNotNull(entidadCreada.id)
            assertEquals(entidadAInsertar.copiar(id = entidadCreada.id), entidadCreada)
        }

        @TestConMultiplesDAO
        fun crear_entidad_sin_prohibiciones_de_fondo_correctamente()
        {
            val entidadAInsertar = instanciarLibroDeProhibicionesDePrueba().copiar(prohibicionesDeFondo = setOf())
            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertNotNull(entidadCreada.id)
            assertEquals(entidadAInsertar.copiar(id = entidadCreada.id), entidadCreada)
        }

        @TestConMultiplesDAO
        fun crear_entidad_sin_prohibiciones_de_paquete_correctamente()
        {
            val entidadAInsertar = instanciarLibroDeProhibicionesDePrueba().copiar(prohibicionesDePaquete = setOf())
            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertNotNull(entidadCreada.id)
            assertEquals(entidadAInsertar.copiar(id = entidadCreada.id), entidadCreada)
        }

        @TestConMultiplesDAO
        fun con_id_asignado_retorna_misma_entidad_con_id_original()
        {
            val entidadAInsertar = instanciarLibroDeProhibicionesDePrueba().copiar(id = 9876543)

            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertEquals(9876543, entidadCreada.id)
            assertEquals(entidadAInsertar.copiar(id = entidadCreada.id), entidadCreada)
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_con_ErrorDeLlaveForanea_con_un_fondo_inexiste_en_alguna_prohibicion_de_fondo()
        {
            val entidadAInsertar = instanciarLibroDeProhibicionesDePrueba()
            val entidadModificada = entidadAInsertar.actualizarIdFondoPrimeraProhibicionDeFondo(entidadAInsertar.prohibicionesDeFondo.first().id + 100)

            assertThrows<ErrorDeLlaveForanea> {
                repositorio.crear(idClientePruebas, entidadModificada)
            }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_con_ErrorDeLlaveForanea_con_un_fondo_inexiste_en_alguna_prohibicion_de_paquete()
        {
            val entidadAInsertar = instanciarLibroDeProhibicionesDePrueba()
            val entidadModificada = entidadAInsertar.actualizarIdFondoPrimeraProhibicionDePaquete(entidadAInsertar.prohibicionesDePaquete.first().id + 100)

            assertThrows<ErrorDeLlaveForanea> {
                repositorio.crear(idClientePruebas, entidadModificada)
            }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_con_ErrorCreacionActualizacionPorDuplicidad_si_se_usa_nombre_duplicado()
        {
            val entidadOriginal = instanciarLibroDeProhibicionesDePrueba()

            repositorio.crear(idClientePruebas, entidadOriginal)

            val fondo = repositorioMonedas.crear(
                    idClientePruebas,
                    Dinero(
                            idClientePruebas,
                            null,
                            "fondo nuevo para prueba",
                            true,
                            false,
                            false,
                            Precio(Decimal(345345), impuesto.id!!),
                            "el código externo de prueba dinero"
                          )
                                                )

            val fechaInicial = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
            val paquete =
                    repositorioPaquetes.crear(
                            idClientePruebas,
                            Paquete(
                                    idClientePruebas,
                                    null,
                                    "Paquete nuevo para prueba",
                                    "Descripción nueva para prueba",
                                    true,
                                    fechaInicial,
                                    fechaInicial.plusDays(1),
                                    listOf(Paquete.FondoIncluido(fondo.id!!, "código externo incluido", Decimal.DIEZ)),
                                    "código externo paquete"
                                   )
                                             )

            val entidadNuevaConMismoNombre =
                    LibroDeProhibiciones(
                            idClientePruebas,
                            null,
                            entidadOriginal.nombre,
                            setOf(Prohibicion.DeFondo(fondo.id!!)),
                            setOf(Prohibicion.DePaquete(paquete.id!!))
                                        )

            assertThrows<ErrorCreacionActualizacionPorDuplicidad> {
                repositorio.crear(idClientePruebas, entidadNuevaConMismoNombre)
            }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_EsquemaNoExiste_si_cliente_con_id_dado_no_existe()
        {
            val entidadAInsertar = instanciarLibroDeProhibicionesDePrueba()

            assertThrows<EsquemaNoExiste> { repositorio.crear(idClientePruebas + 100, entidadAInsertar) }
        }
    }

    @Nested
    inner class Consultar
    {
        @TestConMultiplesDAO
        fun por_id_existente_retorna_entidad_correcta()
        {
            val entidadPrueba = repositorio.crear(idClientePruebas, instanciarLibroDeProhibicionesDePrueba())

            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadPrueba.id!!)

            assertEquals(entidadPrueba, entidadConsultada)
        }

        @TestConMultiplesDAO
        fun por_id_existente_pero_sin_prohibiciones_de_fondo_retorna_entidad_correcta()
        {
            val entidadPrueba = repositorio.crear(
                    idClientePruebas, instanciarLibroDeProhibicionesDePrueba().copiar(prohibicionesDeFondo = setOf())
                                                 )

            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadPrueba.id!!)

            assertEquals(entidadPrueba, entidadConsultada)
        }

        @TestConMultiplesDAO
        fun por_id_existente_pero_sin_prohibiciones_de_paquete_retorna_entidad_correcta()
        {
            val entidadPrueba = repositorio.crear(
                    idClientePruebas, instanciarLibroDeProhibicionesDePrueba().copiar(prohibicionesDePaquete = setOf())
                                                 )

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
            val numeroDeLibrosAConsultar = 3
            val fondos =
                    List(numeroDeLibrosAConsultar) {
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
                                        "el código externo de prueba dinero $it"
                                      )
                                                )
                    }

            val fechaInicial = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
            val paquetes =
                    List(numeroDeLibrosAConsultar) {
                        repositorioPaquetes.crear(
                                idClientePruebas,
                                Paquete(
                                        idClientePruebas,
                                        null,
                                        "Paquete de prueba $it",
                                        "Descripción",
                                        true,
                                        fechaInicial.plusDays(it.toLong()),
                                        fechaInicial.plusDays(it + 1L),
                                        listOf(Paquete.FondoIncluido(fondos[it].id!!, "código externo incluido $it", Decimal.UNO)),
                                        "código externo paquete"
                                       )
                                                 )
                    }

            val entidadesPrueba =
                    (0 until fondos.size)
                        .map {
                            repositorio.crear(
                                    idClientePruebas,
                                    LibroDeProhibiciones(
                                            idClientePruebas,
                                            null,
                                            "libro de prohibiciones prueba $it",
                                            setOf(Prohibicion.DeFondo(fondos[it].id!!)),
                                            setOf(Prohibicion.DePaquete(paquetes[it].id!!))
                                                        )
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
            val entidadPrueba = repositorio.crear(idClientePruebas, instanciarLibroDeProhibicionesDePrueba())

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
            repositorio.crear(idClientePruebas, instanciarLibroDeProhibicionesDePrueba())

            ejecutarConClienteAlternativo {
                val listadoConsultado = repositorio.listar(it.id!!)
                assertTrue(listadoConsultado.none())
            }
        }
    }

    @Nested
    inner class Actualizar
    {
        private lateinit var entidadDePrueba: LibroDeProhibiciones

        @BeforeEach
        fun crearEntidadInicial()
        {
            entidadDePrueba = repositorio.crear(idClientePruebas, instanciarLibroDeProhibicionesDePrueba())
        }

        private fun darEntidadModificada(): LibroDeProhibiciones
        {
            val fondo = repositorioMonedas.crear(
                    idClientePruebas,
                    Dinero(
                            idClientePruebas,
                            null,
                            "fondo nuevo para prueba",
                            true,
                            false,
                            false,
                            Precio(Decimal(345345), impuesto.id!!),
                            "el código externo de prueba"
                          )
                                                )

            val fechaInicial = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
            val paquete =
                    repositorioPaquetes.crear(
                            idClientePruebas,
                            Paquete(
                                    idClientePruebas,
                                    null,
                                    "Paquete nuevo para prueba",
                                    "Descripción nueva para prueba",
                                    true,
                                    fechaInicial,
                                    fechaInicial.plusDays(1),
                                    listOf(Paquete.FondoIncluido(fondo.id!!, "código externo incluido nuevo", Decimal.DIEZ)),
                                    "código externo paquete"
                                   )
                                             )

            return entidadDePrueba.copiar(
                    nombre = entidadDePrueba.nombre + "Algo nuevo",
                    prohibicionesDeFondo = setOf(Prohibicion.DeFondo(fondo.id!!)),
                    prohibicionesDePaquete = setOf(Prohibicion.DePaquete(paquete.id!!))
                                         )
        }

        @TestConMultiplesDAO
        fun se_actualizan_todos_los_campos_correctamente()
        {
            val entidadModificada = darEntidadModificada()

            repositorio.actualizar(idClientePruebas, entidadDePrueba.id!!, entidadModificada)

            val entidadActualizada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.id!!)

            assertEquals(entidadModificada, entidadActualizada)
        }

        @TestConMultiplesDAO
        fun se_actualiza_si_no_tiene_prohibiciones_de_fondo_correctamente()
        {
            val entidadModificada = darEntidadModificada().copiar(prohibicionesDeFondo = setOf())

            repositorio.actualizar(idClientePruebas, entidadDePrueba.id!!, entidadModificada)

            val entidadActualizada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.id!!)

            assertEquals(entidadModificada, entidadActualizada)
        }

        @TestConMultiplesDAO
        fun se_actualiza_si_no_tiene_prohibiciones_de_paquete_correctamente()
        {
            val entidadModificada = darEntidadModificada().copiar(prohibicionesDePaquete = setOf())

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
        fun lanza_excepcion_con_ErrorDeLlaveForanea_con_un_fondo_inexiste_en_alguna_prohibicion_de_fondo()
        {
            val entidadModificada = entidadDePrueba.actualizarIdFondoPrimeraProhibicionDeFondo(entidadDePrueba.prohibicionesDeFondo.first().id + 100)

            assertThrows<ErrorDeLlaveForanea> {
                repositorio.actualizar(idClientePruebas, entidadModificada.id!!, entidadModificada)
            }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_con_ErrorDeLlaveForanea_con_un_fondo_inexiste_en_alguna_prohibicion_de_paquete()
        {
            val entidadModificada = entidadDePrueba.actualizarIdFondoPrimeraProhibicionDePaquete(entidadDePrueba.prohibicionesDePaquete.first().id + 100)

            assertThrows<ErrorDeLlaveForanea> {
                repositorio.actualizar(idClientePruebas, entidadModificada.id!!, entidadModificada)
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
        private lateinit var entidadInicial: LibroDeProhibiciones

        @BeforeEach
        fun crearEntidadInicial()
        {
            entidadInicial = repositorio.crear(idClientePruebas, instanciarLibroDeProhibicionesDePrueba())
        }

        @TestConMultiplesDAO
        fun si_existe_la_entidad_se_elimina_esta_y_sus_prohibiciones_correctamente()
        {
            val resultadoEliminacion = repositorio.eliminarPorId(idClientePruebas, entidadInicial.id!!)
            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadInicial.id!!)

            assertTrue(resultadoEliminacion)
            assertNull(entidadConsultada)
            assertFalse(existeAlgunaProhibicionDAO(idClientePruebas, configuracionRepositorios))
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
                repositorioMonedas.eliminarPorId(idClientePruebas, entidadInicial.prohibicionesDeFondo.first().id)
            }
        }

        @TestConMultiplesDAO
        fun si_se_intenta_eliminar_algun_paquete_usado_lanza_excepcion_ErrorDeLlaveForanea()
        {
            assertThrows<ErrorDeLlaveForanea> {
                repositorioPaquetes.eliminarPorId(idClientePruebas, entidadInicial.prohibicionesDePaquete.first().id)
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
