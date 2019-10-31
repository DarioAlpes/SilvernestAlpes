package co.smartobjects.sincronizadordecontenido

import co.smartobjects.entidades.clientes.Cliente
import co.smartobjects.entidades.fondos.*
import co.smartobjects.entidades.fondos.libros.LibroDePrecios
import co.smartobjects.entidades.fondos.libros.LibroDeProhibiciones
import co.smartobjects.entidades.fondos.libros.LibroSegunReglas
import co.smartobjects.entidades.fondos.libros.LibroSegunReglasCompleto
import co.smartobjects.entidades.fondos.precios.GrupoClientes
import co.smartobjects.entidades.fondos.precios.Impuesto
import co.smartobjects.entidades.personas.ValorGrupoEdad
import co.smartobjects.entidades.ubicaciones.Ubicacion
import co.smartobjects.entidades.ubicaciones.contabilizables.UbicacionesContabilizables
import co.smartobjects.persistencia.basederepositorios.Creable
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorio
import co.smartobjects.persistencia.clientes.llavesnfc.RepositorioLlavesNFC
import co.smartobjects.persistencia.fondos.acceso.RepositorioAccesos
import co.smartobjects.persistencia.fondos.acceso.RepositorioEntradas
import co.smartobjects.persistencia.fondos.categoriaskus.RepositorioCategoriasSkus
import co.smartobjects.persistencia.fondos.libros.RepositorioLibrosDePrecios
import co.smartobjects.persistencia.fondos.libros.RepositorioLibrosDeProhibiciones
import co.smartobjects.persistencia.fondos.libros.RepositorioLibrosSegunReglas
import co.smartobjects.persistencia.fondos.monedas.RepositorioMonedas
import co.smartobjects.persistencia.fondos.paquete.RepositorioPaquetes
import co.smartobjects.persistencia.fondos.precios.gruposclientes.RepositorioGrupoClientes
import co.smartobjects.persistencia.fondos.precios.impuestos.RepositorioImpuestos
import co.smartobjects.persistencia.fondos.skus.RepositorioSkus
import co.smartobjects.persistencia.personas.valorgrupoedad.RepositorioValoresGruposEdad
import co.smartobjects.persistencia.ubicaciones.RepositorioUbicaciones
import co.smartobjects.persistencia.ubicaciones.contabilizables.RepositorioUbicacionesContabilizables
import co.smartobjects.red.clientes.base.ConsultarAPI
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.clientes.LlavesNFCAPI
import co.smartobjects.red.clientes.fondos.*
import co.smartobjects.red.clientes.fondos.libros.LibrosSegunReglasCompletoAPI
import co.smartobjects.red.clientes.fondos.precios.GruposClientesAPI
import co.smartobjects.red.clientes.fondos.precios.ImpuestosAPI
import co.smartobjects.red.clientes.personas.ValoresGrupoEdadAPI
import co.smartobjects.red.clientes.ubicaciones.UbicacionesAPI
import co.smartobjects.red.clientes.ubicaciones.contabilizables.UbicacionesContabilizablesAPI
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatcher
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.*
import org.threeten.bp.ZonedDateTime
import kotlin.test.assertTrue
import kotlin.test.fail


internal class GestorDescargaDeDatosPruebas : PruebasConRxJava()
{
    companion object
    {
        private const val ID_CLIENTE = 1L
    }

    private val mockApiUbicaciones: UbicacionesAPI = mockConDefaultAnswer(UbicacionesAPI::class.java)
    private val mockApiUbicacionesContabilizables: UbicacionesContabilizablesAPI = mockConDefaultAnswer(UbicacionesContabilizablesAPI::class.java)
    private val mockApiDeImpuestos: ImpuestosAPI = mockConDefaultAnswer(ImpuestosAPI::class.java)
    private val mockApiCategoriasSku: CategoriasSkuAPI = mockConDefaultAnswer(CategoriasSkuAPI::class.java)
    private val mockApiMonedas: MonedasAPI = mockConDefaultAnswer(MonedasAPI::class.java)
    private val mockApiSkus: SkusAPI = mockConDefaultAnswer(SkusAPI::class.java)
    private val mockApiEntradas: EntradasAPI = mockConDefaultAnswer(EntradasAPI::class.java)
    private val mockApiAccesos: AccesosAPI = mockConDefaultAnswer(AccesosAPI::class.java)
    private val mockApiDePaquetes: PaquetesAPI = mockConDefaultAnswer(PaquetesAPI::class.java)
    private val mockApiDeLibrosSegunReglasCompleto: LibrosSegunReglasCompletoAPI = mockConDefaultAnswer(LibrosSegunReglasCompletoAPI::class.java)
    private val mockApiDeGruposClientes: GruposClientesAPI = mockConDefaultAnswer(GruposClientesAPI::class.java)
    private val mockApiDeValoresGrupoEdad: ValoresGrupoEdadAPI = mockConDefaultAnswer(ValoresGrupoEdadAPI::class.java)
    private val mockApiLlavesNFC: LlavesNFCAPI = mockConDefaultAnswer(LlavesNFCAPI::class.java)


    private val mockRepositorioUbicaciones: RepositorioUbicaciones = mockConDefaultAnswer(RepositorioUbicaciones::class.java)
    private val mockRepositorioUbicacionesContabilizables: RepositorioUbicacionesContabilizables = mockConDefaultAnswer(RepositorioUbicacionesContabilizables::class.java)
    private val mockRepositorioImpuestos: RepositorioImpuestos = mockConDefaultAnswer(RepositorioImpuestos::class.java)
    private val mockRepositorioAccesos: RepositorioAccesos = mockConDefaultAnswer(RepositorioAccesos::class.java)
    private val mockRepositorioEntradas: RepositorioEntradas = mockConDefaultAnswer(RepositorioEntradas::class.java)
    private val mockRepositorioCategoriasSkus: RepositorioCategoriasSkus = mockConDefaultAnswer(RepositorioCategoriasSkus::class.java)
    private val mockRepositorioSkus: RepositorioSkus = mockConDefaultAnswer(RepositorioSkus::class.java)
    private val mockRepositorioMonedas: RepositorioMonedas = mockConDefaultAnswer(RepositorioMonedas::class.java)
    private val mockRepositorioPaquetes: RepositorioPaquetes = mockConDefaultAnswer(RepositorioPaquetes::class.java)
    private val mockRepositorioLibrosSegunReglas: RepositorioLibrosSegunReglas = mockConDefaultAnswer(RepositorioLibrosSegunReglas::class.java)
    private val mockRepositorioLibrosDePrecios: RepositorioLibrosDePrecios = mockConDefaultAnswer(RepositorioLibrosDePrecios::class.java)
    private val mockRepositorioLibrosDeProhibiciones: RepositorioLibrosDeProhibiciones = mockConDefaultAnswer(RepositorioLibrosDeProhibiciones::class.java)
    private val mockRepositorioGrupoClientes: RepositorioGrupoClientes = mockConDefaultAnswer(RepositorioGrupoClientes::class.java)
    private val mockRepositorioValoresGruposEdad: RepositorioValoresGruposEdad = mockConDefaultAnswer(RepositorioValoresGruposEdad::class.java)
    private val mockRepositorioLlavesNFC: RepositorioLlavesNFC = mockConDefaultAnswer(RepositorioLlavesNFC::class.java)


    private val apis = GestorDescargaDeDatosImpl.APIs(
            mockApiUbicaciones,
            mockApiUbicacionesContabilizables,
            mockApiDeImpuestos,
            mockApiAccesos,
            mockApiEntradas,
            mockApiCategoriasSku,
            mockApiSkus,
            mockApiMonedas,
            mockApiDePaquetes,
            mockApiDeLibrosSegunReglasCompleto,
            mockApiDeGruposClientes,
            mockApiDeValoresGrupoEdad,
            mockApiLlavesNFC
                                                     )

    private val repositorios = GestorDescargaDeDatosImpl.Repositorios(
            mockRepositorioUbicaciones,
            mockRepositorioUbicacionesContabilizables,
            mockRepositorioImpuestos,
            mockRepositorioAccesos,
            mockRepositorioEntradas,
            mockRepositorioCategoriasSkus,
            mockRepositorioSkus,
            mockRepositorioMonedas,
            mockRepositorioPaquetes,
            mockRepositorioLibrosSegunReglas,
            mockRepositorioLibrosDePrecios,
            mockRepositorioLibrosDeProhibiciones,
            mockRepositorioGrupoClientes,
            mockRepositorioValoresGruposEdad,
            mockRepositorioLlavesNFC
                                                                     )

    private val gestorDeDescargas =
            GestorDescargaDeDatosImpl(
                    ID_CLIENTE,
                    apis,
                    repositorios,
                    Schedulers.trampoline(),
                    Schedulers.trampoline()
                                     )

    private fun mockearApisParaResponderListasVacias()
    {
        doReturn(RespuestaIndividual.Exitosa(listOf<Ubicacion>())).`when`(mockApiUbicaciones).consultar()
        doReturn(RespuestaIndividual.Exitosa(listOf<Long>())).`when`(mockApiUbicacionesContabilizables).consultar()
        doReturn(RespuestaIndividual.Exitosa(listOf<Impuesto>())).`when`(mockApiDeImpuestos).consultar()
        doReturn(RespuestaIndividual.Exitosa(listOf<CategoriaSku>())).`when`(mockApiCategoriasSku).consultar()
        doReturn(RespuestaIndividual.Exitosa(listOf<Dinero>())).`when`(mockApiMonedas).consultar()
        doReturn(RespuestaIndividual.Exitosa(listOf<Sku>())).`when`(mockApiSkus).consultar()
        doReturn(RespuestaIndividual.Exitosa(listOf<Entrada>())).`when`(mockApiEntradas).consultar()
        doReturn(RespuestaIndividual.Exitosa(listOf<Acceso>())).`when`(mockApiAccesos).consultar()
        doReturn(RespuestaIndividual.Exitosa(listOf<Paquete>())).`when`(mockApiDePaquetes).consultar()
        doReturn(RespuestaIndividual.Exitosa(listOf<LibroSegunReglasCompleto<*>>())).`when`(mockApiDeLibrosSegunReglasCompleto).consultar()
        doReturn(RespuestaIndividual.Exitosa(listOf<GrupoClientes>())).`when`(mockApiDeGruposClientes).consultar()
        doReturn(RespuestaIndividual.Exitosa(listOf<ValorGrupoEdad>())).`when`(mockApiDeValoresGrupoEdad).consultar()
        doReturn(RespuestaIndividual.Exitosa(mockConDefaultAnswer(Cliente.LlaveNFC::class.java))).`when`(mockApiLlavesNFC).consultar(cualquiera())
    }

    private fun mockearRepositoriosParaNoLimpiar()
    {
        doNothing().`when`(mockRepositorioUbicaciones).limpiarParaCliente(anyLong())
        doNothing().`when`(mockRepositorioUbicacionesContabilizables).limpiarParaCliente(anyLong())
        doNothing().`when`(mockRepositorioImpuestos).limpiarParaCliente(anyLong())
        doNothing().`when`(mockRepositorioAccesos).limpiarParaCliente(anyLong())
        doNothing().`when`(mockRepositorioEntradas).limpiarParaCliente(anyLong())
        doNothing().`when`(mockRepositorioCategoriasSkus).limpiarParaCliente(anyLong())
        doNothing().`when`(mockRepositorioSkus).limpiarParaCliente(anyLong())
        doNothing().`when`(mockRepositorioMonedas).limpiarParaCliente(anyLong())
        doNothing().`when`(mockRepositorioPaquetes).limpiarParaCliente(anyLong())
        doNothing().`when`(mockRepositorioLibrosSegunReglas).limpiarParaCliente(anyLong())
        doNothing().`when`(mockRepositorioLibrosDePrecios).limpiarParaCliente(anyLong())
        doNothing().`when`(mockRepositorioLibrosDeProhibiciones).limpiarParaCliente(anyLong())
        doNothing().`when`(mockRepositorioGrupoClientes).limpiarParaCliente(anyLong())
        doNothing().`when`(mockRepositorioValoresGruposEdad).limpiarParaCliente(anyLong())
        doNothing().`when`(mockRepositorioLlavesNFC).limpiarParaCliente(anyLong())
    }

    private fun mockearRepositoriosParaRetornarMock()
    {
        doReturn(mockConDefaultAnswer(Ubicacion::class.java)).`when`(mockRepositorioUbicaciones).crear(anyLong(), cualquiera())
        doReturn(listOf<Long>()).`when`(mockRepositorioUbicacionesContabilizables).crear(anyLong(), cualquiera())
        doReturn(mockConDefaultAnswer(Impuesto::class.java)).`when`(mockRepositorioImpuestos).crear(anyLong(), cualquiera())
        doReturn(mockConDefaultAnswer(Acceso::class.java)).`when`(mockRepositorioAccesos).crear(anyLong(), cualquiera())
        doReturn(mockConDefaultAnswer(Entrada::class.java)).`when`(mockRepositorioEntradas).crear(anyLong(), cualquiera())
        doReturn(mockConDefaultAnswer(CategoriaSku::class.java)).`when`(mockRepositorioCategoriasSkus).crear(anyLong(), cualquiera())
        doReturn(mockConDefaultAnswer(Sku::class.java)).`when`(mockRepositorioSkus).crear(anyLong(), cualquiera())
        doReturn(mockConDefaultAnswer(Dinero::class.java)).`when`(mockRepositorioMonedas).crear(anyLong(), cualquiera())
        doReturn(mockConDefaultAnswer(Paquete::class.java)).`when`(mockRepositorioPaquetes).crear(anyLong(), cualquiera())
        doReturn(mockConDefaultAnswer(LibroSegunReglas::class.java)).`when`(mockRepositorioLibrosSegunReglas).crear(anyLong(), cualquiera())
        doReturn(mockConDefaultAnswer(LibroDePrecios::class.java)).`when`(mockRepositorioLibrosDePrecios).crear(anyLong(), cualquiera())
        doReturn(mockConDefaultAnswer(LibroDeProhibiciones::class.java)).`when`(mockRepositorioLibrosDeProhibiciones).crear(anyLong(), cualquiera())
        doReturn(mockConDefaultAnswer(GrupoClientes::class.java)).`when`(mockRepositorioGrupoClientes).crear(anyLong(), cualquiera())
        doReturn(mockConDefaultAnswer(ValorGrupoEdad::class.java)).`when`(mockRepositorioValoresGruposEdad).crear(anyLong(), cualquiera())
        doReturn(mockConDefaultAnswer(Cliente.LlaveNFC::class.java)).`when`(mockRepositorioLlavesNFC).crear(anyLong(), cualquiera())
    }

    private fun mockearRepositoriosParaNoHacerNada()
    {
        mockearRepositoriosParaNoLimpiar()
        mockearRepositoriosParaRetornarMock()
    }


    @Test
    fun se_invocan_los_endpoints_correctos()
    {
        mockearApisParaResponderListasVacias()
        mockearRepositoriosParaNoHacerNada()

        val apisAConsultar =
                listOf<ConsultarAPI<*>>(
                        mockApiUbicaciones,
                        mockApiUbicacionesContabilizables,
                        mockApiDeImpuestos,
                        mockApiCategoriasSku,
                        mockApiMonedas,
                        mockApiSkus,
                        mockApiEntradas,
                        mockApiAccesos,
                        mockApiDePaquetes,
                        mockApiDeLibrosSegunReglasCompleto,
                        mockApiDeGruposClientes,
                        mockApiDeValoresGrupoEdad
                                       )

        val descarga = gestorDeDescargas.descargarYAlmacenarDatosIndependientesDeLaUbicacion()

        descarga.subscribeBy({ throw it }, { }, { })

        for (api in apisAConsultar)
        {
            verify(api).consultar()
        }
        verify(mockApiLlavesNFC)
            .consultar(argCumpleQue(ArgumentMatcher {
                // Verificar que la fecha esté en un intervalo a segundos de la fecha de hoy
                assertTrue(it.isAfter(ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO).minusSeconds(3)))
                assertTrue(it.isBefore(ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO).plusSeconds(3)))
                true
            }))
    }

    @Test
    fun se_invocan_los_repositorios_correctamente()
    {
        mockearRepositoriosParaNoHacerNada()

        val mockUbicacion = mockConDefaultAnswer(Ubicacion::class.java)
        val ubicacionesContabilizables = listOf(2345L, 2345L, 333L, 333L)
        val mockImpuesto = mockConDefaultAnswer(Impuesto::class.java)
        val mockAcceso = mockConDefaultAnswer(Acceso::class.java)
        val mockEntrada = mockConDefaultAnswer(Entrada::class.java)
        val mockCategoriaSku = mockConDefaultAnswer(CategoriaSku::class.java)
        val mockSku = mockConDefaultAnswer(Sku::class.java)
        val mockDinero = mockConDefaultAnswer(Dinero::class.java)
        val mockPaquete = mockConDefaultAnswer(Paquete::class.java)

        val mockLibroPrecios = mockConDefaultAnswer(LibroDePrecios::class.java)
        val mockLibroSegunReglasPrecio = mockConDefaultAnswer(LibroSegunReglas::class.java)
        val mockLibroSegunReglasCompletoPrecio = mockConDefaultAnswer(LibroSegunReglasCompleto::class.java).also {
            doReturn(mockLibroPrecios).`when`(it).libro
            doReturn(mockLibroSegunReglasPrecio).`when`(it).aLibroSegunReglas()
        }

        val mockLibroProhibiciones = mockConDefaultAnswer(LibroDeProhibiciones::class.java)
        val mockLibroSegunReglasProhibicion = mockConDefaultAnswer(LibroSegunReglas::class.java)
        val mockLibroSegunReglasCompletoProhibicion = mockConDefaultAnswer(LibroSegunReglasCompleto::class.java).also {
            doReturn(mockLibroProhibiciones).`when`(it).libro
            doReturn(mockLibroSegunReglasProhibicion).`when`(it).aLibroSegunReglas()
        }

        val mockGrupoClientes = mockConDefaultAnswer(GrupoClientes::class.java)
        val mockValorGrupoEdad = mockConDefaultAnswer(ValorGrupoEdad::class.java)
        val mockLlaveNFC = mockConDefaultAnswer(Cliente.LlaveNFC::class.java)

        doReturn(RespuestaIndividual.Exitosa(listOf(mockUbicacion))).`when`(mockApiUbicaciones).consultar()
        doReturn(RespuestaIndividual.Exitosa(ubicacionesContabilizables)).`when`(mockApiUbicacionesContabilizables).consultar()
        doReturn(RespuestaIndividual.Exitosa(listOf(mockImpuesto))).`when`(mockApiDeImpuestos).consultar()
        doReturn(RespuestaIndividual.Exitosa(listOf(mockAcceso))).`when`(mockApiAccesos).consultar()
        doReturn(RespuestaIndividual.Exitosa(listOf(mockEntrada))).`when`(mockApiEntradas).consultar()
        doReturn(RespuestaIndividual.Exitosa(listOf(mockCategoriaSku))).`when`(mockApiCategoriasSku).consultar()
        doReturn(RespuestaIndividual.Exitosa(listOf(mockSku))).`when`(mockApiSkus).consultar()
        doReturn(RespuestaIndividual.Exitosa(listOf(mockDinero))).`when`(mockApiMonedas).consultar()
        doReturn(RespuestaIndividual.Exitosa(listOf(mockPaquete))).`when`(mockApiDePaquetes).consultar()
        doReturn(RespuestaIndividual.Exitosa(listOf(mockLibroSegunReglasCompletoPrecio, mockLibroSegunReglasCompletoProhibicion)))
            .`when`(mockApiDeLibrosSegunReglasCompleto)
            .consultar()
        doReturn(RespuestaIndividual.Exitosa(listOf(mockGrupoClientes))).`when`(mockApiDeGruposClientes).consultar()
        doReturn(RespuestaIndividual.Exitosa(listOf(mockValorGrupoEdad))).`when`(mockApiDeValoresGrupoEdad).consultar()
        doReturn(RespuestaIndividual.Exitosa(mockLlaveNFC)).`when`(mockApiLlavesNFC).consultar(cualquiera())


        val descarga = gestorDeDescargas.descargarYAlmacenarDatosIndependientesDeLaUbicacion()

        descarga.subscribeBy({ throw it }, { }, { })


        fun <T, R> verificarInteracciones(mockValorConsultado: T, mockRepositorio: R)
                where R : CreadorRepositorio<T>,
                      R : Creable<T>
        {
            with(inOrder(mockRepositorio))
            {
                verify(mockRepositorio).limpiarParaCliente(ID_CLIENTE)
                verify(mockRepositorio).crear(ID_CLIENTE, mockValorConsultado)
            }
        }

        verificarInteracciones(mockUbicacion, mockRepositorioUbicaciones)

        with(inOrder(mockRepositorioUbicacionesContabilizables))
        {
            verify(mockRepositorioUbicacionesContabilizables).limpiarParaCliente(ID_CLIENTE)
            verify(mockRepositorioUbicacionesContabilizables).crear(ID_CLIENTE, UbicacionesContabilizables(ID_CLIENTE, ubicacionesContabilizables.toSet()))
        }

        verificarInteracciones(mockImpuesto, mockRepositorioImpuestos)
        verificarInteracciones(mockAcceso, mockRepositorioAccesos)
        verificarInteracciones(mockEntrada, mockRepositorioEntradas)
        verificarInteracciones(mockCategoriaSku, mockRepositorioCategoriasSkus)
        verificarInteracciones(mockSku, mockRepositorioSkus)
        verificarInteracciones(mockDinero, mockRepositorioMonedas)
        verificarInteracciones(mockPaquete, mockRepositorioPaquetes)
        verificarInteracciones(mockLibroPrecios, mockRepositorioLibrosDePrecios)
        verificarInteracciones(mockLibroProhibiciones, mockRepositorioLibrosDeProhibiciones)

        with(inOrder(mockRepositorioLibrosSegunReglas))
        {
            verify(mockRepositorioLibrosSegunReglas).limpiarParaCliente(ID_CLIENTE)
            verify(mockRepositorioLibrosSegunReglas).crear(ID_CLIENTE, mockLibroSegunReglasPrecio)
            verify(mockRepositorioLibrosSegunReglas).crear(ID_CLIENTE, mockLibroSegunReglasProhibicion)
        }

        verificarInteracciones(mockGrupoClientes, mockRepositorioGrupoClientes)
        verificarInteracciones(mockValorGrupoEdad, mockRepositorioValoresGruposEdad)

        with(inOrder(mockRepositorioLlavesNFC))
        {
            verify(mockRepositorioLlavesNFC).limpiarParaCliente(ID_CLIENTE)
            verify(mockRepositorioLlavesNFC).crear(ID_CLIENTE, mockLlaveNFC)
        }
    }

    @Nested
    inner class UnaDescargaALaVez
    {
        @Test
        fun funciona_correctamente()
        {
            val schedulerPrueba = TestScheduler()
            val gestorDeDescargasBackground = GestorDescargaDeDatosImpl(ID_CLIENTE, apis, repositorios, schedulerPrueba)

            mockearApisParaResponderListasVacias()
            mockearRepositoriosParaNoHacerNada()

            gestorDeDescargasBackground
                .descargarYAlmacenarDatosIndependientesDeLaUbicacion()
                .subscribeBy({ throw it }, { fail("No debería haber acabo") }, { /*éxito*/ })

            gestorDeDescargasBackground
                .descargarYAlmacenarDatosIndependientesDeLaUbicacion()
                .subscribeBy({ throw it }, { /*éxito*/ }, { fail("Debería haber completado inmediatamente") })

            schedulerPrueba.triggerActions()
        }

        @Test
        fun cuando_la_descarga_actual_termine_se_puede_volver_a_solicitar_una_nueva()
        {
            val schedulerPrueba = TestScheduler()
            val gestorDeDescargasBackground = GestorDescargaDeDatosImpl(ID_CLIENTE, apis, repositorios, schedulerPrueba)

            mockearApisParaResponderListasVacias()
            mockearRepositoriosParaNoHacerNada()

            gestorDeDescargasBackground
                .descargarYAlmacenarDatosIndependientesDeLaUbicacion()
                .subscribeBy({ throw it }, { fail("No debería haber acabo") }, { /*éxito*/ })

            schedulerPrueba.triggerActions()

            gestorDeDescargasBackground
                .descargarYAlmacenarDatosIndependientesDeLaUbicacion()
                .subscribeBy({ throw it }, { fail("No debería haber acabo") }, { /*éxito*/ })
        }

        @Test
        fun si_la_descarga_actual_falla_se_puede_volver_a_solicitar_una_nueva()
        {
            val schedulerPrueba = TestScheduler()
            val gestorDeDescargasBackground = GestorDescargaDeDatosImpl(ID_CLIENTE, apis, repositorios, schedulerPrueba)

            mockearApisParaResponderListasVacias()
            mockearRepositoriosParaNoHacerNada()

            // Forzar que algo falle
            doThrow(IllegalStateException()).`when`(mockApiUbicaciones).consultar()

            gestorDeDescargasBackground
                .descargarYAlmacenarDatosIndependientesDeLaUbicacion()
                .subscribeBy({ }, { fail("No debería haber acabo") }, { fail("No debería haber tenido éxito") })

            schedulerPrueba.triggerActions()

            gestorDeDescargasBackground
                .descargarYAlmacenarDatosIndependientesDeLaUbicacion()
                .subscribeBy({ }, { fail("No debería haber acabo") }, { fail("No debería haber tenido éxito") })
        }
    }

    @Nested
    inner class EmiteErrorAlDescargarSiFalla
    {
        @Test
        fun algun_api()
        {
            mockearApisParaResponderListasVacias()
            mockearRepositoriosParaNoHacerNada()

            val apisAConsultar =
                    listOf<ConsultarAPI<*>>(
                            mockApiUbicaciones,
                            mockApiUbicacionesContabilizables,
                            mockApiDeImpuestos,
                            mockApiCategoriasSku,
                            mockApiMonedas,
                            mockApiSkus,
                            mockApiEntradas,
                            mockApiAccesos,
                            mockApiDePaquetes,
                            mockApiDeLibrosSegunReglasCompleto,
                            mockApiDeGruposClientes,
                            mockApiDeValoresGrupoEdad
                                           )

            for (api in apisAConsultar)
            {
                doThrow(IllegalStateException()).`when`(api).consultar()

                val descarga = gestorDeDescargas.descargarYAlmacenarDatosIndependientesDeLaUbicacion()

                descarga.subscribeBy(
                        { },
                        { fail("'${api::class.java.name}' no debería haber completado") },
                        { fail("'${api::class.java.name}' no debería haber sido exitoso") }
                                    )

                // Reinicar los mocks al estado inicial para poder probar un api a la vez
                mockearApisParaResponderListasVacias()
            }

            doThrow(IllegalStateException()).`when`(mockApiLlavesNFC).consultar(cualquiera())

            val descarga = gestorDeDescargas.descargarYAlmacenarDatosIndependientesDeLaUbicacion()

            descarga.subscribeBy(
                    { },
                    { fail("'${mockApiLlavesNFC::class.java.name}' no debería haber completado") },
                    { fail("'${mockApiLlavesNFC::class.java.name}' no debería haber sido exitoso") }
                                )

            mockearApisParaResponderListasVacias()
        }
    }

    @Nested
    inner class EstaDescargando
    {
        private val schedulerPrueba = TestScheduler()
        private val gestorDeDescargasBackground = GestorDescargaDeDatosImpl(ID_CLIENTE, apis, repositorios, schedulerPrueba, schedulerPrueba)
        private val observadorPrueba = gestorDeDescargasBackground.estaDescargando.test()

        @Test
        fun es_false_si_no_esta_descargando_datos_y_es_true_cuando_esta_descargando()
        {
            mockearApisParaResponderListasVacias()
            mockearRepositoriosParaNoHacerNada()

            observadorPrueba.assertValuesOnly(false)

            gestorDeDescargasBackground
                .descargarYAlmacenarDatosIndependientesDeLaUbicacion()
                .subscribeBy({ throw it }, { fail("No debería haber acabo") }, { /*éxito*/ })

            observadorPrueba.assertValuesOnly(false, true)

            schedulerPrueba.triggerActions()

            observadorPrueba.assertValuesOnly(false, true, false)
        }

        @Test
        fun si_falla_la_descarga_por_peticion_o_por_base_de_datos_emite_false()
        {
            mockearApisParaResponderListasVacias()
            mockearRepositoriosParaNoHacerNada()
            doThrow(IllegalStateException()).`when`(mockApiUbicaciones).consultar()

            gestorDeDescargasBackground
                .descargarYAlmacenarDatosIndependientesDeLaUbicacion()
                .subscribeBy({ }, { fail("No debería haber acabo") }, { fail("No debería haber tenido éxito") })

            schedulerPrueba.triggerActions()

            observadorPrueba.assertValuesOnly(false, true, false)
        }
    }
}