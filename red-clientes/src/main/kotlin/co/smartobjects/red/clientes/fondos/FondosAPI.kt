package co.smartobjects.red.clientes.fondos

import co.smartobjects.entidades.fondos.Fondo
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.base.*
import co.smartobjects.red.clientes.retrofit.fondos.FondosRetrofitAPI

interface FondosAPI
    : ConsultarAPI<List<Fondo<*>>>,
      ConsultarPorParametrosAPI<Long, Fondo<*>>,
      EliminarPorParametrosAPI<Long, Fondo<*>>/*,
	  ActualizarCamposPorParametrosAPI<Long, co.smartobjects.red.modelos.fondos.FondoDisponibleParaLaVentaDTO<co.smartobjects.entidades.fondos.Fondo<*>>>*/

internal class FondosAPIRetrofit
(
        private val apiDeFondos: FondosRetrofitAPI,
        private val parserRespuestasRetrofit: ParserRespuestasRetrofit,
        private val idCliente: Long
) : FondosAPI
{
    override fun consultar(): RespuestaIndividual<List<Fondo<*>>>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeFondos
                .listar(idCliente)
                .execute()
        }
    }

    override fun consultar(parametros: Long): RespuestaIndividual<Fondo<*>>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeFondos
                .consultar(idCliente, parametros)
                .execute()
        }
    }

    override fun eliminar(parametros: Long): RespuestaVacia
    {
        return parserRespuestasRetrofit.haciaRespuestaVacia {
            apiDeFondos
                .eliminar(idCliente, parametros)
                .execute()
        }
    }
    /*override fun actualizarCampos(idFondo : Long, entidad: co.smartobjects.red.modelos.fondos.FondoDisponibleParaLaVentaDTO<co.smartobjects.entidades.fondos.Fondo<*>>): RespuestaVacia
    {
        return parserRespuestasRetrofit.haciaRespuestaVacia {
            apiDeFondo
                .actualizarPorCamposIndividuales(idCliente, parametros.idFondo, entidad)
                .execute()
        }
    }*/
}
