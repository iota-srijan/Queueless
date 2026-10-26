# QueueLess — Full Roadmap (L1–L40)

One place to track progress across the whole project: v1 (Lectures 1–25, this repo today)
and v2 (Lectures 26–40, the planned upgrade — same repo, not a rewrite).

Milestone details, rule ids, and endpoints: [BLUEPRINT.md](BLUEPRINT.md).
Session-to-session state (what's done, what's next, open questions): [CONTEXT.md](CONTEXT.md).

> **Note on numbering:** M0–M11 are the blueprint's own v1 milestones (BLUEPRINT.md §11).
> M12–M20 are *my own grouping* of the v2 limitations table (BLUEPRINT.md §13) into
> milestone-shaped chunks for tracking purposes — not something the blueprint itself numbered.
> Feel free to reorder/split these once you're actually in L26–40.

---

## Part 1 — v1 (Lectures 1–25)

| # | Milestone | Lecture(s) | Status |
|---|---|---|---|
| M0 | Project setup, MySQL DB, profiles, `ClinicProperties` | L3, L9, L10, L18 | ✅ Done |
| M1 | Doctors: entity, DTOs, validation, service, controller, soft delete (D1–D5\*) | L11–13, L16 | ✅ Done (D4 deferred — see note) |
| M2 | `GlobalExceptionHandler` + `BusinessException` family + error JSON | L17 | ✅ Done |
| M3 | Patients: register, get (P1–P2\*\*) | L11–13, L16 | ✅ Done (P2 deferred, see note) |
| M4 | `HolidayCalendar` + slot generation + list + block (S1–S4) | L7, L11–12 | 🔶 Next |
| M5 | **Booking** — core rules A1–A4 | L11–12, L16 | ⬜ Not started |
| M6 | Cancel / complete / no-show + status transition rules (A5–A8) | L11–12 | ⬜ Not started |
| M7 | Notifications with profiles (A11) + `DevDataSeeder` | L4, L7, L10, L18 | ⬜ Not started |
| M8 | `RequestIdFilter`, `RequestTimingFilter` | L14, L19–20 | ⬜ Not started |
| M9 | Interceptors, `@RequiresRole`, `CurrentRequest`, ownership checks (P2, A8) | L6, L15, L21 | ⬜ Not started |
| M10 | Aspects: `@RateLimited` (A9), `@Auditable` (A10), `@TrackExecutionTime`, `@Order`, audit endpoint | L22–25 | ⬜ Not started |
| M11 | README, Postman collection, demo video | — | 🔶 README started |

\* D4 ("can't deactivate a doctor with a future booked appointment") needs `AppointmentRepository`,
which doesn't exist until M5/M6. Left as a `TODO` in `DoctorService.deactivate()`; will be wired
in once appointments exist.

\*\* P2 ("patient reads own profile only, receptionist reads any, else 403") needs
`CurrentRequest`/`RoleInterceptor`, which don't exist until M9. `GET /api/patients/{id}` is open
to everyone for now — `TODO` in `PatientService.getById()`. `GET /api/patients/{id}/appointments`
(also P2) isn't built at all yet — needs `Appointment`, which lands in M5/M6.

### v1 lecture map (L1–25)

| Lecture | Concept | Where it lives in QueueLess |
|---|---|---|
| L1–2 | Client–server, first Spring Boot app | The whole REST API |
| L3 | Maven, `pom.xml` | `pom.xml` |
| L4 | DI, IoC, loose coupling | `NotificationService` interface (M7) |
| L5 | IoC container, `@Component`/`@Bean`/`@Qualifier`/`@Primary` | All services/repositories |
| L6 | Bean scopes, circular dependency | `CurrentRequest` `@RequestScope` (M9) |
| L7 | Bean lifecycle, `@PostConstruct`/`@PreDestroy` | `HolidayCalendar` (M4), `RateLimitAspect` (M10) |
| L8 | XML configuration | Not used — interview talking point for why annotations won |
| L9 | Auto-configuration, `@SpringBootApplication` | `QueuelessApplication` ✅ |
| L10 | `application.properties`, `@ConfigurationProperties`, `CommandLineRunner` | `ClinicProperties` ✅, `DevDataSeeder` (M7) |
| L11–12 | CRUD: Entity, Repository, Service, Controller | Every entity (Doctor ✅, Patient, Slot, Appointment) |
| L13 | Soft delete via PATCH | Doctor `active` ✅, Patient `deleted` (M3) |
| L14 | Servlets, Tomcat | Underlying runtime for filters (M8) |
| L15 | Spring MVC, DispatcherServlet | How every request reaches a controller |
| L16 | DTOs, Bean Validation | `dto/request`, `dto/response`, `@Valid` ✅ |
| L17 | Exception handling, `@RestControllerAdvice` | `GlobalExceptionHandler` ✅ |
| L18 | Profiles | `dev`/`prod` ✅ |
| L19–20 | Filters, `OncePerRequestFilter` | `RequestIdFilter`, `RequestTimingFilter` (M8) |
| L21 | Interceptors, `HandlerInterceptor` | `ApiKeyInterceptor`, `RoleInterceptor` (M9) |
| L22 | Why AOP | Motivates the whole aspect/ package (M10) |
| L23 | Advice types (`@Before`/`@Around`/etc.) | `AuditAspect`, `ExecutionTimeAspect` (M10) |
| L24 | Pointcut expressions, JDK vs CGLIB proxies | `ApplicationPointcuts` (M10) |
| L25 | Custom annotations, `@Order` | `@Auditable`, `@RateLimited`, `@TrackExecutionTime` (M10) |

---

## Part 2 — v2 (Lectures 26–40)

**Do not start any of this until v1 (M0–M11) is finished and L26–40 are actually watched** —
per CONTEXT.md's own guardrail.

| # | Milestone | Lecture(s) | What it fixes (BLUEPRINT §13) |
|---|---|---|---|
| M12 | JPA relationships: `@ManyToOne`/`@OneToMany` + N+1 fix | L30, L31 | Plain `Long` ids / copied columns → real joins; `@EntityGraph` against N+1 |
| M13 | `JdbcTemplate` report (daily no-show / bookings-per-doctor) | L27 | JPA is awkward for aggregation reports |
| M14 | Pagination: `Pageable`, `Page<T>`, sorting | L32 | Lists currently return everything |
| M15 | `@Transactional` safe booking | L33 | Booking does slot-update + appointment-insert with no transaction |
| M16 | Concurrency-safe booking: isolation + DB unique constraint, proven with a 50-thread test | L34 | Two patients can race for the same slot and both "win" |
| M17 | Audit in `Propagation.REQUIRES_NEW` | L34 | A failed booking's audit row would roll back along with the booking once transactions exist |
| M18 | Spring Security + BCrypt + JWT | L35–38 | Fake header-based auth — anyone can claim any role |
| M19 | Google OIDC login for patients | L39 | Extends the real-auth story from M18 |
| M20 | JUnit + Mockito + `@WebMvcTest` test suite | L40 | No automated tests — rules can silently break |

### v2 lecture map (L26–40)

| Lecture | Concept | Where it lands in QueueLess |
|---|---|---|
| L26 | Plain JDBC (`jdbcDemo` in the course repo) — `DriverManager`, raw SQL, before `JdbcTemplate` wraps it | Groundwork for M13; nothing in QueueLess uses raw JDBC directly, `JdbcTemplate` (L27) is the layer actually used |
| L27 | `JdbcTemplate` | M13 — report queries |
| L28 | Hibernate basics (`hibernateDemo`) — Hibernate as a JPA provider, session, `SessionFactory` | Explains the ORM layer `spring-boot-starter-data-jpa` has been using since M1 — good interview context for "what is Hibernate vs JPA" |
| L29 | Hibernate internals (`hibernateInternalsDemo`) — first-level cache, dirty checking, entity lifecycle states | Explains *why* `doctorRepository.save(doctor)` after mutating a managed entity works the way it does; relevant once `@Transactional` (M15) makes session boundaries matter |
| L30 | JPA relationships (`@ManyToOne`/`@OneToMany`/`@OneToOne`) | M12 |
| L31 | `@EntityGraph`, N+1 problem | M12 |
| L32 | `Pageable`, `Page<T>`, sorting | M14 |
| L33 | `@Transactional` | M15 |
| L34 | Transaction isolation, concurrency, `Propagation.REQUIRES_NEW` | M16, M17 |
| L35–38 | Spring Security, BCrypt, JWT | M18 |
| L39 | Google OAuth / OIDC login | M19 |
| L40 | JUnit, Mockito, `@WebMvcTest` | M20 |

L26, L28, L29 confirmed directly from the course repo's own folder names
(`jdbcDemo`, `hibernateDemo`, `hibernateInternalsDemo` — github.com/adityatandon15/Spring-Framework-Full-Course),
not guessed. Exact in-video explanations may go deeper than the one-line summaries above —
update this table with specifics once you've actually watched them.

---

## How to use this file

- Flip a milestone's status to ✅ in the v1 table as each one is actually tested against
  BLUEPRINT §12's checklist — not just "code compiles."
- After finishing a v1 milestone, also update **CONTEXT.md §8 (Current status)** — that's the
  file read at the start of every session, this one is just the big-picture tracker.
- When you reach v2: rename each `(not yet mapped)` row once you know what that lecture covers,
  and re-check whether my M12–M20 grouping still makes sense once you see the real lecture order.
