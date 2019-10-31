package co.smartobjects.persistencia.operativas.reservas

import co.smartobjects.entidades.operativas.EntidadTransaccional
import co.smartobjects.entidades.operativas.reservas.Reserva
import co.smartobjects.persistencia.EntidadRelacionUnoAMuchos
import co.smartobjects.persistencia.operativas.ColumnasTransaccionales
import co.smartobjects.persistencia.usuarios.UsuarioDAO
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable(tableName = ReservaDAO.TABLA)
internal data class ReservaDAO(
        @DatabaseField(columnName = COLUMNA_ID, id = true)
        val id: String = "",

        @DatabaseField(columnName = COLUMNA_ID_USUARIO, foreign = true, canBeNull = false, columnDefinition = "VARCHAR NOT NULL REFERENCES ${UsuarioDAO.TABLA}(${UsuarioDAO.COLUMNA_ID})")
        val usuarioDAO: UsuarioDAO = UsuarioDAO(),

        @DatabaseField(columnName = COLUMNA_CREACION_TERMINADA, canBeNull = false)
        val creacionTerminada: Boolean = false,

        @DatabaseField(columnName = COLUMNA_NUMERO_DE_RESERVA)
        val numeroDeReserva: Long? = null
                              )
{
    companion object
    {
        const val TABLA = "reserva"
        const val COLUMNA_ID = "id_reserva"
        const val COLUMNA_ID_USUARIO = "fk_${TABLA}_${UsuarioDAO.TABLA}"
        const val COLUMNA_CREACION_TERMINADA = ColumnasTransaccionales.COLUMNA_CREACION_TERMINADA
        const val COLUMNA_NUMERO_DE_RESERVA = "numero_de_reserva"
    }

    constructor(entidadDeNegocio: Reserva, creando: Boolean) :
            this(
                    entidadDeNegocio.id,
                    UsuarioDAO(usuario = entidadDeNegocio.nombreUsuario),
                    if (creando) false else entidadDeNegocio.creacionTerminada,
                    null
                )

    fun aEntidadDeNegocio(idCliente: Long, sesionesDeManilla: List<EntidadRelacionUnoAMuchos<SesionDeManillaDAO, CreditoEnSesionDeManillaDAO>>): Reserva
    {
        val partesId = EntidadTransaccional.idAPartes(id)
        return Reserva(
                idCliente,
                usuarioDAO.usuario,
                partesId.uuid,
                partesId.tiempoCreacion,
                creacionTerminada,
                numeroDeReserva,
                sesionesDeManilla.map {
                    it.entidadOrigen.aEntidadDeNegocio(idCliente, it.entidadDestino)
                }
                      )
    }
}