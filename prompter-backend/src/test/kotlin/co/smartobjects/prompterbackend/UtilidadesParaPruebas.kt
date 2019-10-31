package co.smartobjects.prompterbackend

import org.glassfish.jersey.client.HttpUrlConnectorProvider
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.mock
import javax.ws.rs.client.Entity
import javax.ws.rs.client.Invocation
import javax.ws.rs.core.MediaType


internal fun <T> mockConDefaultAnswer(clase: Class<T>): T
{
    return mock(clase) {
        val argumentos =
                it.arguments.joinToString(",\n\t\t").let {
                    if (it.isEmpty())
                    {
                        ""
                    }
                    else
                    {
                        "\n\t\t$it\n\t"
                    }
                }

        throw RuntimeException("Llamado a\n\t${it.method.declaringClass.simpleName}.${it.method.name}($argumentos)\n\tno esta mockeado\n[$it]")
    }
}

internal fun <TipoAEnviar, TipoRespuesta> Invocation.Builder.patch(entidad: TipoAEnviar, tipoDeRespuesta: Class<TipoRespuesta>): TipoRespuesta
{
    return this.property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
        .method<TipoRespuesta>("PATCH", Entity.entity<TipoAEnviar>(entidad, MediaType.APPLICATION_JSON_TYPE), tipoDeRespuesta)
}

internal fun <TipoAEnviar> Invocation.Builder.patch(entidad: TipoAEnviar)
{
    this.property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
        .method("PATCH", Entity.entity<TipoAEnviar>(entidad, MediaType.APPLICATION_JSON_TYPE))
}

internal fun <T : Any> eqParaKotlin(value: T): T = ArgumentMatchers.eq(value) ?: value

internal fun <T> cualquiera(): T
{
    ArgumentMatchers.any<T>()
    @Suppress("UNCHECKED_CAST")
    return null as T
}