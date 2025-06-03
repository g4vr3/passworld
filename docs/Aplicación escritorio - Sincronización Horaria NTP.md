# Aplicación Escritorio - Sincronización Horaria NTP

## Descripción
Este diagrama muestra el proceso de sincronización horaria mediante protocolo NTP (Network Time Protocol) en la aplicación de escritorio PassWorld. La sincronización temporal es crucial para mantener la coherencia en las operaciones de sincronización de contraseñas entre dispositivos.

## Propósito
- **Coherencia temporal**: Asegurar que todas las operaciones tengan timestamps precisos  
- **Resolución de conflictos**: Determinar qué versión de datos es más reciente  
- **Integridad de sincronización**: Evitar inconsistencias por diferencias horarias

```mermaid
flowchart TD
    A["Iniciar Sincronización"] --> B["Obtener hora UTC<br/>del servidor NTP"]
    B --> C["Servidor NTP usado:<br/>time.google.com"]
    C --> D["Enviar Solicitud NTP<br/>(UDP Puerto 123)"]
    D --> E{"¿Respuesta recibida?"}
    
    E -- No --> F["Error: no se pudo sincronizar<br/>Se sigue con la hora local"]
    E -- Sí --> G["Obtener hora UTC<br/>desde el servidor"]
    G --> H["Comparar con<br/>hora UTC del sistema"]
    H --> I["Calcular Offset:<br/>Offset = (servidor - sistema)"]
    I --> J["Aplicar Offset<br/>a las fechas locales"]
    J --> K["Usar hora UTC<br/>corregida para sincronización"]
    K --> L["Registrar en Log:<br/>offset aplicado"]
    L --> M["Continuar con<br/>Sincronización Normal"]

    %% Estilos
    style A fill:#E3F2FD, color: #000000
    style F fill:#FFEBEE, color: #000000, stroke:#F44336, stroke-width:2px
    style M fill:#E8F5E8, color: #000000, stroke:#4CAF50, stroke-width:2px
    style I fill:#F3E5F5, color: #000000, stroke:#9C27B0, stroke-width:1px
    style J fill:#E0F2F1, color: #000000, stroke:#009688, stroke-width:1px
```

# Detalles Técnicos

## Servidor NTP Utilizado  
- `time.google.com` – Servidor principal (único usado actualmente)

## Algoritmo de Sincronización  
- **Protocolo:** NTP v4  
- **Puerto:** UDP 123  
- **Timeout:** 3 segundos por solicitud  
- **Offset:** Diferencia entre hora del sistema y hora UTC del servidor

## Qué hace el sistema  
- Usa Apache Commons Net (`NTPUDPClient`) para obtener la hora UTC desde un servidor NTP.  
- Calcula el offset entre la hora del sistema y la del servidor.  
- Aplica el offset para ajustar cualquier marca temporal antes de sincronizar.  
- En caso de error, se registra un warning y se sigue usando la hora local.

## Beneficios  
- ✅ Timestamps corregidos con hora UTC real  
- ✅ Resolución de conflictos más precisa  
- ✅ Registro en logs del offset aplicado  
- ✅ Tolerancia a fallos (sigue funcionando sin NTP)

