BEGIN;
INSERT INTO `MENU` (`ID`, `ADMIN_MENU`, `ENABLED`, `ICON`, `LEVEL`, `MENU_KEY`, `MENU_NAME`, `MENU_URL`, `PARENT_ID`, `POSITION`) 
VALUES ('6200', 'Y', 'Y', 'icon-codelist-center', '3', '/admin/codelist-center', 'Code List Center', '/admin/codelist-center', '2600', '3');
UPDATE `MENU` SET ENABLED = 'Y' WHERE ID = 6200;
COMMIT;