DROP TABLE IF EXISTS STATISTICS_EXT;

CREATE TABLE STATISTICS_EXT (
	ID bigint not null,
	CDATA_ID bigint,

	CREATED_AT DATETIME,
	UPDATED_AT DATETIME,
	
	SHOT_COUNT_CTT INT,
	SHOT_COUNT_CTT_VAL INT,
	
	PRIMARY KEY (ID)
);