package co.smartobjects.red.clientes.base

interface ConsultarAPI<T>
{
    fun consultar(): RespuestaIndividual<T>
}