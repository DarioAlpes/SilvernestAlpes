package co.smartobjects.logica.fondos

import co.smartobjects.entidades.fondos.Fondo


interface CalculadorPuedeAgregarseSegunUnicidad
{
    fun puedeAgregarFondos(idsFondosAAgregar: Set<Long>, idsFondosEnCarrito: Set<Long>): Boolean
    fun algunoEsUnico(idsDeFondos: Set<Long>): Boolean
}

class CalculadorPuedeAgregarseSegunUnicidadEnMemoria
(
        fondos: Sequence<Fondo<*>>
) : CalculadorPuedeAgregarseSegunUnicidad
{
    private val idsFondosVsEsUnico: Map<Long, Boolean>
    private val idsFondosUnicos: Set<Long>

    init
    {
        val idsFondosVsEsUnicoMutable = mutableMapOf<Long, Boolean>()
        val idsFondosUnicosMutable = mutableSetOf<Long>()

        for (fondo in fondos)
        {
            idsFondosVsEsUnicoMutable[fondo.id!!] = fondo.debeAparecerSoloUnaVez
            if (fondo.debeAparecerSoloUnaVez)
            {
                idsFondosUnicosMutable.add(fondo.id!!)
            }
        }

        idsFondosVsEsUnico = idsFondosVsEsUnicoMutable
        idsFondosUnicos = idsFondosUnicosMutable
    }

    override fun puedeAgregarFondos(idsFondosAAgregar: Set<Long>, idsFondosEnCarrito: Set<Long>): Boolean
    {
        val fondosAAgregarQueSonUnicos = idsFondosAAgregar.intersect(idsFondosUnicos)

        return if (fondosAAgregarQueSonUnicos.isNotEmpty())
        {
            fondosAAgregarQueSonUnicos.intersect(idsFondosEnCarrito).isEmpty()
        }
        else
        {
            true
        }
    }

    override fun algunoEsUnico(idsDeFondos: Set<Long>): Boolean
    {
        for (idDeFondo in idsDeFondos)
        {
            if (idsFondosVsEsUnico[idDeFondo]!!)
            {
                return true
            }
        }

        return false
    }
}

object CalculadorPuedeAgregarseSegunUnicidadUnit : CalculadorPuedeAgregarseSegunUnicidad
{
    override fun puedeAgregarFondos(idsFondosAAgregar: Set<Long>, idsFondosEnCarrito: Set<Long>) = true

    override fun algunoEsUnico(idsDeFondos: Set<Long>) = false
}