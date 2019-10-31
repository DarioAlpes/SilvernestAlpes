package co.smartobjects.prompterbackend.serviciosrest.ubicaciones.contabilizables

import co.smartobjects.entidades.ubicaciones.contabilizables.ConteoUbicacion
import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.persistencia.excepciones.ErrorDeConsultaEntidad
import co.smartobjects.persistencia.excepciones.ErrorDeLlaveForanea
import co.smartobjects.persistencia.excepciones.EsquemaNoExiste
import co.smartobjects.persistencia.ubicaciones.contabilizables.FiltroConteosUbicaciones
import co.smartobjects.persistencia.ubicaciones.contabilizables.RepositorioConteosUbicaciones
import co.smartobjects.prompterbackend.*
import co.smartobjects.prompterbackend.excepciones.*
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.clientes.RecursoClientes
import co.smartobjects.prompterbackend.serviciosrest.usuarios.RecursoPermisosPosibles
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.modelos.clientes.ClienteDTO
import co.smartobjects.red.modelos.ubicaciones.contabilizables.ConteoUbicacionDTO
import co.smartobjects.red.modelos.usuarios.PermisoBackDTO
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import com.fasterxml.jackson.datatype.jaxrs.Jaxrs2TypesModule
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider
import org.glassfish.grizzly.http.server.HttpServer
import org.junit.jupiter.api.*
import org.mockito.Mockito.*
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.Year
import org.threeten.bp.ZonedDateTime
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.WebTarget
import kotlin.test.assertEquals
import kotlin.test.assertTrue


internal class RecursoConteosUbicacionesPruebas
{
    companion object
    {
        const val ID_CLIENTE = 1L
        const val ID_UBICACION = 2L
    }

    private fun darEntidadNegocioSegunIndice(indice: Int): ConteoUbicacion
    {
        return ConteoUbicacion(
                ID_CLIENTE,
                ID_UBICACION,
                indice.toLong(),
                ZonedDateTime.of(LocalDate.of(Year.now(ZONA_HORARIA_POR_DEFECTO).value + 1 + indice, 1, 1), LocalTime.of(5, 5), ZONA_HORARIA_POR_DEFECTO)
                              )
    }

    private fun darEntidadDTOSegunIndice(indice: Int): ConteoUbicacionDTO
    {
        return ConteoUbicacionDTO(darEntidadNegocioSegunIndice(indice))
    }

    @[Nested DisplayName("Al llamar por red")]
    inner class Red
    {
        private lateinit var server: HttpServer
        private lateinit var target: WebTarget
        private lateinit var mockRecursoTodasEntidades: RecursoConteosUbicaciones

        @[BeforeEach Throws(Exception::class)]
        fun antesDeCadaTest()
        {
            mockRecursoTodasEntidades = mockConDefaultAnswer(RecursoConteosUbicaciones::class.java)

            val mockRecursoClientes = mockConDefaultAnswer(RecursoClientes::class.java)
            val mockRecursoCliente = mockConDefaultAnswer(RecursoClientes.RecursoCliente::class.java)

            doReturn(mockRecursoCliente).`when`(mockRecursoClientes).darRecursosEntidadEspecifica(ID_CLIENTE)
            doReturn(mockRecursoTodasEntidades).`when`(mockRecursoCliente).darRecursoConteosUbicaciones()

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
                doReturn(sequenceOf<ConteoUbicacionDTO>())
                    .`when`(mockRecursoTodasEntidades)
                    .darTodas()

                target
                    .path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoConteosUbicaciones.RUTA}")
                    .request()
                    .get(String::class.java)

                verify(mockRecursoTodasEntidades).darTodas()
            }
        }

        @[Nested DisplayName("Al eliminar")]
        inner class Eliminar
        {
            @Test
            fun llama_la_funcion_eliminarPorId_con_dto_correcto()
            {
                doNothing().`when`(mockRecursoTodasEntidades).eliminarTodas()

                target
                    .path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoConteosUbicaciones.RUTA}")
                    .request()
                    .delete()

                verify(mockRecursoTodasEntidades).eliminarTodas()
            }
        }
    }

    @[Nested DisplayName("Al llamar por código")]
    inner class Codigo
    {
        private val mockRepositorio = mockConDefaultAnswer(RepositorioConteosUbicaciones::class.java)

        private fun darRecursoTodasEntidadesConManejador(
                manejadorSeguridad: ManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteSiempreTienePermisos
                                                        ): RecursoConteosUbicaciones
        {
            return RecursoConteosUbicaciones(
                    ID_CLIENTE,
                    mockRepositorio,
                    manejadorSeguridad
                                            )
        }

        @[Nested DisplayName("Al consultar todos")]
        inner class ConsultarTodos
        {
            @Test
            fun retorna_una_lista_vacia_de_dtos_cuando_el_repositorio_retorna_una_lista_vacia()
            {
                doReturn(sequenceOf<ConteoUbicacion>())
                    .`when`(mockRepositorio)
                    .listarSegunParametros(ID_CLIENTE, FiltroConteosUbicaciones.Todos)

                val listaRetornada = darRecursoTodasEntidadesConManejador().darTodas()

                assertTrue(listaRetornada.none())
                verify(mockRepositorio).listarSegunParametros(ID_CLIENTE, FiltroConteosUbicaciones.Todos)
            }

            @Test
            fun retorna_una_lista_de_dtos_correcta_cuando_el_repositorio_retorna_una_lista_de_entidades()
            {
                val listaNegocio = List(5) { darEntidadNegocioSegunIndice(it) }.asSequence()
                val listaDTO = List(5) { darEntidadDTOSegunIndice(it) }
                doReturn(listaNegocio)
                    .`when`(mockRepositorio)
                    .listarSegunParametros(ID_CLIENTE, FiltroConteosUbicaciones.Todos)

                val listaRetornada = darRecursoTodasEntidadesConManejador().darTodas()

                assertEquals(listaDTO, listaRetornada.toList())
                verify(mockRepositorio).listarSegunParametros(ID_CLIENTE, FiltroConteosUbicaciones.Todos)
            }

            @Test
            fun lanza_excepcion_ErrorDesconocido_con_codigo_interno_correcto_cuando_el_repositorio_lanza_ErrorDeConsultaEntidad()
            {
                doThrow(ErrorDeConsultaEntidad("conteo de ubicación"))
                    .`when`(mockRepositorio)
                    .listarSegunParametros(ID_CLIENTE, FiltroConteosUbicaciones.Todos)

                val errorApi = assertThrows<ErrorDesconocido> { darRecursoTodasEntidadesConManejador().darTodas() }

                assertEquals(ConteoUbicacionDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                verify(mockRepositorio).listarSegunParametros(ID_CLIENTE, FiltroConteosUbicaciones.Todos)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .listarSegunParametros(ID_CLIENTE, FiltroConteosUbicaciones.Todos)

                val errorApi = assertThrows<EntidadNoExiste> { darRecursoTodasEntidadesConManejador().darTodas() }

                assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).listarSegunParametros(ID_CLIENTE, FiltroConteosUbicaciones.Todos)
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Conteos de Ubicaciones", PermisoBack.Accion.GET_TODOS)
                private val permisoBuscadoDTO = PermisoBackDTO(permisoBuscadoNegocio)

                @Test
                fun el_servicio_de_permisos_posibles_retorna_el_permiso_correspondiente_una_vez()
                {
                    val permisos: Sequence<PermisoBackDTO> =
                            RecursoPermisosPosibles(ID_CLIENTE, mockManejadorSeguridadUsuarioDeClienteSiempreTienePermisos)
                                .darTodas()

                    val numeroPermisos = permisos.count { it == permisoBuscadoDTO }

                    assertEquals(1, numeroPermisos)
                }

                @Test
                fun se_ejecuta_la_operacion_de_bd_cuando_manejador_de_seguridad_no_lanza_excepcion()
                {
                    doReturn(sequenceOf<ConteoUbicacion>())
                        .`when`(mockRepositorio)
                        .listarSegunParametros(ID_CLIENTE, FiltroConteosUbicaciones.Todos)

                    darRecursoTodasEntidadesConManejador(darMockManejadorSeguridadUsuarioDeClienteConPermiso(permisoBuscadoNegocio))
                        .darTodas()

                    verify(mockRepositorio).listarSegunParametros(ID_CLIENTE, FiltroConteosUbicaciones.Todos)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    assertThrows<UsuarioNoAutenticado> {
                        darRecursoTodasEntidadesConManejador(mockManejadorSeguridadUsuarioDeClienteNuncaEstaAutenticado)
                            .darTodas()
                    }

                    verify(mockRepositorio, times(0)).listarSegunParametros(ID_CLIENTE, FiltroConteosUbicaciones.Todos)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso()
                {
                    assertThrows<UsuarioNoTienePermiso> {
                        darRecursoTodasEntidadesConManejador(darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio))
                            .darTodas()
                    }

                    verify(mockRepositorio, times(0)).listarSegunParametros(ID_CLIENTE, FiltroConteosUbicaciones.Todos)
                }
            }
        }

        @[Nested DisplayName("Al eliminar")]
        inner class Eliminar
        {
            private lateinit var entidadNegocio: ConteoUbicacion
            private lateinit var entidadDTO: ConteoUbicacionDTO

            @BeforeEach
            private fun crearEntidadesDePrueba()
            {
                entidadNegocio = darEntidadNegocioSegunIndice(2)
                entidadDTO = darEntidadDTOSegunIndice(2)
            }

            @Test
            fun no_lanza_excepcion_cuando_el_repositorio_retorna_true()
            {
                doReturn(true)
                    .`when`(mockRepositorio)
                    .eliminarSegunFiltros(ID_CLIENTE, FiltroConteosUbicaciones.Todos)

                darRecursoTodasEntidadesConManejador().eliminarTodas()

                verify(mockRepositorio).eliminarSegunFiltros(ID_CLIENTE, FiltroConteosUbicaciones.Todos)
            }

            @Test
            fun lanza_excepcion_ErrorEliminandoEntidad_cuando_el_repositorio_retorna_false()
            {
                doReturn(false)
                    .`when`(mockRepositorio)
                    .eliminarSegunFiltros(ID_CLIENTE, FiltroConteosUbicaciones.Todos)

                assertThrows<ErrorEliminandoEntidad> { darRecursoTodasEntidadesConManejador().eliminarTodas() }

                verify(mockRepositorio).eliminarSegunFiltros(ID_CLIENTE, FiltroConteosUbicaciones.Todos)
            }

            @Test
            fun lanza_excepcion_con_codigo_interno_entidad_referenciada_cuando_el_repositorio_lanza_ErrorDeLlaveForanea()
            {
                doThrow(ErrorDeLlaveForanea(0, "Error eliminando acceso"))
                    .`when`(mockRepositorio)
                    .eliminarSegunFiltros(ID_CLIENTE, FiltroConteosUbicaciones.Todos)

                val errorApi = assertThrows<ErrorEliminandoEntidad> { darRecursoTodasEntidadesConManejador().eliminarTodas() }

                kotlin.test.assertEquals(ConteoUbicacionDTO.CodigosError.ENTIDAD_REFERENCIADA, errorApi.codigoInterno)
                verify(mockRepositorio).eliminarSegunFiltros(ID_CLIENTE, FiltroConteosUbicaciones.Todos)
            }

            @Test
            fun lanza_excepcion_con_codigo_interno_desconocido_cuando_el_repositorio_lanza_ErrorEliminandoEntidad()
            {
                doThrow(co.smartobjects.persistencia.excepciones.ErrorEliminandoEntidad(1, "Nombre entidad"))
                    .`when`(mockRepositorio)
                    .eliminarSegunFiltros(ID_CLIENTE, FiltroConteosUbicaciones.Todos)

                val errorApi = assertThrows<ErrorEliminandoEntidad> { darRecursoTodasEntidadesConManejador().eliminarTodas() }

                assertEquals(ConteoUbicacionDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                verify(mockRepositorio).eliminarSegunFiltros(ID_CLIENTE, FiltroConteosUbicaciones.Todos)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .eliminarSegunFiltros(ID_CLIENTE, FiltroConteosUbicaciones.Todos)

                val errorApi = assertThrows<EntidadNoExiste> { darRecursoTodasEntidadesConManejador().eliminarTodas() }

                assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).eliminarSegunFiltros(ID_CLIENTE, FiltroConteosUbicaciones.Todos)
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Conteos de Ubicaciones", PermisoBack.Accion.DELETE)
                private val permisoBuscadoDTO = PermisoBackDTO(permisoBuscadoNegocio)

                @Test
                fun el_servicio_de_permisos_posibles_retorna_el_permiso_correspondiente_una_vez()
                {
                    val permisos: Sequence<PermisoBackDTO> =
                            RecursoPermisosPosibles(ID_CLIENTE, mockManejadorSeguridadUsuarioDeClienteSiempreTienePermisos)
                                .darTodas()

                    val numeroPermisos = permisos.count { it == permisoBuscadoDTO }

                    assertEquals(1, numeroPermisos)
                }

                @Test
                fun se_ejecuta_la_operacion_de_bd_cuando_manejador_de_seguridad_no_lanza_excepcion()
                {
                    doReturn(true)
                        .`when`(mockRepositorio)
                        .eliminarSegunFiltros(ID_CLIENTE, FiltroConteosUbicaciones.Todos)

                    darRecursoTodasEntidadesConManejador(darMockManejadorSeguridadUsuarioDeClienteConPermiso(permisoBuscadoNegocio))
                        .eliminarTodas()

                    verify(mockRepositorio).eliminarSegunFiltros(ID_CLIENTE, FiltroConteosUbicaciones.Todos)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    assertThrows<UsuarioNoAutenticado> {
                        darRecursoTodasEntidadesConManejador(mockManejadorSeguridadUsuarioDeClienteNuncaEstaAutenticado)
                            .eliminarTodas()
                    }

                    verify(mockRepositorio, times(0)).eliminarSegunFiltros(ID_CLIENTE, FiltroConteosUbicaciones.Todos)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso()
                {
                    assertThrows<UsuarioNoTienePermiso> {
                        darRecursoTodasEntidadesConManejador(darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio))
                            .eliminarTodas()
                    }

                    verify(mockRepositorio, times(0)).eliminarSegunFiltros(ID_CLIENTE, FiltroConteosUbicaciones.Todos)
                }
            }
        }
    }
}