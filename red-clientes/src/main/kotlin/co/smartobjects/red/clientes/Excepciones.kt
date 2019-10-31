package co.smartobjects.red.clientes


abstract class ErrorClienteRed internal constructor(mensaje: String, excepcion: Throwable?)
    : Exception(mensaje, excepcion)

class Timeout(mensaje: String) : ErrorClienteRed(mensaje, null)
class ProblemaRed(mensaje: String) : ErrorClienteRed(mensaje, null)
class ProblemaBackend(mensaje: String) : ErrorClienteRed(mensaje, null)