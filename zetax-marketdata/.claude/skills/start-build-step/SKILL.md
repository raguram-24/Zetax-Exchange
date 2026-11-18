---
name: start-build-step
description: Use when Raguram says "start step N", "next step", or "what's next" in the ZetaX market-data Go project. Walks through learn → plan → propose → build → explain-back → log for one step of ../docs/go/TASKS.md.
---

# Start a build step

## 1. Find the step
- Open `../docs/go/TASKS.md`. Find the first step with status `todo` or `doing` (or the one Raguram named).
- Check the previous step is `done`. If not, say so and ask whether to finish it first.
- Before Step 0: check the "business reading" checklist. If it isn't done, offer a short explain-back on the ZetaX basics first.
- Check `../docs/go/DECISIONS.md` and `../docs/go/EVENTS.md` for open questions this step must answer. Raise them **before** coding.
- Propose setting the step status to `doing` and creating the flow's branch if it doesn't exist yet (`feature/<flow-name>`, see `../docs/go/CONVENTIONS.md`) — both need approval.

## 2. Learn first (10–15 min)
- Open `../docs/go/LEARNING.md` → the row for this step.
- Explain each Go item briefly with the `explain-go-for-java` skill: tiny examples (not project code yet), the Java habit to unlearn, one trap.
- Explain the ZetaX / market-data concept in the row, linked to the vault section it comes from.
- Ask 1–2 check questions. Don't move on until he's comfortable.

## 3. Plan
- Break the step into small pieces (the checkboxes in `../docs/go/TASKS.md`).
- Name the packages, files, types and functions you plan, using `../docs/go/CONVENTIONS.md`.
- Say what the test for each piece will check. Prefer writing the test first for logic (book, candles, decoder).

## 4. Build, one small piece at a time
- For each piece, use the `approval-gate` skill. Never skip it.
- After each approved piece: Raguram runs it (`go run`, `go test -v`, `go test -race`) and you read the output together.
- When the compiler complains, let him read the error first and guess the fix.
- Tick the checkbox in `../docs/go/TASKS.md` (docs change → mention it, small approval).

## 5. Explain-back
- Ask Raguram to explain the step in his own words: what the code does, why Go does it that way, what would break if…
- Correct gently, add what's missing.

## 6. Close the step
- `../docs/go/LEARNING_LOG.md` entry (use `project-logs`), based on his explanation.
- New Go vs Java rows → quick-reference table in `../docs/go/LEARNING.md`.
- `../docs/go/DEV_LOG.md` has an entry for each change.
- Answered questions → move to Decisions in `../docs/go/DECISIONS.md` (his words, with the reason).
- Run `go-review`, then propose the commit (approval needed).
- Set step status to `done` in `../docs/go/TASKS.md`.
- Suggest saving key notes to Second Brain (Raguram decides).

## Stay focused
If a new idea comes up mid-step, add it to "Parked ideas" in `../docs/go/TASKS.md` and return to the current step.
