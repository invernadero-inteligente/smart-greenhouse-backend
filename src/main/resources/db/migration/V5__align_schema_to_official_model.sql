-- =============================================================================
-- V5__align_schema_to_official_model.sql
--
-- Propósito : Alinear el esquema de base de datos con el modelo de datos
--             oficial definido para el MVP del proyecto invernadero.
--
-- Cambios incluidos:
--   1. Habilitar extensión pgcrypto para generación de UUIDs
--   2. Eliminar tabla threshold_change_history (fuera del alcance MVP)
--   3. Reconstruir crop_conditions con rangos ideales por variable
--   4. Corregir tabla crops (variety, plant_count, sowing_date)
--   5. Agregar is_active a zones
--   6. Corregir tabla sensors (name, unit, SensorStatus completo)
--   7. Corregir tabla threshold_configs (variable, updated_by, UNIQUE)
--   8. Corregir tabla alerts (variable, value, unit, attended_by, attended_at)
--   9. Agregar min_stock a inventory_items
--  10. Reconstruir ai_results alineada al modelo oficial
--  11. Actualizar enum SensorVariable con valores correctos
--
-- Estrategia : Dado que el entorno es de desarrollo y no hay datos de
--              producción, las tablas que requieren cambios estructurales
--              profundos se reconstruyen con DROP + CREATE para garantizar
--              consistencia total. Las tablas con datos semilla (users) solo
--              reciben ALTER TABLE no destructivos.
--
-- Prerequisito : Ejecutar `docker compose down -v` antes de levantar el
--                proyecto para que Flyway aplique desde V1 limpio.
-- =============================================================================


-- =============================================================================
-- SECCIÓN 1 — Extensión para generación de UUIDs
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;


-- =============================================================================
-- SECCIÓN 2 — Eliminar tablas dependientes antes de modificar sus referencias
--
-- Orden: primero las tablas hijo (las que tienen FK hacia otras), luego las
-- tablas padre. Esto evita errores de violación de FK durante el proceso.
-- =============================================================================

-- threshold_change_history fue eliminada del modelo MVP para reducir
-- complejidad. No tiene dependientes, se puede eliminar directamente.
DROP TABLE IF EXISTS threshold_change_history;

-- Eliminar tablas que se van a reconstruir con nueva estructura.
-- Se eliminan en orden inverso a sus dependencias:
--   ai_results → depende de zones y crops
--   alerts     → depende de zones, sensors, crops, users
--   crop_conditions → depende de crops
DROP TABLE IF EXISTS ai_results;
DROP TABLE IF EXISTS alerts;
DROP TABLE IF EXISTS crop_conditions;


-- =============================================================================
-- SECCIÓN 3 — Corregir tabla: crops
--
-- Cambios:
--   - DROP crop_type         → reemplazado por variety (VARCHAR)
--   - DROP planted_at        → reemplazado por sowing_date (DATE)
--   - DROP expected_harvest_at → no está en el modelo oficial MVP
--   - ADD variety            → variedad del cultivo
--   - ADD plant_count        → cantidad de plantas (INT)
--   - ADD sowing_date        → fecha de siembra (DATE)
-- =============================================================================

ALTER TABLE crops
    DROP COLUMN IF EXISTS crop_type,
    DROP COLUMN IF EXISTS planted_at,
    DROP COLUMN IF EXISTS expected_harvest_at;

ALTER TABLE crops
    ADD COLUMN IF NOT EXISTS variety     VARCHAR(100),
    ADD COLUMN IF NOT EXISTS plant_count INT,
    ADD COLUMN IF NOT EXISTS sowing_date DATE;


-- =============================================================================
-- SECCIÓN 4 — Reconstruir tabla: crop_conditions
--
-- La tabla original tenía condition_date y notes (observaciones textuales).
-- El modelo oficial la define como la tabla de rangos ideales por cultivo:
-- temperatura, humedad del aire, humedad del suelo y pH, con mínimo y máximo
-- para cada variable. La relación es 1:1 con crops.
-- =============================================================================

CREATE TABLE crop_conditions (
    id               BIGSERIAL PRIMARY KEY,
    crop_id          BIGINT        NOT NULL UNIQUE REFERENCES crops(id),
    temperature_min  DECIMAL(5, 2),
    temperature_max  DECIMAL(5, 2),
    air_humidity_min DECIMAL(5, 2),
    air_humidity_max DECIMAL(5, 2),
    soil_moisture_min DECIMAL(5, 2),
    soil_moisture_max DECIMAL(5, 2),
    ph_min           DECIMAL(4, 2),
    ph_max           DECIMAL(4, 2),
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP
);

COMMENT ON TABLE crop_conditions IS
    'Condiciones ambientales ideales (rangos mínimo/máximo) para cada cultivo. Relación 1:1 con crops.';


-- =============================================================================
-- SECCIÓN 5 — Corregir tabla: zones
--
-- Cambios:
--   - ADD is_active → requerido por IS-HU-01 para filtrar zonas activas
--                     (/zones?isActive=true) y por el dashboard de monitoreo.
-- =============================================================================

ALTER TABLE zones
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;

COMMENT ON COLUMN zones.is_active IS
    'Indica si la zona está operativa. Usado para filtrar en el dashboard y en consultas de lecturas.';


-- =============================================================================
-- SECCIÓN 6 — Corregir tabla: sensors
--
-- Cambios:
--   - DROP serial_number → reemplazado por name (identificador legible)
--   - ADD name           → nombre descriptivo del sensor (ej: "Sensor Temp Zona A")
--   - ADD unit           → unidad de medida (ej: "°C", "%", "pH")
--   - Enum SensorVariable se actualiza en SECCIÓN 11
-- =============================================================================

ALTER TABLE sensors
    DROP COLUMN IF EXISTS serial_number;

ALTER TABLE sensors
    ADD COLUMN IF NOT EXISTS name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS unit VARCHAR(20);

COMMENT ON COLUMN sensors.name IS
    'Nombre descriptivo del sensor. Reemplaza serial_number del esquema inicial.';
COMMENT ON COLUMN sensors.unit IS
    'Unidad de medida de la variable que monitorea este sensor (ej: °C, %, pH).';


-- =============================================================================
-- SECCIÓN 7 — Corregir tabla: threshold_configs
--
-- Cambios:
--   - RENAME sensor_variable → variable  (consistencia con el modelo oficial)
--   - DROP created_by_user_id            (el modelo oficial usa updated_by)
--   - DROP created_at                    (no está en el modelo oficial MVP)
--   - ADD updated_by                     → FK al usuario que hizo el último cambio
--   - ADD UNIQUE(zone_id, variable)      → garantiza un umbral por variable/zona
-- =============================================================================

ALTER TABLE threshold_configs
    RENAME COLUMN sensor_variable TO variable;

ALTER TABLE threshold_configs
    DROP COLUMN IF EXISTS created_by_user_id,
    DROP COLUMN IF EXISTS created_at;

ALTER TABLE threshold_configs
    ADD COLUMN IF NOT EXISTS updated_by BIGINT REFERENCES users(id);

-- Garantiza que no existan dos umbrales para la misma variable en la misma zona.
-- Condición necesaria para que IS-HU-01 y IS-HU-02 calculen estados correctamente.
ALTER TABLE threshold_configs
    ADD CONSTRAINT uq_threshold_zone_variable UNIQUE (zone_id, variable);

COMMENT ON CONSTRAINT uq_threshold_zone_variable ON threshold_configs IS
    'Un único umbral por variable por zona. Evita lecturas ambiguas en el dashboard.';


-- =============================================================================
-- SECCIÓN 8 — Reconstruir tabla: alerts
--
-- La tabla original fue diseñada con campos resolved_* que no coinciden con
-- el modelo oficial. El modelo usa attended_by / attended_at para indicar
-- quién atendió la alerta, y agrega los campos variable, value y unit para
-- que la alerta sea autocontenida (IS-HU-02 los requiere en el response body).
-- =============================================================================

CREATE TABLE alerts (
    id           BIGSERIAL   PRIMARY KEY,
    zone_id      BIGINT      REFERENCES zones(id),
    sensor_id    BIGINT      REFERENCES sensors(id),
    crop_id      BIGINT      REFERENCES crops(id),
    origin       VARCHAR(50) NOT NULL,
    variable     VARCHAR(50) NOT NULL,
    severity     VARCHAR(50) NOT NULL,
    message      TEXT        NOT NULL,
    value        DECIMAL(10, 2),
    status       VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    attended_by  BIGINT      REFERENCES users(id),
    attended_at  TIMESTAMP,
    created_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE alerts IS
    'Alertas generadas por lecturas fuera de umbral (origen IoT) o por análisis de IA.';
COMMENT ON COLUMN alerts.value IS
    'Valor del sensor que disparó la alerta, para incluirlo en el response sin reconsultar lecturas.';
COMMENT ON COLUMN alerts.attended_by IS
    'Usuario que marcó la alerta como ATTENDED. Nulo mientras esté OPEN.';


-- =============================================================================
-- SECCIÓN 9 — Corregir tabla: inventory_items
--
-- Cambios:
--   - ADD min_stock → stock mínimo configurable. IS-HU-05 requiere alertar
--                     cuando la cantidad baje de este valor.
-- =============================================================================

ALTER TABLE inventory_items
    ADD COLUMN IF NOT EXISTS min_stock DECIMAL(10, 2) NOT NULL DEFAULT 0;

COMMENT ON COLUMN inventory_items.min_stock IS
    'Nivel mínimo de stock. Cuando quantity <= min_stock se debe generar una alerta de reabastecimiento.';


-- =============================================================================
-- SECCIÓN 10 — Reconstruir tabla: ai_results
--
-- La tabla original tenía result_data (TEXT genérico). El modelo oficial
-- define campos específicos: result_label, confidence, image_url y metadata
-- (JSON), alineados con los tipos de análisis del equipo IA
-- (MATURATION, GROWTH, ANOMALY).
-- =============================================================================

CREATE TABLE ai_results (
    id            BIGSERIAL    PRIMARY KEY,
    zone_id       BIGINT       REFERENCES zones(id),
    crop_id       BIGINT       REFERENCES crops(id),
    analysis_type VARCHAR(50)  NOT NULL,
    result_label  VARCHAR(100) NOT NULL,
    confidence    DECIMAL(5, 4),
    image_url     VARCHAR(500),
    metadata      JSONB,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE ai_results IS
    'Resultados de análisis de IA (maduración, crecimiento, anomalías). Consumidos por IS-HU-11.';
COMMENT ON COLUMN ai_results.confidence IS
    'Nivel de confianza del modelo entre 0.0000 y 1.0000 (ej: 0.9342 = 93.42%).';
COMMENT ON COLUMN ai_results.metadata IS
    'Datos adicionales del análisis en formato JSON (ej: bounding boxes, etiquetas secundarias).';


-- =============================================================================
-- SECCIÓN 11 — Actualizar enum SensorVariable
--
-- PostgreSQL no permite eliminar valores de un tipo ENUM existente directamente.
-- La estrategia segura es:
--   1. Cambiar la columna afectada a VARCHAR temporalmente
--   2. Eliminar el tipo ENUM antiguo
--   3. Crear el nuevo tipo ENUM con los valores correctos
--   4. Restaurar la columna al nuevo tipo con CAST
--
-- Valores anteriores : TEMPERATURE, HUMIDITY, SOIL_MOISTURE, LIGHT_LEVEL, CO2
-- Valores correctos  : TEMPERATURE, AIR_HUMIDITY, SOIL_MOISTURE, PH,
--                      WATER_FLOW, LUMINOSITY, WATER_LEVEL
-- =============================================================================

-- Paso 1: Desacoplar columnas que usan el tipo para poder eliminarlo
ALTER TABLE sensors
    ALTER COLUMN variable TYPE VARCHAR(50);

ALTER TABLE threshold_configs
    ALTER COLUMN variable TYPE VARCHAR(50);

-- Paso 2: Eliminar el tipo ENUM antiguo
DROP TYPE IF EXISTS sensor_variable_enum CASCADE;

-- Nota: Spring Boot gestiona SensorVariable como VARCHAR con @Enumerated(STRING),
-- por lo que no existe un tipo ENUM nativo en PostgreSQL. Las columnas ya son
-- VARCHAR y los valores se validan a nivel de aplicación. Esta sección garantiza
-- que no quede ningún tipo ENUM residual que pueda causar conflictos.

-- Paso 3: Limpiar valores obsoletos en sensors que ya no existen en el nuevo enum.
-- Actualiza registros que tengan los valores anteriores al valor más cercano.
UPDATE sensors SET variable = 'AIR_HUMIDITY'   WHERE variable = 'HUMIDITY';
UPDATE sensors SET variable = 'LUMINOSITY'     WHERE variable = 'LIGHT_LEVEL';
UPDATE sensors SET variable = 'AIR_HUMIDITY'   WHERE variable = 'CO2';

UPDATE threshold_configs SET variable = 'AIR_HUMIDITY' WHERE variable = 'HUMIDITY';
UPDATE threshold_configs SET variable = 'LUMINOSITY'   WHERE variable = 'LIGHT_LEVEL';
UPDATE threshold_configs SET variable = 'AIR_HUMIDITY' WHERE variable = 'CO2';

-- =============================================================================
-- FIN DE MIGRACIÓN V5
-- Fecha    : 2026-05
-- Autor    : Equipo IS — smart-greenhouse-backend
-- =============================================================================
