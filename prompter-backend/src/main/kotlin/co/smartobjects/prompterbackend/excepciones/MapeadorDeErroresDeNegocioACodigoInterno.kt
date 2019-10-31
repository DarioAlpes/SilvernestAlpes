package co.smartobjects.prompterbackend.excepciones

import co.smartobjects.entidades.clientes.Cliente
import co.smartobjects.entidades.excepciones.EntidadMalInicializada
import co.smartobjects.entidades.fondos.Fondo
import co.smartobjects.entidades.fondos.Paquete
import co.smartobjects.entidades.fondos.libros.Libro
import co.smartobjects.entidades.fondos.libros.LibroDePrecios
import co.smartobjects.entidades.fondos.libros.LibroDeProhibiciones
import co.smartobjects.entidades.fondos.libros.LibroSegunReglas
import co.smartobjects.entidades.fondos.precios.GrupoClientes
import co.smartobjects.entidades.fondos.precios.Impuesto
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.entidades.fondos.precios.SegmentoClientes
import co.smartobjects.entidades.operativas.EntidadTransaccional
import co.smartobjects.entidades.operativas.compras.Compra
import co.smartobjects.entidades.operativas.compras.CreditoFondo
import co.smartobjects.entidades.operativas.compras.CreditoPaquete
import co.smartobjects.entidades.operativas.compras.Pago
import co.smartobjects.entidades.operativas.ordenes.LoteDeOrdenes
import co.smartobjects.entidades.operativas.ordenes.Orden
import co.smartobjects.entidades.operativas.reservas.Reserva
import co.smartobjects.entidades.operativas.reservas.SesionDeManilla
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.personas.ValorGrupoEdad
import co.smartobjects.entidades.ubicaciones.Ubicacion
import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.entidades.usuarios.Rol
import co.smartobjects.entidades.usuarios.Usuario
import co.smartobjects.entidades.usuariosglobales.UsuarioGlobal
import co.smartobjects.red.modelos.clientes.ClienteDTO
import co.smartobjects.red.modelos.fondos.FondoDTO
import co.smartobjects.red.modelos.fondos.PaqueteDTO
import co.smartobjects.red.modelos.fondos.libros.LibroDTO
import co.smartobjects.red.modelos.fondos.libros.LibroDePreciosDTO
import co.smartobjects.red.modelos.fondos.libros.LibroDeProhibicionesDTO
import co.smartobjects.red.modelos.fondos.libros.LibroSegunReglasDTO
import co.smartobjects.red.modelos.fondos.precios.GrupoClientesDTO
import co.smartobjects.red.modelos.fondos.precios.ImpuestoDTO
import co.smartobjects.red.modelos.fondos.precios.PrecioDTO
import co.smartobjects.red.modelos.fondos.precios.SegmentoClientesDTO
import co.smartobjects.red.modelos.operativas.EntidadTransaccionalDTO
import co.smartobjects.red.modelos.operativas.compras.CompraDTO
import co.smartobjects.red.modelos.operativas.compras.CreditoFondoDTO
import co.smartobjects.red.modelos.operativas.compras.CreditoPaqueteDTO
import co.smartobjects.red.modelos.operativas.compras.PagoDTO
import co.smartobjects.red.modelos.operativas.ordenes.LoteDeOrdenesDTO
import co.smartobjects.red.modelos.operativas.ordenes.OrdenDTO
import co.smartobjects.red.modelos.operativas.reservas.ReservaDTO
import co.smartobjects.red.modelos.operativas.reservas.SesionDeManillaDTO
import co.smartobjects.red.modelos.personas.PersonaDTO
import co.smartobjects.red.modelos.personas.ValorGrupoEdadDTO
import co.smartobjects.red.modelos.ubicaciones.UbicacionDTO
import co.smartobjects.red.modelos.usuarios.PermisoBackDTO
import co.smartobjects.red.modelos.usuarios.RolDTO
import co.smartobjects.red.modelos.usuarios.UsuarioDTO
import co.smartobjects.red.modelos.usuariosglobales.UsuarioGlobalDTO

private val codigosInternosErrorSegunEntidadYCampo = mapOf(

        Cliente.NOMBRE_ENTIDAD to mapOf(
                Cliente.Campos.NOMBRE to ClienteDTO.CodigosError.NOMBRE_INVALIDO
                                       ),

        Rol.NOMBRE_ENTIDAD to mapOf(
                Rol.Campos.NOMBRE to RolDTO.CodigosError.NOMBRE_INVALIDO,
                Rol.Campos.DESCRIPCION to RolDTO.CodigosError.DESCRIPCION_INVALIDA,
                Rol.Campos.PERMISOS to RolDTO.CodigosError.PERMISOS_INVALIDOS
                                   ),

        Usuario.NOMBRE_ENTIDAD to mapOf(
                Usuario.Campos.USUARIO to UsuarioDTO.CodigosError.USUARIO_INVALIDO,
                Usuario.Campos.NOMBRE_COMPLETO to UsuarioDTO.CodigosError.NOMBRE_INVALIDO,
                Usuario.Campos.EMAIL to UsuarioDTO.CodigosError.EMAIL_INVALIDO,
                Usuario.Campos.CONTRASEÑA to UsuarioDTO.CodigosError.CONTRASEÑA_INVALIDA,
                Usuario.Campos.ROLES to UsuarioDTO.CodigosError.ROLES_INVALIDOS,
                Usuario.Campos.ACTIVO to UsuarioDTO.CodigosError.ACTIVO_INVALIDO
                                       ),

        UsuarioGlobal.NOMBRE_ENTIDAD to mapOf(
                Usuario.Campos.USUARIO to UsuarioGlobalDTO.CodigosError.USUARIO_INVALIDO,
                Usuario.Campos.NOMBRE_COMPLETO to UsuarioGlobalDTO.CodigosError.NOMBRE_INVALIDO,
                Usuario.Campos.EMAIL to UsuarioGlobalDTO.CodigosError.EMAIL_INVALIDO,
                Usuario.Campos.CONTRASEÑA to UsuarioGlobalDTO.CodigosError.CONTRASEÑA_INVALIDA,
                Usuario.Campos.ACTIVO to UsuarioGlobalDTO.CodigosError.ACTIVO_INVALIDO
                                             ),

        PermisoBack.NOMBRE_ENTIDAD to mapOf(
                PermisoBack.Campos.ENDPOINT to PermisoBackDTO.CodigosError.ENDPOINT_INVALIDO
                                           ),

        Ubicacion.NOMBRE_ENTIDAD to mapOf(
                Ubicacion.Campos.NOMBRE to UbicacionDTO.CodigosError.NOMBRE_INVALIDO
                                         ),

        Persona.NOMBRE_ENTIDAD to mapOf(
                Persona.Campos.NOMBRE_COMPLETO to PersonaDTO.CodigosError.NOMBRE_INVALIDO,
                Persona.Campos.NUMERO_DOCUMENTO to PersonaDTO.CodigosError.DOCUMENTO_INVALIDO,
                Persona.Campos.FECHA_NACIMIENTO to PersonaDTO.CodigosError.FECHA_DE_NACIMIENTO_INVALIDA
                                       ),

        Paquete.NOMBRE_ENTIDAD to mapOf(
                Paquete.Campos.NOMBRE to PaqueteDTO.CodigosError.NOMBRE_INVALIDO,
                Paquete.Campos.DESCRIPCION to PaqueteDTO.CodigosError.DESCRIPCION_INVALIDA,
                Paquete.Campos.FECHA_VALIDEZ_DESDE to PaqueteDTO.CodigosError.FECHA_VALIDEZ_DESDE_INVALIDA,
                Paquete.Campos.FECHA_VALIDEZ_HASTA to PaqueteDTO.CodigosError.FECHA_VALIDEZ_HASTA_INVALIDA,
                Paquete.Campos.FONDOS_INCLUIDOS to PaqueteDTO.CodigosError.FONDOS_INCLUIDOS_INVALIDOS
                                       ),

        Fondo.NOMBRE_ENTIDAD to mapOf(
                Fondo.Campos.NOMBRE to FondoDTO.CodigosError.NOMBRE_INVALIDO
                                     ),

        Precio.NOMBRE_ENTIDAD to mapOf(
                Precio.Campos.VALOR to PrecioDTO.CodigosError.VALOR_INVALIDO
                                      ),

        Impuesto.NOMBRE_ENTIDAD to mapOf(
                Impuesto.Campos.NOMBRE to ImpuestoDTO.CodigosError.NOMBRE_INVALIDO,
                Impuesto.Campos.TASA to ImpuestoDTO.CodigosError.TASA_INVALIDA
                                        ),

        SegmentoClientes.NOMBRE_ENTIDAD to mapOf(
                SegmentoClientes.Campos.VALOR to SegmentoClientesDTO.CodigosError.VALOR_INVALIDO
                                                ),

        GrupoClientes.NOMBRE_ENTIDAD to mapOf(
                GrupoClientes.Campos.NOMBRE to GrupoClientesDTO.CodigosError.NOMBRE_INVALIDO,
                GrupoClientes.Campos.SEGMENTOS to GrupoClientesDTO.CodigosError.SEGMENTOS_CLIENTES_INVALIDOS
                                             ),

        ValorGrupoEdad.NOMBRE_ENTIDAD to mapOf(
                ValorGrupoEdad.Campos.VALOR to ValorGrupoEdadDTO.CodigosError.VALOR_INVALIDO,
                ValorGrupoEdad.Campos.EDAD_MINIMA to ValorGrupoEdadDTO.CodigosError.EDAD_MINIMA_SUPERIOR_A_MAXIMA
                                              ),

        Libro.NOMBRE_ENTIDAD to mapOf(
                Libro.Campos.NOMBRE to LibroDTO.CodigosError.NOMBRE_INVALIDO
                                     ),

        LibroDePrecios.NOMBRE_ENTIDAD to mapOf(
                LibroDePrecios.Campos.PRECIOS to LibroDePreciosDTO.CodigosError.PRECIOS_VACIOS
                                              ),

        LibroDeProhibiciones.NOMBRE_ENTIDAD to mapOf(
                LibroDeProhibiciones.Campos.PROHIBICIONES_FONDO to LibroDeProhibicionesDTO.CodigosError.NO_HAY_PROHIBICIONES_DE_NINGUN_TIPO,
                LibroDeProhibiciones.Campos.PROHIBICIONES_PAQUETE to LibroDeProhibicionesDTO.CodigosError.NO_HAY_PROHIBICIONES_DE_NINGUN_TIPO
                                                    ),

        LibroSegunReglas.NOMBRE_ENTIDAD to mapOf(
                LibroSegunReglas.Campos.NOMBRE to LibroSegunReglasDTO.CodigosError.NOMBRE_INVALIDO
                                                ),

        EntidadTransaccional.NOMBRE_ENTIDAD to mapOf(
                EntidadTransaccional.Campos.ID to EntidadTransaccionalDTO.CodigosError.ID_INVALIDO,
                EntidadTransaccional.Campos.NOMBRE_USUARIO to EntidadTransaccionalDTO.CodigosError.NOMBRE_USUARIO_INVALIDO
                                                    ),

        Compra.NOMBRE_ENTIDAD to mapOf(
                Compra.Campos.CREDITOS to CompraDTO.CodigosError.CREDITOS_INVALIDOS,
                Compra.Campos.PAGOS to CompraDTO.CodigosError.PAGOS_INVALIDOS,
                Compra.Campos.FECHA_REALIZACION to CompraDTO.CodigosError.FECHA_REALIZACION_INVALIDA
                                      ),

        Pago.NOMBRE_ENTIDAD to mapOf(
                Pago.Campos.VALOR_PAGADO to PagoDTO.CodigosError.VALOR_PAGADO_INVALIDO,
                Pago.Campos.NUMERO_TRANSACCION_POS to PagoDTO.CodigosError.NUMERO_TRANSACCION_POS_INVALIDO
                                    ),

        CreditoPaquete.NOMBRE_ENTIDAD to mapOf(
                CreditoPaquete.Campos.CREDITOS_FONDOS to CreditoPaqueteDTO.CodigosError.CREDITOS_FONDOS_INVALIDOS
                                              ),

        CreditoFondo.NOMBRE_ENTIDAD to mapOf(
                CreditoFondo.Campos.CANTIDAD to CreditoFondoDTO.CodigosError.CANTIDAD_INVALIDA,
                CreditoFondo.Campos.VALOR_PAGADO to CreditoFondoDTO.CodigosError.VALOR_BASE_PAGADO_INVALIDO,
                CreditoFondo.Campos.VALOR_IMPUESTO_PAGADO to CreditoFondoDTO.CodigosError.VALOR_IMPUESTO_PAGADO_INVALIDO,
                CreditoFondo.Campos.FECHA_VALIDEZ_DESDE to CreditoFondoDTO.CodigosError.FECHA_VALIDEZ_DESDE_INVALIDA,
                CreditoFondo.Campos.FECHA_VALIDEZ_HASTA to CreditoFondoDTO.CodigosError.FECHA_VALIDEZ_HASTA_INVALIDA,
                CreditoFondo.Campos.ORIGEN to CreditoFondoDTO.CodigosError.ORIGEN_INVALIDO,
                CreditoFondo.Campos.ID_DISPOSITIVO to CreditoFondoDTO.CodigosError.ID_DISPOSITIVO_INVALIDO
                                            ),

        Reserva.NOMBRE_ENTIDAD to mapOf(
                Reserva.Campos.SESIONES_DE_MANILLA to ReservaDTO.CodigosError.SESIONES_DE_MANILLAS_INVALIDAS
                                       ),

        SesionDeManilla.NOMBRE_ENTIDAD to mapOf(
                SesionDeManilla.Campos.UUID_TAG to SesionDeManillaDTO.CodigosError.UUID_TAG_VACIO,
                SesionDeManilla.Campos.FECHA_ACTIVACION to SesionDeManillaDTO.CodigosError.FECHA_DE_ACTIVACION_INVALIDA,
                SesionDeManilla.Campos.FECHA_DESACTIVACION to SesionDeManillaDTO.CodigosError.FECHA_DE_DESACTIVACION_INVALIDA,
                SesionDeManilla.Campos.IDS_CREDITOS_CODIFICADOS to SesionDeManillaDTO.CodigosError.IDS_CREDITOS_A_CODIFICAR_VACIOS
                                               ),

        Orden.NOMBRE_ENTIDAD to mapOf(
                Orden.Campos.TRANSACCIONES to OrdenDTO.CodigosError.TRANSACCIONES_VACIAS,
                Orden.Campos.FECHA_REALIZACION to OrdenDTO.CodigosError.FECHA_DE_REALIZACION_INVALIDA
                                     ),

        LoteDeOrdenes.NOMBRE_ENTIDAD to mapOf(
                LoteDeOrdenes.Campos.ORDENES to LoteDeOrdenesDTO.CodigosError.ORDENES_INVALIDAS
                                             )


                                                          )

internal fun EntidadMalInicializada.darCodigoInterno(codigoInternoPorDefecto: Int): Int
{
    return codigosInternosErrorSegunEntidadYCampo[nombreEntidad]?.get(nombreDelCampo) ?: codigoInternoPorDefecto
}