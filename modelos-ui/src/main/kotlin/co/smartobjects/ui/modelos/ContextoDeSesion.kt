package co.smartobjects.ui.modelos

interface ContextoDeSesion
{
    val idCliente: Long
    val origenDeProcesamiento: String
    val nombreDeUsuario: String
    val idDispositivoDeProcesamiento: String
    val idUbicacion: Long
}

data class ContextoDeSesionImpl(
        override val idCliente: Long,
        override val origenDeProcesamiento: String,
        override val nombreDeUsuario: String,
        override val idDispositivoDeProcesamiento: String,
        override val idUbicacion: Long
                               ) : ContextoDeSesion