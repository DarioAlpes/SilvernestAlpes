package co.smartobjects.entidades.ubicaciones

import co.smartobjects.entidades.excepciones.EntidadConCampoVacio
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals


@DisplayName("Ubicacion")
internal class UbicacionPruebas
{
    companion object
    {
        internal val entidadInicial =
                Ubicacion(
                        1,
                        1,
                        "Prueba",
                        Ubicacion.Tipo.AREA,
                        Ubicacion.Subtipo.AP,
                        null,
                        linkedSetOf()
                         )

        internal val entidadModificada =
                Ubicacion(
                        1,
                        1,
                        "Prueba editada",
                        Ubicacion.Tipo.AREA,
                        Ubicacion.Subtipo.AP,
                        null,
                        linkedSetOf()
                         )
    }

    @Test
    fun hace_trim_a_nombre_correctamente()
    {
        val entidadSinTrim = Ubicacion(
                1,
                1,
                "    Prueba    ",
                Ubicacion.Tipo.AREA,
                Ubicacion.Subtipo.AP,
                null,
                linkedSetOf()
                                      )
        val entidadConTrim = Ubicacion(
                1,
                1,
                "Prueba",
                Ubicacion.Tipo.AREA,
                Ubicacion.Subtipo.AP,
                null,
                linkedSetOf()
                                      )
        assertEquals(entidadConTrim, entidadSinTrim)
    }

    @Test
    fun campos_quedan_con_valores_correctos()
    {
        val entidad = Ubicacion(
                1,
                1,
                "Prueba",
                Ubicacion.Tipo.AREA,
                Ubicacion.Subtipo.AP,
                null,
                linkedSetOf()
                               )
        assertEquals("Prueba", entidad.campoNombre.valor)
    }

    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val entidadCopiada = entidadInicial.copiar(nombre = "Prueba editada")

        assertEquals(entidadModificada, entidadCopiada)
    }

    @Test
    fun copiar_con_id_funciona_correctamente()
    {
        val entidadEsperada = entidadInicial.copiar(id = 56367)

        val entidadCopiada = entidadInicial.copiarConId(56367)

        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun no_se_permite_instanciar_con_nombre_vacio()
    {
        val excepcion = assertThrows<EntidadConCampoVacio> {
            Ubicacion(
                    1,
                    1,
                    "",
                    Ubicacion.Tipo.AREA,
                    Ubicacion.Subtipo.AP,
                    null,
                    linkedSetOf()
                     )
        }

        assertEquals(Ubicacion.Campos.NOMBRE, excepcion.nombreDelCampo)
        assertEquals(Ubicacion.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun no_se_permite_instanciar_con_nombre_con_espacios_o_tabs()
    {
        val excepcion = assertThrows<EntidadConCampoVacio> {
            Ubicacion(
                    1,
                    1,
                    "             ",
                    Ubicacion.Tipo.AREA,
                    Ubicacion.Subtipo.AP,
                    null,
                    linkedSetOf()
                     )
        }

        assertEquals(Ubicacion.Campos.NOMBRE, excepcion.nombreDelCampo)
        assertEquals(Ubicacion.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }
}