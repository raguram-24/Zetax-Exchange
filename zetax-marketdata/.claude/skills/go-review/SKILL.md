---
name: go-review
description: Use to review Go code in the ZetaX market-data project - before proposing code for approval, when Raguram typed code himself, and before every commit. A checklist for correctness, concurrency and ZetaX rules.
---

# Go review checklist

Run the tools first, then read the code. Report findings as: what, where (file:line), why it matters, suggested fix. Don't fix without approval.

## Tools

```
gofmt -l .          # must print nothing
go vet ./...
go test -race ./...
```

## Correctness

- [ ] Every error is checked; returned errors are wrapped with context (`%w`). No ignored `err` without a comment.
- [ ] No `panic` for normal failures (bad JSON, unknown symbol, closed connection).
- [ ] Maps are created with `make` (or a literal) before writing.
- [ ] Pointer vs value receivers: methods that change state use pointer receivers.
- [ ] Slices: no surprise sharing when returning internal slices (return a copy for snapshots).
- [ ] Map iteration order never leaks into output (sort book levels).

## Concurrency

- [ ] Every goroutine: who starts it, who stops it, how (`ctx.Done()`), who waits for it.
- [ ] Only the sender closes a channel; never send after close.
- [ ] Shared state: one owner goroutine, or protected by a mutex — never both, never neither.
- [ ] No blocking send that can hang forever (use `select` with `ctx.Done()` or a buffer + drop policy).
- [ ] `context.Context` is the first parameter of blocking / I/O functions and is passed down, not stored in structs (unless there's a reason written down).
- [ ] `go test -race` covers the concurrent paths.

## ZetaX rules (from `CLAUDE.md`)

- [ ] Prices / quantities are `int64` ticks. No `float64`.
- [ ] State-changing logic uses event time, never `time.Now()`.
- [ ] Duplicate `eventId` changes nothing.
- [ ] One symbol's events are applied in order on one goroutine.
- [ ] Service never writes to the `commands` topic.
- [ ] WebSocket: snapshot then deltas; sequence number increases by exactly one per change.

## Style

- [ ] Names follow `../docs/go/CONVENTIONS.md` (MixedCaps, short packages, `ErrX`, acronyms like `ID`).
- [ ] Small interfaces, defined where used. No unused abstractions "for later".
- [ ] No new dependency that wasn't approved.
- [ ] Exported names have a doc comment starting with the name (`// Book holds ...`).
- [ ] Tests are table-driven where there are several cases; test names say the expected result.
