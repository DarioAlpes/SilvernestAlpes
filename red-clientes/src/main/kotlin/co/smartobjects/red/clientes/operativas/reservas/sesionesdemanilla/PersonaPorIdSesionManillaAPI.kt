package co.smartobjects.red.clientes.operativas.reservas.sesionesdemanilla

import co.smartobjects.entidades.personas.Persona
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.base.ConsultarPorParametrosAPI
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.retrofit.operativas.reservas.sesionesdemanilla.PersonaPorIdSesionManillaRetrofitAPI

interface PersonaPorIdSesionManillaAPI : ConsultarPorParametrosAPI<Long, Persona>

internal class PersonaPorIdSesionManillaAPIRetrofit
(
        private val apiPersonaPorIdSesionManillaRetrofitAPI: PersonaPorIdSesionManillaRetrofitAPI,
        private val parserRespuestasRetrofit: ParserRespuestasRetrofit,
        private val idCliente: Long
) : PersonaPorIdSesionManillaAPI
{
    override fun consultar(parametros: Long): RespuestaIndividual<Persona>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiPersonaPorIdSesionManillaRetrofitAPI
                .darPersona(idCliente, parametros)
                .execute()
        }
    }
}
