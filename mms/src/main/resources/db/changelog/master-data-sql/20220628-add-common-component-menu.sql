BEGIN;

-- menu level 3
INSERT INTO `MENU`(`ID`, `PARENT_ID`, `LEVEL`, `MENU_NAME`, `MENU_KEY`, `MENU_URL`, `ICON`, `ADMIN_MENU`, `ENABLED`, `CREATED_AT`, `UPDATED_AT`)
VALUES
    (2190, 2100, 3, 'Common Component', '/admin/common-component', '/admin/common-component', 'icon-common-component', 'N', 'Y', NOW(), NOW());
COMMIT;