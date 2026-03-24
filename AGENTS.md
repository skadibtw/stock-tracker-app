# AGENTS.md

> Project map for AI agents. Keep this file up-to-date as the project evolves.

## Project Overview
This repository is intended to hold the full trading and investing application platform. For the current branch, the active implementation scope is only the Kotlin backend for the mobile app and the Kotlin database-access layer; the wider system context still includes Android and cross-platform mobile clients, Go quote ingestion, a Linux quote driver in C, and shared infrastructure.

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
├── mobile-backend/              # Kotlin Ktor backend module for the mobile app
│   ├── build.gradle.kts        # Module dependencies and build logic
│   └── src/                    # Kotlin sources, resources, and tests for backend work
├── docs/                        # Backend implementation and endpoint reference docs
├── gradle/                      # Gradle wrapper files shared by repo modules
├── build.gradle.kts             # Root multi-module Gradle configuration
├── settings.gradle.kts          # Root module includes and naming
├── gradle.properties            # Shared Gradle and version properties
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
| `mobile-backend/build.gradle.kts` | Kotlin Ktor backend module build definition |
| `docs/endpoints.md` | API contract reference for implemented backend endpoints |
| `.opencode/skills/trading-backend-kotlin-api/SKILL.md` | Custom project skill for Kotlin trading backend work |

## Documentation
| Document | Path | Description |
|----------|------|-------------|
| README | `README.md` | Project landing page |
| Architecture | `.ai-factory/ARCHITECTURE.md` | Architecture pattern and dependency rules |
| Project Description | `.ai-factory/DESCRIPTION.md` | Project specification and stack |
| Backend Implementation | `docs/backend-implementation.md` | Backend setup, configuration, and integration notes |
| Endpoint Reference | `docs/endpoints.md` | Request and response contracts for current endpoints |
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
| Android mobile app | `@Mirajain` |
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
- Treat Go quote ingestion, the Linux C driver, and mobile clients as adjacent systems for this branch, even though they may later live in the same repository.
- Never combine shell commands with `&&`, `||`, or `;` — execute each command as a separate Bash tool call.
