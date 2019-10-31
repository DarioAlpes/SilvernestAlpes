package co.smartobjects.prompterbackend.serviciosrest.operativas.reservas

import co.smartobjects.campos.CampoModificableEntidad
import co.smartobjects.entidades.operativas.EntidadTransaccional
import co.smartobjects.entidades.operativas.reservas.Reserva
import co.smartobjects.entidades.operativas.reservas.SesionDeManilla
import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.persistencia.excepciones.*
import co.smartobjects.persistencia.excepciones.ErrorEliminandoEntidad
import co.smartobjects.persistencia.operativas.reservas.RepositorioReservas
import co.smartobjects.prompterbackend.*
import co.smartobjects.prompterbackend.excepciones.*
import co.smartobjects.prompterbackend.excepciones.EntidadNoExiste
import co.smartobjects.prompterbackend.serviciosrest.clientes.RecursoClientes
import co.smartobjects.prompterbackend.serviciosrest.usuarios.RecursoPermisosPosibles
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.modelos.clientes.ClienteDTO
import co.smartobjects.red.modelos.operativas.EntidadTransaccionalDTO
import co.smartobjects.red.modelos.operativas.TransaccionEntidadTerminadaDTO
import co.smartobjects.red.modelos.operativas.reservas.ReservaDTO
import co.smartobjects.red.modelos.operativas.reservas.SesionDeManillaDTO
import co.smartobjects.red.modelos.usuarios.PermisoBackDTO
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


@DisplayName("Recurso Reservas")
internal class RecursoReservasPruebas
{
    companion object
    {
        private const val ID_CLIENTE = 1L
        private val UUID_PRUEBAS: UUID = UUID.randomUUID()
        private val ID_ENTIDAD_PRUEBAS = EntidadTransaccional.PartesId(1, "Un usuario", UUID_PRUEBAS).id
    }

    private fun darEntidadNegocioSegunIndice(indice: Int): Reserva
    {
        return Reserva(
                ID_CLIENTE,
                "Un usuario",
                UUID_PRUEBAS,
                indice.toLong(),
                true,
                indice.toLong(),
                List(indice + 1) {
                    SesionDeManilla(
                            ID_CLIENTE,
                            (indice + it).toLong(),
                            (indice + it).toLong(),
                            ByteArray(it + 1) { (indice + it).toByte() },
                            ZonedDateTime.of(LocalDate.of(Year.now(ZONA_HORARIA_POR_DEFECTO).value + indice, 1, 1), LocalTime.of(5, 5), ZONA_HORARIA_POR_DEFECTO),
                            ZonedDateTime.of(LocalDate.of(Year.now(ZONA_HORARIA_POR_DEFECTO).value + 2 + indice, 1, 1), LocalTime.of(5, 5), ZONA_HORARIA_POR_DEFECTO),
                            List(it + 1) { it.toLong() }.toSet()
                                   )
                }
                      )
    }

    private fun darEntidadDTOSegunIndice(indice: Int): ReservaDTO
    {
        return ReservaDTO(darEntidadNegocioSegunIndice(indice))
    }

    @[Nested DisplayName("Al llamar por red")]
    inner class Red
    {
        private lateinit var server: HttpServer
        private lateinit var target: WebTarget
        private lateinit var mockRecursoTodasEntidades: RecursoReservas
        private lateinit var mockRecursoEntidadEspecifica: RecursoReservas.RecursoReserva

        @[BeforeEach Throws(Exception::class)]
        fun antesDeCadaTest()
        {
            mockRecursoEntidadEspecifica = mockConDefaultAnswer(RecursoReservas.RecursoReserva::class.java)
            mockRecursoTodasEntidades = mockConDefaultAnswer(RecursoReservas::class.java)

            val mockRecursoCliente = mockConDefaultAnswer(RecursoClientes.RecursoCliente::class.java)
            val mockRecursoClientes = mockConDefaultAnswer(RecursoClientes::class.java)

            doReturn(mockRecursoCliente).`when`(mockRecursoClientes).darRecursosEntidadEspecifica(ID_CLIENTE)
            doReturn(mockRecursoTodasEntidades).`when`(mockRecursoCliente).darRecursoReservas()
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
                doReturn(sequenceOf<ReservaDTO>()).`when`(mockRecursoTodasEntidades).darTodas()

                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoReservas.RUTA}").request().get(String::class.java)

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
                doReturn(ReservaDTO(entidadPruebas)).`when`(mockRecursoEntidadEspecifica).darPorId()

                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoReservas.RUTA}/$ID_ENTIDAD_PRUEBAS").request()
                    .get(ReservaDTO::class.java)

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
                doReturn(ReservaDTO(entidadPruebas)).`when`(mockRecursoEntidadEspecifica).actualizar(dtoPruebas)

                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoReservas.RUTA}/$ID_ENTIDAD_PRUEBAS").request()
                    .put(Entity.entity(dtoPruebas, MediaType.APPLICATION_JSON_TYPE), ReservaDTO::class.java)

                verify(mockRecursoEntidadEspecifica).actualizar(dtoPruebas)
            }
        }

        @[Nested DisplayName("Al hacer patch")]
        inner class Patch
        {
            @Test
            fun llama_la_funcion_actualizar_con_dto_correcto()
            {
                val entidadPatch = TransaccionEntidadTerminadaDTO<Reserva>(true)
                doNothing().`when`(mockRecursoEntidadEspecifica).patch(entidadPatch)

                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoReservas.RUTA}/$ID_ENTIDAD_PRUEBAS")
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

                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoReservas.RUTA}/$ID_ENTIDAD_PRUEBAS").request()
                    .delete()

                verify(mockRecursoEntidadEspecifica).eliminarPorId()
            }
        }
    }

    @[Nested DisplayName("Al llamar por código")]
    inner class Codigo
    {
        private val mockRepositorio = mockConDefaultAnswer(RepositorioReservas::class.java)
        private var mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteSiempreTienePermisos
        private val recursoTodasEntidades: RecursoReservas by lazy { RecursoReservas(ID_CLIENTE, mockRepositorio, mockManejadorSeguridad) }
        private val recursoEntidadEspecifica: RecursoReservas.RecursoReserva by lazy { recursoTodasEntidades.darRecursosEntidadEspecifica(ID_ENTIDAD_PRUEBAS) }


        @Nested
        @DisplayName("Al consultar todos")
        inner class ConsultarTodos
        {
            @Test
            fun retorna_una_lista_vacia_de_dtos_cuando_el_repositorio_retorna_una_lista_vacia()
            {
                doReturn(sequenceOf<Reserva>())
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
                doThrow(ErrorDeConsultaEntidad("Reserva"))
                    .`when`(mockRepositorio)
                    .listar(ID_CLIENTE)

                val errorApi = assertThrows<ErrorDesconocido> { recursoTodasEntidades.darTodas() }

                assertEquals(ReservaDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                verify(mockRepositorio).listar(ID_CLIENTE)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .listar(ID_CLIENTE)

                val errorApi = assertThrows<EntidadNoExiste> { recursoTodasEntidades.darTodas() }

                assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).listar(ID_CLIENTE)
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Reservas", PermisoBack.Accion.GET_TODOS)
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
                    doReturn(sequenceOf<Reserva>())
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

                assertEquals(ReservaDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)
            }

            @Test
            fun lanza_excepcion_ErrorDesconocido_con_codigo_interno_correcto_cuando_el_repositorio_lanza_ErrorDeConsultaEntidad()
            {
                doThrow(ErrorDeConsultaEntidad("Reserva"))
                    .`when`(mockRepositorio)
                    .buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)

                val errorApi = assertThrows<ErrorDesconocido> { recursoEntidadEspecifica.darPorId() }

                assertEquals(ReservaDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                verify(mockRepositorio).buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.darPorId() }

                assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Reservas", PermisoBack.Accion.GET_UNO)
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
            private lateinit var entidadNegocio: Reserva
            private lateinit var entidadDTO: ReservaDTO

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

                assertEquals(ReservaDTO.CodigosError.ENTIDAD_REFERENCIADA_NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_no_existe_cuando_el_repositorio_lanza_EntidadNoExiste()
            {
                doThrow(co.smartobjects.persistencia.excepciones.EntidadNoExiste(Random().nextLong(), "no importa"))
                    .`when`(mockRepositorio)
                    .crear(ID_CLIENTE, entidadNegocio)

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.actualizar(entidadDTO) }

                assertEquals(ReservaDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
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
                    doThrow(ErrorCreacionActualizacionPorDuplicidad("Reserva"))
                        .`when`(mockRepositorio)
                        .crear(ID_CLIENTE, entidadNegocio)

                    val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTO) }

                    assertEquals(ReservaDTO.CodigosError.ENTIDAD_DUPLICADA_EN_BD, errorApi.codigoInterno)
                    verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
                }

                @Test
                fun desconocido_cuando_el_repositorio_lanza_ErrorDeCreacionActualizacionEntidad()
                {
                    doThrow(ErrorDeCreacionActualizacionEntidad("Error creando Reserva"))
                        .`when`(mockRepositorio)
                        .crear(ID_CLIENTE, entidadNegocio)

                    val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTO) }

                    assertEquals(ReservaDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                    verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
                }

                @Test
                fun cuando_el_repositorio_lanza_ErrorCreacionViolacionDeRestriccion()
                {
                    doThrow(ErrorCreacionViolacionDeRestriccion("no importa", "no importa", arrayOf()))
                        .`when`(mockRepositorio)
                        .crear(ID_CLIENTE, entidadNegocio)

                    val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTO) }

                    assertEquals(ReservaDTO.CodigosError.SESIONES_DE_MANILLAS_INVALIDAS, errorApi.codigoInterno)
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
                @DisplayName("Sesiones de manilla inválidas")
                inner class SesionesDeManillaInvalidas
                {
                    @Test
                    fun cuando_el_dto_estan_vacias()
                    {
                        val errorApi = assertThrows<EntidadInvalida> {
                            recursoEntidadEspecifica.actualizar(entidadDTO.copy(sesionesDeManilla = listOf()))
                        }

                        assertEquals(ReservaDTO.CodigosError.SESIONES_DE_MANILLAS_INVALIDAS, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                    }
                }

                @Nested
                @DisplayName("Para sesión de manilla")
                inner class ParaSesionDeManilla
                {
                    private lateinit var sesionDeManillaDTOEnPrueba: SesionDeManillaDTO

                    @BeforeEach
                    fun obtenerSesionDeManillaEnPrueba()
                    {
                        sesionDeManillaDTOEnPrueba = entidadDTO.sesionesDeManilla.first()
                    }

                    private fun actualizarConSesionDeManilla(sesionDeManillaDTOEnPrueba: SesionDeManillaDTO): ReservaDTO
                    {
                        return recursoEntidadEspecifica.actualizar(entidadDTO.copy(sesionesDeManilla = listOf(sesionDeManillaDTOEnPrueba)))
                    }

                    @Nested
                    @DisplayName("Uuid tag")
                    inner class UuidTagInvalido
                    {
                        @Test
                        fun cuando_el_dto_es_vacio()
                        {
                            sesionDeManillaDTOEnPrueba = sesionDeManillaDTOEnPrueba.copy(uuidTag = byteArrayOf())
                            val errorApi = assertThrows<EntidadInvalida> {
                                actualizarConSesionDeManilla(sesionDeManillaDTOEnPrueba)
                            }

                            assertEquals(SesionDeManillaDTO.CodigosError.UUID_TAG_VACIO, errorApi.codigoInterno)
                            verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                        }
                    }

                    @Nested
                    @DisplayName("Fecha de activación invalida")
                    inner class FechaActivacionInvalida
                    {
                        @Test
                        fun cuando_el_dto_tiene_una_zona_horaria_diferente_a_la_por_defecto()
                        {
                            sesionDeManillaDTOEnPrueba =
                                    sesionDeManillaDTOEnPrueba.copy(fechaActivacion = sesionDeManillaDTOEnPrueba.fechaActivacion!!.withZoneSameInstant(ZoneId.of("America/Bogota")))
                            val errorApi = assertThrows<EntidadInvalida> {
                                actualizarConSesionDeManilla(sesionDeManillaDTOEnPrueba)
                            }

                            assertEquals(SesionDeManillaDTO.CodigosError.FECHA_DE_ACTIVACION_INVALIDA, errorApi.codigoInterno)
                            verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                        }

                        @Test
                        fun cuando_en_el_dto_es_superior_a_la_fecha_de_desactivacion()
                        {
                            val fechaErronea = sesionDeManillaDTOEnPrueba.fechaDesactivacion!!.plusDays(10)
                            sesionDeManillaDTOEnPrueba = sesionDeManillaDTOEnPrueba.copy(fechaActivacion = fechaErronea)
                            val errorApi = assertThrows<EntidadInvalida> {
                                actualizarConSesionDeManilla(sesionDeManillaDTOEnPrueba)
                            }

                            assertEquals(SesionDeManillaDTO.CodigosError.FECHA_DE_ACTIVACION_INVALIDA, errorApi.codigoInterno)
                            verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                        }
                    }

                    @Nested
                    @DisplayName("Fecha de desactivación invalida")
                    inner class FechaDesactivacionInvalida
                    {
                        @Test
                        fun cuando_el_dto_tiene_una_zona_horaria_diferente_a_la_por_defecto()
                        {
                            sesionDeManillaDTOEnPrueba =
                                    sesionDeManillaDTOEnPrueba.copy(fechaDesactivacion =
                                                                    sesionDeManillaDTOEnPrueba.fechaDesactivacion!!.withZoneSameInstant(ZoneId.of("America/Bogota"))
                                                                   )
                            val errorApi = assertThrows<EntidadInvalida> {
                                actualizarConSesionDeManilla(sesionDeManillaDTOEnPrueba)
                            }

                            assertEquals(SesionDeManillaDTO.CodigosError.FECHA_DE_DESACTIVACION_INVALIDA, errorApi.codigoInterno)
                            verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                        }

                        @Test
                        fun cuando_el_dto_tiene_fecha_de_desactivacion_pero_no_de_activacion()
                        {
                            sesionDeManillaDTOEnPrueba = sesionDeManillaDTOEnPrueba.copy(fechaActivacion = null)
                            val errorApi = assertThrows<EntidadInvalida> {
                                actualizarConSesionDeManilla(sesionDeManillaDTOEnPrueba)
                            }

                            assertEquals(SesionDeManillaDTO.CodigosError.FECHA_DE_DESACTIVACION_INVALIDA, errorApi.codigoInterno)
                            verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                        }
                    }

                    @Nested
                    @DisplayName("Ids de créditos a codificar")
                    inner class IdsCreditosACodificar
                    {
                        @Test
                        fun cuando_el_dto_estan_vacios()
                        {
                            sesionDeManillaDTOEnPrueba = sesionDeManillaDTOEnPrueba.copy(idsCreditosCodificados = listOf())
                            val errorApi = assertThrows<EntidadInvalida> {
                                actualizarConSesionDeManilla(sesionDeManillaDTOEnPrueba)
                            }

                            assertEquals(SesionDeManillaDTO.CodigosError.IDS_CREDITOS_A_CODIFICAR_VACIOS, errorApi.codigoInterno)
                            verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                        }
                    }
                }
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Reservas", PermisoBack.Accion.PUT)
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
            private lateinit var entidadDTO: ReservaDTO

            @BeforeEach
            private fun crearEntidadesDePrueba()
            {
                entidadDTO = darEntidadDTOSegunIndice(1)
            }

            @Test
            fun funciona_correctamente_cuando_el_repositorio_actualiza_la_entidad()
            {
                val entidadPatch = TransaccionEntidadTerminadaDTO<Reserva>(true)
                val mapeoDeCamposEsperado =
                        mapOf<String, CampoModificableEntidad<Reserva, *>>(
                                EntidadTransaccional.Campos.CREACION_TERMINADA to EntidadTransaccional.CampoCreacionTerminada(entidadPatch.creacionTerminada)
                                                                          )

                doNothing()
                    .`when`(mockRepositorio)
                    .actualizarCamposIndividuales(ID_CLIENTE, entidadDTO.id, mapeoDeCamposEsperado)

                recursoEntidadEspecifica.patch(entidadPatch)

                verify(mockRepositorio)
                    .actualizarCamposIndividuales(ID_CLIENTE, entidadDTO.id, mapeoDeCamposEsperado)
            }

            @Test
            fun lanza_excepcion_ErrorActualizandoEntidad_con_codigo_interno_error_de_bd_desconocido_cuando_el_repositorio_lanza_ErrorAlActualizarCampo()
            {
                val entidadPatch = TransaccionEntidadTerminadaDTO<Reserva>(true)
                val mapeoDeCamposEsperado =
                        mapOf<String, CampoModificableEntidad<Reserva, *>>(
                                EntidadTransaccional.Campos.CREACION_TERMINADA to EntidadTransaccional.CampoCreacionTerminada(entidadPatch.creacionTerminada)
                                                                          )

                doThrow(ErrorAlActualizarCampo("no importa", "no importa", "no importa"))
                    .`when`(mockRepositorio)
                    .actualizarCamposIndividuales(ID_CLIENTE, entidadDTO.id, mapeoDeCamposEsperado)

                val errorApi = assertThrows<ErrorActualizandoEntidad> { recursoEntidadEspecifica.patch(entidadPatch) }

                assertEquals(ReservaDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                verify(mockRepositorio)
                    .actualizarCamposIndividuales(ID_CLIENTE, entidadDTO.id, mapeoDeCamposEsperado)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                val entidadPatch = TransaccionEntidadTerminadaDTO<Reserva>(true)
                val mapeoDeCamposEsperado =
                        mapOf<String, CampoModificableEntidad<Reserva, *>>(
                                EntidadTransaccional.Campos.CREACION_TERMINADA to EntidadTransaccional.CampoCreacionTerminada(entidadPatch.creacionTerminada)
                                                                          )

                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .actualizarCamposIndividuales(ID_CLIENTE, entidadDTO.id, mapeoDeCamposEsperado)

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.patch(entidadPatch) }

                assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio)
                    .actualizarCamposIndividuales(ID_CLIENTE, entidadDTO.id, mapeoDeCamposEsperado)
            }

            @Nested
            @DisplayName("Lanzan EntidadInvalida y código interno")
            inner class LanzanExcepcionEntidadInvalida
            {
                @Test
                fun desconocido_cuando_el_repositorio_lanza_ErrorDeCreacionActualizacionEntidad()
                {
                    val entidadPatch = TransaccionEntidadTerminadaDTO<Reserva>(true)
                    doThrow(ErrorDeCreacionActualizacionEntidad("no importa"))
                        .`when`(mockRepositorio)
                        .actualizarCamposIndividuales(
                                ID_CLIENTE,
                                entidadDTO.id,
                                mapOf(EntidadTransaccional.Campos.CREACION_TERMINADA to EntidadTransaccional.CampoCreacionTerminada(entidadPatch.creacionTerminada))
                                                     )

                    val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.patch(entidadPatch) }

                    assertEquals(ReservaDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
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
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Reservas", PermisoBack.Accion.PATCH)
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
                    val entidadPatch = TransaccionEntidadTerminadaDTO<Reserva>(true)
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
                    val entidadPatch = TransaccionEntidadTerminadaDTO<Reserva>(true)
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
                    val entidadPatch = TransaccionEntidadTerminadaDTO<Reserva>(true)
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
            private lateinit var entidadNegocio: Reserva
            private lateinit var entidadDTO: ReservaDTO

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

                assertEquals(ReservaDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).eliminarPorId(ID_CLIENTE, entidadNegocio.id)
            }

            @Test
            fun lanza_excepcion_con_codigo_interno_entidad_referenciada_cuando_el_repositorio_lanza_ErrorDeLlaveForanea()
            {
                doThrow(ErrorDeLlaveForanea(0, "Error eliminando acceso"))
                    .`when`(mockRepositorio)
                    .eliminarPorId(ID_CLIENTE, entidadNegocio.id)

                val errorApi = assertThrows<co.smartobjects.prompterbackend.excepciones.ErrorEliminandoEntidad> { recursoEntidadEspecifica.eliminarPorId() }

                assertEquals(ReservaDTO.CodigosError.ENTIDAD_REFERENCIADA, errorApi.codigoInterno)
                verify(mockRepositorio).eliminarPorId(ID_CLIENTE, entidadNegocio.id)
            }

            @Test
            fun lanza_excepcion_con_codigo_interno_desconocido_cuando_el_repositorio_lanza_ErrorEliminandoEntidad()
            {
                doThrow(ErrorEliminandoEntidad(1, "Nombre entidad"))
                    .`when`(mockRepositorio)
                    .eliminarPorId(ID_CLIENTE, entidadNegocio.id)

                val errorApi = assertThrows<co.smartobjects.prompterbackend.excepciones.ErrorEliminandoEntidad> { recursoEntidadEspecifica.eliminarPorId() }

                assertEquals(ReservaDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                verify(mockRepositorio).eliminarPorId(ID_CLIENTE, entidadNegocio.id)
            }

            @Test
            fun lanza_excepcion_ErrorEliminandoEntidad_con_codigo_interno_esta_marcada_como_creacion_terminada_cuando_el_repositorio_lanza_ErrorEliminacionViolacionDeRestriccion()
            {
                doThrow(ErrorEliminacionViolacionDeRestriccion("no importa", "no importa", "no importa", null))
                    .`when`(mockRepositorio)
                    .eliminarPorId(ID_CLIENTE, entidadNegocio.id)

                val errorApi = assertThrows<co.smartobjects.prompterbackend.excepciones.ErrorEliminandoEntidad> { recursoEntidadEspecifica.eliminarPorId() }

                assertEquals(ReservaDTO.CodigosError.ESTA_MARCADA_CON_CREACION_TERMINADA, errorApi.codigoInterno)
                verify(mockRepositorio).eliminarPorId(ID_CLIENTE, entidadNegocio.id)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .eliminarPorId(ID_CLIENTE, entidadNegocio.id)

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.eliminarPorId() }

                assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).eliminarPorId(ID_CLIENTE, entidadNegocio.id)
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Reservas", PermisoBack.Accion.DELETE)
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