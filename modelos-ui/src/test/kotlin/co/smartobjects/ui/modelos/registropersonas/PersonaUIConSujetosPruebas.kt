package co.smartobjects.ui.modelos.registropersonas

import co.smartobjects.entidades.excepciones.EntidadConCampoFueraDeRango
import co.smartobjects.entidades.excepciones.EntidadConCampoVacio
import co.smartobjects.entidades.personas.DocumentoCompleto
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.ui.modelos.PruebasModelosRxBase
import co.smartobjects.ui.modelos.fechas.FechaUI
import co.smartobjects.ui.modelos.mockConDefaultAnswer
import co.smartobjects.ui.modelos.verificarUltimoValorEmitido
import co.smartobjects.utilidades.Opcional
import io.reactivex.Notification
import io.reactivex.subjects.BehaviorSubject
import org.junit.jupiter.api.*
import org.mockito.Mockito
import org.threeten.bp.LocalDate

@DisplayName("PersonaUIConSujetos")
internal class PersonaUIConSujetosPruebas : PruebasModelosRxBase()
{
    companion object
    {
        private const val ID_CLIENTE = 10L
    }


    @Nested
    inner class SinMockearFechaNacimiento
    {
        private val modelo: PersonaUI = PersonaUIConSujetos(ID_CLIENTE)

        @Nested
        inner class IdCliente
        {
            @Test
            fun queda_con_valor_correcto()
            {
                Assertions.assertEquals(ID_CLIENTE, modelo.idCliente)
            }
        }

        @Nested
        inner class EnObservableDeFecha
        {
            private val testFecha = modelo.fechaNacimiento.fecha.test()

            @Test
            fun emite_valor_correcto_al_cambiar_a_fecha_pasada()
            {
                val fechaPrueba = LocalDate.now().minusDays(2)
                modelo.fechaNacimiento.asignarFecha(fechaPrueba)
                testFecha.assertValue(Notification.createOnNext(fechaPrueba))
            }

            @Test
            fun emite_error_correcto_al_cambiar_a_fecha_futura()
            {
                modelo.fechaNacimiento.asignarFecha(LocalDate.now().plusDays(2))
                testFecha.assertValue { it.isOnError }
                testFecha.assertValue { it.error!! is EntidadConCampoFueraDeRango }
            }
        }
        @Nested
        inner class EnObservableDeEsFechaValida
        {
            private val testEsFechaValida = modelo.fechaNacimiento.esFechaValida.test()

            @Test
            fun emite_true_al_cambiar_a_fecha_pasada()
            {
                modelo.fechaNacimiento.asignarFecha(LocalDate.now().minusDays(2))
                testEsFechaValida.assertValue(true)
            }

            @Test
            fun emite_false_al_cambiar_a_fecha_futura()
            {
                modelo.fechaNacimiento.asignarFecha(LocalDate.now().plusDays(2))
                testEsFechaValida.assertValue(false)
            }
        }
    }

    @Nested
    inner class MockeandoFechaNacimiento
    {
        // Para estas pruebas se va a suponer que FechaUI funciona correctamente
        private val mockFecha = mockConDefaultAnswer(FechaUI::class.java)

        private fun mockearFinalizarProceso()
        {
            Mockito.doNothing()
                .`when`(mockFecha)
                .finalizarProceso()
        }

        //Nota: No se mockean observables para asegurar que no se usen directamente
        private val sujetoEsFechaValida = BehaviorSubject.create<Boolean>()

        @BeforeEach
        fun mockearEsFechaValida()
        {
            Mockito.doReturn(sujetoEsFechaValida)
                .`when`(mockFecha)
                .esFechaValida
        }

        private val modelo: PersonaUI by lazy { PersonaUIConSujetos(ID_CLIENTE, mockFecha) }

        @Nested
        inner class IdCliente
        {
            @Test
            fun queda_con_valor_correcto()
            {
                Assertions.assertEquals(ID_CLIENTE, modelo.idCliente)
            }
        }

        @Nested
        inner class Id
        {
            private val testId by lazy { modelo.id.test() }

            @BeforeEach
            fun forzar_lazy()
            {
                testId.valueCount()
            }

            @Test
            fun emite_nothing_al_inicializar()
            {
                testId.assertValue(Opcional.Vacio())
                testId.assertValueCount(1)
            }
        }

        @Nested
        inner class CambiarNombreCompleto
        {
            private val testNombreCompleto by lazy {  modelo.nombreCompleto.test() }

            @BeforeEach
            fun forzar_lazy()
            {
                testNombreCompleto.valueCount()
            }

            @Test
            fun no_emite_valor_al_inicializar()
            {
                testNombreCompleto.assertValueCount(0)
            }

            @Test
            fun con_valor_valido_emite_valor_cambiado_una_vez_por_cada_llamado()
            {
                val numeroEventos = 10
                (0 until numeroEventos).forEach {
                    val nombrePruebas = "Nombre $it"
                    modelo.cambiarNombreCompleto(nombrePruebas)
                    testNombreCompleto.assertValueAt(it, Notification.createOnNext(nombrePruebas))
                }
                testNombreCompleto.assertValueCount(numeroEventos)
            }

            @Test
            fun con_espacios_emite_valor_con_trim()
            {
                modelo.cambiarNombreCompleto("        Nombre       ")
                testNombreCompleto.assertValue(Notification.createOnNext("Nombre"))
                testNombreCompleto.assertValueCount(1)
            }

            @Test
            fun con_valor_vacio_emite_error_EntidadConCampoVacio()
            {
                modelo.cambiarNombreCompleto("")
                testNombreCompleto.assertValue { it.isOnError }
                testNombreCompleto.assertValue { it.error!! is EntidadConCampoVacio }
                testNombreCompleto.assertValueCount(1)
            }

            @Test
            fun con_valor_con_espacios_y_tabs_emite_error_EntidadConCampoVacio()
            {
                modelo.cambiarNombreCompleto("             ")
                testNombreCompleto.assertValue { it.isOnError }
                testNombreCompleto.assertValue { it.error!! is EntidadConCampoVacio }
                testNombreCompleto.assertValueCount(1)
            }

            @Test
            fun a_error_y_luego_a_valor_valido_emite_ambos_valores()
            {
                modelo.cambiarNombreCompleto("")
                modelo.cambiarNombreCompleto("Nombre")
                testNombreCompleto.assertValueAt(0) { it.isOnError }
                testNombreCompleto.assertValueAt(0) { it.error!! is EntidadConCampoVacio }
                testNombreCompleto.assertValueAt(1, Notification.createOnNext("Nombre"))
                testNombreCompleto.assertValueCount(2)
            }

            @Test
            fun a_valor_valido_y_luego_a_error_emite_ambos_valores()
            {
                modelo.cambiarNombreCompleto("Nombre")
                modelo.cambiarNombreCompleto("")
                testNombreCompleto.assertValueAt(0, Notification.createOnNext("Nombre"))
                testNombreCompleto.assertValueAt(1) { it.isOnError }
                testNombreCompleto.assertValueAt(1) { it.error!! is EntidadConCampoVacio }
                testNombreCompleto.assertValueCount(2)
            }

            @Test
            fun no_emite_mas_eventos_despues_de_llamar_finalizar_proceso()
            {
                mockearFinalizarProceso()
                modelo.finalizarProceso()
                modelo.cambiarNombreCompleto("Nombre")
                modelo.cambiarNombreCompleto("")
                testNombreCompleto.assertValueCount(0)
            }
        }

        @Nested
        inner class CambiarTipoDocumento
        {
            private val testTipoDocumento by lazy { modelo.tipoDocumento.test() }

            @BeforeEach
            fun forzar_lazy()
            {
                testTipoDocumento.valueCount()
            }

            @Test
            fun no_emite_valor_al_inicializar()
            {
                testTipoDocumento.assertValueCount(0)
            }

            @Test
            fun con_valor_valido_emite_valor_cambiado_una_vez_por_cada_llamado()
            {
                Persona.TipoDocumento.values().forEachIndexed { index, tipoDocumento ->
                    modelo.cambiarTipoDocumento(tipoDocumento)
                    testTipoDocumento.assertValueAt(index, tipoDocumento)
                }
                testTipoDocumento.assertValueCount(Persona.TipoDocumento.values().size)
            }

            @Test
            fun no_emite_mas_eventos_despues_de_llamar_finalizar_proceso()
            {
                mockearFinalizarProceso()
                modelo.finalizarProceso()
                modelo.cambiarTipoDocumento(Persona.TipoDocumento.CC)
                modelo.cambiarTipoDocumento(Persona.TipoDocumento.CD)
                testTipoDocumento.assertValueCount(0)
            }
        }

        @Nested
        inner class CambiarNumeroDocumento
        {
            private val testNumeroDocumento by lazy { modelo.numeroDocumento.test() }

            @BeforeEach
            fun forzar_lazy()
            {
                testNumeroDocumento.valueCount()
            }

            @Test
            fun no_emite_valor_al_inicializar()
            {
                testNumeroDocumento.assertValueCount(0)
            }

            @Test
            fun con_valor_valido_emite_valor_cambiado_una_vez_por_cada_llamado()
            {
                val numeroEventos = 10
                (0 until numeroEventos).forEach {
                    val documentoPruebas = "123$it"
                    modelo.cambiarNumeroDocumento(documentoPruebas)
                    testNumeroDocumento.assertValueAt(it, Notification.createOnNext(documentoPruebas))
                }
                testNumeroDocumento.assertValueCount(numeroEventos)
            }

            @Test
            fun con_espacios_emite_valor_con_trim()
            {
                modelo.cambiarNumeroDocumento("        12345       ")
                testNumeroDocumento.assertValue(Notification.createOnNext("12345"))
                testNumeroDocumento.assertValueCount(1)
            }

            @Test
            fun con_valor_vacio_emite_error_EntidadConCampoVacio()
            {
                modelo.cambiarNumeroDocumento("")
                testNumeroDocumento.assertValue { it.isOnError }
                testNumeroDocumento.assertValue { it.error!! is EntidadConCampoVacio }
                testNumeroDocumento.assertValueCount(1)
            }

            @Test
            fun con_valor_con_espacios_y_tabs_emite_error_EntidadConCampoVacio()
            {
                modelo.cambiarNumeroDocumento("             ")
                testNumeroDocumento.assertValue { it.isOnError }
                testNumeroDocumento.assertValue { it.error!! is EntidadConCampoVacio }
                testNumeroDocumento.assertValueCount(1)
            }

            @Test
            fun a_error_y_luego_a_valor_valido_emite_ambos_valores()
            {
                modelo.cambiarNumeroDocumento("")
                modelo.cambiarNumeroDocumento("12345")
                testNumeroDocumento.assertValueAt(0) { it.isOnError }
                testNumeroDocumento.assertValueAt(0) { it.error!! is EntidadConCampoVacio }
                testNumeroDocumento.assertValueAt(1, Notification.createOnNext("12345"))
                testNumeroDocumento.assertValueCount(2)
            }

            @Test
            fun a_valor_valido_y_luego_a_error_emite_ambos_valores()
            {
                modelo.cambiarNumeroDocumento("12345")
                modelo.cambiarNumeroDocumento("")
                testNumeroDocumento.assertValueAt(0, Notification.createOnNext("12345"))
                testNumeroDocumento.assertValueAt(1) { it.isOnError }
                testNumeroDocumento.assertValueAt(1) { it.error!! is EntidadConCampoVacio }
                testNumeroDocumento.assertValueCount(2)
            }

            @Test
            fun no_emite_mas_eventos_despues_de_llamar_finalizar_proceso()
            {
                mockearFinalizarProceso()
                modelo.finalizarProceso()
                modelo.cambiarNumeroDocumento("12345")
                modelo.cambiarNumeroDocumento("")
                testNumeroDocumento.assertValueCount(0)
            }
        }

        @Nested
        inner class CambiarGenero
        {
            private val testGenero by lazy { modelo.genero.test() }

            @BeforeEach
            fun forzar_lazy()
            {
                testGenero.valueCount()
            }

            @Test
            fun no_emite_valor_al_inicializar()
            {
                testGenero.assertValueCount(0)
            }

            @Test
            fun con_valor_valido_emite_valor_cambiado_una_vez_por_cada_llamado()
            {
                Persona.Genero.values().forEachIndexed { index, genero ->
                    modelo.cambiarGenero(genero)
                    testGenero.assertValueAt(index, genero)
                }
                testGenero.assertValueCount(Persona.Genero.values().size)
            }

            @Test
            fun no_emite_mas_eventos_despues_de_llamar_finalizar_proceso()
            {
                mockearFinalizarProceso()
                modelo.finalizarProceso()
                modelo.cambiarGenero(Persona.Genero.MASCULINO)
                modelo.cambiarGenero(Persona.Genero.DESCONOCIDO)
                testGenero.assertValueCount(0)
            }
        }

        @Nested
        inner class FechaNacimiento
        {
            @Test
            fun llama_finalizar_proceso_de_fecha_al_llamar_finalizar_proceso()
            {
                mockearFinalizarProceso()
                modelo.finalizarProceso()
                Mockito.verify(mockFecha, Mockito.times(1)).finalizarProceso()
            }
        }

        @Nested
        inner class CambiarCategoria
        {
            private val testCategoria by lazy { modelo.categoria.test() }

            @BeforeEach
            fun forzar_lazy()
            {
                testCategoria.valueCount()
            }

            @Test
            fun no_emite_valor_al_inicializar()
            {
                testCategoria.assertValueCount(0)
            }

            @Test
            fun con_valor_valido_emite_valor_cambiado_una_vez_por_cada_llamado()
            {
                Persona.Categoria.values().forEachIndexed { index, categoria ->
                    modelo.cambiarCategoria(categoria)
                    testCategoria.assertValueAt(index, categoria)
                }
                testCategoria.assertValueCount(Persona.Categoria.values().size)
            }

            @Test
            fun no_emite_mas_eventos_despues_de_llamar_finalizar_proceso()
            {
                mockearFinalizarProceso()
                modelo.finalizarProceso()
                modelo.cambiarCategoria(Persona.Categoria.A)
                modelo.cambiarCategoria(Persona.Categoria.D)
                testCategoria.assertValueCount(0)
            }
        }

        @Nested
        inner class CambiarAfiliacion
        {
            private val testAfiliacion by lazy { modelo.afiliacion.test() }

            @BeforeEach
            fun forzar_lazy()
            {
                testAfiliacion.valueCount()
            }

            @Test
            fun no_emite_valor_al_inicializar()
            {
                testAfiliacion.assertValueCount(0)
            }

            @Test
            fun con_valor_valido_emite_valor_cambiado_una_vez_por_cada_llamado()
            {
                Persona.Afiliacion.values().forEachIndexed { index, afiliacion ->
                    modelo.cambiarAfiliacion(afiliacion)
                    testAfiliacion.assertValueAt(index, afiliacion)
                }
                testAfiliacion.assertValueCount(Persona.Afiliacion.values().size)
            }

            @Test
            fun no_emite_mas_eventos_despues_de_llamar_finalizar_proceso()
            {
                mockearFinalizarProceso()
                modelo.finalizarProceso()
                modelo.cambiarAfiliacion(Persona.Afiliacion.COTIZANTE)
                modelo.cambiarAfiliacion(Persona.Afiliacion.BENEFICIARIO)
                testAfiliacion.assertValueCount(0)
            }
        }

        @Nested
        inner class EsPersonaValida
        {
            private val testEsPersonaValida by lazy { modelo.esPersonaValida.test() }

            @BeforeEach
            fun forzar_lazy()
            {
                testEsPersonaValida.valueCount()
            }

            @Test
            fun no_emite_evento_al_cambiar_otros_campos_si_no_se_cambia_nombre_completo()
            {
                modelo.cambiarTipoDocumento(Persona.TipoDocumento.CC)
                modelo.cambiarNumeroDocumento("1234")
                modelo.cambiarGenero(Persona.Genero.MASCULINO)
                sujetoEsFechaValida.onNext(true)
                modelo.cambiarCategoria(Persona.Categoria.A)
                modelo.cambiarAfiliacion(Persona.Afiliacion.COTIZANTE)
                testEsPersonaValida.assertValueCount(0)
            }

            @Test
            fun no_emite_evento_al_cambiar_otros_campos_si_no_se_cambia_tipo_documento()
            {
                modelo.cambiarNombreCompleto("Nombre Valido")
                modelo.cambiarNumeroDocumento("1234")
                modelo.cambiarGenero(Persona.Genero.MASCULINO)
                sujetoEsFechaValida.onNext(true)
                modelo.cambiarCategoria(Persona.Categoria.A)
                modelo.cambiarAfiliacion(Persona.Afiliacion.COTIZANTE)
                testEsPersonaValida.assertValueCount(0)
            }

            @Test
            fun no_emite_evento_al_cambiar_otros_campos_si_no_se_cambia_numero_documento()
            {
                modelo.cambiarNombreCompleto("Nombre Valido")
                modelo.cambiarTipoDocumento(Persona.TipoDocumento.CC)
                modelo.cambiarGenero(Persona.Genero.MASCULINO)
                sujetoEsFechaValida.onNext(true)
                modelo.cambiarCategoria(Persona.Categoria.A)
                modelo.cambiarAfiliacion(Persona.Afiliacion.COTIZANTE)
                testEsPersonaValida.assertValueCount(0)
            }

            @Test
            fun no_emite_evento_al_cambiar_otros_campos_si_no_se_cambia_genero()
            {
                modelo.cambiarNombreCompleto("Nombre Valido")
                modelo.cambiarTipoDocumento(Persona.TipoDocumento.CC)
                modelo.cambiarNumeroDocumento("1234")
                sujetoEsFechaValida.onNext(true)
                modelo.cambiarCategoria(Persona.Categoria.A)
                modelo.cambiarAfiliacion(Persona.Afiliacion.COTIZANTE)
                testEsPersonaValida.assertValueCount(0)
            }

            @Test
            fun no_emite_evento_al_cambiar_otros_campos_si_fecha_no_emite_es_fecha_valida()
            {
                modelo.cambiarNombreCompleto("Nombre Valido")
                modelo.cambiarTipoDocumento(Persona.TipoDocumento.CC)
                modelo.cambiarNumeroDocumento("1234")
                modelo.cambiarGenero(Persona.Genero.MASCULINO)
                modelo.cambiarCategoria(Persona.Categoria.A)
                modelo.cambiarAfiliacion(Persona.Afiliacion.COTIZANTE)
                testEsPersonaValida.assertValueCount(0)
            }

            @Test
            fun no_emite_evento_al_cambiar_otros_campos_si_no_se_cambia_categoria()
            {
                modelo.cambiarNombreCompleto("Nombre Valido")
                modelo.cambiarTipoDocumento(Persona.TipoDocumento.CC)
                modelo.cambiarNumeroDocumento("1234")
                modelo.cambiarGenero(Persona.Genero.MASCULINO)
                sujetoEsFechaValida.onNext(true)
                modelo.cambiarAfiliacion(Persona.Afiliacion.COTIZANTE)
                testEsPersonaValida.assertValueCount(0)
            }

            @Test
            fun no_emite_evento_al_cambiar_otros_campos_si_no_se_cambia_afiliacion()
            {
                modelo.cambiarNombreCompleto("Nombre Valido")
                modelo.cambiarTipoDocumento(Persona.TipoDocumento.CC)
                modelo.cambiarNumeroDocumento("1234")
                modelo.cambiarGenero(Persona.Genero.MASCULINO)
                sujetoEsFechaValida.onNext(true)
                modelo.cambiarCategoria(Persona.Categoria.A)
                testEsPersonaValida.assertValueCount(0)
            }

            @Test
            fun emite_evento_true_al_cambiar_todos_los_campos_a_valores_validos()
            {
                modelo.cambiarNombreCompleto("Nombre Valido")
                modelo.cambiarTipoDocumento(Persona.TipoDocumento.CC)
                modelo.cambiarNumeroDocumento("1234")
                modelo.cambiarGenero(Persona.Genero.MASCULINO)
                sujetoEsFechaValida.onNext(true)
                modelo.cambiarCategoria(Persona.Categoria.A)
                modelo.cambiarAfiliacion(Persona.Afiliacion.COTIZANTE)
                testEsPersonaValida.assertValue(true)
                testEsPersonaValida.assertValueCount(1)
            }

            @Test
            fun emite_evento_false_al_cambiar_otros_campos_a_valor_valido_y_nombre_completo_a_valor_invalido()
            {
                modelo.cambiarNombreCompleto("")
                modelo.cambiarTipoDocumento(Persona.TipoDocumento.CC)
                modelo.cambiarNumeroDocumento("1234")
                modelo.cambiarGenero(Persona.Genero.MASCULINO)
                sujetoEsFechaValida.onNext(true)
                modelo.cambiarCategoria(Persona.Categoria.A)
                modelo.cambiarAfiliacion(Persona.Afiliacion.COTIZANTE)
                testEsPersonaValida.assertValue(false)
                testEsPersonaValida.assertValueCount(1)
            }

            @Test
            fun emite_evento_false_al_cambiar_otros_campos_a_valor_valido_y_numero_documento_a_valor_invalido()
            {
                modelo.cambiarNombreCompleto("Nombre Valido")
                modelo.cambiarTipoDocumento(Persona.TipoDocumento.CC)
                modelo.cambiarNumeroDocumento("")
                modelo.cambiarGenero(Persona.Genero.MASCULINO)
                sujetoEsFechaValida.onNext(true)
                modelo.cambiarCategoria(Persona.Categoria.A)
                modelo.cambiarAfiliacion(Persona.Afiliacion.COTIZANTE)
                testEsPersonaValida.assertValue(false)
                testEsPersonaValida.assertValueCount(1)
            }

            @Test
            fun emite_evento_false_al_cambiar_otros_campos_a_valor_valido_y_fecha_emite_es_fecha_invalida()
            {
                modelo.cambiarNombreCompleto("Nombre Valido")
                modelo.cambiarTipoDocumento(Persona.TipoDocumento.CC)
                modelo.cambiarNumeroDocumento("1234")
                modelo.cambiarGenero(Persona.Genero.MASCULINO)
                sujetoEsFechaValida.onNext(false)
                modelo.cambiarCategoria(Persona.Categoria.A)
                modelo.cambiarAfiliacion(Persona.Afiliacion.COTIZANTE)
                testEsPersonaValida.assertValue(false)
                testEsPersonaValida.assertValueCount(1)
            }

            @Test
            fun no_emite_mas_eventos_despues_de_llamar_finalizar_proceso()
            {
                mockearFinalizarProceso()
                modelo.finalizarProceso()
                // Se inicializan todos los campos
                modelo.cambiarNombreCompleto("Nombre")
                modelo.cambiarTipoDocumento(Persona.TipoDocumento.CC)
                modelo.cambiarNumeroDocumento("1234")
                modelo.cambiarGenero(Persona.Genero.MASCULINO)
                sujetoEsFechaValida.onNext(true)
                modelo.cambiarCategoria(Persona.Categoria.A)
                modelo.cambiarAfiliacion(Persona.Afiliacion.COTIZANTE)

                // Se emiten valores validos e invalidos de cada campo
                modelo.cambiarNombreCompleto("")
                modelo.cambiarNombreCompleto("Nombre")
                modelo.cambiarTipoDocumento(Persona.TipoDocumento.CD)
                modelo.cambiarNumeroDocumento("")
                modelo.cambiarNumeroDocumento("1234")
                modelo.cambiarGenero(Persona.Genero.FEMENINO)
                sujetoEsFechaValida.onNext(false)
                sujetoEsFechaValida.onNext(true)
                modelo.cambiarCategoria(Persona.Categoria.B)
                modelo.cambiarAfiliacion(Persona.Afiliacion.BENEFICIARIO)
                testEsPersonaValida.assertValueCount(0)
            }
        }

        @Nested
        inner class AsignarPersona
        {
            private val fechaPruebas = LocalDate.now()
            private val personaPruebas = Persona(
                    ID_CLIENTE,
                    null,
                    "Nombre Valido",
                    Persona.TipoDocumento.CC,
                    "1234",
                    Persona.Genero.MASCULINO,
                    fechaPruebas,
                    Persona.Categoria.A,
                    Persona.Afiliacion.COTIZANTE,
                    null,
                    "empresa",
                    "0",
                    Persona.Tipo.NO_AFILIADO
                                                )

            @BeforeEach
            fun mockearAsignarFecha()
            {
                Mockito.doNothing()
                    .`when`(mockFecha)
                    .asignarFecha(fechaPruebas)
            }

            @Nested
            inner class EnId
            {
                private val testId by lazy { modelo.id.test() }

                @BeforeEach
                fun forzar_lazy()
                {
                    testId.valueCount()
                }

                @Test
                fun emite_valor_correcto_cuando_id_es_null()
                {
                    modelo.asignarPersona(personaPruebas)
                    testId.assertValueAt(1, Opcional.Vacio())
                    testId.assertValueCount(2)
                }

                @Test
                fun emite_valor_correcto_cuando_id_no_es_null()
                {
                    modelo.asignarPersona(personaPruebas.copiar(id = 100))
                    testId.assertValueAt(1, Opcional.De(100L))
                    testId.assertValueCount(2)
                }

                @Test
                fun al_anular_el_id_emite_vacio()
                {
                    modelo.asignarPersona(personaPruebas.copiar(id = 100))

                    modelo.anularId()

                    testId.verificarUltimoValorEmitido(Opcional.Vacio())
                    testId.assertValueCount(3)
                }
            }

            @Nested
            inner class EnNombreCompleto
            {
                private val testNombreCompleto by lazy { modelo.nombreCompleto.test() }

                @BeforeEach
                fun forzar_lazy()
                {
                    testNombreCompleto.valueCount()
                }

                @Test
                fun emite_valor_correcto()
                {
                    modelo.asignarPersona(personaPruebas)
                    testNombreCompleto.assertValue(Notification.createOnNext(personaPruebas.nombreCompleto))
                    testNombreCompleto.assertValueCount(1)
                }
            }

            @Nested
            inner class EnTipoDocumento
            {
                private val testTipoDocumento by lazy { modelo.tipoDocumento.test() }

                @BeforeEach
                fun forzar_lazy()
                {
                    testTipoDocumento.valueCount()
                }

                @Test
                fun emite_valor_correcto()
                {
                    modelo.asignarPersona(personaPruebas)
                    testTipoDocumento.assertValue(personaPruebas.tipoDocumento)
                    testTipoDocumento.assertValueCount(1)
                }
            }

            @Nested
            inner class EnNumeroDocumento
            {
                private val testNumeroDocumento by lazy { modelo.numeroDocumento.test() }

                @BeforeEach
                fun forzar_lazy()
                {
                    testNumeroDocumento.valueCount()
                }

                @Test
                fun emite_valor_correcto()
                {
                    modelo.asignarPersona(personaPruebas)
                    testNumeroDocumento.assertValue(Notification.createOnNext(personaPruebas.numeroDocumento))
                    testNumeroDocumento.assertValueCount(1)
                }
            }

            @Nested
            inner class EnGenero
            {
                private val testGenero by lazy { modelo.genero.test() }

                @BeforeEach
                fun forzar_lazy()
                {
                    testGenero.valueCount()
                }

                @Test
                fun emite_valor_correcto()
                {
                    modelo.asignarPersona(personaPruebas)
                    testGenero.assertValue(personaPruebas.genero)
                    testGenero.assertValueCount(1)
                }
            }

            @Nested
            inner class EnFechaNacimiento
            {
                @Test
                fun llama_el_metodo_asignar_fecha_con_valor_correcto()
                {
                    modelo.asignarPersona(personaPruebas)
                    Mockito.verify(mockFecha, Mockito.times(1)).asignarFecha(fechaPruebas)
                }
            }

            @Nested
            inner class EnCategoria
            {
                private val testCategoria by lazy { modelo.categoria.test() }

                @BeforeEach
                fun forzar_lazy()
                {
                    testCategoria.valueCount()
                }

                @Test
                fun emite_valor_correcto()
                {
                    modelo.asignarPersona(personaPruebas)
                    testCategoria.assertValue(personaPruebas.categoria)
                    testCategoria.assertValueCount(1)
                }
            }

            @Nested
            inner class EnAfiliacion
            {
                private val testAfiliacion by lazy { modelo.afiliacion.test() }

                @BeforeEach
                fun forzar_lazy()
                {
                    testAfiliacion.valueCount()
                }

                @Test
                fun emite_valor_correcto()
                {
                    modelo.asignarPersona(personaPruebas)
                    testAfiliacion.assertValue(personaPruebas.afiliacion)
                    testAfiliacion.assertValueCount(1)
                }
            }
        }

        @Nested
        inner class APersona
        {
            private val fechaPruebas = LocalDate.now()

            private fun mockearAFecha()
            {
                Mockito.doReturn(fechaPruebas)
                    .`when`(mockFecha)
                    .aFecha()
            }

            private fun mockearAFechaInvalida()
            {
                Mockito.doThrow(IllegalStateException())
                    .`when`(mockFecha)
                    .aFecha()
            }

            @BeforeEach
            fun mockearAsignarFecha()
            {
                Mockito.doNothing()
                    .`when`(mockFecha)
                    .asignarFecha(fechaPruebas)
            }

            @Test
            fun retorna_persona_correcta_al_asignar_campos_validos()
            {
                modelo.cambiarNombreCompleto("Nombre Valido")
                modelo.cambiarTipoDocumento(Persona.TipoDocumento.CC)
                modelo.cambiarNumeroDocumento("1234")
                modelo.cambiarGenero(Persona.Genero.MASCULINO)
                modelo.cambiarCategoria(Persona.Categoria.A)
                mockearAFecha()
                modelo.cambiarAfiliacion(Persona.Afiliacion.COTIZANTE)
                val persona = modelo.aPersona()
                val personaEsperada = Persona(
                        ID_CLIENTE,
                        null,
                        "Nombre Valido",
                        Persona.TipoDocumento.CC,
                        "1234",
                        Persona.Genero.MASCULINO,
                        fechaPruebas,
                        Persona.Categoria.A,
                        Persona.Afiliacion.COTIZANTE,
                        null,
                        "empresa",
                        "0",
                        Persona.Tipo.NO_AFILIADO
                                             )
                Assertions.assertEquals(personaEsperada, persona)
            }

            @Test
            fun retorna_persona_correcta_al_asignar_persona_valida()
            {
                val personaEsperada = Persona(
                        ID_CLIENTE,
                        null,
                        "Nombre Valido",
                        Persona.TipoDocumento.CC,
                        "1234",
                        Persona.Genero.MASCULINO,
                        fechaPruebas,
                        Persona.Categoria.A,
                        Persona.Afiliacion.COTIZANTE,
                        null,
                        "empresa",
                        "0",
                        Persona.Tipo.NO_AFILIADO
                                             )
                modelo.asignarPersona(personaEsperada)
                mockearAFecha()
                val persona = modelo.aPersona()
                Assertions.assertEquals(personaEsperada, persona)
            }

            @Test
            fun lanza_IllegalStateException_si_no_se_han_asignado_ningun_campo()
            {
                mockearAFechaInvalida()
                assertThrows<IllegalStateException> { modelo.aPersona() }
            }

            @Test
            fun lanza_IllegalStateException_con_todos_los_campos_validos_pero_sin_asignar_nombre_completo()
            {
                modelo.cambiarTipoDocumento(Persona.TipoDocumento.CC)
                modelo.cambiarNumeroDocumento("1234")
                modelo.cambiarGenero(Persona.Genero.MASCULINO)
                mockearAFecha()
                modelo.cambiarCategoria(Persona.Categoria.A)
                modelo.cambiarAfiliacion(Persona.Afiliacion.COTIZANTE)
                assertThrows<IllegalStateException> { modelo.aPersona() }
            }

            @Test
            fun lanza_IllegalStateException_con_todos_los_campos_validos_pero_sin_asignar_tipo_documento()
            {
                modelo.cambiarNombreCompleto("Nombre Valido")
                modelo.cambiarNumeroDocumento("1234")
                modelo.cambiarGenero(Persona.Genero.MASCULINO)
                mockearAFecha()
                modelo.cambiarCategoria(Persona.Categoria.A)
                modelo.cambiarAfiliacion(Persona.Afiliacion.COTIZANTE)
                assertThrows<IllegalStateException> { modelo.aPersona() }
            }

            @Test
            fun lanza_IllegalStateException_con_todos_los_campos_validos_pero_sin_asignar_numero_documento()
            {
                modelo.cambiarNombreCompleto("Nombre Valido")
                modelo.cambiarTipoDocumento(Persona.TipoDocumento.CC)
                modelo.cambiarGenero(Persona.Genero.MASCULINO)
                mockearAFecha()
                modelo.cambiarCategoria(Persona.Categoria.A)
                modelo.cambiarAfiliacion(Persona.Afiliacion.COTIZANTE)
                assertThrows<IllegalStateException> { modelo.aPersona() }
            }

            @Test
            fun lanza_IllegalStateException_con_todos_los_campos_validos_pero_sin_asignar_genero()
            {
                modelo.cambiarNombreCompleto("Nombre Valido")
                modelo.cambiarTipoDocumento(Persona.TipoDocumento.CC)
                modelo.cambiarNumeroDocumento("1234")
                mockearAFecha()
                modelo.cambiarCategoria(Persona.Categoria.A)
                modelo.cambiarAfiliacion(Persona.Afiliacion.COTIZANTE)
                assertThrows<IllegalStateException> { modelo.aPersona() }
            }

            @Test
            fun lanza_IllegalStateException_con_todos_los_campos_validos_pero_sin_asignar_fecha_nacimiento()
            {
                modelo.cambiarNombreCompleto("Nombre Valido")
                modelo.cambiarTipoDocumento(Persona.TipoDocumento.CC)
                modelo.cambiarNumeroDocumento("1234")
                mockearAFechaInvalida()
                modelo.cambiarGenero(Persona.Genero.MASCULINO)
                modelo.cambiarCategoria(Persona.Categoria.A)
                modelo.cambiarAfiliacion(Persona.Afiliacion.COTIZANTE)
                assertThrows<IllegalStateException> { modelo.aPersona() }
            }

            @Test
            fun lanza_IllegalStateException_con_todos_los_campos_validos_pero_sin_asignar_categoria()
            {
                modelo.cambiarNombreCompleto("Nombre Valido")
                modelo.cambiarTipoDocumento(Persona.TipoDocumento.CC)
                modelo.cambiarNumeroDocumento("1234")
                modelo.cambiarGenero(Persona.Genero.MASCULINO)
                mockearAFecha()
                modelo.cambiarAfiliacion(Persona.Afiliacion.COTIZANTE)
                assertThrows<IllegalStateException> { modelo.aPersona() }
            }

            @Test
            fun lanza_IllegalStateException_con_todos_los_campos_validos_pero_sin_asignar_afiliacion()
            {
                modelo.cambiarNombreCompleto("Nombre Valido")
                modelo.cambiarTipoDocumento(Persona.TipoDocumento.CC)
                modelo.cambiarNumeroDocumento("1234")
                modelo.cambiarGenero(Persona.Genero.MASCULINO)
                mockearAFecha()
                modelo.cambiarCategoria(Persona.Categoria.A)
                assertThrows<IllegalStateException> { modelo.aPersona() }
            }

            @Test
            fun lanza_IllegalStateException_con_todos_los_campos_validos_pero_con_nombre_completo_invalido()
            {
                modelo.cambiarNombreCompleto("")
                modelo.cambiarTipoDocumento(Persona.TipoDocumento.CC)
                modelo.cambiarNumeroDocumento("1234")
                modelo.cambiarGenero(Persona.Genero.MASCULINO)
                mockearAFecha()
                modelo.cambiarCategoria(Persona.Categoria.A)
                modelo.cambiarAfiliacion(Persona.Afiliacion.COTIZANTE)
                assertThrows<IllegalStateException> { modelo.aPersona() }
            }

            @Test
            fun lanza_IllegalStateException_con_todos_los_campos_validos_pero_con_numero_documento_invalido()
            {
                modelo.cambiarNombreCompleto("Nombre Valido")
                modelo.cambiarTipoDocumento(Persona.TipoDocumento.CC)
                modelo.cambiarNumeroDocumento("")
                modelo.cambiarGenero(Persona.Genero.MASCULINO)
                mockearAFecha()
                modelo.cambiarCategoria(Persona.Categoria.A)
                modelo.cambiarAfiliacion(Persona.Afiliacion.COTIZANTE)
                modelo.cambiarNombreCompleto("")
                assertThrows<IllegalStateException> { modelo.aPersona() }
            }
        }

        @Nested
        inner class DarDocumentoCompleto
        {
            private val fechaPruebas = LocalDate.now()

            @BeforeEach
            fun mockearAsignarFecha()
            {
                Mockito.doNothing()
                    .`when`(mockFecha)
                    .asignarFecha(fechaPruebas)
            }

            @Test
            fun retorna_documento_correcto_al_asignar_campos_validos()
            {
                modelo.cambiarTipoDocumento(Persona.TipoDocumento.CC)
                modelo.cambiarNumeroDocumento("1234")
                val documento = modelo.darDocumentoCompleto()
                val documentoEsperado = DocumentoCompleto(Persona.TipoDocumento.CC,"1234")
                Assertions.assertEquals(documentoEsperado, documento)
            }

            @Test
            fun retorna_documento_correcto_al_asignar_persona_valida()
            {
                val personaEsperada = Persona(
                        ID_CLIENTE,
                        null,
                        "Nombre Valido",
                        Persona.TipoDocumento.CC,
                        "1234",
                        Persona.Genero.MASCULINO,
                        fechaPruebas,
                        Persona.Categoria.A,
                        Persona.Afiliacion.COTIZANTE,
                        null,
                        "empresa",
                        "0",
                        Persona.Tipo.NO_AFILIADO
                                             )
                modelo.asignarPersona(personaEsperada)
                val documento = modelo.darDocumentoCompleto()
                val documentoEsperado = DocumentoCompleto(Persona.TipoDocumento.CC,"1234")
                Assertions.assertEquals(documentoEsperado, documento)
            }

            @Test
            fun lanza_IllegalStateException_si_no_se_han_asignado_ningun_campo()
            {
                assertThrows<IllegalStateException> { modelo.darDocumentoCompleto() }
            }

            @Test
            fun lanza_IllegalStateException_con_todos_los_campos_validos_pero_sin_asignar_tipo_documento()
            {
                modelo.cambiarNumeroDocumento("1234")
                assertThrows<IllegalStateException> { modelo.darDocumentoCompleto() }
            }

            @Test
            fun lanza_IllegalStateException_con_todos_los_campos_validos_pero_sin_asignar_numero_documento()
            {
                modelo.cambiarTipoDocumento(Persona.TipoDocumento.CC)
                assertThrows<IllegalStateException> { modelo.darDocumentoCompleto() }
            }

            @Test
            fun lanza_IllegalStateException_con_todos_los_campos_validos_pero_con_numero_documento_invalido()
            {
                modelo.cambiarTipoDocumento(Persona.TipoDocumento.CC)
                modelo.cambiarNumeroDocumento("")
                assertThrows<IllegalStateException> { modelo.darDocumentoCompleto() }
            }
        }
    }
}