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
- Logging: SLF4J with placeholders: `log.info("Order {} rested at {}", id, priceTicks)`. No `System.out`. No logging inside the per-order matching loop at INFO.
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

## 6. Review checklist (before proposing code or a commit)

- [ ] Each class has one responsibility; names say what it does (`PriceLevel`, not `Helper`/`Utils`/`Manager`).
- [ ] Dependencies point inward: `book` ← `runner`/`api`/`persistence`, never the other way.
- [ ] No duplicated knowledge (ticks, topics, ids, limits); shared contracts in `zetax-common`.
- [ ] No unused code, no commented-out code, no `TODO` without a backlog ID.
- [ ] Errors: specific exceptions, nothing swallowed, `InterruptedException` handled correctly.
- [ ] Records / enums / sealed types used where they fit; fields `final`.
- [ ] No `double` for money; no wall-clock time or randomness in the engine.
- [ ] Logging via SLF4J with placeholders; no secrets in logs.
- [ ] Tests cover the new behaviour (`java-unit-tests` skill) and pass.
- [ ] SonarQube for IDE shows no **new** issues on changed files; `mvn verify` passes with no new warnings.
