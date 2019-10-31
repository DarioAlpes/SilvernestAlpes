package co.smartobjects.red.modelos

import co.smartobjects.red.modelos.clientes.ClienteDTO
import co.smartobjects.red.modelos.clientes.LlaveNFCDTO
import co.smartobjects.red.modelos.fondos.*
import co.smartobjects.red.modelos.fondos.libros.*
import co.smartobjects.red.modelos.fondos.precios.*
import co.smartobjects.red.modelos.operativas.EntidadTransaccionalDTO
import co.smartobjects.red.modelos.operativas.TransaccionEntidadTerminadaDTO
import co.smartobjects.red.modelos.operativas.compras.*
import co.smartobjects.red.modelos.operativas.ordenes.*
import co.smartobjects.red.modelos.operativas.reservas.ReservaDTO
import co.smartobjects.red.modelos.operativas.reservas.SesionDeManillaDTO
import co.smartobjects.red.modelos.personas.CampoDePersonaDTO
import co.smartobjects.red.modelos.personas.PersonaConFamiliaresDTO
import co.smartobjects.red.modelos.personas.PersonaDTO
import co.smartobjects.red.modelos.personas.ValorGrupoEdadDTO
import co.smartobjects.red.modelos.ubicaciones.UbicacionDTO
import co.smartobjects.red.modelos.ubicaciones.consumibles.ConsumibleEnPuntoDeVentaDTO
import co.smartobjects.red.modelos.ubicaciones.contabilizables.ConteoUbicacionDTO
import co.smartobjects.red.modelos.ubicaciones.contabilizables.UbicacionesContabilizablesDTO
import co.smartobjects.red.modelos.usuarios.*
import co.smartobjects.red.modelos.usuariosglobales.ContraseñaUsuarioGlobalDTO
import co.smartobjects.red.modelos.usuariosglobales.UsuarioGlobalDTO
import co.smartobjects.red.modelos.usuariosglobales.UsuarioGlobalParaCreacionDTO
import co.smartobjects.red.modelos.usuariosglobales.UsuarioGlobalPatchDTO
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

@DisplayName("Codigos de Error")
internal class CodigosDeErrorPruebas
{
    private val clasesDTO = listOf(
            AccesoDTO::class,
            CampoDePersonaDTO::class,
            CategoriaSkuDTO::class,
            ClienteDTO::class,
            CodigoDeErrorDTO::class,
            CodigosErrorDTO::class,
            CompraDTO::class,
            ConsumibleEnPuntoDeVentaDTO::class,
            ConteoUbicacionDTO::class,
            ContraseñaUsuarioDTO::class,
            ContraseñaUsuarioGlobalDTO::class,
            CreditoFondoDTO::class,
            CreditoPaqueteDTO::class,
            CreditosDeUnaPersonaDTO::class,
            DineroDTO::class,
            EntidadDTO::class,
            EntidadTransaccionalDTO::class,
            EntradaDTO::class,
            FondoDTO::class,
            FondoDisponibleParaLaVentaDTO::class,
            PaqueteDTO.FondoIncluidoDTO::class,
            GrupoClientesDTO::class,
            IFondoDTO::class,
            ILibroDTO::class,
            IProhibicionDTO::class,
            IReglaDTO::class,
            ITransaccionDTO::class,
            ImpuestoDTO::class,
            LlaveNFCDTO::class,
            LibroDTO::class,
            LibroDePreciosDTO::class,
            LibroDeProhibicionesDTO::class,
            LibroSegunReglasCompletoDTO::class,
            LibroSegunReglasDTO::class,
            LoteDeOrdenesDTO::class,
            NombreGrupoClientesDTO::class,
            OrdenDTO::class,
            PagoDTO::class,
            PaqueteDTO::class,
            PaquetePatchDTO::class,
            PermisoBackDTO::class,
            PersonaDTO::class,
            PersonaConFamiliaresDTO::class,
            PrecioDTO::class,
            LibroDePreciosDTO.PrecioDeFondoDTO::class,
            ProhibicionDeFondoDTO::class,
            ProhibicionDePaqueteDTO::class,
            ReglaDeIdGrupoDeClientesDTO::class,
            ReglaDeIdPaqueteDTO::class,
            ReglaDeIdUbicacionDTO::class,
            ReservaDTO::class,
            RolDTO::class,
            SegmentoClientesDTO::class,
            SesionDeManillaDTO::class,
            SkuDTO::class,
            TransaccionCreditoDTO::class,
            TransaccionDTO::class,
            TransaccionDebitoDTO::class,
            TransaccionEntidadTerminadaDTO::class,
            UbicacionDTO::class,
            UbicacionesContabilizablesDTO::class,
            UsuarioDTO::class,
            UsuarioGlobalDTO::class,
            UsuarioGlobalParaCreacionDTO::class,
            UsuarioGlobalPatchDTO::class,
            UsuarioParaCreacionDTO::class,
            UsuarioPatchDTO::class,
            ValorGrupoEdadDTO::class
                                  )

    private data class CodigoDeErrorDTO(val nombreCompletoCampo: String, val valor: String)
    {
        override fun equals(other: Any?): Boolean
        {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as CodigoDeErrorDTO

            if (valor != other.valor) return false

            return true
        }

        override fun hashCode(): Int
        {
            return valor.hashCode()
        }
    }

    @Test
    fun no_se_repiten_codigos_de_error()
    {
        val codigosDeErrorTotales =
                clasesDTO
                    .map { clase ->
                        clase
                            .nestedClasses
                            .find {
                                it.simpleName == "CodigosError"
                            }?.run { members.map { this to it } }
                            ?.filter {
                                it.second.name !in arrayOf("equals", "hashCode", "toString", "codigoErrorBase")
                            }?.map {
                                val noRequiereParametroDeInstancia =
                                        (it.second as KProperty<*>).let {
                                            it.isConst || it.findAnnotation<JvmField>() != null
                                        }

                                val valor =
                                        try
                                        {
                                            "${if (noRequiereParametroDeInstancia) it.second.call() else it.second.call(it.first.objectInstance)}"
                                        }
                                        catch (e: IllegalArgumentException) // Machetazo
                                        {
                                            "${if (noRequiereParametroDeInstancia) it.second.call(it.first.objectInstance) else it.second.call()}"
                                        }

                                CodigoDeErrorDTO("${clase.simpleName}.${it.second.name}", valor)
                            }

                        ?: listOf()
                    }.flatMap { it }

        val histogramaDeUsos = codigosDeErrorTotales.groupBy({ it.valor }, { it.nombreCompletoCampo })

        if (histogramaDeUsos.values.sumBy { it.size } != histogramaDeUsos.size)
        {
            val assertsQueFallaran = mutableListOf<Executable>()

            for (dato in histogramaDeUsos)
            {
                if (dato.value.size > 1)
                {
                    assertsQueFallaran.add(
                            Executable {
                                Assertions.fail("El código de error ${dato.key} se usa en\n\t\t${dato.value.joinToString("\n\t\t")}")
                            }
                                          )
                }
            }

            if (assertsQueFallaran.isNotEmpty())
            {
                Assertions.assertAll("Hay códigos de error repetidos", *(assertsQueFallaran.toTypedArray()))
            }
        }
    }
}