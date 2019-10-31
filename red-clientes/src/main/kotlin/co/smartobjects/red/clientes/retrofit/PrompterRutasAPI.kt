package co.smartobjects.red.clientes.retrofit

internal object RuteoClientesApi
{
    const val RUTA: String = "clients"

    const val RUTA_RESUELTA: String = RUTA

    object Operaciones
    {
        object GET

        object POST
    }

    internal object RuteoClienteApi
    {
        const val RUTA: String = "{id_cliente}"

        const val PARAMETRO_RUTA: String = "id_cliente"

        const val RUTA_RESUELTA: String = "${RuteoClientesApi.RUTA_RESUELTA}/$RUTA"

        object Operaciones
        {
            object DELETE

            object GET

            object PUT
        }

        internal object RuteoAccesosApi
        {
            const val RUTA: String = "accesses"

            const val RUTA_RESUELTA: String = "${RuteoClienteApi.RUTA_RESUELTA}/$RUTA"

            object Operaciones
            {
                object GET

                object POST
            }

            internal object RuteoAccesoApi
            {
                const val RUTA: String = "{id_acceso}"

                const val PARAMETRO_RUTA: String = "id_acceso"

                const val RUTA_RESUELTA: String = "${RuteoAccesosApi.RUTA_RESUELTA}/$RUTA"

                object Operaciones
                {
                    object DELETE

                    object GET

                    object PATCH

                    object PUT
                }
            }
        }

        internal object RuteoCamposDePersonaApi
        {
            const val RUTA: String = "persons-fields"

            const val RUTA_RESUELTA: String = "${RuteoClienteApi.RUTA_RESUELTA}/$RUTA"

            object Operaciones
            {
                object GET
            }

            internal object RuteoCampoDePersonaApi
            {
                const val RUTA: String = "{campo_campodepersona}"

                const val PARAMETRO_RUTA: String = "campo_campodepersona"

                const val RUTA_RESUELTA: String = "${RuteoCamposDePersonaApi.RUTA_RESUELTA}/$RUTA"

                object Operaciones
                {
                    object GET

                    object PUT
                }
            }
        }

        internal object RuteoCategoriasSkuApi
        {
            const val RUTA: String = "sku-categories"

            const val RUTA_RESUELTA: String = "${RuteoClienteApi.RUTA_RESUELTA}/$RUTA"

            object Operaciones
            {
                object GET

                object POST
            }

            internal object RuteoCategoriaSkuApi
            {
                const val RUTA: String = "{id_categoriasku}"

                const val PARAMETRO_RUTA: String = "id_categoriasku"

                const val RUTA_RESUELTA: String = "${RuteoCategoriasSkuApi.RUTA_RESUELTA}/$RUTA"

                object Operaciones
                {
                    object DELETE

                    object GET

                    object PATCH

                    object PUT
                }
            }
        }

        internal object RuteoComprasApi
        {
            const val RUTA: String = "purchases"

            const val RUTA_RESUELTA: String = "${RuteoClienteApi.RUTA_RESUELTA}/$RUTA"

            object Operaciones
            {
                object GET
            }

            internal object RuteoCompraApi
            {
                const val RUTA: String = "{id_compra}"

                const val PARAMETRO_RUTA: String = "id_compra"

                const val RUTA_RESUELTA: String = "${RuteoComprasApi.RUTA_RESUELTA}/$RUTA"

                object Operaciones
                {
                    object DELETE

                    object GET

                    object PATCH

                    object PUT
                }
            }
        }

        internal object RuteoConteosUbicacionesApi
        {
            const val RUTA: String = "locations-counts"

            const val RUTA_RESUELTA: String = "${RuteoClienteApi.RUTA_RESUELTA}/$RUTA"

            object Operaciones
            {
                object DELETE

                object GET
            }
        }

        internal object RuteoEntradasApi
        {
            const val RUTA: String = "entries"

            const val RUTA_RESUELTA: String = "${RuteoClienteApi.RUTA_RESUELTA}/$RUTA"

            object Operaciones
            {
                object GET

                object POST
            }

            internal object RuteoEntradaApi
            {
                const val RUTA: String = "{id_entrada}"

                const val PARAMETRO_RUTA: String = "id_entrada"

                const val RUTA_RESUELTA: String = "${RuteoEntradasApi.RUTA_RESUELTA}/$RUTA"

                object Operaciones
                {
                    object DELETE

                    object GET

                    object PATCH

                    object PUT
                }
            }
        }

        internal object RuteoFondosApi
        {
            const val RUTA: String = "funds"

            const val RUTA_RESUELTA: String = "${RuteoClienteApi.RUTA_RESUELTA}/$RUTA"

            object Operaciones
            {
                object GET
            }

            internal object RuteoFondoApi
            {
                const val RUTA: String = "{id_fondo}"

                const val PARAMETRO_RUTA: String = "id_fondo"

                const val RUTA_RESUELTA: String = "${RuteoFondosApi.RUTA_RESUELTA}/$RUTA"

                object Operaciones
                {
                    object DELETE

                    object GET

                    object PATCH
                }
            }
        }

        internal object RuteoGruposClientesApi
        {
            const val RUTA: String = "customers-groups"

            const val RUTA_RESUELTA: String = "${RuteoClienteApi.RUTA_RESUELTA}/$RUTA"

            object Operaciones
            {
                object GET

                object POST
            }

            internal object RuteoGrupoClientesApi
            {
                const val RUTA: String = "{id_grupoclientes}"

                const val PARAMETRO_RUTA: String = "id_grupoclientes"

                const val RUTA_RESUELTA: String = "${RuteoGruposClientesApi.RUTA_RESUELTA}/$RUTA"

                object Operaciones
                {
                    object DELETE

                    object GET

                    object PATCH
                }
            }
        }

        internal object RuteoImpuestosApi
        {
            const val RUTA: String = "taxes"

            const val RUTA_RESUELTA: String = "${RuteoClienteApi.RUTA_RESUELTA}/$RUTA"

            object Operaciones
            {
                object GET

                object POST
            }

            internal object RuteoImpuestoApi
            {
                const val RUTA: String = "{id_impuesto}"

                const val PARAMETRO_RUTA: String = "id_impuesto"

                const val RUTA_RESUELTA: String = "${RuteoImpuestosApi.RUTA_RESUELTA}/$RUTA"

                object Operaciones
                {
                    object DELETE

                    object GET

                    object PUT
                }
            }
        }

        internal object RuteoLibrosDePreciosApi
        {
            const val RUTA: String = "pricing-books"

            const val RUTA_RESUELTA: String = "${RuteoClienteApi.RUTA_RESUELTA}/$RUTA"

            object Operaciones
            {
                object GET

                object POST
            }

            internal object RuteoLibroDePreciosApi
            {
                const val RUTA: String = "{id_librodeprecios}"

                const val PARAMETRO_RUTA: String = "id_librodeprecios"

                const val RUTA_RESUELTA: String = "${RuteoLibrosDePreciosApi.RUTA_RESUELTA}/$RUTA"

                object Operaciones
                {
                    object DELETE

                    object GET

                    object PUT
                }
            }
        }

        internal object RuteoLibrosDeProhibicionesApi
        {
            const val RUTA: String = "sales-prohibitions-books"

            const val RUTA_RESUELTA: String = "${RuteoClienteApi.RUTA_RESUELTA}/$RUTA"

            object Operaciones
            {
                object GET

                object POST
            }

            internal object RuteoLibroDeProhibicionesApi
            {
                const val RUTA: String = "{id_librodeprohibiciones}"

                const val PARAMETRO_RUTA: String = "id_librodeprohibiciones"

                const val RUTA_RESUELTA: String =
                        "${RuteoLibrosDeProhibicionesApi.RUTA_RESUELTA}/$RUTA"

                object Operaciones
                {
                    object DELETE

                    object GET

                    object PUT
                }
            }
        }

        internal object RuteoLibrosSegunReglasApi
        {
            const val RUTA: String = "rules-books"

            const val RUTA_RESUELTA: String = "${RuteoClienteApi.RUTA_RESUELTA}/$RUTA"

            object Operaciones
            {
                object GET

                object POST
            }

            internal object RuteoLibroSegunReglasApi
            {
                const val RUTA: String = "{id_librosegunreglas}"

                const val PARAMETRO_RUTA: String = "id_librosegunreglas"

                const val RUTA_RESUELTA: String = "${RuteoLibrosSegunReglasApi.RUTA_RESUELTA}/$RUTA"

                object Operaciones
                {
                    object DELETE

                    object GET

                    object PUT
                }
            }
        }

        internal object RuteoLibrosSegunReglasCompletoApi
        {
            const val RUTA: String = "rules-books-complete"

            const val RUTA_RESUELTA: String = "${RuteoClienteApi.RUTA_RESUELTA}/$RUTA"

            object Operaciones
            {
                object GET
            }

            internal object RuteoLibroSegunReglasCompletoApi
            {
                const val RUTA: String = "{id_librosegunreglascompleto}"

                const val PARAMETRO_RUTA: String = "id_librosegunreglascompleto"

                const val RUTA_RESUELTA: String =
                        "${RuteoLibrosSegunReglasCompletoApi.RUTA_RESUELTA}/$RUTA"

                object Operaciones
                {
                    object GET
                }
            }
        }

        internal object RuteoLlavesNFCApi
        {
            const val RUTA: String = "nfc-keys"

            const val RUTA_RESUELTA: String = "${RuteoClienteApi.RUTA_RESUELTA}/$RUTA"

            object Operaciones
            {
                object DELETE
                {
                    const val fecha: String = "base-datetime"

                    const val fecha_tipo: String = "org.threeten.bp.ZonedDateTime?"
                }

                object GET
                {
                    const val fecha: String = "base-datetime"

                    const val fecha_tipo: String = "org.threeten.bp.ZonedDateTime?"
                }

                object POST
                {
                    const val fecha: String = "base-datetime"

                    const val fecha_tipo: String = "org.threeten.bp.ZonedDateTime?"
                }
            }
        }

        internal object RuteoLotesDeOrdenesApi
        {
            const val RUTA: String = "orders-batches"

            const val RUTA_RESUELTA: String = "${RuteoClienteApi.RUTA_RESUELTA}/$RUTA"

            object Operaciones

            internal object RuteoLoteDeOrdenesApi
            {
                const val RUTA: String = "{id_lotedeordenes}"

                const val PARAMETRO_RUTA: String = "id_lotedeordenes"

                const val RUTA_RESUELTA: String = "${RuteoLotesDeOrdenesApi.RUTA_RESUELTA}/$RUTA"

                object Operaciones
                {
                    object PATCH

                    object PUT
                }
            }
        }

        internal object RuteoMonedasApi
        {
            const val RUTA: String = "currencies"

            const val RUTA_RESUELTA: String = "${RuteoClienteApi.RUTA_RESUELTA}/$RUTA"

            object Operaciones
            {
                object GET

                object POST
            }

            internal object RuteoMonedaApi
            {
                const val RUTA: String = "{id_moneda}"

                const val PARAMETRO_RUTA: String = "id_moneda"

                const val RUTA_RESUELTA: String = "${RuteoMonedasApi.RUTA_RESUELTA}/$RUTA"

                object Operaciones
                {
                    object DELETE

                    object GET

                    object PATCH

                    object PUT
                }
            }
        }

        internal object RuteoOrdenesApi
        {
            const val RUTA: String = "orders"

            const val RUTA_RESUELTA: String = "${RuteoClienteApi.RUTA_RESUELTA}/$RUTA"

            object Operaciones
            {
                object GET
            }

            internal object RuteoOrdenApi
            {
                const val RUTA: String = "{id_orden}"

                const val PARAMETRO_RUTA: String = "id_orden"

                const val RUTA_RESUELTA: String = "${RuteoOrdenesApi.RUTA_RESUELTA}/$RUTA"

                object Operaciones
                {
                    object DELETE

                    object GET
                }
            }
        }

        internal object RuteoPaquetesApi
        {
            const val RUTA: String = "packages"

            const val RUTA_RESUELTA: String = "${RuteoClienteApi.RUTA_RESUELTA}/$RUTA"

            object Operaciones
            {
                object GET

                object POST
            }

            internal object RuteoPaqueteApi
            {
                const val RUTA: String = "{id_paquete}"

                const val PARAMETRO_RUTA: String = "id_paquete"

                const val RUTA_RESUELTA: String = "${RuteoPaquetesApi.RUTA_RESUELTA}/$RUTA"

                object Operaciones
                {
                    object DELETE

                    object GET

                    object PATCH

                    object PUT
                }
            }
        }

        internal object RuteoPermisosPosiblesApi
        {
            const val RUTA: String = "possible-permissions"

            const val RUTA_RESUELTA: String = "${RuteoClienteApi.RUTA_RESUELTA}/$RUTA"

            object Operaciones
            {
                object GET
            }
        }

        internal object RuteoPersonaPorDocumentoApi
        {
            const val RUTA: String = "person-by-document"

            const val RUTA_RESUELTA: String = "${RuteoClienteApi.RUTA_RESUELTA}/$RUTA"

            object Operaciones
            {
                object GET
                {
                    const val numeroDocumento: String = "document-number"

                    const val numeroDocumento_tipo: String = "kotlin.String?"

                    const val tipoDocumentoStr: String = "document-type"

                    const val tipoDocumentoStr_tipo: String = "kotlin.String?"
                }
            }
        }

        internal object RuteoPersonasApi
        {
            const val RUTA: String = "persons"

            const val RUTA_RESUELTA: String = "${RuteoClienteApi.RUTA_RESUELTA}/$RUTA"

            object Operaciones
            {
                object GET

                object POST
            }

            internal object RuteoPersonaApi
            {
                const val RUTA: String = "{id_persona}"

                const val PARAMETRO_RUTA: String = "id_persona"

                const val RUTA_RESUELTA: String = "${RuteoPersonasApi.RUTA_RESUELTA}/$RUTA"

                object Operaciones
                {
                    object DELETE

                    object GET

                    object PUT
                }

                internal object RuteoComprasDeUnaPersonaApi
                {
                    const val RUTA: String = "available-purchases"

                    const val RUTA_RESUELTA: String = "${RuteoPersonaApi.RUTA_RESUELTA}/$RUTA"

                    object Operaciones
                    {
                        object GET
                        {
                            const val fecha: String = "base-datetime"

                            const val fecha_tipo: String = "org.threeten.bp.ZonedDateTime?"
                        }
                    }
                }

                internal object RuteoCreditosDeUnaPersonaApi
                {
                    const val RUTA: String = "credits"

                    const val RUTA_RESUELTA: String = "${RuteoPersonaApi.RUTA_RESUELTA}/$RUTA"

                    object Operaciones
                    {
                        object GET
                        {
                            const val fecha: String = "base-datetime"

                            const val fecha_tipo: String = "org.threeten.bp.ZonedDateTime?"
                        }
                    }
                }
            }
        }

        internal object RuteoPersonasDeUnaCompraApi
        {
            const val RUTA: String = "persons-by-transaction-number"

            const val RUTA_RESUELTA: String = "${RuteoClienteApi.RUTA_RESUELTA}/$RUTA"

            object Operaciones
            {
                object GET
                {
                    const val numeroTransaccion: String = "transaction-number"

                    const val numeroTransaccion_tipo: String = "kotlin.String?"
                }
            }
        }

        internal object RuteoReservasApi
        {
            const val RUTA: String = "reservations"

            const val RUTA_RESUELTA: String = "${RuteoClienteApi.RUTA_RESUELTA}/$RUTA"

            object Operaciones
            {
                object GET
            }

            internal object RuteoReservaApi
            {
                const val RUTA: String = "{id_reserva}"

                const val PARAMETRO_RUTA: String = "id_reserva"

                const val RUTA_RESUELTA: String = "${RuteoReservasApi.RUTA_RESUELTA}/$RUTA"

                object Operaciones
                {
                    object DELETE

                    object GET

                    object PATCH

                    object PUT
                }
            }
        }

        internal object RuteoRolesApi
        {
            const val RUTA: String = "roles"

            const val RUTA_RESUELTA: String = "${RuteoClienteApi.RUTA_RESUELTA}/$RUTA"

            object Operaciones
            {
                object GET

                object POST
            }

            internal object RuteoRolApi
            {
                const val RUTA: String = "{rol_rol}"

                const val PARAMETRO_RUTA: String = "rol_rol"

                const val RUTA_RESUELTA: String = "${RuteoRolesApi.RUTA_RESUELTA}/$RUTA"

                object Operaciones
                {
                    object DELETE

                    object GET

                    object PUT
                }
            }
        }

        internal object RuteoSesionesDeManillaApi
        {
            const val RUTA: String = "tag-sessions"

            const val RUTA_RESUELTA: String = "${RuteoClienteApi.RUTA_RESUELTA}/$RUTA"

            object Operaciones

            internal object RuteoSesionDeManillaApi
            {
                const val RUTA: String = "{id_sesiondemanilla}"

                const val PARAMETRO_RUTA: String = "id_sesiondemanilla"

                const val RUTA_RESUELTA: String = "${RuteoSesionesDeManillaApi.RUTA_RESUELTA}/$RUTA"

                object Operaciones
                {
                    object GET

                    object PATCH
                }

                internal object RuteoOrdenesDeUnaSesionDeManillaApi
                {
                    const val RUTA: String = "orders"

                    const val RUTA_RESUELTA: String =
                            "${RuteoSesionDeManillaApi.RUTA_RESUELTA}/$RUTA"

                    object Operaciones
                    {
                        object GET
                    }
                }

                internal object RuteoPersonaPorIdSesionManillaApi
                {
                    const val RUTA: String = "person"

                    const val RUTA_RESUELTA: String =
                            "${RuteoSesionDeManillaApi.RUTA_RESUELTA}/$RUTA"

                    object Operaciones
                    {
                        object GET
                    }
                }
            }
        }

        internal object RuteoSkusApi
        {
            const val RUTA: String = "skus"

            const val RUTA_RESUELTA: String = "${RuteoClienteApi.RUTA_RESUELTA}/$RUTA"

            object Operaciones
            {
                object GET

                object POST
            }

            internal object RuteoSkuApi
            {
                const val RUTA: String = "{id_sku}"

                const val PARAMETRO_RUTA: String = "id_sku"

                const val RUTA_RESUELTA: String = "${RuteoSkusApi.RUTA_RESUELTA}/$RUTA"

                object Operaciones
                {
                    object DELETE

                    object GET

                    object PATCH

                    object PUT
                }
            }
        }

        internal object RuteoUbicacionesApi
        {
            const val RUTA: String = "locations"

            const val RUTA_RESUELTA: String = "${RuteoClienteApi.RUTA_RESUELTA}/$RUTA"

            object Operaciones
            {
                object GET

                object POST
            }

            internal object RuteoUbicacionApi
            {
                const val RUTA: String = "{id_ubicacion}"

                const val PARAMETRO_RUTA: String = "id_ubicacion"

                const val RUTA_RESUELTA: String = "${RuteoUbicacionesApi.RUTA_RESUELTA}/$RUTA"

                object Operaciones
                {
                    object DELETE

                    object GET

                    object PUT
                }

                internal object RuteoConsumiblesEnPuntoDeVentaApi
                {
                    const val RUTA: String = "consumables"

                    const val RUTA_RESUELTA: String = "${RuteoUbicacionApi.RUTA_RESUELTA}/$RUTA"

                    object Operaciones
                    {
                        object GET

                        object PUT
                    }
                }

                internal object RuteoConteosEnUbicacionApi
                {
                    const val RUTA: String = "count"

                    const val RUTA_RESUELTA: String = "${RuteoUbicacionApi.RUTA_RESUELTA}/$RUTA"

                    object Operaciones
                    {
                        object POST
                    }
                }

                internal object RuteoFondosEnPuntoDeVentaApi
                {
                    const val RUTA: String = "funds"

                    const val RUTA_RESUELTA: String = "${RuteoUbicacionApi.RUTA_RESUELTA}/$RUTA"

                    object Operaciones
                    {
                        object GET
                    }
                }
            }
        }

        internal object RuteoUbicacionesContabilizablesApi
        {
            const val RUTA: String = "countable-locations"

            const val RUTA_RESUELTA: String = "${RuteoClienteApi.RUTA_RESUELTA}/$RUTA"

            object Operaciones
            {
                object GET

                object PUT
            }
        }

        internal object RuteoUsuariosApi
        {
            const val RUTA: String = "users"

            const val RUTA_RESUELTA: String = "${RuteoClienteApi.RUTA_RESUELTA}/$RUTA"

            object Operaciones
            {
                object GET

                object POST
            }

            internal object RuteoUsuarioApi
            {
                const val RUTA: String = "{usuario_usuario}"

                const val PARAMETRO_RUTA: String = "usuario_usuario"

                const val RUTA_RESUELTA: String = "${RuteoUsuariosApi.RUTA_RESUELTA}/$RUTA"

                object Operaciones
                {
                    object DELETE

                    object GET

                    object PATCH

                    object POST

                    object PUT
                }

                object RuteoLoginApi
                {
                    const val RUTA: String = "login"

                    const val RUTA_RESUELTA: String = "${RuteoUsuarioApi.RUTA_RESUELTA}/$RUTA"

                    object Operaciones
                    {
                        object POST
                    }
                }

                object RuteoLogoutApi
                {
                    const val RUTA: String = "logout"

                    const val RUTA_RESUELTA: String = "${RuteoUsuarioApi.RUTA_RESUELTA}/$RUTA"

                    object Operaciones
                    {
                        object GET
                    }
                }
            }
        }

        internal object RuteoValoresGrupoEdadApi
        {
            const val RUTA: String = "age-groups"

            const val RUTA_RESUELTA: String = "${RuteoClienteApi.RUTA_RESUELTA}/$RUTA"

            object Operaciones
            {
                object GET

                object POST
            }

            internal object RuteoValorGrupoEdadApi
            {
                const val RUTA: String = "{valor_valorgrupoedad}"

                const val PARAMETRO_RUTA: String = "valor_valorgrupoedad"

                const val RUTA_RESUELTA: String = "${RuteoValoresGrupoEdadApi.RUTA_RESUELTA}/$RUTA"

                object Operaciones
                {
                    object DELETE

                    object GET

                    object PUT
                }
            }
        }
    }
}

