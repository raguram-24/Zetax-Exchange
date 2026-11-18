---
name: java-approval-gate
description: Use before ANY change in the Java side of ZetaX - writing or editing a .java file, pom.xml, application.yml, a zetax-db SQL migration, docker-compose.yml, docs/java files (incl. flow documents), adding a dependency, deleting/renaming, or a git commit. Raguram approves every change first, then it is logged in docs/java/DEV_LOG.md.
---

# Java approval gate

Rule: **no change without Raguram's approval.** Same method as the Go service.

## Steps
1. **What and why** (2–4 lines): which step/checkbox in `docs/java/TASKS.md`, which business rule (R-xx / D-xx) it serves.
2. **Show the change** in chat: new file → full file; existing file → diff or changed block with context; command → exact command. Aim for ≤ ~30 new lines; split bigger work.
3. **Explain new ideas** with `explain-java-concepts` (only what isn't in `docs/java/LEARNING_LOG.md` yet).
4. **List the files** created or changed.
5. **Who types it:** Raguram types **only the new, important code** (the heart of a new concept: matching loop, concurrency/thread code, key Spring wiring). For that, describe what to write and let him type it; then review, explain, don't rewrite unless asked. Routine code (records, enums, config, boilerplate, test scaffolding) → Claude shows it, gets approval, writes it.
6. Ask exactly: `Approve? (yes / change something / no)`
7. **Wait.** Only "yes" / "approved" / "go ahead" / "do it" counts. A question or "looks good but…" is not approval: answer, ask again.
8. **After approval:** make exactly that change. No "while I'm here" extras.
9. **Check:** `mvn -q -pl <module> -am verify` (or the narrower `test`) **and** the Sonar check for each new/changed function (`java-clean-code` section 0). Show the result honestly, including failures and Sonar findings.
10. **Log:** append to `docs/java/DEV_LOG.md` (format at the top of the file, date from `date +%F`).

## Dependencies (root CLAUDE.md: ask first)
Own approval, with: why the JDK / existing starters aren't enough, `groupId:artifactId:version` (managed by the Boot BOM?), which module, licence. Say "verify" if you couldn't check the docs.

## Commits
Separate approval. Run `java-clean-code` checklist and `mvn verify` first, show `git status`, propose:
**One commit per module per flow**, made when that module's part of the flow is fully implemented (all its `TASKS.md` checkboxes done, `mvn verify` green, Sonar clean). Until then the work stays uncommitted on the flow branch.
Format (Conventional Commits, **no space before the colon**): `<type>(<module>): <summary>`, blank line, footer `Refs: ZTX-<issue>`. Modules: `common`, `engine`, `gateway`, `persistor`, `db`, `marketdata`; `docs` / `setup` for non-module work. Step numbers (`j01`) never appear in commits; they stay in `TASKS.md`. Types `feat` `fix` `refactor` `test` `docs` `chore` (e.g. `feat(common): add place-order contracts` + `Refs: ZTX-1`).
Branches: **one branch per flow**, `feature/ZTX-<issue>/<flow-name>`: `<issue>` = the flow's GitHub issue number (autolink `ZTX-` → issue), `<flow-name>` in kebab case from the flow document's title (e.g. BF-02, issue #12 → `feature/ZTX-12/place-limit-order`). All steps and the design work of that flow go on it; commits use the module scope (see above); the PR says `Closes #<issue>`. Work not tied to a flow: ask Raguram for the branch name.
`git status` must show **no private learning file** (`LEARNING*.md`, `DEV_LOG.md`, `INTERVIEW.md`, `PROGRESS_BUSINESS_LEARNING.md`, `GLOSSARY.md`); never `git add -f` them.
After the commit, put message + short hash into the DEV_LOG entry.

## Does NOT need approval
Reading, searching, explaining, `mvn test` / `mvn verify` on approved code, `git status` / `git log`, `java -version`.

## Never
- Write a file first and ask afterwards.
- Bundle unrelated changes.
- Touch `zetax-marketdata/`, `zetax-web/` or the vault without asking.
- Edit a `zetax-db` migration that may already have been applied: add a new `V…` file instead.
- Edit old DEV_LOG / LEARNING_LOG entries (add a correction entry instead).
