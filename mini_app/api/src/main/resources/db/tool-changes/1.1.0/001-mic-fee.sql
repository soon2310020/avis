-- SEQUENCE: mic_fee_id_seq
-- DROP TABLE IF EXISTS mic_fee;
-- DROP SEQUENCE IF EXISTS mic_fee_id_seq;
CREATE SEQUENCE IF NOT EXISTS mic_fee_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

-- ALTER SEQUENCE mic_fee_id_seq OWNER TO "bancas";

-- Table: mic_fee

CREATE TABLE IF NOT EXISTS mic_fee
(
    id bigint NOT NULL DEFAULT nextval('mic_fee_id_seq'::regclass),
    created_by character varying(255) COLLATE pg_catalog."default",
    created_date timestamp without time zone,
    last_modified_by character varying(255) COLLATE pg_catalog."default",
    last_modified_date timestamp without time zone,
    age_range integer NOT NULL,
    fee numeric(19,0) NOT NULL,
    fee_package character varying(255) COLLATE pg_catalog."default" NOT NULL,
    fee_type character varying(255) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT mic_fee_pkey PRIMARY KEY (id)
    )

    TABLESPACE pg_default;

-- ALTER TABLE IF EXISTS mic_fee OWNER to "bancas";

delete from mic_fee;
    
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),1,1500000,'COPPER','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),2,1200000,'COPPER','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),3,700000,'COPPER','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),4,700000,'COPPER','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),5,700000,'COPPER','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),6,600000,'COPPER','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),7,700000,'COPPER','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),8,700000,'COPPER','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),9,700000,'COPPER','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),10,800000,'COPPER','MAIN');

insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),1,2300000,'SILVER','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),2,1800000,'SILVER','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),3,1100000,'SILVER','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),4,1000000,'SILVER','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),5,1000000,'SILVER','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),6,900000,'SILVER','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),7,1000000,'SILVER','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),8,1100000,'SILVER','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),9,1100000,'SILVER','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),10,1200000,'SILVER','MAIN');

insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),1,4400000,'GOLD','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),2,3500000,'GOLD','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),3,2100000,'GOLD','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),4,1900000,'GOLD','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),5,1900000,'GOLD','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),6,1800000,'GOLD','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),7,1900000,'GOLD','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),8,2000000,'GOLD','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),9,2100000,'GOLD','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),10,2200000,'GOLD','MAIN');

insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),1,6700000,'PLATINUM','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),2,5300000,'PLATINUM','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),3,3200000,'PLATINUM','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),4,3000000,'PLATINUM','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),5,2800000,'PLATINUM','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),6,2700000,'PLATINUM','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),7,3000000,'PLATINUM','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),8,3100000,'PLATINUM','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),9,3200000,'PLATINUM','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),10,3400000,'PLATINUM','MAIN');

insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),1,9400000,'DIAMOND','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),2,7400000,'DIAMOND','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),3,4500000,'DIAMOND','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),4,4200000,'DIAMOND','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),5,4100000,'DIAMOND','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),6,3900000,'DIAMOND','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),7,4200000,'DIAMOND','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),8,4400000,'DIAMOND','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),9,4500000,'DIAMOND','MAIN');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),10,4800000,'DIAMOND','MAIN');

insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),1,2400000,'COPPER','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),2,1800000,'COPPER','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),3,1000000,'COPPER','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),4,900000,'COPPER','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),5,900000,'COPPER','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),6,900000,'COPPER','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),7,900000,'COPPER','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),8,1000000,'COPPER','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),9,1000000,'COPPER','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),10,1100000,'COPPER','BS_01');

insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),1,3400000,'SILVER','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),2,2600000,'SILVER','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),3,1500000,'SILVER','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),4,1300000,'SILVER','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),5,1300000,'SILVER','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),6,1200000,'SILVER','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),7,1300000,'SILVER','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),8,1400000,'SILVER','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),9,1400000,'SILVER','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),10,1600000,'SILVER','BS_01');

insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),1,4800000,'GOLD','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),2,3700000,'GOLD','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),3,2100000,'GOLD','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),4,1900000,'GOLD','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),5,1800000,'GOLD','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),6,1700000,'GOLD','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),7,1900000,'GOLD','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),8,2000000,'GOLD','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),9,2100000,'GOLD','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),10,2200000,'GOLD','BS_01');

insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),1,7300000,'PLATINUM','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),2,5600000,'PLATINUM','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),3,3100000,'PLATINUM','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),4,2800000,'PLATINUM','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),5,2700000,'PLATINUM','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),6,2600000,'PLATINUM','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),7,2800000,'PLATINUM','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),8,3000000,'PLATINUM','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),9,3100000,'PLATINUM','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),10,3300000,'PLATINUM','BS_01');

insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),1,9700000,'DIAMOND','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),2,7500000,'DIAMOND','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),3,4100000,'DIAMOND','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),4,3800000,'DIAMOND','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),5,3600000,'DIAMOND','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),6,3400000,'DIAMOND','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),7,3800000,'DIAMOND','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),8,3900000,'DIAMOND','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),9,4100000,'DIAMOND','BS_01');
insert into mic_fee (id, age_range, fee, fee_package, fee_type) values (nextval('mic_fee_id_seq'),10,4500000,'DIAMOND','BS_01');