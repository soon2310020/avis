BEGIN ;

ALTER TABLE `NOTIFICATION` ADD COLUMN `IS_REPLY` VARCHAR(1) DEFAULT 'N';

COMMIT ;