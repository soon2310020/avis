DROP TABLE IF EXISTS `MACHINE_ENGINEER`;

CREATE TABLE MACHINE_ENGINEER (
	MACHINE_ID bigint not null,
	USER_ID bigint not null
);

ALTER TABLE MACHINE_ENGINEER
	ADD CONSTRAINT FK_MACHINE_ENGINEER_USER
	FOREIGN KEY (USER_ID)
	REFERENCES USER (ID);

ALTER TABLE MACHINE_ENGINEER
	ADD CONSTRAINT FK_MACHINE_ENGINEER_MACHINE
	FOREIGN KEY (MACHINE_ID)
	REFERENCES MACHINE (ID);
