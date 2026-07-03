"""The LangGraph graph: review the issue (and post open questions as a comment).

Self-contained and runnable without Prefect (see __main__.py). Opik tracing is
attached in run_agent() exactly as in the BI agent; swap OpikTracer for another
LangChain callback handler (e.g. Langfuse) to use a different tracer.
"""

import os
import subprocess
import tempfile
from datetime import datetime
from pathlib import Path

from langgraph.graph import END, StateGraph
from opik.integrations.langchain import OpikTracer

from agents.github_issue_agent.config import PREFECT_DIR
from agents.github_issue_agent.nodes import AgentState, review_issue

GRAPHS_DIR = Path(os.environ.get("GRAPHS_DIR", str(PREFECT_DIR / "graphs")))


def build_graph():
    """Wire the agent steps into a compiled graph."""
    g = StateGraph(AgentState)
    g.add_node("review_issue", review_issue)
    g.set_entry_point("review_issue")
    g.add_edge("review_issue", END)
    return g.compile()


def _callbacks() -> list:
    """Attach Opik tracing when OPIK_TRACING_ENABLED=true. Swappable for any tracer."""
    if os.environ.get("OPIK_TRACING_ENABLED", "").lower() != "true":
        return []

    return [OpikTracer(project_name="github-issue-agent")]


def render_graph_image() -> None:
    """Render the agent graph as a PNG to graphs/ using the Mermaid CLI."""
    GRAPHS_DIR.mkdir(parents=True, exist_ok=True)
    mermaid = build_graph().get_graph().draw_mermaid().replace("graph TD;", "graph LR;")
    out = GRAPHS_DIR / f"github-issue-agent-graph_{datetime.now():%Y%m%d_%H%M%S}.png"
    with tempfile.NamedTemporaryFile(suffix=".mmd", mode="w", delete=False) as f:
        f.write(mermaid)
        tmp = f.name
    subprocess.run(
        ["npx", "--yes", "@mermaid-js/mermaid-cli", "-i", tmp, "-o", str(out)],
        check=True,
    )
    Path(tmp).unlink(missing_ok=True)
    print(f"Graph written to {out}")


def run_agent(owner: str, repo: str, issue_number: int) -> str:
    """Run the issue review agent end to end and return its review as text."""
    graph = build_graph()
    state = graph.invoke(
        {"owner": owner, "repo": repo, "issue_number": issue_number},
        config={"callbacks": _callbacks()},
    )
    return state["review"]
