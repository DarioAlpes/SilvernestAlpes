package co.smartobjects.visitscreator.entities;

import co.smartobjects.visitscreator.utils.services.POSTable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase que representa una lista de lecturas
 * Created by Jorge on 12/09/2016.
 */
public class Readings implements POSTable{
    private final static String READINGS_NAME = "readings";

    private List<Reading> readings;
    private long idClient;
    private long idSensor;

    public Readings(long idClient, long idSensor, List<Reading> readings) {
        this.readings = readings;
        this.idClient = idClient;
        this.idSensor = idSensor;
    }

    public Readings(long idClient, long idSensor){
        this(idClient, idSensor, new ArrayList<Reading>());
    }

    public void addReading(Reading reading) {
        this.readings.add(reading);
    }

    @Override
    public String getPostURL() {
        return "https://smartobjectssas.appspot.com/clients/" + idClient + "/sensors/" + idSensor + "/readings/";
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONArray readingsArray = new JSONArray();
        for(Reading reading:readings){
            readingsArray.put(reading.toJSON());
        }
        JSONObject readingsJSON = new JSONObject();
        readingsJSON.put(READINGS_NAME, readingsArray);
        return  readingsJSON;
    }

    public List<Reading> getReadings() {
        return readings;
    }

    public void setReadings(List<Reading> readings) {
        this.readings = readings;
    }

    public long getIdClient() {
        return idClient;
    }

    public void setIdClient(long idClient) {
        this.idClient = idClient;
    }

    public long getIdSensor() {
        return idSensor;
    }

    public void setIdSensor(long idSensor) {
        this.idSensor = idSensor;
    }
}
