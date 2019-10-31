package co.smartobjects.persistencia.usuarios

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.entidades.usuarios.Rol
import co.smartobjects.entidades.usuarios.Usuario
import co.smartobjects.persistencia.*
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorio
import co.smartobjects.persistencia.excepciones.EntidadNoExiste
import co.smartobjects.persistencia.excepciones.ErrorCreacionActualizacionPorDuplicidad
import co.smartobjects.persistencia.excepciones.ErrorDeLlaveForanea
import co.smartobjects.persistencia.excepciones.EsquemaNoExiste
import co.smartobjects.persistencia.usuarios.roles.RepositorioRoles
import co.smartobjects.persistencia.usuarios.roles.RepositorioRolesSQL
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DisplayName("UsuarioDAO")
internal class UsuarioDAOPruebas : EntidadDAOBasePruebas()
{
    companion object
    {
        private const val hashPruebasCreacion = "\$shiro1\$SHA-512\$500000\$Atk2TMaxtKSRMTLMay+kut9lnG5QfiEzQkMP1MtL8+Y=\$l4OQPChonLAun1+0t+yGsY9VAiESaOEGRVnODk+g9pdWr+wRftRa0lA3MyYJD8NPaqTB6pgjy7w9ZdNOhDVxHg=="
        private const val hashPruebasActualizacion = "\$shiro1\$SHA-512\$500000\$M5g1wTYxkEvUJYXMDW2+PyW8CYQNcb8HvwjGKWEzEKs=\$qM3AYdOeWOB+OKnvxqazd01QpIJvuKVo6GNLk8R5CRYK7uBdcBws7C+ejgHOq37eJePypMqs77art7GrULiFkA=="
        private const val hashPruebasContraseñaPorDefecto = "\$shiro1\$SHA-512\$500000\$1NMay5vxD+sYJTmvvbxDqgyXDzVGzPn0nQz5FJ6el1M=\$1fuogoAW6YUfk2rKzI0Dz8JicKNO57w+S89Y6VvaSxfUilsgigSUK2xyWlILCdr/w8dhrvO85BqjqsddAH5tAQ=="
        private val contraseñaPruebasCreacion = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
        private val contraseñaPruebasActualizacion = charArrayOf('o', 't', 'r', 'o', 'p', 'a', 's', 's', 'w', 'o', 'r', 'd', '2', '_')
        private val contraseñaPorDefecto = charArrayOf('p', 'a', 's', 's', 'w', 'o', 'r', 'd')

        private const val nombreUsuarioPorDefecto = "admin"
        private const val nombreCompletoUsuarioPorDefecto = "Juan Ciga"
        private const val emailUsuarioPorDefecto = "juan.ciga@smartobjects.co"
    }

    private val hasherDummy = object : Hasher
    {
        override fun calcularHash(entrada: CharArray): String
        {
            return when
            {
                Arrays.equals(contraseñaPruebasCreacion, entrada)      -> hashPruebasCreacion
                Arrays.equals(contraseñaPruebasActualizacion, entrada) -> hashPruebasActualizacion
                Arrays.equals(contraseñaPorDefecto, entrada)           -> hashPruebasContraseñaPorDefecto
                else                                                   -> throw RuntimeException("Contraseña ${String(entrada)} no soportada por hasher de pruebas")
            }
        }
    }

    private val repositorioRoles: RepositorioRoles by lazy { RepositorioRolesSQL(configuracionRepositorios) }
    private val repositorio: RepositorioUsuarios by lazy { RepositorioUsuariosSQL(configuracionRepositorios, hasherDummy) }
    private val repositorioCredenciales: RepositorioCredencialesGuardadasUsuario by lazy { RepositorioCredencialesGuardadasUsuarioSQL(configuracionRepositorios) }
    override val creadoresRepositoriosUsados: List<CreadorRepositorio<*>> by lazy {
        listOf<CreadorRepositorio<*>>(repositorioRoles, repositorio)
    }

    private val rolPruebas1 by lazy {
        val permisos = setOf(PermisoBack(idClientePruebas, "ElEndpoint", PermisoBack.Accion.POST), PermisoBack(idClientePruebas, "ElEndpoint", PermisoBack.Accion.PUT))
        repositorioRoles.crear(
                idClientePruebas,
                Rol(
                        "ElRol1",
                        "Descripcion1",
                        permisos
                   )
                              )
    }

    private val rolPruebas2 by lazy {
        val permisos = setOf(PermisoBack(idClientePruebas, "ElEndpoint2", PermisoBack.Accion.GET_UNO), PermisoBack(idClientePruebas, "ElEndpoint2", PermisoBack.Accion.GET_TODOS), PermisoBack(idClientePruebas, "ElEndpoint2", PermisoBack.Accion.DELETE))
        repositorioRoles.crear(
                idClientePruebas,
                Rol(
                        "ElRol2",
                        "Descripcion2",
                        permisos
                   )
                              )

    }

    private fun darInstanciaEntidadValida(): Usuario
    {
        return Usuario(
                Usuario.DatosUsuario(
                        idClientePruebas,
                        "El Usuario",
                        "El nombre completo",
                        "elemail@mail.com",
                        true
                                    ),
                setOf(rolPruebas1)
                      )
    }

    private fun darInstanciaEntidadCreacionValida(usuario: Usuario = darInstanciaEntidadValida())
            : Usuario.UsuarioParaCreacion
    {
        return Usuario.UsuarioParaCreacion(
                usuario.datosUsuario,
                contraseñaPruebasCreacion.copyOf(),
                usuario.roles.map { Rol.RolParaCreacionDeUsuario(it.nombre) }.toSet()
                                          )
    }

    @Nested
    inner class Crear
    {
        @TestConMultiplesDAO
        fun retorna_entidad_correcta()
        {
            val entidadEsperada = darInstanciaEntidadValida()
            val entidadAInsertar = darInstanciaEntidadCreacionValida()
            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertEquals(entidadEsperada, entidadCreada)
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
            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertEquals(entidadEsperada, entidadCreada)
        }

        @TestConMultiplesDAO
        fun limpia_contraseña_de_entidad_de_entrada()
        {
            val entidadAInsertar = darInstanciaEntidadCreacionValida()
            repositorio.crear(idClientePruebas, entidadAInsertar)

            assertTrue(entidadAInsertar.contraseña.all { it == '\u0000' })
        }

        @TestConMultiplesDAO
        fun retorna_entidad_correcta_con_dos_roles_con_multiples_permisos()
        {
            val entidadEsperada = darInstanciaEntidadValida().copiar(roles = setOf(rolPruebas1, rolPruebas2))
            val entidadAInsertar = darInstanciaEntidadCreacionValida().copiar(roles = setOf(Rol.RolParaCreacionDeUsuario(rolPruebas2.nombre), Rol.RolParaCreacionDeUsuario(rolPruebas1.nombre)))
            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertEquals(entidadEsperada, entidadCreada)
        }

        @TestConMultiplesDAO
        fun ignora_el_id_de_cliente_y_retorna_entidad_correcta_con_id_cliente_correcto_en_datos_y_permisos()
        {
            val entidadConIdsCorrectos = darInstanciaEntidadValida()
            val entidadOriginalAInsertar = darInstanciaEntidadCreacionValida()
            val entidadAInsertar = entidadOriginalAInsertar.copiar(datosUsuario = entidadOriginalAInsertar.datosUsuario.copiar(idCliente = 9999))

            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            assertEquals(entidadConIdsCorrectos, entidadCreada)
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_con_ErrorCreacionActualizacionPorDuplicidad_si_se_usa_usuario_duplicado()
        {
            val entidadAInsertar = darInstanciaEntidadCreacionValida()

            repositorio.crear(idClientePruebas, entidadAInsertar)

            assertThrows<ErrorCreacionActualizacionPorDuplicidad> {
                repositorio.crear(
                        idClientePruebas,
                        entidadAInsertar.copiar(contraseña = contraseñaPruebasCreacion.copyOf(), datosUsuario = entidadAInsertar.datosUsuario.copiar(email = "otroemail@pruebas.net"))
                                 )
            }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_con_ErrorCreacionActualizacionPorDuplicidad_si_se_usa_email_duplicado()
        {
            val entidadAInsertar = darInstanciaEntidadCreacionValida()

            repositorio.crear(idClientePruebas, entidadAInsertar)

            assertThrows<ErrorCreacionActualizacionPorDuplicidad> { repositorio.crear(idClientePruebas, entidadAInsertar.copiar(contraseña = contraseñaPruebasCreacion.copyOf(), datosUsuario = entidadAInsertar.datosUsuario.copiar(usuario = "otrousuariodepruebas"))) }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_ErrorDeLlaveForanea_si_se_referencia_un_rol_que_no_existe()
        {
            val entidadAInsertar = darInstanciaEntidadCreacionValida().copiar(roles = setOf(Rol.RolParaCreacionDeUsuario("Rol inexistente")))

            assertThrows<ErrorDeLlaveForanea> { repositorio.crear(idClientePruebas, entidadAInsertar) }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_EsquemaNoExiste_si_cliente_con_id_dado_no_existe()
        {
            val entidadAInsertar = darInstanciaEntidadCreacionValida()

            assertThrows<EsquemaNoExiste> { repositorio.crear(idClientePruebas + 100, entidadAInsertar) }
        }
    }

    @Nested
    inner class Consultar
    {
        @TestConMultiplesDAO
        fun por_id_existente_retorna_entidad_correcta()
        {
            val entidadPrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadCreacionValida())

            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadPrueba.datosUsuario.usuario)

            assertEquals(entidadPrueba, entidadConsultada)
        }

        @TestConMultiplesDAO
        fun con_multiples_roles_retorna_entidad_correcta()
        {
            val entidadOriginal = darInstanciaEntidadCreacionValida()
            val entidadAInsertar = entidadOriginal.copiar(roles = setOf(Rol.RolParaCreacionDeUsuario(rolPruebas1.nombre), Rol.RolParaCreacionDeUsuario(rolPruebas2.nombre)))
            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadCreada.datosUsuario.usuario)

            assertEquals(entidadCreada, entidadConsultada)
        }

        @TestConMultiplesDAO
        fun listado_de_entidades_sin_entidades_retorna_lista_vacia()
        {
            val listadoConsultado = repositorio.listar(idClientePruebas)

            assertTrue(listadoConsultado.none())
        }

        @TestConMultiplesDAO
        fun listado_de_entidades_retorna_entidades_correctas()
        {
            val entidadAInsertar = darInstanciaEntidadCreacionValida()
            val entidadAInsertar2 = darInstanciaEntidadCreacionValida().copiar(datosUsuario = entidadAInsertar.datosUsuario.copiar(usuario = "otroUsuarioDePruebas", email = "Otroemail@pruebas.net"))
            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)
            val entidadCreada2 = repositorio.crear(idClientePruebas, entidadAInsertar2)
            val entidadesPrueba = listOf(entidadCreada, entidadCreada2)

            val listadoEsperado = entidadesPrueba.sortedBy { it.datosUsuario.usuario }

            val listadoConsultado = repositorio.listar(idClientePruebas)

            assertEquals(listadoEsperado, listadoConsultado.sortedBy { it.datosUsuario.usuario }.toList())
        }

        @TestConMultiplesDAO
        fun por_id_no_existente_retorna_null()
        {
            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, "Usuario no existente")

            assertNull(entidadConsultada)
        }

        @TestConMultiplesDAO
        fun por_id_existente_en_otro_cliente_retorna_null()
        {
            val entidadPrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadCreacionValida())

            ejecutarConClienteAlternativo {
                val entidadConsultada = repositorio.buscarPorId(it.id!!, entidadPrueba.datosUsuario.usuario)
                assertNull(entidadConsultada)
            }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_EsquemaNoExiste_si_se_consulta_por_id_con_id_cliente_inexistente()
        {
            assertThrows<EsquemaNoExiste> { repositorio.buscarPorId(idClientePruebas + 100, "Usuario iexistente") }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_EsquemaNoExiste_si_se_consulta_listado_de_entidades_con_id_cliente_inexistente()
        {
            assertThrows<EsquemaNoExiste> { repositorio.listar(idClientePruebas + 100) }
        }

        @TestConMultiplesDAO
        fun listado_de_entidades_con_entidades_en_otro_cliente_retorna_lista_vacia()
        {
            repositorio.crear(idClientePruebas, darInstanciaEntidadCreacionValida())

            ejecutarConClienteAlternativo {
                val listadoConsultado = repositorio.listar(it.id!!)
                assertTrue(listadoConsultado.none())
            }
        }
    }

    @Nested
    inner class ConsultarCredenciales
    {
        @TestConMultiplesDAO
        fun por_id_existente_retorna_entidad_correcta()
        {
            val entidadPrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadCreacionValida())
            val entidadEsperada = Usuario.CredencialesGuardadas(entidadPrueba, hashPruebasCreacion)
            val entidadConsultada = repositorioCredenciales.buscarPorId(idClientePruebas, entidadPrueba.datosUsuario.usuario)

            assertEquals(entidadEsperada, entidadConsultada)
        }

        @TestConMultiplesDAO
        fun por_id_existente_pero_usuario_desactivado_retorna_null()
        {
            val entidadPrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadCreacionValida())

            val campoActivoComoFalse = Usuario.DatosUsuario.CampoActivo(false)
            repositorio.actualizarCamposIndividuales(
                    idClientePruebas,
                    entidadPrueba.datosUsuario.usuario,
                    mapOf(campoActivoComoFalse.nombreCampo to campoActivoComoFalse)
                                                    )

            val entidadConsultada = repositorioCredenciales.buscarPorId(idClientePruebas, entidadPrueba.datosUsuario.usuario)

            assertNull(entidadConsultada)
        }

        @TestConMultiplesDAO
        fun con_multiples_roles_retorna_entidad_correcta()
        {
            val entidadOriginal = darInstanciaEntidadCreacionValida()
            val entidadAInsertar = entidadOriginal.copiar(roles = setOf(Rol.RolParaCreacionDeUsuario(rolPruebas1.nombre), Rol.RolParaCreacionDeUsuario(rolPruebas2.nombre)))
            val entidadCreada = repositorio.crear(idClientePruebas, entidadAInsertar)

            val entidadEsperada = Usuario.CredencialesGuardadas(entidadCreada, hashPruebasCreacion)
            val entidadConsultada = repositorioCredenciales.buscarPorId(idClientePruebas, entidadCreada.datosUsuario.usuario)

            assertEquals(entidadEsperada, entidadConsultada)
        }

        @TestConMultiplesDAO
        fun por_id_no_existente_retorna_null()
        {
            val entidadConsultada = repositorioCredenciales.buscarPorId(idClientePruebas, "Usuario no existente")

            assertNull(entidadConsultada)
        }

        @TestConMultiplesDAO
        fun por_id_existente_en_otro_cliente_retorna_null()
        {
            val entidadPrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadCreacionValida())

            ejecutarConClienteAlternativo {
                val entidadConsultada = repositorioCredenciales.buscarPorId(it.id!!, entidadPrueba.datosUsuario.usuario)
                assertNull(entidadConsultada)
            }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_EsquemaNoExiste_si_se_consulta_por_id_con_id_cliente_inexistente()
        {
            assertThrows<EsquemaNoExiste> { repositorioCredenciales.buscarPorId(idClientePruebas + 100, "Usuario iexistente") }
        }
    }

    @Nested
    inner class Actualizar
    {
        private lateinit var entidadDePrueba: Usuario
        private lateinit var entidadDePruebaParaActualizacion: Usuario.UsuarioParaCreacion

        @BeforeEach
        fun crearEntidadDePrueba()
        {
            entidadDePruebaParaActualizacion = darInstanciaEntidadCreacionValida()
            entidadDePrueba = repositorio.crear(idClientePruebas, entidadDePruebaParaActualizacion)
        }

        @TestConMultiplesDAO
        fun se_actualizan_todos_los_campos_correctamente_y_credenciales_se_actualizan_correctamente()
        {
            val entidadModificada = Usuario(
                    entidadDePrueba.datosUsuario.copiar(
                            idClientePruebas,
                            entidadDePrueba.datosUsuario.usuario,
                            "Otro nombre largo",
                            "otroemail@pruebas.algo"
                                                       ),
                    setOf(rolPruebas1, rolPruebas2)
                                           )
            val entidadModificadaParaActualizacion = Usuario.UsuarioParaCreacion(
                    entidadModificada.datosUsuario,
                    contraseñaPruebasActualizacion.copyOf(),
                    setOf(Rol.RolParaCreacionDeUsuario(rolPruebas1.nombre), Rol.RolParaCreacionDeUsuario(rolPruebas2.nombre))
                                                                                )
            val entidadActualizada = repositorio.actualizar(idClientePruebas, entidadModificadaParaActualizacion.datosUsuario.usuario, entidadModificadaParaActualizacion)
            assertEquals(entidadModificada, entidadActualizada)

            // Verificar credenciales
            val credencialesEsperadas = Usuario.CredencialesGuardadas(entidadModificada, hashPruebasActualizacion)
            val credencialesConsultadas = repositorioCredenciales.buscarPorId(idClientePruebas, entidadModificada.datosUsuario.usuario)
            assertEquals(credencialesEsperadas, credencialesConsultadas)
        }

        @TestConMultiplesDAO
        fun limpia_contraseña_de_entidad_de_entrada()
        {
            val entidadConCambios = darInstanciaEntidadCreacionValida()
            repositorio.actualizar(idClientePruebas, entidadConCambios.datosUsuario.usuario, entidadConCambios)

            assertTrue(entidadConCambios.contraseña.all { it == '\u0000' })
        }

        @TestConMultiplesDAO
        fun utiliza_el_parametro_id_cliente_y_retorna_misma_entidad_con_id_cliente_correcto()
        {
            val entidadConCambios = darInstanciaEntidadCreacionValida().copiar(entidadDePrueba.datosUsuario.copiar(idCliente = 99999))

            val entidadActualizada = repositorio.actualizar(idClientePruebas, entidadConCambios.datosUsuario.usuario, entidadConCambios)

            assertEquals(entidadDePrueba, entidadActualizada)
        }

        @TestConMultiplesDAO
        fun con_id_inexistente_lanza_excepcion_EntidadNoExiste()
        {
            val entidadConCambios = darInstanciaEntidadCreacionValida().copiar(entidadDePrueba.datosUsuario.copiar(usuario = "Usuario inexistente"))

            assertThrows<EntidadNoExiste> { repositorio.actualizar(idClientePruebas, entidadConCambios.datosUsuario.usuario, entidadConCambios) }
        }


        @TestConMultiplesDAO
        fun con_rol_inexistente_lanza_excepcion_ErrorDeLlaveForanea()
        {
            val entidadConCambios = darInstanciaEntidadCreacionValida().copiar(roles = setOf(Rol.RolParaCreacionDeUsuario("Rol inexistente")))

            assertThrows<ErrorDeLlaveForanea> {
                repositorio.actualizar(idClientePruebas, entidadConCambios.datosUsuario.usuario, entidadConCambios)
            }
        }


        @TestConMultiplesDAO
        fun con_email_duplicado_lanza_excepcion_ErrorCreacionActualizacionPorDuplicidad()
        {
            val entidadACrear = darInstanciaEntidadCreacionValida().copiar(datosUsuario = entidadDePrueba.datosUsuario.copiar(usuario = "Otro usuario", email = "otroemail@pruebas.net"))
            repositorio.crear(idClientePruebas, entidadACrear)

            assertThrows<ErrorCreacionActualizacionPorDuplicidad> {
                repositorio.actualizar(idClientePruebas, entidadACrear.datosUsuario.usuario, darInstanciaEntidadCreacionValida().copiar(datosUsuario = entidadACrear.datosUsuario.copiar(email = entidadDePrueba.datosUsuario.email)))
            }
        }

        @TestConMultiplesDAO
        fun con_id_existente_en_otro_cliente_lanza_excepcion_EntidadNoExiste()
        {
            ejecutarConClienteAlternativo {
                assertThrows<EntidadNoExiste> { repositorio.actualizar(it.id!!, entidadDePruebaParaActualizacion.datosUsuario.usuario, darInstanciaEntidadCreacionValida()) }
            }
        }

        @TestConMultiplesDAO
        fun con_id_cliente_inexistente_lanza_excepcion_EsquemaNoExiste()
        {
            assertThrows<EsquemaNoExiste> { repositorio.actualizar(idClientePruebas + 100, entidadDePruebaParaActualizacion.datosUsuario.usuario, entidadDePruebaParaActualizacion) }
        }
    }

    @Nested
    inner class ActualizarParcialmente
    {
        private lateinit var entidadDePrueba: Usuario
        private lateinit var entidadDePruebaCreacion: Usuario.UsuarioParaCreacion

        @BeforeEach
        fun crearEntidadPrueba()
        {
            entidadDePruebaCreacion = darInstanciaEntidadCreacionValida()
            entidadDePrueba = repositorio.crear(idClientePruebas, entidadDePruebaCreacion)
        }

        @TestConMultiplesDAO
        fun solo_se_actualizan_los_campos_necesarios_y_credenciales_se_actualizan_correctamente_y_limpia_contraseña()
        {
            desactivarUsuario(configuracionRepositorios, idClientePruebas, entidadDePrueba.datosUsuario.usuario)

            val campoContraseña = Usuario.CredencialesUsuario.CampoContraseña(contraseñaPruebasActualizacion.copyOf())
            val campoActivo = Usuario.DatosUsuario.CampoActivo(true)

            val entidadEsperada = entidadDePrueba.copiar(datosUsuario = entidadDePrueba.datosUsuario.copiar(activo = campoActivo.valor))
            val credencialesEsperadas = Usuario.CredencialesGuardadas(entidadEsperada, hashPruebasActualizacion)

            repositorio.actualizarCamposIndividuales(
                    idClientePruebas,
                    entidadDePrueba.datosUsuario.usuario,
                    mapOf<String, CampoModificable<Usuario, *>>(
                            entidadDePruebaCreacion.campoContraseña.nombreCampo to campoContraseña,
                            entidadDePruebaCreacion.datosUsuario.campoActivo.nombreCampo to campoActivo
                                                               )
                                                    )

            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadDePrueba.datosUsuario.usuario)
            assertEquals(entidadEsperada, entidadConsultada)

            // Verificar credenciales
            val credencialesConsultadas = repositorioCredenciales.buscarPorId(idClientePruebas, entidadDePrueba.datosUsuario.usuario)
            assertEquals(credencialesEsperadas, credencialesConsultadas)

            // Verificar limpieza contraseña
            assertTrue(campoContraseña.valor.all { it == '\u0000' })
        }

        @TestConMultiplesDAO
        fun con_id_inexistente_lanza_excepcion_EntidadNoExiste_y_limpia_contraseña()
        {
            val campoContraseña = Usuario.CredencialesUsuario.CampoContraseña(contraseñaPruebasActualizacion.copyOf())
            assertThrows<EntidadNoExiste> {
                repositorio.actualizarCamposIndividuales(
                        idClientePruebas,
                        "Usuario inexistente",
                        mapOf<String, CampoModificable<Usuario, *>>(entidadDePruebaCreacion.campoContraseña.nombreCampo to campoContraseña)
                                                        )
            }

            // Verificar limpieza contraseña
            assertTrue(campoContraseña.valor.all { it == '\u0000' })
        }

        @TestConMultiplesDAO
        fun con_id_existente_en_otro_cliente_lanza_excepcion_EntidadNoExiste_y_limpia_contraseña()
        {
            val campoContraseña = Usuario.CredencialesUsuario.CampoContraseña(contraseñaPruebasActualizacion.copyOf())

            ejecutarConClienteAlternativo {
                assertThrows<EntidadNoExiste> {
                    repositorio.actualizarCamposIndividuales(
                            it.id!!,
                            entidadDePrueba.datosUsuario.usuario,
                            mapOf<String, CampoModificable<Usuario, *>>(entidadDePruebaCreacion.campoContraseña.nombreCampo to campoContraseña)
                                                            )
                }
            }

            // Verificar limpieza contraseña
            assertTrue(campoContraseña.valor.all { it == '\u0000' })
        }

        @TestConMultiplesDAO
        fun con_id_cliente_inexistente_lanza_excepcion_EsquemaNoExiste()
        {
            val campoContraseña = Usuario.CredencialesUsuario.CampoContraseña(contraseñaPruebasActualizacion.copyOf())

            assertThrows<EsquemaNoExiste> {
                repositorio.actualizarCamposIndividuales(
                        idClientePruebas + 100,
                        entidadDePrueba.datosUsuario.usuario,
                        mapOf<String, CampoModificable<Usuario, *>>(entidadDePruebaCreacion.campoContraseña.nombreCampo to campoContraseña)
                                                        )
            }
        }
    }

    @Nested
    inner class Eliminar
    {
        private lateinit var entidadPrueba: Usuario

        @BeforeEach
        fun crearEntidadDePrueba()
        {
            entidadPrueba = repositorio.crear(idClientePruebas, darInstanciaEntidadCreacionValida())
        }

        @TestConMultiplesDAO
        fun si_existe_la_entidad_se_elimina_correctamente()
        {
            val resultadoEliminacion = repositorio.eliminarPorId(idClientePruebas, entidadPrueba.datosUsuario.usuario)
            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadPrueba.datosUsuario.usuario)

            assertTrue(resultadoEliminacion)
            assertNull(entidadConsultada)
        }

        @TestConMultiplesDAO
        fun se_eliminan_roles_usuario()
        {
            val resultadoEliminacion = repositorio.eliminarPorId(idClientePruebas, entidadPrueba.datosUsuario.usuario)
            assertTrue(resultadoEliminacion)
            assertFalse(existeAlgunUsuarioDAO(idClientePruebas, configuracionRepositorios) { it.usuario != nombreUsuarioPorDefecto })
            assertFalse(existeAlgunRolUsuarioDAO(idClientePruebas, configuracionRepositorios) { it.usuarioDAO.usuario != nombreUsuarioPorDefecto })
        }

        @TestConMultiplesDAO
        fun si_no_existe_la_entidad_retorna_false_y_no_elimina_entidad_existente()
        {
            val resultadoEliminacion = repositorio.eliminarPorId(idClientePruebas, "${entidadPrueba.datosUsuario.usuario} inexistente")
            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadPrueba.datosUsuario.usuario)

            assertFalse(resultadoEliminacion)
            assertEquals(entidadPrueba, entidadConsultada)
        }

        @TestConMultiplesDAO
        fun con_id_existente_en_otro_cliente_retorna_false_y_no_elimina_entidad_existente()
        {
            ejecutarConClienteAlternativo {
                val resultadoEliminacion = repositorio.eliminarPorId(it.id!!, entidadPrueba.datosUsuario.usuario)
                kotlin.test.assertFalse(resultadoEliminacion)
            }
            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadPrueba.datosUsuario.usuario)
            assertEquals(entidadPrueba, entidadConsultada)
        }

        @TestConMultiplesDAO
        fun con_id_cliente_inexistente_lanza_excepcion_EsquemaNoExiste()
        {
            assertThrows<EsquemaNoExiste> { repositorio.eliminarPorId(idClientePruebas + 100, entidadPrueba.datosUsuario.usuario) }
        }

        @TestConMultiplesDAO
        fun si_se_intenta_borrar_el_rol_lanza_excepcion_ErrorDeLlaveForanea_y_no_elimina_entidad()
        {
            assertThrows<ErrorDeLlaveForanea> {
                repositorioRoles.eliminarPorId(idClientePruebas, rolPruebas1.nombre)
            }
            val entidadConsultada = repositorio.buscarPorId(idClientePruebas, entidadPrueba.datosUsuario.usuario)
            assertEquals(entidadPrueba, entidadConsultada)
        }
    }
}