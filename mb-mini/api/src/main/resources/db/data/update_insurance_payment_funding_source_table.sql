-- Update insurance_payment
UPDATE insurance_payment
SET  funding_source='CARD'
WHERE installment = true;