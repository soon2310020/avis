BEGIN;
DROP TABLE IF EXISTS `MACHINE_OEE`;
CREATE TABLE MACHINE_OEE
(
    `ID`                BIGINT(20) AUTO_INCREMENT NOT NULL,
    `CREATED_AT`        DATETIME    DEFAULT NOW(),
    `UPDATED_AT`        DATETIME    DEFAULT NOW(),
    `CREATED_BY`        BIGINT      DEFAULT NULL,
    `UPDATED_BY`        BIGINT      DEFAULT NULL,
    `MACHINE_ID`        BIGINT(20)  DEFAULT NULL,
    `DAY`               VARCHAR(20) DEFAULT NULL,
    `HOUR`              VARCHAR(20) DEFAULT NULL,
    `PART_PRODUCED`     BIGINT      DEFAULT NULL,
    `PART_PRODUCED_VAL` BIGINT      DEFAULT NULL,
    `FA`                DOUBLE      DEFAULT NULL,
    `FP`                DOUBLE      DEFAULT NULL,
    `FQ`                DOUBLE      DEFAULT NULL,
    PRIMARY KEY (`ID`),
    KEY `IDX_MACHINE_ID` (`MACHINE_ID`),
    KEY `IDX_DAY` (`DAY`),
    KEY `IDX_HOUR` (`HOUR`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
COMMIT;