BEGIN;
ALTER TABLE `WORK_ORDER`
    ADD COLUMN REQUEST_APPROVAL VARCHAR(1) DEFAULT NULL;
COMMIT;