package co.smartobjects.red.modelos.operativas.compras

import co.smartobjects.entidades.operativas.compras.Pago
import co.smartobjects.utilidades.Decimal

internal const val jsonPagoPorDefecto = """{ "value": 0, "method": "CREDIT_CARD", "pos-transaction-number": "12-3"}"""

@JvmField
internal val pagoDTOPorDefecto = PagoDTO(Decimal.CERO, PagoDTO.MetodoDePago.TARJETA_CREDITO, "12-3")

@JvmField
internal val pagoNegocioPorDefecto = Pago(Decimal.CERO, Pago.MetodoDePago.TARJETA_CREDITO, "12-3")

internal const val jsonPagoConNulos = jsonPagoPorDefecto

@JvmField
internal val pagoDTOConNulos = pagoDTOPorDefecto

@JvmField
internal val pagoNegocioConNulos = pagoNegocioPorDefecto

internal const val jsonPagoSinNulos = """{ "value": 100.5, "method": "CASH", "pos-transaction-number": "45-6" }"""

@JvmField
internal val pagoDTOSinNulos = PagoDTO(Decimal(100.5), PagoDTO.MetodoDePago.EFECTIVO, "45-6")

@JvmField
internal val pagoNegocioSinNulos = Pago(Decimal(100.5), Pago.MetodoDePago.EFECTIVO, "45-6")