ALTER TABLE `TERMINAL_DISCONNECT`
	ADD COLUMN `DELETED` varchar(1) DEFAULT 'N' AFTER `LATEST`;

ALTER TABLE `MOLD_DISCONNECT`
	ADD COLUMN `DELETED` varchar(1) DEFAULT 'N' AFTER `LATEST`;

ALTER TABLE `MOLD_DETACHMENT`
	ADD COLUMN `DELETED` varchar(1) DEFAULT 'N' AFTER `LATEST`;

ALTER TABLE `MOLD_MISCONFIGURE`
	ADD COLUMN `DELETED` varchar(1) DEFAULT 'N' AFTER `LATEST`;

ALTER TABLE `MOLD_DATA_SUBMISSION`
	ADD COLUMN `DELETED` varchar(1) DEFAULT 'N' AFTER `LATEST`;

ALTER TABLE `MOLD_LOCATION`
	ADD COLUMN `DELETED` varchar(1) DEFAULT 'N' AFTER `LATEST`;

ALTER TABLE `MOLD_REFURBISHMENT`
	ADD COLUMN `DELETED` varchar(1) DEFAULT 'N' AFTER `LATEST`;

ALTER TABLE `MACHINE_DOWNTIME_ALERT`
	ADD COLUMN `DELETED` varchar(1) DEFAULT 'N' AFTER `LATEST`;

ALTER TABLE `MOLD_CYCLE_TIME`
	ADD COLUMN `DELETED` varchar(1) DEFAULT 'N' AFTER `LATEST`;

ALTER TABLE `MOLD_EFFICIENCY`
	ADD COLUMN `DELETED` varchar(1) DEFAULT 'N' AFTER `LATEST`;