package co.smartobjects.visitscreator.entities;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import co.smartobjects.visitscreator.utils.services.POSTable;

/**
 * Represents a visit of a person to a location
 * Created by Jorge on 24/08/2016.
 */
public class Visit implements POSTable{
    private final static String ID_LOCATION_NAME = "id-location";
    private final static String START_DATE_NAME = "initial-time";
    private final static String FINAL_DATE_NAME = "final-time";

    private long idClient;
    private long idPerson;
    private long idLocation;
    private Date startDate;
    private Date endDate;
    private SimpleDateFormat dateFormater;

    public Visit(long idClient, long idPerson, long idLocation) {
        this( idClient, idPerson, idLocation, null);
    }

    public Visit(long idClient, long idPerson, long idLocation, Date startDate) {
        this( idClient, idPerson, idLocation, startDate, null);
    }

    public Visit(long idClient, long idPerson, long idLocation, Date startDate, Date endDate) {
        this.idClient = idClient;
        this.idPerson = idPerson;
        this.idLocation = idLocation;
        this.startDate = startDate;
        this.endDate = endDate;
        dateFormater = new SimpleDateFormat("yyyyMMddHHmmss");
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject jsonVisit = new JSONObject();
        jsonVisit.put(ID_LOCATION_NAME, idLocation);
        if(startDate!=null){
            jsonVisit.put(START_DATE_NAME, dateFormater.format(startDate));
        }
        if(endDate!=null){
            jsonVisit.put(FINAL_DATE_NAME, dateFormater.format(endDate));
        }
        return jsonVisit;
    }

    @Override
    public String getPostURL() {
        return "https://smartobjectssas.appspot.com/clients/"+idClient+"/persons/"+idPerson+"/visits/";
    }

    public long getIdClient() {
        return idClient;
    }

    public void setIdClient(long idClient) {
        this.idClient = idClient;
    }

    public long getIdPerson() {
        return idPerson;
    }

    public void setIdPerson(long idPerson) {
        this.idPerson = idPerson;
    }

    public long getIdLocation() {
        return idLocation;
    }

    public void setIdLocation(long idLocation) {
        this.idLocation = idLocation;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public SimpleDateFormat getDateFormater() {
        return dateFormater;
    }

    public void setDateFormater(SimpleDateFormat dateFormater) {
        this.dateFormater = dateFormater;
    }
}
