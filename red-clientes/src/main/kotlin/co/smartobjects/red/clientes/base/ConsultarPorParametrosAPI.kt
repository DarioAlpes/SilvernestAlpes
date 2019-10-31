package co.smartobjects.red.clientes.base

interface ConsultarPorParametrosAPI<P, T>
{
    fun consultar(parametros: P): RespuestaIndividual<T>
}