"""Azure AI Foundry chat model, isolated for easy swapping.

To use a different provider, return another LangChain chat model here (e.g.
ChatOpenAI or ChatAnthropic) — nothing else in the agent needs to change.
Secrets come from the environment (see config.py); never hardcode them.
"""

import os

from langchain_openai import AzureChatOpenAI

import agents.bi_agent.config  # noqa: F401  (imported for its load_dotenv side effect)


def get_llm() -> AzureChatOpenAI:
    """Construct the chat model from environment variables."""
    return AzureChatOpenAI(
        azure_endpoint=os.environ["AZURE_OPENAI_ENDPOINT"],
        azure_deployment=os.environ["AZURE_OPENAI_DEPLOYMENT"],
        api_key=os.environ["AZURE_OPENAI_API_KEY"],
        api_version=os.environ["AZURE_OPENAI_API_VERSION"],
        temperature=0,
    )
