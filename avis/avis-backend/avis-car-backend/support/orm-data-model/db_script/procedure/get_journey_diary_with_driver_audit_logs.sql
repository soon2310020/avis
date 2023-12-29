drop procedure if exists get_journey_diary_with_driver_audit_logs;

create procedure get_journey_diary_with_driver_audit_logs(contract_code varchar(255),
                                                          journey_diary_date date,
                                                          start_date date,
                                                          end_date date)
begin
    select rev.rev                                                                  as id,
           if(jdd.revtype = 0, 'CREATE', if(jdd.is_deleted, 'DELETE', 'UPDATE'))    as action,
           at.name                                                                  as target,
           ct.name                                                                  as contract_type,
           c.code                                                                   as contract_code,
           date(convert_tz(jdd.date, '+00:00', '+07:00'))                           as date,
           jdd.customer_name_used                                                   as customer_name,
           jdd.customer_department                                                  as customer_department,
           jdd.trip_itinerary                                                       as trip_itinerary,
           jdd.km_start                                                             as km_start,
           jdd.km_customer_get_in                                                   as km_customer_get_in,
           jdd.km_customer_get_out                                                  as km_customer_get_out,
           jdd.km_end                                                               as km_end,
           jdd.empty_km                                                             as empty_km,
           jdd.total_km                                                             as total_km,
           jdd.used_km                                                              as used_km,
           jdd.used_km_self_drive                                                   as used_km_self_drive,
           jdd.working_time_gps_from                                                as working_time_gps_from,
           jdd.working_time_gps_to                                                  as working_time_gps_to,
           jdd.over_time                                                            as overtime,
           jdd.over_km                                                              as over_km,
           jdd.over_km_self_drive                                                   as over_km_self_drive,
           jdd.overnight                                                            as overnight,
           jdd.is_holiday                                                           as is_holiday,
           jdd.is_weekend                                                           as is_weekend,
           if(jdd.driver_id is not null, d.name, jdd.driver_name)                   as driver_name,
           if(jdd.vehicle_id is not null, v.number_plate, jdd.vehicle_number_plate) as number_plate,
           jct10.value                                                              as car_washing_fee,
           jct8.value                                                               as parking_fee,
           jct9.value                                                               as tire_reparing_fee,
           jct13.value                                                              as other_fee,
           jct7.value                                                               as tolls_fee,
           jct11.value                                                              as night_storage_fee,
           jct12.value                                                              as incidents_fee,
           jdd.note                                                                 as note,
           cre.code                                                                 as creator_code,
           cre.name                                                                 as creator_name,
           convert_tz(rev.created_at, '+00:00', '+07:00')                           as datetime
    from revinfo rev
             left join action_type at on rev.action_type = at.id
             left join user cre on rev.created_by = cre.id
             left join journey_diary_daily_aud jdd on rev.rev = jdd.rev
             left join contract c on jdd.contract_id = c.id
             left join contract_type ct on c.contract_type_id = ct.id
             left join user d on jdd.driver_id = d.id
             left join vehicle v on jdd.vehicle_id = v.id
             left join journey_diary_daily_cost_type jct7
                       on jdd.id = jct7.journey_diary_daily_id and jct7.cost_type_id = 7
             left join journey_diary_daily_cost_type jct8
                       on jdd.id = jct8.journey_diary_daily_id and jct8.cost_type_id = 8
             left join journey_diary_daily_cost_type jct9
                       on jdd.id = jct9.journey_diary_daily_id and jct9.cost_type_id = 9
             left join journey_diary_daily_cost_type jct10
                       on jdd.id = jct10.journey_diary_daily_id and jct10.cost_type_id = 10
             left join journey_diary_daily_cost_type jct11
                       on jdd.id = jct11.journey_diary_daily_id and jct11.cost_type_id = 11
             left join journey_diary_daily_cost_type jct12
                       on jdd.id = jct12.journey_diary_daily_id and jct12.cost_type_id = 12
             left join journey_diary_daily_cost_type jct13
                       on jdd.id = jct13.journey_diary_daily_id and jct13.cost_type_id = 13
    where rev.created_by is not null
      and at.code = 'DIARY'
      and ct.id = 1
      and (contract_code is null or c.code like upper(contract_code))
      and (journey_diary_date is null or date(convert_tz(jdd.date, '+00:00', '+07:00')) = journey_diary_date)
      and (start_date is null or date(convert_tz(rev.created_at, '+00:00', '+07:00')) >= start_date)
      and (end_date is null or date(convert_tz(rev.created_at, '+00:00', '+07:00')) <= end_date)
    order by rev.rev, c.code, jdd.date, jdd.id;
end;

call get_journey_diary_with_driver_audit_logs(null, null, null, null);