package co.smartobjects.integraciones.cafam

import co.smartobjects.entidades.personas.Persona
import dom.cafam.newhotel.integracionerp_subsidio.InfoAfilNewHotel
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.threeten.bp.LocalDate
import java.math.BigInteger
import java.util.stream.Stream
import javax.xml.datatype.DatatypeFactory
import kotlin.test.assertEquals


internal class MapeadorServicioCAFAMPruebas
{
    companion object
    {
        val mapeador = MapeadorServicioCAFAMImpl()
    }

    internal class ProveedorTiposDeDocumentoEsperados : ArgumentsProvider
    {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments>
        {
            return Stream.of(
                    Arguments.of(Persona.TipoDocumento.CC, "1"),
                    Arguments.of(Persona.TipoDocumento.PA, "2"),
                    Arguments.of(Persona.TipoDocumento.RC, "4"),
                    Arguments.of(Persona.TipoDocumento.CE, "6"),
                    Arguments.of(Persona.TipoDocumento.TI, "7"),
                    Arguments.of(Persona.TipoDocumento.CD, "1"),
                    Arguments.of(Persona.TipoDocumento.NIT, "1"),
                    Arguments.of(Persona.TipoDocumento.NUIP, "1")
            )
        }
    }

    internal class ProveedorNITs : ArgumentsProvider
    {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments>
        {
            return Stream.of(

                    Arguments.of("800141644-1", "2", Persona.Categoria.A),
                    Arguments.of("800141644-1", "3", Persona.Categoria.A),
                    Arguments.of("800141644-1", "22", Persona.Categoria.A),
                    Arguments.of("800141644", "2", Persona.Categoria.A),
                    Arguments.of("800141644", "3", Persona.Categoria.A),
                    Arguments.of("800141644", "22", Persona.Categoria.A),
                    Arguments.of("899999102-2", "2", Persona.Categoria.A),
                    Arguments.of("899999102-2", "3", Persona.Categoria.A),
                    Arguments.of("899999102-2", "22", Persona.Categoria.A),
                    Arguments.of("899999102", "2", Persona.Categoria.A),
                    Arguments.of("899999102", "3", Persona.Categoria.A),
                    Arguments.of("899999102", "22", Persona.Categoria.A),
                    Arguments.of("123", "2", Persona.Categoria.A),
                    Arguments.of("123", "3", Persona.Categoria.B),
                    Arguments.of("123", "22", Persona.Categoria.C),
                    Arguments.of("12312-1", "2", Persona.Categoria.A),
                    Arguments.of("12312-1", "3", Persona.Categoria.B),
                    Arguments.of("12312-1", "22", Persona.Categoria.C),
                    Arguments.of("56452312", "2", Persona.Categoria.A),
                    Arguments.of("56452312", "3", Persona.Categoria.B),
                    Arguments.of("56452312", "22", Persona.Categoria.C),
                    Arguments.of("43324234", "2", Persona.Categoria.A),
                    Arguments.of("43324234", "3", Persona.Categoria.B),
                    Arguments.of("43324234", "22", Persona.Categoria.C),
                    Arguments.of(null, "2", Persona.Categoria.A),
                    Arguments.of(null, "3", Persona.Categoria.B),
                    Arguments.of(null, "22", Persona.Categoria.C)
            )
        }
    }

    internal class ProveedorDocumentosCategorias : ArgumentsProvider
    {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments>
        {
            return Stream.of(
                    Arguments.of("12343","1",Persona.Categoria.A),
                    Arguments.of("234234","2",Persona.Categoria.A),
                    Arguments.of("3423423","4",Persona.Categoria.A),
                    Arguments.of("3453453","6",Persona.Categoria.A),
                    Arguments.of("453453","7",Persona.Categoria.A),
                    Arguments.of("234234","1",Persona.Categoria.A),
                    Arguments.of("546456","1",Persona.Categoria.A),
                    Arguments.of("234234","1",Persona.Categoria.A),
                    Arguments.of("213123","1",Persona.Categoria.B),
                    Arguments.of("345345","2",Persona.Categoria.B),
                    Arguments.of("456456","4",Persona.Categoria.B),
                    Arguments.of("234234","6",Persona.Categoria.B),
                    Arguments.of("123123","7",Persona.Categoria.B),
                    Arguments.of("234234","1",Persona.Categoria.B),
                    Arguments.of("234234","1",Persona.Categoria.B),
                    Arguments.of("123123","1",Persona.Categoria.B),
                    Arguments.of("534534","1",Persona.Categoria.C),
                    Arguments.of("23423","2",Persona.Categoria.C),
                    Arguments.of("123123","4",Persona.Categoria.C),
                    Arguments.of("435435","6",Persona.Categoria.C),
                    Arguments.of("32424","7",Persona.Categoria.C),
                    Arguments.of("34534","1",Persona.Categoria.C),
                    Arguments.of("234234","1",Persona.Categoria.C),
                    Arguments.of("345345","1",Persona.Categoria.C)
            )
        }
    }


    @ParameterizedTest(name = "Cuando el nit es ''{0}'' y la categoria ''{1}''")
    @ArgumentsSource(ProveedorNITs::class)
    fun prueba_calculo_categorias(nit: String?, categoriaCafam : String, categoria : Persona.Categoria)
    {
        val infoAfiliado = InfoAfilNewHotel()
        val empresa = InfoAfilNewHotel.EMPRESA()
        empresa.idempr = nit
        infoAfiliado.empresa.add(empresa)
        infoAfiliado.ticlcodi = BigInteger(categoriaCafam)
        infoAfiliado.clienome = "primer-nombre segundo-nombre primer-apellido segundo-apellido"
        infoAfiliado.clieapel = "no importa"
        infoAfiliado.clienuid = "11232"
        infoAfiliado.clietiid = "1"
        infoAfiliado.cliesexo = "1"
        infoAfiliado.clieesta = "VIGEN"
        infoAfiliado.cliedana = DatatypeFactory.newInstance().newXMLGregorianCalendarDate(1990, 12, 1, 0)
        val valor = mapeador.afiliadoCafamAPersona(1, infoAfiliado)
        assertEquals(valor.categoria, categoria)
        assertEquals("primer-nombre segundo-nombre primer-apellido segundo-apellido", valor.nombreCompleto)
        assertEquals("11232", valor.numeroDocumento)
    }
    @ParameterizedTest(name = "Cuando el afiliado no está VIGEN")
    @ArgumentsSource(ProveedorNITs::class)
    fun prueba_calculo_categorias_no_vigen(nit: String?, categoriaCafam : String, categoria : Persona.Categoria)
    {
        val infoAfiliado = InfoAfilNewHotel()
        val empresa = InfoAfilNewHotel.EMPRESA()
        empresa.idempr = nit
        infoAfiliado.empresa.add(empresa)
        infoAfiliado.ticlcodi = BigInteger(categoriaCafam)
        infoAfiliado.clienome = "primer-nombre segundo-nombre primer-apellido segundo-apellido"
        infoAfiliado.clieapel = "no importa"
        infoAfiliado.clienuid = "11232"
        infoAfiliado.clietiid = "1"
        infoAfiliado.cliesexo = "1"
        infoAfiliado.clieesta = "NOVIGEN"
        infoAfiliado.cliedana = DatatypeFactory.newInstance().newXMLGregorianCalendarDate(1990, 12, 1, 0)
        val valor = mapeador.afiliadoCafamAPersona(1, infoAfiliado)
        assertEquals(valor.categoria, Persona.Categoria.D)
    }

    @ParameterizedTest(name = "Cuando el tipo de documento es ''{0}'' y la categoria ''{1}''")
    @ArgumentsSource(ProveedorDocumentosCategorias::class)
    fun prueba_creacionPresonaACargo(documento : String, tipoDocumento : String, categoria: Persona.Categoria)
    {
        val persona = Persona(1, 1, "stdyg", Persona.TipoDocumento.CC, "4356546", Persona.Genero.FEMENINO, LocalDate.now(), categoria, Persona.Afiliacion.COTIZANTE, "","empresa","0",Persona.Tipo.NO_AFILIADO)
        val personaACargo = InfoAfilNewHotel.PERSONASACARGO()
        personaACargo.tidpac = tipoDocumento
        personaACargo.idpac = documento
        personaACargo.nompac = "Andres Prueba"
        personaACargo.parpac = "53425"
        personaACargo.sexopac = "1"
        personaACargo.danapac = DatatypeFactory.newInstance().newXMLGregorianCalendarDate(1990, 12, 1, 0)
        val valor = mapeador.personaACargoCafamAPersona(1, persona, personaACargo)
        assertEquals(valor.categoria, categoria)
        assertEquals(valor.afiliacion, Persona.Afiliacion.BENEFICIARIO)
        assertEquals(documento, valor.numeroDocumento)
        assertEquals("Andres Prueba", valor.nombreCompleto)
        assertEquals(Persona.Genero.MASCULINO, valor.genero)
        assertEquals(LocalDate.of(1990,12,1), valor.fechaNacimiento)
    }

    @ParameterizedTest(name = "Cuando el tipo de documento es ''{0}'' y la categoria ''{1}''")
    @ArgumentsSource(ProveedorDocumentosCategorias::class)
    fun prueba_cracionConyugeCafamAPersona(documento : String, tipoDocumento : String, categoria: Persona.Categoria)
    {
        val persona = Persona(1, 1, "stdyg", Persona.TipoDocumento.CC, "4356546", Persona.Genero.FEMENINO, LocalDate.now(), categoria, Persona.Afiliacion.COTIZANTE, "", "empresa","0",Persona.Tipo.NO_AFILIADO)
        val personaACargo = InfoAfilNewHotel.CONYUGE()
        personaACargo.tidcony = tipoDocumento
        personaACargo.idcony = documento
        personaACargo.nomcony = "Andres Prueba"
        personaACargo.estconv = "53425"
        personaACargo.sexocony = "1"
        personaACargo.danacony = DatatypeFactory.newInstance().newXMLGregorianCalendarDate(1990, 12, 1, 0)

        val valor = mapeador.conyugeCafamAPersona(1, persona, personaACargo)
        assertEquals(valor.categoria, categoria)
        assertEquals(valor.afiliacion, Persona.Afiliacion.BENEFICIARIO)
        assertEquals(valor.numeroDocumento, documento)
        assertEquals(valor.nombreCompleto, "Andres Prueba")
        assertEquals(Persona.Genero.MASCULINO, valor.genero)
        assertEquals(LocalDate.of(1990,12,1), valor.fechaNacimiento)
    }

    @DisplayName("Verificar que se mapean bien los tipos de documento")
    @ParameterizedTest(name = "Cuando el tipo de documento es ''{0}''")
    @ArgumentsSource(ProveedorTiposDeDocumentoEsperados::class)
    fun prueba_convertirADocumentoCAFAM(tipoDoc: Persona.TipoDocumento, esperado : String)
    {
        val valor = mapeador.convertirADocumentoCAFAM(tipoDoc)
        assertEquals(valor, esperado)
    }

    @DisplayName("El afiliado de CAFAM se parsea correctamente")
    @Test
    fun prueba_afiliadoCafamAPersona_perseo_correcto(){
        val infoAfiliado = InfoAfilNewHotel()
        val empresa = InfoAfilNewHotel.EMPRESA()
        empresa.idempr = "123454421434"
        infoAfiliado.empresa.add(empresa)
        infoAfiliado.ticlcodi = BigInteger("1")
        infoAfiliado.clienome = "Andres Prueba"
        infoAfiliado.clieapel = "Test Apellido No Importa"
        infoAfiliado.clienuid = "11232"
        infoAfiliado.clietiid = "1"
        infoAfiliado.cliesexo = "1"
        infoAfiliado.cliedana = DatatypeFactory.newInstance().newXMLGregorianCalendarDate(1990, 12, 1, 0)
        val valor = mapeador.afiliadoCafamAPersona(1, infoAfiliado)
        assertEquals("Andres Prueba", valor.nombreCompleto)
        assertEquals("11232", valor.numeroDocumento)
        assertEquals(Persona.Genero.MASCULINO, valor.genero)
        assertEquals(Persona.TipoDocumento.CC, valor.tipoDocumento)
        assertEquals(LocalDate.of(1990,12,1), valor.fechaNacimiento)

    }
}