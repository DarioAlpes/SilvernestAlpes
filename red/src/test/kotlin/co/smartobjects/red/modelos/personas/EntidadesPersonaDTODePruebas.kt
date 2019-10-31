package co.smartobjects.red.modelos.personas

import co.smartobjects.entidades.personas.Persona
import org.threeten.bp.LocalDate


internal const val jsonPersonaPorDefecto = """
            {
                "client-id": 0,
                "full-name": "Por Defecto",
                "document-type": "CC",
                "document-number": "Por Defecto",
                "gender": "UNKNOWN",
                "birthdate": "2017-05-31",
                "category": "A",
                "affiliation": "CONTRIBUTOR",
                "is-anonymous": false,
                "image-key": "llave"
            }
            """

internal const val jsonPersonaPorDefectoEsperado = """
            {
                "client-id": 0,
                "id": null,
                "full-name": "Por Defecto",
                "document-type": "CC",
                "document-number": "Por Defecto",
                "gender": "UNKNOWN",
                "birthdate": "2017-05-31",
                "category": "A",
                "affiliation": "CONTRIBUTOR",
                "is-anonymous": false,
                "image-key": "llave"
            }
            """

internal val personaDTOPorDefecto = PersonaDTO(
        0,
        null,
        "Por Defecto",
        PersonaDTO.TipoDocumento.CC,
        "Por Defecto",
        PersonaDTO.Genero.DESCONOCIDO,
        LocalDate.of(2017, 5, 31),
        PersonaDTO.Categoria.A,
        PersonaDTO.Afiliacion.COTIZANTE,
        false,
        "llave",
        "Por Defecto",
        "0",
        PersonaDTO.Tipo.NO_AFILIADO
                                              )

internal val personaNegocioPorDefecto = Persona(
        0,
        null,
        "Por Defecto",
        Persona.TipoDocumento.CC,
        "Por Defecto",
        Persona.Genero.DESCONOCIDO,
        LocalDate.of(2017, 5, 31),
        Persona.Categoria.A,
        Persona.Afiliacion.COTIZANTE,
        false,
        "llave",
        "Por Defecto",
        "0",
        Persona.Tipo.NO_AFILIADO
                                               )

internal const val jsonPersonaConNulos = """
            {
                "client-id": 0,
                "id": null,
                "full-name": "Por Defecto",
                "document-type": "CC",
                "document-number": "Por Defecto",
                "gender": "UNKNOWN",
                "birthdate": "2017-05-31",
                "category": "A",
                "affiliation": "CONTRIBUTOR",
                "is-anonymous": false,
                "image-key": "llave",
                "company": "n/a",
                "company_nit": "0",
                "type of worker"
            }
            """

internal val personaDTOConNulos = personaDTOPorDefecto

internal val personaNegocioConNulos = personaNegocioPorDefecto


internal const val jsonPersonaSinNulos = """
            {
                "client-id": 0,
                "id": 0,
                "full-name": "Por Defecto",
                "document-type": "CC",
                "document-number": "Por Defecto",
                "gender": "UNKNOWN",
                "birthdate": "2017-05-31",
                "category": "A",
                "affiliation": "CONTRIBUTOR",
                "is-anonymous": false,
                "image-key": "llave",
                "company": "n/a",
                "company_nit": "0",
                "type of worker"
            }
            """

internal val personaDTOSinNulos = PersonaDTO(
        0,
        0,
        "Por Defecto",
        PersonaDTO.TipoDocumento.CC,
        "Por Defecto",
        PersonaDTO.Genero.DESCONOCIDO,
        LocalDate.of(2017, 5, 31),
        PersonaDTO.Categoria.A,
        PersonaDTO.Afiliacion.COTIZANTE,
        false,
        "llave",
        "Por Defecto",
        "0",
        PersonaDTO.Tipo.NO_AFILIADO
                                            )

internal val personaNegocioSinNulos = Persona(
        0,
        0,
        "Por Defecto",
        Persona.TipoDocumento.CC,
        "Por Defecto",
        Persona.Genero.DESCONOCIDO,
        LocalDate.of(2017, 5, 31),
        Persona.Categoria.A,
        Persona.Afiliacion.COTIZANTE,
        false,
        "llave",
        "Por Defecto",
        "0",
        Persona.Tipo.NO_AFILIADO
                                             )