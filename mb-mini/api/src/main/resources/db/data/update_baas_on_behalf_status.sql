-- Update status baas_pay_on_behalf
update baas_pay_on_behalf
set status = 'SUCCESS' where error_code = '000';

update baas_pay_on_behalf as bpob
set mbal_hook_status = true where bpob.error_code = '000' and bpob.type = 'MBAL';
