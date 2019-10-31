package co.smartobjects.entidades.personas

import co.smartobjects.entidades.excepciones.EntidadConCampoFueraDeRango
import co.smartobjects.entidades.excepciones.EntidadConCampoVacio
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.threeten.bp.LocalDate
import kotlin.test.assertEquals

@DisplayName("Persona")
internal class PersonaPruebas
{
    @Test
    fun hace_trim_a_nombre_completo_correctamente()
    {
        val entidadSinTrim = Persona(
                1,
                1,
                "    Prueba    ",
                Persona.TipoDocumento.TI,
                "123",
                Persona.Genero.FEMENINO,
                LocalDate.now(),
                Persona.Categoria.D,
                Persona.Afiliacion.BENEFICIARIO,
                null,
                "empresa",
                "0",
                Persona.Tipo.NO_AFILIADO
                                    )
        val entidadConTrim = Persona(
                1,
                1,
                "Prueba",
                Persona.TipoDocumento.TI,
                "123",
                Persona.Genero.FEMENINO,
                LocalDate.now(),
                Persona.Categoria.D,
                Persona.Afiliacion.BENEFICIARIO,
                null,
                "empresa",
                "0",
                Persona.Tipo.NO_AFILIADO
                                    )
        assertEquals(entidadConTrim, entidadSinTrim)
    }

    @Test
    fun hace_trim_a_numero_documento_correctamente()
    {
        val entidadSinTrim = Persona(
                1,
                1,
                "Prueba",
                Persona.TipoDocumento.TI,
                "     123      ",
                Persona.Genero.FEMENINO,
                LocalDate.now(),
                Persona.Categoria.D,
                Persona.Afiliacion.BENEFICIARIO,
                null,
                "empresa",
                "0",
                Persona.Tipo.NO_AFILIADO
                                    )
        val entidadConTrim = Persona(
                1,
                1,
                "Prueba",
                Persona.TipoDocumento.TI,
                "123",
                Persona.Genero.FEMENINO,
                LocalDate.now(),
                Persona.Categoria.D,
                Persona.Afiliacion.BENEFICIARIO,
                null,
                "empresa",
                "0",
                Persona.Tipo.NO_AFILIADO
                                    )
        assertEquals(entidadConTrim, entidadSinTrim)
    }

    @Nested
    inner class EdadCorrecta
    {
        private fun crearPersonaVariandoFechaDeNacimiento(fechaDeNacimiento: LocalDate): Persona
        {
            return Persona(
                    1,
                    1,
                    "    Prueba    ",
                    Persona.TipoDocumento.TI,
                    "123",
                    Persona.Genero.FEMENINO,
                    fechaDeNacimiento,
                    Persona.Categoria.D,
                    Persona.Afiliacion.BENEFICIARIO,
                    null,
                    "empresa",
                    "0",
                    Persona.Tipo.NO_AFILIADO
                          )
        }

        @Test
        fun si_la_fecha_de_nacimiento_es_antes_del_cumpleaños()
        {
            val ahora = LocalDate.now()

            val personaUnMesDeNacida = crearPersonaVariandoFechaDeNacimiento(ahora.minusMonths(1))
            val personaUnDiaDeNacida = crearPersonaVariandoFechaDeNacimiento(ahora.minusDays(1))

            val personaUnAñoY11MesesDeNacida = crearPersonaVariandoFechaDeNacimiento(ahora.minusYears(1).minusMonths(11))
            val personaUnAñoYUnDiaAntesDeSegundoAño = crearPersonaVariandoFechaDeNacimiento(ahora.minusYears(2).plusDays(1))

            assertEquals(0, personaUnMesDeNacida.edad)
            assertEquals(0, personaUnDiaDeNacida.edad)

            assertEquals(1, personaUnAñoY11MesesDeNacida.edad)
            assertEquals(1, personaUnAñoYUnDiaAntesDeSegundoAño.edad)
        }

        @Test
        fun si_la_fecha_de_nacimiento_es_el_dia_del_cumpleaños()
        {
            val personaConCumpleañosUnAñoAntes = crearPersonaVariandoFechaDeNacimiento(LocalDate.now().minusYears(1))

            assertEquals(1, personaConCumpleañosUnAñoAntes.edad)
        }

        @Test
        fun si_la_fecha_después_del_cumpleaños_se_calcula_correctamente()
        {
            val ahora = LocalDate.now()

            val personaUnAñoYUnDiaDespuesDeSegundoAño = crearPersonaVariandoFechaDeNacimiento(ahora.minusYears(2).minusDays(1))
            val personaUnAñoYUnMesDespuesDeSegundoAño = crearPersonaVariandoFechaDeNacimiento(ahora.minusYears(2).minusMonths(1))

            assertEquals(2, personaUnAñoYUnDiaDespuesDeSegundoAño.edad)
            assertEquals(2, personaUnAñoYUnMesDespuesDeSegundoAño.edad)
        }
    }

    @Test
    fun campos_quedan_con_valores_correctos()
    {
        val fechaNacimiento = LocalDate.now()
        val entidad = Persona(
                1,
                1,
                "Prueba",
                Persona.TipoDocumento.TI,
                "123",
                Persona.Genero.FEMENINO,
                fechaNacimiento,
                Persona.Categoria.D,
                Persona.Afiliacion.BENEFICIARIO,
                null,
                "empresa",
                "0",
                Persona.Tipo.NO_AFILIADO
                             )
        assertEquals("Prueba", entidad.campoNombreCompleto.valor)
        assertEquals("123", entidad.campoNumeroDocumento.valor)
        assertEquals(fechaNacimiento, entidad.campoFechaNacimiento.valor)
    }

    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val entidadInicial =
                Persona(
                        1,
                        1,
                        "Prueba",
                        Persona.TipoDocumento.TI,
                        "123",
                        Persona.Genero.FEMENINO,
                        LocalDate.now(),
                        Persona.Categoria.D,
                        Persona.Afiliacion.BENEFICIARIO,
                        false,
                        null,
                        "empresa",
                        "0",
                        Persona.Tipo.NO_AFILIADO
                       )

        val entidadModificada =
                Persona(
                        2,
                        3,
                        "Prueba editada",
                        Persona.TipoDocumento.CC,
                        "456",
                        Persona.Genero.MASCULINO,
                        entidadInicial.fechaNacimiento.minusDays(5),
                        Persona.Categoria.A,
                        Persona.Afiliacion.COTIZANTE,
                        true,
                        "asdfasfsdfwef",
                        "empresa",
                        "0",
                        Persona.Tipo.NO_AFILIADO
                       )

        val entidadCopiada = entidadInicial.copiar(
                2,
                3,
                "Prueba editada",
                Persona.TipoDocumento.CC,
                "456",
                Persona.Genero.MASCULINO,
                LocalDate.now().minusDays(5),
                Persona.Categoria.A,
                Persona.Afiliacion.COTIZANTE,
                true,
                "asdfasfsdfwef",
                "empresa",
                "0",
                Persona.Tipo.NO_AFILIADO
                                                  )
        assertEquals(entidadModificada, entidadCopiada)
    }

    @Test
    fun copiar_con_id_funciona_correctamente()
    {
        val entidadInicial =
                Persona(
                        1,
                        1,
                        "Prueba",
                        Persona.TipoDocumento.TI,
                        "123",
                        Persona.Genero.FEMENINO,
                        LocalDate.now(),
                        Persona.Categoria.D,
                        Persona.Afiliacion.BENEFICIARIO,
                        false,
                        null,
                        "empresa",
                        "0",
                        Persona.Tipo.NO_AFILIADO
                       )

        val entidadModificada =
                Persona(
                        1,
                        4745645,
                        "Prueba",
                        Persona.TipoDocumento.TI,
                        "123",
                        Persona.Genero.FEMENINO,
                        LocalDate.now(),
                        Persona.Categoria.D,
                        Persona.Afiliacion.BENEFICIARIO,
                        false,
                        null,
                        "empresa",
                        "0",
                        Persona.Tipo.NO_AFILIADO
                       )

        val entidadCopiada = entidadInicial.copiarConId(4745645)

        assertEquals(entidadModificada, entidadCopiada)
    }

    @Test
    fun permite_instanciar_persona_no_anonima()
    {
        val persona = Persona(
                1,
                1,
                "Persona prueba",
                Persona.TipoDocumento.TI,
                "123",
                Persona.Genero.FEMENINO,
                LocalDate.now(),
                Persona.Categoria.D,
                Persona.Afiliacion.BENEFICIARIO,
                null,
                "empresa",
                "0",
                Persona.Tipo.NO_AFILIADO
                             )
        assertEquals(false, persona.esAnonima)
    }

    @Test
    fun permite_instanciar_persona_anonima()
    {
        val persona = Persona(
                1,
                null
                             )
        val personaEsperada = Persona(
                1,
                null,
                Persona.NOMBRE_ANONIMA,
                Persona.TipoDocumento.CC,
                Persona.NUMERO_DOCUMENTO_ANONIMA,
                Persona.Genero.DESCONOCIDO,
                Persona.FECHA_NACIMIENTO_ANONIMA,
                Persona.Categoria.D,
                Persona.Afiliacion.COTIZANTE,
                true,
                null,
                "empresa",
                "0",
                Persona.Tipo.NO_AFILIADO
                                     )
        assertEquals(true, persona.esAnonima)
        assertEquals(personaEsperada, persona)
    }

    @Test
    fun no_se_permite_instanciar_con_nombre_vacio()
    {
        val excepcion = assertThrows<EntidadConCampoVacio> {
            Persona(
                    1,
                    1,
                    "",
                    Persona.TipoDocumento.CC,
                    "123",
                    Persona.Genero.FEMENINO,
                    LocalDate.now(),
                    Persona.Categoria.D,
                    Persona.Afiliacion.BENEFICIARIO,
                    null,
                    "empresa",
                    "0",
                    Persona.Tipo.NO_AFILIADO
                   )
        }

        assertEquals(Persona.Campos.NOMBRE_COMPLETO, excepcion.nombreDelCampo)
        assertEquals(Persona.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun no_se_permite_instanciar_con_nombre_con_espacios_o_tabs()
    {
        val excepcion = assertThrows<EntidadConCampoVacio> {
            Persona(
                    1,
                    1,
                    "              ",
                    Persona.TipoDocumento.CC,
                    "123",
                    Persona.Genero.FEMENINO,
                    LocalDate.now(),
                    Persona.Categoria.D,
                    Persona.Afiliacion.BENEFICIARIO,
                    null,
                    "empresa",
                    "0",
                    Persona.Tipo.NO_AFILIADO
                   )
        }

        assertEquals(Persona.Campos.NOMBRE_COMPLETO, excepcion.nombreDelCampo)
        assertEquals(Persona.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun no_se_permite_instanciar_con_numero_de_documento_vacio()
    {
        val excepcion = assertThrows<EntidadConCampoVacio> {
            Persona(
                    1,
                    1,
                    "Persona de prueba",
                    Persona.TipoDocumento.CC,
                    "",
                    Persona.Genero.FEMENINO,
                    LocalDate.now(),
                    Persona.Categoria.D,
                    Persona.Afiliacion.BENEFICIARIO,
                    null,
                    "empresa",
                    "0",
                    Persona.Tipo.NO_AFILIADO
                   )
        }

        assertEquals(Persona.Campos.NUMERO_DOCUMENTO, excepcion.nombreDelCampo)
        assertEquals(Persona.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun no_se_permite_instanciar_con_numero_de_documento_con_espacios_o_tabs()
    {
        val excepcion = assertThrows<EntidadConCampoVacio> {
            Persona(
                    1,
                    1,
                    "Persona de prueba",
                    Persona.TipoDocumento.CC,
                    "              ",
                    Persona.Genero.FEMENINO,
                    LocalDate.now(),
                    Persona.Categoria.D,
                    Persona.Afiliacion.BENEFICIARIO,
                    null,
                    "empresa",
                    "0",
                    Persona.Tipo.NO_AFILIADO
                   )
        }

        assertEquals(Persona.Campos.NUMERO_DOCUMENTO, excepcion.nombreDelCampo)
        assertEquals(Persona.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun no_se_permite_instanciar_con_fecha_futura_de_nacimiento()
    {
        val fechaLimite = LocalDate.now()

        val fechaAUsar = fechaLimite.plusDays(2)

        val excepcion = assertThrows<EntidadConCampoFueraDeRango> {
            Persona(
                    1,
                    1,
                    "Persona de prueba",
                    Persona.TipoDocumento.CC,
                    "123",
                    Persona.Genero.FEMENINO,
                    fechaAUsar,
                    Persona.Categoria.D,
                    Persona.Afiliacion.BENEFICIARIO,
                    null,
                    "empresa",
                    "0",
                    Persona.Tipo.NO_AFILIADO
                   )
        }

        assertEquals(Persona.Campos.FECHA_NACIMIENTO, excepcion.nombreDelCampo)
        assertEquals(Persona.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        assertEquals(fechaAUsar.toString(), excepcion.valorUsado)
        assertEquals(fechaLimite.toString(), excepcion.valorDelLimite)
        assertEquals(EntidadConCampoFueraDeRango.Limite.SUPERIOR, excepcion.limiteSobrepasado)
    }

    @Test
    fun el_documento_completo_es_la_concatenacion_del_tipo_de_documento_y_el_numero()
    {
        val valorEsperado = "NUIP 123456789"
        val persona = Persona(
                1,
                null,
                "no importa",
                Persona.TipoDocumento.NUIP,
                "123456789",
                Persona.Genero.DESCONOCIDO,
                LocalDate.now(),
                Persona.Categoria.D,
                Persona.Afiliacion.COTIZANTE,
                null,
                "empresa",
                "0",
                Persona.Tipo.NO_AFILIADO
                             )

        assertEquals(valorEsperado, persona.documentoCompleto)
    }
}