-- Insert insurance_package
INSERT INTO insurance_package
(id, package_name, total_fee, total_benefit, issuer, mic_package_id, sub_one, sub_two, sub_three, sub_four, mbal_package_id, mic_fee_payment_time, mic_periodic_fee_payment, mbal_fee_payment_time, mbal_periodic_fee_payment, type)
VALUES(8, 'Gói Phong Cách', '', '', 'MBAL + MIC', 1, false, false, false, false, 1, '1 năm', 'Không', '10 năm', 'Hàng năm', 'FREE_STYLE');

update insurance_package
set type = 'FIX_COMBO' where id <= 7;