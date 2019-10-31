package co.smartobjects.persistencia.operativas

import co.smartobjects.entidades.fondos.precios.GrupoClientes
import co.smartobjects.entidades.fondos.precios.Impuesto
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.ubicaciones.Ubicacion
import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.entidades.usuarios.Rol
import co.smartobjects.entidades.usuarios.Usuario
import co.smartobjects.persistencia.EntidadDAOBasePruebas
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorio
import co.smartobjects.persistencia.fondos.grupoClientesCategoriaA
import co.smartobjects.persistencia.fondos.impuestoPruebasPorDefecto
import co.smartobjects.persistencia.fondos.monedas.RepositorioMonedas
import co.smartobjects.persistencia.fondos.monedas.RepositorioMonedasSQL
import co.smartobjects.persistencia.fondos.paquete.RepositorioPaquetes
import co.smartobjects.persistencia.fondos.paquete.RepositorioPaquetesSQL
import co.smartobjects.persistencia.fondos.precios.gruposclientes.RepositorioGrupoClientes
import co.smartobjects.persistencia.fondos.precios.gruposclientes.RepositorioGrupoClientesSQL
import co.smartobjects.persistencia.fondos.precios.impuestos.RepositorioImpuestos
import co.smartobjects.persistencia.fondos.precios.impuestos.RepositorioImpuestosSQL
import co.smartobjects.persistencia.personas.RepositorioPersonas
import co.smartobjects.persistencia.personas.RepositorioPersonasSQL
import co.smartobjects.persistencia.ubicaciones.RepositorioUbicaciones
import co.smartobjects.persistencia.ubicaciones.RepositorioUbicacionesSQL
import co.smartobjects.persistencia.usuarios.Hasher
import co.smartobjects.persistencia.usuarios.RepositorioUsuarios
import co.smartobjects.persistencia.usuarios.RepositorioUsuariosSQL
import co.smartobjects.persistencia.usuarios.roles.RepositorioRoles
import co.smartobjects.persistencia.usuarios.roles.RepositorioRolesSQL
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.threeten.bp.LocalDate
import org.threeten.bp.ZonedDateTime


internal abstract class ClasePruebasEntidadesTransaccionales : EntidadDAOBasePruebas()
{
    private val hasherDummy = object : Hasher
    {
        override fun calcularHash(entrada: CharArray) = "un-hash-que-no-importa"
    }
    protected val repositorioMonedas: RepositorioMonedas by lazy { RepositorioMonedasSQL(configuracionRepositorios) }
    protected val repositorioPersonas: RepositorioPersonas by lazy { RepositorioPersonasSQL(configuracionRepositorios) }
    protected val repositorioUbicaciones: RepositorioUbicaciones by lazy { RepositorioUbicacionesSQL(configuracionRepositorios) }
    protected val repositorioImpuestos: RepositorioImpuestos by lazy { RepositorioImpuestosSQL(configuracionRepositorios) }
    protected val repositorioGrupoClientes: RepositorioGrupoClientes by lazy { RepositorioGrupoClientesSQL(configuracionRepositorios) }
    protected val repositorioPaquetes: RepositorioPaquetes by lazy { RepositorioPaquetesSQL(configuracionRepositorios) }
    protected val repositorioRoles: RepositorioRoles by lazy { RepositorioRolesSQL(configuracionRepositorios) }
    protected val repositorioUsuarios: RepositorioUsuarios by lazy { RepositorioUsuariosSQL(configuracionRepositorios, hasherDummy) }

    override val creadoresRepositoriosUsados: List<CreadorRepositorio<*>> by lazy {
        listOf<CreadorRepositorio<*>>(
                repositorioRoles,
                repositorioUsuarios,
                repositorioImpuestos,
                repositorioGrupoClientes,
                repositorioMonedas,
                repositorioPaquetes,
                repositorioUbicaciones,
                repositorioPersonas
                                     )
    }

    protected val fechaHoraActual = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)!!
    protected val grupoDeClientesCreado: GrupoClientes by lazy { repositorioGrupoClientes.crear(idClientePruebas, grupoClientesCategoriaA) }
    protected val impuestoCreado: Impuesto by lazy { repositorioImpuestos.crear(idClientePruebas, impuestoPruebasPorDefecto) }
    protected val personaCreada: Persona by lazy {
        repositorioPersonas.crear(idClientePruebas, Persona(
                idClientePruebas,
                null,
                "Persona prueba",
                Persona.TipoDocumento.CC,
                "123",
                Persona.Genero.DESCONOCIDO,
                LocalDate.now(ZONA_HORARIA_POR_DEFECTO),
                Persona.Categoria.D,
                Persona.Afiliacion.COTIZANTE,
                null,
                "empresa",
                "0",
                Persona.Tipo.NO_AFILIADO
                                                           )
                                 )
    }

    protected val personaCreada2: Persona by lazy {
        repositorioPersonas.crear(idClientePruebas, Persona(
                idClientePruebas,
                null,
                "Persona prueba",
                Persona.TipoDocumento.TI,
                "123",
                Persona.Genero.DESCONOCIDO,
                LocalDate.now(ZONA_HORARIA_POR_DEFECTO),
                Persona.Categoria.D,
                Persona.Afiliacion.COTIZANTE,
                null,
                "empresa",
                "0",
                Persona.Tipo.NO_AFILIADO
                                                           )
                                 )
    }

    protected val personaCreada3: Persona by lazy {
        repositorioPersonas.crear(idClientePruebas, Persona(
                idClientePruebas,
                null,
                "Persona prueba",
                Persona.TipoDocumento.CE,
                "123",
                Persona.Genero.DESCONOCIDO,
                LocalDate.now(ZONA_HORARIA_POR_DEFECTO),
                Persona.Categoria.D,
                Persona.Afiliacion.COTIZANTE,
                null,
                "empresa",
                "0",
                Persona.Tipo.NO_AFILIADO
                                                           )
                                 )
    }

    protected val personaCreada4: Persona by lazy {
        repositorioPersonas.crear(idClientePruebas, Persona(
                idClientePruebas,
                null,
                "Persona prueba",
                Persona.TipoDocumento.CD,
                "123",
                Persona.Genero.DESCONOCIDO,
                LocalDate.now(ZONA_HORARIA_POR_DEFECTO),
                Persona.Categoria.D,
                Persona.Afiliacion.COTIZANTE,
                null,
                "empresa",
                "0",
                Persona.Tipo.NO_AFILIADO
                                                           )
                                 )
    }

    protected val ubicacionCreada: Ubicacion by lazy {
        repositorioUbicaciones.crear(idClientePruebas, Ubicacion(
                idClientePruebas,
                null,
                "Ubicacion madre",
                Ubicacion.Tipo.PROPIEDAD,
                Ubicacion.Subtipo.POS,
                null,
                linkedSetOf()
                                                                )
                                    )
    }

    protected val rolCreado: Rol by lazy {
        val permisos = setOf(PermisoBack(idClientePruebas, "ElEndpoint", PermisoBack.Accion.POST), PermisoBack(idClientePruebas, "ElEndpoint", PermisoBack.Accion.PUT))
        repositorioRoles.crear(idClientePruebas, Rol(
                "ElRol",
                "Descripcion",
                permisos
                                                    )
                              )
    }

    protected val usuarioCreado: Usuario by lazy {
        repositorioUsuarios.crear(idClientePruebas, Usuario.UsuarioParaCreacion(
                Usuario.DatosUsuario(
                        idClientePruebas,
                        "El Usuario",
                        "El nombre completo",
                        "elemail@mail.com",
                        true
                                    ),
                charArrayOf('1', '2', '3'),
                setOf(Rol.RolParaCreacionDeUsuario(rolCreado.nombre))
                                                                               )
                                 )
    }
}