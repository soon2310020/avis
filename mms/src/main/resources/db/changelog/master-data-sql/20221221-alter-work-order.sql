BEGIN;
ALTER TABLE `WORK_ORDER`
    ADD COLUMN `REPORT_FAILURE_SHOT` INT DEFAULT NULL;
ALTER TABLE `WORK_ORDER`
    ADD COLUMN `START_WORK_ORDER_SHOT` INT DEFAULT NULL;
ALTER TABLE `WORK_ORDER`
    MODIFY `STATUS` VARCHAR(100) DEFAULT NULL;
COMMIT;