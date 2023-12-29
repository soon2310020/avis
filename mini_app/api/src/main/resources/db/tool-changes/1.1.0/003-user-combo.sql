-- SEQUENCE: user_combo_id_seq
-- DROP SEQUENCE IF EXISTS user_combo_id_seq;
CREATE SEQUENCE IF NOT EXISTS user_combo_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

-- Table: user_combo
CREATE TABLE IF NOT EXISTS user_combo
(
    id bigint NOT NULL DEFAULT nextval('user_combo_id_seq'::regclass),
    created_by character varying(255) COLLATE pg_catalog."default",
    created_date timestamp without time zone,
    last_modified_by character varying(255) COLLATE pg_catalog."default",
    last_modified_date timestamp without time zone,
    attributes character varying(1024) COLLATE pg_catalog."default" NOT NULL,
    combo_code character varying(255) COLLATE pg_catalog."default",
    combo_name character varying(255) COLLATE pg_catalog."default",
    dob date,
    gender character varying(255) COLLATE pg_catalog."default",
    input_amount numeric(19,2),
    "raw" boolean NOT NULL,
    username character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT user_combo_pkey PRIMARY KEY (id)
    )

    TABLESPACE pg_default;