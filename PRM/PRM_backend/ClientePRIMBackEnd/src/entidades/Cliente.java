package entidades;
import org.json.JSONObject;

import excepciones.Conexion;

public class Cliente {
	public final long id;
	public Cliente(long idCliente) {
		this.id = idCliente;
	}

	public static Cliente crear(String nombre) throws Exception{
		JSONObject solicitud = new JSONObject();
		solicitud.put("nombre", nombre);
		JSONObject json = Conexion.doPOSTSimple("/cliente/create", solicitud.toString());
		return new Cliente(json.getInt("id"));
	}
	
}
