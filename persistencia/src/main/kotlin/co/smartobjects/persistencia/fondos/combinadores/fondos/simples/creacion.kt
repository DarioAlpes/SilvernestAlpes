package co.smartobjects.persistencia.fondos.combinadores.fondos.simples

import co.smartobjects.entidades.fondos.Dinero
import co.smartobjects.entidades.fondos.Fondo
import co.smartobjects.persistencia.TransformadorEntidadCliente
import co.smartobjects.persistencia.basederepositorios.CreableConTransformacion
import co.smartobjects.persistencia.basederepositorios.CreableDAO
import co.smartobjects.persistencia.basederepositorios.CreableEnTransaccionSQL
import co.smartobjects.persistencia.basederepositorios.ParametrosParaDAOEntidadDeCliente
import co.smartobjects.persistencia.fondos.FondoDAO

private fun <FondoSimple : Fondo<*>> crearTransformadorFondoSimpleARelacionesEnDao()
        : TransformadorEntidadCliente<FondoSimple, FondoDAO>
{
    return object : TransformadorEntidadCliente<FondoSimple, FondoDAO>
    {
        override fun transformar(idCliente: Long, origen: FondoSimple): FondoDAO
        {
            return FondoDAO(origen)
        }
    }
}

private fun <FondoSimple : Fondo<*>> crearTransformadorRelacionesEnDaoAFondoSimple()
        : TransformadorEntidadCliente<FondoDAO, FondoSimple>
{
    return object : TransformadorEntidadCliente<FondoDAO, FondoSimple>
    {
        override fun transformar(idCliente: Long, origen: FondoDAO): FondoSimple
        {
            @Suppress("UNCHECKED_CAST")
            return origen.aEntidadDeNegocio(idCliente) as FondoSimple
        }
    }
}

internal fun darCombinadorCreableParaFondoSimple(
        parametrosDaoFondo: ParametrosParaDAOEntidadDeCliente<FondoDAO, Long>,
        nombreEntidad: String
                                                )
        : CreableEnTransaccionSQL<Dinero>
{
    return CreableEnTransaccionSQL(
            parametrosDaoFondo.configuracion,
            CreableConTransformacion
            (
                    CreableDAO(parametrosDaoFondo, nombreEntidad),
                    crearTransformadorFondoSimpleARelacionesEnDao(),
                    crearTransformadorRelacionesEnDaoAFondoSimple()
            )
                                  )
}