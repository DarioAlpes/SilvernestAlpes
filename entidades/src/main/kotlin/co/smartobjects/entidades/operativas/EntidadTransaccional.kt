package co.smartobjects.entidades.operativas

import co.smartobjects.campos.CampoEntidad
import co.smartobjects.campos.CampoModificable
import co.smartobjects.campos.ValidadorCampoStringNoVacio
import co.smartobjects.entidades.excepciones.EntidadMalInicializada
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.threeten.bp.ZonedDateTime
import java.util.*

abstract class EntidadTransaccional<out TipoEntidad : EntidadTransaccional<TipoEntidad>> private constructor(
        val campoNombreUsuario: CampoNombreUsuario<TipoEntidad>,
        val uuid: UUID,
        val tiempoCreacion: Long,
        val campoCreacionTerminada: CampoCreacionTerminada<TipoEntidad>
                                                                                                            )
{
    protected constructor(
            nombreUsuario: String,
            uuid: UUID?,
            tiempoCreacion: Long?,
            creacionTerminada: Boolean?)
            : this(
            CampoNombreUsuario(nombreUsuario),
            uuid ?: UUID.randomUUID(),
            tiempoCreacion ?: ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO).toInstant().toEpochMilli(),
            CampoCreacionTerminada(creacionTerminada ?: false)
                  )

    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = EntidadTransaccional::class.java.simpleName

        const val SEPARADOR_COMPONENTES_ID = ':'

        fun idAPartes(id: String): PartesId
        {
            val partes = id.split(SEPARADOR_COMPONENTES_ID)
            if (partes.size != 3)
            {
                throw EntidadMalInicializada(NOMBRE_ENTIDAD, Campos.ID, id, "El id debe tener 3 partes separadas por $SEPARADOR_COMPONENTES_ID")
            }
            val tiempoCreacion = try
            {
                partes[0].toLong()
            }
            catch (e: NumberFormatException)
            {
                throw EntidadMalInicializada(NOMBRE_ENTIDAD, Campos.ID, id, "La primera parte del id debe ser un long representando el tiempo de creación", e)
            }

            val uuid = try
            {
                UUID.fromString(partes[2])
            }
            catch (e: IllegalArgumentException)
            {
                throw EntidadMalInicializada(NOMBRE_ENTIDAD, Campos.ID, id, "La tercera parte del id debe ser un uuid", e)
            }
            return PartesId(tiempoCreacion, partes[1], uuid)
        }
    }

    object Campos
    {
        @JvmField
        val ID = EntidadTransaccional<*>::id.name
        @JvmField
        val NOMBRE_USUARIO = EntidadTransaccional<*>::nombreUsuario.name
        @JvmField
        val CREACION_TERMINADA: String = EntidadTransaccional<*>::creacionTerminada.name
    }

    val nombreUsuario: String = campoNombreUsuario.valor
    val creacionTerminada: Boolean = campoCreacionTerminada.valor
    val id: String = "$tiempoCreacion$SEPARADOR_COMPONENTES_ID$nombreUsuario$SEPARADOR_COMPONENTES_ID$uuid"

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EntidadTransaccional<*>

        if (uuid != other.uuid) return false
        if (tiempoCreacion != other.tiempoCreacion) return false
        if (nombreUsuario != other.nombreUsuario) return false
        if (creacionTerminada != other.creacionTerminada) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = uuid.hashCode()
        result = 31 * result + tiempoCreacion.hashCode()
        result = 31 * result + nombreUsuario.hashCode()
        result = 31 * result + creacionTerminada.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "EntidadTransaccional(uuid=$uuid, tiempoCreacion=$tiempoCreacion, creacionTerminada=$creacionTerminada, nombreUsuario='$nombreUsuario')"
    }

    class CampoNombreUsuario<out TipoEntidad : EntidadTransaccional<TipoEntidad>>(nombreUsuario: String)
        : CampoEntidad<TipoEntidad, String>(nombreUsuario, ValidadorCampoStringNoVacio(), NOMBRE_ENTIDAD, Campos.NOMBRE_USUARIO)

    class CampoCreacionTerminada<out TipoEntidad : EntidadTransaccional<TipoEntidad>>(creacionTerminada: Boolean)
        : CampoModificable<TipoEntidad, Boolean>(creacionTerminada, null, NOMBRE_ENTIDAD, Campos.CREACION_TERMINADA)

    data class PartesId(val tiempoCreacion: Long, val nombreUsuario: String, val uuid: UUID)
    {
        val id: String = "$tiempoCreacion$SEPARADOR_COMPONENTES_ID$nombreUsuario$SEPARADOR_COMPONENTES_ID$uuid"
    }
}