package co.smartobjects.prompterbackend

import co.smartobjects.persistencia.usuarios.RepositorioCredencialesGuardadasUsuario
import co.smartobjects.persistencia.usuarios.RepositorioCredencialesGuardadasUsuarioSQL
import co.smartobjects.persistencia.usuariosglobales.RepositorioCredencialesGuardadasUsuarioGlobal
import co.smartobjects.persistencia.usuariosglobales.RepositorioCredencialesGuardadasUsuarioGlobalSQL
import co.smartobjects.persistencia.usuariosglobales.RepositorioUsuariosGlobalesSQL
import co.smartobjects.prompterbackend.excepciones.MapperErrorAPI
import co.smartobjects.prompterbackend.seguridad.shiro.RealmRepositorioUsuarios
import co.smartobjects.prompterbackend.serviciosrest.clientes.RecursoClientes
import co.smartobjects.prompterbackend.serviciosrest.usuariosglobales.RecursoUsuariosGlobales
import co.smartobjects.red.ConfiguracionJackson
import com.fasterxml.jackson.datatype.jaxrs.Jaxrs2TypesModule
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider
import com.fasterxml.jackson.module.afterburner.AfterburnerModule
import org.glassfish.jersey.server.ResourceConfig
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.lang.reflect.Type
import javax.ws.rs.ext.ParamConverter
import javax.ws.rs.ext.ParamConverterProvider
import javax.ws.rs.ext.Provider


internal class ConfiguracionAplicacionJersey : ResourceConfig()
{
    companion object
    {
        /* Culpa de https://stackoverflow.com/questions/51007975/isinitialized-property-for-lateinit-isnt-working-in-companion-object
                https://stackoverflow.com/questions/46366869/kotlin-workaround-for-no-lateinit-when-using-custom-setter
        */

        private var _DEPENDENCIAS: Dependencias? = null
        internal var DEPENDENCIAS: Dependencias
            get() = _DEPENDENCIAS
                    ?: throw UninitializedPropertyAccessException("\"DEPENDENCIAS\" no ha sido inicializada")
            set(value)
            {
                _DEPENDENCIAS = value
            }

        private var _RECURSO_CLIENTES: RecursoClientes? = null
        internal var RECURSO_CLIENTES: RecursoClientes
            get() = _RECURSO_CLIENTES
                    ?: throw UninitializedPropertyAccessException("\"RECURSO_CLIENTES\" no ha sido inicializada")
            set(value)
            {
                _RECURSO_CLIENTES = value
            }

        private var _RECURSO_USUARIOS_GLOBALES: RecursoUsuariosGlobales? = null
        internal var RECURSO_USUARIOS_GLOBALES: RecursoUsuariosGlobales
            get() = _RECURSO_USUARIOS_GLOBALES
                    ?: throw UninitializedPropertyAccessException("\"RECURSO_USUARIOS_GLOBALES\" no ha sido inicializada")
            set(value)
            {
                _RECURSO_USUARIOS_GLOBALES = value
            }

        @JvmField
        internal var RECURSOS_ADICIONALES: List<Any> = listOf()

        private var _REPOSITORIO_CREDENCIALES_GUARDADAS: RepositorioCredencialesGuardadasUsuario? = null
        internal var REPOSITORIO_CREDENCIALES_GUARDADAS: RepositorioCredencialesGuardadasUsuario
            get() = _REPOSITORIO_CREDENCIALES_GUARDADAS
                    ?: throw UninitializedPropertyAccessException("\"REPOSITORIO_CREDENCIALES_GUARDADAS\" no ha sido inicializada")
            set(value)
            {
                _REPOSITORIO_CREDENCIALES_GUARDADAS = value
            }

        private var _REPOSITORIO_CREDENCIALES_GUARDADAS_GLOBALES: RepositorioCredencialesGuardadasUsuarioGlobal? = null
        internal var REPOSITORIO_CREDENCIALES_GUARDADAS_GLOBALES: RepositorioCredencialesGuardadasUsuarioGlobal
            get() = _REPOSITORIO_CREDENCIALES_GUARDADAS_GLOBALES
                    ?: throw UninitializedPropertyAccessException("\"REPOSITORIO_CREDENCIALES_GUARDADAS_GLOBALES\" no ha sido inicializada")
            set(value)
            {
                _REPOSITORIO_CREDENCIALES_GUARDADAS_GLOBALES = value
            }


        fun inicializacionPorDefecto()
        {
            if (_DEPENDENCIAS == null)
            {
                DEPENDENCIAS = DependenciasImpl()
            }

            if (_RECURSO_CLIENTES == null)
            {
                RECURSO_CLIENTES = RecursoClientes(DEPENDENCIAS)
            }

            if (_RECURSO_USUARIOS_GLOBALES == null)
            {
                RECURSO_USUARIOS_GLOBALES =
                        RecursoUsuariosGlobales(
                                RepositorioUsuariosGlobalesSQL(
                                        DEPENDENCIAS.configuracionRepositorios,
                                        RealmRepositorioUsuarios.hasherShiro
                                                              ),
                                RECURSO_CLIENTES.manejadorSeguridad
                                               )
            }

            if (_REPOSITORIO_CREDENCIALES_GUARDADAS == null)
            {
                REPOSITORIO_CREDENCIALES_GUARDADAS =
                        RepositorioCredencialesGuardadasUsuarioSQL(DEPENDENCIAS.configuracionRepositorios)
            }

            if (_REPOSITORIO_CREDENCIALES_GUARDADAS_GLOBALES == null)
            {
                REPOSITORIO_CREDENCIALES_GUARDADAS_GLOBALES =
                        RepositorioCredencialesGuardadasUsuarioGlobalSQL(DEPENDENCIAS.configuracionRepositorios)
            }
        }
    }


    init
    {
        inicializacionPorDefecto()

        val proveedorJson = JacksonJaxbJsonProvider().apply {
            val mapeadorJackson =
                    ConfiguracionJackson.objectMapperDeJackson.apply {
                        registerModule(Jaxrs2TypesModule())
                        registerModule(AfterburnerModule())
                    }

            setMapper(mapeadorJackson)
        }

        register(proveedorJson)
        register(ZonedDateTimeConverterProvider())
        register(FiltroCORS().apply { origenesPermitidos.add(PrompterBackend.BASE_URI.host) })
        registrarShiro()
        registrarRecursos()
    }

    private fun registrarShiro()
    {
        RealmRepositorioUsuarios.repositorioCredencialesUsuarioGuardadas = REPOSITORIO_CREDENCIALES_GUARDADAS
        RealmRepositorioUsuarios.repositorioCredencialesUsuariosGlobalesGuardadas = REPOSITORIO_CREDENCIALES_GUARDADAS_GLOBALES
    }

    private fun registrarRecursos()
    {
        register(RECURSO_CLIENTES)
        register(RECURSO_USUARIOS_GLOBALES)
        RECURSO_USUARIOS_GLOBALES.inicializar()
        RECURSOS_ADICIONALES.forEach {
            register(it)
        }
        register(MapperErrorAPI::class.java)
    }
}

@Provider
private class ZonedDateTimeConverterProvider : ParamConverterProvider
{
    override fun <T> getConverter(rawType: Class<T>, genericType: Type, annotations: Array<Annotation>): ParamConverter<T>?
    {
        return if (rawType == ZonedDateTime::class.java)
        {
            @Suppress("UNCHECKED_CAST")
            ZonedDateTimeConverter() as ParamConverter<T>
        }
        else
        {
            null
        }
    }
}


private class ZonedDateTimeConverter : ParamConverter<ZonedDateTime?>
{
    override fun fromString(valorString: String): ZonedDateTime?
    {
        return ZonedDateTime.parse(valorString, DateTimeFormatter.ISO_ZONED_DATE_TIME)
    }

    override fun toString(objeto: ZonedDateTime?): String?
    {
        return objeto?.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
    }
}