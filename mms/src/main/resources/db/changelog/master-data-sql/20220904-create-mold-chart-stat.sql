DROP TABLE IF EXISTS MOLD_CHART_STAT;
DROP TABLE IF EXISTS MOLD_CHART_STAT_ACK;

CREATE TABLE MOLD_CHART_STAT (
	ID bigint not null auto_increment,
	
	MOLD_ID bigint,
	
	TIME_SCALE varchar(20),
	YEAR varchar(4),
	MONTH varchar(6),
	WEEK varchar(6),
	DAY varchar(8),
	
	ACT double,
	WACT double,
	
	SC bigint,
	CVT double,
	CT double,
	
	CREATED_AT datetime,
	UPDATED_AT datetime,
	
	PARTS longtext,
	
	PRIMARY KEY (ID)
) CHARSET=utf8;
