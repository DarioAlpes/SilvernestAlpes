package co.smartobjects.persistencia.operativas

import co.smartobjects.entidades.operativas.EntidadTransaccional
import co.smartobjects.persistencia.CampoTabla
import co.smartobjects.persistencia.Transformador
import co.smartobjects.persistencia.basederepositorios.*
import com.j256.ormlite.field.SqlType

internal object ColumnasTransaccionales
{
    const val COLUMNA_CREACION_TERMINADA = "creacion_terminada"
}

internal class RepositorioTransaccional<Entidad : EntidadTransaccional<Entidad>, out EntidadDao> private constructor(
        override val nombreEntidad: String,
        private val creador: Creable<Entidad>,
        private val eliminador: EliminablePorId<Entidad, String>)
    : Creable<Entidad> by creador,
      EliminablePorId<Entidad, String> by eliminador
{
    constructor(
            tabla: String,
            nombreEntidad: String,
            parametrosDao: ParametrosParaDAOEntidadDeCliente<EntidadDao, String>,
            creadorSinTransaccion: Creable<Entidad>,
            eliminadorSinTransaccionParaCreacion: EliminablePorIdFiltrable<Entidad, String>,
            eliminadorSinTransaccionParaEliminacion: EliminablePorIdFiltrable<Entidad, String> = eliminadorSinTransaccionParaCreacion
               ) :
            this(
                    nombreEntidad,
                    CreableEnTransaccionSQL<Entidad>
                    (
                            parametrosDao.configuracion,
                            CreableEliminandoEntidad
                            (
                                    creadorSinTransaccion,
                                    eliminadorSinTransaccionParaCreacion.conFiltrosSQL(
                                            sequenceOf(FiltroIgualdad(CampoTabla(tabla, ColumnasTransaccionales.COLUMNA_CREACION_TERMINADA), false, SqlType.BOOLEAN))
                                                                                      ),
                                    object : Transformador<Entidad, String>
                                    {
                                        override fun transformar(origen: Entidad): String
                                        {
                                            return origen.id
                                        }
                                    }
                            )
                    ),
                    EliminablePorIdEnTransaccionSQL
                    (
                            parametrosDao.configuracion,
                            eliminadorSinTransaccionParaEliminacion
                    )
                )
}