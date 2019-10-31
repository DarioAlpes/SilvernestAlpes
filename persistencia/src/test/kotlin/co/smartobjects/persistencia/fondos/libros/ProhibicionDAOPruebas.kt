package co.smartobjects.persistencia.fondos.libros

import co.smartobjects.entidades.fondos.libros.Prohibicion
import co.smartobjects.persistencia.fondos.FondoDAO
import co.smartobjects.persistencia.fondos.paquete.PaqueteDAO
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@DisplayName("ProhibicionDAO")
internal class ProhibicionDAOPruebas
{
    private fun verificarConversionDeProhibicion(tipoEnPrueba: ProhibicionDAO.Tipo, metodoEnPrueba: ProhibicionDAO.() -> Prohibicion?, generarDatosDePrueba: () -> Pair<Long, ProhibicionDAO>)
    {
        val datosDePrueba = generarDatosDePrueba()
        val idEsperado = datosDePrueba.first
        val reglaDAO = datosDePrueba.second

        val reglaObtenida = reglaDAO.metodoEnPrueba()

        assertNotNull(reglaObtenida)
        assertEquals(idEsperado, reglaObtenida!!.id)

        for (tipoDeRegla in ProhibicionDAO.Tipo.values())
        {
            if (tipoDeRegla != tipoEnPrueba)
            {
                val reglaDePrueba = reglaDAO.copy(tipo = tipoDeRegla)
                assertNull(reglaDePrueba.metodoEnPrueba())
            }
        }
    }

    private fun crearProhibicionDaoDePrueba(tipo: ProhibicionDAO.Tipo) =
            ProhibicionDAO(null, LibroDAO(), FondoDAO(id = 0L), PaqueteDAO(id = 0L), tipo)

    @Test
    fun convertirAProhibicionDeFondo_retorna_diferente_a_null_solo_si_tiene_el_tipo_de_paquete_correcto()
    {
        val idEsperado = 1234L
        val tipoDeReglaEnPrueba = ProhibicionDAO.Tipo.FONDO

        verificarConversionDeProhibicion(
                tipoDeReglaEnPrueba,
                {
                    convertirAProhibicionDeFondo()
                },
                {
                    Pair(idEsperado, crearProhibicionDaoDePrueba(tipoDeReglaEnPrueba).copy(fondoDAO = FondoDAO(id = idEsperado)))
                })
    }

    @Test
    fun convertirAProhibicionDePaquete_retorna_diferente_a_null_solo_si_tiene_el_tipo_de_paquete_correcto()
    {
        val idEsperado = 1234L
        val tipoDeReglaEnPrueba = ProhibicionDAO.Tipo.PAQUETE

        verificarConversionDeProhibicion(
                tipoDeReglaEnPrueba,
                {
                    convertirAProhibicionDePaquete()
                },
                {
                    Pair(idEsperado, crearProhibicionDaoDePrueba(tipoDeReglaEnPrueba).copy(paqueteDao = PaqueteDAO(id = idEsperado)))
                })
    }

    @Test
    fun convertirAEntidadesDeNegocio_retorna_reglas_convertidas_correctamente()
    {
        val prohibicionDeFondo = Prohibicion.DeFondo(0)
        val prohibicionDePaquete = Prohibicion.DePaquete(1)

        val reglasEsperadas = ProhibicionDAO.ProhibicionesConvertidas(mutableSetOf(prohibicionDeFondo), mutableSetOf(prohibicionDePaquete))

        val prohibicionesDao =
                setOf(
                        ProhibicionDAO(null, LibroDAO(), fondoDAO = FondoDAO(id = 0), tipo = ProhibicionDAO.Tipo.FONDO),
                        ProhibicionDAO(null, LibroDAO(), paqueteDao = PaqueteDAO(id = 1), tipo = ProhibicionDAO.Tipo.PAQUETE)
                     )

        val reglasConvertidas = ProhibicionDAO.convertirAEntidadesDeNegocio(prohibicionesDao)

        assertEquals(reglasEsperadas, reglasConvertidas)
    }
}