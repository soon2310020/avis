-- Update illustration_table
update illustration_table
set insurance_package_id = 8
where package_type = 'FREE_STYLE'
  and package_code in ('PROTECT1', 'PROTECT2', 'PROTECT3', 'PROTECT4', 'INVEST1_80', 'INVEST2_80', 'INVEST3_80', 'DAN', 'NHA', 'PHQ', 'COD');

update illustration_table
set insurance_package_id = 16
where package_type = 'FREE_STYLE'
  and package_code in ('ULPROTECT5_1', 'ULPROTECT5_2', 'ULPROTECT5_3', 'ULPROTECT5_4',
                       'ULINVEST5_1', 'ULINVEST5_2', 'ULINVEST5_3');