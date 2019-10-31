package co.smartobjects.persistencia.usuariosglobales

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.usuariosglobales.UsuarioGlobal
import co.smartobjects.persistencia.EntidadDAOBasePruebas
import co.smartobjects.persistencia.TestConMultiplesDAO
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorio
import co.smartobjects.persistencia.desactivarUsuario
import co.smartobjects.persistencia.excepciones.EntidadNoExiste
import co.smartobjects.persistencia.excepciones.ErrorCreacionActualizacionPorDuplicidad
import co.smartobjects.persistencia.usuarios.Hasher
import org.junit.jupiter.api.*
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DisplayName("UsuarioGlobalDAO")
internal class UsuarioGlobalDAOPruebas : EntidadDAOBasePruebas(false)
{
    companion object
    {
        private const val hashPruebasCreacion = "\$shiro1\$SHA-512\$500000\$Atk2TMaxtKSRMTLMay+kut9lnG5QfiEzQkMP1MtL8+Y=\$l4OQPChonLAun1+0t+yGsY9VAiESaOEGRVnODk+g9pdWr+wRftRa0lA3MyYJD8NPaqTB6pgjy7w9ZdNOhDVxHg=="
        private const val hashPruebasActualizacion = "\$shiro1\$SHA-512\$500000\$M5g1wTYxkEvUJYXMDW2+PyW8CYQNcb8HvwjGKWEzEKs=\$qM3AYdOeWOB+OKnvxqazd01QpIJvuKVo6GNLk8R5CRYK7uBdcBws7C+ejgHOq37eJePypMqs77art7GrULiFkA=="
        private val contraseñaPruebasCreacion = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
        private val contraseñaPruebasActualizacion = charArrayOf('o', 't', 'r', 'o', 'p', 'a', 's', 's', 'w', 'o', 'r', 'd', '2', '_')
    }

    private val hasherDummy = object : Hasher
    {
        override fun calcularHash(entrada: CharArray): String
        {
            return when
            {
                Arrays.equals(contraseñaPruebasCreacion, entrada)      -> hashPruebasCreacion
                Arrays.equals(contraseñaPruebasActualizacion, entrada) -> hashPruebasActualizacion
                else                                                   -> throw RuntimeException("Contraseña ${String(entrada)} no soportada por hasher de pruebas")
            }
        }
    }
    private val repositorio: RepositorioUsuariosGlobales by lazy { RepositorioUsuariosGlobalesSQL(configuracionRepositorios, hasherDummy) }
    private val repositorioCredenciales: RepositorioCredencialesGuardadasUsuarioGlobal by lazy { RepositorioCredencialesGuardadasUsuarioGlobalSQL(configuracionRepositorios) }

    override val creadoresRepositoriosUsados: List<CreadorRepositorio<*>> = listOf()


    private fun darInstanciaEntidadValida(): UsuarioGlobal
    {
        return UsuarioGlobal(
                UsuarioGlobal.DatosUsuario(
                        "El Usuario",
                        "El nombre completo",
                        "elemail@mail.com",
                        true
                                          )
                            )
    }

    private fun darInstanciaEntidadCreacionValida(usuario: UsuarioGlobal = darInstanciaEntidadValida()): UsuarioGlobal.UsuarioParaCreacion
    {
        return UsuarioGlobal.UsuarioParaCreacion(
                usuario.datosUsuario,
                contraseñaPruebasCreacion.copyOf()
                                                )
    }

    @BeforeEach
    fun crearTablaUsuariosGlobal()
    {
        repositorio.inicializar()
    }

    @AfterEach
    fun eliminarTablaUsuariosGlobal()
    {
        repositorio.limpiar()
    }


    @TestConMultiplesDAO
    fun si_existe_la_tabla_al_inicializar_retorna_true()
    {
        assertTrue(repositorio.inicializar())
    }


    @Nested
    inner class Crear
    {
        @TestConMultiplesDAO
        fun retorna_entidad_correcta()
        {
            val entidadAInsertar = darInstanciaEntidadCreacionValida()
            val entidadEspeara = darInstanciaEntidadValida()
            val entidadCreada = repositorio.crear(entidadAInsertar)

            assertEquals(entidadEspeara, entidadCreada)
        }

        @TestConMultiplesDAO
        fun crea_la_entidad_como_activa()
        {
            val entidadEsperada = darInstanciaEntidadValida()
            val entidadAInsertar = darInstanciaEntidadCreacionValida(
                    entidadEsperada.let {
                        it.copiar(datosUsuario = it.datosUsuario.copiar(activo = false))
                    }
                                                                    )
            val entidadCreada = repositorio.crear(entidadAInsertar)

            assertEquals(entidadEsperada, entidadCreada)
        }

        @TestConMultiplesDAO
        fun limpia_contraseña_de_entidad_de_entrada()
        {
            val entidadAInsertar = darInstanciaEntidadCreacionValida()
            repositorio.crear(entidadAInsertar)

            assertTrue(entidadAInsertar.contraseña.all { it == '\u0000' })
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_con_ErrorCreacionActualizacionPorDuplicidad_si_se_usa_usuario_duplicado()
        {
            val entidadAInsertar = darInstanciaEntidadCreacionValida()

            repositorio.crear(entidadAInsertar)

            assertThrows<ErrorCreacionActualizacionPorDuplicidad> {
                repositorio.crear(
                        darInstanciaEntidadCreacionValida()
                            .copiar(datosUsuario = entidadAInsertar.datosUsuario.copiar(email = "otro@email.com"))
                                 )
            }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_con_ErrorCreacionActualizacionPorDuplicidad_si_se_usa_email_duplicado()
        {
            val entidadAInsertar = darInstanciaEntidadCreacionValida()

            repositorio.crear(entidadAInsertar)

            assertThrows<ErrorCreacionActualizacionPorDuplicidad> {
                repositorio.crear(
                        darInstanciaEntidadCreacionValida()
                            .copiar(datosUsuario = entidadAInsertar.datosUsuario.copiar(usuario = "otro_usuario"))
                                 )
            }
        }
    }

    @Nested
    inner class Consultar
    {
        @TestConMultiplesDAO
        fun por_id_existente_retorna_entidad_correcta()
        {
            val entidadPrueba = repositorio.crear(darInstanciaEntidadCreacionValida())

            val entidadConsultada = repositorio.buscarPorId(entidadPrueba.datosUsuario.usuario)

            assertEquals(entidadPrueba, entidadConsultada)
        }

        @TestConMultiplesDAO
        fun listado_de_entidades_sin_entidades_retorna_lista_vacia()
        {
            val listadoConsultado = repositorio.listar()

            assertTrue(listadoConsultado.none())
        }

        @TestConMultiplesDAO
        fun listado_de_entidades_retorna_entidades_correctas()
        {
            val entidadesPrueba = (1..3).map {
                val entidad = darInstanciaEntidadCreacionValida()
                repositorio.crear(
                        entidad.copiar(datosUsuario = entidad.datosUsuario.copiar(usuario = "Usuario $it", email = "email_$it@email.com"))
                                 )
            }

            val listadoEsperado = entidadesPrueba.toMutableList().sortedBy { it.datosUsuario.usuario }


            val listadoConsultado = repositorio.listar().toList()

            assertEquals(listadoEsperado, listadoConsultado.sortedBy { it.datosUsuario.usuario })
        }

        @TestConMultiplesDAO
        fun por_id_no_existente_retorna_null()
        {
            val entidadConsultada = repositorio.buscarPorId("Usuario inexistente")

            assertNull(entidadConsultada)
        }
    }

    @Nested
    inner class ConsultarCredenciales
    {
        @TestConMultiplesDAO
        fun por_id_existente_retorna_entidad_correcta()
        {
            val entidadPrueba = repositorio.crear(darInstanciaEntidadCreacionValida())
            val entidadEsperada = UsuarioGlobal.CredencialesGuardadas(entidadPrueba, hashPruebasCreacion)
            val entidadConsultada = repositorioCredenciales.buscarPorId(entidadPrueba.datosUsuario.usuario)

            assertEquals(entidadEsperada, entidadConsultada)
        }

        @TestConMultiplesDAO
        fun por_id_existente_pero_usuario_desactivado_retorna_null()
        {
            val entidadPrueba = repositorio.crear(darInstanciaEntidadCreacionValida())

            val campoActivoComoFalse = UsuarioGlobal.DatosUsuario.CampoActivo(false)
            repositorio.actualizarCamposIndividuales(
                    entidadPrueba.datosUsuario.usuario,
                    mapOf(campoActivoComoFalse.nombreCampo to campoActivoComoFalse)
                                                    )

            val entidadConsultada = repositorioCredenciales.buscarPorId(entidadPrueba.datosUsuario.usuario)

            assertNull(entidadConsultada)
        }

        @TestConMultiplesDAO
        fun por_id_no_existente_retorna_null()
        {
            val entidadConsultada = repositorioCredenciales.buscarPorId("Usuario no existente")

            assertNull(entidadConsultada)
        }
    }

    @Nested
    inner class Actualizar
    {
        private lateinit var entidadDePrueba: UsuarioGlobal
        private lateinit var entidadDePruebaParaActualizacion: UsuarioGlobal.UsuarioParaCreacion

        @BeforeEach
        fun crearEntidadDePrueba()
        {
            entidadDePruebaParaActualizacion = darInstanciaEntidadCreacionValida()
            entidadDePrueba = repositorio.crear(entidadDePruebaParaActualizacion)
        }

        @TestConMultiplesDAO
        fun se_actualizan_todos_los_campos_correctamente_y_credenciales_se_actualizan_correctamente()
        {
            val entidadModificada = UsuarioGlobal(
                    entidadDePrueba.datosUsuario.copiar(
                            entidadDePrueba.datosUsuario.usuario,
                            "Otro nombre largo",
                            "otroemail@pruebas.algo"
                                                       )
                                                 )
            val entidadModificadaParaActualizacion = UsuarioGlobal.UsuarioParaCreacion(
                    entidadModificada.datosUsuario,
                    contraseñaPruebasActualizacion.copyOf()
                                                                                      )
            val entidadActualizada = repositorio.actualizar(entidadModificadaParaActualizacion.datosUsuario.usuario, entidadModificadaParaActualizacion)
            assertEquals(entidadModificada, entidadActualizada)

            // Verificar credenciales
            val credencialesEsperadas = UsuarioGlobal.CredencialesGuardadas(entidadModificada, hashPruebasActualizacion)
            val credencialesConsultadas = repositorioCredenciales.buscarPorId(entidadModificada.datosUsuario.usuario)
            assertEquals(credencialesEsperadas, credencialesConsultadas)
        }

        @TestConMultiplesDAO
        fun limpia_contraseña_de_entidad_de_entrada()
        {
            val entidadConCambios = darInstanciaEntidadCreacionValida()
            repositorio.actualizar(entidadConCambios.datosUsuario.usuario, entidadConCambios)

            assertTrue(entidadConCambios.contraseña.all { it == '\u0000' })
        }

        @TestConMultiplesDAO
        fun con_id_inexistente_lanza_excepcion_EntidadNoExiste()
        {
            val entidadConCambios = darInstanciaEntidadCreacionValida().copiar(entidadDePrueba.datosUsuario.copiar(usuario = "Usuario inexistente"))

            assertThrows<EntidadNoExiste> {
                repositorio.actualizar(entidadConCambios.datosUsuario.usuario, entidadConCambios)
            }
        }

        @TestConMultiplesDAO
        fun con_email_duplicado_lanza_excepcion_ErrorCreacionActualizacionPorDuplicidad()
        {
            val entidadACrear =
                    darInstanciaEntidadCreacionValida()
                        .copiar(datosUsuario = entidadDePrueba.datosUsuario.copiar(usuario = "Otro usuario", email = "otroemail@pruebas.net"))

            repositorio.crear(entidadACrear)

            assertThrows<ErrorCreacionActualizacionPorDuplicidad> {
                repositorio.actualizar(
                        entidadACrear.datosUsuario.usuario,
                        darInstanciaEntidadCreacionValida()
                            .copiar(datosUsuario = entidadACrear.datosUsuario.copiar(email = entidadDePrueba.datosUsuario.email))
                                      )
            }
        }
    }

    @Nested
    inner class ActualizarParcialmente
    {
        private lateinit var entidadDePrueba: UsuarioGlobal
        private lateinit var entidadDePruebaCreacion: UsuarioGlobal.UsuarioParaCreacion

        @BeforeEach
        fun crearEntidadPrueba()
        {
            entidadDePruebaCreacion = darInstanciaEntidadCreacionValida()
            entidadDePrueba = repositorio.crear(entidadDePruebaCreacion)
        }

        @TestConMultiplesDAO
        fun solo_se_actualizan_los_campos_necesarios_y_credenciales_se_actualizan_correctamente_y_limpia_contraseña()
        {
            desactivarUsuario(configuracionRepositorios, null, entidadDePrueba.datosUsuario.usuario)

            val campoContraseña = UsuarioGlobal.CredencialesUsuario.CampoContraseña(contraseñaPruebasActualizacion.copyOf())
            val campoActivo = UsuarioGlobal.DatosUsuario.CampoActivo(true)

            val entidadEsperada = entidadDePrueba.copiar(datosUsuario = entidadDePrueba.datosUsuario.copiar(activo = campoActivo.valor))
            val credencialesEsperadas = UsuarioGlobal.CredencialesGuardadas(entidadEsperada, hashPruebasActualizacion)

            repositorio.actualizarCamposIndividuales(
                    entidadDePrueba.datosUsuario.usuario,
                    mapOf<String, CampoModificable<UsuarioGlobal, *>>(
                            entidadDePruebaCreacion.campoContraseña.nombreCampo to campoContraseña,
                            entidadDePruebaCreacion.datosUsuario.campoActivo.nombreCampo to campoActivo
                                                                     )
                                                    )

            val entidadConsultada = repositorio.buscarPorId(entidadDePrueba.datosUsuario.usuario)
            assertEquals(entidadEsperada, entidadConsultada)

            // Verificar credenciales
            val credencialesConsultadas = repositorioCredenciales.buscarPorId(entidadDePrueba.datosUsuario.usuario)
            assertEquals(credencialesEsperadas, credencialesConsultadas)

            // Verificar limpieza contraseña
            assertTrue(campoContraseña.valor.all { it == '\u0000' })
        }

        @TestConMultiplesDAO
        fun con_id_inexistente_lanza_excepcion_EntidadNoExiste_y_limpia_contraseña()
        {
            val campoContraseña = UsuarioGlobal.CredencialesUsuario.CampoContraseña(contraseñaPruebasActualizacion.copyOf())
            assertThrows<EntidadNoExiste> {
                repositorio.actualizarCamposIndividuales(
                        "Usuario inexistente",
                        mapOf<String, CampoModificable<UsuarioGlobal, *>>(entidadDePruebaCreacion.campoContraseña.nombreCampo to campoContraseña)
                                                        )
            }

            // Verificar limpieza contraseña
            assertTrue(campoContraseña.valor.all { it == '\u0000' })
        }
    }

    @Nested
    inner class Eliminar
    {
        private lateinit var entidadPrueba: UsuarioGlobal

        @BeforeEach
        fun crearEntidadDePrueba()
        {
            entidadPrueba = repositorio.crear(darInstanciaEntidadCreacionValida())
        }

        @TestConMultiplesDAO
        fun si_existe_la_entidad_se_elimina_correctamente()
        {
            val resultadoEliminacion = repositorio.eliminarPorId(entidadPrueba.datosUsuario.usuario)
            val entidadConsultada = repositorio.buscarPorId(entidadPrueba.datosUsuario.usuario)

            assertTrue(resultadoEliminacion)
            assertNull(entidadConsultada)
        }

        @TestConMultiplesDAO
        fun si_no_existe_la_entidad_retorna_false_y_no_elimina_entidad_existente()
        {
            val resultadoEliminacion = repositorio.eliminarPorId("${entidadPrueba.datosUsuario.usuario} inexistente")
            val entidadConsultada = repositorio.buscarPorId(entidadPrueba.datosUsuario.usuario)

            assertFalse(resultadoEliminacion)
            assertEquals(entidadPrueba, entidadConsultada)
        }
    }
}