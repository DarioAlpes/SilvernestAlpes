package cjm;

import org.json.JSONObject;

import excepciones.Conexion;

public class SensorMovil {
	public final int id;
	public final String firma;
	public final String ubicacion;
	public SensorMovil(JSONObject json) {
		id = json.getInt("id");
		firma = json.getString("sign");
		ubicacion = json.optString("ubicacion");
	}
	public static SensorMovil crear() throws Exception{
		return new SensorMovil(Conexion.doGET("/sensor/create/movil"));
	}
}
