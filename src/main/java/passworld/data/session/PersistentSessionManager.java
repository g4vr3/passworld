package passworld.data.session;

import org.json.JSONObject;
import passworld.data.exceptions.EncryptionException;
import passworld.utils.LogUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Objects;
import java.util.Properties;

public class PersistentSessionManager {

    private static final String API_KEY = "AIzaSyB02VJOwdZp-QTf43icfVew7x0uNcdGEHE";
    private static final String authFilePath;

    static {
        String userHome = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            authFilePath = userHome + "\\AppData\\Local\\passworld\\auth\\auth.properties";
        } else if (os.contains("mac")) {
            authFilePath = userHome + "/Library/Application Support/passworld/auth/auth.properties";
        } else {
            authFilePath = userHome + "/.local/share/passworld/auth/auth.properties";
        }

        File authDir = new File(authFilePath).getParentFile();
        if (!authDir.exists()) {
            boolean created = authDir.mkdirs();
            if (created) {
                System.out.println("Directorio creado: " + authDir.getAbsolutePath());
                LogUtils.LOGGER.info("Auth direcroty created successfully: " + authDir.getAbsolutePath());
            } else {
                System.out.println("No se pudo crear el directorio.");
                LogUtils.LOGGER.severe("Failed to create auth directory: " + authDir.getAbsolutePath());
            }
        }
    }

    // Comprueba si hay un token guardado localmente
    public static boolean tokenSavedLocally() {
        Properties props = loadProperties();
        String refreshToken = props.getProperty("refreshToken");
        if (refreshToken != null) {
            try {
                UserSession.getInstance().setRefreshToken(decrypt(refreshToken));
                return true;
            } catch (Exception e) {
                LogUtils.LOGGER.severe("Error fetching the refresh token: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return false;
    }

    // Verifica si hay conexión a Internet
    public static boolean hasInternet() {
        try {
            URL url = new URL("https://www.google.com");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(2000);
            conn.connect();
            LogUtils.LOGGER.info("Internet connection available");
            return conn.getResponseCode() == 200;
        } catch (Exception e) {

            return false;
        }
    }

    // Refresca el token con Firebase
    public static void refreshToken() {
        try {
            Properties props = loadProperties();
            String refreshToken = props.getProperty("refreshToken");
            if (refreshToken == null) return;

            String decryptedRefreshToken = decrypt(refreshToken);
            String url = "https://securetoken.googleapis.com/v1/token?key=" + API_KEY;
            String payload = "grant_type=refresh_token&refresh_token=" + decryptedRefreshToken;

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

                // Guardamos en sesión y archivo (encriptado)
                UserSession.getInstance().setIdToken(newIdToken);
                UserSession.getInstance().setRefreshToken(newRefreshToken);
                UserSession.getInstance().setUserId(uid);

                props.setProperty("idToken", encrypt(Objects.requireNonNull(newIdToken)));
                props.setProperty("refreshToken", encrypt(Objects.requireNonNull(newRefreshToken)));
                props.setProperty("uid", encrypt(Objects.requireNonNull(uid)));

                saveProperties(props);
                LogUtils.LOGGER.info("Token refreshed successfully with Firebase");
            }
        } catch (Exception e) {
            LogUtils.LOGGER.severe("Error refreshing token with Firebase: " + e);
            e.printStackTrace();
        }
    }

    // Establece el UID en UserSession a partir del archivo
    public static void setUserId() {
        Properties props = loadProperties();
        String uid = props.getProperty("uid");
        if (uid != null) {
            try {
                UserSession.getInstance().setUserId(decrypt(uid));
                LogUtils.LOGGER.info("User id set successfully");
            } catch (Exception e) {
                LogUtils.LOGGER.severe("Error setting the user id: " + e);
                e.printStackTrace();
            }
        }
    }

    // Guarda los tokens en sesión y archivo (encriptado)
    public static void saveTokens(String idToken, String refreshToken, String uid) throws EncryptionException {
        Properties props = new Properties();
        try {
            props.setProperty("idToken", encrypt(idToken));
            LogUtils.LOGGER.info("idToken encrypted successfully");
        } catch (Exception e) {
            LogUtils.LOGGER.severe("Error encrypting the idToken: " + e);
            throw new EncryptionException("Error al cifrar el idToken", e);
        }

        try {
            props.setProperty("refreshToken", encrypt(refreshToken));
            LogUtils.LOGGER.info("refreshToken encrypted successfully");
        } catch (Exception e) {
            LogUtils.LOGGER.severe("Error encrypting the refreshToken: " + e);
            throw new EncryptionException("Error al cifrar el refreshToken", e);
        }
        try {
            props.setProperty("uid", encrypt(uid));
            LogUtils.LOGGER.info("uid encrypted successfully");
        } catch (Exception e) {
            LogUtils.LOGGER.severe("Error encrypting the uid: " + e);
            throw new EncryptionException("Error al cifrar el uid", e);
        }

        UserSession.getInstance().setIdToken(idToken);
        UserSession.getInstance().setRefreshToken(refreshToken);
        UserSession.getInstance().setUserId(uid);

        saveProperties(props);
        LogUtils.LOGGER.info("Tokens saved successfully");
    }

    // Borra los tokens
    public static void clearTokens() {
        try {
            UserSession.getInstance().clearSession();
            Files.delete(Path.of(authFilePath));
        } catch (SQLException | IOException e) {
            LogUtils.LOGGER.severe("Error clearing tokens: " + e);
        }


    }

    // Extrae un valor de un JSON plano
    private static String extractJsonValue(String json, String key) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            return jsonObject.getString(key);
        } catch (Exception e) {
            LogUtils.LOGGER.severe("Error extracting JSON value: " + e);
            e.printStackTrace();
            return null;
        }
    }

    // === CIFRADO/DECIFRADO AES CON CLAVE DERIVADA DEL UUID DEL SISTEMA ===

    private static SecretKeySpec getSystemKey() {
        String uuid = getSystemUUID();
        byte[] keyBytes = uuid.substring(0, 16).getBytes(StandardCharsets.UTF_8);
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
    // Añade una variable estática para cachear el UUID
    private static String cachedUUID = null;

    // === OBTENER UUID DEL SISTEMA COMPATIBLE CROSS-PLATFORM ===
    public static String getSystemUUID() {
        if (cachedUUID != null) return cachedUUID; // Ya está cacheado

        String os = System.getProperty("os.name").toLowerCase();
        String uuid = null;

        try {
            Process process = null;

            if (os.contains("win")) {
                // PowerShell moderno para Windows 10/11
                String command = "powershell -Command \"(Get-CimInstance -Class Win32_ComputerSystemProduct).UUID\"";
                process = Runtime.getRuntime().exec(command);
                LogUtils.LOGGER.info("Executing command: " + command);
            } else if (os.contains("mac")) {
                process = Runtime.getRuntime().exec(new String[]{"ioreg", "-rd1", "-c", "IOPlatformExpertDevice"});
                LogUtils.LOGGER.info("Executing command: ioreg -rd1 -c IOPlatformExpertDevice");
            } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
                File machineId = new File("/etc/machine-id");
                if (machineId.exists()) {
                    cachedUUID = new String(java.nio.file.Files.readAllBytes(machineId.toPath())).trim();
                    return cachedUUID;
                }

                File dbusId = new File("/var/lib/dbus/machine-id");
                if (dbusId.exists()) {
                    cachedUUID = new String(java.nio.file.Files.readAllBytes(dbusId.toPath())).trim();
                    return cachedUUID;
                }

                LogUtils.LOGGER.warning("No such machine-id: " + cachedUUID);
                process = Runtime.getRuntime().exec("cat /etc/machine-id");
            }

            if (process != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.toLowerCase().contains("uuid") && !line.contains("=")) {
                        uuid = line;
                        break;
                    }
                }
                process.waitFor();
            }

        } catch (Exception e) {
            LogUtils.LOGGER.severe("Error getting system UUID: " + e);
            e.printStackTrace();
        }

        if (uuid == null) {
            uuid = "fallback-uuid-123456"; // fallback
        }

        cachedUUID = uuid;
        System.out.println("UUID: " + cachedUUID);
        return cachedUUID;
    }

    // === UTILIDADES DE PROPIEDADES ===

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(authFilePath)) {
            props.load(in);
        } catch (Exception ignored) {}
        return props;
    }

    private static void saveProperties(Properties props) {
        try (FileOutputStream out = new FileOutputStream(authFilePath)) {
            props.store(out, null);
        } catch (Exception e) {
            LogUtils.LOGGER.severe("Error saving properties: " + e);
            e.printStackTrace();
        }
    }
}
