BEGIN;
ALTER TABLE DASHBOARD_CHART_PARAM_SETTING modify PARAM_VALUE varchar(255);
COMMIT;