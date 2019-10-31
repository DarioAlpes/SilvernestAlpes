package co.smartobjects.nfc.windows.pcsc.repositoriollavesnfc

import co.smartobjects.entidades.clientes.Cliente
import co.smartobjects.persistencia.basederepositorios.BuscableConParametros
import co.smartobjects.persistencia.basederepositorios.Creable
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorio
import co.smartobjects.persistencia.basederepositorios.EliminablePorParametros
import co.smartobjects.persistencia.clientes.llavesnfc.FiltroLlavesNFC
import co.smartobjects.persistencia.clientes.llavesnfc.RepositorioLlavesNFC
import co.smartobjects.persistencia.excepciones.ErrorDeConsultaEntidad
import co.smartobjects.persistencia.excepciones.ErrorDeCreacionActualizacionEntidad
import javax.naming.OperationNotSupportedException


class RepositorioLlavesNFCWindows
(
        private val repositorioLlavesNFC: RepositorioLlavesNFC,
        private val proveedorLlaveCifradoLlavesNFC: ProveedorLlaveCifradoLlavesNFC
) : CreadorRepositorio<Cliente.LlaveNFC> by repositorioLlavesNFC,
    Creable<Cliente.LlaveNFC>,
    BuscableConParametros<Cliente.LlaveNFC, FiltroLlavesNFC>,
    EliminablePorParametros<Cliente.LlaveNFC, FiltroLlavesNFC>,
    RepositorioLlavesNFC
{
    override val nombreEntidad = repositorioLlavesNFC.nombreEntidad


    override fun crear(idCliente: Long, entidadACrear: Cliente.LlaveNFC): Cliente.LlaveNFC
    {
        proveedorLlaveCifradoLlavesNFC.generarYGuardarLLaveMaestraNFCSiNoExiste()

        val llaveNFCMaestraCifrada = proveedorLlaveCifradoLlavesNFC.cifrarConLlaveDelKeystore(String(entidadACrear.llave))

        return if (llaveNFCMaestraCifrada != null)
        {
            val llaveAGuardar = Cliente.LlaveNFC(idCliente, llaveNFCMaestraCifrada.llaveConIV)

            repositorioLlavesNFC.crear(idCliente, llaveAGuardar)
        }
        else
        {
            throw ErrorDeCreacionActualizacionEntidad("No se pudo guardar la llave maestra de NFC")
        }
    }

    override fun buscarSegunParametros(idCliente: Long, parametros: FiltroLlavesNFC): Cliente.LlaveNFC?
    {
        return repositorioLlavesNFC.buscarSegunParametros(idCliente, parametros)?.let {

            val llaveCifrada = ProveedorLlaveCifradoLlavesNFC.LlaveMaestraNFCCifrada.desdeLlaveConIV(String(it.llave))

            val llaveDescifrada =
                    proveedorLlaveCifradoLlavesNFC.descifrarConLlaveDelKeystore(llaveCifrada)
                    ?: throw ErrorDeConsultaEntidad("No se pudo descifrar la llave maestra de NFC")

            Cliente.LlaveNFC(idCliente, llaveDescifrada)
        }
    }

    override fun eliminarSegunFiltros(idCliente: Long, parametros: FiltroLlavesNFC): Boolean
    {
        throw OperationNotSupportedException("No se permite por el momento borrar llaves maestras de NFC")
    }
}