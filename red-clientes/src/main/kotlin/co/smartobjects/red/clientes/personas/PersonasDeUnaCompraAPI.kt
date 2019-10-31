package co.smartobjects.red.clientes.personas

import co.smartobjects.entidades.personas.Persona
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.base.ConsultarPorParametrosAPI
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.retrofit.personas.PersonasDeUnaCompraRetrofitAPI

interface PersonasDeUnaCompraAPI
    : ConsultarPorParametrosAPI<String, List<Persona>>

internal class PersonasDeUnaCompraAPIRetrofit
(
        private val apiDePersonasDeUnaCompra: PersonasDeUnaCompraRetrofitAPI,
        private val parserRespuestasRetrofit: ParserRespuestasRetrofit,
        private val idCliente: Long
) : PersonasDeUnaCompraAPI
{
    override fun consultar(numeroTransaccion: String): RespuestaIndividual<List<Persona>>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualColeccionDesdeDTO {
            apiDePersonasDeUnaCompra
                .listar(idCliente, numeroTransaccion)
                .execute()
        }
    }
}
