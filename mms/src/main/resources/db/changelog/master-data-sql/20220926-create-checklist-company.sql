BEGIN;
DROP TABLE IF EXISTS `CHECKLIST_COMPANY`;
CREATE TABLE `CHECKLIST_COMPANY`
(
    `ID`            BIGINT NOT NULL AUTO_INCREMENT,
    `CHECKLIST_ID` BIGINT   DEFAULT NULL,
    `COMPANY_ID`       BIGINT   DEFAULT NULL,
    `CREATED_AT`    DATETIME DEFAULT NULL,
    `UPDATED_AT`    DATETIME DEFAULT NULL,
    PRIMARY KEY (`ID`),
    KEY `FKa0obegqwt324sgarskek8s` (`CHECKLIST_ID`),
    CONSTRAINT `FKa0obegqwt324sgarskek8s` FOREIGN KEY (`CHECKLIST_ID`) REFERENCES `CHECKLIST` (`ID`),
    KEY `FKa0obhgsdh6463kek8s` (`COMPANY_ID`),
    CONSTRAINT `FKa0obhgsdh6463kek8s` FOREIGN KEY (`COMPANY_ID`) REFERENCES `COMPANY` (`ID`)
) ENGINE = INNODB
  AUTO_INCREMENT = 1 DEFAULT CHARSET=utf8mb4;
INSERT INTO `CHECKLIST_COMPANY` (CHECKLIST_ID, COMPANY_ID, CREATED_AT, UPDATED_AT)
SELECT ID, COMPANY_ID, NOW(), NOW()  FROM `CHECKLIST` WHERE COMPANY_ID IS NOT NULL;
COMMIT;