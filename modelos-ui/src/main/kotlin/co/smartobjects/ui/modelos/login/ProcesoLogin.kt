package co.smartobjects.ui.modelos.login

import co.smartobjects.ui.modelos.login.activeDirectory
import co.smartobjects.entidades.usuarios.Usuario
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.usuarios.UsuariosAPI
import co.smartobjects.ui.modelos.ModeloUI
import co.smartobjects.ui.modelos.ResultadoAccionUI
import co.smartobjects.utilidades.Opcional
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject



interface ProcesoLogin : ModeloUI
{
    val credenciales: CredencialesUsuarioUI
    val estado: Observable<Estado>
    val errorGlobal: Observable<Opcional<String>>
    val usuarioSesionIniciada: Observable<Usuario>
    val puedeIniciarSesion: Observable<Boolean>

    fun intentarLogin(): ResultadoAccionUI

    override val modelosHijos: List<ModeloUI>
        get() = listOf(credenciales)

    enum class Estado
    {
        ESPERANDO_CREDENCIALES, INICIANDO_SESION, SESION_INICIADA
    }
}

class ProcesoLoginConSujetos internal constructor(
        override val credenciales: CredencialesUsuarioUI,
        private val apiUsuarios: UsuariosAPI,
        private val ioScheduler: Scheduler = Schedulers.io()
                                                 ) : ProcesoLogin
{
    constructor(apiLogin: UsuariosAPI, ioScheduler: Scheduler = Schedulers.io())
            : this(CredencialesUsuarioUIConSujetos(), apiLogin, ioScheduler)

    private val sujetoEstado: BehaviorSubject<ProcesoLogin.Estado> = BehaviorSubject.createDefault(ProcesoLogin.Estado.ESPERANDO_CREDENCIALES)
    private val sujetoUsuarioIniciado: BehaviorSubject<Usuario> = BehaviorSubject.create()
    private val sujetoSonCredencialesCorrectas: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)
    private val sujetoErrorGlobal: BehaviorSubject<Opcional<String>> = BehaviorSubject.createDefault(Opcional.Vacio())

    override val estado: Observable<ProcesoLogin.Estado> = sujetoEstado
    override val errorGlobal: Observable<Opcional<String>> = sujetoErrorGlobal
    override val usuarioSesionIniciada: Observable<Usuario> = sujetoUsuarioIniciado
    override val puedeIniciarSesion: Observable<Boolean> =
            Observables.combineLatest(sujetoSonCredencialesCorrectas, estado)
            { sonCredencialesCorrectas, estado ->
                sonCredencialesCorrectas && estado == ProcesoLogin.Estado.ESPERANDO_CREDENCIALES
            }

    override val observadoresInternos: List<Subject<*>> =
            listOf(sujetoEstado, sujetoUsuarioIniciado, sujetoSonCredencialesCorrectas, sujetoErrorGlobal)

    init
    {
        credenciales.sonCredencialesValidas.subscribe(sujetoSonCredencialesCorrectas)
    }

    override fun intentarLogin(): ResultadoAccionUI
    {   print("\nIntento Login")
        var ex :String
        return if (sujetoEstado.value == ProcesoLogin.Estado.ESPERANDO_CREDENCIALES)
        {
            if (sujetoSonCredencialesCorrectas.value!!)
            {
                var credencialesActuales = try
                {
                    credenciales.aCredencialesUsuario()
                }
                catch (e: IllegalStateException)
                {
                    sujetoErrorGlobal.onNext(Opcional.De("Credenciales inválidas"))
                    return ResultadoAccionUI.OBSERVABLES_EN_ESTADO_INVALIDO
                }
                sujetoErrorGlobal.onNext(Opcional.Vacio())
                sujetoEstado.onNext(ProcesoLogin.Estado.INICIANDO_SESION)
                val userCafam = credencialesActuales.usuario+"@cafam.com.co"
                val passCafam = String(credencialesActuales.contraseña)
                val tex = activeDirectory();
                ex = tex.main(userCafam, passCafam)
                println("respuesta "+ex)
                if(ex.equals("Datos invalidos")){
                    sujetoEstado.onNext(ProcesoLogin.Estado.ESPERANDO_CREDENCIALES)
                    sujetoErrorGlobal.onNext(Opcional.De("Credenciales Incorrectas"))
                }else{
                Observable
                    .fromCallable { apiUsuarios.login(credencialesActuales) }
                    .subscribeOn(ioScheduler)
                    .subscribe {
                        when (it)
                        {
                            is RespuestaIndividual.Exitosa       ->
                            {
                                sujetoEstado.onNext(ProcesoLogin.Estado.SESION_INICIADA)
                                sujetoUsuarioIniciado.onNext(it.respuesta)
                                finalizarProceso()
                            }
                            is RespuestaIndividual.Vacia         ->
                            {
                                sujetoEstado.onNext(ProcesoLogin.Estado.ESPERANDO_CREDENCIALES)
                            }
                            is RespuestaIndividual.Error.Timeout ->
                            {
                                sujetoEstado.onNext(ProcesoLogin.Estado.ESPERANDO_CREDENCIALES)
                                sujetoErrorGlobal.onNext(Opcional.De("Timeout contactando el backend"))
                            }
                            is RespuestaIndividual.Error.Red     ->
                            {
                                sujetoEstado.onNext(ProcesoLogin.Estado.ESPERANDO_CREDENCIALES)
                                sujetoErrorGlobal.onNext(Opcional.De("Error contactando el backend"))
                            }
                            is RespuestaIndividual.Error.Back    ->
                            {
                                sujetoEstado.onNext(ProcesoLogin.Estado.ESPERANDO_CREDENCIALES)
                                sujetoErrorGlobal.onNext(Opcional.De("Error en petición: ${it.error.mensaje}"))
                            }
                        }
                    }
                }
                ResultadoAccionUI.ACCION_INICIADA
            }
            else
            {
                ResultadoAccionUI.MODELO_EN_ESTADO_INVALIDO
            }
        }
        else
        {
            ResultadoAccionUI.PROCESO_EN_ESTADO_INVALIDO
        }
    }
}