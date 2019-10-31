package co.smartobjects.red.clientes.personas.compras

import co.smartobjects.entidades.operativas.compras.Compra
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.base.ConsultarPorParametrosAPI
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.retrofit.personas.compras.ComprasDeUnaPersonaRetrofitAPI
import org.threeten.bp.ZonedDateTime

interface ComprasDeUnaPersonaAPI
    : ConsultarPorParametrosAPI<ComprasDeUnaPersonaAPI.ParametrosListarRecursoComprasDeUnaPersona, List<Compra>>
{
    data class ParametrosListarRecursoComprasDeUnaPersona(val idPersona: Long, val fecha: ZonedDateTime)
}

internal class ComprasDeUnaPersonaAPIRetrofit
(
        private val apiDeComprasDeUnaPersona: ComprasDeUnaPersonaRetrofitAPI,
        private val parserRespuestasRetrofit: ParserRespuestasRetrofit,
        private val idCliente: Long
) : ComprasDeUnaPersonaAPI
{
    override fun consultar(parametros: ComprasDeUnaPersonaAPI.ParametrosListarRecursoComprasDeUnaPersona): RespuestaIndividual<List<Compra>>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualColeccionDesdeDTO {
            apiDeComprasDeUnaPersona
                .listar(idCliente, parametros.idPersona, parametros.fecha)
                .execute()
        }
    }
}
