BEGIN;
ALTER TABLE `MOLD_LOCATION` ADD COLUMN PREVIOUS_LOCATION_ID BIGINT DEFAULT NULL;
COMMIT;