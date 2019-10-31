package co.smartobjects.entidades.usuarios

import co.smartobjects.entidades.excepciones.EntidadConCampoVacio
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DisplayName("Usuario")
internal class UsuarioPruebas
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

        private val rolPrueba1 = Rol(
                "Prueba",
                "Descripcion",
                setOf(permisoPrueba1)
                                    )

        private val rolPrueba2 = Rol(
                "Prueba2",
                "Descripcion",
                setOf(permisoPrueba2)
                                    )

        private val rolParaCreacionPrueba1 = Rol.RolParaCreacionDeUsuario(
                rolPrueba1.nombre
                                                                         )

        private val rolParaCreacionPrueba2 = Rol.RolParaCreacionDeUsuario(
                rolPrueba2.nombre
                                                                         )

        private const val hashEjemplo = "\$shiro1\$SHA-512\$500000\$Atk2TMaxtKSRMTLMay+kut9lnG5QfiEzQkMP1MtL8+Y=\$l4OQPChonLAun1+0t+yGsY9VAiESaOEGRVnODk+g9pdWr+wRftRa0lA3MyYJD8NPaqTB6pgjy7w9ZdNOhDVxHg=="
    }

    @Test
    fun campos_quedan_con_valores_correctos()
    {
        val entidad = Usuario(
                Usuario.DatosUsuario(
                        1,
                        "Usuario",
                        "Nombre Completo",
                        "email@prueba.com",
                        true,"","","","","",""
                                    ),
                setOf(rolPrueba1, rolPrueba2)
                             )
        assertEquals(setOf(rolPrueba1, rolPrueba2), entidad.campoRoles.valor)
    }

    @Test
    fun se_ignoran_roles_repetidos()
    {
        val entidad1 = Usuario(
                Usuario.DatosUsuario(
                        1,
                        "Usuario",
                        "Nombre Completo",
                        "email@prueba.com",
                        true,
                        "",
                        "",
                        "",
                        "",
                        "",
                        ""
                                    ),
                setOf(rolPrueba1, rolPrueba1)
                              )
        val entidad2 = Usuario(
                Usuario.DatosUsuario(
                        1,
                        "Usuario",
                        "Nombre Completo",
                        "email@prueba.com",
                        true,
                        "",
                        "",
                        "",
                        "",
                        "",
                        ""
                                    ),
                setOf(rolPrueba1)
                              )
        assertEquals(entidad1, entidad2)
    }

    @Test
    fun no_importa_el_orden_en_que_se_dan_los_roles()
    {
        val entidad1 = Usuario(
                Usuario.DatosUsuario(
                        1,
                        "Usuario",
                        "Nombre Completo",
                        "email@prueba.com",
                        true,
                        "",
                        "",
                        "",
                        "",
                        "",
                        ""
                                    ),
                setOf(rolPrueba1, rolPrueba2)
                              )
        val entidad2 = Usuario(
                Usuario.DatosUsuario(
                        1,
                        "Usuario",
                        "Nombre Completo",
                        "email@prueba.com",
                        true,
                        "",
                        "",
                        "",
                        "",
                        "",
                        ""
                                    ),
                setOf(rolPrueba2, rolPrueba1)
                              )
        assertEquals(entidad1, entidad2)
    }

    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val entidadInicial = Usuario(
                Usuario.DatosUsuario(
                        1,
                        "Usuario",
                        "Nombre Completo",
                        "email@prueba.com",
                        true,
                        "",
                        "",
                        "",
                        "",
                        "",
                        ""
                                    ),
                setOf(rolPrueba1)
                                    )

        val entidadEsperada = Usuario(
                Usuario.DatosUsuario(
                        2,
                        "Usuario editado",
                        "Nombre Completo editado",
                        "email_editado@prueba.com",
                        false,
                        "",
                        "",
                        "",
                        "",
                        "",
                        ""
                                    ),
                setOf(rolPrueba2)
                                     )


        val entidadCopiada = entidadInicial.copiar(
                Usuario.DatosUsuario(
                        2,
                        "Usuario editado",
                        "Nombre Completo editado",
                        "email_editado@prueba.com",
                        false,
                        "",
                        "",
                        "",
                        "",
                        "",
                        ""
                                    ),
                setOf(rolPrueba2)
                                                  )

        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun no_se_permite_instanciar_con_roles_vacios()
    {
        val excepcion = Assertions.assertThrows(EntidadConCampoVacio::class.java, {
            Usuario(
                    Usuario.DatosUsuario(
                            1,
                            "Usuario",
                            "Nombre Completo",
                            "email@prueba.com",
                            true,
                            "",
                            "",
                            "",
                            "",
                            "",
                            ""
                                        ),
                    setOf()
                   )
        })

        assertEquals(Usuario.Campos.ROLES, excepcion.nombreDelCampo)
        assertEquals(Usuario.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Nested
    @DisplayName("UsuarioParaCreacion")
    inner class UsuarioParaCreacionPruebas
    {
        @Test
        fun campos_quedan_con_valores_correctos()
        {
            val contraseñaPrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val entidad = Usuario.UsuarioParaCreacion(
                    Usuario.DatosUsuario(
                            1,
                            "Usuario",
                            "Nombre Completo",
                            "email@prueba.com",
                            true,
                            "",
                            "",
                            "",
                            "",
                            "",
                            ""
                                        ),
                    contraseñaPrueba,
                    setOf(rolParaCreacionPrueba1, rolParaCreacionPrueba2)
                                                     )
            assertEquals(setOf(rolParaCreacionPrueba1, rolParaCreacionPrueba2), entidad.campoRolesParaCreacion.valor)
            assertTrue(contraseñaPrueba === entidad.campoContraseña.valor)
            assertTrue(contraseñaPrueba === entidad.contraseña)
            assertTrue(Arrays.equals(charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-'), entidad.contraseña))
        }

        @Test
        fun toString_no_contiene_contraseña()
        {
            val contraseñaPrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val entidad = Usuario.UsuarioParaCreacion(
                    Usuario.DatosUsuario(
                            1,
                            "Usuario",
                            "Nombre Completo",
                            "email@prueba.com",
                            true,
                            "",
                            "",
                            "",
                            "",
                            "",
                            ""
                                        ),
                    contraseñaPrueba,
                    setOf(rolParaCreacionPrueba1, rolParaCreacionPrueba2)
                                                     )
            assertFalse(entidad.toString().contains(Usuario.Campos.CONTRASEÑA))
            assertFalse(entidad.toString().contains(String(contraseñaPrueba)))
        }

        @Test
        fun se_ignoran_roles_repetidos()
        {
            val contraseñaPrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val entidad1 = Usuario.UsuarioParaCreacion(
                    Usuario.DatosUsuario(
                            1,
                            "Usuario",
                            "Nombre Completo",
                            "email@prueba.com",
                            true,
                            "",
                            "",
                            "",
                            "",
                            "",
                            ""
                                        ),
                    contraseñaPrueba,
                    setOf(rolParaCreacionPrueba1, rolParaCreacionPrueba1)
                                                      )
            val entidad2 = Usuario.UsuarioParaCreacion(
                    Usuario.DatosUsuario(
                            1,
                            "Usuario",
                            "Nombre Completo",
                            "email@prueba.com",
                            true,
                            "",
                            "",
                            "",
                            "",
                            "",
                            ""
                                        ),
                    contraseñaPrueba,
                    setOf(rolParaCreacionPrueba1)
                                                      )
            assertEquals(entidad1, entidad2)
        }

        @Test
        fun no_importa_el_orden_en_que_se_dan_los_roles()
        {
            val contraseñaPrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val entidad1 = Usuario.UsuarioParaCreacion(
                    Usuario.DatosUsuario(
                            1,
                            "Usuario",
                            "Nombre Completo",
                            "email@prueba.com",
                            true,
                            "",
                            "",
                            "",
                            "",
                            "",
                            ""
                                        ),
                    contraseñaPrueba,
                    setOf(rolParaCreacionPrueba1, rolParaCreacionPrueba2)
                                                      )
            val entidad2 = Usuario.UsuarioParaCreacion(
                    Usuario.DatosUsuario(
                            1,
                            "Usuario",
                            "Nombre Completo",
                            "email@prueba.com",
                            true,
                            "",
                            "",
                            "",
                            "",
                            "",
                            ""
                                        ),
                    contraseñaPrueba,
                    setOf(rolParaCreacionPrueba2, rolParaCreacionPrueba1)
                                                      )
            assertEquals(entidad1, entidad2)
        }

        @Test
        fun limpiar_contraseña_manda_a_0_la_contraseña_original_el_campo_y_el_atributo()
        {
            val contraseñaPrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val entidad = Usuario.UsuarioParaCreacion(
                    Usuario.DatosUsuario(
                            1,
                            "Usuario",
                            "Nombre Completo",
                            "email@prueba.com",
                            true,
                            "",
                            "",
                            "",
                            "",
                            "",
                            ""
                                        ),
                    contraseñaPrueba,
                    setOf(rolParaCreacionPrueba1, rolParaCreacionPrueba2)
                                                     )
            entidad.limpiarContraseña()
            assertTrue(contraseñaPrueba.all { it == '\u0000' })
            assertTrue(entidad.campoContraseña.valor.all { it == '\u0000' })
            assertTrue(entidad.contraseña.all { it == '\u0000' })
        }

        @Test
        fun copiar_funciona_correctamente_con_parametros_primitivos()
        {
            val contraseñaPrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val entidadInicial = Usuario.UsuarioParaCreacion(
                    Usuario.DatosUsuario(
                            1,
                            "Usuario",
                            "Nombre Completo",
                            "email@prueba.com",
                            true,
                            "",
                            "",
                            "",
                            "",
                            "",
                            ""
                                        ),
                    contraseñaPrueba,
                    setOf(rolParaCreacionPrueba1)
                                                            )

            val contraseñaPrueba2 = charArrayOf('o', 't', 'r', 'o', 'p', 'a', 's', 's', 'w', 'o', 'r', 'd', '2', '_')
            val entidadEsperada = Usuario.UsuarioParaCreacion(
                    Usuario.DatosUsuario(
                            2,
                            "Usuario editado",
                            "Nombre Completo editado",
                            "email_editado@prueba.com",
                            false,
                            "",
                            "",
                            "",
                            "",
                            "",
                            ""
                                        ),
                    contraseñaPrueba2,
                    setOf(rolParaCreacionPrueba2)
                                                             )


            val entidadCopiada = entidadInicial.copiar(
                    Usuario.DatosUsuario(
                            2,
                            "Usuario editado",
                            "Nombre Completo editado",
                            "email_editado@prueba.com",
                            false,
                            "",
                            "",
                            "",
                            "",
                            "",
                            ""
                                        ),
                    contraseñaPrueba2,
                    setOf(rolParaCreacionPrueba2)
                                                      )

            assertEquals(entidadEsperada, entidadCopiada)
        }

        @Test
        fun copiar_manda_a_0_la_contraseña_original_el_campo_y_el_atributo_cuando_se_envia_nueva_contraseña()
        {
            val contraseñaPrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val contraseñaPrueba2 = charArrayOf('o', 't', 'r', 'o', 'p', 'a', 's', 's', 'w', 'o', 'r', 'd', '2', '_')
            val entidad = Usuario.UsuarioParaCreacion(
                    Usuario.DatosUsuario(
                            1,
                            "Usuario",
                            "Nombre Completo",
                            "email@prueba.com",
                            true,
                            "",
                            "",
                            "",
                            "",
                            "",
                            ""
                                        ),
                    contraseñaPrueba,
                    setOf(rolParaCreacionPrueba1, rolParaCreacionPrueba2)
                                                     )
            val entidad2 = entidad.copiar(contraseña = contraseñaPrueba2)
            assertTrue(Arrays.equals(charArrayOf('o', 't', 'r', 'o', 'p', 'a', 's', 's', 'w', 'o', 'r', 'd', '2', '_'), entidad2.contraseña))
            assertTrue(contraseñaPrueba.all { it == '\u0000' })
            assertTrue(entidad.campoContraseña.valor.all { it == '\u0000' })
            assertTrue(entidad.contraseña.all { it == '\u0000' })
        }

        @Test
        fun copiar_manda_a_0_la_contraseña_original_el_campo_y_el_atributo_cuando_se_envia_nuevo_arreglo_con_la_misma_contraseña()
        {
            val contraseñaPrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val contraseñaPrueba2 = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val entidad = Usuario.UsuarioParaCreacion(
                    Usuario.DatosUsuario(
                            1,
                            "Usuario",
                            "Nombre Completo",
                            "email@prueba.com",
                            true,
                            "",
                            "",
                            "",
                            "",
                            "",
                            ""
                                        ),
                    contraseñaPrueba,
                    setOf(rolParaCreacionPrueba1, rolParaCreacionPrueba2)
                                                     )
            val entidad2 = entidad.copiar(contraseña = contraseñaPrueba2)
            assertTrue(Arrays.equals(charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-'), entidad2.contraseña))
            assertTrue(contraseñaPrueba.all { it == '\u0000' })
            assertTrue(entidad.campoContraseña.valor.all { it == '\u0000' })
            assertTrue(entidad.contraseña.all { it == '\u0000' })
        }

        @Test
        fun copiar_NO_cambia_la_contraseña_original_el_campo_y_el_atributo_cuando_se_envia_el_mismo_arreglo()
        {
            val contraseñaPrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val entidad = Usuario.UsuarioParaCreacion(
                    Usuario.DatosUsuario(
                            1,
                            "Usuario",
                            "Nombre Completo",
                            "email@prueba.com",
                            true,
                            "",
                            "",
                            "",
                            "",
                            "",
                            ""
                                        ),
                    contraseñaPrueba,
                    setOf(rolParaCreacionPrueba1, rolParaCreacionPrueba2)
                                                     )
            val entidad2 = entidad.copiar(contraseña = contraseñaPrueba)
            assertTrue(Arrays.equals(charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-'), entidad2.contraseña))
            assertTrue(Arrays.equals(charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-'), contraseñaPrueba))
            assertTrue(Arrays.equals(charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-'), entidad.campoContraseña.valor))
            assertTrue(Arrays.equals(charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-'), entidad.contraseña))
        }

        @Test
        fun copiar_NO_cambia_la_contraseña_original_el_campo_y_el_atributo_cuando_no_se_envia_valor()
        {
            val contraseñaPrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val entidad = Usuario.UsuarioParaCreacion(
                    Usuario.DatosUsuario(
                            1,
                            "Usuario",
                            "Nombre Completo",
                            "email@prueba.com",
                            true,
                            "",
                            "",
                            "",
                            "",
                            "",
                            ""
                                        ),
                    contraseñaPrueba,
                    setOf(rolParaCreacionPrueba1, rolParaCreacionPrueba2)
                                                     )
            val entidad2 = entidad.copiar()
            assertTrue(Arrays.equals(charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-'), entidad2.contraseña))
            assertTrue(Arrays.equals(charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-'), contraseñaPrueba))
            assertTrue(Arrays.equals(charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-'), entidad.campoContraseña.valor))
            assertTrue(Arrays.equals(charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-'), entidad.contraseña))
        }

        @Test
        fun no_se_permite_instanciar_con_roles_vacios()
        {
            val contraseñaPrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val excepcion = Assertions.assertThrows(EntidadConCampoVacio::class.java, {
                Usuario.UsuarioParaCreacion(
                        Usuario.DatosUsuario(
                                1,
                                "Usuario",
                                "Nombre Completo",
                                "email@prueba.com",
                                true,
                                "",
                                "",
                                "",
                                "",
                                "",
                                ""
                                            ),
                        contraseñaPrueba,
                        setOf()
                                           )
            })

            assertEquals(Usuario.Campos.ROLES, excepcion.nombreDelCampo)
            assertEquals(Usuario.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        }

        @Test
        fun no_se_permite_instanciar_con_contraseña_vacia()
        {
            val excepcion = Assertions.assertThrows(EntidadConCampoVacio::class.java, {
                Usuario.UsuarioParaCreacion(
                        Usuario.DatosUsuario(
                                1,
                                "Usuario",
                                "Nombre Completo",
                                "email@prueba.com",
                                true,
                                "",
                                "",
                                "",
                                "",
                                "",
                                ""
                                            ),
                        charArrayOf(),
                        setOf(rolParaCreacionPrueba1, rolParaCreacionPrueba2)
                                           )
            })

            assertEquals(Usuario.Campos.CONTRASEÑA, excepcion.nombreDelCampo)
            assertEquals(Usuario.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        }
    }

    @Nested
    @DisplayName("CredencialesUsuario")
    inner class CredencialesUsuarioPruebas
    {
        @Test
        fun hace_trim_a_usuario_correctamente()
        {
            val contraseñaPrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val entidadSinTrim = Usuario.CredencialesUsuario(
                    "    Usuario    ",
                    contraseñaPrueba
                                                            )

            val entidadConTrim = Usuario.CredencialesUsuario(
                    "Usuario",
                    contraseñaPrueba
                                                            )
            assertEquals(entidadConTrim, entidadSinTrim)
        }

        @Test
        fun campos_quedan_con_valores_correctos()
        {
            val contraseñaPrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val entidad = Usuario.CredencialesUsuario(
                    "Usuario",
                    contraseñaPrueba
                                                     )
            assertTrue(contraseñaPrueba === entidad.campoContraseña.valor)
            assertTrue(contraseñaPrueba === entidad.contraseña)
            assertTrue(Arrays.equals(charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-'), entidad.contraseña))
            assertEquals("Usuario", entidad.campoUsuario.valor)
        }

        @Test
        fun toString_no_contiene_contraseña()
        {
            val contraseñaPrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val entidad = Usuario.CredencialesUsuario(
                    "Usuario",
                    contraseñaPrueba
                                                     )
            assertFalse(entidad.toString().contains(Usuario.Campos.CONTRASEÑA))
            assertFalse(entidad.toString().contains(String(contraseñaPrueba)))
        }

        @Test
        fun limpiar_contraseña_manda_a_0_la_contraseña_original_el_campo_y_el_atributo()
        {
            val contraseñaPrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val entidad = Usuario.CredencialesUsuario(
                    "Usuario",
                    contraseñaPrueba
                                                     )
            entidad.limpiarContraseña()
            assertTrue(contraseñaPrueba.all { it == '\u0000' })
            assertTrue(entidad.campoContraseña.valor.all { it == '\u0000' })
            assertTrue(entidad.contraseña.all { it == '\u0000' })
        }

        @Test
        fun copiar_funciona_correctamente_con_parametros_primitivos()
        {
            val contraseñaPrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val entidadInicial = Usuario.CredencialesUsuario(
                    "Usuario",
                    contraseñaPrueba
                                                            )
            val contraseñaPrueba2 = charArrayOf('o', 't', 'r', 'o', 'p', 'a', 's', 's', 'w', 'o', 'r', 'd', '2', '_')
            val entidadCopiada = entidadInicial.copiar(
                    "Usuario editado",
                    contraseñaPrueba2
                                                      )
            val entidadEsperada = Usuario.CredencialesUsuario(
                    "Usuario editado",
                    contraseñaPrueba2
                                                             )
            assertEquals(entidadEsperada, entidadCopiada)
        }

        @Test
        fun copiar_manda_a_0_la_contraseña_original_el_campo_y_el_atributo_cuando_se_envia_nueva_contraseña()
        {
            val contraseñaPrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val contraseñaPrueba2 = charArrayOf('o', 't', 'r', 'o', 'p', 'a', 's', 's', 'w', 'o', 'r', 'd', '2', '_')
            val entidad = Usuario.CredencialesUsuario(
                    "Usuario",
                    contraseñaPrueba
                                                     )
            val entidad2 = entidad.copiar(contraseña = contraseñaPrueba2)
            assertTrue(Arrays.equals(charArrayOf('o', 't', 'r', 'o', 'p', 'a', 's', 's', 'w', 'o', 'r', 'd', '2', '_'), entidad2.contraseña))
            assertTrue(contraseñaPrueba.all { it == '\u0000' })
            assertTrue(entidad.campoContraseña.valor.all { it == '\u0000' })
            assertTrue(entidad.contraseña.all { it == '\u0000' })
        }

        @Test
        fun copiar_manda_a_0_la_contraseña_original_el_campo_y_el_atributo_cuando_se_envia_nuevo_arreglo_con_la_misma_contraseña()
        {
            val contraseñaPrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val contraseñaPrueba2 = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val entidad = Usuario.CredencialesUsuario(
                    "Usuario",
                    contraseñaPrueba
                                                     )
            val entidad2 = entidad.copiar(contraseña = contraseñaPrueba2)
            assertTrue(Arrays.equals(charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-'), entidad2.contraseña))
            assertTrue(contraseñaPrueba.all { it == '\u0000' })
            assertTrue(entidad.campoContraseña.valor.all { it == '\u0000' })
            assertTrue(entidad.contraseña.all { it == '\u0000' })
        }

        @Test
        fun copiar_NO_cambia_la_contraseña_original_el_campo_y_el_atributo_cuando_se_envia_el_mismo_arreglo()
        {
            val contraseñaPrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val entidad = Usuario.CredencialesUsuario(
                    "Usuario",
                    contraseñaPrueba
                                                     )
            val entidad2 = entidad.copiar(contraseña = contraseñaPrueba)
            assertTrue(Arrays.equals(charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-'), entidad2.contraseña))
            assertTrue(Arrays.equals(charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-'), contraseñaPrueba))
            assertTrue(Arrays.equals(charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-'), entidad.campoContraseña.valor))
            assertTrue(Arrays.equals(charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-'), entidad.contraseña))
        }

        @Test
        fun copiar_NO_cambia_la_contraseña_original_el_campo_y_el_atributo_cuando_no_se_envia_valor()
        {
            val contraseñaPrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val entidad = Usuario.CredencialesUsuario(
                    "Usuario",
                    contraseñaPrueba
                                                     )
            val entidad2 = entidad.copiar()
            assertTrue(Arrays.equals(charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-'), entidad2.contraseña))
            assertTrue(Arrays.equals(charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-'), contraseñaPrueba))
            assertTrue(Arrays.equals(charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-'), entidad.campoContraseña.valor))
            assertTrue(Arrays.equals(charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-'), entidad.contraseña))
        }

        @Test
        fun no_se_permite_instanciar_con_usuario_vacio()
        {
            val contraseñaPrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val excepcion = Assertions.assertThrows(EntidadConCampoVacio::class.java, {
                Usuario.CredencialesUsuario(
                        "",
                        contraseñaPrueba
                                           )
            })

            assertEquals(Usuario.Campos.USUARIO, excepcion.nombreDelCampo)
            assertEquals(Usuario.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        }

        @Test
        fun no_se_permite_instanciar_con_usuario_con_espacios_o_tabs()
        {
            val contraseñaPrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val excepcion = Assertions.assertThrows(EntidadConCampoVacio::class.java, {
                Usuario.CredencialesUsuario(
                        "              ",
                        contraseñaPrueba
                                           )
            })

            assertEquals(Usuario.Campos.USUARIO, excepcion.nombreDelCampo)
            assertEquals(Usuario.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        }

        @Test
        fun no_se_permite_instanciar_con_contraseña_vacia()
        {
            val excepcion = Assertions.assertThrows(EntidadConCampoVacio::class.java, {
                Usuario.CredencialesUsuario(
                        "Usuario",
                        charArrayOf()
                                           )
            })

            assertEquals(Usuario.Campos.CONTRASEÑA, excepcion.nombreDelCampo)
            assertEquals(Usuario.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        }
    }

    @Nested
    @DisplayName("DatosUsuario")
    inner class DatosUsuarioPruebas
    {
        @Test
        fun hace_trim_a_usuario_correctamente()
        {
            val entidadSinTrim = Usuario.DatosUsuario(
                    1,
                    "    Usuario    ",
                    "Nombre Completo",
                    "email@prueba.com",
                    true,
                    "",
                    "",
                    "",
                    "",
                    "",
                    ""
                                                     )

            val entidadConTrim = Usuario.DatosUsuario(
                    1,
                    "Usuario",
                    "Nombre Completo",
                    "email@prueba.com",
                    true,
                    "",
                    "",
                    "",
                    "",
                    "",
                    ""
                                                     )
            assertEquals(entidadConTrim, entidadSinTrim)
        }

        @Test
        fun hace_trim_a_nombre_correctamente()
        {
            val entidadSinTrim = Usuario.DatosUsuario(
                    1,
                    "Usuario",
                    "    Nombre Completo    ",
                    "email@prueba.com",
                    true,
                    "",
                    "",
                    "",
                    "",
                    "",
                    ""
                                                     )
            val entidadConTrim = Usuario.DatosUsuario(
                    1,
                    "Usuario",
                    "Nombre Completo",
                    "email@prueba.com",
                    true,
                    "",
                    "",
                    "",
                    "",
                    "",
                    ""
                                                     )
            assertEquals(entidadConTrim, entidadSinTrim)
        }

        @Test
        fun hace_trim_a_email_correctamente()
        {
            val entidadSinTrim = Usuario.DatosUsuario(
                    1,
                    "Usuario",
                    "Nombre Completo",
                    "    email@prueba.com    ",
                    true,
                    "",
                    "",
                    "",
                    "",
                    "",
                    ""
                                                     )
            val entidadConTrim = Usuario.DatosUsuario(
                    1,
                    "Usuario",
                    "Nombre Completo",
                    "email@prueba.com",
                    true,
                    "",
                    "",
                    "",
                    "",
                    "",
                    ""
                                                     )
            assertEquals(entidadConTrim, entidadSinTrim)
        }

        @Test
        fun campos_quedan_con_valores_correctos()
        {
            val entidad = Usuario.DatosUsuario(
                    1,
                    "Usuario",
                    "Nombre Completo",
                    "email@prueba.com",
                    true,
                    "",
                    "",
                    "",
                    "",
                    "",
                    ""
                                              )
            assertEquals("Usuario", entidad.campoUsuario.valor)
            assertEquals("Nombre Completo", entidad.campoNombreCompleto.valor)
            assertEquals("email@prueba.com", entidad.campoEmail.valor)
        }

        @Test
        fun copiar_funciona_correctamente_con_parametros_primitivos()
        {
            val entidadInicial = Usuario.DatosUsuario(
                    1,
                    "Usuario",
                    "Nombre Completo",
                    "email@prueba.com",
                    true,
                    "",
                    "",
                    "",
                    "",
                    "",
                    ""
                                                     )

            val entidadEsperada = Usuario.DatosUsuario(
                    2,
                    "Usuario editado",
                    "Nombre Completo editado",
                    "email_editado@prueba.com",
                    false,
                    "",
                    "",
                    "",
                    "",
                    "",
                    ""
                                                      )

            val entidadCopiada = entidadInicial.copiar(
                    2,
                    "Usuario editado",
                    "Nombre Completo editado",
                    "email_editado@prueba.com",
                    false
                                                      )

            assertEquals(entidadEsperada, entidadCopiada)
        }

        @Test
        fun no_se_permite_instanciar_con_usuario_vacio()
        {
            val excepcion = Assertions.assertThrows(EntidadConCampoVacio::class.java, {
                Usuario.DatosUsuario(
                        1,
                        "",
                        "Nombre Completo",
                        "email@prueba.com",
                        true,
                        "",
                        "",
                        "",
                        "",
                        "",
                        ""
                                    )
            })

            assertEquals(Usuario.Campos.USUARIO, excepcion.nombreDelCampo)
            assertEquals(Usuario.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        }

        @Test
        fun no_se_permite_instanciar_con_usuario_con_espacios_o_tabs()
        {
            val excepcion = Assertions.assertThrows(EntidadConCampoVacio::class.java, {
                Usuario.DatosUsuario(
                        1,
                        "              ",
                        "Nombre Completo",
                        "email@prueba.com",
                        true,
                        "",
                        "",
                        "",
                        "",
                        "",
                        ""
                                    )
            })

            assertEquals(Usuario.Campos.USUARIO, excepcion.nombreDelCampo)
            assertEquals(Usuario.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        }

        @Test
        fun no_se_permite_instanciar_con_nombre_completo_vacio()
        {
            val excepcion = Assertions.assertThrows(EntidadConCampoVacio::class.java, {
                Usuario.DatosUsuario(
                        1,
                        "Usuario",
                        "",
                        "email@prueba.com",
                        true,
                        "",
                        "",
                        "",
                        "",
                        "",
                        ""
                                    )
            })

            assertEquals(Usuario.Campos.NOMBRE_COMPLETO, excepcion.nombreDelCampo)
            assertEquals(Usuario.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        }

        @Test
        fun no_se_permite_instanciar_con_nombre_completo_con_espacios_o_tabs()
        {
            val excepcion = Assertions.assertThrows(EntidadConCampoVacio::class.java, {
                Usuario.DatosUsuario(
                        1,
                        "Usuario",
                        "              ",
                        "email@prueba.com",
                        true,
                        "",
                        "",
                        "",
                        "",
                        "",
                        ""
                                    )
            })

            assertEquals(Usuario.Campos.NOMBRE_COMPLETO, excepcion.nombreDelCampo)
            assertEquals(Usuario.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        }

        @Test
        fun no_se_permite_instanciar_con_email_vacio()
        {
            val excepcion = Assertions.assertThrows(EntidadConCampoVacio::class.java, {
                Usuario.DatosUsuario(
                        1,
                        "Usuario",
                        "Nombre Completo",
                        "",
                        true,
                        "",
                        "",
                        "",
                        "",
                        "",
                        ""
                                    )
            })

            assertEquals(Usuario.Campos.EMAIL, excepcion.nombreDelCampo)
            assertEquals(Usuario.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        }

        @Test
        fun no_se_permite_instanciar_con_email_con_espacios_o_tabs()
        {
            val excepcion = Assertions.assertThrows(EntidadConCampoVacio::class.java, {
                Usuario.DatosUsuario(
                        1,
                        "Usuario",
                        "Nombre Completo",
                        "              ",
                        true,
                        "",
                        "",
                        "",
                        "",
                        "",
                        ""
                                    )
            })

            assertEquals(Usuario.Campos.EMAIL, excepcion.nombreDelCampo)
            assertEquals(Usuario.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        }
    }

    @Nested
    @DisplayName("CredencialesGuardadas")
    inner class CredencialesGuardadasPruebas
    {
        @Test
        fun se_instancia_campos_correctamente()
        {
            val usuario = Usuario(
                    Usuario.DatosUsuario(
                            1,
                            "Usuario",
                            "Nombre Completo",
                            "email@prueba.com",
                            true,
                            "",
                            "",
                            "",
                            "",
                            "",
                            ""
                                        ),
                    setOf(rolPrueba1)
                                 )
            val entidad = Usuario.CredencialesGuardadas(usuario, hashEjemplo)
            assertEquals(hashEjemplo, entidad.hashContraseña)
            assertEquals(usuario, entidad.usuario)
        }

        @Test
        fun permisos_son_la_union_de_permisos_de_los_roles()
        {
            val usuario = Usuario(
                    Usuario.DatosUsuario(
                            1,
                            "Usuario",
                            "Nombre Completo",
                            "email@prueba.com",
                            true,
                            "",
                            "",
                            "",
                            "",
                            "",
                            ""
                                        ),
                    setOf(rolPrueba1, rolPrueba2)
                                 )
            val entidad = Usuario.CredencialesGuardadas(usuario, hashEjemplo)
            assertEquals(rolPrueba1.permisos + rolPrueba2.permisos, entidad.permisos)
        }

        @Test
        fun permisos_en_dos_roles_diferentes_se_ignoran()
        {
            val usuario = Usuario(
                    Usuario.DatosUsuario(
                            1,
                            "Usuario",
                            "Nombre Completo",
                            "email@prueba.com",
                            true,
                            "",
                            "",
                            "",
                            "",
                            "",
                            ""
                                        ),
                    setOf(rolPrueba1, rolPrueba2.copiar(permisos = rolPrueba1.permisos))
                                 )
            val entidad = Usuario.CredencialesGuardadas(usuario, hashEjemplo)
            assertEquals(rolPrueba1.permisos, entidad.permisos)
        }
    }

    // Se necesitan estas pruebas porque el campo usa arreglos como valor. La implementación por defecto del equlas/hashcode verifican referencias en lugar de valores (ver warning de un data class con arreglos por ejemplo)
    @Nested
    @DisplayName("CampoContraseña")
    inner class CampoContraseñaPruebas
    {
        @Test
        fun dos_campos_contraseña_con_misma_contraseña_pero_diferente_arreglo_son_iguales_segun_equals()
        {
            val contraseñaPrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val entidad1 = Usuario.CredencialesUsuario.CampoContraseña(contraseñaPrueba.copyOf())
            val entidad2 = Usuario.CredencialesUsuario.CampoContraseña(contraseñaPrueba.copyOf())
            assertEquals(entidad1, entidad2)
        }

        @Test
        fun dos_campos_contraseña_con_misma_contraseña_pero_diferente_arreglo_producen_mismo_hash_code()
        {
            val contraseñaPrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val entidad1 = Usuario.CredencialesUsuario.CampoContraseña(contraseñaPrueba.copyOf())
            val entidad2 = Usuario.CredencialesUsuario.CampoContraseña(contraseñaPrueba.copyOf())
            assertEquals(entidad1.hashCode(), entidad2.hashCode())
        }
    }
}