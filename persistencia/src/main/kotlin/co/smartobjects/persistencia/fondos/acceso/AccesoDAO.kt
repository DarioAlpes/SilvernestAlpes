package co.smartobjects.persistencia.fondos.acceso

import co.smartobjects.entidades.fondos.Acceso
import co.smartobjects.entidades.fondos.AccesoBase
import co.smartobjects.entidades.fondos.Entrada
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.persistencia.fondos.EntidadFondoDAO
import co.smartobjects.persistencia.fondos.FondoDAO
import co.smartobjects.persistencia.ubicaciones.UbicacionDAO
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable


@DatabaseTable(tableName = AccesoDAO.TABLA)
internal data class AccesoDAO(
        @DatabaseField(columnName = COLUMNA_ID_DUMMY, id = true)
        val id: Long? = null,

        @DatabaseField(columnName = COLUMNA_ID, foreign = true, uniqueIndex = true, columnDefinition = "BIGINT NOT NULL REFERENCES ${FondoDAO.TABLA}(${FondoDAO.COLUMNA_ID}) ON DELETE CASCADE")
        override val fondoDAO: FondoDAO = FondoDAO(),

        @DatabaseField(columnName = COLUMNA_ID_UBICACION, foreign = true, index = true, columnDefinition = "BIGINT NOT NULL REFERENCES ${UbicacionDAO.TABLA}(${UbicacionDAO.COLUMNA_ID})")
        val ubicacionDAO: UbicacionDAO = UbicacionDAO()
                             ) : EntidadFondoDAO<AccesoBase<*>>
{
    companion object
    {
        const val TABLA = "acceso"
        const val COLUMNA_ID_DUMMY = "id_dummy_acceso"
        const val COLUMNA_ID = "id_acceso"
        const val COLUMNA_ID_UBICACION = "fk_${TABLA}_${UbicacionDAO.TABLA}"
    }

    constructor(entidadDeNegocio: Acceso) :
            this(
                    entidadDeNegocio.id,
                    FondoDAO(entidadDeNegocio),
                    UbicacionDAO(entidadDeNegocio.idUbicacion)
                )

    constructor(entidadDeNegocio: Entrada) :
            this(
                    entidadDeNegocio.id,
                    FondoDAO(entidadDeNegocio),
                    UbicacionDAO(entidadDeNegocio.idUbicacion)
                )

    override fun aEntidadDeNegocio(idCliente: Long, fondoDAO: FondoDAO): AccesoBase<*>
    {
        when (fondoDAO.tipoDeFondo)
        {
            FondoDAO.TipoDeFondoEnBD.ACCESO  ->
                return Acceso(
                        idCliente,
                        fondoDAO.id,
                        fondoDAO.nombre,
                        fondoDAO.disponibleParaLaVenta,
                        fondoDAO.debeAparecerSoloUnaVez,
                        fondoDAO.esIlimitado,
                        Precio(fondoDAO.precio, fondoDAO.impuestoPorDefectoDAO.id!!),
                        fondoDAO.codigoExterno,
                        ubicacionDAO.id!!
                             )
            FondoDAO.TipoDeFondoEnBD.ENTRADA ->
                return Entrada(
                        idCliente,
                        fondoDAO.id,
                        fondoDAO.nombre,
                        fondoDAO.disponibleParaLaVenta,
                        fondoDAO.esIlimitado,
                        Precio(fondoDAO.precio, fondoDAO.impuestoPorDefectoDAO.id!!),
                        fondoDAO.codigoExterno,
                        ubicacionDAO.id!!
                              )
            else                             ->
                throw RuntimeException("El tipo de fondo ${fondoDAO.tipoDeFondo} no se puede convertir a entidad de negocio desde acceso")
        }
    }
}