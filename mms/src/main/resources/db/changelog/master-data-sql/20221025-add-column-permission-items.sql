BEGIN;

ALTER TABLE PERMISSION 
	ADD COLUMN `ITEMS` LONGTEXT;

COMMIT;