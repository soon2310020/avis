BEGIN;
INSERT INTO `MENU`(`ID`, `PARENT_ID`, `LEVEL`, `MENU_NAME`, `MENU_KEY`, `MENU_URL`, `ICON`, `ADMIN_MENU`, `ENABLED`, `POSITION`, `CREATED_AT`, `UPDATED_AT`)
VALUES
    (2630, 2600, 3, 'Checklist Center', '/admin/checklist-center', '/admin/checklist-center', 'icon-checklist-center', 'N', 'Y',2, NOW(), NOW());

UPDATE `MENU` SET ENABLED = 'N' WHERE ID = 2270;
COMMIT;