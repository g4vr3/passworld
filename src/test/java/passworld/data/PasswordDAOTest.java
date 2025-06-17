package passworld.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import passworld.data.session.UserSession;
import passworld.utils.EncryptionUtil;

import java.sql.*;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Clase de pruebas para PasswordDAO.
 * 
 * Esta clase verifica todas las operaciones CRUD de la capa de acceso a datos
 * para las contraseñas, incluyendo:
 * - Creación de contraseñas (local y remota)
 * - Actualización de contraseñas (local, remota y por ID)
 * - Verificación de existencia por ID de Firebase
 * - Manejo de excepciones de base de datos
 * - Validación de datos nulos
 * 
 * Utiliza Mockito para simular las dependencias de base de datos y servicios externos.
 */
@ExtendWith(MockitoExtension.class)
class PasswordDAOTest {

    // Objetos de prueba y mocks
    private PasswordDTO testPassword;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;
    private UserSession mockUserSession;

    /**
     * Configuración inicial que se ejecuta antes de cada prueba.
     * Inicializa los objetos de prueba y los mocks necesarios.
     */
    @BeforeEach
    void setUp() throws Exception {
        // Crear una contraseña de prueba con datos válidos
        testPassword = new PasswordDTO("Test Service", "testuser", 
                                     "https://test.com", "testpass123", 
                                     LocalDateTime.now());
        testPassword.setId(1);
        testPassword.setIdFb("fb_123");

        // Configurar mocks para componentes de base de datos
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        
        // Mock para sesión de usuario y utilidades de encriptación
        mockUserSession = mock(UserSession.class);
    }

    /**
     * Prueba la creación exitosa de una contraseña en la base de datos local.
     * Verifica que:
     * - La contraseña se crea correctamente
     * - Se asigna un ID generado automáticamente
     * - Se realizan las llamadas correctas a setString y setBoolean
     */
    @Test
    @DisplayName("Test createPassword success")
    void testCreatePasswordSuccess() throws SQLException {
        // Configurar comportamiento de la base de datos simulada
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(42);

        // Usar mocks estáticos para simular dependencias externas
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class);
             MockedStatic<DDL> mockedDDL = mockStatic(DDL.class);
             MockedStatic<UserSession> mockedUserSession = mockStatic(UserSession.class);
             MockedStatic<EncryptionUtil> mockedEncryption = mockStatic(EncryptionUtil.class)) {
            
            // Configurar respuestas de los mocks estáticos
            mockedDDL.when(DDL::getDbUrl).thenReturn("jdbc:sqlite::memory:");
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString()))
                              .thenReturn(mockConnection);
            mockedUserSession.when(UserSession::getInstance).thenReturn(mockUserSession);
            mockedEncryption.when(() -> EncryptionUtil.encryptData(anyString(), any()))
                           .thenReturn("encrypted_data");

            // Ejecutar la operación bajo prueba
            boolean result = PasswordDAO.createPassword(testPassword);

            // Verificar resultados esperados
            assertTrue(result);
            assertEquals(42, testPassword.getId());
            verify(mockPreparedStatement, times(6)).setString(anyInt(), anyString());
            verify(mockPreparedStatement, times(5)).setBoolean(anyInt(), anyBoolean());
        }
    }

    /**
     * Prueba el fallo en la creación de una contraseña.
     * Simula cuando executeUpdate() retorna 0 (ninguna fila afectada).
     */
    @Test
    @DisplayName("Test createPassword failure")
    void testCreatePasswordFailure() throws SQLException {
        // Configurar mock para simular fallo en la inserción
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0); // Ninguna fila afectada

        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class);
             MockedStatic<DDL> mockedDDL = mockStatic(DDL.class);
             MockedStatic<UserSession> mockedUserSession = mockStatic(UserSession.class);
             MockedStatic<EncryptionUtil> mockedEncryption = mockStatic(EncryptionUtil.class)) {
            
            mockedDDL.when(DDL::getDbUrl).thenReturn("jdbc:sqlite::memory:");
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString()))
                              .thenReturn(mockConnection);
            mockedUserSession.when(UserSession::getInstance).thenReturn(mockUserSession);
            mockedEncryption.when(() -> EncryptionUtil.encryptData(anyString(), any()))
                           .thenReturn("encrypted_data");

            // Ejecutar y verificar que la operación falla
            boolean result = PasswordDAO.createPassword(testPassword);

            assertFalse(result);
            verify(mockPreparedStatement).executeUpdate();
        }
    }

    /**
     * Prueba la creación exitosa de una contraseña desde datos remotos.
     * Este método no encripta los datos, ya que vienen desde Firebase.
     */
    @Test
    @DisplayName("Test createFromRemote success")
    void testCreateFromRemoteSuccess() throws SQLException {
        // Configurar comportamiento exitoso de la base de datos
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(42);

        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class);
             MockedStatic<DDL> mockedDDL = mockStatic(DDL.class)) {
            
            mockedDDL.when(DDL::getDbUrl).thenReturn("jdbc:sqlite::memory:");
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString()))
                              .thenReturn(mockConnection);

            boolean result = PasswordDAO.createFromRemote(testPassword);

            assertTrue(result);
            assertEquals(42, testPassword.getId());
            // Verificar que la contraseña se almacena sin encriptación adicional
            verify(mockPreparedStatement, times(6)).setString(anyInt(), anyString());
            verify(mockPreparedStatement, times(5)).setBoolean(anyInt(), anyBoolean());
        }
    }

    /**
     * Prueba la actualización exitosa de una contraseña existente.
     * Verifica que se actualicen todos los campos y se mantenga la encriptación.
     */
    @Test
    @DisplayName("Test updatePassword success")
    void testUpdatePasswordSuccess() throws SQLException {
        // Configurar mock para actualización exitosa
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class);
             MockedStatic<DDL> mockedDDL = mockStatic(DDL.class);
             MockedStatic<UserSession> mockedUserSession = mockStatic(UserSession.class);
             MockedStatic<EncryptionUtil> mockedEncryption = mockStatic(EncryptionUtil.class)) {
            
            mockedDDL.when(DDL::getDbUrl).thenReturn("jdbc:sqlite::memory:");
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString()))
                              .thenReturn(mockConnection);
            mockedUserSession.when(UserSession::getInstance).thenReturn(mockUserSession);
            mockedEncryption.when(() -> EncryptionUtil.encryptData(anyString(), any()))
                           .thenReturn("encrypted_data");

            boolean result = PasswordDAO.updatePassword(testPassword);

            // Verificar que la actualización fue exitosa y se hicieron las llamadas correctas
            assertTrue(result);
            verify(mockPreparedStatement).executeUpdate();
            verify(mockPreparedStatement, times(6)).setString(anyInt(), anyString());
            verify(mockPreparedStatement, times(5)).setBoolean(anyInt(), anyBoolean());
            verify(mockPreparedStatement).setInt(anyInt(), anyInt());
        }
    }

    /**
     * Prueba el fallo en la actualización de una contraseña.
     * Simula cuando no se encuentra la contraseña a actualizar.
     */
    @Test
    @DisplayName("Test updatePassword failure")
    void testUpdatePasswordFailure() throws SQLException {
        // Configurar mock para simular que no se encontró la contraseña
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0); // Ninguna fila afectada

        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class);
             MockedStatic<DDL> mockedDDL = mockStatic(DDL.class);
             MockedStatic<UserSession> mockedUserSession = mockStatic(UserSession.class);
             MockedStatic<EncryptionUtil> mockedEncryption = mockStatic(EncryptionUtil.class)) {
            
            mockedDDL.when(DDL::getDbUrl).thenReturn("jdbc:sqlite::memory:");
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString()))
                              .thenReturn(mockConnection);
            mockedUserSession.when(UserSession::getInstance).thenReturn(mockUserSession);
            mockedEncryption.when(() -> EncryptionUtil.encryptData(anyString(), any()))
                           .thenReturn("encrypted_data");

            boolean result = PasswordDAO.updatePassword(testPassword);

            assertFalse(result);
        }
    }

    /**
     * Prueba la verificación de existencia de una contraseña por su ID de Firebase.
     * Caso: la contraseña existe en la base de datos.
     */
    @Test
    @DisplayName("Test existsByIdFb - exists")
    void testExistsByIdFbExists() throws SQLException {
        // Configurar mock para simular que la contraseña existe
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(1); // COUNT > 0

        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class);
             MockedStatic<DDL> mockedDDL = mockStatic(DDL.class)) {
            
            mockedDDL.when(DDL::getDbUrl).thenReturn("jdbc:sqlite::memory:");
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString()))
                              .thenReturn(mockConnection);

            boolean result = PasswordDAO.existsByIdFb("fb_123");

            assertTrue(result);
            verify(mockPreparedStatement).setString(1, "fb_123");
        }
    }

    /**
     * Prueba la verificación de existencia de una contraseña por su ID de Firebase.
     * Caso: la contraseña no existe en la base de datos.
     */
    @Test
    @DisplayName("Test existsByIdFb - does not exist")
    void testExistsByIdFbNotExists() throws SQLException {
        // Configurar mock para simular que la contraseña no existe
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(0); // COUNT = 0

        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class);
             MockedStatic<DDL> mockedDDL = mockStatic(DDL.class)) {
            
            mockedDDL.when(DDL::getDbUrl).thenReturn("jdbc:sqlite::memory:");
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString()))
                              .thenReturn(mockConnection);

            boolean result = PasswordDAO.existsByIdFb("nonexistent");

            assertFalse(result);
        }
    }

    /**
     * Prueba el manejo de excepciones de conexión a la base de datos.
     * Verifica que se propague correctamente la SQLException cuando falla la conexión.
     */
    @Test
    @DisplayName("Test database connection exception handling")
    void testDatabaseConnectionException() {
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class);
             MockedStatic<DDL> mockedDDL = mockStatic(DDL.class)) {
            
            mockedDDL.when(DDL::getDbUrl).thenReturn("jdbc:sqlite::memory:");
            // Crear excepción de antemano para evitar problemas de stubbing
            SQLException connectionException = new SQLException("Connection failed");
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString()))
                              .thenThrow(connectionException);

            // Verificar que se lanza la excepción esperada
            assertThrows(SQLException.class, () -> {
                PasswordDAO.createPassword(testPassword);
            });
        }
    }

    /**
     * Prueba el manejo de excepciones durante la ejecución de prepared statements.
     * Verifica que se propague correctamente la SQLException cuando falla la ejecución.
     */
    @Test
    @DisplayName("Test SQL exception during prepared statement execution")
    void testSQLExceptionDuringExecution() throws SQLException {
        // Configurar mock para que falle durante la ejecución
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Execution failed"));

        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class);
             MockedStatic<DDL> mockedDDL = mockStatic(DDL.class);
             MockedStatic<UserSession> mockedUserSession = mockStatic(UserSession.class);
             MockedStatic<EncryptionUtil> mockedEncryption = mockStatic(EncryptionUtil.class)) {
            
            mockedDDL.when(DDL::getDbUrl).thenReturn("jdbc:sqlite::memory:");
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString()))
                              .thenReturn(mockConnection);
            mockedUserSession.when(UserSession::getInstance).thenReturn(mockUserSession);
            mockedEncryption.when(() -> EncryptionUtil.encryptData(anyString(), any()))
                           .thenReturn("encrypted_data");

            // Verificar que se lanza la excepción de ejecución
            assertThrows(SQLException.class, () -> {
                PasswordDAO.createPassword(testPassword);
            });
        }
    }

    /**
     * Prueba el manejo de contraseñas con campos nulos.
     * Verifica que el DAO pueda manejar correctamente datos parciales o nulos.
     */
    @Test
    @DisplayName("Test password with null fields")
    void testPasswordWithNullFields() throws SQLException {
        // Crear contraseña con campos nulos para probar robustez
        PasswordDTO nullFieldsPassword = new PasswordDTO(null, null, null, null);
        
        // Configurar comportamiento exitoso a pesar de los campos nulos
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(42);

        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class);
             MockedStatic<DDL> mockedDDL = mockStatic(DDL.class);
             MockedStatic<UserSession> mockedUserSession = mockStatic(UserSession.class);
             MockedStatic<EncryptionUtil> mockedEncryption = mockStatic(EncryptionUtil.class)) {
            
            mockedDDL.when(DDL::getDbUrl).thenReturn("jdbc:sqlite::memory:");
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString()))
                              .thenReturn(mockConnection);
            mockedUserSession.when(UserSession::getInstance).thenReturn(mockUserSession);
            // Configurar encriptación para manejar valores nulos
            mockedEncryption.when(() -> EncryptionUtil.encryptData(isNull(), any()))
                           .thenReturn(null);

            boolean result = PasswordDAO.createPassword(nullFieldsPassword);

            // Verificar que aún se puede crear la contraseña con campos nulos
            assertTrue(result);
            assertEquals(42, nullFieldsPassword.getId());
        }
    }

    /**
     * Prueba la actualización exitosa de una contraseña desde datos remotos usando idFb.
     * Este método intenta actualizar primero por idFb y luego por ID local si falla.
     */
    @Test
    @DisplayName("Test updatePasswordFromRemote with idFb success")
    void testUpdatePasswordFromRemoteWithIdFb() throws SQLException {
        // Configurar actualización exitosa por idFb
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class);
             MockedStatic<DDL> mockedDDL = mockStatic(DDL.class)) {
            
            mockedDDL.when(DDL::getDbUrl).thenReturn("jdbc:sqlite::memory:");
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString()))
                              .thenReturn(mockConnection);

            boolean result = PasswordDAO.updatePasswordFromRemote(testPassword);

            assertTrue(result);
            verify(mockPreparedStatement).executeUpdate();
        }
    }

    /**
     * Prueba el mecanismo de respaldo en updatePasswordFromRemote.
     * Si la actualización por idFb falla, debe intentar actualizar por ID local.
     */
    @Test
    @DisplayName("Test updatePasswordFromRemote fallback to ID")
    void testUpdatePasswordFromRemoteFallbackToId() throws SQLException {
        // Configurar para que falle por idFb pero funcione por ID
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        // Primera llamada (por idFb) retorna 0, segunda llamada (por id) retorna 1
        when(mockPreparedStatement.executeUpdate()).thenReturn(0, 1);

        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class);
             MockedStatic<DDL> mockedDDL = mockStatic(DDL.class)) {
            
            mockedDDL.when(DDL::getDbUrl).thenReturn("jdbc:sqlite::memory:");
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString()))
                              .thenReturn(mockConnection);

            boolean result = PasswordDAO.updatePasswordFromRemote(testPassword);

            assertTrue(result);
            // Verificar que se intentaron ambas actualizaciones
            verify(mockPreparedStatement, times(2)).executeUpdate();
        }
    }

    /**
     * Prueba la actualización exitosa de una contraseña por su ID local.
     * Este método actualiza específicamente usando el ID numérico de la contraseña.
     */
    @Test
    @DisplayName("Test updatePasswordById success")
    void testUpdatePasswordByIdSuccess() throws SQLException {
        // Configurar actualización exitosa por ID
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class);
             MockedStatic<DDL> mockedDDL = mockStatic(DDL.class)) {
            
            mockedDDL.when(DDL::getDbUrl).thenReturn("jdbc:sqlite::memory:");
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString()))
                              .thenReturn(mockConnection);

            boolean result = PasswordDAO.updatePasswordById(testPassword);

            assertTrue(result);
            verify(mockPreparedStatement).executeUpdate();
            // Verificar que se establece el ID en la posición correcta del prepared statement
            verify(mockPreparedStatement).setInt(12, testPassword.getId());
        }
    }
}
