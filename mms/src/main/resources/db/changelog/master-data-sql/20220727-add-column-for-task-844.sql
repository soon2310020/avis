BEGIN;
ALTER TABLE MOLD ADD COLUMN `LAST_MAINTENANCE_DATE` DATETIME;
ALTER TABLE MOLD ADD COLUMN `LAST_REFURBISHMENT_DATE` DATETIME;
COMMIT;