BEGIN;
ALTER TABLE `MOLD` ADD COLUMN `DAY` VARCHAR(20) DEFAULT NULL;
ALTER TABLE `MOLD` ADD COLUMN `WEEK` VARCHAR(20) DEFAULT NULL;
ALTER TABLE `MOLD` ADD COLUMN `MONTH` VARCHAR(20) DEFAULT NULL;
COMMIT;