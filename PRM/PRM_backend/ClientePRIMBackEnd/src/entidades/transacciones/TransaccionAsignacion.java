package entidades.transacciones;

import org.json.JSONArray;
import org.json.JSONObject;

import entidades.Item;
import excepciones.Conexion;

public class TransaccionAsignacion extends Transaccion{
	public TransaccionAsignacion(JSONObject json) {
		super(json);
	}
	public static TransaccionAsignacion crear(String SKU, int idUbicacion, int cantidad) throws Exception{
		JSONObject obj = new JSONObject();
		obj.put("SKU", SKU);
		obj.put("idUbicacion", idUbicacion);
		obj.put("cantidad", cantidad);
		JSONObject transaccion = Conexion.doPOSTSimple("/operacion/asignacion/crear", obj.toString());
		TransaccionAsignacion ret = new TransaccionAsignacion(transaccion);
		for(String p : ret.items)
			System.out.println(p);
		return ret;
	}
	public String finalizar() throws Exception{
		JSONObject obj = new JSONObject();
		obj.put("idAsignacion", id);
		return Conexion.doPOSTString("/operacion/asignacion/terminar", obj.toString());
	}
}
