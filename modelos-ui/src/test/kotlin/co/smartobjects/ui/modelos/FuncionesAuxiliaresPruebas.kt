package co.smartobjects.ui.modelos

import io.reactivex.functions.Predicate
import io.reactivex.subjects.PublishSubject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class FuncionesAuxiliaresPruebas
{
    @Test
    fun ultimoEmitido_funciona_correctamente()
    {
        val sujetoParaPruebas = PublishSubject.create<Long>()
        val observadorDePrueba = sujetoParaPruebas.test()
        val secuenciaDeNumeros = List(5) { Thread.sleep(30); System.nanoTime() }

        secuenciaDeNumeros.forEach { sujetoParaPruebas.onNext(it) }

        assertEquals(secuenciaDeNumeros.last(), observadorDePrueba.ultimoEmitido())
    }

    @Test
    fun verificarUltimoValorEmitido_funciona_correctamente()
    {
        val sujetoParaPruebas = PublishSubject.create<Long>()
        val observadorDePrueba = sujetoParaPruebas.test()
        val secuenciaDeNumeros = List(5) { Thread.sleep(30); System.nanoTime() }

        secuenciaDeNumeros.forEach { sujetoParaPruebas.onNext(it) }

        secuenciaDeNumeros.forEachIndexed { index, valor ->
            if (index != secuenciaDeNumeros.lastIndex)
            {
                assertThrows<AssertionError> { observadorDePrueba.verificarUltimoValorEmitido(valor) }
            }
            else
            {
                observadorDePrueba.verificarUltimoValorEmitido(valor)
            }
        }
    }

    @Test
    fun verificarUltimoValorEmitido_usando_predicado_funciona_correctamente()
    {
        val sujetoParaPruebas = PublishSubject.create<Long>()
        val observadorDePrueba = sujetoParaPruebas.test()
        val secuenciaDeNumeros = List(5) { Thread.sleep(30); System.nanoTime() }

        secuenciaDeNumeros.forEach { sujetoParaPruebas.onNext(it) }

        secuenciaDeNumeros.forEachIndexed { index, valor ->
            if (index != secuenciaDeNumeros.lastIndex)
            {
                assertThrows<AssertionError> { observadorDePrueba.verificarUltimoValorEmitido(Predicate { it == valor }) }
            }
            else
            {
                observadorDePrueba.verificarUltimoValorEmitido(Predicate { it == valor })
            }
        }
    }
}