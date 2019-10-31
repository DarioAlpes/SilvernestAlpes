package co.smartobjects.entidades.operativas.compras

class CreditosDeUnaPersona(
        val idCliente: Long,
        val idPersona: Long,
        val creditosFondos: List<CreditoFondo>,
        val creditosPaquetes: List<CreditoPaquete>
                          )
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = CreditosDeUnaPersona::class.java.simpleName
    }

    @Transient
    val creditos = creditosFondos + creditosPaquetes.flatMap { it.creditosFondos }


    fun copiar(
            idCliente: Long = this.idCliente,
            idPersona: Long = this.idPersona,
            creditosFondos: List<CreditoFondo> = this.creditosFondos,
            creditosPaquetes: List<CreditoPaquete> = this.creditosPaquetes
              ): CreditosDeUnaPersona
    {
        return CreditosDeUnaPersona(idCliente, idPersona, creditosFondos, creditosPaquetes)
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CreditosDeUnaPersona

        if (idCliente != other.idCliente) return false
        if (idPersona != other.idPersona) return false
        if (creditosFondos != other.creditosFondos) return false
        if (creditosPaquetes != other.creditosPaquetes) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = idCliente.hashCode()
        result = 31 * result + idPersona.hashCode()
        result = 31 * result + creditosFondos.hashCode()
        result = 31 * result + creditosPaquetes.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "CreditosDeUnaPersona(idCliente=$idCliente, idPersona=$idPersona, creditosFondos=$creditosFondos, creditosPaquetes=$creditosPaquetes, creditos=$creditos)"
    }
}