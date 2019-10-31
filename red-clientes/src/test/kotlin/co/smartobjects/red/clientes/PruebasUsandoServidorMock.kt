package co.smartobjects.red.clientes

import co.smartobjects.red.clientes.retrofit.ManejadorDePeticiones
import co.smartobjects.red.clientes.retrofit.ManejadorDePeticionesRetrofit
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.clearInvocations
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal abstract class PruebasUsandoServidorMock<Api>
{
    protected val ID_CLIENTE = 1L

    private lateinit var mockServidor: MockWebServer
    protected lateinit var urlBase: String

    protected abstract fun ManejadorDePeticiones.extraerApi(): Api
    protected abstract val mockParser: ParserRespuestasRetrofit

    protected val api: Api by lazy {
        ManejadorDePeticionesRetrofit(idCliente = ID_CLIENTE, urlBase = urlBase, parserDeRespuestas = mockParser).extraerApi()
    }

    @BeforeAll
    fun subirServidor()
    {
        mockServidor = MockWebServer()
        mockServidor.start()
        urlBase = mockServidor.url("").toString()
    }

    @BeforeEach
    fun prepararMocks()
    {
        clearInvocations(mockParser)
    }

    @AfterAll
    fun bajarServidor()
    {
        mockServidor.shutdown()
    }

    protected inline fun llamarBackendCon(jsonRespuesta: String, llamadoAlBackend: () -> Unit): RecordedRequest
    {
        return with(mockServidor)
        {
            enqueue(MockResponse().setBody(jsonRespuesta).setResponseCode(200).addHeader("Content-Type", "application/json"))
            try
            {
                llamadoAlBackend()
                takeRequest()
            }
            catch (e: Exception)
            {
                mockServidor.takeRequest(50, TimeUnit.MILLISECONDS)
            }
        }
    }
}