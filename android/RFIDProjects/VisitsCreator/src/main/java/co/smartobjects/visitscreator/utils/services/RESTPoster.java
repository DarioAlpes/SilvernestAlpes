package co.smartobjects.visitscreator.utils.services;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Class used to do async POST request to backend services.
 * Created by Jorge on 24/08/2016.
 */
public class RESTPoster<T extends POSTable> extends AsyncTask<T, Void, Pair<Integer, String>[]> {

    private T[] objectsToPost;
    private POSTResponseProcessor<T> postResponseProcessor;

    public RESTPoster() {
        this(null);
    }

    public RESTPoster(POSTResponseProcessor<T> postResponseProcessor) {
        this.postResponseProcessor = postResponseProcessor;
    }

    @Override
    protected Pair<Integer, String>[] doInBackground(T...objectsToPost) {
        this.objectsToPost = objectsToPost;
        @SuppressWarnings("unchecked")
        Pair<Integer, String>[] results = (Pair<Integer, String>[]) new Pair[objectsToPost.length];
        for(int i=0;i<objectsToPost.length;i++) {
            try {
                URL url = new URL(objectsToPost[i].getPostURL());
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestProperty("Content-type", "application/json");
                try {
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
                    writer.write(objectsToPost[i].toJSON().toString());
                    writer.flush();
                    writer.close();
                    BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    results[i] = new Pair<>(urlConnection.getResponseCode(), stringBuilder.toString());
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
    protected void onPostExecute(Pair<Integer, String>[] results) {
        if(postResponseProcessor!=null) {
            for (int i = 0; i < results.length; i++) {
                postResponseProcessor.processPOSTResult(objectsToPost[i], results[i]);
            }
        }
    }

}
