package co.smartobjects.red.clientes.base

interface ActualizarCamposPorParametrosAPI<P, T>
{
    fun actualizarCampos(parametros: P, entidad: T): RespuestaVacia
}