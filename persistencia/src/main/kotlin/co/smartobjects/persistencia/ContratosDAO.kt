@file:Suppress("NOTHING_TO_INLINE")

package co.smartobjects.persistencia

import co.smartobjects.campos.CampoModificableEntidad


internal interface EntidadReferenciableDAO<out TipoId>
{
    val id: TipoId
}

internal interface EntidadDAO<out EntidadDeNegocio>
{
    fun aEntidadDeNegocio(idCliente: Long): EntidadDeNegocio
}

internal interface CampoProyeccion
{
    val nombreCompleto: String
}

internal data class CampoTabla(private val nombreTabla: String, internal val nombreColumna: String) : CampoProyeccion
{
    override val nombreCompleto = "$nombreTabla.$nombreColumna"
}

internal data class CampoOrdenamiento(private val campoTabla: CampoTabla, internal val orden: Orden) : CampoProyeccion
{
    override val nombreCompleto = "${campoTabla.nombreCompleto} $orden"

    enum class Orden
    {
        ASC, DESC
    }
}

internal class CampoContarTodos : CampoProyeccion
{
    override val nombreCompleto = "COUNT(*)"
}

internal class ProyeccionTodos(private val nombreTabla: String) : CampoProyeccion
{
    override val nombreCompleto = "$nombreTabla.*"
}

internal class ProyeccionMaximo(private val campoTabla: CampoTabla) : CampoProyeccion
{
    override val nombreCompleto = "MAX(${campoTabla.nombreCompleto})"
}

internal data class EntidadRelacionUnoAUno<out EntidadOrigen, out EntidadDestino>
(
        val entidadOrigen: EntidadOrigen,
        val entidadDestino: EntidadDestino
)

internal typealias EntidadRelacionUnoAMuchos<EntidadOrigen, EntidadDestino> = EntidadRelacionUnoAUno<EntidadOrigen, List<EntidadDestino>>

internal inline fun <T, S, U> shiftIzquierdaEntidadRelacionUnoAUno(): TransformadorEntidadCliente<EntidadRelacionUnoAUno<T, EntidadRelacionUnoAUno<S, U>>, EntidadRelacionUnoAUno<EntidadRelacionUnoAUno<T, S>, U>>
{
    return object : TransformadorEntidadCliente<EntidadRelacionUnoAUno<T, EntidadRelacionUnoAUno<S, U>>, EntidadRelacionUnoAUno<EntidadRelacionUnoAUno<T, S>, U>>
    {
        override fun transformar(idCliente: Long, origen: EntidadRelacionUnoAUno<T, EntidadRelacionUnoAUno<S, U>>): EntidadRelacionUnoAUno<EntidadRelacionUnoAUno<T, S>, U>
        {
            return EntidadRelacionUnoAUno(EntidadRelacionUnoAUno(origen.entidadOrigen, origen.entidadDestino.entidadOrigen), origen.entidadDestino.entidadDestino)
        }
    }
}

internal inline fun <T, S, U, W> shiftIzquierdaEntidadRelacionUnoAUnoAnidada(): TransformadorEntidadCliente<EntidadRelacionUnoAUno<T, EntidadRelacionUnoAUno<S, EntidadRelacionUnoAUno<U, W>>>, EntidadRelacionUnoAUno<EntidadRelacionUnoAUno<EntidadRelacionUnoAUno<T, S>, U>, W>>
{
    return object : TransformadorEntidadCliente<EntidadRelacionUnoAUno<T, EntidadRelacionUnoAUno<S, EntidadRelacionUnoAUno<U, W>>>, EntidadRelacionUnoAUno<EntidadRelacionUnoAUno<EntidadRelacionUnoAUno<T, S>, U>, W>>
    {
        override fun transformar(idCliente: Long, origen: EntidadRelacionUnoAUno<T, EntidadRelacionUnoAUno<S, EntidadRelacionUnoAUno<U, W>>>): EntidadRelacionUnoAUno<EntidadRelacionUnoAUno<EntidadRelacionUnoAUno<T, S>, U>, W>
        {
            return EntidadRelacionUnoAUno(EntidadRelacionUnoAUno(EntidadRelacionUnoAUno(origen.entidadOrigen, origen.entidadDestino.entidadOrigen), origen.entidadDestino.entidadDestino.entidadOrigen), origen.entidadDestino.entidadDestino.entidadDestino)
        }
    }
}

internal inline fun <T, S, U> shiftDerechaEntidadRelacionUnoAUno(): TransformadorEntidadCliente<EntidadRelacionUnoAUno<EntidadRelacionUnoAUno<T, S>, U>, EntidadRelacionUnoAUno<T, EntidadRelacionUnoAUno<S, U>>>
{
    return object : TransformadorEntidadCliente<EntidadRelacionUnoAUno<EntidadRelacionUnoAUno<T, S>, U>, EntidadRelacionUnoAUno<T, EntidadRelacionUnoAUno<S, U>>>
    {
        override fun transformar(idCliente: Long, origen: EntidadRelacionUnoAUno<EntidadRelacionUnoAUno<T, S>, U>): EntidadRelacionUnoAUno<T, EntidadRelacionUnoAUno<S, U>>
        {
            return EntidadRelacionUnoAUno(origen.entidadOrigen.entidadOrigen, EntidadRelacionUnoAUno(origen.entidadOrigen.entidadDestino, origen.entidadDestino))
        }
    }
}

internal inline fun <T, S, U, W> shiftDerechaEntidadRelacionUnoAUnoAnidada(): TransformadorEntidadCliente<EntidadRelacionUnoAUno<EntidadRelacionUnoAUno<EntidadRelacionUnoAUno<T, S>, U>, W>, EntidadRelacionUnoAUno<T, EntidadRelacionUnoAUno<S, EntidadRelacionUnoAUno<U, W>>>>
{
    return object : TransformadorEntidadCliente<EntidadRelacionUnoAUno<EntidadRelacionUnoAUno<EntidadRelacionUnoAUno<T, S>, U>, W>, EntidadRelacionUnoAUno<T, EntidadRelacionUnoAUno<S, EntidadRelacionUnoAUno<U, W>>>>
    {
        override fun transformar(idCliente: Long, origen: EntidadRelacionUnoAUno<EntidadRelacionUnoAUno<EntidadRelacionUnoAUno<T, S>, U>, W>): EntidadRelacionUnoAUno<T, EntidadRelacionUnoAUno<S, EntidadRelacionUnoAUno<U, W>>>
        {
            return EntidadRelacionUnoAUno(origen.entidadOrigen.entidadOrigen.entidadOrigen, EntidadRelacionUnoAUno(origen.entidadOrigen.entidadOrigen.entidadDestino, EntidadRelacionUnoAUno(origen.entidadOrigen.entidadDestino, origen.entidadDestino)))
        }
    }
}

internal interface ComparadorEntidades<in OrigenIzquierda, in OrigenDerecha>
{
    fun comparar(origenIzquierda: OrigenIzquierda, origenDerecha: OrigenDerecha): Int
}

internal interface Transformador<in Origen, out Destino>
{
    fun transformar(origen: Origen): Destino
}

internal class TransformadorIdentidad<Tipo> : Transformador<Tipo, Tipo>
{
    override fun transformar(origen: Tipo): Tipo
    {
        return origen
    }
}


internal interface TransformadorEntidadCliente<in Origen, out Destino>
{
    fun transformar(idCliente: Long, origen: Origen): Destino
}

internal class TransformadorIdentidadCliente<Tipo> : TransformadorEntidadCliente<Tipo, Tipo>
{
    override fun transformar(idCliente: Long, origen: Tipo): Tipo
    {
        return origen
    }
}

internal interface AsignadorParametro<Entidad, in Parametro>
{
    fun asignarParametro(entidad: Entidad, parametro: Parametro): Entidad
}

internal interface TransformadorEntidadesRelacionadas<in Origen, Destino>
{
    fun asignarCampoRelacionAEntidadDestino(entidadOrigen: Origen, entidadDestino: Destino): Destino
}

internal class TransformadorEntidadDao<out EntidadNegocio, in EntidadDao : EntidadDAO<EntidadNegocio>>
    : TransformadorEntidadCliente<EntidadDao, EntidadNegocio>
{
    override fun transformar(idCliente: Long, origen: EntidadDao): EntidadNegocio
    {
        return origen.aEntidadDeNegocio(idCliente)
    }
}

internal class TransformadorListaSegunTransformadorEntidad<out EntidadDestino, in EntidadOrigen>(
        private val transformadorEntidad: TransformadorEntidadCliente<EntidadOrigen, EntidadDestino>
                                                                                                ) : TransformadorEntidadCliente<List<EntidadOrigen>, List<EntidadDestino>>
{
    override fun transformar(idCliente: Long, origen: List<EntidadOrigen>): List<EntidadDestino>
    {
        return origen.map { transformadorEntidad.transformar(idCliente, it) }
    }
}

internal data class CampoModificableEntidadDao<out EntidadDao, out TipoCampo>(
        override val valor: TipoCampo,
        override val nombreCampo: String)
    : CampoModificableEntidad<EntidadDao, TipoCampo>