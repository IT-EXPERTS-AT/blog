create sequence person_id
    start 1;

create table person
(
    id                       bigint primary key,
    first_name               text   not null,
    last_name                text   not null
);
