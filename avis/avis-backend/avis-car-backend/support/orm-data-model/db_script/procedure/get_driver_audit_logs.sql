drop procedure if exists get_driver_audit_logs;

create procedure get_driver_audit_logs(driver_code varchar(255), start_date date, end_date date)
begin
    select rev.rev                                                            as id,
           if(d.revtype = 0, 'CREATE', if(d.is_deleted, 'DELETE', 'UPDATE'))  as action,
           d.code                                                             as code,
           d.name                                                             as name,
           d.id_card                                                          as personal_id_number,
           if(d.card_type = 1, 'CMND',
              if(d.card_type = 2, 'CCCD', 'HC'))                              as card_type,
           date(convert_tz(d.birthdate, '+00:00', '+07:00'))                  as date_of_birth,
           d.address                                                          as address,
           d.iso2                                                             as country_code,
           concat(d.country_code, d.mobile)                                   as phone_number,
           d.email                                                            as email,
           d.driver_licenses                                                  as driver_licenses,
           ug.name                                                            as human_resource,
           b.name                                                             as branch,
           d.driver_license_number                                            as driver_license_number,
           date(convert_tz(d.driver_license_expiry_date, '+00:00', '+07:00')) as driver_license_expiry_date,
           if(d.status = 0, 'KTDD', if(d.status = 1, 'CDD', 'DDD'))           as driver_status,
           if(d.active, 'Active', 'Inactive')                                 as account_status,
           if(d.know_english, 'Yes', 'No')                                    as know_english,
           if(d.ddt_certificate, 'Yes', 'No')                                 as ddt_certificate,
           uo.code                                                            as unit_operator_code,
           uo.name                                                            as unit_operator_name,
           cre.code                                                           as creator_code,
           cre.name                                                           as creator_name,
           d.note                                                             as note,
           convert_tz(rev.created_at, '+00:00', '+07:00')                     as datetime
    from revinfo rev
             left join action_type at on rev.action_type = at.id
             left join user cre on rev.created_by = cre.id
             left join user_aud d on rev.rev = d.rev
             left join user_role ur on d.user_role_id = ur.id
             left join user_group ug on d.user_group_id = ug.id
             left join branch b on d.branch_id = b.id
             left join user uo on d.unit_operator_id = uo.id
    where rev.created_by is not null
      and at.code = 'DRIVER'
      and d.user_role_id = 3
      and (driver_code is null or d.code like upper(driver_code))
      and (start_date is null or date(convert_tz(rev.created_at, '+00:00', '+07:00')) >= start_date)
      and (end_date is null or date(convert_tz(rev.created_at, '+00:00', '+07:00')) <= end_date)
    order by rev.rev, d.code;
end;

call get_driver_audit_logs(null, null, null);