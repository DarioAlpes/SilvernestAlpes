package co.smartobjects.red.clientes.usuarios

import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.base.ConsultarAPI
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.retrofit.usuarios.PermisosPosiblesRetrofitAPI

interface PermisosPosiblesAPI
    : ConsultarAPI<List<PermisoBack>>

internal class PermisosPosiblesAPIRetrofit
(
        private val apiDePermisosPosibles: PermisosPosiblesRetrofitAPI,
        private val parserRespuestasRetrofit: ParserRespuestasRetrofit,
        private val idCliente: Long
) : PermisosPosiblesAPI
{
    override fun consultar(): RespuestaIndividual<List<PermisoBack>>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualColeccionDesdeDTO {
            apiDePermisosPosibles
                .consultarTodasLasPermisoBack(idCliente)
                .execute()
        }
    }
}
