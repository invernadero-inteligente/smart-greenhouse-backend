-- Add is_active column to threshold_configs table
ALTER TABLE threshold_configs
ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT true;

