BEGIN;
ALTER TABLE `DATA_REQUEST`
    ADD COLUMN `CANCELED_BY_ID` bigint(20) DEFAULT NULL;
ALTER TABLE `DATA_REQUEST`
    ADD COLUMN `DECLINED_BY_ID` bigint(20) DEFAULT NULL;
ALTER TABLE `DATA_REQUEST`
    MODIFY `CREATED_BY_ID` bigint(20);
COMMIT;