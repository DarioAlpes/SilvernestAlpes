package co.smartobjects.red.clientes.usuarios

import co.smartobjects.entidades.usuarios.Rol
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.base.*
import co.smartobjects.red.clientes.retrofit.usuarios.RolesRetrofitAPI
import co.smartobjects.red.modelos.usuarios.RolDTO


interface RolesAPI
    : CrearAPI<Rol, Rol>,
      ConsultarAPI<List<Rol>>,
      ActualizarPorParametrosAPI<String, Rol, Rol>,
      ConsultarPorParametrosAPI<String, Rol>,
      EliminarPorParametrosAPI<String, Rol>

internal class RolesAPIRetrofit
(
        private val apiDeRoles: RolesRetrofitAPI,
        private val parserRespuestasRetrofit: ParserRespuestasRetrofit,
        private val idCliente: Long
) : RolesAPI
{
    override fun crear(entidad: Rol): RespuestaIndividual<Rol>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeRoles
                .crearRol(idCliente, RolDTO(entidad))
                .execute()
        }
    }

    override fun consultar(): RespuestaIndividual<List<Rol>>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualColeccionDesdeDTO {
            apiDeRoles
                .consultarTodasLasRol(idCliente)
                .execute()
        }
    }

    override fun actualizar(parametros: String, entidad: Rol): RespuestaIndividual<Rol>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeRoles
                .actualizarRol(idCliente, parametros, RolDTO(entidad))
                .execute()
        }
    }

    override fun consultar(parametros: String): RespuestaIndividual<Rol>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeRoles
                .darRol(idCliente, parametros)
                .execute()
        }
    }

    override fun eliminar(parametros: String): RespuestaVacia
    {
        return parserRespuestasRetrofit.haciaRespuestaVacia {
            apiDeRoles
                .eliminarRol(idCliente, parametros)
                .execute()
        }
    }
}
