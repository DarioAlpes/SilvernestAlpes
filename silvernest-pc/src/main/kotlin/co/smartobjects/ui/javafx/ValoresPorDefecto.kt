package co.smartobjects.ui.javafx

import javafx.scene.image.Image

internal object ValoresPorDefecto
{
    const val tamañoImagenProducto = 95.0
    val imagenPorDefecto =
            Image(
                    ValoresPorDefecto::class.java.getResource("/imagenes/imagen_no_disponible.png").toExternalForm(),
                    false
                 )

    val imagenErrorCargaDatos =
            Image(
                    ValoresPorDefecto::class.java.getResource("/imagenes/no_fue_posible_cargar.png").toExternalForm(),
                    true
                 )

    val imagenNoHayDatos =
            Image(
                    ValoresPorDefecto::class.java.getResource("/imagenes/no_hay_datos.png").toExternalForm(),
                    true
                 )

    val imagenCargaParcial =
            Image(
                    ValoresPorDefecto::class.java.getResource("/imagenes/carga_parcial.png").toExternalForm(),
                    true
                 )
}