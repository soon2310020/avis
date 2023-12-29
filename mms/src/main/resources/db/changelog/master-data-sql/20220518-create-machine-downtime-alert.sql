BEGIN;

DROP TABLE IF EXISTS MACHINE_DOWNTIME_REASON;
CREATE TABLE MACHINE_DOWNTIME_REASON (
                                         ID bigint not null auto_increment,

                                         MACHINE_DOWNTIME_ALERT_ID bigint,
                                         CODE_DATA_ID bigint,

                                         START_TIME datetime,
                                         END_TIME datetime,

                                         NOTE longtext,

                                         CREATED_BY bigint,
                                         CREATED_AT datetime,
                                         UPDATED_BY bigint,
                                         UPDATED_AT datetime,

                                         PRIMARY KEY (ID)
) CHARSET=utf8;


DROP TABLE IF EXISTS MACHINE_DOWNTIME_ALERT_USER;
CREATE TABLE MACHINE_DOWNTIME_ALERT_USER (
                                             ID bigint not null auto_increment,

                                             MACHINE_DOWNTIME_ALERT_ID bigint,
                                             NOTIFICATION_STATUS varchar(20),

                                             USER_ID bigint,
                                             CONFIRMED_AT datetime,

                                             CREATED_AT datetime,

                                             PRIMARY KEY (ID)
) CHARSET=utf8;


DROP TABLE IF EXISTS MACHINE_DOWNTIME_ALERT;
CREATE TABLE MACHINE_DOWNTIME_ALERT (
                                  ID bigint not null auto_increment,

                                  MACHINE_ID bigint,

                                  DOWNTIME_TYPE varchar(20),
                                  DOWNTIME_STATUS varchar(20),
                                  START_TIME datetime,
                                  END_TIME datetime,

                                  REPORTED_BY bigint,
                                  CONFIRMED_BY bigint,
                                  CREATED_BY bigint,
                                  CREATED_AT datetime,
                                  UPDATED_BY bigint,
                                  UPDATED_AT datetime,

                                  PRIMARY KEY (ID)
) CHARSET=utf8;
COMMIT;