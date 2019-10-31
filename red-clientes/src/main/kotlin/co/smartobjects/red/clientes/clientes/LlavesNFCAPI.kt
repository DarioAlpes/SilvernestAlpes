package co.smartobjects.red.clientes.clientes

import co.smartobjects.entidades.clientes.Cliente.LlaveNFC
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.base.ConsultarPorParametrosAPI
import co.smartobjects.red.clientes.base.CrearAPI
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.base.RespuestaVacia
import co.smartobjects.red.clientes.retrofit.clientes.LlavesNFCRetrofitAPI
import co.smartobjects.red.modelos.clientes.LlaveNFCDTO
import org.threeten.bp.ZonedDateTime

interface LlavesNFCAPI
    : CrearAPI<LlaveNFC, LlaveNFC>,
      ConsultarPorParametrosAPI<ZonedDateTime, LlaveNFC>
{
    fun eliminarHastaFechaCorte(fecha: ZonedDateTime): RespuestaVacia
}

internal class LlavesNFCAPIRetrofit
(
        private val apiDeLlavesNFC: LlavesNFCRetrofitAPI,
        private val parserRespuestasRetrofit: ParserRespuestasRetrofit,
        private val idCliente: Long
) : LlavesNFCAPI
{
    override fun crear(entidad: LlaveNFC): RespuestaIndividual<LlaveNFC>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeLlavesNFC.crear(idCliente, LlaveNFCDTO(entidad)).execute()
        }
    }

    override fun consultar(parametros: ZonedDateTime): RespuestaIndividual<LlaveNFC>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeLlavesNFC.consultar(idCliente, parametros).execute()
        }
    }

    override fun eliminarHastaFechaCorte(fecha: ZonedDateTime): RespuestaVacia
    {
        return parserRespuestasRetrofit.haciaRespuestaVacia {
            apiDeLlavesNFC.eliminarHastaFechaCorte(idCliente, fecha).execute()
        }
    }
}
