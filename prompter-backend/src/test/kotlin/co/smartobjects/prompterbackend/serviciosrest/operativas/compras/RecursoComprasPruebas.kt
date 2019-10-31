package co.smartobjects.prompterbackend.serviciosrest.operativas.compras

import co.smartobjects.entidades.operativas.EntidadTransaccional
import co.smartobjects.entidades.operativas.compras.Compra
import co.smartobjects.entidades.operativas.compras.CreditoFondo
import co.smartobjects.entidades.operativas.compras.Pago
import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.persistencia.excepciones.*
import co.smartobjects.persistencia.excepciones.ErrorEliminandoEntidad
import co.smartobjects.persistencia.operativas.compras.RepositorioCompras
import co.smartobjects.prompterbackend.*
import co.smartobjects.prompterbackend.excepciones.*
import co.smartobjects.prompterbackend.excepciones.EntidadNoExiste
import co.smartobjects.prompterbackend.serviciosrest.clientes.RecursoClientes
import co.smartobjects.prompterbackend.serviciosrest.usuarios.RecursoPermisosPosibles
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.modelos.clientes.ClienteDTO
import co.smartobjects.red.modelos.operativas.EntidadTransaccionalDTO
import co.smartobjects.red.modelos.operativas.TransaccionEntidadTerminadaDTO
import co.smartobjects.red.modelos.operativas.compras.CompraDTO
import co.smartobjects.red.modelos.operativas.compras.CreditoFondoDTO
import co.smartobjects.red.modelos.operativas.compras.CreditoPaqueteDTO
import co.smartobjects.red.modelos.operativas.compras.PagoDTO
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
import kotlin.test.assertTrue


@DisplayName("Recurso Compras")
internal class RecursoComprasPruebas
{
    companion object
    {
        private const val ID_CLIENTE = 1L
        private val UUID_PRUEBAS: UUID = UUID.randomUUID()
        private val ID_ENTIDAD_PRUEBAS = EntidadTransaccional.PartesId(1, "Un usuario", UUID_PRUEBAS).id
    }

    private fun darEntidadNegocioSegunIndice(indice: Int): Compra
    {
        return Compra(
                ID_CLIENTE,
                "Un usuario",
                UUID_PRUEBAS,
                indice.toLong(),
                false,
                listOf(
                        CreditoFondo
                        (
                                ID_CLIENTE,
                                null,
                                Decimal.DIEZ,
                                Decimal.DIEZ,
                                Decimal.DIEZ,
                                ZonedDateTime.of(LocalDate.of(Year.now(ZONA_HORARIA_POR_DEFECTO).value + indice, 1, 1), LocalTime.of(5, 5), ZONA_HORARIA_POR_DEFECTO),
                                ZonedDateTime.of(LocalDate.of(Year.now(ZONA_HORARIA_POR_DEFECTO).value + 2 + indice, 1, 1), LocalTime.of(5, 5), ZONA_HORARIA_POR_DEFECTO),
                                false,
                                "Taquilla",
                                "Un usuario",
                                indice.toLong() + 2,
                                indice.toLong() + 3,
                                "código externo fondo $indice",
                                indice.toLong() + 4,
                                "uuid-pc",
                                indice.toLong() + 5,
                                indice.toLong() + 6
                        )
                      ),
                listOf(),
                listOf(
                        Pago(Decimal.DIEZ, Pago.MetodoDePago.EFECTIVO, indice.toString())
                      ),
                ZonedDateTime.of(LocalDate.of(Year.now(ZONA_HORARIA_POR_DEFECTO).value + 1 + indice, 1, 1), LocalTime.of(5, 5), ZONA_HORARIA_POR_DEFECTO)
                     )
    }

    private fun darEntidadDTOSegunIndice(indice: Int): CompraDTO
    {
        return CompraDTO(darEntidadNegocioSegunIndice(indice))
    }

    @[Nested DisplayName("Al llamar por red")]
    inner class Red
    {
        private lateinit var server: HttpServer
        private lateinit var target: WebTarget
        private lateinit var mockRecursoTodasEntidades: RecursoCompras
        private lateinit var mockRecursoEntidadEspecifica: RecursoCompras.RecursoCompra

        @[BeforeEach Throws(Exception::class)]
        fun antesDeCadaTest()
        {
            mockRecursoEntidadEspecifica = mockConDefaultAnswer(RecursoCompras.RecursoCompra::class.java)
            mockRecursoTodasEntidades = mockConDefaultAnswer(RecursoCompras::class.java)

            val mockRecursoCliente = mockConDefaultAnswer(RecursoClientes.RecursoCliente::class.java)
            val mockRecursoClientes = mockConDefaultAnswer(RecursoClientes::class.java)

            doReturn(mockRecursoCliente).`when`(mockRecursoClientes).darRecursosEntidadEspecifica(ID_CLIENTE)
            doReturn(mockRecursoTodasEntidades).`when`(mockRecursoCliente).darRecursoCompras()
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

        @[Nested DisplayName("Al consultar todos")]
        inner class ConsultarTodos
        {
            @Test
            fun llama_la_funcion_darTodas()
            {
                doReturn(sequenceOf<CompraDTO>()).`when`(mockRecursoTodasEntidades).darTodas()

                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoCompras.RUTA}").request().get(String::class.java)

                verify(mockRecursoTodasEntidades).darTodas()
            }
        }

        @[Nested DisplayName("Al consultar una")]
        inner class ConsultarUna
        {
            @Test
            fun llama_la_funcion_darPorId_con_dto_correcto()
            {
                val entidadPruebas = darEntidadNegocioSegunIndice(1)
                doReturn(CompraDTO(entidadPruebas)).`when`(mockRecursoEntidadEspecifica).darPorId()

                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoCompras.RUTA}/$ID_ENTIDAD_PRUEBAS").request()
                    .get(CompraDTO::class.java)

                verify(mockRecursoEntidadEspecifica).darPorId()
            }
        }

        @[Nested DisplayName("Al actualizar")]
        inner class Actualizar
        {
            @Test
            fun llama_la_funcion_actualizar_con_dto_correcto()
            {
                val dtoPruebas = darEntidadDTOSegunIndice(1)
                val entidadPruebas = darEntidadNegocioSegunIndice(1)
                doReturn(CompraDTO(entidadPruebas)).`when`(mockRecursoEntidadEspecifica).actualizar(dtoPruebas)

                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoCompras.RUTA}/$ID_ENTIDAD_PRUEBAS").request()
                    .put(Entity.entity(dtoPruebas, MediaType.APPLICATION_JSON_TYPE), CompraDTO::class.java)

                verify(mockRecursoEntidadEspecifica).actualizar(dtoPruebas)
            }
        }

        @[Nested DisplayName("Al hacer patch")]
        inner class Patch
        {
            @Test
            fun llama_la_funcion_actualizar_con_dto_correcto()
            {
                val entidadPatch = TransaccionEntidadTerminadaDTO<Compra>(true)
                doNothing().`when`(mockRecursoEntidadEspecifica).patch(entidadPatch)

                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoCompras.RUTA}/$ID_ENTIDAD_PRUEBAS")
                    .request()
                    .patch(entidadPatch, TransaccionEntidadTerminadaDTO::class.java)

                verify(mockRecursoEntidadEspecifica).patch(entidadPatch)
            }
        }

        @[Nested DisplayName("Al eliminar")]
        inner class Eliminar
        {
            @Test
            fun llama_la_funcion_eliminarPorId_con_dto_correcto()
            {
                doNothing().`when`(mockRecursoEntidadEspecifica).eliminarPorId()

                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoCompras.RUTA}/$ID_ENTIDAD_PRUEBAS").request()
                    .delete()

                verify(mockRecursoEntidadEspecifica).eliminarPorId()
            }
        }
    }

    @Nested
    @DisplayName("Al llamar por código")
    inner class Codigo
    {
        private val mockRepositorio = mockConDefaultAnswer(RepositorioCompras::class.java)
        private var mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteSiempreTienePermisos
        private val recursoTodasEntidades: RecursoCompras by lazy { RecursoCompras(ID_CLIENTE, mockRepositorio, mockManejadorSeguridad) }
        private val recursoEntidadEspecifica: RecursoCompras.RecursoCompra by lazy { recursoTodasEntidades.darRecursosEntidadEspecifica(ID_ENTIDAD_PRUEBAS) }


        @Nested
        @DisplayName("Al consultar todos")
        inner class ConsultarTodos
        {
            @Test
            fun retorna_una_lista_vacia_de_dtos_cuando_el_repositorio_retorna_una_lista_vacia()
            {
                doReturn(sequenceOf<Compra>())
                    .`when`(mockRepositorio)
                    .listar(ID_CLIENTE)

                val listaRetornada = recursoTodasEntidades.darTodas()

                assertTrue(listaRetornada.none())
                verify(mockRepositorio).listar(ID_CLIENTE)
            }

            @Test
            fun retorna_una_lista_de_dtos_correcta_cuando_el_repositorio_retorna_una_lista_de_entidades()
            {
                val listaNegocio = List(5) { darEntidadNegocioSegunIndice(it) }
                val listaDTO = List(5) { darEntidadDTOSegunIndice(it) }
                doReturn(listaNegocio.asSequence())
                    .`when`(mockRepositorio)
                    .listar(ID_CLIENTE)

                val listaRetornada = recursoTodasEntidades.darTodas()

                assertEquals(listaDTO, listaRetornada.toList())
                verify(mockRepositorio).listar(ID_CLIENTE)
            }

            @Test
            fun lanza_excepcion_ErrorDesconocido_con_codigo_interno_correcto_cuando_el_repositorio_lanza_ErrorDeConsultaEntidad()
            {
                doThrow(ErrorDeConsultaEntidad("Compra"))
                    .`when`(mockRepositorio)
                    .listar(ID_CLIENTE)

                val errorApi = assertThrows<ErrorDesconocido> { recursoTodasEntidades.darTodas() }

                assertEquals(CompraDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                verify(mockRepositorio).listar(ID_CLIENTE)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .listar(ID_CLIENTE)

                val errorApi = assertThrows<EntidadNoExiste> { recursoTodasEntidades.darTodas() }

                Assertions.assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).listar(ID_CLIENTE)
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Compras", PermisoBack.Accion.GET_TODOS)
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
                    doReturn(sequenceOf<Compra>())
                        .`when`(mockRepositorio)
                        .listar(ID_CLIENTE)
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteConPermiso(permisoBuscadoNegocio)
                    recursoTodasEntidades.darTodas()
                    verify(mockRepositorio).listar(ID_CLIENTE)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteNuncaEstaAutenticado
                    assertThrows<UsuarioNoAutenticado> { recursoTodasEntidades.darTodas() }
                    verify(mockRepositorio, times(0)).listar(ID_CLIENTE)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso()
                {
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio)
                    assertThrows<UsuarioNoTienePermiso> { recursoTodasEntidades.darTodas() }
                    verify(mockRepositorio, times(0)).listar(ID_CLIENTE)
                }
            }
        }

        @Nested
        @DisplayName("Al consultar uno")
        inner class ConsultarUno
        {
            @Test
            fun retorna_el_dto_correcto_cuando_el_repositorio_retorna_un_entidad()
            {
                val entidadNegocio = darEntidadNegocioSegunIndice(1)
                val entidadDTO = darEntidadDTOSegunIndice(1)
                doReturn(entidadNegocio)
                    .`when`(mockRepositorio)
                    .buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)

                val entidadRetornada = recursoEntidadEspecifica.darPorId()

                assertEquals(entidadDTO, entidadRetornada)
                verify(mockRepositorio).buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_correcto_cuando_el_repositorio_retorna_null()
            {
                doReturn(null)
                    .`when`(mockRepositorio)
                    .buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.darPorId() }

                assertEquals(CompraDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)
            }

            @Test
            fun lanza_excepcion_ErrorDesconocido_con_codigo_interno_correcto_cuando_el_repositorio_lanza_ErrorDeConsultaEntidad()
            {
                doThrow(ErrorDeConsultaEntidad("Compra"))
                    .`when`(mockRepositorio)
                    .buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)

                val errorApi = assertThrows<ErrorDesconocido> { recursoEntidadEspecifica.darPorId() }

                assertEquals(CompraDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                verify(mockRepositorio).buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.darPorId() }

                Assertions.assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Compras", PermisoBack.Accion.GET_UNO)
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
                    val entidadNegocio = darEntidadNegocioSegunIndice(1)
                    doReturn(entidadNegocio)
                        .`when`(mockRepositorio)
                        .buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteConPermiso(permisoBuscadoNegocio)
                    recursoEntidadEspecifica.darPorId()
                    verify(mockRepositorio).buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteNuncaEstaAutenticado
                    assertThrows<UsuarioNoAutenticado> { recursoEntidadEspecifica.darPorId() }
                    verify(mockRepositorio, times(0)).buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso()
                {
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio)
                    assertThrows<UsuarioNoTienePermiso> { recursoEntidadEspecifica.darPorId() }
                    verify(mockRepositorio, times(0)).buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)
                }
            }
        }

        @Nested
        @DisplayName("Al crear (PUT)")
        inner class Actualizar
        {
            private lateinit var entidadNegocio: Compra
            private lateinit var entidadDTO: CompraDTO

            @BeforeEach
            private fun crearEntidadesDePrueba()
            {
                entidadNegocio = darEntidadNegocioSegunIndice(1)
                entidadDTO = darEntidadDTOSegunIndice(1)
            }

            @Test
            fun retorna_el_dto_correcto_cuando_el_repositorio_actualiza_la_entidad()
            {
                doReturn(entidadNegocio)
                    .`when`(mockRepositorio)
                    .crear(ID_CLIENTE, entidadNegocio)

                val entidadRetornada = recursoEntidadEspecifica.actualizar(entidadDTO)

                assertEquals(entidadDTO, entidadRetornada)
                verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
            }

            @Test
            fun usa_el_id_de_la_ruta_cuando_el_id_de_la_entidad_no_coincide_con_el_de_la_ruta()
            {
                doReturn(entidadNegocio)
                    .`when`(mockRepositorio)
                    .crear(ID_CLIENTE, entidadNegocio)

                val entidadRetornada = recursoEntidadEspecifica.actualizar(entidadDTO.copy(id = EntidadTransaccional.PartesId(1234567L, "Otro Usuario", UUID.randomUUID()).id))

                assertEquals(entidadDTO, entidadRetornada)
                verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_entidad_referenciada_no_existe_cuando_el_repositorio_lanza_ErrorDeLlaveForanea()
            {
                doThrow(ErrorDeLlaveForanea(0, "Alguien no existe"))
                    .`when`(mockRepositorio)
                    .crear(ID_CLIENTE, entidadNegocio)

                val errorApi = assertThrows<EntidadReferenciadaNoExiste> { recursoEntidadEspecifica.actualizar(entidadDTO) }

                assertEquals(CompraDTO.CodigosError.ENTIDAD_REFERENCIADA_NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .crear(ID_CLIENTE, entidadNegocio)

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.actualizar(entidadDTO) }

                Assertions.assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
            }

            @Nested
            @DisplayName("Lanzan EntidadInvalida y código interno")
            inner class LanzanExcepcionEntidadInvalida
            {
                @Test
                fun entidad_duplicada_cuando_el_repositorio_lanza_ErrorCreacionActualizacionPorDuplicidad()
                {
                    doThrow(ErrorCreacionActualizacionPorDuplicidad("Compra"))
                        .`when`(mockRepositorio)
                        .crear(ID_CLIENTE, entidadNegocio)

                    val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTO) }

                    assertEquals(CompraDTO.CodigosError.ENTIDAD_DUPLICADA_EN_BD, errorApi.codigoInterno)
                    verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
                }

                @Test
                fun desconocido_cuando_el_repositorio_lanza_ErrorDeCreacionActualizacionEntidad()
                {
                    doThrow(ErrorDeCreacionActualizacionEntidad("Error creando Compra"))
                        .`when`(mockRepositorio)
                        .crear(ID_CLIENTE, entidadNegocio)

                    val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTO) }

                    assertEquals(CompraDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
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
                @DisplayName("Fecha Realizacion invalida")
                inner class FechaRealizacionInvalida
                {
                    @Test
                    fun cuando_el_dto_tiene_una_zona_horaria_diferente_a_la_por_defecto()
                    {
                        val entidadDTOErronea = entidadDTO.copy(fechaDeRealizacion = entidadDTO.fechaDeRealizacion.withZoneSameInstant(ZoneId.of("America/Bogota")))
                        val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTOErronea) }

                        assertEquals(CompraDTO.CodigosError.FECHA_REALIZACION_INVALIDA, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                    }
                }

                @Nested
                @DisplayName("Creditos inválidos")
                inner class CreditosInvalidos
                {
                    @Test
                    fun cuando_el_dto_tiene_creditos_vacio()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTO.copy(creditosFondos = listOf(), creditosPaquetes = listOf())) }

                        assertEquals(CompraDTO.CodigosError.CREDITOS_INVALIDOS, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                    }
                }

                @Nested
                @DisplayName("Pagos inválidos")
                inner class PagosInvalidos
                {
                    @Test
                    fun cuando_el_dto_tiene_pagos_vacios()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTO.copy(pagos = listOf())) }

                        assertEquals(CompraDTO.CodigosError.PAGOS_INVALIDOS, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                    }

                    @Test
                    fun entidad_duplicada_cuando_el_repositorio_lanza_ErrorActualizacionViolacionDeRestriccion()
                    {
                        doThrow(ErrorActualizacionViolacionDeRestriccion("no importa", "no importa", "no importa", arrayOf("")))
                            .`when`(mockRepositorio)
                            .crear(ID_CLIENTE, entidadNegocio)

                        val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTO) }

                        assertEquals(CompraDTO.CodigosError.PAGOS_CON_NUMERO_DE_TRANSACCION_POS_REPETIDOS, errorApi.codigoInterno)
                        verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
                    }
                }

                @Nested
                @DisplayName("En crédito paquete")
                inner class EnCreditoPaquete
                {
                    @Nested
                    @DisplayName("Creditos inválidos")
                    inner class CreditosInvalidos
                    {
                        @Test
                        fun cuando_el_dto_tiene_creditos_vacio()
                        {
                            val errorApi = assertThrows<EntidadInvalida> {
                                val creditoPaqueteDTO = CreditoPaqueteDTO(1, "código externo paquete", listOf())

                                recursoEntidadEspecifica
                                    .actualizar(entidadDTO.copy(creditosPaquetes = listOf(creditoPaqueteDTO)))
                            }

                            assertEquals(CreditoPaqueteDTO.CodigosError.CREDITOS_FONDOS_INVALIDOS, errorApi.codigoInterno)
                            verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                        }
                    }
                }

                @Nested
                @DisplayName("En credito fondo")
                inner class EnCreditoFondo
                {
                    @Nested
                    @DisplayName("Cantidad inválida")
                    inner class CantidadInvalida
                    {
                        @Test
                        fun cuando_el_dto_tiene_valor_pago_negativo()
                        {
                            val creditoPrueba = entidadDTO.creditosFondos.first().copy(cantidad = Decimal(-1))
                            val errorApi = assertThrows<EntidadInvalida> {
                                recursoEntidadEspecifica.actualizar(entidadDTO.copy(creditosFondos = listOf(creditoPrueba)))
                            }

                            assertEquals(CreditoFondoDTO.CodigosError.CANTIDAD_INVALIDA, errorApi.codigoInterno)
                            verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                        }
                    }

                    @Nested
                    @DisplayName("Valor base pagado inválido")
                    inner class ValorBaseInvalido
                    {
                        @Test
                        fun cuando_el_dto_tiene_valor_pago_negativo()
                        {
                            val creditoPrueba = entidadDTO.creditosFondos.first().copy(valorBasePagado = Decimal(-1))
                            val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTO.copy(creditosFondos = listOf(creditoPrueba))) }

                            assertEquals(CreditoFondoDTO.CodigosError.VALOR_BASE_PAGADO_INVALIDO, errorApi.codigoInterno)
                            verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                        }
                    }

                    @Nested
                    @DisplayName("Valor impuesto inválido")
                    inner class ValorImpuestoInvalido
                    {
                        @Test
                        fun cuando_el_dto_tiene_valor_pago_negativo()
                        {
                            val creditoPrueba = entidadDTO.creditosFondos.first().copy(valorImpuestoPagado = Decimal(-1))
                            val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTO.copy(creditosFondos = listOf(creditoPrueba))) }

                            assertEquals(CreditoFondoDTO.CodigosError.VALOR_IMPUESTO_PAGADO_INVALIDO, errorApi.codigoInterno)
                            verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                        }
                    }

                    @Nested
                    @DisplayName("Origen inválido")
                    inner class OrigenInvalido
                    {
                        @Test
                        fun cuando_el_dto_es_vacio()
                        {
                            val creditoPrueba = entidadDTO.creditosFondos.first().copy(origen = "")
                            val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTO.copy(creditosFondos = listOf(creditoPrueba))) }

                            assertEquals(CreditoFondoDTO.CodigosError.ORIGEN_INVALIDO, errorApi.codigoInterno)
                            verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                        }

                        @Test
                        fun cuando_el_dto_tiene_solo_espacios_y_tabs()
                        {
                            val creditoPrueba = entidadDTO.creditosFondos.first().copy(origen = "      ")
                            val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTO.copy(creditosFondos = listOf(creditoPrueba))) }

                            assertEquals(CreditoFondoDTO.CodigosError.ORIGEN_INVALIDO, errorApi.codigoInterno)
                            verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                        }
                    }

                    @Nested
                    @DisplayName("Id dispositivo inválido")
                    inner class IdDispositivoInvalido
                    {
                        @Test
                        fun cuando_el_dto_es_vacio()
                        {
                            val creditoPrueba = entidadDTO.creditosFondos.first().copy(idDispositivo = "")
                            val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTO.copy(creditosFondos = listOf(creditoPrueba))) }

                            assertEquals(CreditoFondoDTO.CodigosError.ID_DISPOSITIVO_INVALIDO, errorApi.codigoInterno)
                            verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                        }

                        @Test
                        fun cuando_el_dto_tiene_solo_espacios_y_tabs()
                        {
                            val creditoPrueba = entidadDTO.creditosFondos.first().copy(idDispositivo = "      ")
                            val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTO.copy(creditosFondos = listOf(creditoPrueba))) }

                            assertEquals(CreditoFondoDTO.CodigosError.ID_DISPOSITIVO_INVALIDO, errorApi.codigoInterno)
                            verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                        }
                    }

                    @Nested
                    @DisplayName("Fecha desde invalida")
                    inner class FechaDesdeInvalida
                    {
                        @Test
                        fun cuando_el_dto_tiene_una_zona_horaria_diferente_a_la_por_defecto()
                        {
                            val creditoPrueba = entidadDTO.creditosFondos.first().copy(validoDesde = entidadDTO.creditosFondos.first().validoDesde!!.withZoneSameInstant(ZoneId.of("America/Bogota")))
                            val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTO.copy(creditosFondos = listOf(creditoPrueba))) }

                            assertEquals(CreditoFondoDTO.CodigosError.FECHA_VALIDEZ_DESDE_INVALIDA, errorApi.codigoInterno)
                            verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                        }

                        @Test
                        fun cuando_en_el_dto_es_inferior_a_la_fecha_minima_creacion_COMPRA()
                        {
                            val fechaErronea = FECHA_MINIMA_CREACION.minusNanos(1)
                            val creditoPrueba = entidadDTO.creditosFondos.first().copy(validoDesde = fechaErronea)
                            val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTO.copy(creditosFondos = listOf(creditoPrueba))) }

                            assertEquals(CreditoFondoDTO.CodigosError.FECHA_VALIDEZ_DESDE_INVALIDA, errorApi.codigoInterno)
                            verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                        }

                        @Test
                        fun cuando_en_el_dto_es_superior_a_la_fecha_final()
                        {
                            val fechaErronea = entidadDTO.creditosFondos.first().validoHasta!!.plusDays(10)
                            val creditoPrueba = entidadDTO.creditosFondos.first().copy(validoDesde = fechaErronea)
                            val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTO.copy(creditosFondos = listOf(creditoPrueba))) }

                            assertEquals(CreditoFondoDTO.CodigosError.FECHA_VALIDEZ_DESDE_INVALIDA, errorApi.codigoInterno)
                            verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                        }
                    }

                    @Nested
                    @DisplayName("Fecha Hasta invalida")
                    inner class FechaHastaInvalida
                    {
                        @Test
                        fun cuando_el_dto_tiene_una_zona_horaria_diferente_a_la_por_defecto()
                        {
                            val creditoPrueba = entidadDTO.creditosFondos.first().copy(validoHasta = entidadDTO.creditosFondos.first().validoHasta!!.withZoneSameInstant(ZoneId.of("America/Bogota")))
                            val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTO.copy(creditosFondos = listOf(creditoPrueba))) }

                            assertEquals(CreditoFondoDTO.CodigosError.FECHA_VALIDEZ_HASTA_INVALIDA, errorApi.codigoInterno)
                            verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                        }
                    }
                }

                @Nested
                @DisplayName("En pago")
                inner class EnPago
                {
                    @Nested
                    @DisplayName("Valor pago inválido")
                    inner class ValorPagoInvalidos
                    {
                        @Test
                        fun cuando_el_dto_tiene_valor_pago_negativo()
                        {
                            val pagoPrueba = PagoDTO(Decimal(-1), PagoDTO.MetodoDePago.EFECTIVO, "12-3")
                            val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTO.copy(pagos = listOf(pagoPrueba))) }

                            assertEquals(PagoDTO.CodigosError.VALOR_PAGADO_INVALIDO, errorApi.codigoInterno)
                            verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                        }
                    }

                    @Nested
                    @DisplayName("Numero transacción inválido")
                    inner class NumeroTransaccionInvalido
                    {
                        @Test
                        fun cuando_el_dto_es_vacio()
                        {
                            val pagoPrueba = PagoDTO(Decimal(10), PagoDTO.MetodoDePago.EFECTIVO, "")
                            val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTO.copy(pagos = listOf(pagoPrueba))) }

                            assertEquals(PagoDTO.CodigosError.NUMERO_TRANSACCION_POS_INVALIDO, errorApi.codigoInterno)
                            verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                        }

                        @Test
                        fun cuando_el_dto_tiene_solo_espacios_y_tabs()
                        {
                            val pagoPrueba = PagoDTO(Decimal(10), PagoDTO.MetodoDePago.EFECTIVO, "      ")
                            val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTO.copy(pagos = listOf(pagoPrueba))) }

                            assertEquals(PagoDTO.CodigosError.NUMERO_TRANSACCION_POS_INVALIDO, errorApi.codigoInterno)
                            verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                        }
                    }
                }
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Compras", PermisoBack.Accion.PUT)
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
                    doReturn(entidadNegocio)
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
            private lateinit var entidadNegocio: Compra
            private lateinit var entidadDTO: CompraDTO

            @BeforeEach
            private fun crearEntidadesDePrueba()
            {
                entidadNegocio = darEntidadNegocioSegunIndice(1)
                entidadDTO = darEntidadDTOSegunIndice(1)
            }

            @Test
            fun funciona_correctamente_cuando_el_repositorio_actualiza_la_entidad()
            {
                val entidadPatch = TransaccionEntidadTerminadaDTO<Compra>(true)
                doNothing()
                    .`when`(mockRepositorio)
                    .actualizarCamposIndividuales(
                            ID_CLIENTE,
                            entidadDTO.id,
                            mapOf(EntidadTransaccional.Campos.CREACION_TERMINADA to EntidadTransaccional.CampoCreacionTerminada(entidadPatch.creacionTerminada))
                                                 )

                recursoEntidadEspecifica.patch(entidadPatch)

                verify(mockRepositorio)
                    .actualizarCamposIndividuales(
                            ID_CLIENTE,
                            entidadDTO.id,
                            mapOf(EntidadTransaccional.Campos.CREACION_TERMINADA to EntidadTransaccional.CampoCreacionTerminada(entidadPatch.creacionTerminada))
                                                 )
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EntidadNoExiste()
            {
                val entidadPatch = TransaccionEntidadTerminadaDTO<Compra>(true)
                doThrow(co.smartobjects.persistencia.excepciones.EntidadNoExiste(entidadDTO.id, "no importa"))
                    .`when`(mockRepositorio)
                    .actualizarCamposIndividuales(
                            ID_CLIENTE,
                            entidadDTO.id,
                            mapOf(EntidadTransaccional.Campos.CREACION_TERMINADA to EntidadTransaccional.CampoCreacionTerminada(entidadPatch.creacionTerminada))
                                                 )

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.patch(entidadPatch) }

                assertEquals(CompraDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio)
                    .actualizarCamposIndividuales(
                            ID_CLIENTE,
                            entidadDTO.id,
                            mapOf(EntidadTransaccional.Campos.CREACION_TERMINADA to EntidadTransaccional.CampoCreacionTerminada(entidadPatch.creacionTerminada))
                                                 )
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                val entidadPatch = TransaccionEntidadTerminadaDTO<Compra>(true)
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .actualizarCamposIndividuales(
                            ID_CLIENTE,
                            entidadDTO.id,
                            mapOf(EntidadTransaccional.Campos.CREACION_TERMINADA to EntidadTransaccional.CampoCreacionTerminada(entidadPatch.creacionTerminada))
                                                 )

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.patch(entidadPatch) }

                Assertions.assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio)
                    .actualizarCamposIndividuales(
                            ID_CLIENTE,
                            entidadDTO.id,
                            mapOf(EntidadTransaccional.Campos.CREACION_TERMINADA to EntidadTransaccional.CampoCreacionTerminada(entidadPatch.creacionTerminada))
                                                 )
            }

            @Nested
            @DisplayName("Lanzan EntidadInvalida y código interno")
            inner class LanzanExcepcionEntidadInvalida
            {
                @Test
                fun desconocido_cuando_el_repositorio_lanza_ErrorDeCreacionActualizacionEntidad()
                {
                    val entidadPatch = TransaccionEntidadTerminadaDTO<Compra>(true)
                    doThrow(ErrorDeCreacionActualizacionEntidad("no importa"))
                        .`when`(mockRepositorio)
                        .actualizarCamposIndividuales(
                                ID_CLIENTE,
                                entidadDTO.id,
                                mapOf(EntidadTransaccional.Campos.CREACION_TERMINADA to EntidadTransaccional.CampoCreacionTerminada(entidadPatch.creacionTerminada))
                                                     )

                    val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.patch(entidadPatch) }

                    assertEquals(CompraDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                    verify(mockRepositorio)
                        .actualizarCamposIndividuales(
                                ID_CLIENTE,
                                entidadDTO.id,
                                mapOf(EntidadTransaccional.Campos.CREACION_TERMINADA to EntidadTransaccional.CampoCreacionTerminada(entidadPatch.creacionTerminada))
                                                     )
                }
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Compras", PermisoBack.Accion.PATCH)
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
                    val entidadPatch = TransaccionEntidadTerminadaDTO<Compra>(true)
                    doNothing()
                        .`when`(mockRepositorio)
                        .actualizarCamposIndividuales(
                                ID_CLIENTE,
                                entidadDTO.id,
                                mapOf(EntidadTransaccional.Campos.CREACION_TERMINADA to EntidadTransaccional.CampoCreacionTerminada(entidadPatch.creacionTerminada))
                                                     )
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteConPermiso(permisoBuscadoNegocio)
                    recursoEntidadEspecifica.patch(entidadPatch)
                    verify(mockRepositorio)
                        .actualizarCamposIndividuales(
                                ID_CLIENTE,
                                entidadDTO.id,
                                mapOf(EntidadTransaccional.Campos.CREACION_TERMINADA to EntidadTransaccional.CampoCreacionTerminada(entidadPatch.creacionTerminada))
                                                     )
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    val entidadPatch = TransaccionEntidadTerminadaDTO<Compra>(true)
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteNuncaEstaAutenticado
                    assertThrows<UsuarioNoAutenticado> { recursoEntidadEspecifica.patch(entidadPatch) }
                    verify(mockRepositorio, times(0))
                        .actualizarCamposIndividuales(
                                ID_CLIENTE,
                                entidadDTO.id,
                                mapOf(EntidadTransaccional.Campos.CREACION_TERMINADA to EntidadTransaccional.CampoCreacionTerminada(entidadPatch.creacionTerminada))
                                                     )
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso()
                {
                    val entidadPatch = TransaccionEntidadTerminadaDTO<Compra>(true)
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio)
                    assertThrows<UsuarioNoTienePermiso> { recursoEntidadEspecifica.patch(entidadPatch) }
                    verify(mockRepositorio, times(0))
                        .actualizarCamposIndividuales(
                                ID_CLIENTE,
                                entidadDTO.id,
                                mapOf(EntidadTransaccional.Campos.CREACION_TERMINADA to EntidadTransaccional.CampoCreacionTerminada(entidadPatch.creacionTerminada))
                                                     )
                }
            }
        }

        @Nested
        @DisplayName("Al eliminar")
        inner class Eliminar
        {
            private lateinit var entidadNegocio: Compra
            private lateinit var entidadDTO: CompraDTO

            @BeforeEach
            private fun crearEntidadesDePrueba()
            {
                entidadNegocio = darEntidadNegocioSegunIndice(1)
                entidadDTO = darEntidadDTOSegunIndice(1)
            }

            @Test
            fun no_lanza_excepcion_cuando_el_repositorio_retorna_true()
            {
                doReturn(true)
                    .`when`(mockRepositorio)
                    .eliminarPorId(ID_CLIENTE, entidadNegocio.id)

                recursoEntidadEspecifica.eliminarPorId()
                verify(mockRepositorio).eliminarPorId(ID_CLIENTE, entidadNegocio.id)
            }

            @Test
            fun lanza_excepcion_con_codigo_interno_no_existe_cuando_el_repositorio_retorna_false()
            {
                doReturn(false)
                    .`when`(mockRepositorio)
                    .eliminarPorId(ID_CLIENTE, entidadNegocio.id)

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.eliminarPorId() }

                assertEquals(CompraDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).eliminarPorId(ID_CLIENTE, entidadNegocio.id)
            }

            @Test
            fun lanza_excepcion_con_codigo_interno_entidad_referenciada_cuando_el_repositorio_lanza_ErrorDeLlaveForanea()
            {
                doThrow(ErrorDeLlaveForanea(0, "Error eliminando acceso"))
                    .`when`(mockRepositorio)
                    .eliminarPorId(ID_CLIENTE, entidadNegocio.id)

                val errorApi = assertThrows<co.smartobjects.prompterbackend.excepciones.ErrorEliminandoEntidad> { recursoEntidadEspecifica.eliminarPorId() }

                assertEquals(CompraDTO.CodigosError.ENTIDAD_REFERENCIADA, errorApi.codigoInterno)
                verify(mockRepositorio).eliminarPorId(ID_CLIENTE, entidadNegocio.id)
            }

            @Test
            fun lanza_excepcion_con_codigo_interno_desconocido_cuando_el_repositorio_lanza_ErrorEliminandoEntidad()
            {
                doThrow(ErrorEliminandoEntidad(1, "Nombre entidad"))
                    .`when`(mockRepositorio)
                    .eliminarPorId(ID_CLIENTE, entidadNegocio.id)

                val errorApi = assertThrows<co.smartobjects.prompterbackend.excepciones.ErrorEliminandoEntidad> { recursoEntidadEspecifica.eliminarPorId() }

                assertEquals(CompraDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                verify(mockRepositorio).eliminarPorId(ID_CLIENTE, entidadNegocio.id)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .eliminarPorId(ID_CLIENTE, entidadNegocio.id)

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.eliminarPorId() }

                Assertions.assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).eliminarPorId(ID_CLIENTE, entidadNegocio.id)
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Compras", PermisoBack.Accion.DELETE)
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
                    doReturn(true)
                        .`when`(mockRepositorio)
                        .eliminarPorId(ID_CLIENTE, entidadNegocio.id)
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteConPermiso(permisoBuscadoNegocio)
                    recursoEntidadEspecifica.eliminarPorId()
                    verify(mockRepositorio).eliminarPorId(ID_CLIENTE, entidadNegocio.id)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteNuncaEstaAutenticado
                    assertThrows<UsuarioNoAutenticado> { recursoEntidadEspecifica.eliminarPorId() }
                    verify(mockRepositorio, times(0)).eliminarPorId(ID_CLIENTE, entidadNegocio.id)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso()
                {
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio)
                    assertThrows<UsuarioNoTienePermiso> { recursoEntidadEspecifica.eliminarPorId() }
                    verify(mockRepositorio, times(0)).eliminarPorId(ID_CLIENTE, entidadNegocio.id)
                }
            }
        }
    }
}