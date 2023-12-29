BEGIN;
set @queryDr=null;
SELECT
    CONCAT('ALTER TABLE `USER` DROP INDEX `' , CONSTRAINT_NAME , '`;') as q INTO @queryDr
FROM
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE  TABLE_SCHEMA = DATABASE()
  AND COLUMN_NAME = 'EMAIL'
  AND   TABLE_NAME = 'USER'
    LIMIT 1;

set @query = IF(@queryDr is null, 'select \'UK not Exists\'',
                @queryDr);

select @query;

prepare stmt from @query;

EXECUTE stmt;

CREATE INDEX IDX_user_email
    ON `USER` (EMAIL);

COMMIT;
