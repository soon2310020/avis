BEGIN;
ALTER TABLE `REJECTED_PART_DETAILS` ADD COLUMN IS_DEFAULT VARCHAR(1) DEFAULT NULL;
UPDATE `REJECTED_PART_DETAILS` SET IS_DEFAULT = 'Y';
COMMIT;