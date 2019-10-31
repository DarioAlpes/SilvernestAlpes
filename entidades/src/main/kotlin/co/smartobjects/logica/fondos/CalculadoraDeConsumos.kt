package co.smartobjects.logica.fondos

import co.smartobjects.entidades.excepciones.EntidadConCampoVacio
import co.smartobjects.entidades.fondos.*
import co.smartobjects.entidades.tagscodificables.FondoEnTag
import co.smartobjects.entidades.ubicaciones.consumibles.Consumo
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.sumar

interface CalculadoraDeConsumos
{
    fun descontarConsumosDeFondos(consumos: List<Consumo>, fondosEnTag: List<CalculadoraDeConsumos.FondoEnTagConIdPaquete>): ResultadosDeConsumos

    data class ResultadosDeConsumos(val desgloses: List<DesgloseDeConsumo>, val fondosEnTag: List<FondoEnTag>)
    {
        init
        {
            if (desgloses.isEmpty())
            {
                throw EntidadConCampoVacio(CalculadoraDeConsumos::ResultadosDeConsumos.name, ResultadosDeConsumos::desgloses.name)
            }

            if (fondosEnTag.isEmpty())
            {
                throw EntidadConCampoVacio(CalculadoraDeConsumos::ResultadosDeConsumos.name, ResultadosDeConsumos::fondosEnTag.name)
            }
        }

        val todosLosConsumosRealizadosPorCompleto = desgloses.all { it.consumidoCompetamente }
    }

    data class DesgloseDeConsumo(val consumo: Consumo, val consumosRealizados: List<ConsumoRealizado>)
    {
        val consumidoCompetamente = consumo.cantidad == consumosRealizados.sumar { it.cantidadConsumida }
    }

    data class ConsumoRealizado(
            val idFondoConsumido: Long,
            val cantidadInicial: Decimal,
            val cantidadConsumida: Decimal,
            val cantidadFinal: Decimal
                               )

    data class FondoEnTagConIdPaquete(val fondoEnTag: FondoEnTag)
    {
        val idFondoComprado = fondoEnTag.idFondoComprado
        val cantidad = fondoEnTag.cantidad
        fun copiarFondoEnTag(cantidad: Decimal = fondoEnTag.cantidad, idFondoComprado: Long = fondoEnTag.idFondoComprado)
                : FondoEnTagConIdPaquete
        {
            return copy(fondoEnTag = fondoEnTag.copiar(cantidad, idFondoComprado))
        }
    }
}

class CalculadoraDeConsumosEnMemoria
(
        fondos: Sequence<Fondo<*>>
) : CalculadoraDeConsumos
{
    private val skusPorIdFondo: Map<Long, Fondo<Sku>>
    private val categoriasSkuPorIdFondo: Map<Long, Fondo<CategoriaSku>>
    private val dineroPorIdFondo: Map<Long, Fondo<Dinero>>
    private val accesoPorIdFondo: Map<Long, AccesoBase<*>>

    init
    {
        val skusPorIdFondoEnProceso = mutableMapOf<Long, Fondo<Sku>>()
        val categoriasSkuPorIdFondoEnProceso = mutableMapOf<Long, Fondo<CategoriaSku>>()
        val dineroPorIdFondoEnProceso = mutableMapOf<Long, Fondo<Dinero>>()
        val accesoPorIdFondoEnProceso = mutableMapOf<Long, AccesoBase<*>>()

        fondos.forEach {
            when (it)
            {
                is Sku           -> skusPorIdFondoEnProceso[it.id!!] = it
                is CategoriaSku  -> categoriasSkuPorIdFondoEnProceso[it.id!!] = it
                is Dinero        -> dineroPorIdFondoEnProceso[it.id!!] = it
                is AccesoBase<*> -> accesoPorIdFondoEnProceso[it.id!!] = it
            }
        }

        skusPorIdFondo = skusPorIdFondoEnProceso
        categoriasSkuPorIdFondo = categoriasSkuPorIdFondoEnProceso
        dineroPorIdFondo = dineroPorIdFondoEnProceso
        accesoPorIdFondo = accesoPorIdFondoEnProceso
    }

    override fun descontarConsumosDeFondos(consumos: List<Consumo>, fondosEnTag: List<CalculadoraDeConsumos.FondoEnTagConIdPaquete>): CalculadoraDeConsumos.ResultadosDeConsumos
    {
        if (consumos.isEmpty())
        {
            throw IllegalStateException("Se debe definir consumos")
        }
        if (fondosEnTag.isEmpty())
        {
            throw IllegalStateException("Se debe tener fondos en el tag")
        }

        var fondosDisponibles = fondosEnTag.associateBy { it.idFondoComprado }
        val consumosRealizados = mutableListOf<CalculadoraDeConsumos.DesgloseDeConsumo>()

        for (consumo in consumos)
        {
            val resultadoConsumo = intentarConsumo(consumo, fondosDisponibles)

            consumosRealizados.add(CalculadoraDeConsumos.DesgloseDeConsumo(consumo, resultadoConsumo.consumosRealizados))
            fondosDisponibles = resultadoConsumo.fondosRestantes
        }

        return CalculadoraDeConsumos.ResultadosDeConsumos(consumosRealizados, fondosDisponibles.values.asSequence().map { it.fondoEnTag }.toList())
    }

    private fun intentarConsumo(consumo: Consumo, fondosDisponibles: Map<Long, CalculadoraDeConsumos.FondoEnTagConIdPaquete>): ResultadoConsumoFinal
    {
        val esFondoIlimitado = esUnFondoIlimitado(consumo.idConsumible)

        val consumoDirectoSobreFondo = consumoUsandoFondoDirectamenteDisponible(consumo, fondosDisponibles, esFondoIlimitado)

        val consumoPorCategorias = consumoDirectoSobreFondo?.consumoFaltante ?: consumo
        val fondosDisponiblesParaCategorias = consumoDirectoSobreFondo?.fondosRestantes ?: fondosDisponibles

        return if (consumoDirectoSobreFondo == null || consumoDirectoSobreFondo.consumoFaltante.cantidad > Decimal.CERO)
        {
            val esTipoAcceso = accesoPorIdFondo.containsKey(consumo.idConsumible)
            if (esTipoAcceso)
            {
                throw NotImplementedError("No está soportado el consumo de accesos/entradas por el momento")
            }
            else
            {
                val consumoUsandoCategoriasSku = consumoUsandoCategoriasSku(consumoPorCategorias, fondosDisponiblesParaCategorias, esFondoIlimitado)

                val consumoPorDinero = consumoUsandoCategoriasSku?.consumoFaltante ?: consumoPorCategorias
                val fondosDisponiblesParaDinero = consumoUsandoCategoriasSku?.fondosRestantes ?: fondosDisponiblesParaCategorias

                if (consumoUsandoCategoriasSku == null || consumoUsandoCategoriasSku.consumoFaltante.cantidad > Decimal.CERO)
                {
                    val consumoUsandoDinero = consumoUsandoDinero(consumoPorDinero, fondosDisponiblesParaDinero, esFondoIlimitado)

                    val consumoFinal = consumoUsandoCategoriasSku?.consumoFaltante ?: consumoPorCategorias
                    val fondosDisponiblesFinales = consumoUsandoCategoriasSku?.fondosRestantes ?: fondosDisponiblesParaCategorias

                    if (consumoUsandoDinero == null || consumoUsandoDinero.consumoFaltante.cantidad > Decimal.CERO)
                    {
                        //Combinar así: Preguntar si (consumoDirectoSobreFondo | consumoUsandoCategoriasSku | consumoUsandoDinero) es nulo. Si sí, ignorar sino sumar
                        consumoDirectoSobreFondo?.aResultadoFinal(fondosDisponibles)
                        ?: ResultadoConsumoFinal(
                                listOf(CalculadoraDeConsumos.ConsumoRealizado(consumo.idConsumible, Decimal.CERO, Decimal.CERO, Decimal.CERO)),
                                fondosDisponibles
                                                )
                    }
                    else
                    {
                        TODO("Combinar consumos de fondos, luego de sku y luego de dinero")
                    }
                }
                else
                {
                    TODO("Combinar consumos de fondos, luego de sku")
                }
            }
        }
        else
        {
            consumoDirectoSobreFondo.aResultadoFinal()
        }
    }

    private fun consumoUsandoFondoDirectamenteDisponible(
            consumo: Consumo,
            fondosDisponibles: Map<Long, CalculadoraDeConsumos.FondoEnTagConIdPaquete>,
            esFondoIlimitado: Boolean
                                                        )
            : ResultadoConsumoIntermedio?
    {
        val posibleFondoDisponible = fondosDisponibles[consumo.idConsumible]

        return if (posibleFondoDisponible != null)
        {
            val cantidadNueva = (posibleFondoDisponible.cantidad - consumo.cantidad).let {
                if (esFondoIlimitado) it else it.max(Decimal.CERO)
            }
            val cantidadConsumida =
                    if (esFondoIlimitado)
                    {
                        consumo.cantidad
                    }
                    else
                    {
                        posibleFondoDisponible.cantidad.min(consumo.cantidad)
                    }

            val faltantePorConsumir = consumo.cantidad - cantidadConsumida

            val nuevosFondosDisponibles = fondosDisponibles.toMutableMap().apply {
                this[consumo.idConsumible] = posibleFondoDisponible.copiarFondoEnTag(cantidad = cantidadNueva)
            }

            ResultadoConsumoIntermedio(
                    listOf(
                            CalculadoraDeConsumos.ConsumoRealizado(
                                    consumo.idConsumible,
                                    posibleFondoDisponible.cantidad,
                                    cantidadConsumida,
                                    cantidadNueva
                                                                  )
                          ),
                    nuevosFondosDisponibles,
                    consumo.copiar(cantidad = faltantePorConsumir)
                                      )
        }
        else
        {
            null
        }
    }

    private fun consumoUsandoCategoriasSku(
            consumo: Consumo,
            fondosDisponibles: Map<Long, CalculadoraDeConsumos.FondoEnTagConIdPaquete>,
            esFondoIlimitado: Boolean
                                          )
            : ResultadoConsumoIntermedio?
    {
        return null
    }

    private fun consumoUsandoDinero(
            consumo: Consumo,
            fondosDisponibles: Map<Long, CalculadoraDeConsumos.FondoEnTagConIdPaquete>,
            esFondoIlimitado: Boolean
                                   )
            : ResultadoConsumoIntermedio?
    {
        return null
    }

    private fun esUnFondoIlimitado(idFondo: Long): Boolean
    {
        return skusPorIdFondo[idFondo]?.esIlimitado
               ?: categoriasSkuPorIdFondo[idFondo]?.esIlimitado
               ?: accesoPorIdFondo[idFondo]?.esIlimitado
               ?: throw FondoNoEncontrado(idFondo)
    }

    private class ResultadoConsumoIntermedio(
            val consumosRealizados: List<CalculadoraDeConsumos.ConsumoRealizado>,
            val fondosRestantes: Map<Long, CalculadoraDeConsumos.FondoEnTagConIdPaquete>,
            val consumoFaltante: Consumo
                                            )
    {
        fun aResultadoFinal(
                fondosRestantes: Map<Long, CalculadoraDeConsumos.FondoEnTagConIdPaquete> = this.fondosRestantes
                           ): ResultadoConsumoFinal
        {
            return ResultadoConsumoFinal(consumosRealizados, fondosRestantes)
        }
    }

    private class ResultadoConsumoFinal(
            val consumosRealizados: List<CalculadoraDeConsumos.ConsumoRealizado>,
            val fondosRestantes: Map<Long, CalculadoraDeConsumos.FondoEnTagConIdPaquete>
                                       )
}

class FondoNoEncontrado(idFondo: Long) : Exception("No se encontró el fondo con id '$idFondo'")