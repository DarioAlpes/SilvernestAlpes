package co.smartobjects.entidades.clientes

import co.smartobjects.campos.*
import co.smartobjects.entidades.EntidadReferenciable
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.threeten.bp.ZonedDateTime
import java.util.*

class Cliente private constructor
(
        override val id: Long?,
        val campoNombre: CampoNombre
) : EntidadReferenciable<Long?, Cliente>
{
    constructor(id: Long?, nombre: String) : this(id, CampoNombre(nombre))

    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = Cliente::class.java.simpleName
    }

    object Campos
    {
        @JvmField
        val NOMBRE = Cliente::nombre.name
    }

    val nombre = campoNombre.valor


    override fun copiarConId(idNuevo: Long?): Cliente
    {
        return copiar(id = idNuevo)
    }

    fun copiar(id: Long? = this.id, nombre: String = this.nombre): Cliente
    {
        return Cliente(id, nombre)
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Cliente

        if (id != other.id) return false
        if (nombre != other.nombre) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = id.hashCode()
        result = 31 * result + nombre.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "Cliente(id=$id, nombre='$nombre')"
    }

    class CampoNombre(nombre: String) : CampoEntidad<Cliente, String>(nombre, ValidadorCampoStringNoVacio(), NOMBRE_ENTIDAD, Campos.NOMBRE)

    class LlaveNFC private constructor
    (
            val idCliente: Long,
            val campoLlave: CampoLlave,
            val campoFechaCreacion: CampoFechaDeRealizacion
    )
    {
        companion object
        {
            @JvmField
            val NOMBRE_ENTIDAD: String = LlaveNFC::class.java.simpleName
        }

        object Campos
        {
            @JvmField
            val LLAVE = LlaveNFC::llave.name

            @JvmField
            val FECHA_CREACION = LlaveNFC::campoFechaCreacion.name
        }

        constructor(idCliente: Long, llaveNFC: CharArray) : this(idCliente, llaveNFC, ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO))
        constructor(idCliente: Long, llaveNFC: String) : this(idCliente, llaveNFC.toCharArray())

        constructor(idCliente: Long, llaveNFC: CharArray, fechaCreacion: ZonedDateTime)
                : this(idCliente, CampoLlave(llaveNFC), CampoFechaDeRealizacion(fechaCreacion))

        constructor(idCliente: Long, llaveNFC: String, fechaCreacion: ZonedDateTime)
                : this(idCliente, llaveNFC.toCharArray(), fechaCreacion)

        val llave: CharArray = campoLlave.valor
        val fechaCreacion: ZonedDateTime = campoFechaCreacion.valor


        fun copiar(idCliente: Long = this.idCliente, llave: CharArray = this.llave, fechaCreacion: ZonedDateTime = this.fechaCreacion): LlaveNFC
        {
            if (llave !== this.llave)
            {
                limpiar()
            }
            return LlaveNFC(idCliente, llave, fechaCreacion)
        }

        fun limpiar()
        {
            campoLlave.limpiarValor()
        }

        override fun toString() = ""

        override fun equals(other: Any?): Boolean
        {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as LlaveNFC

            if (idCliente != other.idCliente) return false
            if (!Arrays.equals(llave, other.llave)) return false

            return true
        }

        override fun hashCode(): Int
        {
            var result = idCliente.hashCode()
            result = 31 * result + Arrays.hashCode(llave)
            return result
        }

        // El hashcode
        @Suppress("EqualsOrHashCode")
        class CampoLlave(contraseña: CharArray)
            : CampoModificable<LlaveNFC, CharArray>(contraseña, ValidadorCampoArregloCharNoVacio(), NOMBRE_ENTIDAD, Campos.LLAVE)
        {
            fun limpiarValor()
            {
                valor.fill('\u0000')
            }

            override fun equals(other: Any?): Boolean
            {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false
                other as CampoLlave
                if (!Arrays.equals(valor, other.valor)) return false
                if (!Arrays.equals(valor, other.valor)) return false
                if (!Arrays.equals(valor, other.valor)) return false
                return true
            }

            override fun hashCode(): Int
            {
                return Arrays.hashCode(valor)
            }
        }

        class CampoFechaDeRealizacion(fechaDeRealizacion: ZonedDateTime)
            : CampoEntidad<LlaveNFC, ZonedDateTime>(
                fechaDeRealizacion,
                ValidadorEnCadena(validadorDeZonaHoraria, validadorFechaMayorAMinima),
                NOMBRE_ENTIDAD,
                Campos.FECHA_CREACION)
    }
}