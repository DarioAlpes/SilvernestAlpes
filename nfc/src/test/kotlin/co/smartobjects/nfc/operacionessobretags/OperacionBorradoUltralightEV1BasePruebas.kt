package co.smartobjects.nfc.operacionessobretags

import co.smartobjects.nfc.lectorestags.ILectorTag
import co.smartobjects.nfc.lectorestags.parametrosDeAutenticacion.ParametrosAutenticacionUltralight
import co.smartobjects.nfc.tags.ITag
import co.smartobjects.nfc.tags.mifare.ultralight.UltralightEV1
import kotlin.test.assertTrue


internal class OperacionBorradoUltralightEV1BasePruebas(_operacionesCompuestas: OperacionesCompuestas<ITag, ILectorTag<ITag>>) : OperacionUltralightEV1BasePruebas()
{
    init
    {
        operacionesCompuestas = _operacionesCompuestas
    }

    private fun activarAutenticacionYEjecutarOperacion(resultadoEsperado: Boolean, pasosIntermedios: () -> Unit)
    {
        activarAutenticacion(operacionesCompuestas)
        pasosIntermedios()
        val callbacks = CallbackBorradoPruebas(operacionesCompuestas, resultadoEsperado)
        OperacionBorrado(callbacks, operacionesCompuestas).ejecutarOperacion()
        assertTrue(callbacks.llamoAlgunCallback)
    }

    fun borrar_paginas_ultralight_ev1_de_tag_sin_autenticacion()
    {
        val callbacks = CallbackBorradoPruebas(operacionesCompuestas, true)
        OperacionBorrado(callbacks, operacionesCompuestas).ejecutarOperacion()
        assertTrue(callbacks.llamoAlgunCallback)
    }

    fun borrar_paginas_ultralight_ev1_de_tag_con_autenticacion_actividad()
    {
        activarAutenticacionYEjecutarOperacion(true) {}
    }

    fun borrar_ultralight_ev1_con_autenticacion_activada_pero_lectura_desprotegida()
    {
        activarAutenticacionYEjecutarOperacion(true) {
            val parametrosAutenticacion =
                    ParametrosAutenticacionUltralight(
                            UltralightEV1.PARAMETROS_DE_CONFIGURACION_DE_AUTENTICACION.paginaInicialAutenticacion,
                            UltralightEV1.PARAMETROS_DE_CONFIGURACION_DE_AUTENTICACION.maximoNumeroIntentos,
                            false)
            cambiarParametrosAutenticacionUltralightEV1(operacionesCompuestas, parametrosAutenticacion)
        }
    }

    fun borrar_ultralight_ev1_con_autenticacion_activada_pero_numero_de_intentos_incorrecto()
    {
        activarAutenticacionYEjecutarOperacion(true) {
            val parametrosAutenticacion =
                    ParametrosAutenticacionUltralight(
                            UltralightEV1.PARAMETROS_DE_CONFIGURACION_DE_AUTENTICACION.paginaInicialAutenticacion,
                            0,
                            UltralightEV1.PARAMETROS_DE_CONFIGURACION_DE_AUTENTICACION.protegerLectura)
            cambiarParametrosAutenticacionUltralightEV1(operacionesCompuestas, parametrosAutenticacion)
        }
    }

    fun borrar_ultralight_ev1_con_autenticacion_activada_pero_pagina_inicial_incorrecta()
    {
        activarAutenticacionYEjecutarOperacion(true) {

            val parametrosAutenticacion =
                    ParametrosAutenticacionUltralight(
                            0xFF.toByte(),
                            UltralightEV1.PARAMETROS_DE_CONFIGURACION_DE_AUTENTICACION.maximoNumeroIntentos,
                            UltralightEV1.PARAMETROS_DE_CONFIGURACION_DE_AUTENTICACION.protegerLectura)
            cambiarParametrosAutenticacionUltralightEV1(operacionesCompuestas, parametrosAutenticacion)
        }
    }

    fun intentar_borrar_ultralight_ev1_con_autenticacion_activada_y_contraseña_incorrecta()
    {
        activarAutenticacionYEjecutarOperacion(false) {
            cambiarContraseñaUltralightEV1(operacionesCompuestas, byteArrayOf(1, 2, 3, 4))
        }
    }

    fun intentar_borrar_ultralight_ev1_con_autenticacion_activada_y_pack_incorrecto()
    {
        activarAutenticacionYEjecutarOperacion(false) {
            cambiarPackUltralightEV1(operacionesCompuestas, byteArrayOf(1, 2))
        }
    }
}