package co.smartobjects.ui.modelos.menuprincipal

import co.smartobjects.entidades.ubicaciones.Ubicacion
import co.smartobjects.persistencia.ubicaciones.RepositorioUbicaciones
import co.smartobjects.red.clientes.base.RespuestaVacia
import co.smartobjects.sincronizadordecontenido.GestorDescargaDeDatos
import co.smartobjects.ui.modelos.*
import co.smartobjects.utilidades.Opcional
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doReturn


internal class MenuPrincipalPruebas : PruebasModelosRxBase()
{
    companion object
    {
        private const val ID_CLIENTE = 1L
    }

    private val sujetoContextoDeSesion = BehaviorSubject.createDefault(Opcional.Vacio<ContextoDeSesion>())
    private val mockRepositorioUbicaciones = mockConDefaultAnswer(RepositorioUbicaciones::class.java)
    private val mockGestorDescargaDeDatos = mockConDefaultAnswer(GestorDescargaDeDatos::class.java).also {
        doReturn(PublishSubject.create<Boolean>()).`when`(it).estaDescargando
    }

    @Nested
    inner class PuedeIrARegistrarPersonas
    {
        @Test
        fun si_existen_ubicaciones_emite_true()
        {
            doReturn(sequenceOf(mockConDefaultAnswer(Ubicacion::class.java)))
                .`when`(mockRepositorioUbicaciones)
                .listar(eqParaKotlin(ID_CLIENTE))

            val modelo =
                    MenuPrincipal(
                            ID_CLIENTE,
                            sujetoContextoDeSesion,
                            mockRepositorioUbicaciones,
                            mockGestorDescargaDeDatos,
                            Schedulers.trampoline()
                                 )

            val observadorDePrueba = modelo.puedeIrARegistrarPersonas.test()

            observadorDePrueba.verificarUltimoValorEmitido(true)
        }

        @Nested
        inner class SiNoExistenUbicaciones
        {
            private lateinit var modelo: MenuPrincipalUI

            @BeforeEach
            fun mockearRepositorio()
            {
                doReturn(sequenceOf<Ubicacion>()).`when`(mockRepositorioUbicaciones).listar(eqParaKotlin(ID_CLIENTE))
            }


            @Test
            fun y_hay_error_relanza_la_excepcion_y_no_cambia_el_valor()
            {
                val excepcionEsperada = IllegalStateException("Un error cualquiera")

                doReturn(Maybe.error<List<RespuestaVacia.Exitosa>>(excepcionEsperada))
                    .`when`(mockGestorDescargaDeDatos)
                    .descargarYAlmacenarDatosIndependientesDeLaUbicacion()

                erroresEsperados.add(excepcionEsperada)

                val schedulerPrueba = TestScheduler()
                modelo =
                        MenuPrincipal(
                                ID_CLIENTE,
                                sujetoContextoDeSesion,
                                mockRepositorioUbicaciones,
                                mockGestorDescargaDeDatos,
                                schedulerPrueba
                                     )

                val observadorDePrueba = modelo.puedeIrARegistrarPersonas.test()

                schedulerPrueba.triggerActions()

                observadorDePrueba.assertValuesOnly(false)
            }

            @Test
            fun y_ya_estaba_descargando_no_emite_nada()
            {
                doReturn(Maybe.empty<List<RespuestaVacia.Exitosa>>())
                    .`when`(mockGestorDescargaDeDatos)
                    .descargarYAlmacenarDatosIndependientesDeLaUbicacion()

                val schedulerPrueba = TestScheduler()
                modelo =
                        MenuPrincipal(
                                ID_CLIENTE,
                                sujetoContextoDeSesion,
                                mockRepositorioUbicaciones,
                                mockGestorDescargaDeDatos,
                                schedulerPrueba
                                     )

                val observadorDePrueba = modelo.puedeIrARegistrarPersonas.test()

                schedulerPrueba.triggerActions()

                observadorDePrueba.assertValuesOnly(false)
            }

            @Test
            fun y_descargo_todo_exitosamente_emite_true()
            {
                doReturn(Maybe.just(listOf(RespuestaVacia.Exitosa)))
                    .`when`(mockGestorDescargaDeDatos)
                    .descargarYAlmacenarDatosIndependientesDeLaUbicacion()

                val schedulerPrueba = TestScheduler()
                modelo =
                        MenuPrincipal(
                                ID_CLIENTE,
                                sujetoContextoDeSesion,
                                mockRepositorioUbicaciones,
                                mockGestorDescargaDeDatos,
                                schedulerPrueba
                                     )

                val observadorDePrueba = modelo.puedeIrARegistrarPersonas.test()

                schedulerPrueba.triggerActions()

                observadorDePrueba.assertValuesOnly(false, true)
            }

            @Test
            fun y_alguna_descarga_fallo_con_timeout_no_cambia_el_valor()
            {
                doReturn(Maybe.just(listOf(RespuestaVacia.Exitosa, RespuestaVacia.Error.Timeout)))
                    .`when`(mockGestorDescargaDeDatos)
                    .descargarYAlmacenarDatosIndependientesDeLaUbicacion()

                val schedulerPrueba = TestScheduler()
                modelo =
                        MenuPrincipal(
                                ID_CLIENTE,
                                sujetoContextoDeSesion,
                                mockRepositorioUbicaciones,
                                mockGestorDescargaDeDatos,
                                schedulerPrueba
                                     )

                val observadorDePrueba = modelo.puedeIrARegistrarPersonas.test()

                schedulerPrueba.triggerActions()

                observadorDePrueba.assertValuesOnly(false)
            }

            @Test
            fun y_alguna_descarga_fallo_con_error_de_red_no_cambia_el_valor()
            {
                doReturn(Maybe.just(listOf(RespuestaVacia.Exitosa, mockConDefaultAnswer(RespuestaVacia.Error.Red::class.java))))
                    .`when`(mockGestorDescargaDeDatos)
                    .descargarYAlmacenarDatosIndependientesDeLaUbicacion()

                val schedulerPrueba = TestScheduler()
                modelo =
                        MenuPrincipal(
                                ID_CLIENTE,
                                sujetoContextoDeSesion,
                                mockRepositorioUbicaciones,
                                mockGestorDescargaDeDatos,
                                schedulerPrueba
                                     )

                val observadorDePrueba = modelo.puedeIrARegistrarPersonas.test()

                schedulerPrueba.triggerActions()

                observadorDePrueba.assertValuesOnly(false)
            }

            @Test
            fun y_alguna_descarga_fallo_con_error_de_back_no_cambia_el_valor()
            {
                doReturn(Maybe.just(listOf(RespuestaVacia.Exitosa, mockConDefaultAnswer(RespuestaVacia.Error.Back::class.java))))
                    .`when`(mockGestorDescargaDeDatos)
                    .descargarYAlmacenarDatosIndependientesDeLaUbicacion()

                val schedulerPrueba = TestScheduler()
                modelo =
                        MenuPrincipal(
                                ID_CLIENTE,
                                sujetoContextoDeSesion,
                                mockRepositorioUbicaciones,
                                mockGestorDescargaDeDatos,
                                schedulerPrueba
                                     )

                val observadorDePrueba = modelo.puedeIrARegistrarPersonas.test()

                schedulerPrueba.triggerActions()

                observadorDePrueba.assertValuesOnly(false)
            }
        }
    }

    @Nested
    inner class PuedeIrAComprarCreditos
    {
        @Test
        fun si_existen_ubicaciones_emite_true()
        {
            doReturn(sequenceOf(mockConDefaultAnswer(Ubicacion::class.java)))
                .`when`(mockRepositorioUbicaciones)
                .listar(eqParaKotlin(ID_CLIENTE))

            val modelo =
                    MenuPrincipal(
                            ID_CLIENTE,
                            sujetoContextoDeSesion,
                            mockRepositorioUbicaciones,
                            mockGestorDescargaDeDatos,
                            Schedulers.trampoline()
                                 )

            val observadorDePrueba = modelo.puedeIrAComprarCreditos.test()

            observadorDePrueba.verificarUltimoValorEmitido(true)
        }

        @Nested
        inner class SiNoExistenUbicaciones
        {
            private lateinit var modelo: MenuPrincipalUI

            @BeforeEach
            fun mockearRepositorio()
            {
                doReturn(sequenceOf<Ubicacion>()).`when`(mockRepositorioUbicaciones).listar(eqParaKotlin(ID_CLIENTE))
            }


            @Test
            fun y_hay_error_relanza_la_excepcion_y_no_cambia_el_valor()
            {
                val excepcionEsperada = IllegalStateException("Un error cualquiera")

                doReturn(Maybe.error<List<RespuestaVacia.Exitosa>>(excepcionEsperada))
                    .`when`(mockGestorDescargaDeDatos)
                    .descargarYAlmacenarDatosIndependientesDeLaUbicacion()

                erroresEsperados.add(excepcionEsperada)

                val schedulerPrueba = TestScheduler()
                modelo =
                        MenuPrincipal(
                                ID_CLIENTE,
                                sujetoContextoDeSesion,
                                mockRepositorioUbicaciones,
                                mockGestorDescargaDeDatos,
                                schedulerPrueba
                                     )

                val observadorDePrueba = modelo.puedeIrAComprarCreditos.test()

                schedulerPrueba.triggerActions()

                observadorDePrueba.assertValuesOnly(false)
            }

            @Test
            fun y_ya_estaba_descargando_no_emite_nada()
            {
                doReturn(Maybe.empty<List<RespuestaVacia.Exitosa>>())
                    .`when`(mockGestorDescargaDeDatos)
                    .descargarYAlmacenarDatosIndependientesDeLaUbicacion()

                val schedulerPrueba = TestScheduler()
                modelo =
                        MenuPrincipal(
                                ID_CLIENTE,
                                sujetoContextoDeSesion,
                                mockRepositorioUbicaciones,
                                mockGestorDescargaDeDatos,
                                schedulerPrueba
                                     )

                val observadorDePrueba = modelo.puedeIrAComprarCreditos.test()

                schedulerPrueba.triggerActions()

                observadorDePrueba.assertValuesOnly(false)
            }

            @Test
            fun y_descargo_todo_exitosamente_emite_true()
            {
                doReturn(Maybe.just(listOf(RespuestaVacia.Exitosa)))
                    .`when`(mockGestorDescargaDeDatos)
                    .descargarYAlmacenarDatosIndependientesDeLaUbicacion()

                val schedulerPrueba = TestScheduler()
                modelo =
                        MenuPrincipal(
                                ID_CLIENTE,
                                sujetoContextoDeSesion,
                                mockRepositorioUbicaciones,
                                mockGestorDescargaDeDatos,
                                schedulerPrueba
                                     )

                val observadorDePrueba = modelo.puedeIrAComprarCreditos.test()

                schedulerPrueba.triggerActions()

                observadorDePrueba.assertValuesOnly(false, true)
            }

            @Test
            fun y_alguna_descarga_fallo_con_timeout_no_cambia_el_valor()
            {
                doReturn(Maybe.just(listOf(RespuestaVacia.Exitosa, RespuestaVacia.Error.Timeout)))
                    .`when`(mockGestorDescargaDeDatos)
                    .descargarYAlmacenarDatosIndependientesDeLaUbicacion()

                val schedulerPrueba = TestScheduler()
                modelo =
                        MenuPrincipal(
                                ID_CLIENTE,
                                sujetoContextoDeSesion,
                                mockRepositorioUbicaciones,
                                mockGestorDescargaDeDatos,
                                schedulerPrueba
                                     )

                val observadorDePrueba = modelo.puedeIrAComprarCreditos.test()

                schedulerPrueba.triggerActions()

                observadorDePrueba.assertValuesOnly(false)
            }

            @Test
            fun y_alguna_descarga_fallo_con_error_de_red_no_cambia_el_valor()
            {
                doReturn(Maybe.just(listOf(RespuestaVacia.Exitosa, mockConDefaultAnswer(RespuestaVacia.Error.Red::class.java))))
                    .`when`(mockGestorDescargaDeDatos)
                    .descargarYAlmacenarDatosIndependientesDeLaUbicacion()

                val schedulerPrueba = TestScheduler()
                modelo =
                        MenuPrincipal(
                                ID_CLIENTE,
                                sujetoContextoDeSesion,
                                mockRepositorioUbicaciones,
                                mockGestorDescargaDeDatos,
                                schedulerPrueba
                                     )

                val observadorDePrueba = modelo.puedeIrAComprarCreditos.test()

                schedulerPrueba.triggerActions()

                observadorDePrueba.assertValuesOnly(false)
            }

            @Test
            fun y_alguna_descarga_fallo_con_error_de_back_no_cambia_el_valor()
            {
                doReturn(Maybe.just(listOf(RespuestaVacia.Exitosa, mockConDefaultAnswer(RespuestaVacia.Error.Back::class.java))))
                    .`when`(mockGestorDescargaDeDatos)
                    .descargarYAlmacenarDatosIndependientesDeLaUbicacion()

                val schedulerPrueba = TestScheduler()
                modelo =
                        MenuPrincipal(
                                ID_CLIENTE,
                                sujetoContextoDeSesion,
                                mockRepositorioUbicaciones,
                                mockGestorDescargaDeDatos,
                                schedulerPrueba
                                     )

                val observadorDePrueba = modelo.puedeIrAComprarCreditos.test()

                schedulerPrueba.triggerActions()

                observadorDePrueba.assertValuesOnly(false)
            }
        }
    }

    @Nested
    inner class DialogoEsperaPorSincronizacionVisible
    {
        @Test
        fun el_valor_depende_de_el_estado_de_descarga_deL_gestor_de_descargas()
        {
            doReturn(sequenceOf<Ubicacion>())
                .`when`(mockRepositorioUbicaciones)
                .listar(eqParaKotlin(ID_CLIENTE))

            doReturn(Maybe.just(listOf(RespuestaVacia.Exitosa)))
                .`when`(mockGestorDescargaDeDatos)
                .descargarYAlmacenarDatosIndependientesDeLaUbicacion()

            doReturn(Observable.just(false, true, false))
                .`when`(mockGestorDescargaDeDatos)
                .estaDescargando

            val schedulerPrueba = TestScheduler()
            val modelo =
                    MenuPrincipal(
                            ID_CLIENTE,
                            sujetoContextoDeSesion,
                            mockRepositorioUbicaciones,
                            mockGestorDescargaDeDatos,
                            schedulerPrueba
                                 )

            val observadorDePrueba = modelo.dialogoEsperaPorSincronizacionVisible.test()

            schedulerPrueba.triggerActions()

            observadorDePrueba.assertResult(false, true, false)
        }
    }

    @Nested
    inner class DebeSolicitarUbicacion
    {
        @Nested
        inner class SiExistenUbicaciones
        {
            @Test
            fun y_no_hay_ubicacion_definida_emite_true()
            {
                doReturn(sequenceOf(mockConDefaultAnswer(Ubicacion::class.java)))
                    .`when`(mockRepositorioUbicaciones)
                    .listar(eqParaKotlin(ID_CLIENTE))

                val schedulerPrueba = TestScheduler()
                val modelo =
                        MenuPrincipal(
                                ID_CLIENTE,
                                sujetoContextoDeSesion,
                                mockRepositorioUbicaciones,
                                mockGestorDescargaDeDatos,
                                schedulerPrueba
                                     )

                val observadorDePrueba = modelo.debeSolicitarUbicacion.test()

                schedulerPrueba.triggerActions()

                observadorDePrueba.assertResult(true)
            }

            @Test
            fun y_hay_ubicacion_definida_emite_false()
            {
                sujetoContextoDeSesion.onNext(Opcional.De(mockConDefaultAnswer(ContextoDeSesion::class.java)))

                doReturn(sequenceOf(mockConDefaultAnswer(Ubicacion::class.java)))
                    .`when`(mockRepositorioUbicaciones)
                    .listar(eqParaKotlin(ID_CLIENTE))

                val schedulerPrueba = TestScheduler()
                val modelo =
                        MenuPrincipal(
                                ID_CLIENTE,
                                sujetoContextoDeSesion,
                                mockRepositorioUbicaciones,
                                mockGestorDescargaDeDatos,
                                schedulerPrueba
                                     )

                val observadorDePrueba = modelo.debeSolicitarUbicacion.test()

                schedulerPrueba.triggerActions()

                observadorDePrueba.assertResult(false)
            }
        }

        @Nested
        inner class SiNoExistenUbicaciones
        {
            private lateinit var modelo: MenuPrincipalUI

            @BeforeEach
            fun mockearRepositorio()
            {
                doReturn(sequenceOf<Ubicacion>()).`when`(mockRepositorioUbicaciones).listar(eqParaKotlin(ID_CLIENTE))
            }

            @Test
            fun y_hay_error_relanza_la_excepcion_y_no_cambia_el_valor()
            {
                val excepcionEsperada = IllegalStateException("Un error cualquiera")

                doReturn(Maybe.error<List<RespuestaVacia.Exitosa>>(excepcionEsperada))
                    .`when`(mockGestorDescargaDeDatos)
                    .descargarYAlmacenarDatosIndependientesDeLaUbicacion()

                erroresEsperados.add(excepcionEsperada)

                val schedulerPrueba = TestScheduler()
                modelo =
                        MenuPrincipal(
                                ID_CLIENTE,
                                sujetoContextoDeSesion,
                                mockRepositorioUbicaciones,
                                mockGestorDescargaDeDatos,
                                schedulerPrueba
                                     )

                val observadorDePrueba = modelo.debeSolicitarUbicacion.test()

                schedulerPrueba.triggerActions()

                observadorDePrueba.assertEmpty()
            }

            @Test
            fun y_ya_estaba_descargando_no_emite_nada()
            {
                doReturn(Maybe.empty<List<RespuestaVacia.Exitosa>>())
                    .`when`(mockGestorDescargaDeDatos)
                    .descargarYAlmacenarDatosIndependientesDeLaUbicacion()

                val schedulerPrueba = TestScheduler()
                modelo =
                        MenuPrincipal(
                                ID_CLIENTE,
                                sujetoContextoDeSesion,
                                mockRepositorioUbicaciones,
                                mockGestorDescargaDeDatos,
                                schedulerPrueba
                                     )

                val observadorDePrueba = modelo.debeSolicitarUbicacion.test()

                schedulerPrueba.triggerActions()

                observadorDePrueba.assertEmpty()
            }

            @Nested
            inner class YDescargaExitosamente
            {
                private val schedulerPrueba = TestScheduler()

                @BeforeEach
                fun prepararMocks()
                {
                    doReturn(Maybe.just(listOf(RespuestaVacia.Exitosa)))
                        .`when`(mockGestorDescargaDeDatos)
                        .descargarYAlmacenarDatosIndependientesDeLaUbicacion()
                }

                @Test
                fun sin_ubicacion_definida_true()
                {
                    modelo =
                            MenuPrincipal(
                                    ID_CLIENTE,
                                    sujetoContextoDeSesion,
                                    mockRepositorioUbicaciones,
                                    mockGestorDescargaDeDatos,
                                    schedulerPrueba
                                         )

                    val observadorDePrueba = modelo.debeSolicitarUbicacion.test()

                    schedulerPrueba.triggerActions()

                    observadorDePrueba.assertResult(true)
                }

                @Test
                fun con_ubicacion_definida_emite_true()
                {
                    sujetoContextoDeSesion.onNext(Opcional.De(mockConDefaultAnswer(ContextoDeSesion::class.java)))
                    modelo =
                            MenuPrincipal(
                                    ID_CLIENTE,
                                    sujetoContextoDeSesion,
                                    mockRepositorioUbicaciones,
                                    mockGestorDescargaDeDatos,
                                    schedulerPrueba
                                         )

                    val observadorDePrueba = modelo.debeSolicitarUbicacion.test()

                    schedulerPrueba.triggerActions()

                    observadorDePrueba.assertResult(true)
                }
            }

            @Nested
            inner class YDescargaConErrorTimeout
            {
                private val schedulerPrueba = TestScheduler()

                @BeforeEach
                fun prepararMocks()
                {
                    doReturn(Maybe.just(listOf(RespuestaVacia.Exitosa, RespuestaVacia.Error.Timeout)))
                        .`when`(mockGestorDescargaDeDatos)
                        .descargarYAlmacenarDatosIndependientesDeLaUbicacion()
                }

                @Test
                fun sin_ubicacion_definida_emite_false()
                {
                    modelo =
                            MenuPrincipal(
                                    ID_CLIENTE,
                                    sujetoContextoDeSesion,
                                    mockRepositorioUbicaciones,
                                    mockGestorDescargaDeDatos,
                                    schedulerPrueba
                                         )

                    val observadorDePrueba = modelo.debeSolicitarUbicacion.test()

                    schedulerPrueba.triggerActions()

                    observadorDePrueba.assertResult(false)
                }

                @Test
                fun con_ubicacion_definida_emite_false()
                {
                    sujetoContextoDeSesion.onNext(Opcional.De(mockConDefaultAnswer(ContextoDeSesion::class.java)))

                    modelo =
                            MenuPrincipal(
                                    ID_CLIENTE,
                                    sujetoContextoDeSesion,
                                    mockRepositorioUbicaciones,
                                    mockGestorDescargaDeDatos,
                                    schedulerPrueba
                                         )

                    val observadorDePrueba = modelo.debeSolicitarUbicacion.test()

                    schedulerPrueba.triggerActions()

                    observadorDePrueba.assertResult(false)
                }
            }

            @Nested
            inner class YDescargaConErrorRed
            {
                private val schedulerPrueba = TestScheduler()

                @BeforeEach
                fun prepararMocks()
                {
                    doReturn(Maybe.just(listOf(RespuestaVacia.Exitosa, mockConDefaultAnswer(RespuestaVacia.Error.Red::class.java))))
                        .`when`(mockGestorDescargaDeDatos)
                        .descargarYAlmacenarDatosIndependientesDeLaUbicacion()
                }

                @Test
                fun sin_ubicacion_definida_emite_false()
                {
                    modelo =
                            MenuPrincipal(
                                    ID_CLIENTE,
                                    sujetoContextoDeSesion,
                                    mockRepositorioUbicaciones,
                                    mockGestorDescargaDeDatos,
                                    schedulerPrueba
                                         )

                    val observadorDePrueba = modelo.debeSolicitarUbicacion.test()

                    schedulerPrueba.triggerActions()

                    observadorDePrueba.assertResult(false)
                }

                @Test
                fun con_ubicacion_definida_emite_false()
                {
                    sujetoContextoDeSesion.onNext(Opcional.De(mockConDefaultAnswer(ContextoDeSesion::class.java)))

                    modelo =
                            MenuPrincipal(
                                    ID_CLIENTE,
                                    sujetoContextoDeSesion,
                                    mockRepositorioUbicaciones,
                                    mockGestorDescargaDeDatos,
                                    schedulerPrueba
                                         )

                    val observadorDePrueba = modelo.debeSolicitarUbicacion.test()

                    schedulerPrueba.triggerActions()

                    observadorDePrueba.assertResult(false)
                }
            }

            @Nested
            inner class YDescargaConErrorBack
            {
                private val schedulerPrueba = TestScheduler()

                @BeforeEach
                fun prepararMocks()
                {
                    doReturn(Maybe.just(listOf(RespuestaVacia.Exitosa, mockConDefaultAnswer(RespuestaVacia.Error.Back::class.java))))
                        .`when`(mockGestorDescargaDeDatos)
                        .descargarYAlmacenarDatosIndependientesDeLaUbicacion()
                }

                @Test
                fun sin_ubicacion_definida_emite_false()
                {
                    modelo =
                            MenuPrincipal(
                                    ID_CLIENTE,
                                    sujetoContextoDeSesion,
                                    mockRepositorioUbicaciones,
                                    mockGestorDescargaDeDatos,
                                    schedulerPrueba
                                         )

                    val observadorDePrueba = modelo.debeSolicitarUbicacion.test()

                    schedulerPrueba.triggerActions()

                    observadorDePrueba.assertResult(false)
                }

                @Test
                fun con_ubicacion_definida_emite_false()
                {
                    sujetoContextoDeSesion.onNext(Opcional.De(mockConDefaultAnswer(ContextoDeSesion::class.java)))

                    modelo =
                            MenuPrincipal(
                                    ID_CLIENTE,
                                    sujetoContextoDeSesion,
                                    mockRepositorioUbicaciones,
                                    mockGestorDescargaDeDatos,
                                    schedulerPrueba
                                         )

                    val observadorDePrueba = modelo.debeSolicitarUbicacion.test()

                    schedulerPrueba.triggerActions()

                    observadorDePrueba.assertResult(false)
                }
            }
        }

        @Test
        fun funciona_si_se_realiza_la_peticion_en_background()
        {
            doReturn(sequenceOf(mockConDefaultAnswer(Ubicacion::class.java)))
                .`when`(mockRepositorioUbicaciones)
                .listar(eqParaKotlin(ID_CLIENTE))

            val schedulerPrueba = TestScheduler()
            val modelo =
                    MenuPrincipal(
                            ID_CLIENTE,
                            sujetoContextoDeSesion,
                            mockRepositorioUbicaciones,
                            mockGestorDescargaDeDatos,
                            schedulerPrueba
                                 )

            schedulerPrueba.triggerActions()

            val observadorDePrueba = modelo.debeSolicitarUbicacion.test()

            observadorDePrueba.assertResult(true)
        }
    }

    @Nested
    inner class ErrorDeDescarga
    {
        @Test
        fun si_existen_ubicaciones_completa()
        {
            doReturn(sequenceOf(mockConDefaultAnswer(Ubicacion::class.java)))
                .`when`(mockRepositorioUbicaciones)
                .listar(eqParaKotlin(ID_CLIENTE))

            val modelo =
                    MenuPrincipal(
                            ID_CLIENTE,
                            sujetoContextoDeSesion,
                            mockRepositorioUbicaciones,
                            mockGestorDescargaDeDatos,
                            Schedulers.trampoline()
                                 )

            val observadorDePrueba = modelo.errorDeDescarga.test()

            observadorDePrueba.assertComplete()
        }

        @Nested
        inner class SiNoExistenUbicaciones
        {
            private lateinit var modelo: MenuPrincipalUI

            @BeforeEach
            fun mockearRepositorio()
            {
                doReturn(sequenceOf<Ubicacion>()).`when`(mockRepositorioUbicaciones).listar(eqParaKotlin(ID_CLIENTE))
            }


            @Test
            fun y_hay_error_no_emite_nada_y_relanza_la_excepcion()
            {
                val excepcionEsperada = IllegalStateException("Un error cualquiera")

                doReturn(Maybe.error<List<RespuestaVacia.Exitosa>>(excepcionEsperada))
                    .`when`(mockGestorDescargaDeDatos)
                    .descargarYAlmacenarDatosIndependientesDeLaUbicacion()

                erroresEsperados.add(excepcionEsperada)

                val schedulerPrueba = TestScheduler()
                modelo =
                        MenuPrincipal(
                                ID_CLIENTE,
                                sujetoContextoDeSesion,
                                mockRepositorioUbicaciones,
                                mockGestorDescargaDeDatos,
                                schedulerPrueba
                                     )

                val observadorDePrueba = modelo.errorDeDescarga.test()

                schedulerPrueba.triggerActions()

                observadorDePrueba.assertEmpty()
            }

            @Test
            fun y_ya_estaba_descargando_no_emite_nada()
            {
                doReturn(Maybe.empty<List<RespuestaVacia.Exitosa>>())
                    .`when`(mockGestorDescargaDeDatos)
                    .descargarYAlmacenarDatosIndependientesDeLaUbicacion()

                val schedulerPrueba = TestScheduler()
                modelo =
                        MenuPrincipal(
                                ID_CLIENTE,
                                sujetoContextoDeSesion,
                                mockRepositorioUbicaciones,
                                mockGestorDescargaDeDatos,
                                schedulerPrueba
                                     )

                val observadorDePrueba = modelo.errorDeDescarga.test()

                schedulerPrueba.triggerActions()

                observadorDePrueba.assertEmpty()
            }

            @Test
            fun y_descargo_todo_exitosamente_entonces_completa()
            {
                doReturn(Maybe.just(listOf(RespuestaVacia.Exitosa)))
                    .`when`(mockGestorDescargaDeDatos)
                    .descargarYAlmacenarDatosIndependientesDeLaUbicacion()

                val schedulerPrueba = TestScheduler()
                modelo =
                        MenuPrincipal(
                                ID_CLIENTE,
                                sujetoContextoDeSesion,
                                mockRepositorioUbicaciones,
                                mockGestorDescargaDeDatos,
                                schedulerPrueba
                                     )

                val observadorDePrueba = modelo.errorDeDescarga.test()

                schedulerPrueba.triggerActions()

                observadorDePrueba.assertComplete()
            }

            @Test
            fun y_alguna_descarga_fallo_con_timeout_entonces_emite_respuesta_vacia_de_timeout()
            {
                doReturn(Maybe.just(listOf(RespuestaVacia.Exitosa, RespuestaVacia.Error.Timeout)))
                    .`when`(mockGestorDescargaDeDatos)
                    .descargarYAlmacenarDatosIndependientesDeLaUbicacion()

                val schedulerPrueba = TestScheduler()
                modelo =
                        MenuPrincipal(
                                ID_CLIENTE,
                                sujetoContextoDeSesion,
                                mockRepositorioUbicaciones,
                                mockGestorDescargaDeDatos,
                                schedulerPrueba
                                     )

                val observadorDePrueba = modelo.errorDeDescarga.test()

                schedulerPrueba.triggerActions()

                observadorDePrueba.assertResult(RespuestaVacia.Error.Timeout)
            }

            @Test
            fun y_alguna_descarga_fallo_con_error_de_red_entonces_emite_respuesta_vacia_del_primer_error_de_red()
            {
                val resultados =
                        listOf(
                                RespuestaVacia.Exitosa,
                                mockConDefaultAnswer(RespuestaVacia.Error.Red::class.java),
                                mockConDefaultAnswer(RespuestaVacia.Error.Red::class.java)
                              )

                doReturn(Maybe.just(resultados))
                    .`when`(mockGestorDescargaDeDatos)
                    .descargarYAlmacenarDatosIndependientesDeLaUbicacion()

                val schedulerPrueba = TestScheduler()
                modelo =
                        MenuPrincipal(
                                ID_CLIENTE,
                                sujetoContextoDeSesion,
                                mockRepositorioUbicaciones,
                                mockGestorDescargaDeDatos,
                                schedulerPrueba
                                     )

                val observadorDePrueba = modelo.errorDeDescarga.test()

                schedulerPrueba.triggerActions()

                observadorDePrueba.assertResult(resultados[1])
            }

            @Test
            fun y_alguna_descarga_fallo_con_error_de_back_entonces_emite_respuesta_vacia_del_primer_error_de_back()
            {
                val resultados =
                        listOf(
                                RespuestaVacia.Exitosa,
                                mockConDefaultAnswer(RespuestaVacia.Error.Back::class.java),
                                mockConDefaultAnswer(RespuestaVacia.Error.Back::class.java)
                              )

                doReturn(Maybe.just(resultados))
                    .`when`(mockGestorDescargaDeDatos)
                    .descargarYAlmacenarDatosIndependientesDeLaUbicacion()

                val schedulerPrueba = TestScheduler()
                modelo =
                        MenuPrincipal(
                                ID_CLIENTE,
                                sujetoContextoDeSesion,
                                mockRepositorioUbicaciones,
                                mockGestorDescargaDeDatos,
                                schedulerPrueba
                                     )

                val observadorDePrueba = modelo.errorDeDescarga.test()

                schedulerPrueba.triggerActions()

                observadorDePrueba.assertResult(resultados[1])
            }
        }
    }

    @Nested
    inner class PantallaANavegar
    {
        private lateinit var modelo: MenuPrincipalUI
        private lateinit var observadorDePrueba: TestObserver<MenuPrincipalUI.PantallaSeleccionada>

        @BeforeEach
        fun mockearRepositorio()
        {
            doReturn(sequenceOf(mockConDefaultAnswer(Ubicacion::class.java)))
                .`when`(mockRepositorioUbicaciones)
                .listar(eqParaKotlin(ID_CLIENTE))

            modelo =
                    MenuPrincipal(
                            ID_CLIENTE,
                            sujetoContextoDeSesion,
                            mockRepositorioUbicaciones,
                            mockGestorDescargaDeDatos,
                            Schedulers.trampoline()
                                 )

            observadorDePrueba = modelo.pantallaANavegar.test()
        }

        @Test
        fun al_ir_a_registrar_personas_emite_REGISTRAR_PERSONAS_y_queda_completado()
        {
            modelo.irARegistrarPersonas()

            observadorDePrueba.assertResult(MenuPrincipalUI.PantallaSeleccionada.REGISTRAR_PERSONAS)
        }

        @Test
        fun al_ir_a_comprar_creditos_emite_COMPRAR_CREDITOS_y_queda_completado()
        {
            modelo.irAComprarCreditos()

            observadorDePrueba.assertResult(MenuPrincipalUI.PantallaSeleccionada.COMPRAR_CREDITOS)
        }
    }
}