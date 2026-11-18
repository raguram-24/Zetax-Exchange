# CONVENTIONS — naming, layout and commits

## 1. Naming (Go standard = Effective Go + `gofmt`)

| Thing | Style | Example | Java equivalent |
|---|---|---|---|
| Module path | lowercase, repo URL + folder | `github.com/raguram-24/zetax-exchange/zetax-marketdata` | `groupId:artifactId` |
| Packages (folders) | short, lowercase, one word, no `_` | `book`, `candle`, `ws`, `kafkasrc` | `com.zetax.marketdata.book` |
| Files | lowercase, `snake_case.go` | `book.go`, `price_level.go` | `PriceLevel.java` |
| Tests | same folder, `_test.go` | `book_test.go` | `src/test/java/...` |
| Exported (public) | `MixedCaps`, capital first | `Book`, `NewBook`, `ApplyEvent` | `public` |
| Unexported (package-private) | `mixedCaps`, small first | `levelFor`, `maxLevels` | `private` / package-private |
| Constructors | `NewX` returning `*X` (or `X`) | `NewBook(symbol string) *Book` | `new Book(symbol)` |
| Interfaces | behaviour, often `-er` | `EventSource`, `Publisher` | `interface` (but small!) |
| Receivers | 1–2 letters, same everywhere for a type | `func (b *Book) Apply(...)` | `this` |
| Errors (sentinel) | `ErrX` | `ErrUnknownSymbol` | custom exception class |
| Error variables | `err`, always the **last** return value | `book, err := load()` | `throws` |
| Constants | `MixedCaps` (not UPPER_SNAKE) | `DefaultDepth = 20` | `static final` |
| Acronyms | all same case | `userID`, `HTTPServer`, `eventID` | `userId` |
| Env vars | `UPPER_SNAKE_CASE` | `KAFKA_BROKERS`, `HTTP_ADDR` | same |

### JSON field names
- JSON stays **camelCase** to match the Java engine (`priceTicks`, `eventId`); Go fields follow Go rules (`PriceTicks`, `EventID`) with tags: `EventID string \`json:"eventId"\``.

### Tests
- Function names: `TestBook_Apply_TradeReducesLevel`, `TestDecode_UnknownType_ReturnsError`.
- Table-driven by default: `tests := []struct{ name string; ... }{...}` + `t.Run(tt.name, ...)`.
- Test data in `testdata/` (Go tooling ignores that folder for builds).

### Rules
- No abbreviations except well-known ones (`id`, `url`, `http`, `ws`) and Go idioms (`ctx`, `err`, `r`, `w`).
- The package name is part of every call: `book.New(...)` reads better than `book.NewBook(...)`. Decide the style once in Step 3 and keep it.
- No `utils` / `common` / `helpers` packages.

## 2. Layout (grows step by step — don't create empty folders early)

```
zetax-marketdata/
├── cmd/marketdata/main.go   # wiring only (from Step 5; Step 0 may start with ./main.go)
├── internal/                # code no other module can import
│   ├── event/               # event types + decoding (Step 1)
│   ├── source/              # fake + kafka sources (Steps 2, 9)
│   ├── book/                # price-level book view (Step 3)
│   ├── candle/              # trades + OHLC (Step 4)
│   ├── api/                 # REST handlers (Step 6)
│   └── ws/                  # WebSocket hub (Step 7)
├── testdata/
└── docs/
```
Final names are proposed in each step and need approval.

## 3. Git branches

`feature/ZTX-<issue>/<flow-name>`: one branch per flow; `<issue>` = the flow's GitHub issue number (autolink `ZTX-`), `<flow-name>` in kebab case from the flow document in `../flows/` (e.g. `feature/ZTX-20/market-data`). The PR says `Closes #<issue>`. All steps of that flow go on it; commits keep the step scope (`feat(step-01): …`). Work not tied to a flow: ask Raguram.

- `ZTX-<issue>` = the ticket; `<flow-name>` = readable name

## 4. Commit messages

Format (Conventional Commits, module scope, ticket footer). **One commit per module per flow**, made when the service's part of the flow is fully implemented (all its steps done, `go test -race ./...` green, lint clean):

```
<type>(marketdata): <short summary in present tense, max ~60 chars>

<optional body: what and why, wrapped at ~72 chars>

Refs: ZTX-<issue>
```

Step numbers never appear in commits; they stay in `TASKS.md`.

| Type | Use for |
|---|---|
| `feat` | New behaviour |
| `fix` | Bug fix |
| `refactor` | Code change with no behaviour change |
| `test` | Adding or changing tests |
| `docs` | Docs and logs only |
| `chore` | Setup, dependencies, config, Docker |

Examples:

```
feat(marketdata): stream order book snapshots and deltas

Refs: ZTX-20
```

Rules:

- No space before the colon (`feat(step-01): …`, not `feat(step-01) : …`). Scope `docs` / `setup` for work not tied to the service.
- One logical change per commit.
- Before every commit: `gofmt` clean, `go vet ./...`, `go test -race ./...` pass.
- Commit only after Raguram approves (`CLAUDE.md`, Rule 1).
- Never commit `.env` or the private learning files (`../docs/go/DEV_LOG.md`, `LEARNING_LOG.md`, `LEARNING.md`). Check `git status` before every commit.
