drop procedure if exists get_contract_without_driver_audit_logs;

create procedure get_contract_without_driver_audit_logs(contract_code varchar(255),
                                         start_date date,
                                         end_date date)
begin
    select rev.rev                                                          as id,
           if(c.revtype = 0, 'CREATE',
              if(c.is_deleted, 'DELETE',
                 if(c.date_early_termination is null, 'UPDATE', 'CANCEL'))) as action,
           at.name                                                          as target,
           ct.name                                                          as contract_type,
           c.code                                                           as contract_code,
           b.code                                                           as branch,
           c.vehicle_working_area                                           as vehicle_working_area,
           date(convert_tz(c.sign_date, '+00:00', '+07:00'))                as sign_date,
           cu.code                                                          as customer_code,
           cu.name                                                          as customer_name,
           mc.code                                                          as customer_user_code,
           mc.name                                                          as customer_user_name,
           mc.role                                                          as customer_user_role,
           d.code                                                           as driver_code,
           d.name                                                           as driver_name,
           v.number_plate                                                   as vehicle_number_plate,
           date(convert_tz(c.from_datetime, '+00:00', '+07:00'))            as start_date,
           date(convert_tz(c.to_datetime, '+00:00', '+07:00'))              as end_date,
           rst.name                                                         as rental_type,
           if(c.return_vehicle_early, 'Yes', 'No')                          as return_vehicle_early,
           c.days_inform_before_early_termination                           as days_inform_before_cancel_early,
           c.penalty_rate_early_termination                                 as penalty_rate_cancel_early,
           cct1.price                                                       as rental_price_per_month,
           cnl1.quota                                                       as km_norm,
           cnl3.quota                                                       as fuel_norm,
           c.fuel_adjust_percent                                            as fuel_adjustment_percent,
           ft.name                                                          as fuel_type,
           cct19.price                                                      as fuel_price,
           cct3.price                                                       as over_km_surcharge,
           c.deposit                                                        as deposit,
           c.payment_term                                                   as payment_term,
           c.days_inform_before_return_vehicle                              as days_inform_before_return_vehicle,
           c.note                                                           as note,
           cre.code                                                         as creator_code,
           cre.name                                                         as creator_name,
           convert_tz(rev.created_at, '+00:00', '+07:00')                   as datetime
    from revinfo rev
             left join action_type at on rev.action_type = at.id
             left join user cre on rev.created_by = cre.id
             left join contract_aud c on rev.rev = c.rev
             left join contract_type ct on c.contract_type_id = ct.id
             left join branch b on c.branch_id = b.id
             left join customer cu on c.customer_id = cu.id
             left join member_customer mc on c.member_customer_id = mc.id
             left join user d on c.driver_id = d.id
             left join vehicle v on c.vehicle_id = v.id
             left join rental_service_type rst on c.rental_service_type_id = rst.id
             left join working_day wd on c.working_day_id = wd.id
             left join fuel_type ft on c.fuel_type_id = ft.id
             left join contract_cost_type cct1 on c.id = cct1.contract_id and cct1.cost_type_id = 1
             left join contract_cost_type cct2 on c.id = cct2.contract_id and cct2.cost_type_id = 2
             left join contract_cost_type cct3 on c.id = cct3.contract_id and cct3.cost_type_id = 3
             left join contract_cost_type cct5 on c.id = cct5.contract_id and cct5.cost_type_id = 5
             left join contract_cost_type cct6 on c.id = cct6.contract_id and cct6.cost_type_id = 6
             left join contract_cost_type cct14 on c.id = cct14.contract_id and cct14.cost_type_id = 14
             left join contract_cost_type cct15 on c.id = cct15.contract_id and cct15.cost_type_id = 15
             left join contract_cost_type cct16 on c.id = cct16.contract_id and cct16.cost_type_id = 16
             left join contract_cost_type cct17 on c.id = cct17.contract_id and cct17.cost_type_id = 17
             left join contract_cost_type cct18 on c.id = cct18.contract_id and cct18.cost_type_id = 18
             left join contract_cost_type cct19 on c.id = cct19.contract_id and cct19.cost_type_id = 19
             left join contract_cost_type cct20 on c.id = cct20.contract_id and cct20.cost_type_id = 20
             left join contract_norm_list cnl1 on c.id = cnl1.contract_id and cnl1.norm_list_id = 1
             left join contract_norm_list cnl2 on c.id = cnl2.contract_id and cnl2.norm_list_id = 2
             left join contract_norm_list cnl3 on c.id = cnl3.contract_id and cnl3.norm_list_id = 3
             left join contract_norm_list cnl4 on c.id = cnl4.contract_id and cnl4.norm_list_id = 4
             left join contract_norm_list cnl5 on c.id = cnl5.contract_id and cnl5.norm_list_id = 5
             left join contract_norm_list cnl6 on c.id = cnl6.contract_id and cnl6.norm_list_id = 6
             left join contract_norm_list cnl7 on c.id = cnl7.contract_id and cnl7.norm_list_id = 7
    where rev.created_by is not null
      and at.code = 'CONTRACT'
      and ct.id = 2
      and (contract_code is null or c.code like upper(contract_code))
      and (start_date is null or date(convert_tz(rev.created_at, '+00:00', '+07:00')) >= start_date)
      and (end_date is null or date(convert_tz(rev.created_at, '+00:00', '+07:00')) <= end_date)
    order by rev.rev, c.code;
end;

call get_contract_without_driver_audit_logs(null, null, null);