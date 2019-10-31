package co.smartobjects.persistencia.fondos

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.fondos.*
import co.smartobjects.entidades.fondos.precios.Impuesto
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.entidades.ubicaciones.Ubicacion
import co.smartobjects.persistencia.*
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorio
import co.smartobjects.persistencia.excepciones.EntidadNoExiste
import co.smartobjects.persistencia.excepciones.ErrorDeLlaveForanea
import co.smartobjects.persistencia.excepciones.EsquemaNoExiste
import co.smartobjects.persistencia.fondos.acceso.RepositorioAccesos
import co.smartobjects.persistencia.fondos.acceso.RepositorioAccesosSQL
import co.smartobjects.persistencia.fondos.acceso.RepositorioEntradas
import co.smartobjects.persistencia.fondos.acceso.RepositorioEntradasSQL
import co.smartobjects.persistencia.fondos.categoriaskus.RepositorioCategoriasSkus
import co.smartobjects.persistencia.fondos.categoriaskus.RepositorioCategoriasSkusSQL
import co.smartobjects.persistencia.fondos.monedas.RepositorioMonedas
import co.smartobjects.persistencia.fondos.monedas.RepositorioMonedasSQL
import co.smartobjects.persistencia.fondos.precios.impuestos.RepositorioImpuestos
import co.smartobjects.persistencia.fondos.precios.impuestos.RepositorioImpuestosSQL
import co.smartobjects.persistencia.fondos.skus.RepositorioSkus
import co.smartobjects.persistencia.fondos.skus.RepositorioSkusSQL
import co.smartobjects.persistencia.ubicaciones.RepositorioUbicaciones
import co.smartobjects.persistencia.ubicaciones.RepositorioUbicacionesSQL
import co.smartobjects.utilidades.Decimal
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DisplayName("FondoDAO")
internal class FondoDAOPruebas : EntidadDAOBasePruebas()
{
    private val repositorioImpuestos: RepositorioImpuestos by lazy { RepositorioImpuestosSQL(configuracionRepositorios) }
    private val repositorioFondos: RepositorioFondos by lazy { RepositorioFondosSQL(configuracionRepositorios) }
    private val repositorioMonedas: RepositorioMonedas by lazy { RepositorioMonedasSQL(configuracionRepositorios) }
    private val repositorioCategoriasSkus: RepositorioCategoriasSkus by lazy { RepositorioCategoriasSkusSQL(configuracionRepositorios) }
    private val repositorioSkus: RepositorioSkus by lazy { RepositorioSkusSQL(configuracionRepositorios) }
    private val repositorioAccesos: RepositorioAccesos by lazy { RepositorioAccesosSQL(configuracionRepositorios) }
    private val repositorioEntradas: RepositorioEntradas by lazy { RepositorioEntradasSQL(configuracionRepositorios) }
    private val repositorioUbicaciones: RepositorioUbicaciones by lazy { RepositorioUbicacionesSQL(configuracionRepositorios) }
    override val creadoresRepositoriosUsados: List<CreadorRepositorio<*>> by lazy {
        listOf<CreadorRepositorio<*>>(
                repositorioImpuestos, repositorioUbicaciones, repositorioFondos
                                     )
    }

    private lateinit var impuestoPrueba: Impuesto
    private lateinit var ubicacionPrueba: Ubicacion
    private lateinit var categoriaPrueba: CategoriaSku
    private lateinit var skuPrueba: Sku
    private lateinit var accesoPrueba: Acceso
    private lateinit var entradaPrueba: Entrada
    private lateinit var dineroPrueba: Dinero

    // El orden importa para las pruebas de delete, se eliminan en este orden. Para que sea exitosa debe ponerse sku antes de categoria
    private val listaFondos by lazy { listOf(skuPrueba, categoriaPrueba, accesoPrueba, entradaPrueba, dineroPrueba) }

    @BeforeEach
    fun antesDeCadaTest()
    {
        impuestoPrueba = repositorioImpuestos.crear(idClientePruebas, impuestoPruebasPorDefecto)
    }

    private fun crearEntidadesDePrueba()
    {
        ubicacionPrueba =
                repositorioUbicaciones.crear(
                        idClientePruebas,
                        Ubicacion(
                                idClientePruebas,
                                null,
                                "Ubicacion abuela",
                                Ubicacion.Tipo.PROPIEDAD,
                                Ubicacion.Subtipo.POS,
                                null,
                                linkedSetOf()
                                 ))

        categoriaPrueba =
                repositorioCategoriasSkus.crear(
                        idClientePruebas,
                        CategoriaSku(
                                idClientePruebas,
                                null,
                                "Entidad prueba",
                                true,
                                false,
                                false,
                                Precio(Decimal.UNO, impuestoPrueba.id!!),
                                "el código externo de prueba categoría",
                                null,
                                LinkedHashSet(),
                                null
                                    ))

        skuPrueba =
                repositorioSkus.crear(
                        idClientePruebas,
                        Sku(
                                idClientePruebas,
                                null,
                                "Entidad prueba",
                                true,
                                false,
                                false,
                                Precio(Decimal.UNO, impuestoPrueba.id!!),
                                "el código externo de prueba sku",
                                categoriaPrueba.id!!,
                                null
                           ))

        accesoPrueba =
                repositorioAccesos.crear(
                        idClientePruebas,
                        Acceso(
                                idClientePruebas,
                                null,
                                "Entidad prueba",
                                true,
                                false,
                                false,
                                Precio(Decimal.UNO, impuestoPrueba.id!!),
                                "el código externo de prueba acceso",
                                ubicacionPrueba.id!!
                              ))

        entradaPrueba =
                repositorioEntradas.crear(
                        idClientePruebas,
                        Entrada(
                                idClientePruebas,
                                null,
                                "Entidad prueba",
                                false,
                                false,
                                Precio(Decimal.UNO, impuestoPrueba.id!!),
                                "el código externo de prueba entrada",
                                ubicacionPrueba.id!!
                               ))

        dineroPrueba =
                repositorioMonedas.crear(
                        idClientePruebas,
                        Dinero(
                                idClientePruebas,
                                null,
                                "Entidad prueba",
                                true,
                                false,
                                false,
                                Precio(Decimal.UNO, impuestoPrueba.id!!),
                                "el código externo de prueba dinero"
                              ))
    }

    @Nested
    inner class Consultar
    {
        @TestConMultiplesDAO
        fun consultar_por_id_existente_retorna_entidad_correcta()
        {
            crearEntidadesDePrueba()
            listaFondos.forEach {
                val entidadConsultada = repositorioFondos.buscarPorId(idClientePruebas, it.id!!)
                assertEquals(it, entidadConsultada)
            }
        }

        @TestConMultiplesDAO
        fun consultar_por_id_existente_pero_con_metodo_incorecto_retorna_null()
        {
            crearEntidadesDePrueba()
            listaFondos.forEach {
                if (it !is Sku)
                {
                    assertNull(repositorioSkus.buscarPorId(idClientePruebas, it.id!!))
                }
                if (it !is CategoriaSku)
                {
                    assertNull(repositorioCategoriasSkus.buscarPorId(idClientePruebas, it.id!!))
                }
                if (it !is Acceso)
                {
                    assertNull(repositorioAccesos.buscarPorId(idClientePruebas, it.id!!))
                }
                if (it !is Entrada)
                {
                    assertNull(repositorioEntradas.buscarPorId(idClientePruebas, it.id!!))
                }
                if (it !is Dinero)
                {
                    assertNull(repositorioMonedas.buscarPorId(idClientePruebas, it.id!!))
                }
            }
        }

        @TestConMultiplesDAO
        fun consultar_listado_de_entidades_sin_entidades_retorna_lista_vacia()
        {
            val listadoConsultado = repositorioFondos.listar(idClientePruebas)

            assertTrue(listadoConsultado.none())
        }

        @TestConMultiplesDAO
        fun consultar_listado_de_entidades_retorna_entidades_correctas()
        {
            crearEntidadesDePrueba()
            val listadoEsperado = listaFondos.sortedBy { it.id!! }

            val listadoConsultado = repositorioFondos.listar(idClientePruebas).toList()

            assertEquals(listadoEsperado, listadoConsultado.sortedBy { it.id!! })
        }

        @TestConMultiplesDAO
        fun consultar_listado_de_entidades_por_fondos_y_luego_metodos_especificos_retorna_entidades_correctas()
        {
            crearEntidadesDePrueba()
            val listadoEsperado = listaFondos.sortedBy { it.id!! }
            val listadoConsultado = repositorioFondos.listar(idClientePruebas).toList()
            assertEquals(listadoEsperado, listadoConsultado.sortedBy { it.id!! })
            assertEquals(listOf(categoriaPrueba), repositorioCategoriasSkus.listar(idClientePruebas).toList())
            assertEquals(listOf(skuPrueba), repositorioSkus.listar(idClientePruebas).toList())
            assertEquals(listOf(accesoPrueba), repositorioAccesos.listar(idClientePruebas).toList())
            assertEquals(listOf(entradaPrueba), repositorioEntradas.listar(idClientePruebas).toList())
            assertEquals(listOf(dineroPrueba), repositorioMonedas.listar(idClientePruebas).toList())
        }

        @TestConMultiplesDAO
        fun consultar_por_id_no_existente_retorna_null()
        {
            val entidadConsultada = repositorioFondos.buscarPorId(idClientePruebas, 789456789)

            assertNull(entidadConsultada)
        }

        @TestConMultiplesDAO
        fun por_id_existente_en_otro_cliente_retorna_null()
        {
            crearEntidadesDePrueba()

            ejecutarConClienteAlternativo { cliente ->
                listaFondos.forEach {
                    val entidadConsultada = repositorioFondos.buscarPorId(cliente.id!!, it.id!!)
                    assertNull(entidadConsultada)
                }
            }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_EsquemaNoExiste_si_se_consulta_por_id_con_id_cliente_inexistente()
        {
            assertThrows<EsquemaNoExiste> { repositorioFondos.buscarPorId(idClientePruebas + 100, 789456789) }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_EsquemaNoExiste_si_se_consulta_listado_de_entidades_con_id_cliente_inexistente()
        {
            assertThrows<EsquemaNoExiste> { repositorioFondos.listar(idClientePruebas + 100) }
        }

        @TestConMultiplesDAO
        fun listado_de_entidades_con_entidades_en_otro_cliente_retorna_lista_vacia()
        {
            crearEntidadesDePrueba()

            ejecutarConClienteAlternativo {
                val listadoConsultado = repositorioFondos.listar(it.id!!)
                assertTrue(listadoConsultado.none())
            }
        }
    }

    @Nested
    inner class ActualizarParcialmente
    {
        private lateinit var entidadDePrueba: Fondo<*>

        @BeforeEach
        fun crearFondoCualquieraDePrueba()
        {
            entidadDePrueba = repositorioMonedas.crear(
                    idClientePruebas,
                    Dinero(
                            idClientePruebas,
                            null,
                            "Dinero para pruebas",
                            true,
                            false,
                            false,
                            Precio(Decimal.UNO, impuestoPrueba.id!!),
                            "el código externo de prueba dinero"
                          ))
        }

        @TestConMultiplesDAO
        fun solo_se_actualizan_los_campos_necesarios()
        {
            val nuevoValor = Fondo.CampoDisponibleParaLaVenta<Dinero>(!entidadDePrueba.disponibleParaLaVenta)
            repositorioFondos.actualizarCamposIndividuales(
                    idClientePruebas,
                    entidadDePrueba.id!!,
                    mapOf<String, CampoModificable<Fondo<*>, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                          )

            val entidadConsultada = repositorioFondos.buscarPorId(idClientePruebas, entidadDePrueba.id!!)
            val entidadEsperada =
                    Dinero(
                            entidadDePrueba.idCliente,
                            entidadDePrueba.id,
                            entidadDePrueba.nombre,
                            !entidadDePrueba.disponibleParaLaVenta,
                            entidadDePrueba.debeAparecerSoloUnaVez,
                            entidadDePrueba.esIlimitado,
                            entidadDePrueba.precioPorDefecto,
                            entidadDePrueba.codigoExterno
                          )

            assertEquals(entidadEsperada, entidadConsultada)
        }

        @TestConMultiplesDAO
        fun no_se_permite_actualizar_la_entidad_fondo_con_el_metodo_incorrecto()
        {
            repositorioMonedas.eliminarPorId(idClientePruebas, entidadDePrueba.id!!)
            crearEntidadesDePrueba()

            listaFondos.forEach {
                if (it !is Sku)
                {
                    val nuevoValor = Fondo.CampoDisponibleParaLaVenta<Sku>(!entidadDePrueba.disponibleParaLaVenta)
                    assertThrows<EntidadNoExiste> {
                        repositorioSkus.actualizarCamposIndividuales(
                                idClientePruebas,
                                it.id!!,
                                mapOf<String, CampoModificable<Sku, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                                    )
                    }
                }
                if (it !is CategoriaSku)
                {
                    val nuevoValor = Fondo.CampoDisponibleParaLaVenta<CategoriaSku>(!entidadDePrueba.disponibleParaLaVenta)
                    assertThrows<EntidadNoExiste> {
                        repositorioCategoriasSkus.actualizarCamposIndividuales(
                                idClientePruebas,
                                it.id!!,
                                mapOf<String, CampoModificable<CategoriaSku, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                                              )
                    }
                }
                if (it !is Acceso)
                {
                    val nuevoValor = Fondo.CampoDisponibleParaLaVenta<Acceso>(!entidadDePrueba.disponibleParaLaVenta)
                    assertThrows<EntidadNoExiste> {
                        repositorioAccesos.actualizarCamposIndividuales(
                                idClientePruebas,
                                it.id!!,
                                mapOf<String, CampoModificable<Acceso, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                                       )
                    }
                }
                if (it !is Entrada)
                {
                    val nuevoValor = Fondo.CampoDisponibleParaLaVenta<Entrada>(!entidadDePrueba.disponibleParaLaVenta)
                    assertThrows<EntidadNoExiste> {
                        repositorioEntradas.actualizarCamposIndividuales(
                                idClientePruebas,
                                it.id!!,
                                mapOf<String, CampoModificable<Entrada, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                                        )
                    }
                }
                if (it !is Dinero)
                {
                    val nuevoValor = Fondo.CampoDisponibleParaLaVenta<Dinero>(!entidadDePrueba.disponibleParaLaVenta)
                    assertThrows<EntidadNoExiste> {
                        repositorioMonedas.actualizarCamposIndividuales(
                                idClientePruebas,
                                it.id!!,
                                mapOf<String, CampoModificable<Dinero, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                                       )
                    }
                }
            }

            val listadoEsperado = listaFondos.sortedBy { it.id!! }
            val listadoConsultado = repositorioFondos.listar(idClientePruebas).toList().sortedBy { it.id!! }

            assertEquals(listadoEsperado, listadoConsultado)
        }

        @TestConMultiplesDAO
        fun con_id_inexistente_lanza_excepcion_EntidadNoExiste()
        {
            val nuevoValor = Fondo.CampoDisponibleParaLaVenta<Dinero>(!entidadDePrueba.disponibleParaLaVenta)
            assertThrows<EntidadNoExiste> {
                repositorioFondos.actualizarCamposIndividuales(
                        idClientePruebas,
                        entidadDePrueba.id!! + 789456,
                        mapOf<String, CampoModificable<Fondo<*>, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                              )
            }
        }

        @TestConMultiplesDAO
        fun con_id_existente_en_otro_cliente_lanza_excepcion_EntidadNoExiste()
        {
            val nuevoValor = Fondo.CampoDisponibleParaLaVenta<Dinero>(!entidadDePrueba.disponibleParaLaVenta)
            ejecutarConClienteAlternativo {
                assertThrows<EntidadNoExiste> {
                    repositorioFondos.actualizarCamposIndividuales(
                            it.id!!,
                            entidadDePrueba.id!! + 789456,
                            mapOf<String, CampoModificable<Fondo<*>, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                                  )
                }
            }
        }

        @TestConMultiplesDAO
        fun con_id_cliente_inexistente_lanza_excepcion_EsquemaNoExiste()
        {
            val nuevoValor = Fondo.CampoDisponibleParaLaVenta<Dinero>(!entidadDePrueba.disponibleParaLaVenta)
            assertThrows<EsquemaNoExiste> {
                repositorioFondos.actualizarCamposIndividuales(
                        idClientePruebas + 100,
                        entidadDePrueba.id!! + 789456,
                        mapOf<String, CampoModificable<Fondo<*>, *>>(nuevoValor.nombreCampo to nuevoValor)
                                                              )
            }
        }
    }

    @Nested
    inner class Eliminar
    {
        @BeforeEach
        fun crearEntidadDePrueba()
        {
            crearEntidadesDePrueba()
        }

        @TestConMultiplesDAO
        fun si_existe_la_entidad_se_elimina_correctamente()
        {
            listaFondos.forEach {
                val resultadoEliminacion = repositorioFondos.eliminarPorId(idClientePruebas, it.id!!)
                val entidadConsultada = repositorioFondos.buscarPorId(idClientePruebas, it.id!!)
                assertTrue(resultadoEliminacion)
                assertNull(entidadConsultada)
            }

            val listadoConsultado = repositorioFondos.listar(idClientePruebas)

            assertTrue(listadoConsultado.none())
            assertFalse(existeAlgunFondoDAO(idClientePruebas, configuracionRepositorios))
            assertFalse(existeAlgunAccesoDAO(idClientePruebas, configuracionRepositorios))
            assertFalse(existeAlgunSkuDAO(idClientePruebas, configuracionRepositorios))
            assertFalse(existeAlgunaCategoriaSkuDAO(idClientePruebas, configuracionRepositorios))
        }

        @TestConMultiplesDAO
        fun no_se_permite_eliminar_la_entidad_fondo_con_el_metodo_incorrecto()
        {
            listaFondos.forEach {
                if (it !is Sku)
                {
                    assertFalse(repositorioSkus.eliminarPorId(idClientePruebas, it.id!!))
                }
                if (it !is CategoriaSku)
                {
                    assertFalse(repositorioCategoriasSkus.eliminarPorId(idClientePruebas, it.id!!))
                }
                if (it !is Acceso)
                {
                    assertFalse(repositorioAccesos.eliminarPorId(idClientePruebas, it.id!!))
                }
                if (it !is Entrada)
                {
                    assertFalse(repositorioEntradas.eliminarPorId(idClientePruebas, it.id!!))
                }
                if (it !is Dinero)
                {
                    assertFalse(repositorioMonedas.eliminarPorId(idClientePruebas, it.id!!))
                }
            }
            val listadoEsperado = listaFondos.sortedBy { it.id!! }
            val listadoConsultado = repositorioFondos.listar(idClientePruebas).toList()
            assertEquals(listadoEsperado, listadoConsultado.sortedBy { it.id!! })
        }

        @TestConMultiplesDAO
        fun si_no_existe_la_entidad_retorna_false_y_no_elimina_entidades_existentes()
        {
            val resultadoEliminacion = repositorioFondos.eliminarPorId(idClientePruebas, listaFondos.map { it.id!! }.max()!! + 1)

            val listadoEsperado = listaFondos.sortedBy { it.id!! }
            val listadoConsultado = repositorioFondos.listar(idClientePruebas).toList()

            assertFalse(resultadoEliminacion)
            assertEquals(listadoEsperado, listadoConsultado.sortedBy { it.id!! })
        }

        @TestConMultiplesDAO
        fun con_id_existente_en_otro_cliente_retorna_false_y_no_elimina_entidad_existente()
        {
            ejecutarConClienteAlternativo { cliente ->
                listaFondos.forEach {
                    val resultadoEliminacion = repositorioFondos.eliminarPorId(cliente.id!!, it.id!!)
                    kotlin.test.assertFalse(resultadoEliminacion)
                }
            }
            listaFondos.forEach {
                val entidadConsultada = repositorioFondos.buscarPorId(idClientePruebas, it.id!!)
                assertEquals(it, entidadConsultada)
            }
        }

        @TestConMultiplesDAO
        fun con_id_cliente_inexistente_lanza_excepcion_EsquemaNoExiste()
        {
            listaFondos.forEach {
                assertThrows<EsquemaNoExiste> { repositorioFondos.eliminarPorId(idClientePruebas + 100, it.id!!) }
            }
        }

        @TestConMultiplesDAO
        fun si_se_intenta_borrar_la_categoria_del_sku_lanza_excepcion_ErrorDeLlaveForanea_y_no_elimina_entidad()
        {
            assertThrows<ErrorDeLlaveForanea> {
                repositorioFondos.eliminarPorId(idClientePruebas, categoriaPrueba.id!!)
            }
            val entidadConsultada = repositorioFondos.buscarPorId(idClientePruebas, categoriaPrueba.id!!)
            assertEquals(categoriaPrueba, entidadConsultada)
        }

        @TestConMultiplesDAO
        fun si_se_intenta_borrar_la_categoria_padre_de_otra_categoria_lanza_excepcion_ErrorDeLlaveForanea_y_no_elimina_entidad()
        {
            repositorioCategoriasSkus.crear(idClientePruebas,
                                            CategoriaSku(
                                                    idClientePruebas,
                                                    null,
                                                    "Entidad prueba hija",
                                                    true,
                                                    false,
                                                    false,
                                                    Precio(Decimal.DIEZ, impuestoPrueba.id!!),
                                                    "el código externo de prueba categoría",
                                                    categoriaPrueba.id,
                                                    LinkedHashSet(),
                                                    null
                                                        ))
            repositorioFondos.eliminarPorId(idClientePruebas, skuPrueba.id!!)
            assertThrows<ErrorDeLlaveForanea> {
                repositorioFondos.eliminarPorId(idClientePruebas, categoriaPrueba.id!!)
            }
            val entidadConsultada = repositorioFondos.buscarPorId(idClientePruebas, categoriaPrueba.id!!)
            assertEquals(categoriaPrueba, entidadConsultada)
        }
    }
}