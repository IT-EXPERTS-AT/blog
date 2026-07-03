"""Read and append lessons in the memory Postgres.

Deliberately simple append/inject memory — not vector retrieval, not full
conversation history. Lessons are read at the start of a run and injected into
the system prompt; a new one is appended only when there's a genuine insight.
"""

import psycopg

from agents.bi_agent.config import MEMORY_DSN


def read_lessons(limit: int = 20) -> list[str]:
    """Return the most recent lessons, oldest first."""
    with psycopg.connect(MEMORY_DSN) as db:
        rows = db.execute(
            "select lesson from lessons order by created_at desc limit %s",
            (limit,),
        ).fetchall()
    return [row[0] for row in reversed(rows)]


def append_lesson(lesson: str) -> None:
    """Append a single lesson to memory."""
    with psycopg.connect(MEMORY_DSN) as db:
        db.execute("insert into lessons (lesson) values (%s)", (lesson,))
