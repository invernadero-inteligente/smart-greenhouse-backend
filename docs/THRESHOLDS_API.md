# API y guia de pruebas - Modulo de Umbrales (Thresholds)

Este documento centraliza lo necesario para entender el modulo de umbrales y probarlo desde Postman. La prueba asume que no se entra directamente a la base de datos: los datos se preparan por API.

Contenido
- Resumen del modulo
- Requisitos previos
- Variables validas
- Flujo recomendado en Postman
- Endpoints disponibles y ejemplos
- Errores comunes

-----------------------------------------------------------

Resumen del modulo
-------------------
- Entidad principal: `ThresholdConfig` (tabla `threshold_configs`) vincula una zona (`Zone`) con una variable de sensor, unidad, minimo, maximo y auditoria (`updatedBy`, `updatedAt`).
- Proposito: definir los rangos operativos optimos por variable y zona.
- Ubicacion en el proyecto: `modules/thresholds`.
- Seguridad: endpoints protegidos por JWT. Crear, consultar y actualizar umbrales requiere rol `ADMIN`.
- La creacion inicial para pruebas se hace por API con `POST /api/thresholds`. No uses SQL para preparar datos en Postman.

Necesito crear cultivos antes?
------------------------------
No. Los umbrales estan atados a `Zone`, no a `Crop`. Para probar umbrales solo necesitas:
1) Aplicacion corriendo localmente, por ejemplo `http://localhost:8080`.
2) Migraciones ejecutadas para que las tablas existan.
3) Usuario ADMIN para autenticarse.
4) Una zona creada por API con `POST /api/zones`.
5) Umbrales creados por API con `POST /api/thresholds`.

Variables validas
-----------------
Usa exactamente estos nombres en `variableName` y en el filtro `variables`:

- `TEMPERATURE`
- `AIR_HUMIDITY`
- `SOIL_MOISTURE`
- `PH`
- `WATER_FLOW`
- `LUMINOSITY`
- `WATER_LEVEL`

Flujo recomendado en Postman
----------------------------
Configura un environment llamado `Invernadero-Dev` con estas variables:

```text
base_url = http://localhost:8080
admin_email = admin1@invernadero.com
admin_password = Admin123456
admin_token =
zone_id =
temperature_threshold_id =
humidity_threshold_id =
```

Headers comunes para requests protegidas:

```text
Authorization: Bearer {{admin_token}}
Content-Type: application/json
```

1) Login ADMIN
--------------

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

En la respuesta copia el campo `token` y guardalo en `admin_token`.

2) Crear zona de prueba
-----------------------

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

Respuesta esperada: `201 Created`. Guarda el `id` de la zona en `zone_id`.

3) Crear umbral de temperatura
------------------------------

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

Respuesta esperada:

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

Guarda `data.id` en `temperature_threshold_id`.

4) Crear umbral de humedad del aire
-----------------------------------

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

5) Listar umbrales de la zona
-----------------------------

Request:

```http
GET {{base_url}}/api/thresholds?zoneId={{zone_id}}
Authorization: Bearer {{admin_token}}
```

Respuesta esperada:

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
        },
        {
          "id": 2,
          "name": "AIR_HUMIDITY",
          "unit": "%",
          "minValue": 40.0,
          "maxValue": 70.0,
          "updatedAt": "2026-05-15T10:31:10"
        }
      ]
    }
  ]
}
```

6) Consultar una variable especifica
------------------------------------

Request:

```http
GET {{base_url}}/api/thresholds?zoneId={{zone_id}}&variables=TEMPERATURE
Authorization: Bearer {{admin_token}}
```

Notas:
- `zoneId` es requerido.
- Para varias zonas usa query params repetidos: `zoneId=1&zoneId=2`.
- Para varias variables usa query params repetidos: `variables=TEMPERATURE&variables=PH`.

7) Actualizar umbral
--------------------

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

Respuesta esperada: `204 No Content`.

8) Verificar cambio
-------------------

Request:

```http
GET {{base_url}}/api/thresholds?zoneId={{zone_id}}&variables=TEMPERATURE
Authorization: Bearer {{admin_token}}
```

Verifica que `minValue`, `maxValue` y `updatedAt` hayan cambiado.

Ejemplos curl
-------------

```bash
# 1. Login
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin1@invernadero.com","password":"Admin123456"}'

# 2. Crear zona
curl -X POST "http://localhost:8080/api/zones" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Zona Postman 1","description":"Zona creada desde API"}'

# 3. Crear threshold
curl -X POST "http://localhost:8080/api/thresholds" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"zoneId":1,"variableName":"TEMPERATURE","unit":"C","minValue":18.0,"maxValue":30.0}'

# 4. Listar thresholds
curl -X GET "http://localhost:8080/api/thresholds?zoneId=1" \
  -H "Authorization: Bearer <TOKEN>"

# 5. Actualizar threshold
curl -X PUT "http://localhost:8080/api/thresholds/1" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"minValue":20.0,"maxValue":32.0}'
```

Endpoints disponibles
---------------------

| Metodo | URL | Descripcion |
|--------|-----|-------------|
| `POST` | `/api/thresholds` | Crea un umbral para una zona y variable |
| `GET` | `/api/thresholds?zoneId=1` | Lista umbrales por zona |
| `PUT` | `/api/thresholds/{id}` | Actualiza minimo y maximo de un umbral |

Errores comunes
---------------
- `401 Unauthorized`: falta `Authorization: Bearer {{admin_token}}` o el token expiro.
- `403 Forbidden`: el usuario autenticado no tiene rol `ADMIN`.
- `400 Bad Request`: falta `zoneId`, `variableName`, `unit`, `minValue` o `maxValue`; tambien ocurre si `minValue >= maxValue`.
- `400 Bad Request` al crear: ya existe un umbral para esa zona y variable.
- `404 Not Found`: la zona o el umbral indicado no existe.

Checklist de prueba
-------------------
- [ ] Login ADMIN realizado.
- [ ] Token guardado en `admin_token`.
- [ ] Zona creada por API y `zone_id` guardado.
- [ ] Umbral `TEMPERATURE` creado por API.
- [ ] Umbral `AIR_HUMIDITY` creado por API.
- [ ] GET por zona responde `200 OK`.
- [ ] PUT actualiza un umbral y responde `204 No Content`.
- [ ] GET final confirma el cambio.
