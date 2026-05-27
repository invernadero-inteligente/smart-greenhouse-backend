# API y guia de pruebas - Modulo de Reportes (Reports)

Este documento centraliza lo necesario para entender el modulo de generacion de reportes PDF y probarlo desde Postman.

Contenido
- Resumen del modulo
- Requisitos previos
- Tipos de reporte disponibles
- Flujo recomendado en Postman
- Endpoint disponible y ejemplos
- Errores comunes

-----------------------------------------------------------

Resumen del modulo
-------------------
- Proposito: generar reportes PDF del sistema con encabezado institucional, resumen estadistico y detalle agrupado, exportables directamente desde Postman o cualquier cliente HTTP.
- Libreria utilizada: OpenPDF (com.github.librepdf:openpdf:2.0.3). Se eligio sobre JasperReports por ser mas simple de integrar en Spring Boot sin necesidad de plantillas externas.
- Ubicacion en el proyecto: modules/reports.
- Seguridad: solo ADMIN puede generar reportes.
- El endpoint retorna directamente el archivo PDF como bytes con Content-Disposition: attachment.

Tipos de reporte disponibles
-----------------------------

ALERTS — Historial de alertas:
- Fuente de datos: tabla alerts (PostgreSQL).
- Filtros: rango de fechas (from/to) y zona (zoneId opcional).
- Contenido: resumen con total, abiertas y atendidas; detalle agrupado por zona con fecha, variable, valor, severidad, mensaje y estado.
- Severidad: HIGH en rojo, MEDIUM en naranja, LOW en verde.
- Estado: OPEN en rojo, ATTENDED en verde.

INVENTORY — Estado del inventario:
- Fuente de datos: tabla inventory_items (PostgreSQL).
- Filtros: rango de fechas (referencial para el encabezado; muestra todos los items actuales).
- Contenido: resumen con total de items e items con stock bajo; detalle agrupado por categoria con nombre, cantidad, unidad, stock minimo y estado (OK / STOCK BAJO).

PRODUCTION — Estado de cultivos:
- Fuente de datos: tabla crops (PostgreSQL).
- Filtros: rango de fechas (referencial) y zona (zoneId opcional).
- Contenido: resumen con cultivos activos, en cosecha, finalizados y total de plantas; detalle agrupado por zona con cultivo, variedad, plantas, fecha de siembra y estado.
- Estado: ACTIVE en verde, HARVEST en naranja, FINISHED en gris.

Diseño del PDF:
- Banner de titulo verde oscuro con nombre del sistema y titulo del reporte.
- Metadatos: fecha de generacion, periodo desde/hasta.
- Tablas con encabezados verde medio, filas alternadas en verde muy claro y bordes sutiles.
- Mensaje en italica cuando no hay datos en el periodo seleccionado.

Flujo recomendado en Postman
----------------------------
Configura un environment llamado Invernadero-Dev con esta variable (o agrega si ya lo tienes):

```text
base_url = http://localhost:8080
admin_token =
```

IMPORTANTE: Para recibir el PDF en Postman debes configurar la respuesta para guardarse como archivo:
- Haz click en la flecha junto al boton Send
- Selecciona "Send and Download"
- Postman te pedira donde guardar el archivo .pdf

-----------------------------------------------------------

Endpoint disponible
-------------------

| Metodo | URL                      | Descripcion              | Rol requerido |
|--------|--------------------------|--------------------------|---------------|
| `POST` | `/api/reports/generate`  | Genera un reporte PDF    | ADMIN         |

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

Guarda el token en admin_token.

---

2) Generar reporte de alertas
------------------------------

Request:

```http
POST {{base_url}}/api/reports/generate
Authorization: Bearer {{admin_token}}
Content-Type: application/json
```

Body:

```json
{
  "type": "ALERTS",
  "from": "2026-01-01T00:00:00",
  "to": "2026-05-31T23:59:59"
}
```

Con filtro por zona:

```json
{
  "type": "ALERTS",
  "from": "2026-01-01T00:00:00",
  "to": "2026-05-31T23:59:59",
  "zoneId": 1
}
```

Respuesta esperada: archivo PDF con nombre reporte_alertas_YYYYMMDD_HHmm.pdf

---

3) Generar reporte de inventario
----------------------------------

Body:

```json
{
  "type": "INVENTORY",
  "from": "2026-01-01T00:00:00",
  "to": "2026-05-31T23:59:59"
}
```

Respuesta esperada: archivo PDF con nombre reporte_inventario_YYYYMMDD_HHmm.pdf

Nota: el filtro zoneId no aplica para inventario (los items no estan vinculados a zonas). Si se envia, se ignora.

---

4) Generar reporte de produccion
----------------------------------

Body:

```json
{
  "type": "PRODUCTION",
  "from": "2026-01-01T00:00:00",
  "to": "2026-05-31T23:59:59"
}
```

Con filtro por zona:

```json
{
  "type": "PRODUCTION",
  "from": "2026-01-01T00:00:00",
  "to": "2026-05-31T23:59:59",
  "zoneId": 1
}
```

Respuesta esperada: archivo PDF con nombre reporte_produccion_YYYYMMDD_HHmm.pdf

-----------------------------------------------------------

Ejemplos curl
-------------

```bash
BASE_URL="http://localhost:8080"
TOKEN="<tu_token_aqui>"

# Reporte de alertas
curl -X POST "$BASE_URL/api/reports/generate" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"type":"ALERTS","from":"2026-01-01T00:00:00","to":"2026-05-31T23:59:59"}' \
  --output reporte_alertas.pdf

# Reporte de inventario
curl -X POST "$BASE_URL/api/reports/generate" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"type":"INVENTORY","from":"2026-01-01T00:00:00","to":"2026-05-31T23:59:59"}' \
  --output reporte_inventario.pdf

# Reporte de produccion filtrado por zona
curl -X POST "$BASE_URL/api/reports/generate" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"type":"PRODUCTION","from":"2026-01-01T00:00:00","to":"2026-05-31T23:59:59","zoneId":1}' \
  --output reporte_produccion.pdf
```

Errores comunes
---------------
- 401 Unauthorized: falta Authorization: Bearer token o el token expiro.
- 403 Forbidden: el usuario no tiene rol ADMIN.
- 400 Bad Request: falta type, from o to.
- 400 Bad Request: from es posterior a to.
- 400 Bad Request: type con valor invalido. Usa: ALERTS, INVENTORY, PRODUCTION.
- 500 Internal Server Error: verificar logs del backend. Causa comun: query con parametros nulos en PostgreSQL. Solucion aplicada: uso de Specification en lugar de JPQL dinamico.

Checklist de prueba
-------------------
- [ ] Login ADMIN realizado y admin_token guardado.
- [ ] Reporte ALERTS generado correctamente como PDF.
- [ ] Reporte ALERTS con zoneId filtra solo alertas de esa zona.
- [ ] Reporte INVENTORY generado correctamente con items agrupados por categoria.
- [ ] Reporte INVENTORY muestra STOCK BAJO en rojo para items con quantity <= minStock.
- [ ] Reporte PRODUCTION generado correctamente con cultivos agrupados por zona.
- [ ] Reporte PRODUCTION con zoneId filtra solo cultivos de esa zona.
- [ ] PDF sin datos muestra mensaje en italica en lugar de tabla vacia.
- [ ] Nombre del archivo incluye fecha y hora de generacion.

Arquitectura tecnica
---------------------

Estructura del modulo:

```text
modules/reports/
  controller/
    ReportController.java
      POST /api/reports/generate
  dto/
    ReportRequestDTO.java       (type, from, to, zoneId)
  service/
    ReportService.java
    impl/ReportServiceImpl.java
  builder/
    ReportPdfBuilder.java       (clase base: estilos, encabezado, celdas)
    AlertReportBuilder.java     (reporte de alertas)
    InventoryReportBuilder.java (reporte de inventario)
    ProductionReportBuilder.java(reporte de produccion)
```

Dependencia agregada al pom.xml:

```xml
<dependency>
    <groupId>com.github.librepdf</groupId>
    <artifactId>openpdf</artifactId>
    <version>2.0.3</version>
</dependency>
```

Flujo POST /api/reports/generate:

```text
Cliente
  -> ReportController.generateReport()
  -> ReportServiceImpl.generateReport()
  -> valida que from no sea posterior a to
  -> segun type llama al builder correspondiente:
       ALERTS     -> AlertReportBuilder.build()
       INVENTORY  -> InventoryReportBuilder.build()
       PRODUCTION -> ProductionReportBuilder.build()
  -> builder consulta repositorio correspondiente
  -> construye PDF en memoria con OpenPDF
  -> retorna byte[] al controller
  -> controller retorna ResponseEntity<byte[]> con:
       Content-Type: application/pdf
       Content-Disposition: attachment; filename="reporte_xxx_fecha.pdf"
```

Nota tecnica — problema resuelto con AlertSpecification:
---------------------------------------------------------
La query JPQL dinamica original con parametros nulos causaba el error
"could not determine data type of parameter" en PostgreSQL con Hibernate 6.
Se resolvio implementando AlertSpecification con JpaSpecificationExecutor,
que construye los predicados solo para los filtros con valor no nulo,
evitando enviar parametros null al driver JDBC de PostgreSQL.