package co.smartobjects.red.modelos.fondos

import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.modelos.fondos.precios.jsonPrecioSinNulos
import co.smartobjects.red.modelos.fondos.precios.precioDTOSinNulos
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class ListaFondosDTOPruebas
{
    companion object
    {
        private const val jsonListaFondos =
                """{
                        "funds": [
                            {
                                "fund-type": "SKU-CATEGORY",
                                "client-id": 1,
                                "id": 2,
                                "name": "Entidad prueba",
                                "available-for-sale": true,
                                "once-per-session": false,
                                "unlimited": false,
                                "default-price": $jsonPrecioSinNulos,
                                "external-code": "El código externo",
                                "parent-category-id": 3,
                                "ancestors-ids": [1, 2, 3],
                                "icon-key": "prueba-llave"
                            },
                            {
                                "fund-type": "SKU",
                                "client-id": 1,
                                "id": 2,
                                "name": "Entidad prueba",
                                "available-for-sale": true,
                                "once-per-session": false,
                                "unlimited": false,
                                "default-price": $jsonPrecioSinNulos,
                                "external-code": "El código externo",
                                "category-id": 3,
                                "image-key": "prueba-llave"
                            },
                            {
                                "fund-type": "ACCESS",
                                "client-id": 1,
                                "id": 2,
                                "name": "Entidad prueba",
                                "available-for-sale": true,
                                "once-per-session": false,
                                "unlimited": false,
                                "default-price": $jsonPrecioSinNulos,
                                "external-code": "El código externo",
                                "location-id": 3
                            },
                            {
                                "fund-type": "ENTRY",
                                "client-id": 1,
                                "id": 2,
                                "name": "Entidad prueba",
                                "available-for-sale": true,
                                "once-per-session": true,
                                "unlimited": false,
                                "default-price": $jsonPrecioSinNulos,
                                "external-code": "El código externo",
                                "location-id": 3
                            },
                            {
                                "fund-type": "CURRENCY",
                                "client-id": 1,
                                "id": 2,
                                "name": "Entidad prueba",
                                "available-for-sale": true,
                                "once-per-session": false,
                                "unlimited": false,
                                "default-price": $jsonPrecioSinNulos,
                                "external-code": "El código externo"
                            }
                        ]
                    }
                """

        private val listaFondosDTO =
                ListaFondosDTO(
                        listOf(
                                CategoriaSkuDTO(1, 2, "Entidad prueba", true, false, false, precioDTOSinNulos, "El código externo", 3, listOf(1, 2, 3), "prueba-llave"),
                                SkuDTO(1, 2, "Entidad prueba", true, false, false, precioDTOSinNulos, "El código externo", 3, "prueba-llave"),
                                AccesoDTO(1, 2, "Entidad prueba", true, false, false, precioDTOSinNulos, "El código externo", 3),
                                EntradaDTO(1, 2, "Entidad prueba", true, true, false, precioDTOSinNulos, "El código externo", 3),
                                DineroDTO(1, 2, "Entidad prueba", true, false, false, precioDTOSinNulos, "El código externo")
                              )
                              )

        private val listaFondos = listaFondosDTO.fondos.map { it.aEntidadDeNegocio() }
    }

    @Nested
    inner class Deserializacion
    {
        @Test
        fun lista_de_fondos_de_difrentes_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonListaFondos, ListaFondosDTO::class.java)
            assertEquals(listaFondosDTO, entidadDeserializada)
        }
    }

    @Nested
    inner class Conversion
    {
        @Nested
        inner class AEntidadDeNegocio
        {
            @Test
            fun correcta()
            {
                val entidadDeNegocio = listaFondosDTO.aEntidadDeNegocio()
                assertEquals(listaFondos, entidadDeNegocio)
            }
        }
    }
}