package co.smartobjects.red.clientes.personas

import co.smartobjects.entidades.personas.DocumentoCompleto
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.personas.PersonaConFamiliares
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.base.*
import co.smartobjects.red.clientes.retrofit.personas.PersonasRetrofitAPI
import co.smartobjects.red.modelos.personas.PersonaDTO


interface PersonasAPI
    : CrearAPI<Persona, Persona>,
      ConsultarAPI<List<Persona>>,
      ActualizarAPI<Persona>,
      ConsultarPorParametrosAPI<Long, Persona>,
      EliminarPorParametrosAPI<Long, Persona>
{
    fun consultarPorDocumento(documento: DocumentoCompleto): RespuestaIndividual<PersonaConFamiliares>
}

internal class PersonasAPIRetrofit
(
        private val apiDePersonas: PersonasRetrofitAPI,
        private val parserRespuestasRetrofit: ParserRespuestasRetrofit,
        private val idCliente: Long
) : PersonasAPI
{
    override fun crear(entidad: Persona): RespuestaIndividual<Persona>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDePersonas
                .crearPersona(idCliente, PersonaDTO(entidad))
                .execute()
        }
    }

    override fun consultar(): RespuestaIndividual<List<Persona>>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualColeccionDesdeDTO {
            apiDePersonas
                .consultarTodasLasPersona(idCliente)
                .execute()
        }
    }

    override fun actualizar(entidad: Persona): RespuestaIndividual<Persona>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDePersonas
                .actualizarPersona(idCliente, entidad.id!!, PersonaDTO(entidad))
                .execute()
        }
    }

    override fun consultar(parametros: Long): RespuestaIndividual<Persona>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDePersonas
                .darPersona(idCliente, parametros)
                .execute()
        }
    }

    override fun eliminar(parametros: Long): RespuestaVacia
    {
        return parserRespuestasRetrofit.haciaRespuestaVacia {
            apiDePersonas
                .eliminarPersona(idCliente, parametros)
                .execute()
        }
    }

    override fun consultarPorDocumento(documento: DocumentoCompleto): RespuestaIndividual<PersonaConFamiliares>
    {
        println("\nConsultarDocumento")
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDePersonas
                    .consultarPorDocumento(idCliente, documento.numeroDocumento, documento.tipoDocumento.name)
                    .execute()
        }
    }
}
