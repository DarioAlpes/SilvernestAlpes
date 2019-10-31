package co.smartobjects.persistencia.fondos.categoriaskus

import co.smartobjects.entidades.fondos.CategoriaSku
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.persistencia.JerarquicoDAO
import co.smartobjects.persistencia.fondos.EntidadFondoDAO
import co.smartobjects.persistencia.fondos.FondoDAO
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable(tableName = CategoriaSkuDAO.TABLA)
internal data class CategoriaSkuDAO(
        @DatabaseField(columnName = COLUMNA_ID_DUMMY, id = true)
        override val id: Long? = null,

        @DatabaseField(columnName = COLUMNA_ID, foreign = true, uniqueCombo = true, uniqueIndexName = "ux_${COLUMNA_ID}_$TABLA", columnDefinition = "BIGINT NOT NULL REFERENCES ${FondoDAO.TABLA}(${FondoDAO.COLUMNA_ID}) ON DELETE CASCADE")
        override val fondoDAO: FondoDAO = FondoDAO(),

        @DatabaseField(columnName = COLUMNA_ID_PADRE, foreign = true, index = true, columnDefinition = "BIGINT REFERENCES $TABLA($COLUMNA_ID)")
        val padreDAO: CategoriaSkuDAO? = null,

        @DatabaseField(columnName = JerarquicoDAO.NOMBRE_CAMPO_LLAVE, index = true, canBeNull = false)
        override var llaveJerarquia: String = "",

        @DatabaseField(columnName = COLUMNA_LLAVE_ICONO)
        val llaveDeIcono: String? = null)
    : JerarquicoDAO,
      EntidadFondoDAO<CategoriaSku>
{
    companion object
    {
        const val TABLA = "categoria_sku"
        const val COLUMNA_ID_DUMMY = "id_dummy_categoria"
        const val COLUMNA_ID = "id_categoria_sku"
        const val COLUMNA_ID_PADRE = "${COLUMNA_ID}_padre"
        const val COLUMNA_LLAVE_ICONO = "llave_icono_categoria"
    }

    override val idDelPadre: Long?
        get() = padreDAO?.id

    constructor(entidadDeNegocio: CategoriaSku) :
            this(
                    entidadDeNegocio.id,
                    FondoDAO(entidadDeNegocio),
                    CategoriaSkuDAO(entidadDeNegocio.idDelPadre),
                    entidadDeNegocio.darLlaveJerarquia(),
                    entidadDeNegocio.llaveDeIcono
                )

    constructor(idComoLlaveForanea: Long) :
            this(
                    id = idComoLlaveForanea,
                    fondoDAO = FondoDAO(id = idComoLlaveForanea, tipoDeFondo = FondoDAO.TipoDeFondoEnBD.CATEGORIA_SKU)
                )

    override fun aEntidadDeNegocio(idCliente: Long, fondoDAO: FondoDAO): CategoriaSku
    {
        return CategoriaSku(
                idCliente,
                fondoDAO.id,
                fondoDAO.nombre,
                fondoDAO.disponibleParaLaVenta,
                fondoDAO.debeAparecerSoloUnaVez,
                fondoDAO.esIlimitado,
                Precio(fondoDAO.precio, fondoDAO.impuestoPorDefectoDAO.id!!),
                fondoDAO.codigoExterno,
                padreDAO?.id,
                darIdsAncestros(),
                llaveDeIcono
                           )
    }
}