package co.smartobjects.prompterbackend.serviciosrest.operativas.ordenes

import co.smartobjects.campos.CampoModificableEntidad
import co.smartobjects.entidades.operativas.EntidadTransaccional
import co.smartobjects.entidades.operativas.ordenes.LoteDeOrdenes
import co.smartobjects.entidades.operativas.ordenes.Orden
import co.smartobjects.entidades.operativas.ordenes.Transaccion
import co.smartobjects.entidades.ubicaciones.consumibles.ConsumibleEnPuntoDeVenta
import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.persistencia.excepciones.*
import co.smartobjects.persistencia.operativas.ordenes.IdTransaccionActualizacionTerminacionOrden
import co.smartobjects.persistencia.operativas.ordenes.RepositorioOrdenes
import co.smartobjects.prompterbackend.*
import co.smartobjects.prompterbackend.excepciones.*
import co.smartobjects.prompterbackend.excepciones.EntidadNoExiste
import co.smartobjects.prompterbackend.serviciosrest.clientes.RecursoClientes
import co.smartobjects.prompterbackend.serviciosrest.operativas.reservas.sesionesdemanilla.RecursoOrdenesDeUnaSesionDeManillaPruebas
import co.smartobjects.prompterbackend.serviciosrest.usuarios.RecursoPermisosPosibles
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.modelos.clientes.ClienteDTO
import co.smartobjects.red.modelos.operativas.EntidadTransaccionalDTO
import co.smartobjects.red.modelos.operativas.TransaccionEntidadTerminadaDTO
import co.smartobjects.red.modelos.operativas.ordenes.LoteDeOrdenesDTO
import co.smartobjects.red.modelos.operativas.ordenes.OrdenDTO
import co.smartobjects.red.modelos.usuarios.PermisoBackDTO
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.FECHA_MINIMA_CREACION
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import com.fasterxml.jackson.datatype.jaxrs.Jaxrs2TypesModule
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider
import org.glassfish.grizzly.http.server.HttpServer
import org.junit.jupiter.api.*
import org.mockito.Mockito.*
import org.threeten.bp.*
import java.util.*
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import kotlin.test.assertEquals

@DisplayName("Recurso LotesDeOrdenes")
internal class RecursoLotesDeOrdenesPruebas
{
    companion object
    {
        private const val ID_CLIENTE = 1L
        private val UUID_PRUEBAS: UUID = UUID.randomUUID()
        private val ID_ENTIDAD_PRUEBAS = EntidadTransaccional.PartesId(1, "Un usuario", UUID_PRUEBAS).id
    }

    private fun darEntidadNegocioSegunIndice(indice: Int): LoteDeOrdenes
    {
        return LoteDeOrdenes(
                ID_CLIENTE,
                "Un usuario",
                UUID_PRUEBAS,
                indice.toLong(),
                true,
                List(indice + 1) {
                    Orden(
                            RecursoOrdenesDeUnaSesionDeManillaPruebas.ID_CLIENTE,
                            indice.toLong(),
                            indice.toLong(),
                            listOf(
                                    Transaccion.Debito(
                                            RecursoOrdenesDeUnaSesionDeManillaPruebas.ID_CLIENTE,
                                            indice.toLong(),
                                            "usuario",
                                            Decimal.UNO,
                                            indice.toLong(),
                                            "dispositivo",
                                            ConsumibleEnPuntoDeVenta(indice.toLong(), indice.toLong(), "código externo fondo $indice")
                                                      )
                                  ),
                            ZonedDateTime.of(LocalDate.of(Year.now(ZONA_HORARIA_POR_DEFECTO).value + 1 + indice, 1, 1), LocalTime.of(5, 5), ZONA_HORARIA_POR_DEFECTO)
                         )
                }
                            )
    }

    private fun darEntidadDTOSegunIndice(indice: Int): LoteDeOrdenesDTO
    {
        return LoteDeOrdenesDTO(darEntidadNegocioSegunIndice(indice))
    }

    @[Nested DisplayName("Al llamar por red")]
    inner class Red
    {
        private lateinit var server: HttpServer
        private lateinit var target: WebTarget
        private lateinit var mockRecursoTodasEntidades: RecursoLotesDeOrdenes
        private lateinit var mockRecursoEntidadEspecifica: RecursoLotesDeOrdenes.RecursoLoteDeOrdenes

        @[BeforeEach Throws(Exception::class)]
        fun antesDeCadaTest()
        {
            mockRecursoEntidadEspecifica = mockConDefaultAnswer(RecursoLotesDeOrdenes.RecursoLoteDeOrdenes::class.java)
            mockRecursoTodasEntidades = mockConDefaultAnswer(RecursoLotesDeOrdenes::class.java)

            val mockRecursoCliente = mockConDefaultAnswer(RecursoClientes.RecursoCliente::class.java)
            val mockRecursoClientes = mockConDefaultAnswer(RecursoClientes::class.java)

            doReturn(mockRecursoCliente).`when`(mockRecursoClientes).darRecursosEntidadEspecifica(ID_CLIENTE)
            doReturn(mockRecursoTodasEntidades).`when`(mockRecursoCliente).darRecursoLotesDeOrdenes()
            doReturn(mockRecursoEntidadEspecifica).`when`(mockRecursoTodasEntidades).darRecursosEntidadEspecifica(ID_ENTIDAD_PRUEBAS)

            mockearConfiguracionAplicacionJerseyParaNoUsarConfiguracionRepositorios(mockRecursoClientes)

            server = PrompterBackend.arrancarServidor()
            target = ClientBuilder.newClient()
                .register(JacksonJaxbJsonProvider().apply {
                    setMapper(ConfiguracionJackson.objectMapperDeJackson.apply { registerModule(Jaxrs2TypesModule()) })
                })
                .target(PrompterBackend.BASE_URI)
        }

        @[AfterEach Throws(Exception::class)]
        fun despuesDeCadaTest()
        {
            server.shutdownNow()
        }

        @[Nested DisplayName("Al actualizar")]
        inner class Actualizar
        {
            @Test
            fun llama_la_funcion_actualizar_con_dto_correcto()
            {
                val dtoPruebas = darEntidadDTOSegunIndice(1)
                doReturn(dtoPruebas.ordenes).`when`(mockRecursoEntidadEspecifica).actualizar(dtoPruebas)

                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoLotesDeOrdenes.RUTA}/$ID_ENTIDAD_PRUEBAS").request()
                    .put(Entity.entity(dtoPruebas, MediaType.APPLICATION_JSON_TYPE), List::class.java)

                verify(mockRecursoEntidadEspecifica).actualizar(dtoPruebas)
            }
        }

        @[Nested DisplayName("Al hacer patch")]
        inner class Patch
        {
            @Test
            fun llama_la_funcion_actualizar_con_dto_correcto()
            {
                val entidadPatch = TransaccionEntidadTerminadaDTO<LoteDeOrdenes>(true)
                doNothing().`when`(mockRecursoEntidadEspecifica).patch(entidadPatch)

                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoLotesDeOrdenes.RUTA}/$ID_ENTIDAD_PRUEBAS")
                    .request()
                    .patch(entidadPatch, TransaccionEntidadTerminadaDTO::class.java)

                verify(mockRecursoEntidadEspecifica).patch(entidadPatch)
            }
        }
    }

    @[Nested DisplayName("Al llamar por código")]
    inner class Codigo
    {
        private val mockRepositorio = mockConDefaultAnswer(RepositorioOrdenes::class.java)
        private var mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteSiempreTienePermisos
        private val recursoTodasEntidades: RecursoLotesDeOrdenes by lazy { RecursoLotesDeOrdenes(ID_CLIENTE, mockRepositorio, mockManejadorSeguridad) }
        private val recursoEntidadEspecifica: RecursoLotesDeOrdenes.RecursoLoteDeOrdenes by lazy { recursoTodasEntidades.darRecursosEntidadEspecifica(ID_ENTIDAD_PRUEBAS) }


        @Nested
        @DisplayName("Al crear (PUT)")
        inner class Actualizar
        {
            private lateinit var entidadNegocio: LoteDeOrdenes
            private lateinit var entidadDTO: LoteDeOrdenesDTO

            @BeforeEach
            private fun crearEntidadesDePrueba()
            {
                entidadNegocio = darEntidadNegocioSegunIndice(1)
                entidadDTO = darEntidadDTOSegunIndice(1)
            }

            @Test
            fun retorna_el_dto_correcto_cuando_el_repositorio_actualiza_la_entidad()
            {
                doReturn(entidadNegocio.ordenes)
                    .`when`(mockRepositorio)
                    .crear(ID_CLIENTE, entidadNegocio)

                val entidadRetornada = recursoEntidadEspecifica.actualizar(entidadDTO)

                assertEquals(entidadDTO.ordenes, entidadRetornada)
                verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
            }

            @Test
            fun usa_el_id_de_la_ruta_cuando_el_id_de_la_entidad_no_coincide_con_el_de_la_ruta()
            {
                doReturn(entidadNegocio.ordenes)
                    .`when`(mockRepositorio)
                    .crear(ID_CLIENTE, entidadNegocio)

                val entidadRetornada = recursoEntidadEspecifica.actualizar(entidadDTO.copy(id = EntidadTransaccional.PartesId(1234567L, "Otro Usuario", UUID.randomUUID()).id))

                assertEquals(entidadDTO.ordenes, entidadRetornada)
                verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_entidad_referenciada_no_existe_cuando_el_repositorio_lanza_ErrorDeLlaveForanea()
            {
                doThrow(ErrorDeLlaveForanea(0, "Alguien no existe"))
                    .`when`(mockRepositorio)
                    .crear(ID_CLIENTE, entidadNegocio)

                val errorApi = assertThrows<EntidadReferenciadaNoExiste> { recursoEntidadEspecifica.actualizar(entidadDTO) }

                assertEquals(LoteDeOrdenesDTO.CodigosError.ENTIDAD_REFERENCIADA_NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_no_existe_cuando_el_repositorio_lanza_EntidadNoExiste()
            {
                doThrow(co.smartobjects.persistencia.excepciones.EntidadNoExiste(Random().nextLong(), "no importa"))
                    .`when`(mockRepositorio)
                    .crear(ID_CLIENTE, entidadNegocio)

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.actualizar(entidadDTO) }

                assertEquals(LoteDeOrdenesDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .crear(ID_CLIENTE, entidadNegocio)

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.actualizar(entidadDTO) }

                assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
            }

            @Nested
            @DisplayName("Lanzan EntidadInvalida y código interno")
            inner class LanzanExcepcionEntidadInvalida
            {
                @Test
                fun entidad_duplicada_cuando_el_repositorio_lanza_ErrorCreacionActualizacionPorDuplicidad()
                {
                    doThrow(ErrorCreacionActualizacionPorDuplicidad("LoteDeOrdenes"))
                        .`when`(mockRepositorio)
                        .crear(ID_CLIENTE, entidadNegocio)

                    val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTO) }

                    assertEquals(LoteDeOrdenesDTO.CodigosError.ENTIDAD_DUPLICADA_EN_BD, errorApi.codigoInterno)
                    verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
                }

                @Test
                fun desconocido_cuando_el_repositorio_lanza_ErrorDeCreacionActualizacionEntidad()
                {
                    doThrow(ErrorDeCreacionActualizacionEntidad("Error creando LoteDeOrdenes"))
                        .`when`(mockRepositorio)
                        .crear(ID_CLIENTE, entidadNegocio)

                    val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTO) }

                    assertEquals(LoteDeOrdenesDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                    verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
                }

                @Nested
                @DisplayName("Id inválido")
                inner class IdInvalido
                {
                    @Test
                    fun cuando_el_id_tiene_solo_dos_partes()
                    {
                        val id = entidadDTO.id.split(EntidadTransaccional.SEPARADOR_COMPONENTES_ID).subList(0, 2).joinToString(EntidadTransaccional.SEPARADOR_COMPONENTES_ID.toString())

                        val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.darRecursosEntidadEspecifica(id).actualizar(entidadDTO.copy(id = id)) }

                        assertEquals(EntidadTransaccionalDTO.CodigosError.ID_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                    }

                    @Test
                    fun cuando_el_id_tiene_mas_de_tres_partes()
                    {
                        val id = "${entidadDTO.id}${EntidadTransaccional.SEPARADOR_COMPONENTES_ID}Otra cosa"

                        val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.darRecursosEntidadEspecifica(id).actualizar(entidadDTO.copy(id = id)) }

                        assertEquals(EntidadTransaccionalDTO.CodigosError.ID_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                    }

                    @Test
                    fun cuando_el_id_tiene_tiempo_creacion_invalido()
                    {
                        val partes = entidadDTO.id.split(EntidadTransaccional.SEPARADOR_COMPONENTES_ID).toMutableList()
                        partes[0] = "No es numero"
                        val id = partes.joinToString(EntidadTransaccional.SEPARADOR_COMPONENTES_ID.toString())

                        val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.darRecursosEntidadEspecifica(id).actualizar(entidadDTO.copy(id = id)) }

                        assertEquals(EntidadTransaccionalDTO.CodigosError.ID_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                    }

                    @Test
                    fun cuando_el_id_tiene_uuid_invalido()
                    {
                        val partes = entidadDTO.id.split(EntidadTransaccional.SEPARADOR_COMPONENTES_ID).toMutableList()
                        partes[2] = "uuid-invalido"
                        val id = partes.joinToString(EntidadTransaccional.SEPARADOR_COMPONENTES_ID.toString())

                        val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.darRecursosEntidadEspecifica(id).actualizar(entidadDTO.copy(id = id)) }

                        assertEquals(EntidadTransaccionalDTO.CodigosError.ID_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                    }
                }

                @Nested
                @DisplayName("Nombre usuario inválido")
                inner class NombreUsuarioInvalido
                {
                    @Test
                    fun cuando_el_id_tiene_nombre_usuario_vacio()
                    {
                        val partes = entidadDTO.id.split(EntidadTransaccional.SEPARADOR_COMPONENTES_ID).toMutableList()
                        partes[1] = ""
                        val id = partes.joinToString(EntidadTransaccional.SEPARADOR_COMPONENTES_ID.toString())

                        val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.darRecursosEntidadEspecifica(id).actualizar(entidadDTO.copy(id = id)) }

                        assertEquals(EntidadTransaccionalDTO.CodigosError.NOMBRE_USUARIO_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                    }

                    @Test
                    fun cuando_el_id_tiene_solo_espacios_y_tabs()
                    {
                        val partes = entidadDTO.id.split(EntidadTransaccional.SEPARADOR_COMPONENTES_ID).toMutableList()
                        partes[1] = "      "
                        val id = partes.joinToString(EntidadTransaccional.SEPARADOR_COMPONENTES_ID.toString())

                        val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.darRecursosEntidadEspecifica(id).actualizar(entidadDTO.copy(id = id)) }

                        assertEquals(EntidadTransaccionalDTO.CodigosError.NOMBRE_USUARIO_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                    }
                }

                @Nested
                @DisplayName("Órdenes inválidas")
                inner class OrdenesInvalidas
                {
                    @Test
                    fun cuando_el_dto_estan_vacias()
                    {
                        val errorApi = assertThrows<EntidadInvalida> {
                            recursoEntidadEspecifica.actualizar(entidadDTO.copy(ordenes = listOf()))
                        }

                        assertEquals(LoteDeOrdenesDTO.CodigosError.ORDENES_INVALIDAS, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                    }
                }

                @Nested
                @DisplayName("Para orden")
                inner class ParaOrden
                {
                    private lateinit var ordenDTOEnPrueba: OrdenDTO

                    @BeforeEach
                    fun obtenerSesionDeManillaEnPrueba()
                    {
                        ordenDTOEnPrueba = entidadDTO.ordenes.first()
                    }

                    private fun actualizarConOrden(nuevaOrden: OrdenDTO): Collection<OrdenDTO>
                    {
                        return recursoEntidadEspecifica.actualizar(entidadDTO.copy(ordenes = listOf(nuevaOrden)))
                    }

                    @Nested
                    @DisplayName("transacciones a crear")
                    inner class TransaccionesACrear
                    {
                        @Test
                        fun cuando_el_dto_estan_vacias()
                        {
                            ordenDTOEnPrueba = ordenDTOEnPrueba.copy(transacciones = listOf())
                            val errorApi = assertThrows<EntidadInvalida> {
                                actualizarConOrden(ordenDTOEnPrueba)
                            }

                            assertEquals(OrdenDTO.CodigosError.TRANSACCIONES_VACIAS, errorApi.codigoInterno)
                            verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                        }
                    }

                    @Nested
                    @DisplayName("Fecha de realizacion invalida")
                    inner class FechaDesactivacionInvalida
                    {
                        @Test
                        fun cuando_el_dto_tiene_una_zona_horaria_diferente_a_la_por_defecto()
                        {
                            ordenDTOEnPrueba =
                                    ordenDTOEnPrueba.copy(fechaDeRealizacion =
                                                          ordenDTOEnPrueba.fechaDeRealizacion.withZoneSameInstant(ZoneId.of("America/Bogota"))
                                                         )
                            val errorApi = assertThrows<EntidadInvalida> {
                                actualizarConOrden(ordenDTOEnPrueba)
                            }

                            assertEquals(OrdenDTO.CodigosError.FECHA_DE_REALIZACION_INVALIDA, errorApi.codigoInterno)
                            verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                        }

                        @Test
                        fun cuando_el_dto_tiene_fecha_de_realizacion_inferior_al_limite_minimo()
                        {
                            ordenDTOEnPrueba = ordenDTOEnPrueba.copy(fechaDeRealizacion = FECHA_MINIMA_CREACION.minusNanos(1))
                            val errorApi = assertThrows<EntidadInvalida> {
                                actualizarConOrden(ordenDTOEnPrueba)
                            }

                            assertEquals(OrdenDTO.CodigosError.FECHA_DE_REALIZACION_INVALIDA, errorApi.codigoInterno)
                            verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                        }
                    }
                }
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "LotesDeOrdenes", PermisoBack.Accion.PUT)
                private val permisoBuscadoDTO = PermisoBackDTO(permisoBuscadoNegocio)

                @Test
                fun el_servicio_de_permisos_posibles_retorna_el_permiso_correspondiente_una_vez()
                {
                    val permisos: Sequence<PermisoBackDTO> = RecursoPermisosPosibles(ID_CLIENTE, mockManejadorSeguridad).darTodas()
                    val numeroPermisos = permisos.count { it == permisoBuscadoDTO }
                    assertEquals(1, numeroPermisos)
                }

                @Test
                fun se_ejecuta_la_operacion_de_bd_cuando_manejador_de_seguridad_no_lanza_excepcion()
                {
                    doReturn(entidadNegocio.ordenes)
                        .`when`(mockRepositorio)
                        .crear(ID_CLIENTE, entidadNegocio)
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteConPermiso(permisoBuscadoNegocio)
                    recursoEntidadEspecifica.actualizar(entidadDTO)
                    verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteNuncaEstaAutenticado
                    assertThrows<UsuarioNoAutenticado> { recursoEntidadEspecifica.actualizar(entidadDTO) }
                    verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso()
                {
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio)
                    assertThrows<UsuarioNoTienePermiso> { recursoEntidadEspecifica.actualizar(entidadDTO) }
                    verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                }
            }
        }

        @Nested
        @DisplayName("Al hacer patch")
        inner class Patch
        {
            private lateinit var entidadDTO: LoteDeOrdenesDTO

            @BeforeEach
            private fun crearEntidadesDePrueba()
            {
                entidadDTO = darEntidadDTOSegunIndice(1)
            }

            @Test
            fun funciona_correctamente_cuando_el_repositorio_actualiza_la_entidad()
            {
                val entidadPatch = TransaccionEntidadTerminadaDTO<LoteDeOrdenes>(true)
                val mapeoDeCamposEsperado =
                        mapOf<String, CampoModificableEntidad<LoteDeOrdenes, *>>(
                                EntidadTransaccional.Campos.CREACION_TERMINADA to EntidadTransaccional.CampoCreacionTerminada(entidadPatch.creacionTerminada)
                                                                                )

                doNothing()
                    .`when`(mockRepositorio)
                    .actualizarCamposIndividuales(ID_CLIENTE, mapeoDeCamposEsperado, IdTransaccionActualizacionTerminacionOrden(entidadDTO.id))

                recursoEntidadEspecifica.patch(entidadPatch)

                verify(mockRepositorio)
                    .actualizarCamposIndividuales(ID_CLIENTE, mapeoDeCamposEsperado, IdTransaccionActualizacionTerminacionOrden(entidadDTO.id))
            }

            @Test
            fun lanza_excepcion_ErrorActualizandoEntidad_con_codigo_interno_error_de_bd_desconocido_cuando_el_repositorio_lanza_ErrorAlActualizarCampo()
            {
                val entidadPatch = TransaccionEntidadTerminadaDTO<LoteDeOrdenes>(true)
                val mapeoDeCamposEsperado =
                        mapOf<String, CampoModificableEntidad<LoteDeOrdenes, *>>(
                                EntidadTransaccional.Campos.CREACION_TERMINADA to EntidadTransaccional.CampoCreacionTerminada(entidadPatch.creacionTerminada)
                                                                                )

                doThrow(ErrorAlActualizarCampo("no importa", "no importa", "no importa"))
                    .`when`(mockRepositorio)
                    .actualizarCamposIndividuales(ID_CLIENTE, mapeoDeCamposEsperado, IdTransaccionActualizacionTerminacionOrden(entidadDTO.id))

                val errorApi = assertThrows<ErrorActualizandoEntidad> { recursoEntidadEspecifica.patch(entidadPatch) }

                assertEquals(LoteDeOrdenesDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                verify(mockRepositorio)
                    .actualizarCamposIndividuales(ID_CLIENTE, mapeoDeCamposEsperado, IdTransaccionActualizacionTerminacionOrden(entidadDTO.id))
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                val entidadPatch = TransaccionEntidadTerminadaDTO<LoteDeOrdenes>(true)
                val mapeoDeCamposEsperado =
                        mapOf<String, CampoModificableEntidad<LoteDeOrdenes, *>>(
                                EntidadTransaccional.Campos.CREACION_TERMINADA to EntidadTransaccional.CampoCreacionTerminada(entidadPatch.creacionTerminada)
                                                                                )

                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .actualizarCamposIndividuales(ID_CLIENTE, mapeoDeCamposEsperado, IdTransaccionActualizacionTerminacionOrden(entidadDTO.id))

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.patch(entidadPatch) }

                assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio)
                    .actualizarCamposIndividuales(ID_CLIENTE, mapeoDeCamposEsperado, IdTransaccionActualizacionTerminacionOrden(entidadDTO.id))
            }

            @Nested
            @DisplayName("Lanzan EntidadInvalida y código interno")
            inner class LanzanExcepcionEntidadInvalida
            {
                @Test
                fun desconocido_cuando_el_repositorio_lanza_ErrorDeCreacionActualizacionEntidad()
                {
                    val entidadPatch = TransaccionEntidadTerminadaDTO<LoteDeOrdenes>(true)
                    doThrow(ErrorDeCreacionActualizacionEntidad("no importa"))
                        .`when`(mockRepositorio)
                        .actualizarCamposIndividuales(
                                ID_CLIENTE,
                                mapOf(EntidadTransaccional.Campos.CREACION_TERMINADA to EntidadTransaccional.CampoCreacionTerminada(entidadPatch.creacionTerminada)),
                                IdTransaccionActualizacionTerminacionOrden(entidadDTO.id)
                                                     )

                    val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.patch(entidadPatch) }

                    assertEquals(LoteDeOrdenesDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                    verify(mockRepositorio)
                        .actualizarCamposIndividuales(
                                ID_CLIENTE,
                                mapOf(EntidadTransaccional.Campos.CREACION_TERMINADA to EntidadTransaccional.CampoCreacionTerminada(entidadPatch.creacionTerminada)),
                                IdTransaccionActualizacionTerminacionOrden(entidadDTO.id)
                                                     )
                }
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "LotesDeOrdenes", PermisoBack.Accion.PATCH)
                private val permisoBuscadoDTO = PermisoBackDTO(permisoBuscadoNegocio)

                @Test
                fun el_servicio_de_permisos_posibles_retorna_el_permiso_correspondiente_una_vez()
                {
                    val permisos: Sequence<PermisoBackDTO> = RecursoPermisosPosibles(ID_CLIENTE, mockManejadorSeguridad).darTodas()
                    val numeroPermisos = permisos.count { it == permisoBuscadoDTO }
                    assertEquals(1, numeroPermisos)
                }

                @Test
                fun se_ejecuta_la_operacion_de_bd_cuando_manejador_de_seguridad_no_lanza_excepcion()
                {
                    val entidadPatch = TransaccionEntidadTerminadaDTO<LoteDeOrdenes>(true)
                    doNothing()
                        .`when`(mockRepositorio)
                        .actualizarCamposIndividuales(
                                ID_CLIENTE,
                                mapOf(EntidadTransaccional.Campos.CREACION_TERMINADA to EntidadTransaccional.CampoCreacionTerminada(entidadPatch.creacionTerminada)),
                                IdTransaccionActualizacionTerminacionOrden(entidadDTO.id)
                                                     )
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteConPermiso(permisoBuscadoNegocio)
                    recursoEntidadEspecifica.patch(entidadPatch)
                    verify(mockRepositorio)
                        .actualizarCamposIndividuales(
                                ID_CLIENTE,
                                mapOf(EntidadTransaccional.Campos.CREACION_TERMINADA to EntidadTransaccional.CampoCreacionTerminada(entidadPatch.creacionTerminada)),
                                IdTransaccionActualizacionTerminacionOrden(entidadDTO.id)
                                                     )
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    val entidadPatch = TransaccionEntidadTerminadaDTO<LoteDeOrdenes>(true)
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteNuncaEstaAutenticado
                    assertThrows<UsuarioNoAutenticado> { recursoEntidadEspecifica.patch(entidadPatch) }
                    verify(mockRepositorio, times(0))
                        .actualizarCamposIndividuales(
                                ID_CLIENTE,
                                mapOf(EntidadTransaccional.Campos.CREACION_TERMINADA to EntidadTransaccional.CampoCreacionTerminada(entidadPatch.creacionTerminada)),
                                IdTransaccionActualizacionTerminacionOrden(entidadDTO.id)
                                                     )
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso()
                {
                    val entidadPatch = TransaccionEntidadTerminadaDTO<LoteDeOrdenes>(true)
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio)
                    assertThrows<UsuarioNoTienePermiso> { recursoEntidadEspecifica.patch(entidadPatch) }
                    verify(mockRepositorio, times(0))
                        .actualizarCamposIndividuales(
                                ID_CLIENTE,
                                mapOf(EntidadTransaccional.Campos.CREACION_TERMINADA to EntidadTransaccional.CampoCreacionTerminada(entidadPatch.creacionTerminada)),
                                IdTransaccionActualizacionTerminacionOrden(entidadDTO.id)
                                                     )
                }
            }
        }
    }
}