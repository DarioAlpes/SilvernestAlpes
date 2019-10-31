package co.smartobjects.persistencia.basederepositorios

import co.smartobjects.entidades.idsAncestorsEIdALlave
import co.smartobjects.persistencia.*
import co.smartobjects.persistencia.excepciones.*
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.field.SqlType

interface ActualizableConDiferenteSalida<in TipoEntidadNegocioEntrada, out TipoEntidadNegocioSalida, in TipoIdNegocio>
{
    val nombreEntidad: String

    @Throws(ErrorDAO::class)
    fun actualizar(idCliente: Long, idEntidad: TipoIdNegocio, entidadAActualizar: TipoEntidadNegocioEntrada): TipoEntidadNegocioSalida
}

interface Actualizable<EntidadDeNegocio, in TipoIdNegocio> : ActualizableConDiferenteSalida<EntidadDeNegocio, EntidadDeNegocio, TipoIdNegocio>

internal class ActualizableConDiferenteSalidaSegunActualizableYBuscable<in TipoEntidadNegocioEntrada, out TipoEntidadNegocioSalida, in TipoIdNegocio>(
        private val actualizableEntrada: Actualizable<TipoEntidadNegocioEntrada, TipoIdNegocio>,
        private val buscableSalida: Buscable<TipoEntidadNegocioSalida, TipoIdNegocio>)
    : ActualizableConDiferenteSalida<TipoEntidadNegocioEntrada, TipoEntidadNegocioSalida, TipoIdNegocio>
{
    override val nombreEntidad: String = actualizableEntrada.nombreEntidad
    override fun actualizar(idCliente: Long, idEntidad: TipoIdNegocio, entidadAActualizar: TipoEntidadNegocioEntrada): TipoEntidadNegocioSalida
    {
        actualizableEntrada.actualizar(idCliente, idEntidad, entidadAActualizar)
        val salida = buscableSalida.buscarPorId(idCliente, idEntidad)
        if (salida == null)
        {
            throw EntidadNoExiste(idEntidad.toString(), nombreEntidad)
        }
        else
        {
            return salida
        }
    }
}

@Suppress("unused")
internal interface ActualizableFiltrable<Entidad, in TipoId> : Actualizable<Entidad, TipoId>
{
    fun conFiltrosIgualdad(filtrosIgualdad: Sequence<FiltroIgualdad<*>>): ActualizableFiltrable<Entidad, TipoId>
}

internal class ActualizableDAO<TipoEntidadDao, in TipoIdDao>(
        private val parametros: ParametrosParaDAOEntidadDeCliente<TipoEntidadDao, TipoIdDao>,
        override val nombreEntidad: String)
    : ActualizableFiltrable<TipoEntidadDao, TipoIdDao>
{
    override fun actualizar(idCliente: Long, idEntidad: TipoIdDao, entidadAActualizar: TipoEntidadDao): TipoEntidadDao
    {
        val resultadoActualizacion = parametros[idCliente].dao.update(entidadAActualizar)
        if (resultadoActualizacion != 1)
        {
            throw EntidadNoExiste(idEntidad.toString(), nombreEntidad)
        }

        return entidadAActualizar
    }

    override fun conFiltrosIgualdad(filtrosIgualdad: Sequence<FiltroIgualdad<*>>): ActualizableFiltrable<TipoEntidadDao, TipoIdDao>
    {
        return ActualizableDAOConFiltrosIgualdad(parametros, nombreEntidad, filtrosIgualdad)
    }
}

internal class ActualizableDAOConFiltrosIgualdad<TipoEntidadDao, in TipoIdDao>(
        private val parametros: ParametrosParaDAOEntidadDeCliente<TipoEntidadDao, TipoIdDao>,
        override val nombreEntidad: String,
        private val filtrosIgualdad: Sequence<FiltroIgualdad<*>>
                                                                              ) : ActualizableFiltrable<TipoEntidadDao, TipoIdDao>
{
    override fun actualizar(idCliente: Long, idEntidad: TipoIdDao, entidadAActualizar: TipoEntidadDao): TipoEntidadDao
    {
        val queryBuilder = parametros[idCliente].dao.queryBuilder()
        val whereConsulta = queryBuilder.where()
        whereConsulta.idEq(idEntidad)
        var numeroClausulas = 1
        for (filtroIgualdad in filtrosIgualdad)
        {
            whereConsulta.eq(filtroIgualdad.campo.nombreColumna, filtroIgualdad.valorColumnaUsoExterno)
            numeroClausulas++
        }
        if (numeroClausulas > 1)
        {
            whereConsulta.and(numeroClausulas)
            // Solo se ejecuta la consulta si existia al menos un filtro de igualdad
            if (queryBuilder.limit(1).countOf() != 1L)
            {
                throw EntidadNoExiste(idEntidad.toString(), nombreEntidad)
            }
        }
        val resultadoActualizacion = parametros[idCliente].dao.update(entidadAActualizar)
        if (resultadoActualizacion != 1)
        {
            throw EntidadNoExiste(idEntidad.toString(), nombreEntidad)
        }

        return entidadAActualizar
    }

    override fun conFiltrosIgualdad(filtrosIgualdad: Sequence<FiltroIgualdad<*>>): ActualizableFiltrable<TipoEntidadDao, TipoIdDao>
    {
        return ActualizableDAOConFiltrosIgualdad(parametros, nombreEntidad, this.filtrosIgualdad + filtrosIgualdad)
    }
}

internal class ActualizableConIdMutable<Entidad : EntidadReferenciableDAO<TipoId>, out EntidadDao, TipoId>(
        private val parametrosDaoEntidadId: ParametrosParaDAOEntidadDeCliente<EntidadDao, TipoId>,
        private val nombreCampoId: String,
        private val actualizableSinIdMutable: Actualizable<Entidad, TipoId>,
        private val asignadorId: AsignadorParametro<Entidad, TipoId>)
    : Actualizable<Entidad, TipoId>
{
    override val nombreEntidad: String = actualizableSinIdMutable.nombreEntidad

    override fun actualizar(idCliente: Long, idEntidad: TipoId, entidadAActualizar: Entidad): Entidad
    {
        val idNuevo = entidadAActualizar.id

        val entidadDaoActualizada = actualizableSinIdMutable.actualizar(idCliente, idEntidad, asignadorId.asignarParametro(entidadAActualizar, idEntidad))

        if (idEntidad != idNuevo)
        {
            val numeroFilasActualizadas =
                    parametrosDaoEntidadId[idCliente].dao
                        .updateBuilder()
                        .updateColumnValue(nombreCampoId, idNuevo)
                        .apply { where().idEq(idEntidad) }
                        .update()

            if (numeroFilasActualizadas != 1)
            {
                throw ErrorDeCreacionActualizacionEntidad(nombreEntidad)
            }
        }

        return asignadorId.asignarParametro(entidadDaoActualizada, idNuevo)
    }
}


internal class ActualizableParcialCompuesto<EntidadOrigen, in TipoIdOrigen, in EntidadParcial>(
        private val actualizadorEntidad: Actualizable<EntidadOrigen, TipoIdOrigen>,
        private val extractorDatosParcialesDeDestino: Transformador<EntidadOrigen, EntidadParcial>,
        private val asignadorEntidadParcial: AsignadorParametro<EntidadOrigen, EntidadParcial>)
    : Actualizable<EntidadOrigen, TipoIdOrigen>
{
    override val nombreEntidad: String = actualizadorEntidad.nombreEntidad

    override fun actualizar(idCliente: Long, idEntidad: TipoIdOrigen, entidadAActualizar: EntidadOrigen): EntidadOrigen
    {
        val entidadActualizada = actualizadorEntidad.actualizar(idCliente, idEntidad, entidadAActualizar)
        return asignadorEntidadParcial.asignarParametro(entidadActualizada, extractorDatosParcialesDeDestino.transformar(entidadAActualizar))
    }
}

internal class ActualizableConTransformacion<EntidadOrigen, in TipoIdOrigen, out EntidadDestino, in TipoIdDestino>(
        private val actualizadorEntidad: Actualizable<EntidadDestino, TipoIdDestino>,
        private val transformadorId: Transformador<TipoIdOrigen, TipoIdDestino>,
        private val transformadorADestino: Transformador<EntidadOrigen, EntidadDestino>,
        private val transformadorAOrigen: TransformadorEntidadCliente<EntidadDestino, EntidadOrigen>)
    : Actualizable<EntidadOrigen, TipoIdOrigen>
{
    override val nombreEntidad: String = actualizadorEntidad.nombreEntidad

    override fun actualizar(idCliente: Long, idEntidad: TipoIdOrigen, entidadAActualizar: EntidadOrigen): EntidadOrigen
    {
        val entidadActualizada = actualizadorEntidad.actualizar(idCliente, transformadorId.transformar(idEntidad), transformadorADestino.transformar(entidadAActualizar))
        return transformadorAOrigen.transformar(idCliente, entidadActualizada)
    }
}

internal class ActualizableConEntidadParcial<Entidad, out EntidadParcial, in TipoIdDestino, in TipoIdParcial>(
        private val actualizadorEntidad: Actualizable<EntidadParcial, TipoIdParcial>,
        private val transformadorId: Transformador<TipoIdDestino, TipoIdParcial>,
        private val transformadorADestino: Transformador<Entidad, EntidadParcial>,
        private val asignadorEntidadParcial: AsignadorParametro<Entidad, EntidadParcial>,
        private val asignadorIdCliente: AsignadorParametro<Entidad, Long>)
    : Actualizable<Entidad, TipoIdDestino>
{
    override val nombreEntidad: String = actualizadorEntidad.nombreEntidad

    override fun actualizar(idCliente: Long, idEntidad: TipoIdDestino, entidadAActualizar: Entidad): Entidad
    {
        val entidadParcialActualizada = actualizadorEntidad.actualizar(idCliente, transformadorId.transformar(idEntidad), transformadorADestino.transformar(entidadAActualizar))
        return asignadorIdCliente.asignarParametro(asignadorEntidadParcial.asignarParametro(entidadAActualizar, entidadParcialActualizada), idCliente)
    }
}

internal class ActualizableSimple<EntidadDeNegocio, out EntidadDao : EntidadDAO<EntidadDeNegocio>, in TipoIdNegocio, out TipoIdDao>(
        private val actualizadorEntidad: Actualizable<EntidadDao, TipoIdDao>,
        private val transformadorId: Transformador<TipoIdNegocio, TipoIdDao>,
        private val transformadorEntidad: Transformador<EntidadDeNegocio, EntidadDao>)
    : Actualizable<EntidadDeNegocio, TipoIdNegocio>
{
    override val nombreEntidad: String = actualizadorEntidad.nombreEntidad

    override fun actualizar(idCliente: Long, idEntidad: TipoIdNegocio, entidadAActualizar: EntidadDeNegocio): EntidadDeNegocio
    {
        val entidadComoDao = actualizadorEntidad.actualizar(idCliente, transformadorId.transformar(idEntidad), transformadorEntidad.transformar(entidadAActualizar))
        return entidadComoDao.aEntidadDeNegocio(idCliente)
    }
}

internal class ActualizableEntidadCompuesta<EntidadPadre, in TipoIdPadre, EntidadHijo, in TipoIdHijo>(
        private val actualizablePadre: Actualizable<EntidadPadre, TipoIdPadre>,
        private val actualizableHijo: Actualizable<EntidadHijo, TipoIdHijo>)
    : Actualizable<EntidadRelacionUnoAUno<EntidadPadre, EntidadHijo>, EntidadRelacionUnoAUno<TipoIdPadre, TipoIdHijo>>
{
    override val nombreEntidad: String = actualizablePadre.nombreEntidad

    override fun actualizar(idCliente: Long, idEntidad: EntidadRelacionUnoAUno<TipoIdPadre, TipoIdHijo>, entidadAActualizar: EntidadRelacionUnoAUno<EntidadPadre, EntidadHijo>): EntidadRelacionUnoAUno<EntidadPadre, EntidadHijo>
    {
        val actualizadaPadre = actualizablePadre.actualizar(idCliente, idEntidad.entidadOrigen, entidadAActualizar.entidadOrigen)
        val actualizadaHija = actualizableHijo.actualizar(idCliente, idEntidad.entidadDestino, entidadAActualizar.entidadDestino)
        return EntidadRelacionUnoAUno(actualizadaPadre, actualizadaHija)
    }
}

internal interface ValidadorRestriccionActualizacion<in EntidadNegocio, in TipoId>
{
    fun validar(idCliente: Long, id: TipoId, entidadAActualizar: EntidadNegocio)
}

internal class ActualizableConRestriccion<EntidadNegocio, in TipoId>(
        private val actualizador: Actualizable<EntidadNegocio, TipoId>,
        private val validadorRestriccion: ValidadorRestriccionActualizacion<EntidadNegocio, TipoId>)
    : Actualizable<EntidadNegocio, TipoId>
{
    override val nombreEntidad: String = actualizador.nombreEntidad

    override fun actualizar(idCliente: Long, idEntidad: TipoId, entidadAActualizar: EntidadNegocio): EntidadNegocio
    {
        validadorRestriccion.validar(idCliente, idEntidad, entidadAActualizar)
        return actualizador.actualizar(idCliente, idEntidad, entidadAActualizar)
    }
}

internal class JerarquiaActualizableDAO<TipoEntidadDao : JerarquicoDAO>(
        private val parametros: ParametrosParaDAOEntidadDeCliente<TipoEntidadDao, Long>,
        private val actualizadorEntidad: Actualizable<TipoEntidadDao, Long>)
    : Actualizable<TipoEntidadDao, Long>
{
    override val nombreEntidad: String = actualizadorEntidad.nombreEntidad

    private fun asignarLlaveDeJerarquiaOriginal(dao: Dao<TipoEntidadDao, Long>, entidadDao: TipoEntidadDao)
            : TipoEntidadDao
    {
        val idsAncestrosOriginales =
                if (entidadDao.idDelPadre != null)
                {
                    val categoriaPadre = dao.queryForId(entidadDao.idDelPadre)
                    categoriaPadre?.darIdsAncestros()?.apply { add(entidadDao.idDelPadre ?: 0L) }
                    ?: if (parametros.configuracion.llavesForaneasActivadas)
                    {
                        throw ErrorDeLlaveForanea(entidadDao.idDelPadre, nombreEntidad)
                    }
                    else
                    {
                        entidadDao.darIdsAncestros()
                    }
                }
                else
                {
                    linkedSetOf()
                }

        return entidadDao.apply { llaveJerarquia = idsAncestorsEIdALlave(idsAncestrosOriginales, entidadDao.id) }
    }

    private fun actualizarLlavesDescendientesSiCambio(
            dao: Dao<TipoEntidadDao, Long>,
            parametrosDeEntidad: ParametrosActualizacionEntidadJerarquica<TipoEntidadDao>
                                                     )
    {
        with(parametrosDeEntidad)
        {
            if (entidadDaoOriginal.llaveJerarquia != entidadDaoActualizada.llaveJerarquia)
            {
                val descendientes =
                        dao.queryBuilder()
                            .where()
                            .like(nombreCampoLlave, "${entidadDaoOriginal.llaveJerarquia}:%")
                            .query()

                for (descendiente in descendientes)
                {
                    val descendienteActualizado = descendiente.apply {
                        llaveJerarquia =
                                descendiente
                                    .llaveJerarquia
                                    .replace(
                                            entidadDaoOriginal.llaveJerarquia,
                                            entidadDaoActualizada.llaveJerarquia
                                            )
                    }
                    val resultadoDescendiente = dao.update(descendienteActualizado)
                    if (resultadoDescendiente != 1)
                    {
                        throw ErrorDeCreacionActualizacionEntidad("$nombreEntidad[id=${descendiente.id}]")
                    }
                }
            }
        }
    }

    @Throws(ErrorDAO::class)
    override fun actualizar(idCliente: Long, idEntidad: Long, entidadAActualizar: TipoEntidadDao): TipoEntidadDao
    {
        val entidadDAOOriginal =
                parametros[idCliente].dao.queryForId(entidadAActualizar.id)
                ?: throw EntidadNoExiste(idEntidad, nombreEntidad)

        val entidadConAncestros = asignarLlaveDeJerarquiaOriginal(parametros[idCliente].dao, entidadAActualizar)

        if (entidadConAncestros.darIdsAncestros().contains(idEntidad))
        {
            throw ErrorDeJerarquiaPorCiclo(nombreEntidad, idEntidad, entidadAActualizar.idDelPadre!!)
        }

        val entidadDAOActualizada = actualizadorEntidad.actualizar(idCliente, idEntidad, entidadConAncestros)

        actualizarLlavesDescendientesSiCambio(
                parametros[idCliente].dao,
                ParametrosActualizacionEntidadJerarquica(
                        JerarquicoDAO.NOMBRE_CAMPO_LLAVE,
                        entidadDAOOriginal,
                        entidadDAOActualizada
                                                        )
                                             )

        return entidadDAOActualizada
    }

    data class ParametrosActualizacionEntidadJerarquica<out EntidadDao : JerarquicoDAO>(
            val nombreCampoLlave: String,
            val entidadDaoOriginal: EntidadDao,
            val entidadDaoActualizada: EntidadDao
                                                                                       )
}

internal class ActualizableEntidadCompuestaClonandoHijo<EntidadPadre, in TipoIdPadre, out EntidadHijo, in TipoIdHijo>(
        private val buscadorEntidadPadre: Buscable<EntidadPadre, TipoIdPadre>,
        private val creadorHijo: Creable<EntidadHijo>,
        private val eliminadorHijo: EliminablePorId<EntidadHijo, TipoIdHijo>,
        private val actualizadorPadre: Actualizable<EntidadPadre, TipoIdPadre>,
        private val extractorEntidadHijo: Transformador<EntidadPadre, EntidadHijo>,
        private val extractorIdHijo: Transformador<EntidadHijo, TipoIdHijo>,
        private val asignadorHijo: AsignadorParametro<EntidadPadre, EntidadHijo>)
    : Actualizable<EntidadPadre, TipoIdPadre>
{
    override val nombreEntidad: String = actualizadorPadre.nombreEntidad

    override fun actualizar(idCliente: Long, idEntidad: TipoIdPadre, entidadAActualizar: EntidadPadre): EntidadPadre
    {
        val entidadActual = buscadorEntidadPadre.buscarPorId(idCliente, idEntidad)
                            ?: throw EntidadNoExiste(idEntidad.toString(), nombreEntidad)

        val hijoNuevo = creadorHijo.crear(idCliente, extractorEntidadHijo.transformar(entidadAActualizar))

        val entidadConHijoNuevo = asignadorHijo.asignarParametro(entidadAActualizar, hijoNuevo)
        val resultadoActualizacion = actualizadorPadre.actualizar(idCliente, idEntidad, entidadConHijoNuevo)

        val hijoViejo = extractorEntidadHijo.transformar(entidadActual)
        val idHijoViejo = extractorIdHijo.transformar(hijoViejo)
        if (!eliminadorHijo.eliminarPorId(idCliente, idHijoViejo))
        {
            throw ErrorEliminandoEntidad(idHijoViejo.toString(), eliminadorHijo.nombreEntidad)
        }

        return resultadoActualizacion
    }
}

internal class ActualizableEntidadRelacionUnoAMuchosClonandoHijos<EntidadPadre, in TipoIdPadre, TipoIdHijo>(
        private val actualizadorEntidad: Actualizable<EntidadPadre, TipoIdPadre>,
        private val nombreColumnaFkHaciaIdPadre: String,
        vararg hijosActualizableAClonar: HijoClonable<EntidadPadre, *, TipoIdHijo>)
    : Actualizable<EntidadPadre, TipoIdPadre>
{
    override val nombreEntidad: String = actualizadorEntidad.nombreEntidad

    private val hijosAClonar = mutableListOf<HijoClonable<EntidadPadre, *, *>>(*hijosActualizableAClonar)

    override fun actualizar(idCliente: Long, idEntidad: TipoIdPadre, entidadAActualizar: EntidadPadre): EntidadPadre
    {
        return hijosAClonar.fold(actualizadorEntidad.actualizar(idCliente, idEntidad, entidadAActualizar))
        { entidadActualizada: EntidadPadre, hijoClonable: HijoClonable<EntidadPadre, *, *> ->

            val filtroIgualdad =
                    FiltroIgualdad(
                            hijoClonable.darCampoFKEnHijo(nombreColumnaFkHaciaIdPadre),
                            idEntidad,
                            hijoClonable.tipoSQLFK
                                  )

            val eliminoCorrectamenteHijos =
                    hijoClonable.eliminadorHijos
                        .eliminarSegunFiltros(idCliente, sequenceOf(filtroIgualdad))

            if (!eliminoCorrectamenteHijos)
            {
                throw ErrorEliminandoEntidad(idEntidad.toString(), "${hijoClonable.eliminadorHijos.nombreEntidad} de $nombreEntidad")
            }

            hijoClonable.actualizar(idCliente, entidadActualizada)
        }
    }

    data class HijoClonable<Padre, out Tipo, TipoId>(
            private val nombreDeTablaHija: String,
            val eliminadorHijos: EliminablePorIgualdad<Tipo>,
            val tipoSQLFK: SqlType,
            private val creadorHijos: CreableDAOMultiples<Tipo, TipoId>,
            private val extractorEntidadesHijo: Transformador<Padre, List<Tipo>>,
            private val asignadorHijos: AsignadorParametro<Padre, List<Tipo>>)
    {
        fun darCampoFKEnHijo(columnaFkHaciaIdPadre: String): CampoTabla = CampoTabla(nombreDeTablaHija, columnaFkHaciaIdPadre)

        fun actualizar(idCliente: Long, entidadAActualizar: Padre): Padre
        {
            val entidadesHijoACrear = extractorEntidadesHijo.transformar(entidadAActualizar)
            val entidadesHijoCreadas = creadorHijos.crear(idCliente, entidadesHijoACrear)

            return asignadorHijos.asignarParametro(entidadAActualizar, entidadesHijoCreadas)
        }
    }
}

internal class ActualizableEnTransaccionSQL<EntidadDeNegocio, in TipoIdNegocio>(
        private val configuracion: ConfiguracionRepositorios,
        private val actualizableSinTransaccion: Actualizable<EntidadDeNegocio, TipoIdNegocio>)
    : Actualizable<EntidadDeNegocio, TipoIdNegocio>
{
    override val nombreEntidad: String = actualizableSinTransaccion.nombreEntidad

    override fun actualizar(idCliente: Long, idEntidad: TipoIdNegocio, entidadAActualizar: EntidadDeNegocio): EntidadDeNegocio
    {
        return transaccionEnEsquemaClienteYProcesarErroresParaActualizacion(
                configuracion,
                idCliente,
                nombreEntidad
                                                                           ) {
            actualizableSinTransaccion.actualizar(idCliente, idEntidad, entidadAActualizar)
        }
    }
}

internal class ActualizableEnTransaccionSQLParaDiferenteSalida<in EntidadDeNegocioEntrada, out EntidadDeNegocioResultado, in TipoIdNegocio>(
        private val configuracion: ConfiguracionRepositorios,
        private val actualizableSinTransaccion: ActualizableConDiferenteSalida<EntidadDeNegocioEntrada, EntidadDeNegocioResultado, TipoIdNegocio>)
    : ActualizableConDiferenteSalida<EntidadDeNegocioEntrada, EntidadDeNegocioResultado, TipoIdNegocio>
{
    override val nombreEntidad: String = actualizableSinTransaccion.nombreEntidad

    override fun actualizar(idCliente: Long, idEntidad: TipoIdNegocio, entidadAActualizar: EntidadDeNegocioEntrada): EntidadDeNegocioResultado
    {
        return transaccionEnEsquemaClienteYProcesarErroresParaActualizacion(configuracion, idCliente, nombreEntidad) {
            actualizableSinTransaccion.actualizar(idCliente, idEntidad, entidadAActualizar)
        }
    }
}