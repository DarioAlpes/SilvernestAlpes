package co.smartobjects.integraciones.cafam

import co.smartobjects.entidades.personas.DocumentoCompleto
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.personas.PersonaConFamiliares
import co.smartobjects.persistencia.personas.DocumentoCompletoDAO
import co.smartobjects.persistencia.personas.RepositorioPersonas
import co.smartobjects.persistencia.personas.relacionesdepersonas.FiltroRelacionesPersonas
import co.smartobjects.persistencia.personas.relacionesdepersonas.RepositorioRelacionesPersonas
import com.sun.xml.internal.ws.client.ClientTransportException
import dom.cafam.newhotel.integracionerp_subsidio.ConsultarInfoAfilNewHotelOut
import dom.cafam.newhotel.integracionerp_subsidio.InfoAfilNewHotel
import dom.cafam.newhotel.integracionerp_subsidio.InfoAfilNewHotelRequest
import dom.cafam.newhotel.integracionerp_subsidio.XI9Ef3C41E5D9D30F893Ca355Afc87C682Service
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.LocalDate
import javax.xml.ws.BindingProvider
import javax.xml.ws.WebServiceException


interface IntegracionCafam
{
    fun darInformacionAfiliado(documento: DocumentoCompleto): PersonaConFamiliares?
}

class IntegracionCafamImpl
(
        private val idCliente: Long,
        private val repositorioPersonas: RepositorioPersonas,
        private val repositorioRelacionesPersonas: RepositorioRelacionesPersonas
) : IntegracionCafam
{
    private var _mapeadorServicioCAFAM: MapeadorServicioCAFAM? = null
    private val mapeadorServicioCAFAM by lazy {
        _mapeadorServicioCAFAM ?: MapeadorServicioCAFAMImpl()
    }

    private var _servicioCAFAM: ConsultarInfoAfilNewHotelOut? = null
    private val servicioCAFAM by lazy {
        _servicioCAFAM ?: XI9Ef3C41E5D9D30F893Ca355Afc87C682Service().httpPort
    }

    // Este constructor es interno para que solo se pueda usar dentro del módulo, y por extensión en las pruebas
    internal constructor(
            idCliente: Long,
            repositorio: RepositorioPersonas,
            repositorioRelacionesPersonas: RepositorioRelacionesPersonas,
            mockMapeadorServicioCAFAM: MapeadorServicioCAFAM,
            mockServicioCAFAM: ConsultarInfoAfilNewHotelOut
                        ) : this(idCliente, repositorio, repositorioRelacionesPersonas)
    {
        _mapeadorServicioCAFAM = mockMapeadorServicioCAFAM
        _servicioCAFAM = mockServicioCAFAM
    }


    override fun darInformacionAfiliado(documento: DocumentoCompleto): PersonaConFamiliares?
    {
        try
        {
            val resp = obtenerInfoAfiliadoCafam(documento)
            if (resp.clienuid == null)
            {   println("Respuesta vacia ")
                return null
            }

            val personaRecibida = mapeadorServicioCAFAM.afiliadoCafamAPersona(idCliente, resp)
            val familiaresSinCrear = mutableSetOf<Persona>()

            resp.conyuge.forEach { conyuge ->
                val con = mapeadorServicioCAFAM.conyugeCafamAPersona(idCliente, personaRecibida, conyuge)
                familiaresSinCrear.add(con)
            }

            resp.personasacargo.forEach {
                val con = mapeadorServicioCAFAM.personaACargoCafamAPersona(idCliente, personaRecibida, it)
                familiaresSinCrear.add(con)
            }

            val laPesonaQueSeBuscoEsUnFamiliar =
                    familiaresSinCrear.firstOrNull {
                        it.tipoDocumento == documento.tipoDocumento && it.numeroDocumento == documento.numeroDocumento
                    }

            val personaAProcesar: Persona
            if (laPesonaQueSeBuscoEsUnFamiliar != null)
            {
                personaAProcesar = laPesonaQueSeBuscoEsUnFamiliar
                familiaresSinCrear.remove(laPesonaQueSeBuscoEsUnFamiliar)
                familiaresSinCrear.add(personaRecibida)
            }
            else
            {
                personaAProcesar = personaRecibida
            }

            val personaActualizada = upsert(personaAProcesar)
            val familiares = familiaresSinCrear.asSequence().map { upsert(it) }.toSet()
            val personaConRelacionesACrear = PersonaConFamiliares(personaActualizada, familiares)
            repositorioRelacionesPersonas.crear(idCliente, personaConRelacionesACrear)

            return personaConRelacionesACrear
        }
        catch (ex: IOException)
        {
            //TODO_frnusmartobjects (17/10/2018): Hacer log
        }
        catch (ex: ClientTransportException)
        {
            //TODO_frnusmartobjects (17/10/2018): Hacer log
        }
        catch (ex: WebServiceException)
        {
            //TODO_frnusmartobjects (17/10/2018): Hacer log y ver qué hacer cuando ex.cause == java.io.FileNotFoundException: .\servicio_CAFAM.wsdl (El sistema no puede encontrar el archivo especificado)
        }
        return null
    }

    private fun obtenerInfoAfiliadoCafam(documento: DocumentoCompleto): InfoAfilNewHotel
    {
        val test = InfoAfilNewHotelRequest().apply {
            tpiden = mapeadorServicioCAFAM.convertirADocumentoCAFAM(documento.tipoDocumento)
            idtrab = documento.numeroDocumento
        }
        with((servicioCAFAM as BindingProvider).requestContext)
        {
                this[BindingProvider.USERNAME_PROPERTY] = "ws_prd_consu"
                this[BindingProvider.PASSWORD_PROPERTY] = "P1WsCnt@"
        }

        return servicioCAFAM.consultarInfoAfilNewHotelSync(test)
    }

    private fun upsert(persona: Persona): Persona
    {
        val documentoBuscado = DocumentoCompleto(persona.tipoDocumento, persona.numeroDocumento)

        repositorioPersonas.buscarSegunParametros(idCliente, DocumentoCompletoDAO(documentoBuscado))

        val posiblePersonaExistente = repositorioPersonas.buscarSegunParametros(idCliente, DocumentoCompletoDAO(documentoBuscado))

        return if (posiblePersonaExistente != null)
        {
            val personaNuevaConIdExistente = persona.copiar(id = posiblePersonaExistente.id!!)
            GlobalScope.launch(Dispatchers.IO) {
                repositorioPersonas.actualizar(idCliente, posiblePersonaExistente.id!!, personaNuevaConIdExistente)
            }

            personaNuevaConIdExistente
        }
        else
        {
            repositorioPersonas.crear(idCliente, persona)
        }
    }
}