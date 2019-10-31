package co.smartobjects.red.clientes.ubicaciones.consumibles

import co.smartobjects.entidades.fondos.Fondo
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.base.ConsultarPorParametrosAPI
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.retrofit.ubicaciones.consumibles.FondosEnPuntoDeVentaRetrofitAPI

interface FondosEnPuntoDeVentaAPI
    : ConsultarPorParametrosAPI<Long, List<Fondo<*>>>

internal class FondosEnPuntoDeVentaAPIRetrofit
(
        private val apiDeFondosEnPuntoDeVenta: FondosEnPuntoDeVentaRetrofitAPI,
        private val parserRespuestasRetrofit: ParserRespuestasRetrofit,
        private val idCliente: Long
) : FondosEnPuntoDeVentaAPI
{
    override fun consultar(idUbicacion: Long): RespuestaIndividual<List<Fondo<*>>>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeFondosEnPuntoDeVenta
                .listar(idCliente, idUbicacion)
                .execute()

        }
    }
}
