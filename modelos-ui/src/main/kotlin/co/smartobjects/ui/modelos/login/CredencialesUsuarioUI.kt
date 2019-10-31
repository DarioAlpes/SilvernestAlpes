package co.smartobjects.ui.modelos.login

import co.smartobjects.entidades.usuarios.Usuario
import co.smartobjects.ui.modelos.*
import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.rxkotlin.Observables
import io.reactivex.subjects.BehaviorSubject

interface CredencialesUsuarioUI : ModeloUI
{
    val usuario: Observable<Notification<String>>
    val contraseña: Observable<Notification<CharArray>>
    val sonCredencialesValidas: Observable<Boolean>

    fun cambiarUsuario(nuevoUsuario: String)
    fun cambiarContraseña(nuevaContraseña: CharArray)
    @Throws(IllegalStateException::class)
    fun aCredencialesUsuario(): Usuario.CredencialesUsuario

    override val modelosHijos: List<ModeloUI>
        get() = listOf()
}

internal class CredencialesUsuarioUIConSujetos : CredencialesUsuarioUI
{
    private val sujetoUsuario: BehaviorSubject<Notification<Usuario.DatosUsuario.CampoUsuario>> = BehaviorSubject.create<Notification<Usuario.DatosUsuario.CampoUsuario>>()
    private val sujetoContraseña: BehaviorSubject<Notification<Usuario.CredencialesUsuario.CampoContraseña>> = BehaviorSubject.create<Notification<Usuario.CredencialesUsuario.CampoContraseña>>()

    override val usuario: Observable<Notification<String>> = sujetoUsuario.mapNotificationValorCampo()
    override val contraseña: Observable<Notification<CharArray>> = sujetoContraseña.mapNotificationValorCampo()
    override val sonCredencialesValidas: Observable<Boolean> = Observables.combineLatest(
            usuario,
            contraseña,
            { posibleUsuario, posibleContraseña -> !posibleUsuario.isOnError && !posibleContraseña.isOnError }
                                                                                        )
    override val observadoresInternos: List<Observer<*>> = listOf(sujetoUsuario, sujetoContraseña)

    override fun cambiarUsuario(nuevoUsuario: String)
    {
        sujetoUsuario.crearYEmitirEntidadNegocioConPosibleError { Usuario.DatosUsuario.CampoUsuario(nuevoUsuario) }
    }

    override fun cambiarContraseña(nuevaContraseña: CharArray)
    {
        sujetoContraseña.value?.value?.limpiarValor()
        sujetoContraseña.crearYEmitirEntidadNegocioConPosibleError { Usuario.CredencialesUsuario.CampoContraseña(nuevaContraseña) }
    }

    override fun aCredencialesUsuario(): Usuario.CredencialesUsuario
    {
        return transformarAEntidadUIEnvolviendoErrores {
            Usuario.CredencialesUsuario(sujetoUsuario.valorDeCampo(), sujetoContraseña.valorDeCampo())
        }
    }

    override fun finalizarProceso()
    {
        sujetoContraseña.value?.value?.limpiarValor()
        super.finalizarProceso()
    }
}