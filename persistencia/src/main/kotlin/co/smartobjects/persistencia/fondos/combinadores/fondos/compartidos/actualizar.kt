package co.smartobjects.persistencia.fondos.combinadores.fondos.compartidos

import co.smartobjects.entidades.fondos.Fondo
import co.smartobjects.persistencia.AsignadorParametro


internal fun <TipoFondo : Fondo<TipoFondo>> crearAsignadorIdClienteAFondo(): AsignadorParametro<TipoFondo, Long>
{
    return object : AsignadorParametro<TipoFondo, Long>
    {
        override fun asignarParametro(entidad: TipoFondo, parametro: Long): TipoFondo
        {
            return entidad.copiarConIdCliente(parametro)
        }
    }
}