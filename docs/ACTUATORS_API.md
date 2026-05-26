# ACTUATORS API

## Objetivo
Esta guía documenta la gestión de actuadores agregada sobre la estructura actual del backend, **sin modificar la base de datos**.

La implementación reutiliza:
- `modules/actuators/model/Actuator.java`
- `modules/actuators/repository/ActuatorJPARepository.java`
- `modules/iot/service/ActuatorService.java`
- `modules/iot/service/impl/ActuatorServiceImpl.java`
- `modules/iot/controller/IOTController.java`

---

## 1. Modelo actual de actuadores

La entidad `Actuator` ya existe y está asociada a una zona:

- `zone_id` → FK a `zones(id)`
- `name` → nombre lógico del actuador dentro de la zona
- `current_action` → estado actual (`ON` / `OFF`)

La clave lógica usada por el backend para identificar un actuador es:

```text
(zone_id, name)
```

---

## 2. Endpoints nuevos

### 2.1 Listar actuadores

```http
GET /api/actuators
GET /api/actuators?zoneId=1
```

Devuelve todos los actuadores o solo los de una zona.

### 2.2 Consultar actuador por id

```http
GET /api/actuators/{id}
```

### 2.3 Crear actuador

```http
POST /api/actuators
```

Body ejemplo:

```json
{
  "zoneId": 1,
  "name": "BOMBA",
  "currentAction": "OFF"
}
```

Si `currentAction` no se envía, el backend usa `OFF` por defecto.

### 2.4 Actualizar actuador

```http
PATCH /api/actuators/{id}
```

Body ejemplo:

```json
{
  "name": "BOMBA_PRINCIPAL",
  "currentAction": "ON"
}
```

Campos opcionales:
- `zoneId`
- `name`
- `currentAction`

### 2.5 Ejecutar comando sobre actuador

```http
POST /api/actuators/{id}/command
```

Body ejemplo:

```json
{
  "action": "ON"
}
```

Este endpoint:
- busca el actuador por id
- publica el comando por MQTT
- guarda el evento en MongoDB
- actualiza el estado actual en PostgreSQL

### 2.6 Eliminar actuador

```http
DELETE /api/actuators/{id}
```

---

## 3. Endpoint IoT que ya existía

Además del controller nuevo, ya existía este endpoint técnico:

```http
POST /api/iot/actuator/event/{zoneId}
```

Body ejemplo:

```json
{
  "name": "BOMBA",
  "action": "ON"
}
```

Este endpoint quedó como una vía directa para eventos IoT.

---

## 4. Seguridad

- Todos los endpoints requieren JWT válido.
- Listar y consultar por id solo requieren autenticación.
- Crear, actualizar y eliminar requieren rol `ADMIN`.
- Ejecutar comandos sobre un actuador requiere rol `ADMIN` u `OPERATOR`.

---

## 5. Reglas de negocio

### 5.1 Asociación por zona

Un actuador pertenece a una sola zona.

El backend valida duplicados por:
- misma zona
- mismo nombre

### 5.2 Estados permitidos

El estado actual usa el enum:

- `ON`
- `OFF`

### 5.3 Validaciones

- `zoneId` debe existir en PostgreSQL.
- `name` no puede estar vacío.
- No se permiten dos actuadores con el mismo nombre en la misma zona.

---

## 6. Ejemplos de uso en Postman

### 6.1 Crear actuador

**Method:** `POST`
**URL:** `{{baseUrl}}/api/actuators`
**Headers:**

```http
Authorization: Bearer <token>
Content-Type: application/json
Accept: application/json
```

**Body:**

```json
{
  "zoneId": 1,
  "name": "VENTILADOR",
  "currentAction": "OFF"
}
```

### 6.2 Listar actuadores por zona

**Method:** `GET`
**URL:** `{{baseUrl}}/api/actuators?zoneId=1`
**Headers:**

```http
Authorization: Bearer <token>
Accept: application/json
```

### 6.3 Ejecutar comando ON

**Method:** `POST`
**URL:** `{{baseUrl}}/api/actuators/2/command`
**Headers:**

```http
Authorization: Bearer <token>
Content-Type: application/json
Accept: application/json
```

**Body:**

```json
{
  "action": "ON"
}
```

### 6.4 Ejecutar comando OFF

```json
{
  "action": "OFF"
}
```

---

## 7. Respuesta estándar

El backend mantiene el envelope común:

```json
{
  "success": true,
  "status": 200,
  "message": "Actuador actualizado exitosamente",
  "data": {},
  "errors": null,
  "timestamp": "2026-05-25T..."
}
```

---

## 8. Pruebas ejecutadas

Se validó con pruebas unitarias y `mvn test`.

Resultado:
- `BUILD SUCCESS`

---

## 9. Resumen

Con esta implementación ya puedes:
- consultar actuadores
- crear actuadores por zona
- editarlos
- eliminarlos
- registrar comandos `ON/OFF`
- mantener historial y estado actual sin cambiar el esquema de BD

**Última actualización:** 2026-05-25

