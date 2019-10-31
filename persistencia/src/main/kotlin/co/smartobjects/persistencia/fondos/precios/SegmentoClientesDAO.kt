package co.smartobjects.persistencia.fondos.precios

import co.smartobjects.entidades.fondos.precios.SegmentoClientes
import co.smartobjects.persistencia.EntidadDAO
import co.smartobjects.persistencia.fondos.precios.gruposclientes.GrupoClientesDAO
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable


@DatabaseTable(tableName = SegmentoClientesDAO.TABLA)
internal data class SegmentoClientesDAO(
        @DatabaseField(columnName = COLUMNA_ID, generatedId = true)
        val id: Long? = null,

        @DatabaseField(columnName = COLUMNA_NOMBRE_CAMPO, canBeNull = false, indexName = "indice_campo_valor")
        val campo: NombreCampoEnDAO = NombreCampoEnDAO.DESCONOCIDO,

        @DatabaseField(columnName = COLUMNA_VALOR_CAMPO, canBeNull = false, indexName = "indice_campo_valor")
        val valor: String = "",

        @DatabaseField(columnName = COLUMNA_ID_GRUPO_CLIENTES, foreign = true, columnDefinition = "BIGINT NOT NULL REFERENCES ${GrupoClientesDAO.TABLA}(${GrupoClientesDAO.COLUMNA_ID}) ON DELETE CASCADE")
        val grupoClientesDAO: GrupoClientesDAO = GrupoClientesDAO()
                                       ) : EntidadDAO<SegmentoClientes>
{
    companion object
    {
        const val TABLA = "segmento_cliente"
        const val COLUMNA_ID = "id_segmento_cliente"
        const val COLUMNA_NOMBRE_CAMPO = "nombre_campo"
        const val COLUMNA_VALOR_CAMPO = "valor_campo"

        const val COLUMNA_ID_GRUPO_CLIENTES = "fk_${TABLA}_${GrupoClientesDAO.TABLA}"
    }

    constructor(entidadDeNegocio: SegmentoClientes) :
            this(
                    null,
                    NombreCampoEnDAO.desdeNombreCampoEnNegocio(entidadDeNegocio.campo),
                    entidadDeNegocio.valor
                )

    override fun aEntidadDeNegocio(idCliente: Long): SegmentoClientes
    {
        return SegmentoClientes(
                id,
                campo.nombreCampoEnNegocio!!,
                valor
                               )
    }

    enum class NombreCampoEnDAO(val nombreCampoEnNegocio: SegmentoClientes.NombreCampo?)
    {
        CATEGORIA(SegmentoClientes.NombreCampo.CATEGORIA), GRUPO_DE_EDAD(SegmentoClientes.NombreCampo.GRUPO_DE_EDAD),TIPO(SegmentoClientes.NombreCampo.TIPO), DESCONOCIDO(null);

        companion object
        {
            fun desdeNombreCampoEnNegocio(nombreDeCampo: SegmentoClientes.NombreCampo): NombreCampoEnDAO
            {
                return NombreCampoEnDAO.values().first { it.nombreCampoEnNegocio == nombreDeCampo }
            }
        }
    }
}