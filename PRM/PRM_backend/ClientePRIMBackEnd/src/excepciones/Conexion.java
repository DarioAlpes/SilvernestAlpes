package excepciones;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import org.json.JSONArray;
import org.json.JSONObject;

import main.Main;

public class Conexion {
	
	public static String doPOSTString(String ruta, String json) throws Exception{
		return doPOST(ruta, json).getString("valor");
	}
	public static JSONObject doPOSTSimple(String ruta, String json) throws Exception{
		return doPOST(ruta, json).getJSONObject("valor");
	}
	public static JSONArray doPOSTArray(String ruta, String json) throws Exception{
		return doPOST(ruta, json).getJSONArray("valor");
	}

	public static String doGETString(String ruta, String... params) throws Exception{
		return doGET(ruta, params).getString("valor");
	}
	
	public static JSONObject doGETSimple(String ruta, String... params) throws Exception{
		return doGET(ruta, params).getJSONObject("valor");
	}
	public static JSONArray doGETArray(String ruta, String...params) throws Exception{
		return doGET(ruta, params).getJSONArray("valor");
	}
	private static JSONObject doPOST(String ruta, String json)throws RequestException{
		System.out.println("POST: "+ruta);
		System.out.println(json);
		long s = System.currentTimeMillis();
		JSONObject ret = null;
		try{
			String uri = Main.AMBIENTE.URL+ruta;
			URL url = new URL(uri);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("Authorization", "Token "+Main.AMBIENTE.TOKEN);
			connection.setDoOutput(true);
			connection.connect();
			OutputStream out = connection.getOutputStream();
			out.write(json.getBytes(Charset.forName("UTF-8")));
			out.flush();
			InputStream in = connection.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String resp = ""; 
			for(String line;(line=br.readLine())!=null;)
				resp+=line;
			connection.disconnect();
			System.out.println(resp);
			ret = new JSONObject(resp);
		}catch(Exception ex){
			System.out.println(System.currentTimeMillis()-s);
			throw new RequestException(3, "Error inesperado", ex);
		}
		System.out.println(System.currentTimeMillis()-s);
		if(ret.getInt("success") == 1)
			return ret;
		else throw new RequestException(ret.getInt("success"), ret.getString("mensaje"));
	}
	public static JSONObject doGET(String ruta, String... params)throws RequestException{
		JSONObject ret = null;
		try{
			for(int e=0;e<params.length;e+=2){
				ruta+=e==0?"?":"&";
				ruta+=URLEncoder.encode(params[e], "UTF-8")+"="+URLEncoder.encode(params[e+1], "UTF-8");
			}
			String uri = Main.AMBIENTE.URL+ruta;
			System.out.println("GET: "+ruta);
			URL url = new URL(uri);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("Authorization", "Token "+Main.AMBIENTE.TOKEN);
			connection.connect();
			InputStream in = connection.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String resp = ""; 
			for(String line;(line=br.readLine())!=null;)
				resp+=line;
			connection.disconnect();
			System.out.println(resp);
			ret = new JSONObject(resp);
		}catch(Exception ex){
			throw new RequestException(3, "Error inesperado", ex);
		}
		if(ret.getInt("success") == 1)
			return ret;
		else throw new RequestException(ret.getInt("success"), ret.getString("mensaje"));
	}
	
}
