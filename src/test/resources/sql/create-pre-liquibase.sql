CREATE TABLE gl_accounts
(
    id bigint NOT NULL,
    archive boolean,
    created_by character varying(255),
    created_on timestamp without time zone,
    hidden boolean,
    updated_by character varying(255),
    updated_on timestamp without time zone,
    version bigint,
    company_id bigint NOT NULL,
    depth integer NOT NULL,
    "inner" boolean NOT NULL,
    name character varying(255),
    "number" character varying(255),
    parent character varying(255),
    type character varying(255),
    translation jsonb,
    export_id character varying(255),
    labels jsonb,
    CONSTRAINT gl_accounts_pkey PRIMARY KEY (id)
);

CREATE TABLE resp_centers
(
    id bigint NOT NULL,
    archive boolean,
    created_by character varying(255),
    created_on timestamp without time zone,
    hidden boolean,
    updated_by character varying(255),
    updated_on timestamp without time zone,
    version bigint NOT NULL,
    company_id bigint NOT NULL,
    export_id character varying(255),
    labels jsonb,
    depth integer NOT NULL,
    description character varying(255),
    name character varying(255),
    parent bigint,
    CONSTRAINT resp_centers_pkey PRIMARY KEY (id)
);

CREATE TABLE documents
(
    id bigint NOT NULL,
    archive boolean,
    created_by character varying(255),
    created_on timestamp without time zone,
    hidden boolean,
    updated_by character varying(255),
    updated_on timestamp without time zone,
    version bigint NOT NULL,
    company_id bigint NOT NULL,
    export_id character varying(255),
    labels jsonb,
    date date,
    note character varying(255),
    "number" character varying(255),
    ordinal bigint,
    series character varying(255),
    uuid uuid,
    counterparty jsonb,
    employee jsonb,
    exchange jsonb,
    finished boolean,
    finished_gl boolean,
    recallable boolean,
    CONSTRAINT documents_pkey PRIMARY KEY (id)
);

CREATE TABLE gl_opening_balances
(
    id bigint NOT NULL,
    CONSTRAINT gl_opening_balances_pkey PRIMARY KEY (id),
    CONSTRAINT fk_gl_opening_balances_documents_123 FOREIGN KEY (id)
        REFERENCES documents (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE gl_ob_operations
(
    id bigint NOT NULL,
    archive boolean,
    created_by character varying(255),
    created_on timestamp without time zone,
    hidden boolean,
    updated_by character varying(255),
    updated_on timestamp without time zone,
    version bigint NOT NULL,
    company_id bigint NOT NULL,
    export_id character varying(255),
    labels jsonb,
    account jsonb,
    credit jsonb,
    debit jsonb,
    rc jsonb,
    sort_nr double precision,
    parent_id bigint,
    CONSTRAINT gl_ob_operations_pkey PRIMARY KEY (id),
    CONSTRAINT fk_gl_ob_operations_documents_123 FOREIGN KEY (parent_id)
        REFERENCES gl_opening_balances (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

CREATE TABLE double_entries
(
    content character varying(255),
    frozen boolean,
    parent_counterparty jsonb,
    parent_db integer,
    parent_id bigint,
    parent_number character varying(255),
    parent_type character varying(255),
    total jsonb,
    id bigint NOT NULL,
    CONSTRAINT double_entries_pkey PRIMARY KEY (id),
    CONSTRAINT fk_double_entries_documents_123 FOREIGN KEY (id)
        REFERENCES documents (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE gl_operations
(
    id bigint NOT NULL,
    archive boolean,
    created_by character varying(255),
    created_on timestamp without time zone,
    hidden boolean,
    updated_by character varying(255),
    updated_on timestamp without time zone,
    version bigint NOT NULL,
    company_id bigint NOT NULL,
    export_id character varying(255),
    labels jsonb,
    credit jsonb,
    credit_rc jsonb,
    debit jsonb,
    debit_rc jsonb,
    sort_order double precision,
    sum jsonb,
    parent_id bigint,
    CONSTRAINT gl_operations_pkey PRIMARY KEY (id),
    CONSTRAINT fk_gl_operations_double_entries_123 FOREIGN KEY (parent_id)
        REFERENCES double_entries (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

CREATE SEQUENCE gama_sequence
    INCREMENT 50
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;


--
-- Test tables - must exists because Schema-validation
--

CREATE TABLE entity_master
(
    id bigint NOT NULL,
    name character varying,
    customer jsonb,
	money jsonb,
    labels jsonb, --2021 05 11
    CONSTRAINT entity_master_pkey PRIMARY KEY (id)
);

CREATE TABLE entity_child
(
    id bigint NOT NULL,
    name character varying,
    customer jsonb,
    money jsonb,
    parent_id bigint,
    CONSTRAINT entity_child_pkey PRIMARY KEY (id),
    CONSTRAINT entity_child_master_fkey FOREIGN KEY (parent_id)
            REFERENCES entity_master (id) MATCH SIMPLE
            ON UPDATE NO ACTION
            ON DELETE CASCADE
);

CREATE TABLE entity_money
(
    id bigint NOT NULL,
    amount_currency character varying(3),
    amount_amount numeric,
    big_currency character varying(3),
    big_amount numeric,
    money_currency character varying(3),
    money_amount numeric,
    cost_qty numeric,
    cost_amount_currency character varying(3),
    cost_amount_amount numeric,
    remainders jsonb,
    CONSTRAINT entity_money_pkey PRIMARY KEY (id)
);
