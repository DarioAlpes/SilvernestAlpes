package co.smartobjects.persistencia.basederepositorios

import co.smartobjects.persistencia.CampoTabla
import com.j256.ormlite.field.SqlType
import com.j256.ormlite.stmt.SelectArg


sealed class FiltroSQL(val parametros: Sequence<SelectArg>)
{
    internal abstract fun generarSQL(builder: StringBuilder)

    internal fun generarSQL(): StringBuilder
    {
        return StringBuilder().also {
            generarSQL(it)
        }
    }

    abstract fun or(operando: FiltroSQL): FiltroSQL
    abstract fun and(operando: FiltroSQL): FiltroSQL

    internal abstract class Operando(parametros: List<SelectArg>) : FiltroSQL(parametros.asSequence())
    {
        companion object
        {
            fun parsearArgumento(tipoSql: SqlType, arg: Any?): ArgumentoParseado
            {
                val valor =
                        if (arg == null)
                        {
                            "NULL"
                        }
                        else
                        {
                            when (tipoSql)
                            {
                                SqlType.SHORT,
                                SqlType.INTEGER,
                                SqlType.LONG -> "$arg"
                                else         -> "?"
                            }
                        }

                return ArgumentoParseado(valor, if (valor == "?") SelectArg(tipoSql, arg) else null)
            }
        }

        override fun or(operando: FiltroSQL): FiltroSQL
        {
            return Operacion.Or(operando, this)
        }

        override fun and(operando: FiltroSQL): FiltroSQL
        {
            return Operacion.And(operando, this)
        }

        class ArgumentoParseado(val valorSQL: String, val argumento: SelectArg?)
    }

    internal sealed class Operacion(private val opIzq: FiltroSQL, private val opDer: FiltroSQL)
        : FiltroSQL(opIzq.parametros + opDer.parametros)
    {
        internal fun componer(operador: String, builder: StringBuilder)
        {
            builder.append("(")
            opIzq.generarSQL(builder)
            builder.append(" $operador ")
            opDer.generarSQL(builder)
            builder.append(")")
        }

        override fun or(operando: FiltroSQL): FiltroSQL
        {
            return Operacion.Or(this, operando)
        }

        override fun and(operando: FiltroSQL): FiltroSQL
        {
            return Operacion.And(this, operando)
        }

        class Or internal constructor(opIzq: FiltroSQL, opDer: FiltroSQL) : Operacion(opIzq, opDer)
        {
            override fun generarSQL(builder: StringBuilder)
            {
                componer("OR", builder)
            }
        }

        class And internal constructor(opIzq: FiltroSQL, opDer: FiltroSQL) : Operacion(opIzq, opDer)
        {
            override fun generarSQL(builder: StringBuilder)
            {
                return componer("AND", builder)
            }
        }
    }
}

internal class FiltroInSubQuery private constructor(
        private val campo: CampoTabla,
        private val consultaInterna: ConstructorQueryORMLite.ConsultaParametrizada
                                                   ) : FiltroSQL.Operando(consultaInterna.parametros)
{
    constructor(campo: CampoTabla, constructorSubQuery: ConstructorQueryORMLite<*>)
            : this(campo, constructorSubQuery.generarConsulta())

    override fun generarSQL(builder: StringBuilder)
    {
        builder
            .append(campo.nombreColumna)
            .append(" IN (")
            .append(consultaInterna.sql)
            .append(")")
    }
}

internal class FiltroCampoBooleano(val campo: CampoTabla, private val valorColumna: Boolean)
    : FiltroSQL.Operando(emptyList())
{
    override fun generarSQL(builder: StringBuilder)
    {
        if (valorColumna)
        {
            builder.append(campo.nombreColumna)
        }
        else
        {
            builder.append("NOT ").append(campo.nombreColumna)
        }
    }
}

internal open class FiltroCampoIndividual<out TipoColumna> private constructor
(
        val campo: CampoTabla,
        val valorColumnaUsoExterno: TipoColumna,
        private val valorColumna: String,
        private val operadorSQL: String,
        argumento: SelectArg?
) : FiltroSQL.Operando(if (argumento == null) emptyList() else listOf(argumento))
{
    constructor(campo: CampoTabla, valorColumna: TipoColumna, operadorSQL: String, argumentoParseado: ArgumentoParseado)
            : this(campo, valorColumna, argumentoParseado.valorSQL, operadorSQL, argumentoParseado.argumento)

    constructor(campo: CampoTabla, valorColumna: TipoColumna, tipoSql: SqlType, operadorSQL: String)
            : this(campo, valorColumna, operadorSQL, Operando.parsearArgumento(tipoSql, valorColumna))

    final override fun generarSQL(builder: StringBuilder)
    {
        builder
            .append(campo.nombreColumna)
            .append(" $operadorSQL ")
            .append(valorColumna)
    }
}

internal class FiltroIn<out TipoColumna>
(
        private val campo: CampoTabla,
        private val valoresBuscados: List<String>,
        argumentos: List<SelectArg>
) : FiltroSQL.Operando(argumentos)
{
    constructor(campo: CampoTabla, argumentosParseados: List<ArgumentoParseado>)
            : this(campo, argumentosParseados.map { it.valorSQL }, argumentosParseados.mapNotNull { it.argumento })

    constructor(campo: CampoTabla, valoresBuscados: List<TipoColumna>, tipoSql: SqlType)
            : this(campo, valoresBuscados.map { Operando.parsearArgumento(tipoSql, it) })

    override fun generarSQL(builder: StringBuilder)
    {
        builder
            .append(campo.nombreColumna)
            .append(" IN (")
            .append(valoresBuscados.joinToString())
            .append(")")
    }
}

internal class FiltroIgualdad<out TipoColumna>(
        campo: CampoTabla,
        valorColumna: TipoColumna,
        tipoSql: SqlType)
    : FiltroCampoIndividual<TipoColumna>(campo, valorColumna, tipoSql, if (valorColumna == null) "IS" else "=")

internal class FiltroMenorOIgualQue<out TipoColumna>(
        campo: CampoTabla,
        valorColumna: TipoColumna,
        tipoSql: SqlType)
    : FiltroCampoIndividual<TipoColumna>(campo, valorColumna, tipoSql, "<=")

internal class FiltroMayorOIgualQue<out TipoColumna>(
        campo: CampoTabla,
        valorColumna: TipoColumna,
        tipoSql: SqlType)
    : FiltroCampoIndividual<TipoColumna>(campo, valorColumna, tipoSql, ">=")