package co.smartobjects.persistencia.basederepositorios

import co.smartobjects.persistencia.ConfiguracionRepositorios
import co.smartobjects.persistencia.Transformador
import co.smartobjects.persistencia.excepciones.ErrorDAO

interface ActualizablePorCamposIndividualesParaTabla<in Entidad, in TipoFiltros : ParametrosConsulta>
{
    val nombreEntidad: String

    @Throws(ErrorDAO::class)
    fun actualizarCamposIndividuales(idCliente: Long, camposAActualizar: CamposDeEntidad<Entidad>, filtros: TipoFiltros)
}

internal class ActualizablePorCamposIndividualesParaTablaDao<in EntidadNegocio, in EntidadDao, in TipoFiltros : ParametrosConsulta>(
        private val parametros: ParametrosParaDAOEntidadDeCliente<EntidadDao, *>,
        override val nombreEntidad: String,
        private val transformadorCampos: Transformador<CamposDeEntidad<EntidadNegocio>, CamposDeEntidadDAO<EntidadDao>>)
    : ActualizablePorCamposIndividualesParaTabla<EntidadNegocio, TipoFiltros>
{
    @Throws(ErrorDAO::class)
    override fun actualizarCamposIndividuales(idCliente: Long, camposAActualizar: CamposDeEntidad<EntidadNegocio>, filtros: TipoFiltros)
    {
        if (camposAActualizar.isNotEmpty())
        {
            val updateBuilder = this.parametros[idCliente].dao.updateBuilder().apply {
                if (filtros.filtrosSQL.isNotEmpty())
                {
                    val filtro = where()
                    filtros.filtrosSQL.forEach {
                        when (it)
                        {
                            is FiltroIgualdad<*> ->
                            {
                                if (it.valorColumnaUsoExterno == null)
                                {
                                    filtro.isNull(it.campo.nombreColumna)
                                }
                                else
                                {
                                    filtro.eq(it.campo.nombreColumna, it.valorColumnaUsoExterno)
                                }
                            }
                        }
                    }
                    filtro.and(filtros.filtrosSQL.size)
                }
            }

            transformadorCampos.transformar(camposAActualizar).forEach {
                updateBuilder.updateColumnValue(it.key, it.value.valor)
            }

            updateBuilder.update()
        }
    }
}

internal class ActualizablePorCamposIndividualesParaTablaEnTransaccionSQL<in EntidadNegocio, in TipoFiltros : ParametrosConsulta>(
        private val configuracion: ConfiguracionRepositorios,
        private val actualizableSinTransaccion: ActualizablePorCamposIndividualesParaTabla<EntidadNegocio, TipoFiltros>)
    : ActualizablePorCamposIndividualesParaTabla<EntidadNegocio, TipoFiltros>
{
    override val nombreEntidad: String = actualizableSinTransaccion.nombreEntidad

    override fun actualizarCamposIndividuales(idCliente: Long, camposAActualizar: CamposDeEntidad<EntidadNegocio>, filtros: TipoFiltros)
    {
        return transaccionEnEsquemaClienteYProcesarErroresParaActualizacion(configuracion, idCliente, nombreEntidad) {
            actualizableSinTransaccion.actualizarCamposIndividuales(idCliente, camposAActualizar, filtros)
        }
    }
}