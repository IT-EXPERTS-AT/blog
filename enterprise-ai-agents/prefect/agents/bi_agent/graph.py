"""The LangGraph graph: load memory -> analyze -> render PDF.

Self-contained and runnable without Prefect (see __main__.py). Opik tracing is
attached in run_agent(); swap OpikTracer for another LangChain callback handler
(e.g. Langfuse) to use a different tracer.
"""

import os
import subprocess
import tempfile
from datetime import datetime
from pathlib import Path

from langgraph.graph import END, StateGraph
from opik.integrations.langchain import OpikTracer

from agents.bi_agent.config import GRAPHS_DIR
from agents.bi_agent.nodes import AgentState, analyze, load_memory, render_pdf


def build_graph():
    """Wire the agent steps into a compiled graph."""
    g = StateGraph(AgentState)
    g.add_node("load_memory", load_memory)
    g.add_node("analyze", analyze)
    g.add_node("render_pdf", render_pdf)
    g.set_entry_point("load_memory")
    g.add_edge("load_memory", "analyze")
    g.add_edge("analyze", "render_pdf")
    g.add_edge("render_pdf", END)
    return g.compile()


def _callbacks() -> list:
    """Attach Opik tracing when OPIK_TRACING_ENABLED=true. Swappable for any tracer."""
    if os.environ.get("OPIK_TRACING_ENABLED", "").lower() != "true":
        return []

    return [OpikTracer(project_name="bi-agent")]


def render_graph_image() -> None:
    """Render the agent graph as a PNG to graphs/ using the Mermaid CLI."""
    GRAPHS_DIR.mkdir(parents=True, exist_ok=True)
    mermaid = build_graph().get_graph().draw_mermaid().replace("graph TD;", "graph LR;")
    out = GRAPHS_DIR / f"bi-agent-graph_{datetime.now():%Y%m%d_%H%M%S}.png"
    with tempfile.NamedTemporaryFile(suffix=".mmd", mode="w", delete=False) as f:
        f.write(mermaid)
        tmp = f.name
    subprocess.run(
        ["npx", "--yes", "@mermaid-js/mermaid-cli", "-i", tmp, "-o", str(out)],
        check=True,
    )
    Path(tmp).unlink(missing_ok=True)
    print(f"Graph written to {out}")


def run_agent() -> dict:
    """Run the BI agent end to end and return the final state."""
    graph = build_graph()
    return graph.invoke({}, config={"callbacks": _callbacks()})
