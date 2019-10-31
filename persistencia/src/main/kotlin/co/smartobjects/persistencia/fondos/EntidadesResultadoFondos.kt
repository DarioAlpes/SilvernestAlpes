package co.smartobjects.persistencia.fondos

internal interface EntidadFondoDAO<out EntidadDeNegocio>
{
    val fondoDAO: FondoDAO
    fun aEntidadDeNegocio(idCliente: Long, fondoDAO: FondoDAO): EntidadDeNegocio
}