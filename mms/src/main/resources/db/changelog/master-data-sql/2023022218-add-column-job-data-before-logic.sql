BEGIN;

ALTER TABLE `JOB_DATA`
	ADD COLUMN `BEFORE_LOGIC_NAME` varchar(200) AFTER `LOGIC_TX_TYPE`;

ALTER TABLE `JOB_DATA`
	ADD COLUMN `BEFORE_LOGIC_TX_TYPE` varchar(20) AFTER `BEFORE_LOGIC_NAME`;

COMMIT;