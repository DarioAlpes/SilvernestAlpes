package co.smartobjects.nfc.operacionessobretags

import co.smartobjects.nfc.lectorestags.parametrosDeAutenticacion.ParametrosAutenticacionUltralight
import co.smartobjects.nfc.tags.mifare.ultralight.UltralightC
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

internal class OperacionLecturaUltralightCPruebas : OperacionUltralightCBasePruebas()
{
    private fun activarAutenticacionYEjecutarOperacion(resultadoEsperado: ResultadoLecturaNFC, pasosIntermedios: () -> Unit)
    {
        activarAutenticacion(operacionesCompuestas)
        pasosIntermedios()
        val callbacks = CallbackLecturaPruebas(resultadoEsperado)
        OperacionLectura(false, callbacks, operacionesCompuestas).ejecutarOperacion()
        assertTrue(callbacks.llamoAlgunCallback)
    }

    @Test
    fun leer_ultralight_c_con_llave_por_defecto()
    {
        val resultadoEsperado = ResultadoLecturaNFC.TagVacio
        val callbacks = CallbackLecturaPruebas(resultadoEsperado)
        OperacionLectura(false, callbacks, operacionesCompuestas).ejecutarOperacion()
        assertTrue(callbacks.llamoAlgunCallback)
    }

    @Test
    fun leer_ultralight_c_con_autenticacion_activada_y_llave_correcta()
    {
        activarAutenticacionYEjecutarOperacion(ResultadoLecturaNFC.TagLeido(paginasUsuarioIniciales)) {}
    }

    @Test
    fun intentar_leer_ultralight_c_con_autenticacion_activada_y_llave_incorrecta()
    {
        activarAutenticacionYEjecutarOperacion(ResultadoLecturaNFC.LlaveDesconocida) {
            cambiarLlaveUltralightC(operacionesCompuestas, ByteArray(16, Int::toByte))
        }
    }

    @Test
    fun intentar_leer_ultralight_c_con_autenticacion_activada_pero_lectura_desprotegida()
    {
        activarAutenticacionYEjecutarOperacion(ResultadoLecturaNFC.SinAutenticacionActivada) {
            val parametrosAutenticacion =
                    ParametrosAutenticacionUltralight(
                            UltralightC.PARAMETROS_DE_CONFIGURACION_DE_AUTENTICACION.paginaInicialAutenticacion,
                            UltralightC.PARAMETROS_DE_CONFIGURACION_DE_AUTENTICACION.maximoNumeroIntentos,
                            false
                                                     )
            cambiarParametrosAutenticacionUltralightC(operacionesCompuestas, parametrosAutenticacion)
        }
    }

    @Test
    fun intentar_leer_ultralight_c_con_autenticacion_activada_pero_pagina_inicial_incorrecta()
    {
        activarAutenticacionYEjecutarOperacion(ResultadoLecturaNFC.SinAutenticacionActivada) {
            val parametrosAutenticacion =
                    ParametrosAutenticacionUltralight(
                            0x30.toByte(),
                            UltralightC.PARAMETROS_DE_CONFIGURACION_DE_AUTENTICACION.maximoNumeroIntentos,
                            UltralightC.PARAMETROS_DE_CONFIGURACION_DE_AUTENTICACION.protegerLectura
                                                     )
            cambiarParametrosAutenticacionUltralightC(operacionesCompuestas, parametrosAutenticacion)
        }
    }
}