package co.smartobjects.entidades.fondos.libros

import co.smartobjects.entidades.excepciones.EntidadConCampoVacio
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.mockConDefaultAnswer
import co.smartobjects.utilidades.Decimal
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DisplayName("LibroDePrecios")
internal class LibroSegunReglasCompletoCompletoPruebas
{
    @Test
    fun constructor_que_recibe_libros_funciona_correctamente()
    {
        val libroSegunReglas = LibroSegunReglas(45656, 3454, "Nombre", 1, hashSetOf(), hashSetOf(), hashSetOf())
        val precio = PrecioEnLibro(Precio(Decimal(12.45689), 1L), 2L)
        val libro = LibroDePrecios(1, 2, "no importa", setOf(precio))

        val entidadEsperada = LibroSegunReglasCompleto(
                libroSegunReglas.idCliente,
                libroSegunReglas.id,
                libroSegunReglas.nombre,
                libro,
                libroSegunReglas.reglasIdUbicacion,
                libroSegunReglas.reglasIdGrupoDeClientes,
                libroSegunReglas.reglasIdPaquete
                                                      )

        val entidadProcesada = LibroSegunReglasCompleto(libroSegunReglas, libro)

        assertEquals(entidadEsperada, entidadProcesada)
    }

    @Test
    fun convertir_a_libro_segun_reglas_retorna_entidad_correcta()
    {
        val libroSegunReglas = LibroSegunReglas(45656, 3454, "Nombre", 2, hashSetOf(), hashSetOf(), hashSetOf())
        val libro = LibroDePrecios(1, 2, "no importa", setOf(PrecioEnLibro(Precio(Decimal(12.45689), 1L), 2L)))

        val libroSegunReglasCompleto = LibroSegunReglasCompleto(
                libroSegunReglas.idCliente,
                libroSegunReglas.id,
                libroSegunReglas.nombre,
                libro,
                libroSegunReglas.reglasIdUbicacion,
                libroSegunReglas.reglasIdGrupoDeClientes,
                libroSegunReglas.reglasIdPaquete
                                                               )

        val entidadProcesada = libroSegunReglasCompleto.aLibroSegunReglas()

        assertEquals(libroSegunReglas, entidadProcesada)
    }

    @Test
    fun hace_trim_a_nombre_correctamente()
    {
        val precio = PrecioEnLibro(Precio(Decimal(12.45689), 1L), 2L)
        val libro = LibroDePrecios(1, 2, "no importa", setOf(precio))
        val entidadEsperada = LibroSegunReglasCompleto(0, null, "Nombre", libro, hashSetOf(), hashSetOf(), hashSetOf())

        val entidadProcesada = entidadEsperada.copiar(nombre = "   \t  Nombre\t\t   ")

        assertEquals(entidadEsperada, entidadProcesada)
    }

    @Test
    fun campos_quedan_con_valores_correctos()
    {
        val precio1 = PrecioEnLibro(Precio(Decimal(12.45689), 1L), 2L)
        val precio2 = PrecioEnLibro(Precio(Decimal(13.45689), 3L), 4L)
        val libro = LibroDePrecios(1, 2, "Libro precios", setOf(precio1, precio2))
        val entidad = LibroSegunReglasCompleto(0, null, "nombre de prueba", libro, hashSetOf(), hashSetOf(), hashSetOf())

        assertEquals("nombre de prueba", entidad.campoNombre.valor)
    }

    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val precioInicial1 = PrecioEnLibro(Precio(Decimal(12.45689), 1L), 2L)
        val precioInicial2 = PrecioEnLibro(Precio(Decimal(13.45689), 3L), 4L)
        val libroDePrueba = LibroDePrecios(1, 1, "Libro precios", setOf(precioInicial1, precioInicial2))

        val reglaUbicacionInicial = ReglaDeIdUbicacion(1L)
        val reglaGrupoInicial = ReglaDeIdGrupoDeClientes(1L)
        val reglaPaqueteInicial = ReglaDeIdPaquete(1L)

        val entidadInicial =
                LibroSegunReglasCompleto(
                        0,
                        null,
                        "nombre de prueba",
                        libroDePrueba,
                        linkedSetOf(reglaUbicacionInicial),
                        linkedSetOf(reglaGrupoInicial),
                        linkedSetOf(reglaPaqueteInicial)
                                        )

        val precioFinal1 = PrecioEnLibro(Precio(Decimal(20.455), 7L), 8L)
        val precioFinal2 = PrecioEnLibro(Precio(Decimal(13.45689), 3L), 4L)
        val precioFinal3 = PrecioEnLibro(Precio(Decimal(33.45689), 5L), 6L)
        val libroDePruebaFinal = LibroDePrecios(10, 20, "Libro precios copiado", setOf(precioFinal1, precioFinal2, precioFinal3))

        val reglaUbicacionFinal = ReglaDeIdUbicacion(18L)
        val reglaGrupoFinal = ReglaDeIdGrupoDeClientes(456L)
        val reglaPaqueteFinal = ReglaDeIdPaquete(3456L)

        val entidadEsperada =
                LibroSegunReglasCompleto(
                        5,
                        6,
                        "nombre de prueba 2",
                        libroDePruebaFinal,
                        linkedSetOf(reglaUbicacionFinal),
                        linkedSetOf(reglaGrupoFinal),
                        linkedSetOf(reglaPaqueteFinal)
                                        )

        val entidadCopiada =
                entidadInicial
                    .copiar(
                            idCliente = 5,
                            id = 6,
                            nombre = "nombre de prueba 2",
                            libro = libroDePruebaFinal,
                            reglasIdUbicacion = linkedSetOf(reglaUbicacionFinal),
                            reglasIdGrupoDeClientes = linkedSetOf(reglaGrupoFinal),
                            reglasIdPaquete = linkedSetOf(reglaPaqueteFinal)
                           )

        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun copiar_con_id_funciona_correctamente()
    {
        val precioInicial1 = PrecioEnLibro(Precio(Decimal(12.45689), 1L), 2L)
        val precioInicial2 = PrecioEnLibro(Precio(Decimal(13.45689), 3L), 4L)
        val libroDePrueba = LibroDePrecios(1, 1, "Libro precios", setOf(precioInicial1, precioInicial2))

        val reglaUbicacionInicial = ReglaDeIdUbicacion(1L)
        val reglaGrupoInicial = ReglaDeIdGrupoDeClientes(1L)
        val reglaPaqueteInicial = ReglaDeIdPaquete(1L)

        val entidadInicial =
                LibroSegunReglasCompleto(
                        0,
                        null,
                        "nombre de prueba",
                        libroDePrueba,
                        linkedSetOf(reglaUbicacionInicial),
                        linkedSetOf(reglaGrupoInicial),
                        linkedSetOf(reglaPaqueteInicial)
                                        )

        val entidadEsperada =
                LibroSegunReglasCompleto(
                        0,
                        353534,
                        "nombre de prueba",
                        libroDePrueba,
                        linkedSetOf(reglaUbicacionInicial),
                        linkedSetOf(reglaGrupoInicial),
                        linkedSetOf(reglaPaqueteInicial)
                                        )

        val entidadCopiada = entidadInicial.copiarConId(353534)

        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun no_se_permite_instanciar_con_nombre_vacio()
    {
        val precio = PrecioEnLibro(Precio(Decimal(12.45689), 1L), 2L)
        val libro = LibroDePrecios(1, 2, "no importa", setOf(precio))
        val excepcion = assertThrows<EntidadConCampoVacio> {
            LibroSegunReglasCompleto(0, null, "", libro, hashSetOf(), hashSetOf(), hashSetOf())
        }

        assertEquals(LibroSegunReglas.Campos.NOMBRE, excepcion.nombreDelCampo)
        assertEquals(LibroSegunReglas.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun no_se_permite_instanciar_con_nombre_con_solo_espacios_o_tabs()
    {
        val precio = PrecioEnLibro(Precio(Decimal(12.45689), 1L), 2L)
        val libro = LibroDePrecios(1, 2, "no importa", setOf(precio))
        val excepcion = assertThrows<EntidadConCampoVacio> {
            LibroSegunReglasCompleto(0, null, "   \t\t   ", libro, hashSetOf(), hashSetOf(), hashSetOf())
        }

        assertEquals(LibroSegunReglas.Campos.NOMBRE, excepcion.nombreDelCampo)
        assertEquals(LibroSegunReglas.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun reglas_es_la_union_de_todas_las_reglas()
    {
        val precio1 = PrecioEnLibro(Precio(Decimal(12.45689), 1L), 2L)
        val precio2 = PrecioEnLibro(Precio(Decimal(13.45689), 3L), 4L)
        val libroDePrueba = LibroDePrecios(1, 1, "Libro precios", setOf(precio1, precio2))

        val reglaUbicacion = ReglaDeIdUbicacion(1L)
        val reglaGrupo = ReglaDeIdGrupoDeClientes(2L)
        val reglaPaquete = ReglaDeIdPaquete(3L)

        val reglasEsperadas = sequenceOf<Regla<*>>(reglaUbicacion, reglaGrupo, reglaPaquete)

        val unionDeReglas =
                LibroSegunReglasCompleto(
                        0,
                        null,
                        "nombre de prueba",
                        libroDePrueba,
                        linkedSetOf(reglaUbicacion),
                        linkedSetOf(reglaGrupo),
                        linkedSetOf(reglaPaquete)
                                        )
                    .reglas

        assertEquals(reglasEsperadas.toSet(), unionDeReglas.toSet())
    }

    @Nested
    inner class EsAplicable
    {
        @Nested
        inner class RetornaFalse
        {
            @ArgumentsSource(ProveedorVariacionesAlMenosUnaConReglas::class)
            @ParameterizedTest(name = "{0}")
            fun si_para_un_conjunto_de_reglas_no_vacio_se_envia_un_parametro_nulo(
                    nombrePrueba: String,
                    reglas: ProveedorVariacionesReglas.Reglas
                                                                                 )
            {
                fun darIdAUsarSegunConjuntoDeReglas(conjunto: Set<Regla<Long>>): Long?
                {
                    return if (conjunto.isNotEmpty()) null else 1234L
                }

                val libro = darLibroDePrueba(reglas.reglasIdUbicacion, reglas.reglasIdGrupoDeClientes, reglas.reglasIdPaquete)

                val esAplicableCalculado =
                        libro.esAplicable(
                                darIdAUsarSegunConjuntoDeReglas(reglas.reglasIdUbicacion),
                                darIdAUsarSegunConjuntoDeReglas(reglas.reglasIdGrupoDeClientes),
                                darIdAUsarSegunConjuntoDeReglas(reglas.reglasIdPaquete)
                                         )

                assertFalse(esAplicableCalculado)
            }

            @ArgumentsSource(ProveedorVariacionesAlMenosUnaConReglas::class)
            @ParameterizedTest(name = "{0}")
            fun si_para_un_conjunto_de_reglas_no_vacio_se_envia_un_parametro_no_nulo_y_no_valida_ninguna_regla(
                    nombrePrueba: String,
                    reglas: ProveedorVariacionesReglas.Reglas
                                                                                                              )
            {
                fun darIdAUsarSegunConjuntoDeReglas(conjunto: Set<Regla<Long>>): Long?
                {
                    return if (conjunto.isNotEmpty()) conjunto.first().restriccion + 9999L else 1234L
                }

                val libro = darLibroDePrueba(reglas.reglasIdUbicacion, reglas.reglasIdGrupoDeClientes, reglas.reglasIdPaquete)

                val esAplicable =
                        libro.esAplicable(
                                darIdAUsarSegunConjuntoDeReglas(reglas.reglasIdUbicacion),
                                darIdAUsarSegunConjuntoDeReglas(reglas.reglasIdGrupoDeClientes),
                                darIdAUsarSegunConjuntoDeReglas(reglas.reglasIdPaquete)
                                         )

                assertFalse(esAplicable)
            }
        }

        @Nested
        inner class RetornaTrue
        {
            @ArgumentsSource(ProveedorVariacionesReglas::class)
            @DisplayName("Si las reglas están vacías y/o valida reglas según parámetros")
            @ParameterizedTest(name = "{0}")
            fun con_reglas_vacias_o_que_apliquen(
                    nombrePrueba: String,
                    reglas: ProveedorVariacionesReglas.Reglas
                                                )
            {
                fun darNuloSiConjuntoVacioSinoIdQueValide(conjunto: Set<Regla<Long>>): Long?
                {
                    return if (conjunto.isEmpty()) null else conjunto.first().restriccion
                }

                fun darIdCualquieraSiConjuntoVacioSinoIdQueValide(conjunto: Set<Regla<Long>>): Long?
                {
                    return if (conjunto.isEmpty()) null else conjunto.first().restriccion
                }

                val libro = darLibroDePrueba(reglas.reglasIdUbicacion, reglas.reglasIdGrupoDeClientes, reglas.reglasIdPaquete)

                assertTrue(
                        libro.esAplicable(
                                darNuloSiConjuntoVacioSinoIdQueValide(reglas.reglasIdUbicacion),
                                darNuloSiConjuntoVacioSinoIdQueValide(reglas.reglasIdGrupoDeClientes),
                                darNuloSiConjuntoVacioSinoIdQueValide(reglas.reglasIdPaquete)
                                         )
                          )

                assertTrue(
                        libro.esAplicable(
                                darIdCualquieraSiConjuntoVacioSinoIdQueValide(reglas.reglasIdUbicacion),
                                darIdCualquieraSiConjuntoVacioSinoIdQueValide(reglas.reglasIdGrupoDeClientes),
                                darIdCualquieraSiConjuntoVacioSinoIdQueValide(reglas.reglasIdPaquete)
                                         )
                          )
            }
        }
    }

    @Nested
    inner class CalcularEspecificidad
    {
        @ArgumentsSource(ProveedorVariacionesAlMenosUnConjuntoVacio::class)
        @ParameterizedTest(name = "{0}")
        fun aumenta_en_1_la_especificidad_por_cada_parametro_nulo_y_conjunto_de_reglas_vacias(
                nombrePrueba: String,
                reglas: ProveedorVariacionesReglas.Reglas
                                                                                             )
        {
            fun darIdAUsarSegunConjuntoDeReglas(conjunto: Set<Regla<Long>>): Long?
            {
                return if (conjunto.isEmpty()) null else conjunto.first().restriccion
            }

            val darEspecificidadSegunConjuntoDeReglas = { conjunto: Set<Regla<*>> ->
                if (conjunto.isEmpty()) 1 else 10
            }

            val especificidadEsperada =
                    darEspecificidadSegunConjuntoDeReglas(reglas.reglasIdUbicacion) +
                    darEspecificidadSegunConjuntoDeReglas(reglas.reglasIdGrupoDeClientes) +
                    darEspecificidadSegunConjuntoDeReglas(reglas.reglasIdPaquete)

            val libro = darLibroDePrueba(reglas.reglasIdUbicacion, reglas.reglasIdGrupoDeClientes, reglas.reglasIdPaquete)

            val especificidadCalculada =
                    libro.calcularEspecificidad(
                            darIdAUsarSegunConjuntoDeReglas(reglas.reglasIdUbicacion),
                            darIdAUsarSegunConjuntoDeReglas(reglas.reglasIdGrupoDeClientes),
                            darIdAUsarSegunConjuntoDeReglas(reglas.reglasIdPaquete)
                                               )

            assertEquals(especificidadEsperada, especificidadCalculada)
        }

        @ArgumentsSource(ProveedorVariacionesAlMenosUnConjuntoVacio::class)
        @ParameterizedTest(name = "{0}")
        fun aumenta_en_0_la_especificidad_por_cada_parametro_no_nulo_y_conjunto_de_reglas_vacias(
                nombrePrueba: String,
                reglas: ProveedorVariacionesReglas.Reglas
                                                                                                )
        {
            fun darIdAUsarSegunConjuntoDeReglas(conjunto: Set<Regla<Long>>): Long?
            {
                return if (conjunto.isEmpty()) 1234L else conjunto.first().restriccion
            }

            val libro = darLibroDePrueba(reglas.reglasIdUbicacion, reglas.reglasIdGrupoDeClientes, reglas.reglasIdPaquete)

            val especificidadCalculada =
                    libro.calcularEspecificidad(
                            darIdAUsarSegunConjuntoDeReglas(reglas.reglasIdUbicacion),
                            darIdAUsarSegunConjuntoDeReglas(reglas.reglasIdGrupoDeClientes),
                            darIdAUsarSegunConjuntoDeReglas(reglas.reglasIdPaquete)
                                               )

            assertEquals(10 * reglas.numeroConjuntosConReglas, especificidadCalculada)
        }

        @ArgumentsSource(ProveedorVariacionesReglas::class)
        @ParameterizedTest(name = "{0}")
        fun aumenta_en_10_la_especificidad_por_cada_parametro_no_nulo_y_conjunto_de_reglas_que_contengan_el_parametro(
                nombrePrueba: String,
                reglas: ProveedorVariacionesReglas.Reglas
                                                                                                                     )
        {
            fun darIdAUsarSegunConjuntoDeReglas(conjunto: Set<Regla<Long>>): Long?
            {
                return if (conjunto.isEmpty()) 1234L else conjunto.first().restriccion
            }

            val darEspecificidadSegunConjuntoDeReglas = { conjunto: Set<Regla<*>> ->
                if (conjunto.isEmpty()) 0 else 10
            }

            val especificidadEsperada =
                    darEspecificidadSegunConjuntoDeReglas(reglas.reglasIdUbicacion) +
                    darEspecificidadSegunConjuntoDeReglas(reglas.reglasIdGrupoDeClientes) +
                    darEspecificidadSegunConjuntoDeReglas(reglas.reglasIdPaquete)

            val libro = darLibroDePrueba(reglas.reglasIdUbicacion, reglas.reglasIdGrupoDeClientes, reglas.reglasIdPaquete)

            val especificidadCalculada =
                    libro.calcularEspecificidad(
                            darIdAUsarSegunConjuntoDeReglas(reglas.reglasIdUbicacion),
                            darIdAUsarSegunConjuntoDeReglas(reglas.reglasIdGrupoDeClientes),
                            darIdAUsarSegunConjuntoDeReglas(reglas.reglasIdPaquete)
                                               )

            assertEquals(especificidadEsperada, especificidadCalculada)
        }

        @ArgumentsSource(ProveedorVariacionesAlMenosUnaConReglas::class)
        @ParameterizedTest(name = "{0}")
        fun `retorna -1 si un parámetro es nulo y existen reglas para el paráemtro`(
                nombrePrueba: String,
                reglas: ProveedorVariacionesReglas.Reglas
                                                                                   )
        {
            fun darIdAUsarSegunConjuntoDeReglas(conjunto: Set<Regla<Long>>): Long?
            {
                return if (conjunto.isEmpty()) 1234L else null
            }

            val libro = darLibroDePrueba(reglas.reglasIdUbicacion, reglas.reglasIdGrupoDeClientes, reglas.reglasIdPaquete)

            val especificidadCalculada =
                    libro.calcularEspecificidad(
                            darIdAUsarSegunConjuntoDeReglas(reglas.reglasIdUbicacion),
                            darIdAUsarSegunConjuntoDeReglas(reglas.reglasIdGrupoDeClientes),
                            darIdAUsarSegunConjuntoDeReglas(reglas.reglasIdPaquete)
                                               )

            assertEquals(-1, especificidadCalculada)
        }

        @ArgumentsSource(ProveedorVariacionesAlMenosUnaConReglas::class)
        @ParameterizedTest(name = "{0}")
        fun `retorna -1 si un parámetro es no nulo, existen reglas para el paráemtro y no se encontró ninguna`(
                nombrePrueba: String,
                reglas: ProveedorVariacionesReglas.Reglas
                                                                                                              )
        {
            fun darIdAUsarSegunConjuntoDeReglas(conjunto: Set<Regla<Long>>): Long?
            {
                return if (conjunto.isEmpty()) 1234L else conjunto.first().restriccion + 9999L
            }

            val libro = darLibroDePrueba(reglas.reglasIdUbicacion, reglas.reglasIdGrupoDeClientes, reglas.reglasIdPaquete)

            val especificidadCalculada =
                    libro.calcularEspecificidad(
                            darIdAUsarSegunConjuntoDeReglas(reglas.reglasIdUbicacion),
                            darIdAUsarSegunConjuntoDeReglas(reglas.reglasIdGrupoDeClientes),
                            darIdAUsarSegunConjuntoDeReglas(reglas.reglasIdPaquete)
                                               )

            assertEquals(-1, especificidadCalculada)
        }
    }

    @Nested
    inner class BuscarReglasQueAplican
    {
        @ArgumentsSource(ProveedorVariacionesReglas::class)
        @ParameterizedTest(name = "{0}")
        fun si_el_libro_no_contiene_reglas_retorna_un_conjunto_vacio(nombrePrueba: String, reglas: ProveedorVariacionesReglas.Reglas)
        {
            val libroDePrueba = darLibroDePrueba(mutableSetOf(), mutableSetOf(), mutableSetOf())

            val reglasEncontradas =
                    libroDePrueba.buscarReglasQueAplican(
                            reglas.reglasIdUbicacion.firstOrNull()?.restriccion,
                            reglas.reglasIdGrupoDeClientes.firstOrNull()?.restriccion,
                            reglas.reglasIdPaquete.firstOrNull()?.restriccion
                                                        )

            assertEquals(setOf(), reglasEncontradas)
        }

        @Test
        fun cada_regla_que_aplique_se_encuentra_en_el_conjunto()
        {
            val reglaIdUbicacion = ReglaDeIdUbicacion(1L)
            val reglaDeIdGrupoDeClientes = ReglaDeIdGrupoDeClientes(2L)
            val reglaDeIdPaquete = ReglaDeIdPaquete(3L)

            val libroDePrueba = darLibroDePrueba(mutableSetOf(reglaIdUbicacion), mutableSetOf(reglaDeIdGrupoDeClientes), mutableSetOf(reglaDeIdPaquete))

            assertEquals(setOf(reglaIdUbicacion), libroDePrueba.buscarReglasQueAplican(reglaIdUbicacion.restriccion, null, 999))
            assertEquals(setOf(reglaDeIdGrupoDeClientes), libroDePrueba.buscarReglasQueAplican(null, reglaDeIdGrupoDeClientes.restriccion, 999))
            assertEquals(setOf(reglaDeIdPaquete), libroDePrueba.buscarReglasQueAplican(999, null, reglaDeIdPaquete.restriccion))

            assertEquals(
                    setOf(reglaIdUbicacion, reglaDeIdGrupoDeClientes, reglaDeIdPaquete),
                    libroDePrueba.buscarReglasQueAplican(reglaIdUbicacion.restriccion, reglaDeIdGrupoDeClientes.restriccion, reglaDeIdPaquete.restriccion)
                        )
        }
    }

    private fun darLibroDePrueba(
            reglasIdUbicacion: MutableSet<ReglaDeIdUbicacion>,
            reglasIdGrupoDeClientes: MutableSet<ReglaDeIdGrupoDeClientes>,
            reglasIdPaquete: MutableSet<ReglaDeIdPaquete>)
            : LibroSegunReglasCompleto<*>
    {
        return LibroSegunReglasCompleto(
                0,
                null,
                "nombre de prueba",
                mockConDefaultAnswer(LibroDePrecios::class.java),
                reglasIdUbicacion,
                reglasIdGrupoDeClientes,
                reglasIdPaquete
                                       )
    }

    internal class ProveedorVariacionesAlMenosUnaConReglas : ArgumentsProvider
    {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments>
        {
            return ProveedorVariacionesReglas().provideArguments(context).filter {
                (it.get().last() as ProveedorVariacionesReglas.Reglas).alMenosUnaConReglas
            }
        }
    }

    internal class ProveedorVariacionesAlMenosUnConjuntoVacio : ArgumentsProvider
    {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments>
        {
            return ProveedorVariacionesReglas().provideArguments(context).filter {
                (it.get().last() as ProveedorVariacionesReglas.Reglas).alMenosUnConjuntoVacio
            }
        }
    }

    internal class ProveedorVariacionesReglas : ArgumentsProvider
    {
        companion object
        {
            private val REGLA_UBICACION = ReglaDeIdUbicacion(1L)
            private val REGLA_GRUPO = ReglaDeIdGrupoDeClientes(2L)
            private val REGLA_PAQUETE = ReglaDeIdPaquete(3L)
        }

        override fun provideArguments(context: ExtensionContext): Stream<out Arguments>
        {
            return Stream.of(
                    Arguments.of(
                            "Sin reglas definidas",
                            Reglas(linkedSetOf(), linkedSetOf(), linkedSetOf())
                                ),
                    Arguments.of(
                            "Sin reglas de ubicación y grupo definidas",
                            Reglas(linkedSetOf(), linkedSetOf(), linkedSetOf(REGLA_PAQUETE))
                                ),
                    Arguments.of(
                            "Sin reglas de ubicación y paquetes definidas",
                            Reglas(linkedSetOf(), linkedSetOf(REGLA_GRUPO), linkedSetOf())
                                ),
                    Arguments.of(
                            "Sin regla de ubicación definida",
                            Reglas(linkedSetOf(), linkedSetOf(REGLA_GRUPO), linkedSetOf(REGLA_PAQUETE))
                                ),
                    Arguments.of(
                            "Sin reglas de grupo y paquetes definidas",
                            Reglas(linkedSetOf(REGLA_UBICACION), linkedSetOf(), linkedSetOf())
                                ),
                    Arguments.of(
                            "Sin regla de grupo definida",
                            Reglas(linkedSetOf(REGLA_UBICACION), linkedSetOf(), linkedSetOf(REGLA_PAQUETE))
                                ),
                    Arguments.of(
                            "Sin regla de paquete definida",
                            Reglas(linkedSetOf(REGLA_UBICACION), linkedSetOf(REGLA_GRUPO), linkedSetOf())
                                ),
                    Arguments.of(
                            "Con todas las reglas definidas",
                            Reglas(linkedSetOf(REGLA_UBICACION), linkedSetOf(REGLA_GRUPO), linkedSetOf(REGLA_PAQUETE))
                                )
                            )
        }

        data class Reglas(
                val reglasIdUbicacion: MutableSet<ReglaDeIdUbicacion>,
                val reglasIdGrupoDeClientes: MutableSet<ReglaDeIdGrupoDeClientes>,
                val reglasIdPaquete: MutableSet<ReglaDeIdPaquete>
                         )
        {
            val alMenosUnaConReglas =
                    reglasIdUbicacion.isNotEmpty() || reglasIdGrupoDeClientes.isNotEmpty() || reglasIdPaquete.isNotEmpty()

            val alMenosUnConjuntoVacio =
                    reglasIdUbicacion.isEmpty() || reglasIdGrupoDeClientes.isEmpty() || reglasIdPaquete.isEmpty()

            // Asume tamaños de 1 en cada conjunto
            val numeroConjuntosConReglas = reglasIdUbicacion.size + reglasIdGrupoDeClientes.size + reglasIdPaquete.size
        }
    }
}