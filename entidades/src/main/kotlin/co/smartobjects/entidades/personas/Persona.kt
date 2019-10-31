package co.smartobjects.entidades.personas

import co.smartobjects.campos.CampoEntidad
import co.smartobjects.campos.ValidadorCampoConLimiteSuperior
import co.smartobjects.campos.ValidadorCampoStringNoVacio
import co.smartobjects.entidades.EntidadReferenciable
import co.smartobjects.entidades.fondos.precios.GrupoClientes
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.threeten.bp.LocalDate
import org.threeten.bp.Period

class Persona private constructor(
        val idCliente: Long,
        override val id: Long?,
        val campoNombreCompleto: CampoNombreCompleto,
        val tipoDocumento: TipoDocumento,
        val campoNumeroDocumento: CampoNumeroDocumento,
        val genero: Genero,
        val campoFechaNacimiento: CampoFechaNacimiento,
        val categoria: Categoria,
        val afiliacion: Afiliacion,
        val esAnonima: Boolean,
        val llaveImagen: String?,
        val campoEmpresa: CampoEmpresa,
        val campoNitEmpresa: CampoNitEmpresa,
        val tipo: Tipo)
    : EntidadReferenciable<Long?, Persona>
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = Persona::class.java.simpleName
        const val NOMBRE_ANONIMA = "Anónima"
        const val NUMERO_DOCUMENTO_ANONIMA = "0"
        @JvmField
        val FECHA_NACIMIENTO_ANONIMA: LocalDate = LocalDate.of(1990, 1, 1)
        const val EMPRESA_ANONIMA = "n/a"
        const val NIT_EMPRESA_ANONIMA = "0"
    }

    object Campos
    {
        @JvmField
        val NOMBRE_COMPLETO = Persona::nombreCompleto.name
        @JvmField
        val TIPO_DOCUMENTO = Persona::tipoDocumento.name
        @JvmField
        val NUMERO_DOCUMENTO = Persona::numeroDocumento.name
        @JvmField
        val GENERO = Persona::genero.name
        @JvmField
        val FECHA_NACIMIENTO = Persona::fechaNacimiento.name
        @JvmField
        val CATEGORIA = Persona::categoria.name
        @JvmField
        val AFILIACION = Persona::afiliacion.name
        @JvmField
        val ES_ANONIMA = Persona::esAnonima.name
        @JvmField
        val LLAVE_IMAGEN = Persona::llaveImagen.name
        @JvmField
        val EMPRESA = Persona::empresa.name
        @JvmField
        val NIT_EMPRESA = Persona::nitEmpresa.name
        @JvmField
        val TIPO = Persona::tipo.name
    }
    val nombreCompleto = campoNombreCompleto.valor
    val numeroDocumento = campoNumeroDocumento.valor
    val fechaNacimiento = campoFechaNacimiento.valor
    val edad = Period.between(fechaNacimiento, LocalDate.now(ZONA_HORARIA_POR_DEFECTO)).years
    val documentoCompleto = "$tipoDocumento $numeroDocumento"
    val empresa = campoEmpresa.valor
    val nitEmpresa = campoNitEmpresa.valor


    constructor(
            idCliente: Long,
            id: Long?,
            nombreCompleto: String,
            tipoDocumento: TipoDocumento,
            numeroDocumento: String,
            genero: Genero,
            fechaNacimiento: LocalDate,
            categoria: Categoria,
            afiliacion: Afiliacion,
            esAnonima: Boolean,
            llaveImagen: String?,
            empresa: String,
            nitEmpresa: String,
            tipo: Tipo
               ) : this(
            idCliente,
            id,
            CampoNombreCompleto(nombreCompleto),
            tipoDocumento,
            CampoNumeroDocumento(numeroDocumento),
            genero,
            CampoFechaNacimiento(fechaNacimiento),
            categoria,
            afiliacion,
            esAnonima,
            llaveImagen,
            CampoEmpresa(empresa),
            CampoNitEmpresa(nitEmpresa),
            tipo
                       )

    constructor(
            idCliente: Long,
            id: Long?,
            nombreCompleto: String,
            tipoDocumento: TipoDocumento,
            numeroDocumento: String,
            genero: Genero,
            fechaNacimiento: LocalDate,
            categoriaPersona: Categoria,
            afiliacion: Afiliacion,
            imageKey: String?,
            empresa: String,
            nitEmpresa: String,
            tipo: Tipo) : this(idCliente,
                                      id,
                                      nombreCompleto,
                                      tipoDocumento,
                                      numeroDocumento,
                                      genero,
                                      fechaNacimiento,
                                      categoriaPersona,
                                      afiliacion,
                                      false,
                                      imageKey,
                                      empresa,
                                      nitEmpresa,
                                      tipo)

    constructor(
            idCliente: Long,
            id: Long?
               ) : this(idCliente,
                        id,
                        NOMBRE_ANONIMA,
                        TipoDocumento.CC,
                        NUMERO_DOCUMENTO_ANONIMA,
                        Genero.DESCONOCIDO,
                        FECHA_NACIMIENTO_ANONIMA,
                        Categoria.D,
                        Afiliacion.COTIZANTE,
                        true,
                        null,
                        EMPRESA_ANONIMA,
                        NIT_EMPRESA_ANONIMA,
                        Tipo.NO_AFILIADO
            )


    fun copiar(
            idCliente: Long = this.idCliente,
            id: Long? = this.id,
            nombreCompleto: String = this.nombreCompleto,
            tipoDocumento: TipoDocumento = this.tipoDocumento,
            numeroDocumento: String = this.numeroDocumento,
            genero: Genero = this.genero,
            fechaNacimiento: LocalDate = this.fechaNacimiento,
            categoria: Categoria = this.categoria,
            afiliacion: Afiliacion = this.afiliacion,
            esAnonima: Boolean = this.esAnonima,
            llaveImagen: String? = this.llaveImagen,
            empresa: String = this.empresa,
            nitEmpresa: String = this.nitEmpresa,
            tipo: Tipo = this.tipo): Persona
    {
        return Persona(idCliente, id, nombreCompleto, tipoDocumento, numeroDocumento, genero, fechaNacimiento, categoria, afiliacion, esAnonima, llaveImagen,empresa,nitEmpresa,tipo)
    }

    override fun copiarConId(idNuevo: Long?): Persona
    {
        return copiar(id = idNuevo)
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Persona

        if (idCliente != other.idCliente) return false
        if (id != other.id) return false
        if (tipoDocumento != other.tipoDocumento) return false
        if (genero != other.genero) return false
        if (fechaNacimiento != other.fechaNacimiento) return false
        if (categoria != other.categoria) return false
        if (afiliacion != other.afiliacion) return false
        if (esAnonima != other.esAnonima) return false
        if (llaveImagen != other.llaveImagen) return false
        if (nombreCompleto != other.nombreCompleto) return false
        if (numeroDocumento != other.numeroDocumento) return false
        if (empresa != other.empresa) return false
        if (nitEmpresa != other.nitEmpresa) return false
        if (tipo != other.tipo) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = idCliente.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + tipoDocumento.hashCode()
        result = 31 * result + genero.hashCode()
        result = 31 * result + fechaNacimiento.hashCode()
        result = 31 * result + categoria.hashCode()
        result = 31 * result + afiliacion.hashCode()
        result = 31 * result + esAnonima.hashCode()
        result = 31 * result + llaveImagen.hashCode()
        result = 31 * result + nombreCompleto.hashCode()
        result = 31 * result + numeroDocumento.hashCode()
        result = 31 * result + empresa.hashCode()
        result = 31 * result + nitEmpresa.hashCode()
        result = 31 * result + tipo.hashCode()
        return result
    }

    override fun toString(): String
    {   /* Respuesta afiliado silvernest*/
        return "Persona(idCliente=$idCliente, id=$id, tipoDocumento=$tipoDocumento, genero=$genero, fechaNacimiento=$fechaNacimiento, categoria=$categoria, afiliacion=$afiliacion, esAnonima=$esAnonima, llaveImagen=$llaveImagen, nombreCompleto='$nombreCompleto', numeroDocumento='$numeroDocumento' ,empresa='$empresa',nitEmpresa='$nitEmpresa',tipo='$tipo')"
    }
    enum class TipoDocumento
    {
        CC,
        CD,
        CE,
        PA,
        RC,
        NIT,
        NUIP,
        TI
    }

    enum class Genero
    {
        MASCULINO,
        FEMENINO,
        DESCONOCIDO
    }

    enum class Categoria
    {
        A,
        B,
        C,
        D
    }

    enum class Afiliacion
    {
        COTIZANTE,
        BENEFICIARIO,
        NO_AFILIADO
    }

    enum class Tipo
    {
        TRABAJADOR,
        FACULTATIVO,
        INDEPENDIENTE,
        PENSIONADO,
        AFILIADO_25_AÑOS,
        CORTESIA,
        NO_AFILIADO
    }

    class CampoNombreCompleto(nombreCompleto: String)
        : CampoEntidad<Persona, String>(nombreCompleto, ValidadorCampoStringNoVacio(), NOMBRE_ENTIDAD, Campos.NOMBRE_COMPLETO)

    class CampoNumeroDocumento(numeroDocumento: String)
        : CampoEntidad<Persona, String>(numeroDocumento, ValidadorCampoStringNoVacio(), NOMBRE_ENTIDAD, Campos.NUMERO_DOCUMENTO)

    class CampoEmpresa(empresa: String)
        : CampoEntidad<Persona, String>(empresa, ValidadorCampoStringNoVacio(), NOMBRE_ENTIDAD, Campos.EMPRESA)

    class CampoNitEmpresa(nitEmpresa: String)
        : CampoEntidad<Persona, String>(nitEmpresa, ValidadorCampoStringNoVacio(), NOMBRE_ENTIDAD, Campos.NIT_EMPRESA)

    class CampoFechaNacimiento(fechaNacimiento: LocalDate)
        : CampoEntidad<Persona, LocalDate>(fechaNacimiento, ValidadorCampoConLimiteSuperior(LocalDate.now(ZONA_HORARIA_POR_DEFECTO)), NOMBRE_ENTIDAD, Campos.FECHA_NACIMIENTO)
}

data class PersonaConGrupoCliente(val persona: Persona, val posibleGrupoCliente: GrupoClientes?)

data class DocumentoCompleto(val tipoDocumento: Persona.TipoDocumento, val numeroDocumento: String)
{
    constructor(persona: Persona) : this(persona.tipoDocumento, persona.numeroDocumento)
}