BEGIN;

ALTER TABLE LOCATION 
	ADD COLUMN `TIME_ZONE_ID` varchar(20);

ALTER TABLE `OPTION_FIELD_VALUE`
	CHANGE COLUMN `ID` `ID` BIGINT(19) NOT NULL AUTO_INCREMENT FIRST;
ALTER TABLE `OPTION_FIELD_VALUE`
	ADD COLUMN CREATED_BY bigint,
	ADD COLUMN CREATED_AT datetime,
	ADD COLUMN UPDATED_BY bigint,
	ADD COLUMN UPDATED_AT datetime;

COMMIT;