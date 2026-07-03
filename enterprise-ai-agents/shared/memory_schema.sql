-- Memory blueprint for the agent.
--
-- Both stacks (Dify and Prefect) instantiate this SAME schema into their OWN
-- separate Postgres instance. The two databases are never shared.
--
-- A "lesson" is a short, reusable insight the agent chose to remember:
-- a recurring anomaly, a data-quality caveat, something worth knowing next run.

create table if not exists lessons (
    id         serial primary key,
    lesson     text not null,
    created_at timestamptz not null default now()
);
