package co.smartobjects.ui.modelos.selecciondecreditos

import co.smartobjects.entidades.fondos.CategoriaSku
import co.smartobjects.entidades.fondos.Fondo
import co.smartobjects.entidades.fondos.Paquete
import co.smartobjects.entidades.fondos.libros.LibroSegunReglasCompleto
import co.smartobjects.entidades.fondos.precios.Impuesto
import co.smartobjects.entidades.operativas.compras.*
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.personas.PersonaConGrupoCliente
import co.smartobjects.logica.fondos.CalculadorPuedeAgregarseSegunUnicidad
import co.smartobjects.persistencia.fondos.RepositorioFondos
import co.smartobjects.persistencia.fondos.categoriaskus.RepositorioCategoriasSkus
import co.smartobjects.persistencia.fondos.libros.RepositorioLibrosSegunReglasCompleto
import co.smartobjects.persistencia.fondos.paquete.RepositorioPaquetes
import co.smartobjects.persistencia.fondos.precios.impuestos.RepositorioImpuestos
import co.smartobjects.red.clientes.ProblemaBackend
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.personas.creditos.CreditosDeUnaPersonaAPI
import co.smartobjects.red.modelos.ErrorDePeticion
import co.smartobjects.ui.modelos.*
import co.smartobjects.ui.modelos.carritocreditos.CarritoDeCreditos
import co.smartobjects.ui.modelos.catalogo.ProveedorImagenesProductos
import co.smartobjects.ui.modelos.menufiltrado.ProveedorIconosCategoriasFiltrado
import co.smartobjects.ui.modelos.selecciondecreditos.agrupacioncarritosdecreditos.AgrupacionPersonasCarritosDeCreditosUI
import co.smartobjects.ui.modelos.selecciondecreditos.agrupacioncarritosdecreditos.PersonaConCarrito
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.Opcional
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import io.reactivex.Maybe
import io.reactivex.exceptions.CompositeException
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.*
import org.threeten.bp.LocalDate
import java.io.IOException
import kotlin.test.assertEquals


@DisplayName("ProcesoCompraYSeleccionCreditos")
internal class ProcesoCompraYSeleccionCreditosPruebas : PruebasModelosRxBase()
{
    companion object
    {
        private const val ID_CLIENTE = 1L
    }

    private interface IconoPrueba : ProveedorIconosCategoriasFiltrado.Icono


    @Test
    @Suppress("UNCHECKED_CAST")
    fun los_modelos_hijos_son_solo_el_proceso_de_seleccion_de_creditos()
    {
        val mockConfiguracionDeSesion = mockConDefaultAnswer(ContextoDeSesion::class.java)
        val mockRepositorios = mockConDefaultAnswer(ProcesoCompraYSeleccionCreditos.Repositorios::class.java)
        val mockApi = mockConDefaultAnswer(CreditosDeUnaPersonaAPI::class.java)
        val mockProveedorImagenesProductos = mockConDefaultAnswer(ProveedorImagenesProductos::class.java)
        val mockProveedorIconosCategoriasFiltrado = mockConDefaultAnswer(ProveedorIconosCategoriasFiltrado::class.java)
                as ProveedorIconosCategoriasFiltrado<IconoPrueba>
        val mockProcesoSeleccionCreditos = mockConDefaultAnswer(ProcesoSeleccionCreditosUI::class.java)
                as ProcesoSeleccionCreditosUI<IconoPrueba>

        val procesoCreado =
                ProcesoCompraYSeleccionCreditos(
                        mockConfiguracionDeSesion,
                        mockRepositorios,
                        mockApi,
                        mockProveedorImagenesProductos,
                        mockProveedorIconosCategoriasFiltrado,
                        listOf(mockConDefaultAnswer(PersonaConGrupoCliente::class.java)),
                        mockProcesoSeleccionCreditos
                                               )

        assertEquals(procesoCreado.modelosHijos, listOf(mockProcesoSeleccionCreditos))
    }

    @Nested
    inner class AlInstanciar
    {
        private val entidadNegocioVacia =
                CreditosDeUnaPersona(ID_CLIENTE, 1001001, emptyList(), emptyList())

        private val configuracionDeSesion = ContextoDeSesionImpl(ID_CLIENTE, "origen", "usuario", "id dispositivo", 123L)
        private val mockApi = mockConDefaultAnswer(CreditosDeUnaPersonaAPI::class.java).also {
            doReturn(RespuestaIndividual.Exitosa(entidadNegocioVacia)).`when`(it).consultar(cualquiera())
        }
        private val mockProveedorImagenesProductos = mockConDefaultAnswer(ProveedorImagenesProductos::class.java)
        private val mockIcono = mockConDefaultAnswer(IconoPrueba::class.java)
        private val mockProveedorIconosCategoriasFiltrado =
                mockConDefaultAnswer(ProveedorIconosCategoriasFiltrado::class.java).also {
                    doReturn(mockIcono).`when`(it).darIcono(cualquiera())
                }

        private val mockRepositorioImpuestos =
                mockConDefaultAnswer(RepositorioImpuestos::class.java).also {
                    doReturn(sequenceOf<Impuesto>()).`when`(it).listar(anyLong())
                }
        private val mockRepositorioCategoriasSkus =
                mockConDefaultAnswer(RepositorioCategoriasSkus::class.java).also {
                    doReturn(sequenceOf<CategoriaSku>()).`when`(it).listar(anyLong())
                }
        private val mockRepositorioPaquetes =
                mockConDefaultAnswer(RepositorioPaquetes::class.java).also {
                    doReturn(sequenceOf<Paquete>()).`when`(it).listar(anyLong())
                }
        private val mockRepositorioFondos =
                mockConDefaultAnswer(RepositorioFondos::class.java).also {
                    doReturn(sequenceOf<Fondo<*>>()).`when`(it).listar(anyLong())
                }
        private val mockRepositorioLibrosSegunReglasCompleto =
                mockConDefaultAnswer(RepositorioLibrosSegunReglasCompleto::class.java).also {
                    doReturn(sequenceOf<LibroSegunReglasCompleto<*>>()).`when`(it).listar(anyLong())
                }

        private val repositorios =
                ProcesoCompraYSeleccionCreditos.Repositorios(
                        mockRepositorioImpuestos,
                        mockRepositorioCategoriasSkus,
                        mockRepositorioPaquetes,
                        mockRepositorioFondos,
                        mockRepositorioLibrosSegunReglasCompleto
                                                            )

        private val mockPersona =
                mockConDefaultAnswer(Persona::class.java).also {
                    doReturn(1L).`when`(it).id
                }

        private val modelo =
                ProcesoCompraYSeleccionCreditos(
                        configuracionDeSesion,
                        repositorios,
                        mockApi,
                        mockProveedorImagenesProductos,
                        mockProveedorIconosCategoriasFiltrado,
                        listOf(PersonaConGrupoCliente(mockPersona, null)),
                        Schedulers.trampoline()
                                               )

        @Nested
        inner class ConsultarARepositorios
        {
            @Test
            fun solo_se_consulta_una_vez_el_RepositorioImpuestos()
            {
                verify(mockRepositorioImpuestos).listar(ID_CLIENTE)
            }

            @Test
            fun solo_se_consulta_una_vez_el_RepositorioCategoriasSkus()
            {
                verify(mockRepositorioCategoriasSkus).listar(ID_CLIENTE)
            }

            @Test
            fun solo_se_consulta_una_vez_el_RepositorioPaquetes()
            {
                verify(mockRepositorioPaquetes).listar(ID_CLIENTE)
            }

            @Test
            fun solo_se_consulta_una_vez_el_RepositorioFondos()
            {
                verify(mockRepositorioFondos).listar(ID_CLIENTE)
            }

            @Test
            fun solo_se_consulta_una_vez_el_RepositorioLibrosSegunReglasCompleto()
            {
                // Es necesario simular una consulta al backend porque el respositorio de libtros solo se usa cuando se
                // recibe una respuesta
                modelo.consultarComprasEnFecha(LocalDate.now())

                verify(mockRepositorioLibrosSegunReglasCompleto).listar(ID_CLIENTE)
            }
        }

        @Test
        fun el_estado_de_consulta_inicial_es_esperando()
        {
            modelo.estadoConsulta.test().assertValuesOnly(ProcesoCompraYSeleccionCreditosUI.EstadoConsulta.ESPERANDO)
        }

        @Test
        fun el_mensaje_de_error_es_vacio()
        {
            modelo.mensajeErrorConsulta.test().assertValuesOnly(Opcional.Vacio())
        }
    }

    @Nested
    inner class ConsultarComprasEnFecha
    {
        private val mockConfiguracionDeSesion = ContextoDeSesionImpl(ID_CLIENTE, "origen", "usuario", "id dispositivo", 123L)
        private val mockApi = mockConDefaultAnswer(CreditosDeUnaPersonaAPI::class.java)
        private val mockProveedorImagenesProductos =
                mockConDefaultAnswer(ProveedorImagenesProductos::class.java).also {
                    doReturn(Maybe.empty<ProveedorImagenesProductos.ImagenProducto>())
                        .`when`(it)
                        .darImagen(anyLong(), anyBoolean())
                }

        private val mockCalculadorPuedeAgregarseSegunUnicidad =
                mockConDefaultAnswer(CalculadorPuedeAgregarseSegunUnicidad::class.java).also {
                    doReturn(false).`when`(it).algunoEsUnico(cualquiera())
                }

        private val mockIcono = mockConDefaultAnswer(IconoPrueba::class.java)
        @Suppress("UNCHECKED_CAST")
        private val mockProveedorIconosCategoriasFiltrado =
                mockConDefaultAnswer(ProveedorIconosCategoriasFiltrado::class.java).also {
                    doReturn(mockIcono).`when`(it).darIcono(cualquiera())
                } as ProveedorIconosCategoriasFiltrado<IconoPrueba>

        private val mockRepositorioImpuestos =
                mockConDefaultAnswer(RepositorioImpuestos::class.java).also {
                    doReturn(sequenceOf<Impuesto>()).`when`(it).listar(anyLong())
                }
        private val mockRepositorioCategoriasSkus =
                mockConDefaultAnswer(RepositorioCategoriasSkus::class.java).also {
                    doReturn(sequenceOf<CategoriaSku>()).`when`(it).listar(anyLong())
                }
        private val mockRepositorioPaquetes =
                mockConDefaultAnswer(RepositorioPaquetes::class.java).also {
                    doReturn(sequenceOf<Paquete>()).`when`(it).listar(anyLong())
                }
        private val mockRepositorioFondos =
                mockConDefaultAnswer(RepositorioFondos::class.java).also {
                    doReturn(sequenceOf<Fondo<*>>()).`when`(it).listar(anyLong())
                }
        private val mockRepositorioLibrosSegunReglasCompleto =
                mockConDefaultAnswer(RepositorioLibrosSegunReglasCompleto::class.java).also {
                    doReturn(sequenceOf<LibroSegunReglasCompleto<*>>()).`when`(it).listar(anyLong())
                }

        @Suppress("UNCHECKED_CAST")
        private val mockProcesoSeleccionCreditos =
                mockConDefaultAnswer(ProcesoSeleccionCreditosUI::class.java) as ProcesoSeleccionCreditosUI<IconoPrueba>

        private val repositorios =
                ProcesoCompraYSeleccionCreditos.Repositorios(
                        mockRepositorioImpuestos,
                        mockRepositorioCategoriasSkus,
                        mockRepositorioPaquetes,
                        mockRepositorioFondos,
                        mockRepositorioLibrosSegunReglasCompleto
                                                            )

        private val personasConGrupos =
                List(3) { i ->
                    val mockPersona = mockConDefaultAnswer(Persona::class.java).also {
                        doReturn(i.toLong()).`when`(it).id
                        doReturn("CC $i").`when`(it).documentoCompleto
                        doReturn("Persona $i").`when`(it).toString()
                    }
                    PersonaConGrupoCliente(mockPersona, null)
                }

        private val fechaDePrueba = LocalDate.of(2099, 7, 29)
        private val fechaHoraDePrueba = fechaDePrueba.atStartOfDay(ZONA_HORARIA_POR_DEFECTO)


        private val entidadNegocioVacia =
                CreditosDeUnaPersona(ID_CLIENTE, 1001001, emptyList(), emptyList())

        @Nested
        inner class LlamarBackend
        {
            private val modelo =
                    ProcesoCompraYSeleccionCreditos(
                            mockConfiguracionDeSesion,
                            repositorios,
                            mockApi,
                            mockProveedorImagenesProductos,
                            mockProveedorIconosCategoriasFiltrado,
                            personasConGrupos,
                            mockProcesoSeleccionCreditos,
                            Schedulers.trampoline()
                                                   )

            @Test
            fun se_usa_siempre_la_fecha_con_la_zona_horaria_por_defecto()
            {
                val mockAgrupacion =
                        mockConDefaultAnswer(AgrupacionPersonasCarritosDeCreditosUI::class.java).also {
                            doNothing().`when`(it).actualizarItems(cualquiera())
                        }

                doReturn(mockAgrupacion).`when`(mockProcesoSeleccionCreditos).agrupacionCarritoDeCreditos

                doReturn(RespuestaIndividual.Exitosa(entidadNegocioVacia)).`when`(mockApi).consultar(cualquiera())

                modelo.consultarComprasEnFecha(fechaDePrueba)

                for (id in personasConGrupos.indices)
                {
                    verify(mockApi).consultar(CreditosDeUnaPersonaAPI.ParametrosBuscarRecursoCreditosDeUnaPersona(id.toLong(), fechaHoraDePrueba))
                }
            }
        }

        @Nested
        inner class EnEstadoEsperando
        {
            private val eventoItemsRecibidos = PublishSubject.create<ListaFiltrableUI<PersonaConCarrito>>()

            init
            {
                val mockAgrupacion =
                        mockConDefaultAnswer(AgrupacionPersonasCarritosDeCreditosUI::class.java).also {
                            doAnswer { eventoItemsRecibidos.onNext(it.getArgument(0)) }
                                .`when`(it)
                                .actualizarItems(cualquiera())
                        }

                doReturn(mockAgrupacion).`when`(mockProcesoSeleccionCreditos).agrupacionCarritoDeCreditos
            }


            private fun simularRespuesta(generarRespuesta: (Int) -> RespuestaIndividual<CreditosDeUnaPersona>)
            {
                for (id in personasConGrupos.indices)
                {
                    doReturn(generarRespuesta(id))
                        .`when`(mockApi)
                        .consultar(eqParaKotlin(CreditosDeUnaPersonaAPI.ParametrosBuscarRecursoCreditosDeUnaPersona(id.toLong(), fechaHoraDePrueba)))
                }
            }

            fun verificarQuePuedeReConsultar(modelo: ProcesoCompraYSeleccionCreditosUI<*>, listaEsperada: ListaFiltrableUI<PersonaConCarrito>)
            {
                val observableDePrueba = eventoItemsRecibidos.test()

                modelo.consultarComprasEnFecha(fechaDePrueba)
                observableDePrueba.assertValuesOnly(listaEsperada)

                modelo.consultarComprasEnFecha(fechaDePrueba)
                observableDePrueba.assertValueCount(2)
                observableDePrueba.verificarUltimoValorEmitido(listaEsperada)

                verify(mockApi, times(2))
                    .consultar(eqParaKotlin(CreditosDeUnaPersonaAPI.ParametrosBuscarRecursoCreditosDeUnaPersona(0, fechaHoraDePrueba)))
            }


            @Nested
            inner class YRecibirRespuestaRedExitosa
            {
                private val mocksCreditosFondosDePersonas =
                        personasConGrupos.indices.map {
                            val nPersona = it.toLong()
                            (0..1).flatMap {
                                val nCompra = it.toLong()
                                (0..1).map { idCredito ->
                                    CreditoFondo(
                                            ID_CLIENTE,
                                            idCredito.toLong(),
                                            Decimal.UNO,
                                            Decimal.CERO,
                                            Decimal.CERO,
                                            null,
                                            null,
                                            false,
                                            "no importa",
                                            "no importa",
                                            nPersona,
                                            nPersona * 10L + nCompra,
                                            "código externo fondo $nPersona-$nCompra",
                                            1,
                                            "no importa",
                                            null,
                                            null
                                                )
                                }
                            }
                        }

                private val mocksCreditosPaquetesDePersonas =
                        personasConGrupos.indices.map {
                            val nPersona = it.toLong()
                            (0..1).flatMap {
                                val nCompra = it.toLong()
                                (0..1).map {
                                    val creditoFondoAsociado =
                                            CreditoFondo(
                                                    ID_CLIENTE,
                                                    1L,
                                                    Decimal.UNO,
                                                    Decimal.CERO,
                                                    Decimal.CERO,
                                                    null,
                                                    null,
                                                    false,
                                                    "no importa",
                                                    "no importa",
                                                    nPersona,
                                                    nPersona * 10L + nCompra,
                                                    "código externo fondo $nPersona-$nCompra",
                                                    1,
                                                    "no importa",
                                                    null,
                                                    null
                                                        )

                                    CreditoPaquete(
                                            nPersona * 10L + nCompra,
                                            "código externo paquete $nPersona-$nCompra",
                                            listOf(creditoFondoAsociado)
                                                  )
                                }
                            }
                        }

                private val modelo: ProcesoCompraYSeleccionCreditos<*>

                init
                {
                    // Mockear fondos porque se consultan al instanciar el proceso
                    val fondos =
                            mocksCreditosFondosDePersonas
                                .flatMap { it.map { it.idFondoComprado } }
                                .asSequence()
                                .distinct()
                                .map { idFondoComprado ->
                                    mockConDefaultAnswer(Fondo::class.java).also {
                                        doReturn(idFondoComprado).`when`(it).id
                                        doReturn("Fondo $idFondoComprado").`when`(it).nombre
                                        doReturn(false).`when`(it).debeAparecerSoloUnaVez
                                    }
                                }.asSequence()

                    doReturn(fondos).`when`(mockRepositorioFondos).listar(ID_CLIENTE)


                    // Mockear paquetes porque se consultan al instanciar el proceso
                    val paquetes =
                            mocksCreditosPaquetesDePersonas
                                .flatMap { it.map { it.idPaquete } }
                                .asSequence()
                                .distinct()
                                .map { idPaqueteComprado ->
                                    mockConDefaultAnswer(Paquete::class.java).also {
                                        doReturn(idPaqueteComprado).`when`(it).id
                                        doReturn("Paquete $idPaqueteComprado").`when`(it).nombre
                                    }
                                }.asSequence()

                    doReturn(paquetes).`when`(mockRepositorioPaquetes).listar(ID_CLIENTE)

                    modelo =
                            ProcesoCompraYSeleccionCreditos(
                                    mockConfiguracionDeSesion,
                                    repositorios,
                                    mockApi,
                                    mockProveedorImagenesProductos,
                                    mockProveedorIconosCategoriasFiltrado,
                                    personasConGrupos,
                                    mockProcesoSeleccionCreditos,
                                    Schedulers.trampoline()
                                                           )
                }


                private fun generarListaEsperada(): ListaFiltrableUIConSujetos<PersonaConCarrito>
                {
                    val personasConCarritos =
                            personasConGrupos.mapIndexed { i, personaConGrupo ->
                                PersonaConCarrito(
                                        personaConGrupo.persona,
                                        personaConGrupo.posibleGrupoCliente,
                                        CarritoDeCreditos(
                                                Decimal.CERO,
                                                mocksCreditosFondosDePersonas[i].map {
                                                    CreditoFondoConNombre("Fondo ${it.idFondoComprado}", it)
                                                },
                                                mocksCreditosPaquetesDePersonas[i].map {
                                                    CreditoPaqueteConNombre("Paquete ${it.idPaquete}", 1, it)
                                                },
                                                mockCalculadorPuedeAgregarseSegunUnicidad
                                                         )
                                                 )
                            }

                    return ListaFiltrableUIConSujetos(personasConCarritos)
                }

                @Suppress("UNCHECKED_CAST")
                @BeforeEach
                fun simularRespuesta()
                {
                    simularRespuesta {
                        val creditosDeUnaPersona =
                                CreditosDeUnaPersona(
                                        ID_CLIENTE,
                                        it.toLong(),
                                        mocksCreditosFondosDePersonas[it],
                                        mocksCreditosPaquetesDePersonas[it]
                                                    )

                        RespuestaIndividual.Exitosa(creditosDeUnaPersona)
                    }
                }


                @Test
                fun las_personas_con_carritos_creadas_son_correctas()
                {
                    val listaEsperada = generarListaEsperada()
                    val observableDePrueba = eventoItemsRecibidos.test()


                    modelo.consultarComprasEnFecha(fechaDePrueba)


                    observableDePrueba.assertValuesOnly(listaEsperada)
                }

                @Test
                fun los_estados_emitidos_fueron_esperando_consultando_y_esperando()
                {
                    val observadorEstadoConsulta = modelo.estadoConsulta.test()

                    modelo.consultarComprasEnFecha(fechaDePrueba)

                    observadorEstadoConsulta.assertValuesOnly(
                            ProcesoCompraYSeleccionCreditosUI.EstadoConsulta.ESPERANDO,
                            ProcesoCompraYSeleccionCreditosUI.EstadoConsulta.CONSULTANDO,
                            ProcesoCompraYSeleccionCreditosUI.EstadoConsulta.ESPERANDO
                                                             )
                }

                @Test
                fun el_mensaje_de_error_es_vacio()
                {
                    val observadorMensajeErrorConsulta = modelo.mensajeErrorConsulta.test()

                    modelo.consultarComprasEnFecha(fechaDePrueba)

                    observadorMensajeErrorConsulta.assertValuesOnly(Opcional.Vacio())
                }

                @Test
                fun puede_volver_a_consultar()
                {
                    verificarQuePuedeReConsultar(modelo, generarListaEsperada())
                }
            }

            @Nested
            inner class YRecibirRespuestaRedErrorTimeout
            {
                private val modelo: ProcesoCompraYSeleccionCreditosUI<*> =
                        ProcesoCompraYSeleccionCreditos(
                                mockConfiguracionDeSesion,
                                repositorios,
                                mockApi,
                                mockProveedorImagenesProductos,
                                mockProveedorIconosCategoriasFiltrado,
                                personasConGrupos,
                                mockProcesoSeleccionCreditos,
                                Schedulers.trampoline()
                                                       )


                @BeforeEach
                fun simularRespuesta()
                {
                    simularRespuesta { RespuestaIndividual.Error.Timeout() }
                }

                @Test
                fun se_emite_una_lista_vacia_de_personas_con_carritos()
                {
                    val listaEsperada = ListaFiltrableUIConSujetos(listOf<PersonaConCarrito>())
                    val observableDePrueba = eventoItemsRecibidos.test()


                    modelo.consultarComprasEnFecha(fechaDePrueba)


                    observableDePrueba.verificarUltimoValorEmitido(listaEsperada)
                }

                @Test
                fun los_estados_emitidos_fueron_esperando_consultando_y_esperando()
                {
                    val observadorEstadoConsulta = modelo.estadoConsulta.test()

                    modelo.consultarComprasEnFecha(fechaDePrueba)

                    observadorEstadoConsulta.assertValuesOnly(
                            ProcesoCompraYSeleccionCreditosUI.EstadoConsulta.ESPERANDO,
                            ProcesoCompraYSeleccionCreditosUI.EstadoConsulta.CONSULTANDO,
                            ProcesoCompraYSeleccionCreditosUI.EstadoConsulta.ESPERANDO
                                                             )
                }

                @Test
                fun el_mensaje_de_error_informa_que_hubo_timeout_e_indica_la_primera_persona_que_lo_ocasiono()
                {
                    val mensajeEsperado = "Tiempo de espera al servidor agotado. No se pudieron consultar compras de la persona con documento CC 0"
                    val observadorMensajeErrorConsulta = modelo.mensajeErrorConsulta.test()

                    modelo.consultarComprasEnFecha(fechaDePrueba)

                    observadorMensajeErrorConsulta.verificarUltimoValorEmitido(Opcional.De(mensajeEsperado))
                }

                @Test
                fun puede_volver_a_consultar()
                {
                    verificarQuePuedeReConsultar(modelo, ListaFiltrableUIConSujetos(listOf()))
                }
            }

            @Nested
            inner class YRecibirRespuestaRedErrorRed
            {
                private val modelo: ProcesoCompraYSeleccionCreditosUI<*> =
                        ProcesoCompraYSeleccionCreditos(
                                mockConfiguracionDeSesion,
                                repositorios,
                                mockApi,
                                mockProveedorImagenesProductos,
                                mockProveedorIconosCategoriasFiltrado,
                                personasConGrupos,
                                mockProcesoSeleccionCreditos,
                                Schedulers.trampoline()
                                                       )


                @BeforeEach
                fun simularRespuesta()
                {
                    simularRespuesta { RespuestaIndividual.Error.Red(IOException("La excepción")) }
                }

                @Test
                fun se_emite_una_lista_vacia_de_personas_con_carritos()
                {
                    val listaEsperada = ListaFiltrableUIConSujetos(listOf<PersonaConCarrito>())
                    val observableDePrueba = eventoItemsRecibidos.test()


                    modelo.consultarComprasEnFecha(fechaDePrueba)


                    observableDePrueba.verificarUltimoValorEmitido(listaEsperada)
                }

                @Test
                fun los_estados_emitidos_fueron_esperando_consultando_y_esperando()
                {
                    val observadorEstadoConsulta = modelo.estadoConsulta.test()

                    modelo.consultarComprasEnFecha(fechaDePrueba)

                    observadorEstadoConsulta.assertValuesOnly(
                            ProcesoCompraYSeleccionCreditosUI.EstadoConsulta.ESPERANDO,
                            ProcesoCompraYSeleccionCreditosUI.EstadoConsulta.CONSULTANDO,
                            ProcesoCompraYSeleccionCreditosUI.EstadoConsulta.ESPERANDO
                                                             )
                }

                @Test
                fun el_mensaje_de_error_informa_que_hubo_timeout_e_indica_la_primera_persona_que_lo_ocasiono()
                {
                    val mensajeEsperado = "Hubo un error en la conexión y no fue posible contactar al servidor"
                    val observadorMensajeErrorConsulta = modelo.mensajeErrorConsulta.test()

                    modelo.consultarComprasEnFecha(fechaDePrueba)

                    observadorMensajeErrorConsulta.verificarUltimoValorEmitido(Opcional.De(mensajeEsperado))
                }

                @Test
                fun puede_volver_a_consultar()
                {
                    verificarQuePuedeReConsultar(modelo, ListaFiltrableUIConSujetos(listOf()))
                }
            }

            @Nested
            inner class YRecibirRespuestaRedErrorBack
            {
                private val modelo: ProcesoCompraYSeleccionCreditosUI<*> =
                        ProcesoCompraYSeleccionCreditos(
                                mockConfiguracionDeSesion,
                                repositorios,
                                mockApi,
                                mockProveedorImagenesProductos,
                                mockProveedorIconosCategoriasFiltrado,
                                personasConGrupos,
                                mockProcesoSeleccionCreditos,
                                Schedulers.trampoline()
                                                       )


                @BeforeEach
                fun simularRespuestaYAgregarErrorEsperado()
                {
                    val errorDePeticion = ErrorDePeticion(1, "no importa")
                    simularRespuesta { RespuestaIndividual.Error.Back(400, errorDePeticion) }

                    erroresEsperados.add(CompositeException(ProblemaBackend("Se produjo un error de red: $errorDePeticion")))
                }

                @Test
                fun se_emite_una_lista_vacia_de_personas_con_carritos()
                {
                    val listaEsperada = ListaFiltrableUIConSujetos(listOf<PersonaConCarrito>())
                    val observableDePrueba = eventoItemsRecibidos.test()


                    modelo.consultarComprasEnFecha(fechaDePrueba)


                    observableDePrueba.verificarUltimoValorEmitido(listaEsperada)
                }

                @Test
                fun los_estados_emitidos_fueron_esperando_consultando_y_esperando()
                {
                    val observadorEstadoConsulta = modelo.estadoConsulta.test()

                    modelo.consultarComprasEnFecha(fechaDePrueba)

                    observadorEstadoConsulta.assertValuesOnly(
                            ProcesoCompraYSeleccionCreditosUI.EstadoConsulta.ESPERANDO,
                            ProcesoCompraYSeleccionCreditosUI.EstadoConsulta.CONSULTANDO,
                            ProcesoCompraYSeleccionCreditosUI.EstadoConsulta.ESPERANDO
                                                             )
                }

                @Test
                fun el_mensaje_de_error_informa_que_hubo_timeout_e_indica_la_primera_persona_que_lo_ocasiono()
                {
                    val mensajeEsperado = "La petición realizada es errónea y no pudo ser procesada"
                    val observadorMensajeErrorConsulta = modelo.mensajeErrorConsulta.test()

                    modelo.consultarComprasEnFecha(fechaDePrueba)

                    observadorMensajeErrorConsulta.verificarUltimoValorEmitido(Opcional.De(mensajeEsperado))
                }

                @Test
                fun puede_volver_a_consultar()
                {
                    verificarQuePuedeReConsultar(modelo, ListaFiltrableUIConSujetos(listOf()))
                }
            }

            @Nested
            inner class YElApiArrojaExcepcionNoControlada
            {
                private val modelo: ProcesoCompraYSeleccionCreditosUI<*> =
                        ProcesoCompraYSeleccionCreditos(
                                mockConfiguracionDeSesion,
                                repositorios,
                                mockApi,
                                mockProveedorImagenesProductos,
                                mockProveedorIconosCategoriasFiltrado,
                                personasConGrupos,
                                mockProcesoSeleccionCreditos,
                                Schedulers.trampoline()
                                                       )

                private val excepcionGenerada = IllegalStateException()


                @BeforeEach
                fun simularExcepcion()
                {
                    for (id in personasConGrupos.indices)
                    {
                        doThrow(excepcionGenerada)
                            .`when`(mockApi)
                            .consultar(eqParaKotlin(CreditosDeUnaPersonaAPI.ParametrosBuscarRecursoCreditosDeUnaPersona(id.toLong(), fechaHoraDePrueba)))
                    }
                }

                @Test
                fun no_se_recibe_nada()
                {
                    val observableDePrueba = eventoItemsRecibidos.test()
                    erroresEsperados.add(excepcionGenerada)

                    modelo.consultarComprasEnFecha(fechaDePrueba)

                    observableDePrueba.assertEmpty()
                }
            }
        }

        @Nested
        inner class EnEstadoConsultando
        {
            private val modelo: ProcesoCompraYSeleccionCreditosUI<*> =
                    ProcesoCompraYSeleccionCreditos(
                            mockConfiguracionDeSesion,
                            repositorios,
                            mockApi,
                            mockProveedorImagenesProductos,
                            mockProveedorIconosCategoriasFiltrado,
                            personasConGrupos.take(1),
                            mockProcesoSeleccionCreditos
                                                   )

            init
            {
                val mockAgrupacion =
                        mockConDefaultAnswer(AgrupacionPersonasCarritosDeCreditosUI::class.java).also {
                            doNothing().`when`(it).actualizarItems(cualquiera())
                        }

                doReturn(mockAgrupacion).`when`(mockProcesoSeleccionCreditos).agrupacionCarritoDeCreditos

                doReturn(RespuestaIndividual.Error.Timeout<List<Compra>>()).`when`(mockApi).consultar(cualquiera())
            }

            @Test
            fun no_permite_consultar_mientras_que_no_haya_respondido_el_backend()
            {
                val tiempoPorPeticion = 150L
                val numeroDePeticiones = 3

                doAnswer { Thread.sleep(tiempoPorPeticion) }
                    .`when`(mockApi)
                    .consultar(cualquiera())

                for (i in 0 until numeroDePeticiones)
                {
                    modelo.consultarComprasEnFecha(fechaDePrueba)
                }

                Thread.sleep(tiempoPorPeticion * numeroDePeticiones)
                verify(mockApi).consultar(cualquiera())

                modelo.consultarComprasEnFecha(fechaDePrueba)

                Thread.sleep(tiempoPorPeticion)
                verify(mockApi, times(2)).consultar(cualquiera())
            }
        }
    }
}