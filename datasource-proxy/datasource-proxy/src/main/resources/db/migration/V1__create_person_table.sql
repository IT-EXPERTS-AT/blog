create sequence person_id
    start 1 increment by 50;

create table person
(
    id         bigint primary key,
    first_name text not null,
    last_name  text not null
);
