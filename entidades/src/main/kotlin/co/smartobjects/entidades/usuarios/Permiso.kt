package co.smartobjects.entidades.usuarios

import co.smartobjects.campos.CampoEntidad
import co.smartobjects.campos.ValidadorCampoStringNoVacio
import java.io.Serializable

interface Permiso
{
    fun aStringDePermiso(): String
}

class PermisoBack private constructor(val idCliente: Long, @Transient val campoEndPoint: CampoEndPoint, val accion: Accion) : Permiso, Serializable
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = PermisoBack::class.java.simpleName
    }

    object Campos
    {
        @JvmField
        val ENDPOINT = PermisoBack::endPoint.name
    }

    constructor(idCliente: Long, nombrePermiso: String, accion: Accion) : this(idCliente, CampoEndPoint(nombrePermiso), accion)

    val endPoint: String = campoEndPoint.valor

    override fun aStringDePermiso(): String
    {
        return "$idCliente:$endPoint:$accion"
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PermisoBack

        if (idCliente != other.idCliente) return false
        if (accion != other.accion) return false
        if (endPoint != other.endPoint) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = idCliente.hashCode()
        result = 31 * result + accion.hashCode()
        result = 31 * result + endPoint.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "PermisoBack(idCliente=$idCliente, accion=$accion, endPoint='$endPoint')"
    }

    fun copiar(idCliente: Long = this.idCliente, endPoint: String = this.endPoint, accion: Accion = this.accion): PermisoBack
    {
        return PermisoBack(idCliente, endPoint, accion)
    }


    class CampoEndPoint(endPoint: String) : CampoEntidad<Permiso, String>(endPoint, ValidadorCampoStringNoVacio(), NOMBRE_ENTIDAD, Campos.ENDPOINT)


    enum class Accion
    {
        POST, PUT, PUT_TODOS, GET_TODOS, GET_UNO, PATCH, DELETE;
    }
}