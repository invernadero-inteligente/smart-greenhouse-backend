-- V7__revert_user_column_names.sql

-- =============================================================================
-- FASE 2.5: REVERSIÓN DE NOMBRES DE COLUMNAS EN USERS
-- =============================================================================

DO $$
BEGIN
    -- Revert password_hash to password
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='users' AND column_name='password_hash') AND 
       NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='users' AND column_name='password') THEN
        ALTER TABLE users RENAME COLUMN password_hash TO password;
    END IF;
    
    -- Revert is_active to active
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='users' AND column_name='is_active') AND 
       NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='users' AND column_name='active') THEN
        ALTER TABLE users RENAME COLUMN is_active TO active;
    END IF;
END $$;
