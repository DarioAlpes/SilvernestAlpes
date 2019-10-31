package co.smartobjects.persistencia.basederepositorios

import co.smartobjects.entidades.idsAncestorsEIdALlave
import co.smartobjects.persistencia.*
import co.smartobjects.persistencia.excepciones.ErrorDAO
import co.smartobjects.persistencia.excepciones.ErrorDeCreacionActualizacionEntidad
import co.smartobjects.persistencia.excepciones.ErrorDeLlaveForanea

abstract class EntidadConFiltrosIgualdadEliminacion
{
    internal abstract val filtrosEliminacion: List<FiltroIgualdad<*>>
}

interface CreableConDiferenteSalida<in EntidadDeNegocioEntrada, out EntidadDeNegocioResultado>
{
    val nombreEntidad: String

    @Throws(ErrorDAO::class)
    fun crear(idCliente: Long, entidadACrear: EntidadDeNegocioEntrada): EntidadDeNegocioResultado
}

internal class CreableConDiferenteSalidaSegunActualizableYBuscable<in TipoEntidadNegocioEntrada, out TipoEntidadNegocioSalida, in TipoIdNegocio>(
        private val creableEntrada: Creable<TipoEntidadNegocioEntrada>,
        private val buscableSalida: Buscable<TipoEntidadNegocioSalida, TipoIdNegocio>,
        private val extractorIdEntidad: Transformador<TipoEntidadNegocioEntrada, TipoIdNegocio>)
    : CreableConDiferenteSalida<TipoEntidadNegocioEntrada, TipoEntidadNegocioSalida>
{
    override val nombreEntidad: String = creableEntrada.nombreEntidad
    override fun crear(idCliente: Long, entidadACrear: TipoEntidadNegocioEntrada): TipoEntidadNegocioSalida
    {
        val entidadCreada = creableEntrada.crear(idCliente, entidadACrear)
        val salida = buscableSalida.buscarPorId(idCliente, extractorIdEntidad.transformar(entidadCreada))
        if (salida == null)
        {
            throw ErrorDeCreacionActualizacionEntidad(nombreEntidad)
        }
        else
        {
            return salida
        }
    }
}

internal class CreableMultiplesUnoAUnoConDiferenteSalida<in EntidadNegocioOrigen, out EntidadNegocioDestino>(
        private val creador: CreableConDiferenteSalida<EntidadNegocioOrigen, EntidadNegocioDestino>)
    : CreableConDiferenteSalida<List<EntidadNegocioOrigen>, List<EntidadNegocioDestino>>
{
    override val nombreEntidad: String = creador.nombreEntidad
    override fun crear(idCliente: Long, entidadACrear: List<EntidadNegocioOrigen>): List<EntidadNegocioDestino>
    {
        return entidadACrear.map { creador.crear(idCliente, it) }
    }
}

internal class CreableValidandoSeCrearonNumeroEntidadesCorrectasConDiferenteSalida<in EntidadEntrada, out EntidadSalida, Parametros : ParametrosConsulta>(
        private val creador: CreableConDiferenteSalida<EntidadEntrada, List<EntidadSalida>>,
        private val contador: ContableConParametros<EntidadSalida, Parametros>,
        private val extractorParametrosEntrada: Transformador<EntidadEntrada, Parametros>,
        private val generadorExcepcionALanzarSiCuentaEsDiferente: () -> ErrorDAO)
    : CreableConDiferenteSalida<EntidadEntrada, List<EntidadSalida>>
{
    override val nombreEntidad: String = creador.nombreEntidad

    override fun crear(idCliente: Long, entidadACrear: EntidadEntrada): List<EntidadSalida>
    {
        val entidadesCreadas = creador.crear(idCliente, entidadACrear)
        val numeroEntidadesCreadas = contador.contar(idCliente, extractorParametrosEntrada.transformar(entidadACrear))
        if (entidadesCreadas.size.toLong() != numeroEntidadesCreadas)
        {
            throw generadorExcepcionALanzarSiCuentaEsDiferente()
        }
        return entidadesCreadas
    }

}

internal class CreableConRestriccionConDiferenteSalida<in EntidadNegocioEntrada, out EntidadNegocioSalida>(
        private val creador: CreableConDiferenteSalida<EntidadNegocioEntrada, EntidadNegocioSalida>,
        private val validadorRestriccion: ValidadorRestriccionCreacion<EntidadNegocioEntrada>)
    : CreableConDiferenteSalida<EntidadNegocioEntrada, EntidadNegocioSalida>
{
    override val nombreEntidad: String = creador.nombreEntidad

    override fun crear(idCliente: Long, entidadACrear: EntidadNegocioEntrada): EntidadNegocioSalida
    {
        validadorRestriccion.validar(idCliente, entidadACrear)
        return creador.crear(idCliente, entidadACrear)
    }
}

internal class CreableEliminandoMultiplesEntidadesConDiferenteSalida<in EntidadNegocioEntrada : EntidadConFiltrosIgualdadEliminacion, out EntidadNegocioSalida, out EntidadAEliminar>(
        private val creador: CreableConDiferenteSalida<EntidadNegocioEntrada, EntidadNegocioSalida>,
        private val eliminador: EliminablePorIgualdad<EntidadAEliminar>)
    : CreableConDiferenteSalida<EntidadNegocioEntrada, EntidadNegocioSalida>
{
    override val nombreEntidad: String = creador.nombreEntidad

    override fun crear(idCliente: Long, entidadACrear: EntidadNegocioEntrada): EntidadNegocioSalida
    {
        val filtros = entidadACrear.filtrosEliminacion.toList()
        if (filtros.any())
        {
            eliminador.eliminarSegunFiltros(idCliente, filtros.asSequence())
        }
        return creador.crear(idCliente, entidadACrear)
    }
}

internal class CreableConTransformacionConDiferenteSalida<in EntidadOrigen, out EntidadDestino>(
        private val creadorEntidadDestino: Creable<EntidadDestino>,
        private val transformadorHaciaDestino: TransformadorEntidadCliente<EntidadOrigen, EntidadDestino>)
    : CreableConDiferenteSalida<EntidadOrigen, EntidadDestino>
{
    override val nombreEntidad: String = creadorEntidadDestino.nombreEntidad

    override fun crear(idCliente: Long, entidadACrear: EntidadOrigen): EntidadDestino
    {
        val entidadDestinoACrear = transformadorHaciaDestino.transformar(idCliente, entidadACrear)

        return creadorEntidadDestino.crear(idCliente, entidadDestinoACrear)
    }
}

internal class CreableConTransformacionIntermediaConDiferenteSalida<in EntidadInicial, EntidadIntermedia, out EntidadFinal>(
        private val creadorEntidadDestino: Creable<EntidadIntermedia>,
        private val transformadorDeInicialnAIntermedia: TransformadorEntidadCliente<EntidadInicial, EntidadIntermedia>,
        private val transformadorDeIntermediaAFinal: TransformadorEntidadCliente<EntidadIntermedia, EntidadFinal>)
    : CreableConDiferenteSalida<EntidadInicial, EntidadFinal>
{
    override val nombreEntidad: String = creadorEntidadDestino.nombreEntidad

    override fun crear(idCliente: Long, entidadACrear: EntidadInicial): EntidadFinal
    {
        val entidadIntermediaACrear = transformadorDeInicialnAIntermedia.transformar(idCliente, entidadACrear)

        val entidadIntermediaCreada = creadorEntidadDestino.crear(idCliente, entidadIntermediaACrear)

        return transformadorDeIntermediaAFinal.transformar(idCliente, entidadIntermediaCreada)
    }
}

internal class CreableConTransformacionIntermediaTemporalConDiferenteSalida<in EntidadInicial, EntidadIntermedia, out EntidadFinal>(
        private val creadorEntidadDestino: CreableConDiferenteSalida<EntidadIntermedia, EntidadFinal>,
        private val transformadorDeInicialnAIntermedia: TransformadorEntidadCliente<EntidadInicial, EntidadIntermedia>)
    : CreableConDiferenteSalida<EntidadInicial, EntidadFinal>
{
    override val nombreEntidad: String = creadorEntidadDestino.nombreEntidad

    override fun crear(idCliente: Long, entidadACrear: EntidadInicial): EntidadFinal
    {
        val entidadIntermediaACrear = transformadorDeInicialnAIntermedia.transformar(idCliente, entidadACrear)

        return creadorEntidadDestino.crear(idCliente, entidadIntermediaACrear)
    }
}

internal class CreableEnTransaccionSQLParaDiferenteSalida<in EntidadDeNegocioEntrada, out EntidadDeNegocioResultado>(
        private val configuracion: ConfiguracionRepositorios,
        private val creableSinTransaccion: CreableConDiferenteSalida<EntidadDeNegocioEntrada, EntidadDeNegocioResultado>)
    : CreableConDiferenteSalida<EntidadDeNegocioEntrada, EntidadDeNegocioResultado>
{

    override val nombreEntidad: String = creableSinTransaccion.nombreEntidad

    override fun crear(idCliente: Long, entidadACrear: EntidadDeNegocioEntrada): EntidadDeNegocioResultado
    {
        return transaccionEnEsquemaClienteYProcesarErroresParaCreacion(configuracion, idCliente, nombreEntidad) {
            creableSinTransaccion.crear(idCliente, entidadACrear)
        }
    }
}

interface Creable<EntidadDeNegocio> : CreableConDiferenteSalida<EntidadDeNegocio, EntidadDeNegocio>

internal class CreableDAO<EntidadDao, TipoId>(
        private val parametros: ParametrosParaDAOEntidadDeCliente<EntidadDao, TipoId>,
        override val nombreEntidad: String)
    : Creable<EntidadDao>
{
    override fun crear(idCliente: Long, entidadACrear: EntidadDao): EntidadDao
    {
        val resultadoCreacionEntidad = parametros[idCliente].dao.create(entidadACrear)
        if (resultadoCreacionEntidad != 1)
        {
            throw ErrorDeCreacionActualizacionEntidad(nombreEntidad)
        }
        return entidadACrear
    }
}

internal class CreableDAOMultiples<EntidadDao, TipoId>
(
        private val parametros: ParametrosParaDAOEntidadDeCliente<EntidadDao, TipoId>,
        override val nombreEntidad: String
) : Creable<List<EntidadDao>>
{
    override fun crear(idCliente: Long, entidadACrear: List<EntidadDao>): List<EntidadDao>
    {
        val entidadesACrear = entidadACrear.toList()
        val resultadoCreacionEntidad = parametros[idCliente].dao.create(entidadesACrear)
        if (resultadoCreacionEntidad != entidadesACrear.size)
        {
            throw ErrorDeCreacionActualizacionEntidad(nombreEntidad)
        }
        return entidadACrear
    }
}

internal class CreableSiNoEsNulo<Entidad>(
        private val creableEntidad: Creable<Entidad>)
    : Creable<Entidad?>
{
    override val nombreEntidad: String = creableEntidad.nombreEntidad

    override fun crear(idCliente: Long, entidadACrear: Entidad?): Entidad?
    {
        return entidadACrear?.run { creableEntidad.crear(idCliente, entidadACrear) }
    }
}

internal class CreableSimpleLista<EntidadDeNegocio, EntidadDao : EntidadDAO<EntidadDeNegocio>>(
        private val creableConTransformacion: CreableConTransformacion<List<EntidadDeNegocio>, List<EntidadDao>>)
    : Creable<List<EntidadDeNegocio>> by creableConTransformacion
{
    constructor(
            creadorEntidadDAO: Creable<List<EntidadDao>>,
            transformadorHaciaDao: TransformadorEntidadCliente<EntidadDeNegocio, EntidadDao>
               )
            : this(
            CreableConTransformacion(
                    creadorEntidadDAO,
                    TransformadorListaSegunTransformadorEntidad(transformadorHaciaDao),
                    TransformadorListaSegunTransformadorEntidad(TransformadorEntidadDao<EntidadDeNegocio, EntidadDao>())
                                    )
                  )
}

internal class CreableMultiplesUnoAUno<EntidadNegocio>(
        private val creador: Creable<EntidadNegocio>)
    : Creable<List<EntidadNegocio>>
{
    override val nombreEntidad: String = creador.nombreEntidad
    override fun crear(idCliente: Long, entidadACrear: List<EntidadNegocio>): List<EntidadNegocio>
    {
        return entidadACrear.map { creador.crear(idCliente, it) }
    }
}

internal class CreableConEntidadParcial<Entidad, out EntidadParcial>(
        private val creableEntidad: Creable<EntidadParcial>,
        private val transformadorDesdeDestino: Transformador<Entidad, EntidadParcial>,
        private val asignadorEntidadParcial: AsignadorParametro<Entidad, EntidadParcial>)
    : Creable<Entidad>
{
    override val nombreEntidad: String = creableEntidad.nombreEntidad

    override fun crear(idCliente: Long, entidadACrear: Entidad): Entidad
    {
        val entidadParcialActualizada = creableEntidad.crear(idCliente, transformadorDesdeDestino.transformar(entidadACrear))
        return asignadorEntidadParcial.asignarParametro(entidadACrear, entidadParcialActualizada)
    }
}

internal class CreableMultiplesEntidadesParciales<Entidad>(
        private val creablesEntidades: List<CreableConEntidadParcial<Entidad, *>>)
    : Creable<Entidad>
{
    override val nombreEntidad: String = creablesEntidades.first().nombreEntidad

    override fun crear(idCliente: Long, entidadACrear: Entidad): Entidad
    {
        return creablesEntidades.fold(entidadACrear) { actual, creable -> creable.crear(idCliente, actual) }
    }
}

internal interface ValidadorRestriccionCreacion<in EntidadNegocio>
{
    fun validar(idCliente: Long, entidadACrear: EntidadNegocio)
}

internal class CreableConRestriccion<EntidadNegocio>(
        private val creador: Creable<EntidadNegocio>,
        private val validadorRestriccion: ValidadorRestriccionCreacion<EntidadNegocio>)
    : Creable<EntidadNegocio>
{
    override val nombreEntidad: String = creador.nombreEntidad

    override fun crear(idCliente: Long, entidadACrear: EntidadNegocio): EntidadNegocio
    {
        validadorRestriccion.validar(idCliente, entidadACrear)
        return creador.crear(idCliente, entidadACrear)
    }
}

internal class CreableEliminandoMultiplesConDiferenteSalidaYParametros<
        EntidadNegocioEntrada,
        in Parametros : ParametrosConsulta,
        out EntidadNegocioSalida,
        out EntidadAEliminar
        >
(
        private val creador: CreableConDiferenteSalida<EntidadNegocioEntrada, EntidadNegocioSalida>,
        private val parametrosEliminacion: Parametros,
        private val eliminador: EliminablePorParametros<EntidadAEliminar, Parametros>
) : CreableConDiferenteSalida<EntidadNegocioEntrada, EntidadNegocioSalida>
{
    override val nombreEntidad: String = creador.nombreEntidad

    override fun crear(idCliente: Long, entidadACrear: EntidadNegocioEntrada): EntidadNegocioSalida
    {
        eliminador.eliminarSegunFiltros(idCliente, parametrosEliminacion)
        return creador.crear(idCliente, entidadACrear)
    }
}

internal class CreableEliminandoSegunParametrosConDiferenteSalidaYParametros<
        EntidadNegocioEntrada,
        in Parametros : ParametrosConsulta,
        out EntidadNegocioSalida,
        out EntidadAEliminar
        >
(
        private val creador: CreableConDiferenteSalida<EntidadNegocioEntrada, EntidadNegocioSalida>,
        private val generadorDeParametros: (EntidadNegocioEntrada) -> Parametros,
        private val eliminador: EliminablePorParametros<EntidadAEliminar, Parametros>
) : CreableConDiferenteSalida<EntidadNegocioEntrada, EntidadNegocioSalida>
{
    override val nombreEntidad: String = creador.nombreEntidad

    override fun crear(idCliente: Long, entidadACrear: EntidadNegocioEntrada): EntidadNegocioSalida
    {
        eliminador.eliminarSegunFiltros(idCliente, generadorDeParametros(entidadACrear))
        return creador.crear(idCliente, entidadACrear)
    }
}

internal class CreableEliminandoEntidad<EntidadNegocio, in TipoId>(
        private val creador: Creable<EntidadNegocio>,
        private val eliminador: EliminablePorId<EntidadNegocio, TipoId>,
        private val extractorId: Transformador<EntidadNegocio, TipoId>)
    : Creable<EntidadNegocio>
{
    override val nombreEntidad: String = creador.nombreEntidad

    override fun crear(idCliente: Long, entidadACrear: EntidadNegocio): EntidadNegocio
    {
        eliminador.eliminarPorId(idCliente, extractorId.transformar(entidadACrear))
        return creador.crear(idCliente, entidadACrear)
    }
}

internal class CreableDAORelacionUnoAUno<EntidadOrigenDAO, EntidadDestinoDAO>(
        private val creadorEntidadOrigen: Creable<EntidadOrigenDAO>,
        private val creadorEntidadDestino: Creable<EntidadDestinoDAO>,
        private val transformadorEntidadesRelacionadas: TransformadorEntidadesRelacionadas<EntidadOrigenDAO, EntidadDestinoDAO>)
    : Creable<EntidadRelacionUnoAUno<EntidadOrigenDAO, EntidadDestinoDAO>>
{
    override val nombreEntidad: String = creadorEntidadOrigen.nombreEntidad

    override fun crear(idCliente: Long, entidadACrear: EntidadRelacionUnoAUno<EntidadOrigenDAO, EntidadDestinoDAO>)
            : EntidadRelacionUnoAUno<EntidadOrigenDAO, EntidadDestinoDAO>
    {
        val entidadOrigenCreada = creadorEntidadOrigen.crear(idCliente, entidadACrear.entidadOrigen)
        val entidadesDestinoCreadas =
                creadorEntidadDestino.crear(
                        idCliente,
                        transformadorEntidadesRelacionadas.asignarCampoRelacionAEntidadDestino(entidadOrigenCreada, entidadACrear.entidadDestino)
                                           )

        return EntidadRelacionUnoAUno(entidadOrigenCreada, entidadesDestinoCreadas)
    }
}

internal class CreableDAORelacionUnoAMuchos<EntidadOrigenDAO, EntidadDestinoDAO>(
        private val creadorEntidadOrigen: Creable<EntidadOrigenDAO>,
        private val creadorEntidadDestino: Creable<List<EntidadDestinoDAO>>,
        private val transformadorEntidadesRelacionadas: TransformadorEntidadesRelacionadas<EntidadOrigenDAO, EntidadDestinoDAO>)
    : Creable<EntidadRelacionUnoAMuchos<EntidadOrigenDAO, EntidadDestinoDAO>>
{
    override val nombreEntidad: String = creadorEntidadOrigen.nombreEntidad

    override fun crear(idCliente: Long, entidadACrear: EntidadRelacionUnoAMuchos<EntidadOrigenDAO, EntidadDestinoDAO>)
            : EntidadRelacionUnoAMuchos<EntidadOrigenDAO, EntidadDestinoDAO>
    {
        val entidadOrigenCreada = creadorEntidadOrigen.crear(idCliente, entidadACrear.entidadOrigen)
        val entidadesACrearConOrigenActualizado = entidadACrear.entidadDestino.map {
            transformadorEntidadesRelacionadas.asignarCampoRelacionAEntidadDestino(entidadOrigenCreada, it)
        }
        val entidadesDestinoCreadas = creadorEntidadDestino.crear(idCliente, entidadesACrearConOrigenActualizado)
        return EntidadRelacionUnoAMuchos(entidadOrigenCreada, entidadesDestinoCreadas)
    }
}

internal class CreableDAOUnoAMuchosAnidado<EntidadMadreDAO, EntidadHijaDAO, EntidadNietaDAO>(
        private val creadorEntidadMadre: Creable<EntidadMadreDAO>,
        private val creadorRelacionHijaNieta: CreableMultiplesUnoAUno<EntidadRelacionUnoAMuchos<EntidadHijaDAO, EntidadNietaDAO>>,
        private val asignadorRelacionPadreHija: AsignadorParametro<EntidadRelacionUnoAMuchos<EntidadHijaDAO, EntidadNietaDAO>, EntidadMadreDAO>)
    : Creable<EntidadRelacionUnoAMuchos<EntidadMadreDAO, EntidadRelacionUnoAMuchos<EntidadHijaDAO, EntidadNietaDAO>>>
{
    override val nombreEntidad: String = creadorEntidadMadre.nombreEntidad

    override fun crear(
            idCliente: Long,
            entidadACrear: EntidadRelacionUnoAMuchos<EntidadMadreDAO, EntidadRelacionUnoAMuchos<EntidadHijaDAO, EntidadNietaDAO>>)
            : EntidadRelacionUnoAMuchos<EntidadMadreDAO, EntidadRelacionUnoAMuchos<EntidadHijaDAO, EntidadNietaDAO>>
    {
        val entidadMadreCreada = creadorEntidadMadre.crear(idCliente, entidadACrear.entidadOrigen)

        val hijasACrearConRelacionHaciaMadre = entidadACrear.entidadDestino.map {
            asignadorRelacionPadreHija.asignarParametro(it, entidadMadreCreada)
        }

        val entidadesDestinoCreadas = creadorRelacionHijaNieta.crear(idCliente, hijasACrearConRelacionHaciaMadre)

        return EntidadRelacionUnoAMuchos(entidadMadreCreada, entidadesDestinoCreadas)
    }
}

internal class CreableConTransformacionRetornandoEntidadOriginal<EntidadOrigen, EntidadDestino>(
        private val creadorEntidadDestino: Creable<EntidadDestino>,
        private val transformadorHaciaDestino: TransformadorEntidadCliente<EntidadOrigen, EntidadDestino>)
    : Creable<EntidadOrigen>
{
    override val nombreEntidad: String = creadorEntidadDestino.nombreEntidad

    override fun crear(idCliente: Long, entidadACrear: EntidadOrigen): EntidadOrigen
    {
        creadorEntidadDestino.crear(
                idCliente,
                transformadorHaciaDestino.transformar(idCliente, entidadACrear)
                                   )
        return entidadACrear
    }
}

internal class CreableConTransformacion<EntidadOrigen, EntidadDestino>(
        private val creadorEntidadDestino: Creable<EntidadDestino>,
        private val transformadorHaciaDestino: TransformadorEntidadCliente<EntidadOrigen, EntidadDestino>,
        private val transformadorHaciaOrigen: TransformadorEntidadCliente<EntidadDestino, EntidadOrigen>)
    : Creable<EntidadOrigen>
{
    override val nombreEntidad: String = creadorEntidadDestino.nombreEntidad

    override fun crear(idCliente: Long, entidadACrear: EntidadOrigen): EntidadOrigen
    {
        return transformadorHaciaOrigen.transformar(
                idCliente,
                creadorEntidadDestino.crear(
                        idCliente,
                        transformadorHaciaDestino.transformar(idCliente, entidadACrear)
                                           )
                                                   )
    }
}

internal class CreableSimple<EntidadDeNegocio, EntidadDao : EntidadDAO<EntidadDeNegocio>>(
        private val creableConTransformacion: CreableConTransformacion<EntidadDeNegocio, EntidadDao>)
    : Creable<EntidadDeNegocio> by creableConTransformacion
{
    constructor(
            creadorEntidadDAO: Creable<EntidadDao>,
            transformadorHaciaDao: TransformadorEntidadCliente<EntidadDeNegocio, EntidadDao>
               )
            : this(
            CreableConTransformacion(
                    creadorEntidadDAO,
                    transformadorHaciaDao,
                    TransformadorEntidadDao<EntidadDeNegocio, EntidadDao>()
                                    )
                  )
}

internal class CreableJerarquicoDAO<EntidadDao : JerarquicoDAO>(
        private val parametros: ParametrosParaDAOEntidadDeCliente<EntidadDao, Long>,
        private val creadorEntidadDAO: Creable<EntidadDao>)
    : Creable<EntidadDao>
{
    override val nombreEntidad: String = creadorEntidadDAO.nombreEntidad

    override fun crear(idCliente: Long, entidadACrear: EntidadDao): EntidadDao
    {
        val idsAncestros =
                if (entidadACrear.idDelPadre != null)
                {
                    val entidadPadre = parametros[idCliente].dao.queryForId(entidadACrear.idDelPadre)
                    entidadPadre?.darIdsAncestros()?.apply { add(entidadACrear.idDelPadre!!) }
                    ?: if (parametros.configuracion.llavesForaneasActivadas)
                    {
                        throw ErrorDeLlaveForanea(entidadACrear.idDelPadre, nombreEntidad)
                    }
                    else
                    {
                        entidadACrear.darIdsAncestros()
                    }
                }
                else
                {
                    LinkedHashSet()
                }
        val entidadDao = creadorEntidadDAO.crear(idCliente, entidadACrear)
        val entidadComoDao = entidadDao.apply { llaveJerarquia = idsAncestorsEIdALlave(idsAncestros, entidadDao.id) }
        val resultadoActualizacionLlave = parametros[idCliente].dao.update(entidadComoDao)
        if (resultadoActualizacionLlave != 1)
        {
            throw ErrorDeCreacionActualizacionEntidad(nombreEntidad)
        }
        return entidadComoDao
    }
}

// Se podria delegar a un CreableEnTransaccionSQLParaDiferenteSalida pero seria agregar otra capa de indirección, por ahora no creo que valga la pena
internal class CreableEnTransaccionSQL<EntidadDeNegocio>(
        private val configuracion: ConfiguracionRepositorios,
        private val creableSinTransaccion: Creable<EntidadDeNegocio>
                                                        ) : Creable<EntidadDeNegocio>
{
    override val nombreEntidad: String = creableSinTransaccion.nombreEntidad

    override fun crear(idCliente: Long, entidadACrear: EntidadDeNegocio): EntidadDeNegocio
    {
        return transaccionEnEsquemaClienteYProcesarErroresParaCreacion(
                configuracion,
                idCliente,
                nombreEntidad
                                                                      ) {
            creableSinTransaccion.crear(idCliente, entidadACrear)
        }
    }
}