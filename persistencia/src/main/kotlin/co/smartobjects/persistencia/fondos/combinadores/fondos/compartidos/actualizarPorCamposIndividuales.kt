package co.smartobjects.persistencia.fondos.combinadores.fondos.compartidos

import co.smartobjects.entidades.fondos.Dinero
import co.smartobjects.entidades.fondos.Fondo
import co.smartobjects.persistencia.CampoModificableEntidadDao
import co.smartobjects.persistencia.Transformador
import co.smartobjects.persistencia.TransformadorIdentidad
import co.smartobjects.persistencia.basederepositorios.*
import co.smartobjects.persistencia.excepciones.CampoActualizableDesconocido
import co.smartobjects.persistencia.fondos.FondoDAO


internal fun <TipoDeFondo : Fondo<*>> crearCombinadorActualizarFondoPorCamposIndividuales(
        parametrosDaoFondo: ParametrosParaDAOEntidadDeCliente<FondoDAO, Long>,
        nombreEntidad: String,
        filtrosIgualdad: List<FiltroIgualdad<*>>,
        transformadorCamposNegocioACamposDao: Transformador<CamposDeEntidad<TipoDeFondo>, CamposDeEntidadDAO<FondoDAO>>)
        : ActualizablePorCamposIndividualesEnTransaccionSQL<TipoDeFondo, Long>
{
    return ActualizablePorCamposIndividualesEnTransaccionSQL(
            parametrosDaoFondo.configuracion,
            ActualizablePorCamposIndividualesSimple
            (
                    ActualizablePorCamposIndividualesDaoConFiltrosIgualdad(nombreEntidad, parametrosDaoFondo, filtrosIgualdad),
                    TransformadorIdentidad(),
                    transformadorCamposNegocioACamposDao
            )
                                                            )
}

internal class TransformadorCamposNegocioACamposFondo<in TipoFondo : Fondo<*>> : Transformador<CamposDeEntidad<TipoFondo>, CamposDeEntidadDAO<FondoDAO>>
{
    override fun transformar(origen: CamposDeEntidad<TipoFondo>): CamposDeEntidadDAO<FondoDAO>
    {
        return origen.map {
            if (it.key == Fondo.Campos.DISPONIBLE_PARA_LA_VENTA)
            {
                FondoDAO.COLUMNA_DISPONIBLE_PARA_LA_VENTA to CampoModificableEntidadDao<FondoDAO, Any?>(it.value.valor, FondoDAO.COLUMNA_DISPONIBLE_PARA_LA_VENTA)
            }
            else
            {
                throw CampoActualizableDesconocido(it.key, Dinero.NOMBRE_ENTIDAD)
            }
        }.toMap()
    }
}