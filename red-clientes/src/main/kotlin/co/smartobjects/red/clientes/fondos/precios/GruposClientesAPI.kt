package co.smartobjects.red.clientes.fondos.precios

import co.smartobjects.entidades.fondos.precios.GrupoClientes
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.base.*
import co.smartobjects.red.clientes.retrofit.fondos.precios.GruposClientesRetrofitAPI
import co.smartobjects.red.modelos.fondos.precios.GrupoClientesDTO
import co.smartobjects.red.modelos.fondos.precios.NombreGrupoClientesDTO


interface GruposClientesAPI
    : CrearAPI<GrupoClientes, GrupoClientes>,
      ConsultarAPI<List<GrupoClientes>>,
      ConsultarPorParametrosAPI<Long, GrupoClientes>,
      EliminarPorParametrosAPI<Long, GrupoClientes>,
      ActualizarCamposPorParametrosAPI<Long, NombreGrupoClientesDTO>

internal class GruposClientesAPIRetrofit
(
        private val apiDeGruposClientes: GruposClientesRetrofitAPI,
        private val parserRespuestasRetrofit: ParserRespuestasRetrofit,
        private val idCliente: Long
) : GruposClientesAPI
{
    override fun crear(entidad: GrupoClientes): RespuestaIndividual<GrupoClientes>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeGruposClientes
                .crearGrupoClientes(idCliente, GrupoClientesDTO(entidad))
                .execute()
        }
    }

    override fun consultar(): RespuestaIndividual<List<GrupoClientes>>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualColeccionDesdeDTO {
            apiDeGruposClientes
                .consultarTodasLasGrupoClientes(idCliente)
                .execute()
        }
    }

    override fun consultar(parametros: Long): RespuestaIndividual<GrupoClientes>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeGruposClientes
                .darGrupoClientes(idCliente, parametros)
                .execute()
        }
    }

    override fun eliminar(parametros: Long): RespuestaVacia
    {
        return parserRespuestasRetrofit.haciaRespuestaVacia {
            apiDeGruposClientes
                .eliminarGrupoClientes(idCliente, parametros)
                .execute()
        }
    }

    override fun actualizarCampos(parametros: Long, entidad: NombreGrupoClientesDTO): RespuestaVacia
    {
        return parserRespuestasRetrofit.haciaRespuestaVacia {
            apiDeGruposClientes
                .actualizarPorCamposGrupoClientes(idCliente, parametros, entidad)
                .execute()
        }
    }
}
