CREATE TABLE public."countries"
(
    id bigint NOT NULL GENERATED ALWAYS AS IDENTITY,
    json_id bigint,
    version bigint,
    uuid uuid DEFAULT gen_random_uuid(),
    status boolean NOT NULL DEFAULT true,
    name character varying NOT NULL,
    description character varying,
    iso2 character varying,
    iso3 character varying,
    numeric_code integer,
    phone_code character varying,
    capital character varying,
    tld character varying,
    native_name character varying,
    longitude double precision,
    latitude double precision,
    emoji character varying,
    emoji_u character varying,
    currency_uuid uuid,
    region_uuid uuid,
    sub_region_uuid uuid,
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
    deletable boolean DEFAULT false,
    archived boolean DEFAULT false,
    PRIMARY KEY (id)
);
