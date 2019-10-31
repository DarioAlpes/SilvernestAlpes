package co.smartobjects.nfc.operacionessobretags

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class OperacionBorradoUltralightEV1MF0UL11Pruebas : OperacionUltralightEV1MF0UL11BasePruebas()
{
    private lateinit var operacionBorrado: OperacionBorradoUltralightEV1BasePruebas

    @BeforeEach
    override fun antesDeCadaTest()
    {
        super.antesDeCadaTest()
        operacionBorrado = OperacionBorradoUltralightEV1BasePruebas(operacionesCompuestas)
    }

    @Test
    fun borrar_paginas_ultralight_ev1_MF0UL11_de_tag_sin_autenticacion()
    {
        operacionBorrado.borrar_paginas_ultralight_ev1_de_tag_sin_autenticacion()
    }

    @Test
    fun borrar_paginas_ultralight_ev1_MF0UL11_de_tag_con_autenticacion_activada()
    {
        operacionBorrado.borrar_paginas_ultralight_ev1_de_tag_con_autenticacion_actividad()
    }

    @Test
    fun borrar_ultralight_ev1_MF0UL11_con_autenticacion_activada_pero_lectura_desprotegida()
    {
        operacionBorrado.borrar_ultralight_ev1_con_autenticacion_activada_pero_lectura_desprotegida()
    }

    @Test
    fun borrar_ultralight_ev1_MF0UL11_con_autenticacion_activada_pero_numero_de_intentos_incorrecto()
    {
        operacionBorrado.borrar_ultralight_ev1_con_autenticacion_activada_pero_numero_de_intentos_incorrecto()
    }

    @Test
    fun borrar_ultralight_ev1_MF0UL11_con_autenticacion_activada_pero_pagina_inicial_incorrecta()
    {
        operacionBorrado.borrar_ultralight_ev1_con_autenticacion_activada_pero_pagina_inicial_incorrecta()
    }

    @Test
    fun intentar_borrar_ultralight_ev1_MF0UL11_con_autenticacion_activada_y_contraseña_incorrecta()
    {
        operacionBorrado.intentar_borrar_ultralight_ev1_con_autenticacion_activada_y_contraseña_incorrecta()
    }

    @Test
    fun intentar_borrar_ultralight_ev1_MF0UL11_con_autenticacion_activada_y_pack_incorrecto()
    {
        operacionBorrado.intentar_borrar_ultralight_ev1_con_autenticacion_activada_y_pack_incorrecto()
    }
}