package co.smartobjects.nfc.operacionessobretags

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class OperacionLecturaUltralightEV1MF0UL11Pruebas : OperacionUltralightEV1MF0UL11BasePruebas()
{
    private lateinit var operacionLectura: OperacionLecturaUltralightEV1BasePruebas

    @BeforeEach
    override fun antesDeCadaTest()
    {
        super.antesDeCadaTest()
        operacionLectura = OperacionLecturaUltralightEV1BasePruebas(operacionesCompuestas, paginasUsuarioIniciales)
    }

    @Test
    fun leer_ultralight_ev1_MF0UL11_con_contraseña_por_defecto()
    {
        operacionLectura.leer_ultralight_ev1_con_contraseña_por_defecto()
    }

    @Test
    fun leer_ultralight_ev1_MF0UL11_con_autenticacion_activada_y_contraseña_correcta()
    {
        operacionLectura.leer_ultralight_ev1_con_autenticacion_activada_y_contraseña_correcta()
    }

    @Test
    fun intentar_leer_ultralight_ev1_MF0UL11_con_autenticacion_activada_y_contraseña_incorrecta()
    {
        operacionLectura.intentar_leer_ultralight_ev1_con_autenticacion_activada_y_contraseña_incorrecta()
    }

    @Test
    fun intentar_leer_ultralight_ev1_MF0UL11_con_autenticacion_activada_y_pack_incorrecto()
    {
        operacionLectura.intentar_leer_ultralight_ev1_con_autenticacion_activada_y_pack_incorrecto()
    }

    @Test
    fun intentar_leer_ultralight_ev1_MF0UL11_con_autenticacion_activada_pero_lectura_desprotegida()
    {
        operacionLectura.intentar_leer_ultralight_ev1_con_autenticacion_activada_pero_lectura_desprotegida()
    }

    @Test
    fun intentar_leer_ultralight_ev1_MF0UL11_con_autenticacion_activada_pero_numero_de_intentos_incorrecto()
    {
        operacionLectura.intentar_leer_ultralight_ev1_con_autenticacion_activada_pero_numero_de_intentos_incorrecto()
    }

    @Test
    fun intentar_leer_ultralight_ev1_MF0UL11_con_autenticacion_activada_pero_pagina_inicial_incorrecta()
    {
        operacionLectura.intentar_leer_ultralight_ev1_con_autenticacion_activada_pero_pagina_inicial_incorrecta()
    }
}