alter table journey_diary
    add screenshot varchar(255) null after station_fee_images;

alter table journey_diary_aud
    add screenshot varchar(255) null after station_fee_images;

alter table journey_diary_daily
    add confirmation_screenshot varchar(255) null after station_fee_images;

alter table journey_diary_daily_aud
    add confirmation_screenshot varchar(255) null after station_fee_images;