DROP TABLE IF EXISTS `DATA`;

CREATE TABLE DATA (
	ID bigint not null auto_increment,
	
	DATA_TYPE varchar(255),
	TERMINAL_ID varchar(20),
	READ_TIME varchar(23),
	RAW_DATA varchar(5000),
	DUPLICATE_COUNT integer,
	EXECUTE varchar(1),
	CREATED_DATE datetime,
	UPDATED_DATE datetime,
	
	PRIMARY KEY (ID)
);

DROP TABLE IF EXISTS `DATA_ACCELERATION`;

CREATE TABLE DATA_ACCELERATION (
	ID bigint not null auto_increment,
	
	DATA_ID bigint,
	TERMINAL_ID varchar(20),
	READ_TIME varchar(23),
	COUNTER_ID varchar(20),
	MEASUREMENT_DATE varchar(255),
	RAWDATA_CREATED_AT datetime,
	CREATED_AT datetime,
	ACCELERATIONS text,
	
	PRIMARY KEY (ID)
);

DROP TABLE IF EXISTS `DATA_COUNTER`;

CREATE TABLE DATA_COUNTER (
	ID bigint not null auto_increment,
	
	DATA_ID bigint,
	TERMINAL_ID varchar(20),
	READ_TIME varchar(23),
	COUNTER_ID varchar(20),
	SHOT_START_TIME varchar(14),
	SHOT_END_TIME varchar(14),
	SHOT_COUNT integer not null,
	BATTERY_STATUS varchar(2),
	STATUS varchar(2),
	TEMPERATURE varchar(255),
	MODE_OPEN_TIME decimal(19,2),
	MODE_CYCLE_TIME decimal(19,2),
	RAWDATA_CREATED_AT datetime,
	CREATED_AT datetime,
	CYCLE_TIMES varchar(5000),
	
	PRIMARY KEY (ID)
);

DROP TABLE IF EXISTS `DATA_TERMINAL`;

CREATE TABLE DATA_TERMINAL (
	ID bigint not null auto_increment,
	
	DATA_ID bigint,
	TERMINAL_ID varchar(20),
	VERSION varchar(20),
	NETWORK_TYPE varchar(20),
	IP varchar(20),
	CREATED_DATE datetime,
	UPDATED_DATE datetime,
	SENSORS text,
	
	PRIMARY KEY (ID)
);

create index IDX_EXECUTE on DATA (EXECUTE);
create index IDX_COUNTER_ID on DATA_ACCELERATION (COUNTER_ID);
create index IDX_COUNTER_ID on DATA_COUNTER (COUNTER_ID);
create index IDX_TERMINAL_ID on DATA_TERMINAL (TERMINAL_ID);