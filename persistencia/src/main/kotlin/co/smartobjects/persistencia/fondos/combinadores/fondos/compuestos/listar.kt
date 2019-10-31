@file:Suppress("NOTHING_TO_INLINE")

package co.smartobjects.persistencia.fondos.combinadores.fondos.compuestos

import co.smartobjects.entidades.fondos.Fondo
import co.smartobjects.persistencia.EntidadRelacionUnoAUno
import co.smartobjects.persistencia.TransformadorEntidadCliente
import co.smartobjects.persistencia.basederepositorios.*
import co.smartobjects.persistencia.fondos.EntidadFondoDAO
import co.smartobjects.persistencia.fondos.FondoDAO
import co.smartobjects.persistencia.fondos.combinadores.fondos.compartidos.darListableFondoSegunTipoDeFondo
import co.smartobjects.persistencia.fondos.combinadores.repositorioEntidadDao

private typealias FondoCompuestoDAO<TipoDeFondoDAO> = EntidadRelacionUnoAUno<FondoDAO, TipoDeFondoDAO>

private inline fun <TipoDeFondoDAO> darCombinadorJoinFondoYFondoCompuesto(
        parametrosDaoFondo: ParametrosParaDAOEntidadDeCliente<FondoDAO, Long>,
        parametrosFondoEspecifico: ParametrosParaDAOEntidadDeCliente<TipoDeFondoDAO, Long>,
        filtroTipoDeFondo: FiltroIgualdad<FondoDAO.TipoDeFondoEnBD>)
        : ListableInnerJoin<FondoDAO, TipoDeFondoDAO>
{
    return ListableInnerJoin(
            darListableFondoSegunTipoDeFondo(parametrosDaoFondo, filtroTipoDeFondo),
            repositorioEntidadDao(parametrosFondoEspecifico, listOf())
                            )
}

private inline fun <TipoFondo, TipoDeFondoDAO : EntidadFondoDAO<TipoFondo>> transformadorResultadoDAOAFondo()
        : TransformadorEntidadCliente<FondoCompuestoDAO<TipoDeFondoDAO>, TipoFondo>
{
    return object : TransformadorEntidadCliente<FondoCompuestoDAO<TipoDeFondoDAO>, TipoFondo>
    {
        override fun transformar(idCliente: Long, origen: FondoCompuestoDAO<TipoDeFondoDAO>): TipoFondo
        {
            val fondoDao = origen.entidadOrigen
            val fondoEspecificoDao = origen.entidadDestino

            return fondoEspecificoDao.aEntidadDeNegocio(idCliente, fondoDao)
        }
    }
}

// En java 10 debería poderse quitar esto haciendo que TipoDeFondoDAO implemente EntidadFondoDAO de más de una entidad
private inline fun <TipoDeFondoIntermedio : Fondo<*>, TipoFondo : TipoDeFondoIntermedio, TipoDeFondoDAO : EntidadFondoDAO<TipoDeFondoIntermedio>>
        transformadorResultadoDAOAFondoConUnsafeCast()
        : TransformadorEntidadCliente<FondoCompuestoDAO<TipoDeFondoDAO>, TipoFondo>
{
    return object : TransformadorEntidadCliente<FondoCompuestoDAO<TipoDeFondoDAO>, TipoFondo>
    {
        private val transformadorSinCast: TransformadorEntidadCliente<FondoCompuestoDAO<TipoDeFondoDAO>, TipoDeFondoIntermedio> = transformadorResultadoDAOAFondo()

        override fun transformar(idCliente: Long, origen: FondoCompuestoDAO<TipoDeFondoDAO>): TipoFondo
        {
            @Suppress("UNCHECKED_CAST")
            return transformadorSinCast.transformar(idCliente, origen) as TipoFondo
        }
    }
}

private inline fun <TipoDeFondo : Fondo<*>, TipoDeFondoDAO> darCombinadorListableParaFondoCompuestoSegunTransformadorResultado(
        parametrosDaoFondo: ParametrosParaDAOEntidadDeCliente<FondoDAO, Long>,
        parametrosFondoEspecifico: ParametrosParaDAOEntidadDeCliente<TipoDeFondoDAO, Long>,
        nombreEntidad: String,
        filtroTipoDeFondo: FiltroIgualdad<FondoDAO.TipoDeFondoEnBD>,
        transformadorResultadoDAOAFondo: TransformadorEntidadCliente<FondoCompuestoDAO<TipoDeFondoDAO>, TipoDeFondo>)
        : ListableFiltrableOrdenable<TipoDeFondo>
{
    return ListableConTransaccion(
            parametrosDaoFondo.configuracion,
            nombreEntidad,
            ListableConTransformacion
            (
                    darCombinadorJoinFondoYFondoCompuesto(parametrosDaoFondo, parametrosFondoEspecifico, filtroTipoDeFondo),
                    transformadorResultadoDAOAFondo
            )
                                 )
}


internal inline fun <TipoDeFondo : Fondo<*>, TipoDeFondoDAO : EntidadFondoDAO<TipoDeFondo>> darCombinadorListableParaFondoCompuesto(
        parametrosDaoFondo: ParametrosParaDAOEntidadDeCliente<FondoDAO, Long>,
        parametrosFondoEspecifico: ParametrosParaDAOEntidadDeCliente<TipoDeFondoDAO, Long>,
        nombreEntidad: String,
        filtroTipoDeFondo: FiltroIgualdad<FondoDAO.TipoDeFondoEnBD>
                                                                                                                                   )
        : ListableFiltrableOrdenable<TipoDeFondo>
{
    return darCombinadorListableParaFondoCompuestoSegunTransformadorResultado(
            parametrosDaoFondo,
            parametrosFondoEspecifico,
            nombreEntidad,
            filtroTipoDeFondo,
            transformadorResultadoDAOAFondo()
                                                                             )
}

// En java 10 debería poderse quitar esto haciendo que TipoDeFondoDAO implemente EntidadFondoDAO de más de una entidad
internal inline fun <TipoDeFondoIntermedio : Fondo<*>, TipoDeFondo : TipoDeFondoIntermedio, TipoDeFondoDAO : EntidadFondoDAO<TipoDeFondoIntermedio>> darCombinadorListableParaFondoCompuestoConEntidadIntermedia(
        parametrosDaoFondo: ParametrosParaDAOEntidadDeCliente<FondoDAO, Long>,
        parametrosFondoEspecifico: ParametrosParaDAOEntidadDeCliente<TipoDeFondoDAO, Long>,
        nombreEntidad: String,
        filtroTipoDeFondo: FiltroIgualdad<FondoDAO.TipoDeFondoEnBD>
                                                                                                                                                                                                                )
        : ListableFiltrableOrdenable<TipoDeFondo>
{
    return darCombinadorListableParaFondoCompuestoSegunTransformadorResultado(
            parametrosDaoFondo,
            parametrosFondoEspecifico,
            nombreEntidad,
            filtroTipoDeFondo,
            transformadorResultadoDAOAFondoConUnsafeCast()
                                                                             )
}