package co.smartobjects.persistencia.fondos.libros

import co.smartobjects.entidades.fondos.Dinero
import co.smartobjects.entidades.fondos.Paquete
import co.smartobjects.entidades.fondos.libros.*
import co.smartobjects.entidades.fondos.precios.GrupoClientes
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.entidades.ubicaciones.Ubicacion
import co.smartobjects.persistencia.EntidadDAOBasePruebas
import co.smartobjects.persistencia.TestConMultiplesDAO
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorio
import co.smartobjects.persistencia.excepciones.EsquemaNoExiste
import co.smartobjects.persistencia.fondos.grupoClientesCategoriaA
import co.smartobjects.persistencia.fondos.grupoClientesCategoriaB
import co.smartobjects.persistencia.fondos.impuestoPruebasPorDefecto
import co.smartobjects.persistencia.fondos.monedas.RepositorioMonedas
import co.smartobjects.persistencia.fondos.monedas.RepositorioMonedasSQL
import co.smartobjects.persistencia.fondos.paquete.RepositorioPaquetes
import co.smartobjects.persistencia.fondos.paquete.RepositorioPaquetesSQL
import co.smartobjects.persistencia.fondos.precios.gruposclientes.RepositorioGrupoClientes
import co.smartobjects.persistencia.fondos.precios.gruposclientes.RepositorioGrupoClientesSQL
import co.smartobjects.persistencia.fondos.precios.impuestos.RepositorioImpuestos
import co.smartobjects.persistencia.fondos.precios.impuestos.RepositorioImpuestosSQL
import co.smartobjects.persistencia.ubicaciones.RepositorioUbicaciones
import co.smartobjects.persistencia.ubicaciones.RepositorioUbicacionesSQL
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.threeten.bp.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class LibroSegunReglasDAOCompletoPruebas : EntidadDAOBasePruebas()
{
    companion object
    {
        private const val NUMERO_DE_PRECIOS_EN_LIBRO = 5
    }

    private val repositorioImpuestos: RepositorioImpuestos by lazy { RepositorioImpuestosSQL(configuracionRepositorios) }
    private val repositorioUbicaciones: RepositorioUbicaciones by lazy { RepositorioUbicacionesSQL(configuracionRepositorios) }
    private val repositorioGrupoClientes: RepositorioGrupoClientes by lazy { RepositorioGrupoClientesSQL(configuracionRepositorios) }
    private val repositorioMonedas: RepositorioMonedas by lazy { RepositorioMonedasSQL(configuracionRepositorios) }
    private val repositorioPaquetes: RepositorioPaquetes by lazy { RepositorioPaquetesSQL(configuracionRepositorios) }
    private val repositorioLibrosDePrecios: RepositorioLibrosDePrecios by lazy { RepositorioLibrosDePreciosSQL(configuracionRepositorios) }
    private val repositorioLibrosDeProhibiciones: RepositorioLibrosDeProhibiciones by lazy { RepositorioLibrosDeProhibicionesSQL(configuracionRepositorios) }
    private val repositorioLibroSegunReglas: RepositorioLibrosSegunReglas by lazy { RepositorioLibrosSegunReglasSQL(configuracionRepositorios) }
    private val repositorio: RepositorioLibrosSegunReglasCompleto by lazy { RepositorioLibrosSegunReglasCompletoSQL(configuracionRepositorios) }

    override val creadoresRepositoriosUsados: List<CreadorRepositorio<*>> by lazy {
        listOf<CreadorRepositorio<*>>(
                repositorioImpuestos,
                repositorioGrupoClientes,
                repositorioUbicaciones,
                repositorioMonedas,
                repositorioPaquetes,
                repositorioLibrosDePrecios,
                repositorioLibrosDeProhibiciones,
                repositorioLibroSegunReglas,
                repositorio
                                     )
    }

    private lateinit var gruposDeClientes: List<GrupoClientes>
    private lateinit var monedas: List<Dinero>
    private lateinit var libroDePrecios: LibroDePrecios
    private lateinit var libroDeProhibiciones: LibroDeProhibiciones

    @BeforeEach
    fun antesDeCadaTest()
    {
        val impuesto = repositorioImpuestos.crear(idClientePruebas, impuestoPruebasPorDefecto)

        gruposDeClientes = listOf(
                repositorioGrupoClientes.crear(idClientePruebas, grupoClientesCategoriaA),
                repositorioGrupoClientes.crear(idClientePruebas, grupoClientesCategoriaB)
                                 )

        monedas = List(NUMERO_DE_PRECIOS_EN_LIBRO) {
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

        val preciosDeFondos =
                monedas.asSequence().map {
                    PrecioEnLibro(Precio(Decimal(it.id!!.toInt()), impuesto.id!!), it.id!!)
                }.toSet()

        val libroDePreciosACrear = LibroDePrecios(idClientePruebas, null, "Libro de precios en prueba", preciosDeFondos)
        libroDePrecios = repositorioLibrosDePrecios.crear(idClientePruebas, libroDePreciosACrear)

        val paquete = crearPaquete("Paquete de pruebas")
        val libroDeProhibicionesACrear =
                LibroDeProhibiciones(
                        idClientePruebas,
                        null,
                        "Libro de prohibiciones en prueba",
                        setOf(Prohibicion.DeFondo(monedas.first().id!!)),
                        setOf(Prohibicion.DePaquete(paquete.id!!))
                                    )
        libroDeProhibiciones = repositorioLibrosDeProhibiciones.crear(idClientePruebas, libroDeProhibicionesACrear)
    }

    private fun crearUbicacion(nombre: String): Ubicacion
    {
        return repositorioUbicaciones.crear(
                idClientePruebas,
                Ubicacion(
                        idClientePruebas,
                        null,
                        nombre,
                        Ubicacion.Tipo.PROPIEDAD,
                        Ubicacion.Subtipo.POS,
                        null,
                        linkedSetOf()
                         )
                                           )
    }

    private fun crearPaquete(nombre: String): Paquete
    {
        val fechaInicial = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)

        return repositorioPaquetes.crear(
                idClientePruebas,
                Paquete(
                        idClientePruebas,
                        null,
                        nombre,
                        "Descripción",
                        true,
                        fechaInicial,
                        fechaInicial.plusDays(1),
                        monedas.map {
                            Paquete.FondoIncluido(it.id!!, "código externo incluido", Decimal.UNO)
                        },
                        "código externo paquete"
                       )
                                        )
    }

    private fun darInstanciaConLibroDePrecios(
            reglasIdUbicacion: MutableSet<ReglaDeIdUbicacion>? = null,
            reglasIdGrupoDeClientes: MutableSet<ReglaDeIdGrupoDeClientes>? = null,
            reglasIdPaquete: MutableSet<ReglaDeIdPaquete>? = null
                                             ): LibroSegunReglas
    {
        return LibroSegunReglas(
                idClientePruebas,
                null,
                "Libro segun reglas",
                libroDePrecios.id!!,
                reglasIdUbicacion ?: mutableSetOf(),
                reglasIdGrupoDeClientes ?: mutableSetOf(),
                reglasIdPaquete ?: mutableSetOf()
                               )
    }

    private fun darInstanciaConLibroDeProhibiciones(
            reglasIdUbicacion: MutableSet<ReglaDeIdUbicacion>? = null,
            reglasIdGrupoDeClientes: MutableSet<ReglaDeIdGrupoDeClientes>? = null,
            reglasIdPaquete: MutableSet<ReglaDeIdPaquete>? = null
                                                   ): LibroSegunReglas
    {
        return LibroSegunReglas(
                idClientePruebas,
                null,
                "Libro segun reglas",
                libroDeProhibiciones.id!!,
                reglasIdUbicacion ?: mutableSetOf(),
                reglasIdGrupoDeClientes ?: mutableSetOf(),
                reglasIdPaquete ?: mutableSetOf()
                               )
    }

    private fun crearLibroSegunReglasConPreciosDePrueba(i: Int, incluirReglas: Boolean): LibroSegunReglas
    {
        val entidadAInsertar =
                if (incluirReglas)
                {
                    val reglaDeUbicacion = mutableSetOf(ReglaDeIdUbicacion(crearUbicacion("ubicacion $i").id!!))
                    val reglaDeGrupo = mutableSetOf(ReglaDeIdGrupoDeClientes(gruposDeClientes[i].id!!)) // Asume que 0 <= i < gruposDeClientes.size(=2)
                    val reglaDePaquete = mutableSetOf(ReglaDeIdPaquete(crearPaquete("paquete $i").id!!))

                    darInstanciaConLibroDePrecios(reglaDeUbicacion, reglaDeGrupo, reglaDePaquete)
                }
                else
                {
                    darInstanciaConLibroDePrecios()
                }
                    .let { it.copiar(nombre = it.nombre + i) }

        return repositorioLibroSegunReglas.crear(idClientePruebas, entidadAInsertar)
    }

    private fun crearLibroSegunReglasConProhibicionesDePrueba(i: Int, incluirReglas: Boolean): LibroSegunReglas
    {
        val entidadAInsertar =
                if (incluirReglas)
                {
                    val reglaDeUbicacion = mutableSetOf(ReglaDeIdUbicacion(crearUbicacion("ubicacion $i").id!!))
                    val reglaDeGrupo = mutableSetOf(ReglaDeIdGrupoDeClientes(gruposDeClientes[i].id!!)) // Asume que 0 <= i < gruposDeClientes.size(=2)
                    val reglaDePaquete = mutableSetOf(ReglaDeIdPaquete(crearPaquete("paquete $i").id!!))

                    darInstanciaConLibroDeProhibiciones(reglaDeUbicacion, reglaDeGrupo, reglaDePaquete)
                }
                else
                {
                    darInstanciaConLibroDeProhibiciones()
                }
                    .let { it.copiar(nombre = it.nombre + i) }

        return repositorioLibroSegunReglas.crear(idClientePruebas, entidadAInsertar)
    }

    @Nested
    inner class Consultar
    {
        @Nested
        inner class ConLibroDePrecios
        {
            @TestConMultiplesDAO
            fun por_id_existente_retorna_entidad_correcta()
            {
                val entidadEsperada =
                        LibroSegunReglasCompleto(
                                crearLibroSegunReglasConPreciosDePrueba(0, true),
                                libroDePrecios
                                                )

                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadEsperada.id!!)

                assertEquals(entidadEsperada, entidadConsultada)
            }

            @TestConMultiplesDAO
            fun listado_de_entidades_retorna_entidades_correctas()
            {
                val listadoEsperado =
                        sequenceOf(
                                crearLibroSegunReglasConPreciosDePrueba(0, true),
                                crearLibroSegunReglasConPreciosDePrueba(1, true)
                                  )
                            .map {
                                LibroSegunReglasCompleto(it, libroDePrecios)
                            }
                            .sortedBy { it.id!! }.toList()

                val listadoConsultado = repositorio.listar(idClientePruebas).sortedBy { it.id!! }.toList()

                assertEquals(listadoEsperado, listadoConsultado)
            }
        }

        @Nested
        inner class ConLibroDeProhibiciones
        {
            @TestConMultiplesDAO
            fun por_id_existente_retorna_entidad_correcta()
            {
                val entidadEsperada =
                        LibroSegunReglasCompleto(
                                crearLibroSegunReglasConProhibicionesDePrueba(0, true),
                                libroDeProhibiciones
                                                )

                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadEsperada.id!!)

                assertEquals(entidadEsperada, entidadConsultada)
            }

            @TestConMultiplesDAO
            fun listado_de_entidades_retorna_entidades_correctas()
            {
                val listadoEsperado =
                        sequenceOf(
                                crearLibroSegunReglasConProhibicionesDePrueba(0, true),
                                crearLibroSegunReglasConProhibicionesDePrueba(1, true)
                                  )
                            .map {
                                LibroSegunReglasCompleto(it, libroDeProhibiciones)
                            }
                            .sortedBy { it.id!! }.toList()

                val listadoConsultado = repositorio.listar(idClientePruebas).sortedBy { it.id!! }.toList()

                assertEquals(listadoEsperado, listadoConsultado)
            }
        }

        @TestConMultiplesDAO
        fun listado_de_entidades_sin_entidades_retorna_lista_vacia()
        {
            val listadoConsultado = repositorio.listar(idClientePruebas)

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
            val entidadPrueba = crearLibroSegunReglasConPreciosDePrueba(0, true)

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
            crearLibroSegunReglasConPreciosDePrueba(0, true)

            ejecutarConClienteAlternativo {
                val listadoConsultado = repositorio.listar(it.id!!)
                assertTrue(listadoConsultado.none())
            }
        }
    }
}