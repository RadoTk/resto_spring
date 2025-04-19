-- Création des types ENUM
CREATE TYPE unit AS ENUM ('G', 'U', 'L');
CREATE TYPE stock_movement_type AS ENUM ('IN', 'OUT');

-- Table: dish
CREATE TABLE dish (
    id bigint NOT NULL,
    name character varying,
    price numeric,
    CONSTRAINT dish_pkey PRIMARY KEY (id)
);

-- Séquence pour dish
CREATE SEQUENCE dish_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE dish ALTER COLUMN id SET DEFAULT nextval('dish_id_seq'::regclass);

-- Table: ingredient
CREATE TABLE ingredient (
    id bigint NOT NULL,
    name character varying,
    CONSTRAINT ingredient_pkey PRIMARY KEY (id)
);

-- Séquence pour ingredient
CREATE SEQUENCE ingredient_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE ingredient ALTER COLUMN id SET DEFAULT nextval('ingredient_id_seq'::regclass);

-- Table: dish_ingredient
CREATE TABLE dish_ingredient (
    id bigint NOT NULL,
    id_dish bigint,
    id_ingredient bigint,
    required_quantity numeric,
    unit unit,
    CONSTRAINT dish_ingredient_pkey PRIMARY KEY (id),
    CONSTRAINT unique_dish_ingredient_quantity UNIQUE (id_dish, id_ingredient, unit)
);

-- Séquence pour dish_ingredient
CREATE SEQUENCE dish_ingredient_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE dish_ingredient ALTER COLUMN id SET DEFAULT nextval('dish_ingredient_id_seq'::regclass);

-- Table: order
CREATE TABLE "order" (
    id integer NOT NULL,
    reference character varying(50) NOT NULL,
    creation_datetime timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT order_pkey PRIMARY KEY (id),
    CONSTRAINT order_reference_key UNIQUE (reference)
);

-- Séquence pour order
CREATE SEQUENCE order_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE "order" ALTER COLUMN id SET DEFAULT nextval('order_id_seq'::regclass);

-- Table: order_dish
CREATE TABLE order_dish (
    id integer NOT NULL,
    order_id integer NOT NULL,
    dish_id integer NOT NULL,
    quantity integer NOT NULL,
    CONSTRAINT order_dish_pkey PRIMARY KEY (id)
);

-- Séquence pour order_dish
CREATE SEQUENCE order_dish_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE order_dish ALTER COLUMN id SET DEFAULT nextval('order_dish_id_seq'::regclass);

-- Table: order_status
CREATE TABLE order_status (
    id integer NOT NULL,
    order_id integer NOT NULL,
    status character varying(20) NOT NULL,
    status_datetime timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT order_status_pkey PRIMARY KEY (id),
    CONSTRAINT valid_order_status CHECK (status::text = ANY (ARRAY['CREE'::character varying, 'CONFIRME'::character varying, 'EN_PREPARATION'::character varying, 'TERMINE'::character varying, 'SERVI'::character varying]::text[]))
);

-- Séquence pour order_status
CREATE SEQUENCE order_status_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE order_status ALTER COLUMN id SET DEFAULT nextval('order_status_id_seq'::regclass);

-- Table: order_dish_status
CREATE TABLE order_dish_status (
    id integer NOT NULL,
    order_dish_id integer NOT NULL,
    status character varying(20) NOT NULL,
    status_datetime timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT order_dish_status_pkey PRIMARY KEY (id),
    CONSTRAINT valid_dish_status CHECK (status::text = ANY (ARRAY['CREE'::character varying, 'CONFIRME'::character varying, 'EN_PREPARATION'::character varying, 'TERMINE'::character varying, 'SERVI'::character varying]::text[]))
);

-- Séquence pour order_dish_status
CREATE SEQUENCE order_dish_status_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE order_dish_status ALTER COLUMN id SET DEFAULT nextval('order_dish_status_id_seq'::regclass);

-- Table: dish_order_status_history
CREATE TABLE dish_order_status_history (
    id integer NOT NULL,
    dish_order_id bigint NOT NULL,
    status character varying(20) NOT NULL,
    status_date_time timestamp without time zone NOT NULL,
    CONSTRAINT dish_order_status_history_pkey PRIMARY KEY (id)
);

-- Séquence pour dish_order_status_history
CREATE SEQUENCE dish_order_status_history_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE dish_order_status_history ALTER COLUMN id SET DEFAULT nextval('dish_order_status_history_id_seq'::regclass);

-- Table: price
CREATE TABLE price (
    id bigint NOT NULL,
    amount numeric,
    date_value date,
    id_ingredient bigint,
    CONSTRAINT price_pkey PRIMARY KEY (id)
);

-- Séquence pour price
CREATE SEQUENCE price_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE price ALTER COLUMN id SET DEFAULT nextval('price_id_seq'::regclass);

-- Table: stock_movement
CREATE TABLE stock_movement (
    id bigint NOT NULL,
    quantity numeric,
    unit unit,
    movement_type stock_movement_type,
    creation_datetime timestamp without time zone,
    id_ingredient bigint,
    CONSTRAINT stock_movement_pkey PRIMARY KEY (id)
);

-- Séquence pour stock_movement
CREATE SEQUENCE stock_movement_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE stock_movement ALTER COLUMN id SET DEFAULT nextval('stock_movement_id_seq'::regclass);

-- Contraintes de clés étrangères
ALTER TABLE dish_ingredient ADD CONSTRAINT fk_dish_to_dish_ingredient FOREIGN KEY (id_dish) REFERENCES dish(id);
ALTER TABLE dish_ingredient ADD CONSTRAINT fk_ingredient_to_dish_ingredient FOREIGN KEY (id_ingredient) REFERENCES ingredient(id);

ALTER TABLE order_dish ADD CONSTRAINT order_dish_dish_id_fkey FOREIGN KEY (dish_id) REFERENCES dish(id) ON DELETE CASCADE;
ALTER TABLE order_dish ADD CONSTRAINT order_dish_order_id_fkey FOREIGN KEY (order_id) REFERENCES "order"(id) ON DELETE CASCADE;

ALTER TABLE dish_order_status_history ADD CONSTRAINT dish_order_status_history_dish_order_id_fkey FOREIGN KEY (dish_order_id) REFERENCES order_dish(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE order_dish_status ADD CONSTRAINT order_dish_status_order_dish_id_fkey FOREIGN KEY (order_dish_id) REFERENCES order_dish(id) ON DELETE CASCADE;

ALTER TABLE order_status ADD CONSTRAINT order_status_order_id_fkey FOREIGN KEY (order_id) REFERENCES "order"(id) ON DELETE CASCADE;

ALTER TABLE price ADD CONSTRAINT fk_price_id_ingredient FOREIGN KEY (id_ingredient) REFERENCES ingredient(id);

ALTER TABLE stock_movement ADD CONSTRAINT fk_stock_movement_id_ingredient FOREIGN KEY (id_ingredient) REFERENCES ingredient(id);