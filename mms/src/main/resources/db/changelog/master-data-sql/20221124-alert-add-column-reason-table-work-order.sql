BEGIN;
ALTER TABLE `WORK_ORDER` ADD COLUMN REJECTED_CHANGE_REASON LONGTEXT DEFAULT NULL;
ALTER TABLE `WORK_ORDER` ADD COLUMN DECLINED_REASON LONGTEXT DEFAULT NULL;
ALTER TABLE `WORK_ORDER` ADD COLUMN CANCELLED_REASON LONGTEXT DEFAULT NULL;
COMMIT;