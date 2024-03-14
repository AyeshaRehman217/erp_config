CREATE TABLE public.states
(
    id bigint NOT NULL GENERATED ALWAYS AS IDENTITY,
    version bigint,
    uuid uuid DEFAULT gen_random_uuid(),
    status boolean NOT NULL DEFAULT true,
    name character varying,
    description character varying,
    state_code character varying,
    type character varying,
    longitude double precision,
    latitude double precision,
    country_uuid uuid,
    created_by uuid NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_by uuid,
    updated_at timestamp without time zone,
    deleted_by uuid,
    deleted_at timestamp without time zone,
    req_company_uuid uuid,
    req_branch_uuid uuid,
    req_created_browser character varying,
    req_created_ip character varying,
    req_created_port character varying,
    req_created_os character varying,
    req_created_device character varying,
    req_created_referer character varying,
    req_updated_browser character varying,
    req_updated_ip character varying,
    req_updated_port character varying,
    req_updated_os character varying,
    req_updated_device character varying,
    req_updated_referer character varying,
    req_deleted_browser character varying,
    req_deleted_ip character varying,
    req_deleted_port character varying,
    req_deleted_os character varying,
    req_deleted_device character varying,
    req_deleted_referer character varying,
    editable boolean DEFAULT true,
    archived boolean DEFAULT false,
    deletable boolean DEFAULT false,
    PRIMARY KEY (id)
);
