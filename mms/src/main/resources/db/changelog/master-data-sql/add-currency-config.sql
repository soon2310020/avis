BEGIN;
drop table  IF EXISTS  CURRENCY_CONFIG;
CREATE TABLE IF NOT EXISTS  CURRENCY_CONFIG (
                                                  `ID` bigint(20) NOT NULL,
                                                  `CREATED_AT` datetime DEFAULT NULL,
                                                  `UPDATED_AT` datetime DEFAULT NULL,
                                                  `CURRENCY_TYPE` varchar(255) DEFAULT NULL,
                                                  `DELETED` varchar(1) DEFAULT NULL,
                                                  `MAIN` varchar(1) DEFAULT NULL,
                                                  `RATE` double DEFAULT NULL,
                                                  PRIMARY KEY (`ID`),
                                                  UNIQUE KEY `UK_bfqj3ggcrvc7wcddajrhxfc95` (`CURRENCY_TYPE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
INSERT INTO `CURRENCY_CONFIG`(`ID`, `CURRENCY_TYPE`, `RATE`, `MAIN`, `DELETED`, `CREATED_AT`, `UPDATED_AT`) VALUES (1, 'USD', 1, 'Y', 'N', NOW() ,NOW() );
INSERT INTO `CURRENCY_CONFIG`(`ID`, `CURRENCY_TYPE`, `RATE`, `MAIN`, `DELETED`, `CREATED_AT`, `UPDATED_AT`) VALUES (2, 'EUR', NULL, 'N', 'N', NOW() ,NOW() );
COMMIT;