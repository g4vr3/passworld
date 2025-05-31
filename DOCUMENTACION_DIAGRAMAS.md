# Documentación Técnica - Passworld
## Diagramas de Flujo y Arquitectura del Sistema

Este documento contiene los diagramas técnicos más relevantes del proyecto Passworld, incluyendo diagramas de flujo de procesos, diagrama de clases y diagrama de base de datos.

---

## 1. Diagrama de Flujo - Proceso de Autenticación

```mermaid
flowchart TD
    A[Inicio de Aplicación] --> D{¿Tiene Sesión Guardada?}
    
    D -->|Sí| E[Verificar Tokens]
    D -->|No| F[Mostrar Vista de Autenticación]
    
    E --> G{¿Tokens Válidos?}
    G -->|Sí| H[Solicitar Contraseña Maestra]
    G -->|No| F
    
    F --> I{¿Registro o Login?}
    I -->|Registro| J[Validar Datos de Registro]
    I -->|Login| K[Validar Credenciales]
    
    J --> L[Validar Email]
    L --> M[Validar Contraseña]
    M --> N[Validar Contraseña Maestra]
    N --> O[Registrar Usuario en Firebase]
    O --> P[Guardar Hash Contraseña Maestra]
    P --> H
    
    K --> Q[Autenticar con Firebase]
    Q --> R{¿Credenciales Correctas?}
    R -->|No| S[Mostrar Error]
    R -->|Sí| P
    S --> F
    
    H --> T[Verificar Contraseña Maestra]
    T --> U{¿Contraseña Correcta?}
    U -->|No| V[Mostrar Error y Retry]
    U -->|Sí| W[Derivar Clave AES]
    V --> H
    W --> X[Inicializar Sesión Usuario]
    X --> Y[Navegar a Vista Principal]
```

---

## 2. Diagrama de Flujo - Gestión de Contraseñas

```mermaid
flowchart TD
    A[Vista Principal Passworld] --> B{¿Qué Acción?}
    
    B -->|Generar Contraseña| C[Configurar Parámetros]
    B -->|Ver Mis Contraseñas| D[Cargar Lista de Contraseñas]
    B -->|Cerrar Sesión| E[Confirmar Logout]
    
    C --> F[Seleccionar Longitud]
    F --> G[Seleccionar Caracteres]
    G --> H[Generar Contraseña Aleatoria]
    H --> I[Evaluar Fortaleza]
    I --> J{¿Guardar Contraseña?}
    J -->|Sí| K[Mostrar Diálogo Guardar]
    J -->|No| L[Copiar al Portapapeles]
    K --> M[Validar Datos Obligatorios]
    M --> N[Encriptar Datos]
    N --> O[Guardar en BD Local]
    O --> P[Analizar Seguridad]
    P --> Q[Marcar para Sincronizar]
    
    D --> R[Desencriptar Contraseñas]
    R --> S[Aplicar Filtros de Seguridad]
    S --> T[Mostrar en Tabla]
    T --> U{¿Acción sobre Contraseña?}
    U -->|Ver Detalles| V[Abrir Vista Detalle]
    U -->|Buscar| W[Filtrar por Texto]
    U -->|Ordenar| X[Aplicar Ordenamiento]
    
    V --> Y[Mostrar Información Completa]
    Y --> Z{¿Modificar?}
    Z -->|Sí| AA[Validar Cambios]
    Z -->|Eliminar| BB[Confirmar Eliminación]
    AA --> CC[Actualizar BD Local]
    CC --> DD[Marcar para Sincronizar]
    BB --> EE[Eliminar de BD]
    EE --> FF[Registrar Eliminación]
    
    E --> GG[Limpiar Tokens]
    GG --> HH[Limpiar Sesión]
    HH --> II[Volver a Autenticación]
```

---

## 3. Diagrama de Flujo - Sincronización de Datos

```mermaid
flowchart TD
    A[Hilo de Sincronización] --> B{¿Conexión Internet?}
    B -->|No| C[Esperar 7 segundos]
    B -->|Sí| D{¿Usuario Logueado?}
    C --> A
    
    D -->|No| E[Actualizar Estado: No Sincronizado]
    D -->|Sí| F[Sincronizar Tiempo UTC]
    E --> C
    
    F --> G[Obtener Contraseñas Locales]
    G --> H[Eliminar en Remoto las Eliminadas Localmente]
    H --> I[Descargar Contraseñas Remotas]
    I --> J[Crear Mapas de Acceso Rápido]
    
    J --> K[Eliminar Localmente las que ya no existen en Remoto]
    K --> L[Para cada Contraseña Local No Sincronizada]
    
    L --> M{¿Tiene ID Remoto?}
    M -->|No| N[Crear en Remoto]
    M -->|Sí| O{¿Existe en Remoto?}
    
    N --> P[Asignar ID Remoto]
    P --> Q[Marcar como Sincronizada]
    
    O -->|Sí| R[Actualizar en Remoto]
    O -->|No| S[Eliminar Localmente]
    R --> Q
    
    Q --> T{¿Más Contraseñas Locales?}
    T -->|Sí| L
    T -->|No| U[Para cada Contraseña Remota]
    
    U --> V{¿Fue Eliminada por Usuario?}
    V -->|Sí| W[Ignorar]
    V -->|No| X{¿Existe Localmente?}
    
    X -->|No| Y[Insertar Localmente]
    X -->|Sí| Z{¿Remota más Reciente?}
    
    Z -->|Sí| AA[Actualizar Local]
    Z -->|No| BB[Mantener Local]
    
    Y --> CC{¿Más Contraseñas Remotas?}
    AA --> CC
    BB --> CC
    W --> CC
    
    CC -->|Sí| U
    CC -->|No| DD[Actualizar Estado: Sincronizado]
    DD --> EE[Recargar Vista]
    EE --> C
    
    S --> T
```

---

## 4. Diagrama de Flujo - Evaluación de Seguridad de Contraseñas

```mermaid
flowchart TD
    A[Nueva Contraseña] --> B[Analizar Fortaleza]
    B --> F[Asignar Puntuación 0-4]
    
    F --> G{¿Es Débil? Puntuación < 3}
    G -->|Sí| H[Marcar como Débil]
    G -->|No| I[Marcar como Fuerte]
    
    H --> J[Verificar en Lista de Comprometidas]
    I --> J
    
    J --> K{¿Está Comprometida?}
    K -->|Sí| L[Marcar como Comprometida]
    K -->|No| M[Marcar como Segura]
    
    L --> N[Verificar Duplicados]
    M --> N
    
    N --> O[Buscar en Otras Contraseñas]
    O --> P{¿Existe Duplicado?}
    P -->|Sí| Q[Marcar como Duplicada]
    P -->|No| R[Marcar como Única]
    
    Q --> S[Verificar URL]
    R --> S
    
    S --> T{¿URL Utiliza HTTPS?}
    T -->|No| U[Marcar URL como Insegura]
    T -->|Sí| V[Marcar URL como Segura]
    
    U --> W[Actualizar Estado en BD]
    V --> W
    W --> X[Mostrar Indicadores Visuales]
    X --> Y[Fin del Análisis]
      %% Nota explicativa del análisis de fortaleza
    B -.-> NOTA["<b>Criterios de Evaluación:</b><br/>• Longitud (≥8, ≥12, ≥16): +2pts c/u<br/>• Diversidad caracteres: +2pts x tipo<br/>  - Mayúsculas, minúsculas<br/>  - Números, símbolos especiales<br/>• Palabras comunes (Trie): -5pts<br/>• Secuencias (abcd, 1234): -2pts<br/>• Patrones repetitivos: -2pts<br/>• Muy corta (<8 chars): -2pts<br/><br/><b>Escala:</b> 0=Muy débil, 4=Muy fuerte"]
    
    %% Estilo para la nota
    classDef noteStyle fill:#fff2cc,stroke:#d6b656,stroke-width:2px,color:#000
    class NOTA noteStyle
```

---

## 5. Diagrama de Clases Completo del Sistema

```mermaid
classDiagram
    %% ================================
    %% PAQUETE: passworld (Principal)
    %% ================================
    class PassworldApplication {
        -HostServices hostServices
        +start(Stage primaryStage) void
        +main(String[] args) void
        +getHostServicesInstance() HostServices
    }
    
    class Main {
        +main(String[] args) void
    }
      %% ================================
    %% PAQUETE: passworld.controller
    %% ================================
    class AuthController {
        +initialize() void
        +showSignupSection() void
        +showLoginSection() void
        +handleSignup() void
        +handleLogin() void
        +showView() void
        -validateSignupFields() boolean
        -validateLoginFields() boolean
        -setupPasswordToggle() void
        -showErrorMessage(String) void
        -clearErrorMessages() void
    }
    
    class PassworldController {
        +initialize() void
        +generatePassword() void
        +savePassword() void
        +viewPasswords() void
        +handleLogout() void
        +showView() void
        -setupPasswordGeneration() void
        -updatePasswordLength() void
        -validateGenerationOptions() boolean
        -copyToClipboard(String) void
    }
    
    class MyPasswordsController {
        -List~PasswordDTO~ passwordList
        -List~PasswordDTO~ issuePasswordsList
        -List~PasswordDTO~ filteredPasswordsList
        +initialize() void
        +loadPasswords() void
        +showAllPasswords() void
        +showIssuePasswords() void
        +deletePassword(PasswordDTO) void
        +updatePassword(PasswordDTO, String, String, String, String) void
        +syncPasswordsPeriodically() void
        +refreshPasswords() void
        +showView() void
        -setupTable() void
        -setupPasswordEntry() void
        -setupInfoButton() void
        -filterPasswords() void
        -updateSyncStatus() void
    }
    
    class PasswordInfoController {
        -PasswordDTO password
        -MyPasswordsController parentController
        +initialize() void
        +setData(PasswordDTO, MyPasswordsController) void
        +showView(PasswordDTO, MyPasswordsController) void
        -savePassword() void
        -deletePassword() void
        -validateFields() void
        -checkPasswordIssues() void
        -setupPasswordToggle() void
        -setupSecurityWarnings() void
        -copyToClipboard(String) void
        -openUrl() void
    }
    
    class VaultProtectionController {
        -boolean passwordVerified
        +initialize() void
        +showView() void
        +showAndVerifyPassword() boolean
        -setupMasterPasswordField() void
        -setupPasswordToggle() void
        -verifyMasterPassword() void
        -isValidPassword(String) boolean
        -handleUnlock() void
    }
    
    class SplashScreenController {
        +initialize() void
        +showView() void
        -startProgressAnimation() void
        -initializeApplication() void
        -navigateToAuth() void
    }
    
    %% ================================
    %% PAQUETE: passworld.data
    %% ================================
    class PasswordDTO {
        -int id
        -String idFb
        -String description
        -String username
        -String url
        -String password
        -boolean isWeak
        -boolean isDuplicate
        -boolean isCompromised
        -boolean isUrlUnsafe
        -boolean isSynced
        -LocalDateTime lastModified
        +PasswordDTO()
        +PasswordDTO(String, String, String, String)
        +PasswordDTO(int, String, String, String, String, String)
        +getId() int
        +setId(int) void
        +getIdFb() String
        +setIdFb(String) void
        +getDescription() String
        +setDescription(String) void
        +getUsername() String
        +setUsername(String) void
        +getUrl() String
        +setUrl(String) void
        +getPassword() String
        +setPassword(String) void
        +isWeak() boolean
        +setWeak(boolean) void
        +isDuplicate() boolean
        +setDuplicate(boolean) void
        +isCompromised() boolean
        +setCompromised(boolean) void
        +isUrlUnsafe() boolean
        +setUrlUnsafe(boolean) void
        +isSynced() boolean
        +setSynced(boolean) void
        +getLastModified() LocalDateTime
        +setLastModified(LocalDateTime) void
        +hasSecurityIssues() boolean
        +toString() String
    }
    
    class PasswordDAO {
        -String DB_URL
        +createPassword(PasswordDTO) boolean
        +readAllPasswordsDecrypted() List~PasswordDTO~
        +readPasswordById(int) PasswordDTO
        +updatePassword(PasswordDTO) boolean
        +deletePassword(int) boolean
        +updatePasswordSecurity(PasswordDTO) void
        +existsByIdFb(String) boolean
        +updatePasswordSyncStatus(int, boolean) void
        +getUnsyncedPasswords() List~PasswordDTO~
        +deletePasswordByIdFb(String) boolean
        -encryptData(String) String
        -decryptData(String) String
        -mapResultSetToPasswordDTO(ResultSet) PasswordDTO
    }
    
    class DDL {
        +createDatabase() void
        +createPasswordsTable() void
        +createMasterPasswordTable() void
        +createDeletedPasswordsTable() void
        -executeUpdate(String) boolean
    }
    
    class LocalAuthUtil {
        +saveMasterPasswordHash(String) boolean
        +verifyMasterPassword(String) boolean
        +isMasterPasswordSet() boolean
        +deleteMasterPassword() boolean
        -hashPassword(String) String
        -verifyPassword(String, String) boolean
    }
    
    class DeletedPasswordsDAO {
        +addDeletedPassword(String) boolean
        +removeDeletedPassword(String) boolean
        +getAllDeletedPasswordIds() List~String~
        +isPasswordDeleted(String) boolean
        +clearDeletedPasswords() void
    }
    
    %% ================================
    %% PAQUETE: passworld.data.session
    %% ================================
    class UserSession {
        -String idToken
        -String refreshToken
        -String userId
        -String email
        -SecretKeySpec masterKey
        -UserSession instance
        +getInstance() UserSession
        +setIdToken(String) void
        +getIdToken() String
        +setRefreshToken(String) void
        +getRefreshToken() String
        +setUserId(String) void
        +getUserId() String
        +setEmail(String) void
        +getEmail() String
        +setMasterKey(SecretKeySpec) void
        +getMasterKey() SecretKeySpec
        +isLoggedIn() boolean
        +clearSession() void
    }
    
    class PersistentSessionManager {
        -String SESSION_FILE_PATH
        +saveSession(UserSession) void
        +loadSession() UserSession
        +deleteSession() void
        +hasValidSession() boolean
        -encryptSessionData(String) String
        -decryptSessionData(String) String
        -createSessionFile() void
    }
    
    %% ================================
    %% PAQUETE: passworld.data.sync
    %% ================================
    class SyncHandler {
        -PasswordsApiClient passwordsApiClient
        -UsersApiClient usersApiClient
        -PasswordManager passwordManager
        -DeletedPasswordsDAO deletedPasswordsDAO
        -boolean isSyncing
        -Timeline syncTimeline
        -Thread tokenRefreshThread
        +SyncHandler()
        +syncPasswords(List~PasswordDTO~) void
        +startPeriodicSync() void
        +stopPeriodicSync() void
        +hasInternetConnection() boolean
        +startTokenRefreshThread() void
        +stopTokenRefreshThread() void
        -syncDeletedPasswords() void
        -syncLocalToRemote(List~PasswordDTO~) void
        -syncRemoteToLocal(List~PasswordDTO~) void
        -shouldUpdateLocalPassword(PasswordDTO, PasswordDTO) boolean
        -getCurrentUTCTime() String
    }
    
    %% ================================
    %% PAQUETE: passworld.data.apiclients
    %% ================================
    class PasswordsApiClient {
        -String API_BASE_URL
        -OkHttpClient client
        -ObjectMapper objectMapper
        +createPassword(PasswordDTO) PasswordDTO
        +getPasswordsByUserId(String) List~PasswordDTO~
        +updatePassword(PasswordDTO) PasswordDTO
        +deletePassword(String) boolean
        +getServerTime() String
        -buildAuthenticatedRequest(String, String) Request.Builder
        -executeRequest(Request) Response
        -handleApiResponse(Response, Class) Object
    }
    
    class UsersApiClient {
        -String API_BASE_URL
        -OkHttpClient client
        -ObjectMapper objectMapper
        +refreshToken(String) Map~String,String~
        +getUserInfo(String) Map~String,Object~
        +validateToken(String) boolean
        -buildRequest(String, String) Request.Builder
        -executeRequest(Request) Response
        -parseJsonResponse(Response) Map~String,Object~
    }
    
    %% ================================
    %% PAQUETE: passworld.service
    %% ================================
    class PasswordManager {
        -PasswordDAO passwordDAO
        -SecurityFilterUtils securityFilterService
        +savePassword(PasswordDTO) boolean
        +updatePassword(PasswordDTO, String, String, String, String) boolean
        +deletePassword(int) boolean
        +getPasswordById(int) PasswordDTO
        +getAllPasswords() List~PasswordDTO~
        +getPasswordsWithIssues() List~PasswordDTO~
        +savePasswordFromRemote(PasswordDTO) void
        +updatePasswordByRemote(PasswordDTO) void
        +deletePasswordByRemoteId(String) void
        +getUnsyncedPasswords() List~PasswordDTO~
        -validatePasswordData(PasswordDTO) void
        -updateAllPasswordsSecurity() void
        -updatePasswordSecurity(PasswordDTO) void
    }
    
    %% ================================
    %% PAQUETE: passworld.utils
    %% ================================
    class EncryptionUtil {
        -String ALGORITHM
        -String TRANSFORMATION
        -String KEY_ALGORITHM
        -int KEY_LENGTH
        -int IV_LENGTH
        +deriveKeyFromPassword(String, byte[]) SecretKeySpec
        +generateSalt() byte[]
        +encrypt(String, SecretKeySpec) String
        +decrypt(String, SecretKeySpec) String
        -generateIV() byte[]
        -bytesToHex(byte[]) String
        -hexToBytes(String) byte[]
    }
    
    class SecurityFilterUtils {
        -Set~String~ uniquePasswords
        -Set~String~ commonPasswords
        -Set~String~ compromisedPasswords
        +SecurityFilterUtils()
        +analyzePasswordSecurity(PasswordDTO) void
        +hasPasswordSecurityIssues(PasswordDTO) boolean
        +addUniquePassword(String) void
        +removeUniquePassword(String) void
        +clearUniquePasswords() void
        +loadCommonPasswords() void
        -isPasswordWeak(String) boolean
        -isPasswordDuplicate(String) boolean
        -isPasswordCompromised(String) boolean
        -isUrlUnsafe(String) boolean
        -checkPasswordStrength(String) boolean
    }
      class PasswordEvaluator {
        -Set~String~ commonWords
        +calculateStrength(String) int
        +updatePasswordStrengthInfo(int, Label, ProgressBar) void
        +loadCommonWords() void
        +getStrengthText(int) String
        +getStrengthColor(int) String
        -hasUpperCase(String) boolean
        -hasLowerCase(String) boolean
        -hasDigits(String) boolean
        -hasSpecialChars(String) boolean
        -containsCommonWords(String) boolean
        -hasRepeatingPatterns(String) boolean
    }
    
    class PasswordGenerator {
        -String UPPERCASE
        -String LOWERCASE
        -String NUMBERS
        -String SPECIAL_CHARACTERS
        +generatePassword(boolean, boolean, boolean, int) String
    }
    
    class CompromisedPasswordChecker {
        -String API_URL
        +isCompromisedPassword(String) boolean
        -sha1(String) String
    }
    
    class UnsafeUrlChecker {
        -String API_URL
        -String API_KEY
        +isUnsafe(String) boolean
    }
    
    class TimeSyncManager {
        -Duration timeOffset
        -String NTP_SERVER
        -int NTP_PORT
        +syncTimeWithUtcServer() void
        +getCurrentUtcTime() String
        +adjustTimeByOffset(LocalDateTime) LocalDateTime
        -getNtpTime(String) long
    }
    
    class LogUtils {
        +Logger LOGGER
        -String logFilePath
        +initializeLogger() void
    }
    
    class Notifier {
        +showNotification(Window, String) void
    }
    
    class AnimationUtil {
        +shakeField(TextField) void
    }
    
    class Accessibility {
        +setSelectAllOnFocus(TextField) void
        +setShowLanguageListOnFocus(ComboBox) void
        +addCopyShortcut(TextField, Runnable) void
        +isMac() boolean
        +isWindows() boolean
    }
    
    class HeaderConfigurator {
        +configureHeader(ImageView, ImageView, Button, Button) void
        -configureLanguageMenu(ImageView) void
        -configureThemeButton(Button) void
        -configureHelpButton(Button) void
    }
    
    class Trie {
        -TrieNode root
        +Trie()
        +insert(String) void
        +search(String) boolean
        +containsSubstring(String) boolean
        -normalize(String) String
        -insert(TrieNode, String) void
        -commonPrefixLength(String, String) int
    }
    
    class ViewManager {
        -Stage primaryStage
        -Scene currentScene
        -ViewManager instance
        +getInstance() ViewManager
        +setPrimaryStage(Stage) void
        +getPrimaryStage() Stage
        +navigateToView(String) void
        +navigateToView(String, Object) void
        +showDialog(String, String, String) void
        +showConfirmationDialog(String, String) boolean
        -loadFXML(String) Parent
        -applyTheme(Scene) void
    }
    
    class ThemeManager {
        -String currentTheme
        -ThemeManager instance
        +getInstance() ThemeManager
        +getCurrentTheme() String
        +setTheme(String) void
        +isDarkMode() boolean
        +toggleTheme() void
        +applyTheme(Scene) void
        +getThemeStylesheet() String
        -saveThemePreference() void
        -loadThemePreference() void
    }
    
    class LanguageUtil {
        -ResourceBundle resourceBundle
        -Locale currentLocale
        -LanguageUtil instance
        +getInstance() LanguageUtil
        +setLocale(Locale) void
        +getCurrentLocale() Locale
        +getString(String) String
        +getString(String, Object...) String
        +getAvailableLocales() List~Locale~
        -loadResourceBundle() void
        -saveLocalePreference() void
        -loadLocalePreference() void
    }
    
    class DialogUtil {
        +showError(String, String) void
        +showWarning(String, String) void
        +showInfo(String, String) void
        +showConfirmation(String, String) boolean
        +showInputDialog(String, String) String
        +showPasswordDialog(String, String) String
        +showProgressDialog(String, Task) void
        -createAlert(Alert.AlertType, String, String) Alert
        -applyCurrentTheme(Alert) void
    }
      %% ================================
    %% RELACIONES ENTRE CLASES
    %% ================================
    
    %% Relaciones de la aplicación principal
    Main --> PassworldApplication : "inicia aplicación"
    PassworldApplication --> SplashScreenController : "muestra pantalla inicial"
    
    %% Relaciones de navegación entre controladores
    SplashScreenController --> AuthController : "navega hacia autenticación"
    AuthController --> VaultProtectionController : "navega tras login exitoso"
    VaultProtectionController --> PassworldController : "navega tras desbloqueo"
    PassworldController --> MyPasswordsController : "navega a vista contraseñas"
    MyPasswordsController --> PasswordInfoController : "navega a detalles"
    
    %% Relaciones de dependencia de servicios
    AuthController --> UserSession : "gestiona sesión usuario"
    AuthController --> LocalAuthUtil : "valida credenciales locales"
    AuthController --> ViewManager : "controla navegación"
    VaultProtectionController --> UserSession : "accede a sesión activa"
    VaultProtectionController --> LocalAuthUtil : "verifica contraseña maestra"
    PassworldController --> PasswordManager : "gestiona contraseñas"
    PassworldController --> PasswordEvaluator : "evalúa fortaleza"
    MyPasswordsController --> PasswordManager : "obtiene lista contraseñas"
    MyPasswordsController --> SyncHandler : "sincroniza datos"
    PasswordInfoController --> PasswordManager : "actualiza contraseñas"
    PasswordInfoController --> PasswordEvaluator : "analiza seguridad"
    
    %% Relaciones del servicio principal
    PasswordManager --> PasswordDAO : "persiste datos"
    PasswordManager --> SecurityFilterUtils : "analiza seguridad"
    PasswordManager --> PasswordDTO : "manipula entidades"
    
    %% Relaciones de acceso a datos
    PasswordDAO --> UserSession : "obtiene clave encriptación"
    PasswordDAO --> EncryptionUtil : "encripta/desencripta datos"
    PasswordDAO --> PasswordDTO : "mapea entidades"
    DDL --> PasswordDAO : "crea estructura BD"
    LocalAuthUtil --> EncryptionUtil : "encripta hash contraseña"
    
    %% Relaciones de sincronización
    SyncHandler --> PasswordsApiClient : "sincroniza contraseñas"
    SyncHandler --> UsersApiClient : "gestiona tokens usuario"
    SyncHandler --> PasswordManager : "coordina datos locales"
    SyncHandler --> DeletedPasswordsDAO : "rastrea eliminaciones"
    SyncHandler --> UserSession : "obtiene tokens autenticación"
    
    %% Relaciones de sesión
    UserSession --> PersistentSessionManager : "persiste sesión"
    PersistentSessionManager --> EncryptionUtil : "encripta datos sesión"
    
    %% Relaciones de utilidades de seguridad
    SecurityFilterUtils --> PasswordEvaluator : "evalúa fortaleza"
    SecurityFilterUtils --> CompromisedPasswordChecker : "verifica brechas seguridad"
    SecurityFilterUtils --> UnsafeUrlChecker : "valida URLs seguras"
    SecurityFilterUtils --> Trie : "busca palabras comunes"
    PassworldController --> PasswordGenerator : "genera contraseñas aleatorias"
    PasswordEvaluator --> Trie : "busca patrones comunes"
    
    %% Relaciones de gestión UI
    ViewManager --> ThemeManager : "aplica temas visuales"
    ViewManager --> LanguageUtil : "aplica idiomas"
    ThemeManager --> ViewManager : "notifica cambios tema"
    SyncHandler --> TimeSyncManager : "sincroniza tiempo UTC"
    
    %% Relaciones de interfaz y experiencia usuario
    AuthController --> ThemeManager : "aplica tema actual"
    AuthController --> LanguageUtil : "obtiene textos localizados"
    AuthController --> DialogUtil : "muestra mensajes usuario"
    AuthController --> HeaderConfigurator : "configura cabecera vista"
    AuthController --> Accessibility : "mejora accesibilidad"
    AuthController --> AnimationUtil : "aplica animaciones feedback"
    PassworldController --> DialogUtil : "muestra confirmaciones"
    PassworldController --> Notifier : "notifica acciones realizadas"
    PassworldController --> Accessibility : "habilita atajos teclado"
    MyPasswordsController --> DialogUtil : "confirma eliminaciones"
    MyPasswordsController --> Notifier : "notifica sincronización"
    PasswordInfoController --> DialogUtil : "valida datos entrada"
    PasswordInfoController --> Notifier : "confirma guardado exitoso"
    PasswordInfoController --> Accessibility : "facilita navegación"
    
    %% Relaciones de logging y monitoreo
    CompromisedPasswordChecker --> LogUtils : "registra verificaciones API"
    UnsafeUrlChecker --> LogUtils : "registra consultas seguridad"
    TimeSyncManager --> LogUtils : "registra sincronización tiempo"
    HeaderConfigurator --> LogUtils : "registra errores configuración"
```

---

## 6. Diagrama de Base de Datos

```mermaid
erDiagram
    PASSWORDS {
        INTEGER id PK "PRIMARY KEY AUTOINCREMENT"
        TEXT description "NOT NULL - Descripción de la contraseña"
        TEXT username "Nombre de usuario asociado"
        TEXT url "URL del sitio web"
        TEXT password "NOT NULL - Contraseña encriptada"
        BOOLEAN isWeak "DEFAULT 0 - Indica si es débil"
        BOOLEAN isDuplicate "DEFAULT 0 - Indica si está duplicada"
        BOOLEAN isCompromised "DEFAULT 0 - Indica si está comprometida"
        BOOLEAN isUrlUnsafe "DEFAULT 0 - Indica si la URL es insegura"
        TEXT lastModified "Fecha de última modificación"
        BOOLEAN isSynced "DEFAULT 0 - Indica si está sincronizada"
        TEXT idFb "ID de Firebase para sincronización"
    }
    
    MASTER_PASSWORD {
        INTEGER id PK "PRIMARY KEY AUTOINCREMENT"
        TEXT password_hash "NOT NULL - Hash de la contraseña maestra"
    }
    
    DELETED_PASSWORDS {
        INTEGER id PK "PRIMARY KEY AUTOINCREMENT"
        TEXT idFb UK "NOT NULL UNIQUE - ID de Firebase de contraseña eliminada"
        TEXT deletedAt "NOT NULL - Fecha de eliminación"
    }
    
    PASSWORDS ||--o{ DELETED_PASSWORDS : "puede ser eliminada"
```

---

## 7. Diagrama de Flujo - Proceso de Encriptación/Desencriptación

```mermaid
flowchart TD
    A[Ingreso de Contraseña Maestra] --> B[Derivar Clave AES con PBKDF2]
    B --> C[Almacenar Clave en UserSession]
    
    C --> D{¿Operación?}
    D -->|Encriptar| E[Obtener Datos Sensibles]
    D -->|Desencriptar| F[Obtener Datos Encriptados]
    
    E --> G[Convertir a Bytes UTF-8]
    G --> H[Aplicar AES/CBC/PKCS5Padding]
    H --> I[Generar IV Aleatorio]
    I --> J[Concatenar IV + Datos Encriptados]
    J --> K[Codificar en Base64]
    K --> L[Almacenar en Base de Datos]
    
    F --> M[Decodificar Base64]
    M --> N[Extraer IV de los primeros 16 bytes]
    N --> O[Extraer Datos Encriptados]
    O --> P[Aplicar AES/CBC para Desencriptar]
    P --> Q[Convertir Bytes a String UTF-8]
    Q --> R[Mostrar Datos en Interfaz]
    
    L --> S[Datos Protegidos]
    R --> T[Datos Accesibles]
```

---

## 8. Diagrama de Arquitectura del Sistema

```mermaid
graph TB
    subgraph "Capa de Presentación"
        A[SplashScreen]
        B[AuthController]
        C[VaultProtectionController]
        D[PassworldController]
        E[MyPasswordsController]
        F[PasswordInfoController]
    end
    
    subgraph "Capa de Servicios"
        G[PasswordManager]
        H[SyncHandler]
        I[SecurityFilterUtils]
        J[PasswordEvaluator]
        K[EncryptionUtil]
    end
    
    subgraph "Capa de Datos"
        L[PasswordDAO]
        M[LocalAuthUtil]
        N[UserSession]
        O[PersistentSessionManager]
    end
    
    subgraph "Almacenamiento"
        P[(SQLite Local)]
        Q[Firebase Realtime Database]
        R[Archivos de Configuración]
    end
    
    subgraph "APIs Externas"
        S[Firebase Authentication]
        T[HaveIBeenPwned API]
        U[Servidor de Tiempo UTC]
    end
    
    A --> B
    B --> C
    C --> D
    D --> E
    E --> F
    
    D --> G
    E --> G
    F --> G
    G --> L
    
    E --> H
    H --> G
    H --> S
    H --> Q
    
    G --> I
    F --> J
    D --> J
    
    L --> K
    G --> K
    F --> K
    
    L --> N
    M --> N
    O --> N
    
    L --> P
    M --> P
    O --> R
    
    H --> Q
    B --> S
    I --> T
    H --> U
    
    style A fill:#e1f5fe
    style B fill:#e1f5fe
    style C fill:#e1f5fe
    style D fill:#e1f5fe
    style E fill:#e1f5fe
    style F fill:#e1f5fe
    
    style G fill:#e8f5e8
    style H fill:#e8f5e8
    style I fill:#e8f5e8
    style J fill:#e8f5e8
    style K fill:#e8f5e8
    
    style L fill:#fff3e0
    style M fill:#fff3e0
    style N fill:#fff3e0
    style O fill:#fff3e0
    
    style P fill:#f3e5f5
    style Q fill:#f3e5f5
    style R fill:#f3e5f5
    
    style S fill:#ffebee
    style T fill:#ffebee
    style U fill:#ffebee
```

---

## 9. Diagrama de Estados - Ciclo de Vida de una Contraseña

```mermaid
stateDiagram-v2
    [*] --> Creando : Usuario ingresa nueva contraseña
    
    Creando --> Validando : Validar datos obligatorios
    Validando --> Creando : Datos inválidos
    Validando --> Analizando : Datos válidos
    
    Analizando --> Encriptando : Análisis de seguridad completo
    Encriptando --> Almacenada : Encriptación y guardado exitoso
    
    Almacenada --> PendienteSincronizacion : Guardada localmente
    PendienteSincronizacion --> Sincronizada : Sincronización exitosa
    PendienteSincronizacion --> PendienteSincronizacion : Error de conectividad
    
    Sincronizada --> Editando : Usuario modifica
    Editando --> Validando : Guardar cambios
    
    Sincronizada --> Eliminando : Usuario solicita eliminar
    Almacenada --> Eliminando : Usuario solicita eliminar
    
    Eliminando --> ConfirmandoEliminacion : Mostrar confirmación
    ConfirmandoEliminacion --> Sincronizada : Cancelar
    ConfirmandoEliminacion --> Almacenada : Cancelar
    ConfirmandoEliminacion --> EliminadaLocal : Confirmar
    
    EliminadaLocal --> Eliminada : Sincronización completa
    EliminadaLocal --> EliminadaLocal : Error de conectividad
    
    Eliminada --> [*] : Eliminación completa
    
    note right of Analizando
        - Verificar fortaleza (PasswordEvaluator)
        - Buscar duplicados en vault
        - Verificar si está comprometida (API)
        - Validar seguridad de URL
    end note
    
    note right of EliminadaLocal
        - Eliminada de base de datos local
        - Registrada en DeletedPasswordsDAO
        - Pendiente eliminación en Firebase
        - SyncHandler.syncPasswords() procesa eliminación
    end note
```

---

## 10. Resumen de Componentes Principales

### Controladores de Vista
- **AuthController**: Maneja registro y autenticación de usuarios
- **VaultProtectionController**: Gestiona el desbloqueo con contraseña maestra
- **PassworldController**: Vista principal para generar contraseñas
- **MyPasswordsController**: Lista y gestión de contraseñas guardadas
- **PasswordInfoController**: Detalles y edición de contraseñas individuales

### Servicios de Negocio
- **PasswordManager**: Lógica principal de gestión de contraseñas
- **SyncHandler**: Sincronización con Firebase
- **SecurityFilterUtils**: Análisis de seguridad de contraseñas
- **PasswordEvaluator**: Evaluación de fortaleza de contraseñas

### Acceso a Datos
- **PasswordDAO**: Operaciones CRUD en base de datos local
- **UserSession**: Gestión de sesión y claves de encriptación
- **PersistentSessionManager**: Persistencia de tokens de autenticación

### Utilidades
- **EncryptionUtil**: Funciones de encriptación/desencriptación
- **ViewManager**: Gestión de navegación entre vistas
- **ThemeManager**: Gestión de temas claro/oscuro
- **LanguageUtil**: Soporte de internacionalización

Esta documentación proporciona una visión completa de la arquitectura y flujos principales del sistema Passworld, facilitando el entendimiento y mantenimiento del código.
