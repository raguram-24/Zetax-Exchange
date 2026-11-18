---
name: java-clean-code
description: Use when writing, refactoring or reviewing Java code in ZetaX (zetax-common, zetax-engine, zetax-gateway, zetax-persistor, zetax-db) - applies SOLID, DRY, KISS and YAGNI, avoids common SonarQube issues, and runs a review checklist before code is proposed or committed.
---

# Java clean code — ZetaX

Goal: code that a reviewer and SonarQube would both accept: **no new Sonar issues, SOLID, DRY**, but never at the cost of the ZetaX hard rules in `CLAUDE.md` (ticks, determinism, no proxies in matching).

## 0. How to use this skill

- **Writing code:** apply sections 1–4 while designing; show the plan (classes, responsibilities) before code.
- **Reviewing code** (yours or Raguram's): go through the checklist in section 6. Report each finding as *what · where (file:line) · why · suggested fix*. Don't change code without OK.
- **Sonar is the judge:** Raguram runs **SonarQube for IDE** (formerly SonarLint) in IntelliJ / VS Code. Treat its warnings as real. The rule keys below are the common ones; if unsure of a key, describe the problem instead of guessing a number.
- Adding a Sonar Maven plugin, Checkstyle, SpotBugs or any library = a new dependency → ask first.
- **Sonar check after every function** (Raguram, 2026-11-19): as soon as a method is written (by Claude or Raguram), read the IDE diagnostics for that file (`mcp__ide__getDiagnostics`, which shows SonarQube for IDE findings when the IDE is connected). Show each finding (rule key, line, why), fix it in the same piece **before** asking for approval, and re-check until clean. If the diagnostics aren't available, ask Raguram to paste the file's issues from the IDE's Problems / SonarQube panel. Never move to the next function with an open Sonar issue on the current one.

## 1. SOLID — in this codebase

| Principle | Means here | Smell to watch |
|---|---|---|
| **S**ingle responsibility | `OrderBook` matches; it doesn't validate HTTP input, log JSON or talk to Kafka. `runner` does threads + Kafka; `book` stays pure Java | A class with "and" in its description; >1 reason to change |
| **O**pen/closed | New order type or new event → add a class / a `case`, don't rewrite the matcher | `if (type == X) ... else if (type == Y)` spread over many classes |
| **L**iskov | Any `Journal` (in-memory, Kafka) behaves the same for the engine: same ordering, same errors | An implementation that throws `UnsupportedOperationException` |
| **I**nterface segregation | Small interfaces: `Journal` (append/read), `EventPublisher` (publish). Not one `KafkaService` with 12 methods | Clients implementing methods they never use |
| **D**ependency inversion | `book` and services depend on interfaces they own; Spring wires the Kafka/Postgres implementations | `new KafkaTemplate(...)` inside business code; `book` importing Spring or Kafka |

Example — dependency inversion + open/closed for order handling:

```java
// engine.book: pure Java, owns the abstraction
public interface MatchingStrategy {
    List<EngineEvent> match(Order incoming, OrderBook book);
}

final class LimitOrderStrategy implements MatchingStrategy { /* rests the remainder */ }
final class MarketOrderStrategy implements MatchingStrategy { /* IOC, NO_LIQUIDITY on empty book */ }

public final class Matcher {
    private final Map<OrderType, MatchingStrategy> strategies;

    public Matcher(Map<OrderType, MatchingStrategy> strategies) {
        this.strategies = Map.copyOf(strategies);
    }

    public List<EngineEvent> handle(Order incoming, OrderBook book) {
        MatchingStrategy strategy = strategies.get(incoming.type());
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported order type: " + incoming.type());
        }
        return strategy.match(incoming, book);
    }
}
```
Use a pattern only when a second case actually exists (YAGNI). Two order types today (limit, market) → Strategy is justified; a one-off `if` is not a reason for an interface.

## 1b. Implementation patterns in ZetaX (where each one fits)

Use a pattern only when the problem in that row exists (YAGNI). Deeper reading: Raguram's vault `~/Documents/Second_Brain/wiki/topics/design-patterns/` (read-only; start with `DP 02 - How to Apply Patterns.md` and `DP 19 - Pattern Selection Cheatsheet and Quiz.md`).

| Pattern | Where in ZetaX | When (step) | Vault lesson |
|---|---|---|---|
| Command | `PlaceOrder`, `CancelOrder`… as sealed records on the `commands` topic (the journal) | J1 | `DP 14 - Command.md` |
| Sealed types + pattern-matching `switch` | Engine dispatch over `Command`, consumers over `Event` (no `default` → compiler finds missing cases) | J1, J5 | `DP 01 - OOP and SOLID Foundations.md` |
| Strategy | Limit vs market matching, once both exist | J11 | `DP 11 - Strategy.md` |
| State | Instrument ACTIVE / HALTED / DELISTED rules | J13 | `DP 16 - State.md` |
| Repository (JDBC) | Persistor writes, gateway reads (`JdbcClient`), one class per table group | J8, J9 | `DP 17 - Dependency Injection and Repository.md` |
| Dependency injection (constructor) | Spring apps; the `book` is plain `new` | all | `DP 17 - Dependency Injection and Repository.md` |
| Builder | Test data helpers (`buy(40).at(10100).by("alice")`), not production records | J2+ tests | `DP 05 - Builder.md` |
| Factory method | `EventIds` per command (`partition-offset-n`), id generator behind an interface (ULID) | J3, J9 | `DP 04 - Factory Method and Abstract Factory.md` |
| Facade | One gateway service in front of validation + Kafka send | J9 | `DP 09 - Facade.md` |
| Proxy (know it, avoid it) | Spring `@Transactional` proxies: fine in the persistor, **banned in matching** | J8 | `DP 08 - Proxy.md`, `../spring-internals/SI 10 - Proxies and AOP.md` |

Not planned (say why if it comes up): Singleton by hand (Spring beans are singletons already), Observer/event bus inside the engine (events go to Kafka), Chain of Responsibility for validation (a short ordered list of checks is simpler until it grows).

## 2. DRY, KISS, YAGNI

- **DRY = one place for one piece of knowledge.** Tick conversion, topic names (`Topics`), event ids (`partition-offset-n`), validation limits live in one class each. Duplicated *knowledge* is the problem; two similar lines that mean different things are fine.
- **Where a class lives** (BF-02 decision 21): used by more than one app → `zetax-common` (plain records/enums, **no Spring, no JPA**: Kafka messages, enums, shared DB data records); used by one app → that app (REST DTOs in the gateway, the engine's mutable book order). Don't duplicate shared classes across apps.
- **Data access:** only `zetax-persistor` writes; `zetax-gateway` only reads; `zetax-engine` never touches the DB. JDBC (`JdbcClient`), not JPA. Schema only in `zetax-db/` migrations.
- **KISS:** the simplest code that passes the tests. No reflection, no clever generics, no streams that need a comment to understand. In the matching hot path a plain loop is fine.
- **YAGNI:** no "for later" interfaces, flags, config options or abstract base classes. Add them when the second use arrives.

## 3. Java 21 style

- Value types as **records** (commands, events, DB data records); validate always-true rules in the compact constructor (no nulls, qty > 0, price > 0). Rules that need data (tick, min/max qty) belong in the gateway; rules that need the book in the engine (decision 19). The engine's book order is mutable by design (it fills), so it's a class, not a record.
- Closed sets as **enums** or **sealed interfaces** + pattern-matching `switch` (no `default` branch on sealed types, so the compiler finds missing cases).
- Immutability by default: `final` fields, `List.copyOf`, no setters on domain objects.
- Constructor injection only (no `@Autowired` on fields); one constructor → no annotation needed.
- `Optional` only as a return type, never as a field or parameter.
- Money / prices / quantities: `long` ticks. Never `double`/`float`, never `BigDecimal` in the engine hot path.
- Logging: SLF4J with placeholders, message starts with the method name (section 5b): `log.debug("place -> {} rests at {}", id, priceTicks)`. No `System.out`. No logging inside the per-order matching loop at INFO.
- Exceptions: specific types (`OrderRejectedException`), never `catch (Exception e)` to hide errors. Business rejections in the engine are **events** (`OrderRejected`), not exceptions.

## 4. Common Sonar issues to avoid

| Rule | Problem | Do instead |
|---|---|---|
| `java:S106` | `System.out` / `System.err` | SLF4J logger |
| `java:S112` | Throwing generic `RuntimeException` / `Exception` | A specific exception type |
| `java:S125` | Commented-out code | Delete it (git keeps history) |
| `java:S1068` · `S1172` · `S1481` | Unused private field · unused parameter · unused local variable | Remove |
| `java:S1186` | Empty method without a comment | Implement, or comment why it's empty |
| `java:S1192` | Same string literal repeated 3+ times | A constant (e.g. in `Topics`, error codes enum) |
| `java:S2095` | Resource not closed | try-with-resources |
| `java:S2142` | `InterruptedException` swallowed | `Thread.currentThread().interrupt()` then exit/rethrow (engine threads!) |
| `java:S3776` | Cognitive complexity too high | Extract methods; guard clauses; early returns |
| `java:S1135` | `TODO` left in code | Finish it or move it to the backlog with an ID |
| `java:S2699` | Test without assertion | Assert the outcome |
| `java:S2925` | `Thread.sleep` in tests | Awaitility / latch with timeout |
| `java:S5786` | `public` JUnit 5 test class/method | Package-private |

Also: no magic numbers outside constants/config (except 0, 1 and test data), no wildcard imports, methods ≲ 20–30 lines, classes with one clear job, no nested ternaries.

## 5. ZetaX rules that beat "clean code" advice

- **No Spring, Kafka or DB in `engine.book`.** Even if a framework feature would be "cleaner".
- **No `@Transactional`, `@Async` or other proxies in matching code.**
- **Determinism beats convenience:** no `Instant.now()`, random, or UUIDs inside the engine; time = Kafka record timestamp.
- **Performance in the hot path:** avoid allocating streams/lambdas per order inside the matching loop if a benchmark shows it matters; elsewhere prefer readability.

## 5b. Logging format (decided 2026-11-19)

- One logger per class: `private static final Logger log = LoggerFactory.getLogger(OrderBook.class);`
- Every message starts with **`<methodName> -> `**; the class name comes automatically from the logger (Logback pattern `%logger{0} : %msg`):
  `log.debug("place -> {} rests {} @ {}", orderId, remaining, priceTicks);` → `OrderBook : place -> order_01JD… rests 40 @ 10100`
- Never `%method` / `%M` / caller data in the pattern: it walks the stack on every line (too slow for the engine).
- SLF4J placeholders (`{}`), no string concatenation; `debug` on the hot path; no secrets or personal data in logs.

## 6. Review checklist (before proposing code or a commit)

- [ ] Each class has one responsibility; names say what it does (`PriceLevel`, not `Helper`/`Utils`/`Manager`).
- [ ] Dependencies point inward: `book` ← `runner`/`api`/`persistence`, never the other way.
- [ ] No duplicated knowledge (ticks, topics, ids, limits); shared contracts in `zetax-common`.
- [ ] No unused code, no commented-out code, no `TODO` without a backlog ID.
- [ ] Errors: specific exceptions, nothing swallowed, `InterruptedException` handled correctly.
- [ ] Records / enums / sealed types used where they fit; fields `final`.
- [ ] No `double` for money; no wall-clock time or randomness in the engine.
- [ ] Logging via SLF4J with placeholders; message starts with `<methodName> -> ` (5b); no secrets in logs.
- [ ] Tests cover the new behaviour (`java-unit-tests` skill) and pass.
- [ ] SonarQube for IDE: no open issues on the changed files (checked after every function, again before the commit).
- [ ] SonarQube for IDE shows no **new** issues on changed files; `mvn verify` passes with no new warnings.
