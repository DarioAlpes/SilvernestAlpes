package co.smartobjects.ui.modelos.login;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.naming.ServiceUnavailableException;
import javax.swing.*;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;


public class publicClient {

    /*private final static String AUTHORITY = "https://login.microsoftonline.com/fadb2e0a-c75f-4db4-a54d-a6d19a3c78bc/oauth2/authorize";
    private final static String CLIENT_ID = "774eca1e-f63b-4a93-9cd3-64743f2d3361";*/
    //PRODUCCION
    private final static String AUTHORITY = "https://login.microsoftonline.com/f33f0c61-f677-4ffe-a71f-0f3bd552dcfc/oauth2/authorize";
    private final static String CLIENT_ID = "86758390-6ac3-4699-b601-475b20d1f209";

    public String main(String username, String password) throws Exception {
        String userInfo = "Datos invalidos";
        JOptionPane.showMessageDialog(null, "Entro");
            // Request access token from AAD
            AuthenticationResult result = getAccessTokenFromUserCredentials(
                    username, password);
            // Get user info from Microsoft Graph
            if(result!=null){
                userInfo = getUserInfoFromGraph(result.getAccessToken());
            }

        return userInfo;
    }

    private  AuthenticationResult getAccessTokenFromUserCredentials(String username, String password) throws Exception {
        AuthenticationContext context;
        AuthenticationResult result = null;
        ExecutorService service = null;
        try {
            service = Executors.newFixedThreadPool(1);
            context = new AuthenticationContext(AUTHORITY, false, service);
            Future<AuthenticationResult> future = context.acquireToken(
                    "https://graph.microsoft.com", CLIENT_ID, username, password,
                    null);
            result = future.get();
        }catch (Exception e)
        {
            System.out.print("Error ......................");
            System.out.print(e);
            return null;

        }
        finally {
            service.shutdown();
        }
        if (result == null) {
            throw new ServiceUnavailableException(
                    "authentication result was null");
        }

        return result;
    }

    private String getUserInfoFromGraph(String accessToken) throws IOException {

        URL url = new URL("https://graph.microsoft.com/v1.0/me");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Accept","application/json");

        int httpResponseCode = conn.getResponseCode();
        if(httpResponseCode == 200) {
            BufferedReader in = null;
            StringBuilder response;
            try{
                in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String inputLine;
                response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            } finally {
                in.close();
            }

            return response.toString();
        } else {

            StringBuilder responseponse = new StringBuilder("{Connection returned HTTP code: %s with message: %s,\"}");
            return String.format("Connection returned HTTP code: %s with message: %s",
                    httpResponseCode, conn.getResponseMessage());
        }
    }
}

