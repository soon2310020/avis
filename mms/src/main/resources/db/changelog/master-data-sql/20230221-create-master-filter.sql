DROP TABLE IF EXISTS `DATA_FILTER_RESOURCE`;
DROP TABLE IF EXISTS `MASTER_FILTER_RESOURCE`;

CREATE TABLE `MASTER_FILTER_RESOURCE` (
	`ID` bigint not null auto_increment,
	
	`FILTER_CODE` varchar(50),
	`USER_ID` bigint,
	`RESOURCE_TYPE` varchar(50),
	
	`POSITION` integer,
	`MODE` varchar(20),
	
	PRIMARY KEY (`ID`)
);

ALTER TABLE `MASTER_FILTER_RESOURCE`
	ADD CONSTRAINT `UX_MASTER_FILTER_RESOURCE` UNIQUE (`FILTER_CODE`, `USER_ID`, `RESOURCE_TYPE`);


DROP TABLE IF EXISTS `DATA_FILTER_ITEM`;
DROP TABLE IF EXISTS `MASTER_FILTER_ITEM`;

CREATE TABLE `MASTER_FILTER_ITEM` (
	`ID` bigint not null auto_increment,
	
	`FILTER_CODE` varchar(50),
	`USER_ID` bigint,
	`RESOURCE_TYPE` varchar(50),
	`RESOURCE_ID` bigint,
	
	`SELECTED` varchar(1),
	`TEMPORAL` varchar(1),
	
	PRIMARY KEY (`ID`)
);

ALTER TABLE `MASTER_FILTER_ITEM`
   ADD CONSTRAINT `UX_MASTER_FILTER_ITEM` UNIQUE (`FILTER_CODE`, `USER_ID`, `RESOURCE_TYPE`, `RESOURCE_ID`);