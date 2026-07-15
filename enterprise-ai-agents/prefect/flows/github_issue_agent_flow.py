"""Prefect serving layer for the GitHub issue review agent.

Thin orchestration: it runs the agent (which is just code), then records the
review as a flow-run artifact. The agent itself does not depend on Prefect.

Unlike the BI agent, this flow is NOT scheduled — it is triggered manually and
requires inputs: the repository owner, the repository name, and the issue
number.

CLI entry points (see README "Approach B"):
    uv run github-issue-agent <owner> <repo> <issue_number>   # run once now
    uv run github-issue-agent-serve                           # serve for on-demand runs
"""

import argparse

from prefect import flow, get_run_logger, task
from prefect.artifacts import create_markdown_artifact

from agents.github_issue_agent.graph import run_agent


@task
def review_issue(owner: str, repo: str, issue_number: int) -> str:
    """Run the LangGraph agent: read the issue (and code/comments) and review it."""
    logger = get_run_logger()
    logger.info("Reviewing issue %s/%s#%s", owner, repo, issue_number)
    review = run_agent(owner, repo, issue_number)
    logger.info("Review complete:\n%s", review)
    return review


@task
def publish_artifact(owner: str, repo: str, issue_number: int, review: str) -> None:
    """Record the review as a flow-run artifact, visible per run in the UI."""
    logger = get_run_logger()
    create_markdown_artifact(
        key="issue-review",
        markdown=f"# Issue review — {owner}/{repo}#{issue_number}\n\n{review}",
        description="GitHub issue handoff review",
    )
    logger.info("Published 'issue-review' artifact")


@flow(name="github-issue-review")
def github_issue_review_flow(owner: str, repo: str, issue_number: int) -> str:
    """Review a single GitHub issue and publish the result as an artifact.

    All three inputs are required; there is no schedule — trigger it manually.
    """
    review = review_issue(owner, repo, issue_number)
    publish_artifact(owner, repo, issue_number, review)
    return review


def run_once() -> None:
    """Entry point for `github-issue-agent`: run the flow once for one issue."""
    parser = argparse.ArgumentParser(description="Review a GitHub issue for handoff readiness.")
    parser.add_argument("owner", help="Repository owner (user or org)")
    parser.add_argument("repo", help="Repository name")
    parser.add_argument("issue_number", type=int, help="Issue number")
    args = parser.parse_args()

    review = github_issue_review_flow(args.owner, args.repo, args.issue_number)
    print(review)


def serve() -> None:
    """Entry point for `github-issue-agent-serve`: register the deployment for
    on-demand runs (no schedule).

    Needs a self-hosted Prefect server reachable via prefect.toml's [api] url.
    Trigger a run — supplying the required parameters — from another terminal:

        uv run prefect deployment run 'github-issue-review/github-issue-review-manual' \\
            -p owner=<owner> -p repo=<repo> -p issue_number=<n>
    """
    github_issue_review_flow.serve(name="github-issue-review-manual")
