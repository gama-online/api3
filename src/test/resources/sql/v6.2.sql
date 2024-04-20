--
-- PostgreSQL database dump
--

-- Dumped from database version 13.2
-- Dumped by pg_dump version 13.2

-- Started on 2021-03-23 10:58:40 EET

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_table_access_method = heap;

--
-- TOC entry 200 (class 1259 OID 16395)
-- Name: documents; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE documents (
    id bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255),
    created_on timestamp without time zone,
    hidden boolean DEFAULT false,
    updated_by character varying(255),
    updated_on timestamp without time zone,
    version bigint NOT NULL,
    company_id bigint NOT NULL,
    export_id character varying(255),
    labels jsonb,
    date date,
    note character varying(4096), --v4.6 2022.03.26
    number character varying(255),
    ordinal bigint,
    series character varying(255),
    uuid uuid,
    --counterparty jsonb, removed v4.4 2022.02.22
    employee_id bigint, --v3.0 2021.04.14
    employee_name character varying(255) COLLATE pg_catalog."default",--v3.0 2021.04.14
    exchange_base character varying(3) COLLATE pg_catalog."default", --v3.0 2021.03.26
    exchange_base_amount numeric, --v3.0 2021.03.26
    exchange_currency character varying(3) COLLATE pg_catalog."default", --v3.0 2021.03.26
    exchange_amount numeric, --v3.0 2021.03.26
    exchange_date date, --v3.0 2021.03.26
    finished boolean DEFAULT false,
    finished_gl boolean DEFAULT false,
    recallable boolean DEFAULT false,
    foreign_id bigint, --v3.0 2021.03.24
    employee_db character varying(255) COLLATE pg_catalog."default", --v3.0 2021.08.06
    counterparty_id bigint, --v4.4 2022.02.22
    fs boolean, --v4.5 2022.03.07
    urls jsonb --v4.5 2022.03.07
);

--
-- TOC entry 211 (class 1259 OID 218766)
-- Name: double_entries; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE double_entries (
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255),
    created_on timestamp without time zone,
    updated_by character varying(255),
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    company_id bigint NOT NULL,
    export_id character varying(255),
    labels jsonb,
    date date,
    note character varying(255),
    number character varying(255),
    ordinal bigint,
    series character varying(255),
    uuid uuid,
    finished_gl boolean DEFAULT false,
    content character varying(255),
    frozen boolean DEFAULT false,
    --parent_counterparty jsonb, removed v4.4 2022.02.22
    parent_db character varying(255),
    parent_id bigint,
    parent_number character varying(255),
    parent_type character varying(255),
    total_currency character varying(3),
    total_amount numeric,
    foreign_id bigint, --v3.0 2021.03.24
    parent_counterparty_id bigint, --v4.4 2022.02.22
    parent_id_tmp bigint --v6.1 2024-01-17
);


--
-- TOC entry 202 (class 1259 OID 16407)
-- Name: gama_sequence; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE gama_sequence
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 203 (class 1259 OID 16409)
-- Name: gl_accounts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE gl_accounts (
    id bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255),
    created_on timestamp without time zone,
    hidden boolean DEFAULT false,
    updated_by character varying(255),
    updated_on timestamp without time zone,
    version bigint,
    company_id bigint NOT NULL,
    depth integer NOT NULL,
    "inner" boolean NOT NULL,
    name character varying(255),
    number character varying(255),
    parent character varying(255),
    type character varying(255),
    translation jsonb,
    export_id character varying(255),
    labels jsonb,
    foreign_id bigint, --v3.0 2021.03.24
    saft_number character varying(255)	--v5.6 2022-03-27
);


--
-- TOC entry 204 (class 1259 OID 16415)
-- Name: gl_ob_operations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE gl_ob_operations (
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
    rc jsonb,
    sort_nr double precision,
    parent_id bigint,
    debit_currency character varying(3),
    debit_amount numeric,
    credit_currency character varying(3),
    credit_amount numeric,
    account_number character varying(255),
    account_name character varying(255),
    foreign_id bigint --v3.0 2021.03.24
);


--
-- TOC entry 205 (class 1259 OID 16421)
-- Name: gl_opening_balances; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE gl_opening_balances (
    id bigint NOT NULL
);


--
-- TOC entry 212 (class 1259 OID 218782)
-- Name: gl_operations; Type: TABLE; Schema: public; Owner: -
-- v5.1 2022.07.13
--

CREATE TABLE gl_operations (
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255),
    created_on timestamp without time zone,
    updated_by character varying(255),
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    company_id bigint NOT NULL,
    export_id character varying(255),
    labels jsonb,
    credit_rc jsonb,
    debit_rc jsonb,
    sort_order double precision,
    parent_id bigint,
    amount_currency character varying(3), -- v5.1
    amount_amount numeric, -- v5.1
    debit_number character varying(255),
    debit_name character varying(255),
    credit_number character varying(255),
    credit_name character varying(255),
    foreign_id bigint --v3.0 2021.03.24
);


--
-- TOC entry 207 (class 1259 OID 16430)
-- Name: resp_centers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE resp_centers (
    id bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255),
    created_on timestamp without time zone,
    hidden boolean DEFAULT false,
    updated_by character varying(255),
    updated_on timestamp without time zone,
    version bigint NOT NULL,
    company_id bigint NOT NULL,
    export_id character varying(255),
    labels jsonb,
    depth integer DEFAULT 0 NOT NULL,
    description character varying(255),
    name character varying(255),
    parent bigint,
    foreign_id bigint --v3.0 2021.03.24
);


--
-- TOC entry 3189 (class 2606 OID 16437)
-- Name: documents documents_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY documents
    ADD CONSTRAINT documents_pkey PRIMARY KEY (id);


--
-- TOC entry 3197 (class 2606 OID 16441)
-- Name: gl_accounts gl_accounts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY gl_accounts
    ADD CONSTRAINT gl_accounts_pkey PRIMARY KEY (id);


--
-- TOC entry 3200 (class 2606 OID 16443)
-- Name: gl_ob_operations gl_ob_operations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY gl_ob_operations
    ADD CONSTRAINT gl_ob_operations_pkey PRIMARY KEY (id);


--
-- TOC entry 3202 (class 2606 OID 16445)
-- Name: gl_opening_balances gl_opening_balances_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY gl_opening_balances
    ADD CONSTRAINT gl_opening_balances_pkey PRIMARY KEY (id);


--
-- TOC entry 3218 (class 2606 OID 218777)
-- Name: double_entries pk_double_entries; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY double_entries
    ADD CONSTRAINT pk_double_entries PRIMARY KEY (id);


--
-- TOC entry 3220 (class 2606 OID 218791)
-- Name: gl_operations pk_gl_operations; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY gl_operations
    ADD CONSTRAINT pk_gl_operations PRIMARY KEY (id);


--
-- TOC entry 3208 (class 2606 OID 16451)
-- Name: resp_centers resp_centers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY resp_centers
    ADD CONSTRAINT resp_centers_pkey PRIMARY KEY (id);


--
-- TOC entry 3190 (class 1259 OID 16452)
-- Name: idx_documents_company_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_documents_company_id ON documents USING btree (company_id);


--
-- TOC entry 3191 (class 1259 OID 16453)
-- Name: idx_documents_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_documents_date ON documents USING btree (date);


--
-- TOC entry 3192 (class 1259 OID 16454)
-- Name: idx_documents_number; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_documents_number ON documents USING btree (number);


--
-- TOC entry 3193 (class 1259 OID 16455)
-- Name: idx_documents_uuid; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_documents_uuid ON documents USING btree (uuid);


--
-- TOC entry 3213 (class 1259 OID 218778)
-- Name: idx_double_entries_company_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_double_entries_company_id ON double_entries USING btree (company_id);


--
-- TOC entry 3214 (class 1259 OID 218779)
-- Name: idx_double_entries_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_double_entries_date ON double_entries USING btree (date);


--
-- TOC entry 3215 (class 1259 OID 218780)
-- Name: idx_double_entries_ordinal; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_double_entries_ordinal ON double_entries USING btree (ordinal);


--
-- TOC entry 3216 (class 1259 OID 218781)
-- Name: idx_double_entries_parent_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_double_entries_parent_id ON double_entries USING btree (parent_id);


--
-- TOC entry 3198 (class 1259 OID 181635)
-- Name: idx_gl_accounts_number; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gl_accounts_number ON gl_accounts USING btree (company_id, number);


--
-- TOC entry 3205 (class 1259 OID 218800)
-- Name: idx_resp_centers_company_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_resp_centers_company_id ON resp_centers USING btree (company_id);


--
-- TOC entry 3206 (class 1259 OID 218801)
-- Name: idx_resp_centers_name; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_resp_centers_name ON resp_centers USING btree (name);


--
-- TOC entry 3223 (class 2606 OID 16466)
-- Name: gl_opening_balances FKpbxqeyipeu2hu2rug6xu4yt6e; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY gl_opening_balances
    ADD CONSTRAINT "FKpbxqeyipeu2hu2rug6xu4yt6e" FOREIGN KEY (id) REFERENCES documents(id);


--
-- TOC entry 3222 (class 2606 OID 16471)
-- Name: gl_ob_operations FKpmbsicj5v89tjorsve84g9gbe; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY gl_ob_operations
    ADD CONSTRAINT "FKpmbsicj5v89tjorsve84g9gbe" FOREIGN KEY (parent_id) REFERENCES gl_opening_balances(id) ON DELETE CASCADE;


--
-- TOC entry 3225 (class 2606 OID 218792)
-- Name: gl_operations fk_gl_operations_double_entries; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY gl_operations
    ADD CONSTRAINT fk_gl_operations_double_entries FOREIGN KEY (parent_id) REFERENCES double_entries(id) ON DELETE CASCADE;


--
-- Test tables
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

-- V.2.2

-- Table: assets

-- DROP TABLE assets;

CREATE TABLE assets
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255),
    created_on timestamp without time zone,
    updated_by character varying(255),
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    company_id bigint NOT NULL,
    export_id character varying(255),
    labels jsonb,
    code character varying(255),
    cipher character varying(255),
    name character varying(255),
    tangible boolean DEFAULT false,
    note character varying(4096), --v4.6 2022.03.26
    acquisition_date date,
    cost_currency character varying(3),
    cost_amount numeric,
    vat_currency character varying(3),
    vat_amount numeric,
    date date,
    value_currency character varying(3),
    value_amount numeric,
    expenses_currency character varying(3),
    expenses_amount numeric,
    written_off_currency character varying(3),
    written_off_amount numeric,
    --responsible jsonb, removed V3.0 2021.05.11
    --location jsonb, removed V3.0 2021.05.11
    status character varying(255),
    last_date date,
    account_cost_number character varying(255),
    account_cost_name character varying(255),
    account_revaluation_number character varying(255),
    account_revaluation_name character varying(255),
    account_depreciation_number character varying(255),
    account_depreciation_name character varying(255),
    account_expense_number character varying(255),
    account_expense_name character varying(255),
    rc_expense jsonb,
    history jsonb,
    depreciation jsonb,
    foreign_id bigint,
    location_address1 character varying(255) COLLATE pg_catalog."default",--V3.0 2021.05.11
    location_address2 character varying(255) COLLATE pg_catalog."default",--V3.0 2021.05.11
    location_address3 character varying(255) COLLATE pg_catalog."default",--V3.0 2021.05.11
    location_city character varying(255) COLLATE pg_catalog."default",--V3.0 2021.05.11
    location_country character varying(255) COLLATE pg_catalog."default",--V3.0 2021.05.11
    location_municipality character varying(255) COLLATE pg_catalog."default",--V3.0 2021.05.11
    location_name character varying(255) COLLATE pg_catalog."default",--V3.0 2021.05.11
    location_zip character varying(255) COLLATE pg_catalog."default",--V3.0 2021.05.11
    responsible_id bigint,--V3.0 2021.05.11
    responsible_name character varying(255) COLLATE pg_catalog."default",--V3.0 2021.05.11
    responsible_db character varying(255) COLLATE pg_catalog."default",--V3.0 2021.08.06
    CONSTRAINT pk_assets PRIMARY KEY (id)
);

CREATE INDEX idx_assets_company_id ON assets USING btree (company_id ASC NULLS LAST);

CREATE INDEX idx_assets_date ON assets USING btree (date ASC NULLS LAST);

CREATE INDEX idx_assets_last_date ON assets USING btree (last_date ASC NULLS LAST);

--2021.03.24

CREATE TABLE counterparties
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    name character varying(255) COLLATE pg_catalog."default",
    search_name character varying(255) COLLATE pg_catalog."default",
    registration_address_address1 character varying(255) COLLATE pg_catalog."default",
    registration_address_address2 character varying(255) COLLATE pg_catalog."default",
    registration_address_address3 character varying(255) COLLATE pg_catalog."default",
    registration_address_city character varying(255) COLLATE pg_catalog."default",
    registration_address_country character varying(255) COLLATE pg_catalog."default",
    registration_address_municipality character varying(255) COLLATE pg_catalog."default",
    registration_address_name character varying(255) COLLATE pg_catalog."default",
    registration_address_zip character varying(255) COLLATE pg_catalog."default",
    business_address_address1 character varying(255) COLLATE pg_catalog."default",
    business_address_address2 character varying(255) COLLATE pg_catalog."default",
    business_address_address3 character varying(255) COLLATE pg_catalog."default",
    business_address_city character varying(255) COLLATE pg_catalog."default",
    business_address_country character varying(255) COLLATE pg_catalog."default",
    business_address_municipality character varying(255) COLLATE pg_catalog."default",
    business_address_name character varying(255) COLLATE pg_catalog."default",
    business_address_zip character varying(255) COLLATE pg_catalog."default",
    post_address_address1 character varying(255) COLLATE pg_catalog."default",
    post_address_address2 character varying(255) COLLATE pg_catalog."default",
    post_address_address3 character varying(255) COLLATE pg_catalog."default",
    post_address_city character varying(255) COLLATE pg_catalog."default",
    post_address_country character varying(255) COLLATE pg_catalog."default",
    post_address_municipality character varying(255) COLLATE pg_catalog."default",
    post_address_name character varying(255) COLLATE pg_catalog."default",
    post_address_zip character varying(255) COLLATE pg_catalog."default",
    locations jsonb,
    contacts jsonb,
    short_name character varying(255) COLLATE pg_catalog."default",
    com_code character varying(255) COLLATE pg_catalog."default",
    vat_code character varying(255) COLLATE pg_catalog."default",
    banks jsonb,
    note character varying(4096) COLLATE pg_catalog."default", --v3.0 2021.07.08 --v4.6 2022.03.26
    credit_term integer,
    credit_currency character varying(3) COLLATE pg_catalog."default", --v3.0 2021.03.29
    credit_amount numeric, --v3.0 2021.03.29
    discount double precision,
    category character varying(255) COLLATE pg_catalog."default",
    accounts jsonb,
    no_debt_account_number character varying(255) COLLATE pg_catalog."default", --v3.0 2021.03.29
    no_debt_account_name character varying(255) COLLATE pg_catalog."default", --v3.0 2021.03.29
    debts jsonb,
    used_currencies jsonb,
    no_debt boolean DEFAULT false,
    taxpayer_type character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT pk_counterparties PRIMARY KEY (id)
);

CREATE INDEX idx_counterparties_company_id
    ON counterparties USING btree
    (company_id ASC NULLS LAST)
    TABLESPACE pg_default;


--2021.03.30

CREATE TABLE debt_coverage
(
    --document_id removed v3.0 2021.05.27
    id bigint NOT NULL,--V3.0 2021.06.01
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,--V3.0 2021.06.01
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",--V3.0 2021.06.01
    labels jsonb,--V3.0 2021.06.01
    counterparty_id bigint NOT NULL, --v3.0 2021.06.04
    --currency removed v3.0 2021.06.01
    type character varying(255) COLLATE pg_catalog."default" NOT NULL, --v3.0 2021.06.04
    doc_id bigint NOT NULL, --V3.0 2021.06.04
    doc_number character varying(255) COLLATE pg_catalog."default", --V3.0 2021.04.14
    doc_series character varying(255) COLLATE pg_catalog."default", --V3.0 2021.10.05
    doc_ordinal bigint, --V3.0 2021.10.05
    doc_date date, --V3.0 2021.04.14
    doc_due_date date, --V3.0 2021.04.14
    doc_type character varying(255) COLLATE pg_catalog."default", --V3.0 2021.04.14
    doc_uuid uuid, --V4.7
    doc_employee_id bigint, --V3.0 2021.04.14
    doc_employee_name character varying(255) COLLATE pg_catalog."default", --V3.0 2021.04.14
    doc_employee_db character varying(255) COLLATE pg_catalog."default", --V3.0 2021.08.06
    doc_db character varying(255) COLLATE pg_catalog."default" NOT NULL, --v3.0 2021.06.04
    doc_note character varying(4096) COLLATE pg_catalog."default", --v4.4 2022.02.22 --v4.6 2022.03.26
    --date removed V3.0 2021.04.14
    amount_currency character varying(3) COLLATE pg_catalog."default" NOT NULL, --v3.0 2021.06.04
    amount_amount numeric,
    covered_currency character varying(3) COLLATE pg_catalog."default",
    covered_amount numeric,
    docs jsonb,
    finished boolean DEFAULT false,
    --CONSTRAINT pk_debt_coverage PRIMARY KEY (document_id, counterparty_id, type, currency, db) removed v3.0 2021.06.01
    CONSTRAINT pk_debt_coverage PRIMARY KEY (id),
    CONSTRAINT cnst_debt_coverage_doc_id_doc_db_counterparty_id_type_amount_cu UNIQUE (doc_id, doc_db, counterparty_id, type, amount_currency) --v3.0 2021.06.04
);

CREATE INDEX idx_debt_coverage_company_id
    ON debt_coverage USING btree
    (company_id ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE INDEX idx_debt_coverage_doc_ordinal --V3.0 2021.10.05
    ON debt_coverage USING btree
    (doc_ordinal ASC NULLS LAST)
    TABLESPACE pg_default;


--2021.03.29

CREATE TABLE debt_history
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    counterparty_id bigint NOT NULL, --V3.0 2021.06.04
    doc_id bigint NOT NULL, --V3.0 2021.06.04
    doc_number character varying(255) COLLATE pg_catalog."default", --V3.0 2021.04.14
    doc_series character varying(255) COLLATE pg_catalog."default", --V3.0 2021.10.05
    doc_ordinal bigint, --V3.0 2021.10.05
    doc_date date, --V3.0 2021.04.14
    doc_due_date date, --V3.0 2021.04.14
    doc_type character varying(255) COLLATE pg_catalog."default", --V3.0 2021.04.14
    doc_uuid uuid, --V4.7
    doc_employee_id bigint, --V3.0 2021.04.14
    doc_employee_name character varying(255) COLLATE pg_catalog."default", --V3.0 2021.04.14
    doc_employee_db character varying(255) COLLATE pg_catalog."default", --V3.0 2021.08.06
    doc_db character varying(255) COLLATE pg_catalog."default" NOT NULL, --V3.0 2021.06.04
    debt_currency character varying(3) COLLATE pg_catalog."default",
    debt_amount numeric,
    base_debt_currency character varying(3) COLLATE pg_catalog."default",
    base_debt_amount numeric,
    type character varying(255) COLLATE pg_catalog."default" NOT NULL, --V3.0 2021.06.04
    exchange_base character varying(3) COLLATE pg_catalog."default",
    exchange_base_amount numeric,
    exchange_currency character varying(3) COLLATE pg_catalog."default" NOT NULL, --V3.0 2021.06.04
    exchange_amount numeric,
    exchange_date date,
    due_date date,
    doc_note character varying(4096) COLLATE pg_catalog."default", --v4.4 2022.02.22 --v4.6 2022.03.26
    doc_id_tmp bigint, --v6.1 2024-01-17
    CONSTRAINT pk_debt_history PRIMARY KEY (id)
    --CONSTRAINT cnst_debt_history_doc_id_doc_db_counterparty_id_type_exchange_c UNIQUE (doc_id, doc_db, counterparty_id, type, exchange_currency) removed V3.0 2021.06.08
);

CREATE INDEX idx_debt_history_company_id
    ON debt_history USING btree
    (company_id ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE INDEX idx_debt_history_counterparty_id
    ON debt_history USING btree
    (counterparty_id ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE INDEX idx_debt_history_doc_date
    ON debt_history USING btree
    (doc_date ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE INDEX idx_debt_history_doc_ordinal --V3.0 2021.10.05
    ON debt_history USING btree
    (doc_ordinal ASC NULLS LAST)
    TABLESPACE pg_default;


--2021.03.26

CREATE TABLE debt_now
(
    --document_id removed V3.0 2021.06.01
    --timestamp removed V3.0 2021.04.14
    --companyId removed V3.0 2021.06.01
    id bigint NOT NULL,--V3.0 2021.06.01
    version bigint NOT NULL,--V3.0 2021.06.01
    archive boolean DEFAULT false,--V3.0 2021.06.01
    created_by character varying(255) COLLATE pg_catalog."default", --V3.0 2021.06.01
    created_on timestamp without time zone,--V3.0 2021.06.01
    updated_by character varying(255) COLLATE pg_catalog."default", --V3.0 2021.06.01
    updated_on timestamp without time zone, --V3.0 2021.06.01
    hidden boolean DEFAULT false, --V3.0 2021.06.01
    foreign_id bigint, --V3.0 2021.06.01
    company_id bigint NOT NULL, --V3.0 2021.06.01
    export_id character varying(255) COLLATE pg_catalog."default", --V3.0 2021.06.01
    labels jsonb,--V3.0 2021.06.01
    counterparty_id bigint NOT NULL, --V3.0 2021.06.04
    type character varying(255) COLLATE pg_catalog."default" NOT NULL, --V3.0 2021.06.04
    --currency removed V3.0 2021.06.01
    doc_id bigint NOT NULL, --V3.0 2021.06.04
    doc_number character varying(255) COLLATE pg_catalog."default", --V3.0 2021.04.14
    doc_series character varying(255) COLLATE pg_catalog."default", --V3.0 2021.10.05
    doc_ordinal bigint, --V3.0 2021.10.05
    doc_date date, --V3.0 2021.04.14
    doc_due_date date, --V3.0 2021.04.14
    doc_type character varying(255) COLLATE pg_catalog."default", --V3.0 2021.04.14
    doc_uuid uuid, --V4.7
    doc_employee_id bigint, --V3.0 2021.04.14
    doc_employee_name character varying(255) COLLATE pg_catalog."default", --V3.0 2021.04.14
    doc_employee_db character varying(255) COLLATE pg_catalog."default", --V3.0 2021.08.06
    doc_db character varying(255) COLLATE pg_catalog."default" NOT NULL, --V3.0 2021.06.04
    initial_currency character varying(3) COLLATE pg_catalog."default" NOT NULL, --v3.0 2021.06.04
    initial_amount numeric, --v3.0 2021.03.29
    remainder_currency character varying(3) COLLATE pg_catalog."default",
    remainder_amount numeric, --v3.0 2021.03.29
    due_date date,
    doc_id_tmp bigint, --v6.1 2024-01-17
    --CONSTRAINT pk_debt_now PRIMARY KEY (document_id, counterparty_id, type, currency, db) removed v3.0 2021.06.01
    doc_note character varying(4096) COLLATE pg_catalog."default", --v4.4 2022.02.22 --v4.6 2022.03.26
    CONSTRAINT pk_debt_now PRIMARY KEY (id),
    CONSTRAINT cnst_debt_now_doc_id_doc_db_counterparty_id_type_initial_curren UNIQUE (doc_id, doc_db, counterparty_id, type, initial_currency) --v3.0 2021.06.04
);

CREATE INDEX idx_debt_now_company_id
    ON debt_now USING btree
    (company_id ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE INDEX idx_debt_now_doc_ordinal --V3.0 2021.10.05
    ON debt_now USING btree
    (doc_ordinal ASC NULLS LAST)
    TABLESPACE pg_default;


--2021.04.16

CREATE TABLE debt_opening_balances
(
    id bigint NOT NULL,
    finished_debt boolean DEFAULT false,
    --counterparties removed V3.0 2021.05.04
    CONSTRAINT pk_debt_opening_balances PRIMARY KEY (id),
    CONSTRAINT fk_debt_opening_balances_documents FOREIGN KEY (id)
        REFERENCES documents (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);


--2021.05.10
-- v5.1 2022.07.13

CREATE TABLE debt_ob_counterparties
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    counterparty_id bigint,
    parent_id bigint,
    exchange_base character varying(3) COLLATE pg_catalog."default",
    exchange_base_amount numeric,
    exchange_currency character varying(3) COLLATE pg_catalog."default",
    exchange_amount numeric,
    exchange_date date,
    amount_currency character varying(3) COLLATE pg_catalog."default", -- v5.1
    amount_amount numeric, -- v5.1
    base_amount_currency character varying(3) COLLATE pg_catalog."default", -- v5.1
    base_amount_amount numeric, -- v5.1
    base_fix_amount_currency character varying(3) COLLATE pg_catalog."default", -- v5.1
    base_fix_amount_amount numeric, -- v5.1
    finished boolean DEFAULT false,
    type character varying(255) COLLATE pg_catalog."default",
    sort_order double precision, --v3.0 2021.07.08
    CONSTRAINT pk_debt_ob_counterparties PRIMARY KEY (id)
);

CREATE EXTENSION IF NOT EXISTS unaccent;

-- v4.0 2022.01.10

CREATE SEQUENCE IF NOT EXISTS gama_company_sequence
    INCREMENT 10
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

CREATE TABLE IF NOT EXISTS companies
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    name character varying(255) COLLATE pg_catalog."default",
    contacts jsonb,
    business_name character varying(255) COLLATE pg_catalog."default",
    registration_address_address1 character varying(255) COLLATE pg_catalog."default",
    registration_address_address2 character varying(255) COLLATE pg_catalog."default",
    registration_address_address3 character varying(255) COLLATE pg_catalog."default",
    registration_address_city character varying(255) COLLATE pg_catalog."default",
    registration_address_country character varying(255) COLLATE pg_catalog."default",
    registration_address_municipality character varying(255) COLLATE pg_catalog."default",
    registration_address_name character varying(255) COLLATE pg_catalog."default",
    registration_address_zip character varying(255) COLLATE pg_catalog."default",
    business_address_address1 character varying(255) COLLATE pg_catalog."default",
    business_address_address2 character varying(255) COLLATE pg_catalog."default",
    business_address_address3 character varying(255) COLLATE pg_catalog."default",
    business_address_city character varying(255) COLLATE pg_catalog."default",
    business_address_country character varying(255) COLLATE pg_catalog."default",
    business_address_municipality character varying(255) COLLATE pg_catalog."default",
    business_address_name character varying(255) COLLATE pg_catalog."default",
    business_address_zip character varying(255) COLLATE pg_catalog."default",
    locations jsonb,
    banks jsonb,
    contacts_info jsonb,
    code character varying(255) COLLATE pg_catalog."default",
    vat_code character varying(255) COLLATE pg_catalog."default",
    ss_code character varying(255) COLLATE pg_catalog."default",
    logo character varying(255) COLLATE pg_catalog."default",
    email character varying(255) COLLATE pg_catalog."default",
    cc_email character varying(255) COLLATE pg_catalog."default",
    settings jsonb,
    active_accounts integer,
    payer_accounts integer,
    other_accounts jsonb,
    status character varying(255) COLLATE pg_catalog."default",
    total_price_currency character varying(3) COLLATE pg_catalog."default",
    total_price_amount numeric,
    account_price_currency character varying(3) COLLATE pg_catalog."default",
    account_price_amount numeric,
    subscription_date date,
    subscriber_name character varying(255) COLLATE pg_catalog."default",
    subscriber_email character varying(255) COLLATE pg_catalog."default",
    last_login timestamp without time zone,
    payer_id bigint,
    ex_companies character varying(255) COLLATE pg_catalog."default",
    last_total_currency character varying(3) COLLATE pg_catalog."default",
    last_total_amount numeric,
    current_total_currency character varying(3) COLLATE pg_catalog."default",
    current_total_amount numeric,
    CONSTRAINT pk_companies PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_companies_payer_id
    ON companies USING btree
    (payer_id ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE TABLE IF NOT EXISTS connections
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    company_id bigint NOT NULL,
    labels jsonb,
    date date NOT NULL,
    total_accounts integer,
    active_accounts integer,
    payer_accounts integer,
    other_accounts jsonb,
    CONSTRAINT pk_connections PRIMARY KEY (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_connections_date
    ON connections USING btree
    (company_id ASC NULLS LAST, date ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE TABLE IF NOT EXISTS accounts
(
    id character varying(255) COLLATE pg_catalog."default" NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    password character varying(255) COLLATE pg_catalog."default",
    salt character varying(255) COLLATE pg_catalog."default",
    companies jsonb,
    default_company jsonb,
    admin boolean DEFAULT false,
    reset_token character varying(255) COLLATE pg_catalog."default",
    reset_token_date timestamp without time zone,
    refresh_token character varying(255) COLLATE pg_catalog."default",
    refresh_token_date timestamp without time zone,
    last_login timestamp without time zone,
    payer_id bigint,
    company_index integer, --v5.4
    CONSTRAINT pk_accounts PRIMARY KEY (id)
);

-- v4.1 2022.02.09

CREATE TABLE IF NOT EXISTS calendar
(
    id_country character varying(255) COLLATE pg_catalog."default" NOT NULL,
    id_year integer NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    months jsonb,
    CONSTRAINT pk_calendar PRIMARY KEY (id_country, id_year)
);

CREATE TABLE IF NOT EXISTS calendar_settings
(
    id_country character varying(255) COLLATE pg_catalog."default" NOT NULL,
    id_year integer NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    holidays jsonb,
    CONSTRAINT pk_calendar_settings PRIMARY KEY (id_country, id_year)
);

CREATE TABLE IF NOT EXISTS country_vat_code
(
    id character varying(255) COLLATE pg_catalog."default" NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    codes jsonb,
    CONSTRAINT pk_country_vat_code PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS country_vat_note
(
    id character varying(255) COLLATE pg_catalog."default" NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    notes jsonb,
    CONSTRAINT pk_country_vat_note PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS country_vat_rate
(
    id character varying(255) COLLATE pg_catalog."default" NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    vats jsonb,
    CONSTRAINT pk_country_vat_rate PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS country_work_time_code
(
    id character varying(255) COLLATE pg_catalog."default" NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    codes jsonb,
    CONSTRAINT pk_country_work_time_code PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS exchange_rate
(
    id_type character varying(255) COLLATE pg_catalog."default" NOT NULL,
    id_currency character varying(255) COLLATE pg_catalog."default" NOT NULL,
    id_date date NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    exchange_base character varying(3) COLLATE pg_catalog."default",
    exchange_base_amount numeric,
    exchange_currency character varying(3) COLLATE pg_catalog."default",
    exchange_amount numeric,
    exchange_date date,
    CONSTRAINT pk_exchange_rate PRIMARY KEY (id_type, id_currency, id_date)
);

CREATE TABLE IF NOT EXISTS sync
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    date timestamp without time zone,
    settings jsonb,
    CONSTRAINT pk_sync PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS system_settings
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    account_price_currency character varying(3) COLLATE pg_catalog."default",
    account_price_amount numeric,
    owner_company_id bigint,
    subscription_service_id bigint,
    subscription_warehouse_id bigint,
    CONSTRAINT pk_system_settings PRIMARY KEY (id)
);

-- v4.2 2022.02.10

CREATE TABLE IF NOT EXISTS import
(
    id_company_id bigint NOT NULL,
    id_external_id character varying(255) COLLATE pg_catalog."default" NOT NULL,
    id_entity_class character varying(255) COLLATE pg_catalog."default" NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    entity_id bigint NOT NULL,
    entity_db character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT pk_import PRIMARY KEY (id_company_id, id_external_id, id_entity_class)
);

-- v4.3 2022.02.14

CREATE TABLE IF NOT EXISTS counter
(
    id_company_id bigint NOT NULL,
    id_label character varying(255) COLLATE pg_catalog."default" NOT NULL,
    id_prefix character varying(255) COLLATE pg_catalog."default" NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    count integer,
    CONSTRAINT pk_counter PRIMARY KEY (id_company_id, id_label, id_prefix)
);

CREATE TABLE IF NOT EXISTS label
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    name character varying(255) COLLATE pg_catalog."default",
    type character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT pk_label PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS manufacturer
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    name character varying(255) COLLATE pg_catalog."default",
    description character varying(4096) COLLATE pg_catalog."default", --v4.6 2022.03.26
    CONSTRAINT pk_manufacturer PRIMARY KEY (id)
);

-- v4.5 2022.03.07
-- v5.1 2022.07.13

CREATE TABLE IF NOT EXISTS debt_corrections
(
    id bigint NOT NULL,
    amount_currency character varying(3) COLLATE pg_catalog."default",	-- v5.1
    amount_amount numeric, -- v5.1
    base_amount_currency character varying(3) COLLATE pg_catalog."default", -- v5.1
    base_amount_amount numeric, -- v5.1
    debit character varying(255) COLLATE pg_catalog."default",
    credit character varying(255) COLLATE pg_catalog."default",
    correction boolean,
    finished_debt boolean DEFAULT false,
    debt_type character varying(255) COLLATE pg_catalog."default", -- v5.1
    CONSTRAINT pk_debt_corrections PRIMARY KEY (id),
    CONSTRAINT fk_debt_corrections_documents FOREIGN KEY (id)
        REFERENCES documents (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS debt_rate_influences
(
    id bigint NOT NULL,
    finished_debt boolean DEFAULT false,
    CONSTRAINT pk_debt_rate_influences PRIMARY KEY (id),
    CONSTRAINT fk_debt_rate_influences_documents FOREIGN KEY (id)
        REFERENCES documents (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

-- v5.1 2022.07.13

CREATE TABLE IF NOT EXISTS debt_rate_influence_counterparties
(
    id bigint NOT NULL,
    parent_id bigint,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    counterparty_id bigint,
    type character varying(255) COLLATE pg_catalog."default" NOT NULL,
    exchange_base character varying(3) COLLATE pg_catalog."default",
    exchange_base_amount numeric,
    exchange_currency character varying(3) COLLATE pg_catalog."default",
    exchange_amount numeric,
    exchange_date date,
    amount_currency character varying(3) COLLATE pg_catalog."default", -- v5.1
    amount_amount numeric, -- v5.1
    base_amount_currency character varying(3) COLLATE pg_catalog."default", -- v5.1
    base_amount_amount numeric, -- v5.1
    base_fix_amount_currency character varying(3) COLLATE pg_catalog."default", -- v5.1
    base_fix_amount_amount numeric, -- v5.1
    finished boolean DEFAULT false,
    sort_order double precision,
    CONSTRAINT pk_debt_rate_influence_counterparties PRIMARY KEY (id)
);

-- v5.0 2021.07.13 (was created as v4.0 later renamed to v5.0 2022.02.01)

CREATE TABLE money_history
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    account_type character varying(255) COLLATE pg_catalog."default" NOT NULL,
    account_id bigint NOT NULL,
    doc_id bigint NOT NULL,
    doc_number character varying(255) COLLATE pg_catalog."default",
    doc_series character varying(255) COLLATE pg_catalog."default",
    doc_ordinal bigint, --v3.0 2021.10.18
    doc_date date,
    doc_due_date date,
    doc_type character varying(255) COLLATE pg_catalog."default",
    doc_uuid uuid,
    doc_employee_id bigint,
    doc_employee_name character varying(255) COLLATE pg_catalog."default",
    doc_employee_db character varying(255) COLLATE pg_catalog."default",
    doc_db character varying(255) COLLATE pg_catalog."default" NOT NULL,
    doc_note character varying(4096) COLLATE pg_catalog."default",
    counterparty_id bigint,
    employee_id bigint,
    cash_id bigint,
    bank_account_id bigint,
    bank_account2_id bigint, --v4.0 2021.10.27
    exchange_base character varying(3) COLLATE pg_catalog."default",
    exchange_base_amount numeric,
    exchange_currency character varying(3) COLLATE pg_catalog."default" NOT NULL,
    exchange_amount numeric,
    exchange_date date,
    amount_currency character varying(3) COLLATE pg_catalog."default",
    amount_amount numeric,
    base_amount_currency character varying(3) COLLATE pg_catalog."default",
    base_amount_amount numeric,
    CONSTRAINT pk_money_history PRIMARY KEY (id)
);

CREATE INDEX idx_money_history_bank_account2_id --v4.0 2021.10.27
    ON money_history USING btree
    (bank_account2_id ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE INDEX idx_money_history_bank_account_id
    ON money_history USING btree
    (bank_account_id ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE INDEX idx_money_history_cash_id
    ON money_history USING btree
    (cash_id ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE INDEX idx_money_history_company_id
    ON money_history USING btree
    (company_id ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE INDEX idx_money_history_counterparty_id
    ON money_history USING btree
    (counterparty_id ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE INDEX idx_money_history_doc_ordinal
    ON money_history USING btree
    (doc_ordinal ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE INDEX idx_money_history_employee_id
    ON money_history USING btree
    (employee_id ASC NULLS LAST)
    TABLESPACE pg_default;


--2021.07.14

CREATE TABLE bank_accounts
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    money_account_accounts jsonb,
    used_currencies jsonb,
    remainder jsonb,
    account character varying(255) COLLATE pg_catalog."default" NOT NULL,
    bank_name character varying(255) COLLATE pg_catalog."default",
    bank_address1 character varying(255) COLLATE pg_catalog."default",
    bank_address2 character varying(255) COLLATE pg_catalog."default",
    bank_address3 character varying(255) COLLATE pg_catalog."default",
    bank_zip character varying(255) COLLATE pg_catalog."default",
    bank_city character varying(255) COLLATE pg_catalog."default",
    bank_municipality character varying(255) COLLATE pg_catalog."default",
    bank_country character varying(255) COLLATE pg_catalog."default",
    bank_swift character varying(255) COLLATE pg_catalog."default",
    bank_code character varying(255) COLLATE pg_catalog."default",
    invoice boolean,
    cards jsonb,
    CONSTRAINT pk_bank_accounts PRIMARY KEY (id)
);

CREATE INDEX idx_bank_accounts_company_id
    ON bank_accounts USING btree
    (company_id ASC NULLS LAST)
    TABLESPACE pg_default;


--2021.07.14

CREATE TABLE cash
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    money_account_accounts jsonb,
    used_currencies jsonb,
    remainder jsonb,
    name character varying(255) COLLATE pg_catalog."default" NOT NULL,
    cashier character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT pk_cash PRIMARY KEY (id)
);

CREATE INDEX idx_cash_company_id
    ON cash USING btree
    (company_id ASC NULLS LAST)
    TABLESPACE pg_default;


--2021.11.15

CREATE TABLE employee
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    money_account_accounts jsonb,
    used_currencies jsonb,
    remainder jsonb,
    name character varying(255) COLLATE pg_catalog."default" NOT NULL,
    contacts jsonb,
    active boolean,
    email character varying(255) COLLATE pg_catalog."default",
    banks jsonb,
    address_address1 character varying(255) COLLATE pg_catalog."default",
    address_address2 character varying(255) COLLATE pg_catalog."default",
    address_address3 character varying(255) COLLATE pg_catalog."default",
    address_city character varying(255) COLLATE pg_catalog."default",
    address_country character varying(255) COLLATE pg_catalog."default",
    address_municipality character varying(255) COLLATE pg_catalog."default",
    address_name character varying(255) COLLATE pg_catalog."default",
    address_zip character varying(255) COLLATE pg_catalog."default",
    employee_id character varying(255) COLLATE pg_catalog."default",
    office character varying(255) COLLATE pg_catalog."default",
    type character varying(255) COLLATE pg_catalog."default",
    department character varying(255) COLLATE pg_catalog."default",
    cf jsonb,
    translation jsonb,
    CONSTRAINT pk_employee PRIMARY KEY (id)
);

CREATE INDEX idx_employee_company_id
    ON employee USING btree
    (company_id ASC NULLS LAST)
    TABLESPACE pg_default;


--2021.11.29

CREATE TABLE employee_cards
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    ssn character varying(255) COLLATE pg_catalog."default",
    nin character varying(255) COLLATE pg_catalog."default",
    taxes jsonb,
    hired date,
    hire_note character varying(4096) COLLATE pg_catalog."default",
    fired date,
    fire_note character varying(4096) COLLATE pg_catalog."default",
    positions jsonb,
    sex character varying(255) COLLATE pg_catalog."default",
    salary_history jsonb,
    CONSTRAINT pk_employee_card PRIMARY KEY (id)
);

CREATE INDEX idx_employee_card_company_id
    ON employee_cards USING btree
    (company_id ASC NULLS LAST)
    TABLESPACE pg_default;


--2022.02.14

CREATE TABLE work_schedules
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    name character varying(255) COLLATE pg_catalog."default",
    description character varying(255) COLLATE pg_catalog."default",
    type character varying(255) COLLATE pg_catalog."default",
    period integer,
    start date,
    schedule jsonb,
    CONSTRAINT pk_work_schedules PRIMARY KEY (id)
);

CREATE INDEX idx_work_schedules_company_id
    ON work_schedules USING btree
    (company_id ASC NULLS LAST)
    TABLESPACE pg_default;


--2022.02.14

CREATE TABLE positions
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    name character varying(255) COLLATE pg_catalog."default",
    description character varying(255) COLLATE pg_catalog."default",
    work_schedule jsonb, --v5.0 2022.02.17
    start date,
    wage_type character varying(255) COLLATE pg_catalog."default",
    wage_currency character varying(3) COLLATE pg_catalog."default",
    wage_amount numeric,
    advance_currency character varying(3) COLLATE pg_catalog."default",
    advance_amount numeric,
    CONSTRAINT pk_positions PRIMARY KEY (id)
);

CREATE INDEX idx_positions_company_id
    ON positions USING btree
    (company_id ASC NULLS LAST)
    TABLESPACE pg_default;


--2022.02.15

CREATE TABLE roles
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    name character varying(255) COLLATE pg_catalog."default",
    description character varying(255) COLLATE pg_catalog."default",
    permissions jsonb,
    CONSTRAINT pk_roles PRIMARY KEY (id)
);

CREATE INDEX idx_roles_company_id
    ON roles USING btree
    (company_id ASC NULLS LAST)
    TABLESPACE pg_default;


--2022.02.15

CREATE TABLE charges
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    name character varying(255) COLLATE pg_catalog."default",
    debit_number character varying(255) COLLATE pg_catalog."default",
    debit_name character varying(255) COLLATE pg_catalog."default",
    credit_number character varying(255) COLLATE pg_catalog."default",
    credit_name character varying(255) COLLATE pg_catalog."default",
    avg_salary character varying(255) COLLATE pg_catalog."default",
    period integer,
    employee_ss_tax_debit_number character varying(255) COLLATE pg_catalog."default", -- V5.1
    employee_ss_tax_debit_name character varying(255) COLLATE pg_catalog."default", -- V5.1
    employee_ss_tax_credit_number character varying(255) COLLATE pg_catalog."default", -- V5.1
    employee_ss_tax_credit_name character varying(255) COLLATE pg_catalog."default", -- V5.1
    employee_ss_tax_active boolean, -- V5.1
    employee_ss_tax_rate numeric, -- V5.1
    company_ss_tax_debit_number character varying(255) COLLATE pg_catalog."default", -- V5.1
    company_ss_tax_debit_name character varying(255) COLLATE pg_catalog."default", -- V5.1
    company_ss_tax_credit_number character varying(255) COLLATE pg_catalog."default", -- V5.1
    company_ss_tax_credit_name character varying(255) COLLATE pg_catalog."default", -- V5.1
    company_ss_tax_active boolean, -- V5.1
    company_ss_tax_rate numeric, -- V5.1
    income_tax_debit_number character varying(255) COLLATE pg_catalog."default",
    income_tax_debit_name character varying(255) COLLATE pg_catalog."default",
    income_tax_credit_number character varying(255) COLLATE pg_catalog."default",
    income_tax_credit_name character varying(255) COLLATE pg_catalog."default",
    income_tax_active boolean,
    income_tax_rate numeric,
    guaranty_fund_debit_number character varying(255) COLLATE pg_catalog."default",
    guaranty_fund_debit_name character varying(255) COLLATE pg_catalog."default",
    guaranty_fund_credit_number character varying(255) COLLATE pg_catalog."default",
    guaranty_fund_credit_name character varying(255) COLLATE pg_catalog."default",
    guaranty_fund_active boolean,
    guaranty_fund_rate numeric,
    shi_tax_debit_number character varying(255) COLLATE pg_catalog."default",
    shi_tax_debit_name character varying(255) COLLATE pg_catalog."default",
    shi_tax_credit_number character varying(255) COLLATE pg_catalog."default",
    shi_tax_credit_name character varying(255) COLLATE pg_catalog."default",
    shi_tax_active boolean,
    shi_tax_rate numeric,
    CONSTRAINT pk_charges PRIMARY KEY (id)
);

CREATE INDEX idx_charges_company_id
    ON charges USING btree
    (company_id ASC NULLS LAST)
    TABLESPACE pg_default;


--2022.02.28

CREATE TABLE IF NOT EXISTS employee_roles
(
    employee_id bigint NOT NULL,
    roles_id bigint NOT NULL,
    CONSTRAINT pk_employee_roles PRIMARY KEY (employee_id, roles_id),
    CONSTRAINT fk_employee_roles_employee FOREIGN KEY (employee_id)
        REFERENCES employee (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT fk_employee_roles_roles FOREIGN KEY (roles_id)
        REFERENCES roles (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

--2022.04.19

CREATE TABLE IF NOT EXISTS employee_absence
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    employee_id bigint,
    date_from date NOT NULL,
    date_to date NOT NULL,
    code_code character varying(255) COLLATE pg_catalog."default",
    code_name character varying(255) COLLATE pg_catalog."default",
    code_type character varying(255) COLLATE pg_catalog."default",
    code_avg_pay boolean,
    document character varying(4096) COLLATE pg_catalog."default",
    weekends boolean,
    holidays boolean,
    CONSTRAINT pk_employee_absence PRIMARY KEY (id),
    CONSTRAINT fk_employee_absence_employee FOREIGN KEY (employee_id)
        REFERENCES employee (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS employee_vacation
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    employee_id bigint,
    vacations jsonb,
    CONSTRAINT pk_employee_vacation PRIMARY KEY (id),
    CONSTRAINT fk_employee_vacation_employee FOREIGN KEY (employee_id)
        REFERENCES employee (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS salary
(
    id bigint NOT NULL,
    charges jsonb,
    employee_ss_tax_currency character varying(3) COLLATE pg_catalog."default",
    employee_ss_tax_amount numeric,
    company_ss_tax_rate numeric,
    company_ss_tax_currency character varying(3) COLLATE pg_catalog."default",
    company_ss_tax_amount numeric,
    income_tax_rate numeric,
    income_tax_currency character varying(3) COLLATE pg_catalog."default",
    income_tax_amount numeric,
    guaranty_fund_tax_rate numeric,
    guaranty_fund_tax_currency character varying(3) COLLATE pg_catalog."default",
    guaranty_fund_tax_amount numeric,
    shi_tax_rate numeric,
    shi_tax_currency character varying(3) COLLATE pg_catalog."default",
    shi_tax_amount numeric,
    total_currency character varying(3) COLLATE pg_catalog."default",
    total_amount numeric,
    total_ss_currency character varying(3) COLLATE pg_catalog."default",
    total_ss_amount numeric,
    total_income_currency character varying(3) COLLATE pg_catalog."default",
    total_income_amount numeric,
    net_currency character varying(3) COLLATE pg_catalog."default",
    net_amount numeric,
    advance_currency character varying(3) COLLATE pg_catalog."default",
    advance_amount numeric,
    net_total_currency character varying(3) COLLATE pg_catalog."default",
    net_total_amount numeric,
    CONSTRAINT pk_salary PRIMARY KEY (id),
    CONSTRAINT fk_salary_documents FOREIGN KEY (id)
        REFERENCES documents (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS employee_charge
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    parent_id bigint NOT NULL,
    employee_id bigint NOT NULL,
    date date NOT NULL,
    tax_exempt_currency character varying(3) COLLATE pg_catalog."default",
    tax_exempt_amount numeric,
    add_tax_exempt_currency character varying(3) COLLATE pg_catalog."default",
    add_tax_exempt_amount numeric,
    charges jsonb,
    employee_ss_tax_currency character varying(3) COLLATE pg_catalog."default",
    employee_ss_tax_amount numeric,
    company_ss_tax_currency character varying(3) COLLATE pg_catalog."default",
    company_ss_tax_amount numeric,
    income_tax_currency character varying(3) COLLATE pg_catalog."default",
    income_tax_amount numeric,
    guaranty_fund_tax_currency character varying(3) COLLATE pg_catalog."default",
    guaranty_fund_tax_amount numeric,
    shi_tax_currency character varying(3) COLLATE pg_catalog."default",
    shi_tax_amount numeric,
    total_currency character varying(3) COLLATE pg_catalog."default",
    total_amount numeric,
    total_ss_currency character varying(3) COLLATE pg_catalog."default",
    total_ss_amount numeric,
    total_income_currency character varying(3) COLLATE pg_catalog."default",
    total_income_amount numeric,
    net_currency character varying(3) COLLATE pg_catalog."default",
    net_amount numeric,
    advance_currency character varying(3) COLLATE pg_catalog."default",
    advance_amount numeric,
    net_total_currency character varying(3) COLLATE pg_catalog."default",
    net_total_amount numeric,
    work_data_days integer,
    work_data_hours integer,
    work_data_worked integer,
    work_data_overtime integer,
    work_data_night integer,
    work_data_weekend integer,
    work_data_holiday integer,
    work_data_overtime_night integer,
    work_data_overtime_weekend integer,
    work_data_overtime_holiday integer,
    work_data_vacation integer,
    work_data_child_days integer,
    work_data_illness integer,
    operations jsonb,
    finished boolean,
    CONSTRAINT pk_employee_charge PRIMARY KEY (id),
    CONSTRAINT cnst_employee_charge_employee UNIQUE (parent_id, employee_id),
    CONSTRAINT fk_employee_charge_employee FOREIGN KEY (employee_id)
        REFERENCES employee (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT fk_employee_charge_salary FOREIGN KEY (parent_id)
        REFERENCES salary (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS work_hours
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    employee_id bigint,
    date date NOT NULL,
    positions jsonb,
    main_position jsonb,
    fixed boolean,
    finished boolean,
    CONSTRAINT pk_work_hours PRIMARY KEY (id),
    CONSTRAINT cnst_work_hours_employee UNIQUE (company_id, date, employee_id),
    CONSTRAINT fk_work_hours_employee FOREIGN KEY (employee_id)
        REFERENCES employee (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

-- =====================================================================================================================
-- v5.1 2022.07.15
-- =====================================================================================================================

CREATE TABLE IF NOT EXISTS cash_ob
(
    id bigint NOT NULL,
    CONSTRAINT pk_cash_ob PRIMARY KEY (id),
    CONSTRAINT fk_cash_ob_documents FOREIGN KEY (id)
        REFERENCES documents (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS cash_ob_balances
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    parent_id bigint NOT NULL,
    cash_id bigint NOT NULL,
    exchange_base character varying(3) COLLATE pg_catalog."default",
    exchange_base_amount numeric,
    exchange_currency character varying(3) COLLATE pg_catalog."default",
    exchange_amount numeric,
    exchange_date date,
    amount_currency character varying(3) COLLATE pg_catalog."default",
    amount_amount numeric,
    base_amount_currency character varying(3) COLLATE pg_catalog."default",
    base_amount_amount numeric,
    base_fix_amount_currency character varying(3) COLLATE pg_catalog."default",
    base_fix_amount_amount numeric,
    finished boolean DEFAULT false,
    sort_order double precision,
    CONSTRAINT pk_cash_ob_balances PRIMARY KEY (id),
    CONSTRAINT fk_cash_ob_balances_cash FOREIGN KEY (cash_id)
        REFERENCES cash (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT fk_cash_ob_balances_parent FOREIGN KEY (parent_id)
        REFERENCES cash_ob (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS cash_operation
(
    id bigint NOT NULL,
    amount_currency character varying(3) COLLATE pg_catalog."default",
    amount_amount numeric,
    base_amount_currency character varying(3) COLLATE pg_catalog."default",
    base_amount_amount numeric,
    finished_money jsonb,
    no_debt boolean DEFAULT false,
    finished_debt boolean DEFAULT false,
    debt_type character varying(255) COLLATE pg_catalog."default",
    cash_id bigint NOT NULL,
    CONSTRAINT pk_cash_operation PRIMARY KEY (id),
    CONSTRAINT fk_cash_operation_cash FOREIGN KEY (cash_id)
        REFERENCES cash (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_cash_operation_documents FOREIGN KEY (id)
        REFERENCES documents (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS cash_rate_influences
(
    id bigint NOT NULL,
    CONSTRAINT pk_cash_rate_influences PRIMARY KEY (id),
    CONSTRAINT fk_cash_rate_influences_documents FOREIGN KEY (id)
        REFERENCES documents (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS cash_rate_influences_cash
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    parent_id bigint NOT NULL,
    cash_id bigint NOT NULL,
    exchange_base character varying(3) COLLATE pg_catalog."default",
    exchange_base_amount numeric,
    exchange_currency character varying(3) COLLATE pg_catalog."default",
    exchange_amount numeric,
    exchange_date date,
    amount_currency character varying(3) COLLATE pg_catalog."default",
    amount_amount numeric,
    base_amount_currency character varying(3) COLLATE pg_catalog."default",
    base_amount_amount numeric,
    base_fix_amount_currency character varying(3) COLLATE pg_catalog."default",
    base_fix_amount_amount numeric,
    finished boolean DEFAULT false,
    sort_order double precision,
    CONSTRAINT pk_cash_rate_influences_cash PRIMARY KEY (id),
    CONSTRAINT fk_cash_rate_influences_cash FOREIGN KEY (cash_id)
        REFERENCES cash (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT fk_cash_rate_influences_parent FOREIGN KEY (parent_id)
        REFERENCES cash_rate_influences (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS bank_ob
(
    id bigint NOT NULL,
    CONSTRAINT pk_bank_ob PRIMARY KEY (id),
    CONSTRAINT fk_bank_ob_documents FOREIGN KEY (id)
        REFERENCES documents (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS bank_ob_balances
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    parent_id bigint NOT NULL,
    bank_account_id bigint NOT NULL,
    exchange_base character varying(3) COLLATE pg_catalog."default",
    exchange_base_amount numeric,
    exchange_currency character varying(3) COLLATE pg_catalog."default",
    exchange_amount numeric,
    exchange_date date,
    amount_currency character varying(3) COLLATE pg_catalog."default",
    amount_amount numeric,
    base_amount_currency character varying(3) COLLATE pg_catalog."default",
    base_amount_amount numeric,
    base_fix_amount_currency character varying(3) COLLATE pg_catalog."default",
    base_fix_amount_amount numeric,
    finished boolean DEFAULT false,
    sort_order double precision,
    CONSTRAINT pk_bank_ob_balances PRIMARY KEY (id),
    CONSTRAINT fk_bank_ob_balances_bank_account FOREIGN KEY (bank_account_id)
        REFERENCES bank_accounts (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT fk_bank_ob_balances_parent FOREIGN KEY (parent_id)
        REFERENCES bank_ob (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS bank_operation
(
    id bigint NOT NULL,
    amount_currency character varying(3) COLLATE pg_catalog."default",
    amount_amount numeric,
    base_amount_currency character varying(3) COLLATE pg_catalog."default",
    base_amount_amount numeric,
    finished_money jsonb,
    no_debt boolean DEFAULT false,
    finished_debt boolean DEFAULT false,
    debt_type character varying(255) COLLATE pg_catalog."default",
    bank_account_id bigint NOT NULL,
    bank_account2_id bigint,
    cash_operation boolean DEFAULT false,
    payment_code boolean DEFAULT false,
    other_account character varying(255),	-- v5.2
    CONSTRAINT pk_bank_operation PRIMARY KEY (id),
    CONSTRAINT fk_bank_operation_bank_account FOREIGN KEY (bank_account_id)
        REFERENCES bank_accounts (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_bank_operation_bank_account2 FOREIGN KEY (bank_account2_id)
        REFERENCES bank_accounts (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_bank_operation_documents FOREIGN KEY (id)
        REFERENCES documents (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS bank_rate_influences
(
    id bigint NOT NULL,
    CONSTRAINT pk_bank_rate_influences PRIMARY KEY (id),
    CONSTRAINT fk_bank_rate_influences_documents FOREIGN KEY (id)
        REFERENCES documents (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS bank_rate_influences_bank
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    parent_id bigint NOT NULL,
    bank_account_id bigint NOT NULL,
    exchange_base character varying(3) COLLATE pg_catalog."default",
    exchange_base_amount numeric,
    exchange_currency character varying(3) COLLATE pg_catalog."default",
    exchange_amount numeric,
    exchange_date date,
    amount_currency character varying(3) COLLATE pg_catalog."default",
    amount_amount numeric,
    base_amount_currency character varying(3) COLLATE pg_catalog."default",
    base_amount_amount numeric,
    base_fix_amount_currency character varying(3) COLLATE pg_catalog."default",
    base_fix_amount_amount numeric,
    finished boolean DEFAULT false,
    sort_order double precision,
    CONSTRAINT pk_bank_rate_influences_bank PRIMARY KEY (id),
    CONSTRAINT fk_bank_rate_influences_bank_account FOREIGN KEY (bank_account_id)
        REFERENCES bank_accounts (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT fk_bank_rate_influences_parent FOREIGN KEY (parent_id)
        REFERENCES bank_rate_influences (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS employee_ob
(
    id bigint NOT NULL,
    CONSTRAINT pk_employee_ob PRIMARY KEY (id),
    CONSTRAINT fk_employee_ob_documents FOREIGN KEY (id)
        REFERENCES documents (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS employee_ob_balances
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    parent_id bigint NOT NULL,
    employee_id bigint NOT NULL,
    exchange_base character varying(3) COLLATE pg_catalog."default",
    exchange_base_amount numeric,
    exchange_currency character varying(3) COLLATE pg_catalog."default",
    exchange_amount numeric,
    exchange_date date,
    amount_currency character varying(3) COLLATE pg_catalog."default",
    amount_amount numeric,
    base_amount_currency character varying(3) COLLATE pg_catalog."default",
    base_amount_amount numeric,
    base_fix_amount_currency character varying(3) COLLATE pg_catalog."default",
    base_fix_amount_amount numeric,
    finished boolean DEFAULT false,
    sort_order double precision,
    CONSTRAINT pk_employee_ob_balances PRIMARY KEY (id),
    CONSTRAINT fk_employee_ob_balances_employee FOREIGN KEY (employee_id)
        REFERENCES employee (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT fk_employee_ob_balances_parent FOREIGN KEY (parent_id)
        REFERENCES employee_ob (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS employee_operation
(
    id bigint NOT NULL,
    amount_currency character varying(3) COLLATE pg_catalog."default",
    amount_amount numeric,
    base_amount_currency character varying(3) COLLATE pg_catalog."default",
    base_amount_amount numeric,
    finished_money jsonb,
    no_debt boolean DEFAULT false,
    finished_debt boolean DEFAULT false,
    debt_type character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT pk_employee_operation PRIMARY KEY (id),
    CONSTRAINT fk_employee_operation_documents FOREIGN KEY (id)
        REFERENCES documents (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS employee_rate_influences
(
    id bigint NOT NULL,
    CONSTRAINT pk_employee_rate_influences PRIMARY KEY (id),
    CONSTRAINT fk_employee_rate_influences_documents FOREIGN KEY (id)
        REFERENCES documents (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS employee_rate_influences_employee
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    parent_id bigint NOT NULL,
    employee_id bigint NOT NULL,
    exchange_base character varying(3) COLLATE pg_catalog."default",
    exchange_base_amount numeric,
    exchange_currency character varying(3) COLLATE pg_catalog."default",
    exchange_amount numeric,
    exchange_date date,
    amount_currency character varying(3) COLLATE pg_catalog."default",
    amount_amount numeric,
    base_amount_currency character varying(3) COLLATE pg_catalog."default",
    base_amount_amount numeric,
    base_fix_amount_currency character varying(3) COLLATE pg_catalog."default",
    base_fix_amount_amount numeric,
    finished boolean DEFAULT false,
    sort_order double precision,
    CONSTRAINT pk_employee_rate_influences_employee PRIMARY KEY (id),
    CONSTRAINT fk_employee_rate_influences_employee FOREIGN KEY (employee_id)
        REFERENCES employee (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT fk_employee_rate_influences_parent FOREIGN KEY (parent_id)
        REFERENCES employee_rate_influences (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

ALTER TABLE ONLY documents
    ADD CONSTRAINT fk_documents_counterparty FOREIGN KEY (counterparty_id) REFERENCES counterparties(id);

ALTER TABLE ONLY documents
    ADD CONSTRAINT fk_documents_employee FOREIGN KEY (employee_id) REFERENCES employee(id);

ALTER TABLE ONLY debt_ob_counterparties
    ADD CONSTRAINT fk_debt_ob_counterparty FOREIGN KEY (counterparty_id) REFERENCES counterparties(id);

ALTER TABLE ONLY debt_rate_influence_counterparties
    ADD CONSTRAINT fk_debt_rate_influence_counterparty FOREIGN KEY (counterparty_id) REFERENCES counterparties(id);

ALTER TABLE ONLY debt_coverage
    ADD CONSTRAINT fk_debt_coverage_counterparty FOREIGN KEY (counterparty_id) REFERENCES counterparties(id);

ALTER TABLE ONLY debt_history
    ADD CONSTRAINT fk_debt_history_counterparty FOREIGN KEY (counterparty_id) REFERENCES counterparties(id);

ALTER TABLE ONLY debt_now
    ADD CONSTRAINT fk_debt_now_counterparty FOREIGN KEY (counterparty_id) REFERENCES counterparties(id);

-- v5.5 - created indexes

CREATE INDEX idx_gl_operations_parent ON gl_operations USING btree (parent_id);

CREATE INDEX idx_bank_ob_balances_bank_account ON bank_ob_balances USING btree (bank_account_id);

CREATE INDEX idx_bank_ob_balances_parent ON bank_ob_balances USING btree (parent_id);

CREATE INDEX idx_bank_operation_bank_account ON bank_operation USING btree (bank_account_id);

CREATE INDEX idx_bank_operation_bank_account2 ON bank_operation USING btree (bank_account2_id);

CREATE INDEX idx_bank_rate_influences_bank_bank_account ON bank_rate_influences_bank USING btree (bank_account_id);

CREATE INDEX idx_bank_rate_influences_bank_parent ON bank_rate_influences_bank USING btree (parent_id);

CREATE INDEX idx_cash_ob_balances_cash ON cash_ob_balances USING btree (cash_id);

CREATE INDEX idx_cash_ob_balances_parent ON cash_ob_balances USING btree (parent_id);

CREATE INDEX idx_cash_operation_cash ON cash_operation USING btree (cash_id);

CREATE INDEX idx_cash_rate_influences_cash_cash ON cash_rate_influences_cash USING btree (cash_id);

CREATE INDEX idx_cash_rate_influences_cash_parent ON cash_rate_influences_cash USING btree (parent_id);

CREATE INDEX idx_debt_ob_counterparties_counterparty ON debt_ob_counterparties USING btree (counterparty_id);

ALTER TABLE ONLY debt_ob_counterparties
    ADD CONSTRAINT fk_debt_ob_counterparties_parent FOREIGN KEY (parent_id) REFERENCES debt_opening_balances(id);

CREATE INDEX idx_debt_ob_counterparties_parent ON debt_ob_counterparties USING btree (parent_id);

CREATE INDEX idx_debt_rate_influences_counterparties_counterparty ON debt_rate_influence_counterparties USING btree (counterparty_id);

ALTER TABLE ONLY debt_rate_influence_counterparties
    ADD CONSTRAINT fk_debt_rate_influences_counterparties_parent FOREIGN KEY (parent_id) REFERENCES debt_rate_influences(id);

CREATE INDEX idx_debt_rate_influences_counterparties_parent ON debt_rate_influence_counterparties USING btree (parent_id);

CREATE INDEX idx_employee_absence_employee ON employee_absence USING btree (employee_id);

CREATE INDEX idx_employee_ob_balances_employee ON employee_ob_balances USING btree (employee_id);

CREATE INDEX idx_employee_ob_balances_parent ON employee_ob_balances USING btree (parent_id);

CREATE INDEX idx_documents_counterparty ON documents USING btree (counterparty_id);

CREATE INDEX idx_documents_employee ON documents USING btree (employee_id);

CREATE INDEX idx_employee_rate_influences_employee_employee ON employee_rate_influences_employee USING btree (employee_id);

CREATE INDEX idx_employee_rate_influences_employee_parent ON employee_rate_influences_employee USING btree (parent_id);

CREATE INDEX idx_employee_charge_parent ON employee_charge USING btree (parent_id);

CREATE INDEX idx_employee_charge_employee ON employee_charge USING btree (employee_id);

CREATE INDEX idx_employee_vacation_employee ON employee_vacation USING btree (employee_id);

CREATE INDEX idx_gl_ob_operations_parent ON gl_ob_operations USING btree (parent_id);

CREATE INDEX idx_work_hours_employee ON work_hours USING btree (employee_id);

-- v5.6 2022-03-27

CREATE TABLE IF NOT EXISTS gl_saft_accounts
(
    "number" character varying(255) COLLATE pg_catalog."default" NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    name character varying(255) COLLATE pg_catalog."default" NOT NULL,
    "inner" boolean NOT NULL,
    depth integer NOT NULL,
    parent character varying(255) COLLATE pg_catalog."default",
    type character varying(255) COLLATE pg_catalog."default",
    translation jsonb,
    CONSTRAINT pk_gl_saft_accounts PRIMARY KEY ("number")
);

--v6.0 2022.06.03

CREATE TABLE IF NOT EXISTS warehouse
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    name character varying(255) COLLATE pg_catalog."default",
    location_address1 character varying(255) COLLATE pg_catalog."default",
    location_address2 character varying(255) COLLATE pg_catalog."default",
    location_address3 character varying(255) COLLATE pg_catalog."default",
    location_city character varying(255) COLLATE pg_catalog."default",
    location_country character varying(255) COLLATE pg_catalog."default",
    location_municipality character varying(255) COLLATE pg_catalog."default",
    location_name character varying(255) COLLATE pg_catalog."default",
    location_zip character varying(255) COLLATE pg_catalog."default",
    storekeeper_id bigint,
    closed boolean DEFAULT false,
    with_tag boolean DEFAULT false,
    CONSTRAINT pk_warehouse PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_warehouse_company_id
    ON warehouse USING btree
    (company_id ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE TABLE IF NOT EXISTS parts
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    type character varying(255) COLLATE pg_catalog."default" NOT NULL,
    name character varying(255) COLLATE pg_catalog."default",
    description character varying(4096) COLLATE pg_catalog."default",
    sku character varying(255) COLLATE pg_catalog."default",
    barcode character varying(255) COLLATE pg_catalog."default",
    unit character varying(255) COLLATE pg_catalog."default",
    price_currency character varying(3) COLLATE pg_catalog."default",
    price_amount numeric,
    account_asset_number character varying(255) COLLATE pg_catalog."default",
    account_asset_name character varying(255) COLLATE pg_catalog."default",
    gl_income_debit_number character varying(255) COLLATE pg_catalog."default",
    gl_income_debit_name character varying(255) COLLATE pg_catalog."default",
    gl_income_credit_number character varying(255) COLLATE pg_catalog."default",
    gl_income_credit_name character varying(255) COLLATE pg_catalog."default",
    gl_expense_debit_number character varying(255) COLLATE pg_catalog."default",
    gl_expense_debit_name character varying(255) COLLATE pg_catalog."default",
    gl_expense_credit_number character varying(255) COLLATE pg_catalog."default",
    gl_expense_credit_name character varying(255) COLLATE pg_catalog."default",
    taxable boolean DEFAULT false,
    vat_rate_code character varying(255) COLLATE pg_catalog."default",
    cf jsonb,
    remainder jsonb,
    quantity_total numeric,
    cost_total_currency character varying(3) COLLATE pg_catalog."default",
    cost_total_amount numeric,
    forward_sell boolean DEFAULT false,
    manufacturer_id bigint,
    brutto numeric,
    netto numeric,
-- v6.2 2024-02-05
--    pack1_type character varying(255) COLLATE pg_catalog."default",
--    pack2_type character varying(255) COLLATE pg_catalog."default",
--    pack3_type character varying(255) COLLATE pg_catalog."default",
--    pack1_weight numeric,
--    pack2_weight numeric,
--    pack3_weight numeric,
	packaging jsonb,  -- v6.2 2024-02-05
    units_weight character varying(255) COLLATE pg_catalog."default",
    pack_units integer,
    length numeric,
    width numeric,
    height numeric,
    units_length character varying(255) COLLATE pg_catalog."default",
    vendor_id bigint,
    vendor_code character varying(255) COLLATE pg_catalog."default",
    translation jsonb,
    CONSTRAINT pk_parts PRIMARY KEY (id),
    CONSTRAINT fk_parts_counterparties FOREIGN KEY (vendor_id)
            REFERENCES counterparties (id) MATCH SIMPLE
            ON UPDATE NO ACTION
            ON DELETE NO ACTION,
        CONSTRAINT fk_parts_manufacturer FOREIGN KEY (manufacturer_id)
            REFERENCES manufacturer (id) MATCH SIMPLE
            ON UPDATE NO ACTION
            ON DELETE NO ACTION
);

CREATE INDEX IF NOT EXISTS idx_parts_company_id
    ON parts USING btree
    (company_id ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE TABLE IF NOT EXISTS inventory_history
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    part_id bigint NOT NULL,
    warehouse_id bigint NOT NULL,
    sn_sn character varying(255) COLLATE pg_catalog."default",
    sn_cf jsonb,
    uuid uuid,
    inventory_type character varying(255) COLLATE pg_catalog."default" NOT NULL,
    doc_id bigint NOT NULL,
    doc_number character varying(255) COLLATE pg_catalog."default",
    doc_series character varying(255) COLLATE pg_catalog."default",
    doc_ordinal bigint,
    doc_date date,
    doc_due_date date,
    doc_note character varying(4096) COLLATE pg_catalog."default",
    doc_type character varying(255) COLLATE pg_catalog."default",
    doc_uuid uuid,
    doc_employee_id bigint,
    doc_employee_name character varying(255) COLLATE pg_catalog."default",
    doc_employee_db character varying(255) COLLATE pg_catalog."default",
    doc_db character varying(255) COLLATE pg_catalog."default" NOT NULL,
    counterparty_id bigint,
    origin_doc_id bigint NOT NULL,
    origin_doc_number character varying(255) COLLATE pg_catalog."default",
    origin_doc_series character varying(255) COLLATE pg_catalog."default",
    origin_doc_ordinal bigint,
    origin_doc_date date,
    origin_doc_due_date date,
    origin_doc_note character varying(4096) COLLATE pg_catalog."default",
    origin_doc_type character varying(255) COLLATE pg_catalog."default",
    origin_doc_uuid uuid,
    origin_doc_employee_id bigint,
    origin_doc_employee_name character varying(255) COLLATE pg_catalog."default",
    origin_doc_employee_db character varying(255) COLLATE pg_catalog."default",
    origin_doc_db character varying(255) COLLATE pg_catalog."default" NOT NULL,
    origin_counterparty_id bigint,
    quantity numeric,
    cost_total_currency character varying(3) COLLATE pg_catalog."default",
    cost_total_amount numeric,
    sell_total_currency character varying(3) COLLATE pg_catalog."default",
    sell_total_amount numeric,
    sell_base_total_currency character varying(3) COLLATE pg_catalog."default",
    sell_base_total_amount numeric,
    CONSTRAINT pk_inventory_history PRIMARY KEY (id),
    CONSTRAINT fk_counterparty_inventory_history_counterparties FOREIGN KEY (counterparty_id)
        REFERENCES counterparties (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_inventory_history_parts FOREIGN KEY (part_id)
        REFERENCES parts (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_inventory_history_warehouse FOREIGN KEY (warehouse_id)
        REFERENCES warehouse (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_origin_counterparty_inventory_history_counterparties FOREIGN KEY (origin_counterparty_id)
        REFERENCES counterparties (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE INDEX IF NOT EXISTS idx_inventory_history_company_id
    ON inventory_history USING btree
    (company_id ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE TABLE IF NOT EXISTS inventory_now
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    "timestamp" character varying(255) COLLATE pg_catalog."default",
    part_id bigint NOT NULL,
    sn_sn character varying(255) COLLATE pg_catalog."default",
    sn_cf jsonb,
    doc_id bigint NOT NULL,
    doc_number character varying(255) COLLATE pg_catalog."default",
    doc_series character varying(255) COLLATE pg_catalog."default",
    doc_ordinal bigint,
    doc_date date,
    doc_due_date date,
    doc_note character varying(4096) COLLATE pg_catalog."default",
    doc_type character varying(255) COLLATE pg_catalog."default",
    doc_uuid uuid,
    doc_employee_id bigint,
    doc_employee_name character varying(255) COLLATE pg_catalog."default",
    doc_employee_db character varying(255) COLLATE pg_catalog."default",
    doc_db character varying(255) COLLATE pg_catalog."default" NOT NULL,
    counterparty_id bigint,
    warehouse_id bigint NOT NULL,
    remainder jsonb,
    quantity numeric,
    cost_total_currency character varying(3) COLLATE pg_catalog."default",
    cost_total_amount numeric,
    tag character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT pk_inventory_now PRIMARY KEY (id),
    CONSTRAINT fk_inventory_now_counterparties FOREIGN KEY (counterparty_id)
        REFERENCES counterparties (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_inventory_now_parts FOREIGN KEY (part_id)
        REFERENCES parts (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_inventory_now_warehouse FOREIGN KEY (warehouse_id)
        REFERENCES warehouse (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE INDEX IF NOT EXISTS idx_inventory_now_company_id
    ON inventory_now USING btree
    (company_id ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE INDEX IF NOT EXISTS idx_inventory_now_part_id
    ON inventory_now USING btree
    (part_id ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE INDEX IF NOT EXISTS idx_inventory_now_sn_cf
    ON inventory_now USING btree
    (sn_cf ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE INDEX IF NOT EXISTS idx_inventory_now_sn_sn
    ON inventory_now USING btree
    (sn_sn COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE INDEX IF NOT EXISTS idx_inventory_now_warehouse_id
    ON inventory_now USING btree
    (warehouse_id ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE TABLE IF NOT EXISTS part_parts
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    sort_order double precision,
    quantity numeric,
    parent_id bigint NOT NULL,
    part_id bigint NOT NULL,
    CONSTRAINT pk_part_part PRIMARY KEY (id),
    CONSTRAINT fk_parent_part_parts_parts FOREIGN KEY (parent_id)
            REFERENCES parts (id) MATCH SIMPLE
            ON UPDATE NO ACTION
            ON DELETE NO ACTION,
        CONSTRAINT fk_part_part_parts_parts FOREIGN KEY (part_id)
            REFERENCES parts (id) MATCH SIMPLE
            ON UPDATE NO ACTION
            ON DELETE NO ACTION
);

CREATE INDEX IF NOT EXISTS idx_part_parts_company_id
    ON part_parts USING btree
    (company_id ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE TABLE IF NOT EXISTS inventory
(
    id bigint NOT NULL,
    warehouse_id bigint NOT NULL,
    tag character varying(255) COLLATE pg_catalog."default",
    finished_parts boolean DEFAULT false,
    inventory_note character varying(4096) COLLATE pg_catalog."default",
    translation jsonb,
    CONSTRAINT pk_inventory PRIMARY KEY (id),
    CONSTRAINT fk_inventory_documents FOREIGN KEY (id)
        REFERENCES documents (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_inventory_warehouse FOREIGN KEY (warehouse_id)
        REFERENCES warehouse (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS inventory_parts
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    sort_order double precision,
    quantity numeric,
    total_currency character varying(3) COLLATE pg_catalog."default",
    total_amount numeric,
    base_total_currency character varying(3) COLLATE pg_catalog."default",
    base_total_amount numeric,
    warehouse_id bigint NOT NULL,
    tag character varying(255) COLLATE pg_catalog."default",
    finished boolean DEFAULT false,
    cost_total_currency character varying(3) COLLATE pg_catalog."default",
    cost_total_amount numeric,
    cost_info jsonb,
    change boolean DEFAULT false,
    quantity_initial numeric,
    cost_initial_currency character varying(3) COLLATE pg_catalog."default",
    cost_initial_amount numeric,
    quantity_remainder numeric,
    cost_remainder_currency character varying(3) COLLATE pg_catalog."default",
    cost_remainder_amount numeric,
    sn_sn character varying(255) COLLATE pg_catalog."default",
    sn_cf jsonb,
    uuid uuid,
    parent_id bigint NOT NULL,
    part_id bigint NOT NULL,
    doc_part jsonb,
    CONSTRAINT pk_inventory_parts PRIMARY KEY (id),
    CONSTRAINT fk_inventory_history_warehouse FOREIGN KEY (warehouse_id)
        REFERENCES warehouse (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_parent_inventory_parts_inventory FOREIGN KEY (parent_id)
        REFERENCES inventory (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT fk_part_inventory_parts_parts FOREIGN KEY (part_id)
        REFERENCES parts (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS inventory_ob
(
    id bigint NOT NULL,
    warehouse_id bigint NOT NULL,
    tag character varying(255) COLLATE pg_catalog."default",
    finished_parts boolean DEFAULT false,
    CONSTRAINT pk_inventory_ob PRIMARY KEY (id),
    CONSTRAINT fk_inventory_ob_documents FOREIGN KEY (id)
        REFERENCES documents (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_inventory_ob_warehouse FOREIGN KEY (warehouse_id)
        REFERENCES warehouse (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS inventory_ob_parts
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    sort_order double precision,
    quantity numeric,
    total_currency character varying(3) COLLATE pg_catalog."default",
    total_amount numeric,
    base_total_currency character varying(3) COLLATE pg_catalog."default",
    base_total_amount numeric,
    warehouse_id bigint,
    tag character varying(255) COLLATE pg_catalog."default",
    finished boolean DEFAULT false,
    cost_total_currency character varying(3) COLLATE pg_catalog."default",
    cost_total_amount numeric,
    cost_info jsonb,
    exchange_base character varying(3) COLLATE pg_catalog."default",
    exchange_base_amount numeric,
    exchange_currency character varying(3) COLLATE pg_catalog."default",
    exchange_amount numeric,
    exchange_date date,
    sn_sn character varying(255) COLLATE pg_catalog."default",
    sn_cf jsonb,
    uuid uuid,
    parent_id bigint NOT NULL,
    part_id bigint NOT NULL,
    doc_part jsonb,
    CONSTRAINT pk_inventory_ob_parts PRIMARY KEY (id),
    CONSTRAINT fk_inventory_parts_warehouse FOREIGN KEY (warehouse_id)
        REFERENCES warehouse (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_parent_inventory_ob_parts_inventory_ob FOREIGN KEY (parent_id)
        REFERENCES inventory_ob (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT fk_part_inventory_ob_parts_parts FOREIGN KEY (part_id)
        REFERENCES parts (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS "order"
(
    id bigint NOT NULL,
    contact_name character varying(255) COLLATE pg_catalog."default",
    contact_contacts jsonb,
    discount double precision,
    subtotal_currency character varying(3) COLLATE pg_catalog."default",
    subtotal_amount numeric,
    tax_total_currency character varying(3) COLLATE pg_catalog."default",
    tax_total_amount numeric,
    total_currency character varying(3) COLLATE pg_catalog."default",
    total_amount numeric,
    base_subtotal_currency character varying(3) COLLATE pg_catalog."default",
    base_subtotal_amount numeric,
    base_tax_total_currency character varying(3) COLLATE pg_catalog."default",
    base_tax_total_amount numeric,
    base_total_currency character varying(3) COLLATE pg_catalog."default",
    base_total_amount numeric,
    prepayment_currency character varying(3) COLLATE pg_catalog."default",
    prepayment_amount numeric,
    current_subtotal_currency character varying(3) COLLATE pg_catalog."default",
    current_subtotal_amount numeric,
    current_tax_total_currency character varying(3) COLLATE pg_catalog."default",
    current_tax_total_amount numeric,
    current_total_currency character varying(3) COLLATE pg_catalog."default",
    current_total_amount numeric,
    warehouse_id bigint NOT NULL,
    tag character varying(255) COLLATE pg_catalog."default",
    due_date date,
    docs jsonb,
    zero_vat boolean DEFAULT false,
    CONSTRAINT pk_order PRIMARY KEY (id),
    CONSTRAINT fk_order_documents FOREIGN KEY (id)
        REFERENCES documents (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_order_warehouse FOREIGN KEY (warehouse_id)
        REFERENCES warehouse (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS order_parts
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    sort_order double precision,
    quantity numeric,
    estimate numeric,
    remainder numeric,
    total_currency character varying(3) COLLATE pg_catalog."default",
    total_amount numeric,
    base_total_currency character varying(3) COLLATE pg_catalog."default",
    base_total_amount numeric,
    vendor_code character varying(255) COLLATE pg_catalog."default",
    taxable boolean DEFAULT false,
    vat_rate_code character varying(255) COLLATE pg_catalog."default",
    vat_code character varying(255) COLLATE pg_catalog."default",
    vat_name character varying(255) COLLATE pg_catalog."default",
    vat_rate double precision,
    current_total_currency character varying(3) COLLATE pg_catalog."default",
    current_total_amount numeric,
    current_tax_total_currency character varying(3) COLLATE pg_catalog."default",
    current_tax_total_amount numeric,
    sn_sn character varying(255) COLLATE pg_catalog."default",
    sn_cf jsonb,
    uuid uuid,
    doc_part jsonb,
    parent_id bigint NOT NULL,
    part_id bigint NOT NULL,
    CONSTRAINT pk_order_parts PRIMARY KEY (id),
    CONSTRAINT fk_parent_order_parts_order FOREIGN KEY (parent_id)
        REFERENCES "order" (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT fk_part_order_parts_parts FOREIGN KEY (part_id)
        REFERENCES parts (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS trans_prod
(
    id bigint NOT NULL,
    warehouse_from_id bigint NOT NULL,
    tag_from character varying(255) COLLATE pg_catalog."default",
    warehouse_reserved_id bigint,
    tag_reserved character varying(255) COLLATE pg_catalog."default",
    warehouse_to_id bigint NOT NULL,
    tag_to character varying(255) COLLATE pg_catalog."default",
    finished_parts_from boolean DEFAULT false,
    finished_parts_to boolean DEFAULT false,
    reserved boolean DEFAULT false,
    reserved_parts boolean DEFAULT false,
    recipe jsonb,
    driver character varying(255) COLLATE pg_catalog."default",
    load_address_address1 character varying(255) COLLATE pg_catalog."default",
    load_address_address2 character varying(255) COLLATE pg_catalog."default",
    load_address_address3 character varying(255) COLLATE pg_catalog."default",
    load_address_city character varying(255) COLLATE pg_catalog."default",
    load_address_country character varying(255) COLLATE pg_catalog."default",
    load_address_municipality character varying(255) COLLATE pg_catalog."default",
    load_address_name character varying(255) COLLATE pg_catalog."default",
    load_address_zip character varying(255) COLLATE pg_catalog."default",
    unload_address_address1 character varying(255) COLLATE pg_catalog."default",
    unload_address_address2 character varying(255) COLLATE pg_catalog."default",
    unload_address_address3 character varying(255) COLLATE pg_catalog."default",
    unload_address_city character varying(255) COLLATE pg_catalog."default",
    unload_address_country character varying(255) COLLATE pg_catalog."default",
    unload_address_municipality character varying(255) COLLATE pg_catalog."default",
    unload_address_name character varying(255) COLLATE pg_catalog."default",
    unload_address_zip character varying(255) COLLATE pg_catalog."default",
    transport_id character varying(255) COLLATE pg_catalog."default",
    transport_marque character varying(255) COLLATE pg_catalog."default",
    docs jsonb,
    CONSTRAINT pk_trans_prod PRIMARY KEY (id),
    CONSTRAINT fk_trans_prod_documents FOREIGN KEY (id)
        REFERENCES documents (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_trans_prod_warehouse_from FOREIGN KEY (warehouse_from_id)
        REFERENCES warehouse (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_trans_prod_warehouse_reserved FOREIGN KEY (warehouse_reserved_id)
        REFERENCES warehouse (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_trans_prod_warehouse_to FOREIGN KEY (warehouse_to_id)
        REFERENCES warehouse (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS trans_prod_parts
(
    disc character varying(4) COLLATE pg_catalog."default",
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    sort_order double precision,
    quantity numeric,
    total_currency character varying(3) COLLATE pg_catalog."default",
    total_amount numeric,
    base_total_currency character varying(3) COLLATE pg_catalog."default",
    base_total_amount numeric,
    warehouse_id bigint,
    tag character varying(255) COLLATE pg_catalog."default",
    cost_total_currency character varying(3) COLLATE pg_catalog."default",
    cost_total_amount numeric,
    finished boolean DEFAULT false,
    cost_info jsonb,
    sn_sn character varying(255) COLLATE pg_catalog."default",
    sn_cf jsonb,
    uuid uuid,
    doc_part jsonb,
    parent_id bigint NOT NULL,
    part_id bigint NOT NULL,
    remainder numeric,
    reserved boolean DEFAULT false,
    reserved_quantity numeric,
    cost_percent numeric,
    CONSTRAINT pk_trans_prod_parts PRIMARY KEY (id),
    CONSTRAINT fk_parent_trans_prod_parts_trans_prod FOREIGN KEY (parent_id)
        REFERENCES trans_prod (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT fk_part_trans_prod_parts_parts FOREIGN KEY (part_id)
        REFERENCES parts (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_trans_prod_parts_warehouse FOREIGN KEY (warehouse_id)
        REFERENCES warehouse (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS recipe
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    name character varying(255) COLLATE pg_catalog."default",
    description character varying(255) COLLATE pg_catalog."default",
    quantity numeric,
    CONSTRAINT pk_recipe PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS recipe_parts
(
    disc character varying(4) COLLATE pg_catalog."default",
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    sort_order double precision,
    quantity numeric,
    total_currency character varying(3) COLLATE pg_catalog."default",
    total_amount numeric,
    base_total_currency character varying(3) COLLATE pg_catalog."default",
    base_total_amount numeric,
    warehouse_id bigint,
    tag character varying(255) COLLATE pg_catalog."default",
    cost_total_currency character varying(3) COLLATE pg_catalog."default",
    cost_total_amount numeric,
    finished boolean DEFAULT false,
    cost_info jsonb,
    sn_sn character varying(255) COLLATE pg_catalog."default",
    sn_cf jsonb,
    uuid uuid,
    doc_part jsonb,
    parent_id bigint NOT NULL,
    part_id bigint NOT NULL,
    remainder numeric,
    reserved boolean DEFAULT false,
    reserved_quantity numeric,
    cost_percent numeric,
    CONSTRAINT pk_recipe_parts_from PRIMARY KEY (id),
    CONSTRAINT fk_parent_recipe_parts_recipe FOREIGN KEY (parent_id)
        REFERENCES recipe (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT fk_part_recipe_parts_parts FOREIGN KEY (part_id)
        REFERENCES parts (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_recipe_parts_warehouse FOREIGN KEY (warehouse_id)
        REFERENCES warehouse (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS purchase
(
    id bigint NOT NULL,
    finished_debt boolean DEFAULT false,
    debt_type character varying(255) COLLATE pg_catalog."default",
    subtotal_currency character varying(3) COLLATE pg_catalog."default",
    subtotal_amount numeric,
    tax_total_currency character varying(3) COLLATE pg_catalog."default",
    tax_total_amount numeric,
    total_currency character varying(3) COLLATE pg_catalog."default",
    total_amount numeric,
    base_subtotal_currency character varying(3) COLLATE pg_catalog."default",
    base_subtotal_amount numeric,
    base_tax_total_currency character varying(3) COLLATE pg_catalog."default",
    base_tax_total_amount numeric,
    base_total_currency character varying(3) COLLATE pg_catalog."default",
    base_total_amount numeric,
    warehouse_id bigint NOT NULL,
    tag character varying(255) COLLATE pg_catalog."default",
    finished_parts boolean DEFAULT false,
    due_date date,
    expenses jsonb,
    vat_code_totals jsonb,
    isaf_invoice_type character varying(255) COLLATE pg_catalog."default",
    isaf_special_taxation boolean DEFAULT false,
    reverse_vat boolean DEFAULT false,
    order_id bigint,
    order_number character varying(255) COLLATE pg_catalog."default",
    order_series character varying(255) COLLATE pg_catalog."default",
    order_ordinal bigint,
    order_date date,
    order_due_date date,
    order_note character varying(4096) COLLATE pg_catalog."default",
    order_type character varying(255) COLLATE pg_catalog."default",
    order_uuid uuid,
    order_employee_id bigint,
    order_employee_name character varying(255) COLLATE pg_catalog."default",
    order_employee_db character varying(255) COLLATE pg_catalog."default",
    order_db character varying(255) COLLATE pg_catalog."default",
    reg_date date,
    advance boolean DEFAULT false,
    CONSTRAINT pk_purchase PRIMARY KEY (id),
    CONSTRAINT fk_purchase_documents FOREIGN KEY (id)
        REFERENCES documents (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_purchase_warehouse FOREIGN KEY (warehouse_id)
        REFERENCES warehouse (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS purchase_parts
(
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    sort_order double precision,
    quantity numeric,
    total_currency character varying(3) COLLATE pg_catalog."default",
    total_amount numeric,
    base_total_currency character varying(3) COLLATE pg_catalog."default",
    base_total_amount numeric,
    warehouse_id bigint,
    tag character varying(255) COLLATE pg_catalog."default",
    finished boolean DEFAULT false,
    cost_total_currency character varying(3) COLLATE pg_catalog."default",
    cost_total_amount numeric,
    in_cost boolean DEFAULT false,
    add_exp boolean DEFAULT false,
    expense_currency character varying(3) COLLATE pg_catalog."default",
    expense_amount numeric,
    tax_total_currency character varying(3) COLLATE pg_catalog."default",
    tax_total_amount numeric,
    vendor_code character varying(255) COLLATE pg_catalog."default",
    doc_return_id bigint,
    doc_return_number character varying(255) COLLATE pg_catalog."default",
    doc_return_series character varying(255) COLLATE pg_catalog."default",
    doc_return_ordinal bigint,
    doc_return_date date,
    doc_return_due_date date,
    doc_return_note character varying(4096) COLLATE pg_catalog."default",
    doc_return_type character varying(255) COLLATE pg_catalog."default",
    doc_return_uuid uuid,
    doc_return_employee_id bigint,
    doc_return_employee_name character varying(255) COLLATE pg_catalog."default",
    doc_return_employee_db character varying(255) COLLATE pg_catalog."default",
    doc_return_db character varying(255) COLLATE pg_catalog."default",
    cost_info jsonb,
    sn_sn character varying(255) COLLATE pg_catalog."default",
    sn_cf jsonb,
    uuid uuid,
    doc_part jsonb,
    parent_id bigint NOT NULL,
    part_id bigint NOT NULL,
    CONSTRAINT pk_purchase_parts PRIMARY KEY (id),
    CONSTRAINT fk_parent_purchase_parts_purchase FOREIGN KEY (parent_id)
        REFERENCES purchase (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT fk_part_purchase_parts_parts FOREIGN KEY (part_id)
        REFERENCES parts (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_purchase_parts_warehouse FOREIGN KEY (warehouse_id)
        REFERENCES warehouse (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS invoice
(
    id bigint NOT NULL,
    finished_debt boolean DEFAULT false,
    debt_type character varying(255) COLLATE pg_catalog."default",
    location_address1 character varying(255) COLLATE pg_catalog."default",
    location_address2 character varying(255) COLLATE pg_catalog."default",
    location_address3 character varying(255) COLLATE pg_catalog."default",
    location_city character varying(255) COLLATE pg_catalog."default",
    location_country character varying(255) COLLATE pg_catalog."default",
    location_municipality character varying(255) COLLATE pg_catalog."default",
    location_name character varying(255) COLLATE pg_catalog."default",
    location_zip character varying(255) COLLATE pg_catalog."default",
    contact_name character varying(255) COLLATE pg_catalog."default",
    contact_contacts jsonb,
    parts_total_currency character varying(3) COLLATE pg_catalog."default",
    parts_total_amount numeric,
    discount double precision,
    discount_total_currency character varying(3) COLLATE pg_catalog."default",
    discount_total_amount numeric,
    subtotal_currency character varying(3) COLLATE pg_catalog."default",
    subtotal_amount numeric,
    tax_total_currency character varying(3) COLLATE pg_catalog."default",
    tax_total_amount numeric,
    total_currency character varying(3) COLLATE pg_catalog."default",
    total_amount numeric,
    base_subtotal_currency character varying(3) COLLATE pg_catalog."default",
    base_subtotal_amount numeric,
    base_tax_total_currency character varying(3) COLLATE pg_catalog."default",
    base_tax_total_amount numeric,
    base_total_currency character varying(3) COLLATE pg_catalog."default",
    base_total_amount numeric,
    cost_total_currency character varying(3) COLLATE pg_catalog."default",
    cost_total_amount numeric,
    warehouse_id bigint NOT NULL,
    tag character varying(255) COLLATE pg_catalog."default",
    finished_parts boolean DEFAULT false,
    due_date date,
    according character varying(255) COLLATE pg_catalog."default",
    account_id bigint,
    zero_vat boolean DEFAULT false,
    driver character varying(255) COLLATE pg_catalog."default",
    load_address_address1 character varying(255) COLLATE pg_catalog."default",
    load_address_address2 character varying(255) COLLATE pg_catalog."default",
    load_address_address3 character varying(255) COLLATE pg_catalog."default",
    load_address_city character varying(255) COLLATE pg_catalog."default",
    load_address_country character varying(255) COLLATE pg_catalog."default",
    load_address_municipality character varying(255) COLLATE pg_catalog."default",
    load_address_name character varying(255) COLLATE pg_catalog."default",
    load_address_zip character varying(255) COLLATE pg_catalog."default",
    unload_address_address1 character varying(255) COLLATE pg_catalog."default",
    unload_address_address2 character varying(255) COLLATE pg_catalog."default",
    unload_address_address3 character varying(255) COLLATE pg_catalog."default",
    unload_address_city character varying(255) COLLATE pg_catalog."default",
    unload_address_country character varying(255) COLLATE pg_catalog."default",
    unload_address_municipality character varying(255) COLLATE pg_catalog."default",
    unload_address_name character varying(255) COLLATE pg_catalog."default",
    unload_address_zip character varying(255) COLLATE pg_catalog."default",
    transport_id character varying(255) COLLATE pg_catalog."default",
    transport_marque character varying(255) COLLATE pg_catalog."default",
    vat_code_totals jsonb,
    isaf_invoice_type character varying(255) COLLATE pg_catalog."default",
    isaf_special_taxation boolean DEFAULT false,
    invoice_note character varying(500) COLLATE pg_catalog."default",
    ecr boolean DEFAULT false,
    ecr_no character varying(255) COLLATE pg_catalog."default",
    ecr_receipt character varying(255) COLLATE pg_catalog."default",
    packing jsonb,
    reverse_vat boolean DEFAULT false,
    payment_id character varying(255) COLLATE pg_catalog."default",
    translation jsonb,
    advance boolean DEFAULT false,
    credit_invoice boolean DEFAULT false,
    tax_free jsonb,
    CONSTRAINT pk_invoice PRIMARY KEY (id),
    CONSTRAINT fk_invoice_bank_accounts FOREIGN KEY (account_id)
        REFERENCES bank_accounts (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_invoice_documents FOREIGN KEY (id)
        REFERENCES documents (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_invoice_warehouse FOREIGN KEY (warehouse_id)
        REFERENCES warehouse (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS invoice_parts
(
    disc character varying(4) COLLATE pg_catalog."default",
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    sort_order double precision,
    quantity numeric,
    total_currency character varying(3) COLLATE pg_catalog."default",
    total_amount numeric,
    base_total_currency character varying(3) COLLATE pg_catalog."default",
    base_total_amount numeric,
    warehouse_id bigint,
    tag character varying(255) COLLATE pg_catalog."default",
    finished boolean DEFAULT false,
    remainder numeric,
    cost_total_currency character varying(3) COLLATE pg_catalog."default",
    cost_total_amount numeric,
    cost_info jsonb,
    sn_sn character varying(255) COLLATE pg_catalog."default",
    sn_cf jsonb,
    uuid uuid,
    doc_part jsonb,
    parent_id bigint NOT NULL,
    part_id bigint NOT NULL,
    taxable boolean DEFAULT false,
    fix_total boolean DEFAULT false,
    vat_rate_code character varying(255) COLLATE pg_catalog."default",
    vat_code character varying(255) COLLATE pg_catalog."default",
    vat_name character varying(255) COLLATE pg_catalog."default",
    vat_rate double precision,
    discount double precision,
    discount_doc double precision,
    discounted_price_currency character varying(3) COLLATE pg_catalog."default",
    discounted_price_amount numeric,
    discounted_total_currency character varying(3) COLLATE pg_catalog."default",
    discounted_total_amount numeric,
    discount_doc_total_currency character varying(3) COLLATE pg_catalog."default",
    discount_doc_total_amount numeric,
    no_print boolean DEFAULT false,
    doc_return_id bigint,
    doc_return_number character varying(255) COLLATE pg_catalog."default",
    doc_return_series character varying(255) COLLATE pg_catalog."default",
    doc_return_ordinal bigint,
    doc_return_date date,
    doc_return_due_date date,
    doc_return_note character varying(4096) COLLATE pg_catalog."default",
    doc_return_type character varying(255) COLLATE pg_catalog."default",
    doc_return_uuid uuid,
    doc_return_employee_id bigint,
    doc_return_employee_name character varying(255) COLLATE pg_catalog."default",
    doc_return_employee_db character varying(255) COLLATE pg_catalog."default",
    doc_return_db character varying(255) COLLATE pg_catalog."default",
    link_uuid uuid,
    parent_link_uuid uuid,
    CONSTRAINT pk_invoice_parts PRIMARY KEY (id),
    CONSTRAINT fk_invoice_parts_warehouse FOREIGN KEY (warehouse_id)
        REFERENCES warehouse (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_parent_invoice_parts_invoice FOREIGN KEY (parent_id)
        REFERENCES invoice (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT fk_part_invoice_parts_parts FOREIGN KEY (part_id)
        REFERENCES parts (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS estimate
(
    id bigint NOT NULL,
    type character varying(255) COLLATE pg_catalog."default" NOT NULL,
    location_address1 character varying(255) COLLATE pg_catalog."default",
    location_address2 character varying(255) COLLATE pg_catalog."default",
    location_address3 character varying(255) COLLATE pg_catalog."default",
    location_city character varying(255) COLLATE pg_catalog."default",
    location_country character varying(255) COLLATE pg_catalog."default",
    location_municipality character varying(255) COLLATE pg_catalog."default",
    location_name character varying(255) COLLATE pg_catalog."default",
    location_zip character varying(255) COLLATE pg_catalog."default",
    contact_name character varying(255) COLLATE pg_catalog."default",
    contact_contacts jsonb,
    discount double precision,
    subtotal_currency character varying(3) COLLATE pg_catalog."default",
    subtotal_amount numeric,
    tax_total_currency character varying(3) COLLATE pg_catalog."default",
    tax_total_amount numeric,
    total_currency character varying(3) COLLATE pg_catalog."default",
    total_amount numeric,
    base_subtotal_currency character varying(3) COLLATE pg_catalog."default",
    base_subtotal_amount numeric,
    base_tax_total_currency character varying(3) COLLATE pg_catalog."default",
    base_tax_total_amount numeric,
    base_total_currency character varying(3) COLLATE pg_catalog."default",
    base_total_amount numeric,
    prepayment_currency character varying(3) COLLATE pg_catalog."default",
    prepayment_amount numeric,
    warehouse_id bigint NOT NULL,
    tag character varying(255) COLLATE pg_catalog."default",
    due_date date,
    period character varying(255) COLLATE pg_catalog."default",
    step integer,
    start_date date,
    end_date date,
    docs jsonb,
    next_date date,
    account_id bigint,
    zero_vat boolean DEFAULT false,
    estimate_note character varying(500) COLLATE pg_catalog."default",
    reverse_vat boolean DEFAULT false,
    CONSTRAINT pk_estimate PRIMARY KEY (id),
    CONSTRAINT fk_estimate_bank_accounts FOREIGN KEY (account_id)
        REFERENCES bank_accounts (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_estimate_documents FOREIGN KEY (id)
        REFERENCES documents (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_estimate_warehouse FOREIGN KEY (warehouse_id)
        REFERENCES warehouse (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS estimate_parts
(
    disc character varying(4) COLLATE pg_catalog."default",
    id bigint NOT NULL,
    version bigint NOT NULL,
    archive boolean DEFAULT false,
    created_by character varying(255) COLLATE pg_catalog."default",
    created_on timestamp without time zone,
    updated_by character varying(255) COLLATE pg_catalog."default",
    updated_on timestamp without time zone,
    hidden boolean DEFAULT false,
    foreign_id bigint,
    company_id bigint NOT NULL,
    export_id character varying(255) COLLATE pg_catalog."default",
    labels jsonb,
    sort_order double precision,
    quantity numeric,
    total_currency character varying(3) COLLATE pg_catalog."default",
    total_amount numeric,
    base_total_currency character varying(3) COLLATE pg_catalog."default",
    base_total_amount numeric,
    warehouse_id bigint,
    tag character varying(255) COLLATE pg_catalog."default",
    finished boolean DEFAULT false,
    remainder numeric,
    sn_sn character varying(255) COLLATE pg_catalog."default",
    sn_cf jsonb,
    uuid uuid,
    doc_part jsonb,
    parent_id bigint NOT NULL,
    part_id bigint NOT NULL,
    estimate numeric,
    taxable boolean DEFAULT false,
    vat_rate_code character varying(255) COLLATE pg_catalog."default",
    vat_code character varying(255) COLLATE pg_catalog."default",
    vat_name character varying(255) COLLATE pg_catalog."default",
    vat_rate double precision,
    discount double precision,
    discount_doc double precision,
    discounted_price_currency character varying(3) COLLATE pg_catalog."default",
    discounted_price_amount numeric,
    link_uuid uuid,
    parent_link_uuid uuid,
    CONSTRAINT pk_estimate_parts PRIMARY KEY (id),
    CONSTRAINT fk_estimate_parts_warehouse FOREIGN KEY (warehouse_id)
        REFERENCES warehouse (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_parent_estimate_parts_estimate FOREIGN KEY (parent_id)
        REFERENCES estimate (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT fk_part_estimate_parts_parts FOREIGN KEY (part_id)
        REFERENCES parts (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

-- v6.1 2024-01-17
CREATE INDEX IF NOT EXISTS idx_invoice_parts_part_id ON invoice_parts(part_id);
CREATE INDEX IF NOT EXISTS idx_purchase_parts_part_id ON purchase_parts(part_id);
CREATE INDEX IF NOT EXISTS idx_trans_prod_parts_part_id ON trans_prod_parts(part_id);
CREATE INDEX IF NOT EXISTS idx_inventory_ob_parts_part_id ON inventory_ob_parts(part_id);
CREATE INDEX IF NOT EXISTS idx_inventory_parts_part_id ON inventory_parts(part_id);
CREATE INDEX IF NOT EXISTS idx_estimate_parts_part_id ON estimate_parts(part_id);
CREATE INDEX IF NOT EXISTS idx_order_parts_part_id ON order_parts(part_id);

CREATE INDEX IF NOT EXISTS idx_inventory_history_part_id ON inventory_history(part_id);

CREATE INDEX IF NOT EXISTS idx_debt_coverage_counterparty_id ON debt_coverage(counterparty_id);
CREATE INDEX IF NOT EXISTS idx_debt_now_counterparty_id ON debt_now(counterparty_id);

CREATE INDEX IF NOT EXISTS idx_part_parts_parent_id ON part_parts(parent_id);

CREATE INDEX IF NOT EXISTS idx_invoice_parts_parent_id ON invoice_parts(parent_id);
CREATE INDEX IF NOT EXISTS idx_purchase_parts_parent_id ON purchase_parts(parent_id);
CREATE INDEX IF NOT EXISTS idx_trans_prod_parts_parent_id ON trans_prod_parts(parent_id);
CREATE INDEX IF NOT EXISTS idx_inventory_ob_parts_parent_id ON inventory_ob_parts(parent_id);
CREATE INDEX IF NOT EXISTS idx_inventory_parts_parent_id ON inventory_parts(parent_id);
CREATE INDEX IF NOT EXISTS idx_estimate_parts_parent_id ON estimate_parts(parent_id);
CREATE INDEX IF NOT EXISTS idx_order_parts_parent_id ON order_parts(parent_id);
