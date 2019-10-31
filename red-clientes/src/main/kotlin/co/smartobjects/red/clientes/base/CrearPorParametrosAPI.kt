package co.smartobjects.red.clientes.base

interface CrearPorParametrosAPI<P, T>
{
    fun crear(parametros: P, entidad: T): RespuestaIndividual<T>
}