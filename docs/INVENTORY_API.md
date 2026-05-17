# API y guia de pruebas - Modulo de Inventario (Inventory)

Este documento centraliza lo necesario para entender el modulo de inventario y probarlo desde Postman. La prueba asume que no se entra directamente a la base de datos: los datos se preparan por API.

Contenido
- Resumen del modulo
- Requisitos previos
- Valores validos
- Flujo recomendado en Postman
- Endpoints disponibles y ejemplos
- Errores comunes

-----------------------------------------------------------

Resumen del modulo
-------------------
- Entidad principal: `InventoryItem` (tabla `inventory_items`) representa un insumo, herramienta o producto del invernadero con su cantidad actual y stock minimo.
- Proposito: gestionar los recursos fisicos del invernadero (fertilizantes, herramientas, semillas, etc.) y detectar cuando alguno esta por debajo del nivel minimo de stock.
- Ubicacion en el proyecto: `modules/inventory`.
- Seguridad: endpoints GET accesibles para cualquier usuario autenticado. Crear y actualizar requiere rol `ADMIN` o `OPERATOR`. Eliminar requiere rol `ADMIN`.
- A diferencia de zonas y cultivos, los items de inventario si admiten eliminacion fisica porque no tienen dependencias con otros modulos.
- El campo `lowStock` en las respuestas indica si `quantity <= minStock`.

Valores validos
---------------

Categorias de inventario (InventoryCategory):

- `SEEDS`      -- Semillas
- `FERTILIZER` -- Fertilizante
- `PESTICIDE`  -- Pesticida
- `TOOLS`      -- Herramientas
- `OTHER`      -- Otro

Flujo recomendado en Postman
----------------------------
Configura un environment llamado `Invernadero-Dev` con estas variables (o agrega las nuevas si ya lo tienes):

```text
base_url = http://localhost:8080
admin_token =
inventory_item_id =
```

Headers comunes para requests protegidas:

```text
Authorization: Bearer {{admin_token}}
Content-Type: application/json
```

-----------------------------------------------------------

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

2) Crear item de inventario
----------------------------

Request:

```http
POST {{base_url}}/api/inventory
Authorization: Bearer {{admin_token}}
Content-Type: application/json
```

Body completo:

```json
{
  "name": "Fertilizante NPK",
  "category": "FERTILIZER",
  "quantity": 50.0,
  "unit": "kg",
  "minStock": 10.0
}
```

Body sin minStock (queda en 0 por defecto):

```json
{
  "name": "Manguera de riego",
  "category": "TOOLS",
  "quantity": 3.0,
  "unit": "unidad"
}
```

Respuesta esperada `201 Created`:

```json
{
  "success": true,
  "status": 201,
  "message": "Item registrado exitosamente",
  "data": {
    "id": 1,
    "name": "Fertilizante NPK",
    "category": "FERTILIZER",
    "quantity": 50.0,
    "unit": "kg",
    "minStock": 10.0,
    "lowStock": false,
    "updatedAt": null
  }
}
```

Guarda `data.id` en `inventory_item_id`.

Validaciones:
- `name` es obligatorio.
- `category` es obligatorio. Valores validos: `SEEDS`, `FERTILIZER`, `PESTICIDE`, `TOOLS`, `OTHER`.
- `quantity` es obligatorio y no puede ser negativa.
- `unit` es obligatorio.
- `minStock` es opcional. Si no se envia, queda en 0.

---

3) Listar items de inventario
------------------------------

Todos los items:

```http
GET {{base_url}}/api/inventory
Authorization: Bearer {{admin_token}}
```

Filtrar por categoria:

```http
GET {{base_url}}/api/inventory?category=FERTILIZER
Authorization: Bearer {{admin_token}}
```

Solo items con stock bajo:

```http
GET {{base_url}}/api/inventory?lowStock=true
Authorization: Bearer {{admin_token}}
```

Filtrar por categoria y stock bajo:

```http
GET {{base_url}}/api/inventory?category=SEEDS&lowStock=true
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
      "name": "Fertilizante NPK",
      "category": "FERTILIZER",
      "quantity": 50.0,
      "unit": "kg",
      "minStock": 10.0,
      "lowStock": false
    },
    {
      "id": 2,
      "name": "Semillas de tomate",
      "category": "SEEDS",
      "quantity": 2.0,
      "unit": "kg",
      "minStock": 5.0,
      "lowStock": true
    }
  ]
}
```

Nota: `lowStock` es `true` cuando `quantity <= minStock`.

---

4) Obtener detalle de un item
------------------------------

Request:

```http
GET {{base_url}}/api/inventory/{{inventory_item_id}}
Authorization: Bearer {{admin_token}}
```

Respuesta esperada `200 OK`:

```json
{
  "success": true,
  "status": 200,
  "data": {
    "id": 1,
    "name": "Fertilizante NPK",
    "category": "FERTILIZER",
    "quantity": 50.0,
    "unit": "kg",
    "minStock": 10.0,
    "lowStock": false,
    "updatedAt": "2026-05-17T10:00:00"
  }
}
```

---

5) Actualizar item de inventario
---------------------------------

Request:

```http
PATCH {{base_url}}/api/inventory/{{inventory_item_id}}
Authorization: Bearer {{admin_token}}
Content-Type: application/json
```

Actualizar cantidad tras un consumo:

```json
{
  "quantity": 35.0
}
```

Actualizar stock minimo:

```json
{
  "minStock": 15.0
}
```

Actualizar varios campos:

```json
{
  "quantity": 8.0,
  "minStock": 10.0,
  "unit": "bolsas"
}
```

Respuesta esperada `200 OK`:

```json
{
  "success": true,
  "status": 200,
  "message": "Item actualizado exitosamente",
  "data": {
    "id": 1,
    "name": "Fertilizante NPK",
    "category": "FERTILIZER",
    "quantity": 8.0,
    "unit": "bolsas",
    "minStock": 10.0,
    "lowStock": true,
    "updatedAt": "2026-05-17T10:30:00"
  }
}
```

Nota: cuando `quantity` queda por debajo de `minStock`, `lowStock` cambia a `true` automaticamente en el response.

---

6) Eliminar item de inventario
--------------------------------

Request:

```http
DELETE {{base_url}}/api/inventory/{{inventory_item_id}}
Authorization: Bearer {{admin_token}}
```

Respuesta esperada `200 OK`:

```json
{
  "success": true,
  "status": 200,
  "message": "Item eliminado exitosamente",
  "data": null
}
```

Nota: la eliminacion es fisica. A diferencia de zonas y cultivos, los items de inventario no tienen dependencias con otros modulos, por lo que pueden eliminarse definitivamente.

-----------------------------------------------------------

Ejemplos curl
-------------

```bash
BASE_URL="http://localhost:8080"
TOKEN="<tu_token_aqui>"

# Crear item
curl -X POST "$BASE_URL/api/inventory" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name":"Fertilizante NPK",
    "category":"FERTILIZER",
    "quantity":50.0,
    "unit":"kg",
    "minStock":10.0
  }'

# Listar todos
curl -X GET "$BASE_URL/api/inventory" \
  -H "Authorization: Bearer $TOKEN"

# Listar por categoria
curl -X GET "$BASE_URL/api/inventory?category=FERTILIZER" \
  -H "Authorization: Bearer $TOKEN"

# Listar stock bajo
curl -X GET "$BASE_URL/api/inventory?lowStock=true" \
  -H "Authorization: Bearer $TOKEN"

# Actualizar cantidad
curl -X PATCH "$BASE_URL/api/inventory/1" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"quantity":35.0}'

# Eliminar item
curl -X DELETE "$BASE_URL/api/inventory/1" \
  -H "Authorization: Bearer $TOKEN"
```

Errores comunes
---------------
- `401 Unauthorized`: falta `Authorization: Bearer {{admin_token}}` o el token expiro.
- `403 Forbidden`: el usuario no tiene el rol requerido. Crear/actualizar requiere `ADMIN` o `OPERATOR`. Eliminar requiere `ADMIN`.
- `400 Bad Request`: falta `name`, `category`, `quantity` o `unit`.
- `400 Bad Request`: `quantity` o `minStock` con valor negativo.
- `400 Bad Request`: `category` con valor invalido. Usa: `SEEDS`, `FERTILIZER`, `PESTICIDE`, `TOOLS`, `OTHER`.
- `404 Not Found`: el item indicado no existe.

Checklist de prueba
-------------------
- [ ] Login ADMIN realizado y `admin_token` guardado.
- [ ] Item FERTILIZER creado con quantity > minStock. `lowStock` debe ser `false`.
- [ ] Item SEEDS creado con quantity < minStock. `lowStock` debe ser `true`.
- [ ] GET de todos los items responde `200 OK`.
- [ ] GET con `?category=FERTILIZER` filtra correctamente.
- [ ] GET con `?lowStock=true` devuelve solo items con stock bajo.
- [ ] GET con `?category=SEEDS&lowStock=true` filtra por ambos criterios.
- [ ] PATCH actualiza cantidad y `lowStock` refleja el nuevo estado.
- [ ] GET por id incluye `updatedAt` actualizado.
- [ ] DELETE elimina el item. GET posterior responde `404 Not Found`.

Arquitectura tecnica
---------------------

Estructura del modulo:

```text
modules/inventory/
  controller/
    InventoryController.java
      GET    /api/inventory
      GET    /api/inventory/{id}
      POST   /api/inventory
      PATCH  /api/inventory/{id}
      DELETE /api/inventory/{id}
  dto/request/
    InventoryCreateRequest.java
    InventoryUpdateRequest.java
  dto/response/
    InventoryListResponseDTO.java
    InventoryResponseDTO.java
  model/
    InventoryCategory.java
    InventoryItem.java
  repository/
    InventoryRepository.java
  service/
    InventoryService.java
    impl/InventoryServiceImpl.java
```

Flujo POST /api/inventory:

```text
Cliente
  -> InventoryController.createItem()
  -> InventoryServiceImpl.createItem()
  -> guarda InventoryItem con minStock = 0 si no se envia
  -> calcula lowStock = (quantity <= minStock)
  -> responde DTO con todos los campos incluyendo lowStock
```

Flujo GET /api/inventory:

```text
Cliente
  -> InventoryController.listItems(category, lowStock)
  -> InventoryServiceImpl.listItems()
  -> si lowStock=true y category != null: filtra en memoria
  -> si solo lowStock=true: usa query JPQL findLowStockItems()
  -> si solo category != null: usa findAllByCategory()
  -> si ninguno: findAll()
  -> mapea cada item calculando lowStock en tiempo real
  -> responde lista de InventoryListResponseDTO
```

Flujo DELETE /api/inventory/{id}:

```text
Cliente
  -> InventoryController.deleteItem()
  -> InventoryServiceImpl.deleteItem()
  -> verifica que el item exista (lanza 404 si no)
  -> elimina fisicamente de la BD
  -> responde 200 OK con mensaje de confirmacion
```
