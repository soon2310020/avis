BEGIN;
ALTER TABLE `MACHINE_OEE` ADD COLUMN REJECTED_PART BIGINT(20)  DEFAULT NULL;
ALTER TABLE `MACHINE_DOWNTIME_ALERT` ADD COLUMN MOLD_ID BIGINT(20)  DEFAULT NULL;
COMMIT;
