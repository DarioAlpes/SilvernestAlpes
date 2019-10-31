package smartobjects.com.smobapp.utils;

/**
 * Created by Andres Rubiano on 26/10/2015.
 * Clase que se encarga de mostrar el tipo de objeto a través del
 * método <<instanceOf>>. Se usa el patrón de diseño <<Singleton>>.
 */
public class UtilsTypeClass {

    private static UtilsTypeClass mUtilsTypeClass;

    public UtilsTypeClass() {
    }

    //Instanciamos la clase usando el patrón de diseño Singleton
    public static UtilsTypeClass newInstance () {
        if (mUtilsTypeClass == null) {
            mUtilsTypeClass = new UtilsTypeClass();
        }
        return mUtilsTypeClass;
    }

    //Preguntamos si el tipo de objeto creado, es de tipo Integer.
    public boolean isInteger (Object object) throws NullPointerException {
        if (object != null) {
            if (object instanceof Integer) {
                return true;
            }
        }
        return false;
    }

    //Preguntamos si el tipo de objeto creado, es de tipo float.
    public boolean isFloat (Object object) throws NullPointerException {
        if (object != null) {
            if (object instanceof Float) {
                return true;
            }
        }
        return false;
    }

    //Preguntamos si el tipo de objeto creado, es de tipo Long.
    public boolean isLong (Object object) throws NullPointerException {
        if (object != null) {
            if (object instanceof Long) {
                return true;
            }
        }
        return false;
    }

    //Preguntamos si el tipo de objeto creado, es de tipo byte.
    public boolean isByte (Object object) throws NullPointerException {
        if (object != null) {
            if (object instanceof Byte) {
                return true;
            }
        }
        return false;
    }

    //Preguntamos si el tipo de objeto creado, es de tipo short.
    public boolean isShort (Object object) throws NullPointerException {
        if (object != null) {
            if (object instanceof Short) {
                return true;
            }
        }
        return false;
    }

    //Preguntamos si el tipo de objeto creado, es de tipo Double.
    public boolean isDouble (Object object) throws NullPointerException {
        if (object != null) {
            if (object instanceof Double) {
                return true;
            }
        }
        return false;
    }

    //Preguntamos si el tipo de objeto creado, es de tipo String.
    public boolean isString (Object object) throws NullPointerException {
        if (object != null) {
            if (object instanceof String) {
                return true;
            }
        }
        return false;
    }

    //Preguntamos si el tipo de objeto creado, es de tipo CharSequence.
    public boolean isCharSequence (Object object) throws NullPointerException {
        if (object != null) {
            if (object instanceof CharSequence) {
                return true;
            }
        }
        return false;
    }
}
