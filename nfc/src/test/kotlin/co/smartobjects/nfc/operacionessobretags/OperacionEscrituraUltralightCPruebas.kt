package co.smartobjects.nfc.operacionessobretags

import co.smartobjects.nfc.lectorestags.parametrosDeAutenticacion.ParametrosAutenticacionUltralight
import co.smartobjects.nfc.tags.mifare.ultralight.UltralightC
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

internal class OperacionEscrituraUltralightCPruebas : OperacionUltralightCBasePruebas()
{
    private fun activarAutenticacionYEjecutarOperacion(usarCompresion: Boolean, datosAEscribir: ByteArray, resultadoEsperado: Boolean, pasosIntermedios: () -> Unit)
    {
        activarAutenticacion(operacionesCompuestas)
        pasosIntermedios()
        val callbacks = CallbackEscrituraPruebas(paginasUsuarioIniciales.size, usarCompresion, operacionesCompuestas, resultadoEsperado, datosAEscribir)
        OperacionEscritura(usarCompresion,
                           datosAEscribir,
                           callbacks,
                           operacionesCompuestas).ejecutarOperacion()
        assertTrue(callbacks.llamoAlgunCallback)
    }


    @Test
    fun escribir_todas_las_paginas_ultralight_c_sin_autenticacion()
    {
        val datosAEscribir = ByteArray(paginasUsuarioIniciales.size, Int::toByte)
        val usarCompresion = false
        val callbacks = CallbackEscrituraPruebas(paginasUsuarioIniciales.size, usarCompresion, operacionesCompuestas, true, datosAEscribir)
        OperacionEscritura(usarCompresion,
                           datosAEscribir,
                           callbacks,
                           operacionesCompuestas).ejecutarOperacion()
        assertTrue(callbacks.llamoAlgunCallback)
    }

    @Test
    fun escribir_la_mitad_de_las_paginas_ultralight_c_sin_autenticacion()
    {
        val datosAEscribir = ByteArray(paginasUsuarioIniciales.size / 2, Int::toByte)
        val usarCompresion = false
        val callbacks = CallbackEscrituraPruebas(paginasUsuarioIniciales.size, usarCompresion, operacionesCompuestas, true, datosAEscribir)
        OperacionEscritura(usarCompresion,
                           datosAEscribir,
                           callbacks,
                           operacionesCompuestas).ejecutarOperacion()
        assertTrue(callbacks.llamoAlgunCallback)
    }

    @Test
    fun escribir_todas_las_paginas_ultralight_c_con_autenticacion()
    {
        val datosAEscribir = ByteArray(paginasUsuarioIniciales.size, Int::toByte)
        val usarCompresion = false

        activarAutenticacionYEjecutarOperacion(usarCompresion, datosAEscribir, true) {}
    }

    @Test
    fun escribir_paginas_ultralight_c_comprimiendo()
    {
        val datosAEscribir = ByteArray(paginasUsuarioIniciales.size / 2, Int::toByte)
        val usarCompresion = true
        val callbacks = CallbackEscrituraPruebas(paginasUsuarioIniciales.size, usarCompresion, operacionesCompuestas, true, datosAEscribir)
        OperacionEscritura(usarCompresion,
                           datosAEscribir,
                           callbacks,
                           operacionesCompuestas).ejecutarOperacion()
        assertTrue(callbacks.llamoAlgunCallback)
    }

    @Test
    fun intentar_escribir_mas_paginas_de_las_existentes_ultralight_c()
    {
        val datosAEscribir = ByteArray(paginasUsuarioIniciales.size + 1, Int::toByte)
        val usarCompresion = false
        val callbacks = CallbackEscrituraPruebas(paginasUsuarioIniciales.size, usarCompresion, operacionesCompuestas, false, datosAEscribir)
        OperacionEscritura(usarCompresion,
                           datosAEscribir,
                           callbacks,
                           operacionesCompuestas).ejecutarOperacion()
        assertTrue(callbacks.llamoAlgunCallback)
    }

    @Test
    fun intentar_escribir_ultralight_c_con_autenticacion_activada_y_llave_incorrecta()
    {
        val datosAEscribir = ByteArray(paginasUsuarioIniciales.size, Int::toByte)
        val usarCompresion = false

        activarAutenticacionYEjecutarOperacion(usarCompresion, datosAEscribir, false) {
            cambiarLlaveUltralightC(operacionesCompuestas, ByteArray(16, Int::toByte))
        }
    }

    @Test
    fun intentar_escribir_ultralight_c_con_autenticacion_activada_pero_lectura_desprotegida()
    {
        val datosAEscribir = ByteArray(paginasUsuarioIniciales.size, Int::toByte)
        val usarCompresion = false

        activarAutenticacionYEjecutarOperacion(usarCompresion, datosAEscribir, false) {
            val parametrosAutenticacion =
                    ParametrosAutenticacionUltralight(
                            UltralightC.PARAMETROS_DE_CONFIGURACION_DE_AUTENTICACION.paginaInicialAutenticacion,
                            UltralightC.PARAMETROS_DE_CONFIGURACION_DE_AUTENTICACION.maximoNumeroIntentos,
                            false)
            cambiarParametrosAutenticacionUltralightC(operacionesCompuestas, parametrosAutenticacion)
        }
    }

    @Test
    fun intentar_escribir_ultralight_C_con_autenticacion_activada_pero_pagina_inicial_incorrecta()
    {
        val datosAEscribir = ByteArray(paginasUsuarioIniciales.size, Int::toByte)
        val usarCompresion = false

        activarAutenticacionYEjecutarOperacion(usarCompresion, datosAEscribir, false) {
            val parametrosAutenticacion =
                    ParametrosAutenticacionUltralight(
                            0x30.toByte(),
                            UltralightC.PARAMETROS_DE_CONFIGURACION_DE_AUTENTICACION.maximoNumeroIntentos,
                            UltralightC.PARAMETROS_DE_CONFIGURACION_DE_AUTENTICACION.protegerLectura)
            cambiarParametrosAutenticacionUltralightC(operacionesCompuestas, parametrosAutenticacion)
        }
    }
}