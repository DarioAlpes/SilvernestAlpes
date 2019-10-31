package co.smartobjects.entidades.ubicaciones

import co.smartobjects.campos.CampoEntidad
import co.smartobjects.campos.ValidadorCampoStringNoVacio
import co.smartobjects.entidades.Jerarquico
import java.util.*

class Ubicacion private constructor
(
        val idCliente: Long,
        override val id: Long?,
        val campoNombre: CampoNombre,
        val tipo: Tipo,
        val subtipo: Subtipo,
        override val idDelPadre: Long?,
        override val idsDeAncestros: LinkedHashSet<Long>
) : Jerarquico<Ubicacion>
{
    constructor(
            idCliente: Long,
            id: Long?,
            nombre: String,
            tipo: Tipo,
            subtipo: Subtipo,
            idDelPadre: Long?,
            idsDeAncestros: LinkedHashSet<Long>
               ) : this(
            idCliente,
            id,
            CampoNombre(nombre),
            tipo,
            subtipo,
            idDelPadre,
            idsDeAncestros
                       )

    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = Ubicacion::class.java.simpleName
    }

    object Campos
    {
        @JvmField
        val NOMBRE = Ubicacion::nombre.name
    }

    val nombre = campoNombre.valor


    fun copiar(idCliente: Long = this.idCliente, id: Long? = this.id, nombre: String = this.nombre, tipo: Tipo = this.tipo, subtipo: Subtipo = this.subtipo, idDelPadre: Long? = this.idDelPadre, idsDeAncestros: LinkedHashSet<Long> = this.idsDeAncestros): Ubicacion
    {
        return Ubicacion(idCliente, id, nombre, tipo, subtipo, idDelPadre, idsDeAncestros)
    }

    override fun copiarConId(idNuevo: Long?): Ubicacion
    {
        return copiar(id = idNuevo)
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Ubicacion

        if (idCliente != other.idCliente) return false
        if (id != other.id) return false
        if (tipo != other.tipo) return false
        if (subtipo != other.subtipo) return false
        if (idDelPadre != other.idDelPadre) return false
        if (idsDeAncestros != other.idsDeAncestros) return false
        if (nombre != other.nombre) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = idCliente.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + tipo.hashCode()
        result = 31 * result + subtipo.hashCode()
        result = 31 * result + idDelPadre.hashCode()
        result = 31 * result + idsDeAncestros.hashCode()
        result = 31 * result + nombre.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "Ubicacion(idCliente=$idCliente, id=$id, tipo=$tipo, subtipo=$subtipo, idDelPadre=$idDelPadre, idsDeAncestros=$idsDeAncestros, nombre='$nombre')"
    }

    enum class Tipo
    {
        AREA,
        CIUDAD,
        PAIS,
        PROPIEDAD,
        PUNTO_DE_CONTACTO,
        PUNTO_DE_INTERES,
        REGION,
        ZONA
    }

    enum class Subtipo
    {
        AP,
        AP_INALAMBRICO,
        AP_RESTRINGIDO,
        KIOSKO,
        POS,
        POS_SIN_DINERO
    }

    class CampoNombre(nombre: String) : CampoEntidad<Ubicacion, String>(nombre, ValidadorCampoStringNoVacio(), NOMBRE_ENTIDAD, Campos.NOMBRE)
}