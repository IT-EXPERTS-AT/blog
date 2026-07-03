"""Agent tools: read the source Postgres and write to memory.

Source access and memory writes are tools the model calls, not fixed pipeline
steps. Both stacks (Prefect and Dify) read the same Postgres-seeded data.
"""

import psycopg

from langchain_core.tools import tool

from agents.bi_agent.config import SOURCES_DSN
from agents.bi_agent.memory import append_lesson


@tool
def list_sources() -> str:
    """List the data sources (tables) available in the source database, including their columns."""
    with psycopg.connect(SOURCES_DSN) as db:
        rows = db.execute("""
            select table_name, column_name, data_type
            from information_schema.columns
            where table_schema = 'public'
            order by table_name, ordinal_position
        """).fetchall()
    tables: dict[str, list[str]] = {}
    for table, col, dtype in rows:
        tables.setdefault(table, []).append(f"{col} {dtype}")
    return "\n".join(f"{t}({', '.join(cols)})" for t, cols in tables.items())


@tool
def query_source(sql: str) -> str:
    """Run a single read-only SELECT against the source database.

    Returns rows as text. Only SELECT statements are allowed.
    """
    if not sql.lstrip().lower().startswith("select"):
        return "Only SELECT queries are allowed."
    try:
        with psycopg.connect(SOURCES_DSN) as db:
            rows = db.execute(sql).fetchall()
    except psycopg.Error as e:
        return f"SQL error: {e}"
    return "\n".join(str(row) for row in rows) or "(no rows)"


@tool
def remember_lesson(lesson: str) -> str:
    """Save one short, reusable lesson (1-3 sentences) for future runs.

    Use ONLY for a genuine insight worth keeping. Do not save anything for a
    clean, unremarkable run.
    """
    append_lesson(lesson)
    return "Lesson saved."


SOURCE_TOOLS = [list_sources, query_source]
