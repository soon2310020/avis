BEGIN;

ALTER TABLE JOB_DATA 
	ADD COLUMN `JOB_DETAIL` longtext;

COMMIT;