BEGIN;

ALTER TABLE `ROLE`
	ADD COLUMN EMOLDINO_ENABLED varchar(1),
	ADD COLUMN OEM_ENABLED varchar(1),
	ADD COLUMN SUPPLIER_ENABLED varchar(1),
	ADD COLUMN TOOLMAKER_ENABLED varchar(1);

COMMIT;