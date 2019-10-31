package co.smartobjects.red.clientes.personas

import co.smartobjects.entidades.personas.CampoDePersona
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.base.ActualizarPorParametrosAPI
import co.smartobjects.red.clientes.base.ConsultarPorParametrosAPI
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.retrofit.personas.CampoDePersonaRetrofitAPI
import co.smartobjects.red.modelos.personas.CampoDePersonaDTO


interface CampoDePersonaAPI
    : ActualizarPorParametrosAPI<Long, CampoDePersona, CampoDePersona>,
      ConsultarPorParametrosAPI<Long, CampoDePersona>

internal class CampoDePersonaAPIRetrofit
(
        private val apiDeCampoDePersona: CampoDePersonaRetrofitAPI,
        private val parserRespuestasRetrofit: ParserRespuestasRetrofit,
        private val idCliente: Long
) : CampoDePersonaAPI
{
    override fun actualizar(parametros: Long, entidad: CampoDePersona): RespuestaIndividual<CampoDePersona>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeCampoDePersona
                .actualizarCampoDePersona(idCliente, parametros, CampoDePersonaDTO(entidad))
                .execute()
        }
    }

    override fun consultar(parametros: Long): RespuestaIndividual<CampoDePersona>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeCampoDePersona
                .darCampoDePersona(idCliente, parametros)
                .execute()
        }
    }
}
