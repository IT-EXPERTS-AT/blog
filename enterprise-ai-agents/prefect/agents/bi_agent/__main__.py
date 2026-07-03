"""Run the agent standalone, without Prefect:

    uv run python -m agents.bi_agent
"""

from datetime import date

from agents.bi_agent.graph import run_agent

if __name__ == "__main__":
    result = run_agent()
    print(f"Report written to: {result['pdf_path']}")
