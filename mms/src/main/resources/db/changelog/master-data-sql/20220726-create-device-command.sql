DROP TABLE IF EXISTS DEVICE_COMMAND;

CREATE TABLE DEVICE_COMMAND (
	ID bigint not null auto_increment,
	
	DEVICE_ID varchar(100),
	DEVICE_TYPE varchar(20),
	COMMAND varchar(100),
	INDEX_NO integer,
	STATUS varchar(20),
	DATA longtext,
	COMMENT varchar(1000),
	
	CREATED_BY bigint,
	CREATED_AT datetime,
	UPDATED_BY bigint,
	UPDATED_AT datetime,
	
	PRIMARY KEY (ID)
) CHARSET=utf8;