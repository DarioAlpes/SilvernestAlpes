package co.smartobjects.ui.modelos.pagos

import co.smartobjects.ui.modelos.ListaFiltrableUI
import co.smartobjects.ui.modelos.ListaFiltrableUIConSujetos
import co.smartobjects.ui.modelos.ModeloUI
import co.smartobjects.ui.modelos.selecciondecreditos.ProcesoSeleccionCreditosUI
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.sumar
import io.reactivex.Observable
import io.reactivex.Observer

interface TotalAPagarSegunPersonasUI : ModeloUI
{
    val listadoDePersonasConCreditos: ListaFiltrableUI<ProcesoSeleccionCreditosUI.CreditosPorPersonaAProcesar>

    val totalSinImpuesto: Observable<Decimal>
    val impuestoTotal: Observable<Decimal>
    val total: Observable<Decimal>
    val granTotal: Decimal

    override val observadoresInternos: List<Observer<*>>
        get() = listOf()
}

class TotalAPagarSegunPersonas
(
        creditosPorPersonaAProcesar: List<ProcesoSeleccionCreditosUI.CreditosPorPersonaAProcesar>
) : TotalAPagarSegunPersonasUI
{
    override val listadoDePersonasConCreditos = ListaFiltrableUIConSujetos(creditosPorPersonaAProcesar)

    override val granTotal =
            creditosPorPersonaAProcesar.map {
                it.creditosFondoAPagar.sumar { it.creditoAsociado.valorPagado } +
                it.creditosPaqueteAPagar.sumar { it.creditoAsociado.valorPagado }
            }.sumar()

    override val totalSinImpuesto: Observable<Decimal> =
            listadoDePersonasConCreditos.itemsSeleccionados.map {
                it.asSequence().flatMap {
                    it.creditosFondoAPagar.asSequence().map { it.creditoAsociado.valorPagadoSinImpuesto } +
                    it.creditosPaqueteAPagar.asSequence().map { it.creditoAsociado.valorPagadoSinImpuesto }
                }.fold(Decimal.CERO) { sumado, siguienteValor -> sumado + siguienteValor }
            }

    override val impuestoTotal: Observable<Decimal> =
            listadoDePersonasConCreditos.itemsSeleccionados.map {
                it.asSequence().flatMap {
                    it.creditosFondoAPagar.asSequence().map { it.creditoAsociado.valorImpuestoPagado } +
                    it.creditosPaqueteAPagar.asSequence().map { it.creditoAsociado.valorImpuestoPagado }
                }.fold(Decimal.CERO) { sumado, siguienteValor -> sumado + siguienteValor }
            }

    override val total: Observable<Decimal> =
            listadoDePersonasConCreditos.itemsSeleccionados.map {
                it.asSequence().flatMap {
                    it.creditosFondoAPagar.asSequence().map { it.creditoAsociado.valorPagado } +
                    it.creditosPaqueteAPagar.asSequence().map { it.creditoAsociado.valorPagado }
                }.fold(Decimal.CERO) { sumado, siguienteValor -> sumado + siguienteValor }
            }

    override val modelosHijos = listOf(listadoDePersonasConCreditos)
}