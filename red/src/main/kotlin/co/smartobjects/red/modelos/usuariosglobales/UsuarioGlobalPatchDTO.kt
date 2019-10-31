package co.smartobjects.red.modelos.usuariosglobales

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.usuariosglobales.UsuarioGlobal
import co.smartobjects.red.modelos.EntidadDTOParcial
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class UsuarioGlobalPatchDTO @JsonCreator constructor
(
        @get:JsonProperty(UsuarioGlobalDTO.PropiedadesJSON.CONTRASEÑA)
        @param:JsonProperty(UsuarioGlobalDTO.PropiedadesJSON.CONTRASEÑA)
        val contraseña: CharArray?,

        @get:JsonProperty(UsuarioGlobalDTO.PropiedadesJSON.ACTIVO)
        @param:JsonProperty(UsuarioGlobalDTO.PropiedadesJSON.ACTIVO)
        val activo: Boolean?
) : EntidadDTOParcial<UsuarioGlobal>
{
    override fun aConjuntoCamposModificables(): Set<CampoModificable<UsuarioGlobal, *>>
    {
        val camposAActualizar = mutableSetOf<CampoModificable<UsuarioGlobal, *>>()

        if (contraseña != null)
        {
            camposAActualizar.add(UsuarioGlobal.CredencialesUsuario.CampoContraseña(contraseña))
        }

        if (activo != null)
        {
            camposAActualizar.add(UsuarioGlobal.DatosUsuario.CampoActivo(activo))
        }

        return camposAActualizar
    }

    fun limpiarContraseña()
    {
        contraseña?.fill('\u0000')
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (other !is UsuarioGlobalPatchDTO) return false

        if (!Arrays.equals(contraseña, other.contraseña)) return false
        if (activo != other.activo) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = contraseña?.let { Arrays.hashCode(it) } ?: 0
        result = 31 * result + activo.hashCode()
        return result
    }
}