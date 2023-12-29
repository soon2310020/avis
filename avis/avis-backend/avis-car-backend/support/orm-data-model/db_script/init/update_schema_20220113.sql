create table system_log
(
    id int auto_increment
        primary key,
    user varchar(255)  null,
    user_id int null,
    ip varchar(255)  null,
    device varchar(255)  null,
    `level` varchar(255)  null,
    `type` varchar(255)  null,
    call_time datetime null,
    `function` varchar(255)  null,
    logs LONGTEXT null,

    is_deleted bit not null,
    created_at datetime null,
    updated_at datetime null
);