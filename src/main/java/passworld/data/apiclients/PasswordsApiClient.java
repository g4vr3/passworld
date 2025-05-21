package passworld.data.apiclients;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import passworld.data.PasswordDTO;
import passworld.data.session.UserSession;
import passworld.utils.LogUtils;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class PasswordsApiClient {

    private static final String BASE_URL = "https://passworld-g4vj4vi-default-rtdb.firebaseio.com/users";
    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Crear una nueva contraseña para un usuario
    public static String createPassword(String userId, PasswordDTO password) throws IOException {
        String authToken = UserSession.getInstance().getIdToken();
        String passwordsEndpoint = BASE_URL + "/" + userId + "/passwords.json?auth=" + authToken;
        String requestBody = String.format("""
        {
            "description": "%s",
            "username": "%s",
            "url": "%s",
            "password": "%s",
            "isWeak": %b,
            "isDuplicate": %b,
            "isCompromised": %b,
            "isUrlUnsafe": %b,
            "lastModified": "%S"
        }
    """, password.getDescription(), password.getUsername(),
                password.getUrl(), password.getPassword(),
                password.isWeak(), password.isDuplicate(),
                password.isCompromised(), password.isUrlUnsafe(),password.getLastModified());

        String response = sendRequest(passwordsEndpoint, "POST", requestBody);

        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);

        LogUtils.LOGGER.info("Password created successfully");

        // Devuelve el id generado por Firebase (clave "name")
        return responseMap != null ? (String) responseMap.get("name") : null;
    }

    // Leer todas las contraseñas
    public static List<PasswordDTO> readAllPasswords(String userId) throws IOException {
        String authToken = UserSession.getInstance().getIdToken();
        String endpoint = BASE_URL + "/" + userId + "/passwords.json?auth=" + authToken;
        String response = sendRequest(endpoint, "GET", null);

        List<PasswordDTO> passwordList = new ArrayList<>();
        if (!response.equals("null")) {
            Map<String, Map<String, Object>> passwords = objectMapper.readValue(response, Map.class);
            if (passwords != null) {
                for (Map.Entry<String, Map<String, Object>> entry : passwords.entrySet()) {
                    Map<String, Object> passwordData = entry.getValue();
                    PasswordDTO passwordDTO = new PasswordDTO(
                            (String) passwordData.get("description"),
                            (String) passwordData.get("username"),
                            (String) passwordData.get("url"),
                            (String) passwordData.get("password")
                    );
                    passwordDTO.setWeak((Boolean) passwordData.getOrDefault("isWeak", false));
                    passwordDTO.setDuplicate((Boolean) passwordData.getOrDefault("isDuplicate", false));
                    passwordDTO.setCompromised((Boolean) passwordData.getOrDefault("isCompromised", false));
                    passwordDTO.setUrlUnsafe((Boolean) passwordData.getOrDefault("isUrlUnsafe", false));
                    passwordDTO.setLastModified(parseLastModified(passwordData.get("lastModified")));
                    passwordDTO.setIdFb(entry.getKey());
                    passwordList.add(passwordDTO);
                }
            }
        }

        LogUtils.LOGGER.info("User passwords read successfully from remote database");
        return passwordList;
    }

    // Leer una contraseña por su ID
    public static PasswordDTO readPassword(String userId, String passwordId) throws IOException {
        String authToken = UserSession.getInstance().getIdToken();
        String endpoint = BASE_URL + "/" + userId + "/passwords/" + passwordId + ".json?auth=" + authToken;
        String response = sendRequest(endpoint, "GET", null);

        if (!response.equals("null")) {
            Map<String, Object> passwordData = objectMapper.readValue(response, Map.class);
            PasswordDTO passwordDTO = new PasswordDTO(
                    (String) passwordData.get("description"),
                    (String) passwordData.get("username"),
                    (String) passwordData.get("url"),
                    (String) passwordData.get("password")
            );
            passwordDTO.setWeak((Boolean) passwordData.getOrDefault("isWeak", false));
            passwordDTO.setDuplicate((Boolean) passwordData.getOrDefault("isDuplicate", false));
            passwordDTO.setCompromised((Boolean) passwordData.getOrDefault("isCompromised", false));
            passwordDTO.setUrlUnsafe((Boolean) passwordData.getOrDefault("isUrlUnsafe", false));
            passwordDTO.setLastModified(parseLastModified(passwordData.get("lastModified")));
            passwordDTO.setIdFb(passwordId);

            LogUtils.LOGGER.info("User password read successfully from remote database");
            return passwordDTO;
        }

        LogUtils.LOGGER.warning("Password not found in remote database");
        return null;
    }

    // Actualizar contraseña existente
    public static void updatePassword(String userId, String passwordId, String description, String username, String url, String password,
                                      boolean isWeak, boolean isDuplicate, boolean isCompromised, boolean isUrlUnsafe, String lastModified) throws IOException {
        String authToken = UserSession.getInstance().getIdToken();
        String endpoint = BASE_URL + "/" + userId + "/passwords/" + passwordId + ".json?auth=" + authToken;

        String requestBody = String.format("""
            {
                "description": "%s",
                "username": "%s",
                "url": "%s",
                "password": "%s",
                "isWeak": %b,
                "isDuplicate": %b,
                "isCompromised": %b,
                "isUrlUnsafe": %b,
                "lastModified": "%s"
            }
        """, description, username, url, password, isWeak, isDuplicate, isCompromised, isUrlUnsafe, lastModified);

        LogUtils.LOGGER.info("User password update successfully");

        sendRequest(endpoint, "PUT", requestBody);
    }

    // Eliminar contraseña
    public static void deletePassword(String userId, String passwordId) throws IOException {
        String authToken = UserSession.getInstance().getIdToken();
        String endpoint = BASE_URL + "/" + userId + "/passwords/" + passwordId + ".json?auth=" + authToken;
        sendRequest(endpoint, "DELETE", null);

        // Verificar si fue eliminada
        PasswordDTO deleted = readPassword(userId, passwordId);

        if (deleted != null) {
            LogUtils.LOGGER.warning("Password not deleted from remote database");
        } else {
            LogUtils.LOGGER.info("User password deleted successfully from remote database");
        }

    }

    // Método auxiliar para enviar solicitudes HTTP
    private static String sendRequest(String url, String method, String jsonData) throws IOException {
        RequestBody body = jsonData != null
                ? RequestBody.create(jsonData, MediaType.parse("application/json; charset=utf-8"))
                : null;

        Request request = new Request.Builder()
                .url(url)
                .method(method, body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {

                LogUtils.LOGGER.info("Request to remote database successfully");

                return response.body().string();
            } else {
                LogUtils.LOGGER.severe("Request to remote database failed with code: " + response.code() + " and message: " + response.message());
                throw new IOException("Error: " + (response.body() != null ? response.body().string() : "Unknown error"));
            }
        }
    }

    // Parsear lastModified a LocalDateTime
    private static LocalDateTime parseLastModified(Object lastModifiedObj) {
        if (lastModifiedObj instanceof Number) {
            long timestampMillis = ((Number) lastModifiedObj).longValue();
            return Instant.ofEpochMilli(timestampMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
        } else if (lastModifiedObj instanceof String) {
            try {
                return LocalDateTime.parse((String) lastModifiedObj);
            } catch (Exception e) {
                LogUtils.LOGGER.warning("Error parsing lastModified: " + e);
                // Manejo de error si el formato no es válido
                return null;
            }
        }
        return null;
    }
}
