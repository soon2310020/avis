ALTER TABLE `MOLD`
	ADD COLUMN `SUPPLIER_MOLD_CODE` varchar(100) AFTER `EQUIPMENT_CODE`;

ALTER TABLE `MOLD`
	ADD CONSTRAINT `UX_MOLD__SUPPLIER_MOLD_CODE` UNIQUE (`SUPPLIER_MOLD_CODE`);