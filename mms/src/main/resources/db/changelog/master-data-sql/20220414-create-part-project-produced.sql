BEGIN;
DROP TABLE IF EXISTS `PART_PROJECT_PRODUCED`;
CREATE TABLE `PART_PROJECT_PRODUCED`
(
    `ID`                 BIGINT(20) NOT NULL AUTO_INCREMENT,
    `PART_ID`            BIGINT(20) NOT NULL,
    `PROJECT_ID`         BIGINT(20) NOT NULL,
    `TOTAL_PRODUCED`     BIGINT(20) DEFAULT NULL,
    `TOTAL_PRODUCED_VAL` BIGINT(20) DEFAULT NULL,
    `CREATED_AT`         DATETIME DEFAULT NULL,
    `UPDATED_AT`         DATETIME DEFAULT NULL,
    `CREATED_BY`         BIGINT   DEFAULT NULL,
    `UPDATED_BY`         BIGINT   DEFAULT NULL,
    PRIMARY KEY (`ID`),
    KEY                  `IDX_PART_ID` (`PART_ID`),
    KEY                  `IDX_PROJECT_ID` (`PROJECT_ID`),
    UNIQUE KEY `IDX_PART_ID_PROJECT_ID` (`PART_ID`,`PROJECT_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#create indexes
CREATE INDEX IDX_PART_ENABLED on PART (ENABLED);
CREATE INDEX IDX_SP_03 on STATISTICS_PART (PROJECT_ID);
CREATE INDEX IDX_CREATED_AT on STATISTICS_PART (CREATED_AT);

#init data
INSERT
INTO
    PART_PROJECT_PRODUCED (PART_ID,
                           PROJECT_ID,
                           TOTAL_PRODUCED,
                           TOTAL_PRODUCED_VAL,
                           CREATED_AT,
                           UPDATED_AT)
SELECT statistics1_.PART_ID,
       statistics1_.PROJECT_ID,
       sum(statistics1_.CAVITY * statistics0_.SHOT_COUNT),
       sum(statistics1_.CAVITY * statistics0_.SHOT_COUNT_VAL) AS col_1_0_,
       now(),
       now()
FROM STATISTICS statistics0_
         INNER JOIN
     STATISTICS_PART statistics1_ ON
         (
             statistics0_.ID = statistics1_.STATISTICS_ID
             )
WHERE ((statistics0_.SHOT_COUNT IS NOT NULL
    AND statistics0_.SHOT_COUNT > 0)
    OR (statistics0_.SHOT_COUNT_VAL IS NOT NULL
        AND statistics0_.SHOT_COUNT_VAL > 0))
  AND (
    statistics1_.CAVITY IS NOT NULL
    )
  AND statistics1_.CAVITY > 0
  AND (
            statistics0_.CT > 0
        OR statistics0_.CT_VAL > 0
        OR statistics0_.FIRST_DATA = 'Y'
    )
GROUP BY statistics1_.PART_ID,
         statistics1_.PROJECT_ID;

COMMIT;