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
    note character varying(255),
    number character varying(255),
    ordinal bigint,
    series character varying(255),
    uuid uuid,
    counterparty jsonb,
    employee jsonb,
    exchange jsonb,
    finished boolean DEFAULT false,
    finished_gl boolean DEFAULT false,
    recallable boolean DEFAULT false
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
    parent_counterparty jsonb,
    parent_db character varying(255),
    parent_id bigint,
    parent_number character varying(255),
    parent_type character varying(255),
    total_currency character varying(3),
    total_amount numeric
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
    labels jsonb
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
    account_name character varying(255)
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
    credit_name character varying(255)
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
    parent bigint
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
