DROP TABLE IF EXISTS DATA3COLLECTED;

CREATE TABLE DATA3COLLECTED (
	ID bigint not null auto_increment,
	
	REQUEST_ID varchar(50),
	POSITION integer,
	PROC_STATUS varchar(20),
	PROC_ERROR_ID bigint,
	
	OCCURRED_AT datetime,
	SENT_AT datetime,
	CREATED_AT datetime,
	DISTRIBUTED_AT datetime,
	ANALYZED_AT datetime,
	UPDATED_AT datetime,
	
	DATA_TYPE varchar(255),
	DATA longtext,
	
	DEVICE_ID varchar(100),
	DEVICE_TYPE varchar(20),
	DEVICE_SW_VERSION varchar(20),
	
	BROKER_ID varchar(100),
	BROKER_TYPE varchar(20),
	BROKER_SW_VERSION varchar(20),
	
	PRIMARY KEY (ID)
) CHARSET=utf8;
