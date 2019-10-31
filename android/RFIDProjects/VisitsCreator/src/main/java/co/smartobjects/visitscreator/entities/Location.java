package co.smartobjects.visitscreator.entities;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa una ubicación
 * Created by Jorge on 22/07/2016.
 */
public class Location {
    private String name;
    private String type;
    private long id;

    public Location(String name, String type, long id) {
        this.name = name;
        this.type = type;
        this.id = id;
    }

    public Location(JSONObject jsonLocation) throws JSONException {
        this(jsonLocation.getString("name"), jsonLocation.getString("type"),
                jsonLocation.getLong("id"));
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static List<Location> listFromJSONString(String json){
        try
        {
            JSONArray locationList = (JSONArray) new JSONTokener(json).nextValue();
            List<Location> locations = new ArrayList<>(locationList.length());
            for(int i=0; i < locationList.length(); i++)
            {
                locations.add(i, new Location(locationList.getJSONObject(i)));

            }
            return locations;
        }
        catch (JSONException e)
        {
            Log.e("ERROR", e.getMessage(), e);
            return null;
        }
    }
}
