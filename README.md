# Fullstack Interview Monorepo

In-office fullstack interview setup. The candidate reviews a PR, finds bugs in a running system, and fixes them.

## Architecture

```
Browser (Vue 3 / Vite :5173)
    ↓  /api/*  (Vite proxy)
Spring Boot / Kotlin (:8080)
    ├── FastAPI / Python (:8000)   ← patient master data
    └── H2 in-memory database      ← vital signs (via jOOQ)
         ↑  AMQP (optional)
    RabbitMQ                       ← incoming vital sign events
```

## Structure

| Folder | Stack | Purpose |
|---|---|---|
| `backend/` | Kotlin, Spring Boot 3, jOOQ, H2 | REST API + RabbitMQ listener |
| `frontend/` | Vue 3, Vite | Patient overview UI |
| `patient-service/` | Python, FastAPI | Patient master data service |
| `docs/` | — | Interviewer guide + candidate instructions |

## Branches

| Branch | Purpose |
|---|---|
| `main` | Clean, fully working reference implementation |
| `challenge/patient-overview-pr` | PR with three intentional bugs for the interview |

## Quick Start (Interviewer)

1. Check out `challenge/patient-overview-pr`
2. Start VS Code tasks in order: **Start Patient Service** → **Start Backend** → **Start Frontend**
3. Open `http://localhost:5173` — three bugs are immediately visible in the browser
4. Hand over to the candidate with `docs/CANDIDATE_INSTRUCTIONS.md` open

## Important

This repo uses a Dev Container. VS Code tasks call **`gradle`** directly (not `./gradlew`).
