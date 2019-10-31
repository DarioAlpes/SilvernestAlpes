package co.smartobjects.persistencia

import co.smartobjects.entidades.Jerarquico
import java.util.*

internal interface JerarquicoDAO : EntidadReferenciableDAO<Long?>
{
    companion object
    {
        const val SEPARADOR_LLAVES_JERARQUIAS = Jerarquico.SEPARADOR_LLAVES_JERARQUIAS
        const val NOMBRE_CAMPO_LLAVE = "llave_jerarquia"
    }

    val idDelPadre: Long?
    var llaveJerarquia: String

    fun darIdsAncestros() = LinkedHashSet<Long>(llaveJerarquia.split(SEPARADOR_LLAVES_JERARQUIAS).dropLast(1).map { it.toLong() })
}