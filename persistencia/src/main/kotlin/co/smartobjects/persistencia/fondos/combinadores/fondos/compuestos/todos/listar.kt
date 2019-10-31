@file:Suppress("NOTHING_TO_INLINE")

package co.smartobjects.persistencia.fondos.combinadores.fondos.compuestos.todos

import co.smartobjects.entidades.fondos.Acceso
import co.smartobjects.entidades.fondos.Dinero
import co.smartobjects.entidades.fondos.Entrada
import co.smartobjects.entidades.fondos.Fondo
import co.smartobjects.persistencia.EntidadRelacionUnoAUno
import co.smartobjects.persistencia.Transformador
import co.smartobjects.persistencia.TransformadorEntidadCliente
import co.smartobjects.persistencia.basederepositorios.*
import co.smartobjects.persistencia.fondos.FondoDAO
import co.smartobjects.persistencia.fondos.acceso.AccesoDAO
import co.smartobjects.persistencia.fondos.categoriaskus.CategoriaSkuDAO
import co.smartobjects.persistencia.fondos.combinadores.repositorioEntidadDao
import co.smartobjects.persistencia.fondos.skus.SkuDAO

private typealias FondoConSkuDAO = EntidadRelacionUnoAUno<FondoDAO, SkuDAO?>
private typealias FondoConSkuYCategoriaSkuDAO = EntidadRelacionUnoAUno<FondoConSkuDAO, CategoriaSkuDAO?>
private typealias FondoCompletoDAO = EntidadRelacionUnoAUno<FondoConSkuYCategoriaSkuDAO, AccesoDAO?>

private val transformadorSkuDAOEsNull =
        object : Transformador<SkuDAO, Boolean>
        {
            override fun transformar(origen: SkuDAO): Boolean
            {
                return origen.id == null
            }
        }

private val transformadorCategoriaSkuDAOEsNull =
        object : Transformador<CategoriaSkuDAO, Boolean>
        {
            override fun transformar(origen: CategoriaSkuDAO): Boolean
            {
                return origen.id == null
            }
        }

private val transformadorAccesoDAOEsNull =
        object : Transformador<AccesoDAO, Boolean>
        {
            override fun transformar(origen: AccesoDAO): Boolean
            {
                return origen.id == null
            }
        }

private val extractorIdFondoDaoEnJoinAnidado =
        object : Transformador<FondoCompletoDAO, Long>
        {
            override fun transformar(origen: FondoCompletoDAO): Long
            {
                return origen.entidadOrigen.entidadOrigen.entidadOrigen.id!!
            }
        }

private inline fun <Parametros : ParametrosConsulta> darCombinadorJoinFondoCompletoConParametrosSegunRepositorioFondos(
        repositorioFondos: ListableSQL<FondoDAO>,
        parametrosDaoSku: ParametrosParaDAOEntidadDeCliente<SkuDAO, Long>,
        parametrosDaoCategoriaSku: ParametrosParaDAOEntidadDeCliente<CategoriaSkuDAO, Long>,
        parametrosDaoAcceso: ParametrosParaDAOEntidadDeCliente<AccesoDAO, Long>
                                                                                                                      )
        : ListableConParametrosSQL<FondoCompletoDAO, Parametros>
{
    return ListableConParametrosSegunListableSQL(
            ListableLeftJoin
            (
                    ListableLeftJoin
                    (
                            ListableLeftJoin
                            (
                                    repositorioFondos,
                                    repositorioEntidadDao(parametrosDaoSku, listOf()),
                                    transformadorSkuDAOEsNull
                            ),
                            repositorioEntidadDao(parametrosDaoCategoriaSku, listOf()),
                            transformadorCategoriaSkuDAOEsNull
                    ),
                    repositorioEntidadDao(parametrosDaoAcceso, listOf()),
                    transformadorAccesoDAOEsNull
            )
                                                )
}

private val transformadorResultadoDAOAFondo =
        object : TransformadorEntidadCliente<FondoCompletoDAO, Fondo<*>>
        {
            override fun transformar(idCliente: Long, origen: FondoCompletoDAO): Fondo<*>
            {
                val fondoDao = origen.entidadOrigen.entidadOrigen.entidadOrigen

                when (fondoDao.tipoDeFondo)
                {
                    FondoDAO.TipoDeFondoEnBD.CATEGORIA_SKU ->
                    {
                        val categoriaSkuDao = origen.entidadOrigen.entidadDestino!!
                        return categoriaSkuDao.aEntidadDeNegocio(idCliente, fondoDao)
                    }
                    FondoDAO.TipoDeFondoEnBD.DINERO        ->
                    {
                        return fondoDao.aEntidadDeNegocio(idCliente) as Dinero
                    }
                    FondoDAO.TipoDeFondoEnBD.SKU           ->
                    {
                        val skuDao = origen.entidadOrigen.entidadOrigen.entidadDestino!!
                        return skuDao.aEntidadDeNegocio(idCliente, fondoDao)
                    }
                    FondoDAO.TipoDeFondoEnBD.ACCESO        ->
                    {
                        val accesoDao = origen.entidadDestino!!
                        return accesoDao.aEntidadDeNegocio(idCliente, fondoDao) as Acceso
                    }
                    FondoDAO.TipoDeFondoEnBD.ENTRADA       ->
                    {
                        val accesoDao = origen.entidadDestino!!
                        return accesoDao.aEntidadDeNegocio(idCliente, fondoDao) as Entrada
                    }
                    FondoDAO.TipoDeFondoEnBD.DESCONOCIDO   -> throw IllegalStateException("Estado en base de datos inconsistente")
                }
            }
        }


internal inline fun darCombinadorListableParaFondoCompleto(
        parametrosDaoFondo: ParametrosParaDAOEntidadDeCliente<FondoDAO, Long>,
        parametrosDaoSku: ParametrosParaDAOEntidadDeCliente<SkuDAO, Long>,
        parametrosDaoCategoriaSku: ParametrosParaDAOEntidadDeCliente<CategoriaSkuDAO, Long>,
        parametrosDaoAcceso: ParametrosParaDAOEntidadDeCliente<AccesoDAO, Long>,
        nombreEntidad: String
                                                          )
        : ListableFiltrableOrdenable<Fondo<*>>
{
    return ListableConTransaccion(
            parametrosDaoFondo.configuracion,
            nombreEntidad,
            ListableConTransformacion
            (
                    ListableSegunListableConParametrosUnit
                    (
                            darCombinadorJoinFondoCompletoConParametrosSegunRepositorioFondos(repositorioEntidadDao(parametrosDaoFondo, listOf()), parametrosDaoSku, parametrosDaoCategoriaSku, parametrosDaoAcceso)
                    ),
                    transformadorResultadoDAOAFondo
            )
                                 )
}


internal inline fun <TipoParametro : ParametrosConsulta> darCombinadorListableParaFondoCompletoConParametros(
        repositorioFondos: ListableSQL<FondoDAO>,
        parametrosDaoSku: ParametrosParaDAOEntidadDeCliente<SkuDAO, Long>,
        parametrosDaoCategoriaSku: ParametrosParaDAOEntidadDeCliente<CategoriaSkuDAO, Long>,
        parametrosDaoAcceso: ParametrosParaDAOEntidadDeCliente<AccesoDAO, Long>,
        nombreEntidad: String
                                                                                                            )
        : ListableConParametrosFiltrableOrdenable<Fondo<*>, TipoParametro>
{
    return ListableConTransaccionYParametros(
            parametrosDaoSku.configuracion,
            nombreEntidad,
            ListableConTransformacionYParametros(
                    darCombinadorJoinFondoCompletoConParametrosSegunRepositorioFondos(repositorioFondos, parametrosDaoSku, parametrosDaoCategoriaSku, parametrosDaoAcceso),
                    transformadorResultadoDAOAFondo
                                                )
                                            )
}