package co.smartobjects.entidades.personas

import co.smartobjects.entidades.excepciones.EntidadConValorEnColeccionNoPermitido
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.threeten.bp.LocalDate
import kotlin.test.assertEquals


internal class PersonaConFamiliaresPruebas
{
    @Test
    fun campos_quedan_con_valores_correctos()
    {
        val persona = Persona(
                1,
                1,
                "Persona",
                Persona.TipoDocumento.CC,
                "12345",
                Persona.Genero.FEMENINO,
                LocalDate.now(),
                Persona.Categoria.D,
                Persona.Afiliacion.BENEFICIARIO,
                null,
                "empresa",
                "0",
                Persona.Tipo.NO_AFILIADO
                             )

        val familiar1 = Persona(
                1,
                2,
                "Familiar 1",
                Persona.TipoDocumento.TI,
                "4321",
                Persona.Genero.MASCULINO,
                LocalDate.now(),
                Persona.Categoria.D,
                Persona.Afiliacion.BENEFICIARIO,
                null,
                "empresa",
                "0",
                Persona.Tipo.NO_AFILIADO
                               )

        val familiar2 = Persona(
                1,
                2,
                "Familiar 2",
                Persona.TipoDocumento.TI,
                "4321",
                Persona.Genero.MASCULINO,
                LocalDate.now(),
                Persona.Categoria.D,
                Persona.Afiliacion.BENEFICIARIO,
                null,
                "empresa",
                "0",
                Persona.Tipo.NO_AFILIADO
                               )

        val entidad = PersonaConFamiliares(persona, setOf(familiar1, familiar2))

        assertEquals(setOf(familiar1, familiar2), entidad.campoFamiliares.valor)
    }

    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val persona = Persona(
                1,
                1,
                "Persona",
                Persona.TipoDocumento.CC,
                "12345",
                Persona.Genero.FEMENINO,
                LocalDate.now(),
                Persona.Categoria.D,
                Persona.Afiliacion.BENEFICIARIO,
                null,
                "empresa",
                "0",
                Persona.Tipo.NO_AFILIADO
                             )

        val familiar1 = Persona(
                1,
                2,
                "Familiar 1",
                Persona.TipoDocumento.TI,
                "4321",
                Persona.Genero.MASCULINO,
                LocalDate.now(),
                Persona.Categoria.D,
                Persona.Afiliacion.BENEFICIARIO,
                null,
                "empresa",
                "0",
                Persona.Tipo.NO_AFILIADO
                               )

        val familiar2 = Persona(
                1,
                2,
                "Familiar 2",
                Persona.TipoDocumento.TI,
                "4321",
                Persona.Genero.MASCULINO,
                LocalDate.now(),
                Persona.Categoria.D,
                Persona.Afiliacion.BENEFICIARIO,
                null,
                "empresa",
                "0",
                Persona.Tipo.NO_AFILIADO
                               )

        val entidadInicial = PersonaConFamiliares(persona, setOf(familiar1, familiar2))


        val personaModificada = Persona(
                1,
                1,
                "Persona",
                Persona.TipoDocumento.PA,
                "34737",
                Persona.Genero.MASCULINO,
                LocalDate.now().minusMonths(1),
                Persona.Categoria.A,
                Persona.Afiliacion.COTIZANTE,
                "asdfasdf",
                "empresa",
                "0",
                Persona.Tipo.NO_AFILIADO
                                       )

        val familiar1Modificada = Persona(
                1,
                2,
                "fgjtyk",
                Persona.TipoDocumento.RC,
                "484584",
                Persona.Genero.DESCONOCIDO,
                LocalDate.now().minusMonths(1),
                Persona.Categoria.B,
                Persona.Afiliacion.COTIZANTE,
                "asdfasdf",
                "empresa",
                "0",
                Persona.Tipo.NO_AFILIADO
                                         )

        val familiar2Modificada = Persona(
                1,
                2,
                "tuwrnn",
                Persona.TipoDocumento.CD,
                "4321",
                Persona.Genero.FEMENINO,
                LocalDate.now().minusMonths(1),
                Persona.Categoria.C,
                Persona.Afiliacion.COTIZANTE,
                "asdfasdf",
                "empresa",
                "0",
                Persona.Tipo.NO_AFILIADO
                                         )

        val entidadModificada = PersonaConFamiliares(personaModificada, setOf(familiar1Modificada, familiar2Modificada))

        val entidadCopiada = entidadInicial.copiar(personaModificada, setOf(familiar1Modificada, familiar2Modificada))

        assertEquals(entidadModificada, entidadCopiada)
    }

    @Test
    fun no_se_permite_instanciar_con_la_persona_dentro_de_los_familiares()
    {
        val persona = Persona(
                1,
                1,
                "Persona",
                Persona.TipoDocumento.CC,
                "12345",
                Persona.Genero.FEMENINO,
                LocalDate.now(),
                Persona.Categoria.D,
                Persona.Afiliacion.BENEFICIARIO,
                null,
                "empresa",
                "0",
                Persona.Tipo.NO_AFILIADO
                             )

        val familiar1 = Persona(
                1,
                2,
                "Familiar 1",
                Persona.TipoDocumento.TI,
                "4321",
                Persona.Genero.MASCULINO,
                LocalDate.now(),
                Persona.Categoria.D,
                Persona.Afiliacion.BENEFICIARIO,
                null,
                "empresa",
                "0",
                Persona.Tipo.NO_AFILIADO
                               )

        val excepcion = assertThrows<EntidadConValorEnColeccionNoPermitido> {
            PersonaConFamiliares(persona, setOf(familiar1, persona))
        }

        assertEquals(PersonaConFamiliares.Campos.FAMILIARES, excepcion.nombreDelCampo)
        assertEquals(PersonaConFamiliares.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        assertEquals(setOf(familiar1, persona).joinToString(), excepcion.valorUsado)
    }
}