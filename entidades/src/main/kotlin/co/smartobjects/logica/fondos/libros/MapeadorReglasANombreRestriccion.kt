package co.smartobjects.logica.fondos.libros

import co.smartobjects.entidades.fondos.Paquete
import co.smartobjects.entidades.fondos.libros.ReglaDeIdGrupoDeClientes
import co.smartobjects.entidades.fondos.libros.ReglaDeIdPaquete
import co.smartobjects.entidades.fondos.libros.ReglaDeIdUbicacion
import co.smartobjects.entidades.fondos.precios.GrupoClientes
import co.smartobjects.entidades.ubicaciones.Ubicacion

interface MapeadorReglasANombresRestricciones
{
    fun mapear(idFondo: Long, idUbicacion: Long?, idGrupoDeCliente: Long?, idPaquete: Long?): List<String>
}

class MapeadorReglasANombresRestriccionesEnMemoria
(
        private val buscadorReglasDePreciosAplicables: BuscadorReglasDePreciosAplicables,
        private val ubicaciones: List<Ubicacion>,
        private val gruposDeClientes: List<GrupoClientes>,
        private val paquetes: List<Paquete>
) : MapeadorReglasANombresRestricciones
{
    override fun mapear(idFondo: Long, idUbicacion: Long?, idGrupoDeCliente: Long?, idPaquete: Long?): List<String>
    {
        val reglasQueAplican =
                buscadorReglasDePreciosAplicables
                    .buscarReglasQueDeterminanPrecio(idFondo, idUbicacion, idGrupoDeCliente, idPaquete)

        return reglasQueAplican.map { regla ->
            when (regla)
            {
                is ReglaDeIdUbicacion       -> ubicaciones.first { it.id == regla.restriccion }.nombre
                is ReglaDeIdGrupoDeClientes -> gruposDeClientes.first { it.id == regla.restriccion }.nombre
                is ReglaDeIdPaquete         -> paquetes.first { it.id == regla.restriccion }.nombre
            }
        }
    }
}