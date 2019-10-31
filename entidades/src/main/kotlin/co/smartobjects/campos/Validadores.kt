package co.smartobjects.campos

import co.smartobjects.entidades.excepciones.*
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.FECHA_MINIMA_CREACION
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.threeten.bp.ZonedDateTime

class ValidadorCampoStringNoVacio : ValidadorCampo<String>
{
    override fun validarCampo(valorLimpio: String, valorOriginal: String, nombreEntidad: String, nombreCampo: String)
    {
        if (valorLimpio.isEmpty())
        {
            throw EntidadConCampoVacio(nombreEntidad, nombreCampo)
        }
    }

    override fun limpiarValorInicial(valorOriginal: String): String
    {
        return valorOriginal.trim()
    }
}

class ValidadorCampoArregloCharNoVacio : ValidadorCampo<CharArray>
{
    override fun validarCampo(valorLimpio: CharArray, valorOriginal: CharArray, nombreEntidad: String, nombreCampo: String)
    {
        if (valorLimpio.isEmpty())
        {
            throw EntidadConCampoVacio(nombreEntidad, nombreCampo)
        }
    }
}

class ValidadorCampoArregloBytesNoVacio : ValidadorCampo<ByteArray>
{
    override fun validarCampo(valorLimpio: ByteArray, valorOriginal: ByteArray, nombreEntidad: String, nombreCampo: String)
    {
        if (valorLimpio.isEmpty())
        {
            throw EntidadConCampoVacio(nombreEntidad, nombreCampo)
        }
    }
}

class ValidadorMapeoDeCampo<TipoCampo, out TipoMapeo>(
        private val funcionMapeo: (TipoCampo) -> TipoMapeo,
        private val asignarValorMapeoLimpiado: (TipoCampo, TipoMapeo) -> TipoCampo,
        private val nombreCampoMapeo: String,
        private val validadorMapeo: ValidadorCampo<TipoMapeo>
                                                     ) : ValidadorCampo<TipoCampo>
{
    override fun validarCampo(valorLimpio: TipoCampo, valorOriginal: TipoCampo, nombreEntidad: String, nombreCampo: String)
    {
        try
        {
            validadorMapeo.validarCampo(funcionMapeo(valorLimpio), funcionMapeo(valorOriginal), nombreEntidad, nombreCampo)
        }
        catch (ex: EntidadMalInicializada)
        {
            throw EntidadMalInicializada(
                    nombreEntidad,
                    nombreCampo,
                    valorOriginal.toString(),
                    "El $nombreCampoMapeo de $nombreCampo es inválido",
                    ex
                                        )
        }
    }

    override fun limpiarValorInicial(valorOriginal: TipoCampo): TipoCampo
    {
        return asignarValorMapeoLimpiado(valorOriginal, validadorMapeo.limpiarValorInicial(funcionMapeo(valorOriginal)))
    }
}


class ValidadorCampoColeccionNoVacio<out TipoCampo, TipoColeccion : Collection<TipoCampo>> : ValidadorCampo<TipoColeccion>
{
    override fun validarCampo(valorLimpio: TipoColeccion, valorOriginal: TipoColeccion, nombreEntidad: String, nombreCampo: String)
    {
        if (valorLimpio.isEmpty())
        {
            throw EntidadConCampoVacio(nombreEntidad, nombreCampo)
        }
    }
}

class ValidadorCampoIgualEnColeccion<out TipoCampo, TipoColeccion : Collection<TipoCampo>, out TipoMapeo>
(
        private val nombreCampoMapeado: String,
        private val funcionMapeo: (TipoCampo) -> TipoMapeo
) : ValidadorCampo<TipoColeccion>
{
    override fun validarCampo(valorLimpio: TipoColeccion, valorOriginal: TipoColeccion, nombreEntidad: String, nombreCampo: String)
    {
        val valoresVistos = mutableSetOf<TipoMapeo>()

        valorLimpio.mapTo(valoresVistos, funcionMapeo)

        if (valoresVistos.size > 1)
        {
            throw EntidadConCampoHeterogeneo(nombreEntidad, nombreCampo, nombreCampoMapeado, valoresVistos)
        }
    }
}

class ValidadorNoPuedeContenerElemento<out TipoCampo, TipoColeccion : Collection<TipoCampo>>
(
        private val elementoProhibidoEnColeccion: TipoCampo
) : ValidadorCampo<TipoColeccion>
{
    override fun validarCampo(valorLimpio: TipoColeccion, valorOriginal: TipoColeccion, nombreEntidad: String, nombreCampo: String)
    {
        if (valorLimpio.contains(elementoProhibidoEnColeccion))
        {
            throw EntidadConValorEnColeccionNoPermitido(
                    nombreEntidad,
                    nombreCampo,
                    elementoProhibidoEnColeccion.toString(),
                    valorLimpio.map { it.toString() }
                                                       )
        }
    }
}

internal val validadorDeZonaHoraria =
        ValidadorMapeoDeCampo<ZonedDateTime, String>(
                { it.zone.id },
                { fecha, _ -> fecha },
                "Id de la zona horaria",
                ValidadorCampoConUnicoValorPermitido(ZONA_HORARIA_POR_DEFECTO.id)
                                                    )

/**
 * Agrupa los campos según la funcion de agrupación y revisa que cada grupo tenga tamaño de 1. Dos elementos E1 y E2 van a quedar en el mismo grupo
 * si y solo si funcionAgrupacion(E1) == funcionAgrupacion(E2).
 * Si no se especifica ninguna función de agrupación se usa la funcion de identidad. Es decir, dos elementos E1 y E2 van a quedar en el mismo grupo
 * si y solo si E1 == E2, i.e. el validador revisa que no existan elementos duplicados en la colección
 *
 * Si se ignoran los nulos no se contabilizan.
 */
class ValidadorCampoColeccionSinRepetidos<out TipoCampo : Any?, TipoColeccion : Collection<TipoCampo>>
(
        private val ignorarNulos: Boolean,
        private val nombreCampoMapeado: String,
        private val funcionAgrupacion: (TipoCampo) -> Any? = { it }
) : ValidadorCampo<TipoColeccion>
{
    override fun validarCampo(valorLimpio: TipoColeccion, valorOriginal: TipoColeccion, nombreEntidad: String, nombreCampo: String)
    {
        val primerGrupoDuplicado =
                valorLimpio
                    .groupBy(funcionAgrupacion)
                    .asSequence()
                    .let {
                        if (ignorarNulos) it.filter { it.key != null } else it
                    }
                    .firstOrNull {
                        it.value.size > 1
                    }

        if (primerGrupoDuplicado != null)
        {
            throw EntidadConCampoDuplicado(nombreEntidad, nombreCampo, nombreCampoMapeado, primerGrupoDuplicado.value, primerGrupoDuplicado.key)
        }
    }
}

class ValidadorCampoDosColeccionesAlMenosUnElemento<
        out TipoCampoIzq : Any?, TipoColeccionIzq : Collection<TipoCampoIzq>,
        out TipoCampoDer : Any?, TipoColeccionDer : Collection<TipoCampoDer>>(
        private val valorOtroCampo: TipoColeccionDer,
        private val nombreCampoComparacion: String)
    : ValidadorCampo<TipoColeccionIzq>
{
    override fun validarCampo(valorLimpio: TipoColeccionIzq, valorOriginal: TipoColeccionIzq, nombreEntidad: String, nombreCampo: String)
    {
        if (valorLimpio.isEmpty() && valorOtroCampo.isEmpty())
        {
            throw RelacionEntreCamposInvalida(
                    nombreEntidad,
                    nombreCampo,
                    nombreCampoComparacion,
                    valorOriginal.toString(),
                    valorOtroCampo.toString(),
                    RelacionEntreCamposInvalida.Relacion.NO_VACIO_SIMULTANEO
                                             )
        }
    }
}

internal val validadorDecimalMayorACero = ValidadorCampoConLimiteInferior<Decimal, Decimal>(Decimal(0))
internal val validadorFechaMayorAMinima = ValidadorCampoConLimiteInferior<ZonedDateTime, ZonedDateTime>(FECHA_MINIMA_CREACION)

class ValidadorCampoConLimiteInferior<in TipoLimite, TipoCampo : Comparable<TipoLimite>>(private val limiteInferior: TipoLimite) : ValidadorCampo<TipoCampo>
{
    override fun validarCampo(valorLimpio: TipoCampo, valorOriginal: TipoCampo, nombreEntidad: String, nombreCampo: String)
    {
        if (valorLimpio < limiteInferior)
        {
            throw EntidadConCampoFueraDeRango(
                    nombreEntidad,
                    nombreCampo,
                    valorOriginal.toString(),
                    limiteInferior.toString(),
                    EntidadConCampoFueraDeRango.Limite.INFERIOR)
        }
    }
}

class ValidadorCampoEsMenorOIgualQueOtroCampo<in TipoCampoComparacion, TipoCampo : Comparable<TipoCampoComparacion>>(
        private val valorOtroCampo: TipoCampoComparacion,
        private val nombreCampoComparacion: String)
    : ValidadorCampo<TipoCampo>
{
    override fun validarCampo(valorLimpio: TipoCampo, valorOriginal: TipoCampo, nombreEntidad: String, nombreCampo: String)
    {
        if (valorLimpio > valorOtroCampo)
        {
            throw RelacionEntreCamposInvalida(
                    nombreEntidad,
                    nombreCampo,
                    nombreCampoComparacion,
                    valorOriginal.toString(),
                    valorOtroCampo.toString(),
                    RelacionEntreCamposInvalida.Relacion.MENOR
                                             )
        }
    }

}

class ValidadorCampoConLimiteSuperior<in TipoLimite, TipoCampo : Comparable<TipoLimite>>(private val limiteSuperior: TipoLimite) : ValidadorCampo<TipoCampo>
{
    override fun validarCampo(valorLimpio: TipoCampo, valorOriginal: TipoCampo, nombreEntidad: String, nombreCampo: String)
    {
        if (valorLimpio > limiteSuperior)
        {
            throw EntidadConCampoFueraDeRango(
                    nombreEntidad,
                    nombreCampo,
                    valorOriginal.toString(),
                    limiteSuperior.toString(),
                    EntidadConCampoFueraDeRango.Limite.SUPERIOR)
        }
    }
}

class ValidadorCampoEsNuloOCumpleValidacion<TipoCampo>(private val validadorCampo: ValidadorCampo<TipoCampo>) : ValidadorCampo<TipoCampo?>
{
    override fun validarCampo(valorLimpio: TipoCampo?, valorOriginal: TipoCampo?, nombreEntidad: String, nombreCampo: String)
    {
        if (valorLimpio != null)
        {
            validadorCampo.validarCampo(valorLimpio, valorOriginal ?: valorLimpio, nombreEntidad, nombreCampo)
        }
    }

    override fun limpiarValorInicial(valorOriginal: TipoCampo?): TipoCampo?
    {
        return if (valorOriginal != null)
        {
            validadorCampo.limpiarValorInicial(valorOriginal)
        }
        else
        {
            valorOriginal
        }
    }
}

class ValidadorCampoConValorPermitido<TipoCampo>(private val valoresPermitidos: Iterable<TipoCampo>) : ValidadorCampo<TipoCampo>
{
    override fun validarCampo(valorLimpio: TipoCampo, valorOriginal: TipoCampo, nombreEntidad: String, nombreCampo: String)
    {
        if (!valoresPermitidos.any { it == valorLimpio })
        {
            throw EntidadConValorNoPermitido(nombreEntidad, nombreCampo, valorOriginal.toString(), valoresPermitidos.map { it.toString() })
        }
    }
}

class ValidadorCampoConUnicoValorPermitido<TipoCampo>(private val valorPermitido: TipoCampo) : ValidadorCampo<TipoCampo>
{
    override fun validarCampo(valorLimpio: TipoCampo, valorOriginal: TipoCampo, nombreEntidad: String, nombreCampo: String)
    {
        ValidadorCampoConValorPermitido(listOf(valorPermitido)).validarCampo(valorLimpio, valorOriginal, nombreEntidad, nombreCampo)
    }
}

class ValidadorCampoDeReferenciaNoNulo<TipoCampo>(
        private val valorOtroCampo: TipoCampo?,
        private val nombreCampoComparacion: String)
    : ValidadorCampo<TipoCampo>
{
    override fun validarCampo(valorLimpio: TipoCampo, valorOriginal: TipoCampo, nombreEntidad: String, nombreCampo: String)
    {
        if (valorOtroCampo == null)
        {
            throw RelacionEntreCamposInvalida(
                    nombreEntidad,
                    nombreCampo,
                    nombreCampoComparacion,
                    valorOriginal.toString(),
                    valorOtroCampo.toString(),
                    RelacionEntreCamposInvalida.Relacion.NULO_SI
                                             )
        }
    }

}

class ValidadorCondicional<TipoCampo>(
        private val aplicaValidacion: Boolean,
        private val validadorCampo: ValidadorCampo<TipoCampo>
                                     ) : ValidadorCampo<TipoCampo>
{
    override fun validarCampo(valorLimpio: TipoCampo, valorOriginal: TipoCampo, nombreEntidad: String, nombreCampo: String)
    {
        if (aplicaValidacion)
        {
            validadorCampo.validarCampo(valorLimpio, valorOriginal, nombreEntidad, nombreCampo)
        }
    }

    override fun limpiarValorInicial(valorOriginal: TipoCampo): TipoCampo
    {
        return if (aplicaValidacion)
        {
            validadorCampo.limpiarValorInicial(valorOriginal)
        }
        else
        {
            valorOriginal
        }
    }
}

class ValidadorEnCadena<TipoCampo>(private val validadores: List<ValidadorCampo<TipoCampo>>) : ValidadorCampo<TipoCampo>
{
    constructor(vararg validadores: ValidadorCampo<TipoCampo>) : this(validadores.toList())

    override fun limpiarValorInicial(valorOriginal: TipoCampo): TipoCampo
    {
        return validadores.fold(valorOriginal, { valorActual, validador -> validador.limpiarValorInicial(valorActual) })
    }

    override fun validarCampo(valorLimpio: TipoCampo, valorOriginal: TipoCampo, nombreEntidad: String, nombreCampo: String)
    {   //print("\nvalorLimpio "+valorLimpio)
        validadores.forEach { it.validarCampo(valorLimpio, valorOriginal, nombreEntidad, nombreCampo) }
    }
}