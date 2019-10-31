package co.smartobjects.entidades.excepciones

open class EntidadMalInicializada(
        val nombreEntidad: String,
        val nombreDelCampo: String,
        val valorUsado: String,
        val descripcionDeLaRestriccion: String,
        causa: Throwable? = null)
    : Exception("El campo '$nombreDelCampo' de '$nombreEntidad' debe cumplir que: $descripcionDeLaRestriccion. Se usó $valorUsado", causa)

class EntidadConCampoDuplicado(
        nombreEntidad: String,
        nombreDelCampo: String,
        nombreCampoMapeado: String,
        valoresDuplicados: Collection<Any?>,
        valorMapeado: Any?,
        causa: Throwable? = null)
    : EntidadMalInicializada(
        nombreEntidad,
        nombreDelCampo,
        "[${valoresDuplicados.joinToString(separator = ", ")}] que mapean a $valorMapeado",
        "'$nombreCampoMapeado' debe ser único en cada elemento en $nombreDelCampo",
        causa
                            )

class EntidadConCampoHeterogeneo(
        nombreEntidad: String,
        nombreDelCampo: String,
        nombreCampoMapeado: String,
        val valoresDiferentes: Set<Any?>,
        causa: Throwable? = null)
    : EntidadMalInicializada(
        nombreEntidad,
        "$nombreCampoMapeado en cada $nombreDelCampo",
        "[${valoresDiferentes.joinToString()}]",
        "Debe tener todos los valores iguales",
        causa
                            )

class EntidadConCampoVacio(
        nombreEntidad: String,
        nombreDelCampo: String,
        causa: Throwable? = null)
    : EntidadMalInicializada(nombreEntidad, nombreDelCampo, "Vacío", "No debe estar vacío", causa)

class EntidadConValorNoPermitido(
        nombreEntidad: String,
        nombreDelCampo: String,
        valorUsado: String,
        val valoresPermitidos: List<String>,
        causa: Throwable? = null)
    : EntidadMalInicializada(nombreEntidad, nombreDelCampo, valorUsado, "Debe tener un valor en $valoresPermitidos", causa)

class EntidadConValorEnColeccionNoPermitido(
        nombreEntidad: String,
        nombreDelCampo: String,
        valorUsado: String,
        coleccionUsada: Collection<String>,
        causa: Throwable? = null)
    : EntidadMalInicializada(nombreEntidad, nombreDelCampo, coleccionUsada.joinToString(), "No debe contener $valorUsado", causa)

class RelacionEntreCamposInvalida(
        nombreEntidad: String,
        val nombreDelCampoIzquierdo: String,
        val nombreDelCampoDerecho: String,
        val valorUsadoPorCampoIzquierdo: String,
        val valorUsadoPorCampoDerecho: String,
        val relacionViolada: Relacion,
        causa: Throwable? = null)
    : EntidadMalInicializada(
        nombreEntidad,
        nombreDelCampoIzquierdo,
        valorUsadoPorCampoIzquierdo,
        "$nombreDelCampoIzquierdo ($valorUsadoPorCampoIzquierdo) debe ser ${relacionViolada.nombreDeRelacion} ${relacionViolada.mensajeDireccionRestriccion} $nombreDelCampoDerecho ($valorUsadoPorCampoDerecho)",
        causa
                            )
{
    enum class Relacion(
            internal val nombreDeRelacion: String,
            internal val mensajeDireccionRestriccion: String
                       )
    {
        IGUAL("IGUAL", "que"), MENOR("MENOR", "que"), NO_VACIO_SIMULTANEO("NO VACÍO", "al mismo tiempo que"), NULO_SI("NULO", "si se usa nulo en");
    }
}

class EntidadConCampoFueraDeRango(
        nombreEntidad: String,
        nombreDelCampo: String,
        valorUsado: String,
        val valorDelLimite: String,
        val limiteSobrepasado: Limite,
        causa: Throwable? = null)
    : EntidadMalInicializada(nombreEntidad, nombreDelCampo, valorUsado, "No puede ser $limiteSobrepasado a $valorDelLimite", causa)
{
    enum class Limite
    {
        SUPERIOR, INFERIOR, DIFERENTE
    }
}