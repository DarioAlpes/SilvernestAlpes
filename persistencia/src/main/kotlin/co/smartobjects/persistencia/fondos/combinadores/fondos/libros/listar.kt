package co.smartobjects.persistencia.fondos.combinadores.fondos.libros

import co.smartobjects.persistencia.CampoTabla
import co.smartobjects.persistencia.basederepositorios.FiltroIgualdad
import co.smartobjects.persistencia.fondos.libros.LibroDAO
import com.j256.ormlite.field.SqlType


internal fun darFiltroSobreTipoDeLibro(tipo: LibroDAO.TipoEnBD): List<FiltroIgualdad<LibroDAO.TipoEnBD>>
{
    return listOf(FiltroIgualdad(CampoTabla(LibroDAO.TABLA, LibroDAO.COLUMNA_TIPO_LIBRO), tipo, SqlType.STRING))
}