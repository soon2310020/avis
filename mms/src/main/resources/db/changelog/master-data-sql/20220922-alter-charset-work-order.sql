BEGIN;
ALTER TABLE `WORK_ORDER` CONVERT TO CHARACTER SET utf8mb4;
ALTER TABLE `WORK_ORDER_ASSET` CONVERT TO CHARACTER SET utf8mb4;
ALTER TABLE `WORK_ORDER_USER` CONVERT TO CHARACTER SET utf8mb4;
ALTER TABLE `WORK_ORDER_COST` CONVERT TO CHARACTER SET utf8mb4;
COMMIT;