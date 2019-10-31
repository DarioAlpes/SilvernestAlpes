package co.smartobjects.red.clientes.ubicaciones.contabilizables

import co.smartobjects.entidades.ubicaciones.contabilizables.ConteoUbicacion
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.base.CrearPorParametrosAPI
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.retrofit.ubicaciones.contabilizables.ConteosEnUbicacionRetrofitAPI
import co.smartobjects.red.modelos.ubicaciones.contabilizables.ConteoUbicacionDTO

interface ConteosEnUbicacionAPI
    : CrearPorParametrosAPI<Long, ConteoUbicacion>

internal class ConteosEnUbicacionAPIRetrofit
(
        private val apiDeConteosEnUbicacion: ConteosEnUbicacionRetrofitAPI,
        private val parserRespuestasRetrofit: ParserRespuestasRetrofit,
        private val idCliente: Long
) : ConteosEnUbicacionAPI
{
    override fun crear(idUbicacion: Long, entidad: ConteoUbicacion): RespuestaIndividual<ConteoUbicacion>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeConteosEnUbicacion
                .crearConteoUbicacion(idCliente, idUbicacion, ConteoUbicacionDTO(entidad))
                .execute()
        }
    }
}
