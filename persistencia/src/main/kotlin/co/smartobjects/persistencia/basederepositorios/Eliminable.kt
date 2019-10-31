package co.smartobjects.persistencia.basederepositorios

import co.smartobjects.persistencia.ConfiguracionRepositorios
import co.smartobjects.persistencia.Transformador
import co.smartobjects.persistencia.excepciones.ErrorDAO
import co.smartobjects.persistencia.excepciones.ErrorEliminandoEntidad

@Suppress("unused")
internal interface EliminablePorIgualdad<out Entidad>
{
    val nombreEntidad: String

    @Throws(ErrorDAO::class)
    fun eliminarSegunFiltros(idCliente: Long, filtrosIgualdad: Sequence<FiltroIgualdad<*>>): Boolean
}

@Suppress("unused")
interface EliminablePorId<out Entidad, in TipoId>
{
    val nombreEntidad: String

    @Throws(ErrorDAO::class)
    fun eliminarPorId(idCliente: Long, id: TipoId): Boolean
}

@Suppress("unused")
internal interface EliminablePorIdFiltrable<out Entidad, in TipoId> : EliminablePorId<Entidad, TipoId>
{
    fun conFiltrosSQL(filtrosIgualdad: Sequence<FiltroIgualdad<*>>): EliminablePorIdFiltrable<Entidad, TipoId>
}

internal class EliminableDao<out EntidadDao, in TipoIdDao>(
        override val nombreEntidad: String,
        private val parametros: ParametrosParaDAOEntidadDeCliente<EntidadDao, TipoIdDao>)
    : EliminablePorIdFiltrable<EntidadDao, TipoIdDao>,
      EliminablePorIgualdad<EntidadDao>
{
    override fun eliminarSegunFiltros(idCliente: Long, filtrosIgualdad: Sequence<FiltroIgualdad<*>>): Boolean
    {
        val deleteBuilder = parametros[idCliente].dao.deleteBuilder()
        val queryBuilder = parametros[idCliente].dao.queryBuilder()
        val whereEliminacion = deleteBuilder.where()
        val whereQuery = queryBuilder.where()

        var numeroClausulas = 0
        for (filtroIgualdad in filtrosIgualdad)
        {
            whereEliminacion.eq(filtroIgualdad.campo.nombreColumna, filtroIgualdad.valorColumnaUsoExterno)
            whereQuery.eq(filtroIgualdad.campo.nombreColumna, filtroIgualdad.valorColumnaUsoExterno)
            numeroClausulas++
        }
        if (numeroClausulas > 1)
        {
            whereEliminacion.and(numeroClausulas)
            whereQuery.and(numeroClausulas)
        }

        val numeroEntidadesAEliminar = queryBuilder.countOf()
        val filasEliminadas = deleteBuilder.delete().toLong()

        return numeroEntidadesAEliminar == filasEliminadas && numeroEntidadesAEliminar != 0L
    }

    override fun conFiltrosSQL(filtrosIgualdad: Sequence<FiltroIgualdad<*>>): EliminablePorIdFiltrable<EntidadDao, TipoIdDao>
    {
        return EliminableDAOConFiltrosIgualdad(nombreEntidad, parametros, filtrosIgualdad)
    }

    override fun eliminarPorId(idCliente: Long, id: TipoIdDao): Boolean
    {
        return parametros[idCliente].dao.deleteById(id) == 1
    }
}

internal class EliminableDAOConFiltrosIgualdad<out EntidadDao, in TipoIdDao>(
        override val nombreEntidad: String,
        private val parametros: ParametrosParaDAOEntidadDeCliente<EntidadDao, TipoIdDao>,
        private val filtrosIgualdad: Sequence<FiltroIgualdad<*>>)
    : EliminablePorIdFiltrable<EntidadDao, TipoIdDao>
{
    override fun eliminarPorId(idCliente: Long, id: TipoIdDao): Boolean
    {
        val deleteBuilder = parametros[idCliente].dao.deleteBuilder()
        val whereEliminacion = deleteBuilder.where()
        whereEliminacion.idEq(id)
        var numeroClausulas = 1
        for (filtroIgualdad in filtrosIgualdad)
        {
            whereEliminacion.eq(filtroIgualdad.campo.nombreColumna, filtroIgualdad.valorColumnaUsoExterno)
            numeroClausulas++
        }
        if (numeroClausulas > 1)
        {
            whereEliminacion.and(numeroClausulas)
        }
        return deleteBuilder.delete() == 1
    }

    override fun conFiltrosSQL(filtrosIgualdad: Sequence<FiltroIgualdad<*>>): EliminablePorIdFiltrable<EntidadDao, TipoIdDao>
    {
        return EliminableDAOConFiltrosIgualdad(nombreEntidad, parametros, this.filtrosIgualdad + filtrosIgualdad)
    }
}

internal class EliminableSimple<out EntidadNegocio, in TipoIdNegocio, out EntidadDao, in TipoIdDao>(
        private val eliminador: EliminablePorIdFiltrable<EntidadDao, TipoIdDao>,
        private val transformadorId: Transformador<TipoIdNegocio, TipoIdDao>)
    : EliminablePorIdFiltrable<EntidadNegocio, TipoIdNegocio>
{
    override val nombreEntidad: String = eliminador.nombreEntidad

    override fun eliminarPorId(idCliente: Long, id: TipoIdNegocio): Boolean
    {
        return eliminador.eliminarPorId(idCliente, transformadorId.transformar(id))
    }

    override fun conFiltrosSQL(filtrosIgualdad: Sequence<FiltroIgualdad<*>>): EliminablePorIdFiltrable<EntidadNegocio, TipoIdNegocio>
    {
        return EliminableSimple(eliminador.conFiltrosSQL(filtrosIgualdad), transformadorId)
    }
}

internal interface ValidadorRestriccionEliminacion<in TipoId>
{
    fun validarRestriccion(idCliente: Long, idAEliminar: TipoId)
}

internal class EliminableConRestriccion<out Entidad, in TipoId>(
        private val eliminador: EliminablePorIdFiltrable<Entidad, TipoId>,
        private val validadorRestriccion: ValidadorRestriccionEliminacion<TipoId>)
    : EliminablePorIdFiltrable<Entidad, TipoId>
{
    override val nombreEntidad: String = eliminador.nombreEntidad

    override fun eliminarPorId(idCliente: Long, id: TipoId): Boolean
    {
        validadorRestriccion.validarRestriccion(idCliente, id)
        return eliminador.eliminarPorId(idCliente, id)
    }

    override fun conFiltrosSQL(filtrosIgualdad: Sequence<FiltroIgualdad<*>>): EliminablePorIdFiltrable<Entidad, TipoId>
    {
        return EliminableConRestriccion(eliminador.conFiltrosSQL(filtrosIgualdad), validadorRestriccion)
    }
}

internal class EliminableEntidadCompuesta<out EntidadPadre, in TipoIdPadre, out EntidadHijo, in TipoIdHijo>(
        private val buscadorRelacion: Buscable<EntidadPadre, TipoIdPadre>,
        private val eliminadorPadre: EliminablePorId<EntidadPadre, TipoIdPadre>,
        private val eliminadorHijo: EliminablePorId<EntidadHijo, TipoIdHijo>,
        private val extractorIdHijo: Transformador<EntidadPadre, TipoIdHijo>)
    : EliminablePorId<EntidadPadre, TipoIdPadre>
{
    override val nombreEntidad: String = eliminadorPadre.nombreEntidad

    override fun eliminarPorId(idCliente: Long, id: TipoIdPadre): Boolean
    {
        val entidadRelacion = buscadorRelacion.buscarPorId(idCliente, id)
        return if (entidadRelacion != null)
        {
            val eliminoPadreCorrectamente = eliminadorPadre.eliminarPorId(idCliente, id)
            if (eliminoPadreCorrectamente)
            {
                val idHijo = extractorIdHijo.transformar(entidadRelacion)
                if (!eliminadorHijo.eliminarPorId(idCliente, idHijo))
                {
                    throw ErrorEliminandoEntidad("$idHijo", eliminadorHijo.nombreEntidad)
                }
            }
            eliminoPadreCorrectamente
        }
        else
        {
            false
        }
    }
}

internal class EliminablePorIdEnTransaccionSQL<out Entidad, in TipoId>(
        private val configuracion: ConfiguracionRepositorios,
        private val eliminableSinTransaccion: EliminablePorId<Entidad, TipoId>)
    : EliminablePorId<Entidad, TipoId>
{
    override val nombreEntidad: String = eliminableSinTransaccion.nombreEntidad

    override fun eliminarPorId(idCliente: Long, id: TipoId): Boolean
    {
        return transaccionEnEsquemaClienteYProcesarErroresParaEliminacion(
                configuracion,
                idCliente,
                nombreEntidad
                                                                         ) {
            eliminableSinTransaccion.eliminarPorId(idCliente, id)
        }
    }
}