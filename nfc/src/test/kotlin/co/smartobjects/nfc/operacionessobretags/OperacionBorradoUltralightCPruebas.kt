package co.smartobjects.nfc.operacionessobretags

import co.smartobjects.nfc.lectorestags.parametrosDeAutenticacion.ParametrosAutenticacionUltralight
import co.smartobjects.nfc.tags.mifare.ultralight.UltralightC
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

internal class OperacionBorradoUltralightCPruebas : OperacionUltralightCBasePruebas()
{
    private fun activarAutenticacionYEjecutarOperacion(resultadoEsperado: Boolean, pasosIntermedios: () -> Unit)
    {
        activarAutenticacion(operacionesCompuestas)
        pasosIntermedios()
        val callbacks = CallbackBorradoPruebas(operacionesCompuestas, resultadoEsperado)
        OperacionBorrado(callbacks, operacionesCompuestas).ejecutarOperacion()
        assertTrue(callbacks.llamoAlgunCallback)
    }

    @Test
    fun borrar_paginas_ultralight_c_de_tag_sin_autenticacion()
    {
        val callbacks = CallbackBorradoPruebas(operacionesCompuestas, true)
        OperacionBorrado(callbacks, operacionesCompuestas).ejecutarOperacion()
        assertTrue(callbacks.llamoAlgunCallback)
    }

    @Test
    fun borrar_paginas_ultralight_c_de_tag_con_autenticacion_activada()
    {
        activarAutenticacionYEjecutarOperacion(true) {}
    }

    @Test
    fun borrar_ultralight_c_con_autenticacion_activada_pero_lectura_desprotegida()
    {
        activarAutenticacionYEjecutarOperacion(true) {
            val parametrosAutenticacion =
                    ParametrosAutenticacionUltralight(UltralightC.PARAMETROS_DE_CONFIGURACION_DE_AUTENTICACION.paginaInicialAutenticacion,
                                                      UltralightC.PARAMETROS_DE_CONFIGURACION_DE_AUTENTICACION.maximoNumeroIntentos,
                                                      false)
            cambiarParametrosAutenticacionUltralightC(operacionesCompuestas, parametrosAutenticacion)
        }
    }

    @Test
    fun borrar_ultralight_c_con_autenticacion_activada_pero_pagina_inicial_incorrecta()
    {
        activarAutenticacionYEjecutarOperacion(true) {
            val parametrosAutenticacion =
                    ParametrosAutenticacionUltralight(0x30.toByte(),
                                                      UltralightC.PARAMETROS_DE_CONFIGURACION_DE_AUTENTICACION.maximoNumeroIntentos,
                                                      UltralightC.PARAMETROS_DE_CONFIGURACION_DE_AUTENTICACION.protegerLectura)
            cambiarParametrosAutenticacionUltralightC(operacionesCompuestas, parametrosAutenticacion)
        }
    }

    @Test
    fun intentar_borrar_ultralight_c_con_autenticacion_activada_y_llave_incorrecta()
    {
        activarAutenticacionYEjecutarOperacion(false) {
            cambiarLlaveUltralightC(operacionesCompuestas, ByteArray(16, Int::toByte))
        }
    }
}