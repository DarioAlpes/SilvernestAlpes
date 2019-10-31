package co.smartobjects.red.clientes.personas.creditos

import co.smartobjects.entidades.operativas.compras.CreditosDeUnaPersona
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.base.ConsultarPorParametrosAPI
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.retrofit.personas.creditos.CreditosDeUnaPersonaRetrofitAPI
import org.threeten.bp.ZonedDateTime

interface CreditosDeUnaPersonaAPI
    : ConsultarPorParametrosAPI<CreditosDeUnaPersonaAPI.ParametrosBuscarRecursoCreditosDeUnaPersona, CreditosDeUnaPersona>
{
    data class ParametrosBuscarRecursoCreditosDeUnaPersona(val idPersona: Long, val fecha: ZonedDateTime)
}

internal class CreditosDeUnaPersonaAPIRetrofit
(
        private val apiDeCreditosDeUnaPersona: CreditosDeUnaPersonaRetrofitAPI,
        private val parserRespuestasRetrofit: ParserRespuestasRetrofit,
        private val idCliente: Long
) : CreditosDeUnaPersonaAPI
{
    override fun consultar(parametros: CreditosDeUnaPersonaAPI.ParametrosBuscarRecursoCreditosDeUnaPersona): RespuestaIndividual<CreditosDeUnaPersona>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeCreditosDeUnaPersona
                .buscar(idCliente, parametros.idPersona, parametros.fecha)
                .execute()
        }
    }
}
