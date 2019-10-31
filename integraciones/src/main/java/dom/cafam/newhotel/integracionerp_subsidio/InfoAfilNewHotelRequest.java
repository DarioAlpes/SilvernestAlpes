
package dom.cafam.newhotel.integracionerp_subsidio;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Parámetro para obtener Datos afiliado New Hotel
 * 
 * <p>Java class for InfoAfil_NewHotelRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="InfoAfil_NewHotelRequest"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="TPIDEN" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="IDTRAB" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InfoAfil_NewHotelRequest", propOrder = {
    "tpiden",
    "idtrab"
})
public class InfoAfilNewHotelRequest {

    @XmlElement(name = "TPIDEN", required = true)
    protected String tpiden;
    @XmlElement(name = "IDTRAB", required = true)
    protected String idtrab;

    /**
     * Gets the value of the tpiden property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTPIDEN() {
        return tpiden;
    }

    /**
     * Sets the value of the tpiden property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTPIDEN(String value) {
        this.tpiden = value;
    }

    /**
     * Gets the value of the idtrab property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIDTRAB() {
        return idtrab;
    }

    /**
     * Sets the value of the idtrab property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIDTRAB(String value) {
        this.idtrab = value;
    }

}
