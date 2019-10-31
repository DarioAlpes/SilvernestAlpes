package smartobjects.com.smobapp.connectivity;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

/**
 * Created by Andres Rubiano on 09/11/2015.
 */
public class HttpUtils {

    InputStream mInputStream = null;
    String mResult = "";
    public static HttpUtils mUtils;

    public HttpUtils() {}

    public static HttpUtils newInstance() {
        if (mUtils == null) {
            mUtils = new HttpUtils();
        }
        return mUtils;
    }

    public JSONArray getserverdata(ArrayList<NameValuePair> parameters, String urlwebserver ){
        //conecta via http y envia un post.
        httppostconnect(parameters,urlwebserver);
        if (mInputStream !=null){//si obtuvo una respuesta
            getpostresponse();
            return getjsonarray();
        }else{
            return null;
        }
    }

    public JSONObject getServerDataObject(ArrayList<NameValuePair> parameters, String urlwebserver ){
        //conecta via http y envia un post.
        httppostconnect(parameters,urlwebserver);
        if (mInputStream !=null){//si obtuvo una respuesta
            getpostresponse();
            return getJsonObject();
        }else{
            return null;
        }
    }


    //peticion HTTP
    private void httppostconnect(ArrayList<NameValuePair> parametros, String urlwebserver){
        try{
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(urlwebserver);
            if (parametros != null) {
                httppost.setEntity(new UrlEncodedFormEntity(parametros));
            }
            //ejecuto peticion enviando datos por POST
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            mInputStream = entity.getContent();

        }catch(Exception e){
            Log.e("log_tag", "Error in http connection " + e.toString());
        }

    }

    public void getpostresponse(){
        //Convierte respuesta a String
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(mInputStream,"iso-8859-1"),8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            mInputStream.close();
            mResult =sb.toString();
            Log.e("getpostresponse"," mResult= "+sb.toString());
        }catch(Exception e){
            Log.e("log_tag", "Error converting mResult "+e.toString());
        }
    }

    public JSONArray getjsonarray(){
        //parse json data
        try{
            JSONArray jArray = new JSONArray(mResult);

            return jArray;
        }
        catch(JSONException e){
            Log.e("log_tag", "Error parsing data "+e.toString());
            return null;
        }

    }

    public JSONObject getJsonObject(){
        //parse json data
        try{
            if (mResult != null) {
                return new JSONObject(mResult);
            }
        }
        catch(JSONException e){
            Log.e("log_tag", "Error parsing data "+e.toString());
            return null;
        }
        return null;
    }

}
