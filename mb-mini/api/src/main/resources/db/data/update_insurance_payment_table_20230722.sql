-- Update insurance_payment for installment
UPDATE insurance_payment ip
SET installment = false, normal= true
WHERE (way4_docs_id is null or installment_status is null);