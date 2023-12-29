BEGIN;
INSERT INTO `MENU`(`ID`, `PARENT_ID`, `LEVEL`, `MENU_NAME`, `MENU_KEY`, `MENU_URL`, `ICON`, `ADMIN_MENU`, `ENABLED`, `CREATED_AT`, `UPDATED_AT`)
VALUES (2160, 2100, 3, 'Currency', '/admin/currency', '/admin/currency', 'icon-currency', 'Y', 'Y', NOW(), NOW());
COMMIT;