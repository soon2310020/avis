-- Update insurance_payment
ALTER TABLE insurance_payment ADD normal bool NOT NULL DEFAULT true;

update insurance_payment
set installment = false;

ALTER TABLE insurance_payment ADD auto_pay_set_time timestamp;
ALTER TABLE insurance_payment ADD auto_pay_status bool;

CREATE INDEX payment_transaction_id_idx ON insurance_payment (transaction_id);

CREATE INDEX contract_transaction_id_idx ON insurance_contract (transaction_id);
