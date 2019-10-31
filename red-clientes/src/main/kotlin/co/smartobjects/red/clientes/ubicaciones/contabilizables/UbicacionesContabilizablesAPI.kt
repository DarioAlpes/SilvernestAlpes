package co.smartobjects.red.clientes.ubicaciones.contabilizables

import co.smartobjects.entidades.ubicaciones.contabilizables.UbicacionesContabilizables
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.base.ConsultarAPI
import co.smartobjects.red.clientes.base.CrearAPI
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.retrofit.ubicaciones.contabilizables.UbicacionesContabilizablesRetrofitAPI
import co.smartobjects.red.modelos.ubicaciones.contabilizables.UbicacionesContabilizablesDTO

interface UbicacionesContabilizablesAPI
    : CrearAPI<UbicacionesContabilizables, List<Long>>,
      ConsultarAPI<List<Long>>

internal class UbicacionesContabilizablesAPIRetrofit
(
        private val apiDeUbicacionesContabilizables: UbicacionesContabilizablesRetrofitAPI,
        private val parserRespuestasRetrofit: ParserRespuestasRetrofit,
        private val idCliente: Long
) : UbicacionesContabilizablesAPI
{
    override fun crear(entidad: UbicacionesContabilizables): RespuestaIndividual<List<Long>>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualColeccionSimple {
            apiDeUbicacionesContabilizables
                .crear(idCliente, UbicacionesContabilizablesDTO(entidad))
                .execute()
        }
    }

    override fun consultar(): RespuestaIndividual<List<Long>>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualColeccionSimple {
            apiDeUbicacionesContabilizables
                .darTodas(idCliente)
                .execute()
        }
    }
}