package co.smartobjects.visitscreator.entities;

import co.smartobjects.visitscreator.utils.services.JSONSerializable;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Representa una lectura
 * Created by Jorge on 12/09/2016.
 */
public class Reading implements JSONSerializable{
    private final static String ID_BEACON_NAME = "id-beacon";
    private final static String READING_TIME_NAME = "reading-time";
    private final static SimpleDateFormat DATE_FORMATER = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);

    private long idBeacon;
    private Date readingTime;

    public Reading(long idBeacon, Date readingTime) {
        this.idBeacon = idBeacon;
        this.readingTime = readingTime;
    }

    public Reading(long idBeacon) {
        this(idBeacon, new Date());
    }

    public long getIdBeacon() {
        return idBeacon;
    }

    public void setIdBeacon(long idBeacon) {
        this.idBeacon = idBeacon;
    }

    public Date getReadingTime() {
        return readingTime;
    }

    public void setReadingTime(Date readingTime) {
        this.readingTime = readingTime;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject jsonReading = new JSONObject();
        jsonReading.put(ID_BEACON_NAME, idBeacon);
        jsonReading.put(READING_TIME_NAME, DATE_FORMATER.format(readingTime));
        return jsonReading;
    }
}
