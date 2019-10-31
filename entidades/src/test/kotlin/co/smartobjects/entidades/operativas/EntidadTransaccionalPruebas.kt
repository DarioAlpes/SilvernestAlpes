package co.smartobjects.entidades.operativas

import co.smartobjects.entidades.excepciones.EntidadMalInicializada
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

@DisplayName("EntidadTransaccional")
internal class EntidadTransaccionalPruebas
{
    @Test
    fun convierte_un_id_valido_a_partes()
    {
        val nombreUsuario = "un-usuario"
        val tiempo = 12345L
        val uuid = UUID.randomUUID()
        val id = "$tiempo${EntidadTransaccional.SEPARADOR_COMPONENTES_ID}$nombreUsuario${EntidadTransaccional.SEPARADOR_COMPONENTES_ID}$uuid"
        val partes = EntidadTransaccional.idAPartes(id)
        assertEquals(nombreUsuario, partes.nombreUsuario)
        assertEquals(tiempo, partes.tiempoCreacion)
        assertEquals(uuid, partes.uuid)
    }

    @Test
    fun partes_id_calcula_id_correctamente()
    {
        val nombreUsuario = "un-usuario"
        val tiempo = 12345L
        val uuid = UUID.randomUUID()
        val id = "$tiempo${EntidadTransaccional.SEPARADOR_COMPONENTES_ID}$nombreUsuario${EntidadTransaccional.SEPARADOR_COMPONENTES_ID}$uuid"
        val partes = EntidadTransaccional.idAPartes(id)
        assertEquals(id, partes.id)
    }

    @Test
    fun falla_cuando_el_id_tiene_solo_dos_partes()
    {
        val nombreUsuario = "un-usuario"
        val tiempo = 12345L
        val id = "$tiempo${EntidadTransaccional.SEPARADOR_COMPONENTES_ID}$nombreUsuario"
        val excepcion = Assertions.assertThrows(EntidadMalInicializada::class.java, {
            EntidadTransaccional.idAPartes(id)
        })

        assertEquals(EntidadTransaccional.Campos.ID, excepcion.nombreDelCampo)
        assertEquals(EntidadTransaccional.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun falla_cuando_el_id_tiene_mas_de_tres_partes()
    {
        val nombreUsuario = "un-usuario"
        val tiempo = 12345L
        val uuid = UUID.randomUUID()
        val id = "$tiempo${EntidadTransaccional.SEPARADOR_COMPONENTES_ID}$nombreUsuario${EntidadTransaccional.SEPARADOR_COMPONENTES_ID}$uuid${EntidadTransaccional.SEPARADOR_COMPONENTES_ID}otra_cosa"
        val excepcion = Assertions.assertThrows(EntidadMalInicializada::class.java, {
            EntidadTransaccional.idAPartes(id)
        })

        assertEquals(EntidadTransaccional.Campos.ID, excepcion.nombreDelCampo)
        assertEquals(EntidadTransaccional.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun falla_cuando_el_tiempo_creacion_es_invalido()
    {
        val nombreUsuario = "un-usuario"
        val tiempo = "tiempo_invalido"
        val uuid = UUID.randomUUID()
        val id = "$tiempo${EntidadTransaccional.SEPARADOR_COMPONENTES_ID}$nombreUsuario$uuid"
        val excepcion = Assertions.assertThrows(EntidadMalInicializada::class.java, {
            EntidadTransaccional.idAPartes(id)
        })

        assertEquals(EntidadTransaccional.Campos.ID, excepcion.nombreDelCampo)
        assertEquals(EntidadTransaccional.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun falla_cuando_el_uuid_es_invalido()
    {
        val nombreUsuario = "un-usuario"
        val tiempo = 12345L
        val uuid = "un-uuid-invalido"
        val id = "$tiempo${EntidadTransaccional.SEPARADOR_COMPONENTES_ID}$nombreUsuario$uuid"
        val excepcion = Assertions.assertThrows(EntidadMalInicializada::class.java, {
            EntidadTransaccional.idAPartes(id)
        })

        assertEquals(EntidadTransaccional.Campos.ID, excepcion.nombreDelCampo)
        assertEquals(EntidadTransaccional.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }
}