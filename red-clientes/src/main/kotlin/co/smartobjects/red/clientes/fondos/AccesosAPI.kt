package co.smartobjects.red.clientes.fondos

import co.smartobjects.entidades.fondos.Acceso
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.base.*
import co.smartobjects.red.clientes.retrofit.fondos.AccesosRetrofitAPI
import co.smartobjects.red.modelos.fondos.AccesoDTO
import co.smartobjects.red.modelos.fondos.FondoDisponibleParaLaVentaDTO


interface AccesosAPI
    : CrearAPI<Acceso, Acceso>,
      ConsultarAPI<List<Acceso>>,
      ActualizarPorParametrosAPI<Long, Acceso, Acceso>,
      ConsultarPorParametrosAPI<Long, Acceso>,
      EliminarPorParametrosAPI<Long, Acceso>,
      ActualizarCamposPorParametrosAPI<Long, FondoDisponibleParaLaVentaDTO<Acceso>>

internal class AccesosAPIRetrofit
(
        private val apiDeAccesos: AccesosRetrofitAPI,
        private val parserRespuestasRetrofit: ParserRespuestasRetrofit,
        private val idCliente: Long
) : AccesosAPI
{
    override fun crear(entidad: Acceso): RespuestaIndividual<Acceso>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeAccesos
                .crearAcceso(idCliente, AccesoDTO(entidad))
                .execute()
        }
    }

    override fun consultar(): RespuestaIndividual<List<Acceso>>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualColeccionDesdeDTO {
            apiDeAccesos
                .consultarTodasLasAcceso(idCliente)
                .execute()
        }
    }

    override fun actualizar(parametros: Long, entidad: Acceso): RespuestaIndividual<Acceso>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeAccesos
                .actualizarAcceso(idCliente, parametros, AccesoDTO(entidad))
                .execute()
        }
    }

    override fun consultar(parametros: Long): RespuestaIndividual<Acceso>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeAccesos
                .darAcceso(idCliente, parametros)
                .execute()
        }
    }

    override fun eliminar(parametros: Long): RespuestaVacia
    {
        return parserRespuestasRetrofit.haciaRespuestaVacia {
            apiDeAccesos
                .eliminarAcceso(idCliente, parametros)
                .execute()
        }
    }

    override fun actualizarCampos(parametros: Long, entidad: FondoDisponibleParaLaVentaDTO<Acceso>): RespuestaVacia
    {
        return parserRespuestasRetrofit.haciaRespuestaVacia {
            apiDeAccesos
                .actualizarPorCamposAcceso(idCliente, parametros, entidad)
                .execute()
        }
    }

}
