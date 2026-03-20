# Fullstack Interview Monorepo

This monorepo is intended for an in-office interview setup.

## Structure
- `backend/` Kotlin + Spring Boot
- `frontend/` Vue 3 + Vite
- `patient-service/` FastAPI mini service

## Branches
- `main` → clean base system
- `challenge/patient-overview-pr` → challenge branch

## Important
This repo intentionally uses:
- a Dev Container that installs **Gradle** into the container
- VS Code tasks that call **`gradle ...`**, not `./gradlew`
