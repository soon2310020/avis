BEGIN;
UPDATE `GENERAL_CONFIG` SET REQUIRED = 'N' WHERE CONFIG_CATEGORY = 'TOOLING' and FIELD_NAME = 'preventOverdue';
COMMIT;