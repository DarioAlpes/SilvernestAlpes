package cjm;

import org.json.JSONObject;

import entidades.Producto;
import excepciones.Conexion;

public class Beacon {
	public final int major, minor;
	public final String ubicacion, tipo;
	public Beacon(JSONObject json) {
		major = json.getInt("major");
		minor = json.getInt("minor");
		ubicacion = json.optString("ubicacion");
		tipo = json.getString("tipo");
	}
	public static Beacon crearFijo(int idUbicacion, int major, int minor) throws Exception{
		JSONObject obj = new JSONObject();
		obj.put("ubicacion", idUbicacion);
		obj.put("major", major);
		obj.put("minor", minor);
		return new Beacon(Conexion.doPOSTSimple("/beacons/create/fijo", obj.toString()));
	}
	public static Beacon crearMovil(int major, int minor) throws Exception{
		JSONObject obj = new JSONObject();
		obj.put("major", major);
		obj.put("minor", minor);
		return new Beacon(Conexion.doPOSTSimple("/beacons/create/movil", obj.toString()));
	}
}
