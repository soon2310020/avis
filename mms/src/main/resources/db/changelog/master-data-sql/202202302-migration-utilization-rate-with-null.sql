BEGIN;
UPDATE
MOLD
SET
UTILIZATION_RATE = 0
WHERE
LAST_SHOT IS NULL
OR DESIGNED_SHOT IS NULL
OR DESIGNED_SHOT = 0;
COMMIT;