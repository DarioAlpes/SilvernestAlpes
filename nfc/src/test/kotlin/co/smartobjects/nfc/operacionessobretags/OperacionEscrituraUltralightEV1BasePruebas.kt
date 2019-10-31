package co.smartobjects.nfc.operacionessobretags

import co.smartobjects.nfc.lectorestags.ILectorTag
import co.smartobjects.nfc.lectorestags.parametrosDeAutenticacion.ParametrosAutenticacionUltralight
import co.smartobjects.nfc.tags.ITag
import co.smartobjects.nfc.tags.mifare.ultralight.UltralightEV1
import kotlin.test.assertTrue


internal class OperacionEscrituraUltralightEV1BasePruebas(_operacionesCompuestas: OperacionesCompuestas<ITag, ILectorTag<ITag>>, _paginasUsuarioIniciales: ByteArray) : OperacionUltralightEV1BasePruebas()
{
    init
    {
        operacionesCompuestas = _operacionesCompuestas
        paginasUsuarioIniciales = _paginasUsuarioIniciales
    }

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

    fun escribir_todas_las_paginas_ultralight_ev1_sin_autenticacion()
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

    fun escribir_la_mitad_de_las_paginas_ultralight_ev1_sin_autenticacion()
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

    fun escribir_todas_las_paginas_ultralight_ev1_con_autenticacion()
    {
        val datosAEscribir = ByteArray(paginasUsuarioIniciales.size, Int::toByte)
        val usarCompresion = false

        activarAutenticacionYEjecutarOperacion(usarCompresion, datosAEscribir, true) {}
    }

    fun escribir_paginas_ultralight_ev1_comprimiendo()
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

    fun intentar_escribir_mas_paginas_de_las_existentes_ultralight_ev1()
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

    fun intentar_escribir_ultralight_ev1_con_autenticacion_activada_y_contraseña_incorrecta()
    {
        val datosAEscribir = ByteArray(paginasUsuarioIniciales.size, Int::toByte)
        val usarCompresion = false

        activarAutenticacionYEjecutarOperacion(usarCompresion, datosAEscribir, false) { cambiarContraseñaUltralightEV1(operacionesCompuestas, byteArrayOf(1, 2, 3, 4)) }
    }

    fun intentar_escribir_ultralight_ev1_con_autenticacion_activada_y_pack_incorrecto()
    {
        val datosAEscribir = ByteArray(paginasUsuarioIniciales.size, Int::toByte)
        val usarCompresion = false

        activarAutenticacionYEjecutarOperacion(usarCompresion, datosAEscribir, false) { cambiarPackUltralightEV1(operacionesCompuestas, byteArrayOf(1, 2)) }
    }

    fun intentar_escribir_ultralight_ev1_con_autenticacion_activada_pero_lectura_desprotegida()
    {
        val datosAEscribir = ByteArray(paginasUsuarioIniciales.size, Int::toByte)
        val usarCompresion = false

        activarAutenticacionYEjecutarOperacion(usarCompresion, datosAEscribir, false) {
            val parametrosAutenticacion =
                    ParametrosAutenticacionUltralight(
                            UltralightEV1.PARAMETROS_DE_CONFIGURACION_DE_AUTENTICACION.paginaInicialAutenticacion,
                            UltralightEV1.PARAMETROS_DE_CONFIGURACION_DE_AUTENTICACION.maximoNumeroIntentos,
                            false)
            cambiarParametrosAutenticacionUltralightEV1(operacionesCompuestas, parametrosAutenticacion)
        }
    }

    fun intentar_escribir_ultralight_ev1_con_autenticacion_activada_pero_numero_de_intentos_incorrecto()
    {
        val datosAEscribir = ByteArray(paginasUsuarioIniciales.size, Int::toByte)
        val usarCompresion = false

        activarAutenticacionYEjecutarOperacion(usarCompresion, datosAEscribir, false) {
            val parametrosAutenticacion =
                    ParametrosAutenticacionUltralight(
                            UltralightEV1.PARAMETROS_DE_CONFIGURACION_DE_AUTENTICACION.paginaInicialAutenticacion,
                            0,
                            UltralightEV1.PARAMETROS_DE_CONFIGURACION_DE_AUTENTICACION.protegerLectura)
            cambiarParametrosAutenticacionUltralightEV1(operacionesCompuestas, parametrosAutenticacion)
        }
    }

    fun intentar_escribir_ultralight_ev1_con_autenticacion_activada_pero_pagina_inicial_incorrecta()
    {
        val datosAEscribir = ByteArray(paginasUsuarioIniciales.size, Int::toByte)
        val usarCompresion = false

        activarAutenticacionYEjecutarOperacion(usarCompresion, datosAEscribir, false) {
            val parametrosAutenticacion =
                    ParametrosAutenticacionUltralight(
                            0xFF.toByte(),
                            UltralightEV1.PARAMETROS_DE_CONFIGURACION_DE_AUTENTICACION.maximoNumeroIntentos,
                            UltralightEV1.PARAMETROS_DE_CONFIGURACION_DE_AUTENTICACION.protegerLectura)
            cambiarParametrosAutenticacionUltralightEV1(operacionesCompuestas, parametrosAutenticacion)
        }
    }
}