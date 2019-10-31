package co.smartobjects.red.clientes.fondos

import co.smartobjects.entidades.fondos.Entrada
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.base.*
import co.smartobjects.red.clientes.retrofit.fondos.EntradasRetrofitAPI
import co.smartobjects.red.modelos.fondos.EntradaDTO
import co.smartobjects.red.modelos.fondos.FondoDisponibleParaLaVentaDTO


interface EntradasAPI
    : CrearAPI<Entrada, Entrada>,
      ConsultarAPI<List<Entrada>>,
      ActualizarPorParametrosAPI<Long, Entrada, Entrada>,
      ConsultarPorParametrosAPI<Long, Entrada>,
      EliminarPorParametrosAPI<Long, Entrada>,
      ActualizarCamposPorParametrosAPI<Long, FondoDisponibleParaLaVentaDTO<Entrada>>

internal class EntradasAPIRetrofit
(
        private val apiDeEntradas: EntradasRetrofitAPI,
        private val parserRespuestasRetrofit: ParserRespuestasRetrofit,
        private val idCliente: Long
) : EntradasAPI
{
    override fun crear(entidad: Entrada): RespuestaIndividual<Entrada>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeEntradas
                .crearEntrada(idCliente, EntradaDTO(entidad))
                .execute()
        }
    }

    override fun consultar(): RespuestaIndividual<List<Entrada>>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualColeccionDesdeDTO {
            apiDeEntradas
                .consultarTodasLasEntrada(idCliente)
                .execute()
        }
    }

    override fun actualizar(parametros: Long, entidad: Entrada): RespuestaIndividual<Entrada>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeEntradas
                .actualizarEntrada(idCliente, parametros, EntradaDTO(entidad))
                .execute()
        }
    }

    override fun consultar(parametros: Long): RespuestaIndividual<Entrada>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeEntradas
                .darEntrada(idCliente, parametros)
                .execute()
        }
    }

    override fun eliminar(parametros: Long): RespuestaVacia
    {
        return parserRespuestasRetrofit.haciaRespuestaVacia {
            apiDeEntradas
                .eliminarEntrada(idCliente, parametros)
                .execute()
        }
    }

    override fun actualizarCampos(parametros: Long, entidad: FondoDisponibleParaLaVentaDTO<Entrada>): RespuestaVacia
    {
        return parserRespuestasRetrofit.haciaRespuestaVacia {
            apiDeEntradas
                .actualizarPorCamposEntrada(idCliente, parametros, entidad)
                .execute()
        }
    }
}
