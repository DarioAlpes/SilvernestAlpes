package co.smartobjects.silvernestandroid.iniciodesesion

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.*
import co.smartobjects.entidades.clientes.Cliente
import co.smartobjects.entidades.ubicaciones.contabilizables.UbicacionesContabilizables
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.base.RespuestaVacia
import co.smartobjects.red.clientes.retrofit.ManejadorDePeticiones
import co.smartobjects.silvernestandroid.AplicacionPrincipal.Companion.TAG
import co.smartobjects.silvernestandroid.BuildConfig
import co.smartobjects.silvernestandroid.R
import co.smartobjects.silvernestandroid.conteodeubicaciones.ConteoDeUbicacionesActividad
import co.smartobjects.silvernestandroid.inyecciondedependencias.schedulerBD
import co.smartobjects.silvernestandroid.utilidades.UtilidadesDispositivo
import co.smartobjects.silvernestandroid.utilidades.UtilidadesInterfaz
import co.smartobjects.silvernestandroid.utilidades.controladores.ActividadBase
import co.smartobjects.silvernestandroid.utilidades.controlesui.TextInputLayoutErrorEscondible
import co.smartobjects.silvernestandroid.utilidades.isVisible
import co.smartobjects.silvernestandroid.utilidades.observarEnUI
import co.smartobjects.silvernestandroid.utilidades.persistencia.DependenciasBD
import co.smartobjects.ui.modelos.login.ProcesoLogin
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.focusChanges
import com.jakewharton.rxbinding3.widget.editorActionEvents
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.scope.ext.android.bindScope
import org.koin.android.scope.ext.android.getOrCreateScope
import java.util.concurrent.TimeUnit

class InicioDeSesionActividad : ActividadBase()
{
    override val idLayout: Int = R.layout.inicio_de_sesion_actividad

    private val campoUsuario by lazy(LazyThreadSafetyMode.NONE) { findViewById<TextInputLayoutErrorEscondible>(R.id.TextInputLayout_email) }
    private val campoContraseña by lazy(LazyThreadSafetyMode.NONE) { findViewById<TextInputLayoutErrorEscondible>(R.id.TextInputLayout_password) }
    private val barraDeProgreso by lazy(LazyThreadSafetyMode.NONE) { findViewById<ProgressBar>(R.id.barra_de_progreso) }
    private val botonIniciarSesion by lazy(LazyThreadSafetyMode.NONE) { findViewById<Button>(R.id.btn_iniciar_sesion) }
    private val scrollViewFormularioLogin by lazy(LazyThreadSafetyMode.NONE) { findViewById<ScrollView>(R.id.scrollview_formulario_inicio_de_sesion) }
    private val mensajeEstadoInicioDesesion by lazy(LazyThreadSafetyMode.NONE) { findViewById<TextView>(R.id.texto_estado_inicio_de_sesion) }
    private val versionAplicacion by lazy(LazyThreadSafetyMode.NONE) { findViewById<TextView>(R.id.txvw_version_aplicacion) }

    private val procesoLogin: ProcesoLogin by inject()
    //    private val sincronizadorDeDatos: SincronizadorDeDatos by inject()
    private val manejadorDeDatos: ManejadorDePeticiones by inject()
    private val dependenciasBD: DependenciasBD by inject()

    private val compositeDisposable = CompositeDisposable()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        bindScope(getOrCreateScope(ProcesoLogin::class.simpleName!!))

        versionAplicacion.text = "Versión: ${BuildConfig.VERSION_NAME}"

        inicializarCampoDeUsuario()
        inicializarCampoContraseña()
        inicializarManejoDeEstados()
        inicializarBotonDeInicioDeSesion()
    }

    override fun configurarToolbar(instanciaToolbar: Toolbar?)
    {
        super.configurarToolbar(instanciaToolbar)

        if (instanciaToolbar != null)
        {
            supportActionBar!!.setDisplayHomeAsUpEnabled(false)

            with(instanciaToolbar.findViewById<TextView>(R.id.toolbar_titulo))
            {
                isVisible = true
                setText(R.string.mensaje_iniciar_sesion)
            }
        }
    }

    private fun inicializarCampoDeUsuario()
    {
        UtilidadesInterfaz.inicializarTextInputLayoutConTextoLimpiable(campoUsuario)

        with(campoUsuario.editText!!)
        {
            focusChanges().skip(1).filter { tieneFoco -> !tieneFoco }.observarEnUI().subscribe {
                if (TextUtils.isEmpty(campoUsuario.editText!!.text))
                {
                    campoUsuario.error = getString(R.string.error_campo_obligatorio)
                }
            }.addTo(compositeDisposable)

            textChanges().skip(1).debounce(300, TimeUnit.MILLISECONDS).map { it.toString() }.distinctUntilChanged().observarEnUI().subscribe {
                procesoLogin.credenciales.cambiarUsuario(it)
            }.addTo(compositeDisposable)
        }

        procesoLogin.credenciales.usuario.observarEnUI().subscribe {
            campoUsuario.error = null
            if (it.isOnNext)
            {
                with(campoUsuario.editText!!)
                {
                    setText(it.value!!)
                    setSelection(it.value!!.length)
                }
            }
            else if (it.isOnError)
            {
                campoUsuario.error = it.error!!.message
            }
        }.addTo(compositeDisposable)
    }

    private fun inicializarCampoContraseña()
    {
        with(campoContraseña.editText!!)
        {
            focusChanges().skip(1).filter { tieneFoco -> !tieneFoco }.observarEnUI().subscribe {
                if (TextUtils.isEmpty(campoContraseña.editText!!.text))
                {
                    campoContraseña.error = getString(R.string.error_campo_obligatorio)
                }
            }.addTo(compositeDisposable)

            textChanges().skip(1).debounce(300, TimeUnit.MILLISECONDS).map { it.toString() }.distinctUntilChanged().observarEnUI().subscribe {
                procesoLogin.credenciales.cambiarContraseña(it.toCharArray())
            }.addTo(compositeDisposable)
        }

        procesoLogin.credenciales.contraseña.observarEnUI().subscribe {
            campoContraseña.error = null
            if (it.isOnNext)
            {
                with(campoContraseña.editText!!)
                {
                    val contraseñaStr = String(it.value!!)
                    setText(contraseñaStr)
                    setSelection(contraseñaStr.length)
                }
            }
            else if (it.isOnError)
            {
                campoContraseña.error = it.error!!.message
            }
        }.addTo(compositeDisposable)

        campoContraseña.editText!!
            .editorActionEvents {
                it.actionId in arrayOf(R.id.accionIniciarLogin, EditorInfo.IME_ACTION_DONE, EditorInfo.IME_NULL)
            }
            .debounce(300, TimeUnit.MILLISECONDS)
            .withLatestFrom(procesoLogin.puedeIniciarSesion.observarEnUI())
            .filter { it.second }
            .observarEnUI()
            .subscribe {
                verificarConexionEIniciarSesion()
            }
            .addTo(compositeDisposable)
    }

    private fun inicializarBotonDeInicioDeSesion()
    {
        botonIniciarSesion.clicks().debounce(250, TimeUnit.MILLISECONDS).observarEnUI().subscribe {
            verificarConexionEIniciarSesion()
        }.addTo(compositeDisposable)
    }

    private fun inicializarManejoDeEstados()
    {
        procesoLogin.errorGlobal.observarEnUI().filter { !it.esVacio }.subscribe {
            UtilidadesInterfaz.mostrarToastError(this, it.valor, Toast.LENGTH_LONG)
        }.addTo(compositeDisposable)

        procesoLogin.estado.observarEnUI().subscribe {
            actualizarUISiEstaIniciandoSesion(it === ProcesoLogin.Estado.INICIANDO_SESION)
        }.addTo(compositeDisposable)

        procesoLogin.usuarioSesionIniciada
            .observarEnUI()
            .doOnNext {
                aplicacionPrincipal.usuarioActual = it

                Log.d(TAG, it.toString())

                mensajeEstadoInicioDesesion.text = getString(R.string.descargando_datos)
                actualizarUISiEstaIniciandoSesion(true)
            }
            .observeOn(Schedulers.io())
            .doOnNext {
                dependenciasBD.repositorioLlavesNFC.limpiarParaCliente(BuildConfig.ID_CLIENTE)
                dependenciasBD.repositorioLlavesNFC.crear(BuildConfig.ID_CLIENTE, Cliente.LlaveNFC(BuildConfig.ID_CLIENTE, "0123456789"))
            }
            .flatMapSingle { usuarioRecibido ->
                Single
                    .fromCallable { manejadorDeDatos.apiDeUbicaciones.consultar() }
                    .observeOn(schedulerBD)
                    .map { respuesta ->
                        when (respuesta)
                        {
                            is RespuestaIndividual.Exitosa ->
                            {
                                dependenciasBD.repositorioUbicaciones.limpiarParaCliente(BuildConfig.ID_CLIENTE)

                                for (entidad in respuesta.respuesta)
                                {
                                    dependenciasBD.repositorioUbicaciones.crear(BuildConfig.ID_CLIENTE, entidad)
                                }
                                RespuestaVacia.Exitosa
                            }
                            else                           ->
                            {
                                RespuestaVacia.desdeRespuestaInvidual(respuesta)
                            }
                        }
                    }
                    .flatMap {
                        if (it === RespuestaVacia.Exitosa)
                        {
                            Single
                                .fromCallable { manejadorDeDatos.apiDeUbicacionesContabilizables.consultar() }
                                .observeOn(schedulerBD)
                                .map { respuesta ->
                                    when (respuesta)
                                    {
                                        is RespuestaIndividual.Exitosa ->
                                        {
                                            dependenciasBD.repositorioUbicacionesContabilizables.limpiarParaCliente(BuildConfig.ID_CLIENTE)

                                            val ubicacionesConsultadas =
                                                    UbicacionesContabilizables(BuildConfig.ID_CLIENTE, respuesta.respuesta.map { it }.toSet())

                                            dependenciasBD.repositorioUbicacionesContabilizables.crear(BuildConfig.ID_CLIENTE, ubicacionesConsultadas)

                                            RespuestaVacia.Exitosa
                                        }
                                        else                           ->
                                        {
                                            RespuestaVacia.desdeRespuestaInvidual(respuesta)
                                        }
                                    }
                                }
                                .subscribeOn(Schedulers.io())
                        }
                        else
                        {
                            Single.just(it)
                        }
                    }
                    .subscribeOn(Schedulers.io())
            }
            .observarEnUI()
            .doFinally { actualizarUISiEstaIniciandoSesion(false) }
            .subscribeBy(
                    onNext = {
                        if (it === RespuestaVacia.Exitosa)
                        {
                            Log.d(TAG, "Datos descargados correctamente")
                            finalizarEIniciarActividadPrincipal()
                        }
                        else
                        {
                            UtilidadesInterfaz
                                .mostrarToastError(
                                        this,
                                        getString(R.string.error_al_descargar_datos),
                                        Toast.LENGTH_LONG
                                                  )
                        }
                    },
                    onComplete = { }
                        )
            .addTo(compositeDisposable)
    }

    private fun finalizarEIniciarActividadPrincipal()
    {
        val intent = Intent(this, ConteoDeUbicacionesActividad::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    private fun verificarConexionEIniciarSesion()
    {
        esconderTeclado(this)

        actualizarUISiEstaVerificandoInternet(true)

        launch {
            val tieneInternet = UtilidadesDispositivo.tieneInternetDisponible(this@InicioDeSesionActividad)

            if (tieneInternet)
            {
                actualizarUISiEstaVerificandoInternet(false)
                procesoLogin.intentarLogin()
            }
            else
            {
                actualizarUISiEstaVerificandoInternet(false)
                actualizarUIParaErrorRealizandoLogin()
            }
        }
    }

    private fun actualizarUIParaErrorRealizandoLogin()
    {
        UtilidadesInterfaz.mostrarToastError(this, getString(R.string.error_no_hay_conexion_a_internet), Toast.LENGTH_LONG)
    }

    private fun actualizarUISiEstaVerificandoInternet(estaVerificandoInternet: Boolean)
    {
        botonIniciarSesion.isEnabled = !estaVerificandoInternet
        botonIniciarSesion.text = getString(if (estaVerificandoInternet) R.string.verificando_conexion_a_internet else R.string.mensaje_iniciar_sesion)
    }

    private fun actualizarUISiEstaIniciandoSesion(estaIniciandoSesion: Boolean)
    {
        barraDeProgreso.isVisible = estaIniciandoSesion
        mensajeEstadoInicioDesesion.isVisible = estaIniciandoSesion
        scrollViewFormularioLogin.isVisible = !estaIniciandoSesion
    }

//    override fun onStart()
//    {
//        super.onStart()
//    }

//    override fun onStop()
//    {
//        super.onStop()
//
//        compositeDisposable.clear()
//    }

    override fun onDestroy()
    {
        super.onDestroy()

//        compositeDisposable.dispose()
    }
}
