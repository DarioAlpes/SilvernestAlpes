package entidades.transacciones;
import org.json.JSONArray;
import org.json.JSONObject;

import entidades.Item;
import excepciones.Conexion;

public class Transaccion {
	public final int id;
	public final int idUbicacion;
	public final String estado;
	public final String[] items;
	
	public Transaccion(JSONObject json) {
		this.id = json.getInt("id");
		this.idUbicacion = json.getInt("idUbicacion");
		this.estado = json.getString("estado");
		JSONArray items = json.getJSONArray("items");
		this.items = new String[items.length()];
		for(int e = 0;e < this.items.length; e++){
			this.items[e]=items.getString(e);
		}
		
	}
	public static Transaccion crearAuditoria(int idUbicacion) throws Exception{
		JSONObject obj = new JSONObject();
		obj.put("idUbicacion", idUbicacion);
		return new Transaccion(Conexion.doPOSTSimple("/operacion/auditoria/crear", obj.toString()));
	}
	@Override
	public String toString() {
		return "Producto [id=" + id + "]";
	}
}
