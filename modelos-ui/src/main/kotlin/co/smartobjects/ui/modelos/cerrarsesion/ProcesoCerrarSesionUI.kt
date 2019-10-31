package co.smartobjects.ui.modelos.cerrarsesion

import co.smartobjects.entidades.usuarios.Usuario
import co.smartobjects.red.clientes.base.RespuestaVacia
import co.smartobjects.red.clientes.usuarios.UsuariosAPI
import co.smartobjects.ui.modelos.ModeloUI
import co.smartobjects.utilidades.Opcional
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject


interface ProcesoCerrarSesionUI : ModeloUI
{
    val estado: Observable<Estado>

    fun cerrarSesion()
    fun reiniciarEstado()

    override val modelosHijos: List<ModeloUI>
        get() = listOf()

    sealed class Estado
    {
        object Esperando : Estado()
        object CerrandoSesion : Estado()
        sealed class Resultado : Estado()
        {
            object SesionCerrada : Resultado()
            data class ErrorCerrandoSesion(val mensaje: String) : Resultado()
        }
    }
}

class ProcesoCerrarSesion
(
        private val apiUsuariosAPI: UsuariosAPI,
        private val usuarioActual: BehaviorSubject<Opcional<Usuario>>,
        private val schedulerBackground: Scheduler = Schedulers.io()
) : ProcesoCerrarSesionUI
{
    private val eventosEstado = BehaviorSubject.createDefault<ProcesoCerrarSesionUI.Estado>(ProcesoCerrarSesionUI.Estado.Esperando)
    override val estado = eventosEstado.hide()!!

    private val eventosCerrarSesion = PublishSubject.create<Unit>()

    override val observadoresInternos = listOf(eventosEstado)

    private val disposables = CompositeDisposable()

    init
    {
        eventosCerrarSesion
            .withLatestFrom(estado, usuarioActual)
            .filter {
                it.second !== ProcesoCerrarSesionUI.Estado.CerrandoSesion
                && !it.third.esVacio
            }
            .doOnNext {
                eventosEstado.onNext(ProcesoCerrarSesionUI.Estado.CerrandoSesion)
            }
            .map { it.third }
            .flatMap {
                intentarCierreDeSesion(it.valor)
            }
            .subscribe {
                eventosEstado.onNext(it)
                if (it == ProcesoCerrarSesionUI.Estado.Resultado.SesionCerrada)
                {
                    usuarioActual.onNext(Opcional.Vacio())
                }
            }.addTo(disposables)
    }

    private fun intentarCierreDeSesion(usuario: Usuario): Observable<ProcesoCerrarSesionUI.Estado.Resultado>
    {
        return Single
            .fromCallable {
                apiUsuariosAPI.logout(usuario.datosUsuario.usuario)
            }
            .subscribeOn(schedulerBackground)
            .map<ProcesoCerrarSesionUI.Estado.Resultado> {
                when (it)
                {
                    RespuestaVacia.Exitosa       ->
                    {
                        ProcesoCerrarSesionUI.Estado.Resultado.SesionCerrada
                    }
                    RespuestaVacia.Error.Timeout ->
                    {
                        ProcesoCerrarSesionUI
                            .Estado
                            .Resultado
                            .ErrorCerrandoSesion("Tiempo de espera al servidor agotado. No se pudo cerrar sesión.")
                    }
                    is RespuestaVacia.Error.Red  ->
                    {
                        ProcesoCerrarSesionUI
                            .Estado
                            .Resultado
                            .ErrorCerrandoSesion("Hubo un error en la conexión y no fue posible contactar al servidor")
                    }
                    is RespuestaVacia.Error.Back ->
                    {
                        ProcesoCerrarSesionUI
                            .Estado
                            .Resultado
                            .ErrorCerrandoSesion("La petición realizada es errónea y no pudo ser procesada.\n${it.error.mensaje}")
                    }
                }
            }.toObservable()
    }

    override fun cerrarSesion()
    {
        eventosCerrarSesion.onNext(Unit)
    }

    override fun reiniciarEstado()
    {
        eventosEstado.onNext(ProcesoCerrarSesionUI.Estado.Esperando)
    }

    override fun finalizarProceso()
    {
        disposables.dispose()
        super.finalizarProceso()
    }
}