DROP TABLE IF EXISTS `OPTION_CONTENT`;

CREATE TABLE `OPTION_CONTENT` (
	`ID` BIGINT not null auto_increment,
	
	`OPTION_NAME` varchar(100),
	`USER_ID` bigint,
	`CONTENT` longtext,
	
	`CREATED_BY` bigint,
	`CREATED_AT` datetime,
	`UPDATED_BY` bigint,
	`UPDATED_AT` datetime,
	
	PRIMARY KEY (ID)
);

ALTER TABLE OPTION_CONTENT
   ADD CONSTRAINT UX_OPTION_CONTENT UNIQUE (OPTION_NAME, USER_ID);