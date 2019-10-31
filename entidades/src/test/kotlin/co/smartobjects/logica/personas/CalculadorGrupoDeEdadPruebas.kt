package co.smartobjects.logica.personas

import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.personas.ValorGrupoEdad
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.threeten.bp.LocalDate

@DisplayName("CalculadorGrupoDeEdadEnMemoria")
internal class CalculadorGrupoDeEdadEnMemoriaPruebas
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
            "",
            Persona.Tipo.NO_AFILIADO
    )
    private val grupoMatchExacto = ValorGrupoEdad("match exacto", personaPruebas.edad, personaPruebas.edad)
    private val grupoMatchNoExacto = ValorGrupoEdad("match no exacto", personaPruebas.edad - 10, personaPruebas.edad + 10)
    private val grupoMatchAbiertoHaciaArriba = ValorGrupoEdad("match abierto +", personaPruebas.edad,null)
    private val grupoMatchAbiertoHaciaAbajo = ValorGrupoEdad("match abierto -", null, personaPruebas.edad)
    private val grupoMatchCompleto = ValorGrupoEdad("match completo", null, null)
    private val grupoSinMatchInferior = ValorGrupoEdad("sin match -", null, personaPruebas.edad - 11)
    private val grupoSinMatchSuperior = ValorGrupoEdad("sin match +", personaPruebas.edad + 11, null)

    @Nested
    inner class ConListaVacia
    {
        private val calculadorGrupoDeEdad: CalculadorGrupoDeEdad = CalculadorGrupoEdadEnMemoria(listOf())

        @Test
        fun retorna_null()
        {
            Assertions.assertNull(calculadorGrupoDeEdad.darGrupoEdadParaPersona(personaPruebas))
        }
    }

    @Nested
    inner class ConListaConValorConMatchExacto
    {
        private val calculadorGrupoDeEdad: CalculadorGrupoDeEdad = CalculadorGrupoEdadEnMemoria(listOf(grupoSinMatchInferior, grupoMatchExacto, grupoSinMatchSuperior))

        @Test
        fun retorna_el_valor_correcto()
        {
            Assertions.assertEquals(grupoMatchExacto, calculadorGrupoDeEdad.darGrupoEdadParaPersona(personaPruebas))
        }
    }

    @Nested
    inner class ConListaConValorConMatchNoExacto
    {
        private val calculadorGrupoDeEdad: CalculadorGrupoDeEdad = CalculadorGrupoEdadEnMemoria(listOf(grupoSinMatchInferior, grupoMatchNoExacto, grupoSinMatchSuperior))

        @Test
        fun retorna_el_valor_correcto()
        {
            Assertions.assertEquals(grupoMatchNoExacto, calculadorGrupoDeEdad.darGrupoEdadParaPersona(personaPruebas))
        }
    }

    @Nested
    inner class ConListaConValorConMatchAbiertoHaciaArriba
    {
        private val calculadorGrupoDeEdad: CalculadorGrupoDeEdad = CalculadorGrupoEdadEnMemoria(listOf(grupoSinMatchInferior, grupoMatchAbiertoHaciaArriba))

        @Test
        fun retorna_el_valor_correcto()
        {
            Assertions.assertEquals(grupoMatchAbiertoHaciaArriba, calculadorGrupoDeEdad.darGrupoEdadParaPersona(personaPruebas))
        }
    }

    @Nested
    inner class ConListaConValorConMatchAbiertoHaciaAbajo
    {
        private val calculadorGrupoDeEdad: CalculadorGrupoDeEdad = CalculadorGrupoEdadEnMemoria(listOf(grupoMatchAbiertoHaciaAbajo, grupoSinMatchSuperior))

        @Test
        fun retorna_el_valor_correcto()
        {
            Assertions.assertEquals(grupoMatchAbiertoHaciaAbajo, calculadorGrupoDeEdad.darGrupoEdadParaPersona(personaPruebas))
        }
    }

    @Nested
    inner class ConListaConValorConMatchCompleto
    {
        private val calculadorGrupoDeEdad: CalculadorGrupoDeEdad = CalculadorGrupoEdadEnMemoria(listOf(grupoMatchCompleto))

        @Test
        fun retorna_el_valor_correcto()
        {
            Assertions.assertEquals(grupoMatchCompleto, calculadorGrupoDeEdad.darGrupoEdadParaPersona(personaPruebas))
        }
    }

    @Nested
    inner class ConListaConValorSinMatch
    {
        private val calculadorGrupoDeEdad: CalculadorGrupoDeEdad = CalculadorGrupoEdadEnMemoria(listOf(grupoSinMatchInferior, grupoSinMatchSuperior))

        @Test
        fun retorna_null()
        {
            Assertions.assertNull(calculadorGrupoDeEdad.darGrupoEdadParaPersona(personaPruebas))
        }
    }

    @Nested
    inner class ConListaConValorConMultiplesMatchs
    {
        private val calculadorGrupoDeEdad: CalculadorGrupoDeEdad = CalculadorGrupoEdadEnMemoria(listOf(grupoSinMatchInferior, grupoMatchNoExacto, grupoSinMatchSuperior, grupoMatchAbiertoHaciaArriba, grupoMatchCompleto))

        @Test
        fun retorna_primer_match()
        {
            Assertions.assertEquals(grupoMatchNoExacto, calculadorGrupoDeEdad.darGrupoEdadParaPersona(personaPruebas))
        }
    }
}