package co.smartobjects.entidades.fondos.precios

import co.smartobjects.entidades.excepciones.RelacionEntreCamposInvalida
import co.smartobjects.utilidades.Decimal
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

@DisplayName("PrecioCompleto")
class PrecioCompletoPruebas
{
    companion object
    {
        private val impuestoDePrueba = ImpuestoSoloTasa(1, 1, Decimal(19.15))
        private val precioDePrueba = Precio(Decimal(12.45689), impuestoDePrueba.id!!)
    }

    @Test
    fun campos_quedan_con_valores_correctos()
    {
        val entidad = PrecioCompleto(precioDePrueba, impuestoDePrueba)
        assertEquals(Decimal(12.45689), entidad.precioConImpuesto)
        assertEquals(impuestoDePrueba, entidad.impuesto)
    }

    @Test
    fun `El precio sin impuesto se calcula como "precio con impuesto ÷ (1 + tasa_para_calculos)"`()
    {
        val entidad = PrecioCompleto(precioDePrueba, impuestoDePrueba)
        assertEquals(precioDePrueba.valor / (Decimal.UNO + impuestoDePrueba.tasaParaCalculos), entidad.precioSinImpuesto)
    }

    @Test
    fun `El valor del impuesto se calcula como "precio con impuesto - precio sin impuesto"`()
    {
        val entidad = PrecioCompleto(precioDePrueba, impuestoDePrueba)
        assertEquals(precioDePrueba.valor - entidad.precioSinImpuesto, entidad.valorImpuesto)
    }

    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val entidadInicial = PrecioCompleto(precioDePrueba, impuestoDePrueba)

        val nuevoImpuesto = ImpuestoSoloTasa(2, 5, Decimal.DIEZ)
        val nuevoPrecio = Precio(Decimal(3.1416), nuevoImpuesto.id!!)
        val entidadEsperada = PrecioCompleto(nuevoPrecio, nuevoImpuesto)

        val entidadCopiada = entidadInicial.copiar(nuevoPrecio, nuevoImpuesto)

        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun no_se_permite_instanciar_con_un_precio_cuyo_id_de_impuesto_no_coincide_con_el_id_del_impuesto()
    {
        val idImpuestoErroneo = impuestoDePrueba.id!! + 999
        val excepcion = assertThrows<RelacionEntreCamposInvalida> {
            PrecioCompleto(precioDePrueba.copiar(idImpuesto = idImpuestoErroneo), impuestoDePrueba)
        }

        assertEquals("Id del impuesto en el precio", excepcion.nombreDelCampo)
        assertEquals(PrecioCompleto.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        assertEquals("Id del impuesto en el precio", excepcion.nombreDelCampoIzquierdo)
        assertEquals("Id del impuesto", excepcion.nombreDelCampoDerecho)
        assertEquals(idImpuestoErroneo.toString(), excepcion.valorUsadoPorCampoIzquierdo)
        assertEquals(impuestoDePrueba.id.toString(), excepcion.valorUsadoPorCampoDerecho)
        assertEquals(RelacionEntreCamposInvalida.Relacion.IGUAL, excepcion.relacionViolada)
    }
}