package co.smartobjects.entidades.fondos.precios

import co.smartobjects.campos.*
import co.smartobjects.entidades.EntidadReferenciable
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.personas.ValorGrupoEdad


class GrupoClientes private constructor
(
        override val id: Long?,
        val campoNombre: CampoNombre,
        val campoSegmentoClientes: CampoSegmentos
) : EntidadReferenciable<Long?, GrupoClientes>
{
    constructor(id: Long?, nombre: String, segmentosClientes: List<SegmentoClientes>) : this(id, CampoNombre(nombre), CampoSegmentos(segmentosClientes))

    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = GrupoClientes::class.java.simpleName

        @JvmField
        val COMPARADOR_PARA_PRIORIDAD = Comparator<GrupoClientes> { o1, o2 ->
            val diferenciaTamaños = o1.segmentosClientes.size - o2.segmentosClientes.size
            if (diferenciaTamaños != 0)
            {
                diferenciaTamaños
            }
            else
            {
                o1.segmentosClientes.sumBy { it.campo.prioridad } - o1.segmentosClientes.sumBy { it.campo.prioridad }
            }
        }
    }

    object Campos
    {
        @JvmField
        val NOMBRE = GrupoClientes::nombre.name
        @JvmField
        val SEGMENTOS = GrupoClientes::segmentosClientes.name
    }

    val nombre = campoNombre.valor
    val segmentosClientes = campoSegmentoClientes.valor

    fun copiar(id: Long? = this.id, nombre: String = this.nombre, segmentosClientes: List<SegmentoClientes> = this.segmentosClientes): GrupoClientes
    {
        return GrupoClientes(id, nombre, segmentosClientes)
    }

    override fun copiarConId(idNuevo: Long?): GrupoClientes
    {
        return copiar(id = idNuevo)
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GrupoClientes

        if (id != other.id) return false
        if (nombre != other.nombre) return false
        if (segmentosClientes != other.segmentosClientes) return false

        return true
    }

    fun aplicaParaDatos(datos: DatosParaCalculoGrupoClientes): Boolean
    {
        return segmentosClientes.all { it.aplicaParaDatos(datos) }
    }

    override fun hashCode(): Int
    {
        var result = id.hashCode()
        result = 31 * result + nombre.hashCode()
        result = 31 * result + segmentosClientes.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "GrupoClientes(id=$id, nombre='$nombre', segmentosClientes=$segmentosClientes)"
    }

    class CampoNombre(nombre: String)
        : CampoModificable<GrupoClientes, String>(nombre, ValidadorCampoStringNoVacio(), NOMBRE_ENTIDAD, Campos.NOMBRE)

    class CampoSegmentos(segmentosClientes: List<SegmentoClientes>)
        : CampoEntidad<GrupoClientes, List<SegmentoClientes>>(
            segmentosClientes,
            ValidadorEnCadena(
                    ValidadorCampoColeccionNoVacio(),
                    ValidadorCampoColeccionSinRepetidos(false, "campo") { it.campo }
                             ),
            NOMBRE_ENTIDAD,
            Campos.SEGMENTOS)
}

class SegmentoClientes private constructor(
        val id: Long?,
        val campo: NombreCampo,
        val campoValor: CampoValor
                                          )
{
    constructor(id: Long?, campo: NombreCampo, valor: String) : this(id, campo, CampoValor(valor, campo))

    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = SegmentoClientes::class.java.simpleName
    }

    object Campos
    {
        @JvmField
        val VALOR = SegmentoClientes::valor.name
    }

    val valor = campoValor.valor

    enum class NombreCampo(val prioridad: Int)
    {
        CATEGORIA(0), GRUPO_DE_EDAD(1), TIPO(2) ;

    }

    fun aplicaParaDatos(datos: DatosParaCalculoGrupoClientes): Boolean
    {   println(datos.posibleCategoria.toString())
        println(datos.posibleGrupoEdad?.valor)
        println(datos.posibleTipo.toString())
        return when (campo)
        {
            SegmentoClientes.NombreCampo.CATEGORIA     -> valor == datos.posibleCategoria.toString()
            SegmentoClientes.NombreCampo.GRUPO_DE_EDAD -> valor == datos.posibleGrupoEdad?.valor
            SegmentoClientes.NombreCampo.TIPO -> valor == datos.posibleTipo.toString()
        }
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SegmentoClientes

        if (id != other.id) return false
        if (campo != other.campo) return false
        if (valor != other.valor) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = id.hashCode()
        result = 31 * result + campo.hashCode()
        result = 31 * result + valor.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "SegmentoClientes(id=$id, campo=$campo, valor='$valor')"
    }

    fun copiar(id: Long? = this.id, campo: NombreCampo = this.campo, valor: String = this.valor): SegmentoClientes
    {
        return SegmentoClientes(id, campo, valor)
    }

    class CampoValor(valor: String, campo: NombreCampo) : CampoEntidad<SegmentoClientes, String>(
            valor,
            ValidadorEnCadena(
                    ValidadorCampoStringNoVacio(),
                    ValidadorCondicional(campo == NombreCampo.CATEGORIA, ValidadorCampoConValorPermitido(Persona.Categoria.values().map { it.toString() })),
                    ValidadorCondicional(campo == NombreCampo.TIPO, ValidadorCampoConValorPermitido(Persona.Tipo.values().map{it.toString()}))
                             ),
            NOMBRE_ENTIDAD,
            Campos.VALOR)
}

data class DatosParaCalculoGrupoClientes(val posibleCategoria: Persona.Categoria, val posibleGrupoEdad: ValorGrupoEdad?, val posibleTipo: Persona.Tipo)