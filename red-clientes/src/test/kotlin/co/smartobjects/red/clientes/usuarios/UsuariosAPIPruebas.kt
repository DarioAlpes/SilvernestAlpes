package co.smartobjects.red.clientes.usuarios

import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.entidades.usuarios.Rol
import co.smartobjects.entidades.usuarios.Usuario
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.PruebasUsandoServidorMock
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.base.RespuestaVacia
import co.smartobjects.red.clientes.cualquiera
import co.smartobjects.red.clientes.mockConDefaultAnswer
import co.smartobjects.red.clientes.retrofit.ManejadorDePeticiones
import co.smartobjects.red.modelos.usuarios.UsuarioDTO
import co.smartobjects.red.modelos.usuarios.UsuarioPatchDTO
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import retrofit2.Response
import kotlin.test.assertEquals


internal class UsuariosAPIPruebas : PruebasUsandoServidorMock<UsuariosAPI>()
{
    companion object
    {
        private const val ID_ENTIDAD = "2222"
    }

    private val entidadNegocioCreacion =
            Usuario.UsuarioParaCreacion(
                    Usuario.DatosUsuario(ID_CLIENTE, ID_ENTIDAD, "Prueba", "Prueba", true,"","","","","",""),
                    "lacontraseña".toCharArray(),
                    setOf(Rol.RolParaCreacionDeUsuario("Prueba"))
                                       )

    private val entidadNegocio =
            Usuario(
                    entidadNegocioCreacion.datosUsuario,
                    setOf(Rol("Prueba", "Prueba", setOf(PermisoBack(1, "Prueba", PermisoBack.Accion.GET_TODOS))))
                   )

    private val entidadDTO = UsuarioDTO(entidadNegocio)

    override fun ManejadorDePeticiones.extraerApi(): UsuariosAPI = apiDeUsuarios

    override val mockParser: ParserRespuestasRetrofit = mockConDefaultAnswer(ParserRespuestasRetrofit::class.java).also { mock ->
        doAnswer {
            it.getArgument<() -> Response<UsuarioDTO>>(0).invoke()
            RespuestaIndividual.Exitosa(entidadNegocio)
        }
            .`when`(mock)
            .haciaRespuestaIndividualDesdeDTO(cualquiera<() -> Response<UsuarioDTO>>())

        doAnswer {
            it.getArgument<() -> Response<Unit>>(0).invoke()
            RespuestaVacia.Exitosa
        }
            .`when`(mock)
            .haciaRespuestaVacia(cualquiera())

        doAnswer {
            it.getArgument<() -> Response<UsuarioDTO>>(0).invoke()
            RespuestaIndividual.Exitosa(listOf(entidadNegocio))
        }
            .`when`(mock)
            .haciaRespuestaIndividualColeccionDesdeDTO(cualquiera<() -> Response<List<UsuarioDTO>>>())
    }


    @Nested
    inner class Crear
    {
        private val jsonRespuesta = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTO)

        @Test
        fun invoca_el_metodo_de_parseo_de_respuesta_individual_dto()
        {
            llamarBackendCon(jsonRespuesta) {
                api.crear(entidadNegocioCreacion)
            }

            verify(mockParser).haciaRespuestaIndividualDesdeDTO(cualquiera<() -> Response<UsuarioDTO>>())
            verifyNoMoreInteractions(mockParser)
        }

        @Test
        fun invoca_url_correcta_usa_POST()
        {
            val peticionRealizada = llamarBackendCon(jsonRespuesta) {
                api.crear(entidadNegocioCreacion)
            }

            assertEquals("/clients/$ID_CLIENTE/users", peticionRealizada.path)
            assertEquals("POST", peticionRealizada.method)
        }
    }

    @Nested
    inner class Consultar
    {
        private val jsonRespuesta = "[" + ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTO) + "]"

        @Test
        fun invoca_el_metodo_de_parseo_de_respuesta_individual_de_una_coleccion_dto()
        {
            llamarBackendCon(jsonRespuesta) {
                api.consultar()
            }

            verify(mockParser).haciaRespuestaIndividualColeccionDesdeDTO(cualquiera<() -> Response<List<UsuarioDTO>>>())
            verifyNoMoreInteractions(mockParser)
        }

        @Test
        fun invoca_url_correcta_usa_GET()
        {
            val peticionRealizada = llamarBackendCon(jsonRespuesta) {
                api.consultar()
            }

            assertEquals("/clients/$ID_CLIENTE/users", peticionRealizada.path)
            assertEquals("GET", peticionRealizada.method)
        }
    }

    @Nested
    inner class Actualizar
    {
        private val jsonRespuesta = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTO)

        @Test
        fun invoca_el_metodo_de_parseo_de_respuesta_individual_dto()
        {
            llamarBackendCon(jsonRespuesta) {
                api.actualizar(ID_ENTIDAD, entidadNegocioCreacion)
            }

            verify(mockParser).haciaRespuestaIndividualDesdeDTO(cualquiera<() -> Response<UsuarioDTO>>())
            verifyNoMoreInteractions(mockParser)
        }

        @Test
        fun invoca_url_correcta_usa_PUT()
        {
            val peticionRealizada = llamarBackendCon(jsonRespuesta) {
                api.actualizar(ID_ENTIDAD, entidadNegocioCreacion)
            }

            assertEquals("/clients/$ID_CLIENTE/users/$ID_ENTIDAD", peticionRealizada.path)
            assertEquals("PUT", peticionRealizada.method)
        }
    }

    @Nested
    inner class ConsultarUno
    {
        private val jsonRespuesta = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTO)

        @Test
        fun invoca_el_metodo_de_parseo_de_respuesta_individual_dto()
        {
            llamarBackendCon(jsonRespuesta) {
                api.consultar(ID_ENTIDAD)
            }

            verify(mockParser).haciaRespuestaIndividualDesdeDTO(cualquiera<() -> Response<UsuarioDTO>>())
            verifyNoMoreInteractions(mockParser)
        }

        @Test
        fun invoca_url_correcta_usa_GET()
        {
            val peticionRealizada = llamarBackendCon(jsonRespuesta) {
                api.consultar(ID_ENTIDAD)
            }

            assertEquals("/clients/$ID_CLIENTE/users/$ID_ENTIDAD", peticionRealizada.path)
            assertEquals("GET", peticionRealizada.method)
        }
    }

    @Nested
    inner class Eliminar
    {
        @Test
        fun invoca_el_metodo_de_parseo_de_respuesta_vacia()
        {
            llamarBackendCon("{}") {
                api.eliminar(ID_ENTIDAD)
            }

            verify(mockParser).haciaRespuestaVacia(cualquiera())
            verifyNoMoreInteractions(mockParser)
        }

        @Test
        fun invoca_url_correcta_usa_DELETE()
        {
            val peticionRealizada = llamarBackendCon("{}") {
                api.eliminar(ID_ENTIDAD)
            }

            assertEquals("/clients/$ID_CLIENTE/users/$ID_ENTIDAD", peticionRealizada.path)
            assertEquals("DELETE", peticionRealizada.method)
        }
    }

    @Nested
    inner class Login
    {
        private val credenciales = Usuario.CredencialesUsuario("Prueba", "Contraseña".toCharArray())
        private val jsonRespuesta = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTO)

        @Test
        fun invoca_el_metodo_de_parseo_de_respuesta_individual_dto()
        {
            llamarBackendCon(jsonRespuesta) {
                api.login(credenciales)
            }

            verify(mockParser).haciaRespuestaIndividualDesdeDTO(cualquiera<() -> Response<UsuarioDTO>>())
            verifyNoMoreInteractions(mockParser)
        }

        @Test
        fun invoca_url_correcta_usa_POST()
        {
            val peticionRealizada = llamarBackendCon(jsonRespuesta) {
                api.login(credenciales)
            }

            assertEquals("/clients/$ID_CLIENTE/users/${credenciales.usuario}/login", peticionRealizada.path)
            assertEquals("POST", peticionRealizada.method)
        }
    }

    @Nested
    inner class Logout
    {
        @Test
        fun invoca_el_metodo_de_parseo_de_respuesta_vacia()
        {
            llamarBackendCon("{}") {
                api.logout(ID_ENTIDAD)
            }

            verify(mockParser).haciaRespuestaVacia(cualquiera())
            verifyNoMoreInteractions(mockParser)
        }

        @Test
        fun invoca_url_correcta_usa_DELETE()
        {
            val peticionRealizada = llamarBackendCon("{}") {
                api.logout(ID_ENTIDAD)
            }

            assertEquals("/clients/$ID_CLIENTE/users/$ID_ENTIDAD/logout", peticionRealizada.path)
            assertEquals("GET", peticionRealizada.method)
        }
    }

    @Nested
    inner class ActualizarPorCampos
    {
        private val entidadPatch = UsuarioPatchDTO("asdfas".toCharArray(), false)

        @Test
        fun invoca_el_metodo_de_parseo_de_respuesta_vacia()
        {
            llamarBackendCon("{}") {
                api.actualizarCampos(ID_ENTIDAD, entidadPatch)
            }

            verify(mockParser).haciaRespuestaVacia(cualquiera())
            verifyNoMoreInteractions(mockParser)
        }

        @Test
        fun invoca_url_correcta_usa_PATCH()
        {
            val peticionRealizada = llamarBackendCon("{}") {
                api.actualizarCampos(ID_ENTIDAD, entidadPatch)
            }

            assertEquals("/clients/$ID_CLIENTE/users/$ID_ENTIDAD", peticionRealizada.path)
            assertEquals("PATCH", peticionRealizada.method)
        }
    }
}
