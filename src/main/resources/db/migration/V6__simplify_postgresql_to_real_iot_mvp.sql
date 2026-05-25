-- V6__simplify_postgresql_to_real_iot_mvp.sql

-- =============================================================================
-- FASE 2: MIGRACIÓN POSTGRESQL V6 - Arquitectura Híbrida IoT
-- =============================================================================

-- 1. Eliminar FK o columnas dependientes (alerts.sensor_id) antes de eliminar tablas
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='alerts' AND column_name='sensor_id') THEN
        ALTER TABLE alerts DROP COLUMN sensor_id;
    END IF;
END $$;

-- 2. Eliminar tablas obsoletas (sin CASCADE para evitar borrados accidentales de otras dependencias ocultas)
DROP TABLE IF EXISTS threshold_change_history;
DROP TABLE IF EXISTS irrigation_events;
DROP TABLE IF EXISTS actuator_events;
DROP TABLE IF EXISTS sensors;

-- 3. Ajustar users
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='users' AND column_name='full_name') THEN
        ALTER TABLE users ADD COLUMN full_name VARCHAR(255) NOT NULL DEFAULT '';
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='users' AND column_name='password') AND 
       NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='users' AND column_name='password_hash') THEN
        ALTER TABLE users RENAME COLUMN password TO password_hash;
    ELSIF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='users' AND column_name='password_hash') THEN
        ALTER TABLE users ADD COLUMN password_hash VARCHAR(255) NOT NULL DEFAULT '';
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='users' AND column_name='active') AND 
       NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='users' AND column_name='is_active') THEN
        ALTER TABLE users RENAME COLUMN active TO is_active;
    ELSIF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='users' AND column_name='is_active') THEN
        ALTER TABLE users ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;
    END IF;
END $$;

-- 4. Ajustar zones
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='zones' AND column_name='is_active') THEN
        ALTER TABLE zones ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='zones' AND column_name='area') THEN
        ALTER TABLE zones DROP COLUMN area;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c JOIN pg_class t ON c.conrelid = t.oid WHERE t.relname = 'zones' AND c.contype = 'u' AND (c.conname = 'zones_name_key' OR c.conname = 'uq_zones_name')) THEN
        ALTER TABLE zones ADD CONSTRAINT uq_zones_name UNIQUE (name);
    END IF;
EXCEPTION WHEN duplicate_table THEN NULL; WHEN duplicate_object THEN NULL;
END $$;

-- 5. Ajustar crops
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='crops' AND column_name='crop_type') AND 
       NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='crops' AND column_name='variety') THEN
        ALTER TABLE crops RENAME COLUMN crop_type TO variety;
    ELSIF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='crops' AND column_name='variety') THEN
        ALTER TABLE crops ADD COLUMN variety VARCHAR(100);
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='crops' AND column_name='planted_at') AND 
       NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='crops' AND column_name='sowing_date') THEN
        ALTER TABLE crops RENAME COLUMN planted_at TO sowing_date;
        ALTER TABLE crops ALTER COLUMN sowing_date TYPE DATE USING sowing_date::date;
    ELSIF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='crops' AND column_name='sowing_date') THEN
        ALTER TABLE crops ADD COLUMN sowing_date DATE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='crops' AND column_name='plant_count') THEN
        ALTER TABLE crops ADD COLUMN plant_count INT NOT NULL DEFAULT 0;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='crops' AND column_name='expected_harvest_at') THEN
        ALTER TABLE crops DROP COLUMN expected_harvest_at;
    END IF;
END $$;

-- 6. Ajustar crop_conditions
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='crop_conditions' AND column_name='temperature_min') THEN
        ALTER TABLE crop_conditions ADD COLUMN temperature_min DECIMAL(10,2);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='crop_conditions' AND column_name='temperature_max') THEN
        ALTER TABLE crop_conditions ADD COLUMN temperature_max DECIMAL(10,2);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='crop_conditions' AND column_name='air_humidity_min') THEN
        ALTER TABLE crop_conditions ADD COLUMN air_humidity_min DECIMAL(10,2);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='crop_conditions' AND column_name='air_humidity_max') THEN
        ALTER TABLE crop_conditions ADD COLUMN air_humidity_max DECIMAL(10,2);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='crop_conditions' AND column_name='soil_moisture_min') THEN
        ALTER TABLE crop_conditions ADD COLUMN soil_moisture_min DECIMAL(10,2);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='crop_conditions' AND column_name='soil_moisture_max') THEN
        ALTER TABLE crop_conditions ADD COLUMN soil_moisture_max DECIMAL(10,2);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='crop_conditions' AND column_name='ph_min') THEN
        ALTER TABLE crop_conditions ADD COLUMN ph_min DECIMAL(10,2);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='crop_conditions' AND column_name='ph_max') THEN
        ALTER TABLE crop_conditions ADD COLUMN ph_max DECIMAL(10,2);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='crop_conditions' AND column_name='updated_at') THEN
        ALTER TABLE crop_conditions ADD COLUMN updated_at TIMESTAMP;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='crop_conditions' AND column_name='condition_date') THEN
        ALTER TABLE crop_conditions DROP COLUMN condition_date;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='crop_conditions' AND column_name='notes') THEN
        ALTER TABLE crop_conditions DROP COLUMN notes;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c JOIN pg_class t ON c.conrelid = t.oid WHERE t.relname = 'crop_conditions' AND c.contype = 'u' AND c.conkey[1] = (SELECT a.attnum FROM pg_attribute a WHERE a.attrelid = t.oid AND a.attname = 'crop_id')) THEN
        ALTER TABLE crop_conditions ADD CONSTRAINT uq_crop_conditions_crop_id UNIQUE (crop_id);
    END IF;
EXCEPTION WHEN duplicate_table THEN NULL; WHEN duplicate_object THEN NULL;
END $$;

-- 7. Ajustar threshold_configs
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='threshold_configs' AND column_name='sensor_variable') AND 
       NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='threshold_configs' AND column_name='variable_name') THEN
        ALTER TABLE threshold_configs RENAME COLUMN sensor_variable TO variable_name;
    ELSIF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='threshold_configs' AND column_name='variable') AND 
          NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='threshold_configs' AND column_name='variable_name') THEN
        ALTER TABLE threshold_configs RENAME COLUMN variable TO variable_name;
    ELSIF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='threshold_configs' AND column_name='variable_name') THEN
        ALTER TABLE threshold_configs ADD COLUMN variable_name VARCHAR(100) NOT NULL DEFAULT 'unknown';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='threshold_configs' AND column_name='unit') THEN
        ALTER TABLE threshold_configs ADD COLUMN unit VARCHAR(50) NOT NULL DEFAULT 'N/A';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='threshold_configs' AND column_name='updated_by') THEN
        ALTER TABLE threshold_configs ADD COLUMN updated_by BIGINT REFERENCES users(id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='threshold_configs' AND column_name='updated_at') THEN
        ALTER TABLE threshold_configs ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='threshold_configs' AND column_name='created_by_user_id') THEN
        ALTER TABLE threshold_configs DROP COLUMN created_by_user_id;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='threshold_configs' AND column_name='created_at') THEN
        ALTER TABLE threshold_configs DROP COLUMN created_at;
    END IF;

    -- Asegurar min_value y max_value NOT NULL
    UPDATE threshold_configs SET min_value = 0.00 WHERE min_value IS NULL;
    UPDATE threshold_configs SET max_value = 0.00 WHERE max_value IS NULL;
    ALTER TABLE threshold_configs ALTER COLUMN min_value SET NOT NULL;
    ALTER TABLE threshold_configs ALTER COLUMN max_value SET NOT NULL;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint c JOIN pg_class t ON c.conrelid = t.oid WHERE t.relname = 'threshold_configs' AND c.conname = 'uq_threshold_zone_variable') THEN
        ALTER TABLE threshold_configs DROP CONSTRAINT uq_threshold_zone_variable;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c JOIN pg_class t ON c.conrelid = t.oid WHERE t.relname = 'threshold_configs' AND c.conname = 'uq_threshold_zone_variable_name') THEN
        ALTER TABLE threshold_configs ADD CONSTRAINT uq_threshold_zone_variable_name UNIQUE (zone_id, variable_name);
    END IF;
EXCEPTION WHEN duplicate_table THEN NULL; WHEN duplicate_object THEN NULL;
END $$;

-- 8. Ajustar alerts
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='alerts' AND column_name='variable') AND 
       NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='alerts' AND column_name='variable_name') THEN
        ALTER TABLE alerts RENAME COLUMN variable TO variable_name;
    ELSIF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='alerts' AND column_name='variable_name') THEN
        ALTER TABLE alerts ADD COLUMN variable_name VARCHAR(100) NOT NULL DEFAULT 'unknown';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='alerts' AND column_name='unit') THEN
        ALTER TABLE alerts ADD COLUMN unit VARCHAR(50) NOT NULL DEFAULT 'N/A';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='alerts' AND column_name='value') THEN
        ALTER TABLE alerts ADD COLUMN value DECIMAL(10,2);
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='alerts' AND column_name='resolved_by_user_id') AND 
       NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='alerts' AND column_name='attended_by') THEN
        ALTER TABLE alerts RENAME COLUMN resolved_by_user_id TO attended_by;
    ELSIF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='alerts' AND column_name='attended_by') THEN
        ALTER TABLE alerts ADD COLUMN attended_by BIGINT REFERENCES users(id);
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='alerts' AND column_name='resolved_at') AND 
       NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='alerts' AND column_name='attended_at') THEN
        ALTER TABLE alerts RENAME COLUMN resolved_at TO attended_at;
    ELSIF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='alerts' AND column_name='attended_at') THEN
        ALTER TABLE alerts ADD COLUMN attended_at TIMESTAMP;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='alerts' AND column_name='zone_id') THEN
        ALTER TABLE alerts ADD COLUMN zone_id BIGINT REFERENCES zones(id);
    END IF;
END $$;

-- 9. Ajustar actuators
CREATE TABLE IF NOT EXISTS actuators (
    id BIGSERIAL PRIMARY KEY,
    zone_id BIGINT NOT NULL REFERENCES zones(id),
    name VARCHAR(100) NOT NULL,
    current_action VARCHAR(10) NOT NULL DEFAULT 'OFF',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='actuators' AND column_name='zone_id') THEN
        ALTER TABLE actuators ADD COLUMN zone_id BIGINT REFERENCES zones(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='actuators' AND column_name='name') THEN
        ALTER TABLE actuators ADD COLUMN name VARCHAR(100) NOT NULL DEFAULT 'unknown';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='actuators' AND column_name='current_action') THEN
        ALTER TABLE actuators ADD COLUMN current_action VARCHAR(10) NOT NULL DEFAULT 'OFF';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='actuators' AND column_name='created_at') THEN
        ALTER TABLE actuators ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='actuators' AND column_name='updated_at') THEN
        ALTER TABLE actuators ADD COLUMN updated_at TIMESTAMP;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='actuators' AND column_name='current_state') THEN
        UPDATE actuators SET current_action = 'ON' WHERE current_state = 'true' OR current_state = '1';
        UPDATE actuators SET current_action = 'OFF' WHERE current_state = 'false' OR current_state = '0';
        ALTER TABLE actuators DROP COLUMN current_state;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='actuators' AND column_name='type') THEN
        ALTER TABLE actuators DROP COLUMN type;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='actuators' AND column_name='status') THEN
        ALTER TABLE actuators DROP COLUMN status;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c JOIN pg_class t ON c.conrelid = t.oid WHERE t.relname = 'actuators' AND c.conname = 'uq_actuators_zone_name') THEN
        ALTER TABLE actuators ADD CONSTRAINT uq_actuators_zone_name UNIQUE(zone_id, name);
    END IF;
EXCEPTION WHEN duplicate_table THEN NULL; WHEN duplicate_object THEN NULL;
END $$;

-- 10. Ajustar ai_results
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='ai_results' AND column_name='analysis_type') THEN
        ALTER TABLE ai_results ADD COLUMN analysis_type VARCHAR(50) NOT NULL DEFAULT 'MATURATION';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='ai_results' AND column_name='result_label') THEN
        ALTER TABLE ai_results ADD COLUMN result_label VARCHAR(100) NOT NULL DEFAULT 'N/A';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='ai_results' AND column_name='confidence') THEN
        ALTER TABLE ai_results ADD COLUMN confidence DECIMAL(5,4) NOT NULL DEFAULT 0.0000;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='ai_results' AND column_name='image_url') THEN
        ALTER TABLE ai_results ADD COLUMN image_url VARCHAR(500);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='ai_results' AND column_name='metadata') THEN
        ALTER TABLE ai_results ADD COLUMN metadata JSONB;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='ai_results' AND column_name='zone_id') THEN
        ALTER TABLE ai_results ADD COLUMN zone_id BIGINT REFERENCES zones(id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='ai_results' AND column_name='crop_id') THEN
        ALTER TABLE ai_results ADD COLUMN crop_id BIGINT REFERENCES crops(id);
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='ai_results' AND column_name='result_data') THEN
        UPDATE ai_results SET result_label = SUBSTRING(result_data, 1, 100) WHERE result_label = 'N/A';
        ALTER TABLE ai_results DROP COLUMN result_data;
    END IF;
END $$;

-- 11. Ajustar inventory_items
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='inventory_items' AND column_name='min_stock') THEN
        ALTER TABLE inventory_items ADD COLUMN min_stock DECIMAL(10,2) NOT NULL DEFAULT 0;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='inventory_items' AND column_name='quantity') THEN
        ALTER TABLE inventory_items ADD COLUMN quantity DECIMAL(10,2) NOT NULL DEFAULT 0;
    ELSE
        UPDATE inventory_items SET quantity = 0 WHERE quantity IS NULL;
        ALTER TABLE inventory_items ALTER COLUMN quantity SET NOT NULL;
    END IF;
END $$;

-- 12. Agregar Índices
CREATE INDEX IF NOT EXISTS idx_alerts_zone_status_created_at ON alerts(zone_id, status, created_at);
CREATE INDEX IF NOT EXISTS idx_ai_results_zone_crop_created_at ON ai_results(zone_id, crop_id, created_at);
CREATE INDEX IF NOT EXISTS idx_threshold_configs_zone_variable_name ON threshold_configs(zone_id, variable_name);

-- Fin de V6
