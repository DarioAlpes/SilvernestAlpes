package co.smartobjects.red.clientes.fondos.libros

import co.smartobjects.entidades.fondos.libros.LibroDeProhibiciones
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.base.*
import co.smartobjects.red.clientes.retrofit.fondos.libros.LibrosDeProhibicionesRetrofitAPI
import co.smartobjects.red.modelos.fondos.libros.LibroDeProhibicionesDTO


interface LibrosDeProhibicionesAPI
    : CrearAPI<LibroDeProhibiciones, LibroDeProhibiciones>,
      ConsultarAPI<List<LibroDeProhibiciones>>,
      ActualizarPorParametrosAPI<Long, LibroDeProhibiciones, LibroDeProhibiciones>,
      ConsultarPorParametrosAPI<Long, LibroDeProhibiciones>,
      EliminarPorParametrosAPI<Long, LibroDeProhibiciones>

internal class LibrosDeProhibicionesAPIRetrofit
(
        private val apiDeLibrosDeProhibiciones: LibrosDeProhibicionesRetrofitAPI,
        private val parserRespuestasRetrofit: ParserRespuestasRetrofit,
        private val idCliente: Long
) : LibrosDeProhibicionesAPI
{
    override fun crear(entidad: LibroDeProhibiciones): RespuestaIndividual<LibroDeProhibiciones>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeLibrosDeProhibiciones
                .crearLibroDeProhibiciones(idCliente, LibroDeProhibicionesDTO(entidad))
                .execute()
        }
    }

    override fun consultar(): RespuestaIndividual<List<LibroDeProhibiciones>>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualColeccionDesdeDTO {
            apiDeLibrosDeProhibiciones
                .consultarTodasLasLibroDeProhibiciones(idCliente)
                .execute()
        }
    }

    override fun actualizar(parametros: Long, entidad: LibroDeProhibiciones): RespuestaIndividual<LibroDeProhibiciones>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeLibrosDeProhibiciones
                .actualizarLibroDeProhibiciones(idCliente, parametros, LibroDeProhibicionesDTO(entidad))
                .execute()
        }
    }

    override fun consultar(parametros: Long): RespuestaIndividual<LibroDeProhibiciones>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeLibrosDeProhibiciones
                .darLibroDeProhibiciones(idCliente, parametros)
                .execute()
        }
    }

    override fun eliminar(parametros: Long): RespuestaVacia
    {
        return parserRespuestasRetrofit.haciaRespuestaVacia {
            apiDeLibrosDeProhibiciones
                .eliminarLibroDeProhibiciones(idCliente, parametros)
                .execute()
        }
    }
}
