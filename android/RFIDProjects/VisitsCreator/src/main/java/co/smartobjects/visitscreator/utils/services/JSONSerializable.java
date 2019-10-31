package co.smartobjects.visitscreator.utils.services;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Objects that can transform themselves in JSON objects to consume services expecting JSON objects.
 * Created by Jorge on 24/08/2016.
 */
public interface JSONSerializable {
    JSONObject toJSON() throws JSONException;
}
