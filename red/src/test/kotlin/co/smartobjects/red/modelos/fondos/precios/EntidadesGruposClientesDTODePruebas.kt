package co.smartobjects.red.modelos.fondos.precios

import co.smartobjects.entidades.fondos.precios.GrupoClientes

internal const val jsonGrupoClientesPorDefecto = """{
            "name": "Por Defecto",
            "clients-segments": []
        }"""
internal const val jsonGrupoClientesPorDefectoEsperado = """{
            "id": null,
            "name": "Por Defecto",
            "clients-segments": []
        }"""

@JvmField
internal val grupoClientesDTOPorDefecto = GrupoClientesDTO(null, "Por Defecto", listOf())


internal const val jsonGrupoClientesConCategoria = """{
            "id": 1,
            "name": "Grupo por categoria",
            "clients-segments": [$jsonSegmentoClientesConCategoria]
        }"""

@JvmField
internal val grupoClientesDTOConCategoria = GrupoClientesDTO(1, "Grupo por categoria", listOf(segmentoClientesDTOConCategoria))

@JvmField
internal val grupoClientesNegocioConCategoria = GrupoClientes(1, "Grupo por categoria", listOf(segmentoClientesNegocioConCategoria))


internal const val jsonGrupoClientesConGrupoEdad = """{
            "id": 1,
            "name": "Grupo por edad",
            "clients-segments": [$jsonSegmentoClientesConGrupoEdad]
        }"""

@JvmField
internal val grupoClientesDTOConGrupoEdad = GrupoClientesDTO(1, "Grupo por edad", listOf(segmentoClientesDTOConGrupoEdad))

@JvmField
internal val grupoClientesNegocioConGrupoEdad = GrupoClientes(1, "Grupo por edad", listOf(segmentoClientesNegocioConGrupoEdad))


internal const val jsonGrupoClientesConGrupoEdadYCategoria = """{
            "id": 1,
            "name": "Grupo por edad y categoria",
            "clients-segments": [$jsonSegmentoClientesConGrupoEdad, $jsonSegmentoClientesConCategoria]
        }"""

@JvmField
internal val grupoClientesDTOConGrupoEdadYCategoria = GrupoClientesDTO(1, "Grupo por edad y categoria", listOf(segmentoClientesDTOConGrupoEdad, segmentoClientesDTOConCategoria))

@JvmField
internal val grupoClientesNegocioConGrupoEdadYCategoria = GrupoClientes(1, "Grupo por edad y categoria", listOf(segmentoClientesNegocioConGrupoEdad, segmentoClientesNegocioConCategoria))