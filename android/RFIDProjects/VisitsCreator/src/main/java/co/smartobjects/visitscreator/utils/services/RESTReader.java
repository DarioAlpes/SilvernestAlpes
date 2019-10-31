package co.smartobjects.visitscreator.utils.services;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Clase usada para leer objetos de un api web, solo hace GET
 * Created by Jorge on 21/07/2016.
 */
public class RESTReader extends AsyncTask<String, Void, String[]> {

    private RESTResultProcessor listener;
    private String[] urls;

    public RESTReader(RESTResultProcessor listener){
        this.listener = listener;
    }

    @Override
    protected String[] doInBackground(String... urls) {
        String results[] = new String[urls.length];
        this.urls = urls;
        for(int i=0; i < urls.length; i++) {
            try {
                URL url = new URL(urls[i]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    results[i] = stringBuilder.toString();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                results[i] = null;
            }
        }
        return results;
    }

    @Override
    protected void onPostExecute(String[] results) {
        for(int i=0; i < results.length; i++) {
            listener.processResult(urls[i], results[i]);
        }
    }

}
