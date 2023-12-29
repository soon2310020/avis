-- Update mbal package name
update mbal_package
set name = 'Gói bảo vệ' where id <= 7;

-- Update insurance_package
update insurance_package
set mbal_package_id = 2 where id = 2;

update insurance_package
set mbal_package_id = 3 where id = 3;

update insurance_package
set mbal_package_id = 4 where id = 4;

update insurance_package
set mbal_package_id = 5 where id = 5;

update insurance_package
set mbal_package_id = 6 where id = 6;

update insurance_package
set mbal_package_id = 7 where id = 7;