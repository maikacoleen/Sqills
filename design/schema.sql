--
-- PostgreSQL database dump
--

-- Dumped from database version 9.4.15
-- Dumped by pg_dump version 11.2

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'SQL_ASCII';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: get_next_smallest(text, text); Type: FUNCTION; Schema: public; Owner: docker
--

CREATE FUNCTION public.get_next_smallest(_table text, _column text) RETURNS bigint
    LANGUAGE plpgsql
    AS $$DECLARE
	ids bigint[];
	minimum bigint = 1;
BEGIN
	EXECUTE FORMAT('SELECT ARRAY_AGG(%I) FROM %I', _column, _table) INTO ids;
	IF ids IS NULL THEN
		RETURN minimum;
	END IF;
	WHILE minimum = ANY(ids) LOOP
		minimum = minimum + 1;
	END LOOP;
	RETURN minimum;
END;$$;


ALTER FUNCTION public.get_next_smallest(_table text, _column text) OWNER TO docker;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: account; Type: TABLE; Schema: public; Owner: docker
--

CREATE TABLE public.account (
    username text NOT NULL,
    password text NOT NULL,
    salt text NOT NULL,
    iteration integer NOT NULL,
    session text
);


ALTER TABLE public.account OWNER TO docker;

--
-- Name: reservation; Type: TABLE; Schema: public; Owner: docker
--

CREATE TABLE public.reservation (
    id bigint DEFAULT public.get_next_smallest('reservation'::text, 'id'::text) NOT NULL,
    event_id text NOT NULL,
    room_id bigint NOT NULL,
    start_time timestamp with time zone NOT NULL,
    end_time timestamp with time zone NOT NULL,
    user_id bigint NOT NULL,
    name text NOT NULL,
    number_of_attendees integer NOT NULL,
    visible boolean NOT NULL
);


ALTER TABLE public.reservation OWNER TO docker;

--
-- Name: room; Type: TABLE; Schema: public; Owner: docker
--

CREATE TABLE public.room (
    id bigint DEFAULT public.get_next_smallest('room'::text, 'id'::text) NOT NULL,
    name text NOT NULL,
    capacity integer NOT NULL
);


ALTER TABLE public.room OWNER TO docker;

--
-- Name: user; Type: TABLE; Schema: public; Owner: docker
--

CREATE TABLE public."user" (
    id bigint DEFAULT public.get_next_smallest('user'::text, 'id'::text) NOT NULL,
    email text NOT NULL,
    first_name text NOT NULL,
    last_name text NOT NULL
);


ALTER TABLE public."user" OWNER TO docker;

--
-- Name: account account_pkey; Type: CONSTRAINT; Schema: public; Owner: docker
--

ALTER TABLE ONLY public.account
    ADD CONSTRAINT account_pkey PRIMARY KEY (username);


--
-- Name: account account_salt_key; Type: CONSTRAINT; Schema: public; Owner: docker
--

ALTER TABLE ONLY public.account
    ADD CONSTRAINT account_salt_key UNIQUE (salt);


--
-- Name: account account_session_key; Type: CONSTRAINT; Schema: public; Owner: docker
--

ALTER TABLE ONLY public.account
    ADD CONSTRAINT account_session_key UNIQUE (session);


--
-- Name: reservation reservation_calendar_id_key; Type: CONSTRAINT; Schema: public; Owner: docker
--

ALTER TABLE ONLY public.reservation
    ADD CONSTRAINT reservation_calendar_id_key UNIQUE (event_id);


--
-- Name: reservation reservation_pkey; Type: CONSTRAINT; Schema: public; Owner: docker
--

ALTER TABLE ONLY public.reservation
    ADD CONSTRAINT reservation_pkey PRIMARY KEY (id);


--
-- Name: room room_new_name_key; Type: CONSTRAINT; Schema: public; Owner: docker
--

ALTER TABLE ONLY public.room
    ADD CONSTRAINT room_new_name_key UNIQUE (name);


--
-- Name: room room_new_pkey; Type: CONSTRAINT; Schema: public; Owner: docker
--

ALTER TABLE ONLY public.room
    ADD CONSTRAINT room_new_pkey PRIMARY KEY (id);


--
-- Name: user user_email_key; Type: CONSTRAINT; Schema: public; Owner: docker
--

ALTER TABLE ONLY public."user"
    ADD CONSTRAINT user_email_key UNIQUE (email);


--
-- Name: user user_pkey; Type: CONSTRAINT; Schema: public; Owner: docker
--

ALTER TABLE ONLY public."user"
    ADD CONSTRAINT user_pkey PRIMARY KEY (id);


--
-- Name: reservation reservation_room_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: docker
--

ALTER TABLE ONLY public.reservation
    ADD CONSTRAINT reservation_room_id_fkey FOREIGN KEY (room_id) REFERENCES public.room(id);


--
-- Name: reservation reservation_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: docker
--

ALTER TABLE ONLY public.reservation
    ADD CONSTRAINT reservation_user_id_fkey FOREIGN KEY (user_id) REFERENCES public."user"(id);


--
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

