package co.smartobjects.red.modelos.usuarios

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.usuarios.Usuario
import co.smartobjects.red.modelos.EntidadDTOParcial
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class ContraseñaUsuarioDTO @JsonCreator constructor
(
        @get:JsonProperty(value = UsuarioDTO.PropiedadesJson.CONTRASEÑA, required = true)
        @param:JsonProperty(value = UsuarioDTO.PropiedadesJson.CONTRASEÑA, required = true)
        val contraseña: CharArray
) : EntidadDTOParcial<Usuario>
{
    override fun aConjuntoCamposModificables(): Set<CampoModificable<Usuario, *>>
    {
        return setOf(Usuario.CredencialesUsuario.CampoContraseña(contraseña))
    }

    fun limpiarContraseña()
    {
        contraseña.fill('\u0000')
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ContraseñaUsuarioDTO

        if (!Arrays.equals(contraseña, other.contraseña)) return false

        return true
    }

    override fun hashCode(): Int
    {
        return Arrays.hashCode(contraseña)
    }
}