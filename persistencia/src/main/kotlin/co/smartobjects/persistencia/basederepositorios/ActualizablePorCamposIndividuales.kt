package co.smartobjects.persistencia.basederepositorios

import co.smartobjects.campos.CampoModificableEntidad
import co.smartobjects.persistencia.CampoModificableEntidadDao
import co.smartobjects.persistencia.ConfiguracionRepositorios
import co.smartobjects.persistencia.Transformador
import co.smartobjects.persistencia.excepciones.EntidadNoExiste
import co.smartobjects.persistencia.excepciones.ErrorDAO
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.stmt.UpdateBuilder

typealias CamposDeEntidad<Entidad> = Map<String, CampoModificableEntidad<Entidad, *>>
internal typealias CamposDeEntidadDAO<EntidadDAO> = Map<String, CampoModificableEntidadDao<EntidadDAO, *>>

interface ActualizablePorCamposIndividuales<in Entidad, in TipoId>
{
    val nombreEntidad: String

    @Throws(ErrorDAO::class)
    fun actualizarCamposIndividuales(idCliente: Long, id: TipoId, camposAActualizar: CamposDeEntidad<Entidad>)
}

internal interface ActualizablePorCamposIndividualesFiltrable<in Entidad, in TipoId>
    : ActualizablePorCamposIndividuales<Entidad, TipoId>
{
    fun conFiltrosIgualdad(filtrosIgualdad: List<FiltroIgualdad<*>>): ActualizablePorCamposIndividualesFiltrable<Entidad, TipoId>

    fun <EntidadEnMetodo, TipoIdEnMetodo> darUpdateBuilderParaActualizacion(
            dao: Dao<EntidadEnMetodo, TipoIdEnMetodo>,
            id: TipoIdEnMetodo,
            camposAActualizar: CamposDeEntidad<EntidadEnMetodo>,
            filtrosIgualdad: List<FiltroIgualdad<*>>)
            : UpdateBuilder<EntidadEnMetodo, TipoIdEnMetodo>
    {
        val updateBuilder = dao.updateBuilder().apply {
            val filtro = where().idEq(id)
            if (filtrosIgualdad.isNotEmpty())
            {
                filtrosIgualdad.forEach {
                    if (it.valorColumnaUsoExterno == null)
                    {
                        filtro.isNull(it.campo.nombreColumna)
                    }
                    else
                    {
                        filtro.eq(it.campo.nombreColumna, it.valorColumnaUsoExterno)
                    }
                }
                filtro.and(filtrosIgualdad.size + 1)
            }
        }

        camposAActualizar.forEach {
            updateBuilder.updateColumnValue(it.key, it.value.valor)
        }

        return updateBuilder
    }
}

internal class ActualizablePorCamposIndividualesDaoConFiltrosIgualdad<in EntidadDao, in TipoId>(
        override val nombreEntidad: String,
        private val parametros: ParametrosParaDAOEntidadDeCliente<EntidadDao, TipoId>,
        private val filtrosIgualdad: List<FiltroIgualdad<*>>)
    : ActualizablePorCamposIndividualesFiltrable<EntidadDao, TipoId>
{
    override fun actualizarCamposIndividuales(idCliente: Long, id: TipoId, camposAActualizar: CamposDeEntidad<EntidadDao>)
    {
        if (camposAActualizar.isNotEmpty())
        {
            val updateBuilder = darUpdateBuilderParaActualizacion(parametros[idCliente].dao, id, camposAActualizar, filtrosIgualdad)
            val numeroFilasActualizadas = updateBuilder.update()
            if (numeroFilasActualizadas != 1)
            {
                throw EntidadNoExiste(id.toString(), nombreEntidad)
            }
        }
    }

    override fun conFiltrosIgualdad(filtrosIgualdad: List<FiltroIgualdad<*>>): ActualizablePorCamposIndividualesFiltrable<EntidadDao, TipoId>
    {
        return ActualizablePorCamposIndividualesDaoConFiltrosIgualdad(nombreEntidad, parametros, this.filtrosIgualdad + filtrosIgualdad)
    }
}

internal class ActualizablePorCamposIndividualesDao<in EntidadDao, in TipoId>(
        override val nombreEntidad: String,
        private val parametros: ParametrosParaDAOEntidadDeCliente<EntidadDao, TipoId>)
    : ActualizablePorCamposIndividualesFiltrable<EntidadDao, TipoId>
{
    @Throws(ErrorDAO::class)
    override fun actualizarCamposIndividuales(idCliente: Long, id: TipoId, camposAActualizar: CamposDeEntidad<EntidadDao>)
    {
        if (camposAActualizar.isNotEmpty())
        {
            val updateBuilder = darUpdateBuilderParaActualizacion(parametros[idCliente].dao, id, camposAActualizar, listOf())
            val numeroFilasActualizadas = updateBuilder.update()
            if (numeroFilasActualizadas != 1)
            {
                throw EntidadNoExiste(id.toString(), nombreEntidad)
            }
        }
    }

    override fun conFiltrosIgualdad(filtrosIgualdad: List<FiltroIgualdad<*>>): ActualizablePorCamposIndividualesFiltrable<EntidadDao, TipoId>
    {
        return ActualizablePorCamposIndividualesDaoConFiltrosIgualdad(nombreEntidad, parametros, filtrosIgualdad)
    }
}

internal class ActualizablePorCamposIndividualesSimple<in EntidadNegocio, in EntidadDao, in TipoIdNegocio, in TipoIdDao>(
        private val actualizadorDao: ActualizablePorCamposIndividuales<EntidadDao, TipoIdDao>,
        private val transformadorId: Transformador<TipoIdNegocio, TipoIdDao>,
        private val transformadorCampos: Transformador<CamposDeEntidad<EntidadNegocio>, CamposDeEntidadDAO<EntidadDao>>)
    : ActualizablePorCamposIndividuales<EntidadNegocio, TipoIdNegocio>
{
    override val nombreEntidad: String = actualizadorDao.nombreEntidad

    override fun actualizarCamposIndividuales(idCliente: Long, id: TipoIdNegocio, camposAActualizar: CamposDeEntidad<EntidadNegocio>)
    {
        actualizadorDao.actualizarCamposIndividuales(idCliente, transformadorId.transformar(id), transformadorCampos.transformar(camposAActualizar))
    }
}

internal class ActualizablePorCamposIndividualesEnTransaccionSQL<in EntidadNegocio, in TipoIdNegocio>(
        private val configuracion: ConfiguracionRepositorios,
        private val actualizableSinTransaccion: ActualizablePorCamposIndividuales<EntidadNegocio, TipoIdNegocio>)
    : ActualizablePorCamposIndividuales<EntidadNegocio, TipoIdNegocio>
{
    override val nombreEntidad: String = actualizableSinTransaccion.nombreEntidad

    override fun actualizarCamposIndividuales(idCliente: Long, id: TipoIdNegocio, camposAActualizar: CamposDeEntidad<EntidadNegocio>)
    {
        return transaccionEnEsquemaClienteYProcesarErroresParaActualizacion(configuracion, idCliente, nombreEntidad) {
            actualizableSinTransaccion.actualizarCamposIndividuales(idCliente, id, camposAActualizar)
        }
    }
}