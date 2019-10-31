package co.smartobjects.persistencia

import co.smartobjects.persistencia.excepciones.EsquemaNoExiste
import com.j256.ormlite.support.ConnectionSource
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

abstract class ConfiguracionRepositorios(val nombreBD: String)
{
    private val fuentesConexionesSegunNombreDeEsquema: ConcurrentMap<String, ConnectionSource> = ConcurrentHashMap()
    abstract val fuenteConexionesEsquemaPrincipal: ConnectionSource
    abstract val mapeadorCodigosError: MapeadorCodigosSQL

    open val llavesForaneasActivadas = true

    protected abstract fun crearFuenteDeConexionesParaNombreDeEsquema(nombreDeEsquema: String): ConnectionSource

    protected abstract fun crearEsquemaSiNoExiste(nombreDeEsquema: String)

    protected abstract fun eliminarEsquema(nombreDeEsquema: String)

    abstract fun limpiarRecursos()

    fun eliminarEsquemaParaLlave(nombreDeEsquema: String)
    {
        fuentesConexionesSegunNombreDeEsquema.remove(nombreDeEsquema)?.run {
            eliminarEsquema(nombreDeEsquema)
            close()
        }
    }

    fun inicializarConexionAEsquemaDeSerNecesario(nombreDeEsquema: String)
    {
        crearEsquemaSiNoExiste(nombreDeEsquema)
        if (!fuentesConexionesSegunNombreDeEsquema.containsKey(nombreDeEsquema))
        {
            fuentesConexionesSegunNombreDeEsquema[nombreDeEsquema] = crearFuenteDeConexionesParaNombreDeEsquema(nombreDeEsquema)
        }
    }

    @Throws(EsquemaNoExiste::class)
    fun darFuenteDeConexionesParaLlave(nombreDeEsquema: String): ConnectionSource
    {
        return fuentesConexionesSegunNombreDeEsquema[nombreDeEsquema] ?: throw EsquemaNoExiste(nombreDeEsquema)
    }
}