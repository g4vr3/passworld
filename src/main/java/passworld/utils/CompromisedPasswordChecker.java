package passworld.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;

public class CompromisedPasswordChecker {
    private static final String API_URL = "https://api.pwnedpasswords.com/range/";

    public static boolean isCompromisedPassword(String password) throws Exception {
        String hash = sha1(password);
        String prefix = hash.substring(0, 5);
        String suffix = hash.substring(5).toUpperCase();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + prefix))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        String[] lines = response.body().split("\n");
        for (String line : lines) {
            if (line.startsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    private static String sha1(String input) throws Exception {
        MessageDigest mDigest = MessageDigest.getInstance("SHA-1");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : result) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString().toUpperCase();
    }
}