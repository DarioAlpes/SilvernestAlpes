package co.smartobjects.red.clientes.retrofit

import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.ParserRespuestasRetrofitImpl
import co.smartobjects.red.clientes.clientes.LlavesNFCAPI
import co.smartobjects.red.clientes.clientes.LlavesNFCAPIRetrofit
import co.smartobjects.red.clientes.fondos.*
import co.smartobjects.red.clientes.fondos.libros.*
import co.smartobjects.red.clientes.fondos.precios.GruposClientesAPI
import co.smartobjects.red.clientes.fondos.precios.GruposClientesAPIRetrofit
import co.smartobjects.red.clientes.fondos.precios.ImpuestosAPI
import co.smartobjects.red.clientes.fondos.precios.ImpuestosAPIRetrofit
import co.smartobjects.red.clientes.operativas.compras.ComprasAPI
import co.smartobjects.red.clientes.operativas.compras.ComprasAPIRetrofit
import co.smartobjects.red.clientes.operativas.ordenes.LoteDeOrdenesAPI
import co.smartobjects.red.clientes.operativas.ordenes.LoteDeOrdenesAPIRetrofit
import co.smartobjects.red.clientes.operativas.ordenes.OrdenesAPI
import co.smartobjects.red.clientes.operativas.ordenes.OrdenesAPIRetrofit
import co.smartobjects.red.clientes.operativas.reservas.ReservasAPI
import co.smartobjects.red.clientes.operativas.reservas.ReservasAPIRetrofit
import co.smartobjects.red.clientes.operativas.reservas.sesionesdemanilla.*
import co.smartobjects.red.clientes.personas.*
import co.smartobjects.red.clientes.personas.compras.ComprasDeUnaPersonaAPI
import co.smartobjects.red.clientes.personas.compras.ComprasDeUnaPersonaAPIRetrofit
import co.smartobjects.red.clientes.personas.creditos.CreditosDeUnaPersonaAPI
import co.smartobjects.red.clientes.personas.creditos.CreditosDeUnaPersonaAPIRetrofit
import co.smartobjects.red.clientes.retrofit.clientes.LlavesNFCRetrofitAPI
import co.smartobjects.red.clientes.retrofit.fondos.*
import co.smartobjects.red.clientes.retrofit.fondos.libros.LibrosDePreciosRetrofitAPI
import co.smartobjects.red.clientes.retrofit.fondos.libros.LibrosDeProhibicionesRetrofitAPI
import co.smartobjects.red.clientes.retrofit.fondos.libros.LibrosSegunReglasCompletoRetrofitAPI
import co.smartobjects.red.clientes.retrofit.fondos.libros.LibrosSegunReglasRetrofitAPI
import co.smartobjects.red.clientes.retrofit.fondos.precios.GruposClientesRetrofitAPI
import co.smartobjects.red.clientes.retrofit.fondos.precios.ImpuestosRetrofitAPI
import co.smartobjects.red.clientes.retrofit.operativas.compras.ComprasRetrofitAPI
import co.smartobjects.red.clientes.retrofit.operativas.ordenes.LoteDeOrdenesRetrofitAPI
import co.smartobjects.red.clientes.retrofit.operativas.ordenes.OrdenesRetrofitAPI
import co.smartobjects.red.clientes.retrofit.operativas.reservas.ReservasRetrofitAPI
import co.smartobjects.red.clientes.retrofit.operativas.reservas.sesionesdemanilla.OrdenesDeUnaSesionDeManillaRetrofitAPI
import co.smartobjects.red.clientes.retrofit.operativas.reservas.sesionesdemanilla.PersonaPorIdSesionManillaRetrofitAPI
import co.smartobjects.red.clientes.retrofit.operativas.reservas.sesionesdemanilla.SesionDeManillaRetrofitAPI
import co.smartobjects.red.clientes.retrofit.personas.*
import co.smartobjects.red.clientes.retrofit.personas.compras.ComprasDeUnaPersonaRetrofitAPI
import co.smartobjects.red.clientes.retrofit.personas.creditos.CreditosDeUnaPersonaRetrofitAPI
import co.smartobjects.red.clientes.retrofit.ubicaciones.UbicacionesRetrofitAPI
import co.smartobjects.red.clientes.retrofit.ubicaciones.consumibles.ConsumiblesEnPuntoDeVentaRetrofitAPI
import co.smartobjects.red.clientes.retrofit.ubicaciones.consumibles.FondosEnPuntoDeVentaRetrofitAPI
import co.smartobjects.red.clientes.retrofit.ubicaciones.contabilizables.ConteosEnUbicacionRetrofitAPI
import co.smartobjects.red.clientes.retrofit.ubicaciones.contabilizables.ConteosUbicacionesRetrofitAPI
import co.smartobjects.red.clientes.retrofit.ubicaciones.contabilizables.UbicacionesContabilizablesRetrofitAPI
import co.smartobjects.red.clientes.retrofit.usuarios.PermisosPosiblesRetrofitAPI
import co.smartobjects.red.clientes.retrofit.usuarios.RolesRetrofitAPI
import co.smartobjects.red.clientes.retrofit.usuarios.UsuariosRetrofitAPI
import co.smartobjects.red.clientes.ubicaciones.UbicacionesAPI
import co.smartobjects.red.clientes.ubicaciones.UbicacionesAPIRetrofit
import co.smartobjects.red.clientes.ubicaciones.consumibles.ConsumiblesEnPuntoDeVentaAPI
import co.smartobjects.red.clientes.ubicaciones.consumibles.ConsumiblesEnPuntoDeVentaAPIRetrofit
import co.smartobjects.red.clientes.ubicaciones.consumibles.FondosEnPuntoDeVentaAPI
import co.smartobjects.red.clientes.ubicaciones.consumibles.FondosEnPuntoDeVentaAPIRetrofit
import co.smartobjects.red.clientes.ubicaciones.contabilizables.*
import co.smartobjects.red.clientes.usuarios.*
import okhttp3.CookieJar
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.net.CookieManager
import java.util.concurrent.TimeUnit

interface ManejadorDePeticiones
{
    val apiDeAccesos: AccesosAPI
    val apiDeCamposDePersona: CamposDePersonaAPI
    val apiDeCampoDePersona: CampoDePersonaAPI
    val apiDeCategoriasSku: CategoriasSkuAPI
    val apiDeCompras: ComprasAPI
    val apiDeConteosUbicaciones: ConteosUbicacionesAPI
    val apiDeEntradas: EntradasAPI
    val apiDeFondos: FondosAPI
    val apiDeGruposClientes: GruposClientesAPI
    val apiDeImpuestos: ImpuestosAPI
    val apiDeLibrosDePrecios: LibrosDePreciosAPI
    val apiDeLibrosDeProhibiciones: LibrosDeProhibicionesAPI
    val apiDeLibrosSegunReglas: LibrosSegunReglasAPI
    val apiDeLibrosSegunReglasCompleto: LibrosSegunReglasCompletoAPI
    val apiDeLlavesNFC: LlavesNFCAPI
    val apiDeLoteDeOrdenes: LoteDeOrdenesAPI
    val apiDeMonedas: MonedasAPI
    val apiDeOrdenes: OrdenesAPI
    val apiDePaquetes: PaquetesAPI
    val apiDePermisosPosibles: PermisosPosiblesAPI
    val apiDePersonas: PersonasAPI
    val apiDeComprasDeUnaPersona: ComprasDeUnaPersonaAPI
    val apiDeCreditosDeUnaPersona: CreditosDeUnaPersonaAPI
    val apiDePersonasDeUnaCompra: PersonasDeUnaCompraAPI
    val apiDeReservas: ReservasAPI
    val apiDeRoles: RolesAPI
    val apiDeSesionDeManilla: SesionDeManillaAPI
    val apiDeOrdenesDeUnaSesionDeManilla: OrdenesDeUnaSesionDeManillaAPI
    val apiPersonaPorIdSesionManilla: PersonaPorIdSesionManillaAPI
    val apiDeSkus: SkusAPI
    val apiDeUbicaciones: UbicacionesAPI
    val apiDeUbicacionesContabilizables: UbicacionesContabilizablesAPI
    val apiDeConsumiblesEnPuntoDeVenta: ConsumiblesEnPuntoDeVentaAPI
    val apiDeConteosEnUbicacion: ConteosEnUbicacionAPI
    val apiDeFondosEnPuntoDeVenta: FondosEnPuntoDeVentaAPI
    val apiDeUsuarios: UsuariosAPI
    val apiDeValoresGruposEdad: ValoresGrupoEdadAPI

    val urlBaseEnUso: String
}

class ManejadorDePeticionesRetrofit internal constructor
(
        idCliente: Long,
        @Suppress("CanBeParameter") private val urlBase: String,
        parserDeRespuestas: ParserRespuestasRetrofit,
        cookieJarPersistible: CookieJar? = null,
        modificadorBuilder: (okHttpBuilder: OkHttpClient.Builder) -> OkHttpClient.Builder = { it }
) : ManejadorDePeticiones
{
    constructor(
            idCliente: Long,
            urlBase: String,
            cookieJarPersistible: CookieJar? = null,
            modificadorBuilder: (okHttpBuilder: OkHttpClient.Builder) -> OkHttpClient.Builder = { it }
               )
            : this(
            idCliente,
            urlBase,
            ParserRespuestasRetrofitImpl(),
            cookieJarPersistible,
            modificadorBuilder
                  )

    override val apiDeAccesos: AccesosAPI
    override val apiDeCamposDePersona: CamposDePersonaAPI
    override val apiDeCampoDePersona: CampoDePersonaAPI
    override val apiDeCategoriasSku: CategoriasSkuAPI
    override val apiDeCompras: ComprasAPI
    override val apiDeConteosUbicaciones: ConteosUbicacionesAPI
    override val apiDeEntradas: EntradasAPI
    override val apiDeFondos: FondosAPI
    override val apiDeGruposClientes: GruposClientesAPI
    override val apiDeImpuestos: ImpuestosAPI
    override val apiDeLibrosDePrecios: LibrosDePreciosAPI
    override val apiDeLibrosDeProhibiciones: LibrosDeProhibicionesAPI
    override val apiDeLibrosSegunReglas: LibrosSegunReglasAPI
    override val apiDeLibrosSegunReglasCompleto: LibrosSegunReglasCompletoAPI
    override val apiDeLlavesNFC: LlavesNFCAPI
    override val apiDeLoteDeOrdenes: LoteDeOrdenesAPI
    override val apiDeMonedas: MonedasAPI
    override val apiDeOrdenes: OrdenesAPI
    override val apiDePaquetes: PaquetesAPI
    override val apiDePermisosPosibles: PermisosPosiblesAPI
    override val apiDePersonas: PersonasAPI
    override val apiDeComprasDeUnaPersona: ComprasDeUnaPersonaAPI
    override val apiDeCreditosDeUnaPersona: CreditosDeUnaPersonaAPI
    override val apiDePersonasDeUnaCompra: PersonasDeUnaCompraAPI
    override val apiDeReservas: ReservasAPI
    override val apiDeRoles: RolesAPI
    override val apiDeSesionDeManilla: SesionDeManillaAPI
    override val apiDeOrdenesDeUnaSesionDeManilla: OrdenesDeUnaSesionDeManillaAPI
    override val apiPersonaPorIdSesionManilla: PersonaPorIdSesionManillaAPI
    override val apiDeSkus: SkusAPI
    override val apiDeUbicaciones: UbicacionesAPI
    override val apiDeUbicacionesContabilizables: UbicacionesContabilizablesAPI
    override val apiDeConsumiblesEnPuntoDeVenta: ConsumiblesEnPuntoDeVentaAPI
    override val apiDeConteosEnUbicacion: ConteosEnUbicacionAPI
    override val apiDeFondosEnPuntoDeVenta: FondosEnPuntoDeVentaAPI
    override val apiDeUsuarios: UsuariosAPI
    override val apiDeValoresGruposEdad: ValoresGrupoEdadAPI

    override val urlBaseEnUso: String = if (!urlBase.endsWith("/")) "$urlBase/" else urlBase
    private val clienteOkHttp: OkHttpClient

    init
    {
        val cookieJar = cookieJarPersistible
                        ?: JavaNetCookieJar(CookieManager().apply { setCookiePolicy(java.net.CookiePolicy.ACCEPT_ALL) })

        clienteOkHttp = modificadorBuilder(
                OkHttpClient.Builder()
                    .cookieJar(cookieJar)
                    .connectTimeout(21, TimeUnit.SECONDS)
                    .readTimeout(21, TimeUnit.SECONDS)
                    .writeTimeout(21, TimeUnit.SECONDS)
                                          )
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(urlBaseEnUso)
            .client(clienteOkHttp)
            .addConverterFactory(JacksonConverterFactory.create(ConfiguracionJackson.objectMapperDeJackson))
            .build()

        apiDeAccesos = AccesosAPIRetrofit(retrofit.create(AccesosRetrofitAPI::class.java), parserDeRespuestas, idCliente)
        apiDeCamposDePersona = CamposDePersonaAPIRetrofit(retrofit.create(CamposDePersonaRetrofitAPI::class.java), parserDeRespuestas, idCliente)
        apiDeCampoDePersona = CampoDePersonaAPIRetrofit(retrofit.create(CampoDePersonaRetrofitAPI::class.java), parserDeRespuestas, idCliente)
        apiDeCategoriasSku = CategoriasSkuAPIRetrofit(retrofit.create(CategoriasSkuRetrofitAPI::class.java), parserDeRespuestas, idCliente)
        apiDeCompras = ComprasAPIRetrofit(retrofit.create(ComprasRetrofitAPI::class.java), parserDeRespuestas, idCliente)
        apiDeConteosUbicaciones = ConteosUbicacionesAPIRetrofit(retrofit.create(ConteosUbicacionesRetrofitAPI::class.java), parserDeRespuestas, idCliente)
        apiDeEntradas = EntradasAPIRetrofit(retrofit.create(EntradasRetrofitAPI::class.java), parserDeRespuestas, idCliente)
        apiDeFondos = FondosAPIRetrofit(retrofit.create(FondosRetrofitAPI::class.java), parserDeRespuestas, idCliente)
        apiDeGruposClientes = GruposClientesAPIRetrofit(retrofit.create(GruposClientesRetrofitAPI::class.java), parserDeRespuestas, idCliente)
        apiDeImpuestos = ImpuestosAPIRetrofit(retrofit.create(ImpuestosRetrofitAPI::class.java), parserDeRespuestas, idCliente)
        apiDeLibrosDePrecios = LibrosDePreciosAPIRetrofit(retrofit.create(LibrosDePreciosRetrofitAPI::class.java), parserDeRespuestas, idCliente)
        apiDeLibrosDeProhibiciones = LibrosDeProhibicionesAPIRetrofit(retrofit.create(LibrosDeProhibicionesRetrofitAPI::class.java), parserDeRespuestas, idCliente)
        apiDeLibrosSegunReglas = LibrosSegunReglasAPIRetrofit(retrofit.create(LibrosSegunReglasRetrofitAPI::class.java), parserDeRespuestas, idCliente)
        apiDeLibrosSegunReglasCompleto = LibrosSegunReglasCompletoAPIRetrofit(retrofit.create(LibrosSegunReglasCompletoRetrofitAPI::class.java), parserDeRespuestas, idCliente)
        apiDeLlavesNFC = LlavesNFCAPIRetrofit(retrofit.create(LlavesNFCRetrofitAPI::class.java), parserDeRespuestas, idCliente)
        apiDeLoteDeOrdenes = LoteDeOrdenesAPIRetrofit(retrofit.create(LoteDeOrdenesRetrofitAPI::class.java), parserDeRespuestas, idCliente)
        apiDeMonedas = MonedasAPIRetrofit(retrofit.create(MonedasRetrofitAPI::class.java), parserDeRespuestas, idCliente)
        apiDeOrdenes = OrdenesAPIRetrofit(retrofit.create(OrdenesRetrofitAPI::class.java), parserDeRespuestas, idCliente)
        apiDePaquetes = PaquetesAPIRetrofit(retrofit.create(PaquetesRetrofitAPI::class.java), parserDeRespuestas, idCliente)
        apiDePermisosPosibles = PermisosPosiblesAPIRetrofit(retrofit.create(PermisosPosiblesRetrofitAPI::class.java), parserDeRespuestas, idCliente)
        apiDePersonas = PersonasAPIRetrofit(retrofit.create(PersonasRetrofitAPI::class.java), parserDeRespuestas, idCliente)
        apiDeComprasDeUnaPersona = ComprasDeUnaPersonaAPIRetrofit(retrofit.create(ComprasDeUnaPersonaRetrofitAPI::class.java), parserDeRespuestas, idCliente)
        apiDeCreditosDeUnaPersona = CreditosDeUnaPersonaAPIRetrofit(retrofit.create(CreditosDeUnaPersonaRetrofitAPI::class.java), parserDeRespuestas, idCliente)
        apiDePersonasDeUnaCompra = PersonasDeUnaCompraAPIRetrofit(retrofit.create(PersonasDeUnaCompraRetrofitAPI::class.java), parserDeRespuestas, idCliente)
        apiDeReservas = ReservasAPIRetrofit(retrofit.create(ReservasRetrofitAPI::class.java), parserDeRespuestas, idCliente)
        apiDeRoles = RolesAPIRetrofit(retrofit.create(RolesRetrofitAPI::class.java), parserDeRespuestas, idCliente)
        apiDeSesionDeManilla = SesionDeManillaAPIRetrofit(retrofit.create(SesionDeManillaRetrofitAPI::class.java), parserDeRespuestas, idCliente)
        apiDeOrdenesDeUnaSesionDeManilla = OrdenesDeUnaSesionDeManillaAPIRetrofit(retrofit.create(OrdenesDeUnaSesionDeManillaRetrofitAPI::class.java), parserDeRespuestas, idCliente)
        apiPersonaPorIdSesionManilla = PersonaPorIdSesionManillaAPIRetrofit(retrofit.create(PersonaPorIdSesionManillaRetrofitAPI::class.java), parserDeRespuestas, idCliente)
        apiDeSkus = SkusAPIRetrofit(retrofit.create(SkusRetrofitAPI::class.java), parserDeRespuestas, idCliente)
        apiDeUbicaciones = UbicacionesAPIRetrofit(retrofit.create(UbicacionesRetrofitAPI::class.java), parserDeRespuestas, idCliente)
        apiDeUbicacionesContabilizables = UbicacionesContabilizablesAPIRetrofit(retrofit.create(UbicacionesContabilizablesRetrofitAPI::class.java), parserDeRespuestas, idCliente)
        apiDeConsumiblesEnPuntoDeVenta = ConsumiblesEnPuntoDeVentaAPIRetrofit(retrofit.create(ConsumiblesEnPuntoDeVentaRetrofitAPI::class.java), parserDeRespuestas, idCliente)
        apiDeConteosEnUbicacion = ConteosEnUbicacionAPIRetrofit(retrofit.create(ConteosEnUbicacionRetrofitAPI::class.java), parserDeRespuestas, idCliente)
        apiDeFondosEnPuntoDeVenta = FondosEnPuntoDeVentaAPIRetrofit(retrofit.create(FondosEnPuntoDeVentaRetrofitAPI::class.java), parserDeRespuestas, idCliente)
        apiDeUsuarios = UsuariosAPIRetrofit(retrofit.create(UsuariosRetrofitAPI::class.java), parserDeRespuestas, idCliente)
        apiDeValoresGruposEdad = ValoresGrupoEdadAPIRetrofit(retrofit.create(ValoresGrupoEdadRetrofitAPI::class.java), parserDeRespuestas, idCliente)
    }
}
