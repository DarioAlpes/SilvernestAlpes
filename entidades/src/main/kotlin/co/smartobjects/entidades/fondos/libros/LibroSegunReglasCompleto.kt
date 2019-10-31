package co.smartobjects.entidades.fondos.libros

import co.smartobjects.entidades.EntidadReferenciable

class LibroSegunReglasCompleto<TipoLibro : Libro<TipoLibro>> private constructor
(
        val idCliente: Long,
        override val id: Long?,
        val campoNombre: LibroSegunReglas.CampoNombre,
        val libro: TipoLibro,
        val reglasIdUbicacion: MutableSet<ReglaDeIdUbicacion>,
        val reglasIdGrupoDeClientes: MutableSet<ReglaDeIdGrupoDeClientes>,
        val reglasIdPaquete: MutableSet<ReglaDeIdPaquete>
) : EntidadReferenciable<Long?, LibroSegunReglasCompleto<TipoLibro>>
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = LibroSegunReglasCompleto::class.java.simpleName
    }

    object Campos
    {
        @JvmField
        val NOMBRE = Libro<*>::nombre.name
    }

    val nombre = campoNombre.valor

    val reglas: Sequence<Regla<*>>
        get() = reglasIdUbicacion.asSequence() + reglasIdGrupoDeClientes + reglasIdPaquete

    constructor(
            idCliente: Long,
            id: Long?,
            nombre: String,
            libro: TipoLibro,
            reglasIdUbicacion: MutableSet<ReglaDeIdUbicacion>,
            reglasIdGrupoDeClientes: MutableSet<ReglaDeIdGrupoDeClientes>,
            reglasIdPaquete: MutableSet<ReglaDeIdPaquete>)
            : this(idCliente, id, LibroSegunReglas.CampoNombre(nombre), libro, reglasIdUbicacion, reglasIdGrupoDeClientes, reglasIdPaquete)

    constructor(
            libroSegunReglas: LibroSegunReglas,
            libro: TipoLibro
               )
            : this(
            libroSegunReglas.idCliente,
            libroSegunReglas.id,
            libroSegunReglas.campoNombre,
            libro,
            libroSegunReglas.reglasIdUbicacion,
            libroSegunReglas.reglasIdGrupoDeClientes,
            libroSegunReglas.reglasIdPaquete
                  )

    fun copiar(
            idCliente: Long = this.idCliente,
            id: Long? = this.id,
            nombre: String = this.nombre,
            libro: TipoLibro = this.libro,
            reglasIdUbicacion: MutableSet<ReglaDeIdUbicacion> = this.reglasIdUbicacion,
            reglasIdGrupoDeClientes: MutableSet<ReglaDeIdGrupoDeClientes> = this.reglasIdGrupoDeClientes,
            reglasIdPaquete: MutableSet<ReglaDeIdPaquete> = this.reglasIdPaquete)
            : LibroSegunReglasCompleto<TipoLibro>
    {
        return LibroSegunReglasCompleto(idCliente, id, nombre, libro, reglasIdUbicacion, reglasIdGrupoDeClientes, reglasIdPaquete)
    }

    override fun copiarConId(idNuevo: Long?): LibroSegunReglasCompleto<TipoLibro>
    {
        return copiar(id = idNuevo)
    }

    fun aLibroSegunReglas() =
            LibroSegunReglas(
                    idCliente,
                    id,
                    nombre,
                    libro.id!!,
                    reglasIdUbicacion,
                    reglasIdGrupoDeClientes,
                    reglasIdPaquete
                            )

    fun esAplicable(idUbicacion: Long?, idGrupoDeCliente: Long?, idPaquete: Long?): Boolean
    {
        return calcularEspecificidad(idUbicacion, idGrupoDeCliente, idPaquete) > -1
    }

    /**
     * Determina qué tan específico es un libro de reglas.
     *
     * Un libro es más específico entre más reglas apliquen.
     *
     * Ejemplo:
     *
     * Libro A tiene las reglas (a, b, c). Libro B tiene las reglas (a, b, ). Libro C no tiene reglas (, , , )
     *
     * y se usan los parámetros a, b, c
     *
     * Entonces A.[calcularEspecificidad](a, b, c) > B.[calcularEspecificidad](a, b, c) > C.[calcularEspecificidad](a, b, c)
     *
     *
     * @return El grado de especificidad. Si para algún parámetro no nulo no se encontró regla retorna -1.
     */
    fun calcularEspecificidad(idUbicacion: Long?, idGrupoDeCliente: Long?, idPaquete: Long?): Int
    {
        fun <TipoId> especificidadParaParametro(idAVerificar: TipoId?, reglas: Set<Regla<TipoId>>): Int?
        {
            return if (reglas.isEmpty())
            {
                if (idAVerificar == null)
                {
                    1
                }
                else
                {
                    0
                }
            }
            else
            {
                if (idAVerificar == null)
                {
                    // Si define reglas tiene que definir un id
                    null
                }
                else
                {
                    if (reglas.any { it.validar(idAVerificar) })
                    {
                        // Usar 10 para darle más peso cuando se encuentra una regla que aplique que cuando no tiene
                        // reglas ni definió id
                        10
                    }
                    else
                    {
                        // Si definió id y no aplicó ninguna regla quiere decir que el libro no se puede usar
                        null
                    }
                }
            }
        }

        val especificidadDeUbicacion =
                especificidadParaParametro(idUbicacion, reglasIdUbicacion)
                ?: return -1

        val especificidadDeGrupoClientes =
                especificidadParaParametro(idGrupoDeCliente, reglasIdGrupoDeClientes)
                ?: return -1

        val especificidadDePaquete =
                especificidadParaParametro(idPaquete, reglasIdPaquete)
                ?: return -1

        return especificidadDeUbicacion + especificidadDeGrupoClientes + especificidadDePaquete
    }

    fun buscarReglasQueAplican(idUbicacion: Long?, idGrupoDeCliente: Long?, idPaquete: Long?): Set<Regla<*>>
    {
        fun <TipoId> buscarReglaAplicada(idAVerificar: TipoId?, reglas: Set<Regla<TipoId>>): Regla<TipoId>?
        {
            return if (reglas.isNotEmpty() && idAVerificar != null)
            {
                reglas.find { it.validar(idAVerificar) }
            }
            else
            {
                null
            }
        }

        return mutableListOf<Regla<*>>().apply {
            buscarReglaAplicada(idUbicacion, reglasIdUbicacion)?.also { add(it) }
            buscarReglaAplicada(idGrupoDeCliente, reglasIdGrupoDeClientes)?.also { add(it) }
            buscarReglaAplicada(idPaquete, reglasIdPaquete)?.also { add(it) }
        }.toSet()
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LibroSegunReglasCompleto<*>

        if (idCliente != other.idCliente) return false
        if (id != other.id) return false
        if (libro != other.libro) return false
        if (reglasIdUbicacion != other.reglasIdUbicacion) return false
        if (reglasIdGrupoDeClientes != other.reglasIdGrupoDeClientes) return false
        if (reglasIdPaquete != other.reglasIdPaquete) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = idCliente.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + libro.hashCode()
        result = 31 * result + reglasIdUbicacion.hashCode()
        result = 31 * result + reglasIdGrupoDeClientes.hashCode()
        result = 31 * result + reglasIdPaquete.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "LibroSegunReglasCompleto(idCliente=$idCliente, id=$id, nombre='$nombre', libro=$libro, reglasIdUbicacion=$reglasIdUbicacion, reglasIdGrupoDeClientes=$reglasIdGrupoDeClientes, reglasIdPaquete=$reglasIdPaquete)"
    }
}