package co.smartobjects.red.clientes.base

interface ActualizarAPI<T>
{
    fun actualizar(entidad: T): RespuestaIndividual<T>
}