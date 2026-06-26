# TiffinBox — Full Build Plan (build first, learn after)

> Approved build plan, copied into the project so it travels across machines.
> Companion: `CLAUDE.md` (project context), `docs/ROADMAP.md` (phase tracker).

## Context
TiffinBox digitizes a real WhatsApp-based tiffin business into a Spring Boot
microservices platform. It is a **resume project**; the goal is a working,
defensible system the user can explain in interviews. Approach: **build the whole
project first, then learn phase-by-phase by reading the codebase** (rigorous quizzes
deferred to after the build). Full reference design: `tiffin_microservices_engineering_plan.md`.

`eureka-server` is already built and boots (HTTP 200 on :8761).

## Locked decisions
- **Primary dev machine: Ubuntu** (16GB, Ryzen 3600x, RTX 2060 Super) — RAM, native Docker,
  Linux/prod parity, GPU for Ollama later. 8GB M2 Mac can't run the full stack comfortably.
- **Migration:** move project to Ubuntu (zip or, preferred, git + GitHub). Portable context
  in `CLAUDE.md`. The prior chat/memory/plan file do NOT travel — only project files do.
- **Scope:** all backend services + React frontend + deployment, built stepwise; each
  service verified (compiles + boots + registers with Eureka) before the next.
- **AI service: SKIPPED this pass.** No ai-service, no Ollama. Added later separately.
- **Kafka:** broker in a **Docker** container (native Docker on Ubuntu).
- **Project generation:** user generates each project **manually** from start.spring.io
  using the exact dependency list below.
- **Java 21** everywhere (Spring Boot 3.x is incompatible with Java 26). On Ubuntu install
  JDK 21, `JAVA_HOME` → Linux path (e.g. `/usr/lib/jvm/java-21-openjdk-amd64`).
- **Config: `application.properties`.** Independent Maven projects (no parent pom).
- **DB-per-service**: one local Postgres instance, one database per service.
- **JWT validated only at the Gateway**; services trust `X-User-Id` / `X-User-Role` headers.

## Machine migration & Ubuntu setup (FIRST)
1. `CLAUDE.md` written into project root (done) so context survives the move.
2. User moves project to Ubuntu (zip, or `git init` + GitHub then clone — git preferred).
3. User installs Claude Code on Ubuntu, logs in (same account), `cd TiffinBox`, runs `claude`.
4. Claude reads `CLAUDE.md` + `docs/` and resumes.
5. Ubuntu prereqs (Claude guides): JDK 21, Maven, PostgreSQL, Docker (native), Node.
   Verify `java -version` = 21 and `JAVA_HOME` correct.

## Services in this build (no AI)
eureka-server (done) · api-gateway · user-service · menu-service · payment-service ·
notification-service · order-service · subscription-service · frontend (React)

## Infra gates (the only points where the user must act)
- **G1 — Postgres running:** user starts the Postgres service. Claude then creates DBs via
  `psql`: `userdb, menudb, orderdb, paymentdb, notifdb, subscriptiondb`.
- **G2 — Docker installed:** user installs Docker; Claude runs the Kafka broker container
  (`confluentinc/cp-kafka`, KRaft mode) + creates the `order-events` topic.
- **G3 — Project generation (×8):** Claude gives exact start.spring.io settings + deps per
  service; user generates, unzips into `TiffinBox/<service>/`.
- **G4 — Gmail App Password:** user provides one for notification-service SMTP (when built).
- **Node:** already available — no gate for the frontend.

## start.spring.io dependency lists (per service)
All: Group `com.tiffinbox`, Artifact = service name, Package `com.tiffinbox.<svc>`,
Maven, Jar, Java **21**, Spring Boot **3.5.x** (not 4.x, not SNAPSHOT/M).

- **api-gateway:** Gateway (reactive, Spring Cloud Gateway) · Eureka Discovery Client.
  Manually add `jjwt` (api/impl/jackson) to pom for JWT validation.
- **user-service:** Spring Web · Spring Data JPA · PostgreSQL Driver · Spring Security ·
  Validation · Eureka Discovery Client · Lombok. Manually add `jjwt` to pom.
- **menu-service:** Spring Web · Spring Data JPA · PostgreSQL Driver · Validation ·
  Eureka Discovery Client · Lombok.
- **payment-service:** Spring Web · Spring Data JPA · PostgreSQL Driver · Validation ·
  Eureka Discovery Client · Lombok.
- **notification-service:** Spring Web · Spring Data JPA · PostgreSQL Driver · Validation ·
  Eureka Discovery Client · Lombok · Java Mail Sender · Spring for Apache Kafka.
- **order-service:** Spring Web · Spring Data JPA · PostgreSQL Driver · Validation ·
  Eureka Discovery Client · Lombok · OpenFeign · Resilience4j · Spring for Apache Kafka.
- **subscription-service:** Spring Web · Spring Data JPA · PostgreSQL Driver · Validation ·
  Eureka Discovery Client · Lombok · OpenFeign · Resilience4j.

## Build order (stepwise; each step = generate → Claude writes code → verify boots)
1. **api-gateway** — Eureka client + routes to all services by path prefix. Verify it
   registers with Eureka and proxies a route. (JWT filter added after user-service exists.)
2. **G1 Postgres + create all DBs.**
3. **user-service** — User/Address entities, register/login, BCrypt, JWT generation
   (`JwtService`), SecurityConfig. Verify register→login→JWT via curl.
4. **api-gateway AuthFilter** — validate JWT at the edge, forward `X-User-*` headers;
   public routes for login/register/menu-today. Verify a protected route via the Gateway.
5. **menu-service** — Menu/MenuItem entities, owner + customer endpoints, `@Scheduled`
   cutoff auto-close, internal Feign endpoints (`validate-items`, `is-open`).
6. **payment-service** — Payment entity (mock), create/get/mark-paid, summaries.
7. **G2 Docker + Kafka broker container + `order-events` topic.**
8. **notification-service** — Notification entity, `EmailService` (JavaMailSender, **G4**),
   REST trigger endpoints, **Kafka consumer** of `order-events` (idempotent).
9. **order-service** — Order/OrderItem entities (denormalized name/price), Feign clients
   (menu, payment) + Resilience4j fallbacks, full place-order flow with `@Transactional`
   boundaries, **Kafka producer** publishing `order-placed`, owner management + summary.
10. **subscription-service** — Subscription entities + CRUD, Feign clients (menu, order,
    notification), `AutoOrderScheduler` (`@Scheduled` daily), owner summary.
11. **frontend** — React 18 + Vite + Tailwind; Axios + JWT interceptor; AuthContext;
    customer pages (login/register, menu, cart, place order, history/tracking); owner
    dashboard (orders, status, payments, menu mgmt). Talks only to the Gateway.
12. **deployment** — Dockerfile per service + `docker-compose.yml` (eureka, gateway, all
    services, Postgres, Kafka, frontend); `.env.example`; GitHub Actions CI. Cloud target
    (Railway vs AWS) decided at this stage.

## Verification (end-to-end)
- Each service boots and appears on the Eureka dashboard (:8761).
- Auth: register → login → call a protected endpoint through the Gateway with the JWT.
- Order flow (Postman): place order → menu validated via Feign → payment record created →
  `order-placed` published → notification-service consumes → email sent.
- Resilience: stop menu-service → placing an order fast-fails via Resilience4j fallback.
- Subscription: create subscription → trigger scheduler → order auto-placed → email sent.
- Frontend: full customer + owner flows through the Gateway.
- Deployment: `docker-compose up --build` brings the whole stack up; smoke-test the flow.

## After the build — learning phase (deferred, not dropped)
Resume the explainer + **rigorous quiz** loop per `docs/ROADMAP.md`, reading the codebase
service by service. Quizzes stay hard (interview follow-ups); on a gap, pause for a deep-dive.

## Notes
- AI service intentionally excluded; revisit later (ai-service + Ollama, NL menu search).
- Deployment cloud target (Railway/AWS) deferred to step 12.
