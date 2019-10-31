package co.smartobjects.persistencia.fondos.combinadores.fondos.compuestos

import co.smartobjects.entidades.fondos.Fondo
import co.smartobjects.persistencia.EntidadRelacionUnoAUno
import co.smartobjects.persistencia.TransformadorEntidadCliente
import co.smartobjects.persistencia.TransformadorEntidadesRelacionadas
import co.smartobjects.persistencia.basederepositorios.*
import co.smartobjects.persistencia.fondos.EntidadFondoDAO
import co.smartobjects.persistencia.fondos.FondoDAO

private typealias TransformadorDeFondoCompuestoARelacionDao<FondoCompuesto, FondoCompuestoDao> =
        TransformadorEntidadCliente<FondoCompuesto, EntidadRelacionUnoAUno<FondoDAO, FondoCompuestoDao>>

private fun <FondoCompuesto : Fondo<*>, FondoCompuestoDao : EntidadFondoDAO<Fondo<*>>> crearTransformadorFondoCompuestoARelacionesEnDao(
        crearFondoCompuestoDao: (FondoCompuesto) -> FondoCompuestoDao)
        : TransformadorDeFondoCompuestoARelacionDao<FondoCompuesto, FondoCompuestoDao>
{
    return object : TransformadorDeFondoCompuestoARelacionDao<FondoCompuesto, FondoCompuestoDao>
    {
        override fun transformar(idCliente: Long, origen: FondoCompuesto): EntidadRelacionUnoAUno<FondoDAO, FondoCompuestoDao>
        {
            val fondoDAO = FondoDAO(origen)
            val fondoCompuestoDAO = crearFondoCompuestoDao(origen)

            return EntidadRelacionUnoAUno(fondoDAO, fondoCompuestoDAO)
        }
    }
}

private typealias TransformadorDeRelacionDaoAFondoCompuesto<FondoCompuesto, FondoCompuestoDao> =
        TransformadorEntidadCliente<EntidadRelacionUnoAUno<FondoDAO, FondoCompuestoDao>, FondoCompuesto>

private fun <FondoCompuesto : Fondo<*>, FondoCompuestoDao : EntidadFondoDAO<FondoCompuesto>> crearTransformadorRelacionesEnDaoAFondoCompuesto()
        : TransformadorDeRelacionDaoAFondoCompuesto<FondoCompuesto, FondoCompuestoDao>
{
    return object : TransformadorDeRelacionDaoAFondoCompuesto<FondoCompuesto, FondoCompuestoDao>
    {
        override fun transformar(idCliente: Long, origen: EntidadRelacionUnoAUno<FondoDAO, FondoCompuestoDao>): FondoCompuesto
        {
            val fondoDAO = origen.entidadOrigen
            val fondoEspecificoDAO = origen.entidadDestino

            return fondoEspecificoDAO.aEntidadDeNegocio(idCliente, fondoDAO)
        }
    }
}

// En java 10 debería poderse quitar esto haciendo que TipoDeFondoDAO implemente EntidadFondoDAO de más de una entidad
private fun <TipoDeFondoIntermedio : Fondo<*>, FondoCompuesto : TipoDeFondoIntermedio, FondoCompuestoDao : EntidadFondoDAO<TipoDeFondoIntermedio>>
        crearTransformadorRelacionesEnDaoAFondoCompuestoConUnsafeCast()
        : TransformadorDeRelacionDaoAFondoCompuesto<FondoCompuesto, FondoCompuestoDao>
{
    return object : TransformadorDeRelacionDaoAFondoCompuesto<FondoCompuesto, FondoCompuestoDao>
    {
        private val transformadorSinCast: TransformadorDeRelacionDaoAFondoCompuesto<TipoDeFondoIntermedio, FondoCompuestoDao> = crearTransformadorRelacionesEnDaoAFondoCompuesto()

        override fun transformar(idCliente: Long, origen: EntidadRelacionUnoAUno<FondoDAO, FondoCompuestoDao>): FondoCompuesto
        {
            @Suppress("UNCHECKED_CAST")
            return transformadorSinCast.transformar(idCliente, origen) as FondoCompuesto
        }
    }
}

private fun <FondoCompuesto : Fondo<*>, FondoCompuestoDao : EntidadFondoDAO<Fondo<*>>> darCombinadorCreableParaFondoCompuestoSegunTransformador(
        parametrosDaoFondo: ParametrosParaDAOEntidadDeCliente<FondoDAO, Long>,
        creableFondoCompuesto: CreableDAORelacionUnoAUno<FondoDAO, FondoCompuestoDao>,
        crearFondoCompuestoDao: (FondoCompuesto) -> FondoCompuestoDao,
        transformadorResultado: TransformadorDeRelacionDaoAFondoCompuesto<FondoCompuesto, FondoCompuestoDao>)
        : Creable<FondoCompuesto>
{
    return CreableEnTransaccionSQL(
            parametrosDaoFondo.configuracion,
            CreableConTransformacion
            (
                    creableFondoCompuesto,
                    crearTransformadorFondoCompuestoARelacionesEnDao(crearFondoCompuestoDao),
                    transformadorResultado
            )
                                  )
}

internal fun <FondoCompuesto : Fondo<*>, FondoCompuestoDao : EntidadFondoDAO<FondoCompuesto>> darCombinadorCreableParaFondoCompuesto(
        parametrosDaoFondo: ParametrosParaDAOEntidadDeCliente<FondoDAO, Long>,
        creableFondoCompuesto: CreableDAORelacionUnoAUno<FondoDAO, FondoCompuestoDao>,
        crearFondoCompuestoDao: (FondoCompuesto) -> FondoCompuestoDao)
        : Creable<FondoCompuesto>
{
    return darCombinadorCreableParaFondoCompuestoSegunTransformador(
            parametrosDaoFondo,
            creableFondoCompuesto,
            crearFondoCompuestoDao,
            crearTransformadorRelacionesEnDaoAFondoCompuesto()
                                                                   )
}

// En java 10 debería poderse quitar esto haciendo que TipoDeFondoDAO implemente EntidadFondoDAO de más de una entidad
internal fun <TipoDeFondoIntermedio : Fondo<*>, FondoCompuesto : TipoDeFondoIntermedio, FondoCompuestoDao : EntidadFondoDAO<TipoDeFondoIntermedio>> darCombinadorCreableParaFondoCompuestoConEntidadIntermedia(
        parametrosDaoFondo: ParametrosParaDAOEntidadDeCliente<FondoDAO, Long>,
        creableFondoCompuesto: CreableDAORelacionUnoAUno<FondoDAO, FondoCompuestoDao>,
        crearFondoCompuestoDao: (FondoCompuesto) -> FondoCompuestoDao)
        : Creable<FondoCompuesto>
{
    return darCombinadorCreableParaFondoCompuestoSegunTransformador(
            parametrosDaoFondo,
            creableFondoCompuesto,
            crearFondoCompuestoDao,
            crearTransformadorRelacionesEnDaoAFondoCompuestoConUnsafeCast()
                                                                   )
}


private fun <FondoCompuestoDAO : EntidadFondoDAO<Fondo<*>>> crearTransformadorParaAsignarFondoYIdAFondoCompuesto(
        actualizarFondoEIdDeFondoHijo: (FondoCompuestoDAO, FondoDAO, Long?) -> FondoCompuestoDAO)
        : TransformadorEntidadesRelacionadas<FondoDAO, FondoCompuestoDAO>
{
    return object : TransformadorEntidadesRelacionadas<FondoDAO, FondoCompuestoDAO>
    {
        override fun asignarCampoRelacionAEntidadDestino(entidadOrigen: FondoDAO, entidadDestino: FondoCompuestoDAO): FondoCompuestoDAO
        {
            return actualizarFondoEIdDeFondoHijo(entidadDestino, entidadOrigen, entidadOrigen.id)
        }
    }
}

internal fun <TipoDeFondoDAO : EntidadFondoDAO<Fondo<*>>> darCombinadorCreableFondoCompuesto(
        parametrosDaoFondo: ParametrosParaDAOEntidadDeCliente<FondoDAO, Long>,
        parametrosFondoEspecifico: ParametrosParaDAOEntidadDeCliente<TipoDeFondoDAO, Long>,
        nombreEntidad: String,
        actualizarFondoEIdDeFondoHijo: (TipoDeFondoDAO, FondoDAO, Long?) -> TipoDeFondoDAO)
        : CreableDAORelacionUnoAUno<FondoDAO, TipoDeFondoDAO> = darCombinadorCreableFondoCompuestoSegunCreadorFondoEspecifico(
        parametrosDaoFondo,
        CreableDAO(parametrosFondoEspecifico, nombreEntidad),
        nombreEntidad,
        actualizarFondoEIdDeFondoHijo
                                                                                                                             )


internal fun <TipoDeFondoDAO : EntidadFondoDAO<Fondo<*>>> darCombinadorCreableFondoCompuestoSegunCreadorFondoEspecifico(
        parametrosDaoFondo: ParametrosParaDAOEntidadDeCliente<FondoDAO, Long>,
        creadorFondoEspecifico: Creable<TipoDeFondoDAO>,
        nombreEntidad: String,
        actualizarFondoEIdDeFondoHijo: (TipoDeFondoDAO, FondoDAO, Long?) -> TipoDeFondoDAO)
        : CreableDAORelacionUnoAUno<FondoDAO, TipoDeFondoDAO>
{
    return CreableDAORelacionUnoAUno(
            CreableDAO(parametrosDaoFondo, nombreEntidad),
            creadorFondoEspecifico,
            crearTransformadorParaAsignarFondoYIdAFondoCompuesto(actualizarFondoEIdDeFondoHijo)
                                    )
}