package co.smartobjects.entidades.fondos.libros

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DisplayName("ReglaDeIdUbicacion")
internal class ReglaDeIdUbicacionPruebas
{
    @Test
    fun para_un_valor_correcto_dado_pasa_la_validacion()
    {
        val idUbicacion = 1L

        assertTrue(ReglaDeIdUbicacion(idUbicacion).validar(idUbicacion))
    }

    @Test
    fun para_un_valor_incorrecto_dado_no_pasa_la_validacion()
    {
        val idUbicacion = 1L

        assertFalse(ReglaDeIdUbicacion(idUbicacion).validar(idUbicacion + 100))
    }
}

@DisplayName("ReglaDeIdGrupoDeClientes")
internal class ReglaDeIdGrupoDeClientesPruebas
{
    @Test
    fun para_un_valor_correcto_dado_pasa_la_validacion()
    {
        val idGrupoDeClientes = 1L

        assertTrue(ReglaDeIdGrupoDeClientes(idGrupoDeClientes).validar(idGrupoDeClientes))
    }

    @Test
    fun para_un_valor_incorrecto_dado_no_pasa_la_validacion()
    {
        val idGrupoDeClientes = 1L

        assertFalse(ReglaDeIdGrupoDeClientes(idGrupoDeClientes).validar(idGrupoDeClientes + 100))
    }
}

@DisplayName("ReglaDeIdPaquete")
internal class ReglaDeIdPaquetePruebas
{
    @Test
    fun para_un_valor_correcto_dado_pasa_la_validacion()
    {
        val idPaquete = 1L

        assertTrue(ReglaDeIdPaquete(idPaquete).validar(idPaquete))
    }

    @Test
    fun para_un_valor_incorrecto_dado_no_pasa_la_validacion()
    {
        val idPaquete = 1L

        assertFalse(ReglaDeIdPaquete(idPaquete).validar(idPaquete + 100))
    }
}