BEGIN;
ALTER TABLE PART ADD COLUMN QUANTITY_REQUIRED BIGINT(20) DEFAULT NULL;
COMMIT;