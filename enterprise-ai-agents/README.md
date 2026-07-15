# Deploying AI agents two ways

This repo deploys AI agents two ways in an enterprise context:

- **Approach A — Dify**: no-code, built and run in the Dify UI.
- **Approach B — Prefect + LangGraph**: code-first, the agent is plain Python
  orchestrated by Prefect.

The point is the *deployment pattern*, not fancy analytics. Each agent is built
the same way in both stacks — same system prompt, same steps, same tools — so a
given agent genuinely feels like the same agent whichever way it's deployed.

There are two example agents:

- a **daily business-intelligence agent** (scheduled), and
- a **GitHub issue review agent** (triggered manually).

Everything here is **free and open-source and self-hosted**. There are no paid
or managed-cloud dependencies anywhere.

## Prerequisites

- **[Docker](https://www.docker.com/) and Docker Compose** — runs Dify, Opik,
  the Prefect server, and all Postgres databases.
- **[git](https://git-scm.com/)** — to clone the vendor repos (Dify, Opik)
  into `vendor/`.
- **[uv](https://docs.astral.sh/uv/)** — runs the Prefect + LangGraph agents
  (Approach B); it installs the required Python version itself, so no separate
  Python install is needed. Install it with
  `curl -LsSf https://astral.sh/uv/install.sh | sh`, or see the
  [install docs](https://docs.astral.sh/uv/getting-started/installation/) for
  other platforms.

## The agents

### 1. Daily business-intelligence agent (scheduled)

Once a day it:

1. **Loads memory** — reads accumulated "lessons" and injects them into the
   system prompt.
2. **Reads sources** — queries the sources Postgres, whose tables stand in for
   the data a BI agent would check.
3. **Analyzes** — one LLM call turns the data + lessons into a concise summary
   (notable changes, anomalies, trends).
4. **Produces output** — renders the summary as a clean PDF.
5. **Writes memory (conditionally)** — appends *one* short lesson **only** if
   there's a genuine, reusable insight. Clean runs write nothing.

Reading sources and writing memory are **tools the model calls**, not hardcoded
pipeline steps. PDF rendering is a deterministic step after the analysis.

### 2. GitHub issue review agent (manual)

Triggered on demand with three required inputs — repository owner, repository
name, and issue number. For one issue it:

1. **Reads the issue** and its full comment thread.
2. **Investigates the codebase** — searches code and browses repository
   contents through the GitHub REST API to understand what the issue touches
   and plan the implementation in its head. This exploration is required, not
   optional: the verdict has to be grounded in the actual code.
3. **Judges handoff-readiness** — decides whether an engineer has enough to
   start, and if not, the open questions that block them.
4. **Posts its review** — writes a Markdown review (verdict, summary,
   assessment, and open questions when not ready) and posts it as an issue
   comment.

Reading the issue, searching the code, and commenting are all **tools the model
calls**. This agent keeps no memory and touches no Postgres — its world is the
GitHub API.

## Shared pieces

| Path | Role |
|------|------|
| `shared/sources_seed.sql` | **(BI agent)** Demo source data as Postgres-compatible SQL — auto-applied into each stack's `sources-db` container on first start. Both stacks read from identical databases. |
| `shared/memory_schema.sql` | **(BI agent)** The memory table blueprint. Each stack instantiates it into its own separate `memory-db` Postgres — the databases are never shared. |
| `vendor/` | **Gitignored.** Where you clone the official Dify and Opik repos — infrastructure, not tied to any agent. Vendor code is never committed or forked. |

The two `shared/*.sql` files back the **BI agent**; the GitHub agent is
stateless and needs neither — it talks only to the GitHub API. The BI agent's
memory contract is deliberately simple: append-and-inject text lessons. No
vector retrieval, no conversation history.

Each agent's code-first prompt lives next to its code — the BI agent's in
[`prefect/agents/bi_agent/prompts.py`](prefect/agents/bi_agent/prompts.py) and
the GitHub agent's in
[`prefect/agents/github_issue_agent/prompts.py`](prefect/agents/github_issue_agent/prompts.py).
In Dify the prompt is embedded in the exported workflow rather than a separate
file. The behaviour is kept the same across stacks; they differ only where the
stacks differ — e.g. the Dify BI prompt names that stack's tools and notes the
quirk that schema tools must be called with empty parameters.

---

## Versions

This demo was built and tested with **Dify 1.15.0**, **Opik 2.1.11**, and
**Prefect 3.7.6**. The clone commands below pin Dify and Opik to those
releases; Prefect is pinned in [`prefect/docker-compose.yml`](prefect/docker-compose.yml)
(the `prefect-server` image tag) and in [`prefect/uv.lock`](prefect/uv.lock)
(the `prefect` client library resolved from `pyproject.toml`'s `prefect>=3.1.0`
constraint) — both should be kept in sync if you bump it.

Opik uses two **different version numbers** for its two halves: `2.1.11` is the
self-hosted **server** (the git tag pinned by the clone command under
[Observability](#observability-opik)), while
[`prefect/pyproject.toml`](prefect/pyproject.toml)'s `opik>=1.3.0` pins the
separate `1.x` **Python SDK**. They aren't in conflict — one versions
the server, the other the client.

Newer versions will
generally work, but Dify in particular evolves quickly — node types get
renamed or removed, configuration options change, and the workflow DSL format
itself can shift between releases. If you use a newer Dify version, an
exported workflow under `dify/workflows/` (e.g. `bi-agent/bi-agent.yml`) may
fail to import or import incorrectly; rebuild it in the UI against your
version in that case.

---

## Approach A — Dify (no-code)

Dify ships its own Docker Compose. **Do not fork or copy it** — clone the
official repo into `vendor/` and layer on the one piece this repo owns (two
dedicated Postgres databases).

```bash
# From the repo root:
git clone --branch 1.15.0 --depth 1 https://github.com/langgenius/dify vendor/dify

# Add the override + shared SQL files into Dify's docker dir:
cp dify/docker-compose.override.yaml vendor/dify/docker/
cp shared/memory_schema.sql          vendor/dify/docker/
cp shared/sources_seed.sql           vendor/dify/docker/

# Extra packages for Python code nodes (fpdf2 for the PDF):
mkdir vendor/dify/docker/volumes/sandbox/dependencies/ && cp dify/python-requirements.txt vendor/dify/docker/volumes/sandbox/dependencies/

cd vendor/dify/docker
cp .env.example .env
docker compose up -d        # picks up docker-compose.override.yaml automatically
```

The sandbox installs the packages in `python-requirements.txt` on startup — no
image rebuild. To add more later, edit that file (and copy over again) and 
`docker compose restart sandbox`.

> **Why fpdf2 here, but reportlab in the Prefect stack?** Dify runs code nodes
> in a seccomp-sandboxed process that kills native-code libraries with *"bad
> system call"* (SIGSYS). reportlab ships a C extension and pulls in Pillow, so
> it can't run in the sandbox; pure-Python **fpdf2** can. The Prefect agent runs
> on the host with no such restriction, so it keeps reportlab. Same report,
> different renderer — a concrete example of how the deployment model constrains
> your library choices.

This brings up Dify plus:
- **sources-db** — a Postgres seeded with the demo data (port 5435)
- **memory-db** — a Postgres for agent lessons (port 5434)

Both are separate from Dify's own internal Postgres.

### Import the workflows and wire them up

The workflows are exported under [`dify/workflows/`](dify/workflows/), one
subfolder per agent, import them In Dify, go to
**Studio → Create app → Import DSL file** and import each:

- **BI agent** — both files in
  [`dify/workflows/bi-agent/`](dify/workflows/bi-agent/), **in this order**:
  1. `append-lesson.yml` first — a helper workflow that appends a lesson to
     memory. After importing it, **publish it as a tool** (open it → **Publish
     → Publish as a tool**) so it exists as a workspace tool.
  2. `bi-agent.yml` second — the daily agent. Import it only after
     `append-lesson` has been published; see the reassignment note in step 2
     below for why the order matters.
- **GitHub agent** —
  [`dify/workflows/github-issue-agent/github-issue-agent.yml`](dify/workflows/github-issue-agent/github-issue-agent.yml)
  (manual trigger; reads the issue, searches code/repos, and posts its review).

Imported DSL carries the graph and prompts but not your credentials or
workspace-level tools, so three things need wiring up:

**1. Add an AI provider and select the model.** In **Integrations → Model
Provider**, add your provider and credentials (this demo uses Azure AI Foundry;
any OpenAI-compatible provider works). Then open each workflow's Agent/LLM node
and point it at the correct model.

**2. Install the tool plugins (and add the GitHub custom tools).**

Dify detects the plugins a DSL file depends on and pops up an **install
dialog during import itself** — install everything it lists there from the
Marketplace in one go. Plugin-based tools come in already wired by the import,
so once installed you don't re-select them in the node — you just add each
plugin's credentials.

- **BI agent** — install the **database tool plugin** it references. The plugin
  is installed once but needs a separate **authorization** (credential entry)
  per database, since `bi-agent` and `append-lesson` each talk to a different
  one — add both and pick the right one on each workflow's database tool node:
  - `bi-agent` needs **read-only** access to sources-db:
    `postgresql://reader:reader@sources-db:5432/sources`
  - `append-lesson` needs **read/write** access to memory-db:
    `postgresql://agent:agent@memory-db:5432/memory`

  Use the hostnames above (`sources-db`/`memory-db`), not `localhost`, since
  the database tool runs as a Dify plugin container on the same Docker network
  as the databases — the override file added them to Dify's own compose
  project.

  Its other tool, `append-lesson`, is the **workflow tool** you published
  above — same as the GitHub custom tools below, the import can't bind a tool
  that didn't exist yet, so open the BI agent's Agent node and **reassign
  `append-lesson`** on it manually (this is exactly why it must be published
  *before* `bi-agent.yml` is imported).
- **GitHub agent** — install the **GitHub tool plugin** and set your token. Its
  other two tools — reading the issue and its comments — are **custom tools**,
  not a plugin, so create them once: **Integrations → Tools → Swagger API as Tools**, add the
  **Swagger/OpenAPI schema**
  [`dify/workflows/github-issue-agent/github-issue-tools.json`](dify/workflows/github-issue-agent/github-issue-tools.json). Since custom tools are created at the
  workspace level, add these two on the workflow's agent node afterwards (the
  import can't bind a tool that didn't exist yet).

**3. Add Opik monitoring.** Observability is configured per app: open a
workflow, go to its **Monitoring** section, and add **Opik**. For a locally
started Opik, set the URL to `http://host.docker.internal:5173/api/` (Dify runs
in Docker, so it reaches host-run Opik via `host.docker.internal`; note the
trailing `/api/`) and leave the **API key empty**. See
[Observability](#observability-opik).

---

## Approach B — Prefect + LangGraph (code-first)

Both agents are plain code under
[`prefect/agents/`](prefect/agents/) and do **not** depend on Prefect — Prefect
is only the serving/scheduling layer. The infrastructure (Prefect server + the
BI agent's two Postgres databases) runs in **Docker**, so nothing is installed
or written to your home folder — `prefect.toml` keeps all client state in a
project-local `.prefect/`. The agents run from your **CLI via `uv`**, so you see
all the logs in your terminal. All commands are run from the `prefect/`
directory. The LLM provider (**Azure AI Foundry**) and tracing (**Opik**) are
isolated in each agent's `llm.py` / `graph.py` and wired the same way for both.

### Start the infrastructure (Docker)

```bash
cd prefect
cp .env.example .env          # Azure AI Foundry values (+ GITHUB_TOKEN for the GitHub agent)
docker compose up -d          # Prefect server + sources-db + memory-db
```

This starts:
- **Prefect server** (UI at <http://127.0.0.1:4200>; state in a Docker volume)
- **sources-db** — Postgres seeded with the demo data on `localhost:5436` *(BI agent)*
- **memory-db** — Postgres for agent lessons on `localhost:5433` *(BI agent)*

Follow logs with `docker compose logs -f`. The GitHub agent uses neither
Postgres — it needs only `GITHUB_TOKEN` in `.env` and the Prefect server.

### Install dependencies

The agents run via [`uv`](https://docs.astral.sh/uv/) (see
[Prerequisites](#prerequisites) if you don't have it installed). From
`prefect/`, create the virtualenv and install the locked dependencies:

```bash
uv sync
```

`uv run <command>` (used throughout below) re-syncs automatically if
`pyproject.toml`/`uv.lock` change, but running `uv sync` once up front avoids
paying that sync cost on your first command.

---

### The daily BI agent

Scheduled; reads the two Postgres DBs and renders a PDF.

```
agents/bi_agent/
  graph.py    # the LangGraph graph + run_agent()
  nodes.py    # one node per agent step
  tools.py    # source access + memory write, as tools
  memory.py   # read/append lessons in Postgres
  llm.py      # Azure AI Foundry model, isolated + swappable
  prompts.py  # canonical system prompt (shared with Dify)
  schemas.py  # the structured BIReport the agent returns
  render.py   # render the report to PDF and Markdown
  config.py   # DSNs + .env loading
```

#### Run it from your CLI

```bash
uv run bi-agent
```

This runs the agent once, **streaming all logs to your terminal**, writes a
timestamped PDF to `prefect/output/`, and publishes the run's artifacts (a
markdown report + a key-metrics table) to the Prefect UI. Each run produces a
new PDF — runs never overwrite each other.

To see that the agent is just code, independent of Prefect, run the graph
directly (no flow, no server needed — only the two DBs):

```bash
uv run python -m agents.bi_agent
```

#### Serve it

```bash
uv run bi-agent-serve
```

A single long-running process: it registers the `bi-agent/bi-agent-daily`
deployment and runs the **daily schedule** (07:00), logging to your terminal.
Trigger a run on demand from another terminal:

```bash
uv run prefect deployment run 'bi-agent/bi-agent-daily'
```

Or trigger it straight from the **Prefect UI** (<http://127.0.0.1:4200> →
Deployments → `bi-agent-daily` → **Run**); a Quick run works since it takes no
parameters.

To run it as a containerized service instead — closer to how you'd ship it —
there's an opt-in `bi-agent-worker` service (built from `Dockerfile.bi-agent`
with `uv`) behind the **`bi-agent`** Compose profile that runs the same
`bi-agent-serve` inside a container:

```bash
docker compose --profile bi-agent up -d --build         # also (re)builds the image
docker compose logs -f bi-agent-worker                  # watch it serve + run
docker compose exec bi-agent-worker prefect deployment run 'bi-agent/bi-agent-daily'
```

---

### The GitHub issue review agent

Triggered manually with inputs (owner, repo, issue number); talks only to the
GitHub API — no Postgres, no memory. (See [The agents](#the-agents) for what it
does; it will also get a Dify build under Approach A.)

```
agents/github_issue_agent/
  graph.py    # the LangGraph graph + run_agent(owner, repo, issue_number)
  nodes.py    # the single review step (reads issue, explores code, comments)
  tools.py    # custom GitHub REST API tools (the model calls these)
  llm.py      # same Azure AI Foundry model, isolated + swappable
  prompts.py  # the review system prompt
  config.py   # GitHub token/URL + .env loading
```

Its **custom GitHub tools** call the GitHub REST API directly, authenticated
with `GITHUB_TOKEN`: `get_issue`, `get_issue_comments`,
`search_code`, `search_repositories`, `get_repository_contents`, and
`post_issue_comment`. `post_issue_comment` posts the review to the issue for
real, so `GITHUB_TOKEN` needs **write** access to the repository.

#### Run it from your CLI

Set `GITHUB_TOKEN` in `prefect/.env`, then run it once for a single issue:

```bash
uv run github-issue-agent <owner> <repo> <issue_number>
```

Or run the graph directly, without Prefect:

```bash
uv run python -m agents.github_issue_agent <owner> <repo> <issue_number>
```

#### Serve it

Because it's manual, "serving" it just registers a deployment with **no
schedule** so you can trigger it on demand with the required parameters:

```bash
uv run github-issue-agent-serve   # in one terminal: registers the deployment
# in another terminal:
uv run prefect deployment run 'github-issue-review/github-issue-review-manual' \
    -p owner=<owner> -p repo=<repo> -p issue_number=<n>
```

You can also trigger it from the **Prefect UI** (<http://127.0.0.1:4200> →
Deployments → `github-issue-review-manual`). Choose **Custom run**, not Quick
run — the flow's `owner`, `repo`, and `issue_number` inputs are required and
you set them on the custom-run form.

Or run it as a container — its own `github-agent-worker` service (built from
`Dockerfile.github-agent`) behind the **`github-agent`** Compose profile:

```bash
docker compose --profile github-agent up -d --build     # also (re)builds the image
docker compose logs -f github-agent-worker              # watch it register the deployment
docker compose exec github-agent-worker \
    prefect deployment run 'github-issue-review/github-issue-review-manual' \
    -p owner=<owner> -p repo=<repo> -p issue_number=<n>
```

---

### Tear down

```bash
docker compose down # add -v to also drop the volumes (server + DB data)
```

---

## Observability (Opik)

Both approaches can send traces to a **self-hosted Opik** instance. This repo
ships **no Opik files** — clone and run the official stack:

```bash
git clone --branch 2.1.11 --depth 1 https://github.com/comet-ml/opik vendor/opik
cd vendor/opik
./opik.sh           # starts the local Opik stack
```

Then point the agent at it. For the **code-first** stack, set
`OPIK_TRACING_ENABLED=true` in `prefect/.env`. The Opik SDK reads its own
connection vars from the environment automatically (e.g. `OPIK_URL_OVERRIDE`,
`OPIK_API_KEY`) — no code change needed. Tracing is wired in `graph.py` via `OpikTracer` and is
**swappable** — replace it with another LangChain callback handler (e.g.
Langfuse) to change tracers.
For **Dify**, configure its built-in observability integration to point at the
local Opik instance.

---

## Demo vs. production

The Docker Compose setups here are for running **locally** — they are *not* a
production deployment. Everything used is **free, open-source, and
self-hosted**. For production you would:

- externalize state — a managed-by-you (self-hosted) Postgres, Redis, and
  object storage instead of in-container volumes;
- run the stacks on your own **Kubernetes** cluster via each project's official
  open-source **Helm charts** (Dify, Prefect, and Opik all publish them) — not
  via Docker Compose;
- containerize the agents and run them as Prefect deployments on a work pool
  (e.g. Kubernetes), instead of `serve()` from your CLI. The agent code in
  `agents/` (both `bi_agent/` and `github_issue_agent/`) stays exactly the same
  — only the serving layer changes.
