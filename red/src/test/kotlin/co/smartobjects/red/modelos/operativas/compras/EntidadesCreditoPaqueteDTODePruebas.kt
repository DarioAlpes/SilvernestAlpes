package co.smartobjects.red.modelos.operativas.compras

import co.smartobjects.entidades.operativas.compras.CreditoPaquete

internal const val jsonCreditoPaquetePorDefecto = """{ "package-id": 0, "package-external-code": "código externo paquete", "credits": [] }"""

@JvmField
internal val creditoPaqueteDTOPorDefecto = CreditoPaqueteDTO(0, "código externo paquete", listOf())

internal const val jsonCreditoPaqueteConNulos = """{ "package-id": 1, "package-external-code": "código externo paquete 1", "credits": [$jsonCreditoFondoConNulos] }"""

@JvmField
internal val creditoPaqueteDTOConNulos = CreditoPaqueteDTO(1, "código externo paquete 1", listOf(creditoFondoDTOConNulos))

@JvmField
internal val creditoPaqueteNegocioConNulos = CreditoPaquete(1, "código externo paquete 1", listOf(creditoFondoNegocioConNulos))

internal const val jsonCreditoPaqueteSinNulos = """{ "package-id": 2, "package-external-code": "código externo paquete 2", "credits": [$jsonCreditoFondoSinNulos, $jsonCreditoFondoSinNulos]}"""

@JvmField
internal val creditoPaqueteDTOSinNulos = CreditoPaqueteDTO(2, "código externo paquete 2", listOf(creditoFondoDTOSinNulos, creditoFondoDTOSinNulos))

@JvmField
internal val creditoPaqueteNegocioSinNulos = CreditoPaquete(2, "código externo paquete 2", listOf(creditoFondoNegocioSinNulos, creditoFondoNegocioSinNulos))