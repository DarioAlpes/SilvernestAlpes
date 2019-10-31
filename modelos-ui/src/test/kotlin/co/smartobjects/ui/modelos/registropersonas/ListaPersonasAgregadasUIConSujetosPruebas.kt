package co.smartobjects.ui.modelos.registropersonas

import co.smartobjects.entidades.fondos.precios.GrupoClientes
import co.smartobjects.entidades.fondos.precios.SegmentoClientes
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.personas.PersonaConGrupoCliente
import co.smartobjects.logica.fondos.precios.CalculadorGrupoCliente
import co.smartobjects.ui.modelos.PruebasModelosRxBase
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.threeten.bp.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DisplayName("ListaPersonasAgregadasUIConSujetos")
internal class ListaPersonasAgregadasUIConSujetosPruebas : PruebasModelosRxBase()
{
    private val segmentoCategoria = SegmentoClientes(null, SegmentoClientes.NombreCampo.CATEGORIA, "A")
    private val segmentoGrupoEdad = SegmentoClientes(null, SegmentoClientes.NombreCampo.GRUPO_DE_EDAD, "Edad")
    private val grupoClientesPruebas1 = GrupoClientes(1, "Grupo 1", listOf(segmentoCategoria, segmentoGrupoEdad))
    private val grupoClientesPruebas2 = GrupoClientes(2, "Grupo 2", listOf(segmentoCategoria))
    private val grupoClientesPruebas3 = GrupoClientes(3, "Grupo 3", listOf(segmentoGrupoEdad))

    private val personaPruebas1 = Persona(
            1,
            1,
            "Prueba 1",
            Persona.TipoDocumento.TI,
            "123",
            Persona.Genero.FEMENINO,
            LocalDate.now(ZONA_HORARIA_POR_DEFECTO),
            Persona.Categoria.D,
            Persona.Afiliacion.BENEFICIARIO,
            null,
            "empresa",
            "0",
            Persona.Tipo.NO_AFILIADO
                                         )

    private val personaPruebas2 = Persona(
            1,
            2,
            "Prueba 2",
            Persona.TipoDocumento.CC,
            "456",
            Persona.Genero.MASCULINO,
            LocalDate.now(ZONA_HORARIA_POR_DEFECTO),
            Persona.Categoria.C,
            Persona.Afiliacion.COTIZANTE,
            null,
            "empresa",
            "0",
            Persona.Tipo.NO_AFILIADO
                                         )

    private val personaPruebas3 = Persona(
            1,
            3,
            "Prueba 3",
            Persona.TipoDocumento.CE,
            "789",
            Persona.Genero.MASCULINO,
            LocalDate.now(ZONA_HORARIA_POR_DEFECTO),
            Persona.Categoria.B,
            Persona.Afiliacion.COTIZANTE,
            null,
            "empresa",
            "0",
            Persona.Tipo.NO_AFILIADO
                                         )

    private val mockCalculadorGrupoCliente = mock(CalculadorGrupoCliente::class.java) {
        throw RuntimeException("Llamado a\n\t${it.method.declaringClass.simpleName}.${it.method.name}(\n\t\t${it.arguments.joinToString(",\n\t\t")}\n\t)\n\tno esta mockeado")
    }

    private val modelo: ListaPersonasAgregadasUI = ListaPersonasAgregadasUIConSujetos(mockCalculadorGrupoCliente)

    @Nested
    inner class CuandoCalculadorGrupoClienteRetornaGrupoValido
    {

        @BeforeEach
        fun mockearDarGrupoCliente()
        {
            doReturn(grupoClientesPruebas1)
                .`when`(mockCalculadorGrupoCliente)
                .darGrupoClienteParaPersona(personaPruebas1)
            doReturn(grupoClientesPruebas2)
                .`when`(mockCalculadorGrupoCliente)
                .darGrupoClienteParaPersona(personaPruebas2)
            doReturn(grupoClientesPruebas3)
                .`when`(mockCalculadorGrupoCliente)
                .darGrupoClienteParaPersona(personaPruebas3)
        }

        @Nested
        inner class PersonasRegistradas
        {
            private val testPersonasRegistradas = modelo.personasRegistradas.test()

            @Test
            fun emite_lista_vacia_al_inicializar()
            {
                testPersonasRegistradas.assertValue(listOf())
                testPersonasRegistradas.assertValueCount(1)
            }

            @Nested
            inner class AgregarPersona
            {
                @Test
                fun inexistente_emite_lista_con_persona_agregada_en_ultima_posicion_con_grupo_correcto_y_retorna_empty()
                {
                    modelo.agregarPersona(personaPruebas1)
                    modelo.agregarPersona(personaPruebas2)
                    modelo.agregarPersona(personaPruebas3)

                    testPersonasRegistradas.assertValueCount(4)
                    val listaEsperada = mutableListOf(PersonaConGrupoCliente(personaPruebas1, grupoClientesPruebas1))
                    testPersonasRegistradas.assertValueAt(1) {
                        it.zip(listaEsperada).all { it.first == it.second }
                    }
                    listaEsperada.add(PersonaConGrupoCliente(personaPruebas2, grupoClientesPruebas2))
                    testPersonasRegistradas.assertValueAt(2) {
                        it.zip(listaEsperada).all { it.first == it.second }
                    }
                    listaEsperada.add(PersonaConGrupoCliente(personaPruebas3, grupoClientesPruebas3))
                    testPersonasRegistradas.assertValueAt(3) {
                        it.zip(listaEsperada).all { it.first == it.second }
                    }
                }

                @Test
                fun que_ya_existe_no_emite_nuevo_valor_y_retorna_error_correcto()
                {
                    modelo.agregarPersona(personaPruebas1)
                    modelo.agregarPersona(personaPruebas1)
                    testPersonasRegistradas.assertValueCount(2)
                }

                @Test
                fun diferente_pero_con_mismo_id_no_emite_nuevo_valor()
                {
                    modelo.agregarPersona(personaPruebas1)
                    modelo.agregarPersona(personaPruebas2.copiar(id = personaPruebas1.id))
                    testPersonasRegistradas.assertValueCount(2)
                }
            }

            @Nested
            inner class AgregarPersonas
            {
                @Test
                fun inexistentes_emite_una_sola_lista_con_personas_agregadas()
                {
                    modelo.agregarPersonas(listOf(personaPruebas1, personaPruebas2, personaPruebas3))

                    testPersonasRegistradas.assertValueCount(2)

                    val listaEsperada = listOf(
                            PersonaConGrupoCliente(personaPruebas1, grupoClientesPruebas1),
                            PersonaConGrupoCliente(personaPruebas2, grupoClientesPruebas2),
                            PersonaConGrupoCliente(personaPruebas3, grupoClientesPruebas3)
                                              )

                    testPersonasRegistradas.assertValueAt(1) {
                        it.zip(listaEsperada).all { it.first == it.second }
                    }
                }

                @Test
                fun que_ya_existen_no_emite_nuevo_valor()
                {
                    modelo.agregarPersonas(listOf(personaPruebas1, personaPruebas2, personaPruebas3))
                    modelo.agregarPersonas(listOf(personaPruebas1, personaPruebas2, personaPruebas3))

                    testPersonasRegistradas.assertValueCount(2)
                }

                @Test
                fun cuando_algunas_ya_existen_agrega_solo_las_que_no_existen_emite_nuevo()
                {
                    modelo.agregarPersonas(listOf(personaPruebas2))
                    modelo.agregarPersonas(listOf(personaPruebas1, personaPruebas2, personaPruebas3))

                    val listaEsperada = listOf(
                            PersonaConGrupoCliente(personaPruebas2, grupoClientesPruebas2),
                            PersonaConGrupoCliente(personaPruebas1, grupoClientesPruebas1),
                            PersonaConGrupoCliente(personaPruebas3, grupoClientesPruebas3)
                                              )
                    testPersonasRegistradas.assertValueAt(2) {
                        it.zip(listaEsperada).all { it.first == it.second }
                    }
                    testPersonasRegistradas.assertValueCount(3)
                }

                @Test
                fun diferentes_pero_con_mismo_id_no_emite_nuevo_valor()
                {
                    modelo.agregarPersonas(listOf(personaPruebas1))
                    modelo.agregarPersonas(listOf(personaPruebas2.copiar(id = personaPruebas1.id)))

                    testPersonasRegistradas.assertValueCount(2)
                }
            }

            @Nested
            inner class EliminarPersona
            {
                @Test
                fun inexistente_no_emite_nuevo_valor_y_retorna_error_correcto()
                {
                    modelo.agregarPersona(personaPruebas1)
                    modelo.eliminarPersona(personaPruebas2)
                    testPersonasRegistradas.assertValueCount(2)
                }

                @Test
                fun inexistente_con_mismo_id_no_emite_nuevo_valor_y_retorna_error_correcto()
                {
                    modelo.agregarPersona(personaPruebas1)
                    modelo.eliminarPersona(personaPruebas2.copiar(id = personaPruebas1.id))
                    testPersonasRegistradas.assertValueCount(2)
                }

                @Test
                fun existente_emite_nuevo_valor_con_persona_eliminada_y_retorna_empty()
                {
                    modelo.agregarPersona(personaPruebas1)
                    modelo.eliminarPersona(personaPruebas1)
                    testPersonasRegistradas.assertValueAt(2, listOf())
                    testPersonasRegistradas.assertValueCount(3)
                }
            }
        }

        @Nested
        inner class PersonasActuales
        {
            @Test
            fun inicia_con_lista_vacia_al_inicializar()
            {
                assertEquals(listOf<PersonaConGrupoCliente>(), modelo.personasActuales)
            }

            @Nested
            inner class AgregarPersona
            {
                @Test
                fun inexistente_emite_lista_con_persona_en_ultima_posicion()
                {
                    modelo.agregarPersona(personaPruebas1)
                    val listaEsperada = mutableListOf(PersonaConGrupoCliente(personaPruebas1, grupoClientesPruebas1))
                    assertTrue(modelo.personasActuales.zip(listaEsperada).all { it.first == it.second })

                    modelo.agregarPersona(personaPruebas2)
                    listaEsperada.add(PersonaConGrupoCliente(personaPruebas2, grupoClientesPruebas2))
                    assertTrue(modelo.personasActuales.zip(listaEsperada).all { it.first == it.second })

                    modelo.agregarPersona(personaPruebas3)
                    listaEsperada.add(PersonaConGrupoCliente(personaPruebas3, grupoClientesPruebas3))
                    assertTrue(modelo.personasActuales.zip(listaEsperada).all { it.first == it.second })

                    assertEquals(3, modelo.personasActuales.size)
                }

                @Test
                fun que_ya_existe_no_agrega_nuevo_valor()
                {
                    modelo.agregarPersona(personaPruebas1)
                    modelo.agregarPersona(personaPruebas1)
                    assertTrue(modelo.personasActuales.zip(listOf(PersonaConGrupoCliente(personaPruebas1, grupoClientesPruebas1))).all { it.first == it.second })
                    assertEquals(1, modelo.personasActuales.size)
                }

                @Test
                fun diferente_pero_con_mismo_id_no_agrega_nuevo_valor()
                {
                    modelo.agregarPersona(personaPruebas1)
                    modelo.agregarPersona(personaPruebas2.copiar(id = personaPruebas1.id))
                    assertTrue(modelo.personasActuales.zip(listOf(PersonaConGrupoCliente(personaPruebas1, grupoClientesPruebas1))).all { it.first == it.second })
                    assertEquals(1, modelo.personasActuales.size)
                }
            }

            @Nested
            inner class AgregarPersonas
            {
                @Test
                fun inexistentes_emite_lista_con_personas_en_ultima_posicion()
                {
                    val listaEsperada = listOf(
                            PersonaConGrupoCliente(personaPruebas1, grupoClientesPruebas1),
                            PersonaConGrupoCliente(personaPruebas2, grupoClientesPruebas2),
                            PersonaConGrupoCliente(personaPruebas3, grupoClientesPruebas3)
                                              )

                    modelo.agregarPersonas(listOf(personaPruebas1, personaPruebas2, personaPruebas3))

                    assertTrue(modelo.personasActuales.zip(listaEsperada).all { it.first == it.second })
                    assertEquals(3, modelo.personasActuales.size)
                }

                @Test
                fun que_ya_existen_no_agrega_nuevos_valores()
                {
                    val listaEsperada = listOf(
                            PersonaConGrupoCliente(personaPruebas1, grupoClientesPruebas1),
                            PersonaConGrupoCliente(personaPruebas2, grupoClientesPruebas2),
                            PersonaConGrupoCliente(personaPruebas3, grupoClientesPruebas3)
                                              )

                    modelo.agregarPersonas(listOf(personaPruebas1, personaPruebas2, personaPruebas3))
                    modelo.agregarPersonas(listOf(personaPruebas1, personaPruebas2, personaPruebas3))

                    assertTrue(modelo.personasActuales.zip(listaEsperada).all { it.first == it.second })
                    assertEquals(3, modelo.personasActuales.size)
                }

                @Test
                fun cuando_algunas_existen_y_otras_no_agrega_solo_las_que_no_existen()
                {
                    val listaEsperada = listOf(
                            PersonaConGrupoCliente(personaPruebas2, grupoClientesPruebas2),
                            PersonaConGrupoCliente(personaPruebas1, grupoClientesPruebas1),
                            PersonaConGrupoCliente(personaPruebas3, grupoClientesPruebas3)
                                              )

                    modelo.agregarPersonas(listOf(personaPruebas2))
                    modelo.agregarPersonas(listOf(personaPruebas1, personaPruebas2, personaPruebas3))

                    assertTrue(modelo.personasActuales.zip(listaEsperada).all { it.first == it.second })
                    assertEquals(3, modelo.personasActuales.size)
                }

                @Test
                fun diferentes_pero_con_mismo_id_no_agrega_nuevo_valor()
                {
                    modelo.agregarPersonas(listOf(personaPruebas1))
                    modelo.agregarPersonas(listOf(personaPruebas2.copiar(id = personaPruebas1.id)))
                    assertTrue(modelo.personasActuales.zip(listOf(PersonaConGrupoCliente(personaPruebas1, grupoClientesPruebas1))).all { it.first == it.second })
                    assertEquals(1, modelo.personasActuales.size)
                }
            }

            @Nested
            inner class EliminarPersona
            {
                @Test
                fun inexistente_no_cambia_el_valor()
                {
                    modelo.agregarPersona(personaPruebas1)
                    modelo.eliminarPersona(personaPruebas2)
                    assertTrue(modelo.personasActuales.zip(listOf(PersonaConGrupoCliente(personaPruebas1, grupoClientesPruebas1))).all { it.first == it.second })
                    assertEquals(1, modelo.personasActuales.size)
                }

                @Test
                fun inexistente_con_mismo_id_no_cambia_valor()
                {
                    modelo.agregarPersona(personaPruebas1)
                    modelo.eliminarPersona(personaPruebas2.copiar(id = personaPruebas1.id))
                    assertTrue(modelo.personasActuales.zip(listOf(PersonaConGrupoCliente(personaPruebas1, grupoClientesPruebas1))).all { it.first == it.second })
                    assertEquals(1, modelo.personasActuales.size)
                }

                @Test
                fun existente_elimina_valor()
                {
                    modelo.agregarPersona(personaPruebas1)
                    modelo.eliminarPersona(personaPruebas1)
                    assertEquals(listOf<PersonaConGrupoCliente>(), modelo.personasActuales)
                }
            }
        }

        @Nested
        inner class PuedeRegistrarPersonas
        {
            private val testPuedeRegistrarPersonas = modelo.puedeRegistrarPersonas.test()

            @Test
            fun emite_false_al_inicializar()
            {
                testPuedeRegistrarPersonas.assertValue(false)
                testPuedeRegistrarPersonas.assertValueCount(1)
            }

            @Test
            fun emite_true_despues_de_agregar_una_persona_al_inicializar()
            {
                modelo.agregarPersona(personaPruebas1)
                testPuedeRegistrarPersonas.assertValueAt(1, true)
                testPuedeRegistrarPersonas.assertValueCount(2)
            }

            @Test
            fun solo_emite_true_al_agregar_la_primera_persona()
            {
                modelo.agregarPersona(personaPruebas1)
                modelo.agregarPersona(personaPruebas2)
                modelo.agregarPersona(personaPruebas3)
                testPuedeRegistrarPersonas.assertValueCount(2)
            }

            @Test
            fun no_emite_false_mientras_quede_al_menos_una_persona()
            {
                modelo.agregarPersona(personaPruebas1)
                modelo.agregarPersona(personaPruebas2)
                modelo.agregarPersona(personaPruebas3)
                modelo.eliminarPersona(personaPruebas2)
                modelo.eliminarPersona(personaPruebas3)
                testPuedeRegistrarPersonas.assertValueCount(2)
            }

            @Test
            fun emite_false_al_eliminar_todas_las_personas()
            {
                modelo.agregarPersona(personaPruebas1)
                modelo.agregarPersona(personaPruebas2)
                modelo.agregarPersona(personaPruebas3)
                modelo.eliminarPersona(personaPruebas1)
                modelo.eliminarPersona(personaPruebas2)
                modelo.eliminarPersona(personaPruebas3)
                testPuedeRegistrarPersonas.assertValueAt(2, false)
                testPuedeRegistrarPersonas.assertValueCount(3)
            }
        }
    }
}