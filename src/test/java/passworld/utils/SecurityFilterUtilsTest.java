package passworld.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import passworld.data.PasswordDTO;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Clase de pruebas para SecurityFilterUtils.
 * 
 * Esta clase verifica la funcionalidad del sistema de análisis de seguridad
 * de contraseñas, que identifica múltiples tipos de vulnerabilidades:
 * - Contraseñas débiles (fortaleza < 3)
 * - Contraseñas duplicadas entre diferentes servicios
 * - Contraseñas comprometidas en filtraciones conocidas
 * - URLs inseguras (HTTP en lugar de HTTPS)
 * 
 * Las pruebas utilizan Mockito para simular las dependencias externas:
 * - PasswordEvaluator para evaluación de fortaleza
 * - CompromisedPasswordChecker para verificación de filtraciones
 * - UnsafeUrlChecker para validación de URLs
 * 
 * Se incluyen pruebas de rendimiento, manejo de errores, casos límite,
 * y validación de toda la funcionalidad de filtrado de seguridad.
 */
@ExtendWith(MockitoExtension.class)
class SecurityFilterUtilsTest {

    // Objetos de prueba para diferentes escenarios de seguridad
    private PasswordDTO weakPassword;
    private PasswordDTO strongPassword;
    private PasswordDTO duplicatePassword1;
    private PasswordDTO duplicatePassword2;
    private PasswordDTO compromisedPassword;
    private PasswordDTO unsafeUrlPassword;

    /**
     * Configuración inicial que se ejecuta antes de cada prueba.
     * Crea objetos PasswordDTO para diferentes escenarios de seguridad.
     */
    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        
        weakPassword = new PasswordDTO("Weak Service", "user1", 
                                      "https://weak.com", "123", now);
        
        strongPassword = new PasswordDTO("Strong Service", "user2", 
                                        "https://strong.com", "VeryStr0ng!P@ssw0rd2024", now);
        
        duplicatePassword1 = new PasswordDTO("Service 1", "user3", 
                                            "https://service1.com", "duplicatepass", now);
        
        duplicatePassword2 = new PasswordDTO("Service 2", "user4", 
                                            "https://service2.com", "duplicatepass", now);
        
        compromisedPassword = new PasswordDTO("Compromised Service", "user5", 
                                             "https://compromised.com", "compromisedpass", now);
        
        unsafeUrlPassword = new PasswordDTO("Unsafe URL Service", "user6", 
                                           "http://unsafe.com", "securepass123", now);
    }

    /**
     * Prueba el análisis de una lista con múltiples niveles de seguridad.
     * Verifica que el sistema identifique correctamente:
     * - Contraseñas débiles (fortaleza < 3)
     * - Contraseñas duplicadas (solo las repeticiones, no la primera ocurrencia)
     * - Contraseñas comprometidas en bases de datos de filtraciones
     * - URLs inseguras (HTTP vs HTTPS)
     * 
     * Utiliza mocks para simular respuestas consistentes de los servicios externos.
     */
    @Test
    @DisplayName("Test analyzePasswordList with mixed security levels")
    void testAnalyzePasswordListMixed() {
        try (MockedStatic<PasswordEvaluator> mockedEvaluator = mockStatic(PasswordEvaluator.class);
             MockedStatic<CompromisedPasswordChecker> mockedChecker = mockStatic(CompromisedPasswordChecker.class);
             MockedStatic<UnsafeUrlChecker> mockedUrlChecker = mockStatic(UnsafeUrlChecker.class)) {
            
            // Mock password strength evaluations
            mockedEvaluator.when(() -> PasswordEvaluator.calculateStrength("123")).thenReturn(0);
            mockedEvaluator.when(() -> PasswordEvaluator.calculateStrength("VeryStr0ng!P@ssw0rd2024")).thenReturn(4);
            mockedEvaluator.when(() -> PasswordEvaluator.calculateStrength("duplicatepass")).thenReturn(2);
            mockedEvaluator.when(() -> PasswordEvaluator.calculateStrength("compromisedpass")).thenReturn(2);
            mockedEvaluator.when(() -> PasswordEvaluator.calculateStrength("securepass123")).thenReturn(3);
            
            // Mock compromised password check
            mockedChecker.when(() -> CompromisedPasswordChecker.isCompromisedPassword("123")).thenReturn(false);
            mockedChecker.when(() -> CompromisedPasswordChecker.isCompromisedPassword("VeryStr0ng!P@ssw0rd2024")).thenReturn(false);
            mockedChecker.when(() -> CompromisedPasswordChecker.isCompromisedPassword("duplicatepass")).thenReturn(false);
            mockedChecker.when(() -> CompromisedPasswordChecker.isCompromisedPassword("compromisedpass")).thenReturn(true);
            mockedChecker.when(() -> CompromisedPasswordChecker.isCompromisedPassword("securepass123")).thenReturn(false);
            
            // Mock URL safety check
            mockedUrlChecker.when(() -> UnsafeUrlChecker.isUnsafe("https://weak.com")).thenReturn(false);
            mockedUrlChecker.when(() -> UnsafeUrlChecker.isUnsafe("https://strong.com")).thenReturn(false);
            mockedUrlChecker.when(() -> UnsafeUrlChecker.isUnsafe("https://service1.com")).thenReturn(false);
            mockedUrlChecker.when(() -> UnsafeUrlChecker.isUnsafe("https://service2.com")).thenReturn(false);
            mockedUrlChecker.when(() -> UnsafeUrlChecker.isUnsafe("https://compromised.com")).thenReturn(false);
            mockedUrlChecker.when(() -> UnsafeUrlChecker.isUnsafe("http://unsafe.com")).thenReturn(true);

            List<PasswordDTO> passwords = Arrays.asList(
                weakPassword, strongPassword, duplicatePassword1, 
                duplicatePassword2, compromisedPassword, unsafeUrlPassword
            );

            SecurityFilterUtils.analyzePasswordList(passwords);

            // Verify weak password detection
            assertTrue(weakPassword.isWeak());
            assertFalse(weakPassword.isDuplicate());
            assertFalse(weakPassword.isCompromised());
            assertFalse(weakPassword.isUrlUnsafe());

            // Verify strong password has no issues
            assertFalse(strongPassword.isWeak());
            assertFalse(strongPassword.isDuplicate());
            assertFalse(strongPassword.isCompromised());
            assertFalse(strongPassword.isUrlUnsafe());

            // Verify duplicate detection
            assertFalse(duplicatePassword1.isDuplicate()); // First occurrence
            assertTrue(duplicatePassword2.isDuplicate());  // Second occurrence (duplicate)

            // Verify compromised password detection
            assertTrue(compromisedPassword.isCompromised());

            // Verify unsafe URL detection
            assertTrue(unsafeUrlPassword.isUrlUnsafe());
        }
    }

    /**
     * Prueba el análisis con una lista vacía.
     * Verifica que el sistema maneje adecuadamente el caso extremo
     * de una lista sin contraseñas sin lanzar excepciones.
     */
    @Test
    @DisplayName("Test analyzePasswordList with empty list")
    void testAnalyzePasswordListEmpty() {
        assertDoesNotThrow(() -> {
            SecurityFilterUtils.analyzePasswordList(Collections.emptyList());
        });
    }

    /**
     * Prueba el análisis con una sola contraseña.
     * Verifica que el sistema funcione correctamente cuando solo hay
     * una contraseña en la lista, asegurando que no se marque como
     * duplicada (ya que no hay otras para comparar).
     */
    @Test
    @DisplayName("Test analyzePasswordList with single password")
    void testAnalyzePasswordListSingle() {
        try (MockedStatic<PasswordEvaluator> mockedEvaluator = mockStatic(PasswordEvaluator.class);
             MockedStatic<CompromisedPasswordChecker> mockedChecker = mockStatic(CompromisedPasswordChecker.class);
             MockedStatic<UnsafeUrlChecker> mockedUrlChecker = mockStatic(UnsafeUrlChecker.class)) {
            
            mockedEvaluator.when(() -> PasswordEvaluator.calculateStrength("VeryStr0ng!P@ssw0rd2024")).thenReturn(4);
            mockedChecker.when(() -> CompromisedPasswordChecker.isCompromisedPassword("VeryStr0ng!P@ssw0rd2024")).thenReturn(false);
            mockedUrlChecker.when(() -> UnsafeUrlChecker.isUnsafe("https://strong.com")).thenReturn(false);

            SecurityFilterUtils.analyzePasswordList(Arrays.asList(strongPassword));

            assertFalse(strongPassword.isWeak());
            assertFalse(strongPassword.isDuplicate());
            assertFalse(strongPassword.isCompromised());
            assertFalse(strongPassword.isUrlUnsafe());
        }
    }

    /**
     * Prueba la detección de contraseñas duplicadas.
     * Verifica que cuando múltiples contraseñas son idénticas:
     * - La primera ocurrencia NO se marca como duplicada
     * - Las siguientes ocurrencias SÍ se marcan como duplicadas
     * - El algoritmo funciona correctamente con 3+ duplicados
     */
    @Test
    @DisplayName("Test duplicate detection with identical passwords")
    void testDuplicateDetection() {
        try (MockedStatic<PasswordEvaluator> mockedEvaluator = mockStatic(PasswordEvaluator.class);
             MockedStatic<CompromisedPasswordChecker> mockedChecker = mockStatic(CompromisedPasswordChecker.class);
             MockedStatic<UnsafeUrlChecker> mockedUrlChecker = mockStatic(UnsafeUrlChecker.class)) {
            
            mockedEvaluator.when(() -> PasswordEvaluator.calculateStrength("samepassword")).thenReturn(3);
            mockedChecker.when(() -> CompromisedPasswordChecker.isCompromisedPassword("samepassword")).thenReturn(false);
            mockedUrlChecker.when(() -> UnsafeUrlChecker.isUnsafe(anyString())).thenReturn(false);

            PasswordDTO password1 = new PasswordDTO("Service 1", "user1", "https://site1.com", "samepassword");
            PasswordDTO password2 = new PasswordDTO("Service 2", "user2", "https://site2.com", "samepassword");
            PasswordDTO password3 = new PasswordDTO("Service 3", "user3", "https://site3.com", "samepassword");

            SecurityFilterUtils.analyzePasswordList(Arrays.asList(password1, password2, password3));

            assertFalse(password1.isDuplicate()); // First occurrence
            assertTrue(password2.isDuplicate());  // Second occurrence
            assertTrue(password3.isDuplicate());  // Third occurrence
        }
    }

    /**
     * Prueba la función hasPasswordSecurityIssues con varios escenarios.
     * Verifica que la función detecte correctamente si una contraseña
     * tiene algún problema de seguridad (débil, duplicada, comprometida,
     * o URL insegura), tanto para casos individuales como combinados.
     */
    @Test
    @DisplayName("Test hasPasswordSecurityIssues with various scenarios")
    void testHasPasswordSecurityIssues() {
        // Password with no issues
        PasswordDTO cleanPassword = new PasswordDTO("Clean", "user", "https://clean.com", "cleanpass");
        cleanPassword.setWeak(false);
        cleanPassword.setDuplicate(false);
        cleanPassword.setCompromised(false);
        cleanPassword.setUrlUnsafe(false);
        assertFalse(SecurityFilterUtils.hasPasswordSecurityIssues(cleanPassword));

        // Password with weak issue
        cleanPassword.setWeak(true);
        assertTrue(SecurityFilterUtils.hasPasswordSecurityIssues(cleanPassword));
        cleanPassword.setWeak(false);

        // Password with duplicate issue
        cleanPassword.setDuplicate(true);
        assertTrue(SecurityFilterUtils.hasPasswordSecurityIssues(cleanPassword));
        cleanPassword.setDuplicate(false);

        // Password with compromised issue
        cleanPassword.setCompromised(true);
        assertTrue(SecurityFilterUtils.hasPasswordSecurityIssues(cleanPassword));
        cleanPassword.setCompromised(false);

        // Password with unsafe URL issue
        cleanPassword.setUrlUnsafe(true);
        assertTrue(SecurityFilterUtils.hasPasswordSecurityIssues(cleanPassword));

        // Password with multiple issues
        cleanPassword.setWeak(true);
        cleanPassword.setDuplicate(true);
        assertTrue(SecurityFilterUtils.hasPasswordSecurityIssues(cleanPassword));
    }

    /**
     * Prueba el método isUrlUnsafe directamente.
     * Verifica que la función delegue correctamente al UnsafeUrlChecker
     * y devuelva los resultados apropiados para URLs seguras e inseguras.
     * También valida que se realicen las llamadas esperadas al servicio externo.
     */
    @Test
    @DisplayName("Test isUrlUnsafe method directly")
    void testIsUrlUnsafeDirectly() {
        try (MockedStatic<UnsafeUrlChecker> mockedUrlChecker = mockStatic(UnsafeUrlChecker.class)) {
            mockedUrlChecker.when(() -> UnsafeUrlChecker.isUnsafe("http://unsafe.com")).thenReturn(true);
            mockedUrlChecker.when(() -> UnsafeUrlChecker.isUnsafe("https://safe.com")).thenReturn(false);

            assertTrue(SecurityFilterUtils.isUrlUnsafe("http://unsafe.com"));
            assertFalse(SecurityFilterUtils.isUrlUnsafe("https://safe.com"));

            mockedUrlChecker.verify(() -> UnsafeUrlChecker.isUnsafe("http://unsafe.com"));
            mockedUrlChecker.verify(() -> UnsafeUrlChecker.isUnsafe("https://safe.com"));
        }
    }

    /**
     * Prueba el manejo de excepciones en la verificación de contraseñas comprometidas.
     * Verifica que cuando el servicio de verificación de contraseñas comprometidas
     * falla (por ejemplo, error de red), el sistema:
     * - No lance excepciones hacia arriba
     * - Continúe con el análisis de otras características
     * - Marque la contraseña como NO comprometida por defecto (fail-safe)
     */
    @Test
    @DisplayName("Test exception handling in compromised password check")
    void testCompromisedPasswordCheckException() {
        try (MockedStatic<PasswordEvaluator> mockedEvaluator = mockStatic(PasswordEvaluator.class);
             MockedStatic<CompromisedPasswordChecker> mockedChecker = mockStatic(CompromisedPasswordChecker.class);
             MockedStatic<UnsafeUrlChecker> mockedUrlChecker = mockStatic(UnsafeUrlChecker.class)) {
            
            mockedEvaluator.when(() -> PasswordEvaluator.calculateStrength("testpass")).thenReturn(3);
            mockedChecker.when(() -> CompromisedPasswordChecker.isCompromisedPassword("testpass"))
                        .thenThrow(new RuntimeException("Network error"));
            mockedUrlChecker.when(() -> UnsafeUrlChecker.isUnsafe("https://test.com")).thenReturn(false);

            PasswordDTO testPassword = new PasswordDTO("Test", "user", "https://test.com", "testpass");

            assertDoesNotThrow(() -> {
                SecurityFilterUtils.analyzePasswordList(Arrays.asList(testPassword));
            });

            // Should default to false when exception occurs
            assertFalse(testPassword.isCompromised());
        }
    }

    /**
     * Prueba el comportamiento con valores nulos en contraseñas.
     * Verifica que el sistema maneje adecuadamente contraseñas con valores
     * nulos (password=null, url=null) sin lanzar excepciones y que
     * las contraseñas nulas se consideren automáticamente como débiles.
     */
    @Test
    @DisplayName("Test with null password values")
    void testWithNullPasswordValues() {
        try (MockedStatic<PasswordEvaluator> mockedEvaluator = mockStatic(PasswordEvaluator.class);
             MockedStatic<CompromisedPasswordChecker> mockedChecker = mockStatic(CompromisedPasswordChecker.class);
             MockedStatic<UnsafeUrlChecker> mockedUrlChecker = mockStatic(UnsafeUrlChecker.class)) {
            
            mockedEvaluator.when(() -> PasswordEvaluator.calculateStrength(null)).thenReturn(0);
            mockedChecker.when(() -> CompromisedPasswordChecker.isCompromisedPassword(null)).thenReturn(false);
            mockedUrlChecker.when(() -> UnsafeUrlChecker.isUnsafe(null)).thenReturn(false);

            PasswordDTO nullPassword = new PasswordDTO("Test", "user", null, null);

            assertDoesNotThrow(() -> {
                SecurityFilterUtils.analyzePasswordList(Arrays.asList(nullPassword));
            });

            // Verify that null password is considered weak
            assertTrue(nullPassword.isWeak());
        }
    }

    /**
     * Prueba los límites de fortaleza de contraseñas.
     * Verifica que el umbral de fortaleza (< 3 = débil, >= 3 = fuerte)
     * se aplique correctamente en todos los casos límite:
     * - Fortaleza 0 y 2: débiles
     * - Fortaleza 3 y 4: fuertes
     */
    @Test
    @DisplayName("Test password strength boundaries")
    void testPasswordStrengthBoundaries() {
        try (MockedStatic<PasswordEvaluator> mockedEvaluator = mockStatic(PasswordEvaluator.class);
             MockedStatic<CompromisedPasswordChecker> mockedChecker = mockStatic(CompromisedPasswordChecker.class);
             MockedStatic<UnsafeUrlChecker> mockedUrlChecker = mockStatic(UnsafeUrlChecker.class)) {
            
            mockedChecker.when(() -> CompromisedPasswordChecker.isCompromisedPassword(anyString())).thenReturn(false);
            mockedUrlChecker.when(() -> UnsafeUrlChecker.isUnsafe(anyString())).thenReturn(false);

            // Test strength boundary (< 3 is weak)
            PasswordDTO strength0 = new PasswordDTO("Test0", "user", "https://test.com", "weak0");
            PasswordDTO strength2 = new PasswordDTO("Test2", "user", "https://test.com", "weak2");
            PasswordDTO strength3 = new PasswordDTO("Test3", "user", "https://test.com", "strong3");
            PasswordDTO strength4 = new PasswordDTO("Test4", "user", "https://test.com", "strong4");

            mockedEvaluator.when(() -> PasswordEvaluator.calculateStrength("weak0")).thenReturn(0);
            mockedEvaluator.when(() -> PasswordEvaluator.calculateStrength("weak2")).thenReturn(2);
            mockedEvaluator.when(() -> PasswordEvaluator.calculateStrength("strong3")).thenReturn(3);
            mockedEvaluator.when(() -> PasswordEvaluator.calculateStrength("strong4")).thenReturn(4);

            SecurityFilterUtils.analyzePasswordList(Arrays.asList(strength0, strength2, strength3, strength4));

            assertTrue(strength0.isWeak());  // 0 < 3
            assertTrue(strength2.isWeak());  // 2 < 3
            assertFalse(strength3.isWeak()); // 3 >= 3
            assertFalse(strength4.isWeak()); // 4 >= 3
        }
    }

    /**
     * Prueba el rendimiento con listas grandes de contraseñas.
     * Verifica que el sistema pueda procesar eficientemente grandes
     * volúmenes de datos (1000 contraseñas) en un tiempo razonable
     * (menos de 5 segundos), asegurando escalabilidad para usuarios
     * con muchas contraseñas almacenadas.
     */
    @Test
    @DisplayName("Test large password list performance")
    void testLargePasswordListPerformance() {
        try (MockedStatic<PasswordEvaluator> mockedEvaluator = mockStatic(PasswordEvaluator.class);
             MockedStatic<CompromisedPasswordChecker> mockedChecker = mockStatic(CompromisedPasswordChecker.class);
             MockedStatic<UnsafeUrlChecker> mockedUrlChecker = mockStatic(UnsafeUrlChecker.class)) {
            
            mockedEvaluator.when(() -> PasswordEvaluator.calculateStrength(anyString())).thenReturn(3);
            mockedChecker.when(() -> CompromisedPasswordChecker.isCompromisedPassword(anyString())).thenReturn(false);
            mockedUrlChecker.when(() -> UnsafeUrlChecker.isUnsafe(anyString())).thenReturn(false);

            // Create a large list of passwords
            List<PasswordDTO> largeList = Arrays.asList(new PasswordDTO[1000]);
            for (int i = 0; i < 1000; i++) {
                largeList.set(i, new PasswordDTO("Service" + i, "user" + i, "https://site" + i + ".com", "pass" + i));
            }

            long startTime = System.currentTimeMillis();
            assertDoesNotThrow(() -> {
                SecurityFilterUtils.analyzePasswordList(largeList);
            });
            long endTime = System.currentTimeMillis();

            // Verify it completes in reasonable time (less than 5 seconds)
            assertTrue(endTime - startTime < 5000, "Analysis should complete in reasonable time");
        }
    }
}
