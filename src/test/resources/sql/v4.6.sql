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
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_table_access_method = heap;

CREATE SCHEMA IF NOT EXISTS public
    AUTHORIZATION postgres;

GRANT ALL ON SCHEMA public TO PUBLIC;

--
-- TOC entry 200 (class 1259 OID 16395)
-- Name: documents; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.documents (
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

CREATE TABLE public.double_entries (
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
    parent_counterparty_id bigint --v4.4 2022.02.22
);


--
-- TOC entry 202 (class 1259 OID 16407)
-- Name: gama_sequence; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.gama_sequence
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 203 (class 1259 OID 16409)
-- Name: gl_accounts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gl_accounts (
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
    foreign_id bigint --v3.0 2021.03.24
);


--
-- TOC entry 204 (class 1259 OID 16415)
-- Name: gl_ob_operations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gl_ob_operations (
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

CREATE TABLE public.gl_opening_balances (
    id bigint NOT NULL
);


--
-- TOC entry 212 (class 1259 OID 218782)
-- Name: gl_operations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gl_operations (
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
    sum_currency character varying(3),
    sum_amount numeric,
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

CREATE TABLE public.resp_centers (
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

ALTER TABLE ONLY public.documents
    ADD CONSTRAINT documents_pkey PRIMARY KEY (id);


--
-- TOC entry 3197 (class 2606 OID 16441)
-- Name: gl_accounts gl_accounts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gl_accounts
    ADD CONSTRAINT gl_accounts_pkey PRIMARY KEY (id);


--
-- TOC entry 3200 (class 2606 OID 16443)
-- Name: gl_ob_operations gl_ob_operations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gl_ob_operations
    ADD CONSTRAINT gl_ob_operations_pkey PRIMARY KEY (id);


--
-- TOC entry 3202 (class 2606 OID 16445)
-- Name: gl_opening_balances gl_opening_balances_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gl_opening_balances
    ADD CONSTRAINT gl_opening_balances_pkey PRIMARY KEY (id);


--
-- TOC entry 3218 (class 2606 OID 218777)
-- Name: double_entries pk_double_entries; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.double_entries
    ADD CONSTRAINT pk_double_entries PRIMARY KEY (id);


--
-- TOC entry 3220 (class 2606 OID 218791)
-- Name: gl_operations pk_gl_operations; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gl_operations
    ADD CONSTRAINT pk_gl_operations PRIMARY KEY (id);


--
-- TOC entry 3208 (class 2606 OID 16451)
-- Name: resp_centers resp_centers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.resp_centers
    ADD CONSTRAINT resp_centers_pkey PRIMARY KEY (id);


--
-- TOC entry 3190 (class 1259 OID 16452)
-- Name: idx_documents_company_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_documents_company_id ON public.documents USING btree (company_id);


--
-- TOC entry 3191 (class 1259 OID 16453)
-- Name: idx_documents_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_documents_date ON public.documents USING btree (date);


--
-- TOC entry 3192 (class 1259 OID 16454)
-- Name: idx_documents_number; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_documents_number ON public.documents USING btree (number);


--
-- TOC entry 3193 (class 1259 OID 16455)
-- Name: idx_documents_uuid; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_documents_uuid ON public.documents USING btree (uuid);


--
-- TOC entry 3213 (class 1259 OID 218778)
-- Name: idx_double_entries_company_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_double_entries_company_id ON public.double_entries USING btree (company_id);


--
-- TOC entry 3214 (class 1259 OID 218779)
-- Name: idx_double_entries_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_double_entries_date ON public.double_entries USING btree (date);


--
-- TOC entry 3215 (class 1259 OID 218780)
-- Name: idx_double_entries_ordinal; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_double_entries_ordinal ON public.double_entries USING btree (ordinal);


--
-- TOC entry 3216 (class 1259 OID 218781)
-- Name: idx_double_entries_parent_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_double_entries_parent_id ON public.double_entries USING btree (parent_id);


--
-- TOC entry 3198 (class 1259 OID 181635)
-- Name: idx_gl_accounts_number; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gl_accounts_number ON public.gl_accounts USING btree (company_id, number);


--
-- TOC entry 3205 (class 1259 OID 218800)
-- Name: idx_resp_centers_company_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_resp_centers_company_id ON public.resp_centers USING btree (company_id);


--
-- TOC entry 3206 (class 1259 OID 218801)
-- Name: idx_resp_centers_name; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_resp_centers_name ON public.resp_centers USING btree (name);


--
-- TOC entry 3223 (class 2606 OID 16466)
-- Name: gl_opening_balances FKpbxqeyipeu2hu2rug6xu4yt6e; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gl_opening_balances
    ADD CONSTRAINT "FKpbxqeyipeu2hu2rug6xu4yt6e" FOREIGN KEY (id) REFERENCES public.documents(id);


--
-- TOC entry 3222 (class 2606 OID 16471)
-- Name: gl_ob_operations FKpmbsicj5v89tjorsve84g9gbe; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gl_ob_operations
    ADD CONSTRAINT "FKpmbsicj5v89tjorsve84g9gbe" FOREIGN KEY (parent_id) REFERENCES public.gl_opening_balances(id) ON DELETE CASCADE;


--
-- TOC entry 3225 (class 2606 OID 218792)
-- Name: gl_operations fk_gl_operations_double_entries; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gl_operations
    ADD CONSTRAINT fk_gl_operations_double_entries FOREIGN KEY (parent_id) REFERENCES public.double_entries(id) ON DELETE CASCADE;


--
-- Test tables
--

CREATE TABLE public.entity_master
(
    id bigint NOT NULL,
    name character varying,
    customer jsonb,
    money jsonb,
    labels jsonb, --2021 05 11
    CONSTRAINT entity_master_pkey PRIMARY KEY (id)
);


CREATE TABLE public.entity_child
(
    id bigint NOT NULL,
    name character varying,
    customer jsonb,
    money jsonb,
    parent_id bigint,
    CONSTRAINT entity_child_pkey PRIMARY KEY (id),
    CONSTRAINT entity_child_master_fkey FOREIGN KEY (parent_id)
            REFERENCES public.entity_master (id) MATCH SIMPLE
            ON UPDATE NO ACTION
            ON DELETE CASCADE
);

-- V.2.2

-- Table: public.assets

-- DROP TABLE public.assets;

CREATE TABLE public.assets
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

CREATE INDEX idx_assets_company_id ON public.assets USING btree (company_id ASC NULLS LAST);

CREATE INDEX idx_assets_date ON public.assets USING btree (date ASC NULLS LAST);

CREATE INDEX idx_assets_last_date ON public.assets USING btree (last_date ASC NULLS LAST);

--2021.03.24

CREATE TABLE public.counterparties
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
    ON public.counterparties USING btree
    (company_id ASC NULLS LAST)
    TABLESPACE pg_default;


--2021.03.30

CREATE TABLE public.debt_coverage
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
    doc_uuid character varying(255) COLLATE pg_catalog."default", --V3.0 2021.04.14
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
    ON public.debt_coverage USING btree
    (company_id ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE INDEX idx_debt_coverage_doc_ordinal --V3.0 2021.10.05
    ON public.debt_coverage USING btree
    (doc_ordinal ASC NULLS LAST)
    TABLESPACE pg_default;


--2021.03.29

CREATE TABLE public.debt_history
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
    doc_uuid character varying(255) COLLATE pg_catalog."default", --V3.0 2021.04.14
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
    CONSTRAINT pk_debt_history PRIMARY KEY (id)
    --CONSTRAINT cnst_debt_history_doc_id_doc_db_counterparty_id_type_exchange_c UNIQUE (doc_id, doc_db, counterparty_id, type, exchange_currency) removed V3.0 2021.06.08
);

CREATE INDEX idx_debt_history_company_id
    ON public.debt_history USING btree
    (company_id ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE INDEX idx_debt_history_counterparty_id
    ON public.debt_history USING btree
    (counterparty_id ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE INDEX idx_debt_history_doc_date
    ON public.debt_history USING btree
    (doc_date ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE INDEX idx_debt_history_doc_ordinal --V3.0 2021.10.05
    ON public.debt_history USING btree
    (doc_ordinal ASC NULLS LAST)
    TABLESPACE pg_default;


--2021.03.26

CREATE TABLE public.debt_now
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
    doc_uuid character varying(255) COLLATE pg_catalog."default", --V3.0 2021.04.14
    doc_employee_id bigint, --V3.0 2021.04.14
    doc_employee_name character varying(255) COLLATE pg_catalog."default", --V3.0 2021.04.14
    doc_employee_db character varying(255) COLLATE pg_catalog."default", --V3.0 2021.08.06
    doc_db character varying(255) COLLATE pg_catalog."default" NOT NULL, --V3.0 2021.06.04
    initial_currency character varying(3) COLLATE pg_catalog."default" NOT NULL, --v3.0 2021.06.04
    initial_amount numeric, --v3.0 2021.03.29
    remainder_currency character varying(3) COLLATE pg_catalog."default",
    remainder_amount numeric, --v3.0 2021.03.29
    due_date date,
    --CONSTRAINT pk_debt_now PRIMARY KEY (document_id, counterparty_id, type, currency, db) removed v3.0 2021.06.01
    doc_note character varying(4096) COLLATE pg_catalog."default", --v4.4 2022.02.22 --v4.6 2022.03.26
    CONSTRAINT pk_debt_now PRIMARY KEY (id),
    CONSTRAINT cnst_debt_now_doc_id_doc_db_counterparty_id_type_initial_curren UNIQUE (doc_id, doc_db, counterparty_id, type, initial_currency) --v3.0 2021.06.04
);

CREATE INDEX idx_debt_now_company_id
    ON public.debt_now USING btree
    (company_id ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE INDEX idx_debt_now_doc_ordinal --V3.0 2021.10.05
    ON public.debt_now USING btree
    (doc_ordinal ASC NULLS LAST)
    TABLESPACE pg_default;


--2021.04.16

CREATE TABLE public.debt_opening_balances
(
    id bigint NOT NULL,
    finished_debt boolean DEFAULT false,
    --counterparties removed V3.0 2021.05.04
    CONSTRAINT pk_debt_opening_balances PRIMARY KEY (id),
    CONSTRAINT fk_debt_opening_balances_documents FOREIGN KEY (id)
        REFERENCES public.documents (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);


--2021.05.10

CREATE TABLE public.debt_ob_counterparties
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
    sum_currency character varying(3) COLLATE pg_catalog."default",
    sum_amount numeric,
    base_sum_currency character varying(3) COLLATE pg_catalog."default",
    base_sum_amount numeric,
    base_fix_sum_currency character varying(3) COLLATE pg_catalog."default",
    base_fix_sum_amount numeric,
    finished boolean DEFAULT false,
    type character varying(255) COLLATE pg_catalog."default",
    sort_order double precision, --v3.0 2021.07.08
    CONSTRAINT pk_debt_ob_counterparties PRIMARY KEY (id)
);

CREATE EXTENSION IF NOT EXISTS unaccent SCHEMA public;

-- v4.0 2022.01.10

CREATE SEQUENCE IF NOT EXISTS public.gama_company_sequence
    INCREMENT 10
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

CREATE TABLE IF NOT EXISTS public.companies
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
    ON public.companies USING btree
    (payer_id ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE TABLE IF NOT EXISTS public.connections
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
    ON public.connections USING btree
    (company_id ASC NULLS LAST, date ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE TABLE IF NOT EXISTS public.accounts
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
    CONSTRAINT pk_accounts PRIMARY KEY (id)
);

-- v4.1 2022.02.09

CREATE TABLE IF NOT EXISTS public.calendar
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

CREATE TABLE IF NOT EXISTS public.calendar_settings
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

CREATE TABLE IF NOT EXISTS public.country_vat_code
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

CREATE TABLE IF NOT EXISTS public.country_vat_note
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

CREATE TABLE IF NOT EXISTS public.country_vat_rate
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

CREATE TABLE IF NOT EXISTS public.country_work_time_code
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

CREATE TABLE IF NOT EXISTS public.exchange_rate
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

CREATE TABLE IF NOT EXISTS public.sync
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

CREATE TABLE IF NOT EXISTS public.system_settings
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

CREATE TABLE IF NOT EXISTS public.import
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

CREATE TABLE IF NOT EXISTS public.counter
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

CREATE TABLE IF NOT EXISTS public.label
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

CREATE TABLE IF NOT EXISTS public.manufacturer
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

--v4.5 2022.03.07

CREATE TABLE IF NOT EXISTS public.debt_corrections
(
    id bigint NOT NULL,
    sum_currency character varying(3) COLLATE pg_catalog."default",
    sum_amount numeric,
    base_sum_currency character varying(3) COLLATE pg_catalog."default",
    base_sum_amount numeric,
    debit character varying(255) COLLATE pg_catalog."default",
    credit character varying(255) COLLATE pg_catalog."default",
    correction boolean,
    finished_debt boolean DEFAULT false,
    CONSTRAINT pk_debt_corrections PRIMARY KEY (id),
    CONSTRAINT fk_debt_corrections_documents FOREIGN KEY (id)
        REFERENCES public.documents (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS public.debt_rate_influences
(
    id bigint NOT NULL,
    finished_debt boolean DEFAULT false,
    CONSTRAINT pk_debt_rate_influences PRIMARY KEY (id),
    CONSTRAINT fk_debt_rate_influences_documents FOREIGN KEY (id)
        REFERENCES public.documents (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS public.debt_rate_influence_counterparties
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
    sum_currency character varying(3) COLLATE pg_catalog."default",
    sum_amount numeric,
    base_sum_currency character varying(3) COLLATE pg_catalog."default",
    base_sum_amount numeric,
    base_fix_sum_currency character varying(3) COLLATE pg_catalog."default",
    base_fix_sum_amount numeric,
    finished boolean DEFAULT false,
    sort_order double precision,
    CONSTRAINT pk_debt_rate_influence_counterparties PRIMARY KEY (id)
);
