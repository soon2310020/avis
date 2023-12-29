create table DATABASECHANGELOG
(
    ID varchar(255) not null,
    AUTHOR varchar(255) not null,
    FILENAME varchar(255) not null,
    DATEEXECUTED datetime not null,
    ORDEREXECUTED int not null,
    EXECTYPE varchar(10) not null,
    MD5SUM varchar(35) null,
    DESCRIPTION varchar(255) null,
    COMMENTS varchar(255) null,
    TAG varchar(255) null,
    LIQUIBASE varchar(20) null,
    CONTEXTS varchar(255) null,
    LABELS varchar(255) null,
    DEPLOYMENT_ID varchar(10) null
);

create table DATABASECHANGELOGLOCK
(
    ID int not null
        primary key,
    LOCKED bit not null,
    LOCKGRANTED datetime null,
    LOCKEDBY varchar(255) null
);

create table action_type
(
    id int auto_increment
        primary key,
    code varchar(255) null,
    name varchar(255) null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null
);

create table branch
(
    id int auto_increment
        primary key,
    code varchar(255) not null,
    name varchar(255) null,
    is_deleted bit not null,
    created_at datetime null,
    updated_at datetime null
);

create table contract_period_type
(
    id int auto_increment
        primary key,
    name varchar(255) not null,
    is_deleted bit not null,
    created_at datetime null,
    updated_at datetime null
);

create table contract_type
(
    id int auto_increment
        primary key,
    name varchar(255) not null,
    is_deleted bit not null,
    created_at datetime null,
    updated_at datetime null
);

create table cost_type
(
    id int auto_increment
        primary key,
    code varchar(255) not null,
    name varchar(255) null,
    type varchar(255) null,
    is_deleted bit not null,
    created_at datetime null,
    updated_at datetime null
);

create table customer_type
(
    id int auto_increment
        primary key,
    code varchar(100) not null,
    name varchar(100) not null,
    is_deleted bit not null,
    created_at datetime not null,
    updated_at datetime not null,
    constraint customer_type_UN
        unique (code)
);

create table customer
(
    id int auto_increment
        primary key,
    user_id int null,
    code varchar(255) null,
    name varchar(255) null,
    position varchar(255) null,
    iso2 varchar(100) null,
    country_code varchar(100) null,
    mobile varchar(255) null,
    email varchar(255) null,
    address varchar(255) null,
    id_card varchar(255) null,
    card_type int null,
    tax_code varchar(255) null,
    bank_account_number varchar(100) null,
    bank_account_holder varchar(100) null,
    bank_name varchar(100) null,
    bank_branch varchar(100) null,
    customer_type_id int null,
    active bit default b'1' null,
    in_contract bit default b'0' null,
    created_by_id int null,
    updated_by_id int null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null,
    constraint customer_FK
        foreign key (customer_type_id) references customer_type (id)
);

create table department
(
    id int auto_increment
        primary key,
    name varchar(255) null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null
);

create table email
(
    id int auto_increment
        primary key,
    name varchar(255) null,
    address varchar(255) null,
    active bit default b'1' null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null
);

create table fuel_type_group
(
    id int auto_increment
        primary key,
    name varchar(255) null,
    is_deleted bit default b'0' not null,
    created_at datetime null,
    updated_at datetime null
);

create table fuel_type
(
    id int auto_increment
        primary key,
    code varchar(100) collate utf8_unicode_ci not null,
    name varchar(255) null,
    fuel_type_group_id int null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null,
    constraint fuel_type_fuel_type_group
        foreign key (fuel_type_group_id) references fuel_type_group (id)
);

create table holiday
(
    id int auto_increment
        primary key,
    date datetime not null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null
);

create table mapping_field_code_fontend
(
    id int auto_increment
        primary key,
    field_name varchar(100) not null,
    table_name varchar(100) not null,
    code_fontend varchar(100) not null,
    name varchar(100) not null,
    unit varchar(100) null,
    is_deleted bit not null,
    created_at datetime not null,
    updated_at datetime not null
);

create table member_customer
(
    id int auto_increment
        primary key,
    user_id int null,
    code varchar(100) null,
    name varchar(100) not null,
    department varchar(100) null,
    role varchar(100) not null,
    parent_id int null,
    email varchar(100) null,
    iso2 varchar(100) null,
    country_code varchar(100) null,
    mobile varchar(100) null,
    customer_id int not null,
    active bit default b'1' null,
    in_contract bit default b'0' null,
    is_deleted bit default b'0' not null,
    created_at datetime null,
    updated_at datetime null,
    constraint member_customer_FK
        foreign key (parent_id) references member_customer (id),
    constraint member_customer_customer
        foreign key (customer_id) references customer (id)
            on update cascade on delete cascade
);

create table norm_list
(
    id int auto_increment
        primary key,
    code varchar(255) not null,
    name varchar(255) null,
    unit varchar(100) null,
    type varchar(255) null,
    is_deleted bit not null,
    created_at datetime null,
    updated_at datetime null
);

create table notification_content
(
    id int auto_increment
        primary key,
    title varchar(255) null,
    content varchar(512) null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null
);

create table oauth_access_token
(
    token_id varchar(255) null,
    token blob null,
    authentication_id varchar(255) null,
    user_name varchar(255) null,
    client_id varchar(255) null,
    authentication blob null,
    refresh_token varchar(255) null
);

create table oauth_client_details
(
    client_id varchar(64) not null
        primary key,
    resource_ids varchar(255) null,
    client_secret varchar(255) null,
    scope varchar(255) null,
    authorized_grant_types varchar(255) null,
    web_server_redirect_uri varchar(255) null,
    authorities varchar(255) null,
    access_token_validity int null,
    refresh_token_validity int null,
    additional_information varchar(4096) null,
    autoapprove tinyint null
);

create table oauth_client_token
(
    token_id varchar(255) null,
    token blob null,
    authentication_id varchar(255) null,
    user_name varchar(255) null,
    client_id varchar(255) null
);

create table oauth_code
(
    code varchar(255) null,
    authentication blob null
);

create table oauth_refresh_token
(
    token_id varchar(255) null,
    token blob null,
    authentication blob null
);

create table rental_service_type
(
    id int auto_increment
        primary key,
    code varchar(255) null,
    name varchar(255) null,
    contract_type_id int not null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null,
    constraint rental_service_type_FK
        foreign key (contract_type_id) references contract_type (id)
);

create table revinfo
(
    rev int auto_increment
        primary key,
    action_type int null,
    created_by int null,
    created_at datetime null,
    timestamp mediumtext null
);

create table contract_aud
(
    rev int not null,
    revtype smallint null,
    id int not null,
    prefix_code varchar(100) null,
    suffix_code varchar(100) null,
    code varchar(100) null,
    contract_period_type_id int null,
    contract_type_id int null,
    term int null,
    branch_id int null,
    vehicle_working_area varchar(255) null,
    sign_date datetime null,
    customer_id int null,
    member_customer_id int null,
    driver_id int null,
    driver_is_transferred_another bit null,
    driver_is_transferred_another_at datetime null,
    driver_know_english bit null,
    vehicle_id int null,
    vehicle_is_transferred_another bit null,
    vehicle_is_transferred_another_at datetime null,
    parking_id int null,
    from_datetime datetime null,
    to_datetime datetime null,
    rental_service_type_id int null,
    working_time_weekend_holiday_from time null,
    working_time_weekend_holiday_to time null,
    time_use_policy_group_id int null,
    deposit decimal(11) null,
    payment_term int null,
    vat_toll_fee int null,
    is_include_empty_km bit null,
    working_time_from time null,
    working_time_to time null,
    working_day_id int null,
    working_day int null,
    fuel_type_id int null,
    return_vehicle_early bit null,
    days_inform_before_return_vehicle int null,
    days_inform_before_early_termination int null,
    fuel_adjust_percent decimal(5,2) null,
    penalty_rate_early_termination decimal(5,2) null,
    date_early_termination datetime null,
    status int null,
    note varchar(1000) null comment 'note',
    created_by_user_id int null,
    include_appendix bit null,
    canceled_at datetime null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null,
    primary key (rev, id),
    constraint contract_aud_revinfo
        foreign key (rev) references revinfo (rev)
);

create table contract_change_history_aud
(
    rev int not null,
    revtype smallint null,
    id int not null,
    contract_id int not null,
    mapping_field_code_fontend_id int null,
    old_value varchar(255) null,
    new_value varchar(255) null,
    from_date datetime null,
    to_date datetime null,
    created_by_id int null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null,
    primary key (rev, id),
    constraint contract_change_history_aud_revinfo
        foreign key (rev) references revinfo (rev)
);

create table contract_cost_type_aud
(
    rev int not null,
    revtype smallint null,
    contract_id int not null,
    cost_type_id int not null,
    price decimal(11) null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null,
    primary key (rev, contract_id, cost_type_id),
    constraint contract_cost_type_aud_revinfo
        foreign key (rev) references revinfo (rev)
);

create table contract_driver_history_aud
(
    rev int not null,
    revtype smallint null,
    id int not null,
    user_id int not null,
    contract_id int null,
    from_date datetime null,
    to_date datetime null,
    status int null,
    is_deleted int null,
    created_at datetime null,
    updated_at datetime null,
    primary key (rev, id),
    constraint contract_driver_history_aud_revinfo
        foreign key (rev) references revinfo (rev)
);

create table contract_norm_list_aud
(
    rev int not null,
    revtype smallint null,
    contract_id int not null,
    norm_list_id int not null,
    quota decimal(12,1) null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null,
    primary key (rev, contract_id, norm_list_id),
    constraint contract_norm_list_aud_revinfo
        foreign key (rev) references revinfo (rev)
);

create table customer_aud
(
    rev int not null,
    revtype smallint null,
    id int not null,
    user_id int null,
    code varchar(255) null,
    name varchar(255) null,
    position varchar(255) null,
    iso2 varchar(100) null,
    country_code varchar(100) null,
    mobile varchar(255) null,
    email varchar(255) null,
    address varchar(255) null,
    id_card varchar(255) null,
    card_type int null,
    tax_code varchar(255) null,
    bank_account_number varchar(100) null,
    bank_account_holder varchar(100) null,
    bank_name varchar(100) null,
    bank_branch varchar(100) null,
    customer_type_id int null,
    active bit null,
    in_contract bit null,
    created_by_id int null,
    updated_by_id int null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null,
    primary key (rev, id),
    constraint customer_aud_revinfo
        foreign key (rev) references revinfo (rev)
);

create table customer_journey_diary_daily_aud
(
    rev int not null,
    revtype smallint null,
    id int not null,
    customer_payment_request_id int null,
    date datetime null,
    customer_name_used varchar(255) null,
    customer_department varchar(255) null,
    trip_itinerary varchar(10000) null,
    km_start decimal(11) null,
    km_end decimal(11) null,
    used_km decimal(11) null,
    used_km_self_drive decimal(11) null,
    working_time_gps_from time null,
    working_time_gps_to time null,
    over_time int null,
    over_km decimal(11) null,
    over_km_self_drive decimal(11) null,
    overnight int null,
    is_holiday bit null,
    is_weekend bit null,
    is_self_drive bit null,
    driver_name varchar(255) null,
    vehicle_number_plate varchar(255) null,
    parking_fee decimal(11) null,
    tolls_fee decimal(11) null,
    note varchar(1000) null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null,
    primary key (rev, id),
    constraint customer_journey_diary_daily_aud_revinfo
        foreign key (rev) references revinfo (rev)
);

create table customer_payment_request_aud
(
    rev int not null,
    revtype smallint null,
    id int not null,
    contract_id int null,
    contract_code varchar(255) null,
    contract_type_id int null,
    customer_name varchar(255) null,
    admin_name varchar(255) null,
    customer_address varchar(255) null,
    driver_name varchar(255) null,
    vehicle_number_plate varchar(255) null,
    from_date datetime null,
    to_date datetime null,
    created_by_id int null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null,
    primary key (rev, id),
    constraint customer_payment_request_aud_revinfo
        foreign key (rev) references revinfo (rev)
);

create table feedback_aud
(
    rev int not null,
    revtype smallint null,
    id int not null,
    content varchar(1000) null,
    created_by_id int null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null,
    primary key (rev, id),
    constraint feedback_aud_revinfo
        foreign key (rev) references revinfo (rev)
);

create table journey_dairy_daily_lock_aud
(
    rev int not null,
    revtype smallint null,
    id int not null,
    lock_time datetime null,
    updated_by int null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null,
    primary key (rev, id),
    constraint journey_dairy_daily_lock_aud_revinfo
        foreign key (rev) references revinfo (rev)
);

create table journey_diary_aud
(
    rev int not null,
    revtype smallint null,
    id int not null,
    contract_id int null,
    vehicle_id int null,
    driver_id int null,
    time_start datetime null,
    address_start varchar(255) null,
    image_odo_link_start varchar(255) null,
    km_odo_start decimal(11) null,
    km_driver_start decimal(11) null,
    customer_name_used varchar(255) null,
    customer_department varchar(255) null,
    time_customer_get_in datetime null,
    address_customer_get_in varchar(255) null,
    image_customer_get_in_link varchar(255) null,
    image_odo_link_customer_get_in varchar(255) null,
    km_odo_customer_get_in decimal(11) null,
    km_driver_customer_get_in decimal(11) null,
    time_customer_get_out datetime null,
    address_customer_get_out varchar(255) null,
    image_customer_get_out_link varchar(255) null,
    image_odo_link_customer_get_out varchar(255) null,
    km_odo_customer_get_out decimal(11) null,
    km_driver_customer_get_out decimal(11) null,
    time_breakdown datetime null,
    image_breakdown_link varchar(255) null,
    image_odo_breakdown_link varchar(255) null,
    km_odo_breakdown decimal(11) null,
    km_driver_breakdown decimal(11) null,
    time_end datetime null,
    address_end varchar(255) null,
    image_odo_link_end varchar(255) null,
    km_odo_end decimal(11) null,
    km_driver_end decimal(11) null,
    total_km_gps decimal(11) null,
    flag_unavailable_vehicle bit null,
    step int null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null,
    primary key (rev, id),
    constraint journey_diary_aud_revinfo
        foreign key (rev) references revinfo (rev)
);

create table journey_diary_cost_type_aud
(
    rev int not null,
    revtype smallint null,
    id int not null,
    journey_diary_id int not null,
    cost_type_id int null,
    value decimal(11) null,
    image_cost_link varchar(255) null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null,
    primary key (rev, id),
    constraint journey_diary_cost_type_aud_revinfo
        foreign key (rev) references revinfo (rev)
);

create table journey_diary_daily_aud
(
    rev int not null,
    revtype smallint null,
    id int not null,
    journey_diary_id int null,
    contract_id int null,
    driver_id int null,
    vehicle_id int null,
    parent_id int null,
    date datetime null,
    month datetime null,
    customer_name_used varchar(255) null,
    customer_department varchar(255) null,
    trip_itinerary varchar(1000) null,
    km_start decimal(11) null,
    km_customer_get_in decimal(11) null,
    km_customer_get_out decimal(11) null,
    km_end decimal(11) null,
    total_km decimal(11) null,
    empty_km decimal(11) null,
    used_km decimal(11) null,
    used_km_self_drive decimal(11) null,
    working_time_app_from time null,
    working_time_app_to time null,
    working_time_gps_from time null,
    working_time_gps_to time null,
    over_time int null,
    over_km decimal(11) null,
    over_km_self_drive decimal(11) null,
    overnight int null,
    is_holiday bit null,
    is_weekend bit null,
    is_over_day bit null,
    is_self_drive bit null,
    driver_name varchar(255) null,
    vehicle_number_plate varchar(255) null,
    image_cost_links varchar(1000) null,
    image_breakdown_link varchar(1000) null,
    image_odo_links varchar(1000) null,
    image_customer_get_in_link varchar(255) null,
    image_customer_get_out_link varchar(255) null,
    flag_odo_recognition_failed bit null,
    flag_multi_date bit null,
    flag_unavailable_vehicle bit null,
    flag_changed_vehicle bit null,
    flag_changed_km_norm bit null,
    flag_changed_working_day bit null,
    flag_created_manually bit null,
    flag_finished_modify bit null,
    note varchar(1000) null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null,
    primary key (rev, id),
    constraint journey_diary_daily_aud_revinfo
        foreign key (rev) references revinfo (rev)
);

create table journey_diary_daily_cost_type_aud
(
    rev int not null,
    revtype smallint null,
    id int not null,
    journey_diary_daily_id int not null,
    cost_type_id int not null,
    value decimal(11) null,
    image_cost_link varchar(1000) null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null,
    primary key (rev, id),
    constraint journey_diary_daily_cost_type_aud_revinfo
        foreign key (rev) references revinfo (rev)
);

create table log_contract_norm_list_aud
(
    rev int not null,
    revtype smallint null,
    id int not null,
    contract_id int null,
    norm_list_id int null,
    from_date datetime null,
    to_date datetime null,
    quota decimal(12,1) null,
    created_by_id int null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null,
    primary key (rev, id),
    constraint log_contract_norm_list_aud_revinfo
        foreign key (rev) references revinfo (rev)
);

create table log_contract_price_cost_type_aud
(
    rev int not null,
    revtype smallint null,
    id int not null,
    contract_id int null,
    cost_type_id int null,
    from_date datetime null,
    to_date datetime null,
    price decimal(11) null,
    created_by_id int null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null,
    primary key (rev, id),
    constraint log_contract_price_cost_type_aud_revinfo
        foreign key (rev) references revinfo (rev)
);

create table member_customer_aud
(
    rev int not null,
    revtype smallint null,
    id int not null,
    user_id int null,
    code varchar(100) null,
    name varchar(100) not null,
    department varchar(100) null,
    role varchar(100) not null,
    parent_id int null,
    email varchar(100) null,
    iso2 varchar(100) null,
    country_code varchar(100) null,
    mobile varchar(100) null,
    customer_id int not null,
    active bit null,
    in_contract bit null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null,
    primary key (rev, id),
    constraint member_customer_aud_revinfo
        foreign key (rev) references revinfo (rev)
);

create table payment_request_item_aud
(
    rev int not null,
    revtype smallint null,
    id int not null,
    customer_payment_request_id int null,
    name varchar(255) null,
    price decimal(11) null,
    count decimal(11,2) null,
    unit varchar(255) null,
    total_price decimal(13) null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null,
    primary key (rev, id),
    constraint payment_request_item_aud_revinfo
        foreign key (rev) references revinfo (rev)
);

create table time_use_policy
(
    id int auto_increment
        primary key,
    name varchar(255) not null,
    start_value int null,
    end_value int null,
    rate float null,
    group_id int not null,
    from_date datetime null,
    to_date datetime not null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null
);

create table token_firebase
(
    token varchar(255) null,
    user_id int null,
    id int auto_increment
        primary key
);

create table user_aud
(
    rev int not null,
    revtype smallint null,
    id int not null,
    customer_id int null,
    member_customer_id int null,
    code varchar(255) null,
    name varchar(255) null,
    username varchar(255) null,
    password varchar(255) null,
    birthdate datetime null,
    id_card varchar(255) null,
    card_type int null,
    iso2 varchar(100) null,
    country_code varchar(100) null,
    mobile varchar(255) null,
    mobile_full varchar(255) null,
    email varchar(255) null,
    address varchar(255) null,
    department_id int null,
    driver_licenses varchar(255) null,
    driver_license_number varchar(255) null,
    driver_license_expiry_date datetime null,
    know_english bit null,
    ddt_certificate bit null,
    user_role_id int null,
    user_type_id int null,
    active bit null,
    status tinyint null,
    user_group_id int null,
    branch_id int null,
    unit_operator_id int null,
    in_contract bit null,
    note varchar(1000) null,
    current_journey_diary_id int null,
    current_contract_id int null,
    lending_contract_id int null,
    login_times int null,
    login_failed_times int null,
    removable bit null,
    created_by int null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null,
    primary key (rev, id),
    constraint user_aud_revinfo
        foreign key (rev) references revinfo (rev)
);

create table user_group
(
    id int auto_increment
        primary key,
    code varchar(255) null,
    name varchar(255) null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null
);

create table user_role
(
    id int auto_increment
        primary key,
    code varchar(255) null,
    name varchar(255) null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null
);

create table user_type
(
    id int auto_increment
        primary key,
    code varchar(255) null,
    name varchar(255) null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null
);

create table vehicle_aud
(
    rev int not null,
    revtype smallint null,
    id int not null,
    number_plate varchar(255) null,
    type varchar(255) null,
    transmission_type int null,
    owner varchar(255) null,
    model varchar(255) null,
    color varchar(255) null,
    number_seat int null,
    chassis_no varchar(255) null,
    engine_no varchar(255) null,
    year_manufacture int null,
    start_using_date datetime null,
    registration_no varchar(255) null,
    travel_warrant_expiry_date datetime null,
    registration_to_date datetime null,
    insurance_no varchar(255) null,
    insurance_expiry_date datetime null,
    road_fee_expiry_date datetime null,
    liquidation_date datetime null,
    place_of_origin varchar(255) null,
    fuel_type_group_id int null,
    branch_id int null,
    vehicle_supplier_group_id int null,
    operation_admin_id int null,
    unit_operator_id int null,
    accountant_id int null,
    status tinyint null,
    in_contract bit null,
    note varchar(1000) null,
    current_journey_diary_id int null,
    current_contract_id int null,
    lending_contract_id int null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null,
    primary key (rev, id),
    constraint vehicle_aud_revinfo
        foreign key (rev) references revinfo (rev)
);

create table vehicle_supplier_group
(
    id int auto_increment
        primary key,
    code varchar(255) null,
    name varchar(255) null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null
);

create table working_day
(
    id int auto_increment
        primary key,
    code varchar(100) not null,
    name varchar(255) not null,
    is_deleted bit not null,
    created_at datetime null,
    updated_at datetime null
);

create table contract
(
    id int auto_increment
        primary key,
    prefix_code varchar(100) not null,
    suffix_code varchar(100) not null,
    code varchar(100) not null,
    contract_period_type_id int null,
    contract_type_id int null,
    term int null,
    branch_id int null,
    vehicle_working_area varchar(255) null,
    sign_date datetime null,
    customer_id int null,
    member_customer_id int null,
    driver_id int null,
    driver_is_transferred_another bit null,
    driver_is_transferred_another_at datetime null,
    driver_know_english bit null,
    vehicle_id int null,
    vehicle_is_transferred_another bit not null,
    vehicle_is_transferred_another_at datetime null,
    parking_id int null,
    from_datetime datetime null,
    to_datetime datetime null,
    rental_service_type_id int null,
    working_time_weekend_holiday_from time null,
    working_time_weekend_holiday_to time null,
    time_use_policy_group_id int null,
    deposit decimal(11) null,
    payment_term int null,
    vat_toll_fee int null,
    is_include_empty_km bit null,
    working_time_from time null,
    working_time_to time null,
    working_day_id int null,
    working_day int null,
    fuel_type_id int null,
    return_vehicle_early bit default b'0' null,
    days_inform_before_return_vehicle int null,
    days_inform_before_early_termination int null,
    fuel_adjust_percent decimal(5,2) null,
    penalty_rate_early_termination decimal(5,2) null,
    date_early_termination datetime null,
    status int not null,
    note varchar(1000) null,
    created_by_user_id int not null,
    include_appendix bit default b'0' null,
    canceled_at datetime null,
    is_deleted bit not null,
    created_at datetime null,
    updated_at datetime null,
    constraint contract_branch
        foreign key (branch_id) references branch (id)
            on update cascade on delete cascade,
    constraint contract_contract_period_type
        foreign key (contract_period_type_id) references contract_period_type (id),
    constraint contract_contract_type
        foreign key (contract_type_id) references contract_type (id),
    constraint contract_customer
        foreign key (customer_id) references customer (id),
    constraint contract_fuel_type
        foreign key (fuel_type_id) references fuel_type (id),
    constraint contract_member_customer
        foreign key (member_customer_id) references member_customer (id),
    constraint contract_rental_service_type
        foreign key (rental_service_type_id) references rental_service_type (id),
    constraint contract_working_day
        foreign key (working_day_id) references working_day (id)
);

create table contract_cost_type
(
    contract_id int not null,
    cost_type_id int not null,
    price decimal(11) null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null,
    primary key (contract_id, cost_type_id),
    constraint contract_contract_cost_type
        foreign key (contract_id) references contract (id),
    constraint cost_type_contract_cost_type
        foreign key (cost_type_id) references cost_type (id)
);

create table contract_norm_list
(
    contract_id int not null,
    norm_list_id int not null,
    quota decimal(12,1) null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null,
    primary key (contract_id, norm_list_id),
    constraint contract_norm_list_FK
        foreign key (contract_id) references contract (id),
    constraint contract_norm_list_FK_1
        foreign key (norm_list_id) references norm_list (id)
);

create table journey_diary
(
    id int auto_increment
        primary key,
    contract_id int null,
    vehicle_id int null,
    driver_id int null,
    time_start datetime null,
    address_start varchar(255) null,
    image_odo_link_start varchar(255) null,
    km_odo_start decimal(11) null,
    km_driver_start decimal(11) null,
    customer_name_used varchar(255) null,
    customer_department varchar(255) null,
    time_customer_get_in datetime null,
    address_customer_get_in varchar(255) null,
    image_customer_get_in_link varchar(255) null,
    image_odo_link_customer_get_in varchar(255) null,
    km_odo_customer_get_in decimal(11) null,
    km_driver_customer_get_in decimal(11) null,
    time_customer_get_out datetime null,
    address_customer_get_out varchar(255) null,
    image_customer_get_out_link varchar(255) null,
    image_odo_link_customer_get_out varchar(255) null,
    km_odo_customer_get_out decimal(11) null,
    km_driver_customer_get_out decimal(11) null,
    time_breakdown datetime null,
    image_breakdown_link varchar(255) null,
    image_odo_breakdown_link varchar(255) null,
    km_odo_breakdown decimal(11) null,
    km_driver_breakdown decimal(11) null,
    time_end datetime null,
    address_end varchar(255) null,
    image_odo_link_end varchar(255) null,
    km_odo_end decimal(11) null,
    km_driver_end decimal(11) null,
    total_km_gps decimal(11) null,
    flag_unavailable_vehicle bit null,
    step int null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null,
    constraint contract_journey_diary
        foreign key (contract_id) references contract (id)
);

create table journey_diary_cost_type
(
    id int auto_increment
        primary key,
    journey_diary_id int not null,
    cost_type_id int null,
    value decimal(11) null,
    image_cost_link varchar(255) null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null,
    constraint cost_type_journey_diary_cost_type
        foreign key (cost_type_id) references cost_type (id),
    constraint journey_diary_journey_diary_cost_type
        foreign key (journey_diary_id) references journey_diary (id)
);

create table user
(
    id int auto_increment
        primary key,
    customer_id int null,
    member_customer_id int null,
    code varchar(255) null,
    name varchar(255) null,
    username varchar(255) null,
    password varchar(255) null,
    birthdate datetime null,
    id_card varchar(255) null,
    card_type int null,
    iso2 varchar(100) null,
    country_code varchar(100) null,
    mobile varchar(255) null,
    mobile_full varchar(255) null,
    email varchar(255) null,
    address varchar(255) null,
    department_id int null,
    driver_licenses varchar(255) null,
    driver_license_number varchar(255) null,
    driver_license_expiry_date datetime null,
    know_english bit null,
    ddt_certificate bit null,
    user_role_id int null,
    user_type_id int null,
    active bit null,
    status tinyint null,
    user_group_id int null,
    branch_id int null,
    unit_operator_id int null,
    in_contract bit default b'0' null,
    note varchar(1000) null,
    current_journey_diary_id int null,
    current_contract_id int null,
    lending_contract_id int null,
    login_times int default 0 not null,
    login_failed_times int default 0 not null,
    removable bit default b'0' null,
    created_by int null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null,
    constraint user_branch
        foreign key (branch_id) references branch (id),
    constraint user_created_by
        foreign key (created_by) references user (id),
    constraint user_current_contract
        foreign key (current_contract_id) references contract (id),
    constraint user_current_journey_diary
        foreign key (current_journey_diary_id) references journey_diary (id),
    constraint user_customer
        foreign key (customer_id) references customer (id),
    constraint user_department
        foreign key (department_id) references department (id),
    constraint user_lending_contract
        foreign key (lending_contract_id) references contract (id),
    constraint user_member_customer
        foreign key (member_customer_id) references member_customer (id),
    constraint user_unit_operator
        foreign key (unit_operator_id) references user (id),
    constraint user_user_group
        foreign key (user_group_id) references user_group (id),
    constraint user_user_role
        foreign key (user_role_id) references user_role (id),
    constraint user_user_type
        foreign key (user_type_id) references user_type (id)
);

alter table contract
    add constraint contract_driver
        foreign key (driver_id) references user (id);

alter table contract
    add constraint contract_user_created_by
        foreign key (created_by_user_id) references user (id);

create table contract_change_history
(
    id int auto_increment
        primary key,
    contract_id int not null,
    mapping_field_code_fontend_id int not null,
    old_value varchar(255) null,
    new_value varchar(255) null,
    from_date datetime null,
    to_date datetime null,
    created_by_id int not null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null,
    constraint contract_change_history_FK
        foreign key (mapping_field_code_fontend_id) references mapping_field_code_fontend (id),
    constraint contract_change_history_FK_1
        foreign key (created_by_id) references user (id),
    constraint contract_contract_change_history
        foreign key (contract_id) references contract (id)
);

create table contract_driver_history
(
    id int auto_increment
        primary key,
    user_id int not null,
    contract_id int null,
    from_date datetime null,
    to_date datetime null,
    status int null,
    is_deleted int null,
    created_at datetime null,
    updated_at datetime null,
    constraint contract_contract_driver_history
        foreign key (contract_id) references contract (id),
    constraint user_contract_driver_history
        foreign key (user_id) references user (id)
);

alter table customer
    add constraint customer_FK_1
        foreign key (created_by_id) references user (id);

alter table customer
    add constraint customer_user
        foreign key (user_id) references user (id);

create table customer_payment_request
(
    id int auto_increment
        primary key,
    contract_id int null,
    contract_code varchar(255) null,
    contract_type_id int null,
    customer_name varchar(255) null,
    admin_name varchar(255) null,
    customer_address varchar(255) null,
    driver_name varchar(255) null,
    vehicle_number_plate varchar(255) null,
    from_date datetime null,
    to_date datetime null,
    created_by_id int null,
    is_deleted bit default b'0' null,
    created_at datetime null,
    updated_at datetime null,
    constraint customer_payment_request_contract
        foreign key (contract_id) references contract (id),
    constraint customer_payment_request_created_by
        foreign key (created_by_id) references user (id)
);

create table customer_journey_diary_daily
(
    id int auto_increment
        primary key,
    customer_payment_request_id int null,
    date datetime null,
    customer_name_used varchar(255) null,
    customer_department varchar(255) null,
    trip_itinerary varchar(10000) null,
    km_start decimal(11) null,
    km_end decimal(11) null,
    used_km decimal(11) null,
    used_km_self_drive decimal(11) null,
    working_time_gps_from time null,
    working_time_gps_to time null,
    over_time int null,
    over_km decimal(11) null,
    over_km_self_drive decimal(11) null,
    overnight int null,
    is_holiday bit default b'0' not null,
    is_weekend bit default b'0' not null,
    is_self_drive bit default b'0' not null,
    driver_name varchar(255) null,
    vehicle_number_plate varchar(255) null,
    parking_fee decimal(11) null,
    tolls_fee decimal(11) null,
    note varchar(1000) null,
    is_deleted bit default b'0' null,
    created_at datetime null,
    updated_at datetime null,
    constraint customer_journey_diary_daily_customer_payment_request
        foreign key (customer_payment_request_id) references customer_payment_request (id)
);

create table feedback
(
    id int auto_increment
        primary key,
    content varchar(1000) null,
    created_by_id int null,
    is_deleted bit default b'0' null,
    created_at datetime null,
    updated_at datetime null,
    constraint feedback_created_by
        foreign key (created_by_id) references user (id)
);

create table journey_dairy_daily_lock
(
    id int auto_increment
        primary key,
    lock_time datetime null,
    updated_by int null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null,
    constraint journey_dairy_daily_lock_user
        foreign key (updated_by) references user (id)
);

alter table journey_diary
    add constraint driver_journey_diary
        foreign key (driver_id) references user (id);

create table log_contract_norm_list
(
    id int auto_increment
        primary key,
    contract_id int null,
    norm_list_id int null,
    from_date datetime null,
    to_date datetime null,
    quota decimal(12,1) null,
    created_by_id int null,
    is_deleted bit not null,
    created_at datetime null,
    updated_at datetime null,
    constraint contract_log_contract_norm_list
        foreign key (contract_id) references contract (id),
    constraint norm_list_log_contract_norm_list
        foreign key (norm_list_id) references norm_list (id),
    constraint user_log_contract_norm_list
        foreign key (created_by_id) references user (id)
);

create index from_date_log_contract_norm_list
    on log_contract_norm_list (from_date);

create table log_contract_price_cost_type
(
    id int auto_increment
        primary key,
    contract_id int null,
    cost_type_id int null,
    from_date datetime null,
    to_date datetime null,
    price decimal(11) null,
    created_by_id int null,
    is_deleted bit not null,
    created_at datetime null,
    updated_at datetime null,
    constraint contract_log_contract_price_cost
        foreign key (contract_id) references contract (id),
    constraint cost_type_log_contract_price_cost
        foreign key (cost_type_id) references cost_type (id),
    constraint user_log_contract_price_cost_type
        foreign key (created_by_id) references user (id)
);

create index from_date_log_contract_price_cost_type
    on log_contract_price_cost_type (from_date);

alter table member_customer
    add constraint member_customer_user
        foreign key (user_id) references user (id);

create table notification
(
    id int auto_increment
        primary key,
    user_id int null,
    notification_content_id int null,
    status varchar(255) null,
    type varchar(255) null,
    spec_id int null,
    params varchar(255) null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null,
    constraint notification_content_notification
        foreign key (notification_content_id) references notification_content (id),
    constraint user_notification
        foreign key (user_id) references user (id)
);

create table payment_request_item
(
    id int auto_increment
        primary key,
    customer_payment_request_id int null,
    name varchar(255) null,
    price decimal(11) null,
    count decimal(11,2) null,
    unit varchar(255) null,
    total_price decimal(13) null,
    is_deleted bit default b'0' null,
    created_at datetime null,
    updated_at datetime null,
    constraint payment_request_item_customer_payment_request
        foreign key (customer_payment_request_id) references customer_payment_request (id)
);

create table vehicle
(
    id int auto_increment
        primary key,
    number_plate varchar(255) null,
    type varchar(255) null,
    transmission_type int null,
    owner varchar(255) null,
    model varchar(255) null,
    color varchar(255) null,
    number_seat int null,
    chassis_no varchar(255) null,
    engine_no varchar(255) null,
    year_manufacture int null,
    start_using_date datetime null,
    registration_no varchar(255) null,
    travel_warrant_expiry_date datetime null,
    registration_to_date datetime null,
    insurance_no varchar(255) null,
    insurance_expiry_date datetime null,
    road_fee_expiry_date datetime null,
    liquidation_date datetime null,
    place_of_origin varchar(255) null,
    fuel_type_group_id int null,
    branch_id int null,
    vehicle_supplier_group_id int null,
    operation_admin_id int null,
    unit_operator_id int null,
    accountant_id int null,
    status tinyint null,
    in_contract bit default b'0' not null,
    note varchar(1000) null,
    current_journey_diary_id int null,
    current_contract_id int null,
    lending_contract_id int null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null,
    constraint vehicle_accountant
        foreign key (accountant_id) references user (id),
    constraint vehicle_branch
        foreign key (branch_id) references branch (id),
    constraint vehicle_current_contract
        foreign key (current_contract_id) references contract (id),
    constraint vehicle_current_journey_diary
        foreign key (current_journey_diary_id) references journey_diary (id),
    constraint vehicle_fuel_type_group
        foreign key (fuel_type_group_id) references fuel_type_group (id),
    constraint vehicle_lending_contract
        foreign key (lending_contract_id) references contract (id),
    constraint vehicle_operation_admin
        foreign key (operation_admin_id) references user (id),
    constraint vehicle_unit_operator
        foreign key (unit_operator_id) references user (id),
    constraint vehicle_vehicle_supplier_group
        foreign key (vehicle_supplier_group_id) references vehicle_supplier_group (id)
);

alter table contract
    add constraint contract_vehicle
        foreign key (vehicle_id) references vehicle (id);

alter table journey_diary
    add constraint vehicle_journey_diary
        foreign key (vehicle_id) references vehicle (id);

create table journey_diary_daily
(
    id int auto_increment
        primary key,
    journey_diary_id int null,
    contract_id int null,
    driver_id int null,
    vehicle_id int null,
    parent_id int null,
    date datetime null,
    month datetime null,
    customer_name_used varchar(255) null,
    customer_department varchar(255) null,
    trip_itinerary varchar(1000) null,
    km_start decimal(11) null,
    km_customer_get_in decimal(11) null,
    km_customer_get_out decimal(11) null,
    km_end decimal(11) null,
    total_km decimal(11) null,
    empty_km decimal(11) null,
    used_km decimal(11) null,
    used_km_self_drive decimal(11) null,
    working_time_app_from time null,
    working_time_app_to time null,
    working_time_gps_from time null,
    working_time_gps_to time null,
    over_time int null,
    over_km decimal(11) null,
    over_km_self_drive decimal(11) null,
    overnight int null,
    is_holiday bit not null,
    is_weekend bit not null,
    is_over_day bit not null,
    is_self_drive bit not null,
    driver_name varchar(255) null,
    vehicle_number_plate varchar(255) null,
    image_cost_links varchar(1000) null,
    image_breakdown_link varchar(1000) null,
    image_odo_links varchar(1000) null,
    image_customer_get_in_link varchar(255) null,
    image_customer_get_out_link varchar(255) null,
    flag_odo_recognition_failed bit null,
    flag_multi_date bit null,
    flag_unavailable_vehicle bit null,
    flag_changed_vehicle bit null,
    flag_changed_km_norm bit null,
    flag_changed_working_day bit null,
    flag_created_manually bit null,
    flag_finished_modify bit null,
    note varchar(1000) null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null,
    constraint journey_diary_daily_contract
        foreign key (contract_id) references contract (id),
    constraint journey_diary_daily_driver
        foreign key (driver_id) references user (id),
    constraint journey_diary_daily_journey_diary
        foreign key (journey_diary_id) references journey_diary (id),
    constraint journey_diary_daily_parent
        foreign key (parent_id) references journey_diary_daily (id),
    constraint journey_diary_daily_vehicle
        foreign key (vehicle_id) references vehicle (id)
);

create table journey_diary_daily_cost_type
(
    id int auto_increment
        primary key,
    journey_diary_daily_id int not null,
    cost_type_id int not null,
    value decimal(11) null,
    image_cost_link varchar(1000) null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null,
    constraint journey_diary_daily_cost_type_cost_type
        foreign key (cost_type_id) references cost_type (id),
    constraint journey_diary_daily_cost_type_journey_diary_daily
        foreign key (journey_diary_daily_id) references journey_diary_daily (id)
);

create table working_calendar
(
    id int auto_increment
        primary key,
    working_day_id int not null,
    date date null,
    is_holiday bit default b'0' not null,
    is_weekend bit default b'0' not null,
    is_deleted bit null,
    created_at datetime null,
    updated_at datetime null,
    constraint working_calendar_working_day
        foreign key (working_day_id) references working_day (id)
);

