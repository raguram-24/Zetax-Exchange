---
name: explain-go-for-java
description: Use whenever showing or explaining Go code or a Go concept to Raguram, a Java/Spring Boot developer who is new to Go. Explains new lines in simple English with Java comparisons and the Java habit to unlearn.
---

# Explain Go for a Java developer

Raguram knows Java, Spring Boot, TypeScript and NestJS well. He is new to Go and did **not** read Go lessons first. Teach just in time.

## How to explain

1. **Only explain what's new.** Skip things already covered (check `../docs/go/LEARNING_LOG.md`).
2. **Line by line for new ideas**: what the line does, in one simple sentence.
3. **Compare with Java** (or TypeScript) when it helps:

   | Go | Java |
   |---|---|
   | `b := NewBook("ABC")` | `var b = new Book("ABC");` (no `new` keyword, a plain function) |

4. **Say where the Java comparison breaks** — that's where the bugs are.
5. **Point out the traps** Java developers hit in Go, when they show up:
   - Capital letter = exported (public); small letter = package-private. No `public` keyword.
   - No exceptions: errors are return values. `if err != nil { return ..., fmt.Errorf("...: %w", err) }`. `panic` is for bugs, not normal errors.
   - Unused imports and variables are **compile errors**.
   - Zero values: every type has a usable default (`0`, `""`, `nil`, `false`). A `nil` map can be read but **writing panics** — `make` it first.
   - Value vs pointer receivers: a value receiver works on a **copy**; changes are lost.
   - Slices share their backing array; `append` may or may not copy.
   - Map iteration order is random. Sort keys when order matters (book levels!).
   - Interfaces are implicit (no `implements`), and small. Define them where they're **used**, not next to the implementation.
   - A `nil` pointer inside an interface is not a `nil` interface.
   - Goroutines aren't threads you manage; but every goroutine needs an owner and a way to stop (`context`).
   - Closing a channel twice, or sending on a closed one, panics. Only the sender closes.
   - Maps are **not** safe for concurrent use. The race detector (`go test -race`) finds this.
   - No DI container: wiring is plain code in `main`. That's a feature.
6. **Connect to ZetaX** when the line is about market data (e.g. "this is why we sort: bids must be shown highest price first").
7. **End with one check question**, e.g. "What happens if two goroutines call `Apply` on the same book at once?"

## Style

- Simple English, short sentences.
- Small examples first, then the project code.
- Use `go doc <pkg>.<Name>` output or the official docs (pkg.go.dev, go.dev) when a detail matters. If unsure, say so and point to the docs. Don't guess.
