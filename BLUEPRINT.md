# QueueLess v1 — Blueprint

A clinic appointment-booking REST API. Patients book time slots with doctors,
receptionists run the day, doctors mark visits done.

**Scope of v1:** only concepts from Lectures 1–25 of the Coder Army Spring course.
Anything slightly beyond that is marked **(new, tiny)**. Things v1 deliberately does
*not* do are listed at the end; they become the v2 upgrade story (Lectures 26–40).

**Stack:** Spring Boot 4.x (the course uses 4.1.0), Java 21, MySQL, Maven.
**Initializr dependencies:** Spring Web, Spring Data JPA, MySQL Driver, Validation,
AspectJ (AOP), plus optional Spring Boot DevTools and Lombok (the course writes getters by hand
until L30, either is fine). In Boot 4 the AOP starter is `spring-boot-starter-aspectj`
(it was `spring-boot-starter-aop` in Boot 3). If Initializr doesn't list it, add it to `pom.xml` by hand.

---

## 1. Actors

| Actor | Who | What they can do |
|---|---|---|
| `PATIENT` | Anyone who registers | Browse doctors & free slots, book, cancel own appointment, see own history |
| `RECEPTIONIST` | Clinic staff | Manage doctors, generate/block slots, cancel any appointment, view audit log |
| `DOCTOR` | Clinic doctor | See own schedule, mark appointments completed / no-show |

**v1 has no real login.** Every protected request carries three headers:

| Header | Example | Meaning |
|---|---|---|
| `x-api-key` | `dev-secret` | Proves the request comes from the clinic's app |
| `x-user-role` | `PATIENT` | Who the caller claims to be |
| `x-user-id` | `7` | The patient's / doctor's id (receptionist: any number) |

This is intentionally fake auth (same idea as the Lecture 21 interceptors). v2 replaces it
with Spring Security + JWT — that before/after is a great part of the LinkedIn story.

---

## 2. Business rules

Write code *from this list*. Each rule has an id — mention the id in a comment where you
enforce it, and in your test checklist.

### Doctors
- **D1** A doctor has name, specialization, email (unique), phone, consultation fee, `active` flag.
- **D2** Duplicate email → `409 DUPLICATE_EMAIL`.
- **D3** Deactivating a doctor is a soft delete (`active = false`), never a real delete.
- **D4** A doctor with any future `BOOKED` appointment cannot be deactivated → `409 DOCTOR_HAS_UPCOMING_APPOINTMENTS`.
- **D5** Inactive doctors don't appear in listings and can't get new slots.

### Patients
- **P1** A patient has name, email (unique), 10-digit phone, date of birth (in the past).
- **P2** A patient may only read their own profile and appointments (receptionist can read any) → else `403`.

### Slots
- **S1** Slots are generated per doctor per date by a receptionist, using clinic hours from config.
  Example: 09:00–17:00, 15-minute slots, lunch 13:00–14:00 → **28 slots**.
- **S2** Can't generate for a past date, a clinic holiday, or a date that already has slots for that doctor → `422` / `409`.
- **S3** Slot status is one of `AVAILABLE`, `BOOKED`, `BLOCKED`.
- **S4** A receptionist can block a slot only while it is `AVAILABLE` (doctor on leave, emergency).

### Appointments (the heart of the app)
- **A1** A patient can book only an `AVAILABLE` slot whose start time is in the future.
- **A2** A patient can hold at most **N** `BOOKED` appointments on the same day (config, default 2).
- **A3** A patient can't have two `BOOKED` appointments starting at the same date & time (even with different doctors).
- **A4** Booking sets the slot to `BOOKED` and creates an appointment with status `BOOKED`.
- **A5** Allowed status changes only:
  ```
  BOOKED ──► CANCELLED
  BOOKED ──► COMPLETED   (doctor, only after slot start time)
  BOOKED ──► NO_SHOW     (doctor, only after slot start time)
  ```
  Anything else → `409 INVALID_STATUS_TRANSITION`.
- **A6** A patient can cancel their own appointment only if it starts more than **X** hours from now
  (config, default 2). A receptionist can cancel any time.
- **A7** Cancelling puts the slot back to `AVAILABLE`. The appointment row is kept (status
  `CANCELLED`, `cancelledBy`, `cancelReason`) — appointments are never deleted.
- **A8** A doctor can only complete / no-show appointments that belong to them.
- **A9** The booking endpoint allows at most **5 requests per patient per minute** → `429`.
- **A10** Every book / cancel / complete / no-show attempt — success *or* failure — is written to the audit log.
- **A11** The patient gets a notification on booking and on cancellation.

---

## 3. Tables

v1 links tables with plain `Long` id columns (`doctorId`, `patientId`) — exactly how far
Lectures 11–17 go. v2 turns these into `@ManyToOne` relationships.

### `doctors`
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | `@GeneratedValue(IDENTITY)` |
| name | VARCHAR(100) | |
| specialization | VARCHAR(60) | e.g. `Dermatology` |
| email | VARCHAR(150) | unique |
| phone | VARCHAR(10) | |
| consultation_fee | DECIMAL(10,2) | use `BigDecimal`, never `double` for money |
| active | BOOLEAN | soft delete flag (D3) |
| created_at, updated_at | DATETIME | |

### `patients`
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| name | VARCHAR(100) | |
| email | VARCHAR(150) | unique |
| phone | VARCHAR(10) | |
| date_of_birth | DATE | |
| deleted | BOOLEAN | soft delete, like L13 |
| created_at, updated_at | DATETIME | |

### `slots`
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| doctor_id | BIGINT | plain id |
| slot_date | DATE | |
| start_time | TIME | |
| end_time | TIME | |
| status | VARCHAR(20) | `AVAILABLE` / `BOOKED` / `BLOCKED` |
| created_at | DATETIME | |

**(new, tiny)** Add a unique constraint on `(doctor_id, slot_date, start_time)`:
`@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"doctorId","slotDate","startTime"}))`.
**(new, tiny)** Store enums as text: `@Enumerated(EnumType.STRING) private SlotStatus status;`

### `appointments`
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| slot_id | BIGINT | |
| doctor_id | BIGINT | copied from the slot |
| patient_id | BIGINT | from `x-user-id` |
| appointment_date | DATE | copied from the slot |
| start_time | TIME | copied from the slot |
| status | VARCHAR(20) | `BOOKED` / `CANCELLED` / `COMPLETED` / `NO_SHOW` |
| reason | VARCHAR(255) | patient's symptoms, optional |
| cancelled_by | VARCHAR(20) | `PATIENT` / `RECEPTIONIST` |
| cancel_reason | VARCHAR(255) | |
| created_at, updated_at | DATETIME | |

Why copy `doctor_id`, `appointment_date`, `start_time` from the slot? Without relationships
we can't join, and rules A2/A3 need to query by patient + date + time. Copying makes those
one derived query each. (In v2 you can discuss whether to keep or remove the copies.)

### `audit_logs`
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| action | VARCHAR(40) | `BOOK_APPOINTMENT`, `CANCEL_APPOINTMENT`, … |
| actor_role | VARCHAR(20) | from header |
| actor_id | BIGINT | from header |
| outcome | VARCHAR(10) | `SUCCESS` / `FAILURE` |
| details | VARCHAR(500) | method args summary or exception message |
| request_id | VARCHAR(36) | from `RequestIdFilter` |
| created_at | DATETIME | |

### Enums
`SlotStatus`, `AppointmentStatus`, `UserRole { PATIENT, RECEPTIONIST, DOCTOR }`.

---

## 4. Endpoints

**Public** = no headers needed. Otherwise all three headers are required.

### Doctors
| Method & path | Who | Body / params | Success | Rules |
|---|---|---|---|---|
| `POST /api/doctors` | RECEPTIONIST | `CreateDoctorRequest` | 201 | D1, D2 |
| `GET /api/doctors?specialization=` | Public | optional filter | 200 list | D5 |
| `GET /api/doctors/{id}` | Public | — | 200 | D5 |
| `PUT /api/doctors/{id}` | RECEPTIONIST | `UpdateDoctorRequest` | 200 | |
| `PATCH /api/doctors/{id}/deactivate` | RECEPTIONIST | — | 204 | D3, D4 |

### Patients
| Method & path | Who | Body / params | Success | Rules |
|---|---|---|---|---|
| `POST /api/patients` | Public (registration) | `RegisterPatientRequest` | 201 | P1 |
| `GET /api/patients/{id}` | PATIENT (self), RECEPTIONIST | — | 200 | P2 |
| `GET /api/patients/{id}/appointments` | PATIENT (self), RECEPTIONIST | — | 200 list, newest first | P2 |

### Slots
| Method & path | Who | Body / params | Success | Rules |
|---|---|---|---|---|
| `POST /api/doctors/{doctorId}/slots/generate` | RECEPTIONIST | `{ "date": "2026-11-02" }` | 201 list | S1, S2, D5 |
| `GET /api/doctors/{doctorId}/slots?date=&status=` | Public | date required | 200 list, by time | |
| `PATCH /api/slots/{id}/block` | RECEPTIONIST | — | 200 | S4 |

### Appointments
| Method & path | Who | Body / params | Success | Rules |
|---|---|---|---|---|
| `POST /api/appointments` | PATIENT | `{ "slotId": 41, "reason": "skin rash" }` | 201 | A1–A4, A9–A11 |
| `GET /api/appointments/{id}` | owner PATIENT, DOCTOR (own), RECEPTIONIST | — | 200 | P2 |
| `GET /api/doctors/{doctorId}/schedule?date=` | DOCTOR (self), RECEPTIONIST | date required | 200 list | |
| `PATCH /api/appointments/{id}/cancel` | PATIENT (own), RECEPTIONIST | `{ "reason": "..." }` | 200 | A5–A7, A10, A11 |
| `PATCH /api/appointments/{id}/complete` | DOCTOR (own) | — | 200 | A5, A8, A10 |
| `PATCH /api/appointments/{id}/no-show` | DOCTOR (own) | — | 200 | A5, A8, A10 |

### Audit
| Method & path | Who | Params | Success |
|---|---|---|---|
| `GET /api/audit-logs?action=&outcome=` | RECEPTIONIST | optional filters | 200 list, newest first |

### Example: book an appointment
```http
POST /api/appointments
x-api-key: dev-secret
x-user-role: PATIENT
x-user-id: 7
Content-Type: application/json

{ "slotId": 41, "reason": "skin rash for 3 days" }
```
```json
201 Created
X-Request-Id: 3f2c9a1e-...
{
  "appointmentId": 18,
  "doctorName": "Dr. Mehra",
  "specialization": "Dermatology",
  "date": "2026-11-02",
  "startTime": "10:15",
  "status": "BOOKED",
  "message": "Appointment booked successfully"
}
```

---

## 5. Error contract

Every error, from every layer, has the same JSON shape (extend your L17 `ExceptionResponseDto`
with `code` and `requestId`):

```json
{
  "timestamp": "2026-11-01T18:22:04",
  "status": 409,
  "error": "Conflict",
  "code": "SLOT_NOT_AVAILABLE",
  "message": "Slot 41 is already booked",
  "path": "/api/appointments",
  "requestId": "3f2c9a1e-..."
}
```
Validation errors add `"fieldErrors": { "email": "must be a valid email" }`.

| Status | `code` | Thrown when |
|---|---|---|
| 400 | `VALIDATION_FAILED` | `@Valid` fails |
| 401 | `INVALID_API_KEY` | missing / wrong `x-api-key` (interceptor) |
| 403 | `ROLE_NOT_ALLOWED` | role not allowed on this endpoint (interceptor) |
| 403 | `NOT_YOUR_RESOURCE` | P2, A6 (patient), A8 |
| 404 | `RESOURCE_NOT_FOUND` | doctor / patient / slot / appointment missing |
| 409 | `DUPLICATE_EMAIL` | D2, P1 |
| 409 | `SLOT_NOT_AVAILABLE` | A1 (slot booked or blocked) |
| 409 | `SLOTS_ALREADY_GENERATED` | S2 |
| 409 | `INVALID_STATUS_TRANSITION` | A5, S4 |
| 409 | `DOCTOR_HAS_UPCOMING_APPOINTMENTS` | D4 |
| 422 | `SLOT_IN_PAST`, `CLINIC_HOLIDAY`, `DAILY_LIMIT_REACHED`, `TIME_CLASH`, `CANCELLATION_WINDOW_PASSED` | business rules S2, A1, A2, A3, A6 |
| 429 | `TOO_MANY_REQUESTS` | A9 |
| 500 | `INTERNAL_ERROR` | anything unexpected — never leak the stack trace |

Design tip: one `BusinessException(HttpStatus status, String code, String message)` class plus a
few subclasses (`ResourceNotFoundException`, `ConflictException`, …) keeps the handler short.

---

## 6. Configuration

`application.properties` (shared):
```properties
spring.application.name=queueless
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

clinic.name=QueueLess Clinic
clinic.opening-time=09:00
clinic.closing-time=17:00
clinic.lunch-start=13:00
clinic.lunch-end=14:00
clinic.slot-duration-minutes=15
clinic.max-booked-per-day=2
clinic.cancellation-window-hours=2
clinic.holidays=2026-12-25,2027-01-26
clinic.api-key=dev-secret
```
Bind with `@ConfigurationProperties("clinic")` into a `ClinicProperties` class (L10).
Spring can convert `09:00` to `LocalTime` and the holidays list to `List<LocalDate>`.

`application-dev.properties`: port 8080, DB `queueless_dev`.
`application-prod.properties`: port 8081, DB `queueless`, a different api key.

---

## 7. Package structure

```
in.<yourname>.queueless
├── QueuelessApplication.java
├── config/          ClinicProperties, WebConfig (interceptors), FilterConfig
├── controller/      DoctorController, PatientController, SlotController,
│                    AppointmentController, AuditLogController
├── dto/request/     CreateDoctorRequest, RegisterPatientRequest, BookAppointmentRequest, ...
├── dto/response/    DoctorResponse, SlotResponse, AppointmentResponse, ErrorResponse, ...
├── entity/          Doctor, Patient, Slot, Appointment, AuditLog
├── enums/           SlotStatus, AppointmentStatus, UserRole
├── repository/      one JpaRepository per entity
├── service/         DoctorService, PatientService, SlotService, AppointmentService,
│                    AuditLogService, HolidayCalendar
├── notification/    NotificationService, ConsoleNotificationService (dev),
│                    EmailNotificationService (prod)
├── context/         CurrentRequest   (request-scoped bean)
├── filter/          RequestIdFilter, RequestTimingFilter
├── interceptor/     ApiKeyInterceptor, RoleInterceptor
├── annotation/      RequiresRole, Auditable, RateLimited, TrackExecutionTime
├── aspect/          ApplicationPointcuts, RateLimitAspect, AuditAspect, ExecutionTimeAspect
├── exception/       BusinessException + subclasses, GlobalExceptionHandler
└── seed/            DevDataSeeder
```

---

## 8. How the cross-cutting pieces fit

One request, start to finish:

```
HTTP request
  │
  ├─ RequestIdFilter        (L20)  reuse incoming X-Request-Id or create a UUID,
  │                                put it in a request attribute + response header
  ├─ RequestTimingFilter    (L19)  log "POST /api/appointments 201 in 38 ms"
  │
DispatcherServlet
  │
  ├─ ApiKeyInterceptor      (L21)  order 1: check x-api-key, else 401
  ├─ RoleInterceptor        (L21)  order 2: read @RequiresRole on the controller method
  │                                via HandlerMethod; check x-user-role, else 403;
  │                                fill the CurrentRequest bean (role, userId, requestId)
  │
Controller (@Valid DTOs)    (L16)
  │
Service method, wrapped by aspects (L22–25):
  ├─ RateLimitAspect        @Order(1)  @RateLimited  → 429 before anything else runs
  ├─ AuditAspect            @Order(2)  @Auditable    → writes SUCCESS / FAILURE row
  └─ ExecutionTimeAspect    @Order(3)  @TrackExecutionTime → logs, warns if slow
  │
Repository → MySQL
  │
GlobalExceptionHandler      (L17)  turns any exception into the error JSON
```

### Annotations to create
```java
@Target(ElementType.METHOD) @Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRole { UserRole[] value(); }            // read by RoleInterceptor

@Target(ElementType.METHOD) @Retention(RetentionPolicy.RUNTIME)
public @interface Auditable { String action(); }                  // AuditAspect, @Around

@Target(ElementType.METHOD) @Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited { int maxRequests(); int windowSeconds(); }

@Target(ElementType.METHOD) @Retention(RetentionPolicy.RUNTIME)
public @interface TrackExecutionTime { long warnAfter() default 500; String operation() default ""; }
```
Endpoints marked "Public" simply have no `@RequiresRole`, and the interceptors skip them
(`excludePathPatterns` for `POST /api/patients`; GET-only public routes can be checked by method).

### `CurrentRequest` (request-scoped bean, Lecture 6)
```java
@Component
@RequestScope
public class CurrentRequest {
    private UserRole role;
    private Long userId;
    private String requestId;
    // getters/setters
}
```
The `RoleInterceptor` fills it; services and `AuditAspect` inject it instead of passing
headers through every method. (Fallback if this feels like too much: read the headers in the
controller with `@RequestHeader` and pass them down.)

### Ordering decision (write this in your README)
`RateLimitAspect` runs *outside* `AuditAspect`, so requests rejected by the rate limit are
**not** audited — otherwise a spammer could flood your audit table. Being able to explain a
choice like this is what makes the project stand out.

### Rate limiter idea (in memory, v1)
`ConcurrentHashMap<String, Deque<Long>>` where the key is `methodName + ":" + userId` and the deque
holds request timestamps. On each call: drop timestamps older than the window, if
`size >= maxRequests` throw `TooManyRequestsException`, else add `now` and `proceed()`.
Wrap the check-and-add for one key in `synchronized` (or `map.compute(...)`).

### Profiles & notifications (L4, L18)
`NotificationService` interface with `notifyBooked(...)`, `notifyCancelled(...)`.
- `ConsoleNotificationService` `@Profile({"dev","default"})` → prints a nicely formatted message.
- `EmailNotificationService` `@Profile("prod")` → v1 just logs "would email …"
  (stretch: real email with `spring-boot-starter-mail`).

### Bean lifecycle (L7)
- `HolidayCalendar` (`@Service`): `@PostConstruct` loads `clinic.holidays` into a `Set<LocalDate>`
  and logs "Loaded 2 clinic holidays". Method: `boolean isHoliday(LocalDate)`.
- `RateLimitAspect`: `@PreDestroy` logs how many keys it tracked and clears the map.

### Dev data (L10)
`DevDataSeeder implements CommandLineRunner`, `@Profile("dev")`: if the doctors table is empty,
insert 3 doctors + 2 patients and generate slots for tomorrow. You can demo straight
after `mvn spring-boot:run`.

---

## 9. Repository methods you'll need (derived queries)

```java
// DoctorRepository
boolean existsByEmail(String email);
List<Doctor> findByActiveTrue();
List<Doctor> findBySpecializationIgnoreCaseAndActiveTrue(String specialization);
Optional<Doctor> findByIdAndActiveTrue(Long id);

// PatientRepository
boolean existsByEmail(String email);
Optional<Patient> findByIdAndDeletedFalse(Long id);

// SlotRepository
boolean existsByDoctorIdAndSlotDate(Long doctorId, LocalDate date);
List<Slot> findByDoctorIdAndSlotDateOrderByStartTime(Long doctorId, LocalDate date);
List<Slot> findByDoctorIdAndSlotDateAndStatusOrderByStartTime(Long doctorId, LocalDate date, SlotStatus status);

// AppointmentRepository
long countByPatientIdAndAppointmentDateAndStatus(Long patientId, LocalDate date, AppointmentStatus status);           // A2
boolean existsByPatientIdAndAppointmentDateAndStartTimeAndStatus(Long patientId, LocalDate date, LocalTime time, AppointmentStatus status); // A3
boolean existsByDoctorIdAndStatusAndAppointmentDateGreaterThanEqual(Long doctorId, AppointmentStatus status, LocalDate from); // D4
List<Appointment> findByPatientIdOrderByAppointmentDateDescStartTimeDesc(Long patientId);
List<Appointment> findByDoctorIdAndAppointmentDateOrderByStartTime(Long doctorId, LocalDate date);

// AuditLogRepository
List<AuditLog> findAllByOrderByCreatedAtDesc();
List<AuditLog> findByActionOrderByCreatedAtDesc(String action);
```

---

## 10. The booking method, step by step

`AppointmentService.book(BookAppointmentRequest req)` — annotated
`@RateLimited(maxRequests = 5, windowSeconds = 60)`, `@Auditable(action = "BOOK_APPOINTMENT")`,
`@TrackExecutionTime(warnAfter = 300)`.

1. `patientId = currentRequest.getUserId()`; load patient (`findByIdAndDeletedFalse`) → 404.
2. Load slot → 404.
3. Slot status must be `AVAILABLE` → else 409 `SLOT_NOT_AVAILABLE` (A1).
4. `LocalDateTime.of(slot.date, slot.startTime)` must be after now → else 422 `SLOT_IN_PAST` (A1).
5. Doctor of the slot must be active → else 409 `SLOT_NOT_AVAILABLE`.
6. Count patient's `BOOKED` that day ≥ `maxBookedPerDay` → 422 `DAILY_LIMIT_REACHED` (A2).
7. Patient already has a `BOOKED` one at that date + time → 422 `TIME_CLASH` (A3).
8. Set slot `BOOKED`, save. Create appointment (copy doctorId/date/time), save (A4).
9. `notificationService.notifyBooked(...)` (A11).
10. Map to `AppointmentResponse`, return.

Cancel, complete and no-show follow the same shape: load → check owner → check allowed
transition (A5) → check time rule → update → save → notify.

Tip: put the transition check in one place, e.g. a method on the enum
`boolean canMoveTo(AppointmentStatus next)`, so the A5 rules live in exactly one spot.

---

## 11. Build order (milestones)

Finish each milestone and test it in Postman before starting the next. Commit after each one.

| # | Milestone | Done when |
|---|---|---|
| M0 | Project setup, MySQL DB, profiles, `ClinicProperties` | App starts with `dev` profile; properties print from a `CommandLineRunner` |
| M1 | Doctors: entity, DTOs, validation, service, controller, soft delete | All 5 doctor endpoints work; bad input gives 400 with `fieldErrors` |
| M2 | `GlobalExceptionHandler` + `BusinessException` family + error JSON | Every error in Postman has the same shape |
| M3 | Patients: register, get | Duplicate email → 409 |
| M4 | `HolidayCalendar` + slot generation + list + block | 28 slots generated; second attempt → 409; holiday → 422 |
| M5 | **Booking (rules A1–A4)** | All 7 checks from section 10 demonstrable |
| M6 | Cancel / complete / no-show + status rules | Every invalid transition → 409 |
| M7 | Notifications with profiles + `DevDataSeeder` | Fresh DB → app starts with demo data; booking prints a notification |
| M8 | `RequestIdFilter`, `RequestTimingFilter` | Every response has `X-Request-Id`; timing logs per request |
| M9 | Interceptors, `@RequiresRole`, `CurrentRequest`, ownership checks | Wrong key → 401, wrong role → 403, someone else's appointment → 403 |
| M10 | Aspects: `@RateLimited`, `@Auditable`, `@TrackExecutionTime`, `@Order`, audit endpoint | 6th booking in a minute → 429; failed booking shows `FAILURE` in audit log |
| M11 | README, Postman collection, demo video | Someone else can run it in 5 minutes |

---

## 12. Manual test checklist

- [ ] Create doctor with invalid email → 400 with `fieldErrors.email`
- [ ] Create two doctors with the same email → 409 `DUPLICATE_EMAIL`
- [ ] Generate slots for a doctor tomorrow → 28 slots
- [ ] Generate again → 409; generate on 2026-12-25 → 422 `CLINIC_HOLIDAY`
- [ ] Book a slot → 201; book the same slot again (other patient) → 409
- [ ] Book 3 slots on the same day → 3rd gives 422 `DAILY_LIMIT_REACHED`
- [ ] Book two doctors at the same time → 422 `TIME_CLASH`
- [ ] Hit book 6 times in a minute → 6th is 429
- [ ] Patient cancels an appointment 1 hour before → 422; receptionist can
- [ ] Cancelled slot shows `AVAILABLE` again
- [ ] Complete a cancelled appointment → 409 `INVALID_STATUS_TRANSITION`
- [ ] Patient 7 reads patient 8's appointments → 403
- [ ] No `x-api-key` → 401; `x-user-role: PATIENT` on generate slots → 403
- [ ] Audit log shows both SUCCESS and FAILURE rows with request ids
- [ ] Every response has `X-Request-Id`; logs show timing per request
- [ ] Deactivate a doctor with upcoming bookings → 409

---

## 13. Known limitations of v1 → your v2 roadmap

Put this section in your README. It shows you know what's missing, and it sets up the v2 post.

| v1 limitation | Why it matters | v2 fix (lecture) |
|---|---|---|
| Booking does slot update + appointment insert with **no transaction** | A crash between the two saves leaves a `BOOKED` slot with no appointment | `@Transactional` (L33) |
| Two patients booking the same slot at the same instant can both pass check #3 | Double booking under load | Transactions + isolation + DB unique constraint on `slot_id`; prove it with a 50-thread test (L34, L40) |
| A failed booking's audit row would roll back along with the booking once transactions exist | Lose the evidence of failures | Audit in `Propagation.REQUIRES_NEW` (L34) |
| Plain `Long` ids, copied columns | No joins; data can drift | `@ManyToOne` / `@OneToMany` / `@OneToOne` (L30) and `@EntityGraph` against N+1 (L31) |
| Lists return everything | Slow with thousands of rows | `Pageable`, `Page<T>`, sorting (L32) |
| Fake auth with headers | Anyone can claim any role | Spring Security, BCrypt, JWT (L35–38), Google login for patients (L39) |
| In-memory rate limiter | Resets on restart; doesn't work with 2 app instances | Discuss; Redis in the GateKeeper project is the real-world answer |
| No automated tests | Rules can silently break | JUnit + Mockito + `@WebMvcTest` (L40) |
| Reports need complex aggregation | JPA is awkward for reports | Daily no-show / bookings-per-doctor report with `JdbcTemplate` (L27) |

---

## 14. LinkedIn demo script (60–90 s screen recording)

1. Start the app → seeder creates doctors and tomorrow's slots.
2. List free slots for Dr. Mehra.
3. Patient books 10:15 → 201, show `X-Request-Id`.
4. Second patient books 10:15 → 409 `SLOT_NOT_AVAILABLE`.
5. Spam book → 429.
6. Patient tries to cancel too late → 422; receptionist cancels → slot is free again.
7. Open `/api/audit-logs` → SUCCESS and FAILURE rows, each with a request id.
8. Flash the `AppointmentService.book` method: three annotations, zero logging/timing/rate-limit code inside.

Post hook idea: *"My booking method has zero lines of logging, auditing or rate limiting —
yet all three happen on every call. Here's how I built my own annotations with Spring AOP."*
