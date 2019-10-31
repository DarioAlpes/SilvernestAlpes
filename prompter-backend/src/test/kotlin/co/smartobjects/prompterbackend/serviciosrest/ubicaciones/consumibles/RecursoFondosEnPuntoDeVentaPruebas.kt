package co.smartobjects.prompterbackend.serviciosrest.ubicaciones.consumibles

import co.smartobjects.entidades.fondos.Fondo
import co.smartobjects.entidades.fondos.Sku
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.persistencia.excepciones.ErrorDeConsultaEntidad
import co.smartobjects.persistencia.excepciones.EsquemaNoExiste
import co.smartobjects.persistencia.ubicaciones.consumibles.IdUbicacionConsultaConsumibles
import co.smartobjects.persistencia.ubicaciones.consumibles.RepositorioFondosEnPuntoDeVenta
import co.smartobjects.prompterbackend.*
import co.smartobjects.prompterbackend.excepciones.EntidadNoExiste
import co.smartobjects.prompterbackend.excepciones.ErrorDesconocido
import co.smartobjects.prompterbackend.excepciones.UsuarioNoAutenticado
import co.smartobjects.prompterbackend.excepciones.UsuarioNoTienePermiso
import co.smartobjects.prompterbackend.serviciosrest.clientes.RecursoClientes
import co.smartobjects.prompterbackend.serviciosrest.ubicaciones.RecursoUbicaciones
import co.smartobjects.prompterbackend.serviciosrest.usuarios.RecursoPermisosPosibles
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.modelos.clientes.ClienteDTO
import co.smartobjects.red.modelos.fondos.FondoDTO
import co.smartobjects.red.modelos.fondos.IFondoDTO
import co.smartobjects.red.modelos.fondos.SkuDTO
import co.smartobjects.red.modelos.usuarios.PermisoBackDTO
import co.smartobjects.utilidades.Decimal
import com.fasterxml.jackson.datatype.jaxrs.Jaxrs2TypesModule
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider
import org.glassfish.grizzly.http.server.HttpServer
import org.junit.jupiter.api.*
import org.mockito.Mockito.*
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.WebTarget
import kotlin.test.assertEquals


@DisplayName("Recurso Fondos En Punto De Venta")
internal class RecursoFondosEnPuntoDeVentaPruebas
{
    companion object
    {
        const val ID_CLIENTE = 1L
        const val ID_ENTIDAD_PRUEBAS = 3L
    }

    private fun darEntidadNegocioSegunIndice(indice: Int): Sku
    {
        return Sku(
                ID_CLIENTE,
                indice.toLong(),
                "Entidad prueba $indice",
                indice % 2 == 0,
                indice % 2 == 1,
                indice % 2 == 0,
                Precio(Decimal(1000) * indice * indice, indice.toLong() + 10 * indice),
                "El código externo $indice",
                indice.toLong() + 10000,
                null
                  )
    }

    private fun darEntidadDTOSegunIndice(indice: Int): SkuDTO
    {
        return SkuDTO(darEntidadNegocioSegunIndice(indice))
    }

    @[Nested DisplayName("Al llamar por red")]
    inner class Red
    {
        private lateinit var server: HttpServer
        private lateinit var target: WebTarget
        private lateinit var mockRecursoTodasEntidades: RecursoFondosEnPuntoDeVenta

        @[BeforeEach Throws(Exception::class)]
        fun antesDeCadaTest()
        {
            mockRecursoTodasEntidades = mockConDefaultAnswer(RecursoFondosEnPuntoDeVenta::class.java)

            val mockRecursoUbicacion = mockConDefaultAnswer(RecursoUbicaciones.RecursoUbicacion::class.java)
            val mockRecursoUbicaciones = mockConDefaultAnswer(RecursoUbicaciones::class.java)

            val mockRecursoCliente = mockConDefaultAnswer(RecursoClientes.RecursoCliente::class.java)
            val mockRecursoClientes = mockConDefaultAnswer(RecursoClientes::class.java)

            doReturn(mockRecursoCliente).`when`(mockRecursoClientes).darRecursosEntidadEspecifica(ID_CLIENTE)
            doReturn(mockRecursoUbicaciones).`when`(mockRecursoCliente).darRecursoUbicaciones()
            doReturn(mockRecursoUbicacion).`when`(mockRecursoUbicaciones).darRecursosEntidadEspecifica(ID_ENTIDAD_PRUEBAS)
            doReturn(mockRecursoTodasEntidades).`when`(mockRecursoUbicacion).darRecursoFondosEnPuntoDeVenta()

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
            fun llama_la_funcion_listar()
            {
                doReturn(sequenceOf<IFondoDTO<*>>()).`when`(mockRecursoTodasEntidades).darTodas()
                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoUbicaciones.RUTA}/$ID_ENTIDAD_PRUEBAS/${RecursoFondosEnPuntoDeVenta.RUTA}")
                    .request()
                    .get(String::class.java)

                verify(mockRecursoTodasEntidades).darTodas()
            }
        }
    }

    @Nested
    @DisplayName("Al llamar por código")
    inner class Codigo
    {
        private val mockRepositorio = mockConDefaultAnswer(RepositorioFondosEnPuntoDeVenta::class.java)
        private var mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteSiempreTienePermisos
        private val recursoTodasEntidades: RecursoFondosEnPuntoDeVenta by lazy { RecursoFondosEnPuntoDeVenta(ID_CLIENTE, ID_ENTIDAD_PRUEBAS, mockRepositorio, mockManejadorSeguridad) }

        @Nested
        @DisplayName("Al consultar todos")
        inner class ConsultarTodos
        {
            private val parametros = IdUbicacionConsultaConsumibles(ID_ENTIDAD_PRUEBAS)
            @Test
            fun retorna_una_lista_vacia_de_dtos_cuando_el_repositorio_retorna_una_lista_vacia()
            {
                doReturn(sequenceOf<Fondo<*>>())
                    .`when`(mockRepositorio)
                    .listarSegunParametros(ID_CLIENTE, parametros)

                val listaRetornada = recursoTodasEntidades.darTodas()

                Assertions.assertTrue(listaRetornada.none())
                verify(mockRepositorio).listarSegunParametros(ID_CLIENTE, parametros)
            }

            @Test
            fun retorna_una_lista_de_dtos_correcta_cuando_el_repositorio_retorna_una_lista_de_entidades()
            {
                val listaNegocio = List(5) { darEntidadNegocioSegunIndice(it) }.asSequence()
                val listaDTO = List(5) { darEntidadDTOSegunIndice(it) }
                doReturn(listaNegocio)
                    .`when`(mockRepositorio)
                    .listarSegunParametros(ID_CLIENTE, parametros)

                val listaRetornada = recursoTodasEntidades.darTodas()

                assertEquals(listaDTO, listaRetornada.toList())
                verify(mockRepositorio).listarSegunParametros(ID_CLIENTE, parametros)
            }

            @Test
            fun lanza_excepcion_ErrorDesconocido_con_codigo_interno_correcto_cuando_el_repositorio_lanza_ErrorDeConsultaEntidad()
            {
                doThrow(ErrorDeConsultaEntidad("Consumible"))
                    .`when`(mockRepositorio)
                    .listarSegunParametros(ID_CLIENTE, parametros)

                val errorApi = assertThrows<ErrorDesconocido> { recursoTodasEntidades.darTodas() }

                Assertions.assertEquals(FondoDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                verify(mockRepositorio).listarSegunParametros(ID_CLIENTE, parametros)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .listarSegunParametros(ID_CLIENTE, parametros)

                val errorApi = assertThrows<EntidadNoExiste> { recursoTodasEntidades.darTodas() }

                Assertions.assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).listarSegunParametros(ID_CLIENTE, parametros)
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Fondos En Punto De Venta", PermisoBack.Accion.GET_TODOS)
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
                    doReturn(sequenceOf<Fondo<*>>())
                        .`when`(mockRepositorio)
                        .listarSegunParametros(ID_CLIENTE, parametros)
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteConPermiso(permisoBuscadoNegocio)
                    recursoTodasEntidades.darTodas()
                    verify(mockRepositorio).listarSegunParametros(ID_CLIENTE, parametros)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteNuncaEstaAutenticado
                    assertThrows<UsuarioNoAutenticado> { recursoTodasEntidades.darTodas() }
                    verify(mockRepositorio, times(0)).listarSegunParametros(ID_CLIENTE, parametros)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso()
                {
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio)
                    assertThrows<UsuarioNoTienePermiso> { recursoTodasEntidades.darTodas() }
                    verify(mockRepositorio, times(0)).listarSegunParametros(ID_CLIENTE, parametros)
                }
            }
        }
    }
}