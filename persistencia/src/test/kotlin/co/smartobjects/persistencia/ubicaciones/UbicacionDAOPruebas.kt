package co.smartobjects.persistencia.ubicaciones

import co.smartobjects.entidades.ubicaciones.Ubicacion
import co.smartobjects.persistencia.EntidadDAOBasePruebas
import co.smartobjects.persistencia.TestConMultiplesDAO
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorio
import co.smartobjects.persistencia.excepciones.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.util.*
import java.util.stream.Stream
import kotlin.test.*

@DisplayName("UbicacionDAO")
internal class UbicacionDAOPruebas
{
    internal class ProveedorTipoUbicacionDAOConTipoNegocio : ArgumentsProvider
    {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments>
        {
            return Stream.of(
                    Arguments.of(UbicacionDAO.Tipo.AREA, Ubicacion.Tipo.AREA),
                    Arguments.of(UbicacionDAO.Tipo.CIUDAD, Ubicacion.Tipo.CIUDAD),
                    Arguments.of(UbicacionDAO.Tipo.PAIS, Ubicacion.Tipo.PAIS),
                    Arguments.of(UbicacionDAO.Tipo.PROPIEDAD, Ubicacion.Tipo.PROPIEDAD),
                    Arguments.of(UbicacionDAO.Tipo.PUNTO_DE_CONTACTO, Ubicacion.Tipo.PUNTO_DE_CONTACTO),
                    Arguments.of(UbicacionDAO.Tipo.PUNTO_DE_INTERES, Ubicacion.Tipo.PUNTO_DE_INTERES),
                    Arguments.of(UbicacionDAO.Tipo.REGION, Ubicacion.Tipo.REGION),
                    Arguments.of(UbicacionDAO.Tipo.ZONA, Ubicacion.Tipo.ZONA)
                            )
        }
    }

    internal class ProveedorSubTipoUbicacionDAOConSubTipoNegocio : ArgumentsProvider
    {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments>
        {
            return Stream.of(
                    Arguments.of(UbicacionDAO.Subtipo.AP, Ubicacion.Subtipo.AP),
                    Arguments.of(UbicacionDAO.Subtipo.AP_INALAMBRICO, Ubicacion.Subtipo.AP_INALAMBRICO),
                    Arguments.of(UbicacionDAO.Subtipo.AP_RESTRINGIDO, Ubicacion.Subtipo.AP_RESTRINGIDO),
                    Arguments.of(UbicacionDAO.Subtipo.KIOSKO, Ubicacion.Subtipo.KIOSKO),
                    Arguments.of(UbicacionDAO.Subtipo.POS, Ubicacion.Subtipo.POS),
                    Arguments.of(UbicacionDAO.Subtipo.POS_SIN_DINERO, Ubicacion.Subtipo.POS_SIN_DINERO)
                            )
        }
    }

    private data class MadreEHija(val madre: Ubicacion, val hija: Ubicacion)

    @Nested
    inner class Conversion
    {
        @Nested
        @DisplayName("Al convertir a entidad de negocio")
        inner class AEntidadNegocio
        {

            @DisplayName("Cuando el Tipo de Ubicación en DAO")
            @ParameterizedTest(name = "Es ''{0}'' asigna ''{1}'' en negocio")
            @ArgumentsSource(ProveedorTipoUbicacionDAOConTipoNegocio::class)
            fun paraTipoUbicacion(tipoDAO: UbicacionDAO.Tipo, tipoNegocio: Ubicacion.Tipo)
            {
                val entidadDAO = UbicacionDAO(nombre = "Entidad prueba", tipo = tipoDAO, subtipo = UbicacionDAO.Subtipo.AP)
                val entidadNegocio = entidadDAO.aEntidadDeNegocio(1)
                assertEquals(tipoNegocio, entidadNegocio.tipo)
            }

            @Test
            fun cuando_el_tipo_de_ubicacion_en_dao_es_INVALIDO_lanza_null_pointer_exception()
            {
                val entidadDAO = UbicacionDAO(nombre = "Entidad prueba", tipo = UbicacionDAO.Tipo.INVALIDO, subtipo = UbicacionDAO.Subtipo.AP)
                assertThrows<NullPointerException> { entidadDAO.aEntidadDeNegocio(1) }
            }


            @DisplayName("Cuando el SubTipo de Ubicación en DAO")
            @ParameterizedTest(name = "Es ''{0}'' asigna ''{1}'' en negocio")
            @ArgumentsSource(ProveedorSubTipoUbicacionDAOConSubTipoNegocio::class)
            fun paraSubTipoUbicacion(subtipoDAO: UbicacionDAO.Subtipo, subtipoNegocio: Ubicacion.Subtipo)
            {
                val entidadDAO = UbicacionDAO(nombre = "Entidad prueba", tipo = UbicacionDAO.Tipo.AREA, subtipo = subtipoDAO)
                val entidadNegocio = entidadDAO.aEntidadDeNegocio(1)
                assertEquals(subtipoNegocio, entidadNegocio.subtipo)
            }

            @Test
            fun cuando_el_subtipo_de_ubicacion_en_dao_es_INVALIDO_lanza_null_pointer_exception()
            {
                val entidadDAO = UbicacionDAO(nombre = "Entidad prueba", tipo = UbicacionDAO.Tipo.AREA, subtipo = UbicacionDAO.Subtipo.INVALIDO)
                assertThrows<NullPointerException> { entidadDAO.aEntidadDeNegocio(1) }
            }
        }

        @Nested
        @DisplayName("Al convertir desde entidad de negocio")
        inner class DesdeEntidadNegocio
        {
            private fun darEntidadNegocioPruebaSegunTipoYSubTipoDeUbicacion(tipo: Ubicacion.Tipo, subtipo: Ubicacion.Subtipo): Ubicacion
            {
                return Ubicacion(
                        1,
                        null,
                        "Ubicacion prueba",
                        tipo,
                        subtipo,
                        null,
                        linkedSetOf()
                                )
            }

            @DisplayName("Cuando el Tipo de Ubicación")
            @ParameterizedTest(name = "Es ''{1}'' asigna ''{0}'' en dao")
            @ArgumentsSource(ProveedorTipoUbicacionDAOConTipoNegocio::class)
            fun paraTipoUbicacion(tipoDAO: UbicacionDAO.Tipo, tipoNegocio: Ubicacion.Tipo)
            {
                val entidadNegocio = darEntidadNegocioPruebaSegunTipoYSubTipoDeUbicacion(tipoNegocio, Ubicacion.Subtipo.AP)
                val entidadDAOCreando = UbicacionDAO(entidadNegocio)
                assertEquals(tipoDAO, entidadDAOCreando.tipo)
                val entidadDAOEditando = UbicacionDAO(entidadNegocio)
                assertEquals(tipoDAO, entidadDAOEditando.tipo)
            }

            @DisplayName("Cuando el SubTipo de Ubicación")
            @ParameterizedTest(name = "Es ''{1}'' asigna ''{0}'' en dao")
            @ArgumentsSource(ProveedorSubTipoUbicacionDAOConSubTipoNegocio::class)
            fun paraSubTipoUbicacion(subtipoDAO: UbicacionDAO.Subtipo, subtipoNegocio: Ubicacion.Subtipo)
            {
                val entidadNegocio = darEntidadNegocioPruebaSegunTipoYSubTipoDeUbicacion(Ubicacion.Tipo.AREA, subtipoNegocio)
                val entidadDAOCreando = UbicacionDAO(entidadNegocio)
                assertEquals(subtipoDAO, entidadDAOCreando.subtipo)
                val entidadDAOEditando = UbicacionDAO(entidadNegocio)
                assertEquals(subtipoDAO, entidadDAOEditando.subtipo)
            }
        }
    }

    @Nested
    inner class EnBD : EntidadDAOBasePruebas()
    {

        private val repositorio: RepositorioUbicaciones by lazy { RepositorioUbicacionesSQL(configuracionRepositorios) }
        override val creadoresRepositoriosUsados: List<CreadorRepositorio<*>> by lazy {
            listOf<CreadorRepositorio<*>>(repositorio)
        }

        private fun crearMadreYNieta(entidadAbuela: Ubicacion): MadreEHija
        {
            val entidadMadre =
                    Ubicacion(
                            idClientePruebas,
                            null,
                            "Ubicacion madre",
                            Ubicacion.Tipo.PROPIEDAD,
                            Ubicacion.Subtipo.POS,
                            entidadAbuela.id,
                            linkedSetOf<Long>(97, 15, 32)
                             )

            val entidadMadreCreada = repositorio.crear(idClientePruebas, entidadMadre)

            val entidadHija =
                    Ubicacion(
                            idClientePruebas,
                            null,
                            "Ubicacion hija",
                            Ubicacion.Tipo.PROPIEDAD,
                            Ubicacion.Subtipo.POS,
                            entidadMadreCreada.id,
                            linkedSetOf<Long>(97, 15, 32)
                             )

            val entidadHijaCreada = repositorio.crear(idClientePruebas, entidadHija)

            return MadreEHija(entidadMadreCreada, entidadHijaCreada)
        }

        @Nested
        inner class Crear
        {
            @TestConMultiplesDAO
            fun sin_id_asignado_retorna_misma_entidad_con_id_asignado_por_bd()
            {
                val entidadAInsertar =
                        Ubicacion(
                                idClientePruebas,
                                null,
                                "Ubicacion",
                                Ubicacion.Tipo.PROPIEDAD,
                                Ubicacion.Subtipo.POS,
                                null,
                                linkedSetOf()
                                 )

                val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

                assertNotNull(entidadCreada.id)
                assertEquals(entidadAInsertar.copiar(id = entidadCreada.id), entidadCreada)
            }

            @TestConMultiplesDAO
            fun con_id_asignado_retorna_misma_entidad_con_id_original()
            {
                val entidadAInsertar =
                        Ubicacion(
                                idClientePruebas,
                                792302,
                                "Ubicacion",
                                Ubicacion.Tipo.PROPIEDAD,
                                Ubicacion.Subtipo.POS,
                                null,
                                linkedSetOf()
                                 )

                val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

                assertEquals(792302, entidadCreada.id)
                assertEquals(entidadAInsertar.copiar(id = entidadCreada.id), entidadCreada)
            }

            @TestConMultiplesDAO
            fun utiliza_el_parametro_id_cliente_y_retorna_misma_entidad_con_id_cliente_correcto()
            {
                val entidadAInsertar =
                        Ubicacion(
                                Random().nextLong(),
                                null,
                                "Ubicacion",
                                Ubicacion.Tipo.PROPIEDAD,
                                Ubicacion.Subtipo.POS,
                                null,
                                linkedSetOf()
                                 )

                val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

                assertEquals(idClientePruebas, entidadCreada.idCliente)
                assertEquals(entidadAInsertar.copiar(id = entidadCreada.id, idCliente = idClientePruebas), entidadCreada)
            }

            @TestConMultiplesDAO
            fun cuando_se_envia_una_lista_no_vacia_de_ancestros_y_sin_id_padre_retorna_una_lista_vacia_de_ancestros()
            {
                val entidadAInsertar =
                        Ubicacion(
                                idClientePruebas,
                                null,
                                "Ubicacion",
                                Ubicacion.Tipo.PROPIEDAD,
                                Ubicacion.Subtipo.POS,
                                null,
                                linkedSetOf<Long>(99, 98, 97)
                                 )

                val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

                assertEquals(linkedSetOf(), entidadCreada.idsDeAncestros)
            }

            @TestConMultiplesDAO
            fun se_ignora_la_lista_de_ancestros_y_retorna_lista_de_ancestros_correcta()
            {
                val entidadAbuela =
                        Ubicacion(
                                idClientePruebas,
                                null,
                                "Ubicacion 1",
                                Ubicacion.Tipo.PROPIEDAD,
                                Ubicacion.Subtipo.POS,
                                null,
                                linkedSetOf<Long>(97, 15, 32)
                                 )

                val entidadAbuelaCreada = repositorio.crear(idClientePruebas, entidadAbuela)
                assertEquals(linkedSetOf(), entidadAbuelaCreada.idsDeAncestros)

                val entidadMadre =
                        Ubicacion(
                                idClientePruebas,
                                null,
                                "Ubicacion 2",
                                Ubicacion.Tipo.PROPIEDAD,
                                Ubicacion.Subtipo.POS,
                                entidadAbuelaCreada.id,
                                linkedSetOf<Long>(97, 15, 32)
                                 )

                val entidadMadreCreada = repositorio.crear(idClientePruebas, entidadMadre)
                assertEquals(linkedSetOf(entidadAbuelaCreada.id!!), entidadMadreCreada.idsDeAncestros)

                val entidadHija =
                        Ubicacion(
                                idClientePruebas,
                                null,
                                "Ubicacion 3",
                                Ubicacion.Tipo.PROPIEDAD,
                                Ubicacion.Subtipo.POS,
                                entidadMadreCreada.id,
                                linkedSetOf<Long>(97, 15, 32)
                                 )

                val entidadHijaCreada = repositorio.crear(idClientePruebas, entidadHija)
                assertEquals(linkedSetOf(entidadAbuelaCreada.id!!, entidadMadreCreada.id!!), entidadHijaCreada.idsDeAncestros)
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_con_ErrorCreacionActualizacionPorDuplicidad_si_se_usa_nombre_duplicado()
            {
                val entidadAbuela =
                        Ubicacion(
                                idClientePruebas,
                                null,
                                "Ubicacion 1",
                                Ubicacion.Tipo.PROPIEDAD,
                                Ubicacion.Subtipo.POS,
                                null,
                                linkedSetOf<Long>(97, 15, 32)
                                 )

                val entidadAbuelaCreada = repositorio.crear(idClientePruebas, entidadAbuela)

                assertThrows<ErrorCreacionActualizacionPorDuplicidad> { repositorio.crear(idClientePruebas, entidadAbuelaCreada) }
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_EntidadNoExiste_si_se_referencia_una_ubicacion_padre_que_no_existe()
            {
                val entidadPadre =
                        Ubicacion(
                                idClientePruebas,
                                null,
                                "Ubicacion Padre",
                                Ubicacion.Tipo.PROPIEDAD,
                                Ubicacion.Subtipo.POS,
                                null,
                                linkedSetOf()
                                 )

                val idPadreValido = repositorio.crear(idClientePruebas, entidadPadre).id!!

                val entidadHijo =
                        Ubicacion(
                                idClientePruebas,
                                null,
                                "Ubicacion Hijo",
                                Ubicacion.Tipo.PROPIEDAD,
                                Ubicacion.Subtipo.POS,
                                idPadreValido + (Random().nextLong() - idPadreValido),
                                linkedSetOf()
                                 )

                assertThrows<ErrorDeLlaveForanea> { repositorio.crear(entidadPadre.idCliente, entidadHijo) }
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_EsquemaNoExiste_si_cliente_con_id_dado_no_existe()
            {
                val entidadAInsertar =
                        Ubicacion(
                                idClientePruebas + 100,
                                null,
                                "Ubicacion",
                                Ubicacion.Tipo.PROPIEDAD,
                                Ubicacion.Subtipo.POS,
                                null,
                                linkedSetOf()
                                 )

                assertThrows<EsquemaNoExiste> { repositorio.crear(idClientePruebas + 100, entidadAInsertar) }
            }
        }

        @Nested
        inner class Consultar
        {
            private fun crearAbuela(): Ubicacion
            {
                return repositorio.crear(
                        idClientePruebas,
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

            @TestConMultiplesDAO
            fun por_id_existente_retorna_entidad_correcta()
            {
                val entidadAbuela = crearAbuela()

                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadAbuela.id!!)

                assertEquals(entidadAbuela, entidadConsultada)
            }

            @TestConMultiplesDAO
            fun con_ancestros_retorna_entidad_correcta()
            {
                val entidadAbuela = crearAbuela()
                val entidadConsultadaAbuela = repositorio.buscarPorId(idClientePruebas, entidadAbuela.id!!)
                assertEquals(entidadAbuela, entidadConsultadaAbuela)

                val madreEHija = crearMadreYNieta(entidadAbuela)
                val entidadConsultadPadre = repositorio.buscarPorId(idClientePruebas, madreEHija.madre.id!!)
                assertEquals(madreEHija.madre, entidadConsultadPadre)

                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, madreEHija.hija.id!!)
                assertEquals(madreEHija.hija, entidadConsultada)
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
                val entidadAbuela = crearAbuela()
                val madreEHija = crearMadreYNieta(entidadAbuela)

                val listadoEsperado = listOf(entidadAbuela, madreEHija.madre, madreEHija.hija).sortedBy { it.id!! }

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
                val entidadAbuela = crearAbuela()

                ejecutarConClienteAlternativo {
                    val entidadConsultada = repositorio.buscarPorId(it.id!!, entidadAbuela.id!!)
                    assertNull(entidadConsultada)
                }
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_EsquemaNoExiste_si_se_consulta_por_id_con_id_cliente_inexistente()
            {
                assertThrows<EsquemaNoExiste> { repositorio.buscarPorId(idClientePruebas + 100, 789456789) }
            }

            @TestConMultiplesDAO
            fun listado_de_entidades_con_entidades_en_otro_cliente_retorna_lista_vacia()
            {
                crearAbuela()

                ejecutarConClienteAlternativo {
                    val listadoConsultado = repositorio.listar(it.id!!)
                    assertTrue(listadoConsultado.none())
                }
            }

            @TestConMultiplesDAO
            fun lanza_excepcion_EsquemaNoExiste_si_se_consulta_listado_de_entidades_con_id_cliente_inexistente()
            {
                assertThrows<EsquemaNoExiste> { repositorio.listar(idClientePruebas + 100) }
            }
        }

        @Nested
        inner class Actualizar
        {
            private lateinit var entidadAbuela: Ubicacion

            @BeforeEach
            fun crearUbicacionPrueba()
            {
                entidadAbuela =
                        repositorio.crear(
                                idClientePruebas,
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

            @TestConMultiplesDAO
            fun se_actualizan_todos_los_campos_correctamente()
            {
                val entidadHija1 =
                        Ubicacion(
                                idClientePruebas,
                                null,
                                "Ubicacion hija 1",
                                Ubicacion.Tipo.PROPIEDAD,
                                Ubicacion.Subtipo.POS,
                                entidadAbuela.id,
                                linkedSetOf(entidadAbuela.id!!)
                                 )

                val entidadHijaCreada1 = repositorio.crear(idClientePruebas, entidadHija1)

                val entidadHija2 =
                        Ubicacion(
                                idClientePruebas,
                                null,
                                "Ubicacion 3",
                                Ubicacion.Tipo.PROPIEDAD,
                                Ubicacion.Subtipo.POS,
                                entidadAbuela.id,
                                linkedSetOf<Long>(97, 15, 32)
                                 )

                val entidadHijaCreada2 = repositorio.crear(idClientePruebas, entidadHija2)

                val entidadAActualizar =
                        entidadHijaCreada2.copiar(
                                nombre = "Ubicacion actualizada",
                                tipo = Ubicacion.Tipo.PUNTO_DE_INTERES,
                                subtipo = Ubicacion.Subtipo.AP_RESTRINGIDO,
                                idDelPadre = entidadHijaCreada1.id,
                                idsDeAncestros = linkedSetOf(entidadHijaCreada1.idDelPadre!!, entidadHijaCreada1.id!!)
                                                 )

                val entidadActualizada = repositorio.actualizar(idClientePruebas, entidadAActualizar.id!!, entidadAActualizar)

                assertEquals(entidadAActualizar, entidadActualizada)
            }

            @TestConMultiplesDAO
            fun utiliza_el_parametro_id_cliente_y_retorna_misma_entidad_con_id_cliente_correcto()
            {
                val entidadConCambios = entidadAbuela.copiar(idCliente = Random().nextLong())

                val entidadActualizada = repositorio.actualizar(idClientePruebas, entidadConCambios.id!!, entidadConCambios)

                assertEquals(idClientePruebas, entidadActualizada.idCliente)
                assertEquals(entidadConCambios.copiar(idCliente = idClientePruebas), entidadActualizada)
            }

            @TestConMultiplesDAO
            fun con_id_inexistente_lanza_excepcion_EntidadNoExiste()
            {
                val abuelaConCambios = entidadAbuela.copiar(id = 789456)

                assertThrows<EntidadNoExiste> { repositorio.actualizar(idClientePruebas, 789456, abuelaConCambios) }
            }

            @TestConMultiplesDAO
            fun con_id_existente_en_otro_cliente_lanza_excepcion_EntidadNoExiste()
            {
                val entidadConCambios = entidadAbuela

                ejecutarConClienteAlternativo {
                    assertThrows<EntidadNoExiste> { repositorio.actualizar(it.id!!, entidadConCambios.id!!, entidadConCambios) }
                }
            }

            @TestConMultiplesDAO
            fun con_id_cliente_inexistente_lanza_excepcion_EsquemaNoExiste()
            {
                val entidadConCambios = entidadAbuela
                assertThrows<EsquemaNoExiste> { repositorio.actualizar(idClientePruebas + 100, entidadConCambios.id!!, entidadConCambios) }
            }

            @TestConMultiplesDAO
            fun cuando_se_envia_una_lista_no_vacia_de_ancestros_y_sin_id_padre_retorna_una_lista_vacia_de_ancestros()
            {
                val entidadConCambios = entidadAbuela.copiar(idDelPadre = null, idsDeAncestros = linkedSetOf(79, 56, 18))

                val entidadActualizada = repositorio.actualizar(idClientePruebas, entidadConCambios.id!!, entidadConCambios)

                assertEquals(entidadConCambios.copiar(idCliente = idClientePruebas, idsDeAncestros = linkedSetOf()), entidadActualizada)
            }

            @TestConMultiplesDAO
            fun se_ignora_la_lista_de_ancestros_y_retorna_lista_de_ancestros_correcta()
            {
                val madreYNieta = crearMadreYNieta(entidadAbuela)

                val entidadConCambios = madreYNieta.hija.copiar(idsDeAncestros = linkedSetOf(79, 56, 18))

                val entidadActualizada = repositorio.actualizar(idClientePruebas, entidadConCambios.id!!, entidadConCambios)

                assertEquals(linkedSetOf(entidadAbuela.id!!, madreYNieta.madre.id!!), entidadActualizada.idsDeAncestros)
            }

            @TestConMultiplesDAO
            fun con_padre_e_hijos_actualiza_los_ancestros_de_las_entidades_hijas()
            {
                val madreYNieta = crearMadreYNieta(entidadAbuela)

                val nuevaBisabuela =
                        Ubicacion(
                                idClientePruebas,
                                null,
                                "Ubicacion nueva abuela",
                                Ubicacion.Tipo.PROPIEDAD,
                                Ubicacion.Subtipo.POS,
                                null,
                                linkedSetOf()
                                 )

                val nuevaNuevabisabuelaCreada = repositorio.crear(idClientePruebas, nuevaBisabuela)

                val abuelaConCambios =
                        entidadAbuela.copiar(
                                nombre = "Abuela actualizada",
                                tipo = Ubicacion.Tipo.PUNTO_DE_INTERES,
                                subtipo = Ubicacion.Subtipo.AP_RESTRINGIDO,
                                idDelPadre = nuevaNuevabisabuelaCreada.id,
                                idsDeAncestros = linkedSetOf(nuevaNuevabisabuelaCreada.id!!)
                                            )

                val abuelaActualizada = repositorio.actualizar(idClientePruebas, abuelaConCambios.id!!, abuelaConCambios)

                assertEquals(abuelaConCambios, abuelaActualizada)

                val ubicacionMadreConsultada = repositorio.buscarPorId(idClientePruebas, madreYNieta.madre.id!!)!!
                assertEquals(linkedSetOf(nuevaNuevabisabuelaCreada.id!!, entidadAbuela.id!!), ubicacionMadreConsultada.idsDeAncestros)

                val ubicacionHijaConsultada = repositorio.buscarPorId(idClientePruebas, madreYNieta.hija.id!!)!!
                assertEquals(linkedSetOf(nuevaNuevabisabuelaCreada.id!!, entidadAbuela.id!!, madreYNieta.madre.id!!), ubicacionHijaConsultada.idsDeAncestros)
            }

            @TestConMultiplesDAO
            fun con_padre_formando_ciclo_lanza_excepcion_ErrorDeJerarquiaPorCiclo()
            {
                val madreYNieta = crearMadreYNieta(entidadAbuela)

                val abuelaConCambios = entidadAbuela.copiar(idDelPadre = madreYNieta.hija.id!!)

                assertThrows<ErrorDeJerarquiaPorCiclo> { repositorio.actualizar(idClientePruebas, abuelaConCambios.id!!, abuelaConCambios) }
            }

            @TestConMultiplesDAO
            fun con_nombre_duplicado_lanza_excepcion_ErrorCreacionActualizacionPorDuplicidad()
            {
                val madreEHija = crearMadreYNieta(entidadAbuela)
                val hijaConMismoNombreQueMadre = madreEHija.hija.copiar(nombre = madreEHija.madre.nombre)

                assertThrows<ErrorCreacionActualizacionPorDuplicidad> { repositorio.actualizar(idClientePruebas, hijaConMismoNombreQueMadre.id!!, hijaConMismoNombreQueMadre) }
            }

            @TestConMultiplesDAO
            fun con_padre_inexistente_lanza_excepcion_ErrorDeLlaveForanea()
            {
                val abuelaConCambios = entidadAbuela.copiar(idDelPadre = 789456)

                assertThrows<ErrorDeLlaveForanea> { repositorio.actualizar(idClientePruebas, abuelaConCambios.id!!, abuelaConCambios) }
            }
        }

        @Nested
        inner class Eliminar
        {
            private lateinit var entidadAbuela: Ubicacion

            @BeforeEach
            fun crearUbicacionPrueba()
            {
                entidadAbuela =
                        repositorio.crear(
                                idClientePruebas,
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

            @TestConMultiplesDAO
            fun si_existe_la_entidad_se_elimina_correctamente()
            {
                val resultadoEliminacion = repositorio.eliminarPorId(idClientePruebas, entidadAbuela.id!!)
                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadAbuela.id!!)

                assertTrue(resultadoEliminacion)
                assertNull(entidadConsultada)
            }

            @TestConMultiplesDAO
            fun con_id_existente_en_otro_cliente_retorna_false_y_no_elimina_entidad_existente()
            {
                ejecutarConClienteAlternativo {
                    val resultadoEliminacion = repositorio.eliminarPorId(it.id!!, entidadAbuela.id!!)
                    assertFalse(resultadoEliminacion)
                }
                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadAbuela.id!!)
                assertEquals(entidadAbuela, entidadConsultada)
            }

            @TestConMultiplesDAO
            fun con_id_cliente_inexistente_lanza_excepcion_EsquemaNoExiste()
            {
                assertThrows<EsquemaNoExiste> { repositorio.eliminarPorId(idClientePruebas + 100, entidadAbuela.id!!) }
            }

            @TestConMultiplesDAO
            fun si_no_existe_la_entidad_retorna_false_y_no_elimina_entidad_existente()
            {
                val resultadoEliminacion = repositorio.eliminarPorId(idClientePruebas, entidadAbuela.id!! + 1)
                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadAbuela.id!!)

                assertFalse(resultadoEliminacion)
                assertEquals(entidadAbuela, entidadConsultada)
            }

            @TestConMultiplesDAO
            fun si_se_intenta_borrar_el_padre_lanza_excepcion_ErrorDeLlaveForanea_y_no_elimina_entidad()
            {
                crearMadreYNieta(entidadAbuela)

                assertThrows<ErrorDeLlaveForanea> { repositorio.eliminarPorId(idClientePruebas, entidadAbuela.id!!) }

                val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadAbuela.id!!)
                assertEquals(entidadAbuela, entidadConsultada)
            }
        }
    }
}