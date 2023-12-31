BEGIN ;

DROP TABLE IF EXISTS `MOLD_CT_DEVIATION`;
DROP TABLE IF EXISTS `CYCLE_TIME_DEVIATION`;

CREATE TABLE `CYCLE_TIME_DEVIATION` (
	`ID` BIGINT NOT NULL AUTO_INCREMENT,

	`SUPPLIER_ID` BIGINT,
	`MOLD_ID` BIGINT,
	`PART_ID` BIGINT,

	`TIME_SCALE` VARCHAR(255),
	`YEAR` VARCHAR(255),
	`MONTH` VARCHAR(255),
	`WEEK` VARCHAR(255),
	`DAY` VARCHAR(255),

	`APPROVED_CYCLE_TIME` DOUBLE,
	`AVERAGE_CYCLE_TIME` DOUBLE,
	`NCTD` DOUBLE,
	`NCTD_TREND` DOUBLE,

	`ABOVE_TOLERANCE_RATE` DOUBLE,
	`WITHIN_UPPER_L2_TOLERANCE_RATE` DOUBLE,
	`WITHIN_L1_TOLERANCE_RATE` DOUBLE,
	`WITHIN_LOWER_L2_TOLERANCE_RATE` DOUBLE,
	`BELOW_TOLERANCE_RATE` DOUBLE,

	`SHOT_COUNT` INT,
	`ABOVE_TOLERANCE_SC` INT,
	`WITHIN_UPPER_L2_TOLERANCE_SC` INT,
	`WITHIN_L1_TOLERANCE_SC` INT,
	`WITHIN_LOWER_L2_TOLERANCE_SC` INT,
	`BELOW_TOLERANCE_SC` INT,

	`CREATED_AT` DATETIME,
	`UPDATED_AT` DATETIME,

	PRIMARY KEY (`ID`)
) DEFAULT CHARSET = UTF8MB4;

COMMIT ;
