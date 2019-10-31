package co.smartobjects.ui.modelos

import co.smartobjects.entidades.fondos.precios.ImpuestoSoloTasa
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.entidades.fondos.precios.PrecioCompleto
import co.smartobjects.entidades.operativas.compras.CreditoFondo
import co.smartobjects.entidades.operativas.compras.CreditoPaquete
import co.smartobjects.nfc.lectorestags.ILectorTag
import co.smartobjects.nfc.operacionessobretags.OperacionesBase
import co.smartobjects.nfc.operacionessobretags.OperacionesCompuestas
import co.smartobjects.nfc.operacionessobretags.ResultadoLecturaNFC
import co.smartobjects.nfc.tags.ITag
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.spy
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.Year
import org.threeten.bp.ZonedDateTime

internal fun crearCreditoFondo(indice: Int, idPersona: Long, autoId: Boolean = false)
        : CreditoFondo
{
    val precioCompleto = PrecioCompleto(Precio(Decimal.DIEZ * (1 + indice), 1), ImpuestoSoloTasa(1, 1, Decimal(15)))
    return CreditoFondo(
            1,
            if (autoId) indice.toLong() + 1 else null,
            Decimal.UNO * (1 + indice),
            precioCompleto.precioConImpuesto,
            precioCompleto.valorImpuesto,
            ZonedDateTime.of(LocalDate.of(Year.now(ZONA_HORARIA_POR_DEFECTO).value + indice, 1, 1), LocalTime.of(5, 5), ZONA_HORARIA_POR_DEFECTO),
            ZonedDateTime.of(LocalDate.of(Year.now(ZONA_HORARIA_POR_DEFECTO).value + 2 + indice, 1, 1), LocalTime.of(5, 5), ZONA_HORARIA_POR_DEFECTO),
            false,
            "Taquilla",
            "Un usuario",
            idPersona,
            indice.toLong() + 3,
            "código externo fondo $indice",
            indice.toLong() + 4,
            "uuid-pc",
            indice.toLong() + 5,
            indice.toLong() + 6
                       )
}

internal fun crearCreditoPaquete(indice: Int, idPersona: Long, autoIdCreditoFondo: Boolean = false)
        : CreditoPaquete
{
    return CreditoPaquete(
            indice.toLong(),
            "código externo paquete $indice",
            List(2) {
                crearCreditoFondo((it + 1 + indice) * 10, idPersona, autoIdCreditoFondo)
            }
                         )
}

internal class OperacionesCompuestasMockeadas private constructor(operacionesBase: OperacionesBase<*, *>)
    : OperacionesCompuestas<ITag, ILectorTag<*>>(operacionesBase)
{
    companion object
    {
        val UIDTagPrueba = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)

        private val mockLector = mockConDefaultAnswer(ILectorTag::class.java).also {
            doNothing().`when`(it).conectar()
            doNothing().`when`(it).desconectar()
            doReturn(UIDTagPrueba).`when`(it).darUID()
        }

        private val operacionesBase = mockConDefaultAnswer(OperacionesBase::class.java).also {
            doReturn(mockLector).`when`(it).lector
            doReturn(true).`when`(it).autenticarConLlaveProveedor()
            doReturn(true).`when`(it).autenticarConLlavePorDefecto()
            doReturn(true).`when`(it).estaActivadaLaAutenticacion()
            doReturn(byteArrayOf()).`when`(it).leerTodosLosDatosDeUsuario()
            doNothing().`when`(it).activarAutenticacion()
            doNothing().`when`(it).escribirDatosDeUsuario(cualquiera())
            doNothing().`when`(it).desactivarAutenticacion()
            doNothing().`when`(it).borrarDatosDeUsuario()
        }

        fun crear(): OperacionesCompuestasMockeadas
        {
            return spy(OperacionesCompuestasMockeadas(operacionesBase)).also {
                doReturn(true).`when`(it).escribirTag(cualquiera())
                doReturn(UIDTagPrueba).`when`(it).darUID()
                doReturn(false).`when`(it).borrarTag()
                doReturn(ResultadoLecturaNFC.TagVacio).`when`(it).leerTag()
            }
        }
    }

    var retornoDeEscribirTag: Boolean = true
        set(value)
        {
            field = value
            doReturn(value).`when`(this).escribirTag(cualquiera())
        }
    var retornoDeDarUID: ByteArray = UIDTagPrueba
        set(value)
        {
            field = value
            doReturn(value).`when`(this).darUID()
        }
    var retornoDeBorrarTag: Boolean = false
        set(value)
        {
            field = value
            doReturn(value).`when`(this).borrarTag()
        }
    var retornoDeLeerTag: ResultadoLecturaNFC = ResultadoLecturaNFC.TagVacio
        set(value)
        {
            field = value
            doReturn(value).`when`(this).leerTag()
        }
}