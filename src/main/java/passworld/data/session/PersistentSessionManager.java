package passworld.data.session;

import org.json.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;

public class PersistentSessionManager {

    private static final String API_KEY = "AIzaSyB02VJOwdZp-QTf43icfVew7x0uNcdGEHE";
    private static final String AUTH_FILE = "auth.properties";

    // Este método asegura que la sesión esté activa usando la clase UserSession
    public static boolean tokenSavedLocally() {
        Properties props = loadProperties();
        String refreshToken = props.getProperty("refreshToken");
        if (refreshToken != null) {
            try {
                UserSession.getInstance().setRefreshToken(refreshToken);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean hasInternet() {
        try {
            URL url = new URL("https://www.google.com");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(2000);
            conn.connect();
            return conn.getResponseCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean refreshToken() {
        try {
            Properties props = loadProperties();
            String refreshToken = props.getProperty("refreshToken");
            if (refreshToken == null) return false;

            String url = "https://securetoken.googleapis.com/v1/token?key=" + API_KEY;
            String payload = "grant_type=refresh_token&refresh_token=" + refreshToken;

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.getOutputStream().write(payload.getBytes());

            if (conn.getResponseCode() == 200) {
                String response = new String(conn.getInputStream().readAllBytes());
                String newIdToken = extractJsonValue(response, "id_token");
                String newRefreshToken = extractJsonValue(response, "refresh_token");
                String uid = extractJsonValue(response, "user_id");

                // Guardamos los tokens en UserSession y en archivo de propiedades
                UserSession.getInstance().setIdToken(newIdToken);
                UserSession.getInstance().setRefreshToken(newRefreshToken);
                UserSession.getInstance().setUserId(uid);

                props.setProperty("idToken", newIdToken);
                props.setProperty("refreshToken", newRefreshToken);
                props.setProperty("uid", uid);

                saveProperties(props);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void saveTokens(String idToken, String refreshToken, String uid) throws Exception {
        Properties props = new Properties();
        props.setProperty("idToken", idToken);
        props.setProperty("refreshToken" ,refreshToken);
        props.setProperty("uid", uid);

        // Establecemos la sesión en memoria
        UserSession.getInstance().setIdToken(idToken);
        UserSession.getInstance().setRefreshToken(refreshToken);
        UserSession.getInstance().setUserId(uid);

        saveProperties(props);
    }

    public static void clearTokens() {
        new File(AUTH_FILE).delete();
        UserSession.getInstance().clearSession(); // Limpiar la sesión en memoria
    }

    private static String extractJsonValue(String json, String key) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            return jsonObject.getString(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // === Clave derivada del UUID del sistema ===
    private static SecretKeySpec getSystemKey() throws Exception {
        String uuid = getSystemUUID();
        byte[] keyBytes = uuid.substring(0, 16).getBytes(StandardCharsets.UTF_8); // 128-bit
        return new SecretKeySpec(keyBytes, "AES");
    }

    private static String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, getSystemKey());
        byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    private static String decrypt(String encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, getSystemKey());
        byte[] decoded = Base64.getDecoder().decode(encrypted);
        return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);

    }

    public static String getSystemUUID() {
        String uuid = null;
        try {
            // Comando de PowerShell para obtener el UUID
            String command = "powershell -Command \"(Get-WmiObject -Class Win32_ComputerSystemProduct).UUID\"";

            // Ejecuta el comando
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            // Lee la salida del comando
            while ((line = reader.readLine()) != null) {
                uuid = line.trim(); // Obtener el UUID
            }

            // Espera a que el proceso termine
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("UUID: " + uuid);
        return uuid;
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(AUTH_FILE)) {
            props.load(in);
        } catch (Exception ignored) {}
        return props;
    }

    private static void saveProperties(Properties props) {
        try (FileOutputStream out = new FileOutputStream(AUTH_FILE)) {
            props.store(out, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
