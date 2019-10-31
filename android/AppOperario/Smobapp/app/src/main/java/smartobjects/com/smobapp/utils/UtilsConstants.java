package smartobjects.com.smobapp.utils;

/**
 * Created by Andres Rubiano on 07/10/2015.
 */
public class UtilsConstants {

    public final static class GENERAL {
        public final static String LOG_ERROR = "ERROR",
                                    LOG_INFO = "INFO";
        public final static CharSequence LOG_OK    = "OK";

        public final static String PREFERENCES = "preferencesApp";

        public final static String MENSAJE_GENERICO = "mensajeGenerico";
        public final static String WITHOUT_INTERNET = "No hay conexión a internet. Por favor, vuelva a intentarlo.";

    }

    public final static class  ERROR {
        public final static String FIELD_NULL = "Campo nulo";
        public final static String UNEXPECTED_ERROR = "Error inesperado en ";
    }

    public final static class LAST_SCREEN {
        public final static int ATENDER_HOME = 1;
    }

    public final static class FECHA {
        public final static String SIMPLE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    }

    public final static class SQLITE {
        public final static String DATABASE_NAME = "BINGO";
        public final static String DROP_TABLE   = "drop table if exists";
        public final static String CREATE_TABLE = "create table";
        public final static String SPACE        = " ";

        public final static String TABLE_CARTON = "CARTON";
    }

    public final static class DOWNLOAD {
        public final static String MESSAGE_SORTEO = "Descargando sorteo...";
    }

    public final static class USUARIO {
        public final static String JSON_KEY = "autenticarUsuario";

        public final static String USER_TOKEN = "token";
        public final static String USER_PARCELABLE = "userParcelable";

    }

    public final static class PRODUCTO {
        public final static String JSON_LIST_PRODUCT = "listaProducto";
        public final static String JSON_DETAIL_PRODUCT = "listaProductoDetalle";
        public final static String JSON_PRODUCT_BY_SKU = "productoPorSku";
        public final static String BUNDLE_LIST_PRODUCT = "bundleListaProducto";
        public final static String BUNDLE_PRODUCT = "bundleProducto";

        public final static String BUNDLE_ID = "bundleProductoId";
        public final static String BUNDLE_SKU = "bundleProductoSku";
    }

    public final static class ITEM {
        public final static String JSON_LIST_ITEM = "listaItem";
        public final static String JSON_DETAIL_ITEM = "listaItemDetalle";
        public final static String BUNDLE_LIST_ITEM = "bundleListaItem";
        public final static String BUNDLE_ITEM = "bundleItem";

        public final static String BUNDLE_ID = "bundleItemId";
        public final static String BUNDLE_SKU = "bundleItemSku";
    }

    public final static class PROGRESS_DIALOG {
        public final static String AUTHENTICATE_USER  = "Validando usuario...";
        public final static String DOWNLOAD_PRODUCTS  = "Descargando productos...";
        public final static String DOWNLOAD_PRODUCTS_DETAIL  = "Descargando detalle producto...";
        public final static String DOWNLOAD_CONSUTLAR_PRODUCTS  = "Consultando producto...";
        public final static String DOWNLOAD_ITEMS = "Descargando items...";
    }

    public final static class BUNDLE {
        public final static String ID_PRODUCTO  = "bundleIdProducto";
    }

    public final static class URL {
//        public final static String URL_BASE = "http://prmbackend.azurewebsites.net/";
        public final static String URL_BASE = "https://v1-dot-smartobjectssas.appspot.com/";
        public final static String PARAMETER_ACTIVIDAD_PROVENIENTE = "actividadProveniente";

        public final static String PARAMETER_RESUMEN = "resumen";
        public final static String PARAMETER_MENSAJE = "mensaje";
        public final static String PARAMETER_RESUMEN_ERROR = "resumenError";
        public final static String PARAMETER_RESUMEN_OK = "resumenOk";

        public final static String USER_AUTHENTICATE = "usuario/login";

        public final static String PRODUCT_LIST = "producto/list";
        public final static String PRODUCT_DETAIL = "items/getFromEPC";
        public final static String PRODUCT_BY_SKU = "producto/get";

        public final static String ITEM_LIST = "item/list";

        public final static String UBICACION_LISTS = "ubicacion/list";
    }

    public final static class JSON {
        public final static String JSON_SUCCESS = "success";
        public final static String JSON_VALOR = "valor";
        public final static String JSON_MENSAJE = "mensaje";
    }

    public final static class UBICACION {
        public final static String JSON_LIST_UBICACIONES = "listaUbicaciones";
        public final static String ARRAY_UBICACIONES = "arrayUbicaciones";
        public final static String BUNDLE_UBICACION_FINAL = "bundleUbicacionFinal";
    }

    public final static class AUDITORIA {
        public final static String AUDITORIA_INIT = "auditoriaInit";
    }

}

