package co.smartobjects.prompterbackend.serviciosrest.personas

import co.smartobjects.entidades.personas.DocumentoCompleto
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.personas.PersonaConFamiliares
import co.smartobjects.integraciones.cafam.IntegracionCafam
import co.smartobjects.persistencia.personas.relacionesdepersonas.FiltroRelacionesPersonas
import co.smartobjects.persistencia.personas.relacionesdepersonas.RepositorioRelacionesPersonas
import co.smartobjects.prompterbackend.excepciones.EntidadInvalida
import co.smartobjects.prompterbackend.excepciones.EntidadNoExiste
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.ejecutarFuncionListarTransformandoExcepcionesAExcepcionesBackend
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.ejecutarVerificandoPermisosYTransformandoExcepcionesAExcepcionesBackend
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaConsultarSegunNombrePermiso
import co.smartobjects.red.modelos.personas.PersonaConFamiliaresDTO
import co.smartobjects.red.modelos.personas.PersonaDTO
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoPersonaPorDocumento(
        private val idCliente: Long,
        private val repositorioRelacionesPersonas: RepositorioRelacionesPersonas,
        private val manejadorSeguridad: ManejadorSeguridad,
        private val integracionCafam: IntegracionCafam)
{
    companion object
    {
        const val RUTA = "person-by-document"
        internal const val NOMBRE_PARAMETRO_NUMERO_DOCUMENTO = "document-number"
        internal const val NOMBRE_PARAMETRO_TIPO_DOCUMENTO = "document-type"
        // Esta constante va hasta la bd en la tabla de permisos permitidos a un rol. Si se cambia deben actualizarse los permisos correspondientes en la bd
        private const val NOMBRE_PERMISO = "Persona por Documento"
        internal val INFORMACION_PERMISO_CONSULTAR = darInformacionPermisoParaConsultarSegunNombrePermiso(NOMBRE_PERMISO)
    }

    @GET
    fun consultarPorDocumento(
            @QueryParam(NOMBRE_PARAMETRO_NUMERO_DOCUMENTO) numeroDocumento: String?,
            @QueryParam(NOMBRE_PARAMETRO_TIPO_DOCUMENTO) tipoDocumentoStr: String?
                             ): PersonaConFamiliaresDTO?
    {

        return ejecutarFuncionListarTransformandoExcepcionesAExcepcionesBackend(PersonaDTO.CodigosError) {
            if (numeroDocumento == null)
            {
                throw EntidadInvalida("$NOMBRE_PARAMETRO_NUMERO_DOCUMENTO no puede ser nulo", PersonaDTO.CodigosError.DOCUMENTO_INVALIDO)
            }
            if (tipoDocumentoStr == null)
            {
                throw EntidadInvalida("$NOMBRE_PARAMETRO_TIPO_DOCUMENTO no puede ser nulo", PersonaDTO.CodigosError.DOCUMENTO_INVALIDO)
            }
            val tipoDocumeto = try
            {
                Persona.TipoDocumento.valueOf(tipoDocumentoStr.toUpperCase())
            }
            catch (e: IllegalArgumentException)
            {
                throw EntidadInvalida("$NOMBRE_PARAMETRO_TIPO_DOCUMENTO no puede ser $tipoDocumentoStr", PersonaDTO.CodigosError.DOCUMENTO_INVALIDO)
            }


            println("\nnumeroDocumento: "+numeroDocumento)
            val documentoCompleto = DocumentoCompleto(tipoDocumeto, numeroDocumento)
            ejecutarVerificandoPermisosYTransformandoExcepcionesAExcepcionesBackend(manejadorSeguridad, INFORMACION_PERMISO_CONSULTAR, idCliente) {
                val personaConFamiliares =
                        integracionCafam.darInformacionAfiliado(documentoCompleto)
                        ?: repositorioRelacionesPersonas
                            .buscarSegunParametros(idCliente, FiltroRelacionesPersonas.PorDocumentoCompleto(documentoCompleto))

                personaConFamiliares?.let { PersonaConFamiliaresDTO(it) }
                ?: throw EntidadNoExiste(
                        "${documentoCompleto.tipoDocumento} ${documentoCompleto.numeroDocumento}",
                        PersonaConFamiliares.NOMBRE_ENTIDAD,
                        PersonaConFamiliaresDTO.CodigosError.NO_EXISTE
                                        )
            }
        }
    }
}