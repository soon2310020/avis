DROP TABLE IF EXISTS TRANSFER_MESSAGE;

CREATE TABLE TRANSFER_MESSAGE (
	ID bigint not null auto_increment,
	
	PROC_STATUS varchar(20),
	PROC_ERROR_ID bigint,
	ELAPSED_TIME_MILLIS bigint,
	
	AT varchar(20),
	CI varchar(100),
	DATA longtext,
	
	CREATED_AT datetime,
	UPDATED_AT datetime,
	
	PRIMARY KEY (ID)
) CHARSET=utf8;
