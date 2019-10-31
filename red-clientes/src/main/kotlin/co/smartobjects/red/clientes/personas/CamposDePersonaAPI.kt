package co.smartobjects.red.clientes.personas

import co.smartobjects.entidades.personas.CampoDePersona
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.base.ConsultarAPI
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.retrofit.personas.CamposDePersonaRetrofitAPI

interface CamposDePersonaAPI
    : ConsultarAPI<List<CampoDePersona>>

internal class CamposDePersonaAPIRetrofit
(
        private val apiDeCamposDePersona: CamposDePersonaRetrofitAPI,
        private val parserRespuestasRetrofit: ParserRespuestasRetrofit,
        private val idCliente: Long
) : CamposDePersonaAPI
{
    override fun consultar(): RespuestaIndividual<List<CampoDePersona>>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualColeccionDesdeDTO {
            apiDeCamposDePersona
                .consultarTodasLasCampoDePersona(idCliente)
                .execute()
        }
    }
}
