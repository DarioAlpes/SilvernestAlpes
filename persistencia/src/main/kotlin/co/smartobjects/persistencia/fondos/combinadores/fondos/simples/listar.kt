@file:Suppress("NOTHING_TO_INLINE")

package co.smartobjects.persistencia.fondos.combinadores.fondos.simples

import co.smartobjects.entidades.fondos.Fondo
import co.smartobjects.persistencia.TransformadorEntidadCliente
import co.smartobjects.persistencia.basederepositorios.*
import co.smartobjects.persistencia.fondos.FondoDAO
import co.smartobjects.persistencia.fondos.combinadores.fondos.compartidos.darListableFondoSegunTipoDeFondo


internal inline fun <TipoDeFondo : Fondo<*>> darCombinadorListableParaFondoSimple(
        parametrosDaoFondo: ParametrosParaDAOEntidadDeCliente<FondoDAO, Long>,
        nombreEntidad: String,
        filtroTipoDeFondo: FiltroIgualdad<FondoDAO.TipoDeFondoEnBD>
                                                                                 )
        : ListableFiltrableOrdenable<TipoDeFondo>
{
    return ListableConTransaccion(
            parametrosDaoFondo.configuracion,
            nombreEntidad,
            ListableConTransformacion
            (
                    darListableFondoSegunTipoDeFondo(parametrosDaoFondo, filtroTipoDeFondo),
                    object : TransformadorEntidadCliente<FondoDAO, TipoDeFondo>
                    {
                        override fun transformar(idCliente: Long, origen: FondoDAO): TipoDeFondo
                        {
                            // En java 10 debería poderse quitar esto haciendo que TipoDeFondoDAO implemente EntidadFondoDAO de más de una entidad
                            @Suppress("UNCHECKED_CAST")
                            return origen.aEntidadDeNegocio(idCliente) as TipoDeFondo
                        }
                    }
            )
                                 )
}