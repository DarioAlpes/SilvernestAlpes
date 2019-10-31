package co.smartobjects.integraciones.cafam

import co.smartobjects.entidades.personas.DocumentoCompleto
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.personas.PersonaConFamiliares
import co.smartobjects.integraciones.cualquiera
import co.smartobjects.integraciones.eqParaKotlin
import co.smartobjects.integraciones.mockConDefaultAnswer
import co.smartobjects.persistencia.personas.DocumentoCompletoDAO
import co.smartobjects.persistencia.personas.RepositorioPersonas
import co.smartobjects.persistencia.personas.relacionesdepersonas.RepositorioRelacionesPersonas
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl
import dom.cafam.newhotel.integracionerp_subsidio.ConsultarInfoAfilNewHotelOut
import dom.cafam.newhotel.integracionerp_subsidio.InfoAfilNewHotel
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.threeten.bp.LocalDate
import java.math.BigInteger
import java.util.*
import java.util.stream.Stream
import javax.xml.ws.BindingProvider
import kotlin.test.assertEquals


internal class IntegracionCafamPruebas
{
    internal interface InterfazDummyPruebas : ConsultarInfoAfilNewHotelOut, BindingProvider

    internal class ProveedorRelacionesAfiliado : ArgumentsProvider
    {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments>
        {
            return Stream.of(
                    Arguments.of("varios cónyuges y personas a cargo", listOf(conyugePorDefecto, conyuge2), listOf(aCargo, aCargo)),
                    Arguments.of("ningún cónyuge y personas a cargo", listOf<InfoAfilNewHotel.CONYUGE>(), listOf(aCargo, aCargo, aCargo)),
                    Arguments.of("varios cónyuges y ninguna persona a cargo", listOf(conyugePorDefecto, conyuge2), listOf<InfoAfilNewHotel.PERSONASACARGO>()),
                    Arguments.of("ningún cónyuge y ninguna persona a cargo", listOf<InfoAfilNewHotel.CONYUGE>(), listOf<InfoAfilNewHotel.PERSONASACARGO>())
                            )
        }
    }

    companion object
    {
        private const val ID_CLIENTE = 1L
        private const val TIPO_DE_DOCUMENTO_CAFAM_PRUEBAS = "1"
        private val TIPO_DE_DOCUMENTO_NEGOCIO_PRUEBAS = Persona.TipoDocumento.CC
        private const val DOCUMENTO_PERSONA = "documento-persona-123"
        private const val DOCUMENTO_CONYUGE = "documento-conyuge-456"
        private const val DOCUMENTO_PERSONA_A_CARGO = "documento-persona-a-cargo-789"
        private val conyugePorDefecto = InfoAfilNewHotel.CONYUGE().apply {
            tidcony = TIPO_DE_DOCUMENTO_CAFAM_PRUEBAS
            idcony = DOCUMENTO_CONYUGE
            nomcony = "nombre-conyuge"
        }
        private val conyuge2 = InfoAfilNewHotel.CONYUGE().apply {
            tidcony = TIPO_DE_DOCUMENTO_CAFAM_PRUEBAS
            idcony = "documento-conyuge-2-456"
            nomcony = "nombre-conyuge-2"
        }
        private val aCargo = InfoAfilNewHotel.PERSONASACARGO().apply {
            tidpac = TIPO_DE_DOCUMENTO_CAFAM_PRUEBAS
            idpac = DOCUMENTO_PERSONA_A_CARGO
        }
        private val persona = Persona(ID_CLIENTE, 12L, "Preuba", Persona.TipoDocumento.CC, DOCUMENTO_PERSONA, Persona.Genero.FEMENINO, LocalDate.now(), Persona.Categoria.A, Persona.Afiliacion.BENEFICIARIO, true, null, "empresa","999",Persona.Tipo.NO_AFILIADO)
        private val personaConyuge = Persona(ID_CLIENTE, 2L, "Preuba2", Persona.TipoDocumento.CC, DOCUMENTO_CONYUGE, Persona.Genero.MASCULINO, LocalDate.now(), Persona.Categoria.A, Persona.Afiliacion.BENEFICIARIO, true, null, "empresa","999",Persona.Tipo.NO_AFILIADO)
        private val personaACargo = Persona(ID_CLIENTE, 3L, "Preuba3", Persona.TipoDocumento.CC, DOCUMENTO_PERSONA_A_CARGO, Persona.Genero.FEMENINO, LocalDate.now(), Persona.Categoria.A, Persona.Afiliacion.BENEFICIARIO, true, null, "empresa","999",Persona.Tipo.NO_AFILIADO)
    }

    private val afiliadoCafamPorDefecto = mockConDefaultAnswer(InfoAfilNewHotel::class.java).also {
        doReturn("El nombre").`when`(it).clienome
        doReturn("El apellido").`when`(it).clieapel
        doReturn(TIPO_DE_DOCUMENTO_CAFAM_PRUEBAS).`when`(it).clietiid
        doReturn(DOCUMENTO_PERSONA).`when`(it).clienuid
        doReturn("1").`when`(it).cliesexo
        doReturn(XMLGregorianCalendarImpl(GregorianCalendar(2018, 1, 31, 17, 55))).`when`(it).cliedana
        doReturn(BigInteger("2")).`when`(it).ticlcodi

        doReturn(listOf(conyugePorDefecto))
            .`when`(it)
            .conyuge

        doReturn(listOf(aCargo))
            .`when`(it)
            .personasacargo
    }
    private val mockRepositorioPersonas = mockConDefaultAnswer(RepositorioPersonas::class.java).also {
        val documentoBuscado = DocumentoCompleto(persona.tipoDocumento, persona.numeroDocumento)
        val documentoBuscadoConyuge = DocumentoCompleto(personaConyuge.tipoDocumento, personaConyuge.numeroDocumento)
        val documentoBuscadoACargo = DocumentoCompleto(personaACargo.tipoDocumento, personaACargo.numeroDocumento)

        doReturn(persona)
            .`when`(it)
            .buscarSegunParametros(eq(ID_CLIENTE), eqParaKotlin(DocumentoCompletoDAO(documentoBuscado)))

        doReturn(personaConyuge)
            .`when`(it)
            .buscarSegunParametros(eq(ID_CLIENTE), eqParaKotlin(DocumentoCompletoDAO(documentoBuscadoConyuge)))

        doReturn(personaACargo)
            .`when`(it)
            .buscarSegunParametros(eq(ID_CLIENTE), eqParaKotlin(DocumentoCompletoDAO(documentoBuscadoACargo)))

        doReturn(persona)
            .`when`(it)
            .actualizar(eq(ID_CLIENTE), eq(persona.id!!), eqParaKotlin(persona))

        doReturn(personaConyuge)
            .`when`(it)
            .actualizar(eq(ID_CLIENTE), eq(personaConyuge.id!!), eqParaKotlin(personaConyuge))

        doReturn(personaACargo)
            .`when`(it)
            .actualizar(eq(ID_CLIENTE), eq(personaACargo.id!!), eqParaKotlin(personaACargo))

    }

    private val mockRepositorioRelacionesPersonas = mockConDefaultAnswer(RepositorioRelacionesPersonas::class.java).also {
        doReturn(mockConDefaultAnswer(Unit::class.java))
            .`when`(it)
            .crear(eq(ID_CLIENTE), cualquiera())
    }

    private val mockServicioCafam = mockConDefaultAnswer(InterfazDummyPruebas::class.java).also {
        doReturn(mutableMapOf<String, Any>()).`when`(it).requestContext

        doReturn(afiliadoCafamPorDefecto)
            .`when`(it)
            .consultarInfoAfilNewHotelSync(cualquiera())
    }

    private val mockMapeadorServicioCAFAM = mockConDefaultAnswer(MapeadorServicioCAFAM::class.java).also {
        doReturn(persona)
            .`when`(it)
            .afiliadoCafamAPersona(eq(ID_CLIENTE), cualquiera())

        doReturn(personaACargo)
            .`when`(it)
            .personaACargoCafamAPersona(eq(ID_CLIENTE), cualquiera(), cualquiera())

        doReturn(personaConyuge)
            .`when`(it)
            .conyugeCafamAPersona(eq(ID_CLIENTE), cualquiera(), cualquiera())

        for (tipoDocumento in Persona.TipoDocumento.values())
        {
            doReturn(TIPO_DE_DOCUMENTO_CAFAM_PRUEBAS).`when`(it).convertirADocumentoCAFAM(tipoDocumento)
        }
    }

    private val integracionEnPrueba =
            IntegracionCafamImpl(
                    ID_CLIENTE,
                    mockRepositorioPersonas,
                    mockRepositorioRelacionesPersonas,
                    mockMapeadorServicioCAFAM,
                    mockServicioCafam
                                )


    @Test
    fun al_consultar_la_informacion_de_un_afiliado_se_invoca_el_servicio_de_cafam()
    {
        doReturn(afiliadoCafamPorDefecto)
            .`when`(mockServicioCafam)
            .consultarInfoAfilNewHotelSync(cualquiera())

        val tipoDeDocumento = Persona.TipoDocumento.CC
        doReturn("documento formato cafam")
            .`when`(mockMapeadorServicioCAFAM)
            .convertirADocumentoCAFAM(eqParaKotlin(tipoDeDocumento))

        val documentoAConsultar = DocumentoCompleto(tipoDeDocumento, "010203418857e3jt923ytg23")

        integracionEnPrueba.darInformacionAfiliado(documentoAConsultar)

        verify(mockServicioCafam).consultarInfoAfilNewHotelSync(argThat {
            assertEquals("documento formato cafam", it.tpiden)
            assertEquals(documentoAConsultar.numeroDocumento, it.idtrab)
            true
        })
    }

    @DisplayName("Al consultar la información del afiliado, se invocó el método buscar del repositorio correctamente")
    @ParameterizedTest(name = "Cuando la persona tiene {0}")
    @ArgumentsSource(ProveedorRelacionesAfiliado::class)
    fun al_consultar_la_informacion_de_un_afiliado_se_invoca_el_metodo_buscar(
            testName: String,
            conyuges: List<InfoAfilNewHotel.CONYUGE>,
            personasACargo: List<InfoAfilNewHotel.PERSONASACARGO>
                                                                             )
    {
        doReturn(conyuges)
            .`when`(afiliadoCafamPorDefecto)
            .conyuge

        for (conyuge in conyuges)
        {
            val conyugeComoPersona = mockConDefaultAnswer(Persona::class.java).also {
                doReturn(Persona.TipoDocumento.CC).`when`(it).tipoDocumento
                doReturn(conyuge.idcony).`when`(it).numeroDocumento
                doReturn(it).`when`(it).copiar(
                        idCliente = eq(ID_CLIENTE),
                        id = anyLong(),
                        nombreCompleto = anyString(),
                        tipoDocumento = cualquiera(),
                        numeroDocumento = anyString(),
                        genero = cualquiera(),
                        fechaNacimiento = cualquiera(),
                        categoria = cualquiera(),
                        afiliacion = cualquiera(),
                        esAnonima = anyBoolean(),
                        llaveImagen = anyString()
                                              )
            }
            doReturn(conyugeComoPersona)
                .`when`(mockMapeadorServicioCAFAM)
                .conyugeCafamAPersona(eq(ID_CLIENTE), eqParaKotlin(persona), eqParaKotlin(conyuge))
        }

        doReturn(personasACargo)
            .`when`(afiliadoCafamPorDefecto)
            .personasacargo

        val documentoAConsultar = DocumentoCompleto(TIPO_DE_DOCUMENTO_NEGOCIO_PRUEBAS, DOCUMENTO_PERSONA)

        integracionEnPrueba.darInformacionAfiliado(documentoAConsultar)

        verify(mockRepositorioPersonas).buscarSegunParametros(eq(ID_CLIENTE), eqParaKotlin(DocumentoCompletoDAO(documentoAConsultar)))

        for (conyuge in conyuges)
        {
            val documentoAConsultarConyuge = DocumentoCompleto(TIPO_DE_DOCUMENTO_NEGOCIO_PRUEBAS, conyuge.idcony)
            verify(mockRepositorioPersonas).buscarSegunParametros(eq(ID_CLIENTE), eqParaKotlin(DocumentoCompletoDAO(documentoAConsultarConyuge)))
        }

        if (personasACargo.isNotEmpty())
        {
            val documentoAConsultarACargo = DocumentoCompleto(TIPO_DE_DOCUMENTO_NEGOCIO_PRUEBAS, DOCUMENTO_PERSONA_A_CARGO)
            verify(mockRepositorioPersonas, times(personasACargo.size)).buscarSegunParametros(eq(ID_CLIENTE), eqParaKotlin(DocumentoCompletoDAO(documentoAConsultarACargo))
                                                                                             )
        }
    }

    @DisplayName("Al consultar la información de un afiliado que existe, se invocó el método actualizar")
    @Test
    fun al_consultar_la_informacion_de_un_afiliado_se_invoca_el_metodo_actualizar()
    {

        doReturn(listOf<InfoAfilNewHotel.CONYUGE>())
            .`when`(afiliadoCafamPorDefecto)
            .conyuge

        doReturn(listOf<InfoAfilNewHotel.PERSONASACARGO>())
            .`when`(afiliadoCafamPorDefecto)
            .personasacargo

        doReturn(afiliadoCafamPorDefecto)
            .`when`(mockServicioCafam)
            .consultarInfoAfilNewHotelSync(cualquiera())

        doReturn(persona)
            .`when`(mockRepositorioPersonas)
            .buscarSegunParametros(eq(ID_CLIENTE), cualquiera())

        doReturn(persona)
            .`when`(mockRepositorioPersonas)
            .actualizar(eq(ID_CLIENTE), anyLong(), cualquiera())

        doReturn(persona)
            .`when`(mockMapeadorServicioCAFAM)
            .afiliadoCafamAPersona(eq(ID_CLIENTE), cualquiera())

        doReturn(afiliadoCafamPorDefecto)
            .`when`(mockServicioCafam)
            .consultarInfoAfilNewHotelSync(cualquiera())

        val documentoAConsultar = DocumentoCompleto(TIPO_DE_DOCUMENTO_NEGOCIO_PRUEBAS, DOCUMENTO_PERSONA)

        integracionEnPrueba.darInformacionAfiliado(documentoAConsultar)

        verify(mockRepositorioPersonas, times(1 + afiliadoCafamPorDefecto.conyuge.size + afiliadoCafamPorDefecto.personasacargo.size))
            .actualizar(eq(1L), eq(12L), eqParaKotlin(persona))
    }

    @DisplayName("Al consultar la información de un afiliado que no existe, se invocó el método crear")
    @Test
    fun al_consultar_la_informacion_de_un_afiliado_se_invoca_el_metodo_crear()
    {
        val documentoAConsultar = DocumentoCompleto(TIPO_DE_DOCUMENTO_NEGOCIO_PRUEBAS, DOCUMENTO_PERSONA)

        doReturn(null)
            .`when`(mockRepositorioPersonas)
            .buscarSegunParametros(eq(ID_CLIENTE), cualquiera())


        doReturn(persona)
            .`when`(mockRepositorioPersonas)
            .crear(eq(ID_CLIENTE), eqParaKotlin(persona))

        doReturn(personaACargo)
            .`when`(mockRepositorioPersonas)
            .crear(eq(ID_CLIENTE), eqParaKotlin(personaACargo))

        doReturn(personaConyuge)
            .`when`(mockRepositorioPersonas)
            .crear(eq(ID_CLIENTE), eqParaKotlin(personaConyuge))

        doReturn(afiliadoCafamPorDefecto)
            .`when`(mockServicioCafam)
            .consultarInfoAfilNewHotelSync(cualquiera())


        integracionEnPrueba.darInformacionAfiliado(documentoAConsultar)
        verify(mockRepositorioPersonas, times(1))
            .crear(eq(1L), eqParaKotlin(persona))
        verify(mockRepositorioPersonas, times(1))
            .crear(eq(1L), eqParaKotlin(personaConyuge))
        verify(mockRepositorioPersonas, times(1))
            .crear(eq(1L), eqParaKotlin(personaACargo))
    }

    @DisplayName("Al consultar la información de un afiliado, se invoca el método del repositorio de relaciones para crearlas")
    @Test
    fun al_consultar_la_informacion_de_un_afiliado_se_invoca_el_metodo_crear_relaciones()
    {
        val documentoAConsultar = DocumentoCompleto(TIPO_DE_DOCUMENTO_NEGOCIO_PRUEBAS, DOCUMENTO_PERSONA)
        doReturn(afiliadoCafamPorDefecto)
            .`when`(mockServicioCafam)
            .consultarInfoAfilNewHotelSync(cualquiera())
        integracionEnPrueba.darInformacionAfiliado(documentoAConsultar)
        verify(mockRepositorioRelacionesPersonas).crear(eq(ID_CLIENTE), eqParaKotlin(PersonaConFamiliares(persona, setOf(personaConyuge, personaACargo))))
    }
}