BEGIN;
INSERT INTO `MENU`(`ID`, `PARENT_ID`, `LEVEL`, `MENU_NAME`, `MENU_KEY`, `MENU_URL`, `ICON`, `ADMIN_MENU`, `ENABLED`, `CREATED_AT`, `UPDATED_AT`) VALUES (5120, 5100, 3, 'Predictive Maintenance', '/insight/predictive-maintenance', '/insight/predictive-maintenance', 'icon-predictive-maintenance', 'N', 'Y', '2021-04-02 21:47:37', '2021-04-02 21:47:41');
INSERT INTO `MENU`(`ID`, `PARENT_ID`, `LEVEL`, `MENU_NAME`, `MENU_KEY`, `MENU_URL`, `ICON`, `ADMIN_MENU`, `ENABLED`, `CREATED_AT`, `UPDATED_AT`) VALUES (5130, 5100, 3, 'Delivery Forecast', '/insight/delivery-forecast', '/insight/delivery-forecast', 'icon-delivery-forecast', 'N', 'Y', '2021-04-02 21:49:16', '2021-04-02 21:49:22');
INSERT INTO `MENU`(`ID`, `PARENT_ID`, `LEVEL`, `MENU_NAME`, `MENU_KEY`, `MENU_URL`, `ICON`, `ADMIN_MENU`, `ENABLED`, `CREATED_AT`, `UPDATED_AT`) VALUES (5140, 5100, 3, 'Quality Management', '/insight/quality-management', '/insight/quality-management', 'icon-quality-management', 'N', 'Y', '2021-04-02 21:50:38', '2021-04-02 21:50:41');
COMMIT;