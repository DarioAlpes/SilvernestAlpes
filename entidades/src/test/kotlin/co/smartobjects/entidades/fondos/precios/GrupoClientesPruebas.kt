package co.smartobjects.entidades.fondos.precios

import co.smartobjects.entidades.excepciones.EntidadConCampoDuplicado
import co.smartobjects.entidades.excepciones.EntidadConCampoVacio
import co.smartobjects.entidades.excepciones.EntidadConValorNoPermitido
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.personas.ValorGrupoEdad
import org.junit.jupiter.api.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DisplayName("GrupoClientes")
internal class GrupoClientesPruebas
{
    private val segmentoCategoria = SegmentoClientes(null, SegmentoClientes.NombreCampo.CATEGORIA, "A")
    private val segmentoGrupoEdad = SegmentoClientes(null, SegmentoClientes.NombreCampo.GRUPO_DE_EDAD, "Edad")

    @Test
    fun hace_trim_a_nombre_correctamente()
    {
        val entidadSinTrim = GrupoClientes(null, "    Prueba    ", listOf(segmentoCategoria, segmentoGrupoEdad))
        val entidadConTrim = GrupoClientes(null, "Prueba", listOf(segmentoCategoria, segmentoGrupoEdad))
        assertEquals(entidadConTrim, entidadSinTrim)
    }

    @Test
    fun campos_quedan_con_valores_correctos()
    {
        val entidad = GrupoClientes(null, "Prueba", listOf(segmentoCategoria, segmentoGrupoEdad))
        assertEquals("Prueba", entidad.campoNombre.valor)
        assertEquals(listOf(segmentoCategoria, segmentoGrupoEdad), entidad.campoSegmentoClientes.valor)
    }

    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val entidadInicial = GrupoClientes(null, "Prueba", listOf(segmentoCategoria, segmentoGrupoEdad))
        val entidadCopiada = entidadInicial.copiar(nombre = "Prueba editada")
        val entidadEsperada = GrupoClientes(null, "Prueba editada", listOf(segmentoCategoria, segmentoGrupoEdad))
        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun copiar_con_id_funciona_correctamente()
    {
        val entidadInicial = GrupoClientes(null, "Prueba", listOf(segmentoCategoria, segmentoGrupoEdad))
        val entidadEsperada = GrupoClientes(34636, "Prueba", listOf(segmentoCategoria, segmentoGrupoEdad))
        val entidadCopiada = entidadInicial.copiarConId(34636)
        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun no_se_permite_instanciar_con_nombre_vacio()
    {
        val excepcion = assertThrows<EntidadConCampoVacio> {
            GrupoClientes(null, "", listOf(segmentoCategoria, segmentoGrupoEdad))
        }

        assertEquals(GrupoClientes.Campos.NOMBRE, excepcion.nombreDelCampo)
        assertEquals(GrupoClientes.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun no_se_permite_instanciar_con_nombre_con_espacio_o_tabs()
    {
        val excepcion = assertThrows<EntidadConCampoVacio> {
            GrupoClientes(null, "                 ", listOf(segmentoCategoria, segmentoGrupoEdad))
        }

        assertEquals(GrupoClientes.Campos.NOMBRE, excepcion.nombreDelCampo)
        assertEquals(GrupoClientes.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun no_se_permite_instanciar_sin_segmentos()
    {
        val excepcion = assertThrows<EntidadConCampoVacio> {
            GrupoClientes(null, "nombre", listOf())
        }

        assertEquals(GrupoClientes.Campos.SEGMENTOS, excepcion.nombreDelCampo)
        assertEquals(GrupoClientes.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun no_se_permite_instanciar_con_dos_segmentos_sobre_categoria()
    {
        val excepcion = assertThrows<EntidadConCampoDuplicado> {
            GrupoClientes(null, "nombre", listOf(segmentoCategoria, segmentoCategoria.copiar(valor = "B")))
        }

        assertEquals(GrupoClientes.Campos.SEGMENTOS, excepcion.nombreDelCampo)
        assertEquals(GrupoClientes.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        assertTrue(excepcion.valorUsado.contains(segmentoCategoria.toString()))
        assertTrue(excepcion.valorUsado.contains(segmentoCategoria.copiar(valor = "B").toString()))
        assertTrue(excepcion.valorUsado.endsWith(SegmentoClientes.NombreCampo.CATEGORIA.toString()))
    }

    @Test
    fun no_se_permite_instanciar_con_dos_segmentos_sobre_grupo_edad()
    {
        val excepcion = assertThrows<EntidadConCampoDuplicado> {
            GrupoClientes(null, "nombre", listOf(segmentoGrupoEdad, segmentoGrupoEdad.copiar(valor = "B")))
        }

        assertEquals(GrupoClientes.Campos.SEGMENTOS, excepcion.nombreDelCampo)
        assertEquals(GrupoClientes.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        assertTrue(excepcion.valorUsado.contains(segmentoGrupoEdad.toString()))
        assertTrue(excepcion.valorUsado.contains(segmentoGrupoEdad.copiar(valor = "B").toString()))
        assertTrue(excepcion.valorUsado.endsWith(SegmentoClientes.NombreCampo.GRUPO_DE_EDAD.toString()))
    }

    @Nested
    inner class AplicaParaDatos
    {
        private val valorGrupoEdadSegmento = ValorGrupoEdad("Edad", null, null)
        private val valorGrupoEdadOtro = ValorGrupoEdad("Otro", null, null)

        @Nested
        inner class ConGrupoDeClientesConCategoriaYGrupoEdad
        {
            private val grupoDeClientes = GrupoClientes(null, "Prueba", listOf(segmentoCategoria, segmentoGrupoEdad))

            @Test
            fun retorna_true_para_datos_con_la_misma_categoria_y_grupo_de_edad()
            {
                val datosGrupoClientesPruebas = DatosParaCalculoGrupoClientes(Persona.Categoria.A, valorGrupoEdadSegmento,Persona.Tipo.FACULTATIVO)
                Assertions.assertTrue(grupoDeClientes.aplicaParaDatos(datosGrupoClientesPruebas))
            }

            @Test
            fun retorna_false_para_datos_con_la_misma_categoria_pero_diferente_grupo_de_edad()
            {
                val datosGrupoClientesPruebas = DatosParaCalculoGrupoClientes(Persona.Categoria.A, valorGrupoEdadOtro, Persona.Tipo.FACULTATIVO)
                Assertions.assertFalse(grupoDeClientes.aplicaParaDatos(datosGrupoClientesPruebas))
            }

            @Test
            fun retorna_false_para_datos_con_la_misma_categoria_pero_grupo_de_edad_nulo()
            {
                val datosGrupoClientesPruebas = DatosParaCalculoGrupoClientes(Persona.Categoria.A, null,Persona.Tipo.FACULTATIVO)
                Assertions.assertFalse(grupoDeClientes.aplicaParaDatos(datosGrupoClientesPruebas))
            }

            @Test
            fun retorna_false_para_datos_con_mismo_grupo_de_edad_pero_diferente_categoria()
            {
                val datosGrupoClientesPruebas = DatosParaCalculoGrupoClientes(Persona.Categoria.C, valorGrupoEdadSegmento,Persona.Tipo.FACULTATIVO)
                Assertions.assertFalse(grupoDeClientes.aplicaParaDatos(datosGrupoClientesPruebas))
            }

            @Test
            fun retorna_false_para_datos_con_mismo_grupo_de_edad_pero_categoria_NINGUNA()
            {
                val datosGrupoClientesPruebas = DatosParaCalculoGrupoClientes(Persona.Categoria.D, valorGrupoEdadSegmento,Persona.Tipo.FACULTATIVO)
                Assertions.assertFalse(grupoDeClientes.aplicaParaDatos(datosGrupoClientesPruebas))
            }
        }

        @Nested
        inner class ConGrupoDeClientesSoloConCategoria
        {
            private val grupoDeClientes = GrupoClientes(null, "Prueba", listOf(segmentoCategoria))

            @Test
            fun retorna_true_para_datos_con_la_misma_categoria_y_un_grupo_de_edad_cualquiera()
            {
                val datosGrupoClientesPruebas = DatosParaCalculoGrupoClientes(Persona.Categoria.A, valorGrupoEdadSegmento,Persona.Tipo.FACULTATIVO)
                Assertions.assertTrue(grupoDeClientes.aplicaParaDatos(datosGrupoClientesPruebas))
            }

            @Test
            fun retorna_true_para_datos_con_la_misma_categoria_pero_grupo_de_edad_nulo()
            {
                val datosGrupoClientesPruebas = DatosParaCalculoGrupoClientes(Persona.Categoria.A, null,Persona.Tipo.FACULTATIVO)
                Assertions.assertTrue(grupoDeClientes.aplicaParaDatos(datosGrupoClientesPruebas))
            }

            @Test
            fun retorna_false_para_datos_con_diferente_categoria()
            {
                val datosGrupoClientesPruebas = DatosParaCalculoGrupoClientes(Persona.Categoria.C, valorGrupoEdadSegmento,Persona.Tipo.FACULTATIVO)
                Assertions.assertFalse(grupoDeClientes.aplicaParaDatos(datosGrupoClientesPruebas))
            }

            @Test
            fun retorna_false_para_datos_con_categoria_NINGUNA()
            {
                val datosGrupoClientesPruebas = DatosParaCalculoGrupoClientes(Persona.Categoria.D, valorGrupoEdadSegmento,Persona.Tipo.FACULTATIVO)
                Assertions.assertFalse(grupoDeClientes.aplicaParaDatos(datosGrupoClientesPruebas))
            }
        }

        @Nested
        inner class ConGrupoDeClientesSoloConGrupoEdad
        {
            private val grupoDeClientes = GrupoClientes(null, "Prueba", listOf( segmentoGrupoEdad))

            @Test
            fun retorna_true_para_datos_con_el_mismo_grupo_de_edad_y_una_categoria_cualquiera()
            {
                val datosGrupoClientesPruebas = DatosParaCalculoGrupoClientes(Persona.Categoria.D, valorGrupoEdadSegmento,Persona.Tipo.FACULTATIVO)
                Assertions.assertTrue(grupoDeClientes.aplicaParaDatos(datosGrupoClientesPruebas))
            }

            @Test
            fun retorna_true_para_datos_con_mismo_grupo_de_edad_pero_categoria_NINGUNA()
            {
                val datosGrupoClientesPruebas = DatosParaCalculoGrupoClientes(Persona.Categoria.D, valorGrupoEdadSegmento,Persona.Tipo.FACULTATIVO)
                Assertions.assertTrue(grupoDeClientes.aplicaParaDatos(datosGrupoClientesPruebas))
            }

            @Test
            fun retorna_false_para_datos_con_diferente_grupo_de_edad()
            {
                val datosGrupoClientesPruebas = DatosParaCalculoGrupoClientes(Persona.Categoria.A, valorGrupoEdadOtro,Persona.Tipo.FACULTATIVO)
                Assertions.assertFalse(grupoDeClientes.aplicaParaDatos(datosGrupoClientesPruebas))
            }

            @Test
            fun retorna_false_para_datos_con_grupo_de_edad_nulo()
            {
                val datosGrupoClientesPruebas = DatosParaCalculoGrupoClientes(Persona.Categoria.A, null,Persona.Tipo.FACULTATIVO)
                Assertions.assertFalse(grupoDeClientes.aplicaParaDatos(datosGrupoClientesPruebas))
            }
        }
    }
}

@DisplayName("SegmentoClientes")
internal class SegmentoClientesPruebas
{
    @Test
    fun hace_trim_a_valor_correctamente()
    {
        val entidadSinTrim = SegmentoClientes(1, SegmentoClientes.NombreCampo.GRUPO_DE_EDAD, "    Prueba    ")
        val entidadConTrim = SegmentoClientes(1, SegmentoClientes.NombreCampo.GRUPO_DE_EDAD, "Prueba")
        assertEquals(entidadConTrim, entidadSinTrim)
    }

    @Test
    fun campos_quedan_con_valores_correctos()
    {
        val entidad = SegmentoClientes(1, SegmentoClientes.NombreCampo.GRUPO_DE_EDAD, "Prueba")
        assertEquals("Prueba", entidad.campoValor.valor)
    }

    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val entidadInicial = SegmentoClientes(1, SegmentoClientes.NombreCampo.GRUPO_DE_EDAD, "Prueba")
        val entidadCopiada = entidadInicial.copiar(valor = "Prueba editada")
        val entidadEsperada = SegmentoClientes(1, SegmentoClientes.NombreCampo.GRUPO_DE_EDAD, "Prueba editada")
        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun no_se_permite_instanciar_con_valor_vacio()
    {
        val excepcion = assertThrows<EntidadConCampoVacio> {
            SegmentoClientes(1, SegmentoClientes.NombreCampo.CATEGORIA, "")
        }

        assertEquals(SegmentoClientes.Campos.VALOR, excepcion.nombreDelCampo)
        assertEquals(SegmentoClientes.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun no_se_permite_instanciar_con_valor_con_espacio_o_tabs()
    {
        val excepcion = assertThrows<EntidadConCampoVacio> {
            SegmentoClientes(1, SegmentoClientes.NombreCampo.CATEGORIA, "           ")
        }

        assertEquals(SegmentoClientes.Campos.VALOR, excepcion.nombreDelCampo)
        assertEquals(SegmentoClientes.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun cuando_el_campo_es_categoria_se_permiten_todos_los_valores_de_categoria_validos()
    {
        Persona.Categoria.values().forEach {
            SegmentoClientes(1, SegmentoClientes.NombreCampo.CATEGORIA, it.toString())
        }
    }

    @Test
    fun cuando_el_campo_es_categoria_no_se_permiten_valores_de_categoria_invalidos()
    {
        val valoresInvalidos = listOf("E", "INVALIDO", "MI_CATEGORIA")
        valoresInvalidos.forEach {
            val excepcion = assertThrows<EntidadConValorNoPermitido> { SegmentoClientes(1, SegmentoClientes.NombreCampo.CATEGORIA, it) }

            assertEquals(SegmentoClientes.Campos.VALOR, excepcion.nombreDelCampo)
            assertEquals(SegmentoClientes.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
            assertEquals(it, excepcion.valorUsado)
            assertEquals(Persona.Categoria.values().map { it.toString() }.sorted(), excepcion.valoresPermitidos.sorted())
        }
    }
}