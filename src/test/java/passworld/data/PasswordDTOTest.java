package passworld.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Clase de pruebas para PasswordDTO.
 * 
 * Esta clase verifica la funcionalidad del objeto de transferencia de datos (DTO)
 * que representa una contraseña en el sistema. Las pruebas cubren:
 * - Constructores con diferentes parámetros
 * - Métodos getter y setter para todos los campos
 * - Valores por defecto de los campos booleanos
 * - Manejo de valores nulos y cadenas vacías
 * - Combinaciones de flags de seguridad
 * - Escenarios de uso típicos
 * - Casos extremos (caracteres especiales, cadenas muy largas)
 */
class PasswordDTOTest {

    // Objetos de prueba
    private PasswordDTO passwordDTO;
    private LocalDateTime testDateTime;

    /**
     * Configuración inicial que se ejecuta antes de cada prueba.
     * Inicializa un PasswordDTO de prueba y una fecha/hora de referencia.
     */
    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.now();
        passwordDTO = new PasswordDTO("Test Service", "testuser", "https://test.com", "testpass123");
    }

    /**
     * Prueba el constructor que acepta todos los parámetros incluyendo fecha de modificación.
     * Verifica que todos los campos se asignen correctamente y que los valores por defecto sean apropiados.
     */
    @Test
    @DisplayName("Test constructor with all parameters")
    void testConstructorWithAllParameters() {
        PasswordDTO dto = new PasswordDTO("Gmail", "user@gmail.com", 
                                         "https://gmail.com", "password123", testDateTime);
        
        assertEquals("Gmail", dto.getDescription());
        assertEquals("user@gmail.com", dto.getUsername());
        assertEquals("https://gmail.com", dto.getUrl());
        assertEquals("password123", dto.getPassword());
        assertEquals(testDateTime, dto.getLastModified());
        assertFalse(dto.isSynced()); // Debe ser false por defecto
    }

    /**
     * Prueba el constructor sin fecha de modificación.
     * Verifica que la fecha quede como null cuando no se especifica.
     */
    @Test
    @DisplayName("Test constructor without lastModified")
    void testConstructorWithoutLastModified() {
        PasswordDTO dto = new PasswordDTO("Facebook", "user@facebook.com", 
                                         "https://facebook.com", "password456");
        
        assertEquals("Facebook", dto.getDescription());
        assertEquals("user@facebook.com", dto.getUsername());
        assertEquals("https://facebook.com", dto.getUrl());
        assertEquals("password456", dto.getPassword());
        assertNull(dto.getLastModified()); // Debe ser null si no se establece
    }

    /**
     * Prueba exhaustiva de todos los métodos getter y setter.
     * Verifica que cada campo se pueda establecer y recuperar correctamente.
     */
    @Test
    @DisplayName("Test all getters and setters")
    void testGettersAndSetters() {
        // Probar ID numérico
        passwordDTO.setId(42);
        assertEquals(42, passwordDTO.getId());

        // Probar ID de Firebase
        passwordDTO.setIdFb("fb_123456");
        assertEquals("fb_123456", passwordDTO.getIdFb());

        // Probar descripción del servicio
        passwordDTO.setDescription("Updated Service");
        assertEquals("Updated Service", passwordDTO.getDescription());

        // Probar nombre de usuario
        passwordDTO.setUsername("newuser@example.com");
        assertEquals("newuser@example.com", passwordDTO.getUsername());

        // Probar URL del servicio
        passwordDTO.setUrl("https://updated.com");
        assertEquals("https://updated.com", passwordDTO.getUrl());

        // Probar contraseña
        passwordDTO.setPassword("newpassword456");
        assertEquals("newpassword456", passwordDTO.getPassword());

        // Probar fecha de última modificación
        passwordDTO.setLastModified(testDateTime);
        assertEquals(testDateTime, passwordDTO.getLastModified());

        // Probar flags booleanos de seguridad
        passwordDTO.setWeak(true);
        assertTrue(passwordDTO.isWeak());

        passwordDTO.setDuplicate(true);
        assertTrue(passwordDTO.isDuplicate());

        passwordDTO.setCompromised(true);
        assertTrue(passwordDTO.isCompromised());

        passwordDTO.setUrlUnsafe(true);
        assertTrue(passwordDTO.isUrlUnsafe());

        passwordDTO.setSynced(true);
        assertTrue(passwordDTO.isSynced());
    }

    /**
     * Prueba que todos los valores booleanos tengan sus valores por defecto correctos.
     * Todos los flags de seguridad deben inicializarse como false.
     */
    @Test
    @DisplayName("Test default boolean values")
    void testDefaultBooleanValues() {
        // Todos los flags booleanos deben ser false por defecto
        assertFalse(passwordDTO.isWeak());
        assertFalse(passwordDTO.isDuplicate());
        assertFalse(passwordDTO.isCompromised());
        assertFalse(passwordDTO.isUrlUnsafe());
        assertFalse(passwordDTO.isSynced());
    }

    /**
     * Prueba el método toString para verificar que incluya la información relevante.
     * Útil para debugging y logging.
     */
    @Test
    @DisplayName("Test toString method")
    void testToString() {
        passwordDTO.setId(1);
        passwordDTO.setIdFb("fb_test");
        
        String result = passwordDTO.toString();
        
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("idFb='fb_test'"));
        assertTrue(result.contains("description='Test Service'"));
        assertTrue(result.contains("username='testuser'"));
        assertTrue(result.contains("url='https://test.com'"));
        assertTrue(result.contains("password='testpass123'"));
    }

    /**
     * Prueba el comportamiento del DTO cuando se pasan valores nulos.
     * Verifica que el DTO maneje correctamente los valores null sin lanzar excepciones.
     */
    @Test
    @DisplayName("Test with null values")
    void testWithNullValues() {
        // Crear DTO con todos los valores nulos
        PasswordDTO dto = new PasswordDTO(null, null, null, null);
        
        // Verificar que todos los campos sean null
        assertNull(dto.getDescription());
        assertNull(dto.getUsername());
        assertNull(dto.getUrl());
        assertNull(dto.getPassword());
        assertNull(dto.getLastModified());
    }

    /**
     * Prueba el comportamiento del DTO con cadenas vacías.
     * Verifica que el DTO acepte y mantenga cadenas vacías correctamente.
     */
    @Test
    @DisplayName("Test with empty strings")
    void testWithEmptyStrings() {
        // Crear DTO con cadenas vacías
        PasswordDTO dto = new PasswordDTO("", "", "", "");
        
        // Verificar que todas las cadenas permanezcan vacías
        assertEquals("", dto.getDescription());
        assertEquals("", dto.getUsername());
        assertEquals("", dto.getUrl());
        assertEquals("", dto.getPassword());
    }

    /**
     * Prueba las diferentes combinaciones de flags de seguridad.
     * Verifica que cada flag puede establecerse independientemente de los demás.
     */
    @Test
    @DisplayName("Test security flags combinations")
    void testSecurityFlagsCombinations() {
        // Establecer algunos flags como true y otros como false
        passwordDTO.setWeak(true);
        passwordDTO.setDuplicate(false);
        passwordDTO.setCompromised(true);
        passwordDTO.setUrlUnsafe(false);
        
        // Verificar que cada flag mantiene su valor independiente
        assertTrue(passwordDTO.isWeak());
        assertFalse(passwordDTO.isDuplicate());
        assertTrue(passwordDTO.isCompromised());
        assertFalse(passwordDTO.isUrlUnsafe());
    }

    /**
     * Prueba el manejo de diferentes fechas en el campo lastModified.
     * Verifica que se puedan establecer fechas del pasado, futuro y valores null.
     */
    @Test
    @DisplayName("Test lastModified with various dates")
    void testLastModifiedWithVariousDates() {
        // Crear fechas de prueba: una del pasado y una del futuro
        LocalDateTime past = LocalDateTime.of(2020, 1, 1, 12, 0);
        LocalDateTime future = LocalDateTime.of(2030, 12, 31, 23, 59);
        
        // Probar fecha del pasado
        passwordDTO.setLastModified(past);
        assertEquals(past, passwordDTO.getLastModified());
        
        // Probar fecha del futuro
        passwordDTO.setLastModified(future);
        assertEquals(future, passwordDTO.getLastModified());
        
        // Probar establecer como null
        passwordDTO.setLastModified(null);
        assertNull(passwordDTO.getLastModified());
    }

    /**
     * Prueba escenarios típicos de uso del DTO en la aplicación.
     * Simula los flujos más comunes: crear nueva entrada, actualizar existente y marcar problemas de seguridad.
     */
    @Test
    @DisplayName("Test typical use case scenarios")
    void testTypicalUseCaseScenarios() {
        // Escenario 1: Crear una nueva entrada de contraseña
        PasswordDTO newEntry = new PasswordDTO("GitHub", "developer@example.com", 
                                              "https://github.com", "securePass123!");
        newEntry.setLastModified(LocalDateTime.now());
        
        // Verificar que todos los campos estén correctamente establecidos
        assertNotNull(newEntry.getDescription());
        assertNotNull(newEntry.getUsername());
        assertNotNull(newEntry.getUrl());
        assertNotNull(newEntry.getPassword());
        assertNotNull(newEntry.getLastModified());
        assertFalse(newEntry.isSynced()); // Nueva entrada no está sincronizada

        // Escenario 2: Actualizar una entrada existente
        newEntry.setId(100);
        newEntry.setPassword("newSecurePass456!");
        newEntry.setLastModified(LocalDateTime.now());
        newEntry.setSynced(false); // Marcar como pendiente de sincronización
        
        // Verificar que los cambios se aplicaron correctamente
        assertEquals(100, newEntry.getId());
        assertEquals("newSecurePass456!", newEntry.getPassword());
        assertFalse(newEntry.isSynced());

        // Escenario 3: Marcar entrada con problemas de seguridad
        newEntry.setWeak(true);
        newEntry.setDuplicate(true);
        
        // Verificar que los flags de seguridad se establecieron correctamente
        assertTrue(newEntry.isWeak());
        assertTrue(newEntry.isDuplicate());
        assertFalse(newEntry.isCompromised()); // Este debe permanecer false
    }

    /**
     * Prueba el manejo de caracteres especiales en todos los campos del DTO.
     * Verifica que el DTO pueda almacenar correctamente caracteres Unicode y símbolos especiales.
     */
    @Test
    @DisplayName("Test special characters in fields")
    void testSpecialCharactersInFields() {
        // Cadena con caracteres especiales, Unicode y símbolos
        String specialChars = "特殊字符!@#$%^&*()_+-=[]{}|;':\",./<>?";
        
        // Crear DTO con caracteres especiales en los campos principales
        PasswordDTO dto = new PasswordDTO(specialChars, specialChars, 
                                         "https://test.com", specialChars);
        
        // Verificar que todos los caracteres especiales se conserven correctamente
        assertEquals(specialChars, dto.getDescription());
        assertEquals(specialChars, dto.getUsername());
        assertEquals(specialChars, dto.getPassword());
    }

    /**
     * Prueba el comportamiento del DTO con cadenas muy largas.
     * Verifica que el DTO pueda manejar cadenas de gran tamaño sin problemas de rendimiento o memoria.
     */
    @Test
    @DisplayName("Test very long strings")
    void testVeryLongStrings() {
        // Crear una cadena muy larga (1000 caracteres)
        StringBuilder longString = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longString.append("a");
        }
        
        String longText = longString.toString();
        
        // Crear DTO con cadenas muy largas en todos los campos
        PasswordDTO dto = new PasswordDTO(longText, longText, longText, longText);
        
        // Verificar que las cadenas largas se almacenen correctamente
        assertEquals(longText, dto.getDescription());
        assertEquals(longText, dto.getUsername());
        assertEquals(longText, dto.getUrl());
        assertEquals(longText, dto.getPassword());
        assertEquals(1000, dto.getDescription().length());
    }
}
