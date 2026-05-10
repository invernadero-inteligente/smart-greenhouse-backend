-- V1__init_schema.sql

-- 1. users
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- 2. zones
CREATE TABLE zones (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    area DECIMAL(10, 2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- 3. crops
CREATE TABLE crops (
    id BIGSERIAL PRIMARY KEY,
    zone_id BIGINT NOT NULL REFERENCES zones(id),
    name VARCHAR(100) NOT NULL,
    crop_type VARCHAR(100) NOT NULL,
    planted_at TIMESTAMP,
    expected_harvest_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- 4. crop_conditions
CREATE TABLE crop_conditions (
    id BIGSERIAL PRIMARY KEY,
    crop_id BIGINT NOT NULL REFERENCES crops(id),
    condition_date TIMESTAMP NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- 5. sensors
CREATE TABLE sensors (
    id BIGSERIAL PRIMARY KEY,
    zone_id BIGINT NOT NULL REFERENCES zones(id),
    serial_number VARCHAR(100) NOT NULL UNIQUE,
    variable VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- 6. threshold_configs
CREATE TABLE threshold_configs (
    id BIGSERIAL PRIMARY KEY,
    zone_id BIGINT NOT NULL REFERENCES zones(id),
    sensor_variable VARCHAR(50) NOT NULL,
    min_value DECIMAL(10, 2),
    max_value DECIMAL(10, 2),
    created_by_user_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- 7. threshold_change_history
CREATE TABLE threshold_change_history (
    id BIGSERIAL PRIMARY KEY,
    threshold_config_id BIGINT NOT NULL REFERENCES threshold_configs(id),
    old_min_value DECIMAL(10, 2),
    old_max_value DECIMAL(10, 2),
    new_min_value DECIMAL(10, 2),
    new_max_value DECIMAL(10, 2),
    changed_by_user_id BIGINT REFERENCES users(id),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 8. alerts
CREATE TABLE alerts (
    id BIGSERIAL PRIMARY KEY,
    origin VARCHAR(50) NOT NULL,
    severity VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    zone_id BIGINT REFERENCES zones(id),
    sensor_id BIGINT REFERENCES sensors(id),
    crop_id BIGINT REFERENCES crops(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP,
    resolved_by_user_id BIGINT REFERENCES users(id)
);

-- 9. inventory_items
CREATE TABLE inventory_items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    quantity DECIMAL(10, 2) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- 10. ai_results
CREATE TABLE ai_results (
    id BIGSERIAL PRIMARY KEY,
    analysis_type VARCHAR(50) NOT NULL,
    result_data TEXT NOT NULL,
    zone_id BIGINT REFERENCES zones(id),
    crop_id BIGINT REFERENCES crops(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
