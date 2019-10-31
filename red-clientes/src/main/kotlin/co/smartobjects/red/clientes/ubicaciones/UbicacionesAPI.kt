package co.smartobjects.red.clientes.ubicaciones

import co.smartobjects.entidades.ubicaciones.Ubicacion
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.base.*
import co.smartobjects.red.clientes.retrofit.ubicaciones.UbicacionesRetrofitAPI
import co.smartobjects.red.modelos.ubicaciones.UbicacionDTO


interface UbicacionesAPI
    : CrearAPI<Ubicacion, Ubicacion>,
      ConsultarAPI<List<Ubicacion>>,
      ActualizarPorParametrosAPI<Long, Ubicacion, Ubicacion>,
      ConsultarPorParametrosAPI<Long, Ubicacion>,
      EliminarPorParametrosAPI<Long, Ubicacion>

internal class UbicacionesAPIRetrofit
(
        private val apiDeUbicaciones: UbicacionesRetrofitAPI,
        private val parserRespuestasRetrofit: ParserRespuestasRetrofit,
        private val idCliente: Long
) : UbicacionesAPI
{
    override fun crear(entidad: Ubicacion): RespuestaIndividual<Ubicacion>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeUbicaciones
                .crearUbicacion(idCliente, UbicacionDTO(entidad))
                .execute()
        }
    }

    override fun consultar(): RespuestaIndividual<List<Ubicacion>>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualColeccionDesdeDTO {
            apiDeUbicaciones
                .consultarTodasLasUbicacion(idCliente)
                .execute()
        }
    }

    override fun actualizar(parametros: Long, entidad: Ubicacion): RespuestaIndividual<Ubicacion>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeUbicaciones
                .actualizarUbicacion(idCliente, parametros, UbicacionDTO(entidad))
                .execute()
        }
    }

    override fun consultar(parametros: Long): RespuestaIndividual<Ubicacion>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeUbicaciones
                .darUbicacion(idCliente, parametros)
                .execute()
        }
    }

    override fun eliminar(parametros: Long): RespuestaVacia
    {
        return parserRespuestasRetrofit.haciaRespuestaVacia {
            apiDeUbicaciones
                .eliminarUbicacion(idCliente, parametros)
                .execute()
        }
    }
}
