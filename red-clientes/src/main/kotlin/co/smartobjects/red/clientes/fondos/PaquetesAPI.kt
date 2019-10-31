package co.smartobjects.red.clientes.fondos

import co.smartobjects.entidades.fondos.Paquete
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.base.*
import co.smartobjects.red.clientes.retrofit.fondos.PaquetesRetrofitAPI
import co.smartobjects.red.modelos.fondos.PaqueteDTO
import co.smartobjects.red.modelos.fondos.PaquetePatchDTO


interface PaquetesAPI
    : CrearAPI<Paquete, Paquete>,
      ConsultarAPI<List<Paquete>>,
      ActualizarPorParametrosAPI<Long, Paquete, Paquete>,
      ConsultarPorParametrosAPI<Long, Paquete>,
      EliminarPorParametrosAPI<Long, Paquete>,
      ActualizarCamposPorParametrosAPI<Long, PaquetePatchDTO>

internal class PaquetesAPIRetrofit
(
        private val apiDePaquetes: PaquetesRetrofitAPI,
        private val parserRespuestasRetrofit: ParserRespuestasRetrofit,
        private val idCliente: Long
) : PaquetesAPI
{
    override fun crear(entidad: Paquete): RespuestaIndividual<Paquete>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDePaquetes
                .crearPaquete(idCliente, PaqueteDTO(entidad))
                .execute()
        }
    }

    override fun consultar(): RespuestaIndividual<List<Paquete>>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualColeccionDesdeDTO {
            apiDePaquetes
                .consultarTodasLasPaquete(idCliente)
                .execute()
        }
    }

    override fun actualizar(parametros: Long, entidad: Paquete): RespuestaIndividual<Paquete>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDePaquetes
                .actualizarPaquete(idCliente, parametros, PaqueteDTO(entidad))
                .execute()
        }
    }

    override fun consultar(parametros: Long): RespuestaIndividual<Paquete>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDePaquetes
                .darPaquete(idCliente, parametros)
                .execute()
        }
    }

    override fun eliminar(parametros: Long): RespuestaVacia
    {
        return parserRespuestasRetrofit.haciaRespuestaVacia {
            apiDePaquetes
                .eliminarPaquete(idCliente, parametros)
                .execute()
        }
    }

    override fun actualizarCampos(parametros: Long, entidad: PaquetePatchDTO): RespuestaVacia
    {
        return parserRespuestasRetrofit.haciaRespuestaVacia {
            apiDePaquetes
                .actualizarPorCamposPaquete(idCliente, parametros, entidad)
                .execute()
        }
    }
}
