package co.smartobjects.persistencia.operativas.ordenes

import co.smartobjects.entidades.operativas.EntidadTransaccional
import co.smartobjects.entidades.operativas.ordenes.LoteDeOrdenes
import co.smartobjects.entidades.operativas.ordenes.Orden
import co.smartobjects.persistencia.operativas.ColumnasTransaccionales
import co.smartobjects.persistencia.operativas.reservas.SesionDeManillaDAO
import co.smartobjects.persistencia.persistoresormlite.ZonedDateTimeThreeTenType
import co.smartobjects.persistencia.usuarios.UsuarioDAO
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import org.threeten.bp.ZonedDateTime

@DatabaseTable(tableName = OrdenDAO.TABLA)
internal data class OrdenDAO
(
        @DatabaseField(columnName = COLUMNA_ID, generatedId = true, allowGeneratedIdInsert = true)
        val id: Long? = null,

        @DatabaseField(columnName = COLUMNA_ID_TRANSACCION, canBeNull = false)
        val idTransaccion: String = "",

        @DatabaseField(columnName = COLUMNA_ID_USUARIO, foreign = true, canBeNull = false, columnDefinition = "VARCHAR NOT NULL REFERENCES ${UsuarioDAO.TABLA}(${UsuarioDAO.COLUMNA_ID})")
        val usuarioDAO: UsuarioDAO = UsuarioDAO(),

        @DatabaseField(columnName = COLUMNA_CREACION_TERMINADA, canBeNull = false)
        val creacionTerminada: Boolean = false,

        @DatabaseField(columnName = COLUMNA_ID_SESION_DE_MANILLA, foreign = true, canBeNull = false, columnDefinition = "BIGINT NOT NULL REFERENCES ${SesionDeManillaDAO.TABLA}(${SesionDeManillaDAO.COLUMNA_ID})")
        val sesionDeManillaDAO: SesionDeManillaDAO = SesionDeManillaDAO(),

        @DatabaseField(columnName = COLUMNA_FECHA_REALIZACION, canBeNull = false, persisterClass = ZonedDateTimeThreeTenType::class)
        val fechaDeRealizacion: ZonedDateTime = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
)
{
    companion object
    {
        const val TABLA = "orden"
        const val COLUMNA_ID = "id_orden"
        const val COLUMNA_ID_TRANSACCION = "id_transaccion"
        const val COLUMNA_FECHA_REALIZACION = "fecha_realizacion_transaccion"
        const val COLUMNA_CREACION_TERMINADA = ColumnasTransaccionales.COLUMNA_CREACION_TERMINADA
        const val COLUMNA_ID_USUARIO = "fk_${TABLA}_${UsuarioDAO.TABLA}"
        const val COLUMNA_ID_SESION_DE_MANILLA = "fk_${TABLA}_${SesionDeManillaDAO.TABLA}"
    }

    constructor(loteDeOrdenes: EntidadTransaccional<LoteDeOrdenes>, entidadDeNegocio: Orden, creando: Boolean) :
            this(
                    entidadDeNegocio.id,
                    loteDeOrdenes.id,
                    UsuarioDAO(usuario = loteDeOrdenes.nombreUsuario),
                    if (creando) false else loteDeOrdenes.creacionTerminada,
                    SesionDeManillaDAO(id = entidadDeNegocio.idSesionDeManilla),
                    entidadDeNegocio.fechaDeRealizacion
                )


    fun aEntidadDeNegocio(idCliente: Long, transaccionesDAO: List<TransaccionDAO>): Orden
    {
        return Orden(
                idCliente,
                id,
                sesionDeManillaDAO.id!!,
                transaccionesDAO.map {
                    it.aEntidadDeNegocio(idCliente)
                },
                fechaDeRealizacion
                    )
    }
}