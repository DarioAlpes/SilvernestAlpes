package co.smartobjects.logica.personas

import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.personas.ValorGrupoEdad

interface CalculadorGrupoDeEdad
{
    fun darGrupoEdadParaPersona(persona: Persona): ValorGrupoEdad?
}

class CalculadorGrupoEdadEnMemoria(private val listaGruposEdad: List<ValorGrupoEdad>): CalculadorGrupoDeEdad
{
    override fun darGrupoEdadParaPersona(persona: Persona): ValorGrupoEdad?
    {
        return listaGruposEdad.firstOrNull { it.aplicaParaPersona(persona) }
    }
}