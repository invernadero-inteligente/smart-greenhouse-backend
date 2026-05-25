-- V2__add_updated_at_to_crop_conditions.sql
-- Agrega la columna updated_at a crop_conditions para alinear con AuditableEntity

ALTER TABLE crop_conditions
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
