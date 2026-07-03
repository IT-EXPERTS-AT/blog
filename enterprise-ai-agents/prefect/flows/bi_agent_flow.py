"""Prefect serving layer for the BI agent.

Thin orchestration: it runs the agent (which is just code), then records the
result as flow-run artifacts. The agent itself does not depend on Prefect.

CLI entry points (see README "Approach B"):
    uv run bi-agent          # run once now, logs stream to your terminal
    uv run bi-agent-serve    # serve the daily schedule
"""

from datetime import date

from prefect import flow, get_run_logger, task
from prefect.artifacts import create_markdown_artifact, create_table_artifact

from agents.bi_agent.graph import run_agent
from agents.bi_agent.render import render_report_as_markdown
from agents.bi_agent.schemas import BIReport


@task
def generate_report(run_id: str) -> dict:
    """Run the LangGraph agent: read sources, analyze, render the PDF."""
    logger = get_run_logger()
    logger.info("Running BI agent for %s", run_id)
    result = run_agent()
    logger.info("Report ready; PDF written to %s", result["pdf_path"])
    return result


@task
def publish_artifacts(report: BIReport, pdf_path: str) -> None:
    """Record the run as flow-run artifacts, visible per run in the UI."""
    logger = get_run_logger()
    create_markdown_artifact(
        key="bi-summary",
        markdown=render_report_as_markdown(report, pdf_path),
        description="Daily BI report",
    )
    create_table_artifact(
        key="bi-key-metrics",
        table=[m.model_dump() for m in report.key_metrics],
        description="Key metrics",
    )
    logger.info("Published 'bi-summary' and 'bi-key-metrics' artifacts")


@flow(name="bi-agent")
def bi_agent_flow() -> str:
    """Run the daily BI agent and publish its report as artifacts."""
    result = generate_report(date.today().isoformat())
    publish_artifacts(result["report"], result["pdf_path"])
    return result["pdf_path"]


def run_once() -> None:
    """Entry point for `bi-agent`: run the flow once and report the PDF path."""
    pdf_path = bi_agent_flow()
    print(f"Report written to: {pdf_path}")


def serve() -> None:
    """Entry point for `bi-agent-serve`: serve the deployment on a daily schedule.

    Needs a self-hosted Prefect server reachable via prefect.toml's [api] url.
    """
    bi_agent_flow.serve(name="bi-agent-daily", cron="0 7 * * *")
