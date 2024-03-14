CREATE TABLE public."Product"
(
    id bigint NOT NULL GENERATED ALWAYS AS IDENTITY,
    name character varying,
    price character varying,
    CONSTRAINT id PRIMARY KEY (id)
);