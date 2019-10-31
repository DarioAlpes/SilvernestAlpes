package co.smartobjects.ui.modelos.registropersonas

import co.smartobjects.entidades.personas.DocumentoCompleto
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.personas.PersonaConFamiliares
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.personas.PersonasAPI
import co.smartobjects.red.modelos.ErrorDePeticion
import co.smartobjects.red.modelos.personas.PersonaDTO
import co.smartobjects.ui.modelos.PruebasModelosRxBase
import co.smartobjects.ui.modelos.ResultadoAccionUI
import co.smartobjects.ui.modelos.schedulerThreadActual
import co.smartobjects.utilidades.Opcional
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.threeten.bp.LocalDate
import java.io.IOException

@DisplayName("ProcesoConsultarFamiliaresConSujetos")
internal class ProcesoConsultarFamiliaresConSujetosPruebas : PruebasModelosRxBase()
{
    companion object
    {
        private const val ID_CLIENTE = 10L
    }

    private val mockApiPersonas = mock(PersonasAPI::class.java, {
        throw RuntimeException("Llamado a\n\t${it.method.declaringClass.simpleName}.${it.method.name}(\n\t\t${it.arguments.joinToString(",\n\t\t")}\n\t)\n\tno esta mockeado")
    })


    private fun darPersonaSinIdSegunIndice(indice: Int): Persona
    {
        return Persona(
                ID_CLIENTE,
                null,
                "Entidad prueba $indice",
                Persona.TipoDocumento.values()[indice % Persona.TipoDocumento.values().size],
                "Documento $indice",
                Persona.Genero.values()[indice % Persona.Genero.values().size],
                LocalDate.of(1980 + (indice % (LocalDate.now().year - 1980)), 1, 1),
                Persona.Categoria.values()[indice % Persona.Categoria.values().size],
                Persona.Afiliacion.values()[indice % Persona.Afiliacion.values().size],
                indice % 2 == 0,
                "Llave $indice",
                "Empresa $indice",
                "NitEmpresa $indice",
                Persona.Tipo.values()[indice % Persona.Tipo.values().size]
                      )
    }

    private fun darPersonaConIdSegunIndice(indice: Int): Persona
    {
        return darPersonaSinIdSegunIndice(indice).copiar(id = indice.toLong())
    }

    private fun mockearRespuestaDeRedExitosaAlConsultar(personaConFamiliares: PersonaConFamiliares)
    {
        doReturn(RespuestaIndividual.Exitosa(personaConFamiliares))
            .`when`(mockApiPersonas)
            .consultarPorDocumento(DocumentoCompleto(personaConFamiliares.persona))
    }

    private fun mockearRespuestaDeRedInvalidaAlConsultar(persona: Persona, errorEsperado: IllegalStateException)
    {
        doThrow(errorEsperado)
            .`when`(mockApiPersonas)
            .consultarPorDocumento(DocumentoCompleto(persona))
    }

    private fun mockearRespuestaDeRedErrorTimeoutAlConsultar(persona: Persona)
    {
        doReturn(RespuestaIndividual.Error.Timeout<Persona>())
            .`when`(mockApiPersonas)
            .consultarPorDocumento(DocumentoCompleto(persona))
    }

    private fun mockearRespuestaDeRedErrorRedAlConsultar(persona: Persona)
    {
        doReturn(RespuestaIndividual.Error.Red<Persona>(IOException("Error")))
            .`when`(mockApiPersonas)
            .consultarPorDocumento(DocumentoCompleto(persona))
    }

    private fun mockearRespuestaDeRedErrorBackAlConsultar(persona: Persona, mensajeError: String)
    {
        doReturn(RespuestaIndividual.Error.Back<Persona>(400, ErrorDePeticion(100, mensajeError)))
            .`when`(mockApiPersonas)
            .consultarPorDocumento(DocumentoCompleto(persona))
    }

    private fun mockearRespuestaDeRedErrorBackPersonaNoExisteAlConsultar(persona: Persona)
    {
        doReturn(RespuestaIndividual.Error.Back<Persona>(400, ErrorDePeticion(PersonaDTO.CodigosError.NO_EXISTE, "No existe")))
            .`when`(mockApiPersonas)
            .consultarPorDocumento(DocumentoCompleto(persona))
    }

    private val proceso: ProcesoConsultarFamiliares by lazy { ProcesoConsultarFamiliaresConSujetos(mockApiPersonas, schedulerThreadActual) }

    @Nested
    inner class PersonaCreada
    {
        private val testPersonaConsultada = proceso.personaConsultada.test()

        @Test
        fun empieza_sin_valor()
        {
            testPersonaConsultada.assertValueCount(0)
        }
    }

    @Nested
    inner class Estado
    {
        private val testEstado = proceso.estado.test()

        @Test
        fun empieza_con_valor_ESPERANDO_DATOS()
        {
            testEstado.assertValue(ProcesoConsultarFamiliares.Estado.ESPERANDO_DATOS)
            testEstado.assertValueCount(1)
        }
    }

    @Nested
    inner class Familiares
    {
        private val testFamiliares = proceso.familiares.test()

        @Test
        fun empieza_con_lista_vacia()
        {
            testFamiliares.assertValue { !it.any() }
            testFamiliares.assertValueCount(1)
        }
    }

    @Nested
    inner class ErrorGlobal
    {
        private val testErrorGlobal by lazy { proceso.errorGlobal.test() }

        @Test
        fun empieza_con_valor_vacio()
        {
            testErrorGlobal.assertValue(Opcional.Vacio())
            testErrorGlobal.assertValueCount(1)
        }
    }

    @Nested
    inner class AgregarPersonaConFamiliares
    {

        @Nested
        inner class ConPersonaConIdNull
        {
            @Test
            fun lanza_IllegalArgumentException()
            {
                Assertions.assertThrows(IllegalArgumentException::class.java) {
                    proceso.agregarPersonaConFamiliares(PersonaConFamiliares(darPersonaSinIdSegunIndice(1), setOf()))
                }
            }

            @Nested
            inner class Familiares
            {
                private val testFamiliares = proceso.familiares.test()

                @Test
                fun no_emite_mas_valores_cuando_no_hay_familia()
                {
                    Assertions.assertThrows(IllegalArgumentException::class.java) {
                        proceso.agregarPersonaConFamiliares(PersonaConFamiliares(darPersonaSinIdSegunIndice(1), setOf()))
                    }
                    testFamiliares.assertValueCount(1)
                }

                @Test
                fun no_emite_mas_valores_cuando_hay_familia()
                {
                    Assertions.assertThrows(IllegalArgumentException::class.java) {
                        proceso.agregarPersonaConFamiliares(PersonaConFamiliares(darPersonaSinIdSegunIndice(1), setOf(darPersonaConIdSegunIndice(2))))
                    }
                    testFamiliares.assertValueCount(1)
                }
            }
        }

        @Nested
        inner class ConPersonaValida
        {
            @Nested
            inner class Familiares
            {
                private val testFamiliares = proceso.familiares.test()

                @Test
                fun emite_lista_vacia_cuando_no_hay_familia()
                {
                    proceso.agregarPersonaConFamiliares(PersonaConFamiliares(darPersonaConIdSegunIndice(1), setOf()))
                    testFamiliares.assertValueCount(2)
                    testFamiliares.assertValueAt(1) { !it.any() }
                }

                @Test
                fun emite_nueva_lista_con_familia_en_orden_cuando_hay_familia()
                {
                    val familia = setOf(darPersonaConIdSegunIndice(4), darPersonaSinIdSegunIndice(3), darPersonaConIdSegunIndice(2))
                    proceso.agregarPersonaConFamiliares(PersonaConFamiliares(darPersonaConIdSegunIndice(1), familia))
                    testFamiliares.assertValueCount(2)
                    testFamiliares.assertValueAt(1) { it.toList().size == familia.size }
                    testFamiliares.assertValueAt(1) { it.zip(familia).all { it.first == it.second } }
                }

                @Test
                fun con_familiar_con_mismo_documento_que_persona_emite_nueva_lista_con_familia_en_orden_sin_familiar_con_documento_de_persona()
                {
                    val persona = darPersonaConIdSegunIndice(1)
                    val familiarMismoDocumento = darPersonaSinIdSegunIndice(3).copiar(tipoDocumento = persona.tipoDocumento, numeroDocumento = persona.numeroDocumento)
                    val familia = setOf(darPersonaConIdSegunIndice(4), familiarMismoDocumento, darPersonaConIdSegunIndice(2))
                    val familiaEsperada = listOf(darPersonaConIdSegunIndice(4), darPersonaConIdSegunIndice(2))
                    proceso.agregarPersonaConFamiliares(PersonaConFamiliares(persona, familia))
                    testFamiliares.assertValueCount(2)
                    testFamiliares.assertValueAt(1) { it.toList().size == familiaEsperada.size }
                    testFamiliares.assertValueAt(1) { it.zip(familiaEsperada).all { it.first == it.second } }
                }

                @Test
                fun con_dos_familiares_con_mismo_documento_emite_nueva_lista_con_primer_familiar_con_documento_duplicado()
                {
                    val persona = darPersonaConIdSegunIndice(1)
                    val familiarMismoDocumento1 = darPersonaSinIdSegunIndice(3)
                    val familiarMismoDocumento2 =
                            darPersonaConIdSegunIndice(2)
                                .copiar(tipoDocumento = familiarMismoDocumento1.tipoDocumento, numeroDocumento = familiarMismoDocumento1.numeroDocumento)
                    val familia = setOf(darPersonaConIdSegunIndice(4), familiarMismoDocumento1, familiarMismoDocumento2)
                    val familiaEsperada = setOf(darPersonaConIdSegunIndice(4), familiarMismoDocumento1)
                    proceso.agregarPersonaConFamiliares(PersonaConFamiliares(persona, familia))
                    testFamiliares.assertValueCount(2)
                    testFamiliares.assertValueAt(1) { it.toList().size == familiaEsperada.size }
                    testFamiliares.assertValueAt(1) { it.zip(familiaEsperada).all { it.first == it.second } }
                }

                @Test
                fun emite_nueva_lista_con_misma_familia_cuando_se_agrega_segunda_persona_sin_familia()
                {
                    val familia = setOf(darPersonaConIdSegunIndice(4), darPersonaSinIdSegunIndice(3), darPersonaConIdSegunIndice(2))
                    proceso.agregarPersonaConFamiliares(PersonaConFamiliares(darPersonaConIdSegunIndice(1), familia))
                    proceso.agregarPersonaConFamiliares(PersonaConFamiliares(darPersonaConIdSegunIndice(6), familia))
                    testFamiliares.assertValueCount(3)
                    testFamiliares.assertValueAt(2) { it.toList().size == familia.size }
                    testFamiliares.assertValueAt(2) { it.zip(familia).all { it.first == it.second } }
                }

                @Test
                fun emite_nueva_lista_con_familias_en_orden_cuando_se_agrega_segunda_persona_con_familia()
                {
                    val familia1 = setOf(darPersonaConIdSegunIndice(4), darPersonaSinIdSegunIndice(3), darPersonaConIdSegunIndice(2))
                    val familia2 = setOf(darPersonaConIdSegunIndice(9), darPersonaSinIdSegunIndice(8), darPersonaConIdSegunIndice(7))
                    val familiaCompleta = familia1 + familia2
                    proceso.agregarPersonaConFamiliares(PersonaConFamiliares(darPersonaConIdSegunIndice(1), familia1))
                    proceso.agregarPersonaConFamiliares(PersonaConFamiliares(darPersonaConIdSegunIndice(6), familia2))
                    testFamiliares.assertValueCount(3)
                    testFamiliares.assertValueAt(2) { it.toList().size == familiaCompleta.size }
                    testFamiliares.assertValueAt(2) { it.zip(familiaCompleta).all { it.first == it.second } }
                }

                @Test
                fun emite_nueva_lista_sin_persona_al_agregar_persona_que_estaba_en_familia_previa()
                {
                    val persona2 = darPersonaConIdSegunIndice(6)
                    val familia = setOf(darPersonaConIdSegunIndice(4), persona2, darPersonaConIdSegunIndice(2))
                    val familiaEsperada = setOf(darPersonaConIdSegunIndice(4), darPersonaConIdSegunIndice(2))
                    proceso.agregarPersonaConFamiliares(PersonaConFamiliares(darPersonaConIdSegunIndice(1), familia))
                    proceso.agregarPersonaConFamiliares(PersonaConFamiliares(persona2, setOf()))
                    testFamiliares.assertValueCount(3)
                    testFamiliares.assertValueAt(2) { it.toList().size == familiaEsperada.size }
                    testFamiliares.assertValueAt(2) { it.zip(familiaEsperada).all { it.first == it.second } }
                }

                @Test
                fun emite_nueva_lista_sin_persona_al_agregar_persona_con_mismo_documento_que_persona_en_familia_previa()
                {
                    val persona2 = darPersonaConIdSegunIndice(6)
                    val personaConMismoDocumento = darPersonaSinIdSegunIndice(3).copiar(tipoDocumento = persona2.tipoDocumento, numeroDocumento = persona2.numeroDocumento)
                    val familia = setOf(darPersonaConIdSegunIndice(4), personaConMismoDocumento, darPersonaConIdSegunIndice(2))
                    val familiaEsperada = setOf(darPersonaConIdSegunIndice(4), darPersonaConIdSegunIndice(2))
                    proceso.agregarPersonaConFamiliares(PersonaConFamiliares(darPersonaConIdSegunIndice(1), familia))
                    proceso.agregarPersonaConFamiliares(PersonaConFamiliares(persona2, setOf()))
                    testFamiliares.assertValueCount(3)
                    testFamiliares.assertValueAt(2) { it.toList().size == familiaEsperada.size }
                    testFamiliares.assertValueAt(2) { it.zip(familiaEsperada).all { it.first == it.second } }
                }

                @Test
                fun emite_nueva_lista_con_una_sola_aparicion_de_cada_persona_duplicada()
                {
                    val familiarDuplicado1 = darPersonaConIdSegunIndice(4)
                    val familiarDuplicado2 = darPersonaConIdSegunIndice(9)
                    val familia1 = setOf(familiarDuplicado1, darPersonaSinIdSegunIndice(3), familiarDuplicado2)
                    val familia2 = setOf(familiarDuplicado2, familiarDuplicado1, darPersonaConIdSegunIndice(7))
                    val familiaCompleta = setOf(familiarDuplicado1, darPersonaSinIdSegunIndice(3), familiarDuplicado2, darPersonaConIdSegunIndice(7))
                    proceso.agregarPersonaConFamiliares(PersonaConFamiliares(darPersonaConIdSegunIndice(1), familia1))
                    proceso.agregarPersonaConFamiliares(PersonaConFamiliares(darPersonaConIdSegunIndice(6), familia2))
                    testFamiliares.assertValueCount(3)
                    testFamiliares.assertValueAt(2) { it.toList().size == familiaCompleta.size }
                    testFamiliares.assertValueAt(2) { it.zip(familiaCompleta).all { it.first == it.second } }
                }

                @Test
                fun emite_nueva_lista_con_una_sola_aparicion_de_cada_persona_con_mismo_documento_y_mantiene_primera_aparicion()
                {
                    val familiar1 = darPersonaConIdSegunIndice(4)
                    val personaConMismoDocumento1 = darPersonaSinIdSegunIndice(8).copiar(tipoDocumento = familiar1.tipoDocumento, numeroDocumento = familiar1.numeroDocumento)
                    val familiar2 = darPersonaConIdSegunIndice(2)
                    val personaConMismoDocumento2 = darPersonaSinIdSegunIndice(9).copiar(tipoDocumento = familiar2.tipoDocumento, numeroDocumento = familiar2.numeroDocumento)
                    val familia1 = setOf(familiar1, darPersonaSinIdSegunIndice(3), familiar2)
                    val familia2 = setOf(personaConMismoDocumento2, personaConMismoDocumento1, darPersonaConIdSegunIndice(7))
                    val familiaCompleta = setOf(familiar1, darPersonaSinIdSegunIndice(3), familiar2, darPersonaConIdSegunIndice(7))
                    proceso.agregarPersonaConFamiliares(PersonaConFamiliares(darPersonaConIdSegunIndice(1), familia1))
                    proceso.agregarPersonaConFamiliares(PersonaConFamiliares(darPersonaConIdSegunIndice(6), familia2))
                    testFamiliares.assertValueCount(3)
                    testFamiliares.assertValueAt(2) { it.toList().size == familiaCompleta.size }
                    testFamiliares.assertValueAt(2) { it.zip(familiaCompleta).all { it.first == it.second } }
                }
            }
        }
    }

    @Nested
    inner class EliminarPersonaConFamiliares
    {

        @Nested
        inner class ConPersonaConIdNull
        {
            @Test
            fun lanza_IllegalArgumentException()
            {
                Assertions.assertThrows(IllegalArgumentException::class.java) {
                    proceso.eliminarPersonaConFamiliares(darPersonaSinIdSegunIndice(1))
                }
            }

            @Nested
            inner class Familiares
            {
                private val testFamiliares = proceso.familiares.test()

                @Test
                fun no_emite_mas_valores()
                {
                    Assertions.assertThrows(IllegalArgumentException::class.java) {
                        proceso.eliminarPersonaConFamiliares(darPersonaSinIdSegunIndice(1))
                    }
                    testFamiliares.assertValueCount(1)
                }
            }
        }

        @Nested
        inner class ConPersonaValida
        {
            @Nested
            inner class Familiares
            {
                private val testFamiliares = proceso.familiares.test()

                @Test
                fun emite_lista_vacia_cuando_no_hay_familia()
                {
                    proceso.eliminarPersonaConFamiliares(darPersonaConIdSegunIndice(1))
                    testFamiliares.assertValueCount(2)
                    testFamiliares.assertValueAt(1) { !it.any() }
                }

                @Test
                fun elimina_todos_los_familiares_cuando_se_elimina_persona_existente()
                {
                    val familia = setOf(darPersonaConIdSegunIndice(4), darPersonaSinIdSegunIndice(3), darPersonaConIdSegunIndice(2))
                    proceso.agregarPersonaConFamiliares(PersonaConFamiliares(darPersonaConIdSegunIndice(1), familia))
                    proceso.eliminarPersonaConFamiliares(darPersonaConIdSegunIndice(1))
                    testFamiliares.assertValueCount(3)
                    testFamiliares.assertValueAt(2) { !it.any() }
                }

                @Test
                fun elimina_todos_los_familiares_cuando_se_elimina_persona_diferente_con_mismo_id_de_persona_existente()
                {
                    val persona = darPersonaConIdSegunIndice(1)
                    val familia = setOf(darPersonaConIdSegunIndice(4), darPersonaSinIdSegunIndice(3), darPersonaConIdSegunIndice(2))
                    proceso.agregarPersonaConFamiliares(PersonaConFamiliares(persona, familia))
                    proceso.eliminarPersonaConFamiliares(darPersonaConIdSegunIndice(10).copiar(id = persona.id))
                    testFamiliares.assertValueCount(3)
                    testFamiliares.assertValueAt(2) { !it.any() }
                }

                @Test
                fun emite_nueva_lista_con_misma_familia_cuando_se_elimina_persona_sin_familia()
                {
                    val familia = setOf(darPersonaConIdSegunIndice(4), darPersonaSinIdSegunIndice(3), darPersonaConIdSegunIndice(2))
                    proceso.agregarPersonaConFamiliares(PersonaConFamiliares(darPersonaConIdSegunIndice(1), familia))
                    proceso.agregarPersonaConFamiliares(PersonaConFamiliares(darPersonaConIdSegunIndice(6), familia))
                    proceso.eliminarPersonaConFamiliares(darPersonaConIdSegunIndice(6))
                    testFamiliares.assertValueCount(4)
                    testFamiliares.assertValueAt(3) { it.toList().size == familia.size }
                    testFamiliares.assertValueAt(3) { it.zip(familia).all { it.first == it.second } }
                }

                @Test
                fun emite_nueva_lista_con_familia_correcta_en_orden_cuando_se_elimina_persona_con_familia()
                {
                    val familia1 = setOf(darPersonaConIdSegunIndice(4), darPersonaSinIdSegunIndice(3), darPersonaConIdSegunIndice(2))
                    val familia2 = setOf(darPersonaConIdSegunIndice(9), darPersonaSinIdSegunIndice(8), darPersonaConIdSegunIndice(7))
                    proceso.agregarPersonaConFamiliares(PersonaConFamiliares(darPersonaConIdSegunIndice(1), familia1))
                    proceso.agregarPersonaConFamiliares(PersonaConFamiliares(darPersonaConIdSegunIndice(6), familia2))
                    proceso.eliminarPersonaConFamiliares(darPersonaConIdSegunIndice(1))
                    testFamiliares.assertValueCount(4)
                    testFamiliares.assertValueAt(3) { it.toList().size == familia2.size }
                    testFamiliares.assertValueAt(3) { it.zip(familia2).all { it.first == it.second } }
                }

                @Test
                fun al_eliminar_persona_que_estaba_en_lista_de_familiar_previo_vuelve_a_aparecer_en_lista_de_familiares()
                {
                    val persona2 = darPersonaConIdSegunIndice(6)
                    val familia = setOf(darPersonaConIdSegunIndice(4), persona2, darPersonaConIdSegunIndice(2))
                    proceso.agregarPersonaConFamiliares(PersonaConFamiliares(darPersonaConIdSegunIndice(1), familia))
                    proceso.agregarPersonaConFamiliares(PersonaConFamiliares(persona2, setOf()))
                    proceso.eliminarPersonaConFamiliares(persona2)
                    testFamiliares.assertValueCount(4)
                    testFamiliares.assertValueAt(3) { it.toList().size == familia.size }
                    testFamiliares.assertValueAt(3) { it.zip(familia).all { it.first == it.second } }
                }

                @Test
                fun al_eliminar_persona_con_mismo_documento_que_persona_en_lista_de_familiar_previo_vuelve_a_aparecer_en_lista_de_familiares()
                {
                    val persona2 = darPersonaConIdSegunIndice(6)
                    val personaConMismoDocumento = darPersonaSinIdSegunIndice(3).copiar(tipoDocumento = persona2.tipoDocumento, numeroDocumento = persona2.numeroDocumento)
                    val familia = setOf(darPersonaConIdSegunIndice(4), personaConMismoDocumento, darPersonaConIdSegunIndice(2))
                    proceso.agregarPersonaConFamiliares(PersonaConFamiliares(darPersonaConIdSegunIndice(1), familia))
                    proceso.agregarPersonaConFamiliares(PersonaConFamiliares(persona2, setOf()))
                    proceso.eliminarPersonaConFamiliares(persona2)
                    testFamiliares.assertValueCount(4)
                    testFamiliares.assertValueAt(3) { it.toList().size == familia.size }
                    testFamiliares.assertValueAt(3) { it.zip(familia).all { it.first == it.second } }
                }

                @Test
                fun emite_lista_correcta_al_eliminar_persona_con_algunos_familiares_duplicados_de_persona_previa()
                {
                    val familiarDuplicado1 = darPersonaConIdSegunIndice(4)
                    val familiarDuplicado2 = darPersonaConIdSegunIndice(9)
                    val familia1 = setOf(familiarDuplicado1, darPersonaSinIdSegunIndice(3), familiarDuplicado2)
                    val familia2 = setOf(familiarDuplicado2, familiarDuplicado1, darPersonaConIdSegunIndice(7))
                    proceso.agregarPersonaConFamiliares(PersonaConFamiliares(darPersonaConIdSegunIndice(1), familia1))
                    proceso.agregarPersonaConFamiliares(PersonaConFamiliares(darPersonaConIdSegunIndice(6), familia2))
                    proceso.eliminarPersonaConFamiliares(darPersonaConIdSegunIndice(6))
                    testFamiliares.assertValueCount(4)
                    testFamiliares.assertValueAt(3) { it.toList().size == familia1.size }
                    testFamiliares.assertValueAt(3) { it.zip(familia1).all { it.first == it.second } }
                }

                @Test
                fun emite_lista_correcta_al_eliminar_persona_con_algunos_familiares_con_mismo_documento_de_familaires_de_persona_previa()
                {
                    val familiar1 = darPersonaConIdSegunIndice(4)
                    val personaConMismoDocumento1 = darPersonaSinIdSegunIndice(8).copiar(tipoDocumento = familiar1.tipoDocumento, numeroDocumento = familiar1.numeroDocumento)
                    val familiar2 = darPersonaConIdSegunIndice(2)
                    val personaConMismoDocumento2 = darPersonaSinIdSegunIndice(9).copiar(tipoDocumento = familiar2.tipoDocumento, numeroDocumento = familiar2.numeroDocumento)
                    val familia1 = setOf(familiar1, darPersonaSinIdSegunIndice(3), familiar2)
                    val familia2 = setOf(personaConMismoDocumento2, personaConMismoDocumento1, darPersonaConIdSegunIndice(7))
                    proceso.agregarPersonaConFamiliares(PersonaConFamiliares(darPersonaConIdSegunIndice(1), familia1))
                    proceso.agregarPersonaConFamiliares(PersonaConFamiliares(darPersonaConIdSegunIndice(6), familia2))
                    proceso.eliminarPersonaConFamiliares(darPersonaConIdSegunIndice(6))
                    testFamiliares.assertValueCount(4)
                    testFamiliares.assertValueAt(3) { it.toList().size == familia1.size }
                    testFamiliares.assertValueAt(3) { it.zip(familia1).all { it.first == it.second } }
                }
            }
        }
    }

    @Nested
    inner class IntentarConsultarFamiliarPorDocumento
    {
        @Nested
        @Suppress("ClassName")
        inner class EnEstadoESPERANDO_DATOS
        {
            private val personaConFamiliares = PersonaConFamiliares(darPersonaConIdSegunIndice(1), setOf(darPersonaConIdSegunIndice(20), darPersonaConIdSegunIndice(10)))
            private val personaSinFamiliares = PersonaConFamiliares(darPersonaConIdSegunIndice(15), setOf())

            @Test
            fun retorna_ACCION_INICIADA_cuando_api_personas_lanza_IllegalStateException_con_familiares()
            {
                mockearRespuestaDeRedExitosaAlConsultar(personaConFamiliares)
                Assertions.assertEquals(ResultadoAccionUI.ACCION_INICIADA, proceso.intentarConsultarFamiliarPorDocumento(personaConFamiliares.persona))
            }

            @Test
            fun retorna_ACCION_INICIADA_cuando_api_personas_lanza_IllegalStateException_sin_familiares()
            {
                mockearRespuestaDeRedExitosaAlConsultar(personaSinFamiliares)
                Assertions.assertEquals(ResultadoAccionUI.ACCION_INICIADA, proceso.intentarConsultarFamiliarPorDocumento(personaSinFamiliares.persona))
            }

            @Test
            fun retorna_ACCION_INICIADA_cuando_api_personas_lanza_IllegalStateExceptionVacia()
            {
                val errorEsperado = IllegalStateException()
                erroresEsperados.add(errorEsperado)
                mockearRespuestaDeRedInvalidaAlConsultar(personaConFamiliares.persona, errorEsperado)
                Assertions.assertEquals(ResultadoAccionUI.ACCION_INICIADA, proceso.intentarConsultarFamiliarPorDocumento(personaConFamiliares.persona))
            }

            @Test
            fun retorna_ACCION_INICIADA_cuando_api_personas_retorna_respuesta_ErrorTimeout()
            {
                mockearRespuestaDeRedErrorTimeoutAlConsultar(personaConFamiliares.persona)
                Assertions.assertEquals(ResultadoAccionUI.ACCION_INICIADA, proceso.intentarConsultarFamiliarPorDocumento(personaConFamiliares.persona))
            }

            @Test
            fun retorna_ACCION_INICIADA_cuando_api_personas_retorna_respuesta_ErrorRed()
            {
                mockearRespuestaDeRedErrorRedAlConsultar(personaConFamiliares.persona)
                Assertions.assertEquals(ResultadoAccionUI.ACCION_INICIADA, proceso.intentarConsultarFamiliarPorDocumento(personaConFamiliares.persona))
            }

            @Test
            fun retorna_ACCION_INICIADA_cuando_api_personas_retorna_respuesta_ErrorBack()
            {
                mockearRespuestaDeRedErrorBackAlConsultar(personaConFamiliares.persona, "Error de pruebas")
                Assertions.assertEquals(ResultadoAccionUI.ACCION_INICIADA, proceso.intentarConsultarFamiliarPorDocumento(personaConFamiliares.persona))
            }

            @Nested
            inner class ErrorGlobal
            {
                private val testErrorGlobal = proceso.errorGlobal.test()

                @Test
                fun emite_error_vacio_cuando_api_personas_lanza_IllegalStateException_con_familiares()
                {
                    mockearRespuestaDeRedExitosaAlConsultar(personaConFamiliares)
                    testErrorGlobal.assertValue(Opcional.Vacio())
                    testErrorGlobal.assertValueCount(1)
                    proceso.intentarConsultarFamiliarPorDocumento(personaConFamiliares.persona)
                    testErrorGlobal.assertValueCount(2)
                    testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                }

                @Test
                fun emite_error_vacio_cuando_api_personas_lanza_IllegalStateException_sin_familiares()
                {
                    mockearRespuestaDeRedExitosaAlConsultar(personaSinFamiliares)
                    testErrorGlobal.assertValue(Opcional.Vacio())
                    testErrorGlobal.assertValueCount(1)
                    proceso.intentarConsultarFamiliarPorDocumento(personaSinFamiliares.persona)
                    testErrorGlobal.assertValueCount(2)
                    testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                }

                @Test
                fun emite_error_vacio_cuando_api_personas_lanza_IllegalStateExceptionVacia()
                {
                    val errorEsperado = IllegalStateException()
                    erroresEsperados.add(errorEsperado)
                    mockearRespuestaDeRedInvalidaAlConsultar(personaConFamiliares.persona, errorEsperado)
                    testErrorGlobal.assertValue(Opcional.Vacio())
                    testErrorGlobal.assertValueCount(1)
                    proceso.intentarConsultarFamiliarPorDocumento(personaConFamiliares.persona)
                    testErrorGlobal.assertValueCount(2)
                    testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                }

                @Test
                fun emite_error_vacio_y_luego_error_correcto_cuando_api_personas_retorna_respuesta_ErrorTimeout()
                {
                    mockearRespuestaDeRedErrorTimeoutAlConsultar(personaConFamiliares.persona)
                    testErrorGlobal.assertValue(Opcional.Vacio())
                    testErrorGlobal.assertValueCount(1)
                    proceso.intentarConsultarFamiliarPorDocumento(personaConFamiliares.persona)
                    testErrorGlobal.assertValueCount(3)
                    testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                    testErrorGlobal.assertValueAt(2, Opcional.De("Timeout contactando el backend"))
                }

                @Test
                fun emite_error_vacio_y_luego_error_correcto_cuando_api_personas_retorna_respuesta_ErrorRed()
                {
                    mockearRespuestaDeRedErrorRedAlConsultar(personaConFamiliares.persona)
                    testErrorGlobal.assertValue(Opcional.Vacio())
                    testErrorGlobal.assertValueCount(1)
                    proceso.intentarConsultarFamiliarPorDocumento(personaConFamiliares.persona)
                    testErrorGlobal.assertValueCount(3)
                    testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                    testErrorGlobal.assertValueAt(2, Opcional.De("Error contactando el backend"))
                }

                @Test
                fun emite_error_vacio_y_luego_error_correcto_cuando_api_personas_retorna_respuesta_ErrorBack()
                {
                    val mensajeError = "Error de pruebas"
                    mockearRespuestaDeRedErrorBackAlConsultar(personaConFamiliares.persona, mensajeError)
                    testErrorGlobal.assertValue(Opcional.Vacio())
                    testErrorGlobal.assertValueCount(1)
                    proceso.intentarConsultarFamiliarPorDocumento(personaConFamiliares.persona)
                    testErrorGlobal.assertValueCount(3)
                    testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                    testErrorGlobal.assertValueAt(2, Opcional.De("Error en petición: $mensajeError"))
                }

                @Test
                fun emite_error_vacio_y_luego_error_correcto_cuando_api_personas_retorna_respuesta_ErrorBack_con_codigo_persona_no_existe()
                {
                    mockearRespuestaDeRedErrorBackPersonaNoExisteAlConsultar(personaConFamiliares.persona)
                    testErrorGlobal.assertValue(Opcional.Vacio())
                    testErrorGlobal.assertValueCount(1)
                    proceso.intentarConsultarFamiliarPorDocumento(personaConFamiliares.persona)
                    testErrorGlobal.assertValueCount(3)
                    testErrorGlobal.assertValueAt(1, Opcional.Vacio())
                    testErrorGlobal.assertValueAt(2, Opcional.De("Error en petición: No existe"))
                }
            }

            @Nested
            inner class PersonaConsultada
            {
                private val testPersonaConsultada by lazy { proceso.personaConsultada.test() }

                @Test
                fun emite_valor_correcto_cuando_api_personas_lanza_IllegalStateException_con_familiares()
                {
                    mockearRespuestaDeRedExitosaAlConsultar(personaConFamiliares)
                    proceso.intentarConsultarFamiliarPorDocumento(personaConFamiliares.persona)
                    testPersonaConsultada.assertValueCount(1)
                    testPersonaConsultada.assertValue(personaConFamiliares)
                }

                @Test
                fun emite_valor_correcto_cuando_api_personas_lanza_IllegalStateException_sin_familiares()
                {
                    mockearRespuestaDeRedExitosaAlConsultar(personaSinFamiliares)
                    proceso.intentarConsultarFamiliarPorDocumento(personaSinFamiliares.persona)
                    testPersonaConsultada.assertValueCount(1)
                    testPersonaConsultada.assertValue(personaSinFamiliares)
                }

                @Test
                fun no_emite_valor_cuando_api_personas_retorna_respuesta_ErrorTimeout()
                {
                    mockearRespuestaDeRedErrorTimeoutAlConsultar(personaConFamiliares.persona)
                    proceso.intentarConsultarFamiliarPorDocumento(personaConFamiliares.persona)
                    testPersonaConsultada.assertValueCount(0)
                }

                @Test
                fun no_emite_valor_cuando_api_personas_retorna_respuesta_ErrorRed()
                {
                    mockearRespuestaDeRedErrorRedAlConsultar(personaConFamiliares.persona)
                    proceso.intentarConsultarFamiliarPorDocumento(personaConFamiliares.persona)
                    testPersonaConsultada.assertValueCount(0)
                }

                @Test
                fun no_emite_valor_cuando_api_personas_retorna_respuesta_ErrorBack()
                {
                    mockearRespuestaDeRedErrorBackAlConsultar(personaConFamiliares.persona, "Error de pruebas")
                    proceso.intentarConsultarFamiliarPorDocumento(personaConFamiliares.persona)
                    testPersonaConsultada.assertValueCount(0)
                }
            }

            @Nested
            inner class Estado
            {
                private val testEstado = proceso.estado.test()

                @Test
                fun cambia_a_CONSULTANDO_FAMILIAR_y_luego_a_ESPERANDO_DATOS_cuando_api_de_personas_retorna_Exitosa_con_familiares()
                {
                    mockearRespuestaDeRedExitosaAlConsultar(personaConFamiliares)
                    testEstado.assertValue(ProcesoConsultarFamiliares.Estado.ESPERANDO_DATOS)
                    testEstado.assertValueCount(1)
                    proceso.intentarConsultarFamiliarPorDocumento(personaConFamiliares.persona)
                    testEstado.assertValueCount(3)
                    testEstado.assertValueAt(1, ProcesoConsultarFamiliares.Estado.CONSULTANDO_FAMILIAR)
                    testEstado.assertValueAt(2, ProcesoConsultarFamiliares.Estado.ESPERANDO_DATOS)
                }

                @Test
                fun cambia_a_CONSULTANDO_FAMILIAR_y_luego_a_ESPERANDO_DATOS_cuando_api_de_personas_retorna_Exitosa_sin_familiares()
                {
                    mockearRespuestaDeRedExitosaAlConsultar(personaSinFamiliares)
                    testEstado.assertValue(ProcesoConsultarFamiliares.Estado.ESPERANDO_DATOS)
                    testEstado.assertValueCount(1)
                    proceso.intentarConsultarFamiliarPorDocumento(personaSinFamiliares.persona)
                    testEstado.assertValueCount(3)
                    testEstado.assertValueAt(1, ProcesoConsultarFamiliares.Estado.CONSULTANDO_FAMILIAR)
                    testEstado.assertValueAt(2, ProcesoConsultarFamiliares.Estado.ESPERANDO_DATOS)
                }

                @Test
                fun cambia_a_CONSULTANDO_FAMILIAR_cuando_api_de_personas_retorna_ExitosaVacia()
                {
                    val errorEsperado = IllegalStateException()
                    erroresEsperados.add(errorEsperado)
                    mockearRespuestaDeRedInvalidaAlConsultar(personaConFamiliares.persona, errorEsperado)
                    testEstado.assertValue(ProcesoConsultarFamiliares.Estado.ESPERANDO_DATOS)
                    testEstado.assertValueCount(1)
                    proceso.intentarConsultarFamiliarPorDocumento(personaConFamiliares.persona)
                    testEstado.assertValueCount(2)
                    testEstado.assertValueAt(1, ProcesoConsultarFamiliares.Estado.CONSULTANDO_FAMILIAR)
                }

                @Test
                fun cambia_a_CONSULTANDO_FAMILIAR_y_luego_a_ESPERANDO_DATOS_cuando_api_de_personas_retorna_ErrorTimeout()
                {
                    mockearRespuestaDeRedErrorTimeoutAlConsultar(personaConFamiliares.persona)
                    testEstado.assertValue(ProcesoConsultarFamiliares.Estado.ESPERANDO_DATOS)
                    testEstado.assertValueCount(1)
                    proceso.intentarConsultarFamiliarPorDocumento(personaConFamiliares.persona)
                    testEstado.assertValueCount(3)
                    testEstado.assertValueAt(1, ProcesoConsultarFamiliares.Estado.CONSULTANDO_FAMILIAR)
                    testEstado.assertValueAt(2, ProcesoConsultarFamiliares.Estado.ESPERANDO_DATOS)
                }

                @Test
                fun cambia_a_CONSULTANDO_FAMILIAR_y_luego_a_ESPERANDO_DATOS_cuando_api_de_personas_retorna_ErrorBack()
                {
                    mockearRespuestaDeRedErrorBackAlConsultar(personaConFamiliares.persona, "Error de pruebas")
                    testEstado.assertValue(ProcesoConsultarFamiliares.Estado.ESPERANDO_DATOS)
                    testEstado.assertValueCount(1)
                    proceso.intentarConsultarFamiliarPorDocumento(personaConFamiliares.persona)
                    testEstado.assertValueCount(3)
                    testEstado.assertValueAt(1, ProcesoConsultarFamiliares.Estado.CONSULTANDO_FAMILIAR)
                    testEstado.assertValueAt(2, ProcesoConsultarFamiliares.Estado.ESPERANDO_DATOS)
                }
            }
        }
    }
}