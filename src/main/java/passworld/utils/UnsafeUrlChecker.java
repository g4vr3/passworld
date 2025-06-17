package passworld.utils;

import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class UnsafeUrlChecker {
    private static final String API_URL = "https://safebrowsing.googleapis.com/v4/threatMatches:find";
    private static final String API_KEY = ""; // API Key

    public static boolean isUnsafe(String urlToCheck) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("client", new JSONObject()
                    .put("clientId", "PassworldSecurityChecker")
                    .put("clientVersion", "1.0"));
            requestBody.put("threatInfo", new JSONObject()
                    .put("threatTypes", new String[]{"MALWARE", "SOCIAL_ENGINEERING"})
                    .put("platformTypes", new String[]{"WINDOWS"})
                    .put("threatEntryTypes", new String[]{"URL"})
                    .put("threatEntries", new JSONObject[]{new JSONObject().put("url", urlToCheck)}));

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "?key=" + API_KEY))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseBody = response.body();
                
                // Si la respuesta está vacía o no contiene datos, la URL es segura
                if (responseBody == null || responseBody.trim().isEmpty()) {
                    return false; // URL segura
                }
                
                // Si hay contenido en la respuesta, verificar si contiene amenazas
                try {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    return jsonResponse.has("matches"); // Si tiene "matches", es insegura
                } catch (Exception e) {
                    // Si no se puede parsear como JSON, asumir que es segura
                    return false;
                }
            } else {
                LogUtils.LOGGER.warning("Safe Browsing API error: " + response.statusCode());
            }
        } catch (Exception e) {
            LogUtils.LOGGER.warning("Error checking URL safety: " + e);
        }
        
        // En caso de error, asumir que es segura para no bloquear innecesariamente
        return false;
    }
}