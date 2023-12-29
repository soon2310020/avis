BEGIN;
DROP TABLE IF EXISTS `STATISTICS_PART_REPLACED`;
CREATE TABLE `STATISTICS_PART_REPLACED`
(
    `ID`            bigint(20) NOT NULL,
    `STATISTICS_ID` bigint(20) DEFAULT NULL,
    `CAVITY`        int(11) DEFAULT NULL,
    `PART_ID`       bigint(20) DEFAULT NULL,
    `PART_CODE`     varchar(255) DEFAULT NULL,
    `PROJECT_ID`    bigint(20) DEFAULT NULL,
    `PROJECT_NAME`  varchar(255) DEFAULT NULL,
    `CATEGORY_ID`   bigint(20) DEFAULT NULL,
    `CATEGORY_NAME` varchar(255) DEFAULT NULL,
    `CREATED_AT`    datetime     DEFAULT NULL,
    PRIMARY KEY (`ID`),
    KEY             `FK_STATISTICS_PART_REPLACED` (`STATISTICS_ID`),
    KEY             `IDX_SPR_01` (`PART_ID`),
    KEY             `IDX_SPR_02` (`PART_CODE`),
    CONSTRAINT `FK_STATISTICS_PART_REPLACED` FOREIGN KEY (`STATISTICS_ID`) REFERENCES `STATISTICS` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
COMMIT;