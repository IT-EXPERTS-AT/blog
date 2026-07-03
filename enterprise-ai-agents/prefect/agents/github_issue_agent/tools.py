"""Agent tools: read GitHub through its REST API.

These are custom tools (not a marketplace plugin) that call the GitHub REST API
directly. The model decides when to search code, browse repository contents,
look up an issue and its comments — they are tools it calls, not fixed pipeline
steps. Auth is a Bearer token from the environment (see config.py).
"""

import base64
import logging

import httpx

from langchain_core.tools import tool

from agents.github_issue_agent.config import GITHUB_API_URL, GITHUB_TOKEN

logger = logging.getLogger(__name__)

# Trim large payloads so the model gets useful context without blowing the
# token budget on a single file or a huge search result set.
_MAX_ITEMS = 30
_MAX_FILE_CHARS = 8000


def _headers() -> dict[str, str]:
    headers = {
        "Accept": "application/vnd.github+json",
        "X-GitHub-Api-Version": "2022-11-28",
    }
    if GITHUB_TOKEN:
        headers["Authorization"] = f"Bearer {GITHUB_TOKEN}"
    return headers


def _get(path: str, params: dict | None = None) -> httpx.Response:
    """GET a GitHub REST endpoint. `path` is relative to the API base."""
    url = path if path.startswith("http") else f"{GITHUB_API_URL}{path}"
    return httpx.get(url, headers=_headers(), params=params, timeout=30.0)


def _post(path: str, json: dict | None = None) -> httpx.Response:
    """POST to a GitHub REST endpoint. `path` is relative to the API base."""
    url = path if path.startswith("http") else f"{GITHUB_API_URL}{path}"
    return httpx.post(url, headers=_headers(), json=json, timeout=30.0)


@tool(parse_docstring=True)
def get_issue(owner: str, repo: str, issue_number: int) -> str:
    """Get a single issue's title, state, labels, author and body.

    Use this first to read what the issue is actually asking for.

    Args:
        owner: Repository owner — the user or organization, e.g. 'IT-EXPERTS-AT'.
        repo: Repository name, e.g. 'Hello-World'.
        issue_number: The issue's number within the repository, e.g. 42.
    """
    request = _get(f"/repos/{owner}/{repo}/issues/{issue_number}")
    if request.status_code != 200:
        return f"GitHub error {request.status_code}: {request.text[:300]}"
    i = request.json()
    labels = ", ".join(lbl["name"] for lbl in i.get("labels", [])) or "(none)"
    return (
        f"#{i['number']} {i['title']}\n"
        f"State: {i['state']} | Author: {i['user']['login']} | Labels: {labels}\n"
        f"Comments: {i.get('comments', 0)}\n\n"
        f"{i.get('body') or '(no description)'}"
    )


@tool(parse_docstring=True)
def get_issue_comments(owner: str, repo: str, issue_number: int) -> str:
    """Get the comment thread on an issue, oldest first.

    Use this to see clarifications already provided before asking for more.

    Args:
        owner: Repository owner — the user or organization, e.g. 'IT-EXPERTS-AT'.
        repo: Repository name, e.g. 'Hello-World'.
        issue_number: The issue's number within the repository, e.g. 42.
    """
    request = _get(f"/repos/{owner}/{repo}/issues/{issue_number}/comments")
    if request.status_code != 200:
        return f"GitHub error {request.status_code}: {request.text[:300]}"
    comments = request.json()
    if not comments:
        return "(no comments)"
    return "\n\n---\n\n".join(
        f"{c['user']['login']} at {c['created_at']}:\n{c.get('body') or ''}"
        for c in comments[:_MAX_ITEMS]
    )


@tool(parse_docstring=True)
def search_code(query: str) -> str:
    """Search code across GitHub with the code-search syntax.

    Returns matching file paths.

    Args:
        query: A GitHub code-search query; scope it to a repo with the repo qualifier, e.g. 'AuthMiddleware repo:acme/api'.
    """
    request = _get("/search/code", params={"q": query, "per_page": _MAX_ITEMS})
    if request.status_code != 200:
        return f"GitHub error {request.status_code}: {request.text[:300]}"
    items = request.json().get("items", [])
    if not items:
        return "(no code matches)"
    return "\n".join(
        f"{it['repository']['full_name']}: {it['path']}" for it in items
    )


@tool(parse_docstring=True)
def search_repositories(query: str) -> str:
    """Search repositories with the repo-search syntax.

    Returns full name, description and primary language for each match.

    Args:
        query: A GitHub repository-search query, e.g. 'payments org:acme' or 'topic:billing language:python'.
    """
    request = _get("/search/repositories", params={"q": query, "per_page": _MAX_ITEMS})
    if request.status_code != 200:
        return f"GitHub error {request.status_code}: {request.text[:300]}"
    items = request.json().get("items", [])
    if not items:
        return "(no repositories found)"
    return "\n".join(
        f"{it['full_name']} [{it.get('language') or '?'}]: "
        f"{it.get('description') or '(no description)'}"
        for it in items
    )


@tool(parse_docstring=True)
def get_repository_contents(owner: str, repo: str, path: str = "") -> str:
    """List a directory or read a file in a repository.

    For a directory, returns its entries; for a file, returns its (truncated)
    text content.

    Args:
        owner: Repository owner — the user or organization, e.g. 'IT-EXPERTS-AT'.
        repo: Repository name, e.g. 'Hello-World'.
        path: Path within the repo — 'src/app.py' for a file, 'src' for a directory, or empty for the repository root.
    """
    request = _get(f"/repos/{owner}/{repo}/contents/{path}")
    if request.status_code != 200:
        return f"GitHub error {request.status_code}: {request.text[:300]}"
    data = request.json()
    if isinstance(data, list):  # directory listing
        return "\n".join(f"{e['type']:4} {e['path']}" for e in data[:_MAX_ITEMS])
    # single file
    if data.get("encoding") == "base64" and data.get("content"):
        text = base64.b64decode(data["content"]).decode("utf-8", errors="replace")
        if len(text) > _MAX_FILE_CHARS:
            text = text[:_MAX_FILE_CHARS] + "\n... (truncated)"
        return text
    return f"(cannot display {data.get('path', path)}; size {data.get('size', '?')} bytes)"


@tool(parse_docstring=True)
def post_issue_comment(owner: str, repo: str, issue_number: int, body: str) -> str:
    """Post your review as a comment on the issue.

    Pass your review as `body`.

    Args:
        owner: Repository owner — the user or organization, e.g. 'IT-EXPERTS-AT'.
        repo: Repository name, e.g. 'Hello-World'.
        issue_number: The issue's number within the repository, e.g. 42.
        body: The comment text, in GitHub-flavored Markdown — your review with the open questions.
    """
    logger.info("Posting comment to %s/%s#%s:\n%s", owner, repo, issue_number, body)
    request = _post(
        f"/repos/{owner}/{repo}/issues/{issue_number}/comments",
        json={"body": body},
    )
    if request.status_code != 201:
        return f"GitHub error {request.status_code}: {request.text[:300]}"
    return f"Comment posted: {request.json()['html_url']}"


# Tools handed to the agent.
GITHUB_TOOLS = [
    get_issue,
    get_issue_comments,
    search_code,
    search_repositories,
    get_repository_contents,
    post_issue_comment,
]
