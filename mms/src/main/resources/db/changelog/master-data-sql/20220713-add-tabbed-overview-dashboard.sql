BEGIN;
INSERT INTO `MENU` (`ID`, `PARENT_ID`, `MENU_NAME`, `MENU_KEY`, `MENU_URL`, `ADMIN_MENU`, `LEVEL`, `ICON`, `ENABLED`, `CREATED_AT`, `UPDATED_AT`, `POSITION`) VALUES
    (1130, 1000, 'Overview', '/front/tabbed-overview', '/front/tabbed-overview', 'N', 3, 'icon-overview', 'Y', now(), now(), 0);
COMMIT;
