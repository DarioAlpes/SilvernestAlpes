package co.smartobjects.persistencia.usuarios.roles

import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.entidades.usuarios.Rol
import co.smartobjects.persistencia.*
import co.smartobjects.persistencia.basederepositorios.*
import com.j256.ormlite.field.SqlType

interface RepositorioRoles
    : CreadorRepositorio<Rol>,
      Creable<Rol>,
      Listable<Rol>,
      Buscable<Rol, String>,
      Actualizable<Rol, String>,
      EliminablePorId<Rol, String>

private val extractorIdRolDao = object : Transformador<RolDAO, String?>
{
    override fun transformar(origen: RolDAO): String?
    {
        return origen.nombre
    }
}

private val transformadorDeRolARelacionUnoAMuchos = object
    : TransformadorEntidadCliente<Rol, EntidadRelacionUnoAMuchos<RolDAO, PermisoBackDAO>>
{
    override fun transformar(idCliente: Long, origen: Rol): EntidadRelacionUnoAMuchos<RolDAO, PermisoBackDAO>
    {
        return EntidadRelacionUnoAMuchos(transformadorRolARolDAO.transformar(origen), transformadorRolAPermisosBackDAO.transformar(origen))
    }
}

private val transformadorRolARolDAO = object : Transformador<Rol, RolDAO>
{
    override fun transformar(origen: Rol): RolDAO
    {
        return RolDAO(origen)
    }
}

private val asignadorRolDAOARol = object : AsignadorParametro<Rol, RolDAO>
{
    override fun asignarParametro(entidad: Rol, parametro: RolDAO): Rol
    {
        return entidad
    }
}

private val transformadorRolAPermisosBackDAO = object : Transformador<Rol, List<PermisoBackDAO>>
{
    override fun transformar(origen: Rol): List<PermisoBackDAO>
    {
        return origen.permisos.map { PermisoBackDAO(it as PermisoBack, origen.nombre) }
    }
}

private val asignadorIdClienteAPermisos = object : AsignadorParametro<Rol, Long>
{
    override fun asignarParametro(entidad: Rol, parametro: Long): Rol
    {
        return entidad.copiar(permisos = entidad.permisos.map { (it as PermisoBack).copiar(idCliente = parametro) }.toSet())
    }
}

private val asignadorPermisosDAOARol = object : AsignadorParametro<Rol, List<PermisoBackDAO>>
{
    override fun asignarParametro(entidad: Rol, parametro: List<PermisoBackDAO>): Rol
    {
        return entidad
    }
}

private val transformadorDeRelacionUnoAMuchosARol = object
    : TransformadorEntidadCliente<EntidadRelacionUnoAMuchos<RolDAO, PermisoBackDAO>, Rol>
{
    override fun transformar(idCliente: Long, origen: EntidadRelacionUnoAMuchos<RolDAO, PermisoBackDAO>): Rol
    {
        return origen.entidadOrigen.aEntidadDeNegocio(origen.entidadDestino.map { it.aEntidadDeNegocio(idCliente) }.toSet())
    }
}

private val transformadorAsignarRelacionRolDaoAPermisoDao = object
    : TransformadorEntidadesRelacionadas<RolDAO, PermisoBackDAO>
{
    override fun asignarCampoRelacionAEntidadDestino(entidadOrigen: RolDAO, entidadDestino: PermisoBackDAO): PermisoBackDAO
    {
        return entidadDestino.copy(rolDAO = entidadOrigen)
    }
}

class RepositorioRolesSQL private constructor
(
        private val creadorRepositorio: CreadorRepositorio<Rol>,
        private val creador: Creable<Rol>,
        private val listador: Listable<Rol>,
        private val buscador: Buscable<Rol, String>,
        private val actualizador: Actualizable<Rol, String>,
        private val eliminador: EliminablePorId<Rol, String>
) : CreadorRepositorio<Rol> by creadorRepositorio,
    Creable<Rol> by creador,
    Listable<Rol> by listador,
    Buscable<Rol, String> by buscador,
    Actualizable<Rol, String> by actualizador,
    EliminablePorId<Rol, String> by eliminador,
    RepositorioRoles
{
    companion object
    {
        private val NOMBRE_ENTIDAD = Rol.NOMBRE_ENTIDAD
    }

    override val nombreEntidad: String = NOMBRE_ENTIDAD

    private constructor(
            creador: Creable<Rol>,
            listador: ListableFiltrableOrdenable<Rol>,
            actualizador: Actualizable<Rol, String>,
            eliminador: EliminablePorId<Rol, String>,
            parametrosDAOPermisosBack: ParametrosParaDAOEntidadDeCliente<PermisoBackDAO, Long?>,
            parametrosRolDAO: ParametrosParaDAOEntidadDeCliente<RolDAO, String?>
                       )
            : this(
            CreadorUnicaVez
            (
                    CreadorRepositorioCompuesto
                    (
                            listOf(
                                    CreadorRepositorioDAO(parametrosRolDAO, NOMBRE_ENTIDAD),
                                    CreadorRepositorioDAO(parametrosDAOPermisosBack, NOMBRE_ENTIDAD)
                                  ),
                            NOMBRE_ENTIDAD
                    )
            ),
            CreableEnTransaccionSQL<Rol>(parametrosRolDAO.configuracion, creador),
            listador,
            BuscableSegunListableFiltrable(listador, CampoTabla(RolDAO.TABLA, RolDAO.COLUMNA_ID), SqlType.STRING),
            actualizador,
            eliminador
                  )

    private constructor(
            parametrosDAOPermisosBack: ParametrosParaDAOEntidadDeCliente<PermisoBackDAO, Long?>,
            parametrosRolDAO: ParametrosParaDAOEntidadDeCliente<RolDAO, String?>
                       )
            : this(
            CreableConTransformacion<Rol, EntidadRelacionUnoAMuchos<RolDAO, PermisoBackDAO>>
            (
                    CreableDAORelacionUnoAMuchos(
                            CreableDAO(parametrosRolDAO, NOMBRE_ENTIDAD),
                            CreableDAOMultiples(parametrosDAOPermisosBack, NOMBRE_ENTIDAD),
                            transformadorAsignarRelacionRolDaoAPermisoDao
                                                ),
                    transformadorDeRolARelacionUnoAMuchos,
                    transformadorDeRelacionUnoAMuchosARol
            ),
            ListableConTransformacion<EntidadRelacionUnoAMuchos<RolDAO, PermisoBackDAO>, Rol>
            (
                    ListableUnoAMuchos
                    (
                            ListableInnerJoin(
                                    ListableDAO(listOf(RolDAO.COLUMNA_ID), parametrosRolDAO),
                                    ListableDAO(listOf(PermisoBackDAO.COLUMNA_ID), parametrosDAOPermisosBack)
                                             ),
                            extractorIdRolDao
                    ),
                    transformadorDeRelacionUnoAMuchosARol
            ),
            ActualizableEnTransaccionSQL<Rol, String>
            (
                    parametrosRolDAO.configuracion,

                    ActualizableEntidadRelacionUnoAMuchosClonandoHijos
                    (
                            ActualizableConEntidadParcial<Rol, RolDAO, String, String>
                            (
                                    ActualizableDAO(parametrosRolDAO, NOMBRE_ENTIDAD),
                                    TransformadorIdentidad(),
                                    transformadorRolARolDAO,
                                    asignadorRolDAOARol,
                                    asignadorIdClienteAPermisos
                            ),
                            PermisoBackDAO.COLUMNA_ID_ROL,
                            ActualizableEntidadRelacionUnoAMuchosClonandoHijos.HijoClonable
                            (
                                    PermisoBackDAO.TABLA,
                                    EliminableDao(PermisoBack.NOMBRE_ENTIDAD, parametrosDAOPermisosBack),
                                    SqlType.STRING,
                                    CreableDAOMultiples(parametrosDAOPermisosBack, PermisoBack.NOMBRE_ENTIDAD),
                                    transformadorRolAPermisosBackDAO,
                                    asignadorPermisosDAOARol
                            )
                    )
            ),
            EliminablePorIdEnTransaccionSQL(
                    parametrosDAOPermisosBack.configuracion,
                    EliminableSimple(
                            EliminableDao(Rol.NOMBRE_ENTIDAD, parametrosRolDAO),
                            TransformadorIdentidad()
                                    )
                                           ),
            parametrosDAOPermisosBack,
            parametrosRolDAO
                  )

    constructor(configuracion: ConfiguracionRepositorios)
            : this(
            ParametrosParaDAOEntidadDeCliente(configuracion, PermisoBackDAO.TABLA, PermisoBackDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, RolDAO.TABLA, RolDAO::class.java)
                  )
}