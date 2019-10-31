package co.smartobjects.logica.fondos.precios

import co.smartobjects.entidades.fondos.precios.GrupoClientes
import co.smartobjects.entidades.fondos.precios.SegmentoClientes
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.personas.ValorGrupoEdad
import co.smartobjects.logica.personas.CalculadorGrupoDeEdad
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.junit.jupiter.api.*
import org.threeten.bp.LocalDate

@DisplayName("CalculadorGrupoDeClientesEnMemoria")
internal class CalculadorGrupoDeClientesEnMemoriaPruebas
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

    private val segmentoCategoria = SegmentoClientes(null, SegmentoClientes.NombreCampo.CATEGORIA, "A")
    private val segmentoGrupoEdad = SegmentoClientes(null, SegmentoClientes.NombreCampo.GRUPO_DE_EDAD, "Edad")

    private val valorGrupoEdadSegmento = ValorGrupoEdad("Edad", null, null)
    private val valorGrupoEdadOtro = ValorGrupoEdad("Otro", null, null)

    private var valorGrupoEdadPruebas: ValorGrupoEdad? = null
    private val calculadorGrupoDeEdad = object : CalculadorGrupoDeEdad
    {
        override fun darGrupoEdadParaPersona(persona: Persona): ValorGrupoEdad?
        {
            return valorGrupoEdadPruebas
        }
    }

    @Nested
    inner class ConListaVacia
    {
        private val calculadorGrupoDeCliente: CalculadorGrupoCliente = CalculadorGrupoClientesEnMemoria(listOf(), calculadorGrupoDeEdad)

        @Test
        fun retorna_null()
        {
            Assertions.assertNull(calculadorGrupoDeCliente.darGrupoClienteParaPersona(personaPruebas))
        }
    }

    @Nested
    inner class ConListaConGrupoDeClienteConCategoriaYGrupoEdad
    {
        private val grupoDeClientes = GrupoClientes(null, "Prueba", listOf(segmentoCategoria, segmentoGrupoEdad))
        private val calculadorGrupoDeCliente: CalculadorGrupoCliente = CalculadorGrupoClientesEnMemoria(listOf(grupoDeClientes), calculadorGrupoDeEdad)

        @Nested
        inner class CuandoValoresCoinciden
        {
            @BeforeEach
            fun asignarGrupoEdadCorrecto()
            {
                valorGrupoEdadPruebas = valorGrupoEdadSegmento
            }

            @Test
            fun retorna_valor_correcto()
            {
                Assertions.assertEquals(grupoDeClientes, calculadorGrupoDeCliente.darGrupoClienteParaPersona(personaPruebas))
            }
        }

        @Nested
        inner class CuandoCategoriaCoincidePeroGrupoEdadNo
        {
            private val calculadorGrupoDeCliente: CalculadorGrupoCliente = CalculadorGrupoClientesEnMemoria(listOf(grupoDeClientes), calculadorGrupoDeEdad)

            @BeforeEach
            fun asignarGrupoEdadIncorrecto()
            {
                valorGrupoEdadPruebas = valorGrupoEdadOtro
            }

            @Test
            fun retorna_null()
            {
                Assertions.assertNull(calculadorGrupoDeCliente.darGrupoClienteParaPersona(personaPruebas))
            }
        }

        @Nested
        inner class CuandoCategoriaCoincidePeroGrupoEdadEsNulo
        {
            private val calculadorGrupoDeCliente: CalculadorGrupoCliente = CalculadorGrupoClientesEnMemoria(listOf(grupoDeClientes), calculadorGrupoDeEdad)

            @BeforeEach
            fun asignarGrupoEdadNulo()
            {
                valorGrupoEdadPruebas = null
            }

            @Test
            fun retorna_null()
            {
                Assertions.assertNull(calculadorGrupoDeCliente.darGrupoClienteParaPersona(personaPruebas))
            }
        }

        @Nested
        inner class CuandoGrupoDeEdadCoincidePeroCategoriaNo
        {
            private val calculadorGrupoDeCliente: CalculadorGrupoCliente = CalculadorGrupoClientesEnMemoria(listOf(grupoDeClientes), calculadorGrupoDeEdad)

            @BeforeEach
            fun asignarGrupoEdadCorrecto()
            {
                valorGrupoEdadPruebas = valorGrupoEdadSegmento
            }

            @Test
            fun retorna_null()
            {
                Assertions.assertNull(calculadorGrupoDeCliente.darGrupoClienteParaPersona(personaPruebas.copiar(categoria = Persona.Categoria.C)))
            }
        }

        @Nested
        inner class CuandoGrupoDeEdadCoincidePeroCategoriaEsNINGUNA
        {
            private val calculadorGrupoDeCliente: CalculadorGrupoCliente = CalculadorGrupoClientesEnMemoria(listOf(grupoDeClientes), calculadorGrupoDeEdad)

            @BeforeEach
            fun asignarGrupoEdadCorrecto()
            {
                valorGrupoEdadPruebas = valorGrupoEdadSegmento
            }

            @Test
            fun retorna_null()
            {
                Assertions.assertNull(calculadorGrupoDeCliente.darGrupoClienteParaPersona(personaPruebas.copiar(categoria = Persona.Categoria.D)))
            }
        }
    }

    @Nested
    inner class ConListaConGrupoDeClienteSoloConCategoria
    {
        private val grupoDeClientes = GrupoClientes(null, "Prueba", listOf(segmentoCategoria))
        private val calculadorGrupoDeCliente: CalculadorGrupoCliente = CalculadorGrupoClientesEnMemoria(listOf(grupoDeClientes), calculadorGrupoDeEdad)

        @Nested
        inner class CuandoCategoriaCoincideConCualquierValorDeGrupoDeEdad
        {
            @BeforeEach
            fun asignarGrupoEdadCorrecto()
            {
                valorGrupoEdadPruebas = valorGrupoEdadSegmento
            }

            @Test
            fun retorna_valor_correcto()
            {
                Assertions.assertEquals(grupoDeClientes, calculadorGrupoDeCliente.darGrupoClienteParaPersona(personaPruebas))
            }
        }

        @Nested
        inner class CuandoCategoriaCoincidePeroGrupoEdadEsNulo
        {
            private val calculadorGrupoDeCliente: CalculadorGrupoCliente = CalculadorGrupoClientesEnMemoria(listOf(grupoDeClientes), calculadorGrupoDeEdad)

            @BeforeEach
            fun asignarGrupoEdadNulo()
            {
                valorGrupoEdadPruebas = null
            }

            @Test
            fun retorna_valor_correcto()
            {
                Assertions.assertEquals(grupoDeClientes, calculadorGrupoDeCliente.darGrupoClienteParaPersona(personaPruebas))
            }
        }

        @Nested
        inner class CuandoCategoriaNoCoincide
        {
            private val calculadorGrupoDeCliente: CalculadorGrupoCliente = CalculadorGrupoClientesEnMemoria(listOf(grupoDeClientes), calculadorGrupoDeEdad)

            @BeforeEach
            fun asignarGrupoEdadCorrecto()
            {
                valorGrupoEdadPruebas = valorGrupoEdadSegmento
            }

            @Test
            fun retorna_null()
            {
                Assertions.assertNull(calculadorGrupoDeCliente.darGrupoClienteParaPersona(personaPruebas.copiar(categoria = Persona.Categoria.C)))
            }
        }

        @Nested
        inner class CuandoCategoriaEsNINGUNA
        {
            private val calculadorGrupoDeCliente: CalculadorGrupoCliente = CalculadorGrupoClientesEnMemoria(listOf(grupoDeClientes), calculadorGrupoDeEdad)

            @BeforeEach
            fun asignarGrupoEdadCorrecto()
            {
                valorGrupoEdadPruebas = valorGrupoEdadSegmento
            }

            @Test
            fun retorna_null()
            {
                Assertions.assertNull(calculadorGrupoDeCliente.darGrupoClienteParaPersona(personaPruebas.copiar(categoria = Persona.Categoria.D)))
            }
        }
    }

    @Nested
    inner class ConListaConGrupoDeClienteSoloConGrupoEdad
    {
        private val grupoDeClientes = GrupoClientes(null, "Prueba", listOf(segmentoGrupoEdad))
        private val calculadorGrupoDeCliente: CalculadorGrupoCliente = CalculadorGrupoClientesEnMemoria(listOf(grupoDeClientes), calculadorGrupoDeEdad)

        @Nested
        inner class CuandoValorDeGrupoDeEdadCategoriaCoincideConCualquierValorDeCategoria
        {
            @BeforeEach
            fun asignarGrupoEdadCorrecto()
            {
                valorGrupoEdadPruebas = valorGrupoEdadSegmento
            }

            @Test
            fun retorna_valor_correcto()
            {
                Assertions.assertEquals(grupoDeClientes, calculadorGrupoDeCliente.darGrupoClienteParaPersona(personaPruebas))
            }
        }

        @Nested
        inner class CuandoGrupoDeEdadCoincidePeroCategoriaEsNINGUNA
        {
            private val calculadorGrupoDeCliente: CalculadorGrupoCliente = CalculadorGrupoClientesEnMemoria(listOf(grupoDeClientes), calculadorGrupoDeEdad)

            @BeforeEach
            fun asignarGrupoEdadCorrecto()
            {
                valorGrupoEdadPruebas = valorGrupoEdadSegmento
            }

            @Test
            fun retorna_valor_correcto()
            {
                Assertions.assertEquals(grupoDeClientes, calculadorGrupoDeCliente.darGrupoClienteParaPersona(personaPruebas.copiar(categoria = Persona.Categoria.D)))
            }
        }

        @Nested
        inner class CuandoGrupoEdadNoCoincide
        {
            private val calculadorGrupoDeCliente: CalculadorGrupoCliente = CalculadorGrupoClientesEnMemoria(listOf(grupoDeClientes), calculadorGrupoDeEdad)

            @BeforeEach
            fun asignarGrupoEdadIncorrecto()
            {
                valorGrupoEdadPruebas = valorGrupoEdadOtro
            }

            @Test
            fun retorna_null()
            {
                Assertions.assertNull(calculadorGrupoDeCliente.darGrupoClienteParaPersona(personaPruebas))
            }
        }

        @Nested
        inner class CuandoGrupoEdadEsNulo
        {
            private val calculadorGrupoDeCliente: CalculadorGrupoCliente = CalculadorGrupoClientesEnMemoria(listOf(grupoDeClientes), calculadorGrupoDeEdad)

            @BeforeEach
            fun asignarGrupoEdadNulo()
            {
                valorGrupoEdadPruebas = null
            }

            @Test
            fun retorna_null()
            {
                Assertions.assertNull(calculadorGrupoDeCliente.darGrupoClienteParaPersona(personaPruebas))
            }
        }
    }

    @Nested
    inner class ConListaConMultiplosGruposDeClienteAplicables
    {
        private val grupoDeClientesCompleto = GrupoClientes(null, "Prueba", listOf(segmentoCategoria, segmentoGrupoEdad))
        private val grupoDeClientesEdad = GrupoClientes(null, "Prueba", listOf(segmentoGrupoEdad))
        private val grupoDeClientesCategoria = GrupoClientes(null, "Prueba", listOf(segmentoCategoria))

        @Nested
        inner class CuandoAplicanAmbosGrupos
        {
            @BeforeEach
            fun asignarGrupoEdadCorrecto()
            {
                valorGrupoEdadPruebas = valorGrupoEdadSegmento
            }

            @Test
            fun da_valor_correcto_cuando_grupo_mas_especifico_aparece_de_primero()
            {
                val calculadorGrupoDeCliente = CalculadorGrupoClientesEnMemoria(listOf(grupoDeClientesCompleto, grupoDeClientesCategoria, grupoDeClientesEdad), calculadorGrupoDeEdad)
                Assertions.assertEquals(grupoDeClientesCompleto, calculadorGrupoDeCliente.darGrupoClienteParaPersona(personaPruebas))
            }

            @Test
            fun da_valor_correcto_cuando_grupo_mas_especifico_aparece_en_la_mitad()
            {
                val calculadorGrupoDeCliente = CalculadorGrupoClientesEnMemoria(listOf(grupoDeClientesCategoria, grupoDeClientesCompleto, grupoDeClientesEdad), calculadorGrupoDeEdad)
                Assertions.assertEquals(grupoDeClientesCompleto, calculadorGrupoDeCliente.darGrupoClienteParaPersona(personaPruebas))
            }

            @Test
            fun da_valor_correcto_cuando_grupo_mas_especifico_aparece_al_final()
            {
                val calculadorGrupoDeCliente = CalculadorGrupoClientesEnMemoria(listOf(grupoDeClientesCategoria, grupoDeClientesEdad, grupoDeClientesCompleto), calculadorGrupoDeEdad)
                Assertions.assertEquals(grupoDeClientesCompleto, calculadorGrupoDeCliente.darGrupoClienteParaPersona(personaPruebas))
            }
        }

        @Nested
        inner class CuandoSoloAplicaLaCategoria
        {
            @BeforeEach
            fun asignarGrupoEdadIncorrecto()
            {
                valorGrupoEdadPruebas = valorGrupoEdadOtro
            }

            @Test
            fun da_valor_correcto_cuando_grupo_correcto_aparece_de_primero()
            {
                val calculadorGrupoDeCliente = CalculadorGrupoClientesEnMemoria(listOf(grupoDeClientesCategoria, grupoDeClientesCompleto, grupoDeClientesEdad), calculadorGrupoDeEdad)
                Assertions.assertEquals(grupoDeClientesCategoria, calculadorGrupoDeCliente.darGrupoClienteParaPersona(personaPruebas))
            }

            @Test
            fun da_valor_correcto_cuando_grupo_correcto_aparece_en_la_mitad()
            {
                val calculadorGrupoDeCliente = CalculadorGrupoClientesEnMemoria(listOf(grupoDeClientesEdad, grupoDeClientesCategoria, grupoDeClientesCompleto), calculadorGrupoDeEdad)
                Assertions.assertEquals(grupoDeClientesCategoria, calculadorGrupoDeCliente.darGrupoClienteParaPersona(personaPruebas))
            }

            @Test
            fun da_valor_correcto_cuando_grupo_correcto_aparece_al_final()
            {
                val calculadorGrupoDeCliente = CalculadorGrupoClientesEnMemoria(listOf(grupoDeClientesEdad, grupoDeClientesCompleto, grupoDeClientesCategoria), calculadorGrupoDeEdad)
                Assertions.assertEquals(grupoDeClientesCategoria, calculadorGrupoDeCliente.darGrupoClienteParaPersona(personaPruebas))
            }
        }

        @Nested
        inner class CuandoSoloAplicaElGrupoEdad
        {
            private val personaSinCategoria = personaPruebas.copiar(categoria = Persona.Categoria.D)

            @BeforeEach
            fun asignarGrupoEdadCorrecto()
            {
                valorGrupoEdadPruebas = valorGrupoEdadSegmento
            }

            @Test
            fun da_valor_correcto_cuando_grupo_correcto_aparece_de_primero()
            {
                val calculadorGrupoDeCliente = CalculadorGrupoClientesEnMemoria(listOf(grupoDeClientesEdad, grupoDeClientesCompleto, grupoDeClientesCategoria), calculadorGrupoDeEdad)
                Assertions.assertEquals(grupoDeClientesEdad, calculadorGrupoDeCliente.darGrupoClienteParaPersona(personaSinCategoria))
            }

            @Test
            fun da_valor_correcto_cuando_grupo_correcto_aparece_en_la_mitad()
            {
                val calculadorGrupoDeCliente = CalculadorGrupoClientesEnMemoria(listOf(grupoDeClientesCategoria, grupoDeClientesEdad, grupoDeClientesCompleto), calculadorGrupoDeEdad)
                Assertions.assertEquals(grupoDeClientesEdad, calculadorGrupoDeCliente.darGrupoClienteParaPersona(personaSinCategoria))
            }

            @Test
            fun da_valor_correcto_cuando_grupo_correcto_aparece_al_final()
            {
                val calculadorGrupoDeCliente = CalculadorGrupoClientesEnMemoria(listOf(grupoDeClientesCategoria, grupoDeClientesCompleto, grupoDeClientesEdad), calculadorGrupoDeEdad)
                Assertions.assertEquals(grupoDeClientesEdad, calculadorGrupoDeCliente.darGrupoClienteParaPersona(personaSinCategoria))
            }
        }

        @Nested
        inner class CuandoNoAplicaNinguno
        {
            private val personaSinCategoria = personaPruebas.copiar(categoria = Persona.Categoria.D)

            @BeforeEach
            fun asignarGrupoEdadIncorrecto()
            {
                valorGrupoEdadPruebas = valorGrupoEdadOtro
            }

            @Test
            fun retorna_null()
            {
                val calculadorGrupoDeCliente = CalculadorGrupoClientesEnMemoria(listOf(grupoDeClientesCategoria, grupoDeClientesCompleto, grupoDeClientesEdad), calculadorGrupoDeEdad)
                Assertions.assertNull(calculadorGrupoDeCliente.darGrupoClienteParaPersona(personaSinCategoria))
            }
        }
    }
}