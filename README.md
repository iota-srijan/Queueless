# QueueLess

A clinic appointment-booking REST API, built with Spring Boot while learning the framework
lecture by lecture — every cross-cutting concern (auditing, rate limiting, timing, request
tracing) is implemented with my own AOP annotations instead of a library, so business code
stays clean.

Receptionists manage doctors and generate time slots from clinic hours. Patients browse free
slots and book them. Doctors mark visits completed or no-show. Every important action is
audited, the booking endpoint is rate-limited, and every response carries a request id.

**Full design spec:** [`BLUEPRINT.md`](BLUEPRINT.md) — business rules, tables, endpoints, error
contract, config, package layout, request flow, booking algorithm, milestones, test checklist.
**Build log / how I worked with AI on this:** [`CONTEXT.md`](CONTEXT.md).

---

## Status

**v1 in progress.** Building strictly within Lectures 1–25 of the
[Coder Army Spring course](https://github.com/adityatandon15/Spring-Framework-Full-Course) —
no transactions, relationships, or security yet, on purpose. See [Roadmap](#roadmap-v2) below
for what comes after.

| Milestone | What it covers | Status |
|---|---|---|
| M0 | Project setup, profiles, `ClinicProperties` | ✅ |
| M1 | Doctors: entity, DTOs, validation, service, controller, soft delete | ✅ |
| M2 | `GlobalExceptionHandler` + `BusinessException` family, error contract | ⬜ |
| M3 | Patients: register, get | ⬜ |
| M4 | Holiday calendar + slot generation, list, block | ⬜ |
| M5 | **Booking** (core rules A1–A4) | ⬜ |
| M6 | Cancel / complete / no-show + status transition rules | ⬜ |
| M7 | Notifications (profiles) + dev data seeder | ⬜ |
| M8 | `RequestIdFilter`, `RequestTimingFilter` | ⬜ |
| M9 | Interceptors, `@RequiresRole`, ownership checks | ⬜ |
| M10 | Custom AOP: `@RateLimited`, `@Auditable`, `@TrackExecutionTime`, audit endpoint | ⬜ |
| M11 | README, Postman collection, demo video | ⬜ |

---

## Tech stack

- Java 21, Spring Boot 4.1 (Web, Data JPA, Validation, AspectJ/AOP)
- MySQL 8
- Maven (via the included Maven Wrapper — no local Maven install needed)

## Running it

```bash
# 1. create the dev database
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS queueless_dev;"

# 2. set your MySQL password as an env var (never committed to the repo)
export DB_PASSWORD=your_mysql_password      # PowerShell: $env:DB_PASSWORD = "..."

# 3. run
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

The app starts on the `dev` profile by default (port `8080`, DB `queueless_dev`). A `prod`
profile exists for port `8081` / DB `queueless` with its own API key — see
[`BLUEPRINT.md` §6](BLUEPRINT.md#6-configuration).

## API so far

| Method & path | Who | Notes |
|---|---|---|
| `POST /api/doctors` | Receptionist | Create a doctor (duplicate email → `409`) |
| `GET /api/doctors?specialization=` | Public | List active doctors, optional filter |
| `GET /api/doctors/{id}` | Public | `404` if missing or deactivated |
| `PUT /api/doctors/{id}` | Receptionist | Update doctor details |
| `PATCH /api/doctors/{id}/deactivate` | Receptionist | Soft delete (`204`) |

Full endpoint list (as each milestone lands): [`BLUEPRINT.md` §4](BLUEPRINT.md#4-endpoints).

> **Note:** `v1` has no real authentication yet — the `x-api-key` / `x-user-role` / `x-user-id`
> header scheme described in the blueprint lands in M9. Right now every endpoint above is open.

## Why it's built this way

- **No `@Transactional`, no entity relationships, no Spring Security in v1** — deliberately
  deferred until I've covered those lectures, so every line of code here is something I can
  explain. See [Roadmap](#roadmap-v2) for exactly what's missing and why.
- **Cross-cutting concerns live outside business logic** — filters, interceptors, and four
  custom annotations (`@Auditable`, `@RateLimited`, `@TrackExecutionTime`, `@RequiresRole`) keep
  service methods free of logging/auditing/rate-limiting code (coming in M8–M10).
- **DTOs everywhere** — controllers never accept or return JPA entities.

## Roadmap (v2)

Once Lectures 26–40 are done, this same repo gets upgraded instead of rewritten:

| v1 limitation | v2 fix |
|---|---|
| Booking does two separate saves with no transaction | `@Transactional` |
| Two patients can race for the same slot | Transactions + DB unique constraint, proven with a 50-thread concurrency test |
| A failed booking's audit row would roll back too | Audit written in `Propagation.REQUIRES_NEW` |
| Plain `Long` foreign keys, no joins | `@ManyToOne`/`@OneToMany` relationships + N+1 fix with `@EntityGraph` |
| Lists return everything | `Pageable`/`Page<T>` |
| Fake header-based auth | Spring Security, BCrypt, JWT, Google OIDC login for patients |
| In-memory rate limiter | Discussed; Redis is the real-world answer (see the course's GateKeeper project) |
| No automated tests | JUnit + Mockito + `@WebMvcTest` |
| Reports need aggregation JPA is awkward at | `JdbcTemplate`-based report |

Full detail: [`BLUEPRINT.md` §13](BLUEPRINT.md#13-known-limitations-of-v1--your-v2-roadmap).
