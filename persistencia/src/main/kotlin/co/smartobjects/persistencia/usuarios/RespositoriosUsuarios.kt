package co.smartobjects.persistencia.usuarios

import co.smartobjects.entidades.usuarios.Rol
import co.smartobjects.entidades.usuarios.Usuario
import co.smartobjects.persistencia.*
import co.smartobjects.persistencia.basederepositorios.*
import co.smartobjects.persistencia.excepciones.CampoActualizableDesconocido
import co.smartobjects.persistencia.usuarios.roles.PermisoBackDAO
import co.smartobjects.persistencia.usuarios.roles.RolDAO
import com.j256.ormlite.field.SqlType

private typealias UsuarioConUnRol = EntidadRelacionUnoAUno<EntidadRelacionUnoAUno<UsuarioDAO, RolUsuarioDAO>, RolDAO>
private typealias RolUsuarioConPermisos = EntidadRelacionUnoAUno<RolUsuarioDAO, EntidadRelacionUnoAMuchos<RolDAO, PermisoBackDAO>>
private typealias UsuarioEnBD = EntidadRelacionUnoAMuchos<UsuarioDAO, RolUsuarioConPermisos>

private fun darTansformadorCampoContraseñaACampoContraseñaDaoSegunHasher(hasher: Hasher): Transformador<CamposDeEntidad<Usuario>, CamposDeEntidadDAO<UsuarioDAO>>
{
    return object : Transformador<CamposDeEntidad<Usuario>, CamposDeEntidadDAO<UsuarioDAO>>
    {
        override fun transformar(origen: CamposDeEntidad<Usuario>): CamposDeEntidadDAO<UsuarioDAO>
        {
            return origen.map {
                if (it.key == Usuario.Campos.CONTRASEÑA)
                {
                    val campo = it.value as Usuario.CredencialesUsuario.CampoContraseña
                    val hash = hasher.calcularHash(campo.valor)
                    campo.limpiarValor()
                    UsuarioDAO.COLUMNA_HASH_CONTRASEÑA to CampoModificableEntidadDao<UsuarioDAO, Any?>(hash, UsuarioDAO.COLUMNA_HASH_CONTRASEÑA)
                }
                else if (it.key == Usuario.Campos.ACTIVO)
                {
                    UsuarioDAO.COLUMNA_ACTIVO to CampoModificableEntidadDao(it.value.valor, UsuarioDAO.COLUMNA_ACTIVO)
                }
                else
                {
                    throw CampoActualizableDesconocido(it.key, Usuario.NOMBRE_ENTIDAD)
                }
            }.toMap()
        }
    }
}

// En java 10 se pueden combinar estos dos respositorios en uno solo
interface RepositorioUsuarios
    : CreadorRepositorio<Usuario>,
      CreableConDiferenteSalida<Usuario.UsuarioParaCreacion, Usuario>,
      Listable<Usuario>,
      Buscable<Usuario, String>,
      ActualizablePorCamposIndividuales<Usuario, String>,
      ActualizableConDiferenteSalida<Usuario.UsuarioParaCreacion, Usuario, String>,
      EliminablePorId<Usuario, String>

interface RepositorioCredencialesGuardadasUsuario
    : Buscable<Usuario.CredencialesGuardadas, String>

private fun crearTransformadorUsuarioAUsuarioDAOSegunHasher(hasherContraseña: Hasher, creando: Boolean)
        : Transformador<Usuario.UsuarioParaCreacion, UsuarioDAO>
{
    return object : Transformador<Usuario.UsuarioParaCreacion, UsuarioDAO>
    {
        override fun transformar(origen: Usuario.UsuarioParaCreacion): UsuarioDAO
        {
            val usuarioDao = UsuarioDAO(origen, hasherContraseña.calcularHash(origen.contraseña), creando)
            origen.limpiarContraseña()
            return usuarioDao
        }
    }
}

private val transformadorUsuarioARolesUsuarioDAO = object : TransformadorEntidadCliente<Usuario.UsuarioParaCreacion, List<RolUsuarioDAO>>
{
    override fun transformar(idCliente: Long, origen: Usuario.UsuarioParaCreacion): List<RolUsuarioDAO>
    {
        return origen.rolesParaCreacion.map { RolUsuarioDAO(origen.datosUsuario.usuario, it) }
    }
}

private val asignadorUsuarioDAOAUsuarioParaCreacion = object : AsignadorParametro<Usuario.UsuarioParaCreacion, UsuarioDAO>
{
    override fun asignarParametro(entidad: Usuario.UsuarioParaCreacion, parametro: UsuarioDAO): Usuario.UsuarioParaCreacion
    {
        return entidad
    }
}

private val transformadorUsuarioParaCreacionARolUsuarioDAO = object : Transformador<Usuario.UsuarioParaCreacion, List<RolUsuarioDAO>>
{
    override fun transformar(origen: Usuario.UsuarioParaCreacion): List<RolUsuarioDAO>
    {
        return origen.rolesParaCreacion.map { RolUsuarioDAO(origen.datosUsuario.usuario, it) }
    }
}

private val asignadorRolesUsuarioDAOAUsuarioParaCreacion = object : AsignadorParametro<Usuario.UsuarioParaCreacion, List<RolUsuarioDAO>>
{
    override fun asignarParametro(entidad: Usuario.UsuarioParaCreacion, parametro: List<RolUsuarioDAO>): Usuario.UsuarioParaCreacion
    {
        return entidad
    }
}

private val extractorLlaveUsuarioConUnRol = object : Transformador<UsuarioConUnRol, EntidadRelacionUnoAUno<String, String>>
{
    override fun transformar(origen: UsuarioConUnRol): EntidadRelacionUnoAUno<String, String>
    {
        return EntidadRelacionUnoAUno(origen.entidadOrigen.entidadOrigen.usuario, origen.entidadDestino.nombre)
    }
}

private val extractorLlaveUsuario = object : Transformador<UsuarioDAO, String>
{
    override fun transformar(origen: UsuarioDAO): String
    {
        return origen.usuario
    }
}

private val extractorLlaveUsuarioParaCreacion = object : Transformador<Usuario.UsuarioParaCreacion, String>
{
    override fun transformar(origen: Usuario.UsuarioParaCreacion): String
    {
        return origen.datosUsuario.usuario
    }
}

private val transformadorUsuarioBDAUsuarioNegocio = object : TransformadorEntidadCliente<UsuarioEnBD, Usuario>
{
    override fun transformar(idCliente: Long, origen: UsuarioEnBD): Usuario
    {
        val roles = origen.entidadDestino.map {
            val rol = it.entidadDestino.entidadOrigen
            rol.aEntidadDeNegocio(it.entidadDestino.entidadDestino.map { it.aEntidadDeNegocio(idCliente) }.toSet())
        }
        return origen.entidadOrigen.aEntidadDeNegocio(idCliente, roles.toSet())
    }

}

private val transformadorUsuarioBDACredencialesUsuario = object : TransformadorEntidadCliente<UsuarioEnBD, Usuario.CredencialesGuardadas>
{
    override fun transformar(idCliente: Long, origen: UsuarioEnBD): Usuario.CredencialesGuardadas
    {
        val roles = origen.entidadDestino.map {
            val rol = it.entidadDestino.entidadOrigen
            rol.aEntidadDeNegocio(it.entidadDestino.entidadDestino.map { it.aEntidadDeNegocio(idCliente) }.toSet())
        }
        return Usuario.CredencialesGuardadas(origen.entidadOrigen.aEntidadDeNegocio(idCliente, roles.toSet()), origen.entidadOrigen.hashContraseña)
    }
}

private val asignarUsuarioARolDAO =
        object : TransformadorEntidadesRelacionadas<UsuarioDAO, RolUsuarioDAO>
        {
            override fun asignarCampoRelacionAEntidadDestino(
                    entidadOrigen: UsuarioDAO,
                    entidadDestino: RolUsuarioDAO
                                                            )
                    : RolUsuarioDAO
            {
                return entidadDestino.copy(usuarioDAO = entidadOrigen)
            }
        }

private fun crearTransformadorUsuarioAUsuarioConPermisosDAOSegunHasher(hasherContraseña: Hasher, creando: Boolean)
        : TransformadorEntidadCliente<Usuario.UsuarioParaCreacion, EntidadRelacionUnoAMuchos<UsuarioDAO, RolUsuarioDAO>>
{
    return object : TransformadorEntidadCliente<Usuario.UsuarioParaCreacion, EntidadRelacionUnoAMuchos<UsuarioDAO, RolUsuarioDAO>>
    {
        val transformadorAUsuarioDao = crearTransformadorUsuarioAUsuarioDAOSegunHasher(hasherContraseña, creando)
        override fun transformar(idCliente: Long, origen: Usuario.UsuarioParaCreacion): EntidadRelacionUnoAMuchos<UsuarioDAO, RolUsuarioDAO>
        {
            return EntidadRelacionUnoAUno(
                    transformadorAUsuarioDao.transformar(origen),
                    transformadorUsuarioARolesUsuarioDAO.transformar(idCliente, origen)
                                         )
        }
    }
}

private fun darListableUsuarioBD(
        parametrosDAOUsuario: ParametrosParaDAOEntidadDeCliente<UsuarioDAO, String?>,
        parametrosRolUsuarioDAO: ParametrosParaDAOEntidadDeCliente<RolUsuarioDAO, Long?>,
        parametrosDAOPermisosBack: ParametrosParaDAOEntidadDeCliente<PermisoBackDAO, Long?>,
        parametrosRolDAO: ParametrosParaDAOEntidadDeCliente<RolDAO, String?>,
        listarSoloActivos: Boolean
                                ): ListableFiltrableOrdenable<UsuarioEnBD>
{
    return ListableUnoAMuchos(
            ListableConTransformacion
            (
                    ListableUnoAMuchos
                    (
                            ListableConTransformacion
                            (
                                    ListableInnerJoin
                                    (
                                            ListableSQLConFiltrosSQL
                                            (
                                                    ListableDAO(listOf(UsuarioDAO.COLUMNA_ID), parametrosDAOUsuario),
                                                    if (listarSoloActivos)
                                                    {
                                                        listOf(FiltroCampoBooleano(CampoTabla(UsuarioDAO.TABLA, UsuarioDAO.COLUMNA_ACTIVO), true))
                                                    }
                                                    else
                                                    {
                                                        listOf()
                                                    }
                                            ),
                                            ListableInnerJoin
                                            (
                                                    ListableDAO(listOf(RolUsuarioDAO.COLUMNA_ID), parametrosRolUsuarioDAO),
                                                    ListableInnerJoin
                                                    (
                                                            ListableDAO(listOf(RolDAO.COLUMNA_ID), parametrosRolDAO),
                                                            ListableDAO(listOf(PermisoBackDAO.COLUMNA_ID), parametrosDAOPermisosBack)
                                                    )
                                            )
                                    ),
                                    shiftIzquierdaEntidadRelacionUnoAUnoAnidada()
                            ),
                            extractorLlaveUsuarioConUnRol
                    ),
                    shiftDerechaEntidadRelacionUnoAUnoAnidada()
            ),
            extractorLlaveUsuario
                             )
}

interface Hasher
{
    fun calcularHash(entrada: CharArray): String
}

class RepositorioUsuariosSQL private constructor(
        private val creadorRepositorio: CreadorRepositorio<Usuario>,
        private val creador: CreableConDiferenteSalida<Usuario.UsuarioParaCreacion, Usuario>,
        private val listador: Listable<Usuario>,
        private val buscador: Buscable<Usuario, String>,
        private val actualizador: ActualizableConDiferenteSalida<Usuario.UsuarioParaCreacion, Usuario, String>,
        private val actualizadorCamposIndividuales: ActualizablePorCamposIndividuales<Usuario, String>,
        private val eliminador: EliminablePorId<Usuario, String>)
    : CreadorRepositorio<Usuario> by creadorRepositorio,
      CreableConDiferenteSalida<Usuario.UsuarioParaCreacion, Usuario> by creador,
      Listable<Usuario> by listador,
      Buscable<Usuario, String> by buscador,
      ActualizableConDiferenteSalida<Usuario.UsuarioParaCreacion, Usuario, String> by actualizador,
      ActualizablePorCamposIndividuales<Usuario, String> by actualizadorCamposIndividuales,
      EliminablePorId<Usuario, String> by eliminador,
      RepositorioUsuarios
{
    companion object
    {
        private val NOMBRE_ENTIDAD = Usuario.NOMBRE_ENTIDAD
    }

    override val nombreEntidad: String = NOMBRE_ENTIDAD

    private constructor(
            creador: CreableConDiferenteSalida<Usuario.UsuarioParaCreacion, Usuario>,
            listador: ListableFiltrableOrdenable<Usuario>,
            buscador: Buscable<Usuario, String>,
            actualizador: ActualizableConDiferenteSalida<Usuario.UsuarioParaCreacion, Usuario, String>,
            actualizadorCamposIndividuales: ActualizablePorCamposIndividuales<Usuario, String>,
            eliminador: EliminablePorId<Usuario, String>,
            parametrosDAOUsuario: ParametrosParaDAOEntidadDeCliente<UsuarioDAO, String?>,
            parametrosRolUsuarioDAO: ParametrosParaDAOEntidadDeCliente<RolUsuarioDAO, Long?>
                       )
            : this(
            CreadorUnicaVez
            (
                    CreadorRepositorioCompuesto
                    (
                            listOf(
                                    CreadorRepositorioDAO(parametrosDAOUsuario, NOMBRE_ENTIDAD),
                                    CreadorRepositorioDAO(parametrosRolUsuarioDAO, NOMBRE_ENTIDAD)
                                  ),
                            NOMBRE_ENTIDAD
                    )
            ),
            CreableEnTransaccionSQLParaDiferenteSalida<Usuario.UsuarioParaCreacion, Usuario>
            (
                    parametrosDAOUsuario.configuracion,
                    creador
            ),
            listador,
            buscador,
            actualizador,
            actualizadorCamposIndividuales,
            eliminador
                  )

    private constructor(
            listador: ListableFiltrableOrdenable<Usuario>,
            buscador: Buscable<Usuario, String>,
            actualizadorCamposIndividuales: ActualizablePorCamposIndividuales<Usuario, String>,
            eliminador: EliminablePorId<Usuario, String>,
            parametrosDAOUsuario: ParametrosParaDAOEntidadDeCliente<UsuarioDAO, String?>,
            parametrosRolUsuarioDAO: ParametrosParaDAOEntidadDeCliente<RolUsuarioDAO, Long?>,
            hasherContraseña: Hasher
                       )
            : this(
            CreableConDiferenteSalidaSegunActualizableYBuscable<Usuario.UsuarioParaCreacion, Usuario, String>
            (
                    CreableConTransformacionRetornandoEntidadOriginal
                    (
                            CreableDAORelacionUnoAMuchos
                            (
                                    CreableDAO(parametrosDAOUsuario, NOMBRE_ENTIDAD),
                                    CreableDAOMultiples(parametrosRolUsuarioDAO, NOMBRE_ENTIDAD),
                                    asignarUsuarioARolDAO
                            ),
                            crearTransformadorUsuarioAUsuarioConPermisosDAOSegunHasher(hasherContraseña, true)
                    ),
                    buscador,
                    extractorLlaveUsuarioParaCreacion
            ),
            listador,
            buscador,
            ActualizableEnTransaccionSQLParaDiferenteSalida<Usuario.UsuarioParaCreacion, Usuario, String>
            (
                    parametrosDAOUsuario.configuracion,
                    ActualizableConDiferenteSalidaSegunActualizableYBuscable<Usuario.UsuarioParaCreacion, Usuario, String>
                    (
                            ActualizableEntidadRelacionUnoAMuchosClonandoHijos
                            (
                                    ActualizableConEntidadParcial<Usuario.UsuarioParaCreacion, UsuarioDAO, String, String>
                                    (
                                            ActualizableDAO(parametrosDAOUsuario, NOMBRE_ENTIDAD),
                                            TransformadorIdentidad(),
                                            crearTransformadorUsuarioAUsuarioDAOSegunHasher(hasherContraseña, false),
                                            asignadorUsuarioDAOAUsuarioParaCreacion,
                                            object : AsignadorParametro<Usuario.UsuarioParaCreacion, Long>
                                            {
                                                override fun asignarParametro(entidad: Usuario.UsuarioParaCreacion, parametro: Long): Usuario.UsuarioParaCreacion
                                                {
                                                    return entidad.copiar(datosUsuario = entidad.datosUsuario.copiar(idCliente = parametro))
                                                }
                                            }
                                    ),
                                    RolUsuarioDAO.COLUMNA_ID_USUARIO,
                                    ActualizableEntidadRelacionUnoAMuchosClonandoHijos.HijoClonable
                                    (
                                            RolUsuarioDAO.TABLA,
                                            EliminableDao(Rol.RolParaCreacionDeUsuario.NOMBRE_ENTIDAD, parametrosRolUsuarioDAO),
                                            SqlType.STRING,
                                            CreableDAOMultiples(parametrosRolUsuarioDAO, Rol.RolParaCreacionDeUsuario.NOMBRE_ENTIDAD),
                                            transformadorUsuarioParaCreacionARolUsuarioDAO,
                                            asignadorRolesUsuarioDAOAUsuarioParaCreacion
                                    )
                            ),
                            buscador
                    )
            ),
            actualizadorCamposIndividuales,
            eliminador,
            parametrosDAOUsuario,
            parametrosRolUsuarioDAO
                  )

    private constructor(
            listador: ListableFiltrableOrdenable<Usuario>,
            actualizadorCamposIndividuales: ActualizablePorCamposIndividuales<Usuario, String>,
            eliminador: EliminablePorId<Usuario, String>,
            parametrosDAOUsuario: ParametrosParaDAOEntidadDeCliente<UsuarioDAO, String?>,
            parametrosRolUsuarioDAO: ParametrosParaDAOEntidadDeCliente<RolUsuarioDAO, Long?>,
            hasherContraseña: Hasher
                       )
            : this(
            listador,
            BuscableSegunListableFiltrable(listador, CampoTabla(UsuarioDAO.TABLA, UsuarioDAO.COLUMNA_ID), SqlType.STRING),
            actualizadorCamposIndividuales,
            eliminador,
            parametrosDAOUsuario,
            parametrosRolUsuarioDAO,
            hasherContraseña
                  )

    private constructor(
            parametrosDAOUsuario: ParametrosParaDAOEntidadDeCliente<UsuarioDAO, String?>,
            parametrosRolUsuarioDAO: ParametrosParaDAOEntidadDeCliente<RolUsuarioDAO, Long?>,
            parametrosDAOPermisosBack: ParametrosParaDAOEntidadDeCliente<PermisoBackDAO, Long?>,
            parametrosRolDAO: ParametrosParaDAOEntidadDeCliente<RolDAO, String?>,
            hasherContraseña: Hasher
                       )
            : this(
            ListableConTransformacion<UsuarioEnBD, Usuario>(
                    darListableUsuarioBD(
                            parametrosDAOUsuario,
                            parametrosRolUsuarioDAO,
                            parametrosDAOPermisosBack,
                            parametrosRolDAO,
                            false),
                    transformadorUsuarioBDAUsuarioNegocio
                                                           ),
            ActualizablePorCamposIndividualesEnTransaccionSQL(
                    parametrosDAOUsuario.configuracion,
                    ActualizablePorCamposIndividualesSimple
                    (
                            ActualizablePorCamposIndividualesDao(NOMBRE_ENTIDAD, parametrosDAOUsuario),
                            TransformadorIdentidad(),
                            darTansformadorCampoContraseñaACampoContraseñaDaoSegunHasher(hasherContraseña)
                    )
                                                             ),
            EliminablePorIdEnTransaccionSQL(
                    parametrosDAOUsuario.configuracion,
                    EliminableSimple(
                            EliminableDao(Usuario.NOMBRE_ENTIDAD, parametrosDAOUsuario),
                            TransformadorIdentidad()
                                    )
                                           ),
            parametrosDAOUsuario,
            parametrosRolUsuarioDAO,
            hasherContraseña
                  )

    constructor(configuracion: ConfiguracionRepositorios, hasherContraseña: Hasher)
            : this(
            ParametrosParaDAOEntidadDeCliente(configuracion, UsuarioDAO.TABLA, UsuarioDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, RolUsuarioDAO.TABLA, RolUsuarioDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, PermisoBackDAO.TABLA, PermisoBackDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, RolDAO.TABLA, RolDAO::class.java),
            hasherContraseña
                  )
}

class RepositorioCredencialesGuardadasUsuarioSQL private constructor(
        private val buscador: Buscable<Usuario.CredencialesGuardadas, String>)
    : Buscable<Usuario.CredencialesGuardadas, String> by buscador,
      RepositorioCredencialesGuardadasUsuario
{
    private constructor(
            parametrosDAOUsuario: ParametrosParaDAOEntidadDeCliente<UsuarioDAO, String?>,
            parametrosRolUsuarioDAO: ParametrosParaDAOEntidadDeCliente<RolUsuarioDAO, Long?>,
            parametrosDAOPermisosBack: ParametrosParaDAOEntidadDeCliente<PermisoBackDAO, Long?>,
            parametrosRolDAO: ParametrosParaDAOEntidadDeCliente<RolDAO, String?>
                       ) : this(
            BuscableSegunListableFiltrable(
                    ListableConTransformacion<UsuarioEnBD, Usuario.CredencialesGuardadas>(
                            darListableUsuarioBD(
                                    parametrosDAOUsuario,
                                    parametrosRolUsuarioDAO,
                                    parametrosDAOPermisosBack,
                                    parametrosRolDAO,
                                    true),
                            transformadorUsuarioBDACredencialesUsuario
                                                                                         ),
                    CampoTabla(UsuarioDAO.TABLA, UsuarioDAO.COLUMNA_ID),
                    SqlType.STRING
                                          )
                               )

    constructor(configuracion: ConfiguracionRepositorios)
            : this(
            ParametrosParaDAOEntidadDeCliente(configuracion, UsuarioDAO.TABLA, UsuarioDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, RolUsuarioDAO.TABLA, RolUsuarioDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, PermisoBackDAO.TABLA, PermisoBackDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, RolDAO.TABLA, RolDAO::class.java)
                  )
}