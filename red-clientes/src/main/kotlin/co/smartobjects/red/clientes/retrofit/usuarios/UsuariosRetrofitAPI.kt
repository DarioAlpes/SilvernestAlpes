package co.smartobjects.red.clientes.retrofit.usuarios

import co.smartobjects.red.clientes.retrofit.RuteoClientesApi
import co.smartobjects.red.modelos.usuarios.ContraseñaUsuarioDTO
import co.smartobjects.red.modelos.usuarios.UsuarioDTO
import co.smartobjects.red.modelos.usuarios.UsuarioParaCreacionDTO
import co.smartobjects.red.modelos.usuarios.UsuarioPatchDTO
import retrofit2.Call
import retrofit2.http.*

internal interface UsuariosRetrofitAPI
{
    @GET(RuteoClientesApi.RuteoClienteApi.RuteoUsuariosApi.RUTA_RESUELTA)
    fun consultarTodos(@Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long): Call<List<UsuarioDTO>>

    @POST(RuteoClientesApi.RuteoClienteApi.RuteoUsuariosApi.RUTA_RESUELTA)
    fun crear(@Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long, @Body dto: UsuarioParaCreacionDTO): Call<UsuarioDTO>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoUsuariosApi.RuteoUsuarioApi.RUTA_RESUELTA)
    fun consultar(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoUsuariosApi.RuteoUsuarioApi.PARAMETRO_RUTA) usuario: String
                 ): Call<UsuarioDTO>

    @DELETE(RuteoClientesApi.RuteoClienteApi.RuteoUsuariosApi.RuteoUsuarioApi.RUTA_RESUELTA)
    fun eliminar(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoUsuariosApi.RuteoUsuarioApi.PARAMETRO_RUTA) usuario: String
                ): Call<Unit>

    @POST(RuteoClientesApi.RuteoClienteApi.RuteoUsuariosApi.RuteoUsuarioApi.RuteoLoginApi.RUTA_RESUELTA)
    fun login(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoUsuariosApi.RuteoUsuarioApi.PARAMETRO_RUTA) usuario_usuario: String,
            @Body contraseña: ContraseñaUsuarioDTO
             ): Call<UsuarioDTO>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoUsuariosApi.RuteoUsuarioApi.RuteoLogoutApi.RUTA_RESUELTA)
    fun logout(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoUsuariosApi.RuteoUsuarioApi.PARAMETRO_RUTA) usuario: String
              ): Call<Unit>

    @PUT(RuteoClientesApi.RuteoClienteApi.RuteoUsuariosApi.RuteoUsuarioApi.RUTA_RESUELTA)
    fun actualizar(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoUsuariosApi.RuteoUsuarioApi.PARAMETRO_RUTA) usuario: String,
            @Body dto: UsuarioParaCreacionDTO
                  ): Call<UsuarioDTO>

    @PATCH(RuteoClientesApi.RuteoClienteApi.RuteoUsuariosApi.RuteoUsuarioApi.RUTA_RESUELTA)
    fun actualizarPorCampos(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoUsuariosApi.RuteoUsuarioApi.PARAMETRO_RUTA) usuario: String,
            @Body camposAActualizar: UsuarioPatchDTO
                           ): Call<Unit>
}
