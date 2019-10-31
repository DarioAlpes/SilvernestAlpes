package co.smartobjects.persistencia.ubicaciones.consumibles

import co.smartobjects.entidades.ubicaciones.consumibles.ConsumibleEnPuntoDeVenta
import co.smartobjects.persistencia.EntidadDAO
import co.smartobjects.persistencia.fondos.FondoDAO
import co.smartobjects.persistencia.ubicaciones.UbicacionDAO
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable(tableName = ConsumibleEnPuntoDeVentaDAO.TABLA)
internal data class ConsumibleEnPuntoDeVentaDAO(
        @DatabaseField(columnName = COLUMNA_ID, generatedId = true)
        val id: Long? = null,

        @DatabaseField(columnName = COLUMNA_ID_FONDO, uniqueCombo = true, foreign = true, columnDefinition = "BIGINT NOT NULL REFERENCES ${FondoDAO.TABLA}(${FondoDAO.COLUMNA_ID})")
        val consumibleDAO: FondoDAO = FondoDAO(),

        @DatabaseField(columnName = COLUMNA_CODIGO_EXTERNO_FONDO, canBeNull = false)
        val codigoExternoFondo: String = "",

        @DatabaseField(columnName = COLUMNA_ID_UBICACION, uniqueCombo = true, foreign = true, columnDefinition = "BIGINT NOT NULL REFERENCES ${UbicacionDAO.TABLA}(${UbicacionDAO.COLUMNA_ID})")
        val ubicacionDAO: UbicacionDAO = UbicacionDAO()
                                               ) : EntidadDAO<ConsumibleEnPuntoDeVenta>
{
    companion object
    {
        const val TABLA = "consumible_en_punto_de_venta"
        const val COLUMNA_ID = "id_dummy_consumible_en_punto_de_venta"
        const val COLUMNA_ID_FONDO = "fk_${TABLA}_${FondoDAO.TABLA}"
        const val COLUMNA_CODIGO_EXTERNO_FONDO = "codigo_externo_fondo"
        const val COLUMNA_ID_UBICACION = "fk_${TABLA}_${UbicacionDAO.TABLA}"
    }

    constructor(consumibleEnPuntoDeVenta: ConsumibleEnPuntoDeVenta) :
            this(
                    null,
                    FondoDAO(id = consumibleEnPuntoDeVenta.idConsumible),
                    consumibleEnPuntoDeVenta.codigoExternoConsumible,
                    UbicacionDAO(id = consumibleEnPuntoDeVenta.idUbicacion)
                )

    override fun aEntidadDeNegocio(idCliente: Long): ConsumibleEnPuntoDeVenta
    {
        return ConsumibleEnPuntoDeVenta(ubicacionDAO.id!!, consumibleDAO.id!!, codigoExternoFondo)
    }
}