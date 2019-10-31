package co.smartobjects.red.clientes.fondos

import co.smartobjects.entidades.fondos.Sku
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.base.*
import co.smartobjects.red.clientes.retrofit.fondos.SkusRetrofitAPI
import co.smartobjects.red.modelos.fondos.FondoDisponibleParaLaVentaDTO
import co.smartobjects.red.modelos.fondos.SkuDTO


interface SkusAPI
    : CrearAPI<Sku, Sku>,
      ConsultarAPI<List<Sku>>,
      ActualizarPorParametrosAPI<Long, Sku, Sku>,
      ConsultarPorParametrosAPI<Long, Sku>,
      EliminarPorParametrosAPI<Long, Sku>,
      ActualizarCamposPorParametrosAPI<Long, FondoDisponibleParaLaVentaDTO<Sku>>

internal class SkusAPIRetrofit
(
        private val apiDeSkus: SkusRetrofitAPI,
        private val parserRespuestasRetrofit: ParserRespuestasRetrofit,
        private val idCliente: Long
) : SkusAPI
{
    override fun crear(entidad: Sku): RespuestaIndividual<Sku>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeSkus
                .crearSku(idCliente, SkuDTO(entidad))
                .execute()
        }
    }

    override fun consultar(): RespuestaIndividual<List<Sku>>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualColeccionDesdeDTO {
            apiDeSkus
                .consultarTodasLasSku(idCliente)
                .execute()
        }
    }

    override fun actualizar(parametros: Long, entidad: Sku): RespuestaIndividual<Sku>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeSkus
                .actualizarSku(idCliente, parametros, SkuDTO(entidad))
                .execute()
        }
    }

    override fun consultar(parametros: Long): RespuestaIndividual<Sku>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeSkus
                .darSku(idCliente, parametros)
                .execute()
        }
    }

    override fun eliminar(parametros: Long): RespuestaVacia
    {
        return parserRespuestasRetrofit.haciaRespuestaVacia {
            apiDeSkus
                .eliminarSku(idCliente, parametros)
                .execute()
        }
    }

    override fun actualizarCampos(parametros: Long, entidad: FondoDisponibleParaLaVentaDTO<Sku>): RespuestaVacia
    {
        return parserRespuestasRetrofit.haciaRespuestaVacia {
            apiDeSkus
                .actualizarPorCamposSku(idCliente, parametros, entidad)
                .execute()
        }
    }
}
