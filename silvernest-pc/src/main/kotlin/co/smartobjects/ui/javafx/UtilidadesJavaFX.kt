@file:Suppress("NOTHING_TO_INLINE")

package co.smartobjects.ui.javafx

import co.smartobjects.entidades.excepciones.EntidadMalInicializada
import co.smartobjects.ui.javafx.controladores.genericos.DialogoDeEspera
import co.smartobjects.ui.modelos.ListaFiltrableUI
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.Opcional
import com.jfoenix.controls.*
import com.jfoenix.validation.base.ValidatorBase
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import io.reactivex.*
import io.reactivex.Observable
import io.reactivex.rxjavafx.observers.JavaFxObserver
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.Tooltip
import javafx.scene.layout.Pane
import javafx.stage.PopupWindow
import javafx.util.Duration
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*


internal inline fun Node.bindEstaHabilitado(estaHabilitado: Observable<Boolean>)
{
    estaHabilitado.observarEnFx().subscribe {
        isDisable = !it
    }
}

internal inline fun Node.hacerEscondible()
{
    managedProperty().bind(visibleProperty())
}

internal inline fun JFXCheckBox.inicializarBindingSeleccionarTodos(
        modeloListaFiltrable: ListaFiltrableUI<*>
                                                                  )
{
    inicializarBindingSeleccion(
            modeloListaFiltrable.todosLosHabilitadosEstanSeleccionados,
            { modeloListaFiltrable.seleccionarTodos() },
            { modeloListaFiltrable.deseleccionarTodos() }
                               )
}

internal inline fun JFXCheckBox.inicializarBindingSeleccion(
        observableEstaRealizandoAccion: Observable<Boolean>,
        crossinline accionAlSeleccionar: () -> Unit,
        crossinline accionAlDeseleccionar: () -> Unit
                                                           )
{
    observableEstaRealizandoAccion.observarEnFx().subscribe {
        isSelected = it
    }
    setOnAction {
        if (isSelected)
        {
            accionAlSeleccionar()
        }
        else
        {
            accionAlDeseleccionar()
        }
    }
}

internal inline fun Label.inicializarBindingLabel(observable: Observable<String>)
{
    observable.observarEnFx().subscribe { text = it }
}

internal inline fun Label.inicializarBindingLabelError(observableError: Observable<Opcional<String>>)
{
    inicializarBindingLabel(observableError.map { it.valorUOtro("") })
}

internal inline fun <T> Label.inicializarBindingCambiarMensajeSegunEstado(
        observableEstado: Observable<T>,
        mensajesAMostrar: Set<DialogoDeEspera.InformacionMensajeEspera<T>>
                                                                         )
{
    mensajesAMostrar.forEach { estado ->
        inicializarBindingCambiarMensaje(observableEstado.map { it == estado.estadoAMostrar }, estado.mensajeAMostrar)
    }
}

internal inline fun inicializarLabelMonetario(campo: Label, observableMonto: Observable<Decimal>)
{
    campo
        .textProperty()
        .bind(JavaFxObserver.toBinding(observableMonto.comoDineroFormateado(numeroEspacios = 1).observarEnFx()))
}

internal inline fun Label.inicializarBindingCambiarMensaje(observableEstaRealizandoAccion: Observable<Boolean>, mensajeAMostrar: String)
{
    observableEstaRealizandoAccion.observarEnFx().subscribe {
        if (it)
        {
            text = mensajeAMostrar
        }
    }
}

internal inline fun Label.inicializarBindingCambiarMensaje(observableMensaje: Observable<String>)
{
    observableMensaje.observarEnFx().subscribe {
        text = it
    }
}

internal inline fun <T> Node.inicializarBindingMostrarNodeSegunEstadoAccion(observableEstado: Observable<T>, estadosAccion: Set<T>)
{
    inicializarBindingEsVisible(observableEstado.map { estadosAccion.contains(it) })
}

internal inline fun Node.inicializarBindingEsVisible(observableEstaRealizandoAccion: Observable<Boolean>)
{
    observableEstaRealizandoAccion.observarEnFx().subscribe {
        isVisible = it
    }
}


internal inline fun JFXButton.inicializarBindingAccion(observablePuedeRealizarAccion: Observable<Boolean>, crossinline accion: () -> Unit)
{
    setOnAction {
        accion()
    }
    observablePuedeRealizarAccion.observarEnFx().subscribe {
        disableProperty().set(!it)
    }
}

internal inline fun <T> JFXComboBox<T>.inicializarBindingCampo(
        valorInicial: T,
        valores: List<T>,
        observableCampo: Observable<T>,
        crossinline asignarValorAObservable: (T) -> Unit
                                                              )
{
    items.addAll(valores)
    valueProperty().addListener { _, oldVal, newVal ->
        if (oldVal != newVal)
        {
            asignarValorAObservable(newVal)
        }
    }
    observableCampo.observarEnFx().subscribe {
        if (value != it)
        {
            value = it
        }
    }
    value = valorInicial
}

inline fun JFXComboBox<*>.alCambiarDeValor(
        crossinline accion: () -> Unit
                                          )
{
    valueProperty().addListener { _, oldVal, newVal ->
        if (oldVal != newVal)
        {
            accion()
        }
    }
}

internal inline fun <T, L : Iterable<T>> Pane.inicializarBindingListaTransformandoANodo(
        observableLista: Observable<L>,
        crossinline transformacionANodo: (T) -> Node
                                                                                       )
{
    inicializarBindingLista(observableLista.map { it.map { transformacionANodo(it) } })
}

internal inline fun <T : Node> Pane.inicializarBindingLista(observableLista: Observable<List<T>>)
{
    observableLista.observarEnFx().subscribe { children.setAll(it) }
}

inline fun Node.alPerderFoco(crossinline accion: () -> Unit)
{
    focusedProperty().addListener { _, teniaFoco, tieneFoco ->
        if (teniaFoco && !tieneFoco)
        {
            accion()
        }
    }
}

internal inline fun JFXTextField.inicializarBindingCampoEntero(
        valorInicial: String,
        observableCampo: Observable<Notification<String>>,
        crossinline asignarValorAObservable: (String) -> Unit,
        crossinline accion: () -> Unit,
        longitudMaxima: Int? = null
                                                              )
{
    inicializarBinding(valorInicial, observableCampo, asignarValorAObservable, accion, { it.filter { it.isDigit() } }, longitudMaxima)
}

internal inline fun JFXTextField.inicializarBindingCampoNumericoPositivo(
        valorInicial: String,
        observableCampo: Observable<Notification<String>>,
        crossinline asignarValorAObservable: (String) -> Unit,
        crossinline accion: () -> Unit,
        longitudMaxima: Int? = null
                                                                        )
{
    inicializarBinding(valorInicial, observableCampo, asignarValorAObservable, accion, { it.filter { it.isDigit() || it == '.' } }, longitudMaxima, charArrayOf('.'))
}

internal inline fun JFXTextField.inicializarBindingCampoRequerido(
        valorInicial: String,
        observableCampo: Observable<Notification<String>>,
        crossinline asignarValorAObservable: (String) -> Unit,
        crossinline accion: () -> Unit,
        longitudMaxima: Int? = null
                                                                 )
{
    inicializarBinding(valorInicial, observableCampo, asignarValorAObservable, accion, { it.trim() }, longitudMaxima)
}


inline fun cortarALongitudMaxima(longitudMaxima: Int?, valor: String): String
{
    return if (longitudMaxima != null && valor.length > longitudMaxima)
    {
        valor.substring(0, longitudMaxima)
    }
    else
    {
        valor
    }
}

internal inline fun JFXTextField.inicializarBinding(
        valorInicial: String,
        observableCampo: Observable<Notification<String>>,
        crossinline asignarValorAObservable: (String) -> Unit,
        crossinline accion: () -> Unit,
        crossinline corregirEnError: (String) -> String,
        longitudMaxima: Int? = null,
        caracteresAIgnorar: CharArray = charArrayOf(' ')
                                                   )
{
    textProperty().addListener { _, _, newVal ->
        asignarValorAObservable(cortarALongitudMaxima(longitudMaxima, newVal))
    }
    setValidators(ValidadorSegunObservableNotification(observableCampo))
    observableCampo.observarEnFx().subscribe {
        if (it.isOnNext)
        {
            val valor = cortarALongitudMaxima(longitudMaxima, it.value!!)
            val valorActualAComparar = if (isFocused) text.trimEnd(*caracteresAIgnorar) else text
            if (valorActualAComparar != valor)
            {
                text = valor
                end()
            }
        }
        else if (it.isOnError)
        {
            text = cortarALongitudMaxima(longitudMaxima, corregirEnError(text))
            end()
        }
    }
    observableCampo.subscribe {
        validate()
    }
    focusedProperty().addListener { _, oldVal, newVal ->
        if (oldVal && !newVal)
        {
            asignarValorAObservable(text)
        }
    }
    setOnAction {
        accion()
    }
    if (valorInicial != text)
    {
        text = valorInicial
    }
    else
    {
        asignarValorAObservable(valorInicial)
    }
    resetValidation()
}

internal inline fun JFXPasswordField.inicializarBindingCampoRequerido(
        observableCampo: Observable<Notification<CharArray>>,
        crossinline asignarValorAObservable: (CharArray) -> Unit,
        crossinline accion: () -> Unit
                                                                     )
{
    textProperty().addListener { _, oldVal, newVal ->
        if (oldVal != newVal)
        {
            asignarValorAObservable(newVal.toCharArray())
        }
    }
    setValidators(ValidadorSegunObservableNotification(observableCampo))
    validarAlCambiarOPerderFoco()
    observableCampo.observarEnFx().subscribe {
        if (it.isOnNext)
        {
            val valor = String(it.value!!)
            if (text != valor)
            {
                text = valor
            }
        }
        else if (it.isOnError && text.isNotEmpty())
        {
            clear()
        }
    }
    setOnAction {
        accion()
    }
    asignarValorAObservable(charArrayOf())
}

internal inline fun JFXPasswordField.validarAlCambiarOPerderFoco()
{
    textProperty().addListener { _, _, _ ->
        validate()
    }
    focusedProperty().addListener { _, _, newVal ->
        if (!newVal)
        {
            validate()
        }
    }
}

internal class ValidadorSegunObservableNotification<out T>(observable: Observable<Notification<T>>) : ValidatorBase()
{
    private var ultimoError: String? = null

    init
    {
        observable.subscribe {
            if (it.isOnError)
            {
                val error = it.error
                ultimoError = if (error is EntidadMalInicializada)
                {
                    error.descripcionDeLaRestriccion
                }
                else
                {
                    error?.message ?: "Inválido"
                }
            }
            else
            {
                ultimoError = null
            }
        }
    }

    override fun getIcon(): Node
    {
        return FontAwesomeIconView(FontAwesomeIcon.WARNING).apply {
            styleClass.add("error-icon")
            style += "-fx-font-family: FontAwesome"
        }
    }

    override fun eval()
    {
        val error = ultimoError
        if (error != null)
        {
            setMessage(error)
            hasErrors.set(true)
        }
        else
        {
            setMessage("")
            hasErrors.set(false)
        }
    }
}


internal val tooltipBehaviorPersonalizado =
        Tooltip()
            .javaClass.declaredClasses.first { it.simpleName == "TooltipBehavior" }
            .getDeclaredConstructor(
                    Duration::class.java,
                    Duration::class.java,
                    Duration::class.java,
                    Boolean::class.javaPrimitiveType)
            .apply { isAccessible = true }
            .newInstance(
                    Duration(250.0), // openDelay
                    Duration(5000.0), // visibleDuration
                    Duration(200.0), // closeDelay
                    false) // hideOnExit

internal fun inicializarTooltipConTexto(texto: String): Tooltip
{
    val formaTooltip = "M-562.9,967.8c0,0.8-0.7,1.5-1.5,1.5h-21.1c-0.8,0-1.5-0.7-1.5-1.5v-6.6v-6.6c0-0.8,0.7-1.5,1.5-1.5l0,0l-1.5-2.3l6.1,2.3h16.5c0.8,0,1.5,0.7,1.5,1.5"
    //        val formaTooltip = "M24 1h-24v16.981h4v5.019l7-5.019h13z"
    val tooltip = Tooltip(texto)
    tooltip.style = """-fx-font-size: 20px;-fx-background-radius: 7 7 7 7; -fx-shape: "$formaTooltip"; -fx-background-color: -acento;"""
    tooltip.anchorLocation = PopupWindow.AnchorLocation.CONTENT_TOP_LEFT

    try
    {
        with(tooltip.javaClass.getDeclaredField("BEHAVIOR"))
        {
            isAccessible = true
            set(tooltip, tooltipBehaviorPersonalizado)
        }
    }
    catch (e: Exception)
    {
    }

    return tooltip
}

internal val formateadorDineroSinSimbolo =
        NumberFormat.getCurrencyInstance(Locale.getDefault())
            .apply {
                maximumFractionDigits = 0
                val simbolos = (this as DecimalFormat).decimalFormatSymbols
                simbolos.currencySymbol = ""
                decimalFormatSymbols = simbolos
            }

internal val formateadorDineroConNegativo =
        NumberFormat.getCurrencyInstance(Locale.getDefault())
            .apply {
                maximumFractionDigits = 0
                this as DecimalFormat
                negativePrefix = "-$"
                negativeSuffix = ""
            }


internal inline fun Decimal.comoDineroFormateado(numeroEspacios: Int = 0) = "$${" ".repeat(numeroEspacios)}${formateadorDineroSinSimbolo.format(valor)}"
internal inline fun Observable<Decimal>.comoDineroFormateado(numeroEspacios: Int = 0) = map { it.comoDineroFormateado(numeroEspacios) }
internal inline fun Decimal.comoDineroConNegativosFormateado() = formateadorDineroConNegativo.format(valor)
internal inline fun Observable<Decimal>.comoDineroConNegativosFormateado() = map { it.comoDineroConNegativosFormateado() }

internal fun <T> Flowable<T>.observarEnFx(): Flowable<T> = observeOn(JavaFxScheduler.platform())
internal fun <T> Observable<T>.observarEnFx(): Observable<T> = observeOn(JavaFxScheduler.platform())
internal fun <T> Single<T>.observarEnFx(): Single<T> = observeOn(JavaFxScheduler.platform())
internal fun <T> Maybe<T>.observarEnFx(): Maybe<T> = observeOn(JavaFxScheduler.platform())

internal fun <T> Flowable<T>.usarSchedulersEnUI(scheduler: Scheduler): Flowable<T> = subscribeOn(scheduler).observarEnFx()
internal fun <T> Observable<T>.usarSchedulersEnUI(scheduler: Scheduler): Observable<T> = subscribeOn(scheduler).observarEnFx()
internal fun <T> Single<T>.usarSchedulersEnUI(scheduler: Scheduler): Single<T> = subscribeOn(scheduler).observarEnFx()
internal fun <T> Maybe<T>.usarSchedulersEnUI(scheduler: Scheduler): Maybe<T> = subscribeOn(scheduler).observarEnFx()