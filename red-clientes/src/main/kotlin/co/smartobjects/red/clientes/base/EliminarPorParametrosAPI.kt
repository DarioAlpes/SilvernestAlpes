package co.smartobjects.red.clientes.base

interface EliminarPorParametrosAPI<P, T>
{
    fun eliminar(parametros: P): RespuestaVacia
}