package smartobjects.com.smobapp.utils;

import android.content.Context;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpresionRegular {

	private Pattern pattern;
	private Matcher matcher;

	private static final String REGISTRO_CIVIL_PATTERN = "[a-zA-Z0-9-]*";
	private static final String PASAPORTE_PATTERN = "[a-zA-Z0-9-]*";
	

	public boolean validarRC(final String cadena) {
		pattern = Pattern.compile(REGISTRO_CIVIL_PATTERN);
		matcher = pattern.matcher(cadena.trim());
		return matcher.matches();
	}

	public boolean validarPasaporte(final String cadena) {
		pattern = Pattern.compile(PASAPORTE_PATTERN);
		matcher = pattern.matcher(cadena.trim());
		return matcher.matches();
	}
	
	public boolean validarNombre(Context context, final String cadena) {
		String nombre  = "[a-záéíóúñA-Z0-9]*";
		pattern = Pattern.compile(nombre);
		matcher = pattern.matcher(cadena.trim());
		return matcher.matches();
	}

}
