BEGIN;
ALTER TABLE `PRODUCED_PART` ADD COLUMN REPORTED_BY_ID BIGINT(20);
COMMIT;