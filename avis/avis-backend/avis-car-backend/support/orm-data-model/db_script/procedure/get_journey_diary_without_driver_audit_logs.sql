drop procedure if exists get_journey_diary_without_driver_audit_logs;

create procedure get_journey_diary_without_driver_audit_logs(contract_code varchar(255),
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
           jdd.trip_itinerary                                                       as trip_itinerary,
           jdd.km_start                                                             as km_start,
           jdd.km_end                                                               as km_end,
           jdd.total_km                                                             as total_km,
           if(jdd.driver_id is not null, d.name, jdd.driver_name)                   as driver_name,
           if(jdd.vehicle_id is not null, v.number_plate, jdd.vehicle_number_plate) as number_plate,
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
    where rev.created_by is not null
      and at.code = 'DIARY'
      and ct.id = 2
      and (contract_code is null or c.code like upper(contract_code))
      and (journey_diary_date is null or date(convert_tz(jdd.date, '+00:00', '+07:00')) = journey_diary_date)
      and (start_date is null or date(convert_tz(rev.created_at, '+00:00', '+07:00')) >= start_date)
      and (end_date is null or date(convert_tz(rev.created_at, '+00:00', '+07:00')) <= end_date)
    order by rev.rev, c.code, jdd.date, jdd.id;
end;

call get_journey_diary_without_driver_audit_logs(null, null, null, null);