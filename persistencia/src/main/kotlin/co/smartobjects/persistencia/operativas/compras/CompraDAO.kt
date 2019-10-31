package co.smartobjects.persistencia.operativas.compras

import co.smartobjects.entidades.operativas.EntidadTransaccional
import co.smartobjects.entidades.operativas.compras.Compra
import co.smartobjects.persistencia.EntidadRelacionUnoAMuchos
import co.smartobjects.persistencia.operativas.ColumnasTransaccionales
import co.smartobjects.persistencia.persistoresormlite.ZonedDateTimeThreeTenType
import co.smartobjects.persistencia.usuarios.UsuarioDAO
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import org.threeten.bp.ZonedDateTime

@DatabaseTable(tableName = CompraDAO.TABLA)
internal data class CompraDAO(
        @DatabaseField(columnName = COLUMNA_ID, id = true)
        val id: String = "",

        @DatabaseField(columnName = COLUMNA_ID_USUARIO, foreign = true, columnDefinition = "VARCHAR NOT NULL REFERENCES ${UsuarioDAO.TABLA}(${UsuarioDAO.COLUMNA_ID})")
        val usuarioDAO: UsuarioDAO = UsuarioDAO(),

        @DatabaseField(columnName = COLUMNA_FECHA_REALIZACION, canBeNull = false, persisterClass = ZonedDateTimeThreeTenType::class)
        val fechaDeRealizacion: ZonedDateTime = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO),

        @DatabaseField(columnName = COLUMNA_CREACION_TERMINADA, canBeNull = false)
        val creacionTerminada: Boolean = false
                             )
{
    companion object
    {
        const val TABLA = "compra"
        const val COLUMNA_ID = "id_compra"
        const val COLUMNA_CREACION_TERMINADA = ColumnasTransaccionales.COLUMNA_CREACION_TERMINADA
        const val COLUMNA_FECHA_REALIZACION = "fecha_realizacion"
        const val COLUMNA_ID_USUARIO = "fk_${TABLA}_${UsuarioDAO.TABLA}"
    }

    constructor(entidadDeNegocio: Compra, creando: Boolean) :
            this(
                    entidadDeNegocio.id,
                    UsuarioDAO(usuario = entidadDeNegocio.nombreUsuario),
                    entidadDeNegocio.fechaDeRealizacion,
                    if (creando) false else entidadDeNegocio.creacionTerminada
                )

    fun aEntidadDeNegocio(
            idCliente: Long,
            creditosDAOAgrupados: List<EntidadRelacionUnoAMuchos<CreditoPaqueteDAO?, CreditoFondoDAO>>,
            pagos: List<PagoDAO>): Compra
    {
        val partesId = EntidadTransaccional.idAPartes(id)
        val creditosPorPaqueteAgrupados = creditosDAOAgrupados.map { it.entidadOrigen to it.entidadDestino }.toMap()
        return Compra(
                idCliente,
                usuarioDAO.usuario,
                partesId.uuid,
                partesId.tiempoCreacion,
                creacionTerminada,
                creditosPorPaqueteAgrupados[null]?.map { it.aEntidadDeNegocio(idCliente) } ?: listOf(),
                creditosPorPaqueteAgrupados.filterKeys { it != null }.map { it.key!!.aEntidadDeNegocio(idCliente, it.value) },
                pagos.map { it.aEntidadDeNegocio(idCliente) },
                fechaDeRealizacion
                     )
    }
}