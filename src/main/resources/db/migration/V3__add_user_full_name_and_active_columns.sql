-- V3__add_user_full_name_and_active_columns.sql

ALTER TABLE users
    ADD COLUMN full_name VARCHAR(255) NOT NULL DEFAULT '';

ALTER TABLE users
    ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;
