package co.smartobjects.persistencia.operativas.compras

import co.smartobjects.entidades.operativas.compras.CreditoFondo
import co.smartobjects.persistencia.EntidadDAO
import co.smartobjects.persistencia.fondos.FondoDAO
import co.smartobjects.persistencia.fondos.precios.gruposclientes.GrupoClientesDAO
import co.smartobjects.persistencia.fondos.precios.impuestos.ImpuestoDAO
import co.smartobjects.persistencia.persistoresormlite.ZonedDateTimeThreeTenType
import co.smartobjects.persistencia.personas.PersonaDAO
import co.smartobjects.persistencia.ubicaciones.UbicacionDAO
import co.smartobjects.persistencia.usuarios.UsuarioDAO
import co.smartobjects.utilidades.Decimal
import com.j256.ormlite.field.DataType
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import org.threeten.bp.ZonedDateTime
import java.math.BigDecimal

@DatabaseTable(tableName = CreditoFondoDAO.TABLA)
internal data class CreditoFondoDAO(
        @DatabaseField(columnName = COLUMNA_ID, generatedId = true, allowGeneratedIdInsert = true)
        val id: Long? = null,

        @DatabaseField(columnName = COLUMNA_CANTIDAD, canBeNull = false, dataType = DataType.BIG_DECIMAL_NUMERIC)
        val cantidad: BigDecimal = BigDecimal.ZERO,

        @DatabaseField(columnName = COLUMNA_VALOR_PAGADO, canBeNull = false, dataType = DataType.BIG_DECIMAL_NUMERIC)
        val valorPagado: BigDecimal = BigDecimal.ZERO,

        @DatabaseField(columnName = COLUMNA_VALOR_IMPUESTO_PAGADO, canBeNull = false, dataType = DataType.BIG_DECIMAL_NUMERIC)
        val valorImpuestoPagado: BigDecimal = BigDecimal.ZERO,

        @DatabaseField(columnName = COLUMNA_VALIDO_DESDE, persisterClass = ZonedDateTimeThreeTenType::class)
        val validoDesde: ZonedDateTime? = null,

        @DatabaseField(columnName = COLUMNA_VALIDO_HASTA, persisterClass = ZonedDateTimeThreeTenType::class)
        val validoHasta: ZonedDateTime? = null,

        @DatabaseField(columnName = COLUMNA_ID_DISPOSITIVO, canBeNull = false)
        val idDispositivo: String = "",

        @DatabaseField(columnName = COLUMNA_ORIGEN, canBeNull = false)
        val origen: String = "",

        @DatabaseField(columnName = COLUMNA_CONSUMIDO, canBeNull = false)
        val consumido: Boolean = false,

        @DatabaseField(columnName = COLUMNA_ID_COMPRA, foreign = true, columnDefinition = "VARCHAR NOT NULL REFERENCES ${CompraDAO.TABLA}(${CompraDAO.COLUMNA_ID}) ON DELETE CASCADE")
        val compraDAO: CompraDAO = CompraDAO(),

        @DatabaseField(columnName = COLUMNA_ID_USUARIO, foreign = true, columnDefinition = "VARCHAR NOT NULL REFERENCES ${UsuarioDAO.TABLA}(${UsuarioDAO.COLUMNA_ID})")
        val usuarioDAO: UsuarioDAO = UsuarioDAO(),

        @DatabaseField(columnName = COLUMNA_ID_CREDITO_PAQUETE, foreign = true, columnDefinition = "BIGINT REFERENCES ${CreditoPaqueteDAO.TABLA}(${CreditoPaqueteDAO.COLUMNA_ID}) ON DELETE CASCADE")
        val creditoPaqueteDAO: CreditoPaqueteDAO? = null,

        @DatabaseField(columnName = COLUMNA_ID_FONDO, foreign = true, columnDefinition = "BIGINT NOT NULL REFERENCES ${FondoDAO.TABLA}(${FondoDAO.COLUMNA_ID})")
        val fondoCompradoDAO: FondoDAO = FondoDAO(),

        @DatabaseField(columnName = COLUMNA_CODIGO_EXTERNO_FONDO, canBeNull = false)
        val codigoExternoFondo: String = "",

        @DatabaseField(columnName = COLUMNA_ID_IMPUESTO, foreign = true, columnDefinition = "BIGINT NOT NULL REFERENCES ${ImpuestoDAO.TABLA}(${ImpuestoDAO.COLUMNA_ID})")
        val impuestoPagadoDAO: ImpuestoDAO = ImpuestoDAO(),

        @DatabaseField(columnName = COLUMNA_ID_PERSONA, foreign = true, columnDefinition = "BIGINT NOT NULL REFERENCES ${PersonaDAO.TABLA}(${PersonaDAO.COLUMNA_ID})")
        val personaDueñaDAO: PersonaDAO = PersonaDAO(),

        @DatabaseField(columnName = COLUMNA_ID_UBICACION, foreign = true, columnDefinition = "BIGINT REFERENCES ${UbicacionDAO.TABLA}(${UbicacionDAO.COLUMNA_ID})")
        val ubicacionCompraDAO: UbicacionDAO? = null,

        @DatabaseField(columnName = COLUMNA_ID_GRUPO_CLIENTES, foreign = true, columnDefinition = "BIGINT REFERENCES ${GrupoClientesDAO.TABLA}(${GrupoClientesDAO.COLUMNA_ID})")
        val grupoClientesCompraDAO: GrupoClientesDAO? = null
                                   ) : EntidadDAO<CreditoFondo>
{
    companion object
    {
        const val TABLA = "credito_fondo"
        const val COLUMNA_ID = "id_credito_fondo"
        const val COLUMNA_CANTIDAD = "cantidad"
        const val COLUMNA_VALOR_PAGADO = "valor_pagado"
        const val COLUMNA_VALOR_IMPUESTO_PAGADO = "valor_impuesto_pagado"
        const val COLUMNA_VALIDO_DESDE = "valido_desde"
        const val COLUMNA_VALIDO_HASTA = "valido_hasta"
        const val COLUMNA_ID_DISPOSITIVO = "id_dispositivo"
        const val COLUMNA_ORIGEN = "origen"
        const val COLUMNA_CONSUMIDO = "consumido"
        const val COLUMNA_CODIGO_EXTERNO_FONDO = "codigo_externo_fondo"

        const val COLUMNA_ID_COMPRA = "fk_${TABLA}_${CompraDAO.TABLA}"
        const val COLUMNA_ID_USUARIO = "fk_${TABLA}_${UsuarioDAO.TABLA}"
        const val COLUMNA_ID_CREDITO_PAQUETE = "fk_${TABLA}_${CreditoPaqueteDAO.TABLA}"
        const val COLUMNA_ID_FONDO = "fk_${TABLA}_${FondoDAO.TABLA}"
        const val COLUMNA_ID_IMPUESTO = "fk_${TABLA}_${ImpuestoDAO.TABLA}"
        const val COLUMNA_ID_PERSONA = "fk_${TABLA}_${PersonaDAO.TABLA}"
        const val COLUMNA_ID_UBICACION = "fk_${TABLA}_${UbicacionDAO.TABLA}"
        const val COLUMNA_ID_GRUPO_CLIENTES = "fk_${TABLA}_${GrupoClientesDAO.TABLA}"
    }

    constructor(entidadDeNegocio: CreditoFondo, idCompra: String, creando: Boolean) :
            this(
                    if (creando) null else entidadDeNegocio.id,
                    entidadDeNegocio.cantidad.valor,
                    entidadDeNegocio.valorPagado.valor,
                    entidadDeNegocio.valorImpuestoPagado.valor,
                    entidadDeNegocio.validoDesde,
                    entidadDeNegocio.validoHasta,
                    entidadDeNegocio.idDispositivo,
                    entidadDeNegocio.origen,
                    if (creando) false else entidadDeNegocio.consumido,
                    CompraDAO(id = idCompra),
                    UsuarioDAO(usuario = entidadDeNegocio.nombreUsuario),
                    CreditoPaqueteDAO(),
                    FondoDAO(id = entidadDeNegocio.idFondoComprado),
                    entidadDeNegocio.codigoExternoFondo,
                    ImpuestoDAO(id = entidadDeNegocio.idImpuestoPagado),
                    PersonaDAO(id = entidadDeNegocio.idPersonaDueña),
                    UbicacionDAO(id = entidadDeNegocio.idUbicacionCompra),
                    GrupoClientesDAO(id = entidadDeNegocio.idGrupoClientesPersona)
                )

    override fun aEntidadDeNegocio(idCliente: Long): CreditoFondo
    {
        return CreditoFondo(
                idCliente,
                id,
                Decimal(cantidad),
                Decimal(valorPagado),
                Decimal(valorImpuestoPagado),
                validoDesde,
                validoHasta,
                consumido,
                origen,
                usuarioDAO.usuario,
                personaDueñaDAO.id!!,
                fondoCompradoDAO.id!!,
                codigoExternoFondo,
                impuestoPagadoDAO.id!!,
                idDispositivo,
                ubicacionCompraDAO?.id,
                grupoClientesCompraDAO?.id
                           )
    }
}