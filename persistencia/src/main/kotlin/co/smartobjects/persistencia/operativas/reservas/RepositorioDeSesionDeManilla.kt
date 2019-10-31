package co.smartobjects.persistencia.operativas.reservas

import co.smartobjects.entidades.operativas.reservas.SesionDeManilla
import co.smartobjects.persistencia.*
import co.smartobjects.persistencia.basederepositorios.*
import co.smartobjects.persistencia.excepciones.CampoActualizableDesconocido
import co.smartobjects.persistencia.excepciones.ErrorActualizacionViolacionDeRestriccion
import co.smartobjects.persistencia.fondos.combinadores.repositorioEntidadDao
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import com.j256.ormlite.field.SqlType
import org.threeten.bp.ZonedDateTime

interface RepositorioDeSesionDeManilla
    : CreadorRepositorio<SesionDeManilla>,
      Buscable<SesionDeManilla, Long>,
      ActualizablePorCamposIndividuales<SesionDeManilla, Long>


private class ActualizadorCamposIndividualesDeSesionDeManilla(
        private val actualizadorDao: ActualizablePorCamposIndividuales<SesionDeManillaDAO, Long>,
        private val transformadorCampos: Transformador<CamposDeEntidad<SesionDeManilla>, CamposDeEntidadDAO<SesionDeManillaDAO>>,
        private val parametrosDaoSesionDeManilla: ParametrosParaDAOEntidadDeCliente<SesionDeManillaDAO, Long>)
    : ActualizablePorCamposIndividuales<SesionDeManilla, Long>
{
    override val nombreEntidad: String = actualizadorDao.nombreEntidad

    override fun actualizarCamposIndividuales(idCliente: Long, id: Long, camposAActualizar: CamposDeEntidad<SesionDeManilla>)
    {
        val camposTransformados = transformadorCampos.transformar(camposAActualizar).toMutableMap()

        val uuidTagAAsignar = camposTransformados[SesionDeManillaDAO.COLUMNA_UUID_TAG]?.valor as ByteArray?
        val posibleFechaDeDesactivacion = camposTransformados[SesionDeManillaDAO.COLUMNA_FECHA_DESACTIVACION]?.valor as? ZonedDateTime

        if (uuidTagAAsignar != null || posibleFechaDeDesactivacion != null)
        {
            val sesionManillaAActualizar = parametrosDaoSesionDeManilla[idCliente].dao.queryForId(id)

            if (sesionManillaAActualizar != null)
            {
                if (uuidTagAAsignar != null)
                {
                    if (sesionManillaAActualizar.uuidTag != null)
                    {
                        throw ErrorActualizacionViolacionDeRestriccion(
                                SesionDeManilla.NOMBRE_ENTIDAD,
                                id.toString(),
                                "La sesión ya cuenta con un tag activo asignado",
                                arrayOf(uuidTagAAsignar.toString())
                                                                      )
                    }

                    val campoConFechaDeActivacion =
                            CampoModificableEntidadDao<SesionDeManillaDAO, Any?>(
                                    ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO),
                                    SesionDeManillaDAO.COLUMNA_FECHA_ACTIVACION
                                                                                )

                    camposTransformados[SesionDeManillaDAO.COLUMNA_FECHA_ACTIVACION] = campoConFechaDeActivacion
                }
                else if (posibleFechaDeDesactivacion != null)
                {
                    if (sesionManillaAActualizar.uuidTag == null)
                    {
                        throw ErrorActualizacionViolacionDeRestriccion(
                                SesionDeManilla.NOMBRE_ENTIDAD,
                                id.toString(),
                                "La sesión no ha sido activada todavía",
                                arrayOf("null")
                                                                      )
                    }
                    else if (sesionManillaAActualizar.fechaDesactivacion != null)
                    {
                        throw ErrorActualizacionViolacionDeRestriccion(
                                SesionDeManilla.NOMBRE_ENTIDAD,
                                id.toString(),
                                "La sesión ya había sido desactivada",
                                arrayOf(sesionManillaAActualizar.fechaDesactivacion.toString())
                                                                      )
                    }
                }
            }
        }

        actualizadorDao.actualizarCamposIndividuales(idCliente, id, camposTransformados)
    }
}

private val transformadorCamposNegocioACamposDaoSesionDeManilla =
        object : Transformador<CamposDeEntidad<SesionDeManilla>, CamposDeEntidadDAO<SesionDeManillaDAO>>
        {
            override fun transformar(origen: CamposDeEntidad<SesionDeManilla>): CamposDeEntidadDAO<SesionDeManillaDAO>
            {
                return origen.map {
                    when (it.key)
                    {
                        SesionDeManilla.Campos.UUID_TAG            ->
                        {
                            SesionDeManillaDAO.COLUMNA_UUID_TAG to CampoModificableEntidadDao<SesionDeManillaDAO, Any?>(it.value.valor, SesionDeManillaDAO.COLUMNA_UUID_TAG)
                        }
                        SesionDeManilla.Campos.FECHA_DESACTIVACION ->
                        {
                            SesionDeManillaDAO.COLUMNA_FECHA_DESACTIVACION to CampoModificableEntidadDao<SesionDeManillaDAO, Any?>(it.value.valor, SesionDeManillaDAO.COLUMNA_FECHA_DESACTIVACION)
                        }
                        else                                       ->
                        {
                            throw CampoActualizableDesconocido(it.key, SesionDeManilla.NOMBRE_ENTIDAD)
                        }
                    }
                }.toMap()
            }
        }

class RepositorioDeSesionDeManillaSQL private constructor(
        private val creadorRepositorio: CreadorRepositorio<SesionDeManilla>,
        private val buscador: Buscable<SesionDeManilla, Long>,
        private val actualizadorCamposIndividuales: ActualizablePorCamposIndividuales<SesionDeManilla, Long>)
    : CreadorRepositorio<SesionDeManilla> by creadorRepositorio,
      Buscable<SesionDeManilla, Long> by buscador,
      ActualizablePorCamposIndividuales<SesionDeManilla, Long> by actualizadorCamposIndividuales,
      RepositorioDeSesionDeManilla
{
    companion object
    {
        private val NOMBRE_ENTIDAD = SesionDeManilla.NOMBRE_ENTIDAD
    }

    override val nombreEntidad: String = NOMBRE_ENTIDAD

    private constructor(
            creadorRepositorio: CreadorRepositorio<SesionDeManilla>,
            listadorEntidad: ListableFiltrableOrdenable<SesionDeManilla>,
            actualizadorCamposIndividuales: ActualizablePorCamposIndividuales<SesionDeManilla, Long>
                       )
            :
            this(
                    creadorRepositorio,
                    BuscableSegunListableFiltrable(listadorEntidad, CampoTabla(SesionDeManillaDAO.TABLA, SesionDeManillaDAO.COLUMNA_ID), SqlType.LONG),
                    actualizadorCamposIndividuales
                )

    private constructor(
            parametrosDao: ParametrosParaDAOEntidadDeCliente<SesionDeManillaDAO, Long>,
            parametrosDaoCreditoEnSesionDeManilla: ParametrosParaDAOEntidadDeCliente<CreditoEnSesionDeManillaDAO, Long>
                       )
            :
            this(
                    CreadorUnicaVez
                    (
                            CreadorRepositorioSimple(parametrosDao, NOMBRE_ENTIDAD)
                    ),
                    ListableConTransformacion<EntidadRelacionUnoAMuchos<SesionDeManillaDAO, CreditoEnSesionDeManillaDAO>, SesionDeManilla>
                    (
                            ListableUnoAMuchos<SesionDeManillaDAO, CreditoEnSesionDeManillaDAO, Long>
                            (
                                    ListableInnerJoin<SesionDeManillaDAO, CreditoEnSesionDeManillaDAO>
                                    (
                                            repositorioEntidadDao(parametrosDao, SesionDeManillaDAO.COLUMNA_ID),
                                            repositorioEntidadDao(parametrosDaoCreditoEnSesionDeManilla, CreditoEnSesionDeManillaDAO.COLUMNA_ID)
                                    ),
                                    object : Transformador<SesionDeManillaDAO, Long>
                                    {
                                        override fun transformar(origen: SesionDeManillaDAO): Long
                                        {
                                            return origen.id!!
                                        }
                                    }
                            ),
                            object : TransformadorEntidadCliente<EntidadRelacionUnoAMuchos<SesionDeManillaDAO, CreditoEnSesionDeManillaDAO>, SesionDeManilla>
                            {
                                override fun transformar(idCliente: Long, origen: EntidadRelacionUnoAMuchos<SesionDeManillaDAO, CreditoEnSesionDeManillaDAO>): SesionDeManilla
                                {
                                    return origen.entidadOrigen.aEntidadDeNegocio(idCliente, origen.entidadDestino)
                                }
                            }
                    ),
                    ActualizablePorCamposIndividualesEnTransaccionSQL
                    (
                            parametrosDao.configuracion,
                            ActualizadorCamposIndividualesDeSesionDeManilla
                            (
                                    ActualizablePorCamposIndividualesDao(NOMBRE_ENTIDAD, parametrosDao),
                                    transformadorCamposNegocioACamposDaoSesionDeManilla,
                                    parametrosDao
                            )
                    )
                )

    constructor(configuracion: ConfiguracionRepositorios)
            : this(
            ParametrosParaDAOEntidadDeCliente(configuracion, SesionDeManillaDAO.TABLA, SesionDeManillaDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, CreditoEnSesionDeManillaDAO.TABLA, CreditoEnSesionDeManillaDAO::class.java)
                  )
}