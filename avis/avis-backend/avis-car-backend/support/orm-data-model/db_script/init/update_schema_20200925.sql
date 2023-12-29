alter table journey_diary_daily
    add station_fee_images varchar(1000) null after image_customer_get_out_link;

alter table journey_diary_daily_aud
    add station_fee_images varchar(1000) null after image_customer_get_out_link;
