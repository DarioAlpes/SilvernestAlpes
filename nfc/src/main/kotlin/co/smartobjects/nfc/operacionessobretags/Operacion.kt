package co.smartobjects.nfc.operacionessobretags

abstract class Operacion<Resultado>
{
    protected abstract fun operacion(): Resultado

    protected abstract fun procesarResultado(resultado: Resultado)

    fun ejecutarOperacion()
    {
        procesarResultado(operacion())
    }
}