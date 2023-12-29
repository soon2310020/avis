alter table journey_diary
    add station_fee_images varchar(1000) null after step;

alter table journey_diary
    add note varchar(1000) null after station_fee_images;

alter table journey_diary_aud
    add station_fee_images varchar(1000) null after step;

alter table journey_diary_aud
    add note varchar(1000) null after station_fee_images;

create table journey_diary_station_fee
(
    id int auto_increment,
    journey_diary_id int null,
    driver_id int null,
    vehicle_id int null,
    vehicle_plate varchar(255) null,
    private_code varchar(255) null,
    vehicle_type_name varchar(255) null,
    bot_name varchar(255) null,
    stage_name varchar(255) null,
    in_name varchar(255) null,
    in_time datetime null,
    out_name varchar(255) null,
    out_time datetime null,
    check_name varchar(255) null,
    check_time datetime null,
    km_on_stage decimal(13,2) null,
    fee decimal(11) null,
    note varchar(1000) null,
    station_confirm bit null,
    fee_confirm bit null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null,
    constraint journey_diary_daily_station_fee_pk
        primary key (id)
);

alter table journey_diary_station_fee
    add constraint journey_diary_station_fee_driver
        foreign key (driver_id) references user (id);

alter table journey_diary_station_fee
    add constraint journey_diary_station_fee_journey_diary
        foreign key (journey_diary_id) references journey_diary (id);

alter table journey_diary_station_fee
    add constraint journey_diary_station_fee_vehicle
        foreign key (vehicle_id) references vehicle (id);

create table journey_diary_station_fee_aud
(
    rev int not null,
    revtype smallint null,
    id int not null,
    journey_diary_id int null,
    driver_id int null,
    vehicle_id int null,
    vehicle_plate varchar(255) null,
    private_code varchar(255) null,
    vehicle_type_name varchar(255) null,
    bot_name varchar(255) null,
    stage_name varchar(255) null,
    in_name varchar(255) null,
    in_time datetime null,
    out_name varchar(255) null,
    out_time datetime null,
    check_name varchar(255) null,
    check_time datetime null,
    km_on_stage decimal(13,2) null,
    fee decimal(11) null,
    note varchar(1000) null,
    station_confirm bit null,
    fee_confirm bit null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null,
    primary key (rev, id),
    constraint journey_diary_station_fee_aud_revinfo
        foreign key (rev) references revinfo (rev)
);


