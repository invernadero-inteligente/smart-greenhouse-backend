-- V9: Ensure threshold_configs has is_active column (idempotent)
-- Adds the column only if it does not exist, backfills existing rows,
-- sets a DEFAULT and enforces NOT NULL.
DO $$
BEGIN
	IF NOT EXISTS (
		SELECT 1
		FROM information_schema.columns
		WHERE table_schema = current_schema()
		  AND table_name = 'threshold_configs'
		  AND column_name = 'is_active'
	) THEN
		ALTER TABLE threshold_configs
		ADD COLUMN is_active BOOLEAN;

		-- Backfill any existing rows with the default value
		UPDATE threshold_configs
		SET is_active = true
		WHERE is_active IS NULL;

		-- Set the default and enforce NOT NULL after backfill
		ALTER TABLE threshold_configs
		ALTER COLUMN is_active SET DEFAULT true,
		ALTER COLUMN is_active SET NOT NULL;
	END IF;
END
$$;


