package co.smartobjects.nfc.operacionessobretags

import co.smartobjects.nfc.lectorestags.ILectorTag
import co.smartobjects.nfc.lectorestags.parametrosDeAutenticacion.ParametrosAutenticacionUltralight
import co.smartobjects.nfc.tags.ITag
import co.smartobjects.nfc.tags.mifare.ultralight.UltralightEV1
import kotlin.test.assertTrue


internal class OperacionLecturaUltralightEV1BasePruebas(_operacionesCompuestas: OperacionesCompuestas<ITag, ILectorTag<ITag>>, _paginasUsuarioIniciales: ByteArray) : OperacionUltralightEV1BasePruebas()
{
    init
    {
        operacionesCompuestas = _operacionesCompuestas
        paginasUsuarioIniciales = _paginasUsuarioIniciales
    }

    private fun activarAutenticacionYEjecutarOperacion(resultadoEsperado: ResultadoLecturaNFC, pasosIntermedios: () -> Unit)
    {
        activarAutenticacion(operacionesCompuestas)
        pasosIntermedios()
        val callbacks = CallbackLecturaPruebas(resultadoEsperado)
        OperacionLectura(false, callbacks, operacionesCompuestas).ejecutarOperacion()
        assertTrue(callbacks.llamoAlgunCallback)
    }

    fun leer_ultralight_ev1_con_contraseña_por_defecto()
    {
        val resultadoEsperado = ResultadoLecturaNFC.TagVacio
        val callbacks = CallbackLecturaPruebas(resultadoEsperado)
        OperacionLectura(false, callbacks, operacionesCompuestas).ejecutarOperacion()
        assertTrue(callbacks.llamoAlgunCallback)
    }

    fun leer_ultralight_ev1_con_autenticacion_activada_y_contraseña_correcta()
    {
        activarAutenticacionYEjecutarOperacion(ResultadoLecturaNFC.TagLeido(paginasUsuarioIniciales)) {}
    }

    fun intentar_leer_ultralight_ev1_con_autenticacion_activada_y_contraseña_incorrecta()
    {
        activarAutenticacionYEjecutarOperacion(ResultadoLecturaNFC.LlaveDesconocida) {
            cambiarContraseñaUltralightEV1(operacionesCompuestas, byteArrayOf(1, 2, 3, 4))
        }
    }

    fun intentar_leer_ultralight_ev1_con_autenticacion_activada_y_pack_incorrecto()
    {
        activarAutenticacionYEjecutarOperacion(ResultadoLecturaNFC.LlaveDesconocida) {
            cambiarPackUltralightEV1(operacionesCompuestas, byteArrayOf(1, 2))
        }
    }

    fun intentar_leer_ultralight_ev1_con_autenticacion_activada_pero_lectura_desprotegida()
    {
        activarAutenticacionYEjecutarOperacion(ResultadoLecturaNFC.SinAutenticacionActivada) {
            val parametrosAutenticacion =
                    ParametrosAutenticacionUltralight(
                            UltralightEV1.PARAMETROS_DE_CONFIGURACION_DE_AUTENTICACION.paginaInicialAutenticacion,
                            UltralightEV1.PARAMETROS_DE_CONFIGURACION_DE_AUTENTICACION.maximoNumeroIntentos,
                            false
                                                     )
            cambiarParametrosAutenticacionUltralightEV1(operacionesCompuestas, parametrosAutenticacion)
        }
    }

    fun intentar_leer_ultralight_ev1_con_autenticacion_activada_pero_numero_de_intentos_incorrecto()
    {
        activarAutenticacionYEjecutarOperacion(ResultadoLecturaNFC.SinAutenticacionActivada) {
            val parametrosAutenticacion =
                    ParametrosAutenticacionUltralight(
                            UltralightEV1.PARAMETROS_DE_CONFIGURACION_DE_AUTENTICACION.paginaInicialAutenticacion,
                            0,
                            UltralightEV1.PARAMETROS_DE_CONFIGURACION_DE_AUTENTICACION.protegerLectura
                                                     )
            cambiarParametrosAutenticacionUltralightEV1(operacionesCompuestas, parametrosAutenticacion)
        }
    }

    fun intentar_leer_ultralight_ev1_con_autenticacion_activada_pero_pagina_inicial_incorrecta()
    {
        activarAutenticacionYEjecutarOperacion(ResultadoLecturaNFC.SinAutenticacionActivada) {
            val parametrosAutenticacion =
                    ParametrosAutenticacionUltralight(
                            0xFF.toByte(),
                            UltralightEV1.PARAMETROS_DE_CONFIGURACION_DE_AUTENTICACION.maximoNumeroIntentos,
                            UltralightEV1.PARAMETROS_DE_CONFIGURACION_DE_AUTENTICACION.protegerLectura
                                                     )
            cambiarParametrosAutenticacionUltralightEV1(operacionesCompuestas, parametrosAutenticacion)
        }
    }
}