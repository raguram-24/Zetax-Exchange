---
name: java-unit-tests
description: Use when writing, updating or reviewing Java tests in ZetaX (zetax-common, zetax-engine, zetax-gateway, zetax-persistor, zetax-db) - unit tests for the matching book and services, randomized invariant tests, Spring test slices, Testcontainers flows, or a regression test for a bug.
---

# Java unit tests — ZetaX

Stack: Java 21 · JUnit 5 (Jupiter) · AssertJ + Mockito (come with `spring-boot-starter-test` in the Spring apps) · Testcontainers for integration and end-to-end tests (decided 2026-11-17, BF-02 decision 17; the dependency itself still gets its own approval when first added, check class names for Boot 4.1).
`zetax-common` only has `junit-jupiter`: use JUnit assertions there, or ask before adding `assertj-core`.

## 0. Before writing a test

1. Find the **rule** being tested and where it is decided: D-13 business rules, B-sections in the vault "Business and Technical Flow". Never test invented behaviour; if the rule isn't decided, ask.
2. Pick the **lowest test level** that proves it:

| What | Level | Tools |
|---|---|---|
| Matching rules in `engine.book` (pure Java) | Unit | JUnit + AssertJ, no Spring, no mocks |
| Validation, mapping, small services | Unit | JUnit + AssertJ, Mockito only for real collaborators at a boundary |
| REST controller (status, JSON, validation errors) | Slice | `@WebMvcTest` + `MockMvc`, collaborators as `@MockitoBean` |
| JSON shape of commands/events (contracts in `zetax-common`) | Contract | Golden files in `src/test/resources/golden/`: serialized JSON must match the file, and the file must still deserialize |
| SQL, `zetax-db` migrations, persistor upserts | Integration | Testcontainers Postgres (migrations applied first) |
| Kafka produce/consume, replay | Integration | Testcontainers Kafka |
| Whole app starts | Smoke | one `@SpringBootTest` per app, no more |
| A whole flow (e.g. BF-02: POST → engine → persistor → rows) | End-to-end | Testcontainers Kafka + Postgres; **1–2 per flow**, not more |

Test pyramid (BF-02 decision 17): many unit tests, some slice/integration, golden-file contract tests, very few end-to-end.

Spring Boot 4 moved some test annotations into new modules/packages: check the import against the Boot 4 docs instead of guessing.

3. Plan the test cases (names + one line each) and show them **before** writing code (CLAUDE.md: plan first, wait for OK).

## 1. Structure

- Test class mirrors the class under test: `OrderBook` → `OrderBookTest`, same package, `src/test/java`.
- Class and methods are **package-private** (no `public`): JUnit 5 doesn't need it.
- Method name says behaviour + expected result: `limitBuy_crossesBestAsk_tradesAtMakerPrice()`. Add `@DisplayName` only when the sentence needs it.
- Arrange / Act / Assert, separated by a blank line. **One behaviour per test** (several asserts about the same outcome are fine).
- Group related rules with `@Nested` classes: `LimitOrders`, `MarketOrders`, `Cancel`, `SelfTradePrevention`.
- Many inputs, same rule → `@ParameterizedTest` + `@CsvSource` / `@MethodSource`, not copy-pasted tests.
- Build test data with small helpers or builders (`buy(40).at(101).by("alice")`), not 10-line constructors repeated in every test. Keep helpers in the test class or a `testsupport` package, not in `src/main`.

## 2. Assertions

- AssertJ: `assertThat(trades).extracting(Trade::priceTicks).containsExactly(10100L, 10100L);`
- Assert **values**, not just "not null". Assert collections with `containsExactly` (order matters for price-time priority).
- Exceptions: `assertThatThrownBy(() -> ...).isInstanceOf(X.class).hasMessageContaining("...")` (or `assertThrows` in common).
- Every test has at least one assertion (or `verify` for a pure side effect).
- Failure messages explain the rule when it isn't obvious (`as("D-13: trade happens at the resting order's price")`).

## 3. ZetaX-specific rules

- **Prices are `long` ticks, quantities `long`.** Test data uses ticks (`10100` for 101.00). Never `double`.
- **Determinism:** no `System.currentTimeMillis()`, `Instant.now()`, `UUID.randomUUID()` or unseeded `Random` in tests or in engine code. Engine time = the command's Kafka record timestamp (`acceptedAt`, `executedAt`). The gateway's `order_` + ULID ids are random by design: put the generator behind a small interface so tests pass a fixed id.
- **Every D-13 rule has a test**: price-time priority, maker price, partial fill, market = IOC + `NO_LIQUIDITY` on empty book, self-trade prevention cancels the taker remainder, reduce-qty keeps priority, price change / qty increase loses it, min/max qty, price band, cancel only own + only unfilled part, cancel after fill rejected.
- **Randomized invariant test** for the book (seeded, seed printed on failure):
  - generate N random place/cancel commands, apply them,
  - after every command check: best bid < best ask (book never crossed), no level with qty ≤ 0, total traded qty of a buy = sum of its fills, no order filled more than its qty,
  - replay the same commands into a new book → identical trades and identical book (replay determinism).
- **Idempotency:** applying the same event / `eventId` twice gives the same DB state (persistor tests).
- **Data access:** a gateway integration test should prove it can't write (the `zetax_gateway` DB user is `SELECT` only).
- **Kafka/replay:** never `Thread.sleep` to wait. Use Awaitility (needs approval) or a latch with a timeout.

## 4. Mockito — use sparingly

- Don't mock what you own and can construct (the book, value objects, records). Use real objects.
- Never mock the database or Kafka to test SQL or consumers: use Testcontainers (H2 behaves differently from Postgres).
- Mock at boundaries: Kafka template, repositories, clocks (or pass a fixed `Clock`).
- Prefer `@ExtendWith(MockitoExtension.class)` + constructor injection over field injection.
- No `any()` everywhere: verify the important arguments (`verify(journal).append(argThat(c -> c.symbol().equals("ABC")))`).
- No mocking of static methods or private methods. If that seems needed, the design needs a change (see `java-clean-code`).

## 5. Bug → regression test first

1. Write a failing test that reproduces the bug (show it, get OK).
2. Run it, show it fails for the right reason.
3. Fix, run again, run the whole module.

## 6. Done

- `mvn -pl <module> test` passes; then `mvn verify` at the root.
- No ignored/disabled tests without a reason in a comment.
- No Sonar test issues (see `java-clean-code` → "Tests"): no empty tests, no `public` test modifiers needed, no `Thread.sleep`, assertions present.
- New behaviour covered; the business rule ID (D-xx) is mentioned in the test name, `@DisplayName` or a short comment where it helps.
