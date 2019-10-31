package co.smartobjects.persistencia.personas

import co.smartobjects.entidades.personas.Persona
import co.smartobjects.persistencia.EntidadDAO
import co.smartobjects.persistencia.EntidadReferenciableDAO
import co.smartobjects.persistencia.persistoresormlite.LocalDateThreeTenType
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import org.threeten.bp.LocalDate

@DatabaseTable(tableName = PersonaDAO.TABLA)
internal data class PersonaDAO constructor(
        @DatabaseField(columnName = COLUMNA_ID, generatedId = true, allowGeneratedIdInsert = true)
        override val id: Long? = null,
        @DatabaseField(columnName = COLUMNA_NOMBRE_COMPLETO, canBeNull = false)
        val nombreCompleto: String = "",
        @DatabaseField(columnName = COLUMNA_TIPO_DOCUMENTO, canBeNull = false, uniqueCombo = true, uniqueIndexName = "indice_${TABLA}_documento")
        val tipoDocumento: TipoDocumento = TipoDocumento.CC,
        @DatabaseField(columnName = COLUMNA_NUMERO_DOCUMENTO, canBeNull = false, uniqueCombo = true, uniqueIndexName = "indice_${TABLA}_documento")
        val numeroDocumento: String = "",
        @DatabaseField(columnName = COLUMNA_GENERO, canBeNull = false)
        val genero: Genero = Genero.DESCONOCIDO,
        @DatabaseField(columnName = COLUMNA_FECHA_NACIMIENTO, canBeNull = false, persisterClass = LocalDateThreeTenType::class)
        val fechaNacimiento: LocalDate = LocalDate.now(ZONA_HORARIA_POR_DEFECTO),
        @DatabaseField(columnName = COLUMNA_CATEGORIA, canBeNull = false)
        val categoria: Categoria = Categoria.D,
        @DatabaseField(columnName = COLUMNA_AFILIACION, canBeNull = false)
        val afiliacion: Afiliacion = Afiliacion.COTIZANTE,
        @DatabaseField(columnName = COLUMNA_ES_ANONIMA, canBeNull = false)
        val esAnonima: Boolean = true,
        @DatabaseField(columnName = COLUMNA_LLAVE_IMAGEN)
        val llaveImagen: String? = null,
        @DatabaseField(columnName = COLUMNA_EMPRESA, canBeNull = false)
        val empresa: String = "",
        @DatabaseField(columnName = COLUMNA_NIT_EMPRESA, canBeNull = false)
        val nitEmpresa: String = "",
        @DatabaseField(columnName = COLUMNA_TIPO, canBeNull = false)
        val tipo: Tipo = Tipo.NO_AFILIADO)
    : EntidadDAO<Persona>,
      EntidadReferenciableDAO<Long?>
{
    companion object
    {
        const val TABLA = "persona"
        const val COLUMNA_ID = "id_persona"
        const val COLUMNA_NOMBRE_COMPLETO = "nombre_completo"
        const val COLUMNA_TIPO_DOCUMENTO = "tipo_documento"
        const val COLUMNA_NUMERO_DOCUMENTO = "numero_documento"
        const val COLUMNA_GENERO = "genero"
        const val COLUMNA_FECHA_NACIMIENTO = "fecha_nacimiento"
        const val COLUMNA_CATEGORIA = "categoria"
        const val COLUMNA_AFILIACION = "afiliacion"
        const val COLUMNA_ES_ANONIMA = "es_anonima"
        const val COLUMNA_LLAVE_IMAGEN = "llave_imagen_persona"
        const val COLUMNA_EMPRESA = "empresa"
        const val COLUMNA_NIT_EMPRESA = "nit_empresa"
        const val COLUMNA_TIPO = "tipo"
    }

    constructor(entidadDeNegocio: Persona) : this(
            entidadDeNegocio.id,
            entidadDeNegocio.nombreCompleto,
            TipoDocumento.desdeNegocio(entidadDeNegocio.tipoDocumento),
            entidadDeNegocio.numeroDocumento,
            Genero.desdeNegocio(entidadDeNegocio.genero),
            entidadDeNegocio.fechaNacimiento,
            Categoria.desdeNegocio(entidadDeNegocio.categoria),
            Afiliacion.desdeNegocio(entidadDeNegocio.afiliacion),
            entidadDeNegocio.esAnonima,
            entidadDeNegocio.llaveImagen,
            entidadDeNegocio.empresa,
            entidadDeNegocio.nitEmpresa,
            Tipo.desdeNegocio(entidadDeNegocio.tipo)
                                                 )

    override fun aEntidadDeNegocio(idCliente: Long): Persona
    {   print("\n----------------------------------------------------------------------------------\n")
        return Persona(
                idCliente,
                id,
                nombreCompleto,
                tipoDocumento.valorEnNegocio,
                numeroDocumento,
                genero.valorEnNegocio,
                fechaNacimiento,
                categoria.valorEnNegocio,
                afiliacion.valorEnNegocio,
                esAnonima,
                llaveImagen,
                empresa,
                nitEmpresa,
                tipo.valorEnNegocio
                      )
    }

    // No se agrega valor INVALIDO a las enumeraciones porque el campo esAnonima ya tiene esa información
    enum class TipoDocumento(val valorEnNegocio: Persona.TipoDocumento)
    {
        CC(Persona.TipoDocumento.CC),
        CD(Persona.TipoDocumento.CD),
        CE(Persona.TipoDocumento.CE),
        PA(Persona.TipoDocumento.PA),
        RC(Persona.TipoDocumento.RC),
        NIT(Persona.TipoDocumento.NIT),
        NUIP(Persona.TipoDocumento.NUIP),
        TI(Persona.TipoDocumento.TI);

        companion object
        {
            fun desdeNegocio(valorEnNegocio: Persona.TipoDocumento): TipoDocumento
            {
                return values().first { it.valorEnNegocio == valorEnNegocio }
            }
        }
    }

    enum class Genero(val valorEnNegocio: Persona.Genero)
    {
        MASCULINO(Persona.Genero.MASCULINO),
        FEMENINO(Persona.Genero.FEMENINO),
        DESCONOCIDO(Persona.Genero.DESCONOCIDO);

        companion object
        {
            fun desdeNegocio(valorEnNegocio: Persona.Genero): Genero
            {
                return values().first { it.valorEnNegocio == valorEnNegocio }
            }
        }
    }

    enum class Categoria(val valorEnNegocio: Persona.Categoria)
    {
        A(Persona.Categoria.A),
        B(Persona.Categoria.B),
        C(Persona.Categoria.C),
        D(Persona.Categoria.D);

        companion object
        {
            fun desdeNegocio(valorEnNegocio: Persona.Categoria): Categoria
            {
                return values().first { it.valorEnNegocio == valorEnNegocio }
            }
        }
    }

    enum class Afiliacion(val valorEnNegocio: Persona.Afiliacion)
    {
        COTIZANTE(Persona.Afiliacion.COTIZANTE),
        BENEFICIARIO(Persona.Afiliacion.BENEFICIARIO),
        NO_AFILIADO(Persona.Afiliacion.NO_AFILIADO);

        companion object
        {
            fun desdeNegocio(valorEnNegocio: Persona.Afiliacion): Afiliacion
            {
                return values().first { it.valorEnNegocio == valorEnNegocio }
            }
        }
    }

    enum class Tipo(val valorEnNegocio: Persona.Tipo)
    {
        TRABAJADOR(Persona.Tipo.TRABAJADOR),
        FACULTATIVO(Persona.Tipo.FACULTATIVO),
        INDEPENDIENTE(Persona.Tipo.INDEPENDIENTE),
        PENSIONADO(Persona.Tipo.PENSIONADO),
        AFILIADO_25_AÑOS(Persona.Tipo.AFILIADO_25_AÑOS),
        CORTESIA(Persona.Tipo.CORTESIA),
        NO_AFILIADO(Persona.Tipo.NO_AFILIADO);

        companion object
        {
            fun desdeNegocio(valorEnNegocio: Persona.Tipo): Tipo
            {
                return values().first { it.valorEnNegocio == valorEnNegocio }
            }
        }
    }
}