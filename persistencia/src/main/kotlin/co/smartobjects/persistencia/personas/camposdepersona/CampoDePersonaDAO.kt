package co.smartobjects.persistencia.personas.camposdepersona

import co.smartobjects.entidades.personas.CampoDePersona
import co.smartobjects.persistencia.EntidadDAO
import co.smartobjects.persistencia.EntidadReferenciableDAO
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable


@DatabaseTable(tableName = CampoDePersonaDAO.TABLA)
internal data class CampoDePersonaDAO constructor(
        @DatabaseField(columnName = COLUMNA_CAMPO, id = true, canBeNull = false)
        val campo: Predeterminado = Predeterminado.INVALIDO,

        @DatabaseField(columnName = COLUMNA_ES_REQUERIDO, canBeNull = false)
        val esRequerido: Boolean = false)
    : EntidadDAO<CampoDePersona>,
      EntidadReferenciableDAO<CampoDePersonaDAO.Predeterminado>
{
    override val id: Predeterminado = campo

    companion object
    {
        const val TABLA = "campo_de_persona"
        const val COLUMNA_CAMPO = "campo"
        const val COLUMNA_ES_REQUERIDO = "es_requerido"
    }

    constructor(entidadDeNegocio: CampoDePersona) :
            this(
                    Predeterminado.desdeNegocio(entidadDeNegocio.campo),
                    entidadDeNegocio.esRequerido.valor
                )

    override fun aEntidadDeNegocio(idCliente: Long): CampoDePersona
    {
        return CampoDePersona(campo.valorEnNegocio!!, esRequerido)
    }

    enum class Predeterminado(val valorEnNegocio: CampoDePersona.Predeterminado?)
    {
        INVALIDO(null),
        NOMBRE_COMPLETO(CampoDePersona.Predeterminado.NOMBRE_COMPLETO),
        TIPO_DOCUMENTO(CampoDePersona.Predeterminado.TIPO_DOCUMENTO),
        NUMERO_DOCUMENTO(CampoDePersona.Predeterminado.NUMERO_DOCUMENTO),
        GENERO(CampoDePersona.Predeterminado.GENERO),
        FECHA_NACIMIENTO(CampoDePersona.Predeterminado.FECHA_NACIMIENTO),
        CATEGORIA(CampoDePersona.Predeterminado.CATEGORIA),
        AFILIACION(CampoDePersona.Predeterminado.AFILIACION),
        ES_ANONIMA(CampoDePersona.Predeterminado.ES_ANONIMA),
        LLAVE_IMAGEN(CampoDePersona.Predeterminado.LLAVE_IMAGEN),
        EMPRESA(CampoDePersona.Predeterminado.EMPRESA),
        NIT_EMPRESA(CampoDePersona.Predeterminado.NIT_EMPRESA),
        TIPO(CampoDePersona.Predeterminado.TIPO);

        companion object
        {
            fun desdeNegocio(valorEnNegocio: CampoDePersona.Predeterminado): Predeterminado
            {
                return Predeterminado.values().first { it.valorEnNegocio == valorEnNegocio }
            }
        }
    }
}