package co.smartobjects.prompterbackend.serviciosrest.clientes

import co.smartobjects.entidades.clientes.Cliente
import co.smartobjects.integraciones.cafam.IntegracionCafamImpl
import co.smartobjects.persistencia.personas.camposdepersona.ListadoCamposPredeterminados
import co.smartobjects.prompterbackend.Dependencias
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.fondos.*
import co.smartobjects.prompterbackend.serviciosrest.fondos.libros.RecursoLibrosDePrecios
import co.smartobjects.prompterbackend.serviciosrest.fondos.libros.RecursoLibrosDeProhibiciones
import co.smartobjects.prompterbackend.serviciosrest.fondos.libros.RecursoLibrosSegunReglas
import co.smartobjects.prompterbackend.serviciosrest.fondos.libros.RecursoLibrosSegunReglasCompleto
import co.smartobjects.prompterbackend.serviciosrest.fondos.precios.RecursoGruposClientes
import co.smartobjects.prompterbackend.serviciosrest.fondos.precios.RecursoImpuestos
import co.smartobjects.prompterbackend.serviciosrest.operativas.compras.RecursoCompras
import co.smartobjects.prompterbackend.serviciosrest.operativas.ordenes.RecursoLotesDeOrdenes
import co.smartobjects.prompterbackend.serviciosrest.operativas.ordenes.RecursoOrdenes
import co.smartobjects.prompterbackend.serviciosrest.operativas.reservas.RecursoReservas
import co.smartobjects.prompterbackend.serviciosrest.operativas.reservas.sesionesdemanilla.RecursoSesionesDeManilla
import co.smartobjects.prompterbackend.serviciosrest.personas.*
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.*
import co.smartobjects.prompterbackend.serviciosrest.ubicaciones.RecursoUbicaciones
import co.smartobjects.prompterbackend.serviciosrest.ubicaciones.contabilizables.RecursoConteosUbicaciones
import co.smartobjects.prompterbackend.serviciosrest.ubicaciones.contabilizables.RecursoUbicacionesContabilizables
import co.smartobjects.prompterbackend.serviciosrest.usuarios.RecursoPermisosPosibles
import co.smartobjects.prompterbackend.serviciosrest.usuarios.RecursoRoles
import co.smartobjects.prompterbackend.serviciosrest.usuarios.RecursoUsuarios
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.clientes.ClienteDTO
import javax.ws.rs.Consumes
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path(RecursoClientes.RUTA)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoClientes constructor(private val dependencias: Dependencias)
    : RecursoCreacionConAutenticacionGlobal<Cliente, ClienteDTO>,
      RecursoListarTodosConAutenticacionGlobal<Cliente, ClienteDTO>
{
    companion object
    {
        const val RUTA = "clients"
    }


    override val manejadorSeguridad = dependencias.manejadorSeguridad

    override val codigosError: CodigosErrorDTO = ClienteDTO.CodigosError
    override val nombreEntidad: String = Cliente.NOMBRE_ENTIDAD


    override fun transformarHaciaDTO(entidadDeNegocio: Cliente): ClienteDTO
    {
        return ClienteDTO(entidadDeNegocio)
    }

    override fun crearEntidadDeNegocio(entidad: Cliente): Cliente
    {
        return dependencias.repositorioClientes.crear(entidad.copiar(id = null)).also {
            val idCliente = it.id!!

            dependencias.inicializarTablasNecesariasCliente(idCliente)

            crearCamposPredeterminados(idCliente)
            crearRolParaConfiguracionInicial(idCliente)
            crearUsuarioParaConfiguracionInicial(idCliente)
            crearLlaveNFC(idCliente)
        }
    }

    private fun crearCamposPredeterminados(idCliente: Long)
    {
        dependencias.repositorioCampoDePersonas.crear(idCliente, ListadoCamposPredeterminados())
    }

    private fun crearRolParaConfiguracionInicial(idCliente: Long)
    {
        darRecursosEntidadEspecifica(idCliente).darRecursoRoles().crearRolParaConfiguracionInicial()
    }

    private fun crearUsuarioParaConfiguracionInicial(idCliente: Long)
    {
        darRecursosEntidadEspecifica(idCliente).darRecursoUsuarios().crearUsuarioParaConfiguracionInicial()
    }

    private fun crearLlaveNFC(idCliente: Long)
    {
        val llaveGenerada = dependencias.generadorLlaveNFCCliente.generarLlave()
        val llaveNFCACrear = Cliente.LlaveNFC(idCliente, llaveGenerada)
        darRecursosEntidadEspecifica(idCliente).darRecursoLlavesNFC().crearEntidadDeNegocio(llaveNFCACrear)
    }

    override fun listarTodos(): Sequence<Cliente>
    {
        return dependencias.repositorioClientes.listar()
    }

    @Path("{id}")
    fun darRecursosEntidadEspecifica(@PathParam("id") id: Long): RecursoCliente
    {
        dependencias.repositorioClientes.inicializarConexionAEsquemaDeSerNecesario(id)
        return RecursoCliente(id)
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    internal inner class RecursoCliente(override val id: Long)
        : RecursoActualizableConAutenticacionGlobal<Cliente, ClienteDTO, Long>,
          RecursoConsultarUnoConAutenticacionGlobal<Cliente, ClienteDTO, Long>,
          RecursoEliminarPorIdConAutenticacionGlobal<Cliente, ClienteDTO, Long>
    {
        override fun consultarPorId(id: Long): Cliente?
        {
            return dependencias.repositorioClientes.buscarPorId(id)
        }

        override val codigosError: CodigosErrorDTO = this@RecursoClientes.codigosError
        override val nombreEntidad: String = this@RecursoClientes.nombreEntidad
        override val manejadorSeguridad: ManejadorSeguridad = this@RecursoClientes.manejadorSeguridad

        override fun sustituirIdEnEntidad(idAUsar: Long, dto: ClienteDTO): ClienteDTO
        {
            return dto.copy(id = idAUsar)
        }

        override fun transformarHaciaDTO(entidadDeNegocio: Cliente): ClienteDTO
        {
            return this@RecursoClientes.transformarHaciaDTO(entidadDeNegocio)
        }

        override fun actualizarEntidadDeNegocio(entidad: Cliente): Cliente
        {
            return dependencias.repositorioClientes.actualizar(id, entidad)
        }

        override fun eliminarPorId(id: Long): Boolean
        {
            return dependencias.repositorioClientes.eliminarPorId(id)
        }


        @Path(RecursoLlavesNFC.RUTA)
        fun darRecursoLlavesNFC() = RecursoLlavesNFC(id, dependencias.repositorioLlavesNFC, manejadorSeguridad)

        @Path(RecursoPermisosPosibles.RUTA)
        fun darRecursoPermisosPosibles() = RecursoPermisosPosibles(id, manejadorSeguridad)

        @Path(RecursoRoles.RUTA)
        fun darRecursoRoles() = RecursoRoles(id, dependencias.repositorioRoles, manejadorSeguridad)

        @Path(RecursoUsuarios.RUTA)
        fun darRecursoUsuarios() = RecursoUsuarios(id, dependencias.repositorioUsuarios, manejadorSeguridad)

        @Path(RecursoUbicaciones.RUTA)
        fun darRecursoUbicaciones() =
                RecursoUbicaciones(
                        id,
                        dependencias.repositorioUbicaciones,
                        dependencias.repositorioConsumibleEnPuntoDeVenta,
                        dependencias.repositorioFondosEnPuntoDeVenta,
                        dependencias.repositorioConteoUbicaciones,
                        manejadorSeguridad
                                  )

        @Path(RecursoUbicacionesContabilizables.RUTA)
        fun darRecursoUbicacionesContabilizables() =
                RecursoUbicacionesContabilizables(
                        id,
                        dependencias.repositorioUbicacionesContabilizables,
                        manejadorSeguridad
                                                 )

        @Path(RecursoConteosUbicaciones.RUTA)
        fun darRecursoConteosUbicaciones() =
                RecursoConteosUbicaciones(
                        id,
                        dependencias.repositorioConteoUbicaciones,
                        manejadorSeguridad
                                         )

        @Path(RecursoPersonas.RUTA)
        fun darRecursoPersonas() =
                RecursoPersonas(
                        id,
                        dependencias.repositorioPersonas,
                        dependencias.repositorioComprasDeUnaPersona,
                        dependencias.repositorioCreditosDeUnaPersona,
                        manejadorSeguridad
                               )

        @Path(RecursoPersonasDeUnaCompra.RUTA)
        fun darRecursoPersonasDeUnaCompra() = RecursoPersonasDeUnaCompra(id, dependencias.repositorioPersonasDeUnaCompra, manejadorSeguridad)

        private val integracionCafam: IntegracionCafamImpl by lazy {
            IntegracionCafamImpl(id, dependencias.repositorioPersonas, dependencias.repositorioRelacionesPersonas)
        }

        @Path(RecursoPersonaPorDocumento.RUTA)
        fun darRecursoPersonaPorDocumento() =
                RecursoPersonaPorDocumento(
                        id,
                        dependencias.repositorioRelacionesPersonas,
                        manejadorSeguridad,
                        integracionCafam
                                          )

        @Path(RecursoPaquetes.RUTA)
        fun darRecursoPaquetes() = RecursoPaquetes(id, dependencias.repositorioPaquetes, manejadorSeguridad)

        @Path(RecursoFondos.RUTA)
        fun darRecursoFondos() = RecursoFondos(id, dependencias.repositorioFondos, manejadorSeguridad)

        @Path(RecursoCategoriasSku.RUTA)
        fun darRecursoCategoriasSkus() = RecursoCategoriasSku(id, dependencias.repositorioCategoriasSkus, manejadorSeguridad)

        @Path(RecursoSkus.RUTA)
        fun darRecursoSkus() = RecursoSkus(id, dependencias.repositorioSkus, manejadorSeguridad)

        @Path(RecursoAccesos.RUTA)
        fun darRecursoAccesos() = RecursoAccesos(id, dependencias.repositorioAccesos, manejadorSeguridad)

        @Path(RecursoEntradas.RUTA)
        fun darRecursoEntradas() = RecursoEntradas(id, dependencias.repositorioEntradas, manejadorSeguridad)

        @Path(RecursoMonedas.RUTA)
        fun darRecursoMonedas() = RecursoMonedas(id, dependencias.repositorioMonedas, manejadorSeguridad)

        @Path(RecursoImpuestos.RUTA)
        fun darRecursoImpuestos() = RecursoImpuestos(id, dependencias.repositorioImpuestos, manejadorSeguridad)

        @Path(RecursoGruposClientes.RUTA)
        fun darRecursoGruposClientes() = RecursoGruposClientes(id, dependencias.repositorioGrupoClientes, manejadorSeguridad)

        @Path(RecursoCamposDePersona.RUTA)
        fun darRecursoCamposDePersona() = RecursoCamposDePersona(id, dependencias.repositorioCampoDePersonas, manejadorSeguridad)

        @Path(RecursoValoresGrupoEdad.RUTA)
        fun darRecursoValoresGrupoEdad() = RecursoValoresGrupoEdad(id, dependencias.repositorioValoresGruposEdad, manejadorSeguridad)

        @Path(RecursoLibrosDePrecios.RUTA)
        fun darRecursoLibrosDePrecios() = RecursoLibrosDePrecios(id, dependencias.repositorioLibroDePrecios, manejadorSeguridad)

        @Path(RecursoLibrosDeProhibiciones.RUTA)
        fun darRecursoLibrosDeProhibiciones() = RecursoLibrosDeProhibiciones(id, dependencias.repositorioLibroDeProhibiciones, manejadorSeguridad)

        @Path(RecursoLibrosSegunReglas.RUTA)
        fun darRecursoLibrosSegunReglas() = RecursoLibrosSegunReglas(id, dependencias.repositorioLibrosSegunReglas, manejadorSeguridad)

        @Path(RecursoLibrosSegunReglasCompleto.RUTA)
        fun darRecursoLibrosSegunReglasCompleto() = RecursoLibrosSegunReglasCompleto(id, dependencias.repositorioLibrosSegunReglasCompleto, manejadorSeguridad)

        @Path(RecursoCompras.RUTA)
        fun darRecursoCompras() = RecursoCompras(id, dependencias.repositorioCompras, manejadorSeguridad)

        @Path(RecursoReservas.RUTA)
        fun darRecursoReservas() = RecursoReservas(id, dependencias.repositorioReservas, manejadorSeguridad)

        @Path(RecursoSesionesDeManilla.RUTA)
        fun darRecursoSesionesDeManilla() =
                RecursoSesionesDeManilla(
                        id,
                        dependencias.repositorioDeSesionDeManilla,
                        dependencias.repositorioOrdenesDeUnaSesionDeManilla,
                        dependencias.repositorioPersonas,
                        manejadorSeguridad
                                        )

        @Path(RecursoLotesDeOrdenes.RUTA)
        fun darRecursoLotesDeOrdenes() = RecursoLotesDeOrdenes(id, dependencias.repositorioOrdenes, manejadorSeguridad)

        @Path(RecursoOrdenes.RUTA)
        fun darRecursoOrdenes() = RecursoOrdenes(id, dependencias.repositorioOrdenes, manejadorSeguridad)
    }
}