BEGIN;
ALTER TABLE MOLD ADD COLUMN `LAST_SHOT_AT_VAL` DATETIME;
ALTER TABLE COUNTER ADD COLUMN `LAST_SHOT_AT_VAL` DATETIME;
COMMIT;