ALTER TABLE `CUSTOM_FIELD`
	CHANGE COLUMN `OBJECT_TYPE` `OBJECT_TYPE` varchar(50) AFTER `ID`;

ALTER TABLE `CUSTOM_FIELD`
	CHANGE COLUMN `PROPERTY_GROUP` `PROPERTY_GROUP` varchar(50) AFTER `OBJECT_TYPE`;

ALTER TABLE `CUSTOM_FIELD`
	CHANGE COLUMN `FIELD_NAME` `FIELD_NAME` varchar(255) AFTER `PROPERTY_GROUP`;

ALTER TABLE `CUSTOM_FIELD`
	CHANGE COLUMN `FIELD_TYPE` `FIELD_TYPE` varchar(255) AFTER `FIELD_NAME`;

ALTER TABLE `CUSTOM_FIELD`
	CHANGE COLUMN `POSITION` `POSITION` integer AFTER `FIELD_TYPE`;

ALTER TABLE `CUSTOM_FIELD`
	CHANGE COLUMN `REQUIRED` `REQUIRED` varchar(1) AFTER `POSITION`;

ALTER TABLE `CUSTOM_FIELD`
	CHANGE COLUMN `DEFAULT_INPUT` `DEFAULT_INPUT` varchar(1) AFTER `REQUIRED`;

ALTER TABLE `CUSTOM_FIELD`
	CHANGE COLUMN `DEFAULT_INPUT_VALUE` `DEFAULT_INPUT_VALUE` longtext AFTER `DEFAULT_INPUT`;

ALTER TABLE `CUSTOM_FIELD`
	CHANGE COLUMN `ACTIVE` `ACTIVE` bit DEFAULT 1 AFTER `UPDATED_AT`;

ALTER TABLE `CUSTOM_FIELD`
	ADD COLUMN `DEFAULT_FIELD` varchar(1) DEFAULT 'N' AFTER `POSITION`;

ALTER TABLE `CUSTOM_FIELD`
	ADD COLUMN `HIDDEN` varchar(1) DEFAULT 'N' AFTER `DEFAULT_FIELD`;

ALTER TABLE `CUSTOM_FIELD`
	ADD COLUMN `DELETED` varchar(1) DEFAULT 'N' AFTER `HIDDEN`;

ALTER TABLE `CUSTOM_FIELD_VALUE`
	CHANGE COLUMN `OBJECT_ID` `OBJECT_ID` bigint AFTER `ID`;

ALTER TABLE `CUSTOM_FIELD_VALUE`
	CHANGE COLUMN `CUSTOM_FIELD_ID` `CUSTOM_FIELD_ID` bigint AFTER `OBJECT_ID`;

ALTER TABLE `CUSTOM_FIELD_VALUE`
	CHANGE COLUMN `VALUE` `VALUE` longtext AFTER `CUSTOM_FIELD_ID`;