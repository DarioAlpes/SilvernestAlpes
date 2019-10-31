package co.smartobjects.red.modelos.fondos.precios

import co.smartobjects.entidades.fondos.precios.SegmentoClientes


internal const val jsonSegmentoClientesPorDefecto = """{
            "field-name": "DESCONOCIDO",
            "field-value": "DESCONOCIDO"
        }"""
internal const val jsonSegmentoClientesPorDefectoEsperado = """{
            "id": null,
            "field-name": "DESCONOCIDO",
            "field-value": "DESCONOCIDO"
        }"""
@JvmField
internal val segmentoClientesDTOPorDefecto = SegmentoClientesDTO(null, SegmentoClientesDTO.NombreCampo.DESCONOCIDO, "DESCONOCIDO")


internal const val jsonSegmentoClientesConCategoria = """{
            "id": 1,
            "field-name": "CATEGORY",
            "field-value": "A"
        }"""
@JvmField
internal val segmentoClientesDTOConCategoria = SegmentoClientesDTO(1, SegmentoClientesDTO.NombreCampo.CATEGORIA, "A")
@JvmField
internal val segmentoClientesNegocioConCategoria = SegmentoClientes(1, SegmentoClientes.NombreCampo.CATEGORIA, "A")


internal const val jsonSegmentoClientesConGrupoEdad = """{
            "id": 2,
            "field-name": "AGE-GROUP",
            "field-value": "ADULT"
        }"""
@JvmField
internal val segmentoClientesDTOConGrupoEdad = SegmentoClientesDTO(2, SegmentoClientesDTO.NombreCampo.GRUPO_DE_EDAD, "ADULT")
@JvmField
internal val segmentoClientesNegocioConGrupoEdad = SegmentoClientes(2, SegmentoClientes.NombreCampo.GRUPO_DE_EDAD, "ADULT")