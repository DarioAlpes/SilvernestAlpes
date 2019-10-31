package co.smartobjects.logica.fondos.libros

import co.smartobjects.cualquiera
import co.smartobjects.entidades.fondos.Paquete
import co.smartobjects.entidades.fondos.libros.Regla
import co.smartobjects.entidades.fondos.libros.ReglaDeIdGrupoDeClientes
import co.smartobjects.entidades.fondos.libros.ReglaDeIdPaquete
import co.smartobjects.entidades.fondos.libros.ReglaDeIdUbicacion
import co.smartobjects.entidades.fondos.precios.GrupoClientes
import co.smartobjects.entidades.fondos.precios.SegmentoClientes
import co.smartobjects.entidades.ubicaciones.Ubicacion
import co.smartobjects.mockConDefaultAnswer
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.doReturn
import org.threeten.bp.ZonedDateTime
import kotlin.test.assertEquals

@DisplayName("MapeadorReglasANombresRestriccionesEnMemoria")
class MapeadorReglasANombresRestriccionesEnMemoriaPruebas
{
    private val mockBuscadorReglasDePreciosAplicables = mockConDefaultAnswer(BuscadorReglasDePreciosAplicables::class.java)

    private val ubicaciones =
            listOf(
                    Ubicacion(1, 1, "Nombre de la ubicación", Ubicacion.Tipo.PUNTO_DE_CONTACTO, Ubicacion.Subtipo.POS, null, linkedSetOf())
                  )

    private val gruposDeClientes =
            listOf(
                    GrupoClientes(1, "Nombre del grupo de clientes", listOf(SegmentoClientes(1, SegmentoClientes.NombreCampo.CATEGORIA, "A")))
                  )

    private val paquetes =
            listOf(
                    Paquete(
                            1,
                            1,
                            "Nombre del paquete",
                            "asdf",
                            true,
                            ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO).minusYears(1),
                            ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO),
                            listOf(Paquete.FondoIncluido(1, "1", Decimal.UNO)),
                            "asdf"
                           )
                  )

    @Test
    fun si_no_hay_reglas_que_apliquen_al_fondo_retorna_una_lista_vacia()
    {
        doReturn(setOf<Regla<*>>())
            .`when`(mockBuscadorReglasDePreciosAplicables)
            .buscarReglasQueDeterminanPrecio(anyLong(), cualquiera(), cualquiera(), cualquiera())

        val mapeador =
                MapeadorReglasANombresRestriccionesEnMemoria(
                        mockBuscadorReglasDePreciosAplicables, ubicaciones, gruposDeClientes, paquetes
                                                            )

        assertEquals(listOf(), mapeador.mapear(1, 1, 1, 1))
    }

    @Test
    fun si_hay_reglas_que_apliquen_al_fondo_se_retornan_los_nombres_correctos()
    {
        val reglasEncontradas =
                setOf<Regla<*>>(
                        ReglaDeIdUbicacion(ubicaciones.first().id!!),
                        ReglaDeIdGrupoDeClientes(gruposDeClientes.first().id!!),
                        ReglaDeIdPaquete(paquetes.first().id!!)
                               )

        doReturn(reglasEncontradas)
            .`when`(mockBuscadorReglasDePreciosAplicables)
            .buscarReglasQueDeterminanPrecio(anyLong(), cualquiera(), cualquiera(), cualquiera())

        val resultadoEsperado = listOf(ubicaciones.first().nombre, gruposDeClientes.first().nombre, paquetes.first().nombre)

        val mapeador =
                MapeadorReglasANombresRestriccionesEnMemoria(
                        mockBuscadorReglasDePreciosAplicables, ubicaciones, gruposDeClientes, paquetes
                                                            )

        assertEquals(resultadoEsperado, mapeador.mapear(1, 1, 1, 1))
    }
}