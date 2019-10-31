package co.smartobjects.entidades.usuariosglobales

import co.smartobjects.entidades.excepciones.EntidadConCampoVacio
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DisplayName("UsuarioGlobal")
internal class UsuarioGlobalPruebas
{
    companion object
    {
        private const val hashEjemplo = "\$shiro1\$SHA-512\$500000\$Atk2TMaxtKSRMTLMay+kut9lnG5QfiEzQkMP1MtL8+Y=\$l4OQPChonLAun1+0t+yGsY9VAiESaOEGRVnODk+g9pdWr+wRftRa0lA3MyYJD8NPaqTB6pgjy7w9ZdNOhDVxHg=="
    }

    @Test
    fun se_instancia_correctamente_con_datos_correctos()
    {
        UsuarioGlobal(
                UsuarioGlobal.DatosUsuario(
                        "Usuario",
                        "Nombre Completo",
                        "email@prueba.com",
                        true
                                          )
                     )
    }

    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val entidadInicial =
                UsuarioGlobal(
                        UsuarioGlobal.DatosUsuario(
                                "Usuario",
                                "Nombre Completo",
                                "email@prueba.com",
                                true
                                                  )
                             )

        val entidadEsperada =
                UsuarioGlobal(
                        UsuarioGlobal.DatosUsuario(
                                "Usuario editado",
                                "Nombre Completo editado",
                                "email_editado@prueba.com",
                                false
                                                  )
                             )

        val entidadCopiada = entidadInicial.copiar(
                UsuarioGlobal.DatosUsuario(
                        "Usuario editado",
                        "Nombre Completo editado",
                        "email_editado@prueba.com",
                        false
                                          )
                                                  )


        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Nested
    @DisplayName("UsuarioParaCreacion")
    inner class UsuarioParaCreacionPruebas
    {
        @Test
        fun campos_quedan_con_valores_correctos()
        {
            val contraseñaPrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val entidad = UsuarioGlobal.UsuarioParaCreacion(
                    UsuarioGlobal.DatosUsuario(
                            "Usuario",
                            "Nombre Completo",
                            "email@prueba.com",
                            true
                                              ),
                    contraseñaPrueba
                                                           )
            assertTrue(contraseñaPrueba === entidad.campoContraseña.valor)
            assertTrue(contraseñaPrueba === entidad.contraseña)
            assertTrue(Arrays.equals(charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-'), entidad.contraseña))
        }

        @Test
        fun toString_no_contiene_contraseña()
        {
            val contraseñaPrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val entidad = UsuarioGlobal.UsuarioParaCreacion(
                    UsuarioGlobal.DatosUsuario(
                            "Usuario",
                            "Nombre Completo",
                            "email@prueba.com",
                            true
                                              ),
                    contraseñaPrueba
                                                           )
            assertFalse(entidad.toString().contains(UsuarioGlobal.Campos.CONTRASEÑA))
            assertFalse(entidad.toString().contains(String(contraseñaPrueba)))
        }

        @Test
        fun limpiar_contraseña_manda_a_0_la_contraseña_original_el_campo_y_el_atributo()
        {
            val contraseñaPrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val entidad = UsuarioGlobal.UsuarioParaCreacion(
                    UsuarioGlobal.DatosUsuario(
                            "Usuario",
                            "Nombre Completo",
                            "email@prueba.com",
                            true
                                              ),
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
            val entidadInicial = UsuarioGlobal.UsuarioParaCreacion(
                    UsuarioGlobal.DatosUsuario(
                            "Usuario",
                            "Nombre Completo",
                            "email@prueba.com",
                            true
                                              ),
                    contraseñaPrueba
                                                                  )
            val contraseñaPrueba2 = charArrayOf('o', 't', 'r', 'o', 'p', 'a', 's', 's', 'w', 'o', 'r', 'd', '2', '_')
            val entidadEsperada = UsuarioGlobal.UsuarioParaCreacion(
                    UsuarioGlobal.DatosUsuario(
                            "Usuario editado",
                            "Nombre Completo editado",
                            "email_editado@prueba.com",
                            false
                                              ),
                    contraseñaPrueba2
                                                                   )


            val entidadCopiada = entidadInicial.copiar(
                    UsuarioGlobal.DatosUsuario(
                            "Usuario editado",
                            "Nombre Completo editado",
                            "email_editado@prueba.com",
                            false
                                              ),
                    contraseñaPrueba2
                                                      )

            assertEquals(entidadEsperada, entidadCopiada)
        }

        @Test
        fun copiar_manda_a_0_la_contraseña_original_el_campo_y_el_atributo_cuando_se_envia_nueva_contraseña()
        {
            val contraseñaPrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val contraseñaPrueba2 = charArrayOf('o', 't', 'r', 'o', 'p', 'a', 's', 's', 'w', 'o', 'r', 'd', '2', '_')
            val entidad = UsuarioGlobal.UsuarioParaCreacion(
                    UsuarioGlobal.DatosUsuario(
                            "Usuario",
                            "Nombre Completo",
                            "email@prueba.com",
                            true
                                              ),
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
            val entidad = UsuarioGlobal.UsuarioParaCreacion(
                    UsuarioGlobal.DatosUsuario(
                            "Usuario",
                            "Nombre Completo",
                            "email@prueba.com",
                            true
                                              ),
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
            val entidad = UsuarioGlobal.UsuarioParaCreacion(
                    UsuarioGlobal.DatosUsuario(
                            "Usuario",
                            "Nombre Completo",
                            "email@prueba.com",
                            true
                                              ),
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
            val entidad = UsuarioGlobal.UsuarioParaCreacion(
                    UsuarioGlobal.DatosUsuario(
                            "Usuario",
                            "Nombre Completo",
                            "email@prueba.com",
                            true
                                              ),
                    contraseñaPrueba
                                                           )
            val entidad2 = entidad.copiar()
            assertTrue(Arrays.equals(charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-'), entidad2.contraseña))
            assertTrue(Arrays.equals(charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-'), contraseñaPrueba))
            assertTrue(Arrays.equals(charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-'), entidad.campoContraseña.valor))
            assertTrue(Arrays.equals(charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-'), entidad.contraseña))
        }

        @Test
        fun no_se_permite_instanciar_con_contraseña_vacia()
        {
            val excepcion = Assertions.assertThrows(EntidadConCampoVacio::class.java, {
                UsuarioGlobal.UsuarioParaCreacion(
                        UsuarioGlobal.DatosUsuario(
                                "Usuario",
                                "Nombre Completo",
                                "email@prueba.com",
                                true
                                                  ),
                        charArrayOf()
                                                 )
            })

            assertEquals(UsuarioGlobal.Campos.CONTRASEÑA, excepcion.nombreDelCampo)
            assertEquals(UsuarioGlobal.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
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
            val entidadSinTrim = UsuarioGlobal.CredencialesUsuario(
                    "    Usuario    ",
                    contraseñaPrueba
                                                                  )

            val entidadConTrim = UsuarioGlobal.CredencialesUsuario(
                    "Usuario",
                    contraseñaPrueba
                                                                  )
            assertEquals(entidadConTrim, entidadSinTrim)
        }

        @Test
        fun campos_quedan_con_valores_correctos()
        {
            val contraseñaPrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val entidad = UsuarioGlobal.CredencialesUsuario(
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
            val entidad = UsuarioGlobal.CredencialesUsuario(
                    "Usuario",
                    contraseñaPrueba
                                                           )
            assertFalse(entidad.toString().contains(UsuarioGlobal.Campos.CONTRASEÑA))
            assertFalse(entidad.toString().contains(String(contraseñaPrueba)))
        }

        @Test
        fun limpiar_contraseña_manda_a_0_la_contraseña_original_el_campo_y_el_atributo()
        {
            val contraseñaPrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val entidad = UsuarioGlobal.CredencialesUsuario(
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
            val entidadInicial = UsuarioGlobal.CredencialesUsuario(
                    "Usuario",
                    contraseñaPrueba
                                                                  )
            val contraseñaPrueba2 = charArrayOf('o', 't', 'r', 'o', 'p', 'a', 's', 's', 'w', 'o', 'r', 'd', '2', '_')
            val entidadCopiada = entidadInicial.copiar(
                    "Usuario editado",
                    contraseñaPrueba2
                                                      )
            val entidadEsperada = UsuarioGlobal.CredencialesUsuario(
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
            val entidad = UsuarioGlobal.CredencialesUsuario(
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
            val entidad = UsuarioGlobal.CredencialesUsuario(
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
            val entidad = UsuarioGlobal.CredencialesUsuario(
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
            val entidad = UsuarioGlobal.CredencialesUsuario(
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
                UsuarioGlobal.CredencialesUsuario(
                        "",
                        contraseñaPrueba
                                                 )
            })

            assertEquals(UsuarioGlobal.Campos.USUARIO, excepcion.nombreDelCampo)
            assertEquals(UsuarioGlobal.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        }

        @Test
        fun no_se_permite_instanciar_con_usuario_con_espacios_o_tabs()
        {
            val contraseñaPrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val excepcion = Assertions.assertThrows(EntidadConCampoVacio::class.java, {
                UsuarioGlobal.CredencialesUsuario(
                        "              ",
                        contraseñaPrueba
                                                 )
            })

            assertEquals(UsuarioGlobal.Campos.USUARIO, excepcion.nombreDelCampo)
            assertEquals(UsuarioGlobal.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        }

        @Test
        fun no_se_permite_instanciar_con_contraseña_vacia()
        {
            val excepcion = Assertions.assertThrows(EntidadConCampoVacio::class.java, {
                UsuarioGlobal.CredencialesUsuario(
                        "Usuario",
                        charArrayOf()
                                                 )
            })

            assertEquals(UsuarioGlobal.Campos.CONTRASEÑA, excepcion.nombreDelCampo)
            assertEquals(UsuarioGlobal.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        }
    }

    @Nested
    @DisplayName("DatosUsuario")
    inner class DatosUsuarioPruebas
    {
        @Test
        fun hace_trim_a_usuario_correctamente()
        {
            val entidadSinTrim = UsuarioGlobal.DatosUsuario(
                    "    Usuario    ",
                    "Nombre Completo",
                    "email@prueba.com",
                    true
                                                           )

            val entidadConTrim = UsuarioGlobal.DatosUsuario(
                    "Usuario",
                    "Nombre Completo",
                    "email@prueba.com",
                    true
                                                           )
            assertEquals(entidadConTrim, entidadSinTrim)
        }

        @Test
        fun hace_trim_a_nombre_correctamente()
        {
            val entidadSinTrim = UsuarioGlobal.DatosUsuario(
                    "Usuario",
                    "    Nombre Completo    ",
                    "email@prueba.com",
                    true
                                                           )
            val entidadConTrim = UsuarioGlobal.DatosUsuario(
                    "Usuario",
                    "Nombre Completo",
                    "email@prueba.com",
                    true
                                                           )
            assertEquals(entidadConTrim, entidadSinTrim)
        }

        @Test
        fun hace_trim_a_email_correctamente()
        {
            val entidadSinTrim = UsuarioGlobal.DatosUsuario(
                    "Usuario",
                    "Nombre Completo",
                    "    email@prueba.com    ",
                    true
                                                           )
            val entidadConTrim = UsuarioGlobal.DatosUsuario(
                    "Usuario",
                    "Nombre Completo",
                    "email@prueba.com",
                    true
                                                           )
            assertEquals(entidadConTrim, entidadSinTrim)
        }

        @Test
        fun campos_quedan_con_valores_correctos()
        {
            val entidad = UsuarioGlobal.DatosUsuario(
                    "Usuario",
                    "Nombre Completo",
                    "email@prueba.com",
                    true
                                                    )
            assertEquals("Usuario", entidad.campoUsuario.valor)
            assertEquals("Nombre Completo", entidad.campoNombreCompleto.valor)
            assertEquals("email@prueba.com", entidad.campoEmail.valor)
        }

        @Test
        fun copiar_funciona_correctamente_con_parametros_primitivos()
        {
            val entidadInicial = UsuarioGlobal.DatosUsuario(
                    "Usuario",
                    "Nombre Completo",
                    "email@prueba.com",
                    true
                                                           )

            val entidadEsperada = UsuarioGlobal.DatosUsuario(
                    "Usuario editado",
                    "Nombre Completo editado",
                    "email_editado@prueba.com",
                    false
                                                            )

            val entidadCopiada = entidadInicial.copiar(
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
                UsuarioGlobal.DatosUsuario(
                        "",
                        "Nombre Completo",
                        "email@prueba.com",
                        true
                                          )
            })

            assertEquals(UsuarioGlobal.Campos.USUARIO, excepcion.nombreDelCampo)
            assertEquals(UsuarioGlobal.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        }

        @Test
        fun no_se_permite_instanciar_con_usuario_con_espacios_o_tabs()
        {
            val excepcion = Assertions.assertThrows(EntidadConCampoVacio::class.java, {
                UsuarioGlobal.DatosUsuario(
                        "              ",
                        "Nombre Completo",
                        "email@prueba.com",
                        true
                                          )
            })

            assertEquals(UsuarioGlobal.Campos.USUARIO, excepcion.nombreDelCampo)
            assertEquals(UsuarioGlobal.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        }

        @Test
        fun no_se_permite_instanciar_con_nombre_completo_vacio()
        {
            val excepcion = Assertions.assertThrows(EntidadConCampoVacio::class.java, {
                UsuarioGlobal.DatosUsuario(
                        "Usuario",
                        "",
                        "email@prueba.com",
                        true
                                          )
            })

            assertEquals(UsuarioGlobal.Campos.NOMBRE_COMPLETO, excepcion.nombreDelCampo)
            assertEquals(UsuarioGlobal.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        }

        @Test
        fun no_se_permite_instanciar_con_nombre_completo_con_espacios_o_tabs()
        {
            val excepcion = Assertions.assertThrows(EntidadConCampoVacio::class.java, {
                UsuarioGlobal.DatosUsuario(
                        "Usuario",
                        "              ",
                        "email@prueba.com",
                        true
                                          )
            })

            assertEquals(UsuarioGlobal.Campos.NOMBRE_COMPLETO, excepcion.nombreDelCampo)
            assertEquals(UsuarioGlobal.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        }

        @Test
        fun no_se_permite_instanciar_con_email_vacio()
        {
            val excepcion = Assertions.assertThrows(EntidadConCampoVacio::class.java, {
                UsuarioGlobal.DatosUsuario(
                        "Usuario",
                        "Nombre Completo",
                        "",
                        true
                                          )
            })

            assertEquals(UsuarioGlobal.Campos.EMAIL, excepcion.nombreDelCampo)
            assertEquals(UsuarioGlobal.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        }

        @Test
        fun no_se_permite_instanciar_con_email_con_espacios_o_tabs()
        {
            val excepcion = Assertions.assertThrows(EntidadConCampoVacio::class.java, {
                UsuarioGlobal.DatosUsuario(
                        "Usuario",
                        "Nombre Completo",
                        "              ",
                        true
                                          )
            })

            assertEquals(UsuarioGlobal.Campos.EMAIL, excepcion.nombreDelCampo)
            assertEquals(UsuarioGlobal.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        }
    }

    @Nested
    @DisplayName("CredencialesGuardadas")
    inner class CredencialesGuardadasPruebas
    {
        @Test
        fun se_instancia_campos_correctamente()
        {
            val usuario = UsuarioGlobal(
                    UsuarioGlobal.DatosUsuario(
                            "Usuario",
                            "Nombre Completo",
                            "email@prueba.com",
                            true
                                              )
                                       )
            val entidad = UsuarioGlobal.CredencialesGuardadas(usuario, hashEjemplo)
            assertEquals(hashEjemplo, entidad.hashContraseña)
            assertEquals(usuario, entidad.usuario)
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
            val entidad1 = UsuarioGlobal.CredencialesUsuario.CampoContraseña(contraseñaPrueba.copyOf())
            val entidad2 = UsuarioGlobal.CredencialesUsuario.CampoContraseña(contraseñaPrueba.copyOf())
            assertEquals(entidad1, entidad2)
        }

        @Test
        fun dos_campos_contraseña_con_misma_contraseña_pero_diferente_arreglo_producen_mismo_hash_code()
        {
            val contraseñaPrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val entidad1 = UsuarioGlobal.CredencialesUsuario.CampoContraseña(contraseñaPrueba.copyOf())
            val entidad2 = UsuarioGlobal.CredencialesUsuario.CampoContraseña(contraseñaPrueba.copyOf())
            assertEquals(entidad1.hashCode(), entidad2.hashCode())
        }
    }
}