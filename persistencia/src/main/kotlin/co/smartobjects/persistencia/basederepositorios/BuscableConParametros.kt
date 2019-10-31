package co.smartobjects.persistencia.basederepositorios

import co.smartobjects.persistencia.excepciones.ErrorDAO

interface BuscableConParametros<out EntidadDeNegocio, in Parametros : ParametrosConsulta>
{
    @Throws(ErrorDAO::class)
    fun buscarSegunParametros(idCliente: Long, parametros: Parametros): EntidadDeNegocio?
}

internal class BuscableConParametrosSegunListableFiltrableConParametros<out EntidadDeNegocio, in Parametros : ParametrosConsulta>
(
        private val listadorEntidad: ListableConParametros<EntidadDeNegocio, Parametros>
) : BuscableConParametros<EntidadDeNegocio, Parametros>
{
    override fun buscarSegunParametros(idCliente: Long, parametros: Parametros): EntidadDeNegocio?
    {   print("buscarSegunParametros")
        // Se usa toList para forzar a que consuma la lista en caso de transacciones
        return listadorEntidad.listarSegunParametros(idCliente, parametros).toList().firstOrNull()
    }
}

@Suppress("unused")
internal interface ValidadorRestriccionBuscableParametros<in EntidadNegocio, in Parametros : ParametrosConsulta>
{
    fun validar(idCliente: Long, parametros: Parametros)
}

internal class BuscableConParametrosConRestriccion<out EntidadDeNegocio, in Parametros : ParametrosConsulta>
(
        private val buscador: BuscableConParametros<EntidadDeNegocio, Parametros>,
        private val validadorRestriccion: ValidadorRestriccionBuscableParametros<EntidadDeNegocio, Parametros>
) : BuscableConParametros<EntidadDeNegocio, Parametros>
{
    override fun buscarSegunParametros(idCliente: Long, parametros: Parametros): EntidadDeNegocio?
    {
        validadorRestriccion.validar(idCliente, parametros)
        // Se usa toList para forzar a que consuma la lista en caso de transacciones
        return buscador.buscarSegunParametros(idCliente, parametros)
    }
}