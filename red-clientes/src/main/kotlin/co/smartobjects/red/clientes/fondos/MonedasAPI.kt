package co.smartobjects.red.clientes.fondos

import co.smartobjects.entidades.fondos.Dinero
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.base.*
import co.smartobjects.red.clientes.retrofit.fondos.MonedasRetrofitAPI
import co.smartobjects.red.modelos.fondos.DineroDTO
import co.smartobjects.red.modelos.fondos.FondoDisponibleParaLaVentaDTO


interface MonedasAPI
    : CrearAPI<Dinero, Dinero>,
      ConsultarAPI<List<Dinero>>,
      ActualizarPorParametrosAPI<Long, Dinero, Dinero>,
      ConsultarPorParametrosAPI<Long, Dinero>,
      EliminarPorParametrosAPI<Long, Dinero>,
      ActualizarCamposPorParametrosAPI<Long, FondoDisponibleParaLaVentaDTO<Dinero>>

internal class MonedasAPIRetrofit
(
        private val apiDeMonedas: MonedasRetrofitAPI,
        private val parserRespuestasRetrofit: ParserRespuestasRetrofit,
        private val idCliente: Long
) : MonedasAPI
{
    override fun crear(entidad: Dinero): RespuestaIndividual<Dinero>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeMonedas
                .crearDinero(idCliente, DineroDTO(entidad))
                .execute()
        }
    }

    override fun consultar(): RespuestaIndividual<List<Dinero>>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualColeccionDesdeDTO {
            apiDeMonedas
                .consultarTodasLasDinero(idCliente)
                .execute()
        }
    }

    override fun actualizar(parametros: Long, entidad: Dinero): RespuestaIndividual<Dinero>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeMonedas
                .actualizarDinero(idCliente, parametros, DineroDTO(entidad))
                .execute()
        }
    }

    override fun consultar(parametros: Long): RespuestaIndividual<Dinero>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeMonedas
                .darDinero(idCliente, parametros)
                .execute()
        }
    }

    override fun eliminar(parametros: Long): RespuestaVacia
    {
        return parserRespuestasRetrofit.haciaRespuestaVacia {
            apiDeMonedas
                .eliminarDinero(idCliente, parametros)
                .execute()
        }
    }

    override fun actualizarCampos(parametros: Long, entidad: FondoDisponibleParaLaVentaDTO<Dinero>): RespuestaVacia
    {
        return parserRespuestasRetrofit.haciaRespuestaVacia {
            apiDeMonedas
                .actualizarPorCamposDinero(idCliente, parametros, entidad)
                .execute()
        }
    }
}
