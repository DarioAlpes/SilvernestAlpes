package co.smartobjects.ui.modelos.registropersonas

import co.smartobjects.entidades.personas.DocumentoCompleto
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.ui.modelos.*
import co.smartobjects.ui.modelos.fechas.FechaUI
import co.smartobjects.ui.modelos.fechas.FechaUIConSujetos
import co.smartobjects.utilidades.Opcional
import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.rxkotlin.Observables
import io.reactivex.subjects.BehaviorSubject

interface PersonaUI: ModeloUI
{
    val idCliente: Long
    val id: Observable<Opcional<Long>>
    val nombreCompleto: Observable<Notification<String>>
    val tipoDocumento: Observable<Persona.TipoDocumento>
    val numeroDocumento: Observable<Notification<String>>
    val genero: Observable<Persona.Genero>
    val fechaNacimiento: FechaUI
    val categoria: Observable<Persona.Categoria>
    val afiliacion: Observable<Persona.Afiliacion>
    val esPersonaValida: Observable<Boolean>
    val empresa: Observable<Notification<String>>
    val nitEmpresa: Observable<Notification<String>>
    val tipo: Observable<Persona.Tipo>

    fun anularId()
    fun cambiarNombreCompleto(nuevoNombreCompleto: String)
    fun cambiarTipoDocumento(nuevoTipoDocumento: Persona.TipoDocumento)
    fun cambiarNumeroDocumento(nuevoNumeroDocumento: String)
    fun cambiarGenero(nuevoGenero: Persona.Genero)
    fun cambiarCategoria(nuevaCategoria: Persona.Categoria)
    fun cambiarAfiliacion(nuevaAfiliacion: Persona.Afiliacion)
    fun cambiarEmpresa(nuevaEmpresa: String)
    fun cambiarNitEmpresa(nuevoNitEmpresa: String)
    fun cambiarTipo(nuevoTipo: Persona.Tipo)

    fun asignarPersona(nuevaPersona: Persona)

    @Throws(IllegalStateException::class)
    fun aPersona(): Persona

    @Throws(IllegalStateException::class)
    fun darDocumentoCompleto(): DocumentoCompleto

    override val modelosHijos: List<ModeloUI>
        get() = listOf(fechaNacimiento)
}

internal class PersonaUIConSujetos internal constructor(override val idCliente: Long, override val fechaNacimiento: FechaUI): PersonaUI
{
    constructor(idCliente: Long): this(idCliente, FechaUIConSujetos({ Persona.CampoFechaNacimiento(it) }))

    private val sujetoId: BehaviorSubject<Opcional<Long>> = BehaviorSubject.createDefault<Opcional<Long>>(Opcional.Vacio())
    private val sujetoNombreCompleto: BehaviorSubject<Notification<Persona.CampoNombreCompleto>> = BehaviorSubject.create<Notification<Persona.CampoNombreCompleto>>()
    private val sujetoTipoDocumento: BehaviorSubject<Persona.TipoDocumento> = BehaviorSubject.create<Persona.TipoDocumento>()
    private val sujetoNumeroDocumento: BehaviorSubject<Notification<Persona.CampoNumeroDocumento>> = BehaviorSubject.create<Notification<Persona.CampoNumeroDocumento>>()
    private val sujetoGenero: BehaviorSubject<Persona.Genero> = BehaviorSubject.create<Persona.Genero>()
    private val sujetoCategoria: BehaviorSubject<Persona.Categoria> = BehaviorSubject.create<Persona.Categoria>()
    private val sujetoAfiliacion: BehaviorSubject<Persona.Afiliacion> = BehaviorSubject.create<Persona.Afiliacion>()
    private val sujetoEmpresa: BehaviorSubject<Notification<Persona.CampoEmpresa>> = BehaviorSubject.create<Notification<Persona.CampoEmpresa>>()
    private val sujetoNitEmpresa: BehaviorSubject<Notification<Persona.CampoNitEmpresa>> = BehaviorSubject.create<Notification<Persona.CampoNitEmpresa>>()
    private val sujetoTipo: BehaviorSubject<Persona.Tipo> = BehaviorSubject.create<Persona.Tipo>()

    override val id: Observable<Opcional<Long>> = sujetoId
    override val nombreCompleto: Observable<Notification<String>> = sujetoNombreCompleto.mapNotificationValorCampo()
    override val tipoDocumento: Observable<Persona.TipoDocumento> = sujetoTipoDocumento
    override val numeroDocumento: Observable<Notification<String>> = sujetoNumeroDocumento.mapNotificationValorCampo()
    override val genero: Observable<Persona.Genero> = sujetoGenero
    override val categoria: Observable<Persona.Categoria> = sujetoCategoria
    override val afiliacion: Observable<Persona.Afiliacion> = sujetoAfiliacion
    override val empresa: Observable<Notification<String>> = sujetoEmpresa.mapNotificationValorCampo()
    override val nitEmpresa: Observable<Notification<String>> = sujetoNitEmpresa.mapNotificationValorCampo()
    override val tipo: Observable<Persona.Tipo> = sujetoTipo
    override val esPersonaValida: Observable<Boolean> = Observables.combineLatest(
            nombreCompleto,
            tipoDocumento,
            numeroDocumento,
            genero,
            fechaNacimiento.esFechaValida,
            categoria,
            afiliacion,
            {
                posibleNombre, _, posibleNumeroDocumento, _, esFechaValida, _, _ ->
                !posibleNombre.isOnError && !posibleNumeroDocumento.isOnError && esFechaValida
            }
                                                                                 )
    override val observadoresInternos: List<Observer<*>> = listOf(
            sujetoNombreCompleto,
            sujetoTipoDocumento,
            sujetoNumeroDocumento,
            sujetoGenero,
            sujetoCategoria,
            sujetoAfiliacion,
            sujetoEmpresa,
            sujetoNitEmpresa,
            sujetoTipo
                                                                 )


    override fun anularId()
    {
        sujetoId.onNext(Opcional.Vacio())
    }

    override fun cambiarNombreCompleto(nuevoNombreCompleto: String)
    {
        sujetoNombreCompleto.crearYEmitirEntidadNegocioConPosibleError { Persona.CampoNombreCompleto(nuevoNombreCompleto) }
    }

    override fun cambiarTipoDocumento(nuevoTipoDocumento: Persona.TipoDocumento)
    {
        sujetoTipoDocumento.onNext(nuevoTipoDocumento)
    }

    override fun cambiarNumeroDocumento(nuevoNumeroDocumento: String)
    {
        sujetoNumeroDocumento.crearYEmitirEntidadNegocioConPosibleError { Persona.CampoNumeroDocumento(nuevoNumeroDocumento) }
    }

    override fun cambiarGenero(nuevoGenero: Persona.Genero)
    {
        sujetoGenero.onNext(nuevoGenero)
    }

    override fun cambiarCategoria(nuevaCategoria: Persona.Categoria)
    {
        sujetoCategoria.onNext(nuevaCategoria)
    }

    override fun cambiarAfiliacion(nuevaAfiliacion: Persona.Afiliacion)
    {
        sujetoAfiliacion.onNext(nuevaAfiliacion)
    }

    override fun cambiarEmpresa(nuevaEmpresa: String)
    {
        sujetoEmpresa.crearYEmitirEntidadNegocioConPosibleError { Persona.CampoEmpresa(nuevaEmpresa) }
    }

    override fun cambiarNitEmpresa(nuevoNitEmpresa: String)
    {
        sujetoNitEmpresa.crearYEmitirEntidadNegocioConPosibleError { Persona.CampoNitEmpresa(nuevoNitEmpresa) }
    }

    override fun cambiarTipo(nuevoTipo: Persona.Tipo)
    {
        sujetoTipo.onNext(nuevoTipo)
    }

    override fun asignarPersona(nuevaPersona: Persona)
    {
        sujetoId.onNext(Opcional.DeNullable(nuevaPersona.id))
        cambiarNombreCompleto(nuevaPersona.nombreCompleto)
        cambiarTipoDocumento(nuevaPersona.tipoDocumento)
        cambiarNumeroDocumento(nuevaPersona.numeroDocumento)
        cambiarGenero(nuevaPersona.genero)
        fechaNacimiento.asignarFecha(nuevaPersona.fechaNacimiento)
        cambiarCategoria(nuevaPersona.categoria)
        cambiarAfiliacion(nuevaPersona.afiliacion)
        cambiarEmpresa(nuevaPersona.empresa)
        cambiarNitEmpresa(nuevaPersona.nitEmpresa)
        cambiarTipo(nuevaPersona.tipo)
    }

    override fun aPersona(): Persona
    {
        return transformarAEntidadUIEnvolviendoErrores{
            Persona(
                    idCliente,
                    sujetoId.value!!.valorUOtro(null),
                    sujetoNombreCompleto.valorDeCampo(),
                    sujetoTipoDocumento.value!!,
                    sujetoNumeroDocumento.valorDeCampo(),
                    sujetoGenero.value!!,
                    fechaNacimiento.aFecha(),
                    sujetoCategoria.value!!,
                    sujetoAfiliacion.value!!,
                    null,
                    sujetoEmpresa.valorDeCampo(),
                    sujetoNitEmpresa.valorDeCampo(),
                    sujetoTipo.value!!
                   )
        }
    }

    override fun darDocumentoCompleto(): DocumentoCompleto
    {
        return transformarAEntidadUIEnvolviendoErrores{
            DocumentoCompleto(
                    sujetoTipoDocumento.value!!,
                    sujetoNumeroDocumento.valorDeCampo()
                                  )
        }
    }
}