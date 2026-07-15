"""Environment and resolved paths for the BI agent.

Loads prefect/.env so the CLI (`uv run bi-agent`) works without extra flags.
"""

import os
from pathlib import Path

from dotenv import load_dotenv

# prefect/agents/bi_agent/config.py -> parents[2] = prefect/
PREFECT_DIR = Path(__file__).resolve().parents[2]

load_dotenv(PREFECT_DIR / ".env")

# Read-only source Postgres (the Dockerized sources-db, exposed on localhost).
SOURCES_DSN = os.environ.get(
    "SOURCES_DSN", "postgresql://reader:reader@localhost:5436/sources"
)

# Where PDFs are written.
OUTPUT_DIR = Path(os.environ.get("OUTPUT_DIR", str(PREFECT_DIR / "output")))

# Where graph images are written.
GRAPHS_DIR = Path(os.environ.get("GRAPHS_DIR", str(PREFECT_DIR / "graphs")))

# The agent's memory Postgres (the Dockerized memory-db, exposed on localhost).
MEMORY_DSN = os.environ.get(
    "MEMORY_DSN", "postgresql://agent:agent@localhost:5433/memory"
)
