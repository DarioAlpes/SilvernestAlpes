package entidades.transacciones;

import org.json.JSONArray;
import org.json.JSONObject;

import entidades.Item;
import excepciones.Conexion;

public class TransaccionAuditoria extends Transaccion{
	public TransaccionAuditoria(JSONObject json) {
		super(json);
	}
	public static TransaccionAuditoria crear(String SKU, int idUbicacion, int cantidad) throws Exception{
		JSONObject obj = new JSONObject();
		obj.put("idUbicacion", idUbicacion);
		JSONObject transaccion = Conexion.doPOSTSimple("/operacion/auditoria/crear", obj.toString());
		TransaccionAuditoria ret = new TransaccionAuditoria(transaccion);
		for(String p : ret.items)
			System.out.println(p);
		return ret;
	}
	public String finalizar(int idTransaccion) throws Exception{
		JSONObject obj = new JSONObject();
		obj.put("idTransaccion", idTransaccion);
		JSONArray items = new JSONArray();
		for(String item : this.items){
			JSONObject jsonItem = new JSONObject();
			/*jsonItem.put("EPC", item.EPC);
			jsonItem.put("estado", item.estadoPistola);*/
			items.put(jsonItem);
		}
		obj.put("items", items);
		return Conexion.doPOSTString("/operacion/auditoria/terminar", obj.toString());
	}
}
