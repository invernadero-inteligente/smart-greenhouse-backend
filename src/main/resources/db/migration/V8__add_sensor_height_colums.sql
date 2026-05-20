-- V8__add_sensor_height_colums.sql
-- =============================================================================
-- Añadir columna sensor_height a la tabla crops
--
-- Propósito: Registrar la altura a la cual está instalado el sensor en relación
--            con el cultivo. Útil para calibración y análisis de lecturas IoT.
--
-- Especificación:
--   - Tipo: DECIMAL(5, 2) para soportar valores hasta 999.99 metros/centímetros
--   - Nuleable: SÍ (un cultivo existente puede no tener esta información)
--   - Unidad: Se recomienda usar cm o metros según configuración del sistema
-- =============================================================================

ALTER TABLE crops
    ADD COLUMN IF NOT EXISTS sensor_height DECIMAL(5, 2);

COMMENT ON COLUMN crops.sensor_height IS
    'Altura en la que está instalado el sensor relativo al cultivo (en cm). Usado para calibración de lecturas IoT.';

