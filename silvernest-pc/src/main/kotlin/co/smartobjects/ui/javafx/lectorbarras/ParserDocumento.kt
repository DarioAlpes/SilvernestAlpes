package co.smartobjects.ui.javafx.lectorbarras

import co.smartobjects.entidades.personas.Persona
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

class ParserDocumento(private val idCliente: Long, private val contenidoDocumento: String)
{
    private var indicesDocumento: IndicesElementosDocumento? = null
    private var tipoDocumento: Persona.TipoDocumento = Persona.TipoDocumento.CC
    private var consistente: Boolean = true

    companion object
    {
        private const val IDENTIFICADOR_CEDULA = "0"
        private const val IDENTIFICADOR_TARJETA_IDENTIDAD = "I"
        private const val POSICION_INICIAL_TIPO_DOCUMENTO = 0
        private const val POSICION_FINAL_TIPO_DOCUMENTO = 0
        private const val GENERO_MASCULINO = "m"
        private const val GENERO_FEMENINO = "f"

        private const val FORMATO_FECHA_NACIMIENTO = "yyyyMMdd"
    }

    init
    {
        val identificadorDocumento = leerEntre(POSICION_INICIAL_TIPO_DOCUMENTO, POSICION_FINAL_TIPO_DOCUMENTO)
        when (identificadorDocumento)
        {
            IDENTIFICADOR_CEDULA            ->
            {
                indicesDocumento = IndicesElementosDocumento.Cedula()
                tipoDocumento = Persona.TipoDocumento.CC
            }
            IDENTIFICADOR_TARJETA_IDENTIDAD ->
            {
                indicesDocumento = IndicesElementosDocumento.TarjetaIdentidad()
                tipoDocumento = Persona.TipoDocumento.TI
            }
        }
    }

    fun parsearPersona(): Persona?
    {
        if (indicesDocumento != null)
        {
            val indices = indicesDocumento!!
            val numeroDocumento = leerNumeroDocument(indices.indiceInicialDocumento, indices.indiceFinalDocumento)
            val apellidos = leerNombresOApellidos(indices.indiceInicialPrimerApellido, indices.indiceFinalPrimerApellido, indices.indiceInicialSegundoApellido, indices.indiceFinalSegundoApellido)
            val nombres = leerNombresOApellidos(indices.indiceInicialPrimerNombre, indices.indiceFinalPrimerNombre, indices.indiceInicialSegundoNombre, indices.indiceFinalSegundoNombre)
            val genero = leerGenero(indices.indiceInicialGenero, indices.indiceFinalGenero)
            val fechaNacimiento = leerEntre(indices.indiceInicialFechaNacimiento, indices.indiceFinalFechaNacimiento)

            if (consistente)
            {
                return Persona(
                        idCliente,
                        null,
                        "$nombres $apellidos",
                        tipoDocumento,
                        numeroDocumento,
                        genero,
                        LocalDate.parse(fechaNacimiento, DateTimeFormatter.ofPattern(FORMATO_FECHA_NACIMIENTO)),
                        Persona.Categoria.D,
                        Persona.Afiliacion.NO_AFILIADO,
                        false,
                        null,
                        "n/a",
                        "0",
                        Persona.Tipo.NO_AFILIADO
                              )
            }
        }

        return null
    }

    private fun leerNumeroDocument(indiceInicial: Int, indiceFinal: Int): String
    {
        return leerEntre(indiceInicial, indiceFinal).replaceFirst("^0+".toRegex(), "")
    }

    private fun leerGenero(indiceInicial: Int, indiceFinal: Int): Persona.Genero
    {
        val strGenero = leerEntre(indiceInicial, indiceFinal).toLowerCase()
        return when (strGenero)
        {
            GENERO_MASCULINO ->
            {
                Persona.Genero.MASCULINO
            }
            GENERO_FEMENINO  ->
            {
                Persona.Genero.FEMENINO
            }
            else             ->
            {
                Persona.Genero.DESCONOCIDO
            }
        }
    }

    private fun leerNombresOApellidos(
            indiceInicialPrimero: Int, indiceFinalPrimero: Int,
            indiceInicialSegundo: Int, indiceFinalSegundo: Int): String
    {
        val primero = leerEntre(indiceInicialPrimero, indiceFinalPrimero)
        val segundo = leerEntre(indiceInicialSegundo, indiceFinalSegundo)

        return if (!primero.isEmpty())
        {
            primero + if (!segundo.isEmpty()) " $segundo" else ""
        }
        else
        {
            segundo
        }
    }

    private fun leerEntre(indiceInicial: Int, indiceFinal: Int): String
    {
        return if (contenidoDocumento.length > indiceFinal)
        {
            contenidoDocumento.substring(indiceInicial, indiceFinal + 1).replace("\u0000", "").trim()
        }
        else
        {
            consistente = false
            ""
        }
    }
}

private sealed class IndicesElementosDocumento(
        val indiceInicialDocumento: Int,
        val indiceFinalDocumento: Int,
        val indiceInicialPrimerApellido: Int,
        val indiceFinalPrimerApellido: Int,
        val indiceInicialSegundoApellido: Int,
        val indiceFinalSegundoApellido: Int,
        val indiceInicialPrimerNombre: Int,
        val indiceFinalPrimerNombre: Int,
        val indiceInicialSegundoNombre: Int,
        val indiceFinalSegundoNombre: Int,
        val indiceInicialGenero: Int,
        val indiceFinalGenero: Int,
        val indiceInicialFechaNacimiento: Int,
        val indiceFinalFechaNacimiento: Int)
{
    class Cedula : IndicesElementosDocumento(48, 57, 58, 80, 81, 103, 104, 126, 127, 149, 151, 151, 152, 159)
    class TarjetaIdentidad : IndicesElementosDocumento(48, 58, 59, 81, 82, 104, 105, 127, 128, 150, 152, 152, 153, 160)
}