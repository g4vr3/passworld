# Aplicación Escritorio - Sincronización Horaria NTP

## Descripción
Este diagrama muestra el proceso de sincronización horaria mediante protocolo NTP (Network Time Protocol) en la aplicación de escritorio PassWorld. La sincronización temporal es crucial para mantener la coherencia en las operaciones de sincronización de contraseñas entre dispositivos.

## Propósito
- **Coherencia temporal**: Asegurar que todas las operaciones tengan timestamps precisos
- **Resolución de conflictos**: Determinar qué versión de datos es más reciente
- **Integridad de sincronización**: Evitar inconsistencias por diferencias horarias

```mermaid
flowchart TD
    A["Iniciar Sincronización"] --> B{"¿Tiempo UTC<br/>ya sincronizado?"}
    B -- Sí --> SUCCESS["Continuar con<br/>Sincronización Normal"]
    B -- No --> C["Obtener Lista de<br/>Servidores NTP"]
    
    C --> D["Servidor NTP Principal:<br/>pool.ntp.org"]
    D --> E["Enviar Solicitud NTP<br/>(UDP Puerto 123)"]
    E --> F{"¿Respuesta<br/>Recibida?"}
    
    F -- No --> G["Timeout 5 segundos"]
    G --> H["Intentar Servidor<br/>Alternativo"]
    H --> I["Servidores Backup:<br/>time.google.com<br/>time.cloudflare.com<br/>time.windows.com"]
    I --> J["Enviar Solicitud NTP<br/>a Servidor Backup"]
    J --> K{"¿Respuesta<br/>Recibida?"}
    
    K -- No --> L["Incrementar<br/>Contador Fallos"]
    L --> M{"¿Fallos < 3?"}
    M -- Sí --> H
    M -- No --> ERROR["Error: Sin Sincronización<br/>Usar Hora Local"]
    
    F -- Sí --> N["Procesar Respuesta NTP"]
    K -- Sí --> N
    
    N --> O["Extraer Timestamps:<br/>T1: Tiempo origen cliente<br/>T2: Tiempo recepción servidor<br/>T3: Tiempo transmisión servidor<br/>T4: Tiempo llegada cliente"]
    
    O --> P["Calcular Offset:<br/>offset = ((T2-T1) + (T3-T4))/2"]
    P --> Q["Calcular Delay:<br/>delay = (T4-T1) - (T3-T2)"]
    Q --> R{"¿Delay < 1 segundo?"}
    
    R -- No --> S["Respuesta no confiable<br/>Intentar otro servidor"]
    S --> H
    
    R -- Sí --> T["Aplicar Corrección:<br/>Tiempo_UTC = Tiempo_Local + offset"]
    T --> U["Validar Rango:<br/>¿Diferencia < 24 horas?"]
    
    U -- No --> V["Diferencia muy grande<br/>Posible error de red"]
    V --> H
    
    U -- Sí --> W["Actualizar Tiempo UTC<br/>del Sistema"]
    W --> X["Marcar como<br/>Sincronizado"]
    X --> Y["Registrar en Log:<br/>Servidor usado, offset, delay"]
    Y --> SUCCESS

    %% Estilos
    style A fill:#E3F2FD, color: #000000
    style ERROR fill:#FFEBEE, color: #000000, stroke:#F44336, stroke-width:2px
    style SUCCESS fill:#E8F5E8, color: #000000, stroke:#4CAF50, stroke-width:2px
    style N fill:#F3E5F5, color: #000000, stroke:#9C27B0, stroke-width:1px
    style T fill:#E0F2F1, color: #000000, stroke:#009688, stroke-width:1px

    %% Agrupación de servidores NTP
    subgraph "Servidores NTP"
        D
        I
    end
    
    %% Agrupación de cálculos
    subgraph "Procesamiento NTP"
        O
        P
        Q
        T
    end
    
    %% Agrupación de validaciones
    subgraph "Validaciones"
        R
        U
        M
    end
```

## Detalles Técnicos

### Servidores NTP Utilizados
1. **Principal**: `pool.ntp.org` - Pool global de servidores NTP
2. **Backup**:
   - `time.google.com` - Servidores de Google
   - `time.cloudflare.com` - Servidores de Cloudflare  
   - `time.windows.com` - Servidores de Microsoft

### Algoritmo de Sincronización
- **Protocolo**: NTP versión 4 (RFC 5905)
- **Puerto**: UDP 123
- **Timeout**: 5 segundos por solicitud
- **Reintentos**: Máximo 3 fallos antes de usar hora local

### Cálculos NTP
- **Offset**: `((T2-T1) + (T3-T4))/2` - Diferencia horaria con el servidor
- **Delay**: `(T4-T1) - (T3-T2)` - Tiempo de red ida y vuelta
- **Precisión**: Milisegundos

### Validaciones de Seguridad
- **Delay máximo**: 1 segundo (evita respuestas de red lenta)
- **Diferencia máxima**: 24 horas (detecta errores graves)
- **Múltiples fuentes**: Fallback a servidores alternativos

## Beneficios
- ✅ **Timestamps precisos** para operaciones de sincronización
- ✅ **Resolución de conflictos** basada en tiempo real
- ✅ **Redundancia** con múltiples servidores NTP
- ✅ **Tolerancia a fallos** con fallback a hora local
- ✅ **Validación robusta** contra ataques de red
