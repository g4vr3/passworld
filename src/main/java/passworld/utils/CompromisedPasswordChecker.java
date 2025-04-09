package passworld.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CompromisedPasswordChecker {
    private static final String API_URL = "https://api.pwnedpasswords.com/range/";

    public static boolean isCompromisedPassword(String password) throws Exception {
        String hash = sha1(password);
        String prefix = hash.substring(0, 5);
        String suffix = hash.substring(5).toUpperCase();

        URL url = new URL(API_URL + prefix);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(suffix)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String sha1(String input) throws Exception {
        java.security.MessageDigest mDigest = java.security.MessageDigest.getInstance("SHA-1");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : result) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString().toUpperCase();
    }
}