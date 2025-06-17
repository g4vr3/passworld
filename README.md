# 🔐 Passworld Desktop

[![Passworld Web](https://img.shields.io/badge/Passworld-Web-blue?style=for-the-badge&logo=github)](https://github.com/g4vr3/passworld-web)
[![Passworld Web Extension](https://img.shields.io/badge/Passworld-Web%20Extension-green?style=for-the-badge&logo=github)](https://github.com/g4vr3/passworld-web-extension)
[![Passworld Android](https://img.shields.io/badge/Passworld-Android-orange?style=for-the-badge&logo=github)](https://github.com/jagudo27/passworld-android)

Un gestor de contraseñas moderno y seguro construido con JavaFX que prioriza la seguridad, sincronización bidireccional y análisis automático de vulnerabilidades.

## ✨ Características Principales

### 🔒 Seguridad Avanzada
- **Cifrado AES-256-CBC**: Todas las contraseñas se cifran con PKCS5Padding
- **Derivación de claves PBKDF2-SHA256**: 10,000 iteraciones con salt único
- **Vector de inicialización aleatorio**: IV único para cada operación de cifrado
- **Contraseña maestra**: Protección local de tu bóveda de contraseñas
- **Zero-knowledge**: Los datos se cifran localmente antes de la sincronización

### 🔄 Sincronización Bidireccional
- **Tiempo real**: Sincronización automática cada 7 segundos
- **Resolución de conflictos**: Basada en timestamps para determinar la versión más reciente
- **Offline-first**: Funcionalidad completa sin conexión a internet
- **Firebase Realtime Database**: Backend seguro y escalable
- **Tolerancia a fallos**: Reintentos automáticos en caso de errores de conectividad

### 🛡️ Análisis de Seguridad Automático
- **Evaluación de fortaleza**: Análisis en tiempo real de la robustez de contraseñas
- **Detección de compromiso**: Integración con Have I Been Pwned API usando k-anonymity
- **Identificación de duplicados**: Detección automática de contraseñas duplicadas
- **Validación de URLs**: Verificación de seguridad usando Google Safe Browsing API
- **Análisis periódico**: Evaluación continua de vulnerabilidades cada 7 segundos

### 🌐 Multiplataforma
- **Soporte completo**: Windows, macOS y Linux
- **Interfaz nativa**: JavaFX para una experiencia de usuario fluida
- **Persistencia local**: Base de datos SQLite local cifrada
- **Gestión de sesiones**: Tokens encriptados con persistencia automática

### 🎨 Experiencia de Usuario
- **Interfaz moderna**: Diseño intuitivo y responsive
- **Multiidioma**: Soporte para español, inglés y alemán
- **Temas personalizables**: Tema claro y oscuro
- **Generador de contraseñas**: Herramienta integrada con opciones avanzadas
- **Búsqueda instantánea**: Filtros y búsqueda en tiempo real
- **Notificaciones visuales**: Retroalimentación inmediata para todas las acciones

## 🚀 Tecnologías Utilizadas

### Core Framework
- **JavaFX 23.0.1**: Framework de interfaz de usuario moderna
- **Java 23**: Lenguaje de programación principal
- **Maven**: Gestión de dependencias y construcción del proyecto

### Base de Datos y Persistencia
- **SQLite**: Base de datos local embebida
- **Firebase Realtime Database**: Sincronización en la nube
- **Firebase Authentication**: Gestión de usuarios y autenticación

### Seguridad y Cifrado
- **Java Cryptography Architecture (JCA)**: Implementación de cifrado
- **Apache Commons Net**: Sincronización de tiempo NTP
- **PBKDF2WithHmacSHA256**: Derivación segura de claves

### APIs Externas
- **Have I Been Pwned API**: Verificación de contraseñas comprometidas
- **Google Safe Browsing API**: Validación de seguridad de URLs
- **NTP Servers**: Sincronización horaria precisa

### Testing
- **JUnit 5**: Framework de testing unitario
- **Mockito**: Framework de mocking para pruebas

## 🔧 Requisitos del Sistema

### Mínimos
- **Java Runtime Environment (JRE) 17** o superior
- **2 GB RAM** mínimo
- **200 MB** de espacio libre en disco
- **Conexión a internet** (opcional)

### Recomendados
- **Java 21** o superior
- **4 GB RAM** o más
- **1 GB** de espacio libre en disco
- **Conexión estable a internet** para sincronización

### Sistemas Operativos Soportados
- **Windows 10/11** (x64)
- **macOS 10.15** (Catalina) o superior
- **Linux** (distribuciones principales con soporte para JavaFX)

## 🎯 Uso Básico

### Primer Uso

1. **Registro/Inicio de Sesión**
   - Crear cuenta con email y contraseña
   - Establecer contraseña maestra para proteger tu bóveda local

2. **Gestión de Contraseñas**
   - Crear nuevas contraseñas con descripción, usuario, URL y contraseña
   - Usar el generador integrado para contraseñas seguras
   - Visualizar y editar contraseñas existentes

3. **Monitoreo de Seguridad**
   - Revisar el dashboard de seguridad para contraseñas vulnerables
   - Actualizar contraseñas débiles, duplicadas o comprometidas
   - Verificar URLs potencialmente peligrosas

### Características Avanzadas

#### Generador de Contraseñas
- Longitud personalizable
- Inclusión/exclusión de tipos de caracteres
- Evaluación de fortaleza en tiempo real
- Copia automática al portapapeles

#### Análisis de Seguridad
- **Contraseñas débiles**: Identificación automática basada en patrones
- **Contraseñas duplicadas**: Detección de reutilización
- **Contraseñas comprometidas**: Verificación contra bases de datos de filtraciones
- **URLs inseguras**: Validación de dominios maliciosos

#### Sincronización
- **Automática**: Cada 7 segundos en segundo plano
- **Manual**: Botón de sincronización forzada
- **Estado visual**: Indicadores de estado de sincronización
- **Resolución de conflictos**: Automática basada en timestamps

## 🔐 Arquitectura de Seguridad

### Flujo de Cifrado
1. **Entrada de contraseña maestra** → Verificación de hash PBKDF2
2. **Derivación de clave AES** → PBKDF2-SHA256 con 10,000 iteraciones
3. **Cifrado de datos** → AES-256-CBC con IV aleatorio
4. **Almacenamiento** → Base64 en SQLite local
5. **Sincronización** → Datos ya cifrados hacia Firebase

### Gestión de Claves
- **Contraseña maestra**: Solo conocida por el usuario
- **Hash de verificación**: PBKDF2 almacenado localmente
- **Clave de cifrado**: Derivada en memoria, nunca persistida
- **IV aleatorio**: Generado para cada operación de cifrado

## 🌍 Internacionalización

### Idiomas Soportados
- **Español (es)**: Idioma principal
- **Inglés (en)**: Traducción completa
- **Alemán (de)**: Traducción completa

### Agregar Nuevos Idiomas
1. Crear archivo `lang_[código].properties` en `src/main/resources/passworld/resource_bundle/`
2. Traducir todas las claves existentes
3. Agregar el idioma al selector en `LanguageUtil.java`

### Logs de Depuración y Auditoría
Los logs se almacenan en:
- **Windows**: `%APPDATA%/passworld/logs/`
- **macOS**: `~/Library/Application Support/passworld/logs/`
- **Linux**: `~/.local/share/passworld/logs/`

## 👥 Autores

- [@g4vr3](https://github.com/g4vr3)
- [@jagudo27](https://github.com/jagudo27)