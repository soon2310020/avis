drop procedure if exists get_vehicle_audit_logs;

create procedure get_vehicle_audit_logs(number_plate varchar(255), start_date date, end_date date)
begin
    select rev.rev                                                            as id,
           if(v.revtype = 0, 'CREATE', if(v.is_deleted, 'DELETE', 'UPDATE'))  as action,
           v.number_plate                                                     as number_plate,
           v.type                                                             as type,
           if(v.transmission_type = 1, 'MT', 'AT')                            as transmission_type,
           v.color                                                            as color,
           v.number_seat                                                      as number_seat,
           v.model                                                            as model,
           v.chassis_no                                                       as chassis_no,
           v.engine_no                                                        as engine_no,
           v.year_manufacture                                                 as year_manufacture,
           date(convert_tz(v.start_using_date, '+00:00', '+07:00'))           as start_using_date,
           v.registration_no                                                  as registration_no,
           date(convert_tz(v.travel_warrant_expiry_date, '+00:00', '+07:00')) as travel_warrant_expiry_date,
           date(convert_tz(v.registration_to_date, '+00:00', '+07:00'))       as registration_to_date,
           v.insurance_no                                                     as insurance_no,
           date(convert_tz(v.insurance_expiry_date, '+00:00', '+07:00'))      as insurance_expiry_date,
           date(convert_tz(v.road_fee_expiry_date, '+00:00', '+07:00'))       as road_fee_expiry_date,
           ftg.name                                                           as fuel_type,
           v.place_of_origin                                                  as place_of_origin,
           vsg.name                                                           as source,
           v.owner                                                            as owner,
           b.name                                                             as management_area,
           if(v.status = 0, 'KTDD', if(v.status = 1, 'CDD', 'DDD'))           as status,
           date(convert_tz(v.liquidation_date, '+00:00', '+07:00'))           as liquidation_date,
           oa.code                                                            as operation_admin_code,
           oa.name                                                            as operation_admin_name,
           uo.code                                                            as unit_operator_code,
           uo.name                                                            as unit_operator_name,
           a.code                                                             as accountant_code,
           a.name                                                             as accountant_name,
           v.note                                                             as note,
           cre.code                                                           as creator_code,
           cre.name                                                           as creator_name,
           convert_tz(rev.created_at, '+00:00', '+07:00')                     as datetime
    from revinfo rev
             left join action_type at on rev.action_type = at.id
             left join user cre on rev.created_by = cre.id
             left join vehicle_aud v on rev.rev = v.rev
             left join fuel_type_group ftg on v.fuel_type_group_id = ftg.id
             left join vehicle_supplier_group vsg on v.vehicle_supplier_group_id = vsg.id
             left join branch b on v.branch_id = b.id
             left join user oa on v.operation_admin_id = oa.id
             left join user uo on v.unit_operator_id = uo.id
             left join user a on v.accountant_id = a.id
    where rev.created_by is not null
      and at.code = 'VEHICLE'
      and (number_plate is null or v.number_plate like upper(number_plate))
      and (start_date is null or date(convert_tz(rev.created_at, '+00:00', '+07:00')) >= start_date)
      and (end_date is null or date(convert_tz(rev.created_at, '+00:00', '+07:00')) <= end_date)
    order by rev.rev, v.number_plate;
end;

call get_vehicle_audit_logs(null, null, null);