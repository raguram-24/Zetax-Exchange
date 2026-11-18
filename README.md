# ZetaX Exchange

> A Kafka-backed order matching engine with price-time priority, built with Spring Boot, PostgreSQL and React.

🚧 Work in progress (portfolio project).

## Modules
One repo, several apps. Java modules are built by Maven; the Go service and the web app have their own builds.

| Module | What it is |
|---|---|
| `zetax-common` | Shared contracts: commands, events, enums, topic names (plain Java) |
| `zetax-engine` | **Stateful** matching engine: order books in memory, one thread per Kafka partition, replay on start (port 8081) |
| `zetax-gateway` | **Stateless** gateway: REST order + admin API, WebSocket market data, Postgres writer (port 8080) |
| `zetax-marketdata` | **Go** market-data service: live book view, trades, candles over WebSocket + REST (port 8082). 🚧 Planned, see [its README](zetax-marketdata/README.md) |
| `zetax-web` | React + TypeScript trading screen (port 5173 in dev). See [its README](zetax-web/README.md) |

```
React ──REST──► gateway ──commands──► Kafka ──► engine
React ◄──WS──── gateway ◄──trades──── Kafka ◄── engine
                gateway ──► PostgreSQL
```

## Run locally
Requirements: Java 21, Maven 3.9+, Docker (OrbStack works).

```bash
docker compose up -d                 # Kafka + topics + Postgres
mvn verify                           # build + tests
mvn -pl zetax-engine spring-boot:run
mvn -pl zetax-gateway spring-boot:run
```
Health: http://localhost:8081/actuator/health (engine) · http://localhost:8080/actuator/health (gateway)

Optional: add the Maven wrapper once with `mvn wrapper:wrapper`, then use `./mvnw`.

## Docs
- [Glossary of trading terms](docs/GLOSSARY.md)
- [Business learning progress](docs/PROGRESS_BUSINESS_LEARNING.md)
