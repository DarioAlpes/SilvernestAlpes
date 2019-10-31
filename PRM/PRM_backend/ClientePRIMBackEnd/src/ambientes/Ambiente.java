package ambientes;

public class Ambiente {
	public static final Ambiente DEV = new Ambiente(20, "67fd622966477da11b881c51ca46370c3ab8b7f31734a24d534c670be8ec35a3", "http://localhost");
	public static final Ambiente PROD = new Ambiente(1, "6ad5e99bbc1784909bf3c1b62ab2f59bc0f0a472939ff1c2c9ffb3bf08090b44", "https://v1-dot-smartobjectssas.appspot.com/");
	public long idCliente;
	public String TOKEN;
	public final String URL;
	public Ambiente(long idCliente, String tOKEN, String URL) {
		this.idCliente = idCliente;
		this.TOKEN = tOKEN;
		this.URL=URL;
	}
	
}
