"""Node functions for the BI agent graph — one per agent step."""

from datetime import date

from pydantic import BaseModel, Field

from langchain.agents import create_agent
from langchain_core.runnables import RunnableConfig

from agents.bi_agent.llm import get_llm
from agents.bi_agent.memory import read_lessons
from agents.bi_agent.render import render_report_as_pdf
from agents.bi_agent.prompts import ANALYZE_TASK, SYSTEM_PROMPT, format_lessons
from agents.bi_agent.schemas import BIReport
from agents.bi_agent.tools import SOURCE_TOOLS, remember_lesson


class AgentState(BaseModel):
    lessons: list[str] = Field(default_factory=list)
    report: BIReport | None = None
    pdf_path: str = ""


def load_memory(state: AgentState) -> dict:
    """Step 1: read accumulated lessons to inject into the system prompt."""
    return {"lessons": read_lessons()}


def analyze(state: AgentState, config: RunnableConfig) -> dict:
    """Steps 2, 3 & 5: the model reads sources, returns a structured report, and
    may append a lesson — all through tool calls."""
    system = SYSTEM_PROMPT.format(lessons=format_lessons(state.lessons))
    agent = create_agent(
        get_llm(),
        [*SOURCE_TOOLS, remember_lesson],
        system_prompt=system,
        response_format=BIReport,
    )
    task = ANALYZE_TASK.format(date=date.today().isoformat())
    result = agent.invoke({"messages": [("user", task)]}, config=config)
    return {"report": result["structured_response"]}


def render_pdf(state: AgentState) -> dict:
    """Step 4: render the report as a formatted PDF."""
    return {"pdf_path": render_report_as_pdf(state.report)}
