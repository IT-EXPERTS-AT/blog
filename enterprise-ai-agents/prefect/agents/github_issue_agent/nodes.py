"""Node functions for the GitHub issue review agent graph."""

from pydantic import BaseModel

from langchain.agents import create_agent
from langchain_core.runnables import RunnableConfig

from agents.github_issue_agent.llm import get_llm
from agents.github_issue_agent.prompts import REVIEW_TASK, SYSTEM_PROMPT
from agents.github_issue_agent.tools import GITHUB_TOOLS


class AgentState(BaseModel):
    # Inputs (required): which issue to review.
    owner: str
    repo: str
    issue_number: int
    # Output: the agent's free-text review.
    review: str = ""


def review_issue(state: AgentState, config: RunnableConfig) -> dict:
    """Step 1: the model reads the issue (and code/comments as needed) through
    tool calls and returns its review as text."""
    agent = create_agent(get_llm(), GITHUB_TOOLS, system_prompt=SYSTEM_PROMPT)
    task = REVIEW_TASK.format(
        owner=state.owner, repo=state.repo, issue_number=state.issue_number
    )
    result = agent.invoke({"messages": [("user", task)]}, config=config)
    return {"review": result["messages"][-1].content}
