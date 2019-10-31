package co.smartobjects.red.clientes.personas

import co.smartobjects.entidades.personas.ValorGrupoEdad
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.base.*
import co.smartobjects.red.clientes.retrofit.personas.ValoresGrupoEdadRetrofitAPI
import co.smartobjects.red.modelos.personas.ValorGrupoEdadDTO


interface ValoresGrupoEdadAPI
    : CrearAPI<ValorGrupoEdad, ValorGrupoEdad>,
      ConsultarAPI<List<ValorGrupoEdad>>,
      ActualizarPorParametrosAPI<Long, ValorGrupoEdad, ValorGrupoEdad>,
      ConsultarPorParametrosAPI<Long, ValorGrupoEdad>,
      EliminarPorParametrosAPI<Long, ValorGrupoEdad>

internal class ValoresGrupoEdadAPIRetrofit
(
        private val apiDeValoresGrupoEdad: ValoresGrupoEdadRetrofitAPI,
        private val parserRespuestasRetrofit: ParserRespuestasRetrofit,
        private val idCliente: Long
) : ValoresGrupoEdadAPI
{
    override fun crear(entidad: ValorGrupoEdad): RespuestaIndividual<ValorGrupoEdad>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeValoresGrupoEdad
                .crearValorGrupoEdad(idCliente, ValorGrupoEdadDTO(entidad))
                .execute()
        }
    }

    override fun consultar(): RespuestaIndividual<List<ValorGrupoEdad>>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualColeccionDesdeDTO {
            apiDeValoresGrupoEdad
                .consultarTodasLasValorGrupoEdad(idCliente)
                .execute()
        }
    }

    override fun actualizar(parametros: Long, entidad: ValorGrupoEdad): RespuestaIndividual<ValorGrupoEdad>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeValoresGrupoEdad
                .actualizarValorGrupoEdad(idCliente, parametros, ValorGrupoEdadDTO(entidad))
                .execute()
        }
    }

    override fun consultar(parametros: Long): RespuestaIndividual<ValorGrupoEdad>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeValoresGrupoEdad
                .darValorGrupoEdad(idCliente, parametros)
                .execute()
        }
    }

    override fun eliminar(parametros: Long): RespuestaVacia
    {
        return parserRespuestasRetrofit.haciaRespuestaVacia {
            apiDeValoresGrupoEdad
                .eliminarValorGrupoEdad(idCliente, parametros)
                .execute()
        }
    }
}
