package co.smartobjects.prompterbackend

import co.smartobjects.entidades.clientes.Cliente
import co.smartobjects.entidades.fondos.CategoriaSku
import co.smartobjects.entidades.fondos.Entrada
import co.smartobjects.entidades.fondos.Paquete
import co.smartobjects.entidades.fondos.Sku
import co.smartobjects.entidades.fondos.libros.LibroDePrecios
import co.smartobjects.entidades.fondos.libros.LibroSegunReglas
import co.smartobjects.entidades.fondos.libros.PrecioEnLibro
import co.smartobjects.entidades.fondos.libros.ReglaDeIdGrupoDeClientes
import co.smartobjects.entidades.fondos.precios.GrupoClientes
import co.smartobjects.entidades.fondos.precios.Impuesto
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.entidades.fondos.precios.SegmentoClientes
import co.smartobjects.entidades.personas.ValorGrupoEdad
import co.smartobjects.entidades.ubicaciones.Ubicacion
import co.smartobjects.entidades.usuarios.Rol
import co.smartobjects.entidades.usuarios.Usuario
import co.smartobjects.prompterbackend.serviciosrest.fondos.*
import co.smartobjects.prompterbackend.serviciosrest.fondos.libros.RecursoLibrosDePrecios
import co.smartobjects.prompterbackend.serviciosrest.fondos.libros.RecursoLibrosDeProhibiciones
import co.smartobjects.prompterbackend.serviciosrest.fondos.libros.RecursoLibrosSegunReglas
import co.smartobjects.prompterbackend.serviciosrest.fondos.libros.RecursoLibrosSegunReglasCompleto
import co.smartobjects.prompterbackend.serviciosrest.fondos.precios.RecursoGruposClientes
import co.smartobjects.prompterbackend.serviciosrest.fondos.precios.RecursoImpuestos
import co.smartobjects.prompterbackend.serviciosrest.operativas.compras.RecursoCompras
import co.smartobjects.prompterbackend.serviciosrest.operativas.ordenes.RecursoLotesDeOrdenes
import co.smartobjects.prompterbackend.serviciosrest.operativas.ordenes.RecursoOrdenes
import co.smartobjects.prompterbackend.serviciosrest.operativas.reservas.RecursoReservas
import co.smartobjects.prompterbackend.serviciosrest.operativas.reservas.sesionesdemanilla.RecursoOrdenesDeUnaSesionDeManilla
import co.smartobjects.prompterbackend.serviciosrest.operativas.reservas.sesionesdemanilla.RecursoPersonaPorIdSesionManilla
import co.smartobjects.prompterbackend.serviciosrest.operativas.reservas.sesionesdemanilla.RecursoSesionesDeManilla
import co.smartobjects.prompterbackend.serviciosrest.personas.RecursoPersonaPorDocumento
import co.smartobjects.prompterbackend.serviciosrest.personas.RecursoPersonas
import co.smartobjects.prompterbackend.serviciosrest.personas.RecursoPersonasDeUnaCompra
import co.smartobjects.prompterbackend.serviciosrest.personas.RecursoValoresGrupoEdad
import co.smartobjects.prompterbackend.serviciosrest.personas.compras.RecursoComprasDeUnaPersona
import co.smartobjects.prompterbackend.serviciosrest.personas.creditos.RecursoCreditosDeUnaPersona
import co.smartobjects.prompterbackend.serviciosrest.ubicaciones.RecursoUbicaciones
import co.smartobjects.prompterbackend.serviciosrest.ubicaciones.consumibles.RecursoConsumiblesEnPuntoDeVenta
import co.smartobjects.prompterbackend.serviciosrest.ubicaciones.consumibles.RecursoFondosEnPuntoDeVenta
import co.smartobjects.prompterbackend.serviciosrest.usuarios.RecursoPermisosPosibles
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.threeten.bp.ZonedDateTime

@Suppress("UNUSED_VARIABLE")
internal fun poblarConDatosCafam()
{
    ConfiguracionAplicacionJersey.inicializacionPorDefecto()
    val dependencias = ConfiguracionAplicacionJersey.DEPENDENCIAS
    val clientes = dependencias.repositorioClientes.listar().find { it.nombre == "CAFAM" }
    if (clientes != null)
    {
        return
    }

    val clienteUnico = ConfiguracionAplicacionJersey.RECURSO_CLIENTES.crearEntidadDeNegocio(Cliente(null, "CAFAM"))
    val idCliente = clienteUnico.id!!
    ConfiguracionAplicacionJersey.RECURSO_CLIENTES.darRecursosEntidadEspecifica(idCliente)

    val rolSuperusuario =
            dependencias.repositorioRoles.crear(
                    idCliente,
                    Rol(
                            "Superusuario",
                            "Tiene todos los permisos",
                            RecursoPermisosPosibles(idCliente, ConfiguracionAplicacionJersey.RECURSO_CLIENTES.manejadorSeguridad)
                                .listarTodosSegunIdCliente(idCliente).toSet()

                       )
                                               )

    val superusuario =
            dependencias.repositorioUsuarios.crear(
                    idCliente,
                    Usuario.UsuarioParaCreacion(
                            Usuario.DatosUsuario(idCliente, "admin", "usuario de prueba", "noexiste@noexiste.com", true,"apellido","CC","1234","2019-10-01","2019-10-01","co01"),
                            "admin".toCharArray(),
                            setOf(Rol.RolParaCreacionDeUsuario(rolSuperusuario.nombre))
                                               )
                                                  )

    creacionDeUsuariosPropiosDeCafam(dependencias, idCliente)

    val colombia = dependencias.repositorioUbicaciones.crear(
            idCliente,
            Ubicacion(
                    idCliente,
                    null,
                    "Colombia",
                    Ubicacion.Tipo.PAIS,
                    Ubicacion.Subtipo.AP_RESTRINGIDO,
                    null,
                    linkedSetOf()
                     )
                                                            )

    val cundinamarca = dependencias.repositorioUbicaciones.crear(
            idCliente,
            Ubicacion(
                    idCliente,
                    null,
                    "Cundinamarca",
                    Ubicacion.Tipo.REGION,
                    Ubicacion.Subtipo.AP_RESTRINGIDO,
                    colombia.id,
                    linkedSetOf(colombia.id!!)
                     )
                                                                )


    val melgar = dependencias.repositorioUbicaciones.crear(
            idCliente,
            Ubicacion(
                    idCliente,
                    null,
                    "Melgar",
                    Ubicacion.Tipo.CIUDAD,
                    Ubicacion.Subtipo.AP,
                    cundinamarca.id,
                    linkedSetOf(colombia.id!!, cundinamarca.id!!)
                     )
                                                          )

    val centroVacacionalMelgar = dependencias.repositorioUbicaciones.crear(
            idCliente,
            Ubicacion(
                    idCliente,
                    null,
                    "Centro Vacacional Melgar",
                    Ubicacion.Tipo.PROPIEDAD,
                    Ubicacion.Subtipo.AP,
                    melgar.id,
                    linkedSetOf(*cundinamarca.idsDeAncestros.toTypedArray(), melgar.id!!)
                     )
                                                                          )

    val restauranteOlas = dependencias.repositorioUbicaciones.crear(
            idCliente,
            Ubicacion(
                    idCliente,
                    null,
                    "Restaurante Olas",
                    Ubicacion.Tipo.PUNTO_DE_CONTACTO,
                    Ubicacion.Subtipo.POS,
                    centroVacacionalMelgar.id,
                    linkedSetOf(*centroVacacionalMelgar.idsDeAncestros.toTypedArray(), centroVacacionalMelgar.id!!)
                     )
                                                                   )

    val ingresoPrincipal = dependencias.repositorioUbicaciones.crear(
            idCliente,
            Ubicacion(
                    idCliente,
                    null,
                    "Ingreso Principal",
                    Ubicacion.Tipo.PUNTO_DE_CONTACTO,
                    Ubicacion.Subtipo.AP,
                    centroVacacionalMelgar.id,
                    linkedSetOf(*centroVacacionalMelgar.idsDeAncestros.toTypedArray(), centroVacacionalMelgar.id!!)
                     )
                                                                    )

    val ingresoParqueadero = dependencias.repositorioUbicaciones.crear(
            idCliente,
            Ubicacion(
                    idCliente,
                    null,
                    "Ingreso Parqueadero",
                    Ubicacion.Tipo.PUNTO_DE_CONTACTO,
                    Ubicacion.Subtipo.AP,
                    centroVacacionalMelgar.id,
                    linkedSetOf(*centroVacacionalMelgar.idsDeAncestros.toTypedArray(), centroVacacionalMelgar.id!!)
                     )
                                                                      )

    val transporte = dependencias.repositorioUbicaciones.crear(
            idCliente,
            Ubicacion(
                    idCliente,
                    null,
                    "Transporte",
                    Ubicacion.Tipo.PUNTO_DE_CONTACTO,
                    Ubicacion.Subtipo.AP,
                    centroVacacionalMelgar.id,
                    linkedSetOf(*centroVacacionalMelgar.idsDeAncestros.toTypedArray(), centroVacacionalMelgar.id!!)
                     )
                                                              )

    val entradaPiscina = dependencias.repositorioUbicaciones.crear(
            idCliente,
            Ubicacion(
                    idCliente,
                    null,
                    "Entrada Piscina",
                    Ubicacion.Tipo.PUNTO_DE_CONTACTO,
                    Ubicacion.Subtipo.AP,
                    centroVacacionalMelgar.id,
                    linkedSetOf(*centroVacacionalMelgar.idsDeAncestros.toTypedArray(), centroVacacionalMelgar.id!!)
                     )
                                                                  )

    val salidaPiscina = dependencias.repositorioUbicaciones.crear(
            idCliente,
            Ubicacion(
                    idCliente,
                    null,
                    "Salida Piscina",
                    Ubicacion.Tipo.PUNTO_DE_CONTACTO,
                    Ubicacion.Subtipo.AP,
                    centroVacacionalMelgar.id,
                    linkedSetOf(*centroVacacionalMelgar.idsDeAncestros.toTypedArray(), centroVacacionalMelgar.id!!)
                     )
                                                                 )

    val exento = dependencias.repositorioImpuestos.crear(
            idCliente,
            Impuesto(idCliente, null, "Exento", Decimal(0.0))
                                                        )

    val iva = dependencias.repositorioImpuestos.crear(
            idCliente,
            Impuesto(idCliente, null, "IVA", Decimal(19.0))
                                                     )

    val impoconsumo = dependencias.repositorioImpuestos.crear(
            idCliente,
            Impuesto(idCliente, null, "IMPOCONSUMO", Decimal(8.0))
                                                             )

    val bebe = dependencias.repositorioValoresGruposEdad.crear(idCliente, ValorGrupoEdad("Bebé", 0, 4))
    val niño = dependencias.repositorioValoresGruposEdad.crear(idCliente, ValorGrupoEdad("Niño", 5, 11))
    val adulto = dependencias.repositorioValoresGruposEdad.crear(idCliente, ValorGrupoEdad("Adulto", 12, null))

    val niñosA = dependencias.repositorioGrupoClientes.crear(
            idCliente,
            GrupoClientes(null,
                          "Niños Categoría A",
                          listOf(SegmentoClientes(null,
                                                  SegmentoClientes.NombreCampo.CATEGORIA,
                                                  "A"),
                                 SegmentoClientes(null,
                                                  SegmentoClientes.NombreCampo.GRUPO_DE_EDAD,
                                                  niño.valor)
                                )
                         )
                                                            )

    val niñosB = dependencias.repositorioGrupoClientes.crear(
            idCliente,
            GrupoClientes(null,
                          "Niños Categoría B",
                          listOf(SegmentoClientes(null,
                                                  SegmentoClientes.NombreCampo.CATEGORIA,
                                                  "B"),
                                 SegmentoClientes(null,
                                                  SegmentoClientes.NombreCampo.GRUPO_DE_EDAD,
                                                  niño.valor)
                                )
                         )
                                                            )


    val niñosC = dependencias.repositorioGrupoClientes.crear(
            idCliente,
            GrupoClientes(null,
                          "Niños Categoría C",
                          listOf(SegmentoClientes(null,
                                                  SegmentoClientes.NombreCampo.CATEGORIA,
                                                  "C"),
                                 SegmentoClientes(null,
                                                  SegmentoClientes.NombreCampo.GRUPO_DE_EDAD,
                                                  niño.valor)
                                )
                         )
                                                            )


    val niñosD = dependencias.repositorioGrupoClientes.crear(
            idCliente,
            GrupoClientes(null,
                          "Niños Categoría D",
                          listOf(SegmentoClientes(null,
                                                  SegmentoClientes.NombreCampo.CATEGORIA,
                                                  "D"),
                                 SegmentoClientes(null,
                                                  SegmentoClientes.NombreCampo.GRUPO_DE_EDAD,
                                                  niño.valor)
                                )
                         )
                                                            )


    val adultosA = dependencias.repositorioGrupoClientes.crear(
            idCliente,
            GrupoClientes(null,
                          "Adultos Categoría A",
                          listOf(SegmentoClientes(null,
                                                  SegmentoClientes.NombreCampo.CATEGORIA,
                                                  "A"),
                                 SegmentoClientes(null,
                                                  SegmentoClientes.NombreCampo.GRUPO_DE_EDAD,
                                                  adulto.valor)
                                )
                         )
                                                              )

    val adultosB = dependencias.repositorioGrupoClientes.crear(
            idCliente,
            GrupoClientes(null,
                          "Adultos Categoría B",
                          listOf(SegmentoClientes(null,
                                                  SegmentoClientes.NombreCampo.CATEGORIA,
                                                  "B"),
                                 SegmentoClientes(null,
                                                  SegmentoClientes.NombreCampo.GRUPO_DE_EDAD,
                                                  adulto.valor)
                                )
                         )
                                                              )

    val adultosC = dependencias.repositorioGrupoClientes.crear(
            idCliente,
            GrupoClientes(null,
                          "Adultos Categoría C",
                          listOf(SegmentoClientes(null,
                                                  SegmentoClientes.NombreCampo.CATEGORIA,
                                                  "C"),
                                 SegmentoClientes(null,
                                                  SegmentoClientes.NombreCampo.GRUPO_DE_EDAD,
                                                  adulto.valor)
                                )
                         )
                                                              )


    val adultosD = dependencias.repositorioGrupoClientes.crear(
            idCliente,
            GrupoClientes(null,
                          "Adultos Categoría D",
                          listOf(SegmentoClientes(null,
                                                  SegmentoClientes.NombreCampo.CATEGORIA,
                                                  "D"),
                                 SegmentoClientes(null,
                                                  SegmentoClientes.NombreCampo.GRUPO_DE_EDAD,
                                                  adulto.valor)
                                )
                         )
                                                              )


    val bebes = dependencias.repositorioGrupoClientes.crear(
            idCliente,
            GrupoClientes(null,
                          "Bebés",
                          listOf(SegmentoClientes(null,
                                                  SegmentoClientes.NombreCampo.CATEGORIA,
                                                  "NINGUNA"),
                                 SegmentoClientes(null,
                                                  SegmentoClientes.NombreCampo.GRUPO_DE_EDAD,
                                                  bebe.valor)
                                )
                         )
                                                           )


    val categoriaBase = dependencias.repositorioCategoriasSkus.crear(
            idCliente,
            CategoriaSku(
                    idCliente,
                    null,
                    "Cualquier Sku",
                    false,
                    false,
                    false,
                    Precio(Decimal.UNO, iva.id!!),
                    "-",
                    null,
                    linkedSetOf(),
                    null
                        )
                                                                    )

    val almuerzo = dependencias.repositorioSkus.crear(
            idCliente,
            Sku(
                    idCliente,
                    null,
                    "ALMUERZO - MENU DEL DIA",
                    true,
                    false,
                    false,
                    Precio(Decimal(14_500), impoconsumo.id!!),
                    "-",
                    categoriaBase.id!!,
                    null
               )
                                                     )

    val seguro = dependencias.repositorioSkus.crear(
            idCliente,
            Sku(
                    idCliente,
                    null,
                    "SEGURO",
                    true,
                    false,
                    false,
                    Precio(Decimal(750), exento.id!!),
                    "-",
                    categoriaBase.id!!,
                    null
               )
                                                   )

    val entradaTemporadaAlta = dependencias.repositorioEntradas.crear(
            idCliente,
            Entrada(
                    idCliente,
                    null,
                    "ENTRADA TEMPORADA ALTA",
                    true,
                    false,
                    Precio(Decimal(13_000), iva.id!!),
                    "-",
                    categoriaBase.id!!
                   )
                                                                     )

    val entradaTemporadaBaja = dependencias.repositorioEntradas.crear(
            idCliente,
            Entrada(
                    idCliente,
                    null,
                    "ENTRADA TEMPORADA BAJA",
                    true,
                    false,
                    Precio(Decimal(7_500), iva.id!!),
                    "-",
                    categoriaBase.id!!
                   )
                                                                     )

    val paqueteEntradaTemporadaAltaYAlmuerzo = dependencias.repositorioPaquetes.crear(
            idCliente,
            Paquete(
                    idCliente,
                    null,
                    "ENTRADA TEMPORADA ALTA + ALMUERZO",
                    "Entrada al parque en alta temporada con menú del día incluído",
                    true,
                    ZonedDateTime.of(2018, 1, 1, 0, 0, 0, 0, ZONA_HORARIA_POR_DEFECTO),
                    ZonedDateTime.of(2038, 1, 1, 0, 0, 0, 0, ZONA_HORARIA_POR_DEFECTO),
                    listOf(
                            Paquete.FondoIncluido(entradaTemporadaAlta.id!!, entradaTemporadaAlta.codigoExterno, Decimal.UNO),
                            Paquete.FondoIncluido(almuerzo.id!!, almuerzo.codigoExterno, Decimal.UNO)
                          ),
                    "-"
                   )
                                                                                     )

    val paqueteEntradaTemporadaBajaYAlmuerzo = dependencias.repositorioPaquetes.crear(
            idCliente,
            Paquete(
                    idCliente,
                    null,
                    "ENTRADA TEMPORADA BAJA + ALMUERZO",
                    "Entrada al parque en baja temporada con menú del día incluído",
                    true,
                    ZonedDateTime.of(2018, 1, 1, 0, 0, 0, 0, ZONA_HORARIA_POR_DEFECTO),
                    ZonedDateTime.of(2038, 1, 1, 0, 0, 0, 0, ZONA_HORARIA_POR_DEFECTO),
                    listOf(
                            Paquete.FondoIncluido(entradaTemporadaBaja.id!!, entradaTemporadaBaja.codigoExterno, Decimal.UNO),
                            Paquete.FondoIncluido(almuerzo.id!!, almuerzo.codigoExterno, Decimal.UNO)
                          ),
                    "-"
                   )
                                                                                     )

    crearSkusYPaquetesBBVA(dependencias, idCliente, exento, colombia)

    creacionDeLibrosDePrecios(
            dependencias,
            idCliente,
            iva,
            entradaTemporadaAlta,
            entradaTemporadaBaja,
            niñosA,
            adultosA,
            "A",
            listOf(13_000, 7_500),
            listOf(16_250, 9_350)
                             )

    creacionDeLibrosDePrecios(
            dependencias,
            idCliente,
            iva,
            entradaTemporadaAlta,
            entradaTemporadaBaja,
            niñosB,
            adultosB,
            "B",
            listOf(14_500, 8_400),
            listOf(18_150, 10_450)
                             )

    creacionDeLibrosDePrecios(
            dependencias,
            idCliente,
            iva,
            entradaTemporadaAlta,
            entradaTemporadaBaja,
            niñosC,
            adultosC,
            "C",
            listOf(25_100, 16_400),
            listOf(31_350, 20_500)
                             )

    creacionDeLibrosDePrecios(
            dependencias,
            idCliente,
            iva,
            entradaTemporadaAlta,
            entradaTemporadaBaja,
            niñosD,
            adultosD,
            "D",
            listOf(39_200, 21_200),
            listOf(48_950, 26_550)
                             )
}

private fun creacionDeLibrosDePrecios(
        dependencias: Dependencias,
        idCliente: Long,
        impuestoIva: Impuesto,
        entradaTemporadaAlta: Entrada,
        entradaTemporadaBaja: Entrada,
        niños: GrupoClientes,
        adultos: GrupoClientes,
        nombreCategoría: String,
        preciosNiños: List<Int>,
        preciosAdultos: List<Int>
                                     )
{
    val libroDePreciosNiños =
            dependencias.repositorioLibroDePrecios.crear(
                    idCliente,
                    LibroDePrecios(
                            idCliente,
                            null,
                            "Libro de precios para niños de categoría $nombreCategoría",
                            setOf(
                                    PrecioEnLibro(Precio(Decimal(preciosNiños[0]), impuestoIva.id!!), entradaTemporadaAlta.id!!),
                                    PrecioEnLibro(Precio(Decimal(preciosNiños[1]), impuestoIva.id!!), entradaTemporadaBaja.id!!)
                                 )
                                  )
                                                        )

    val preciosParaNiños =
            dependencias.repositorioLibrosSegunReglas.crear(
                    idCliente,
                    LibroSegunReglas(
                            0,
                            null,
                            "Precios para niños de categoría $nombreCategoría",
                            libroDePreciosNiños.id!!,
                            linkedSetOf(),
                            linkedSetOf(ReglaDeIdGrupoDeClientes(niños.id!!)),
                            linkedSetOf()
                                    )
                                                           )

    val libroDePreciosAdultos =
            dependencias.repositorioLibroDePrecios.crear(
                    idCliente,
                    LibroDePrecios(
                            idCliente,
                            null,
                            "Libro de precios para adultos de categoría $nombreCategoría",
                            setOf(
                                    PrecioEnLibro(Precio(Decimal(preciosAdultos[0]), impuestoIva.id!!), entradaTemporadaAlta.id!!),
                                    PrecioEnLibro(Precio(Decimal(preciosAdultos[1]), impuestoIva.id!!), entradaTemporadaBaja.id!!)
                                 )
                                  )
                                                        )

    val preciosParaAdultos =
            dependencias.repositorioLibrosSegunReglas.crear(
                    idCliente,
                    LibroSegunReglas(
                            0,
                            null,
                            "Precios para adultos de categoría $nombreCategoría",
                            libroDePreciosAdultos.id!!,
                            linkedSetOf(),
                            linkedSetOf(ReglaDeIdGrupoDeClientes(adultos.id!!)),
                            linkedSetOf()
                                    )
                                                           )
}

private fun creacionDeUsuariosPropiosDeCafam(dependencias: Dependencias, idCliente: Long)
{
    val rolTaquillero =
            dependencias.repositorioRoles.crear(
                    idCliente,
                    Rol(
                            "Taquillero",
                            "Rol para un taquillero",
                            listOf(
                                    RecursoUbicaciones.INFORMACION_PERMISO_LISTAR,
                                    RecursoUbicaciones.INFORMACION_PERMISO_CONSULTAR,
                                    RecursoPersonas.INFORMACION_PERMISO_CONSULTAR,
                                    RecursoPersonas.INFORMACION_PERMISO_CREACION,
                                    RecursoPersonas.INFORMACION_PERMISO_ACTUALIZACION,
                                    RecursoPersonaPorDocumento.INFORMACION_PERMISO_CONSULTAR,
                                    RecursoValoresGrupoEdad.INFORMACION_PERMISO_LISTAR,
                                    RecursoValoresGrupoEdad.INFORMACION_PERMISO_CONSULTAR,
                                    RecursoGruposClientes.INFORMACION_PERMISO_LISTAR,
                                    RecursoGruposClientes.INFORMACION_PERMISO_CONSULTAR,
                                    RecursoImpuestos.INFORMACION_PERMISO_LISTAR,
                                    RecursoImpuestos.INFORMACION_PERMISO_CONSULTAR,
                                    RecursoFondos.INFORMACION_PERMISO_LISTAR,
                                    RecursoFondos.INFORMACION_PERMISO_CONSULTAR,
                                    RecursoAccesos.INFORMACION_PERMISO_LISTAR,
                                    RecursoAccesos.INFORMACION_PERMISO_CONSULTAR,
                                    RecursoCategoriasSku.INFORMACION_PERMISO_LISTAR,
                                    RecursoCategoriasSku.INFORMACION_PERMISO_CONSULTAR,
                                    RecursoEntradas.INFORMACION_PERMISO_LISTAR,
                                    RecursoEntradas.INFORMACION_PERMISO_CONSULTAR,
                                    RecursoMonedas.INFORMACION_PERMISO_LISTAR,
                                    RecursoMonedas.INFORMACION_PERMISO_CONSULTAR,
                                    RecursoSkus.INFORMACION_PERMISO_LISTAR,
                                    RecursoSkus.INFORMACION_PERMISO_CONSULTAR,
                                    RecursoPaquetes.INFORMACION_PERMISO_LISTAR,
                                    RecursoPaquetes.INFORMACION_PERMISO_CONSULTAR,
                                    RecursoLibrosDePrecios.INFORMACION_PERMISO_LISTAR,
                                    RecursoLibrosDePrecios.INFORMACION_PERMISO_CONSULTAR,
                                    RecursoLibrosDeProhibiciones.INFORMACION_PERMISO_LISTAR,
                                    RecursoLibrosDeProhibiciones.INFORMACION_PERMISO_CONSULTAR,
                                    RecursoLibrosSegunReglas.INFORMACION_PERMISO_LISTAR,
                                    RecursoLibrosSegunReglas.INFORMACION_PERMISO_CONSULTAR,
                                    RecursoLibrosSegunReglasCompleto.INFORMACION_PERMISO_LISTAR,
                                    RecursoLibrosSegunReglasCompleto.INFORMACION_PERMISO_CONSULTAR,
                                    RecursoCompras.INFORMACION_PERMISO_LISTAR,
                                    RecursoCompras.INFORMACION_PERMISO_CONSULTAR,
                                    RecursoCompras.INFORMACION_PERMISO_ACTUALIZACION,
                                    RecursoCompras.INFORMACION_PERMISO_ACTUALIZACION_PARCIAL,
                                    RecursoComprasDeUnaPersona.INFORMACION_PERMISO_LISTAR,
                                    RecursoPersonasDeUnaCompra.INFORMACION_PERMISO_LISTAR,
                                    RecursoCreditosDeUnaPersona.INFORMACION_PERMISO_CONSULTAR,
                                    RecursoConsumiblesEnPuntoDeVenta.INFORMACION_PERMISO_LISTAR,
                                    RecursoFondosEnPuntoDeVenta.INFORMACION_PERMISO_LISTAR,
                                    RecursoReservas.INFORMACION_PERMISO_LISTAR,
                                    RecursoReservas.INFORMACION_PERMISO_CONSULTAR,
                                    RecursoReservas.INFORMACION_PERMISO_ACTUALIZACION,
                                    RecursoReservas.INFORMACION_PERMISO_ACTUALIZACION_PARCIAL,
                                    RecursoSesionesDeManilla.INFORMACION_PERMISO_ACTUALIZACION_PARCIAL,
                                    RecursoSesionesDeManilla.INFORMACION_PERMISO_CONSULTAR,
                                    RecursoOrdenes.INFORMACION_PERMISO_LISTAR,
                                    RecursoOrdenes.INFORMACION_PERMISO_CONSULTAR,
                                    RecursoOrdenesDeUnaSesionDeManilla.INFORMACION_PERMISO_LISTAR,
                                    RecursoPersonaPorIdSesionManilla.INFORMACION_PERMISO_CONSULTAR,
                                    RecursoLotesDeOrdenes.INFORMACION_PERMISO_ACTUALIZACION,
                                    RecursoLotesDeOrdenes.INFORMACION_PERMISO_ACTUALIZACION_PARCIAL
                                  )
                                .map { it.aPermisoBackSegunIdCliente(idCliente) }
                                .toSet()

                       )
                                               )

    print("\nINFORMACION_PERMISO_CONSULTAR"+RecursoPersonaPorDocumento.INFORMACION_PERMISO_CONSULTAR)
    fun crearUsuario(usuario: String, contraseña: String, nombre: String, correo: String, apellidos: String, tipoIdentificacion: String, numeroIdentificacion: String, fechaCreacion: String, vigenciaUsuario: String, ceco: String)
    {
        dependencias.repositorioUsuarios.crear(
                idCliente,
                Usuario.UsuarioParaCreacion(
                        Usuario.DatosUsuario(idCliente, usuario, nombre, correo, true, apellidos, tipoIdentificacion, numeroIdentificacion, fechaCreacion, vigenciaUsuario, ceco),
                        contraseña.toCharArray(),
                        setOf(Rol.RolParaCreacionDeUsuario(rolTaquillero.nombre))
                                           )
                                              )
    }

    crearUsuario("e-amacosta", "1013646770", "ACOSTA RESTREPO ANGELA MARIA", "e-amacostas","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-ldarias", "65823597", "ARIAS RUIZ LUZ DARY", "e-ldarias","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-daya", "1007650877", "AYA GAITAN DANIELA", "e-daya","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-ccbarreto", "1106892207", "BARRETO BARRETO CHRISTIAN CAMILO", "e-ccbarreto","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-jebedoya", "1106890461", "BEDOYA BASTO JHON EDISSON", "e-jebedoya","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-eabogoya", "1106895551", "BOGOYA SANCHEZ ERIKA ANDREA", "e-eabogoya","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-jabuitrago", "1069733253", "BUITRAGO DIAZ JULY ALEJANDRA", "e-jabuitrago","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-bcaicedo", "1106895321", "CAICEDO MAYORGA BRIGITH TATIANA", "e-bcaicedo","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-rmcalderon", "1106894663", "CALDERON GOMEZ RUBY MARCELA", "e-rmcalderon","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-bcampos", "1106894694", "CAMPOS BARRERO BLANCA ISABEL", "e-bcampos","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-adcardenas", "1106894284", "CARDENAS CANTOR ALFREDO", "e-adcardenas","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-hcardenas", "1069726308", "CARDENAS CANTOR HENRY", "e-hcardenas","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-jduran", "14254986", "DURAN CORTES JOSE DANIEL", "e-jduran","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-ierazo", "1110497696", "ERAZO INGRID YULIETH", "e-ierazo","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-saflorian", "5820355", "FLORIAN SANTIAGO ALEXANDER", "e-saflorian","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-ngarces", "1123203739", "GARCES NORI", "e-ngarces","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-pgarcia", "1071987388", "GARCIA CALDERON PAOLA ANDREA", "e-pgarcia","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-nagarcia", "1098627332", "GARCIA RAMIREZ NATHALIE", "e-nagarcia","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-jmgongora", "1108151008", "GONGORA YARA JUAN MANUEL", "e-jmgongora","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-mvguayara", "1106890098", "GUAYARA MARTINEZ MAGDA VIViANA", "e-mvguayara","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-aguevara", "1024589540", "GUEVARA PILLIMUE ANGIE LORENA", "e-aguevara","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-nguzman", " 65824086", "GUZMAN PALMA NANCY", "e-nguzman","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-lvleal", "1106892249", "LEAL OVALLE LIZETH VIVIANA", "e-lvleal","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-yleiva", "26543045", "LEIVA LAGUNA YENY MARCELA", "e-yleiva","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-jnlopez", "1019078921", "LOPEZ OCHOA JORGE NORLEY", "e-jnlopez","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-gcmahecha", "1106896481", "MAHECHA RODRIGUEZ GINNA CAROLINA", "e-gcmahecha","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-dpmendez", "1106894222", "MENDEZ GUTIERREZ DIANA PAOLA", "e-dpmendez","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-dmolina", "1106307799", "MOLINA LOPEZ DIANA MILENA", "e-dmolina","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-lmontes", "1049604826", "MONTES SUAREZ LUZ MILENA", "e-lmontes","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-ljordonez", "28628222", "ORDOÑEZ RUIZ LEIDY JOHANNA", "e-ljordonez","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-lorduz", "39579295", "ORDUZ LOZANO LAURA MARIA", "e-lorduz","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-lyotigoza", "1106893035", "ORTIGOZA LOZANO LILIANA YADITZA", "e-lyotigoza","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-jcortiz", "1106888568", "ORTIZ TOVAR JENNY CLAUDID", "e-jcortiz","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-erpachon", "1070592244", "PACHON MONTAÑA EDNA ROCIO", "e-erpachon","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-jpardo", "1012429160", "PARDO PARDO JEIMY", "e-jpardo","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-dpava", "1106308784", "PAVA FORERO DYLAN FERNANDO", "e-dpava","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-kyperez", "1110496648", "PEREZ BETANCOURTH KIMBERLY YULIETH", "e-kyperez","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-ppineda", "1022412776", "PINEDA NARANJO PEDRO ANTONIO", "e-ppineda","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-strincon", "1110495984", "RINCON SANCHEZ STEPHANY", "e-strincon","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-oerodriguez", "1069750400", "RODRIGUEZ CALDERON OSCAR ESTEBAN", "e-oerodriguez","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-smrodriguez", "39577117", "RODRIGUEZ CONDE SANDRA MILENA", "e-smrodriguez","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-mmrodriguez", "1070607138", "RODRIGUEZ MONCADA MAYRA ALEJANDRA", "e-mmrodriguez","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-drsandoval", "65823923", "SANDOVAL LOPEZ DIANA ROCIO", "e-drsandoval","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-jsuribe", "1106897108", "URIBE PARRA SEBASTIAN", "e-jsuribe","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-yovargas", "1115861745", "VARGAS YONNY ALEXANDER", "e-yovargas","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-jdvasquez", "14254650", "VASQUEZ JOSE DELFIN", "e-jdvasquez","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-evelandia", "1106308041", "VELANDIA CAICEDO ELLIS CRISANTO", "e-evelandia","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-mvitoviz", "1004302911", "VITOVIS SANCHEZ MARLA LILIA", "e-mvitoviz","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-cyate", "1007650666", "YATE POLOCHE CRISTIAN CAMILO", "e-cyate","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-hfavila", "1106897855", "AVILA NIÑO HENRY FABIAN", "e-hfavila","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-sbarbosa", "1069176944", "BARBOSA SANDRA LILIANA", "e-sbarbosa","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-alcruz", "65824164", "CRUZ MARIN ASTRID LEONOR", "e-alcruz","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-ldaza", "1106307850", "DAZA MOLINA LILIANA PAOLA", "e-ldaza","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-ejimenez", "1069752474", "JIMENEZ DELGADO EDISON", "e-ejimenez","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-eacosta", "1109295966", "ACOSTA GONZALEZ EDNA ROCIO", "e-eacosta","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-cjosorio", "1080296349", "OSORIO VARGAS CARMEN JULIETH", "e-cjosorio","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-dhurtado", "1106901232", "HURTADO SANABRIA DANNA KATERIN", "e-dhurtado","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-yemosquera", "1013604459", "MOSQUERA YENIFFER", "e-yemosquera","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-amedina", "1070585469", "MEDINA ANGELA MARCELA", "e-amedina","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-jabarrios", "1016020216", "BARRIOS PINEDA JOHANNA ALEXANDRA", "e-jabarrios","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-avgarzon", "1106306639", "GARZON LISCANO ANDREA VIVIANA", "e-avgarzon","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-lmflorez", "1110545229", "FLOREZ HERRERA LUIS MIGUEL", "e-lmflorez","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-jvgutierrez", "1106895620", "GUTIERREZ RUIZ JENNIFER VANNESA", "e-jvgutierrez","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-mamendoza", "65785941", "MENDOZA CAMPOS MAGNOLIA", "e-mamendoza","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-bgonzalez", "1106897755", "GONZALEZ LOZANO BRIGITH JULIETH", "e-bgonzalez","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-dmoirena", "1006031046", "CARRANZA MOSCOSO DIANA MAIRENA", "e-dmoirena","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-jmendez", "1106897425", "MENDEZ CUELLAR JESSICA TATIANA", "e-jmendez","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-cmarquez", "1116867389", "MARQUEZ BERNAL CARLOS ALBERTO", "e-cmarquez","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-dpinzon", "1105616345", "PINZON FLOREZ DIANA MARCELA", "e-dpinzon","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-dccalderon", "1106892384", "CALDERON GONZALEZ DIANA CAROLINA", "e-dccalderon","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-odusan", "36286207", "DUSAN CERON ORFA LUCIA", "e-odusan","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-ctole", "1106307485", "TOLE GARZON CARLOS HUMBERTO", "e-ctole","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-edortega", "16015564", "ORTEGA MONSALVE EDWIN", "e-edortega","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-mcparra", "1106897738", "PARRA MARTINEZ MARIA DEL CARMEN", "e-mcparra","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-crojas", "1106307113", "ROJAS GARCIA CARLOS EDUARDO", "e-crojas","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-mcaldon", "1106901310", "CALDON BARRERA MARIA CAMILA", "e-mcaldon","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-lrobledo", "1106898473", "ROBLEDO CERVERA LAURA SOFIA", "e-lrobledo","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-ccalderon", "1106892666", "CALDERON MORA CAMILA MARITZA", "e-ccalderon","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-sholguin", "1118535999", "HOLGUIN SANDRA MILENA", "e-sholguin","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-ycartagena", "39048544", "CARTAGENA CANO YOHANNA INES", "e-ycartagena","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-vpiamba", "1106892608", "PIAMBA RODRIGUEZ VIVAN VANESSA", "e-vpiamba","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-kperea", "1106895338", "PEREA BERNAL KERLY MERCEDES", "e-kperea","","CC","1234","2019-10-01","2019-10-01","co01")
    crearUsuario("e-evinasco", "1106899018", "VINASCO ERAZO EVELYN TATIANA", "e-evinasco","","CC","1234","2019-10-01","2019-10-01","co01")
}

private fun crearSkusYPaquetesBBVA(dependencias: Dependencias, idCliente: Long, impuestoExento: Impuesto, colombia: Ubicacion)
{
    val categoriaBBVA = dependencias.repositorioCategoriasSkus.crear(
            idCliente,
            CategoriaSku(
                    idCliente,
                    null,
                    "BBVA",
                    false,
                    true,
                    false,
                    Precio(Decimal.CERO, impuestoExento.id!!),
                    "-",
                    null,
                    linkedSetOf(),
                    null
                        )
                                                                    )

    dependencias.repositorioEntradas.crear(
            idCliente,
            Entrada(
                    idCliente,
                    null,
                    "ENTRADA PRUEBA DE CONSUMO",
                    true,
                    false,
                    Precio(Decimal.CERO, impuestoExento.id!!),
                    "-",
                    colombia.id!!
                   )
                                          )

    val refrigerioBienvenida = dependencias.repositorioSkus.crear(
            idCliente,
            Sku(
                    idCliente,
                    null,
                    "REFRIGERIO DE BIENVENIDA",
                    false,
                    true,
                    false,
                    Precio(Decimal.CERO, impuestoExento.id!!),
                    "-",
                    categoriaBBVA.id!!,
                    null
               )
                                                                 )

    val desayunoSabado = dependencias.repositorioSkus.crear(
            idCliente,
            Sku(
                    idCliente,
                    null,
                    "DESAYUNO SABADO",
                    false,
                    true,
                    false,
                    Precio(Decimal.CERO, impuestoExento.id!!),
                    "-",
                    categoriaBBVA.id!!,
                    null
               )
                                                           )

    val almuerzoSabado = dependencias.repositorioSkus.crear(
            idCliente,
            Sku(
                    idCliente,
                    null,
                    "ALMUERZO SABADO",
                    false,
                    true,
                    false,
                    Precio(Decimal.CERO, impuestoExento.id!!),
                    "-",
                    categoriaBBVA.id!!,
                    null
               )
                                                           )

    val cenaSabado = dependencias.repositorioSkus.crear(
            idCliente,
            Sku(
                    idCliente,
                    null,
                    "CENA SABADO",
                    false,
                    true,
                    false,
                    Precio(Decimal.CERO, impuestoExento.id!!),
                    "-",
                    categoriaBBVA.id!!,
                    null
               )
                                                       )

    val cervezaUno = dependencias.repositorioSkus.crear(
            idCliente,
            Sku(
                    idCliente,
                    null,
                    "CERVEZA UNO",
                    false,
                    true,
                    false,
                    Precio(Decimal.CERO, impuestoExento.id!!),
                    "-",
                    categoriaBBVA.id!!,
                    null
               )
                                                       )

    val cervezaDos = dependencias.repositorioSkus.crear(
            idCliente,
            Sku(
                    idCliente,
                    null,
                    "CERVEZA DOS",
                    false,
                    true,
                    false,
                    Precio(Decimal.CERO, impuestoExento.id!!),
                    "-",
                    categoriaBBVA.id!!,
                    null
               )
                                                       )

    val desayunoDomingo = dependencias.repositorioSkus.crear(
            idCliente,
            Sku(
                    idCliente,
                    null,
                    "DESAYUNO DOMINGO",
                    false,
                    true,
                    false,
                    Precio(Decimal.CERO, impuestoExento.id!!),
                    "-",
                    categoriaBBVA.id!!,
                    null
               )
                                                            )

    val almuerzoDomingo = dependencias.repositorioSkus.crear(
            idCliente,
            Sku(
                    idCliente,
                    null,
                    "ALMUERZO DOMINGO",
                    false,
                    true,
                    false,
                    Precio(Decimal.CERO, impuestoExento.id!!),
                    "-",
                    categoriaBBVA.id!!,
                    null
               )
                                                            )

    val cenaDomingo = dependencias.repositorioSkus.crear(
            idCliente,
            Sku(
                    idCliente,
                    null,
                    "CENA DOMINGO",
                    false,
                    true,
                    false,
                    Precio(Decimal.CERO, impuestoExento.id!!),
                    "-",
                    categoriaBBVA.id!!,
                    null
               )
                                                        )

    dependencias.repositorioPaquetes.crear(
            idCliente,
            Paquete(
                    idCliente,
                    null,
                    "Paquete Evento BBVA",
                    "Productos para una persona asistente al evento del BBVA",
                    true,
                    ZonedDateTime.of(2018, 1, 1, 0, 0, 0, 0, ZONA_HORARIA_POR_DEFECTO),
                    ZonedDateTime.of(2038, 1, 1, 0, 0, 0, 0, ZONA_HORARIA_POR_DEFECTO),
                    listOf(
                            Paquete.FondoIncluido(refrigerioBienvenida.id!!, refrigerioBienvenida.codigoExterno, Decimal.UNO),
                            Paquete.FondoIncluido(desayunoSabado.id!!, desayunoSabado.codigoExterno, Decimal.UNO),
                            Paquete.FondoIncluido(almuerzoSabado.id!!, almuerzoSabado.codigoExterno, Decimal.UNO),
                            Paquete.FondoIncluido(cenaSabado.id!!, cenaSabado.codigoExterno, Decimal.UNO),
                            Paquete.FondoIncluido(cervezaUno.id!!, cervezaUno.codigoExterno, Decimal.UNO),
                            Paquete.FondoIncluido(cervezaDos.id!!, cervezaDos.codigoExterno, Decimal.UNO),
                            Paquete.FondoIncluido(desayunoDomingo.id!!, desayunoDomingo.codigoExterno, Decimal.UNO),
                            Paquete.FondoIncluido(almuerzoDomingo.id!!, almuerzoDomingo.codigoExterno, Decimal.UNO),
                            Paquete.FondoIncluido(cenaDomingo.id!!, cenaDomingo.codigoExterno, Decimal.UNO)
                          ),
                    "-"
                   )
                                          )
}