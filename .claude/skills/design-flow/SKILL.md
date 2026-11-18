---
name: design-flow
description: Use when a ZetaX flow (BF-xx, e.g. place order, cancel, admin) has no flow document yet or must be redesigned - "design flow BF-03", "let's design the next flow", or when start-java-step reaches a flow with no design. Guides Raguram through scope, business check, decisions (one question at a time), contracts, diagrams, data model, build plan and explain-back, filling one document per flow in docs/flows/. Repo-wide: works for Java and Go (zetax-marketdata) flows.
---

# Design a flow (before any code)

You are a guide. One flow = one document: `docs/flows/<BF-xx>-<slug>.md`, listed in `docs/flows/README.md`.
Raguram decides; Claude recommends. **Never invent requirements**: vault = planning input, the repo flow doc = final design.
Shared docs (the exchange): `docs/flows/`, `docs/ER.md`, `docs/GLOSSARY.md`, `docs/PROGRESS_BUSINESS_LEARNING.md`. Learning tracks: `docs/java/` (Java), `docs/go/` (Go).

**Which side?** From phase 1, note which apps the flow touches. Then per side:

| | Java side | Go side (`zetax-marketdata`) |
|---|---|---|
| Approval + log | `java-approval-gate` → `docs/java/DEV_LOG.md` | Go `approval-gate` → `docs/go/DEV_LOG.md` |
| Contracts (phase 4) | `zetax-common` records | `docs/go/EVENTS.md` + Go structs (`handle-new-event`); a contract change lands on **both sides in one commit** |
| Data model (phase 5) | `docs/ER.md`, `zetax-db/` | `docs/ER.md` + the service's own tables (`docs/go/DECISIONS.md`) |
| Steps (phase 6) | `docs/java/TASKS.md` + `docs/java/LEARNING.md` | `docs/go/TASKS.md` + `docs/go/LEARNING.md` |
| Build | `start-java-step` | Go `start-build-step` |


## The sequence
```
1. Plain words      → the boxes, scope, what's out
2. Business check   → the trading idea behind the flow
3. Decisions        → one question at a time
4. Contracts + diagrams
5. Data model       → ER version, tables, migrations
6. Build plan       → steps in TASKS.md
7. Explain-back     → Raguram explains the flow
—  Build: start-java-step (separate) · Review: on request
```

## Rules
1. **At the start:** show the sequence, say which phases this flow probably skips, ask to confirm. Skip patterns:
   - no new trading idea → skip 2
   - no new tables/columns → skip 5
   - no new order states → no new lifecycle version in 4
2. **Before each phase:** name it and what it produces ("Phase 3: Decisions. Each answer goes into the decisions table. Ready?").
3. **After each phase:** summarize what was written (section, key decisions, open items) and ask "Ready for the next phase?". Wait.
4. **Between phases:** say if the last phase changes the next one (e.g. a read-only gateway changes the admin flow).
5. **Raguram can stop anytime:** summarize where we are and which phase is next.

## Phase details

### 1. Plain words
- Read the vault sections (BF-xx, TF-xx, D-xx, R-xx), the existing flow docs and `docs/ER.md`.
- Draw the flow as 3–5 numbered boxes in plain English (who does what), no class names.
- Ask one question: which boxes does this flow build, and what is explicitly out? Write "Scope" + "Not in this flow".

### 2. Business check (`teach-business-concept`)
- Only the trading ideas this flow needs that are not yet in `docs/GLOSSARY.md` or marked done in `docs/PROGRESS_BUSINESS_LEARNING.md`.
- 1–2 predict-the-outcome questions on a fresh book. Fix misunderstandings before deciding anything.

### 3. Decisions: one question at a time
- **Exactly one question per message.** 2–3 options, the recommended one first and marked, with the reason. Short-form answers ("A").
- New concept in a question (ULID, golden file, consumer group…)? Explain it in 3–5 lines **before** asking.
- Raguram's reason goes into the table with the decision, in his words.
- He says he's confused → stop, go back to the plain-words boxes, re-ask one simple question.
- He changes an earlier decision → update that row (no second row), say what it affects.
- He asks a side question → answer it, then repeat the open question.
- Write each decision as it is made (approval gate): `| # | Topic | Decision |` in the flow doc.
- Things for later flows → "Notes for later flows"; ideas → "Parked ideas" in `docs/java/TASKS.md`.

### 4. Contracts + diagrams
- Draft the commands, events, enums and shared records this flow adds (Java signatures, no bodies). Placement rule: used by more than one app → `zetax-common`, one app → that app.
- One sequence diagram (Mermaid) for this flow only.
- New states → new version row + diagram in `docs/flows/ORDER-LIFECYCLE.md`.

### 5. Data model
- New `docs/ER.md` version row; changed entities and fields with their source (vault id or "Raguram's decision, date").
- Tables / columns as planned `zetax-db` migrations (new `V…` files, never edit applied ones). Data-access rules from root `CLAUDE.md`.

### 6. Build plan
- The flow doc's build plan says what each side builds; the steps go into that side's `TASKS.md` (table above). Java order, inside-out: contracts → book → engine on Kafka → `zetax-db` → persistor → gateway → end-to-end test.
- Each step: done-when, blocked-by, and its row in that side's `LEARNING.md` (business + language concept).
- Tests per level (`java-unit-tests`): unit, slice, golden-file contract, integration, 1–2 end-to-end.

### 7. Explain-back
- Raguram explains the flow from the first box to the last: what happens, which decision protects which promise (fairness, certainty, no loss), one "what breaks if…".
- Correct gently; re-teach what's unclear; note mistakes in the learning log of the side being built (`docs/java/` or `docs/go/` `LEARNING_LOG.md`).
- Flow doc status → `designed` in `flows/README.md`. "Still open" must be empty or each item parked with a step.

## Build and review
- Build is not part of this skill: hand over to `start-java-step` (Java) or the Go `start-build-step` with the first step of the flow.
- Review only on request, after code exists (`java-clean-code` checklist, `/code-review`).
- Design work goes on the flow's branch `feature/ZTX-<issue>/<flow-name>` (e.g. `feature/ZTX-12/place-limit-order`; create the flow's GitHub issue first), the same branch its build steps use later; commits `docs(bf-03): …` (format and private-file check: `java-approval-gate` → "Commits", or Go `CONVENTIONS.md`).

## Flow document layout
`Scope` · `Components` · `Sequence` · `Decisions` · `Contracts` · `Data model` · `Still open` · `Notes for later flows` · `Design progress` (checklist of phases 1–7)

## Returning mid-flow
Open `docs/flows/README.md`; for a flow in `designing`, read its "Design progress" checklist and "Still open", and resume at the first unchecked phase. More than one in progress → ask which.
