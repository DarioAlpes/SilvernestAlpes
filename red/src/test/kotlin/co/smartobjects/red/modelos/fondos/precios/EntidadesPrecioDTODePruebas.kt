package co.smartobjects.red.modelos.fondos.precios

import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.utilidades.Decimal

internal const val jsonPrecioPorDefecto = """{ "value": 0, "tax-id": 0 }"""

@JvmField
internal val precioDTOPorDefecto = PrecioDTO(Decimal.CERO, 0)

@JvmField
internal val precioNegocioPorDefecto = Precio(Decimal.CERO, 0)

internal const val jsonPrecioConNulos = jsonPrecioPorDefecto

@JvmField
internal val precioDTOConNulos = precioDTOPorDefecto

@JvmField
internal val precioNegocioConNulos = precioNegocioPorDefecto

internal const val jsonPrecioSinNulos = """{ "value": 100.5, "tax-id": 4 }"""

@JvmField
internal val precioDTOSinNulos = PrecioDTO(Decimal(100.5), 4)

@JvmField
internal val precioNegocioSinNulos = Precio(Decimal(100.5), 4)