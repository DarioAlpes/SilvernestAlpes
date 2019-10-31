
package dom.cafam.newhotel.integracionerp_subsidio;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * Consulta Información Afiliado para New Hotel
 * 
 * <p>Java class for InfoAfil_NewHotel complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="InfoAfil_NewHotel"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="CLIE_TIID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="CLIE_NUID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="CLIE_NOME" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="CLIE_APEL" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="CLIE_APE2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="CLIE_ESCI" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="CLIE_SEXO" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="NACI_CODI" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="CLIE_LONA" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="CLIE_DANA" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/&gt;
 *         &lt;element name="CLIE_ESTA" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="TICL_CODI" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/&gt;
 *         &lt;element name="CLIE_TELE" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="CLIE_NFAX" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="CLIE_NUMO" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="CLIE_MAIL" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="CLIE_MORA" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="CLIE_LOCL" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="PROV_CODI" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="CLIE_PROF" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="CONYUGE" maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="TID_CONY" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="ID_CONY" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="NOM_CONY" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="EST_CONV" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="PERSONAS_A_CARGO" maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="TID_PAC" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="ID_PAC" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="NOM_PAC" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="PAR_PAC" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="EMPRESA" maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="ETIPEMP" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="IDEMPR" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="ENTI_CODI" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="DIR_AFIL" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="CLIE_LOEM" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="TEL_EMP" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="FAX_EMP" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InfoAfil_NewHotel", propOrder = {
    "clietiid",
    "clienuid",
    "clienome",
    "clieapel",
    "clieape2",
    "clieesci",
    "cliesexo",
    "nacicodi",
    "clielona",
    "cliedana",
    "clieesta",
    "ticlcodi",
    "clietele",
    "clienfax",
    "clienumo",
    "cliemail",
    "cliemora",
    "clielocl",
    "provcodi",
    "clieprof",
    "conyuge",
    "personasacargo",
    "empresa",
    "ttra",
})
public class InfoAfilNewHotel {

    @XmlElement(name = "CLIE_TIID")
    protected String clietiid;
    @XmlElement(name = "CLIE_NUID")
    protected String clienuid;
    @XmlElement(name = "CLIE_NOME")
    protected String clienome;
    @XmlElement(name = "CLIE_APEL")
    protected String clieapel;
    @XmlElement(name = "CLIE_APE2")
    protected String clieape2;
    @XmlElement(name = "CLIE_ESCI")
    protected String clieesci;
    @XmlElement(name = "CLIE_SEXO")
    protected String cliesexo;
    @XmlElement(name = "NACI_CODI")
    protected String nacicodi;
    @XmlElement(name = "CLIE_LONA")
    protected String clielona;
    @XmlElement(name = "CLIE_DANA")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar cliedana;
    @XmlElement(name = "CLIE_ESTA")
    protected String clieesta;
    @XmlElement(name = "TICL_CODI")
    protected BigInteger ticlcodi;
    @XmlElement(name = "CLIE_TELE")
    protected String clietele;
    @XmlElement(name = "CLIE_NFAX")
    protected String clienfax;
    @XmlElement(name = "CLIE_NUMO")
    protected String clienumo;
    @XmlElement(name = "CLIE_MAIL")
    protected String cliemail;
    @XmlElement(name = "CLIE_MORA")
    protected String cliemora;
    @XmlElement(name = "CLIE_LOCL")
    protected String clielocl;
    @XmlElement(name = "PROV_CODI")
    protected String provcodi;
    @XmlElement(name = "CLIE_PROF")
    protected String clieprof;
    @XmlElement(name = "CONYUGE")
    protected List<CONYUGE> conyuge;
    @XmlElement(name = "PERSONAS_A_CARGO")
    protected List<PERSONASACARGO> personasacargo;
    @XmlElement(name = "EMPRESA")
    protected List<EMPRESA> empresa;
    @XmlElement(name = "CLIE_TTRA")
    protected String ttra;

    /**
     * Gets the value of the ttra property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */

    public String getCLIE_TTRA() {
        return ttra;
    }

    /**
     * Sets the value of the ttra property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCLIE_TTRA(String value) {
        this.ttra = value;
    }

    /**
     * Gets the value of the clietiid property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCLIETIID() {
        return clietiid;
    }

    /**
     * Sets the value of the clietiid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCLIETIID(String value) {
        this.clietiid = value;
    }

    /**
     * Gets the value of the clienuid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCLIENUID() {
        return clienuid;
    }

    /**
     * Sets the value of the clienuid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCLIENUID(String value) {
        this.clienuid = value;
    }

    /**
     * Gets the value of the clienome property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCLIENOME() {
        return clienome;
    }

    /**
     * Sets the value of the clienome property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCLIENOME(String value) {
        this.clienome = value;
    }

    /**
     * Gets the value of the clieapel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCLIEAPEL() {
        return clieapel;
    }

    /**
     * Sets the value of the clieapel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCLIEAPEL(String value) {
        this.clieapel = value;
    }

    /**
     * Gets the value of the clieape2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCLIEAPE2() {
        return clieape2;
    }

    /**
     * Sets the value of the clieape2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCLIEAPE2(String value) {
        this.clieape2 = value;
    }

    /**
     * Gets the value of the clieesci property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCLIEESCI() {
        return clieesci;
    }

    /**
     * Sets the value of the clieesci property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCLIEESCI(String value) {
        this.clieesci = value;
    }

    /**
     * Gets the value of the cliesexo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCLIESEXO() {
        return cliesexo;
    }

    /**
     * Sets the value of the cliesexo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCLIESEXO(String value) {
        this.cliesexo = value;
    }

    /**
     * Gets the value of the nacicodi property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNACICODI() {
        return nacicodi;
    }

    /**
     * Sets the value of the nacicodi property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNACICODI(String value) {
        this.nacicodi = value;
    }

    /**
     * Gets the value of the clielona property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCLIELONA() {
        return clielona;
    }

    /**
     * Sets the value of the clielona property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCLIELONA(String value) {
        this.clielona = value;
    }

    /**
     * Gets the value of the cliedana property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCLIEDANA() {
        return cliedana;
    }

    /**
     * Sets the value of the cliedana property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCLIEDANA(XMLGregorianCalendar value) {
        this.cliedana = value;
    }

    /**
     * Gets the value of the clieesta property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCLIEESTA() {
        return clieesta;
    }

    /**
     * Sets the value of the clieesta property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCLIEESTA(String value) {
        this.clieesta = value;
    }

    /**
     * Gets the value of the ticlcodi property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTICLCODI() {
        return ticlcodi;
    }

    /**
     * Sets the value of the ticlcodi property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTICLCODI(BigInteger value) {
        this.ticlcodi = value;
    }

    /**
     * Gets the value of the clietele property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCLIETELE() {
        return clietele;
    }

    /**
     * Sets the value of the clietele property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCLIETELE(String value) {
        this.clietele = value;
    }

    /**
     * Gets the value of the clienfax property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCLIENFAX() {
        return clienfax;
    }

    /**
     * Sets the value of the clienfax property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCLIENFAX(String value) {
        this.clienfax = value;
    }

    /**
     * Gets the value of the clienumo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCLIENUMO() {
        return clienumo;
    }

    /**
     * Sets the value of the clienumo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCLIENUMO(String value) {
        this.clienumo = value;
    }

    /**
     * Gets the value of the cliemail property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCLIEMAIL() {
        return cliemail;
    }

    /**
     * Sets the value of the cliemail property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCLIEMAIL(String value) {
        this.cliemail = value;
    }

    /**
     * Gets the value of the cliemora property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCLIEMORA() {
        return cliemora;
    }

    /**
     * Sets the value of the cliemora property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCLIEMORA(String value) {
        this.cliemora = value;
    }

    /**
     * Gets the value of the clielocl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCLIELOCL() {
        return clielocl;
    }

    /**
     * Sets the value of the clielocl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCLIELOCL(String value) {
        this.clielocl = value;
    }

    /**
     * Gets the value of the provcodi property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPROVCODI() {
        return provcodi;
    }

    /**
     * Sets the value of the provcodi property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPROVCODI(String value) {
        this.provcodi = value;
    }

    /**
     * Gets the value of the clieprof property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCLIEPROF() {
        return clieprof;
    }

    /**
     * Sets the value of the clieprof property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCLIEPROF(String value) {
        this.clieprof = value;
    }

    /**
     * Gets the value of the conyuge property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the conyuge property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCONYUGE().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CONYUGE }
     * 
     * 
     */
    public List<CONYUGE> getCONYUGE() {
        if (conyuge == null) {
            conyuge = new ArrayList<CONYUGE>();
        }
        return this.conyuge;
    }

    /**
     * Gets the value of the personasacargo property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the personasacargo property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPERSONASACARGO().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PERSONASACARGO }
     * 
     * 
     */
    public List<PERSONASACARGO> getPERSONASACARGO() {
        if (personasacargo == null) {
            personasacargo = new ArrayList<PERSONASACARGO>();
        }
        return this.personasacargo;
    }

    /**
     * Gets the value of the empresa property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the empresa property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEMPRESA().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EMPRESA }
     * 
     * 
     */
    public List<EMPRESA> getEMPRESA() {
        if (empresa == null) {
            empresa = new ArrayList<EMPRESA>();
        }
        return this.empresa;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="TID_CONY" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="ID_CONY" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="NOM_CONY" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="EST_CONV" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "tidcony",
        "idcony",
        "nomcony",
        "estconv",
        "danacony",
        "sexocony"
    })
    public static class CONYUGE {

        @XmlElement(name = "TID_CONY")
        protected String tidcony;
        @XmlElement(name = "ID_CONY")
        protected String idcony;
        @XmlElement(name = "NOM_CONY")
        protected String nomcony;
        @XmlElement(name = "EST_CONV")
        protected String estconv;
        @XmlElement(name = "SEXO_CONY")
        protected String sexocony;
        @XmlElement(name = "DANA_CONY")
        @XmlSchemaType(name = "date")
        protected XMLGregorianCalendar danacony;

        /**
         * Gets the value of the tidcony property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTIDCONY() {
            return tidcony;
        }

        /**
         * Sets the value of the tidcony property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTIDCONY(String value) {
            this.tidcony = value;
        }

        /**
         * Gets the value of the idcony property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getIDCONY() {
            return idcony;
        }

        /**
         * Sets the value of the idcony property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setIDCONY(String value) {
            this.idcony = value;
        }

        /**
         * Gets the value of the nomcony property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getNOMCONY() {
            return nomcony;
        }

        /**
         * Sets the value of the nomcony property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setNOMCONY(String value) {
            this.nomcony = value;
        }

        /**
         * Gets the value of the estconv property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getESTCONV() {
            return estconv;
        }

        /**
         * Sets the value of the estconv property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setESTCONV(String value) {
            this.estconv = value;
        }

        public XMLGregorianCalendar getDANACONY() {
            return danacony;
        }

        public void setDANACONY(XMLGregorianCalendar danacony) {
            this.danacony = danacony;
        }

        public String getSEXOCONY() {
            return sexocony;
        }

        public void setSEXOCONY(String sexocony) {
            this.sexocony = sexocony;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="ETIPEMP" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="IDEMPR" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="ENTI_CODI" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="DIR_AFIL" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="CLIE_LOEM" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="TEL_EMP" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="FAX_EMP" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "etipemp",
        "idempr",
        "enticodi",
        "dirafil",
        "clieloem",
        "telemp",
        "faxemp"
    })
    public static class EMPRESA {

        @XmlElement(name = "ETIPEMP")
        protected String etipemp;
        @XmlElement(name = "IDEMPR")
        protected String idempr;
        @XmlElement(name = "ENTI_CODI")
        protected String enticodi;
        @XmlElement(name = "DIR_AFIL")
        protected String dirafil;
        @XmlElement(name = "CLIE_LOEM")
        protected String clieloem;
        @XmlElement(name = "TEL_EMP")
        protected String telemp;
        @XmlElement(name = "FAX_EMP")
        protected String faxemp;

        /**
         * Gets the value of the etipemp property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getETIPEMP() {
            return etipemp;
        }

        /**
         * Sets the value of the etipemp property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setETIPEMP(String value) {
            this.etipemp = value;
        }

        /**
         * Gets the value of the idempr property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getIDEMPR() {
            return idempr;
        }

        /**
         * Sets the value of the idempr property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setIDEMPR(String value) {
            this.idempr = value;
        }

        /**
         * Gets the value of the enticodi property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getENTICODI() {
            return enticodi;
        }

        /**
         * Sets the value of the enticodi property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setENTICODI(String value) {
            this.enticodi = value;
        }

        /**
         * Gets the value of the dirafil property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getDIRAFIL() {
            return dirafil;
        }

        /**
         * Sets the value of the dirafil property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDIRAFIL(String value) {
            this.dirafil = value;
        }

        /**
         * Gets the value of the clieloem property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getCLIELOEM() {
            return clieloem;
        }

        /**
         * Sets the value of the clieloem property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setCLIELOEM(String value) {
            this.clieloem = value;
        }

        /**
         * Gets the value of the telemp property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTELEMP() {
            return telemp;
        }

        /**
         * Sets the value of the telemp property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTELEMP(String value) {
            this.telemp = value;
        }

        /**
         * Gets the value of the faxemp property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getFAXEMP() {
            return faxemp;
        }

        /**
         * Sets the value of the faxemp property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setFAXEMP(String value) {
            this.faxemp = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="TID_PAC" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="ID_PAC" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="NOM_PAC" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="PAR_PAC" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "tidpac",
        "idpac",
        "nompac",
        "parpac",
        "danapac",
        "sexopac"
    })
    public static class PERSONASACARGO {

        @XmlElement(name = "TID_PAC")
        protected String tidpac;
        @XmlElement(name = "ID_PAC")
        protected String idpac;
        @XmlElement(name = "NOM_PAC")
        protected String nompac;
        @XmlElement(name = "PAR_PAC")
        protected String parpac;
        @XmlElement(name = "SEXO_PAC")
        protected String sexopac;
        @XmlElement(name = "DANA_PAC")
        @XmlSchemaType(name = "date")
        protected XMLGregorianCalendar danapac;


        /**
         * Gets the value of the tidpac property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTIDPAC() {
            return tidpac;
        }

        /**
         * Sets the value of the tidpac property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTIDPAC(String value) {
            this.tidpac = value;
        }

        /**
         * Gets the value of the idpac property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getIDPAC() {
            return idpac;
        }

        /**
         * Sets the value of the idpac property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setIDPAC(String value) {
            this.idpac = value;
        }

        /**
         * Gets the value of the nompac property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getNOMPAC() {
            return nompac;
        }

        /**
         * Sets the value of the nompac property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setNOMPAC(String value) {
            this.nompac = value;
        }

        /**
         * Gets the value of the parpac property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getPARPAC() {
            return parpac;
        }

        /**
         * Sets the value of the parpac property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setPARPAC(String value) {
            this.parpac = value;
        }

        public XMLGregorianCalendar getDANAPAC() {
            return danapac;
        }

        public void setDANAPAC(XMLGregorianCalendar danapac) {
            this.danapac = danapac;
        }

        public String getSEXOPAC() {
            return sexopac;
        }

        public void setSEXOPAC(String sexopac) {
            this.sexopac = sexopac;
        }
    }

}
