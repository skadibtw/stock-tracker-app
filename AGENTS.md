# AGENTS.md

> Project map for AI agents. Keep this file up-to-date as the project evolves.

## Project Overview
This repository is intended to hold the full trading and investing application platform. For the current branch, the active implementation scope includes the imported Android client prototype, the Kotlin mobile API gateway, the Kotlin transactional backend and database-access layer, the Go quote ingestion service, containerized infrastructure, and supporting architecture documentation; the wider system context still includes cross-platform mobile clients plus a Linux quote driver in C.

## Tech Stack
- **Language:** Kotlin
- **Framework:** Ktor
- **Database:** PostgreSQL
- **ORM:** Exposed
- **Related Platform Stack:** Redis or KeyDB, ClickHouse, Docker, OpenTelemetry, Go quote service, Linux C driver

## Project Structure
```text
.
├── .ai-factory/                 # AI Factory project context artifacts
│   ├── ARCHITECTURE.md         # Clean Architecture rules for the backend
│   └── DESCRIPTION.md          # Project specification and backend scope
├── .agents/                     # External skills installed from skills.sh for OpenCode
│   └── skills/                 # Installed project-level external skills
├── .opencode/                   # Project-local OpenCode skills and generated custom skills
│   └── skills/                 # Built-in AI Factory skills plus custom project skills
├── .github/                     # Bundled project skill sources and repo metadata
│   └── skills/                 # Source copies of AI Factory skills
├── android-app/                 # Imported Android application prototype
│   ├── app/                    # Android app module with Activities, Fragments, and resources
│   └── gradle/                 # Android Gradle wrapper support files
├── mobile-backend/              # Kotlin Ktor backend module for the mobile app
│   ├── build.gradle.kts        # Module dependencies and build logic
│   └── src/                    # Kotlin sources, resources, and tests for backend work
├── mobile-api/                  # Kotlin Ktor API gateway for mobile clients
│   ├── build.gradle.kts        # Module dependencies and build logic
│   └── src/                    # Kotlin sources, resources, and tests for gateway work
├── deploy/                      # Docker Compose and observability configs
│   ├── otel-collector/         # OpenTelemetry Collector config
│   ├── prometheus/             # Prometheus scrape config
│   ├── loki/                   # Loki config
│   ├── tempo/                  # Tempo config
│   └── grafana/                # Grafana provisioning
├── docs/                        # Backend implementation and endpoint reference docs
├── knowledge-base/              # Obsidian knowledge base for the course project
│   └── obsidian/               # Linked markdown notes for theory and defense prep
├── reports/                     # LaTeX reports and generated PDFs
│   └── course-project/         # Final course project report sources
├── gradle/                      # Gradle wrapper files shared by repo modules
├── build.gradle.kts             # Root multi-module Gradle configuration
├── settings.gradle.kts          # Root module includes and naming
├── gradle.properties            # Shared Gradle and version properties
├── docker-compose.yml           # Local and server deployment stack
├── README.md                    # Minimal repository landing page
├── opencode.json                # Existing OpenCode configuration
└── .mcp.json                    # Project-level MCP server configuration
```

## Key Entry Points
| File | Purpose |
|------|---------|
| `README.md` | Repository landing page and branch-scope run/test commands |
| `opencode.json` | Existing OpenCode config currently used by the workspace |
| `.mcp.json` | Project MCP server definitions for GitHub, filesystem, and Postgres |
| `.ai-factory/DESCRIPTION.md` | Product scope, tech stack, and non-functional requirements |
| `.ai-factory/ARCHITECTURE.md` | Clean Architecture rules for the Kotlin backend |
| `settings.gradle.kts` | Root Gradle module declarations |
| `android-app/app/build.gradle.kts` | Android application module build definition |
| `mobile-api/build.gradle.kts` | Mobile API gateway module build definition |
| `mobile-backend/build.gradle.kts` | Kotlin Ktor backend module build definition |
| `docker-compose.yml` | Multi-service local/server stack |
| `deploy/otel-collector/config.yaml` | OpenTelemetry Collector pipeline config |
| `docs/system-architecture.md` | Multi-service architecture and deployment plan |
| `knowledge-base/obsidian/00-Главная.md` | Entry point for the Obsidian knowledge base |
| `reports/course-project/report.tex` | Main LaTeX source for the course report |
| `docs/endpoints.md` | API contract reference for implemented backend endpoints |
| `.opencode/skills/trading-backend-kotlin-api/SKILL.md` | Custom project skill for Kotlin trading backend work |

## Documentation
| Document | Path | Description |
|----------|------|-------------|
| README | `README.md` | Project landing page |
| Architecture | `.ai-factory/ARCHITECTURE.md` | Architecture pattern and dependency rules |
| Project Description | `.ai-factory/DESCRIPTION.md` | Project specification and stack |
| Backend Implementation | `docs/backend-implementation.md` | Backend setup, configuration, and integration notes |
| Architecture Overview | `docs/architecture-overview.md` | Current architecture summary with frontend integration status |
| Android Frontend Overview | `docs/android-frontend-overview.md` | Imported Android client structure and gaps |
| Branch Overview | `docs/branch-overview.md` | Git branch status and external frontend import context |
| System Architecture | `docs/system-architecture.md` | Multi-service architecture and Docker deployment guide |
| Endpoint Reference | `docs/endpoints.md` | Request and response contracts for current endpoints |
| Testing Strategy | `docs/testing-strategy.md` | Current and planned testing model |
| Observability Status | `docs/observability-status.md` | OpenTelemetry and observability baseline |
| Course Requirements | `docs/Full_task.pdf` | Full assignment brief and submission constraints |
| Knowledge Base | `knowledge-base/obsidian/00-Главная.md` | Obsidian note index for theory and project context |
| Course Report | `reports/course-project/report.tex` | LaTeX source for the final PDF report |
| Project Map | `AGENTS.md` | Structural guide for AI agents |

## AI Context Files
| File | Purpose |
|------|---------|
| `AGENTS.md` | This file — project structure map |
| `.ai-factory/DESCRIPTION.md` | Project specification and tech stack |
| `.ai-factory/ARCHITECTURE.md` | Architecture decisions and guidelines |
| `CLAUDE.md` | Agent instructions and preferences if added later |

## Installed Skills
| Skill | Path | Purpose |
|------|------|---------|
| `kotlin-ktor-patterns` | `.agents/skills/kotlin-ktor-patterns` | Ktor server structure and testing patterns |
| `supabase-postgres-best-practices` | `.agents/skills/supabase-postgres-best-practices` | Postgres schema and query best practices |
| `trading-backend-kotlin-api` | `.opencode/skills/trading-backend-kotlin-api` | Domain guidance for trading backend workflows |

## Team Context
| Area | Owner |
|------|-------|
| Android mobile app | `@Mirajain` (imported from external repository) |
| Cross-platform mobile app | Not yet confirmed |
| Kotlin mobile backend | `@Viladit`, `@dismyplay` |
| Go quote collection | `@FreakLord` |
| Linux C quote driver | `@FreakLord` |
| PostgreSQL / Redis / ClickHouse / Docker | `@e345ee` if confirmed |
| OpenTelemetry | `@Viladit` |
| 10,000-client simulator | `@aeshabb` |

## Agent Rules
- Keep this file factual and update it when the project structure changes.
- Read `.ai-factory/DESCRIPTION.md` before planning or implementing backend work.
- Prefer the custom trading backend skill when working on auth, portfolio, transaction, or statistics logic.
- Treat Go quote ingestion, the Linux C driver, and the imported Android client as adjacent systems for this branch, even though they may later live in the same repository.
- Never combine shell commands with `&&`, `||`, or `;` — execute each command as a separate Bash tool call.
