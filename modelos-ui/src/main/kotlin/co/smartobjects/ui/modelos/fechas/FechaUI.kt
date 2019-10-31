package co.smartobjects.ui.modelos.fechas

import co.smartobjects.campos.CampoEntidad
import co.smartobjects.entidades.excepciones.EntidadMalInicializada
import co.smartobjects.ui.modelos.ModeloUI
import co.smartobjects.ui.modelos.transformarAEntidadUIEnvolviendoErrores
import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.rxkotlin.Observables
import io.reactivex.subjects.BehaviorSubject
import org.threeten.bp.DateTimeException
import org.threeten.bp.LocalDate

interface FechaUI: ModeloUI
{
    val dia: Observable<Notification<Int>>
    val mes: Observable<Notification<Int>>
    val año: Observable<Notification<Int>>
    val fecha: Observable<Notification<LocalDate>>
    val esFechaValida: Observable<Boolean>

    @Throws(IllegalStateException::class)
    fun aFecha(): LocalDate

    fun cambiarDia(nuevoDia: String)
    fun cambiarMes(nuevoMes: String)
    fun cambiarAño(nuevoAño: String)

    fun asignarFecha(nuevaFecha: LocalDate)

    override val modelosHijos: List<ModeloUI>
        get() = listOf()
}

internal class FechaUIConSujetos(private val intentarCrearCampoFecha: (LocalDate) -> CampoEntidad<*, LocalDate>): FechaUI
{
    private val sujetoDia: BehaviorSubject<Notification<Int>> = BehaviorSubject.create<Notification<Int>>()
    private val sujetoMes: BehaviorSubject<Notification<Int>> = BehaviorSubject.create<Notification<Int>>()
    private val sujetoAño: BehaviorSubject<Notification<Int>> = BehaviorSubject.create<Notification<Int>>()
    private val sujetoFecha: BehaviorSubject<Notification<LocalDate>> = BehaviorSubject.create<Notification<LocalDate>>()

    override val dia: Observable<Notification<Int>> = sujetoDia
    override val mes: Observable<Notification<Int>> = sujetoMes
    override val año: Observable<Notification<Int>> = sujetoAño
    override val fecha: Observable<Notification<LocalDate>> = sujetoFecha
    override val esFechaValida: Observable<Boolean> = Observables.combineLatest(
            dia,
            mes,
            año,
            fecha,
            {
                posibleDia, posibleMes, posibleAño, posibleFecha ->
                !posibleDia.isOnError && !posibleMes.isOnError && !posibleAño.isOnError && !posibleFecha.isOnError
            }
                                                                               ).distinctUntilChanged()
    override val observadoresInternos: List<Observer<*>> = listOf(
            sujetoDia,
            sujetoMes,
            sujetoAño,
            sujetoFecha
                                                                 )

    override fun cambiarDia(nuevoDia: String)
    {
        val dia = nuevoDia.toIntOrNull()
        if(dia == null || dia < 1 || dia > 31)
        {
            sujetoDia.onNext(Notification.createOnError(Exception("Inválido")))
            sujetoFecha.onNext(Notification.createOnError(Exception("Fecha inválida")))
        }
        else
        {
            sujetoDia.onNext(Notification.createOnNext(dia))
            cambiarFechaSegunCampos(posibleDia = dia)
        }
    }

    override fun cambiarMes(nuevoMes: String)
    {
        val mes = nuevoMes.toIntOrNull()
        if(mes == null || mes < 1 || mes > 12)
        {
            sujetoMes.onNext(Notification.createOnError(Exception("Inválido")))
            sujetoFecha.onNext(Notification.createOnError(Exception("Fecha inválida")))
        }
        else
        {
            sujetoMes.onNext(Notification.createOnNext(mes))
            cambiarFechaSegunCampos(posibleMes = mes)
        }
    }

    override fun cambiarAño(nuevoAño: String)
    {
        val año = nuevoAño.toIntOrNull()
        if(año == null)
        {
            sujetoAño.onNext(Notification.createOnError(Exception("Inválido")))
            sujetoFecha.onNext(Notification.createOnError(Exception("Fecha inválida")))
        }
        else
        {
            sujetoAño.onNext(Notification.createOnNext(año))
            cambiarFechaSegunCampos(posibleAño = año)
        }
    }

    private fun cambiarFechaSegunCampos(posibleDia: Int? = null, posibleMes: Int? = null, posibleAño: Int? = null)
    {
        val dia = posibleDia ?: sujetoDia.value?.value
        dia?.run {
            val mes = posibleMes ?: sujetoMes.value?.value
            mes?.run {
                val año = posibleAño ?: sujetoAño.value?.value
                año?.run {
                    try
                    {
                        val fecha = LocalDate.of(año, mes, dia)
                        val campo = intentarCrearCampoFecha(fecha)
                        sujetoFecha.onNext(Notification.createOnNext(campo.valor))
                    }
                    catch(e: DateTimeException)
                    {
                        sujetoFecha.onNext(Notification.createOnError(Exception("Fecha inválida")))
                    }
                    catch (e: EntidadMalInicializada)
                    {
                        sujetoFecha.onNext(Notification.createOnError(e))
                    }
                }
            }
        }
    }

    override fun aFecha(): LocalDate
    {
        return transformarAEntidadUIEnvolviendoErrores {
            sujetoFecha.value!!.value!!
        }
    }

    override fun asignarFecha(nuevaFecha: LocalDate)
    {
        sujetoDia.onNext(Notification.createOnNext(nuevaFecha.dayOfMonth))
        sujetoMes.onNext(Notification.createOnNext(nuevaFecha.monthValue))
        sujetoAño.onNext(Notification.createOnNext(nuevaFecha.year))
        cambiarFechaSegunCampos(posibleDia = nuevaFecha.dayOfMonth, posibleMes = nuevaFecha.monthValue, posibleAño = nuevaFecha.year)
    }
}