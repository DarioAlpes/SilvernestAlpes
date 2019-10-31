package co.smartobjects.persistencia.fondos.combinadores.fondos.simples

import co.smartobjects.entidades.fondos.Fondo
import co.smartobjects.persistencia.AsignadorParametro
import co.smartobjects.persistencia.Transformador
import co.smartobjects.persistencia.TransformadorIdentidad
import co.smartobjects.persistencia.basederepositorios.*
import co.smartobjects.persistencia.fondos.FondoDAO
import co.smartobjects.persistencia.fondos.combinadores.fondos.compartidos.crearAsignadorIdClienteAFondo


private fun <FondoSimple : Fondo<*>> crearAsignadorDeFondoYFondoSimpleActualizadoAFondoFinal()
        : AsignadorParametro<FondoSimple, FondoDAO>
{
    return object : AsignadorParametro<FondoSimple, FondoDAO>
    {
        override fun asignarParametro(entidad: FondoSimple, parametro: FondoDAO): FondoSimple
        {
            return entidad
        }
    }
}


private fun <FondoCompuesto : Fondo<*>> crearTransformadorDeFondoSimpleAFondoSimpleDaoParaActualizacion()
        : Transformador<FondoCompuesto, FondoDAO>
{
    return object : Transformador<FondoCompuesto, FondoDAO>
    {
        override fun transformar(origen: FondoCompuesto): FondoDAO
        {
            return FondoDAO(origen)
        }
    }
}

internal fun <FondoSimple : Fondo<FondoSimple>> crearCombinadorDeActualizacionDeFondoSimple(
        parametrosDaoFondo: ParametrosParaDAOEntidadDeCliente<FondoDAO, Long>,
        nombreEntidad: String,
        filtroTipoDeFondo: FiltroIgualdad<FondoDAO.TipoDeFondoEnBD>
                                                                                           )
        : ActualizableEnTransaccionSQL<FondoSimple, Long>
{
    return ActualizableEnTransaccionSQL(
            parametrosDaoFondo.configuracion,
            ActualizableConEntidadParcial(
                    ActualizableDAO(parametrosDaoFondo, nombreEntidad).conFiltrosIgualdad(sequenceOf(filtroTipoDeFondo)),
                    TransformadorIdentidad(),
                    crearTransformadorDeFondoSimpleAFondoSimpleDaoParaActualizacion(),
                    crearAsignadorDeFondoYFondoSimpleActualizadoAFondoFinal(),
                    crearAsignadorIdClienteAFondo()
                                         )
                                       )
}