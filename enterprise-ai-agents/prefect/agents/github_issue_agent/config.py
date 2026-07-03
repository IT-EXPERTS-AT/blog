"""Environment and settings for the GitHub issue review agent.

Loads prefect/.env so the CLI (`uv run github-issue-agent ...`) works without
extra flags. The GitHub token is read from the environment; never hardcode it.
"""

import os
from pathlib import Path

from dotenv import load_dotenv

# prefect/agents/github_issue_agent/config.py -> parents[2] = prefect/
PREFECT_DIR = Path(__file__).resolve().parents[2]

load_dotenv(PREFECT_DIR / ".env")

# GitHub REST API base. Override for GitHub Enterprise (e.g.
# https://github.your-company.com/api/v3).
GITHUB_API_URL = os.environ.get("GITHUB_API_URL", "https://api.github.com").rstrip("/")

# Personal access token (or fine-grained token) with read access to the repos
# under review. Set via env var; the API tools send it as a Bearer token.
GITHUB_TOKEN = os.environ.get("GITHUB_TOKEN", "")
