@file:Suppress("NOTHING_TO_INLINE")

package co.smartobjects.persistencia.fondos.combinadores.fondos.libros

import co.smartobjects.persistencia.CampoTabla
import co.smartobjects.persistencia.basederepositorios.FiltroIgualdad
import co.smartobjects.persistencia.fondos.libros.LibroDAO
import com.j256.ormlite.field.SqlType


private inline fun crearFiltroDeLibrosSegunTipo(tipo: LibroDAO.TipoEnBD) =
        FiltroIgualdad(CampoTabla(LibroDAO.TABLA, LibroDAO.COLUMNA_TIPO_LIBRO), tipo, SqlType.STRING)

internal inline fun crearFiltroSoloLibrosDePrecio() = sequenceOf(crearFiltroDeLibrosSegunTipo(LibroDAO.TipoEnBD.PRECIOS))
internal inline fun crearFiltroSoloLibrosDeProhibiciones() = sequenceOf(crearFiltroDeLibrosSegunTipo(LibroDAO.TipoEnBD.PROHIBICIONES))