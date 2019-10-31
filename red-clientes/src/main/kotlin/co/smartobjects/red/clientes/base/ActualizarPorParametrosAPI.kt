package co.smartobjects.red.clientes.base

interface ActualizarPorParametrosAPI<TParametro, TEntidadEnviada, TEntidadRecibida>
{
    fun actualizar(parametros: TParametro, entidad: TEntidadEnviada): RespuestaIndividual<TEntidadRecibida>
}