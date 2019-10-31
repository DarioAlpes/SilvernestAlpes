package co.smartobjects.persistencia.fondos.libros

import co.smartobjects.entidades.fondos.libros.Regla
import co.smartobjects.entidades.fondos.libros.ReglaDeIdGrupoDeClientes
import co.smartobjects.entidades.fondos.libros.ReglaDeIdPaquete
import co.smartobjects.entidades.fondos.libros.ReglaDeIdUbicacion
import co.smartobjects.persistencia.fondos.paquete.PaqueteDAO
import co.smartobjects.persistencia.fondos.precios.gruposclientes.GrupoClientesDAO
import co.smartobjects.persistencia.ubicaciones.UbicacionDAO
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@DisplayName("ReglaDAO")
internal class ReglaDAOPruebas
{
    private fun verificarConversionDeRegla(tipoEnPrueba: ReglaDAO.Tipo, metodoEnPrueba: ReglaDAO.(Long) -> Regla<*>?, generarDatosDePrueba: () -> Pair<Long, ReglaDAO>)
    {
        val datosDePrueba = generarDatosDePrueba()
        val idEsperado = datosDePrueba.first
        val reglaDAO = datosDePrueba.second

        val reglaObtenida = reglaDAO.metodoEnPrueba(999)

        assertNotNull(reglaObtenida)
        assertEquals(idEsperado, reglaObtenida!!.restriccion)

        for (tipoDeRegla in ReglaDAO.Tipo.values())
        {
            if (tipoDeRegla != tipoEnPrueba)
            {
                val reglaDePrueba = reglaDAO.copy(tipo = tipoDeRegla)
                assertNull(reglaDePrueba.metodoEnPrueba(999))
            }
        }
    }

    private fun crearReglaDaoDePrueba(tipo: ReglaDAO.Tipo) =
            ReglaDAO(null, LibroSegunReglasDAO(), UbicacionDAO(id = 0L), GrupoClientesDAO(id = 0L), PaqueteDAO(id = 0L), tipo)

    @Test
    fun intentarConvertirAReglaDeIdUbicacion_retorna_diferente_a_null_solo_si_tiene_el_tipo_de_regla_correcto()
    {
        val idEsperado = 1234L
        val tipoDeReglaEnPrueba = ReglaDAO.Tipo.ID_UBICACION

        verificarConversionDeRegla(
                tipoDeReglaEnPrueba,
                {
                    convertirAReglaDeIdUbicacion()
                },
                {
                    Pair(idEsperado, crearReglaDaoDePrueba(tipoDeReglaEnPrueba).copy(ubicacion = UbicacionDAO(id = idEsperado)))
                })
    }

    @Test
    fun intentarConvertirAReglaDeIdGrupoClientes_retorna_diferente_a_null_solo_si_tiene_el_tipo_de_regla_correcto()
    {
        val idEsperado = 1234L
        val tipoDeReglaEnPrueba = ReglaDAO.Tipo.ID_GRUPO_DE_CLIENTES

        verificarConversionDeRegla(
                tipoDeReglaEnPrueba,
                {
                    convertirAReglaDeIdGrupoClientes()
                },
                {
                    Pair(idEsperado, crearReglaDaoDePrueba(tipoDeReglaEnPrueba).copy(grupo = GrupoClientesDAO(id = idEsperado)))
                })
    }

    @Test
    fun intentarConvertirAReglaDeIdPaquete_retorna_diferente_a_null_solo_si_tiene_el_tipo_de_regla_correcto()
    {
        val idEsperado = 1234L
        val tipoDeReglaEnPrueba = ReglaDAO.Tipo.ID_PAQUETE

        verificarConversionDeRegla(
                tipoDeReglaEnPrueba,
                {
                    convertirAReglaDeIdPaquete()
                },
                {
                    Pair(idEsperado, crearReglaDaoDePrueba(tipoDeReglaEnPrueba).copy(paquete = PaqueteDAO(id = idEsperado)))
                })
    }

    @Test
    fun convertirAEntidadesDeNegocio_retorna_reglas_convertidas_correctamente()
    {
        val idUbicacion = 1L
        val idGrupo = 2L
        val idPaquete = 3L

        val reglaDeUbicacion = ReglaDeIdUbicacion(idUbicacion)

        val reglaDeGrupo = ReglaDeIdGrupoDeClientes(idGrupo)

        val reglaDePaquete = ReglaDeIdPaquete(idPaquete)

        val reglasEsperadas = ReglaDAO.ReglasConvertidas(mutableSetOf(reglaDeUbicacion), mutableSetOf(reglaDeGrupo), mutableSetOf(reglaDePaquete))

        val reglasDao =
                listOf(
                        ReglaDAO(null, LibroSegunReglasDAO(), ubicacion = UbicacionDAO(id = idUbicacion), tipo = ReglaDAO.Tipo.ID_UBICACION),
                        ReglaDAO(null, LibroSegunReglasDAO(), grupo = GrupoClientesDAO(id = idGrupo), tipo = ReglaDAO.Tipo.ID_GRUPO_DE_CLIENTES),
                        ReglaDAO(null, LibroSegunReglasDAO(), paquete = PaqueteDAO(id = idPaquete), tipo = ReglaDAO.Tipo.ID_PAQUETE)
                      )

        val reglasConvertidas = ReglaDAO.convertirAEntidadesDeNegocio(reglasDao)

        assertEquals(reglasEsperadas, reglasConvertidas)
    }
}