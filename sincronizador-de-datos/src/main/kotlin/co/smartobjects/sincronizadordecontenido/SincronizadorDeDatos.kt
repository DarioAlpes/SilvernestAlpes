package co.smartobjects.sincronizadordecontenido


interface SincronizadorDeDatos
{
    val gestorDescargaDeDatos: GestorDescargaDeDatos
}

class SincronizadorDeDatosImpl
(
        override val gestorDescargaDeDatos: GestorDescargaDeDatos
) : SincronizadorDeDatos
{
}