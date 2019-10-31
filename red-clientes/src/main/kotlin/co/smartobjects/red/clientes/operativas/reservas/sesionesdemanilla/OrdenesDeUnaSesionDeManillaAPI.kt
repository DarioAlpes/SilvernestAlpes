package co.smartobjects.red.clientes.operativas.reservas.sesionesdemanilla

import co.smartobjects.entidades.operativas.ordenes.Orden
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.base.ConsultarPorParametrosAPI
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.retrofit.operativas.reservas.sesionesdemanilla.OrdenesDeUnaSesionDeManillaRetrofitAPI

interface OrdenesDeUnaSesionDeManillaAPI : ConsultarPorParametrosAPI<Long, List<Orden>>

internal class OrdenesDeUnaSesionDeManillaAPIRetrofit
(
        private val apiDeOrdenesDeUnaSesionDeManilla: OrdenesDeUnaSesionDeManillaRetrofitAPI,
        private val parserRespuestasRetrofit: ParserRespuestasRetrofit,
        private val idCliente: Long
) : OrdenesDeUnaSesionDeManillaAPI
{
	override fun consultar(parametros: Long): RespuestaIndividual<List<Orden>>
	{
		return parserRespuestasRetrofit.haciaRespuestaIndividualColeccionDesdeDTO {
			apiDeOrdenesDeUnaSesionDeManilla
				.listar(idCliente, parametros)
				.execute()
		}
	}
}
