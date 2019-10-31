package co.smartobjects.persistencia.basederepositorios

import co.smartobjects.persistencia.ConfiguracionRepositorios
import co.smartobjects.persistencia.excepciones.ErrorDAO
import com.j256.ormlite.misc.TransactionManager
import java.sql.SQLException


private inline fun <T> transaccionEnEsquemaClienteYProcesarErrores(
        configuracion: ConfiguracionRepositorios,
        idCliente: Long,
        nombreEntidad: String,
        noinline funcionAEjecutar: () -> T,
        creadorDeExcepcion: (SQLException, String) -> ErrorDAO): T
{
    return try
    {
        TransactionManager.callInTransaction(
                configuracion.darFuenteDeConexionesParaLlave(darNombreEsquemaSegunIdCliente(idCliente)),
                funcionAEjecutar
                                            )
    }
    catch (e: SQLException)
    {
        throw creadorDeExcepcion(e, nombreEntidad)
    }
}

fun <T> transaccionEnEsquemaClienteYProcesarErroresParaCreacion(
        configuracion: ConfiguracionRepositorios,
        idCliente: Long,
        nombreEntidad: String,
        funcionAEjecutar: () -> T): T
{
    return transaccionEnEsquemaClienteYProcesarErrores(
            configuracion,
            idCliente,
            nombreEntidad,
            funcionAEjecutar,
            { e, nombre -> configuracion.mapeadorCodigosError.parsearCodigoDeErrorSQLParaCreacion(e, nombre) }
                                                      )
}

fun <T> transaccionEnEsquemaClienteYProcesarErroresParaConsulta(
        configuracion: ConfiguracionRepositorios,
        idCliente: Long,
        nombreEntidad: String,
        funcionAEjecutar: () -> T): T
{
    return transaccionEnEsquemaClienteYProcesarErrores(
            configuracion,
            idCliente,
            nombreEntidad,
            funcionAEjecutar,
            { e, nombre -> configuracion.mapeadorCodigosError.parsearCodigoDeErrorSQLParaConsulta(e, nombre) }
                                                      )
}

fun <T> transaccionEnEsquemaClienteYProcesarErroresParaActualizacion(
        configuracion: ConfiguracionRepositorios,
        idCliente: Long,
        nombreEntidad: String,
        funcionAEjecutar: () -> T): T
{
    return transaccionEnEsquemaClienteYProcesarErrores(
            configuracion,
            idCliente,
            nombreEntidad,
            funcionAEjecutar,
            { e, nombre -> configuracion.mapeadorCodigosError.parsearCodigoDeErrorSQLParaActualizacion(e, nombre) }
                                                      )
}

fun <T> transaccionEnEsquemaClienteYProcesarErroresParaEliminacion(
        configuracion: ConfiguracionRepositorios,
        idCliente: Long,
        nombreEntidad: String,
        funcionAEjecutar: () -> T
                                                                  ): T
{
    return transaccionEnEsquemaClienteYProcesarErrores(
            configuracion,
            idCliente,
            nombreEntidad,
            funcionAEjecutar,
            { e, nombre -> configuracion.mapeadorCodigosError.parsearCodigoDeErrorSQLParaEliminacion(e, nombre) }
                                                      )
}