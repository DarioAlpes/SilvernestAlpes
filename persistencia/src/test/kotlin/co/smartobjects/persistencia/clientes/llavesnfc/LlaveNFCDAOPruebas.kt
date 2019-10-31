package co.smartobjects.persistencia.clientes.llavesnfc

import co.smartobjects.entidades.clientes.Cliente
import co.smartobjects.persistencia.EntidadDAOBasePruebas
import co.smartobjects.persistencia.TestConMultiplesDAO
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorio
import co.smartobjects.persistencia.excepciones.EsquemaNoExiste
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue


@DisplayName("LlaveNFCDAO")
internal class LlaveNFCDAOPruebas : EntidadDAOBasePruebas()
{
    companion object
    {
        const val LLAVE_DE_PRUEBA = "123456789-asfqwer"
    }

    private val repositorio: RepositorioLlavesNFC by lazy { RepositorioLlavesNFCSQL(configuracionRepositorios) }
    override val creadoresRepositoriosUsados: List<CreadorRepositorio<*>> by lazy {
        listOf<CreadorRepositorio<*>>(repositorio)
    }


    private fun darInstanciaEntidadValida(): Cliente.LlaveNFC
    {
        return Cliente.LlaveNFC(idClientePruebas, LLAVE_DE_PRUEBA)
    }

    @Nested
    inner class Crear
    {
        @TestConMultiplesDAO
        fun retorna_entidad_correcta_y_la_retorna_con_la_llave_limpiada()
        {
            val entidadACrear = darInstanciaEntidadValida()
            val entidadCreada = repositorio.crear(idClientePruebas, entidadACrear)

            assertEquals(entidadACrear, entidadCreada)
        }

        @TestConMultiplesDAO
        fun limpia_contraseña_de_entidad_de_entrada()
        {
            val entidadAInsertar = darInstanciaEntidadValida()
            repositorio.crear(idClientePruebas, entidadAInsertar)

            assertTrue(entidadAInsertar.llave.all { it == '\u0000' })
        }

        @TestConMultiplesDAO
        fun ignora_el_id_de_cliente_y_retorna_entidad_correcta_con_id_cliente_correcto_en_datos_y_permisos()
        {
            val entidadConIdsCorrectos = darInstanciaEntidadValida()
            val entidadAInsertar = entidadConIdsCorrectos.copiar(idCliente = 9999)

            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertEquals(entidadConIdsCorrectos, entidadCreada)
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
        @Nested
        inner class ValidaEnFecha
        {
            private lateinit var entidadACrear: Cliente.LlaveNFC
            private lateinit var parametrosEnRangoDeDatos: FiltroLlavesNFC.ValidaEnFecha
            private lateinit var parametrosFueraRangoDeDatos: FiltroLlavesNFC.ValidaEnFecha


            @BeforeEach
            fun prepararPrueba()
            {
                entidadACrear = darInstanciaEntidadValida()
                parametrosEnRangoDeDatos =
                        FiltroLlavesNFC.ValidaEnFecha(entidadACrear.fechaCreacion.plusDays(10))

                parametrosFueraRangoDeDatos =
                        FiltroLlavesNFC.ValidaEnFecha(entidadACrear.fechaCreacion.minusDays(10))
            }


            @TestConMultiplesDAO
            fun para_fecha_de_corte_igual_o_superior_a_cuando_se_creo_el_cliente_retorna_entidad_correcta()
            {
                val entidadEsperada = darInstanciaEntidadValida().copiar(fechaCreacion = entidadACrear.fechaCreacion)
                repositorio.crear(idClientePruebas, entidadACrear)

                val entidadConsultada = repositorio.buscarSegunParametros(idClientePruebas, parametrosEnRangoDeDatos)

                assertEquals(entidadEsperada, entidadConsultada)
            }

            @TestConMultiplesDAO
            fun para_fecha_de_corte_inferior_a_cuando_se_creo_el_cliente_retorna_null()
            {
                repositorio.crear(idClientePruebas, entidadACrear)

                val entidadConsultada = repositorio.buscarSegunParametros(idClientePruebas, parametrosFueraRangoDeDatos)

                assertNull(entidadConsultada)
            }

            @TestConMultiplesDAO
            fun para_fecha_de_corte_igual_o_superior_a_cuando_se_creo_el_cliente_en_otro_cliente_retorna_null()
            {
                repositorio.crear(idClientePruebas, entidadACrear)

                ejecutarConClienteAlternativo {
                    val entidadConsultada = repositorio.buscarSegunParametros(it.id!!, parametrosEnRangoDeDatos)
                    assertNull(entidadConsultada)
                }
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_EsquemaNoExiste_si_se_consulta_por_id_con_id_cliente_inexistente()
            {
                assertThrows<EsquemaNoExiste> { repositorio.buscarSegunParametros(idClientePruebas + 100, parametrosEnRangoDeDatos) }
            }
        }
    }

    @Nested
    inner class Eliminar
    {
        @Nested
        inner class ValidaEnFecha
        {
            private lateinit var entidadPrueba: Cliente.LlaveNFC
            private lateinit var parametrosEnRangoDeDatos: FiltroLlavesNFC.ValidaEnFecha
            private lateinit var parametrosFueraRangoDeDatos: FiltroLlavesNFC.ValidaEnFecha


            @BeforeEach
            fun prepararPrueba()
            {
                val entidadCreada = repositorio.crear(idClientePruebas, darInstanciaEntidadValida())

                parametrosEnRangoDeDatos =
                        FiltroLlavesNFC.ValidaEnFecha(entidadCreada.fechaCreacion.plusDays(10))

                parametrosFueraRangoDeDatos =
                        FiltroLlavesNFC.ValidaEnFecha(entidadCreada.fechaCreacion.minusDays(10))

                entidadPrueba = repositorio.buscarSegunParametros(idClientePruebas, parametrosEnRangoDeDatos)!!

            }


            @TestConMultiplesDAO
            fun para_fecha_de_corte_igual_o_superior_a_cuando_se_creo_el_cliente_se_elimina_correctamente()
            {
                val resultadoEliminacion = repositorio.eliminarSegunFiltros(idClientePruebas, parametrosEnRangoDeDatos)
                val entidadConsultada = repositorio.buscarSegunParametros(idClientePruebas, parametrosEnRangoDeDatos)

                assertTrue(resultadoEliminacion)
                assertNull(entidadConsultada)
            }

            @TestConMultiplesDAO
            fun para_fecha_de_corte_inferior_a_cuando_se_creo_el_cliente_retorna_false_y_no_elimina_entidad_existente()
            {
                val resultadoEliminacion = repositorio.eliminarSegunFiltros(idClientePruebas, parametrosFueraRangoDeDatos)
                val entidadConsultada = repositorio.buscarSegunParametros(idClientePruebas, parametrosEnRangoDeDatos)

                assertFalse(resultadoEliminacion)
                assertEquals(entidadPrueba, entidadConsultada)
            }

            @TestConMultiplesDAO
            fun con_id_existente_en_otro_cliente_retorna_false_y_no_elimina_entidad_existente()
            {
                ejecutarConClienteAlternativo {
                    val resultadoEliminacion = repositorio.eliminarSegunFiltros(it.id!!, parametrosEnRangoDeDatos)
                    assertFalse(resultadoEliminacion)
                }
                val entidadConsultada = repositorio.buscarSegunParametros(idClientePruebas, parametrosEnRangoDeDatos)
                assertEquals(entidadPrueba, entidadConsultada)
            }

            @TestConMultiplesDAO
            fun con_id_cliente_inexistente_lanza_excepcion_EsquemaNoExiste()
            {
                assertThrows<EsquemaNoExiste> {
                    repositorio.eliminarSegunFiltros(idClientePruebas + 100, parametrosEnRangoDeDatos)
                }
            }
        }
    }
}