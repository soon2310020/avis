BEGIN;
UPDATE `MACHINE_OEE` SET FQ = 100 WHERE PART_PRODUCED <> 0 AND FQ = 0;
COMMIT;