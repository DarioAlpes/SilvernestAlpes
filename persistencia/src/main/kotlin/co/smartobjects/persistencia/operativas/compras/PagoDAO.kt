package co.smartobjects.persistencia.operativas.compras

import co.smartobjects.entidades.operativas.compras.Pago
import co.smartobjects.persistencia.EntidadDAO
import co.smartobjects.utilidades.Decimal
import com.j256.ormlite.field.DataType
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import java.math.BigDecimal

@DatabaseTable(tableName = PagoDAO.TABLA)
internal data class PagoDAO(
        @DatabaseField(columnName = COLUMNA_ID, generatedId = true)
        val id: Long? = null,

        @DatabaseField(columnName = COLUMNA_VALOR_PAGADO, canBeNull = false, dataType = DataType.BIG_DECIMAL_NUMERIC)
        val valorPagadoBigDecimal: BigDecimal = BigDecimal.ZERO,

        @DatabaseField(columnName = COLUMNA_METODO_PAGO, canBeNull = false)
        val metodoPago: MetodoDePago = MetodoDePago.EFECTIVO,

        @DatabaseField(columnName = COLUMNA_NUMERO_TRANSACCION_POS, canBeNull = false)
        val numeroDeTransaccionPOS: String = "",

        @DatabaseField(columnName = COLUMNA_ID_COMPRA, foreign = true, columnDefinition = "VARCHAR NOT NULL REFERENCES ${CompraDAO.TABLA}(${CompraDAO.COLUMNA_ID}) ON DELETE CASCADE")
        val compraDAO: CompraDAO = CompraDAO()
                           ) : EntidadDAO<Pago>
{
    companion object
    {
        const val TABLA = "pago"
        const val COLUMNA_ID = "id_pago"
        const val COLUMNA_VALOR_PAGADO = "valor_pagado"
        const val COLUMNA_METODO_PAGO = "metodo_pago"
        const val COLUMNA_NUMERO_TRANSACCION_POS = "numero_de_transaccion_pos"

        const val COLUMNA_ID_COMPRA = "fk_${TABLA}_${CompraDAO.TABLA}"
    }

    constructor(entidadDeNegocio: Pago, idCompra: String) :
            this(
                    null,
                    entidadDeNegocio.valorPagado.valor,
                    MetodoDePago.desdeNegocio(entidadDeNegocio.metodoPago),
                    entidadDeNegocio.numeroDeTransaccionPOS,
                    CompraDAO(id = idCompra)
                )

    override fun aEntidadDeNegocio(idCliente: Long): Pago
    {
        return Pago(Decimal(valorPagadoBigDecimal), metodoPago.valorEnNegocio, numeroDeTransaccionPOS)
    }

    enum class MetodoDePago(val valorEnNegocio: Pago.MetodoDePago)
    {
        EFECTIVO(Pago.MetodoDePago.EFECTIVO),
        TARJETA_CREDITO(Pago.MetodoDePago.TARJETA_CREDITO),
        TARJETA_DEBITO(Pago.MetodoDePago.TARJETA_DEBITO),
        TIC(Pago.MetodoDePago.TIC);

        companion object
        {
            fun desdeNegocio(valorEnNegocio: Pago.MetodoDePago): MetodoDePago
            {
                return values().first { it.valorEnNegocio == valorEnNegocio }
            }
        }
    }
}