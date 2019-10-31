package co.smartobjects.logica.fondos

import co.smartobjects.entidades.fondos.Fondo

interface ProveedorCodigosExternosFondos
{
    fun darCodigoExterno(idFondo: Long): String?
}

class ProveedorCodigosExternosFondosEnMemoria(fondos: Sequence<Fondo<*>>) : ProveedorCodigosExternosFondos
{
    private val idFondoVsCodigoExterno = fondos.associateBy({ it.id!! }, { it.codigoExterno })

    override fun darCodigoExterno(idFondo: Long): String? = idFondoVsCodigoExterno[idFondo]
}