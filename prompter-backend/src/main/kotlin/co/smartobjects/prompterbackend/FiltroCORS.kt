package co.smartobjects.prompterbackend

import java.io.IOException
import java.util.*
import javax.ws.rs.ForbiddenException
import javax.ws.rs.container.*
import javax.ws.rs.core.Response

/**
 * Handles CORS requests both preflight and simple CORS requests.
 * You must bind this as a singleton and set up origenesPermitidos and other settings to use.
 *
 * https://github.com/resteasy/Resteasy/blob/master/resteasy-jaxrs/src/main/java/org/jboss/resteasy/plugins/interceptors/CorsFilter.java
 *
 * @author [Bill Burke](mailto:bill@burkecentral.com)
 * @version $Revision: 1 $
 */
@PreMatching
internal open class FiltroCORS : ContainerRequestFilter, ContainerResponseFilter
{
    object CorsHeaders
    {
        const val ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin"
        const val ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials"
        const val ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods"
        const val ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers"
        const val ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age"
        const val ORIGIN = "Origin"
        const val ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method"
        const val ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers"
        const val ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers"
        const val VARY = "Vary"
    }

    /**
     * Defaults to true
     *
     * @return
     */
    var permitirCredenciales = true

    /**
     * Will allow all by default
     * comma delimited string for Access-Control-Allow-Methods (e.g. "GET, POST, PUT, DELETE, OPTIONS, HEAD")
     *
     * @param allowedMethods
     */
    var metodosPermitidos: String? = null

    /**
     * Will allow all by default
     * comma delimited string for Access-Control-Allow-Headers (e.g. "origin, content-type, accept, authorization")
     *
     * @param allowedHeaders
     */
    var headersPermitidos: String? = null

    /**
     * comma delimited list
     *
     * @param exposedHeaders
     */
    var exposedHeaders: String? = null
    var corsMaxAge: Int = -1

    /**
     * Put "*" if you want to accept all origins
     *
     * @return
     */
    val origenesPermitidos: HashSet<String> = HashSet<String>().also {
        it.add("*")
    }

    @Throws(IOException::class)
    override fun filter(requestContext: ContainerRequestContext)
    {
        val origin = requestContext.getHeaderString(CorsHeaders.ORIGIN) ?: return
        if (requestContext.method.equals("OPTIONS", ignoreCase = true))
        {
            preflight(origin, requestContext)
        }
        else
        {
            checkOrigin(requestContext, origin)
        }
    }

    @Throws(IOException::class)
    override fun filter(requestContext: ContainerRequestContext, responseContext: ContainerResponseContext)
    {
        val origin = requestContext.getHeaderString(CorsHeaders.ORIGIN)
        if (origin == null || requestContext.method.equals("OPTIONS", ignoreCase = true) || requestContext.getProperty("cors.failure") != null)
        {
            return
        }

        responseContext.headers.putSingle(CorsHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin)
        responseContext.headers.putSingle(CorsHeaders.VARY, CorsHeaders.ORIGIN)

        if (permitirCredenciales)
        {
            responseContext.headers.putSingle(CorsHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true")
        }

        if (exposedHeaders != null)
        {
            responseContext.headers.putSingle(CorsHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, exposedHeaders)
        }
    }


    @Throws(IOException::class)
    protected fun preflight(origin: String, requestContext: ContainerRequestContext)
    {
        checkOrigin(requestContext, origin)

        val builder = Response.ok()
        builder.header(CorsHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin)
        builder.header(CorsHeaders.VARY, CorsHeaders.ORIGIN)

        if (permitirCredenciales)
        {
            builder.header(CorsHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true")
        }

        var requestMethods: String? = requestContext.getHeaderString(CorsHeaders.ACCESS_CONTROL_REQUEST_METHOD)

        if (requestMethods != null)
        {
            if (metodosPermitidos != null)
            {
                requestMethods = this.metodosPermitidos
            }
            builder.header(CorsHeaders.ACCESS_CONTROL_ALLOW_METHODS, requestMethods)
        }

        var allowHeaders: String? = requestContext.getHeaderString(CorsHeaders.ACCESS_CONTROL_REQUEST_HEADERS)

        if (allowHeaders != null)
        {
            if (headersPermitidos != null)
            {
                allowHeaders = this.headersPermitidos
            }
            builder.header(CorsHeaders.ACCESS_CONTROL_ALLOW_HEADERS, allowHeaders)
        }

        if (corsMaxAge > -1)
        {
            builder.header(CorsHeaders.ACCESS_CONTROL_MAX_AGE, corsMaxAge)
        }

        requestContext.abortWith(builder.build())

    }

    private fun checkOrigin(requestContext: ContainerRequestContext, origin: String)
    {
        if (!origenesPermitidos.contains("*") && !origenesPermitidos.contains(origin))
        {
            requestContext.setProperty("cors.failure", true)
            throw ForbiddenException("Origin not allowed: $origin")
        }
    }
}