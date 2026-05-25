# API y guia de pruebas - Modulo de Cultivos y Zonas (Crops & Zones)

Este documento centraliza lo necesario para entender los modulos de cultivos y zonas y probarlos desde Postman. La prueba asume que no se entra directamente a la base de datos: los datos se preparan por API.

Contenido
- Resumen de los modulos
- Requisitos previos
- Valores validos
- Flujo recomendado en Postman
- Endpoints disponibles y ejemplos
- Errores comunes

-----------------------------------------------------------

Resumen de los modulos
-----------------------
- Entidad principal Zonas: `Zone` (tabla `zones`) representa un area fisica del invernadero. Tiene nombre unico, descripcion y estado activo/inactivo.
- Entidad principal Cultivos: `Crop` (tabla `crops`) vincula un cultivo a una zona con nombre, variedad, cantidad de plantas, fecha de siembra y estado. Puede tener condiciones ideales asociadas en `CropCondition`.
- Proposito: gestionar las areas fisicas del invernadero y los cultivos que se siembran en ellas.
- Ubicacion en el proyecto: `modules/zones` y `modules/crops`.
- Seguridad: todos los endpoints requieren JWT. Crear y actualizar requiere rol `ADMIN`. Consultar es accesible para cualquier usuario autenticado.
- El borrado fisico no esta disponible. La desactivacion de zonas se hace con PATCH (`isActive: false`). El cierre de cultivos se hace con PATCH (`status: FINISHED`).

Nota sobre la dependencia entre modulos:
-----------------------------------------
Los cultivos dependen de zonas. Para crear un cultivo debes tener al menos una zona creada previamente. Los umbrales (thresholds) tambien dependen de zonas.

Valores validos
---------------

Estados de cultivo (CropStatus):

- `ACTIVE`   -- Cultivo activo en produccion
- `HARVEST`  -- En proceso de cosecha
- `FINISHED` -- Finalizado. Un cultivo FINISHED no puede ser modificado.

Variables de condicion ideal (en el objeto conditions):

El objeto `conditions` agrupa los rangos ideales del cultivo. Cada variable tiene `min` y `max`:

- `temperature`  -- Temperatura en grados Celsius
- `airHumidity`  -- Humedad del aire en porcentaje
- `soilMoisture` -- Humedad del suelo en porcentaje
- `ph`           -- pH del suelo

Flujo recomendado en Postman
----------------------------
Configura un environment llamado `Invernadero-Dev` con estas variables (o agrega las nuevas si ya lo tienes):

```text
base_url = http://localhost:8080
admin_token =
zone_id =
crop_id =
```

Headers comunes para requests protegidas:

```text
Authorization: Bearer {{admin_token}}
Content-Type: application/json
```

-----------------------------------------------------------

MODULO DE ZONAS
===============

Endpoints disponibles:

| Metodo  | URL                   | Descripcion                              | Rol requerido |
|---------|-----------------------|------------------------------------------|---------------|
| `GET`   | `/api/zones`          | Lista todas las zonas con filtro opcional| Autenticado   |
| `GET`   | `/api/zones/{id}`     | Detalle de una zona                      | Autenticado   |
| `POST`  | `/api/zones`          | Crea una nueva zona                      | ADMIN         |
| `PATCH` | `/api/zones/{id}`     | Actualiza parcialmente una zona          | ADMIN         |

---

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
  "email": "admin3@invernadero.com",
  "password": "Admin123456"
}
```

Guarda el `token` de la respuesta en `admin_token`.

---

2) Crear zona
-------------

Request:

```http
POST {{base_url}}/api/zones
Authorization: Bearer {{admin_token}}
Content-Type: application/json
```

Body:

```json
{
  "name": "BANANO",
  "description": "Zona de cultivo de banano"
}
```

Respuesta esperada `201 Created`:

```json
{
  "success": true,
  "status": 201,
  "message": "Zona creada exitosamente",
  "data": {
    "id": 1,
    "name": "BANANO",
    "description": "Zona de cultivo de banano",
    "isActive": true,
    "createdAt": "2026-05-17T10:00:00",
    "updatedAt": null
  }
}
```

Guarda `data.id` en `zone_id`.

Validaciones:
- `name` es obligatorio y debe ser unico.
- El nombre debe coincidir con el `node.name` que enviara el equipo IoT.

---

3) Listar zonas
---------------

Todas las zonas:

```http
GET {{base_url}}/api/zones
Authorization: Bearer {{admin_token}}
```

Solo zonas activas:

```http
GET {{base_url}}/api/zones?isActive=true
Authorization: Bearer {{admin_token}}
```

Solo zonas inactivas:

```http
GET {{base_url}}/api/zones?isActive=false
Authorization: Bearer {{admin_token}}
```

Respuesta esperada `200 OK`:

```json
{
  "success": true,
  "status": 200,
  "data": [
    {
      "id": 1,
      "name": "BANANO",
      "description": "Zona de cultivo de banano",
      "isActive": true
    }
  ]
}
```

---

4) Obtener detalle de una zona
------------------------------

Request:

```http
GET {{base_url}}/api/zones/{{zone_id}}
Authorization: Bearer {{admin_token}}
```

Respuesta esperada `200 OK`:

```json
{
  "success": true,
  "status": 200,
  "data": {
    "id": 1,
    "name": "BANANO",
    "description": "Zona de cultivo de banano",
    "isActive": true,
    "createdAt": "2026-05-17T10:00:00",
    "updatedAt": null
  }
}
```

---

5) Actualizar zona
------------------

Actualizar descripcion:

```http
PATCH {{base_url}}/api/zones/{{zone_id}}
Authorization: Bearer {{admin_token}}
Content-Type: application/json
```

Body:

```json
{
  "description": "Nueva descripcion de la zona"
}
```

Desactivar zona (borrado logico):

```json
{
  "isActive": false
}
```

Actualizar nombre:

```json
{
  "name": "TOMATE"
}
```

Respuesta esperada `200 OK`:

```json
{
  "success": true,
  "status": 200,
  "message": "Zona actualizada exitosamente",
  "data": {
    "id": 1,
    "name": "TOMATE",
    "description": "Nueva descripcion de la zona",
    "isActive": true,
    "createdAt": "2026-05-17T10:00:00",
    "updatedAt": "2026-05-17T10:05:00"
  }
}
```

Notas sobre PATCH:
- Solo se modifican los campos enviados. Los campos no incluidos en el body no cambian.
- Para desactivar una zona usa `isActive: false`. No existe endpoint DELETE.
- Si cambias el nombre, el nuevo nombre debe coincidir con el `node.name` de IoT.

-----------------------------------------------------------

MODULO DE CULTIVOS
==================

Endpoints disponibles:

| Metodo  | URL               | Descripcion                               | Rol requerido |
|---------|-------------------|-------------------------------------------|---------------|
| `GET`   | `/api/crops`      | Lista cultivos con filtros opcionales     | Autenticado   |
| `GET`   | `/api/crops/{id}` | Detalle completo de un cultivo            | Autenticado   |
| `POST`  | `/api/crops`      | Registra un nuevo cultivo                 | ADMIN         |
| `PATCH` | `/api/crops/{id}` | Actualiza parcialmente un cultivo         | ADMIN         |

---

6) Crear cultivo con condiciones ideales
-----------------------------------------

Request:

```http
POST {{base_url}}/api/crops
Authorization: Bearer {{admin_token}}
Content-Type: application/json
```

Body completo:

```json
{
  "name": "Banano Cavendish",
  "variety": "Cavendish",
  "plantCount": 100,
  "zoneId": {{zone_id}},
  "sowingDate": "2026-05-01",
  "status": "ACTIVE",
  "conditions": {
    "temperature": { "min": 20.0, "max": 30.0 },
    "airHumidity": { "min": 70.0, "max": 90.0 },
    "soilMoisture": { "min": 50.0, "max": 75.0 },
    "ph": { "min": 5.5, "max": 7.0 }
  }
}
```

Body minimo (conditions es opcional):

```json
{
  "name": "Tomate Cherry",
  "zoneId": {{zone_id}},
  "status": "ACTIVE"
}
```

Respuesta esperada `201 Created`:

```json
{
  "data": {
    "id": 1,
    "name": "Banano Cavendish",
    "status": "ACTIVE"
  }
}
```

Guarda `data.id` en `crop_id`.

Validaciones:
- `name` es obligatorio.
- `zoneId` es obligatorio y la zona debe existir.
- `status` es obligatorio. Valores validos: `ACTIVE`, `HARVEST`, `FINISHED`.
- `plantCount` debe ser mayor a cero si se envia.
- `conditions` es opcional. Si se omite, el cultivo queda sin condiciones ideales.

---

7) Listar cultivos
------------------

Todos los cultivos:

```http
GET {{base_url}}/api/crops
Authorization: Bearer {{admin_token}}
```

Filtrar por estado:

```http
GET {{base_url}}/api/crops?status=ACTIVE
Authorization: Bearer {{admin_token}}
```

Filtrar por zona:

```http
GET {{base_url}}/api/crops?zoneId={{zone_id}}
Authorization: Bearer {{admin_token}}
```

Filtrar por zona y estado:

```http
GET {{base_url}}/api/crops?zoneId={{zone_id}}&status=ACTIVE
Authorization: Bearer {{admin_token}}
```

Respuesta esperada `200 OK` (agrupada por zona):

```json
{
  "data": [
    {
      "zoneId": 1,
      "zoneName": "BANANO",
      "info": [
        {
          "id": 1,
          "name": "Banano Cavendish",
          "variety": "Cavendish",
          "plantCount": 100,
          "sowingDate": "2026-05-01",
          "status": "ACTIVE"
        }
      ]
    }
  ]
}
```

---

8) Obtener detalle de un cultivo
---------------------------------

Request:

```http
GET {{base_url}}/api/crops/{{crop_id}}
Authorization: Bearer {{admin_token}}
```

Respuesta esperada `200 OK` (incluye condiciones ideales si existen):

```json
{
  "success": true,
  "status": 200,
  "data": {
    "id": 1,
    "name": "Banano Cavendish",
    "variety": "Cavendish",
    "plantCount": 100,
    "zoneId": 1,
    "zoneName": "BANANO",
    "sowingDate": "2026-05-01",
    "status": "ACTIVE",
    "conditions": {
      "temperatureMin": 20.0,
      "temperatureMax": 30.0,
      "airHumidityMin": 70.0,
      "airHumidityMax": 90.0,
      "soilMoistureMin": 50.0,
      "soilMoistureMax": 75.0,
      "phMin": 5.5,
      "phMax": 7.0
    },
    "createdAt": "2026-05-17T10:00:00",
    "updatedAt": null
  }
}
```

Si el cultivo no tiene condiciones, `conditions` sera `null`.

---

9) Actualizar cultivo
---------------------

Cambiar estado a cosecha:

```http
PATCH {{base_url}}/api/crops/{{crop_id}}
Authorization: Bearer {{admin_token}}
Content-Type: application/json
```

Body:

```json
{
  "status": "HARVEST"
}
```

Actualizar solo condiciones de temperatura:

```json
{
  "conditions": {
    "temperature": { "min": 22.0, "max": 32.0 }
  }
}
```

Actualizar varios campos:

```json
{
  "variety": "Gran Nain",
  "plantCount": 120,
  "conditions": {
    "ph": { "min": 6.0, "max": 7.5 }
  }
}
```

Respuesta esperada `200 OK`:

```json
{
  "success": true,
  "status": 200,
  "data": { ... }
}
```

Notas sobre PATCH en cultivos:
- Solo se modifican los campos enviados.
- Dentro de `conditions`, solo se actualizan las variables incluidas. El resto no cambia.
- Un cultivo con `status: FINISHED` no puede ser modificado. Responde `400 Bad Request`.
- Para cerrar definitivamente un cultivo usa `status: FINISHED`.

-----------------------------------------------------------

Ejemplos curl
-------------

```bash
BASE_URL="http://localhost:8080"
TOKEN="<tu_token_aqui>"

# Crear zona
curl -X POST "$BASE_URL/api/zones" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"BANANO","description":"Zona de cultivo de banano"}'

# Listar zonas activas
curl -X GET "$BASE_URL/api/zones?isActive=true" \
  -H "Authorization: Bearer $TOKEN"

# Desactivar zona
curl -X PATCH "$BASE_URL/api/zones/1" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"isActive":false}'

# Crear cultivo
curl -X POST "$BASE_URL/api/crops" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name":"Banano Cavendish",
    "variety":"Cavendish",
    "plantCount":100,
    "zoneId":1,
    "sowingDate":"2026-05-01",
    "status":"ACTIVE",
    "conditions":{
      "temperature":{"min":20.0,"max":30.0},
      "airHumidity":{"min":70.0,"max":90.0},
      "soilMoisture":{"min":50.0,"max":75.0},
      "ph":{"min":5.5,"max":7.0}
    }
  }'

# Listar cultivos activos
curl -X GET "$BASE_URL/api/crops?status=ACTIVE" \
  -H "Authorization: Bearer $TOKEN"

# Actualizar estado de cultivo
curl -X PATCH "$BASE_URL/api/crops/1" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"HARVEST"}'
```

Errores comunes
---------------
- `401 Unauthorized`: falta `Authorization: Bearer {{admin_token}}` o el token expiro.
- `403 Forbidden`: el usuario autenticado no tiene rol `ADMIN`.
- `400 Bad Request` al crear zona: nombre ya existe (`name` debe ser unico).
- `400 Bad Request` al actualizar cultivo: el cultivo tiene `status: FINISHED` y no puede modificarse.
- `404 Not Found` al crear cultivo: la zona indicada en `zoneId` no existe.
- `404 Not Found` al consultar: la zona o cultivo indicado no existe.

Checklist de prueba
-------------------
- [ ] Login ADMIN realizado y `admin_token` guardado.
- [ ] Zona creada por API y `zone_id` guardado.
- [ ] GET de zonas responde `200 OK`.
- [ ] GET de zonas con `isActive=true` filtra correctamente.
- [ ] Zona actualizada con PATCH (descripcion o isActive).
- [ ] Cultivo creado con condiciones y `crop_id` guardado.
- [ ] GET de cultivos responde `200 OK` agrupado por zona.
- [ ] GET de cultivo por id incluye el objeto `conditions`.
- [ ] Cultivo actualizado con PATCH (status, conditions).
- [ ] Intento de modificar cultivo FINISHED responde `400 Bad Request`.

Arquitectura tecnica
---------------------

Estructura del modulo zones:

```text
modules/zones/
  controller/
    ZoneController.java
      GET  /api/zones
      GET  /api/zones/{id}
      POST /api/zones
      PATCH /api/zones/{id}
  dto/request/
    ZoneCreateRequest.java
    ZoneUpdateRequest.java
  dto/response/
    ZoneListResponseDTO.java
    ZoneResponseDTO.java
  model/
    Zone.java
  repository/
    ZoneRepository.java
  service/
    ZoneService.java
    impl/ZoneServiceImpl.java
```

Estructura del modulo crops:

```text
modules/crops/
  controller/
    CropController.java
      GET  /api/crops
      GET  /api/crops/{id}
      POST /api/crops
      PATCH /api/crops/{id}
  dto/request/
    CropConditionsRequest.java
    CropCreateRequest.java
    CropUpdateRequest.java
  dto/response/
    CropConditionsResponse.java
    CropListResponseDTO.java
    CropResponseDTO.java
    CreateCropResponseDTO.java
    CropsDataResponseDTO.java
    CropsInfoResponseDTO.java
    CropsZoneResponseDTO.java
  mapper/
    CropsMapper.java
  model/
    Crop.java
    CropCondition.java
    CropStatus.java
  repository/
    CropConditionRepository.java
    CropRepository.java
  service/
    CropService.java
    impl/CropServiceImpl.java
```

Flujo POST /api/crops:

```text
Cliente
  -> CropController.createCrop()
  -> CropServiceImpl.createCrop()
  -> valida que la zona exista
  -> guarda Crop
  -> si conditions != null, guarda CropCondition (1:1 con Crop)
  -> responde DTO con id, name, status
```

Flujo PATCH /api/crops/{id}:

```text
Cliente
  -> CropController.updateCrop()
  -> CropServiceImpl.updateCrop()
  -> valida que el cultivo no este FINISHED
  -> actualiza solo los campos no nulos
  -> si conditions != null, actualiza o crea CropCondition
  -> responde 200 con el cultivo actualizado
```
