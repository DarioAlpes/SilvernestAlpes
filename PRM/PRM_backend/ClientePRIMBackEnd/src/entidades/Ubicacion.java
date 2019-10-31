package entidades;
import org.json.JSONArray;
import org.json.JSONObject;

import excepciones.Conexion;

public class Ubicacion {
	public final int id;
	public final int idPadre;
	public final String nombre;
	public final String EPC;
	public final String llave;
	public final String tipo;
	
	public Ubicacion(JSONObject ubicacion) {
		this.id = ubicacion.getInt("id");
		this.nombre = ubicacion.optString("nombre", null);
		this.EPC = ubicacion.optString("EPC", null);
		this.tipo = ubicacion.optString("tipo", null);
		this.llave = ubicacion.optString("llave", null);
		this.idPadre = ubicacion.optInt("padre", -1);
	}
	
	public static Ubicacion get(int id) throws Exception{
		JSONObject obj = new JSONObject();
		obj.put("id", id);
		JSONObject item = Conexion.doPOSTSimple("/ubicacion/get", obj.toString());
		return new Ubicacion(item);
	}
	public static Ubicacion get(String SKU, int idItem) throws Exception{
		JSONObject obj = new JSONObject();
		obj.put("SKU", SKU);
		obj.put("id", idItem);
		JSONObject item = Conexion.doPOSTSimple("/item/getFromSKU", obj.toString());
		return new Ubicacion(item);
	}
	public static Ubicacion[] lista() throws Exception{
		JSONArray ubicaciones = Conexion.doGETArray("/ubicacion/list");
		Ubicacion[] ret = new Ubicacion[ubicaciones.length()];
		for(int e = 0;e < ret.length; e++){
			ret[e]=new Ubicacion(ubicaciones.getJSONObject(e));
		}
		for(Ubicacion p : ret)
			System.out.println(p);
		return ret;
	}
	public static String[] listarTipos() throws Exception{
		JSONArray ubicaciones = Conexion.doGETArray("/ubicacion/listTipos");
		String[] ret = new String[ubicaciones.length()];
		for(int e = 0;e < ret.length; e++){
			ret[e]=ubicaciones.getString(e);
		}
		return ret;
	}
	public static Ubicacion[] listaIdPadre(int idUbicacionPadre) throws Exception{
		JSONObject obj = new JSONObject();
		if(idUbicacionPadre!=-1)
			obj.put("id", idUbicacionPadre);
		JSONArray productos = Conexion.doPOSTArray("/ubicacion/listChilds", obj.toString());
		Ubicacion[] ret = new Ubicacion[productos.length()];
		for(int e = 0;e < ret.length; e++){
			if(productos.get(e) instanceof JSONObject)
				ret[e]=new Ubicacion(productos.getJSONObject(e));
		}
		for(Ubicacion p : ret)
			System.out.println(p);
		return ret;
	}
	public static Ubicacion crear(String nombre, String tipo, int idPadre) throws Exception{
		JSONObject obj = new JSONObject();
		obj.put("nombre", nombre);
		obj.put("tipo", tipo);
		obj.put("idPadre", idPadre);
		
		JSONObject ubicacion = Conexion.doPOSTSimple("/ubicacion/create", obj.toString());
		return new Ubicacion(ubicacion);
	}
	@Override
	public String toString() {
		if(idPadre == -1)
			return "Ubicacion [id=" + id + ", nombre=" + nombre + ", llave=" + llave + ", EPC=" + EPC + ", tipo=" + tipo + "]";
		else
			return "Ubicacion [id=" + id + ", nombre=" + nombre + ", padre=" + idPadre + " llave=" + llave + ", EPC=" + EPC + ", tipo=" + tipo + "]";
	}
	private static String getString(JSONObject item, String key){
		if(item.isNull(key))
			return null;
		return item.getString(key);
	}
}
