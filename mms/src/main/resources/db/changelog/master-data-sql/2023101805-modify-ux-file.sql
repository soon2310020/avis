
ALTER TABLE `FILE_GROUP`
	DROP INDEX `UX_FILE_GROUP`;
ALTER TABLE `FILE_GROUP`
	DROP INDEX `UX_FILE_GROUP2`;

ALTER TABLE `FILE_GROUP`
	ADD CONSTRAINT `UX_FILE_GROUP` UNIQUE (`FILE_GROUP_KEY`, `VERSION`, `PARAM_NAME`);
ALTER TABLE `FILE_GROUP`
	ADD CONSTRAINT `UX_FILE_GROUP2` UNIQUE (`FILE_GROUP_TYPE`, `FILE_GROUP_CODE`, `VERSION`, `PARAM_NAME`);

ALTER TABLE `FILE_ITEM`
	DROP INDEX `UX_FILE_ITEM`;
ALTER TABLE `FILE_ITEM`
	DROP INDEX `UX_FILE_ITEM2`;

ALTER TABLE `FILE_ITEM`
	ADD CONSTRAINT `UX_FILE_ITEM` UNIQUE (`FILE_GROUP_KEY`, `VERSION`, `PARAM_NAME`, `POSITION`);
ALTER TABLE `FILE_ITEM`
	ADD CONSTRAINT `UX_FILE_ITEM2` UNIQUE (`FILE_TYPE`, `FILE_NO`, `VERSION`, `PARAM_NAME`, `POSITION`);
