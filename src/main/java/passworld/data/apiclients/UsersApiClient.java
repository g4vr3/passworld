package passworld.data.apiclients;

import okhttp3.*;
import org.json.JSONObject;
import passworld.data.exceptions.CredentialsException;
import passworld.data.exceptions.EncryptionException;
import passworld.data.session.UserSession;
import passworld.utils.EncryptionUtil;


import java.io.IOException;

public class UsersApiClient {

    private static final String AUTH_BASE_URL = "https://identitytoolkit.googleapis.com/v1/accounts:";
    private static final String DATABASE_BASE_URL = "https://passworld-g4vj4vi-default-rtdb.firebaseio.com/users/";
    private static final String API_KEY = "AIzaSyB02VJOwdZp-QTf43icfVew7x0uNcdGEHE";
    private static final OkHttpClient client = new OkHttpClient();

    public static String registerUserWithMasterPassword(String email, String password, String masterPassword)
            throws CredentialsException, IOException, EncryptionException {
        // 1. Registrar usuario
        String registerEndpoint = AUTH_BASE_URL + "signUp?key=" + API_KEY;
        String registerRequestBody = String.format("""
    {
        "email": "%s",
        "password": "%s",
        "returnSecureToken": true
    }
    """, email, password);

        String registerResponse;
        try {
            registerResponse = sendRequest(registerEndpoint, "POST", registerRequestBody);
        } catch (IOException e) {
            if (e.getMessage().contains("EMAIL_EXISTS")) {
                throw new CredentialsException("El usuario ya existe.");
            } else {
                throw e;
            }
        }

        String userId = extractUserId(registerResponse);

        // 2. Iniciar sesión para obtener idToken
        loginUser(email, password);
        String idToken = UserSession.getInstance().getIdToken();
        String hashedMasterPassword = EncryptionUtil.hashMasterPassword(masterPassword);

        // 3. Crear nodos en la base de datos
        String databaseEndpoint = DATABASE_BASE_URL + userId + ".json?auth=" + idToken;
        String databaseRequestBody = String.format("""
    {
        "masterPassword": "%s"
    }
    """, hashedMasterPassword);

        sendRequest(databaseEndpoint, "PUT", databaseRequestBody);
        return hashedMasterPassword;

    }

    public static void loginUser(String email, String password)
            throws IOException, CredentialsException {
        String endpoint = "signInWithPassword?key=" + API_KEY;
        String requestBody = String.format("""
{
    "email": "%s",
    "password": "%s",
    "returnSecureToken": true
}
""", email, password);


        String response = sendRequest(AUTH_BASE_URL + endpoint, "POST", requestBody);

        JSONObject json = new JSONObject(response);
        if (json.has("error")) {
            String message = json.getJSONObject("error").getString("message");
            System.out.println("Error de Firebase: " + message);
            if (message.contains("INVALID_LOGIN_CREDENTIALS") || message.contains("EMAIL_NOT_FOUND") || message.contains("INVALID_EMAIL")) {
                throw new CredentialsException("Credenciales incorrectas");
            } else {
                throw new IOException("Error de autenticación: " + message);
            }
        }

        UserSession session = UserSession.getInstance();
        session.setLoggedIn(true);
        session.setUserId(json.getString("localId"));
        session.setIdToken(json.getString("idToken"));
        session.setRefreshToken(json.getString("refreshToken"));

    }
    public static String fetchMasterPassword() throws IOException {
        UserSession session = UserSession.getInstance();
        String userId = session.getUserId();
        String idToken = session.getIdToken();
        String url = "https://passworld-g4vj4vi-default-rtdb.firebaseio.com/users/" + userId + "/masterPassword.json?auth=" + idToken;
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Error al recuperar la master password: " + response.message());
            }
            // El valor es un string plano (hash)
            return response.body().string().replace("\"", "");
        }
    }


    // Método para refrescar el token
    public static void refreshIdToken(UserSession session) throws IOException {
        String url = "https://securetoken.googleapis.com/v1/token?key=" + API_KEY;
        String requestBody = String.format("grant_type=refresh_token&refresh_token=%s", session.getRefreshToken());

        RequestBody body = RequestBody.create(requestBody, MediaType.parse("application/x-www-form-urlencoded"));
        Request request = new Request.Builder().url(url).post(body).build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                JSONObject json = new JSONObject(response.body().string());
                session.setIdToken(json.getString("idToken"));
                session.setRefreshToken(json.getString("refreshToken"));
            } else {
                throw new IOException("No se pudo refrescar el token");
            }
        }
    }

    private static String sendRequest(String url, String method, String jsonData) throws IOException {
        RequestBody body = jsonData != null
                ? RequestBody.create(jsonData, MediaType.parse("application/json; charset=utf-8"))
                : null;

        Request request = new Request.Builder()
                .url(url)
                .method(method, body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                throw new IOException("Error en la solicitud HTTP: " + response.code() + " - " + responseBody);
            }
            return responseBody;
        }
    }

    private static String extractUserId(String response) {
        JSONObject jsonResponse = new JSONObject(response);
        return jsonResponse.getString("localId");
    }
}
