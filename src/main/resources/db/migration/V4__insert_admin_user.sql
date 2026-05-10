-- V4__insert_admin.sql
-- Usuario administrador inicial del sistema
-- Contraseña: Admin123456
INSERT INTO users (email, password, role, active, full_name, created_at, updated_at)
VALUES (
    'admin1@invernadero.com',
    '$2a$10$Iuy9WuRzRru825wAcisxk.s8/tp29VKg9Z2.MSBVlPuaT2zshWIS6',
    'ADMIN',
    true,
    'Administrador del Sistema',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);