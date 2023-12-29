-- Update installment_status insurance_payment

UPDATE insurance_payment
SET installment_status = 'INSTALLMENT_FAILED' where installment_status = 'Đăng ký trả góp chưa thành công';

UPDATE insurance_payment
SET installment_status = 'INSTALLMENT_UNQUALIFIED' where installment_status = 'Chưa đủ điều kiện đăng ký trả góp';

UPDATE insurance_payment
SET installment_status = 'INSTALLMENT_SUCCESS' where installment_status = 'Đăng ký trả góp thành công';

UPDATE insurance_payment
SET installment_status = 'INSTALLMENT_UNQUALIFIED_CANCEL' where installment_status = 'Không đủ điều kiện đăng ký. Hủy đăng ký';
