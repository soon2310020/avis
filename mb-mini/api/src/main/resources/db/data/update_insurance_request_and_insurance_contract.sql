-- Update insurance_request
update insurance_request
set insurance_package_id = 8 where insurance_package_id is null;

-- Update insurance_contract
update insurance_contract
set insurance_package_id = 8 where insurance_package_id is null;