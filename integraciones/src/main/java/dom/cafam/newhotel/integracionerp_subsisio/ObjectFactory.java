
package dom.cafam.newhotel.integracionerp_subsisio;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import dom.cafam.newhotel.integracionerp_subsidio.InfoAfilNewHotel;
import dom.cafam.newhotel.integracionerp_subsidio.InfoAfilNewHotelRequest;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the dom.cafam.newhotel.integracionerp_subsisio package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ConsultarInfoAfilNewHotelRequest_QNAME = new QName("http://newhotel.cafam.dom/integracionERP_Subsisio", "ConsultarInfoAfil_NewHotelRequest");
    private final static QName _ConsultarInfoAfilNewHotelResponse_QNAME = new QName("http://newhotel.cafam.dom/integracionERP_Subsisio", "ConsultarInfoAfil_NewHotelResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: dom.cafam.newhotel.integracionerp_subsisio
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InfoAfilNewHotelRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://newhotel.cafam.dom/integracionERP_Subsisio", name = "ConsultarInfoAfil_NewHotelRequest")
    public JAXBElement<InfoAfilNewHotelRequest> createConsultarInfoAfilNewHotelRequest(InfoAfilNewHotelRequest value) {
        return new JAXBElement<InfoAfilNewHotelRequest>(_ConsultarInfoAfilNewHotelRequest_QNAME, InfoAfilNewHotelRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InfoAfilNewHotel }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://newhotel.cafam.dom/integracionERP_Subsisio", name = "ConsultarInfoAfil_NewHotelResponse")
    public JAXBElement<InfoAfilNewHotel> createConsultarInfoAfilNewHotelResponse(InfoAfilNewHotel value) {
        return new JAXBElement<InfoAfilNewHotel>(_ConsultarInfoAfilNewHotelResponse_QNAME, InfoAfilNewHotel.class, null, value);
    }

}
