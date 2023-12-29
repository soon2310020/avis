drop procedure if exists get_enterprise_customer_audit_logs;

create procedure get_enterprise_customer_audit_logs(customer_name varchar(255),
                                                    customer_user_code varchar(255),
                                                    start_date date,
                                                    end_date date)
begin
    select rev.rev                                                            as id,
           if(c.revtype = 0, 'CREATE', if(mc.is_deleted, 'DELETE', 'UPDATE')) as action,
           at.name                                                            as target,
           ct.name                                                            as type,
           c.name                                                             as name,
           c.address                                                          as address,
           c.tax_code                                                         as tax_code,
           c.bank_account_holder                                              as bank_account_holder,
           c.bank_name                                                        as bank_name,
           c.bank_account_number                                              as bank_account_number,
           mc.role                                                            as member_role,
           mc.code                                                            as member_code,
           mc.name                                                            as member_name,
           mc.iso2                                                            as member_country,
           concat(mc.country_code, mc.mobile)                                 as member_phone_number,
           mc.email                                                           as member_email,
           mc.department                                                      as member_department,
           if(mc.active, 'Active', 'Inactive')                                as member_status,
           cre.code                                                           as creator_code,
           cre.name                                                           as creator_name,
           convert_tz(rev.created_at, '+00:00', '+07:00')                     as datetime
    from revinfo rev
             left join action_type at on rev.action_type = at.id
             left join user cre on rev.created_by = cre.id
             left join customer_aud c on rev.rev = c.rev
             left join member_customer_aud mc on rev.rev = mc.rev and (c.id is null or c.id = mc.customer_id)
             left join customer mcc on mc.customer_id = mcc.id
             left join customer_type ct on (c.customer_type_id = ct.id or mcc.customer_type_id = ct.id)
    where rev.created_by is not null
      and at.code = 'CUSTOMER'
      and ct.id = 2
      and (customer_name is null or c.name like upper(customer_name))
      and (customer_user_code is null or mc.code like upper(customer_user_code))
      and (start_date is null or date(convert_tz(rev.created_at, '+00:00', '+07:00')) >= start_date)
      and (end_date is null or date(convert_tz(rev.created_at, '+00:00', '+07:00')) <= end_date)
    order by rev.rev, c.name, mc.code;
end;

call get_enterprise_customer_audit_logs(null, null, null, null);