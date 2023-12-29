BEGIN;
INSERT INTO `MENU`(`ID`, `PARENT_ID`, `LEVEL`, `MENU_NAME`, `MENU_KEY`, `MENU_URL`, `ICON`, `ADMIN_MENU`, `ENABLED`, `CREATED_AT`, `UPDATED_AT`, `POSITION`)
VALUES
(1211, 1200, 3, 'Downtime', '/front/mold-downtime', '/front/mold-downtime', 'icon-downtime', 'N', 'Y', NOW(), NOW(), 0);
COMMIT;