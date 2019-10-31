package main;
import java.util.Arrays;
import java.util.Random;

import ambientes.Ambiente;
import entidades.Cliente;
import entidades.Item;
import entidades.Producto;
import entidades.Ubicacion;
import entidades.Usuario;
import entidades.transacciones.Transaccion;
import entidades.transacciones.TransaccionAsignacion;
import excepciones.Conexion;

public class Main {
	public final static Ambiente AMBIENTE = Ambiente.DEV;
	public static void main(String[] args) throws Exception{
		//setupAmbiente();
		//AMBIENTE.TOKEN = Usuario.getToken("gasotelo2", "admin");
		Ubicacion.lista();
		//Item.list(null, null, null);
		//Producto.lista();
		//Ubicacion.lista();
		//Item.list(null, null, null);
		//int idTransaccion = TransaccionAsignacion.crear("1719768599", 70, 1000).id;
		//TransaccionAsignacion.finalizar(43);
		//int idTransaccion = TransaccionAsignacion.crear("186379155", 70, 2000).id;
		//TransaccionAsignacion.finalizar(idTransaccion);
		//TransaccionAsignacion.finalizar(43);
		//Item.list("ENSITIO", 69, "865405419");
		//setupAmbiente();
		/*Cliente c = Cliente.crear("Nuevo cliente");
		Usuario.crear(c, "gasotelo6", "holamundo");
		Usuario.getToken("gasotelo6", "holamundo");
		*/
		//Conexion.doGET("http://localhost:58850/api/values");
		//Producto.lista();
		//Transaccion.crearAuditoria(70);
		//TransaccionAsignacion.crear("1997714408", 70, 100);
		//TransaccionAsignacion.finalizar(25);
		//TOKEN = Usuario.getToken("german.sotelo456", "admin");
		//setupAmbiente();
		//Ubicacion[] ubicaciones = Ubicacion.lista();
		//Ubicacion.get(0);
		//Ubicacion.crear("Andino", "LOCAL", 38);
		//Ubicacion.listaIdPadre(1);
		//Ubicacion.lista();
		//Ubicacion.listarTipos();
		//System.out.println(Usuario.getToken("andres.rubiano10", "admin"));
		//Producto[] productos = Producto.lista();
		//Item.listaSKU("7806148");
		//Item.lista();
		//TOKEN = rutinaCrearUsuario();
		//rutinaCrearProductos();
		//Producto.delete(41);
		//Producto.lista();
		/*for(Producto p : Producto.lista())
			if(p.id == -1)
				Producto.delete(p.SKU);*/
		//Item.listUbicacion(70);
		//Transaccion.crearAuditoria(70);
		/*Random r = new Random();
		for(Ubicacion u : ubicaciones)
			if(u.tipo.equals("LOCAL"))
				for(Producto p : productos)
					Item.crear(p.SKU, u.id, r.nextInt(5)+1);*/
		//Item.crear("1651876134", 5);
		//System.out.println(Item.get("00010000002300000000013f"));
		//System.out.println(Item.get("7806148", 58));
	}
	public static void rutinaCrearProductos()throws Exception{
		Random r = new Random();
		for(int e=0;e<10;e++)
			System.out.println(Producto.crear(""+Math.abs(r.nextInt())));
	}
	public static void setupAmbiente()throws Exception{
		Cliente c = Cliente.crear("Cliente pruebas Andres");
		AMBIENTE.idCliente = c.id;
		String username = "gasotelo"+Contador.getNext();
		Usuario.crear(c, username, "admin");
		AMBIENTE.TOKEN = Usuario.getToken(username, "admin");
		
		Random r = new Random();
		Producto[] productos = new Producto[10];
		for(int e=0;e<productos.length;e++)
			productos[e] = Producto.crear(""+Math.abs(r.nextInt()));
		
		Ubicacion[] ubicaciones = {
				Ubicacion.crear("Andino", "LOCAL", 38),
				Ubicacion.crear("Unicentro", "LOCAL", 38),
				Ubicacion.crear("Santafe", "LOCAL", 38),
				Ubicacion.crear("Centro Mayor", "LOCAL", 38)
		};
		for(Ubicacion u : ubicaciones)
			for(Producto p : productos){
				TransaccionAsignacion.crear(p.SKU, u.id, r.nextInt(5)+1).finalizar();
			}
	}
}
