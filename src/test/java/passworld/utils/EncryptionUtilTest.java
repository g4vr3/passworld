package passworld.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import passworld.data.exceptions.EncryptionException;
import passworld.data.session.UserSession;

import javax.crypto.spec.SecretKeySpec;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Clase de pruebas para EncryptionUtil.
 * 
 * Esta clase verifica la funcionalidad del sistema de encriptación utilizado
 * para proteger las contraseñas y datos sensibles. Las pruebas cubren:
 * - Generación y verificación de hash de contraseña maestra
 * - Derivación de claves AES a partir de contraseñas
 * - Encriptación y desencriptación de datos con AES
 * - Manejo de casos extremos (datos nulos, vacíos, inválidos)
 * - Verificación de consistencia criptográfica
 * - Pruebas con claves incorrectas y datos corruptos
 * - Validación del uso de salt basado en ID de usuario
 * - Rendimiento con datos de gran tamaño
 * 
 * Utiliza Mockito para simular UserSession y obtener resultados consistentes.
 */
class EncryptionUtilTest {

    // Constantes de prueba para mantener consistencia
    private static final String TEST_USER_ID = "testuser123";
    private static final String TEST_PASSWORD = "TestPassword123!";
    private static final String TEST_PLAINTEXT = "This is a test message";

    /**
     * Configuración inicial que se ejecuta antes de cada prueba.
     * Configura el mock de UserSession para proporcionar un salt consistente.
     */
    @BeforeEach
    void setUp() {
        // Mock UserSession para proporcionar salt consistente
        try (MockedStatic<UserSession> mockedUserSession = Mockito.mockStatic(UserSession.class)) {
            UserSession mockSession = mock(UserSession.class);
            when(mockSession.getUserId()).thenReturn(TEST_USER_ID);
            mockedUserSession.when(UserSession::getInstance).thenReturn(mockSession);
        }
    }

    /**
     * Prueba la generación de hash de la contraseña maestra.
     * Verifica que el hash sea consistente y no esté vacío.
     */
    @Test
    @DisplayName("Test master password hashing")
    void testHashMasterPassword() {
        try (MockedStatic<UserSession> mockedUserSession = Mockito.mockStatic(UserSession.class)) {
            UserSession mockSession = mock(UserSession.class);
            when(mockSession.getUserId()).thenReturn(TEST_USER_ID);
            mockedUserSession.when(UserSession::getInstance).thenReturn(mockSession);

            assertDoesNotThrow(() -> {
                String hash1 = EncryptionUtil.hashMasterPassword(TEST_PASSWORD);
                String hash2 = EncryptionUtil.hashMasterPassword(TEST_PASSWORD);
                
                assertNotNull(hash1);
                assertNotNull(hash2);
                assertEquals(hash1, hash2, "Same password should produce same hash");
                assertFalse(hash1.isEmpty(), "Hash should not be empty");
            });
        }
    }

    /**
     * Prueba la verificación de la contraseña maestra.
     * Verifica que la verificación funcione correctamente con contraseñas correctas e incorrectas.
     */
    @Test
    @DisplayName("Test master password verification")
    void testVerifyMasterPassword() {
        try (MockedStatic<UserSession> mockedUserSession = Mockito.mockStatic(UserSession.class)) {
            UserSession mockSession = mock(UserSession.class);
            when(mockSession.getUserId()).thenReturn(TEST_USER_ID);
            mockedUserSession.when(UserSession::getInstance).thenReturn(mockSession);

            assertDoesNotThrow(() -> {
                String hash = EncryptionUtil.hashMasterPassword(TEST_PASSWORD);
                
                assertTrue(EncryptionUtil.verifyMasterPassword(TEST_PASSWORD, hash),
                          "Correct password should verify successfully");
                
                assertFalse(EncryptionUtil.verifyMasterPassword("WrongPassword", hash),
                           "Wrong password should fail verification");
                
                assertFalse(EncryptionUtil.verifyMasterPassword("", hash),
                           "Empty password should fail verification");
            });
        }
    }

    /**
     * Prueba la derivación de claves AES a partir de contraseñas.
     * Verifica que la misma contraseña siempre genere la misma clave AES.
     */
    @Test
    @DisplayName("Test AES key derivation")
    void testDeriveAESKey() {
        try (MockedStatic<UserSession> mockedUserSession = Mockito.mockStatic(UserSession.class)) {
            UserSession mockSession = mock(UserSession.class);
            when(mockSession.getUserId()).thenReturn(TEST_USER_ID);
            mockedUserSession.when(UserSession::getInstance).thenReturn(mockSession);

            assertDoesNotThrow(() -> {
                SecretKeySpec key1 = EncryptionUtil.deriveAESKey(TEST_PASSWORD);
                SecretKeySpec key2 = EncryptionUtil.deriveAESKey(TEST_PASSWORD);
                
                assertNotNull(key1);
                assertNotNull(key2);
                assertEquals("AES", key1.getAlgorithm());
                assertEquals("AES", key2.getAlgorithm());
                assertArrayEquals(key1.getEncoded(), key2.getEncoded(),
                                "Same password should derive same key");
            });
        }
    }

    /**
     * Prueba la encriptación y desencriptación de datos.
     * Verifica que los datos se encripten correctamente y puedan ser desencriptados
     * para recuperar el texto original.
     */
    @Test
    @DisplayName("Test data encryption and decryption")
    void testEncryptDecryptData() {
        try (MockedStatic<UserSession> mockedUserSession = Mockito.mockStatic(UserSession.class)) {
            UserSession mockSession = mock(UserSession.class);
            when(mockSession.getUserId()).thenReturn(TEST_USER_ID);
            mockedUserSession.when(UserSession::getInstance).thenReturn(mockSession);

            assertDoesNotThrow(() -> {
                SecretKeySpec key = EncryptionUtil.deriveAESKey(TEST_PASSWORD);
                
                String encrypted = EncryptionUtil.encryptData(TEST_PLAINTEXT, key);
                assertNotNull(encrypted);
                assertFalse(encrypted.isEmpty());
                assertNotEquals(TEST_PLAINTEXT, encrypted);
                
                String decrypted = EncryptionUtil.decryptData(encrypted, key);
                assertNotNull(decrypted);
                assertEquals(TEST_PLAINTEXT, decrypted);
            });
        }
    }

    /**
     * Prueba el manejo de valores nulos en la encriptación.
     * Verifica que la utilidad de encriptación maneje adecuadamente
     * los casos donde los parámetros de entrada son nulos.
     */
    @Test
    @DisplayName("Test encryption with null inputs")
    void testEncryptionWithNullInputs() {
        try (MockedStatic<UserSession> mockedUserSession = Mockito.mockStatic(UserSession.class)) {
            UserSession mockSession = mock(UserSession.class);
            when(mockSession.getUserId()).thenReturn(TEST_USER_ID);
            mockedUserSession.when(UserSession::getInstance).thenReturn(mockSession);

            assertDoesNotThrow(() -> {
                SecretKeySpec key = EncryptionUtil.deriveAESKey(TEST_PASSWORD);
                
                assertNull(EncryptionUtil.encryptData(null, key));
                assertNull(EncryptionUtil.encryptData(TEST_PLAINTEXT, null));
                assertNull(EncryptionUtil.decryptData(null, key));
                assertNull(EncryptionUtil.decryptData("encrypted", null));
            });
        }
    }

    /**
     * Prueba la encriptación de cadenas vacías.
     * Verifica que las cadenas vacías se puedan encriptar y desencriptar correctamente,
     * manteniendo su estado vacío tras el proceso.
     */
    @Test
    @DisplayName("Test encryption with empty string")
    void testEncryptionWithEmptyString() {
        try (MockedStatic<UserSession> mockedUserSession = Mockito.mockStatic(UserSession.class)) {
            UserSession mockSession = mock(UserSession.class);
            when(mockSession.getUserId()).thenReturn(TEST_USER_ID);
            mockedUserSession.when(UserSession::getInstance).thenReturn(mockSession);

            assertDoesNotThrow(() -> {
                SecretKeySpec key = EncryptionUtil.deriveAESKey(TEST_PASSWORD);
                
                String encrypted = EncryptionUtil.encryptData("", key);
                assertNotNull(encrypted);
                
                String decrypted = EncryptionUtil.decryptData(encrypted, key);
                assertEquals("", decrypted);
            });
        }
    }

    /**
     * Prueba la consistencia de la encriptación.
     * Verifica que encriptar los mismos datos múltiples veces produzca diferentes
     * textos cifrados (debido al IV aleatorio) pero que ambos se desencripten
     * al mismo texto original.
     */
    @Test
    @DisplayName("Test encryption consistency")
    void testEncryptionConsistency() {
        try (MockedStatic<UserSession> mockedUserSession = Mockito.mockStatic(UserSession.class)) {
            UserSession mockSession = mock(UserSession.class);
            when(mockSession.getUserId()).thenReturn(TEST_USER_ID);
            mockedUserSession.when(UserSession::getInstance).thenReturn(mockSession);

            assertDoesNotThrow(() -> {
                SecretKeySpec key = EncryptionUtil.deriveAESKey(TEST_PASSWORD);
                
                // Encrypt same data multiple times - should produce different ciphertext (due to random IV)
                String encrypted1 = EncryptionUtil.encryptData(TEST_PLAINTEXT, key);
                String encrypted2 = EncryptionUtil.encryptData(TEST_PLAINTEXT, key);
                
                assertNotEquals(encrypted1, encrypted2, 
                              "Multiple encryptions should produce different ciphertext");
                
                // But both should decrypt to same plaintext
                String decrypted1 = EncryptionUtil.decryptData(encrypted1, key);
                String decrypted2 = EncryptionUtil.decryptData(encrypted2, key);
                
                assertEquals(TEST_PLAINTEXT, decrypted1);
                assertEquals(TEST_PLAINTEXT, decrypted2);
            });
        }
    }

    /**
     * Prueba la desencriptación con clave incorrecta.
     * Verifica que intentar desencriptar datos con una clave diferente
     * a la usada para encriptar lance una EncryptionException.
     */
    @Test
    @DisplayName("Test decryption with wrong key")
    void testDecryptionWithWrongKey() {
        try (MockedStatic<UserSession> mockedUserSession = Mockito.mockStatic(UserSession.class)) {
            UserSession mockSession = mock(UserSession.class);
            when(mockSession.getUserId()).thenReturn(TEST_USER_ID);
            mockedUserSession.when(UserSession::getInstance).thenReturn(mockSession);

            assertThrows(EncryptionException.class, () -> {
                SecretKeySpec key1 = EncryptionUtil.deriveAESKey(TEST_PASSWORD);
                SecretKeySpec key2 = EncryptionUtil.deriveAESKey("DifferentPassword");
                
                String encrypted = EncryptionUtil.encryptData(TEST_PLAINTEXT, key1);
                EncryptionUtil.decryptData(encrypted, key2);
            });
        }
    }

    /**
     * Prueba la desencriptación con datos inválidos.
     * Verifica que intentar desencriptar datos corruptos o mal formateados
     * lance una EncryptionException. Incluye pruebas con datos base64 inválidos
     * y datos base64 válidos pero con contenido encriptado incorrecto.
     */
    @Test
    @DisplayName("Test decryption with invalid data")
    void testDecryptionWithInvalidData() {
        try (MockedStatic<UserSession> mockedUserSession = Mockito.mockStatic(UserSession.class)) {
            UserSession mockSession = mock(UserSession.class);
            when(mockSession.getUserId()).thenReturn(TEST_USER_ID);
            mockedUserSession.when(UserSession::getInstance).thenReturn(mockSession);

            assertThrows(EncryptionException.class, () -> {
                SecretKeySpec key = EncryptionUtil.deriveAESKey(TEST_PASSWORD);
                EncryptionUtil.decryptData("invalid_base64_data", key);
            });
            
            assertThrows(EncryptionException.class, () -> {
                SecretKeySpec key = EncryptionUtil.deriveAESKey(TEST_PASSWORD);
                EncryptionUtil.decryptData("dGVzdA==", key); // Valid base64 but invalid encrypted data
            });
        }
    }

    /**
     * Prueba que diferentes IDs de usuario produzcan resultados diferentes.
     * Verifica que el salt basado en el ID de usuario funcione correctamente,
     * generando hashes diferentes para la misma contraseña cuando se usan
     * diferentes IDs de usuario. Esto asegura la separación de datos entre usuarios.
     */
    @Test
    @DisplayName("Test different user IDs produce different results")
    void testDifferentUserIdsSalt() {
        try (MockedStatic<UserSession> mockedUserSession = Mockito.mockStatic(UserSession.class)) {
            UserSession mockSession = mock(UserSession.class);
            
            // Test with first user ID
            when(mockSession.getUserId()).thenReturn("user1");
            mockedUserSession.when(UserSession::getInstance).thenReturn(mockSession);
            
            String hash1 = assertDoesNotThrow(() -> EncryptionUtil.hashMasterPassword(TEST_PASSWORD));
            
            // Test with second user ID
            when(mockSession.getUserId()).thenReturn("user2");
            
            String hash2 = assertDoesNotThrow(() -> EncryptionUtil.hashMasterPassword(TEST_PASSWORD));
            
            assertNotEquals(hash1, hash2, 
                          "Different user IDs should produce different hashes for same password");
        }
    }

    /**
     * Prueba la encriptación de datos de gran tamaño.
     * Verifica que el sistema de encriptación pueda manejar eficientemente
     * volúmenes grandes de datos (10,000 líneas de texto) sin errores
     * y manteniendo la integridad de los datos tras el ciclo completo
     * de encriptación y desencriptación.
     */
    @Test
    @DisplayName("Test large data encryption")
    void testLargeDataEncryption() {
        try (MockedStatic<UserSession> mockedUserSession = Mockito.mockStatic(UserSession.class)) {
            UserSession mockSession = mock(UserSession.class);
            when(mockSession.getUserId()).thenReturn(TEST_USER_ID);
            mockedUserSession.when(UserSession::getInstance).thenReturn(mockSession);

            StringBuilder largeData = new StringBuilder();
            for (int i = 0; i < 10000; i++) {
                largeData.append("This is line ").append(i).append(" of test data.\n");
            }
            
            assertDoesNotThrow(() -> {
                SecretKeySpec key = EncryptionUtil.deriveAESKey(TEST_PASSWORD);
                
                String encrypted = EncryptionUtil.encryptData(largeData.toString(), key);
                assertNotNull(encrypted);
                
                String decrypted = EncryptionUtil.decryptData(encrypted, key);
                assertEquals(largeData.toString(), decrypted);
            });
        }
    }
}
