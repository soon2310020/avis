BEGIN;
INSERT INTO `MENU`(`ID`, `PARENT_ID`, `LEVEL`, `MENU_NAME`, `MENU_KEY`, `MENU_URL`, `ICON`, `ADMIN_MENU`, `ENABLED`, `POSITION`, `CREATED_AT`, `UPDATED_AT`)
VALUES
    (2710, 2700, 3, 'Machine Availability', '/admin/machine-availability', '/admin/machine-availability', 'icon-machine-availability', 'N', 'Y', 2, NOW(), NOW());
COMMIT;