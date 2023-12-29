BEGIN;
set @queryDr=null;
SELECT
    CONCAT('ALTER TABLE `FILE_STORAGE` DROP FOREIGN KEY `' , CONSTRAINT_NAME , '`;') as q INTO @queryDr
FROM
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE  TABLE_SCHEMA = DATABASE()
  AND REFERENCED_TABLE_NAME = 'TERMINAL'
  AND   TABLE_NAME = 'FILE_STORAGE'
 LIMIT 1;

set @query = IF(@queryDr is null, 'select \'Ref not Exists\'',
                @queryDr);

select @query;

prepare stmt from @query;

EXECUTE stmt;

COMMIT;