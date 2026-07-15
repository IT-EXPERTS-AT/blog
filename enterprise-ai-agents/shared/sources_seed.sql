-- Demo source data shared by both stacks.
--
-- Auto-applied by each stack's sources-db container on first start.
-- Prefect: localhost:5436   Dify: localhost:5435

create table if not exists sales (
    date    text    not null,
    region  text,             -- nullable on purpose: the seed includes a row with no region
    revenue integer not null
);

create table if not exists signups (
    date    text    not null,
    plan    text    not null,
    signups integer not null
);

insert into sales values
    ('2026-06-26', 'EMEA', 41200),
    ('2026-06-26', 'AMER', 38800),
    ('2026-06-26', 'APAC', 22500),
    ('2026-06-27', 'EMEA', 42600),
    ('2026-06-27', 'AMER', 39100),
    ('2026-06-27', 'APAC', 21900),
    ('2026-06-28', 'EMEA', 43050),
    ('2026-06-28', 'AMER', 40250),
    ('2026-06-28', 'APAC',  9800),  -- sharp APAC drop
    ('2026-06-28', NULL,   12700);  -- data-quality issue: sale with no region

insert into signups values
    ('2026-06-26', 'Free',       310),
    ('2026-06-26', 'Pro',         48),
    ('2026-06-26', 'Enterprise',   3),
    ('2026-06-27', 'Free',       295),
    ('2026-06-27', 'Pro',         51),
    ('2026-06-27', 'Enterprise',   4),
    ('2026-06-28', 'Free',       302),
    ('2026-06-28', 'Pro',         49),
    ('2026-06-28', 'Enterprise',  14);  -- Enterprise spike
