package co.smartobjects.red.clientes.operativas.compras

import co.smartobjects.entidades.operativas.compras.Compra
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.base.*
import co.smartobjects.red.clientes.retrofit.operativas.compras.ComprasRetrofitAPI
import co.smartobjects.red.modelos.operativas.TransaccionEntidadTerminadaDTO
import co.smartobjects.red.modelos.operativas.compras.CompraDTO

interface ComprasAPI
    : ConsultarAPI<List<Compra>>,
      ActualizarPorParametrosAPI<String, Compra, Compra>,
      ConsultarPorParametrosAPI<String, Compra>,
      EliminarPorParametrosAPI<String, Compra>,
      ActualizarCamposPorParametrosAPI<String, TransaccionEntidadTerminadaDTO<Compra>>

internal class ComprasAPIRetrofit
(
        private val apiDeCompras: ComprasRetrofitAPI,
        private val parserRespuestasRetrofit: ParserRespuestasRetrofit,
        private val idCliente: Long
) : ComprasAPI
{
    override fun consultar(): RespuestaIndividual<List<Compra>>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualColeccionDesdeDTO {
            apiDeCompras
                .consultarTodasLasCompra(idCliente)
                .execute()
        }
    }

    override fun actualizar(parametros: String, entidad: Compra): RespuestaIndividual<Compra>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeCompras
                .actualizarCompra(idCliente, parametros, CompraDTO(entidad))
                .execute()
        }
    }

    override fun consultar(parametros: String): RespuestaIndividual<Compra>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeCompras
                .darCompra(idCliente, parametros)
                .execute()
        }
    }

    override fun eliminar(parametros: String): RespuestaVacia
    {
        return parserRespuestasRetrofit.haciaRespuestaVacia {
            apiDeCompras
                .eliminarCompra(idCliente, parametros)
                .execute()
        }
    }

    override fun actualizarCampos(parametros: String, entidad: TransaccionEntidadTerminadaDTO<Compra>): RespuestaVacia
    {
        return parserRespuestasRetrofit.haciaRespuestaVacia {
            apiDeCompras
                .actualizarPorCamposCompra(idCliente, parametros, entidad)
                .execute()
        }
    }
}
