drop procedure if exists get_odo_image_links;

create procedure get_odo_image_links(contract_code varchar(255), driver_code varchar(255), number_plate varchar(255),
                                     start_date date, end_date date)
begin
    declare image_base_url varchar(255) default 'http://10.0.0.47/images/';
    select c.code                                                                             as contract_code,
           date(convert_tz(jdd.date, '+00:00', '+07:00'))                                     as date,
           d.code                                                                             as driver_code,
           d.name                                                                             as driver_name,
           v.number_plate                                                                     as vehicle_number_plate,

           concat(image_base_url, regexp_substr(jd.image_odo_link_start, '[^/]*$'))           as image_odo_start,
           if(jd.km_odo_start is null, jd.km_driver_start, jd.km_odo_start)                   as km_start,
           if(jd.km_odo_start is null, if(jd.km_driver_start is null, null, 'MANUAL'),
              'AUTO')                                                                         as km_start_source,

           concat(image_base_url, regexp_substr(jd.image_odo_link_customer_get_in, '[^/]*$')) as image_odo_customer_in,
           if(jd.km_odo_customer_get_in is null, jd.km_driver_customer_get_in,
              jd.km_odo_customer_get_in)                                                      as km_customer_in,
           if(jd.km_odo_customer_get_in is null, if(jd.km_driver_customer_get_in is null, null, 'MANUAL'),
              'AUTO')                                                                         as km_customer_in_source,

           concat(image_base_url,
                  regexp_substr(jd.image_odo_link_customer_get_out, '[^/]*$'))                as image_odo_customer_out,
           if(jd.km_odo_customer_get_out is null, jd.km_driver_customer_get_out,
              jd.km_odo_customer_get_out)                                                     as km_customer_out,
           if(jd.km_odo_customer_get_out is null, if(jd.km_driver_customer_get_out is null, null, 'MANUAL'),
              'AUTO')                                                                         as km_customer_out_source,

           concat(image_base_url, regexp_substr(jd.image_odo_link_end, '[^/]*$'))             as image_odo_end,
           if(jd.km_odo_end is null, jd.km_driver_end, jd.km_odo_end)                         as km_end,
           if(jd.km_odo_end is null, if(jd.km_driver_end is null, null, 'MANUAL'),
              'AUTO')                                                                         as km_end_source
    from journey_diary_daily jdd
             left join contract c on jdd.contract_id = c.id
             left join journey_diary jd on jdd.journey_diary_id = jd.id
             left join user d on jdd.driver_id = d.id
             left join vehicle v on jdd.vehicle_id = v.id
    where c.contract_type_id = 1
      and jd.id is not null
      and d.id is not null
      and v.id is not null
      and (contract_code is null or c.code like upper(contract_code))
      and (driver_code is null or d.code like upper(driver_code))
      and (number_plate is null or v.number_plate like upper(number_plate))
      and (start_date is null or date(convert_tz(jdd.date, '+00:00', '+07:00')) >= start_date)
      and (end_date is null or date(convert_tz(jdd.date, '+00:00', '+07:00')) <= end_date)
    order by c.code, date, jd.id;
end;

call get_odo_image_links(null, null, null, null, null);
