BEGIN;
-- Disconnection  ALERT, CONFIRMED, FIXED
UPDATE TERMINAL_DISCONNECT SET DELETED ='Y' WHERE LATEST = 'N' AND NOTIFICATION_STATUS = 'ALERT';
UPDATE MOLD_DISCONNECT SET DELETED ='Y' WHERE LATEST = 'N' AND NOTIFICATION_STATUS = 'ALERT';

-- Detachment ALERT, FIXED
UPDATE MOLD_DETACHMENT SET DELETED ='Y' WHERE LATEST = 'N' AND NOTIFICATION_STATUS = 'ALERT';

-- Reset MISCONFIGURED, CONFIRMED, CANCELED
UPDATE MOLD_MISCONFIGURE SET DELETED ='Y' WHERE LATEST = 'N' AND MISCONFIGURE_STATUS ='MISCONFIGURED';

-- Data Approval PENDING, APPROVED, DISAPPROVED
UPDATE MOLD_DATA_SUBMISSION SET DELETED ='Y' WHERE LATEST = 'N' AND NOTIFICATION_STATUS = 'PENDING';

-- Relocation PENDING, APPROVED, DISAPPROVED, CONFIRMED
UPDATE MOLD_LOCATION SET DELETED ='Y' WHERE LATEST = 'N' AND MOLD_LOCATION_STATUS = 'PENDING';
-- End Of Life END_OF_LIFECYCLE, REQUESTED, APPROVED, DISAPPROVED, COMPLETED, DISCARDED
UPDATE MOLD_REFURBISHMENT SET DELETED ='Y' WHERE LATEST = 'N' AND REFURBISHMENT_STATUS in ('END_OF_LIFECYCLE','REQUESTED');


COMMIT;