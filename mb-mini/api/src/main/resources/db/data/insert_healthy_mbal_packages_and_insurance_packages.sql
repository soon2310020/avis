-- Insert mbal_package (HEALTHY)
INSERT INTO mbal_package
(id, "name", insurance_fee, str_insurance_fee, insurance_fee_str, package_code, product_name, product_code, "type", death_benefit, str_death_benefit, logo, payfrq_cd)
VALUES(8, 'An tâm bảo vệ', 6000000, '6.000.000 VND', '6 Triệu', 'ULPROTECT5_1', 'An tâm bảo vệ', 'UL2020-RP', 'ULLP_PROTECT', '300,000,000 VND', '300 triệu', NULL, NULL);
INSERT INTO mbal_package
(id, "name", insurance_fee, str_insurance_fee, insurance_fee_str, package_code, product_name, product_code, "type", death_benefit, str_death_benefit, logo, payfrq_cd)
VALUES(9, 'An tâm bảo vệ', 10000000, '10.000.000 VND', '10 Triệu', 'ULPROTECT5_2', 'An tâm bảo vệ', 'UL2020-RP', 'ULLP_PROTECT', '600,000,000 VND', '600 triệu', NULL, NULL);
INSERT INTO mbal_package
(id, "name", insurance_fee, str_insurance_fee, insurance_fee_str, package_code, product_name, product_code, "type", death_benefit, str_death_benefit, logo, payfrq_cd)
VALUES(10, 'An tâm bảo vệ', 15000000, '15.000.000 VND', '15 Triệu', 'ULPROTECT5_3', 'An tâm bảo vệ', 'UL2020-RP', 'ULLP_PROTECT', '900,000,000 VND', '900 triệu', NULL, NULL);
INSERT INTO mbal_package
(id, "name", insurance_fee, str_insurance_fee, insurance_fee_str, package_code, product_name, product_code, "type", death_benefit, str_death_benefit, logo, payfrq_cd)
VALUES(11, 'An tâm bảo vệ', 20000000, '20.000.000 VND', '20 Triệu', 'ULPROTECT5_4', 'An tâm bảo vệ', 'UL2020-RP', 'ULLP_PROTECT', '1,500,000,000 VND', '1.5 tỷ', NULL, NULL);
INSERT INTO mbal_package
(id, "name", insurance_fee, str_insurance_fee, insurance_fee_str, package_code, product_name, product_code, "type", death_benefit, str_death_benefit, logo, payfrq_cd)
VALUES(12, 'An tâm bảo vệ', 30000000, '30,000,000 VND', '30 Triệu', 'ULINVEST5_1', 'Vững bước đầu tư', 'UL2020-RP', 'ULLP_INVEST', '1,800,000,000 VND', '1.8 tỷ', NULL, NULL);
INSERT INTO mbal_package
(id, "name", insurance_fee, str_insurance_fee, insurance_fee_str, package_code, product_name, product_code, "type", death_benefit, str_death_benefit, logo, payfrq_cd)
VALUES(13, 'An tâm bảo vệ', 50000000, '50,000,000 VND', '50 Triệu', 'ULINVEST5_2', 'Vững bước đầu tư', 'UL2020-RP', 'ULLP_INVEST', '2,400,000,000 VND', '2.4 tỷ', NULL, NULL);
INSERT INTO mbal_package
(id, "name", insurance_fee, str_insurance_fee, insurance_fee_str, package_code, product_name, product_code, "type", death_benefit, str_death_benefit, logo, payfrq_cd)
VALUES(14, 'An tâm bảo vệ', 100000000, '100,000,000 VND', '100 Triệu', 'ULINVEST5_3', 'Vững bước đầu tư', 'UL2020-RP', 'ULLP_INVEST', '3,000,000,000 VND', '3 tỷ', NULL, NULL);

-- Insert insurance_package (HEALTHY)
INSERT INTO insurance_package
(id, package_name, total_fee, total_benefit, issuer, mic_package_id, sub_one, sub_two, sub_three, sub_four, mbal_package_id, mic_fee_payment_time, mic_periodic_fee_payment, mbal_fee_payment_time, mbal_periodic_fee_payment, "type")
VALUES(9, 'Gói Tự Tin', '', '325 triệu VND', 'MBAL + MIC', 1, false, false, false, false, 8, '1 năm', 'Không', '5 năm', 'Hàng năm', 'FIX_COMBO');
INSERT INTO insurance_package
(id, package_name, total_fee, total_benefit, issuer, mic_package_id, sub_one, sub_two, sub_three, sub_four, mbal_package_id, mic_fee_payment_time, mic_periodic_fee_payment, mbal_fee_payment_time, mbal_periodic_fee_payment, "type")
VALUES(10, 'Gói An Nhiên', '', '445 triệu VND', 'MBAL + MIC', 1, false, false, false, false, 9, '1 năm', 'Không', '5 năm', 'Hàng năm', 'FIX_COMBO');
INSERT INTO insurance_package
(id, package_name, total_fee, total_benefit, issuer, mic_package_id, sub_one, sub_two, sub_three, sub_four, mbal_package_id, mic_fee_payment_time, mic_periodic_fee_payment, mbal_fee_payment_time, mbal_periodic_fee_payment, "type")
VALUES(11, 'Gói Bình An', '', '745 triệu VND', 'MBAL + MIC', 1, false, false, false, false, 10, '1 năm', 'Không', '5 năm', 'Hàng năm', 'FIX_COMBO');
INSERT INTO insurance_package
(id, package_name, total_fee, total_benefit, issuer, mic_package_id, sub_one, sub_two, sub_three, sub_four, mbal_package_id, mic_fee_payment_time, mic_periodic_fee_payment, mbal_fee_payment_time, mbal_periodic_fee_payment, "type")
VALUES(12, 'Gói Vui Khỏe', '', '1.045 tỷ VND', 'MBAL + MIC', 1, false, false, false, false, 11, '1 năm', 'Không', '5 năm', 'Hàng năm', 'FIX_COMBO');
INSERT INTO insurance_package
(id, package_name, total_fee, total_benefit, issuer, mic_package_id, sub_one, sub_two, sub_three, sub_four, mbal_package_id, mic_fee_payment_time, mic_periodic_fee_payment, mbal_fee_payment_time, mbal_periodic_fee_payment, "type")
VALUES(13, 'Gói Hạnh Phúc', '', '1.47 tỷ VND', 'MBAL + MIC', 2, false, false, false, false, 12, '1 năm', 'Không', '5 năm', 'Hàng năm', 'FIX_COMBO');
INSERT INTO insurance_package
(id, package_name, total_fee, total_benefit, issuer, mic_package_id, sub_one, sub_two, sub_three, sub_four, mbal_package_id, mic_fee_payment_time, mic_periodic_fee_payment, mbal_fee_payment_time, mbal_periodic_fee_payment, "type")
VALUES(14, 'Gói Tận Hưởng', '', '2.53 tỷ VND', 'MBAL + MIC', 3, false, false, false, false, 13, '1 năm', 'Không', '5 năm', 'Hàng năm', 'FIX_COMBO');
INSERT INTO insurance_package
(id, package_name, total_fee, total_benefit, issuer, mic_package_id, sub_one, sub_two, sub_three, sub_four, mbal_package_id, mic_fee_payment_time, mic_periodic_fee_payment, mbal_fee_payment_time, mbal_periodic_fee_payment, "type")
VALUES(15, 'Gói Như Ý', '', '3.7 tỷ VND', 'MBAL + MIC', 4, false, false, false, false, 14, '1 năm', 'Không', '5 năm', 'Hàng năm', 'FIX_COMBO');
INSERT INTO insurance_package
(id, package_name, total_fee, total_benefit, issuer, mic_package_id, sub_one, sub_two, sub_three, sub_four, mbal_package_id, mic_fee_payment_time, mic_periodic_fee_payment, mbal_fee_payment_time, mbal_periodic_fee_payment, "type")
VALUES(16, 'Gói Phong Cách', '', '', 'MBAL + MIC', 1, false, false, false, false, null, '1 năm', 'Không', '5 năm', 'Hàng năm', 'FREE_STYLE');

-- Set value for category: 5 năm (HEALTHY), 10 năm (HAPPY)
update insurance_package set category = 'HAPPY' where id <= 8;
update insurance_package set category = 'HEALTHY' where id >= 9 and id <= 16;