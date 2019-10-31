package co.smartobjects.visitscreator.entities;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un sensor
 * Created by Jorge on 22/07/2016.
 */
public class Sensor {
    private long id;
    private long idLocation;

    public Sensor(long id, long idLocation) {
        this.id = id;
        this.idLocation = idLocation;
    }

    public Sensor(JSONObject jsonSensor) throws JSONException {
        this(jsonSensor.getLong("id"), jsonSensor.getLong("id-location"));
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIdLocation() {
        return idLocation;
    }

    public void setIdLocation(long idLocation) {
        this.idLocation = idLocation;
    }

    public static List<Sensor> listFromJSONString(String json){
        try
        {
            JSONArray sensorList = (JSONArray) new JSONTokener(json).nextValue();
            List<Sensor> sensors = new ArrayList<>(sensorList.length());
            for(int i=0; i < sensorList.length(); i++)
            {
                sensors.add(i, new Sensor(sensorList.getJSONObject(i)));

            }
            return sensors;
        }
        catch (JSONException e)
        {
            Log.e("ERROR", e.getMessage(), e);
            return null;
        }
    }
}
