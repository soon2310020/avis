BEGIN;
ALTER TABLE `MACHINE_STATISTICS` ADD COLUMN DAILY_WORKING_HOUR_EDITED_AT datetime DEFAULT NULL;
COMMIT;