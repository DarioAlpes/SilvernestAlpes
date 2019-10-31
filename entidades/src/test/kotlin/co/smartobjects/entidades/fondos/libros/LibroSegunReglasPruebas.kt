package co.smartobjects.entidades.fondos.libros

import co.smartobjects.entidades.excepciones.EntidadConCampoVacio
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

@DisplayName("LibroDePrecios")
internal class LibroSegunReglasPruebas
{
    @Test
    fun hace_trim_a_nombre_correctamente()
    {
        val entidadEsperada = LibroSegunReglas(0, null, "Nombre", 1, hashSetOf(), hashSetOf(), hashSetOf())

        val entidadProcesada = entidadEsperada.copiar(nombre = "   \t  Nombre\t\t   ")

        assertEquals(entidadEsperada, entidadProcesada)
    }

    @Test
    fun campos_quedan_con_valores_correctos()
    {
        val entidad = LibroSegunReglas(0, null, "nombre de prueba", 1, hashSetOf(), hashSetOf(), hashSetOf())

        assertEquals("nombre de prueba", entidad.campoNombre.valor)
    }

    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val reglaUbicacionInicial = ReglaDeIdUbicacion(1L)
        val reglaGrupoInicial = ReglaDeIdGrupoDeClientes(1L)
        val reglaPaqueteInicial = ReglaDeIdPaquete(1L)

        val entidadInicial =
                LibroSegunReglas(
                        0,
                        null,
                        "nombre de prueba",
                        1,
                        linkedSetOf(reglaUbicacionInicial),
                        linkedSetOf(reglaGrupoInicial),
                        linkedSetOf(reglaPaqueteInicial)
                                )

        val reglaUbicacionFinal = ReglaDeIdUbicacion(18L)
        val reglaGrupoFinal = ReglaDeIdGrupoDeClientes(456L)
        val reglaPaqueteFinal = ReglaDeIdPaquete(3456L)

        val entidadEsperada =
                LibroSegunReglas(
                        5,
                        6,
                        "nombre de prueba 2",
                        20,
                        linkedSetOf(reglaUbicacionFinal),
                        linkedSetOf(reglaGrupoFinal),
                        linkedSetOf(reglaPaqueteFinal)
                                )

        val entidadCopiada =
                entidadInicial
                    .copiar(
                            idCliente = 5,
                            id = 6,
                            nombre = "nombre de prueba 2",
                            idLibro = 20,
                            reglasIdUbicacion = linkedSetOf(reglaUbicacionFinal),
                            reglasIdGrupoDeClientes = linkedSetOf(reglaGrupoFinal),
                            reglasIdPaquete = linkedSetOf(reglaPaqueteFinal)
                           )

        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun copiar_con_id_funciona_correctamente()
    {
        val reglaUbicacionInicial = ReglaDeIdUbicacion(1L)
        val reglaGrupoInicial = ReglaDeIdGrupoDeClientes(1L)
        val reglaPaqueteInicial = ReglaDeIdPaquete(1L)

        val entidadInicial =
                LibroSegunReglas(
                        0,
                        null,
                        "nombre de prueba",
                        1,
                        linkedSetOf(reglaUbicacionInicial),
                        linkedSetOf(reglaGrupoInicial),
                        linkedSetOf(reglaPaqueteInicial)
                                )

        val entidadEsperada =
                LibroSegunReglas(
                        0,
                        57345,
                        "nombre de prueba",
                        1,
                        linkedSetOf(reglaUbicacionInicial),
                        linkedSetOf(reglaGrupoInicial),
                        linkedSetOf(reglaPaqueteInicial)
                                )

        val entidadCopiada = entidadInicial.copiarConId(57345)

        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun no_se_permite_instanciar_con_nombre_vacio()
    {
        val excepcion = assertThrows<EntidadConCampoVacio> {
            LibroSegunReglas(0, null, "", 2, hashSetOf(), hashSetOf(), hashSetOf())
        }

        assertEquals(LibroSegunReglas.Campos.NOMBRE, excepcion.nombreDelCampo)
        assertEquals(LibroSegunReglas.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun no_se_permite_instanciar_con_nombre_con_solo_espacios_o_tabs()
    {
        val excepcion = assertThrows<EntidadConCampoVacio> {
            LibroSegunReglas(0, null, "   \t\t   ", 2, hashSetOf(), hashSetOf(), hashSetOf())
        }

        assertEquals(LibroSegunReglas.Campos.NOMBRE, excepcion.nombreDelCampo)
        assertEquals(LibroSegunReglas.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun reglas_es_la_union_de_todas_las_reglas()
    {
        val reglaUbicacion = ReglaDeIdUbicacion(1L)
        val reglaGrupo = ReglaDeIdGrupoDeClientes(2L)
        val reglaPaquete = ReglaDeIdPaquete(3L)

        val reglasEsperadas = sequenceOf<Regla<*>>(reglaUbicacion, reglaGrupo, reglaPaquete)

        val unionDeReglas =
                LibroSegunReglas(
                        0,
                        null,
                        "nombre de prueba",
                        2,
                        linkedSetOf(reglaUbicacion),
                        linkedSetOf(reglaGrupo),
                        linkedSetOf(reglaPaquete)
                                )
                    .reglas

        assertEquals(reglasEsperadas.toSet(), unionDeReglas.toSet())
    }
}