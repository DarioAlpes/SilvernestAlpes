package co.smartobjects.integraciones.cafam

import co.smartobjects.entidades.personas.Persona
import dom.cafam.newhotel.integracionerp_subsidio.InfoAfilNewHotel
import org.threeten.bp.LocalDate


interface MapeadorServicioCAFAM
{
    fun afiliadoCafamAPersona(idCliente: Long, resp: InfoAfilNewHotel): Persona

    fun personaACargoCafamAPersona(
            idCliente: Long,
            afiliado: Persona,
            personaACargo: InfoAfilNewHotel.PERSONASACARGO
                                  ): Persona

    fun conyugeCafamAPersona(
            idCliente: Long,
            afiliado: Persona,
            conyuge: InfoAfilNewHotel.CONYUGE
                            ): Persona

    fun convertirADocumentoCAFAM(tipoDoc: Persona.TipoDocumento): String
}

internal class MapeadorServicioCAFAMImpl
    : MapeadorServicioCAFAM
{
    override fun afiliadoCafamAPersona(idCliente: Long, resp: InfoAfilNewHotel): Persona
    {
        val tipoDocumento = convertirDesdeDocumentoCAFAM(resp.clietiid)

        val genero =
                when
                {
                    resp.cliesexo == "1" -> Persona.Genero.MASCULINO
                    resp.cliesexo == "0" -> Persona.Genero.FEMENINO
                    else                 -> Persona.Genero.DESCONOCIDO
                }

        val tipo =
                when
                {
                    resp.cliE_TTRA =="Trabajador" -> Persona.Tipo.TRABAJADOR
                    resp.cliE_TTRA =="Facultativo" -> Persona.Tipo.FACULTATIVO
                    resp.cliE_TTRA =="Independiente" -> Persona.Tipo.INDEPENDIENTE
                    resp.cliE_TTRA =="Pensionado" -> Persona.Tipo.PENSIONADO
                    resp.cliE_TTRA =="Afiliado 25 años" -> Persona.Tipo.AFILIADO_25_AÑOS
                    resp.cliE_TTRA =="Cortesia" -> Persona.Tipo.CORTESIA
                    else                        -> Persona.Tipo.NO_AFILIADO
                }
        val nombreEmpresa =obtenerNombreEmpresa(resp)
        val nitEmpresa =obtenerNitEmpresa(resp)

        val fechaNacimiento = LocalDate.of(resp.cliedana.year, resp.cliedana.month, resp.cliedana.day)

        val categoria = calcularCategoriaCafam(resp)
        val afiliacion = calcularAfiliacion(resp)

        return Persona(
                idCliente,
                null,
                resp.clienome,
                tipoDocumento,
                resp.clienuid,
                genero,
                fechaNacimiento,
                categoria,
                afiliacion,
                false,
                null,
                nombreEmpresa,
                nitEmpresa,
                tipo
                      )
    }

    private fun obtenerNitEmpresa(resp: InfoAfilNewHotel): String
    {
            if (resp.empresa != null && resp.empresa.size > 0)
            {
                for (empresa in resp.empresa)
                {
                    if (empresa.idempr != null)
                    {
                        return empresa.idempr
                    }else{
                        return "0"
                    }
                }
            }
        return "0"
    }
    private fun obtenerNombreEmpresa(resp: InfoAfilNewHotel): String
    {
        if (resp.empresa != null && resp.empresa.size > 0)
        {
            for (empresa in resp.empresa)
            {
                if (empresa.enticodi != null && empresa.enticodi != " ")
                {
                    return empresa.enticodi
                }else{
                    return "n/a"
                }
            }
        }
        return "n/a"
    }

    private fun calcularCategoriaCafam(resp: InfoAfilNewHotel): Persona.Categoria
    {
        if ("VIGEN" == resp.clieesta)
        {
            if (resp.empresa != null && resp.empresa.size > 0)
            {
                for (empresa in resp.empresa)
                {
                    if (empresa.idempr != null)
                    {
                        when (empresa.idempr)
                        {
                            "800141644-1", "800141644", "899999102-2", "899999102" ->
                            {
                                return Persona.Categoria.A
                            }
                        }
                    }
                }
            }
            return when
            {
                resp.ticlcodi == null            -> Persona.Categoria.D
                resp.ticlcodi.toString() == "2"  -> Persona.Categoria.A
                resp.ticlcodi.toString() == "3"  -> Persona.Categoria.B
                resp.ticlcodi.toString() == "22" -> Persona.Categoria.C
                else                             -> Persona.Categoria.D
            }
        }

        return Persona.Categoria.D
    }

    private fun calcularAfiliacion(resp: InfoAfilNewHotel): Persona.Afiliacion
    {
        return if ("VIGEN" == resp.clieesta) Persona.Afiliacion.COTIZANTE else Persona.Afiliacion.NO_AFILIADO
    }

    override fun personaACargoCafamAPersona(
            idCliente: Long,
            afiliado: Persona,
            personaACargo: InfoAfilNewHotel.PERSONASACARGO
                                           ): Persona
    {
        val tipoDocumentoConyuge = convertirDesdeDocumentoCAFAM(personaACargo.tidpac)

        val fechaNacimiento = LocalDate.of(personaACargo.danapac.year, personaACargo.danapac.month, personaACargo.danapac.day)

        val genero =
                when
                {
                    personaACargo.sexopac == "1" -> Persona.Genero.MASCULINO
                    personaACargo.sexopac == "0" -> Persona.Genero.FEMENINO
                    else                         -> Persona.Genero.DESCONOCIDO
                }

        return Persona(
                idCliente,
                null,
                personaACargo.nompac,
                tipoDocumentoConyuge,
                personaACargo.idpac,
                genero,
                fechaNacimiento,
                afiliado.categoria,
                Persona.Afiliacion.BENEFICIARIO,
                false,
                "",
                "n/a",
                "0",
                afiliado.tipo
                      )
    }

    override fun conyugeCafamAPersona(
            idCliente: Long,
            afiliado: Persona,
            conyuge: InfoAfilNewHotel.CONYUGE
                                     ): Persona
    {
        val tipoDocumentoConyuge = convertirDesdeDocumentoCAFAM(conyuge.tidcony)

        var fechaNacimiento = LocalDate.MIN
        if(conyuge.danacony != null)
        {
            fechaNacimiento = LocalDate.of(conyuge.danacony.year, conyuge.danacony.month, conyuge.danacony.day)
        }

        val genero =
                when
                {
                    conyuge.sexocony == "1" -> Persona.Genero.MASCULINO
                    conyuge.sexocony == "0" -> Persona.Genero.FEMENINO
                    else                    -> Persona.Genero.DESCONOCIDO
                }


        return Persona(
                idCliente,
                null,
                conyuge.nomcony,
                tipoDocumentoConyuge,
                conyuge.idcony,
                genero,
                fechaNacimiento,
                afiliado.categoria,
                Persona.Afiliacion.BENEFICIARIO,
                false,
                "",
                "n/a",
                "0",
                afiliado.tipo
                      )
    }

    private fun convertirDesdeDocumentoCAFAM(tipoDoc: String): Persona.TipoDocumento
    {
        return when (tipoDoc)
        {
            "1"  -> Persona.TipoDocumento.CC
            "2"  -> Persona.TipoDocumento.PA
            "3"  -> Persona.TipoDocumento.RC
            "6"  -> Persona.TipoDocumento.CE
            "7"  -> Persona.TipoDocumento.TI
            else -> Persona.TipoDocumento.CC
        }
    }

    override fun convertirADocumentoCAFAM(tipoDoc: Persona.TipoDocumento): String
    {
        return when (tipoDoc)
        {
            Persona.TipoDocumento.CC -> "1"
            Persona.TipoDocumento.PA -> "2"
            Persona.TipoDocumento.RC -> "4"
            Persona.TipoDocumento.CE -> "6"
            Persona.TipoDocumento.TI -> "7"
            else                     -> "1"
        }
    }
}