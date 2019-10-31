package co.smartobjects.entidades.ubicaciones.contabilizables


data class UbicacionesContabilizables(val idCliente: Long, val idsUbicaciones: Set<Long>)
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = UbicacionesContabilizables::class.java.simpleName
    }


    fun copiar(idCliente: Long = this.idCliente, idsUbicaciones: Set<Long> = this.idsUbicaciones): UbicacionesContabilizables
    {
        return copy(idCliente, idsUbicaciones)
    }
}