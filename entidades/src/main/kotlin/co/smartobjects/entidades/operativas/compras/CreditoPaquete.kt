package co.smartobjects.entidades.operativas.compras

import co.smartobjects.campos.*
import co.smartobjects.entidades.fondos.precios.ImpuestoSoloTasa
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.entidades.fondos.precios.PrecioCompleto

class CreditoPaquete private constructor(
        val idPaquete: Long,
        val codigoExternoPaquete: String,
        val campoCreditosFondos: CampoCreditosFondos
                                        )
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = CreditoPaquete::class.java.simpleName
    }


    object Campos
    {
        @JvmField
        val CREDITOS_FONDOS = CreditoPaquete::creditosFondos.name
    }

    constructor(
            idPaquete: Long,
            codigoExternoPaquete: String,
            creditosFondos: List<CreditoFondo>
               ) : this(
            idPaquete,
            codigoExternoPaquete,
            CampoCreditosFondos(creditosFondos)
                       )

    val creditosFondos: List<CreditoFondo> = campoCreditosFondos.valor
    val valorPagado = creditosFondos.asSequence().map { it.valorPagado }.reduce { acc, siguiente -> acc + siguiente }
    val valorPagadoSinImpuesto = creditosFondos.asSequence().map { it.valorPagadoSinImpuesto }.reduce { acc, siguiente -> acc + siguiente }
    val valorImpuestoPagado = creditosFondos.asSequence().map { it.valorImpuestoPagado }.reduce { acc, siguiente -> acc + siguiente }

    fun copiar(
            idPaquete: Long = this.idPaquete,
            codigoExternoPaquete: String = this.codigoExternoPaquete,
            creditosFondos: List<CreditoFondo> = this.creditosFondos
              ): CreditoPaquete
    {
        return CreditoPaquete(idPaquete, codigoExternoPaquete, creditosFondos)
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CreditoPaquete

        if (idPaquete != other.idPaquete) return false
        if (codigoExternoPaquete != other.codigoExternoPaquete) return false
        if (creditosFondos != other.creditosFondos) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = idPaquete.hashCode()
        result = 31 * result + codigoExternoPaquete.hashCode()
        result = 31 * result + creditosFondos.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "CreditoPaquete(idPaquete=$idPaquete, codigoExternoPaquete='$codigoExternoPaquete', creditosFondos=$creditosFondos)"
    }

    class CampoCreditosFondos(creditosFondos: List<CreditoFondo>)
        : CampoEntidad<CreditoPaquete, List<CreditoFondo>>(
            creditosFondos,
            ValidadorEnCadena(
                    listOf(
                            ValidadorCampoColeccionNoVacio(),
                            ValidadorCampoIgualEnColeccion<CreditoFondo, List<CreditoFondo>, Long>(CreditoFondo.Campos.ID_PERSONA_DUEÑA) {
                                it.idPersonaDueña
                            }
                          )
                             ),
            NOMBRE_ENTIDAD,
            Campos.CREDITOS_FONDOS
                                                          )
}

data class CreditoPaqueteConNombre constructor(val nombreDelPaquete: String, val cantidad: Int, val creditoAsociado: CreditoPaquete)
{
    init
    {
        ValidadorCampoConLimiteInferior<Int, Int>(1).validarCampo(
                cantidad,
                cantidad,
                CreditoPaqueteConNombre::class.java.simpleName,
                CreditoPaqueteConNombre::cantidad.name
                                                                 )
    }

    val estaPagado = creditoAsociado.creditosFondos.any { it.id != null }

    val preciosCompletos: List<PrecioCompleto> = creditoAsociado.creditosFondos.asSequence().map {
        PrecioCompleto(
                Precio(it.valorPagado, it.idImpuestoPagado),
                ImpuestoSoloTasa(it.idCliente, it.idImpuestoPagado, it.tasaImpositivaUsada)
                      )
    }.toList()

    val precioConImpuestos =
            creditoAsociado.creditosFondos.asSequence().map { it.valorPagado }.reduce { acc, precio -> acc + precio }
}