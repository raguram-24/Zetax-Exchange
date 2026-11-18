---
name: approval-gate
description: Use before ANY change in this project - writing or editing a .go file, go.mod, running go get or go mod tidy, changing config or Dockerfile, deleting/renaming, or making a git commit. Raguram must approve every change first.
---

# Approval gate

Raguram's rule: **never write code without his approval.** Follow these steps every time.

## Steps

1. **Say what and why** (2–4 lines, simple English).
   - Which step and task from `../docs/go/TASKS.md` this belongs to.
2. **Show the change** in the chat:
   - New file → the full file.
   - Existing file → a clear diff or the changed block with a few lines of context.
   - Command → the exact command (`go get github.com/...@vX`, `go mod tidy`).
   - Keep it small: aim for under ~30 new lines. Split bigger work into several approvals.
3. **Explain new ideas** using the `explain-go-for-java` skill (only the parts that are new to him — check `../docs/go/LEARNING_LOG.md`).
4. **List the files** that will be created or changed (including `go.mod` / `go.sum` if a command touches them).
5. **Offer to let him type it** when the piece introduces a new Go idea. Typing it teaches more than reading it.
6. **Ask exactly:** `Approve? (yes / change something / no)`
7. **Wait.**
   - Approval = a clear "yes", "approved", "go ahead", "do it".
   - A question, "looks good but…", or silence is **not** approval. Answer and ask again.
   - "Change something" → update the proposal and go back to step 2.
8. **After approval:** make exactly the approved change. Nothing extra (no "while I'm here" fixes).
9. **Check it:** `gofmt -l .` (should print nothing), `go vet ./...`, `go test ./...` where tests exist, and the IDE diagnostics (gopls + SonarQube for IDE) for each new/changed function. Show the output.
10. **Log it:** add a `../docs/go/DEV_LOG.md` entry with the `project-logs` skill.
11. **Commit only with a separate approval:** run the `go-review` skill, propose the commit message (format in `../docs/go/CONVENTIONS.md`), show `git status`, wait for yes.

## Dependencies

A new dependency (`go get`) always needs its own approval with:
- why the standard library is not enough,
- the module path and version, and that you checked its repo / docs (say "verify" if you couldn't),
- what it adds to `go.mod` / `go.sum`.

## Does NOT need approval

Reading files, searching, explaining, answering questions, read-only commands (`go version`, `go env`, `go vet`, `go test`, `go build`, `go doc`, `git status`, `git log`), running already-approved code.

## Never

- Never write a file "to save time" and ask afterwards.
- Never bundle unrelated changes into one approval.
- Never touch `.env` contents or print secrets.
- Never edit the Java modules (`../zetax-common`, `../zetax-engine`, `../zetax-gateway`), the repo-root files (`../pom.xml`, `../docker-compose.yml`, `../CLAUDE.md`, `../README.md`), `../zetax-web` or the vault without asking.
