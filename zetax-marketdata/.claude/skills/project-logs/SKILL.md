---
name: project-logs
description: Use when writing an entry in ../docs/go/DEV_LOG.md (after every approved change), ../docs/go/LEARNING_LOG.md (after every finished step), or a git commit message in the ZetaX market-data Go project.
---

# Project logs

Three records. Formats are fixed — follow them exactly. Logs are append-only: never edit or delete old entries (add a correction entry instead).

## 1. DEV_LOG (`../docs/go/DEV_LOG.md`) — after every approved change

```
### YYYY-MM-DD — step-NN — <short title>
- **What:** what changed
- **Why:** reason
- **Files:** `path` (new / changed / deleted)
- **Approved by Raguram:** yes — <how>
- **Commit:** `<type>(step-NN): <summary>` or "not committed yet"
- **Notes:** surprises, follow-ups, versions (Go, dependencies)
```

- Date from the system (`date +%F`), never guessed.
- Use the step number from `../docs/go/TASKS.md` (two digits: `step-03`).
- If Raguram typed the code himself, say so in **Files** ("typed by Raguram").

## 2. LEARNING_LOG (`../docs/go/LEARNING_LOG.md`) — after every finished step

Use the template at the top of the file. Rules:
- "In my own words" must come from Raguram's explain-back. Tidy grammar only, don't replace his meaning.
- Include Go vs Java rows for new syntax and ideas.
- Record mistakes and confusions honestly (compiler errors, race detector findings) — they are the most useful part.
- New Go vs Java items also go into the quick-reference table in `../docs/go/LEARNING.md`.

## 3. Commit messages — format in `../docs/go/CONVENTIONS.md`

```
<type>(step-NN): <short summary, present tense, ≤ ~60 chars>
```

Types: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`.

Before proposing a commit:
- `gofmt -l .` prints nothing, `go vet ./...` and `go test -race ./...` pass.
- Run `git status` and show it. Make sure `.env` is not staged.
- Commit needs its own approval (`approval-gate`).
- After committing, put the commit message (and short hash) into the DEV_LOG entry.
