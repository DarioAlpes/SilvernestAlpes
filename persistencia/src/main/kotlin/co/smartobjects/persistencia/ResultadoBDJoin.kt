package co.smartobjects.persistencia

import com.j256.ormlite.support.DatabaseResults
import java.io.InputStream
import java.math.BigDecimal
import java.sql.Timestamp

class ResultadoBDJoin(val resultadoBD: DatabaseResults, val rangoColumnas: IntRange) : DatabaseResults by resultadoBD
{
    override fun findColumn(columnName: String): Int
    {
        return rangoColumnas.first { columnName.equals(resultadoBD.columnNames[it], ignoreCase = true) } - rangoColumnas.first
    }

    override fun getObject(columnIndex: Int): Any?
    {
        return resultadoBD.getObject(columnIndex + rangoColumnas.first)
    }

    override fun getLong(columnIndex: Int): Long
    {
        return resultadoBD.getLong(columnIndex + rangoColumnas.first)
    }

    override fun getFloat(columnIndex: Int): Float
    {
        return resultadoBD.getFloat(columnIndex + rangoColumnas.first)
    }

    override fun wasNull(columnIndex: Int): Boolean
    {
        return resultadoBD.wasNull(columnIndex + rangoColumnas.first)
    }

    override fun getBytes(columnIndex: Int): ByteArray?
    {
        return resultadoBD.getBytes(columnIndex + rangoColumnas.first)
    }

    override fun getDouble(columnIndex: Int): Double
    {
        return resultadoBD.getDouble(columnIndex + rangoColumnas.first)
    }

    override fun getBoolean(columnIndex: Int): Boolean
    {
        return resultadoBD.getBoolean(columnIndex + rangoColumnas.first)
    }

    override fun getBigDecimal(columnIndex: Int): BigDecimal?
    {
        return resultadoBD.getBigDecimal(columnIndex + rangoColumnas.first)
    }

    override fun getColumnNames(): Array<String>
    {
        return resultadoBD.columnNames.sliceArray(rangoColumnas)
    }

    override fun getInt(columnIndex: Int): Int
    {
        return resultadoBD.getInt(columnIndex + rangoColumnas.first)
    }

    override fun getBlobStream(columnIndex: Int): InputStream
    {
        return resultadoBD.getBlobStream(columnIndex + rangoColumnas.first)
    }

    override fun getChar(columnIndex: Int): Char
    {
        return resultadoBD.getChar(columnIndex + rangoColumnas.first)
    }

    override fun getShort(columnIndex: Int): Short
    {
        return resultadoBD.getShort(columnIndex + rangoColumnas.first)
    }

    override fun getByte(columnIndex: Int): Byte
    {
        return resultadoBD.getByte(columnIndex + rangoColumnas.first)
    }

    override fun getString(columnIndex: Int): String?
    {
        return resultadoBD.getString(columnIndex + rangoColumnas.first)
    }

    override fun getColumnCount(): Int
    {
        return rangoColumnas.count()
    }

    override fun getTimestamp(columnIndex: Int): Timestamp?
    {
        return resultadoBD.getTimestamp(columnIndex + rangoColumnas.first)
    }
}