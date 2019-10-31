package co.smartobjects.prompterbackend.generadorrutas

import co.smartobjects.prompterbackend.serviciosrest.clientes.RecursoClientes
import co.smartobjects.prompterbackend.serviciosrest.usuariosglobales.RecursoUsuariosGlobales
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.File
import java.io.IOException
import javax.ws.rs.*
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.jvm.jvmErasure


internal object GeneradorRutasAPIRunner
{
    @[JvmStatic Throws(IOException::class)]
    fun main(args: Array<String>)
    {
        /*
        Para generar directamente en el src de red-cliente
        val rutaDeSalida =
                File(
                        File(System.getProperty("user.dir")).parentFile.absolutePath,
                        "red-clientes/src/main/kotlin/co/smartobjects/red/clientes/codigogeneradoporbackend"
                    ).absolutePath
         */

        val rutaDeSalida = File(System.getProperty("user.dir"), """out/codigo_generado""").absolutePath
        File(rutaDeSalida).mkdirs()
        GeneradorRutasAPI().generar(rutaDeSalida)
    }
}

internal class GeneradorRutasAPI
{
    companion object
    {
        private val NOMBRE_CLASES_RECURSOS_RAIZ = listOf<KClass<*>>(RecursoClientes::class, RecursoUsuariosGlobales::class)
    }

    private fun darPropertySpecStringConst(nombre: String, valor: String): PropertySpec
    {
        return PropertySpec.builder(nombre, String::class, KModifier.CONST)
            .initializer("\"$valor\"")
            .build()
    }

    private fun generarNombreObjetoApi(nombreRecurso: String) = "Ruteo${nombreRecurso}Api"

    private fun darAnotacionPath(elementoAnotado: KAnnotatedElement) = elementoAnotado.annotations.first { it is Path } as Path

    private fun darMetodosDeclaradosAnotadosConPath(clase: KClass<*>): Sequence<KFunction<*>>
    {
        return clase.declaredFunctions.asSequence().filter { it.annotations.any { it.annotationClass == Path::class } }
    }

    private fun crearObjetoDePosibleOperaciones(anotacionesConDueñoDeAnotacion: List<InformacionDeTipoEnRuta>): TypeSpec.Builder
    {
        val objetoConOperaciones = TypeSpec.objectBuilder("Operaciones")

        val agregarPropiedadDeOperacionSiTieneAnotacionDeVerbo = { anotacionDeVerboHTTP: KClass<out Annotation> ->
            if (anotacionesConDueñoDeAnotacion.any { it.anotacion.annotationClass == anotacionDeVerboHTTP })
            {
                val objetoOperacionConParametros = TypeSpec.objectBuilder(anotacionDeVerboHTTP.simpleName!!)

                with(anotacionesConDueñoDeAnotacion.filter { it.anotacion.annotationClass == QueryParam::class })
                {
                    if (isNotEmpty())
                    {
                        val parametrosYaAgregados = mutableSetOf<String>()

                        for (anotacionConDueño in this)
                        {
                            val anotacionQuery = anotacionConDueño.anotacion as QueryParam
                            val parametroAnotadoConQueryParam = anotacionConDueño as InformacionDeTipoEnRuta.Parametro
                            val nombreParametroParaQuery = parametroAnotadoConQueryParam.parametro.name!!

                            if (parametrosYaAgregados.add(nombreParametroParaQuery))
                            {
                                objetoOperacionConParametros
                                    .addProperty(darPropertySpecStringConst(nombreParametroParaQuery, anotacionQuery.value))

                                // Agregar tipo del parámetro en el backend
                                val nombreDelCampoQueIndicaElTipo = nombreParametroParaQuery + "_tipo"
                                val tipoDelParametro = parametroAnotadoConQueryParam.parametro.type.toString()
                                objetoOperacionConParametros
                                    .addProperty(darPropertySpecStringConst(nombreDelCampoQueIndicaElTipo, tipoDelParametro))
                            }
                        }
                    }
                }

                objetoConOperaciones.addType(objetoOperacionConParametros.build())
            }
        }

        agregarPropiedadDeOperacionSiTieneAnotacionDeVerbo(DELETE::class)
        agregarPropiedadDeOperacionSiTieneAnotacionDeVerbo(GET::class)
        agregarPropiedadDeOperacionSiTieneAnotacionDeVerbo(PATCH::class)
        agregarPropiedadDeOperacionSiTieneAnotacionDeVerbo(POST::class)
        agregarPropiedadDeOperacionSiTieneAnotacionDeVerbo(PUT::class)

        return objetoConOperaciones
    }

    sealed class InformacionDeTipoEnRuta(val anotacion: Annotation)
    {
        class Funcion(val funcion: KFunction<*>, anotacion: Annotation) : InformacionDeTipoEnRuta(anotacion)
        class Parametro(val parametro: KParameter, anotacion: Annotation) : InformacionDeTipoEnRuta(anotacion)
    }

    private fun agregarReferenciaDePosiblesOperaciones(objetoConRutas: TypeSpec.Builder, claseAsociadaRecursoRaiz: KClass<*>)
    {
        val funcionesDeclaradasEnTotal: Set<KFunction<*>> =
                claseAsociadaRecursoRaiz
                    .allSuperclasses
                    .flatMap { it.declaredFunctions }
                    .asSequence()
                    .plus(claseAsociadaRecursoRaiz.declaredMemberFunctions)
                    .toSet()

        val anotacionesEnMetodosYEnParametrosDeMetodos = mutableListOf<InformacionDeTipoEnRuta>()

        for (funcion in funcionesDeclaradasEnTotal)
        {
            for (anotacion in funcion.annotations)
            {
                anotacionesEnMetodosYEnParametrosDeMetodos.add(InformacionDeTipoEnRuta.Funcion(funcion, anotacion))
            }
            for (parametro in funcion.parameters)
            {
                for (anotacion in parametro.annotations)
                {
                    if (parametro.name != null)
                    {
                        anotacionesEnMetodosYEnParametrosDeMetodos.add(InformacionDeTipoEnRuta.Parametro(parametro, anotacion))
                    }
                }
            }
        }

        val objetoConOperaciones = crearObjetoDePosibleOperaciones(anotacionesEnMetodosYEnParametrosDeMetodos.toList())

        objetoConRutas.addType(objetoConOperaciones.build())
    }

    private fun generarParaRecurso(identificadorRutaResueltaPadre: String?, ruta: String, claseDelRecurso: KClass<*>): TypeSpec.Builder
    {
        val nombreDelRecursoEditado = claseDelRecurso.java.simpleName.replace("Recurso", "")
        val nombreObjetoRutas = generarNombreObjetoApi(nombreDelRecursoEditado)
        val objetoConRutas = TypeSpec.objectBuilder(nombreObjetoRutas)

        objetoConRutas.addModifiers(KModifier.INTERNAL)

        val rutaTieneParametro = ruta.matches("""\{\w+\}""".toRegex()) //ruta.contains("[{}]".toRegex())

        if (rutaTieneParametro)
        {
            val rutaLimpiada = ruta.replace("[{}]".toRegex(), "")
            val rutaConPosfijoDelRecurso = "${rutaLimpiada}_$nombreDelRecursoEditado".toLowerCase()

            objetoConRutas.addProperty(darPropertySpecStringConst("RUTA", "{$rutaConPosfijoDelRecurso}"))

            objetoConRutas.addProperty(darPropertySpecStringConst("PARAMETRO_RUTA", rutaConPosfijoDelRecurso))
        }
        else
        {
            objetoConRutas.addProperty(darPropertySpecStringConst("RUTA", ruta))
        }

        val valorDeRutaResuelta = if (identificadorRutaResueltaPadre == null) "\$RUTA" else "\${$identificadorRutaResueltaPadre}/\$RUTA"
        objetoConRutas.addProperty(darPropertySpecStringConst("RUTA_RESUELTA", valorDeRutaResuelta))

        agregarReferenciaDePosiblesOperaciones(objetoConRutas, claseDelRecurso)

        val funcionesQueRutean =
                darMetodosDeclaradosAnotadosConPath(claseDelRecurso)
                    .partition { it.returnType.jvmErasure.simpleName?.contains("Recurso") == true } // TODO_frnusmartobjects (16/03/2018): Hacer más robusto

        val funcionesQueRuteanYRetornanRecurso = funcionesQueRutean.first
        val funcionesQueRuteanPeroNoRetornanRecurso = funcionesQueRutean.second

        if (funcionesQueRuteanYRetornanRecurso.isEmpty() && funcionesQueRuteanPeroNoRetornanRecurso.isEmpty())
        {
            return objetoConRutas
        }

        for (funcion in funcionesQueRuteanPeroNoRetornanRecurso)
        {
            val nombreRecursoRuteadoFinal = generarNombreObjetoApi(funcion.name.capitalize())
            val objetoRecursoRuteadoFinal =
                    TypeSpec.objectBuilder(nombreRecursoRuteadoFinal)
                        .addProperty(darPropertySpecStringConst("RUTA", darAnotacionPath(funcion).value))
                        .addProperty(darPropertySpecStringConst("RUTA_RESUELTA", "\${$nombreObjetoRutas.RUTA_RESUELTA}/\$RUTA"))

            val objetoConOperaciones = crearObjetoDePosibleOperaciones(funcion.annotations.map { InformacionDeTipoEnRuta.Funcion(funcion, it) })

            objetoRecursoRuteadoFinal.addType(objetoConOperaciones.build())

            objetoConRutas.addType(objetoRecursoRuteadoFinal.build())
        }

        for (funcion in funcionesQueRuteanYRetornanRecurso)
        {
            val rutaRecursoHijo = darAnotacionPath(funcion).value
            val claseRecursoHijo = funcion.returnType.jvmErasure

            val objetoConRutasDeRecursoHijo = generarParaRecurso("$nombreObjetoRutas.RUTA_RESUELTA", rutaRecursoHijo, claseRecursoHijo)

            objetoConRutas.addType(objetoConRutasDeRecursoHijo.build())
        }

        return objetoConRutas
    }

    fun generar(rutaDeSalida: String)
    {
        val archivoConRutas = FileSpec.builder("", "PrompterRutasAPI")

        for (claseAsociadaRecursoRaiz in NOMBRE_CLASES_RECURSOS_RAIZ)
        {
            val anotacionPath = darAnotacionPath(claseAsociadaRecursoRaiz)

            val objetoConRutas = generarParaRecurso(null, anotacionPath.value, claseAsociadaRecursoRaiz)

            archivoConRutas.addType(objetoConRutas.build())
        }

        archivoConRutas.build().writeTo(File(rutaDeSalida))
    }
}