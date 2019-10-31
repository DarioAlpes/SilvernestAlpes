package co.smartobjects.entidades.fondos.libros

import co.smartobjects.campos.CampoEntidad
import co.smartobjects.campos.ValidadorCampoStringNoVacio
import co.smartobjects.entidades.EntidadReferenciable

class LibroSegunReglas private constructor(
        val idCliente: Long,
        override val id: Long?,
        val campoNombre: CampoNombre,
        val idLibro: Long,
        val reglasIdUbicacion: MutableSet<ReglaDeIdUbicacion>,
        val reglasIdGrupoDeClientes: MutableSet<ReglaDeIdGrupoDeClientes>,
        val reglasIdPaquete: MutableSet<ReglaDeIdPaquete>)
    : EntidadReferenciable<Long?, LibroSegunReglas>
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = LibroSegunReglas::class.java.simpleName
    }

    object Campos
    {
        @JvmField
        val NOMBRE = Libro<*>::nombre.name
    }

    val nombre = campoNombre.valor

    val reglas: Sequence<Regla<*>>
        get() = reglasIdUbicacion.asSequence() + reglasIdGrupoDeClientes + reglasIdPaquete


    constructor(
            idCliente: Long,
            id: Long?,
            nombre: String,
            idLibro: Long,
            reglasIdUbicacion: MutableSet<ReglaDeIdUbicacion>,
            reglasIdGrupoDeClientes: MutableSet<ReglaDeIdGrupoDeClientes>,
            reglasIdPaquete: MutableSet<ReglaDeIdPaquete>)
            : this(idCliente, id, LibroSegunReglas.CampoNombre(nombre), idLibro, reglasIdUbicacion, reglasIdGrupoDeClientes, reglasIdPaquete)

    fun copiar(
            idCliente: Long = this.idCliente,
            id: Long? = this.id,
            nombre: String = this.nombre,
            idLibro: Long = this.idLibro,
            reglasIdUbicacion: MutableSet<ReglaDeIdUbicacion> = this.reglasIdUbicacion,
            reglasIdGrupoDeClientes: MutableSet<ReglaDeIdGrupoDeClientes> = this.reglasIdGrupoDeClientes,
            reglasIdPaquete: MutableSet<ReglaDeIdPaquete> = this.reglasIdPaquete)
            : LibroSegunReglas
    {
        return LibroSegunReglas(idCliente, id, nombre, idLibro, reglasIdUbicacion, reglasIdGrupoDeClientes, reglasIdPaquete)
    }

    override fun copiarConId(idNuevo: Long?): LibroSegunReglas
    {
        return copiar(id = idNuevo)
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LibroSegunReglas

        if (idCliente != other.idCliente) return false
        if (id != other.id) return false
        if (idLibro != other.idLibro) return false
        if (reglasIdUbicacion != other.reglasIdUbicacion) return false
        if (reglasIdGrupoDeClientes != other.reglasIdGrupoDeClientes) return false
        if (reglasIdPaquete != other.reglasIdPaquete) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = idCliente.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + idLibro.hashCode()
        result = 31 * result + reglasIdUbicacion.hashCode()
        result = 31 * result + reglasIdGrupoDeClientes.hashCode()
        result = 31 * result + reglasIdPaquete.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "LibroSegunReglas(idCliente=$idCliente, id=$id, nombre='$nombre', libro=$idLibro, reglasIdUbicacion=$reglasIdUbicacion, reglasIdGrupoDeClientes=$reglasIdGrupoDeClientes, reglasIdPaquete=$reglasIdPaquete)"
    }

    class CampoNombre(nombre: String)
        : CampoEntidad<LibroSegunReglas, String>(nombre, ValidadorCampoStringNoVacio(), LibroSegunReglas.NOMBRE_ENTIDAD, LibroSegunReglas.Campos.NOMBRE)
}