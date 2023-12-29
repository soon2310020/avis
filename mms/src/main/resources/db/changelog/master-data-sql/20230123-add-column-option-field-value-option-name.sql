BEGIN;

ALTER TABLE `OPTION_FIELD_VALUE`
	ADD `OPTION_NAME` varchar(100) AFTER `CONFIG_CATEGORY`;

UPDATE `OPTION_FIELD_VALUE` SET OPTION_NAME = CONFIG_CATEGORY;

ALTER TABLE `OPTION_FIELD_VALUE`
	CHANGE COLUMN `USER_ID` `USER_ID` bigint AFTER `OPTION_NAME`;

ALTER TABLE `OPTION_FIELD_VALUE`
	CHANGE COLUMN `FIELD_NAME` `FIELD_NAME` varchar(255) AFTER `USER_ID`;

ALTER TABLE `OPTION_FIELD_VALUE`
	CHANGE COLUMN `VALUE` `VALUE` varchar(4000) AFTER `FIELD_NAME`;

COMMIT;