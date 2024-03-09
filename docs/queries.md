~~~roomsql
CREATE TABLE public.tutorials (
	id int8 NOT NULL,
	description varchar(255) NULL,
	published bool NULL,
	title varchar(255) NULL,
	CONSTRAINT tutorials_pkey PRIMARY KEY (id)
);
~~~