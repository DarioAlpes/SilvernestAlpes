package co.smartobjects.ui.modelos.login

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.naming.ServiceUnavailableException


import com.microsoft.aad.adal4j.AuthenticationContext
import com.microsoft.aad.adal4j.AuthenticationResult


class activeDirectory {

    @Throws(Exception::class)
    fun main(username: String, password: String): String {
        var userInfo = "Datos invalidos"
        // Request access token from AAD
        val result = getAccessTokenFromUserCredentials(
                username, password)
        // Get user info from Microsoft Graph
        if (result != null) {
            userInfo = getUserInfoFromGraph(result.accessToken)
        }

        return userInfo
    }

    @Throws(Exception::class)
    private fun getAccessTokenFromUserCredentials(username: String, password: String): AuthenticationResult? {
        val context: AuthenticationContext
        var result: AuthenticationResult? = null
        var service: ExecutorService? = null
        try {
            service = Executors.newFixedThreadPool(1)
            context = AuthenticationContext(AUTHORITY, false, service!!)
            val future = context.acquireToken(
                    "https://graph.microsoft.com", CLIENT_ID, username, password, null)
            result = future.get()
        } catch (e: Exception) {
            print("Error ......................")
            print(e)
            return null

        } finally {
            service!!.shutdown()
        }
        if (result == null) {
            throw ServiceUnavailableException(
                    "authentication result was null")
        }

        return result
    }

    @Throws(IOException::class)
    private fun getUserInfoFromGraph(accessToken: String): String {

        val url = URL("https://graph.microsoft.com/v1.0/me")
        val conn = url.openConnection() as HttpURLConnection

        conn.requestMethod = "GET"
        conn.setRequestProperty("Authorization", "Bearer $accessToken")
        conn.setRequestProperty("Accept", "application/json")

        val httpResponseCode = conn.responseCode
        if (httpResponseCode == 200) {
            var `in`: BufferedReader? = null
            val response: StringBuilder
            try {
                `in` = BufferedReader(
                        InputStreamReader(conn.inputStream))
                var inputLine: String? = null
                response = StringBuilder()
                while ({inputLine = `in`.readLine(); inputLine}()!= null) {
                    response.append(inputLine)
                }
            } finally {
                `in`!!.close()
            }
            return response.toString()
        } else {

            val responseponse = StringBuilder("{Connection returned HTTP code: %s with message: %s,\"}")
            return String.format("Connection returned HTTP code: %s with message: %s",
                    httpResponseCode, conn.responseMessage)
        }
    }

    companion object {

        /*private final static String AUTHORITY = "https://login.microsoftonline.com/fadb2e0a-c75f-4db4-a54d-a6d19a3c78bc/oauth2/authorize";
    private final static String CLIENT_ID = "774eca1e-f63b-4a93-9cd3-64743f2d3361";*/
        //PRODUCCION
        private val AUTHORITY = "https://login.microsoftonline.com/f33f0c61-f677-4ffe-a71f-0f3bd552dcfc/oauth2/authorize"
        private val CLIENT_ID = "86758390-6ac3-4699-b601-475b20d1f209"
    }
}

