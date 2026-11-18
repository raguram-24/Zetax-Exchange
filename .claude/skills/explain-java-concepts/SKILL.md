---
name: explain-java-concepts
description: Use when teaching or explaining a Java, JVM, concurrency, Kafka-client or Spring concept to Raguram while building ZetaX - e.g. Java memory model, happens-before, threads, SmartLifecycle, bean lifecycle, @Transactional proxies, records/sealed types, TreeMap, JMH. He is a strong Java/Spring developer, so teach the "why" and the internals, not syntax.
---

# Explain Java / Spring / concurrency concepts

Raguram writes Java and Spring every day. He doesn't need syntax. He needs: **how it works underneath, why it matters for a matching engine, and where his everyday Spring habits are wrong here.** Teach just in time, only what the current step needs.

## How to explain
1. **Check what's known:** skip anything already in `docs/java/LEARNING_LOG.md`.
2. **Start from what he already uses:** "You know `@Transactional` works. Here is *how*: a CGLIB proxy wraps your bean…"
3. **Tiny standalone example** (≤ 15 lines, not project code), then the ZetaX code.
4. **Show the failure:** what breaks if you get it wrong (a race, a lost update, a rebalance, a self-invocation that skips the proxy). A concrete broken run beats a rule.
5. **Habit vs engine reality:** say it out loud when a normal web-app habit is wrong in the engine (see table).
6. **Cite the source** for details that matter: JLS ch. 17 (memory model), JDK Javadoc, Spring Framework / Boot reference, Kafka client Javadoc. Unsure → say so and point to the docs. Don't guess version-specific behaviour (Spring Boot 4 / Framework 7, Java 21).
7. **Interview angle (always):** this project is also Raguram's **Java / Spring Boot interview prep**. For each concept give:
   - the question an interviewer would actually ask (e.g. "How does `@Transactional` work? Why doesn't it work on a self-call?"),
   - a crisp 30-second answer, then the deeper answer for "senior" follow-ups,
   - the **ZetaX story** he can tell ("In my matching engine I avoided locks by confining each book to one thread because…"),
   - the common wrong answer (trap).
   These go into `docs/java/INTERVIEW.md` at step close, in his words.
8. **End with one check question** that needs reasoning, e.g. "The engine thread updates `lastOffset` and an Actuator thread reads it. Without `volatile`, what can the reader see?"

## Habit → engine reality (point out when it shows up)
| Web-app habit | In the ZetaX engine `book` / `runner` |
|---|---|
| Add `synchronized` / a lock when threads share data | Don't share. One thread owns each partition's books (thread confinement / single writer). No locks needed. |
| `@Transactional`, `@Async`, `@Cacheable` | Banned in matching: proxies, hidden threads, hidden state. Fine in `zetax-persistor` (DB writes) and the gateway. |
| `System.currentTimeMillis()`, `UUID.randomUUID()` | Breaks replay in the engine. Time = Kafka record timestamp; ids = `partition-offset-n` (`trade_` + it for trades). Random ids only in the gateway (`order_` + ULID), which is never replayed. |
| `@KafkaListener` + consumer group | Engine uses a plain `KafkaConsumer` with `assign()`. No rebalancing of in-memory books. |
| `double` for amounts | `long` ticks. |
| Throw and let the framework return 500 | Engine thread must never die on a bad command: emit `OrderRejected` and continue (B6). |
| Spring bean everywhere | `book` is plain Java: `new OrderBook()` in tests, no context. |
| Allocate freely, GC will cope | Hot path: watch allocations; measure with JMH before optimising. |

## Concurrency topics (when the step reaches them)
Teach in this order, each with a small broken-then-fixed example:
thread confinement → Java Memory Model & happens-before (JLS 17.4) → `volatile` vs `synchronized` vs atomics → safe publication (final fields, immutable records) → `ExecutorService` / thread lifecycle / interrupts → `KafkaConsumer` is not thread-safe (`wakeup()` is the one exception) → graceful shutdown → virtual threads (what they're for, why a CPU-bound engine loop doesn't gain) → false sharing / cache lines (benchmark step only).

## Spring topics (when the step reaches them)
`ApplicationContext` startup order → bean lifecycle (`@PostConstruct`, `SmartLifecycle` phases, `stop(Runnable)`) → `@ConfigurationProperties` (records) → how proxies work (JDK vs CGLIB, self-invocation trap) → Spring MVC request path (`DispatcherServlet` → handler → `HttpMessageConverter`) → Bean Validation → `ProblemDetail` / `@RestControllerAdvice` → `KafkaTemplate` + `CompletableFuture` → test slices (`@WebMvcTest`, `@DataJdbcTest`/`@JdbcTest`) and Testcontainers `@ServiceConnection`.

## Style
Simple English, short sentences. Connect every concept to a ZetaX rule ("this is why the engine is deterministic"). Record new items for the log; the quick-reference rows go into `docs/java/LEARNING.md` at step close.
