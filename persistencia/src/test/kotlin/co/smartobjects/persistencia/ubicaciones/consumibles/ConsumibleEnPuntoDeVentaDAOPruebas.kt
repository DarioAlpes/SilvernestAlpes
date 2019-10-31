package co.smartobjects.persistencia.ubicaciones.consumibles

import co.smartobjects.entidades.fondos.*
import co.smartobjects.entidades.fondos.precios.Impuesto
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.entidades.ubicaciones.Ubicacion
import co.smartobjects.entidades.ubicaciones.consumibles.ConsumibleEnPuntoDeVenta
import co.smartobjects.persistencia.EntidadDAOBasePruebas
import co.smartobjects.persistencia.TestConMultiplesDAO
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorio
import co.smartobjects.persistencia.excepciones.ErrorDeLlaveForanea
import co.smartobjects.persistencia.excepciones.EsquemaNoExiste
import co.smartobjects.persistencia.fondos.acceso.RepositorioAccesos
import co.smartobjects.persistencia.fondos.acceso.RepositorioAccesosSQL
import co.smartobjects.persistencia.fondos.acceso.RepositorioEntradas
import co.smartobjects.persistencia.fondos.acceso.RepositorioEntradasSQL
import co.smartobjects.persistencia.fondos.categoriaskus.RepositorioCategoriasSkus
import co.smartobjects.persistencia.fondos.categoriaskus.RepositorioCategoriasSkusSQL
import co.smartobjects.persistencia.fondos.impuestoPruebasPorDefecto
import co.smartobjects.persistencia.fondos.monedas.RepositorioMonedas
import co.smartobjects.persistencia.fondos.monedas.RepositorioMonedasSQL
import co.smartobjects.persistencia.fondos.precios.impuestos.RepositorioImpuestos
import co.smartobjects.persistencia.fondos.precios.impuestos.RepositorioImpuestosSQL
import co.smartobjects.persistencia.fondos.skus.RepositorioSkus
import co.smartobjects.persistencia.fondos.skus.RepositorioSkusSQL
import co.smartobjects.persistencia.ubicaciones.RepositorioUbicaciones
import co.smartobjects.persistencia.ubicaciones.RepositorioUbicacionesSQL
import co.smartobjects.utilidades.Decimal
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DisplayName("ConsumibleEnPuntoDeVentaDAO")
internal class ConsumibleEnPuntoDeVentaDAOPruebas : EntidadDAOBasePruebas()
{
    private val repositorioMonedas: RepositorioMonedas by lazy { RepositorioMonedasSQL(configuracionRepositorios) }
    private val repositorioCategoriasSkus: RepositorioCategoriasSkus by lazy { RepositorioCategoriasSkusSQL(configuracionRepositorios) }
    private val repositorioSkus: RepositorioSkus by lazy { RepositorioSkusSQL(configuracionRepositorios) }
    private val repositorioAccesos: RepositorioAccesos by lazy { RepositorioAccesosSQL(configuracionRepositorios) }
    private val repositorioEntradas: RepositorioEntradas by lazy { RepositorioEntradasSQL(configuracionRepositorios) }
    private val repositorioUbicaciones: RepositorioUbicaciones by lazy { RepositorioUbicacionesSQL(configuracionRepositorios) }
    private val repositorioImpuestos: RepositorioImpuestos by lazy { RepositorioImpuestosSQL(configuracionRepositorios) }
    private val repositorio: RepositorioConsumibleEnPuntoDeVenta by lazy { RepositorioConsumibleEnPuntoDeVentaSQL(configuracionRepositorios) }
    private val repositorioFondosEnPuntoDeVenta: RepositorioFondosEnPuntoDeVenta by lazy { RepositorioFondosEnPuntoDeVentaSQL(configuracionRepositorios) }

    override val creadoresRepositoriosUsados: List<CreadorRepositorio<*>> by lazy {
        listOf<CreadorRepositorio<*>>(
                repositorioImpuestos,
                repositorioUbicaciones,
                repositorioMonedas,
                repositorioAccesos,
                repositorioEntradas,
                repositorioCategoriasSkus,
                repositorioSkus,
                repositorio
                                     )
    }
    private val impuestoCreado: Impuesto by lazy { repositorioImpuestos.crear(idClientePruebas, impuestoPruebasPorDefecto) }

    private val ubicacionCreada: Ubicacion by lazy {
        repositorioUbicaciones.crear(idClientePruebas, Ubicacion(
                idClientePruebas,
                null,
                "Ubicacion madre",
                Ubicacion.Tipo.PROPIEDAD,
                Ubicacion.Subtipo.POS,
                null,
                linkedSetOf()
                                                                )
                                    )
    }

    private val dineroCreado: Dinero by lazy {
        repositorioMonedas.crear(idClientePruebas, Dinero(
                idClientePruebas,
                null,
                "Dinero pruebas",
                true,
                false,
                false,
                Precio(Decimal.UNO, impuestoCreado.id!!),
                "el código externo de prueba dinero"
                                                         ))
    }

    private val accesoCreado: Acceso by lazy {
        repositorioAccesos.crear(idClientePruebas, Acceso(
                idClientePruebas,
                null,
                "Acceso",
                true,
                true,
                true,
                Precio(Decimal.UNO, impuestoCreado.id!!),
                "el código externo de prueba acceso",
                ubicacionCreada.id!!
                                                         ))
    }

    private val entradaCreada: Entrada by lazy {
        repositorioEntradas.crear(idClientePruebas, Entrada(
                idClientePruebas,
                null,
                "Entrada",
                true,
                true,
                Precio(Decimal.UNO, impuestoCreado.id!!),
                "el código externo de prueba entrada",
                ubicacionCreada.id!!
                                                           ))
    }

    private val categoriaSkuCreada: CategoriaSku by lazy {
        repositorioCategoriasSkus.crear(idClientePruebas, CategoriaSku(
                idClientePruebas,
                null,
                "Categoria prueba",
                true,
                false,
                false,
                Precio(Decimal.UNO, impuestoCreado.id!!),
                "el código externo de prueba categoria sku",
                null,
                LinkedHashSet(),
                null
                                                                      ))
    }

    private val skuCreado: Sku by lazy {
        repositorioSkus.crear(idClientePruebas, Sku(
                idClientePruebas,
                null,
                "Categoria prueba",
                true,
                false,
                false,
                Precio(Decimal.UNO, impuestoCreado.id!!),
                "el código externo de prueba sku",
                categoriaSkuCreada.id!!,
                null
                                                   ))
    }

    private fun darInstanciaEntidadValida(): ListaConsumiblesEnPuntoDeVentaUbicaciones
    {
        return ListaConsumiblesEnPuntoDeVentaUbicaciones(
                setOf
                (
                        ConsumibleEnPuntoDeVenta(ubicacionCreada.id!!, skuCreado.id!!, "código externo fondo ${skuCreado.id!!}"),
                        ConsumibleEnPuntoDeVenta(ubicacionCreada.id!!, entradaCreada.id!!, "código externo fondo ${entradaCreada.id!!}"),
                        ConsumibleEnPuntoDeVenta(ubicacionCreada.id!!, accesoCreado.id!!, "código externo fondo ${accesoCreado.id!!}")
                ),
                ubicacionCreada.id!!

                                                        )
    }

    @Nested
    inner class Crear
    {
        @TestConMultiplesDAO
        fun retorna_misma_entidad_creada()
        {
            val entidadAInsertar = darInstanciaEntidadValida()
            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertEquals(entidadAInsertar.consumiblesEnPuntoDeVentaUbicaciones, entidadCreada.toSet())
        }

        @TestConMultiplesDAO
        fun reemplaza_los_consumibles_de_la_ubicacion_cuando_se_llama_nuevamente()
        {
            val entidadAInsertar = setOf(ConsumibleEnPuntoDeVenta(ubicacionCreada.id!!, accesoCreado.id!!, "código externo fondo ${accesoCreado.id!!}"))
            val entidadCreada = repositorio.crear(idClientePruebas, ListaConsumiblesEnPuntoDeVentaUbicaciones(entidadAInsertar, ubicacionCreada.id!!))
            assertEquals(entidadAInsertar, entidadCreada.toSet())

            val entidadParaReemplazar = setOf(ConsumibleEnPuntoDeVenta(ubicacionCreada.id!!, skuCreado.id!!, "código externo fondo ${skuCreado.id!!}"))
            val entidadCreadaReemplazada = repositorio.crear(idClientePruebas, ListaConsumiblesEnPuntoDeVentaUbicaciones(entidadParaReemplazar, ubicacionCreada.id!!))
            assertEquals(entidadParaReemplazar, entidadCreadaReemplazada.toSet())

            assertEquals(entidadParaReemplazar, repositorio.listarSegunParametros(idClientePruebas, IdUbicacionConsultaConsumibles(ubicacionCreada.id!!)).toSet())
        }

        @TestConMultiplesDAO
        fun funciona_con_set_vacio_y_elimina_todos_los_consumibles_de_la_ubicacion()
        {
            val entidadAInsertar = darInstanciaEntidadValida()
            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)
            assertEquals(entidadAInsertar.consumiblesEnPuntoDeVentaUbicaciones, entidadCreada.toSet())

            val entidadParaReemplazar = setOf<ConsumibleEnPuntoDeVenta>()
            val entidadCreadaReemplazada = repositorio.crear(idClientePruebas, ListaConsumiblesEnPuntoDeVentaUbicaciones(entidadParaReemplazar, ubicacionCreada.id!!))
            assertEquals(entidadParaReemplazar, entidadCreadaReemplazada.toSet())

            assertEquals(entidadParaReemplazar, repositorio.listarSegunParametros(idClientePruebas, IdUbicacionConsultaConsumibles(ubicacionCreada.id!!)).toSet())
        }

        @TestConMultiplesDAO
        fun funciona_con_lista_vacia_y_no_elimina_los_consumibles_de_otra_ubicacion()
        {
            val otraUbicacion = repositorioUbicaciones.crear(idClientePruebas, Ubicacion(
                    idClientePruebas,
                    null,
                    "Otra Ubicacion",
                    Ubicacion.Tipo.PROPIEDAD,
                    Ubicacion.Subtipo.POS,
                    null,
                    linkedSetOf()
                                                                                        )
                                                            )
            val entidadAInsertar = darInstanciaEntidadValida()
            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)
            assertEquals(entidadAInsertar.consumiblesEnPuntoDeVentaUbicaciones, entidadCreada.toSet())

            val entidadParaReemplazar = setOf<ConsumibleEnPuntoDeVenta>()
            val entidadCreadaReemplazada = repositorio.crear(idClientePruebas, ListaConsumiblesEnPuntoDeVentaUbicaciones(entidadParaReemplazar, otraUbicacion.id!!))
            assertEquals(entidadParaReemplazar, entidadCreadaReemplazada.toSet())

            assertEquals(entidadParaReemplazar, repositorio.listarSegunParametros(idClientePruebas, IdUbicacionConsultaConsumibles(otraUbicacion.id!!)).toSet())
            assertEquals(entidadAInsertar.consumiblesEnPuntoDeVentaUbicaciones, repositorio.listarSegunParametros(idClientePruebas, IdUbicacionConsultaConsumibles(ubicacionCreada.id!!)).toSet())
        }

        @TestConMultiplesDAO
        fun usa_id_ubicacion_global_si_id_ubicacion_de_entidades_no_coincide_con_el_parametro()
        {
            val otraUbicacion = repositorioUbicaciones.crear(idClientePruebas, Ubicacion(
                    idClientePruebas,
                    null,
                    "Otra Ubicacion",
                    Ubicacion.Tipo.PROPIEDAD,
                    Ubicacion.Subtipo.POS,
                    null,
                    linkedSetOf()
                                                                                        )
                                                            )
            val entidadAInsertar = darInstanciaEntidadValida().copy(idUbicacion = otraUbicacion.id!!)
            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)
            val entidadEsperada = entidadAInsertar.consumiblesEnPuntoDeVentaUbicaciones.map { it.copiar(idUbicacion = otraUbicacion.id!!) }.toSet()
            assertEquals(entidadEsperada, entidadCreada.toSet())

            assertEquals(entidadEsperada, repositorio.listarSegunParametros(idClientePruebas, IdUbicacionConsultaConsumibles(otraUbicacion.id!!)).toSet())
            assertEquals(setOf(), repositorio.listarSegunParametros(idClientePruebas, IdUbicacionConsultaConsumibles(ubicacionCreada.id!!)).toSet())
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_ErrorDeLlaveForanea_si_usa_una_ubicacion_inexistente()
        {
            val entidadAInsertar = darInstanciaEntidadValida().copy(idUbicacion = 2345345645)

            assertThrows<ErrorDeLlaveForanea> {
                repositorio.crear(idClientePruebas, entidadAInsertar)
            }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_ErrorCreacionViolacionDeRestriccion_si_usa_una_consumible_inexistente()
        {
            val entidadAInsertar = ConsumibleEnPuntoDeVenta(ubicacionCreada.id!!, 2345345645, "código externo fondo")

            assertThrows<ErrorDeLlaveForanea> {
                repositorio.crear(idClientePruebas, ListaConsumiblesEnPuntoDeVentaUbicaciones(setOf(entidadAInsertar), ubicacionCreada.id!!))
            }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_ErrorDeLlaveForanea_si_usa_un_fondo_dinero_existente()
        {
            val entidadAInsertar = ConsumibleEnPuntoDeVenta(ubicacionCreada.id!!, dineroCreado.id!!, "código externo fondo ${dineroCreado.id!!}")

            assertThrows<ErrorDeLlaveForanea> {
                repositorio.crear(idClientePruebas, ListaConsumiblesEnPuntoDeVentaUbicaciones(setOf(entidadAInsertar), ubicacionCreada.id!!))
            }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_ErrorDeLlaveForanea_si_usa_un_fondo_dinero_existente_con_otros_consumibles_validos()
        {
            val entidadAInsertar = ConsumibleEnPuntoDeVenta(ubicacionCreada.id!!, dineroCreado.id!!, "código externo fondo ${dineroCreado.id!!}")
            val entidadValida = darInstanciaEntidadValida()

            assertThrows<ErrorDeLlaveForanea> {
                repositorio.crear(idClientePruebas, ListaConsumiblesEnPuntoDeVentaUbicaciones(setOf(entidadAInsertar) + entidadValida.consumiblesEnPuntoDeVentaUbicaciones, ubicacionCreada.id!!))
            }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_ErrorDeLlaveForanea_si_usa_un_fondo_categoria_existente()
        {
            val entidadAInsertar = ConsumibleEnPuntoDeVenta(ubicacionCreada.id!!, categoriaSkuCreada.id!!, "código externo fondo ${categoriaSkuCreada.id!!}")

            assertThrows<ErrorDeLlaveForanea> {
                repositorio.crear(idClientePruebas, ListaConsumiblesEnPuntoDeVentaUbicaciones(setOf(entidadAInsertar), ubicacionCreada.id!!))
            }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_ErrorDeLlaveForanea_si_usa_un_fondo_categoria_existente_con_otros_consumibles_validos()
        {
            val entidadAInsertar = ConsumibleEnPuntoDeVenta(ubicacionCreada.id!!, categoriaSkuCreada.id!!, "código externo fondo ${categoriaSkuCreada.id!!}")
            val entidadValida = darInstanciaEntidadValida()

            assertThrows<ErrorDeLlaveForanea> {
                repositorio.crear(idClientePruebas, ListaConsumiblesEnPuntoDeVentaUbicaciones(setOf(entidadAInsertar) + entidadValida.consumiblesEnPuntoDeVentaUbicaciones, ubicacionCreada.id!!))
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
    inner class ConsultarConsumiblesEnPuntoDeVentaDeUnaUbicacion
    {
        @TestConMultiplesDAO
        fun listado_de_entidades_sin_entidades_retorna_lista_vacia()
        {
            val parametros = IdUbicacionConsultaConsumibles(ubicacionCreada.id!!)
            val listadoConsultado = repositorio.listarSegunParametros(idClientePruebas, parametros)

            assertTrue(listadoConsultado.none())
        }

        @TestConMultiplesDAO
        fun listado_de_entidades_retorna_lista_correcta()
        {
            val parametros = IdUbicacionConsultaConsumibles(ubicacionCreada.id!!)
            val entidadCreada = repositorio.crear(idClientePruebas, darInstanciaEntidadValida())
            val listadoEsperado = entidadCreada.toSet()

            val listadoConsultado = repositorio.listarSegunParametros(idClientePruebas, parametros).toSet()
            assertEquals(listadoEsperado, listadoConsultado)
        }

        @TestConMultiplesDAO
        fun listado_de_entidades_con_entidades_en_otra_ubicacion_retorna_lista_vacia()
        {
            val otraUbicacion = repositorioUbicaciones.crear(idClientePruebas, Ubicacion(
                    idClientePruebas,
                    null,
                    "Otra Ubicacion",
                    Ubicacion.Tipo.PROPIEDAD,
                    Ubicacion.Subtipo.POS,
                    null,
                    linkedSetOf()
                                                                                        )
                                                            )
            val parametros = IdUbicacionConsultaConsumibles(otraUbicacion.id!!)
            repositorio.crear(idClientePruebas, darInstanciaEntidadValida())

            val listadoConsultado = repositorio.listarSegunParametros(idClientePruebas, parametros).toSet()
            assertTrue(listadoConsultado.none())
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_EsquemaNoExiste_si_se_consulta_listado_de_entidades_con_id_cliente_inexistente()
        {
            val parametros = IdUbicacionConsultaConsumibles(ubicacionCreada.id!!)
            assertThrows<EsquemaNoExiste> { repositorio.listarSegunParametros(idClientePruebas + 100, parametros) }
        }

        @TestConMultiplesDAO
        fun listado_de_entidades_con_entidades_en_otro_cliente_retorna_lista_vacia()
        {
            val parametros = IdUbicacionConsultaConsumibles(ubicacionCreada.id!!)
            repositorio.crear(idClientePruebas, darInstanciaEntidadValida())


            ejecutarConClienteAlternativo {
                val listadoConsultado = repositorio.listarSegunParametros(it.id!!, parametros)
                Assertions.assertTrue(listadoConsultado.none())
            }
        }
    }

    @Nested
    inner class ConsultarFondosEnPuntoDeVentaDeUnaUbicacion
    {
        @TestConMultiplesDAO
        fun listado_de_entidades_sin_entidades_retorna_lista_vacia()
        {
            val parametros = IdUbicacionConsultaConsumibles(ubicacionCreada.id!!)
            val listadoConsultado = repositorioFondosEnPuntoDeVenta.listarSegunParametros(idClientePruebas, parametros)

            assertTrue(listadoConsultado.none())
        }

        @TestConMultiplesDAO
        fun listado_de_entidades_con_todos_los_tipos_de_entidades_retorna_lista_correcta()
        {
            val parametros = IdUbicacionConsultaConsumibles(ubicacionCreada.id!!)
            repositorio.crear(idClientePruebas, darInstanciaEntidadValida())
            val listadoEsperado = setOf(skuCreado, accesoCreado, entradaCreada)

            val listadoConsultado = repositorioFondosEnPuntoDeVenta.listarSegunParametros(idClientePruebas, parametros).toSet()
            assertEquals(listadoEsperado, listadoConsultado)
        }

        @TestConMultiplesDAO
        fun listado_de_entidades_con_un_solo_sku_retorna_lista_correcta()
        {
            val parametros = IdUbicacionConsultaConsumibles(ubicacionCreada.id!!)
            repositorio.crear(idClientePruebas, ListaConsumiblesEnPuntoDeVentaUbicaciones(setOf(ConsumibleEnPuntoDeVenta(ubicacionCreada.id!!, skuCreado.id!!, "código externo fondo ${skuCreado.id!!}")), ubicacionCreada.id!!))
            val listadoEsperado = setOf(skuCreado)

            val listadoConsultado = repositorioFondosEnPuntoDeVenta.listarSegunParametros(idClientePruebas, parametros).toSet()
            assertEquals(listadoEsperado, listadoConsultado)
        }

        @TestConMultiplesDAO
        fun listado_de_entidades_con_un_solo_acceso_retorna_lista_correcta()
        {
            val parametros = IdUbicacionConsultaConsumibles(ubicacionCreada.id!!)
            repositorio.crear(idClientePruebas, ListaConsumiblesEnPuntoDeVentaUbicaciones(setOf(ConsumibleEnPuntoDeVenta(ubicacionCreada.id!!, accesoCreado.id!!, "código externo fondo ${accesoCreado.id!!}")), ubicacionCreada.id!!))
            val listadoEsperado = setOf(accesoCreado)

            val listadoConsultado = repositorioFondosEnPuntoDeVenta.listarSegunParametros(idClientePruebas, parametros).toSet()
            assertEquals(listadoEsperado, listadoConsultado)
        }

        @TestConMultiplesDAO
        fun listado_de_entidades_con_una_sola_entrada_retorna_lista_correcta()
        {
            val parametros = IdUbicacionConsultaConsumibles(ubicacionCreada.id!!)
            repositorio.crear(idClientePruebas, ListaConsumiblesEnPuntoDeVentaUbicaciones(setOf(ConsumibleEnPuntoDeVenta(ubicacionCreada.id!!, entradaCreada.id!!, "código externo fondo ${entradaCreada.id!!}")), ubicacionCreada.id!!))
            val listadoEsperado = setOf(entradaCreada)

            val listadoConsultado = repositorioFondosEnPuntoDeVenta.listarSegunParametros(idClientePruebas, parametros).toSet()
            assertEquals(listadoEsperado, listadoConsultado)
        }

        @TestConMultiplesDAO
        fun listado_de_entidades_con_entidades_en_otra_ubicacion_retorna_lista_vacia()
        {
            val otraUbicacion = repositorioUbicaciones.crear(idClientePruebas, Ubicacion(
                    idClientePruebas,
                    null,
                    "Otra Ubicacion",
                    Ubicacion.Tipo.PROPIEDAD,
                    Ubicacion.Subtipo.POS,
                    null,
                    linkedSetOf()
                                                                                        )
                                                            )
            val parametros = IdUbicacionConsultaConsumibles(otraUbicacion.id!!)
            repositorio.crear(idClientePruebas, darInstanciaEntidadValida())

            val listadoConsultado = repositorioFondosEnPuntoDeVenta.listarSegunParametros(idClientePruebas, parametros).toSet()
            assertTrue(listadoConsultado.none())
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_EsquemaNoExiste_si_se_consulta_listado_de_entidades_con_id_cliente_inexistente()
        {
            val parametros = IdUbicacionConsultaConsumibles(ubicacionCreada.id!!)
            assertThrows<EsquemaNoExiste> { repositorioFondosEnPuntoDeVenta.listarSegunParametros(idClientePruebas + 100, parametros) }
        }

        @TestConMultiplesDAO
        fun listado_de_entidades_con_entidades_en_otro_cliente_retorna_lista_vacia()
        {
            val parametros = IdUbicacionConsultaConsumibles(ubicacionCreada.id!!)
            repositorio.crear(idClientePruebas, darInstanciaEntidadValida())


            ejecutarConClienteAlternativo {
                val listadoConsultado = repositorioFondosEnPuntoDeVenta.listarSegunParametros(it.id!!, parametros)
                Assertions.assertTrue(listadoConsultado.none())
            }
        }
    }
}