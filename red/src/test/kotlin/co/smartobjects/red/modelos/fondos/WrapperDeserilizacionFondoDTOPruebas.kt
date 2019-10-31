package co.smartobjects.red.modelos.fondos

import co.smartobjects.red.ConfiguracionJackson
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class WrapperDeserilizacionFondoDTOPruebas
{
    @Nested
    inner class Deserializacion
    {
        private fun crearJsonDePrueba(jsonEntidadEnvuelta: String): String
        {
            return """{"fund":$jsonEntidadEnvuelta}"""
        }

        @Test
        fun de_un_acceso_envuelto_funciona_correctamente()
        {
            val entidadDeserializada =
                    ConfiguracionJackson
                        .objectMapperDeJackson
                        .readValue(crearJsonDePrueba(AccesoDTOPruebas.jsonSinNulos), WrapperDeserilizacionFondoDTO::class.java)

            assertEquals(WrapperDeserilizacionFondoDTO(AccesoDTOPruebas.entidadDTOSinNulos), entidadDeserializada)
        }

        @Test
        fun de_una_categoria_sku_envuelta_funciona_correctamente()
        {
            val entidadDeserializada =
                    ConfiguracionJackson
                        .objectMapperDeJackson
                        .readValue(crearJsonDePrueba(CategoriaSkuDTOPruebas.jsonSinNulosConAncestros), WrapperDeserilizacionFondoDTO::class.java)

            assertEquals(WrapperDeserilizacionFondoDTO(CategoriaSkuDTOPruebas.entidadDTOSinNulosConAncestros), entidadDeserializada)
        }

        @Test
        fun de_dinero_envuelto_funciona_correctamente()
        {
            val entidadDeserializada =
                    ConfiguracionJackson
                        .objectMapperDeJackson
                        .readValue(crearJsonDePrueba(DineroDTOPruebas.jsonSinNulos), WrapperDeserilizacionFondoDTO::class.java)

            assertEquals(WrapperDeserilizacionFondoDTO(DineroDTOPruebas.entidadDTOSinNulos), entidadDeserializada)
        }

        @Test
        fun de_entrada_envuelta_funciona_correctamente()
        {
            val entidadDeserializada =
                    ConfiguracionJackson
                        .objectMapperDeJackson
                        .readValue(crearJsonDePrueba(EntradaDTOPruebas.jsonSinNulos), WrapperDeserilizacionFondoDTO::class.java)

            assertEquals(WrapperDeserilizacionFondoDTO(EntradaDTOPruebas.entidadDTOSinNulos), entidadDeserializada)
        }

        @Test
        fun de_sku_envuelto_funciona_correctamente()
        {
            val entidadDeserializada =
                    ConfiguracionJackson
                        .objectMapperDeJackson
                        .readValue(crearJsonDePrueba(SkuDTOPruebas.jsonSinNulos), WrapperDeserilizacionFondoDTO::class.java)

            assertEquals(WrapperDeserilizacionFondoDTO(SkuDTOPruebas.entidadDTOSinNulos), entidadDeserializada)
        }
    }

    @Nested
    inner class Conversion
    {
        @Nested
        inner class AEntidadDeNegocio
        {
            @Test
            fun con_un_acceso_envuelto_correcta()
            {
                val entidadDeNegocio = WrapperDeserilizacionFondoDTO(AccesoDTOPruebas.entidadDTOSinNulos).aEntidadDeNegocio()
                assertEquals(AccesoDTOPruebas.entidadNegocioSinNulos, entidadDeNegocio)
            }

            @Test
            fun con_una_categoria_sku_envuelta_correcta()
            {
                val entidadDeNegocio = WrapperDeserilizacionFondoDTO(CategoriaSkuDTOPruebas.entidadDTOSinNulosConAncestros).aEntidadDeNegocio()
                assertEquals(CategoriaSkuDTOPruebas.entidadNegocioSinNulosConAncestros, entidadDeNegocio)
            }

            @Test
            fun con_dinero_envuelto_correcta()
            {
                val entidadDeNegocio = WrapperDeserilizacionFondoDTO(DineroDTOPruebas.entidadDTOSinNulos).aEntidadDeNegocio()
                assertEquals(DineroDTOPruebas.entidadNegocioSinNulos, entidadDeNegocio)
            }

            @Test
            fun con_entrada_envuelta_correcta()
            {
                val entidadDeNegocio = WrapperDeserilizacionFondoDTO(EntradaDTOPruebas.entidadDTOSinNulos).aEntidadDeNegocio()
                assertEquals(EntradaDTOPruebas.entidadNegocioSinNulos, entidadDeNegocio)
            }

            @Test
            fun con_sku_envuelto_correcta()
            {
                val entidadDeNegocio = WrapperDeserilizacionFondoDTO(SkuDTOPruebas.entidadDTOSinNulos).aEntidadDeNegocio()
                assertEquals(SkuDTOPruebas.entidadNegocioSinNulos, entidadDeNegocio)
            }
        }
    }
}