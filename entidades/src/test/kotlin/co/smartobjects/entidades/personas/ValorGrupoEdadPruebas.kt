package co.smartobjects.entidades.personas


import co.smartobjects.entidades.excepciones.EntidadConCampoFueraDeRango
import co.smartobjects.entidades.excepciones.EntidadConCampoVacio
import co.smartobjects.entidades.excepciones.RelacionEntreCamposInvalida
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.junit.jupiter.api.*
import org.threeten.bp.LocalDate
import kotlin.test.assertEquals

@DisplayName("CampoDePersona")
internal class ValorGrupoEdadPruebas
{
    @Test
    fun hace_trim_a_nombre_correctamente()
    {
        val entidadSinTrim = ValorGrupoEdad("    Prueba    ", 0, 0)
        val entidadConTrim = ValorGrupoEdad("Prueba", 0, 0)
        assertEquals(entidadConTrim, entidadSinTrim)
    }

    @Test
    fun campos_quedan_con_valores_correctos()
    {
        val entidad = ValorGrupoEdad("Prueba", 0, 0)
        assertEquals("Prueba", entidad.campoValor.valor)
        assertEquals(0, entidad.campoEdadMinima.valor)
    }

    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val entidadInicial = ValorGrupoEdad("Prueba", 0, 0)
        val entidadCopiada = entidadInicial.copiar(valor = "Prueba editada", edadMinima = null, edadMaxima = 100)
        val entidadEsperada = ValorGrupoEdad("Prueba editada", null, 100)
        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun copiar_con_id_funciona_correctamente()
    {
        val entidadInicial = ValorGrupoEdad("Prueba", 0, 0)
        val entidadEsperada = ValorGrupoEdad("Nuevo valor", 0, 0)

        val entidadCopiada = entidadInicial.copiarConId(ValorGrupoEdad.CampoValor("Nuevo valor"))

        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun no_se_permite_instanciar_con_campo_vacio()
    {
        val excepcion = assertThrows<EntidadConCampoVacio> {
            ValorGrupoEdad("", 0, 0)
        }

        assertEquals(ValorGrupoEdad.Campos.VALOR, excepcion.nombreDelCampo)
        assertEquals(ValorGrupoEdad.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun no_se_permite_instanciar_con_campo_con_espacios_o_tabs()
    {
        val excepcion = assertThrows<EntidadConCampoVacio> {
            ValorGrupoEdad("                 ", 0, 0)
        }

        assertEquals(ValorGrupoEdad.Campos.VALOR, excepcion.nombreDelCampo)
        assertEquals(ValorGrupoEdad.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun la_fecha_de_validez_desde_es_siempre_menor_o_igual_a_la_fecha_final()
    {
        val edadMinima = 10
        val edadMaxima = edadMinima - 1

        val excepcion = assertThrows<RelacionEntreCamposInvalida> {
            ValorGrupoEdad("valor asociado", edadMinima, edadMaxima)
        }

        assertEquals(ValorGrupoEdad.Campos.EDAD_MINIMA, excepcion.nombreDelCampo)
        assertEquals(ValorGrupoEdad.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        assertEquals(ValorGrupoEdad.Campos.EDAD_MINIMA, excepcion.nombreDelCampoIzquierdo)
        assertEquals(ValorGrupoEdad.Campos.EDAD_MAXIMA, excepcion.nombreDelCampoDerecho)
        assertEquals(edadMinima.toString(), excepcion.valorUsadoPorCampoIzquierdo)
        assertEquals(edadMaxima.toString(), excepcion.valorUsadoPorCampoDerecho)
        assertEquals(RelacionEntreCamposInvalida.Relacion.MENOR, excepcion.relacionViolada)
    }

    @Test
    fun no_se_permite_instanciar_con_edad_minima_inferior_a_cero()
    {
        val edadMinima = -1
        val edadMaxima = 100

        val excepcion = assertThrows<EntidadConCampoFueraDeRango> {
            ValorGrupoEdad("valor asociado", edadMinima, edadMaxima)
        }

        assertEquals(ValorGrupoEdad.Campos.EDAD_MINIMA, excepcion.nombreDelCampo)
        assertEquals(ValorGrupoEdad.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        assertEquals(edadMinima.toString(), excepcion.valorUsado)
        assertEquals("0", excepcion.valorDelLimite)
        assertEquals(EntidadConCampoFueraDeRango.Limite.INFERIOR, excepcion.limiteSobrepasado)
    }

    @Nested
    inner class AplicaParaPersona
    {
        val personaPruebas = Persona(
                1,
                1,
                "Prueba",
                Persona.TipoDocumento.TI,
                "123",
                Persona.Genero.FEMENINO,
                LocalDate.now(ZONA_HORARIA_POR_DEFECTO).minusYears(10),
                Persona.Categoria.A,
                Persona.Afiliacion.BENEFICIARIO,
                null,
                "empresa",
                "0",
                Persona.Tipo.NO_AFILIADO
                                    )

        @Test
        fun retorna_false_con_valor_inferior_al_rango()
        {
            val grupo = ValorGrupoEdad("sin match +", personaPruebas.edad + 1, null)
            Assertions.assertFalse(grupo.aplicaParaPersona(personaPruebas))
        }

        @Test
        fun retorna_false_con_valor_superior_al_rango()
        {
            val grupo = ValorGrupoEdad("sin match -", null, personaPruebas.edad - 1)
            Assertions.assertFalse(grupo.aplicaParaPersona(personaPruebas))
        }

        @Test
        fun retorna_true_con_valor_igual_a_rango_puntual()
        {
            val grupo = ValorGrupoEdad("match exacto", personaPruebas.edad, personaPruebas.edad)
            Assertions.assertTrue(grupo.aplicaParaPersona(personaPruebas))
        }

        @Test
        fun retorna_true_con_valor_en_a_rango_cerrado()
        {
            val grupo = ValorGrupoEdad("match no exacto", personaPruebas.edad - 10, personaPruebas.edad + 10)
            Assertions.assertTrue(grupo.aplicaParaPersona(personaPruebas))
        }

        @Test
        fun retorna_true_con_valor_en_a_rango_abierto_hacia_arriba()
        {
            val grupo = ValorGrupoEdad("match abierto +", personaPruebas.edad,null)
            Assertions.assertTrue(grupo.aplicaParaPersona(personaPruebas))
        }

        @Test
        fun retorna_true_con_valor_en_a_rango_abierto_hacia_abajo()
        {
            val grupo = ValorGrupoEdad("match abierto -", null, personaPruebas.edad)
            Assertions.assertTrue(grupo.aplicaParaPersona(personaPruebas))
        }

        @Test
        fun retorna_true_con_valor_en_a_rango_completo()
        {
            val grupo =  ValorGrupoEdad("match completo", null, null)
            Assertions.assertTrue(grupo.aplicaParaPersona(personaPruebas))
        }
    }
}