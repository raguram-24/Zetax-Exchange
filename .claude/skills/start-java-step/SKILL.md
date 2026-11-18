---
name: start-java-step
description: Use when Raguram says "start step J3", "next Java step", "what's next" for the Java modules (zetax-common, zetax-engine, zetax-gateway, zetax-persistor, zetax-db). Runs one step of docs/java/TASKS.md as business concept → Java/Spring concept → plan → approved small pieces → explain-back → logs.
---

# Start a Java build step

One step = one business idea + one Java/Spring idea + working, tested code.
Files: `docs/java/TASKS.md` (plan + status, steps grouped by flow), `docs/flows/` (one design document per flow), `docs/java/LEARNING.md` (what each step teaches), `docs/java/LEARNING_LOG.md`, `docs/java/DEV_LOG.md`.

## 1. Find the step
- Open `docs/java/TASKS.md`. Take the step Raguram named, or the first `doing` / `todo` step.
- Check the previous step is `done`. If not, say so and ask whether to finish it first.
- Read the step's **flow document** first (`docs/flows/<flow>.md`: decisions, contracts, diagram). For flows the repo wins over the vault's planning diagrams.
- If the step starts a flow that has **no flow document yet**, design it first with the `design-flow` skill. No code before the flow's decisions are written down.
- Then the **Blocked by** line and the vault sections (Overview D-xx, "Business and Technical Flow" A/B/TF). If an open question blocks the step, ask it **before** teaching or coding. Never invent the answer.
- Propose: status → `doing` (docs change, needs approval via `java-approval-gate`).

## 2. Business first (5–10 min) — `teach-business-concept` skill
- The trading idea this step implements, from the vault section in the step's row.
- 1–2 check questions. Don't move on until the answer is right.
- Tick the matching row in `docs/PROGRESS_BUSINESS_LEARNING.md` when that part is understood.

## 3. Java / Spring concept (10–15 min) — `explain-java-concepts` skill
- Only the concepts in the step's row of `docs/java/LEARNING.md`.
- Tiny standalone example first, then how it shows up in ZetaX.
- 1–2 check questions.

## 4. Plan
- Break the step into pieces (the checkboxes in `TASKS.md`), each ≤ ~30 new lines (one class or one rule + its test).
- Name packages, classes, methods. Follow the module rules (D-21, root `CLAUDE.md`): `book` = no Spring, no Kafka; used by more than one app → `zetax-common`, used by one app → that app.
- Follow the data-access rules: `zetax-persistor` does all writes, `zetax-gateway` only reads, `zetax-engine` never touches the DB; schema changes only as new files in `zetax-db/`.
- For each piece say what the test checks. For `book` logic, **test first** (`java-unit-tests` skill).
- Say which pieces Raguram types himself: **only the new, important code** (the core of the concept, e.g. the matching loop, the poll loop, thread start/stop, a proxy-sensitive Spring wiring). Routine code (records, enums, simple getters, config, test scaffolding, boilerplate) Claude proposes and writes after approval.

## 5. Build, one piece at a time
- Every piece goes through `java-approval-gate`. No exceptions.
- After each piece: Raguram runs `mvn -pl <module> test` (or `mvn verify`); read the output together.
- On a compile or test failure, let him read it first and guess the cause.
- Tick the checkbox in `TASKS.md`.

## 6. Explain-back
- Ask him to explain: what the code does, which business rule it enforces, why the Java/Spring choice, and one "what breaks if…" (e.g. "what if two threads called `place` on the same book?").
- Correct gently, add what's missing. If he can't explain a piece, re-teach before closing.

## 7. Close the step
- `docs/java/LEARNING_LOG.md` entry from his explain-back (his words, tidied).
- New rows in the quick-reference tables of `docs/java/LEARNING.md`; new trading terms → `docs/GLOSSARY.md`.
- **Interview prep:** 2–4 questions from this step's Java/Spring concepts → `docs/java/INTERVIEW.md` (format in the file). Ask him to answer one out loud first; record his answer, add follow-ups and the trap.
- `java-clean-code` checklist + `mvn verify` green, then propose the commit (separate approval).
- If the step finishes a flow: flow document status → `done` in `docs/flows/README.md`; lifecycle / `ER.md` versions match what was built.
- Status → `done`. Suggest notes worth saving to the vault (Raguram decides; never edit the vault unasked).

## Stay focused
New idea mid-step → "Parked ideas" in `TASKS.md`, return to the step.
