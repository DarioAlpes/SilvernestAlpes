package co.smartobjects.entidades.fondos.libros

import co.smartobjects.campos.CampoEntidad
import co.smartobjects.campos.ValidadorCampoDosColeccionesAlMenosUnElemento

class LibroDeProhibiciones private constructor(
        idCliente: Long,
        id: Long?,
        nombre: String,
        val campoProhibicionesFondo: CampoProhibicionesFondo,
        val campoProhibicionesPaquete: CampoProhibicionesPaquete)
    : Libro<LibroDeProhibiciones>(idCliente, id, nombre, Tipo.PROHIBICIONES)
{
    constructor(
            idCliente: Long,
            id: Long?,
            nombre: String,
            prohibicionesDeFondo: Set<Prohibicion.DeFondo>,
            prohibicionesDePaquete: Set<Prohibicion.DePaquete>
               )
            : this(
            idCliente,
            id,
            nombre,
            CampoProhibicionesFondo(prohibicionesDeFondo, prohibicionesDePaquete),
            CampoProhibicionesPaquete(prohibicionesDePaquete, prohibicionesDeFondo)
                  )

    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = LibroDeProhibiciones::class.java.simpleName
    }

    object Campos
    {
        @JvmField
        val NOMBRE = Libro.Campos.NOMBRE
        @JvmField
        val PROHIBICIONES_FONDO = LibroDeProhibiciones::campoProhibicionesFondo.name
        @JvmField
        val PROHIBICIONES_PAQUETE = LibroDeProhibiciones::campoProhibicionesPaquete.name
    }

    val prohibicionesDeFondo: Set<Prohibicion.DeFondo> = campoProhibicionesFondo.valor
    val prohibicionesDePaquete: Set<Prohibicion.DePaquete> = campoProhibicionesPaquete.valor

    fun copiar(
            idCliente: Long = this.idCliente,
            id: Long? = this.id,
            nombre: String = this.nombre,
            prohibicionesDeFondo: Set<Prohibicion.DeFondo> = this.prohibicionesDeFondo,
            prohibicionesDePaquete: Set<Prohibicion.DePaquete> = this.prohibicionesDePaquete): LibroDeProhibiciones
    {
        return LibroDeProhibiciones(idCliente, id, nombre, prohibicionesDeFondo, prohibicionesDePaquete)
    }

    override fun copiarConId(idNuevo: Long?): LibroDeProhibiciones
    {
        return copiar(id = idNuevo)
    }


    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as LibroDeProhibiciones

        if (campoProhibicionesFondo != other.campoProhibicionesFondo) return false
        if (campoProhibicionesPaquete != other.campoProhibicionesPaquete) return false
        if (prohibicionesDeFondo != other.prohibicionesDeFondo) return false
        if (prohibicionesDePaquete != other.prohibicionesDePaquete) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = super.hashCode()
        result = 31 * result + campoProhibicionesFondo.hashCode()
        result = 31 * result + campoProhibicionesPaquete.hashCode()
        result = 31 * result + prohibicionesDeFondo.hashCode()
        result = 31 * result + prohibicionesDePaquete.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "LibroDeProhibiciones(campoProhibicionesFondo=$campoProhibicionesFondo, campoProhibicionesPaquete=$campoProhibicionesPaquete, prohibicionesDeFondo=$prohibicionesDeFondo, prohibicionesDePaquete=$prohibicionesDePaquete) ${super.toString()}"
    }


    class CampoProhibicionesFondo(prohibicionesDeFondo: Set<Prohibicion.DeFondo>, prohibicionesDePaquetes: Set<Prohibicion.DePaquete>)
        : CampoEntidad<LibroDeProhibiciones, Set<Prohibicion.DeFondo>>(
            prohibicionesDeFondo,
            ValidadorCampoDosColeccionesAlMenosUnElemento(prohibicionesDePaquetes, Campos.PROHIBICIONES_PAQUETE),
            NOMBRE_ENTIDAD,
            Campos.PROHIBICIONES_FONDO
                                                                      )

    class CampoProhibicionesPaquete(prohibicionesDePaquetes: Set<Prohibicion.DePaquete>, prohibicionesDeFondo: Set<Prohibicion.DeFondo>)
        : CampoEntidad<LibroDeProhibiciones, Set<Prohibicion.DePaquete>>(
            prohibicionesDePaquetes,
            ValidadorCampoDosColeccionesAlMenosUnElemento(prohibicionesDeFondo, Campos.PROHIBICIONES_FONDO),
            NOMBRE_ENTIDAD,
            Campos.PROHIBICIONES_PAQUETE
                                                                        )
}