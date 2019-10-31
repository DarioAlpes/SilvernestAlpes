package co.smartobjects.red.modelos.operativas.compras

import co.smartobjects.entidades.operativas.EntidadTransaccional
import co.smartobjects.entidades.operativas.compras.Compra
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZonedDateTime
import java.util.*

internal const val FECHA_REALIZACION_DEFECTO_STR = "2000-01-02T03:04:05-05:00[UTC-05:00]"
private val FECHA_REALIZACION_DEFECTO = ZonedDateTime.of(LocalDate.of(2000, 1, 2), LocalTime.of(3, 4, 5), ZONA_HORARIA_POR_DEFECTO)
private val UUID_DEFECTO = UUID.randomUUID()
private const val TIEMPO_DEFECTO = 1234567L
private const val NOMBRE_USUARIO_DEFECTO = "Usuario"
internal val ID_COMPRA_DEFECTO = EntidadTransaccional.PartesId(TIEMPO_DEFECTO, NOMBRE_USUARIO_DEFECTO, UUID_DEFECTO).id

internal val jsonCompraPorDefecto = """{
        "id": "$ID_COMPRA_DEFECTO",
        "fund-credits": [],
        "package-credits": [],
        "payments": [],
        "operation-datetime": "$FECHA_REALIZACION_DEFECTO_STR"
    } """

internal val jsonCompraPorDefectoEsperado = """{
        "client-id": 0,
        "id": "$ID_COMPRA_DEFECTO",
        "committed": false,
        "fund-credits": [],
        "package-credits": [],
        "payments": [],
        "operation-datetime": "$FECHA_REALIZACION_DEFECTO_STR"
    } """

internal val compraDTOPorDefecto = CompraDTO(
        0,
        ID_COMPRA_DEFECTO,
        false,
        listOf(),
        listOf(),
        listOf(),
        FECHA_REALIZACION_DEFECTO
                                            )

internal val jsonCompraConNulos = """{
        "client-id": 10,
        "id": "$ID_COMPRA_DEFECTO",
        "committed": false,
        "fund-credits": [$jsonCreditoFondoConNulos],
        "package-credits": [$jsonCreditoPaqueteConNulos],
        "payments": [$jsonPagoConNulos],
        "operation-datetime": "$FECHA_REALIZACION_DEFECTO_STR"
    } """

internal val compraDTOConNulos = CompraDTO(
        10,
        ID_COMPRA_DEFECTO,
        false,
        listOf(creditoFondoDTOConNulos),
        listOf(creditoPaqueteDTOConNulos),
        listOf(pagoDTOConNulos),
        FECHA_REALIZACION_DEFECTO
                                          )

internal val compraNegocioConNulos = Compra(
        10,
        NOMBRE_USUARIO_DEFECTO,
        UUID_DEFECTO,
        TIEMPO_DEFECTO,
        false,
        listOf(creditoFondoNegocioConNulos),
        listOf(creditoPaqueteNegocioConNulos),
        listOf(pagoNegocioConNulos),
        FECHA_REALIZACION_DEFECTO
                                           )

internal val jsonCompraSinNulos = """{
        "client-id": 20,
        "id": "$ID_COMPRA_DEFECTO",
        "committed": true,
        "fund-credits": [$jsonCreditoFondoSinNulos, $jsonCreditoFondoSinNulos],
        "package-credits": [$jsonCreditoPaqueteSinNulos, $jsonCreditoPaqueteSinNulos],
        "payments": [$jsonPagoSinNulos, $jsonPagoSinNulos],
        "operation-datetime": "$FECHA_REALIZACION_DEFECTO_STR"
    } """

internal val compraDTOSinNulos = CompraDTO(
        20,
        ID_COMPRA_DEFECTO,
        true,
        listOf(creditoFondoDTOSinNulos, creditoFondoDTOSinNulos),
        listOf(creditoPaqueteDTOSinNulos, creditoPaqueteDTOSinNulos),
        listOf(pagoDTOSinNulos, pagoDTOSinNulos),
        FECHA_REALIZACION_DEFECTO
                                          )

internal val compraNegocioSinNulos = Compra(
        20,
        NOMBRE_USUARIO_DEFECTO,
        UUID_DEFECTO,
        TIEMPO_DEFECTO,
        true,
        listOf(creditoFondoNegocioSinNulos, creditoFondoNegocioSinNulos),
        listOf(creditoPaqueteNegocioSinNulos, creditoPaqueteNegocioSinNulos),
        listOf(pagoNegocioSinNulos, pagoNegocioSinNulos),
        FECHA_REALIZACION_DEFECTO
                                           )