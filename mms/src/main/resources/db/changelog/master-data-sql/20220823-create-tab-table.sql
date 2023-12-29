BEGIN;
CREATE TABLE TAB_TABLE (
                           ID bigint not null auto_increment,
                           NAME varchar(100),
                           USER_ID bigint,
                           OBJECT_TYPE varchar(100),
                           IS_SHOW VARCHAR(1),
                           DELETED varchar(1),
                           CREATED_AT datetime,
                           UPDATED_AT datetime,
                           PRIMARY KEY (ID)
) CHARSET=utf8;


CREATE TABLE TAB_TABLE_DATA (
                                ID bigint not null auto_increment,
                                REF_ID bigint,
                                TAB_TABLE_ID bigint,
                                CREATED_AT datetime,
                                UPDATED_AT datetime,
                                PRIMARY KEY (ID)
) CHARSET=utf8;
COMMIT;