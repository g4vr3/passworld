package passworld.utils;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UnsafeUrlChecker {
    private static final String API_URL = "https://safebrowsing.googleapis.com/v4/threatMatches:find";
    private static final String API_KEY = ""; // API key

    public static boolean isUnsafe(String urlToCheck) {
        try {
            URL url = new URL(API_URL + "?key=" + API_KEY);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            JSONObject requestBody = new JSONObject();
            requestBody.put("client", new JSONObject().put("clientId", "PassworldSecurityChecker").put("clientVersion", "1.0"));
            requestBody.put("threatInfo", new JSONObject()
                    .put("threatTypes", new String[]{"MALWARE", "SOCIAL_ENGINEERING"})
                    .put("platformTypes", new String[]{"WINDOWS"})
                    .put("threatEntryTypes", new String[]{"URL"})
                    .put("threatEntries", new JSONObject[]{new JSONObject().put("url", urlToCheck)}));

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    return !response.toString().isEmpty();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}