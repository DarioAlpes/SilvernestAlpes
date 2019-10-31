package co.smartobjects.persistencia.fondos.combinadores.fondos.compuestos

import co.smartobjects.entidades.Jerarquico
import co.smartobjects.entidades.fondos.Fondo
import co.smartobjects.persistencia.AsignadorParametro
import co.smartobjects.persistencia.EntidadRelacionUnoAUno
import co.smartobjects.persistencia.JerarquicoDAO
import co.smartobjects.persistencia.Transformador
import co.smartobjects.persistencia.basederepositorios.*
import co.smartobjects.persistencia.fondos.EntidadFondoDAO
import co.smartobjects.persistencia.fondos.FondoDAO
import co.smartobjects.persistencia.fondos.combinadores.fondos.compartidos.crearAsignadorIdClienteAFondo


private fun <TipoDeFondoDAO : EntidadFondoDAO<Fondo<*>>> crearCombinadorActualizableDAOParaFondoCompuestoSegunRepositorioFondoEspecifico(
        nombreEntidad: String,
        parametrosDaoFondo: ParametrosParaDAOEntidadDeCliente<FondoDAO, Long>,
        actualizableDeFondoCompuesto: Actualizable<TipoDeFondoDAO, Long>,
        filtroTipoDeFondo: FiltroIgualdad<FondoDAO.TipoDeFondoEnBD>
                                                                                                                                        )
        : ActualizableEntidadCompuesta<FondoDAO, Long, TipoDeFondoDAO, Long>
{
    return ActualizableEntidadCompuesta(
            ActualizableDAO(parametrosDaoFondo, nombreEntidad).conFiltrosIgualdad(sequenceOf(filtroTipoDeFondo)),
            actualizableDeFondoCompuesto
                                       )
}

private fun <TipoDeFondoDAO : EntidadFondoDAO<Fondo<*>>> crearCombinadorActualizableDAOParaFondoCompuesto(
        nombreEntidad: String,
        parametrosDaoFondo: ParametrosParaDAOEntidadDeCliente<FondoDAO, Long>,
        parametrosFondoEspecifico: ParametrosParaDAOEntidadDeCliente<TipoDeFondoDAO, Long>,
        filtroTipoDeFondo: FiltroIgualdad<FondoDAO.TipoDeFondoEnBD>
                                                                                                         )
        : ActualizableEntidadCompuesta<FondoDAO, Long, TipoDeFondoDAO, Long>
{
    return crearCombinadorActualizableDAOParaFondoCompuestoSegunRepositorioFondoEspecifico(
            nombreEntidad,
            parametrosDaoFondo,
            ActualizableDAO(parametrosFondoEspecifico, nombreEntidad),
            filtroTipoDeFondo
                                                                                          )
}

private fun <TipoDeFondoDAO> crearCombinadorActualizableDAOParaFondoCompuestoJerarquico(
        nombreEntidad: String,
        parametrosDaoFondo: ParametrosParaDAOEntidadDeCliente<FondoDAO, Long>,
        parametrosFondoEspecifico: ParametrosParaDAOEntidadDeCliente<TipoDeFondoDAO, Long>,
        filtroTipoDeFondo: FiltroIgualdad<FondoDAO.TipoDeFondoEnBD>
                                                                                       )
        : ActualizableEntidadCompuesta<FondoDAO, Long, TipoDeFondoDAO, Long>
        where TipoDeFondoDAO : EntidadFondoDAO<Fondo<*>>,
              TipoDeFondoDAO : JerarquicoDAO
{
    return crearCombinadorActualizableDAOParaFondoCompuestoSegunRepositorioFondoEspecifico(
            nombreEntidad,
            parametrosDaoFondo,
            JerarquiaActualizableDAO(
                    parametrosFondoEspecifico,
                    ActualizableDAO(parametrosFondoEspecifico, nombreEntidad)
                                    ),
            filtroTipoDeFondo
                                                                                          )
}

private fun <FondoCompuesto : Fondo<*>, FondoCompuestoDao : EntidadFondoDAO<Fondo<*>>> crearAsignadorDeFondoYFondoCompuestoNoJerarquicoActualizadoAFondoFinal()
        : AsignadorParametro<FondoCompuesto, EntidadRelacionUnoAUno<FondoDAO, FondoCompuestoDao>>
{
    return object : AsignadorParametro<FondoCompuesto, EntidadRelacionUnoAUno<FondoDAO, FondoCompuestoDao>>
    {
        override fun asignarParametro(entidad: FondoCompuesto, parametro: EntidadRelacionUnoAUno<FondoDAO, FondoCompuestoDao>): FondoCompuesto
        {
            return entidad
        }
    }
}

private fun <TipoDeFondo, FondoCompuesto, FondoCompuestoDAO>
        crearAsignadorDeFondoYFondoCompuestoJerarquicoActualizadoAFondoFinal(
        actualizarIdsDeAncestros: (FondoCompuesto, LinkedHashSet<Long>) -> FondoCompuesto
                                                                            )
        : AsignadorParametro<FondoCompuesto, EntidadRelacionUnoAUno<FondoDAO, FondoCompuestoDAO>>
        where FondoCompuesto : Fondo<TipoDeFondo>,
              FondoCompuesto : Jerarquico<TipoDeFondo>,
              FondoCompuestoDAO : EntidadFondoDAO<Fondo<*>>,
              FondoCompuestoDAO : JerarquicoDAO
{
    return object : AsignadorParametro<FondoCompuesto, EntidadRelacionUnoAUno<FondoDAO, FondoCompuestoDAO>>
    {
        override fun asignarParametro(entidad: FondoCompuesto, parametro: EntidadRelacionUnoAUno<FondoDAO, FondoCompuestoDAO>): FondoCompuesto
        {
            return actualizarIdsDeAncestros(entidad, parametro.entidadDestino.darIdsAncestros())
        }
    }
}

private fun <FondoCompuesto : Fondo<*>, FondoCompuestoDao : EntidadFondoDAO<Fondo<*>>>
        crearTransformadorDeFondoCompuestoAFondoCompuestoDaoParaActualizacion(
        instanciarFondoCompuestoDAO: (FondoCompuesto) -> FondoCompuestoDao
                                                                             )
        : Transformador<FondoCompuesto, EntidadRelacionUnoAUno<FondoDAO, FondoCompuestoDao>>
{
    return object : Transformador<FondoCompuesto, EntidadRelacionUnoAUno<FondoDAO, FondoCompuestoDao>>
    {
        override fun transformar(origen: FondoCompuesto): EntidadRelacionUnoAUno<FondoDAO, FondoCompuestoDao>
        {
            return EntidadRelacionUnoAUno(FondoDAO(origen), instanciarFondoCompuestoDAO(origen))
        }
    }
}

private fun crearTransformadorDeIdFondoCompuestoAIdsDao(): Transformador<Long, EntidadRelacionUnoAUno<Long, Long>>
{
    return object : Transformador<Long, EntidadRelacionUnoAUno<Long, Long>>
    {
        override fun transformar(origen: Long): EntidadRelacionUnoAUno<Long, Long>
        {
            return EntidadRelacionUnoAUno(origen, origen)
        }
    }
}

internal fun <FondoCompuesto : Fondo<FondoCompuesto>, FondoCompuestoDAO : EntidadFondoDAO<Fondo<*>>> crearCombinadorDeActualizacionDeFondoCompuestoNoJerarquico(
        parametrosDaoFondo: ParametrosParaDAOEntidadDeCliente<FondoDAO, Long>,
        parametrosFondoEspecifico: ParametrosParaDAOEntidadDeCliente<FondoCompuestoDAO, Long>,
        nombreEntidad: String,
        filtroTipoDeFondo: FiltroIgualdad<FondoDAO.TipoDeFondoEnBD>,
        instanciarFondoDao: (FondoCompuesto) -> FondoCompuestoDAO)
        : ActualizableEnTransaccionSQL<FondoCompuesto, Long>
{
    return crearCombinadorDeActualizacionDeFondoCompuestoSegunRepositorioFondoCompuesto(
            crearCombinadorActualizableDAOParaFondoCompuesto(nombreEntidad, parametrosDaoFondo, parametrosFondoEspecifico, filtroTipoDeFondo),
            parametrosDaoFondo,
            instanciarFondoDao,
            crearAsignadorDeFondoYFondoCompuestoNoJerarquicoActualizadoAFondoFinal()
                                                                                       )
}

private fun <FondoCompuesto : Fondo<FondoCompuesto>, FondoCompuestoDAO : EntidadFondoDAO<Fondo<*>>> crearCombinadorDeActualizacionDeFondoCompuestoSegunRepositorioFondoCompuesto(
        repositorioFondoCompuesto: ActualizableEntidadCompuesta<FondoDAO, Long, FondoCompuestoDAO, Long>,
        parametrosFondoDAO: ParametrosParaDAOEntidadDeCliente<FondoDAO, Long>,
        instanciarFondoDao: (FondoCompuesto) -> FondoCompuestoDAO,
        asignadorIdsJerarquia: AsignadorParametro<FondoCompuesto, EntidadRelacionUnoAUno<FondoDAO, FondoCompuestoDAO>>)
        : ActualizableEnTransaccionSQL<FondoCompuesto, Long>
{
    val actualizadorDeFondo =
            ActualizableConEntidadParcial(
                    repositorioFondoCompuesto,
                    crearTransformadorDeIdFondoCompuestoAIdsDao(),
                    crearTransformadorDeFondoCompuestoAFondoCompuestoDaoParaActualizacion(instanciarFondoDao),
                    asignadorIdsJerarquia,
                    crearAsignadorIdClienteAFondo()
                                         )

    return ActualizableEnTransaccionSQL(
            parametrosFondoDAO.configuracion,
            actualizadorDeFondo
                                       )
}

internal fun <FondoCompuesto, FondoCompuestoDAO> crearCombinadorDeActualizacionDeFondoCompuestoJerarquico(
        parametrosDaoFondo: ParametrosParaDAOEntidadDeCliente<FondoDAO, Long>,
        parametrosFondoEspecifico: ParametrosParaDAOEntidadDeCliente<FondoCompuestoDAO, Long>,
        nombreEntidad: String,
        filtroTipoDeFondo: FiltroIgualdad<FondoDAO.TipoDeFondoEnBD>,
        instanciarFondoDao: (FondoCompuesto) -> FondoCompuestoDAO,
        actualizarIdsDeAncestros: (FondoCompuesto, LinkedHashSet<Long>) -> FondoCompuesto)
        : ActualizableEnTransaccionSQL<FondoCompuesto, Long>
        where FondoCompuesto : Fondo<FondoCompuesto>,
              FondoCompuesto : Jerarquico<FondoCompuesto>,
              FondoCompuestoDAO : EntidadFondoDAO<Fondo<*>>,
              FondoCompuestoDAO : JerarquicoDAO
{
    return crearCombinadorDeActualizacionDeFondoCompuestoSegunRepositorioFondoCompuesto(
            crearCombinadorActualizableDAOParaFondoCompuestoJerarquico(nombreEntidad, parametrosDaoFondo, parametrosFondoEspecifico, filtroTipoDeFondo),
            parametrosDaoFondo,
            instanciarFondoDao,
            crearAsignadorDeFondoYFondoCompuestoJerarquicoActualizadoAFondoFinal(actualizarIdsDeAncestros)
                                                                                       )
}