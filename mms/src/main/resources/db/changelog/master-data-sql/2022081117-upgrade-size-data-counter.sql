BEGIN;
SET FOREIGN_KEY_CHECKS=0;
ALTER TABLE DATA_COUNTER MODIFY COLUMN CYCLE_TIMES LONGTEXT;
SET FOREIGN_KEY_CHECKS=1;
COMMIT;