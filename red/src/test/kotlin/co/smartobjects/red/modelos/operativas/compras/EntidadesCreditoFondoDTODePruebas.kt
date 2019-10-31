package co.smartobjects.red.modelos.operativas.compras

import co.smartobjects.entidades.operativas.compras.CreditoFondo
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZonedDateTime

internal const val FECHA_DESDE_DEFECTO_STR = "2000-01-02T03:04:05-05:00[UTC-05:00]"
internal val FECHA_DESDE_DEFECTO = ZonedDateTime.of(LocalDate.of(2000, 1, 2), LocalTime.of(3, 4, 5), ZONA_HORARIA_POR_DEFECTO)
internal const val FECHA_HASTA_DEFECTO_STR = "2001-02-03T03:04:05-05:00[UTC-05:00]"
internal val FECHA_HASTA_DEFECTO = ZonedDateTime.of(LocalDate.of(2001, 2, 3), LocalTime.of(3, 4, 5), ZONA_HORARIA_POR_DEFECTO)

internal const val jsonCreditoFondoPorDefecto = """{
        "amount": 0,
        "price-paid": 0,
        "tax-paid": 0,
        "source": "taquilla",
        "username": "usuario",
        "person-id": 1,
        "fund-id": 2,
        "fund-external-code": "código externo fondo",
        "tax-id": 3,
        "device-id": "un-uuid",
        "location-id": null,
        "customer-group-id": null
    } """

internal const val jsonCreditoFondoPorDefectoEsperado = """{
        "client-id": 0,
        "id": null ,
        "amount": 0,
        "price-paid": 0,
        "tax-paid": 0,
        "valid-from": null,
        "valid-until": null,
        "consumed": false,
        "source": "taquilla",
        "username": "usuario",
        "person-id": 1,
        "fund-id": 2,
        "fund-external-code": "código externo fondo",
        "tax-id": 3,
        "device-id": "un-uuid",
        "location-id": null,
        "customer-group-id": null
    } """

@JvmField
internal val creditoFondoDTOPorDefecto = CreditoFondoDTO(
        0,
        null,
        Decimal.CERO,
        Decimal.CERO,
        Decimal.CERO,
        null,
        null,
        false,
        "taquilla",
        "usuario",
        1,
        2,
        "código externo fondo",
        3,
        "un-uuid",
        null,
        null
                                                        )

@JvmField
internal val creditoFondoNegocioPorDefecto = CreditoFondo(
        0,
        null,
        Decimal.CERO,
        Decimal.CERO,
        Decimal.CERO,
        null,
        null,
        false,
        "taquilla",
        "usuario",
        1,
        2,
        "código externo fondo",
        3,
        "un-uuid",
        null,
        null
                                                         )

internal const val jsonCreditoFondoConNulos = """{
        "client-id": 10,
        "id": null ,
        "amount": 30.5,
        "price-paid": 40.5,
        "tax-paid": 50.5,
        "valid-from": null,
        "valid-until": null,
        "consumed": false,
        "source": "Orbita",
        "username": "el-usuario",
        "person-id": 60,
        "fund-id": 70,
        "fund-external-code": "código externo fondo",
        "tax-id": 80,
        "device-id": "otro-uuid",
        "location-id": null,
        "customer-group-id": null
    } """

@JvmField
internal val creditoFondoDTOConNulos = CreditoFondoDTO(
        10,
        null,
        Decimal(30.5),
        Decimal(40.5),
        Decimal(50.5),
        null,
        null,
        false,
        "Orbita",
        "el-usuario",
        60,
        70,
        "código externo fondo",
        80,
        "otro-uuid",
        null,
        null

                                                      )

@JvmField
internal val creditoFondoNegocioConNulos = CreditoFondo(
        10,
        null,
        Decimal(30.5),
        Decimal(40.5),
        Decimal(50.5),
        null,
        null,
        false,
        "Orbita",
        "el-usuario",
        60,
        70,
        "código externo fondo",
        80,
        "otro-uuid",
        null,
        null
                                                       )

internal const val jsonCreditoFondoSinNulos = """{
        "client-id": 10,
        "id": 20 ,
        "amount": 30.5,
        "price-paid": 40.5,
        "tax-paid": 50.5,
        "valid-from": "$FECHA_DESDE_DEFECTO_STR",
        "valid-until": "$FECHA_HASTA_DEFECTO_STR",
        "consumed": true,
        "source": "Orbita",
        "username": "el-usuario",
        "person-id": 60,
        "fund-id": 70,
        "fund-external-code": "código externo fondo",
        "tax-id": 80,
        "device-id": "otro-uuid",
        "location-id": 90,
        "customer-group-id": 100
    } """

@JvmField
internal val creditoFondoDTOSinNulos = CreditoFondoDTO(
        10,
        20,
        Decimal(30.5),
        Decimal(40.5),
        Decimal(50.5),
        FECHA_DESDE_DEFECTO,
        FECHA_HASTA_DEFECTO,
        true,
        "Orbita",
        "el-usuario",
        60,
        70,
        "código externo fondo",
        80,
        "otro-uuid",
        90,
        100
                                                      )

@JvmField
internal val creditoFondoNegocioSinNulos = CreditoFondo(
        10,
        20,
        Decimal(30.5),
        Decimal(40.5),
        Decimal(50.5),
        FECHA_DESDE_DEFECTO,
        FECHA_HASTA_DEFECTO,
        true,
        "Orbita",
        "el-usuario",
        60,
        70,
        "código externo fondo",
        80,
        "otro-uuid",
        90,
        100
                                                       )