# 📊 IS-HU-04: Gráficas Históricas de Variables IoT

## 🎯 ¿Qué Hace?

Endpoint `GET /api/readings/history` retorna datos históricos de sensores por zona y variable. Opcional: rango de fechas (ISO 8601). Sin fechas, trae todos los datos disponibles.

**Query Parameters:** 
- `zoneId` (requerido)
- `variableName` (requerido)  
- `from` (opcional) - Fecha inicio
- `to` (opcional) - Fecha fin

---

## 🧪 Pruebas en Postman

### ✅ Caso 1: Exitosa (Con Rango)
```
GET {{base_url}}/api/readings/history?zoneId=1&variableName=temperature&from=2026-05-01T00:00:00Z&to=2026-05-14T23:59:59Z
Authorization: Bearer {{token}}
```
**→ 200 OK** con `points` del rango

---

### ✅ Caso 1b: Exitosa (Sin Rango - Todos los Datos)
```
GET {{base_url}}/api/readings/history?zoneId=1&variableName=temperature
Authorization: Bearer {{token}}
```
**→ 200 OK** con todos los `points` disponibles

---

### ❌ Caso 2: Sin Datos en el Rango
```
GET {{base_url}}/api/readings/history?zoneId=1&variableName=temperature&from=2026-04-01T00:00:00Z&to=2026-04-02T00:00:00Z
Authorization: Bearer {{token}}
```
**→ 200 OK** con `points: []`

---

### ❌ Caso 3: Zona No Existe
```
GET {{base_url}}/api/readings/history?zoneId=999&variableName=temperature
Authorization: Bearer {{token}}
```
**→ 404 Not Found**

---

### ❌ Caso 4: zoneId Inválido (≤ 0)
```
GET {{base_url}}/api/readings/history?zoneId=0&variableName=temperature
Authorization: Bearer {{token}}
```
**→ 400 Bad Request** "zoneId debe ser mayor que cero"

---

### ❌ Caso 5: Parámetro Faltante
```
GET {{base_url}}/api/readings/history?zoneId=1&from=2026-05-01T00:00:00Z&to=2026-05-14T23:59:59Z
Authorization: Bearer {{token}}
```
**→ 400 Bad Request** "variableName es requerido"

---

### ❌ Caso 6: Fechas Invertidas
```
GET {{base_url}}/api/readings/history?zoneId=1&variableName=temperature&from=2026-05-14T23:59:59Z&to=2026-05-01T00:00:00Z
Authorization: Bearer {{token}}
```
**→ 400 Bad Request** "from debe ser anterior a to"

---

### ❌ Caso 7: Sin Token
```
GET {{base_url}}/api/readings/history?zoneId=1&variableName=temperature
```
**→ 401 Unauthorized**

---

**Implementación:** IS-HU-04 ✅




