BEGIN;

ALTER TABLE MOLD modify COLUMN COST DOUBLE;

ALTER TABLE MOLD_VERSION modify COLUMN COST DOUBLE;

COMMIT;