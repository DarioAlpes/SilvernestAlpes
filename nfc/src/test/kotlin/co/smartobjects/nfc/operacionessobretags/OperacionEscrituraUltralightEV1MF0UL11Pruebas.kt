package co.smartobjects.nfc.operacionessobretags

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class OperacionEscrituraUltralightEV1MF0UL11Pruebas : OperacionUltralightEV1MF0UL11BasePruebas()
{
    private lateinit var operacionEscritura: OperacionEscrituraUltralightEV1BasePruebas

    @BeforeEach
    override fun antesDeCadaTest()
    {
        super.antesDeCadaTest()
        operacionEscritura = OperacionEscrituraUltralightEV1BasePruebas(operacionesCompuestas, paginasUsuarioIniciales)
    }

    @Test
    fun escribir_todas_las_paginas_ultralight_ev1_MF0UL11_sin_autenticacion()
    {
        operacionEscritura.escribir_todas_las_paginas_ultralight_ev1_sin_autenticacion()
    }

    @Test
    fun escribir_la_mitad_de_las_paginas_ultralight_ev1_MF0UL11_sin_autenticacion()
    {
        operacionEscritura.escribir_la_mitad_de_las_paginas_ultralight_ev1_sin_autenticacion()
    }

    @Test
    fun escribir_todas_las_paginas_ultralight_ev1_MF0UL11_con_autenticacion()
    {
        operacionEscritura.escribir_todas_las_paginas_ultralight_ev1_con_autenticacion()
    }

    @Test
    fun escribir_paginas_ultralight_ev1_MF0UL11_comprimiendo()
    {
        operacionEscritura.escribir_paginas_ultralight_ev1_comprimiendo()
    }

    @Test
    fun intentar_escribir_mas_paginas_de_las_existentes_ultralight_ev1_MF0UL11()
    {
        operacionEscritura.intentar_escribir_mas_paginas_de_las_existentes_ultralight_ev1()
    }

    @Test
    fun intentar_escribir_ultralight_ev1_MF0UL11_con_autenticacion_activada_y_contraseña_incorrecta()
    {
        operacionEscritura.intentar_escribir_ultralight_ev1_con_autenticacion_activada_y_contraseña_incorrecta()
    }

    @Test
    fun intentar_escribir_ultralight_ev1_MF0UL11_con_autenticacion_activada_y_pack_incorrecto()
    {
        operacionEscritura.intentar_escribir_ultralight_ev1_con_autenticacion_activada_y_pack_incorrecto()
    }

    @Test
    fun intentar_escribir_ultralight_ev1_MF0UL11_con_autenticacion_activada_pero_lectura_desprotegida()
    {
        operacionEscritura.intentar_escribir_ultralight_ev1_con_autenticacion_activada_pero_lectura_desprotegida()
    }

    @Test
    fun intentar_escribir_ultralight_ev1_MF0UL11_con_autenticacion_activada_pero_numero_de_intentos_incorrecto()
    {
        operacionEscritura.intentar_escribir_ultralight_ev1_con_autenticacion_activada_pero_numero_de_intentos_incorrecto()
    }

    @Test
    fun intentar_escribir_ultralight_ev1_MF0UL11_con_autenticacion_activada_pero_pagina_inicial_incorrecta()
    {
        operacionEscritura.intentar_escribir_ultralight_ev1_con_autenticacion_activada_pero_pagina_inicial_incorrecta()
    }
}