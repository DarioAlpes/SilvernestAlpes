package entidades;
import org.json.JSONObject;

import excepciones.Conexion;

public class Usuario {
	final long idCliente;
	final String username;
	public Usuario(long idCliente, String username) {
		this.idCliente = idCliente;
		this.username = username;
	}
	public static Usuario crear(Cliente cliente, String username, String password) throws Exception{
		JSONObject obj = new JSONObject();
		obj.put("username", username);
		obj.put("password", password);
		obj.put("idCliente", cliente.id);
		Conexion.doPOSTString("/usuario/create", obj.toString());
		return new Usuario(cliente.id, username);
	}
	public static String getToken(String username, String password) throws Exception{
		JSONObject obj = new JSONObject();
		obj.put("username", username);
		obj.put("password", password);
		return Conexion.doPOSTString("/usuario/login", obj.toString());
	}
}
