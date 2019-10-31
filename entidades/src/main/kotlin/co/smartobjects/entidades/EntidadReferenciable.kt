package co.smartobjects.entidades

interface EntidadReferenciable<TipoId, EntidadSalida : EntidadReferenciable<TipoId, EntidadSalida>>
{
    val id: TipoId

    fun copiarConId(idNuevo: TipoId): EntidadSalida
}