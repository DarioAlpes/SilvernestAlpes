package co.smartobjects.entidades.usuarios

import co.smartobjects.entidades.excepciones.EntidadConCampoVacio
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@DisplayName("Rol")
internal class RolPruebas
{
    companion object
    {
        private val permisoPrueba1 = PermisoBack(
                1,
                "Prueba",
                PermisoBack.Accion.PUT
                                                )
        private val permisoPrueba2 = PermisoBack(
                1,
                "Prueba2",
                PermisoBack.Accion.PUT
                                                )
    }

    @Test
    fun hace_trim_a_nombre_correctamente()
    {
        val entidadSinTrim = Rol(
                "    Prueba    ",
                "Descripcion",
                setOf(permisoPrueba1)
                                )
        val entidadConTrim = Rol(
                "Prueba",
                "Descripcion",
                setOf(permisoPrueba1)
                                )
        assertEquals(entidadConTrim, entidadSinTrim)
    }

    @Test
    fun hace_trim_a_descripcion_correctamente()
    {
        val entidadSinTrim = Rol(
                "Prueba",
                "    Descripcion    ",
                setOf(permisoPrueba1)
                                )
        val entidadConTrim = Rol(
                "Prueba",
                "Descripcion",
                setOf(permisoPrueba1)
                                )
        assertEquals(entidadConTrim, entidadSinTrim)
    }

    @Test
    fun campos_quedan_con_valores_correctos()
    {
        val entidad = Rol(
                "Prueba",
                "Descripcion",
                setOf(permisoPrueba1)
                         )
        assertEquals("Prueba", entidad.campoNombre.valor)
        assertEquals("Descripcion", entidad.campoDescripcion.valor)
        assertEquals(setOf(permisoPrueba1), entidad.campoPermisos.valor)
    }

    @Test
    fun se_ignoran_permisos_repetidos()
    {
        val entidad1 = Rol(
                "Prueba",
                "Descripcion",
                setOf(permisoPrueba1, permisoPrueba1)
                          )
        val entidad2 = Rol(
                "Prueba",
                "Descripcion",
                setOf(permisoPrueba1)
                          )
        assertEquals(entidad1, entidad2)
    }

    @Test
    fun no_importa_el_orden_en_que_se_dan_los_permisos()
    {
        val entidad1 = Rol(
                "Prueba",
                "Descripcion",
                setOf(permisoPrueba1, permisoPrueba2)
                          )
        val entidad2 = Rol(
                "Prueba",
                "Descripcion",
                setOf(permisoPrueba2, permisoPrueba1)
                          )
        assertEquals(entidad1, entidad2)
    }

    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val entidadInicial = Rol(
                "Prueba",
                "Descripcion",
                setOf(permisoPrueba1)
                                )
        val entidadCopiada = entidadInicial.copiar(
                "Prueba editada",
                "Descripcion editada",
                setOf(permisoPrueba2)
                                                  )
        val entidadEsperada = Rol(
                "Prueba editada",
                "Descripcion editada",
                setOf(permisoPrueba2)
                                 )
        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun no_se_permite_instanciar_con_nombre_vacio()
    {
        val excepcion = Assertions.assertThrows(EntidadConCampoVacio::class.java, {
            Rol(
                    "",
                    "Descripcion",
                    setOf(permisoPrueba1)
               )
        })

        assertEquals(Rol.Campos.NOMBRE, excepcion.nombreDelCampo)
        assertEquals(Rol.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun no_se_permite_instanciar_con_nombre_con_espacios_o_tabs()
    {
        val excepcion = Assertions.assertThrows(EntidadConCampoVacio::class.java, {
            Rol(
                    "              ",
                    "Descripcion",
                    setOf(permisoPrueba1)
               )
        })

        assertEquals(Rol.Campos.NOMBRE, excepcion.nombreDelCampo)
        assertEquals(Rol.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun no_se_permite_instanciar_con_descripcion_vacia()
    {
        val excepcion = Assertions.assertThrows(EntidadConCampoVacio::class.java, {
            Rol(
                    "Prueba",
                    "",
                    setOf(permisoPrueba1)
               )
        })

        assertEquals(Rol.Campos.DESCRIPCION, excepcion.nombreDelCampo)
        assertEquals(Rol.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun no_se_permite_instanciar_con_descripcion_con_espacios_o_tabs()
    {
        val excepcion = Assertions.assertThrows(EntidadConCampoVacio::class.java, {
            Rol(
                    "Prueba",
                    "              ",
                    setOf(permisoPrueba1)
               )
        })

        assertEquals(Rol.Campos.DESCRIPCION, excepcion.nombreDelCampo)
        assertEquals(Rol.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun no_se_permite_instanciar_con_permisos_vacios()
    {
        val excepcion = Assertions.assertThrows(EntidadConCampoVacio::class.java, {
            Rol(
                    "Prueba",
                    "Descripcion",
                    setOf()
               )
        })

        assertEquals(Rol.Campos.PERMISOS, excepcion.nombreDelCampo)
        assertEquals(Rol.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Nested
    @DisplayName("RolParaCreacionDeUsuario")
    inner class RolParaCreacionDeUsuarioPruebas
    {
        @Test
        fun se_instancia_campos_correctamente()
        {
            val entidad = Rol.RolParaCreacionDeUsuario("Prueba")
            assertEquals("Prueba", entidad.campoNombre.valor)
        }

        @Test
        fun no_se_permite_instanciar_con_nombre_vacio()
        {
            val excepcion = Assertions.assertThrows(EntidadConCampoVacio::class.java, {
                Rol.RolParaCreacionDeUsuario("")
            })

            assertEquals(Rol.Campos.NOMBRE, excepcion.nombreDelCampo)
            assertEquals(Rol.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        }

        @Test
        fun no_se_permite_instanciar_con_nombre_con_espacios_o_tabs()
        {
            val excepcion = Assertions.assertThrows(EntidadConCampoVacio::class.java, {
                Rol.RolParaCreacionDeUsuario("              ")
            })

            assertEquals(Rol.Campos.NOMBRE, excepcion.nombreDelCampo)
            assertEquals(Rol.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        }
    }
}