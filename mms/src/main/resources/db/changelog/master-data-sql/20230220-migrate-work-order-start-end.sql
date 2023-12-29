BEGIN;

UPDATE WORK_ORDER w
SET w.`START` = w.CREATED_AT WHERE w.`START` IS NULL AND w.MOLD_MAINTENANCE_ID IS NOT NULL;

UPDATE WORK_ORDER w
    LEFT JOIN MOLD_MAINTENANCE m ON w.MOLD_MAINTENANCE_ID = m.ID
SET w.`END` = DATE_ADD(w.`START`, INTERVAL m.DUE_DATE DAY) WHERE w.`END` IS NULL AND w.MOLD_MAINTENANCE_ID IS NOT NULL;

COMMIT;