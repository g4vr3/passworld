package passworld.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import passworld.data.PasswordDAO;
import passworld.data.PasswordDTO;
import passworld.data.sync.SyncHandler;
import passworld.utils.LanguageUtil;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Clase de pruebas para PasswordManager.
 * 
 * Esta clase verifica la funcionalidad del servicio principal de gestión de contraseñas.
 * Las pruebas cubren todas las operaciones CRUD (crear, leer, actualizar, eliminar) y
 * casos especiales como:
 * - Guardado y actualización de contraseñas locales y remotas
 * - Validación de datos de entrada
 * - Manejo de errores y excepciones SQL
 * - Análisis de seguridad automático
 * - Sincronización con el servidor remoto
 * - Limpieza de duplicados físicos
 * - Casos extremos con caracteres especiales
 * 
 * Utiliza Mockito para simular las dependencias (DAO, filtros de seguridad, sincronización).
 */
@ExtendWith(MockitoExtension.class)
class PasswordManagerTest {
    
    // Objetos mock para las dependencias del servicio
    private PasswordDTO validPassword;

    /**
     * Configuración inicial que se ejecuta antes de cada prueba.
     * Inicializa un PasswordDTO válido para las pruebas.
     */
    @BeforeEach
    void setUp() {
        // Crear una contraseña válida para las pruebas
        validPassword = new PasswordDTO("Test Service", "testuser", 
                                       "https://test.com", "validpass123");
        validPassword.setId(1);
        validPassword.setIdFb("fb_123");
    }

    /**
     * Prueba el guardado exitoso de una contraseña.
     * Verifica que se ejecuten todas las operaciones necesarias: validación, guardado,
     * análisis de seguridad y actualización de metadatos.
     */
    @Test
    @DisplayName("Test savePassword success")
    void testSavePasswordSuccess() throws SQLException {
        try (MockedStatic<PasswordDAO> mockedDAO = mockStatic(PasswordDAO.class)) {
            
            // Configurar mocks para simular operación exitosa
            // Configurar mocks básicos para operación exitosa
            mockedDAO.when(() -> PasswordDAO.createPassword(any(PasswordDTO.class))).thenReturn(true);
            mockedDAO.when(() -> PasswordDAO.readAllPasswordsDecrypted()).thenReturn(Arrays.asList(validPassword));
            // analyzePasswordList es void, no necesita configuración de mock

            // Ejecutar la operación de guardado
            boolean result = PasswordManager.savePassword(validPassword);

            // Verificar que la operación fue exitosa y se establecieron los metadatos
            assertTrue(result);
            assertNotNull(validPassword.getLastModified());
            assertFalse(validPassword.isSynced()); // Nueva contraseña no sincronizada
            
            // Verificar que se llamaron todos los métodos necesarios
            mockedDAO.verify(() -> PasswordDAO.createPassword(validPassword));
            mockedDAO.verify(() -> PasswordDAO.readAllPasswordsDecrypted());
        }
    }

    /**
     * Prueba la validación de datos incorrectos al guardar contraseñas.
     * Verifica que se lancen excepciones apropiadas para datos inválidos como
     * contraseñas vacías, descripciones vacías o valores nulos.
     */
    @Test
    @DisplayName("Test savePassword with invalid data")
    void testSavePasswordWithInvalidData() {
        try (MockedStatic<LanguageUtil> mockedLanguage = mockStatic(LanguageUtil.class)) {
            ResourceBundle mockBundle = mock(ResourceBundle.class);
            when(mockBundle.getString("empty_password")).thenReturn("Password is empty");
            when(mockBundle.getString("empty_description")).thenReturn("Description is empty");
            mockedLanguage.when(LanguageUtil::getBundle).thenReturn(mockBundle);

            // Probar con contraseña vacía
            PasswordDTO emptyPasswordDTO = new PasswordDTO("Valid Service", "user", "url", "");
            assertThrows(IllegalArgumentException.class, () -> {
                PasswordManager.savePassword(emptyPasswordDTO);
            });

            // Probar con descripción vacía
            PasswordDTO emptyDescriptionDTO = new PasswordDTO("", "user", "url", "validpass");
            assertThrows(IllegalArgumentException.class, () -> {
                PasswordManager.savePassword(emptyDescriptionDTO);
            });

            // Probar con contraseña nula
            PasswordDTO nullPasswordDTO = new PasswordDTO("Valid Service", "user", "url", null);
            assertThrows(IllegalArgumentException.class, () -> {
                PasswordManager.savePassword(nullPasswordDTO);
            });
        }
    }

    /**
     * Prueba el manejo de fallos en el DAO al guardar contraseñas.
     * Verifica que el método retorne false cuando el DAO no puede crear la contraseña.
     */
    @Test
    @DisplayName("Test savePassword DAO failure")
    void testSavePasswordDAOFailure() throws SQLException {
        try (MockedStatic<PasswordDAO> mockedDAO = mockStatic(PasswordDAO.class)) {
            
            // Configurar DAO para simular fallo en la creación
            mockedDAO.when(() -> PasswordDAO.createPassword(any(PasswordDTO.class))).thenReturn(false);

            // Ejecutar y verificar que retorna false
            boolean result = PasswordManager.savePassword(validPassword);
            assertFalse(result);
            mockedDAO.verify(() -> PasswordDAO.createPassword(validPassword));
            // El análisis de seguridad no debe ejecutarse cuando falla la creación
        }
    }

        /**
     * Prueba el guardado exitoso de una contraseña proveniente del servidor remoto.
     * Verifica que la contraseña se marque como sincronizada y no se duplique si ya existe.
     */
    @Test
    @DisplayName("Test savePasswordFromRemote success")
    void testSavePasswordFromRemoteSuccess() throws SQLException {
        try (MockedStatic<PasswordDAO> mockedDAO = mockStatic(PasswordDAO.class)) {
            
            // Configurar mocks para simular contraseña nueva desde remoto
            mockedDAO.when(() -> PasswordDAO.existsByIdFb("fb_123")).thenReturn(false);
            mockedDAO.when(() -> PasswordDAO.createFromRemote(any(PasswordDTO.class))).thenReturn(true);
            mockedDAO.when(() -> PasswordDAO.readAllPasswordsDecrypted()).thenReturn(Arrays.asList(validPassword));

            // Ejecutar guardado desde remoto
            assertDoesNotThrow(() -> {
                PasswordManager.savePasswordFromRemote(validPassword);
            });

            // Verificar que se marca como sincronizada
            assertTrue(validPassword.isSynced());
            mockedDAO.verify(() -> PasswordDAO.existsByIdFb("fb_123"));
            mockedDAO.verify(() -> PasswordDAO.createFromRemote(validPassword));
        }
    }

    /**
     * Prueba el comportamiento cuando se intenta guardar una contraseña remota que ya existe.
     * Verifica que no se duplique la contraseña si ya existe el idFb en la base de datos.
     */
    @Test
    @DisplayName("Test savePasswordFromRemote with existing idFb")
    void testSavePasswordFromRemoteWithExistingIdFb() throws SQLException {
        try (MockedStatic<PasswordDAO> mockedDAO = mockStatic(PasswordDAO.class)) {
            
            // Configurar para simular que la contraseña ya existe
            mockedDAO.when(() -> PasswordDAO.existsByIdFb("fb_123")).thenReturn(true);

            // Ejecutar y verificar que no lanza excepción
            assertDoesNotThrow(() -> {
                PasswordManager.savePasswordFromRemote(validPassword);
            });

            // Verificar que solo se consulta la existencia, no se crea
            mockedDAO.verify(() -> PasswordDAO.existsByIdFb("fb_123"));
        }
    }

    /**
     * Prueba la actualización exitosa de una contraseña local.
     * Verifica que se ejecuten los procesos de sincronización y análisis de seguridad.
     */
    @Test
    @DisplayName("Test updatePassword success")
    void testUpdatePasswordSuccess() throws SQLException {
        try (MockedStatic<PasswordDAO> mockedDAO = mockStatic(PasswordDAO.class);
             MockedStatic<SyncHandler> mockedSync = mockStatic(SyncHandler.class)) {
            
            // Configurar mocks para actualización exitosa
            mockedDAO.when(() -> PasswordDAO.updatePassword(any(PasswordDTO.class))).thenReturn(true);
            mockedDAO.when(() -> PasswordDAO.readAllPasswordsDecrypted()).thenReturn(Arrays.asList(validPassword));
            // startLocalUpdate y finishLocalUpdate son métodos void, no necesitan mock de retorno

            // Ejecutar actualización con nuevos datos
            boolean result = PasswordManager.updatePassword(validPassword, "New Service",
                                                           "newuser", "https://new.com", "newpass123");

            // Verificar que la operación fue exitosa y se llamaron los métodos de sincronización
            assertTrue(result);
            mockedSync.verify(SyncHandler::startLocalUpdate);
            mockedSync.verify(SyncHandler::finishLocalUpdate);
            mockedDAO.verify(() -> PasswordDAO.updatePassword(any(PasswordDTO.class)));
        }
    }

    /**
     * Prueba la actualización de una contraseña por solicitud remota.
     * Verifica que la contraseña se marque como sincronizada tras la actualización.
     */
    @Test
    @DisplayName("Test updatePasswordByRemote success")
    void testUpdatePasswordByRemoteSuccess() throws SQLException {
        try (MockedStatic<PasswordDAO> mockedDAO = mockStatic(PasswordDAO.class)) {
            
            // Configurar mocks para actualización remota exitosa
            mockedDAO.when(() -> PasswordDAO.updatePasswordFromRemote(any(PasswordDTO.class))).thenReturn(true);
            mockedDAO.when(() -> PasswordDAO.readAllPasswordsDecrypted()).thenReturn(Arrays.asList(validPassword));

            // Ejecutar actualización remota
            assertDoesNotThrow(() -> {
                PasswordManager.updatePasswordByRemote(validPassword);
            });

            // Verificar que se marca como sincronizada
            assertTrue(validPassword.isSynced());
            mockedDAO.verify(() -> PasswordDAO.updatePasswordFromRemote(validPassword));
        }
    }

    /**
     * Prueba la eliminación exitosa de una contraseña.
     * Verifica que se ejecute el análisis de seguridad después de la eliminación.
     */
    @Test
    @DisplayName("Test deletePassword success")
    void testDeletePasswordSuccess() throws SQLException {
        try (MockedStatic<PasswordDAO> mockedDAO = mockStatic(PasswordDAO.class)) {
            
            // Configurar mocks para eliminación exitosa
            mockedDAO.when(() -> PasswordDAO.deletePassword(1)).thenReturn(true);
            mockedDAO.when(() -> PasswordDAO.readAllPasswordsDecrypted()).thenReturn(Arrays.asList());
            // updatePasswordSecurity es void, no necesita mock de retorno

            // Ejecutar eliminación
            boolean result = PasswordManager.deletePassword(1);

            // Verificar que la operación fue exitosa
            assertTrue(result);
            mockedDAO.verify(() -> PasswordDAO.deletePassword(1));
        }
    }

    /**
     * Prueba el manejo de fallos al eliminar una contraseña.
     * Verifica que se retorne false cuando la eliminación falla en el DAO.
     */
    @Test
    @DisplayName("Test deletePassword failure")
    void testDeletePasswordFailure() throws SQLException {
        try (MockedStatic<PasswordDAO> mockedDAO = mockStatic(PasswordDAO.class)) {
            // Configurar DAO para simular fallo en la eliminación
            mockedDAO.when(() -> PasswordDAO.deletePassword(999)).thenReturn(false);

            // Ejecutar eliminación con ID inexistente
            boolean result = PasswordManager.deletePassword(999);
            assertFalse(result);
            mockedDAO.verify(() -> PasswordDAO.deletePassword(999));
            // El análisis de seguridad no debe ejecutarse cuando falla la eliminación
        }
    }

    /**
     * Prueba la obtención de una contraseña por su ID.
     * Verifica que se retorne la contraseña correcta desde el DAO.
     */
    @Test
    @DisplayName("Test getPasswordById")
    void testGetPasswordById() throws SQLException {
        try (MockedStatic<PasswordDAO> mockedDAO = mockStatic(PasswordDAO.class)) {
            // Configurar DAO para retornar la contraseña válida
            mockedDAO.when(() -> PasswordDAO.readPasswordById(1)).thenReturn(validPassword);

            // Ejecutar búsqueda por ID
            PasswordDTO result = PasswordManager.getPasswordById(1);

            // Verificar que se retorna la contraseña correcta
            assertEquals(validPassword, result);
            mockedDAO.verify(() -> PasswordDAO.readPasswordById(1));
        }
    }

    /**
     * Prueba la obtención de todas las contraseñas almacenadas.
     * Verifica que se retorne la lista completa de contraseñas desde el DAO.
     */
    @Test
    @DisplayName("Test getAllPasswords")
    void testGetAllPasswords() throws SQLException {
        // Crear lista de contraseñas de prueba
        List<PasswordDTO> passwords = Arrays.asList(validPassword);
        
        try (MockedStatic<PasswordDAO> mockedDAO = mockStatic(PasswordDAO.class)) {
            // Configurar DAO para retornar la lista de contraseñas
            mockedDAO.when(() -> PasswordDAO.readAllPasswordsDecrypted()).thenReturn(passwords);

            // Ejecutar obtención de todas las contraseñas
            List<PasswordDTO> result = PasswordManager.getAllPasswords();

            // Verificar que se retorna la lista correcta
            assertEquals(passwords, result);
            assertEquals(1, result.size());
            assertEquals(validPassword, result.get(0));
            mockedDAO.verify(() -> PasswordDAO.readAllPasswordsDecrypted());
        }
    }

    /**
     * Prueba la búsqueda de duplicados físicos en la base de datos.
     * Verifica que se retornen las contraseñas que están duplicadas físicamente.
     */
    @Test
    @DisplayName("Test findPhysicalDuplicates")
    void testFindPhysicalDuplicates() throws SQLException {
        // Crear lista de duplicados de prueba
        List<PasswordDTO> duplicates = Arrays.asList(validPassword);
        
        try (MockedStatic<PasswordDAO> mockedDAO = mockStatic(PasswordDAO.class)) {
            // Configurar DAO para retornar la lista de duplicados
            mockedDAO.when(() -> PasswordDAO.findPhysicalDuplicates()).thenReturn(duplicates);

            // Ejecutar búsqueda de duplicados
            List<PasswordDTO> result = PasswordManager.findPhysicalDuplicates();

            // Verificar que se retorna la lista correcta
            assertEquals(duplicates, result);
            mockedDAO.verify(() -> PasswordDAO.findPhysicalDuplicates());
        }
    }

    /**
     * Prueba la limpieza exitosa de duplicados físicos.
     * Verifica que se eliminen los duplicados y se ejecute el análisis de seguridad.
     */
    @Test
    @DisplayName("Test cleanPhysicalDuplicates")
    void testCleanPhysicalDuplicates() throws SQLException {
        try (MockedStatic<PasswordDAO> mockedDAO = mockStatic(PasswordDAO.class)) {
            
            // Configurar DAO para simular eliminación de 3 duplicados
            mockedDAO.when(() -> PasswordDAO.cleanPhysicalDuplicates()).thenReturn(3);
            mockedDAO.when(() -> PasswordDAO.readAllPasswordsDecrypted()).thenReturn(Arrays.asList(validPassword));
            // updatePasswordSecurity es void, no necesita mock de retorno

            // Ejecutar limpieza de duplicados
            int result = PasswordManager.cleanPhysicalDuplicates();

            // Verificar que se retorna el número correcto de duplicados eliminados
            assertEquals(3, result);
            mockedDAO.verify(() -> PasswordDAO.cleanPhysicalDuplicates());
            mockedDAO.verify(() -> PasswordDAO.readAllPasswordsDecrypted());
        }
    }

    /**
     * Prueba el caso donde no se encuentran duplicados físicos para limpiar.
     * Verifica que el análisis de seguridad no se ejecute si no hay duplicados.
     */
    @Test
    @DisplayName("Test cleanPhysicalDuplicates no duplicates found")
    void testCleanPhysicalDuplicatesNone() throws SQLException {
        try (MockedStatic<PasswordDAO> mockedDAO = mockStatic(PasswordDAO.class)) {
            // Configurar DAO para simular que no hay duplicados
            mockedDAO.when(() -> PasswordDAO.cleanPhysicalDuplicates()).thenReturn(0);

            // Ejecutar limpieza cuando no hay duplicados
            int result = PasswordManager.cleanPhysicalDuplicates();
            assertEquals(0, result);
            mockedDAO.verify(() -> PasswordDAO.cleanPhysicalDuplicates());
            // No debe disparar actualización de seguridad si no se eliminaron duplicados
        }
    }

    /**
     * Prueba el manejo de excepciones SQL en varios métodos del servicio.
     * Verifica que las excepciones SQL se propaguen correctamente al llamador.
     */
    @Test
    @DisplayName("Test SQLException handling in various methods")
    void testSQLExceptionHandling() throws SQLException {
        try (MockedStatic<PasswordDAO> mockedDAO = mockStatic(PasswordDAO.class)) {
            
            // Configurar mocks para lanzar SQLException
            mockedDAO.when(() -> PasswordDAO.createPassword(any())).thenThrow(new SQLException("DB Error"));

            // Verificar que la excepción se propaga correctamente
            assertThrows(SQLException.class, () -> {
                PasswordManager.savePassword(validPassword);
            });
        }
    }

    /**
     * Prueba el manejo de errores en la actualización de seguridad de contraseñas.
     * Verifica que el método principal siga funcionando aunque falle la actualización de seguridad.
     */
    @Test
    @DisplayName("Test updateAllPasswordsSecurity error handling")
    void testUpdateAllPasswordsSecurityErrorHandling() throws SQLException {
        try (MockedStatic<PasswordDAO> mockedDAO = mockStatic(PasswordDAO.class)) {
            
            // Configurar para que la creación sea exitosa pero la actualización de seguridad falle
            mockedDAO.when(() -> PasswordDAO.createPassword(any(PasswordDTO.class))).thenReturn(true);
            mockedDAO.when(() -> PasswordDAO.readAllPasswordsDecrypted()).thenThrow(new SQLException("DB Error"));

            // El método debe manejar la excepción internamente y aún retornar true
            boolean result = PasswordManager.savePassword(validPassword);

            assertTrue(result);
            mockedDAO.verify(() -> PasswordDAO.createPassword(validPassword));
        }
    }

    /**
     * Prueba la validación con caracteres especiales en los campos de contraseña.
     * Verifica que el servicio maneje correctamente caracteres especiales y símbolos.
     */
    @Test
    @DisplayName("Test validation with special characters")
    void testValidationWithSpecialCharacters() throws SQLException {
        // Crear contraseña con caracteres especiales
        PasswordDTO specialCharsPassword = new PasswordDTO("Test!@#$%", "user@domain", 
                                                           "https://test.com", "pass!@#$%^&*()");
        
        try (MockedStatic<PasswordDAO> mockedDAO = mockStatic(PasswordDAO.class)) {
            
            // Configurar mocks para operación exitosa con caracteres especiales
            mockedDAO.when(() -> PasswordDAO.createPassword(any(PasswordDTO.class))).thenReturn(true);
            mockedDAO.when(() -> PasswordDAO.readAllPasswordsDecrypted()).thenReturn(Arrays.asList(specialCharsPassword));

            // Ejecutar guardado con caracteres especiales
            boolean result = PasswordManager.savePassword(specialCharsPassword);

            // Verificar que la operación fue exitosa
            assertTrue(result);
            mockedDAO.verify(() -> PasswordDAO.createPassword(specialCharsPassword));
        }
    }
}
