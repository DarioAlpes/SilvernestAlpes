package co.smartobjects.prompterbackend

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.clientes.Cliente
import co.smartobjects.entidades.fondos.*
import co.smartobjects.entidades.fondos.libros.*
import co.smartobjects.entidades.fondos.precios.GrupoClientes
import co.smartobjects.entidades.fondos.precios.Impuesto
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.entidades.fondos.precios.SegmentoClientes
import co.smartobjects.entidades.operativas.EntidadTransaccional
import co.smartobjects.entidades.operativas.compras.Compra
import co.smartobjects.entidades.operativas.compras.CreditoFondo
import co.smartobjects.entidades.operativas.compras.CreditoPaquete
import co.smartobjects.entidades.operativas.compras.Pago
import co.smartobjects.entidades.operativas.reservas.Reserva
import co.smartobjects.entidades.operativas.reservas.SesionDeManilla
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.personas.PersonaConFamiliares
import co.smartobjects.entidades.personas.ValorGrupoEdad
import co.smartobjects.entidades.ubicaciones.Ubicacion
import co.smartobjects.entidades.ubicaciones.contabilizables.UbicacionesContabilizables
import co.smartobjects.entidades.usuarios.Rol
import co.smartobjects.entidades.usuarios.Usuario
import co.smartobjects.prompterbackend.serviciosrest.usuarios.RecursoPermisosPosibles
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.ID_ZONA_HORARIA_POR_DEFECTO
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.apache.shiro.web.env.EnvironmentLoaderListener
import org.apache.shiro.web.servlet.ShiroFilter
import org.glassfish.grizzly.http.server.HttpServer
import org.glassfish.grizzly.servlet.WebappContext
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory
import org.glassfish.jersey.servlet.ServletContainer
import org.glassfish.jersey.servlet.ServletProperties
import org.threeten.bp.LocalDate
import org.threeten.bp.ZonedDateTime
import java.io.IOException
import java.net.URI
import java.util.*
import javax.servlet.DispatcherType
import javax.ws.rs.core.UriBuilder

internal object PrompterBackend
{
    internal val BASE_URI: URI = UriBuilder.fromUri("http://0.0.0.0/").port(80).build()

    internal fun arrancarServidor(): HttpServer
    {
        val webappContext = WebappContext("grizzly web context", "").apply {
            addListener(EnvironmentLoaderListener::class.java)

            with(addFilter("Shiro", ShiroFilter::class.java))
            {
                addMappingForUrlPatterns(
                        EnumSet.of(
                                DispatcherType.INCLUDE,
                                DispatcherType.REQUEST,
                                DispatcherType.FORWARD,
                                DispatcherType.ERROR
                                  ),
                        "/*"
                                        )
            }

            with(addServlet("Jersey", ServletContainer::class.java))
            {
                addMapping("/*")
                setInitParameter(ServletProperties.JAXRS_APPLICATION_CLASS, ConfiguracionAplicacionJersey::class.java.canonicalName)
            }
        }

        return GrizzlyHttpServerFactory.createHttpServer(BASE_URI).also {
            webappContext.deploy(it)
        }
    }

    @[JvmStatic Throws(IOException::class)]
    fun main(args: Array<String>)
    {
        System.setProperty("user.timezone", ID_ZONA_HORARIA_POR_DEFECTO)

        arrancarServidor().start()

        //                poblarConDatosCafam()
        //poblarConDatosFicti()
    }

    @Suppress("UNUSED_VARIABLE")
    private fun poblarConDatosFicti()
    {
        ConfiguracionAplicacionJersey.inicializacionPorDefecto()
        val dependencias = ConfiguracionAplicacionJersey.DEPENDENCIAS
        val clienteUnico = ConfiguracionAplicacionJersey.RECURSO_CLIENTES.crearEntidadDeNegocio(Cliente(null, "El Cliente de Prueba"))
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

        val usuario =
                dependencias.repositorioUsuarios.crear(
                        idCliente,
                        Usuario.UsuarioParaCreacion(
                                Usuario.DatosUsuario(idCliente, "admin", "usuario de prueba", "assf@asf.com", true,"","CC","1234","2019-10-01","2019-10-01","co01"),
                                "admin".toCharArray(),
                                setOf(Rol.RolParaCreacionDeUsuario("Superusuario"))
                                                   )
                                                      )

        val ubicacionPadre1 = dependencias.repositorioUbicaciones.crear(
                idCliente,
                Ubicacion(
                        idCliente,
                        null,
                        "Ubicación 1 - País - AP",
                        Ubicacion.Tipo.PAIS,
                        Ubicacion.Subtipo.AP,
                        null,
                        linkedSetOf()
                         )
                                                                       )

        val ubicacionHija2 = dependencias.repositorioUbicaciones.crear(
                idCliente,
                Ubicacion(
                        idCliente,
                        null,
                        "Ubicación 2 - Región - AP",
                        Ubicacion.Tipo.REGION,
                        Ubicacion.Subtipo.AP,
                        ubicacionPadre1.id,
                        linkedSetOf(ubicacionPadre1.id!!)
                         )
                                                                      )

        val ubicacionHija3 = dependencias.repositorioUbicaciones.crear(
                idCliente,
                Ubicacion(
                        idCliente,
                        null,
                        "Ubicación 3 - Ciudad - AP",
                        Ubicacion.Tipo.CIUDAD,
                        Ubicacion.Subtipo.AP,
                        ubicacionPadre1.id,
                        linkedSetOf(ubicacionPadre1.id!!)
                         )
                                                                      )

        val ubicacionHija4 = dependencias.repositorioUbicaciones.crear(
                idCliente,
                Ubicacion(
                        idCliente,
                        null,
                        "Ubicación 4 - Propiedad - AP",
                        Ubicacion.Tipo.PROPIEDAD,
                        Ubicacion.Subtipo.AP,
                        ubicacionHija2.id,
                        linkedSetOf(*ubicacionHija2.idsDeAncestros.toTypedArray(), ubicacionHija2.id!!)
                         )
                                                                      )

        val ubicacionHija5 = dependencias.repositorioUbicaciones.crear(
                idCliente,
                Ubicacion(
                        idCliente,
                        null,
                        "Ubicación 5 - Zona - AP",
                        Ubicacion.Tipo.ZONA,
                        Ubicacion.Subtipo.AP,
                        ubicacionHija4.id,
                        linkedSetOf(*ubicacionHija4.idsDeAncestros.toTypedArray(), ubicacionHija4.id!!)
                         )
                                                                      )

        val ubicacionHija6 = dependencias.repositorioUbicaciones.crear(
                idCliente,
                Ubicacion(
                        idCliente,
                        null,
                        "Ubicación 6 - Área - AP",
                        Ubicacion.Tipo.AREA,
                        Ubicacion.Subtipo.AP,
                        ubicacionHija4.id,
                        linkedSetOf(*ubicacionHija4.idsDeAncestros.toTypedArray(), ubicacionHija4.id!!)
                         )
                                                                      )

        val ubicacionHija7 = dependencias.repositorioUbicaciones.crear(
                idCliente,
                Ubicacion(
                        idCliente,
                        null,
                        "Ubicación 7 - Punto de contacto - pos sin dinero",
                        Ubicacion.Tipo.PUNTO_DE_CONTACTO,
                        Ubicacion.Subtipo.POS_SIN_DINERO,
                        ubicacionHija6.id,
                        linkedSetOf(*ubicacionHija6.idsDeAncestros.toTypedArray(), ubicacionHija6.id!!)
                         )
                                                                      )

        val ubicacionHija8 = dependencias.repositorioUbicaciones.crear(
                idCliente,
                Ubicacion(
                        idCliente,
                        null,
                        "Ubicación 8 - Punto de contacto contabilizable 1 - pos sin dinero",
                        Ubicacion.Tipo.PUNTO_DE_CONTACTO,
                        Ubicacion.Subtipo.POS_SIN_DINERO,
                        ubicacionHija6.id,
                        linkedSetOf(*ubicacionHija6.idsDeAncestros.toTypedArray(), ubicacionHija6.id!!)
                         )
                                                                      )

        val ubicacionHija9 = dependencias.repositorioUbicaciones.crear(
                idCliente,
                Ubicacion(
                        idCliente,
                        null,
                        "Ubicación 9 - Punto de contacto contabilizable 2 - pos sin dinero",
                        Ubicacion.Tipo.PUNTO_DE_CONTACTO,
                        Ubicacion.Subtipo.POS_SIN_DINERO,
                        ubicacionHija6.id,
                        linkedSetOf(*ubicacionHija6.idsDeAncestros.toTypedArray(), ubicacionHija6.id!!)
                         )
                                                                      )

        dependencias.repositorioUbicacionesContabilizables.crear(
                idCliente, UbicacionesContabilizables(idCliente, setOf(ubicacionHija8.id!!, ubicacionHija9.id!!))
                                                                )


        val impuesto = dependencias.repositorioImpuestos.crear(
                idCliente,
                Impuesto(
                        idCliente,
                        null,
                        "IVA",
                        Decimal(19.0)
                        )
                                                              )

        val grupoDeEdad = dependencias.repositorioValoresGruposEdad.crear(idCliente, ValorGrupoEdad("ADULTO", null, null))

        val grupoCreadoAyAdulto = dependencias.repositorioGrupoClientes.crear(
                idCliente,
                GrupoClientes(null,
                              "Grupo dos segementos",
                              listOf(SegmentoClientes(null,
                                                      SegmentoClientes.NombreCampo.CATEGORIA,
                                                      "A"),
                                     SegmentoClientes(null,
                                                      SegmentoClientes.NombreCampo.GRUPO_DE_EDAD,
                                                      grupoDeEdad.valor)
                                    )
                             )
                                                                             )

        val grupoCreadoB = dependencias.repositorioGrupoClientes.crear(
                idCliente,
                GrupoClientes(null,
                              "Grupo solo B",
                              listOf(SegmentoClientes(null,
                                                      SegmentoClientes.NombreCampo.CATEGORIA,
                                                      "B")
                                    )
                             )
                                                                      )

        val categoriaPadre = dependencias.repositorioCategoriasSkus.crear(
                idCliente,
                CategoriaSku(
                        idCliente,
                        null,
                        "Categoria 1",
                        true,
                        false,
                        false,
                        Precio(Decimal.UNO, impuesto.id!!),
                        "El código externo de categoría 1",
                        null,
                        linkedSetOf(),
                        null
                            )
                                                                         )

        val categoriaHija = dependencias.repositorioCategoriasSkus.crear(
                idCliente,
                CategoriaSku(
                        idCliente,
                        null,
                        "Categoria 2",
                        true,
                        false,
                        false,
                        Precio(Decimal.UNO, impuesto.id!!),
                        "El código externo de categoría 2",
                        categoriaPadre.id,
                        linkedSetOf(categoriaPadre.id!!),
                        null
                            )
                                                                        )

        val sku = dependencias.repositorioSkus.crear(
                idCliente,
                Sku(
                        idCliente,
                        null,
                        "Sku 1",
                        true,
                        false,
                        false,
                        Precio(Decimal.UNO, impuesto.id!!),
                        "El código externo de sku 1",
                        categoriaHija.id!!,
                        null
                   )
                                                    )

        val sku2 = dependencias.repositorioSkus.crear(
                idCliente,
                Sku(
                        idCliente,
                        null,
                        "Sku 2",
                        true,
                        false,
                        false,
                        Precio(Decimal(3), impuesto.id!!),
                        "El código externo de sku 2",
                        categoriaHija.id!!,
                        null
                   )
                                                     )

        val acceso = dependencias.repositorioAccesos.crear(
                idCliente,
                Acceso(
                        idCliente,
                        null,
                        "Acceso 1",
                        true,
                        false,
                        false,
                        Precio(Decimal.UNO, impuesto.id!!),
                        "El código externo de acceso",
                        ubicacionPadre1.id!!
                      )
                                                          )

        val entrada = dependencias.repositorioEntradas.crear(
                idCliente,
                Entrada(
                        idCliente,
                        null,
                        "Entrada 1",
                        true,
                        false,
                        Precio(Decimal.UNO, impuesto.id!!),
                        "El código externo de entrada",
                        ubicacionPadre1.id!!
                       )
                                                            )

        val dinero = dependencias.repositorioMonedas.crear(
                idCliente,
                Dinero(
                        idCliente,
                        null,
                        "Dinero 1",
                        true,
                        false,
                        true,
                        Precio(Decimal.UNO, impuesto.id!!),
                        "El código externo de dinero"
                      )
                                                          )

        val paquete = dependencias.repositorioPaquetes.crear(
                idCliente,
                Paquete(
                        idCliente,
                        null,
                        "Paquete 1",
                        "Paquete",
                        true,
                        ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO),
                        ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO),
                        listOf(
                                Paquete.FondoIncluido(acceso.id!!, "El código externo acceso", Decimal.UNO),
                                Paquete.FondoIncluido(dinero.id!!, "El código externo dinero", Decimal.UNO)
                              ),
                        "El código externo de paquete"
                       )
                                                            )

        val libroDePrecios =
                dependencias.repositorioLibroDePrecios.crear(
                        idCliente,
                        LibroDePrecios(
                                idCliente,
                                null,
                                "Primer libro de precios",
                                setOf(
                                        PrecioEnLibro(Precio(Decimal(123.456), impuesto.id!!), acceso.id!!),
                                        PrecioEnLibro(Precio(Decimal(321.654), impuesto.id!!), entrada.id!!),
                                        PrecioEnLibro(Precio(Decimal(159.487), impuesto.id!!), dinero.id!!)
                                     )
                                      )
                                                            )

        val libroDeProhibiciones =
                dependencias.repositorioLibroDeProhibiciones.crear(
                        idCliente,
                        LibroDeProhibiciones(
                                idCliente,
                                null,
                                "Primer libro de prohibiciones",
                                setOf(Prohibicion.DeFondo(acceso.id!!), Prohibicion.DeFondo(sku.id!!)),
                                setOf(Prohibicion.DePaquete(paquete.id!!))
                                            )
                                                                  )

        val libroPreciosParaCategoriaBYEnUbicacionHija7 =
                dependencias.repositorioLibrosSegunReglas.crear(
                        idCliente,
                        LibroSegunReglas(
                                0,
                                null,
                                "Precio para categoría B en ubicación hija 7",
                                libroDePrecios.id!!,
                                linkedSetOf(ReglaDeIdUbicacion(ubicacionHija7.id!!)),
                                linkedSetOf(ReglaDeIdGrupoDeClientes(grupoCreadoB.id!!)),
                                linkedSetOf()
                                        )
                                                               )

        val libroPrhibicionesParaCategoriaBYEnUbicacionHija7 =
                dependencias.repositorioLibrosSegunReglas.crear(
                        idCliente,
                        LibroSegunReglas(
                                0,
                                null,
                                "Prohibiciones para categoría B en ubicación hija 7",
                                libroDeProhibiciones.id!!,
                                linkedSetOf(ReglaDeIdUbicacion(ubicacionHija7.id!!)),
                                linkedSetOf(ReglaDeIdGrupoDeClientes(grupoCreadoB.id!!)),
                                linkedSetOf()
                                        )
                                                               )

        val personaConCompra = dependencias.repositorioPersonas.crear(
                idCliente,
                Persona(
                        0,
                        null,
                        "El nombre de la persona desde el back",
                        Persona.TipoDocumento.CC,
                        "123",
                        Persona.Genero.DESCONOCIDO,
                        LocalDate.of(2017, 5, 31),
                        Persona.Categoria.A,
                        Persona.Afiliacion.COTIZANTE,
                        false,
                        "llave",
                        "El nombre de la empresa desde el back",
                        "El nit de la persona desde el back",
                        Persona.Tipo.NO_AFILIADO
                       )
                                                                     )

        val compra =
                dependencias.repositorioCompras.crear(
                        idCliente,
                        Compra(
                                1,
                                usuario.datosUsuario.usuario,
                                listOf(
                                        CreditoFondo(
                                                idCliente,
                                                null,
                                                Decimal(1),
                                                Decimal(1),
                                                Decimal(0.19),
                                                null,
                                                null,
                                                false,
                                                "Taquilla",
                                                usuario.datosUsuario.usuario,
                                                personaConCompra.id!!,
                                                entrada.id!!,
                                                "El código externo de entrada",
                                                impuesto.id!!,
                                                "un-uuid-de-dispositivo",
                                                ubicacionHija7.id!!,
                                                grupoCreadoB.id!!
                                                    )
                                      ),
                                listOf(
                                        CreditoPaquete(
                                                paquete.id!!,
                                                "El código externo paquete",
                                                listOf(
                                                        CreditoFondo(
                                                                idCliente,
                                                                null,
                                                                Decimal(1),
                                                                Decimal(1),
                                                                Decimal(0.19),
                                                                null,
                                                                null,
                                                                false,
                                                                "Taquilla",
                                                                usuario.datosUsuario.usuario,
                                                                personaConCompra.id!!,
                                                                acceso.id!!,
                                                                "El código externo de acceso",
                                                                impuesto.id!!,
                                                                "un-uuid-de-dispositivo",
                                                                ubicacionHija7.id!!,
                                                                grupoCreadoB.id!!
                                                                    ),
                                                        CreditoFondo(
                                                                idCliente,
                                                                null,
                                                                Decimal(1),
                                                                Decimal(1),
                                                                Decimal(0.19),
                                                                null,
                                                                null,
                                                                false,
                                                                "Taquilla",
                                                                usuario.datosUsuario.usuario,
                                                                personaConCompra.id!!,
                                                                dinero.id!!,
                                                                "El código externo de dinero",
                                                                impuesto.id!!,
                                                                "un-uuid-de-dispositivo",
                                                                ubicacionHija7.id!!,
                                                                grupoCreadoB.id!!
                                                                    )
                                                      )
                                                      )
                                      ),
                                listOf(Pago(Decimal(3), Pago.MetodoDePago.EFECTIVO, "1234-548")),
                                ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
                              )
                                                     )
                    .copiar(creacionTerminada = true)

        dependencias.repositorioCompras.actualizarCamposIndividuales(
                idCliente,
                compra.id,
                EntidadTransaccional.CampoCreacionTerminada<Compra>(true).let {
                    mapOf<String, CampoModificable<Compra, *>>(it.nombreCampo to it)
                }
                                                                    )


        val familiar1 = dependencias.repositorioPersonas.crear(
                idCliente,
                Persona(
                        0,
                        null,
                        "Primer familiar",
                        Persona.TipoDocumento.CC,
                        "456",
                        Persona.Genero.MASCULINO,
                        LocalDate.of(1988, 1, 1),
                        Persona.Categoria.B,
                        Persona.Afiliacion.BENEFICIARIO,
                        false,
                        "llave",
                        "Primer familiar",
                        "0",
                        Persona.Tipo.NO_AFILIADO
                       )
                                                              )

        val familiar2 = dependencias.repositorioPersonas.crear(
                idCliente,
                Persona(
                        0,
                        null,
                        "Segundo familiar",
                        Persona.TipoDocumento.CC,
                        "789",
                        Persona.Genero.MASCULINO,
                        LocalDate.of(2001, 1, 1),
                        Persona.Categoria.B,
                        Persona.Afiliacion.BENEFICIARIO,
                        false,
                        "llave",
                        "Segundo familiar",
                        "0",
                        Persona.Tipo.NO_AFILIADO
                       )
                                                              )

        val personaConFamiliares = dependencias.repositorioRelacionesPersonas.crear(
                idCliente,
                PersonaConFamiliares(
                        personaConCompra,
                        setOf(familiar1, familiar2)
                                    )
                                                                                   )

        val reserva = dependencias.repositorioReservas.crear(
                idCliente,
                Reserva(
                        idCliente,
                        usuario.datosUsuario.usuario,
                        listOf(
                                SesionDeManilla(
                                        idCliente,
                                        null,
                                        personaConCompra.id!!,
                                        null,
                                        null,
                                        null,
                                        compra.creditos.asSequence().map { it.id!! }.toSet()
                                               )
                              )
                       )
                                                            )
    }
}