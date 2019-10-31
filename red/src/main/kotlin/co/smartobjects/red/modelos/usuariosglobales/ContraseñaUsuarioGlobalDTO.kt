package co.smartobjects.red.modelos.usuariosglobales

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.usuariosglobales.UsuarioGlobal
import co.smartobjects.red.modelos.EntidadDTOParcial
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class ContraseñaUsuarioGlobalDTO @JsonCreator constructor
(
        @get:JsonProperty(value = UsuarioGlobalDTO.PropiedadesJSON.CONTRASEÑA, required = true)
        @param:JsonProperty(value = UsuarioGlobalDTO.PropiedadesJSON.CONTRASEÑA, required = true)
        val contraseña: CharArray
) : EntidadDTOParcial<UsuarioGlobal>
{
    override fun aConjuntoCamposModificables(): Set<CampoModificable<UsuarioGlobal, *>>
    {
        return setOf(UsuarioGlobal.CredencialesUsuario.CampoContraseña(contraseña))
    }

    fun limpiarContraseña()
    {
        contraseña.fill('\u0000')
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ContraseñaUsuarioGlobalDTO

        if (!Arrays.equals(contraseña, other.contraseña)) return false

        return true
    }

    override fun hashCode(): Int
    {
        return Arrays.hashCode(contraseña)
    }
}