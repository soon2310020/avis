BEGIN ;

DROP TABLE IF EXISTS `CYCLE_TIME_FLUCTUATION`;
ALTER TABLE `CYCLE_TIME_DEVIATION` ADD COLUMN `NCTF` DOUBLE DEFAULT NULL;
ALTER TABLE `CYCLE_TIME_DEVIATION` ADD COLUMN `L1_LIMIT` DOUBLE DEFAULT NULL;
ALTER TABLE `CYCLE_TIME_DEVIATION` ADD COLUMN `CT_FLUCTUATION` DOUBLE DEFAULT NULL;
ALTER TABLE `CYCLE_TIME_DEVIATION` ADD COLUMN `CTF_TREND` DOUBLE DEFAULT NULL;

COMMIT ;