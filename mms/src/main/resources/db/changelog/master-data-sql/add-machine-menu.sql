BEGIN;
INSERT INTO `MENU`(`ID`, `PARENT_ID`, `LEVEL`, `MENU_NAME`, `MENU_KEY`, `MENU_URL`, `ICON`, `ADMIN_MENU`, `ENABLED`, `CREATED_AT`, `UPDATED_AT`)
VALUES
(2255, 2200, 3, 'Machine', '/admin/machines', '/admin/machines', 'icon-machine', 'N', 'Y', NOW(), NOW());
COMMIT;