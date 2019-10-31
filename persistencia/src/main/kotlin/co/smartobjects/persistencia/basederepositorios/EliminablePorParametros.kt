package co.smartobjects.persistencia.basederepositorios

import co.smartobjects.persistencia.ConfiguracionRepositorios
import co.smartobjects.persistencia.EntidadDAO
import co.smartobjects.persistencia.excepciones.ErrorDAO


@Suppress("unused")
interface EliminablePorParametros<out Entidad, in Parametros : ParametrosConsulta>
{
    val nombreEntidad: String

    @Throws(ErrorDAO::class)
    fun eliminarSegunFiltros(idCliente: Long, parametros: Parametros): Boolean
}

internal class EliminablePorParametrosDao<out EntidadDao, in TipoIdDao, in Parametros : ParametrosConsulta>(
        override val nombreEntidad: String,
        private val parametrosDaoDeCliente: ParametrosParaDAOEntidadDeCliente<EntidadDao, TipoIdDao>)
    : EliminablePorParametros<EntidadDao, Parametros>
{
    override fun eliminarSegunFiltros(idCliente: Long, parametros: Parametros): Boolean
    {
        val parametrosDao = parametrosDaoDeCliente[idCliente]
        val deleteBuilder = parametrosDao.dao.deleteBuilder()

        if (parametros.filtrosSQL.isNotEmpty())
        {
            val clausulaWhere = parametros.filtrosSQL.reduce { acc, filtroSiguiente -> acc.and(filtroSiguiente) }

            val sql = clausulaWhere.generarSQL()
            val parametrosDeConsulta = clausulaWhere.parametros.toList()

            deleteBuilder.where()
                .raw(
                        sql.toString(),
                        *(parametrosDeConsulta.toTypedArray())
                    )
        }

        val numeroEntidadesAEliminar =
                ConstructorQueryORMLite(idCliente, parametrosDaoDeCliente)
                    .conFiltrosSQL(parametros.filtrosSQL)
                    .contar()

        val filasEliminadas = deleteBuilder.delete().toLong()

        return numeroEntidadesAEliminar == filasEliminadas && numeroEntidadesAEliminar != 0L
    }
}

internal class EliminablePorParametrosSimple<out EntidadNegocio, out EntidadDao : EntidadDAO<EntidadNegocio>, in TipoIdDao, in Parametros : ParametrosConsulta>
(
        private val eliminador: EliminablePorParametrosDao<EntidadDao, TipoIdDao, Parametros>
) : EliminablePorParametros<EntidadNegocio, Parametros>
{
    override val nombreEntidad: String = eliminador.nombreEntidad

    override fun eliminarSegunFiltros(idCliente: Long, parametros: Parametros): Boolean
    {
        return eliminador.eliminarSegunFiltros(idCliente, parametros)
    }
}


internal class EliminablePorParametrosEnTransaccionSQL<out Entidad, in Parametros : ParametrosConsulta>(
        private val configuracion: ConfiguracionRepositorios,
        private val eliminableSinTransaccion: EliminablePorParametros<Entidad, Parametros>)
    : EliminablePorParametros<Entidad, Parametros>
{
    override val nombreEntidad: String = eliminableSinTransaccion.nombreEntidad

    override fun eliminarSegunFiltros(idCliente: Long, parametros: Parametros): Boolean
    {
        return transaccionEnEsquemaClienteYProcesarErroresParaEliminacion(configuracion, idCliente, nombreEntidad) {
            eliminableSinTransaccion.eliminarSegunFiltros(idCliente, parametros)
        }
    }
}