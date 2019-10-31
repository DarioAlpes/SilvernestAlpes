package entidades;
import org.json.JSONArray;
import org.json.JSONObject;

import excepciones.Conexion;

public class Item {
	public final String EPC;
	public final String SKU;
	public final String estado;
	public final int ubicacion;
	public int estadoPistola;
	public Item(JSONObject item) {
		this.EPC = item.optString("EPC");
		this.SKU = item.optString("SKU");
		this.estado = item.optString("estado");
		this.ubicacion = item.optInt("ubicacion");
	}
	
	public static Item get(String EPC) throws Exception{
		JSONObject obj = new JSONObject();
		obj.put("EPC", EPC);
		JSONObject item = Conexion.doPOSTSimple("/item/getFromEPC", obj.toString());
		return new Item(item);
	}
	public static Item get(String SKU, int idItem) throws Exception{
		JSONObject obj = new JSONObject();
		obj.put("SKU", SKU);
		obj.put("id", idItem);
		JSONObject item = Conexion.doPOSTSimple("/item/getFromSKU", obj.toString());
		return new Item(item);
	}
	public static Item[] list(String estado, Integer ubicacion, String SKU) throws Exception{
		JSONObject obj = new JSONObject();
		if(ubicacion != null)
			obj.put("idUbicacion", ubicacion);
		if(estado != null)
			obj.put("estado", estado);
		if(SKU != null)
			obj.put("SKU", SKU);
		JSONArray productos;
		if(estado == null && ubicacion == null && SKU == null)
			productos = Conexion.doGETArray("/item/list");
		else
			productos = Conexion.doPOSTArray("/item/list", obj.toString());
		Item[] ret = new Item[productos.length()];
		for(int e = 0;e < ret.length; e++){
			if(productos.get(e) instanceof JSONObject)
				ret[e]=new Item(productos.getJSONObject(e));
		}
		for(Item p : ret)
			System.out.println(p);
		return ret;
	}
	public static Item[] listaSKU(String SKU) throws Exception{
		JSONObject obj = new JSONObject();
		obj.put("SKU", SKU);
		JSONArray productos = Conexion.doPOSTArray("/item/listSKU", obj.toString());
		Item[] ret = new Item[productos.length()];
		for(int e = 0;e < ret.length; e++){
			if(productos.get(e) instanceof JSONObject)
				ret[e]=new Item(productos.getJSONObject(e));
		}
		for(Item p : ret)
			System.out.println(p);
		return ret;
	}
	public static Item[] listaUbicacion(int ubicacion) throws Exception{
		JSONObject obj = new JSONObject();
		obj.put("idUbicacion", ubicacion);
		JSONArray productos = Conexion.doPOSTArray("/item/listUbicacion", obj.toString());
		Item[] ret = new Item[productos.length()];
		for(int e = 0;e < ret.length; e++){
			if(productos.get(e) instanceof JSONObject)
				ret[e]=new Item(productos.getJSONObject(e));
		}
		for(Item p : ret)
			System.out.println(p);
		return ret;
	}
	@Override
	public String toString() {
		return "Item [EPC=" + EPC + ", SKU=" + SKU + ", estado=" + estado + ", ubicacion="
				+ ubicacion + "]";
	}
}
