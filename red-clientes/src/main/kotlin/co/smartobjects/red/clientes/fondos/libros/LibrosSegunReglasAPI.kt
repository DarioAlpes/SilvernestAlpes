package co.smartobjects.red.clientes.fondos.libros

import co.smartobjects.entidades.fondos.libros.LibroSegunReglas
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.base.*
import co.smartobjects.red.clientes.retrofit.fondos.libros.LibrosSegunReglasRetrofitAPI
import co.smartobjects.red.modelos.fondos.libros.LibroSegunReglasDTO


interface LibrosSegunReglasAPI
    : CrearAPI<LibroSegunReglas, LibroSegunReglas>,
      ConsultarAPI<List<LibroSegunReglas>>,
      ActualizarPorParametrosAPI<Long, LibroSegunReglas, LibroSegunReglas>,
      ConsultarPorParametrosAPI<Long, LibroSegunReglas>,
      EliminarPorParametrosAPI<Long, LibroSegunReglas>

internal class LibrosSegunReglasAPIRetrofit
(
        private val apiDeLibrosSegunReglas: LibrosSegunReglasRetrofitAPI,
        private val parserRespuestasRetrofit: ParserRespuestasRetrofit,
        private val idCliente: Long
) : LibrosSegunReglasAPI
{
    override fun crear(entidad: LibroSegunReglas): RespuestaIndividual<LibroSegunReglas>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeLibrosSegunReglas
                .crearLibroSegunReglas(idCliente, LibroSegunReglasDTO(entidad))
                .execute()
        }
    }

    override fun consultar(): RespuestaIndividual<List<LibroSegunReglas>>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualColeccionDesdeDTO {
            apiDeLibrosSegunReglas
                .consultarTodasLasLibroSegunReglas(idCliente)
                .execute()
        }
    }

    override fun actualizar(parametros: Long, entidad: LibroSegunReglas): RespuestaIndividual<LibroSegunReglas>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeLibrosSegunReglas
                .actualizarLibroSegunReglas(idCliente, parametros, LibroSegunReglasDTO(entidad))
                .execute()
        }
    }

    override fun consultar(parametros: Long): RespuestaIndividual<LibroSegunReglas>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeLibrosSegunReglas
                .darLibroSegunReglas(idCliente, parametros)
                .execute()
        }
    }

    override fun eliminar(parametros: Long): RespuestaVacia
    {
        return parserRespuestasRetrofit.haciaRespuestaVacia {
            apiDeLibrosSegunReglas
                .eliminarLibroSegunReglas(idCliente, parametros)
                .execute()
        }
    }
}
