"""Run the agent standalone, without Prefect:

    uv run python -m agents.github_issue_agent <owner> <repo> <issue_number>
"""

import argparse
import logging

from agents.github_issue_agent.graph import run_agent

if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO, format="%(message)s")
    parser = argparse.ArgumentParser(description="Review a GitHub issue for handoff readiness.")
    parser.add_argument("owner", help="Repository owner (user or org)")
    parser.add_argument("repo", help="Repository name")
    parser.add_argument("issue_number", type=int, help="Issue number")
    args = parser.parse_args()

    review = run_agent(args.owner, args.repo, args.issue_number)
    print(review)
