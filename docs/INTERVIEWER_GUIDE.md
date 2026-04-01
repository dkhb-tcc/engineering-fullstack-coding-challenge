# Interviewer Guide

## Setup Checklist (before the candidate arrives)

- [ ] Check out `challenge/patient-overview-pr`
- [ ] Start **Start Patient Service** task (FastAPI · :8000)
- [ ] Start **Start Backend** task (Spring Boot · :8080)
- [ ] Start **Start Frontend** task (Vite · :5173)
- [ ] Open `http://localhost:5173` — confirm all three visible bugs
- [ ] Open `docs/CANDIDATE_INSTRUCTIONS.md` for the candidate
- [ ] Close this file

---

## The Three Bugs

### Bug 1 · jOOQ sort order (`VitalSignService`)

**Location:** `backend/src/main/kotlin/com/example/demo/vitalsign/VitalSignService.kt` → `getLatest()`

```kotlin
// BUG: ASC + LIMIT 1 → returns the OLDEST record
.orderBy(F_MEASURED_AT.asc())
```

| | |
|---|---|
| **Fix** | `.asc()` → `.desc()` |
| **Browser** | Heart Rate shows `72 bpm` (2024-01-01) instead of `68 bpm` (2024-06-10) |
| **Test failure** | `expected: <68> but was: <72>` |

---

### Bug 2 · Response mapping (`PatientOverviewController`)

**Location:** `backend/src/main/kotlin/com/example/demo/patient/PatientOverviewController.kt` → `getOverview()`

```kotlin
// BUG: firstName and lastName are swapped
firstName = info.lastName,
lastName  = info.firstName,
```

| | |
|---|---|
| **Fix** | Swap the two lines |
| **Browser** | Shows "Schmidt Anna" instead of "Anna Schmidt" |
| **Test failure** | `expected: <Anna> but was: <Schmidt>` |
| **Diagnostic path** | `curl localhost:8000/patients/1` → correct; `curl localhost:8080/api/patients/1/overview` → swapped → bug is in the mapping |

---

### Bug 3 · Raw timestamp in frontend (`App.vue`)

**Location:** `frontend/src/App.vue`, Measured field

```vue
<!-- BUG: renders 1704067200000 instead of a readable date -->
{{ patient.latestVitalSign.measuredAt }}
```

| | |
|---|---|
| **Minimal fix** | Restore `{{ formatDate(patient.latestVitalSign.measuredAt) }}` |
| **Root-cause fix** | Change `measuredAt: Long` → `measuredAt: Instant` in `VitalSign`; Jackson serialises it to `"2024-06-10T00:00:00Z"` automatically — no `formatDate()` needed |
| **Browser** | Measured shows a 13-digit number |

> The frontend bug is a symptom of the API contract. A strong candidate proposes `Instant` as the proper fix, not just a template patch.

---

## Interview Flow

### Phase 1 — PR Review (15 min)

Say: *„A colleague just opened this PR. Please read through the changes and give me your review."*

```bash
git diff main..HEAD
```

**Follow-up questions:**
- „What would happen if this went to production as-is?"
  - Heart rate shows the oldest instead of the latest reading
  - Name swap displays wrong patient identity
  - Strong candidate: names the domain consequence — stale vital signs could lead to wrong clinical decisions (e.g. incorrect medication dosage, missed deterioration) — and argues for blocking the PR
- „How would you phrase your review comment on the sort order?"
  - Names the exact method call: `orderBy(F_MEASURED_AT.asc())` + `limit(1)` → returns oldest
  - States the fix: change to `.desc()`
  - References the failing test as evidence
  - Look for: precision and a non-accusatory tone
- „What does the test tell you about the expected behaviour?"
  - Two records seeded (2024-01-01 / 72 bpm and 2024-06-10 / 68 bpm), assertion on 68 bpm
  - Contract: *latest by timestamp*
  - Strong candidate: treats tests as specifications, not just regression safety nets

---

### Phase 2 — Debugging & Fixing (25–30 min)

Say: *„The tests are failing. The app is already running at `http://localhost:5173`. Investigate and fix what you find."*

**Follow-up questions while they work:**
- *(after opening the browser)*: „What do you notice? What's wrong?"
  - Name reversed ("Schmidt Anna")
  - Vital sign value looks wrong
  - Measured date is a raw number
  - Strong candidate: spots all three without being prompted
- *(after Bug 1)*: „Would you write this query differently? Is jOOQ the right tool here?"
  - Yes — jOOQ is exactly the right lever: `.orderBy(F_MEASURED_AT.desc()).limit(1)` compiles to `ORDER BY measured_at DESC LIMIT 1` — type-safe, no raw SQL, lets the DB sort efficiently with an index
  - The alternative would be fetching all records and using `maxByOrNull { it.measuredAt }` in Kotlin — unnecessary data transfer, sorts in memory, scales poorly
  - Strong candidate: understands that jOOQ exists precisely to avoid raw SQL while still expressing the query intent clearly
- *(after Bug 2)*: „How did you determine where the wrong values came from?"
  - `curl localhost:8000/patients/1` → correct data from Python service
  - `curl localhost:8080/api/patients/1/overview` → swapped → bug is in the Spring mapping
  - Strong candidate: verifies source of truth before touching code
- *(after Bug 3, if they use `formatDate()`)*: „What would a client-independent fix look like?"
  - Change `measuredAt: Long` → `measuredAt: Instant` in `VitalSign`
  - Jackson (`jackson-module-kotlin` + `JavaTimeModule`) serialises `Instant` as ISO-8601 automatically
  - Every client benefits; `formatDate()` in the frontend becomes unnecessary
- *(if they finish early)*: „What could go wrong in `VitalSignListener`?"
  - `System.nanoTime()` is JVM-local — not globally unique across instances
  - Two nodes or rapid messages on the same JVM can produce colliding IDs → constraint violation or silent overwrite
  - Fix: `UUID.randomUUID()`, DB sequence, or RabbitMQ message ID
  - Also: no dead-letter queue, no idempotency guard

---

### Phase 3 — Architecture Discussion (15 min)

**On the service boundary:**
- „Why do we have a separate Python service for patient data?"
  - Separation of concerns — patient demographics may belong to a different domain/team (e.g. hospital PAS/EHR)
  - Python/FastAPI is common for data-heavy or ML-adjacent services
  - **Push back here:** is a synchronous runtime dependency on a separate service really the right cut? The Spring backend can't serve the overview without the patient service being up — that's tight coupling disguised as separation
  - Strong candidate: questions the split proactively and can articulate when a service boundary adds value vs. just adds failure modes
  - Very strong candidate: brings up Self-Contained Systems (SCS) — each system owns its own UI, logic, and data, communicates asynchronously, and can operate independently. Patient data could be replicated into the Spring service's own DB via events, making the overview endpoint resilient by default
- „What happens to the `overview` endpoint if the patient service is down?"
  - Spring endpoint returns 500 (propagates whatever `PatientClient` throws)
  - No fallback — caller gets an error even if vital-sign data is available
  - Strong candidate: spots this without prompting and connects it back to the architectural decision
- „How would you make that call resilient?"
  - Circuit breaker (Resilience4j)
  - Timeout + fallback (cached/stale data or partial response)
  - Return 503 to the frontend instead of a raw 500
  - Very strong candidate: argues that async replication (SCS-style) eliminates the runtime dependency entirely — resilience by design, not by workaround

**On RabbitMQ:**
- „Walk me through what happens when a vital sign message arrives."
  - RabbitMQ delivers message → `VitalSignListener` deserialises → persists via `VitalSignRepository`
  - Complete answer: covers AMQP exchange/queue binding, acknowledgement mode (auto-ack vs. manual)
  - Strong candidate: explains what happens on exception (message requeued or dropped)
- „What could go wrong with `id = System.nanoTime()` in the listener?"
  - `nanoTime()` is JVM-local — not globally unique across instances or rapid calls
  - Risk: constraint violation or silent overwrite
  - Better: use the message timestamp as the key — deterministic and tied to the event itself
  - Best: `UUID.randomUUID()` or a DB-generated identity (no semantic coupling to the timestamp)
- „Is the listener safe if two backend instances run in parallel?"
  - RabbitMQ has no partition concept — unlike Kafka, where the same key always routes to the same partition, consumed by exactly one node in a consumer group
  - With RabbitMQ at-least-once delivery, the same message can be redelivered to two nodes simultaneously (e.g. after a network hiccup before ACK)
  - Even a timestamp from the message as key doesn't fully protect against this: both nodes receive the same message and try to persist the same record → constraint violation or race condition
  - Fix: unique constraint on `(patientId, measuredAt, type)` as a DB-level safety net, plus idempotent handling of the resulting conflict (catch and ignore duplicate key errors)

**On the data model:**
- „`getLatest()` ignores the `patientId` path variable — what's missing?"
  - A `WHERE patient_id = :patientId` clause
  - Without it: returns the most recent vital sign across *all* patients — data-leak / privacy bug
  - Strong candidate: adds the filter and mentions a test with multiple patients
- „`measuredAt` is a `Long`. What are the consequences for API consumers?"
  - Consumers must know the epoch unit (ms? s?) — not self-documenting
  - Timezone handling is implicit
  - Value is unreadable in logs and UIs
  - Fix: use `Instant` → ISO-8601 string over the wire — timezone-safe, readable, language-agnostic

---

## Assessment

Score each dimension 0–3. Total: 0–18.

| Dimension | 0 | 1 | 2 | 3 |
|---|---|---|---|---|
| **Code Review** | Finds nothing | Finds one bug | Finds two bugs | Finds all three by reading the diff alone |
| **Debugging approach** | Random code changes | Uses tests OR browser | Uses both systematically | Calls APIs directly to isolate the mapping bug |
| **Fix quality** | Workarounds / wrong layer | Compiles, but suboptimal | Correct minimal fix | Cleanest fix + explains trade-offs |
| **Kotlin / Spring / jOOQ depth** | No familiarity | Reads code, struggles to write | Comfortable with fixes | Names `Instant`, `maxByOrNull`, jOOQ codegen unprompted |
| **System thinking** | Treats bugs in isolation | Notices service boundary | Understands FastAPI ↔ Spring contract | Discusses resilience, error handling, multi-instance concerns proactively |
| **Communication** | Silent, no explanation | Explains after prompting | Narrates while working | Clear, structured, adapts to the level of the interviewer |

### Score Guide

| Total | Recommendation |
|---|---|
| 15–18 | **Strong hire** — senior-level instincts across all dimensions |
| 11–14 | **Hire** — solid engineer, minor gaps that are coachable |
| 7–10 | **Maybe** — strong in one area, significant gaps elsewhere |
| 0–6 | **No hire** — fundamentals missing |

### Calibration Notes

- A candidate who only fixes Bug 1 (the failing test forces it) but misses Bug 2 and 3 **has not done a real review**.
- Proposing `Instant` for `measuredAt` without prompting shows **API design awareness** beyond the immediate task.
- Mentioning idempotency, UUID, or dead-letter queues unprompted shows **production mindset**.
- If all three bugs are fixed in under 15 minutes, invest the remaining time in Phase 3 — the interesting signal is in the discussion, not the fixes.
