package co.smartobjects.red.clientes.usuarios

import co.smartobjects.entidades.usuarios.Usuario
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.base.*
import co.smartobjects.red.clientes.retrofit.usuarios.UsuariosRetrofitAPI
import co.smartobjects.red.modelos.usuarios.ContraseñaUsuarioDTO
import co.smartobjects.red.modelos.usuarios.UsuarioParaCreacionDTO
import co.smartobjects.red.modelos.usuarios.UsuarioPatchDTO

interface UsuariosAPI
    : CrearAPI<Usuario.UsuarioParaCreacion, Usuario>,
      ConsultarAPI<List<Usuario>>,
      ActualizarPorParametrosAPI<String, Usuario.UsuarioParaCreacion, Usuario>,
      ConsultarPorParametrosAPI<String, Usuario>,
      EliminarPorParametrosAPI<String, Usuario>,
      ActualizarCamposPorParametrosAPI<String, UsuarioPatchDTO>
{
    fun login(credencialesUsuario: Usuario.CredencialesUsuario): RespuestaIndividual<Usuario>
    fun logout(usuario: String): RespuestaVacia
}

internal class UsuariosAPIRetrofit
(
        private val apiDeUsuarios: UsuariosRetrofitAPI,
        private val parserRespuestasRetrofit: ParserRespuestasRetrofit,
        private val idCliente: Long
) : UsuariosAPI
{
    override fun consultar(): RespuestaIndividual<List<Usuario>>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualColeccionDesdeDTO {
            apiDeUsuarios
                .consultarTodos(idCliente)
                .execute()
        }
    }

    override fun crear(usuarioACrear: Usuario.UsuarioParaCreacion): RespuestaIndividual<Usuario>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeUsuarios
                .crear(idCliente, UsuarioParaCreacionDTO(usuarioACrear))
                .execute()
        }
    }

    override fun consultar(parametros: String): RespuestaIndividual<Usuario>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeUsuarios
                .consultar(idCliente, parametros)
                .execute()
        }
    }

    override fun eliminar(parametros: String): RespuestaVacia
    {
        return parserRespuestasRetrofit.haciaRespuestaVacia {
            apiDeUsuarios
                .eliminar(idCliente, parametros)
                .execute()
        }
    }

    override fun login(credencialesUsuario: Usuario.CredencialesUsuario): RespuestaIndividual<Usuario>
    {   val ps = "admin"
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeUsuarios
                //.login(idCliente, credencialesUsuario.usuario, ContraseñaUsuarioDTO(credencialesUsuario.contraseña))
                  .login(idCliente, credencialesUsuario.usuario.toLowerCase(), ContraseñaUsuarioDTO(ps.toCharArray()))
                  .execute()
        }
    }

    override fun logout(usuario: String): RespuestaVacia
    {
        return parserRespuestasRetrofit.haciaRespuestaVacia {
            apiDeUsuarios
                .logout(idCliente, usuario)
                .execute()
        }
    }

    override fun actualizar(nombreUsuario: String, entidad: Usuario.UsuarioParaCreacion): RespuestaIndividual<Usuario>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeUsuarios
                .actualizar(idCliente, nombreUsuario, UsuarioParaCreacionDTO(entidad))
                .execute()
        }
    }

    override fun actualizarCampos(parametros: String, entidad: UsuarioPatchDTO): RespuestaVacia
    {
        return parserRespuestasRetrofit.haciaRespuestaVacia {
            apiDeUsuarios
                .actualizarPorCampos(idCliente, parametros, entidad)
                .execute()
        }
    }
}
