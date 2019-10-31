package co.smartobjects.red.clientes.fondos.precios

import co.smartobjects.entidades.fondos.precios.Impuesto
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.base.*
import co.smartobjects.red.clientes.retrofit.fondos.precios.ImpuestosRetrofitAPI
import co.smartobjects.red.modelos.fondos.precios.ImpuestoDTO


interface ImpuestosAPI
    : CrearAPI<Impuesto, Impuesto>,
      ConsultarAPI<List<Impuesto>>,
      ActualizarPorParametrosAPI<Long, Impuesto, Impuesto>,
      ConsultarPorParametrosAPI<Long, Impuesto>,
      EliminarPorParametrosAPI<Long, Impuesto>

internal class ImpuestosAPIRetrofit
(
        private val apiDeImpuestos: ImpuestosRetrofitAPI,
        private val parserRespuestasRetrofit: ParserRespuestasRetrofit,
        private val idCliente: Long
) : ImpuestosAPI
{
    override fun crear(entidad: Impuesto): RespuestaIndividual<Impuesto>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeImpuestos
                .crearImpuesto(idCliente, ImpuestoDTO(entidad))
                .execute()
        }
    }

    override fun consultar(): RespuestaIndividual<List<Impuesto>>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualColeccionDesdeDTO {
            apiDeImpuestos
                .consultarTodasLasImpuesto(idCliente)
                .execute()
        }
    }

    override fun actualizar(parametros: Long, entidad: Impuesto): RespuestaIndividual<Impuesto>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeImpuestos
                .actualizarImpuesto(idCliente, parametros, ImpuestoDTO(entidad))
                .execute()
        }
    }

    override fun consultar(parametros: Long): RespuestaIndividual<Impuesto>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeImpuestos
                .darImpuesto(idCliente, parametros)
                .execute()
        }
    }

    override fun eliminar(parametros: Long): RespuestaVacia
    {
        return parserRespuestasRetrofit.haciaRespuestaVacia {
            apiDeImpuestos
                .eliminarImpuesto(idCliente, parametros)
                .execute()
        }
    }
}
