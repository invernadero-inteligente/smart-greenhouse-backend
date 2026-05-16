# MODULO THRESHOLDS - GUIA COMPLETA

Esta guia explica como probar el modulo de umbrales desde Postman. La regla para las pruebas es simple: no se ingresa a la base de datos ni se usa SQL para preparar datos. Todo se crea y valida por API, igual que lo haria el frontend.

## Indice

1. [Resumen rapido](#resumen-rapido)
2. [Que es Thresholds](#que-es-thresholds)
3. [Variables disponibles](#variables-disponibles)
4. [Endpoints](#endpoints)
5. [Guia Postman paso a paso](#guia-postman-paso-a-paso)
6. [Ejemplos curl](#ejemplos-curl)
7. [Casos de error](#casos-de-error)
8. [Arquitectura tecnica](#arquitectura-tecnica)
9. [FAQ](#faq)

---

## Resumen rapido

Flujo logico para probar:

1. Hacer login como ADMIN.
2. Crear una zona por API.
3. Crear los umbrales necesarios para esa zona por API.
4. Listar los umbrales.
5. Actualizar un umbral.
6. Consultar de nuevo para verificar el cambio.

Endpoints principales:

| Metodo | URL | Descripcion |
|--------|-----|-------------|
| `POST` | `/api/thresholds` | Crear umbral |
| `GET` | `/api/thresholds?zoneId=1` | Consultar umbrales por zona |
| `PUT` | `/api/thresholds/{id}` | Actualizar minimo y maximo |

Seguridad:
- Requiere JWT.
- Crear, consultar y actualizar umbrales requiere rol `ADMIN`.
- Sin token responde `401`.
- Sin rol ADMIN responde `403`.

---

## Que es Thresholds

El modulo **Thresholds** permite configurar rangos minimos y maximos para variables del invernadero en una zona especifica.

Ejemplo:

```text
Zona 1
- TEMPERATURE: 18.0 - 30.0 C
- AIR_HUMIDITY: 40.0 - 70.0 %
- PH: 5.5 - 7.0 pH
```

Estos rangos pueden ser usados por otros modulos para generar alertas cuando una lectura queda por debajo del minimo o por encima del maximo.

Los thresholds dependen de `Zone`, no de `Crop`. Para probar este modulo no necesitas crear cultivos.

---

## Variables disponibles

El enum real del backend es `SensorVariable`. Valores permitidos:

```text
TEMPERATURE
AIR_HUMIDITY
SOIL_MOISTURE
PH
WATER_FLOW
LUMINOSITY
WATER_LEVEL
```

Usa estos nombres exactamente en:
- `variableName` al crear un threshold.
- `variables` al consultar con filtro.

---

## Endpoints

### POST /api/thresholds - Crear umbral

Crea un umbral para una zona y una variable.

Request:

```http
POST /api/thresholds
Authorization: Bearer <TOKEN>
Content-Type: application/json
```

Body:

```json
{
  "zoneId": 1,
  "variableName": "TEMPERATURE",
  "unit": "C",
  "minValue": 18.0,
  "maxValue": 30.0
}
```

Respuesta `201 Created`:

```json
{
  "data": {
    "id": 1,
    "name": "TEMPERATURE",
    "unit": "C",
    "minValue": 18.0,
    "maxValue": 30.0,
    "updatedAt": "2026-05-15T10:30:45"
  }
}
```

Validaciones:
- `zoneId` debe existir.
- `variableName` debe existir en `SensorVariable`.
- `unit` es obligatorio.
- `minValue` y `maxValue` son obligatorios.
- `minValue` debe ser menor que `maxValue`.
- No puede existir otro umbral con la misma zona y la misma variable.

### GET /api/thresholds - Consultar umbrales

Retorna los umbrales configurados para una o mas zonas.

Request:

```http
GET /api/thresholds?zoneId=1
Authorization: Bearer <TOKEN>
```

Con filtros:

```http
GET /api/thresholds?zoneId=1&zoneId=2&variables=TEMPERATURE&variables=PH
Authorization: Bearer <TOKEN>
```

Respuesta `200 OK`:

```json
{
  "data": [
    {
      "zoneId": 1,
      "variables": [
        {
          "id": 1,
          "name": "TEMPERATURE",
          "unit": "C",
          "minValue": 18.0,
          "maxValue": 30.0,
          "updatedAt": "2026-05-15T10:30:45"
        }
      ]
    }
  ]
}
```

Parametros:

| Parametro | Requerido | Descripcion |
|-----------|-----------|-------------|
| `zoneId` | Si | Uno o mas IDs de zona. Se repite: `zoneId=1&zoneId=2` |
| `variables` | No | Una o mas variables. Se repite: `variables=TEMPERATURE&variables=PH` |

### PUT /api/thresholds/{id} - Actualizar umbral

Actualiza solo `minValue` y `maxValue`.

Request:

```http
PUT /api/thresholds/1
Authorization: Bearer <TOKEN>
Content-Type: application/json
```

Body:

```json
{
  "minValue": 20.0,
  "maxValue": 32.0
}
```

Respuesta esperada: `204 No Content`.

---

## Guia Postman paso a paso

### Paso 1: Crear environment

Crea un environment llamado `Invernadero-Dev`:

```text
base_url = http://localhost:8080
admin_email = admin1@invernadero.com
admin_password = Admin123456
admin_token =
zone_id =
temperature_threshold_id =
humidity_threshold_id =
ph_threshold_id =
```

### Paso 2: Login ADMIN

Request:

```http
POST {{base_url}}/api/auth/login
Content-Type: application/json
```

Body:

```json
{
  "email": "{{admin_email}}",
  "password": "{{admin_password}}"
}
```

Guarda el token de la respuesta en `admin_token`.

Headers para las siguientes requests:

```text
Authorization: Bearer {{admin_token}}
Content-Type: application/json
```

### Paso 3: Crear zona

Request:

```http
POST {{base_url}}/api/zones
Authorization: Bearer {{admin_token}}
Content-Type: application/json
```

Body:

```json
{
  "name": "Zona Postman 1",
  "description": "Zona creada desde Postman para probar thresholds"
}
```

Guarda el `id` de la respuesta en `zone_id`.

### Paso 4: Crear threshold de temperatura

Request:

```http
POST {{base_url}}/api/thresholds
Authorization: Bearer {{admin_token}}
Content-Type: application/json
```

Body:

```json
{
  "zoneId": {{zone_id}},
  "variableName": "TEMPERATURE",
  "unit": "C",
  "minValue": 18.0,
  "maxValue": 30.0
}
```

Guarda `data.id` en `temperature_threshold_id`.

### Paso 5: Crear threshold de humedad

Request:

```http
POST {{base_url}}/api/thresholds
Authorization: Bearer {{admin_token}}
Content-Type: application/json
```

Body:

```json
{
  "zoneId": {{zone_id}},
  "variableName": "AIR_HUMIDITY",
  "unit": "%",
  "minValue": 40.0,
  "maxValue": 70.0
}
```

Guarda `data.id` en `humidity_threshold_id`.

### Paso 6: Crear threshold de pH

Request:

```http
POST {{base_url}}/api/thresholds
Authorization: Bearer {{admin_token}}
Content-Type: application/json
```

Body:

```json
{
  "zoneId": {{zone_id}},
  "variableName": "PH",
  "unit": "pH",
  "minValue": 5.5,
  "maxValue": 7.0
}
```

Guarda `data.id` en `ph_threshold_id`.

### Paso 7: Listar todos los thresholds de la zona

Request:

```http
GET {{base_url}}/api/thresholds?zoneId={{zone_id}}
Authorization: Bearer {{admin_token}}
```

Debe responder `200 OK` y listar `TEMPERATURE`, `AIR_HUMIDITY` y `PH`.

### Paso 8: Consultar solo temperatura

Request:

```http
GET {{base_url}}/api/thresholds?zoneId={{zone_id}}&variables=TEMPERATURE
Authorization: Bearer {{admin_token}}
```

### Paso 9: Actualizar temperatura

Request:

```http
PUT {{base_url}}/api/thresholds/{{temperature_threshold_id}}
Authorization: Bearer {{admin_token}}
Content-Type: application/json
```

Body:

```json
{
  "minValue": 20.0,
  "maxValue": 32.0
}
```

Debe responder `204 No Content`.

### Paso 10: Verificar cambio

Request:

```http
GET {{base_url}}/api/thresholds?zoneId={{zone_id}}&variables=TEMPERATURE
Authorization: Bearer {{admin_token}}
```

Verifica que el rango sea `20.0 - 32.0`.

---

## Ejemplos curl

```bash
BASE_URL="http://localhost:8080"

# 1. Login
curl -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin1@invernadero.com","password":"Admin123456"}'

# 2. Crear zona
curl -X POST "$BASE_URL/api/zones" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Zona Postman 1","description":"Zona creada desde API"}'

# 3. Crear temperatura
curl -X POST "$BASE_URL/api/thresholds" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"zoneId":1,"variableName":"TEMPERATURE","unit":"C","minValue":18.0,"maxValue":30.0}'

# 4. Crear humedad
curl -X POST "$BASE_URL/api/thresholds" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"zoneId":1,"variableName":"AIR_HUMIDITY","unit":"%","minValue":40.0,"maxValue":70.0}'

# 5. Listar
curl -X GET "$BASE_URL/api/thresholds?zoneId=1" \
  -H "Authorization: Bearer <TOKEN>"

# 6. Actualizar temperatura
curl -X PUT "$BASE_URL/api/thresholds/1" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"minValue":20.0,"maxValue":32.0}'
```

---

## Casos de error

### 400 - zoneId faltante al consultar

```http
GET /api/thresholds
```

Solucion:

```http
GET /api/thresholds?zoneId=1
```

### 400 - rango invalido

Body incorrecto:

```json
{
  "minValue": 35.0,
  "maxValue": 20.0
}
```

Solucion:

```json
{
  "minValue": 20.0,
  "maxValue": 35.0
}
```

### 400 - variable invalida

Body incorrecto:

```json
{
  "zoneId": 1,
  "variableName": "HUMIDITY",
  "unit": "%",
  "minValue": 40.0,
  "maxValue": 70.0
}
```

Solucion: usa `AIR_HUMIDITY`.

### 400 - umbral duplicado

Ocurre si intentas crear dos veces la misma variable en la misma zona.

Solucion: usa `GET /api/thresholds?zoneId={{zone_id}}` para obtener el `id` existente y luego `PUT /api/thresholds/{id}`.

### 401 - sin token

Solucion: agrega:

```text
Authorization: Bearer {{admin_token}}
```

### 403 - sin rol ADMIN

Solucion: usa un usuario con rol `ADMIN`.

### 404 - zona o threshold inexistente

Solucion:
- Para crear threshold, primero crea o consulta la zona.
- Para actualizar threshold, primero consulta `GET /api/thresholds?zoneId={{zone_id}}` y usa un `id` real.

---

## Arquitectura tecnica

Estructura principal:

```text
modules/thresholds/
  controller/
    ThresholdController.java
      POST /api/thresholds
      GET /api/thresholds
      PUT /api/thresholds/{id}
  dto/request/
    ThresholdCreateRequest.java
    ThresholdUpdateRequest.java
  dto/response/
    ThresholdVariableResponseDTO.java
    ThresholdZoneResponseDTO.java
    ThresholdsDataResponseDTO.java
  mapper/
    ThresholdsMapper.java
  model/
    ThresholdConfig.java
  repository/
    ThresholdConfigRepository.java
  service/
    ThresholdService.java
    impl/ThresholdServiceImpl.java
```

Flujo `POST /api/thresholds`:

```text
Cliente Postman
  -> ThresholdController.createThreshold()
  -> ThresholdServiceImpl.createThreshold()
  -> valida rango
  -> valida variable del enum SensorVariable
  -> valida que la zona exista
  -> valida que no exista duplicado zona + variable
  -> guarda ThresholdConfig
  -> responde DTO con id, name, unit, minValue, maxValue, updatedAt
```

Flujo `GET /api/thresholds`:

```text
Cliente Postman
  -> ThresholdController.listThresholds()
  -> ThresholdServiceImpl.listThresholds()
  -> consulta por zonas y variables opcionales
  -> agrupa por zoneId
  -> responde { data: [ { zoneId, variables: [...] } ] }
```

Flujo `PUT /api/thresholds/{id}`:

```text
Cliente Postman
  -> ThresholdController.updateThreshold()
  -> ThresholdServiceImpl.updateThreshold()
  -> valida minValue < maxValue
  -> busca el threshold por id
  -> actualiza minValue y maxValue
  -> updatedAt se actualiza automaticamente
  -> responde 204 No Content
```

---

## FAQ

**P: Puedo crear thresholds via API?**  
R: Si. Usa `POST /api/thresholds`.

**P: Debo usar SQL para probar en Postman?**  
R: No. Para pruebas funcionales crea datos por API: login, zona, thresholds, consulta y actualizacion.

**P: Que hago si ya existe el threshold?**  
R: Consulta los existentes con `GET /api/thresholds?zoneId={{zone_id}}` y actualiza con `PUT /api/thresholds/{id}`.

**P: Puedo mover un threshold a otra zona?**  
R: No por `PUT`. El `PUT` solo cambia `minValue` y `maxValue`.

**P: Que pasa si envio minValue = maxValue?**  
R: Responde `400 Bad Request`. Siempre debe cumplirse `minValue < maxValue`.

**P: Que variable uso para humedad?**  
R: Usa `AIR_HUMIDITY`, no `HUMIDITY`.

---

## Checklist

- [ ] Configure environment en Postman.
- [ ] Hice login y guarde `admin_token`.
- [ ] Cree zona por API y guarde `zone_id`.
- [ ] Cree thresholds por API.
- [ ] Liste thresholds por zona.
- [ ] Actualice un threshold con `PUT`.
- [ ] Verifique el cambio con `GET`.
- [ ] Probe errores esperados: sin token, rango invalido, variable invalida e ID inexistente.
