package co.smartobjects.persistencia.fondos.skus

import co.smartobjects.entidades.fondos.Sku
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.persistencia.fondos.EntidadFondoDAO
import co.smartobjects.persistencia.fondos.FondoDAO
import co.smartobjects.persistencia.fondos.categoriaskus.CategoriaSkuDAO
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable(tableName = SkuDAO.TABLA)
internal data class SkuDAO(
        @DatabaseField(columnName = COLUMNA_ID_DUMMY, id = true)
        val id: Long? = null,

        @DatabaseField(columnName = COLUMNA_ID, foreign = true, uniqueIndex = true, columnDefinition = "BIGINT NOT NULL REFERENCES ${FondoDAO.TABLA}(${FondoDAO.COLUMNA_ID}) ON DELETE CASCADE")
        override val fondoDAO: FondoDAO = FondoDAO(),

        @DatabaseField(columnName = COLUMNA_ID_CATEGORIA_SKU, foreign = true, canBeNull = false, index = true, columnDefinition = "BIGINT NOT NULL REFERENCES ${CategoriaSkuDAO.TABLA}(${CategoriaSkuDAO.COLUMNA_ID_DUMMY})")
        val categoriaSkuDAO: CategoriaSkuDAO = CategoriaSkuDAO(),

        @DatabaseField(columnName = COLUMNA_LLAVE_IMAGEN)
        val llaveDeImagen: String? = null
                          ) : EntidadFondoDAO<Sku>
{
    companion object
    {
        const val TABLA = "sku"
        const val COLUMNA_ID = "id_sku"
        const val COLUMNA_ID_DUMMY = "id_dummy_sku"
        const val COLUMNA_LLAVE_IMAGEN = "llave_imagen_sku"
        const val COLUMNA_ID_CATEGORIA_SKU = "fk_${TABLA}_${CategoriaSkuDAO.TABLA}"
    }

    constructor(entidadDeNegocio: Sku) :
            this(
                    entidadDeNegocio.id,
                    FondoDAO(entidadDeNegocio),
                    CategoriaSkuDAO(entidadDeNegocio.idDeCategoria),
                    entidadDeNegocio.llaveDeImagen
                )

    override fun aEntidadDeNegocio(idCliente: Long, fondoDAO: FondoDAO): Sku
    {
        return Sku(
                idCliente,
                fondoDAO.id,
                fondoDAO.nombre,
                fondoDAO.disponibleParaLaVenta,
                fondoDAO.debeAparecerSoloUnaVez,
                fondoDAO.esIlimitado,
                Precio(fondoDAO.precio, fondoDAO.impuestoPorDefectoDAO.id!!),
                fondoDAO.codigoExterno,
                categoriaSkuDAO.id!!,
                llaveDeImagen
                  )
    }
}