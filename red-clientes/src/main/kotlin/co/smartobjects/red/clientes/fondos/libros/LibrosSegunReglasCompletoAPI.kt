package co.smartobjects.red.clientes.fondos.libros

import co.smartobjects.entidades.fondos.libros.LibroSegunReglasCompleto
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.base.ConsultarAPI
import co.smartobjects.red.clientes.base.ConsultarPorParametrosAPI
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.retrofit.fondos.libros.LibrosSegunReglasCompletoRetrofitAPI

interface LibrosSegunReglasCompletoAPI
    : ConsultarAPI<List<LibroSegunReglasCompleto<*>>>,
      ConsultarPorParametrosAPI<Long, LibroSegunReglasCompleto<*>>

internal class LibrosSegunReglasCompletoAPIRetrofit
(
        private val apiDeLibrosSegunReglasCompleto: LibrosSegunReglasCompletoRetrofitAPI,
        private val parserRespuestasRetrofit: ParserRespuestasRetrofit,
        private val idCliente: Long
) : LibrosSegunReglasCompletoAPI
{
    override fun consultar(): RespuestaIndividual<List<LibroSegunReglasCompleto<*>>>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualColeccionDesdeDTO {
            apiDeLibrosSegunReglasCompleto
                .consultarTodasLasLibroSegunReglasCompleto(idCliente)
                .execute()
        }
    }

    override fun consultar(parametros: Long): RespuestaIndividual<LibroSegunReglasCompleto<*>>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeLibrosSegunReglasCompleto
                .darLibroSegunReglasCompleto(idCliente, parametros)
                .execute()
        }
    }
}
