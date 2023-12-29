drop procedure if exists get_individual_customer_logs;

create procedure get_individual_customer_logs(customer_code varchar(255),
                                              start_date date,
                                              end_date date)
begin
    select rev.rev                                                           as id,
           if(c.revtype = 0, 'CREATE', if(c.is_deleted, 'DELETE', 'UPDATE')) as action,
           at.name                                                           as target,
           ct.name                                                           as type,
           c.code                                                            as code,
           c.name                                                            as name,
           c.address                                                         as address,
           c.id_card                                                         as personal_id_number,
           if(c.card_type = 1, 'CMND',
              if(c.card_type = 2, 'CCCD', 'HC'))                             as card_type,
           c.bank_account_holder                                             as bank_account_holder,
           c.bank_name                                                       as bank_name,
           c.bank_account_number                                             as bank_account_number,
           c.iso2                                                            as country,
           concat(c.country_code, c.mobile)                                  as phone_number,
           c.email                                                           as email,
           c.position                                                        as department,
           if(c.active, 'Active', 'Inactive')                                as status,
           cre.code                                                          as creator_code,
           cre.name                                                          as creator_name,
           convert_tz(rev.created_at, '+00:00', '+07:00')                    as datetime
    from revinfo rev
             left join action_type at on rev.action_type = at.id
             left join user cre on rev.created_by = cre.id
             left join customer_aud c on rev.rev = c.rev
             left join customer_type ct on c.customer_type_id = ct.id
    where rev.created_by is not null
      and at.code = 'CUSTOMER'
      and ct.id = 1
      and (customer_code is null or c.code like upper(customer_code))
      and (start_date is null or date(convert_tz(rev.created_at, '+00:00', '+07:00')) >= start_date)
      and (end_date is null or date(convert_tz(rev.created_at, '+00:00', '+07:00')) <= end_date)
    order by rev.rev, c.code;
end;

call get_individual_customer_logs(null, null, null);