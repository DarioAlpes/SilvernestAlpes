package co.smartobjects.red

import co.smartobjects.red.modelos.fondos.FondoDTO
import co.smartobjects.red.modelos.fondos.libros.IProhibicionDTO
import co.smartobjects.red.modelos.fondos.libros.IReglaDTO
import co.smartobjects.red.modelos.fondos.libros.LibroDTO
import co.smartobjects.red.modelos.fondos.precios.SegmentoClientesDTO
import co.smartobjects.red.modelos.operativas.compras.PagoDTO
import co.smartobjects.red.modelos.operativas.ordenes.TransaccionDTO
import co.smartobjects.red.modelos.personas.CampoDePersonaDTO
import co.smartobjects.red.modelos.personas.PersonaDTO
import co.smartobjects.red.modelos.ubicaciones.UbicacionDTO
import co.smartobjects.red.modelos.usuarios.PermisoBackDTO
import co.smartobjects.utilidades.Decimal
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.paranamer.ParanamerModule
import org.threeten.bp.LocalDate
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.io.IOException


object ConfiguracionJackson
{
    @JvmField
    val objectMapperDeJackson = ObjectMapper().apply {
        disable(
                MapperFeature.AUTO_DETECT_CREATORS,
                MapperFeature.AUTO_DETECT_FIELDS,
                MapperFeature.AUTO_DETECT_GETTERS,
                MapperFeature.AUTO_DETECT_IS_GETTERS
               )
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

        registerModule(ParanamerModule())
        registerModule(KotlinModule())

        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
        enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)

        registerModule(
                SimpleModule().apply {
                    addSerializer(UbicacionDTO.Tipo::class.java, TipoUbicacionSerializer())
                    addDeserializer(UbicacionDTO.Tipo::class.java, TipoUbicacionDeserializer())
                }
                      )

        registerModule(
                SimpleModule().apply {
                    addSerializer(UbicacionDTO.Subtipo::class.java, SubTipoUbicacionSerializer())
                    addDeserializer(UbicacionDTO.Subtipo::class.java, SubTipoUbicacionDeserializer())
                }
                      )

        registerModule(
                SimpleModule().apply {
                    addSerializer(FondoDTO.TipoDeFondoEnRed::class.java, TipoDeFondoEnRedSerializer())
                    addDeserializer(FondoDTO.TipoDeFondoEnRed::class.java, TipoDeFondoEnRedDeserializer())
                }
                      )

        registerModule(
                SimpleModule().apply {
                    addSerializer(SegmentoClientesDTO.NombreCampo::class.java, SegmentoNombreCampoEnRedSerializer())
                    addDeserializer(SegmentoClientesDTO.NombreCampo::class.java, SegmentoNombreCampoEnRedDeserializer())
                }
                      )

        registerModule(
                SimpleModule().apply {
                    addSerializer(CampoDePersonaDTO.Predeterminado::class.java, CampoDePersonaCampoEnRedSerializer())
                    addDeserializer(CampoDePersonaDTO.Predeterminado::class.java, CampoDePersonaCampoEnRedDeserializer())
                }
                      )

        registerModule(
                SimpleModule().apply {
                    addSerializer(PersonaDTO.TipoDocumento::class.java, TipoDocumentoEnRedSerializer())
                    addDeserializer(PersonaDTO.TipoDocumento::class.java, TipoDocumentoEnRedDeserializer())
                }
                      )

        registerModule(
                SimpleModule().apply {
                    addSerializer(PersonaDTO.Genero::class.java, GeneroEnRedSerializer())
                    addDeserializer(PersonaDTO.Genero::class.java, GeneroEnRedDeserializer())
                }
                      )

        registerModule(
                SimpleModule().apply {
                    addSerializer(PersonaDTO.Categoria::class.java, CategoriaEnRedSerializer())
                    addDeserializer(PersonaDTO.Categoria::class.java, CategoriaEnRedDeserializer())
                }
                      )

        registerModule(
                SimpleModule().apply {
                    addSerializer(PersonaDTO.Afiliacion::class.java, AfiliacionEnRedSerializer())
                    addDeserializer(PersonaDTO.Afiliacion::class.java, AfiliacionEnRedDeserializer())
                }
                      )

        registerModule(
                SimpleModule().apply {
                    addSerializer(PermisoBackDTO.AccionDTO::class.java, AccionPermisoDTOSerializer())
                    addDeserializer(PermisoBackDTO.AccionDTO::class.java, AccionPermisoDTODeserializer())
                }
                      )

        registerModule(
                SimpleModule().apply {
                    addSerializer(LibroDTO.Tipo::class.java, TipoDeLibroEnRedSerializer())
                    addDeserializer(LibroDTO.Tipo::class.java, TipoDeLibroEnRedDeserializer())
                }
                      )

        registerModule(
                SimpleModule().apply {
                    addSerializer(IReglaDTO.Tipo::class.java, TipoDeReglaEnRedSerializer())
                    addDeserializer(IReglaDTO.Tipo::class.java, TipoDeReglaEnRedDeserializer())
                }
                      )

        registerModule(
                SimpleModule().apply {
                    addSerializer(IProhibicionDTO.Tipo::class.java, TipoDeProhibicionEnRedSerializer())
                    addDeserializer(IProhibicionDTO.Tipo::class.java, TipoDeProhibicionEnRedDeserializer())
                }
                      )

        registerModule(
                SimpleModule().apply {
                    addSerializer(TransaccionDTO.Tipo::class.java, TipoDeTransaccionSerializer())
                    addDeserializer(TransaccionDTO.Tipo::class.java, TipoDeTransaccionDeserializer())
                }
                      )

        registerModule(
                SimpleModule().apply {
                    addSerializer(PagoDTO.MetodoDePago::class.java, MetodoDePagoEnRedSerializer())
                    addDeserializer(PagoDTO.MetodoDePago::class.java, MetodoDePagoEnRedDeserializer())
                }
                      )

        registerModule(
                SimpleModule().apply {
                    addSerializer(Decimal::class.java, DecimalSerializer())
                    addDeserializer(Decimal::class.java, DecimalDeserializer())
                }
                      )

        registerModule(
                SimpleModule().apply {
                    addSerializer(Sequence::class.java, SequenceSerializer())
                }
                      )

        registerModule(
                SimpleModule().apply {
                    addSerializer(ZonedDateTime::class.java, ZonedDateTimeSerializer())
                    addDeserializer(ZonedDateTime::class.java, ZonedDateTimeDeserializer())
                }
                      )

        registerModule(
                SimpleModule().apply {
                    addSerializer(LocalDate::class.java, LocalDateSerializer())
                    addDeserializer(LocalDate::class.java, LocalDateDeserializer())
                }
                      )
    }

    internal class TipoUbicacionSerializer : JsonSerializer<UbicacionDTO.Tipo>()
    {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun serialize(value: UbicacionDTO.Tipo, jgen: JsonGenerator, provider: SerializerProvider)
        {
            jgen.writeString(value.valorEnRed)
        }
    }

    internal class TipoUbicacionDeserializer : JsonDeserializer<UbicacionDTO.Tipo>()
    {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): UbicacionDTO.Tipo
        {
            return UbicacionDTO.Tipo.values().first { it.valorEnRed == jp.valueAsString }
        }
    }

    internal class SubTipoUbicacionSerializer : JsonSerializer<UbicacionDTO.Subtipo>()
    {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun serialize(value: UbicacionDTO.Subtipo, jgen: JsonGenerator, provider: SerializerProvider)
        {
            jgen.writeString(value.valorEnRed)
        }
    }

    internal class SubTipoUbicacionDeserializer : JsonDeserializer<UbicacionDTO.Subtipo>()
    {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): UbicacionDTO.Subtipo
        {
            return UbicacionDTO.Subtipo.values().first { it.valorEnRed == jp.valueAsString }
        }
    }

    internal class TipoDeFondoEnRedSerializer : JsonSerializer<FondoDTO.TipoDeFondoEnRed>()
    {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun serialize(value: FondoDTO.TipoDeFondoEnRed, jgen: JsonGenerator, provider: SerializerProvider)
        {
            jgen.writeString(value.valorEnRed)
        }
    }

    internal class TipoDeFondoEnRedDeserializer : JsonDeserializer<FondoDTO.TipoDeFondoEnRed>()
    {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): FondoDTO.TipoDeFondoEnRed
        {
            return FondoDTO.TipoDeFondoEnRed.values().first { it.valorEnRed == jp.valueAsString }
        }
    }

    internal class SegmentoNombreCampoEnRedSerializer : JsonSerializer<SegmentoClientesDTO.NombreCampo>()
    {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun serialize(value: SegmentoClientesDTO.NombreCampo, jgen: JsonGenerator, provider: SerializerProvider)
        {
            jgen.writeString(value.valorEnRed)
        }
    }

    internal class SegmentoNombreCampoEnRedDeserializer : JsonDeserializer<SegmentoClientesDTO.NombreCampo>()
    {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): SegmentoClientesDTO.NombreCampo
        {
            return SegmentoClientesDTO.NombreCampo.values().first { it.valorEnRed == jp.valueAsString }
        }
    }

    internal class CampoDePersonaCampoEnRedSerializer : JsonSerializer<CampoDePersonaDTO.Predeterminado>()
    {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun serialize(value: CampoDePersonaDTO.Predeterminado, jgen: JsonGenerator, provider: SerializerProvider)
        {
            jgen.writeString(value.valorEnRed)
        }
    }

    internal class CampoDePersonaCampoEnRedDeserializer : JsonDeserializer<CampoDePersonaDTO.Predeterminado>()
    {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): CampoDePersonaDTO.Predeterminado
        {
            return CampoDePersonaDTO.Predeterminado.values().first { it.valorEnRed == jp.valueAsString }
        }
    }

    internal class TipoDocumentoEnRedSerializer : JsonSerializer<PersonaDTO.TipoDocumento>()
    {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun serialize(value: PersonaDTO.TipoDocumento, jgen: JsonGenerator, provider: SerializerProvider)
        {
            jgen.writeString(value.valorEnRed)
        }
    }

    internal class TipoDocumentoEnRedDeserializer : JsonDeserializer<PersonaDTO.TipoDocumento>()
    {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): PersonaDTO.TipoDocumento
        {
            return PersonaDTO.TipoDocumento.values().first { it.valorEnRed == jp.valueAsString }
        }
    }

    internal class GeneroEnRedSerializer : JsonSerializer<PersonaDTO.Genero>()
    {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun serialize(value: PersonaDTO.Genero, jgen: JsonGenerator, provider: SerializerProvider)
        {
            jgen.writeString(value.valorEnRed)
        }
    }

    internal class GeneroEnRedDeserializer : JsonDeserializer<PersonaDTO.Genero>()
    {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): PersonaDTO.Genero
        {
            return PersonaDTO.Genero.values().first { it.valorEnRed == jp.valueAsString }
        }
    }

    internal class MetodoDePagoEnRedSerializer : JsonSerializer<PagoDTO.MetodoDePago>()
    {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun serialize(value: PagoDTO.MetodoDePago, jgen: JsonGenerator, provider: SerializerProvider)
        {
            jgen.writeString(value.valorEnRed)
        }
    }

    internal class MetodoDePagoEnRedDeserializer : JsonDeserializer<PagoDTO.MetodoDePago>()
    {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): PagoDTO.MetodoDePago
        {
            return PagoDTO.MetodoDePago.values().first { it.valorEnRed == jp.valueAsString }
        }
    }

    internal class CategoriaEnRedSerializer : JsonSerializer<PersonaDTO.Categoria>()
    {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun serialize(value: PersonaDTO.Categoria, jgen: JsonGenerator, provider: SerializerProvider)
        {
            jgen.writeString(value.valorEnRed)
        }
    }

    internal class CategoriaEnRedDeserializer : JsonDeserializer<PersonaDTO.Categoria>()
    {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): PersonaDTO.Categoria
        {
            return PersonaDTO.Categoria.values().first { it.valorEnRed == jp.valueAsString }
        }
    }

    internal class AfiliacionEnRedSerializer : JsonSerializer<PersonaDTO.Afiliacion>()
    {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun serialize(value: PersonaDTO.Afiliacion, jgen: JsonGenerator, provider: SerializerProvider)
        {
            jgen.writeString(value.valorEnRed)
        }
    }

    internal class AfiliacionEnRedDeserializer : JsonDeserializer<PersonaDTO.Afiliacion>()
    {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): PersonaDTO.Afiliacion
        {
            return PersonaDTO.Afiliacion.values().first { it.valorEnRed == jp.valueAsString }
        }
    }

    internal class AccionPermisoDTOSerializer : JsonSerializer<PermisoBackDTO.AccionDTO>()
    {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun serialize(value: PermisoBackDTO.AccionDTO, jgen: JsonGenerator, provider: SerializerProvider)
        {
            jgen.writeString(value.valorEnRed)
        }
    }

    internal class AccionPermisoDTODeserializer : JsonDeserializer<PermisoBackDTO.AccionDTO>()
    {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): PermisoBackDTO.AccionDTO
        {
            return PermisoBackDTO.AccionDTO.values().first { it.valorEnRed == jp.valueAsString }
        }
    }

    internal class TipoDeLibroEnRedSerializer : JsonSerializer<LibroDTO.Tipo>()
    {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun serialize(value: LibroDTO.Tipo, jgen: JsonGenerator, provider: SerializerProvider)
        {
            jgen.writeString(value.valorEnRed)
        }
    }

    internal class TipoDeLibroEnRedDeserializer : JsonDeserializer<LibroDTO.Tipo>()
    {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): LibroDTO.Tipo
        {
            return LibroDTO.Tipo.values().first { it.valorEnRed == jp.valueAsString }
        }
    }

    internal class TipoDeReglaEnRedSerializer : JsonSerializer<IReglaDTO.Tipo>()
    {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun serialize(value: IReglaDTO.Tipo, jgen: JsonGenerator, provider: SerializerProvider)
        {
            jgen.writeString(value.valorEnRed)
        }
    }

    internal class TipoDeReglaEnRedDeserializer : JsonDeserializer<IReglaDTO.Tipo>()
    {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): IReglaDTO.Tipo
        {
            return IReglaDTO.Tipo.values().first { it.valorEnRed == jp.valueAsString }
        }
    }

    internal class TipoDeProhibicionEnRedSerializer : JsonSerializer<IProhibicionDTO.Tipo>()
    {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun serialize(value: IProhibicionDTO.Tipo, jgen: JsonGenerator, provider: SerializerProvider)
        {
            jgen.writeString(value.valorEnRed)
        }
    }

    internal class TipoDeProhibicionEnRedDeserializer : JsonDeserializer<IProhibicionDTO.Tipo>()
    {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): IProhibicionDTO.Tipo
        {
            return IProhibicionDTO.Tipo.values().first { it.valorEnRed == jp.valueAsString }
        }
    }

    internal class TipoDeTransaccionSerializer : JsonSerializer<TransaccionDTO.Tipo>()
    {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun serialize(value: TransaccionDTO.Tipo, jgen: JsonGenerator, provider: SerializerProvider)
        {
            jgen.writeString(value.valorEnRed)
        }
    }

    internal class TipoDeTransaccionDeserializer : JsonDeserializer<TransaccionDTO.Tipo>()
    {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): TransaccionDTO.Tipo
        {
            return TransaccionDTO.Tipo.values().first { it.valorEnRed == jp.valueAsString }
        }
    }



    internal class DecimalSerializer : JsonSerializer<Decimal?>()
    {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun serialize(value: Decimal?, jgen: JsonGenerator, provider: SerializerProvider)
        {
            jgen.writeNumber(value?.toString())
        }
    }

    internal class DecimalDeserializer : JsonDeserializer<Decimal?>()
    {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Decimal?
        {
            val valorComoString = jp.valueAsString
            return if (valorComoString != null)
            {
                Decimal(valorComoString)
            }
            else
            {
                null
            }
        }
    }

    internal class ZonedDateTimeSerializer : JsonSerializer<ZonedDateTime?>()
    {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun serialize(value: ZonedDateTime?, jgen: JsonGenerator, provider: SerializerProvider)
        {
            jgen.writeString(value?.format(DateTimeFormatter.ISO_ZONED_DATE_TIME))
        }
    }

    internal class ZonedDateTimeDeserializer : JsonDeserializer<ZonedDateTime?>()
    {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): ZonedDateTime?
        {
            val valorComoString = jp.valueAsString
            return if (valorComoString != null)
            {
                ZonedDateTime.parse(valorComoString, DateTimeFormatter.ISO_ZONED_DATE_TIME)
            }
            else
            {
                null
            }
        }
    }

    internal class LocalDateSerializer : JsonSerializer<LocalDate?>()
    {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun serialize(value: LocalDate?, jgen: JsonGenerator, provider: SerializerProvider)
        {
            jgen.writeString(value?.format(DateTimeFormatter.ISO_DATE))
        }
    }

    internal class LocalDateDeserializer : JsonDeserializer<LocalDate?>()
    {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): LocalDate?
        {
            val valorComoString = jp.valueAsString
            return if (valorComoString != null)
            {
                LocalDate.parse(valorComoString, DateTimeFormatter.ISO_DATE)
            }
            else
            {
                null
            }
        }
    }

    internal class SequenceSerializer : JsonSerializer<Sequence<*>>()
    {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun serialize(value: Sequence<*>, jgen: JsonGenerator, provider: SerializerProvider)
        {
            jgen.writeStartArray()
            value.forEach {
                jgen.writeObject(it)
            }
            jgen.writeEndArray()
        }
    }
}