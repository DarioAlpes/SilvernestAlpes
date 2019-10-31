package co.smartobjects.ui.modelos


abstract class ErrorUI internal constructor(mensaje: String, val excepcion: Throwable?)
    : Exception(mensaje, excepcion)

class NoSePudoCargarNingunDatoAsociadoAlCatalogo(mensaje: String, excepcion: Throwable?)
    : ErrorUI(mensaje, excepcion)

class NoExisteCategoriaParaElFondo(val idFondo: Long?, val idCategoriaIntentada: Long)
    : ErrorUI("El fondo con id='$idFondo' referencia una categoría inválida con id='$idCategoriaIntentada'", null)