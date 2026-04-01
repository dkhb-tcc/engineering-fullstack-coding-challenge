# Candidate Instructions

Welcome. This is a working fullstack application. A colleague has opened a PR and something is not behaving correctly.

## System Overview

The app shows a **Patient Overview** consisting of:
- Patient master data (name, date of birth) — fetched from a Python/FastAPI service on port 8000
- The patient's most recent vital sign measurement — stored in a database and served by the Spring Boot backend on port 8080

```
Browser (Vue 3 · :5173)
    └── Spring Boot API (:8080)
            ├── FastAPI patient service (:8000)
            └── H2 database (in-memory, via jOOQ)
```

## Your Tasks

### 1 — Review the PR (~15 min)

Look at the changes introduced by the PR and note what you find:
```bash
git diff main..HEAD
```

### 2 — Run the failing tests (~5 min)

Use the VS Code task **Run Interview Test**, or run:
```bash
gradle test --tests '*PatientOverviewIntegrationTest*'
```

The tests are failing. Understand why, then fix the bugs.

### 3 — Check the running application

All services are already running. Open the browser at `http://localhost:5173`.

You can also call the APIs directly:
```bash
curl http://localhost:8080/api/patients/1/overview
curl http://localhost:8000/patients/1
```

### 4 — Fix the bugs

Fix everything you find. Commit your changes on this branch.

### 5 — Discuss

Be ready to talk through:
- What each bug was and where it lived
- How you located it
- What you would improve beyond the immediate fix

## Tips

- The test assertions tell you exactly what the correct values should be
- Compare the FastAPI response with the Spring Boot response
- Ask yourself: does your fix address the symptom, or the root cause?
