package co.smartobjects.red.clientes.operativas.reservas.sesionesdemanilla

import co.smartobjects.entidades.operativas.reservas.SesionDeManilla
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.base.ActualizarCamposPorParametrosAPI
import co.smartobjects.red.clientes.base.ConsultarPorParametrosAPI
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.base.RespuestaVacia
import co.smartobjects.red.clientes.retrofit.operativas.reservas.sesionesdemanilla.SesionDeManillaRetrofitAPI
import co.smartobjects.red.modelos.operativas.reservas.SesionDeManillaPatchDTO
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.threeten.bp.ZonedDateTime

interface SesionDeManillaAPI
    : ConsultarPorParametrosAPI<Long, SesionDeManilla>,
      ActualizarCamposPorParametrosAPI<Long, SesionDeManillaAPI.ParametrosActualizacionParcial>
{
    sealed class ParametrosActualizacionParcial
    {
        class Activacion(val uuidTag: ByteArray) : ParametrosActualizacionParcial()
        {
            override fun equals(other: Any?): Boolean
            {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as Activacion

                if (!uuidTag.contentEquals(other.uuidTag)) return false

                return true
            }

            override fun hashCode(): Int
            {
                return uuidTag.contentHashCode()
            }
        }

        object Desactivacion : ParametrosActualizacionParcial()
    }
}

internal class SesionDeManillaAPIRetrofit
(
        private val apiDeSesionDeManilla: SesionDeManillaRetrofitAPI,
        private val parserRespuestasRetrofit: ParserRespuestasRetrofit,
        private val idCliente: Long
) : SesionDeManillaAPI
{
    override fun consultar(parametros: Long): RespuestaIndividual<SesionDeManilla>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeSesionDeManilla.consultar(idCliente, parametros).execute()
        }
    }

    override fun actualizarCampos(parametros: Long, entidad: SesionDeManillaAPI.ParametrosActualizacionParcial): RespuestaVacia
    {
        return parserRespuestasRetrofit.haciaRespuestaVacia {

            val objetoPatch = when (entidad)
            {
                is SesionDeManillaAPI.ParametrosActualizacionParcial.Activacion -> SesionDeManillaPatchDTO(entidad.uuidTag, null)
                SesionDeManillaAPI.ParametrosActualizacionParcial.Desactivacion -> SesionDeManillaPatchDTO(null, ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO))
            }

            apiDeSesionDeManilla.actualizarPorCamposSesionDeManilla(idCliente, parametros, objetoPatch).execute()
        }
    }
}
