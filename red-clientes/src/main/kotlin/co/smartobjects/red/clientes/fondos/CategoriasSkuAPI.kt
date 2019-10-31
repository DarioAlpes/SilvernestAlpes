package co.smartobjects.red.clientes.fondos

import co.smartobjects.entidades.fondos.CategoriaSku
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.base.*
import co.smartobjects.red.clientes.retrofit.fondos.CategoriasSkuRetrofitAPI
import co.smartobjects.red.modelos.fondos.CategoriaSkuDTO
import co.smartobjects.red.modelos.fondos.FondoDisponibleParaLaVentaDTO


interface CategoriasSkuAPI
    : CrearAPI<CategoriaSku, CategoriaSku>,
      ConsultarAPI<List<CategoriaSku>>,
      ActualizarPorParametrosAPI<Long, CategoriaSku, CategoriaSku>,
      ConsultarPorParametrosAPI<Long, CategoriaSku>,
      EliminarPorParametrosAPI<Long, CategoriaSku>,
      ActualizarCamposPorParametrosAPI<Long, FondoDisponibleParaLaVentaDTO<CategoriaSku>>

internal class CategoriasSkuAPIRetrofit
(
        private val apiDeCategoriasSku: CategoriasSkuRetrofitAPI,
        private val parserRespuestasRetrofit: ParserRespuestasRetrofit,
        private val idCliente: Long
) : CategoriasSkuAPI
{
    override fun crear(entidad: CategoriaSku): RespuestaIndividual<CategoriaSku>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeCategoriasSku
                .crearCategoriaSku(idCliente, CategoriaSkuDTO(entidad))
                .execute()
        }
    }

    override fun consultar(): RespuestaIndividual<List<CategoriaSku>>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualColeccionDesdeDTO {
            apiDeCategoriasSku
                .consultarTodasLasCategoriaSku(idCliente)
                .execute()
        }
    }

    override fun actualizar(parametros: Long, entidad: CategoriaSku): RespuestaIndividual<CategoriaSku>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeCategoriasSku
                .actualizarCategoriaSku(idCliente, parametros, CategoriaSkuDTO(entidad))
                .execute()
        }
    }

    override fun consultar(parametros: Long): RespuestaIndividual<CategoriaSku>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeCategoriasSku
                .darCategoriaSku(idCliente, parametros)
                .execute()
        }
    }

    override fun eliminar(parametros: Long): RespuestaVacia
    {
        return parserRespuestasRetrofit.haciaRespuestaVacia {
            apiDeCategoriasSku
                .eliminarCategoriaSku(idCliente, parametros)
                .execute()
        }
    }

    override fun actualizarCampos(parametros: Long, entidad: co.smartobjects.red.modelos.fondos.FondoDisponibleParaLaVentaDTO<co.smartobjects.entidades.fondos.CategoriaSku>): RespuestaVacia
    {
        return parserRespuestasRetrofit.haciaRespuestaVacia {
            apiDeCategoriasSku
                .actualizarPorCamposCategoriaSku(idCliente, parametros, entidad)
                .execute()
        }
    }
}
