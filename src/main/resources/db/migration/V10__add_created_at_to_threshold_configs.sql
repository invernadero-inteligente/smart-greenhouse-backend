-- V10: Add created_at to threshold_configs if missing
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'threshold_configs'
          AND column_name = 'created_at'
    ) THEN
        ALTER TABLE threshold_configs
        ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
END
$$;
