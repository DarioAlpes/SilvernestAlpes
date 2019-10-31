package co.smartobjects.persistencia.operativas.ordenes

import co.smartobjects.entidades.operativas.ordenes.Transaccion
import co.smartobjects.persistencia.EntidadDAO
import co.smartobjects.persistencia.fondos.FondoDAO
import co.smartobjects.persistencia.fondos.precios.gruposclientes.GrupoClientesDAO
import co.smartobjects.persistencia.persistoresormlite.ZonedDateTimeThreeTenType
import co.smartobjects.persistencia.ubicaciones.UbicacionDAO
import co.smartobjects.persistencia.usuarios.UsuarioDAO
import co.smartobjects.utilidades.Decimal
import com.j256.ormlite.field.DataType
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import org.threeten.bp.ZonedDateTime
import java.math.BigDecimal

@DatabaseTable(tableName = TransaccionDAO.TABLA)
internal data class TransaccionDAO
(
        @DatabaseField(columnName = COLUMNA_ID, generatedId = true, allowGeneratedIdInsert = true)
        val id: Long? = null,

        @DatabaseField(columnName = COLUMNA_ID_USUARIO, foreign = true, canBeNull = false, columnDefinition = "VARCHAR NOT NULL REFERENCES ${UsuarioDAO.TABLA}(${UsuarioDAO.COLUMNA_ID})")
        val usuarioDAO: UsuarioDAO = UsuarioDAO(),

        @DatabaseField(columnName = COLUMNA_ID_UBICACION, foreign = true, canBeNull = false, columnDefinition = "BIGINT REFERENCES ${UbicacionDAO.TABLA}(${UbicacionDAO.COLUMNA_ID})")
        val ubicacionDAO: UbicacionDAO? = null,

        @DatabaseField(columnName = COLUMNA_ID_FONDO, foreign = true, canBeNull = false, columnDefinition = "BIGINT NOT NULL REFERENCES ${FondoDAO.TABLA}(${FondoDAO.COLUMNA_ID})")
        val fondoDAO: FondoDAO = FondoDAO(),

        @DatabaseField(columnName = COLUMNA_CODIGO_EXTERNO_FONDO, canBeNull = false)
        val codigoExternoFondo: String = "",

        @DatabaseField(columnName = COLUMNA_CANTIDAD, canBeNull = false, dataType = DataType.BIG_DECIMAL_NUMERIC)
        val cantidad: BigDecimal = BigDecimal.ZERO,

        @DatabaseField(columnName = COLUMNA_ID_GRUPO_CLIENTES, foreign = true, columnDefinition = "BIGINT REFERENCES ${GrupoClientesDAO.TABLA}(${GrupoClientesDAO.COLUMNA_ID})")
        val grupoClientesCompraDAO: GrupoClientesDAO? = null,

        @DatabaseField(columnName = COLUMNA_ID_DISPOSITIVO, canBeNull = false)
        val idDispositivo: String = "",

        @DatabaseField(columnName = COLUMNA_VALIDO_DESDE, persisterClass = ZonedDateTimeThreeTenType::class)
        val validoDesde: ZonedDateTime? = null,

        @DatabaseField(columnName = COLUMNA_VALIDO_HASTA, persisterClass = ZonedDateTimeThreeTenType::class)
        val validoHasta: ZonedDateTime? = null,

        @DatabaseField(columnName = COLUMNA_ID_ORDEN, foreign = true, canBeNull = false, columnDefinition = "BIGINT NOT NULL REFERENCES ${OrdenDAO.TABLA}(${OrdenDAO.COLUMNA_ID}) ON DELETE CASCADE")
        val ordenDAO: OrdenDAO = OrdenDAO(),

        @DatabaseField(columnName = COLUMNA_TIPO, canBeNull = false)
        val tipo: Tipo = Tipo.DESCONOCIDO

) : EntidadDAO<Transaccion>
{
    companion object
    {
        const val TABLA = "transaccion"
        const val COLUMNA_ID = "id"

        const val COLUMNA_ID_USUARIO = "fk_${TABLA}_${UsuarioDAO.TABLA}"
        const val COLUMNA_ID_UBICACION = "fk_${TABLA}_${UbicacionDAO.TABLA}"
        const val COLUMNA_ID_FONDO = "fk_${TABLA}_${FondoDAO.TABLA}"
        const val COLUMNA_CODIGO_EXTERNO_FONDO = "codigo_externo_fondo"
        const val COLUMNA_CANTIDAD = "cantidad"
        const val COLUMNA_ID_GRUPO_CLIENTES = "fk_${TABLA}_${GrupoClientesDAO.TABLA}"
        const val COLUMNA_ID_DISPOSITIVO = "id_dispositivo"
        const val COLUMNA_VALIDO_DESDE = "valido_desde"
        const val COLUMNA_VALIDO_HASTA = "valido_hasta"
        const val COLUMNA_ID_ORDEN = "fk_${TABLA}_${OrdenDAO.TABLA}"
        const val COLUMNA_TIPO = "tipo"
    }

    constructor(entidadDeNegocio: Transaccion.Credito, idOrden: Long?, creando: Boolean) :
            this(
                    if (creando) null else entidadDeNegocio.id,
                    UsuarioDAO(usuario = entidadDeNegocio.nombreUsuario),
                    UbicacionDAO(id = entidadDeNegocio.idUbicacion),
                    FondoDAO(id = entidadDeNegocio.idFondo),
                    entidadDeNegocio.codigoExternoFondo,
                    entidadDeNegocio.cantidad.valor,
                    GrupoClientesDAO(id = entidadDeNegocio.idGrupoClientesPersona),
                    entidadDeNegocio.idDispositivo,
                    entidadDeNegocio.validoDesde,
                    entidadDeNegocio.validoHasta,
                    OrdenDAO(id = idOrden),
                    Tipo.CREDITO
                )

    constructor(entidadDeNegocio: Transaccion.Debito, idOrden: Long?, creando: Boolean) :
            this(
                    if (creando) null else entidadDeNegocio.id,
                    UsuarioDAO(usuario = entidadDeNegocio.nombreUsuario),
                    UbicacionDAO(id = entidadDeNegocio.idUbicacion),
                    FondoDAO(id = entidadDeNegocio.idFondo),
                    entidadDeNegocio.codigoExternoFondo,
                    entidadDeNegocio.cantidad.valor,
                    GrupoClientesDAO(id = entidadDeNegocio.idGrupoClientesPersona),
                    entidadDeNegocio.idDispositivo,
                    null,
                    null,
                    OrdenDAO(id = idOrden),
                    Tipo.DEBITO
                )

    override fun aEntidadDeNegocio(idCliente: Long): Transaccion
    {
        return when (tipo)
        {
            TransaccionDAO.Tipo.CREDITO     ->
            {
                Transaccion.Credito(
                        idCliente,
                        id,
                        usuarioDAO.usuario,
                        ubicacionDAO!!.id,
                        fondoDAO.id!!,
                        codigoExternoFondo,
                        Decimal(cantidad),
                        grupoClientesCompraDAO?.id,
                        idDispositivo,
                        validoDesde,
                        validoHasta
                                   )
            }
            TransaccionDAO.Tipo.DEBITO      ->
            {
                Transaccion.Debito(
                        idCliente,
                        id,
                        usuarioDAO.usuario,
                        ubicacionDAO!!.id!!,
                        fondoDAO.id!!,
                        codigoExternoFondo,
                        Decimal(cantidad),
                        grupoClientesCompraDAO?.id,
                        idDispositivo
                                  )
            }
            TransaccionDAO.Tipo.DESCONOCIDO ->
            {
                throw IllegalStateException("Base de datos en estado inconsistente")
            }
        }
    }

    internal enum class Tipo
    {
        CREDITO, DEBITO, DESCONOCIDO;
    }
}