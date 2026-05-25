# API y guía de pruebas - Dashboard principal de monitoreo

Este documento resume los cambios realizados para la historia de usuario del dashboard en tiempo real y explica cómo probarlos desde Postman.

---

## 1. Qué se implementó

### Nuevo módulo `dashboard`
Se agregó el módulo `modules/dashboard` con estos componentes:

- `DashboardController`
- `DashboardService`
- `DashboardServiceImpl`
- DTOs de respuesta para zonas y lecturas
- `ReadingStatus` en `shared/enums`

### Endpoints involucrados
- `GET /api/readings/latest`
- `GET /api/readings/latest?zoneId=2`
- `GET /api/zones`
- `GET /api/zones?isActive=false`

### Ajuste adicional
`GET /api/zones` ahora usa `isActive=true` por defecto cuando el frontend no envía el query param.

---

## 2. Reglas de negocio aplicadas

### Relación zona ↔ lecturas
El documento Mongo `sensor_readings` no tiene `zone_id`. La relación se resuelve por:

- `sensor_readings.nodeName = zones.name`

### Tipo real de `zoneId`
En el backend la zona usa `Long`, no UUID.

### Envelope de respuesta
El endpoint nuevo responde con el formato estándar del backend:

```json
{
  "success": true,
  "status": 200,
  "message": "Lecturas recientes retornadas correctamente",
  "data": { ... },
  "errors": null,
  "timestamp": "2026-05-24T12:00:00"
}
```

### Estado de lectura
La lógica aplicada en el backend es:

- `CRITICAL`: valor fuera del rango `[minValue, maxValue]`
- `WARNING`: valor exactamente en el mínimo o en el máximo
- `NORMAL`: valor dentro del rango
- `UNKNOWN`: no existe umbral o el valor no se pudo convertir a número

### Estado de conexión
- `online = true` si la última lectura de la zona tiene menos de 2 minutos
- `online = false` si la última lectura es más antigua

---

## 3. Estructura de la respuesta

`GET /api/readings/latest` retorna un objeto como este dentro de `data`:

```json
{
  "generatedAt": "2026-05-24T12:00:00Z",
  "zones": [
    {
      "zoneId": 1,
      "zoneName": "Zona A",
      "description": "Sector de prueba",
      "isActive": true,
      "online": true,
      "lastReadingAt": "2026-05-24T11:59:30Z",
      "readings": [
        {
          "variable": "TEMPERATURE",
          "value": "24.5",
          "unit": "C",
          "status": "NORMAL",
          "timestamp": "2026-05-24T11:59:30Z",
          "online": true
        }
      ]
    }
  ]
}
```

---

## 4. Endpoint `GET /api/readings/latest`

### Descripción
Devuelve las lecturas más recientes consolidadas por zona y por variable.

### Seguridad
- Requiere JWT válido.
- No requiere rol `ADMIN`.
- Si falta el token, responde `401 Unauthorized`.

### Query params
- `zoneId` opcional
- Tipo real: `Long`

### Ejemplos

```http
GET {{base_url}}/api/readings/latest
Authorization: Bearer {{token}}
Accept: application/json
```

```http
GET {{base_url}}/api/readings/latest?zoneId=2
Authorization: Bearer {{token}}
Accept: application/json
```

### Comportamiento
- Si no se envía `zoneId`, el backend toma todas las zonas activas.
- Si se envía `zoneId`, el backend devuelve solo esa zona si existe y está activa.
- Si la zona no existe o está inactiva, responde error.
- Si una variable no tiene umbral configurado, la lectura se devuelve con `status: UNKNOWN`.
- Si una lectura no tiene valor numérico, también se marca como `UNKNOWN`.

---

## 5. Endpoint `GET /api/zones`

### Cambio aplicado
Ahora `isActive` tiene valor por defecto `true`.

### Ejemplos

```http
GET {{base_url}}/api/zones
Authorization: Bearer {{token}}
Accept: application/json
```

Equivale a:

```http
GET {{base_url}}/api/zones?isActive=true
Authorization: Bearer {{token}}
Accept: application/json
```

También sigue funcionando:

```http
GET {{base_url}}/api/zones?isActive=false
Authorization: Bearer {{token}}
Accept: application/json
```

---

## 6. Configuración recomendada en Postman

Crea un environment llamado `Invernadero-Dev` con estas variables:

```text
base_url = http://localhost:8080
token =
zone_id =
```

### Headers comunes

Para endpoints protegidos:

```text
Authorization: Bearer {{token}}
Accept: application/json
```

Para `POST`, `PUT` y `PATCH` con JSON:

```text
Content-Type: application/json
Authorization: Bearer {{token}}
```

Para `GET` no es necesario `Content-Type`.

---

## 7. Flujo de prueba sugerido en Postman

### Paso 1: Autenticarse
Obtén un JWT usando el endpoint de login del backend.

### Paso 2: Crear o verificar zonas
Si necesitas una zona de prueba, crea una por `POST /api/zones`.

### Paso 3: Crear umbrales
Crea umbrales para la zona usando `POST /api/thresholds`.

### Paso 4: Enviar lecturas IoT
Las lecturas deben guardarse en Mongo por el flujo normal del sistema IoT.

### Paso 5: Consultar el dashboard
Ejecuta:

```http
GET {{base_url}}/api/readings/latest
Authorization: Bearer {{token}}
Accept: application/json
```

O filtrando por zona:

```http
GET {{base_url}}/api/readings/latest?zoneId={{zone_id}}
Authorization: Bearer {{token}}
Accept: application/json
```

### Paso 6: Validar `GET /api/zones`
Ejecuta:

```http
GET {{base_url}}/api/zones
Authorization: Bearer {{token}}
Accept: application/json
```

Y confirma que devuelve solo zonas activas por defecto.

---

## 8. Casos que debes verificar

### Caso 1: zona activa con lecturas recientes
Debe responder `online: true`.

### Caso 2: zona activa sin lecturas recientes
Debe responder `online: false` y `readings: []` si no hay datos disponibles.

### Caso 3: lectura dentro de rango
Debe responder `status: NORMAL`.

### Caso 4: lectura en el borde del rango
Debe responder `status: WARNING`.

### Caso 5: lectura fuera de rango
Debe responder `status: CRITICAL`.

### Caso 6: lectura sin umbral
Debe responder `status: UNKNOWN`.

---

## 9. Prueba rápida en Postman

### Request

```http
GET {{base_url}}/api/readings/latest
Authorization: Bearer {{token}}
Accept: application/json
```

### Respuesta esperada
- `200 OK`
- `success = true`
- `data.zones` con una lista de zonas activas
- Cada zona con su estado `online`
- Cada lectura con `variable`, `value`, `unit`, `status`, `timestamp` y `online`

---

## 10. Archivos modificados

### Nuevos
- `src/main/java/com/greenhouse/smart_backend/modules/dashboard/...`
- `src/main/java/com/greenhouse/smart_backend/shared/enums/ReadingStatus.java`
- `docs/DASHBOARD_MONITOREO_API.md`

### Modificados
- `src/main/java/com/greenhouse/smart_backend/modules/zones/controller/ZoneController.java`
- `src/main/java/com/greenhouse/smart_backend/modules/iot/repository/SensorReadingMongoRepository.java`

---

## 11. Observaciones técnicas

- El dashboard usa `Long` para `zoneId`.
- Mongo se asocia a PostgreSQL mediante `nodeName = zone.name`.
- Si el frontend cambia el nombre de la zona, también debe considerar ese efecto en el mapeo de lecturas.
- La lógica actual prioriza consistencia con el proyecto antes que inventar una nueva estructura de respuesta.

---

## 12. Siguiente paso sugerido

Si el equipo quiere una experiencia más precisa, el siguiente ajuste natural sería agregar un historial por variable con series temporales y/o enriquecer el estado `WARNING` con una banda de alerta configurable.

