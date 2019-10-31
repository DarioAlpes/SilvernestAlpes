package co.smartobjects.red.clientes.fondos.libros

import co.smartobjects.entidades.fondos.libros.LibroDePrecios
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.base.*
import co.smartobjects.red.clientes.retrofit.fondos.libros.LibrosDePreciosRetrofitAPI
import co.smartobjects.red.modelos.fondos.libros.LibroDePreciosDTO


interface LibrosDePreciosAPI
    : CrearAPI<LibroDePrecios, LibroDePrecios>,
      ConsultarAPI<List<LibroDePrecios>>,
      ActualizarPorParametrosAPI<Long, LibroDePrecios, LibroDePrecios>,
      ConsultarPorParametrosAPI<Long, LibroDePrecios>,
      EliminarPorParametrosAPI<Long, LibroDePrecios>

internal class LibrosDePreciosAPIRetrofit
(
        private val apiDeLibrosDePrecios: LibrosDePreciosRetrofitAPI,
        private val parserRespuestasRetrofit: ParserRespuestasRetrofit,
        private val idCliente: Long
) : LibrosDePreciosAPI
{
    override fun crear(entidad: LibroDePrecios): RespuestaIndividual<LibroDePrecios>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeLibrosDePrecios
                .crearLibroDePrecios(idCliente, LibroDePreciosDTO(entidad))
                .execute()
        }
    }

    override fun consultar(): RespuestaIndividual<List<LibroDePrecios>>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualColeccionDesdeDTO {
            apiDeLibrosDePrecios
                .consultarTodasLasLibroDePrecios(idCliente)
                .execute()
        }
    }

    override fun actualizar(parametros: Long, entidad: LibroDePrecios): RespuestaIndividual<LibroDePrecios>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeLibrosDePrecios
                .actualizarLibroDePrecios(idCliente, parametros, LibroDePreciosDTO(entidad))
                .execute()
        }
    }

    override fun consultar(parametros: Long): RespuestaIndividual<LibroDePrecios>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeLibrosDePrecios
                .darLibroDePrecios(idCliente, parametros)
                .execute()
        }
    }

    override fun eliminar(parametros: Long): RespuestaVacia
    {
        return parserRespuestasRetrofit.haciaRespuestaVacia {
            apiDeLibrosDePrecios
                .eliminarLibroDePrecios(idCliente, parametros)
                .execute()
        }
    }
}
