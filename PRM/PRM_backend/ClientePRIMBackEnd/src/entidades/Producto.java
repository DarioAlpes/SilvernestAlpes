package entidades;
import org.json.JSONArray;
import org.json.JSONObject;

import excepciones.Conexion;

public class Producto {
	public final String SKU;
	public Producto(String sKU) {
		SKU = sKU;
	}
	public Producto(JSONObject producto) {
		this(producto.getString("SKU"));
	}
	public static Producto[] lista() throws Exception{
		JSONArray productos = Conexion.doGETArray("/producto/list");
		Producto[] ret = new Producto[productos.length()];
		for(int e = 0;e < ret.length; e++){
			if(productos.get(e) instanceof JSONObject)
				ret[e]=new Producto((JSONObject)productos.get(e));
		}
		for(Producto p : ret)
			System.out.println(p);
		return ret;
	}
	public static Producto get(String SKU) throws Exception{
		JSONObject obj = new JSONObject();
		obj.put("SKU", SKU);
		return new Producto(Conexion.doPOSTSimple("/producto/get", obj.toString()));
	}
	public static Producto crear(String SKU) throws Exception{
		JSONObject obj = new JSONObject();
		obj.put("SKU", SKU);
		return new Producto(Conexion.doPOSTSimple("/producto/create", obj.toString()));
	}
	public static String delete(int id) throws Exception{
		JSONObject obj = new JSONObject();
		obj.put("id", id);
		return Conexion.doPOSTString("/producto/delete", obj.toString());
	}
	public static String delete(String SKU) throws Exception{
		JSONObject obj = new JSONObject();
		obj.put("SKU", SKU);
		return Conexion.doPOSTString("/producto/delete", obj.toString());
	}
	@Override
	public String toString() {
		return "Producto [SKU=" + SKU + "]";
	}
}
