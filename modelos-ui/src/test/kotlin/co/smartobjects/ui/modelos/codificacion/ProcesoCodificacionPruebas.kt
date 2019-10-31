package co.smartobjects.ui.modelos.codificacion

import co.smartobjects.entidades.operativas.compras.CreditoFondoConNombre
import co.smartobjects.entidades.operativas.compras.CreditoPaqueteConNombre
import co.smartobjects.entidades.operativas.reservas.SesionDeManilla
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.personas.PersonaConGrupoCliente
import co.smartobjects.nfc.ProveedorOperacionesNFC
import co.smartobjects.nfc.ResultadoNFC
import co.smartobjects.red.clientes.base.RespuestaVacia
import co.smartobjects.red.clientes.operativas.reservas.sesionesdemanilla.SesionDeManillaAPI
import co.smartobjects.ui.modelos.*
import co.smartobjects.ui.modelos.pagos.ProcesoPagarUI
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import io.reactivex.rxkotlin.cast
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.threeten.bp.LocalDate
import kotlin.test.assertEquals

internal class ProcesoCodificacionPruebas : PruebasModelosRxBase()
{
    companion object
    {
        private const val ID_CLIENTE = 1L
    }

    private val sesionesDeManillaYCreditosACodificar =
            List(5) {
                val idPersona = it.toLong() + 1

                val creditosACodificar =
                        ProcesoPagarUI.CreditosACodificarPorPersona(
                                PersonaConGrupoCliente(
                                        Persona(
                                                1, idPersona, "Persona $idPersona", Persona.TipoDocumento.CC, "$idPersona$idPersona", Persona.Genero.MASCULINO,
                                                LocalDate.now(ZONA_HORARIA_POR_DEFECTO), Persona.Categoria.A, Persona.Afiliacion.COTIZANTE, false,
                                                null,"empresa","0",Persona.Tipo.NO_AFILIADO
                                               ),
                                        null
                                                      ),
                                listOf(
                                        CreditoFondoConNombre("Crédito Fondo Pagado 10", crearCreditoFondo(10, idPersona, true)),
                                        CreditoFondoConNombre("Crédito Fondo Pagado 20", crearCreditoFondo(20, idPersona, true))
                                      ),
                                listOf(
                                        CreditoPaqueteConNombre("Crédito Paquete Pagado 30", 1, crearCreditoPaquete(30, idPersona, true)),
                                        CreditoPaqueteConNombre("Crédito Paquete Pagado 40", 1, crearCreditoPaquete(40, idPersona, true))
                                      )
                                                                   )

                val idSesionDeManilla = it.toLong() + 1
                val idsCreditosFondo = creditosACodificar.creditosFondoTotales.asSequence().map { it.id!! }.toSet()

                ProcesoCodificacionUI.SesionDeManillaYCreditosACodificar(
                        SesionDeManilla(ID_CLIENTE, idSesionDeManilla, idPersona, null, null, null, idsCreditosFondo),
                        creditosACodificar
                                                                        )
            }

    private val mockProveedorOperacionesTagNFC = mockConDefaultAnswer(ProveedorOperacionesNFC::class.java).also {
        doReturn(true).`when`(it).permitirLecturaNFC
        doNothing().`when`(it).permitirLecturaNFC = anyBoolean()
        doReturn(PublishProcessor.create<ResultadoNFC>()).`when`(it).resultadosNFCLeidos
        doReturn(BehaviorProcessor.createDefault<Boolean>(true)).`when`(it).listoParaLectura
    }

    private val apiSesionDeManilla = mockConDefaultAnswer(SesionDeManillaAPI::class.java).also {
        doReturn(RespuestaVacia.Exitosa).`when`(it).actualizarCampos(anyLong(), cualquiera())
    }


    @Nested
    inner class Instanciacion
    {
        @Test
        fun los_items_a_codificar_se_inicializan_correctamente()
        {
            val schedulerQueDeberiaSerComun = TestScheduler()
            val observableResultadosNFC =
                    mockProveedorOperacionesTagNFC
                        .resultadosNFCLeidos
                        .filter { it is ResultadoNFC.Exitoso }
                        .cast<ResultadoNFC.Exitoso>()

            val itemsACodificarEsperados =
                    sesionesDeManillaYCreditosACodificar.map {
                        ItemACodificar(
                                it.sesionDeManilla,
                                it.creditosACodificar,
                                observableResultadosNFC,
                                apiSesionDeManilla,
                                schedulerQueDeberiaSerComun
                                      )
                    }

            val modelo = ProcesoCodificacion(
                    sesionesDeManillaYCreditosACodificar,
                    mockProveedorOperacionesTagNFC,
                    apiSesionDeManilla,
                    schedulerQueDeberiaSerComun
                                            )

            assertEquals(itemsACodificarEsperados, modelo.itemsACodificar)
        }

    }

    @Nested
    inner class NumeroDeSesionesActivas
    {
        @Test
        fun al_instanciar_el_numero_de_sesiones_activas_es_0()
        {
            val modelo = ProcesoCodificacion(
                    sesionesDeManillaYCreditosACodificar,
                    mockProveedorOperacionesTagNFC,
                    apiSesionDeManilla
                                            )

            val observadorEstaCodificandoAlgunItem = modelo.numeroDeSesionesActivas.test()

            observadorEstaCodificandoAlgunItem.assertValue(0)
        }

        @Test
        fun a_medida_que_se_activan_items_el_contador_incrementa()
        {
            val mocksItemsACodificar =
                    List(sesionesDeManillaYCreditosACodificar.size) {
                        mockConDefaultAnswer(ItemACodificarUI::class.java).also {
                            val valorInicial =
                                    ItemACodificarUI.Estado.EtapaSesionManilla.SinCodificar

                            doReturn(BehaviorSubject.createDefault<ItemACodificarUI.Estado>(valorInicial))
                                .`when`(it)
                                .estado
                        }
                    }

            val modelo =
                    ProcesoCodificacion(
                            sesionesDeManillaYCreditosACodificar,
                            mockProveedorOperacionesTagNFC,
                            apiSesionDeManilla,
                            itemsACodificarDePruebas = mocksItemsACodificar
                                       )

            val observadorDePrueba = modelo.numeroDeSesionesActivas.test()

            var valorEsperado = 0
            observadorDePrueba.assertValue(valorEsperado)

            for (i in mocksItemsACodificar.indices)
            {
                (mocksItemsACodificar[i].estado as BehaviorSubject<ItemACodificarUI.Estado>)
                    .onNext(ItemACodificarUI.Estado.EtapaSesionManilla.Activada)

                valorEsperado++
                observadorDePrueba.verificarUltimoValorEmitido(valorEsperado)
            }
        }
    }

    @Nested
    inner class EstanTodosActivados
    {
        @Test
        fun al_instanciar_no_emite_nada()
        {
            val modelo = ProcesoCodificacion(
                    sesionesDeManillaYCreditosACodificar,
                    mockProveedorOperacionesTagNFC,
                    apiSesionDeManilla
                                            )

            val observadorEstaCodificandoAlgunItem = modelo.seActivaronTodasLasSesiones.test()

            observadorEstaCodificandoAlgunItem.assertEmpty()
        }

        @Test
        fun si_existe_un_item_no_activo_no_emite_nada()
        {
            val indiceItemSinActivar = 0
            val mocksItemsACodificar =
                    List(sesionesDeManillaYCreditosACodificar.size) {
                        val indice = it
                        mockConDefaultAnswer(ItemACodificarUI::class.java).also {
                            val valorInicial =
                                    if (indice == indiceItemSinActivar)
                                    {
                                        ItemACodificarUI.Estado.EtapaSesionManilla.SinCodificar
                                    }
                                    else
                                    {
                                        ItemACodificarUI.Estado.EtapaSesionManilla.Activada
                                    }

                            doReturn(BehaviorSubject.createDefault<ItemACodificarUI.Estado>(valorInicial))
                                .`when`(it)
                                .estado
                        }
                    }

            val modelo =
                    ProcesoCodificacion(
                            sesionesDeManillaYCreditosACodificar,
                            mockProveedorOperacionesTagNFC,
                            apiSesionDeManilla,
                            itemsACodificarDePruebas = mocksItemsACodificar
                                       )

            val observadorEstaCodificandoAlgunItem = modelo.seActivaronTodasLasSesiones.test()

            observadorEstaCodificandoAlgunItem.assertEmpty()
        }

        @Test
        fun si_todos_los_items_estan_activados_emite_unit()
        {
            val indiceItemSinActivar = 0
            val mocksItemsACodificar =
                    List(sesionesDeManillaYCreditosACodificar.size) {
                        val indice = it
                        mockConDefaultAnswer(ItemACodificarUI::class.java).also {
                            val valorInicial =
                                    if (indice == indiceItemSinActivar)
                                    {
                                        ItemACodificarUI.Estado.EtapaSesionManilla.SinCodificar
                                    }
                                    else
                                    {
                                        ItemACodificarUI.Estado.EtapaSesionManilla.Activada
                                    }

                            doReturn(BehaviorSubject.createDefault<ItemACodificarUI.Estado>(valorInicial))
                                .`when`(it)
                                .estado
                        }
                    }

            val modelo =
                    ProcesoCodificacion(
                            sesionesDeManillaYCreditosACodificar,
                            mockProveedorOperacionesTagNFC,
                            apiSesionDeManilla,
                            itemsACodificarDePruebas = mocksItemsACodificar
                                       )

            val observadorEstaCodificandoAlgunItem = modelo.seActivaronTodasLasSesiones.test()

            observadorEstaCodificandoAlgunItem.assertEmpty()

            (mocksItemsACodificar[indiceItemSinActivar].estado as BehaviorSubject<ItemACodificarUI.Estado>)
                .onNext(ItemACodificarUI.Estado.EtapaSesionManilla.Activada)

            observadorEstaCodificandoAlgunItem.assertResult(Unit)
        }
    }

    @Nested
    inner class AlIniciarCodificacionDesdePrimeroSinCodificar
    {
        @Test
        fun se_permiten_lecturas_nfc()
        {
            val modelo = ProcesoCodificacion(
                    sesionesDeManillaYCreditosACodificar,
                    mockProveedorOperacionesTagNFC,
                    apiSesionDeManilla
                                            )

            modelo.iniciarCodificacionDesdePrimeroSinCodificar()

            verify(mockProveedorOperacionesTagNFC).permitirLecturaNFC = true
        }

        @Nested
        inner class SiNoHayEnEstadoSinCodificar
        {
            private val mocksItemsACodificar =
                    List(sesionesDeManillaYCreditosACodificar.size) {
                        mockConDefaultAnswer(ItemACodificarUI::class.java).also {
                            val valorInicial = ItemACodificarUI.Estado.EtapaSesionManilla.Activada

                            doReturn(BehaviorSubject.createDefault<ItemACodificarUI.Estado>(valorInicial))
                                .`when`(it)
                                .estado

                            doNothing().`when`(it).habilitarParaCodificacion()
                        }
                    }

            @Test
            fun no_se_habilita_ninguno_para_codificacion()
            {
                val modelo = ProcesoCodificacion(
                        sesionesDeManillaYCreditosACodificar,
                        mockProveedorOperacionesTagNFC,
                        apiSesionDeManilla,
                        itemsACodificarDePruebas = mocksItemsACodificar
                                                )

                modelo.iniciarCodificacionDesdePrimeroSinCodificar()

                for (item in mocksItemsACodificar)
                {
                    verify(item, never()).habilitarParaCodificacion()
                }
            }
        }

        @Nested
        inner class SiHayEnEstadoSinCodificar
        {
            private val indicesItemsSinCodificar = arrayOf(1, 3)
            private val mocksItemsACodificar =
                    List(sesionesDeManillaYCreditosACodificar.size) {
                        val indice = it
                        mockConDefaultAnswer(ItemACodificarUI::class.java).also {
                            val valorInicial =
                                    if (indice in indicesItemsSinCodificar)
                                    {
                                        ItemACodificarUI.Estado.EtapaSesionManilla.SinCodificar
                                    }
                                    else
                                    {
                                        ItemACodificarUI.Estado.EtapaSesionManilla.Activada
                                    }

                            doReturn(BehaviorSubject.createDefault<ItemACodificarUI.Estado>(valorInicial))
                                .`when`(it)
                                .estado

                            doNothing().`when`(it).habilitarParaCodificacion()
                        }
                    }

            private val modelo = ProcesoCodificacion(
                    sesionesDeManillaYCreditosACodificar,
                    mockProveedorOperacionesTagNFC,
                    apiSesionDeManilla,
                    Schedulers.trampoline(),
                    mocksItemsACodificar
                                                    )

            init
            {
                assert(indicesItemsSinCodificar.size == 2)
                indicesItemsSinCodificar.forEach {
                    assert(it < sesionesDeManillaYCreditosACodificar.size)
                }
            }

            @Test
            fun se_habilita_para_codificacion_el_primero_que_este_sin_codificar()
            {
                modelo.iniciarCodificacionDesdePrimeroSinCodificar()

                for (i in mocksItemsACodificar.indices)
                {
                    val invocaciones = if (i == indicesItemsSinCodificar.min()) times(1) else never()
                    verify(mocksItemsACodificar[i], invocaciones).habilitarParaCodificacion()
                }
            }

            @Test
            fun si_le_lector_no_esta_listo_no_habilita_ningun_item()
            {
                val eventoListoParaLectura = mockProveedorOperacionesTagNFC.listoParaLectura as BehaviorProcessor<Boolean>
                eventoListoParaLectura.onNext(false)

                modelo.iniciarCodificacionDesdePrimeroSinCodificar()

                for (mockItemACodificar in mocksItemsACodificar)
                {
                    verify(mockItemACodificar, never()).habilitarParaCodificacion()
                }
            }

            @Test
            fun cuando_el_item_habilitado_para_codificacion_actual_emite_estado_codificado_habilita_el_siguiente_si_el_lector_esta_listo()
            {
                modelo.iniciarCodificacionDesdePrimeroSinCodificar()

                val indicePrimeroACodificar = indicesItemsSinCodificar.min()!!
                for (i in mocksItemsACodificar.indices)
                {
                    val invocaciones = if (i == indicePrimeroACodificar) times(1) else never()
                    verify(mocksItemsACodificar[i], invocaciones).habilitarParaCodificacion()
                }

                val eventosEstadoItemActual = mocksItemsACodificar[indicePrimeroACodificar].estado as Subject<ItemACodificarUI.Estado>

                val eventoListoParaLectura = mockProveedorOperacionesTagNFC.listoParaLectura as BehaviorProcessor<Boolean>
                eventoListoParaLectura.onNext(false)
                eventosEstadoItemActual.onNext(ItemACodificarUI.Estado.EtapaSesionManilla.Codificada(byteArrayOf()))

                val indiceSiguienteACodificar = indicesItemsSinCodificar.max()!!

                verify(mocksItemsACodificar[indiceSiguienteACodificar], never()).habilitarParaCodificacion()

                eventoListoParaLectura.onNext(true)

                for (i in mocksItemsACodificar.indices)
                {
                    val invocaciones = if (i == indicePrimeroACodificar || i == indiceSiguienteACodificar) times(1) else never()
                    verify(mocksItemsACodificar[i], invocaciones).habilitarParaCodificacion()
                }
            }
        }
    }

    @Nested
    inner class CuandoHayUnErrorNFC
    {
        private val mocksItemsACodificar =
                List(sesionesDeManillaYCreditosACodificar.size) {
                    mockConDefaultAnswer(ItemACodificarUI::class.java).also {
                        val valorInicial =
                                ItemACodificarUI.Estado.EtapaSesionManilla.SinCodificar

                        doReturn(BehaviorSubject.createDefault<ItemACodificarUI.Estado>(valorInicial))
                            .`when`(it)
                            .estado

                        doNothing().`when`(it).habilitarParaCodificacion()
                    }
                }

        private val modelo = ProcesoCodificacion(
                sesionesDeManillaYCreditosACodificar,
                mockProveedorOperacionesTagNFC,
                apiSesionDeManilla,
                itemsACodificarDePruebas = mocksItemsACodificar
                                                )

        private val observadorMensajesDeError = modelo.mensajesDeError.test()
        private val eventosNFC = mockProveedorOperacionesTagNFC.resultadosNFCLeidos as FlowableProcessor<ResultadoNFC>

        @Nested
        inner class TagNoSoportado
        {
            @Test
            fun se_emite_un_mensaje_de_error_con_el_nombre_del_tag()
            {
                val nombreDelTag = "nombre del tag esperado"

                eventosNFC.onNext(ResultadoNFC.Error.TagNoSoportado(nombreDelTag))

                observadorMensajesDeError.assertValue("El tag '$nombreDelTag' no se encuentra soportado en el momento")
            }

            @Test
            fun el_item_en_codificacion_lo_rehabilita_para_codificacion()
            {
                val eventosDeEstado = mocksItemsACodificar[1].estado as BehaviorSubject<ItemACodificarUI.Estado>

                eventosDeEstado.onNext(ItemACodificarUI.Estado.Codificando)

                eventosNFC.onNext(ResultadoNFC.Error.TagNoSoportado("no importa"))

                verify(mocksItemsACodificar[1]).habilitarParaCodificacion()
            }
        }

        @Nested
        inner class ConectandoseAlTag
        {
            @Test
            fun se_emite_un_mensaje_de_error_diciendo_que_no_fue_posible_conectarse_al_tag()
            {
                eventosNFC.onNext(ResultadoNFC.Error.ConectandoseAlTag)

                observadorMensajesDeError.assertValue("No fue posible conectarse con el tag")
            }

            @Test
            fun el_item_en_codificacion_lo_rehabilita_para_codificacion()
            {
                val eventosDeEstado = mocksItemsACodificar[1].estado as BehaviorSubject<ItemACodificarUI.Estado>

                eventosDeEstado.onNext(ItemACodificarUI.Estado.Codificando)

                eventosNFC.onNext(ResultadoNFC.Error.ConectandoseAlTag)

                verify(mocksItemsACodificar[1]).habilitarParaCodificacion()
            }
        }
    }

    @Test
    fun al_finalizar_el_prceso_se_deshabilita_la_lectura_nfc()
    {
        val modelo = ProcesoCodificacion(
                sesionesDeManillaYCreditosACodificar,
                mockProveedorOperacionesTagNFC,
                apiSesionDeManilla
                                        )

        modelo.finalizarProceso()

        verify(mockProveedorOperacionesTagNFC).permitirLecturaNFC = false
    }
}