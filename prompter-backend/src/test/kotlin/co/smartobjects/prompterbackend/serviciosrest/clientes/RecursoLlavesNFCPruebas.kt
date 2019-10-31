package co.smartobjects.prompterbackend.serviciosrest.clientes

import co.smartobjects.entidades.clientes.Cliente
import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.persistencia.clientes.llavesnfc.FiltroLlavesNFC
import co.smartobjects.persistencia.clientes.llavesnfc.RepositorioLlavesNFC
import co.smartobjects.persistencia.excepciones.ErrorDeConsultaEntidad
import co.smartobjects.persistencia.excepciones.ErrorDeCreacionActualizacionEntidad
import co.smartobjects.persistencia.excepciones.ErrorDeLlaveForanea
import co.smartobjects.persistencia.excepciones.EsquemaNoExiste
import co.smartobjects.prompterbackend.*
import co.smartobjects.prompterbackend.excepciones.*
import co.smartobjects.prompterbackend.serviciosrest.usuarios.RecursoPermisosPosibles
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.modelos.clientes.ClienteDTO
import co.smartobjects.red.modelos.clientes.LlaveNFCDTO
import co.smartobjects.red.modelos.usuarios.PermisoBackDTO
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import com.fasterxml.jackson.datatype.jaxrs.Jaxrs2TypesModule
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider
import org.glassfish.grizzly.http.server.HttpServer
import org.junit.jupiter.api.*
import org.mockito.Mockito.*
import org.threeten.bp.*
import org.threeten.bp.format.DateTimeFormatter
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import kotlin.test.assertEquals


internal class RecursoLlavesNFCPruebas
{
    companion object
    {
        const val ID_CLIENTE = 1L

        private val FECHA_PRUEBAS =
                ZonedDateTime.of(
                        LocalDate.of(Year.now(ZONA_HORARIA_POR_DEFECTO).value, 1, 1),
                        LocalTime.of(5, 5),
                        ZONA_HORARIA_POR_DEFECTO
                                )
        private val FECHA_PRUEBAS_STRING = FECHA_PRUEBAS.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
    }

    private fun darEntidadNegocioSegunIndice(indice: Int): Cliente.LlaveNFC
    {
        return Cliente.LlaveNFC(
                ID_CLIENTE,
                "llave $indice",
                ZonedDateTime.of(LocalDate.of(Year.now(ZONA_HORARIA_POR_DEFECTO).value + indice, 1, 1), LocalTime.of(5, 5), ZONA_HORARIA_POR_DEFECTO)
                               )
    }

    private fun darEntidadDTOSegunIndice(indice: Int): LlaveNFCDTO
    {
        return LlaveNFCDTO(darEntidadNegocioSegunIndice(indice))
    }


    @[Nested DisplayName("Al llamar por red")]
    inner class Red
    {
        private lateinit var server: HttpServer
        private lateinit var target: WebTarget
        private lateinit var mockRecursoTodasEntidades: RecursoLlavesNFC

        @[BeforeEach Throws(Exception::class)]
        fun antesDeCadaTest()
        {
            mockRecursoTodasEntidades = mockConDefaultAnswer(RecursoLlavesNFC::class.java)

            val mockRecursoCliente = mockConDefaultAnswer(RecursoClientes.RecursoCliente::class.java)
            val mockRecursoClientes = mockConDefaultAnswer(RecursoClientes::class.java)

            doReturn(mockRecursoCliente).`when`(mockRecursoClientes).darRecursosEntidadEspecifica(RecursoLlavesNFCPruebas.ID_CLIENTE)
            doReturn(mockRecursoTodasEntidades).`when`(mockRecursoCliente).darRecursoLlavesNFC()

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

        @[Nested DisplayName("Al crear")]
        inner class Crear
        {
            @Test
            fun llama_la_funcion_crear_con_dto_correcto()
            {
                val dtoPruebas = darEntidadDTOSegunIndice(0)
                val entidadPruebas = darEntidadNegocioSegunIndice(0)

                doReturn(LlaveNFCDTO(entidadPruebas)).`when`(mockRecursoTodasEntidades).crear(dtoPruebas)

                target.path("${RecursoClientes.RUTA}/${RecursoLlavesNFCPruebas.ID_CLIENTE}/${RecursoLlavesNFC.RUTA}").request()
                    .post(Entity.entity(dtoPruebas, MediaType.APPLICATION_JSON_TYPE), LlaveNFCDTO::class.java)

                verify(mockRecursoTodasEntidades).crear(dtoPruebas)
            }
        }

        @[Nested DisplayName("Al consultar una")]
        inner class ConsultarUna
        {
            @Test
            fun llama_la_funcion_buscar_con_fecha_valida()
            {
                doReturn(darEntidadDTOSegunIndice(0))
                    .`when`(mockRecursoTodasEntidades)
                    .buscar(FECHA_PRUEBAS)

                target
                    .path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoLlavesNFC.RUTA}")
                    .queryParam(RecursoLlavesNFC.NOMBRE_PARAMETRO_TIEMPO_CONSULTA, FECHA_PRUEBAS_STRING)
                    .request()
                    .get(String::class.java)

                verify(mockRecursoTodasEntidades).buscar(FECHA_PRUEBAS)
            }

            @Test
            fun llama_la_funcion_buscar_con_fecha_null()
            {
                doReturn(darEntidadDTOSegunIndice(0))
                    .`when`(mockRecursoTodasEntidades)
                    .buscar(null)

                target
                    .path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoLlavesNFC.RUTA}")
                    .request()
                    .get(String::class.java)

                verify(mockRecursoTodasEntidades).buscar(null)
            }
        }

        @[Nested DisplayName("Al eliminar una")]
        inner class Eliminar
        {
            @Test
            fun llama_la_funcion_eliminarHastaFechaCorte_con_fecha_valida()
            {
                doNothing().`when`(mockRecursoTodasEntidades).eliminarHastaFechaCorte(FECHA_PRUEBAS)

                target
                    .path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoLlavesNFC.RUTA}")
                    .queryParam(RecursoLlavesNFC.NOMBRE_PARAMETRO_TIEMPO_CONSULTA, FECHA_PRUEBAS_STRING)
                    .request()
                    .delete()

                verify(mockRecursoTodasEntidades).eliminarHastaFechaCorte(FECHA_PRUEBAS)
            }

            @Test
            fun llama_la_funcion_eliminarHastaFechaCorte_con_fecha_null()
            {
                doNothing().`when`(mockRecursoTodasEntidades).eliminarHastaFechaCorte(null)

                target
                    .path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoLlavesNFC.RUTA}")
                    .request()
                    .delete()

                verify(mockRecursoTodasEntidades).eliminarHastaFechaCorte(null)
            }
        }
    }


    @[Nested DisplayName("Al llamar por código")]
    inner class Codigo
    {
        private val mockRepositorio = mockConDefaultAnswer(RepositorioLlavesNFC::class.java)
        private var mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteSiempreTienePermisos
        private val recursoTodasEntidades by lazy {
            RecursoLlavesNFC(ID_CLIENTE, mockRepositorio, mockManejadorSeguridad)
        }


        @Nested
        @DisplayName("Al crear")
        inner class Crear
        {
            private lateinit var entidadNegocio: Cliente.LlaveNFC
            private lateinit var entidadDTO: LlaveNFCDTO

            @BeforeEach
            private fun crearEntidadesDePrueba()
            {
                entidadNegocio = darEntidadNegocioSegunIndice(0)
                entidadDTO = darEntidadDTOSegunIndice(0)
            }


            @Test
            fun retorna_el_dto_correcto_cuando_el_repositorio_crea_la_entidad()
            {
                doReturn(entidadNegocio)
                    .`when`(mockRepositorio)
                    .crear(ID_CLIENTE, entidadNegocio)

                val entidadRetornada = recursoTodasEntidades.crear(entidadDTO)

                assertEquals(entidadDTO, entidadRetornada)
                verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_entidad_referenciada_no_existe_cuando_el_repositorio_lanza_ErrorDeLlaveForanea()
            {
                doThrow(ErrorDeLlaveForanea(0, "Alguien no existe"))
                    .`when`(mockRepositorio)
                    .crear(ID_CLIENTE, entidadNegocio)

                val errorApi = assertThrows<EntidadReferenciadaNoExiste> { recursoTodasEntidades.crear(entidadDTO) }

                assertEquals(LlaveNFCDTO.CodigosError.ENTIDAD_REFERENCIADA_NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .crear(ID_CLIENTE, entidadNegocio)

                val errorApi = assertThrows<EntidadNoExiste> { recursoTodasEntidades.crear(entidadDTO) }

                assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
            }

            @Nested
            @DisplayName("Lanzan EntidadInvalida y código interno")
            inner class LanzanExcepcionEntidadInvalida
            {
                @Test
                fun desconocido_cuando_el_repositorio_lanza_ErrorDeCreacionActualizacionEntidad()
                {
                    doThrow(ErrorDeCreacionActualizacionEntidad("Error creando Cliente.LlaveNFC"))
                        .`when`(mockRepositorio)
                        .crear(ID_CLIENTE, entidadNegocio)

                    val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTO) }

                    assertEquals(LlaveNFCDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                    verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
                }
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Llaves NFC", PermisoBack.Accion.POST)
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
                    recursoTodasEntidades.crear(entidadDTO)
                    verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteNuncaEstaAutenticado
                    assertThrows<UsuarioNoAutenticado> { recursoTodasEntidades.crear(entidadDTO) }
                    verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso()
                {
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio)
                    assertThrows<UsuarioNoTienePermiso> { recursoTodasEntidades.crear(entidadDTO) }
                    verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                }
            }
        }

        @[Nested DisplayName("Al consultar una")]
        inner class ConsultarUna
        {
            private val parametros: FiltroLlavesNFC = FiltroLlavesNFC.ValidaEnFecha(FECHA_PRUEBAS)

            @Test
            fun cambia_timezone_de_fecha_a_timezone_por_defecto()
            {
                val fechaConTimeZoneDiferente = FECHA_PRUEBAS.withZoneSameInstant(ZoneId.of("Australia/Sydney"))

                doReturn(darEntidadNegocioSegunIndice(0))
                    .`when`(mockRepositorio)
                    .buscarSegunParametros(ID_CLIENTE, parametros)

                val entidadRetorna = recursoTodasEntidades.buscar(fechaConTimeZoneDiferente)

                assertEquals(darEntidadDTOSegunIndice(0), entidadRetorna)
                verify(mockRepositorio).buscarSegunParametros(ID_CLIENTE, parametros)
            }

            @Test
            fun retorna_dto_correcto_cuando_el_repositorio_retorna_una_lista_de_entidades()
            {
                doReturn(darEntidadNegocioSegunIndice(0))
                    .`when`(mockRepositorio)
                    .buscarSegunParametros(ID_CLIENTE, parametros)

                val entidadRetorna = recursoTodasEntidades.buscar(FECHA_PRUEBAS)

                assertEquals(darEntidadDTOSegunIndice(0), entidadRetorna)
                verify(mockRepositorio).buscarSegunParametros(ID_CLIENTE, parametros)
            }

            @Test
            fun lanza_excepcion_ErrorDesconocido_con_codigo_interno_correcto_cuando_el_repositorio_lanza_ErrorDeConsultaEntidad()
            {
                doThrow(ErrorDeConsultaEntidad("Cliente.LlaveNFC"))
                    .`when`(mockRepositorio)
                    .buscarSegunParametros(ID_CLIENTE, parametros)

                val errorApi = assertThrows<ErrorDesconocido> { recursoTodasEntidades.buscar(FECHA_PRUEBAS) }

                assertEquals(LlaveNFCDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                verify(mockRepositorio).buscarSegunParametros(ID_CLIENTE, parametros)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .buscarSegunParametros(ID_CLIENTE, parametros)

                val errorApi = assertThrows<EntidadNoExiste> { recursoTodasEntidades.buscar(FECHA_PRUEBAS) }

                assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).buscarSegunParametros(ID_CLIENTE, parametros)
            }

            @Nested
            @DisplayName("Lanzan EntidadInvalida y código interno")
            inner class LanzanExcepcionEntidadInvalida
            {
                @Test
                fun fecha_consulta_invalida_cuando_se_pasa_fecha_consulta_nula()
                {
                    val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.buscar(null) }

                    assertEquals(LlaveNFCDTO.CodigosError.FECHA_CONSULTA_INVALIDA, errorApi.codigoInterno)
                    verify(mockRepositorio, times(0)).buscarSegunParametros(ID_CLIENTE, parametros)
                }
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Llaves NFC", PermisoBack.Accion.GET_UNO)
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
                    doReturn(darEntidadNegocioSegunIndice(0))
                        .`when`(mockRepositorio)
                        .buscarSegunParametros(ID_CLIENTE, parametros)
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteConPermiso(permisoBuscadoNegocio)
                    recursoTodasEntidades.buscar(FECHA_PRUEBAS)
                    verify(mockRepositorio).buscarSegunParametros(ID_CLIENTE, parametros)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteNuncaEstaAutenticado
                    assertThrows<UsuarioNoAutenticado> { recursoTodasEntidades.buscar(FECHA_PRUEBAS) }
                    verify(mockRepositorio, times(0)).buscarSegunParametros(ID_CLIENTE, parametros)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso()
                {
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio)
                    assertThrows<UsuarioNoTienePermiso> { recursoTodasEntidades.buscar(FECHA_PRUEBAS) }
                    verify(mockRepositorio, times(0)).buscarSegunParametros(ID_CLIENTE, parametros)
                }
            }
        }

        @[Nested DisplayName("Al eliminar")]
        inner class Eliminar
        {
            private val parametros: FiltroLlavesNFC = FiltroLlavesNFC.ValidaEnFecha(FECHA_PRUEBAS)

            private lateinit var entidadNegocio: Cliente.LlaveNFC
            private lateinit var entidadDTO: LlaveNFCDTO

            @BeforeEach
            private fun crearEntidadesDePrueba()
            {
                entidadNegocio = darEntidadNegocioSegunIndice(0)
                entidadDTO = darEntidadDTOSegunIndice(0)
            }

            @Test
            fun no_lanza_excepcion_cuando_el_repositorio_retorna_true()
            {
                doReturn(true)
                    .`when`(mockRepositorio)
                    .eliminarSegunFiltros(ID_CLIENTE, parametros)

                recursoTodasEntidades.eliminarHastaFechaCorte(FECHA_PRUEBAS)
                verify(mockRepositorio).eliminarSegunFiltros(ID_CLIENTE, parametros)
            }

            @Test
            fun lanza_excepcion_con_codigo_interno_no_existe_cuando_el_repositorio_retorna_false()
            {
                doReturn(false)
                    .`when`(mockRepositorio)
                    .eliminarSegunFiltros(ID_CLIENTE, parametros)

                val errorApi = assertThrows<EntidadNoExisteParaParametros> { recursoTodasEntidades.eliminarHastaFechaCorte(FECHA_PRUEBAS) }

                assertEquals(LlaveNFCDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).eliminarSegunFiltros(ID_CLIENTE, parametros)
            }

            @Test
            fun lanza_excepcion_con_codigo_interno_entidad_referenciada_cuando_el_repositorio_lanza_ErrorDeLlaveForanea()
            {
                doThrow(ErrorDeLlaveForanea(0, "Error eliminando acceso"))
                    .`when`(mockRepositorio)
                    .eliminarSegunFiltros(ID_CLIENTE, parametros)

                val errorApi = assertThrows<ErrorEliminandoEntidad> {
                    recursoTodasEntidades.eliminarHastaFechaCorte(FECHA_PRUEBAS)
                }

                assertEquals(LlaveNFCDTO.CodigosError.ENTIDAD_REFERENCIADA, errorApi.codigoInterno)
                verify(mockRepositorio).eliminarSegunFiltros(ID_CLIENTE, parametros)
            }

            @Test
            fun lanza_excepcion_con_codigo_interno_desconocido_cuando_el_repositorio_lanza_ErrorEliminandoEntidad()
            {
                doThrow(co.smartobjects.persistencia.excepciones.ErrorEliminandoEntidad(1, "Nombre entidad"))
                    .`when`(mockRepositorio)
                    .eliminarSegunFiltros(ID_CLIENTE, parametros)

                val errorApi = assertThrows<ErrorEliminandoEntidad> {
                    recursoTodasEntidades.eliminarHastaFechaCorte(FECHA_PRUEBAS)
                }

                Assertions.assertEquals(LlaveNFCDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                verify(mockRepositorio).eliminarSegunFiltros(ID_CLIENTE, parametros)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .eliminarSegunFiltros(ID_CLIENTE, parametros)

                val errorApi = assertThrows<EntidadNoExiste> {
                    recursoTodasEntidades.eliminarHastaFechaCorte(FECHA_PRUEBAS)
                }

                Assertions.assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).eliminarSegunFiltros(ID_CLIENTE, parametros)
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Llaves NFC", PermisoBack.Accion.DELETE)
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
                        .eliminarSegunFiltros(ID_CLIENTE, parametros)
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteConPermiso(permisoBuscadoNegocio)
                    recursoTodasEntidades.eliminarHastaFechaCorte(FECHA_PRUEBAS)
                    verify(mockRepositorio).eliminarSegunFiltros(ID_CLIENTE, parametros)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteNuncaEstaAutenticado
                    assertThrows<UsuarioNoAutenticado> {
                        recursoTodasEntidades.eliminarHastaFechaCorte(FECHA_PRUEBAS)
                    }
                    verify(mockRepositorio, times(0)).eliminarSegunFiltros(ID_CLIENTE, parametros)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso()
                {
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio)
                    assertThrows<UsuarioNoTienePermiso> {
                        recursoTodasEntidades.eliminarHastaFechaCorte(FECHA_PRUEBAS)
                    }
                    verify(mockRepositorio, times(0)).eliminarSegunFiltros(ID_CLIENTE, parametros)
                }
            }
        }
    }
}