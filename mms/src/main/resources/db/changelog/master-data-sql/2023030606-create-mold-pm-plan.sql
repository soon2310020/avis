BEGIN;

DROP TABLE IF EXISTS `MOLD_PM_PLAN`;

CREATE TABLE `MOLD_PM_PLAN` (
	`ID` bigint not null auto_increment,
	`MOLD_ID` bigint,
	`PM_STRATEGY` varchar(20),
	`PM_FREQUENCY` varchar(20),
	`SCHED_START_DATE` varchar(8),
	`SCHED_INTERVAL` integer,
	`SCHED_ORDINAL_NUM` integer,
	`SCHED_DAY_OF_WEEK` varchar(10),
	`SCHED_UPCOMING_TOLERANCE` integer,
	`RECURR_CONSTRAINT_TYPE` varchar(20),
	`RECURR_NUM` integer,
	`RECURR_DUE_DATE` varchar(8),
	`NEXT_SCHED_DATE` varchar(8),
	`NEXT_UPCOMING_TOLERANCE_DATE` varchar(8),
	`CREATED_BY` bigint,
	`CREATED_AT` datetime,
	`UPDATED_BY` bigint,
	`UPDATED_AT` datetime,
	PRIMARY KEY (`ID`)
) DEFAULT CHARSET=UTF8;

ALTER TABLE `MOLD_PM_PLAN`
	ADD CONSTRAINT UX_MOLD_PM_PLAN UNIQUE (`MOLD_ID`);

COMMIT;