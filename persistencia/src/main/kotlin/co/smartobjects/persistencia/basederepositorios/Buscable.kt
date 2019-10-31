package co.smartobjects.persistencia.basederepositorios

import co.smartobjects.persistencia.CampoTabla
import co.smartobjects.persistencia.EntidadDAO
import co.smartobjects.persistencia.Transformador
import co.smartobjects.persistencia.excepciones.ErrorDAO
import com.j256.ormlite.field.SqlType


interface Buscable<out EntidadDeNegocio, in TipoIdNegocio>
{
    @Throws(ErrorDAO::class)
    fun buscarPorId(idCliente: Long, id: TipoIdNegocio): EntidadDeNegocio?
}

internal class BuscableDao<out EntidadDao, in TipoIdDao>(
        private val parametros: ParametrosParaDAOEntidadDeCliente<EntidadDao, TipoIdDao>)
    : Buscable<EntidadDao, TipoIdDao>
{
    @Throws(ErrorDAO::class)
    override fun buscarPorId(idCliente: Long, id: TipoIdDao): EntidadDao?
    {
        return parametros[idCliente].dao.queryForId(id)
    }
}

internal class BuscableSimple<out EntidadDeNegocio, EntidadDaoConvertible : EntidadDAO<EntidadDeNegocio>, in TipoIdNegocio, TipoIdDao>(
        private val transformadorId: Transformador<TipoIdNegocio, TipoIdDao>,
        private val buscador: BuscableDao<EntidadDaoConvertible, TipoIdDao>)
    : Buscable<EntidadDeNegocio, TipoIdNegocio>
{
    @Throws(ErrorDAO::class)
    override fun buscarPorId(idCliente: Long, id: TipoIdNegocio): EntidadDeNegocio?
    {
        return buscador.buscarPorId(idCliente, transformadorId.transformar(id))?.aEntidadDeNegocio(idCliente)
    }
}

internal class BuscableSegunListableFiltrable<out EntidadDeNegocio, in TipoIdNegocio>(
        private val listadorEntidad: ListableFiltrableOrdenable<EntidadDeNegocio>,
        private val campoId: CampoTabla,
        private val tipoCampoId: SqlType)
    : Buscable<EntidadDeNegocio, TipoIdNegocio>
{
    override fun buscarPorId(idCliente: Long, id: TipoIdNegocio): EntidadDeNegocio?
    {
        // Se usa toList para forzar a que consuma la lista en caso de transacciones
        return listadorEntidad.conFiltrosSQL(listOf(FiltroIgualdad(campoId, id, tipoCampoId))).listar(idCliente).toList().firstOrNull()
    }
}