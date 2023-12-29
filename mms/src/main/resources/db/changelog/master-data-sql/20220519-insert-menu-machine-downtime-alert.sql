BEGIN;
INSERT INTO `MENU` (`ID`, `PARENT_ID`, `MENU_NAME`, `MENU_KEY`, `MENU_URL`, `ADMIN_MENU`, `LEVEL`, `ICON`, `ENABLED`, `CREATED_AT`, `UPDATED_AT`, `POSITION`) VALUES
    (1291, 1200, 'Machine Downtime', '/front/machine-downtime', '/front/machine-downtime', 'N', 3, 'icon-downtime', 'Y', now(), now(), 10);

DELETE FROM ROLE_MENU WHERE MENU_ID = 1291;
SET FOREIGN_KEY_CHECKS=0;
INSERT INTO ROLE_MENU(ROLE_ID, MENU_ID) SELECT ID,1291 FROM ROLE;
SET FOREIGN_KEY_CHECKS=1;
COMMIT;