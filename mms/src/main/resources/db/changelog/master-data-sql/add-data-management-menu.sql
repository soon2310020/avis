BEGIN;
-- menu level 2
INSERT INTO `MENU`(`ID`, `PARENT_ID`, `LEVEL`, `MENU_NAME`, `MENU_KEY`, `MENU_URL`, `ICON`, `ADMIN_MENU`, `ENABLED`, `CREATED_AT`, `UPDATED_AT`)
VALUES
    (2600,2000,2, 'Data Management', null,null,null, 'N', 'Y', NOW(), NOW());

-- menu level 3
INSERT INTO `MENU`(`ID`, `PARENT_ID`, `LEVEL`, `MENU_NAME`, `MENU_KEY`, `MENU_URL`, `ICON`, `ADMIN_MENU`, `ENABLED`, `CREATED_AT`, `UPDATED_AT`)
VALUES
    (2610, 2600, 3, 'Data Registration', '/admin/data-registration', '/admin/data-registration', 'icon-data-registration', 'N', 'Y', NOW(), NOW()),
    (2620, 2600, 3, 'Data Completion', '/admin/data-completion', '/admin/data-completion', 'icon-data-completion', 'N', 'Y', NOW(), NOW());
COMMIT;