package co.smartobjects.logica.fondos.precios

import co.smartobjects.entidades.fondos.precios.DatosParaCalculoGrupoClientes
import co.smartobjects.entidades.fondos.precios.GrupoClientes
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.logica.personas.CalculadorGrupoDeEdad


interface CalculadorGrupoCliente
{
    fun darGrupoClienteParaPersona(persona: Persona): GrupoClientes?
}

class CalculadorGrupoClientesEnMemoria(listaGruposClientesSinOrdenar: List<GrupoClientes>, private val calculadorGrupoDeEdad: CalculadorGrupoDeEdad): CalculadorGrupoCliente
{
    private val listaGruposClientes = listaGruposClientesSinOrdenar.sortedWith(GrupoClientes.COMPARADOR_PARA_PRIORIDAD).reversed()
    override fun darGrupoClienteParaPersona(persona: Persona): GrupoClientes?
    {
        val datosGrupoCliente = DatosParaCalculoGrupoClientes(persona.categoria, calculadorGrupoDeEdad.darGrupoEdadParaPersona(persona),persona.tipo)

        listaGruposClientes.forEach{
            println("it: "+it)
        }
        return listaGruposClientes.firstOrNull { it.aplicaParaDatos(datosGrupoCliente) }
    }
}