package co.smartobjects.entidades.usuarios

import co.smartobjects.entidades.excepciones.EntidadConCampoVacio
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

@DisplayName("PermisoBack")
internal class PermisoBackPruebas
{
    @Test
    fun hace_trim_a_endpoint_correctamente()
    {
        val entidadSinTrim = PermisoBack(
                1,
                "    Prueba    ",
                PermisoBack.Accion.PUT
                                        )
        val entidadConTrim = PermisoBack(
                1,
                "Prueba",
                PermisoBack.Accion.PUT
                                        )
        assertEquals(entidadConTrim, entidadSinTrim)
    }

    @Test
    fun campos_quedan_con_valores_correctos()
    {
        val entidad = PermisoBack(
                1,
                "Prueba",
                PermisoBack.Accion.PUT
                                 )
        assertEquals("Prueba", entidad.campoEndPoint.valor)
    }

    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val entidadInicial = PermisoBack(
                1,
                "Prueba",
                PermisoBack.Accion.PUT
                                        )
        val entidadCopiada = entidadInicial.copiar(
                2,
                "Prueba editada",
                PermisoBack.Accion.DELETE
                                                  )
        val entidadEsperada = PermisoBack(
                2,
                "Prueba editada",
                PermisoBack.Accion.DELETE
                                         )
        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun no_se_permite_instanciar_con_endpoint_vacio()
    {
        val excepcion = assertThrows<EntidadConCampoVacio> {
            PermisoBack(1, "", PermisoBack.Accion.PUT)
        }

        assertEquals(PermisoBack.Campos.ENDPOINT, excepcion.nombreDelCampo)
        assertEquals(PermisoBack.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun no_se_permite_instanciar_con_endpoint_con_espacios_o_tabs()
    {
        val excepcion = assertThrows<EntidadConCampoVacio> {
            PermisoBack(1, "              ", PermisoBack.Accion.PUT)
        }

        assertEquals(PermisoBack.Campos.ENDPOINT, excepcion.nombreDelCampo)
        assertEquals(PermisoBack.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }
}