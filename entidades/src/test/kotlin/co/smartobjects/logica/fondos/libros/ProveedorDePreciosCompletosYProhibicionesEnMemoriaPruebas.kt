package co.smartobjects.logica.fondos.libros

import co.smartobjects.entidades.fondos.Dinero
import co.smartobjects.entidades.fondos.libros.*
import co.smartobjects.entidades.fondos.precios.Impuesto
import co.smartobjects.entidades.fondos.precios.ImpuestoSoloTasa
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.entidades.fondos.precios.PrecioCompleto
import co.smartobjects.utilidades.Decimal
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


@DisplayName("ProveedorDePreciosCompletosYProhibicionesEnMemoria")
internal class ProveedorDePreciosCompletosYProhibicionesEnMemoriaPruebas
{
    companion object
    {
        private const val ID_UNICO_IMPUESTO = 1L
        private const val ID_UNICA_UBICACION = 1L
        private const val ID_UNICO_GRUPO_CLIENTES = 1L
        private const val ID_PAQUETE_PRINCIPAL = 1L
        private const val ID_PAQUETE_SECUNDARIO = ID_PAQUETE_PRINCIPAL + 999
        private const val ID_PAQUETE_TERCIARIO = ID_PAQUETE_PRINCIPAL + ID_PAQUETE_PRINCIPAL
    }

    private val impuestosDisponibles = sequenceOf(Impuesto(1, ID_UNICO_IMPUESTO, "Impuesto", Decimal(10)))
    private val fondosDisponibles =
            sequenceOf(
                    Dinero(1, 1, "Fondo id 1", true, false, false, Precio(Decimal(111), 1), ""),
                    Dinero(1, 2, "Fondo id 2", true, false, false, Precio(Decimal(222), 1), "")
                      )

    private val reglaUbicacion = ReglaDeIdUbicacion(ID_UNICA_UBICACION)
    private val reglaGrupo = ReglaDeIdGrupoDeClientes(ID_UNICO_GRUPO_CLIENTES)
    private val reglaPaquete = ReglaDeIdPaquete(ID_PAQUETE_PRINCIPAL)

    @Nested
    inner class DarPreciosCompletosDeFondos
    {
        private val libroReglasSoloPrecios =
                sequenceOf(
                        LibroSegunReglasCompleto(
                                1, 1, "Libro menos específico",
                                LibroDePrecios(
                                        1,
                                        1,
                                        "Libro Precios 2",
                                        setOf(
                                                PrecioEnLibro(Precio(Decimal(123), ID_UNICO_IMPUESTO), 1),
                                                PrecioEnLibro(Precio(Decimal(321), ID_UNICO_IMPUESTO), 2)
                                             )
                                              ),
                                hashSetOf(reglaUbicacion),
                                hashSetOf(),
                                hashSetOf()
                                                ),
                        LibroSegunReglasCompleto(
                                1, 1, "Libro más específico",
                                LibroDePrecios(
                                        1,
                                        1,
                                        "Libro Precios 1",
                                        setOf(
                                                PrecioEnLibro(Precio(Decimal(789), ID_UNICO_IMPUESTO), 1),
                                                PrecioEnLibro(Precio(Decimal(987), ID_UNICO_IMPUESTO), 2)
                                             )
                                              ),
                                hashSetOf(reglaUbicacion),
                                hashSetOf(reglaGrupo),
                                hashSetOf(reglaPaquete)
                                                )

                          )

        @Test
        fun si_no_hay_libros_retorna_el_precio_por_defecto()
        {
            val proveedorEnPrueba =
                    ProveedorDePreciosCompletosYProhibicionesEnMemoria(
                            sequenceOf(), fondosDisponibles, impuestosDisponibles
                                                                      )

            val impuesto = impuestosDisponibles.first()
            val preciosCompletosEsperados =
                    fondosDisponibles.map {
                        PrecioCompleto(it.precioPorDefecto, ImpuestoSoloTasa(impuesto))
                    }.toList()

            val preciosCalculados =
                    proveedorEnPrueba.darPreciosCompletosDeFondos(
                            LinkedHashSet(fondosDisponibles.map { it.id!! }.toList()),
                            ID_UNICA_UBICACION,
                            ID_UNICO_GRUPO_CLIENTES,
                            ID_PAQUETE_PRINCIPAL
                                                                 )

            assertEquals(preciosCompletosEsperados, preciosCalculados)
        }

        @Test
        fun si_no_se_definen_reglas_retorna_el_precio_por_defecto()
        {
            val proveedorEnPrueba =
                    ProveedorDePreciosCompletosYProhibicionesEnMemoria(
                            libroReglasSoloPrecios, fondosDisponibles, impuestosDisponibles
                                                                      )

            val impuesto = impuestosDisponibles.first()
            val preciosCompletosEsperados =
                    fondosDisponibles.map {
                        PrecioCompleto(it.precioPorDefecto, ImpuestoSoloTasa(impuesto))
                    }.toList()

            val preciosCalculados =
                    proveedorEnPrueba.darPreciosCompletosDeFondos(
                            LinkedHashSet(fondosDisponibles.map { it.id!! }.toList()),
                            null,
                            null,
                            null
                                                                 )

            assertEquals(preciosCompletosEsperados, preciosCalculados)
        }

        @Test
        fun si_se_definen_reglas_retorna_el_precio_del_libro_mas_especifico()
        {
            val proveedorEnPrueba =
                    ProveedorDePreciosCompletosYProhibicionesEnMemoria(
                            libroReglasSoloPrecios, fondosDisponibles, impuestosDisponibles
                                                                      )

            val impuesto = impuestosDisponibles.first()
            val preciosCompletosEsperados =
                    listOf(
                            PrecioCompleto(Precio(Decimal(789), impuesto.id!!), ImpuestoSoloTasa(impuesto)),
                            PrecioCompleto(Precio(Decimal(987), impuesto.id!!), ImpuestoSoloTasa(impuesto))
                          )

            val preciosCalculados =
                    proveedorEnPrueba.darPreciosCompletosDeFondos(
                            LinkedHashSet(fondosDisponibles.map { it.id!! }.toList()),
                            ID_UNICA_UBICACION,
                            ID_UNICO_GRUPO_CLIENTES,
                            ID_PAQUETE_PRINCIPAL
                                                                 )

            assertEquals(preciosCompletosEsperados, preciosCalculados)
        }
    }

    @Nested
    inner class BuscarReglasQueAplican
    {
        private val libroReglasSoloPrecios =
                sequenceOf(
                        LibroSegunReglasCompleto(
                                1, 1, "Libro menos específico",
                                LibroDePrecios(
                                        1,
                                        1,
                                        "Libro Precios 2",
                                        setOf(
                                                PrecioEnLibro(Precio(Decimal(123), ID_UNICO_IMPUESTO), 1),
                                                PrecioEnLibro(Precio(Decimal(321), ID_UNICO_IMPUESTO), 2)
                                             )
                                              ),
                                hashSetOf(reglaUbicacion),
                                hashSetOf(),
                                hashSetOf()
                                                ),
                        LibroSegunReglasCompleto(
                                1, 1, "Libro más específico",
                                LibroDePrecios(
                                        1,
                                        1,
                                        "Libro Precios 1",
                                        setOf(
                                                PrecioEnLibro(Precio(Decimal(789), ID_UNICO_IMPUESTO), 1),
                                                PrecioEnLibro(Precio(Decimal(987), ID_UNICO_IMPUESTO), 2)
                                             )
                                              ),
                                hashSetOf(reglaUbicacion),
                                hashSetOf(reglaGrupo),
                                hashSetOf(reglaPaquete)
                                                )

                          )

        @Test
        fun si_no_hay_libros_retorna_un_conjunto_vacio()
        {
            val proveedorEnPrueba =
                    ProveedorDePreciosCompletosYProhibicionesEnMemoria(
                            sequenceOf(),
                            fondosDisponibles,
                            impuestosDisponibles
                                                                      )

            val reglasEncontradas =
                    proveedorEnPrueba.buscarReglasQueDeterminanPrecio(
                            fondosDisponibles.first().id!!,
                            ID_UNICA_UBICACION,
                            ID_UNICO_GRUPO_CLIENTES,
                            ID_PAQUETE_PRINCIPAL
                                                                     )

            assertEquals(setOf(), reglasEncontradas)
        }

        @Test
        fun si_no_se_definen_reglas_retorna_un_conjunto_vacio()
        {
            val proveedorEnPrueba =
                    ProveedorDePreciosCompletosYProhibicionesEnMemoria(
                            libroReglasSoloPrecios, fondosDisponibles, impuestosDisponibles
                                                                      )

            val reglasEncontradas =
                    proveedorEnPrueba.buscarReglasQueDeterminanPrecio(fondosDisponibles.first().id!!, null, null, null)

            assertEquals(setOf(), reglasEncontradas)
        }

        @Test
        fun si_se_definen_reglas_retorna_el_precio_del_libro_mas_especifico()
        {
            val proveedorEnPrueba =
                    ProveedorDePreciosCompletosYProhibicionesEnMemoria(
                            libroReglasSoloPrecios, fondosDisponibles, impuestosDisponibles
                                                                      )

            val reglasEsperadas = setOf(reglaUbicacion, reglaGrupo, reglaPaquete)

            val reglasEncontradas =
                    proveedorEnPrueba.buscarReglasQueDeterminanPrecio(
                            fondosDisponibles.first().id!!,
                            ID_UNICA_UBICACION,
                            ID_UNICO_GRUPO_CLIENTES,
                            ID_PAQUETE_PRINCIPAL
                                                                     )

            assertEquals(reglasEsperadas, reglasEncontradas)
        }
    }

    @Nested
    inner class VerificarSiFondoEsVendible
    {
        @Nested
        inner class SinReglaDeIdPaquete
        {
            private val idFondoProhibido = fondosDisponibles.first().id!!
            private val librosProhibiendoUbicacionYGrupoDeClientes =
                    sequenceOf(
                            LibroSegunReglasCompleto(
                                    1, 1, "Libro con regla sobre ubicación y grupo",
                                    LibroDeProhibiciones(
                                            1,
                                            1,
                                            "Libro Prohibiciones",
                                            setOf(Prohibicion.DeFondo(idFondoProhibido)),
                                            setOf()
                                                        ),
                                    hashSetOf(reglaUbicacion),
                                    hashSetOf(reglaGrupo),
                                    hashSetOf()
                                                    )
                              )


            @Test
            fun si_no_hay_libros_retorna_true()
            {
                val proveedorEnPrueba =
                        ProveedorDePreciosCompletosYProhibicionesEnMemoria(
                                sequenceOf(), fondosDisponibles, impuestosDisponibles
                                                                          )

                val sePuedeVender =
                        proveedorEnPrueba.verificarSiFondoEsVendible(
                                fondosDisponibles.first().id!!,
                                ID_UNICA_UBICACION,
                                ID_UNICO_GRUPO_CLIENTES,
                                setOf()
                                                                    )

                assertTrue(sePuedeVender)
            }

            @Test
            fun si_un_libro_de_prohibiciones_aplica_y_tiene_prohibicion_al_fondo_entonces_retorna_false()
            {
                val proveedorEnPrueba =
                        ProveedorDePreciosCompletosYProhibicionesEnMemoria(
                                librosProhibiendoUbicacionYGrupoDeClientes, fondosDisponibles, impuestosDisponibles
                                                                          )

                val sePuedeVender =
                        proveedorEnPrueba.verificarSiFondoEsVendible(
                                idFondoProhibido,
                                ID_UNICA_UBICACION,
                                ID_UNICO_GRUPO_CLIENTES,
                                setOf()
                                                                    )

                assertFalse(sePuedeVender)
            }

            @Test
            fun si_un_libro_de_prohibiciones_aplica_y_no_tiene_prohibicion_al_fondo_entonces_retorna_true()
            {
                val proveedorEnPrueba =
                        ProveedorDePreciosCompletosYProhibicionesEnMemoria(
                                librosProhibiendoUbicacionYGrupoDeClientes, fondosDisponibles, impuestosDisponibles
                                                                          )

                val sePuedeVender =
                        proveedorEnPrueba.verificarSiFondoEsVendible(
                                fondosDisponibles.last().id!!,
                                ID_UNICA_UBICACION,
                                ID_UNICO_GRUPO_CLIENTES,
                                setOf()
                                                                    )

                assertTrue(sePuedeVender)
            }

            @Test
            fun si_se_verifica_una_prohibicion_usando_ambas_reglas_pero_solo_hay_libros_con_la_disyuncion_de_las_reglas_retorna_false()
            {
                val librosProhibiendoOUbicacionOGrupoDeClientes =
                        sequenceOf(
                                LibroSegunReglasCompleto(
                                        1, 1, "Libro con regla sobre ubicación",
                                        LibroDeProhibiciones(
                                                1,
                                                1,
                                                "Libro Prohibiciones",
                                                setOf(Prohibicion.DeFondo(idFondoProhibido)),
                                                setOf()
                                                            ),
                                        hashSetOf(reglaUbicacion),
                                        hashSetOf(),
                                        hashSetOf()
                                                        ),
                                LibroSegunReglasCompleto(
                                        1, 1, "Libro con regla sobre grupo",
                                        LibroDeProhibiciones(
                                                1,
                                                1,
                                                "Libro Prohibiciones",
                                                setOf(Prohibicion.DeFondo(idFondoProhibido)),
                                                setOf()
                                                            ),
                                        hashSetOf(),
                                        hashSetOf(reglaGrupo),
                                        hashSetOf()
                                                        )
                                  )

                val proveedorEnPrueba =
                        ProveedorDePreciosCompletosYProhibicionesEnMemoria(
                                librosProhibiendoOUbicacionOGrupoDeClientes, fondosDisponibles, impuestosDisponibles
                                                                          )

                val sePuedeVender =
                        proveedorEnPrueba.verificarSiFondoEsVendible(
                                idFondoProhibido,
                                ID_UNICA_UBICACION,
                                ID_UNICO_GRUPO_CLIENTES,
                                setOf()
                                                                    )

                assertFalse(sePuedeVender)
            }
        }

        @Nested
        inner class ConReglaDeIdPaquete
        {
            private val libroConReglaPaqueteSoloProhibicionEnFondo =
                    sequenceOf(
                            LibroSegunReglasCompleto(
                                    1, 1, "Libro con prohibción de paquete",
                                    LibroDeProhibiciones(
                                            1,
                                            1,
                                            "Libro Prohibiciones",
                                            setOf(Prohibicion.DeFondo(1L)),
                                            setOf()
                                                        ),
                                    hashSetOf(reglaUbicacion),
                                    hashSetOf(reglaGrupo),
                                    hashSetOf(reglaPaquete)
                                                    )
                              )

            @Test
            fun si_para_un_paquete_en_el_carrito_se_prohibe_la_venta_en_un_libro_que_aplica_con_el_fondo_a_verificar_retorna_false()
            {
                val proveedorEnPrueba =
                        ProveedorDePreciosCompletosYProhibicionesEnMemoria(
                                libroConReglaPaqueteSoloProhibicionEnFondo, fondosDisponibles, impuestosDisponibles
                                                                          )

                val sePuedeVenderSiElPaqueteNoEstaEnElCarrito =
                        proveedorEnPrueba.verificarSiFondoEsVendible(
                                fondosDisponibles.first().id!!,
                                ID_UNICA_UBICACION,
                                ID_UNICO_GRUPO_CLIENTES,
                                setOf()
                                                                    )

                val noSePuedeVenderSiElPaqueteEstaEnElCarrito =
                        proveedorEnPrueba.verificarSiFondoEsVendible(
                                fondosDisponibles.first().id!!,
                                ID_UNICA_UBICACION,
                                ID_UNICO_GRUPO_CLIENTES,
                                setOf(reglaPaquete.restriccion)
                                                                    )

                assertTrue(sePuedeVenderSiElPaqueteNoEstaEnElCarrito)
                assertFalse(noSePuedeVenderSiElPaqueteEstaEnElCarrito)
            }

            @Test
            fun si_para_un_paquete_en_el_carrito_se_prohibe_la_venta_en_un_libro_que_aplica_pero_no_tiene_prohibiciones_con_el_fondo_a_verificar_retorna_true()
            {
                val proveedorEnPrueba =
                        ProveedorDePreciosCompletosYProhibicionesEnMemoria(
                                libroConReglaPaqueteSoloProhibicionEnFondo, fondosDisponibles, impuestosDisponibles
                                                                          )

                val sePuedeVenderSiElPaqueteNoEstaEnElCarrito =
                        proveedorEnPrueba.verificarSiFondoEsVendible(
                                fondosDisponibles.last().id!!,
                                ID_UNICA_UBICACION,
                                ID_UNICO_GRUPO_CLIENTES,
                                setOf()
                                                                    )

                val noSePuedeVenderSiElPaqueteEstaEnElCarrito =
                        proveedorEnPrueba.verificarSiFondoEsVendible(
                                fondosDisponibles.last().id!!,
                                ID_UNICA_UBICACION,
                                ID_UNICO_GRUPO_CLIENTES,
                                setOf(reglaPaquete.restriccion)
                                                                    )

                assertTrue(sePuedeVenderSiElPaqueteNoEstaEnElCarrito)
                assertTrue(noSePuedeVenderSiElPaqueteEstaEnElCarrito)
            }
        }
    }

    @Nested
    inner class VerificarSiPaqueteEsVendible
    {
        @Nested
        inner class SinReglaDeIdPaquete
        {
            private val librosProhibiendoUbicacionYGrupoDeClientes =
                    sequenceOf(
                            LibroSegunReglasCompleto(
                                    1, 1, "Libro con regla sobre ubicación y grupo",
                                    LibroDeProhibiciones(
                                            1,
                                            1,
                                            "Libro Prohibiciones",
                                            setOf(),
                                            setOf(Prohibicion.DePaquete(ID_PAQUETE_PRINCIPAL))
                                                        ),
                                    hashSetOf(reglaUbicacion),
                                    hashSetOf(reglaGrupo),
                                    hashSetOf()
                                                    )
                              )

            @Test
            fun si_no_hay_libros_retorna_true()
            {
                val proveedorEnPrueba =
                        ProveedorDePreciosCompletosYProhibicionesEnMemoria(
                                sequenceOf(), fondosDisponibles, impuestosDisponibles
                                                                          )

                val sePuedeVender =
                        proveedorEnPrueba.verificarSiPaqueteEsVendible(
                                fondosDisponibles.first().id!!,
                                ID_UNICA_UBICACION,
                                ID_UNICO_GRUPO_CLIENTES,
                                setOf(),
                                setOf()
                                                                      )

                assertTrue(sePuedeVender)
            }

            @Test
            fun si_un_libro_de_prohibiciones_aplica_y_no_tiene_prohibicion_al_paquete_entonces_retorna_true()
            {
                val proveedorEnPrueba =
                        ProveedorDePreciosCompletosYProhibicionesEnMemoria(
                                librosProhibiendoUbicacionYGrupoDeClientes, fondosDisponibles, impuestosDisponibles
                                                                          )

                val sePuedeVender =
                        proveedorEnPrueba.verificarSiPaqueteEsVendible(
                                ID_PAQUETE_SECUNDARIO,
                                ID_UNICA_UBICACION,
                                ID_UNICO_GRUPO_CLIENTES,
                                setOf(),
                                setOf()
                                                                      )

                assertTrue(sePuedeVender)
            }

            @Test
            fun si_un_libro_de_prohibiciones_aplica_y_tiene_prohibicion_al_paquete_entonces_retorna_false()
            {
                val proveedorEnPrueba =
                        ProveedorDePreciosCompletosYProhibicionesEnMemoria(
                                librosProhibiendoUbicacionYGrupoDeClientes, fondosDisponibles, impuestosDisponibles
                                                                          )

                val sePuedeVender =
                        proveedorEnPrueba.verificarSiPaqueteEsVendible(
                                ID_PAQUETE_PRINCIPAL,
                                ID_UNICA_UBICACION,
                                ID_UNICO_GRUPO_CLIENTES,
                                setOf(),
                                setOf()
                                                                      )

                assertFalse(sePuedeVender)
            }

            @Test
            fun si_se_verifica_una_prohibicion_usando_ambas_reglas_pero_solo_hay_libros_con_la_disyuncion_de_las_reglas_retorna_false()
            {
                val librosProhibiendoOUbicacionOGrupoDeClientes =
                        sequenceOf(
                                LibroSegunReglasCompleto(
                                        1, 1, "Libro con regla sobre ubicación",
                                        LibroDeProhibiciones(
                                                1,
                                                1,
                                                "Libro Prohibiciones",
                                                setOf(),
                                                setOf(Prohibicion.DePaquete(ID_PAQUETE_PRINCIPAL))
                                                            ),
                                        hashSetOf(reglaUbicacion),
                                        hashSetOf(),
                                        hashSetOf()
                                                        ),
                                LibroSegunReglasCompleto(
                                        1, 1, "Libro con regla sobre grupo",
                                        LibroDeProhibiciones(
                                                1,
                                                1,
                                                "Libro Prohibiciones",
                                                setOf(),
                                                setOf(Prohibicion.DePaquete(ID_PAQUETE_PRINCIPAL))
                                                            ),
                                        hashSetOf(),
                                        hashSetOf(reglaGrupo),
                                        hashSetOf()
                                                        )
                                  )

                val proveedorEnPrueba =
                        ProveedorDePreciosCompletosYProhibicionesEnMemoria(
                                librosProhibiendoOUbicacionOGrupoDeClientes, fondosDisponibles, impuestosDisponibles
                                                                          )

                val sePuedeVender =
                        proveedorEnPrueba.verificarSiPaqueteEsVendible(
                                ID_PAQUETE_PRINCIPAL,
                                ID_UNICA_UBICACION,
                                ID_UNICO_GRUPO_CLIENTES,
                                setOf(),
                                setOf()
                                                                      )

                assertFalse(sePuedeVender)
            }
        }

        @Nested
        inner class ConReglaDeIdPaquete
        {
            private val ID_FONDO_PROHIBIDO = fondosDisponibles.sumBy { it.id!!.toInt() }.toLong()
            private val libroConReglaPaqueteSoloProhibicionEnFondo =
                    sequenceOf(
                            LibroSegunReglasCompleto(
                                    1, 1, "Libro con prohibción de paquete",
                                    LibroDeProhibiciones(
                                            1,
                                            1,
                                            "Libro Prohibiciones",
                                            setOf(Prohibicion.DeFondo(ID_FONDO_PROHIBIDO)),
                                            setOf(Prohibicion.DePaquete(ID_PAQUETE_SECUNDARIO))
                                                        ),
                                    hashSetOf(reglaUbicacion),
                                    hashSetOf(reglaGrupo),
                                    hashSetOf(reglaPaquete)
                                                    )
                              )

            @Test
            fun si_un_paquete_en_el_carrito_es_regla_y_se_verifica_un_paquete_que_prohibe_esa_regla_retorna_false()
            {
                val proveedorEnPrueba =
                        ProveedorDePreciosCompletosYProhibicionesEnMemoria(
                                libroConReglaPaqueteSoloProhibicionEnFondo, fondosDisponibles, impuestosDisponibles
                                                                          )

                val sePuedeVenderSiElPaqueteNoEstaEnElCarrito =
                        proveedorEnPrueba.verificarSiPaqueteEsVendible(
                                ID_PAQUETE_SECUNDARIO,
                                ID_UNICA_UBICACION,
                                ID_UNICO_GRUPO_CLIENTES,
                                setOf(),
                                setOf()
                                                                      )

                val noSePuedeVenderSiElPaqueteEstaEnElCarrito =
                        proveedorEnPrueba.verificarSiPaqueteEsVendible(
                                ID_PAQUETE_SECUNDARIO,
                                ID_UNICA_UBICACION,
                                ID_UNICO_GRUPO_CLIENTES,
                                setOf(),
                                setOf(reglaPaquete.restriccion)
                                                                      )

                assertTrue(sePuedeVenderSiElPaqueteNoEstaEnElCarrito)
                assertFalse(noSePuedeVenderSiElPaqueteEstaEnElCarrito)
            }

            @Test
            fun si_un_paquete_en_el_carrito_es_regla_y_se_verifica_un_paquete_que_no_prohibido_esa_regla_retorna_true()
            {
                val proveedorEnPrueba =
                        ProveedorDePreciosCompletosYProhibicionesEnMemoria(
                                libroConReglaPaqueteSoloProhibicionEnFondo, fondosDisponibles, impuestosDisponibles
                                                                          )

                val sePuedeVenderSiElPaqueteNoEstaEnElCarrito =
                        proveedorEnPrueba.verificarSiFondoEsVendible(
                                ID_PAQUETE_TERCIARIO,
                                ID_UNICA_UBICACION,
                                ID_UNICO_GRUPO_CLIENTES,
                                setOf()
                                                                    )

                val noSePuedeVenderSiElPaqueteEstaEnElCarrito =
                        proveedorEnPrueba.verificarSiFondoEsVendible(
                                ID_PAQUETE_TERCIARIO,
                                ID_UNICA_UBICACION,
                                ID_UNICO_GRUPO_CLIENTES,
                                setOf(reglaPaquete.restriccion)
                                                                    )

                assertTrue(sePuedeVenderSiElPaqueteNoEstaEnElCarrito)
                assertTrue(noSePuedeVenderSiElPaqueteEstaEnElCarrito)
            }

            @Test
            fun si_para_un_fondo_en_el_carrito_se_prohibe_la_venta_en_un_libro_que_aplica_con_el_paquete_a_verificar_retorna_false()
            {
                val proveedorEnPrueba =
                        ProveedorDePreciosCompletosYProhibicionesEnMemoria(
                                libroConReglaPaqueteSoloProhibicionEnFondo, fondosDisponibles, impuestosDisponibles
                                                                          )

                val sePuedeVenderSiElFondoNoEstaEnElCarrito =
                        proveedorEnPrueba.verificarSiPaqueteEsVendible(
                                reglaPaquete.restriccion,
                                ID_UNICA_UBICACION,
                                ID_UNICO_GRUPO_CLIENTES,
                                setOf(),
                                setOf()
                                                                      )

                val noSePuedeVenderSiElFondoEstaEnElCarrito =
                        proveedorEnPrueba.verificarSiPaqueteEsVendible(
                                reglaPaquete.restriccion,
                                ID_UNICA_UBICACION,
                                ID_UNICO_GRUPO_CLIENTES,
                                setOf(ID_FONDO_PROHIBIDO),
                                setOf()
                                                                      )

                assertTrue(sePuedeVenderSiElFondoNoEstaEnElCarrito)
                assertFalse(noSePuedeVenderSiElFondoEstaEnElCarrito)
            }

            @Test
            fun si_para_un_fondo_en_el_carrito_se_prohibe_la_venta_en_un_libro_que_aplica_pero_no_tiene_prohibiciones_con_el_paquete_a_verificar_retorna_true()
            {
                val proveedorEnPrueba =
                        ProveedorDePreciosCompletosYProhibicionesEnMemoria(
                                libroConReglaPaqueteSoloProhibicionEnFondo, fondosDisponibles, impuestosDisponibles
                                                                          )

                val sePuedeVenderSiElPaqueteNoEstaEnElCarrito =
                        proveedorEnPrueba.verificarSiFondoEsVendible(
                                ID_PAQUETE_TERCIARIO,
                                ID_UNICA_UBICACION,
                                ID_UNICO_GRUPO_CLIENTES,
                                setOf()
                                                                    )

                val noSePuedeVenderSiElPaqueteEstaEnElCarrito =
                        proveedorEnPrueba.verificarSiFondoEsVendible(
                                ID_PAQUETE_TERCIARIO,
                                ID_UNICA_UBICACION,
                                ID_UNICO_GRUPO_CLIENTES,
                                setOf()
                                                                    )

                assertTrue(sePuedeVenderSiElPaqueteNoEstaEnElCarrito)
                assertTrue(noSePuedeVenderSiElPaqueteEstaEnElCarrito)
            }

            @Test
            fun si_un_paquete_en_el_carrito_es_prohibido_por_el_paquete_regla_a_verificar_retorna_false()
            {
                val proveedorEnPrueba =
                        ProveedorDePreciosCompletosYProhibicionesEnMemoria(
                                libroConReglaPaqueteSoloProhibicionEnFondo, fondosDisponibles, impuestosDisponibles
                                                                          )

                val sePuedeVenderSiElPaqueteNoEstaEnElCarrito =
                        proveedorEnPrueba.verificarSiPaqueteEsVendible(
                                reglaPaquete.restriccion,
                                ID_UNICA_UBICACION,
                                ID_UNICO_GRUPO_CLIENTES,
                                setOf(),
                                setOf()
                                                                      )

                val noSePuedeVenderSiElPaqueteEstaEnElCarrito =
                        proveedorEnPrueba.verificarSiPaqueteEsVendible(
                                reglaPaquete.restriccion,
                                ID_UNICA_UBICACION,
                                ID_UNICO_GRUPO_CLIENTES,
                                setOf(),
                                setOf(ID_PAQUETE_SECUNDARIO)
                                                                      )

                assertTrue(sePuedeVenderSiElPaqueteNoEstaEnElCarrito)
                assertFalse(noSePuedeVenderSiElPaqueteEstaEnElCarrito)
            }

            @Test
            fun si_un_paquete_en_el_carrito_no_es_prohibido_por_el_paquete_regla_a_verificar_retorna_true()
            {
                val proveedorEnPrueba =
                        ProveedorDePreciosCompletosYProhibicionesEnMemoria(
                                libroConReglaPaqueteSoloProhibicionEnFondo, fondosDisponibles, impuestosDisponibles
                                                                          )

                val sePuedeVenderSiElPaqueteNoEstaEnElCarrito =
                        proveedorEnPrueba.verificarSiFondoEsVendible(
                                reglaPaquete.restriccion,
                                ID_UNICA_UBICACION,
                                ID_UNICO_GRUPO_CLIENTES,
                                setOf()
                                                                    )

                val noSePuedeVenderSiElPaqueteEstaEnElCarrito =
                        proveedorEnPrueba.verificarSiFondoEsVendible(
                                reglaPaquete.restriccion,
                                ID_UNICA_UBICACION,
                                ID_UNICO_GRUPO_CLIENTES,
                                setOf(ID_PAQUETE_TERCIARIO)
                                                                    )

                assertTrue(sePuedeVenderSiElPaqueteNoEstaEnElCarrito)
                assertTrue(noSePuedeVenderSiElPaqueteEstaEnElCarrito)
            }
        }
    }
}