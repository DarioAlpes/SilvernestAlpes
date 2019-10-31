package co.smartobjects.red.modelos.usuarios

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.usuarios.Usuario
import co.smartobjects.red.modelos.EntidadDTOParcial
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class UsuarioPatchDTO @JsonCreator constructor(
        @get:JsonProperty(value = UsuarioDTO.PropiedadesJson.CONTRASEÑA)
        val contraseña: CharArray?,

        @get:JsonProperty(value = UsuarioDTO.PropiedadesJson.ACTIVO)
        val activo: Boolean?)
    : EntidadDTOParcial<Usuario>
{
    override fun aConjuntoCamposModificables(): Set<CampoModificable<Usuario, *>>
    {
        val camposAActualizar = mutableSetOf<CampoModificable<Usuario, *>>()

        if (contraseña != null)
        {
            camposAActualizar.add(Usuario.CredencialesUsuario.CampoContraseña(contraseña))
        }

        if (activo != null)
        {
            camposAActualizar.add(Usuario.DatosUsuario.CampoActivo(activo))
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
        if (other !is UsuarioPatchDTO) return false

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