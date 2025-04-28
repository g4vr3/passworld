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
                return responseBody != null && !responseBody.trim().isEmpty();
            }
        } catch (Exception e) {
            System.err.println("Error checking URL safety: " + e.getMessage());
        }
        return false;
    }
}
