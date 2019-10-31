package excepciones;

public class RequestException extends Exception{
	private int level;
	private String mensaje;
	public RequestException(int level, String mensaje) {
		super(mensaje);
		this.level = level;
		this.mensaje = mensaje;
	}
	public RequestException(int level, String mensaje, Exception parent) {
		super(mensaje, parent);
		this.level = level;
		this.mensaje = mensaje;
	}
}
