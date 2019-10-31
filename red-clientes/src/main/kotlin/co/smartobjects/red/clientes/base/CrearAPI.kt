package co.smartobjects.red.clientes.base

interface CrearAPI<EntidadEnviada, EntidadRecibida>
{
    fun crear(entidad: EntidadEnviada): RespuestaIndividual<EntidadRecibida>
}