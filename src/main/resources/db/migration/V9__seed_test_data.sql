-- =============================================================================
-- V9__seed_test_data.sql
-- Datos de prueba para desarrollo — zonas, cultivos, inventario, umbrales y alertas
-- Se usa ON CONFLICT DO NOTHING para que sea idempotente.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. ZONAS
-- -----------------------------------------------------------------------------
INSERT INTO zones (name, description, is_active, created_at, updated_at)
VALUES
    ('Zona A - Tomates',     'Invernadero principal, cultivo de tomates cherry', true, NOW(), NOW()),
    ('Zona B - Lechugas',    'Zona de cultivo hidropónico de lechugas', true, NOW(), NOW()),
    ('Zona C - Pimientos',   'Zona de pimientos rojos y verdes', true, NOW(), NOW()),
    ('Zona D - Pepinos',     'Zona de pepinos en sustrato', false, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

-- -----------------------------------------------------------------------------
-- 2. CULTIVOS
-- -----------------------------------------------------------------------------
INSERT INTO crops (zone_id, name, variety, plant_count, sowing_date, status, created_at, updated_at)
SELECT z.id, 'Tomate Cherry', 'Cerise F1', 120, '2026-03-15', 'ACTIVE', NOW(), NOW()
FROM zones z WHERE z.name = 'Zona A - Tomates'
ON CONFLICT DO NOTHING;

INSERT INTO crops (zone_id, name, variety, plant_count, sowing_date, status, created_at, updated_at)
SELECT z.id, 'Lechuga Mantequilla', 'Butterhead', 200, '2026-04-01', 'ACTIVE', NOW(), NOW()
FROM zones z WHERE z.name = 'Zona B - Lechugas'
ON CONFLICT DO NOTHING;

INSERT INTO crops (zone_id, name, variety, plant_count, sowing_date, status, created_at, updated_at)
SELECT z.id, 'Pimiento Rojo', 'California Wonder', 80, '2026-02-20', 'ACTIVE', NOW(), NOW()
FROM zones z WHERE z.name = 'Zona C - Pimientos'
ON CONFLICT DO NOTHING;

INSERT INTO crops (zone_id, name, variety, plant_count, sowing_date, status, created_at, updated_at)
SELECT z.id, 'Pepino Europeo', 'Long English', 60, '2026-01-10', 'HARVEST', NOW(), NOW()
FROM zones z WHERE z.name = 'Zona D - Pepinos'
ON CONFLICT DO NOTHING;

-- -----------------------------------------------------------------------------
-- 3. CONDICIONES DE CULTIVOS (crop_conditions)
-- -----------------------------------------------------------------------------
INSERT INTO crop_conditions (crop_id, temperature_min, temperature_max, air_humidity_min, air_humidity_max,
                              soil_moisture_min, soil_moisture_max, ph_min, ph_max, created_at, updated_at)
SELECT c.id, 18.00, 28.00, 60.00, 85.00, 55.00, 80.00, 6.00, 7.00, NOW(), NOW()
FROM crops c WHERE c.name = 'Tomate Cherry'
ON CONFLICT DO NOTHING;

INSERT INTO crop_conditions (crop_id, temperature_min, temperature_max, air_humidity_min, air_humidity_max,
                              soil_moisture_min, soil_moisture_max, ph_min, ph_max, created_at, updated_at)
SELECT c.id, 15.00, 22.00, 55.00, 80.00, 50.00, 75.00, 6.00, 7.00, NOW(), NOW()
FROM crops c WHERE c.name = 'Lechuga Mantequilla'
ON CONFLICT DO NOTHING;

INSERT INTO crop_conditions (crop_id, temperature_min, temperature_max, air_humidity_min, air_humidity_max,
                              soil_moisture_min, soil_moisture_max, ph_min, ph_max, created_at, updated_at)
SELECT c.id, 20.00, 30.00, 60.00, 80.00, 55.00, 75.00, 6.00, 6.80, NOW(), NOW()
FROM crops c WHERE c.name = 'Pimiento Rojo'
ON CONFLICT DO NOTHING;

-- -----------------------------------------------------------------------------
-- 4. UMBRALES (threshold_configs)
-- -----------------------------------------------------------------------------
INSERT INTO threshold_configs (zone_id, variable_name, unit, min_value, max_value, updated_at)
SELECT z.id, 'temperatura', '°C', 18.00, 28.00, NOW()
FROM zones z WHERE z.name = 'Zona A - Tomates'
ON CONFLICT (zone_id, variable_name) DO NOTHING;

INSERT INTO threshold_configs (zone_id, variable_name, unit, min_value, max_value, updated_at)
SELECT z.id, 'humedad_aire', '%', 60.00, 85.00, NOW()
FROM zones z WHERE z.name = 'Zona A - Tomates'
ON CONFLICT (zone_id, variable_name) DO NOTHING;

INSERT INTO threshold_configs (zone_id, variable_name, unit, min_value, max_value, updated_at)
SELECT z.id, 'humedad_suelo', '%', 55.00, 80.00, NOW()
FROM zones z WHERE z.name = 'Zona A - Tomates'
ON CONFLICT (zone_id, variable_name) DO NOTHING;

INSERT INTO threshold_configs (zone_id, variable_name, unit, min_value, max_value, updated_at)
SELECT z.id, 'temperatura', '°C', 15.00, 22.00, NOW()
FROM zones z WHERE z.name = 'Zona B - Lechugas'
ON CONFLICT (zone_id, variable_name) DO NOTHING;

INSERT INTO threshold_configs (zone_id, variable_name, unit, min_value, max_value, updated_at)
SELECT z.id, 'humedad_aire', '%', 55.00, 80.00, NOW()
FROM zones z WHERE z.name = 'Zona B - Lechugas'
ON CONFLICT (zone_id, variable_name) DO NOTHING;

INSERT INTO threshold_configs (zone_id, variable_name, unit, min_value, max_value, updated_at)
SELECT z.id, 'temperatura', '°C', 20.00, 30.00, NOW()
FROM zones z WHERE z.name = 'Zona C - Pimientos'
ON CONFLICT (zone_id, variable_name) DO NOTHING;

-- -----------------------------------------------------------------------------
-- 5. INVENTARIO (inventory_items)
-- -----------------------------------------------------------------------------
INSERT INTO inventory_items (name, category, quantity, unit, min_stock, updated_at)
VALUES
    ('Abono NPK 15-15-15',       'FERTILIZER', 45.50,  'kg',       20.00,  NOW()),
    ('Semillas Tomate Cherry',   'SEEDS',        2.80,  'kg',        1.00,  NOW()),
    ('Semillas Lechuga',         'SEEDS',        1.20,  'kg',        0.50,  NOW()),
    ('Fungicida Cobre',          'PESTICIDE',    8.00,  'L',         5.00,  NOW()),
    ('Insecticida Orgánico',     'PESTICIDE',    3.50,  'L',         4.00,  NOW()),
    ('Tijeras de Poda',          'TOOLS',       12.00,  'unidades',  3.00,  NOW()),
    ('Manguera 20m',             'TOOLS',        4.00,  'unidades',  2.00,  NOW()),
    ('Sustrato Universal',       'OTHER',      120.00,  'kg',       50.00,  NOW()),
    ('Macetas 5L',               'OTHER',       35.00,  'unidades', 20.00,  NOW()),
    ('Cal Agrícola',             'FERTILIZER',  18.00,  'kg',       25.00,  NOW()),
    ('Urea Granulada',           'FERTILIZER',  60.00,  'kg',       30.00,  NOW()),
    ('Cinta de Riego Goteo',     'TOOLS',        1.50,  'rollos',    1.00,  NOW())
ON CONFLICT DO NOTHING;

-- -----------------------------------------------------------------------------
-- 6. ALERTAS (alerts)
-- -----------------------------------------------------------------------------
INSERT INTO alerts (zone_id, origin, variable_name, unit, severity, message, value, status, created_at)
SELECT z.id, 'IOT', 'temperatura', '°C', 'HIGH',
       'Temperatura crítica — supera el umbral máximo de 28°C en Zona A', 32.40, 'OPEN', NOW() - INTERVAL '2 hours'
FROM zones z WHERE z.name = 'Zona A - Tomates'
ON CONFLICT DO NOTHING;

INSERT INTO alerts (zone_id, origin, variable_name, unit, severity, message, value, status, created_at)
SELECT z.id, 'IOT', 'humedad_suelo', '%', 'MEDIUM',
       'Humedad del suelo por debajo del mínimo recomendado en Zona A', 48.00, 'OPEN', NOW() - INTERVAL '45 minutes'
FROM zones z WHERE z.name = 'Zona A - Tomates'
ON CONFLICT DO NOTHING;

INSERT INTO alerts (zone_id, origin, variable_name, unit, severity, message, value, status, created_at)
SELECT z.id, 'IOT', 'humedad_aire', '%', 'LOW',
       'Humedad del aire ligeramente por debajo del mínimo en Zona B', 52.00, 'OPEN', NOW() - INTERVAL '20 minutes'
FROM zones z WHERE z.name = 'Zona B - Lechugas'
ON CONFLICT DO NOTHING;

INSERT INTO alerts (zone_id, origin, variable_name, unit, severity, message, value, status, created_at, attended_at)
SELECT z.id, 'IA', 'temperatura', '°C', 'MEDIUM',
       'IA detectó tendencia de temperatura ascendente fuera de rango en Zona C', 31.10, 'ATTENDED', NOW() - INTERVAL '3 hours', NOW() - INTERVAL '1 hour'
FROM zones z WHERE z.name = 'Zona C - Pimientos'
ON CONFLICT DO NOTHING;

INSERT INTO alerts (zone_id, origin, variable_name, unit, severity, message, value, status, created_at, attended_at)
SELECT z.id, 'IOT', 'humedad_suelo', '%', 'HIGH',
       'Humedad del suelo criticamente baja en Zona C — riesgo de estrés hídrico', 35.00, 'ATTENDED', NOW() - INTERVAL '6 hours', NOW() - INTERVAL '4 hours'
FROM zones z WHERE z.name = 'Zona C - Pimientos'
ON CONFLICT DO NOTHING;
