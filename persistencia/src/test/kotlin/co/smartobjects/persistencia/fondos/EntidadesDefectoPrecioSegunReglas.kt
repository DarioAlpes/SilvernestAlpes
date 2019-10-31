package co.smartobjects.persistencia.fondos

import co.smartobjects.entidades.fondos.precios.GrupoClientes
import co.smartobjects.entidades.fondos.precios.Impuesto
import co.smartobjects.entidades.fondos.precios.SegmentoClientes
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.utilidades.Decimal

internal val impuestoPruebasPorDefecto = Impuesto(0, null, "IVA", Decimal(19))


internal val segmentoClientesCategoriaA = SegmentoClientes(null,
                                                           SegmentoClientes.NombreCampo.CATEGORIA,
                                                           Persona.Categoria.A.toString())


internal val segmentoClientesCategoriaB = SegmentoClientes(null,
                                                           SegmentoClientes.NombreCampo.CATEGORIA,
                                                           Persona.Categoria.B.toString())

internal val segmentoClientesCategoriaC = SegmentoClientes(null,
                                                           SegmentoClientes.NombreCampo.CATEGORIA,
                                                           Persona.Categoria.C.toString())

internal val grupoClientesCategoriaA = GrupoClientes(null,
                                                     "Grupo categoria A",
                                                     listOf(segmentoClientesCategoriaA))

internal val grupoClientesCategoriaB = GrupoClientes(null,
                                                     "Grupo categoria B",
                                                     listOf(segmentoClientesCategoriaB))

internal val grupoClientesCategoriaC = GrupoClientes(null,
                                                     "Grupo categoria C",
                                                     listOf(segmentoClientesCategoriaC))