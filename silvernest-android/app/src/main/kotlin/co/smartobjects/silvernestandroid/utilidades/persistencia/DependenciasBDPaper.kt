package co.smartobjects.silvernestandroid.utilidades.persistencia

import co.smartobjects.entidades.clientes.Cliente
import co.smartobjects.entidades.ubicaciones.Ubicacion
import co.smartobjects.entidades.ubicaciones.contabilizables.UbicacionesContabilizables
import co.smartobjects.persistencia.clientes.llavesnfc.FiltroLlavesNFC
import co.smartobjects.persistencia.clientes.llavesnfc.RepositorioLlavesNFC
import co.smartobjects.persistencia.ubicaciones.RepositorioUbicaciones
import co.smartobjects.persistencia.ubicaciones.contabilizables.RepositorioUbicacionesContabilizables
import io.paperdb.Paper

class DependenciasBDPaper : DependenciasBD
{
    override val repositorioLlavesNFC: RepositorioLlavesNFC =
            object : RepositorioLlavesNFC
            {
                override val nombreEntidad: String = Cliente.LlaveNFC.NOMBRE_ENTIDAD

                override fun inicializarParaCliente(idCliente: Long)
                {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun limpiarParaCliente(idCliente: Long)
                {
                    Paper.book(nombreEntidad).destroy()
                }

                override fun crear(idCliente: Long, entidadACrear: Cliente.LlaveNFC): Cliente.LlaveNFC
                {
                    Paper.book(nombreEntidad).write(nombreEntidad, entidadACrear)

                    return entidadACrear
                }

                override fun buscarSegunParametros(idCliente: Long, parametros: FiltroLlavesNFC): Cliente.LlaveNFC?
                {
                    return Paper.book(nombreEntidad).read(nombreEntidad)
                }

                override fun eliminarSegunFiltros(idCliente: Long, parametros: FiltroLlavesNFC): Boolean
                {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            }

    override val repositorioUbicaciones: RepositorioUbicaciones =
            object : RepositorioUbicaciones
            {
                override val nombreEntidad: String = Ubicacion.NOMBRE_ENTIDAD

                override fun inicializarParaCliente(idCliente: Long)
                {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun limpiarParaCliente(idCliente: Long)
                {
                    Paper.book(nombreEntidad).destroy()
                }

                override fun crear(idCliente: Long, entidadACrear: Ubicacion): Ubicacion
                {
                    Paper.book(nombreEntidad).write(entidadACrear.id!!.toString(), entidadACrear)

                    return entidadACrear
                }

                override fun listar(idCliente: Long): Sequence<Ubicacion>
                {
                    return Paper.book(nombreEntidad).allKeys.asSequence().map { Paper.book(nombreEntidad).read<Ubicacion>(it) }
                }

                override fun buscarPorId(idCliente: Long, id: Long): Ubicacion?
                {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun actualizar(idCliente: Long, idEntidad: Long, entidadAActualizar: Ubicacion): Ubicacion
                {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun eliminarPorId(idCliente: Long, id: Long): Boolean
                {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            }

    override val repositorioUbicacionesContabilizables: RepositorioUbicacionesContabilizables =
            object : RepositorioUbicacionesContabilizables
            {
                override val nombreEntidad: String = UbicacionesContabilizables.NOMBRE_ENTIDAD

                override fun inicializarParaCliente(idCliente: Long)
                {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun limpiarParaCliente(idCliente: Long)
                {
                    Paper.book(nombreEntidad).destroy()
                }

                override fun crear(idCliente: Long, entidadACrear: UbicacionesContabilizables): List<Long>
                {
                    val lista = entidadACrear.idsUbicaciones.toList()

                    Paper.book(nombreEntidad).write(nombreEntidad, lista)

                    return lista
                }

                override fun listar(idCliente: Long): Sequence<Long>
                {
                    return Paper.book(nombreEntidad).read<List<Long>>(nombreEntidad).asSequence()
                }
            }
}