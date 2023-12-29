-- SEQUENCE: combo_suggestion_id_seq
-- DROP TABLE IF EXISTS combo_suggestion;
-- DROP SEQUENCE IF EXISTS combo_suggestion_id_seq;
CREATE SEQUENCE IF NOT EXISTS combo_suggestion_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

-- ALTER SEQUENCE combo_suggestion_id_seq OWNER TO "bancas";

-- Table: combo_suggestion
CREATE TABLE IF NOT EXISTS combo_suggestion
(
    id bigint NOT NULL DEFAULT nextval('combo_suggestion_id_seq'::regclass),
    created_by character varying(255) COLLATE pg_catalog."default",
    created_date timestamp without time zone,
    last_modified_by character varying(255) COLLATE pg_catalog."default",
    last_modified_date timestamp without time zone,
    attributes character varying(512) COLLATE pg_catalog."default" NOT NULL,
    combo_code character varying(255) COLLATE pg_catalog."default" NOT NULL,
    fee_package character varying(255) COLLATE pg_catalog."default" NOT NULL,
    fee_rank integer NOT NULL,
    insurance_term character varying(255) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT combo_suggestion_pkey PRIMARY KEY (id)
    )

    TABLESPACE pg_default;

-- ALTER TABLE IF EXISTS combo_suggestion OWNER to "bancas";

DELETE FROM combo_suggestion;
    
INSERT INTO combo_suggestion(id, attributes, combo_code, fee_package, fee_rank, insurance_term) VALUES (nextval('combo_suggestion_id_seq'),'{"X0":5,"X1":145000000,"X2":30,"X3":100000000,"X4":100000000,"X5":1750000,"X6":10000000,"X7":35000000,"X8":10000000,"X9":0}','SIGNATURE','COPPER',1,'MAIN');
INSERT INTO combo_suggestion(id, attributes, combo_code, fee_package, fee_rank, insurance_term) VALUES (nextval('combo_suggestion_id_seq'),'{"X0":5,"X1":145000000,"X2":30,"X3":100000000,"X4":100000000,"X5":1750000,"X6":10000000,"X7":35000000,"X8":10000000,"X9":1000000}','SIGNATURE','COPPER',2,'MAIN,BS_01');
INSERT INTO combo_suggestion(id, attributes, combo_code, fee_package, fee_rank, insurance_term) VALUES (nextval('combo_suggestion_id_seq'),'{"X0":5,"X1":270000000,"X2":30,"X3":200000000,"X4":200000000,"X5":2500000,"X6":20000000,"X7":50000000,"X8":20000000,"X9":1400000}','SIGNATURE','SILVER',3,'MAIN,BS_01');
INSERT INTO combo_suggestion(id, attributes, combo_code, fee_package, fee_rank, insurance_term) VALUES (nextval('combo_suggestion_id_seq'),'{"X0":5,"X1":430000000,"X2":30,"X3":300000000,"X4":300000000,"X5":5000000,"X6":30000000,"X7":100000000,"X8":30000000,"X9":2000000}','SIGNATURE','GOLD',4,'MAIN,BS_01');
INSERT INTO combo_suggestion(id, attributes, combo_code, fee_package, fee_rank, insurance_term) VALUES (nextval('combo_suggestion_id_seq'),'{"X0":5,"X1":700000000,"X2":30,"X3":500000000,"X4":500000000,"X5":7500000,"X6":50000000,"X7":150000000,"X8":50000000,"X9":3000000}','SIGNATURE','PLATINUM',5,'MAIN,BS_01');
INSERT INTO combo_suggestion(id, attributes, combo_code, fee_package, fee_rank, insurance_term) VALUES (nextval('combo_suggestion_id_seq'),'{"X0":5,"X1":1300000000,"X2":30,"X3":1000000000,"X4":1000000000,"X5":10000000,"X6":50000000,"X7":200000000,"X8":50000000,"X9":4000000}','SIGNATURE','DIAMOND',6,'MAIN,BS_01');

INSERT INTO combo_suggestion(id, attributes, combo_code, fee_package, fee_rank, insurance_term) VALUES (nextval('combo_suggestion_id_seq'),'{"X0":5,"X1":145000000,"X2":20,"X3":100000000,"X4":100000000,"X5":1750000,"X6":10000000,"X7":35000000,"X8":10000000,"X9":0}','SPECIAL_OFFER','COPPER',1,'MAIN');
INSERT INTO combo_suggestion(id, attributes, combo_code, fee_package, fee_rank, insurance_term) VALUES (nextval('combo_suggestion_id_seq'),'{"X0":5,"X1":145000000,"X2":20,"X3":100000000,"X4":100000000,"X5":1750000,"X6":10000000,"X7":35000000,"X8":10000000,"X9":0}','SPECIAL_OFFER','COPPER',2,'MAIN');
INSERT INTO combo_suggestion(id, attributes, combo_code, fee_package, fee_rank, insurance_term) VALUES (nextval('combo_suggestion_id_seq'),'{"X0":5,"X1":270000000,"X2":20,"X3":200000000,"X4":200000000,"X5":2500000,"X6":20000000,"X7":50000000,"X8":20000000,"X9":0}','SPECIAL_OFFER','SILVER',3,'MAIN');
INSERT INTO combo_suggestion(id, attributes, combo_code, fee_package, fee_rank, insurance_term) VALUES (nextval('combo_suggestion_id_seq'),'{"X0":5,"X1":430000000,"X2":20,"X3":300000000,"X4":300000000,"X5":5000000,"X6":30000000,"X7":100000000,"X8":30000000,"X9":0}','SPECIAL_OFFER','GOLD',4,'MAIN');
INSERT INTO combo_suggestion(id, attributes, combo_code, fee_package, fee_rank, insurance_term) VALUES (nextval('combo_suggestion_id_seq'),'{"X0":5,"X1":700000000,"X2":20,"X3":500000000,"X4":500000000,"X5":7500000,"X6":50000000,"X7":150000000,"X8":50000000,"X9":0}','SPECIAL_OFFER','PLATINUM',5,'MAIN');
INSERT INTO combo_suggestion(id, attributes, combo_code, fee_package, fee_rank, insurance_term) VALUES (nextval('combo_suggestion_id_seq'),'{"X0":5,"X1":1300000000,"X2":20,"X3":1000000000,"X4":1000000000,"X5":10000000,"X6":50000000,"X7":200000000,"X8":50000000,"X9":0}','SPECIAL_OFFER','DIAMOND',6,'MAIN');

INSERT INTO combo_suggestion(id, attributes, combo_code, fee_package, fee_rank, insurance_term) VALUES (nextval('combo_suggestion_id_seq'),'{"X0":5,"X1":145000000,"X2":10,"X3":100000000,"X4":100000000,"X5":1750000,"X6":10000000,"X7":35000000,"X8":10000000,"X9":0}','MUST_TRY','COPPER',1,'MAIN');
INSERT INTO combo_suggestion(id, attributes, combo_code, fee_package, fee_rank, insurance_term) VALUES (nextval('combo_suggestion_id_seq'),'{"X0":5,"X1":145000000,"X2":10,"X3":100000000,"X4":100000000,"X5":1750000,"X6":10000000,"X7":35000000,"X8":10000000,"X9":0}','MUST_TRY','COPPER',2,'MAIN');
INSERT INTO combo_suggestion(id, attributes, combo_code, fee_package, fee_rank, insurance_term) VALUES (nextval('combo_suggestion_id_seq'),'{"X0":5,"X1":270000000,"X2":10,"X3":200000000,"X4":200000000,"X5":2500000,"X6":20000000,"X7":50000000,"X8":20000000,"X9":0}','MUST_TRY','SILVER',3,'MAIN');
INSERT INTO combo_suggestion(id, attributes, combo_code, fee_package, fee_rank, insurance_term) VALUES (nextval('combo_suggestion_id_seq'),'{"X0":5,"X1":430000000,"X2":10,"X3":300000000,"X4":300000000,"X5":5000000,"X6":30000000,"X7":100000000,"X8":30000000,"X9":0}','MUST_TRY','GOLD',4,'MAIN');
INSERT INTO combo_suggestion(id, attributes, combo_code, fee_package, fee_rank, insurance_term) VALUES (nextval('combo_suggestion_id_seq'),'{"X0":5,"X1":700000000,"X2":10,"X3":500000000,"X4":500000000,"X5":7500000,"X6":50000000,"X7":150000000,"X8":50000000,"X9":0}','MUST_TRY','PLATINUM',5,'MAIN');
INSERT INTO combo_suggestion(id, attributes, combo_code, fee_package, fee_rank, insurance_term) VALUES (nextval('combo_suggestion_id_seq'),'{"X0":5,"X1":1300000000,"X2":10,"X3":1000000000,"X4":1000000000,"X5":10000000,"X6":50000000,"X7":200000000,"X8":50000000,"X9":0}','MUST_TRY','DIAMOND',6,'MAIN');
