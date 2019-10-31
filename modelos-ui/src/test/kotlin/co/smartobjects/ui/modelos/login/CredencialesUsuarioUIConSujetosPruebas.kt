package co.smartobjects.ui.modelos.login

import co.smartobjects.entidades.excepciones.EntidadConCampoVacio
import co.smartobjects.entidades.usuarios.Usuario
import co.smartobjects.ui.modelos.PruebasModelosRxBase
import io.reactivex.Notification
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Nested
import java.util.*

@DisplayName("CredencialesUsuarioUIConSujetos")
internal class CredencialesUsuarioUIConSujetosPruebas: PruebasModelosRxBase()
{
    private val modelo: CredencialesUsuarioUI = CredencialesUsuarioUIConSujetos()

    @Nested
    inner class CambiarUsuario
    {
        private val testUsuario = modelo.usuario.test()

        @Test
        fun no_emite_valor_al_inicializar()
        {
            testUsuario.assertValueCount(0)
        }

        @Test
        fun con_valor_valido_emite_valor_cambiado_una_vez_por_cada_llamado()
        {
            val numeroEventos = 10
            (0 until numeroEventos).forEach {
                val usuarioPruebas = "Usuario $it"
                modelo.cambiarUsuario(usuarioPruebas)
                testUsuario.assertValueAt(it, Notification.createOnNext(usuarioPruebas))
            }
            testUsuario.assertValueCount(numeroEventos)
        }

        @Test
        fun con_espacios_emite_valor_con_trim()
        {
            modelo.cambiarUsuario("        Usuario       ")
            testUsuario.assertValue(Notification.createOnNext("Usuario"))
            testUsuario.assertValueCount(1)
        }

        @Test
        fun con_valor_vacio_emite_error_EntidadConCampoVacio()
        {
            modelo.cambiarUsuario("")
            testUsuario.assertValue({ it.isOnError })
            testUsuario.assertValue({ it.error!! is EntidadConCampoVacio })
            testUsuario.assertValueCount(1)
        }

        @Test
        fun con_valor_con_espacios_y_tabs_emite_error_EntidadConCampoVacio()
        {
            modelo.cambiarUsuario("             ")
            testUsuario.assertValue({ it.isOnError })
            testUsuario.assertValue({ it.error!! is EntidadConCampoVacio })
            testUsuario.assertValueCount(1)
        }

        @Test
        fun a_error_y_luego_a_valor_valido_emite_ambos_valores()
        {
            modelo.cambiarUsuario("")
            modelo.cambiarUsuario("Usuario")
            testUsuario.assertValueAt(0, { it.isOnError })
            testUsuario.assertValueAt(0, { it.error!! is EntidadConCampoVacio })
            testUsuario.assertValueAt(1, Notification.createOnNext("Usuario"))
            testUsuario.assertValueCount(2)
        }

        @Test
        fun a_valor_valido_y_luego_a_error_emite_ambos_valores()
        {
            modelo.cambiarUsuario("Usuario")
            modelo.cambiarUsuario("")
            testUsuario.assertValueAt(0, Notification.createOnNext("Usuario"))
            testUsuario.assertValueAt(1, { it.isOnError })
            testUsuario.assertValueAt(1, { it.error!! is EntidadConCampoVacio })
            testUsuario.assertValueCount(2)
        }

        @Test
        fun no_emite_mas_eventos_despues_de_llamar_finalizar_proceso()
        {
            modelo.finalizarProceso()
            modelo.cambiarUsuario("Usuario")
            modelo.cambiarUsuario("")
            testUsuario.assertValueCount(0)
        }
    }

    @Nested
    inner class CambiarContraseña
    {
        private val testContraseña = modelo.contraseña.test()

        @Test
        fun no_emite_valor_al_inicializar()
        {
            testContraseña.assertValueCount(0)
        }

        @Test
        fun con_valor_valido_emite_valor_cambiado_una_vez_por_cada_llamado()
        {
            val numeroEventos = 10
            (0 until numeroEventos).forEach {
                val contraseñaPruebas = "Contraseña $it".toCharArray()
                modelo.cambiarContraseña(contraseñaPruebas)
                testContraseña.assertValueAt(it, Notification.createOnNext(contraseñaPruebas))
            }
            testContraseña.assertValueCount(numeroEventos)
        }

        @Test
        fun con_valor_vacio_emite_error_EntidadConCampoVacio()
        {
            modelo.cambiarContraseña(charArrayOf())
            testContraseña.assertValue({ it.isOnError })
            testContraseña.assertValue({ it.error!! is EntidadConCampoVacio })
            testContraseña.assertValueCount(1)
        }

        @Test
        fun a_error_y_luego_a_valor_valido_emite_ambos_valores()
        {
            modelo.cambiarContraseña(charArrayOf())
            modelo.cambiarContraseña("Contraseña".toCharArray())
            testContraseña.assertValueAt(0, { it.isOnError })
            testContraseña.assertValueAt(0, { it.error!! is EntidadConCampoVacio })
            testContraseña.assertValueAt(1, { it.isOnNext })
            testContraseña.assertValueAt(1, { Arrays.equals("Contraseña".toCharArray(), it.value) })
            testContraseña.assertValueCount(2)
        }

        @Test
        fun a_valor_valido_y_luego_a_error_emite_ambos_valores()
        {
            modelo.cambiarContraseña("Contraseña".toCharArray())
            testContraseña.assertValueAt(0, { it.isOnNext })
            testContraseña.assertValueAt(0, { Arrays.equals("Contraseña".toCharArray(), it.value) })
            // Se debe validar el valor en 0 antes de cambiar contraseña porque la va a limpiar
            modelo.cambiarContraseña(charArrayOf())
            testContraseña.assertValueAt(1, { it.isOnError })
            testContraseña.assertValueAt(1, { it.error!! is EntidadConCampoVacio })
            testContraseña.assertValueCount(2)
        }

        @Test
        fun no_emite_mas_eventos_despues_de_llamar_finalizar_proceso()
        {
            modelo.finalizarProceso()
            modelo.cambiarContraseña("Contraseña".toCharArray())
            modelo.cambiarContraseña(charArrayOf())
            testContraseña.assertValueCount(0)
        }

        @Test
        fun a_valor_valido_limpia_anterior_valor_aterior()
        {
            val contraseñaOriginal = "Contraseña".toCharArray()
            modelo.cambiarContraseña(contraseñaOriginal)
            modelo.cambiarContraseña("Contraseña nueva".toCharArray())
            Assertions.assertTrue(contraseñaOriginal.all { it == '\u0000' })
            testContraseña.assertValueCount(2)
        }

        @Test
        fun a_error_limpia_anterior_valor_anterior()
        {
            val contraseñaOriginal = "Contraseña".toCharArray()
            modelo.cambiarContraseña(contraseñaOriginal)
            modelo.cambiarContraseña(charArrayOf())
            Assertions.assertTrue(contraseñaOriginal.all { it == '\u0000' })
            testContraseña.assertValueCount(2)
        }

        @Test
        fun finalizar_proceso_limpia_ultimo_valor()
        {
            val contraseñaPrueba = "Contraseña".toCharArray()
            modelo.cambiarContraseña(contraseñaPrueba)
            modelo.finalizarProceso()
            Assertions.assertTrue(contraseñaPrueba.all { it == '\u0000' })
        }
    }

    @Nested
    inner class SonCredencialesValidas
    {
        private val testSonCredencialesValidas = modelo.sonCredencialesValidas.test()

        @Test
        fun no_emite_evento_en_al_cambiar_usuario_si_no_se_cambia_contraseña()
        {
            modelo.cambiarUsuario("Usuario")
            modelo.cambiarUsuario("")
            testSonCredencialesValidas.assertValueCount(0)
        }

        @Test
        fun no_emite_evento_en_al_cambiar_contraseña_si_no_se_cambia_usuario()
        {
            modelo.cambiarContraseña("Contraseña".toCharArray())
            modelo.cambiarContraseña(charArrayOf())
            testSonCredencialesValidas.assertValueCount(0)
        }

        @Test
        fun emite_evento_true_al_cambiar_usuario_y_contraseña_a_valores_validos()
        {
            modelo.cambiarUsuario("Usuario")
            modelo.cambiarContraseña("Contraseña".toCharArray())
            testSonCredencialesValidas.assertValue(true)
            testSonCredencialesValidas.assertValueCount(1)
        }

        @Test
        fun emite_evento_false_al_cambiar_usuario_valido_y_contraseña_invalida()
        {
            modelo.cambiarUsuario("Usuario")
            modelo.cambiarContraseña(charArrayOf())
            testSonCredencialesValidas.assertValue(false)
            testSonCredencialesValidas.assertValueCount(1)
        }

        @Test
        fun emite_evento_false_al_cambiar_contraseña_valida_y_usuario_invalido()
        {
            modelo.cambiarContraseña("Contraseña".toCharArray())
            modelo.cambiarUsuario("")
            testSonCredencialesValidas.assertValue(false)
            testSonCredencialesValidas.assertValueCount(1)
        }

        @Test
        fun emite_evento_false_al_cambiar_usuario_y_contraseña_a_valores_invalidos()
        {
            modelo.cambiarUsuario("")
            modelo.cambiarContraseña(charArrayOf())
            testSonCredencialesValidas.assertValue(false)
            testSonCredencialesValidas.assertValueCount(1)
        }

        @Test
        fun no_emite_mas_eventos_despues_de_llamar_finalizar_proceso()
        {
            modelo.finalizarProceso()
            modelo.cambiarContraseña(charArrayOf())
            modelo.cambiarUsuario("Usuario")
            modelo.cambiarUsuario("")
            modelo.cambiarContraseña("Contraseña".toCharArray())
            modelo.cambiarContraseña(charArrayOf())
            testSonCredencialesValidas.assertValueCount(0)
        }
    }

    @Nested
    inner class ACredencialesUsuario
    {
        @Test
        fun retorna_credenciales_correctas_al_asignar_valores_validos()
        {
            modelo.cambiarContraseña("Contraseña".toCharArray())
            modelo.cambiarUsuario("Usuario")
            val credenciales = modelo.aCredencialesUsuario()
            val credencialesEsperadas = Usuario.CredencialesUsuario("Usuario", "Contraseña".toCharArray())
            Assertions.assertEquals(credencialesEsperadas, credenciales)
        }

        @Test
        fun lanza_IllegalStateException_si_no_se_han_asignado_usuario_ni_contraseña()
        {
            Assertions.assertThrows(IllegalStateException::class.java, { modelo.aCredencialesUsuario() })
        }

        @Test
        fun lanza_IllegalStateException_con_usuario_valido_pero_sin_asignar_contraseña()
        {
            modelo.cambiarUsuario("Usuario")
            Assertions.assertThrows(IllegalStateException::class.java, { modelo.aCredencialesUsuario() })
        }

        @Test
        fun lanza_IllegalStateException_con_contraseña_valida_pero_sin_asignar_usuario()
        {
            modelo.cambiarContraseña("Contraseña".toCharArray())
            Assertions.assertThrows(IllegalStateException::class.java, { modelo.aCredencialesUsuario() })
        }

        @Test
        fun lanza_IllegalStateException_con_contraseña_invalida()
        {
            modelo.cambiarContraseña(charArrayOf())
            modelo.cambiarUsuario("Usuario")
            Assertions.assertThrows(IllegalStateException::class.java, { modelo.aCredencialesUsuario() })
        }

        @Test
        fun lanza_IllegalStateException_con_usuario_invalido()
        {
            modelo.cambiarContraseña("Contraseña".toCharArray())
            modelo.cambiarUsuario("")
            Assertions.assertThrows(IllegalStateException::class.java, { modelo.aCredencialesUsuario() })
        }

        @Test
        fun lanza_IllegalStateException_con_contraseña_y_usuario_invalidos()
        {
            modelo.cambiarContraseña(charArrayOf())
            modelo.cambiarUsuario("")
            Assertions.assertThrows(IllegalStateException::class.java, { modelo.aCredencialesUsuario() })
        }
    }
}