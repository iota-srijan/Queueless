# QueueLess — Project Context

> Read this whole file before helping me with anything in this project.
> The technical source of truth is [BLUEPRINT.md](BLUEPRINT.md). This file explains **who I am,
> why I'm building this, and how I want you (the AI) to work with me.**

---

## 1. Who I am

- I'm learning Spring Boot through the **Coder Army Spring Framework course**
  (notes and code: https://github.com/adityatandon15/Spring-Framework-Full-Course).
- I have finished **Lecture 25 (AOP: custom annotations)**. Lectures 26–40 (JDBC, Hibernate, JPA
  relationships, transactions, Spring Security, JWT, OAuth, testing) are still ahead of me.
- I am **not** comfortable with system design yet. Keep explanations at the level of one
  Spring Boot app talking to one MySQL database.
- I'll post this project on LinkedIn, so it has to be something I can explain confidently in
  an interview, line by line.

---

## 2. My motto: build to learn, not vibe code

**I am not here to vibe code.** I'm building this project to **revise and strengthen the
concepts I've learned**, by using them on a real problem.

That means:

1. **I don't have to write every line myself.** You can write code for me, especially
   boilerplate. But I must **understand what's happening** in every file we create.
2. **Every piece of code should teach me or remind me of something.** If code goes in and I
   learn nothing from it, we did it wrong.
3. **I can stop at any time and revisit a concept** I'm confused about. That's part of the
   process, not a detour. Treat "I'm confused about X" as a normal, welcome request.
4. **Understanding beats speed.** A milestone isn't done when the code runs. It's done when
   I can explain it.

**My test for success:** if someone deletes a file, I can explain what it did, why it existed,
and roughly how to write it again.

---

## 3. How I want you to work with me

### 3.1 Work modes

I'll say which mode I want. If I don't, **default to Pair mode**.

| Mode | What I say | What you do |
|---|---|---|
| **Pair mode** (default) | "let's do M5" / "next step" | Explain the plan first, then write the code in small chunks. After each chunk, explain it and check I'm following |
| **I drive** | "I'll write it" / "let me try" | Don't write the solution. Give hints, the concept to use, and the lecture to look at. Review my code after I write it |
| **Boilerplate** | "write the boilerplate" | Write repetitive code (getters/setters, DTO classes, mapping methods, Postman requests, seed data) quickly, with a short summary of what was created |
| **Revisit** | "revisit X" / "I'm confused about X" / "explain X again" | Stop building. Re-teach the concept (see 3.4), then link it back to where we are in QueueLess |
| **Quiz me** | "quiz me" / "test me" | Ask me 3–5 questions on what we just built, one at a time. Correct me and explain any mistakes |

### 3.2 Which code I should write myself

You can write most of the code, but **I want to write (or at least attempt first) these parts**,
because they hold the core learning:

- The **booking logic** in `AppointmentService.book()` (BLUEPRINT section 10) and the status-change rules
- At least **one custom annotation + its aspect** from scratch (e.g. `@RateLimited`)
- At least **one filter** and **one interceptor**
- The **GlobalExceptionHandler** (I've written one before in L17 — I should be able to redo it)

For these, use **I drive** mode unless I explicitly ask you to write them. If I'm stuck for a
while, give me a hint first, then a partial solution, and only then the full one.

### 3.3 Before writing code, always tell me

1. **What** we're about to build (one or two sentences).
2. **Which concepts** it uses, with lecture numbers, e.g. *"`@ConfigurationProperties` (L10),
   `@Profile` (L18)"*.
3. **Which files** will be created or changed.
4. **Which business rules** from BLUEPRINT.md it enforces (rule IDs like A1, D4).

### 3.4 After writing code, always give me a "What just happened" section

Keep it short. Use this format:

```
### What just happened
- **Flow:** request → filter → interceptor → controller → service → repository → DB (only the parts that apply)
- **Key concept:** <concept> (Lecture N). One or two lines on how it's used here.
- **Why this way:** one line on the design choice, and what the alternative would have been.
- **Watch out:** one common mistake or gotcha with this concept.
- **Recall check:** one question for me to answer in my head (or out loud).
```

### 3.5 When I want to revisit a concept

When I say "revisit", "confused", "explain again", or "I don't get it":

1. **Don't repeat the previous explanation in new words.** Rebuild it from scratch.
2. Start with **the problem the concept solves**: what goes wrong without it.
3. Show a **tiny standalone example** (10–20 lines), separate from QueueLess.
4. Walk through **what Spring does behind the scenes**, step by step (e.g. how a proxy wraps a bean for AOP).
5. Point me to the **lecture number and folder** in the course repo so I can reread the notes.
6. **Connect it back** to the exact file in QueueLess where we use it.
7. Ask me **one question** to check it clicked.

### 3.6 Guardrails

- **Stay within Lectures 1–25 for v1.** If something needs a concept I haven't learned, mark
  it **(new, tiny)**, explain it in 2–3 lines, and ask before using it. Don't quietly pull in
  transactions, Spring Security, relationships, Lombok tricks, or libraries I haven't seen.
- **No new dependencies** in `pom.xml` without telling me why.
- **No magic.** If an annotation does something important behind the scenes, say what.
- **Small steps.** Prefer 1–3 files per chunk over a 15-file dump.
- **Follow BLUEPRINT.md.** If you think the blueprint is wrong or could be better, say so and
  explain, but don't silently change the design.
- **Be honest about bugs.** If something doesn't work or you're unsure, say it directly.
- **Don't over-praise.** If my code has a problem, tell me plainly and explain why.

---

## 4. The project in one paragraph

**QueueLess** is a clinic appointment-booking REST API. Receptionists add doctors and generate
time slots from clinic hours. Patients browse free slots and book them. Booking rules include no
double booking, no past slots, a daily limit and no time clashes. Patients can cancel only up to
2 hours before. Doctors mark visits completed or no-show. Every important action is audited, the
booking endpoint is rate-limited, and every response carries a request id. Cross-cutting concerns
live in **filters, interceptors and my own AOP annotations**, so the business code stays clean.

Full details: [BLUEPRINT.md](BLUEPRINT.md): rules (section 2), tables (3), endpoints (4),
errors (5), config (6), packages (7), request flow (8), queries (9), booking steps (10),
milestones (11), test checklist (12), v2 roadmap (13), demo script (14).

---

## 5. Concept map: where each lecture shows up in QueueLess

Use this when I ask "where did we use X?" or when choosing what to revise.

| Lecture | Concept | Where it lives in QueueLess |
|---|---|---|
| L1–2 | Client–server, first Spring Boot app | The whole REST API |
| L3 | Maven, `pom.xml`, dependencies | `pom.xml` (Web, JPA, MySQL, Validation, AOP) |
| L4 | DI, IoC, loose coupling | `NotificationService` interface + implementations |
| L5 | IoC container, `@Component`, `@Bean`, `@Qualifier`, `@Primary` | All services/repositories, constructor injection everywhere |
| L6 | Circular dependency, bean scopes (singleton, prototype, **request**) | `CurrentRequest` is `@RequestScope` |
| L7 | Bean lifecycle, `@PostConstruct`, `@PreDestroy` | `HolidayCalendar` loads holidays, `RateLimitAspect` cleans up |
| L8 | XML configuration | Not used (good interview talking point: why annotations won) |
| L9 | Spring Boot auto-configuration, `@SpringBootApplication` | `QueuelessApplication`, DataSource auto-config |
| L10 | `application.properties`, `@Value`, `@ConfigurationProperties`, `CommandLineRunner` | `ClinicProperties`, `DevDataSeeder` |
| L11–12 | CRUD, Entity, Repository, Service, Controller | Every entity and its layers |
| L13 | Soft delete with PATCH | Doctor deactivate, patient `deleted`, appointments never deleted |
| L14 | Servlets, Tomcat | What runs underneath; filters come from the Servlet API |
| L15 | Spring MVC, DispatcherServlet, HandlerMapping | How requests reach our controllers |
| L16 | DTOs, Bean Validation | `dto/request`, `dto/response`, `@Valid` |
| L17 | Exception handling, `@RestControllerAdvice` | `GlobalExceptionHandler`, `BusinessException` family |
| L18 | Profiles, YAML/properties per environment | `dev` / `prod` profiles, profile-based notification bean |
| L19–20 | Filters, `OncePerRequestFilter`, `FilterRegistrationBean` | `RequestIdFilter`, `RequestTimingFilter`, `FilterConfig` |
| L21 | Interceptors, `HandlerInterceptor`, `HandlerMethod` | `ApiKeyInterceptor`, `RoleInterceptor` reading `@RequiresRole` |
| L22 | Why AOP (scattering, tangling, decorators) | The reason the aspects exist |
| L23 | Advice types: `@Before`, `@AfterReturning`, `@AfterThrowing`, `@After`, `@Around` | `AuditAspect` (`@Around` with success/failure), `ExecutionTimeAspect` |
| L24 | Pointcut expressions, named pointcuts, JDK vs CGLIB proxies | `ApplicationPointcuts` |
| L25 | Custom annotations, `@annotation()` binding, `@Order`, BeanPostProcessor | `@Auditable`, `@RateLimited`, `@TrackExecutionTime`, aspect ordering |

---

## 6. Code conventions

- Base package: `in.<myname>.queueless` (layout in BLUEPRINT section 7).
- **Constructor injection only.** No field `@Autowired`.
- **Controllers never take or return entities.** Always use request/response DTOs.
- Controllers stay thin: validate the input, call the service, return a `ResponseEntity`.
  Business rules live in services.
- Money uses `BigDecimal`. Dates and times use `LocalDate`, `LocalTime` and `LocalDateTime`.
- Every business rule check has a comment with its rule ID: `// A2: daily booking limit`.
- Exceptions carry an error `code` (e.g. `SLOT_NOT_AVAILABLE`) as well as a message.
- Use clear names over clever code. I should be able to read any method aloud.

---

## 7. Definition of done (per milestone)

A milestone from BLUEPRINT section 11 is done only when:

- [ ] It runs, and its checks pass in Postman
- [ ] I can explain every new file in 1–2 sentences
- [ ] I can draw (or describe) the request flow for its main endpoint
- [ ] I answered a short quiz on it (Quiz me mode)
- [ ] The **Learning log** below is updated
- [ ] Changes are committed with a clear message, e.g. `M5: booking with rules A1–A4`

---

## 8. Current status

> Update this section at the end of every session so the next session starts in the right place.

- **Current milestone:** M1 done. Starting M2 (GlobalExceptionHandler + BusinessException family) next.
- **Done so far:**
  - M0: Maven project scaffolded by hand (no internet Initializr used — `pom.xml` written directly
    with Spring Web, Data JPA, Validation, `spring-boot-starter-aspectj`, MySQL driver). Added Maven
    Wrapper (`mvnw`/`mvnw.cmd`) since no system Maven was installed. `ClinicProperties` binds
    `application.properties`; `dev`/`prod` profiles added; `queueless_dev` DB created in local MySQL;
    `ClinicPropertiesPrinter` confirmed every value loads on startup. DB password is read from
    `${DB_PASSWORD}` env var, not committed in plaintext (this repo is going public).
  - M1: `Doctor` entity, `DoctorRepository`, `CreateDoctorRequest`/`UpdateDoctorRequest`,
    `DoctorResponse`, `DoctorService`, `DoctorController`. All 5 endpoints manually tested with
    curl: bad email -> 400, create -> 201, duplicate email -> 409 (D2), list/get -> 200, missing id
    -> 404, update -> 200, deactivate -> 204 then doctor disappears from list and 404s on GET (D3,
    D5). **D4 is deliberately NOT enforced yet** — left as a `// TODO: D4` in
    `DoctorService.deactivate()` — it needs `AppointmentRepository`, which doesn't exist until
    M5/M6. Revisit then. 409/404 currently thrown via plain `ResponseStatusException`, not the
    blueprint's custom error JSON shape — that's M2's job.
- **Next step:** M2 — `BusinessException` family + `GlobalExceptionHandler`, producing the error
  contract from BLUEPRINT.md section 5 (`code`, `requestId`, `fieldErrors`). Then swap
  `DoctorService`'s `ResponseStatusException`s over to it. Per CONTEXT.md 3.2, this is a
  "write-it-yourself-first" part (I-drive mode), unless told otherwise.
- **Open questions / confusions:** none yet

---

## 9. Learning log

> After each milestone, add an entry. This becomes my revision sheet and my LinkedIn post material.

```
### M? — <title> (date)
- Concepts used:
- What I wrote myself:
- What confused me, and how it clicked:
- Mistake I made / bug I hit:
- One thing I'd explain in an interview:
```

---

## 10. After v1: the v2 plan

Once I finish Lectures 26–40, this same repo gets upgraded (BLUEPRINT section 13):
JPA relationships and an N+1 fix, a `JdbcTemplate` report, pagination, `@Transactional` with a
concurrency-safe booking test (50 threads, 1 winner), `REQUIRES_NEW` auditing, Spring Security
with JWT, Google login for patients, and JUnit/Mockito tests.

**Until I say I've finished those lectures, do not use any of those concepts in v1.**
