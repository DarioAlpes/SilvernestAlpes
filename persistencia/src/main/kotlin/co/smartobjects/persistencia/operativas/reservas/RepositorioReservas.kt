package co.smartobjects.persistencia.operativas.reservas

import co.smartobjects.entidades.operativas.EntidadTransaccional
import co.smartobjects.entidades.operativas.compras.CreditoFondo
import co.smartobjects.entidades.operativas.ordenes.LoteDeOrdenes
import co.smartobjects.entidades.operativas.ordenes.Orden
import co.smartobjects.entidades.operativas.ordenes.Transaccion
import co.smartobjects.entidades.operativas.reservas.Reserva
import co.smartobjects.entidades.operativas.reservas.SesionDeManilla
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.persistencia.*
import co.smartobjects.persistencia.basederepositorios.*
import co.smartobjects.persistencia.excepciones.*
import co.smartobjects.persistencia.fondos.combinadores.repositorioEntidadDao
import co.smartobjects.persistencia.operativas.ColumnasTransaccionales
import co.smartobjects.persistencia.operativas.RepositorioTransaccional
import co.smartobjects.persistencia.operativas.compras.CompraDAO
import co.smartobjects.persistencia.operativas.compras.CreditoFondoDAO
import co.smartobjects.persistencia.operativas.ordenes.LoteDeOrdenesConParametrosEliminacion
import co.smartobjects.persistencia.operativas.ordenes.OrdenDAO
import co.smartobjects.persistencia.operativas.ordenes.TransaccionDAO
import co.smartobjects.persistencia.operativas.ordenes.darCreableDeOrdenesDesdeLoteDeOrdenesSinTransaccion
import co.smartobjects.persistencia.persistoresormlite.ZonedDateTimeThreeTenType
import co.smartobjects.persistencia.personas.PersonaDAO
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import com.j256.ormlite.dao.RawRowObjectMapper
import com.j256.ormlite.field.DataType
import com.j256.ormlite.field.SqlType
import com.j256.ormlite.support.DatabaseResults
import org.threeten.bp.ZonedDateTime


interface RepositorioReservas
    : CreadorRepositorio<Reserva>,
      Creable<Reserva>,
      Listable<Reserva>,
      Buscable<Reserva, String>,
      ActualizablePorCamposIndividuales<Reserva, String>,
      EliminablePorId<Reserva, String>


private class ValidadorCreacionReserva(
        private val parametrosDaoPersona: ParametrosParaDAOEntidadDeCliente<PersonaDAO, Long>,
        private val parametrosDaoCompra: ParametrosParaDAOEntidadDeCliente<CompraDAO, String>,
        private val parametrosDaoCreditoFondo: ParametrosParaDAOEntidadDeCliente<CreditoFondoDAO, Long>)
    : ValidadorRestriccionCreacion<Reserva>
{
    private fun validarQueExistanLasPersonas(idCliente: Long, sesionesDeManilla: List<SesionDeManilla>)
    {
        val idsPersonas = sesionesDeManilla.map { it.idPersona }.toSet()

        val numeroDePersonasEncontradas =
                parametrosDaoPersona[idCliente].dao
                    .queryBuilder()
                    .apply { where().`in`(PersonaDAO.COLUMNA_ID, idsPersonas) }
                    .orderBy(PersonaDAO.COLUMNA_ID, true)
                    .selectColumns(PersonaDAO.COLUMNA_ID)
                    .query()
                    .map { it.id!! }
                    .toMutableSet()

        if (idsPersonas.size != numeroDePersonasEncontradas.size)
        {
            for (idPersona in idsPersonas)
            {
                if (!numeroDePersonasEncontradas.contains(idPersona))
                {
                    throw ErrorDeLlaveForanea(idPersona, Persona.NOMBRE_ENTIDAD)
                }
            }
        }
    }

    override fun validar(idCliente: Long, entidadACrear: Reserva)
    {
        if (parametrosDaoPersona.configuracion.llavesForaneasActivadas)
        {
            validarQueExistanLasPersonas(idCliente, entidadACrear.sesionesDeManilla)
        }

        val personaVsCreditos = entidadACrear.sesionesDeManilla.associateBy({ it.idPersona }, { it.idsCreditosCodificados })
        val idsTotalesCreditosACodificar = personaVsCreditos.values.flatMap { it }

        val queryCompras = parametrosDaoCompra[idCliente].dao.queryBuilder()
        val queryCreditosFondo = parametrosDaoCreditoFondo[idCliente].dao.queryBuilder()

        val columnaPersonaFk = CampoTabla(CreditoFondoDAO.TABLA, CreditoFondoDAO.COLUMNA_ID_PERSONA)
        val columnaCreditoFondoId = CampoTabla(CreditoFondoDAO.TABLA, CreditoFondoDAO.COLUMNA_ID)
        val columnaCreditoFondoConsumido = CampoTabla(CreditoFondoDAO.TABLA, CreditoFondoDAO.COLUMNA_CONSUMIDO)
        val columnaCompraCreacionTerminada = CampoTabla(CompraDAO.TABLA, CompraDAO.COLUMNA_CREACION_TERMINADA)

        queryCreditosFondo
            .selectRaw(columnaPersonaFk.nombreColumna)
            .selectRaw(columnaCreditoFondoId.nombreColumna)
            .selectRaw(columnaCreditoFondoConsumido.nombreColumna)
            .selectRaw(columnaCompraCreacionTerminada.nombreColumna)

            .join(queryCompras)
            .also { it.where().`in`(CreditoFondoDAO.COLUMNA_ID, idsTotalesCreditosACodificar) }
            .orderBy(CreditoFondoDAO.COLUMNA_ID_PERSONA, true)

        val tiposDeColumnas = arrayOf(DataType.LONG, DataType.LONG, DataType.BOOLEAN, DataType.BOOLEAN)
        val resultados =
                parametrosDaoCompra[idCliente].dao
                    .queryRaw<ResultadoVerificacion>(
                            queryCreditosFondo.prepareStatementString(),
                            tiposDeColumnas,
                            RawRowObjectMapper
                            { _, _, resultColumns ->
                                resultColumns!!.let { ResultadoVerificacion(it[0] as Long, it[1] as Long, it[2] as Boolean, it[3] as Boolean) }
                            })
                    .iterator()

        for (resultado in resultados)
        {
            if (resultado.compraFinalizada)
            {
                val creditosCodificarDePersona = personaVsCreditos[resultado.idPersona]
                if (creditosCodificarDePersona == null || !creditosCodificarDePersona.contains(resultado.idCredito))
                {
                    val idPersonaEnSesion = personaVsCreditos.filter { it.value.contains(resultado.idCredito) }.keys.first()

                    throw ErrorCreacionViolacionDeRestriccion(
                            SesionDeManilla.NOMBRE_ENTIDAD,
                            "El crédito a codificar no le pertence a la persona",
                            arrayOf(
                                    "Id de persona de la sesión = $idPersonaEnSesion",
                                    "Id de persona dueña del crédito = ${resultado.idPersona}",
                                    "Id de crédito a codificar = ${resultado.idCredito}"
                                   )
                                                             )
                }
                else if (resultado.creditoConsumido)
                {
                    throw ErrorCreacionViolacionDeRestriccion(
                            SesionDeManilla.NOMBRE_ENTIDAD,
                            "El crédito ya fue consumido",
                            arrayOf("Id de crédito a codificar = ${resultado.idCredito}")
                                                             )
                }
            }
            else
            {
                throw ErrorCreacionViolacionDeRestriccion(
                        SesionDeManilla.NOMBRE_ENTIDAD,
                        "La compra asociada al crédito no ha finalizado",
                        arrayOf("Id de crédito a codificar = ${resultado.idCredito}")
                                                         )
            }
        }

        if (parametrosDaoCreditoFondo.configuracion.llavesForaneasActivadas)
        {
            val idsQueExisten =
                    parametrosDaoCreditoFondo[idCliente].dao.queryBuilder()
                        .selectColumns(CreditoFondoDAO.COLUMNA_ID)
                        .apply {
                            where().`in`(CreditoFondoDAO.COLUMNA_ID, idsTotalesCreditosACodificar)
                        }
                        .query()
                        .map { it.id!! }

            if (idsQueExisten.size != idsTotalesCreditosACodificar.size)
            {
                val idsQueNoExisten = idsTotalesCreditosACodificar - idsQueExisten
                throw ErrorDeLlaveForanea(idsQueNoExisten.joinToString(), CreditoFondo.NOMBRE_ENTIDAD)
            }
        }

        // Marcar créditos como asignados a una reserva
        val updateBuilder = parametrosDaoCreditoFondo[idCliente].dao.updateBuilder().apply {
            where().`in`(CreditoFondoDAO.COLUMNA_ID, idsTotalesCreditosACodificar)
        }

        updateBuilder.updateColumnValue(CreditoFondoDAO.COLUMNA_CONSUMIDO, true)

        val numeroFilasActualizadas = updateBuilder.update()
        if (numeroFilasActualizadas != idsTotalesCreditosACodificar.size)
        {
            throw ErrorAlActualizarCampo(
                    Reserva.NOMBRE_ENTIDAD,
                    ReservaDAO.COLUMNA_CREACION_TERMINADA,
                    "No se pudieron marcar todos los créditos como consumidos"
                                        )
        }
    }

    private data class ResultadoVerificacion(val idPersona: Long, val idCredito: Long, val creditoConsumido: Boolean, val compraFinalizada: Boolean)
}

private val asignadorDeIdSesionManillaACreditoDeSesion = object : TransformadorEntidadesRelacionadas<SesionDeManillaDAO, CreditoEnSesionDeManillaDAO>
{
    override fun asignarCampoRelacionAEntidadDestino(entidadOrigen: SesionDeManillaDAO, entidadDestino: CreditoEnSesionDeManillaDAO)
            : CreditoEnSesionDeManillaDAO
    {
        return entidadDestino.copy(sesionDeManillaDAO = entidadOrigen.copy(id = entidadOrigen.id))
    }
}

private val asignadorDeIdDeReservaASesionDeManilla = object : AsignadorParametro<EntidadRelacionUnoAMuchos<SesionDeManillaDAO, CreditoEnSesionDeManillaDAO>, ReservaDAO>
{
    override fun asignarParametro(entidad: EntidadRelacionUnoAMuchos<SesionDeManillaDAO, CreditoEnSesionDeManillaDAO>, parametro: ReservaDAO)
            : EntidadRelacionUnoAMuchos<SesionDeManillaDAO, CreditoEnSesionDeManillaDAO>
    {
        val sesionDeManillaDAO = entidad.entidadOrigen
        val reservaDaoSinId = sesionDeManillaDAO.reservaDao

        return entidad.copy(entidadOrigen = sesionDeManillaDAO.copy(reservaDao = reservaDaoSinId.copy(id = parametro.id)))
    }
}

private val crearLlaveCompuestaDeReservaConSesion = object : Transformador<EntidadRelacionUnoAUno<ReservaDAO, SesionDeManillaDAO>, EntidadRelacionUnoAUno<String, Long>>
{
    override fun transformar(origen: EntidadRelacionUnoAUno<ReservaDAO, SesionDeManillaDAO>): EntidadRelacionUnoAUno<String, Long>
    {
        return EntidadRelacionUnoAUno(origen.entidadOrigen.id, origen.entidadDestino.id!!)
    }
}

private val transformadorDeReservaAReservaEnBD =
        object : TransformadorEntidadCliente<Reserva, EntidadRelacionUnoAMuchos<ReservaDAO, EntidadRelacionUnoAMuchos<SesionDeManillaDAO, CreditoEnSesionDeManillaDAO>>>
        {
            override fun transformar(idCliente: Long, origen: Reserva): EntidadRelacionUnoAMuchos<ReservaDAO, EntidadRelacionUnoAMuchos<SesionDeManillaDAO, CreditoEnSesionDeManillaDAO>>
            {
                val reservaDao = ReservaDAO(origen, true)
                val sesionesManillaEnBD = origen.sesionesDeManilla.map { sesionManilla ->
                    EntidadRelacionUnoAMuchos(
                            SesionDeManillaDAO(sesionManilla, origen.id),
                            sesionManilla.idsCreditosCodificados.map { CreditoEnSesionDeManillaDAO(sesionManilla.id, it) }
                                             )
                }

                return EntidadRelacionUnoAUno(reservaDao, sesionesManillaEnBD)
            }
        }

private val transformadorDeReservaEnBDAReserva =
        object : TransformadorEntidadCliente<EntidadRelacionUnoAMuchos<ReservaDAO, EntidadRelacionUnoAMuchos<SesionDeManillaDAO, CreditoEnSesionDeManillaDAO>>, Reserva>
        {
            override fun transformar(idCliente: Long, origen: EntidadRelacionUnoAMuchos<ReservaDAO, EntidadRelacionUnoAMuchos<SesionDeManillaDAO, CreditoEnSesionDeManillaDAO>>): Reserva
            {
                val reservaDao = origen.entidadOrigen
                val sesionesDeManillaEnBD = origen.entidadDestino

                return reservaDao.aEntidadDeNegocio(idCliente, sesionesDeManillaEnBD)
            }
        }


private val extractorIdDeReserva = object : Transformador<ReservaDAO, String>
{
    override fun transformar(origen: ReservaDAO): String
    {
        return origen.id
    }
}

private class ActualizadorCamposIndividualesDeReserva(
        private val actualizadorDao: ActualizablePorCamposIndividuales<ReservaDAO, String>,
        private val transformadorCampos: Transformador<CamposDeEntidad<Reserva>, CamposDeEntidadDAO<ReservaDAO>>,
        private val parametrosDaoReserva: ParametrosParaDAOEntidadDeCliente<ReservaDAO, String>,
        private val parametrosDaoSesionDeManilla: ParametrosParaDAOEntidadDeCliente<SesionDeManillaDAO, Long>,
        private val parametrosDaoCreditoEnSesionDeManilla: ParametrosParaDAOEntidadDeCliente<CreditoEnSesionDeManillaDAO, Long>,
        private val parametrosDaoCreditoFondo: ParametrosParaDAOEntidadDeCliente<CreditoFondoDAO, Long>,
        private val parametrosDaoOrden: ParametrosParaDAOEntidadDeCliente<OrdenDAO, Long>,
        private val parametrosDaoTransaccion: ParametrosParaDAOEntidadDeCliente<TransaccionDAO, Long>)
    : ActualizablePorCamposIndividuales<Reserva, String>
{
    override val nombreEntidad: String = actualizadorDao.nombreEntidad

    private fun crearOrdenes(idCliente: Long, id: String)
    {
        /*
            Para crear las órdenes cuando se crea la reserva:
            1. Buscar ids sesiones de manilla por id de reserva junto con sus créditos
            2. Buscar usuario de quién creó la reserva usando el id de reserva
            3. Generar id de transacción para las órdenes como: ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO) + usaurio encontrado + UUID
            4. Crear las OrdenDAO usando el id de transaccion y los ids de sesión manilla encontrados
            5. Marcar orden como transaccion terminada
         */

        val filtroDeSesionesPorIdDeReserva =
                listOf(FiltroIgualdad(CampoTabla(SesionDeManillaDAO.TABLA, SesionDeManillaDAO.COLUMNA_ID_RESERVA), id, SqlType.STRING))

        val repositorioSesionesManillaFiltradoPorIdReserva =
                ListableSQLConFiltrosSQL(
                        repositorioEntidadDao(parametrosDaoSesionDeManilla, SesionDeManillaDAO.COLUMNA_ID),
                        filtroDeSesionesPorIdDeReserva
                                        )

        val combinador =
                ListableProyectandoColumnasSoloParaCrearOrdenes(
                        ListableInnerJoin
                        (
                                repositorioSesionesManillaFiltradoPorIdReserva,
                                ListableInnerJoin
                                (
                                        repositorioEntidadDao(parametrosDaoCreditoEnSesionDeManilla, CreditoEnSesionDeManillaDAO.COLUMNA_ID),
                                        repositorioEntidadDao(parametrosDaoCreditoFondo, CreditoFondoDAO.COLUMNA_ID)
                                )
                        )
                                                               )

        val idsDeSesionesDeManillaConCreditosCodificados =
                combinador.listar(idCliente).groupBy({ it.idSesionDeManilla }, { it })

        val usuarioCreadorDeReserva = EntidadTransaccional.idAPartes(id).nombreUsuario

        val fechaDeRealizacionDeOrdenes = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
        val ordenes = idsDeSesionesDeManillaConCreditosCodificados.map { arriba ->
            val transacciones = arriba.value.map {
                Transaccion.Credito(
                        idCliente,
                        null,
                        usuarioCreadorDeReserva,
                        it.idUbicacion,
                        it.idFondo,
                        it.codigoExternoFondo,
                        it.cantidad,
                        it.idGrupoDeClientes,
                        "Back-end",
                        it.validoDesde,
                        it.validoHasta
                                   )
            }

            Orden(idCliente, null, arriba.key, transacciones, fechaDeRealizacionDeOrdenes)
        }

        val loteDeOrdenes = LoteDeOrdenes(idCliente, usuarioCreadorDeReserva, ordenes)

        darCreableDeOrdenesDesdeLoteDeOrdenesSinTransaccion(parametrosDaoOrden, parametrosDaoTransaccion)
            .crear(idCliente, LoteDeOrdenesConParametrosEliminacion(loteDeOrdenes))

        val updateBuilder = parametrosDaoOrden[idCliente].dao.updateBuilder().apply {
            where().eq(OrdenDAO.COLUMNA_ID_TRANSACCION, loteDeOrdenes.id)
        }

        updateBuilder.updateColumnValue(OrdenDAO.COLUMNA_CREACION_TERMINADA, true)

        val numeroFilasActualizadas = updateBuilder.update()
        if (numeroFilasActualizadas != loteDeOrdenes.ordenes.size)
        {
            throw ErrorAlActualizarCampo(Orden.NOMBRE_ENTIDAD, OrdenDAO.COLUMNA_CREACION_TERMINADA, "No se pudieron marcar las órdenes iniciales como terminadas")
        }
    }

    private fun asignarNumeroDeReserva(idCliente: Long, id: String)
    {
        val columnaNumeroDeReservaEscapada = CampoTabla(ReservaDAO.TABLA, ReservaDAO.COLUMNA_NUMERO_DE_RESERVA)

        val queryMaximoNumeroDeReservaNoNulo =
                parametrosDaoReserva[idCliente].dao.queryBuilder()
                    .selectRaw("MAX(${columnaNumeroDeReservaEscapada.nombreColumna})")
                    .apply {
                        where()
                            .isNotNull(ReservaDAO.COLUMNA_NUMERO_DE_RESERVA)
                            .and()
                            .ne(ReservaDAO.COLUMNA_ID, id)
                    }

        val numeroDeReservaSiguiente =
                parametrosDaoReserva[idCliente].dao
                    .queryRaw(queryMaximoNumeroDeReservaNoNulo.prepareStatementString())
                    .firstResult
                    ?.first()?.toLong()?.plus(1L)
                ?: 1L

        val updateBuilder = parametrosDaoReserva[idCliente].dao.updateBuilder().apply {
            where().idEq(id)
        }

        updateBuilder.updateColumnValue(ReservaDAO.COLUMNA_NUMERO_DE_RESERVA, numeroDeReservaSiguiente)

        val numeroFilasActualizadas = updateBuilder.update()
        if (numeroFilasActualizadas != 1)
        {
            throw ErrorAlActualizarCampo(nombreEntidad, ReservaDAO.COLUMNA_NUMERO_DE_RESERVA, "No se pudo asignar el número de reserva")
        }
    }

    override fun actualizarCamposIndividuales(idCliente: Long, id: String, camposAActualizar: CamposDeEntidad<Reserva>)
    {
        // Actualizar campos de la reserva
        val estabaYaTerminada = parametrosDaoReserva[idCliente].dao.queryForId(id)?.creacionTerminada
        val camposTransformados = transformadorCampos.transformar(camposAActualizar)
        actualizadorDao.actualizarCamposIndividuales(idCliente, id, camposTransformados)

        if (camposTransformados[ReservaDAO.COLUMNA_CREACION_TERMINADA]?.valor as Boolean)
        {
            if (!estabaYaTerminada!!)
            {
                crearOrdenes(idCliente, id)
                asignarNumeroDeReserva(idCliente, id)
            }
        }
    }

    private data class ResultadoSesionesConCreditos(
            val idSesionDeManilla: Long,
            val idFondo: Long,
            val codigoExternoFondo: String,
            val idUbicacion: Long?,
            val cantidad: Decimal,
            val idGrupoDeClientes: Long?,
            val validoDesde: ZonedDateTime?,
            val validoHasta: ZonedDateTime?
                                                   )

    private class ListableProyectandoColumnasSoloParaCrearOrdenes<EntidadOrigen>(
            private val listadorOrigen: ListableSQL<EntidadOrigen>)
        : ListableSQL<ResultadoSesionesConCreditos>
    {
        private val camposProyectados =
                listOf(
                        CampoTabla(CreditoEnSesionDeManillaDAO.TABLA, CreditoEnSesionDeManillaDAO.COLUMNA_ID_SESION_DE_MANILLA),
                        CampoTabla(CreditoFondoDAO.TABLA, CreditoFondoDAO.COLUMNA_ID_FONDO),
                        CampoTabla(CreditoFondoDAO.TABLA, CreditoFondoDAO.COLUMNA_CODIGO_EXTERNO_FONDO),
                        CampoTabla(CreditoFondoDAO.TABLA, CreditoFondoDAO.COLUMNA_ID_UBICACION),
                        CampoTabla(CreditoFondoDAO.TABLA, CreditoFondoDAO.COLUMNA_CANTIDAD),
                        CampoTabla(CreditoFondoDAO.TABLA, CreditoFondoDAO.COLUMNA_ID_GRUPO_CLIENTES),
                        CampoTabla(CreditoFondoDAO.TABLA, CreditoFondoDAO.COLUMNA_VALIDO_DESDE),
                        CampoTabla(CreditoFondoDAO.TABLA, CreditoFondoDAO.COLUMNA_VALIDO_HASTA)
                      )

        override val camposDeOrdenamiento: List<CampoTabla> = listadorOrigen.camposDeOrdenamiento

        override fun conFiltrosSQL(filtrosSQL: List<FiltroSQL>): ListableFiltrableOrdenable<ResultadoSesionesConCreditos>
        {
            return ListableSQLConFiltrosSQL(this, filtrosSQL)
        }

        override fun darConstructorQuery(idCliente: Long): ConstructorQueryORMLite<ResultadoSesionesConCreditos>
        {
            return listadorOrigen.darConstructorQuery(idCliente).reemplazandoProyeccion(
                    InformacionProyeccion(camposProyectados, MapeadorResultado())
                                                                                       )
        }

        private inner class MapeadorResultado : MapeadorResultadoORMLite<ResultadoSesionesConCreditos>
        {
            override var columnaInicial: Int = 0
            override val numeroColumnas: Int = camposProyectados.size
            override fun mapearResultado(resultado: DatabaseResults): ResultadoSesionesConCreditos
            {
                val rangoColumnas = columnaInicial until columnaInicial + numeroColumnas
                val resultadoBD = ResultadoBDJoin(resultado, rangoColumnas)

                val comoLongNull = { numero: Any? ->
                    if (numero is Long?) numero else (numero as Int?)?.toLong()
                }

                return ResultadoSesionesConCreditos(
                        resultadoBD.getLong(0),
                        resultadoBD.getLong(1),
                        resultadoBD.getString(2)!!,
                        comoLongNull(resultadoBD.getObject(3)),
                        Decimal(resultadoBD.getBigDecimal(4)!!),
                        comoLongNull(resultadoBD.getObject(5)),
                        resultadoBD.getString(6)?.let { ZonedDateTimeThreeTenType.deBD(it) },
                        resultadoBD.getString(7)?.let { ZonedDateTimeThreeTenType.deBD(it) }
                                                   )
            }
        }
    }
}

private val transformadorCamposNegocioACamposDaoReserva = object : Transformador<CamposDeEntidad<Reserva>, CamposDeEntidadDAO<ReservaDAO>>
{
    override fun transformar(origen: CamposDeEntidad<Reserva>): CamposDeEntidadDAO<ReservaDAO>
    {
        return origen.map {
            when (it.key)
            {
                EntidadTransaccional.Campos.CREACION_TERMINADA ->
                {
                    ReservaDAO.COLUMNA_CREACION_TERMINADA to CampoModificableEntidadDao<ReservaDAO, Any?>(it.value.valor, ReservaDAO.COLUMNA_CREACION_TERMINADA)
                }
                else                                           ->
                {
                    throw CampoActualizableDesconocido(it.key, Reserva.NOMBRE_ENTIDAD)
                }
            }
        }.toMap()
    }
}

internal class EliminadorReservaConActualizacionCreditos(
        private val parametrosDaoSesionDeManilla: ParametrosParaDAOEntidadDeCliente<SesionDeManillaDAO, Long>,
        private val parametrosDaoCreditoEnSesionDeManilla: ParametrosParaDAOEntidadDeCliente<CreditoEnSesionDeManillaDAO, Long>,
        private val parametrosDaoCreditoFondo: ParametrosParaDAOEntidadDeCliente<CreditoFondoDAO, Long>,
        private val eliminador: EliminablePorIdFiltrable<ReservaDAO, String>)
    : EliminablePorIdFiltrable<ReservaDAO, String>
{
    override val nombreEntidad: String = Reserva.NOMBRE_ENTIDAD

    private fun marcarCreditosComoNoConsumidos(idCliente: Long, idReserva: String)
    {
        val querySesionesManilla = parametrosDaoSesionDeManilla[idCliente].dao.queryBuilder()
        val queryCreditosEnSesion = parametrosDaoCreditoEnSesionDeManilla[idCliente].dao.queryBuilder()

        val columnaCreditoFondoFk = CampoTabla(CreditoEnSesionDeManillaDAO.TABLA, CreditoEnSesionDeManillaDAO.COLUMNA_ID_CREDITO_FONDO)

        querySesionesManilla
            .selectRaw(columnaCreditoFondoFk.nombreColumna)
            .join(queryCreditosEnSesion)
            .also { it.where().eq(SesionDeManillaDAO.COLUMNA_ID_RESERVA, idReserva) }

        val tiposDeColumnas = arrayOf(DataType.LONG)
        val idsDeCreditosAActualizar =
                parametrosDaoSesionDeManilla[idCliente].dao
                    .queryRaw<Long>(
                            querySesionesManilla.prepareStatementString(),
                            tiposDeColumnas,
                            RawRowObjectMapper { _, _, resultColumns -> resultColumns!![0] as Long })
                    .results

        if (idsDeCreditosAActualizar.isNotEmpty())
        {
            val updateBuilder = parametrosDaoCreditoFondo[idCliente].dao.updateBuilder().apply {
                where().`in`(CreditoFondoDAO.COLUMNA_ID, idsDeCreditosAActualizar)
            }

            updateBuilder.updateColumnValue(CreditoFondoDAO.COLUMNA_CONSUMIDO, false)

            val numeroFilasActualizadas = updateBuilder.update()
            if (numeroFilasActualizadas != idsDeCreditosAActualizar.size)
            {
                throw ErrorAlActualizarCampo(nombreEntidad, ReservaDAO.COLUMNA_CREACION_TERMINADA, "No se pudieron desmarcar todos los créditos a no consumidos")
            }
        }
    }

    override fun eliminarPorId(idCliente: Long, id: String): Boolean
    {
        marcarCreditosComoNoConsumidos(idCliente, id)
        return eliminador.eliminarPorId(idCliente, id)
    }

    override fun conFiltrosSQL(filtrosIgualdad: Sequence<FiltroIgualdad<*>>): EliminablePorIdFiltrable<ReservaDAO, String>
    {
        return EliminadorReservaConActualizacionCreditos(
                parametrosDaoSesionDeManilla,
                parametrosDaoCreditoEnSesionDeManilla,
                parametrosDaoCreditoFondo,
                eliminador.conFiltrosSQL(filtrosIgualdad)
                                                        )
    }
}

class RepositorioReservasSQL private constructor(
        private val creadorRepositorio: CreadorRepositorio<Reserva>,
        private val repositorioTransaccional: RepositorioTransaccional<Reserva, ReservaDAO>,
        private val listador: Listable<Reserva>,
        private val buscador: Buscable<Reserva, String>,
        private val actualizadorCamposIndividuales: ActualizablePorCamposIndividuales<Reserva, String>)
    : CreadorRepositorio<Reserva> by creadorRepositorio,
      Creable<Reserva> by repositorioTransaccional,
      Listable<Reserva> by listador,
      Buscable<Reserva, String> by buscador,
      ActualizablePorCamposIndividuales<Reserva, String> by actualizadorCamposIndividuales,
      EliminablePorId<Reserva, String> by repositorioTransaccional,
      RepositorioReservas
{
    companion object
    {
        private val NOMBRE_ENTIDAD = Reserva.NOMBRE_ENTIDAD
    }

    override val nombreEntidad: String = NOMBRE_ENTIDAD


    private constructor(
            creadorRepositorio: CreadorRepositorio<Reserva>,
            creadorSinTransaccion: Creable<Reserva>,
            listador: ListableFiltrableOrdenable<Reserva>,
            actualizadorCamposIndividuales: ActualizablePorCamposIndividuales<Reserva, String>,
            eliminadorSinTransaccion: EliminablePorIdFiltrable<Reserva, String>,
            parametrosDaoReserva: ParametrosParaDAOEntidadDeCliente<ReservaDAO, String>
                       )
            : this(
            creadorRepositorio,
            RepositorioTransaccional<Reserva, ReservaDAO>
            (
                    ReservaDAO.TABLA,
                    NOMBRE_ENTIDAD,
                    parametrosDaoReserva,
                    creadorSinTransaccion,
                    eliminadorSinTransaccion,
                    EliminableConRestriccion
                    (
                            eliminadorSinTransaccion,
                            object : ValidadorRestriccionEliminacion<String>
                            {
                                override fun validarRestriccion(idCliente: Long, idAEliminar: String)
                                {
                                    val reservaAEliminar = parametrosDaoReserva[idCliente].dao.queryForId(idAEliminar)

                                    if (reservaAEliminar != null && reservaAEliminar.creacionTerminada)
                                    {
                                        throw ErrorEliminacionViolacionDeRestriccion(
                                                NOMBRE_ENTIDAD,
                                                idAEliminar,
                                                "La reserva se encuentra marcada como terminada",
                                                null
                                                                                    )
                                    }
                                }
                            }
                    )
            ),
            listador,
            BuscableSegunListableFiltrable(listador, CampoTabla(ReservaDAO.TABLA, ReservaDAO.COLUMNA_ID), SqlType.STRING),
            actualizadorCamposIndividuales
                  )

    private constructor(
            parametrosDaoReserva: ParametrosParaDAOEntidadDeCliente<ReservaDAO, String>,
            parametrosDaoSesionDeManilla: ParametrosParaDAOEntidadDeCliente<SesionDeManillaDAO, Long>,
            parametrosDaoCreditoEnSesionDeManilla: ParametrosParaDAOEntidadDeCliente<CreditoEnSesionDeManillaDAO, Long>,
            parametrosDaoCompra: ParametrosParaDAOEntidadDeCliente<CompraDAO, String>,
            parametrosDaoCreditoFondo: ParametrosParaDAOEntidadDeCliente<CreditoFondoDAO, Long>,
            parametrosDaoPersona: ParametrosParaDAOEntidadDeCliente<PersonaDAO, Long>,
            parametrosDaoOrden: ParametrosParaDAOEntidadDeCliente<OrdenDAO, Long>,
            parametrosDaoTransaccion: ParametrosParaDAOEntidadDeCliente<TransaccionDAO, Long>
                       )
            :
            this(
                    CreadorUnicaVez
                    (
                            CreadorRepositorioCompuesto
                            (
                                    listOf(
                                            CreadorRepositorioDAO(parametrosDaoReserva, NOMBRE_ENTIDAD),
                                            CreadorRepositorioDAO(parametrosDaoSesionDeManilla, NOMBRE_ENTIDAD),
                                            CreadorRepositorioDAO(parametrosDaoCreditoEnSesionDeManilla, NOMBRE_ENTIDAD)
                                          ),
                                    NOMBRE_ENTIDAD
                            )
                    ),
                    CreableConRestriccion
                    (
                            CreableConTransformacion<Reserva, EntidadRelacionUnoAMuchos<ReservaDAO, EntidadRelacionUnoAMuchos<SesionDeManillaDAO, CreditoEnSesionDeManillaDAO>>>
                            (
                                    CreableDAOUnoAMuchosAnidado
                                    (
                                            CreableDAO(parametrosDaoReserva, NOMBRE_ENTIDAD),
                                            CreableMultiplesUnoAUno<EntidadRelacionUnoAMuchos<SesionDeManillaDAO, CreditoEnSesionDeManillaDAO>>
                                            (
                                                    CreableDAORelacionUnoAMuchos<SesionDeManillaDAO, CreditoEnSesionDeManillaDAO>
                                                    (
                                                            CreableDAO(parametrosDaoSesionDeManilla, NOMBRE_ENTIDAD),
                                                            CreableDAOMultiples(parametrosDaoCreditoEnSesionDeManilla, NOMBRE_ENTIDAD),
                                                            asignadorDeIdSesionManillaACreditoDeSesion
                                                    )
                                            ),
                                            asignadorDeIdDeReservaASesionDeManilla
                                    ),
                                    transformadorDeReservaAReservaEnBD,
                                    transformadorDeReservaEnBDAReserva
                            ),
                            ValidadorCreacionReserva(parametrosDaoPersona, parametrosDaoCompra, parametrosDaoCreditoFondo)
                    ),
                    ListableConTransaccion
                    (
                            parametrosDaoReserva.configuracion,
                            NOMBRE_ENTIDAD,
                            ListableConTransformacion
                            (
                                    ListableUnoAMuchos
                                    (
                                            ListableConTransformacion
                                            (
                                                    ListableUnoAMuchos
                                                    (
                                                            ListableConTransformacion
                                                            (
                                                                    ListableInnerJoin<ReservaDAO, EntidadRelacionUnoAUno<SesionDeManillaDAO, CreditoEnSesionDeManillaDAO>>
                                                                    (
                                                                            repositorioEntidadDao(parametrosDaoReserva, ReservaDAO.COLUMNA_ID),
                                                                            ListableInnerJoin<SesionDeManillaDAO, CreditoEnSesionDeManillaDAO>
                                                                            (
                                                                                    repositorioEntidadDao(parametrosDaoSesionDeManilla, SesionDeManillaDAO.COLUMNA_ID),
                                                                                    repositorioEntidadDao(parametrosDaoCreditoEnSesionDeManilla, CreditoEnSesionDeManillaDAO.COLUMNA_ID)
                                                                            )
                                                                    ),
                                                                    shiftIzquierdaEntidadRelacionUnoAUno()
                                                            ),
                                                            crearLlaveCompuestaDeReservaConSesion
                                                    ),
                                                    shiftDerechaEntidadRelacionUnoAUno()
                                            ),
                                            extractorIdDeReserva
                                    ),
                                    transformadorDeReservaEnBDAReserva
                            )
                    ),
                    ActualizablePorCamposIndividualesEnTransaccionSQL
                    (
                            parametrosDaoReserva.configuracion,
                            ActualizadorCamposIndividualesDeReserva
                            (
                                    ActualizablePorCamposIndividualesDao(NOMBRE_ENTIDAD, parametrosDaoReserva),
                                    transformadorCamposNegocioACamposDaoReserva,
                                    parametrosDaoReserva,
                                    parametrosDaoSesionDeManilla,
                                    parametrosDaoCreditoEnSesionDeManilla,
                                    parametrosDaoCreditoFondo,
                                    parametrosDaoOrden,
                                    parametrosDaoTransaccion
                            )
                    ),
                    EliminableSimple
                    (
                            EliminadorReservaConActualizacionCreditos
                            (
                                    parametrosDaoSesionDeManilla,
                                    parametrosDaoCreditoEnSesionDeManilla,
                                    parametrosDaoCreditoFondo,
                                    EliminableDao(NOMBRE_ENTIDAD, parametrosDaoReserva)
                                        .conFiltrosSQL(
                                                sequenceOf(FiltroIgualdad(CampoTabla(ReservaDAO.TABLA, ColumnasTransaccionales.COLUMNA_CREACION_TERMINADA), false, SqlType.BOOLEAN))
                                                      )
                            ),
                            TransformadorIdentidad()
                    ),
                    parametrosDaoReserva
                )

    constructor(configuracion: ConfiguracionRepositorios)
            : this(
            ParametrosParaDAOEntidadDeCliente(configuracion, ReservaDAO.TABLA, ReservaDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, SesionDeManillaDAO.TABLA, SesionDeManillaDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, CreditoEnSesionDeManillaDAO.TABLA, CreditoEnSesionDeManillaDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, CompraDAO.TABLA, CompraDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, CreditoFondoDAO.TABLA, CreditoFondoDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, PersonaDAO.TABLA, PersonaDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, OrdenDAO.TABLA, OrdenDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, TransaccionDAO.TABLA, TransaccionDAO::class.java)
                  )
}
