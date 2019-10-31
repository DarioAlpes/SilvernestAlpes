package co.smartobjects.visitscreator.utils.services;

/**
 * Interfaz a implementar por todas las clases que vayan a procesar el resultado de una petición
 * GET al backend
 * Created by Jorge on 21/07/2016.
 */
public interface RESTResultProcessor {
    void processResult(String url, String result);
}