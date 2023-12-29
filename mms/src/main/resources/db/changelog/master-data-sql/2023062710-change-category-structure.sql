BEGIN;

UPDATE
	CATEGORY
SET
	GRAND_PARENT_ID =
		CASE
			WHEN LEVEL = 2 AND PARENT_ID IS NOT NULL THEN
				PARENT_ID
			WHEN LEVEL = 3 AND GRAND_PARENT_ID IS NULL AND PARENT_ID IS NOT NULL THEN (
				SELECT 
					PARENT_ID 
				FROM 
					(SELECT PARENT_ID, ID FROM CATEGORY) AS subquery
				WHERE 
					subquery.ID = CATEGORY.PARENT_ID
				LIMIT 1
			)
			ELSE 
				GRAND_PARENT_ID 
		END,
	PARENT_ID = IF(LEVEL = 2 || LEVEL = 0, NULL, PARENT_ID);

COMMIT;