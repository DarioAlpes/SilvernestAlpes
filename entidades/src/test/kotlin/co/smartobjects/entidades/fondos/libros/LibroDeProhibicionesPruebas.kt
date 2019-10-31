package co.smartobjects.entidades.fondos.libros

import co.smartobjects.entidades.excepciones.EntidadConCampoVacio
import co.smartobjects.entidades.excepciones.RelacionEntreCamposInvalida
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

@DisplayName("LibroDeProhibiciones")
internal class LibroDeProhibicionesPruebas
{
    @Test
    fun hace_trim_a_nombre_correctamente()
    {
        val prohibicionDeFondo = Prohibicion.DeFondo(2L)
        val prohibicionDePaquete = Prohibicion.DePaquete(4L)
        val entidadEsperada = LibroDeProhibiciones(1, 2, "Nombre", setOf(prohibicionDeFondo), setOf(prohibicionDePaquete))

        val entidadProcesada = LibroDeProhibiciones(1, 2, "   \t  Nombre\t\t   ", setOf(prohibicionDeFondo), setOf(prohibicionDePaquete))

        assertEquals(entidadEsperada, entidadProcesada)
    }

    @Test
    fun campos_quedan_con_valores_correctos()
    {
        val prohibicionDeFondo = Prohibicion.DeFondo(2L)
        val prohibicionDePaquete = Prohibicion.DePaquete(4L)
        val entidad = LibroDeProhibiciones(1, 2, "Libro", setOf(prohibicionDeFondo), setOf(prohibicionDePaquete))

        assertEquals("Libro", entidad.campoNombre.valor)
    }

    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val prohibicionDeFondoInicio = Prohibicion.DeFondo(2L)
        val prohibicionDePaqueteInicio = Prohibicion.DePaquete(4L)
        val entidadInicial = LibroDeProhibiciones(1, 2, "Libro", setOf(prohibicionDeFondoInicio), setOf(prohibicionDePaqueteInicio))

        val prohibicionDeFondoFinal = Prohibicion.DeFondo(3445L)
        val prohibicionDePaqueteFinal = Prohibicion.DePaquete(37433L)

        val entidadEsperada = LibroDeProhibiciones(10, 20, "Libro precios copiado", setOf(prohibicionDeFondoFinal), setOf(prohibicionDePaqueteFinal))

        val entidadCopiada = entidadInicial.copiar(
                idCliente = 10,
                id = 20,
                nombre = "Libro precios copiado",
                prohibicionesDeFondo = setOf(prohibicionDeFondoFinal),
                prohibicionesDePaquete = setOf(prohibicionDePaqueteFinal)
                                                  )

        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun copiar_con_id_funciona_correctamente()
    {
        val prohibicionDeFondoInicio = Prohibicion.DeFondo(2L)
        val prohibicionDePaqueteInicio = Prohibicion.DePaquete(4L)
        val entidadInicial = LibroDeProhibiciones(1, 2, "Libro", setOf(prohibicionDeFondoInicio), setOf(prohibicionDePaqueteInicio))
        val entidadEsperada = LibroDeProhibiciones(1, 2845786, "Libro", setOf(prohibicionDeFondoInicio), setOf(prohibicionDePaqueteInicio))

        val entidadCopiada = entidadInicial.copiarConId(2845786)

        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun no_se_permite_instanciar_con_nombre_vacio()
    {
        val prohibicionDeFondo = Prohibicion.DeFondo(2L)
        val prohibicionDePaquete = Prohibicion.DePaquete(4L)
        val excepcion = assertThrows<EntidadConCampoVacio> {
            LibroDeProhibiciones(1, 2, "", setOf(prohibicionDeFondo), setOf(prohibicionDePaquete))
        }

        assertEquals(LibroDeProhibiciones.Campos.NOMBRE, excepcion.nombreDelCampo)
        assertEquals(Libro.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun no_se_permite_instanciar_con_nombre_con_solo_espacios_o_tabs()
    {
        val prohibicionDeFondo = Prohibicion.DeFondo(2L)
        val prohibicionDePaquete = Prohibicion.DePaquete(4L)
        val excepcion = assertThrows<EntidadConCampoVacio> {
            LibroDeProhibiciones(1, 2, "   \t\t   ", setOf(prohibicionDeFondo), setOf(prohibicionDePaquete))
        }

        assertEquals(LibroDeProhibiciones.Campos.NOMBRE, excepcion.nombreDelCampo)
        assertEquals(Libro.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun no_se_permite_instanciar_sin_al_menos_una_prohibicion()
    {
        val excepcion = assertThrows<RelacionEntreCamposInvalida> {
            LibroDeProhibiciones(1, 1, "Libro precios", setOf(), setOf())
        }

        assertEquals(LibroDeProhibiciones.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        assertEquals(LibroDeProhibiciones.Campos.PROHIBICIONES_FONDO, excepcion.nombreDelCampo)
        assertEquals(LibroDeProhibiciones.Campos.PROHIBICIONES_FONDO, excepcion.nombreDelCampoIzquierdo)
        assertEquals(LibroDeProhibiciones.Campos.PROHIBICIONES_PAQUETE, excepcion.nombreDelCampoDerecho)
        assertEquals("[]", excepcion.valorUsadoPorCampoIzquierdo)
        assertEquals("[]", excepcion.valorUsadoPorCampoDerecho)
        assertEquals(RelacionEntreCamposInvalida.Relacion.NO_VACIO_SIMULTANEO, excepcion.relacionViolada)
    }
}